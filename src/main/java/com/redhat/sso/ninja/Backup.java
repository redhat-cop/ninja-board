package com.redhat.sso.ninja;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class Backup {
  private static final Logger log = Logger.getLogger(Backup.class);
  private static Timer t;

  public static void main(String[] asd){
    try{
      Backup.runOnce();
    }catch(Exception e){
      e.printStackTrace();
    }
  }
  
  public static void runOnce(){
    new BackupRunnable(new String[]{Database2.STORAGE_AS_FILE.getAbsolutePath()}).run();
  }
  
  public static void start(long intervalInMs, String... paths) {
    t = new Timer(Backup.class.getSimpleName()+"-timer", false);
    t.scheduleAtFixedRate(new BackupRunnable(paths), 30000l, intervalInMs);
  }

  public static void stop() {
    t.cancel();
  }
  
  static class BackupRunnable extends TimerTask {
//    static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
    private String[] paths;
    
    public BackupRunnable(String[] paths){
      this.paths=paths;
    }
    
    @Override
    public void run() {
      log.info(Backup.class.getSimpleName()+ " fired");
      
      for(String path:paths){
        File source=new File(path);
        String newName=FilenameUtils.getBaseName(source.getName())+"-"+sdf.format(new Date())+"."+FilenameUtils.getExtension(source.getName());
        File newFile=new File(source.getParentFile(), newName);
        
        try{
          System.out.println("Copying from ["+source.getAbsolutePath()+"] to ["+newFile.getAbsolutePath()+"]");
          IOUtils.copy(new FileInputStream(source), new FileOutputStream(newFile));
        }catch(Exception e){
          e.printStackTrace();
        }
        
      }
      
    }      
  }

}
