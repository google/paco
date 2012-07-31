// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.server;

import static org.junit.Assert.*;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.shared.Experiment;
import com.google.sampling.experiential.shared.LikertInput;
import com.google.sampling.experiential.shared.ListInput;
import com.google.sampling.experiential.shared.TextInput;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class DAOTest {
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @Before
  public void setUp() {
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void testCreateExperiment() {
    Experiment experiment = new Experiment();
    experiment.setTitle("Test");
    experiment.setDescription("This is a test experiment.");
    experiment.setCreator("Google, Inc.");
    experiment.setConsentForm(null);
    experiment.setPublished(false);
    experiment.setDeleted(false);
    experiment.setObservers(Lists.newArrayList("corycornelius@google.com"));
    experiment.setSubjects(null);
    experiment.setInputs(Lists.newArrayList(
        new TextInput("happiness_text", false, null, "Are you happy?", false),
        new LikertInput("happiness_likert", false, null, "How happy are you?", null, false),
        new ListInput("happiness_list", false, null, "What makes you happy?", null, false)));
    experiment.setSchedule(null);
    experiment.setFeedbacks(null);

    Long id = DAO.getInstance().createExperiment(experiment);

    Experiment experiment2 = DAO.getInstance().getExperiment(id);

    assertEquals(experiment, experiment2);
  }
}
