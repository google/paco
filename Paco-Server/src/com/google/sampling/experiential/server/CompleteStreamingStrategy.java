package com.google.sampling.experiential.server;

import java.util.logging.Logger;

public class CompleteStreamingStrategy extends StreamingStrategyImpl {
  public static final Logger log = Logger.getLogger(CompleteStreamingStrategy.class.getName());

  @Override
  boolean isCreateTableNeeded() {
    // TODO Auto-generated method stub
    return true;
  }
}
