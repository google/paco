package com.google.sampling.experiential.server;

import java.util.Date;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.paco.shared.Outcome;
import com.google.sampling.experiential.model.Experiment;
import com.google.sampling.experiential.model.PhotoBlob;
import com.google.sampling.experiential.model.What;

public class EventJsonUploadProcessorTest extends TestCase {

  private static final String who = "steve@bad_rapper_names.com";
  private EventRetriever noOpEventRetriever;
  private EventRetriever blowUpEventRetriever;
  private EventRetriever noOpThenBlowUpEventRetriever;
  private ExperimentRetriever emptyExperimentRetriever;
  private ExperimentRetriever noExperimentRetriever;
  private ExperimentRetriever notAllowedExperimentRetriever;
  private ExperimentRetriever noThenYesExperimentRetriever;

  @Before
  public void setUp() {
    noOpEventRetriever = new EventRetriever() {
      @Override
      public void postEvent(String who, String lat, String lon, Date whenDate, String appId, String pacoVersion,
                            Set<What> what, boolean shared, String experimentId, String experimentName, Integer experimentVersion,
                            Date responseTime, Date scheduledTime, List<PhotoBlob> blobs, String timezone) {
      }
    };
    blowUpEventRetriever = new EventRetriever() {
      @Override
      public void postEvent(String who, String lat, String lon, Date whenDate, String appId, String pacoVersion,
                            Set<What> what, boolean shared, String experimentId, String experimentName, Integer experimentVersion,
                            Date responseTime, Date scheduledTime, List<PhotoBlob> blobs, String timezone) {
        throw new IllegalArgumentException("This event is bad");
      }
    };

    noOpThenBlowUpEventRetriever = new EventRetriever() {
      private boolean second = false;

      @Override
      public void postEvent(String who, String lat, String lon, Date whenDate, String appId, String pacoVersion,
                            Set<What> what, boolean shared, String experimentId, String experimentName, Integer experimentVersion,
                            Date responseTime, Date scheduledTime, List<PhotoBlob> blobs, String timezone) {
        if (!second) {
          second = true;
        } else {
          throw new IllegalArgumentException("This event is bad");
        }
      }
    };

    emptyExperimentRetriever = new ExperimentRetriever() {
      @Override
      public Experiment getExperiment(String experimentId) {
        return new Experiment() {
          @Override
          public boolean isWhoAllowedToPostToExperiment(String who) {
            return true;
          };
        };
      }
    };

    noExperimentRetriever = new ExperimentRetriever() {
      @Override
      public Experiment getExperiment(String experimentId) {
        return null;
      }
    };

    notAllowedExperimentRetriever = new ExperimentRetriever() {
      @Override
      public Experiment getExperiment(String experimentId) {
        return new Experiment() {
          @Override
          public boolean isWhoAllowedToPostToExperiment(String who) {
            return false;
          };
        };
      }
    };

    noThenYesExperimentRetriever = new ExperimentRetriever() {
      private boolean second;
      @Override
      public Experiment getExperiment(String experimentId) {
        if (!second) {
          second = true;
          return null;
        } else {
          return new Experiment() {
            @Override
            public boolean isWhoAllowedToPostToExperiment(String who) {
              return true;
            };
          };
        }
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
    EventJsonUploadProcessor ejup = new EventJsonUploadProcessor(emptyExperimentRetriever, noOpEventRetriever);
    try {
      ejup.processJsonEvents("", who, null, null);
      fail("Should have complained about empty json string");
    } catch (IllegalArgumentException e) {
    }
  }

  @Test
  public void testBadJsonBodyEvent() throws Exception {
    EventJsonUploadProcessor ejup = new EventJsonUploadProcessor(emptyExperimentRetriever, noOpEventRetriever);
    try {
      ejup.processJsonEvents("[{}", who, null, null);
      fail("Should have complained about bad json string");
    } catch (IllegalArgumentException e) {
    }
  }


  @Test
  public void testSingleJsonEventNoId() throws Exception {
    EventJsonUploadProcessor ejup = new EventJsonUploadProcessor(emptyExperimentRetriever, noOpEventRetriever);
    String result = ejup.processJsonEvents("{}", who, null, null);
    String expectedOutcomeJson = toJson(new Outcome(0, "No experiment ID for this event: 0"));
    assertEquals(expectedOutcomeJson, result);
  }

  @Test
  public void testSingleJsonEvent() throws Exception {
    EventJsonUploadProcessor ejup = new EventJsonUploadProcessor(emptyExperimentRetriever, noOpEventRetriever);
    String result = ejup.processJsonEvents("{\"experimentId\" : \"ignored\"}", who, null, null);
    String expectedOutcomeJson = toJson(new Outcome(0));
    assertEquals(expectedOutcomeJson, result);
  }


  @Test
  public void testEmptyJsonArray() throws Exception {
    EventJsonUploadProcessor ejup = new EventJsonUploadProcessor(emptyExperimentRetriever, noOpEventRetriever);
    String result = ejup.processJsonEvents("[{ \"experimentId\" : \"ignored\"}]", who, null, null);
    String expectedOutcomeJson = toJson(new Outcome(0));
    assertEquals(expectedOutcomeJson, result);
  }

  @Test
  public void testTwoEventJsonArray() throws Exception {
    EventJsonUploadProcessor ejup = new EventJsonUploadProcessor(emptyExperimentRetriever, noOpEventRetriever);
    String result = ejup.processJsonEvents("[{\"experimentId\" : \"ignored\", \"foo\" : \"bar\"}, { \"experimentId\" : \"ignored\", \"foo2\" : \"baz\"}]", who, null, null);
    String expectedOutcomeJson = toJson(new Outcome(0), new Outcome(1));
    assertEquals(expectedOutcomeJson, result);
  }

  @Test
  public void testBadEvent() throws Exception {
    EventJsonUploadProcessor ejup = new EventJsonUploadProcessor(emptyExperimentRetriever, blowUpEventRetriever);
    String result = ejup.processJsonEvents("[{\"experimentId\" : \"ignored\"}]", who, null, null);
    String expectedOutcomeJson = toJson(new Outcome(0, "Exception posting event: 0. This event is bad"));
    assertEquals(expectedOutcomeJson, result);
  }

  @Test
  public void testOneGoodEventOneBadEvent() throws Exception {
    EventJsonUploadProcessor ejup = new EventJsonUploadProcessor(emptyExperimentRetriever, noOpThenBlowUpEventRetriever);
    String result = ejup.processJsonEvents("[{\"experimentId\" : \"ignored\"},{\"experimentId\" : \"ignored\"}]", who, null, null);
    String expectedOutcomeJson = toJson(new Outcome(0),
                                        new Outcome(1, "Exception posting event: 1. This event is bad"));
    assertEquals(expectedOutcomeJson, result);
  }

  @Test
  public void testExperimentDoesNotExist() throws Exception {
    EventJsonUploadProcessor ejup = new EventJsonUploadProcessor(noExperimentRetriever, noOpEventRetriever);
    String result = ejup.processJsonEvents("{\"experimentId\" : \"ignored\"}", who, null, null);
    String expectedOutcomeJson = toJson(new Outcome(0, "No existing experiment for this event: 0"));
    assertEquals(expectedOutcomeJson, result);
  }

  @Test
  public void testOneOfTwoExperimentsDoesNotExist() throws Exception {
    EventJsonUploadProcessor ejup = new EventJsonUploadProcessor(noThenYesExperimentRetriever, noOpEventRetriever);
    String result = ejup.processJsonEvents("[{\"experimentId\" : \"ignored\"},{\"experimentId\" : \"ignored\"}]", who, null, null);
    String expectedOutcomeJson = toJson(new Outcome(0, "No existing experiment for this event: 0"), new Outcome(1));
    assertEquals(expectedOutcomeJson, result);
  }


  @Test
  public void testExperimentNotAllowed() throws Exception {
    EventJsonUploadProcessor ejup = new EventJsonUploadProcessor(notAllowedExperimentRetriever, noOpEventRetriever);
    String result = ejup.processJsonEvents("{\"experimentId\" : \"ignored\"}", who, null, null);
    String expectedOutcomeJson = toJson(new Outcome(0, "No existing experiment for this event: 0"));
    assertEquals(expectedOutcomeJson, result);
  }

  @Test
  public void testDateParseErrorJsonArray() throws Exception {
    EventJsonUploadProcessor ejup = new EventJsonUploadProcessor(emptyExperimentRetriever, noOpEventRetriever);
    String result = ejup.processJsonEvents("[{\"responseTime\":\"12baddate\",\"experimentId\" : \"ignored\"}]", who, null, null);
    String expectedOutcomeJson = toJson(new Outcome(0,"Exception posting event: 0. Invalid format: \"12baddate\" is malformed at \"baddate\""));
    assertEquals(expectedOutcomeJson, result);
  }

  @Test
  public void testOneOfTwoExperimentsParseErrorsNotExist() throws Exception {
    EventJsonUploadProcessor ejup = new EventJsonUploadProcessor(emptyExperimentRetriever, noOpEventRetriever);
    String result = ejup.processJsonEvents("[{\"experimentId\" : \"ignored\",\"responseTime\":\"12baddate\"},{\"experimentId\" : \"ignored\"}]", who, null, null);
    String expectedOutcomeJson = toJson(new Outcome(0, "Exception posting event: 0. Invalid format: \"12baddate\" is malformed at \"baddate\""), new Outcome(1));
    assertEquals(expectedOutcomeJson, result);
  }

}
