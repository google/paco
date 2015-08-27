package com.google.paco.shared.model2;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.pacoapp.paco.shared.model2.ExperimentValidator;
import com.pacoapp.paco.shared.model2.SignalTime;

public class ValidatorTest {

  @Test
  public void testSignalTimeValidator() {
    SignalTime st = new SignalTime(new Integer(0),
                                   SignalTime.OFFSET_BASIS_SCHEDULED_TIME,
                                   null, // shoudl throw validation error //1000 * 60 * 60 * 8,
                                   SignalTime.MISSED_BEHAVIOR_USE_SCHEDULED_TIME,
                                   0,
                                   "test time");

    ExperimentValidator experimentValidator = new ExperimentValidator();
    st.validateWith(experimentValidator);
    assertEquals("Should have an error", 1, experimentValidator.getResults().size());
  }


}
