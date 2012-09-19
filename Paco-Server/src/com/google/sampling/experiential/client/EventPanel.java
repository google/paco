package com.google.sampling.experiential.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;

import com.google.common.collect.Lists;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
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
    mainPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    initWidget(mainPanel);
    //mainPanel.setWidth("258px");
    
    renderEventData(eventDAO);
    renderResponseValues();
  }

  private void renderEventData(EventDAO eventDAO) {
    mainPanel.add(new Label("Scheduled Time: " + eventDAO.getScheduledTime()));
    mainPanel.add(new Label("Response Time: " + eventDAO.getResponseTime()));
  }

  private void renderResponseValues() {
    mainPanel.add(new Label("Responses: "));
    
    Map<String, String> whatMap = event.getWhat();
    Set<String> keys = whatMap.keySet();
    if (keys != null) {
      ArrayList<String> keysAsList = Lists.newArrayList(keys);
      Collections.sort(keysAsList);
      Collections.reverse(keysAsList);
      
      Grid grid = new Grid();
      mainPanel.add(grid);
      int row = 0;
      
      for (int i=0;i < keysAsList.size(); i++) {        
        String key = keysAsList.get(i);        
        String value = whatMap.get(key);
        InputDAO input = inputs.get(key);
        if (value == null || value.length() == 0) {
          value = "";
        } else if (input.getResponseType().equals(InputDAO.PHOTO) && !value.equals("==")) {
            value = "<img height=\"375\" src=\"data:image/jpg;base64," + value + "\">";
        } else {
          value = new SafeHtmlBuilder().appendEscaped(value).toSafeHtml().asString();
        }
        SafeHtml questionText = new SafeHtmlBuilder().appendEscaped(input.getText()).toSafeHtml();
        grid.setHTML(0, i, questionText);
        grid.setHTML(1, i, value);
      }
    }
  }

}
