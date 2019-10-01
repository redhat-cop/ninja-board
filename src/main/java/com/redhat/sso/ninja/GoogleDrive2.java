package com.redhat.sso.ninja;

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

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.redhat.sso.ninja.utils.DownloadFile;

public class GoogleDrive2 {
  private static Logger log=Logger.getLogger(GoogleDrive2.class);
	
  public static final String DEFAULT_EXECUTABLE="/home/%s/drive_linux";
//  public static final String DEFAULT_PULL_COMMAND=DEFAULT_EXECUTABLE+" pull -export xls -quiet=true --id %s"; //worked with 0.3.1
  public static final String DEFAULT_PULL_COMMAND=DEFAULT_EXECUTABLE+" pull -export xls -no-prompt --id %s"; // 0.3.7+ changed its output that we parse
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
  	SimpleDateFormat sdf=new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
  	
    // parse excel file using apache poi
    // read out "tasks" and create/update solutions
    // use timestamp (column A) as the unique identifier (if in doubt i'll hash it with the requester's username)
    List<Map<String,String>> entries=new ArrayList<Map<String,String>>();
    FileInputStream in=null;
    if (file==null || !file.exists()) return new ArrayList<Map<String,String>>();
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
                e.put(header, sdf.format(cell.getDateCellValue()));
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
  
  public static void initialise(){ //ie. download gdrive executable if necessary
    if (!new File(GoogleDrive2.getDefaultExecutable()).exists()){
      // attempt to download it
    	File credsFile=new File(new File(GoogleDrive2.getDefaultWorkingFolder(), ".gd"), "credentials.json");
      try{
        String url="https://github.com/odeke-em/drive/releases/download/v0.3.9/drive_linux";
        
        log.info("Downloading gdrive from: "+url);
        new DownloadFile().get(url, new File(GoogleDrive2.getDefaultExecutable()).getParentFile(), PosixFilePermission.OTHERS_EXECUTE);
        
        credsFile.getParentFile().mkdirs();
        log.info("Deploying credentials.json in: "+credsFile);
        
        InputStream is=GoogleDrive2.class.getClassLoader().getResourceAsStream("/gd_credentials.json");
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
        
      }catch(Exception e){
        System.out.println("Failed to initialise gdrive and/or credentials, cleaning up exe and creds");
        credsFile.delete();
        new File(GoogleDrive2.getDefaultExecutable()).delete();
        e.printStackTrace();
      }
    }else{
      log.info("gdrive already initialised. Existing binary is here: "+GoogleDrive2.getDefaultExecutable());
    }
  }
  
}
