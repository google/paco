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


import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.allen_sauer.gwt.dnd.client.DragEndEvent;
import com.allen_sauer.gwt.dnd.client.DragHandler;
import com.allen_sauer.gwt.dnd.client.DragStartEvent;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import com.allen_sauer.gwt.dnd.client.drop.VerticalPanelDropController;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.paco.shared.model.InputDAO;

/**
 * A composite container for a bunch of InputPanel views.
 * 
 * @author Bob Evans
 *
 */
public class InputsListPanel extends Composite {
  
  private MyConstants myConstants;

  private AbsolutePanel rootPanel;
  private VerticalPanel mainPanel;
  private ExperimentDAO experiment;
  private PickupDragController dragController;

  // Visible for testing.
  protected LinkedList<InputsPanel> inputsPanelsList;
  
  private int signalGroupNum;
  
  // TODO: this is here for backwards compatibility. Remove later.
  public InputsListPanel(ExperimentDAO experiment) {
    this(experiment, 0);
  }

  public InputsListPanel(ExperimentDAO experiment, int signalGroupNum) {
    myConstants = GWT.create(MyConstants.class);
    
    this.experiment = experiment;
    this.signalGroupNum =  signalGroupNum;

    // An absolute panel is necessary for dragging.
    rootPanel = new AbsolutePanel();
    rootPanel.setSize("100%", "100%");
    initWidget(rootPanel);

    // Holds content.
    mainPanel = new VerticalPanel();
    mainPanel.setSpacing(2);
    mainPanel.add(createSignalGroupHeader());
    mainPanel.add(createInputsHeader());
    rootPanel.add(mainPanel);

    createInputsPanels(experiment);

    createDragController();
    for (InputsPanel panel : inputsPanelsList) {
      makeDraggable(panel);
    }
    createDragSpaceBuffer();
  }
  
  private Label createSignalGroupHeader() {
    String titleText = myConstants.experimentSingleSignalGroupHeaderText() + " " + signalGroupNum;
    Label lblExperimentSchedule = new Label(titleText);
    lblExperimentSchedule.setStyleName("paco-HTML-Large");
    return lblExperimentSchedule;
  }
  
  private HTML createInputsHeader() {
    HTML questionsPrompt = new HTML("<h2>" + myConstants.enterAtLeastOneQuestion() + "</h2>");
    questionsPrompt.setStyleName("keyLabel");
    return questionsPrompt;
  }

  public void deleteInput(InputsPanel inputsPanel) {
    if (inputsPanelsList.size() == 1) {
      return;
    }
    inputsPanelsList.remove(inputsPanel);
    mainPanel.remove(inputsPanel);
    updateExperimentInputs();
  }

  // Visible for testing
  protected void addInput(InputsPanel inputsPanel) {
    int index = inputsPanelsList.indexOf(inputsPanel);
    InputsPanel newInputsPanel = new InputsPanel(this, createEmptyInput());
    inputsPanelsList.add(index + 1, newInputsPanel);

    int widgetIndex = mainPanel.getWidgetIndex(inputsPanel);
    mainPanel.insert(newInputsPanel, widgetIndex + 1);

    makeDraggable(newInputsPanel);

    updateExperimentInputs();
  }
  
  public boolean checkListItemsHaveAtLeastOneOptionAndHighlight() {
    boolean requiredFieldsAreFilled = true;
    for (InputsPanel inputsPanel : inputsPanelsList) {
      if (!inputsPanel.checkListItemsHaveAtLeastOneOptionAndHighlight()) {
        requiredFieldsAreFilled = false;
        // Note: no break statement here since we need to continue
        // highlighting erroneous panels.
      }
    }
    return requiredFieldsAreFilled;
  }
  
  public boolean checkVarNamesFilledWithoutSpacesAndHighlight() {
    boolean varNamesHaveNoSpaces = true;
    for (InputsPanel inputsPanel : inputsPanelsList) {
      if (!inputsPanel.checkVarNameFilledWithoutSpacesAndHighlight()) {
        varNamesHaveNoSpaces = false;
        // Note: no break statement here since we need to continue
        // highlighting erroneous panels.
      }
    }
    return varNamesHaveNoSpaces;
  }

  private void createInputsPanels(ExperimentDAO experiment) {
    inputsPanelsList = new LinkedList<InputsPanel>();
    InputDAO[] inputs = experiment.getInputs();
    if (signalGroupNum != 0 || // TODO: for now high input group numbers have no meaning. Will change with signal groups.
        inputs == null || inputs.length == 0) {
      InputDAO emptyInputDAO = createEmptyInput();
      InputsPanel inputsPanel = new InputsPanel(this, emptyInputDAO);
      inputs = new InputDAO[] {emptyInputDAO};
      mainPanel.add(inputsPanel);
      inputsPanelsList.add(inputsPanel);
      experiment.setInputs(inputs);
    } else {
      for (int i = 0; i < inputs.length; i++) {
        InputsPanel inputsPanel = new InputsPanel(this, inputs[i]);
        mainPanel.add(inputsPanel);
        inputsPanelsList.add(inputsPanel);
      }
    }
  }

  private void createDragController() {
    dragController = new PickupDragController(rootPanel, false);
    dragController.setBehaviorMultipleSelection(false);
    dragController.setBehaviorDragStartSensitivity(5);
    dragController.addDragHandler(new InputsDragHandler());
    dragController.registerDropController(new VerticalPanelDropController(mainPanel));
  }

  // Creates bottom buffer on panel to prevent too much CSS collapse while dragging.
  private void createDragSpaceBuffer() {
    Label spacerLabel = new Label("");
    spacerLabel.setStylePrimaryName("vertical-Panel-Drag-Spacer");
    spacerLabel.setWidth("100%");
    spacerLabel.setHeight("60px");
    rootPanel.add(spacerLabel);
  }

  private InputDAO createEmptyInput() {
    return new InputDAO(null, null, null, "");
  }

  private void makeDraggable(InputsPanel panel) {
    dragController.makeDraggable(panel, panel.getDraggingPanel());
  }

  // TODO this is not very efficient.
  private void updateExperimentInputs() {
    InputDAO[] newInputs = new InputDAO[inputsPanelsList.size()];
    for (int i = 0; i < inputsPanelsList.size(); i++) {
      newInputs[i] = inputsPanelsList.get(i).getInput();
    }
    experiment.setInputs(newInputs);
  }

  /**
   * Drag handler to update inputs order after they have been rearranged.
   */
  private final class InputsDragHandler implements DragHandler {

    @Override
    public void onDragEnd(DragEndEvent event) { 
        updateInputPanelsList();
        updateExperimentInputs();
    }

    @Override
    public void onDragStart(DragStartEvent event) {
      // Nothing to be done here.
    }

    @Override
    public void onPreviewDragEnd(DragEndEvent event) throws VetoDragException {
      // Nothing to be done here.
    }

    @Override
    public void onPreviewDragStart(DragStartEvent event) throws VetoDragException {
      // Nothing to be done here.
    }
  }
  
  // Visible for testing
  protected void updateInputPanelsList() {
    Collections.sort(inputsPanelsList, new Comparator<InputsPanel>() {
      @Override
      public int compare(InputsPanel first, InputsPanel second) {
        // Sort input panels based on vertical position.
        return first.getAbsoluteTop() - second.getAbsoluteTop();
      }
    });
  }
  
  // Visible for testing
  protected LinkedList<InputsPanel> getInputsPanels() {
    return inputsPanelsList;
  }
  
  // Visible for testing
  protected VerticalPanel getContentPanel() {
    return mainPanel;
  }

  // TODO PRIYA this is slow
  public List<InputDAO> getPrecedingInputsWithVarName(String varName, InputDAO stopInput) {
    List<InputDAO> varNameInputs = new LinkedList<InputDAO>();
    for (InputDAO input : experiment.getInputs()) {
      if (input.equals(stopInput)) {
        break;
      } else if (input.getName().equals(varName)) {
        varNameInputs.add(input);
        // PRIYA - should I break or let it continue?
      }
    }
    return varNameInputs;
  }

}
