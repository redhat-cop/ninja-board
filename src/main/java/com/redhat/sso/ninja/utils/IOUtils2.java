package com.redhat.sso.ninja.utils;

import org.apache.commons.io.IOUtils;

import java.io.*;

public abstract class IOUtils2{
  
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
