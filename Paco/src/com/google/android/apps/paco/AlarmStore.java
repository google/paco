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


import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

/**
 * Non-Android-specific interface for storing and retrieving Alarms.
 * This particular implementation is the only one, and is backed by an 
 * Android ContentProvider.
 * 
 *
 */
public class AlarmStore {

  private Context context;

  public AlarmStore(Context context) {
    this.context = context;
  }
  
  public void storeSignal(long date, long experimentId, long alarmTime) {
    ContentValues values = new ContentValues();
    values.put(Signal.DATE, date);
    values.put(Signal.EXPERIMENT_ID, experimentId);
    values.put(Signal.TIME, alarmTime);
    context.getContentResolver().insert(SignalProvider.CONTENT_URI, values);
  }

  public List<DateTime> getSignals(long experimentId, long periodStart) {
    String[] args = new String[] {Long.toString(periodStart), Long.toString(experimentId)};
    Cursor cursor = context.getContentResolver().query(SignalProvider.CONTENT_URI, 
        null /* all columns */, 
        Signal.DATE + " = ? and "+ Signal.EXPERIMENT_ID + " = ?", 
        args, 
        null /* default sort */);
    try {
      List<DateTime> dateTimes = new ArrayList<DateTime>();
      while (cursor.moveToNext()) {
        dateTimes.add(new DateTime(cursor.getLong(3)));
      }
      return dateTimes;
    } finally {
      cursor.close();
    }
  }

  public void deleteAll() {
      context.getContentResolver().delete(SignalProvider.CONTENT_URI, null, null);  
  }

  public void deleteAllSignalsForSurvey(Long experimentId) {
    if (experimentId == null) {
      return;
    }
    String[] args = new String[] {Long.toString(experimentId)};
    String selection = Signal.EXPERIMENT_ID + " = ?";
    context.getContentResolver().delete(SignalProvider.CONTENT_URI, selection, args);  
  }
  
  public void deleteSignalsForPeriod(Long experimentId, Long periodStart) {
    if (experimentId == null) {
      return;
    }

    String[] selectionArgs = new String[]{Long.toString(experimentId), 
        Long.toString(periodStart)};
    context.getContentResolver().delete(SignalProvider.CONTENT_URI, Signal.EXPERIMENT_ID + " = ? AND " + Signal.DATE + " = ?", selectionArgs);
  }

  public Cursor getAllSignalsForCurrentPeriod() {
//    DateMidnight today = new DateMidnight();
//    int dow = today.getDayOfWeek();
//    if (dow != DateTimeConstants.MONDAY) {
//      today = today.minusDays(dow - 1);
//    }
//  
//    long todayMillis = today.getMillis();
//    String[] args = new String[] {Long.toString(todayMillis)};
    Cursor cursor = context.getContentResolver().query(SignalProvider.CONTENT_URI, 
        null /* all columns */, 
        null, null,
        null /* default sort */);
    return cursor;
  }
  

}
