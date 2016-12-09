package com.google.sampling.experiential.server;

import java.util.logging.Logger;

public class IncrementalStreamingStrategy extends StreamingStrategyImpl {

  private static final Logger log = Logger.getLogger(IncrementalStreamingStrategy.class.getName());

  @Override
  boolean isCreateTableNeeded() {
    // TODO Auto-generated method stub
    return false;
  }
}
