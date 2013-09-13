package com.google.sampling.experiential.client;

import com.google.common.base.Preconditions;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.paco.shared.model.InputDAO;
import com.google.paco.shared.model.SignalScheduleDAO;
import com.google.paco.shared.model.SignalingMechanismDAO;
import com.google.sampling.experiential.shared.LoginInfo;

public class CreationTestUtil {

  public static final String EMAIL = "bobevans@google.com";
  public static final String NICKNAME = "Bob Evans";

  public static ExperimentDAO getEmptyExperiment() {
    return new ExperimentDAO();
  }

  public static ExperimentDAO createValidOngoingExperiment() {
    ExperimentDAO experiment = new ExperimentDAO();
    experiment.setTitle("title");
    experiment.setInformedConsentForm("informed consent");
    experiment.getSignalGroups()[0].setFixedDuration(false);
    experiment.getSignalGroups()[0].setInputs(new InputDAO[]{createValidNameInput(InputDAO.LIKERT)});
    experiment.getSignalGroups()[0].setSignalingMechanisms(new SignalingMechanismDAO[] {new SignalScheduleDAO()});
    return experiment;
  }

  public static InputDAO createValidNameInput(String type) {
    Preconditions.checkArgument(!type.equals(InputDAO.LIST));
    return createInput(type, "inputName");
  }

  public static InputDAO createInput(String type, String name) {
    InputDAO input = new InputDAO(null, name, null, "");
    input.setResponseType(type);
    return input;
  }

  public static InputDAO createValidListInput() {
    InputDAO input = new InputDAO(null, "inputName", null, "inputPrompt");
    input.setResponseType(InputDAO.LIST);
    input.setListChoices(new String[]{"option1"});
    return input;
  }

  public static ExperimentCreationPanel createExperimentCreationPanel(ExperimentDAO experiment) {
    return new ExperimentCreationPanel(experiment, createLoginInfo(), null);
  }

  public static LoginInfo createLoginInfo() {
    LoginInfo info = new LoginInfo();
    info.setLoggedIn(true);
    info.setEmailAddress(EMAIL);
    info.setNickname(NICKNAME);
    info.setWhitelisted(true);
    return info;
  }

}
