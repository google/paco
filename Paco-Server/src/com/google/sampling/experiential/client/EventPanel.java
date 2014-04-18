package com.google.sampling.experiential.client;

import java.util.Map;
import java.util.Set;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.paco.shared.model.InputDAO;
import com.google.sampling.experiential.shared.EventDAO;

public class EventPanel extends Composite {

  private AbstractExperimentExecutorPanel parent;
  private EventDAO event;
  private VerticalPanel mainPanel;
  private InputDAO[] inputs;
  protected MyConstants myConstants;
  protected MyMessages myMessages;


  public EventPanel(AbstractExperimentExecutorPanel parent, EventDAO eventDAO, InputDAO[] inputDAOs) {
    myConstants = GWT.create(MyConstants.class);
    myMessages = GWT.create(MyMessages.class);

    this.parent = parent;
    this.event = eventDAO;
    this.inputs = inputDAOs;

    mainPanel = new VerticalPanel();
    mainPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    mainPanel.setStyleName("paco-offset-background");
    initWidget(mainPanel);

    renderEventTimes(eventDAO);
    renderResponseValues();
  }

  private void renderEventTimes(EventDAO eventDAO) {
    createScheduleTimePanel(eventDAO);
    createResponseTimePanel(eventDAO);
  }

  private void createResponseTimePanel(EventDAO eventDAO) {
    HorizontalPanel rtPanel = new HorizontalPanel();
    mainPanel.add(rtPanel);

    Label responseTimeLabel = new Label(myConstants.responseTime() + ": ");
    responseTimeLabel.setStyleName("keyLabel");
    rtPanel.add(responseTimeLabel);
    Label responseTimeValueLabel = new Label(eventDAO.getResponseTime() != null ? eventDAO.getResponseTime().toString() : null);
    rtPanel.add(responseTimeValueLabel);
  }

  private void createScheduleTimePanel(EventDAO eventDAO) {
    HorizontalPanel stPanel = new HorizontalPanel();
    mainPanel.add(stPanel);

    Label scheduledTimeLabel = new Label(myConstants.scheduledTime() + ": ");
    scheduledTimeLabel.setStyleName("keyLabel");
    stPanel.add(scheduledTimeLabel);
    Label timeLabel = new Label(eventDAO.getScheduledTime() != null ? eventDAO.getScheduledTime().toString() : "");
    stPanel.add(timeLabel);
  }

  private void renderResponseValues() {
    HorizontalPanel panel = new HorizontalPanel();
    mainPanel.add(panel);
    Label responseLabel = new Label(myConstants.responses() + ": ");
    responseLabel.setStyleName("keyLabel");
    panel.add(responseLabel);

    Map<String, String> whatMap = event.getWhat();
    Set<String> keys = whatMap.keySet();
    if (keys == null) {
      return;
    }

    Grid grid = new Grid(inputs.length * 2, 1);
    //grid.setBorderWidth(1);
    grid.setWidth("100%");
    mainPanel.add(grid);

    int rowIndex = 0;
    for (int i=0;i < inputs.length; i++) {
      InputDAO input = inputs[i];
      String valueString = whatMap.get(input.getName());
      Widget value = null;

      String displayText = input.getText();
      if (Strings.isNullOrEmpty(displayText)) {
        displayText = input.getName();
      }


      if (input == null || valueString == null || valueString.length() == 0) {
        value = new Label("");
      } else if (input.getResponseType().equals("photo"/*InputDAO.PHOTO*/) && !valueString.equals("==") && !valueString.isEmpty() && event.getBlobs().length > 0 ) {
          String blobData = event.getBlobs()[0];
          if (blobData.isEmpty()) {
            value = new Label("");
          } else {
            // the inline image would get destroyed by wrapping a label and 
            // there is no gwt html element for an image that allows setting the data attribute
            // so, if we have an image, we directly add it to the html and return here.
            // TODO get rid of this kludge
            String imgValue = "<img height=\"375\" src=\"data:image/jpg;base64," + blobData + "\">";
            addColumnToGrid(grid, rowIndex, displayText, imgValue);
            rowIndex = rowIndex + 2;
            return;
          }
      } else if (input.getResponseType().equals(InputDAO.LIST)) {
        String[] listChoices = input.getListChoices();
        if (input.getMultiselect() != null && input.getMultiselect()) {
          StringBuffer buff = new StringBuffer();
          boolean first = true;
          for (String currentChoice : Splitter.on(',').split(valueString)) {
            if (first) {
              first = false;
            } else {
              buff.append(",");
            }

            String answerString = getListChoiceForAnswer(currentChoice, listChoices);
            buff.append(answerString);
          }
          value = new Label(buff.toString());
        } else {
          value = new Label(getListChoiceForAnswer(valueString, listChoices));
        }
      } else if (input.getResponseType().equals(InputDAO.LOCATION)) {
        value = new ChartPanel(input, Lists.newArrayList(event), 300, 300, false);
      } else {
        if (valueString.equals("blob") && input.getResponseType().equals("photo")) {
          value = new Label("");
        }
        value = new HTML(new SafeHtmlBuilder().appendEscaped(valueString).toSafeHtml().asString());
      }
      addColumnToGrid(grid, rowIndex, displayText, value);
      rowIndex = rowIndex + 2;
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

  private void addColumnToGrid(Grid grid, int i, String text, Widget widget) {
    SafeHtml questionText = new SafeHtmlBuilder().appendEscaped(text).toSafeHtml();
    //Label label = new Label(questionText.asString());
    Label label = new Label(text);
    label.setStyleName("keyLabel");

    grid.setWidget(i, 0, label);
    grid.setWidget(i + 1, 0, widget);
  }
  
  private void addColumnToGrid(Grid grid, int i, String text, String value) {
    SafeHtml questionText = new SafeHtmlBuilder().appendEscaped(text).toSafeHtml();
    //Label label = new Label(questionText.asString());
    Label label = new Label(text);
    label.setStyleName("keyLabel");
    grid.setWidget(i, 0, label);
    grid.setHTML(i + 1, 0, value);
  }


}
