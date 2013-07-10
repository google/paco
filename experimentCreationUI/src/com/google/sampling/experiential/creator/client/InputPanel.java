package com.google.sampling.experiential.creator.client;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimpleCheckBox;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.TextBox;

public class InputPanel extends TabLayoutPanel {

  public InputPanel(double barHeight, Unit barUnit) {
    super(barHeight, barUnit);
    createPanel();
  }
  
  private void createPanel() {
    FlowPanel flowPanel = new FlowPanel();
    add(flowPanel, "Flow Panel", false);
    
    AbsolutePanel absolutePanel = new AbsolutePanel();
    add(absolutePanel, "Absolute Panel", false);
    
    ListBox listBox = new ListBox();
    absolutePanel.add(listBox, 10, 41);
    listBox.setVisibleItemCount(1);
    listBox.addItem("likert smileys");
    listBox.addItem("likert");
    listBox.addItem("open text");
    listBox.addItem("list");
    listBox.addItem("number");
    listBox.addItem("location");
    listBox.addItem("photo");
    listBox.setSelectedIndex(1);
    listBox.setSize("20%", "15%");
    
    TextBox textBox = new TextBox();
    absolutePanel.add(textBox, 123, 41);
    textBox.setSize("68%", "8%");
    
    SimpleCheckBox simpleCheckBox = new SimpleCheckBox();
    absolutePanel.add(simpleCheckBox, 493, 46);
    
    HTMLPanel panel = new HTMLPanel("HTML Panel");
    add(panel, "HTML Panel", false);
  }

}
