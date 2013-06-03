package com.google.sampling.experiential.server;

import java.util.Iterator;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.common.collect.Lists;

public class DBWhitelist extends Whitelist {
  
  private static final String EMAIL_PROPERTY = "email";
  private static final String WHITELISTED_USER_KIND = "whitelist";


  @Override
  public boolean allowed(String email) {
    email = email.toLowerCase();
    return isAdmin(email) || getUserByEmail(email) != null;
  }

  protected boolean isAdmin(String email) {
	  if(email.equals("ymggtest@gmail.com") || email.equals("yimingzhang@google.com")){
		  return true;
	  }
    return false;
  }
  
  private String getUserByEmail(String email) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query(WHITELISTED_USER_KIND);
    query.addFilter(EMAIL_PROPERTY, FilterOperator.EQUAL, email);
    PreparedQuery preparedQuery = ds.prepare(query);    
    Iterator<Entity> iterator = preparedQuery.asIterator();
    Entity user = null;
    if (iterator.hasNext()) {
      user = iterator.next();
    }
    return user != null ? (String)user.getProperty(EMAIL_PROPERTY) : null;
  }
  
  public void addUser(String email) {
    email = email.toLowerCase();
    if (getUserByEmail(email) == null) {
      DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
      Entity entity = new Entity(WHITELISTED_USER_KIND);
      entity.setProperty(EMAIL_PROPERTY, email);
      ds.put(entity);
    }
  }
  
  public void addAllUsers(List<String> emailAddresses) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    List<String> existingUsers = getUsers();
    List<Entity> newUsers = Lists.newArrayList();
    for (String email : emailAddresses) {
      if (existingUsers.contains(email)) {
        continue;
      } else {
        Entity user = new Entity(WHITELISTED_USER_KIND);
        user.setProperty(EMAIL_PROPERTY, email);
        newUsers.add(user);
      }
    }
    ds.put(newUsers);
  }

  
  public List<String> getUsers() {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();    
    Query q = new Query(WHITELISTED_USER_KIND);
    PreparedQuery preparedQuery = ds.prepare(q);
    
    List<String> users = Lists.newArrayList();
    QueryResultIterator<Entity> iterator = preparedQuery.asQueryResultIterator();
    while (iterator.hasNext()) {
      users.add((String) iterator.next().getProperty(EMAIL_PROPERTY));
    }
    return users;
  }
  
  

}
