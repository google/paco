package com.google.sampling.experiential.client;

import com.google.paco.shared.model.ExperimentDAO;
import com.google.paco.shared.model.InputDAO;
import com.google.paco.shared.model.SignalGroupDAO;
import com.google.sampling.experiential.shared.PacoServiceAsync;

public class ExperimentExecutorPanel extends AbstractExperimentExecutorPanel {

  public ExperimentExecutorPanel(ExperimentListener listener, PacoServiceAsync mapService, ExperimentDAO experiment) {
    super(listener, experiment,mapService);
    createLayout();
  }

  protected void renderInputItems() {
    // TODO add a selector for which signalgroup to respond to.
    // Or, at least, wrap each signal group in a panel and title it.
    // then add that to the responses.
    SignalGroupDAO[] signalGroups = experiment.getSignalGroups();
    for (SignalGroupDAO signalGroupDAO : signalGroups) {
      InputDAO[] inputs = signalGroupDAO.getInputs();
      for (int i = 0; i < inputs.length; i++) {
        InputExecutorPanel inputsPanel = new InputExecutorPanel(inputs[i]);
        mainPanel.add(inputsPanel);
        inputsPanelsList.add(inputsPanel);
      }
    }
  }

}