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
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.paco.shared.model.InputDAO;


/**
 * Panel to hold the configuration of an Input's response details. 
 * 
 * @author Bob Evans
 *
 */
@SuppressWarnings("deprecation")
public class ResponseViewPanel extends Composite {
  private MyConstants myConstants;
  
  private HorizontalPanel mainPanel;
  private InputDAO input;
  private TextBox leftSideText;
  private TextBox rightSideText;
  
  private InputsPanel parent;
  
  // Visible for testing
  protected ListChoicesPanel listChoicesPanel;
  protected MouseOverTextBoxBase stepsText;

  public ResponseViewPanel(InputDAO input, InputsPanel parent) {
    super();
    myConstants = GWT.create(MyConstants.class);
    this.parent = parent;
    mainPanel = new HorizontalPanel();
    initWidget(mainPanel);
    drawWidgetForInput(input);
  }

  public void drawWidgetForInput(InputDAO input) {
    this.input = input;
    mainPanel.clear();
    
    // Note: since response view panel content is re-drawn each time response type
    // is changed, we need only remove errors and do not need to check the
    // re-drawn panels for errors.
    removeResponseTypeErrors();
    
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

  private void removeResponseTypeErrors() {
    if (stepsText != null) {
      parent.removeLikertStepsError(stepsText.getMessage());
    } 
    if (listChoicesPanel != null) {
      parent.removeFirstListChoiceError(listChoicesPanel.getListChoiceErrorMessage());
    }
  }

  private void drawListPanel() {
    listChoicesPanel = new ListChoicesPanel(input, parent, this);
    listChoicesPanel.setStyleName("left");
    mainPanel.add(listChoicesPanel);
  }
  
  public void checkListChoicesAreNotEmptyAndHighlight() {
    listChoicesPanel.checkListChoicesAreNotEmptyAndHighlight();
  }
  
  public void ensureListChoicesErrorNotFired() {
    if (listChoicesPanel != null) {
      listChoicesPanel.ensureListChoicesErrorNotFired();
    }
  }
  
  public ListChoicesPanel getListChoicesPanel() {
    return listChoicesPanel;
  }

  private void drawLikertStepsPanel() {
    HorizontalPanel outer = new HorizontalPanel();
    outer.setStyleName("left");
    mainPanel.add(outer);
    outer.add(GWTUtil.createLabel("Number of steps in scale"));
    stepsText = new MouseOverTextBoxBase(MouseOverTextBoxBase.TEXT_BOX);
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
          removeLikertStepsError();

          String leftSideLabel = leftSideText.getValue();
          input.setLeftSideLabel(leftSideLabel);

          String rightSideLabel = rightSideText.getValue();
          input.setRightSideLabel(rightSideLabel);
        } catch (NumberFormatException e) {
//          input.setLikertSteps(InputDAO.DEFAULT_LIKERT_STEPS);
          handleLikertStepsError();
        } catch (IllegalArgumentException e) {
          handleLikertStepsError();
        }
      }

      private void removeLikertStepsError() {
        ExperimentCreationPanel.setPanelHighlight(stepsText, true);
        stepsText.disableMouseOver();
        parent.removeLikertStepsError(stepsText.getMessage());
      }

      private void handleLikertStepsError() {
        ExperimentCreationPanel.setPanelHighlight(stepsText, false);
        stepsText.enableMouseOver();
        parent.addLikertStepsError(stepsText.getMessage());
      }
    };
    stepsText.setMessage(myConstants.likertStepsMustBeValid());
    stepsText.addChangeHandler(handler);
    leftSideText.addChangeHandler(handler);
    rightSideText.addChangeHandler(handler);
    
    // Let InputsPanel handle mouse down events due to InputsPanel draggability.
    stepsText.addMouseDownHandler(parent);
    leftSideText.addMouseDownHandler(parent);
    rightSideText.addMouseDownHandler(parent);
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

  public void addFirstListChoiceError(String message) {
    parent.addFirstListChoiceError(message);
  }

  public void removeFirstListChoiceError(String message) {
    parent.removeFirstListChoiceError(message);
  }
}
