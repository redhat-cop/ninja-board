package com.redhat.cop.giveback;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.cop.giveback.legacy.Config;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class Initialization {
	
  private static Logger log=LoggerFactory.getLogger(Initialization.class);
  public static final String DATE_FORMAT="yyyy-MM-dd'T'HH:mm:ss";
  public static final SimpleDateFormat sdf=new SimpleDateFormat(DATE_FORMAT);
  
  public void onStartup(@Observes StartupEvent ev) {
    if (log.isInfoEnabled()) log.info("Starting up...");
    if (log.isInfoEnabled()) log.info(String.format("%s Giveback startup", sdf.format(new Date())));
    Heartbeat2.start(Config.get());
  }
  
  void onShutdown(@Observes ShutdownEvent e) {
    if (log.isInfoEnabled()) log.info("Shutting down...");
    if (log.isInfoEnabled()) log.info(String.format("%s Giveback shutdown", sdf.format(new Date())));
    Heartbeat2.stop();
  }
  

}
