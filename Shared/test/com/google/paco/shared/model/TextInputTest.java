// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.paco.shared.model.LikertInput;
import com.google.paco.shared.model.ListInput;
import com.google.paco.shared.model.TextInput;

import org.junit.Test;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class TextInputTest {
  @Test
  public void testEquality() {
    TextInput input1 = new TextInput();
    TextInput input2 = new TextInput();

    assertTrue(input1.equals(input2));
  }

  @Test
  public void testEqualityWhenQuestionSet() {
    TextInput input1 = new TextInput();
    TextInput input2 = new TextInput();

    input1.setQuestion("question");
    input2.setQuestion("question");

    assertTrue(input1.equals(input2));
  }

  @Test
  public void testEqualityWhenQuestionSetNull() {
    TextInput input1 = new TextInput();
    TextInput input2 = new TextInput();

    input1.setQuestion(null);
    input2.setQuestion(null);

    assertTrue(input1.equals(input2));
  }

  @Test
  public void testEqualityWhenMultilineSet() {
    TextInput input1 = new TextInput();
    TextInput input2 = new TextInput();

    input1.setMultiline(true);
    input2.setMultiline(true);

    assertTrue(input1.equals(input2));
  }

  @Test
  public void testInequality() {
    TextInput input1 = new TextInput();
    LikertInput input2 = new LikertInput();
    ListInput input3 = new ListInput();

    assertFalse(input1.equals(input2));
    assertFalse(input1.equals(input3));
  }

  @Test
  public void testInequalityWhenQuestionSet() {
    TextInput input1 = new TextInput();
    TextInput input2 = new TextInput();

    input1.setQuestion("question1");
    input2.setQuestion("question2");

    assertFalse(input1.equals(input2));
  }

  @Test
  public void testInequalityWhenQuestionSetNull() {
    TextInput input1 = new TextInput();
    TextInput input2 = new TextInput();

    input1.setQuestion(null);
    input2.setQuestion("question2");

    assertFalse(input1.equals(input2));
  }

  @Test
  public void testInequalityWhenMultilineSet() {
    TextInput input1 = new TextInput();
    TextInput input2 = new TextInput();

    input1.setMultiline(true);
    input2.setMultiline(false);

    assertFalse(input1.equals(input2));
  }

  @Test
  public void testQuestionNotNullable() {
    TextInput input = new TextInput();

    input.setQuestion(null);

    assertNotNull(input.getQuestion());
  }
}
