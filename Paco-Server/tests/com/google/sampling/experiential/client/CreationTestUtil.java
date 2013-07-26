package com.google.sampling.experiential.client;

import com.google.paco.shared.model.ExperimentDAO;
import com.google.sampling.experiential.shared.LoginInfo;

public class CreationTestUtil {
  
  public static final String EMAIL = "bobevans@google.com";
  public static final String NICKNAME = "Bob Evans";

  public static LoginInfo createLoginInfo() {
    LoginInfo info = new LoginInfo();
    info.setLoggedIn(true);
    info.setEmailAddress(EMAIL);
    info.setNickname(NICKNAME);
    info.setWhitelisted(true);
    return info;
  }
  
  public static ExperimentDAO getEmptyExperiment() {
    return new ExperimentDAO();
  }
  
}
