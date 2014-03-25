package com.google.sampling.experiential.client;

import com.google.gwt.i18n.client.Constants;

public interface MyConstants extends Constants {

  @DefaultStringValue("Save")
  String save();

  @DefaultStringValue("Cancel")
  String cancel();

  @DefaultStringValue("OK")
  String ok();

  @DefaultStringValue("Success!")
  String success();

  @DefaultStringValue("Failure")
  String failure();

  @DefaultStringValue("Failed to login.")
  String failedToLogin();

  @DefaultStringValue("Failure to join experiment.")
  String failureToJoinExperiment();

  @DefaultStringValue("Something went wrong in saving to the server.")
  String saveToServerFailure();

  @DefaultStringValue("Error message")
  String errorMessage();

  @DefaultStringValue("Error deleting experiment.")
  String errorDeletingExperiment();

  @DefaultStringValue("Could not retrieve charts.")
  String couldNotRetrieveCharts();

  @DefaultStringValue("No results for your query.")
  String noResultsForQuery();


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

  @DefaultStringValue("1) Paco is now available on the Play Store.")
  String downloadAppStep1a();

  @DefaultStringValue("2a) Scan this code with your phone which will launch the Play store and take you to the Paco listing.")
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

  @DefaultStringValue("Are you sure you want to delete this experiment definition? "
            + "Perhaps you want to unpublish it to hide it from new users?")
  String areYouSureYouWantToDelete();

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

  @DefaultStringValue("ID")
  String experimentId();

  @DefaultStringValue("Choose Signaling Mechanism")
  String signalMechanism();

  @DefaultStringValue("Choose Trigger")
  String chooseTrigger();

  @DefaultStringValue("Delay after trigger until notification (in seconds)")
  String chooseTriggerDelay();

  @DefaultStringValue("This is a triggered experiment. No schedule to configure.")
  String triggeredExperimentNotScheduled();



  @DefaultStringValue("")
  String defaultListItem();

  @DefaultStringValue("Source of Paco Action")
  String chooseTriggerSourceIdentifier();

  @DefaultStringValue("Please fix the following before submitting your experiment:")
  String experimentCreationError();

  @DefaultStringValue("Some required fields need to be completed.")
  String needToCompleteRequiredFields();

  @DefaultStringValue("End date cannot be before start date.")
  String startEndDateError();

  @DefaultStringValue("Email address list may be invalid.")
  String emailAddressesError();

  @DefaultStringValue("Input names are required and cannot contain spaces.")
  String varNameUnfilledOrHasSpacesError();

  @DefaultStringValue("Minimum time between signals")
  String minimumBuffer();

  @DefaultStringValue("Custom Rendering")
  String customRendering();

  @DefaultStringValue("Click to edit custom rendering")
  String clickToEditCustomRendering();

  @DefaultStringValue("Click to close custom rendering")
  String clickToCloseCustomRenderingEditor();

  @DefaultStringValue("Enter html and javascript for custom rendering of experiment")
  String customRenderingInstructions();

  @DefaultStringValue("There are no experiments.")
  String noExperimentsReturned();

  @DefaultStringValue("Show Feedback (leave checked unless your custom rendering code handles feedback presentation)")
  String showFeedback();

  @DefaultStringValue("This feature is not currently not compatible with iOS")
  String iOSIncompatible();

}
