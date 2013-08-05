package com.google.sampling.experiential.client;

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
    mainPanel.clear();
    predicateListBox.clear();
    responseType = input.getResponseType();
    if (responseType.equals(InputDAO.LIKERT)) {
      for (Integer i = 1; i <= input.getLikertSteps(); ++i) {
        predicateListBox.addItem(i.toString(), i.toString());
      }
      predicateListBox.setSelectedIndex(0);
      mainPanel.add(predicateListBox);
    } else if (responseType.equals(InputDAO.LIST)) {
      for (Integer i = 1; i <= input.getListChoices().length; ++i) {
        predicateListBox.addItem(input.getListChoices()[i - 1], i.toString());
      }
      predicateListBox.setSelectedIndex(0);
      mainPanel.add(predicateListBox);
    } else if (responseType.equals(InputDAO.LIKERT_SMILEYS)) {
      for (Integer i = 1; i <= 5; ++i) {
        predicateListBox.addItem(i.toString(), i.toString());
      }
      predicateListBox.setSelectedIndex(0);
      mainPanel.add(predicateListBox);
    } else if (responseTypeRequiresTextBox()) {
      predicateTextBox.setValue("0", true);
      mainPanel.add(predicateTextBox);
    }
  }

  public void setEnabled(boolean isEnabled) {
    predicateListBox.setEnabled(isEnabled);
    predicateTextBox.setEnabled(isEnabled);
  }

  public String getValue() {
    if (responseTypeRequiresListBox()) {
      return predicateListBox.getValue(predicateListBox.getSelectedIndex());
    } else if (responseTypeRequiresTextBox()) {
      return predicateTextBox.getValue();
    }
    return "";
  }
  
  public void setValue(Integer value) {
    if (value == null) {
      throw new IllegalArgumentException("Predicate value cannot be null.");
    }
    if (responseTypeRequiresListBox()) {
      setListBoxSelectedIndex(value);
    } else if (responseTypeRequiresTextBox()) {
      predicateTextBox.setValue(value.toString());
    }
  }

  private boolean responseTypeRequiresTextBox() {
    return responseType.equals(InputDAO.NUMBER);
  }
  
  private boolean responseTypeRequiresListBox() {
    return responseType.equals(InputDAO.LIKERT) || responseType.equals(InputDAO.LIST)
        || responseType.equals(InputDAO.LIKERT_SMILEYS);
  }

  private void setListBoxSelectedIndex(Integer value) {
    // Note: error-checking is done this way because ListBox objects do not throw
    // exceptions when given an illegal index.
    int selectedIndex = value - 1;
    if (valueIsOutOfBounds(selectedIndex)) {
      invalidateSelection();
    } else {
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
