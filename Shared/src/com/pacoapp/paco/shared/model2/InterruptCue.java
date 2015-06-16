package com.pacoapp.paco.shared.model2;


public class InterruptCue implements Validatable, java.io.Serializable {

  public static final int PHONE_HANGUP = 1;
  public static final int USER_PRESENT = 2;
  public static final int PACO_ACTION_EVENT = 3;
  public static final int APP_USAGE = 4;
  public static final int APP_CLOSED = 5;
  public static final int MUSIC_STARTED = 6;
  public static final int MUSIC_STOPPED = 7;
  public static final int PHONE_INCOMING_CALL_STARTED = 8;
  public static final int PHONE_INCOMING_CALL_ENDED = 9;
  public static final int PHONE_OUTGOING_CALL_STARTED = 10;
  public static final int PHONE_OUTGOING_CALL_ENDED = 11;
  public static final int PHONE_MISSED_CALL = 12;
  public static final int PHONE_CALL_STARTED = 13;
  public static final int PHONE_CALL_ENDED = 14;
  public static final int PACO_EXPERIMENT_JOINED_EVENT = 15;
  public static final int PACO_EXPERIMENT_ENDED_EVENT = 16;
  public static final int PACO_EXPERIMENT_RESPONSE_RECEIVED_EVENT = 17;

  public static final int[] CUE_EVENTS = new int[] {PHONE_HANGUP, USER_PRESENT, PACO_ACTION_EVENT, APP_USAGE, APP_CLOSED, MUSIC_STARTED, MUSIC_STOPPED,
                                                PHONE_INCOMING_CALL_STARTED, PHONE_INCOMING_CALL_ENDED,
                                                PHONE_OUTGOING_CALL_STARTED, PHONE_OUTGOING_CALL_ENDED, PHONE_MISSED_CALL, PHONE_CALL_STARTED, PHONE_CALL_ENDED,
                                                PACO_EXPERIMENT_JOINED_EVENT,
                                                PACO_EXPERIMENT_ENDED_EVENT, PACO_EXPERIMENT_RESPONSE_RECEIVED_EVENT};
  public static final String[] CUE_EVENT_NAMES = new String[] {"HANGUP (deprecated)", "USER_PRESENT", "Paco Action",
                                                           "App Started", "App Stopped",
                                                           "Music Started", "Music Stopped",
                                                           "Incoming call started", "Incoming call ended",
                                                           "Outgoing call started", "Outgoing call ended",
                                                           "Missed call", "Call started (in or out)", "Call ended (in or out)",
                                                           "Experiment joined", "Experiment ended", "Response received"};






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
//    System.out.println("VALIDATING CUE");
    validator.isNotNull(cueCode, "cue code is not properly initialized");
    if (cueCode == PACO_ACTION_EVENT || cueCode == APP_USAGE) {
      validator.isNotNullAndNonEmptyString(cueSource,
                                           "cuesource must be valid for cuecode: " + CUE_EVENT_NAMES[cueCode - 1]);
    }
  }
}
