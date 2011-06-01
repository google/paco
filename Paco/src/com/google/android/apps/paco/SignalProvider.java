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


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class SignalProvider extends ContentProvider {

  public static final Uri CONTENT_URI =
      Uri.parse("content://com.google.android.apps.paco.signal");
  private static final String DATABASE_NAME = "signal.db";
  private static final int DATABASE_VERSION = 3;
  private static final String TABLE_NAME = "signal";

  private DatabaseHelper dbHelper;

  @Override
  public boolean onCreate() {
    dbHelper = new DatabaseHelper(getContext());
    return true;
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    long id = ContentUris.parseId(uri);
    if (id != -1) {
      selection = Signal._ID + " = " + id;
    }
    return db.delete(TABLE_NAME, selection, selectionArgs);
  }

  @Override
  public String getType(Uri uri) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    long signalId = db.insert(TABLE_NAME, Signal.DATE, values);
    return ContentUris.withAppendedId(uri, signalId);
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
      String sortOrder) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    return db.query(TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    return db.update(TABLE_NAME, values, selection, selectionArgs);
  }

  /**
   * This class helps open, create, and upgrade the database file.
   */
  private static class DatabaseHelper extends SQLiteOpenHelper {

    DatabaseHelper(Context context) {
      super(context, DATABASE_NAME, null /* no cursor factory */, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + Signal._ID + " INTEGER PRIMARY KEY,"
          + Signal.DATE + " INTEGER, " + Signal.EXPERIMENT_ID + " INTEGER, " + Signal.TIME + " INTEGER, "
          + Signal.NOTIFICATION_CREATED + " INTEGER" + ");");
    }

    private void dropTable(SQLiteDatabase db) {
      db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      Log.w(DatabaseHelper.class.getName(), "Upgrading database from version " + oldVersion
          + " to " + newVersion + ", which will destroy all old data");
      dropTable(db);
      onCreate(db);
    }
  }
}

