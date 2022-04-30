package com.redhat.sso.ninja.controllers;

import org.junit.Test;

import com.redhat.sso.ninja.utils.MapBuilder;

public class EventsControllerTest{

	@Test
	public void testGetEventsV2_30DaysOld() throws Exception{

  	long s=System.currentTimeMillis();
		new EventsController().getEventsV2(new MapBuilder<String,String>().put("daysOld","30").put("includeLM","true").build());
//  	System.out.println(
			//new ExportController().exportEvents(null, "csv")
//  	);
  	System.out.println("took "+(System.currentTimeMillis()-s+"ms"));
	}
}
