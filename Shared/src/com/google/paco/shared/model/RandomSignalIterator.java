package com.google.paco.shared.model;

import java.util.List;
import java.util.Random;
import java.util.Set;

import org.joda.time.Duration;
import org.joda.time.LocalTime;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class RandomSignalIterator extends SignalIterator {
  private static int SIGNAL_DURATION = 15; // minutes

  public RandomSignalIterator(RandomSignal signal, Random random) {
    List<LocalTime> times = generate(signal, random);

    if (times != null) {
      this.setTimes(times);
    }
  }

  /**
   * Generate min(frequency, duration / SIGNAL_DURATION) times between the start and end time
   * randomly spaced at SIGNAL_DURATION_IN_SECONDS intervals with an additional random
   * SIGNAL_DURATION amount of minutes.
   * 
   * @param seed a seed for the random number generator
   */
  private List<LocalTime> generate(RandomSignal signal, Random random) {
    // Clear times
    List<LocalTime> times = Lists.newArrayList();

    // Check pre-conditions
    if (!signal.hasStartTime() || !signal.hasEndTime() || signal.getFrequency() < 1) {
     return null;
    }

    // Compute the number of SIGNAL_DURATION buckets
    Duration duration;

    if (signal.getStartTime().isAfter(signal.getEndTime())) {
      duration =
          new Duration(signal.getStartTime().toDateTimeToday(), signal.getEndTime()
              .toDateTimeToday().plusDays(1));
    } else {
      duration =
          new Duration(signal.getStartTime().toDateTimeToday(), signal.getEndTime()
              .toDateTimeToday());
    }

    int bucket_count = (int) duration.getStandardMinutes() / SIGNAL_DURATION;

    // Randomly choose min(frequency, bucket_size) buckets
    Set<Integer> selected = Sets.newHashSet();
    int count = (signal.getFrequency() > bucket_count ? bucket_count : signal.getFrequency());

    while (selected.size() < count) {
      selected.add(random.nextInt(bucket_count));
    }

    // Compute times from chosen buckets
    for (int value : selected) {
      int seconds = value * SIGNAL_DURATION + random.nextInt(SIGNAL_DURATION);
      times.add(signal.getStartTime().plusMinutes(seconds));
    }

    return times;
  }
}
