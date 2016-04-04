package com.pacoapp.paco.sensors.android.diagnostics;

import android.content.Context;

public abstract class Diagnostic<T> {

  private String name;
  private T value;

  public Diagnostic(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public T getValue() {
    return value;
  }

  public void setValue(T value) {
    this.value = value;
  }

  public abstract void run(Context context);

  @Override
  public String toString() {
    return getName() + " : " + getValue() != null ? getValue().toString() : "null";
  }

  

}
