package com.google.sampling.experiential.datastore;

import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.json.JSONException;

import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;
import com.google.common.collect.Lists;

public class SimonConfigEntity {
  private static final String CONFIG_COLUMN = "config";
  private static final String CREATION_DATE_COLUMN = "creationTime";
  private static final String IS_PUBLIC_COLUMN = "isPublic";
  private static final String TITLE_COLUMN = "title";
  private static final String UPDATED_DATE_COLUMN = "updateTime";
  private static final String ENTITY_KIND = "SimonConfig";

  public static final Logger log = Logger.getLogger(SimonConfigEntity.class.getName());

  /**
   * Wrapper for SimonConfig entity kinds.
   * 
   * Provides accessor methods for entity properties and for converting an
   * entity to a JsonNode object.
   */
  public static class SimonConfig {
    private static final ObjectMapper JSON = new ObjectMapper();
    private Entity simonEntity;

    SimonConfig(Entity simonConfig) {
      simonEntity = simonConfig;
    }

    /**
     * Reads the entity's Text config and converts it to a JsonNode.
     * 
     * @return JsonNode
     * @throws IOException
     * @throws JsonProcessingException
     */
    public JsonNode config() throws IOException, JsonProcessingException {
      Text simonConfig = (Text) simonEntity.getProperty(CONFIG_COLUMN);
      return JSON.readTree(simonConfig.getValue());
    }

    /**
     * Casts the creation date to a Date object.
     * 
     * @return Date
     */
    public Date creationDate() {
      return (Date) simonEntity.getProperty(CREATION_DATE_COLUMN);
    }

    /**
     * Casts the isPublic property to a Boolean.
     * 
     * @return Boolean
     */
    public Boolean isPublic() {
      return (Boolean) simonEntity.getProperty(IS_PUBLIC_COLUMN);
    }

    /**
     * Returns the entity's key object.
     * 
     * @return Key
     */
    public Key key() {
      return simonEntity.getKey();
    }

    /**
     * Casts the entity's title to a String object.
     * 
     * @return String
     */
    public String title() {
      return (String) simonEntity.getProperty(TITLE_COLUMN);
    }

    /**
     * Casts the updated date to a Date object.
     * 
     * @return Date
     */
    public Date updatedDate() {
      return (Date) simonEntity.getProperty(UPDATED_DATE_COLUMN);
    }

    /**
     * Creates a JsonNode from the entity.
     * 
     * @return JsonNode
     * @throws IOException
     * @throws JsonProcessingException
     */
    public JsonNode toJson() throws IOException, JsonProcessingException {
      ObjectNode config = JSON.createObjectNode();
      config.put(CONFIG_COLUMN, config());
      config.put(CREATION_DATE_COLUMN, creationDate().getTime());
      config.put(IS_PUBLIC_COLUMN, isPublic());
      config.put("key", KeyFactory.keyToString(key()));
      config.put(TITLE_COLUMN, title());
      config.put(UPDATED_DATE_COLUMN, updatedDate().getTime());
      return (JsonNode) config;
    }

    /**
     * Creates a modified version of the entity's toJson. Creation, updated, and
     * key properties are removed. This is useful for creating copies of a
     * config.
     * 
     * @return JsonNode
     * @throws IOException
     * @throws JsonProcessingException
     */
    public JsonNode copyJson() throws IOException, JsonProcessingException {
      ObjectNode config = (ObjectNode) toJson();
      config.remove("key");
      config.remove(CREATION_DATE_COLUMN);
      config.remove(UPDATED_DATE_COLUMN);
      return (JsonNode) config;
    }

    /**
     * Converts the entity to a json encoded string.
     * 
     * @return String
     * @throws IOException
     * @throws JsonProcessingException
     */
    public String toJsonString() throws IOException, JsonProcessingException {
      return toJson().toString();
    }
  }

  /**
   * Validates an email address.
   * 
   * @param emailAddress
   * @return String
   * @throws AddressException
   */
  public static String getValidEmailAddress(String emailAddress) throws AddressException {
    InternetAddress address = new InternetAddress(emailAddress.toLowerCase());
    address.validate();
    return address.toString();
  }

  /**
   * Gets a specific config entity.
   * 
   * The requestorEmail must be the parent or the entity's isPublic == true.
   * 
   * @param requestorEmail
   * @param key
   * @return SimonConfig
   * @throws AddressException
   * @throws EntityNotFoundException
   * @throws UnauthorizedException
   */
  public static SimonConfig getSimonConfig(String requestorEmail, Key key) throws AddressException,
                                                                          EntityNotFoundException,
                                                                          UnauthorizedException {
    Key requestorKey = KeyFactory.createKey(ENTITY_KIND, getValidEmailAddress(requestorEmail));
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Entity simonConfigEntity = ds.get(key);
    Boolean isPublic = (Boolean) simonConfigEntity.getProperty(IS_PUBLIC_COLUMN);

    if (!isPublic && !simonConfigEntity.getParent().equals(requestorKey)) {
      throw new UnauthorizedException("Not authorized to view config.");
    }

    return new SimonConfigEntity.SimonConfig(simonConfigEntity);
  }

  /**
   * Finds all the configs for a given creatorEmail.
   * 
   * @param creatorEmail
   * @return List<SimonConfig>
   * @throws AddressException
   */
  public static List<SimonConfig> getSimonConfigs(String creatorEmail) throws AddressException {
    Key parent = KeyFactory.createKey(ENTITY_KIND, getValidEmailAddress(creatorEmail));

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Query q = new Query(ENTITY_KIND).setAncestor(parent);

    PreparedQuery pq = ds.prepare(q);

    List<SimonConfig> results = Lists.newArrayList();
    for (Entity result : pq.asIterable()) {
      results.add(new SimonConfigEntity.SimonConfig(result));
    }
    return results;
  }

  /**
   * Updates a given entity with the given property values.
   * 
   * @param entity
   * @param title
   * @param config
   * @param isPublic
   * @param time
   */
  private static void updateConfigEntity(Entity entity, String title, JsonNode config, Boolean isPublic, Date time) {
    entity.setProperty(CONFIG_COLUMN, new Text(config.toString()));
    entity.setProperty(IS_PUBLIC_COLUMN, isPublic);
    entity.setProperty(TITLE_COLUMN, title);
    entity.setProperty(UPDATED_DATE_COLUMN, time);
  }

  /**
   * Updates the given entity with the given property values.
   * 
   * @param entity
   * @param title
   * @param config
   * @param isPublic
   */
  private static void updateConfigEntity(Entity entity, String title, JsonNode config, Boolean isPublic) {
    updateConfigEntity(entity, title, config, isPublic, new Date());
  }

  /**
   * Creates a new config with the parent key created from creatorEmail.
   * 
   * @param creatorEmail
   * @param configTitle
   * @param config
   * @param isPublic
   * @return SimonConfig
   * @throws AddressException
   */
  public static SimonConfig createSimonConfig(String creatorEmail, String configTitle, JsonNode config, Boolean isPublic)
                                                                                                                         throws AddressException {
    Key parent = KeyFactory.createKey(ENTITY_KIND, getValidEmailAddress(creatorEmail));
    Date now = new Date();

    Entity entity = new Entity(ENTITY_KIND, parent);
    entity.setProperty(CREATION_DATE_COLUMN, now);
    updateConfigEntity(entity, configTitle, config, isPublic, now);

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    ds.put(entity);

    return new SimonConfigEntity.SimonConfig(entity);
  }

  /**
   * Creates a new config with the parent key created from creatorEmail.
   * 
   * @param creatorEmail
   * @param title
   * @param config
   * @return SimonConfig
   * @throws AddressException
   */
  public static SimonConfig createSimonConfig(String creatorEmail, String title, JsonNode config)
                                                                                                 throws AddressException {
    return createSimonConfig(creatorEmail, title, config, false);
  }

  /**
   * Creates a new config with the parent key created from creatorEmail.
   * 
   * @param creatorEmail
   * @param json
   * @return SimonConfig
   * @throws AddressException
   */
  public static SimonConfig createSimonConfig(String creatorEmail, JsonNode json) throws AddressException {
    if (json.has("isPublic")) {
      return createSimonConfig(creatorEmail, json.get("title").asText(), json.get("config"), json.get("isPublic")
                                                                                                 .asBoolean());
    }
    return createSimonConfig(creatorEmail, json.get("title").asText(), json.get("config"));
  }

  /**
   * Deletes an entity and returns the entity's copyJson.
   * 
   * @param key
   * @param requestorEmail
   * @return SimonConfig
   * @throws AddressException
   * @throws ConcurrentModificationException
   * @throws DatastoreFailureException
   * @throws EntityNotFoundException
   * @throws IllegalArgumentException
   * @throws IOException
   * @throws JsonProcessingException
   * @throws UnauthorizedException
   */
  public static JsonNode deleteSimonConfig(Key key, String requestorEmail) throws AddressException,
                                                                          ConcurrentModificationException,
                                                                          DatastoreFailureException,
                                                                          EntityNotFoundException,
                                                                          IllegalArgumentException, IOException,
                                                                          JsonProcessingException,
                                                                          UnauthorizedException {
    Key requestorKey = KeyFactory.createKey(ENTITY_KIND, getValidEmailAddress(requestorEmail));

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Entity simonConfigEntity = ds.get(key);
    if (!simonConfigEntity.getParent().equals(requestorKey)) {
      throw new UnauthorizedException("Not authorized to delete config.");
    }

    JsonNode config = new SimonConfigEntity.SimonConfig(simonConfigEntity).copyJson();
    ds.delete(simonConfigEntity.getKey());
    return config;
  }

  /**
   * Updates the requested entity.
   * 
   * Only the entity's parent may update the entity.
   * 
   * @param key
   * @param requestorEmail
   * @param title
   * @param config
   * @param isPublic
   * @return SimonConfig
   * @throws AddressException
   * @throws EntityNotFoundException
   * @throws UnauthorizedException
   */
  public static SimonConfig updateSimonConfig(Key key, String requestorEmail, String title, JsonNode config,
                                              Boolean isPublic) throws AddressException, EntityNotFoundException,
                                                               UnauthorizedException {
    Key requestorKey = KeyFactory.createKey(ENTITY_KIND, getValidEmailAddress(requestorEmail));

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Entity simonConfigEntity = ds.get(key);
    if (!simonConfigEntity.getParent().equals(requestorKey)) {
      throw new UnauthorizedException("Not authorized to edit config.");
    }
    updateConfigEntity(simonConfigEntity, title, config, isPublic);
    ds.put(simonConfigEntity);
    return new SimonConfigEntity.SimonConfig(simonConfigEntity);
  }

  /**
   * Updates the an entity using data contained in json.
   * 
   * @param requestorEmail
   * @param json
   * @return SimonConfig
   * @throws AddressException
   * @throws JSONException
   * @throws EntityNotFoundException
   * @throws UnauthorizedException
   */
  public static SimonConfig updateSimonConfig(String requestorEmail, JsonNode json) throws AddressException,
                                                                                   JSONException,
                                                                                   EntityNotFoundException,
                                                                                   UnauthorizedException {
    Key key = KeyFactory.stringToKey(json.get("key").asText());
    return updateSimonConfig(key, requestorEmail, json.get("title").asText(), json.get("config"), json.get("isPublic")
                                                                                                      .asBoolean());
  }

}
