package com.google.sampling.experiential.client;

import com.google.gwt.junit.client.GWTTestCase;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.sampling.experiential.ExperimentTestConstants;
import com.google.sampling.experiential.datastore.JsonConverter;

import junit.framework.TestCase;

public class ExperimentCreationValidationTest extends GWTTestCase {
  
  private ExperimentDefinitionPanel experimentDefinitionPanel;
  private InputsListPanel inputsListPanel;
  private DurationView durationPanel;
  
  protected void setUp() {
    ExperimentDAO experiment = JsonConverter.fromSingleEntityJson(ExperimentTestConstants.TEST_EXPERIMENT_0);
    experiment.setFixedDuration(true);
    experimentDefinitionPanel = new ExperimentDefinitionPanel(experiment, null, null);
    inputsListPanel = experimentDefinitionPanel.getInputsListPanel();
    durationPanel = experimentDefinitionPanel.getDurationPanel();
  }
  
  public void testValidExperimentIsSubmittable() {
    assertTrue(experimentDefinitionPanel.canSubmit());
  }
  
  public void testTitleIsMandatory() {
    experimentDefinitionPanel.setTitleInPanel("");
    assertFalse(experimentDefinitionPanel.canSubmit());
  }
  
  public void testInformedConsentIsMandatory() {
    experimentDefinitionPanel.setInformedConsentInPanel("");
    assertFalse(experimentDefinitionPanel.canSubmit());
  }

}
