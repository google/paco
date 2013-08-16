package com.google.sampling.experiential.client;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.ui.ListBox;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.paco.shared.model.InputDAO;

public class ExperimentCreationConditionalsTest extends GWTTestCase {
  
  private static final int VALID_PREDICATE = 3;
  private static final int VALID_PREDICATE_2 = 5;
  private static final int EQUALS = 0;
  private static final int GREATER_THAN = 2;
  public static final String VALID_NAME_PREFIX = "q";
  public static final String VALID_NAME_0 = "q0";
  public static final String VALID_NAME_1 = "q1";
  public static final String VALID_NAME_2 = "q2";
  public static final String VALID_NAME_EXTRA = "unique";
  
  public static final String VALID_SIMPLE_CONDITIONAL = VALID_NAME_0 + " > 3";
  public static final String VALID_SIMPLE_CONDITIONAL_PT2 = "&& " + VALID_NAME_1 + " == 5";
  public static final String VALID_SIMPLE_CONDITIONAL_PT3 = "|| " + VALID_NAME_2 + " != 1";
  public static final String VALID_SPACEY_CONDITIONAL = "  " + VALID_NAME_0 + " > 3  ";
  public static final String VALID_COMPOUND_CONDITIONAL = 
      VALID_NAME_0 + " > 3 && "+ VALID_NAME_1 + " == 5";
  public static final String VALID_PAREN_CONDITIONAL = VALID_NAME_0 + ">3 && (" +
      VALID_NAME_0 + "!=4 || " + VALID_NAME_1 + "==1)";
  public static final String INVALID_COMP_CONDITIONAL = VALID_NAME_0 + " ? 3";
  public static final String INVALID_OP_SYNTAX_CONDITIONAL = VALID_NAME_0 + " > 3 &&& " + 
      VALID_NAME_1 + " == 5";
  public static final String INVALID_VARNAME_CONDITIONAL = "1q > 3";
  public static final String INVALID_UNBALANCED_PARENS_CONDITIONAL = "((q1 > 3) && q2 == 5";
  public static final String INVALID_LIKERT_OUT_OF_BOUNDS_CONDITIONAL = VALID_NAME_2 + " > 19";
  private static final String VALID_SIMPLE_CONDITIONAL_TAIL = " > 3";
  
  public static final Integer NON_DEFAULT_LIKERT_STEPS = 8;
  public static final String LIST_CHOICE_1 = "hello";
  public static final String LIST_CHOICE_2 = "goodbye";
  
  private ConditionalExpressionsPanel thirdExpressionsPanel;
  private ExperimentCreationPanel experimentCreationPanel;
  private ExperimentDAO experiment;
  private InputDAO thirdInput;

  @Override
  public String getModuleName() {
    return "com.google.sampling.experiential.PacoEventserver";
  }
  
  public void gwtSetUp() {
    experiment = createExperimentWithNumberLikertLikertsmileys();
    experimentCreationPanel = CreationTestUtil.createExperimentCreationPanel(experiment);
    thirdInput = experiment.getInputs()[2];
    thirdExpressionsPanel = 
        experimentCreationPanel.inputsListPanels.get(0).inputsPanelsList.get(2).conditionalPanel;
  }
  
  public void testConditionalRegex() {
    assertTrue(thirdExpressionsPanel.expressionIsValid(VALID_SIMPLE_CONDITIONAL));
    assertTrue(thirdExpressionsPanel.expressionIsValid(VALID_SPACEY_CONDITIONAL));
    assertTrue(thirdExpressionsPanel.expressionIsValid(VALID_COMPOUND_CONDITIONAL));
    assertTrue(thirdExpressionsPanel.expressionIsValid(VALID_PAREN_CONDITIONAL));
    assertFalse(thirdExpressionsPanel.expressionIsValid(INVALID_COMP_CONDITIONAL));
    assertFalse(thirdExpressionsPanel.expressionIsValid(INVALID_OP_SYNTAX_CONDITIONAL));
    assertFalse(thirdExpressionsPanel.expressionIsValid(INVALID_VARNAME_CONDITIONAL));
    assertFalse(thirdExpressionsPanel.expressionIsValid(INVALID_UNBALANCED_PARENS_CONDITIONAL));
  }
  
  public void testSimpleTextUpdatesModelAndMenu() {
    assertTextUpdatesModelAndMenu(VALID_SIMPLE_CONDITIONAL);
  }
  
  public void testSpaceyTextUpdatesModelAndMenu() {
    assertTextUpdatesModelAndMenu(VALID_SPACEY_CONDITIONAL);
  }
  
  public void testCompoundTextUpdatesModelAndMenu() {
    assertTextUpdatesModelAndMenu(VALID_COMPOUND_CONDITIONAL);
  }
  
  public void testParenTextUpdatesModelAndMenu() {
    assertTextUpdatesModelAndMenu(VALID_PAREN_CONDITIONAL);
  }
  
  public void testSimpleMenuUpdatesModelAndText() {
    ConditionalExpressionPanel panel = thirdExpressionsPanel.conditionPanels.get(0);
    setMenuAttributesWithEventsFired(panel, VALID_NAME_0, GREATER_THAN, VALID_PREDICATE);
    String menuExpression = 
        replaceWhitespace(VALID_NAME_0 + getOpStr(GREATER_THAN) + VALID_PREDICATE);
    assertEquals(menuExpression, getTrimmedMenuExpression());
    assertEquals(menuExpression, getTrimmedInputExpression());
    assertEquals(menuExpression, getTrimmedTextDisplayExpression());
  }
  
  public void testCompoundMenuUpdatesModelAndText() {
    ConditionalExpressionPanel firstPanel = thirdExpressionsPanel.conditionPanels.get(0);
    setMenuAttributesWithEventsFired(firstPanel, VALID_NAME_0, GREATER_THAN, VALID_PREDICATE);
    String op = addNextWithOp(firstPanel, ConditionalExpressionsPanel.AND_OP);
    ConditionalExpressionPanel secondPanel = thirdExpressionsPanel.conditionPanels.get(1);
    setMenuAttributesWithEventsFired(secondPanel, VALID_NAME_1, EQUALS, VALID_PREDICATE_2);
    String menuExpression = 
        replaceWhitespace(VALID_NAME_0 + getOpStr(GREATER_THAN) + VALID_PREDICATE + op 
                          + VALID_NAME_1 + getOpStr(EQUALS) + VALID_PREDICATE_2);
    assertEquals(menuExpression, getTrimmedMenuExpression());
    assertEquals(menuExpression, getTrimmedInputExpression());
    assertEquals(menuExpression, getTrimmedTextDisplayExpression());
  }
  
  public void testParenMenuUpdatesModelAndText() {
    ConditionalExpressionPanel panel = thirdExpressionsPanel.conditionPanels.get(0);
    setMenuAttributesWithEventsFired(panel, VALID_NAME_0, GREATER_THAN, VALID_PREDICATE);
    panel.increaseLeftParensWithUpdate();
    panel.increaseRightParensWithUpdate();
    String menuExpression = 
        replaceWhitespace("(" + VALID_NAME_0 + getOpStr(GREATER_THAN) + VALID_PREDICATE + ")");
    assertEquals(menuExpression, getTrimmedMenuExpression());
    assertEquals(menuExpression, getTrimmedInputExpression());
    assertEquals(menuExpression, getTrimmedTextDisplayExpression());
  }
  
  public void testGoodConditionalSubmits() {
    thirdExpressionsPanel.conditionDisplayTextBox.setValue(VALID_PAREN_CONDITIONAL, true);
    assertTrue(experimentCreationPanel.canSubmit());
  }
  
  public void testBadConditionalDoesNotSubmit() {
    thirdExpressionsPanel.conditionDisplayTextBox.setValue(INVALID_COMP_CONDITIONAL, true);
    assertFalse(experimentCreationPanel.canSubmit());
  }
  
  public void testFixingOneBadConditionalItemDoesNotAllowOtherBadConditionalItem() {
    experiment.setInputs(new InputDAO[] {createInput(InputDAO.LIKERT, VALID_NAME_0),
                                         createInput(InputDAO.LIKERT, VALID_NAME_1),
                                         createInput(InputDAO.LIKERT, VALID_NAME_2)});
    experimentCreationPanel = createExperimentCreationPanel(experiment);

    // Get inputs panels' conditional text fields.
    InputsListPanel firstInputsListPanel = experimentCreationPanel.inputsListPanels.get(0);
    InputsPanel secondInputsPanel = firstInputsListPanel.inputsPanelsList.get(1);
    InputsPanel thirdInputsPanel = firstInputsListPanel.inputsPanelsList.get(2);
    MouseOverTextBoxBase secondInputConditionalText = 
        secondInputsPanel.conditionalPanel.conditionDisplayTextBox;
    MouseOverTextBoxBase thirdInputConditionalText = 
        thirdInputsPanel.conditionalPanel.conditionDisplayTextBox;

    // Set invalid conditional text for both. Fire events.
    secondInputConditionalText.setValue(INVALID_COMP_CONDITIONAL, true);
    thirdInputConditionalText.setValue(INVALID_VARNAME_CONDITIONAL, true);
    assertFalse(experimentCreationPanel.canSubmit());

    // Set valid conditional for first input. Fire events.
    secondInputConditionalText.setValue(VALID_SIMPLE_CONDITIONAL, true);
    assertFalse(experimentCreationPanel.canSubmit());

    // Set valid conditional for second input. Fire events.
    thirdInputConditionalText.setValue(VALID_COMPOUND_CONDITIONAL, true);
    assertTrue(experimentCreationPanel.canSubmit());
  }
  
  public void testInputRenameRenamesRelatedConditionals() {
    // Get inputs panels and pertinent fields.
    InputsListPanel firstInputsListPanel = experimentCreationPanel.inputsListPanels.get(0);
    InputsPanel firstInputsPanel = firstInputsListPanel.inputsPanelsList.get(0);
    InputsPanel thirdInputsPanel = firstInputsListPanel.inputsPanelsList.get(2);
    MouseOverTextBoxBase firstInputNameText = firstInputsPanel.varNameText;
    MouseOverTextBoxBase thirdInputConditionalText = 
        thirdInputsPanel.conditionalPanel.conditionDisplayTextBox;
    
    // Set conditional text for third panel based on first input. Fire events.
    thirdInputConditionalText.setValue(VALID_NAME_0 + VALID_SIMPLE_CONDITIONAL_TAIL, true);
    
    // Rename first input to a valid name. Fire events.
    firstInputNameText.setValue(VALID_NAME_EXTRA, true);
    
    // Check that third input now conditionalizes based on new first input name, not old.
    String newConditional = replaceWhitespace(VALID_NAME_EXTRA + VALID_SIMPLE_CONDITIONAL_TAIL);
    assertEquals(getTrimmedTextDisplayExpression(), newConditional);
    assertEquals(getTrimmedInputExpression(), newConditional);
    assertFalse(newConditional.contains(VALID_NAME_0));
  }
  
  public void testInputResponseTypeChangeToLikertUpdatesConditionalMenu() {
    ConditionalExpressionPanel conditionPanel = 
        setConditionThenChangeResponseType(0, InputDAO.LIKERT);
    
    // Check the proper changes happened.
    assertTrue(conditionPanel.isValid());
    assertTrue(conditionPanel.predicatePanel.responseTypeRequiresListBox());
    assertEquals(conditionPanel.predicatePanel.predicateListBox.getItemCount(),
                 InputDAO.DEFAULT_LIKERT_STEPS.intValue());
  }
  
  public void testInputResponseTypeChangeToLikertSmileysUpdatesConditionalMenu() {
    ConditionalExpressionPanel conditionPanel = 
        setConditionThenChangeResponseType(0, InputDAO.LIKERT_SMILEYS);
    
    // Check the proper changes happened.
    assertTrue(conditionPanel.isValid());
    assertTrue(conditionPanel.predicatePanel.responseTypeRequiresListBox());
    assertEquals(conditionPanel.predicatePanel.predicateListBox.getItemCount(),
                 InputDAO.DEFAULT_LIKERT_STEPS.intValue());
  }
  
  public void testInputResponseTypeChangeToListUpdatesConditionalMenu() {
    ConditionalExpressionPanel conditionPanel = 
        setConditionThenChangeResponseType(0, InputDAO.LIST);
    
    // Check the proper changes happened.
    assertTrue(conditionPanel.isValid());
    assertTrue(conditionPanel.predicatePanel.responseTypeRequiresListBox());
    // No list items (besides the default empty item) have been added yet.
    assertEquals(conditionPanel.predicatePanel.predicateListBox.getItemCount(), 1);
  }

  public void testInputResponseTypeChangeToNumberUpdatesConditionalMenu() {
    ConditionalExpressionPanel conditionPanel = 
        setConditionThenChangeResponseType(0, InputDAO.NUMBER);
    
    // Check the proper changes happened.
    assertTrue(conditionPanel.isValid());
    assertTrue(conditionPanel.predicatePanel.responseTypeRequiresTextBox());
    assertTrue(conditionPanel.predicatePanel.predicateTextBox != null);
  }

  public void testInputResponseTypeChangeToLocationUpdatesConditionalMenu() {
    ConditionalExpressionPanel conditionPanel = 
        setConditionThenChangeResponseType(0, InputDAO.LOCATION);
    
    // Check the proper changes happened.
    assertFalse(conditionPanel.isValid());
  }

  public void testInputResponseTypeChangeToPhotoUpdatesConditionalMenu() {
    ConditionalExpressionPanel conditionPanel = 
        setConditionThenChangeResponseType(0, InputDAO.PHOTO);
    
    // Check the proper changes happened.
    assertFalse(conditionPanel.isValid());
  }
  
  public void testLikertStepsChangeUpdatesConditionalMenu() {
    // Get inputs panels and pertinent fields.
    InputsListPanel firstInputsListPanel = experimentCreationPanel.inputsListPanels.get(0);
    InputsPanel secondInputsPanel = firstInputsListPanel.inputsPanelsList.get(1);
    InputsPanel thirdInputsPanel = firstInputsListPanel.inputsPanelsList.get(2);
    MouseOverTextBoxBase secondInputLikertStepsText = secondInputsPanel.responseView.stepsText;
    MouseOverTextBoxBase thirdInputConditionalText = 
        thirdInputsPanel.conditionalPanel.conditionDisplayTextBox;
    
    // Set conditional text for third panel based on first input. Fire events.
    thirdInputConditionalText.setValue(VALID_NAME_1 + VALID_SIMPLE_CONDITIONAL_TAIL, true);
    
    // Check that third input predicate panel has correct number of options.
    PredicatePanel thirdInputFirstPredicatePanel = 
        thirdInputsPanel.conditionalPanel.conditionPanels.get(0).predicatePanel;
    assertTrue(thirdInputFirstPredicatePanel.predicateListBox != null);
    assertEquals(thirdInputFirstPredicatePanel.predicateListBox.getItemCount(), 
                 InputDAO.DEFAULT_LIKERT_STEPS.intValue());
    
    // Change likert steps number.
    secondInputLikertStepsText.setValue(NON_DEFAULT_LIKERT_STEPS.toString(), true);
    
    // Check that third input predicate panel has correct number of options.
    assertTrue(thirdInputFirstPredicatePanel.predicateListBox != null);
    assertEquals(thirdInputFirstPredicatePanel.predicateListBox.getItemCount(), 
                 NON_DEFAULT_LIKERT_STEPS.intValue());
  }
  
  public void testAddingSubtractingListOptionsUpdatesConditionalMenu() {
    // Change second input to list type.
    setConditionThenChangeResponseType(1, InputDAO.LIST);
    
    // Get inputs panels and pertinent fields.
    InputsListPanel firstInputsListPanel = experimentCreationPanel.inputsListPanels.get(0);
    InputsPanel secondInputsPanel = firstInputsListPanel.inputsPanelsList.get(1);
    InputsPanel thirdInputsPanel = firstInputsListPanel.inputsPanelsList.get(2);
    ListChoicesPanel secondInputListChoicesPanel = secondInputsPanel.responseView.listChoicesPanel;
    ListChoicePanel listChoicePanel = secondInputListChoicesPanel.choicePanelsList.get(0);
    MouseOverTextBoxBase thirdInputConditionalText = 
        thirdInputsPanel.conditionalPanel.conditionDisplayTextBox;
    
    // Set conditional text for third panel based on first input. Fire events.
    thirdInputConditionalText.setValue(VALID_NAME_1 + VALID_SIMPLE_CONDITIONAL_TAIL, true);
    
    // Add a list option.
    PredicatePanel thirdInputFirstPredicatePanel = 
        thirdInputsPanel.conditionalPanel.conditionPanels.get(0).predicatePanel;
    listChoicePanel.addChoicePanel();
    assertTrue(thirdInputFirstPredicatePanel.predicateListBox != null);
    assertEquals(thirdInputFirstPredicatePanel.predicateListBox.getItemCount(), 2);
    
    // Delete a list option.
    listChoicePanel.deleteThis();
    assertTrue(thirdInputFirstPredicatePanel.predicateListBox != null);
    assertEquals(thirdInputFirstPredicatePanel.predicateListBox.getItemCount(), 1);
  }
  
  public void testRenamingListOptionsUpdatesConditionalMenu() {
    // Change second input to list type.
    setConditionThenChangeResponseType(1, InputDAO.LIST);
    
    // Get inputs panels and pertinent fields.
    InputsListPanel firstInputsListPanel = experimentCreationPanel.inputsListPanels.get(0);
    InputsPanel secondInputsPanel = firstInputsListPanel.inputsPanelsList.get(1);
    InputsPanel thirdInputsPanel = firstInputsListPanel.inputsPanelsList.get(2);
    ListChoicesPanel secondInputListChoicesPanel = secondInputsPanel.responseView.listChoicesPanel;
    ListChoicePanel listChoicePanel = secondInputListChoicesPanel.choicePanelsList.get(0);
    MouseOverTextBoxBase thirdInputConditionalText = 
        thirdInputsPanel.conditionalPanel.conditionDisplayTextBox;
    
    // Set conditional text for third panel based on first input. Fire events.
    thirdInputConditionalText.setValue(VALID_NAME_1 + VALID_SIMPLE_CONDITIONAL_TAIL, true);
    
    // Change first list choice text.
    PredicatePanel thirdInputFirstPredicatePanel = 
        thirdInputsPanel.conditionalPanel.conditionPanels.get(0).predicatePanel;
    listChoicePanel.setChoice(LIST_CHOICE_1, true);
    assertEquals(thirdInputFirstPredicatePanel.predicateListBox.getItemCount(), 1);
    assertEquals(thirdInputFirstPredicatePanel.predicateListBox.getItemText(0), LIST_CHOICE_1);
    
    // Again change first list choice text.
    listChoicePanel.setChoice(LIST_CHOICE_2, true);
    assertEquals(thirdInputFirstPredicatePanel.predicateListBox.getItemCount(), 1);
    assertEquals(thirdInputFirstPredicatePanel.predicateListBox.getItemText(0), LIST_CHOICE_2);
  }
  
  public void testDraggingFromBeforeToBeforeDependentInputDoesNotInvalidateConditional() {
    // Get inputs panels and pertinent fields.
    InputsListPanel firstInputsListPanel = experimentCreationPanel.inputsListPanels.get(0);
    InputsPanel firstInputsPanel = firstInputsListPanel.inputsPanelsList.get(0);
    InputsPanel secondInputsPanel = firstInputsListPanel.inputsPanelsList.get(1);
    InputsPanel thirdInputsPanel = firstInputsListPanel.inputsPanelsList.get(2);
    MouseOverTextBoxBase thirdInputConditionalText = 
        thirdInputsPanel.conditionalPanel.conditionDisplayTextBox;
    
    // Set conditional text for third panel based on first input. Fire events.
    thirdInputConditionalText.setValue(VALID_NAME_0 + VALID_SIMPLE_CONDITIONAL_TAIL, true);
    
    ConditionalExpressionPanel thirdInputFirstExpressionPanel = getFirstExpressionPanel(thirdInputsPanel);
    assertTrue(thirdInputFirstExpressionPanel.isValid());
    
    // "Move" first inputs panel.
    reorderPanels(secondInputsPanel, firstInputsPanel, thirdInputsPanel, firstInputsListPanel);
    
    // Ensure first list choice panel is still valid.
    assertTrue(thirdInputFirstExpressionPanel.isValid());
  }
  
  public void testDraggingFromBeforeToAfterDependentInputInvalidatesConditional() {
    // Get inputs panels and pertinent fields.
    InputsListPanel firstInputsListPanel = experimentCreationPanel.inputsListPanels.get(0);
    InputsPanel firstInputsPanel = firstInputsListPanel.inputsPanelsList.get(0);
    InputsPanel secondInputsPanel = firstInputsListPanel.inputsPanelsList.get(1);
    InputsPanel thirdInputsPanel = firstInputsListPanel.inputsPanelsList.get(2);
    MouseOverTextBoxBase thirdInputConditionalText = 
        thirdInputsPanel.conditionalPanel.conditionDisplayTextBox;
    
    // Set conditional text for third panel based on first input. Fire events.
    thirdInputConditionalText.setValue(VALID_NAME_0 + VALID_SIMPLE_CONDITIONAL_TAIL, true);
    
    ConditionalExpressionPanel thirdInputFirstExpressionPanel = getFirstExpressionPanel(thirdInputsPanel);
    assertTrue(thirdInputFirstExpressionPanel.isValid());
    
    // "Move" first inputs panel.
    reorderPanels(secondInputsPanel, thirdInputsPanel, firstInputsPanel, firstInputsListPanel);
    
    // Ensure first list choice panel is now invalid.
    assertFalse(thirdInputFirstExpressionPanel.isValid());
  }
  
  public void testDraggingFromAfterToBeforeDependentInputValidatesConditional() {
    // Get inputs panels and pertinent fields.
    InputsListPanel firstInputsListPanel = experimentCreationPanel.inputsListPanels.get(0);
    InputsPanel firstInputsPanel = firstInputsListPanel.inputsPanelsList.get(0);
    InputsPanel secondInputsPanel = firstInputsListPanel.inputsPanelsList.get(1);
    InputsPanel thirdInputsPanel = firstInputsListPanel.inputsPanelsList.get(2);
    MouseOverTextBoxBase firstInputConditionalText = 
        firstInputsPanel.conditionalPanel.conditionDisplayTextBox;
    
    // Set conditional text for first panel based on third input. Fire events.
    firstInputConditionalText.setValue(VALID_NAME_2 + VALID_SIMPLE_CONDITIONAL_TAIL, true);
    
    ConditionalExpressionPanel firstInputFirstExpressionPanel = getFirstExpressionPanel(firstInputsPanel);
    assertFalse(firstInputFirstExpressionPanel.isValid());
    
    // "Move" third inputs panel.
    reorderPanels(secondInputsPanel, thirdInputsPanel, firstInputsPanel, firstInputsListPanel);
    
    // Ensure first list choice panel is now valid.
    assertTrue(firstInputFirstExpressionPanel.isValid());
  }
  
  public void testDraggingFromAfterToAfterDependentInputDoesNotValidateConditional() {
    // Get inputs panels and pertinent fields.
    InputsListPanel firstInputsListPanel = experimentCreationPanel.inputsListPanels.get(0);
    InputsPanel firstInputsPanel = firstInputsListPanel.inputsPanelsList.get(0);
    InputsPanel secondInputsPanel = firstInputsListPanel.inputsPanelsList.get(1);
    InputsPanel thirdInputsPanel = firstInputsListPanel.inputsPanelsList.get(2);
    MouseOverTextBoxBase firstInputConditionalText = 
        firstInputsPanel.conditionalPanel.conditionDisplayTextBox;
    
    // Set conditional text for first panel based on third input. Fire events.
    firstInputConditionalText.setValue(VALID_NAME_2 + VALID_SIMPLE_CONDITIONAL_TAIL, true);
    
    // Ensure first list choice panel is invalid.
    ConditionalExpressionPanel firstInputFirstExpressionPanel = getFirstExpressionPanel(firstInputsPanel);
    assertFalse(firstInputFirstExpressionPanel.isValid());
    
    // "Move" third inputs panel.
    reorderPanels(firstInputsPanel, thirdInputsPanel, secondInputsPanel, firstInputsListPanel);
    
    // Ensure first list choice panel is still invalid.
    assertFalse(firstInputFirstExpressionPanel.isValid());
  }
  
  public void testEnteringTextConditionalWithInvalidInputNameDoesNotSubmit() {
    // Get inputs panels and pertinent fields.
    InputsListPanel firstInputsListPanel = experimentCreationPanel.inputsListPanels.get(0);
    InputsPanel secondInputsPanel = firstInputsListPanel.inputsPanelsList.get(1);
    MouseOverTextBoxBase secondInputConditionalText = 
        secondInputsPanel.conditionalPanel.conditionDisplayTextBox;

    // Set conditional for second input based on second and third input (disallowed).
    secondInputConditionalText.setValue(VALID_COMPOUND_CONDITIONAL, true);
    assertFalse(experimentCreationPanel.canSubmit());
  }
  
  public void testEnteringTextConditionalWithOutOfBoundsPredicateDoesNotSubmit() {
    // Get inputs panels' conditional text fields.
    InputsListPanel firstInputsListPanel = experimentCreationPanel.inputsListPanels.get(0);
    InputsPanel thirdInputsPanel = firstInputsListPanel.inputsPanelsList.get(2);
    MouseOverTextBoxBase thirdInputConditionalText = 
        thirdInputsPanel.conditionalPanel.conditionDisplayTextBox;
    
    // Set conditional with valid syntax but out-of-bounds predicate for third input. 
    // Fire events.
    thirdInputConditionalText.setValue(INVALID_LIKERT_OUT_OF_BOUNDS_CONDITIONAL, true);
    assertFalse(experimentCreationPanel.canSubmit());
  }
  
  public void testConstructConditionalExpression() {
    InputsListPanel firstInputsListPanel = experimentCreationPanel.inputsListPanels.get(0);
    ConditionalExpressionsPanel conditionPanel = 
        firstInputsListPanel.inputsPanelsList.get(0).conditionalPanel;
    List<String> expressions = new ArrayList<String>();
    expressions.add(VALID_SIMPLE_CONDITIONAL);
    expressions.add(VALID_SIMPLE_CONDITIONAL_PT2);
    expressions.add(VALID_SIMPLE_CONDITIONAL_PT3);
    assertEquals(joinConditionalList(expressions), 
                 replaceWhitespace(conditionPanel.constructConditionalExpression(expressions)));
  }
  
  private String joinConditionalList(List<String> expressions) {
    return replaceWhitespace(Joiner.on(" ").join(expressions));
  }
  
  private ExperimentDAO createExperimentWithNumberLikertLikertsmileys() {
    ExperimentDAO experiment = CreationTestUtil.createValidOngoingExperiment();
    InputDAO input1 = CreationTestUtil.createInput(InputDAO.NUMBER, VALID_NAME_0);
    InputDAO input2 = CreationTestUtil.createInput(InputDAO.LIKERT, VALID_NAME_1);
    InputDAO input3 = CreationTestUtil.createInput(InputDAO.LIKERT_SMILEYS, VALID_NAME_2);
    experiment.setInputs(new InputDAO[]{input1, input2, input3});
    return experiment;
  }
  
  private void assertTextUpdatesModelAndMenu(String conditionalText) {
    thirdExpressionsPanel.conditionDisplayTextBox.setValue(conditionalText, true);
    String textDisplay = replaceWhitespace(conditionalText);
    assertEquals(textDisplay, getTrimmedInputExpression());
    assertEquals(textDisplay, getTrimmedMenuExpression());
  }
  
  private void setMenuAttributesWithEventsFired(ConditionalExpressionPanel panel,
                                                 String name, int comparator, int value) {
    panel.varNameText.setValue(name, true);
    setSelectedIndexWithFiringEvents(panel.comparatorListBox, comparator);
    panel.predicatePanel.setValue(value, true);
  }
  
  private void setSelectedIndexWithFiringEvents(ListBox listBox, int index) {
    listBox.setSelectedIndex(index);
    ChangeEvent.fireNativeEvent(Document.get().createChangeEvent(),listBox);
  }
  
  private String getOpStr(int opIndex) {
    return ConditionalExpressionsPanel.COMPARATORS[opIndex];
  }
  
  private String addNextWithOp(ConditionalExpressionPanel panel, int op) {
    panel.addNextListBox.setSelectedIndex(op);
    ChangeEvent.fireNativeEvent(Document.get().createChangeEvent(), panel.addNextListBox);
    return ConditionalExpressionsPanel.OPS[op];
  }
  
  private String getTrimmedMenuExpression() {
    List<String> expressions = new ArrayList<String>();
    for (ConditionalExpressionPanel panel : thirdExpressionsPanel.conditionPanels) {
      expressions.add(panel.constructExpression());
    }
    return replaceWhitespace(Joiner.on("").join(expressions));
  }
  
  private String getTrimmedInputExpression() {
    return replaceWhitespace(thirdInput.getConditionExpression());
  }
  
  private String getTrimmedTextDisplayExpression() {
    return replaceWhitespace(thirdExpressionsPanel.conditionDisplayTextBox.getValue());
  }
  
  private String replaceWhitespace(String spacey) {
    return spacey.replaceAll("\\s*", "");
  } 
  
  private InputDAO createInput(String type, String name) {
    return CreationTestUtil.createInput(type, name);
  }
  
  private ExperimentCreationPanel createExperimentCreationPanel(ExperimentDAO experiment) {
    return CreationTestUtil.createExperimentCreationPanel(experiment);
  }
  
  private ConditionalExpressionPanel setConditionThenChangeResponseType(int changedTypeInputIndex,
                                                                        String responseTypeChangeTo) {
    // Get inputs panels and pertinent fields.
    InputsListPanel firstInputsListPanel = experimentCreationPanel.inputsListPanels.get(0);
    InputsPanel changingInputsPanel = 
        firstInputsListPanel.inputsPanelsList.get(changedTypeInputIndex);
    InputsPanel thirdInputsPanel = firstInputsListPanel.inputsPanelsList.get(2);
    ListBox changingInputResponseType = changingInputsPanel.responseTypeListBox;
    ConditionalExpressionPanel thirdInputFirstConditionPanel = getFirstExpressionPanel(thirdInputsPanel);
    
    // Set conditional text for third panel based on changing input. Fire events.
    String changingInputName = VALID_NAME_PREFIX + changedTypeInputIndex ;
    thirdInputFirstConditionPanel.varNameText.setValue(changingInputName, true);
    
    // Change third input type.
    setSelectedIndexWithFiringEvents(changingInputResponseType, 
                                     getResponseTypeIndex(responseTypeChangeTo));
    
    // Return the Conditional Expression Panel to check.
    return thirdInputFirstConditionPanel;
  }
  
  private int getResponseTypeIndex(String responseType) {
    int index = -1;
    for (int i = 0; i < InputDAO.RESPONSE_TYPES.length; ++i) {
      if (InputDAO.RESPONSE_TYPES[i].equals(responseType)) {
        index = i;
        break;
      }
    }
    return index;
  }
  
  private ConditionalExpressionPanel getFirstExpressionPanel(InputsPanel firstInputsPanel) {
    ConditionalExpressionPanel firstInputFirstExpressionPanel =
        firstInputsPanel.conditionalPanel.conditionPanels.get(0);
    return firstInputFirstExpressionPanel;
  }
  
  private void reorderPanels(InputsPanel panel1, InputsPanel panel2, InputsPanel panel3, 
                             InputsListPanel listPanel) {
    LinkedList<InputsPanel> newOrder = new LinkedList<InputsPanel>();
    newOrder.add(panel1);
    newOrder.add(panel2);
    newOrder.add(panel3);
    listPanel.inputsPanelsList = newOrder;
    // The method called upon the end of input dragging.
    listPanel.updateModelInputsAndConditionals();
  }

}
