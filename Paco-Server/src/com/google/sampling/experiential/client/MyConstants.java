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

  @DefaultStringValue("Days")
  String repeatTypeDays();
  
  @DefaultStringValue("Weeks")
  String repeatTypeWeeks();

  @DefaultStringValue("Repeat on")
  String repeatOn();

  @DefaultStringValue("Months")
  String repeatTypeMonths();

    


  @DefaultStringValue("Signal Time (s)")
  String signalTimes();

  @DefaultStringValue("Timeout")
  String timeout();

  @DefaultStringValue("Repeat every")
  String repeatEvery();

  @DefaultStringValue("minutes")
  String minutes();

  @DefaultStringValue("Frequency")
  String frequency();

  @DefaultStringValue("Period")
  String period();

  @DefaultStringValue("Include weekends")
  String includeWeekends();

  @DefaultStringValue("Start Time")
  String startTime();

  @DefaultStringValue("End Time")
  String endTime();

  @DefaultStringValue("S")
  String sundayInitial();
  
  @DefaultStringValue("M")
  String mondayInitial();
  
  @DefaultStringValue("T")
  String tuesdayInitial();
  
  @DefaultStringValue("W")
  String wednesdayInitial();
  
  @DefaultStringValue("T")
  String thursdayInitial();
  
  @DefaultStringValue("SF")
  String fridayInitial();
  
  @DefaultStringValue("S")
  String satdayInitial();

  @DefaultStringValue("byGroup")
  String byGroup();

  @DefaultStringValue("Day Of Month")
  String dayOfMonth();

  @DefaultStringValue("Day Of Week")
  String dayOfWeek();

  @DefaultStringValue("By")
  String by();

  @DefaultStringValue("First")
  String nthWeekOfMonthFirst();

  @DefaultStringValue("Second")
  String nthWeekOfMonthSecond();

  @DefaultStringValue("Third")
  String nthWeekOfMonthThird();

  @DefaultStringValue("Fourth")
  String nthWeekOfMonthFourth();

  @DefaultStringValue("Fifth")
  String nthWeekOfMonthFifth();

  @DefaultStringValue("Experiment Icon")
  String experimentIcon();

  @DefaultStringValue("AnonCSV")
  String anonCsv();

  @DefaultStringValue("AnonMap")
  String anonMap();

  @DefaultStringValue("Unhide")
  String unHide();
  
  @DefaultStringValue("Hide")
  String hide();

  @DefaultStringValue("Purge")
  String purge();

  @DefaultStringValue("QR Code")
  String qrCode();

  @DefaultStringValue("EOD Ref")
  String eodRef();

  @DefaultStringValue("Experiment Definition")
  String experimentDefinition();

  @DefaultStringValue("Version")
  String experimentVersion();

  @DefaultStringValue("Custom Feedback")
  String customFeedback();

  @DefaultStringValue("Click to edit custom feedback")
  String clickToEditCustomFeedback();

  @DefaultStringValue("Click to close editing of custom feedback")
  String clickToCloseCustomFeedbackEditor();

  @DefaultStringValue("Enter custom feedback page using html and javascript")
  String customFeedbackInstructions();

  @DefaultStringValue("Title")
  String experimentTitle();

  @DefaultStringValue("Description (<500 chars)")
  String experimentDescription();

  @DefaultStringValue("Description")
  String experimentDescriptionNoPrompt();
  
  @DefaultStringValue("Creator")
  String experimentCreator();

  @DefaultStringValue("Enter at least one question")
  String enterAtLeastOneQuestion();

  @DefaultStringValue("Informed Consent Text")
  String informedConsent();

  @DefaultStringValue("Published")
  String published();

  @DefaultStringValue("Click to edit administrators")
  String clickToEditAdministrators();

  @DefaultStringValue("Click to close editing of administrators")
  String clickToCloseAdministratorEditor();

  @DefaultStringValue("Enter emails separated by commas of who can edit this experiment and see results.")
  String administratorEditorPrompt();

  @DefaultStringValue("Click to edit published audience")
  String clickToEditPublished();

  @DefaultStringValue("Click to close editing of published audience")
  String clickToClosePublishedEditor();

  @DefaultStringValue("Enter emails separated by commas. An empty list is public.")
  String publishedEditorPrompt();

  @DefaultStringValue("Create Experiment")
  String createExperiment();

  @DefaultStringValue("Update Experiment")
  String updateExperiment();

  @DefaultStringValue("Duration")
  String duration();

  @DefaultStringValue("Ongoing")
  String ongoingDuration();

  @DefaultStringValue("Fixed Length")
  String fixedDuration();

  @DefaultStringValue("Start Date")
  String startDate();

  @DefaultStringValue("End Date")
  String endDate();

  @DefaultStringValue("Limit 500 chars")
  String fiveHundredCharLimit();

  @DefaultStringValue("Condition to enable this question")
  String conditionalPrompt();

  @DefaultStringValue("e.g.")
  String eg();

  @DefaultStringValue("Conditional")
  String conditional();

  @DefaultStringValue("Required")
  String required();

  @DefaultStringValue("Response Type")
  String responseType();

  @DefaultStringValue("Text Prompt for Input")
  String inputPromptPrompt();

  @DefaultStringValue("Name")
  String varName();

  @DefaultStringValue("This account does not have access to the Paco Service")
  String notWhiteListed();

  @DefaultStringValue("There are no events that need responses at this time")
  String noEventsNeedingResponseAtThisTime();

  @DefaultStringValue("Inputs")
  String inputs();

  @DefaultStringValue("Signal Schedule")
  String signalSchedule();
  
  @DefaultStringValue("Signaling")
  String signaling();

  @DefaultStringValue("Already responded")
  String alreadyResponded();

  @DefaultStringValue("Response has expired")
  String expired();

  @DefaultStringValue("Sign in as another user")
  String signInAsOtherUser();

  @DefaultStringValue("Join Experiment")
  String joinExperiment();


}
