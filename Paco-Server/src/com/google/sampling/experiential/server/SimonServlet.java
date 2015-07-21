/**
 * 
 */
package com.google.sampling.experiential.server;

import java.io.IOException;

import javax.mail.internet.AddressException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.json.JSONException;

import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.sampling.experiential.datastore.SimonConfigEntity;
import com.google.sampling.experiential.datastore.UnauthorizedException;
import com.google.sampling.experiential.datastore.SimonConfigEntity.SimonConfig;

import java.util.ConcurrentModificationException;
import java.util.List;

/**
 * Simon servlet handles simon config CRUD.
 */
public class SimonServlet extends BaseServlet {

  private JsonNode createConfig(String creatorEmail, JsonNode json) throws AddressException, JSONException,
                                                                   JsonProcessingException, IOException {
    return SimonConfigEntity.createSimonConfig(creatorEmail, json).toJson();
  }

  protected JsonNode deleteConfig(String key, String email) throws AddressException, ConcurrentModificationException,
                                                           DatastoreFailureException, IllegalArgumentException,
                                                           JsonProcessingException, EntityNotFoundException,
                                                           IOException, UnauthorizedException {
    Key configKey = KeyFactory.stringToKey(key);
    return SimonConfigEntity.deleteSimonConfig(configKey, email);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    AuthenticatedHttpServletRequest request = (AuthenticatedHttpServletRequest) req;
    String email = request.user.getEmail();
    String key = req.getParameter("key");
    String del = req.getParameter("delete");

    try {
      if ((key == null) && (del == null)) {
        replyJson(resp, getConfigList(email));
      } else if (key != null) {
        replyJson(resp, getConfig(key, email));
      } else {
        replyJson(resp, deleteConfig(del, email));
      }
    } catch (AddressException e) {
      resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
      replyJson(resp, jsonErrorMessage(e.getMessage()));
    } catch (IllegalArgumentException e) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      replyJson(resp, jsonErrorMessage(e.getMessage()));
    } catch (EntityNotFoundException e) {
      resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
      replyJson(resp, jsonErrorMessage(e.getMessage()));
    } catch (UnauthorizedException e) {
      resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      replyJson(resp, jsonErrorMessage(e.getMessage()));
    } catch (JSONException e) {
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      replyJson(resp, jsonErrorMessage(e.getMessage()));
    } catch (IOException e) {
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      replyJson(resp, jsonErrorMessage(e.getMessage()));
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    AuthenticatedHttpServletRequest request = (AuthenticatedHttpServletRequest) req;
    String email = request.user.getEmail();

    try {
      JsonNode jsonRequest = getJsonFromRequestBody(req);
      log(jsonRequest.asText());
      if (jsonRequest.has("key")) {
        replyJson(resp, updateConfig(email, jsonRequest));
      } else {
        replyJson(resp, createConfig(email, jsonRequest));
      }
    } catch (AddressException e) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      replyJson(resp, jsonErrorMessage(e.getMessage()));
    } catch (IllegalArgumentException e) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      replyJson(resp, jsonErrorMessage(e.getMessage()));
    } catch (EntityNotFoundException e) {
      resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
      replyJson(resp, jsonErrorMessage(e.getMessage()));
    } catch (JSONException e) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      replyJson(resp, jsonErrorMessage(e.getMessage()));
    } catch (UnauthorizedException e) {
      resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      replyJson(resp, jsonErrorMessage(e.getMessage()));
    } catch (IOException e) {
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      replyJson(resp, jsonErrorMessage(e.getMessage()));
    }
  }

  protected JsonNode getConfig(String key, String email) throws IOException, AddressException, EntityNotFoundException,
                                                        UnauthorizedException, JSONException {
    Key configKey = KeyFactory.stringToKey(key);
    return SimonConfigEntity.getSimonConfig(email, configKey).toJson();
  }

  protected JsonNode getConfigList(String email) throws IOException, JSONException, AddressException {
    ArrayNode results = JSON.createArrayNode();
    List<SimonConfig> configs = SimonConfigEntity.getSimonConfigs(email);
    for (SimonConfig config : configs) {
      results.add(config.toJson());
    }

    ObjectNode jsonResults = JSON.createObjectNode();
    jsonResults.put("results", results);
    return (JsonNode) jsonResults;
  }

  private String jsonErrorMessage(String message) {
    return String.format("{\"error\": \"%s\"}", message);
  }

  private JsonNode updateConfig(String updaterEmail, JsonNode json) throws AddressException, JSONException,
                                                                   EntityNotFoundException, UnauthorizedException,
                                                                   JsonProcessingException, IOException {
    return SimonConfigEntity.updateSimonConfig(updaterEmail, json).toJson();
  }
}
