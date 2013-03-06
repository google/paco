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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class InformedConsentActivity extends Activity {

  private Uri uri;
  private Experiment experiment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.informed_consent);
    final Intent intent = getIntent();
    uri = intent.getData();
    experiment = new ExperimentProviderUtil(this).getExperiment(uri);
    if (experiment == null) {
      Toast.makeText(this, R.string.cannot_find_the_experiment_warning, Toast.LENGTH_SHORT).show();
      finish();
    } else {
      // TextView title = (TextView)findViewById(R.id.experimentNameIc);
      // title.setText(experiment.getTitle());

      TextView ic = (TextView) findViewById(R.id.InformedConsentTextView);
      ic.setText(experiment.getInformedConsentForm());

      if (experiment.getJoinDate() == null) {
        final CheckBox icCheckbox = (CheckBox) findViewById(R.id.InformedConsentAgreementCheckBox);
        icCheckbox.setOnClickListener(new OnClickListener() {
          public void onClick(View v) {
            if (icCheckbox.isChecked()) {
              Intent intent = new Intent(InformedConsentActivity.this,
                  ExperimentScheduleActivity.class);
              intent.setAction(Intent.ACTION_EDIT);
              intent.setData(uri);
              startActivityForResult(intent, FindExperimentsActivity.JOIN_REQUEST_CODE);
            }
          }
        });
      }
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == FindExperimentsActivity.JOIN_REQUEST_CODE) {
      if (resultCode == FindExperimentsActivity.JOINED_EXPERIMENT) {
        setResult(resultCode);
        finish();
      }
    }
  }

}
