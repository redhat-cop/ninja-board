package com.redhat.sso.ninja;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.attribute.PosixFilePermission;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.commons.io.IOUtils;

import com.redhat.sso.ninja.utils.DownloadFile;

public class InitServlet extends HttpServlet {
	
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
//    Summarizer.start();
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
        System.out.println("Deploying credentials.json in: "+credsFile);
        IOUtils.copy(getClass().getResourceAsStream("/gd_credentials.json"), new FileOutputStream(credsFile));
      }catch(Exception e){
        System.out.println("Failed to initialise gdrive and/or credentials");
        e.printStackTrace();
      }
    }
    
    
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
//    Summarizer.stop();
    Heartbeat2.stop();
  }

}