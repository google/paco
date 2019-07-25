package com.google.sampling.experiential.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
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
import com.google.sampling.experiential.server.invitations.InvitationParticipantServlet;
import com.google.sampling.experiential.shared.EventDAO;
import com.pacoapp.paco.shared.model2.JsonConverter;
import com.pacoapp.paco.shared.model2.SQLQuery;
import com.pacoapp.paco.shared.util.Constants;
import com.pacoapp.paco.shared.util.ErrorMessages;
import com.pacoapp.paco.shared.util.QueryJsonParser;

import net.sf.jsqlparser.JSQLParserException;

@SuppressWarnings("serial")
public class CloudSqlSearchServlet extends HttpServlet {
  public static final Logger log = Logger.getLogger(CloudSqlSearchServlet.class.getName());
  
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    throw new ServletException(ErrorMessages.METHOD_NOT_SUPPORTED.getDescription());
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    setCharacterEncoding(req, resp);
    User user = AuthUtil.getWhoFromLogin();
    String loggedInUser = null;
    boolean oldFormatFlag = Constants.USE_OLD_SEARCH_QUERY;
    ObjectMapper mapper = JsonConverter.getObjectMapper();
    
    if (user == null) {
      AuthUtil.redirectUserToLogin(req, resp);
    } else {
      loggedInUser = AuthUtil.getEmailOfUser(req, user);
      SQLQuery sqlQueryObj = null;
      // NOTE: Group by, having and projection columns related functionality can be toggled on and off with the following flag 
      boolean enableGrpByAndProjection = true;
      try {
        String postBodyString = RequestProcessorUtil.getBody(req);
        Float pacoProtocol = RequestProcessorUtil.getPacoProtocolVersionAsFloat(req);
        sqlQueryObj = QueryJsonParser.parseSqlQueryFromJson(postBodyString, enableGrpByAndProjection);
        SearchQuery searchQuery = QueryFactory.createSearchQuery(sqlQueryObj, pacoProtocol);
        long startTime = System.currentTimeMillis();
        PacoResponse pr = searchQuery.process(loggedInUser, oldFormatFlag);
        long diff = System.currentTimeMillis() - startTime;
        log.info("complete search qry took " + diff + " seconds");
        
        if (searchQuery instanceof AllFieldsSearchQuery) {
          log.info("We are in an AllFieldsSearchQuery result");
          final List<EventDAO> events = ((EventQueryStatus)pr).getEvents();
          boolean anon = InvitationParticipantServlet.getAnonFromReq(req);
          boolean inlineBlobs = InvitationParticipantServlet.getInlineBlobsFromReq(req);
          boolean fullBlobAddress = InvitationParticipantServlet.getFullBlobAddressFromReq(req, true);
          String json = EventJsonDownloader.jsonifyEventDAOs(anon, inlineBlobs, pacoProtocol, events, null, fullBlobAddress);
          resp.setContentType("text/json");
          resp.getWriter().println(json);
        } else {
          log.info("We are NOT in an AllFieldsSearchQuery result");
          resp.setContentType("text/json");
          resp.getWriter().println(pr);
        }
      } catch (JSONException jsonEx) {
        String exceptionString = ExceptionUtil.getStackTraceAsString(jsonEx);
        log.warning( ErrorMessages.JSON_EXCEPTION.getDescription() + exceptionString);
        sendErrorMessage(resp, mapper, ErrorMessages.JSON_EXCEPTION.getDescription() + exceptionString);
        return;
      } catch (JSQLParserException e) {
        String exceptionString = ExceptionUtil.getStackTraceAsString(e);
        log.warning( ErrorMessages.ADD_DEFAULT_COLUMN_EXCEPTION.getDescription() + exceptionString);
        sendErrorMessage(resp, mapper, ErrorMessages.ADD_DEFAULT_COLUMN_EXCEPTION.getDescription() + exceptionString);
        return;
      } catch (SQLException sqle) {
        String exceptionString = ExceptionUtil.getStackTraceAsString(sqle);
        log.warning( ErrorMessages.SQL_EXCEPTION.getDescription() + exceptionString);
        sendErrorMessage(resp, mapper, ErrorMessages.SQL_EXCEPTION.getDescription() + exceptionString);
        return;
      } catch (IOException ioe) {
        String exceptionString = ExceptionUtil.getStackTraceAsString(ioe);
        log.warning( ErrorMessages.IO_EXCEPTION.getDescription() + exceptionString);
        sendErrorMessage(resp, mapper, ErrorMessages.IO_EXCEPTION.getDescription() + exceptionString);
        return;
      } catch (ParseException e) {
        String exceptionString = ExceptionUtil.getStackTraceAsString(e);
        log.warning( ErrorMessages.TEXT_PARSE_EXCEPTION.getDescription() + exceptionString);
        sendErrorMessage(resp, mapper, ErrorMessages.TEXT_PARSE_EXCEPTION.getDescription() + exceptionString);
        return;
      } catch (Exception e) {
        String exceptionString = ExceptionUtil.getStackTraceAsString(e);
        if (e.toString().contains(ErrorMessages.UNAUTHORIZED_ACCESS.getDescription())) {
          log.warning( ErrorMessages.UNAUTHORIZED_ACCESS.getDescription() + exceptionString);
          sendErrorMessage(resp, mapper,  e.getMessage());
          return;
        } else {
          log.warning( ErrorMessages.GENERAL_EXCEPTION.getDescription() + exceptionString);
          sendErrorMessage(resp, mapper, ErrorMessages.GENERAL_EXCEPTION.getDescription()+ e);
          return;
        }
      }
    }
  }
  
  private void sendErrorMessage(HttpServletResponse resp, ObjectMapper mapper,
                                String errorMessage) throws JsonGenerationException, JsonMappingException, IOException {
    EventQueryStatus evQryStatus = createResponse(errorMessage);
    String results = mapper.writeValueAsString(evQryStatus);
    resp.getWriter().println(results);
  }
  
  private EventQueryStatus createResponse(String errorMessage)  {
    EventQueryStatus evQryStatus = new EventQueryStatus();
    evQryStatus.setErrorMessage(errorMessage);
    evQryStatus.setStatus(Constants.FAILURE);
    return evQryStatus;
  }

  private void setCharacterEncoding(HttpServletRequest req,
                                    HttpServletResponse resp) throws UnsupportedEncodingException {
    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");
  }

}