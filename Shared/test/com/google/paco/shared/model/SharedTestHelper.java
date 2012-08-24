// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import com.google.paco.shared.model.Schedule;
import com.google.paco.shared.model.Signal;
import com.google.paco.shared.model.SignalSchedule;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class SharedTestHelper {
  public static SignalSchedule createSignalSchedule(Signal signal, Schedule schedule) {
    SignalSchedule signalSchedule = new SignalSchedule();

    signalSchedule.setSignal(signal);
    signalSchedule.setSchedule(schedule);

    return signalSchedule;
  }
}
