package com.google.paco.shared.model;

public class FixedSignalIterator extends SignalIterator {
  public FixedSignalIterator(FixedSignal signal) {
    this.setTimes(signal.getTimes());
  }
}
