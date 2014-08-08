package com.google.android.apps.paco.utils;

public class CallingStack {


  public static Exception callChain() {
    try {
      throw new Exception();
    } catch (Exception e) {
      return e;
    }
  }
}
