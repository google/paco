package com.google.sampling.experiential.client;

import com.google.common.base.Joiner;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.paco.shared.model.FeedbackDAO;
import com.google.paco.shared.model.InputDAO;
import com.google.paco.shared.model.SignalScheduleDAO;
import com.google.paco.shared.model.TriggerDAO;
import com.google.sampling.experiential.shared.LoginInfo;

public class ExperimentCreationTest extends GWTTestCase {
  
  private static final String EMAIL = "bobevans@google.com";
  private static final String NICKNAME = "Bob Evans";
  private static final String TEST_TEXT = "test_text";
  private static final String TEST_EMAIL_1 = "test@test.com" ;
  private static final String TEST_EMAIL_2 = "example@example.org";
  private static final String START_DATE = "2013/07/21";
  private static final String END_DATE = "2013/07/24";
  private static final int RESPONSE_TYPE_INDEX = 3;
  
  private ExperimentDAO experiment;
  private ExperimentDAO savedExperiment;
  private ExperimentDefinitionPanel experimentDefinitionPanel;
  
  @Override
  public String getModuleName() {
    return "com.google.sampling.experiential.PacoEventserver";
  }
  
  protected void gwtSetUp() {
    experiment = getEmptyExperiment();
    experimentDefinitionPanel = new  ExperimentDefinitionPanel(experiment, createLoginInfo(), null);
  }
  
  public void testEmptyExperimentSubmits() {
    submitAndGetSavedExperiment();
    assertEquals(experiment.getTitle(), savedExperiment.getTitle());
    assertEquals(experiment.getDescription(), savedExperiment.getDescription());
    assertEquals(savedExperiment.getCreator(), EMAIL);
    assertEquals(savedExperiment.getAdmins()[0], EMAIL);
    assertEquals(experiment.getInformedConsentForm(), savedExperiment.getInformedConsentForm());
    assertEquals(experiment.getFixedDuration(), savedExperiment.getFixedDuration());
    assertEquals(experiment.getSchedule(), savedExperiment.getSchedule());
    assertEquals(experiment.getInputs(), savedExperiment.getInputs());
    assertEquals(experiment.getFeedback(), savedExperiment.getFeedback());
    assertEquals(experiment.getPublished(), savedExperiment.getPublished());
    assertEquals(experiment.getPublishedUsers(), savedExperiment.getPublishedUsers());
  }
  
  public void testTitleSavedOnExperiment() {
    experimentDefinitionPanel.descriptionPanel.titlePanel.setValue(TEST_TEXT, true);
    submitAndGetSavedExperiment();
    assertEquals(savedExperiment.getTitle(), TEST_TEXT);
  }
  
  public void testDescriptionSavedOnExperiment() {
    experimentDefinitionPanel.descriptionPanel.descriptionPanel.setValue(TEST_TEXT, true);
    submitAndGetSavedExperiment();
    assertEquals(savedExperiment.getDescription(), TEST_TEXT);
  }
  
  public void testAdminsSavedOnExperiment() {
    String[] adminList = new String[]{TEST_EMAIL_1, TEST_EMAIL_2};
    experimentDefinitionPanel.descriptionPanel.adminList.setValue(Joiner.on(",").join(adminList), true);
    submitAndGetSavedExperiment();
    assertEquals(savedExperiment.getAdmins().length, 2);
    assertEquals(savedExperiment.getAdmins()[0], TEST_EMAIL_1);
    assertEquals(savedExperiment.getAdmins()[1], TEST_EMAIL_2);
  }
  
  public void testInformedConsentSavedOnExperiment() {
    experimentDefinitionPanel.descriptionPanel.informedConsentPanel.setValue(TEST_TEXT, true);
    submitAndGetSavedExperiment();
    assertEquals(savedExperiment.getInformedConsentForm(), TEST_TEXT);
  }
  
  public void testFixedDurationSavedOnExperiment() {
   experimentDefinitionPanel.descriptionPanel.durationPanel.setFixedDuration(true);
   submitAndGetSavedExperiment();
   assertTrue(savedExperiment.getFixedDuration());
  }
  
  public void testFixedDurationWithDatesSavedOnExperiment() {
    DurationView durationPanel = experimentDefinitionPanel.descriptionPanel.durationPanel;
    durationPanel.setFixedDuration(true);
    durationPanel.setStartDate(START_DATE);
    durationPanel.setEndDate(END_DATE);
    submitAndGetSavedExperiment();
    assertTrue(savedExperiment.getFixedDuration());
    assertEquals(savedExperiment.getStartDate(), START_DATE);
    assertEquals(savedExperiment.getEndDate(), END_DATE);
  }
  
  public void testOngoingDurationSavedOnExperiment() {
    DurationView durationPanel = experimentDefinitionPanel.descriptionPanel.durationPanel;
    durationPanel.setFixedDuration(true);
    durationPanel.setFixedDuration(false);
    submitAndGetSavedExperiment();
    assertFalse(savedExperiment.getFixedDuration());
  }
  
  public void testScheduledSignalingSavedOnExperiment() {
    SignalMechanismChooserPanel panel = experimentDefinitionPanel.signalPanel;
    panel.signalingMechanismChoices.setSelectedIndex(SignalMechanismChooserPanel.SCHEDULED_SIGNALING_INDEX);
    ChangeEvent.fireNativeEvent(Document.get().createChangeEvent(), panel.signalingMechanismChoices);
    submitAndGetSavedExperiment();
    assertTrue(savedExperiment.getSignalingMechanisms()[0] instanceof SignalScheduleDAO);
  }
  
  public void testTriggeredSignalingSavedOnExperiment() {
    SignalMechanismChooserPanel panel = experimentDefinitionPanel.signalPanel;
    panel.signalingMechanismChoices.setSelectedIndex(SignalMechanismChooserPanel.TRIGGERED_SIGNALING_INDEX);
    ChangeEvent.fireNativeEvent(Document.get().createChangeEvent(), panel.signalingMechanismChoices);
    submitAndGetSavedExperiment();
    assertTrue(savedExperiment.getSignalingMechanisms()[0] instanceof TriggerDAO);
  }
  
  public void testInputsSavedOnExperiment() {
    InputsListPanel panel = experimentDefinitionPanel.inputsListPanel;
    InputsPanel input1 = panel.inputsPanelsList.get(0);
    input1.varNameText.setValue(TEST_TEXT, true);
    input1.inputPromptText.setValue(TEST_TEXT, true);
    panel.addInput(input1);
    InputsPanel input2 = panel.inputsPanelsList.get(1);
    input2.responseTypeListBox.setSelectedIndex(RESPONSE_TYPE_INDEX);
    ChangeEvent.fireNativeEvent(Document.get().createChangeEvent(), input2.responseTypeListBox);
    input2.varNameText.setValue(TEST_TEXT, true);
    input2.inputPromptText.setValue("", true);
    submitAndGetSavedExperiment();
    assertEquals(savedExperiment.getInputs()[0].getName(), TEST_TEXT);
    assertEquals(savedExperiment.getInputs()[0].getText(), TEST_TEXT);
    assertEquals(savedExperiment.getInputs()[1].getName(), TEST_TEXT);
    assertEquals(savedExperiment.getInputs()[1].getText(), "");
    assertEquals(savedExperiment.getInputs()[1].getResponseType(), InputDAO.RESPONSE_TYPES[RESPONSE_TYPE_INDEX]);
  }
  
  public void testEmptyButEnabledCustomFeedbackSavedOnExperiment() {
    CheckBox feedbackCheckBox = experimentDefinitionPanel.publishingPanel.customFeedbackCheckBox;
    feedbackCheckBox.setValue(true, true);
    submitAndGetSavedExperiment();
    assertEquals(savedExperiment.getFeedback().length, 1);
    assertEquals(savedExperiment.getFeedback()[0].getText(), "");
  }
  
  public void testFilledAndEnabledCustomFeedbackSavedOnExperiment() {
    CheckBox feedbackCheckBox = experimentDefinitionPanel.publishingPanel.customFeedbackCheckBox;
    feedbackCheckBox.setValue(true, true);
    TextBoxBase feedbackTextBox = experimentDefinitionPanel.publishingPanel.customFeedbackText;
    feedbackTextBox.setValue(TEST_TEXT, true);
    submitAndGetSavedExperiment();
    assertEquals(savedExperiment.getFeedback().length, 1);
    assertEquals(savedExperiment.getFeedback()[0].getText(), TEST_TEXT);
  }
  
  public void testFilledAndReDisabledCustomFeedbackSavedOnExperiment() {
    CheckBox feedbackCheckBox = experimentDefinitionPanel.publishingPanel.customFeedbackCheckBox;
    feedbackCheckBox.setValue(true, true);
    TextBoxBase feedbackTextBox = experimentDefinitionPanel.publishingPanel.customFeedbackText;
    feedbackTextBox.setValue(TEST_TEXT, true);
    feedbackCheckBox.setValue(false, true);
    submitAndGetSavedExperiment();
    assertEquals(savedExperiment.getFeedback().length, 1);
    assertEquals(savedExperiment.getFeedback()[0].getText(), FeedbackDAO.DEFAULT_FEEDBACK_MSG);
  }
  
  public void testPublishedStatusSavedOnExperiment() {
    CheckBox publishCheckBox = experimentDefinitionPanel.publishingPanel.publishCheckBox;
    publishCheckBox.setValue(true, true);
    submitAndGetSavedExperiment();
    assertTrue(savedExperiment.getPublished());
  }
    
  public void testPublishedUsersSavedOnExperiment() {
    String[] adminList = new String[]{TEST_EMAIL_1, TEST_EMAIL_2};
    experimentDefinitionPanel.publishingPanel.publishedUserList.setValue(Joiner.on(",").join(adminList), true);
    submitAndGetSavedExperiment();
    assertEquals(savedExperiment.getPublishedUsers().length, 2);
    assertEquals(savedExperiment.getPublishedUsers()[0], TEST_EMAIL_1);
    assertEquals(savedExperiment.getPublishedUsers()[1], TEST_EMAIL_2);
  }
  
  private LoginInfo createLoginInfo() {
    LoginInfo info = new LoginInfo();
    info.setLoggedIn(true);
    info.setEmailAddress(EMAIL);
    info.setNickname(NICKNAME);
    info.setWhitelisted(true);
    return info;
  }
  
  private void submitAndGetSavedExperiment() {
    experimentDefinitionPanel.submitEvent();
    savedExperiment = experimentDefinitionPanel.getExperiment();
  }
  
  private ExperimentDAO getEmptyExperiment() {
    return new ExperimentDAO();
  }

}
