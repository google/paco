package com.google.paco.cmdline;

import java.io.IOException;
import java.util.List;

import com.google.appengine.tools.remoteapi.RemoteApiInstaller;
import com.google.appengine.tools.remoteapi.RemoteApiOptions;
import com.google.sampling.experiential.model.Experiment;
import com.google.sampling.experiential.server.ExperimentRetriever;

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
      long experimentId = 0;
//      Console console = new bsh.Console();
//      console.main(new String[]{});
      queryForEventsFromExperiment(experimentId);
    } finally {
      installer.uninstall();
    }
  }

  private static void queryForEventsFromExperiment(long l) {
    ExperimentRetriever er = ExperimentRetriever.getInstance();
    Experiment experiment = er.getExperiment(Long.toString(l));
    List<String> admins = experiment.getAdmins();
    for (String admin : admins) {
      System.out.println("Admin: " + admin);
    }
  }

}
