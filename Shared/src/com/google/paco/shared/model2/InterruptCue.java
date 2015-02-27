package com.google.paco.shared.model2;


public class InterruptCue implements Validatable, java.io.Serializable {

  public static final int PHONE_HANGUP = 1;
  public static final int USER_PRESENT = 2;
  public static final int PACO_ACTION_EVENT = 3;
  public static final int APP_USAGE = 4;
  public static final int PHONE_STARTED = 5;


  public static final int[] CUE_EVENTS = new int[] {PHONE_HANGUP, USER_PRESENT,
                                                    PACO_ACTION_EVENT, APP_USAGE, PHONE_STARTED};
  public static final String[] CUE_EVENT_NAMES = new String[] {"Phone call ended", "User present",
                                                               "Paco action", "App used",
                                                               "Phone call started"};


  private Integer cueCode;
  private String cueSource;

  public InterruptCue() {
    super();
    cueCode = 0;
  }

  public Integer getCueCode() {
    return cueCode;
  }

  public void setCueCode(Integer cueCode) {
    this.cueCode = cueCode;
  }

  public String getCueSource() {
    return cueSource;
  }

  public void setCueSource(String cueSource) {
    this.cueSource = cueSource;
  }

  public void validateWith(Validator validator) {
    validator.isNotNull(cueCode, "cue code is not properly initialized");
    if (cueCode == PACO_ACTION_EVENT || cueCode == APP_USAGE) {
      validator.isNotNullAndNonEmptyString(cueSource,
                                           "cuesource must be valid for cuecode: " + CUE_EVENT_NAMES[cueCode - 1]);
    }
  }
}
