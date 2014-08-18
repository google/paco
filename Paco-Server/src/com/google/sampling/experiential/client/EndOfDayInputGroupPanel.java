package com.google.sampling.experiential.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.Output;

public class EndOfDayInputGroupPanel extends Composite {

  public static final String UNANSWERED_EVENT_KEY = "unansweredEvent";

  private EventDAO event;
  private VerticalPanel mainPanel;
  private List<InputExecutorPanel> inputsPanelsList;

  public EndOfDayInputGroupPanel(EventDAO event) {
    this.event = event;
    inputsPanelsList = new ArrayList<InputExecutorPanel>();
    createLayout();
  }

  private void createLayout() {
    mainPanel = new VerticalPanel();
    mainPanel.setSpacing(2);
    initWidget(mainPanel);    
  }

  public void add(EndOfDayInputExecutorPanel inputsPanel) {
    inputsPanelsList.add(inputsPanel);    
    mainPanel.add(inputsPanel);    
  }

  /**
   * builds the responses from the input panels for the event that this group represents.
   * If all the responses values are empty, then this is considered an unanswered event, which
   * then adds a key item into the outputs signifying such. This is useful for ignoring these responses altogether.
   * @return
   */
  public Map<String, String> getOutputs() {
    boolean unansweredEvent = true;
    Map<String, String> outputs = Maps.newHashMap();
    List<InputExecutorPanel> inputPanelList = inputsPanelsList;
    for (InputExecutorPanel inputPanel : inputPanelList) {
      Output output = inputPanel.getValue();      
      String value = output.getValue();
      if (value != null && !value.isEmpty()) {
        unansweredEvent = false;
      }
      outputs.put(output.getName(), value);
    }
    if (unansweredEvent) {
      outputs.put(UNANSWERED_EVENT_KEY, "1");
    }
    return outputs;
  }

}
