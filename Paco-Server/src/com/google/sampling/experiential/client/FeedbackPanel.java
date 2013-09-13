package com.google.sampling.experiential.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.DisclosurePanelImages;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.paco.shared.model.FeedbackDAO;
import com.google.paco.shared.model.SignalGroupDAO;

public class FeedbackPanel extends Composite {

  private SignalGroupDAO signalGroupDAO;
  private int signalGroupNum;
  private ExperimentCreationListener listener;

  private MyConstants myConstants;
  private VerticalPanel mainPanel;
  private Tree tree;

  private DisclosurePanel customFeedbackPanel;
  protected CheckBox customFeedbackCheckBox;
  protected TextArea customFeedbackText;

  public FeedbackPanel(SignalGroupDAO signalGroup, int groupNum, ExperimentCreationPanel experimentCreationPanel) {
    myConstants = GWT.create(MyConstants.class);
    this.signalGroupDAO = signalGroup;
    this.signalGroupNum = signalGroupNum;
    this.listener = listener;

    mainPanel = new VerticalPanel();
    initWidget(mainPanel);

    createFeedbackEntryPanel();


    tree = new Tree();
    mainPanel.add(tree);

    if (signalGroupNum != 0 ||
        signalGroup.getFeedback() == null || signalGroup.getFeedback().length == 0) {
      FeedbackDAO[] signalingMechanisms = new FeedbackDAO[1];
      signalingMechanisms[0] = new FeedbackDAO();
      signalGroup.setFeedback(signalingMechanisms);
    }

  }

  private Widget createFeedbackEntryPanel() {
    // checkbox for default or custom feedback "[] Create Custom Feedback Page"
    // if custom selected then fill with feedback from experiment in TextArea
    VerticalPanel outer = new VerticalPanel();
    mainPanel.add(outer);

    outer.add(createSignalGroupHeader());
    outer.add(createTitle());

    HorizontalPanel feedbackPanel = new HorizontalPanel();

    customFeedbackCheckBox = new CheckBox();
    customFeedbackCheckBox.setValue(signalGroupDAO.getFeedback() != null && signalGroupDAO.getFeedback().length > 0
        && !defaultFeedback(signalGroupDAO.getFeedback()[0]));
    customFeedbackCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        setFeedbackOn(signalGroupDAO);
      }
    });
    feedbackPanel.add(customFeedbackCheckBox);

    Label feedbackLabel = new Label(myConstants.customFeedback());
    feedbackPanel.add(feedbackLabel);
    outer.add(feedbackPanel);

    createCustomFeedbackDisclosurePanel(signalGroupDAO);
    outer.add(customFeedbackPanel);

    setFeedbackOn(signalGroupDAO); // Set initial feedback value for experiment.
    return feedbackPanel;
  }

  private Widget createTitle() {
    HTML questionsPrompt = new HTML("<h2>" + myConstants.feedbackHeader() + "</h2>");
    questionsPrompt.setStyleName("keyLabel");
    return questionsPrompt;
  }

  private Label createSignalGroupHeader() {
    // Groups are numbered starting from 0, but user sees the numbering as starting from 1.
    String titleText = myConstants.signalGroup() + " " + (signalGroupNum + 1);
    Label lblExperimentSchedule = new Label(titleText);
    lblExperimentSchedule.setStyleName("paco-HTML-Large");
    return lblExperimentSchedule;
  }


  private void createCustomFeedbackDisclosurePanel(final SignalGroupDAO signalGroup) {
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

    customFeedbackPanel.setHeader(openHeaderWidget);
    customFeedbackPanel.setOpen(true);
    customFeedbackPanel.addOpenHandler(new OpenHandler<DisclosurePanel>() {
      @Override
      public void onOpen(OpenEvent<DisclosurePanel> event) {
        customFeedbackPanel.setHeader(openHeaderWidget);
      }
    });
    customFeedbackPanel.addCloseHandler(new CloseHandler<DisclosurePanel>() {
      @Override
      public void onClose(CloseEvent<DisclosurePanel> event) {
        customFeedbackPanel.setHeader(closedHeaderWidget);
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
        setFeedbackOn(signalGroup);
      }
    });

    FeedbackDAO[] feedbacks = signalGroup.getFeedback();

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

  private void setFeedbackOn(SignalGroupDAO signalGroup) {
    if (!customFeedbackCheckBox.getValue()) {
      signalGroup.setFeedback(new FeedbackDAO[] { new FeedbackDAO(null, FeedbackDAO.DISPLAY_FEEBACK_TYPE,
                                                                 FeedbackDAO.DEFAULT_FEEDBACK_MSG) });
    } else {
      signalGroup.setFeedback(new FeedbackDAO[] { new FeedbackDAO(null, FeedbackDAO.DISPLAY_FEEBACK_TYPE,
                                                                 customFeedbackText.getText()) });
    }
  }






}
