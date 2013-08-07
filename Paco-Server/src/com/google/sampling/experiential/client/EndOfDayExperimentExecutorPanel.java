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
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.paco.shared.model.InputDAO;
import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.PacoServiceAsync;
import com.google.sampling.experiential.shared.TimeUtil;

/**
 * A composite container for rendering an end of day experiment referring to an esm experiment.
 * This is particularly for the DIN study
 * 
 * @author Bob Evans
 *
 */
public class EndOfDayExperimentExecutorPanel extends AbstractExperimentExecutorPanel {
  private static final int TIMEOUT_HOURS = 36;

  private final class EventComparator implements Comparator<EventDAO> {
    @Override
    public int compare(EventDAO o1, EventDAO o2) {
      return o2.getIdFromTimes().compareTo(o1.getIdFromTimes());
    }
  }

  private List<EventDAO> dailyEventList;
  ExperimentDAO referredExperiment;
  private Map<Date, EventDAO> eodEventList;
  private boolean anythingNeedingResponse;
  private List<EndOfDayInputGroupPanel> endOfDayPanelsList;
  
  public EndOfDayExperimentExecutorPanel(ExperimentListener experimentListener, 
                                         PacoServiceAsync mapService, 
                                         ExperimentDAO experiment, 
                                         List<EventDAO> dailyEventList, 
                                         Map<Date, EventDAO> eventList, ExperimentDAO referredExperiment) {
    super(experimentListener, experiment, mapService);
    this.endOfDayPanelsList = Lists.newArrayList();
    this.dailyEventList = dailyEventList;    
    this.eodEventList = eventList;
    Collections.sort(this.dailyEventList, new EventComparator());    
    this.referredExperiment = referredExperiment;
    createLayout();
  }

  protected void createLayout() {
    createMainPanel();
    if (dailyEventList != null && !dailyEventList.isEmpty()) {      
      createExperimentHeader();     
      renderInputItems();  
      HorizontalPanel buttonPanel = new HorizontalPanel();
      mainPanel.add(buttonPanel);
      if (!anythingNeedingResponse) {
        createNothingToRespondMessage();
      } else {
        renderSaveButton(buttonPanel);
      }
      renderCancelButton(buttonPanel);
    } else {
      super.createExperimentHeader();    
      createNothingToRespondMessage();
      HorizontalPanel buttonPanel = new HorizontalPanel();
      mainPanel.add(buttonPanel);
      renderCancelButton(buttonPanel);
    }
    
  }

  private void createNothingToRespondMessage() {
    createDescriptionPanel(myConstants.noEventsNeedingResponseAtThisTime());
  }


  protected void createExperimentHeader() {
    super.createExperimentHeader();
    createDescriptionPanel(experiment.getDescription());
  }

  private void createDescriptionPanel(String description) {
    Label descriptionLabel = new Label(description);    
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
    int eventCount = 1;
    for (EventDAO dailyEvent : this.dailyEventList) {
      if (dailyEvent.isJoinEvent() || dailyEvent.isMissedSignal()) {
        continue;
      }
      anythingNeedingResponse = true;
      if (first) {
        first = false;
        lastDateShown = dailyEvent.getIdFromTimes();
      } else {
        if (itemPanel != null) {
          itemPanel.add(new HTML("<hr/>"));
        } else {
          mainPanel.add(new HTML("<hr/>"));
        }
      }
      
      if (isToday(dailyEvent)) { 
        if (!showingToday) {
          showingToday = true;
          mainPanel.add(new HTML("<h2>" + myConstants.todaysResponses() + "</h2>"));
        }
      } else if (!isToday(dailyEvent) && showingToday) {
        showingToday = false;
          mainPanel.add(new HTML("<h3>" + myConstants.previousDaysResponses() + "</h3><hr/>"));
      }
      
      
      if (dailyEvent.getIdFromTimes().getDate() != lastDateShown.getDate()) {
        Button button = new Button("<b>" + myMessages.datedResponses(dailyEvent.getIdFromTimes()) + " </b>");
        currentDateDisclosurePanel = new DisclosurePanel(button);
        itemPanel = new VerticalPanel();
        currentDateDisclosurePanel.add(itemPanel);
        currentDateDisclosurePanel.setOpen(false);
        mainPanel.add(currentDateDisclosurePanel);
      }
      
      
      if (currentDateDisclosurePanel == null) {
        itemPanel = new VerticalPanel();
        mainPanel.add(itemPanel);
      }
      Label eventCounter = new Label(Integer.toString(eventCount++));
      eventCounter.setStyleName("paco-HTML-Large-offset-background");
      itemPanel.add(eventCounter);
      itemPanel.add(renderEventPanel(dailyEvent));
      renderInputsPanelForEvent(itemPanel, this.experiment, dailyEvent);
      lastDateShown = dailyEvent.getIdFromTimes();
    }

  }
  
  

  public boolean alreadyHasResponse(EventDAO dailyEventDAO) {
    Date responseTime = dailyEventDAO.getIdFromTimes();
    DateTimeFormat formatter = DateTimeFormat.getFormat(TimeUtil.DATETIME_FORMAT);
    String stringResponseTime = formatter.format(responseTime);
    responseTime = formatter.parse(stringResponseTime);
    
    EventDAO eodEvent = eodEventList.get(responseTime);
    return eodEvent != null && !eodEvent.isEmptyResponse();
  }


  private boolean isToday(EventDAO eventDAO) {
    String tzStr = eventDAO.getTimezone();
    Date now = createNow(tzStr);
    Date time = null;
    if (eventDAO.getScheduledTime() == null && eventDAO.getResponseTime() == null) {
      return false;
    } else {
      time = eventDAO.getIdFromTimes();
    }
    return time.getYear() == now.getYear() &&
        time.getMonth() == now.getMonth() &&
        time.getDate() == now.getDate();
  }

  private Date createNow(String tzStr) {
    Date now = new Date();
//    if (tzStr != null) {
//      TimeZone tz = TimeZone.getTimeZone(tzStr); // getTimeZone returns gmt if there is translation for the string.
//      now = Calendar.getInstance(tz).getTime();
//    } else {
//      now = Calendar.getInstance().getTime();
//    }
    return now;
  }


  protected void addOutputsToEvent(EventDAO event) {
    Map<String, String> outputs = Maps.newHashMap();
    for (EndOfDayInputGroupPanel eventInputsGroup : endOfDayPanelsList) {
      Map<String, String> eventOutputs = eventInputsGroup.getOutputs();
      if (eventOutputs.get(EndOfDayInputGroupPanel.UNANSWERED_EVENT_KEY) != null) {
        continue;
      } else {
        outputs.putAll(eventOutputs);
      }
    }
    outputs.put(EventDAO.REFERRED_EXPERIMENT_INPUT_ITEM_KEY, Long.toString(referredExperiment.getId()));
    event.setWhat(outputs);
    
  }
  
  private EventPanel renderEventPanel(EventDAO eventDAO) {
    return new EventPanel(this, eventDAO, referredExperiment.getInputs());
  }

  private void renderInputsPanelForEvent(VerticalPanel itemPanel, ExperimentDAO dailyExperiment, EventDAO dailyEvent) {
    if (alreadyHasResponse(dailyEvent)) {
      itemPanel.add(new HTML("<b style=\"background-color:#ECF2FF;\">" + myConstants.alreadyResponded() + "</b>"));
    } else if (isTimedOut(dailyEvent)) {
      itemPanel.add(new HTML("<b style=\"background-color:#ECF2FF;\">" + myConstants.expired() + "</b>"));
    } else {      
      EndOfDayInputGroupPanel endOfDayPanel = new EndOfDayInputGroupPanel(dailyEvent);
      itemPanel.add(endOfDayPanel);
      
      InputDAO[] inputs = dailyExperiment.getInputs();
      for (int i = 0; i < inputs.length; i++) {        
        EndOfDayInputExecutorPanel inputsPanel = new EndOfDayInputExecutorPanel(inputs[i], dailyEvent);
        endOfDayPanel.add(inputsPanel);
      }
      endOfDayPanelsList.add(endOfDayPanel);
    }
  }

  private boolean isTimedOut(EventDAO eventDAO) {
    boolean timedOut = false;
    Date now = createNow(eventDAO.getTimezone());
    Date eventTime = eventDAO.getIdFromTimes();
    if (now.after(eventTime)) {
      timedOut = now.getTime() - eventTime.getTime() >= 3600000 * TIMEOUT_HOURS; 
    }
    return timedOut;
  }

}
