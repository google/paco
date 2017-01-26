package com.google.sampling.experiential.server;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@SuppressWarnings("serial")
public class CloudSqlInsertServlet extends HttpServlet {
  public static final Logger log = Logger.getLogger(ExperimentServlet.class.getName());
//TODO authentication
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException,
  ServletException {
    doGet(req, resp);
  }
  
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException,
      ServletException {
    String requestBody = RequestProcessorUtil.getBody(req);

    String appIdHeader = req.getHeader("http.useragent");
    String pacoVersion = req.getHeader("paco.version");
    log.info("Paco version = " + pacoVersion);
    //should not send this event to cloud sql insert queue again
    String results = EventJsonUploadProcessor.create().processJsonEvents(true, requestBody, "pacotest100@gmail.com", appIdHeader, pacoVersion);
   
    if (req.getHeader("pacoProtocol") != null && req.getHeader("pacoProtocol").indexOf("4") == -1) {
      log.severe("oldProtocol " + req.getHeader("pacoProtocol") + " (iOS) results?");
      log.severe(results);
    }
    
    resp.getWriter().println(results);

  }
  
  
}