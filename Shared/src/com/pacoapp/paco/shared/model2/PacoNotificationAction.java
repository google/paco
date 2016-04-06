package com.pacoapp.paco.shared.model2;


public class PacoNotificationAction extends PacoAction {

  public static final String DEFAULT_NOTIFICATION_MSG = "Time to participate";
  public static final int SNOOZE_TIME_DEFAULT = 600000; // 10 minutes (10min * 60sec * 1000ms)
  public static final int SNOOZE_COUNT_DEFAULT = 0;
  public static final String TRIGGER_SIGNAL_TIMEOUT = "59";
  public static final String ESM_SIGNAL_TIMEOUT = "59";
  public static final String FIXED_SCHEDULE_TIMEOUT = "479";
  public static final int DEFAULT_NOTIFICATION_DELAY = 5000;
  public static final int DEFAULT_COLOR = 0; 
  public static final boolean DEFAULT_DISMISSIBLE = true;


  protected Integer snoozeCount = SNOOZE_COUNT_DEFAULT;
  protected Integer snoozeTime = SNOOZE_TIME_DEFAULT;
  private Integer timeout;  //min? TODO findout
  private long delay = DEFAULT_NOTIFICATION_DELAY; // ms
  private Integer color = DEFAULT_COLOR;
  private Boolean dismissible = DEFAULT_DISMISSIBLE;

  private String msgText;
  
  public PacoNotificationAction(Integer snoozeCount, Integer snoozeTime, Integer timeout, long delay, String msgText, Integer color, Boolean dismissible) {
    super();
    this.type = "pacoNotificationAction";
    this.timeout = timeout;
    this.delay = delay;
    this.snoozeCount = (snoozeCount != null) ? snoozeCount : PacoNotificationAction.SNOOZE_COUNT_DEFAULT;
    this.snoozeTime = (snoozeTime != null) ? snoozeTime : PacoNotificationAction.SNOOZE_TIME_DEFAULT;
    this.msgText = msgText;
    this.color = color;
    this.dismissible = dismissible;
  }

  public PacoNotificationAction() {
    this(SNOOZE_COUNT_DEFAULT, SNOOZE_TIME_DEFAULT, Integer.parseInt(ESM_SIGNAL_TIMEOUT), DEFAULT_NOTIFICATION_DELAY, DEFAULT_NOTIFICATION_MSG, DEFAULT_COLOR, DEFAULT_DISMISSIBLE);
  }

  public Integer getTimeout() {
    return timeout;
  }

  public void setTimeout(Integer timeout) {
    this.timeout = timeout;
  }

  public Integer getSnoozeCount() {
    return snoozeCount;
  }

  public void setSnoozeCount(Integer snoozeCount) {
    this.snoozeCount = snoozeCount != null ? snoozeCount : PacoNotificationAction.SNOOZE_COUNT_DEFAULT;
  }

  public Integer getSnoozeTime() {
    return snoozeTime;
  }

  public void setSnoozeTime(Integer snoozeTime) {
    this.snoozeTime = snoozeTime != null ? snoozeTime : PacoNotificationAction.SNOOZE_TIME_DEFAULT;
  }

  public int getSnoozeTimeInMinutes() {
    return getSnoozeTime() / 1000 / 60;
  }

  public void setSnoozeTimeInMinutes(int minutes) {
    this.snoozeTime = minutes * 60 * 1000;
  }

  public String getMsgText() {
    return msgText;
  }

  public void setMsgText(String msgText) {
    this.msgText = msgText;
  }

  public long getDelay() {
    return delay;
  }

  public void setDelay(long delay) {
    this.delay = delay;
  }
  
  public Integer getColor(){
	  return color;
  }

  public void setColor(Integer color){
    this.color = color;
  }

  public Boolean getDismissible(){
    return dismissible;
  }

  public void setDismissible(Boolean dismissible){
    this.dismissible = dismissible;
  }

  public void validateWith(Validator validator) {
    super.validateWith(validator);
//    System.out.println("VALIDATING PACONOTIFICATIONACTION");
    // need to detect if we are an action for InterruptTrigger
    validator.isNotNull(delay, "delay is not properly initialized for PacoNotificationActions for InterruptTriggers");
    validator.isNotNull(msgText, "msgText is not properly initialized");
    validator.isNotNull(snoozeCount, "snoozeCount is not properly initialized");
    if (snoozeCount > 0) {
      validator.isNotNull(snoozeTime, "snoozeTime must be properly initialized when snoozeCount is  > 0");
    }

  }



}
