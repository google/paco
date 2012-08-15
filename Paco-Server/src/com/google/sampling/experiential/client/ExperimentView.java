// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import com.google.sampling.experiential.shared.Experiment;
import com.google.sampling.experiential.shared.Input;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class ExperimentView extends Composite implements HasValue<Experiment> {
  private static ExperimentUiBinder uiBinder = GWT.create(ExperimentUiBinder.class);

  interface ExperimentUiBinder extends UiBinder<Widget, ExperimentView> {
  }

  @UiField
  TextBox title;
  @UiField
  TextArea description;
  @UiField
  TextArea consentForm;
  @UiField
  SignalScheduleView signalSchedule;
  @UiField
  FlowPanel inputs;
  @UiField
  TextArea feedback;
  @UiField
  CheckBox published;

  public ExperimentView() {
    initWidget(uiBinder.createAndBindUi(this));

    inputs.add(new InputView(inputs));
  }

  public ExperimentView(Experiment experiment) {
    this();

    setValue(experiment);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google
   * .gwt.event.logical.shared.ValueChangeHandler)
   */
  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Experiment> handler) {
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.gwt.user.client.ui.HasValue#getValue()
   */
  @Override
  public Experiment getValue() {
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
   */
  @Override
  public void setValue(Experiment experiment) {
    setValue(experiment, false);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
   */
  @Override
  public void setValue(Experiment experiment, boolean fireEvents) {
    title.setValue(experiment.getTitle());
    description.setValue(experiment.getDescription());
    consentForm.setValue(experiment.getConsentForm());
    signalSchedule.setValue(experiment.getSignalSchedule());
    feedback.setValue(experiment.getFeedback());

    for (Input input : experiment.getInputs()) {
      inputs.add(new InputView(inputs, input));
    }
  }
}
