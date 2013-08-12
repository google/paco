package com.google.sampling.experiential.client;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.paco.shared.model.InputDAO;
import com.google.sampling.experiential.shared.LoginInfo;

// TODO: split this file.

public class ExperimentCreationValidationTest extends GWTTestCase { 

  private static final String INVALID_EMAIL_STRING = "donti@google.com\nyimingzhang@google.com\nrbe5000@gmail.com";
  
  private static final String NAME_WITH_SPACES = "name With spaces";
  private static final String NAME_WITHOUT_SPACES = "nameWithoutSpaces";
  private static final String NAME_WITHOUT_SPACES_2 = "nameWithoutSpaces2";

  private static final String POS_NUM_STR = "5";
  private static final String POS_NUM_STR_2 = "7";

  private static final int OPEN_TEXT_INPUT_INDEX = 2;

  private ExperimentCreationPanel experimentCreationPanel;

  private LoginInfo loginInfo;
  private ExperimentDAO experiment;

  public String getModuleName() {
    return "com.google.sampling.experiential.PacoEventserver";
  }

  protected void gwtSetUp() {
    loginInfo = CreationTestUtil.createLoginInfo();
    experiment = CreationTestUtil.createValidOngoingExperiment();
  }

  public void testValidExperimentIsSubmittable() {
    createValidExperimentCreationPanel();
    assertTrue(experimentCreationPanel.canSubmit());
  }

  public void testTitleIsMandatoryInDefPanel() {
    createValidExperimentCreationPanel();
    experimentCreationPanel.setTitleInPanel("");
    assertFalse(experimentCreationPanel.canSubmit());
  }

  public void testAdminsMustBeValidInDefPanel() {
    createValidExperimentCreationPanel();
    experimentCreationPanel.setAdminsInPanel(INVALID_EMAIL_STRING);
    assertFalse(experimentCreationPanel.canSubmit());
  }

  public void testPublishedUsersMustBeValidInDefPanel() {
    createValidExperimentCreationPanel();
    experimentCreationPanel.setPublishedUsersInPanel(INVALID_EMAIL_STRING);
    assertFalse(experimentCreationPanel.canSubmit());
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
    MouseOverTextBoxBase firstInputFirstListChoice = 
        firstInputsPanel.responseView.listChoicesPanel.choicePanelsList.get(0).textField;
    MouseOverTextBoxBase thirdInputFirstListChoice = 
        thirdInputsPanel.responseView.listChoicesPanel.choicePanelsList.get(0).textField;

    // Set valid list choice for first list panel. Fire events.
    firstInputFirstListChoice.setValue(NAME_WITH_SPACES, true);
    assertFalse(experimentCreationPanel.canSubmit());

    // Set valid list choice for second list panel. Fire events.
    thirdInputFirstListChoice.setValue(NAME_WITHOUT_SPACES, true);
    assertTrue(experimentCreationPanel.canSubmit());
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
    TextBoxBase firstInputLikertSteps = firstInputsPanel.responseView.stepsText.textBox;
    TextBoxBase thirdInputLikertSteps = thirdInputsPanel.responseView.stepsText.textBox;

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
    MouseOverTextBoxBase firstInputLikertSteps = firstInputsPanel.responseView.stepsText;
    ListBox responseTypeListBox = firstInputsPanel.responseTypeListBox;

    // Invalidate inputs panel (likert error).
    firstInputLikertSteps.setValue(NAME_WITH_SPACES);
    DomEvent.fireNativeEvent(Document.get().createChangeEvent(), firstInputLikertSteps.textBox);
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

  public void testTimeoutPanelDisallowsNonNumericInput() {
    createValidExperimentCreationPanel();
    SignalMechanismChooserPanel ancestor = experimentCreationPanel.signalPanels.get(0).chooserPanels.get(0);
    TimeoutPanel panel = new TimeoutPanel(experiment.getSchedule(), ancestor);
    panel.textBox.setValue(NAME_WITHOUT_SPACES, true);
    assertFalse(experimentCreationPanel.canSubmit());
  }

  private void createValidExperimentCreationPanel() {
    createExperimentCreationPanel(experiment);
  }
  
  private InputDAO createValidNameInput(String type) {
    return CreationTestUtil.createValidNameInput(type);
  }

  private InputDAO createNameInputWithoutVarName(String type) {
    assertTrue(!type.equals(InputDAO.LIST));
    InputDAO input = new InputDAO(null, "", null, "inputPrompt");
    input.setResponseType(type);
    return input;
  }

  private InputDAO createValidListInput() {
    return CreationTestUtil.createValidListInput();
  }

  private InputDAO createListInputWithoutFirstOption() {
    InputDAO input = new InputDAO(null, "inputName", null, "inputPrompt");
    input.setResponseType(InputDAO.LIST);
    return input;
  }

  private void createExperimentCreationPanel(ExperimentDAO experiment) {
    experimentCreationPanel = CreationTestUtil.createExperimentCreationPanel(experiment, loginInfo);
  }
}