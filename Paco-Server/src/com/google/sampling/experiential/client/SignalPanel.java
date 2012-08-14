// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.client;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.sampling.experiential.shared.FixedSignal;
import com.google.sampling.experiential.shared.RandomSignal;
import com.google.sampling.experiential.shared.Signal;
import com.google.sampling.experiential.shared.Signal.Type;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class SignalPanel extends VerticalPanel implements ChangeHandler {
  ListBox typesListBox;
  FixedSignalPanel fixedPanel;
  RandomSignalPanel randomPanel;

  /**
   *
   */
  public SignalPanel() {
    addTypePanel();
    addFixedPanel();
    addRandomPanel();
  }

  /**
   * @param signal the signal
   */
  public void setSignal(Signal signal) {
    updateTypePanel(signal);
    updateFixedPanel(signal);
    updateRandomPanel(signal);
  }

  /**
   * @return the signal
   */
  public Signal getSignal() {
    Type type = Type.valueOf(typesListBox.getItemText(typesListBox
        .getSelectedIndex()));

    Signal signal = null;

    switch (type) {
    case Fixed:
      signal = fixedPanel.getSignal();
      break;
    case Random:
      signal = randomPanel.getSignal();
      break;
    }

    return signal;
  }

  private void addTypePanel() {
    Panel panel = new HorizontalPanel();
    Label label = new Label("Schedule: ");

    typesListBox = new ListBox();
    typesListBox.addChangeHandler(this);
    typesListBox.addItem("Fixed");
    typesListBox.addItem("Random");

    panel.add(label);
    panel.add(typesListBox);
  }

  private void updateTypePanel(Signal signal) {
    typesListBox.setSelectedIndex(signal.getType().ordinal());
  }

  private void addFixedPanel() {
    fixedPanel = new FixedSignalPanel();

    add(fixedPanel);
  }

  private void updateFixedPanel(Signal signal) {
    if (signal.getType().equals(Type.Fixed) == false) {
      return;
    }

    fixedPanel.setSignal((FixedSignal) signal);
  }

  private void addRandomPanel() {
    randomPanel = new RandomSignalPanel();

    add(randomPanel);
  }

  private void updateRandomPanel(Signal signal) {
    if (signal.getType().equals(Type.Random) == false) {
      return;
    }

    randomPanel.setSignal((RandomSignal) signal);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.google.gwt.event.dom.client.ChangeHandler#onChange(com.google.gwt.event
   * .dom.client.ChangeEvent)
   */
  @Override
  public void onChange(ChangeEvent event) {
    if (event.getSource() == typesListBox) {
      if (typesListBox.getSelectedIndex() == 0) {
        fixedPanel.setVisible(true);
      } else {
        fixedPanel.setVisible(false);
      }

      if (typesListBox.getSelectedIndex() == 1) {
        randomPanel.setVisible(true);
      } else {
        randomPanel.setVisible(false);
      }
    }
  }
}
