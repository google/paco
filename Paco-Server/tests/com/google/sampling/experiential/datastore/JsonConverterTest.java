package com.google.sampling.experiential.datastore;

import java.util.List;

import junit.framework.TestCase;

import com.google.common.collect.Lists;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.paco.shared.model.InputDAO;
import com.google.paco.shared.model.SignalScheduleDAO;
import com.google.paco.shared.model.SignalingMechanismDAO;

public class JsonConverterTest extends TestCase {

  private List<ExperimentDAO> experimentList;

  protected void setUp() throws Exception {
    super.setUp();

    experimentList = Lists.newArrayList();
    experimentList.add(new ExperimentDAO());

    SignalingMechanismDAO[] signalingMechanisms = new SignalingMechanismDAO[1];
    signalingMechanisms[0] = null;
    String[] publishedAdmins = new String[1];
    publishedAdmins[0] = "example@example.com";
    experimentList.add(new ExperimentDAO(new Long(1), "1title", "1descr", "1consent", "1email",
                                         signalingMechanisms, true, false, null , null, null,
                                         null, null, false, publishedAdmins, publishedAdmins,
                                         false, false, 1));
  }

  public void testShortJsonifyShortening() throws Exception {
    String longJson = JsonConverter.jsonify(experimentList);
    String shortJson = JsonConverter.shortJsonify(experimentList);
    assertTrue(shortJson.length() < longJson.length());
  }

  public void testJsonifyCatchesWholeGraph() throws Exception {
    ExperimentDAO experiment = experimentList.get(1);

    SignalingMechanismDAO[] signalingMechanisms = new SignalingMechanismDAO[1];
    SignalScheduleDAO signalSchedule = createEsmSignalSchedule();


    signalingMechanisms[0] = signalSchedule;
    experiment.setSignalingMechanisms(signalingMechanisms);

    InputDAO[] inputs = new InputDAO[1];
    InputDAO input = new InputDAO();
    input.setResponseType(InputDAO.OPEN_TEXT);

    input.setText("Prompt");
    inputs[0] = input;

    experiment.setInputs(inputs);

    String json = JsonConverter.jsonify(experiment);
    ExperimentDAO newExperiment = JsonConverter.fromSingleEntityJson(json);
    assertEqualSchedules(experiment.getSignalingMechanisms(), newExperiment.getSignalingMechanisms());
    assertEqualInputs(experiment.getInputs(), newExperiment.getInputs());
  }

  private void assertEqualInputs(InputDAO[] expectedInputs, InputDAO[] actualInputs) {
    assertEquals(expectedInputs.length, actualInputs.length);
    InputDAO expectedInput = expectedInputs[0];
    InputDAO actualInput = actualInputs[0];
    assertEquals(expectedInput.getResponseType(), actualInput.getResponseType());
    assertEquals(expectedInput.getText(), actualInput.getText());
  }

  private SignalScheduleDAO createEsmSignalSchedule() {
    SignalScheduleDAO signalSchedule = new SignalScheduleDAO();
    signalSchedule.setScheduleType(SignalScheduleDAO.ESM);
    signalSchedule.setEsmPeriodInDays(SignalScheduleDAO.ESM_PERIOD_DAY);
    signalSchedule.setEsmFrequency(8);
    signalSchedule.setEsmStartHour(3600000 * 9l);
    signalSchedule.setEsmEndHour(3600000 * 19l);
    signalSchedule.setEsmWeekends(true);
    return signalSchedule;
  }

  private void assertEqualSchedules(SignalingMechanismDAO[] expectedSignalingMechanisms,
                                    SignalingMechanismDAO[] actualSignalingMechanisms) {
    assertEquals(expectedSignalingMechanisms.length, actualSignalingMechanisms.length);
    SignalingMechanismDAO expectedSignalingMechanism = expectedSignalingMechanisms[0];
    SignalingMechanismDAO actualSignalingMechanism = actualSignalingMechanisms[0];
    assertEquals(expectedSignalingMechanism.getType(), actualSignalingMechanism.getType());

    SignalScheduleDAO expectedSignalSchedule = (SignalScheduleDAO)expectedSignalingMechanism;
    SignalScheduleDAO actualSignalSchedule = (SignalScheduleDAO)actualSignalingMechanism;

    assertEquals(expectedSignalSchedule.getScheduleType(), actualSignalSchedule.getScheduleType());
    assertEquals(expectedSignalSchedule.getEsmFrequency(), actualSignalSchedule.getEsmFrequency());



  }
}
