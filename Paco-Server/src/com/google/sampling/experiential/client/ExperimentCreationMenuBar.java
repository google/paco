package com.google.sampling.experiential.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ExperimentCreationMenuBar extends Composite {

  public static final int DESCRIPTION_PANEL = 0;
  public static final int SCHEDULE_PANEL = 1;
  public static final int INPUTS_PANEL = 2;
  public static final int PUBLISHING_PANEL = 3;

  private MyConstants myConstants;

  private VerticalPanel mainPanel;
  private Tree menuTree;
  private TreeItem showDescriptionItem;
  private TreeItem signalGroupsRootTree;
  private TreeItem showPublishingItem;
  
  private int numSignalGroups;

  private ExperimentCreationListener listener;

  public ExperimentCreationMenuBar(ExperimentCreationListener listener) {
    super();
    myConstants = GWT.create(MyConstants.class);
    mainPanel = new VerticalPanel();
    initWidget(mainPanel);
    initMenu();
    this.listener = listener;
  }

  private void initMenu() {
    createMenuHeader();
    menuTree = new Tree();

    // Main menu roots.
    showDescriptionItem = new TreeItem(myConstants.experimentDescriptionButtonText());
    signalGroupsRootTree = new TreeItem(myConstants.experimentSignalGroupsHeaderText());
    showPublishingItem = new TreeItem(myConstants.experimentPublishingButtonText());

    numSignalGroups = 0;
    createAddSignalGroupButton();
    createSignalGroup();
    signalGroupsRootTree.setState(true); // Unfold the tree by default.

    // Add main menu headers to menu tree.
    menuTree.addItem(showDescriptionItem);
    menuTree.addItem(signalGroupsRootTree);
    menuTree.addItem(showPublishingItem);
    
    createMainMenuTreeSelectionHandler();
    mainPanel.add(menuTree);
  }

  private void createMainMenuTreeSelectionHandler() {
    // Navigation callbacks.
    menuTree.addSelectionHandler(new SelectionHandler<TreeItem>() {
      @Override
      public void onSelection(SelectionEvent<TreeItem> event) {
        TreeItem selectedButton = event.getSelectedItem();
        if (selectedButton.equals(showDescriptionItem)) {
          fireExperimentCreationCode(ExperimentCreationListener.SHOW_DESCRIPTION_CODE, 0);
        } else if (selectedButton.equals(showPublishingItem)) {
          fireExperimentCreationCode(ExperimentCreationListener.SHOW_PUBLISHING_CODE, 0);
        } else if (selectedItemIsSpecificSignalGroupHeader(selectedButton)){
          int signalGroupNum = signalGroupsRootTree.getChildIndex(selectedButton);
          if (selectedItemIsAddNewSignalGroup(signalGroupNum)) {
            fireExperimentCreationCode(ExperimentCreationListener.NEW_SIGNAL_GROUP, null);
          } else {
            fireExperimentCreationCode(ExperimentCreationListener.SHOW_SCHEDULE_CODE, signalGroupNum);
          }
        } else {
          int signalGroupNum = signalGroupsRootTree.getChildIndex(selectedButton.getParentItem());
          int viewWithinSignalGroupNum = selectedButton.getParentItem().getChildIndex(selectedButton);
          if (selectedItemIsShowSchedule(viewWithinSignalGroupNum)) {
            fireExperimentCreationCode(ExperimentCreationListener.SHOW_SCHEDULE_CODE, signalGroupNum);
          } else {
            fireExperimentCreationCode(ExperimentCreationListener.SHOW_INPUTS_CODE, signalGroupNum);
          }
        }
      }
    });
  }

  public void addSignalGroup() {
    createSignalGroup();
  }

  public void deleteSignalGroup(int groupNum) {
    TreeItem toRemove = signalGroupsRootTree.getChild(groupNum - 1);
    signalGroupsRootTree.removeItem(toRemove);
  }

  public void setSelectedItem(int itemType, Integer groupNum) {
    switch (itemType) {
    case DESCRIPTION_PANEL:
      menuTree.setSelectedItem(showDescriptionItem, false);
      break;
    case SCHEDULE_PANEL:
      menuTree.setSelectedItem(getShowScheduleItem(groupNum), false);
      break;
    case INPUTS_PANEL:
      menuTree.setSelectedItem(getShowInputsItem(groupNum), false);
      break;
    case PUBLISHING_PANEL:
      menuTree.setSelectedItem(showPublishingItem, false);
      break;
    default:
      System.err.println("Unhandled code sent to experiment creation menu.");
    }
  }

  private void createMenuHeader() {
    Label labelMessage = new Label();
    labelMessage.setSize("200", "30");
    labelMessage.setText(myConstants.experimentCreation());
    mainPanel.add(labelMessage);
  }

  private void createAddSignalGroupButton() {
    TreeItem addSignalGroup = new TreeItem(myConstants.newSignalGroupButtonText());
    signalGroupsRootTree.addItem(addSignalGroup);
  }
  
  private void createSignalGroup() {
    int groupNum = signalGroupsRootTree.getChildCount() - 1;
    TreeItem signalGroup = new TreeItem(myConstants.experimentSingleSignalGroupHeaderText() + " " + groupNum);
    TreeItem showSchedule = new TreeItem(myConstants.experimentScheduleButtonText());
    TreeItem showInputs = new TreeItem(myConstants.experimentInputsButtonText());
    signalGroup.addItem(showSchedule);
    signalGroup.addItem(showInputs);
    signalGroup.setState(true); // Input group is open by default.
    signalGroupsRootTree.insertItem(groupNum, signalGroup); // Insert in front of "Add Signal Group" item.
    ++numSignalGroups;
  }
  
  private TreeItem getSignalGroupHeaderItem(int groupNum) {
    return signalGroupsRootTree.getChild(groupNum);
  }
  
  private TreeItem getShowScheduleItem(int groupNum) {
    return getSignalGroupHeaderItem(groupNum).getChild(0);
  }

  private TreeItem getShowInputsItem(int groupNum) {
    return getSignalGroupHeaderItem(groupNum).getChild(1);
  }
  
  private boolean selectedItemIsShowSchedule(int viewWithinSignalGroupNum) {
    return viewWithinSignalGroupNum == 0;
  }

  private boolean selectedItemIsAddNewSignalGroup(int signalGroupNum) {
    return signalGroupNum == numSignalGroups;
  }

  private boolean selectedItemIsSpecificSignalGroupHeader(TreeItem selectedButton) {
    return selectedButton.getParentItem().equals(signalGroupsRootTree);
  }

  private void fireExperimentCreationCode(int code, Integer groupNum) {
    listener.eventFired(code, null, groupNum);
  }
}
