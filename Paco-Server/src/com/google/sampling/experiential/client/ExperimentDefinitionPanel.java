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
import java.util.List;

import com.google.common.base.Splitter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
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
  private ExperimentMetadataPanel metadataPanel;
  private SignalMechanismChooserPanel signalPanel;
  private InputsListPanel inputsListPanel;
  private ExperimentCreationContentPanel contentPanel;
  private ExperimentCreationPublishingPanel publishingPanel;

  Widget showingPanel;

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

    leftMenuBar = new ExperimentCreationMenuBar(this);
    mainPanel.add(leftMenuBar);

    contentPanel = new ExperimentCreationContentPanel(experiment, this);
    mainPanel.add(contentPanel);

    metadataPanel = new ExperimentMetadataPanel(experiment, loginInfo, listener);
    contentPanel.changeShowingView(metadataPanel);
    showingPanel = metadataPanel;

    signalPanel = createSignalMechanismPanel(experiment);

    inputsListPanel = createInputsListPanel(experiment);
    
    publishingPanel = new ExperimentCreationPublishingPanel(experiment);
    
    createButtonPanel(experiment);
  }

  private void showExperimentDescription() {
    contentPanel.changeShowingView(metadataPanel);
    showingPanel = metadataPanel;
  }

  private void showExperimentSchedule() {
    contentPanel.changeShowingView(signalPanel);
    showingPanel = signalPanel;
  }

  private void showExperimentInputs() {
    contentPanel.changeShowingView(inputsListPanel);
    showingPanel = inputsListPanel;
  }

  private void showExperimentPublishing() {
    contentPanel.changeShowingView(publishingPanel);
    showingPanel = publishingPanel;
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

  private SignalMechanismChooserPanel createSignalMechanismPanel(ExperimentDAO experiment2) {
    return new SignalMechanismChooserPanel(experiment);
  }

 

  private InputsListPanel createInputsListPanel(ExperimentDAO experiment) {
    InputsListPanel inputsListPanel = new InputsListPanel(experiment);
    inputsListPanel.setStyleName("left");
    return inputsListPanel;
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

  private PanelPair createFormLine(String key, String value) {
    return createFormLine(key, value, null);
  }

  private PanelPair createFormLine(String key, String value, String styleName) {
    VerticalPanel line = new VerticalPanel();
    line.setStyleName("left");
    Label keyLabel = new Label(key + ": ");
    keyLabel.setStyleName(styleName == null ? "keyLabel" : styleName);
    TextBox valueBox = new TextBox();
    if (value != null) {
      valueBox.setText(value);
    }
    valueBox.setEnabled(true);
    line.add(keyLabel);
    line.add(valueBox);
    return new PanelPair(line, valueBox);
  }

  private PanelPair createFormArea(String key, String value, int width, String height) {
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
    valueBox.addValueChangeHandler(new ValueChangeHandler<String>() {

      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        if (valueBox.getText().length() >= 500) {
          // TODO surface a message that their text is being truncated.
          valueBox.setText(valueBox.getText().substring(0, 499));
        }

      }
    });
    valueBox.setEnabled(true);
    line.add(keyLabel);
    line.add(valueBox);
    return new PanelPair(line, valueBox);
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
    if (showingPanel.equals(metadataPanel)) {
      showExperimentSchedule();
      leftMenuBar.setSelectedItem(ExperimentCreationMenuBar.SCHEDULE_PANEL);
    } else if (showingPanel.equals(signalPanel)) {
      showExperimentInputs();
      leftMenuBar.setSelectedItem(ExperimentCreationMenuBar.INPUTS_PANEL);
    } else if (showingPanel.equals(inputsListPanel)) {
      showExperimentPublishing();
      leftMenuBar.setSelectedItem(ExperimentCreationMenuBar.PUBLISHING_PANEL);
    } else {
      showExperimentDescription();  
      leftMenuBar.setSelectedItem(ExperimentCreationMenuBar.DESCRIPTION_PANEL);
    }
  }

  private Label createLabel(String title) {
    Label responseTypeLabel = new Label(title);
    responseTypeLabel.setStyleName("keyLabel");
    return responseTypeLabel;
  }

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
    if (experiment.getCreator() == null) {
      experiment.setCreator(loginInfo.getEmailAddress());
    }
  }

  private void setDescriptionOn(ExperimentDAO experiment) {
    experiment.setDescription(metadataPanel.getExperimentDescription());
  }

  private void setTitleOn(ExperimentDAO experiment) {
    experiment.setTitle(metadataPanel.getExperimentTitle());
  }

  // private void setQuestionsChangeOn(ExperimentDAO experiment) {
  // experiment.setQuestionsChange(
  // ((BooleanValueHolder) fieldToWidgetMap.get("questionsChange")).getValue());
  // }

  private void setInformedConsentOn(ExperimentDAO experiment) {
    experiment.setInformedConsentForm(metadataPanel.getExperimentInformedConsent());
  }

  private void setModifyDateOn(ExperimentDAO experiment) {
    if (experiment.getModifyDate() == null) {
      experiment.setModifyDate(formatDateAsString(new Date()));
    }
  }

  private String formatDateAsString(Date date) {
    DateTimeFormat formatter = DateTimeFormat.getFormat(DATE_FORMAT); // PRIYA -
    return formatter.format(date);
  }

  private void setDurationOn(ExperimentDAO experiment) {
    experiment.setFixedDuration(metadataPanel.getExperimentIsFixedDuration());
    if (experiment.getFixedDuration()) {
      experiment.setStartDate(metadataPanel.getExperimentStartDate());
      experiment.setEndDate(metadataPanel.getExperimentEndDate());
    } else {
      experiment.setStartDate(null);
      experiment.setEndDate(null);
    }
  }

  private void setAdminsOn(ExperimentDAO experiment) {
    List<String> admins = new ArrayList<String>();
    String adminsText = metadataPanel.getExperimentAdminsList();
    if (adminsText.length() == 0) {
      admins.add(loginInfo.getEmailAddress());
    } else {
      Splitter sp = Splitter.on(",").trimResults().omitEmptyStrings();
      for (String admin : sp.split(adminsText)) {
        admins.add(admin);
      }
    }
    String[] adminStrArray = new String[admins.size()];
    adminStrArray = admins.toArray(adminStrArray);
    experiment.setAdmins(adminStrArray);
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
