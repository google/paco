package com.google.paco.shared.model2;


public abstract class PacoAction implements Validatable, java.io.Serializable {

  public static final int NOTIFICATION_TO_PARTICIPATE_ACTION_CODE = 1;
  public static final int NOTIFICATION_ACTION_CODE = 2;
  public static final int LOG_EVENT_ACTION_CODE = 3;
  public static final int EXECUTE_SCRIPT_ACTION_CODE = 4;
  public static final int[] ACTIONS = new int[]{NOTIFICATION_TO_PARTICIPATE_ACTION_CODE ,NOTIFICATION_ACTION_CODE, LOG_EVENT_ACTION_CODE,EXECUTE_SCRIPT_ACTION_CODE};
  public static final String[] ACTION_NAMES = new String[] {"Create notification to participate", "Create notification message", "Log data", "Execute script"};
  private Integer actionCode = NOTIFICATION_TO_PARTICIPATE_ACTION_CODE;

  protected String type;

  public PacoAction() {
    super();
  }

  /**
   *  The main work of an action is to be called.
   * @return success or failure
   */
  public boolean execute() {
    return false;
  }

  public Integer getActionCode() {
    return actionCode;
  }

  public void setActionCode(Integer actionCode) {
    this.actionCode = actionCode;
  }

  public void validateWith(Validator validator) {
    validator.isNotNull(actionCode, "action code is not properly initialized");
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

}