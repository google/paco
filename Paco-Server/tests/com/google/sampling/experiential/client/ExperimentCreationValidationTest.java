package com.google.sampling.experiential.client;

import java.util.ArrayList;

import com.google.common.collect.Lists;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.sampling.experiential.shared.LoginInfo;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.Input2;

public class ExperimentCreationValidationTest extends GWTTestCase {

  private static final String LATER_DAY = "2013/25/07";
  private static final String EARLIER_DAY = "2013/24/07";

  private ExperimentGroupPanel experimentGroupPanel;

  private LoginInfo loginInfo;
  private ExperimentGroup experimentGroup;
  private ExperimentDAO experiment;
  private ExperimentDefinitionPanel experimentDefinitionPanel;

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
    createValidOngoingExperimentGroup();
  }

  public void testValidExperimentIsSubmittable() {
    createValidExperimentGroupPanel();
    assertTrue(experimentGroupPanel.canSubmit());
  }

  public void testTitleIsMandatory() {
    createValidOngoingExperiment();
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
    createValidOngoingExperiment();
    createValidExperimentDefinitionPanel();
    experimentDefinitionPanel.setAdminsInPanel(INVALID_EMAIL_STRING_0);
    assertFalse(experimentGroupPanel.canSubmit());
  }

  private void createValidExperimentDefinitionPanel() {
    createValidOngoingExperiment();
    experimentDefinitionPanel = new ExperimentDefinitionPanel(experiment, loginInfo, null);
  }

  public void testPublishedUsersMustBeValid() {
    createValidExperimentGroupPanel();
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
    experimentGroup.setFixedDuration(true);
    experimentGroup.setStartDate(EARLIER_DAY);
    experimentGroup.setEndDate(LATER_DAY);
    createExperimentDefinitionPanel(experimentGroup);
    assertTrue(experimentGroupPanel.canSubmit());
  }

  public void testInvalidFixedDurationIsNotAccepted() {
    experimentGroup.setFixedDuration(true);
    experimentGroup.setStartDate(LATER_DAY);
    experimentGroup.setEndDate(EARLIER_DAY);
    createExperimentDefinitionPanel(experimentGroup);
    assertFalse(experimentGroupPanel.canSubmit());
  }

  public void testDurationPanelAcceptsStartDateBeforeEndDate() {
    createValidExperimentGroupPanel();
    setDurationOnDurationPanel(EARLIER_DAY, LATER_DAY);
    assertTrue(experimentGroupPanel.startDateIsNotAfterEndDate());
  }

  public void testDurationPanelAcceptsStartDateSameAsEndDate() {
    createValidExperimentGroupPanel();
    setDurationOnDurationPanel(EARLIER_DAY, EARLIER_DAY);
    assertTrue(experimentGroupPanel.startDateIsNotAfterEndDate());
  }

  public void testDurationPanelDisallowsStartDateAfterEndDate() {
    createValidExperimentGroupPanel();
    setDurationOnDurationPanel(LATER_DAY, EARLIER_DAY);
    assertFalse(experimentGroupPanel.startDateIsNotAfterEndDate());
  }

  public void testInvalidInputNotAccepted() {
    ArrayList inputDAOs = new java.util.ArrayList();
    inputDAOs.add(createNameInputWithoutVarName(Input2.LIKERT));
    experimentGroup.setInputs(inputDAOs);
    createExperimentDefinitionPanel(experimentGroup);
    assertFalse(experimentGroupPanel.canSubmit());
  }

  public void testListWithOnlyValidInputsIsAccepted() {
    ArrayList inputDAOs = new java.util.ArrayList();
    inputDAOs.add(createValidNameInput(Input2.LIKERT_SMILEYS));
    inputDAOs.add(createValidNameInput(Input2.LOCATION));
    inputDAOs.add(createValidListInput());
    experimentGroup.setInputs(inputDAOs);
    createExperimentDefinitionPanel(experimentGroup);
    assertTrue(experimentGroupPanel.canSubmit());
  }

  public void testListWithInvalidInputNotAccepted() {
    ArrayList inputDAOs = new java.util.ArrayList();
    inputDAOs.add(createValidNameInput(Input2.LIKERT_SMILEYS));
    inputDAOs.add(createValidNameInput(Input2.LOCATION));
    inputDAOs.add(createListInputWithoutFirstOption());
    inputDAOs.add(createValidListInput());
    experimentGroup.setInputs(inputDAOs);
    createExperimentDefinitionPanel(experimentGroup);
    assertFalse(experimentGroupPanel.canSubmit());
  }

  public void testLikertInputProperlyValidated() {
    assertTrue(inputsPanelIsValid(createValidNameInput(Input2.LIKERT)));
    assertFalse(inputsPanelIsValid(createNameInputWithoutVarName(Input2.LIKERT)));
    assertFalse(inputsPanelIsValid(createNameInputWithSpaceyVarName(Input2.LIKERT)));
  }

  public void testLikertSmileysInputProperlyValidated() {
    assertTrue(inputsPanelIsValid(createValidNameInput(Input2.LIKERT_SMILEYS)));
    assertFalse(inputsPanelIsValid(createNameInputWithoutVarName(Input2.LIKERT_SMILEYS)));
    assertFalse(inputsPanelIsValid(createNameInputWithSpaceyVarName(Input2.LIKERT_SMILEYS)));
  }

  public void testOpenTextInputProperlyValidated() {
    assertTrue(inputsPanelIsValid(createValidNameInput(Input2.OPEN_TEXT)));
    assertFalse(inputsPanelIsValid(createNameInputWithoutVarName(Input2.OPEN_TEXT)));
    assertFalse(inputsPanelIsValid(createNameInputWithSpaceyVarName(Input2.OPEN_TEXT)));
  }

  public void testNumberInputProperlyValidated() {
    assertTrue(inputsPanelIsValid(createValidNameInput(Input2.NUMBER)));
    assertFalse(inputsPanelIsValid(createNameInputWithoutVarName(Input2.NUMBER)));
    assertFalse(inputsPanelIsValid(createNameInputWithSpaceyVarName(Input2.NUMBER)));
  }

  public void testLocationInputProperlyValidated() {
    assertTrue(inputsPanelIsValid(createValidNameInput(Input2.LOCATION)));
    assertFalse(inputsPanelIsValid(createNameInputWithoutVarName(Input2.LOCATION)));
    assertFalse(inputsPanelIsValid(createNameInputWithSpaceyVarName(Input2.LOCATION)));
  }

  public void testPhotoInputProperlyValidated() {
    assertTrue(inputsPanelIsValid(createValidNameInput(Input2.PHOTO)));
    assertFalse(inputsPanelIsValid(createNameInputWithoutVarName(Input2.PHOTO)));
    assertFalse(inputsPanelIsValid(createNameInputWithSpaceyVarName(Input2.PHOTO)));
  }

  public void testListInputProperlyValidated() {
    assertTrue(inputsPanelIsValid(createValidListInput()));
    assertFalse(inputsPanelIsValid(createListInputWithoutFirstOption()));
    assertFalse(inputsPanelIsValid(createListInputWithSpaceyVarName()));
  }

  private boolean inputsPanelIsValid(Input2 input) {
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

  private void createValidExperimentGroupPanel() {
    createExperimentDefinitionPanel(experimentGroup);
  }


  private void createValidOngoingExperiment() {
    experiment = new ExperimentDAO();
    experiment.setTitle("title");
  }
  private void createValidOngoingExperimentGroup() {
    experimentGroup = new ExperimentGroup();
    experimentGroup.setName("title");
    experimentGroup.setFixedDuration(false);
    ArrayList inputDAOs = new java.util.ArrayList();
    inputDAOs.add(createValidNameInput(Input2.LIKERT));
    experimentGroup.setInputs(inputDAOs);
  }

  private Input2 createValidNameInput(String type) {
    assertTrue(!type.equals(Input2.LIST));
    Input2 input = new Input2("inputName", "");
    input.setResponseType(type);
    return input;
  }

  private Input2 createNameInputWithoutVarName(String type) {
    assertTrue(!type.equals(Input2.LIST));
    Input2 input = new Input2("", "inputPrompt");
    input.setResponseType(type);
    return input;
  }

  private Input2 createNameInputWithSpaceyVarName(String type) {
    assertTrue(!type.equals(Input2.LIST));
    Input2 input = new Input2("varName with space", "inputPrompt");
    input.setResponseType(type);
    return input;
  }

  private Input2 createValidListInput() {
    Input2 input = new Input2("inputName", "inputPrompt");
    input.setResponseType(Input2.LIST);
    input.setListChoices(Lists.newArrayList("option1"));
    return input;
  }

  private Input2 createListInputWithoutFirstOption() {
    Input2 input = new Input2("inputName", "inputPrompt");
    input.setResponseType(Input2.LIST);
    return input;
  }

  private Input2 createListInputWithSpaceyVarName() {
    Input2 input = new Input2("varName with space", "inputPrompt");
    input.setResponseType(Input2.LIST);
    input.setListChoices(Lists.newArrayList("option1"));
    return input;
  }

  private void createExperimentDefinitionPanel(ExperimentGroup experiment2) {
    experimentGroupPanel = new ExperimentGroupPanel(new ExperimentGroupListPanel(experiment), experiment2);
  }

  private void setDurationOnDurationPanel(String startDate, String endDate) {
    DurationView durationPanel = experimentGroupPanel.getDurationPanel();
    durationPanel.setFixedDuration(true);
    durationPanel.setStartDate(startDate);
    durationPanel.setEndDate(endDate);
  }
}