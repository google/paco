// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.server;

import static org.junit.Assert.*;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.shared.DailySchedule;
import com.google.sampling.experiential.shared.Event;
import com.google.sampling.experiential.shared.Experiment;
import com.google.sampling.experiential.shared.Feedback;
import com.google.sampling.experiential.shared.FixedSignal;
import com.google.sampling.experiential.shared.LikertInput;
import com.google.sampling.experiential.shared.ListInput;
import com.google.sampling.experiential.shared.SharedTestHelper;
import com.google.sampling.experiential.shared.TextInput;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class DAOTest {
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  private final DAO dao = DAO.getInstance();

  protected static Experiment constructObservedExperiment() {
    Experiment experiment = new Experiment();

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
    experiment.setSignalSchedule(null);
    experiment.setFeedbacks(Lists.newArrayList(new Feedback()));

    return experiment;
  }

  protected static Experiment constructExperiment() {
    return constructExperiment(null);
  }

  protected static Experiment constructExperiment(Long id) {
    Experiment experiment = new Experiment();

    experiment.setId(id);
    experiment.setVersion(1);
    experiment.setTitle("title");
    experiment.setDescription("description");
    experiment.setCreator("creator");
    experiment.setConsentForm("consent form");
    experiment.setDeleted(false);
    experiment.setInputs(Lists.newArrayList(new TextInput(), new ListInput(), new LikertInput()));
    experiment.setSignalSchedule(null);
    experiment.setFeedbacks(Lists.newArrayList(new Feedback()));

    return experiment;
  }

  private Event constructEvent() {
    Event event = new Event();

    event.setExperimentVersion(1);
    event.setSignalTime(new Date(3));
    event.setResponseTime(new Date(13));
    event.setOutputByKey("test", "value");

    return event;
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
    Experiment experiment = constructObservedExperiment();

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
    Experiment experiment = constructObservedExperiment();

    assertTrue(dao.createExperiment(experiment));

    Experiment retrievedExperiment = dao.getExperiment(experiment.getId());

    assertNotNull(retrievedExperiment);
    assertEquals(retrievedExperiment, experiment);
  }

  @Test
  public void testUpdateExperiment() {
    Experiment oldExperiment = constructObservedExperiment();
    Experiment newExperiment = constructObservedExperiment();

    assertFalse(dao.updateExperiment(newExperiment, oldExperiment));
  }

  @Test
  public void testUpdateExperimentAfterCreate() {
    Experiment oldExperiment = constructObservedExperiment();

    assertTrue(dao.createExperiment(oldExperiment));

    Experiment newExperiment = constructObservedExperiment();
    newExperiment.setTitle("new title");

    assertTrue(dao.updateExperiment(newExperiment, oldExperiment));
    assertEquals(oldExperiment.getVersion() + 1, newExperiment.getVersion());
  }

  @Test
  public void testGetExperimentAfterCreateAndUpdate() {
    Experiment oldExperiment = constructObservedExperiment();

    assertTrue(dao.createExperiment(oldExperiment));

    Experiment newExperiment = constructObservedExperiment();
    newExperiment.setTitle("new title");

    assertTrue(dao.updateExperiment(newExperiment, oldExperiment));

    Experiment retrievedExperiment = dao.getExperiment(newExperiment.getId());

    assertNotNull(retrievedExperiment);
    assertEquals(retrievedExperiment, newExperiment);
  }

  @Test
  public void testDeleteExperiment() {
    Experiment experiment = constructObservedExperiment();

    assertFalse(dao.deleteExperiment(experiment));
  }

  @Test
  public void testDeleteExperimentAfterCreate() {
    Experiment experiment = constructObservedExperiment();

    assertTrue(dao.createExperiment(experiment));
    assertTrue(dao.deleteExperiment(experiment));
    assertTrue(experiment.isDeleted());
  }

  @Test
  public void testGetExperimentAfterCreateAndDelete() {
    Experiment experiment = constructObservedExperiment();

    assertTrue(dao.createExperiment(experiment));
    assertTrue(dao.deleteExperiment(experiment));

    Experiment retrievedExperiment = dao.getExperiment(experiment.getId());

    assertNull(retrievedExperiment);
  }

  @Test
  public void testGetObserverExperiments() {
    List<Experiment> experiments = dao.getObservedExperiments("user");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetObserverExperimentsAfterCreate() {
    Experiment experiment = constructObservedExperiment();
    experiment.setObservers(Lists.newArrayList("user"));

    assertTrue(dao.createExperiment(experiment));

    List<Experiment> experiments = dao.getObservedExperiments("user");

    assertEquals(1, experiments.size());
  }

  @Test
  public void testGetObserverExperimentsAfterCreateAndDelete() {
    Experiment experiment = constructObservedExperiment();
    experiment.setObservers(Lists.newArrayList("user"));

    assertTrue(dao.createExperiment(experiment));
    assertTrue(dao.deleteExperiment(experiment));

    List<Experiment> experiments = dao.getObservedExperiments("user");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetSubjectExperiments() {
    List<Experiment> experiments = dao.getSubjectedExperiments("user");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetSubjectExperimentsAfterCreateUnpublished() {
    Experiment experiment = constructObservedExperiment();
    experiment.setSubjects(Lists.newArrayList("user"));
    experiment.setPublished(false);

    assertTrue(dao.createExperiment(experiment));

    List<Experiment> experiments = dao.getSubjectedExperiments("user");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetSubjectExperimentsAfterCreatePublished() {
    Experiment experiment = constructObservedExperiment();
    experiment.setSubjects(Lists.newArrayList("user"));
    experiment.setPublished(true);

    assertTrue(dao.createExperiment(experiment));

    List<Experiment> experiments = dao.getSubjectedExperiments("user");

    assertEquals(1, experiments.size());
    assertEquals(experiments.get(0), experiment);
  }

  @Test
  public void testGetSubjectExperimentsAfterCreateAndDelete() {
    Experiment experiment = constructObservedExperiment();
    experiment.setSubjects(Lists.newArrayList("user"));

    assertTrue(dao.createExperiment(experiment));
    assertTrue(dao.deleteExperiment(experiment));

    List<Experiment> experiments = dao.getSubjectedExperiments("user");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testJoinExperiment() {
    Experiment experiment = constructObservedExperiment();

    assertFalse(dao.joinExperiment("user", experiment, null));
  }

  @Test
  public void testJoinExperimentWithSchedule() {
    Experiment experiment = constructObservedExperiment();

    assertFalse(dao.joinExperiment("user", experiment,
        SharedTestHelper.createSignalSchedule(new FixedSignal(), new DailySchedule())));
  }

  @Test
  public void testJoinExperimentAfterCreate() {
    Experiment experiment = constructObservedExperiment();

    assertTrue(dao.createExperiment(experiment));
    assertTrue(dao.joinExperiment("user", experiment, null));
  }

  @Test
  public void testJoinExperimentWithScheduleAfterCreate() {
    Experiment experiment = constructObservedExperiment();

    assertTrue(dao.createExperiment(experiment));
    assertTrue(dao.joinExperiment("user", experiment,
        SharedTestHelper.createSignalSchedule(new FixedSignal(), new DailySchedule())));
  }

  @Test
  public void testLeaveExperiment() {
    Experiment experiment = constructObservedExperiment();

    assertFalse(dao.leaveExperiment("user", experiment));
  }

  @Test
  public void testLeaveExperimentAfterCreate() {
    Experiment experiment = constructObservedExperiment();

    assertTrue(dao.createExperiment(experiment));
    assertFalse(dao.leaveExperiment("user", experiment));
  }

  @Test
  public void testLeaveExperimentAfterCreateAndJoin() {
    Experiment experiment = constructObservedExperiment();

    assertTrue(dao.createExperiment(experiment));
    assertTrue(dao.joinExperiment("user", experiment, null));
    assertTrue(dao.leaveExperiment("user", experiment));
  }

  @Test
  public void testGetExperiments() {
    List<Experiment> experiments = dao.getViewedExperiments("user");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetExperimentsAfterCreatePublishedPublic() {
    Experiment experiment = constructObservedExperiment();
    experiment.setPublished(true);
    experiment.setViewers(null);

    assertTrue(dao.createExperiment(experiment));

    List<Experiment> experiments = dao.getViewedExperiments("user");

    assertEquals(1, experiments.size());
  }

  @Test
  public void testGetExperimentsAfterCreatePublishedPrivate() {
    Experiment experiment = constructObservedExperiment();
    experiment.setPublished(true);
    experiment.setViewers(Lists.newArrayList("user1"));

    assertTrue(dao.createExperiment(experiment));

    List<Experiment> experiments = dao.getViewedExperiments("user2");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetExperimentsAfterCreatePublishedPrivateViewable() {
    Experiment experiment = constructObservedExperiment();
    experiment.setPublished(true);
    experiment.setViewers(Lists.newArrayList("user"));

    assertTrue(dao.createExperiment(experiment));

    List<Experiment> experiments = dao.getViewedExperiments("user");

    assertEquals(1, experiments.size());
  }

  @Test
  public void testGetExperimentsAfterCreateUnpublishedPublic() {
    Experiment experiment = constructObservedExperiment();
    experiment.setPublished(false);
    experiment.setViewers(null);

    assertTrue(dao.createExperiment(experiment));

    List<Experiment> experiments = dao.getViewedExperiments("user");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetExperimentsAfterCreateUnpublishedPrivate() {
    Experiment experiment = constructObservedExperiment();
    experiment.setPublished(false);
    experiment.setViewers(Lists.newArrayList("user1"));

    assertTrue(dao.createExperiment(experiment));

    List<Experiment> experiments = dao.getViewedExperiments("user2");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetExperimentsAfterCreateUnpublishedPrivateViewable() {
    Experiment experiment = constructObservedExperiment();
    experiment.setPublished(false);
    experiment.setViewers(Lists.newArrayList("user"));

    assertTrue(dao.createExperiment(experiment));

    List<Experiment> experiments = dao.getViewedExperiments("user");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetExperimentsAfterCreateAndDeletePublishedPublic() {
    Experiment experiment = constructObservedExperiment();
    experiment.setPublished(true);
    experiment.setViewers(null);

    assertTrue(dao.createExperiment(experiment));
    assertTrue(dao.deleteExperiment(experiment));

    List<Experiment> experiments = dao.getViewedExperiments("user");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetExperimentsAfterCreateAndDeletePublishedPrivate() {
    Experiment experiment = constructObservedExperiment();
    experiment.setPublished(true);
    experiment.setViewers(Lists.newArrayList("user1"));

    assertTrue(dao.createExperiment(experiment));
    assertTrue(dao.deleteExperiment(experiment));

    List<Experiment> experiments = dao.getViewedExperiments("user2");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetExperimentsAfterCreateAndDeletePublishedPrivateViewable() {
    Experiment experiment = constructObservedExperiment();
    experiment.setPublished(true);
    experiment.setViewers(Lists.newArrayList("user"));

    assertTrue(dao.createExperiment(experiment));
    assertTrue(dao.deleteExperiment(experiment));

    List<Experiment> experiments = dao.getViewedExperiments("user");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetExperimentsAfterCreateAndDeleteUnpublishedPublic() {
    Experiment experiment = constructObservedExperiment();
    experiment.setPublished(false);
    experiment.setViewers(null);

    assertTrue(dao.createExperiment(experiment));
    assertTrue(dao.deleteExperiment(experiment));

    List<Experiment> experiments = dao.getViewedExperiments("user");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetExperimentsAfterCreateAndDeleteUnpublishedPrivate() {
    Experiment experiment = constructObservedExperiment();
    experiment.setPublished(false);
    experiment.setViewers(Lists.newArrayList("user1"));

    assertTrue(dao.createExperiment(experiment));
    assertTrue(dao.deleteExperiment(experiment));

    List<Experiment> experiments = dao.getViewedExperiments("user2");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testGetExperimentsAfterCreateAndDeleteUnpublishedPrivateViewable() {
    Experiment experiment = constructObservedExperiment();
    experiment.setPublished(false);
    experiment.setViewers(Lists.newArrayList("user"));

    assertTrue(dao.createExperiment(experiment));
    assertTrue(dao.deleteExperiment(experiment));

    List<Experiment> experiments = dao.getViewedExperiments("user");

    assertEquals(0, experiments.size());
  }

  @Test
  public void testCreateEvent() {
    Experiment experiment = constructObservedExperiment();
    experiment.setId(1l);
    experiment.setVersion(1);

    Event event = constructEvent();

    assertTrue(dao.createEvent("test@google.com", event, experiment));
  }

  @Test
  public void testCreateEventWhenUserNull() {
    Experiment experiment = constructObservedExperiment();
    experiment.setId(1l);
    experiment.setVersion(1);

    Event event = constructEvent();
    event.setExperimentVersion(1l);

    assertFalse(dao.createEvent(null, event, experiment));
  }

  @Test
  public void testCreateEventWhenEventNull() {
    Experiment experiment = constructObservedExperiment();
    experiment.setId(1l);
    experiment.setVersion(1);

    assertFalse(dao.createEvent("test@google.com", null, experiment));
  }

  @Test
  public void testCreateEventWhenExperimentNull() {
    Experiment experiment = constructObservedExperiment();
    experiment.setId(1l);
    experiment.setVersion(1);

    Event event = constructEvent();
    event.setExperimentVersion(1);

    assertFalse(dao.createEvent("test@google.com", event, null));
  }

  @Test
  public void testCreateEventWhenVersionLess() {
    Experiment experiment = constructObservedExperiment();
    experiment.setId(1l);
    experiment.setVersion(2);

    Event event = constructEvent();
    event.setExperimentVersion(1);

    assertTrue(dao.createEvent("test@google.com", event, experiment));
  }

  @Test
  public void testCreateEventWhenVersionGreater() {
    Experiment experiment = constructObservedExperiment();
    experiment.setId(1l);
    experiment.setVersion(1);

    Event event = constructEvent();
    event.setExperimentVersion(2);

    assertFalse(dao.createEvent("test@google.com", event, experiment));
  }

  @Test
  public void testGetEvent() {
    Event event = dao.getEvent(1l);

    assertNull(event);
  }

  @Test
  public void testGetEventAfterCreate() {
    Experiment experiment = constructObservedExperiment();
    experiment.setId(1l);
    experiment.setVersion(1);

    Event event = constructEvent();

    assertTrue(dao.createEvent("test@google.com", event, experiment));

    Event retrievedEvent = dao.getEvent(1l);

    assertEquals(event, retrievedEvent);
  }

  @Test
  public void testGetEvents() {
    Experiment experiment = constructObservedExperiment();
    experiment.setId(1l);
    experiment.setVersion(1);

    List<Event> events = dao.getEvents(experiment);

    assertEquals(0, events.size());
  }

  @Test
  public void testGetEventsAfterCreate() {
    Experiment experiment = constructObservedExperiment();
    experiment.setId(1l);
    experiment.setVersion(1);

    Event event = constructEvent();

    assertTrue(dao.createEvent("test@google.com", event, experiment));

    List<Event> events = dao.getEvents(experiment);

    assertEquals(1, events.size());
    assertEquals(event, events.get(0));
  }

  @Test
  public void testGetEventsWithSubjectAfterCreate() {
    Experiment experiment = constructObservedExperiment();
    experiment.setId(1l);
    experiment.setVersion(1);

    Event event = constructEvent();

    assertTrue(dao.createEvent("test@google.com", event, experiment));

    List<Event> events = dao.getEvents(experiment, "test@google.com");

    assertEquals(1, events.size());
    assertEquals(event, events.get(0));
  }

  @Test
  public void testGetEventsWithImpostorAfterCreate() {
    Experiment experiment = constructObservedExperiment();
    experiment.setId(1l);
    experiment.setVersion(1);

    Event event = constructEvent();

    assertTrue(dao.createEvent("test@google.com", event, experiment));

    List<Event> events = dao.getEvents(experiment, "impostor@google.com");

    assertEquals(0, events.size());
  }

  @Test
  public void testGetEventsAfterCreateWithVersion() {
    Experiment experiment = constructObservedExperiment();
    experiment.setId(1l);
    experiment.setVersion(1);

    Event event = constructEvent();

    assertTrue(dao.createEvent("test@google.com", event, experiment));

    experiment.setVersion(2l);

    List<Event> events = dao.getEvents(experiment);

    assertEquals(1, events.size());
    assertEquals(event, events.get(0));
  }

  @Test
  public void testGetEventsWithSubjectAfterCreateWithVersion() {
    Experiment experiment = constructObservedExperiment();
    experiment.setId(1l);
    experiment.setVersion(1);

    Event event = constructEvent();

    assertTrue(dao.createEvent("test@google.com", event, experiment));

    experiment.setVersion(2l);

    List<Event> events = dao.getEvents(experiment, "test@google.com");

    assertEquals(1, events.size());
    assertEquals(event, events.get(0));
  }

  @Test
  public void testGetEventsWithImpostorAfterCreateWithVersion() {
    Experiment experiment = constructObservedExperiment();
    experiment.setId(1l);
    experiment.setVersion(1);

    Event event = constructEvent();

    assertTrue(dao.createEvent("test@google.com", event, experiment));

    experiment.setVersion(2l);

    List<Event> events = dao.getEvents(experiment, "impostor@google.com");

    assertEquals(0, events.size());
  }

  @Test
  public void testGetEventsAfterCreateWithId() {
    Experiment experiment = constructObservedExperiment();
    experiment.setId(1l);
    experiment.setVersion(1);

    Event event = constructEvent();

    assertTrue(dao.createEvent("test@google.com", event, experiment));

    experiment.setId(2l);

    List<Event> events = dao.getEvents(experiment);

    assertEquals(0, events.size());
  }

  @Test
  public void testGetEventsWithSubjectAfterCreateWithId() {
    Experiment experiment = constructObservedExperiment();
    experiment.setId(1l);
    experiment.setVersion(1);

    Event event = constructEvent();

    assertTrue(dao.createEvent("test@google.com", event, experiment));

    experiment.setId(2l);

    List<Event> events = dao.getEvents(experiment, "test@google.com");

    assertEquals(0, events.size());
  }

  @Test
  public void testGetEventsWithImpostorAfterCreateWithId() {
    Experiment experiment = constructObservedExperiment();
    experiment.setId(1l);
    experiment.setVersion(1);

    Event event = constructEvent();

    assertTrue(dao.createEvent("test@google.com", event, experiment));

    experiment.setId(2l);

    List<Event> events = dao.getEvents(experiment, "impostor@google.com");

    assertEquals(0, events.size());
  }
}
