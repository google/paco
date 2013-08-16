package com.google.paco.shared;
import static org.junit.Assert.*;

import org.junit.Test;


public class OutcomeTest {

  @Test
  public void testDefaultCtor() {
    Outcome out = new Outcome(0);
    assertTrue(out.succeeded());
    assertNull(out.getErrorMessage());
  }
  
  @Test
  public void testErrorCtor() {
    String errorMessage = "failed event";
    Outcome out = new Outcome(0, errorMessage);
    assertFalse(out.succeeded());
    assertEquals(errorMessage, out.getErrorMessage());
  }
  

}
