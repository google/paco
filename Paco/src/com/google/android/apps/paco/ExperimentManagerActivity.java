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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

/**
 * 
 */
public class ExperimentManagerActivity extends Activity {

  private static final int ABOUT_PACO_ITEM = 3;
  private static final int DEBUG_ITEM = 4;
  private static final int SERVER_ADDRESS_ITEM = 5;
  private static final int UPDATE_ITEM = 6;

  private static final CharSequence ABOUT_PACO_STRING = "About Paco";
  private static final CharSequence DEBUG_STRING = "Debug";
  private static final CharSequence SERVER_ADDRESS_STRING = "Server Address";
  private static final String CHECK_FOR_UPDATES = "Check Updates";

  static final int CHECK_UPDATE_REQUEST_CODE = 0;
  
  private ImageButton currentExperimentsButton;
  private ExperimentProviderUtil experimentProviderUtil;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
 // This will show the eula until the user accepts or quits the app.
    experimentProviderUtil = new ExperimentProviderUtil(this);
    Experiment experiment = getExperimentFromIntent();
    if (experiment != null) {
      Intent executorIntent = new Intent(this, ExperimentExecutor.class);
      executorIntent.putExtras(getIntent());
      executorIntent.setData(getIntent().getData());
      startActivity(executorIntent);
      finish();
    }

    Eula.showEula(this);
    //loginHelper = getLoginHelper();
    setContentView(R.layout.experiment_manager_main);
    currentExperimentsButton = (ImageButton) findViewById(R.id.CurrentExperimentsBtn);
    
    currentExperimentsButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        Intent intent = new Intent(ExperimentManagerActivity.this,
            FindExperimentsActivity.class);
        intent.setData(ExperimentColumns.JOINED_EXPERIMENTS_CONTENT_URI);
        startActivity(intent);
      }
    });
    
    ImageButton findExperimentsButton = (ImageButton) findViewById(R.id.FindExperimentsBtn);
    findExperimentsButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        startActivity(new Intent(ExperimentManagerActivity.this,
            FindExperimentsActivity.class));
      }
    });
    
    ImageButton exploreDataButton = (ImageButton) findViewById(R.id.ExploreDataBtn);
    exploreDataButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        startActivity(new Intent(ExperimentManagerActivity.this,
          ExploreDataActivity.class));
      }
    });
    
    ImageButton createExperimentsButton = (ImageButton) findViewById(R.id.CreateExperimentBtn);    
    createExperimentsButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        new AlertDialog.Builder(v.getContext()).setMessage("Since creating experiments involves a fair amount of text entry, a phone is not so well-suited to creating experiments. \n\nPlease point your browser to " + getResources().getString(R.string.server)+ " to create an experiment.").setTitle("How to Create an Experiment").setCancelable(true).setPositiveButton("OK", new Dialog.OnClickListener() {

          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
          }
          
        }).create().show();
      }     
      });

        
    ImageButton feedbackButton = (ImageButton)findViewById(R.id.FeedbackButton);
    feedbackButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        startActivity(new Intent(ExperimentManagerActivity.this,
                ContactOptionsActivity.class));
        
      }
    });
    
    ImageButton helpButton = (ImageButton)findViewById(R.id.HelpButton);
    helpButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        startActivity(new Intent(ExperimentManagerActivity.this,
                HelpActivity.class));
        
      }
    });

  }  

  private Experiment getExperimentFromIntent() {
    Uri uri = getIntent().getData();    
    if (uri == null) {
      return null;
    }
    return experimentProviderUtil.getExperiment(uri);
  }

  @Override
  protected void onResume() {
    super.onResume();
    //login(loginHelper);
    currentExperimentsButton.setEnabled(hasRegisteredExperiments());
  }

  private boolean hasRegisteredExperiments() {
    return experimentProviderUtil.hasJoinedExperiments();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(0, ABOUT_PACO_ITEM, 1, ABOUT_PACO_STRING);
    menu.add(0, DEBUG_ITEM, 1, DEBUG_STRING);
    menu.add(0, SERVER_ADDRESS_ITEM, 1, SERVER_ADDRESS_STRING);
    menu.add(0, UPDATE_ITEM, 2, CHECK_FOR_UPDATES);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case ABOUT_PACO_ITEM:      
      launchPaco();
      return true;
    case DEBUG_ITEM:
      launchDebug();
      return true;
    case SERVER_ADDRESS_ITEM:
      launchServerConfiguration();
      return true;
    case UPDATE_ITEM:
      launchUpdateCheck();
      return true;
    default:
      return false;
    }
  }

  private void launchServerConfiguration() {
    Intent startIntent = new Intent(this, ServerConfiguration.class);
    startActivity(startIntent);
  }

  private void launchDebug() {
    Intent startIntent = new Intent(this, ESMSignalViewer.class);
    startActivity(startIntent);
  }

  private void launchPaco() {
    Intent startIntent = new Intent(this, WelcomeActivity.class);
    startActivity(startIntent);
  }

  private void launchUpdateCheck() {
    Intent debugIntent = new Intent("com.google.android.apps.paco.UPDATE");
    startActivityForResult(debugIntent, CHECK_UPDATE_REQUEST_CODE);    
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == CHECK_UPDATE_REQUEST_CODE && resultCode == RESULT_OK) {
      finish();
    }
  }

  
}
