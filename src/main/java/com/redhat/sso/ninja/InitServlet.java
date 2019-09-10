package com.redhat.sso.ninja;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.util.concurrent.TimeUnit;

public class InitServlet extends HttpServlet {
	
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    GoogleDrive2.initialise();
    Heartbeat2.start(Config.get());
    Backup.start(TimeUnit.DAYS.toMillis(1), Database2.STORAGE_AS_FILE.getAbsolutePath());
  }
  
  @Override
  public void destroy() {
    super.destroy();
    Heartbeat2.stop();
    Backup.stop();
  }

}