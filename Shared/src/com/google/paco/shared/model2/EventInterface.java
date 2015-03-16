package com.google.paco.shared.model2;

import org.joda.time.DateTime;

public interface EventInterface {

  DateTime getScheduledTime();

  DateTime getResponseTime();

}