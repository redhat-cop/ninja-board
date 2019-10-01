package com.redhat.sso.ninja.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class DownloadFile{
  private Logger log= Logger.getLogger(this.getClass());
  
  public String get(String remoteLocation, String localDestination, PosixFilePermission... permissions) throws IOException{
    return get(remoteLocation, new File(localDestination), permissions);
  }
  
  public String get(String remoteLocation, File localDestination, PosixFilePermission... permissions) throws IOException{
    
      log.debug("Provided remote location is: "+remoteLocation);
      localDestination.mkdirs();
      URL sanitizedRemoteLocation=new URL(remoteLocation.contains(" ")?remoteLocation.substring(0, remoteLocation.indexOf(" ")):remoteLocation);
      
      File dest=new File(localDestination, new File(sanitizedRemoteLocation.getPath()).getName()); // extract just the name, not the path
      
      boolean hasExecutablePermissions=dest.exists() && Files.getPosixFilePermissions(dest.toPath()).contains(PosixFilePermission.GROUP_EXECUTE);
      
      if (!dest.exists() || !hasExecutablePermissions){ // then its not been downloaded yet, so go get it
        
        log.debug("Downloading from ["+sanitizedRemoteLocation+"] to ["+dest.getAbsolutePath()+"]");
        if (dest.exists()) dest.delete();
        FileOutputStream os=new FileOutputStream(dest);
        try{
          IOUtils.copy(sanitizedRemoteLocation.openStream(), os);
        }finally{
          os.close();
        }
        
        FilePermissions.set(dest, 
            PosixFilePermission.OWNER_READ, 
            PosixFilePermission.OWNER_WRITE, 
            PosixFilePermission.OWNER_EXECUTE,
            PosixFilePermission.GROUP_READ, 
            PosixFilePermission.GROUP_WRITE, 
            PosixFilePermission.GROUP_EXECUTE
            );
        
        Files.getPosixFilePermissions(dest.toPath()).contains(PosixFilePermission.GROUP_EXECUTE);
        
      }else{
        log.debug("file exists, not downloading: "+dest.getAbsolutePath());
      }
      String result=dest.getAbsolutePath() + (remoteLocation.contains(" ")?remoteLocation.substring(remoteLocation.indexOf(" ")):"");
      log.debug("New remote location is: "+result);
      return result;
//      log.debug("command is now: "+command);
//    }
  }
}
