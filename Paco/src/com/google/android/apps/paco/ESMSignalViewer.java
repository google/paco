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
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.pacoapp.paco.R;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class ESMSignalViewer extends ListActivity {

  private DateTimeFormatter timeFormatter;
  private ExperimentProviderUtil experimentProviderUtil;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    timeFormatter = ISODateTimeFormat.dateTime();
    setContentView(R.layout.schedule_list);
    experimentProviderUtil = new ExperimentProviderUtil(this);
    fillData();
  }

  private void fillData() {
    // Get all of the schedule items from the database and create the item list
    Cursor cursor = new AlarmStore(this).getAllSignalsForCurrentPeriod();
    startManagingCursor(cursor);
//
//    String[] from = new String[] {Signal.EXPERIMENT_ID, Signal.TIME};
//    int[] to = new int[] {R.id.text1};
//
//    // Now create an array adapter and set it to display using our row
//    SimpleCursorAdapter scheduleAdapter = new SimpleCursorAdapter(this, R.layout.schedule_row, cursor, from, to);
//    scheduleAdapter.setViewBinder(new ViewBinder() {
//
//      public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
//        Long experimentId = cursor.getLong(0);
//        String experimentName = experimentProviderUtil.getExperiment(experimentId).getTitle();
//        String signalTime = new DateTime(cursor.getLong(1)).toString(timeFormatter);
//        ((TextView) view).setText(experimentName +": " + signalTime);
//        return true;
//      }
//
//    });
    
    List<String> nameAndTime = new ArrayList<String>();
    while (cursor.moveToNext()) {
      Long experimentId = cursor.getLong(2);
      String experimentName = experimentProviderUtil.getExperiment(experimentId).getTitle();
      String signalTime = new DateTime(cursor.getLong(3)).toString(timeFormatter);
      nameAndTime.add(experimentName + ": " + signalTime);
    }
    ArrayAdapter scheduleAdapter = new ArrayAdapter(this, R.layout.schedule_row, nameAndTime);
    setListAdapter(scheduleAdapter);

  }

}
