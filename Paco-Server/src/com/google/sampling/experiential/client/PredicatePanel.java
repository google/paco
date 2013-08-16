package com.google.sampling.experiential.client;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.paco.shared.model.InputDAO;

public class PredicatePanel extends Composite implements ChangeHandler {

  private MyConstants myConstants;
  private HorizontalPanel mainPanel;
  private String responseType;
  private ConditionalExpressionPanel parent;
  
  // Visible for testing
  protected ListBox predicateListBox;
  protected MouseOverTextBoxBase predicateTextBox;

  public PredicatePanel(MouseDownHandler precedenceMouseDownHandler, ConditionalExpressionPanel parent) {
    myConstants = GWT.create(MyConstants.class); 
    this.parent = parent;
    
    mainPanel = new HorizontalPanel();
    initWidget(mainPanel);

    predicateListBox = new ListBox();
    predicateListBox.addMouseDownHandler(precedenceMouseDownHandler);
    predicateListBox.addChangeHandler(this);

    predicateTextBox = new MouseOverTextBoxBase(MouseOverTextBoxBase.TEXT_BOX, myConstants.predicateError());
    predicateTextBox.addMouseDownHandler(precedenceMouseDownHandler);
    // Use value change events for error-checking.
    predicateTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        String text = predicateTextBox.getText();
        try {
          Integer conditionalNumber = Integer.parseInt(text);
          if (isValidNumberValue(conditionalNumber)) {
            ensurePredicateErrorNotFired();
          } else {
            handlePredicateError();
          }
        } catch (NumberFormatException nfe) {
          handlePredicateError();
        }    
      }
    });
    predicateTextBox.addChangeHandler(this);

    mainPanel.add(predicateListBox);
  }
  
  private void handlePredicateError() {
    ExperimentCreationPanel.setPanelHighlight(mainPanel, false);
    addPredicateError();
    predicateTextBox.enableMouseOver();
  }
  
  public void addPredicateError() {
    parent.addPredicateError();
  }
  
  private void ensurePredicateErrorNotFired() {
    ExperimentCreationPanel.setPanelHighlight(mainPanel, true);
    predicateTextBox.disableMouseOver();
    removePredicateError();
  }
  
  public void removePredicateError() {
    parent.removePredicateError();
  }

  public void configureForInput(InputDAO input) {
    // Get old values for value restoration purposes
    Integer oldValue = getValueAsInt();
    String oldText = getSelectedText();
    
    // Reset state.
    mainPanel.clear();
    predicateListBox.clear();
    ensurePredicateErrorNotFired();
    
    // Configure predicate item.  Restore old value if possible.
    // Otherwise, set a default value.
    responseType = input.getResponseType();
    if (responseType.equals(InputDAO.LIKERT)) {
      for (Integer i = 1; i <= input.getLikertSteps(); ++i) {
        predicateListBox.addItem(i.toString(), i.toString());
      }
      setListBoxSelectedIndex(oldValue, false);
      mainPanel.add(predicateListBox);
    } else if (responseType.equals(InputDAO.LIST)) {
      for (Integer i = 1; i <= input.getListChoices().length; ++i) {
        predicateListBox.addItem(input.getListChoices()[i - 1], i.toString());
      }
      restoreListSelectedItemOrDefault(oldText);
      mainPanel.add(predicateListBox);
    } else if (responseType.equals(InputDAO.LIKERT_SMILEYS)) {
      for (Integer i = 1; i <= 5; ++i) {
        predicateListBox.addItem(i.toString(), i.toString());
      }
      setListBoxSelectedIndex(oldValue, false);
      mainPanel.add(predicateListBox);
    } else if (responseTypeRequiresTextBox()) {
      setPredicateTextBoxValueOrDefault(oldValue);
      mainPanel.add(predicateTextBox);
    }
  }
  
  private void setPredicateTextBoxValueOrDefault(Integer oldValue) {
    if (isValidNumberValue(oldValue)) {
      predicateTextBox.setValue(oldValue.toString(), true);
    } else {
      predicateTextBox.setValue("0", true);
    }
  }

  private boolean isValidNumberValue(Integer oldValue) {
    return oldValue != null && oldValue >= 0;
  }
  
  // TODO: this will have strange behavior if there are two list
  // items with the same text.
  private void restoreListSelectedItemOrDefault(String oldText) {
    int selectedIndex = 0;
    for (int i = 0; i < predicateListBox.getItemCount(); ++i) {
      if (predicateListBox.getItemText(i).equals(oldText)) {
        selectedIndex = i;
        break;
      }
    }
    predicateListBox.setSelectedIndex(selectedIndex);
  }

  protected void setEnabled(boolean isEnabled) {
    predicateListBox.setEnabled(isEnabled);
    predicateTextBox.setEnabled(isEnabled);
  }

  public String getValue() {
    if (responseType == null) {
      return "";
    }
    if (responseTypeRequiresListBox()) {
      return getListBoxValue();
    } else if (responseTypeRequiresTextBox()) {
      return predicateTextBox.getValue();
    }
    // In case input cannot be conditionalized.
    return "";
  }

  private String getListBoxValue() {
    if (listBoxHasValidValue()) {
      return predicateListBox.getValue(predicateListBox.getSelectedIndex());
    }
    return null;
  }
  
  private boolean listBoxHasValidValue() {
    return predicateListBox.getSelectedIndex() != -1;
  }
  
  public int getValueAsInt() {
    String value = getValue();
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException nfe) {
      // Note: value being null causes NumberFormatException
      return 0;
    }
  }
  
  private String getSelectedText() {
    if (responseType == null) {
      return null;
    }
    if (responseTypeRequiresListBox()) {
      return getListBoxText();
    }
    return null;
  }

  private String getListBoxText() {
    if (listBoxHasValidValue()) {
      return predicateListBox.getItemText(predicateListBox.getSelectedIndex());
    }
    return null;
    
  }
  
  public int getTextBoxIntValue() {
    try {
      return Integer.parseInt(predicateTextBox.getValue());
    } catch (NumberFormatException nfe) {
      return 0;
    }
  }
  
  public void setValue(Integer value) {
    setValue(value, false);
  }

  public void setValue(Integer value, boolean fireUpdateEvents) {
    if (value == null) {
      throw new IllegalArgumentException("Predicate value cannot be null.");
    }
    if (responseTypeRequiresListBox()) {
      setListBoxSelectedIndex(value, true);
    } else if (responseTypeRequiresTextBox()) {
      predicateTextBox.setValue(value.toString(), true);
    }
    if (fireUpdateEvents) {
      updateExpression();
    }
  }

  private void updateExpression() {
    parent.updateExpression();
  }

  // Visible for testing
  protected boolean responseTypeRequiresTextBox() {
    return responseType.equals(InputDAO.NUMBER);
  }
  
  // Visible for testing
  protected boolean responseTypeRequiresListBox() {
    return responseType.equals(InputDAO.LIKERT) || responseType.equals(InputDAO.LIST)
        || responseType.equals(InputDAO.LIKERT_SMILEYS);
  }

  private void setListBoxSelectedIndex(Integer value, boolean shouldNotCushionError) {
    // Note: error-checking is done this way because ListBox objects do not throw
    // exceptions when given an illegal index.
    ensurePredicateErrorNotFired();
    int selectedIndex = value - 1;
    boolean valueIsOutOfBounds = valueIsOutOfBounds(selectedIndex);
    if (valueIsOutOfBounds && shouldNotCushionError) {
      handlePredicateError();
      predicateListBox.setSelectedIndex(-1); // No item is selected.
    } else if (valueIsOutOfBounds) {
      predicateListBox.setSelectedIndex(0);
    } else if (!valueIsOutOfBounds) {
      predicateListBox.setSelectedIndex(selectedIndex);
    }
  }

  private boolean valueIsOutOfBounds(Integer value) {
    return value < 0 || value >= predicateListBox.getItemCount();
  }

  @Override
  public void onChange(ChangeEvent event) {
    // This method only gets called when the user selects a text box item.
    // It is never called programmatically.  Thus, there can be no list box errors.
    if (event.getSource().equals(predicateListBox)) {
      ensurePredicateErrorNotFired();
    }
    updateExpression();
  }

}
