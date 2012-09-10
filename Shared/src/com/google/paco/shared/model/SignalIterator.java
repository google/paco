package com.google.paco.shared.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.joda.time.LocalTime;

import com.google.common.collect.Lists;

public abstract class SignalIterator implements Iterator<LocalTime> {
  private int index;
  private List<LocalTime> times;

  public void setTimes(Collection<LocalTime> times) {
    this.times = Lists.newArrayList(times);
    this.index = 0;

    Collections.sort(this.times);
  }

  public void advanceTo(LocalTime newStart) {
    if (times == null) {
      return;
    }

    index = 0;

    while (index < times.size() && !times.get(index).isAfter(newStart)) {
      index += 1;
    }
  }

  @Override
  public boolean hasNext() {
    if (times == null) {
      return false;
    }

    return (index < times.size() && times.size() > 0);
  }

  @Override
  public LocalTime next() {
    if (times == null || times.size() == 0) {
      return null;
    }

    return times.get(index++);
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
