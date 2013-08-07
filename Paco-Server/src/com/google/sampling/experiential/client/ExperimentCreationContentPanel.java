package com.google.sampling.experiential.client;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
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
  private ScrollPanel scrollPanel;
  private VerticalPanel scrollInner;
  private Widget showingView;
  
  private Widget addNewSignalGroupButton;
  private Widget createExperimentButton;
  
  private int numStandardButtons;

  public ExperimentCreationContentPanel(ExperimentCreationListener listener,
                                        List<Composite> showingViews) {
    myConstants = GWT.create(MyConstants.class);
    mainPanel = new VerticalPanel();
    initWidget(mainPanel);
    mainPanel.setSpacing(10);
    this.listener = listener;
    createButtonPanel();
    
    scrollInner = new VerticalPanel();
    scrollPanel = new ScrollPanel(scrollInner);
    scrollPanel.setSize("900px", "600px");
    mainPanel.add(scrollPanel);
    addShowingViews(showingViews);
  }
  
  public void changeShowingView(Composite view, int buttonPanelId) {
    // The view should be a child of this panel and should not be
    // the button panel.
    if (view.getParent() != null && view.getParent().equals(scrollInner)
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
    // Button count should only ever be one larger than standard.
    if (buttonPanel.getWidgetCount() == numStandardButtons + 1) {
      buttonPanel.remove(numStandardButtons);
    }
  }
  
  public void addContentView(Composite view) {
    scrollInner.add(view);
    view.setVisible(false);
  }

  private void addShowingViews(List<Composite> showingViews) {
    // By default, the first widget added is visible.
    boolean isFirstWidget = true;
    for (Widget widget : showingViews) {
      scrollInner.add(widget);
      widget.setVisible(isFirstWidget);
      if (isFirstWidget) {
        isFirstWidget = false;
        showingView = widget;
      }
    }
  }

  private void createButtonPanel() {
    buttonPanel = new HorizontalPanel();
    numStandardButtons = 0;
    addStandardButton(createPreviousButton());
    addStandardButton(createNextButton());
    addNewSignalGroupButton = createNewSignalGroupButton();
    createExperimentButton = createCreateExperimentButton();
    mainPanel.add(buttonPanel);
  }
  
  private void addStandardButton(Widget button) {
    buttonPanel.add(button);
    ++numStandardButtons;
  }
  
  private Widget createPreviousButton() {
    Button previousButton = new Button(myConstants.previous());
    previousButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        fireExperimentCreationCode(ExperimentCreationListener.PREVIOUS);
      }
    });
    return previousButton;
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
