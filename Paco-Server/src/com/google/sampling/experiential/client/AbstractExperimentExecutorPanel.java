package com.google.sampling.experiential.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.Output;
import com.google.sampling.experiential.shared.PacoServiceAsync;

public abstract class AbstractExperimentExecutorPanel extends Composite {

  protected VerticalPanel mainPanel;
  protected ExperimentDAO experiment;
  protected List<InputExecutorPanel> inputsPanelsList;
  protected PacoServiceAsync pacoService;
  protected ExperimentListener experimentListener;
  protected MyConstants myConstants;
  protected MyMessages myMessages;



  public AbstractExperimentExecutorPanel(ExperimentListener experimentListener,
                                         ExperimentDAO experiment, PacoServiceAsync pacoService) {
    myConstants = GWT.create(MyConstants.class);
    myMessages = GWT.create(MyMessages.class);

    this.experiment = experiment;
    this.pacoService = pacoService;
    this.experimentListener = experimentListener;
    inputsPanelsList = new ArrayList<InputExecutorPanel>();
    //createLayout();
  }

  protected void createLayout() {
    createMainPanel();
    createExperimentHeader();
    renderInputItems();
    HorizontalPanel buttonPanel = new HorizontalPanel();
    mainPanel.add(buttonPanel);
    renderSaveButton(buttonPanel);
    renderCancelButton(buttonPanel);
  }

  protected abstract void renderInputItems();

  protected void createExperimentHeader() {
    Label label = new Label(experiment.getTitle());
    label.setStyleName("paco-HTML-Larger");
    mainPanel.add(label);
  }

  protected void createMainPanel() {
    mainPanel = new VerticalPanel();
    mainPanel.setSpacing(2);
    initWidget(mainPanel);
  }

  protected void renderSaveButton(HorizontalPanel buttonPanel) {
    Button saveButton = new Button(myConstants.save());
    buttonPanel.add(saveButton);
    saveButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        saveResponse();
      }
    });
  }

  protected void renderCancelButton(HorizontalPanel buttonPanel) {
    Button cancelButton = new Button(myConstants.cancel());
    buttonPanel.add(cancelButton);
    cancelButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        experimentListener.eventFired(ExperimentListener.EXPERIMENT_RESPONSE_CANCELED_CODE, experiment, true, false);
      }
    });
  }

  protected void saveResponse() {
    EventDAO event = createEvent();
    addOutputsToEvent(event);
    postEventToServer(event);
  }

  protected void postEventToServer(EventDAO event) {
    AsyncCallback<Void> asyncCallback = new AsyncCallback<Void>(){

      @Override
      public void onFailure(Throwable caught) {
        Window.alert(myMessages.saveFailed(caught.getMessage()));
      }

      @Override
      public void onSuccess(Void result) {
        Window.alert(myConstants.success());
        experimentListener.eventFired(ExperimentListener.EXPERIMENT_RESPONSE_CODE, experiment, true, false);
      }

    };

    pacoService.saveEvent(event, asyncCallback);
  }

  protected void addOutputsToEvent(EventDAO event) {
    Map<String, String> outputs = new HashMap<String, String>();
    for (int i=0; i < inputsPanelsList.size(); i++) {
      InputExecutorPanel inputPanel = inputsPanelsList.get(i);
      Output output = inputPanel.getValue();
      outputs.put(output.getName(), output.getValue());
    }
    event.setWhat(outputs);
  }

  protected EventDAO createEvent() {
    EventDAO event = new EventDAO();
    event.setResponseTime(new Date());
    event.setExperimentName(experiment.getTitle());
    event.setExperimentId(experiment.getId());
    event.setExperimentVersion(experiment.getVersion());
    event.setTimezone(DateTimeFormat.getFormat("ZZZ").format(new Date()));//TimeZone.getDefault().getID());
    return event;
  }

}