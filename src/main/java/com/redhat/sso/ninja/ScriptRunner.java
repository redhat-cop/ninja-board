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
	
	
}
