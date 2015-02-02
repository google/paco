package com.google.android.apps.paco;

class JavascriptEmail {
  /**
   * 
   */
  private final ExperimentExecutorCustomRendering innerType;

  /**
   * @param experimentExecutorCustomRendering
   */
  JavascriptEmail(ExperimentExecutorCustomRendering experimentExecutorCustomRendering) {
    innerType = experimentExecutorCustomRendering;
  }

  public void sendEmail(String body, String subject, String userEmail) {
    innerType.sendEmail(body, subject, userEmail);
  }
}