package com.google.sampling.experiential.client;

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

  private VerticalPanel mainPanel;

  private Tree menuTree;
  private TreeItem showDescription;
  private TreeItem inputGroupsRootTree;
  private TreeItem showPublishing;
  
  private ExperimentCreationListener listener;

  public ExperimentCreationMenuBar(ExperimentCreationListener listener) {
    super();
    mainPanel = new VerticalPanel();
    initWidget(mainPanel);
    initMenu();
    this.listener = listener;
  }

  private void initMenu() {
    createMenuHeader();
    menuTree = new Tree();

    // Main menu roots.
    showDescription = new TreeItem("Experiment Description");
    inputGroupsRootTree = new TreeItem("Input Groups");
    showPublishing = new TreeItem("Experiment Finishing");

    // Create first input group and add to input groups root tree.
    // Unfold the tree by default.
    createInputGroup();
    inputGroupsRootTree.setState(true);

    // Add main menu headers to menu tree.
    menuTree.addItem(showDescription);
    menuTree.addItem(inputGroupsRootTree);
    menuTree.addItem(showPublishing);

    menuTree.addSelectionHandler(new SelectionHandler<TreeItem>() {
      @Override
      public void onSelection(SelectionEvent<TreeItem> event) {
        TreeItem selectedButton = event.getSelectedItem();
        if (selectedButton.equals(showDescription)) {
          fireExperimentCreationCode(ExperimentCreationListener.SHOW_DESCRIPTION_CODE);
        } else if (selectedButton.equals(inputGroupsRootTree.getChild(0).getChild(0))) {
          fireExperimentCreationCode(ExperimentCreationListener.SHOW_SCHEDULE_CODE);
        } else if (selectedButton.equals(inputGroupsRootTree.getChild(0).getChild(1))) {
          fireExperimentCreationCode(ExperimentCreationListener.SHOW_INPUTS_CODE);
        } else if (selectedButton.equals(showPublishing)) {
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
        menuTree.setSelectedItem(showDescription, false);
        break;
      case SCHEDULE_PANEL:
        menuTree.setSelectedItem(inputGroupsRootTree.getChild(0).getChild(0), false);
        break;
      case INPUTS_PANEL:
        menuTree.setSelectedItem(inputGroupsRootTree.getChild(0).getChild(1), false);
        break;
      case PUBLISHING_PANEL:
        menuTree.setSelectedItem(showPublishing, false);
        break;
      default:
        System.err.println("Unhandled code sent to experiment listener.");
    }
  }

  private void createMenuHeader() {
    Label labelMessage = new Label();
    labelMessage.setSize("200", "30");
    labelMessage.setText("Experiment Creation\n");
    mainPanel.add(labelMessage);
  }

  private void createInputGroup() {
    int groupNum = inputGroupsRootTree.getChildCount() + 1;
    TreeItem inputGroup = new TreeItem("Input Group " + groupNum);
    TreeItem showSchedule = new TreeItem("Experiment Schedule");
    TreeItem showInputs = new TreeItem("Experiment Inputs");
    inputGroup.addItem(showSchedule);
    inputGroup.addItem(showInputs);
    inputGroup.setState(true);
    inputGroupsRootTree.addItem(inputGroup);
  }
  
  private void fireExperimentCreationCode(int code) {
    listener.eventFired(code, null, null);
  }
}
