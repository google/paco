package com.google.sampling.experiential.client;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.paco.shared.model.SignalScheduleDAO;
import com.google.paco.shared.model.SignalingMechanismDAO;


public class SignalMechanismChooserListPanel extends Composite {
  
  private ExperimentDAO experiment; 
  private int signalGroupNum;
  private ExperimentCreationListener listener;
  
  private MyConstants myConstants;
  private VerticalPanel rootPanel;
  private Tree tree;
  
  private List<SignalMechanismChooserPanel> chooserPanelsWithTimeoutErrors;
  
  // Visible for testing
  protected List<SignalMechanismChooserPanel> chooserPanels; 
  
  public SignalMechanismChooserListPanel(ExperimentDAO experiment, int signalGroupNum,
                                     ExperimentCreationListener listener) {
    myConstants = GWT.create(MyConstants.class);
    this.experiment = experiment;  
    this.signalGroupNum = signalGroupNum;
    this.listener = listener;

    rootPanel = new VerticalPanel();
    initWidget(rootPanel);
    
    rootPanel.add(createSignalGroupHeader());
    rootPanel.add(createScheduleHeader());
    
    tree = new Tree();
    rootPanel.add(tree);
    
    if (signalGroupNum != 0 ||
        experiment.getSignalingMechanisms() == null || experiment.getSignalingMechanisms().length == 0) {
      SignalingMechanismDAO[] signalingMechanisms = new SignalingMechanismDAO[1];
      signalingMechanisms[0] = createEmptySignalingMechanism();
      experiment.setSignalingMechanisms(signalingMechanisms);
    }
    
    chooserPanels = new ArrayList<SignalMechanismChooserPanel>();
    for (SignalingMechanismDAO signalingMechanism : experiment.getSignalingMechanisms()) {
      addSignalPanelForExistingMechanism(signalingMechanism);
    }
    
    chooserPanelsWithTimeoutErrors = new ArrayList<SignalMechanismChooserPanel>();
  }

  private Label createScheduleHeader() {
    String titleText = myConstants.experimentSchedule();
    Label lblExperimentSchedule = new Label(titleText);
    lblExperimentSchedule.setStyleName("paco-HTML-Large");
    return lblExperimentSchedule;
  }
  
  private Label createSignalGroupHeader() {
    // Groups are numbered starting from 0, but user sees the numbering as starting from 1.
    String titleText = myConstants.signalGroup() + " " + (signalGroupNum + 1);
    Label lblExperimentSchedule = new Label(titleText);
    lblExperimentSchedule.setStyleName("paco-HTML-Large");
    return lblExperimentSchedule;
  }

  private void addSignalPanelForExistingMechanism(SignalingMechanismDAO signalingMechanism) {
    SignalMechanismChooserPanel panel = new SignalMechanismChooserPanel(signalingMechanism, this);
    chooserPanels.add(panel);
    tree.addItem(createSignalMechanismTreeItem(panel));
  }

  private TreeItem createSignalMechanismTreeItem(SignalMechanismChooserPanel panel) {
    HorizontalPanel headerPanel = createTreeHeaderPanel();
    TreeItem headerItem = new TreeItem(headerPanel);
    headerItem.addItem(panel);
    headerItem.setState(true);
    return headerItem;
  }

  private HorizontalPanel createTreeHeaderPanel() {
    HorizontalPanel headerPanel = new HorizontalPanel();
    headerPanel.add(new HTML(myConstants.schedule() + ":"));
    createListMgmtButtons(headerPanel);
    return headerPanel;
  }
  
  private void createListMgmtButtons(final HorizontalPanel headerPanel) {
    Button deleteButton = new Button("-");
    deleteButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        int chooserPanelIndex = getHeaderPanelTreeItemIndex(headerPanel);
        deleteSignalingMechanism(chooserPanelIndex);
      }
    });
    headerPanel.add(deleteButton);

    Button addButton = new Button("+");
    addButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        int chooserPanelIndex = getHeaderPanelTreeItemIndex(headerPanel);
        addSignalingMechanism(chooserPanelIndex);
      }
    });
    headerPanel.add(addButton);
  }
  
  private int getHeaderPanelTreeItemIndex(HorizontalPanel headerPanel) {
    int index = tree.getItemCount() - 1;
    for (int i = 0; i < tree.getItemCount(); ++i) {
      if (tree.getItem(i).getWidget().equals(headerPanel)) {
        index = i;
        break;
      }
    }
    return index;
  }
  
  protected void updateExperimentSignalingMechanism(SignalMechanismChooserPanel panel, 
                                          SignalingMechanismDAO signalingMechanism) {
    int index = chooserPanels.indexOf(panel);
    experiment.getSignalingMechanisms()[index] = signalingMechanism;
  }
  
  protected void addSignalingMechanism(int senderIndex) {
    SignalMechanismChooserPanel newChooserPanel = 
        new SignalMechanismChooserPanel(createEmptySignalingMechanism(), this);
    chooserPanels.add(senderIndex + 1, newChooserPanel);   
    updateExperimentSignalingMechanisms();
    addChooserPanelToTree(newChooserPanel, senderIndex + 1);
  }
  
  public void deleteSignalingMechanism(int index) {
    if (chooserPanels.size() == 1) {
      return;
    }
    
    chooserPanels.remove(index);
    updateExperimentSignalingMechanisms();
    
    tree.removeItem(tree.getItem(index));
  }
  
  // TODO this is not very efficient.
  private void updateExperimentSignalingMechanisms() {
    SignalingMechanismDAO[] newSignalingMechanisms = new SignalingMechanismDAO[chooserPanels.size()];
    for (int i = 0; i < chooserPanels.size(); i++) {
      newSignalingMechanisms[i] = chooserPanels.get(i).getSignalingMechanism();
    }
    experiment.setSignalingMechanisms(newSignalingMechanisms);
  }
  
  private void addChooserPanelToTree(SignalMechanismChooserPanel panel, int widgetIndex) { 
    tree.insertItem(widgetIndex, createSignalMechanismTreeItem(panel));
  }
  
  private SignalingMechanismDAO createEmptySignalingMechanism() {
    return new SignalScheduleDAO();
  }
  
  protected void removeTimeoutErrorMessage(SignalMechanismChooserPanel panel, String message) {
    chooserPanelsWithTimeoutErrors.remove(panel);
    if (chooserPanelsWithTimeoutErrors.isEmpty()) {
      fireExperimentCode(ExperimentCreationListener.REMOVE_ERROR, message);
    }
  }
  
  protected void addTimeoutErrorMessage(SignalMechanismChooserPanel panel, String message) {
    if (chooserPanelsWithTimeoutErrors.isEmpty()) {
      fireExperimentCode(ExperimentCreationListener.ADD_ERROR, message);
    }
    if (!chooserPanelsWithTimeoutErrors.contains(panel)) {
      chooserPanelsWithTimeoutErrors.add(panel);
    }
  }
  
  public void fireExperimentCode(int code, String message) {
    listener.eventFired(code, signalGroupNum, message);
  }

}
