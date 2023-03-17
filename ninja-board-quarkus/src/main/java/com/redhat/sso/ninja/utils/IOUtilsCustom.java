package com.redhat.sso.ninja.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

public abstract class IOUtilsCustom {
  
  public static String toStringAndClose(InputStream is) throws IOException{
    String result=IOUtils.toString(is);
    IOUtils.closeQuietly(is);
    return result;
  }
  
  public static void writeAndClose(byte[] bytes, OutputStream out) throws IOException{
    out.write(bytes);
    IOUtils.closeQuietly(out);
  }
  
  
  public static abstract class DelegateMethod{
    public abstract Object process(InputStream is);
  }
  public static Object autoCloseFileInputStream(File file, DelegateMethod delegateMethod) throws FileNotFoundException{
    FileInputStream fis=null;
    try{
      fis=new FileInputStream(file);
      return delegateMethod.process(fis);
//      T result=(T)Json.newObjectMapper(true).readValue(fis, Summary.class);
//      return result;
//    } catch (FileNotFoundException e) {
//      e.printStackTrace();
//    } catch (JsonParseException e) {
//      e.printStackTrace();
//    } catch (JsonMappingException e) {
//      e.printStackTrace();
//    } catch (IOException e) {
//      e.printStackTrace();
    }finally{
      if (fis!=null)
        org.apache.commons.io.IOUtils.closeQuietly(fis);
    }
  }
}
