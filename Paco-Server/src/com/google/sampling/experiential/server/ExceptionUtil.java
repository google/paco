package com.google.sampling.experiential.server;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ExceptionUtil {

  public static String getStackTraceAsString(Throwable e) {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream pw = new PrintStream(out);
    e.printStackTrace(pw);
    final String string = out.toString();
    return string;
  }
}
