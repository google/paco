package com.google.sampling.experiential.client;

import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.ui.Composite;
import com.google.paco.shared.model.ExperimentDAO;

public class ExperimentCreationNavigationTest extends GWTTestCase {

  private ExperimentDAO experiment;
  private ExperimentCreationPanel experimentCreationPanel;
  private int numSignalGroups;

  @Override
  public String getModuleName() {
    return "com.google.sampling.experiential.PacoEventserver";
  }

  protected void gwtSetUp() {
    experiment = CreationTestUtil.getEmptyExperiment();
    experimentCreationPanel = new ExperimentCreationPanel(experiment,
                                                           CreationTestUtil.createLoginInfo(),
                                                           null);
    experimentCreationPanel.createNewSignalGroup();
    numSignalGroups = experimentCreationPanel.getNumSignalGroups();
    assertEquals(numSignalGroups, 2);
  }

  public void testNextButtonOnDescriptionView() {
    checkNextButtonProgressedProperly(experimentCreationPanel.getDescriptionPanel(),
                                      experimentCreationPanel.getSignalPanelForSignalGroup(0));
  }

  public void testNextButtonOnScheduleView() {
    checkNextButtonProgressedProperly(experimentCreationPanel.getSignalPanelForSignalGroup(0),
                                      experimentCreationPanel.getInputsListPanelForSignalGroup(0));
  }

  public void testNextButtonOnNotLastInputsView() {
    checkNextButtonProgressedProperly(experimentCreationPanel.getInputsListPanelForSignalGroup(0),
                                      experimentCreationPanel.getSignalPanelForSignalGroup(1));
  }

  public void testNextButtonOnLastInputsView() {
    checkNextButtonProgressedProperly(experimentCreationPanel.getInputsListPanelForSignalGroup(numSignalGroups-1),
                                      experimentCreationPanel.getPublishingPanel());
  }

  public void testNextButtonOnPublishingView() {
    checkNextButtonProgressedProperly(experimentCreationPanel.getPublishingPanel(),
                                      experimentCreationPanel.getDescriptionPanel());
  }

  public void testPreviousButtonOnDescriptionView() {
    checkPreviousButtonRegressedProperly(experimentCreationPanel.getDescriptionPanel(),
                                      experimentCreationPanel.getPublishingPanel());
  }

  public void testPreviousButtonOnFirstScheduleView() {
    checkPreviousButtonRegressedProperly(experimentCreationPanel.getSignalPanelForSignalGroup(0),
                                         experimentCreationPanel.getDescriptionPanel());
  }

  public void testPreviousButtonOnNotFirstScheduleView() {
    checkPreviousButtonRegressedProperly(experimentCreationPanel.getSignalPanelForSignalGroup(1),
                                         experimentCreationPanel.getInputsListPanelForSignalGroup(0));
  }

  public void testPreviousButtonOnInputsView() {
    checkPreviousButtonRegressedProperly(experimentCreationPanel.getInputsListPanelForSignalGroup(0),
                                         experimentCreationPanel.getSignalPanelForSignalGroup(0));
  }

  public void testPreviousButtonOnPublishingView() {
    checkPreviousButtonRegressedProperly(experimentCreationPanel.getPublishingPanel(),
                                         experimentCreationPanel.getInputsListPanelForSignalGroup(numSignalGroups-1));
  }

  private void checkNextButtonProgressedProperly(Composite fromPanel, Composite toPanel) {
    experimentCreationPanel.showPanel(fromPanel);
    experimentCreationPanel.progressView();
    assertSame(experimentCreationPanel.getShowingPanel(), toPanel);
  }

  private void checkPreviousButtonRegressedProperly(Composite fromPanel, Composite toPanel) {
    experimentCreationPanel.showPanel(fromPanel);
    experimentCreationPanel.regressView();
    assertSame(experimentCreationPanel.getShowingPanel(), toPanel);
  }

}
