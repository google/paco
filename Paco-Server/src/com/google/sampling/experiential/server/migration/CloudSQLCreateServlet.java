package com.google.sampling.experiential.server.migration;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;
import com.google.sampling.experiential.server.AuthUtil;
import com.google.sampling.experiential.server.migration.dao.CloudSQLMigrationDao;
import com.google.sampling.experiential.server.migration.dao.impl.CloudSQLMigrationDaoImpl;
import com.pacoapp.paco.shared.util.Constants;
import com.pacoapp.paco.shared.util.ErrorMessages;

@SuppressWarnings("serial")
public class CloudSQLCreateServlet extends HttpServlet {
  public static final Logger log = Logger.getLogger(CloudSQLCreateServlet.class.getName());

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException,
      ServletException {
    setCharacterEncoding(req, resp);
    User user = AuthUtil.getWhoFromLogin();
    if (user == null) {
      AuthUtil.redirectUserToLogin(req, resp);
    } else if (AuthUtil.isUserAdmin()) {
      PrintWriter out = resp.getWriter();
      resp.setContentType("text/plain");
      String stepNo = req.getParameter("stepNo");
      CloudSQLMigrationDao dao = new CloudSQLMigrationDaoImpl();
      try {
        if (stepNo == null) {
          out.println(dao.createTables("one"));
          out.println(dao.createTables("two"));
        } else { 
          out.println(dao.createTables(stepNo));
        }
        
      } catch (SQLException e) {
        throw new ServletException(ErrorMessages.SQL_EXCEPTION.getDescription(),e);
      }
    } else {
      log.warning(ErrorMessages.UNAUTHORIZED_ACCESS.getDescription() + user + " is not an admin");
      resp.sendError(Constants.FORBIDDEN_STATUS);
    }
  }
  
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException,
  ServletException {
    doPost(req,resp);
  }
  
  private void setCharacterEncoding(HttpServletRequest req, HttpServletResponse resp)
          throws UnsupportedEncodingException {
    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");
  }
}