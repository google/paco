package com.google.sampling.experiential.server.stats.usage;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.server.ExperimentServiceFactory;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentQueryResult;

/**
 * This class runs some stats on creators
 * 
 * 
 * 
 */
public class UsageStatsCreatorStatsJob {

  private static final Logger log = Logger.getLogger(UsageStatsCreatorStatsJob.class.getName());
  private String adminDomainSystemSetting;

  public static class CreatorStat {
    private String id;
    private List<Long> experimentIds;
    
    
    public CreatorStat(String id, List<Long> experimentIds) {
      super();
      this.id = id;
      if (experimentIds != null) {
        this.experimentIds = experimentIds;
      } else {
        this.experimentIds = Lists.newArrayList();
      }
    }
    
    public CreatorStat(String admin) {
      this(admin, new ArrayList<Long>());
    }

    public String getId() {
      return id;
    }
    
    public void setId(String id) {
      this.id = id;
    }
    
    public List<Long> getExperimentIds() {
      return experimentIds;
    }
    
    public void setExperimentIds(List<Long> experimentIds) {
      this.experimentIds = experimentIds;
    }

    public void add(Long id) {
      this.experimentIds.add(id);
    }

    public String toLine() {
      String idstr = "";
      if (experimentIds != null && experimentIds.size() > 0) {
        idstr = Joiner.on(",").join(experimentIds);
      }
      
      return id +"," + idstr;
    }
    
    
  }
  
  public UsageStatsCreatorStatsJob() {
  }

  public void run() throws IOException {
    // part 2 experiment stats
    ExperimentQueryResult experimentsQueryResults = ExperimentServiceFactory.getExperimentService().getAllExperiments(null);
    List<ExperimentDAO> experimentList = experimentsQueryResults.getExperiments();

    if (experimentList != null) {
      log.info("writing usage stats");
      loadAdminDomainSetting();

      Map<String, CreatorStat> administratorExperimentMap = Maps.newConcurrentMap();
      for (ExperimentDAO experimentDAO : experimentList) {
        // build graph
        String creator = experimentDAO.getCreator().toLowerCase();
//        List<String> admins = experimentDAO.getAdmins();
//        for (String admin : admins) {
//          admin = admin.toLowerCase();
        String admin = creator; // keep existing admin code
          CreatorStat existingStat = administratorExperimentMap.get(admin);
          if (existingStat == null) {
            existingStat = new CreatorStat(admin);
            administratorExperimentMap.put(admin, existingStat);
          }
          Long id = experimentDAO.getId();
          if (id == null) {
            log.info("Could not add creator stat for experiment. null Id: " + experimentDAO.getTitle());
          } else {
            existingStat.add(id);
          }
       // }
      }

      // partition graph by adminDomain
      List<CreatorStat> nonDomainExperimentStats = Lists.newArrayList();
      List<CreatorStat> domainExperimentStats = Lists.newArrayList();

      for (String admin : administratorExperimentMap.keySet()) {
        CreatorStat stat = administratorExperimentMap.get(admin);
        if (admin.indexOf("@" + adminDomainSystemSetting) != -1) {
          domainExperimentStats.add(stat);
        } else {
          nonDomainExperimentStats.add(stat);
        }
      }
      // write each graph to GCS
      writeCreatorStat(nonDomainExperimentStats);
      writeCreatorStat(domainExperimentStats);
    }
  }

 

  private void writeCreatorStat(List<CreatorStat> nonDomainExperimentStats) {
    List<String> lines = Lists.newArrayList();
    for (CreatorStat creatorStat : nonDomainExperimentStats) {
      lines.add(creatorStat.toLine()); 
    }
    writeFile(lines);
    
  }

  private void writeFile(List<String> lines) {
    // TODO GCS file write. Use a good title!
    try {
      GcsService gcsService = GcsServiceFactory.createGcsService();
      String bucketName = System.getProperty("com.pacoapp.reportbucketname");
      String fileName = "creator_stats_" + System.currentTimeMillis() + ".csv";
      GcsFilename filename = new GcsFilename(bucketName, fileName);
      GcsFileOptions options = new GcsFileOptions.Builder()
              .mimeType("text/csv")
              .acl("project-private")
              .addUserMetadata("jobId", "000001")
              .build();

      GcsOutputChannel writeChannel = gcsService.createOrReplace(filename, options);
      PrintWriter writer = new PrintWriter(Channels.newWriter(writeChannel, "UTF8"));
      if (lines != null) {
        for (String line : lines) {
          writer.println(line);
        }
      }
      writer.flush();

      writeChannel.waitForOutstandingWrites();

      writeChannel.close();
    } catch (Exception e) {
      log.info("Could not write creator stats files: " + e.getMessage());
      e.printStackTrace();
    }    
  }

  private boolean isNonPilotExperiment(ExperimentDAO experimentDAO) {
    if (experimentDAO.getPublished() && experimentDAO.getPublishedUsers() != null
        && experimentDAO.getPublishedUsers().size() >= 5) {
      return true;
    }

    // TODO add more heuristics that non-pilot experiments possess
    return false;
  }

  private void loadAdminDomainSetting() {
    adminDomainSystemSetting = System.getProperty("com.pacoapp.adminDomain");
    if (Strings.isNullOrEmpty(adminDomainSystemSetting)) {
      adminDomainSystemSetting = "";
    }
  }

}
