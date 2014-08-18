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


import java.util.LinkedList;

import com.google.gwt.user.client.ui.Composite;
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
  private VerticalPanel mainPanel;
  private ExperimentDAO experiment;
  private LinkedList<InputsPanel> inputsPanelsList;

  public InputsListPanel(ExperimentDAO experiment) {
    this.experiment = experiment;
    mainPanel = new VerticalPanel();
    mainPanel.setSpacing(2);
    initWidget(mainPanel);

    inputsPanelsList = new LinkedList<InputsPanel>();
    InputDAO[] inputs = experiment.getInputs();
    if (inputs == null || inputs.length == 0) {
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

  public void deleteInput(InputsPanel inputsPanel) {
    if (inputsPanelsList.size() == 1) {
      return;
    }
    inputsPanelsList.remove(inputsPanel);
    mainPanel.remove(inputsPanel);
    updateExperimentInputs();
  }

  public void addInput(InputsPanel inputsPanel) {
    int index = inputsPanelsList.indexOf(inputsPanel);

    InputsPanel newInputsPanel = new InputsPanel(this, createEmptyInput());
    inputsPanelsList.add(index + 1, newInputsPanel);

    int widgetIndex = mainPanel.getWidgetIndex(inputsPanel);
    mainPanel.insert(newInputsPanel, widgetIndex + 1);

    updateExperimentInputs();
  }
  
  public boolean checkListItemsHaveAtLeatOneOptionAndHighlight() {
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

  /**
   * @return
   */
  private InputDAO createEmptyInput() {
    return new InputDAO(null, null, null, "");
  }

  // TODO this is not very efficient.
  private void updateExperimentInputs() {
    InputDAO[] newInputs = new InputDAO[inputsPanelsList.size()];
    for (int i = 0; i < inputsPanelsList.size(); i++) {
      newInputs[i] = inputsPanelsList.get(i).getInput();
    }
    experiment.setInputs(newInputs);
  }

}
