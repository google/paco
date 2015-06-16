package com.pacoapp.paco.utils;

import java.util.Locale;

import com.pacoapp.paco.shared.client.LocaleHelper;

public abstract class AndroidLocaleHelper<T> extends LocaleHelper<T>{

  @Override
  protected String getLanguage() {
    return getLocale();
  }

  public static String getLocale() {
    return Locale.getDefault().getLanguage();
  }


}

