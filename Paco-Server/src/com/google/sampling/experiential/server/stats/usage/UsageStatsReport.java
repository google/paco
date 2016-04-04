package com.google.sampling.experiential.server.stats.usage;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.joda.time.DateTime;


/**
 * Data holder class used for generating json output.
 * 
 *
 */
public class UsageStatsReport {
  
  private final class UsageStatComparator implements Comparator<UsageStat> {
    @Override
    public int compare(UsageStat o1, UsageStat o2) {
      DateTime o1Date = o1.getDate();
      DateTime o2Date = o2.getDate();
      if (o1Date == null && o2Date == null) {
        return 0;
      } else if (o1Date != null && o2Date == null) {
        return -1;
      } else if (o1Date == null && o2Date != null) {
        return 1;
      } else {
        return o1Date.compareTo(o2Date);
      }
    }
  }

  /**
   * Time series of stats for the whole server
   */
  private List<UsageStat> allExperimentStats;
  
  /**
   * Time series of stats for a particular domain
   */
  private List<UsageStat> domainExperimentStats;
  
  /**
   * Date report was generated
   */
  private DateTime generationDate;

  public UsageStatsReport(List<UsageStat> allExperimentStats, List<UsageStat> domainExperimentStats,
                          DateTime generationDate) {
    super();
    this.allExperimentStats = allExperimentStats;
    if (this.allExperimentStats != null) {
      Collections.sort(this.allExperimentStats, new UsageStatComparator());
    }
    
    this.domainExperimentStats = domainExperimentStats;
    if (this.domainExperimentStats != null) {
      Collections.sort(this.domainExperimentStats, new UsageStatComparator());
    }
    this.generationDate = generationDate;
  }

  public List<UsageStat> getAllExperimentStats() {
    return allExperimentStats;
  }

  public void setAllExperimentStats(List<UsageStat> allExperimentStats) {
    this.allExperimentStats = allExperimentStats;
  }

  public List<UsageStat> getDomainExperimentStats() {
    return domainExperimentStats;
  }

  public void setDomainExperimentStats(List<UsageStat> domainExperimentStats) {
    this.domainExperimentStats = domainExperimentStats;
  }

  public DateTime getGenerationDate() {
    return generationDate;
  }

  public void setGenerationDate(DateTime generationDate) {
    this.generationDate = generationDate;
  }
  
  
  

}
