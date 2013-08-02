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

  private MyConstants myConstants;

  private ConditionalExpressionsPanel parent;
  private MouseDownHandler precedenceMouseDownHandler;

  private HorizontalPanel mainPanel;
  private ListBox operatorListBox;
  private TextBox varNameText;
  private ListBox comparatorListBox;
  private PredicatePanel predicatePanel;
  private ListBox addNextListBox;

  private boolean isValid;

  public ConditionalExpressionPanel(ConditionalExpressionsPanel parent, MouseDownHandler precedenceMouseDownHandler,
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
    createVarNameTextBox();
    createComparatorListBox();
    createPredicatePanel();
    createAddNextListBox();

    isValid = false;
  }
  
  public ConditionalExpressionPanel(ConditionalExpressionsPanel parent, MouseDownHandler precedenceMouseDownHandler,
                                    int initialOp, String inputName, int comparator, int predicate) {
    this(parent, precedenceMouseDownHandler, initialOp);
    varNameText.setValue(inputName);
    configurePanelForInput(inputName);
    if (isValid) {
      setComparatorListBoxSelectedIndex(comparator);
      predicatePanel.setValue(predicate);
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
      String varName = varNameText.getText();
      String comparator = comparatorListBox.getValue(comparatorListBox.getSelectedIndex());
      String predicate = predicatePanel.getValue();
      expression = Joiner.on(" ").join(op, varName, comparator, predicate);
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
    operatorListBox.addItem(myConstants.AND(), ConditionalExpressionsPanel.OPS[ConditionalExpressionsPanel.AND_OP]);
    operatorListBox.addItem(myConstants.OR(), ConditionalExpressionsPanel.OPS[ConditionalExpressionsPanel.OR_OP]);
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
    comparatorListBox.setEnabled(false);
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
    return input.getResponseType().equals(InputDAO.OPEN_TEXT) || input.getResponseType().equals(InputDAO.LOCATION)
        || input.getResponseType().equals(InputDAO.PHOTO);
  }

  private void setExpressionValidity(boolean isValid) {
    this.isValid = isValid;
    comparatorListBox.setEnabled(isValid);
    predicatePanel.setEnabled(isValid);
    addNextListBox.setEnabled(isValid);
  }

  private void createPredicatePanel() {
    predicatePanel = new PredicatePanel(precedenceMouseDownHandler, this);
    mainPanel.add(predicatePanel);
  }

  private void createAddNextListBox() {
    addNextListBox = new ListBox();
    addNextListBox.addItem("");
    addNextListBox.addItem("AND");
    addNextListBox.addItem("OR");
    addNextListBox.setVisibleItemCount(1);
    addNextListBox.setSelectedIndex(0);
    addNextListBox.addMouseDownHandler(precedenceMouseDownHandler);
    addNextListBox.setEnabled(false);
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
