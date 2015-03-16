package com.google.paco.shared.util;

import java.util.ArrayList;
import java.util.List;

public class ListMaker {

  public static <T> List<T> paramOrNewList(List<T> list, Class<T> clazz) {
    if (list != null) {
      return list;
    }
    list = new ArrayList<T>();
  
  return list;
  }

}
