package com.google.sampling.experiential.server;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.sampling.experiential.model.PhotoBlob;
import com.google.sampling.experiential.model.What;
import com.pacoapp.paco.shared.comm.Outcome;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentQueryResult;
import com.pacoapp.paco.shared.model2.ValidationMessage;

import junit.framework.TestCase;

public class EventJsonUploadProcessorTest extends TestCase {

  private static final String who = "steve@bad_rapper_names.com";
  private EventRetriever noOpEventRetriever;
  private EventRetriever blowUpEventRetriever;
  private EventRetriever noOpThenBlowUpEventRetriever;
  private ExperimentService emptyExperimentService;
  private ExperimentService noExperimentService;
  private ExperimentService notAllowedExperimentService;
  private ExperimentService noThenYesExperimentService;

  @Before
  public void setUp() {
    noOpEventRetriever = new EventRetriever() {
      @Override
      public void postEvent(boolean persistInCloudSql, JSONObject eventJson, String who, String lat, String lon, Date whenDate, String appId, String pacoVersion,
                            Set<What> what, boolean shared, String experimentId, String experimentName, Integer experimentVersion,
                            Date responseTime, Date scheduledTime, List<PhotoBlob> blobs, String timezone,
                            String groupName, Long actionTriggerId, Long actionTriggerSpecId, Long actionId) {
      }
    };
    blowUpEventRetriever = new EventRetriever() {
      @Override
      public void postEvent(boolean persistInCloudSql, JSONObject eventJson, String who, String lat, String lon, Date whenDate, String appId, String pacoVersion,
                            Set<What> what, boolean shared, String experimentId, String experimentName, Integer experimentVersion,
                            Date responseTime, Date scheduledTime, List<PhotoBlob> blobs, String timezone,
                            String groupName, Long actionTriggerId, Long actionTriggerSpecId, Long actionId) {
        throw new IllegalArgumentException("This event is bad");
      }
    };

    noOpThenBlowUpEventRetriever = new EventRetriever() {
      private boolean second = false;

      @Override
      public void postEvent(boolean persistInCloudSql, JSONObject eventJson, String who, String lat, String lon, Date whenDate, String appId, String pacoVersion,
                            Set<What> what, boolean shared, String experimentId, String experimentName, Integer experimentVersion,
                            Date responseTime, Date scheduledTime, List<PhotoBlob> blobs, String timezone,
                            String groupName, Long actionTriggerId, Long actionTriggerSpecId, Long actionId) {
        if (!second) {
          second = true;
        } else {
          throw new IllegalArgumentException("This event is bad");
        }
      }
    };

    emptyExperimentService = new ExperimentService() {
      @Override
      public ExperimentDAO getExperiment(Long experimentId) {
        return new ExperimentDAO() {
          @Override
          public boolean isWhoAllowedToPostToExperiment(String who) {
            return true;
          };
        };
      }

      @Override
      public ExperimentDAO getReferredExperiment(long parseLong) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public Boolean deleteExperiment(ExperimentDAO experimentDAO, String loggedInUserEmail) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public void setReferredExperiment(Long referringExperimentId, Long referencedExperimentId) {
        // TODO Auto-generated method stub

      }

      @Override
      public ExperimentQueryResult getMyJoinableExperiments(String lowerCase, DateTimeZone timeZoneForClient,
                                                            Integer limit, String cursor) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public List<ExperimentDAO> getExperimentsById(List<Long> experimentIds, String email, DateTimeZone timezone) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public ExperimentQueryResult getUsersAdministeredExperiments(String email, DateTimeZone timezone, Integer limit,
                                                                   String cursor, String sortColumn, String sortOrder) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public ExperimentQueryResult getExperimentsPublishedPublicly(DateTimeZone timezone, Integer limit, String cursor, String email) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public boolean isOver(ExperimentDAO experiment, DateTime today) {
        // TODO Auto-generated method stub
        return false;
      }

      @Override
      public Boolean deleteExperiment(Long experimentId, String loggedInUserEmail) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public List<ValidationMessage> saveExperiment(ExperimentDAO experimentDAO, String userFromLogin, DateTimeZone timezone) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public Boolean deleteExperiments(List<Long> experimentIds, String email) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public ExperimentQueryResult getMyJoinedExperiments(String email, DateTimeZone timezone, Integer limit,
                                                          String cursor, String sortColumn, String sortOrder) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public ExperimentQueryResult getAllExperiments(String cursor) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public List<ValidationMessage> saveExperiment(ExperimentDAO experimentDAO, String loggedInUserEmail,
                                                    DateTimeZone timezone, Boolean validate) {
        // TODO Auto-generated method stub
        return null;
      }

    };

    noExperimentService = new ExperimentService() {
      @Override
      public ExperimentDAO getExperiment(Long experimentId) {
        return null;
      }

      @Override
      public ExperimentDAO getReferredExperiment(long parseLong) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public Boolean deleteExperiment(ExperimentDAO experimentDAO, String loggedInUserEmail) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public void setReferredExperiment(Long referringExperimentId, Long referencedExperimentId) {
        // TODO Auto-generated method stub

      }

      @Override
      public ExperimentQueryResult getMyJoinableExperiments(String lowerCase, DateTimeZone timeZoneForClient,
                                                            Integer limit, String cursor) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public List<ExperimentDAO> getExperimentsById(List<Long> experimentIds, String email, DateTimeZone timezone) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public ExperimentQueryResult getUsersAdministeredExperiments(String email, DateTimeZone timezone, Integer limit,
                                                                   String cursor, String sortColumn, String sortOrder) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public ExperimentQueryResult getExperimentsPublishedPublicly(DateTimeZone timezone, Integer limit, String cursor, String email) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public boolean isOver(ExperimentDAO experiment, DateTime today) {
        // TODO Auto-generated method stub
        return false;
      }

      @Override
      public Boolean deleteExperiment(Long experimentId, String loggedInUserEmail) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public List<ValidationMessage> saveExperiment(ExperimentDAO experimentDAO, String userFromLogin, DateTimeZone timezone) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public Boolean deleteExperiments(List<Long> experimentIds, String email) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public ExperimentQueryResult getMyJoinedExperiments(String email, DateTimeZone timezone, Integer limit,
                                                          String cursor, String sortColumn, String sortOrder) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public ExperimentQueryResult getAllExperiments(String cursor) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public List<ValidationMessage> saveExperiment(ExperimentDAO experimentDAO, String loggedInUserEmail,
                                                    DateTimeZone timezone, Boolean validate) {
        // TODO Auto-generated method stub
        return null;
      }

    };

    notAllowedExperimentService = new ExperimentService() {
      @Override
      public ExperimentDAO getExperiment(Long experimentId) {
        return new ExperimentDAO() {
          @Override
          public boolean isWhoAllowedToPostToExperiment(String who) {
            return false;
          };
        };
      }

      @Override
      public ExperimentDAO getReferredExperiment(long parseLong) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public Boolean deleteExperiment(ExperimentDAO experimentDAO, String loggedInUserEmail) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public void setReferredExperiment(Long referringExperimentId, Long referencedExperimentId) {
        // TODO Auto-generated method stub

      }

      @Override
      public ExperimentQueryResult getMyJoinableExperiments(String lowerCase, DateTimeZone timeZoneForClient,
                                                            Integer limit, String cursor) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public List<ExperimentDAO> getExperimentsById(List<Long> experimentIds, String email, DateTimeZone timezone) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public ExperimentQueryResult getUsersAdministeredExperiments(String email, DateTimeZone timezone, Integer limit,
                                                                   String cursor, String sortColumn, String sortOrder) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public ExperimentQueryResult getExperimentsPublishedPublicly(DateTimeZone timezone, Integer limit, String cursor, String email) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public boolean isOver(ExperimentDAO experiment, DateTime today) {
        // TODO Auto-generated method stub
        return false;
      }

      @Override
      public Boolean deleteExperiment(Long experimentId, String loggedInUserEmail) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public List<ValidationMessage> saveExperiment(ExperimentDAO experimentDAO, String userFromLogin, DateTimeZone timezone) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public Boolean deleteExperiments(List<Long> experimentIds, String email) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public ExperimentQueryResult getMyJoinedExperiments(String email, DateTimeZone timezone, Integer limit,
                                                          String cursor, String sortColumn, String sortOrder) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public ExperimentQueryResult getAllExperiments(String cursor) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public List<ValidationMessage> saveExperiment(ExperimentDAO experimentDAO, String loggedInUserEmail,
                                                    DateTimeZone timezone, Boolean validate) {
        // TODO Auto-generated method stub
        return null;
      }

    };

    noThenYesExperimentService = new ExperimentService() {
      private boolean second;
      @Override
      public ExperimentDAO getExperiment(Long experimentId) {
        if (!second) {
          second = true;
          return null;
        } else {
          return new ExperimentDAO() {
            @Override
            public boolean isWhoAllowedToPostToExperiment(String who) {
              return true;
            };
          };
        }
      }
      @Override
      public ExperimentDAO getReferredExperiment(long parseLong) {
        // TODO Auto-generated method stub
        return null;
      }
      @Override
      public Boolean deleteExperiment(ExperimentDAO experimentDAO, String loggedInUserEmail) {
        // TODO Auto-generated method stub
        return null;
      }
      @Override
      public void setReferredExperiment(Long referringExperimentId, Long referencedExperimentId) {
        // TODO Auto-generated method stub

      }
      @Override
      public ExperimentQueryResult getMyJoinableExperiments(String lowerCase, DateTimeZone timeZoneForClient,
                                                            Integer limit, String cursor) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public List<ExperimentDAO> getExperimentsById(List<Long> experimentIds, String email, DateTimeZone timezone) {
        // TODO Auto-generated method stub
        return null;
      }
      @Override
      public ExperimentQueryResult getUsersAdministeredExperiments(String email, DateTimeZone timezone, Integer limit,
                                                                   String cursor, String sortColumn, String sortOrder) {
        // TODO Auto-generated method stub
        return null;
      }
      @Override
      public ExperimentQueryResult getExperimentsPublishedPublicly(DateTimeZone timezone, Integer limit, String cursor, String email) {
        // TODO Auto-generated method stub
        return null;
      }
      @Override
      public boolean isOver(ExperimentDAO experiment, DateTime today) {
        // TODO Auto-generated method stub
        return false;
      }
      @Override
      public Boolean deleteExperiment(Long experimentId, String loggedInUserEmail) {
        // TODO Auto-generated method stub
        return null;
      }
      @Override
      public List<ValidationMessage> saveExperiment(ExperimentDAO experimentDAO, String userFromLogin, DateTimeZone timezone) {
        // TODO Auto-generated method stub
        return null;
      }
      @Override
      public Boolean deleteExperiments(List<Long> experimentIds, String email) {
        // TODO Auto-generated method stub
        return null;
      }
      @Override
      public ExperimentQueryResult getMyJoinedExperiments(String email, DateTimeZone timezone, Integer limit,
                                                          String cursor, String sortColumn, String sortOrder) {
        // TODO Auto-generated method stub
        return null;
      }
      @Override
      public ExperimentQueryResult getAllExperiments(String cursor) {
        // TODO Auto-generated method stub
        return null;
      }
      @Override
      public List<ValidationMessage> saveExperiment(ExperimentDAO experimentDAO, String loggedInUserEmail,
                                                    DateTimeZone timezone, Boolean validate) {
        // TODO Auto-generated method stub
        return null;
      }
    };
  }

  private String toJson(Object... outcomes) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    mapper.getSerializationConfig().setSerializationInclusion(Inclusion.NON_NULL);
    return mapper.writeValueAsString(Lists.newArrayList(outcomes));
  }

  @Test
  public void testEmptyBodyEvent() throws Exception {
    EventJsonUploadProcessor ejup = new EventJsonUploadProcessor(emptyExperimentService, noOpEventRetriever);
    try {
      ejup.processJsonEvents("", who, null, null);
      fail("Should have complained about empty json string");
    } catch (IllegalArgumentException e) {
    }
  }

  @Test
  public void testBadJsonBodyEvent() throws Exception {
    EventJsonUploadProcessor ejup = new EventJsonUploadProcessor(emptyExperimentService, noOpEventRetriever);
    try {
      ejup.processJsonEvents("[{}", who, null, null);
      fail("Should have complained about bad json string");
    } catch (IllegalArgumentException e) {
    }
  }


  @Test
  public void testSingleJsonEventNoId() throws Exception {
    EventJsonUploadProcessor ejup = new EventJsonUploadProcessor(emptyExperimentService, noOpEventRetriever);
    String result = ejup.processJsonEvents("{}", who, null, null);
    String expectedOutcomeJson = toJson(new Outcome(0, "No experiment ID for this event: 0"));
    assertEquals(expectedOutcomeJson, result);
  }

  @Test
  public void testSingleJsonEvent() throws Exception {
    EventJsonUploadProcessor ejup = new EventJsonUploadProcessor(emptyExperimentService, noOpEventRetriever);
    String result = ejup.processJsonEvents("{\"experimentId\" : \"1\"}", who, null, null);
    String expectedOutcomeJson = toJson(new Outcome(0));
    assertEquals(expectedOutcomeJson, result);
  }


  @Test
  public void testEmptyJsonArray() throws Exception {
    EventJsonUploadProcessor ejup = new EventJsonUploadProcessor(emptyExperimentService, noOpEventRetriever);
    String result = ejup.processJsonEvents("[]", who, null, null);
    assertEquals("[]", result);
  }

  @Test
  public void testTwoEventJsonArray() throws Exception {
    EventJsonUploadProcessor ejup = new EventJsonUploadProcessor(emptyExperimentService, noOpEventRetriever);
    String result = ejup.processJsonEvents("[{\"experimentId\" : \"1\", \"foo\" : \"bar\"}, { \"experimentId\" : \"2\", \"foo2\" : \"baz\"}]", who, null, null);
    String expectedOutcomeJson = toJson(new Outcome(0), new Outcome(1));
    assertEquals(expectedOutcomeJson, result);
  }

  @Test
  public void testBadEvent() throws Exception {
    EventJsonUploadProcessor ejup = new EventJsonUploadProcessor(emptyExperimentService, blowUpEventRetriever);
    String result = ejup.processJsonEvents("[{\"experimentId\" : \"ignored\"}]", who, null, null);
    String expectedOutcomeJson = toJson(new Outcome(0, "experimentId, ignored, not a number for this event: 0"));
    assertEquals(expectedOutcomeJson, result);
  }

  @Test
  public void testOneGoodEventOneBadEvent() throws Exception {
    EventJsonUploadProcessor ejup = new EventJsonUploadProcessor(emptyExperimentService, noOpThenBlowUpEventRetriever);
    String result = ejup.processJsonEvents("[{\"experimentId\" : \"1\"},{\"experimentId\" : \"2\"}]", who, null, null);
    String expectedOutcomeJson = toJson(new Outcome(0),
                                        new Outcome(1, "Exception posting event: 1. This event is bad"));
    assertEquals(expectedOutcomeJson, result);
  }

  @Test
  public void testExperimentDoesNotExist() throws Exception {
    EventJsonUploadProcessor ejup = new EventJsonUploadProcessor(noExperimentService, noOpEventRetriever);
    String result = ejup.processJsonEvents("{\"experimentId\" : \"525\"}", who, null, null);
    String expectedOutcomeJson = toJson(new Outcome(0, "No existing experiment for this event: 0"));
    assertEquals(expectedOutcomeJson, result);
  }

  @Test
  public void testOneOfTwoExperimentsDoesNotExist() throws Exception {
    EventJsonUploadProcessor ejup = new EventJsonUploadProcessor(noThenYesExperimentService, noOpEventRetriever);
    String result = ejup.processJsonEvents("[{\"experimentId\" : \"525\"},{\"experimentId\" : \"1\"}]", who, null, null);
    String expectedOutcomeJson = toJson(new Outcome(0, "No existing experiment for this event: 0"), new Outcome(1));
    assertEquals(expectedOutcomeJson, result);
  }


  @Test
  public void testExperimentNotAllowed() throws Exception {
    EventJsonUploadProcessor ejup = new EventJsonUploadProcessor(notAllowedExperimentService, noOpEventRetriever);
    String result = ejup.processJsonEvents("{\"experimentId\" : \"ignored\"}", who, null, null);
    String expectedOutcomeJson = toJson(new Outcome(0, "experimentId, ignored, not a number for this event: 0"));
    assertEquals(expectedOutcomeJson, result);
  }

  @Test
  public void testDateParseErrorJsonArray() throws Exception {
    EventJsonUploadProcessor ejup = new EventJsonUploadProcessor(emptyExperimentService, noOpEventRetriever);
    String result = ejup.processJsonEvents("[{\"responseTime\":\"12baddate\",\"experimentId\" : \"1\"}]", who, null, null);
    String expectedOutcomeJson = toJson(new Outcome(0,"Exception posting event: 0. Invalid format: \"12baddate\" is malformed at \"baddate\""));
    assertEquals(expectedOutcomeJson, result);
  }

  @Test
  public void testOneOfTwoExperimentsParseErrorsNotExist() throws Exception {
    EventJsonUploadProcessor ejup = new EventJsonUploadProcessor(emptyExperimentService, noOpEventRetriever);
    String result = ejup.processJsonEvents("[{\"experimentId\" : \"1\",\"responseTime\":\"12baddate\"},{\"experimentId\" : \"2\"}]", who, null, null);
    String expectedOutcomeJson = toJson(new Outcome(0, "Exception posting event: 0. Invalid format: \"12baddate\" is malformed at \"baddate\""), new Outcome(1));
    assertEquals(expectedOutcomeJson, result);
  }

}
