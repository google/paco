package com.google.android.apps.paco;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.Splitter;
import com.pacoapp.paco.model.Event;
import com.pacoapp.paco.model.ExperimentUtil;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.test.AndroidTestCase;
import android.test.mock.MockCursor;

public class ExperimentUtilTestCase  extends AndroidTestCase{
	Map<String, String> eventsOutputColumns;
	
	
	@Before
	public void before() {
		loadMap();
	}
	
	public void loadMap(){
		  if (eventsOutputColumns ==null){
			  eventsOutputColumns = new HashMap<String,String>();
			  eventsOutputColumns.put("EXPERIMENT_ID", "EVENTS");
			  eventsOutputColumns.put("EXPERIMENT_SERVER_ID", "EVENTS");
			  eventsOutputColumns.put("EXPERIMENT_NAME", "EVENTS");
			  eventsOutputColumns.put("EXPERIMENT_VERSION", "EVENTS");
			  eventsOutputColumns.put("SCHEDULE_TIME", "EVENTS");
			  eventsOutputColumns.put("RESPONSE_TIME", "EVENTS");
			  eventsOutputColumns.put("UPLOADED", "EVENTS");
			  eventsOutputColumns.put("GROUP_NAME", "EVENTS");
			  eventsOutputColumns.put("ACTION_TRIGGER_ID","EVENTS");
			  eventsOutputColumns.put("ACTION_TRIGGER_SPEC_ID","EVENTS");
			  eventsOutputColumns.put("ACTION_ID","EVENTS");
			  eventsOutputColumns.put("EVENT_ID", "OUTPUTS");
			  eventsOutputColumns.put("TEXT", "OUTPUTS");
			  eventsOutputColumns.put("ANSWER", "OUTPUTS");
			  eventsOutputColumns.put("INPUT_SERVER_ID", "OUTPUTS");
		  }
	}
	
	@Test
	public void testIdentifyTablesInvolved_withStar(){
		  	final String EVENTS_OUTPUTS_TABLE_NAME = "eventsoutputs";
			List<String> inpList = new ArrayList<String>();
			inpList.add("*");
			String actualReturnValue = ExperimentUtil.identifyTablesInvolved(eventsOutputColumns, inpList);
			assertEquals(EVENTS_OUTPUTS_TABLE_NAME, actualReturnValue);
	}
	
	@Test
	public void testIdentifyTablesInvolved_withBothTable(){
		  	final String EVENTS_OUTPUTS_TABLE_NAME = "eventsoutputs";
			List<String> inpList = new ArrayList<String>();
			inpList.add("Response_time");
			inpList.add("answer");
			String actualReturnValue = ExperimentUtil.identifyTablesInvolved(eventsOutputColumns, inpList);
			assertEquals(EVENTS_OUTPUTS_TABLE_NAME, actualReturnValue);
	}
	
	@Test
	public void testIdentifyTablesInvolved_withSingleTable(){
		  	final String EVENTS_TABLE_NAME = "events";
			List<String> inpList = new ArrayList<String>();
			inpList.add("Response_time");
			inpList.add("experiment_version");
			String actualReturnValue = ExperimentUtil.identifyTablesInvolved(eventsOutputColumns, inpList);
			assertEquals(EVENTS_TABLE_NAME, actualReturnValue);
	}
//	@Test
//	public void testCreateEventWithPartialResponses_Sc1(){
//	
//		  	final String[] col = new String[]{"experiment_group_name"};
//		  	final Object[] colValue = new String[]{"New Group"};
//			MatrixCursor mc = new MatrixCursor(col);
//			mc.addRow(colValue);
//			Event actualEvent = ExperimentUtil.createEventWithPartialResponses(mc);
//			assertEquals("New Group", actualEvent.getExperimentGroupName());
//	}
}
