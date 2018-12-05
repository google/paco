package com.pacoapp.paco.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.PacoNotificationAction;
import com.pacoapp.paco.shared.util.ErrorMessages;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

/**
 * This class helps open, create, and upgrade the database file.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

  private static Logger Log = LoggerFactory.getLogger(DatabaseHelper.class);

  private Context context;

  // Visible for testing
  public DatabaseHelper(Context context/* , InputStream in */) {
    super(context, ExperimentProvider.DATABASE_NAME, null, ExperimentProvider.DATABASE_VERSION);
    // this.sqlInput = in;
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
            + ExperimentColumns.JOIN_DATE + " TEXT, "
            + ExperimentColumns.JSON + " TEXT " + ");");

    db.execSQL("CREATE TABLE " + ExperimentProvider.EVENTS_TABLE_NAME + " ("
            + EventColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + EventColumns.EXPERIMENT_ID + " INTEGER, "
            + EventColumns.EXPERIMENT_SERVER_ID + " INTEGER, "
            + EventColumns.EXPERIMENT_NAME + " TEXT, "
            + EventColumns.EXPERIMENT_VERSION + " INTEGER, "
            + EventColumns.SCHEDULE_TIME + " INTEGER, "
            + EventColumns.RESPONSE_TIME + " INTEGER, "
            + EventColumns.UPLOADED + " INTEGER, "
            + EventColumns.GROUP_NAME + " TEXT, "
            + EventColumns.ACTION_TRIGGER_ID + " INTEGER, "
            + EventColumns.ACTION_TRIGGER_SPEC_ID + " INTEGER, "
            + EventColumns.ACTION_ID + " INTEGER "
            + ");");

    db.execSQL("CREATE TABLE " + ExperimentProvider.OUTPUTS_TABLE_NAME + " ("
            + OutputColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + OutputColumns.EVENT_ID + " INTEGER, "
            + OutputColumns.INPUT_SERVER_ID + " INTEGER, "
            + OutputColumns.NAME + " TEXT,"
            + OutputColumns.ANSWER + " TEXT" + ");");

    db.execSQL("CREATE TABLE " + ExperimentProvider.NOTIFICATION_TABLE_NAME + " ("
            + NotificationHolderColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + NotificationHolderColumns.ALARM_TIME + " INTEGER, "
            + NotificationHolderColumns.EXPERIMENT_ID + " INTEGER, "
            + NotificationHolderColumns.NOTICE_COUNT + " INTEGER, "
            + NotificationHolderColumns.TIMEOUT_MILLIS + " INTEGER, "
            + NotificationHolderColumns.NOTIFICATION_SOURCE + " TEXT, "
            + NotificationHolderColumns.CUSTOM_MESSAGE + " TEXT,"
            + NotificationHolderColumns.SNOOZE_COUNT + " INTEGER, "
            + NotificationHolderColumns.SNOOZE_TIME + " INTEGER, "
            + NotificationHolderColumns.EXPERIMENT_GROUP_NAME + " TEXT, "
            + NotificationHolderColumns.ACTION_TRIGGER_ID + " INTEGER, "
            + NotificationHolderColumns.ACTION_ID + " INTEGER, "
            + NotificationHolderColumns.ACTION_TRIGGER_SPEC_ID + " INTEGER "
            + ");");

    // insertValues(db);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Log.warn("Upgrading database from version " + oldVersion + " to " + newVersion + ".");

    // if (oldVersion <= 12) {
    // throw new
    // IllegalStateException("Your paco client is too old. Uninstall this version and install the new one");
    // // TODO create a notification to the user instead.
    // }
    // if (oldVersion <= 13) {
    //
    // HashMap<Integer, String> startDatePairs = convertDateLongsToStrings(db,
    // ExperimentProvider.EXPERIMENTS_TABLE_NAME,
    // ExperimentColumns.START_DATE, ExperimentColumns._ID);
    // HashMap<Integer, String> endDatePairs = convertDateLongsToStrings(db,
    // ExperimentProvider.EXPERIMENTS_TABLE_NAME,
    // ExperimentColumns.END_DATE, ExperimentColumns._ID);
    // HashMap<Integer, String> joinDatePairs = convertDateLongsToTzStrings(db,
    // ExperimentProvider.EXPERIMENTS_TABLE_NAME,
    // ExperimentColumns.JOIN_DATE, ExperimentColumns._ID);
    // createTruncatedExperimentsTable(db);
    // insertNewDateColumnWithData(db,
    // ExperimentProvider.EXPERIMENTS_TABLE_NAME, startDatePairs,
    // ExperimentColumns.START_DATE, ExperimentColumns._ID);
    // insertNewDateColumnWithData(db,
    // ExperimentProvider.EXPERIMENTS_TABLE_NAME, endDatePairs,
    // ExperimentColumns.END_DATE, ExperimentColumns._ID);
    // insertNewDateColumnWithData(db,
    // ExperimentProvider.EXPERIMENTS_TABLE_NAME, joinDatePairs,
    // ExperimentColumns.JOIN_DATE, ExperimentColumns._ID);
    // }
    // if (oldVersion <= 15) {
    // ExperimentProviderUtil eu = new ExperimentProviderUtil(context);
    // List<Experiment> joined = eu.getJoinedExperiments();
    //
    // for (Experiment experiment : joined) {
    // //verify that feedbackType is correct
    // if (experiment.getFeedbackType() == null || experiment.getFeedbackType()
    // == FeedbackDAO.FEEDBACK_TYPE_RETROSPECTIVE) { // the default
    // // if it is our default value make sure that it is not actually custom
    // code.
    // if
    // (!FeedbackDAO.DEFAULT_FEEDBACK_MSG.equals(experiment.getFeedback().get(0).getText()))
    // {
    // experiment.setFeedbackType(FeedbackDAO.FEEDBACK_TYPE_CUSTOM);
    // eu.updateJoinedExperiment(experiment);
    // }
    //
    // }
    // }
    // eu.deleteExperimentCachesOnDisk();
    // }
    // if (oldVersion <= 20) {
    // db.execSQL("ALTER TABLE " + ExperimentProvider.NOTIFICATION_TABLE_NAME +
    // " ADD "
    // + NotificationHolderColumns.NOTIFICATION_SOURCE + " TEXT default " +
    // SignalingMechanism.DEFAULT_SIGNALING_GROUP_NAME
    // + ";");
    // db.execSQL("ALTER TABLE " + ExperimentProvider.NOTIFICATION_TABLE_NAME +
    // " ADD "
    // + NotificationHolderColumns.CUSTOM_MESSAGE + " TEXT;");
    // }
    // if (oldVersion <= 21) {
    // List<Experiment> joined = Lists.newArrayList();
    // Cursor experimentCursor =
    // db.query(ExperimentProvider.EXPERIMENTS_TABLE_NAME, null, null, null,
    // null, null, null);
    // while (experimentCursor.moveToNext()) {
    // Experiment experiment =
    // ExperimentProviderUtil.createExperiment(experimentCursor);
    // joined.add(experiment);
    // }
    // experimentCursor.close();
    // for (Experiment experiment : joined) {
    // Log.i(PacoConstants.TAG, "Re-assembling experiment: id: " +
    // experiment.getId());
    // //Log.i(PacoConstants.TAG, "Current json for experiment:\n " +
    // ExperimentProviderUtil.getJson(experiment));
    //
    // // inputs
    // List<Input> inputsList = Lists.newArrayList();
    // Cursor inputCursor = db.query("inputs", null,
    // ExperimentProviderUtil.InputColumns.EXPERIMENT_ID + " = " +
    // experiment.getId(), null, null, null, null, null);
    // while (inputCursor.moveToNext()) {
    // Input input = ExperimentProviderUtil.createInput(inputCursor);
    // inputsList.add(input);
    // }
    // //Log.i(PacoConstants.TAG, "Experiment: " + experiment.getTitle()
    // +" has " + inputsList.size() +" inputs");
    // experiment.setInputs(inputsList);
    // inputCursor.close();
    // // feedback
    // List<Feedback> feedbackList = Lists.newArrayList();
    // Cursor feedbackCursor = db.query("feedback", null,
    // ExperimentProviderUtil.FeedbackColumns.EXPERIMENT_ID + " = " +
    // experiment.getId(), null, null, null, null, null);
    // while (feedbackCursor.moveToNext()) {
    // Feedback feedback =
    // ExperimentProviderUtil.createFeedback(feedbackCursor);
    // feedbackList.add(feedback);
    // }
    // experiment.setFeeback(feedbackList);
    // //Log.i(PacoConstants.TAG, "Experiment: " + experiment.getTitle()
    // +" has " + feedbackList.size() +" feedbacks");
    // feedbackCursor.close();
    // // signalingMechanisms
    // List<SignalingMechanism> scheduleList = Lists.newArrayList();
    // Cursor scheduleCursor = db.query("schedules", null,
    // ExperimentProviderUtil.SignalScheduleColumns.EXPERIMENT_ID + " = " +
    // experiment.getId(), null, null, null, null, null);
    // while (scheduleCursor.moveToNext()) {
    // SignalingMechanism schedule =
    // ExperimentProviderUtil.createSchedule(scheduleCursor);
    // scheduleList.add(schedule);
    // }
    // //Log.i(PacoConstants.TAG, "Experiment: " + experiment.getTitle()
    // +" has " + scheduleList.size() +" schedules");
    // if (!scheduleList.isEmpty()) {
    // experiment.setSignalingMechanisms(scheduleList);
    // }
    // scheduleCursor.close();
    // //Log.i(PacoConstants.TAG, "Final json for experiment:\n " +
    // ExperimentProviderUtil.getJson(experiment));
    // db.update(ExperimentProvider.EXPERIMENTS_TABLE_NAME,
    // ExperimentProviderUtil.createContentValues(experiment),
    // ExperimentColumns._ID + " = " +experiment.getId(), null);
    // }
    //
    // // might need to make the json field of experiments up-to-date
    // db.execSQL("DROP TABLE " + "schedules" + ";");
    // db.execSQL("DROP TABLE " + "inputs" + ";");
    // db.execSQL("DROP TABLE " + "feedback" + ";");
    // }
    if (oldVersion < 22) {
      throw new IllegalArgumentException("This version is to old to be updated. Please uninstall and reinstall Paco.");

    }
    if (oldVersion == 22) {
      // loadAllExperiments with existing json

      rewriteJsonOfAllExperiments(db);

      // delete cached downloaded experiments
      ExperimentProviderUtil.deleteExperimentCachesOnDisk(context);

      // remove obsolete columns on table.
//      db.execSQL("ALTER TABLE " + ExperimentProvider.EXPERIMENTS_TABLE_NAME + " DROP COLUMN "
//                 + "version" + ";");
//      db.execSQL("ALTER TABLE " + ExperimentProvider.EXPERIMENTS_TABLE_NAME + " DROP COLUMN "
//                 + "description" + ";");
//      db.execSQL("ALTER TABLE " + ExperimentProvider.EXPERIMENTS_TABLE_NAME + " DROP COLUMN "
//                 + "creator" + ";");
//      db.execSQL("ALTER TABLE " + ExperimentProvider.EXPERIMENTS_TABLE_NAME + " DROP COLUMN "
//                 + "informed_consent" + ";");
//      db.execSQL("ALTER TABLE " + ExperimentProvider.EXPERIMENTS_TABLE_NAME + " DROP COLUMN "
//                 + "hash" + ";");
//      db.execSQL("ALTER TABLE " + ExperimentProvider.EXPERIMENTS_TABLE_NAME + " DROP COLUMN "
//                 + "fixed_duration" + ";");
//      db.execSQL("ALTER TABLE " + ExperimentProvider.EXPERIMENTS_TABLE_NAME + " DROP COLUMN "
//                 + "start_date" + ";");
//      db.execSQL("ALTER TABLE " + ExperimentProvider.EXPERIMENTS_TABLE_NAME + " DROP COLUMN "
//                 + "end_date" + ";");

      //update to pass information along in notifications
      db.execSQL("ALTER TABLE " + ExperimentProvider.NOTIFICATION_TABLE_NAME + " ADD COLUMN "
              + NotificationHolderColumns.SNOOZE_COUNT + " INTEGER default " + PacoNotificationAction.SNOOZE_COUNT_DEFAULT + ";");
      db.execSQL("ALTER TABLE " + ExperimentProvider.NOTIFICATION_TABLE_NAME + " ADD COLUMN "
              + NotificationHolderColumns.SNOOZE_TIME + " INTEGER default " + PacoNotificationAction.SNOOZE_TIME_DEFAULT + ";");
      db.execSQL("ALTER TABLE " + ExperimentProvider.NOTIFICATION_TABLE_NAME + " ADD COLUMN "
              + NotificationHolderColumns.EXPERIMENT_GROUP_NAME + " TEXT;");
      db.execSQL("ALTER TABLE " + ExperimentProvider.NOTIFICATION_TABLE_NAME + " ADD COLUMN "
              + NotificationHolderColumns.ACTION_TRIGGER_ID + " INTEGER ;");
      db.execSQL("ALTER TABLE " + ExperimentProvider.NOTIFICATION_TABLE_NAME + " ADD COLUMN "
              + NotificationHolderColumns.ACTION_ID + " INTEGER;");
      db.execSQL("ALTER TABLE " + ExperimentProvider.NOTIFICATION_TABLE_NAME + " ADD COLUMN "
              + NotificationHolderColumns.ACTION_TRIGGER_SPEC_ID + " INTEGER;");

      db.execSQL("ALTER TABLE " + ExperimentProvider.EVENTS_TABLE_NAME + " ADD COLUMN "
              + EventColumns.GROUP_NAME + " TEXT;");
      db.execSQL("ALTER TABLE " + ExperimentProvider.EVENTS_TABLE_NAME + " ADD COLUMN "
              + EventColumns.ACTION_TRIGGER_ID + " INTEGER;");
      db.execSQL("ALTER TABLE " + ExperimentProvider.EVENTS_TABLE_NAME + " ADD COLUMN "
              + EventColumns.ACTION_TRIGGER_SPEC_ID + " INTEGER;");
      db.execSQL("ALTER TABLE " + ExperimentProvider.EVENTS_TABLE_NAME + " ADD COLUMN "
              + EventColumns.ACTION_ID + " INTEGER;");
    }
  }

  private void rewriteJsonOfAllExperiments(SQLiteDatabase db) {
    // TODO Auto-generated method stub
    List<Experiment> joined = getExperimentsWithDAO(db);
    for (Experiment experiment : joined) {
      updateJoinedExperiment(db, experiment);
    }
  }

  private void updateJoinedExperiment(SQLiteDatabase db, Experiment experiment) {
    int count = db.update(ExperimentProvider.EXPERIMENTS_TABLE_NAME,
        ExperimentProviderUtil.createContentValues(experiment),
        ExperimentColumns._ID + "=" + experiment.getId(), null);

  }

  public List<Experiment> getExperimentsWithDAO(SQLiteDatabase db) {
    List<Experiment> experiments = new ArrayList<Experiment>();
    Cursor cursor = null;
    try {
      cursor = db.query(ExperimentProvider.EXPERIMENTS_TABLE_NAME,
          null, null, null, null, null, null);
      if (cursor != null) {
        int idIndex = cursor.getColumnIndex(ExperimentColumns._ID);
        int jsonIndex = cursor.getColumnIndex(ExperimentColumns.JSON);

        while (cursor.moveToNext()) {
          try {
            Experiment experiment = new Experiment();
            ExperimentDAO experimentDAO = new ExperimentDAO();

            if (!cursor.isNull(idIndex)) {
              experiment.setId(cursor.getLong(idIndex));
            }

            if (!cursor.isNull(jsonIndex)) {
              String jsonOfExperiment = cursor.getString(jsonIndex);
              ExperimentProviderUtil.copyAllPropertiesFromJsonToExperimentDAO(experimentDAO, jsonOfExperiment);
              experiment.setExperimentDAO(experimentDAO);
              experiments.add(experiment);
            }
          } catch (JsonProcessingException e) {
            e.printStackTrace();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    } catch (RuntimeException e) {
      Log.warn("Caught unexpected exception.", e);
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return experiments;
  }


  // private static HashMap<Integer, String>
  // convertDateLongsToStrings(SQLiteDatabase db,
  // String tableName,
  // String dateCol, String refCol) {
  // String[] columns = {dateCol, refCol};
  // HashMap<Integer, String> data = new HashMap<Integer, String>();
  // Cursor cursor = db.query(tableName, columns, null, null, null, null, null);
  //
  // try {
  // if (cursor != null) {
  // while (cursor.moveToNext()) {
  // Long longVal = cursor.getLong(cursor.getColumnIndex(dateCol));
  // if (longVal != null) {
  // String dateStr = TimeUtil.formatDate(longVal);
  // Integer id = cursor.getInt(cursor.getColumnIndex(refCol));
  // data.put(id, dateStr);
  // }
  // }
  // }
  // } finally {
  // cursor.close();
  // }
  // return data;
  // }

  // private static HashMap<Integer, String>
  // convertDateLongsToTzStrings(SQLiteDatabase db,
  // String tableName,
  // String dateCol, String refCol) {
  // String[] columns = {dateCol, refCol};
  // HashMap<Integer, String> data = new HashMap<Integer, String>();
  // Cursor cursor = db.query(tableName, columns, null, null, null, null, null);
  //
  // try {
  // if (cursor != null) {
  // while (cursor.moveToNext()) {
  // Long longVal = cursor.getLong(cursor.getColumnIndex(dateCol));
  // if (longVal != null) {
  // String dateStr = TimeUtil.formatDateWithZone(longVal);
  // Integer id = cursor.getInt(cursor.getColumnIndex(refCol));
  // data.put(id, dateStr);
  // }
  // }
  // }
  // } finally {
  // cursor.close();
  // }
  // return data;
  // }
  //

//  private static void createTruncatedExperimentsTable(SQLiteDatabase db) {
//    String tempTable = "tempTable";
//    db.execSQL("CREATE TABLE " + tempTable + " (" + ExperimentColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
//               + ExperimentColumns.SERVER_ID + " INTEGER," + ExperimentColumns.TITLE + " TEXT, "
//               + ExperimentColumns.VERSION + " INTEGER," + ExperimentColumns.DESCRIPTION + " TEXT, "
//               + ExperimentColumns.CREATOR + " TEXT, " + ExperimentColumns.INFORMED_CONSENT + " TEXT, "
//               + ExperimentColumns.HASH + " TEXT, " + ExperimentColumns.FIXED_DURATION + " INTEGER, "
//               + ExperimentColumns.QUESTIONS_CHANGE + " INTEGER, " + ExperimentColumns.ICON + " BLOB, "
//               + ExperimentColumns.WEB_RECOMMENDED + " INTEGER, " + ExperimentColumns.JSON + " TEXT " + ");");
//    db.execSQL("INSERT INTO " + tempTable + " (" + ExperimentColumns._ID + ", " + ExperimentColumns.SERVER_ID + ", "
//               + ExperimentColumns.TITLE + ", " + ExperimentColumns.VERSION + ", " + ExperimentColumns.DESCRIPTION
//               + ", " + ExperimentColumns.CREATOR + ", " + ExperimentColumns.INFORMED_CONSENT + ", "
//               + ExperimentColumns.HASH + ", " + ExperimentColumns.FIXED_DURATION + ", "
//               + ExperimentColumns.QUESTIONS_CHANGE + ", " + ExperimentColumns.ICON + ", "
//               + ExperimentColumns.WEB_RECOMMENDED + ", " + ExperimentColumns.JSON + ") " + "SELECT "
//               + ExperimentColumns._ID + ", " + ExperimentColumns.SERVER_ID + ", " + ExperimentColumns.TITLE + ", "
//               + ExperimentColumns.VERSION + ", " + ExperimentColumns.DESCRIPTION + ", " + ExperimentColumns.CREATOR
//               + ", " + ExperimentColumns.INFORMED_CONSENT + ", " + ExperimentColumns.HASH + ", "
//               + ExperimentColumns.FIXED_DURATION + ", " + ExperimentColumns.QUESTIONS_CHANGE + ", "
//               + ExperimentColumns.ICON + ", " + ExperimentColumns.WEB_RECOMMENDED + ", " + ExperimentColumns.JSON
//               + " FROM " + ExperimentProvider.EXPERIMENTS_TABLE_NAME + ";");
//    db.execSQL("DROP TABLE " + ExperimentProvider.EXPERIMENTS_TABLE_NAME);
//    db.execSQL("ALTER TABLE " + tempTable + " RENAME TO " + ExperimentProvider.EXPERIMENTS_TABLE_NAME);
//  }

//  private static void insertNewDateColumnWithData(SQLiteDatabase db, String tableName, HashMap<Integer, String> data,
//                                                  String dateCol, String refCol) {
//    db.execSQL("ALTER TABLE " + tableName + " ADD " + dateCol + " TEXT " + ";");
//    for (Map.Entry<Integer, String> entry : data.entrySet()) {
//      db.execSQL("UPDATE " + tableName + " SET " + dateCol + " = " + "\'" + entry.getValue() + "\'" + " WHERE "
//                 + refCol + " = " + entry.getKey() + ";");
//    }
//  }
  
  public Cursor query(int tableIndicator, String[] projection, String selection,
		  String[] selectionArgs, String sortOrder, String groupBy, String having, String limit) {
    SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
    Cursor resultSet = null;
    switch (tableIndicator) {
	    case ExperimentProvider.OUTPUTS_DATATYPE:
	      qb.setTables(ExperimentProvider.EVENTS_TABLE_NAME+ " INNER JOIN " + ExperimentProvider.OUTPUTS_TABLE_NAME + 
					" ON " + (ExperimentProvider.EVENTS_TABLE_NAME+ "." +EventColumns._ID) + " = " + OutputColumns.EVENT_ID);
	      break;
	    case ExperimentProvider.EVENTS_DATATYPE:
	      qb.setTables(ExperimentProvider.EVENTS_TABLE_NAME);
	      break;
	    default:
	      throw new IllegalArgumentException(ErrorMessages.UNKNOWN_TABLE_INDICATOR.getDescription() + tableIndicator);
	  }
    try{
      // While validating the columns, with their corresponding data types, JSQL parser considers a value as
      // string when enclosed in single quotes. So, in the input we send with single quotes.
      // But when we send it to the following query method which takes a string array, it considers 
      // the single quote as part of the string. So, we need to remove it explicitly.  
      String selectionArgsWithoutQuotes[] = new String[selectionArgs.length];
      for(int i=0; i<selectionArgs.length;i++){
        String temp = selectionArgs[i];
        selectionArgsWithoutQuotes[i] = temp;
        if(temp.startsWith("'")){
          selectionArgsWithoutQuotes[i] = temp.substring(1,temp.length()-1);
        } 
      }
      
      resultSet = qb.query(getReadableDatabase(), projection, selection, selectionArgsWithoutQuotes, groupBy, having,
	      sortOrder, limit);
    }catch (SQLiteException s){
      Log.warn(ErrorMessages.SQL_EXCEPTION.getDescription(), s);
      //Client should receive the exception
      throw s;
    }
    return resultSet;
  }
}
