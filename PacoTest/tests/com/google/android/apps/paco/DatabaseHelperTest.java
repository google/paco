package com.google.android.apps.paco;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import junit.framework.TestCase;


public class DatabaseHelperTest extends AndroidTestCase {
  
  private static String v13DB = "v13db.db";
  private static String v13DB_TEST = "v13db-test.db";
  
  public void testMigration13To14() throws Exception {
   
    copy13db();   // Exception handling?
    
    DatabaseHelper oldDbHelper = new DatabaseHelper(getContext(), v13DB, 13);
    SQLiteDatabase oldDb = oldDbHelper.getReadableDatabase();
    
    DatabaseHelper dbHelper = new DatabaseHelper(getContext(), v13DB_TEST, 14);
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    
    // old cursor
    // new cursor
    // comparison
    
    
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

}
