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


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.sampling.experiential.shared.Experiment;
import com.google.sampling.experiential.shared.Input;
import com.google.sampling.experiential.shared.TextInput;

/**
 * A composite container for a bunch of InputPanel views.
 * 
 * @author Bob Evans
 *
 */
public class InputsListPanel extends Composite {
  private VerticalPanel mainPanel;
  private Experiment experiment;
  private LinkedList<InputsPanel> inputsPanelsList;

  public InputsListPanel(Experiment experiment) {
    this.experiment = experiment;
    mainPanel = new VerticalPanel();
    mainPanel.setSpacing(2);
    initWidget(mainPanel);

    inputsPanelsList = new LinkedList<InputsPanel>();
    List<Input> inputs = experiment.getInputs();
    if (inputs == null || inputs.size() == 0) {
      Input emptyInput = createEmptyInput();
      InputsPanel inputsPanel = new InputsPanel(this, emptyInput);
      inputs = Lists.newArrayList(emptyInput);
      mainPanel.add(inputsPanel);
      inputsPanelsList.add(inputsPanel);
      experiment.setInputs(inputs);
    } else {
      for (int i = 0; i < inputs.size(); i++) {
        InputsPanel inputsPanel = new InputsPanel(this, inputs.get(i));
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

  /**
   * @return
   */
  private Input createEmptyInput() {
    return new TextInput();
  }

  // TODO this is not very efficient.
  private void updateExperimentInputs() {
    List<Input> newInputs = new ArrayList<Input>(inputsPanelsList.size());
    for (int i = 0; i < inputsPanelsList.size(); i++) {
      newInputs.add(inputsPanelsList.get(i).getInput());
    }
    experiment.setInputs(newInputs);
  }

}
