package com.google.sampling.experiential.server;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;

public final class AuthenticatedServletFilter implements Filter {
  public static final Logger log = Logger.getLogger(AuthenticatedServletFilter.class.getName());

  @Override
  public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException,
                                                                                   ServletException {
    User user = AuthUtil.getWhoFromLogin();
    if (user == null) {
      AuthUtil.redirectUserToLogin((HttpServletRequest) req, (HttpServletResponse) resp);
      return;
    }
    AuthenticatedHttpServletRequest wrappedRequest = new AuthenticatedHttpServletRequest((HttpServletRequest) req);
    wrappedRequest.setUser(user);
    chain.doFilter(wrappedRequest, resp);
  }

  @Override
  public void destroy() {
  }

  @Override
  public void init(FilterConfig arg0) throws ServletException {
  }
}
