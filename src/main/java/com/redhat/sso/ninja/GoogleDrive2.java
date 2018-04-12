package com.redhat.sso.ninja;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class GoogleDrive2 {
  
  public static final String DEFAULT_EXECUTABLE="/home/%s/drive";
  public static final String DEFAULT_PULL_COMMAND=DEFAULT_EXECUTABLE+" pull -export xls -quiet=true --id %s";
  public static final String DEFAULT_WORKING_FOLDER="/home/%s/google_drive";
  
  private String googleSheetPullCommand;
  private String googleSheetWorkingFolder;
  
  public static String getDefaultExecutable(){
    return String.format(DEFAULT_EXECUTABLE, System.getProperty("user.name"));
  }
  public static String getDefaultWorkingFolder(){
    return String.format(DEFAULT_WORKING_FOLDER, System.getProperty("user.name"));
  }
  
  public GoogleDrive2(){
    this.googleSheetPullCommand=DEFAULT_PULL_COMMAND;
    this.googleSheetWorkingFolder=DEFAULT_WORKING_FOLDER;
  }
  public GoogleDrive2(String googleSheetPullCommand, String googleSheetWorkingFolder){
    this.googleSheetPullCommand=googleSheetPullCommand;
    this.googleSheetWorkingFolder=googleSheetWorkingFolder;
  }

  public File downloadFile(String fileId) throws IOException, InterruptedException {
    String command = String.format(googleSheetPullCommand, System.getProperty("user.name"), fileId);
    
    String googleDrivePath=String.format(googleSheetWorkingFolder, System.getProperty("user.name"));
    File workingFolder=new File(googleDrivePath);
    workingFolder.mkdirs(); // just in case it's not there
    System.out.println("Using working folder: "+workingFolder.getAbsolutePath());
    System.out.println("Downloading google file: "+fileId);
    System.out.println("Command: "+command);
    
    if (googleDrivePath.contains("google")){
      recursivelyDelete(workingFolder);
    }else
      System.out.println("Not cleaning working folder unless it contains the name 'google' - for safety reasons");
    
    Process exec = Runtime.getRuntime().exec(command, null, workingFolder);
    
//    Process exec = Runtime.getRuntime().exec(command, null, new File("/home/mallen/Work/google_drive"));
    exec.waitFor();
    String syserr = IOUtils.toString(exec.getErrorStream());
    String sysout = IOUtils.toString(exec.getInputStream());
    System.out.println("sysout=\""+sysout+"\"; syserr=\""+syserr+"\"");
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
        return new File(preFilePath);
      }
    }
    return null;
    // System.out.println(exec.exitValue());
  }
  
  public int getHeaderRow(){
    return 0;
  }
  
  public boolean valid(Map<String,String> entry){
    return true;
  }
  
  public List<Map<String,String>> parseExcelDocument(File file) throws FileNotFoundException, IOException{
    // parse excel file using apache poi
    // read out "tasks" and create/update solutions
    // use timestamp (column A) as the unique identifier (if in doubt i'll hash it with the requester's username)
    List<Map<String,String>> entries=new ArrayList<Map<String,String>>();
    FileInputStream in=null;
    try{
      in=new FileInputStream(file);
      XSSFWorkbook wb=new XSSFWorkbook(in);
      XSSFSheet s=wb.getSheetAt(0);
      int maxColumns=20;
      
      for(int iRow=getHeaderRow()+1;iRow<=s.getLastRowNum();iRow++){
        Map<String,String> e=new HashMap<String,String>();
        for(int iColumn=0;iColumn<=maxColumns;iColumn++){
          if (s.getRow(getHeaderRow()).getCell(iColumn)==null) continue;
          String header=s.getRow(getHeaderRow()).getCell(iColumn).getStringCellValue();
          XSSFRow row = s.getRow(iRow);
          if (row==null) break; // next line/row
          XSSFCell cell=row.getCell(iColumn);
          if (cell==null) continue; // try next cell/column
          
            try{
              e.put(header, cell.getStringCellValue());
            }catch(Exception ex){}
            if (!e.containsKey(header))
              try{
                e.put(header, cell.getDateCellValue().toString());
              }catch(Exception ex){}
          
        }
        
        if (valid(e)){
          e.put("ROW_#", String.valueOf(iRow));
          entries.add(e);
        }
      }
    }finally{
      IOUtils.closeQuietly(in);
    }
    return entries;
  }
  
  private void recursivelyDelete(File file){
    for(File f:file.listFiles()){
      if (!f.getName().startsWith(".") && f.isDirectory())
        recursivelyDelete(f);
      if (!f.getName().startsWith("."))
        f.delete();
    }
  }
  
}
