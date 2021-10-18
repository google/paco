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
package com.pacoapp.paco.ui;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.pacoapp.paco.R;
import com.pacoapp.paco.model.Event;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.model.Output;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.Input2;
import com.pacoapp.paco.shared.util.ExperimentHelper;
import com.pacoapp.paco.utils.IntentExtraHelper;

public class RawDataActivity extends AppCompatActivity implements ExperimentLoadingActivity {

  DateTimeFormatter df = DateTimeFormat.forPattern("MM/dd/yy HH:mm");

  private ExperimentProviderUtil experimentProviderUtil;
  private Experiment experiment;

  private ExperimentGroup experimentGroup;

  private ListView list;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    experimentProviderUtil = new ExperimentProviderUtil(this);
    IntentExtraHelper.loadExperimentInfoFromIntent(this, getIntent(), experimentProviderUtil);
    if (experiment == null || experimentGroup == null) {
      displayNoExperimentMessage();
    } else {
      setContentView(R.layout.event_list);

      ActionBar actionBar = getSupportActionBar();
      actionBar.setLogo(R.drawable.ic_launcher);
      actionBar.setDisplayUseLogoEnabled(true);
      actionBar.setDisplayShowHomeEnabled(true);
      actionBar.setBackgroundDrawable(new ColorDrawable(0xff4A53B3));
      actionBar.setDisplayHomeAsUpEnabled(true);

      experimentProviderUtil.loadEventsForExperimentGroup(experiment, experimentGroup);
      fillData();
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == android.R.id.home) {
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void displayNoExperimentMessage() {
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
          Input2 input = ExperimentHelper.getInputWithName(experiment.getExperimentDAO(), output.getName(), null);
          if (input != null && input.getResponseType() != null &&
              (input.getResponseType().equals(Input2.PHOTO) ||
                  input.getResponseType().equals(Input2.SOUND))) {
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
      list = (ListView) findViewById(R.id.eventList);
      list.setAdapter(new ArrayAdapter(this, R.layout.schedule_row, nameAndTime));
    }

    @Override
    public void setExperiment(Experiment experimentByServerId) {
      this.experiment = experimentByServerId;

    }

    @Override
    public Experiment getExperiment() {
      return experiment;
    }

    @Override
    public void setExperimentGroup(ExperimentGroup groupByName) {
      this.experimentGroup = groupByName;

    }

}
