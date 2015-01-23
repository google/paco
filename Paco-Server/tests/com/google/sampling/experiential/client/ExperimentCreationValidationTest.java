package com.google.sampling.experiential.client;

import com.google.gwt.junit.client.GWTTestCase;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.paco.shared.model.InputDAO;
import com.google.sampling.experiential.shared.LoginInfo;

public class ExperimentCreationValidationTest extends GWTTestCase {

  private static final String LATER_DAY = "2013/25/07";
  private static final String EARLIER_DAY = "2013/24/07";

  private ExperimentDefinitionPanel experimentDefinitionPanel;

  private LoginInfo loginInfo;
  private ExperimentDAO experiment;

  private static final String VALID_EMAIL_STRING_0 = "donti@google.com, yimingzhang@google.com, rbe5000@gmail.com";
  private static final String VALID_EMAIL_STRING_1 = "donti@google.com,  yimingzhang@google.com,, rbe5000@gmail.com";
  private static final String INVALID_EMAIL_STRING_0 = "donti@google.com\nyimingzhang@google.com\nrbe5000@gmail.com";
  private static final String INVALID_EMAIL_STRING_1 = "donti@google.com, yimingzhang@google.com, rbe5000@gmail.com]";
  private static final String INVALID_EMAIL_STRING_2 = "donti@google,com, yimingzhang@google.com, rbe5000@gmail.com";
  private static final String INVALID_EMAIL_STRING_3 = "donti@google, yimingzhang@google, rbe5000@gmail";
  private static final String INVALID_EMAIL_STRING_4 = "donti@google.com, yimingzhang@google.com, rbe5000@@gmail.com";

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

  public void testTitleIsMandatory() {
    createValidExperimentDefinitionPanel();
    experimentDefinitionPanel.setTitleInPanel("");
    assertFalse(experimentDefinitionPanel.canSubmit());
  }

//  public void testInformedConsentIsMandatory() {
//    createValidExperimentDefinitionPanel();
//    experimentDefinitionPanel.setInformedConsentInPanel("");
//    assertFalse(experimentDefinitionPanel.canSubmit());
//  }

  public void testAdminsMustBeValid() {
    createValidExperimentDefinitionPanel();
    experimentDefinitionPanel.setAdminsInPanel(INVALID_EMAIL_STRING_0);
    assertFalse(experimentDefinitionPanel.canSubmit());
  }

  public void testPublishedUsersMustBeValid() {
    createValidExperimentDefinitionPanel();
    experimentDefinitionPanel.setPublishedUsersInPanel(INVALID_EMAIL_STRING_0);
    assertFalse(experimentDefinitionPanel.canSubmit());
  }

  public void testEmailAddressStringCheck() {
    createValidExperimentDefinitionPanel();
    assertTrue(experimentDefinitionPanel.emailStringIsValid(VALID_EMAIL_STRING_0));
    assertTrue(experimentDefinitionPanel.emailStringIsValid(VALID_EMAIL_STRING_1));
    assertFalse(experimentDefinitionPanel.emailStringIsValid(INVALID_EMAIL_STRING_0));
    assertFalse(experimentDefinitionPanel.emailStringIsValid(INVALID_EMAIL_STRING_1));
    assertFalse(experimentDefinitionPanel.emailStringIsValid(INVALID_EMAIL_STRING_2));
    assertFalse(experimentDefinitionPanel.emailStringIsValid(INVALID_EMAIL_STRING_3));
    assertFalse(experimentDefinitionPanel.emailStringIsValid(INVALID_EMAIL_STRING_4));
  }

  public void testValidFixedDurationIsAccepted() {
    experiment.setFixedDuration(true);
    experiment.setStartDate(EARLIER_DAY);
    experiment.setEndDate(LATER_DAY);
    createExperimentDefinitionPanel(experiment);
    assertTrue(experimentDefinitionPanel.canSubmit());
  }

  public void testInvalidFixedDurationIsNotAccepted() {
    experiment.setFixedDuration(true);
    experiment.setStartDate(LATER_DAY);
    experiment.setEndDate(EARLIER_DAY);
    createExperimentDefinitionPanel(experiment);
    assertFalse(experimentDefinitionPanel.canSubmit());
  }

  public void testDurationPanelAcceptsStartDateBeforeEndDate() {
    createValidExperimentDefinitionPanel();
    setDurationOnDurationPanel(EARLIER_DAY, LATER_DAY);
    assertTrue(experimentDefinitionPanel.startDateIsNotAfterEndDate());
  }

  public void testDurationPanelAcceptsStartDateSameAsEndDate() {
    createValidExperimentDefinitionPanel();
    setDurationOnDurationPanel(EARLIER_DAY, EARLIER_DAY);
    assertTrue(experimentDefinitionPanel.startDateIsNotAfterEndDate());
  }

  public void testDurationPanelDisallowsStartDateAfterEndDate() {
    createValidExperimentDefinitionPanel();
    setDurationOnDurationPanel(LATER_DAY, EARLIER_DAY);
    assertFalse(experimentDefinitionPanel.startDateIsNotAfterEndDate());
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

  public void testLikertInputProperlyValidated() {
    assertTrue(inputsPanelIsValid(createValidNameInput(InputDAO.LIKERT)));
    assertFalse(inputsPanelIsValid(createNameInputWithoutVarName(InputDAO.LIKERT)));
    assertFalse(inputsPanelIsValid(createNameInputWithSpaceyVarName(InputDAO.LIKERT)));
  }

  public void testLikertSmileysInputProperlyValidated() {
    assertTrue(inputsPanelIsValid(createValidNameInput(InputDAO.LIKERT_SMILEYS)));
    assertFalse(inputsPanelIsValid(createNameInputWithoutVarName(InputDAO.LIKERT_SMILEYS)));
    assertFalse(inputsPanelIsValid(createNameInputWithSpaceyVarName(InputDAO.LIKERT_SMILEYS)));
  }

  public void testOpenTextInputProperlyValidated() {
    assertTrue(inputsPanelIsValid(createValidNameInput(InputDAO.OPEN_TEXT)));
    assertFalse(inputsPanelIsValid(createNameInputWithoutVarName(InputDAO.OPEN_TEXT)));
    assertFalse(inputsPanelIsValid(createNameInputWithSpaceyVarName(InputDAO.OPEN_TEXT)));
  }

  public void testNumberInputProperlyValidated() {
    assertTrue(inputsPanelIsValid(createValidNameInput(InputDAO.NUMBER)));
    assertFalse(inputsPanelIsValid(createNameInputWithoutVarName(InputDAO.NUMBER)));
    assertFalse(inputsPanelIsValid(createNameInputWithSpaceyVarName(InputDAO.NUMBER)));
  }

  public void testLocationInputProperlyValidated() {
    assertTrue(inputsPanelIsValid(createValidNameInput(InputDAO.LOCATION)));
    assertFalse(inputsPanelIsValid(createNameInputWithoutVarName(InputDAO.LOCATION)));
    assertFalse(inputsPanelIsValid(createNameInputWithSpaceyVarName(InputDAO.LOCATION)));
  }

  public void testPhotoInputProperlyValidated() {
    assertTrue(inputsPanelIsValid(createValidNameInput(InputDAO.PHOTO)));
    assertFalse(inputsPanelIsValid(createNameInputWithoutVarName(InputDAO.PHOTO)));
    assertFalse(inputsPanelIsValid(createNameInputWithSpaceyVarName(InputDAO.PHOTO)));
  }

  public void testListInputProperlyValidated() {
    assertTrue(inputsPanelIsValid(createValidListInput()));
    assertFalse(inputsPanelIsValid(createListInputWithoutFirstOption()));
    assertFalse(inputsPanelIsValid(createListInputWithSpaceyVarName()));
  }

  private boolean inputsPanelIsValid(InputDAO input) {
    InputsPanel panel = new InputsPanel(null, input);
    return panel.checkListItemsHaveAtLeastOneOptionAndHighlight()
        && panel.checkVarNameFilledWithoutSpacesAndHighlight();
  }

  private LoginInfo createLoginInfo() {
    LoginInfo info = new LoginInfo();
    info.setLoggedIn(true);
    info.setEmailAddress("janeDoe@gmail.com");
    info.setNickname("JaneyD");
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

  private InputDAO createNameInputWithSpaceyVarName(String type) {
    assertTrue(!type.equals(InputDAO.LIST));
    InputDAO input = new InputDAO(null, "varName with space", null, "inputPrompt");
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

  private InputDAO createListInputWithSpaceyVarName() {
    InputDAO input = new InputDAO(null, "varName with space", null, "inputPrompt");
    input.setResponseType(InputDAO.LIST);
    input.setListChoices(new String[]{"option1"});
    return input;
  }

  private void createExperimentDefinitionPanel(ExperimentDAO experiment) {
    experimentDefinitionPanel = new ExperimentDefinitionPanel(experiment,loginInfo, null);
  }

  private void setDurationOnDurationPanel(String startDate, String endDate) {
    DurationView durationPanel = experimentDefinitionPanel.getDurationPanel();
    durationPanel.setFixedDuration(true);
    durationPanel.setStartDate(startDate);
    durationPanel.setEndDate(endDate);
  }
}