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
package com.google.android.apps.paco;

import java.util.HashMap;

import org.joda.time.DateTime;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.LiveFolders;
import android.text.TextUtils;
import android.util.Log;


/**
 * Provides access to a database of experiments.
 */

public class ExperimentProvider extends ContentProvider {

  static final String TAG = "ExperimentProvider";

  private static final String DATABASE_NAME = "experiments.db";
  private static final int DATABASE_VERSION = 11;
  
  private static final String EXPERIMENTS_TABLE_NAME = "experiments";
  private static final String SCHEDULES_TABLE_NAME = "schedules";

  private static final String INPUTS_TABLE_NAME = "inputs";
  private static final String EVENTS_TABLE_NAME = "events";
  private static final String OUTPUTS_TABLE_NAME = "outputs";
  private static final String FEEDBACK_TABLE_NAME = "feedback";
  private static final String NOTIFICATION_TABLE_NAME = "notifications";

  
  private static HashMap<String, String> liveFolderProjectionMap;

  private static final int EXPERIMENTS_DATATYPE = 1;
  private static final int EXPERIMENT_ITEM_DATATYPE = 2;
  private static final int LIVE_FOLDER_NOTES = 3;
  
  private static final int JOINED_EXPERIMENTS_DATATYPE = 4;
  private static final int JOINED_EXPERIMENT_ITEM_DATATYPE = 5;
  
  private static final int INPUTS_DATATYPE = 6;
  private static final int INPUT_ITEM_DATATYPE = 7;
  
  private static final int OUTPUTS_DATATYPE = 8;
  private static final int OUTPUT_ITEM_DATATYPE = 9;
  
  private static final int EVENTS_DATATYPE = 10;
  private static final int EVENT_ITEM_DATATYPE = 11;
  
  private static final int FEEDBACK_DATATYPE = 12;
  private static final int FEEDBACK_ITEM_DATATYPE = 13;
  
  private static final int NOTIFICATION_DATATYPE = 14;
  private static final int NOTIFICATION_ITEM_DATATYPE = 15;
  
  private static final int SCHEDULE_DATATYPE = 16;
  private static final int SCHEDULE_ITEM_DATATYPE = 17;
  
  /**
   * This class helps open, create, and upgrade the database file.
   */
  private static class DatabaseHelper extends SQLiteOpenHelper {

//	private InputStream sqlInput;

	DatabaseHelper(Context context/*, InputStream in*/) {
	  super(context, DATABASE_NAME, null, DATABASE_VERSION);
//	  this.sqlInput = in;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
	  db.execSQL("CREATE TABLE " + EXPERIMENTS_TABLE_NAME + " ("
        + ExperimentColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
        + ExperimentColumns.SERVER_ID + " INTEGER,"
        + ExperimentColumns.TITLE + " TEXT, "
        + ExperimentColumns.DESCRIPTION + " TEXT, "
        + ExperimentColumns.CREATOR + " TEXT, "
        + ExperimentColumns.INFORMED_CONSENT + " TEXT, "
        + ExperimentColumns.HASH + " TEXT, "
//        + ExperimentColumns.SCHEDULE_TYPE + " TEXT, "
        + ExperimentColumns.FIXED_DURATION + " INTEGER, "		  
        + ExperimentColumns.START_DATE + " INTEGER, "
        + ExperimentColumns.END_DATE + " INTEGER, "
//        + ExperimentColumns.DEFAULT_TIME + " INTEGER, "
//        + ExperimentColumns.ESM_FREQUENCY + " INTEGER, "
//        + ExperimentColumns.ESM_PERIOD + " INTEGER, "
        + ExperimentColumns.JOIN_DATE + " INTEGER, "
        + ExperimentColumns.QUESTIONS_CHANGE + " INTEGER, "
        + ExperimentColumns.ICON + " BLOB, "
        + ExperimentColumns.WEB_RECOMMENDED + " INTEGER "
        + ");");
	  db.execSQL("CREATE TABLE " + SCHEDULES_TABLE_NAME + " ("
          + SignalScheduleColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "          
          + SignalScheduleColumns.SERVER_ID + " INTEGER, "
          + SignalScheduleColumns.EXPERIMENT_ID + " INTEGER, "          
          + SignalScheduleColumns.SCHEDULE_TYPE + " INTEGER, "
          + SignalScheduleColumns.ESM_FREQUENCY + " INTEGER, "
          + SignalScheduleColumns.ESM_PERIOD + " INTEGER, "
          + SignalScheduleColumns.ESM_START_HOUR + " INTEGER, "
          + SignalScheduleColumns.ESM_END_HOUR + " INTEGER, "
          + SignalScheduleColumns.ESM_WEEKENDS + " INTEGER, "
          + SignalScheduleColumns.TIMES_CSV + " TEXT, "
          + SignalScheduleColumns.REPEAT_RATE + " INTEGER, "
          + SignalScheduleColumns.WEEKDAYS_SCHEDULED + " INTEGER, "
          + SignalScheduleColumns.NTH_OF_MONTH + " INTEGER, "
          + SignalScheduleColumns.BY_DAY_OF_MONTH + " INTEGER, "
          + SignalScheduleColumns.DAY_OF_MONTH + " INTEGER, "
          + SignalScheduleColumns.BEGIN_DATE + " INTEGER, "
          + SignalScheduleColumns.USER_EDITABLE + " INTEGER "
          + ");");
      db.execSQL("CREATE TABLE " + INPUTS_TABLE_NAME + " ("
          + InputColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "          
          + InputColumns.EXPERIMENT_ID + " INTEGER, "
          + InputColumns.SERVER_ID + " INTEGER, "
          + InputColumns.NAME + " TEXT,"
          + InputColumns.TEXT + " TEXT,"
          + InputColumns.MANDATORY + " INTEGER,"
          + InputColumns.SCHEDULED_DATE + " INTEGER,"
          + InputColumns.QUESTION_TYPE + " INTEGER,"
          + InputColumns.RESPONSE_TYPE + " INTEGER,"
          + InputColumns.LIKERT_STEPS + " INTEGER,"
          + InputColumns.LEFT_SIDE_LABEL + " TEXT,"
          + InputColumns.RIGHT_SIDE_LABEL + " TEXT,"
          + InputColumns.LIST_CHOICES_JSON + " TEXT,"
          + InputColumns.CONDITIONAL + " INTEGER,"
          + InputColumns.CONDITIONAL_EXPRESSION + " TEXT,"
          + InputColumns.MULTISELECT + " INTEGER"
          + ");");
      db.execSQL("CREATE TABLE " + EVENTS_TABLE_NAME + " ("
          + EventColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "          
          + EventColumns.EXPERIMENT_ID + " INTEGER, "
          + EventColumns.EXPERIMENT_SERVER_ID + " INTEGER, "
          + EventColumns.EXPERIMENT_NAME + " TEXT, "
          + EventColumns.SCHEDULE_TIME + " INTEGER, "
          + EventColumns.RESPONSE_TIME + " INTEGER,"
          + EventColumns.UPLOADED + " INTEGER"
          + ");");
      db.execSQL("CREATE TABLE " + OUTPUTS_TABLE_NAME + " ("
          + OutputColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
          + OutputColumns.EVENT_ID + " INTEGER, "
          + OutputColumns.INPUT_SERVER_ID + " INTEGER, "          
          + OutputColumns.NAME + " TEXT,"
          + OutputColumns.ANSWER + " TEXT"
          + ");");
      db.execSQL("CREATE TABLE " + FEEDBACK_TABLE_NAME + " ("
          + FeedbackColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
          + FeedbackColumns.EXPERIMENT_ID + " INTEGER, "
          + FeedbackColumns.SERVER_ID + " INTEGER, "
          + FeedbackColumns.FEEDBACK_TYPE + " TEXT,"
          + FeedbackColumns.TEXT + " TEXT"
          + ");");
      
      db.execSQL("CREATE TABLE " + NOTIFICATION_TABLE_NAME + " ("
          + NotificationHolderColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "         
          + NotificationHolderColumns.ALARM_TIME + " INTEGER, "
          + NotificationHolderColumns.EXPERIMENT_ID + " INTEGER, "
          + NotificationHolderColumns.NOTICE_COUNT + " INTEGER, "
          + NotificationHolderColumns.TIMEOUT_MILLIS + " INTEGER"
          + ");");

//	  insertValues(db);	  
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	  Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
		  + newVersion + ".");
	  
	  if (oldVersion <= 8) {
      db.execSQL("ALTER TABLE " + INPUTS_TABLE_NAME + " ADD "
          + InputColumns.MULTISELECT + " INTEGER default 0"
          + ";");
	  }  
	  
	  if (oldVersion <= 9) {
	    db.execSQL("ALTER TABLE " + SCHEDULES_TABLE_NAME + " ADD "
          + SignalScheduleColumns.USER_EDITABLE + " INTEGER default 1"
          + ";");
	  }
	  if (oldVersion <= 10) {
      db.execSQL("ALTER TABLE " + EXPERIMENTS_TABLE_NAME+ " ADD "
          + ExperimentColumns.WEB_RECOMMENDED + " INTEGER default 0"
          + ";");
    }  
	 }
	
//	public void insertValues(SQLiteDatabase db) {
//	  String CurLine = "";
//	  InputStreamReader converter = new InputStreamReader(sqlInput);
//	  BufferedReader in = new BufferedReader(converter);
//	  try {
//		while ((CurLine = in.readLine()) != null) {
//		  db.execSQL(CurLine);
//		}
//	  } catch (IOException e) {
//		Log.e(TAG, "error reading insert values");
//	  }
//	}
  }

  private SQLiteDatabase db;
  private final UriMatcher uriMatcher;

  public ExperimentProvider() {
    uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    uriMatcher.addURI(ExperimentProviderUtil.AUTHORITY, "experiments", EXPERIMENTS_DATATYPE);
    uriMatcher.addURI(ExperimentProviderUtil.AUTHORITY, "experiments/#", EXPERIMENT_ITEM_DATATYPE);
    uriMatcher.addURI(ExperimentProviderUtil.AUTHORITY, "live_folders/experiments", LIVE_FOLDER_NOTES);
    uriMatcher.addURI(ExperimentProviderUtil.AUTHORITY, "joinedexperiments", JOINED_EXPERIMENTS_DATATYPE);
    uriMatcher.addURI(ExperimentProviderUtil.AUTHORITY, "joinedexperiments/#", JOINED_EXPERIMENT_ITEM_DATATYPE);
    uriMatcher.addURI(ExperimentProviderUtil.AUTHORITY, "inputs", INPUTS_DATATYPE);
    uriMatcher.addURI(ExperimentProviderUtil.AUTHORITY, "inputs/#", INPUT_ITEM_DATATYPE);
    uriMatcher.addURI(ExperimentProviderUtil.AUTHORITY, "events", EVENTS_DATATYPE);
    uriMatcher.addURI(ExperimentProviderUtil.AUTHORITY, "events/#", EVENT_ITEM_DATATYPE);
    uriMatcher.addURI(ExperimentProviderUtil.AUTHORITY, "outputs", OUTPUTS_DATATYPE);
    uriMatcher.addURI(ExperimentProviderUtil.AUTHORITY, "outputs/#", OUTPUT_ITEM_DATATYPE);
    uriMatcher.addURI(ExperimentProviderUtil.AUTHORITY, "feedback", FEEDBACK_DATATYPE);
    uriMatcher.addURI(ExperimentProviderUtil.AUTHORITY, "feedback/#", FEEDBACK_ITEM_DATATYPE);
    uriMatcher.addURI(ExperimentProviderUtil.AUTHORITY, "notifications", NOTIFICATION_DATATYPE);
    uriMatcher.addURI(ExperimentProviderUtil.AUTHORITY, "notifications/#", NOTIFICATION_ITEM_DATATYPE);
    uriMatcher.addURI(ExperimentProviderUtil.AUTHORITY, "schedules", SCHEDULE_DATATYPE);
    uriMatcher.addURI(ExperimentProviderUtil.AUTHORITY, "schedules/#", SCHEDULE_ITEM_DATATYPE);
    
  }
  
  @Override
  public boolean onCreate() {
	DatabaseHelper dbHelper = new DatabaseHelper(getContext()/*,
    	getContext().getAssets().open("insert.sql")*/);
    db = dbHelper.getWritableDatabase();
	return db != null;
  } 

  @Override
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
    case LIVE_FOLDER_NOTES:
      qb.setTables(EXPERIMENTS_TABLE_NAME);
      qb.setProjectionMap(liveFolderProjectionMap);
      break;
    case JOINED_EXPERIMENTS_DATATYPE:
      qb.setTables(EXPERIMENTS_TABLE_NAME);
      qb.appendWhere(addJoinedNotNullClause());
      break;
    case JOINED_EXPERIMENT_ITEM_DATATYPE:
      qb.setTables(EXPERIMENTS_TABLE_NAME);
      qb.appendWhere(addIdEqualsClause(getIdFromPath(uri)));
      break;
    case INPUTS_DATATYPE:
      qb.setTables(INPUTS_TABLE_NAME);
      break;
    case INPUT_ITEM_DATATYPE:
      qb.setTables(INPUTS_TABLE_NAME);
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
    case FEEDBACK_DATATYPE:
      qb.setTables(FEEDBACK_TABLE_NAME);
      break;
    case FEEDBACK_ITEM_DATATYPE:
      qb.setTables(FEEDBACK_TABLE_NAME);
      qb.appendWhere(addIdEqualsClause(getIdFromPath(uri)));
      break;
    case NOTIFICATION_DATATYPE:
      qb.setTables(NOTIFICATION_TABLE_NAME);
      break;
    case NOTIFICATION_ITEM_DATATYPE:
      qb.setTables(NOTIFICATION_TABLE_NAME);
      qb.appendWhere(addIdEqualsClause(getIdFromPath(uri)));
      break;
    case SCHEDULE_DATATYPE:
      qb.setTables(SCHEDULES_TABLE_NAME);
      break;
    case SCHEDULE_ITEM_DATATYPE:
      qb.setTables(SCHEDULES_TABLE_NAME);
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
	case LIVE_FOLDER_NOTES:
	case JOINED_EXPERIMENTS_DATATYPE:
	  return ExperimentColumns.CONTENT_TYPE;

	case EXPERIMENT_ITEM_DATATYPE:
	case JOINED_EXPERIMENT_ITEM_DATATYPE:
	  return ExperimentColumns.CONTENT_ITEM_TYPE;
	case INPUTS_DATATYPE:
	  return InputColumns.CONTENT_TYPE;
	case INPUT_ITEM_DATATYPE:
      return InputColumns.CONTENT_ITEM_TYPE;
	case OUTPUTS_DATATYPE:
      return OutputColumns.CONTENT_TYPE;
    case OUTPUT_ITEM_DATATYPE:
      return OutputColumns.CONTENT_ITEM_TYPE;
    case EVENTS_DATATYPE:
      return EventColumns.CONTENT_TYPE;
    case EVENT_ITEM_DATATYPE:
      return EventColumns.CONTENT_ITEM_TYPE;
    case FEEDBACK_DATATYPE:
      return FeedbackColumns.CONTENT_TYPE;
    case FEEDBACK_ITEM_DATATYPE:
      return FeedbackColumns.CONTENT_ITEM_TYPE;
    case NOTIFICATION_DATATYPE:
      return NotificationHolderColumns.CONTENT_TYPE;
    case NOTIFICATION_ITEM_DATATYPE:
      return NotificationHolderColumns.CONTENT_ITEM_TYPE;  
    case SCHEDULE_DATATYPE:
      return SignalScheduleColumns.CONTENT_TYPE;
    case SCHEDULE_ITEM_DATATYPE:
      return SignalScheduleColumns.CONTENT_ITEM_TYPE;  
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
	  return insertExperiment(uri, values);
	case JOINED_EXPERIMENTS_DATATYPE:
	  ensureJoinDate(values);
	  return insertExperiment(uri, values);  
	case INPUTS_DATATYPE:
	  return insertInput(uri, values);
	case OUTPUTS_DATATYPE:
	      return insertOutput(uri, values);
    case EVENTS_DATATYPE:
      return insertEvent(uri, values);
    case FEEDBACK_DATATYPE:
      return insertFeedback(uri, values);
    case NOTIFICATION_DATATYPE:
      return insertNotification(uri, values);
    case SCHEDULE_DATATYPE:
      return insertSchedule(uri, values);
	default:
	  throw new IllegalArgumentException("Unknown Url: " + uri);
	}
  }

  private Uri insertSchedule(Uri uri, ContentValues values) {
    long rowId = db.insert(SCHEDULES_TABLE_NAME, SignalScheduleColumns.SERVER_ID, values);
    if (rowId > 0) {
      Uri resultUri = ContentUris.withAppendedId(SignalScheduleColumns.CONTENT_URI, rowId);
      getContext().getContentResolver().notifyChange(uri, null, true);
      return resultUri;
    }
    throw new SQLException("Failed to insert row into " + uri);

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

  private Uri insertFeedback(Uri uri, ContentValues values) {
    long rowId = db.insert(FEEDBACK_TABLE_NAME, FeedbackColumns.SERVER_ID, values);
    if (rowId > 0) {
      Uri resultUri = ContentUris.withAppendedId(FeedbackColumns.CONTENT_URI, rowId);
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

  private Uri insertInput(Uri uri, ContentValues values) {
    long rowId = db.insert(INPUTS_TABLE_NAME, InputColumns.TEXT, values);
    if (rowId > 0) {
      Uri resultUri = ContentUris.withAppendedId(InputColumns.CONTENT_URI, rowId);
      getContext().getContentResolver().notifyChange(uri, null, true);
      return resultUri;
    }
    throw new SQLException("Failed to insert row into " + uri);
  }

  private Uri insertExperiment(Uri uri, ContentValues values) {
	long rowId = db.insert(EXPERIMENTS_TABLE_NAME, ExperimentColumns.TITLE, values);
	if (rowId > 0) {
	  Uri experimentUri = ContentUris.withAppendedId(TextUtils.isEmpty(values.getAsString(ExperimentColumns.JOIN_DATE)) 
		  ? ExperimentColumns.CONTENT_URI 
		  : ExperimentColumns.JOINED_EXPERIMENTS_CONTENT_URI, 
		  rowId);
	  getContext().getContentResolver().notifyChange(uri, null, true);
	  return experimentUri;
	}
	throw new SQLException("Failed to insert row into " + uri);
  }

  @Override
  public int delete(Uri uri, String where, String[] whereArgs) {
	int count;
	switch (uriMatcher.match(uri)) {
	case EXPERIMENTS_DATATYPE:
	  count = db.delete(EXPERIMENTS_TABLE_NAME, 
		  where, //addJoinedExperimentExclusionClauseTo(where),
		  whereArgs);
	  break;
	case EXPERIMENT_ITEM_DATATYPE:
	  count = db.delete(EXPERIMENTS_TABLE_NAME, 
		  addIdEqualsClauseTo(where, getIdFromPath(uri)),
		  whereArgs);
	  break;
	case JOINED_EXPERIMENTS_DATATYPE:
	  count = db.delete(EXPERIMENTS_TABLE_NAME, 
		  addJoinedExperimentInclusionClauseTo(where), 
		  whereArgs);
	  break;
	case JOINED_EXPERIMENT_ITEM_DATATYPE:
	  count = db.delete(EXPERIMENTS_TABLE_NAME, 
		  addIdEqualsClauseTo(where, getIdFromPath(uri)),
		  whereArgs);
	  break;
	case INPUTS_DATATYPE:
      count = db.delete(INPUTS_TABLE_NAME, 
          where, 
          whereArgs);
      break;
    case INPUT_ITEM_DATATYPE:
    count = db.delete(INPUTS_TABLE_NAME, 
          addIdEqualsClauseTo(where, getIdFromPath(uri)),
          whereArgs);
      break;
    case OUTPUTS_DATATYPE:
      count = db.delete(OUTPUTS_TABLE_NAME, 
          where, 
          whereArgs);
      break;
    case OUTPUT_ITEM_DATATYPE:
      count = db.delete(OUTPUTS_TABLE_NAME, 
          addIdEqualsClauseTo(where, getIdFromPath(uri)),
          whereArgs);
      break;  
    case EVENTS_DATATYPE:
      count = db.delete(EVENTS_TABLE_NAME, 
          where, 
          whereArgs);
      break;
    case EVENT_ITEM_DATATYPE:
    count = db.delete(EVENTS_TABLE_NAME, 
          addIdEqualsClauseTo(where, getIdFromPath(uri)),
          whereArgs);
      break;  

    case FEEDBACK_DATATYPE:
      count = db.delete(FEEDBACK_TABLE_NAME, 
          where, 
          whereArgs);
      break;
    case FEEDBACK_ITEM_DATATYPE:
    count = db.delete(FEEDBACK_TABLE_NAME, 
          addIdEqualsClauseTo(where, getIdFromPath(uri)),
          whereArgs);
      break;  
    case NOTIFICATION_DATATYPE:
      count = db.delete(NOTIFICATION_TABLE_NAME, 
          where, 
          whereArgs);
      break;
    case NOTIFICATION_ITEM_DATATYPE:
    count = db.delete(NOTIFICATION_TABLE_NAME, 
          addIdEqualsClauseTo(where, getIdFromPath(uri)),
          whereArgs);
      break;  
    case SCHEDULE_DATATYPE:
      count = db.delete(SCHEDULES_TABLE_NAME, 
          where, 
          whereArgs);
      break;
    case SCHEDULE_ITEM_DATATYPE:
    count = db.delete(SCHEDULES_TABLE_NAME, 
          addIdEqualsClauseTo(where, getIdFromPath(uri)),
          whereArgs);
      break;

	default:
	  throw new IllegalArgumentException("Unknown URI " + uri);
	}

	getContext().getContentResolver().notifyChange(uri, null, true);
	return count;
  }

  @Override
  public int update(Uri uri, ContentValues values, String where,
	  String[] whereArgs) {
	int count;
	switch (uriMatcher.match(uri)) {
	case EXPERIMENTS_DATATYPE:
	  count = db.update(EXPERIMENTS_TABLE_NAME, values, 
		  addJoinedExperimentExclusionClauseTo(where), 
		  whereArgs);
	  break;

	case EXPERIMENT_ITEM_DATATYPE:
	  count = db.update(EXPERIMENTS_TABLE_NAME, values,
		  addIdEqualsClauseTo(where, getIdFromPath(uri)),
		  whereArgs);
	  break;

	case JOINED_EXPERIMENTS_DATATYPE:
	  count = db.update(EXPERIMENTS_TABLE_NAME, values, 
		  addJoinedExperimentInclusionClauseTo(where),
		  whereArgs);
	  break;

	case JOINED_EXPERIMENT_ITEM_DATATYPE:
	  count = db.update(EXPERIMENTS_TABLE_NAME, values,
		  addIdEqualsClauseTo(where, getIdFromPath(uri)),
		  whereArgs);
	  break;

    case INPUTS_DATATYPE:
      count = db.update(INPUTS_TABLE_NAME, values, 
          where,
          whereArgs);
      break;

    case INPUT_ITEM_DATATYPE:
      count = db.update(INPUTS_TABLE_NAME, values,
          addIdEqualsClauseTo(where, getIdFromPath(uri)),
          whereArgs);
      break;
      
    case OUTPUTS_DATATYPE:
      count = db.update(OUTPUTS_TABLE_NAME, values, 
          where,
          whereArgs);
      break;

    case OUTPUT_ITEM_DATATYPE:
      count = db.update(OUTPUTS_TABLE_NAME, values,
          addIdEqualsClauseTo(where, getIdFromPath(uri)),
          whereArgs);
      break;

    case EVENTS_DATATYPE:
      count = db.update(EVENTS_TABLE_NAME, values, 
          where,
          whereArgs);
      break;

    case EVENT_ITEM_DATATYPE:
      count = db.update(EVENTS_TABLE_NAME, values,
          addIdEqualsClauseTo(where, getIdFromPath(uri)),
          whereArgs);
      break;

    case FEEDBACK_DATATYPE:
      count = db.update(FEEDBACK_TABLE_NAME, values,
          where, 
          whereArgs);
      break;
    case FEEDBACK_ITEM_DATATYPE:
    count = db.update(FEEDBACK_TABLE_NAME, values, 
          addIdEqualsClauseTo(where, getIdFromPath(uri)),
          whereArgs);
      break;  
    case NOTIFICATION_DATATYPE:
      count = db.update(NOTIFICATION_TABLE_NAME, values,
          where, 
          whereArgs);
      break;
    case NOTIFICATION_ITEM_DATATYPE:
    count = db.update(NOTIFICATION_TABLE_NAME, values, 
        addIdEqualsClauseTo(where, getIdFromPath(uri)),
        whereArgs);
      break;  
    case SCHEDULE_DATATYPE:
      count = db.update(SCHEDULES_TABLE_NAME, values, 
          where,
          whereArgs);
      break;

    case SCHEDULE_ITEM_DATATYPE:
      count = db.update(SCHEDULES_TABLE_NAME, values,
          addIdEqualsClauseTo(where, getIdFromPath(uri)),
          whereArgs);
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


  static {
	// Support for Live Folders.
	liveFolderProjectionMap = new HashMap<String, String>();
	liveFolderProjectionMap.put(LiveFolders._ID, ExperimentColumns._ID + " AS " + LiveFolders._ID);
	liveFolderProjectionMap.put(LiveFolders.NAME, ExperimentColumns.TITLE + " AS " + LiveFolders.NAME);
	// Add more columns here for more robust Live Folders.
  }
  
}
