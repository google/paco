package com.google.sampling.experiential.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;

import com.google.appengine.api.users.User;
import com.google.sampling.experiential.model.ApplicationUsage;
import com.google.sampling.experiential.model.ApplicationUsageProcessor;
import com.google.sampling.experiential.model.ApplicationUsageRaw;
import com.pacoapp.paco.shared.model2.JsonConverter;
import com.pacoapp.paco.shared.model2.SPRequest;
import com.pacoapp.paco.shared.util.ErrorMessages;
import com.pacoapp.paco.shared.util.QueryJsonParser;

@SuppressWarnings("serial")
public class CloudSqlStoredProcServlet extends HttpServlet {
  public static final Logger log = Logger.getLogger(CloudSqlStoredProcServlet.class.getName());
  private static final String SUCCESS = "Success";
  private static final String FAILURE = "Failure";

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    throw new ServletException(ErrorMessages.METHOD_NOT_SUPPORTED.getDescription());
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    setCharacterEncoding(req, resp);
    User user = AuthUtil.getWhoFromLogin();
    String loggedInUser = null;
    SPRequest request = null;
    AppUsageSPResponse storedProcResp = new AppUsageSPResponse();
    ApplicationUsageProcessor auProcessor = new ApplicationUsageProcessor();
    ObjectMapper mapper = JsonConverter.getObjectMapper();

    if (user == null) {
      AuthUtil.redirectUserToLogin(req, resp);
    } else {
      loggedInUser = AuthUtil.getEmailOfUser(req, user);
      resp.setContentType("text/plain");
      CloudSQLDao impl = new CloudSQLDaoImpl();
      String reqBody = RequestProcessorUtil.getBody(req);
      // TODO Date fields conversion to UTC
      // TODO preprocesor validation
      // TODO ACL
      try {
        // process into sp request obj
        request = QueryJsonParser.parseStoredProcRequestFromJson(reqBody);
        // get the raw data from db
        Map<String, List<ApplicationUsageRaw>> usageMap = impl.getAppUsageTopN(request);
        // process the data and get the required data in correct format
        ApplicationUsage au = auProcessor.getTotalAppStartsAndDuration(usageMap);
        storedProcResp.setStatus(SUCCESS);
        storedProcResp.setAppUsage(au);
        String results = mapper.writeValueAsString(storedProcResp);
        resp.getWriter().println(results);
      } catch (SQLException e) {
        log.info("sql exception" + e);
        storedProcResp.setStatus(FAILURE);
        sendErrorMessage(resp, mapper, ErrorMessages.SQL_EXCEPTION.getDescription() + e);
      } catch (JSONException jsone) {
        log.info("json exception" + jsone);
        storedProcResp.setStatus(FAILURE);
        sendErrorMessage(resp, mapper, ErrorMessages.JSON_EXCEPTION.getDescription() + jsone);
      }
    }
  }

  private void sendErrorMessage(HttpServletResponse resp, ObjectMapper mapper,
                                String errorMessage) throws JsonGenerationException, JsonMappingException, IOException {
    AppUsageSPResponse spResponse = new AppUsageSPResponse();
    spResponse.setMessage(errorMessage);
    spResponse.setStatus(FAILURE);
    String results = mapper.writeValueAsString(spResponse);
    resp.getWriter().println(results);
  }

  private void setCharacterEncoding(HttpServletRequest req,
                                    HttpServletResponse resp) throws UnsupportedEncodingException {
    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");
  }
}