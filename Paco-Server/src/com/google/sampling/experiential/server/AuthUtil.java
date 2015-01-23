package com.google.sampling.experiential.server;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.oauth.OAuthServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class AuthUtil {
  private static final String EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";

  private AuthUtil() {

  }

  public static User getWhoFromLogin() {
      UserService userService = UserServiceFactory.getUserService();
      User currentUser = userService.getCurrentUser();
      if (currentUser == null) {
        try {
          currentUser = OAuthServiceFactory.getOAuthService().getCurrentUser(EMAIL_SCOPE);
        } catch (OAuthRequestException e) {
          e.printStackTrace();
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

}
