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
import com.google.sampling.experiential.shared.LoginInfo;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentDAOCore;

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
  protected MyConstants myConstants;
  protected MyMessages myMessages;
  private TextArea informedConsentPanel;
  private TextBox titlePanel;
  private Label creatorPanel;
  private TextArea descriptionPanel;
  private List<String> errorMessagesToDisplay;
  private ExperimentGroupListPanel experimentGroupListPanel;

  private TextBox organizationPanel;
  private TextBox contactEmailPanel;

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
    formPanel.setStyleName("experiment-definition-panel");
    initWidget(formPanel);

    String titleText = myConstants.experimentDefinition();
    Label lblExperimentDefinition = new Label(titleText);
    lblExperimentDefinition.setStyleName("paco-HTML-Large");
    formPanel.add(lblExperimentDefinition);

    createExperimentForm();

    errorMessagesToDisplay = new ArrayList<String>();
    errorMessagesToDisplay.add(myConstants.experimentCreationError());
  }

  private void createIdLabel(ExperimentDAOCore experiment) {
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

    PanelPair organizationPanelPair = createOrganizationPanel(experiment);
    organizationPanel = (TextBox) organizationPanelPair.valueHolder;
    formPanel.add(organizationPanelPair.container);

    PanelPair contactEmailPanelPair = createContactEmailPanel(experiment);
    contactEmailPanel = (TextBox) contactEmailPanelPair.valueHolder;
    formPanel.add(contactEmailPanelPair.container);

    PanelPair creatorPanelPair = createCreatorPanel(experiment);
    creatorPanel = (Label) creatorPanelPair.valueHolder;
    formPanel.add(creatorPanelPair.container);


    formPanel.add(createIdPanel(experiment).container);
    formPanel.add(createVersionPanel(experiment).container);

    PanelPair descriptionPanelPair = createDescriptionPanel(experiment);
    descriptionPanel = (TextArea) descriptionPanelPair.valueHolder;
    formPanel.add(descriptionPanelPair.container);

    experimentGroupListPanel = (ExperimentGroupListPanel) createExperimentGroupListPanel();
    formPanel.add(experimentGroupListPanel);

    formPanel.add(createAdminDisclosurePanel(experiment));
    PanelPair informedConsentPanelPair = createInformedConsentPanel(experiment);
    informedConsentPanel = (TextArea) informedConsentPanelPair.valueHolder;
    formPanel.add(informedConsentPanelPair.container);
    formPanel.add(createRecordPhoneDetailsPanel(experiment));
    formPanel.add(createExtraDataCollectionDeclarationPanel(experiment));

    formPanel.add(createPublishingPanel(experiment));
    createButtonPanel(experiment);
  }

  private Widget createExperimentGroupListPanel() {
    return new ExperimentGroupListPanel(experiment);
  }

  private Widget createRecordPhoneDetailsPanel(ExperimentDAOCore experiment) {
    return new RecordPhoneDetailsPanel(experiment);
  }


  private PanelPair createTitlePanel(ExperimentDAOCore experiment) {
    return createFormLine(myConstants.experimentTitle(), experiment.getTitle(), "keyLabel");
  }

  private PanelPair createIdPanel(ExperimentDAOCore experiment) {
    return createDisplayLine(myConstants.experimentId(),
                             Long.toString(experiment.getId() != null ? experiment.getId() : 0));
  }

  private PanelPair createVersionPanel(ExperimentDAO experiment) {
    return createDisplayLine(myConstants.experimentVersion(),
                             Integer.toString(experiment.getVersion() == null ? 0 : experiment.getVersion()));
  }

  private PanelPair createDescriptionPanel(ExperimentDAOCore experiment) {
    return createFormArea(myConstants.experimentDescription(), experiment.getDescription(), 75, "100");
  }

  private PanelPair createCreatorPanel(ExperimentDAOCore experiment) {
    return createDisplayLine(myConstants.experimentCreator(),
                             experiment.getCreator() != null ? experiment.getCreator() : loginInfo.getEmailAddress());
  }

  private PanelPair createOrganizationPanel(ExperimentDAOCore experiment) {
    return createFormLine(myConstants.experimentOrganization(),
                                 experiment.getOrganization(), "keyLabel");
  }

  private PanelPair createContactEmailPanel(ExperimentDAOCore experiment) {
    return createFormLine(myConstants.experimentContactEmail(),
                             experiment.getContactEmail(), "keyLabel");
  }


  private PanelPair createInformedConsentPanel(ExperimentDAOCore experiment) {
    return createFormArea(myConstants.informedConsent(), experiment.getInformedConsentForm(), 100, "200");
  }

  private void createButtonPanel(ExperimentDAO experiment) {
    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.add(createSubmitButton(experiment));
    buttonPanel.add(createCancelButton());
    buttonPanel.add(createPreviewButton(experiment));
    formPanel.add(buttonPanel);
  }

  private VerticalPanel createPublishingPanel(ExperimentDAO experiment) {
    VerticalPanel containerPanel = new VerticalPanel();
    containerPanel.setStyleName("bordered");

    HorizontalPanel publishingPanel = new HorizontalPanel();
    publishCheckBox = new CheckBox();
    publishCheckBox.setValue(experiment.getPublished());
    publishingPanel.add(publishCheckBox);
    HTML publishLabel = new HTML("<h3> " + myConstants.published() + "</h3>");
    publishingPanel.add(publishLabel);
    containerPanel.add(publishingPanel);

    createPublishedUsersDisclosurePanel(experiment);
    containerPanel.add(publishedUsersPanel);
    return containerPanel;
  }

  private DisclosurePanel createAdminDisclosurePanel(ExperimentDAO experiment) {
    final DisclosurePanel adminPanel = new DisclosurePanel();
    adminPanel.setStyleName("bordered");
    final DisclosurePanelHeader closedHeaderWidget = new DisclosurePanelHeader(
                                                                               false,
//                                                                               "<b>" +
                                                                                myConstants.clickToEditAdministrators()
//                                                                                   + "</b>"
                                                                                   );
    closedHeaderWidget.setStyleName("keyLabel");
    final DisclosurePanelHeader openHeaderWidget = new DisclosurePanelHeader(
                                                                             true,
//                                                                             "<b>" +
                                                                             myConstants.clickToCloseAdministratorEditor()
//                                                                                 + "</b>"
                                                                                 );
    openHeaderWidget.setStyleName("keyLabel");
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
    Label instructionlabel = new Label(myConstants.administratorEditorPrompt());
    adminContentPanel.add(instructionlabel);

    adminList = new TextArea();
    adminList.setCharacterWidth(100);
    adminList.setHeight("100");
    List<String> admins = experiment.getAdmins();
    String loginEmailLowercase = loginInfo.getEmailAddress().toLowerCase();
    if (!admins.contains(loginEmailLowercase)) {
      admins.add(loginEmailLowercase);
    }
    adminList.setText(toCSVString(admins));

    adminContentPanel.add(adminList);
    adminPanel.setContent(adminContentPanel);
    return adminPanel;
  }

  private DisclosurePanel createExtraDataCollectionDeclarationPanel(ExperimentDAOCore experiment) {
    final DisclosurePanel dataCollectedPanel = new DisclosurePanel();
    dataCollectedPanel.setStyleName("bordered");
    final DisclosurePanelHeader closedHeaderWidget = new DisclosurePanelHeader(
                                                                               false,
                                                                                myConstants.clickToEditExtraDataCollectionDeclarations()
                                                                                   );
    closedHeaderWidget.setStyleName("keyLabel");
    final DisclosurePanelHeader openHeaderWidget = new DisclosurePanelHeader(
                                                                             true,
                                                                             myConstants.clickToCloseExtraDataCollectionDeclarations()
                                                                                 );
    openHeaderWidget.setStyleName("keyLabel");
    dataCollectedPanel.setHeader(closedHeaderWidget);
    dataCollectedPanel.addEventHandler(new DisclosureHandler() {
      public void onClose(DisclosureEvent event) {
        dataCollectedPanel.setHeader(closedHeaderWidget);
      }

      public void onOpen(DisclosureEvent event) {
        dataCollectedPanel.setHeader(openHeaderWidget);
      }
    });
    VerticalPanel dataCollectionDeclarationContentPanel = new VerticalPanel();
    Label instructionlabel = new Label(myConstants.extraDataCollectionEditorPrompt());
    dataCollectionDeclarationContentPanel.add(instructionlabel);
    ListOfExtraDataCollectionDeclsPanel declList = new ListOfExtraDataCollectionDeclsPanel(experiment);
    declList.setWidth("100");
    declList.setHeight("100");
    dataCollectionDeclarationContentPanel.add(declList);
    dataCollectedPanel.setContent(dataCollectionDeclarationContentPanel);
    return dataCollectedPanel;
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

    List<String> userEmails = experiment.getPublishedUsers();
    userList.setText(toCSVString(userEmails));

    userContentPanel.add(userList);
    publishedUsersPanel.setContent(userContentPanel);
  }

  private PanelPair createFormLine(String key, String value) {
    return createFormLine(key, value, null);
  }

  private PanelPair createFormLine(String key, String value, String styleName) {
    VerticalPanel line = new VerticalPanel();
    line.setStyleName("bordered");
    Label keyLabel = new Label(key + ": ");
    keyLabel.setStyleName(styleName == null ? "keyLabel" : styleName);
    TextBox valueBox = new TextBox();
    if (value != null) {
      valueBox.setText(value);
    }
    valueBox.setEnabled(true);
    valueBox.setWidth("100%");
    line.add(keyLabel);
    line.add(valueBox);

    return new PanelPair(line, valueBox);
  }

  private PanelPair createFormArea(String key, String value, int width, String height) {
    VerticalPanel line = new VerticalPanel();
    line.setStyleName("bordered");
    //line.setStyleName("left");
    Label keyLabel = new Label(key + ": ");
    keyLabel.setStyleName("keyLabel");
    final TextArea valueBox = new TextArea();
    valueBox.setWidth("100%");
    valueBox.setHeight(height);
    if (value != null) {
      valueBox.setText(value);
    }
    valueBox.addValueChangeHandler(new ValueChangeHandler<String>() {

      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        if (valueBox.getText().length() >= 500) {
          // TODO surface a message that their text is being truncated.
          valueBox.setText(valueBox.getText().substring(0, 500));
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

  private Widget createPreviewButton(final ExperimentDAO experiment) {

    Button whatButton = new Button("Preview Json");
    whatButton.addClickListener(new ClickListener() {

      @Override
      public void onClick(Widget sender) {
        previewJson(experiment);
      }

    });
    return whatButton;
  }

  public native String stringify(Object s) /*-{
  return JSON.stringify(s, null, 2);
}-*/;

  public native void showPreview(String s) /*-{
    var x=window.open();
    x.document.open();
    x.document.write(s);
    x.document.close();
  }-*/;

  protected void previewJson(ExperimentDAO experiment2) {
    AutoBean<ExperimentDAO> bean = AutoBeanUtils.getAutoBean(experiment2);
    String json = AutoBeanCodex.encode(bean).getPayload();
    //Window.open("<h2>Experiment json</h2><br>" + stringify(experiment2), "JSON Preview", null);
    showPreview("<pre>" + stringify(experiment2) + "</pre>");
  }

  // Visible for testing
  protected boolean canSubmit() {
    List<Boolean> allRequirementsAreMet = Arrays.asList(checkRequiredFieldsAreFilledAndHighlight(),
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
    List<Boolean> areRequiredWidgetsFilled = Arrays.asList(checkTextFieldIsFilledAndHighlight(titlePanel));
    return !areRequiredWidgetsFilled.contains(false);
  }

  private boolean checkTextFieldIsFilledAndHighlight(TextBoxBase widget) {
    boolean isFilled = !widget.getText().isEmpty();
    setPanelHighlight(widget, isFilled);
    return isFilled;
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
      setContactEmailOn();
      setOrganizationOn();
      setDescriptionOn(experiment);
      setCreatorOn(experiment);
      setAdminsOn(experiment);
      setInformedConsentOn(experiment);
      setPublishingOn(experiment);
      setModifyDateOn(experiment);
      experimentGroupListPanel.recordUIChanges();
      saveExperiment();
    } catch (Throwable t) {
      Window.alert("Throwable: " + t.getMessage());
    }
  }

  private void setOrganizationOn() {
    experiment.setOrganization(organizationPanel.getText());
  }

  private void setContactEmailOn() {
      experiment.setContactEmail(contactEmailPanel.getText());
  }

  private void setCreatorOn(ExperimentDAOCore experiment) {
    if (experiment.getCreator() == null) {
      experiment.setCreator(loginInfo.getEmailAddress());
    }
  }

  private void setDescriptionOn(ExperimentDAOCore experiment) {
    experiment.setDescription(descriptionPanel.getText());
  }

  private void setTitleOn(ExperimentDAOCore experiment) {
    experiment.setTitle(titlePanel.getText());
  }

  protected void setTitleInPanel(String title) {
    titlePanel.setText(title);
  }

  private void setInformedConsentOn(ExperimentDAOCore experiment) {
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
    experiment.setAdmins(admins);
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
    experiment.setPublishedUsers(userEmails);
  }

  // Visible for testing
  protected void setPublishedUsersInPanel(String commaSepEmailList) {
    userList.setText(commaSepEmailList);
  }
}
