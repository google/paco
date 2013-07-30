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
import com.google.sampling.experiential.shared.LoginInfo;

public class ExperimentCreationValidationTest extends GWTTestCase {
  
  private static final String LATEST_DAY = "2013/30/07";
  private static final String LATER_DAY = "2013/25/07";
  private static final String EARLIER_DAY = "2013/24/07";
  
  private static final String VALID_EMAIL_STRING_0 = "donti@google.com, yimingzhang@google.com, rbe5000@gmail.com";
  private static final String VALID_EMAIL_STRING_1 = "donti@google.com,  yimingzhang@google.com,, rbe5000@gmail.com";
  private static final String INVALID_EMAIL_STRING_0 = "donti@google.com\nyimingzhang@google.com\nrbe5000@gmail.com";
  private static final String INVALID_EMAIL_STRING_1 = "donti@google.com, yimingzhang@google.com, rbe5000@gmail.com]";
  private static final String INVALID_EMAIL_STRING_2 = "donti@google,com, yimingzhang@google.com, rbe5000@gmail.com";
  private static final String INVALID_EMAIL_STRING_3 = "donti@google, yimingzhang@google, rbe5000@gmail";
  private static final String INVALID_EMAIL_STRING_4 = "donti@google.com, yimingzhang@google.com, rbe5000@@gmail.com";
  
  private static final String NAME_WITH_SPACES = "name with spaces";
  private static final String NAME_WITHOUT_SPACES = "nameWithoutSpaces";
  private static final String NAME_WITHOUT_SPACES_2 = "nameWithoutSpaces2";
  
  private static final String FIVE = "5";
  private static final String SEVEN = "7";
  
  private static final int OPEN_TEXT_INPUT_INDEX = 2;

  private ExperimentCreationPanel experimentDefinitionPanel;
  
  private LoginInfo loginInfo;
  private ExperimentDAO experiment;

  public String getModuleName() {
    return "com.google.sampling.experiential.PacoEventserver";
  }
  
  protected void gwtSetUp() {
    loginInfo = createLoginInfo();
    createValidOngoingExperiment();
  }
  
  public void testValidExperimentIsSubmittable() {
    createValidExperimentDefinitionPanel();
    assertTrue(experimentDefinitionPanel.canSubmit());
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
    createValidExperimentDefinitionPanel();
    experimentDefinitionPanel.setTitleInPanel("");
    assertFalse(experimentDefinitionPanel.canSubmit());
  }
  
  public void testExperimentModelCorrectlyValidatesEmailAddresses() {
    assertTrue(experiment.emailListIsValid(tokenize(VALID_EMAIL_STRING_0)));
    assertTrue(experiment.emailListIsValid(tokenize(VALID_EMAIL_STRING_1)));
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
    createValidExperimentDefinitionPanel();
    experimentDefinitionPanel.setAdminsInPanel(INVALID_EMAIL_STRING_0);
    assertFalse(experimentDefinitionPanel.canSubmit());
  }
  
  public void testPublishedUsersMustBeValidInDefPanel() {
    createValidExperimentDefinitionPanel();
    experimentDefinitionPanel.setPublishedUsersInPanel(INVALID_EMAIL_STRING_0);
    assertFalse(experimentDefinitionPanel.canSubmit());
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
    createValidExperimentDefinitionPanel();
    setDurationOnDurationPanel(EARLIER_DAY, LATER_DAY);
    assertTrue(experimentDefinitionPanel.canSubmit());
  }

  public void testDurationPanelAcceptsStartDateSameAsEndDate() {
    createValidExperimentDefinitionPanel();
    setDurationOnDurationPanel(EARLIER_DAY, EARLIER_DAY);
    assertTrue(experimentDefinitionPanel.canSubmit());
  }
  
  public void testDurationPanelDisallowsStartDateAfterEndDate() {
    createValidExperimentDefinitionPanel();
    setDurationOnDurationPanel(LATER_DAY, EARLIER_DAY);
    assertFalse(experimentDefinitionPanel.canSubmit());
  }
  
  public void testDurationPanelProtectsMovingStartDateAfterEndDate() {
    createValidExperimentDefinitionPanel();
    setDurationOnDurationPanel(EARLIER_DAY, LATER_DAY);
    setDurationPanelStartDate(LATEST_DAY);
    assertTrue(getDateFromString(experiment.getStartDate()).equals(getDateFromString(LATEST_DAY)));
    assertTrue(getDateFromString(experiment.getEndDate()).after(getDateFromString(LATEST_DAY)));
    assertTrue(experimentDefinitionPanel.canSubmit());
  }
  
  public void testDurationPanelRejectsMovingEndDateBeforeStartDate() {
    createValidExperimentDefinitionPanel();
    setDurationOnDurationPanel(LATER_DAY, LATEST_DAY);
    setDurationPanelEndDate(EARLIER_DAY);
    assertTrue(getDateFromString(experiment.getStartDate()).equals(getDateFromString(LATER_DAY)));
    assertTrue(getDateFromString(experiment.getEndDate()).after(getDateFromString(LATER_DAY)));
    assertFalse(experimentDefinitionPanel.canSubmit());
  }
  
  public void testDurationPanelAllowsMovingEndDateToStartDate() {
    createValidExperimentDefinitionPanel();
    setDurationOnDurationPanel(LATER_DAY, LATEST_DAY);
    setDurationPanelEndDate(LATER_DAY);
    assertTrue(getDateFromString(experiment.getEndDate()).equals(getDateFromString(LATER_DAY)));
    assertTrue(experimentDefinitionPanel.canSubmit());
  }
  
  public void testDurationPanelErrorFixedByMovingEndDateAfterStartDate() {
    createValidExperimentDefinitionPanel();
    setDurationOnDurationPanel(LATER_DAY, EARLIER_DAY);
    assertFalse(experimentDefinitionPanel.canSubmit());
    setDurationPanelEndDate(LATEST_DAY);
    assertTrue(getDateFromString(experiment.getStartDate()).equals(getDateFromString(LATER_DAY)));
    assertTrue(getDateFromString(experiment.getEndDate()).equals(getDateFromString(LATEST_DAY)));
    assertTrue(experimentDefinitionPanel.canSubmit());
  }
  
  public void testDurationPanelErrorFixedByMovingEndDateToStartDate() {
    createValidExperimentDefinitionPanel();
    setDurationOnDurationPanel(LATER_DAY, EARLIER_DAY);
    assertFalse(experimentDefinitionPanel.canSubmit());
    setDurationPanelEndDate(LATER_DAY);
    assertTrue(getDateFromString(experiment.getStartDate()).equals(getDateFromString(LATER_DAY)));
    assertTrue(getDateFromString(experiment.getEndDate()).equals(getDateFromString(LATER_DAY)));
    assertTrue(experimentDefinitionPanel.canSubmit());
  }
  
  public void testDurationPanelErrorFixedByMovingStartDateBeforeEndDate() {
    createValidExperimentDefinitionPanel();
    setDurationOnDurationPanel(LATEST_DAY, LATER_DAY);
    assertFalse(experimentDefinitionPanel.canSubmit());
    setDurationPanelStartDate(EARLIER_DAY);
    assertTrue(getDateFromString(experiment.getStartDate()).equals(getDateFromString(EARLIER_DAY)));
    assertTrue(getDateFromString(experiment.getEndDate()).equals(getDateFromString(LATER_DAY)));
    assertTrue(experimentDefinitionPanel.canSubmit());
  }
  
  public void testDurationPanelErrorFixedByMovingStartDateToEndDate() {
    createValidExperimentDefinitionPanel();
    setDurationOnDurationPanel(LATEST_DAY, LATER_DAY);
    assertFalse(experimentDefinitionPanel.canSubmit());
    setDurationPanelStartDate(LATER_DAY);
    assertTrue(getDateFromString(experiment.getStartDate()).equals(getDateFromString(LATER_DAY)));
    assertTrue(getDateFromString(experiment.getEndDate()).equals(getDateFromString(LATER_DAY)));
    assertTrue(experimentDefinitionPanel.canSubmit());
  }
  
  public void testDurationPanelErrorFixedByMakingOngoing() {
    createValidExperimentDefinitionPanel();
    setDurationOnDurationPanel(LATEST_DAY, LATER_DAY);
    assertFalse(experimentDefinitionPanel.canSubmit());
    setOngoingDurationOnDurationPanel();
    assertTrue(experimentDefinitionPanel.canSubmit());
  }
  
  
  public void testInvalidInputNotAccepted() {
    experiment.setInputs(new InputDAO[]{createNameInputWithoutVarName(InputDAO.LIKERT)});
    createExperimentDefinitionPanel(experiment);
    assertFalse(experimentDefinitionPanel.canSubmit());
  }
  
  public void testListWithOnlyValidInputsIsAccepted() {
    experiment.setInputs(new InputDAO[] {createValidNameInput(InputDAO.LIKERT_SMILEYS),
                                         createValidNameInput(InputDAO.LOCATION),
                                         createValidListInput()});
    createExperimentDefinitionPanel(experiment);
    assertTrue(experimentDefinitionPanel.canSubmit());
  }
  
  public void testListWithInvalidInputNotAccepted() {
    experiment.setInputs(new InputDAO[] {createValidNameInput(InputDAO.LIKERT_SMILEYS),
                                         createValidNameInput(InputDAO.LOCATION),
                                         createListInputWithoutFirstOption(),
                                         createValidListInput()});
    createExperimentDefinitionPanel(experiment);
    assertFalse(experimentDefinitionPanel.canSubmit());
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
  
  public void testSettingBlankVarNameRejectedByDefPanel() {    
    experiment.setInputs(new InputDAO[] {createValidNameInput(InputDAO.LIKERT_SMILEYS),
                                         createValidNameInput(InputDAO.LOCATION),
                                         createValidListInput()});
    createExperimentDefinitionPanel(experiment);
    InputsPanel firstInputsPanel = 
        experimentDefinitionPanel.inputsListPanels.get(0).inputsPanelsList.get(0);
    firstInputsPanel.varNameText.setValue("", true);
    assertFalse(experimentDefinitionPanel.canSubmit());
  }
  
  public void testFixingOneBlankVarNameDoesNotAllowOtherBlankVarName() {
    experiment.setInputs(new InputDAO[] {createValidNameInput(InputDAO.LIKERT_SMILEYS),
                                         createValidNameInput(InputDAO.LOCATION),
                                         createValidListInput()});
    createExperimentDefinitionPanel(experiment);
    
    // Get two inputs panels
    InputsListPanel firstInputsListPanel = experimentDefinitionPanel.inputsListPanels.get(0);
    InputsPanel firstInputsPanel = firstInputsListPanel.inputsPanelsList.get(0);
    InputsPanel secondInputsPanel = firstInputsListPanel.inputsPanelsList.get(1);
    
    // Set invalid variable names for both
    firstInputsPanel.varNameText.setValue("", true);
    secondInputsPanel.varNameText.setValue(NAME_WITH_SPACES, true);
    assertFalse(experimentDefinitionPanel.canSubmit());
    
    // Set valid variable name for second inputs panel. Fire events.
    secondInputsPanel.varNameText.setValue(NAME_WITHOUT_SPACES, true);
    assertFalse(experimentDefinitionPanel.canSubmit());
    
    // Set valid variable name for first inputs panel. Fire events.
    firstInputsPanel.varNameText.setValue(NAME_WITHOUT_SPACES_2, true);
    assertTrue(experimentDefinitionPanel.canSubmit());
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
    createExperimentDefinitionPanel(experiment);
    assertFalse(experimentDefinitionPanel.canSubmit());
    
    // Get list inputs panels first list choice text field
    InputsListPanel firstInputsListPanel = experimentDefinitionPanel.inputsListPanels.get(0);
    InputsPanel firstInputsPanel = firstInputsListPanel.inputsPanelsList.get(0);
    InputsPanel thirdInputsPanel = firstInputsListPanel.inputsPanelsList.get(2);
    TextBox firstInputFirstListChoice = 
        firstInputsPanel.responseView.listChoicesPanel.choicePanelsList.get(0).textField;
    TextBox thirdInputFirstListChoice = 
        thirdInputsPanel.responseView.listChoicesPanel.choicePanelsList.get(0).textField;
    
    // Set valid list choice for first list panel. Fire events.
    firstInputFirstListChoice.setValue(NAME_WITH_SPACES, true);
    assertFalse(experimentDefinitionPanel.canSubmit());
    
    // Set valid list choice for second list panel. Fire events.
    thirdInputFirstListChoice.setValue(NAME_WITHOUT_SPACES, true);
    assertTrue(experimentDefinitionPanel.canSubmit());
  }
  
  public void testFixingOneBadLikertItemDoesNotAllowOtherBadLikertItem() {
    experiment.setInputs(new InputDAO[] {createValidNameInput(InputDAO.LIKERT),
                                         createValidNameInput(InputDAO.LOCATION),
                                         createValidNameInput(InputDAO.LIKERT)});
    createExperimentDefinitionPanel(experiment);
    
    // Get inputs panels likert steps text fields
    InputsListPanel firstInputsListPanel = experimentDefinitionPanel.inputsListPanels.get(0);
    InputsPanel firstInputsPanel = firstInputsListPanel.inputsPanelsList.get(0);
    InputsPanel thirdInputsPanel = firstInputsListPanel.inputsPanelsList.get(2);
    TextBox firstInputLikertSteps = firstInputsPanel.responseView.stepsText;
    TextBox thirdInputLikertSteps = thirdInputsPanel.responseView.stepsText;
    
    // Set invalid likert steps for both. Fire events.
    firstInputLikertSteps.setValue(NAME_WITH_SPACES);
    DomEvent.fireNativeEvent(Document.get().createChangeEvent(), firstInputLikertSteps);
    thirdInputLikertSteps.setValue(NAME_WITHOUT_SPACES);
    DomEvent.fireNativeEvent(Document.get().createChangeEvent(), thirdInputLikertSteps);
    assertFalse(experimentDefinitionPanel.canSubmit());
    
    // Set valid likert steps for first input. Fire events.
    firstInputLikertSteps.setValue(FIVE);
    DomEvent.fireNativeEvent(Document.get().createChangeEvent(), firstInputLikertSteps);
    assertFalse(experimentDefinitionPanel.canSubmit());
    
    // Set valid likert steps for third input. Fire events.
    thirdInputLikertSteps.setValue(SEVEN);
    DomEvent.fireNativeEvent(Document.get().createChangeEvent(), thirdInputLikertSteps);
    assertTrue(experimentDefinitionPanel.canSubmit());
  }
  
  public void testSwitchingInputAwayFromLikertInvalidatesErrors() {
    experiment.setInputs(new InputDAO[] {createValidNameInput(InputDAO.LIKERT)});
    createExperimentDefinitionPanel(experiment);
    
    // Get inputs panel and some child widgets.
    InputsListPanel firstInputsListPanel = experimentDefinitionPanel.inputsListPanels.get(0);
    InputsPanel firstInputsPanel = firstInputsListPanel.inputsPanelsList.get(0);
    TextBox firstInputLikertSteps = firstInputsPanel.responseView.stepsText;
    ListBox responseTypeListBox = firstInputsPanel.responseTypeListBox;
    
    // Invalidate inputs panel (likert error).
    firstInputLikertSteps.setValue(NAME_WITH_SPACES);
    DomEvent.fireNativeEvent(Document.get().createChangeEvent(), firstInputLikertSteps);
    assertFalse(experimentDefinitionPanel.canSubmit());
    
    // Switch input type.
    responseTypeListBox.setSelectedIndex(OPEN_TEXT_INPUT_INDEX);
    DomEvent.fireNativeEvent(Document.get().createChangeEvent(), responseTypeListBox);
    assertTrue(experimentDefinitionPanel.canSubmit());
  }
  
  public void testSwitchingInputAwayFromListInvalidatesErrors() {
    // Start with invalid input.
    experiment.setInputs(new InputDAO[] {createListInputWithoutFirstOption()});
    createExperimentDefinitionPanel(experiment);
    assertFalse(experimentDefinitionPanel.canSubmit());
    
    // Get inputs panel and some child widgets.
    InputsListPanel firstInputsListPanel = experimentDefinitionPanel.inputsListPanels.get(0);
    InputsPanel firstInputsPanel = firstInputsListPanel.inputsPanelsList.get(0);
    ListBox responseTypeListBox = firstInputsPanel.responseTypeListBox;

    // Switch input type.
    responseTypeListBox.setSelectedIndex(OPEN_TEXT_INPUT_INDEX);
    DomEvent.fireNativeEvent(Document.get().createChangeEvent(), responseTypeListBox);
    assertTrue(experimentDefinitionPanel.canSubmit());
  }
  
  private LoginInfo createLoginInfo() {
    LoginInfo info = new LoginInfo();
    info.setLoggedIn(true);
    info.setEmailAddress("janeDoe@gmail.com");
    info.setNickname("JaneyD");
    info.setWhitelisted(true);
    return info;
  }
  
  private void createValidExperimentDefinitionPanel() {
    createExperimentDefinitionPanel(experiment);
  }
  
  private void createValidOngoingExperiment() {
    experiment = new ExperimentDAO();
    experiment.setTitle("title");
    experiment.setInformedConsentForm("informed consent");
    experiment.setFixedDuration(false);
    experiment.setInputs(new InputDAO[]{createValidNameInput(InputDAO.LIKERT)});
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
  
  private void createExperimentDefinitionPanel(ExperimentDAO experiment) {
    experimentDefinitionPanel = new ExperimentCreationPanel(experiment,loginInfo, null);
  }
  
  private void setDurationOnDurationPanel(String startDate, String endDate) {
    setFixedDurationOnDurationPanel();
    setDurationPanelStartDate(startDate);
    setDurationPanelEndDate(endDate);
  }

  private void setFixedDurationOnDurationPanel() {
    DurationView durationPanel = experimentDefinitionPanel.getDurationPanel();
    durationPanel.ensureValueChangeEventsWillFire();
    durationPanel.setFixedDuration(true);
  }
  
  private void setDurationPanelStartDate(String startDate) {
    DurationView durationPanel = experimentDefinitionPanel.getDurationPanel();
    durationPanel.ensureValueChangeEventsWillFire();
    durationPanel.setStartDate(startDate);
  }
  
  private void setDurationPanelEndDate(String endDate) {
    DurationView durationPanel = experimentDefinitionPanel.getDurationPanel();
    durationPanel.ensureValueChangeEventsWillFire();
    durationPanel.setEndDate(endDate);
  }
  
  private void setOngoingDurationOnDurationPanel() {
    DurationView durationPanel = experimentDefinitionPanel.getDurationPanel();
    durationPanel.ensureValueChangeEventsWillFire();
    durationPanel.setFixedDuration(false);
  }
  
  private Date getDateFromString(String dateString) {
      DateTimeFormat formatter = DateTimeFormat.getFormat(ExperimentCreationPanel.DATE_FORMAT);
      return formatter.parse(dateString);
  }
}