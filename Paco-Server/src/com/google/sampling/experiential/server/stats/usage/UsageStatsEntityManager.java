package com.google.sampling.experiential.server.stats.usage;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.pacoapp.paco.shared.model2.JsonConverter;

/**
 * Manages the entity storage and retrieval for usage stats data.
 * 
 * This class is used by the cron job that runs weekly to update stats
 * and by the usage report generator driven through UsageStatsServlet.
 * 
 * The structure of usage stats rows is two for each week:
 * non domain specific stats to track other experiment aggregate stats.
 * domain specific stats to track our domain's experiments.
 * 
 * To get total stats, allExperimentStats, the two rows for a given week are added together.
 * 
 */
public class UsageStatsEntityManager {

  private static final String KIND = "experiment_usage_stat";

  private static final Logger log = Logger.getLogger(UsageStatsEntityManager.class.getName());
  
  private String DATE_PROPERTY = "date";
  private String ADMIN_DOMAIN_FILTER_PROPERTY = "adminDomainFilter";
  
  private static UsageStatsEntityManager instance;

  public static UsageStatsEntityManager getInstance() {
    if (instance == null) {
      instance = new UsageStatsEntityManager();
    }
    return instance;
  }

  private UsageStatsEntityManager() {
    super();
  }

  public List<UsageStat> getAllStats() {
    Query query = new Query(KIND);    
    return execute(query);
  }

  public List<UsageStat> getStatsForDomain(String adminDomainFilter) {
    Query query = new Query(KIND);
    query.setFilter(new FilterPredicate(ADMIN_DOMAIN_FILTER_PROPERTY, FilterOperator.EQUAL, adminDomainFilter));
    return execute(query);
  }
  
  public void addStats(UsageStat nonDomainStat, UsageStat domainStat) {
    Entity statRow, domainStatRow;
    try {
      statRow = createEntity(nonDomainStat);
      domainStatRow = createEntity(domainStat);
    } catch (IOException e) {
      log.severe("Could not generate json to store UsageStats: " + e.getMessage());
      return;
    }
    
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Transaction tx = null;     
    try {
      TransactionOptions txOpts = TransactionOptions.Builder.withXG(true);
      tx = ds.beginTransaction(txOpts);
      ds.put(statRow);
      ds.put(domainStatRow);
      tx.commit();
    } finally {
      if (tx.isActive()) {
        tx.rollback();
      }
    }
  }

  private Entity createEntity(UsageStat stat) throws IOException, JsonGenerationException, JsonMappingException {
    Entity statRow = new Entity(KIND);
    statRow.setProperty(DATE_PROPERTY, stat.getDate().toDateMidnight().toDate());
    statRow.setProperty(ADMIN_DOMAIN_FILTER_PROPERTY, stat.getAdminDomainFilter());
    Text json = new Text(JsonConverter.getObjectMapper().writer().writeValueAsString(stat));
    statRow.setUnindexedProperty("json", json);
    return statRow;
  }


  private List<UsageStat> execute(Query query) {
    List<UsageStat> stats = Lists.newArrayList();
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    
    PreparedQuery q = ds.prepare(query);
    FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();
    QueryResultList<Entity> list = q.asQueryResultList(fetchOptions);
    ObjectMapper mapper = JsonConverter.getObjectMapper();
    for (Entity entity : list) {
      String json = ((Text)entity.getProperty("json")).getValue();
      try {
        UsageStat stat = (UsageStat)mapper.readValue(json, UsageStat.class);
        stats.add(stat);
      } catch (IOException e) {
        log.severe("could not deserialize usage stat json: " + e.getMessage());
        e.printStackTrace();
      }
    }
    return stats;
  }

  public UsageStatsReport getStatsReport(String adminDomainFilter) {
    
    List<UsageStat> allExperimentStats = getAllStats();        
    allExperimentStats = combineByDate(allExperimentStats);
    

    if (!Strings.isNullOrEmpty(adminDomainFilter)) {
      //  todo just filter through the existing list instead of making another call.
      List<UsageStat> domainExperimentStats = getStatsForDomain(adminDomainFilter);       
      return new UsageStatsReport(allExperimentStats, domainExperimentStats, DateTime.now());
    } else {
      return new UsageStatsReport(allExperimentStats, null, DateTime.now());
    }
  }

  private List<UsageStat> combineByDate(List<UsageStat> allExperimentStats) {
    List<UsageStat> combined = Lists.newArrayList();
    Map<DateTime, List<UsageStat>> statsByDate = Maps.newConcurrentMap();
    
    // combine stats by date into map
    for (UsageStat usageStat : allExperimentStats) {
      List<UsageStat> existingStats = statsByDate.get(usageStat.getDate());
      if (existingStats == null) {
        existingStats = Lists.newArrayList();
        statsByDate.put(usageStat.getDate(), existingStats);
      } 
      existingStats.add(usageStat);
    }
    
    // reduce stats into one stat by adding them
    for ( Entry<DateTime, List<UsageStat>> entry : statsByDate.entrySet()) {
      List<UsageStat> stats = entry.getValue();
      if (stats.size() > 2) {
        log.severe("GOT back too many entries for a date on stats: " + entry.getKey().toString() + ", size: " + stats.size());
      } else if (stats.size() == 2) {
        UsageStat summedStat = new UsageStat(stats.get(0), stats.get(1));
        combined.add(summedStat);
      } else if (stats.size() == 1) {
        combined.add(stats.get(0));
      } else {
        log.info("could not happen. got 0 stats for date key: " + entry.getKey().toString());
      }
    }
    
    return combined;
  }

}
