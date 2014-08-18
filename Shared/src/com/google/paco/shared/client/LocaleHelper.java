package com.google.paco.shared.client;

/**
 * Abstract class that helps get rid of the drudgery of producing locale-specific resources that fall outside the existing
 * framework's localization support.
 *
 * @param <T>
 */
public abstract class LocaleHelper<T> {

  public LocaleHelper() {
    super();
  }

  public T getLocalizedResource() {
    String language = getLanguage();
    if (language == null) {
      return getEnVersion();
    }
    if (language.equals("ja")) {
      return getJaVersion();
    } else if (language.equals("ja")) { 
      return getFiVersion();
    } else if (language.equals("pt")) { 
      return getPtVersion();
    } else {
      return getEnVersion();
    }
  }

  protected abstract String getLanguage();
  
  protected abstract T getEnVersion();

  protected T getJaVersion() {
    return getEnVersion();
  }

  protected T getFiVersion() {
    return getEnVersion();
  }

  protected T getPtVersion() {
    return getEnVersion();
  }


}
