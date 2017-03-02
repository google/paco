package com.google.android.apps.paco;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.model.ExperimentUtil;

public class ExperimentUtilTestCase {

  @Before
  public void before() {
    ExperimentProviderUtil.loadColumnTableAssociationMap();
  }

  @Test
  public void testIdentifyTablesInvolved_withStar() {
    final String EVENTS_OUTPUTS_TABLE_NAME = "eventsoutputs";
    List<String> inpList = new ArrayList<String>();
    inpList.add("*");
    String actualReturnValue = ExperimentUtil.identifyTablesInvolved(ExperimentProviderUtil.getEventsOutputColumns(),
                                                                     inpList);
    assertEquals(EVENTS_OUTPUTS_TABLE_NAME, actualReturnValue);
  }

  @Test
  public void testIdentifyTablesInvolved_withBothTable() {
    final String EVENTS_OUTPUTS_TABLE_NAME = "eventsoutputs";
    List<String> inpList = new ArrayList<String>();
    inpList.add("Response_time");
    inpList.add("answer");
    String actualReturnValue = ExperimentUtil.identifyTablesInvolved(ExperimentProviderUtil.getEventsOutputColumns(),
                                                                     inpList);
    assertEquals(EVENTS_OUTPUTS_TABLE_NAME, actualReturnValue);
  }

  @Test
  public void testIdentifyTablesInvolved_withSingleTable() {
    final String EVENTS_TABLE_NAME = "events";
    List<String> inpList = new ArrayList<String>();
    inpList.add("Response_time");
    inpList.add("experiment_version");
    String actualReturnValue = ExperimentUtil.identifyTablesInvolved(ExperimentProviderUtil.getEventsOutputColumns(),
                                                                     inpList);
    assertEquals(EVENTS_TABLE_NAME, actualReturnValue);
  }
  // @Test
  // public void testCreateEventWithPartialResponses_Sc1(){
  //
  // final String[] col = new String[]{"experiment_group_name"};
  // final Object[] colValue = new String[]{"New Group"};
  // MatrixCursor mc = new MatrixCursor(col);
  // mc.addRow(colValue);
  // Event actualEvent = ExperimentUtil.createEventWithPartialResponses(mc);
  // assertEquals("New Group", actualEvent.getExperimentGroupName());
  // }
}
