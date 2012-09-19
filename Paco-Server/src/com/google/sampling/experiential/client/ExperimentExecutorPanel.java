package com.google.sampling.experiential.client;

import com.google.sampling.experiential.shared.ExperimentDAO;
import com.google.sampling.experiential.shared.InputDAO;
import com.google.sampling.experiential.shared.MapServiceAsync;

public class ExperimentExecutorPanel extends AbstractExperimentExecutorPanel {

  public ExperimentExecutorPanel(ExperimentListener listener, MapServiceAsync mapService, ExperimentDAO experiment) {
    super(listener, experiment,mapService);
  }

  protected void renderInputItems() {
    InputDAO[] inputs = experiment.getInputs();
    for (int i = 0; i < inputs.length; i++) {
      InputExecutorPanel inputsPanel = new InputExecutorPanel(inputs[i]);
      mainPanel.add(inputsPanel);
      inputsPanelsList.add(inputsPanel);
    }
  }

}