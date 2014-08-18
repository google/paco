package com.google.android.apps.paco;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.common.collect.Lists;
import com.google.paco.shared.model.FeedbackDAO;

/**
 * This class helps open, create, and upgrade the database file.
 */
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

    db.execSQL("CREATE TABLE " + ExperimentProvider.NOTIFICATION_TABLE_NAME + " ("
        + NotificationHolderColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
        + NotificationHolderColumns.ALARM_TIME + " INTEGER, "
        + NotificationHolderColumns.EXPERIMENT_ID + " INTEGER, "
        + NotificationHolderColumns.NOTICE_COUNT + " INTEGER, "
        + NotificationHolderColumns.TIMEOUT_MILLIS + " INTEGER, "
        + NotificationHolderColumns.NOTIFICATION_SOURCE + " TEXT, "
        + NotificationHolderColumns.CUSTOM_MESSAGE + " TEXT"
        + ");");

    //	  insertValues(db);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Log.w(ExperimentProvider.TAG, "Upgrading database from version " + oldVersion + " to "
        + newVersion + ".");

    if (oldVersion <= 12) {
      throw new IllegalStateException("Your paco client is too old. Uninstall this version and install the new one");
      // TODO create a notification to the user instead.
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
    if (oldVersion <= 15) {
      ExperimentProviderUtil eu = new ExperimentProviderUtil(context);
      List<Experiment> joined = eu.getJoinedExperiments();

      for (Experiment experiment : joined) {
      //verify that feedbackType is correct
        if (experiment.getFeedbackType() == null || experiment.getFeedbackType() == FeedbackDAO.FEEDBACK_TYPE_RETROSPECTIVE) { // the default
          // if it is our default value make sure that it is not actually custom code.
          if (!FeedbackDAO.DEFAULT_FEEDBACK_MSG.equals(experiment.getFeedback().get(0).getText())) {
            experiment.setFeedbackType(FeedbackDAO.FEEDBACK_TYPE_CUSTOM);
            eu.updateJoinedExperiment(experiment);
          }

        }
      }
      eu.deleteExperimentCachesOnDisk();
    }
    if (oldVersion <= 20) {
      db.execSQL("ALTER TABLE " + ExperimentProvider.NOTIFICATION_TABLE_NAME + " ADD "
              + NotificationHolderColumns.NOTIFICATION_SOURCE + " TEXT default " + SignalingMechanism.DEFAULT_SIGNALING_GROUP_NAME
              + ";");
      db.execSQL("ALTER TABLE " + ExperimentProvider.NOTIFICATION_TABLE_NAME + " ADD "
              + NotificationHolderColumns.CUSTOM_MESSAGE + " TEXT;");
    }
    if (oldVersion <= 21) {
      List<Experiment> joined = Lists.newArrayList();
      Cursor experimentCursor = db.query(ExperimentProvider.EXPERIMENTS_TABLE_NAME, null, null, null, null, null, null);
      while (experimentCursor.moveToNext()) {
        Experiment experiment = ExperimentProviderUtil.createExperiment(experimentCursor);
        joined.add(experiment);
      }
      experimentCursor.close();
      for (Experiment experiment : joined) {
        Log.i(PacoConstants.TAG, "Re-assembling experiment: id: " + experiment.getId());
        //Log.i(PacoConstants.TAG, "Current json for experiment:\n " + ExperimentProviderUtil.getJson(experiment));

        // inputs
        List<Input> inputsList = Lists.newArrayList();
        Cursor inputCursor = db.query("inputs", null, ExperimentProviderUtil.InputColumns.EXPERIMENT_ID + " = " + experiment.getId(), null, null, null, null, null);
        while (inputCursor.moveToNext()) {
          Input input = ExperimentProviderUtil.createInput(inputCursor);
          inputsList.add(input);
        }
        //Log.i(PacoConstants.TAG, "Experiment: " + experiment.getTitle() +" has " + inputsList.size() +" inputs");
        experiment.setInputs(inputsList);
        inputCursor.close();
        // feedback
        List<Feedback> feedbackList = Lists.newArrayList();
        Cursor feedbackCursor = db.query("feedback", null, ExperimentProviderUtil.FeedbackColumns.EXPERIMENT_ID + " = " + experiment.getId(), null, null, null, null, null);
        while (feedbackCursor.moveToNext()) {
          Feedback feedback = ExperimentProviderUtil.createFeedback(feedbackCursor);
          feedbackList.add(feedback);
        }
        experiment.setFeeback(feedbackList);
        //Log.i(PacoConstants.TAG, "Experiment: " + experiment.getTitle() +" has " + feedbackList.size() +" feedbacks");
        feedbackCursor.close();
        // signalingMechanisms
        List<SignalingMechanism> scheduleList = Lists.newArrayList();
        Cursor scheduleCursor = db.query("schedules", null, ExperimentProviderUtil.SignalScheduleColumns.EXPERIMENT_ID + " = " + experiment.getId(), null, null, null, null, null);
        while (scheduleCursor.moveToNext()) {
          SignalingMechanism schedule = ExperimentProviderUtil.createSchedule(scheduleCursor);
          scheduleList.add(schedule);
        }
        //Log.i(PacoConstants.TAG, "Experiment: " + experiment.getTitle() +" has " + scheduleList.size() +" schedules");
        if (!scheduleList.isEmpty()) {
          experiment.setSignalingMechanisms(scheduleList);
        }
        scheduleCursor.close();
        //Log.i(PacoConstants.TAG, "Final json for experiment:\n " + ExperimentProviderUtil.getJson(experiment));
        db.update(ExperimentProvider.EXPERIMENTS_TABLE_NAME, ExperimentProviderUtil.createContentValues(experiment), ExperimentColumns._ID + " = " +experiment.getId(), null);
      }

      // might need to make the json field of experiments up-to-date
      db.execSQL("DROP TABLE " + "schedules" + ";");
      db.execSQL("DROP TABLE " + "inputs" + ";");
      db.execSQL("DROP TABLE " + "feedback" + ";");
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
