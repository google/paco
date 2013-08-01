package com.google.sampling.experiential.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.common.base.Splitter;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.paco.shared.model.InputDAO;
import com.google.paco.shared.model.SignalScheduleDAO;
import com.google.paco.shared.model.SignalingMechanismDAO;
import com.google.paco.shared.model.TriggerDAO;
import com.google.sampling.experiential.shared.LoginInfo;

// TODO: split this file.

public class ExperimentCreationValidationTest extends GWTTestCase {

  private static final String LATEST_DAY = "2013/30/07";
  private static final String LATER_DAY = "2013/25/07";
  private static final String EARLIER_DAY = "2013/24/07";

  private static final String VALID_EMAIL_STRING_0 = "donti@google.com, yimingzhang@google.com, rbe5000@gmail.com";
  private static final String VALID_EMAIL_STRING_1 = "donti@google.com,  yimingzhang@google.com,, rbe5000@gmail.com";
  private static final String VALID_EMAIL_STRING_2 = "donti@google.com, me@yahoo.co.uk";
  private static final String INVALID_EMAIL_STRING_0 = "donti@google.com\nyimingzhang@google.com\nrbe5000@gmail.com";
  private static final String INVALID_EMAIL_STRING_1 = "donti@google.com, yimingzhang@google.com, rbe5000@gmail.com]";
  private static final String INVALID_EMAIL_STRING_2 = "donti@google,com, yimingzhang@google.com, rbe5000@gmail.com";
  private static final String INVALID_EMAIL_STRING_3 = "donti@google, yimingzhang@google, rbe5000@gmail";
  private static final String INVALID_EMAIL_STRING_4 = "donti@google.com, yimingzhang@google.com, rbe5000@@gmail.com";

  private static final String NAME_WITH_SPACES = "name With spaces";
  private static final String NAME_WITHOUT_SPACES = "nameWithoutSpaces";
  private static final String NAME_WITHOUT_SPACES_2 = "nameWithoutSpaces2";
  private static final String NAME_STARTING_WITH_NUMBER = "9apPples";
  private static final String NAME_WITH_INVALID_CHARACTERS = "rstl*&E";

  private static final String POS_NUM_STR = "5";
  private static final String POS_NUM_STR_2 = "7";
  private static final int NEG_NUM = -90;
  private static final int ZERO = 0;
  private static final int VALID_TIMEOUT = 80;

  private static final int OPEN_TEXT_INPUT_INDEX = 2;

  private ExperimentCreationPanel experimentCreationPanel;

  private LoginInfo loginInfo;
  private ExperimentDAO experiment;

  public String getModuleName() {
    return "com.google.sampling.experiential.PacoEventserver";
  }

  protected void gwtSetUp() {
    loginInfo = CreationTestUtil.createLoginInfo();
    createValidOngoingExperiment();
  }

  public void testValidExperimentIsSubmittable() {
    createValidExperimentCreationPanel();
    assertTrue(experimentCreationPanel.canSubmit());
  }

  public void testExperimentModelBlocksInvalidTitle() {
    try {
      experiment.setTitle("");
      assertTrue(false);
    } catch (IllegalArgumentException e) {
      assertTrue(true);
    }
  }

  public void testTitleIsMandatoryInDefPanel() {
    createValidExperimentCreationPanel();
    experimentCreationPanel.setTitleInPanel("");
    assertFalse(experimentCreationPanel.canSubmit());
  }

  public void testExperimentModelCorrectlyValidatesEmailAddresses() {
    assertTrue(experiment.emailListIsValid(tokenize(VALID_EMAIL_STRING_0)));
    assertTrue(experiment.emailListIsValid(tokenize(VALID_EMAIL_STRING_1)));
    assertTrue(experiment.emailListIsValid(tokenize(VALID_EMAIL_STRING_2)));
    assertFalse(experiment.emailListIsValid(tokenize(INVALID_EMAIL_STRING_0)));
    assertFalse(experiment.emailListIsValid(tokenize(INVALID_EMAIL_STRING_1)));
    assertFalse(experiment.emailListIsValid(tokenize(INVALID_EMAIL_STRING_2)));
    assertFalse(experiment.emailListIsValid(tokenize(INVALID_EMAIL_STRING_3)));
    assertFalse(experiment.emailListIsValid(tokenize(INVALID_EMAIL_STRING_4)));
  }

  // Copied from ExperimentDescriptionPanel/ExperimentPublishingPanel.
  // TODO: consolidate.
  private String[] tokenize(String emailString) {
    List<String> emails = new ArrayList<String>();
    Splitter sp = Splitter.on(",").trimResults().omitEmptyStrings();
    for (String admin : sp.split(emailString)) {
      emails.add(admin);
    }
    String[] emailStrArray = new String[emails.size()];
    emailStrArray = emails.toArray(emailStrArray);
    return emailStrArray;
  }

  public void testAdminsMustBeValidInDefPanel() {
    createValidExperimentCreationPanel();
    experimentCreationPanel.setAdminsInPanel(INVALID_EMAIL_STRING_0);
    assertFalse(experimentCreationPanel.canSubmit());
  }

  public void testPublishedUsersMustBeValidInDefPanel() {
    createValidExperimentCreationPanel();
    experimentCreationPanel.setPublishedUsersInPanel(INVALID_EMAIL_STRING_0);
    assertFalse(experimentCreationPanel.canSubmit());
  }

  public void testExperimentModelAcceptsValidFixedDuration() {
    try {
      experiment.setFixedDuration(true);
      experiment.setStartDate(EARLIER_DAY);
      experiment.setEndDate(LATER_DAY);
      assertTrue(true);
    } catch (IllegalArgumentException e) {
      assertTrue(false);
    }
  }

  public void testExperimentModelAcceptsSameDayFixedDuration() {
    try {
      experiment.setFixedDuration(true);
      experiment.setStartDate(EARLIER_DAY);
      experiment.setEndDate(EARLIER_DAY);
      assertTrue(true);
    } catch (IllegalArgumentException e) {
      assertTrue(false);
    }
  }

  public void testExperimentModelDeclinesInvalidFixedDuration() {
    try {
      experiment.setFixedDuration(true);
      experiment.setStartDate(LATER_DAY);
      experiment.setEndDate(EARLIER_DAY);
      assertTrue(false);
    } catch (IllegalArgumentException e) {
      assertTrue(true);
    }
  }

  public void testDurationPanelAcceptsStartDateBeforeEndDate() {
    createValidExperimentCreationPanel();
    setDurationOnDurationPanel(EARLIER_DAY, LATER_DAY);
    assertTrue(experimentCreationPanel.canSubmit());
  }

  public void testDurationPanelAcceptsStartDateSameAsEndDate() {
    createValidExperimentCreationPanel();
    setDurationOnDurationPanel(EARLIER_DAY, EARLIER_DAY);
    assertTrue(experimentCreationPanel.canSubmit());
  }

  public void testDurationPanelDisallowsStartDateAfterEndDate() {
    createValidExperimentCreationPanel();
    setDurationOnDurationPanel(LATER_DAY, EARLIER_DAY);
    assertFalse(experimentCreationPanel.canSubmit());
  }

  public void testDurationPanelProtectsMovingStartDateAfterEndDate() {
    createValidExperimentCreationPanel();
    setDurationOnDurationPanel(EARLIER_DAY, LATER_DAY);
    setDurationPanelStartDate(LATEST_DAY);
    assertTrue(getDateFromString(experiment.getStartDate()).equals(getDateFromString(LATEST_DAY)));
    assertTrue(getDateFromString(experiment.getEndDate()).after(getDateFromString(LATEST_DAY)));
    assertTrue(experimentCreationPanel.canSubmit());
  }

  public void testDurationPanelRejectsMovingEndDateBeforeStartDate() {
    createValidExperimentCreationPanel();
    setDurationOnDurationPanel(LATER_DAY, LATEST_DAY);
    setDurationPanelEndDate(EARLIER_DAY);
    assertTrue(getDateFromString(experiment.getStartDate()).equals(getDateFromString(LATER_DAY)));
    assertTrue(getDateFromString(experiment.getEndDate()).after(getDateFromString(LATER_DAY)));
    assertFalse(experimentCreationPanel.canSubmit());
  }

  public void testDurationPanelAllowsMovingEndDateToStartDate() {
    createValidExperimentCreationPanel();
    setDurationOnDurationPanel(LATER_DAY, LATEST_DAY);
    setDurationPanelEndDate(LATER_DAY);
    assertTrue(getDateFromString(experiment.getEndDate()).equals(getDateFromString(LATER_DAY)));
    assertTrue(experimentCreationPanel.canSubmit());
  }

  public void testDurationPanelErrorFixedByMovingEndDateAfterStartDate() {
    createValidExperimentCreationPanel();
    setDurationOnDurationPanel(LATER_DAY, EARLIER_DAY);
    assertFalse(experimentCreationPanel.canSubmit());
    setDurationPanelEndDate(LATEST_DAY);
    assertTrue(getDateFromString(experiment.getStartDate()).equals(getDateFromString(LATER_DAY)));
    assertTrue(getDateFromString(experiment.getEndDate()).equals(getDateFromString(LATEST_DAY)));
    assertTrue(experimentCreationPanel.canSubmit());
  }

  public void testDurationPanelErrorFixedByMovingEndDateToStartDate() {
    createValidExperimentCreationPanel();
    setDurationOnDurationPanel(LATER_DAY, EARLIER_DAY);
    assertFalse(experimentCreationPanel.canSubmit());
    setDurationPanelEndDate(LATER_DAY);
    assertTrue(getDateFromString(experiment.getStartDate()).equals(getDateFromString(LATER_DAY)));
    assertTrue(getDateFromString(experiment.getEndDate()).equals(getDateFromString(LATER_DAY)));
    assertTrue(experimentCreationPanel.canSubmit());
  }

  public void testDurationPanelErrorFixedByMovingStartDateBeforeEndDate() {
    createValidExperimentCreationPanel();
    setDurationOnDurationPanel(LATEST_DAY, LATER_DAY);
    assertFalse(experimentCreationPanel.canSubmit());
    setDurationPanelStartDate(EARLIER_DAY);
    assertTrue(getDateFromString(experiment.getStartDate()).equals(getDateFromString(EARLIER_DAY)));
    assertTrue(getDateFromString(experiment.getEndDate()).equals(getDateFromString(LATER_DAY)));
    assertTrue(experimentCreationPanel.canSubmit());
  }

  public void testDurationPanelErrorFixedByMovingStartDateToEndDate() {
    createValidExperimentCreationPanel();
    setDurationOnDurationPanel(LATEST_DAY, LATER_DAY);
    assertFalse(experimentCreationPanel.canSubmit());
    setDurationPanelStartDate(LATER_DAY);
    assertTrue(getDateFromString(experiment.getStartDate()).equals(getDateFromString(LATER_DAY)));
    assertTrue(getDateFromString(experiment.getEndDate()).equals(getDateFromString(LATER_DAY)));
    assertTrue(experimentCreationPanel.canSubmit());
  }

  public void testDurationPanelErrorFixedByMakingOngoing() {
    createValidExperimentCreationPanel();
    setDurationOnDurationPanel(LATEST_DAY, LATER_DAY);
    assertFalse(experimentCreationPanel.canSubmit());
    setOngoingDurationOnDurationPanel();
    assertTrue(experimentCreationPanel.canSubmit());
  }


  public void testInvalidInputNotAccepted() {
    experiment.setInputs(new InputDAO[]{createNameInputWithoutVarName(InputDAO.LIKERT)});
    createExperimentCreationPanel(experiment);
    assertFalse(experimentCreationPanel.canSubmit());
  }

  public void testListWithOnlyValidInputsIsAccepted() {
    experiment.setInputs(new InputDAO[] {createValidNameInput(InputDAO.LIKERT_SMILEYS),
                                         createValidNameInput(InputDAO.LOCATION),
                                         createValidListInput()});
    createExperimentCreationPanel(experiment);
    assertTrue(experimentCreationPanel.canSubmit());
  }

  public void testListWithInvalidInputNotAccepted() {
    experiment.setInputs(new InputDAO[] {createValidNameInput(InputDAO.LIKERT_SMILEYS),
                                         createValidNameInput(InputDAO.LOCATION),
                                         createListInputWithoutFirstOption(),
                                         createValidListInput()});
    createExperimentCreationPanel(experiment);
    assertFalse(experimentCreationPanel.canSubmit());
  }


  public void testInputModelAllowsValidName() {
    InputDAO input = new InputDAO();
    try {
      input.setName(NAME_WITHOUT_SPACES);
      assertTrue(true);
    } catch (IllegalArgumentException e) {
      assertTrue(false);
    }
  }

  public void testInputModelBlocksBlankName() {
    InputDAO input = new InputDAO();
    try {
      input.setName("");
      assertTrue(false);
    } catch (IllegalArgumentException e) {
      assertTrue(true);
    }
  }

  public void testInputModelBlocksSpaceyName() {
    InputDAO input = new InputDAO();
    try {
      input.setName(NAME_WITH_SPACES);
      assertTrue(false);
    } catch (IllegalArgumentException e) {
      assertTrue(true);
    }
  }

  public void testInputModelBlocksNameWithInvalidCharacters() {
    InputDAO input = new InputDAO();
    try {
      input.setName(NAME_WITH_INVALID_CHARACTERS);
      assertTrue(false);
    } catch (IllegalArgumentException e) {
      assertTrue(true);
    }
  }

  public void testInputModelBlocksNameStartingWithNumber() {
    InputDAO input = new InputDAO();
    try {
      input.setName(NAME_STARTING_WITH_NUMBER);
      assertTrue(false);
    } catch (IllegalArgumentException e) {
      assertTrue(true);
    }
  }

  public void testSettingBlankVarNameRejectedByDefPanel() {    
    experiment.setInputs(new InputDAO[] {createValidNameInput(InputDAO.LIKERT_SMILEYS),
                                         createValidNameInput(InputDAO.LOCATION),
                                         createValidListInput()});
    createExperimentCreationPanel(experiment);
    InputsPanel firstInputsPanel = 
        experimentCreationPanel.inputsListPanels.get(0).inputsPanelsList.get(0);
    firstInputsPanel.varNameText.setValue("", true);
    assertFalse(experimentCreationPanel.canSubmit());
  }

  public void testFixingOneBlankVarNameDoesNotAllowOtherBlankVarName() {
    experiment.setInputs(new InputDAO[] {createValidNameInput(InputDAO.LIKERT_SMILEYS),
                                         createValidNameInput(InputDAO.LOCATION),
                                         createValidListInput()});
    createExperimentCreationPanel(experiment);

    // Get two inputs panels
    InputsListPanel firstInputsListPanel = experimentCreationPanel.inputsListPanels.get(0);
    InputsPanel firstInputsPanel = firstInputsListPanel.inputsPanelsList.get(0);
    InputsPanel secondInputsPanel = firstInputsListPanel.inputsPanelsList.get(1);

    // Set invalid variable names for both
    firstInputsPanel.varNameText.setValue("", true);
    secondInputsPanel.varNameText.setValue(NAME_WITH_SPACES, true);
    assertFalse(experimentCreationPanel.canSubmit());

    // Set valid variable name for second inputs panel. Fire events.
    secondInputsPanel.varNameText.setValue(NAME_WITHOUT_SPACES, true);
    assertFalse(experimentCreationPanel.canSubmit());

    // Set valid variable name for first inputs panel. Fire events.
    firstInputsPanel.varNameText.setValue(NAME_WITHOUT_SPACES_2, true);
    assertTrue(experimentCreationPanel.canSubmit());
  }

  public void testInputModelDisallowsBlankFirstListItem() {
    InputDAO input = createValidListInput();
    try {
      input.setListChoiceAtIndex(0, "");
      assertTrue(false);
    } catch (IllegalArgumentException e) {
      assertTrue(true);
    }
  } 

  public void testFixingOneBlankListItemDoesNotAllowOtherBlankListItem() {
    experiment.setInputs(new InputDAO[] {createListInputWithoutFirstOption(),
                                         createValidNameInput(InputDAO.LOCATION),
                                         createListInputWithoutFirstOption()});
    createExperimentCreationPanel(experiment);
    assertFalse(experimentCreationPanel.canSubmit());

    // Get list inputs panels first list choice text field
    InputsListPanel firstInputsListPanel = experimentCreationPanel.inputsListPanels.get(0);
    InputsPanel firstInputsPanel = firstInputsListPanel.inputsPanelsList.get(0);
    InputsPanel thirdInputsPanel = firstInputsListPanel.inputsPanelsList.get(2);
    TextBox firstInputFirstListChoice = 
        firstInputsPanel.responseView.listChoicesPanel.choicePanelsList.get(0).textField;
    TextBox thirdInputFirstListChoice = 
        thirdInputsPanel.responseView.listChoicesPanel.choicePanelsList.get(0).textField;

    // Set valid list choice for first list panel. Fire events.
    firstInputFirstListChoice.setValue(NAME_WITH_SPACES, true);
    assertFalse(experimentCreationPanel.canSubmit());

    // Set valid list choice for second list panel. Fire events.
    thirdInputFirstListChoice.setValue(NAME_WITHOUT_SPACES, true);
    assertTrue(experimentCreationPanel.canSubmit());
  }

  public void testInputModelDisallowsInvalidLikertSteps() {
    InputDAO input = createValidListInput();
    try {
      input.setLikertSteps(NEG_NUM);
      assertTrue(false);
    } catch (IllegalArgumentException e) {
      assertTrue(true);
    }
    try {
      input.setLikertSteps(ZERO);
      assertTrue(false);
    } catch (IllegalArgumentException e) {
      assertTrue(true);
    }
  } 

  public void testFixingOneBadLikertItemDoesNotAllowOtherBadLikertItem() {
    experiment.setInputs(new InputDAO[] {createValidNameInput(InputDAO.LIKERT),
                                         createValidNameInput(InputDAO.LOCATION),
                                         createValidNameInput(InputDAO.LIKERT)});
    createExperimentCreationPanel(experiment);

    // Get inputs panels likert steps text fields
    InputsListPanel firstInputsListPanel = experimentCreationPanel.inputsListPanels.get(0);
    InputsPanel firstInputsPanel = firstInputsListPanel.inputsPanelsList.get(0);
    InputsPanel thirdInputsPanel = firstInputsListPanel.inputsPanelsList.get(2);
    TextBox firstInputLikertSteps = firstInputsPanel.responseView.stepsText;
    TextBox thirdInputLikertSteps = thirdInputsPanel.responseView.stepsText;

    // Set invalid likert steps for both. Fire events.
    firstInputLikertSteps.setValue(NAME_WITH_SPACES);
    DomEvent.fireNativeEvent(Document.get().createChangeEvent(), firstInputLikertSteps);
    thirdInputLikertSteps.setValue(NAME_WITHOUT_SPACES);
    DomEvent.fireNativeEvent(Document.get().createChangeEvent(), thirdInputLikertSteps);
    assertFalse(experimentCreationPanel.canSubmit());

    // Set valid likert steps for first input. Fire events.
    firstInputLikertSteps.setValue(POS_NUM_STR);
    DomEvent.fireNativeEvent(Document.get().createChangeEvent(), firstInputLikertSteps);
    assertFalse(experimentCreationPanel.canSubmit());

    // Set valid likert steps for third input. Fire events.
    thirdInputLikertSteps.setValue(POS_NUM_STR_2);
    DomEvent.fireNativeEvent(Document.get().createChangeEvent(), thirdInputLikertSteps);
    assertTrue(experimentCreationPanel.canSubmit());
  }

  public void testSwitchingInputAwayFromLikertInvalidatesErrors() {
    experiment.setInputs(new InputDAO[] {createValidNameInput(InputDAO.LIKERT)});
    createExperimentCreationPanel(experiment);

    // Get inputs panel and some child widgets.
    InputsListPanel firstInputsListPanel = experimentCreationPanel.inputsListPanels.get(0);
    InputsPanel firstInputsPanel = firstInputsListPanel.inputsPanelsList.get(0);
    TextBox firstInputLikertSteps = firstInputsPanel.responseView.stepsText;
    ListBox responseTypeListBox = firstInputsPanel.responseTypeListBox;

    // Invalidate inputs panel (likert error).
    firstInputLikertSteps.setValue(NAME_WITH_SPACES);
    DomEvent.fireNativeEvent(Document.get().createChangeEvent(), firstInputLikertSteps);
    assertFalse(experimentCreationPanel.canSubmit());

    // Switch input type.
    responseTypeListBox.setSelectedIndex(OPEN_TEXT_INPUT_INDEX);
    DomEvent.fireNativeEvent(Document.get().createChangeEvent(), responseTypeListBox);
    assertTrue(experimentCreationPanel.canSubmit());
  }

  public void testSwitchingInputAwayFromListInvalidatesErrors() {
    // Start with invalid input.
    experiment.setInputs(new InputDAO[] {createListInputWithoutFirstOption()});
    createExperimentCreationPanel(experiment);
    assertFalse(experimentCreationPanel.canSubmit());

    // Get inputs panel and some child widgets.
    InputsListPanel firstInputsListPanel = experimentCreationPanel.inputsListPanels.get(0);
    InputsPanel firstInputsPanel = firstInputsListPanel.inputsPanelsList.get(0);
    ListBox responseTypeListBox = firstInputsPanel.responseTypeListBox;

    // Switch input type.
    responseTypeListBox.setSelectedIndex(OPEN_TEXT_INPUT_INDEX);
    DomEvent.fireNativeEvent(Document.get().createChangeEvent(), responseTypeListBox);
    assertTrue(experimentCreationPanel.canSubmit());
  }

  public void testSignalModelAllowsValidTimeout() {
    SignalingMechanismDAO signal = new SignalScheduleDAO();
    SignalingMechanismDAO trigger = new TriggerDAO();
    try {
      signal.setTimeout(VALID_TIMEOUT);
      trigger.setTimeout(VALID_TIMEOUT);
      assertTrue(true);
    } catch (IllegalArgumentException e) {
      assertTrue(false);
    }
  }

  public void testSignalModelBlocksInvalidTimeout() {
    SignalingMechanismDAO signal = new SignalScheduleDAO();
    SignalingMechanismDAO trigger = new TriggerDAO();
    try {
      signal.setTimeout(NEG_NUM);
      assertTrue(false);
    } catch (IllegalArgumentException e) {
      assertTrue(true);
    }
    try {
      trigger.setTimeout(NEG_NUM);
      assertTrue(false);
    } catch (IllegalArgumentException e) {
      assertTrue(true);
    }
  }

  public void testTimeoutPanelDisallowsNonNumericInput() {
    createValidExperimentCreationPanel();
    SignalMechanismChooserPanel ancestor = experimentCreationPanel.signalPanels.get(0);
    TimeoutPanel panel = new TimeoutPanel(experiment.getSchedule(), ancestor);
    panel.textBox.setValue(NAME_WITHOUT_SPACES, true);
    assertFalse(experimentCreationPanel.canSubmit());
  }

  private void createValidExperimentCreationPanel() {
    createExperimentCreationPanel(experiment);
  }

  private void createValidOngoingExperiment() {
    experiment = new ExperimentDAO();
    experiment.setTitle("title");
    experiment.setInformedConsentForm("informed consent");
    experiment.setFixedDuration(false);
    experiment.setInputs(new InputDAO[]{createValidNameInput(InputDAO.LIKERT)});
    experiment.setSchedule(new SignalScheduleDAO());
  }

  private InputDAO createValidNameInput(String type) {
    assertTrue(!type.equals(InputDAO.LIST));
    InputDAO input = new InputDAO(null, "inputName", null, "");
    input.setResponseType(type);
    return input;
  }

  private InputDAO createNameInputWithoutVarName(String type) {
    assertTrue(!type.equals(InputDAO.LIST));
    InputDAO input = new InputDAO(null, "", null, "inputPrompt");
    input.setResponseType(type);
    return input;
  }

  private InputDAO createValidListInput() {
    InputDAO input = new InputDAO(null, "inputName", null, "inputPrompt");
    input.setResponseType(InputDAO.LIST);
    input.setListChoices(new String[]{"option1"});
    return input;
  }

  private InputDAO createListInputWithoutFirstOption() {
    InputDAO input = new InputDAO(null, "inputName", null, "inputPrompt");
    input.setResponseType(InputDAO.LIST);
    return input;
  }

  private void createExperimentCreationPanel(ExperimentDAO experiment) {
    experimentCreationPanel = new ExperimentCreationPanel(experiment,loginInfo, null);
  }

  private void setDurationOnDurationPanel(String startDate, String endDate) {
    setFixedDurationOnDurationPanel();
    setDurationPanelStartDate(startDate);
    setDurationPanelEndDate(endDate);
  }

  private void setFixedDurationOnDurationPanel() {
    DurationView durationPanel = experimentCreationPanel.getDurationPanel();
    durationPanel.ensureValueChangeEventsWillFire();
    durationPanel.setFixedDuration(true);
  }

  private void setDurationPanelStartDate(String startDate) {
    DurationView durationPanel = experimentCreationPanel.getDurationPanel();
    durationPanel.ensureValueChangeEventsWillFire();
    durationPanel.setStartDate(startDate);
  }

  private void setDurationPanelEndDate(String endDate) {
    DurationView durationPanel = experimentCreationPanel.getDurationPanel();
    durationPanel.ensureValueChangeEventsWillFire();
    durationPanel.setEndDate(endDate);
  }

  private void setOngoingDurationOnDurationPanel() {
    DurationView durationPanel = experimentCreationPanel.getDurationPanel();
    durationPanel.ensureValueChangeEventsWillFire();
    durationPanel.setFixedDuration(false);
  }

  private Date getDateFromString(String dateString) {
    DateTimeFormat formatter = DateTimeFormat.getFormat(ExperimentCreationPanel.DATE_FORMAT);
    return formatter.parse(dateString);
  }
}