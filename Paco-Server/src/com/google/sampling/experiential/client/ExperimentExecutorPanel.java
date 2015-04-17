package com.google.sampling.experiential.client;

import java.util.List;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.sampling.experiential.shared.PacoServiceAsync;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.Input2;

public class ExperimentExecutorPanel extends AbstractExperimentExecutorPanel {

  private HorizontalPanel buttonPanel;
  private VerticalPanel groupsPanel;

  public ExperimentExecutorPanel(ExperimentListener listener, PacoServiceAsync mapService, ExperimentDAO experiment) {
    super(listener, experiment, mapService);
    createLayout();
  }

  @Override
  protected void createLayout() {
    createMainPanel();
    createExperimentHeader();
    buttonPanel = new HorizontalPanel();

    if (experiment.getGroups().size() == 1) {
      ExperimentGroup experimentGroup = experiment.getGroups().get(0);
      renderFormForGroup(experimentGroup);
    } else {
      renderGroupChooser();
      mainPanel.add(buttonPanel);
      renderCancelButton(buttonPanel);
    }
  }

  private void renderGroupChooser() {
    groupsPanel = new VerticalPanel();
    final ListBox groupsList = GWT.create(ListBox.class);
    for (ExperimentGroup group : experiment.getGroups()) {
      String name = group.getName();
      groupsList.addItem(name);
    }
    groupsList.addChangeHandler(new ChangeHandler() {
      public void onChange(ChangeEvent changeEvent) {
        ExperimentGroup selectedGroup = experiment.getGroups().get(groupsList.getSelectedIndex());
        setGroupChooserPanelVisibility(false);
        renderFormForGroup(selectedGroup );
      }
    });
    groupsPanel.add(groupsList);
    mainPanel.add(groupsPanel);

  }

  protected void setGroupChooserPanelVisibility(boolean b) {
    groupsPanel.setVisible(b);

  }

  private void renderFormForGroup(ExperimentGroup experimentGroup) {
    renderInputItems(experimentGroup);
    mainPanel.add(buttonPanel);
    renderSaveButton(buttonPanel);
    renderCancelButton(buttonPanel);
  }

  protected void renderInputItems(ExperimentGroup experimentGroup) {
    List<Input2> inputs = experimentGroup.getInputs();
    for (Input2 input : inputs) {
      InputExecutorPanel inputsPanel = new InputExecutorPanel(input);
      mainPanel.add(inputsPanel);
      inputsPanelsList.add(inputsPanel);
    }
  }

  @Override
  protected void renderInputItems() {
    throw new IllegalArgumentException("Blah, bad use of inheritance! TODO fix");

  }

}