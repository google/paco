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

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.paco.shared.model2.ExperimentDAO;
import com.google.paco.shared.model2.ExperimentDAOCore;
import com.google.sampling.experiential.shared.LoginInfo;

/**
 * The main panel for viewing the details of an experiment
 * Also used as the basis of creation and editing of experiments.
 * Delegates specific parts of experiment definition to sub panels.
 * Handles communication with subpanels about state of edits.
 *
 * @author Bob Evans
 *
 */
public class ExperimentDescriptionPanel extends Composite {

  private ExperimentDAO experiment;
  private LoginInfo loginInfo;

  private ArrayList<ExperimentListener> listeners;

  protected MyConstants myConstants;
  protected MyMessages myMessages;

  private VerticalPanel mainPanel;
  private CheckBox publishCheckBox;
  private CheckBox customFeedbackCheckBox;
  private DurationView durationPanel;
  private TextArea informedConsentPanel;
  private Label titlePanel;
  private Label creatorPanel;
  private TextArea descriptionPanel;


  public ExperimentDescriptionPanel() {
    super();
  }


  public ExperimentDescriptionPanel(ExperimentDAO experiment, LoginInfo loginInfo, ExperimentListener listener) {
    myConstants = GWT.create(MyConstants.class);
    myMessages = GWT.create(MyMessages.class);

    this.experiment = experiment;
    this.loginInfo = loginInfo;

    this.listeners = new ArrayList<ExperimentListener>();
    if (listener != null) {
      listeners.add(listener);
    }
    mainPanel = new VerticalPanel();
    initWidget(mainPanel);
    createExperimentForm();
  }

  private Widget createVersionPanel(ExperimentDAO experiment) {
    String experimentVersionStr = "1";
    if (experiment.getVersion() != null) {
      experimentVersionStr = experiment.getVersion().toString();
    }
    return createFormLine(myConstants.experimentVersion(), experimentVersionStr);
  }


  protected void fireCanceled() {
    fireExperimentCode(ExperimentListener.CANCELED);
  }

  private void fireExperimentCode(int code) {
    for (ExperimentListener listener : listeners) {
      listener.eventFired(code, experiment, true, false);
    }
  }

  private void createExperimentForm() {
    mainPanel.add(createTitlePanel(experiment));
    mainPanel.add(createCreatorPanel(experiment));
    mainPanel.add(createVersionPanel(experiment));
    mainPanel.add(createDescriptionPanel(experiment));
    mainPanel.add(createInformedConsentPanel(experiment));
    mainPanel.add(createSectionHeader(myConstants.signaling()));
    //mainPanel.add(createDurationPanel(experiment));
    //mainPanel.add(createSchedulePanel(experiment));
    //mainPanel.add(createSectionHeader(myConstants.inputs()));
    //mainPanel.add(createInputsListPanel(experiment));
    //createFeedbackEntryPanel(experiment);
    createButtonPanel(experiment);
  }


  private Widget createTitlePanel(ExperimentDAOCore experiment) {
    return createFormLine(myConstants.experimentTitle(), experiment.getTitle());
  }

  private Widget createDescriptionPanel(ExperimentDAOCore experiment) {
    return createFormArea(myConstants.experimentDescriptionNoPrompt(), experiment.getDescription(), 100, "200");
  }

  private Widget createCreatorPanel(ExperimentDAOCore experiment) {
    return createFormLine(myConstants.experimentCreator(),
        experiment.getCreator());
  }

  private Widget createInformedConsentPanel(ExperimentDAOCore experiment) {
    return createFormArea(myConstants.informedConsent(), experiment.getInformedConsentForm(), 100, "200");
  }

  private HTML createSectionHeader(String headerText) {
    HTML questionsPrompt = new HTML("<h2>" + headerText + "</h2>");
    questionsPrompt.setStyleName("keyLabel");
    return questionsPrompt;
  }

//  private Widget createDurationPanel(ExperimentDAOCore experiment) {
//    if (experiment.getFixedDuration()) {
//      // TODO: change to different date format if desired.
//      String startDateStr = experiment.getStartDate();
//      String endDateStr = experiment.getEndDate();
//      return createFormLine(myConstants.duration(), startDateStr + "- " + endDateStr);
//    } else {
//      return createFormLine(myConstants.duration(), myConstants.ongoingDuration());
//    }
//}

//  private InputsListPanel createInputsListPanel(ExperimentDAOCore experiment) {
//    InputsListPanel inputsListPanel = new InputsListPanel(experiment);
//    inputsListPanel.setStyleName("left");
//    return inputsListPanel;
//  }

  private void createButtonPanel(ExperimentDAOCore experiment) {
    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.add(createCancelButton());
    mainPanel.add(buttonPanel);
  }

//  private Widget createSchedulePanel(ExperimentDAOCore experiment) {
//    HTML spacer = new HTML("&nbsp;");
//    ActionTrigger actionTrigger = experiment.getActionTriggers().get(0);
//    if (!(actionTrigger instanceof InterruptTrigger)) {
//      return new Label(myConstants.triggeredExperimentNotScheduled());
//    }
//    Schedule schedule = ((ScheduleTrigger) actionTrigger).getSchedules().get(0);
//
//    int scheduleType = Schedule.SCHEDULE_TYPES[schedule.getScheduleType()];
//    Panel panel = (Panel) createFormLine(myConstants.signalSchedule(), Schedule.SCHEDULE_TYPES_NAMES[scheduleType]);
//    if (scheduleType == Schedule.ESM) {
//      panel.add(spacer);
//      panel.add(new Label(", "));
//      panel.add(new Label(schedule.getEsmFrequency().toString() + " / " +
//          Schedule.ESM_PERIODS_NAMES[schedule.getEsmPeriodInDays()]));
//      //panel.add(spacer);
//      //panel.add(new Label(experiment.getSchedule().getEsmStartHour() + " - " + experiment.getSchedule().getEsmEndHour()));
//    } else {
////      panel.add(spacer);
////      panel.add(new Label(", "));
////      Long[] times = experiment.getSchedule().getTimes();
////      List<String> timeStrs = Lists.newArrayList();
////      for (Long long1 : times) {
////        long hour = long1 / 1000 * 60 * 60;
////        long minute = (long1 - hour)
////        timeStrs .add(Long.toString(hour +":" + minute));
////      }
////      panel.add(new Label(Joiner.on(", ").join(timeStrs)));
//    }
//    return panel;
//  }

  private Widget createFormLine(String key, String value) {
    HorizontalPanel line = new HorizontalPanel();
    line.setStyleName("left");
    Label keyLabel = new Label(key + ":  ");
    keyLabel.setStyleName("keyLabel");
    Label valueBox = new Label();
    if (value != null) {
      valueBox.setText(value);
    }
    line.add(keyLabel);
    line.add(valueBox);
    return line;
  }

  private Widget createFormArea(String key, String value, int width, String height) {
    VerticalPanel line = new VerticalPanel();
    line.setStyleName("left");
    Label keyLabel = new Label(key + ": ");
    keyLabel.setStyleName("keyLabel");
    final TextArea valueBox = new TextArea();
    valueBox.setCharacterWidth(width);
    valueBox.setHeight(height);
    if (value != null) {
      valueBox.setText(value);
    }
    valueBox.setEnabled(false);
    line.add(keyLabel);
    line.add(valueBox);
    return line;
  }

  /**
   * @return
   */
  private Widget createCancelButton() {
    Button cancelButton = new Button(myConstants.cancel());
    cancelButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        fireCanceled();
      }
    });
    return cancelButton;
  }

}
