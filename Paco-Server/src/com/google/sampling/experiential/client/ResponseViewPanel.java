/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.client;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.sampling.experiential.shared.Input;
import com.google.sampling.experiential.shared.LikertInput;
import com.google.sampling.experiential.shared.ListInput;
import com.google.sampling.experiential.shared.TextInput;


/**
 * Panel to hold the configuration of an Input's response details.
 *
 * @author Bob Evans
 *
 */
public class ResponseViewPanel extends Composite {

  private HorizontalPanel mainPanel;
  private Input input;

  // private TextBox stepsText;
  // private TextBox leftSideText;
  // private TextBox rightSideText;

  public ResponseViewPanel(Input input) {
    super();
    mainPanel = new HorizontalPanel();
    initWidget(mainPanel);
    drawWidgetForInput(input);
  }

  public void drawWidgetForInput(Input input) {
    this.input = input;
    mainPanel.clear();
    String responseType = input.getType();
    if (responseType.equals(Input.TEXT)) {
      drawTextPanel((TextInput) input);
      mainPanel.setVisible(true);
    } else if (responseType.equals(Input.LIKERT)) {
      drawLikertStepsPanel((LikertInput) input);
      mainPanel.setVisible(true);
    } else if (responseType.equals(Input.LIST)) {
      drawListPanel((ListInput) input);
      mainPanel.setVisible(true);
    } else {
      mainPanel.add(new HTML("Unknown Response Type"));
      mainPanel.setVisible(false);
    }
    HorizontalPanel line = new HorizontalPanel();
    line.setStyleName("left");
  }

  private void drawTextPanel(final TextInput textInput) {
    HorizontalPanel outer = new HorizontalPanel();
    outer.setStyleName("left");

    HorizontalPanel inputPromptTextPanel = new HorizontalPanel();
    Label valueLabel = new Label("Text Prompt for Input:");
    valueLabel.setStyleName("keyLabel");
    inputPromptTextPanel.add(valueLabel);
    final TextBox valueText = new TextBox();
    valueText.setWidth("350px");
    if (textInput.getQuestion() != null) {
      valueText.setText(textInput.getQuestion());
    }
    inputPromptTextPanel.add(valueText);
    valueText.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> arg0) {
        textInput.setQuestion(valueText.getText());
      }
    });
    outer.add(inputPromptTextPanel);

    VerticalPanel mp = new VerticalPanel();
    outer.add(mp);
    Label mandatoryLabel = new Label("Multiline:");
    mandatoryLabel.setStyleName("keyLabel");
    mp.add(mandatoryLabel);
    final CheckBox valueBox = new CheckBox();
    valueBox.setValue(input.isRequired());
    mp.add(valueBox);
    valueBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> arg0) {
        textInput.setMultiline(valueBox.getValue());
      }
    });
    outer.add(inputPromptTextPanel);

    mainPanel.add(outer);
  }

  private void drawListPanel(final ListInput listInput) {
    ListChoicesPanel outer = new ListChoicesPanel(listInput);
    outer.setStyleName("left");

    mainPanel.add(outer);
  }

  private void drawLikertStepsPanel(final LikertInput likertInput) {
    VerticalPanel outer = new VerticalPanel();

    HorizontalPanel inputPromptTextPanel = new HorizontalPanel();
    Label valueLabel = new Label("Text Prompt for Input:");
    valueLabel.setStyleName("keyLabel");
    inputPromptTextPanel.add(valueLabel);
    final TextBox valueText = new TextBox();
    valueText.setWidth("350px");
    if (likertInput.getQuestion() != null) {
      valueText.setText(likertInput.getQuestion());
    }
    inputPromptTextPanel.add(valueText);
    valueText.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> arg0) {
        likertInput.setQuestion(valueText.getText());
      }
    });
    outer.add(inputPromptTextPanel);

    HorizontalPanel likertPanel = new HorizontalPanel();
    likertPanel.add(GWTUtil.createLabel("Number of steps in scale"));
    TextBox stepsText = new TextBox();
    likertPanel.add(stepsText);

    likertPanel.add(GWTUtil.createLabel("Left side label"));
    TextBox leftSideText = new TextBox();
    likertPanel.add(leftSideText);
    likertPanel.add(GWTUtil.createLabel("Right side label"));
    TextBox rightSideText = new TextBox();
    likertPanel.add(rightSideText);
    // setLikertValueInWidget(likertInput);
    /*
     *if (likertInput.getLikertSteps() == null) {
     * stepsText.setValue(Integer.toString(LikertInput.DEFAULT_STEPS)); } else {
     * stepsText.setValue(Integer.toString(likertInput.getLikertSteps())); } if
     * (likertInput.getLeftSideLabel() != null) {
     * leftSideText.setValue(likertInput.getLeftSideLabel()); } if (likertInput.getRightSideLabel()
     * != null) { rightSideText.setValue(likertInput.getRightSideLabel()); }
     */
    //
    ChangeHandler handler = new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        getLikertStepsFromText();
      }

      private void getLikertStepsFromText() {
        /*
        try {
          Integer steps = Integer.valueOf(stepsText.getValue());
          likertInput.setLikertSteps(steps);

          String leftSideLabel = leftSideText.getValue();
          likertInput.setLeftSideLabel(leftSideLabel);

          String rightSideLabel = rightSideText.getValue();
          likertInput.setRightSideLabel(rightSideLabel);
        } catch (NumberFormatException e) {
          likertInput.setSteps(LikertInput.DEFAULT_STEPS);
        }
        */
      }
    };
    stepsText.addChangeHandler(handler);
    leftSideText.addChangeHandler(handler);
    rightSideText.addChangeHandler(handler);
    outer.add(likertPanel);

    mainPanel.add(outer);
  }

  public Input getInput() {
    return input;
  }
}
