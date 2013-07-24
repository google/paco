package com.google.android.apps.paco;

import com.google.android.apps.paco.DatabaseHelper;
import com.google.android.apps.paco.ExperimentColumns;
import com.google.android.apps.paco.ExperimentProvider;
import com.google.android.apps.paco.TimeUtil;
import com.google.common.base.Splitter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.test.AndroidTestCase;

public class DatabaseHelperTest extends AndroidTestCase {
  
  private static String v13DB_OLD = "v13db-old.db";
  private static String v13DB_TO_MIGRATE = "v13db-to-migrate.db";
  
  public void testMigration13To14() throws Exception {
    
    DatabaseHelper13Mock oldDbHelper = new DatabaseHelper13Mock(getContext(), v13DB_OLD, 13);
    SQLiteDatabase oldDb = oldDbHelper.getWritableDatabase();
    
    DatabaseHelper13Mock tempDbHelper = new DatabaseHelper13Mock(getContext(), v13DB_TO_MIGRATE, 13);
    SQLiteDatabase tempDb = tempDbHelper.getWritableDatabase();
    tempDb.setVersion(13);
    
    DatabaseHelper newDbHelper = new DatabaseHelper(getContext(), v13DB_TO_MIGRATE, 14);
    SQLiteDatabase newDb = newDbHelper.getWritableDatabase();
    
    String[] columns = {ExperimentColumns._ID, ExperimentColumns.FIXED_DURATION, 
                        ExperimentColumns.START_DATE, ExperimentColumns.END_DATE,
                        ExperimentColumns.JOIN_DATE};
    
    Cursor oldCursor = oldDb.query(ExperimentProvider.EXPERIMENTS_TABLE_NAME, columns, null, null, null, null, null);
    Cursor newCursor = newDb.query(ExperimentProvider.EXPERIMENTS_TABLE_NAME, columns, null, null, null, null, null);
    
    assertNotNull(oldCursor);
    assertNotNull(newCursor);
    assertEquals(oldCursor.getCount(), newCursor.getCount());
    
    try {  
      checkDateEquality(oldCursor, newCursor);
    } finally {
      oldCursor.close();
      newCursor.close();
      oldDb.close();
      newDb.close();
      getContext().deleteDatabase(v13DB_OLD);
      getContext().deleteDatabase(v13DB_TO_MIGRATE);
    }
  }
  
  
  private void checkDateEquality(Cursor oldCursor, Cursor newCursor) {
    while (oldCursor.moveToNext() && newCursor.moveToNext()) {
      if (isFixedDuration(oldCursor)) {
        checkStartDate(oldCursor, newCursor);
        checkEndDate(oldCursor, newCursor);
        checkJoinDate(oldCursor, newCursor);
      } 
    }
  }
  
  private boolean isFixedDuration(Cursor cursor) {
    return ( cursor.getLong(cursor.getColumnIndex(ExperimentColumns.FIXED_DURATION)) == 1 );
  }
  
  private void checkStartDate(Cursor oldCursor, Cursor newCursor) {
    checkDate(oldCursor, newCursor, ExperimentColumns.START_DATE);
  }
  
  private void checkEndDate(Cursor oldCursor, Cursor newCursor) {
    checkDate(oldCursor, newCursor, ExperimentColumns.END_DATE);
  }
  
  private void checkDate(Cursor oldCursor, Cursor newCursor, String columnName) {
    String oldDate = formatDateAsStr(oldCursor.getLong(oldCursor.getColumnIndex(columnName)));
    String newDate = newCursor.getString(newCursor.getColumnIndex(columnName));
    assertEquals(oldDate, newDate);
  }
  
  private void checkJoinDate(Cursor oldCursor, Cursor newCursor) {
    String oldDate = formatDateAsStrWithZone(oldCursor.getLong(oldCursor.getColumnIndex(ExperimentColumns.JOIN_DATE)));
    String newDate = newCursor.getString(newCursor.getColumnIndex(ExperimentColumns.JOIN_DATE));
    assertEquals(oldDate, newDate);
  }
  
  private String formatDateAsStr(Long dateLong) {
    return TimeUtil.formatDate(dateLong);
  }
  
  private String formatDateAsStrWithZone(Long dateLong) {
    return TimeUtil.formatDateWithZone(dateLong);
  }
}

class DatabaseHelper13Mock extends SQLiteOpenHelper {
  
  DatabaseHelper13Mock(Context context, String dbName, int dbVersion) {
    super(context, dbName, null, dbVersion);
  }
  
  @Override
  public void onCreate(SQLiteDatabase db) {
    executeBatchSql(db);
  }
  
  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // Nothing to be done here.
  }
  
  private void executeBatchSql(SQLiteDatabase db) {
    Iterable<String> sqlCommands = Splitter.on("\n").split(DatabaseHelperTestSchemata.v13_MOCK_SCHEMA);
    for (String command : sqlCommands) {
      System.out.println(command);
      db.execSQL(command);
    }
  }
 
}


