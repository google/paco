package com.google.sampling.experiential.server.invitations;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.server.AuthUtil;
import com.google.sampling.experiential.server.ExperimentAccessManager;
import com.pacoapp.paco.shared.comm.Outcome;
import com.pacoapp.paco.shared.model2.JsonConverter;

@SuppressWarnings("serial")
public class InvitationServlet extends HttpServlet {

  public static final Logger log = Logger.getLogger(InvitationServlet.class.getName());

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    log.info("doGet of InvitationServlet - test");
    resp.setContentType("application/json;charset=UTF-8");
    logPacoClientVersion(req);

    User user = AuthUtil.getWhoFromLogin();
    String email = null;
    if (user != null) {
      email = AuthUtil.getEmailOfUser(req, user);
    } else {
      email = req.getRemoteAddr();
    }

    String generateParam = req.getParameter("generate");
    if (!Strings.isNullOrEmpty(generateParam) && user != null) {
      generateCodes(req, resp, email);
    } else {

      String inviteCodeParam = req.getParameter("code");

      if (!Strings.isNullOrEmpty(inviteCodeParam)) {
        log.info("Checking invite code...");
        Invitation invitation = new InvitationEntityManager().redeem(inviteCodeParam);
        if (invitation != null) {
          resp.getWriter().println(JsonConverter.convertToJsonString(invitation));
        } else {
          resp.getWriter().println(JsonConverter.convertToJsonString(createErrorOutcome("Used code")));
        }
      } else {
        resp.getWriter().println(JsonConverter.convertToJsonString(createErrorOutcome("Unrecognized code")));
      }
    }
  }

  private void logPacoClientVersion(HttpServletRequest req) {
    String pacoVersion = req.getHeader("paco.version");
    if (pacoVersion != null) {
      log.info("Paco version of request = " + pacoVersion);
    }
  }

  public List<Outcome> createErrorOutcome(String msg) {
    Outcome outcome = new Outcome(0, msg);
    List<Outcome> outcomes = Lists.newArrayList(outcome);
    return outcomes;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    // TODO Auto-generated method stub
    log.info("in post of inviteservlet");
    resp.setContentType("application/json;charset=UTF-8");
    logPacoClientVersion(req);

    User user = AuthUtil.getWhoFromLogin();
    String email = null;
    if (user == null) {
      AuthUtil.redirectUserToLogin(req, resp);
    } else {
      email = AuthUtil.getEmailOfUser(req, user);
    }

    generateCodes(req, resp, email);
  }

  private void generateCodes(HttpServletRequest req, HttpServletResponse resp, String email) throws IOException {
    String experimentIdParam = req.getParameter("id");
    String countParam = req.getParameter("count");

    if (!Strings.isNullOrEmpty(experimentIdParam) && !Strings.isNullOrEmpty(countParam) && !Strings.isNullOrEmpty(email)) {
      if (ExperimentAccessManager.isUserAdmin(new Long(experimentIdParam), email)) {
        int count = getCount(countParam);
        int offset = getOffset(req.getParameter("offset"));

        List<Invitation> invitations = new InvitationGenerator().generateNInvitations(email,
                                                                                      new Long(experimentIdParam),
                                                                                      count, offset);
        if (invitations != null && !invitations.isEmpty()) {
          new InvitationEntityManager().addInvitations(invitations);
        } else {
          invitations = Lists.newArrayList();
        }
        String json = JsonConverter.convertToJsonString(invitations);
        resp.getWriter().println(json);
      } else {
        resp.getWriter().println(JsonConverter.convertToJsonString(createErrorOutcome("unauthorized")));
      }
    } else {
      resp.getWriter().println(JsonConverter.convertToJsonString(createErrorOutcome("arguments missing")));
    }
  }

  private int getCount(String countParam) {
    int count = 1;
    if (!Strings.isNullOrEmpty(countParam)) {
      try {
        count = new Integer(countParam);
      } catch (NumberFormatException nf) {
        log.info("invalid countparam");
      }
    }
    return count;
  }

  private int getOffset(final String offsetparam) {
    int offset = 0;
    if (!Strings.isNullOrEmpty(offsetparam)) {
      try {
        offset = new Integer(offsetparam);
      } catch (NumberFormatException e) {
        log.info("invalid offset");
      }
    }
    return offset;
  }

}