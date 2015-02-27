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
import com.google.paco.shared.model2.ExperimentGroup;
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
public class ExperimentGroupPanel extends Composite {

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

//  private ExperimentDAO experiment;

  private VerticalPanel formPanel;

  private CheckBox publishCheckBox;
  private TextArea adminList;
  private DisclosurePanel publishedUsersPanel;
  private TextArea userList;
  private LoginInfo loginInfo;
  protected MyConstants myConstants;
  protected MyMessages myMessages;
  private DurationView durationPanel;
  private TextArea informedConsentPanel;
  private TextBox namePanel;
  private Label creatorPanel;
  private TextArea descriptionPanel;
  private InputsListPanel inputsListPanel;

  private List<String> errorMessagesToDisplay;

  private CheckBox customRenderingCheckBox;

  private DisclosurePanel customRenderingPanel;

  private TextArea customRenderingText;

  private AceEditor customRenderingEditor;
  private ExperimentGroup group;
  private ExperimentGroupListPanel parent;

  public ExperimentGroupPanel(ExperimentGroupListPanel parent, ExperimentGroup group) {
    this.parent = parent;
    myConstants = GWT.create(MyConstants.class);
    myMessages = GWT.create(MyMessages.class);
    this.group = group;

    formPanel = new VerticalPanel();
    formPanel.setStyleName("experiment-definition-panel");
    initWidget(formPanel);

    String titleText = myConstants.experimentGroupDefinition();
    Label lblExperimentDefinition = new Label(titleText);
    lblExperimentDefinition.setStyleName("paco-HTML-Large");
    formPanel.add(lblExperimentDefinition);

    createExperimentForm();

    errorMessagesToDisplay = new ArrayList<String>();
    errorMessagesToDisplay.add(myConstants.experimentCreationError());
  }

  private void createExperimentForm() {
    PanelPair namePanelPair = createTitlePanel(group);
    createListMgmtButtons();
    namePanel = (TextBox) namePanelPair.valueHolder;
    formPanel.add(namePanelPair.container);

    VerticalPanel inputsContainerPanel = new VerticalPanel();
    inputsContainerPanel.setStyleName("bordered");
    inputsContainerPanel.add(createInputsHeader());
    inputsContainerPanel.add(createInputsListPanel(group));
    formPanel.add(inputsContainerPanel);

    formPanel.add(createDurationPanel());
    formPanel.add(createActionTriggerListPanel(group));

    formPanel.add(createBackgroundListeningPanel(group));
    formPanel.add(createBackgroundPollingPanel(group));

    formPanel.add(createCustomRenderingEntryPanel(group));

    formPanel.add(createFeedbackEntryPanel(group));
    formPanel.add(createListMgmtButtons());
  }

  private HorizontalPanel createListMgmtButtons() {
    HorizontalPanel upperLinePanel = new HorizontalPanel();
    Button deleteButton = new Button("-");
    deleteButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        parent.deleteGroup(ExperimentGroupPanel.this);
      }
    });
    upperLinePanel.add(deleteButton);

    Button addButton = new Button("+");
    upperLinePanel.add(addButton);

    addButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        parent.addGroup(ExperimentGroupPanel.this);
      }
    });
    return upperLinePanel;
  }


  private Widget createBackgroundListeningPanel(ExperimentGroup group) {
    return new BackgroundListeningPanel(group);
  }

  private Widget createBackgroundPollingPanel(ExperimentGroup group) {
    return new BackgroundPollingPanel(group);
  }

  private ActionTriggerListPanel createActionTriggerListPanel(ExperimentGroup group) {
    return new ActionTriggerListPanel(group);
  }


  private Widget createCustomRenderingEntryPanel(ExperimentGroup group2) {
    VerticalPanel containerPanel = new VerticalPanel();
    containerPanel.setStyleName("bordered");

    HorizontalPanel renderingPanel = new HorizontalPanel();
    customRenderingCheckBox = new CheckBox();
    customRenderingCheckBox.setValue(group2.getCustomRendering() != null ? group2.getCustomRendering() : false);
    renderingPanel.add(customRenderingCheckBox);

    Label renderingLabel = new Label(myConstants.customRendering());
    renderingPanel.add(renderingLabel);

    HTML html = new HTML("&nbsp;&nbsp;&nbsp;<font color=\"red\" size=\"smaller\"><i>(" + myConstants.iOSIncompatible() + ")</i></font>");
    renderingPanel.add(html);

    containerPanel.add(renderingPanel);

    createCustomRenderingDisclosurePanel(group2);
    customRenderingPanel.getHeader().setVisible(customRenderingCheckBox.getValue());
    containerPanel.add(customRenderingPanel);

    customRenderingCheckBox.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        customRenderingPanel.getHeader().setVisible(customRenderingCheckBox.getValue());
        customRenderingPanel.setOpen(customRenderingCheckBox.getValue());
      }
    });
    return containerPanel;
  }

  /**
   * @param group2
   */
  private void createCustomRenderingDisclosurePanel(ExperimentGroup group2) {
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
        boolean currentlyVisible = customRenderingPanel.getHeader().isVisible();
        customRenderingPanel.setHeader(closedHeaderWidget);
        closedHeaderWidget.setVisible(currentlyVisible);
      }

      public void onOpen(DisclosureEvent event) {
        boolean currentlyVisible = customRenderingPanel.getHeader().isVisible();
        customRenderingPanel.setHeader(openHeaderWidget);
        openHeaderWidget.setVisible(currentlyVisible);
      }
    });

    VerticalPanel userContentPanel = new VerticalPanel();
    Label instructionLabel = new Label(myConstants.customRenderingInstructions());
    userContentPanel.add(instructionLabel);

    customRenderingEditor = new AceEditor();
    customRenderingEditor.setWidth("1024px");
    customRenderingEditor.setHeight("800px");
    customRenderingEditor.startEditor();
    customRenderingEditor.setMode(AceEditorMode.JAVASCRIPT);
    customRenderingEditor.setTheme(AceEditorTheme.ECLIPSE);

    String customRendering = group.getCustomRenderingCode();

    if (customRendering != null) {
      customRenderingEditor.setText(customRendering);
    }

    userContentPanel.add(customRenderingEditor);
    customRenderingPanel.setContent(userContentPanel);
  }

  /**
   * @param group2
   * @return
   */
  private Widget createFeedbackEntryPanel(ExperimentGroup group2) {
    VerticalPanel containerPanel = new VerticalPanel();
    containerPanel.setStyleName("bordered");

    VerticalPanel feedbackPanel = new VerticalPanel();
    feedbackPanel.add(createFeedbackTypeSelectorPanel());
    containerPanel.add(feedbackPanel);
    return containerPanel;
  }

  private FeedbackChooserPanel createFeedbackTypeSelectorPanel() {
    return new FeedbackChooserPanel(group);
  }


  private PanelPair createTitlePanel(ExperimentGroup group2) {
    return createFormLine(myConstants.experimentTitle(), group2.getName(), "keyLabel");
  }

  private HTML createInputsHeader() {
    HTML questionsPrompt = new HTML("<h2>" + myConstants.enterAtLeastOneQuestion() + ":</h2>");
    questionsPrompt.setStyleName("keyLabel");
    return questionsPrompt;
  }

  private DurationView createDurationPanel() {
    durationPanel = new DurationView(group.getFixedDuration(), group.getStartDate(),
                                                  group.getEndDate());

    return durationPanel;
  }

  private InputsListPanel createInputsListPanel(ExperimentGroup group2) {
    inputsListPanel = new InputsListPanel(group2);
    return inputsListPanel;
  }


  final DisclosurePanelImages images = (DisclosurePanelImages) GWT.create(DisclosurePanelImages.class);


  class DisclosurePanelHeader extends HorizontalPanel {
    public DisclosurePanelHeader(boolean isOpen, String html) {
      add(isOpen ? images.disclosurePanelOpen().createImage()
                 : images.disclosurePanelClosed().createImage());
      add(new HTML(html));
    }
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
    List<Boolean> areRequiredWidgetsFilled = Arrays.asList(checkTextFieldIsFilledAndHighlight(namePanel),
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

  // TODO this is not currently called. Change to do valuechangehandler updating
  // or the experiment has to call through all of it's groups and call submit event.
  void recordUIChanges() {
    try {
      setNameOn(group);
      setDurationOn(group);
      setCustomRenderingOn(group);
    } catch (Throwable t) {
      Window.alert("Throwable: " + t.getMessage());
    }
  }

  private void setCustomRenderingOn(ExperimentGroup group2) {
    group.setCustomRendering(customRenderingCheckBox.getValue());
    group.setCustomRenderingCode(customRenderingEditor.getText());
  }

  private void setNameOn(ExperimentGroup group2) {
    group2.setName(namePanel.getText());
  }

  protected void setTitleInPanel(String title) {
    namePanel.setText(title);
  }

  private String formatDateAsString(Date date) {
    DateTimeFormat formatter = DateTimeFormat.getFormat(DATE_FORMAT);
    return formatter.format(date);
  }

  private Date getDateFromFormattedString(String dateString) {
    DateTimeFormat formatter = DateTimeFormat.getFormat(DATE_FORMAT);
    return formatter.parse(dateString);
  }

  private void setDurationOn(ExperimentGroup group2) {
    group2.setFixedDuration(durationPanel.isFixedDuration());
    if (group2.getFixedDuration()) {
      group2.setStartDate(durationPanel.getStartDate());
      group2.setEndDate(durationPanel.getEndDate());
    } else {
      group2.setStartDate(null);
      group2.setEndDate(null);
    }

  }

  public ExperimentGroup getExperimentGroup() {
    return group;
  }

}
