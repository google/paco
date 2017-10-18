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

import com.google.apphosting.api.ApiProxy;

public final class InternalRequestIdentificationFilter implements Filter {
  public static final Logger log = Logger.getLogger(InternalRequestIdentificationFilter.class.getName());
  //TODO: Should we respond with different messages for each of the scenario ??
  private static final String INVALID_HEADER = "Unauthorized : Invalid Header";
  private static final String INVALID_HEADER_MISMATCH = "Unauthorized : Header not matching";
  private static final String INVALID_HOST = "Unauthorized : Invalid host name";
  
  @Override
  public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException,
                                                                                   ServletException {
    String appIdFromHeader = ((HttpServletRequest)req).getHeader("X-Appengine-Inbound-Appid");
    log.info("IRI filter");
    ApiProxy.Environment env = ApiProxy.getCurrentEnvironment();
    
    if (appIdFromHeader == null) {
      log.warning(INVALID_HEADER);
      resp.getWriter().write(INVALID_HEADER);
      return;
    }
    String appIdFullHostName = env.getAttributes().get("com.google.appengine.runtime.default_version_hostname").toString();
   
    if (appIdFullHostName != null) {
      String[] appIdPartialHostName = appIdFullHostName.split("\\.");
      String appIdFromRuntime = appIdPartialHostName[0];
      if (!appIdFromHeader.equalsIgnoreCase(appIdFromRuntime)) {
        log.warning(INVALID_HEADER_MISMATCH);
        resp.getWriter().write(INVALID_HEADER_MISMATCH);
      }
    } else {
      log.warning(INVALID_HOST);
      resp.getWriter().write(INVALID_HOST);
    }
    chain.doFilter(req, resp);
  }

  @Override
  public void destroy() {
  }

  @Override
  public void init(FilterConfig arg0) throws ServletException {
  }
}
