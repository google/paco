package com.google.sampling.experiential.server;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.sampling.experiential.shared.EventDAO;


@SuppressWarnings("serial")
public class CloudSqlServlet extends HttpServlet {
  public static final Logger log = Logger.getLogger(ExperimentServlet.class.getName());

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException,
  ServletException {
    doGet(req, resp);
  }
  
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException,
      ServletException {
    log.info("in cloudsql");
    CloudSQLDao dao = new CloudSQLDaoImpl();
    
    EventDAO event = RequestProcessorUtil.getEventDAO(req);
    dao.insertEvent(event);
    dao.insertOutputs(event);
  }
}