/*
* Copyright 2011 Google Inc. All Rights Reserved.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance  with the License.  
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package com.google.sampling.experiential.client;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;

import com.google.common.collect.Maps;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.LoginInfo;
import com.google.sampling.experiential.shared.PacoServiceAsync;
import com.google.sampling.experiential.shared.TimeUtil;

/**
 * Component for holding an individual chart for an Input's responses.
 * 
 * @author Bob Evans
 *
 */
public class ExperimentLandingPanel extends Composite {

  private static final Class<String> DEFAULT_DATA_CLASS = String.class;

  private DateTimeFormat formatter = DateTimeFormat.getFormat(TimeUtil.DATETIME_FORMAT);

  private VerticalPanel mainPanel;

  private JoinExperimentModule container;

  private FlexTable buttonTable;

  private VerticalPanel contentPanel;
  private PacoServiceAsync mapService;

  protected ExperimentDAO experiment;

  private Label statusLabel;

  private MyConstants myConstants;

  private MyMessages myMessages;

  private LoginInfo loginInfo;

  public ExperimentLandingPanel(JoinExperimentModule joinExperimentModule) {
    this.container = joinExperimentModule;
    loginInfo = container.loginInfo;
    myConstants = GWT.create(MyConstants.class);
    myMessages = GWT.create(MyMessages.class);
    contentPanel = new VerticalPanel();
    mainPanel = contentPanel;
    mainPanel.setSpacing(2);
    statusLabel = new Label("");
    mainPanel.add(statusLabel);
    initWidget(mainPanel);

    Label titleLabel = new Label("Welcome to the Daily + End of Day Study Landing Page");
    titleLabel.setStyleName("paco-HTML");
    mainPanel.add(titleLabel);
    mainPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    mainPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
    
    buttonTable = new FlexTable();
    mainPanel.add(buttonTable);
    mainPanel.add(contentPanel);
    
    Button join = new Button("Join Study");   
    Button leave = new Button("Leave Study");
    Button respond = new Button("Respond to End of Day Study");
    
    buttonTable.add(join);
    buttonTable.add(respond);
    buttonTable.add(leave);

    loadExperiment();
    
    join.addClickHandler(new ClickHandler() {
      
      @Override
      public void onClick(ClickEvent event) {
        contentPanel.clear();
        contentPanel.add(new JoinPanel());
      }
    });
    
    respond.addClickHandler(new ClickHandler() {
      
      @Override
      public void onClick(ClickEvent event) {
        contentPanel.clear();
        showReferredExperimentExecutor(experiment);
      }
    });
    
    leave.addClickHandler(new ClickHandler() {
      
      @Override
      public void onClick(ClickEvent event) {
        contentPanel.clear();
        contentPanel.add(new LeavePanel());
      }
    });
  }

  protected void showReferredExperimentExecutor(final ExperimentDAO experiment) {
    statusLabel.setVisible(true);
    
    final ExperimentDAO referencedExperiment = null;
    
    AsyncCallback<List<EventDAO>> callback = new AsyncCallback<List<EventDAO>>() {

      @Override
      public void onFailure(Throwable caught) {
        Window.alert("Could not retrieve events from referenced experiment.<br/>" + caught.getMessage());
        statusLabel.setVisible(false);

      }

      @Override
      public void onSuccess(List<EventDAO> eventList) {
        if (eventList.size() == 0) {
          Window.alert("No events found for referencing.");
          statusLabel.setVisible(false);
          return;
        }
        
        ExperimentDAO referredExperiment = null;
        ExperimentListener experimentListener = new ExperimentListener() {
          @Override
          public void eventFired(int experimentCode, ExperimentDAO experiment, boolean joined, boolean findView) {
            String scheduledTime = formatTime(new Date());
            Map<String, String> kvPairs = Maps.newHashMap();
            container.getMapService().saveEvent(container.loginInfo.getEmailAddress(), 
                                 scheduledTime, 
                                 formatTime(new Date()), 
                                 experiment.getId().toString(), 
                                 kvPairs, 
                                 experiment.getVersion(),
                                 false, 
                                 new AsyncCallback<Void>() {

                                    @Override
                                    public void onFailure(Throwable caught) {
                                      Window.alert("Could not save responses.");
                                    }
  
                                    @Override
                                    public void onSuccess(Void result) {
                                      Window.alert(myConstants.success());
                                    }
              
                                 });
          }
          
        };
        AbstractExperimentExecutorPanel ep = new EndOfDayExperimentExecutorPanel(experimentListener, mapService, 
                                                                                 experiment, eventList, null, referencedExperiment);
        contentPanel.add(ep);
        statusLabel.setVisible(false);
      }
    };
    String queryText = "experimentId=" + referencedExperiment.getId() + ":who=" + loginInfo.getEmailAddress();
    mapService.eventSearch(queryText, callback);
    
  }

  private void loadExperiment() {
//    mapService.getExperimentsForUser(new AsyncCallback<List<ExperimentDAO>>() {
//
//      
//      @Override
//      public void onFailure(Throwable caught) {
//        Window.alert("Could not load Experiment");        
//      }
//
//      @Override
//      public void onSuccess(List<ExperimentDAO> result) {
//        for (ExperimentDAO experimentDAO : result) {
//          if (experimentDAO.getTitle().equals("Study")) {
//            ExperimentLandingPanel.this.experiment = experimentDAO;
//            break;
//          } else {
//            Window.alert("Could not load experiment");
//          }
//        }
//        
//      }
//    });
    
  }


  private String formatTime(Date time) {
    if (time == null) {
      return "";
    }
    return formatter.format(time);
  }


  
}
