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
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.maps.client.LoadApi;
import com.google.gwt.maps.client.LoadApi.LoadLibrary;
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
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.corechart.LineChart;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.paco.shared.model.ExperimentQueryResult;
import com.google.paco.shared.model.FeedbackDAO;
import com.google.paco.shared.model.InputDAO;
import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.ExperimentStatsDAO;
import com.google.sampling.experiential.shared.LoginInfo;
import com.google.sampling.experiential.shared.LoginService;
import com.google.sampling.experiential.shared.LoginServiceAsync;
import com.google.sampling.experiential.shared.PacoService;
import com.google.sampling.experiential.shared.PacoServiceAsync;

/**
 * Default Entry point into the GWT application.
 * Checks login. Renders Joined Experiments view by default.
 *
 * @author Bob Evans
 *
 */
public class Main implements EntryPoint, ExperimentListener {

  private static final Integer DEFAULT_LIMIT_SIZE = 20;

  public static String ERROR_HIGHLIGHT = "error-highlight";

  private Label statusLabel;
  private FlexTable flexTable;
  Images resources;
  private HTML listTitle;
  private VerticalPanel contentPanel;
  private VerticalPanel mainPanel;
  private VerticalPanel experimentPanel;
  private List<ExperimentDAO> experiments;


  private PacoServiceAsync pacoService = GWT.create(PacoService.class);

  private LoginInfo loginInfo = null;
  private Anchor signInLink = new Anchor("Login");
  private Anchor signOutLink = new Anchor("Sign in as another user");

  private FlowPanel loginPanel = new FlowPanel();

  protected MyConstants myConstants;
  protected MyMessages myMessages;
  private ScrollPanel leftSidePanel;

  protected String cursor;


  public void onModuleLoad() {
    if (GWT.getHostPageBaseURL().startsWith("http://") && !(GWT.getHostPageBaseURL().contains("127.0.0.1") ||
        GWT.getHostPageBaseURL().contains("localhost"))) {
      Window.Location.assign(GWT.getHostPageBaseURL().replace("http://", "https://") );
    }
    resources = GWT.create(Images.class);
    myConstants = GWT.create(MyConstants.class);
    myMessages = GWT.create(MyMessages.class);

    signInLink = new Anchor(myConstants.login());
    signOutLink = new Anchor(myConstants.signInAsOtherUser());


    if (Document.get() != null) {
      Document.get().setTitle(myConstants.pacoPageTitle());
    }

    checkLoginStatusAndLoadPage();
  }

  private void checkLoginStatusAndLoadPage() {
    LoginServiceAsync loginService = GWT.create(LoginService.class);
    loginService.login(GWT.getHostPageBaseURL(), new AsyncCallback<LoginInfo>() {
      public void onFailure(Throwable error) {
        //Window.alert(myConstants.failedToLogin());
      }

      public void onSuccess(LoginInfo result) {
        loginInfo = result;
        if (loginInfo.isLoggedIn()) {
          Runnable onLoad = new Runnable() {
            @Override
            public void run() {
              loginPanel.setVisible(false);
              createHomePage();
              signOutLink.setHref(loginInfo.getLogoutUrl());
            }
          };

          loadMapApi(onLoad);
        } else {
          loadLogin();
        }
      }
    });
  }

  private void loadMapApi(Runnable runnable) {
    boolean sensor = true;

    // load all the libs for use in the maps
    ArrayList<LoadLibrary> loadLibraries = new ArrayList<LoadApi.LoadLibrary>();
    loadLibraries.add(LoadLibrary.ADSENSE);
    loadLibraries.add(LoadLibrary.DRAWING);
    loadLibraries.add(LoadLibrary.GEOMETRY);
    loadLibraries.add(LoadLibrary.PANORAMIO);
    loadLibraries.add(LoadLibrary.PLACES);
    loadLibraries.add(LoadLibrary.WEATHER);
    //loadLibraries.add(LoadLibrary.VISUALIZATION);
    LoadApi.go(runnable, loadLibraries, sensor);
  }
  private void loadLogin() {
    loginPanel.setStyleName("front_page");
    HTML index2Html = null;

    index2Html = new HomePageLocaleHelper().getLocalizedResource();

    signInLink.setHref(loginInfo.getLoginUrl());
    signInLink.setStyleName("paco-Login");
    signInLink.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

    loginPanel.add(signInLink);

    loginPanel.setVisible(true);

    loginPanel.add(index2Html);
    RootPanel.get().add(loginPanel);
    RootPanel.get().add(new HTML("<div style=\"text-align:center;\"><a href=\"/privacypolicy.html\">Privacy Policy</a></div>"));
  }

  class HomePageLocaleHelper extends GWTLocaleHelper<HTML> {

    protected HTML getEnVersion() {
      return new HTML(resources.indexHtml().getText());
    }

    protected HTML getJaVersion() {
      return new HTML(resources.indexHtml_ja().getText());
    }

    protected HTML getFiVersion() {
      return new HTML(resources.helpHtml_ja().getText());
    }

    protected HTML getPtVersion() {
      return new HTML(resources.helpHtml_pt().getText());
    }
  };



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
    leftSidePanel = new ScrollPanel(experimentPanel);
    horizontalPanel.add(leftSidePanel);
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
    //contentPanel.setSize("550px", "325px");
    rootPanel.add(new HTML("<div style=\"text-align:center;\"><a href=\"/privacypolicy.html\">Privacy Policy</a></div>"));
    loadJoinedExperiments();

    createCallbackForGviz();
  }

  private void createStatusPanelOnMenubar(HorizontalPanel menuPanel) {
    statusLabel = new Label(myConstants.loading());
    statusLabel.setStyleName("paco-Loading-Panel");
    statusLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

    statusLabel.setSize("80px", "26px");
    statusLabel.setVisible(false);

    menuPanel.add(statusLabel);
  }

  private HorizontalPanel createMenuBar() {
    HorizontalPanel menuPanel = new HorizontalPanel();
    mainPanel.add(menuPanel);
    Image pacoLogo = new Image(resources.pacoSmallLogo());
    pacoLogo.setStylePrimaryName("paco-Logo");
    menuPanel.add(pacoLogo);

    VerticalPanel rootMenuAndGreetingBar = new VerticalPanel();
    menuPanel.add(rootMenuAndGreetingBar);
    MenuBar rootMenuBar = new MenuBar(false);
    rootMenuAndGreetingBar.add(rootMenuBar);

    Label greeting = new Label(myMessages.hello(loginInfo.getEmailAddress()));
    greeting.setStyleName("paco-Name-Greeting");
    //greeting.setSize("200px", "20px");
    rootMenuAndGreetingBar.add(greeting);


    MenuBar joinedSubMenuBar = new MenuBar(true);
    MenuItem joinedMenuItem = new MenuItem(myConstants.joinedExperiments(), false, joinedSubMenuBar);
    MenuItem mntmShowAllJoined = new MenuItem(myConstants.showAll(), false, new Command() {
      public void execute() {
        loadJoinedExperiments();
      }
    });
    joinedSubMenuBar.addItem(mntmShowAllJoined);

    MenuItem mntmFindExperiments = new MenuItem(myConstants.findExperiments(), false, new Command() {
      public void execute() {
        findExperiments();
      }
    });
    mntmFindExperiments.setEnabled(true);
    joinedSubMenuBar.addItem(mntmFindExperiments);
    rootMenuBar.addItem(joinedMenuItem);

    MenuBar adminMenuBar = new MenuBar(true);
    MenuItem adminMenuItem = new MenuItem(myConstants.administerExperiments(), false, adminMenuBar);
    MenuItem mntmShowAllAdmin = new MenuItem(myConstants.showAll(), false, new Command() {
      public void execute() {
        loadAdministeredExperiments(false);
      }
    });
    adminMenuBar.addItem(mntmShowAllAdmin);

    MenuItem mntmCreateNew = new MenuItem(myConstants.createNew(), false, new Command() {
      public void execute() {
        createNewExperiment();
      }
    });
    adminMenuBar.addItem(mntmCreateNew);
    rootMenuBar.addItem(adminMenuItem);
    // //////////////////

    MenuItem mntmQR_Code = new MenuItem(myConstants.getAndroid(), false, new Command() {
      public void execute() {
        showAndroidDownloadPage();
      }
    });
    rootMenuBar.addItem(mntmQR_Code);


    // ////////////////
    MenuBar helpMenuBar = new MenuBar(true);
    MenuItem helpMenuItem = new MenuItem(myConstants.help(), false, helpMenuBar);
    rootMenuBar.addItem(helpMenuItem);

    MenuItem helpContentsMenuItem = new MenuItem(myConstants.userGuide(), false, new Command() {
      public void execute() {
        launchHelp();
      }
    });
    //helpContentsMenuItem.setEnabled(false);
    helpMenuBar.addItem(helpContentsMenuItem);

    MenuItem aboutMenuItem = new MenuItem(myConstants.about(), false, new Command() {
      public void execute() {
        launchAbout();
      }
    });
    aboutMenuItem.setEnabled(false);
    helpMenuBar.addItem(aboutMenuItem);

    // logout

    MenuItem mntmLogout = new MenuItem(myConstants.logout(), false, new Command() {
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
    setContentTitle(myConstants.downloadAppTitle());
    VerticalPanel dl = new VerticalPanel();

    HTML barCodeLabel = new HTML(myConstants.downloadAppStep1a());
    barCodeLabel.setStyleName("paco-HTML-Large");
    dl.add(barCodeLabel);

    HTML barCodeLabel2 = new HTML(myConstants.downloadAppStep2a());
    barCodeLabel2.setStyleName("paco-HTML-Large");
    dl.add(barCodeLabel2);
    dl.add(new Image(resources.qrcode()));

    HTML downloadLink = new HTML(myMessages.downloadAppStep2b("http://play.google.com/store/apps/details?id=com.pacoapp.paco"));
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
    setContentTitle(myConstants.findExperiments());
    contentPanel.clear();
    experimentPanel.setVisible(true);
    getExperiments(false, false, true);
  }

  private void setContentTitle(String text) {
    listTitle.setHTML(text);
  }

  protected void createNewExperiment() {
    setContentTitle(myConstants.createNewExperiment());
    contentPanel.clear();
    experimentPanel.setVisible(false);
    ExperimentDAO experiment = new ExperimentDAO();
    showExperimentDetailPanel(experiment, true, false);
  }

  protected void launchAbout() {
    setContentTitle(myConstants.about() + " PACO");
    contentPanel.clear();
    experimentPanel.setVisible(false);
  }

  protected void launchHelp() {
    setContentTitle(myConstants.help());
    contentPanel.clear();
    experimentPanel.setVisible(false);
    HelpPage hp = new HelpPage(this);
    contentPanel.add(hp);
  }

  protected void loadJoinedExperiments() {
    statusLabel.setVisible(true);
    setContentTitle(myConstants.joinedExperiments());
    contentPanel.clear();
    flexTable.clear();
    experimentPanel.setVisible(true);
    toggleExperimentList(true);
    getExperiments(true, false, false);
  }

  protected void loadAdministeredExperiments(boolean experimentsDirty) {
    statusLabel.setVisible(true);
    setContentTitle(myConstants.administerExperiments());
    contentPanel.clear();
    flexTable.clear();
    experimentPanel.setVisible(true);
    toggleExperimentList(true);
    getExperiments(false, experimentsDirty, false);
  }

  private void addRowsToTable(List<ExperimentRow> exRows) {
    int row = 0;
    for (ExperimentRow experimentRow : exRows) {
      flexTable.setWidget(row++, 0, experimentRow);
    }
  }

  private List<ExperimentRow> createExperimentRows(boolean joinedExperimentsView, List<ExperimentDAO> experiments, boolean findExperimentsView) {
    List<ExperimentRow> exRows = new ArrayList<ExperimentRow>();
    for (ExperimentDAO experiment : experiments) {
      exRows.add(new ExperimentRow(resources, experiment, this, joinedExperimentsView, findExperimentsView));
    }
    return exRows;
  }

  private void getExperiments(final boolean joinedExperimentsView, final boolean experimentsDirty, final boolean findExperimentsView) {
    AsyncCallback<ExperimentQueryResult> callback = new AsyncCallback<ExperimentQueryResult>() {
      @Override
      public void onFailure(Throwable caught) {
        Window.alert(myMessages.loadExperimentsFailed(caught.getMessage()));
        statusLabel.setVisible(false);
      }

      @Override
      public void onSuccess(ExperimentQueryResult result) {
        List<ExperimentDAO> experimentResults = result.getExperiments();
        Collections.sort(experimentResults, new Comparator<ExperimentDAO>() {

          @Override
          public int compare(ExperimentDAO arg0, ExperimentDAO arg1) {
            return arg0.getTitle().toLowerCase().compareTo(arg1.getTitle().toLowerCase());
          }

        });
        experiments = experimentResults;
        cursor = result.getCursor();
        if (experiments == null || experiments.size() == 0) {
          Window.alert(myConstants.noExperimentsReturned());
        } else {
          addRowsToTable(createExperimentRows(joinedExperimentsView, experiments, findExperimentsView));
        }
        statusLabel.setVisible(false);

      }
    };
    if (findExperimentsView) {
      pacoService.getMyJoinableExperiments(TimeUtil.getTimezone(), null, null, callback);
//      pacoService.getAllJoinableExperiments(TimeUtil.getTimezone(), null, null, callback);
    } else if (joinedExperimentsView) {
      pacoService.getUsersJoinedExperiments(DEFAULT_LIMIT_SIZE, cursor, callback);
    } else {
      pacoService.getUsersAdministeredExperiments(DEFAULT_LIMIT_SIZE, cursor, callback);
    }
  }

  public void eventFired(int experimentCode, ExperimentDAO experiment, boolean joined, boolean findView) {
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
          joinedStr = ":who=" + loginInfo.getEmailAddress().toLowerCase();
        }
        Window.open(
            "/events?csv&q='experimentId=" + experiment.getId() + joinedStr + "'", "_blank","");
        break;
      case ExperimentListener.DELETE_CODE:
        if (Window.confirm(myConstants.areYouSureYouWantToDelete())) {
          deleteExperiment(experiment, joined);
        }
        break;
      case ExperimentListener.EDIT_CODE:
        contentPanel.clear();
        statusLabel.setVisible(true);
        toggleExperimentList(false);
        showExperimentDetailPanel(experiment, !joined, findView);
        break;
      case ExperimentListener.SAVED:
        saveToServer(experiment);
        break;
      case ExperimentListener.CANCELED:
        contentPanel.clear();
        toggleExperimentList(true);
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
        toggleExperimentList(false);
        showExperimentDetailPanel(experiment, true, false);
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
        toggleExperimentList(true);
        break;
      case ExperimentListener.EXPERIMENT_RESPONSE_CANCELED_CODE:
        contentPanel.clear();
        toggleExperimentList(true);
        break;
      case ExperimentListener.SHOW_EXPERIMENT_RESPONSE_CODE:
        contentPanel.clear();
        toggleExperimentList(false);
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
      case ExperimentListener.JOINED_CODE:
        contentPanel.clear();
        joinExperiment(experiment);
        break;


    }
  }

  private void joinExperiment(ExperimentDAO experiment) {
    statusLabel.setVisible(true);

    pacoService.joinExperiment(experiment.getId(), new AsyncCallback<Boolean>() {

      @Override
      public void onFailure(Throwable caught) {
        Window.alert(myConstants.failure());
        statusLabel.setVisible(false);

      }

      @Override
      public void onSuccess(Boolean result) {
        if (result) {
          Window.alert(myConstants.success());
        } else {
          Window.alert(myConstants.failureToJoinExperiment());
        }
        statusLabel.setVisible(false);

      }
    });


  }

  private void toggleExperimentList(boolean enable) {
    leftSidePanel.setVisible(enable);
    listTitle.setVisible(enable);
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
      Button ok = new Button(myConstants.ok());
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
                Window.alert(myConstants.success());
              }

            };
            pacoService.setReferencedExperiment(referringExperimentId, referencedExperimentId, callback);
          }
          ExperimentReferenceDialog.this.hide();

        }
      });
      Button cancel = new Button(myConstants.cancel());
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
    experiment.setTitle(myConstants.copyOfExperimentTitlePrefix() + experiment.getTitle());
    experiment.getSignalingMechanisms()[0].setId(null);
    experiment.setPublished(false);
    experiment.setPublishedUsers(new String[]{});
    experiment.setAdmins(new String[]{});

    for(InputDAO input : experiment.getInputs()) {
      input.setId(null);
    }
    for (FeedbackDAO feedback : experiment.getFeedback()) {
      feedback.setId(null);
    }
  }


  private void saveToServer(ExperimentDAO experiment) {
    statusLabel.setVisible(true);

    pacoService.saveExperiment(experiment, TimeUtil.getTimezone(), new AsyncCallback<Void>() {

      // PRIYA - see how this is
      @Override
      public void onFailure(Throwable caught) {
        Window.alert(myConstants.failure() + ": " + myConstants.saveToServerFailure()
                     + "\n" + myConstants.errorMessage() + ": " + caught.getMessage());
        statusLabel.setVisible(false);

      }

      @Override
      public void onSuccess(Void result) {
        Window.alert(myConstants.success());
        contentPanel.clear();
        loadAdministeredExperiments(true);

        statusLabel.setVisible(false);
      }
    });
  }

  private void softDeleteExperiment(ExperimentDAO experiment) {
    statusLabel.setVisible(true);
    // toggle
    experiment.setDeleted(experiment.getDeleted() == null || !experiment.getDeleted());

    pacoService.saveExperiment(experiment, TimeUtil.getTimezone(), new AsyncCallback<Void>() {

      @Override
      public void onFailure(Throwable caught) {
        Window.alert(myConstants.failure());
        statusLabel.setVisible(false);

      }

      @Override
      public void onSuccess(Void result) {
        Window.alert(myConstants.success());
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
    pacoService.deleteExperiment(experiment, new AsyncCallback<Boolean>() {

      @Override
      public void onFailure(Throwable caught) {
        Window.alert(myConstants.errorDeletingExperiment());
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
        Window.alert(myConstants.couldNotRetrieveCharts());
        statusLabel.setVisible(false);

      }

      @Override
      public void onSuccess(List<EventDAO> eventList) {
        if (eventList.size() == 0) {
          Window.alert(myConstants.noResultsForQuery());
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
      queryText += ":who=" + loginInfo.getEmailAddress().toLowerCase();
    }
    pacoService.eventSearch(queryText, callback);
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
    pacoService.referencedExperiment(experiment.getId(), referencedCheckCallback);

  }

  protected void showReferredExperimentExecutor(final ExperimentDAO experiment, final ExperimentDAO referencedExperiment) {
    statusLabel.setVisible(true);

    // TODO rewrite this with two futures that join() before calling the EndofDayExecutor.
    AsyncCallback<List<EventDAO>> callback = new AsyncCallback<List<EventDAO>>() {

      @Override
      public void onFailure(Throwable caught) {
        Window.alert(myMessages.loadReferencedEventsFailed(caught.getMessage()));
        statusLabel.setVisible(false);

      }

      @Override
      public void onSuccess(final List<EventDAO> referencedEventList) {
        if (referencedEventList.size() == 0) {
          Window.alert(myConstants.noEventsFoundForReferredExperiment());
          statusLabel.setVisible(false);
          return;
        }
        AsyncCallback<Map<Date, EventDAO>> referringCallback = new AsyncCallback<Map<Date, EventDAO>>() {

          @Override
          public void onFailure(Throwable caught) {
            Window.alert(myMessages.loadReferencedEventsFailed(caught.getMessage()));
            statusLabel.setVisible(false);

          }

          @Override
          public void onSuccess(Map<Date, EventDAO> eodEventList) {
            AbstractExperimentExecutorPanel ep = new EndOfDayExperimentExecutorPanel(Main.this, pacoService,
                                                                                     experiment, referencedEventList, eodEventList, referencedExperiment);
            contentPanel.add(ep);
            statusLabel.setVisible(false);
          }
        };

        String queryText = "experimentId=" + experiment.getId() + ":who=" + loginInfo.getEmailAddress().toLowerCase();
        pacoService.getEndOfDayEvents(queryText, referringCallback);
      }
    };
    String queryText = "experimentId=" + referencedExperiment.getId() + ":who=" + loginInfo.getEmailAddress().toLowerCase();
    pacoService.eventSearch(queryText, callback);

  }

  protected void showRegularExperimentEntry(ExperimentDAO experiment, boolean joined) {
    statusLabel.setVisible(true);
    AbstractExperimentExecutorPanel ep = new ExperimentExecutorPanel(this, pacoService, experiment);
    contentPanel.add(ep);
    statusLabel.setVisible(false);
  }

  private void showExperimentDetailPanel(ExperimentDAO experiment, boolean joined, boolean findView) {
    statusLabel.setVisible(true);
    if (findView) {
      ExperimentJoinPanel ep = new ExperimentJoinPanel(experiment, loginInfo, this);
      contentPanel.add(ep);
    } else if (!joined) {
      ExperimentDescriptionPanel ep = new ExperimentDescriptionPanel(experiment, loginInfo, this);
      contentPanel.add(ep);
    } else {
      ExperimentDefinitionPanel ep = new ExperimentDefinitionPanel(experiment, loginInfo, this);
      contentPanel.add(ep);
    }

    statusLabel.setVisible(false);
  }

  private void showStatsPanel(final ExperimentDAO experiment, final boolean joined) {
    statusLabel.setVisible(true);
    AsyncCallback<ExperimentStatsDAO> callback = new AsyncCallback<ExperimentStatsDAO>() {

      @Override
      public void onFailure(Throwable caught) {
        Window.alert(myMessages.loadEventsForExperimentFailed(experiment.getTitle()) + "\n"
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
    pacoService.statsForExperiment(experiment.getId(), joined, callback);


  }
}
