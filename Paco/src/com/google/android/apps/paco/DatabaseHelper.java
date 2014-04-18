package com.google.android.apps.paco;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.paco.shared.model.FeedbackDAO;

/**
 * This class helps open, create, and upgrade the database file.
 */
@SuppressLint("UseSparseArrays")
public class DatabaseHelper extends SQLiteOpenHelper {

  private Context context;

	// Visible for testing
  public DatabaseHelper(Context context/*, InputStream in*/) {
	  super(context, ExperimentProvider.DATABASE_NAME, null, ExperimentProvider.DATABASE_VERSION);
//	  this.sqlInput = in;
	  this.context = context;
	}

  // For testing
  DatabaseHelper(Context context, String dbName, int dbVersion) {
    super(context, dbName, null, dbVersion);
    this.context = context;
  }


  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL("CREATE TABLE " + ExperimentProvider.EXPERIMENTS_TABLE_NAME + " ("
        + ExperimentColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
        + ExperimentColumns.SERVER_ID + " INTEGER,"
        + ExperimentColumns.TITLE + " TEXT, "
        + ExperimentColumns.VERSION + " INTEGER,"
        + ExperimentColumns.DESCRIPTION + " TEXT, "
        + ExperimentColumns.CREATOR + " TEXT, "
        + ExperimentColumns.INFORMED_CONSENT + " TEXT, "
        + ExperimentColumns.HASH + " TEXT, "
        + ExperimentColumns.FIXED_DURATION + " INTEGER, "
        + ExperimentColumns.START_DATE + " TEXT, "
        + ExperimentColumns.END_DATE + " TEXT, "
        + ExperimentColumns.JOIN_DATE + " TEXT, "
        + ExperimentColumns.QUESTIONS_CHANGE + " INTEGER, "
        + ExperimentColumns.ICON + " BLOB, "
        + ExperimentColumns.WEB_RECOMMENDED + " INTEGER, "
        + ExperimentColumns.JSON + " TEXT "
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
        + SignalScheduleColumns.USER_EDITABLE + " INTEGER, "
        + SignalScheduleColumns.TIME_OUT + " INTEGER, "
        + SignalScheduleColumns.MINIMUM_BUFFER + " INTEGER, "
        + SignalScheduleColumns.SNOOZE_COUNT + " INTEGER, "
        + SignalScheduleColumns.SNOOZE_TIME + " INTEGER "
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
        + EventColumns.EXPERIMENT_VERSION + " INTEGER, "
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
      db.execSQL("ALTER TABLE " + ExperimentProvider.EXPERIMENTS_TABLE_NAME + " ADD "
          + ExperimentColumns.WEB_RECOMMENDED + " INTEGER default 0"
          + ";");
    }
    if (oldVersion <= 11) {
      db.execSQL("ALTER TABLE " + ExperimentProvider.EXPERIMENTS_TABLE_NAME + " ADD "
          + ExperimentColumns.VERSION + " INTEGER default 0"
          + ";");
      db.execSQL("ALTER TABLE " + ExperimentProvider.EVENTS_TABLE_NAME + " ADD "
          + EventColumns.EXPERIMENT_VERSION + " INTEGER default 0"
          + ";");
      db.execSQL("ALTER TABLE " + ExperimentProvider.SCHEDULES_TABLE_NAME + " ADD "
          + SignalScheduleColumns.TIME_OUT + " INTEGER"
          + ";");
    }
    if (oldVersion <= 12) {
      db.execSQL("ALTER TABLE " + ExperimentProvider.EXPERIMENTS_TABLE_NAME + " ADD "
          + ExperimentColumns.JSON + " TEXT "
          + ";");
      ExperimentProviderUtil eu = new ExperimentProviderUtil(context);
      List<Experiment> joined = eu.getJoinedExperiments();

      for (Experiment experiment : joined) {
        eu.updateJoinedExperiment(experiment);
      }
    }
    if (oldVersion <= 13) {

      HashMap<Integer, String> startDatePairs = convertDateLongsToStrings(db,
                                                                          ExperimentProvider.EXPERIMENTS_TABLE_NAME,
                                                                          ExperimentColumns.START_DATE, ExperimentColumns._ID);
      HashMap<Integer, String> endDatePairs = convertDateLongsToStrings(db,
                                                                        ExperimentProvider.EXPERIMENTS_TABLE_NAME,
                                                                        ExperimentColumns.END_DATE, ExperimentColumns._ID);
      HashMap<Integer, String> joinDatePairs = convertDateLongsToTzStrings(db,
                                                                           ExperimentProvider.EXPERIMENTS_TABLE_NAME,
                                                                           ExperimentColumns.JOIN_DATE, ExperimentColumns._ID);
      createTruncatedExperimentsTable(db);
      insertNewDateColumnWithData(db, ExperimentProvider.EXPERIMENTS_TABLE_NAME, startDatePairs,
                                  ExperimentColumns.START_DATE, ExperimentColumns._ID);
      insertNewDateColumnWithData(db, ExperimentProvider.EXPERIMENTS_TABLE_NAME, endDatePairs,
                                  ExperimentColumns.END_DATE, ExperimentColumns._ID);
      insertNewDateColumnWithData(db, ExperimentProvider.EXPERIMENTS_TABLE_NAME, joinDatePairs,
                                  ExperimentColumns.JOIN_DATE, ExperimentColumns._ID);
    }
    if (oldVersion <= 14) {
      db.execSQL("ALTER TABLE " + ExperimentProvider.SCHEDULES_TABLE_NAME + " ADD "
              + SignalScheduleColumns.MINIMUM_BUFFER + " INTEGER"
              + ";");
    }
    if (oldVersion <= 15) {
      ExperimentProviderUtil eu = new ExperimentProviderUtil(context);
      List<Experiment> joined = eu.getJoinedExperiments();

      for (Experiment experiment : joined) {
      //verify that feedbackType is correct
        if (experiment.getFeedbackType() == null || experiment.getFeedbackType() == FeedbackDAO.FEEDBACK_TYPE_RETROSPECTIVE) { // the default
          eu.loadFeedbackForExperiment(experiment);
          // if it is our default value make sure that it is not actually custom code.
          if (!FeedbackDAO.DEFAULT_FEEDBACK_MSG.equals(experiment.getFeedback().get(0).getText())) {
            experiment.setFeedbackType(FeedbackDAO.FEEDBACK_TYPE_CUSTOM);
            eu.updateJoinedExperiment(experiment);
          }

        }
      }
      eu.deleteExperimentCachesOnDisk();
    }
    if (oldVersion <= 16) {
      db.execSQL("ALTER TABLE " + ExperimentProvider.SCHEDULES_TABLE_NAME + " ADD "
              + SignalScheduleColumns.SNOOZE_COUNT + " INTEGER"
              + ";");
      db.execSQL("ALTER TABLE " + ExperimentProvider.SCHEDULES_TABLE_NAME + " ADD "
              + SignalScheduleColumns.SNOOZE_TIME + " INTEGER"
              + ";");
    }
  }

  private static HashMap<Integer, String> convertDateLongsToStrings(SQLiteDatabase db,
                                                                    String tableName,
                                                                    String dateCol, String refCol) {
    String[] columns = {dateCol, refCol};
    HashMap<Integer, String> data = new HashMap<Integer, String>();
    Cursor cursor = db.query(tableName, columns, null, null, null, null, null);

    try {
      if (cursor != null) {
        while (cursor.moveToNext()) {
          Long longVal = cursor.getLong(cursor.getColumnIndex(dateCol));
          if (longVal != null) {
            String dateStr = TimeUtil.formatDate(longVal);
            Integer id = cursor.getInt(cursor.getColumnIndex(refCol));
            data.put(id, dateStr);
          }
        }
      }
    } finally {
      cursor.close();
    }
    return data;
  }

  private static HashMap<Integer, String> convertDateLongsToTzStrings(SQLiteDatabase db,
                                                                      String tableName,
                                                                      String dateCol, String refCol) {
    String[] columns = {dateCol, refCol};
    HashMap<Integer, String> data = new HashMap<Integer, String>();
    Cursor cursor = db.query(tableName, columns, null, null, null, null, null);

    try {
      if (cursor != null) {
        while (cursor.moveToNext()) {
          Long longVal = cursor.getLong(cursor.getColumnIndex(dateCol));
          if (longVal != null) {
            String dateStr = TimeUtil.formatDateWithZone(longVal);
            Integer id = cursor.getInt(cursor.getColumnIndex(refCol));
            data.put(id, dateStr);
          }
        }
      }
    } finally {
      cursor.close();
    }
    return data;
  }


  private static void createTruncatedExperimentsTable(SQLiteDatabase db) {
    String tempTable = "tempTable";
    db.execSQL("CREATE TABLE " + tempTable + " ("
        + ExperimentColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
        + ExperimentColumns.SERVER_ID + " INTEGER,"
        + ExperimentColumns.TITLE + " TEXT, "
        + ExperimentColumns.VERSION + " INTEGER,"
        + ExperimentColumns.DESCRIPTION + " TEXT, "
        + ExperimentColumns.CREATOR + " TEXT, "
        + ExperimentColumns.INFORMED_CONSENT + " TEXT, "
        + ExperimentColumns.HASH + " TEXT, "
        + ExperimentColumns.FIXED_DURATION + " INTEGER, "
        + ExperimentColumns.QUESTIONS_CHANGE + " INTEGER, "
        + ExperimentColumns.ICON + " BLOB, "
        + ExperimentColumns.WEB_RECOMMENDED + " INTEGER, "
        + ExperimentColumns.JSON + " TEXT "
        + ");");
    db.execSQL("INSERT INTO " + tempTable + " (" +
        ExperimentColumns._ID + ", " +
        ExperimentColumns.SERVER_ID + ", " +
        ExperimentColumns.TITLE + ", " +
        ExperimentColumns.VERSION + ", " +
        ExperimentColumns.DESCRIPTION + ", " +
        ExperimentColumns.CREATOR + ", " +
        ExperimentColumns.INFORMED_CONSENT + ", " +
        ExperimentColumns.HASH + ", " +
        ExperimentColumns.FIXED_DURATION + ", " +
        ExperimentColumns.QUESTIONS_CHANGE + ", " +
        ExperimentColumns.ICON + ", " +
        ExperimentColumns.WEB_RECOMMENDED + ", " +
        ExperimentColumns.JSON +
        ") " + "SELECT " +
        ExperimentColumns._ID + ", " +
        ExperimentColumns.SERVER_ID + ", " +
        ExperimentColumns.TITLE + ", " +
        ExperimentColumns.VERSION + ", " +
        ExperimentColumns.DESCRIPTION + ", " +
        ExperimentColumns.CREATOR + ", " +
        ExperimentColumns.INFORMED_CONSENT + ", " +
        ExperimentColumns.HASH + ", " +
        ExperimentColumns.FIXED_DURATION + ", " +
        ExperimentColumns.QUESTIONS_CHANGE + ", " +
        ExperimentColumns.ICON + ", " +
        ExperimentColumns.WEB_RECOMMENDED + ", " +
        ExperimentColumns.JSON +
        " FROM " + ExperimentProvider.EXPERIMENTS_TABLE_NAME + ";");
    db.execSQL("DROP TABLE " + ExperimentProvider.EXPERIMENTS_TABLE_NAME);
    db.execSQL("ALTER TABLE " + tempTable + " RENAME TO " + ExperimentProvider.EXPERIMENTS_TABLE_NAME);
  }

  private static void insertNewDateColumnWithData(SQLiteDatabase db, String tableName,
                                                  HashMap<Integer,String> data,
                                                  String dateCol, String refCol) {
    db.execSQL("ALTER TABLE " + tableName + " ADD " + dateCol + " TEXT " + ";");
    for (Map.Entry<Integer, String> entry : data.entrySet()) {
      db.execSQL("UPDATE " + tableName +
                 " SET " + dateCol + " = " + "\'" + entry.getValue() + "\'" +
                 " WHERE " + refCol + " = " + entry.getKey() + ";");
    }
  }
}
