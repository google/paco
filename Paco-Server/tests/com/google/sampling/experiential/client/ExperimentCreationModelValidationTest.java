package com.google.sampling.experiential.client;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Splitter;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.paco.shared.model.InputDAO;
import com.google.paco.shared.model.SignalScheduleDAO;
import com.google.paco.shared.model.SignalingMechanismDAO;
import com.google.paco.shared.model.TriggerDAO;

import junit.framework.TestCase;

public class ExperimentCreationModelValidationTest extends TestCase {
  
  private static final String LATER_DAY = "2013/25/07";
  private static final String EARLIER_DAY = "2013/24/07"; 
  
  private static final String VALID_EMAIL_STRING_0 = "donti@google.com, yimingzhang@google.com, rbe5000@gmail.com";
  private static final String VALID_EMAIL_STRING_1 = "donti@google.com,  yimingzhang@google.com,, rbe5000@gmail.com";
  private static final String VALID_EMAIL_STRING_2 = "donti@google.com, me@yahoo.co.uk";
  private static final String INVALID_EMAIL_STRING_0 = "donti@google.com\nyimingzhang@google.com\nrbe5000@gmail.com";
  private static final String INVALID_EMAIL_STRING_1 = "donti@google.com, yimingzhang@google.com, rbe5000@gmail.com]";
  private static final String INVALID_EMAIL_STRING_2 = "donti@google,com, yimingzhang@google.com, rbe5000@gmail.com";
  private static final String INVALID_EMAIL_STRING_3 = "donti@google, yimingzhang@google, rbe5000@gmail";
  private static final String INVALID_EMAIL_STRING_4 = "donti@google.com, yimingzhang@google.com, rbe5000@@gmail.com";
  
  private static final String NAME_WITH_SPACES = "name With spaces";
  private static final String NAME_WITHOUT_SPACES = "nameWithoutSpaces";
  private static final String NAME_STARTING_WITH_NUMBER = "9apPples";
  private static final String NAME_WITH_INVALID_CHARACTERS = "rstl*&E";
  
  private static final int NEG_NUM = -90;
  private static final int ZERO = 0;
  private static final int VALID_TIMEOUT = 80;
  
  private ExperimentDAO experiment;
  
  protected void setUp() {
    experiment = new ExperimentDAO();
  }
  
  public void testExperimentModelBlocksInvalidTitle() {
    try {
      experiment.setTitle("");
      assertTrue(false);
    } catch (IllegalArgumentException e) {
      assertTrue(true);
    }
  }
  
  public void testExperimentModelCorrectlyValidatesEmailAddresses() {
    assertTrue(experiment.emailListIsValid(tokenize(VALID_EMAIL_STRING_0)));
    assertTrue(experiment.emailListIsValid(tokenize(VALID_EMAIL_STRING_1)));
    assertTrue(experiment.emailListIsValid(tokenize(VALID_EMAIL_STRING_2)));
    assertFalse(experiment.emailListIsValid(tokenize(INVALID_EMAIL_STRING_0)));
    assertFalse(experiment.emailListIsValid(tokenize(INVALID_EMAIL_STRING_1)));
    assertFalse(experiment.emailListIsValid(tokenize(INVALID_EMAIL_STRING_2)));
    assertFalse(experiment.emailListIsValid(tokenize(INVALID_EMAIL_STRING_3)));
    assertFalse(experiment.emailListIsValid(tokenize(INVALID_EMAIL_STRING_4)));
  }
  
  // Copied from ExperimentDescriptionPanel/ExperimentPublishingPanel.
  // TODO: consolidate.
  private String[] tokenize(String emailString) {
    List<String> emails = new ArrayList<String>();
    Splitter sp = Splitter.on(",").trimResults().omitEmptyStrings();
    for (String admin : sp.split(emailString)) {
      emails.add(admin);
    }
    String[] emailStrArray = new String[emails.size()];
    emailStrArray = emails.toArray(emailStrArray);
    return emailStrArray;
  }
  
  public void testExperimentModelAcceptsValidFixedDuration() {
    try {
      experiment.setFixedDuration(true);
      experiment.setStartDate(EARLIER_DAY);
      experiment.setEndDate(LATER_DAY);
      assertTrue(true);
    } catch (IllegalArgumentException e) {
      assertTrue(false);
    }
  }
  
  public void testInputModelAllowsValidName() {
    InputDAO input = new InputDAO();
    try {
      input.setName(NAME_WITHOUT_SPACES);
      assertTrue(true);
    } catch (IllegalArgumentException e) {
      assertTrue(false);
    }
  }

  public void testInputModelBlocksBlankName() {
    InputDAO input = new InputDAO();
    try {
      input.setName("");
      assertTrue(false);
    } catch (IllegalArgumentException e) {
      assertTrue(true);
    }
  }

  public void testInputModelBlocksSpaceyName() {
    InputDAO input = new InputDAO();
    try {
      input.setName(NAME_WITH_SPACES);
      assertTrue(false);
    } catch (IllegalArgumentException e) {
      assertTrue(true);
    }
  }

  public void testInputModelBlocksNameWithInvalidCharacters() {
    InputDAO input = new InputDAO();
    try {
      input.setName(NAME_WITH_INVALID_CHARACTERS);
      assertTrue(false);
    } catch (IllegalArgumentException e) {
      assertTrue(true);
    }
  }

  public void testInputModelBlocksNameStartingWithNumber() {
    InputDAO input = new InputDAO();
    try {
      input.setName(NAME_STARTING_WITH_NUMBER);
      assertTrue(false);
    } catch (IllegalArgumentException e) {
      assertTrue(true);
    }
  }
  
  public void testInputModelDisallowsBlankFirstListItem() {
    InputDAO input = CreationTestUtil.createValidListInput();
    try {
      input.setListChoiceAtIndex(0, "");
      assertTrue(false);
    } catch (IllegalArgumentException e) {
      assertTrue(true);
    }
  } 

  public void testInputModelDisallowsInvalidLikertSteps() {
    InputDAO input = CreationTestUtil.createValidListInput();
    try {
      input.setLikertSteps(NEG_NUM);
      assertTrue(false);
    } catch (IllegalArgumentException e) {
      assertTrue(true);
    }
    try {
      input.setLikertSteps(ZERO);
      assertTrue(false);
    } catch (IllegalArgumentException e) {
      assertTrue(true);
    }
  } 
  
  public void testSignalModelAllowsValidTimeout() {
    SignalingMechanismDAO signal = new SignalScheduleDAO();
    SignalingMechanismDAO trigger = new TriggerDAO();
    try {
      signal.setTimeout(VALID_TIMEOUT);
      trigger.setTimeout(VALID_TIMEOUT);
      assertTrue(true);
    } catch (IllegalArgumentException e) {
      assertTrue(false);
    }
  }

  public void testSignalModelBlocksInvalidTimeout() {
    SignalingMechanismDAO signal = new SignalScheduleDAO();
    SignalingMechanismDAO trigger = new TriggerDAO();
    try {
      signal.setTimeout(NEG_NUM);
      assertTrue(false);
    } catch (IllegalArgumentException e) {
      assertTrue(true);
    }
    try {
      trigger.setTimeout(NEG_NUM);
      assertTrue(false);
    } catch (IllegalArgumentException e) {
      assertTrue(true);
    }
  }

}
