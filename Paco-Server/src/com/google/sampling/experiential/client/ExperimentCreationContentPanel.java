package com.google.sampling.experiential.client;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.paco.shared.model.ExperimentDAO;

public class ExperimentCreationContentPanel extends Composite {

  private MyConstants myConstants;
  private ExperimentCreationListener listener;
  private ExperimentDAO experiment;

  private VerticalPanel mainPanel;
  private int buttonPanelIndex;
  private Widget showingView;

  public ExperimentCreationContentPanel(ExperimentDAO experiment, ExperimentCreationListener listener,
                                        List<Composite> showingViews) {
    myConstants = GWT.create(MyConstants.class);
    mainPanel = new VerticalPanel();
    initWidget(mainPanel);
    this.listener = listener;
    this.experiment = experiment;
    addShowingViews(showingViews);
    buttonPanelIndex = showingViews.size();
    createButtonPanel();
  }

  public void changeShowingView(Widget view) {
    // The button should not be toggleable.
    if (!view.equals(mainPanel.getWidget(buttonPanelIndex))) {
      showingView.setVisible(false);
      showingView = view;
      showingView.setVisible(true);
    }
  }

  private void addShowingViews(List<Composite> showingViews) {
    // By default, only first widget is visible.
    boolean isFirstWidget = true;
    for (Widget widget : showingViews) {
      mainPanel.add(widget);
      widget.setVisible(isFirstWidget);
      if (isFirstWidget) {
        isFirstWidget = false;
        showingView = widget;
      }
    }
  }

  private void createButtonPanel() {
    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.add(createNextButton());
    mainPanel.add(buttonPanel);
  }

  private Widget createNextButton() {
    Button nextButton = new Button(myConstants.next());
    nextButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        fireExperimentCreationCode(ExperimentCreationListener.NEXT);
      }
    });
    return nextButton;
  }

  private void fireExperimentCreationCode(int code) {
    listener.eventFired(code, experiment, null);
  }

}
