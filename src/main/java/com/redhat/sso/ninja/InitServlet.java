package com.redhat.sso.ninja;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.attribute.PosixFilePermission;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.io.IOUtils;

import com.redhat.sso.ninja.utils.DownloadFile;

public class InitServlet extends HttpServlet {
	
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    
    String intervalString=(String)Config.get().getOptions().get("heartbeat.interval");
    if (null==intervalString) intervalString="60000";
    int interval=Integer.parseInt(intervalString);
    boolean heartbeatDisabled="true".equalsIgnoreCase((String)Config.get().getOptions().get("heartbeat.disabled"));
    
    System.out.println("Heartbeat:");
    System.out.println("  Disabled: "+heartbeatDisabled);
    System.out.println("  Interval: "+interval +" (seconds)");
    
    System.out.println("Initialise some dependencies (hopefully will be able to replace these someday):");
    if (!new File(GoogleDrive2.getDefaultExecutable()).exists()){
      // attempt to download it
      try{
        String url="https://github.com/odeke-em/drive/releases/download/v0.3.9/drive_linux";
        
        System.out.println("Downloading gdrive from: "+url);
        new DownloadFile().get(url, new File(GoogleDrive2.getDefaultExecutable()).getParentFile(), PosixFilePermission.OTHERS_EXECUTE);
        
        File credsFile=new File(new File(GoogleDrive2.getDefaultWorkingFolder(), ".gd"), "credentials.json");
        credsFile.getParentFile().mkdirs();
        System.out.println("Deploying credentials.json in: "+credsFile);
        IOUtils.copy(getClass().getResourceAsStream("/gd_credentials.json"), new FileOutputStream(credsFile));
      }catch(Exception e){
        System.out.println("Failed to initialise gdrive and/or credentials");
        e.printStackTrace();
      }
    }
    
//    System.out.println("Initalise the scripts (again, hopefully remove the need for this someday)");
//    try{
//      // copy the default scripts over to where they can be executed
//      IOUtils.copy(Config.class.getClassLoader().getResourceAsStream("scripts/github-stats.py"), new FileOutputStream("github-stats.py"));
//      IOUtils.copy(Config.class.getClassLoader().getResourceAsStream("scripts/trello-stats.py"), new FileOutputStream("trello-stats.py"));
//      
//      Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
//      perms.add(PosixFilePermission.OWNER_READ);
//      perms.add(PosixFilePermission.OWNER_WRITE);
//      perms.add(PosixFilePermission.OWNER_EXECUTE);
//      Files.setPosixFilePermissions(new File("github-stats.py").toPath(), perms);
//      Files.setPosixFilePermissions(new File("trello-stats.py").toPath(), perms);
//      
//    }catch(IOException e){
//      
//    }

    
    
    if (!heartbeatDisabled)
      Heartbeat2.start(interval);
    
    
//    CamelContext ctx=new DefaultCamelContext();
//    new RouteBuilder(ctx) {
//      @Override
//      public void configure() throws Exception {
//        from("direct:track")
//        .to("")
//        ;
//      }
//    }.addRoutesToCamelContext(ctx);;
    
    
  }

  @Override
  public void destroy() {
    super.destroy();
    Heartbeat2.stop();
  }

}