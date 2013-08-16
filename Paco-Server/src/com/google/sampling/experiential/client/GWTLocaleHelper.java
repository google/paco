package com.google.sampling.experiential.client;

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.paco.shared.client.LocaleHelper;

/**
 * GWT has it's own i18n library that we have to use to determine locale.
 *
 * @param <T> The resource type that you want to produce in a localized format.
 * 
 */
public abstract class GWTLocaleHelper<T> extends LocaleHelper<T> {

  protected String getLanguage() {
    String localeName = LocaleInfo.getCurrentLocale().getLocaleName();
    String language = localeName.substring(0,2);
    return language;
  }


}

