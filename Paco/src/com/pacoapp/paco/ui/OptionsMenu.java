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

import android.app.Activity;
import android.content.Intent;
import androidx.core.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.pacoapp.paco.R;
import com.pacoapp.paco.model.Experiment;

public class OptionsMenu {

  private Activity context;
  private Long experimentServerId;
  private boolean wasSignalled;

  public OptionsMenu(Activity experimentExecutor, Long experimentId, boolean wasSignalled) {
    this.context = experimentExecutor;
    this.experimentServerId = experimentId;
    this.wasSignalled = wasSignalled;
  }

  public boolean init(Menu menu) {
    menu.add(0, PAGING_ITEM, 0, R.string.edit_schedule_menu_item);
    menu.add(0, STOP_ITEM, 1, R.string.stop_experiment_menu_item);
    menu.add(0, DATA_ITEM, 2, R.string.explore_data_menu_item);
    menu.add(0, HELP_ITEM, 3, R.string.about_paco_menu_item);

    return true;
  }

  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case android.R.id.home:
      if (!wasSignalled) {
        final Intent intent = new Intent(context, MyExperimentsActivity.class);
        NavUtils.navigateUpTo(context, intent);
        return true;
      }
      return false;
      case PAGING_ITEM:
        launchScheduleDetailScreen();
        return true;
      case STOP_ITEM:
        launchStopScreen();
        return true;
      case DATA_ITEM:
        launchDataScreen();
        return true;
      case HELP_ITEM:
        launchHelp();
        return true;
      default:
        return false;
    }
  }

  private void launchDataScreen() {
    if (context instanceof ExperimentExecutor) {
      ((ExperimentExecutor)context).showFeedback();
    } else if (context instanceof ExperimentExecutorCustomRendering) {
      ((ExperimentExecutorCustomRendering)context).showFeedback();
    }
  }

  private void launchStopScreen() {
    if (context instanceof ExperimentExecutor) {
      ((ExperimentExecutor)context).stopExperiment();
    } else if (context instanceof ExperimentExecutorCustomRendering) {
      ((ExperimentExecutorCustomRendering)context).stopExperiment();
    }
   }


  private void launchScheduleDetailScreen() {
    Intent debugIntent = new Intent(context, ScheduleListActivity.class);
    debugIntent.putExtra(Experiment.EXPERIMENT_SERVER_ID_EXTRA_KEY, experimentServerId);
    context.startActivity(debugIntent);
  }

  private void launchHelp() {
    Intent startIntent = new Intent(context, WelcomeActivity.class);
    context.startActivity(startIntent);
  }

  private static final int PAGING_ITEM = 0;
  private static final int STOP_ITEM = 1;
  private static final int DATA_ITEM = 2;
  private static final int HELP_ITEM = 4;
}
