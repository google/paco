package com.pacoapp.paco.shared.model2;


public class Pair<T1, T2> implements Comparable<T1>{

  public T1 first;
  public T2 second;

  public Pair(T1 first, T2 second) {
    this.first = first;
    this.second = second;
  }

  @Override
  public int compareTo(T1 o) {
    return this.compareTo(o);
  }

  @Override
  public int hashCode() {
    return first.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    return ((Pair<T1, T2>)obj).first.equals(first);
  }



}
