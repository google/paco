package com.google.sampling.experiential.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.google.appengine.api.users.User;

/**
 * Wraps a HttpServletResponse adding an App Engine user property and setter.
 */
public class AuthenticatedHttpServletRequest extends HttpServletRequestWrapper {
  public User user;

  public AuthenticatedHttpServletRequest(HttpServletRequest response) {
    super(response);
  }

  public void setUser(User pacoUser) {
    user = pacoUser;
  }
}
