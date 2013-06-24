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

import com.pacoapp.paco.R;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;

public class OptionsMenu {

  private Context context;
  private Uri experimentUri;
  private boolean wasSignalled;

  public OptionsMenu(Context experimentExecutor, Uri experimentUri, boolean wasSignalled) {
    this.context = experimentExecutor;
    this.experimentUri = experimentUri;
    this.wasSignalled = wasSignalled;
  }

  public boolean init(Menu menu) {
    menu.add(0, PAGING_ITEM, 0, R.string.edit_schedule_menu_item);
    menu.add(0, STOP_ITEM, 1, R.string.stop_experiment_menu_item);
    menu.add(0, DATA_ITEM, 2, R.string.explore_data_menu_item);
    //menu.add(0, REFRESH_EXPERIMENT_ITEM, 2, R.string.refresh_experiment_menu_item);
    if (context instanceof ExperimentManagerActivity) {
      menu.add(0, UPDATE_ITEM, 2, R.string.check_updates_menu_item);
    }
    if (wasSignalled) {
    menu.add(0, MAINPAGE_ITEM, 2, R.string.main_page_menu_item);
    }
    menu.add(0, HELP_ITEM, 3, R.string.about_paco_menu_item);

//    menu.add(0, OPT_OUT_ITEM, 2, OPT_OUT);
//    if (context instanceof Results) {
//      menu.add(0, REFRESH_ITEM, 2, REFRESH);
//    }
//    menu.add(0, UPDATE_ITEM, 2, CHECK_FOR_UPDATES);
//    menu.add(0, DEBUG_ITEM, 2, DEBUG);
    return true;
  }

  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case PAGING_ITEM:
        launchScheduleDetailScreen();
        return true;
      case STOP_ITEM:
        launchStopScreen();
        return true;
      case DATA_ITEM:
        launchDataScreen();
        return true;  
//      case REFRESH_EXPERIMENT_ITEM:
//        launchRefreshExperiments();
//        return true;
      case MAINPAGE_ITEM:
        launchMainPage();
        return true;
      case HELP_ITEM:      
        launchHelp();
        return true;
//      case DEBUG_ITEM:
//        launchDebugScreen();
//        return true;
//      case REFRESH_ITEM:
//        launchRefreshScreen();
//        return true;
//      case RESULTS_ITEM:
//        launchResultsScreen();
//        return true;
//      case UPDATE_ITEM:
//        launchUpdateCheck();
//        return true;
      default:
        return false;
    } 
  }

//  private void launchRefreshExperiments() {
//    ((ExperimentExecutor)context).refreshExperiment();
//  }

  private void launchUpdateCheck() {
    Intent debugIntent = new Intent("com.google.android.apps.paco.UPDATE");
    context.startActivity(debugIntent);
  }


  private void launchDataScreen() {
   ((ExperimentExecutor)context).showFeedback();
  }

  private void launchStopScreen() {
    ((ExperimentExecutor)context).stopExperiment();
   }

  
  private void launchScheduleDetailScreen() {
    Intent debugIntent = new Intent(context, ExperimentScheduleActivity.class);
    debugIntent.setData(experimentUri);
    context.startActivity(debugIntent);
  }

  private void launchHelp() {
    Intent startIntent = new Intent(context, WelcomeActivity.class);
    context.startActivity(startIntent);
  }

  private void launchMainPage() {
    Intent startIntent = new Intent(context, ExperimentManagerActivity.class);
    context.startActivity(startIntent);
  }

//  private void launchDebugScreen() {
//    Intent debugIntent = new Intent("com.google.sampling.experiential.android.DEBUG");
//    context.startActivity(debugIntent);
//  }
//
//  private void launchRefreshScreen() {
////    final Handler handler = new Handler();
////    Runnable runnable = new Runnable() {
////      public void run() {
////        new ResultsUpdater(context).run();
////        handler.post(new Runnable() {
////          public void run() {
////            // TODO (bobevans): Refactor this to not be so gross.
////            ((Results) context).loadCachedResults();
////          }
////
////        });
////      }
////    };
////    (new Thread(runnable)).start();
////    Toast.makeText(context, "Refreshing Data Now...", Toast.LENGTH_SHORT).show();
//
//  }

  // menu item constants
  private static final String DEBUG = "DEBUG";
  private static final String REFRESH = "Refresh Data";
  private static final String RESULTS = "Results Page";
  private static final int PAGING_ITEM = 0;
  private static final int STOP_ITEM = 1;
  private static final int DATA_ITEM = 2;
  private static final int MAINPAGE_ITEM = 3;
  private static final int HELP_ITEM = 4;
  private static final int REFRESH_EXPERIMENT_ITEM = 5;
  private static final int UPDATE_ITEM = 6;

  
//  private static final int DEBUG_ITEM = 2;
//  private static final int REFRESH_ITEM = 3;
//  private static final int RESULTS_ITEM = 4;

}
