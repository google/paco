// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import static org.junit.Assert.*;

import com.google.paco.shared.model.Input;

import org.junit.Test;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class InputTest {
  private class InputImpl extends Input {
    public InputImpl(Type type) {
      super(type);
    }
  }

  @Test
  public void testEquality() {
    InputImpl input1 = new InputImpl(Input.Type.Text);
    InputImpl input2 = new InputImpl(Input.Type.Text);

    assertTrue(input1.equals(input2));
  }

  @Test
  public void testEqualityWhenNameSet() {
    InputImpl input1 = new InputImpl(Input.Type.Text);
    InputImpl input2 = new InputImpl(Input.Type.Text);

    input1.setName("name");
    input2.setName("name");

    assertTrue(input1.equals(input2));
  }

  @Test
  public void testEqualityWhenRequiredSet() {
    InputImpl input1 = new InputImpl(Input.Type.Text);
    InputImpl input2 = new InputImpl(Input.Type.Text);

    input1.setRequired(true);
    input2.setRequired(true);

    assertTrue(input1.equals(input2));
  }

  @Test
  public void testEqualityWhenConditionalExpressionSet() {
    InputImpl input1 = new InputImpl(Input.Type.Text);
    InputImpl input2 = new InputImpl(Input.Type.Text);

    input1.setConditionalExpression("expression");
    input2.setConditionalExpression("expression");

    assertTrue(input1.equals(input2));
  }

  @Test
  public void testEqualityWhenConditionalExpressionSetNull() {
    InputImpl input1 = new InputImpl(Input.Type.Text);
    InputImpl input2 = new InputImpl(Input.Type.Text);

    input1.setConditionalExpression(null);
    input2.setConditionalExpression(null);

    assertTrue(input1.equals(input2));
  }

  @Test
  public void testInequality() {
    InputImpl input1 = new InputImpl(Input.Type.Text);
    InputImpl input2 = new InputImpl(Input.Type.List);

    assertFalse(input1.equals(input2));
  }

  @Test
  public void testInequalityWhenNameSet() {
    InputImpl input1 = new InputImpl(Input.Type.Text);
    InputImpl input2 = new InputImpl(Input.Type.Text);

    input1.setName("name1");
    input2.setName("name2");

    assertFalse(input1.equals(input2));
  }

  @Test
  public void testInequalityWhenRequiredSet() {
    InputImpl input1 = new InputImpl(Input.Type.Text);
    InputImpl input2 = new InputImpl(Input.Type.Text);

    input1.setRequired(true);
    input2.setRequired(false);

    assertFalse(input1.equals(input2));
  }

  @Test
  public void testInequalityWhenConditionalExpressionSet() {
    InputImpl input1 = new InputImpl(Input.Type.Text);
    InputImpl input2 = new InputImpl(Input.Type.Text);

    input1.setConditionalExpression("expression1");
    input2.setConditionalExpression("expression2");

    assertFalse(input1.equals(input2));
  }

  @Test
  public void testInequalityWhenConditionalExpressionSetNull() {
    InputImpl input1 = new InputImpl(Input.Type.Text);
    InputImpl input2 = new InputImpl(Input.Type.Text);

    input1.setConditionalExpression(null);
    input2.setConditionalExpression("expression2");

    assertFalse(input1.equals(input2));
  }

  @Test
  public void testNameIsNotNullable() {
    InputImpl input = new InputImpl(Input.Type.Text);

    input.setName(null);

    assertNotNull(input.getName());
  }

  @Test
  public void testConditionalExpressionIsNullable() {
    InputImpl input = new InputImpl(Input.Type.Text);

    input.setConditionalExpression(null);

    assertNull(input.getConditionalExpression());
  }

  @Test
  public void testIsConditional() {
    InputImpl input = new InputImpl(Input.Type.Text);

    input.setConditionalExpression("expression");

    assertTrue(input.isConditional());
  }

  @Test
  public void testIsConditionalWhenNull() {
    InputImpl input = new InputImpl(Input.Type.Text);

    input.setConditionalExpression(null);

    assertFalse(input.isConditional());
  }
}
