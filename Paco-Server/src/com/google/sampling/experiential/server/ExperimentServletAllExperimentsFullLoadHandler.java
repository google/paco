package com.google.sampling.experiential.server;

import org.joda.time.DateTimeZone;

public class ExperimentServletAllExperimentsFullLoadHandler extends ExperimentServletHandler {

  public ExperimentServletAllExperimentsFullLoadHandler(String email, DateTimeZone timezone, Integer limit, String cursor, String pacoProtocol) {
    super(email, timezone, limit, cursor, pacoProtocol);
  }


}
