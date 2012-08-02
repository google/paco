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
import com.google.sampling.experiential.shared.ObservedExperiment;
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

  protected static ObservedExperiment constructObservedExperiment() {
    ObservedExperiment experiment = new ObservedExperiment();

    experiment.setTitle("title");
    experiment.setDescription("description");
    experiment.setCreator("creator");
    experiment.setConsentForm("consent form");
    experiment.setDeleted(false);
    experiment.setPublished(false);
    experiment.setObservers(null);
    experiment.setSubjects(null);
    experiment.setViewers(null);
    experiment.setInputs(Lists.newArrayList(new TextInput(), new ListInput(), new LikertInput()));
    experiment.setSignalSchedule(new RandomSignal(), new DailySchedule());
    experiment.setFeedbacks(Lists.newArrayList(new Feedback()));

    return experiment;
  }


  protected static Experiment constructExperiment() {
    Experiment experiment = new Experiment();

    experiment.setTitle("title");
    experiment.setDescription("description");
    experiment.setCreator("creator");
    experiment.setConsentForm("consent form");
    experiment.setDeleted(false);
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
    ObservedExperiment experiment = constructObservedExperiment();

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
    ObservedExperiment experiment = constructObservedExperiment();

    assertTrue(dao.createExperiment(experiment));

    Experiment retrievedExperiment = dao.getExperiment(experiment.getId());

    assertNotNull(retrievedExperiment);
    assertEquals(retrievedExperiment, experiment);
  }

  @Test
  public void testUpdateExperiment() {
    ObservedExperiment experiment = constructObservedExperiment();

    assertFalse(dao.updateExperiment(experiment));
  }

  @Test
  public void testUpdateExperimentAfterCreate() {
    ObservedExperiment experiment = constructObservedExperiment();

    assertTrue(dao.createExperiment(experiment));

    long version = experiment.getVersion();
    experiment.setTitle("new title");

    assertTrue(dao.updateExperiment(experiment));
    assertEquals(version + 1, experiment.getVersion());
  }

  @Test
  public void testGetExperimentAfterCreateAndUpdate() {
    ObservedExperiment experiment = constructObservedExperiment();

    assertTrue(dao.createExperiment(experiment));

    experiment.setTitle("new title");

    assertTrue(dao.updateExperiment(experiment));

    Experiment retrievedExperiment = dao.getExperiment(experiment.getId());

    assertNotNull(retrievedExperiment);
    assertEquals(retrievedExperiment, experiment);
  }

  @Test
  public void testDeleteExperiment() {
    ObservedExperiment experiment = constructObservedExperiment();

    assertFalse(dao.deleteExperiment(experiment));
  }

  @Test
  public void testDeleteExperimentAfterCreate() {
    ObservedExperiment experiment = constructObservedExperiment();

    assertTrue(dao.createExperiment(experiment));
    assertTrue(dao.deleteExperiment(experiment));
    assertTrue(experiment.isDeleted());
  }

  @Test
  public void testGetExperimentAfterCreateAndDelete() {
    ObservedExperiment experiment = constructObservedExperiment();

    assertTrue(dao.createExperiment(experiment));
    assertTrue(dao.deleteExperiment(experiment));

    Experiment retrievedExperiment = dao.getExperiment(experiment.getId());

    assertNull(retrievedExperiment);
  }

  @Test
  public void testGetObserverExperiments() {
    List<ObservedExperiment> experiments = dao.getObserverExperiments("user");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetObserverExperimentsAfterCreate() {
    ObservedExperiment experiment = constructObservedExperiment();
    experiment.setObservers(Lists.newArrayList("user"));

    assertTrue(dao.createExperiment(experiment));

    List<ObservedExperiment> experiments = dao.getObserverExperiments("user");

    assertEquals(1, experiments.size());
  }

  @Test
  public void testGetObserverExperimentsAfterCreateAndDelete() {
    ObservedExperiment experiment = constructObservedExperiment();
    experiment.setObservers(Lists.newArrayList("user"));

    assertTrue(dao.createExperiment(experiment));
    assertTrue(dao.deleteExperiment(experiment));

    List<ObservedExperiment> experiments = dao.getObserverExperiments("user");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetSubjectExperiments() {
    List<Experiment> experiments = dao.getSubjectExperiments("user");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetSubjectExperimentsAfterCreateUnpublished() {
    ObservedExperiment experiment = constructObservedExperiment();
    experiment.setSubjects(Lists.newArrayList("user"));
    experiment.setPublished(false);

    assertTrue(dao.createExperiment(experiment));

    List<Experiment> experiments = dao.getSubjectExperiments("user");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetSubjectExperimentsAfterCreatePublished() {
    ObservedExperiment experiment = constructObservedExperiment();
    experiment.setSubjects(Lists.newArrayList("user"));
    experiment.setPublished(true);

    assertTrue(dao.createExperiment(experiment));

    List<Experiment> experiments = dao.getSubjectExperiments("user");

    assertEquals(1, experiments.size());
    assertEquals(experiments.get(0), experiment);
  }

  @Test
  public void testGetSubjectExperimentsAfterCreateAndDelete() {
    ObservedExperiment experiment = constructObservedExperiment();
    experiment.setSubjects(Lists.newArrayList("user"));

    assertTrue(dao.createExperiment(experiment));
    assertTrue(dao.deleteExperiment(experiment));

    List<Experiment> experiments = dao.getSubjectExperiments("user");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testJoinExperiment() {
    ObservedExperiment experiment = constructObservedExperiment();

    assertFalse(dao.joinExperiment("user", experiment, null));
  }

  @Test
  public void testJoinExperimentAfterCreate() {
    ObservedExperiment experiment = constructObservedExperiment();

    assertTrue(dao.createExperiment(experiment));
    assertTrue(dao.joinExperiment("user", experiment, null));
  }

  @Test
  public void testLeaveExperiment() {
    ObservedExperiment experiment = constructObservedExperiment();

    assertFalse(dao.leaveExperiment("user", experiment));
  }

  @Test
  public void testLeaveExperimentAfterCreate() {
    ObservedExperiment experiment = constructObservedExperiment();

    assertTrue(dao.createExperiment(experiment));
    assertFalse(dao.leaveExperiment("user", experiment));
  }

  @Test
  public void testLeaveExperimentAfterCreateAndJoin() {
    ObservedExperiment experiment = constructObservedExperiment();

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
    ObservedExperiment experiment = constructObservedExperiment();
    experiment.setPublished(true);
    experiment.setViewers(null);

    assertTrue(dao.createExperiment(experiment));

    List<Experiment> experiments = dao.getExperiments("user");

    assertEquals(1, experiments.size());
  }

  @Test
  public void testGetExperimentsAfterCreatePublishedPrivate() {
    ObservedExperiment experiment = constructObservedExperiment();
    experiment.setPublished(true);
    experiment.setViewers(Lists.newArrayList("user1"));

    assertTrue(dao.createExperiment(experiment));

    List<Experiment> experiments = dao.getExperiments("user2");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetExperimentsAfterCreatePublishedPrivateViewable() {
    ObservedExperiment experiment = constructObservedExperiment();
    experiment.setPublished(true);
    experiment.setViewers(Lists.newArrayList("user"));

    assertTrue(dao.createExperiment(experiment));

    List<Experiment> experiments = dao.getExperiments("user");

    assertEquals(1, experiments.size());
  }

  @Test
  public void testGetExperimentsAfterCreateUnpublishedPublic() {
    ObservedExperiment experiment = constructObservedExperiment();
    experiment.setPublished(false);
    experiment.setViewers(null);

    assertTrue(dao.createExperiment(experiment));

    List<Experiment> experiments = dao.getExperiments("user");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetExperimentsAfterCreateUnpublishedPrivate() {
    ObservedExperiment experiment = constructObservedExperiment();
    experiment.setPublished(false);
    experiment.setViewers(Lists.newArrayList("user1"));

    assertTrue(dao.createExperiment(experiment));

    List<Experiment> experiments = dao.getExperiments("user2");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetExperimentsAfterCreateUnpublishedPrivateViewable() {
    ObservedExperiment experiment = constructObservedExperiment();
    experiment.setPublished(false);
    experiment.setViewers(Lists.newArrayList("user"));

    assertTrue(dao.createExperiment(experiment));

    List<Experiment> experiments = dao.getExperiments("user");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetExperimentsAfterCreateAndDeletePublishedPublic() {
    ObservedExperiment experiment = constructObservedExperiment();
    experiment.setPublished(true);
    experiment.setViewers(null);

    assertTrue(dao.createExperiment(experiment));
    assertTrue(dao.deleteExperiment(experiment));

    List<Experiment> experiments = dao.getExperiments("user");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetExperimentsAfterCreateAndDeletePublishedPrivate() {
    ObservedExperiment experiment = constructObservedExperiment();
    experiment.setPublished(true);
    experiment.setViewers(Lists.newArrayList("user1"));

    assertTrue(dao.createExperiment(experiment));
    assertTrue(dao.deleteExperiment(experiment));

    List<Experiment> experiments = dao.getExperiments("user2");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetExperimentsAfterCreateAndDeletePublishedPrivateViewable() {
    ObservedExperiment experiment = constructObservedExperiment();
    experiment.setPublished(true);
    experiment.setViewers(Lists.newArrayList("user"));

    assertTrue(dao.createExperiment(experiment));
    assertTrue(dao.deleteExperiment(experiment));

    List<Experiment> experiments = dao.getExperiments("user");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetExperimentsAfterCreateAndDeleteUnpublishedPublic() {
    ObservedExperiment experiment = constructObservedExperiment();
    experiment.setPublished(false);
    experiment.setViewers(null);

    assertTrue(dao.createExperiment(experiment));
    assertTrue(dao.deleteExperiment(experiment));

    List<Experiment> experiments = dao.getExperiments("user");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetExperimentsAfterCreateAndDeleteUnpublishedPrivate() {
    ObservedExperiment experiment = constructObservedExperiment();
    experiment.setPublished(false);
    experiment.setViewers(Lists.newArrayList("user1"));

    assertTrue(dao.createExperiment(experiment));
    assertTrue(dao.deleteExperiment(experiment));

    List<Experiment> experiments = dao.getExperiments("user2");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetExperimentsAfterCreateAndDeleteUnpublishedPrivateViewable() {
    ObservedExperiment experiment = constructObservedExperiment();
    experiment.setPublished(false);
    experiment.setViewers(Lists.newArrayList("user"));

    assertTrue(dao.createExperiment(experiment));
    assertTrue(dao.deleteExperiment(experiment));

    List<Experiment> experiments = dao.getExperiments("user");

    assertEquals(0, experiments.size());
  }
}
