package com.google.paco.shared.model;

import junit.framework.TestCase;

public class InputDAOTest extends TestCase {

  private InputDAO input;

  @Override
  protected void setUp() throws Exception {
    input = new InputDAO();
  }

  public void testNameWithChars() throws Exception {
    input.setName("abc");
    assertTrue(true);
  }

  public void testNameWithCharsAndNums() throws Exception {
    input.setName("abc9");
    assertTrue(true);
  }

  public void testStartWithNumFails() throws Exception {
    try {
      input.setName("9abc");
      fail("should not start with number");
    } catch (IllegalArgumentException iae) {
    }
  }

  public void testNameWithDashesAndUnderscoresAllowed() throws Exception {
    input.setName("abc-");
    input.setName("abc_def");
    input.setName("abc_");
    input.setName("abc-def");
    assertTrue(true);
  }

  public void testStartWithDashAndUnderscoreFails() throws Exception {
    try {
      input.setName("_def");
      fail("should not start with _");
    } catch (IllegalArgumentException iae) {
    }

    try {
      input.setName("-def");
      fail("should not start with -");
    } catch (IllegalArgumentException iae) {
    }
  }

  public void testNoSpacesAllowed() throws Exception {
    try {
      input.setName(" def");
      fail("should not start with ' '");
    } catch (IllegalArgumentException iae) {
    }

    try {
      input.setName("def ");
      fail("should not end with ' '");
    } catch (IllegalArgumentException iae) {
    }

    try {
      input.setName("abc def");
      fail("should not contain with ' '");
    } catch (IllegalArgumentException iae) {
    }
  }

}
