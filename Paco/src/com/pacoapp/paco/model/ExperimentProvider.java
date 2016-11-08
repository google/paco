/*
* Copyright 2011 Google Inc. All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance  with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package com.pacoapp.paco.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;


/**
 * Provides access to a database of experiments.
 */

public class ExperimentProvider extends ContentProvider {

  static final String TAG = "ExperimentProvider";

  static final String DATABASE_NAME = "experiments.db";
  static final int DATABASE_VERSION = 23;

  static final String EXPERIMENTS_TABLE_NAME = "experiments";
  static final String EVENTS_TABLE_NAME = "events";
  static final String OUTPUTS_TABLE_NAME = "outputs";
  static final String NOTIFICATION_TABLE_NAME = "notifications";

  private static final int EXPERIMENTS_DATATYPE = 1;
  private static final int EXPERIMENT_ITEM_DATATYPE = 2;

  private static final int JOINED_EXPERIMENTS_DATATYPE = 4;
  private static final int JOINED_EXPERIMENT_ITEM_DATATYPE = 5;

  private static final int OUTPUTS_DATATYPE = 8;
  private static final int OUTPUT_ITEM_DATATYPE = 9;

  private static final int EVENTS_DATATYPE = 10;
  private static final int EVENT_ITEM_DATATYPE = 11;

  private static final int NOTIFICATION_DATATYPE = 14;
  private static final int NOTIFICATION_ITEM_DATATYPE = 15;

  private static final int EVENTS_OUTPUTS_DATATYPE=16;

  private SQLiteDatabase db;
  private final UriMatcher uriMatcher;
  private static  Map<String, String> eventsOutputColumns = null;

  public ExperimentProvider() {
    uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    uriMatcher.addURI(ExperimentProviderUtil.AUTHORITY, "experiments", EXPERIMENTS_DATATYPE);
    uriMatcher.addURI(ExperimentProviderUtil.AUTHORITY, "experiments/#", EXPERIMENT_ITEM_DATATYPE);
    uriMatcher.addURI(ExperimentProviderUtil.AUTHORITY, "joinedexperiments", JOINED_EXPERIMENTS_DATATYPE);
    uriMatcher.addURI(ExperimentProviderUtil.AUTHORITY, "joinedexperiments/#", JOINED_EXPERIMENT_ITEM_DATATYPE);
    uriMatcher.addURI(ExperimentProviderUtil.AUTHORITY, "events", EVENTS_DATATYPE);
    uriMatcher.addURI(ExperimentProviderUtil.AUTHORITY, "events/#", EVENT_ITEM_DATATYPE);
    uriMatcher.addURI(ExperimentProviderUtil.AUTHORITY, "outputs", OUTPUTS_DATATYPE);
    uriMatcher.addURI(ExperimentProviderUtil.AUTHORITY, "outputs/#", OUTPUT_ITEM_DATATYPE);
    uriMatcher.addURI(ExperimentProviderUtil.AUTHORITY, "notifications", NOTIFICATION_DATATYPE);
    uriMatcher.addURI(ExperimentProviderUtil.AUTHORITY, "notifications/#", NOTIFICATION_ITEM_DATATYPE);
    loadMap();
  }

  @Override
  public boolean onCreate() {
	DatabaseHelper dbHelper = new DatabaseHelper(getContext()/*,
    	getContext().getAssets().open("insert.sql")*/);
    db = dbHelper.getWritableDatabase();
	return db != null;
  }

  public Cursor query(Uri uri, String[] projection, String selection,
	  String[] selectionArgs, String sortOrder) {

    SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
    switch (uriMatcher.match(uri)) {
    case EXPERIMENTS_DATATYPE:
      qb.setTables(EXPERIMENTS_TABLE_NAME);
//      qb.appendWhere(addJoinedNullClause());
      break;
    case EXPERIMENT_ITEM_DATATYPE:
      qb.setTables(EXPERIMENTS_TABLE_NAME);
      qb.appendWhere(addIdEqualsClause(getIdFromPath(uri)));
      break;
    case JOINED_EXPERIMENTS_DATATYPE:
      qb.setTables(EXPERIMENTS_TABLE_NAME);
      qb.appendWhere(addJoinedNotNullClause());
      break;
    case JOINED_EXPERIMENT_ITEM_DATATYPE:
      qb.setTables(EXPERIMENTS_TABLE_NAME);
      qb.appendWhere(addIdEqualsClause(getIdFromPath(uri)));
      break;
    case OUTPUTS_DATATYPE:
      qb.setTables(OUTPUTS_TABLE_NAME);
      break;
    case OUTPUT_ITEM_DATATYPE:
      qb.setTables(OUTPUTS_TABLE_NAME);
      qb.appendWhere(addIdEqualsClause(getIdFromPath(uri)));
      break;
    case EVENTS_DATATYPE:
      qb.setTables(EVENTS_TABLE_NAME);
      break;
    case EVENT_ITEM_DATATYPE:
      qb.setTables(EVENTS_TABLE_NAME);
      qb.appendWhere(addIdEqualsClause(getIdFromPath(uri)));
      break;
    case NOTIFICATION_DATATYPE:
      qb.setTables(NOTIFICATION_TABLE_NAME);
      break;
    case NOTIFICATION_ITEM_DATATYPE:
      qb.setTables(NOTIFICATION_TABLE_NAME);
      qb.appendWhere(addIdEqualsClause(getIdFromPath(uri)));
      break;
    default:
      throw new IllegalArgumentException("Unknown URI " + uri);
    }

    // If no sort order is specified use the default
    String orderBy;
    if (TextUtils.isEmpty(sortOrder)) {
      orderBy = ExperimentColumns.DEFAULT_SORT_ORDER;
    } else {
      orderBy = sortOrder;
    }

    Cursor c = qb.query(db, projection, selection, selectionArgs, null, null,
        orderBy);

    // Tell the cursor what uri to watch, so it knows when its source data
    // changes
    c.setNotificationUri(getContext().getContentResolver(), uri);
    return c;
  }

  private String getIdFromPath(Uri uri) {
    return uri.getPathSegments().get(1);
  }

  @Override
  public String getType(Uri uri) {
    switch (uriMatcher.match(uri)) {
    case EXPERIMENTS_DATATYPE:
    case JOINED_EXPERIMENTS_DATATYPE:
      return ExperimentColumns.CONTENT_TYPE;

    case EXPERIMENT_ITEM_DATATYPE:
    case JOINED_EXPERIMENT_ITEM_DATATYPE:
      return ExperimentColumns.CONTENT_ITEM_TYPE;
    case OUTPUTS_DATATYPE:
      return OutputColumns.CONTENT_TYPE;
    case OUTPUT_ITEM_DATATYPE:
      return OutputColumns.CONTENT_ITEM_TYPE;
    case EVENTS_DATATYPE:
      return EventColumns.CONTENT_TYPE;
    case EVENT_ITEM_DATATYPE:
      return EventColumns.CONTENT_ITEM_TYPE;
    case NOTIFICATION_DATATYPE:
      return NotificationHolderColumns.CONTENT_TYPE;
    case NOTIFICATION_ITEM_DATATYPE:
      return NotificationHolderColumns.CONTENT_ITEM_TYPE;
    default:
      throw new IllegalArgumentException("Unknown URI " + uri);
    }
  }

  @Override
  public Uri insert(Uri uri, ContentValues initialValues) {
  	ContentValues values = null;
  	if (initialValues != null) {
  	  values = new ContentValues(initialValues);
  	} else {
  	  values = new ContentValues();
  	}

    int match = uriMatcher.match(uri);

    switch (match) {
    case EXPERIMENTS_DATATYPE:
      ensureJoinDate(values);
      return insertExperiment(uri, values);
    case JOINED_EXPERIMENTS_DATATYPE:
      ensureJoinDate(values);
      return insertExperiment(uri, values);
    case OUTPUTS_DATATYPE:
      return insertOutput(uri, values);
    case EVENTS_DATATYPE:
      return insertEvent(uri, values);
    case NOTIFICATION_DATATYPE:
      return insertNotification(uri, values);
    default:
      throw new IllegalArgumentException("Unknown Url: " + uri);
    }
  }

  private Uri insertNotification(Uri uri, ContentValues values) {
    long rowId = db.insert(NOTIFICATION_TABLE_NAME, NotificationHolderColumns.EXPERIMENT_ID, values);
    if (rowId > 0) {
      Uri resultUri = ContentUris.withAppendedId(NotificationHolderColumns.CONTENT_URI, rowId);
      getContext().getContentResolver().notifyChange(uri, null, true);
      return resultUri;
    }
    throw new SQLException("Failed to insert row into " + uri);

  }

  private Uri insertEvent(Uri uri, ContentValues values) {
    long rowId = db.insert(EVENTS_TABLE_NAME, EventColumns.SCHEDULE_TIME, values);
    if (rowId > 0) {
      Uri resultUri = ContentUris.withAppendedId(EventColumns.CONTENT_URI, rowId);
      getContext().getContentResolver().notifyChange(uri, null, true);
      return resultUri;
    }
    throw new SQLException("Failed to insert row into " + uri);
  }

  private Uri insertOutput(Uri uri, ContentValues values) {
    long rowId = db.insert(OUTPUTS_TABLE_NAME, OutputColumns.NAME, values);
    if (rowId > 0) {
      Uri resultUri = ContentUris.withAppendedId(OutputColumns.CONTENT_URI, rowId);
      getContext().getContentResolver().notifyChange(uri, null, true);
      return resultUri;
    }
    throw new SQLException("Failed to insert row into " + uri);
  }

  private Uri insertExperiment(Uri uri, ContentValues values) {
    long rowId = db.insert(EXPERIMENTS_TABLE_NAME, ExperimentColumns.JOIN_DATE, values);
    if (rowId > 0) {
      Uri experimentUri = ContentUris.withAppendedId(ExperimentColumns.JOINED_EXPERIMENTS_CONTENT_URI, rowId);
      getContext().getContentResolver().notifyChange(uri, null, true);
      return experimentUri;
    }
    throw new IllegalStateException("Failed to insert row for joining experiment into " + uri);
  }

  @Override
  public int delete(Uri uri, String where, String[] whereArgs) {
    int count;
    switch (uriMatcher.match(uri)) {
    case EXPERIMENTS_DATATYPE:
      count = db.delete(EXPERIMENTS_TABLE_NAME, where, // addJoinedExperimentExclusionClauseTo(where),
                        whereArgs);
      break;
    case EXPERIMENT_ITEM_DATATYPE:
      count = db.delete(EXPERIMENTS_TABLE_NAME, addIdEqualsClauseTo(where, getIdFromPath(uri)), whereArgs);
      break;
    case JOINED_EXPERIMENTS_DATATYPE:
      count = db.delete(EXPERIMENTS_TABLE_NAME, addJoinedExperimentInclusionClauseTo(where), whereArgs);
      break;
    case JOINED_EXPERIMENT_ITEM_DATATYPE:
      count = db.delete(EXPERIMENTS_TABLE_NAME, addIdEqualsClauseTo(where, getIdFromPath(uri)), whereArgs);
      break;
    case OUTPUTS_DATATYPE:
      count = db.delete(OUTPUTS_TABLE_NAME, where, whereArgs);
      break;
    case OUTPUT_ITEM_DATATYPE:
      count = db.delete(OUTPUTS_TABLE_NAME, addIdEqualsClauseTo(where, getIdFromPath(uri)), whereArgs);
      break;
    case EVENTS_DATATYPE:
      count = db.delete(EVENTS_TABLE_NAME, where, whereArgs);
      break;
    case EVENT_ITEM_DATATYPE:
      count = db.delete(EVENTS_TABLE_NAME, addIdEqualsClauseTo(where, getIdFromPath(uri)), whereArgs);
      break;

    case NOTIFICATION_DATATYPE:
      count = db.delete(NOTIFICATION_TABLE_NAME, where, whereArgs);
      break;
    case NOTIFICATION_ITEM_DATATYPE:
      count = db.delete(NOTIFICATION_TABLE_NAME, addIdEqualsClauseTo(where, getIdFromPath(uri)), whereArgs);
      break;

    default:
      throw new IllegalArgumentException("Unknown URI " + uri);
    }

    getContext().getContentResolver().notifyChange(uri, null, true);
    return count;
  }

  @Override
  public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
    int count;
    switch (uriMatcher.match(uri)) {
    case EXPERIMENTS_DATATYPE:
      count = db.update(EXPERIMENTS_TABLE_NAME, values, addJoinedExperimentExclusionClauseTo(where), whereArgs);
      break;

    case EXPERIMENT_ITEM_DATATYPE:
      count = db.update(EXPERIMENTS_TABLE_NAME, values, addIdEqualsClauseTo(where, getIdFromPath(uri)), whereArgs);
      break;

    case JOINED_EXPERIMENTS_DATATYPE:
      count = db.update(EXPERIMENTS_TABLE_NAME, values, addJoinedExperimentInclusionClauseTo(where), whereArgs);
      break;

    case JOINED_EXPERIMENT_ITEM_DATATYPE:
      count = db.update(EXPERIMENTS_TABLE_NAME, values, addIdEqualsClauseTo(where, getIdFromPath(uri)), whereArgs);
      break;

    case OUTPUTS_DATATYPE:
      count = db.update(OUTPUTS_TABLE_NAME, values, where, whereArgs);
      break;

    case OUTPUT_ITEM_DATATYPE:
      count = db.update(OUTPUTS_TABLE_NAME, values, addIdEqualsClauseTo(where, getIdFromPath(uri)), whereArgs);
      break;

    case EVENTS_DATATYPE:
      count = db.update(EVENTS_TABLE_NAME, values, where, whereArgs);
      break;

    case EVENT_ITEM_DATATYPE:
      count = db.update(EVENTS_TABLE_NAME, values, addIdEqualsClauseTo(where, getIdFromPath(uri)), whereArgs);
      break;

    case NOTIFICATION_DATATYPE:
      count = db.update(NOTIFICATION_TABLE_NAME, values, where, whereArgs);
      break;
    case NOTIFICATION_ITEM_DATATYPE:
      count = db.update(NOTIFICATION_TABLE_NAME, values, addIdEqualsClauseTo(where, getIdFromPath(uri)), whereArgs);
      break;
    default:
      throw new IllegalArgumentException("Unknown URI " + uri);
    }

    getContext().getContentResolver().notifyChange(uri, null, true);
    return count;
  }

  private String addIdEqualsClauseTo(String where, String id) {
    return addIdEqualsClause(id) + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : "");
  }

  private String addIdEqualsClause(String id) {
    return "_id = " + id;
  }

  private String addJoinedExperimentInclusionClauseTo(String where) {
    return addJoinedNotNullClause() + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : "");
  }

  private String addJoinedNotNullClause() {
    return ExperimentColumns.JOIN_DATE + " IS NOT null";
  }

  private String addJoinedExperimentExclusionClauseTo(String where) {
    return addJoinedNullClause() + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : "");
  }

  private String addJoinedNullClause() {
    return ExperimentColumns.JOIN_DATE + " IS null";
  }

  private void ensureJoinDate(ContentValues values) {
    if (!values.containsKey(ExperimentColumns.JOIN_DATE)) {
    values.put(ExperimentColumns.JOIN_DATE, new DateTime().getMillis());
    }
  }
  
  private int identifyTablesInvolved(List<String> colNames){
	  int tableIndicator = 0;
	  if(colNames!=null && colNames.size()>0){
		  //if the input is *, then we do the join and get all the fields in both events and outputs
		  if(colNames.get(0) == "*"){
			  return ExperimentProvider.EVENTS_OUTPUTS_DATATYPE;
		  }
		  String tableName = eventsOutputColumns.get(colNames.get(0).toUpperCase());
		  for (String s: colNames){
			  String crTableName = eventsOutputColumns.get(s.toUpperCase());
			  //once we get a different table, then we need to do a join
			 if(tableName!=null && !tableName.equals(crTableName)){
				 tableIndicator = ExperimentProvider.EVENTS_OUTPUTS_DATATYPE;
				 return tableIndicator;
			 }
		  }
		  if(tableName!=null && tableName.equalsIgnoreCase(ExperimentProvider.EVENTS_TABLE_NAME)){
			  tableIndicator = ExperimentProvider.EVENTS_DATATYPE;
		  }else{
			  tableIndicator = ExperimentProvider.OUTPUTS_DATATYPE;
		  }
	  }
	  return tableIndicator;
  }

  private void loadMap(){
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
  
  public List<Event> findEventsByCriteriaQuery(Context context, String[] projection, String criteriaColumns, String criteriaValues[], String sortOrder, String limit, String groupBy) {	    
	    Cursor cursor = null;
	    List<Event> events = null;
	    DatabaseHelper dbHelper = null;
	    try {
	    	dbHelper = new DatabaseHelper(context);
		    db = dbHelper.getReadableDatabase();
	    	List<String> allColumns = Arrays.asList(projection);
	    	int tableIndicator = identifyTablesInvolved(allColumns);
	    	
	    	switch (tableIndicator){
		    	case EVENTS_DATATYPE:
		    		//TODO: Should we refactor to use query at line 101
		    		cursor = db.query(EVENTS_TABLE_NAME, projection, criteriaColumns, criteriaValues, groupBy, null, sortOrder);
		    		break;
		    	case OUTPUTS_DATATYPE:
		    		//TODO: Should we refactor to use query at line 101 
		    		cursor = db.query(OUTPUTS_TABLE_NAME, projection, criteriaColumns, criteriaValues, groupBy, null, sortOrder);
		    		break;
		    	case EVENTS_OUTPUTS_DATATYPE:
		    		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		    		qb.setTables(EVENTS_TABLE_NAME+ " INNER JOIN " + OUTPUTS_TABLE_NAME + 
		    				" ON " + (EVENTS_TABLE_NAME+ "." +EventColumns._ID) + " = " + OutputColumns.EVENT_ID);
	
		    		cursor = qb.query(db, projection, criteriaColumns, criteriaValues, groupBy, null, sortOrder);
		    		break;
		    	default:
		    		break;
	    	}
	    	
	    	if (cursor != null) {
	    		events = new ArrayList<Event>();
			    while (cursor.moveToNext()) {
			      Event event = createEventWithPartialResponses(cursor);
			      events.add(event);
			    }
			}    
	    } finally{
	    	if(cursor !=null){
	    		cursor.close();
	    	}
	    	db.close();
	    }
    	
		return events;
	  }
  
  Event createEventWithPartialResponses(Cursor cursor) {
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
