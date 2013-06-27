package com.google.sampling.experiential.datastore;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Paco Server tests
 * @author Bob Evans
 */
public class AllTests extends TestCase {
  public static Test suite() {
    Class[] tests = new Class[] {
      com.google.sampling.experiential.datastore.JsonConverterTest.class
    };
    return new TestSuite(tests);
  }
}