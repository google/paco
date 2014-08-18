package com.google.sampling.experiential.client;

import java.util.Date;

import com.google.gwt.i18n.client.Messages;
import com.google.gwt.i18n.client.Constants.DefaultStringValue;

public interface MyMessages extends Messages {

  @DefaultMessage("Could not save your responses. Please try again.\n{0}")
  String saveFailed(String message);
  
  @DefaultMessage("{0,date,full} Responses (click to open)")
  String datedResponses(Date timestamp);
  
  @DefaultMessage("Hello, {0}")
  String hello(String string);

  @DefaultMessage("Could not retrieve your experiments!!\n{0}")
  String loadExperimentsFailed(String message);
  
  @DefaultMessage("Could not retrieve events referenced by your experiment.<br/>{0}")
  String loadReferencedEventsFailed(String message);
  
  @DefaultMessage("2b) Or, if you are browsing this page from your phone, just <a href=\"{0}\">click here to find Paco on the Play store</a>.")
  String downloadAppStep2b(String url);

  @DefaultMessage("Could not retrieve results for experiment: {0}")
  String loadEventsForExperimentFailed(String title);

}
