/*
* Copyright 2011 Google Inc. All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance  with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package com.google.sampling.experiential.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosureEvent;
import com.google.gwt.user.client.ui.DisclosureHandler;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.DisclosurePanelImages;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.paco.shared.model.FeedbackDAO;

import edu.ycp.cs.dh.acegwt.client.ace.AceEditor;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorCallback;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorMode;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorTheme;

/**
 * Container for all scheduling configuration panels.
 *
 * @author Bob Evans
 *
 */
public class FeedbackChooserPanel extends Composite {

  private static final String SHORT_TEXTBOX_WIDTH = "100%";

  private static final int MAXIMUM_SHORT_TEXT_LENGTH = 500;

  private ExperimentDAO experiment;

  private MyConstants myConstants;
  private VerticalPanel rootPanel;
  private HorizontalPanel choicePanel;
  private VerticalPanel detailsPanel;

  private AceEditor customFeedbackEditor;

  final DisclosurePanelImages images = (DisclosurePanelImages) GWT.create(DisclosurePanelImages.class);

  class DisclosurePanelHeader extends HorizontalPanel {
    public DisclosurePanelHeader(boolean isOpen, String html) {
      add(isOpen ? images.disclosurePanelOpen().createImage()
                 : images.disclosurePanelClosed().createImage());
      add(new HTML(html));
    }
  }


  public FeedbackChooserPanel(ExperimentDAO experiment) {
    this.experiment = experiment;
    myConstants = GWT.create(MyConstants.class);

    rootPanel = new VerticalPanel();
    rootPanel.setStyleName("bordered");
    rootPanel.setWidth(SHORT_TEXTBOX_WIDTH);
    initWidget(rootPanel);

    choicePanel = new HorizontalPanel();
    rootPanel.add(choicePanel);

    detailsPanel = new VerticalPanel();
    detailsPanel.setWidth(SHORT_TEXTBOX_WIDTH);
    rootPanel.add(detailsPanel);

    HTML feedbackChoiceLabel = new HTML("<h2>" + myConstants.feedbackChoiceLabel() + ": </h2>");
    choicePanel.add(feedbackChoiceLabel);
    final ListBox feedbackChoices = new ListBox();
    feedbackChoices.addItem(myConstants.staticMessageFeedbackChoice());
    feedbackChoices.addItem(myConstants.retrospectiveMessageFeedbackChoice());
    feedbackChoices.addItem(myConstants.responsiveMessageFeedbackChoice());
    feedbackChoices.addItem(myConstants.customFeedbackChoice());
    feedbackChoices.addItem(myConstants.hideFeedbackChoice());

    choicePanel.add(feedbackChoices);

    if (experiment.getFeedback() == null || experiment.getFeedback().length == 0) {
      FeedbackDAO[] feedback = new FeedbackDAO[1];
      FeedbackDAO feedbackDAO = new FeedbackDAO();
      feedbackDAO.setText(FeedbackDAO.DEFAULT_FEEDBACK_MSG);

      feedback[0] = feedbackDAO;
      experiment.setFeedback(feedback);
      experiment.setFeedbackType(FeedbackDAO.FEEDBACK_TYPE_STATIC_MESSAGE);
      feedbackChoices.setItemSelected(0, true);
    } else {
      Integer feedbackType = experiment.getFeedbackType();
      int selectedIndex = 0;
      if (feedbackType == null) {
        // no existing experiments will have a feedback type unless we write a migration script
        // so, it is either default feedback or custom feedback;
        // assign appropriately
        if (hasNonDefaultFeedback()) {
          selectedIndex = FeedbackDAO.FEEDBACK_TYPE_CUSTOM; // we were retrospective by default
          experiment.setFeedbackType(selectedIndex);
        } else {
          selectedIndex = FeedbackDAO.FEEDBACK_TYPE_STATIC_MESSAGE;
          experiment.setFeedbackType(selectedIndex);
        }
      } else {
        selectedIndex = feedbackType;
      }
      feedbackChoices.setItemSelected(selectedIndex, true);
    }

    updatePanel();

    feedbackChoices.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        int index = feedbackChoices.getSelectedIndex();
        respondToListSelection(index);
      }

    });
  }

  private void respondToListSelection(int index) {
    experiment.setFeedbackType(index);
    switch (index) {
    case FeedbackDAO.FEEDBACK_TYPE_STATIC_MESSAGE:
      experiment.getFeedback()[0].setText(FeedbackDAO.DEFAULT_FEEDBACK_MSG);
      break;
    default:
      experiment.getFeedback()[0].setText("");
      break;
    }

    updatePanel();
  }

  private void updatePanel() {
    detailsPanel.clear();
    switch (experiment.getFeedbackType()) {
      case FeedbackDAO.FEEDBACK_TYPE_STATIC_MESSAGE:
        detailsPanel.add(createStaticMessagePanel());
        break;
      case FeedbackDAO.FEEDBACK_TYPE_RETROSPECTIVE:
        break;
      case FeedbackDAO.FEEDBACK_TYPE_RESPONSIVE:
        detailsPanel.add(createResponsiveFeedbackDisclosurePanel());
        break;
      case FeedbackDAO.FEEDBACK_TYPE_CUSTOM:
        detailsPanel.add(createCustomFeedbackDisclosurePanel());
        break;
      case FeedbackDAO.FEEDBACK_TYPE_HIDE_FEEDBACK:
        break;
      default:
        break;
    }
  }

  private boolean hasNonDefaultFeedback() {
    return oldMethodBasedOnNonDefaultFeedbackText();
  }

  private boolean oldMethodBasedOnNonDefaultFeedbackText() {
    return experiment.getFeedback() != null &&
        experiment.getFeedback().length > 0 &&
        !defaultFeedback(experiment.getFeedback()[0]);
  }

  /**
   * @return
   */
  private Widget createCustomFeedbackDisclosurePanel() {
    detailsPanel.add(makeIOSIncompatibleMessage()); // TODO make a container panel or just get rid of this when iOS is ready

    final DisclosurePanel customFeedbackPanel = new DisclosurePanel();

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

    if (experiment.getFeedback()[0].getId() == null) {
      customFeedbackPanel.setHeader(openHeaderWidget);
      customFeedbackPanel.setOpen(true);
    } else {
      customFeedbackPanel.setHeader(closedHeaderWidget);
      customFeedbackPanel.setOpen(false);
    }
    customFeedbackPanel.addEventHandler(new DisclosureHandler() {
      public void onClose(DisclosureEvent event) {
        boolean currentlyVisible = customFeedbackPanel.getHeader().isVisible();
        customFeedbackPanel.setHeader(closedHeaderWidget);
        closedHeaderWidget.setVisible(currentlyVisible);
      }

      public void onOpen(DisclosureEvent event) {
        boolean currentlyVisible = customFeedbackPanel.getHeader().isVisible();
        customFeedbackPanel.setHeader(openHeaderWidget);
        openHeaderWidget.setVisible(currentlyVisible);
      }
    });

    VerticalPanel userContentPanel = new VerticalPanel();
    Label instructionLabel = new Label(myConstants.customFeedbackInstructions());
    userContentPanel.add(instructionLabel);

    customFeedbackEditor = new AceEditor();
    customFeedbackEditor.setWidth("800px");
    customFeedbackEditor.setHeight("600px");
    customFeedbackEditor.startEditor();
    customFeedbackEditor.setMode(AceEditorMode.JAVASCRIPT);
    customFeedbackEditor.setTheme(AceEditorTheme.ECLIPSE);


    FeedbackDAO[] feedbacks = experiment.getFeedback();

    if (feedbacks != null && feedbacks.length > 0 && !defaultFeedback(feedbacks[0])) {
      customFeedbackEditor.setText(feedbacks[0].getText());
    } else {
      customFeedbackEditor.setText(generateDefaultCustomCode());
    }

    userContentPanel.add(customFeedbackEditor);
    customFeedbackPanel.setContent(userContentPanel);
    customFeedbackEditor.addOnChangeHandler(new AceEditorCallback() {

      @Override
      public void invokeAceCallback(JavaScriptObject obj) {
        experiment.getFeedback()[0].setText(customFeedbackEditor.getText());
      }

    });
    return customFeedbackPanel;
  }

  private String generateDefaultCustomCode() {
    // TODO generate a set of javascript to render the current experiment items.
    // TODO this generated javascript will need to be updated if the items are changed.
    return "";
  }

  /**
   * @param feedbackDAO
   * @return
   */
  private boolean defaultFeedback(FeedbackDAO feedbackDAO) {
    return FeedbackDAO.DEFAULT_FEEDBACK_MSG.equals(feedbackDAO.getText());
  }


  private Widget createResponsiveFeedbackDisclosurePanel() {
    return new Label("Not implemented yet!");
  }

  private Widget createStaticMessagePanel() {
    VerticalPanel container = new VerticalPanel();
    container.setWidth(SHORT_TEXTBOX_WIDTH);
    container.add(makeIOSIncompatibleStaticMessage());

    final TextBox textBox = new TextBox();
    textBox.setWidth(SHORT_TEXTBOX_WIDTH);
    textBox.setMaxLength(MAXIMUM_SHORT_TEXT_LENGTH);
    String feedbackText = experiment.getFeedback()[0].getText();
    textBox.setText(feedbackText);
    textBox.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        experiment.getFeedback()[0].setText(textBox.getText());
      }
    });
    container.add(textBox);
    return container;
  }

  private HTML makeIOSIncompatibleMessage() {
    HTML html = new HTML("&nbsp;&nbsp;&nbsp;<font color=\"red\" size=\"smaller\"><i>(" + myConstants.iOSIncompatible() + ")</i></font>");
    return html;
  }

  /**
   * This is temporary until the first version of iOS custom feedback is implemented.
   * @return
   */
  private HTML makeIOSIncompatibleStaticMessage() {
    HTML html = new HTML("&nbsp;&nbsp;&nbsp;<font color=\"red\" size=\"smaller\"><i>(" + myConstants.iOSIncompatibleStatic() + ")</i></font>");
    return html;
  }


}
