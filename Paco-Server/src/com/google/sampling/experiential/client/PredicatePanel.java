package com.google.sampling.experiential.client;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.paco.shared.model.InputDAO;

public class PredicatePanel extends Composite {

  private HorizontalPanel mainPanel;
  private ListBox predicateListBox;
  private TextBox predicateTextBox;

  private String responseType;
  private ConditionalExpressionPanel parent;

  public PredicatePanel(MouseDownHandler precedenceMouseDownHandler, ConditionalExpressionPanel parent) {
    this.parent = parent;
    
    mainPanel = new HorizontalPanel();
    initWidget(mainPanel);

    predicateListBox = new ListBox();
    predicateListBox.addMouseDownHandler(precedenceMouseDownHandler);
    predicateListBox.addChangeHandler(parent);

    predicateTextBox = new TextBox();
    predicateTextBox.addMouseDownHandler(precedenceMouseDownHandler);
    predicateTextBox.addChangeHandler(parent);

    mainPanel.add(predicateListBox);
  }

  public void configureForInput(InputDAO input) {
    Integer oldValue = getValueAsInt();
    String oldText = getSelectedText();
    mainPanel.clear();
    predicateListBox.clear();
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
      restoreListSelectedItem(oldText);
      mainPanel.add(predicateListBox);
    } else if (responseType.equals(InputDAO.LIKERT_SMILEYS)) {
      for (Integer i = 1; i <= 5; ++i) {
        predicateListBox.addItem(i.toString(), i.toString());
      }
      setListBoxSelectedIndex(oldValue, false);
      mainPanel.add(predicateListBox);
    } else if (responseTypeRequiresTextBox()) {
      predicateTextBox.setValue(oldValue.toString(), true);
      mainPanel.add(predicateTextBox);
    }
  }
  
  // TODO: this will have strange behavior if there are two list
  // items with the same text.
  private void restoreListSelectedItem(String oldText) {
    int selectedIndex = 0;
    for (int i = 0; i < predicateListBox.getItemCount(); ++i) {
      if (predicateListBox.getItemText(i).equals(oldText)) {
        selectedIndex = i;
        break;
      }
    }
    predicateListBox.setSelectedIndex(selectedIndex);
  }

  public void setEnabled(boolean isEnabled) {
    predicateListBox.setEnabled(isEnabled);
    predicateTextBox.setEnabled(isEnabled);
  }

  public String getValue() {
    if (responseType == null) {
      return null;
    }
    if (responseTypeRequiresListBox()) {
      return predicateListBox.getValue(predicateListBox.getSelectedIndex());
    } else if (responseTypeRequiresTextBox()) {
      return predicateTextBox.getValue();
    }
    return null;
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
  
  public void setValue(Integer value, boolean fireEvents) {
    if (value == null) {
      throw new IllegalArgumentException("Predicate value cannot be null.");
    }
    if (responseTypeRequiresListBox()) {
      setListBoxSelectedIndex(value, true);
      if (fireEvents) {
        ChangeEvent.fireNativeEvent(Document.get().createChangeEvent(), predicateListBox);
      }
    } else if (responseTypeRequiresTextBox()) {
      predicateTextBox.setValue(value.toString(), fireEvents);
    }
  }

  private boolean responseTypeRequiresTextBox() {
    return responseType.equals(InputDAO.NUMBER);
  }
  
  private boolean responseTypeRequiresListBox() {
    return responseType.equals(InputDAO.LIKERT) || responseType.equals(InputDAO.LIST)
        || responseType.equals(InputDAO.LIKERT_SMILEYS);
  }

  private void setListBoxSelectedIndex(Integer value, boolean shouldInvalidateSelection) {
    // Note: error-checking is done this way because ListBox objects do not throw
    // exceptions when given an illegal index.
    int selectedIndex = value - 1;
    boolean valueIsOutOfBounds = valueIsOutOfBounds(selectedIndex);
    if (valueIsOutOfBounds && shouldInvalidateSelection) {
      invalidateSelection();
    } else if (valueIsOutOfBounds) {
      predicateListBox.setSelectedIndex(0);
    } else if (!valueIsOutOfBounds) {
      predicateListBox.setSelectedIndex(selectedIndex);
    }
  }

  private boolean valueIsOutOfBounds(Integer value) {
    return value < 0 || value >= predicateListBox.getItemCount();
  }
  
  private void invalidateSelection() {
    parent.invalidateSelection();
  }

}
