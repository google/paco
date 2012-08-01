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

  private Experiment constructExperiment() {
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

    return experiment;
  }

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
    Experiment experiment = constructExperiment();
    boolean created = DAO.getInstance().createExperiment(experiment);

    assertTrue(created);
    assertNotNull(experiment.getId());
    assertEquals(experiment.getVersion(), 1);
  }

  @Test
  public void textGetExperiment() {
    Experiment experiment = DAO.getInstance().getExperiment(1);

    assertNull(experiment);
  }

  @Test
  public void testGetExperimentAfterCreate() {
    Experiment experiment = constructExperiment();
    boolean created = DAO.getInstance().createExperiment(experiment);

    assertTrue(created);

    Experiment retrievedExperiment = DAO.getInstance().getExperiment(experiment.getId());

    assertNotNull(retrievedExperiment);
    assertEquals(experiment, retrievedExperiment);
  }

  @Test
  public void testUpdateExperiment() {
    Experiment experiment = constructExperiment();
    boolean updated = DAO.getInstance().updateExperiment(experiment);

    assertFalse(updated);
  }

  @Test
  public void testUpdateExperimentAfterCreate() {
    Experiment experiment = constructExperiment();
    boolean created = DAO.getInstance().createExperiment(experiment);
    long version = experiment.getVersion();

    assertTrue(created);

    experiment.setTitle("new title");
    boolean updated = DAO.getInstance().updateExperiment(experiment);

    assertTrue(updated);
    assertEquals(version + 1, experiment.getVersion());
  }

  @Test
  public void testGetExperimentAfterCreateAndUpdate() {
    Experiment experiment = constructExperiment();
    boolean created = DAO.getInstance().createExperiment(experiment);

    assertTrue(created);

    experiment.setTitle("new title");
    boolean updated = DAO.getInstance().updateExperiment(experiment);

    assertTrue(updated);

    Experiment retrievedExperiment = DAO.getInstance().getExperiment(experiment.getId());

    assertNotNull(retrievedExperiment);
    assertEquals(experiment, retrievedExperiment);
  }

  @Test
  public void testDeleteExperiment() {
    Experiment experiment = constructExperiment();
    boolean deleted = DAO.getInstance().deleteExperiment(experiment);

    assertFalse(deleted);
  }

  @Test
  public void testDeleteExperimentAfterCreate() {
    Experiment experiment = constructExperiment();
    boolean created = DAO.getInstance().createExperiment(experiment);
    long version = experiment.getVersion();

    assertTrue(created);

    boolean deleted = DAO.getInstance().deleteExperiment(experiment);

    assertTrue(deleted);
    assertTrue(experiment.isDeleted());
  }

  @Test
  public void testGetExperimentAfterCreateAndDelete() {
    Experiment experiment = constructExperiment();
    boolean created = DAO.getInstance().createExperiment(experiment);

    assertTrue(created);

    boolean deleted = DAO.getInstance().deleteExperiment(experiment);

    assertTrue(deleted);

    Experiment retrievedExperiment = DAO.getInstance().getExperiment(experiment.getId());

    assertNull(retrievedExperiment);
  }
}
