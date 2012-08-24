// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import com.google.paco.shared.model.LikertInput;
import com.google.paco.shared.model.ListInput;
import com.google.paco.shared.model.TextInput;

import org.junit.Test;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class ListInputTest {
  @Test
  public void testEquality() {
    ListInput input1 = new ListInput();
    ListInput input2 = new ListInput();

    assertTrue(input1.equals(input2));
  }

  @Test
  public void testEqualityWhenQuestionSet() {
    ListInput input1 = new ListInput();
    ListInput input2 = new ListInput();

    input1.setQuestion("question");
    input2.setQuestion("question");

    assertTrue(input1.equals(input2));
  }

  @Test
  public void testEqualityWhenQuestionSetNull() {
    ListInput input1 = new ListInput();
    ListInput input2 = new ListInput();

    input1.setQuestion(null);
    input2.setQuestion(null);

    assertTrue(input1.equals(input2));
  }

  @Test
  public void testEqualityWhenChoicesSet() {
    ListInput input1 = new ListInput();
    ListInput input2 = new ListInput();

    input1.setChoices(Lists.newArrayList("choice"));
    input2.setChoices(Lists.newArrayList("choice"));

    assertTrue(input1.equals(input2));
  }

  @Test
  public void testEqualityWhenChoicesSetNull() {
    ListInput input1 = new ListInput();
    ListInput input2 = new ListInput();

    input1.setChoices(null);
    input2.setChoices(null);

    assertTrue(input1.equals(input2));
  }

  @Test
  public void testEqualityWhenMultiselectSet() {
    ListInput input1 = new ListInput();
    ListInput input2 = new ListInput();

    input1.setMultiselect(true);
    input2.setMultiselect(true);

    assertTrue(input1.equals(input2));
  }

  @Test
  public void testInequality() {
    ListInput input1 = new ListInput();
    LikertInput input2 = new LikertInput();
    TextInput input3 = new TextInput();

    assertFalse(input1.equals(input2));
    assertFalse(input1.equals(input3));
  }

  @Test
  public void testInequalityWhenQuestionSet() {
    ListInput input1 = new ListInput();
    ListInput input2 = new ListInput();

    input1.setQuestion("question1");
    input2.setQuestion("question2");

    assertFalse(input1.equals(input2));
  }

  @Test
  public void testInequalityWhenQuestionSetNull() {
    ListInput input1 = new ListInput();
    ListInput input2 = new ListInput();

    input1.setQuestion(null);
    input2.setQuestion("question2");

    assertFalse(input1.equals(input2));
  }

  @Test
  public void testInequalityWhenChoicesSet() {
    ListInput input1 = new ListInput();
    ListInput input2 = new ListInput();

    input1.setChoices(Lists.newArrayList("choice1"));
    input2.setChoices(Lists.newArrayList("choice2"));

    assertFalse(input1.equals(input2));
  }

  @Test
  public void testInequalityWhenChoicesSetNull() {
    ListInput input1 = new ListInput();
    ListInput input2 = new ListInput();

    input1.setChoices(null);
    input2.setChoices(Lists.newArrayList("choice2"));

    assertFalse(input1.equals(input2));
  }

  @Test
  public void testInequalityWhenMultiselectSet() {
    ListInput input1 = new ListInput();
    ListInput input2 = new ListInput();

    input1.setMultiselect(true);
    input2.setMultiselect(false);

    assertFalse(input1.equals(input2));
  }

  @Test
  public void testQuestionNotNullable() {
    ListInput input = new ListInput();

    input.setQuestion(null);

    assertNotNull(input.getQuestion());
  }

  @Test
  public void testChoicesNotNullable() {
    ListInput input = new ListInput();

    input.setChoices(null);

    assertNotNull(input.getChoices());
  }
}
