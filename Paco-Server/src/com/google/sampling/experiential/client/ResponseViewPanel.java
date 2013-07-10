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

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.paco.shared.model.InputDAO;


/**
 * Panel to hold the configuration of an Input's response details. 
 * 
 * @author Bob Evans
 *
 */
@SuppressWarnings("deprecation")
public class ResponseViewPanel extends Composite {

  private HorizontalPanel mainPanel;
  private InputDAO input;
  private TextBox stepsText;
  private TextBox leftSideText;
  private TextBox rightSideText;
  private ListChoicesPanel listChoicesPanel;

  public ResponseViewPanel(InputDAO input) {
    super();
    mainPanel = new HorizontalPanel();
    initWidget(mainPanel);
    drawWidgetForInput(input);
  }

  public void drawWidgetForInput(InputDAO input) {
    this.input = input;
    mainPanel.clear();
    String responseType = input.getResponseType();
    if (responseType == null  || responseType.equals(InputDAO.LIKERT_SMILEYS)
        || responseType.equals(InputDAO.OPEN_TEXT)) {
      mainPanel.setVisible(false);
    } else if (responseType.equals(InputDAO.LIKERT)) {
      drawLikertStepsPanel();
      mainPanel.setVisible(true);
    } else if (responseType.equals(InputDAO.LIST)) {
      drawListPanel();
      mainPanel.setVisible(true);
    } else {
      mainPanel.add(new HTML("Unknown Response Type"));
      mainPanel.setVisible(false);
    }
    HorizontalPanel line = new HorizontalPanel();
    line.setStyleName("left");
  }

  private void drawListPanel() {
    listChoicesPanel = new ListChoicesPanel(input);
    listChoicesPanel.setStyleName("left");
    mainPanel.add(listChoicesPanel);
  }
  
  public ListChoicesPanel getListChoicesPanel() {
    return listChoicesPanel;
  }

  private void drawLikertStepsPanel() {
    HorizontalPanel outer = new HorizontalPanel();
    outer.setStyleName("left");
    mainPanel.add(outer);
    outer.add(GWTUtil.createLabel("Number of steps in scale"));
    stepsText = new TextBox();
    outer.add(stepsText);

    outer.add(GWTUtil.createLabel("Left side label"));
    leftSideText = new TextBox();
    outer.add(leftSideText);
    outer.add(GWTUtil.createLabel("Right side label"));
    rightSideText = new TextBox();
    outer.add(rightSideText);
    setLikertValueInWidget();
    ChangeHandler handler = new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        getLikertStepsFromText();
      }

      private void getLikertStepsFromText() {
        try {
          Integer steps = Integer.valueOf(stepsText.getValue());
          input.setLikertSteps(steps);

          String leftSideLabel = leftSideText.getValue();
          input.setLeftSideLabel(leftSideLabel);

          String rightSideLabel = rightSideText.getValue();
          input.setRightSideLabel(rightSideLabel);
        } catch (NumberFormatException e) {
          input.setLikertSteps(InputDAO.DEFAULT_LIKERT_STEPS);
        }
      }
    };
    stepsText.addChangeHandler(handler);
    leftSideText.addChangeHandler(handler);
    rightSideText.addChangeHandler(handler);
  }

  private void setLikertValueInWidget() {
    if (input.getLikertSteps() == null) {
      input.setLikertSteps(InputDAO.DEFAULT_LIKERT_STEPS);
      stepsText.setValue(Integer.toString(InputDAO.DEFAULT_LIKERT_STEPS));
    } else {
      stepsText.setValue(Integer.toString(input.getLikertSteps()));
    }
    if (input.getLeftSideLabel() != null) {
      leftSideText.setValue(input.getLeftSideLabel());
    }
    if (input.getRightSideLabel() != null) {
      rightSideText.setValue(input.getRightSideLabel());
    }
  }

  public InputDAO getInput() {
    return input;
  }
}
