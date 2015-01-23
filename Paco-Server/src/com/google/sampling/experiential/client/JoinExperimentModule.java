package com.google.sampling.experiential.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.sampling.experiential.shared.LoginInfo;
import com.google.sampling.experiential.shared.LoginService;
import com.google.sampling.experiential.shared.LoginServiceAsync;
import com.google.sampling.experiential.shared.PacoService;
import com.google.sampling.experiential.shared.PacoServiceAsync;
import com.google.sampling.experiential.shared.WhitelistService;
import com.google.sampling.experiential.shared.WhitelistServiceAsync;

public class JoinExperimentModule implements EntryPoint {

  private Label statusLabel;
  private FlexTable flexTable;
  Images resources;
  private HTML listTitle;
  private VerticalPanel contentPanel;
  private VerticalPanel mainPanel;
  private VerticalPanel experimentPanel;

  private WhitelistServiceAsync whitelistService = GWT.create(WhitelistService.class);
  private PacoServiceAsync mapService = GWT.create(PacoService.class);
  LoginInfo loginInfo = null;
  private Anchor signInLink = new Anchor("Login");
  private Anchor signOutLink = new Anchor("Logout");

  private FlowPanel loginPanel = new FlowPanel();
  private Label loginLabel =
      new Label("Please sign in to your Google Account " + "to access the application.");


  public void onModuleLoad() {
    if (GWT.getHostPageBaseURL().startsWith("http://") && !(GWT.getHostPageBaseURL().contains("127.0.0.1") ||
        GWT.getHostPageBaseURL().contains("localhost"))) {
      Window.Location.assign(GWT.getHostPageBaseURL().replace("http://", "https://")+"join.html");
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
        if (loginInfo.isLoggedIn()) {
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

    loginPanel.add(signInLink);
    loginPanel.add(index2Html);
    RootPanel.get().add(loginPanel);
  }



  private void createHomePage() {
    mapService = GWT.create(PacoService.class);

    RootPanel rootPanel = RootPanel.get();

    mainPanel = new VerticalPanel();
    mainPanel.setSpacing(2);
    rootPanel.add(mainPanel);

    listTitle = new HTML("");
    mainPanel.add(listTitle);
    listTitle.setStyleName("paco-HTML-Large");
    listTitle.setWordWrap(false);
    listTitle.setSize("270px", "22");

    mainPanel.setCellHorizontalAlignment(listTitle, HasHorizontalAlignment.ALIGN_CENTER);

    mainPanel.add(new ExperimentLandingPanel(this));

  }

  PacoServiceAsync getMapService() {
    return mapService;
  }

}
