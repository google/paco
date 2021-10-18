package com.pacoapp.paco.js.bridge;

public class JavascriptStringHolder {
  private String string;

  public JavascriptStringHolder(String string) {
    this.string = string;
  }

  @android.webkit.JavascriptInterface
  public String getStringValue() {
    return string;
  }
}
