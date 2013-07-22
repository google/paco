package com.google.sampling.experiential.client;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosureEvent;
import com.google.gwt.user.client.ui.DisclosureHandler;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.DisclosurePanelImages;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.paco.shared.model.FeedbackDAO;

public class ExperimentCreationPublishingPanel extends Composite {

  private ExperimentDAO experiment;
  private MyConstants myConstants;

  private VerticalPanel mainPanel;
  private DisclosurePanel customFeedbackPanel;
  private DisclosurePanel publishedUsersPanel;
  
  // Visible for testing
  protected CheckBox customFeedbackCheckBox;
  protected TextArea customFeedbackText;
  protected CheckBox publishCheckBox;
  protected TextArea publishedUserList;

  public ExperimentCreationPublishingPanel(ExperimentDAO experiment) {
    myConstants = GWT.create(MyConstants.class);
    this.experiment = experiment;

    mainPanel = new VerticalPanel();
    initWidget(mainPanel);
    createPublishingHeader();
    createFeedbackEntryPanel();
    createPublishingPanel();
  }
  
  private void createPublishingHeader() {
    String titleText = myConstants.experimentPublishing();
    Label lblExperimentPublishing = new Label(titleText);
    lblExperimentPublishing.setStyleName("paco-HTML-Large");
    mainPanel.add(lblExperimentPublishing);
  }

  private Widget createFeedbackEntryPanel() {
    // checkbox for default or custom feedback "[] Create Custom Feedback Page"
    // if custom selected then fill with feedback from experiment in TextArea
    HorizontalPanel feedbackPanel = new HorizontalPanel();
    customFeedbackCheckBox = new CheckBox();
    customFeedbackCheckBox.setValue(experiment.getFeedback() != null && experiment.getFeedback().length > 0
        && !defaultFeedback(experiment.getFeedback()[0]));
    customFeedbackCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        setFeedbackOn(experiment);
      }
    });
    feedbackPanel.add(customFeedbackCheckBox);
    Label feedbackLabel = new Label(myConstants.customFeedback());
    feedbackPanel.add(feedbackLabel);
    mainPanel.add(feedbackPanel);

    createCustomFeedbackDisclosurePanel();
    mainPanel.add(customFeedbackPanel);
    return feedbackPanel;
  }

  private void createCustomFeedbackDisclosurePanel() {
    customFeedbackPanel = new DisclosurePanel();

    final DisclosurePanelHeader closedHeaderWidget = new DisclosurePanelHeader(
                                                                               false,
                                                                               "<b>"
                                                                                   + myConstants.clickToEditCustomFeedback()
                                                                                   + "</b>");
    final DisclosurePanelHeader openHeaderWidget = new DisclosurePanelHeader(
                                                                             true,
                                                                             "<b>"
                                                                                 + myConstants.clickToCloseCustomFeedbackEditor()
                                                                                 + "</b>");

    customFeedbackPanel.setHeader(closedHeaderWidget);
    customFeedbackPanel.addEventHandler(new DisclosureHandler() {
      public void onClose(DisclosureEvent event) {
        customFeedbackPanel.setHeader(closedHeaderWidget);
      }

      public void onOpen(DisclosureEvent event) {
        customFeedbackPanel.setHeader(openHeaderWidget);
      }
    });

    VerticalPanel userContentPanel = new VerticalPanel();
    Label instructionLabel = new Label(myConstants.customFeedbackInstructions());
    userContentPanel.add(instructionLabel);

    customFeedbackText = new TextArea();
    customFeedbackText.setCharacterWidth(100);
    customFeedbackText.setHeight("100");
    customFeedbackText.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        setFeedbackOn(experiment);
      }
    });

    FeedbackDAO[] feedbacks = experiment.getFeedback();

    if (feedbacks != null && feedbacks.length > 0 && !defaultFeedback(feedbacks[0])) {
      customFeedbackText.setText(feedbacks[0].getText());
    }

    userContentPanel.add(customFeedbackText);
    customFeedbackPanel.setContent(userContentPanel);
  }

  private boolean defaultFeedback(FeedbackDAO feedbackDAO) {
    return feedbackDAO.getFeedbackType().equals(FeedbackDAO.DISPLAY_FEEBACK_TYPE)
        && feedbackDAO.getText().equals(FeedbackDAO.DEFAULT_FEEDBACK_MSG);
  }

  final DisclosurePanelImages images = (DisclosurePanelImages) GWT.create(DisclosurePanelImages.class);

  private class DisclosurePanelHeader extends HorizontalPanel {
    public DisclosurePanelHeader(boolean isOpen, String html) {
      add(isOpen ? images.disclosurePanelOpen().createImage() : images.disclosurePanelClosed().createImage());
      add(new HTML(html));
    }
  }

  private void createPublishingPanel() {
    HorizontalPanel publishingPanel = new HorizontalPanel();
    publishCheckBox = new CheckBox();
    publishCheckBox.setValue(experiment.getPublished());
//    publishCheckBox.addClickHandler(new ClickHandler() {
//      public void onClick(ClickEvent event) {
//        setIsPublishedOn(experiment);
//      }
//    });
    publishCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        setIsPublishedOn(experiment);   
      }
    });
    publishingPanel.add(publishCheckBox);
    Label publishLabel = new Label(myConstants.published());
    publishingPanel.add(publishLabel);
    mainPanel.add(publishingPanel);

    createPublishedUsersDisclosurePanel(experiment);
    mainPanel.add(publishedUsersPanel);
  }

  private void createPublishedUsersDisclosurePanel(ExperimentDAO experiment2) {
    publishedUsersPanel = new DisclosurePanel();

    final DisclosurePanelHeader closedHeaderWidget = new DisclosurePanelHeader(false,
                                                                               "<b>"
                                                                                   + myConstants.clickToEditPublished()
                                                                                   + "</b>");
    final DisclosurePanelHeader openHeaderWidget = new DisclosurePanelHeader(
                                                                             true,
                                                                             "<b>"
                                                                                 + myConstants.clickToClosePublishedEditor()
                                                                                 + "</b>");

    publishedUsersPanel.setHeader(closedHeaderWidget);
    publishedUsersPanel.addEventHandler(new DisclosureHandler() {

      public void onClose(DisclosureEvent event) {
        publishedUsersPanel.setHeader(closedHeaderWidget);
      }

      public void onOpen(DisclosureEvent event) {
        publishedUsersPanel.setHeader(openHeaderWidget);
      }
    });

    VerticalPanel userContentPanel = new VerticalPanel();
    Label instructionLabel = new Label(myConstants.publishedEditorPrompt());
    userContentPanel.add(instructionLabel);

    publishedUserList = new TextArea();
    publishedUserList.setCharacterWidth(100);
    publishedUserList.setHeight("100");
    publishedUserList.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        setPublishedUsersOn(experiment);
      }
    });

    String[] usersStrArray = experiment.getPublishedUsers();
    List<String> userEmails = Lists.newArrayList(usersStrArray);
    publishedUserList.setText(toCSVString(userEmails));

    userContentPanel.add(publishedUserList);
    publishedUsersPanel.setContent(userContentPanel);
  }

  private void setIsPublishedOn(ExperimentDAO experiment) {
    experiment.setPublished(publishCheckBox.getValue());
  }

  private void setPublishedUsersOn(ExperimentDAO experiment) {
    List<String> userEmails = new ArrayList<String>();
    String userListText = publishedUserList.getText();
    if (userListText.length() > 0) {
      Splitter sp = Splitter.on(",").trimResults().omitEmptyStrings();
      for (String userEmail : sp.split(userListText)) {
        userEmails.add(userEmail);
      }
    }
    String[] userEmailsStrArray = new String[userEmails.size()];
    userEmailsStrArray = userEmails.toArray(userEmailsStrArray);
    experiment.setPublishedUsers(userEmailsStrArray);
  }

  private void setFeedbackOn(ExperimentDAO experiment) {
    if (!customFeedbackCheckBox.getValue()) {
      experiment.setFeedback(new FeedbackDAO[] { new FeedbackDAO(null, FeedbackDAO.DISPLAY_FEEBACK_TYPE,
                                                                 FeedbackDAO.DEFAULT_FEEDBACK_MSG) });
    } else {
      experiment.setFeedback(new FeedbackDAO[] { new FeedbackDAO(null, FeedbackDAO.DISPLAY_FEEBACK_TYPE,
                                                                 customFeedbackText.getText()) });
    }
  }

  private String toCSVString(List<String> list) {
    StringBuilder buf = new StringBuilder();
    boolean first = true;
    for (String item : list) {
      if (first) {
        first = false;
      } else {
        buf.append(", ");
      }
      buf.append(item.toLowerCase());
    }
    return buf.toString();
  }

}
