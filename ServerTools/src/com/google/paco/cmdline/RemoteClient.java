package com.google.paco.cmdline;

import bsh.Console;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.remoteapi.RemoteApiInstaller;
import com.google.appengine.tools.remoteapi.RemoteApiOptions;
import com.google.common.base.Joiner;

import java.io.IOException;
import java.util.List;

public class RemoteClient {

  public static void main(String[] args) throws IOException {
    String username = System.console().readLine("username: ");
    String password = new String(System.console().readPassword("password: "));
    String address = new String(System.console().readLine("server: "));
    String port = new String(System.console().readLine("port: "));
    RemoteApiOptions options = new RemoteApiOptions().server(address, Integer.parseInt(port)).credentials(username,
                                                                                                         password);
    RemoteApiInstaller installer = new RemoteApiInstaller();
    installer.install(options);
    try {
//      Console console = new bsh.Console();
//      console.main(new String[]{});

      DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
      Query q = new Query("Experiment");
      List<Entity> results = ds.prepare(q).asList(FetchOptions.Builder.withDefaults().chunkSize(10));
      System.out.println("Kinds: " + results.size() + Joiner.on(",").join(results));
      q = new Query(results.get(0).getKey());
      results = ds.prepare(q).asList(FetchOptions.Builder.withDefaults().chunkSize(10));
      System.out.println("Kinds: " + results.size() + Joiner.on(",").join(results));
      
//      System.out.println("Key of new entity is " + ds.put(new Entity("Hello Remote API!")));
    } finally {
      installer.uninstall();
    }
  }

}
