package com.google.sampling.experiential.client;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.paco.shared.model.InputDAO;
import com.google.sampling.experiential.shared.LoginInfo;

public class ExperimentCreationConditionalsTest extends GWTTestCase {
  
  private static final int VALID_PREDICATE = 3;
  private static final int VALID_PREDICATE_2 = 5;
  private static final int EQUALS = 0;
  private static final int GREATER_THAN = 2;
  public static final String VALID_NAME_1 = "q1";
  public static final String VALID_NAME_2 = "q2";
  public static final String VALID_NAME_3 = "q3";
  
  public static final String VALID_SIMPLE_CONDITIONAL = VALID_NAME_1 + " > 3";
  public static final String VALID_SPACEY_CONDITIONAL = "  " + VALID_NAME_1 + " > 3  ";
  public static final String VALID_COMPOUND_CONDITIONAL = 
      VALID_NAME_1 + " > 3 && "+ VALID_NAME_2 + " == 5";
  public static final String VALID_PAREN_CONDITIONAL = VALID_NAME_1 + ">3 && (" +
      VALID_NAME_1 + "!=4 || " + VALID_NAME_2 + "==1)";
  public static final String INVALID_COMP_CONDITIONAL = VALID_NAME_1 + " ? 3";
  public static final String INVALID_OP_SYNTAX_CONDITIONAL = VALID_NAME_1 + " > 3 &&& " + 
      VALID_NAME_2 + " == 5";
  public static final String INVALID_VARNAME_CONDITIONAL = "1q > 3";
  public static final String INVALID_UNBALANCED_PARENS_CONDITIONAL = "((q1 > 3) && q2 == 5";
  
  private ConditionalExpressionsPanel thirdExpressionsPanel;
  private ExperimentCreationPanel experimentCreationPanel;
  private ExperimentDAO experiment;
  private InputDAO thirdInput;

  @Override
  public String getModuleName() {
    return "com.google.sampling.experiential.PacoEventserver";
  }
  
  public void gwtSetUp() {
    experiment = createExperimentWithThreeInputs();
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
    setMenuAttributesWithEventsFired(panel, VALID_NAME_1, GREATER_THAN, VALID_PREDICATE);
    String menuExpression = 
        replaceWhitespace(VALID_NAME_1 + getOpStr(GREATER_THAN) + VALID_PREDICATE);
    assertEquals(menuExpression, getTrimmedMenuExpression());
    assertEquals(menuExpression, getTrimmedInputExpression());
    assertEquals(menuExpression, getTrimmedTextDisplayExpression());
  }
  
  public void testCompoundMenuUpdatesModelAndText() {
    ConditionalExpressionPanel firstPanel = thirdExpressionsPanel.conditionPanels.get(0);
    setMenuAttributesWithEventsFired(firstPanel, VALID_NAME_1, GREATER_THAN, VALID_PREDICATE);
    String op = addNextWithOp(firstPanel, ConditionalExpressionsPanel.AND_OP);
    ConditionalExpressionPanel secondPanel = thirdExpressionsPanel.conditionPanels.get(1);
    setMenuAttributesWithEventsFired(secondPanel, VALID_NAME_2, EQUALS, VALID_PREDICATE_2);
    String menuExpression = 
        replaceWhitespace(VALID_NAME_1 + getOpStr(GREATER_THAN) + VALID_PREDICATE + op 
                          + VALID_NAME_2 + getOpStr(EQUALS) + VALID_PREDICATE_2);
    assertEquals(menuExpression, getTrimmedMenuExpression());
    assertEquals(menuExpression, getTrimmedInputExpression());
    assertEquals(menuExpression, getTrimmedTextDisplayExpression());
  }
  
  public void testParenMenuUpdatesModelAndText() {
    ConditionalExpressionPanel panel = thirdExpressionsPanel.conditionPanels.get(0);
    setMenuAttributesWithEventsFired(panel, VALID_NAME_1, GREATER_THAN, VALID_PREDICATE);
    panel.increaseLeftParensWithUpdate();
    panel.increaseRightParensWithUpdate();
    String menuExpression = 
        replaceWhitespace("(" + VALID_NAME_1 + getOpStr(GREATER_THAN) + VALID_PREDICATE + ")");
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
    experiment.setInputs(new InputDAO[] {createInput(InputDAO.LIKERT, VALID_NAME_1),
                                         createInput(InputDAO.LIKERT, VALID_NAME_2),
                                         createInput(InputDAO.LIKERT, VALID_NAME_3)});
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
    secondInputConditionalText.setValue(VALID_COMPOUND_CONDITIONAL, true);
    assertFalse(experimentCreationPanel.canSubmit());

    // Set valid conditional for second input. Fire events.
    thirdInputConditionalText.setValue(VALID_SIMPLE_CONDITIONAL, true);
    assertTrue(experimentCreationPanel.canSubmit());
  }
  
  private ExperimentDAO createExperimentWithThreeInputs() {
    ExperimentDAO experiment = CreationTestUtil.createValidOngoingExperiment();
    InputDAO input1 = CreationTestUtil.createInput(InputDAO.LIKERT, VALID_NAME_1);
    InputDAO input2 = CreationTestUtil.createInput(InputDAO.LIKERT, VALID_NAME_2);
    InputDAO input3 = CreationTestUtil.createInput(InputDAO.LIKERT, VALID_NAME_3);
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
    setComparatorWithFiringEvents(panel, comparator);
    panel.predicatePanel.setValue(value, true);
  }
  
  private void setComparatorWithFiringEvents(ConditionalExpressionPanel panel, int operator) {
    panel.comparatorListBox.setSelectedIndex(operator);
    ChangeEvent.fireNativeEvent(Document.get().createChangeEvent(), panel.comparatorListBox);
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

}
