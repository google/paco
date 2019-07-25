package com.google.sampling.experiential.server;

import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.common.collect.Lists;

public class BlobAclStore {

  private static final String BLOB_ACL_KIND = "blob_acl";
  private static BlobAclStore instance;

  public static synchronized BlobAclStore getInstance() {
    if (instance == null) {
      instance = new BlobAclStore();
    }
    
    return instance;
  }

  public void saveAcls(List<BlobAcl> blobAcls) {
    if (blobAcls == null || blobAcls.isEmpty()) {
      return;      
    }
    
    List<Entity> entities = Lists.newArrayList();
    for (BlobAcl blobAcl : blobAcls) {
      Entity entity = new Entity(BLOB_ACL_KIND, blobAcl.getKeyString());
      entity.setProperty("experimentId", Long.parseLong(blobAcl.getExperimentId()));
      entity.setProperty("who", blobAcl.getWho());
      entities.add(entity);
    }
    
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();    
    ds.put(entities);    
  }

  public BlobAcl getAcl(String blobKeyParam) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();    
    try {
      Entity entity = ds.get(KeyFactory.createKey(BLOB_ACL_KIND, blobKeyParam));
      if (entity != null) {
        BlobAcl blobAcl = new BlobAcl(entity.getKey().getName(), 
                                      ((Long)entity.getProperty("experimentId")).toString(), 
                                      (String)entity.getProperty("who"), 
                                      (String)entity.getProperty("bucketName"), 
                                      (String)entity.getProperty("objectName"));
        return blobAcl;
      }
    } catch (EntityNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }

  public void saveAcl(BlobAcl blobAcl) {
    if (blobAcl == null) {
      return;      
    }
    
    Entity entity = new Entity(BLOB_ACL_KIND, blobAcl.getKeyString());
    entity.setProperty("experimentId", Long.parseLong(blobAcl.getExperimentId()));
    entity.setProperty("who", blobAcl.getWho());
    entity.setProperty("bucketName", blobAcl.getBucketName());
    entity.setProperty("objectName", blobAcl.getObjectName());
    
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();    
    ds.put(entity);
    
  }

}
