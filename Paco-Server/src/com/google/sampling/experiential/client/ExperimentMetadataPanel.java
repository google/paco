package com.google.sampling.experiential.client;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosureEvent;
import com.google.gwt.user.client.ui.DisclosureHandler;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.DisclosurePanelImages;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.sampling.experiential.client.ExperimentDefinitionPanel.DisclosurePanelHeader;
import com.google.sampling.experiential.shared.LoginInfo;

public class ExperimentMetadataPanel extends Composite {

  protected MyConstants myConstants;
  protected MyMessages myMessages;
  
  private ExperimentDAO experiment;
  private LoginInfo loginInfo;
  private ArrayList<ExperimentListener> listeners;
  
  VerticalPanel formPanel;
  
  private TextBox titlePanel;
  private Label creatorPanel;
  private TextArea descriptionPanel;
  private DurationView durationPanel;
  private TextArea informedConsentPanel;
  private TextArea adminList;

  public ExperimentMetadataPanel(ExperimentDAO experiment, LoginInfo loginInfo, ExperimentListener listener) {
    myConstants = GWT.create(MyConstants.class);
    myMessages = GWT.create(MyMessages.class);
    
    this.experiment = experiment;
    this.loginInfo = loginInfo;
    this.listeners = new ArrayList<ExperimentListener>();
    if (listener != null) {
      listeners.add(listener);
    }
    
    formPanel = new VerticalPanel();
    initWidget(formPanel);  
    createPanel();
  }
  
  public void createPanel() {
    PanelPair titlePanelPair = createTitlePanel(experiment);
    titlePanel = (TextBox) titlePanelPair.valueHolder;
    titlePanel.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
          experiment.setTitle(event.getValue());
      }
  });
    formPanel.add(titlePanelPair.container);

    formPanel.add(createIdPanel(experiment).container);
    formPanel.add(createVersionPanel(experiment).container);
    
    PanelPair descriptionPanelPair = createDescriptionPanel(experiment);
    descriptionPanel = (TextArea) descriptionPanelPair.valueHolder;
    descriptionPanel.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
          experiment.setDescription(event.getValue());
      }
  });
    formPanel.add(descriptionPanelPair.container);
    
    PanelPair creatorPanelPair = createCreatorPanel(experiment);
    creatorPanel = (Label) creatorPanelPair.valueHolder;
    formPanel.add(creatorPanelPair.container);

    formPanel.add(createAdminDisclosurePanel(experiment));

    PanelPair informedConsentPanelPair = createInformedConsentPanel(experiment);
    informedConsentPanel = (TextArea) informedConsentPanelPair.valueHolder;
    informedConsentPanel.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
          experiment.setInformedConsentForm(event.getValue());
      }
  });
    formPanel.add(informedConsentPanelPair.container);

    
    durationPanel = createDurationPanel(experiment);
    formPanel.add(durationPanel);
  }
  
  private PanelPair createTitlePanel(ExperimentDAO experiment) {
    return createFormLine(myConstants.experimentTitle(), experiment.getTitle(), "paco-HTML-Large");
  }

  private PanelPair createIdPanel(ExperimentDAO experiment) {
    return createDisplayLine(myConstants.experimentId(), 
                          Long.toString(experiment.getId() != null ? experiment.getId() : 0));
  }

  private PanelPair createVersionPanel(ExperimentDAO experiment) {
    return createDisplayLine(myConstants.experimentVersion(), 
                          Integer.toString(experiment.getVersion() == null ? 0 : experiment.getVersion()));
  }

  
  private PanelPair createDescriptionPanel(ExperimentDAO experiment) {
    return createFormArea(myConstants.experimentDescription(), experiment.getDescription(), 75, "100");
  }

  private PanelPair createCreatorPanel(ExperimentDAO experiment) {
    return createDisplayLine(myConstants.experimentCreator(),
        experiment.getCreator() != null ? experiment.getCreator() : loginInfo.getEmailAddress());
  }

  private PanelPair createInformedConsentPanel(ExperimentDAO experiment) {
    return createFormArea(myConstants.informedConsent(), experiment.getInformedConsentForm(), 100, "200");
  }
  
  private DisclosurePanel createAdminDisclosurePanel(final ExperimentDAO experiment) {
    final DisclosurePanel adminPanel = new DisclosurePanel();
    final DisclosurePanelHeader closedHeaderWidget =
        new DisclosurePanelHeader(false, "<b>" + myConstants.clickToEditAdministrators() + "</b>");
    final DisclosurePanelHeader openHeaderWidget =
        new DisclosurePanelHeader(true, "<b>" + myConstants.clickToCloseAdministratorEditor() + "</b>");
    adminPanel.setHeader(closedHeaderWidget);
    adminPanel.addEventHandler(new DisclosureHandler() {
      public void onClose(DisclosureEvent event) {
        adminPanel.setHeader(closedHeaderWidget);
      }

      public void onOpen(DisclosureEvent event) {
        adminPanel.setHeader(openHeaderWidget);
      }
    });
    VerticalPanel adminContentPanel = new VerticalPanel();
    Label instructionlabel = createLabel(myConstants.administratorEditorPrompt());
    adminContentPanel.add(instructionlabel);

    adminList = new TextArea();
    adminList.setCharacterWidth(100);
    adminList.setHeight("100");
    String[] adminStrArray = experiment.getAdmins();
    List<String> admins = Lists.newArrayList(adminStrArray);
    String loginEmailLowercase = loginInfo.getEmailAddress().toLowerCase();
    if (!admins.contains(loginEmailLowercase)) {
      admins.add(loginEmailLowercase);
      setAdminsOn(experiment, toCSVString(admins));
    }
    adminList.setText(toCSVString(admins));
    adminContentPanel.add(adminList);
    adminPanel.setContent(adminContentPanel);

    adminList.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        setAdminsOn(experiment, event.getValue());
      }
    });
    return adminPanel;
  }
  
  private void setAdminsOn(ExperimentDAO experiment, String adminsText) {
    List<String> admins = new ArrayList<String>();
    if (adminsText.length() == 0) {
    admins.add(loginInfo.getEmailAddress());
    } else {
    Splitter sp = Splitter.on(",").trimResults().omitEmptyStrings();
    for (String admin : sp.split(adminsText)) {
    admins.add(admin);
    }
    }
    String[] adminStrArray = new String[admins.size()];
    adminStrArray = admins.toArray(adminStrArray);
    experiment.setAdmins(adminStrArray);
 }
  
  private DurationView createDurationPanel(ExperimentDAO experiment) {
    DurationView durationPanel = new DurationView(experiment);
    return durationPanel;
  }
  
  private PanelPair createFormLine(String key, String value) {
    return createFormLine(key, value, null);
  }


  private PanelPair createFormLine(String key, String value, String styleName) {
    VerticalPanel line = new VerticalPanel();
    line.setStyleName("left");
    Label keyLabel = new Label(key + ": ");
    keyLabel.setStyleName(styleName == null ? "keyLabel" : styleName);
    TextBox valueBox = new TextBox();
    if (value != null) {
      valueBox.setText(value);
    }
    valueBox.setEnabled(true);
    line.add(keyLabel);
    line.add(valueBox);
    return new PanelPair(line, valueBox);
  }

  private PanelPair createFormArea(String key, String value, int width, String height) {
    VerticalPanel line = new VerticalPanel();
    line.setStyleName("left");
    Label keyLabel = new Label(key + ": ");
    keyLabel.setStyleName("keyLabel");
    final TextArea valueBox = new TextArea();
    valueBox.setCharacterWidth(width);
    valueBox.setHeight(height);
    if (value != null) {
      valueBox.setText(value);
    }
    valueBox.addValueChangeHandler(new ValueChangeHandler<String>() {
      
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        if (valueBox.getText().length() >= 500) {
          // TODO surface a message that their text is being truncated.
          valueBox.setText(valueBox.getText().substring(0,499));
        }
        
      }
    });
    valueBox.setEnabled(true);
    line.add(keyLabel);
    line.add(valueBox);
    return new PanelPair(line, valueBox);
  }

  public static PanelPair createDisplayLine(String key, String value) {
    HorizontalPanel line = new HorizontalPanel();
    line.setStyleName("left");
    Label keyLabel = new Label(key + ": ");
    keyLabel.setStyleName("keyLabel");
    
    Label valueBox = new Label();
    if (value != null) {
      valueBox.setText(value);
    }
    line.add(keyLabel);
    line.add(valueBox);
    return new PanelPair(line, valueBox);
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

  private Label createLabel(String title) {
    Label responseTypeLabel = new Label(title);
    responseTypeLabel.setStyleName("keyLabel");
    return responseTypeLabel;
  }
  
  final DisclosurePanelImages images =
      (DisclosurePanelImages) GWT.create(DisclosurePanelImages.class);
  
  class DisclosurePanelHeader extends HorizontalPanel {
    public DisclosurePanelHeader(boolean isOpen, String html) {
      add(
          isOpen ? images.disclosurePanelOpen().createImage()
              : images.disclosurePanelClosed().createImage());
      add(new HTML(html));
    }
  }
  
  public String getExperimentTitle() {
    return titlePanel.getText();
  }
  
  public String getExperimentDescription() {
    return descriptionPanel.getText();
  }
  
  public String getExperimentInformedConsent() {
    return informedConsentPanel.getText();
  }
  
  public boolean getExperimentIsFixedDuration() {
    return durationPanel.isFixedDuration();
  }
  
  public String getExperimentStartDate() {
    return durationPanel.getStartDate();
  }
  
  public String getExperimentEndDate() {
    return durationPanel.getEndDate();
  }
  
  public String getExperimentAdminsList() {
    return adminList.getText();
  }

}
