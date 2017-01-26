package com.google.sampling.experiential.server;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class CloudSQLCreateServlet extends HttpServlet {
//Todo authentication
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException,
      ServletException {
    PrintWriter out = resp.getWriter();
    resp.setContentType("text/plain");

    CloudSQLDao dao = new CloudSQLDaoImpl();
    out.println(dao.createTables());
   
  }
}