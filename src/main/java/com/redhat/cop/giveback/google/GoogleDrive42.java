package com.redhat.cop.giveback.google;

import static com.redhat.services.portfolio.utils.StringUtils.shorten;
import static com.redhat.services.portfolio.utils.TimeUtils.sensibleStringToMs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.common.base.Preconditions;
import com.redhat.cop.giveback.Config;
import com.redhat.cop.giveback.Initialization;
import com.redhat.services.portfolio.utils.Cache;
import com.redhat.services.portfolio.utils.ChatNotification_v2;
import com.redhat.services.portfolio.utils.ChatNotification_v2.ChatEvent;
import com.redhat.services.portfolio.utils.IOUtils2;
import com.redhat.services.portfolio.utils.SyncLazyBlocker;
import com.redhat.services.portfolio.utils.TimeUtils;

/**
 * Provides the ability to use the Google API to export a google sheet into an xlsx file ready for parsing
 */
public class GoogleDrive42{
	private static final Logger log=LoggerFactory.getLogger(GoogleDrive42.class);
	private static final JsonFactory JSON_FACTORY=GsonFactory.getDefaultInstance();
	private String lastCreds=null;
	private Drive driveService;
	protected boolean noCache=false;
	protected int maxRetries=Integer.parseInt(Config.get().getProperty("google.drive.retries","2"));
	private static volatile Cache<String, File> cache=Cache.newCache("gdrive42-data", sensibleStringToMs(Config.get().getPropertySilent("CACHE_EXPIRY_GOOGLE", "CACHE_EXPIRY", "1m")), false);
	private static long GOOGLE_DRIVE_TIMEOUT=TimeUtils.sensibleStringToMs(Config.get().getProperty("google.drive.timeout","25s"));
	
	private SyncLazyBlocker blocker=new SyncLazyBlocker();
	private GoogleDrive42(){}
	
	public static class Builder extends GoogleDrive42{
		protected String credentials; public Builder credentials(String v){this.credentials=v;return this;}; public String getCredentials(){return this.credentials;}
//		public Builder noCache(boolean v){this.noCache=v;return this;};
		public Builder maxRetries(int v){this.maxRetries=v;return this;};
		
		/**Get credentials from either GOOGLE_SERVICE_ACCOUNT_CREDS or GOOGLE_SERVICE_ACCOUNT_PATH */
		public Builder credsFromConfig(){
			credentials=Config.get().getProperty("GOOGLE_SERVICE_ACCOUNT_CREDS");
			String credsFilePath=Config.get().getProperty("GOOGLE_SERVICE_ACCOUNT_PATH");
	  	if (credentials==null && credsFilePath!=null) credsFromFile(credsFilePath);
		if (credentials==null/* && credsFilePath==null */) log.warn("Unable to find Google credentials. Both these env vars are empty!! 'GOOGLE_SERVICE_ACCOUNT_CREDS' or 'GOOGLE_SERVICE_ACCOUNT_PATH'");
	  	return this;
		}
		public Builder credsFromFile(String filePath){
			try{credentials=IOUtils2.toStringAndClose(new FileInputStream(filePath));}catch(IOException e){e.printStackTrace();}
			return this;
		}
		public GoogleDrive42 build(){
			Preconditions.checkArgument(null!=credentials, "Google credentials cannot be empty. You must configure either GOOGLE_SERVICE_ACCOUNT_CREDS or GOOGLE_SERVICE_ACCOUNT_PATH");
			GoogleDrive42 r=new GoogleDrive42(credentials, maxRetries/* , noCache */);
			return r;
		}
	}
	
	public static void clearCache(){ log.info(String.format("Clearing %s items from %s FileCache", cache.get().size(), cache.getName())); cache.clear(); }
	
	private GoogleDrive42(String credentials, int maxRetries/* , boolean noCache */){
  	boolean reInitDriveService=driveService==null || (!credentials.equals(lastCreds));
		if (reInitDriveService){
			try{
//				log.info("GoogleDrive4:: Identifying google credentials from {}", AuthFilter2.shorten(creds,20,10));
				GoogleCredentials gcredentials=ServiceAccountCredentials.fromStream(new ByteArrayInputStream(credentials.getBytes())).createScoped(Collections.singleton(DriveScopes.DRIVE_READONLY)); // DriveScopes.DRIVE_READONLY is referencing best practices for security
				driveService=new Drive.Builder(
  				    GoogleNetHttpTransport.newTrustedTransport(), 
  				    JSON_FACTORY, 
  				    new HttpCredentialsAdapter(gcredentials)
				    ).setApplicationName(Initialization.applicationName)
				    .setHttpRequestInitializer(new HttpRequestInitializer(){
				      @Override
				      public void initialize(HttpRequest request) throws IOException {
				        new HttpCredentialsAdapter(gcredentials).initialize(request);
				        request.setConnectTimeout((int)GOOGLE_DRIVE_TIMEOUT);
				        request.setReadTimeout((int)GOOGLE_DRIVE_TIMEOUT);
			        }
				    })
				    .build();
				log.info("Google Drive Service initialized (creds={})", shorten(lastCreds=credentials,20,10));
			}catch(IOException | GeneralSecurityException ex){
				log.error("We are expecting the Google Service Account path to have the json content {}", credentials, ex);
			}
		}else {
			log.debug("re-using driveService");
		}
		this.maxRetries=maxRetries;
//		this.noCache=noCache;
	}
	
	/** [Cached] Download google sheet to default location /tmp/google_drive/${sheetId}.xlsx */
	public File downloadGoogleSheet(String sheetId){
		File googleDriveDir=new File(Config.get().getProperty("HOME","/tmp"), "google_drive");
		File googleSheetFile=new File(googleDriveDir, sheetId+".xlsx"); // /tmp/${name}/google_drive/${sheetID}.xlsx
		
		String cacheKey=String.format("%s",sheetId.substring(0,6));
		synchronized (blocker.get(cacheKey)){
			if (noCache || cache.hasExpired(cacheKey)){
//  			System.out.println(this.getClass().getSimpleName()+".downloadGoogleSheet - started sync block on "+cacheKey);
//				if (!noCache) System.out.println(this.getClass().getSimpleName()+".downloadGoogleSheet["+cacheKey+"] File cache MISS - keys are: "+Json.toJsonSafe(cache.get().keySet()));
			  File f=downloadGoogleSheet(sheetId, googleSheetFile);
				cache.put(cacheKey, f);
//				System.out.println(this.getClass().getSimpleName()+".downloadGoogleSheet - added file to cache. ended sync block. cache keys now = "+Json.toJsonSafe(cache.get().keySet()));
  		}
		}
//			System.out.println(this.getClass().getSimpleName()+".downloadGoogleSheet - cache HIT!");
		return cache.get(cacheKey);
	}
	
	boolean failedPreviously=false;
	private File cleanupAndMoveAsBackup(File destination){
		// remove all potential old backups, so when we rename the current file we dont have a filename clash
		try {
			File[] backups=destination.getParentFile().listFiles(new FilenameFilter(){@Override public boolean accept(File dir, String name){ return name.endsWith("bak");}});
			if (null!=backups && backups.length>0)
				for (File bak:backups) Files.delete(Path.of(bak.getAbsolutePath()));
		}catch(IOException ignoreAndHopeForTheBest) {
			ignoreAndHopeForTheBest.printStackTrace();
		}
//			bak.delete();
		File bakFile=new File(destination.getParentFile(), destination.getName()+".bak");
		if (destination.exists() && destination.length()>0){ destination.renameTo(bakFile);} // backup in case download fails
		return bakFile;
	}
	/** Download google sheet to specified location */
	protected File downloadGoogleSheet(String sheetId, File destination){
		File bak=cleanupAndMoveAsBackup(destination);
		
		if (destination.exists()) log.warn("Request to download google file to {}, exists={} (expected: false), size={} (expected: 0)", destination.getAbsolutePath(), destination.exists(), destination.length());
		try{
    	destination.getParentFile().mkdirs();
    	int retries=0;
    	while (destination.length()<=0 && retries<maxRetries){
    		if (destination.exists()) destination.delete();
    		if (destination.length()<=0) destination.createNewFile();
    		
    		try (OutputStream fileOutputStream=new FileOutputStream(destination)) {
    			driveService.files().export(sheetId, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet").executeMediaAndDownloadTo(fileOutputStream);
    			log.info("Successfully downloaded sheet to {} (size={}kb)", destination.getAbsolutePath(), (destination.length()/1024));
    			bak.delete();
    			if (failedPreviously==true) {
    				failedPreviously=false;
    				ChatNotification_v2.get().send(ChatEvent.onError, Initialization.applicationName, "False alarm.. google drive download failed last time, but succeeded this time");
    			}
    			return destination;
    		} catch (GoogleJsonResponseException e){
    			log.error("Unable to get file due to an issue with the json key file", e);
    		} catch (SocketTimeoutException e){
    		  log.error("Unable to get file due a socket timeout", e);
    		} catch (IOException e){
    			log.error("Was not able to download the Google Sheet. retry count="+retries, e);
    		}
    		retries=retries+1;
    		try{Thread.sleep(1000l);}catch(Exception e){}
    	}
    	
      log.error("unknown issue. retry count={}. retries exhausted", retries);
		}catch(IOException e){
			log.error("Download didnt work", e);
		}
		// got to here? then download failed, so restore bakup and return that file
		failedPreviously=true;
		ChatNotification_v2.get().send(ChatEvent.onError, Initialization.applicationName, "google drive download failed, returning backup for now.");
		destination.delete();
		bak.renameTo(destination);
		long checkFileSizeUntil=System.currentTimeMillis()+3000; // try for 3 seconds 
		while(destination.length()<=0 && (System.currentTimeMillis()<checkFileSizeUntil)) {
			try{ Thread.currentThread().sleep(100); }catch(InterruptedException e){ e.printStackTrace(); }
		}
		if (destination.length()<=0) throw new RuntimeException(String.format("File %s was 0bytes after downloading and checking for 3 seconds.. we cant wait any longer..", destination.getAbsolutePath()));
		return destination;
	}
	
}
