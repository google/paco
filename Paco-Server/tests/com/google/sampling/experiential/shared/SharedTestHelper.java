// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.shared;

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
