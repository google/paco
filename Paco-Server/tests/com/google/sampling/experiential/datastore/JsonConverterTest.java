package com.google.sampling.experiential.datastore;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.pacoapp.paco.shared.model2.ActionTrigger;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.JsonConverter;

import junit.framework.TestCase;

public class JsonConverterTest extends TestCase {

  private List<ExperimentDAO> experimentList;
  private String pacoProtocolOld = "3.1";

  protected void setUp() throws Exception {
    super.setUp();

    experimentList = Lists.newArrayList();
    //experimentList.add(new ExperimentDAO());

    List<ActionTrigger> signalingMechanisms = new java.util.ArrayList<ActionTrigger>();
    ArrayList publishedAdmins = new java.util.ArrayList();
    publishedAdmins.add("example@example.com");
    experimentList.add(new ExperimentDAO(new Long(1), "1title", "1descr", "1consent", "1email",
                                         (String)null /*pk*/, null, null, false,
                                         publishedAdmins, publishedAdmins,
                                         false, 1, false, null, null, false, null));
  }

  public void testShortJsonifyShortening() throws Exception {
    String longJson = JsonConverter.jsonify(experimentList, null, null, pacoProtocolOld );
    String shortJson = JsonConverter.shortJsonify(experimentList, null, null, pacoProtocolOld);
    assertTrue(shortJson.length() <= longJson.length());
  }

}
