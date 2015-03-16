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

import com.google.common.base.Strings;
import com.google.paco.shared.scheduling.EsmSignalStore;

/**
 * Non-Android-specific interface for storing and retrieving geneated ESM signals.
 * This particular implementation is the only one, and is backed by an
 * Android ContentProvider.
 *
 *
 */
public class AndroidEsmSignalStore implements EsmSignalStore {

  private Context context;

  public AndroidEsmSignalStore(Context context) {
    this.context = context;
  }

  /* (non-Javadoc)
   * @see com.google.android.apps.paco.AlarmStoreInterface#storeSignal(long, long, long)
   */
  @Override
  public void storeSignal(Long date, Long experimentId, Long alarmTime,
                String groupName, Long actionTriggerId, Long scheduleId) {
    assertAllSelectionParams(experimentId, date, groupName, actionTriggerId, scheduleId);

    ContentValues values = new ContentValues();
    values.put(Signal.DATE, date);
    values.put(Signal.EXPERIMENT_ID, experimentId);
    values.put(Signal.TIME, alarmTime);
    values.put(Signal.GROUP_NAME, groupName);
    values.put(Signal.ACTION_TRIGGER_ID, actionTriggerId);
    values.put(Signal.SCHEDULE_ID, scheduleId);

    context.getContentResolver().insert(SignalProvider.CONTENT_URI, values);
  }

  /* (non-Javadoc)
   * @see com.google.android.apps.paco.AlarmStoreInterface#getSignals(long, long)
   */
  @Override
  public List<DateTime> getSignals(Long experimentId, Long periodStart,
               String groupName, Long actionTriggerId, Long scheduleId) {
    assertAllSelectionParams(experimentId, periodStart, groupName, actionTriggerId, scheduleId);
    String[] args = getPerScheduleSelectionArgs(experimentId, periodStart, groupName, actionTriggerId, scheduleId);
    Cursor cursor = context.getContentResolver().query(SignalProvider.CONTENT_URI,
        null /* all columns */,
        getPerScheduleSelectionClause(),
        args,
        null /* default sort */);
    int timeIndex = cursor.getColumnIndex(Signal.TIME);
    try {
      List<DateTime> dateTimes = new ArrayList<DateTime>();
      while (cursor.moveToNext()) {
        dateTimes.add(new DateTime(cursor.getLong(timeIndex)));
      }
      return dateTimes;
    } finally {
      cursor.close();
    }
  }

  private void assertAllSelectionParams(Long experimentId, Long periodStart, String groupName, Long actionTriggerId,
                                        Long scheduleId) {
    if (experimentId == null ||
            periodStart == null ||
            Strings.isNullOrEmpty(groupName) ||
            actionTriggerId == null ||
            scheduleId == null) {
      throw new IllegalArgumentException("Invalid selection parameters for AlarmStore");
    }

  }

  /* (non-Javadoc)
   * @see com.google.android.apps.paco.AlarmStoreInterface#deleteAll()
   */
  @Override
  public void deleteAll() {
      context.getContentResolver().delete(SignalProvider.CONTENT_URI, null, null);
  }

  /* (non-Javadoc)
   * @see com.google.android.apps.paco.AlarmStoreInterface#deleteAllSignalsForSurvey(java.lang.Long)
   */
  @Override
  public void deleteAllSignalsForSurvey(Long experimentId) {
    if (experimentId == null) {
      return;
    }
    String[] args = new String[] {Long.toString(experimentId)};
    String selection = Signal.EXPERIMENT_ID + " = ?";
    context.getContentResolver().delete(SignalProvider.CONTENT_URI, selection, args);
  }

  /* (non-Javadoc)
   * @see com.google.android.apps.paco.AlarmStoreInterface#deleteSignalsForPeriod(java.lang.Long, java.lang.Long)
   */
  @Override
  public void deleteSignalsForPeriod(Long experimentId,
                                     Long periodStart,
                                     String groupName, Long actionTriggerId, Long scheduleId) {
    if (experimentId == null) {
      return;
    }

    String[] selectionArgs = getPerScheduleSelectionArgs(experimentId, periodStart, groupName, actionTriggerId, scheduleId);
    String selectionClause = getPerScheduleSelectionClause();
    context.getContentResolver().delete(SignalProvider.CONTENT_URI,
                                        selectionClause,
                                      selectionArgs);
  }

  public String getPerScheduleSelectionClause() {
    return Signal.DATE + " = ? and "
            + Signal.EXPERIMENT_ID + " = ? and "
            + Signal.GROUP_NAME + " = ? and "
            + Signal.ACTION_TRIGGER_ID + " = ? and "
            + Signal.SCHEDULE_ID + " = ?";
  }

  public String[] getPerScheduleSelectionArgs(Long experimentId, Long periodStart, String groupName,
                                              Long actionTriggerId, Long scheduleId) {
    return new String[] {Long.toString(periodStart), Long.toString(experimentId), groupName,
                                  Long.toString(actionTriggerId), Long.toString(scheduleId)};
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
