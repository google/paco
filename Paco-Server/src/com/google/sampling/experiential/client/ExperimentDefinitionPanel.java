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
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosureEvent;
import com.google.gwt.user.client.ui.DisclosureHandler;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.DisclosurePanelImages;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.paco.shared.model.FeedbackDAO;
import com.google.sampling.experiential.shared.LoginInfo;

import edu.ycp.cs.dh.acegwt.client.ace.AceEditor;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorMode;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorTheme;

/**
 * The main panel for viewing the details of an experiment Also used as the
 * basis of creation and editing of experiments. Delegates specific parts of
 * experiment definition to sub panels. Handles communication with subpanels
 * about state of edits.
 *
 * @author Bob Evans
 *
 */
public class ExperimentDefinitionPanel extends Composite {

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

  private VerticalPanel formPanel;

  private CheckBox publishCheckBox;
  private TextArea adminList;
  private DisclosurePanel publishedUsersPanel;
  private TextArea userList;
  private LoginInfo loginInfo;
  private CheckBox customFeedbackCheckBox;
  private DisclosurePanel customFeedbackPanel;
  private TextArea customFeedbackText;
  protected MyConstants myConstants;
  protected MyMessages myMessages;
  private DurationView durationPanel;
  private TextArea informedConsentPanel;
  private TextBox titlePanel;
  private Label creatorPanel;
  private TextArea descriptionPanel;
  private InputsListPanel inputsListPanel;

  private List<String> errorMessagesToDisplay;

  private CheckBox customRenderingCheckBox;

  private DisclosurePanel customRenderingPanel;

  private TextArea customRenderingText;

  private AceEditor customRenderingEditor;
  private AceEditor customFeedbackEditor;
  private CheckBox showFeedbackCheckBox;

  public ExperimentDefinitionPanel(ExperimentDAO experiment, LoginInfo loginInfo, ExperimentListener listener) {
    myConstants = GWT.create(MyConstants.class);
    myMessages = GWT.create(MyMessages.class);

    this.experiment = experiment;
    this.loginInfo = loginInfo;
    this.listeners = new ArrayList<ExperimentListener>();
    if (listener != null) {
      listeners.add(listener);
    }
    formPanel = new VerticalPanel();
    initWidget(formPanel);

    String titleText = myConstants.experimentDefinition();
    Label lblExperimentDefinition = new Label(titleText);
    lblExperimentDefinition.setStyleName("paco-HTML-Large");
    formPanel.add(lblExperimentDefinition);

    createExperimentForm();

    errorMessagesToDisplay = new ArrayList<String>();
    errorMessagesToDisplay.add(myConstants.experimentCreationError());
    // errorMessagesToDisplay = Arrays.asList(myConstants.experimentCreationError());
  }

  private void createIdLabel(ExperimentDAO experiment) {
    Long experimentVersionStr = 0l;
    if (experiment.getId() != null) {
      experimentVersionStr = experiment.getId();
    }
    HorizontalPanel versionPanel = new HorizontalPanel();
    formPanel.add(versionPanel);
    Label lblExperimentVersion = new Label(myConstants.experimentId() + ":");
    lblExperimentVersion.setStyleName("paco-HTML-Large");
    versionPanel.add(lblExperimentVersion);

    Label experimentVersion = new Label(Long.toString(experimentVersionStr));
    experimentVersion.setStyleName("paco-HTML-Large");
    versionPanel.add(experimentVersion);
  }

  private void createVersionLabel(ExperimentDAO experiment) {
    String experimentVersionStr = "1";
    if (experiment.getVersion() != null) {
      experimentVersionStr = experiment.getVersion().toString();
    }
    HorizontalPanel versionPanel = new HorizontalPanel();
    formPanel.add(versionPanel);

    Label lblExperimentVersion = new Label(myConstants.experimentVersion() + ":");
    lblExperimentVersion.setStyleName("paco-HTML-Large");
    versionPanel.add(lblExperimentVersion);

    Label experimentVersion = new Label(experimentVersionStr);
    experimentVersion.setStyleName("paco-HTML-Large");
    versionPanel.add(experimentVersion);
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

  private void createExperimentForm() {
    PanelPair titlePanelPair = createTitlePanel(experiment);
    titlePanel = (TextBox) titlePanelPair.valueHolder;
    formPanel.add(titlePanelPair.container);

    formPanel.add(createIdPanel(experiment).container);
    formPanel.add(createVersionPanel(experiment).container);

    PanelPair descriptionPanelPair = createDescriptionPanel(experiment);
    descriptionPanel = (TextArea) descriptionPanelPair.valueHolder;
    formPanel.add(descriptionPanelPair.container);

    PanelPair creatorPanelPair = createCreatorPanel(experiment);
    creatorPanel = (Label) creatorPanelPair.valueHolder;
    formPanel.add(creatorPanelPair.container);

    formPanel.add(createAdminDisclosurePanel(experiment));

    PanelPair informedConsentPanelPair = createInformedConsentPanel(experiment);
    informedConsentPanel = (TextArea) informedConsentPanelPair.valueHolder;
    formPanel.add(informedConsentPanelPair.container);

    formPanel.add(createDurationPanel(experiment));
    formPanel.add(createSignalMechanismPanel(experiment));

    formPanel.add(createInputsHeader());
    formPanel.add(createInputsListPanel(experiment));

    createCustomRenderingEntryPanel(experiment);

    createFeedbackEntryPanel(experiment);
    createPublishingPanel(experiment);
    createButtonPanel(experiment);
  }

  private SignalMechanismChooserPanel createSignalMechanismPanel(ExperimentDAO experiment2) {
    return new SignalMechanismChooserPanel(experiment);
  }


  private Widget createCustomRenderingEntryPanel(ExperimentDAO experiment2) {
    HorizontalPanel renderingPanel = new HorizontalPanel();
    customRenderingCheckBox = new CheckBox();
    customRenderingCheckBox.setChecked(experiment.isCustomRendering());
    renderingPanel.add(customRenderingCheckBox);
    Label renderingLabel = new Label(myConstants.customRendering());
    renderingPanel.add(renderingLabel);
    HTML html = new HTML("&nbsp;&nbsp;&nbsp;<font color=\"red\" size=\"smaller\"><i>(" + myConstants.iOSIncompatible() + ")</i></font>");
    renderingPanel.add(html);
    formPanel.add(renderingPanel);

    createCustomRenderingDisclosurePanel(experiment);
    formPanel.add(customRenderingPanel);
    return renderingPanel;
  }

  /**
   * @param experiment2
   */
  private void createCustomRenderingDisclosurePanel(ExperimentDAO experiment2) {
    customRenderingPanel = new DisclosurePanel();

    final DisclosurePanelHeader closedHeaderWidget = new DisclosurePanelHeader(
                                                                               false,
                                                                               "<b>"
                                                                                   + myConstants.clickToEditCustomRendering()
                                                                                   + "</b>");
    final DisclosurePanelHeader openHeaderWidget = new DisclosurePanelHeader(
                                                                             true,
                                                                             "<b>"
                                                                                 + myConstants.clickToCloseCustomRenderingEditor()
                                                                                 + "</b>");

    customRenderingPanel.setHeader(closedHeaderWidget);
    customRenderingPanel.addEventHandler(new DisclosureHandler() {
      public void onClose(DisclosureEvent event) {
        customRenderingPanel.setHeader(closedHeaderWidget);
      }

      public void onOpen(DisclosureEvent event) {
        customRenderingPanel.setHeader(openHeaderWidget);
      }
    });

    VerticalPanel userContentPanel = new VerticalPanel();
    Label instructionLabel = new Label(myConstants.customRenderingInstructions());
    userContentPanel.add(instructionLabel);

    customRenderingEditor = new AceEditor();
    customRenderingEditor.setWidth("600px");
    customRenderingEditor.setHeight("400px");
    customRenderingEditor.startEditor();
    customRenderingEditor.setMode(AceEditorMode.JAVASCRIPT);
    customRenderingEditor.setTheme(AceEditorTheme.ECLIPSE);

    String customRendering = experiment.getCustomRenderingCode();

    if (customRendering != null) {
      customRenderingEditor.setText(customRendering);
    }

    userContentPanel.add(customRenderingEditor);
    customRenderingPanel.setContent(userContentPanel);
  }

  /**
   * @param experiment2
   * @return
   */
  private Widget createFeedbackEntryPanel(ExperimentDAO experiment2) {
    VerticalPanel feedbackPanel = new VerticalPanel();
    feedbackPanel.add(createShowFeedbackCheckboxPanel());
    feedbackPanel.add(createCustomFeedbackCheckboxPanel());
    formPanel.add(feedbackPanel);

    customFeedbackPanel = createCustomFeedbackDisclosurePanel(experiment);
    formPanel.add(customFeedbackPanel);
    return feedbackPanel;
  }

  private HorizontalPanel createShowFeedbackCheckboxPanel() {
    HorizontalPanel showFeedbackPanel = new HorizontalPanel();
    showFeedbackCheckBox = new CheckBox();
    showFeedbackCheckBox.setValue(experiment.shouldShowFeedback());
    showFeedbackPanel.add(showFeedbackCheckBox);
    Label showFeedbackLabel = new Label(myConstants.showFeedback());
    showFeedbackPanel.add(showFeedbackLabel);
    return showFeedbackPanel;
  }

  private HorizontalPanel createCustomFeedbackCheckboxPanel() {
    HorizontalPanel customFeedbackCheckboxPanel = new HorizontalPanel();
    customFeedbackCheckBox = new CheckBox();
    customFeedbackCheckBox.setValue(hasNonDefaultFeedback());
    customFeedbackCheckboxPanel.add(customFeedbackCheckBox);
    Label feedbackLabel = new Label(myConstants.customFeedback());
    customFeedbackCheckboxPanel.add(feedbackLabel);

    HTML html = new HTML("&nbsp;&nbsp;&nbsp;<font color=\"red\" size=\"smaller\"><i>(" + myConstants.iOSIncompatible() + ")</i></font>");
    customFeedbackCheckboxPanel.add(html);
    return customFeedbackCheckboxPanel;
  }

  private boolean hasNonDefaultFeedback() {
    return (experiment.hasCustomFeedback() != null && experiment.hasCustomFeedback()) || oldMethodBasedOnNonDefaultFeedbackText();
  }

  private boolean oldMethodBasedOnNonDefaultFeedbackText() {
    return experiment.getFeedback() != null &&
        experiment.getFeedback().length > 0 &&
        !defaultFeedback(experiment.getFeedback()[0]);
  }

  /**
   * @param experiment2
   * @return
   */
  private DisclosurePanel createCustomFeedbackDisclosurePanel(ExperimentDAO experiment2) {
    final DisclosurePanel customFeedbackPanel = new DisclosurePanel();

    final DisclosurePanelHeader closedHeaderWidget = new DisclosurePanelHeader(
                                                                               false,
                                                                               "<b>"
                                                                                   + myConstants.clickToEditCustomFeedback()
                                                                                   + "</b>");
    final DisclosurePanelHeader openHeaderWidget = new DisclosurePanelHeader(
                                                                             true,
                                                                             "<b>"
                                                                                 + myConstants.clickToCloseCustomFeedbackEditor()
                                                                                 + "</b>");

    customFeedbackPanel.setHeader(closedHeaderWidget);
    customFeedbackPanel.addEventHandler(new DisclosureHandler() {
      public void onClose(DisclosureEvent event) {
        customFeedbackPanel.setHeader(closedHeaderWidget);
      }

      public void onOpen(DisclosureEvent event) {
        customFeedbackPanel.setHeader(openHeaderWidget);
      }
    });

    VerticalPanel userContentPanel = new VerticalPanel();
    Label instructionLabel = new Label(myConstants.customFeedbackInstructions());
    userContentPanel.add(instructionLabel);

    customFeedbackEditor = new AceEditor();
    customFeedbackEditor.setWidth("600px");
    customFeedbackEditor.setHeight("400px");
    customFeedbackEditor.startEditor();
    customFeedbackEditor.setMode(AceEditorMode.JAVASCRIPT);
    customFeedbackEditor.setTheme(AceEditorTheme.ECLIPSE);


    FeedbackDAO[] feedbacks = experiment.getFeedback();

    if (feedbacks != null && feedbacks.length > 0 && !defaultFeedback(feedbacks[0])) {
      customFeedbackEditor.setText(feedbacks[0].getText());
    }

    userContentPanel.add(customFeedbackEditor);
    customFeedbackPanel.setContent(userContentPanel);
    return customFeedbackPanel;
  }

  /**
   * @param feedbackDAO
   * @return
   */
  private boolean defaultFeedback(FeedbackDAO feedbackDAO) {
    return feedbackDAO.getText().equals(FeedbackDAO.DEFAULT_FEEDBACK_MSG);
  }

  private PanelPair createTitlePanel(ExperimentDAO experiment) {
    return createFormLine(myConstants.experimentTitle(), experiment.getTitle(), "paco-HTML-Large");
  }

  private PanelPair createIdPanel(ExperimentDAO experiment) {
    return createDisplayLine(myConstants.experimentId(),
                             Long.toString(experiment.getId() != null ? experiment.getId() : 0));
  }

  private PanelPair createVersionPanel(ExperimentDAO experiment) {
    return createDisplayLine(myConstants.experimentVersion(),
                             Integer.toString(experiment.getVersion() == null ? 0 : experiment.getVersion()));
  }

  private PanelPair createDescriptionPanel(ExperimentDAO experiment) {
    return createFormArea(myConstants.experimentDescription(), experiment.getDescription(), 75, "100");
  }

  private PanelPair createCreatorPanel(ExperimentDAO experiment) {
    return createDisplayLine(myConstants.experimentCreator(),
                             experiment.getCreator() != null ? experiment.getCreator() : loginInfo.getEmailAddress());
  }

  private PanelPair createInformedConsentPanel(ExperimentDAO experiment) {
    return createFormArea(myConstants.informedConsent(), experiment.getInformedConsentForm(), 100, "200");
  }

  private HTML createInputsHeader() {
    HTML questionsPrompt = new HTML("<h2>" + myConstants.enterAtLeastOneQuestion() + "</h2>");
    questionsPrompt.setStyleName("keyLabel");
    return questionsPrompt;
  }

  private DurationView createDurationPanel(ExperimentDAO experiment) {
    durationPanel = new DurationView(experiment.getFixedDuration(), experiment.getStartDate(),
                                                  experiment.getEndDate());

    return durationPanel;
  }

  private InputsListPanel createInputsListPanel(ExperimentDAO experiment) {
    inputsListPanel = new InputsListPanel(experiment);
    inputsListPanel.setStyleName("left");
    return inputsListPanel;
  }

  private void createButtonPanel(ExperimentDAO experiment) {
    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.add(createSubmitButton(experiment));
    buttonPanel.add(createCancelButton());
    formPanel.add(buttonPanel);
  }

  private void createPublishingPanel(ExperimentDAO experiment) {
    HorizontalPanel publishingPanel = new HorizontalPanel();
    publishCheckBox = new CheckBox();
    publishCheckBox.setValue(experiment.getPublished());
    publishingPanel.add(publishCheckBox);
    Label publishLabel = new Label(myConstants.published());
    publishingPanel.add(publishLabel);
    formPanel.add(publishingPanel);

    createPublishedUsersDisclosurePanel(experiment);
    formPanel.add(publishedUsersPanel);
  }

  private DisclosurePanel createAdminDisclosurePanel(ExperimentDAO experiment) {
    final DisclosurePanel adminPanel = new DisclosurePanel();
    final DisclosurePanelHeader closedHeaderWidget = new DisclosurePanelHeader(
                                                                               false,
                                                                               "<b>"
                                                                                   + myConstants.clickToEditAdministrators()
                                                                                   + "</b>");
    final DisclosurePanelHeader openHeaderWidget = new DisclosurePanelHeader(
                                                                             true,
                                                                             "<b>"
                                                                                 + myConstants.clickToCloseAdministratorEditor()
                                                                                 + "</b>");
    adminPanel.setHeader(closedHeaderWidget);
    adminPanel.addEventHandler(new DisclosureHandler() {
      public void onClose(DisclosureEvent event) {
        adminPanel.setHeader(closedHeaderWidget);
      }

      public void onOpen(DisclosureEvent event) {
        adminPanel.setHeader(openHeaderWidget);
      }
    });
    VerticalPanel adminContentPanel = new VerticalPanel();
    Label instructionlabel = createLabel(myConstants.administratorEditorPrompt());
    adminContentPanel.add(instructionlabel);

    adminList = new TextArea();
    adminList.setCharacterWidth(100);
    adminList.setHeight("100");
    String[] adminStrArray = experiment.getAdmins();
    List<String> admins = Lists.newArrayList(adminStrArray);
    String loginEmailLowercase = loginInfo.getEmailAddress().toLowerCase();
    if (!admins.contains(loginEmailLowercase)) {
      admins.add(loginEmailLowercase);
    }
    adminList.setText(toCSVString(admins));

    adminContentPanel.add(adminList);
    adminPanel.setContent(adminContentPanel);
    return adminPanel;
  }

  final DisclosurePanelImages images = (DisclosurePanelImages) GWT.create(DisclosurePanelImages.class);

  class DisclosurePanelHeader extends HorizontalPanel {
    public DisclosurePanelHeader(boolean isOpen, String html) {
      add(isOpen ? images.disclosurePanelOpen().createImage()
                 : images.disclosurePanelClosed().createImage());
      add(new HTML(html));
    }
  }

  private void createPublishedUsersDisclosurePanel(ExperimentDAO experiment) {
    publishedUsersPanel = new DisclosurePanel();

    final DisclosurePanelHeader closedHeaderWidget = new DisclosurePanelHeader(false,
                                                                               "<b>"
                                                                                   + myConstants.clickToEditPublished()
                                                                                   + "</b>");
    final DisclosurePanelHeader openHeaderWidget = new DisclosurePanelHeader(
                                                                             true,
                                                                             "<b>"
                                                                                 + myConstants.clickToClosePublishedEditor()
                                                                                 + "</b>");

    publishedUsersPanel.setHeader(closedHeaderWidget);
    publishedUsersPanel.addEventHandler(new DisclosureHandler() {

      public void onClose(DisclosureEvent event) {
        publishedUsersPanel.setHeader(closedHeaderWidget);
      }

      public void onOpen(DisclosureEvent event) {
        publishedUsersPanel.setHeader(openHeaderWidget);
      }
    });

    VerticalPanel userContentPanel = new VerticalPanel();
    Label instructionLabel = new Label(myConstants.publishedEditorPrompt());
    userContentPanel.add(instructionLabel);

    userList = new TextArea();
    userList.setCharacterWidth(100);
    userList.setHeight("100");

    String[] usersStrArray = experiment.getPublishedUsers();
    List<String> userEmails = Lists.newArrayList(usersStrArray);
    userList.setText(toCSVString(userEmails));

    userContentPanel.add(userList);
    publishedUsersPanel.setContent(userContentPanel);
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

  private String toCSVString(List<String> list) {
    StringBuilder buf = new StringBuilder();
    boolean first = true;
    for (String item : list) {
      if (first) {
        first = false;
      } else {
        buf.append(", ");
      }
      buf.append(item.toLowerCase());
    }
    return buf.toString();
  }

  private Label createLabel(String title) {
    Label responseTypeLabel = new Label(title);
    responseTypeLabel.setStyleName("keyLabel");
    return responseTypeLabel;
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

  private Widget createSubmitButton(final ExperimentDAO experiment) {

    Button whatButton = new Button(experiment.getId() == null ? myConstants.createExperiment()
                                                             : myConstants.updateExperiment());
    whatButton.addClickListener(new ClickListener() {

      @Override
      public void onClick(Widget sender) {
        if (canSubmit()) {
          submitEvent(experiment);
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
    return inputsListPanel;
  }

  // Visible for testing
  protected DurationView getDurationPanel() {
    return durationPanel;
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
    List<Boolean> areRequiredWidgetsFilled = Arrays.asList(checkTextFieldIsFilledAndHighlight(titlePanel),
                                                           // textFieldIsFilled(informedConsentPanel),
                                                           checkListItemsHaveAtLeastOneOptionAndHighlight());
    return !areRequiredWidgetsFilled.contains(false);
  }

  private boolean checkTextFieldIsFilledAndHighlight(TextBoxBase widget) {
    boolean isFilled = !widget.getText().isEmpty();
    setPanelHighlight(widget, isFilled);
    return isFilled;
  }

  private boolean checkListItemsHaveAtLeastOneOptionAndHighlight() {
    return inputsListPanel.checkListItemsHaveAtLeatOneOptionAndHighlight();
  }

  private boolean checkVariableNamesHaveNoSpacesAndHighlight() {
    return inputsListPanel.checkVarNamesFilledWithoutSpacesAndHighlight();
  }

  // Visible for testing
  protected boolean startDateIsNotAfterEndDate() {
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
    boolean adminListIsValid = checkEmailFieldIsValidAndHighlight(adminList);
    boolean userListIsValid = checkEmailFieldIsValidAndHighlight(userList);
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

  private void submitEvent(ExperimentDAO experiment) {
    try {
      setTitleOn(experiment);
      setDescriptionOn(experiment);
      setCreatorOn(experiment);
      setAdminsOn(experiment);
      setInformedConsentOn(experiment);
      // setQuestionsChangeOn(experiment);
      setDurationOn(experiment);
      setFeedbackOn(experiment);
      setCustomRenderingOn(experiment);
      setPublishingOn(experiment);
      setModifyDateOn(experiment);

      saveExperiment();
    } catch (Throwable t) {
      Window.alert("Throwable: " + t.getMessage());
    }
  }

  private void setCustomRenderingOn(ExperimentDAO experiment2) {
    experiment.setCustomRendering(customRenderingCheckBox.getValue());
    experiment.setCustomRenderingCode(customRenderingEditor.getText());
  }

  private void setCreatorOn(ExperimentDAO experiment) {
    if (experiment.getCreator() == null) {
      experiment.setCreator(loginInfo.getEmailAddress());
    }
  }

  private void setDescriptionOn(ExperimentDAO experiment) {
    experiment.setDescription(descriptionPanel.getText());
  }

  private void setTitleOn(ExperimentDAO experiment) {
    experiment.setTitle(titlePanel.getText());
  }

  protected void setTitleInPanel(String title) {
    titlePanel.setText(title);
  }

  // private void setQuestionsChangeOn(ExperimentDAO experiment) {
  // experiment.setQuestionsChange(
  // ((BooleanValueHolder) fieldToWidgetMap.get("questionsChange")).getValue());
  // }

  private void setInformedConsentOn(ExperimentDAO experiment) {
    experiment.setInformedConsentForm(informedConsentPanel.getText());
  }

  protected void setInformedConsentInPanel(String title) {
    informedConsentPanel.setText(title);
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

  private Date getDateFromFormattedString(String dateString) {
    DateTimeFormat formatter = DateTimeFormat.getFormat(DATE_FORMAT);
    return formatter.parse(dateString);
  }

  private void setPublishingOn(ExperimentDAO experiment) {
    experiment.setPublished(publishCheckBox.getValue());
    setPublishedUsersOn(experiment);
  }

  private void setFeedbackOn(ExperimentDAO experiment) {
    experiment.setHasCustomFeedback(customFeedbackCheckBox.getValue());
    experiment.setShowFeedback(showFeedbackCheckBox.getValue());
    if (!customFeedbackCheckBox.getValue()) {
      experiment.setFeedback(new FeedbackDAO[] { new FeedbackDAO(null, FeedbackDAO.DEFAULT_FEEDBACK_MSG) });
    } else {
      experiment.setFeedback(new FeedbackDAO[] { new FeedbackDAO(null, customFeedbackEditor.getText()) });
    }
  }

  private void setDurationOn(ExperimentDAO experiment) {
    experiment.setFixedDuration(durationPanel.isFixedDuration());
    if (experiment.getFixedDuration()) {
      experiment.setStartDate(durationPanel.getStartDate());
      experiment.setEndDate(durationPanel.getEndDate());
    } else {
      experiment.setStartDate(null);
      experiment.setEndDate(null);
    }

  }

  private void setAdminsOn(ExperimentDAO experiment) {
    List<String> admins = new ArrayList<String>();
    String adminsText = adminList.getText();
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

  // Visible for testing
  protected void setAdminsInPanel(String commaSepEmailList) {
    adminList.setText(commaSepEmailList);
  }

  private void setPublishedUsersOn(ExperimentDAO experiment) {
    List<String> userEmails = new ArrayList<String>();
    String userListText = userList.getText();
    if (userListText.length() > 0) {
      Splitter sp = Splitter.on(",").trimResults().omitEmptyStrings();
      for (String userEmail : sp.split(userListText)) {
        userEmails.add(userEmail);
      }
    }
    String[] userEmailsStrArray = new String[userEmails.size()];
    userEmailsStrArray = userEmails.toArray(userEmailsStrArray);
    experiment.setPublishedUsers(userEmailsStrArray);
  }

  // Visible for testing
  protected void setPublishedUsersInPanel(String commaSepEmailList) {
    userList.setText(commaSepEmailList);
  }
}
