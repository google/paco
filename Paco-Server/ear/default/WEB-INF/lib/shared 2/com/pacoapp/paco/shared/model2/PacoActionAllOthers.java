package com.pacoapp.paco.shared.model2;


public class PacoActionAllOthers extends PacoAction {

  String customScript;

  public PacoActionAllOthers() {
    super();
    this.type = "pacoActionAllOthers";
  }

  public String getCustomScript() {
    return customScript;
  }

  public void setCustomScript(String customScript) {
    this.customScript = customScript;
  }

  public void validateWith(Validator validator) {
    super.validateWith(validator);
//    System.out.println("VALIDATING PACOACTIONALLOTHES");
    if (getActionCode() != null && getActionCode().equals(EXECUTE_SCRIPT_ACTION_CODE)) {
      validator.isValidJavascript(customScript, "custom script for action " + ACTION_NAMES[getActionCode() - 1] + "should be valid javascript");
    }

  }


}
