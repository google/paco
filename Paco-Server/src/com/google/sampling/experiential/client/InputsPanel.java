/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance  with the License.  
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasMouseDownHandlers;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.paco.shared.model.InputDAO;

/**
 * 
 * Panel for viewing/editing one Input object.
 * 
 * @author Bob Evans
 * 
 */
public class InputsPanel extends Composite implements MouseDownHandler {

  private InputsListPanel parent;
  private InputDAO input;
  private DraggableAbsolutePanel draggableRootPanel;
  private VerticalPanel mainPanel;
  private HorizontalPanel upperLinePanel;
  private HorizontalPanel lowerLinePanel;
  private ResponseViewPanel responseView;
  private HorizontalPanel conditionalPanel;
  private VerticalPanel inputPromptTextPanel;
  MyConstants myConstants = GWT.create(MyConstants.class);
  
  // Visible for testing
  protected TextBox varNameText;
  protected TextBox inputPromptText;
  protected CheckBox requiredBox;
  protected CheckBox conditionalBox;
  protected ListBox responseTypeListBox; 

  public InputsPanel(InputsListPanel parent, InputDAO input) {
    this.input = input;
    this.parent = parent;
    createDraggableRootPanel();
    createContentPanel();
    createLayout();
  }

  private void createDraggableRootPanel() {
    draggableRootPanel = new DraggableAbsolutePanel();
    initWidget(draggableRootPanel);
    causeDraggablePanelToRemoveFocusFromOtherWidgets();
  }

  private void causeDraggablePanelToRemoveFocusFromOtherWidgets() {
    draggableRootPanel.getElement().setAttribute("tabindex", "-1");
    /*
     * Hack: root panel must steal focus because dragging cancels default events (such as blur
     * events) on other widgets that may need to blur for their change handlers to fire. 
     * See gwt-dnd issue:
     * https://code.google.com/p/gwt-
     * dnd/issues/detail?id=117&can=1&q=sensitivity
     * &colspec=ID%20Type%20Status%20Priority%20Milestone%20Stars%20Summary
     */
    draggableRootPanel.addMouseDownHandler(new MouseDownHandler() {
      public void onMouseDown(MouseDownEvent event) {
        draggableRootPanel.getElement().focus();
        // So the user detects nothing.
        draggableRootPanel.getElement().blur();
      }
    });
  }

  private void createContentPanel() {
    mainPanel = new VerticalPanel();
    draggableRootPanel.add(mainPanel);
    mainPanel.setSpacing(2);
    mainPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    mainPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    mainPanel.setWidth("80%");
    mainPanel.setHeight("80%");
    mainPanel.setStyleName("paco-Input");
  }

  private void createLayout() {
    createInputFormLine();
    createListMgmtButtons();
  }

  public DraggableAbsolutePanel getDraggingPanel() {
    return draggableRootPanel;
  }

  private void createListMgmtButtons() {
    Button deleteButton = new Button("-");
    deleteButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        deleteThis();
      }
    });
    upperLinePanel.add(deleteButton);

    Button addButton = new Button("+");
    addButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        addInputsPanel();
      }
    });
    upperLinePanel.add(addButton);
  }

  protected void addInputsPanel() {
    parent.addInput(this);
  }

  private void deleteThis() {
    parent.deleteInput(this);
  }

  public InputDAO getInput() {
    return input;
  }

  private void createInputFormLine() {
    upperLinePanel = new HorizontalPanel();
    upperLinePanel.setStyleName("left");
    mainPanel.add(upperLinePanel);

    lowerLinePanel = new HorizontalPanel();
    mainPanel.add(lowerLinePanel);

    createResponseTypeColumn();
    createVarNameColumn();
    createInputTextColumn();

    createResponseViewPanel();

    createRequiredCheckBoxColumn();
    createConditionCheckboxColumn();
    createConditionExpressionPanel();
  }

  private void createResponseViewPanel() {
    responseView = new ResponseViewPanel(input, this);
    lowerLinePanel.add(responseView);
  }

  private void createConditionExpressionPanel() {
    conditionalPanel = new HorizontalPanel();
    mainPanel.add(conditionalPanel);
    conditionalPanel.setVisible(conditionalBox.getValue());

    Label conditionalExpressionLabel = new Label(myConstants.conditionalPrompt() + ":");
    conditionalExpressionLabel.setStyleName("keyLabel");
    conditionalPanel.add(conditionalExpressionLabel);

    final TextBox conditionText = new TextBox();
    conditionText.setText(input.getConditionExpression());
    conditionalPanel.add(conditionText);
    conditionText.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> arg0) {
        input.setConditionExpression(conditionText.getText());
      }
    });
    conditionText.addMouseDownHandler(this);

    conditionalPanel.add(new HTML("   <span style='font-style:italic;font-size:small;" + "text-color:#888888;'>" + "("
                                  + myConstants.eg() + ", " + "q1name < 3" + ")" + "</span>"));
  }

  private void createConditionCheckboxColumn() {
    VerticalPanel cp = new VerticalPanel();
    upperLinePanel.add(cp);

    Label conditionalLabel = new Label(myConstants.conditional() + ":");
    conditionalLabel.setStyleName("keyLabel");
    cp.add(conditionalLabel);

    conditionalBox = new CheckBox();
    conditionalBox.setValue(input.getConditional());
    cp.add(conditionalBox);

    conditionalBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> arg0) {
        input.setConditional(conditionalBox.getValue());
        conditionalPanel.setVisible(conditionalBox.getValue());
      }
    });
  }

  private void createRequiredCheckBoxColumn() {
    VerticalPanel mp = new VerticalPanel();
    upperLinePanel.add(mp);
    Label mandatoryLabel = new Label(myConstants.required() + ":");
    mandatoryLabel.setStyleName("keyLabel");
    mp.add(mandatoryLabel);
    requiredBox = new CheckBox();
    requiredBox.setValue(input.getMandatory());
    mp.add(requiredBox);
    requiredBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> arg0) {
        input.setMandatory(requiredBox.getValue());
      }
    });
  }

  private void createResponseTypeColumn() {
    VerticalPanel rp = new VerticalPanel();
    upperLinePanel.add(rp);
    Label responseTypeLabel = new Label(myConstants.responseType() + ":");
    responseTypeLabel.setStyleName("keyLabel");
    rp.add(responseTypeLabel);

    responseTypeListBox = new ListBox();
    responseTypeListBox.addItem(InputDAO.LIKERT_SMILEYS);    
    responseTypeListBox.addItem(InputDAO.LIKERT);
    responseTypeListBox.addItem(InputDAO.OPEN_TEXT);
    responseTypeListBox.addItem(InputDAO.LIST);
    responseTypeListBox.addItem(InputDAO.NUMBER);
    responseTypeListBox.addItem(InputDAO.LOCATION);
    responseTypeListBox.addItem(InputDAO.PHOTO);
//    responseType.addItem(InputDAO.SOUND);
//    responseType.addItem(InputDAO.ACTIVITY);
    responseTypeListBox.setVisibleItemCount(1);
    int responseTypeSelectedIndex = 0;
    for (int i = 0; i < InputDAO.RESPONSE_TYPES.length; i++) {
      if (InputDAO.RESPONSE_TYPES[i].equals(input.getResponseType())) {
        responseTypeSelectedIndex = i;
        break;
      }
    }
    responseTypeListBox.setItemSelected(responseTypeSelectedIndex, true);
    rp.add(responseTypeListBox);

    responseTypeListBox.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        input.setResponseType(responseTypeListBox.getItemText(responseTypeListBox.getSelectedIndex()));
        responseView.drawWidgetForInput(input);
        inputPromptTextPanel.setVisible(!input.isInvisibleInput());
      }
    });
    responseTypeListBox.addMouseDownHandler(this);
  }

  private void createInputTextColumn() {
    inputPromptTextPanel = new VerticalPanel();
    upperLinePanel.add(inputPromptTextPanel);
    Label valueLabel = new Label(myConstants.inputPromptPrompt() + ":");
    valueLabel.setStyleName("keyLabel");
    inputPromptTextPanel.add(valueLabel);
    inputPromptText = new TextBox();
    inputPromptText.setWidth("350px");
    if (input.getText() != null) {
      inputPromptText.setText(input.getText());
    }
    inputPromptTextPanel.add(inputPromptText);
    inputPromptTextPanel.setVisible(!input.isInvisibleInput());
    inputPromptText.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> arg0) {
        input.setText(inputPromptText.getText());
      }
    });
    inputPromptText.addMouseDownHandler(this);
  }

  private void createVarNameColumn() {
    VerticalPanel varNamePanel = new VerticalPanel();
    upperLinePanel.add(varNamePanel);
    Label nameLabel = new Label(myConstants.varName() + ":");
    nameLabel.setStyleName("keyLabel");
    varNamePanel.add(nameLabel);

    varNameText = new TextBox();
    varNameText.setWidth("75px");
    if (input.getName() != null) {
      varNameText.setText(input.getName());
    }
    varNamePanel.add(varNameText);

    varNameText.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        input.setName(varNameText.getText());
      }
    });

    varNameText.addMouseDownHandler(this);
  }

  /*
   * Applying InputsPanel to a widget as a MouseDown handler gives the widget
   * precedence for and exclusive access to its mouse down events (e.g. clicking
   * a text field widget should allow text entry rather than enabling dragging
   * via a MouseDownEvent on the widget's parent panel). Do not use for widgets
   * with a ClickHandler.
   */
  @Override
  public void onMouseDown(MouseDownEvent event) {
    event.stopPropagation();
  }

  // TODO: perhaps create generic mix-in class to allow most panel types to be
  // draggable.
  private class DraggableAbsolutePanel extends AbsolutePanel implements HasMouseDownHandlers {

    @Override
    public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
      return addDomHandler(handler, MouseDownEvent.getType());
    }
  }

}
