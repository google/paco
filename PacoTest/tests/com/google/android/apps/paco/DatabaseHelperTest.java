package com.google.android.apps.paco;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import junit.framework.TestCase;


public class DatabaseHelperTest extends AndroidTestCase {
  
  private static String v13DB = "v13db.db";
  private static String v13DB_TEST = "v13db-test.db";
  
  public void testMigration13To14() throws Exception {
   
    copy13db();
    
    DatabaseHelper oldDbHelper = new DatabaseHelper(getContext(), v13DB, 13);
    SQLiteDatabase oldDb = oldDbHelper.getReadableDatabase();
    
    DatabaseHelper newDbHelper = new DatabaseHelper(getContext(), v13DB_TEST, 14);
    SQLiteDatabase newDb = newDbHelper.getReadableDatabase();
    
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
      closeFiles(oldCursor, newCursor);
    }
  }

  
  private void copy13db() throws IOException {
    InputStream in = new FileInputStream(v13DB);
    OutputStream out = new FileOutputStream(v13DB_TEST);
    byte[] buf = new byte[1024];
    int len;
    while ((len = in.read(buf)) > 0) {
      out.write(buf, 0, len);
    }
    in.close();
    out.close();
  }
  
  private void checkDateEquality(Cursor oldCursor, Cursor newCursor) {
    while (oldCursor.moveToNext()) {
      newCursor.moveToNext();
      if (isFixedDuration(oldCursor)) {
        checkStartDate(oldCursor, newCursor);
        checkEndDate(oldCursor, newCursor);
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
  
  private String formatDateAsStr(Long dateLong) {
    return TimeUtil.formatDate(dateLong);
  }
  
  private void closeFiles(Cursor oldCursor, Cursor newCursor) {
    newCursor.close();
    oldCursor.close();
    
    File newDbFile = new File(v13DB_TEST);
    newDbFile.delete();
  }

}
