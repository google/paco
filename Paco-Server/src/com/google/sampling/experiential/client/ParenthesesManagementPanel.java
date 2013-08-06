package com.google.sampling.experiential.client;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;

public class ParenthesesManagementPanel extends Composite {
  
  public static final int LEFT = 0;
  public static final int RIGHT = 1;
  public static final String[] PAREN_TYPES = new String[]{"(",")"};
  
  private HorizontalPanel mainPanel;
  private ListBox listBox;
  private Button addButton;
  private Button deleteButton;
  
  private int parenType;
  private ConditionalExpressionPanel parent;
  private MouseDownHandler precedenceMouseDownHandler;
  
  public ParenthesesManagementPanel(int parenType, ConditionalExpressionPanel parent,
                                    MouseDownHandler precedenceMouseDownHandler) {
    this.parenType = parenType;
    this.parent = parent;
    this.precedenceMouseDownHandler = precedenceMouseDownHandler;
    
    mainPanel = new HorizontalPanel();
    initWidget(mainPanel);
    
    createListBoxAndAddToPanel();
    createAddButton();
    createDeleteButton();
  }

  private void createListBoxAndAddToPanel() {
    listBox = new ListBox();
    listBox.addItem("");
    listBox.addItem(PAREN_TYPES[parenType]);
    listBox.addItem("-");
    listBox.setVisibleItemCount(1);
    listBox.setSelectedIndex(0);
    listBox.addMouseDownHandler(precedenceMouseDownHandler);
    listBox.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        if (listBox.getSelectedIndex() == 1) {
          enableIncreaseBalancingMode();
        } else if (listBox.getSelectedIndex() == 2) {
          enableDecreaseBalancingMode();
        }
        listBox.setSelectedIndex(0);
      }
    });
    mainPanel.add(listBox);
  }
  
  private void createAddButton() {
    addButton = new Button(PAREN_TYPES[parenType]);
    addButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        disableIncreaseBalancingMode();
      }
    });
  }
  
  private void createDeleteButton() {
    deleteButton = new Button("-");
    deleteButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        disableDecreaseBalancingMode();
      }
    });
  }
  
  public int getType() {
    return parenType;
  }
  
  private void enableIncreaseBalancingMode() {
    parent.enableParenIncreaseBalancingMode(this);
  }
  
  private void enableDecreaseBalancingMode() {
    parent.enableParenDecreaseBalancingMode(this);
  }
  
  private void disableIncreaseBalancingMode() {
    parent.disableParenIncreaseBalancingMode(this);
  }
  
  private void disableDecreaseBalancingMode() {
    parent.disableParenDecreaseBalancingMode(this);
  }
  
  public void setEnabled(boolean isEnabled) {
    listBox.setEnabled(isEnabled);
    addButton.setEnabled(isEnabled);
    deleteButton.setEnabled(isEnabled);
  }
  
  public void enableAddMode() {
    setEnabled(true);
    mainPanel.clear();
    mainPanel.add(addButton);
  }
  
  public void enableDeleteMode() {
    setEnabled(true);
    mainPanel.clear();
    mainPanel.add(deleteButton);
  }
  
  public void restoreDefaultMode() {
    setEnabled(true);
    mainPanel.clear();
    mainPanel.add(listBox);
  }

}
