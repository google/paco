package com.pacoapp.paco.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import android.database.Cursor;

public class ExperimentUtil {
	 public static String identifyTablesInvolved(Map<String, String> eventsOutputColumns, List<String> colNames){
		  String tableIndicator = null;
//		  loadColumnTableAssociationMap();
		  if(colNames!=null && colNames.size()>0){
			  //if the input is *, then we do the join and get all the fields in both events and outputs
			  String firstColName = colNames.get(0).trim().toUpperCase();
			  if(firstColName.equals("*")){
				  return ExperimentProvider.EVENTS_OUTPUTS_TABLE_NAME;
			  }
			  String tableName = eventsOutputColumns.get(firstColName);
			  if(tableName != null){
				  for (String s: colNames){
					 String crTableName = eventsOutputColumns.get(s.toUpperCase());
					  //once we get a different table, then we need to do a join
					 if(!tableName.equals(crTableName)){
						 tableIndicator = ExperimentProvider.EVENTS_OUTPUTS_TABLE_NAME;
						 return tableIndicator;
					 }
				  }
			  
				  if(tableName.equalsIgnoreCase(ExperimentProvider.EVENTS_TABLE_NAME)){
					  tableIndicator = ExperimentProvider.EVENTS_TABLE_NAME;
				  }else if(tableName.equalsIgnoreCase(ExperimentProvider.OUTPUTS_TABLE_NAME)){
					  tableIndicator = ExperimentProvider.OUTPUTS_TABLE_NAME;
				  }
			   }
		  }
		  return tableIndicator;
	  }
	 
	 public static Event createEventWithPartialResponses(Cursor cursor) {
		    Event event = new Event();
		  	Output input = new Output();
		    List<Output> responses = new ArrayList<Output>();
		   
			int idIndex = cursor.getColumnIndex(EventColumns._ID);
			int experimentIdIndex  = cursor.getColumnIndex(EventColumns.EXPERIMENT_ID);
			int experimentServerIdIndex = cursor.getColumnIndex(EventColumns.EXPERIMENT_SERVER_ID);
		    int experimentVersionIndex = cursor.getColumnIndex(EventColumns.EXPERIMENT_VERSION);
		    int experimentNameIndex = cursor.getColumnIndex(EventColumns.EXPERIMENT_NAME);
		    int scheduleTimeIndex = cursor.getColumnIndex(EventColumns.SCHEDULE_TIME);
		    int responseTimeIndex = cursor.getColumnIndex(EventColumns.RESPONSE_TIME);
		    int uploadedIndex = cursor.getColumnIndex(EventColumns.UPLOADED);
		    int groupNameIndex = cursor.getColumnIndex(EventColumns.GROUP_NAME);
		    int actionTriggerIndex = cursor.getColumnIndex(EventColumns.ACTION_TRIGGER_ID);
		    int actionTriggerSpecIndex = cursor.getColumnIndex(EventColumns.ACTION_TRIGGER_SPEC_ID);
		    int actionIdIndex = cursor.getColumnIndex(EventColumns.ACTION_ID);
		    int eventIdIndex = cursor.getColumnIndex(OutputColumns.EVENT_ID);
		    int inputServeridIndex = cursor.getColumnIndex(OutputColumns.INPUT_SERVER_ID);
		    int answerIndex = cursor.getColumnIndex(OutputColumns.ANSWER);
		    int nameIndex = cursor.getColumnIndex(OutputColumns.NAME);

		    if (!cursor.isNull(idIndex)) {
		      event.setId(cursor.getLong(idIndex));
		    }

		    if (!cursor.isNull(experimentIdIndex)) {
		      event.setExperimentId(cursor.getLong(experimentIdIndex));
		    }

		    if (!cursor.isNull(experimentServerIdIndex)) {
		      event.setServerExperimentId(cursor.getLong(experimentServerIdIndex));
		    }
		    if (!cursor.isNull(experimentVersionIndex)) {
		      event.setExperimentVersion(cursor.getInt(experimentVersionIndex));
		    }
		    if (!cursor.isNull(experimentNameIndex)) {
		      event.setExperimentName(cursor.getString(experimentNameIndex));
		    }

		    if (!cursor.isNull(responseTimeIndex)) {
		      event.setResponseTime(new DateTime(cursor.getLong(responseTimeIndex)));
		    }
		    if (!cursor.isNull(scheduleTimeIndex)) {
		      event.setScheduledTime(new DateTime(cursor.getLong(scheduleTimeIndex)));
		    }
		    if (!cursor.isNull(uploadedIndex)) {
		      event.setUploaded(cursor.getInt(uploadedIndex) == 1);
		    }

		    if (!cursor.isNull(groupNameIndex)) {
		      event.setExperimentGroupName(cursor.getString(groupNameIndex));
		    }
		    if (!cursor.isNull(actionTriggerIndex)) {
		      event.setActionTriggerId(cursor.getLong(actionTriggerIndex));
		    }
		    if (!cursor.isNull(actionTriggerSpecIndex)) {
		      event.setActionTriggerSpecId(cursor.getLong(actionTriggerSpecIndex));
		    }
		    if (!cursor.isNull(actionIdIndex)) {
		      event.setActionId(cursor.getLong(actionIdIndex));
		    }
		    if (!cursor.isNull(idIndex)) {
		      input.setId(cursor.getLong(idIndex));
		    }

		    //process output columns
		    if (!cursor.isNull(eventIdIndex)) {
		      input.setEventId(cursor.getLong(eventIdIndex));
		    }

		    if (!cursor.isNull(inputServeridIndex)) {
		      input.setInputServerId(cursor.getLong(inputServeridIndex));
		    }
		    if (!cursor.isNull(nameIndex)) {
		      input.setName(cursor.getString(nameIndex));
		    }
		    if (!cursor.isNull(answerIndex)) {
		      input.setAnswer(cursor.getString(answerIndex));
		    }
		    
		    responses.add(input);

		    event.setResponses(responses);
		    return event;
		  }

}
