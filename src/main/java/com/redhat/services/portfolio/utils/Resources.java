package com.redhat.services.portfolio.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public class Resources{
	private static final String targetClasses="target/classes";
	
	/** HTML template from META-INF/resources */
	public static InputStream getTemplate(String resourceName) throws FileNotFoundException, IOException{
		return get        (!resourceName.startsWith("META-INF")?"META-INF/resources"+(resourceName.startsWith("/")?"":"/")+resourceName:resourceName);
	}
	/** HTML template from META-INF/resources */
	public static String getTemplateAsString(String resourceName) throws FileNotFoundException, IOException{
		return getAsString(!resourceName.startsWith("META-INF")?"META-INF/resources"+(resourceName.startsWith("/")?"":"/")+resourceName:resourceName);
	}
	
	public static InputStream get(String resourceName) throws FileNotFoundException, IOException{
		return new File(targetClasses, resourceName).exists()?
							new FileInputStream(new File(targetClasses, resourceName).getAbsolutePath()):
							Resources.class.getClass().getClassLoader().getResourceAsStream(resourceName);
	}
	
	public static String getAsString(String resourceName) throws FileNotFoundException, IOException{
	  // Old/Working implementation
	  // String template=IOUtils.toString(new File("target/classes", templateName).exists()?new FileInputStream(new File("target/classes", templateName).getAbsolutePath()):getClass().getClassLoader().getResourceAsStream(templateName), "UTF-8");
//		if (!resourceName.startsWith("META-INF")) resourceName="META-INF/resources/"+resourceName;
//		System.out.println("resource="+resourceName);
		// copying the input stream reading code so it's encapsulated in a try-with-resources statement to ensure closure of streams
//		System.out.println("Resource.getAsString:: resourceName -> "+resourceName);
		try(InputStream is=new File(targetClasses, resourceName).exists()?new FileInputStream(new File(targetClasses, resourceName).getAbsolutePath()):new Resources().getClass().getClassLoader().getResourceAsStream(resourceName)){
			if (null==is) throw new IOException(String.format("Resource not found: '%s'",resourceName));
			return IOUtils.toString(is, "UTF-8");
		}catch(Exception e){
			throw e;
		}
	}
}
