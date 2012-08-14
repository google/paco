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
  ListBox typeListBox;
  FixedSignalPanel fixedPanel;
  RandomSignalPanel randomPanel;

  /**
   *
   */
  public SignalPanel() {
    super();

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
    Signal signal = null;

    switch (getType()) {
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

    typeListBox = new ListBox();
    typeListBox.addChangeHandler(this);

    for (int i = 0; i < Type.values().length; i++) {
      typeListBox.addItem(Type.values()[i].name());
    }

    panel.add(label);
    panel.add(typeListBox);
  }

  private void updateTypePanel(Signal signal) {
    typeListBox.setSelectedIndex(signal.getType().ordinal());
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

  private Type getType() {
    return Type.valueOf(typeListBox.getItemText(typeListBox.getSelectedIndex()));
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
    if (event.getSource() == typeListBox) {
      Type type = getType();

      if (type == Type.Fixed) {
        fixedPanel.setVisible(true);
      } else {
        fixedPanel.setVisible(false);
      }

      if (type == Type.Random) {
        randomPanel.setVisible(true);
      } else {
        randomPanel.setVisible(false);
      }
    }
  }
}
