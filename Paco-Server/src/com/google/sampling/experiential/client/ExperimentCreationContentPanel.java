package com.google.sampling.experiential.client;

import java.util.List;

import com.google.common.base.Preconditions;
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
  
  public static final int NO_EXTRA_BUTTON = 0;
  public static final int ADD_SIGNAL_GROUP_BUTTON = 1;
  public static final int ADD_CREATE_EXPERIMENT_BUTTON = 2;

  private MyConstants myConstants;
  private ExperimentCreationListener listener;

  private VerticalPanel mainPanel;
  private HorizontalPanel buttonPanel;
  private Widget showingView;
  
  private Widget addNewSignalGroupButton;
  private Widget createExperimentButton;

  public ExperimentCreationContentPanel(ExperimentCreationListener listener,
                                        List<Composite> showingViews) {
    myConstants = GWT.create(MyConstants.class);
    mainPanel = new VerticalPanel();
    initWidget(mainPanel);
    this.listener = listener;
    addShowingViews(showingViews);
    createButtonPanel();
  }
  
  public void changeShowingView(Composite view, int buttonPanelId) {
    // The view should be a child of this panel and should not be
    // the button panel.
    if (view.getParent() != null && view.getParent().equals(mainPanel)
        && !view.equals(buttonPanel)) {
      showingView.setVisible(false);
      showingView = view;
      showingView.setVisible(true);
      configureButtonPanel(buttonPanelId);
    }
  }
  
  private void configureButtonPanel(int buttonPanelId) {
    resetButtonPanel();
    switch (buttonPanelId) {
    case NO_EXTRA_BUTTON:
      break;
    case ADD_SIGNAL_GROUP_BUTTON:
      buttonPanel.add(addNewSignalGroupButton);
      break;
    case ADD_CREATE_EXPERIMENT_BUTTON:
      buttonPanel.add(createExperimentButton);
      break;
    default:
      break;
    }
  }
  
  private void resetButtonPanel() {
    // Button count should only ever be 1 or 2.
    if (buttonPanel.getWidgetCount() == 2) {
      buttonPanel.remove(1);
    }
  }
  
  // TODO: this is inefficient.
  public void addContentView(Composite view) {
    mainPanel.remove(buttonPanel);
    mainPanel.add(view);
    view.setVisible(false);
    mainPanel.add(buttonPanel);
  }

  private void addShowingViews(List<Composite> showingViews) {
    // By default, the first widget added is visible.
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
    buttonPanel = new HorizontalPanel();
    buttonPanel.add(createNextButton());
    addNewSignalGroupButton = createNewSignalGroupButton();
    createExperimentButton = createCreateExperimentButton();
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
  
  private Widget createNewSignalGroupButton() {
    Button newSignalGroupButton = new Button(myConstants.newSignalGroupButtonText());
    newSignalGroupButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        fireExperimentCreationCode(ExperimentCreationListener.NEW_SIGNAL_GROUP);
      }
    });
    return newSignalGroupButton;
  }
  
  private Widget createCreateExperimentButton() {
    Button createExperimentButton = new Button(myConstants.saveExperiment());
    createExperimentButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        fireExperimentCreationCode(ExperimentCreationListener.SAVE_EXPERIMENT);
      }
    });
    return createExperimentButton;
  }

  private void fireExperimentCreationCode(int code) {
    listener.eventFired(code, null, null);
  }

}
