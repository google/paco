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


import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DisclosurePanel;
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
    Collections.sort(this.eventList, new Comparator<EventDAO>() {

      @Override
      public int compare(EventDAO o1, EventDAO o2) {
        return o2.getIdFromTimes().compareTo(o1.getIdFromTimes());
      }
    });
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
    boolean first = true;

    boolean showingToday = false;
    Date lastDateShown = null;
    DisclosurePanel currentDateDisclosurePanel = null;
    VerticalPanel itemPanel = null;

    for (EventDAO eventDAO : this.eventList) {
      if (eventDAO.isJoinEvent() || eventDAO.isMissedSignal()) {
        continue;
      }
      
      if (first) {
        first = false;
        lastDateShown = eventDAO.getIdFromTimes();
      } else {
        if (itemPanel != null) {
          itemPanel.add(new HTML("<hr/>"));
        } else {
          mainPanel.add(new HTML("<hr/>"));
        }
      }
      
      if (isToday(eventDAO)) { 
        if (!showingToday) {
          showingToday = true;
          mainPanel.add(new HTML("<h1>TODAY'S EVENTS</h1>"));
        }
      } else if (!isToday(eventDAO) && showingToday) {
        showingToday = false;
        mainPanel.add(new HTML("<h2>PREVIOUS DAYS' EVENTS</h2><p>Note: Previous Answers will not be displayed but they are recorded</p>"));
      }
      
      
      if (eventDAO.getIdFromTimes().getDate() != lastDateShown.getDate()) {
        Button button = new Button("<h3>"+DateTimeFormat.getFullDateFormat().format(eventDAO.getIdFromTimes())+" EVENTS</h3>");
        currentDateDisclosurePanel = new DisclosurePanel(button);
        itemPanel = new VerticalPanel();
        currentDateDisclosurePanel.add(itemPanel);
        mainPanel.add(currentDateDisclosurePanel);
      }
      
      
      if (currentDateDisclosurePanel == null) {
        itemPanel = new VerticalPanel();
        mainPanel.add(itemPanel);
      }
      itemPanel.add(renderEventPanel(eventDAO));
      renderInputsPanelForEvent(itemPanel, this.experiment, eventDAO);
      lastDateShown = eventDAO.getIdFromTimes();
    }
  }

  private boolean isToday(EventDAO eventDAO) {
    Date now = new Date();
    Date time = null;
    if (eventDAO.getScheduledTime() == null && eventDAO.getResponseTime() == null) {
      return false;
    } else if (eventDAO.getScheduledTime() != null) {
      time = eventDAO.getScheduledTime();
    } else {
      time = eventDAO.getResponseTime();
    }
    return time.getYear() == now.getYear() &&
        time.getMonth() == now.getMonth() &&
        time.getDate() == now.getDate();
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
