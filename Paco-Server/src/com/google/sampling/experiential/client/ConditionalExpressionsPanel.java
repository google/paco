package com.google.sampling.experiential.client;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Composite;
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

  private MyConstants myConstants;

  private VerticalPanel mainPanel;
  private HorizontalPanel textEntryPanel;
  private VerticalPanel conditionalListPanel;
  private TextBox conditionDisplayTextBox;

  private InputDAO input;
  private InputsPanel parent;

  private List<ConditionalExpressionPanel> conditionPanels;
  private List<String> conditionalExpressions;

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
    createConditionalListPanel();
  }

  private void createTextEntryPanel() {
    textEntryPanel = new HorizontalPanel();
    mainPanel.add(textEntryPanel);

    Label conditionalExpressionLabel = new Label(myConstants.conditionalPrompt() + ": ");
    conditionalExpressionLabel.setStyleName("keyLabel");
    textEntryPanel.add(conditionalExpressionLabel);

    conditionDisplayTextBox = new TextBox();
    conditionDisplayTextBox.setVisibleLength(60);
    conditionDisplayTextBox.setText(input.getConditionExpression());
    conditionDisplayTextBox.addMouseDownHandler(parent);
    conditionDisplayTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> arg0) {
        input.setConditionExpression(conditionDisplayTextBox.getText());
      }
    });
    textEntryPanel.add(conditionDisplayTextBox);

    textEntryPanel.add(new HTML("   <span style='font-style:italic;font-size:small;" + "text-color:#888888;'>" + "("
        + myConstants.eg() + ", " + "q1name < 3" + ")" + "</span>"));
  }

  private void createConditionalListPanel() {
    conditionalListPanel = new VerticalPanel();
    mainPanel.add(conditionalListPanel);
    ConditionalExpressionPanel conditionalPanel = new ConditionalExpressionPanel(this, parent, NO_OP);
    conditionalListPanel.add(conditionalPanel);
    conditionPanels.add(conditionalPanel);
    conditionalExpressions.add(conditionalPanel.constructExpression());
  }

  public List<InputDAO> getPrecedingInputsWithVarName(String varName) {
    return parent.getPrecedingInputsWithVarName(varName);
  }

  public void addConditionalPanel(ConditionalExpressionPanel conditionalExpressionPanel, int conditionalOp) {

    ConditionalExpressionPanel newPanel = new ConditionalExpressionPanel(this, parent, conditionalOp);

    int index = conditionPanels.indexOf(conditionalExpressionPanel);
    conditionPanels.add(index + 1, newPanel);
    conditionalExpressions.add(index + 1, newPanel.constructExpression());

    int widgetIndex = conditionalListPanel.getWidgetIndex(conditionalExpressionPanel);
    conditionalListPanel.insert(newPanel, widgetIndex + 1);

    updateExpression(newPanel);
  }

  public void updateExpression(ConditionalExpressionPanel conditionalExpressionPanel) {
    int index = conditionPanels.indexOf(conditionalExpressionPanel);
    conditionalExpressions.set(index, conditionalExpressionPanel.constructExpression());
    updateDisplayedExpression();
  }

  private void updateDisplayedExpression() {
    String expression = Joiner.on(" ").join(conditionalExpressions);
    conditionDisplayTextBox.setValue(expression, true);
  }

}
