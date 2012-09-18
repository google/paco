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


import java.util.Date;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.sampling.experiential.shared.InputDAO;

/**
 *
 * Panel for viewing/editing one Input object.
 *
 * @author Bob Evans
 *
 */
public class InputsPanel extends Composite {

  private InputsListPanel parent;
  private InputDAO input;
  private VerticalPanel mainPanel;
  private HorizontalPanel upperLinePanel;
  private HorizontalPanel lowerLinePanel;
  private ResponseViewPanel responseView;
  private HorizontalPanel conditionalPanel;
  private CheckBox conditionalBox;
  private VerticalPanel inputPromptTextPanel;

  public InputsPanel(InputsListPanel parent, InputDAO input) {
    this.input = input;
    this.parent = parent;
    mainPanel = new VerticalPanel();
    mainPanel.setSpacing(2);
    mainPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    initWidget(mainPanel);
    mainPanel.setWidth("258px");

    createLayout();
  }

  private void createLayout() {
    createInputFormLine();
    createListMgmtButtons();
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
    upperLinePanel.add(addButton);

    addButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        addInputsPanel();
      }
    });
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

  @SuppressWarnings("deprecation")
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

    //createScheduledDateColumn(upperLinePanel);
  }

  private void createResponseViewPanel() {
    responseView = new ResponseViewPanel(input);
    lowerLinePanel.add(responseView);
  }

  private void createConditionExpressionPanel() {
    conditionalPanel = new HorizontalPanel();
    mainPanel.add(conditionalPanel);
    conditionalPanel.setVisible(conditionalBox.getValue());

    Label conditionalExpressionLabel = new Label("Condition to enable this question:");
    conditionalExpressionLabel.setStyleName("keyLabel");
    conditionalPanel.add(conditionalExpressionLabel);

    final TextBox conditionText = new TextBox();
    conditionText.setText(input.getConditionExpression());
    conditionalPanel.add(conditionText);
    conditionText.addValueChangeHandler(new ValueChangeHandler() {
      @Override
      public void onValueChange(ValueChangeEvent arg0) {
        input.setConditionExpression(conditionText.getText());
      }
    });

    conditionalPanel.add(new HTML("   <span style='font-style:italic;font-size:small;"
        + "text-color:#888888;'>(e.g., q1name < 3)</span>"));
  }

  private void createConditionCheckboxColumn() {
    VerticalPanel cp = new VerticalPanel();
    upperLinePanel.add(cp);

    Label conditionalLabel = new Label("Conditional:");
    conditionalLabel.setStyleName("keyLabel");
    cp.add(conditionalLabel);

    conditionalBox = new CheckBox();
    conditionalBox.setValue(input.getConditional());
    cp.add(conditionalBox);

    conditionalBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent arg0) {
        input.setConditional(conditionalBox.getValue());
        conditionalPanel.setVisible(conditionalBox.getValue());
      }
    });
  }

  private void createRequiredCheckBoxColumn() {
    VerticalPanel mp = new VerticalPanel();
    upperLinePanel.add(mp);
    Label mandatoryLabel = new Label("Required:");
    mandatoryLabel.setStyleName("keyLabel");
    mp.add(mandatoryLabel);
    final CheckBox valueBox = new CheckBox();
    valueBox.setValue(input.getMandatory());
    mp.add(valueBox);
    valueBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent arg0) {
        input.setMandatory(valueBox.getValue());
      }
    });
  }

  private void createResponseTypeColumn() {
    VerticalPanel rp = new VerticalPanel();
    upperLinePanel.add(rp);
    Label responseTypeLabel = new Label("Response Type:");
    responseTypeLabel.setStyleName("keyLabel");
    rp.add(responseTypeLabel);

    final ListBox responseType = new ListBox();
    responseType.addItem(InputDAO.LIKERT_SMILEYS);    
    responseType.addItem(InputDAO.LIKERT);
    responseType.addItem(InputDAO.OPEN_TEXT);
    responseType.addItem(InputDAO.LIST);
    responseType.addItem(InputDAO.NUMBER);
    responseType.addItem(InputDAO.LOCATION);
    responseType.addItem(InputDAO.PHOTO);
    // responseType.addItem(InputDAO.SOUND);
    // responseType.addItem(InputDAO.ACTIVITY);
    responseType.setVisibleItemCount(1);
    int responseTypeSelectedIndex = 0;
    for (int i = 0; i < InputDAO.RESPONSE_TYPES.length; i++) {
      if (InputDAO.RESPONSE_TYPES[i].equals(input.getResponseType())) {
        responseTypeSelectedIndex = i;
        break;
      }
    }
    responseType.setItemSelected(responseTypeSelectedIndex, true);
    rp.add(responseType);

    responseType.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        input.setResponseType(responseType.getItemText(responseType.getSelectedIndex()));
        responseView.drawWidgetForInput(input);
        inputPromptTextPanel.setVisible(!input.isInvisibleInput());
      }
    });
  }

  private void createInputTextColumn() {
    inputPromptTextPanel = new VerticalPanel();
    upperLinePanel.add(inputPromptTextPanel);
    Label valueLabel = new Label("Text Prompt for Input:");
    valueLabel.setStyleName("keyLabel");
    inputPromptTextPanel.add(valueLabel);
    final TextBox valueText = new TextBox();
    valueText.setWidth("350px");
    if (input.getText() != null) {
      valueText.setText(input.getText());
    }
    inputPromptTextPanel.add(valueText);
    inputPromptTextPanel.setVisible(!input.isInvisibleInput());
    valueText.addValueChangeHandler(new ValueChangeHandler<String>() {

      @Override
      public void onValueChange(ValueChangeEvent<String> arg0) {
        input.setText(valueText.getText());
      }
    });
  }

  private void createVarNameColumn() {
    VerticalPanel varNamePanel = new VerticalPanel();
    upperLinePanel.add(varNamePanel);
    Label nameLabel = new Label("Name:");
    nameLabel.setStyleName("keyLabel");
    varNamePanel.add(nameLabel);

    final TextBox nameText = new TextBox();
    nameText.setWidth("75px");
    if (input.getName() != null) {
      nameText.setText(input.getName());
    }
    varNamePanel.add(nameText);

    nameText.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        input.setName(nameText.getText());
      }

    });
  }

//  private void createScheduledDateColumn(final HorizontalPanel upperLine) {
//    Date scheduledDate;
//    if (input.getScheduleDate() != null) {
//      scheduledDate = new Date(input.getScheduleDate());
//    } else {
//      scheduledDate = new Date();
//      input.setScheduleDate(scheduledDate.getTime());
//    }
//
//    VerticalPanel kp = new VerticalPanel();
//    upperLine.add(kp);
//    DateBox scheduleDatePicker = null;
//    Label datePickerLabel = new Label("Specific Date:");
//    // datePickerLabel.setWordWrap(true);
//    datePickerLabel.setWidth("80px");
//    datePickerLabel.setStyleName("keyLabel");
//    kp.add(datePickerLabel);
//
//    scheduleDatePicker = new DateBox();
//    scheduleDatePicker.setWidth("80px");
//    scheduleDatePicker.setFormat(new DateBox.DefaultFormat(DateTimeFormat.getShortDateFormat()));
//    scheduleDatePicker.setValue(scheduledDate);
//    kp.add(scheduleDatePicker);
//    scheduleDatePicker.addValueChangeHandler(new ValueChangeHandler<Date>() {
//
//      @Override
//      public void onValueChange(ValueChangeEvent<Date> arg0) {
//        input.setScheduleDate(arg0.getValue().getTime());
//      }
//
//    });
//  }
//

}
