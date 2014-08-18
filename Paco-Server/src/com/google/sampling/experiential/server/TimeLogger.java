package com.google.sampling.experiential.server;

public class TimeLogger {

  private static long lastTime;
  
  public static void logTimestamp(String label) {
    if (lastTime == 0) {
      System.out.println(label);      
    } else {
      System.out.println(label + (System.currentTimeMillis() - lastTime));
    }
    lastTime = System.currentTimeMillis();
  }

}
