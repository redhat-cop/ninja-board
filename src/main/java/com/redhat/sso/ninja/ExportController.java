package com.redhat.sso.ninja;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import com.redhat.sso.ninja.controllers.EventsController;
import com.redhat.sso.ninja.utils.Json;
import com.redhat.sso.ninja.utils.MapBuilder;

/**
 * Rest controller that exposes methods to export data from the system (events, scorecards etc..) in various formats (csv, json etc...)
 * @author mallen
 */

@Path("/")
public class ExportController{
  enum Format{
    csv,json,xls
  };
  
//  public static void main(String[] asd) throws IOException{
//    System.out.println(
//        new ExportController().exportScorecards(null, "xls").getEntity()
////        new ExportController().exportEvents(null,  "csv").getEntity()
//    );
//  }
  
  /* Comparator to order the header and fields of the exported data */
  public static class HeaderComparator implements Comparator<String>{
    List<String> order;
    public HeaderComparator(String[] orderOfItems){
      order=Lists.newArrayList(orderOfItems);
    }
    public int compare(String o1, String o2){
      if (!o1.equals(o2))
        return getOrder(o1)-getOrder(o2);
      return 0;
    }
    public int getOrder(String hdr){
      for(String orderMatcher:order){
        if (hdr.toLowerCase().contains(orderMatcher))
          return order.indexOf(orderMatcher);
      }
      return -1;
    }
  }
  
  
  // Admin UI: Used to export the list of events for support purposes
  @GET
  @Path("/events/export/{format}")
  public Response exportEvents(@Context HttpServletRequest request, @PathParam("format") String format) throws IOException{
    List<Map<String, String>> data=new EventsController().getAllEvents();
    
    Set<String> headerset=new HashSet<String>();
    for(Map<String, String> row:data)
      headerset.addAll(row.keySet());
    List<String> headers=new ArrayList<String>();
    headers.addAll(headerset);
    
    // Sort the data columns
    headers.sort(new HeaderComparator(new String[]{"timestamp","type","text","user"}));
    
    Map<String,String> dataHeaderMapping=new MapBuilder<String,String>().build();
    
    // export the data
    return writeExportFile("Events", format, headers, data, dataHeaderMapping);
  }
  
  
  // Admin UI: Used to export the list of user/scorecards support or reporting purposes
  @GET
  @Path("/scorecards/export/{format}")
  public Response exportScorecards(@Context HttpServletRequest request, @PathParam("format") String format) throws IOException{
    
    if (!EnumUtils.isValidEnum(Format.class, format)) throw new RuntimeException("format must be in "+Joiner.on(",").join(EnumUtils.getEnumList(Format.class)));
    
    
    List<String> headers=new ArrayList<String>();
    String jsonToOutput=(String)new ManagementController().getScorecards().getEntity();
    
    // Convert data back from datatables json format back to a flatter format for exporting
    Map<String,List<Map<String, Object>>> result=Json.newObjectMapper(true).readValue(jsonToOutput, new TypeReference<Map<String, List<Map<String, Object>>>>(){});
    List<Map<String, Object>> dataRaw=result.get("data");
    
    // Manage Columns (and their data field name)
    List<Map<String, Object>> columnsRaw=result.get("columns");
    Map<String,String> dataHeaderMapping=new HashMap<String, String>();
    for(Map<String,Object> column:columnsRaw){
      String header=(String)column.get("title");
      dataHeaderMapping.put(header, (String)column.get("data"));
      headers.add(header);
    }
    headers.add("id");
    
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
    
    Database2 db=Database2.get();
    for(Map<String,String> d:data){
    	Map<String, String> userInfo=db.getUsers().get(d.get("id"));
    	for(Entry<String, String> e:userInfo.entrySet()){
    		if (e.getKey().equals("email") || e.getKey().endsWith("Id")){
    			if (!headers.contains(e.getKey())) headers.add(e.getKey());
    			d.put(e.getKey(), userInfo.get(e.getKey()));
    		}    		
    	}
    }
    
    // Sort the data columns
    Collections.sort(headers, new HeaderComparator(new String[]{"id","name","email","belt","total","points","github","gitlab","trello","thought"}));
    
    // Sort the data rows by Total Points
    Collections.sort(data, new Comparator<Map<String,String>>(){
      public int compare(Map<String, String> o1, Map<String, String> o2){
        return Integer.parseInt(o2.get("total"))-Integer.parseInt(o1.get("total"));
      }
    });
    
    // export the data
    return writeExportFile("Scorecards", format, headers, data, dataHeaderMapping);
  }
  
  public Response writeExportFile(String type, String format, List<String> headers, List<Map<String,String>> data, Map<String,String> dataHeaderMapping ){
  	File file=new File(type+"."+format.toLowerCase());
  	try{
      switch(Format.valueOf(format)){
      case json:
        writeStringAndClose(Json.newObjectMapper(true).writeValueAsString(data), file);
        break;
        
      case csv:
        StringBuffer sb=new StringBuffer();
        
        for(String header:headers)
          sb.append(header).append(",");
        sb.append("\n");
        
        for(Map<String,String> e:data){
          for(String header:headers){
            String headerField=dataHeaderMapping.containsKey(header)?dataHeaderMapping.get(header):header;
            sb.append(e.containsKey(headerField)?e.get(headerField):"").append(",");
          }
          sb.append("\n");
        }
        writeStringAndClose(sb.toString(), file);
        break;
        
      case xls:
        HSSFWorkbook wb=new HSSFWorkbook();
        HSSFSheet s=wb.createSheet(type);
        
        HSSFRow row=s.createRow(0);
        for(int i=0;i<headers.size();i++){
          row.createCell(i, HSSFCell.CELL_TYPE_STRING).setCellValue(headers.get(i));
        }
        
        int rowCount=1;
        for(Map<String, String> e:data){
          row=s.createRow(rowCount);
          for(int i=0;i<headers.size();i++){
            String header=headers.get(i);
            String headerName=dataHeaderMapping.containsKey(header)?dataHeaderMapping.get(header):header;
            row.createCell(i, HSSFCell.CELL_TYPE_STRING).setCellValue(e.containsKey(headerName)?e.get(headerName):"");
          }
          rowCount=rowCount+1;
        }
        
        FileOutputStream fileOS=new FileOutputStream(file);
        wb.write(fileOS);
        fileOS.flush();
        fileOS.close();
        break;
      }
      return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM)
        .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"" ) //optional
        .build();
    }catch(Exception e){
      e.printStackTrace();
    }
  	return Response.serverError().build();
  }
  
  
  private File writeStringAndClose(String toWrite, File file) throws IOException{
    FileOutputStream fileOS=new FileOutputStream(file);
    IOUtils.write(toWrite, fileOS);
    IOUtils.closeQuietly(fileOS);
    return file;
  }
}
