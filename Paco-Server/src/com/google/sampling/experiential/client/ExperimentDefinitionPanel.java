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

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBoxBase;
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
  
  /* Note: a valid email address, by our definition, contains:
   *  A user name at least one character long. Valid characters are alphanumeric
   *    characters (A-Z, a-z, 0-9), underscore (_), dash (-), plus (+), 
   *    and period (.). The user name cannot start with a period or a plus,
   *    and there cannot be two periods in a row.
   *  A domain name that follows the same restrictions as a user name, except that it
   *    cannot contain any underscore or plus characters.
   *  A top-level domain, or TLD, (e.g. com, gov, edu) at least two characters long
   *    and containing only alphabetic characters.
   * The overall form of the email address must be username@domain.TLD
   * Please update this documentation if changing the email regex below.
   */
  private static String EMAIL_REGEX = 
      "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
      //"[A-Za-z0-9._%\\+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}";

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
  protected List<SignalMechanismChooserPanel> signalPanels;
  protected List<InputsListPanel> inputsListPanels;
  protected ExperimentCreationPublishingPanel publishingPanel;
  
  private int numSignalGroups;

  private List<String> errorMessagesToDisplay;

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
   
    numSignalGroups = 0;
    signalPanels = new ArrayList<SignalMechanismChooserPanel>();
    signalPanels.add(createSignalMechanismPanel(numSignalGroups));
    inputsListPanels = new ArrayList<InputsListPanel>();
    inputsListPanels.add(createInputsListPanel(numSignalGroups));
    ++numSignalGroups;
    
    publishingPanel = createPublishingPanel();

    contentPanel = createContentPanel();
    mainPanel.add(contentPanel);

    createButtonPanel();
    
    // Entry view is description panel.
    showPanel(descriptionPanel);
    setLeftMenuBarHighlight(ExperimentCreationMenuBar.DESCRIPTION_PANEL, null);
    
    // Experiment validation error messages
    errorMessagesToDisplay = new ArrayList<String>();
    errorMessagesToDisplay.add(myConstants.experimentCreationError());
  }

  private ExperimentCreationContentPanel createContentPanel() {
    List<Composite> panels = Arrays.asList(descriptionPanel, signalPanels.get(0), inputsListPanels.get(0), publishingPanel);
    return new ExperimentCreationContentPanel(experiment, this, panels);
  }

  private ExperimentCreationMenuBar createLeftMenuBar() {
    return new ExperimentCreationMenuBar(this);
  }

  private ExperimentMetadataPanel createDescriptionPanel() {
    return new ExperimentMetadataPanel(experiment, loginInfo);
  }

  private SignalMechanismChooserPanel createSignalMechanismPanel(int groupNum) {
    // TODO: Change to reflect new data model
    if (groupNum == 0) {
      return new SignalMechanismChooserPanel(experiment, groupNum);
    } else {
      return new SignalMechanismChooserPanel(new ExperimentDAO(), groupNum);
    }
  }

  private InputsListPanel createInputsListPanel(int groupNum) {
    // TODO: Change to reflect new data model
    InputsListPanel inputsListPanel;
    if (groupNum == 0) {
      inputsListPanel = new InputsListPanel(experiment, groupNum);
    } else {
      inputsListPanel = new InputsListPanel(new ExperimentDAO(), groupNum);
    }
    inputsListPanel.setStyleName("left");
    return inputsListPanel;
  }

  private ExperimentCreationPublishingPanel createPublishingPanel() {
    return new ExperimentCreationPublishingPanel(experiment);
  }

  private void showPanel(Composite panel) {
    showPanel(panel, ExperimentCreationContentPanel.NO_EXTRA_BUTTON);
  }
  
  private void showPanel(Composite panel, Integer buttonPanelId) {
    showingPanel = panel;
    contentPanel.changeShowingView(showingPanel, buttonPanelId);
  }

  private void createButtonPanel() {
    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.add(createSubmitButton(experiment));
    buttonPanel.add(createCancelButton());
    mainPanel.add(buttonPanel);
    buttonPanel.setStyleName("floating-Panel");
  }

  private void progressView() {
    // Change content view and the highlight on the left menu bar.
    if (showingPanel.equals(descriptionPanel)) {
      showExperimentSchedule(0);
      setLeftMenuBarHighlight(ExperimentCreationMenuBar.SCHEDULE_PANEL, 0);
    } else if (showingPanel instanceof SignalMechanismChooserPanel) {
      int groupNum = signalPanels.indexOf(showingPanel);
      showExperimentInputs(groupNum);
      setLeftMenuBarHighlight(ExperimentCreationMenuBar.INPUTS_PANEL, groupNum);
    } else if (showingPanel instanceof InputsListPanel) {
      int groupNum = inputsListPanels.indexOf(showingPanel);
      if (isInLastInputGroup(groupNum)) {
        showExperimentPublishing();
        setLeftMenuBarHighlight(ExperimentCreationMenuBar.PUBLISHING_PANEL, null);
      } else {
        showExperimentSchedule(groupNum);
        setLeftMenuBarHighlight(ExperimentCreationMenuBar.SCHEDULE_PANEL, groupNum);
      }
    } else {
      showExperimentDescription();
      setLeftMenuBarHighlight(ExperimentCreationMenuBar.DESCRIPTION_PANEL, null);
    }
  }
  
  private boolean isInLastInputGroup(int groupNum) {
    return groupNum == numSignalGroups - 1;
  }

  private void showExperimentDescription() {
    showPanel(descriptionPanel);
  }

  private void showExperimentSchedule(int signalGroupNum) {
    showPanel(signalPanels.get(signalGroupNum));
  }

  private void showExperimentInputs(int signalGroupNum) {
    if (signalGroupNum == numSignalGroups - 1) {
      showPanel(inputsListPanels.get(signalGroupNum), ExperimentCreationContentPanel.ADD_SIGNAL_GROUP_BUTTON);
    } else {
      showPanel(inputsListPanels.get(signalGroupNum));
    }
  }

  private void showExperimentPublishing() {
    showPanel(publishingPanel, ExperimentCreationContentPanel.ADD_CREATE_EXPERIMENT_BUTTON);
  }
  
  private void createAndDisplayNewSignalGroup() {
    createNewSignalGroup();
    showExperimentSchedule(numSignalGroups - 1);
    setLeftMenuBarHighlight(ExperimentCreationMenuBar.SCHEDULE_PANEL, numSignalGroups - 1);
  }
  
  private void createNewSignalGroup() {
    // TODO: update the data model as well
    // For now, the panel is associated with a random experiment.
    // Later, each panel will be associated with a particular signal group.
    SignalMechanismChooserPanel signalPanel = createSignalMechanismPanel(numSignalGroups);
    signalPanels.add(signalPanel);
    contentPanel.addContentView(signalPanel);
    
    InputsListPanel inputsPanel = createInputsListPanel(numSignalGroups);
    inputsListPanels.add(inputsPanel);
    contentPanel.addContentView(inputsPanel);
   
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

    Button whatButton = new Button(experiment.getId() == null ? myConstants.createExperiment()
                                                             : myConstants.updateExperiment());
    whatButton.addClickHandler(new ClickHandler() {
      
      @Override
      public void onClick(ClickEvent event) {
        if (canSubmit()) {
          submitEvent();
        } else {
          Window.alert(getErrorMessages());
        }
      }

    });
    return whatButton;
  }

  // Visible for testing
  protected boolean canSubmit() {
    List<Boolean> allRequirementsAreMet = Arrays.asList(checkRequiredFieldsAreFilledAndHighlight(),
                                                        checkVariableNamesHaveNoSpacesAndHighlight(),
                                                        startDateIsNotAfterEndDate(),
                                                        checkEmailFieldsAreValidAndHighlight());
    List<String> requirementMessages = Arrays.asList(myConstants.needToCompleteRequiredFields(),
                                                     myConstants.varNameUnfilledOrHasSpacesError(),
                                                     myConstants.startEndDateError(),
                                                     myConstants.emailAddressesError());
    removeExistingErrorMessages();
    for (int i = 0; i < allRequirementsAreMet.size(); ++i) {
      if (!allRequirementsAreMet.get(i)) {
        addErrorMessage(requirementMessages.get(i));
      }
    }
    return !allRequirementsAreMet.contains(false);
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

  private void removeExistingErrorMessages() {
    if (errorMessagesListHasMessages()) {
      errorMessagesToDisplay.subList(1, errorMessagesToDisplay.size()).clear();
    }
  }
  
  private boolean errorMessagesListHasMessages() {
    Preconditions.checkArgument(!errorMessagesToDisplay.isEmpty());
    return !(errorMessagesToDisplay.size() == 1);
  }

  private void addErrorMessage(String errorMessage) {
    errorMessagesToDisplay.add(errorMessage);
  }
  
  private String getErrorMessages() {
    return Joiner.on("\n").join(errorMessagesToDisplay);
  }

  // Required fields are: title, informed consent, and at least one valid
  // question.
  private boolean checkRequiredFieldsAreFilledAndHighlight() {
    List<Boolean> areRequiredWidgetsFilled = Arrays.asList(
                            checkTextFieldIsFilledAndHighlight(descriptionPanel.getTitleTextPanel()),
                            checkListItemsHaveAtLeastOneOptionAndHighlight());
    return !areRequiredWidgetsFilled.contains(false);
  }

  private boolean checkTextFieldIsFilledAndHighlight(TextBoxBase widget) {
    boolean isFilled = !widget.getText().isEmpty();
    setPanelHighlight(widget, isFilled);
    return isFilled;
  }
  
  private boolean checkListItemsHaveAtLeastOneOptionAndHighlight() {
    for (InputsListPanel panel : inputsListPanels) {
      if (!panel.checkListItemsHaveAtLeastOneOptionAndHighlight()) {
        return false;
      }
    }
    return true;
  }
  
  private boolean checkVariableNamesHaveNoSpacesAndHighlight() {
    for (InputsListPanel panel : inputsListPanels) {
      if (!panel.checkVarNamesFilledWithoutSpacesAndHighlight()) {
        return false;
      }
    }
    return true;
  }
  
  // Visible for testing
  protected boolean startDateIsNotAfterEndDate() {
    DurationView durationPanel = descriptionPanel.getDurationPanel();
    if (durationPanel.isFixedDuration()) {
      Date startDate = getDateFromFormattedString(durationPanel.getStartDate());
      Date endDate = getDateFromFormattedString(durationPanel.getEndDate());
      boolean startDateNotAfterEndDate = !(endDate.before(startDate));
      setPanelHighlight(durationPanel, startDateNotAfterEndDate);
      return startDateNotAfterEndDate;
    } else {
      setPanelHighlight(durationPanel, true);
      return true;
    }
  }
  
  private boolean checkEmailFieldsAreValidAndHighlight() {
    boolean adminListIsValid = checkEmailFieldIsValidAndHighlight(descriptionPanel.getAdminListPanel());
    boolean userListIsValid = checkEmailFieldIsValidAndHighlight(publishingPanel.getPublishedUserPanel());
    return adminListIsValid && userListIsValid;
  }
  
  private boolean checkEmailFieldIsValidAndHighlight(TextBoxBase widget) {
    boolean emailAddressesAreValid = emailStringIsValid(widget.getText());
    setPanelHighlight(widget, emailAddressesAreValid);
    return emailAddressesAreValid;
  }
  
  // Visible for testing
  protected boolean emailStringIsValid(String emailString) {
    Splitter sp = Splitter.on(",").trimResults().omitEmptyStrings();
    for (String email : sp.split(emailString)) {
      if (!email.matches(EMAIL_REGEX)) {
        return false;
      }
    }
    return true;
  }

  private void setPanelHighlight(Widget widget, boolean isFilled) {
    if (isFilled) {
      removeErrorHighlight(widget);
    } else {
      addErrorHighlight(widget);
    }
  }

  private void addErrorHighlight(Widget widget) {
    widget.addStyleName(Main.ERROR_HIGHLIGHT);
  }

  private void removeErrorHighlight(Widget widget) {
    widget.removeStyleName(Main.ERROR_HIGHLIGHT);
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
  public void eventFired(int creationCode, ExperimentDAO experiment, Integer signalGroupNumber) {
    switch (creationCode) {
    case ExperimentCreationListener.SHOW_DESCRIPTION_CODE:
      showExperimentDescription();
      break;
    case ExperimentCreationListener.SHOW_SCHEDULE_CODE:
      showExperimentSchedule(signalGroupNumber);
      break;
    case ExperimentCreationListener.SHOW_INPUTS_CODE:
      showExperimentInputs(signalGroupNumber);
      break;
    case ExperimentCreationListener.SHOW_PUBLISHING_CODE:
      showExperimentPublishing();
      break;
    case ExperimentCreationListener.NEXT:
      progressView();
      break;
    case ExperimentCreationListener.NEW_SIGNAL_GROUP:
      createAndDisplayNewSignalGroup();
      break;
    case ExperimentCreationListener.SAVE_EXPERIMENT:
      submitEvent();
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
  
  private Date getDateFromFormattedString(String dateString) {
    DateTimeFormat formatter = DateTimeFormat.getFormat(DATE_FORMAT);
    return formatter.parse(dateString);
  }
  
  // Visible for testing
  protected ExperimentDAO getExperiment() {
    return experiment;
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
}
