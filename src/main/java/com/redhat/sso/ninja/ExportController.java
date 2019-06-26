package com.redhat.sso.ninja;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.codehaus.jackson.type.TypeReference;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.redhat.sso.ninja.utils.Json;

@Path("/")
public class ExportController{

	enum Format{
		csv,json,xls
	};
	
	public static void main(String[] asd) throws IOException{
		System.out.println(
				new ExportController().export(null, "xls").getEntity()
		);
	}
	
	List<String> order = Lists.newArrayList("id","name","belt","total","points","github","gitlab","trello");
	public int getHeaderOrder(String hdr){
		for(String orderMatcher:order){
			if (hdr.toLowerCase().contains(orderMatcher)){
				return order.indexOf(orderMatcher);
			}
		}
		return -1;
	}
	
  @GET
  @Path("/scorecards/export/{format}")
  public Response export(@Context HttpServletRequest request, @PathParam("format") String format) throws IOException{
    
  	if (!EnumUtils.isValidEnum(Format.class, format)) throw new RuntimeException("format must be in "+Joiner.on(",").join(EnumUtils.getEnumList(Format.class)));
  	
  	
  	List<String> headers=new ArrayList<String>();
  	String jsonToOutput=(String)new ManagementController().getScorecards().getEntity();
  	
  	// Convert data back from datatables json format back to a flatter format for exporting
  	Map<String,List<Map<String, Object>>> result=Json.newObjectMapper(true).readValue(jsonToOutput, new TypeReference<Map<String, List<Map<String, Object>>>>(){});
  	List<Map<String, Object>> dataRaw=result.get("data");
  	
  	// Manage Columns (and their data field name)
  	List<Map<String, Object>> columnsRaw=result.get("columns");
  	Map<String,String> columnFieldName=new HashMap<String, String>();
  	for(Map<String,Object> column:columnsRaw){
  		String header=(String)column.get("title");
  		columnFieldName.put(header, (String)column.get("data"));
  		headers.add(header);
  	}
  	headers.add("id");

  	// Sort the data columns
  	Collections.sort(headers, new Comparator<String>(){
			public int compare(String o1, String o2){
				if (!o1.equals(o2)){
					return getHeaderOrder(o1)-getHeaderOrder(o2);
				}
				return 0;
			}
		});
  	
  	// Normalize the data into a single map/row
  	// flatten the data from a Map of String->Object to String->String
  	List<Map<String,String>> data=new ArrayList<Map<String,String>>();
  	for(Map<String,Object> dataItem:dataRaw){
  		Map<String,String> entry=new HashMap<String, String>();
  		for(Entry<String, Object> die:dataItem.entrySet()){
  			entry.put(die.getKey(), String.valueOf(die.getValue()));
  		}
  		data.add(entry);
  	}
  	
  	// Sort the data rows by Total Points
  	Collections.sort(data, new Comparator<Map<String,String>>(){
			public int compare(Map<String, String> o1, Map<String, String> o2){
				int i1=Integer.parseInt(o1.get("total"));
				int i2=Integer.parseInt(o2.get("total"));
				return i2-i1;
			}
		});
  	
  	
  	// export the data
    File file=new File("export."+format.toLowerCase());
    FileOutputStream fileOS=new FileOutputStream(file);
  	try{
      switch(Format.valueOf(format)){
      case json:
        IOUtils.write(Json.newObjectMapper(true).writeValueAsString(data), fileOS);
        IOUtils.closeQuietly(fileOS);
        return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM)
            .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"" ) //optional
            .build();
        
      case csv:
        StringBuffer sb=new StringBuffer();
        
        for(String header:headers)
        	sb.append(header).append(",");
        sb.append("\n");
        
        for(Map<String,String> e:data){
        	for(String header:headers){
        		String headerName=columnFieldName.containsKey(header)?columnFieldName.get(header):header;
        		sb.append(e.containsKey(headerName)?e.get(headerName):"").append(",");
        	}
          sb.append("\n");
        }
        IOUtils.write(sb.toString(), fileOS);
        IOUtils.closeQuietly(fileOS);
        return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM)
            .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"" ) //optional
            .build();

      case xls:
	      HSSFWorkbook wb=new HSSFWorkbook();
	      HSSFSheet s=wb.createSheet("Scorecards");
	      
	      HSSFRow row=s.createRow(0);
	      for(int i=0;i<headers.size();i++){
	      	row.createCell(i, HSSFCell.CELL_TYPE_STRING).setCellValue(headers.get(i));
	      }
	      
	      int rowCount=1;
        for(Map<String,String> e:data){
        	row=s.createRow(rowCount);
      		for(int i=0;i<headers.size();i++){
      			String header=headers.get(i);
        		String headerName=columnFieldName.containsKey(header)?columnFieldName.get(header):header;
        		row.createCell(i, HSSFCell.CELL_TYPE_STRING).setCellValue(e.containsKey(headerName)?e.get(headerName):"");
        	}
      		rowCount=rowCount+1;
        }
	      
	      wb.write(fileOS);
	      fileOS.flush();
	      fileOS.close();
        return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM)
            .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"" ) //optional
            .build();
        }
      }catch(Exception e){
        e.printStackTrace();
      }
      return Response.serverError().build();
    }

}
