// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.shared;

import static org.junit.Assert.*;

import com.google.common.collect.Lists;

import org.junit.Test;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class ExperimentTest {
  @Test
  public void testEquality() {
    Experiment experiment1 = new Experiment();
    Experiment experiment2 = new Experiment();

    assertTrue(experiment1.equals(experiment2));
  }

  @Test
  public void testEqualityWhenTitleSet() {
    Experiment experiment1 = new Experiment();
    Experiment experiment2 = new Experiment();

    experiment1.setTitle("title");
    experiment2.setTitle("title");

    assertTrue(experiment1.equals(experiment2));
  }

  @Test
  public void testEqualityWhenDescriptionSet() {
    Experiment experiment1 = new Experiment();
    Experiment experiment2 = new Experiment();

    experiment1.setDescription("description");
    experiment2.setDescription("description");

    assertTrue(experiment1.equals(experiment2));
  }

  @Test
  public void testEqualityWhenCreatorSet() {
    Experiment experiment1 = new Experiment();
    Experiment experiment2 = new Experiment();

    experiment1.setCreator("creator");
    experiment2.setCreator("creator");

    assertTrue(experiment1.equals(experiment2));
  }

  @Test
  public void testEqualityWhenConsentFormSet() {
    Experiment experiment1 = new Experiment();
    Experiment experiment2 = new Experiment();

    experiment1.setConsentForm("consent form");
    experiment2.setConsentForm("consent form");

    assertTrue(experiment1.equals(experiment2));
  }

  @Test
  public void testEqualityWhenVersionSet() {
    Experiment experiment1 = new Experiment();
    Experiment experiment2 = new Experiment();

    experiment1.setVersion(3);
    experiment2.setVersion(3);

    assertTrue(experiment1.equals(experiment2));
  }

  @Test
  public void testEqualityWhenPublishedSet() {
    Experiment experiment1 = new Experiment();
    Experiment experiment2 = new Experiment();

    experiment1.setPublished(true);
    experiment2.setPublished(true);

    assertTrue(experiment1.equals(experiment2));
  }

  @Test
  public void testEqualityWhenDeletedSet() {
    Experiment experiment1 = new Experiment();
    Experiment experiment2 = new Experiment();

    experiment1.setDeleted(true);
    experiment2.setDeleted(true);

    assertTrue(experiment1.equals(experiment2));
  }

  @Test
  public void testEqualityWhenObserversSet() {
    Experiment experiment1 = new Experiment();
    Experiment experiment2 = new Experiment();

    experiment1.setObservers(Lists.newArrayList("observer"));
    experiment2.setObservers(Lists.newArrayList("observer"));

    assertTrue(experiment1.equals(experiment2));
  }

  @Test
  public void testEqualityWhenSubjectsSet() {
    Experiment experiment1 = new Experiment();
    Experiment experiment2 = new Experiment();

    experiment1.setSubjects(Lists.newArrayList("subject"));
    experiment2.setSubjects(Lists.newArrayList("subject"));

    assertTrue(experiment1.equals(experiment2));
  }

  @Test
  public void testEqualityWhenInputsSet() {
    Experiment experiment1 = new Experiment();
    Experiment experiment2 = new Experiment();

    experiment1.setInputs(Lists.newArrayList(new TextInput(), new ListInput()));
    experiment2.setInputs(Lists.newArrayList(new TextInput(), new ListInput()));

    assertTrue(experiment1.equals(experiment2));
  }

  @Test
  public void testEqualityWhenScheduleSet() {
    Experiment experiment1 = new Experiment();
    Experiment experiment2 = new Experiment();

    experiment1.setSchedule(new DailySchedule());
    experiment2.setSchedule(new DailySchedule());

    assertTrue(experiment1.equals(experiment2));
  }

  @Test
  public void testEqualityWhenFeedbacksSet() {
    Experiment experiment1 = new Experiment();
    Experiment experiment2 = new Experiment();

    experiment1.setFeedbacks(Lists.newArrayList(new Feedback()));
    experiment2.setFeedbacks(Lists.newArrayList(new Feedback()));

    assertTrue(experiment1.equals(experiment2));
  }

  @Test
  public void testInequalityWhenTitleSet() {
    Experiment experiment1 = new Experiment();
    Experiment experiment2 = new Experiment();

    experiment1.setTitle("title1");
    experiment2.setTitle("title2");

    assertFalse(experiment1.equals(experiment2));
  }

  @Test
  public void testInequalityWhenDescriptionSet() {
    Experiment experiment1 = new Experiment();
    Experiment experiment2 = new Experiment();

    experiment1.setDescription("description1");
    experiment2.setDescription("description2");

    assertFalse(experiment1.equals(experiment2));
  }

  @Test
  public void testInequalityWhenCreatorSet() {
    Experiment experiment1 = new Experiment();
    Experiment experiment2 = new Experiment();

    experiment1.setCreator("creator1");
    experiment2.setCreator("creator2");

    assertFalse(experiment1.equals(experiment2));
  }

  @Test
  public void testInequalityWhenConsentFormSet() {
    Experiment experiment1 = new Experiment();
    Experiment experiment2 = new Experiment();

    experiment1.setConsentForm("consent form1");
    experiment2.setConsentForm("consent form2");

    assertFalse(experiment1.equals(experiment2));
  }

  @Test
  public void testInequalityWhenVersionSet() {
    Experiment experiment1 = new Experiment();
    Experiment experiment2 = new Experiment();

    experiment1.setVersion(3);
    experiment2.setVersion(4);

    assertFalse(experiment1.equals(experiment2));
  }

  @Test
  public void testInequalityWhenPublishedSet() {
    Experiment experiment1 = new Experiment();
    Experiment experiment2 = new Experiment();

    experiment1.setPublished(true);
    experiment2.setPublished(false);

    assertFalse(experiment1.equals(experiment2));
  }

  @Test
  public void testInequalityWhenDeletedSet() {
    Experiment experiment1 = new Experiment();
    Experiment experiment2 = new Experiment();

    experiment1.setDeleted(true);
    experiment2.setDeleted(false);

    assertFalse(experiment1.equals(experiment2));
  }

  @Test
  public void testInequalityWhenObserversSet() {
    Experiment experiment1 = new Experiment();
    Experiment experiment2 = new Experiment();

    experiment1.setObservers(Lists.newArrayList("observer1"));
    experiment2.setObservers(Lists.newArrayList("observer2"));

    assertFalse(experiment1.equals(experiment2));
  }

  @Test
  public void testInequalityWhenSubjectsSet() {
    Experiment experiment1 = new Experiment();
    Experiment experiment2 = new Experiment();

    experiment1.setSubjects(Lists.newArrayList("subject1"));
    experiment2.setSubjects(Lists.newArrayList("subject2"));

    assertFalse(experiment1.equals(experiment2));
  }

  @Test
  public void testInequalityWhenInputsSet() {
    Experiment experiment1 = new Experiment();
    Experiment experiment2 = new Experiment();

    experiment1.setInputs(Lists.newArrayList(new TextInput(), new ListInput()));
    experiment2.setInputs(Lists.newArrayList(new TextInput(), new LikertInput()));

    assertFalse(experiment1.equals(experiment2));
  }

  @Test
  public void testInequalityWhenScheduleSet() {
    Experiment experiment1 = new Experiment();
    Experiment experiment2 = new Experiment();

    experiment1.setSchedule(new DailySchedule());
    experiment2.setSchedule(new WeeklySchedule());

    assertFalse(experiment1.equals(experiment2));
  }

  @Test
  public void testInequalityWhenScheduleSetNull() {
    Experiment experiment1 = new Experiment();
    Experiment experiment2 = new Experiment();

    experiment1.setSchedule(null);
    experiment2.setSchedule(new WeeklySchedule());

    assertFalse(experiment1.equals(experiment2));
  }

  @Test
  public void testInequalityWhenFeedbacksSet() {
    Experiment experiment1 = new Experiment();
    Experiment experiment2 = new Experiment();

    experiment1.setFeedbacks(Lists.newArrayList(new Feedback()));
    experiment2.setFeedbacks(Lists.newArrayList(new Feedback()));

    assertFalse(experiment1.equals(experiment2));
  }

  @Test
  public void testTitleIsNotNull() {
    Experiment experiment = new Experiment();

    experiment.setTitle(null);

    assertNotNull(experiment.getTitle());
  }

  @Test
  public void testDescriptionIsNotNull() {
    Experiment experiment = new Experiment();

    experiment.setDescription(null);

    assertNotNull(experiment.getDescription());
  }

  @Test
  public void testCreatorIsNotNull() {
    Experiment experiment = new Experiment();

    experiment.setCreator(null);

    assertNotNull(experiment.getCreator());
  }

  @Test
  public void testConsentFormIsNotNull() {
    Experiment experiment = new Experiment();

    experiment.setConsentForm(null);

    assertNotNull(experiment.getConsentForm());
  }

  @Test
  public void testObserversIsNotNull() {
    Experiment experiment = new Experiment();

    experiment.setObservers(null);

    assertNotNull(experiment.getObservers());
  }

  @Test
  public void testSubjectsIsNotNull() {
    Experiment experiment = new Experiment();

    experiment.setSubjects(null);

    assertNotNull(experiment.getSubjects());
  }

  @Test
  public void testInputsIsNotNull() {
    Experiment experiment = new Experiment();

    experiment.setInputs(null);

    assertNotNull(experiment.getInputs());
  }

  @Test
  public void testScheduleIsNull() {
    Experiment experiment = new Experiment();

    experiment.setSchedule(null);

    assertNull(experiment.getSchedule());
  }

  @Test
  public void testHasSchedule() {
    Experiment experiment = new Experiment();

    experiment.setSchedule(new DailySchedule());

    assertTrue(experiment.hasSchedule());
  }

  @Test
  public void testHasScheduleWhenNull() {
    Experiment experiment = new Experiment();

    experiment.setSchedule(null);

    assertFalse(experiment.hasSchedule());
  }
}
