package com.google.sampling.experiential.server;

import com.google.appengine.api.utils.SystemProperty;

public final class EnvironmentUtil {
  private EnvironmentUtil() {

  }
  
  public static boolean isDevInstance() {
    return SystemProperty.environment.value() == SystemProperty.Environment.Value.Development;
  }

}
