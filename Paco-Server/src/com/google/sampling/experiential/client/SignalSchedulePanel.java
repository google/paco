/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.sampling.experiential.client;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.sampling.experiential.shared.SignalSchedule;

/**
 * Container for all scheduling configuration panels.
 *
 * @author Bob Evans
 *
 */
public class SignalSchedulePanel extends VerticalPanel {
  private CheckBox editableCheckBox;
  private SchedulePanel schedulePanel;
  private SignalPanel signalPanel;

  /**
   *
   */
  public SignalSchedulePanel() {
    super();

    addSignalSchedulePanel();
    addSchedulePanel();
    addSignalPanel();
  }

  /**
   * @param signalSchedule the signal schedule
   */
  public void setSignalSchedule(SignalSchedule signalSchedule) {
    updateSignalSchedulePanel(signalSchedule);
    updateSchedulePanel(signalSchedule);
    updateSignalPanel(signalSchedule);
  }

  /**
   * @return the signal schedule
   */
  public SignalSchedule getSignalSchedule() {
    SignalSchedule signalSchedule = new SignalSchedule();

    retrieveSignalSchedulePanel(signalSchedule);
    retrieveSchedulePanel(signalSchedule);
    retrieveSignalPanel(signalSchedule);

    return signalSchedule;
  }

  private void addSignalSchedulePanel() {
    Panel panel = new HorizontalPanel();
    Label label = new Label("User Editable: ");

    editableCheckBox = new CheckBox();

    panel.add(label);
    panel.add(editableCheckBox);

    add(panel);
  }

  private void updateSignalSchedulePanel(SignalSchedule signalSchedule) {
    editableCheckBox.setValue(signalSchedule.isEditable());
  }

  private void retrieveSignalSchedulePanel(SignalSchedule signalSchedule) {
    signalSchedule.setEditable(editableCheckBox.getValue());
  }

  private void addSchedulePanel() {
    schedulePanel = new SchedulePanel();

    add(schedulePanel);
  }

  private void updateSchedulePanel(SignalSchedule signalSchedule) {
    schedulePanel.setSchedule(signalSchedule.getSchedule());
  }

  private void retrieveSchedulePanel(SignalSchedule signalSchedule) {
    signalSchedule.setSchedule(schedulePanel.getSchedule());
  }

  private void addSignalPanel() {
    signalPanel = new SignalPanel();

    add(signalPanel);
  }

  private void updateSignalPanel(SignalSchedule signalSchedule) {
    signalPanel.setSignal(signalSchedule.getSignal());
  }

  private void retrieveSignalPanel(SignalSchedule signalSchedule) {
    signalSchedule.setSignal(signalPanel.getSignal());
  }
}
