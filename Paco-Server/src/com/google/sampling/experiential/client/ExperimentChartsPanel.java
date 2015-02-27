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
package com.google.sampling.experiential.client;

import java.util.List;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.paco.shared.model2.ExperimentDAO;
import com.google.paco.shared.model2.ExperimentGroup;
import com.google.paco.shared.model2.Input2;
import com.google.sampling.experiential.shared.EventDAO;

/**
 * Container component for holding a bunch of charts.
 *
 *
 * @author Bob Evans
 *
 */
public class ExperimentChartsPanel extends Composite {

  private ExperimentDAO experiment;
  private List<EventDAO> eventList;

  public ExperimentChartsPanel(ExperimentDAO experiment, List<EventDAO> eventList) {
    this.experiment = experiment;
    ExperimentGroup firstExperimentGroup = experiment.getGroups().get(0);
    this.eventList = eventList;
    VerticalPanel verticalPanel = new VerticalPanel();
    verticalPanel.setSpacing(2);

    initWidget(verticalPanel);
    String title = experiment.getTitle() + " : " + firstExperimentGroup.getName();
    if (title == null) {
      title = "Experiment";
    }
    Label lblChartsForExperiment = new Label("Charts for " + title);
    lblChartsForExperiment.setStyleName("paco-HTML-Large");
    verticalPanel.add(lblChartsForExperiment);


    for (Input2 input : firstExperimentGroup.getInputs()) {
      ChartPanel cp = new ChartPanel(input, eventList);
      if (cp != null) {
        verticalPanel.add(cp);
      }
    }

  }

}
