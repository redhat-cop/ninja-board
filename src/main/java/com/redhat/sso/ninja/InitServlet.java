package com.redhat.sso.ninja;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public class InitServlet extends HttpServlet {
	
  public static void main(String[] asd){
  }
	
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    Heartbeat2.start(Config.get());
  }
  
  @Override
  public void destroy() {
    super.destroy();
    Heartbeat2.stop();
  }

}
