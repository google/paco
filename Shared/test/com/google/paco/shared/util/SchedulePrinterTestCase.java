package com.google.paco.shared.util;

import static org.junit.Assert.*;

import org.junit.Test;

import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.JsonConverter;
import com.pacoapp.paco.shared.util.SchedulePrinter;

public class SchedulePrinterTestCase {

  @Test
  public void testPrinterSingleGroup() {
    
    String experimentJson = "{" + 
            "  \"title\": \"test1\"," + 
            "  \"description\": \"desc\"," + 
            "  \"creator\": \"rbe5000@gmail.com\"," + 
            "  \"contactEmail\": \"rbe5000@gmail.com\"," + 
            "  \"id\": 250004," + 
            "  \"recordPhoneDetails\": false," + 
            "  \"extraDataCollectionDeclarations\": []," + 
            "  \"deleted\": false," + 
            "  \"modifyDate\": \"2011/11/24\"," + 
            "  \"published\": false," + 
            "  \"admins\": [" + 
            "    \"rbe5000@gmail.com\"" + 
            "  ]," + 
            "  \"publishedUsers\": []," + 
            "  \"version\": 1," + 
            "  \"groups\": [" + 
            "    {" + 
            "      \"name\": \"default\"," + 
            "      \"customRendering\": false," + 
            "      \"fixedDuration\": false," + 
            "      \"logActions\": false," + 
            "      \"backgroundListen\": false," + 
            "      \"actionTriggers\": [" + 
            "        {" + 
            "          \"type\": \"scheduleTrigger\"," + 
            "          \"actions\": [" + 
            "            {" + 
            "              \"actionCode\": 1," + 
            "              \"id\": 1," + 
            "              \"type\": \"pacoNotificationAction\"," + 
            "              \"snoozeCount\": 0," + 
            "              \"snoozeTime\": 600000," + 
            "              \"timeout\": 59," + 
            "              \"delay\": 5000," + 
            "              \"color\": 0," + 
            "              \"dismissible\": true," + 
            "              \"msgText\": \"Time to participate\"," + 
            "              \"snoozeTimeInMinutes\": 10," + 
            "              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.PacoNotificationAction\"" + 
            "            }" + 
            "          ]," + 
            "          \"id\": 1," + 
            "          \"schedules\": [" + 
            "            {" + 
            "              \"scheduleType\": 0," + 
            "              \"esmFrequency\": 3," + 
            "              \"esmPeriodInDays\": 0," + 
            "              \"esmStartHour\": 32400000," + 
            "              \"esmEndHour\": 61200000," + 
            "              \"signalTimes\": [" + 
            "                {" + 
            "                  \"type\": 0," + 
            "                  \"fixedTimeMillisFromMidnight\": 64800000," + 
            "                  \"basis\": 0," + 
            "                  \"offsetTimeMillis\": 0," + 
            "                  \"missedBasisBehavior\": 1," + 
            "                  \"label\": \"\"," + 
            "                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"" + 
            "                }" + 
            "              ]," + 
            "              \"repeatRate\": 1," + 
            "              \"weekDaysScheduled\": 0," + 
            "              \"nthOfMonth\": 1," + 
            "              \"byDayOfMonth\": true," + 
            "              \"dayOfMonth\": 1," + 
            "              \"esmWeekends\": false," + 
            "              \"minimumBuffer\": 59," + 
            "              \"joinDateMillis\": 0," + 
            "              \"id\": 1," + 
            "              \"onlyEditableOnJoin\": false," + 
            "              \"userEditable\": true," + 
            "              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Schedule\"" + 
            "            }" + 
            "          ]," + 
            "          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ScheduleTrigger\"" + 
            "        }" + 
            "      ]," + 
            "      \"inputs\": [" + 
            "        {" + 
            "          \"name\": \"wgat\"," + 
            "          \"required\": false," + 
            "          \"conditional\": false," + 
            "          \"responseType\": \"likert\"," + 
            "          \"text\": \"blah\"," + 
            "          \"likertSteps\": 5," + 
            "          \"leftSideLabel\": \"Yeah\"," + 
            "          \"rightSideLabel\": \"Naw\"," + 
            "          \"multiselect\": false," + 
            "          \"numeric\": true," + 
            "          \"invisible\": false," + 
            "          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"" + 
            "        }" + 
            "      ]," + 
            "      \"endOfDayGroup\": false," + 
            "      \"feedback\": {" + 
            "        \"text\": \"Thanks for Participating!\"," + 
            "        \"type\": 1," + 
            "        \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Feedback\"" + 
            "      }," + 
            "      \"feedbackType\": 1," + 
            "      \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentGroup\"" + 
            "    }" + 
            "  ]," + 
            "  \"postInstallInstructions\": \"<b>You have successfully joined the experiment!</b><br/><br/>No need to do anything else for now.<br/><br/>Paco will send you a notification when it is time to" +  "participate.<br/><br/>Be sure your ringer/buzzer is on so you will hear the notification.\"," + 
            "  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentDAO\"" + 
            "}";
    
    ExperimentDAO experiment = JsonConverter.fromSingleEntityJson(experimentJson);
    assertEquals("default:[1:(1:Daily at 06:00PM)]", SchedulePrinter.createStringOfAllSchedules(experiment));
  }
  
  @Test
  public void testPrinterSingleGroupTwoTimes() {
    
    String experimentJson = "{" + 
            "  \"title\": \"test1\"," + 
            "  \"description\": \"desc\"," + 
            "  \"creator\": \"rbe5000@gmail.com\"," + 
            "  \"contactEmail\": \"rbe5000@gmail.com\"," + 
            "  \"id\": 250004," + 
            "  \"recordPhoneDetails\": false," + 
            "  \"extraDataCollectionDeclarations\": []," + 
            "  \"deleted\": false," + 
            "  \"modifyDate\": \"2011/11/24\"," + 
            "  \"published\": false," + 
            "  \"admins\": [" + 
            "    \"rbe5000@gmail.com\"" + 
            "  ]," + 
            "  \"publishedUsers\": []," + 
            "  \"version\": 1," + 
            "  \"groups\": [" + 
            "    {" + 
            "      \"name\": \"default\"," + 
            "      \"customRendering\": false," + 
            "      \"fixedDuration\": false," + 
            "      \"logActions\": false," + 
            "      \"backgroundListen\": false," + 
            "      \"actionTriggers\": [" + 
            "        {" + 
            "          \"type\": \"scheduleTrigger\"," + 
            "          \"actions\": [" + 
            "            {" + 
            "              \"actionCode\": 1," + 
            "              \"id\": 1," + 
            "              \"type\": \"pacoNotificationAction\"," + 
            "              \"snoozeCount\": 0," + 
            "              \"snoozeTime\": 600000," + 
            "              \"timeout\": 59," + 
            "              \"delay\": 5000," + 
            "              \"color\": 0," + 
            "              \"dismissible\": true," + 
            "              \"msgText\": \"Time to participate\"," + 
            "              \"snoozeTimeInMinutes\": 10," + 
            "              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.PacoNotificationAction\"" + 
            "            }" + 
            "          ]," + 
            "          \"id\": 1," + 
            "          \"schedules\": [" + 
            "            {" + 
            "              \"scheduleType\": 0," + 
            "              \"esmFrequency\": 3," + 
            "              \"esmPeriodInDays\": 0," + 
            "              \"esmStartHour\": 32400000," + 
            "              \"esmEndHour\": 61200000," + 
            "              \"signalTimes\": [" + 
            "                {" + 
            "                  \"type\": 0," + 
            "                  \"fixedTimeMillisFromMidnight\": 64800000," + 
            "                  \"basis\": 0," + 
            "                  \"offsetTimeMillis\": 0," + 
            "                  \"missedBasisBehavior\": 1," + 
            "                  \"label\": \"\"," + 
            "                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"" + 
            "                }," +
            "                {" + 
            "                  \"type\": 0," + 
            "                  \"fixedTimeMillisFromMidnight\": 68400000," + 
            "                  \"basis\": 0," + 
            "                  \"offsetTimeMillis\": 0," + 
            "                  \"missedBasisBehavior\": 1," + 
            "                  \"label\": \"\"," + 
            "                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"" + 
            "                }" + 

            
            "              ]," + 
            "              \"repeatRate\": 1," + 
            "              \"weekDaysScheduled\": 0," + 
            "              \"nthOfMonth\": 1," + 
            "              \"byDayOfMonth\": true," + 
            "              \"dayOfMonth\": 1," + 
            "              \"esmWeekends\": false," + 
            "              \"minimumBuffer\": 59," + 
            "              \"joinDateMillis\": 0," + 
            "              \"id\": 1," + 
            "              \"onlyEditableOnJoin\": false," + 
            "              \"userEditable\": true," + 
            "              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Schedule\"" + 
            "            }" + 
            "          ]," + 
            "          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ScheduleTrigger\"" + 
            "        }" + 
            "      ]," + 
            "      \"inputs\": [" + 
            "        {" + 
            "          \"name\": \"wgat\"," + 
            "          \"required\": false," + 
            "          \"conditional\": false," + 
            "          \"responseType\": \"likert\"," + 
            "          \"text\": \"blah\"," + 
            "          \"likertSteps\": 5," + 
            "          \"leftSideLabel\": \"Yeah\"," + 
            "          \"rightSideLabel\": \"Naw\"," + 
            "          \"multiselect\": false," + 
            "          \"numeric\": true," + 
            "          \"invisible\": false," + 
            "          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"" + 
            "        }" + 
            "      ]," + 
            "      \"endOfDayGroup\": false," + 
            "      \"feedback\": {" + 
            "        \"text\": \"Thanks for Participating!\"," + 
            "        \"type\": 1," + 
            "        \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Feedback\"" + 
            "      }," + 
            "      \"feedbackType\": 1," + 
            "      \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentGroup\"" + 
            "    }" + 
            "  ]," + 
            "  \"postInstallInstructions\": \"<b>You have successfully joined the experiment!</b><br/><br/>No need to do anything else for now.<br/><br/>Paco will send you a notification when it is time to" +  "participate.<br/><br/>Be sure your ringer/buzzer is on so you will hear the notification.\"," + 
            "  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentDAO\"" + 
            "}";
    
    ExperimentDAO experiment = JsonConverter.fromSingleEntityJson(experimentJson);
    assertEquals("default:[1:(1:Daily at 06:00PM,07:00PM)]", SchedulePrinter.createStringOfAllSchedules(experiment));
  }

  @Test
  public void testPrinterSingleGroupTwoSchedules() {
    
    String experimentJson = "{" + 
            "  \"title\": \"test1\"," + 
            "  \"description\": \"desc\"," + 
            "  \"creator\": \"rbe5000@gmail.com\"," + 
            "  \"contactEmail\": \"rbe5000@gmail.com\"," + 
            "  \"id\": 250004," + 
            "  \"recordPhoneDetails\": false," + 
            "  \"extraDataCollectionDeclarations\": []," + 
            "  \"deleted\": false," + 
            "  \"modifyDate\": \"2011/11/24\"," + 
            "  \"published\": false," + 
            "  \"admins\": [" + 
            "    \"rbe5000@gmail.com\"" + 
            "  ]," + 
            "  \"publishedUsers\": []," + 
            "  \"version\": 1," + 
            "  \"groups\": [" + 
            "    {" + 
            "      \"name\": \"default\"," + 
            "      \"customRendering\": false," + 
            "      \"fixedDuration\": false," + 
            "      \"logActions\": false," + 
            "      \"backgroundListen\": false," + 
            "      \"actionTriggers\": [" + 
            "        {" + 
            "          \"type\": \"scheduleTrigger\"," + 
            "          \"actions\": [" + 
            "            {" + 
            "              \"actionCode\": 1," + 
            "              \"id\": 1," + 
            "              \"type\": \"pacoNotificationAction\"," + 
            "              \"snoozeCount\": 0," + 
            "              \"snoozeTime\": 600000," + 
            "              \"timeout\": 59," + 
            "              \"delay\": 5000," + 
            "              \"color\": 0," + 
            "              \"dismissible\": true," + 
            "              \"msgText\": \"Time to participate\"," + 
            "              \"snoozeTimeInMinutes\": 10," + 
            "              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.PacoNotificationAction\"" + 
            "            }" + 
            "          ]," + 
            "          \"id\": 1," + 
            "          \"schedules\": [" + 
            "            {" + 
            "              \"scheduleType\": 0," + 
            "              \"esmFrequency\": 3," + 
            "              \"esmPeriodInDays\": 0," + 
            "              \"esmStartHour\": 32400000," + 
            "              \"esmEndHour\": 61200000," + 
            "              \"signalTimes\": [" + 
            "                {" + 
            "                  \"type\": 0," + 
            "                  \"fixedTimeMillisFromMidnight\": 64800000," + 
            "                  \"basis\": 0," + 
            "                  \"offsetTimeMillis\": 0," + 
            "                  \"missedBasisBehavior\": 1," + 
            "                  \"label\": \"\"," + 
            "                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"" + 
            "                }" +
            "              ]," + 
            "              \"repeatRate\": 1," + 
            "              \"weekDaysScheduled\": 0," + 
            "              \"nthOfMonth\": 1," + 
            "              \"byDayOfMonth\": true," + 
            "              \"dayOfMonth\": 1," + 
            "              \"esmWeekends\": false," + 
            "              \"minimumBuffer\": 59," + 
            "              \"joinDateMillis\": 0," + 
            "              \"id\": 1," + 
            "              \"onlyEditableOnJoin\": false," + 
            "              \"userEditable\": true," + 
            "              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Schedule\"" + 
            "            }" + 
            "          ]," + 
            "          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ScheduleTrigger\"" + 
            "        }" +
            
            "," +
            
            "        {" + 
            "          \"type\": \"scheduleTrigger\"," + 
            "          \"actions\": [" + 
            "            {" + 
            "              \"actionCode\": 1," + 
            "              \"id\": 2," + 
            "              \"type\": \"pacoNotificationAction\"," + 
            "              \"snoozeCount\": 0," + 
            "              \"snoozeTime\": 600000," + 
            "              \"timeout\": 59," + 
            "              \"delay\": 5000," + 
            "              \"color\": 0," + 
            "              \"dismissible\": true," + 
            "              \"msgText\": \"Time to participate\"," + 
            "              \"snoozeTimeInMinutes\": 10," + 
            "              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.PacoNotificationAction\"" + 
            "            }" + 
            "          ]," + 
            "          \"id\": 2," + 
            "          \"schedules\": [" + 
            "            {" + 
            "              \"scheduleType\": 0," + 
            "              \"esmFrequency\": 3," + 
            "              \"esmPeriodInDays\": 0," + 
            "              \"esmStartHour\": 32400000," + 
            "              \"esmEndHour\": 61200000," + 
            "              \"signalTimes\": [" + 
            "                {" + 
            "                  \"type\": 0," + 
            "                  \"fixedTimeMillisFromMidnight\": 68400000," + 
            "                  \"basis\": 0," + 
            "                  \"offsetTimeMillis\": 0," + 
            "                  \"missedBasisBehavior\": 1," + 
            "                  \"label\": \"\"," + 
            "                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"" + 
            "                }" +
            "              ]," + 
            "              \"repeatRate\": 1," + 
            "              \"weekDaysScheduled\": 0," + 
            "              \"nthOfMonth\": 1," + 
            "              \"byDayOfMonth\": true," + 
            "              \"dayOfMonth\": 1," + 
            "              \"esmWeekends\": false," + 
            "              \"minimumBuffer\": 59," + 
            "              \"joinDateMillis\": 0," + 
            "              \"id\": 3," + 
            "              \"onlyEditableOnJoin\": false," + 
            "              \"userEditable\": true," + 
            "              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Schedule\"" + 
            "            }" + 
            "          ]," + 
            "          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ScheduleTrigger\"" + 
            "        }" +
            
            
            "      ]," + 
            "      \"inputs\": [" + 
            "        {" + 
            "          \"name\": \"wgat\"," + 
            "          \"required\": false," + 
            "          \"conditional\": false," + 
            "          \"responseType\": \"likert\"," + 
            "          \"text\": \"blah\"," + 
            "          \"likertSteps\": 5," + 
            "          \"leftSideLabel\": \"Yeah\"," + 
            "          \"rightSideLabel\": \"Naw\"," + 
            "          \"multiselect\": false," + 
            "          \"numeric\": true," + 
            "          \"invisible\": false," + 
            "          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"" + 
            "        }" + 
            "      ]," + 
            "      \"endOfDayGroup\": false," + 
            "      \"feedback\": {" + 
            "        \"text\": \"Thanks for Participating!\"," + 
            "        \"type\": 1," + 
            "        \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Feedback\"" + 
            "      }," + 
            "      \"feedbackType\": 1," + 
            "      \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentGroup\"" + 
            "    }" + 
            "  ]," + 
            "  \"postInstallInstructions\": \"<b>You have successfully joined the experiment!</b><br/><br/>No need to do anything else for now.<br/><br/>Paco will send you a notification when it is time to" +  "participate.<br/><br/>Be sure your ringer/buzzer is on so you will hear the notification.\"," + 
            "  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentDAO\"" + 
            "}";
    
    ExperimentDAO experiment = JsonConverter.fromSingleEntityJson(experimentJson);
    assertEquals("default:[1:(1:Daily at 06:00PM) | 2:(3:Daily at 07:00PM)]", SchedulePrinter.createStringOfAllSchedules(experiment));
  }

}
