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
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.app.ListActivity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.pacoapp.paco.R;

public class RawDataActivity extends ListActivity {

  DateTimeFormatter df = DateTimeFormat.forPattern("MM/dd/yy HH:mm");

  private ExperimentProviderUtil experimentProviderUtil;
  private Experiment experiment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    experimentProviderUtil = new ExperimentProviderUtil(this);
    experiment = getExperimentFromIntent();
    if (experiment == null) {
      displayNoExperimentMessage();
    } else {
      setContentView(R.layout.event_list);
      experimentProviderUtil.loadEventsForExperiment(experiment);
      fillData();
    }
  }

    private void displayNoExperimentMessage() {
  }

    private Experiment getExperimentFromIntent() {
      Uri uri = getIntent().getData();
      if (uri == null) {
        return null;
      }
      experiment = experimentProviderUtil.getExperiment(uri);
      return experiment;
    }

    private void fillData() {
      List<String> nameAndTime = new ArrayList<String>();
      for (Event event : experiment.getEvents()) {
        StringBuilder buf = new StringBuilder();
        boolean first = true;
        for (Output output : event.getResponses()) {
          if (first) {
            first = false;
          } else {
            buf.append(",");
          }
          buf.append(output.getName());
          buf.append("=");
          Input input = experiment.getInputById(output.getInputServerId());
          if (input != null && input.getResponseType() != null &&
              (input.getResponseType().equals(Input.PHOTO) ||
                  input.getResponseType().equals(Input.SOUND))) {
            buf.append("<multimedia:"+input.getResponseType()+">");
          } else {
            buf.append(output.getAnswer());
          }
        }
        DateTime responseTime = event.getResponseTime();
        String signalTime = null;
        if (responseTime == null) {
          DateTime scheduledTime = event.getScheduledTime();
          if (scheduledTime != null) {
            signalTime = scheduledTime.toString(df) + ": " + getString(R.string.missed_signal_value);
          } else {
            signalTime = getString(R.string.missed_signal_value);
          }
        } else {
          signalTime = responseTime.toString(df);
        }
        nameAndTime.add(signalTime + ": " + buf.toString());
      }
      ArrayAdapter scheduleAdapter = new ArrayAdapter(this, R.layout.schedule_row, nameAndTime);
      setListAdapter(scheduleAdapter);

    }

}
