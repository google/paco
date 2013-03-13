package com.google.sampling.experiential.server;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.common.collect.Lists;
import com.google.sampling.experiential.model.WhitelistedUser;

public class DBWhitelist extends Whitelist {

  
  
  @Override
  public boolean allowed(String email) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query q = pm.newQuery(WhitelistedUser.class);
    q.setFilter("email == emailParam");
    q.declareParameters("String emailParam");
    
    @SuppressWarnings("unchecked")
    List<WhitelistedUser> users = (List<WhitelistedUser>) q.execute(email);
    pm.close();
    return users.size() == 1;      
  }
  
  public void addUser(String email) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    pm.makePersistent(new WhitelistedUser(email));
    pm.close();
  }
  
  public List<String> getUsers() {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query q = pm.newQuery(WhitelistedUser.class);
    
    @SuppressWarnings("unchecked")
    List<String> usersStrings = Lists.newArrayList();
    List<WhitelistedUser> users = (List<WhitelistedUser>) q.execute();
    for (WhitelistedUser whitelistedUser : users) {
      usersStrings.add(whitelistedUser.getEmail());
    }
    pm.close();
    return usersStrings;
  }
  
  

}
