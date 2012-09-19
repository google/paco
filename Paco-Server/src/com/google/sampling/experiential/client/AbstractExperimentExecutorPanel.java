package com.google.sampling.experiential.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.ExperimentDAO;
import com.google.sampling.experiential.shared.InputDAO;
import com.google.sampling.experiential.shared.MapServiceAsync;
import com.google.sampling.experiential.shared.Output;

public abstract class AbstractExperimentExecutorPanel extends Composite {

  protected VerticalPanel mainPanel;
  protected ExperimentDAO experiment;
  protected List<InputExecutorPanel> inputsPanelsList;
  protected MapServiceAsync mapService;
  protected ExperimentListener experimentListener;

  public AbstractExperimentExecutorPanel(ExperimentListener experimentListener, 
                                         ExperimentDAO experiment, MapServiceAsync mapService) {
    this.experiment = experiment;
    this.mapService = mapService;
    this.experimentListener = experimentListener;
    inputsPanelsList = new ArrayList<InputExecutorPanel>();
    createLayout();
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
    label.setStyleName("paco-HTML-Large");
    mainPanel.add(label);
  }

  private void createMainPanel() {
    mainPanel = new VerticalPanel();
    mainPanel.setSpacing(2);
    initWidget(mainPanel);
  }

  protected void renderSaveButton(HorizontalPanel buttonPanel) {
    Button saveButton = new Button("Save");
    buttonPanel.add(saveButton);
    saveButton.addClickHandler(new ClickHandler() {
      
      @Override
      public void onClick(ClickEvent event) {
        saveResponse();
      }
    });    
  }

  private void renderCancelButton(HorizontalPanel buttonPanel) {
    Button cancelButton = new Button("Cancel");
    buttonPanel.add(cancelButton);
    cancelButton.addClickHandler(new ClickHandler() {
      
      @Override
      public void onClick(ClickEvent event) {
        experimentListener.eventFired(ExperimentListener.EXPERIMENT_RESPONSE_CANCELED_CODE, experiment, true);
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
        Window.alert("Could not save your response. Please try again.\n" + caught.getMessage());        
      }
  
      @Override
      public void onSuccess(Void result) {
        Window.alert("Success!");        
        experimentListener.eventFired(ExperimentListener.EXPERIMENT_RESPONSE_CODE, experiment, true);
      }
      
    };
    mapService.saveEvent(event, asyncCallback);    
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
    return event;
  }

}