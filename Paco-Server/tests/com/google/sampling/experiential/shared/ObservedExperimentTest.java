// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.shared;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;

import org.junit.Test;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class ObservedExperimentTest {
  @Test
  public void testEqualityWhenPublishedSet() {
    ObservedExperiment experiment1 = new ObservedExperiment();
    ObservedExperiment experiment2 = new ObservedExperiment();

    experiment1.setPublished(true);
    experiment2.setPublished(true);

    assertTrue(experiment1.equals(experiment2));
  }

  @Test
  public void testEqualityWhenObserversSet() {
    ObservedExperiment experiment1 = new ObservedExperiment();
    ObservedExperiment experiment2 = new ObservedExperiment();

    experiment1.setObservers(Lists.newArrayList("observer"));
    experiment2.setObservers(Lists.newArrayList("observer"));

    assertTrue(experiment1.equals(experiment2));
  }

  @Test
  public void testEqualityWhenSubjectsSet() {
    ObservedExperiment experiment1 = new ObservedExperiment();
    ObservedExperiment experiment2 = new ObservedExperiment();

    experiment1.setSubjects(Lists.newArrayList("subject"));
    experiment2.setSubjects(Lists.newArrayList("subject"));

    assertTrue(experiment1.equals(experiment2));
  }

  @Test
  public void testEqualityWhenViewersSet() {
    ObservedExperiment experiment1 = new ObservedExperiment();
    ObservedExperiment experiment2 = new ObservedExperiment();

    experiment1.setViewers(Lists.newArrayList("viewer"));
    experiment2.setViewers(Lists.newArrayList("viewer"));

    assertTrue(experiment1.equals(experiment2));
  }

  @Test
  public void testInequalityWhenPublishedSet() {
    ObservedExperiment experiment1 = new ObservedExperiment();
    ObservedExperiment experiment2 = new ObservedExperiment();

    experiment1.setPublished(true);
    experiment2.setPublished(false);

    assertFalse(experiment1.equals(experiment2));
  }

  @Test
  public void testInequalityWhenObserversSet() {
    ObservedExperiment experiment1 = new ObservedExperiment();
    ObservedExperiment experiment2 = new ObservedExperiment();

    experiment1.setObservers(Lists.newArrayList("observer1"));
    experiment2.setObservers(Lists.newArrayList("observer2"));

    assertFalse(experiment1.equals(experiment2));
  }

  @Test
  public void testInequalityWhenSubjectsSet() {
    ObservedExperiment experiment1 = new ObservedExperiment();
    ObservedExperiment experiment2 = new ObservedExperiment();

    experiment1.setSubjects(Lists.newArrayList("subject1"));
    experiment2.setSubjects(Lists.newArrayList("subject2"));

    assertFalse(experiment1.equals(experiment2));
  }

  @Test
  public void testInequalityWhenViewersSet() {
    ObservedExperiment experiment1 = new ObservedExperiment();
    ObservedExperiment experiment2 = new ObservedExperiment();

    experiment1.setViewers(Lists.newArrayList("viewer1"));
    experiment2.setViewers(Lists.newArrayList("viewer2"));

    assertFalse(experiment1.equals(experiment2));
  }

  @Test
  public void testInequalityWhenViewersSetNull() {
    ObservedExperiment experiment1 = new ObservedExperiment();
    ObservedExperiment experiment2 = new ObservedExperiment();

    experiment1.setViewers(null);
    experiment2.setViewers(Lists.newArrayList("viewer2"));

    assertFalse(experiment1.equals(experiment2));
  }

  @Test
  public void testObserversIsNotNullable() {
    ObservedExperiment experiment = new ObservedExperiment();

    experiment.setObservers(null);

    assertNotNull(experiment.getObservers());
  }

  @Test
  public void testSubjectsIsNotNullable() {
    ObservedExperiment experiment = new ObservedExperiment();

    experiment.setSubjects(null);

    assertNotNull(experiment.getSubjects());
  }


  @Test
  public void testIsObservedBy() {
    ObservedExperiment experiment = new ObservedExperiment();

    assertFalse(experiment.isObservedBy("observer"));
  }

  @Test
  public void testIsObservedByWhenObserversSet() {
    ObservedExperiment experiment = new ObservedExperiment();

    experiment.setObservers(Lists.newArrayList("observer"));

    assertTrue(experiment.isObservedBy("observer"));
  }

  @Test
  public void testHasSubject() {
    ObservedExperiment experiment = new ObservedExperiment();

    assertFalse(experiment.hasSubject("Subject"));
  }

  @Test
  public void testHasSubjectWhenSubjectsSet() {
    ObservedExperiment experiment = new ObservedExperiment();

    experiment.setSubjects(Lists.newArrayList("subject"));

    assertTrue(experiment.hasSubject("subject"));
  }

  @Test
  public void testIsViewableBy() {
    ObservedExperiment experiment = new ObservedExperiment();

    assertTrue(experiment.isViewableBy("viewer"));
  }

  @Test
  public void testIsViewableByWhenViewersSet() {
    ObservedExperiment experiment = new ObservedExperiment();

    experiment.setViewers(Lists.newArrayList("viewer1", "viewer2"));

    assertTrue(experiment.isViewableBy("viewer1"));
  }

  @Test
  public void testIsViewableByWhenViewersSetAndNotViewer() {
    ObservedExperiment experiment = new ObservedExperiment();

    experiment.setViewers(Lists.newArrayList("viewer1", "viewer2"));

    assertFalse(experiment.isViewableBy("viewer"));
  }
}
