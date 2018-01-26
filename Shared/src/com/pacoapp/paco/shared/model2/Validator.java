package com.pacoapp.paco.shared.model2;

import java.util.Collection;



public interface Validator {

  int MANDATORY = 1;
  int OPTIONAL = 2;

  void addError(String errorMessage);

  boolean isNonEmptyString(String value, String errorMsg);

  boolean isNotNullAndNonEmptyCollection(Collection collection, String errorMessage);

  boolean isValidEmail(String address, String errorMessage);

  boolean isValidCollectionOfEmailAddresses(Collection<String> collection, String errorMessage);

  boolean isNotNullCollection(Collection actionTriggers, String errorMessage);

  boolean isValidDateString(String dateStr, String errorMessage);

  boolean isNotNull(Object obj, String errorMessage);

  boolean isValidJavascript(String customRenderingCode, String errorMessage);

  boolean isValidHtmlOrJavascript(String text, String errorMessage);

  boolean isTrue(boolean b, String string);

  boolean isNotNullAndNonEmptyArray(String[] arr, String errorMessage);

  boolean isValidConditionalExpression(String conditionExpression, String errorMessage);

}
