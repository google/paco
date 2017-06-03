package com.google.sampling.experiential.server.migration;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;
import com.google.sampling.experiential.server.AuthUtil;

@SuppressWarnings("serial")
public class CloudSQLCreateServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException,
      ServletException {
    setCharacterEncoding(req, resp);
    User user = AuthUtil.getWhoFromLogin();
    if (user == null) {
      AuthUtil.redirectUserToLogin(req, resp);
    } else {
      PrintWriter out = resp.getWriter();
      resp.setContentType("text/plain");
  
      CloudSQLMigrationDao dao = new CloudSQLMigrationDaoImpl();
      try{
        out.println(dao.createTables());
      }catch (SQLException e){
        throw new ServletException("SQL",e);
      }
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