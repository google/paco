package com.google.sampling.experiential.server.viz.appusage;

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.type.TypeReference;

import com.google.common.collect.Lists;
import com.google.sampling.experiential.shared.EventDAO;
import com.pacoapp.paco.shared.model2.JsonConverter;

public class PhoneSessionTestConstants {

  public static List<EventDAO> getEvents() {
    try {
      return JsonConverter.getObjectMapper().readValue(eventsJson, new TypeReference<List<EventDAO>>() {});
    } catch (JsonParseException e) {
      e.printStackTrace();
    } catch (JsonMappingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static String eventsJson  ="["
          + "  { "
          + "    \"id\": 4706459522695168, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1497890362000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1497915562000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Paco\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5410146964471808, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1497890362000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1497915562000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"userPresent\", "
          + "        \"answer\": \"2017-06-19T16:39:22.169-07:00\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 4987934499405824, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1497890364000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1497915564000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Paco\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.ExperimentGroupPicker\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 4636090778517504, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1497890366000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1497915566000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Paco\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.ExperimentExecutorCustomRendering\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 4917565755228160, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1497890556000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1497915756000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Paco\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 4776828266872832, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1497890559000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1497915759000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Google App\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5058303243583488, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1497890561000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1497915761000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Paco\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 4508547429695488, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1497890563000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1497915763000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Paco\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.SettingsActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 4790022406406144, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1497890566000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1497915766000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Paco\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 4649284918050816, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1497890569000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1497915769000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Paco\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.TroubleshootingActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 4930759894761472, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1497890621000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1497915821000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Paco\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 4578916173873152, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1497890622000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1497915822000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Google App\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 4860391150583808, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1497890623000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1497915823000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Paco\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 4719653662228480, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1497890624000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1497915824000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Paco\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.ExperimentGroupPicker\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5001128638939136, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1497890626000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1497915826000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Paco\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.ExperimentExecutorCustomRendering\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 6232581662048256, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066011000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091211000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"userPresent\", "
          + "        \"answer\": \"2017-06-21T17:26:51.751-07:00\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5951106685337600, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066014000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091214000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Music\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.android.music/com.android.music.MusicBrowserActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5247419243560960, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066015000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091215000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Music\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.android.music/com.android.music.ArtistAlbumBrowserActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5528894220271616, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066026000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091226000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Google App\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5177050499383296, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066031000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091231000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Maps\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.google.android.apps.maps/com.google.android.maps.MapsActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5458525476093952, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066039000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091239000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Google App\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5317787987738624, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066043000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091243000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Camera\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.android.camera/com.android.camera.Camera\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5599262964449280, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066050000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091250000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Google App\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5089089569161216, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066054000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091254000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Clock\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.android.deskclock/com.android.deskclock.DeskClock\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5370564545871872, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066056000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091256000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Google App\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5229827057516544, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066061000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091261000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Camera\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.android.gallery/com.android.camera.GalleryPicker\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5511302034227200, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066064000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091264000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Camera\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.android.gallery/com.android.camera.ImageGallery\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5159458313338880, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066066000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091266000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Camera\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.android.gallery/com.android.camera.ViewImage\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5440933290049536, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066074000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091274000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Camera\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.android.gallery/com.android.camera.ImageGallery\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5300195801694208, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066076000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091276000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Camera\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.android.gallery/com.android.camera.GalleryPicker\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5581670778404864, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066078000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091278000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Google App\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5124273941250048, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066081000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091281000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Google App\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.googlequicksearchbox.SearchActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5405748917960704, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066082000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091282000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Google App\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5265011429605376, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066089000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091289000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Google App\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.sidekick.main.optin.NewOptInActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5194642685427712, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066102000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091302000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Google App\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5546486406316032, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066102000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091302000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Google App\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.googlequicksearchbox.SearchActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5476117662138368, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066121000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091321000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Paco\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5335380173783040, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066124000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091324000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Paco\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.ExperimentGroupPicker\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5616855150493696, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066126000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091326000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Paco\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.ExperimentExecutorCustomRendering\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5080293476139008, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066131000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091331000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Paco\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5361768452849664, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066134000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091334000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Paco\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.ExperimentExecutor\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5502505941204992, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066136000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091336000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Paco\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.FeedbackActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5150662220316672, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066138000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091338000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Paco\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5432137197027328, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066140000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091340000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Google App\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5291399708672000, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066143000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091343000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Paco\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5572874685382656, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066146000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091346000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Paco\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.TroubleshootingActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5115477848227840, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066280000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091480000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Paco\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5396952824938496, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066287000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091487000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"userNotPresent\", "
          + "        \"answer\": \"2017-06-21T17:31:27.176-07:00\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 4693265383161856, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066299000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091499000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"userPresent\", "
          + "        \"answer\": \"2017-06-21T17:31:39.696-07:00\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5256215336583168, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066299000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091499000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Paco\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5537690313293824, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066307000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091507000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Google App\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5185846592405504, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066309000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091509000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Music\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.android.music/com.android.music.ArtistAlbumBrowserActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5467321569116160, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066312000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091512000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Music\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.android.music/com.android.music.AlbumBrowserActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5326584080760832, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066313000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091513000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Music\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.android.music/com.android.music.TrackBrowserActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5608059057471488, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066315000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091515000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Music\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.android.music/com.android.music.PlaylistBrowserActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5097885662183424, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066318000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091518000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Google App\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5379360638894080, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066321000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091521000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Maps\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.google.android.apps.maps/com.google.android.maps.MapsActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5238623150538752, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066366000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091566000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Google App\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 4605304452939776, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066380000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091580000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"userPresent\", "
          + "        \"answer\": \"2017-06-21T17:33:00.075-07:00\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5520098127249408, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066380000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091580000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"userNotPresent\", "
          + "        \"answer\": \"2017-06-21T17:33:00.016-07:00\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5168254406361088, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066381000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091581000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Google App\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5449729383071744, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066382000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091582000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Paco\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5308991894716416, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066385000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091585000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Paco\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.TroubleshootingActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5590466871427072, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498066400000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498091600000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Google App\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5133070034272256, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498068719000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498093919000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Paco\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.TroubleshootingActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5414545010982912, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498068723000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498093923000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Paco\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5273807522627584, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498068730000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498093930000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Paco\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.ExperimentExecutor\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5555282499338240, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498068732000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498093932000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Camera\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.android.camera/com.android.camera.Camera\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5203438778449920, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498068736000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498093936000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Paco\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.ExperimentExecutor\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 6470076173647872, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498068737000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498093937000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Paco\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.FeedbackActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 6751551150358528, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498068739000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498093939000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Paco\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 6195198266703872, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498068741000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498093941000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Google App\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 6476673243414528, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498068868000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498094068000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"userNotPresent\", "
          + "        \"answer\": \"2017-06-21T18:14:28.904-07:00\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5772985801637888, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498068878000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498094078000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"userPresent\", "
          + "        \"answer\": \"2017-06-21T18:14:38.893-07:00\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 6335935755059200, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498068878000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498094078000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Google App\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 6617410731769856, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498068880000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498094080000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Paco\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 6265567010881536, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498068884000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498094084000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Paco\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.TroubleshootingActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 6547041987592192, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498068896000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498094096000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Google App\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 6406304499236864, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498069372000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498094572000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"userNotPresent\", "
          + "        \"answer\": \"2017-06-21T18:22:52.960-07:00\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 6124829522526208, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498069383000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498094583000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"userPresent\", "
          + "        \"answer\": \"2017-06-21T18:23:03.547-07:00\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 6687779475947520, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498069383000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498094583000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Google App\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 6230382638792704, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498069392000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498094592000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Paco\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.TroubleshootingActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 6511857615503360, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498069404000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498094604000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Google App\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 6371120127148032, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498069407000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498094607000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Settings\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.android.settings/com.android.settings.Settings\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 6652595103858688, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498069412000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498094612000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Google App\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 6300751382970368, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498069414000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498094614000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Camera\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.android.camera/com.android.camera.Camera\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 6582226359681024, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498069421000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498094621000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Google App\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 6441488871325696, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498069423000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498094623000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Music\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.android.music/com.android.music.PlaylistBrowserActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 6722963848036352, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498069426000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498094626000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Google App\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 6212790452748288, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498069428000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498094628000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"userNotPresent\", "
          + "        \"answer\": \"2017-06-21T18:23:48.935-07:00\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 5931315476037632, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498069439000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498094639000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"userPresent\", "
          + "        \"answer\": \"2017-06-21T18:23:59.255-07:00\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 6494265429458944, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498069439000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498094639000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Google App\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 6353527941103616, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498069440000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498094640000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Paco\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.TroubleshootingActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  } "
          + "] ";


  public static String oneSession = "[ "
          + "  { "
          + "    \"id\": 6124829522526208, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498069383000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498094583000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"userPresent\", "
          + "        \"answer\": \"2017-06-21T18:23:03.547-07:00\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 6687779475947520, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498069383000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498094583000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Google App\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 6230382638792704, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498069392000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498094592000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Paco\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.TroubleshootingActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 6511857615503360, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498069404000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498094604000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Google App\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 6371120127148032, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498069407000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498094607000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Settings\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.android.settings/com.android.settings.Settings\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 6652595103858688, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498069412000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498094612000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Google App\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 6300751382970368, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498069414000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498094614000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Camera\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.android.camera/com.android.camera.Camera\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 6582226359681024, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498069421000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498094621000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Google App\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 6441488871325696, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498069423000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498094623000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Music\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.android.music/com.android.music.PlaylistBrowserActivity\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 6722963848036352, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498069426000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"experimentGroupName\": \"app_logging\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498094626000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"apps_used\", "
          + "        \"answer\": \"Google App\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"apps_used_raw\", "
          + "        \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\" "
          + "      }, "
          + "      { "
          + "        \"name\": \"foreground\", "
          + "        \"answer\": \"true\" "
          + "      } "
          + "    ] "
          + "  }, "
          + "  { "
          + "    \"id\": 6212790452748288, "
          + "    \"experimentId\": 6582776115494912, "
          + "    \"who\": \"testuser@example.com\", "
          + "    \"appId\": \"Android\", "
          + "    \"pacoVersion\": \"4.2.24\", "
          + "    \"experimentName\": \"Sql Viz Demo\", "
          + "    \"responseTime\": 1498069428000, "
          + "    \"experimentVersion\": 44, "
          + "    \"timezone\": \"-07:00\", "
          + "    \"actionTriggerId\": 0, "
          + "    \"actionTriggerSpecId\": 0, "
          + "    \"actionId\": 0, "
          + "    \"joined\": false, "
          + "    \"sortDate\": 1498094628000, "
          + "    \"missedSignal\": false, "
          + "    \"emptyResponse\": false, "
          + "    \"responses\": [ "
          + "      { "
          + "        \"name\": \"userNotPresent\", "
          + "        \"answer\": \"2017-06-21T18:23:48.935-07:00\" "
          + "      } "
          + "    ] "
          + "  } "
              + "] ";


  static String brokenSession = "["+
          "    {"+
          "      \"experimentId\": 5755724958269440,"+
          "      \"who\": \"rbe10001@gmail.com\","+
          "      \"when\": 1500078773304,"+
          "      \"appId\": \"Android\","+
          "      \"pacoVersion\": \"4.2.26\","+
          "      \"experimentName\": \"app logger\","+
          "      \"responseTime\": 1500078768000,"+
          "      \"experimentVersion\": 4,"+
          "      \"timezone\": \"-07:00\","+
          "      \"experimentGroupName\": \"New Group\","+
          "      \"joined\": false,"+
          "      \"emptyResponse\": false,"+
          "      \"missedSignal\": false,"+
          "      \"responses\": ["+
          "        {"+
          "          \"name\": \"apps_used_raw\","+
          "          \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.TroubleshootingActivity\""+
          "        },"+
          "        {"+
          "          \"name\": \"foreground\","+
          "          \"answer\": \"true\""+
          "        },"+
          "        {"+
          "          \"name\": \"apps_used\","+
          "          \"answer\": \"Paco\""+
          "        }"+
          "      ]"+
          "    },"+
          "    {"+
          "      \"experimentId\": 5755724958269440,"+
          "      \"who\": \"rbe10001@gmail.com\","+
          "      \"when\": 1500078772978,"+
          "      \"appId\": \"Android\","+
          "      \"pacoVersion\": \"4.2.26\","+
          "      \"experimentName\": \"app logger\","+
          "      \"responseTime\": 1500078767000,"+
          "      \"experimentVersion\": 4,"+
          "      \"timezone\": \"-07:00\","+
          "      \"experimentGroupName\": \"New Group\","+
          "      \"joined\": false,"+
          "      \"emptyResponse\": false,"+
          "      \"missedSignal\": false,"+
          "      \"responses\": ["+
          "        {"+
          "          \"name\": \"foreground\","+
          "          \"answer\": \"true\""+
          "        },"+
          "        {"+
          "          \"name\": \"apps_used_raw\","+
          "          \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\""+
          "        },"+
          "        {"+
          "          \"name\": \"apps_used\","+
          "          \"answer\": \"Google\""+
          "        }"+
          "      ]"+
          "    },"+
          "    {"+
          "      \"experimentId\": 5755724958269440,"+
          "      \"who\": \"rbe10001@gmail.com\","+
          "      \"when\": 1500078772543,"+
          "      \"appId\": \"Android\","+
          "      \"pacoVersion\": \"4.2.26\","+
          "      \"experimentName\": \"app logger\","+
          "      \"responseTime\": 1500078764000,"+
          "      \"experimentVersion\": 4,"+
          "      \"timezone\": \"-07:00\","+
          "      \"experimentGroupName\": \"New Group\","+
          "      \"joined\": false,"+
          "      \"emptyResponse\": false,"+
          "      \"missedSignal\": false,"+
          "      \"responses\": ["+
          "        {"+
          "          \"name\": \"userPresent\","+
          "          \"answer\": \"2017-07-14T17:32:44.746-07:00\""+
          "        }"+
          "      ]"+
          "    },"+
          "    {"+
          "      \"experimentId\": 5755724958269440,"+
          "      \"who\": \"rbe10001@gmail.com\","+
          "      \"when\": 1500078772149,"+
          "      \"appId\": \"Android\","+
          "      \"pacoVersion\": \"4.2.26\","+
          "      \"experimentName\": \"app logger\","+
          "      \"responseTime\": 1500078736000,"+
          "      \"experimentVersion\": 4,"+
          "      \"timezone\": \"-07:00\","+
          "      \"experimentGroupName\": \"New Group\","+
          "      \"joined\": false,"+
          "      \"emptyResponse\": false,"+
          "      \"missedSignal\": false,"+
          "      \"responses\": ["+
          "        {"+
          "          \"name\": \"userNotPresent\","+
          "          \"answer\": \"2017-07-14T17:32:16.676-07:00\""+
          "        }"+
          "      ]"+
          "    },"+
          "    {"+
          "      \"experimentId\": 5755724958269440,"+
          "      \"who\": \"rbe10001@gmail.com\","+
          "      \"when\": 1500078771786,"+
          "      \"appId\": \"Android\","+
          "      \"pacoVersion\": \"4.2.26\","+
          "      \"experimentName\": \"app logger\","+
          "      \"responseTime\": 1500078730000,"+
          "      \"experimentVersion\": 4,"+
          "      \"timezone\": \"-07:00\","+
          "      \"experimentGroupName\": \"New Group\","+
          "      \"joined\": false,"+
          "      \"emptyResponse\": false,"+
          "      \"missedSignal\": false,"+
          "      \"responses\": ["+
          "        {"+
          "          \"name\": \"apps_used\","+
          "          \"answer\": \"Google Play Music\""+
          "        },"+
          "        {"+
          "          \"name\": \"foreground\","+
          "          \"answer\": \"true\""+
          "        },"+
          "        {"+
          "          \"name\": \"apps_used_raw\","+
          "          \"answer\": \"com.google.android.music/com.google.android.music.ui.mylibrary.ArtistPageActivity\""+
          "        }"+
          "      ]"+
          "    },"+
          "    {"+
          "      \"experimentId\": 5755724958269440,"+
          "      \"who\": \"rbe10001@gmail.com\","+
          "      \"when\": 1500078771258,"+
          "      \"appId\": \"Android\","+
          "      \"pacoVersion\": \"4.2.26\","+
          "      \"experimentName\": \"app logger\","+
          "      \"responseTime\": 1500078724000,"+
          "      \"experimentVersion\": 4,"+
          "      \"timezone\": \"-07:00\","+
          "      \"experimentGroupName\": \"New Group\","+
          "      \"joined\": false,"+
          "      \"emptyResponse\": false,"+
          "      \"missedSignal\": false,"+
          "      \"responses\": ["+
          "        {"+
          "          \"name\": \"apps_used_raw\","+
          "          \"answer\": \"com.google.android.music/com.google.android.music.ui.HomeActivity\""+
          "        },"+
          "        {"+
          "          \"name\": \"apps_used\","+
          "          \"answer\": \"Google Play Music\""+
          "        },"+
          "        {"+
          "          \"name\": \"foreground\","+
          "          \"answer\": \"true\""+
          "        }"+
          "      ]"+
          "    },"+
          "    {"+
          "      \"experimentId\": 5755724958269440,"+
          "      \"who\": \"rbe10001@gmail.com\","+
          "      \"when\": 1500078770823,"+
          "      \"appId\": \"Android\","+
          "      \"pacoVersion\": \"4.2.26\","+
          "      \"experimentName\": \"app logger\","+
          "      \"responseTime\": 1500078723000,"+
          "      \"experimentVersion\": 4,"+
          "      \"timezone\": \"-07:00\","+
          "      \"experimentGroupName\": \"New Group\","+
          "      \"joined\": false,"+
          "      \"emptyResponse\": false,"+
          "      \"missedSignal\": false,"+
          "      \"responses\": ["+
          "        {"+
          "          \"name\": \"userPresent\","+
          "          \"answer\": \"2017-07-14T17:32:03.255-07:00\""+
          "        }"+
          "      ]"+
          "    },"+
          "    {"+
          "      \"experimentId\": 5755724958269440,"+
          "      \"who\": \"rbe10001@gmail.com\","+
          "      \"when\": 1500078770483,"+
          "      \"appId\": \"Android\","+
          "      \"pacoVersion\": \"4.2.26\","+
          "      \"experimentName\": \"app logger\","+
          "      \"responseTime\": 1500078714000,"+
          "      \"experimentVersion\": 4,"+
          "      \"timezone\": \"-07:00\","+
          "      \"experimentGroupName\": \"New Group\","+
          "      \"joined\": false,"+
          "      \"emptyResponse\": false,"+
          "      \"missedSignal\": false,"+
          "      \"responses\": ["+
          "        {"+
          "          \"name\": \"userNotPresent\","+
          "          \"answer\": \"2017-07-14T17:31:54.303-07:00\""+
          "        }"+
          "      ]"+
          "    },"+
          "    {"+
          "      \"experimentId\": 5755724958269440,"+
          "      \"who\": \"rbe10001@gmail.com\","+
          "      \"when\": 1500078770077,"+
          "      \"appId\": \"Android\","+
          "      \"pacoVersion\": \"4.2.26\","+
          "      \"experimentName\": \"app logger\","+
          "      \"responseTime\": 1500078708000,"+
          "      \"experimentVersion\": 4,"+
          "      \"timezone\": \"-07:00\","+
          "      \"experimentGroupName\": \"New Group\","+
          "      \"joined\": false,"+
          "      \"emptyResponse\": false,"+
          "      \"missedSignal\": false,"+
          "      \"responses\": ["+
          "        {"+
          "          \"name\": \"apps_used_raw\","+
          "          \"answer\": \"com.google.android.music/com.google.android.music.ui.HomeActivity\""+
          "        },"+
          "        {"+
          "          \"name\": \"apps_used\","+
          "          \"answer\": \"Google Play Music\""+
          "        },"+
          "        {"+
          "          \"name\": \"foreground\","+
          "          \"answer\": \"true\""+
          "        }"+
          "      ]"+
          "    },"+
          "    {"+
          "      \"experimentId\": 5755724958269440,"+
          "      \"who\": \"rbe10001@gmail.com\","+
          "      \"when\": 1500078769704,"+
          "      \"appId\": \"Android\","+
          "      \"pacoVersion\": \"4.2.26\","+
          "      \"experimentName\": \"app logger\","+
          "      \"responseTime\": 1500078701000,"+
          "      \"experimentVersion\": 4,"+
          "      \"timezone\": \"-07:00\","+
          "      \"experimentGroupName\": \"New Group\","+
          "      \"joined\": false,"+
          "      \"emptyResponse\": false,"+
          "      \"missedSignal\": false,"+
          "      \"responses\": ["+
          "        {"+
          "          \"name\": \"apps_used_raw\","+
          "          \"answer\": \"com.google.android.music/com.google.android.music.ui.TrackContainerActivity\""+
          "        },"+
          "        {"+
          "          \"name\": \"foreground\","+
          "          \"answer\": \"true\""+
          "        },"+
          "        {"+
          "          \"name\": \"apps_used\","+
          "          \"answer\": \"Google Play Music\""+
          "        }"+
          "      ]"+
          "    }" +
//          "," +
//          "    {"+
//          "      \"experimentId\": 5755724958269440,"+
//          "      \"who\": \"rbe10001@gmail.com\","+
//          "      \"when\": 1500078769335,"+
//          "      \"appId\": \"Android\","+
//          "      \"pacoVersion\": \"4.2.26\","+
//          "      \"experimentName\": \"app logger\","+
//          "      \"responseTime\": 1500078692000,"+
//          "      \"experimentVersion\": 4,"+
//          "      \"timezone\": \"-07:00\","+
//          "      \"experimentGroupName\": \"New Group\","+
//          "      \"joined\": false,"+
//          "      \"emptyResponse\": false,"+
//          "      \"missedSignal\": false,"+
//          "      \"responses\": ["+
//          "        {"+
//          "          \"name\": \"apps_used_raw\","+
//          "          \"answer\": \"com.google.android.music/com.google.android.music.ui.HomeActivity\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"apps_used\","+
//          "          \"answer\": \"Google Play Music\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"foreground\","+
//          "          \"answer\": \"true\""+
//          "        }"+
//          "      ]"+
//          "    },"+
//          "    {"+
//          "      \"experimentId\": 5755724958269440,"+
//          "      \"who\": \"rbe10001@gmail.com\","+
//          "      \"when\": 1500078769014,"+
//          "      \"appId\": \"Android\","+
//          "      \"pacoVersion\": \"4.2.26\","+
//          "      \"experimentName\": \"app logger\","+
//          "      \"responseTime\": 1500078689000,"+
//          "      \"experimentVersion\": 4,"+
//          "      \"timezone\": \"-07:00\","+
//          "      \"experimentGroupName\": \"New Group\","+
//          "      \"joined\": false,"+
//          "      \"emptyResponse\": false,"+
//          "      \"missedSignal\": false,"+
//          "      \"responses\": ["+
//          "        {"+
//          "          \"name\": \"foreground\","+
//          "          \"answer\": \"true\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"apps_used\","+
//          "          \"answer\": \"Google\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"apps_used_raw\","+
//          "          \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\""+
//          "        }"+
//          "      ]"+
//          "    },"+
//          "    {"+
//          "      \"experimentId\": 5755724958269440,"+
//          "      \"who\": \"rbe10001@gmail.com\","+
//          "      \"when\": 1500078687465,"+
//          "      \"appId\": \"Android\","+
//          "      \"pacoVersion\": \"4.2.26\","+
//          "      \"experimentName\": \"app logger\","+
//          "      \"responseTime\": 1500078681000,"+
//          "      \"experimentVersion\": 4,"+
//          "      \"timezone\": \"-07:00\","+
//          "      \"experimentGroupName\": \"New Group\","+
//          "      \"joined\": false,"+
//          "      \"emptyResponse\": false,"+
//          "      \"missedSignal\": false,"+
//          "      \"responses\": ["+
//          "        {"+
//          "          \"name\": \"apps_used\","+
//          "          \"answer\": \"Paco\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"apps_used_raw\","+
//          "          \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.TroubleshootingActivity\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"foreground\","+
//          "          \"answer\": \"true\""+
//          "        }"+
//          "      ]"+
//          "    },"+
//          "    {"+
//          "      \"experimentId\": 5755724958269440,"+
//          "      \"who\": \"rbe10001@gmail.com\","+
//          "      \"when\": 1500078687092,"+
//          "      \"appId\": \"Android\","+
//          "      \"pacoVersion\": \"4.2.26\","+
//          "      \"experimentName\": \"app logger\","+
//          "      \"responseTime\": 1500078680000,"+
//          "      \"experimentVersion\": 4,"+
//          "      \"timezone\": \"-07:00\","+
//          "      \"experimentGroupName\": \"New Group\","+
//          "      \"joined\": false,"+
//          "      \"emptyResponse\": false,"+
//          "      \"missedSignal\": false,"+
//          "      \"responses\": ["+
//          "        {"+
//          "          \"name\": \"apps_used_raw\","+
//          "          \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"apps_used\","+
//          "          \"answer\": \"Google\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"foreground\","+
//          "          \"answer\": \"true\""+
//          "        }"+
//          "      ]"+
//          "    },"+
//          "    {"+
//          "      \"experimentId\": 5755724958269440,"+
//          "      \"who\": \"rbe10001@gmail.com\","+
//          "      \"when\": 1500078686687,"+
//          "      \"appId\": \"Android\","+
//          "      \"pacoVersion\": \"4.2.26\","+
//          "      \"experimentName\": \"app logger\","+
//          "      \"responseTime\": 1500078678000,"+
//          "      \"experimentVersion\": 4,"+
//          "      \"timezone\": \"-07:00\","+
//          "      \"experimentGroupName\": \"New Group\","+
//          "      \"joined\": false,"+
//          "      \"emptyResponse\": false,"+
//          "      \"missedSignal\": false,"+
//          "      \"responses\": ["+
//          "        {"+
//          "          \"name\": \"apps_used\","+
//          "          \"answer\": \"ChargePoint\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"apps_used_raw\","+
//          "          \"answer\": \"com.coulombtech/com.cp.ui.activity.map.MapActivity\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"foreground\","+
//          "          \"answer\": \"true\""+
//          "        }"+
//          "      ]"+
//          "    },"+
//          "    {"+
//          "      \"experimentId\": 5755724958269440,"+
//          "      \"who\": \"rbe10001@gmail.com\","+
//          "      \"when\": 1500078686089,"+
//          "      \"appId\": \"Android\","+
//          "      \"pacoVersion\": \"4.2.26\","+
//          "      \"experimentName\": \"app logger\","+
//          "      \"responseTime\": 1500078676000,"+
//          "      \"experimentVersion\": 4,"+
//          "      \"timezone\": \"-07:00\","+
//          "      \"experimentGroupName\": \"New Group\","+
//          "      \"joined\": false,"+
//          "      \"emptyResponse\": false,"+
//          "      \"missedSignal\": false,"+
//          "      \"responses\": ["+
//          "        {"+
//          "          \"name\": \"foreground\","+
//          "          \"answer\": \"true\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"apps_used_raw\","+
//          "          \"answer\": \"com.coulombtech/com.cp.ui.activity.launcher.LauncherActivity\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"apps_used\","+
//          "          \"answer\": \"ChargePoint\""+
//          "        }"+
//          "      ]"+
//          "    },"+
//          "    {"+
//          "      \"experimentId\": 5755724958269440,"+
//          "      \"who\": \"rbe10001@gmail.com\","+
//          "      \"when\": 1500078685572,"+
//          "      \"appId\": \"Android\","+
//          "      \"pacoVersion\": \"4.2.26\","+
//          "      \"experimentName\": \"app logger\","+
//          "      \"responseTime\": 1500078673000,"+
//          "      \"experimentVersion\": 4,"+
//          "      \"timezone\": \"-07:00\","+
//          "      \"experimentGroupName\": \"New Group\","+
//          "      \"joined\": false,"+
//          "      \"emptyResponse\": false,"+
//          "      \"missedSignal\": false,"+
//          "      \"responses\": ["+
//          "        {"+
//          "          \"name\": \"foreground\","+
//          "          \"answer\": \"true\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"apps_used\","+
//          "          \"answer\": \"Google\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"apps_used_raw\","+
//          "          \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\""+
//          "        }"+
//          "      ]"+
//          "    },"+
//          "    {"+
//          "      \"experimentId\": 5755724958269440,"+
//          "      \"who\": \"rbe10001@gmail.com\","+
//          "      \"when\": 1500078685144,"+
//          "      \"appId\": \"Android\","+
//          "      \"pacoVersion\": \"4.2.26\","+
//          "      \"experimentName\": \"app logger\","+
//          "      \"responseTime\": 1500078667000,"+
//          "      \"experimentVersion\": 4,"+
//          "      \"timezone\": \"-07:00\","+
//          "      \"experimentGroupName\": \"New Group\","+
//          "      \"joined\": false,"+
//          "      \"emptyResponse\": false,"+
//          "      \"missedSignal\": false,"+
//          "      \"responses\": ["+
//          "        {"+
//          "          \"name\": \"apps_used\","+
//          "          \"answer\": \"NYTimes\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"foreground\","+
//          "          \"answer\": \"true\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"apps_used_raw\","+
//          "          \"answer\": \"com.nytimes.android/com.nytimes.android.MainActivity\""+
//          "        }"+
//          "      ]"+
//          "    },"+
//          "    {"+
//          "      \"experimentId\": 5755724958269440,"+
//          "      \"who\": \"rbe10001@gmail.com\","+
//          "      \"when\": 1500078684808,"+
//          "      \"appId\": \"Android\","+
//          "      \"pacoVersion\": \"4.2.26\","+
//          "      \"experimentName\": \"app logger\","+
//          "      \"responseTime\": 1500078664000,"+
//          "      \"experimentVersion\": 4,"+
//          "      \"timezone\": \"-07:00\","+
//          "      \"experimentGroupName\": \"New Group\","+
//          "      \"joined\": false,"+
//          "      \"emptyResponse\": false,"+
//          "      \"missedSignal\": false,"+
//          "      \"responses\": ["+
//          "        {"+
//          "          \"name\": \"apps_used\","+
//          "          \"answer\": \"Google\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"apps_used_raw\","+
//          "          \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"foreground\","+
//          "          \"answer\": \"true\""+
//          "        }"+
//          "      ]"+
//          "    },"+
//          "    {"+
//          "      \"experimentId\": 5755724958269440,"+
//          "      \"who\": \"rbe10001@gmail.com\","+
//          "      \"when\": 1500078660200,"+
//          "      \"appId\": \"Android\","+
//          "      \"pacoVersion\": \"4.2.26\","+
//          "      \"experimentName\": \"app logger\","+
//          "      \"responseTime\": 1500078653000,"+
//          "      \"experimentVersion\": 4,"+
//          "      \"timezone\": \"-07:00\","+
//          "      \"experimentGroupName\": \"New Group\","+
//          "      \"joined\": false,"+
//          "      \"emptyResponse\": false,"+
//          "      \"missedSignal\": false,"+
//          "      \"responses\": ["+
//          "        {"+
//          "          \"name\": \"apps_used_raw\","+
//          "          \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.TroubleshootingActivity\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"foreground\","+
//          "          \"answer\": \"true\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"apps_used\","+
//          "          \"answer\": \"Paco\""+
//          "        }"+
//          "      ]"+
//          "    },"+
//          "    {"+
//          "      \"experimentId\": 5755724958269440,"+
//          "      \"who\": \"rbe10001@gmail.com\","+
//          "      \"when\": 1500078659759,"+
//          "      \"appId\": \"Android\","+
//          "      \"pacoVersion\": \"4.2.26\","+
//          "      \"experimentName\": \"app logger\","+
//          "      \"responseTime\": 1500078650000,"+
//          "      \"experimentVersion\": 4,"+
//          "      \"timezone\": \"-07:00\","+
//          "      \"experimentGroupName\": \"New Group\","+
//          "      \"joined\": false,"+
//          "      \"emptyResponse\": false,"+
//          "      \"missedSignal\": false,"+
//          "      \"responses\": ["+
//          "        {"+
//          "          \"name\": \"apps_used\","+
//          "          \"answer\": \"Paco\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"apps_used_raw\","+
//          "          \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"foreground\","+
//          "          \"answer\": \"true\""+
//          "        }"+
//          "      ]"+
//          "    },"+
//          "    {"+
//          "      \"experimentId\": 5755724958269440,"+
//          "      \"who\": \"rbe10001@gmail.com\","+
//          "      \"when\": 1500078659323,"+
//          "      \"appId\": \"Android\","+
//          "      \"pacoVersion\": \"4.2.26\","+
//          "      \"experimentName\": \"app logger\","+
//          "      \"responseTime\": 1500078642000,"+
//          "      \"experimentVersion\": 4,"+
//          "      \"timezone\": \"-07:00\","+
//          "      \"experimentGroupName\": \"New Group\","+
//          "      \"joined\": false,"+
//          "      \"emptyResponse\": false,"+
//          "      \"missedSignal\": false,"+
//          "      \"responses\": ["+
//          "        {"+
//          "          \"name\": \"foreground\","+
//          "          \"answer\": \"true\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"apps_used_raw\","+
//          "          \"answer\": \"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"apps_used\","+
//          "          \"answer\": \"Google\""+
//          "        }"+
//          "      ]"+
//          "    },"+
//          "    {"+
//          "      \"experimentId\": 5755724958269440,"+
//          "      \"who\": \"rbe10001@gmail.com\","+
//          "      \"when\": 1500078658913,"+
//          "      \"appId\": \"Android\","+
//          "      \"pacoVersion\": \"4.2.26\","+
//          "      \"experimentName\": \"app logger\","+
//          "      \"responseTime\": 1500078640000,"+
//          "      \"experimentVersion\": 4,"+
//          "      \"timezone\": \"-07:00\","+
//          "      \"experimentGroupName\": \"New Group\","+
//          "      \"joined\": false,"+
//          "      \"emptyResponse\": false,"+
//          "      \"missedSignal\": false,"+
//          "      \"responses\": ["+
//          "        {"+
//          "          \"name\": \"apps_used\","+
//          "          \"answer\": \"Paco\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"foreground\","+
//          "          \"answer\": \"true\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"apps_used_raw\","+
//          "          \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\""+
//          "        }"+
//          "      ]"+
//          "    },"+
//          "    {"+
//          "      \"experimentId\": 5755724958269440,"+
//          "      \"who\": \"rbe10001@gmail.com\","+
//          "      \"when\": 1500078658492,"+
//          "      \"appId\": \"Android\","+
//          "      \"pacoVersion\": \"4.2.26\","+
//          "      \"experimentName\": \"app logger\","+
//          "      \"responseTime\": 1500078640000,"+
//          "      \"experimentVersion\": 4,"+
//          "      \"timezone\": \"-07:00\","+
//          "      \"experimentGroupName\": \"New Group\","+
//          "      \"joined\": false,"+
//          "      \"emptyResponse\": false,"+
//          "      \"missedSignal\": false,"+
//          "      \"responses\": ["+
//          "        {"+
//          "          \"name\": \"apps_used_raw\","+
//          "          \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.FindMyExperimentsActivity\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"apps_used\","+
//          "          \"answer\": \"Paco\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"foreground\","+
//          "          \"answer\": \"true\""+
//          "        }"+
//          "      ]"+
//          "    },"+
//          "    {"+
//          "      \"experimentId\": 5755724958269440,"+
//          "      \"who\": \"rbe10001@gmail.com\","+
//          "      \"when\": 1500078658004,"+
//          "      \"appId\": \"Android\","+
//          "      \"pacoVersion\": \"4.2.26\","+
//          "      \"experimentName\": \"app logger\","+
//          "      \"responseTime\": 1500078640000,"+
//          "      \"experimentVersion\": 4,"+
//          "      \"timezone\": \"-07:00\","+
//          "      \"experimentGroupName\": \"New Group\","+
//          "      \"joined\": false,"+
//          "      \"emptyResponse\": false,"+
//          "      \"missedSignal\": false,"+
//          "      \"responses\": ["+
//          "        {"+
//          "          \"name\": \"foreground\","+
//          "          \"answer\": \"true\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"apps_used_raw\","+
//          "          \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.ExperimentDetailActivity\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"apps_used\","+
//          "          \"answer\": \"Paco\""+
//          "        }"+
//          "      ]"+
//          "    },"+
//          "    {"+
//          "      \"experimentId\": 5755724958269440,"+
//          "      \"who\": \"rbe10001@gmail.com\","+
//          "      \"when\": 1500078657493,"+
//          "      \"appId\": \"Android\","+
//          "      \"pacoVersion\": \"4.2.26\","+
//          "      \"experimentName\": \"app logger\","+
//          "      \"responseTime\": 1500078640000,"+
//          "      \"experimentVersion\": 4,"+
//          "      \"timezone\": \"-07:00\","+
//          "      \"experimentGroupName\": \"New Group\","+
//          "      \"joined\": false,"+
//          "      \"emptyResponse\": false,"+
//          "      \"missedSignal\": false,"+
//          "      \"responses\": ["+
//          "        {"+
//          "          \"name\": \"apps_used_raw\","+
//          "          \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.InformedConsentActivity\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"foreground\","+
//          "          \"answer\": \"true\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"apps_used\","+
//          "          \"answer\": \"Paco\""+
//          "        }"+
//          "      ]"+
//          "    },"+
//          "    {"+
//          "      \"experimentId\": 5755724958269440,"+
//          "      \"who\": \"rbe10001@gmail.com\","+
//          "      \"when\": 1500078657013,"+
//          "      \"appId\": \"Android\","+
//          "      \"pacoVersion\": \"4.2.26\","+
//          "      \"experimentName\": \"app logger\","+
//          "      \"responseTime\": 1500078639000,"+
//          "      \"experimentVersion\": 4,"+
//          "      \"timezone\": \"-07:00\","+
//          "      \"experimentGroupName\": \"New Group\","+
//          "      \"joined\": false,"+
//          "      \"emptyResponse\": false,"+
//          "      \"missedSignal\": false,"+
//          "      \"responses\": ["+
//          "        {"+
//          "          \"name\": \"apps_used\","+
//          "          \"answer\": \"Paco\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"apps_used_raw\","+
//          "          \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.PostJoinInstructionsActivity\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"foreground\","+
//          "          \"answer\": \"true\""+
//          "        }"+
//          "      ]"+
//          "    },"+
//          "    {"+
//          "      \"experimentId\": 5755724958269440,"+
//          "      \"who\": \"rbe10001@gmail.com\","+
//          "      \"when\": 1500078656558,"+
//          "      \"appId\": \"Android\","+
//          "      \"pacoVersion\": \"4.2.26\","+
//          "      \"experimentName\": \"app logger\","+
//          "      \"responseTime\": 1500078637000,"+
//          "      \"experimentVersion\": 4,"+
//          "      \"timezone\": \"-07:00\","+
//          "      \"experimentGroupName\": \"New Group\","+
//          "      \"joined\": false,"+
//          "      \"emptyResponse\": false,"+
//          "      \"missedSignal\": false,"+
//          "      \"responses\": ["+
//          "        {"+
//          "          \"name\": \"apps_used_raw\","+
//          "          \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.InformedConsentActivity\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"foreground\","+
//          "          \"answer\": \"true\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"apps_used\","+
//          "          \"answer\": \"Paco\""+
//          "        }"+
//          "      ]"+
//          "    },"+
//          "    {"+
//          "      \"experimentId\": 5755724958269440,"+
//          "      \"who\": \"rbe10001@gmail.com\","+
//          "      \"when\": 1500078656094,"+
//          "      \"appId\": \"Android\","+
//          "      \"pacoVersion\": \"4.2.26\","+
//          "      \"experimentName\": \"app logger\","+
//          "      \"responseTime\": 1500078637000,"+
//          "      \"experimentVersion\": 4,"+
//          "      \"timezone\": \"-07:00\","+
//          "      \"experimentGroupName\": \"New Group\","+
//          "      \"joined\": false,"+
//          "      \"emptyResponse\": false,"+
//          "      \"missedSignal\": false,"+
//          "      \"responses\": ["+
//          "        {"+
//          "          \"name\": \"apps_used_raw\","+
//          "          \"answer\": \"com.pacoapp.paco/com.pacoapp.paco.ui.ExperimentDetailActivity\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"apps_used\","+
//          "          \"answer\": \"Paco\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"foreground\","+
//          "          \"answer\": \"true\""+
//          "        }"+
//          "      ]"+
//          "    },"+
//          "    {"+
//          "      \"experimentId\": 5755724958269440,"+
//          "      \"who\": \"rbe10001@gmail.com\","+
//          "      \"when\": 1500078637932,"+
//          "      \"appId\": \"Android\","+
//          "      \"pacoVersion\": \"4.2.26\","+
//          "      \"experimentName\": \"app logger\","+
//          "      \"responseTime\": 1500078637000,"+
//          "      \"experimentVersion\": 4,"+
//          "      \"timezone\": \"-07:00\","+
//          "      \"joined\": false,"+
//          "      \"emptyResponse\": false,"+
//          "      \"missedSignal\": false,"+
//          "      \"responses\": ["+
//          "        {"+
//          "          \"name\": \"schedule\","+
//          "          \"answer\": \"New Group:[]\""+
//          "        },"+
//          "        {"+
//          "          \"name\": \"joined\","+
//          "          \"answer\": \"true\""+
//          "        }"+
//          "      ]"+
//          "    }"+
            "  ]";


  public static List<EventDAO> getEventsForOneSession() {
    return getEventsForJson(oneSession);
  }

  private static List<EventDAO> getEventsForJson(String eventJson) {
    try {
      return JsonConverter.getObjectMapper().readValue(eventJson, new TypeReference<List<EventDAO>>() {});
    } catch (JsonParseException e) {
      e.printStackTrace();
    } catch (JsonMappingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static List<EventDAO> getEventsForOneBrokenSession() {
    try {
      final List<EventDAO> events = JsonConverter.getObjectMapper().readValue(brokenSession, new TypeReference<List<EventDAO>>() {});
      // this data set was retrieved in reverse sort order
      return Lists.reverse(events);
    } catch (JsonParseException e) {
      e.printStackTrace();
    } catch (JsonMappingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static List<EventDAO> getUserResponseEvent() {
      String oneEvent = "     [{ \"experimentId\": 5755724958269440,"+
      "      \"who\": \"rbe10001@gmail.com\","+
      "      \"when\": 1500078637932,"+
      "      \"appId\": \"Android\","+
      "      \"pacoVersion\": \"4.2.26\","+
      "      \"experimentName\": \"app logger\","+
      "      \"responseTime\": 1500078637000,"+
      "      \"experimentVersion\": 4,"+
      "      \"timezone\": \"-07:00\","+
      "      \"joined\": false,"+
      "      \"emptyResponse\": false,"+
      "      \"missedSignal\": false,"+
      "      \"responses\": ["+
      "        {"+
      "          \"name\": \"q1\","+
      "          \"answer\": \"my answer\""+
      "        } " +
      "      ]"+
      "    }"+
        "  ]";
      return getEventsForJson(oneEvent);

  }
}