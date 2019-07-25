package com.google.sampling.experiential.server;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.oauth.OAuthServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import com.google.appengine.api.utils.SystemProperty;

public class AuthUtil {
  private static final String EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";
  
  public static final Logger log = Logger.getLogger(AuthUtil.class.getName());

  private AuthUtil() {

  }

  public static User getWhoFromLogin() {
      UserService userService = UserServiceFactory.getUserService();
      User currentUser = userService.getCurrentUser();
      if (currentUser == null) {
        try {
          currentUser = OAuthServiceFactory.getOAuthService().getCurrentUser(EMAIL_SCOPE);
        } catch (OAuthRequestException e) {
          log.info("OAuthService exception: " + e.getMessage());
          //e.printStackTrace();
        }
      }
      return currentUser;
  }

  public static void redirectUserToLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.sendRedirect(UserServiceFactory.getUserService().createLoginURL(req.getRequestURI()));
  }

  public static boolean isUserAdmin() {
    try {
      return UserServiceFactory.getUserService().isUserAdmin() || OAuthServiceFactory.getOAuthService().isUserAdmin();
    } catch (OAuthRequestException e) {
      e.printStackTrace();
      return false;
    }
  }

  public static String getEmailOfUser(HttpServletRequest req, User user) {
    String email = user != null ? user.getEmail() : null;
    if (email == null) {
      throw new IllegalArgumentException("User not logged in");
    }
    if (EnvironmentUtil.isDevInstance()) {
      if ("example@example.com".equalsIgnoreCase(email)) {
        //throw new IllegalArgumentException("You need to specify a test acct to return when testing mobile clients.");
        // uncomment the line below and put in the test acct. This is necessary because the dev appengine server
        // only returns example@example.com as the user!!
        return "bobevans999@gmail.com";
      } else {
        User currentUser = UserServiceFactory.getUserService().getCurrentUser();
        if (currentUser != null) {
          return currentUser.getEmail().toLowerCase();
        } else {
          return null;
        }
      }
    }
    return email.toLowerCase();
  }

  public static boolean isDevInstance() {
    return SystemProperty.environment.value() == SystemProperty.Environment.Value.Development;
  }

}
