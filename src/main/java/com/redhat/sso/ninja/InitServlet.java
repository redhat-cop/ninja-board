package com.redhat.sso.ninja;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

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