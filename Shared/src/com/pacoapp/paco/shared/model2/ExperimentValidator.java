package com.pacoapp.paco.shared.model2;

import java.util.Collection;
import java.util.List;

import com.pacoapp.paco.shared.util.TimeUtil;


public class ExperimentValidator implements Validator {


  private List<ValidationMessage>  results;

  public ExperimentValidator() {
    results = new java.util.ArrayList<ValidationMessage>();
  }

  @Override
  public void addError(String msg) {
    results.add(new ValidationMessage(msg, Validator.MANDATORY));
  }

  public boolean isNonEmptyString(String value, String msg) {
    boolean b = isNullOrEmptyString(value);
    if (b) {
      addError(msg);
      return false;
    }
    return true;
  }

  public boolean isNullOrEmptyString(String value) {
    return value == null || value.length() == 0;
  }

  @Override
  public boolean isNotNullAndNonEmptyCollection(Collection collection, String message) {
    boolean empty = collection == null || collection.isEmpty();
    if (empty) {
      addError(message);
    }
    return empty;
  }

  /**
   * TODO replace this with a real email address validator
   */
  @Override
  public boolean isValidEmail(String address, String errorMessage) {
    if (!isNonEmptyString(address, errorMessage)) {
      return false;
    }
    int atIndex = address.indexOf('@');
    if (atIndex == -1) {
      addError(errorMessage);
      return false;
    }
    String namePart = address.substring(0, atIndex);
    String domainPart = address.substring(atIndex + 1);
    if (!isNonEmptyString(namePart, errorMessage)) {
      return false;
    }
    if (!isNonEmptyString(domainPart, errorMessage)) {
      return false;
    }
    if (domainPart.indexOf('.') == -1) {
      addError(errorMessage);
      return false;
    }
    return true;
  }

  @Override
  public boolean isValidCollectionOfEmailAddresses(Collection<String> collection, String errorMessage) {
    if (!isNotNullAndNonEmptyCollection(collection, errorMessage)) {
      return false;
    }
    for (String email : collection) {
      if (!isValidEmail(email, errorMessage)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean isNotNullCollection(Collection collection, String errorMessage) {
    if (collection == null) {
      addError(errorMessage);
      return false;
    }
    return true;
  }

  /**
   * TODO replace with real date formatter that is serializable
   */
  @Override
  public boolean isValidDateString(String dateStr, String errorMessage) {
    if (!isNonEmptyString(dateStr, errorMessage)) {
      return false;
    }

    try {
      TimeUtil.dateFormatter.parseDateTime(dateStr);
      return true;
    } catch (Exception e) {
      return false;
    }
//
//    String[] ymd = dateStr.split("/");
//    if (!isArrayWithNElements(3, ymd, errorMessage)) {
//      return false;
//    }
//    if (!isValid4DigitYear(ymd[0], errorMessage)) {
//      return false;
//    }
//    if (!isValid2DigitMonth(ymd[1], errorMessage)) {
//      return false;
//    }
//    if (!isValid2DigitDay(ymd[2], errorMessage)) {
//      return false;
//    }
//    return true;
  }

  private boolean isValid2DigitMonth(String monthString, String errorMessage) {
    if (monthString.length() != 2) {
      addError(errorMessage);
      return false;
    }
    int month;
    try {
      month = Integer.parseInt(monthString);
    } catch (NumberFormatException nfe) {
      addError(errorMessage);
      return false;
    }
    if (month < 0 || month > 12) {
      addError(errorMessage);
      return false;
    }
    return true;
  }

  private boolean isValid2DigitDay(String dayString, String errorMessage) {
    if (dayString.length() != 2) {
      addError(errorMessage);
      return false;
    }
    int day;
    try {
      day = Integer.parseInt(dayString);
    } catch (NumberFormatException nfe) {
      addError(errorMessage);
      return false;
    }
    if (day < 0 || day > 31) {
      addError(errorMessage);
      return false;
    }
    return true;

  }


  public boolean isValid4DigitYear(String yearString, String errorMessage) {
    if (yearString.length() != 4) {
      addError(errorMessage);
      return false;
    }
    int year;
    try {
      year = Integer.parseInt(yearString);
    } catch (NumberFormatException nfe) {
      addError(errorMessage);
      return false;
    }
    return true;
  }

  public boolean isArrayWithNElements(int expectedLength, String[] arr, String errorMessage) {
    if (!isNonNullArray(arr, errorMessage)) {
      return false;
    }
    if (arr.length != expectedLength) {
      addError(errorMessage);
      return false;
    }
    return true;
  }

  public boolean isNonNullArray(String[] array, String errorMessage) {
    if (array == null) {
      addError(errorMessage);
      return false;
    }
    return true;
  }

  @Override
  public boolean isNotNull(Object obj, String errorMessage) {
    if (obj == null) {
      addError(errorMessage);
      return false;
    }
    return true;
  }

  /**
   * TODO do some real basic javascript linting, some real paco symbol checking,
   * some caja sandboxing, etc..
   */
  @Override
  public boolean isValidJavascript(String code, String errorMessage) {
    if (!isNonEmptyString(code, errorMessage)) {
      return false;
    }
    return true;
  }

  /**
   * TODO this is to validate simple html as well as javascript.
   * It is broader than isValidJavascript, but for now....
   */
  @Override
  public boolean isValidHtmlOrJavascript(String text, String errorMessage) {
   return isValidJavascript(text, errorMessage);
  }

  @Override
  public boolean isTrue(boolean b, String errorMessage) {
    if (!b) {
      addError(errorMessage);
      return false;
    }
    return true;
  }

  @Override
  public boolean isNotNullAndNonEmptyArray(String[] arr, String errorMessage) {
    if (arr == null || arr.length == 0) {
      addError(errorMessage);
      return false;
    }
    return true;
  }

  @Override
  public boolean isValidConditionalExpression(String conditionExpression, String errorMessage) {
    if (isNullOrEmptyString(conditionExpression)) {
      addError(errorMessage);
      return false;
    }
    // TODO validate the conditionExpression with the interpreter.
    // If it does not throw any errors, then we are good.
    return true;
  }

  public List<ValidationMessage> getResults() {
    return results;
  }

  public String stringifyResults() {
    StringBuilder buf = new StringBuilder();
    for (ValidationMessage msg : results) {
      buf.append(msg.toString());
      buf.append("\n");
    }
    return buf.toString();
  }

}
