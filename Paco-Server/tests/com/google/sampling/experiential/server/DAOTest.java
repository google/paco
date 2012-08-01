// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.server;

import static org.junit.Assert.*;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.shared.DailySchedule;
import com.google.sampling.experiential.shared.Experiment;
import com.google.sampling.experiential.shared.Feedback;
import com.google.sampling.experiential.shared.LikertInput;
import com.google.sampling.experiential.shared.ListInput;
import com.google.sampling.experiential.shared.RandomSignal;
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
    experiment.setTitle("title");
    experiment.setDescription("description");
    experiment.setCreator("creator");
    experiment.setConsentForm("consent form");
    experiment.setPublished(true);
    experiment.setDeleted(false);
    experiment.setObservers(Lists.newArrayList("observer"));
    experiment.setSubjects(Lists.newArrayList("subject"));
    experiment.setInputs(Lists.newArrayList(new TextInput(), new ListInput(), new LikertInput()));
    experiment.setSignalSchedule(new RandomSignal(), new DailySchedule());
    experiment.setFeedbacks(Lists.newArrayList(new Feedback()));

    Long id = DAO.getInstance().createExperiment(experiment);

    Experiment experiment2 = DAO.getInstance().getExperiment(id);

    assertEquals(experiment, experiment2);
  }
}
