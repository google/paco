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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.paco.shared.model.SignalGroupDAO;
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
public class ExperimentCreationPanel extends Composite implements ExperimentCreationListener {

  /* Note: a valid email address, by our definition, contains:
   *  A user name at least one character long. Valid characters are alphanumeric
   *    characters (A-Z, a-z, 0-9), underscore (_), dash (-), plus (+),
   *    and period (.). The user name cannot start with a period or a plus,
   *    and there cannot be two periods in a row.
   *  A domain name that follows the same restrictions as a user name, except that it
   *    cannot contain any underscore or plus characters.
   *  A top-level domain, or TLD, (e.g. com, gov, edu, co.uk)
   *    containing only alphabetic characters and periods.
   * The overall form of the email address must be username@domain.TLD
   * Please update this documentation if changing the email regex below.
   */
  public static final String EMAIL_REGEX =
      "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
      //"[A-Za-z0-9._%\\+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}";

  // Visible for testing
  protected static String DATE_FORMAT = "yyyy/MM/dd";

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
  protected ExperimentDescriptionPanel descriptionPanel;
  protected List<SignalMechanismChooserListPanel> signalPanels;
  protected List<InputsListPanel> inputsListPanels;
  protected List<FeedbackPanel> feedbackPanels;
  protected List<DurationView> durationPanels;

  protected ExperimentPublishingPanel publishingPanel;

  private int numSignalGroups;

  private List<String> errorMessagesAccumulated;

  public static String ERROR_HIGHLIGHT = "error-highlight";

  public ExperimentCreationPanel(ExperimentDAO experiment, LoginInfo loginInfo, ExperimentListener listener) {
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

    leftMenuBar = createLeftMenuBar(experiment);
    mainPanel.add(leftMenuBar);

    // Experiment validation error messages
    errorMessagesAccumulated = new ArrayList<String>();

    // The panels to be displayed in the content panel.
    descriptionPanel = createDescriptionPanel();

    numSignalGroups = 0;
    SignalGroupDAO signalGroup = new SignalGroupDAO();

    durationPanels = new ArrayList<DurationView>();
    DurationView durationPanel = createDurationPanel(signalGroup, numSignalGroups);
    durationPanels.add(durationPanel);


    signalPanels = new ArrayList<SignalMechanismChooserListPanel>();
    signalPanels.add(createSignalMechanismPanel(signalGroup, numSignalGroups));

    inputsListPanels = new ArrayList<InputsListPanel>();
    inputsListPanels.add(createInputsListPanel(signalGroup, numSignalGroups));

    feedbackPanels = new ArrayList<FeedbackPanel>();
    feedbackPanels.add(createFeedbackPanel(signalGroup, numSignalGroups));

    ++numSignalGroups;

    publishingPanel = createPublishingPanel();

    VerticalPanel viewPanel = new VerticalPanel();
    mainPanel.add(viewPanel);

    contentPanel = createContentPanel();

    createButtonPanel(viewPanel, contentPanel.getButtonPanel());

    viewPanel.add(contentPanel);

    // Entry view is description panel.
    showPanel(descriptionPanel);
    setLeftMenuBarHighlight(ExperimentCreationMenuBar.DESCRIPTION_PANEL, null);
  }

  private ExperimentCreationContentPanel createContentPanel() {
    List<Composite> panels = Arrays.asList(descriptionPanel, durationPanels.get(0), signalPanels.get(0),
                                           inputsListPanels.get(0), feedbackPanels.get(0), publishingPanel);
    return new ExperimentCreationContentPanel(this, panels);
  }

  private ExperimentCreationMenuBar createLeftMenuBar(ExperimentDAO experiment) {
    return new ExperimentCreationMenuBar(this, isNewExperiment(experiment));
  }

  private ExperimentDescriptionPanel createDescriptionPanel() {
    return new ExperimentDescriptionPanel(experiment, loginInfo, this);
  }

  // TODO (SignalGroups): Change to reflect new data model. Perhaps pass in signal group instead of experiment.
  private SignalMechanismChooserListPanel createSignalMechanismPanel(SignalGroupDAO signalGroup, int groupNum) {
    return new SignalMechanismChooserListPanel(signalGroup, groupNum, this);
  }

  // TODO (SignalGroups): Change to reflect new data model. Perhaps pass in signal group instead of experiment.
  private InputsListPanel createInputsListPanel(SignalGroupDAO signalGroup, int groupNum) {
    InputsListPanel inputsListPanel = new InputsListPanel(signalGroup, groupNum, this);
    inputsListPanel.setStyleName("left");
    return inputsListPanel;
  }

  private FeedbackPanel createFeedbackPanel(SignalGroupDAO signalGroup, int groupNum) {
    FeedbackPanel feedbackPanel = new FeedbackPanel(signalGroup, groupNum, this);
    feedbackPanel.setStyleName("left");
    return feedbackPanel;
  }


  private DurationView createDurationPanel(SignalGroupDAO signalGroup, int groupNum) {
    DurationView durationView = new DurationView(signalGroup, groupNum, this);
    durationView.setStyleName("left");
    return durationView;
  }

  private ExperimentPublishingPanel createPublishingPanel() {
    return new ExperimentPublishingPanel(experiment, this);
  }

  // Visible for testing
  protected void showPanel(Composite panel) {
    showPanel(panel, ExperimentCreationContentPanel.NO_EXTRA_BUTTON);
  }

  private void showPanel(Composite panel, Integer buttonPanelId) {
    showingPanel = panel;
    contentPanel.changeShowingView(showingPanel, buttonPanelId);
  }

  private void createButtonPanel(Panel parent, HorizontalPanel navigationButtonPanel) {
    HorizontalPanel masterButtonPanel = new HorizontalPanel();
    parent.add(masterButtonPanel);
    masterButtonPanel.setWidth("100%");

    HorizontalPanel submitButtonPanel = new HorizontalPanel();
    submitButtonPanel.add(createSubmitButton(experiment));
    submitButtonPanel.add(createCancelButton());
    submitButtonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
    masterButtonPanel.add(submitButtonPanel);

    navigationButtonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    masterButtonPanel.add(navigationButtonPanel);
  }

  // Visible for testing
  protected void progressView() {
    // Change content view and the highlight on the left menu bar.
    if (showingPanel.equals(descriptionPanel)) {
      showExperimentDuration(0); // Show first group's schedule.
    } else if (showingPanel instanceof DurationView) {
      int groupNum = durationPanels.indexOf(showingPanel);
      showExperimentSchedule(groupNum);
    } else if (showingPanel instanceof SignalMechanismChooserListPanel) {
      int groupNum = signalPanels.indexOf(showingPanel);
      showExperimentInputs(groupNum);
    } else if (showingPanel instanceof InputsListPanel) {
      int groupNum = inputsListPanels.indexOf(showingPanel);
      showExperimentFeedback(groupNum);
    } else if (showingPanel instanceof FeedbackPanel) {
      int groupNum = feedbackPanels.indexOf(showingPanel);
      if (isLastGroup(groupNum)) {
        showExperimentPublishing();
      } else {
        showExperimentDuration(groupNum + 1);
      }
    } else {
      showExperimentDescription();
    }
  }

  // Visible for testing
  protected void regressView() {
    // Change content view and the highlight on the left menu bar.
    if (showingPanel.equals(descriptionPanel)) {
      showExperimentPublishing();
    } else if (showingPanel instanceof DurationView) {
      int groupNum = durationPanels.indexOf(showingPanel);
      if (isFirstGroup(groupNum)) {
        showExperimentDescription();
      } else {
        showExperimentFeedback(groupNum - 1); // Show previous group's inputs
      }
    } else if (showingPanel instanceof SignalMechanismChooserListPanel) {
      int groupNum = signalPanels.indexOf(showingPanel);
      showExperimentDuration(groupNum);
    } else if (showingPanel instanceof InputsListPanel) {
      int groupNum = inputsListPanels.indexOf(showingPanel);
      showExperimentSchedule(groupNum);
    } else if (showingPanel instanceof FeedbackPanel) {
      int groupNum = feedbackPanels.indexOf(showingPanel);
      showExperimentInputs(groupNum);
    }  else {
      showExperimentFeedback(numSignalGroups - 1);
    }
  }

  private void showExperimentDescription() {
    showExperimentDescriptionPanel();
    setLeftMenuBarHighlight(ExperimentCreationMenuBar.DESCRIPTION_PANEL, null);
  }

  private void showExperimentDuration(int groupNum) {
    showExperimentDurationPanel(groupNum);
    setLeftMenuBarHighlight(ExperimentCreationMenuBar.DURATION_PANEL, groupNum);
  }

  private void showExperimentSchedule(int groupNum) {
    showExperimentSchedulePanel(groupNum);
    setLeftMenuBarHighlight(ExperimentCreationMenuBar.SCHEDULE_PANEL, groupNum);
  }

  private void showExperimentInputs(int groupNum) {
    showExperimentInputsPanel(groupNum);
    setLeftMenuBarHighlight(ExperimentCreationMenuBar.INPUTS_PANEL, groupNum);
  }

  private void showExperimentFeedback(int groupNum) {
    showExperimentFeedbackPanel(groupNum);
    setLeftMenuBarHighlight(ExperimentCreationMenuBar.FEEDBACK_PANEL, groupNum);
  }


  private void showExperimentPublishing() {
    showExperimentPublishingPanel();
    setLeftMenuBarHighlight(ExperimentCreationMenuBar.PUBLISHING_PANEL, null);
  }

  private boolean isLastGroup(int groupNum) {
    return groupNum == numSignalGroups - 1;
  }

  private boolean isFirstGroup(int groupNum) {
    return groupNum == 0;
  }

  private void showExperimentDescriptionPanel() {
    showPanel(descriptionPanel);
  }

  private void showExperimentDurationPanel(int signalGroupNum) {
    showPanel(durationPanels.get(signalGroupNum));
  }

  private void showExperimentSchedulePanel(int signalGroupNum) {
    showPanel(signalPanels.get(signalGroupNum));
  }

  private void showExperimentInputsPanel(int signalGroupNum) {
    showPanel(inputsListPanels.get(signalGroupNum));
  }

  private void showExperimentFeedbackPanel(int signalGroupNum) {
    if (signalGroupNum == numSignalGroups - 1) {
      showPanel(feedbackPanels.get(signalGroupNum), ExperimentCreationContentPanel.ADD_SIGNAL_GROUP_BUTTON);
    } else {
      showPanel(feedbackPanels.get(signalGroupNum));
    }
  }


  private void showExperimentPublishingPanel() {
    showPanel(publishingPanel, ExperimentCreationContentPanel.ADD_CREATE_EXPERIMENT_BUTTON);
  }

  private void createAndDisplayNewSignalGroup() {
    createNewSignalGroup();
    showExperimentDurationPanel(numSignalGroups - 1);
    setLeftMenuBarHighlight(ExperimentCreationMenuBar.DURATION_PANEL, numSignalGroups - 1);
  }

  // Visible for testing
  protected void createNewSignalGroup() {
    SignalGroupDAO[] signalGroups = experiment.getSignalGroups();
    SignalGroupDAO[] newSignalGroups = new SignalGroupDAO[signalGroups.length + 1];
    System.arraycopy(signalGroups, 0, newSignalGroups, 0, signalGroups.length);

    SignalGroupDAO signalGroup = new SignalGroupDAO();
    newSignalGroups[signalGroups.length] = signalGroup;

    experiment.setSignalGroups(newSignalGroups);

    DurationView durationPanel = createDurationPanel(signalGroup, numSignalGroups);
    durationPanels.add(durationPanel);
    contentPanel.addContentView(durationPanel);

    SignalMechanismChooserListPanel signalPanel = createSignalMechanismPanel(signalGroup, numSignalGroups);
    signalPanels.add(signalPanel);

    contentPanel.addContentView(signalPanel);

    InputsListPanel inputsPanel = createInputsListPanel(signalGroup, numSignalGroups);
    inputsListPanels.add(inputsPanel);
    contentPanel.addContentView(inputsPanel);

    FeedbackPanel feedbackPanel = createFeedbackPanel(signalGroup, numSignalGroups);
    feedbackPanels.add(feedbackPanel);
    contentPanel.addContentView(feedbackPanel);

    leftMenuBar.addSignalGroup();

    ++numSignalGroups;
  }


  private void setLeftMenuBarHighlight(int toPanelType, Integer groupNum) {
    leftMenuBar.setSelectedItem(toPanelType, groupNum);
  }

  // private Label createLabel(String title) {
  // Label responseTypeLabel = new Label(title);
  // responseTypeLabel.setStyleName("keyLabel");
  // return responseTypeLabel;
  // }

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

    Button whatButton = new Button(isNewExperiment(experiment) ? myConstants.createExperiment()
                                                             : myConstants.updateExperiment());
    whatButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        submitEventWithValidation();
      }

    });
    return whatButton;
  }

  private boolean isNewExperiment(final ExperimentDAO experiment) {
    return experiment.getId() == null;
  }

  private void submitEventWithValidation() {
    if (canSubmit()) {
      submitEvent();
    } else {
      Window.alert(getErrorMessages());
    }
  }

  // Visible for testing
  protected boolean canSubmit() {
    descriptionPanel.verify();
    for (InputsListPanel panel : inputsListPanels) {
      panel.verify();
    }
    return allValidationCriteriaAreMet();
  }

  private boolean allValidationCriteriaAreMet() {
    return errorMessagesAccumulated.isEmpty();
  }

  // Visible for testing
  protected InputsListPanel getInputsListPanel() {
    // TODO: update data model. This is for backwards compatibility.
    return inputsListPanels.get(0);
  }

  // Visible for testing
  protected DurationView getDurationPanel() {
    return descriptionPanel.getDurationPanel();
  }

  private void removeErrorMessage(String errorMessage, Integer signalGroupNum) {
    if (signalGroupNum != null) {
      errorMessage = prependSignalGroupToErrorMessage(errorMessage, signalGroupNum);
    }
    errorMessagesAccumulated.remove(errorMessage);
  }

  private void addErrorMessage(String errorMessage, Integer signalGroupNum) {
    if (errorMessage == null) {
      throw new IllegalArgumentException("Error message cannot be null.");
    }
    if (signalGroupNum != null) {
      errorMessage = prependSignalGroupToErrorMessage(errorMessage, signalGroupNum);
    }
    if (!errorMessagesAccumulated.contains(errorMessage)) {
      errorMessagesAccumulated.add(errorMessage);
    }
  }

  private String prependSignalGroupToErrorMessage(String errorMessage, Integer signalGroupNum) {
    return myConstants.signalGroup() + " " + (signalGroupNum +  1) + ": " + errorMessage;
  }

  private String getErrorMessages() {
    Collections.sort(errorMessagesAccumulated, String.CASE_INSENSITIVE_ORDER);
    return myConstants.experimentCreationError() + "\n\n" +
        Joiner.on("\n").join(errorMessagesAccumulated);
  }

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
    // Modify date is the creation date.
    if (experiment.getModifyDate() == null) {
      experiment.setModifyDate(formatDateAsString(new Date()));
    }
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
  public void eventFired(int creationCode, Integer signalGroupNumber, String message) {
    switch (creationCode) {
    case ExperimentCreationListener.SHOW_DESCRIPTION_CODE:
      showExperimentDescriptionPanel();
      break;
    case ExperimentCreationListener.SHOW_SCHEDULE_CODE:
      showExperimentSchedulePanel(signalGroupNumber);
      break;
    case ExperimentCreationListener.SHOW_INPUTS_CODE:
      showExperimentInputsPanel(signalGroupNumber);
      break;
    case ExperimentCreationListener.SHOW_PUBLISHING_CODE:
      showExperimentPublishingPanel();
      break;
    case ExperimentCreationListener.NEXT:
      progressView();
      break;
    case ExperimentCreationListener.PREVIOUS:
      regressView();
      break;
    case ExperimentCreationListener.NEW_SIGNAL_GROUP:
      createAndDisplayNewSignalGroup();
      break;
    case ExperimentCreationListener.SAVE_EXPERIMENT:
      submitEventWithValidation();
      break;
    case ExperimentCreationListener.REMOVE_ERROR:
      removeErrorMessage(message, signalGroupNumber);
      break;
    case ExperimentCreationListener.ADD_ERROR:
      addErrorMessage(message, signalGroupNumber);
      break;
    case ExperimentCreationListener.SHOW_DURATION_CODE:
      showExperimentDurationPanel(signalGroupNumber);
      break;
    case ExperimentCreationListener.SHOW_FEEDBACK_CODE:
      showExperimentFeedbackPanel(signalGroupNumber);
      break;
    default:
      System.err.println("Unhandled code sent to experiment creation listener.");
      break;
    }
  }

  private String formatDateAsString(Date date) {
    DateTimeFormat formatter = DateTimeFormat.getFormat(DATE_FORMAT);
    return formatter.format(date);
  }

  // Visible for testing
  protected ExperimentDAO getExperiment() {
    return experiment;
  }

  // Visible for testing
  protected ExperimentDescriptionPanel getDescriptionPanel() {
    return descriptionPanel;
  }

  // Visible for testing
  protected SignalMechanismChooserListPanel getSignalPanelForSignalGroup(int groupNum) {
    return signalPanels.get(groupNum);
  }

  // Visible for testing
  protected InputsListPanel getInputsListPanelForSignalGroup(int groupNum) {
    return inputsListPanels.get(groupNum);
  }

  // Visible for testing
  protected ExperimentPublishingPanel getPublishingPanel() {
    return publishingPanel;
  }

  // Visible for testing
  protected Composite getShowingPanel() {
    return showingPanel;
  }

  // Visible for testing
  protected void setTitleInPanel(String title) {
    descriptionPanel.setTitleInPanel(title);
  }

  // Visible for testing
  protected void setAdminsInPanel(String commaSepEmailList) {
    descriptionPanel.setAdminsInPanel(commaSepEmailList);
  }

  // Visible for testing
  protected void setPublishedUsersInPanel(String commaSepEmailList) {
    publishingPanel.setPublishedUsersInPanel(commaSepEmailList);
  }

  // Visible for testing
  protected int getNumSignalGroups() {
    return numSignalGroups;
  }

  public static void setPanelHighlight(Widget widget, boolean isValid) {
    if (isValid) {
      removeErrorHighlight(widget);
    } else {
      addErrorHighlight(widget);
    }
  }

  public static void addErrorHighlight(Widget widget) {
    widget.addStyleName(ERROR_HIGHLIGHT);
  }

  public static void removeErrorHighlight(Widget widget) {
    widget.removeStyleName(ERROR_HIGHLIGHT);
  }
}
