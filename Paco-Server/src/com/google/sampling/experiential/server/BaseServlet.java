package com.google.sampling.experiential.server;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;

/**
 * Base class for handling servlet requests.
 */
public class BaseServlet extends HttpServlet {
  protected static final ObjectMapper JSON = new ObjectMapper();

  /**
   * Returns true/false if login is required for the servlet. By default all
   * servlet requests require a user to be logged in. Override this method to
   * allow unauthenticated requests.
   * 
   * @return boolean
   */
  protected boolean isLoginRequired() {
    return true; // All requests are protected by default.
  }

  /**
   * Enforces user authentication.
   */
  @Override
  public void service(ServletRequest req, ServletResponse resp) throws AssertionError, ServletException, IOException {
    if (isLoginRequired()) {
      AuthenticatedHttpServletRequest wrappedRequest = (AuthenticatedHttpServletRequest) (HttpServletRequest) req;
      // This should not happen. Check filter-mapping in web.xml.
      if (wrappedRequest.user == null) {
        throw new AssertionError("User not logged in.");
      }
    }
    super.service(req, resp);
  }

  /**
   * Sets the content type to json and replies with a JSON encoded string.
   * 
   * @param resp
   * @param json
   * @throws IOException
   */
  protected void replyJson(HttpServletResponse resp, String json) throws IOException {
    resp.setContentType("application/json;charset=UTF-8");
    resp.getWriter().println(json);
  }

  /**
   * Sets the content type to json and replies with a JSON encoded string.
   * 
   * @param resp
   * @param json
   * @throws IOException
   */
  protected void replyJson(HttpServletResponse resp, JsonNode json) throws IOException {
    replyJson(resp, json.toString());
  }

  /**
   * THIS METHOD MAY ONLY BE CALLED ONCE!
   * 
   * Gets the request body as a string.
   * 
   * @param req
   * @return String
   * @throws IOException
   */
  protected String getStringFromRequestBody(HttpServletRequest req) throws IOException {
    StringBuffer requestBody = new StringBuffer();
    BufferedReader body = req.getReader();
    String line;
    while ((line = body.readLine()) != null) {
      requestBody.append(line);
    }
    return requestBody.toString();
  }

  /**
   * THIS METHOD MAY ONLY BE CALLED ONCE!
   * 
   * Attempts to parse JSON from the request body.
   * 
   * @param req
   * @return JsonNode
   * @throws IOException
   * @throws JSONException
   */
  protected JsonNode getJsonFromRequestBody(HttpServletRequest req) throws IOException, JSONException {
    return JSON.readTree(getStringFromRequestBody(req));
  }
}
