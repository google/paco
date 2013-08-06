package com.google.sampling.experiential.client;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.DisclosurePanelImages;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.paco.shared.model.InputDAO;

public class ConditionalExpressionsPanel extends Composite {

  public static final int NO_OP = 0;
  public static final int AND_OP = 1;
  public static final int OR_OP = 2;
  public static final String[] OPS = new String[] { "", "&&", "||" };
  public static final String[] COMPARATORS = new String[] { "==", "!=", ">", ">=", "<", "<=", "contains" };

  private static final String OP_REGEX = "(&&|\\|\\|)";
  private static final String NAME_REGEX = "(" + InputDAO.NAME_REGEX + ")";
  private static final String COMP_REGEX = "(==|!=|>|>=|<|<=|contains)";
  private static final String PREDICATE_REGEX = "([0-9]+)";
  private static final String WHITESPACE = "\\s*";
  private static final String LEFT_PARENS = "((?:\\(*" + WHITESPACE + ")*)";
  private static final String RIGHT_PARENS = "((?:" + WHITESPACE + "\\))*)";
  private static final String NO_OP_CONDITIONAL_REGEX = LEFT_PARENS + WHITESPACE + NAME_REGEX + WHITESPACE 
      + COMP_REGEX + WHITESPACE + PREDICATE_REGEX + RIGHT_PARENS;
  private static final String OP_CONDITIONAL_REGEX = OP_REGEX + WHITESPACE + NO_OP_CONDITIONAL_REGEX;
  public static final String SINGLE_CONDITIONAL_REGEX = OP_REGEX + "?" + WHITESPACE 
      + NO_OP_CONDITIONAL_REGEX;
  public static final String OVERALL_CONDITIONAL_REGEX = NO_OP_CONDITIONAL_REGEX + "(" 
      + WHITESPACE + OP_CONDITIONAL_REGEX + ")*";

  private MyConstants myConstants;

  private VerticalPanel mainPanel;
  private HorizontalPanel textEntryPanel;
  private DisclosurePanel conditionalListDisclosurePanel;
  private VerticalPanel conditionalListPanel;
  private TextBox conditionDisplayTextBox;
  private Button parenCancelButton;

  private InputDAO input;
  private InputsPanel parent;

  private List<ConditionalExpressionPanel> conditionPanels;
  private List<String> conditionalExpressions;
  private ConditionalExpressionPanel unbalancedParenPanel;

  public ConditionalExpressionsPanel(InputDAO input, InputsPanel parent) {
    super();
    myConstants = GWT.create(MyConstants.class);
    this.input = input;
    this.parent = parent;
    mainPanel = new VerticalPanel();
    initWidget(mainPanel);
    conditionPanels = new ArrayList<ConditionalExpressionPanel>();
    conditionalExpressions = new ArrayList<String>();
    createPanel();
  }

  private void createPanel() {
    createTextEntryPanel();
    createConditionalListDisclosurePanel();
    createParenCancelButton();
  }

  private void createTextEntryPanel() {
    textEntryPanel = new HorizontalPanel();
    mainPanel.add(textEntryPanel);

    Label conditionalExpressionLabel = new Label(myConstants.conditionalPrompt() + ": ");
    conditionalExpressionLabel.setStyleName("keyLabel");
    textEntryPanel.add(conditionalExpressionLabel);

    conditionDisplayTextBox = new TextBox();
    conditionDisplayTextBox.setVisibleLength(60);
    updateTextDisplayExpression(input.getConditionExpression());
    conditionDisplayTextBox.addMouseDownHandler(parent);
    conditionDisplayTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> arg0) {
        updateExpressionUsingTextPanel(conditionDisplayTextBox.getValue());
      }
    });
    textEntryPanel.add(conditionDisplayTextBox);

    textEntryPanel.add(new HTML("   <span style='font-style:italic;font-size:small;" + "text-color:#888888;'>" + "("
        + myConstants.eg() + ", " + "q1name < 3" + ")" + "</span>"));
  }

  private void createConditionalListDisclosurePanel() {
    conditionalListDisclosurePanel = new DisclosurePanel();

    final DisclosurePanelHeader closedHeaderWidget = 
        new DisclosurePanelHeader(false,"<b>" + myConstants.clickToEditCondition() + "</b>");
    final DisclosurePanelHeader openHeaderWidget = 
        new DisclosurePanelHeader(true, "<b>" + myConstants.clickToCloseConditionEditor() + "</b>");

    conditionalListDisclosurePanel.setHeader(closedHeaderWidget);
    conditionalListDisclosurePanel.addOpenHandler(new OpenHandler<DisclosurePanel>() {
      @Override
      public void onOpen(OpenEvent<DisclosurePanel> event) {
        conditionalListDisclosurePanel.setHeader(openHeaderWidget);
      }
    });
    conditionalListDisclosurePanel.addCloseHandler(new CloseHandler<DisclosurePanel>() {
      @Override
      public void onClose(CloseEvent<DisclosurePanel> event) {
        conditionalListDisclosurePanel.setHeader(closedHeaderWidget);
      }
    });

    conditionalListPanel = new VerticalPanel();
    createLonePanel();
    updateListDisplayExpression(input.getConditionExpression());

    conditionalListDisclosurePanel.setContent(conditionalListPanel);

    mainPanel.add(conditionalListDisclosurePanel);
  }

  final DisclosurePanelImages images = (DisclosurePanelImages) GWT.create(DisclosurePanelImages.class);

  private class DisclosurePanelHeader extends HorizontalPanel {
    public DisclosurePanelHeader(boolean isOpen, String html) {
      add(isOpen ? images.disclosurePanelOpen().createImage() : images.disclosurePanelClosed().createImage());
      add(new HTML(html));
    }
  }

  private void createParenCancelButton() {
    parenCancelButton = new Button(myConstants.cancel());
    parenCancelButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (unbalancedParenPanel != null) {
          switch (unbalancedParenPanel.getParenBalancingMode()) {
          case ConditionalExpressionPanel.JUST_ADDED_LEFT_PAREN:
            unbalancedParenPanel.decreaseLeftParensWithUpdate();
            break;
          case ConditionalExpressionPanel.JUST_ADDED_RIGHT_PAREN:
            unbalancedParenPanel.decreaseRightParensWithUpdate();
            break;
          case ConditionalExpressionPanel.JUST_DELETED_LEFT_PAREN:
            unbalancedParenPanel.increaseLeftParensWithUpdate();
            break;
          case ConditionalExpressionPanel.JUST_DELETED_RIGHT_PAREN:
            unbalancedParenPanel.increaseRightParensWithUpdate();
            break;
          default:
            break;
          }
        }
        restoreOverallNormalMode();
      }
    });
  }

  private void addParenCancelButton() {
    mainPanel.add(parenCancelButton);
  }

  private void removeParenCancelButton() {
    mainPanel.remove(parenCancelButton);
  }

  public List<InputDAO> getPrecedingInputsWithVarName(String varName) {
    return parent.getPrecedingInputsWithVarName(varName);
  }

  public void addConditionalPanel(ConditionalExpressionPanel conditionalExpressionPanel, int conditionalOp) {
    ConditionalExpressionPanel newPanel = new ConditionalExpressionPanel(this, parent, conditionalOp);
    int index = conditionPanels.indexOf(conditionalExpressionPanel);
    int widgetIndex = conditionalListPanel.getWidgetIndex(conditionalExpressionPanel);
    insertConditionalPanelIntoLists(newPanel, index, widgetIndex);
    updateExpressionUsingListPanel(newPanel);
  }

  public void updateExpressionUsingListPanel(ConditionalExpressionPanel conditionalExpressionPanel) {
    int index = conditionPanels.indexOf(conditionalExpressionPanel);
    conditionalExpressions.set(index, conditionalExpressionPanel.constructExpression());
    String expression = Joiner.on(" ").join(conditionalExpressions);
    updateInputModelExpression(expression);
    updateTextDisplayExpression(expression);
  }

  private void updateExpressionUsingTextPanel(String expression) {
    updateInputModelExpression(expression);
    updateListDisplayExpression(expression);
  }

  private void updateInputModelExpression(String expression) {
    input.setConditionExpression(expression);
  }

  private void updateTextDisplayExpression(String expression) {
    conditionDisplayTextBox.setValue(expression, false);
  }

  // TODO: clean this up with more error-checking visible to the user.
  private void updateListDisplayExpression(String expression) {
    clearConditionalPanelLists();

    // TODO: callbacks to ExperimentCreationPanel when there are errors.
    // TODO: check for unbalanced parentheses errors.
    if (expression == null || expression.isEmpty() || !expression.matches(OVERALL_CONDITIONAL_REGEX)) {
      createLonePanel();
      return;
    }

    RegExp pattern = RegExp.compile(SINGLE_CONDITIONAL_REGEX, "g");
    MatchResult result = null;
    while ((result = pattern.exec(expression)) != null) {
      String op = extractOperationSymbol(result, 1);
      String leftParens =  result.getGroup(2);
      String name = result.getGroup(3);
      String comp = result.getGroup(4);
      String val = result.getGroup(5);
      String rightParens = result.getGroup(6);
      ConditionalExpressionPanel repPanel = 
          new ConditionalExpressionPanel(this, parent, getOperatorIndex(op), name, 
                                         getComparatorIndex(comp), getPredicateValue(val),
                                         getNumParens(leftParens), getNumParens(rightParens));
      addConditionalPanelToLists(repPanel);
    }
  }

  private int getNumParens(String parenString) {
    return parenString.replaceAll(WHITESPACE, "").length();
  }

  private void createLonePanel() {
    ConditionalExpressionPanel conditionalPanel = new ConditionalExpressionPanel(this, parent, NO_OP);
    addConditionalPanelToLists(conditionalPanel);
  }

  private void clearConditionalPanelLists() {
    conditionalListPanel.clear();
    conditionalExpressions.clear();
    conditionPanels.clear();
  }

  private void addConditionalPanelToLists(ConditionalExpressionPanel panel) {
    conditionalListPanel.add(panel);
    conditionPanels.add(panel);
    conditionalExpressions.add(panel.constructExpression());
  }

  private void insertConditionalPanelIntoLists(ConditionalExpressionPanel panel, 
                                               int index, int widgetIndex) {
    conditionPanels.add(index + 1, panel);
    conditionalExpressions.add(index + 1, panel.constructExpression());
    conditionalListPanel.insert(panel, widgetIndex + 1);
  }

  private int getOperatorIndex(String op) {
    int opsIndex = 0;
    for (int j = 0; j < OPS.length; ++j) {
      if (OPS[j].equals(op)) {
        opsIndex = j;
        break;
      }
    }
    return opsIndex;
  }

  private int getComparatorIndex(String comp) {
    int compIndex = 0;
    for (int k = 0; k < COMPARATORS.length; ++k) {
      if (COMPARATORS[k].equals(comp)) {
        compIndex = k;
        break;
      }
    }
    return compIndex;
  }

  private int getPredicateValue(String val) {
    return Integer.parseInt(val);
  }

  private String extractOperationSymbol(MatchResult result, int captureGroupNum) {
    String op;
    if (result.getGroup(captureGroupNum) != null && result.getGroup(captureGroupNum).length() > 0) {
      op = result.getGroup(1);
    } else {
      op = OPS[NO_OP];
    }
    return op;
  }

  public void enableAddRightParenMode(ConditionalExpressionPanel sender) {
    enableUnbalancedParenMode(sender);
    boolean isAfterSender = false;
    for (ConditionalExpressionPanel panel : conditionPanels) {
      if (panel.equals(sender)) {
        isAfterSender = true;
      }
      if (isAfterSender) {
        panel.enableAddRightParenMode();
      }
    }
  }

  public void enableAddLeftParenMode(ConditionalExpressionPanel sender) {
    enableUnbalancedParenMode(sender);
    for (ConditionalExpressionPanel panel : conditionPanels) {
      panel.enableAddLeftParenMode();
      if (panel.equals(sender)) {
        break;
      }
    }
  }

  public void enableDeleteRightParenMode(ConditionalExpressionPanel sender) {
    enableUnbalancedParenMode(sender);
    boolean isAfterSender = false;
    for (ConditionalExpressionPanel panel : conditionPanels) {
      if (panel.equals(sender)) {
        isAfterSender = true;
      }
      if (isAfterSender) {
        panel.enableDeleteRightParenMode();
      }
    }
  }

  public void enableDeleteLeftParenMode(ConditionalExpressionPanel sender) {
    enableUnbalancedParenMode(sender);
    for (ConditionalExpressionPanel panel : conditionPanels) {
      panel.enableDeleteLeftParenMode();
      if (panel.equals(sender)) {
        break;
      }
    }
  }

  public void restoreOverallNormalMode() {
    disableUnbalancedParenMode();
    for (ConditionalExpressionPanel panel : conditionPanels) {
      panel.enableNormalMode();
    }
  }
  
  private void enableUnbalancedParenMode(ConditionalExpressionPanel sender) {
    unbalancedParenPanel = sender;
    addParenCancelButton();
  }

  private void disableUnbalancedParenMode() {
    unbalancedParenPanel.setParenBalancingMode(ConditionalExpressionPanel.NEUTRAL_PAREN_MODE);
    unbalancedParenPanel = null;
    removeParenCancelButton();
  }

}
