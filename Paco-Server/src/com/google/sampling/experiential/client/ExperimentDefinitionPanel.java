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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
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
  private Composite showingPanel;
  
  // Visible for testing
  protected ExperimentMetadataPanel descriptionPanel;
  protected SignalMechanismChooserPanel signalPanel;
  protected InputsListPanel inputsListPanel;
  protected ExperimentCreationPublishingPanel publishingPanel;

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

    // The panels to be displayed in the content panel.
    descriptionPanel = createDescriptionPanel();
    signalPanel = createSignalMechanismPanel();
    inputsListPanel = createInputsListPanel();
    publishingPanel = createPublishingPanel();

    contentPanel = createContentPanel();
    mainPanel.add(contentPanel);

    createButtonPanel();
    
    // Entry view is description panel.
    showPanel(descriptionPanel);
    setLeftMenuBarHighlight(ExperimentCreationMenuBar.DESCRIPTION_PANEL);

  }

  private ExperimentCreationContentPanel createContentPanel() {
    List<Composite> panels = Arrays.asList(descriptionPanel, signalPanel, inputsListPanel, publishingPanel);
    return new ExperimentCreationContentPanel(experiment, this, panels);
  }

  private ExperimentCreationMenuBar createLeftMenuBar() {
    return new ExperimentCreationMenuBar(this);
  }

  private ExperimentMetadataPanel createDescriptionPanel() {
    return new ExperimentMetadataPanel(experiment, loginInfo);
  }

  private SignalMechanismChooserPanel createSignalMechanismPanel() {
    return new SignalMechanismChooserPanel(experiment);
  }

  private InputsListPanel createInputsListPanel() {
    InputsListPanel inputsListPanel = new InputsListPanel(experiment);
    inputsListPanel.setStyleName("left");
    return inputsListPanel;
  }

  private ExperimentCreationPublishingPanel createPublishingPanel() {
    return new ExperimentCreationPublishingPanel(experiment);
  }

  private void showPanel(Composite panel) {
    showingPanel = panel;
    contentPanel.changeShowingView(showingPanel);
  }

  private void createButtonPanel() {
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
    Button submitButton = new Button(experiment.getId() == null ? myConstants.createExperiment()
                                                               : myConstants.updateExperiment());
    submitButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        submitEvent();
      }
    });
    return submitButton;
  }

  private void progressView() {
    // Change content view and the highlight on the left menu bar.
    if (showingPanel.equals(descriptionPanel)) {
      showExperimentSchedule();
      setLeftMenuBarHighlight(ExperimentCreationMenuBar.SCHEDULE_PANEL);
    } else if (showingPanel.equals(signalPanel)) {
      showExperimentInputs();
      setLeftMenuBarHighlight(ExperimentCreationMenuBar.INPUTS_PANEL);
    } else if (showingPanel.equals(inputsListPanel)) {
      showExperimentPublishing();
      setLeftMenuBarHighlight(ExperimentCreationMenuBar.PUBLISHING_PANEL);
    } else {
      showExperimentDescription();
      setLeftMenuBarHighlight(ExperimentCreationMenuBar.DESCRIPTION_PANEL);
    }
  }

  private void showExperimentDescription() {
    showPanel(descriptionPanel);
  }

  private void showExperimentSchedule() {
    showPanel(signalPanel);
  }

  private void showExperimentInputs() {
    showPanel(inputsListPanel);
  }

  private void showExperimentPublishing() {
    showPanel(publishingPanel);
  }

  private void setLeftMenuBarHighlight(int toPanel) {
    leftMenuBar.setSelectedItem(toPanel);
  }

  // private Label createLabel(String title) {
  // Label responseTypeLabel = new Label(title);
  // responseTypeLabel.setStyleName("keyLabel");
  // return responseTypeLabel;
  // }

  // Visible for testing
  protected void submitEvent() {
    try {
      setCreatorOn(experiment);
      setModifyDateOn(experiment);
      saveExperiment();
    } catch (Throwable t) {
      Window.alert("Throwable: " + t.getMessage());
    }
  }

  private void setCreatorOn(ExperimentDAO experiment) {
    // Ensure there is a creator. If none, the current user is the creator.
    if (experiment.getCreator() == null) {
      experiment.setCreator(loginInfo.getEmailAddress());
    }
  }

  private void setModifyDateOn(ExperimentDAO experiment) {
    // Modify date = creation date.
    if (experiment.getModifyDate() == null) {
      experiment.setModifyDate(formatDateAsString(new Date()));
    }
  }

  private String formatDateAsString(Date date) {
    DateTimeFormat formatter = DateTimeFormat.getFormat(DATE_FORMAT);
    return formatter.format(date);
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
  
  // Visible for testing
  protected ExperimentDAO getExperiment() {
    return experiment;
  }
}
