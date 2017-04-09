package com.pacoapp.paco.sensors.android.diagnostics;

import java.util.List;

public abstract class ListDiagnostic extends Diagnostic<List<String>> {

  public ListDiagnostic(String name) {
    super(name);
  }

  @Override
  public String toString() {
    List<String> values = getValue();
    if (values == null) {
      return getName() + " : null";
    }
    String valueStr = "";
    boolean first = true;
    for (String value : values) {
      if (first) {
        first = false;
      } else {
        valueStr += "\n  ";
      }
      valueStr += value;
    }
    return getName() + " :\n  " + valueStr;
  }

}