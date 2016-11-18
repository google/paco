package com.pacoapp.paco.model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.google.common.collect.Lists;

import android.database.Cursor;

public class ExperimentUtil {
  /**
   * 
   * @param eventsOutputColumns
   *          This holds all the column names of the tables Events and Outputs
   *          as keys and the associated table name as value
   * @param colNames
   *          List of names that the client has asked for in the query
   * @return Table name that should be used for the given columns. 
   *         This will have the following three scenarios 
   *         - If all input columns are from Events table, will return Events table 
   *         - If all input columns are from Outputs table, will return Outputs table 
   *         - If all input columns are a mix of the Outputs and Events table or a '*', will
   *         return Eventsoutputs table
   */
  public static String identifyTablesInvolved(Map<String, String> eventsOutputColumns, List<String> colNames) {
    // This method is not to validate the column names. This just helps identifying if we need to do a join on outputs table.
    // With null, we cannot accomodate new column names ad-hoc. 
    // So, changing the default to Join table instead of null.
    String tableIndicator = ExperimentProvider.EVENTS_OUTPUTS_TABLE_NAME;
    if (colNames != null && colNames.size() > 0) {
      // if the input is *, then we do the join and get all the fields in both
      // events and outputs
      String firstColName = colNames.get(0).trim().toUpperCase();
      if (firstColName != null && firstColName.equals("*")) {
        return ExperimentProvider.EVENTS_OUTPUTS_TABLE_NAME;
      }
      String tableName = eventsOutputColumns.get(firstColName);
      if (tableName != null) {
        for (String s : colNames) {
          String crTableName = eventsOutputColumns.get(s.toUpperCase());
          // once we get a different table, then we need to do a join
          if (!tableName.equals(crTableName)) {
            tableIndicator = ExperimentProvider.EVENTS_OUTPUTS_TABLE_NAME;
            return tableIndicator;
          }
        }

        if (tableName.equalsIgnoreCase(ExperimentProvider.EVENTS_TABLE_NAME)) {
          tableIndicator = ExperimentProvider.EVENTS_TABLE_NAME;
        } else if (tableName.equalsIgnoreCase(ExperimentProvider.OUTPUTS_TABLE_NAME)) {
          tableIndicator = ExperimentProvider.OUTPUTS_TABLE_NAME;
        }
      }
    }
    return tableIndicator;
  }

  public static Event createEventWithPartialResponses(Cursor cursor) {
    Event event = new Event();
    Output output = new Output();
    List<Output> responses = Lists.newArrayList();

    int idIndex = cursor.getColumnIndex(EventColumns._ID);

    int experimentIdIndex = cursor.getColumnIndex(EventColumns.EXPERIMENT_ID);
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

    // process output columns
    if (!cursor.isNull(eventIdIndex)) {
      output.setEventId(cursor.getLong(eventIdIndex));
      // if outputs table's event_id is populated and event tables _id is not popoulated
      if(event.getId()==-1){
        event.setId(cursor.getLong(eventIdIndex));
      }
    }

    if (!cursor.isNull(inputServeridIndex)) {
      output.setInputServerId(cursor.getLong(inputServeridIndex));
    }

    if (!cursor.isNull(nameIndex)) {
      output.setName(cursor.getString(nameIndex));
    }

    if (!cursor.isNull(answerIndex)) {
      output.setAnswer(cursor.getString(answerIndex));
    }

    responses.add(output);

    event.setResponses(responses);
    return event;
  }

  /**
   * 
   * @param inputString
   *          contains all the columns names and with some sql key words
   *          depending upon the user query
   * @return the list of column names
   */
  public static List<String> aggregateExtractedColNames(String inputString) {
    // any non word character or the words 'and' 'or' 'is' 'not' get replaced
    // with blank
    inputString = inputString.replaceAll("\\W+", " ");
    // replace logical operators and other key words
    inputString = inputString.replaceAll(" and | or | is | not | asc | desc | asc| desc", " ");
    // multiple blank spaces get truncated to single blank space
    inputString = inputString.replaceAll("( )+", " ").trim();
    String[] out = inputString.split(" ");
    return Arrays.asList(out);
  }
}
