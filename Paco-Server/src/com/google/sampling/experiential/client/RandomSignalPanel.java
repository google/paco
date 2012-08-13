// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.client;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.sampling.experiential.shared.RandomSignal;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class RandomSignalPanel extends VerticalPanel {
  private TextBox frequencyTextBox;
  private TimePickerFixed startTimePicker;
  private TimePickerFixed endTimePicker;

  /**
   *
   */
  public RandomSignalPanel() {
    super();

    addFrequencyPanel();
    addTimesPanel();
  }

  /**
   * @return
   */
  public RandomSignal getSignal() {
    RandomSignal signal = new RandomSignal();

    retrieveFrequencyPanel(signal);
    retrieveTimesPanel(signal);

    return signal;
  }

  /**
   * @param signal
   */
  public void setSignal(RandomSignal signal) {
    updateFrequencyPanel(signal);
    updateTimesPanel(signal);
  }

  private void addFrequencyPanel() {
    frequencyTextBox = new TextBox();

    Panel panel = new HorizontalPanel();

    panel.add(new Label("Frequency:"));
    panel.add(frequencyTextBox);

    add(panel);
  }

  private void updateFrequencyPanel(RandomSignal signal) {
    frequencyTextBox.setText(Integer.toString(signal.getFrequency()));
  }

  private void retrieveFrequencyPanel(RandomSignal signal) {
    signal.setFrequency(Integer.getInteger(frequencyTextBox.getText()));
  }

  private void addTimesPanel() {
    // Start time
    startTimePicker = new TimePickerFixed(null, DateTimeFormat.getFormat("aa"),
        DateTimeFormat.getFormat("hh"), DateTimeFormat.getFormat("mm"), null);

    Panel startTimePanel = new HorizontalPanel();

    startTimePanel.add(new Label("Start time:"));
    startTimePanel.add(startTimePicker);

    // End time
    endTimePicker = new TimePickerFixed(null, DateTimeFormat.getFormat("aa"),
        DateTimeFormat.getFormat("hh"), DateTimeFormat.getFormat("mm"), null);

    Panel endTimePanel = new HorizontalPanel();

    endTimePanel.add(new Label("End time:"));
    endTimePanel.add(endTimePicker);

    // Start + End time
    Panel panel = new VerticalPanel();

    panel.add(startTimePanel);
    panel.add(endTimePanel);

    add(panel);
  }

  private void updateTimesPanel(RandomSignal signal) {
    startTimePicker.setDateTime(signal.getStartTime());
    endTimePicker.setDateTime(signal.getEndTime());
  }

  private void retrieveTimesPanel(RandomSignal signal) {
    signal.setStartTime(startTimePicker.getDateTime());
    signal.setEndTime(endTimePicker.getDateTime());
  }
}
