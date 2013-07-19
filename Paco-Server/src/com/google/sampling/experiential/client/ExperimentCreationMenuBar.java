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
  private TreeItem inputGroupsRootTree;
  private TreeItem showPublishingItem;

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
    showDescriptionItem = new TreeItem(myConstants.experimentScheduleButtonText());
    inputGroupsRootTree = new TreeItem(myConstants.experimentInputGroupsHeaderText());
    showPublishingItem = new TreeItem(myConstants.experimentPublishingButtonText());

    createInputGroup();
    inputGroupsRootTree.setState(true); // Unfold the tree by default.

    // Add main menu headers to menu tree.
    menuTree.addItem(showDescriptionItem);
    menuTree.addItem(inputGroupsRootTree);
    menuTree.addItem(showPublishingItem);

    menuTree.addSelectionHandler(new SelectionHandler<TreeItem>() {
      @Override
      public void onSelection(SelectionEvent<TreeItem> event) {
        TreeItem selectedButton = event.getSelectedItem();
        if (selectedButton.equals(showDescriptionItem)) {
          fireExperimentCreationCode(ExperimentCreationListener.SHOW_DESCRIPTION_CODE);
        } else if (selectedButton.equals(getShowScheduleItem())) {
          fireExperimentCreationCode(ExperimentCreationListener.SHOW_SCHEDULE_CODE);
        } else if (selectedButton.equals(getShowInputsItem())) {
          fireExperimentCreationCode(ExperimentCreationListener.SHOW_INPUTS_CODE);
        } else if (selectedButton.equals(showPublishingItem)) {
          fireExperimentCreationCode(ExperimentCreationListener.SHOW_PUBLISHING_CODE);
        }
      }
    });

    mainPanel.add(menuTree);
  }

  public void addInputGroup() {
    createInputGroup();
  }

  public void deleteInputGroup(int groupNum) {
    TreeItem toRemove = inputGroupsRootTree.getChild(groupNum - 1);
    inputGroupsRootTree.removeItem(toRemove);
  }

  public void setSelectedItem(int itemNum) {
    switch (itemNum) {
    case DESCRIPTION_PANEL:
      menuTree.setSelectedItem(showDescriptionItem, false);
      break;
    case SCHEDULE_PANEL:
      menuTree.setSelectedItem(inputGroupsRootTree.getChild(0).getChild(0), false);
      break;
    case INPUTS_PANEL:
      menuTree.setSelectedItem(inputGroupsRootTree.getChild(0).getChild(1), false);
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

  private void createInputGroup() {
    int groupNum = inputGroupsRootTree.getChildCount() + 1;
    TreeItem inputGroup = new TreeItem(myConstants.experimentSingleInputGroupHeaderText() + " " + groupNum);
    TreeItem showSchedule = new TreeItem(myConstants.experimentScheduleButtonText());
    TreeItem showInputs = new TreeItem(myConstants.experimentInputsButtonText());
    inputGroup.addItem(showSchedule);
    inputGroup.addItem(showInputs);
    inputGroup.setState(true); // Input group is open by default.
    inputGroupsRootTree.addItem(inputGroup);
  }

  private TreeItem getShowInputsItem() {
    return inputGroupsRootTree.getChild(0).getChild(1);
  }

  private TreeItem getShowScheduleItem() {
    return inputGroupsRootTree.getChild(0).getChild(0);
  }

  private void fireExperimentCreationCode(int code) {
    listener.eventFired(code, null, null);
  }
}
