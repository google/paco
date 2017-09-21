package com.google.sampling.experiential.server;

public final class EnvironmentUtil {
  private static final String RUN_TIME_VERSION_PROPERTY = "com.google.appengine.runtime.version";
  private static final String DEV_HEADER = "Google App Engine/";
  
  private EnvironmentUtil() {

  }
 
  public static final boolean isDevServer(){
    return !System.getProperty(RUN_TIME_VERSION_PROPERTY).startsWith(DEV_HEADER);
 }

}
