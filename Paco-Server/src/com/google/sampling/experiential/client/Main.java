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
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
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
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.corechart.LineChart;
import com.google.sampling.experiential.shared.Response;
import com.google.sampling.experiential.shared.Experiment;
import com.google.sampling.experiential.shared.ExperimentStats;
import com.google.sampling.experiential.shared.Feedback;
import com.google.sampling.experiential.shared.Input;
import com.google.sampling.experiential.shared.LoginInfo;
import com.google.sampling.experiential.shared.LoginService;
import com.google.sampling.experiential.shared.LoginServiceAsync;
import com.google.sampling.experiential.shared.MapService;
import com.google.sampling.experiential.shared.MapServiceAsync;

import org.restlet.client.resource.ClientResource;

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
  private HTML lblYourExperiments;
  private VerticalPanel contentPanel;
  private VerticalPanel mainPanel;
  private VerticalPanel experimentPanel;
  private List<Experiment> joinedExperiments;
  private List<Experiment> adminedExperiments;

  private MapServiceAsync mapService = GWT.create(MapService.class);

  private LoginInfo loginInfo = null;
  private Anchor signInLink = new Anchor("Sign In");
  private Anchor signOutLink = new Anchor("Sign out");

  private VerticalPanel loginPanel = new VerticalPanel();
  private Label loginLabel =
      new Label("Please sign in to your Google Account " + "to access the application.");


  public void onModuleLoad() {
    checkLoginStatusAndLoadPage();
  }

  private void checkLoginStatusAndLoadPage() {
    LoginServiceAsync loginService = GWT.create(LoginService.class);
    loginService.login(GWT.getHostPageBaseURL()+"Main.html", new AsyncCallback<LoginInfo>() {
      public void onFailure(Throwable error) {
      }

      public void onSuccess(LoginInfo result) {
        loginInfo = result; 
        if (loginInfo.isLoggedIn()) {
//          Maps.loadMapsApi("", "2", false, new Runnable() {
//            public void run() {
              createHomePage();
              signOutLink.setHref(loginInfo.getLogoutUrl());

//            }
//          });

        } else {
          loadLogin();
        }
      }
    });
  }

  private void loadLogin() {
    signInLink.setHref(loginInfo.getLoginUrl());
    loginPanel.add(loginLabel);
    loginPanel.add(signInLink);
    RootPanel.get().add(loginPanel);
  }



  private void createHomePage() {
    resources = GWT.create(Images.class);
    RootPanel rootPanel = RootPanel.get();

    mainPanel = new VerticalPanel();
    mainPanel.setSpacing(2);
    rootPanel.add(mainPanel);

    HorizontalPanel menuPanel = new HorizontalPanel();
    mainPanel.add(menuPanel);
    Image pacoLogo = new Image(resources.pacoSmallLogo());
    pacoLogo.setStylePrimaryName("paco-Logo");
    menuPanel.add(pacoLogo);

    MenuBar rootMenuBar = new MenuBar(false);
    menuPanel.add(rootMenuBar);

    MenuBar joinedSubMenuBar = new MenuBar(true);
    MenuItem joinedMenuItem = new MenuItem("Joined Experiments", false, joinedSubMenuBar);
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
    mntmFindExperiments.setEnabled(true);
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

    MenuItem mntmQR_Code = new MenuItem("Get Android Client", false, new Command() {
      public void execute() {
        showAndroidDownloadPage();
      }
    });
    rootMenuBar.addItem(mntmQR_Code);


    // ////////////////
    MenuBar helpMenuBar = new MenuBar(true);
    MenuItem helpMenuItem = new MenuItem("Help", false, helpMenuBar);
    rootMenuBar.addItem(helpMenuItem);

    MenuItem helpContentsMenuItem = new MenuItem("Help Contents", false, new Command() {
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

    // status label


    statusLabel = new Label("Loading");
    statusLabel.setStyleName("paco-Loading-Panel");
    statusLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

    statusLabel.setSize("80px", "24px");
    statusLabel.setVisible(false);

    menuPanel.add(statusLabel);

    lblYourExperiments = new HTML("");
    mainPanel.add(lblYourExperiments);
    lblYourExperiments.setStyleName("paco-HTML-Large");
    lblYourExperiments.setWordWrap(false);
    lblYourExperiments.setSize("270px", "22");

    mainPanel.setCellHorizontalAlignment(lblYourExperiments, HasHorizontalAlignment.ALIGN_CENTER);

    HorizontalPanel horizontalPanel = new HorizontalPanel();
    horizontalPanel.setSpacing(2);
    horizontalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
    mainPanel.add(horizontalPanel);

    experimentPanel = new VerticalPanel();
    horizontalPanel.add(experimentPanel);
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
    lblYourExperiments.setHTML(text);
  }

  protected void createNewExperiment() {
    setContentTitle("Create New Experiment");
    contentPanel.clear();
    experimentPanel.setVisible(false);
    Experiment experiment = new Experiment();
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
    setContentTitle("Joined Experiments");
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
      boolean joined, List<Experiment> experiments) {
    List<ExperimentRow> exRows = new ArrayList<ExperimentRow>();
    for (Experiment experiment : experiments) {
      exRows.add(new ExperimentRow(resources, experiment, this, joined));
    }
    return exRows;
  }

  private void getExperiments(final boolean joined, final boolean experimentsDirty) {
    AsyncCallback<List<Experiment>> callback = new AsyncCallback<List<Experiment>>() {
      @Override
      public void onFailure(Throwable caught) {
        Window.alert("Could not retrieve your experiments!!");
        statusLabel.setVisible(false);
      }

      @Override
      public void onSuccess(List<Experiment> result) {
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

    /*
    if (joined) {
      mapService.getUsersJoinedExperiments(callback);
    } else {
      mapService.getExperimentsForUser(callback);
    }
    */

    String reference;

    if (joined) {
      reference = "/subject/experiments";
    } else {
      reference = "/observer/experiments";
    }

    PacoResourceProxy experimentsResource = GWT.create(PacoResourceProxy.class);
    experimentsResource.getClientResource().setReference(reference);
    experimentsResource.list(callback);
  }

  private List<Experiment> retrieveAdminedExperiments() {
    List<Experiment> experiments = new ArrayList<Experiment>();
    experiments.add(new Experiment());
    experiments.add(new Experiment());
    experiments.add(new Experiment());
    return experiments;
  }

  public void eventFired(int experimentCode, Experiment experiment, boolean joined) {
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
        //Window.Location.assign(
        //    "/responses?csv&q='experimentId=" + experiment.getId() + joinedStr + "'");
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
        //Window.Location.assign(
        //    "/responses?csv&anon=true&q='experimentId=" + experiment.getId() + whoStr + "'");
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
        //Window.Location.assign(
        //    "/responses?csv&mapping=true&q='experimentId=" + experiment.getId() + who2Str + "'");
        break;
    }
  }

  private void copyExperiment(Experiment experiment) {
    //experiment.setId(null);
  }


  private void saveToServer(Experiment experiment) {
    statusLabel.setVisible(true);

    AsyncCallback<Void> callback = new AsyncCallback<Void>() {

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
    };

    //mapService.saveExperiment(experiment, callback);

    PacoResourceProxy experimentsResource = GWT.create(PacoResourceProxy.class);
    experimentsResource.getClientResource().setReference("/experiments");
    experimentsResource.create(experiment, callback);
  }

  private void softDeleteExperiment(Experiment experiment) {
    statusLabel.setVisible(true);
    // toggle
    experiment.setDeleted(!experiment.isDeleted());

    AsyncCallback<Void> callback = new AsyncCallback<Void>() {

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
    };

    //mapService.saveExperiment(experiment, callback);

    //PacoResourceProxy experimentsResource = GWT.create(PacoResourceProxy.class);
    //experimentsResource.getClientResource().setReference("/observer/experiments/" + experiment.getId());
    //experimentsResource.update(experiment, callback);
 }

  /**
   * @param experiment
   * @param joined
   */
  private void deleteExperiment(Experiment experiment, boolean joined) {
    if (joined) {
      deleteData(experiment);
    } else {
      deleteExperimentDefinition(experiment);
    }
  }

  /**
   * @param experiment
   */
  private void deleteExperimentDefinition(Experiment experiment) {
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
  private void deleteData(Experiment experiment) {
  }

  private void showChart(final Experiment experiment, boolean joined) {
    statusLabel.setVisible(true);
    AsyncCallback<List<Response>> callback = new AsyncCallback<List<Response>>() {

      @Override
      public void onFailure(Throwable caught) {
        Window.alert("Could not retrieve charts");
        statusLabel.setVisible(false);

      }

      @Override
      public void onSuccess(List<Response> responses) {
        if (responses.size() == 0) {
          Window.alert("No results for your query");
          statusLabel.setVisible(false);
          return;
        }
        // renderResponsesOnList(responses);
        ExperimentChartsPanel ep = new ExperimentChartsPanel(experiment, responses);
        contentPanel.add(ep);
        statusLabel.setVisible(false);
      }
    };
    //String queryText = "experimentId=" + experiment.getId();
    //if (joined) {
    //  queryText += ":who=" + loginInfo.getEmailAddress();
    //}
    //mapService.mapWithTags(queryText, callback);
    // for each question in the experiment
    // print the title of the experiment
    // lookup the question in the responses list,
    // make a chart with the time series for the values of that question
    // if the response type is number (likert, etc.) - simple connected line
    // else if the response type is open text - word cloud? or skip - yes!

  }

  private void showExperimentDetailPanel(Experiment experiment, boolean joined) {
    statusLabel.setVisible(true);
    ExperimentDefinitionPanel ep = new ExperimentDefinitionPanel(experiment, joined, loginInfo, this);
    contentPanel.add(ep);
    statusLabel.setVisible(false);
  }

  private void showStatsPanel(final Experiment experiment, final boolean joined) {
    statusLabel.setVisible(true);
    AsyncCallback<ExperimentStats> callback = new AsyncCallback<ExperimentStats>() {

      @Override
      public void onFailure(Throwable caught) {
        Window.alert("Could not retrieve results for experiment: " + experiment.getTitle() + "\n"
            + caught.getMessage());
        statusLabel.setVisible(false);
      }

      @Override
      public void onSuccess(ExperimentStats stats) {
        StatsPanel p = new StatsPanel(stats, experiment, joined);
        contentPanel.add(p);
        statusLabel.setVisible(false);
      }
    };

    // TODO (bobevans) move this to the server
    //mapService.statsForExperiment(experiment.getId(), joined, callback);


    /*
    String reference;

    if (joined) {
      reference = "/subject/experiments/" + experiment.getId();
    } else {
      reference = "/observer/experiments/" + experiment.getId();
    }

    PacoResourceProxy experimentsResource = GWT.create(PacoResourceProxy.class);
    experimentsResource.getClientResource().setReference(reference);
    experimentsResource.stats(callback);
    */
  }
}
