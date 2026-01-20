package com.redhat.cop.giveback.google;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
//import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mvel2.MVEL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.redhat.cop.giveback.Config;
import com.redhat.services.portfolio.utils.Cache;
import com.redhat.services.portfolio.utils.Json;
import com.redhat.services.portfolio.utils.MapBuilder;
import com.redhat.services.portfolio.utils.Metrics;
import com.redhat.services.portfolio.utils.RegExHelper;
import com.redhat.services.portfolio.utils.SyncLazyBlocker;
import com.redhat.services.portfolio.utils.TimeUtils;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;

/**
 * Provide ability to download google sheets, parse, filter and cache them, whilst the downloading features are separated into the GoogleDrive class.
 */
public class GoogleSheetReader3{
	private static final Logger log=LoggerFactory.getLogger(GoogleSheetReader3.class);
//	public static volatile Cache<String, List<Map<String, Object>>> cache=Cache.newCache("gsr2-data", sensibleStringToMs(Config.get().getPropertySilent("CACHE_EXPIRY", valueOf(sensibleStringToMs("1d")))), false);
	private volatile Cache<String, List<Map<String, Object>>> cache;
	private GoogleDrive42 drive;
	public GoogleSheetReader3(GoogleDrive42 v, Cache<String, List<Map<String, Object>>> cache){this.drive=v;this.cache=cache; if (cache==null) cache=Cache.newCache(this.getClass().getSimpleName(), 0, false); }
	
  public static interface HeaderRowFinder{
    public int getHeaderRow(XSSFSheet s);
  }
  public static class SheetSearch{
    private XSSFSheet sheet;
    public SheetSearch(XSSFSheet sheet){this.sheet=sheet;}
    static public SheetSearch get(XSSFSheet sheet){return new SheetSearch(sheet);}
    public XSSFCell find(String text){return find(0, text);}
    public XSSFCell find(int col, String text){
      for (int iRow=0;iRow<=sheet.getLastRowNum();iRow++){
        XSSFCell cell=sheet.getRow(iRow).getCell(col);
        if (cell==null)
          log.error(String.format("Likely issue in sheet[%s] at row [%s] col [%s]", sheet.getSheetName(), iRow, col));
        if (cell!=null && cell.getCellType()==CellType.STRING){
          if (cell.getStringCellValue().matches(text)) return cell;
        }
      }
      return null;
    }
  }
	public static class ParserConfig{
		protected int columns;                   public ParserConfig columns(String v){this.columns=Integer.parseInt(v); return this;} public ParserConfig columns(int v){this.columns=v; return this;}
		protected String filters;                public ParserConfig filters(String v){this.filters=v; return this;}
		protected SimpleDateFormat formatter;    public ParserConfig formatter(String v){if (null!=v) this.formatter=new SimpleDateFormat(v); return this;}
		protected int headerRow;                 public ParserConfig headerRow(String v){if (null!=v) this.headerRow=Integer.parseInt(v); return this;}
		protected int headerRowSearchCol;        public ParserConfig headerRowSearchCol(String v){if (null!=v) this.headerRowSearchCol=Integer.parseInt(v); return this;} public ParserConfig headerRowSearchCol(int v){this.headerRowSearchCol=v; return this;}
		protected String headerRowSearchText;    public ParserConfig headerRowSearchText(String v){this.headerRowSearchText=v; return this;}
		public HeaderRowFinder buildFinder(){ // If we have the Search Text, use it, otherwise assume the headerRow is what should be used
			if (StringUtils.isNotBlank(headerRowSearchText))
				return new HeaderRowFinder(){@Override public int getHeaderRow(XSSFSheet s){return SheetSearch.get(s).find(headerRowSearchCol, headerRowSearchText).getRowIndex();}};
			return new HeaderRowFinder(){@Override public int getHeaderRow(XSSFSheet s){return headerRow;}};
		}
	}
	private SyncLazyBlocker blocker=new SyncLazyBlocker();
//	@Context private UriInfo uri;
	/**
	 * Concepts: 
	 *  - thread blocking (there will be multiple requests simultaneously) on the sheet/file, so only 1 thread can read the sheet at once. ie. so Config will have to wait for Solutions
	 *  - if cache empty, get the entire sheet (unfiltered) and put in cache
	 *  - use cache unfiltered data, filter to solution and apply filters, and put in cache
	 *  - return filtered data
	 *  - next time the file doesnt need to be re-read because the unfiltered data in in cache already
	 */
	public List<Map<String, Object>> read(String sheetId, String sheetName, String solution, String token, UriInfo uri, ParserConfig c) throws FileNotFoundException, IOException{
		String cacheKey=String.format("%s/%s%s/%s%s",sheetId.substring(0,6),sheetName,(solution!=null?"/"+solution:""),c.columns,(c.filters!=null?"/"+c.filters:""));
		String blockerKey=String.format("%s",sheetId.substring(0,6));
		
		synchronized (blocker.get(cacheKey)){
  		if (cache.hasExpired(cacheKey)) {
  			cache.cleanupExpiredItems();
//  			System.out.println(Thread.currentThread().getName()+"_"+this.getClass().getSimpleName()+".read["+cacheKey+"] - cache MISS!! keys are: "+Json.toJsonSafe(cache.get().keySet()));
  			String unfilteredDataCacheKey=String.format("%s/%s/%s",sheetId.substring(0,6),sheetName,c.columns); // ie. all lines in a sheet, not just a single solution or filtered
  			synchronized (blocker.get(blockerKey)){
    			if (cache.hasExpired(unfilteredDataCacheKey)) {
//    				System.out.println(Thread.currentThread().getName()+"-"+this.getClass().getSimpleName()+".read["+cacheKey+"] - cache MISS on "+allDataCacheKey+"!! keys are: "+Json.toJsonSafe(cache.get().keySet()));
    				File f=drive.downloadGoogleSheet(sheetId);
//    				System.out.println(Thread.currentThread().getName()+"-"+this.getClass().getSimpleName()+".read["+cacheKey+"] - parsing ALL file into data map");
    				cache.put(unfilteredDataCacheKey, parseExcelDocument(f, sheetName, c.buildFinder(), c.formatter, c.columns));
//    				System.out.println(Thread.currentThread().getName()+"_"+this.getClass().getSimpleName()+".read["+cacheKey+"] - sync ended. added ALL file to cache. cache keys now = "+Json.toJsonSafe(cache.get().keySet()));
    			}
  			}
  			List<Map<String, Object>> data=cache.get(unfilteredDataCacheKey);
  			data=filterSolution(data, solution);
//  			data=filterUserPrivileges(data, sheetName, token==null?Lists.newArrayList():AuthFilter2.getUser(token,uri).getRoles());
  			data=applyFilters(data, c.filters, null);
  			cache.put(cacheKey, data);
//  			System.out.println(Thread.currentThread().getName()+"_"+this.getClass().getSimpleName()+".read["+cacheKey+"] - sync ended. added file to cache. cache keys now = "+Json.toJsonSafe(cache.get().keySet()));
  		}else {
//  			System.out.println(Thread.currentThread().getName()+"_"+this.getClass().getSimpleName()+".read["+cacheKey+"] - cache HIT!!");
  		}
		}
		return cache.get(cacheKey);
	}
	
	
	
	public List<Map<String,Object>> parseExcelDocument(File file, String sheetName, ParserConfig c) throws FileNotFoundException, IOException{
		return parseExcelDocument(file, sheetName, c.buildFinder(), c.formatter, c.columns);
	}
	/**
	 * Basic reading of an excel file using apache poi library into a List of Maps that can be converted to Json later
	 * @param file - as received by downloading a google sheet using GoogleDriveXX implementation
	 * @param sheetName - sheet/tab name to read
	 * @param finder - how to determine where the column definitions end, and data rows start
	 * @param dateFormatter - formatter for date type fields
	 * @param maxColumns - how many columns to read
	 */
	protected List<Map<String,Object>> parseExcelDocument(File file, String sheetName, HeaderRowFinder finder, SimpleDateFormat dateFormatter, int maxColumns) throws FileNotFoundException, IOException{
	  Metrics t=new Metrics("SheetParsing - "+file.getName().substring(0,5)+"/"+sheetName);
	  List<Map<String,Object>> entries=new ArrayList<Map<String,Object>>();
		FileInputStream in=null;
		if (file==null || !file.exists()) return new ArrayList<Map<String,Object>>();
		long startMs=System.currentTimeMillis();
		boolean trackMetrics=Boolean.parseBoolean(Config.get().getProperty("google.sheet.parser.metrics", "false"));
		if (trackMetrics) t.start();
		XSSFWorkbook wb=null;
		try{
			log.debug("parseExcelDocument():: file is "+file.getAbsolutePath() +" (exists="+file.exists()+", size="+file.length()+"bytes)");
			in=new FileInputStream(file);
			wb=new XSSFWorkbook(in);
			if (trackMetrics) t.store("1 - finished reading streams");
			Map<String,String> linkReferences=new HashMap<>(); // this map contains cell references that reference cells containing hyperlinks that we want to transfer to the source formula cell
			int sheetIndex=0;
			if (null!=sheetName) sheetIndex=wb.getSheetIndex(sheetName);
			if (trackMetrics) t.store("2 - found sheet index");
			if (sheetIndex<0) throw new RuntimeException("Unable to find sheet with name '"+sheetName+"' in file '"+file.getAbsolutePath()+"'");
			
			//EFFICIENCIES
			List<String> headerIgnoreList=Lists.newArrayList("SFDC","Change Highlight","Related Products");
			List<String> weKnowThisShouldAlwaysBeString=Lists.newArrayList("Type","State","Section","SubSection","Asset Format","Pillar","Campaign","Sales Play","Sales Tactics","Latest Release","Target Persona","Geo");
			
			XSSFSheet s=wb.getSheetAt(sheetIndex);
			int headerRow=finder.getHeaderRow(s);
			if (trackMetrics) t.store("3 - got sheet & found "+s.getLastRowNum()+" rows");
			int metricsRowsMAX=300;
			int metricsRows=metricsRowsMAX;
//			String rowParsedAs="";
			long rowsStart=System.currentTimeMillis();
			for(int iRow=headerRow+1;iRow<=s.getLastRowNum();iRow++){
				Map<String,Object> e2=new HashMap<String,Object>();
				long irowCounter=System.currentTimeMillis();
				boolean allRowCellsEmpty=true;
				for(int iColumn=0;iColumn<=maxColumns;iColumn++){
					if (s.getRow(headerRow).getCell(iColumn)==null) continue;
					String header=s.getRow(headerRow).getCell(iColumn).getStringCellValue();
					if (headerIgnoreList.contains(header)) continue; // EFFICIENCY - ignore any columns that we know don't make it to the UI
					XSSFRow r=s.getRow(iRow);
					if (Objects.isNull(r)) break; // next line/row
					XSSFCell cell=r.getCell(iColumn);
					if (Objects.isNull(cell)) continue; // try next cell/column
					
					String parsedAs="";
					String value=null;

	        try{
  					if (cell.getCellType().equals(CellType.BLANK)) continue;//value=null;
            if (weKnowThisShouldAlwaysBeString.contains(header)){
              try{
                value=cell.getStringCellValue();
                parsedAs="STRING";
              }catch (Exception e){
                value=cell.getRawValue();
                parsedAs="RAW";
              }
            }
            
            
            if (Objects.isNull(value) && cell.getCellType().equals(CellType.STRING)){
              XSSFHyperlink hlink=cell.getHyperlink();
              if (Objects.nonNull(hlink)) {
                value=cell.getStringCellValue()+"|"+hlink.getAddress();
                parsedAs="HYPERLINK";                 
              }else{
                try{
                  value=cell.getStringCellValue();
                  parsedAs="STRING";
                }catch(Exception e) {
                  value=cell.getRawValue();
                  parsedAs="RAW";
                }
              }
            }
  					if (Objects.isNull(value) && cell.getCellType().equals(CellType.NUMERIC)){
  					  if (DateUtil.isCellDateFormatted(cell)){
  					    value=null!=dateFormatter?dateFormatter.format(cell.getDateCellValue()):cell.getDateCellValue().toString();
  					    parsedAs="DATE";
  					  }else {
  					    value=String.valueOf(cell.getNumericCellValue());
  					    parsedAs="NUMERIC";
  					  }
  					}
  					if (Objects.isNull(value) && cell.getCellType().equals(CellType.BOOLEAN)){
  					  value=String.valueOf(cell.getBooleanCellValue());
              parsedAs="BOOLEAN";
  					}
  					if (Objects.isNull(value) && cell.getCellType().equals(CellType.FORMULA)){
  					  String cellFormula=cell.getCellFormula();
  					  String link=readFormulaToHyperlink(cell);
  					  if (Objects.nonNull(link)) linkReferences.put(cellFormula, link);
  					  
  					  if (cellFormula.matches("([a-zA-Z]+!{1})*[A-Z]+[0-9]+")){
                CellReference ref=new CellReference(cellFormula);
                Row refRow=!cellFormula.contains("!")?s.getRow(ref.getRow()):wb.getSheet(ref.getSheetName()).getRow(ref.getRow());
                Map<String, String> readReference=readReference(cell, ref, refRow);
                linkReferences.putAll(readReference);
                parsedAs="FORMULA -> CELL_REFERENCE1";
              }
  					  
  					  if (cell.getCellType().equals(CellType.FORMULA) && linkReferences.containsKey(cell.getCellFormula())) {
                value=linkReferences.get(cell.getCellFormula());
                parsedAs="FORMULA -> CELL_REFERENCE2 ["+parsedAs+"]";      
              }
  					}
  					
//  					if (StringUtils.isBlank(value)){
//  					  try{
//  					    // get hyperlink if you can
//  					    XSSFHyperlink hlink=cell.getHyperlink();
//  					    if (hlink!=null) {
//  					      parsedAs="HYPERLINK";                 
//  					      value=cell.getStringCellValue()+"|"+hlink.getAddress();
//  					    }
//  					  }catch(Exception exx){
//  					    System.out.println("ERROR - DONT WANT THIS!! cell.getHyperlink - "+exx.getMessage());
//  					  }
//  					}
//  					
//  					if (StringUtils.isBlank(value)){
//    					try{
//                value=cell.getStringCellValue();
//                parsedAs="STRING2";
//              }catch(Exception e){
//                value=cell.getRawValue();
//                parsedAs="RAW2";
//              }
//  					}
  					
  					// now we have a value, write it to the row/map
  					if (StringUtils.isNotBlank(value)) e2.put(header, value);
  					
  					///////////////////////
						
//						allRowCellsEmpty=allRowCellsEmpty && (e2.get(header)==null || "".equals(e2.get(header)));
						allRowCellsEmpty=allRowCellsEmpty && StringUtils.isEmpty((String)e2.get(header));
					}catch(Exception ex){
					  ex.printStackTrace();
					}
					
//					if (!e2.containsKey(header)) // ie. if it doesnt already contain a value for the header, assume it's a Date cell
//						try{
////						  System.out.println("does this happen?  hdr="+header); 
//						  if (!cell.getCellType().equals(CellType.STRING))
//						    e2.put(header, cell.getDateCellValue().toString());
//						}catch(Exception ex){
//						  System.out.println("exception - does this happen? [iRow="+iRow+", iCol="+iColumn+", type="+cell.getCellType().name()+", header="+header+"]"+ex.getMessage());
//						}
					
          if (trackMetrics && metricsRows>0)
            t.store(String.format("[iRow=%-4s,iCol=%-2s] hdr=%-20s, type=%s, parsedAs=%s, value=%s",iRow,iColumn,header,cell.getCellType().name(),parsedAs,value));
          if (trackMetrics) metricsRows=Math.max(metricsRows-1,0);
				}
				if (trackMetrics && metricsRows>0)
				  t.store("row["+iRow+"] took", System.currentTimeMillis()-irowCounter);
				
				if (allRowCellsEmpty) break;
				
				e2.put("ROW_#", String.valueOf(iRow-1));
				entries.add(e2);
			}
			if (trackMetrics) t.store("4 - finished parsing all the rows "+s.getLastRowNum()+"", TimeUtils.msToSensibleString(System.currentTimeMillis()-rowsStart));
		}finally{
			IOUtils.closeQuietly(in);
			if (null!=wb) wb.close();
		}
		
		log.debug("parseExcelDocument() - leaving method, file "+file.getAbsolutePath() +" is "+(file.length()/1024)+"k in size, took "+(System.currentTimeMillis()-startMs)+"ms to run");
		
		if (trackMetrics) System.out.println("Parsing metrics = "+Json.toJson(t));
		
		return entries;
	}
	
	/** Used by the above function only */
	private Map<String,String> readReference(XSSFCell originCell, CellReference ref, Row row){
		if (row!=null){
			XSSFCell refCell=(XSSFCell)row.getCell(ref.getCol());
			try{ return new MapBuilder<String,String>().put(originCell.getCellFormula(), readFormulaToHyperlink(refCell)).build();
			}catch(Exception sinkAndIgnore){}
		}
		return MapBuilder.newHashMap();
	}
	
	/** Used by the above function only */
	private static Pattern extractor=Pattern.compile("\"(.+)\".*\"(.+)\"");
	private String readFormulaToHyperlink(XSSFCell cell){
		if (null!=cell.getHyperlink()) return cell.getStringCellValue()+"|"+cell.getHyperlink().getAddress();
		
		if (cell.getCellType().equals(CellType.FORMULA) && cell.getCellFormula().contains("HYPERLINK")){
			Matcher m=extractor.matcher(cell.getCellFormula());
			if (m.find()) return String.format("%s|%s", m.group(2),m.group(1));
		}
		return null;
	}
	
	/** filtering of data provided by the parseDocument method */
	public List<Map<String,Object>> filterSolution(List<Map<String,Object>> parsedDocument, String optionalSolution) {
		if (null!=optionalSolution){ // if a solution ID is specified then just return that solution and it's documents
			List<Map<String, Object>> db2=Lists.newArrayList();
			boolean startCapturing=false;
//			int countBefore=parsedDocument.size();
			for (Map<String,Object> o: parsedDocument){
//  			if (null==o.get("Name")) continue;
//  			!((String)o.get("Name")).contains("|")?o.get("Name").toLowerCase().replaceAll(" ","_"):"";
				
				String id=o.get("Name")==null?"":RegExHelper.extract((String)o.get("Name"), "([^\\/]+)$", 1);
				if (null==id) continue; // regex failed, so we need to move to the next solution url/name/ID
				boolean isDocument="".equals(id);
//				if (!startCapturing && !isDocument) System.out.println("gsheetraw:: solution id we're looking for (foundinDB="+id+", searchingForParam="+optionalSolution+")");
				if (startCapturing && !isDocument) {
//					System.out.println("gsheetraw:: FOUND "+db2.size()+" items");
					break; // hit the next solution so quit it!
				}
				if (id.equals(optionalSolution)) { // found the solution? - lets start grabbing rows!
//					System.out.println("gsheetraw:: FOUND! -> "+id);
					startCapturing=true;
				}
				if (startCapturing) {
//					System.out.println("gsheetraw:: START=true, adding o -> "+id);
					db2.add(o);
				}
			}
			parsedDocument=db2.size()<=0?parsedDocument:db2; // overwrite the full database with the shortened list
//  		log.info(String.format("getGSheetRaw()::solution specified [%s] truncating parsed data from %s -> %s - %s", optionalSolution, countBefore, parsedDocument.size(), cacheKey));
		}
		return parsedDocument;
	}
	
//	/** filtering of data provided by the parseDocument method */
//	public List<Map<String,Object>> filterUserPrivileges(List<Map<String,Object>> parsedDocument, String sheetName, List<String> userRoles) {
//		if (null==userRoles || userRoles.size()<=0) return parsedDocument;
//			
//			// remove any content intended for NAPS if user doesnt have NAPS role
//		if (/* null!=email && */sheetName.toLowerCase().contains("presales")){ // if "filter-options", we dont need to check role info, only for content searches
//			// this "presales" text check could be added to the mvel expression for easier configuration at a later date
//			
//			String protectedContentExpression=Config.get().getProperty("protected_content", "Geo contains 'NAPS'");
//			String role=RegExHelper.extract(protectedContentExpression, "'(.+)'", 1);
//			boolean userDoesntHaveSpecifiedRole=Collections.disjoint(Lists.newArrayList(role), userRoles); // True if nothing from #1 exists in #2
//			if (userDoesntHaveSpecifiedRole){ // then strip out any content associated with that role. ie. NAPS content can only be seen by NAPS folks
//				parsedDocument=Lists.newArrayList(Iterables.filter(parsedDocument, new Predicate<Map<String,Object>>(){
//					@Override public boolean apply(Map<String,Object> input){
//						try{
//							return "false".equalsIgnoreCase(MVEL.eval(protectedContentExpression, input).toString()); // externalize how it evaluates the security in case we need to change it based on the sheet that's read in
//						}catch(Exception sink){
//							return false;
//						}
////								return input.containsKey("Geo") && !((String)input.get("Geo")).toLowerCase().contains("naps");
//					}
//				}));
//			}
//			
//		}
////		else
////			log.debug(String.format("getGSheetRaw():: [%s] token is NULL! no roles info can be found, so no security it being applied", cacheKey));
//		
//		return parsedDocument;
//	}
	
	/* Example: www.whatever.com/api/integration/search?type=Solutions&filters=NotEmpty(Name);HasStates(Geo) 
	 This method instantiated the NotEmpty/HasStates classes and passes the parameters (Name or Geo) to execute them as predicates to the list of documents
	 */
  @SuppressWarnings({"rawtypes","unchecked"})
  public List<Map<String,Object>> applyFilters(List<Map<String,Object>> parsedDocument, String filters, Metrics timings){
//    System.out.println(String.format("applyFilters:: filters:%s, # input lines: %s",filters, parsedDocument.size());
    if (StringUtils.isNotBlank(filters)){
      String regexWithParams="([a-zA-Z]+)\\((.+)\\)$"; // gets all the params in a string, predicate splits them
      //String regexWithParams="([a-zA-Z]+)\\(([a-zA-Z0-9,]+)\\)$";
      String regexWithoutParams="([a-zA-Z]+)$";
      String regexWithblankParams="([a-zA-Z]+)\\(\\)$";
      for(String filter:Splitter.on(";").omitEmptyStrings().trimResults().split(filters)){
        int orgSize=parsedDocument.size();
        Predicate filterInstance=null;
        if (filter.matches(regexWithParams)){ // characters + param in brackets
          String filterName=RegExHelper.extract(filter, regexWithParams, 1);
          String param=RegExHelper.extract(filter, regexWithParams, 2);
          try{
            Class filterClazz=Class.forName(GooglePredicates.class.getName()+"$"+filterName);
            Constructor constructor=filterClazz.getDeclaredConstructor(String.class);
            filterInstance=(Predicate)constructor.newInstance(param);
          }catch(ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e){ /*e.printStackTrace();*/ }
          
        }else if (filter.matches(regexWithoutParams) || filter.matches(regexWithblankParams)){ // OR just all characters with no param
          String filterName=RegExHelper.extract(filter, "([a-zA-Z]+)", 1);
          try{
            Class filterClazz=Class.forName(GooglePredicates.class.getName()+"$"+filterName);
            filterInstance=(Predicate)filterClazz.newInstance();
          }catch(ClassNotFoundException | InstantiationException | IllegalAccessException e){ /*e.printStackTrace();*/ }
        }
        if (filterInstance!=null) {
          parsedDocument=Lists.newArrayList(Iterables.filter(parsedDocument, filterInstance));
          if (timings!=null) timings.store("filters_"+filterInstance.getClass().getSimpleName(), String.format("reduced items from %s to %s",orgSize, parsedDocument.size()));
        }else {
          log.error("ignoring invalid filter - "+filter);
        }
        
      }
    }
  //  System.out.println("applyFilters:: # lines output: "+parsedDocument.size());
    return parsedDocument;
  }
  
}
