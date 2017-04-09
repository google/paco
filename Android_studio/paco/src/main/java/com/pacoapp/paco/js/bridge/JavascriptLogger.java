package com.pacoapp.paco.js.bridge;

import java.util.logging.Logger;

import android.webkit.JavascriptInterface;

public class JavascriptLogger {

  private Logger logger;

  public JavascriptLogger() {
    logger = Logger.getLogger("SCRIPT_EXECUTOR");
  }

  @JavascriptInterface
  public void info(String message) {
     logger.info(message);
  }

  @JavascriptInterface
  public void error(String message) {
    logger.severe(message);
  }

}