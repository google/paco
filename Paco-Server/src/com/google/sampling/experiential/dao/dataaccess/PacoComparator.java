package com.google.sampling.experiential.dao.dataaccess;

public interface PacoComparator<T> {
  boolean hasChanged(T olderVersion);
}
