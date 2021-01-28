package com.redhat.sso.ninja;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.google.common.base.Joiner;

public class ScriptRunner{
//  private static final Logger log=Logger.getLogger(ScriptRunner.class);
  
//	public static LineParser defaultLineParser=new LineParser(){
//		@Override public void parse(String line){
//			System.out.println(line);
//		}};
//	
//	public interface LineParser{
//		public void parse(String line);
//	}
	
	
	public static void main(String[] asd) throws IOException, InterruptedException{
		File scriptFolder=new File("/home/mallen/temp/ninja4/ninja-board/target/scripts/Trello.ThoughtLeadership/v1.4");
		new ScriptRunner().run(scriptFolder, "/home/mallen/temp/ninja4/ninja-board/target/scripts/Trello.ThoughtLeadership/v1.4/trello-stats.py -s 2021-01-12 -o servicesmarketing3");
	}
	
//	public int run(File workingFolder, String command, LineParser lineParser){
//		boolean isWindows=false;
//		try{
//			ProcessBuilder pBuilder=new ProcessBuilder(isWindows?"cmd.exe":"/bin/sh").directory(workingFolder).command(command.split(" "));
//			pBuilder.redirectErrorStream(true);
//			Process process=pBuilder.start();
//			String buf;
//			BufferedReader reader=new BufferedReader(new InputStreamReader(process.getInputStream()));
//			while ((buf=reader.readLine())!=null) {
////				log.debug(buf);
//				lineParser.parse(buf);
//			}
//			process.waitFor(10, TimeUnit.SECONDS);
////			System.out.println(process.exitValue());
////			System.out.println("==================================");
//			return process.exitValue();
//		}catch (IOException e){
//			e.printStackTrace();
//		}catch (InterruptedException e1){
//			e1.printStackTrace();
//		}
//		return 1;
//	}
	
	
	public class ProcessResult{
		private List<String> lines=new ArrayList<String>();
		private int exitValue;
		public int exitValue(){
			return this.exitValue;
		}
		public List<String> lines(){
			return this.lines;
		}
	}
	
	public ProcessResult run(File workingFolder, String command) throws IOException, InterruptedException{
		boolean isWindows=false;
		ProcessBuilder pBuilder=new ProcessBuilder(isWindows?"cmd.exe":"/bin/sh").directory(workingFolder).command(command.split(" "));
		pBuilder.redirectErrorStream(true);
		Process process=pBuilder.start();
		String buf;
		ProcessResult result=new ProcessResult();
		BufferedReader reader=new BufferedReader(new InputStreamReader(process.getInputStream()));
		while ((buf=reader.readLine())!=null) {
			result.lines.add(buf);
		}
		process.waitFor(10, TimeUnit.SECONDS);
		result.exitValue=process.exitValue();
		return result;
	}
	
//	public interface ErrorHandler{
//		public void onError(int exitValue, String lines);
//	}
//	public ProcessResult run2(File workingFolder, String command, LineParser lineParser, ErrorHandler errorHandler) throws IOException, InterruptedException{
//		boolean isWindows=false;
//		ProcessBuilder pBuilder=new ProcessBuilder(isWindows?"cmd.exe":"/bin/sh").directory(workingFolder).command(command.split(" "));
//		pBuilder.redirectErrorStream(true);
//		Process process=pBuilder.start();
//		String buf;
//		ProcessResult result=new ProcessResult();
//		BufferedReader reader=new BufferedReader(new InputStreamReader(process.getInputStream()));
//		while ((buf=reader.readLine())!=null) {
////			System.out.println(buf);
//			result.lines.add(buf);
//		}
//		process.waitFor(10, TimeUnit.SECONDS);
//		result.exitValue=process.exitValue();
//		
//		if (0==result.exitValue){
//			for (String line:result.lines())
//				lineParser.parse(line);
//		}else{
//			errorHandler.onError(result.exitValue, Joiner.on("\n").join(result.lines()));
//		}
//		
//		return result;
//	}
	
}
