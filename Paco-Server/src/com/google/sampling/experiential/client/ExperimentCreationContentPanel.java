package com.google.sampling.experiential.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ExperimentCreationContentPanel extends Composite {
  
  VerticalPanel mainPanel;
  
  public ExperimentCreationContentPanel() {
    mainPanel = new VerticalPanel();
    initWidget(mainPanel);
  }
  
  public void setShowingView(Widget view) {
    mainPanel.clear();
    mainPanel.add(view);
  }

}
