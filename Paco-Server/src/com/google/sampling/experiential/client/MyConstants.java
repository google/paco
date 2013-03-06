package com.google.sampling.experiential.client;

import com.google.gwt.i18n.client.Constants;
import com.google.gwt.safehtml.shared.SafeHtml;

public interface MyConstants extends Constants {

  @DefaultStringValue("Save")
  String save();

  @DefaultStringValue("Cancel")
  String cancel();

  @DefaultStringValue("Success!")
  String success();

  @DefaultStringValue("TODAY'S RESPONSES")
  String todaysResponses();

  @DefaultStringValue("PREVIOUS DAYS' RESPONSES")
  String previousDaysResponses();

  @DefaultStringValue("Note: Previous Answers will not be displayed but they have been recorded")
  String previousResponsesWarning();

  @DefaultStringValue("Scheduled Time")
  String scheduledTime();

  @DefaultStringValue("Response Time")
  String responseTime();

  @DefaultStringValue("Responses")
  String responses();
  
  @DefaultStringValue("Respond")
  String respond();
  
  @DefaultStringValue("Login")
  String login();
  
  @DefaultStringValue("Logout")
  String logout();
  
  @DefaultStringValue("Help")
  String help();
  
  @DefaultStringValue("Get Android App")
  String getAndroid();
  
  @DefaultStringValue("Administer Experiments")
  String administerExperiments();
  
  @DefaultStringValue("Experiments You Joined")
  String joinedExperiments();
  
  @DefaultStringValue("User Guide")
  String userGuide();
  
  @DefaultStringValue("Show All")
  String showAll();
  
  @DefaultStringValue("Create New")
  String createNew();
  
  @DefaultStringValue("Create New Experiment")
  String createNewExperiment();


  @DefaultStringValue("About")
  String about();
  
  @DefaultStringValue("View Data")
  String viewData();

  @DefaultStringValue("Charts")
  String charts();

  @DefaultStringValue("Stats")
  String stats();
 
  @DefaultStringValue("CSV")
  String csv();

  @DefaultStringValue("Copy")
  String copy();

  @DefaultStringValue("Welcome to the Daily Information Needs Study")
  String welcomeDIN();
  
  @DefaultStringValue("Paco Experiment Dashboard")
  String pacoPageTitle();

  @DefaultStringValue("Loading")
  String loading();

  @DefaultStringValue("Find Experiments")
  String findExperiments();

  @DefaultStringValue("Download the PACO Android Client")
  String downloadAppTitle();

  @DefaultStringValue("1) Ensure that you can install applications from Unknown Sources.")
  String downloadAppStep1a();

  @DefaultStringValue("On your phone, open the 'Settings' app. Click 'Applications' and check 'Unknown Sources'.")
  String downloadAppStep1b();

  @DefaultStringValue("2a) Scan this code with your phone which will launch the browser and download Paco.")
  String downloadAppStep2a();

  @DefaultStringValue("No events found for referenced experiment.")
  String noEventsFoundForReferredExperiment();

    

}
