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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.corechart.LineChart;
import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.ExperimentDAO;
import com.google.sampling.experiential.shared.ExperimentStatsDAO;
import com.google.sampling.experiential.shared.FeedbackDAO;
import com.google.sampling.experiential.shared.InputDAO;
import com.google.sampling.experiential.shared.LoginInfo;
import com.google.sampling.experiential.shared.LoginService;
import com.google.sampling.experiential.shared.LoginServiceAsync;
import com.google.sampling.experiential.shared.MapService;
import com.google.sampling.experiential.shared.MapServiceAsync;
import com.google.sampling.experiential.shared.Whitelist;

/**
 * Default Entry point into the GWT application.
 * Checks login. Renders Joined Experiments view by default.
 * 
 * @author Bob Evans
 *
 */
public class Main implements EntryPoint, ExperimentListener {

  private Label statusLabel;
  private FlexTable flexTable;
  Images resources;
  private HTML listTitle;
  private VerticalPanel contentPanel;
  private VerticalPanel mainPanel;
  private VerticalPanel experimentPanel;
  private List<ExperimentDAO> joinedExperiments;
  private List<ExperimentDAO> adminedExperiments;

  private MapServiceAsync mapService = GWT.create(MapService.class);

  private LoginInfo loginInfo = null;
  private Anchor signInLink = new Anchor("Login");
  private Anchor signOutLink = new Anchor("Logout");

  private FlowPanel loginPanel = new FlowPanel();
  private Label loginLabel =
      new Label("Please sign in to your Google Account " + "to access the application.");


  public void onModuleLoad() {
    if (GWT.getHostPageBaseURL().startsWith("http://") && !(GWT.getHostPageBaseURL().contains("127.0.0.1") ||
        GWT.getHostPageBaseURL().contains("localhost"))) {
      Window.Location.assign(GWT.getHostPageBaseURL().replace("http://", "https://")+"Main.html");
    }
    resources = GWT.create(Images.class);
    checkLoginStatusAndLoadPage();
  }

  private void checkLoginStatusAndLoadPage() {
    LoginServiceAsync loginService = GWT.create(LoginService.class);
    loginService.login(GWT.getHostPageBaseURL()+"Main.html", new AsyncCallback<LoginInfo>() {
      public void onFailure(Throwable error) {
      }

      public void onSuccess(LoginInfo result) {
        loginInfo = result; 
        if (loginInfo.isLoggedIn() && new Whitelist().allowed(loginInfo.getEmailAddress())) {
//          ArrayList<LoadLibrary> loadLibraries = new ArrayList<LoadApi.LoadLibrary>();
//          loadLibraries.add(LoadLibrary.ADSENSE);
//          loadLibraries.add(LoadLibrary.DRAWING);
//          loadLibraries.add(LoadLibrary.GEOMETRY);
//          loadLibraries.add(LoadLibrary.PANORAMIO);
//          loadLibraries.add(LoadLibrary.PLACES);
//          loadLibraries.add(LoadLibrary.WEATHER);
//          
//          LoadApi.go(new Runnable() {
//            public void run() {
              loginPanel.setVisible(false);
              createHomePage();
              signOutLink.setHref(loginInfo.getLogoutUrl());

        } else {
          loadLogin();
        }
      }
    });
  }

  private void loadLogin() {
    loginPanel.setStyleName("front_page");
    HTML index2Html  = new HTML(resources.indexHtml().getText());    
    
    signInLink.setHref(loginInfo.getLoginUrl());
    signInLink.setStyleName("paco-HTML-Large");
    signInLink.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    
    //loginPanel.add(loginLabel);
    loginPanel.add(signInLink);
    loginPanel.add(index2Html);
    RootPanel.get().add(loginPanel);
    //RootPanel.get().add(index2Html);
  }



  private void createHomePage() {
    RootPanel rootPanel = RootPanel.get();

    mainPanel = new VerticalPanel();
    mainPanel.setSpacing(2);
    rootPanel.add(mainPanel);

    HorizontalPanel menuPanel = createMenuBar();
    createStatusPanelOnMenubar(menuPanel);

    listTitle = new HTML("");
    mainPanel.add(listTitle);
    listTitle.setStyleName("paco-HTML-Large");
    listTitle.setWordWrap(false);
    listTitle.setSize("270px", "22");

    mainPanel.setCellHorizontalAlignment(listTitle, HasHorizontalAlignment.ALIGN_CENTER);

    HorizontalPanel horizontalPanel = new HorizontalPanel();
    horizontalPanel.setSpacing(2);
    horizontalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
    mainPanel.add(horizontalPanel);

    experimentPanel = new VerticalPanel();
    ScrollPanel scrollPanel = new ScrollPanel(experimentPanel);
    horizontalPanel.add(scrollPanel);
    experimentPanel.setStyleName("paco-experimentPanel");
    experimentPanel.setSpacing(2);
    experimentPanel.setVisible(false);
    experimentPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);

    flexTable = new FlexTable();
    String height = (Window.getClientHeight() - 200) + "px";
    ScrollPanel sp = new ScrollPanel(flexTable);
    //flexTable.setSize("400px", height);
    experimentPanel.add(flexTable);

    contentPanel = new VerticalPanel();
    contentPanel.setSpacing(2);
    horizontalPanel.add(contentPanel);
    contentPanel.setSize("479px", "325px");

    loadJoinedExperiments();
    createCallbackForGviz();
  }

  private void createStatusPanelOnMenubar(HorizontalPanel menuPanel) {
    statusLabel = new Label("Loading");
    statusLabel.setStyleName("paco-Loading-Panel");
    statusLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

    statusLabel.setSize("80px", "30px");
    statusLabel.setVisible(false);

    menuPanel.add(statusLabel);
  }

  private HorizontalPanel createMenuBar() {
    HorizontalPanel menuPanel = new HorizontalPanel();
    mainPanel.add(menuPanel);
    Image pacoLogo = new Image(resources.pacoSmallLogo());
    pacoLogo.setStylePrimaryName("paco-Logo");
    menuPanel.add(pacoLogo);

    MenuBar rootMenuBar = new MenuBar(false);
    menuPanel.add(rootMenuBar);

    MenuBar joinedSubMenuBar = new MenuBar(true);
    MenuItem joinedMenuItem = new MenuItem("Current Experiments (Joined)", false, joinedSubMenuBar);
    MenuItem mntmShowAllJoined = new MenuItem("Show All", false, new Command() {
      public void execute() {
        loadJoinedExperiments();
      }
    });
    joinedSubMenuBar.addItem(mntmShowAllJoined);

    MenuItem mntmFindExperiments = new MenuItem("Find Experiments", false, new Command() {
      public void execute() {
        findExperiments();
      }
    });
    mntmFindExperiments.setEnabled(false);
    joinedSubMenuBar.addItem(mntmFindExperiments);
    rootMenuBar.addItem(joinedMenuItem);

    MenuBar adminMenuBar = new MenuBar(true);
    MenuItem adminMenuItem = new MenuItem("Administer Experiments", false, adminMenuBar);
    MenuItem mntmShowAllAdmin = new MenuItem("Show All", false, new Command() {
      public void execute() {
        loadAdministeredExperiments(false);
      }
    });
    adminMenuBar.addItem(mntmShowAllAdmin);

    MenuItem mntmCreateNew = new MenuItem("Create New", false, new Command() {
      public void execute() {
        createNewExperiment();
      }
    });
    adminMenuBar.addItem(mntmCreateNew);
    rootMenuBar.addItem(adminMenuItem);
    // //////////////////

    MenuItem mntmQR_Code = new MenuItem("Get Android app", false, new Command() {
      public void execute() {
        showAndroidDownloadPage();
      }
    });
    rootMenuBar.addItem(mntmQR_Code);


    // ////////////////
    MenuBar helpMenuBar = new MenuBar(true);
    MenuItem helpMenuItem = new MenuItem("Help", false, helpMenuBar);
    rootMenuBar.addItem(helpMenuItem);

    MenuItem helpContentsMenuItem = new MenuItem("User Guide", false, new Command() {
      public void execute() {
        launchHelp();
      }
    });
    //helpContentsMenuItem.setEnabled(false);
    helpMenuBar.addItem(helpContentsMenuItem);

    MenuItem aboutMenuItem = new MenuItem("About", false, new Command() {
      public void execute() {
        launchAbout();
      }
    });
    aboutMenuItem.setEnabled(false);
    helpMenuBar.addItem(aboutMenuItem);
    
    // logout

    MenuItem mntmLogout = new MenuItem("Logout", false, new Command() {
      public void execute() {
        logout();
      }
    });
    rootMenuBar.addItem(mntmLogout);
    return menuPanel;
  }

  protected void logout() {
    Window.Location.assign(signOutLink.getHref());
  }

  /**
   * 
   */
  protected void showAndroidDownloadPage() {
    contentPanel.clear();
    experimentPanel.setVisible(false);
    setContentTitle("Download the PACO Android Client");
    VerticalPanel dl = new VerticalPanel();

    HTML barCodeLabel = new HTML("1) Ensure that you can install applications from Unknown Sources.");
    barCodeLabel.setStyleName("paco-HTML-Large");
    dl.add(barCodeLabel);
    dl.add(new HTML("On your phone, open the 'Settings' app. Click 'Applications' and check 'Unknown Sources'."));
 
    HTML barCodeLabel2 = new HTML("2a) Scan this code with your phone which will launch the browser and download Paco.");
    barCodeLabel2.setStyleName("paco-HTML-Large");
    dl.add(barCodeLabel2);
    dl.add(new Image(resources.qrcode()));
    
    HTML downloadLink = new HTML("2b) If you are browsing this page from your phone, just <a href=\"/paco.apk\">click here to download Paco</a>.");
    downloadLink.setStyleName("paco-HTML-Large");
    dl.add(downloadLink);

    contentPanel.add(dl);
  }

  private void createCallbackForGviz() {
    // Create a callback to be called when the visualization API
    // has been loaded.
    Runnable onLoadCallback = new Runnable() {
      public void run() {
      }
    };
    // Load the visualization api, passing the onLoadCallback to be called
    // when loading is done.
    VisualizationUtils.loadVisualizationApi(onLoadCallback, /*
                                                             * ColumnChart.PACKAGE
                                                             * , Table.PACKAGE,
                                                             */
    LineChart.PACKAGE/* , ScatterChart.PACKAGE */);
  }

  protected void findExperiments() {
    statusLabel.setVisible(true);
    setContentTitle("Find Experiments");
    contentPanel.clear();
    experimentPanel.setVisible(true);
    statusLabel.setVisible(false);
    getExperiments(false, false);
  }

  private void setContentTitle(String text) {
    listTitle.setHTML(text);
  }

  protected void createNewExperiment() {
    setContentTitle("Create New Experiment");
    contentPanel.clear();
    experimentPanel.setVisible(false);
    ExperimentDAO experiment = new ExperimentDAO();
    showExperimentDetailPanel(experiment, true);
  }

  protected void launchAbout() {
    setContentTitle("About PACO");
    contentPanel.clear();
    experimentPanel.setVisible(false);
  }

  protected void launchHelp() {
    setContentTitle("Help");
    contentPanel.clear();
    experimentPanel.setVisible(false);
    HelpPage hp = new HelpPage(this);
    contentPanel.add(hp);
  }

  protected void loadJoinedExperiments() {
    statusLabel.setVisible(true);
    setContentTitle("Current Experiments (Personal Data)");
    contentPanel.clear();
    flexTable.clear();
    experimentPanel.setVisible(true);
    getExperiments(true, false);
  }

  protected void loadAdministeredExperiments(boolean experimentsDirty) {
    statusLabel.setVisible(true);
    setContentTitle("Administered Experiments");
    contentPanel.clear();
    flexTable.clear();
    experimentPanel.setVisible(true);
    getExperiments(false, experimentsDirty);
  }

  private void addRowsToTable(List<ExperimentRow> exRows) {
    int row = 0;
    for (ExperimentRow experimentRow : exRows) {
      flexTable.setWidget(row++, 0, experimentRow);
    }
  }

  private List<ExperimentRow> createExperimentRows(
      boolean joined, List<ExperimentDAO> experiments) {
    List<ExperimentRow> exRows = new ArrayList<ExperimentRow>();
    for (ExperimentDAO experiment : experiments) {
      exRows.add(new ExperimentRow(resources, experiment, this, joined));
    }
    return exRows;
  }

  private void getExperiments(final boolean joined, final boolean experimentsDirty) {
    AsyncCallback<List<ExperimentDAO>> callback = new AsyncCallback<List<ExperimentDAO>>() {
      @Override
      public void onFailure(Throwable caught) {
        Window.alert("Could not retrieve your experiments!!");
        statusLabel.setVisible(false);
      }

      @Override
      public void onSuccess(List<ExperimentDAO> result) {
        Collections.sort(result, new Comparator<ExperimentDAO>() {

          @Override
          public int compare(ExperimentDAO arg0, ExperimentDAO arg1) {
            return arg0.getTitle().toLowerCase().compareTo(arg1.getTitle().toLowerCase());            
          }
          
        });
        if (joined) {
          if (joinedExperiments == null) {
            joinedExperiments = result;
          }
        } else {
          if (experimentsDirty || adminedExperiments == null) {
            adminedExperiments = result;
          }
        }
        addRowsToTable(createExperimentRows(joined, result));
        statusLabel.setVisible(false);

      }
    };
    if (joined) {
      mapService.getUsersJoinedExperiments(callback);
    } else {
      mapService.getExperimentsForUser(callback);
    }
  }

  private List<ExperimentDAO> retrieveAdminedExperiments() {
    List<ExperimentDAO> experiments = new ArrayList<ExperimentDAO>();
    experiments.add(new ExperimentDAO());
    experiments.add(new ExperimentDAO());
    experiments.add(new ExperimentDAO());
    return experiments;
  }

  public void eventFired(int experimentCode, ExperimentDAO experiment, boolean joined) {
    switch (experimentCode) {
      case ExperimentListener.STATS_CODE:
        contentPanel.clear();
        showStatsPanel(experiment, joined);
        break;
      case ExperimentListener.CHARTS_CODE:
        contentPanel.clear();
        showChart(experiment, joined);
        break;
      case ExperimentListener.CSV_CODE:
        String joinedStr = "";
        if (joined) {
          joinedStr = ":who=" + loginInfo.getEmailAddress();
        }
        Window.open(
            "/events?csv&q='experimentId=" + experiment.getId() + joinedStr + "'", "_blank","");
        break;
      case ExperimentListener.DELETE_CODE:
        if (Window.confirm("Are you sure you want to deleted this experiment definition? " 
            + "Perhaps you want to unpublish it to hide it from new users?")) {
          deleteExperiment(experiment, joined);
        }
        break;
      case ExperimentListener.EDIT_CODE:
        contentPanel.clear();
        showExperimentDetailPanel(experiment, !joined);
        break;
      case ExperimentListener.SAVED:
        contentPanel.clear();
        saveToServer(experiment);
        break;
      case ExperimentListener.CANCELED:
        contentPanel.clear();
        loadAdministeredExperiments(false);
        break;
      case ExperimentListener.SOFT_DELETE_CODE:
        softDeleteExperiment(experiment);
        break;
      case ExperimentListener.CSV_ANON_CODE:
        String whoStr = "";
        if (joined) {
          whoStr = ":who=" + loginInfo.getEmailAddress();
        }
        Window.open(
            "/events?csv&anon=true&q='experimentId=" + experiment.getId() + whoStr + "'","_blank","");
        break;
      case ExperimentListener.COPY_EXPERIMENT_CODE:
        contentPanel.clear();
        copyExperiment(experiment);
        showExperimentDetailPanel(experiment, true);
        break;
      case ExperimentListener.ANON_MAPPING_CODE:
        String who2Str = "";
        if (joined) {
          who2Str = ":who=" + loginInfo.getEmailAddress();
        }
        Window.open(
            "/events?csv&mapping=true&q='experimentId=" + experiment.getId() + who2Str + "'", "_blank","");
        break;
      case ExperimentListener.DATA_CODE:
        String dataQuery = "";
        if (joined) {
          dataQuery = ":who=" + loginInfo.getEmailAddress();
        }
        Window.open("/events?q='experimentId=" + experiment.getId() + dataQuery + "'", "_blank", "");
        break;   
      case ExperimentListener.EXPERIMENT_RESPONSE_CODE:
        contentPanel.clear();
        break;
      case ExperimentListener.EXPERIMENT_RESPONSE_CANCELED_CODE:
        contentPanel.clear();
        break;     
      case ExperimentListener.SHOW_EXPERIMENT_RESPONSE_CODE:
        contentPanel.clear();
        showExperimentExecutorPanel(experiment, joined);
        break;   
      case ExperimentListener.SHOW_QR_CODE:
        String experimentId = "0000"+Long.toString(experiment.getId());
        Window.open("http://chart.apis.google.com/chart?cht=qr&chs=350x350&chld=" +
        		"L&choe=UTF-8&chl=content%3A%2F%2Fcom.google.android.apps.paco.ExperimentProvider%2Fexperiments%2F"+experimentId,
                    "_blank","");
        break;   
      case ExperimentListener.SHOW_REF_CODE:
        contentPanel.clear();
        showExperimentReferencePanel(experiment);
        break;   
      case ExperimentListener.INDIVIDUAL_STATS_CODE:        
        Window.open("/participantStats?experimentId=" + experiment.getId(),
                    "_blank","");
        break;   

    }
  }

  class ExperimentReferenceDialog extends DialogBox {

    private Long referringExperimentId;

    public ExperimentReferenceDialog(Long referringId) {
      super();
      this.referringExperimentId = referringId;
      setText("Reference an Experiment from this Experiment");
      VerticalPanel referenceDialogPanel = new VerticalPanel();
      Label label = new Label("Enter ID of referenced Experiment");
      referenceDialogPanel.add(label);
      final TextBox id = new TextBox();
      referenceDialogPanel.add(id);
      
      HorizontalPanel buttonPanel = new HorizontalPanel();
      referenceDialogPanel.add(buttonPanel);
      Button ok = new Button("OK");
      buttonPanel.add(ok);
      ok.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
          String value = id.getValue();
          Long referencedExperimentId = null;
          if (value != null) {
            referencedExperimentId = Long.parseLong(value);
            AsyncCallback<Void> callback = new AsyncCallback<Void>() {

              @Override
              public void onFailure(Throwable caught) {
                Window.alert("Could not make reference between experiments. " + caught.getMessage());                
              }

              @Override
              public void onSuccess(Void result) {
                Window.alert("Success.");                
              }
              
            };
            mapService.setReferencedExperiment(referringExperimentId, referencedExperimentId, callback);
          }
          ExperimentReferenceDialog.this.hide();
          
        }
      });
      Button cancel = new Button("Cancel");
      buttonPanel.add(cancel);
      cancel.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
          ExperimentReferenceDialog.this.hide();
          
        }
      });
      

      setWidget(referenceDialogPanel);
    }
    
  }
  
  private void showExperimentReferencePanel(ExperimentDAO experiment) {
    statusLabel.setVisible(true);
    ExperimentReferenceDialog experimentReferenceDialog = new ExperimentReferenceDialog(experiment.getId());
    experimentReferenceDialog.center();
    experimentReferenceDialog.show();
  }

  private void copyExperiment(ExperimentDAO experiment) {
    experiment.setId(null);
    experiment.getSchedule().setId(null);
    for(InputDAO input : experiment.getInputs()) {
      input.setId(null);
    }
    for (FeedbackDAO feedback : experiment.getFeedback()) {
      feedback.setId(null);
    }
  }


  private void saveToServer(ExperimentDAO experiment) {
    statusLabel.setVisible(true);

    mapService.saveExperiment(experiment, new AsyncCallback<Void>() {

      @Override
      public void onFailure(Throwable caught) {
        Window.alert("Failure");
        statusLabel.setVisible(false);

      }

      @Override
      public void onSuccess(Void result) {
        Window.alert("Success");
        loadAdministeredExperiments(true);

        statusLabel.setVisible(false);
      }
    });
  }

  private void softDeleteExperiment(ExperimentDAO experiment) {
    statusLabel.setVisible(true);
    // toggle
    experiment.setDeleted(experiment.getDeleted() == null || !experiment.getDeleted());

    mapService.saveExperiment(experiment, new AsyncCallback<Void>() {

      @Override
      public void onFailure(Throwable caught) {
        Window.alert("Failure");
        statusLabel.setVisible(false);

      }

      @Override
      public void onSuccess(Void result) {
        Window.alert("Success");
        loadAdministeredExperiments(true);

        statusLabel.setVisible(false);
      }
    });
  }

  /**
   * @param experiment
   * @param joined
   */
  private void deleteExperiment(ExperimentDAO experiment, boolean joined) {
    if (joined) {
      deleteData(experiment);
    } else {
      deleteExperimentDefinition(experiment);
    }
  }

  /**
   * @param experiment
   */
  private void deleteExperimentDefinition(ExperimentDAO experiment) {
    statusLabel.setVisible(true);
    mapService.deleteExperiment(experiment, new AsyncCallback<Boolean>() {

      @Override
      public void onFailure(Throwable caught) {
        Window.alert("Error deleting experiment.");
        statusLabel.setVisible(false);
      }

      @Override
      public void onSuccess(Boolean result) {
        // Window.alert("Success deleting experiment.");
        // flexTable.removeRow(obj.getRowIndex());
        loadAdministeredExperiments(true);
      }

    });
  }

  /**
   * @param experiment
   */
  private void deleteData(ExperimentDAO experiment) {
  }

  private void showChart(final ExperimentDAO experiment, boolean joined) {
    statusLabel.setVisible(true);
    AsyncCallback<List<EventDAO>> callback = new AsyncCallback<List<EventDAO>>() {

      @Override
      public void onFailure(Throwable caught) {
        Window.alert("Could not retrieve charts");
        statusLabel.setVisible(false);

      }

      @Override
      public void onSuccess(List<EventDAO> eventList) {
        if (eventList.size() == 0) {
          Window.alert("No results for your query");
          statusLabel.setVisible(false);
          return;
        }
        // renderEventsOnList(eventList);
        ExperimentChartsPanel ep = new ExperimentChartsPanel(experiment, eventList);
        contentPanel.add(ep);
        statusLabel.setVisible(false);
      }
    };
    String queryText = "experimentId=" + experiment.getId();
    if (joined) {
      queryText += ":who=" + loginInfo.getEmailAddress();
    }
    mapService.mapWithTags(queryText, callback);
    // for each question in the experiment
    // print the title of the experiment
    // lookup the question in the events list,
    // make a chart with the time series for the values of that question
    // if the response type is number (likert, etc.) - simple connected line
    // else if the response type is open text - word cloud? or skip - yes!

  }

  private void showExperimentExecutorPanel(final ExperimentDAO experiment, final boolean joined) {
    statusLabel.setVisible(true);
    
    AsyncCallback<ExperimentDAO> referencedCheckCallback = new AsyncCallback<ExperimentDAO>() {

      @Override
      public void onFailure(Throwable caught) {
        //Window.alert("Could not find referenced experiment");
        statusLabel.setVisible(false);
        showRegularExperimentEntry(experiment, joined);
      }

      @Override
      public void onSuccess(ExperimentDAO referringExperiment) {
        statusLabel.setVisible(false);
        if (referringExperiment == null) {
          showRegularExperimentEntry(experiment, joined);
        } else {
          showReferredExperimentExecutor(experiment, referringExperiment);
        }
      }
    };
    mapService.referencedExperiment(experiment.getId(), referencedCheckCallback);

  }

  protected void showReferredExperimentExecutor(final ExperimentDAO experiment, final ExperimentDAO referencedExperiment) {
    statusLabel.setVisible(true);
    AsyncCallback<List<EventDAO>> callback = new AsyncCallback<List<EventDAO>>() {

      @Override
      public void onFailure(Throwable caught) {
        Window.alert("Could not retrieve events from referenced experiment.<br/>" + caught.getMessage());
        statusLabel.setVisible(false);

      }

      @Override
      public void onSuccess(List<EventDAO> eventList) {
        if (eventList.size() == 0) {
          Window.alert("No events found for referencing.");
          statusLabel.setVisible(false);
          return;
        }
        AbstractExperimentExecutorPanel ep = new EndOfDayExperimentExecutorPanel(Main.this, mapService, 
                                                                                 experiment, eventList, referencedExperiment);
        contentPanel.add(ep);
        statusLabel.setVisible(false);
      }
    };
    String queryText = "experimentId=" + referencedExperiment.getId() + ":who=" + loginInfo.getEmailAddress();
    mapService.mapWithTags(queryText, callback);
    
  }

  protected void showRegularExperimentEntry(ExperimentDAO experiment, boolean joined) {
    statusLabel.setVisible(true);
    AbstractExperimentExecutorPanel ep = new ExperimentExecutorPanel(this, mapService, experiment);
    contentPanel.add(ep);
    statusLabel.setVisible(false);
  }

  private void showExperimentDetailPanel(ExperimentDAO experiment, boolean joined) {
    statusLabel.setVisible(true);
    ExperimentDefinitionPanel ep = new ExperimentDefinitionPanel(experiment, joined, loginInfo, this);
    contentPanel.add(ep);
    statusLabel.setVisible(false);
  }

  private void showStatsPanel(final ExperimentDAO experiment, final boolean joined) {
    statusLabel.setVisible(true);
    AsyncCallback<ExperimentStatsDAO> callback = new AsyncCallback<ExperimentStatsDAO>() {

      @Override
      public void onFailure(Throwable caught) {
        Window.alert("Could not retrieve results for experiment: " + experiment.getTitle() + "\n"
            + caught.getMessage());
        statusLabel.setVisible(false);
      }

      @Override
      public void onSuccess(ExperimentStatsDAO stats) {
        StatsPanel p = new StatsPanel(stats, experiment, joined, Main.this);
        contentPanel.add(p);
        statusLabel.setVisible(false);
      }
    };
    // TODO (bobevans) move this to the server
    mapService.statsForExperiment(experiment.getId(), joined, callback);


  }
}
