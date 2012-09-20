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
// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.client;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.ExperimentDAO;
import com.google.sampling.experiential.shared.InputDAO;
import com.google.sampling.experiential.shared.MapServiceAsync;

/**
 * A composite container for rendering an end of day experiment referring to an esm experiment.
 * This is particularly for the DIN study
 * 
 * @author Bob Evans
 *
 */
public class EndOfDayExperimentExecutorPanel extends AbstractExperimentExecutorPanel {
  private List<EventDAO> eventList;
  ExperimentDAO referredExperiment;
  
  public EndOfDayExperimentExecutorPanel(ExperimentListener experimentListener, 
                                         MapServiceAsync mapService, 
                                         ExperimentDAO experiment, 
                                         List<EventDAO> eventList, 
                                         ExperimentDAO referredExperiment) {
    super(experimentListener, experiment, mapService);
    this.eventList = eventList;
    this.referredExperiment = referredExperiment;
    createLayout();
  }


  protected void createExperimentHeader() {
    super.createExperimentHeader();
    Label descriptionLabel = new Label(experiment.getDescription());    
    mainPanel.add(descriptionLabel);
    mainPanel.add(new HTML("<br/>"));
  }

  @Override
  protected void renderInputItems() {
    for (EventDAO eventDAO : this.eventList) {
      if (eventDAO.isJoinEvent()) {
        continue;
      }
      VerticalPanel itemPanel = new VerticalPanel();
      mainPanel.add(itemPanel);
      itemPanel.add(renderEventPanel(eventDAO));
      renderInputsPanelForEvent(itemPanel, this.experiment, eventDAO);
      mainPanel.add(new HTML("<br/>"));
    }
  }

  protected void addOutputsToEvent(EventDAO event) {
    super.addOutputsToEvent(event);
    Map<String, String> outputs = event.getWhat();
    outputs.put("referred_experiment", Long.toString(referredExperiment.getId()));
  }
  
  private EventPanel renderEventPanel(EventDAO eventDAO) {
    InputDAO[] inputs = referredExperiment.getInputs();
    Map<String, InputDAO> inputsByName = new HashMap<String, InputDAO>();
    for (InputDAO input : inputs) {
      inputsByName.put(input.getName(), input);
    }
    return new EventPanel(this, eventDAO, inputsByName);
  }

  private void renderInputsPanelForEvent(VerticalPanel itemPanel, ExperimentDAO experiment, EventDAO eventDAO) {
    InputDAO[] inputs = experiment.getInputs();
    for (int i = 0; i < inputs.length; i++) {
      EndOfDayInputExecutorPanel inputsPanel = new EndOfDayInputExecutorPanel(inputs[i], eventDAO);
      itemPanel.add(inputsPanel);
      inputsPanelsList.add(inputsPanel);
    }
  }

}
