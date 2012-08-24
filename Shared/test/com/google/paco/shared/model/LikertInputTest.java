// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import static org.junit.Assert.*;

import com.google.common.collect.Lists;
import com.google.paco.shared.model.LikertInput;
import com.google.paco.shared.model.ListInput;
import com.google.paco.shared.model.TextInput;

import org.junit.Test;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class LikertInputTest {
  @Test
  public void testEquality() {
    LikertInput input1 = new LikertInput();
    LikertInput input2 = new LikertInput();

    assertTrue(input1.equals(input2));
  }

  @Test
  public void testEqualityWhenQuestionSet() {
    LikertInput input1 = new LikertInput();
    LikertInput input2 = new LikertInput();

    input1.setQuestion("question");
    input2.setQuestion("question");

    assertTrue(input1.equals(input2));
  }

  @Test
  public void testEqualityWhenQuestionSetNull() {
    LikertInput input1 = new LikertInput();
    LikertInput input2 = new LikertInput();

    input1.setQuestion(null);
    input2.setQuestion(null);

    assertTrue(input1.equals(input2));
  }

  @Test
  public void testEqualityWhenLabelsSet() {
    LikertInput input1 = new LikertInput();
    LikertInput input2 = new LikertInput();

    input1.setLabels(Lists.newArrayList("label"));
    input2.setLabels(Lists.newArrayList("label"));

    assertTrue(input1.equals(input2));
  }

  @Test
  public void testEqualityWhenLabelsSetNull() {
    LikertInput input1 = new LikertInput();
    LikertInput input2 = new LikertInput();

    input1.setLabels(null);
    input2.setLabels(null);

    assertTrue(input1.equals(input2));
  }

  @Test
  public void testEqualityWhenSmileysSet() {
    LikertInput input1 = new LikertInput();
    LikertInput input2 = new LikertInput();

    input1.setSmileys(true);
    input2.setSmileys(true);

    assertTrue(input1.equals(input2));
  }

  @Test
  public void testInequality() {
    LikertInput input1 = new LikertInput();
    ListInput input2 = new ListInput();
    TextInput input3 = new TextInput();

    assertFalse(input1.equals(input2));
    assertFalse(input1.equals(input3));
  }

  @Test
  public void testInequalityWhenQuestionSet() {
    LikertInput input1 = new LikertInput();
    LikertInput input2 = new LikertInput();

    input1.setQuestion("question1");
    input2.setQuestion("question2");

    assertFalse(input1.equals(input2));
  }

  @Test
  public void testInequalityWhenQuestionSetNull() {
    LikertInput input1 = new LikertInput();
    LikertInput input2 = new LikertInput();

    input1.setQuestion(null);
    input2.setQuestion("question2");

    assertFalse(input1.equals(input2));
  }

  @Test
  public void testInequalityWhenLabelsSet() {
    LikertInput input1 = new LikertInput();
    LikertInput input2 = new LikertInput();

    input1.setLabels(Lists.newArrayList("label1"));
    input2.setLabels(Lists.newArrayList("label2"));

    assertFalse(input1.equals(input2));
  }

  @Test
  public void testInequalityWhenLabelsSetNull() {
    LikertInput input1 = new LikertInput();
    LikertInput input2 = new LikertInput();

    input1.setLabels(null);
    input2.setLabels(Lists.newArrayList("label2"));

    assertFalse(input1.equals(input2));
  }

  @Test
  public void testInequalityWhenSmileysSet() {
    LikertInput input1 = new LikertInput();
    LikertInput input2 = new LikertInput();

    input1.setSmileys(true);
    input2.setSmileys(false);

    assertFalse(input1.equals(input2));
  }

  @Test
  public void testQuestionNotNullable() {
    LikertInput input = new LikertInput();

    input.setQuestion(null);

    assertNotNull(input.getQuestion());
  }

  @Test
  public void testLabelsNotNullable() {
    LikertInput input = new LikertInput();

    input.setLabels(null);

    assertNotNull(input.getLabels());
  }
}
