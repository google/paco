package com.google.android.apps.paco;


public class Pair {
  String first;
  String second;
  
  public Pair() {
    super();
  }
  
  public void config(String first, String second) {
    this.first = first;
    this.second = second;
  }

  public String getFirst() {
    return first;
  }
  public String dothing() {
    return "thing!";
  }
  public void setFirst(String first) {
    this.first = first;
  }
  public String getSecond() {
    return second;
  }
  public void setSecond(String second) {
    this.second = second;
  }
  
}