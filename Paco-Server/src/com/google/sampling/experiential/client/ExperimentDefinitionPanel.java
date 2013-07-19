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
import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.sampling.experiential.shared.LoginInfo;

/**
 * The main panel for viewing the details of an experiment Also used as the
 * basis of creation and editing of experiments. Delegates specific parts of
 * experiment definition to sub panels. Handles communication with subpanels
 * about state of edits.
 * 
 * @author Bob Evans
 * 
 */
public class ExperimentDefinitionPanel extends Composite implements ExperimentCreationListener {

  private static String DATE_FORMAT = "yyyy/MM/dd";

  private ExperimentDAO experiment;
  private ArrayList<ExperimentListener> listeners;
  private LoginInfo loginInfo;
  protected MyConstants myConstants;
  protected MyMessages myMessages;

  private HorizontalPanel mainPanel;
  private ExperimentCreationMenuBar leftMenuBar;
  private ExperimentCreationContentPanel contentPanel;
  private ExperimentMetadataPanel descriptionPanel;
  private SignalMechanismChooserPanel signalPanel;
  private InputsListPanel inputsListPanel;
  private ExperimentCreationPublishingPanel publishingPanel;
  private Widget showingPanel;

  public ExperimentDefinitionPanel(ExperimentDAO experiment, LoginInfo loginInfo, ExperimentListener listener) {
    myConstants = GWT.create(MyConstants.class);
    myMessages = GWT.create(MyMessages.class);

    this.experiment = experiment;
    this.loginInfo = loginInfo;
    this.listeners = new ArrayList<ExperimentListener>();
    if (listener != null) {
      listeners.add(listener);
    }

    mainPanel = new HorizontalPanel();
    initWidget(mainPanel);

    leftMenuBar = createLeftMenuBar();
    mainPanel.add(leftMenuBar);

    contentPanel = createContentPanel(experiment);
    mainPanel.add(contentPanel);

    descriptionPanel = createDescriptionPanel();
    signalPanel = createSignalMechanismPanel(experiment);
    inputsListPanel = createInputsListPanel(experiment);
    publishingPanel = createPublishingPanel();
    createButtonPanel(experiment);
    
    showingPanel = descriptionPanel;
    showShowingPanel();
  }

  private ExperimentCreationContentPanel createContentPanel(ExperimentDAO experiment) {
    return new ExperimentCreationContentPanel(experiment, this);
  }
  
  private ExperimentCreationMenuBar createLeftMenuBar() {
    return new ExperimentCreationMenuBar(this);
  }
  
  private ExperimentMetadataPanel createDescriptionPanel() {
    return new ExperimentMetadataPanel(experiment, loginInfo);
  }

  private SignalMechanismChooserPanel createSignalMechanismPanel(ExperimentDAO experiment2) {
    return new SignalMechanismChooserPanel(experiment);
  }

  private InputsListPanel createInputsListPanel(ExperimentDAO experiment) {
    InputsListPanel inputsListPanel = new InputsListPanel(experiment);
    inputsListPanel.setStyleName("left");
    return inputsListPanel;
  }

  private ExperimentCreationPublishingPanel createPublishingPanel() {
    return new ExperimentCreationPublishingPanel(experiment);
  }
  
  private void showShowingPanel() {
    contentPanel.changeShowingView(showingPanel);
  }

  private void createButtonPanel(ExperimentDAO experiment) {
    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.add(createSubmitButton(experiment));
    buttonPanel.add(createCancelButton());
    mainPanel.add(buttonPanel);
    buttonPanel.setStyleName("floating-Panel");
  }

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

  private Widget createSubmitButton(final ExperimentDAO experiment) {

    Button whatButton = new Button(experiment.getId() == null ? myConstants.createExperiment()
                                                              : myConstants.updateExperiment());
    whatButton.addClickListener(new ClickListener() {

      @Override
      public void onClick(Widget sender) {
        submitEvent(experiment);
      }

    });
    return whatButton;
  }

  public static PanelPair createDisplayLine(String key, String value) {
    HorizontalPanel line = new HorizontalPanel();
    line.setStyleName("left");
    Label keyLabel = new Label(key + ": ");
    keyLabel.setStyleName("keyLabel");

    Label valueBox = new Label();
    if (value != null) {
      valueBox.setText(value);
    }
    line.add(keyLabel);
    line.add(valueBox);
    return new PanelPair(line, valueBox);
  }

  private void progressView() {
    // Change content view and the highlight on the left menu bar.
    if (showingPanel.equals(descriptionPanel)) {
      showExperimentSchedule();
      advanceLeftMenuBar(ExperimentCreationMenuBar.SCHEDULE_PANEL);
    } else if (showingPanel.equals(signalPanel)) {
      showExperimentInputs();
      advanceLeftMenuBar(ExperimentCreationMenuBar.INPUTS_PANEL);
    } else if (showingPanel.equals(inputsListPanel)) {
      showExperimentPublishing();
      advanceLeftMenuBar(ExperimentCreationMenuBar.PUBLISHING_PANEL);
    } else {
      showExperimentDescription();  
      advanceLeftMenuBar(ExperimentCreationMenuBar.DESCRIPTION_PANEL);
    }
  }

  private void advanceLeftMenuBar(int toPanel) {
    leftMenuBar.setSelectedItem(toPanel);
  }

//  private Label createLabel(String title) {
//    Label responseTypeLabel = new Label(title);
//    responseTypeLabel.setStyleName("keyLabel");
//    return responseTypeLabel;
//  }

  private void submitEvent(ExperimentDAO experiment) {
    try {
      setCreatorOn(experiment);
      setModifyDateOn(experiment);
      saveExperiment();
    } catch (Throwable t) {
      Window.alert("Throwable: " + t.getMessage());
    }
  }

  private void setCreatorOn(ExperimentDAO experiment) {
    // Ensure there is a creator. If none, the current user is
    // the creator.
    if (experiment.getCreator() == null) {
      experiment.setCreator(loginInfo.getEmailAddress());
    }
  }

  private void setModifyDateOn(ExperimentDAO experiment) {
    if (experiment.getModifyDate() == null) {
      experiment.setModifyDate(formatDateAsString(new Date()));
    }
  }

  private String formatDateAsString(Date date) {
    DateTimeFormat formatter = DateTimeFormat.getFormat(DATE_FORMAT);
    return formatter.format(date);
  }
  
  private void showExperimentDescription() {
    showingPanel = descriptionPanel;
    showShowingPanel();
  }

  private void showExperimentSchedule() {
    showingPanel = signalPanel;
    showShowingPanel();
  }

  private void showExperimentInputs() {
    showingPanel = inputsListPanel;
    showShowingPanel();
  }

  private void showExperimentPublishing() {
    showingPanel = publishingPanel;
    contentPanel.changeShowingView(publishingPanel);
  }

  protected void fireCanceled() {
    fireExperimentCode(ExperimentListener.CANCELED);
  }

  protected void saveExperiment() {
    fireExperimentCode(ExperimentListener.SAVED);
  }

  private void fireExperimentCode(int code) {
    for (ExperimentListener listener : listeners) {
      listener.eventFired(code, experiment, false, false);
    }
  }

  @Override
  public void eventFired(int creationCode, ExperimentDAO experiment, Integer inputGroupNumber) {
    switch (creationCode) { 
    case ExperimentCreationListener.SHOW_DESCRIPTION_CODE:
      showExperimentDescription();
      break;
    case ExperimentCreationListener.SHOW_INPUTS_CODE:
      showExperimentInputs();
      break;
    case ExperimentCreationListener.SHOW_SCHEDULE_CODE:
      showExperimentSchedule();
      break;
    case ExperimentCreationListener.SHOW_PUBLISHING_CODE:
      showExperimentPublishing();
      break;
    case ExperimentCreationListener.NEXT:
      progressView();
      break;
    default:
      System.err.println("Unhandled code sent to experiment creation listener.");
      break;
    }

  }
}
