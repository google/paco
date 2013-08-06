package com.google.sampling.experiential.client;

import java.util.List;

import com.google.common.base.Joiner;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.paco.shared.model.InputDAO;

public class ConditionalExpressionPanel extends Composite implements ChangeHandler {
  
  public static final int NEUTRAL_PAREN_MODE = 0;
  public static final int JUST_ADDED_LEFT_PAREN = 1;
  public static final int JUST_DELETED_LEFT_PAREN = 2;
  public static final int JUST_ADDED_RIGHT_PAREN = 3;
  public static final int JUST_DELETED_RIGHT_PAREN = 4;

  private MyConstants myConstants;

  private ConditionalExpressionsPanel parent;
  private MouseDownHandler precedenceMouseDownHandler;

  private HorizontalPanel mainPanel;
  private ListBox operatorListBox;
  private TextBox varNameText;
  private ListBox comparatorListBox;
  private PredicatePanel predicatePanel;
  private ListBox addNextListBox;
  private Label leftParenDisplayLabel;
  private Label rightParenDisplayLabel;
  private ParenthesesManagementPanel leftParenManagementPanel;
  private ParenthesesManagementPanel rightParenManagementPanel;

  private boolean isValid;
  private int numLeftParens;
  private int numRightParens;
  private int parenBalancingMode;

  public ConditionalExpressionPanel(ConditionalExpressionsPanel parent, 
                                    MouseDownHandler precedenceMouseDownHandler,
                                    int initialOp) {
    myConstants = GWT.create(MyConstants.class);
    this.parent = parent;
    this.precedenceMouseDownHandler = precedenceMouseDownHandler;

    mainPanel = new HorizontalPanel();
    mainPanel.setSpacing(2);
    mainPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    mainPanel.setWidth("100%");
    initWidget(mainPanel);

    createOperatorArea(initialOp);
    createLeftParenManagementPanel();
    createLeftParenDisplayPanel();
    createVarNameTextBox();
    createComparatorListBox();
    createPredicatePanel();
    createRightParenDisplayPanel();
    createRightParenManagementPanel();
    createAddNextListBox();

    setExpressionValidity(false);
    numLeftParens = 0;
    numRightParens = 0;
    parenBalancingMode = NEUTRAL_PAREN_MODE;
  }
  
  public ConditionalExpressionPanel(ConditionalExpressionsPanel parent, 
                                    MouseDownHandler precedenceMouseDownHandler,
                                    int initialOp, String inputName, int comparator, int predicate,
                                    int numLeftParens, int numRightParens) {
    this(parent, precedenceMouseDownHandler, initialOp);
    varNameText.setValue(inputName);
    configurePanelForInput(inputName);
    if (isValid) {
      setComparatorListBoxSelectedIndex(comparator);
      predicatePanel.setValue(predicate);
      setInitialLeftParens(numLeftParens);
      setInitialRightParens(numRightParens);
    }
  }

  public boolean isValid() {
    return isValid;
  }

  protected void addConditionalPanel(int conditionalOp) {
    parent.addConditionalPanel(this, conditionalOp);
  }

  protected void updateExpression() {
    parent.updateExpressionUsingListPanel(this);
  }

  protected String constructExpression() {
    String expression = "";
    if (isValid) {
      String op = operatorListBox.getValue(operatorListBox.getSelectedIndex());
      String leftParens = new String(new char[numLeftParens]).replace("\0", "(");
      String varName = varNameText.getText();
      String comparator = comparatorListBox.getValue(comparatorListBox.getSelectedIndex());
      String predicate = predicatePanel.getValue();
      String rightParens = new String(new char[numRightParens]).replace("\0", ")");
      expression = Joiner.on(" ").join(op, leftParens, varName, comparator, predicate, rightParens);
    }
    return expression;
  }

  protected void invalidateSelection() {
    setExpressionValidity(false);
  }

  private void createOperatorArea(int conditionalOp) {
    operatorListBox = new ListBox();
    if (conditionalOp == ConditionalExpressionsPanel.NO_OP) {
      configureInvalidOperatorListBox();
    } else {
      configureValidOperatorListBox(conditionalOp);
    }
  }

  private void configureInvalidOperatorListBox() {
    // Add label instead of box.
    Label blankLabel = new Label();
    blankLabel.setWidth("5em");
    mainPanel.add(blankLabel);

    // Configure list box for sake of value.
    operatorListBox.addItem("", ConditionalExpressionsPanel.OPS[ConditionalExpressionsPanel.NO_OP]);
    operatorListBox.setSelectedIndex(0);
  }

  private void configureValidOperatorListBox(int conditionalOp) {
    operatorListBox.addItem(myConstants.AND(), 
                            ConditionalExpressionsPanel.OPS[ConditionalExpressionsPanel.AND_OP]);
    operatorListBox.addItem(myConstants.OR(), 
                            ConditionalExpressionsPanel.OPS[ConditionalExpressionsPanel.OR_OP]);
    operatorListBox.setVisibleItemCount(1);
    operatorListBox.addMouseDownHandler(precedenceMouseDownHandler);
    operatorListBox.setEnabled(true);
    operatorListBox.addChangeHandler(this);
    // Index is off-by-one from constant since this menu has no no-op option.
    setOperatorListBoxSelectedIndex(conditionalOp - 1);
    mainPanel.add(operatorListBox);
  }
  
  private void setOperatorListBoxSelectedIndex(Integer selectedIndex) {
    setListBoxSelectedIndex(selectedIndex, operatorListBox);
  }

  private void createVarNameTextBox() {
    varNameText = new TextBox();
    varNameText.setWidth("75px"); // Same width as input var name text box.
    varNameText.addMouseDownHandler(precedenceMouseDownHandler);
    varNameText.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        configurePanelForInput(event.getValue());
        updateExpression();
      }
    });
    mainPanel.add(varNameText);
  }

  private List<InputDAO> getPrecedingInputsWithVarName(String varName) {
    return parent.getPrecedingInputsWithVarName(varName);
  }

  private void createComparatorListBox() {
    String[] comparators = ConditionalExpressionsPanel.COMPARATORS;
    comparatorListBox = new ListBox();
    comparatorListBox.addItem(myConstants.equals(), comparators[0]);
    comparatorListBox.addItem(myConstants.notEquals(), comparators[1]);
    comparatorListBox.addItem(myConstants.greaterThan(), comparators[2]);
    comparatorListBox.addItem(myConstants.greaterThanOrEquals(), comparators[3]);
    comparatorListBox.addItem(myConstants.lessThan(), comparators[4]);
    comparatorListBox.addItem(myConstants.lessThanOrEquals(), comparators[5]);
    comparatorListBox.setVisibleItemCount(1);
    comparatorListBox.addMouseDownHandler(precedenceMouseDownHandler);
    comparatorListBox.addChangeHandler(this);
    mainPanel.add(comparatorListBox);
  }

  private void configureComparatorListBoxForInput(InputDAO input) {
    String listExtra = ConditionalExpressionsPanel.COMPARATORS[6];
    int lastListBoxIndex = comparatorListBox.getItemCount() - 1;
    String lastListBoxItem = comparatorListBox.getValue(lastListBoxIndex);
    if (input.getResponseType().equals(InputDAO.LIST) && !lastListBoxItem.equals(listExtra)) {
      comparatorListBox.addItem(myConstants.contains(), listExtra);
    } else if (lastListBoxItem.equals(listExtra)) {
      comparatorListBox.removeItem(lastListBoxIndex);
    }
  }
  
  private void setComparatorListBoxSelectedIndex(Integer value) {
    setListBoxSelectedIndex(value, comparatorListBox);
  }

  private boolean inputCannotBeConditionalized(InputDAO input) {
    return input.getResponseType().equals(InputDAO.OPEN_TEXT) 
        || input.getResponseType().equals(InputDAO.LOCATION)
        || input.getResponseType().equals(InputDAO.PHOTO);
  }

  private void setExpressionValidity(boolean isValid) {
    this.isValid = isValid;
    setConfigurablePanelsEnabled(isValid);
  }
  
  private void setConfigurablePanelsEnabled(boolean isEnabled) {
    comparatorListBox.setEnabled(isEnabled);
    predicatePanel.setEnabled(isEnabled);
    addNextListBox.setEnabled(isEnabled);
    leftParenManagementPanel.setEnabled(isEnabled);
    rightParenManagementPanel.setEnabled(isEnabled);
  }
  
  private void setInitialEntryPanelsEnabled(boolean isEnabled) {
    varNameText.setEnabled(isEnabled);
    operatorListBox.setEnabled(isEnabled);
  }

  private void createPredicatePanel() {
    predicatePanel = new PredicatePanel(precedenceMouseDownHandler, this);
    mainPanel.add(predicatePanel);
  }

  private void createAddNextListBox() {
    addNextListBox = new ListBox();
    addNextListBox.addItem("");
    addNextListBox.addItem(myConstants.AND());
    addNextListBox.addItem(myConstants.OR());
    addNextListBox.setVisibleItemCount(1);
    addNextListBox.setSelectedIndex(0);
    addNextListBox.addMouseDownHandler(precedenceMouseDownHandler);
    addNextListBox.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        if (!(addNextListBox.getSelectedIndex() == ConditionalExpressionsPanel.NO_OP)) {
          addConditionalPanel(addNextListBox.getSelectedIndex());
        }
        addNextListBox.setSelectedIndex(ConditionalExpressionsPanel.NO_OP);
      }
    });
    mainPanel.add(addNextListBox);
  }
  

  private void setInitialLeftParens(int numLeftParens) {
    for (int i = 0; i < numLeftParens; ++i) {
      increaseNumLeftParens();
    }
  }
  
  private void setInitialRightParens(int numRightParens) {
    for (int j = 0; j < numRightParens; ++j) {
      increaseNumRightParens();
    }
  }
  
  protected void enableParenIncreaseBalancingMode(ParenthesesManagementPanel panel) {
    if (panel.getType() == ParenthesesManagementPanel.LEFT) {
      increaseLeftParensWithUpdate();
      setParenBalancingMode(JUST_ADDED_LEFT_PAREN);
      enableOverallAddRightParenMode();
    } else {
      increaseRightParensWithUpdate();
      setParenBalancingMode(JUST_ADDED_RIGHT_PAREN);
      enableOverallAddLeftParenMode();
    }
  }

  private void enableOverallAddRightParenMode() {
    parent.enableAddRightParenMode(this);
  }
  
  private void enableOverallAddLeftParenMode() {
    parent.enableAddLeftParenMode(this);
  }
  
  protected void enableParenDecreaseBalancingMode(ParenthesesManagementPanel panel) {
    if (panel.getType() == ParenthesesManagementPanel.LEFT && !(numLeftParens == 0)) {
      decreaseLeftParensWithUpdate();
      setParenBalancingMode(JUST_DELETED_LEFT_PAREN);
      enableOverallDeleteRightParenMode();
    } else if (!(numRightParens == 0)) {
      decreaseRightParensWithUpdate();
      setParenBalancingMode(JUST_DELETED_RIGHT_PAREN);
      enableOverallDeleteLeftParenMode();
    }
  }

  private void enableOverallDeleteRightParenMode() {
    parent.enableDeleteRightParenMode(this);
  }
  
  private void enableOverallDeleteLeftParenMode() {
    parent.enableDeleteLeftParenMode(this);
  }
  
  protected void disableParenIncreaseBalancingMode(ParenthesesManagementPanel panel) {
    if (panel.getType() == ParenthesesManagementPanel.LEFT) {
      increaseLeftParensWithUpdate();
      restoreOverallNormalParenMode();
    } else {
      increaseRightParensWithUpdate();
      restoreOverallNormalParenMode();
    }
  }
  
  protected void disableParenDecreaseBalancingMode(ParenthesesManagementPanel panel) {
    if (panel.getType() == ParenthesesManagementPanel.LEFT) {
      decreaseLeftParensWithUpdate();
      restoreOverallNormalParenMode();
    } else {
      decreaseRightParensWithUpdate();
      restoreOverallNormalParenMode();
    }
  }
  
  public void restoreOverallNormalParenMode() {
    parent.restoreOverallNormalMode();
  }
  
  protected void setParenBalancingMode(int balancingMode) {
    this.parenBalancingMode = balancingMode;
  }
  
  protected int getParenBalancingMode() {
    return parenBalancingMode;
  }
  
  private void createLeftParenDisplayPanel() {
    leftParenDisplayLabel = new Label();
    leftParenDisplayLabel.setWidth("5em");
    mainPanel.add(leftParenDisplayLabel);
  }
  
  private void createLeftParenManagementPanel() {
    leftParenManagementPanel = new ParenthesesManagementPanel(ParenthesesManagementPanel.LEFT, 
                                                    this, precedenceMouseDownHandler);
    mainPanel.add(leftParenManagementPanel);   
  }
  
  public void enableAddRightParenMode() {
    if (isValid) {
      setAllPanelsEnabled(false);
      rightParenManagementPanel.enableAddMode();
    }
  }
  
  public void enableAddLeftParenMode() {
    if (isValid) {
      setAllPanelsEnabled(false);
      leftParenManagementPanel.enableAddMode();
    }
  }
  
  public void enableDeleteRightParenMode() {
    if (isValid) {
      setAllPanelsEnabled(false);
      rightParenManagementPanel.setEnabled(false);
      if (!(numRightParens == 0)) {
        rightParenManagementPanel.enableDeleteMode();
      }
    }
  }
  
  public void enableDeleteLeftParenMode() {
    if (isValid) {
      setAllPanelsEnabled(false);
      if (!(numLeftParens == 0)) {
        leftParenManagementPanel.enableDeleteMode();
      }
    }
  }
  
  public void enableNormalMode() {
    if (isValid) {
      rightParenManagementPanel.restoreDefaultMode();
      leftParenManagementPanel.restoreDefaultMode();
      setAllPanelsEnabled(true);
    }
  }
  
  private void setAllPanelsEnabled(boolean isEnabled) {
    setConfigurablePanelsEnabled(isEnabled);
    setInitialEntryPanelsEnabled(isEnabled);
  }
  
  protected void increaseParens(ParenthesesManagementPanel panel) {
    if (panel.getType() == ParenthesesManagementPanel.LEFT) {
      increaseLeftParensWithUpdate();
    } else {
      increaseRightParensWithUpdate();
    }
  }

  protected void increaseLeftParensWithUpdate() {
    increaseNumLeftParens();
    updateExpression();
  }

  protected void increaseRightParensWithUpdate() {
    increaseNumRightParens();
    updateExpression();
  }
  
  protected void decreaseParens(ParenthesesManagementPanel panel) {
    if (panel.getType() == ParenthesesManagementPanel.LEFT) {
      decreaseLeftParensWithUpdate();
    } else {
      decreaseRightParensWithUpdate();
    }
  }

  protected void decreaseLeftParensWithUpdate() {
    decreaseNumLeftParens();
    updateExpression();
  }
  
  protected void decreaseRightParensWithUpdate() {
    decreaseNumRightParens();
    updateExpression();
  }
  
  private void increaseNumLeftParens() {
    ++numLeftParens;
    leftParenDisplayLabel.setText(new String(new char[numLeftParens]).replace("\0", "("));
  }
  
  private void decreaseNumLeftParens() {
    numLeftParens = (numLeftParens - 1 >= 0) ? numLeftParens - 1 : 0;
    leftParenDisplayLabel.setText(new String(new char[numLeftParens]).replace("\0", "("));
  }
  
  private void createRightParenDisplayPanel() {
    rightParenDisplayLabel = new Label();
    rightParenDisplayLabel.setWidth("5em");
    mainPanel.add(rightParenDisplayLabel);
  }
  
  private void createRightParenManagementPanel() {
    rightParenManagementPanel = new ParenthesesManagementPanel(ParenthesesManagementPanel.RIGHT, 
                                                     this, precedenceMouseDownHandler);
    mainPanel.add(rightParenManagementPanel);
  }
  
  private void increaseNumRightParens() {
    ++numRightParens;
    rightParenDisplayLabel.setText(new String(new char[numRightParens]).replace("\0", ")"));
  }
  
  private void decreaseNumRightParens() {
    numRightParens = (numRightParens - 1 >= 0) ? numRightParens - 1 : 0;
    rightParenDisplayLabel.setText(new String(new char[numRightParens]).replace("\0", ")"));
  }

  private void configurePanelForInput(String inputName) {
    List<InputDAO> inputs = getPrecedingInputsWithVarName(inputName);
    if (inputs != null && inputs.size() == 1 && !inputCannotBeConditionalized(inputs.get(0))) {
      predicatePanel.configureForInput(inputs.get(0));
      configureComparatorListBoxForInput(inputs.get(0));
      setExpressionValidity(true);
    } else {
      setExpressionValidity(false);
    }
  }
  
  private void setListBoxSelectedIndex(Integer value, ListBox listBox) {
    // Note: error-checking is done this way because ListBox objects do not throw
    // exceptions when given an illegal index.
    if (valueIsNullOrOutOfBounds(value, listBox)) {
      invalidateSelection();
    } else {
      listBox.setSelectedIndex(value);
    }
  }
  
  private boolean valueIsNullOrOutOfBounds(Integer value, ListBox listBox) {
    return value == null || value < 0 || value >= listBox.getItemCount();
  }
 
  @Override
  public void onChange(ChangeEvent event) {
    updateExpression();
  }

}
