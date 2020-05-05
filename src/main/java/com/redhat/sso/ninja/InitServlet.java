package com.redhat.sso.ninja;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public class InitServlet extends HttpServlet {
	
  public static void main(String[] asd){
    GoogleDrive3.initialise("/home/%s/google_drive", com.redhat.sso.ninja.GoogleDrive3.DriverType.gdrive, "v2.1.1PreRelease");
  }
	
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    GoogleDrive3.initialise("/home/%s/google_drive", com.redhat.sso.ninja.GoogleDrive3.DriverType.gdrive, "v2.1.1PreRelease");
    Heartbeat2.start(Config.get());
  }
  
  @Override
  public void destroy() {
    super.destroy();
    Heartbeat2.stop();
  }

}
