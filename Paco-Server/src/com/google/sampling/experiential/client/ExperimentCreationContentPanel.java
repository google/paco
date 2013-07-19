package com.google.sampling.experiential.client;

import com.google.common.base.Preconditions;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.paco.shared.model.ExperimentDAO;

public class ExperimentCreationContentPanel extends Composite {
  
  private MyConstants myConstants;
  
  private VerticalPanel mainPanel;
  
  private ExperimentCreationListener listener;
  
  private ExperimentDAO experiment;
  
  public ExperimentCreationContentPanel(ExperimentDAO experiment, ExperimentCreationListener listener) {
    myConstants = GWT.create(MyConstants.class);
    mainPanel = new VerticalPanel();
    initWidget(mainPanel);
    this.listener = listener;
    this.experiment = experiment;
    createButtonPanel();
  }
  
  public void changeShowingView(Widget view) {
    Preconditions.checkArgument(mainPanel.getWidgetCount() <= 2);
    if (mainPanel.getWidgetCount() == 2) {
      mainPanel.remove(0);
    }
    mainPanel.insert(view, 0);
  }
  
  private void createButtonPanel() {
    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.add(createNextButton());
    mainPanel.add(buttonPanel);
  }

  private void fireExperimentCreationCode(int code) {
    listener.eventFired(code, experiment, null);
  }
  
  private Widget createNextButton() {

    Button nextButton = new Button("Next"); // PRIYA
    nextButton.addClickListener(new ClickListener() {

      @Override
      public void onClick(Widget sender) {
         fireExperimentCreationCode(ExperimentCreationListener.NEXT);
      }
    });

    return nextButton;
  }

}
