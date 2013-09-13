package com.google.sampling.experiential.client;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.paco.shared.model.ExperimentDAO;

public class ExperimentCreationDurationViewTest extends GWTTestCase {

  private static final String LATEST_DAY = "2013/30/07";
  private static final String LATER_DAY = "2013/25/07";
  private static final String EARLIER_DAY = "2013/24/07";

  private ExperimentCreationPanel experimentCreationPanel;

  private ExperimentDAO experiment;

  public String getModuleName() {
    return "com.google.sampling.experiential.PacoEventserver";
  }

  protected void gwtSetUp() {
    experiment = CreationTestUtil.createValidOngoingExperiment();
  }

  public void testDurationPanelAcceptsStartDateBeforeEndDate() {
    createValidExperimentCreationPanel();
    setDurationOnDurationPanel(EARLIER_DAY, LATER_DAY);
    assertTrue(experimentCreationPanel.canSubmit());
  }

  public void testDurationPanelAcceptsStartDateSameAsEndDate() {
    createValidExperimentCreationPanel();
    setDurationOnDurationPanel(EARLIER_DAY, EARLIER_DAY);
    assertTrue(experimentCreationPanel.canSubmit());
  }

  public void testDurationPanelDisallowsStartDateAfterEndDate() {
    createValidExperimentCreationPanel();
    setDurationOnDurationPanel(LATER_DAY, EARLIER_DAY);
    assertFalse(experimentCreationPanel.canSubmit());
  }

  public void testDurationPanelProtectsMovingStartDateAfterEndDate() {
    createValidExperimentCreationPanel();
    setDurationOnDurationPanel(EARLIER_DAY, LATER_DAY);
    setDurationPanelStartDate(LATEST_DAY);
    assertTrue(getDateFromString(experiment.getSignalGroups()[0].getStartDate()).equals(getDateFromString(LATEST_DAY)));
    assertTrue(getDateFromString(experiment.getSignalGroups()[0].getEndDate()).after(getDateFromString(LATEST_DAY)));
    assertTrue(experimentCreationPanel.canSubmit());
  }

  public void testDurationPanelRejectsMovingEndDateBeforeStartDate() {
    createValidExperimentCreationPanel();
    setDurationOnDurationPanel(LATER_DAY, LATEST_DAY);
    setDurationPanelEndDate(EARLIER_DAY);
    assertTrue(getDateFromString(experiment.getSignalGroups()[0].getStartDate()).equals(getDateFromString(LATER_DAY)));
    assertTrue(getDateFromString(experiment.getSignalGroups()[0].getEndDate()).after(getDateFromString(LATER_DAY)));
    assertFalse(experimentCreationPanel.canSubmit());
  }

  public void testDurationPanelAllowsMovingEndDateToStartDate() {
    createValidExperimentCreationPanel();
    setDurationOnDurationPanel(LATER_DAY, LATEST_DAY);
    setDurationPanelEndDate(LATER_DAY);
    assertTrue(getDateFromString(experiment.getSignalGroups()[0].getEndDate()).equals(getDateFromString(LATER_DAY)));
    assertTrue(experimentCreationPanel.canSubmit());
  }

  public void testDurationPanelErrorFixedByMovingEndDateAfterStartDate() {
    createValidExperimentCreationPanel();
    setDurationOnDurationPanel(LATER_DAY, EARLIER_DAY);
    assertFalse(experimentCreationPanel.canSubmit());
    setDurationPanelEndDate(LATEST_DAY);
    assertTrue(getDateFromString(experiment.getSignalGroups()[0].getStartDate()).equals(getDateFromString(LATER_DAY)));
    assertTrue(getDateFromString(experiment.getSignalGroups()[0].getEndDate()).equals(getDateFromString(LATEST_DAY)));
    assertTrue(experimentCreationPanel.canSubmit());
  }

  public void testDurationPanelErrorFixedByMovingEndDateToStartDate() {
    createValidExperimentCreationPanel();
    setDurationOnDurationPanel(LATER_DAY, EARLIER_DAY);
    assertFalse(experimentCreationPanel.canSubmit());
    setDurationPanelEndDate(LATER_DAY);
    assertTrue(getDateFromString(experiment.getSignalGroups()[0].getStartDate()).equals(getDateFromString(LATER_DAY)));
    assertTrue(getDateFromString(experiment.getSignalGroups()[0].getEndDate()).equals(getDateFromString(LATER_DAY)));
    assertTrue(experimentCreationPanel.canSubmit());
  }

  public void testDurationPanelErrorFixedByMovingStartDateBeforeEndDate() {
    createValidExperimentCreationPanel();
    setDurationOnDurationPanel(LATEST_DAY, LATER_DAY);
    assertFalse(experimentCreationPanel.canSubmit());
    setDurationPanelStartDate(EARLIER_DAY);
    assertTrue(getDateFromString(experiment.getSignalGroups()[0].getStartDate()).equals(getDateFromString(EARLIER_DAY)));
    assertTrue(getDateFromString(experiment.getSignalGroups()[0].getEndDate()).equals(getDateFromString(LATER_DAY)));
    assertTrue(experimentCreationPanel.canSubmit());
  }

  public void testDurationPanelErrorFixedByMovingStartDateToEndDate() {
    createValidExperimentCreationPanel();
    setDurationOnDurationPanel(LATEST_DAY, LATER_DAY);
    assertFalse(experimentCreationPanel.canSubmit());
    setDurationPanelStartDate(LATER_DAY);
    assertTrue(getDateFromString(experiment.getSignalGroups()[0].getStartDate()).equals(getDateFromString(LATER_DAY)));
    assertTrue(getDateFromString(experiment.getSignalGroups()[0].getEndDate()).equals(getDateFromString(LATER_DAY)));
    assertTrue(experimentCreationPanel.canSubmit());
  }

  public void testDurationPanelErrorFixedByMakingOngoing() {
    createValidExperimentCreationPanel();
    setDurationOnDurationPanel(LATEST_DAY, LATER_DAY);
    assertFalse(experimentCreationPanel.canSubmit());
    setOngoingDurationOnDurationPanel();
    assertTrue(experimentCreationPanel.canSubmit());
  }

  private void setDurationOnDurationPanel(String startDate, String endDate) {
    setFixedDurationOnDurationPanel();
    setDurationPanelStartDate(startDate);
    setDurationPanelEndDate(endDate);
  }

  private void setFixedDurationOnDurationPanel() {
    DurationView durationPanel = experimentCreationPanel.getDurationPanel();
    durationPanel.ensureValueChangeEventsWillFire();
    durationPanel.setFixedDuration(true);
  }

  private void setDurationPanelStartDate(String startDate) {
    DurationView durationPanel = experimentCreationPanel.getDurationPanel();
    durationPanel.ensureValueChangeEventsWillFire();
    durationPanel.setStartDate(startDate);
  }

  private void setDurationPanelEndDate(String endDate) {
    DurationView durationPanel = experimentCreationPanel.getDurationPanel();
    durationPanel.ensureValueChangeEventsWillFire();
    durationPanel.setEndDate(endDate);
  }

  private void setOngoingDurationOnDurationPanel() {
    DurationView durationPanel = experimentCreationPanel.getDurationPanel();
    durationPanel.ensureValueChangeEventsWillFire();
    durationPanel.setFixedDuration(false);
  }

  private Date getDateFromString(String dateString) {
    DateTimeFormat formatter = DateTimeFormat.getFormat(ExperimentCreationPanel.DATE_FORMAT);
    return formatter.parse(dateString);
  }

  private void createValidExperimentCreationPanel() {
    experimentCreationPanel = CreationTestUtil.createExperimentCreationPanel(experiment);
  }


}
