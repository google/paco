package com.google.android.apps.paco;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
   * This class helps open, create, and upgrade the database file.
   */
  public class DatabaseHelper extends SQLiteOpenHelper {

//	private InputStream sqlInput;

	DatabaseHelper(Context context/*, InputStream in*/) {
	  super(context, ExperimentProvider.DATABASE_NAME, null, ExperimentProvider.DATABASE_VERSION);
//	  this.sqlInput = in;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
	  db.execSQL("CREATE TABLE " + ExperimentProvider.EXPERIMENTS_TABLE_NAME + " ("
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
	  db.execSQL("CREATE TABLE " + ExperimentProvider.SCHEDULES_TABLE_NAME + " ("
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
      db.execSQL("CREATE TABLE " + ExperimentProvider.INPUTS_TABLE_NAME + " ("
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
      db.execSQL("CREATE TABLE " + ExperimentProvider.EVENTS_TABLE_NAME + " ("
          + EventColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "          
          + EventColumns.EXPERIMENT_ID + " INTEGER, "
          + EventColumns.EXPERIMENT_SERVER_ID + " INTEGER, "
          + EventColumns.EXPERIMENT_NAME + " TEXT, "
          + EventColumns.SCHEDULE_TIME + " INTEGER, "
          + EventColumns.RESPONSE_TIME + " INTEGER,"
          + EventColumns.UPLOADED + " INTEGER"
          + ");");
      db.execSQL("CREATE TABLE " + ExperimentProvider.OUTPUTS_TABLE_NAME + " ("
          + OutputColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
          + OutputColumns.EVENT_ID + " INTEGER, "
          + OutputColumns.INPUT_SERVER_ID + " INTEGER, "          
          + OutputColumns.NAME + " TEXT,"
          + OutputColumns.ANSWER + " TEXT"
          + ");");
      db.execSQL("CREATE TABLE " + ExperimentProvider.FEEDBACK_TABLE_NAME + " ("
          + FeedbackColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
          + FeedbackColumns.EXPERIMENT_ID + " INTEGER, "
          + FeedbackColumns.SERVER_ID + " INTEGER, "
          + FeedbackColumns.FEEDBACK_TYPE + " TEXT,"
          + FeedbackColumns.TEXT + " TEXT"
          + ");");
      
      db.execSQL("CREATE TABLE " + ExperimentProvider.NOTIFICATION_TABLE_NAME + " ("
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
	  Log.w(ExperimentProvider.TAG, "Upgrading database from version " + oldVersion + " to "
		  + newVersion + ".");
	  
	  if (oldVersion <= 8) {
      db.execSQL("ALTER TABLE " + ExperimentProvider.INPUTS_TABLE_NAME + " ADD "
          + InputColumns.MULTISELECT + " INTEGER default 0"
          + ";");
	  }  
	  
	  if (oldVersion <= 9) {
	    db.execSQL("ALTER TABLE " + ExperimentProvider.SCHEDULES_TABLE_NAME + " ADD "
          + SignalScheduleColumns.USER_EDITABLE + " INTEGER default 1"
          + ";");
	  }
	  if (oldVersion <= 10) {
      db.execSQL("ALTER TABLE " + ExperimentProvider.EXPERIMENTS_TABLE_NAME+ " ADD "
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