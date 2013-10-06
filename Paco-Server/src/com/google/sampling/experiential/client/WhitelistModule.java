package com.google.sampling.experiential.client;

import java.util.List;

import com.google.common.base.Strings;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.sampling.experiential.shared.LoginInfo;
import com.google.sampling.experiential.shared.LoginService;
import com.google.sampling.experiential.shared.LoginServiceAsync;
import com.google.sampling.experiential.shared.WhitelistService;
import com.google.sampling.experiential.shared.WhitelistServiceAsync;

public class WhitelistModule implements EntryPoint {

  private Label statusLabel;
  private FlexTable flexTable;
  Images resources;
  private HTML listTitle;
  private VerticalPanel contentPanel;
  private VerticalPanel mainPanel;
  private VerticalPanel experimentPanel;
  
  private WhitelistServiceAsync mapService = GWT.create(WhitelistService.class);

  private LoginInfo loginInfo = null;
  private Anchor signInLink = new Anchor("Login");
  private Anchor signOutLink = new Anchor("Logout");

  private FlowPanel loginPanel = new FlowPanel();
  private Label loginLabel =
      new Label("Please sign in to your Google Account " + "to access the application.");

  protected MyConstants myConstants;
  protected MyMessages myMessages;
  private TextBox emailTextBox;
  private VerticalPanel userList;

///
  public void onModuleLoad() {
    if (GWT.getHostPageBaseURL().startsWith("http://") && !(GWT.getHostPageBaseURL().contains("127.0.0.1") ||
        GWT.getHostPageBaseURL().contains("localhost"))) {
      Window.Location.assign(GWT.getHostPageBaseURL().replace("http://", "https://") );
    }
    resources = GWT.create(Images.class);
    myConstants = GWT.create(MyConstants.class);
    myMessages = GWT.create(MyMessages.class);


    if (Document.get() != null) {
      Document.get().setTitle(myConstants.pacoPageTitle());
    }
    
    checkLoginStatusAndLoadPage();
  }

  private void checkLoginStatusAndLoadPage() {
    LoginServiceAsync loginService = GWT.create(LoginService.class);
    loginService.login(GWT.getHostPageBaseURL(), new AsyncCallback<LoginInfo>() {
      public void onFailure(Throwable error) {
      }

      public void onSuccess(LoginInfo result) {
        loginInfo = result; 
        if (loginInfo.isLoggedIn() && loginInfo.isWhitelisted()) {
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
    
    loginPanel.setVisible(true);
    if (loginInfo.isLoggedIn() && !loginInfo.isWhitelisted()) {
      Window.alert(myConstants.notWhiteListed());
      signInLink.setHref(loginInfo.getLogoutUrl());
    }
    loginPanel.add(index2Html);
    RootPanel.get().add(loginPanel);
  }

  private void createHomePage() {
    RootPanel rootPanel = RootPanel.get();

    mainPanel = new VerticalPanel();
    mainPanel.setSpacing(2);
    rootPanel.add(mainPanel);

    listTitle = new HTML("Paco Whitelist Manager");
    mainPanel.add(listTitle);
    listTitle.setStyleName("paco-HTML-Large");
    listTitle.setWordWrap(false);
    listTitle.setSize("270px", "22");

    mainPanel.setCellHorizontalAlignment(listTitle, HasHorizontalAlignment.ALIGN_CENTER);
    
    HorizontalPanel addUserPanel = createAddUserPanel();
    mainPanel.add(addUserPanel);
    
    VerticalPanel userListPanel = new VerticalPanel();
    
    HorizontalPanel listTitlePanel = new HorizontalPanel();  
    userListPanel.add(listTitlePanel);
    Label userListTitleLabel = new Label("Users");
    listTitlePanel.add(userListTitleLabel);
    
    Button loadUsersButton = new Button("Pull Users from Server");
    listTitlePanel.add(loadUsersButton);
    loadUsersButton.addClickHandler(new ClickHandler() {
      
      @Override
      public void onClick(ClickEvent event) {
        getUsersFromDb(); 
      }
    });
    
    userList = new VerticalPanel();
    userListPanel.add(userList);
    
    mainPanel.add(userListPanel);

    
  }

  private HorizontalPanel createAddUserPanel() {
    HorizontalPanel addUserPanel = new HorizontalPanel();    
    
    addUserPanel.add(new Label("Email:"));
    emailTextBox = new TextBox();
    addUserPanel.add(emailTextBox);
    Button addUserbutton = new Button("Add User");
    addUserPanel.add(addUserbutton);
    addUserbutton.addClickHandler(new ClickHandler() {
      
      @Override
      public void onClick(ClickEvent event) {
        String email = emailTextBox.getText();
        if (!Strings.isNullOrEmpty(email)) {
          addUsersToDb(email);
        }
      }
    });
    return addUserPanel;
  }

  protected void addUsersToDb(String email) {
    WhitelistServiceAsync wls = GWT.create(WhitelistService.class);
    wls.addUser(email, new AsyncCallback<Void>() {

      @Override
      public void onFailure(Throwable caught) {
       Window.alert("Could not add user to whitelist: " + caught.getMessage());        
      }

      @Override
      public void onSuccess(Void result) {
        Window.alert(myConstants.success());
        emailTextBox.setText("");
      }
    
    });
  }
  
  protected void getUsersFromDb() {
    WhitelistServiceAsync wls = GWT.create(WhitelistService.class);
    wls.getWhitelist(new AsyncCallback<List<String>>() {

      @Override
      public void onFailure(Throwable caught) {
       Window.alert("Could not get whitelist: " + caught.getMessage());        
      }

      @Override
      public void onSuccess(List<String> result) {
        userList.clear();
        for (String email : result) {
          CheckBox checkBox = new CheckBox();
          Label label = new Label(email);
          HorizontalPanel w = new HorizontalPanel();
          w.add(checkBox);
          w.add(label);
          userList.add(w);
        }
      }
    
    });
  }


}
