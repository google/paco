package com.google.android.apps.paco;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.webkit.JavascriptInterface;

class Environment {


  private HashMap<String, String> map;

  public Environment(Map<String,String> map) {
    super();
    this.map = new HashMap<String, String>();
    this.map.putAll(map);
  }

  @JavascriptInterface
  public String getValue(String key) {
    return map.get(key);
  }

  @JavascriptInterface
  public String put(String key, String value) {
    return this.map.put(key, value);
  }

  @JavascriptInterface
  public List<String> getKeys() {
    return new ArrayList<String>(map.keySet());
  }
}