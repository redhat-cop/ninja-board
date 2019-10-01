package com.redhat.sso.ninja;

/**
 * 
 * V3 is for Team Drive compatibility
 * 
 * For this we need to move from odeke-em's drive, to gdrive-org/gdrive + a customized version of...
 * 
 * 
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.attribute.PosixFilePermission;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.codehaus.jackson.type.TypeReference;

import com.redhat.sso.ninja.utils.DownloadFile;
import com.redhat.sso.ninja.utils.Json;
import com.redhat.sso.ninja.utils.RegExHelper;


public class GoogleDrive3 {
  private static Logger log=Logger.getLogger(GoogleDrive3.class);
  
  public static String DEFAULT_EXECUTABLE="/home/%s/drive_linux";
//  public static final String DEFAULT_PULL_COMMAND=DEFAULT_EXECUTABLE+" pull -export xls -quiet=true --id %s"; //worked with 0.3.1
  public static String DEFAULT_PULL_COMMAND=DEFAULT_EXECUTABLE+" pull -export xls -no-prompt --id %s"; // 0.3.7+ changed its output that we parse
  public static String DEFAULT_WORKING_FOLDER="/home/%s/google_drive";
  private static String gdriveType;
  private long cacheExpiryInMs;
  private static Map<String,File> cache=new HashMap<String, File>();
  private static Map<String,Long> cacheExpiry=new HashMap<String, Long>();
  
  public static String getDefaultExecutable(){
    return String.format(DEFAULT_EXECUTABLE, System.getProperty("user.name"));
  }
  
  public GoogleDrive3(){
    this.cacheExpiryInMs=-1;
  }
  public GoogleDrive3(long cacheExpiryInMs){
    this.cacheExpiryInMs=cacheExpiryInMs;
  }
  
//  private SimpleDateFormat dateFormatter;
//	public void setDateFormatter(SimpleDateFormat dateFormatter){
//		this.dateFormatter=dateFormatter;
//	}
  
  interface HeaderRowFinder{
  	public int getHeaderRow(XSSFSheet s);
  }
  
  enum DriverType{drive,gdrive}
  
  public static void main(String[] args) throws Exception{
  	String PortfolioDatabase="1aPR0_uNRJCVLT9c8mqfEpNvQ2FZBdcD9pL0u6mksu2U";
  	String FeedbackResponsesTeamDrive="1cyVtpYUMW26JBoqbVJJTD79ay--6F7EMAllEVKesn1Q";
  	
  	
  	// Test using odeke's drive (no team drive capability)
//  	GoogleDrive3.initialise("/home/%s/drive_linux_odeke", DriverType.drive, "v0.3.9");
  	
  	// Test using gdrive (with team drive capability)
  	GoogleDrive3.initialise("/home/%s/google_drive", DriverType.gdrive, "v2.1.1PreRelease");
  	
  	GoogleDrive3 gd=new GoogleDrive3();
  	File file=gd.downloadFile(FeedbackResponsesTeamDrive);
  	System.out.println("file exists="+(file!=null?file.exists():"Nope!"));
  	List<Map<String, String>> test=gd.parseExcelDocument(file, new HeaderRowFinder(){
			public int getHeaderRow(XSSFSheet s){
				return 0;
			}
		}, new SimpleDateFormat("dd-MM-yyyy"));
  	System.out.println(test);
  }
  
  public static void initialise(String workingFolder, DriverType type, String version) {
  	try{
  		
  		// load the config
  		String cfgString=IOUtils.toString(GoogleDrive3.class.getClassLoader().getResourceAsStream("GoogleDrive3_initialize.json"));
  		Map<String,Map<String,String>> cfgs=Json.newObjectMapper(true).readValue(cfgString, new TypeReference<Map<String, Map<String,String>>>(){});
  		Map<String,String> cfg=cfgs.get(type+"/"+version+"/"+getOS());
  		
  		if (null==cfg) throw new RuntimeException("Unable to find config for: "+(type+"/"+version+"/"+getOS()));
  		
  		String exe=RegExHelper.extract(cfg.get("url"), ".+/(.+)$");
  		gdriveType=type.name();
  		DEFAULT_WORKING_FOLDER=String.format(workingFolder, System.getProperty("user.name"));
  		DEFAULT_EXECUTABLE=new File(new File(DEFAULT_WORKING_FOLDER, gdriveType).getAbsolutePath(), exe).getAbsolutePath();
  		DEFAULT_PULL_COMMAND=cfg.get("commandTemplate");
  		
  		
	    if (!new File(GoogleDrive3.getDefaultExecutable()).exists()){
	    	File credsFile=null;
	    	try{
		    	
	    		// attempt to download the binary
	    		log.info("Downloading '"+type+"/"+version+"' from: "+cfg.get("url"));
	    		new DownloadFile().get(cfg.get("url"), new File(DEFAULT_WORKING_FOLDER, gdriveType), PosixFilePermission.OTHERS_EXECUTE);
	    		
	    		// set the creds file location
	    		credsFile=new File(String.format(cfg.get("credentialsLocation"), System.getProperty("user.name")));
	    		
	    		log.info("Deploying credentials.json in: "+credsFile);
	    		credsFile.getParentFile().mkdirs();
	        InputStream is=GoogleDrive3.class.getClassLoader().getResourceAsStream("/gd_credentials.json");
	        if (null!=is){
	        	log.info("... from internal classloader path of '/gd_credentials.json'");
	        	IOUtils.copy(is, new FileOutputStream(credsFile));
	        }else if (null!=System.getenv("GD_CREDENTIALS")){
	        	log.info("... from env variable 'GD_CREDENTIALS'");
	        	IOUtils.write(System.getenv("GD_CREDENTIALS").getBytes(), new FileOutputStream(credsFile));
	        }else{
	        	log.error("no gdrive creds specified in either resources, or system props");
	        }
	        log.info("drive credentials file contains: "+IOUtils.toString(new FileInputStream(credsFile)));
	    		
	    		
	    	}catch(IOException e){
	        System.err.println("Failed to initialise gdrive and/or credentials, cleaning up exe and creds");
	        if (null!=credsFile) credsFile.delete();
	        new File(GoogleDrive3.getDefaultExecutable()).delete();
	        e.printStackTrace();
	    	}
	    }else{
	      log.info("gdrive already initialised. Existing binary is here: "+GoogleDrive3.getDefaultExecutable());
	    }
	    
  	}catch(IOException e){
  		log.error("Initialization failed: "+e.getMessage());
  		e.printStackTrace();
  	}
  }
  
  
  public synchronized File downloadFile(String fileId) throws IOException, InterruptedException {
    
  	if (null==gdriveType) throw new RuntimeException("Not yet Initialized");
  	
  	if (cacheExpiryInMs>0){
  		if (cache.containsKey(fileId) && cacheExpiry.get(fileId)>System.currentTimeMillis()){
  			System.out.println("downloadFile():: returning cached copy for "+fileId);
  			return cache.get(fileId);
  		}else{
  			cache.remove(fileId);
  			cacheExpiry.remove(fileId);
  		}
  	}
  	
  	
  	String command = String.format(DEFAULT_PULL_COMMAND, DEFAULT_EXECUTABLE, fileId);
  	
    String googleDrivePath=String.format(DEFAULT_WORKING_FOLDER, System.getProperty("user.name"));
    File workingFolder=new File(googleDrivePath, fileId);
    System.out.println(this.getClass().getName()+"::downloadFile() - Downloading google file: "+fileId + " [workingFolder="+workingFolder.getAbsolutePath()+"]");
    workingFolder.mkdirs(); // just in case it's not there
//    System.out.println(this.getClass().getName()+"::downloadFile() - Using working folder: "+workingFolder.getAbsolutePath());
//    System.out.println(this.getClass().getName()+"::downloadFile() - Downloading google file: "+fileId);
    System.out.println(this.getClass().getName()+"::downloadFile() - Command: "+command);
    
    if (googleDrivePath.contains("google")){
//    	FileUtils.deleteDirectory(workingFolder);
      recursivelyDelete(workingFolder, new File(DEFAULT_EXECUTABLE).getName());
    }else
      System.out.println(this.getClass().getName()+"::downloadFile() - Not cleaning working folder unless it contains the name 'google' - for safety reasons");
    
    
    Process exec = Runtime.getRuntime().exec(command, null, workingFolder);
    
    exec.waitFor();
    String syserr = IOUtils.toString(exec.getErrorStream());
    String sysout = IOUtils.toString(exec.getInputStream());
    System.out.println(this.getClass().getName()+":: sysout=\""+sysout.replaceAll("\\n", " ")+"\"; syserr=\""+syserr+"\"");
    
    
    if (gdriveType.equals("drive")){
    	if (!sysout.contains("Resolving...") && !sysout.contains("Everything is up-to-date"))
    		throw new RuntimeException("Error running google drive script: " + sysout);
    	if (!sysout.contains("Everything is up-to-date")) {
    		// System.out.println("Do Nothing");
    		// return null;
    		// } else {
    		Pattern p = Pattern.compile("to '(.+)'$");
    		Matcher matcher = p.matcher(sysout);
    		if (matcher.find()){
    			String preFilePath = matcher.group(1);
    			// System.out.println("Process the file: " + preFilePath);
    			File result=new File(workingFolder, preFilePath);
    			if (cacheExpiryInMs>0){
    				cache.put(fileId, result);
    				cacheExpiry.put(fileId, System.currentTimeMillis()+cacheExpiryInMs);
    			}
    			return result;
    		}
    	}
    }
    
    if (gdriveType.equals("gdrive")){
    	if (!sysout.contains("Exported"))
    		throw new RuntimeException("Error running google drive script: " + sysout);
  		Pattern p = Pattern.compile("'(.+?)'");
  		Matcher matcher = p.matcher(sysout);
  		if (matcher.find()){
  			String preFilePath = matcher.group(1);
  			File result=new File(workingFolder, preFilePath);
  			if (cacheExpiryInMs>0){
  				cache.put(fileId, result);
  				cacheExpiry.put(fileId, System.currentTimeMillis()+cacheExpiryInMs);
  			}
  			return result;
  		}
    }
    
    
    return null;
    // System.out.println(exec.exitValue());
  }
  
  private static String getOS(){
  	if (System.getProperty("os.name").indexOf("win")>0) return "windows";
  	if (System.getProperty("os.name").indexOf("mac")>0) return "mac";
  	if (System.getProperty("os.name").indexOf("nux")>0 || System.getProperty("os.name").indexOf("nix")>0) return "linux";
  	return null;
  }
  
  
  public int getHeaderRow(XSSFSheet s){
//  	// example to search for a row where in column 0 there is text "State"
//  	return SheetSearch.get().col(0).text("State").find(s).getRowIndex();
    return 0;
  }
  
  public boolean valid(Map<String,String> entry){
    return true;
  }
  
  static class SheetSearch{
//  	private int col;
//  	private String text;
  	private XSSFSheet sheet;
  	public SheetSearch(XSSFSheet sheet){
  		this.sheet=sheet;
  	}
  	static public SheetSearch get(XSSFSheet sheet){
  		return new SheetSearch(sheet);
  	}
//  	public SheetSearch col(int col){ this.col=col; return this; }
//  	public SheetSearch text(String text){ this.text=text; return this; }
//  	public SheetSearch sheet(XSSFSheet sheet){ this.sheet=sheet; return this; }
  	
  	public XSSFCell find(String text){
  		return find(0, text);
  	}
  	public XSSFCell find(int col, String text){
  		
  		for(int iRow=0;iRow<=sheet.getLastRowNum();iRow++){
  			XSSFCell cell=sheet.getRow(iRow).getCell(col);
  			
  			if (cell.getCellType()==Cell.CELL_TYPE_STRING){
//  				if (text.equals(cell.getStringCellValue())){
					if (cell.getStringCellValue().matches(text)){
  					return cell;
  				}
  			}
  		}
  		return null;
  	}
  }
  
  public List<Map<String,String>> parseExcelDocument(File file, HeaderRowFinder finder, SimpleDateFormat dateFormatter) throws FileNotFoundException, IOException{
  	return parseExcelDocument(file, null, finder, dateFormatter);
  }
  public List<Map<String,String>> parseExcelDocument(File file, String sheetName, HeaderRowFinder finder, SimpleDateFormat dateFormatter) throws FileNotFoundException, IOException{
    // parse excel file using apache poi
    // read out "tasks" and create/update solutions
    // use timestamp (column A) as the unique identifier (if in doubt i'll hash it with the requester's username)
    List<Map<String,String>> entries=new ArrayList<Map<String,String>>();
    FileInputStream in=null;
    if (file==null || !file.exists()) return new ArrayList<Map<String,String>>();
    try{
    	System.out.println("GoogleDrive3::parseExcelDocument() - file is "+file.getAbsolutePath() +" (exists="+file.exists()+", size="+(file.length()/1024)+"k)");
      in=new FileInputStream(file);
      XSSFWorkbook wb=new XSSFWorkbook(in);
      
      int sheetIndex=0;
      if (null!=sheetName) sheetIndex=wb.getSheetIndex(sheetName);
      
      if (sheetIndex<0) throw new RuntimeException("Unable to find sheet with name '"+sheetName+"'");
      
      XSSFSheet s=wb.getSheetAt(sheetIndex);
      int maxColumns=20;
//      int headerRow=getHeaderRow(s);
      int headerRow=finder.getHeaderRow(s);
      
      for(int iRow=headerRow+1;iRow<=s.getLastRowNum();iRow++){
//        Map<String,String> e=new HashMap<String,String>();
        Map<String,String> e2=new HashMap<String,String>();
        boolean allRowCellsEmpty=true;
        for(int iColumn=0;iColumn<=maxColumns;iColumn++){
          if (s.getRow(headerRow).getCell(iColumn)==null) continue;
          String header=s.getRow(headerRow).getCell(iColumn).getStringCellValue();
          XSSFRow row = s.getRow(iRow);
          if (row==null) break; // next line/row
          XSSFCell cell=row.getCell(iColumn);
          if (cell==null) continue; // try next cell/column
          
            try{
            	switch(cell.getCellType()){
            	case Cell.CELL_TYPE_NUMERIC: 
            		if (HSSFDateUtil.isCellDateFormatted(cell)){
            			e2.put(header, null!=dateFormatter?dateFormatter.format(cell.getDateCellValue()):cell.getDateCellValue().toString());
//            			System.out.println("parseExcepDocument():: formatting date to -> "+e2.get(header));
            			break;
            		}else{
            			e2.put(header, String.valueOf(cell.getNumericCellValue())); break;
            		}
            	case Cell.CELL_TYPE_BOOLEAN: e2.put(header, String.valueOf(cell.getBooleanCellValue())); break;
            	case Cell.CELL_TYPE_FORMULA: 
            		// detect URL's, else fall back to string value
            		if (cell.getCellFormula().contains("HYPERLINK")){
            			Pattern p=Pattern.compile("\"(.+?)\".*\"(.+?)\"");
            			Matcher m=p.matcher(cell.getCellFormula());
            			if (m.find()){
            				String url=m.group(1);
            				String name=m.group(2);
            				e2.put(header, name+"|"+url); break;
            			}
            		}// else drop to default
            			
            		
//            		e2.put(header, cell.getCellFormula()); break;
            	default: 
            		String value=null;
            		try{ value=cell.getStringCellValue();
            		}catch(Exception e){
            			value=cell.getRawValue();
            		}
            		if (value!=null && !"".equals(value))
            			e2.put(header, value);
            		break;
            	}
            	
            	allRowCellsEmpty=allRowCellsEmpty && (e2.get(header)==null || "".equals(e2.get(header)));
            }catch(Exception ex){}
            if (!e2.containsKey(header))
              try{
                e2.put(header, cell.getDateCellValue().toString());
              }catch(Exception ex){}
          
        }
        
        if (allRowCellsEmpty) break;
        
        if (valid(e2)){
          e2.put("ROW_#", String.valueOf(iRow-1));
          entries.add(e2);
        }
      }
    }finally{
      IOUtils.closeQuietly(in);
    }
    
    System.out.println("GoogleDrive3::parseExcelDocument() - leaving method, file "+file.getAbsolutePath() +" is "+(file.length()/1024)+"k in size");
    
    return entries;
  }
  
  private void recursivelyDelete(File file, String excluding){
    for(File f:file.listFiles()){
      if (!f.getName().startsWith(".") && f.isDirectory())
        recursivelyDelete(f, excluding);
      if (!f.getName().startsWith(".") && !f.getName().equals(excluding)){
        System.out.println("recursivelyDelete():: deleting file: "+f.getAbsolutePath());
      	f.delete();
      }
    }
  }
  
}
