package com.google.sampling.experiential.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.appengine.api.ThreadManager;
import com.google.sampling.experiential.server.migration.StoredProc;
import com.google.sampling.experiential.server.migration.StoredProcLookupTable;
import com.pacoapp.paco.shared.util.Constants;

@SuppressWarnings("serial")
public class CloudSqlStoredProcServlet extends HttpServlet {
  public static final Logger log = Logger.getLogger(CloudSqlStoredProcServlet.class.getName());

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    doPost(req, resp);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    setCharacterEncoding(req, resp);
    resp.setContentType("text/plain");
    final String requestorEmail = getRequestorEmail(req);
    final String spName = req.getParameter("name");
    final String expId = req.getParameter("expId");
    String whoParam = HttpUtil.getParam(req, "who");
    
    final String jobId = spName + "_" +
            DigestUtils.md5Hex(requestorEmail +
            Long.toString(System.currentTimeMillis()));
    log.info("In backend for job: " + jobId);
    Boolean isAdmin = ExperimentAccessManager.isAdminForExperiment(requestorEmail, Long.parseLong(expId));
    log.info("logged in user is an admin of asked experiement: " + isAdmin);
    //if user is an admin of expt, then we get data for all users in that expt. if not we add it to who clause in query
    if (isAdmin) {
      whoParam = null;
    } 
    final String who = whoParam;

    final ReportJobStatusManager statusMgr = new ReportJobStatusManager();
    statusMgr.startReport(requestorEmail, jobId);

    final ClassLoader cl = getClass().getClassLoader();
    final Thread thread2 = ThreadManager.createBackgroundThread(new Runnable() {
      @Override
      public void run() {
        log.info("Backend running");
        String response = null;
        
        Thread.currentThread().setContextClassLoader(cl);
        try {
        
          if(spName!=null) {
            StoredProc storedProc = StoredProcLookupTable.getStoredProcByName(spName);
            if (storedProc != null) {
              response = storedProc.callStoredProc(jobId, Long.parseLong(expId), who);
            }
          }
          
          if (response != null) {
            statusMgr.completeReport(requestorEmail, jobId, response);
          } else {
            statusMgr.failReport(requestorEmail, jobId, "Check server logs for stacktrace");
          }
        } catch (Throwable e) {
          final String fullStack = getStackTraceAsString(e);
          final String string = fullStack.length()>700 ? fullStack.substring(0, 700): fullStack;
          statusMgr.failReport(requestorEmail, jobId, e.getClass() + "." + e.getMessage() +"\n" + string);
          log.severe("Could not run migration job: " + e.getMessage());
          log.severe("stacktrace: " + fullStack);
        }
      }
    });
    thread2.start();
    log.info("Leaving migration backend"+ jobId);
    resp.setContentType("text/plain;charset=UTF-8");
    resp.getWriter().println(jobId);    

  }
  
  private String getStackTraceAsString(Throwable e) {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream pw = new PrintStream(out);
    e.printStackTrace(pw);
    final String string = out.toString();
    return string;
  }
  
  private String getRequestorEmail(HttpServletRequest req) {
    String whoParam = HttpUtil.getParam(req, "who");
    if (whoParam == null) {
      throw new IllegalArgumentException("Must pass the who param");
    }
    return whoParam.toLowerCase();
  }

  private void setCharacterEncoding(HttpServletRequest req,
                                    HttpServletResponse resp) throws UnsupportedEncodingException {
    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");
  }

}