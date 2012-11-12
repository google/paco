package com.google.sampling.experiential.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.InputDAO;

public class EventPanel extends Composite {

  private AbstractExperimentExecutorPanel parent;
  private EventDAO event;
  private VerticalPanel mainPanel;
  private Map<String, InputDAO> inputs;

  public EventPanel(AbstractExperimentExecutorPanel parent, EventDAO eventDAO, Map<String, InputDAO> inputsByName) {
    this.parent = parent;
    this.event = eventDAO;
    this.inputs = inputsByName;
    
    mainPanel = new VerticalPanel();
    mainPanel.setSpacing(2);
    mainPanel.setBorderWidth(1);
    mainPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    initWidget(mainPanel);
    //mainPanel.setWidth("258px");
    
    renderEventData(eventDAO);
    renderResponseValues();
  }

  private void renderEventData(EventDAO eventDAO) {
    HorizontalPanel stPanel = new HorizontalPanel();
    mainPanel.add(stPanel);
    
    Label scheduledTimeLabel = new Label("Scheduled Time: ");
    scheduledTimeLabel.setStyleName("keyLabel");
    stPanel.add(scheduledTimeLabel);
    Label timeLabel = new Label(eventDAO.getScheduledTime() != null ? eventDAO.getScheduledTime().toString() : "");
    stPanel.add(timeLabel);
    
    HorizontalPanel rtPanel = new HorizontalPanel();
    mainPanel.add(rtPanel);

    Label responseTimeLabel = new Label("Response Time: ");
    responseTimeLabel.setStyleName("keyLabel");
    rtPanel.add(responseTimeLabel);
    Label responseTimeValueLabel = new Label(eventDAO.getResponseTime() != null ? eventDAO.getResponseTime().toString() : null);
    rtPanel.add(responseTimeValueLabel);
  }

  private void renderResponseValues() {
    Label responseLabel = new Label("Responses: ");
    responseLabel.setStyleName("keyLabel");
    mainPanel.add(responseLabel);
    
    Map<String, String> whatMap = event.getWhat();
    Set<String> keys = whatMap.keySet();
    if (keys != null) {
      ArrayList<String> keysAsList = Lists.newArrayList(keys);
      Collections.sort(keysAsList);
      Collections.reverse(keysAsList);
      
      Grid grid = new Grid(keysAsList.size(), 2);
      //grid.setBorderWidth(1);
      mainPanel.add(grid);
      
      for (int i=0;i < keysAsList.size(); i++) {        
        String key = keysAsList.get(i);        
        String value = whatMap.get(key);
        InputDAO input = inputs.get(key);
        if (input == null) {
          addColumnToGrid(grid, i, "", key);
        } else if (value == null || value.length() == 0) {
          value = "";
          addColumnToGrid(grid, i, value, input.getText());
        } else if (input.getResponseType().equals("photo"/*InputDAO.PHOTO*/) && 
            !value.equals("==") &&
            !value.isEmpty() && event.getBlobs().length > 0 ) {            
            String blobData = event.getBlobs()[0];
            if (blobData.isEmpty()) {
              value = "";
            } else {
              value = "<img height=\"375\" src=\"data:image/jpg;base64," + blobData + "\">";
            }
            addColumnToGrid(grid, i, value, input.getName());
        } else if (input.getResponseType().equals(InputDAO.LIST)) {
          String[] listChoices = input.getListChoices();
          if (input.getMultiselect() != null && input.getMultiselect()) {
            StringBuffer buff = new StringBuffer(); 
            boolean first = true;
            for (String currentChoice : Splitter.on(',').split(value)) {
              if (first) {
                first = false;
              } else {
                buff.append(",");
              }
              
              String answerString = getListChoiceForAnswer(currentChoice, listChoices);
              buff.append(answerString);
            }
            value = buff.toString();
          } else {
            value = getListChoiceForAnswer(value, listChoices);
          }
          addColumnToGrid(grid, i, value, input.getText());
        } else {
          if (value.equals("blob") && input.getResponseType().equals("photo")) {
            value = "";
          }
          value = new SafeHtmlBuilder().appendEscaped(value).toSafeHtml().asString();
          addColumnToGrid(grid, i, value, input.getText());
        }
        
      }
    }
  }

  private String getListChoiceForAnswer(String value, String[] listChocies) {
    int zeroBasedIndex = -1; 
    try {
      zeroBasedIndex = Integer.parseInt(value) - 1;
    } catch (NumberFormatException nfe) {
      // Log this error.
    }
    if (zeroBasedIndex < 0 || zeroBasedIndex > listChocies.length - 1) {
      value = ""; 
    } else {               
      value = listChocies[zeroBasedIndex]; 
    }
    return value;
  }

  private void addColumnToGrid(Grid grid, int i, String value, String text) {
    SafeHtml questionText = new SafeHtmlBuilder().appendEscaped(text).toSafeHtml();
    Label label = new Label(questionText.asString());
    label.setStyleName("keyLabel");
    grid.setWidget(i, 0, label);
    grid.setHTML(i, 1, value);
  }

}
