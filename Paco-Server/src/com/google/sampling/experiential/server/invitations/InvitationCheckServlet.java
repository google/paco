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
public class InvitationCheckServlet extends HttpServlet {

  public static final Logger log = Logger.getLogger(InvitationCheckServlet.class.getName());

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    log.info("doGet of InvitationServlet - test");
    resp.setContentType("application/json;charset=UTF-8");
    logPacoClientVersion(req);

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


}