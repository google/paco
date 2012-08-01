// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.shared;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class FeedbackTest {
  @Test
  public void testEquality() {
    Feedback feedback1 = new Feedback();
    Feedback feedback2 = new Feedback();

    assertTrue(feedback1.equals(feedback2));
  }

  @Test
  public void testEqualityWhenTextSet() {
    Feedback feedback1 = new Feedback();
    Feedback feedback2 = new Feedback();

    feedback1.setText("text");
    feedback2.setText("text");

    assertTrue(feedback1.equals(feedback2));
  }

  @Test
  public void testEqualityWhenTextSetNull() {
    Feedback feedback1 = new Feedback();
    Feedback feedback2 = new Feedback();

    feedback1.setText(null);
    feedback2.setText(null);

    assertTrue(feedback1.equals(feedback2));
  }

  @Test
  public void testInequalityWhenTextSet() {
    Feedback feedback1 = new Feedback();
    Feedback feedback2 = new Feedback();

    feedback1.setText("text1");
    feedback2.setText("text2");

    assertFalse(feedback1.equals(feedback2));
  }

  @Test
  public void testInequalityWhenTextSetNull() {
    Feedback feedback1 = new Feedback();
    Feedback feedback2 = new Feedback();

    feedback1.setText(null);
    feedback2.setText("text2");

    assertFalse(feedback1.equals(feedback2));
  }

  @Test
  public void testTextIsNotNullable() {
    Feedback feedback = new Feedback();

    feedback.setText(null);

    assertNotNull(feedback.getText());
  }
}
