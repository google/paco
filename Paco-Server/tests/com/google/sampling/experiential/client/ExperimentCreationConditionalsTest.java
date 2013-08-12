package com.google.sampling.experiential.client;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.paco.shared.model.InputDAO;
import com.google.sampling.experiential.shared.LoginInfo;

public class ExperimentCreationConditionalsTest extends GWTTestCase {
  
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
  
  private InputDAO thirdInput;
  private ConditionalExpressionsPanel thirdExpressionsPanel;

  @Override
  public String getModuleName() {
    return "com.google.sampling.experiential.PacoEventserver";
  }
  
  public void gwtSetUp() {
    LoginInfo loginInfo = CreationTestUtil.createLoginInfo();
    ExperimentDAO experiment = createExperimentWithThreeInputs();
    ExperimentCreationPanel creationPanel = 
        CreationTestUtil.createExperimentCreationPanel(experiment, loginInfo);
    thirdInput = experiment.getInputs()[2];
    thirdExpressionsPanel = 
        creationPanel.inputsListPanels.get(0).inputsPanelsList.get(2).conditionalPanel;
  }

  private ExperimentDAO createExperimentWithThreeInputs() {
    ExperimentDAO experiment = new ExperimentDAO();
    InputDAO input1 = new InputDAO(null, VALID_NAME_1, null, null);
    input1.setResponseType(InputDAO.LIKERT);
    InputDAO input2 = new InputDAO(null, VALID_NAME_2, null, null);
    input2.setResponseType(InputDAO.LIKERT);
    InputDAO input3 = new InputDAO(null, VALID_NAME_3, null, null);
    input3.setResponseType(InputDAO.LIKERT);
    experiment.setInputs(new InputDAO[]{input1, input2, input3});
    return experiment;
  }
  
  public void testConditionalRegex() {
    final String conditionalRegex = ConditionalExpressionsPanel.OVERALL_CONDITIONAL_REGEX;
    assertTrue(VALID_SIMPLE_CONDITIONAL.matches(conditionalRegex));
    assertTrue(VALID_SPACEY_CONDITIONAL.matches(conditionalRegex));
    assertTrue(VALID_COMPOUND_CONDITIONAL.matches(conditionalRegex));
    assertTrue(VALID_PAREN_CONDITIONAL.matches(conditionalRegex));
    assertFalse(INVALID_COMP_CONDITIONAL.matches(conditionalRegex));
    assertFalse(INVALID_OP_SYNTAX_CONDITIONAL.matches(conditionalRegex));
    assertFalse(INVALID_VARNAME_CONDITIONAL.matches(conditionalRegex));
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
  
  private void assertTextUpdatesModelAndMenu(String conditionalText) {
    thirdExpressionsPanel.conditionDisplayTextBox.setValue(conditionalText, true);
    String textDisplay = replaceWhitespace(conditionalText);
    assertEquals(textDisplay, getTrimmedInputExpression());
    assertEquals(textDisplay, getTrimmedMenuExpression());
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
  
  private String replaceWhitespace(String spacey) {
    return spacey.replaceAll("\\s*", "");
  }
  
  

}
