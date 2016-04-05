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

  protected Integer getBnVersion() {
    // TODO Auto-generated method stub
    return null;
  }

  protected Integer getElVersion() {
    // TODO Auto-generated method stub
    return null;
  }

  protected Integer getGuVersion() {
    // TODO Auto-generated method stub
    return null;
  }

  protected Integer getHiVersion() {
    // TODO Auto-generated method stub
    return null;
  }

  protected Integer getKnVersion() {
    // TODO Auto-generated method stub
    return null;
  }

  protected Integer getMrVersion() {
    // TODO Auto-generated method stub
    return null;
  }

  protected Integer getTaVersion() {
    // TODO Auto-generated method stub
    return null;
  }


}

