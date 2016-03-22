package com.google.sampling.experiential.server.stats.usage;

import java.util.List;

import org.joda.time.DateTime;


/**
 * Data holder class used for generating json output.
 * 
 *
 */
public class UsageStatsReport {
  
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
    this.domainExperimentStats = domainExperimentStats;
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
