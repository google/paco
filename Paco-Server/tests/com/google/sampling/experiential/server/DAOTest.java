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

import java.util.List;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class DAOTest {
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  private final DAO dao = DAO.getInstance();

  private Experiment constructExperiment() {
    Experiment experiment = new Experiment();

    experiment.setTitle("title");
    experiment.setDescription("description");
    experiment.setCreator("creator");
    experiment.setConsentForm("consent form");
    experiment.setPublished(false);
    experiment.setDeleted(false);
    experiment.setObservers(null);
    experiment.setSubjects(null);
    experiment.setViewers(null);
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

    assertTrue(dao.createExperiment(experiment));
    assertNotNull(experiment.getId());
    assertEquals(1, experiment.getVersion());
  }

  @Test
  public void textGetExperiment() {
    Experiment experiment = dao.getExperiment(1);

    assertNull(experiment);
  }

  @Test
  public void testGetExperimentAfterCreate() {
    Experiment experiment = constructExperiment();

    assertTrue(dao.createExperiment(experiment));

    Experiment retrievedExperiment = dao.getExperiment(experiment.getId());

    assertNotNull(retrievedExperiment);
    assertEquals(experiment, retrievedExperiment);
  }

  @Test
  public void testUpdateExperiment() {
    Experiment experiment = constructExperiment();

    assertFalse(dao.updateExperiment(experiment));
  }

  @Test
  public void testUpdateExperimentAfterCreate() {
    Experiment experiment = constructExperiment();

    assertTrue(dao.createExperiment(experiment));

    long version = experiment.getVersion();
    experiment.setTitle("new title");

    assertTrue(dao.updateExperiment(experiment));
    assertEquals(version + 1, experiment.getVersion());
  }

  @Test
  public void testGetExperimentAfterCreateAndUpdate() {
    Experiment experiment = constructExperiment();

    assertTrue(dao.createExperiment(experiment));

    experiment.setTitle("new title");

    assertTrue(dao.updateExperiment(experiment));

    Experiment retrievedExperiment = dao.getExperiment(experiment.getId());

    assertNotNull(retrievedExperiment);
    assertEquals(experiment, retrievedExperiment);
  }

  @Test
  public void testDeleteExperiment() {
    Experiment experiment = constructExperiment();

    assertFalse(dao.deleteExperiment(experiment));
  }

  @Test
  public void testDeleteExperimentAfterCreate() {
    Experiment experiment = constructExperiment();

    assertTrue(dao.createExperiment(experiment));
    assertTrue(dao.deleteExperiment(experiment));
    assertTrue(experiment.isDeleted());
  }

  @Test
  public void testGetExperimentAfterCreateAndDelete() {
    Experiment experiment = constructExperiment();

    assertTrue(dao.createExperiment(experiment));
    assertTrue(dao.deleteExperiment(experiment));

    Experiment retrievedExperiment = dao.getExperiment(experiment.getId());

    assertNull(retrievedExperiment);
  }

  @Test
  public void testGetObserverExperiments() {
    List<Experiment> experiments = dao.getObserverExperiments("user");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetObserverExperimentsAfterCreate() {
    Experiment experiment = constructExperiment();
    experiment.setObservers(Lists.newArrayList("user"));

    assertTrue(dao.createExperiment(experiment));

    List<Experiment> experiments = dao.getObserverExperiments("user");

    assertEquals(1, experiments.size());
  }

  @Test
  public void testGetObserverExperimentsAfterCreateAndDelete() {
    Experiment experiment = constructExperiment();
    experiment.setObservers(Lists.newArrayList("user"));

    assertTrue(dao.createExperiment(experiment));
    assertTrue(dao.deleteExperiment(experiment));

    List<Experiment> experiments = dao.getObserverExperiments("user");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetSubjectExperiments() {
    List<Experiment> experiments = dao.getSubjectExperiments("user");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetSubjectExperimentsAfterCreateUnpublished() {
    Experiment experiment = constructExperiment();
    experiment.setSubjects(Lists.newArrayList("user"));
    experiment.setPublished(false);

    assertTrue(dao.createExperiment(experiment));

    List<Experiment> experiments = dao.getSubjectExperiments("user");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetSubjectExperimentsAfterCreatePublished() {
    Experiment experiment = constructExperiment();
    experiment.setSubjects(Lists.newArrayList("user"));
    experiment.setPublished(true);

    assertTrue(dao.createExperiment(experiment));

    List<Experiment> experiments = dao.getSubjectExperiments("user");

    assertEquals(1, experiments.size());
    assertEquals(experiment, experiments.get(0));
  }

  @Test
  public void testGetSubjectExperimentsAfterCreateAndDelete() {
    Experiment experiment = constructExperiment();
    experiment.setSubjects(Lists.newArrayList("user"));

    assertTrue(dao.createExperiment(experiment));
    assertTrue(dao.deleteExperiment(experiment));

    List<Experiment> experiments = dao.getSubjectExperiments("user");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testJoinExperiment() {
    Experiment experiment = constructExperiment();

    assertFalse(dao.joinExperiment("user", experiment, null));
  }

  @Test
  public void testJoinExperimentAfterCreate() {
    Experiment experiment = constructExperiment();

    assertTrue(dao.createExperiment(experiment));
    assertTrue(dao.joinExperiment("user", experiment, null));
  }

  @Test
  public void testLeaveExperiment() {
    Experiment experiment = constructExperiment();

    assertFalse(dao.leaveExperiment("user", experiment));
  }

  @Test
  public void testLeaveExperimentAfterCreate() {
    Experiment experiment = constructExperiment();

    assertTrue(dao.createExperiment(experiment));
    assertFalse(dao.leaveExperiment("user", experiment));
  }

  @Test
  public void testLeaveExperimentAfterCreateAndJoin() {
    Experiment experiment = constructExperiment();

    assertTrue(dao.createExperiment(experiment));
    assertTrue(dao.joinExperiment("user", experiment, null));
    assertTrue(dao.leaveExperiment("user", experiment));
  }

  @Test
  public void testGetExperiments() {
    List<Experiment> experiments = dao.getExperiments("user");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetExperimentsAfterCreatePublishedPublic() {
    Experiment experiment = constructExperiment();
    experiment.setPublished(true);
    experiment.setViewers(null);

    assertTrue(dao.createExperiment(experiment));

    List<Experiment> experiments = dao.getExperiments("user");

    assertEquals(1, experiments.size());
  }

  @Test
  public void testGetExperimentsAfterCreatePublishedPrivate() {
    Experiment experiment = constructExperiment();
    experiment.setPublished(true);
    experiment.setViewers(Lists.newArrayList("user1"));

    assertTrue(dao.createExperiment(experiment));

    List<Experiment> experiments = dao.getExperiments("user2");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetExperimentsAfterCreatePublishedPrivateViewable() {
    Experiment experiment = constructExperiment();
    experiment.setPublished(true);
    experiment.setViewers(Lists.newArrayList("user"));

    assertTrue(dao.createExperiment(experiment));

    List<Experiment> experiments = dao.getExperiments("user");

    assertEquals(1, experiments.size());
  }

  @Test
  public void testGetExperimentsAfterCreateUnpublishedPublic() {
    Experiment experiment = constructExperiment();
    experiment.setPublished(false);
    experiment.setViewers(null);

    assertTrue(dao.createExperiment(experiment));

    List<Experiment> experiments = dao.getExperiments("user");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetExperimentsAfterCreateUnpublishedPrivate() {
    Experiment experiment = constructExperiment();
    experiment.setPublished(false);
    experiment.setViewers(Lists.newArrayList("user1"));

    assertTrue(dao.createExperiment(experiment));

    List<Experiment> experiments = dao.getExperiments("user2");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetExperimentsAfterCreateUnpublishedPrivateViewable() {
    Experiment experiment = constructExperiment();
    experiment.setPublished(false);
    experiment.setViewers(Lists.newArrayList("user"));

    assertTrue(dao.createExperiment(experiment));

    List<Experiment> experiments = dao.getExperiments("user");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetExperimentsAfterCreateAndDeletePublishedPublic() {
    Experiment experiment = constructExperiment();
    experiment.setPublished(true);
    experiment.setViewers(null);

    assertTrue(dao.createExperiment(experiment));
    assertTrue(dao.deleteExperiment(experiment));

    List<Experiment> experiments = dao.getExperiments("user");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetExperimentsAfterCreateAndDeletePublishedPrivate() {
    Experiment experiment = constructExperiment();
    experiment.setPublished(true);
    experiment.setViewers(Lists.newArrayList("user1"));

    assertTrue(dao.createExperiment(experiment));
    assertTrue(dao.deleteExperiment(experiment));

    List<Experiment> experiments = dao.getExperiments("user2");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetExperimentsAfterCreateAndDeletePublishedPrivateViewable() {
    Experiment experiment = constructExperiment();
    experiment.setPublished(true);
    experiment.setViewers(Lists.newArrayList("user"));

    assertTrue(dao.createExperiment(experiment));
    assertTrue(dao.deleteExperiment(experiment));

    List<Experiment> experiments = dao.getExperiments("user");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetExperimentsAfterCreateAndDeleteUnpublishedPublic() {
    Experiment experiment = constructExperiment();
    experiment.setPublished(false);
    experiment.setViewers(null);

    assertTrue(dao.createExperiment(experiment));
    assertTrue(dao.deleteExperiment(experiment));

    List<Experiment> experiments = dao.getExperiments("user");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetExperimentsAfterCreateAndDeleteUnpublishedPrivate() {
    Experiment experiment = constructExperiment();
    experiment.setPublished(false);
    experiment.setViewers(Lists.newArrayList("user1"));

    assertTrue(dao.createExperiment(experiment));
    assertTrue(dao.deleteExperiment(experiment));

    List<Experiment> experiments = dao.getExperiments("user2");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetExperimentsAfterCreateAndDeleteUnpublishedPrivateViewable() {
    Experiment experiment = constructExperiment();
    experiment.setPublished(false);
    experiment.setViewers(Lists.newArrayList("user"));

    assertTrue(dao.createExperiment(experiment));
    assertTrue(dao.deleteExperiment(experiment));

    List<Experiment> experiments = dao.getExperiments("user");

    assertEquals(0, experiments.size());
  }
}
