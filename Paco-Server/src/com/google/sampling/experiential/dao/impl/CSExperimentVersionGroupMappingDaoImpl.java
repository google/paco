package com.google.sampling.experiential.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.google.cloud.sql.jdbc.Statement;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.cloudsql.columns.ChoiceCollectionColumns;
import com.google.sampling.experiential.cloudsql.columns.DataTypeColumns;
import com.google.sampling.experiential.cloudsql.columns.ExperimentDetailColumns;
import com.google.sampling.experiential.cloudsql.columns.ExperimentVersionGroupMappingColumns;
import com.google.sampling.experiential.cloudsql.columns.ExternStringInputColumns;
import com.google.sampling.experiential.cloudsql.columns.ExternStringListLabelColumns;
import com.google.sampling.experiential.cloudsql.columns.GroupDetailColumns;
import com.google.sampling.experiential.cloudsql.columns.InformedConsentColumns;
import com.google.sampling.experiential.cloudsql.columns.InputCollectionColumns;
import com.google.sampling.experiential.cloudsql.columns.InputColumns;
import com.google.sampling.experiential.cloudsql.columns.UserColumns;
import com.google.sampling.experiential.dao.CSExperimentDetailDao;
import com.google.sampling.experiential.dao.CSExperimentVersionGroupMappingDao;
import com.google.sampling.experiential.dao.CSGroupDao;
import com.google.sampling.experiential.dao.CSGroupTypeInputMappingDao;
import com.google.sampling.experiential.dao.CSInformedConsentDao;
import com.google.sampling.experiential.dao.CSInputCollectionDao;
import com.google.sampling.experiential.dao.CSInputDao;
import com.google.sampling.experiential.dao.dataaccess.Choice;
import com.google.sampling.experiential.dao.dataaccess.ChoiceCollection;
import com.google.sampling.experiential.dao.dataaccess.DataType;
import com.google.sampling.experiential.dao.dataaccess.ExperimentDetail;
import com.google.sampling.experiential.dao.dataaccess.ExperimentVersionGroupMapping;
import com.google.sampling.experiential.dao.dataaccess.ExternStringInput;
import com.google.sampling.experiential.dao.dataaccess.ExternStringListLabel;
import com.google.sampling.experiential.dao.dataaccess.GroupDetail;
import com.google.sampling.experiential.dao.dataaccess.InformedConsent;
import com.google.sampling.experiential.dao.dataaccess.Input;
import com.google.sampling.experiential.dao.dataaccess.InputCollection;
import com.google.sampling.experiential.dao.dataaccess.InputOrderAndChoice;
import com.google.sampling.experiential.dao.dataaccess.PredefinedInputNames;
import com.google.sampling.experiential.dao.dataaccess.User;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.model.What;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.google.sampling.experiential.server.ExperimentDAOConverter;
import com.google.sampling.experiential.server.PacoId;
import com.google.sampling.experiential.server.QueryConstants;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.GroupTypeEnum;
import com.pacoapp.paco.shared.util.Constants;
import com.pacoapp.paco.shared.util.ErrorMessages;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.insert.Insert;

public class CSExperimentVersionGroupMappingDaoImpl implements CSExperimentVersionGroupMappingDao {

  public static final Logger log = Logger.getLogger(CSExperimentVersionGroupMappingDaoImpl.class.getName());
  private static List<Column> experimentVersionMappingColList = Lists.newArrayList();
  
  CSExperimentDetailDao experimentDaoImpl = new CSExperimentDetailDaoImpl();
  CSGroupDao groupDaoImpl = new CSGroupDaoImpl();
  CSInputDao inputDaoImpl = new CSInputDaoImpl();
  CSInputCollectionDao inputCollectionDaoImpl = new CSInputCollectionDaoImpl();
  
  static {
    experimentVersionMappingColList.add(new Column(ExperimentVersionGroupMappingColumns.EXPERIMENT_ID));
    experimentVersionMappingColList.add(new Column(ExperimentVersionGroupMappingColumns.EXPERIMENT_VERSION));
    experimentVersionMappingColList.add(new Column(ExperimentVersionGroupMappingColumns.EXPERIMENT_DETAIL_ID));
    experimentVersionMappingColList.add(new Column(ExperimentVersionGroupMappingColumns.GROUP_DETAIL_ID));
    experimentVersionMappingColList.add(new Column(ExperimentVersionGroupMappingColumns.INPUT_COLLECTION_ID));
    experimentVersionMappingColList.add(new Column(ExperimentVersionGroupMappingColumns.SOURCE));
  } 
  
  @Override
  public boolean createExperimentVersionGroupMapping(List<ExperimentVersionGroupMapping> experimentVersionMappingLst) throws SQLException {
    
    ExpressionList insertExperimentVersionMappingExprList = new ExpressionList();
    List<Expression> exp = Lists.newArrayList();
    Insert experimentVersionMappingInsert = new Insert();
    Connection conn = null;
    PreparedStatement statementCreateExperimentVersionMapping = null;
    ResultSet rs = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      conn.setAutoCommit(false);
      experimentVersionMappingInsert.setTable(new Table(ExperimentVersionGroupMappingColumns.TABLE_NAME));
      experimentVersionMappingInsert.setUseValues(true);
      insertExperimentVersionMappingExprList.setExpressions(exp);
      experimentVersionMappingInsert.setItemsList(insertExperimentVersionMappingExprList);
      experimentVersionMappingInsert.setColumns(experimentVersionMappingColList);
      // Adding ? for prepared stmt
      for (Column c : experimentVersionMappingColList) {
        ((ExpressionList) experimentVersionMappingInsert.getItemsList()).getExpressions().add(new JdbcParameter());
      }

      statementCreateExperimentVersionMapping = conn.prepareStatement(experimentVersionMappingInsert.toString(), Statement.RETURN_GENERATED_KEYS);
      for (ExperimentVersionGroupMapping evm : experimentVersionMappingLst) {
        statementCreateExperimentVersionMapping.setLong(1, evm.getExperimentId());
        statementCreateExperimentVersionMapping.setInt(2, evm.getExperimentVersion());
        statementCreateExperimentVersionMapping.setLong(3, evm.getExperimentInfo().getExperimentDetailId().getId());
        statementCreateExperimentVersionMapping.setLong(4, evm.getGroupInfo().getGroupId().getId());
        statementCreateExperimentVersionMapping.setObject(5, evm.getInputCollection() != null ? evm.getInputCollection().getInputCollectionId() : null, Types.BIGINT);
        statementCreateExperimentVersionMapping.setString(6, evm.getSource());
        
        log.info(statementCreateExperimentVersionMapping.toString());
        statementCreateExperimentVersionMapping.addBatch();
      }
      if (statementCreateExperimentVersionMapping != null){
        statementCreateExperimentVersionMapping.executeBatch();
        ResultSet generatedKeys = statementCreateExperimentVersionMapping.getGeneratedKeys();
        for (ExperimentVersionGroupMapping evm : experimentVersionMappingLst) {
          if ( generatedKeys == null || ! generatedKeys.next()){
            log.warning("Unable to retrieve all generated keys");
          }
          evm.setExperimentVersionMappingId(generatedKeys.getLong(1));
        }
      }
      conn.commit();
    } catch(SQLException sqle) {
      log.warning("Exception while inserting to experiment_version_mapping table" +  sqle);
      throw sqle;
    } finally {
      try {
        if( rs != null) { 
          rs.close();
        }
        if (statementCreateExperimentVersionMapping != null) {
          statementCreateExperimentVersionMapping.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.info(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription() + ex1);
      }
    }
    return true;
  }

  private void updateNewGroupsWithOldId(Map<String, GroupDetail> oldGroupMap, List<GroupDetail> newGroupList) {
    boolean groupDetailChanged = false;
    for (GroupDetail currentNewGroup : newGroupList) {
      groupDetailChanged = currentNewGroup.hasChanged(oldGroupMap.get(currentNewGroup.getName()));
      if (!groupDetailChanged) {
        // group property fields matched with older version properties
        currentNewGroup.setGroupId(oldGroupMap.get(currentNewGroup.getName()).getGroupId());
      }
    }
  }
  
  private void updateNewInputCollectionWithOldId(Map<String, InputCollection> oldVersion, Map<String, InputCollection> newVersion) {
    Iterator<String> newGrpNameItr = newVersion.keySet().iterator();
    InputCollection currentNewInputCollection = null;
    String currentNewGroupName = null;
    InputCollection matchingOldInputCollection = null;
    Boolean inputCollectionChanged = false;
    // for every new grp
    while (newGrpNameItr.hasNext()) {
      currentNewGroupName = newGrpNameItr.next();
      currentNewInputCollection = newVersion.get(currentNewGroupName);
      if ( currentNewInputCollection == null || (currentNewInputCollection.getInputOrderAndChoices() != null && currentNewInputCollection.getInputOrderAndChoices().size() == 0)) {
        log.info("grp has no inputs yet");
        newVersion.put(currentNewGroupName, null);
      } else {
        matchingOldInputCollection = oldVersion.get(currentNewGroupName) ;
        inputCollectionChanged = currentNewInputCollection.hasChanged(matchingOldInputCollection);
        if (!inputCollectionChanged) { 
          currentNewInputCollection.setInputCollectionId(matchingOldInputCollection.getInputCollectionId());
        }
      }
    } // while grp itr
  }
  @Override
  public void ensureExperimentVersionGroupMapping(ExperimentDAO experimentDao) throws Exception {
    List<ExperimentVersionGroupMapping> allGroupsInNewVersion = Lists.newArrayList();
    ExperimentDAOConverter converter = new ExperimentDAOConverter();
    InformedConsent newInformedConsent = null;
    InformedConsent oldInformedConsent = null;
    ExperimentDetail oldExperimentInfo = null;
    ExperimentDetail newExperimentInfo = null;
    Boolean experimentDetailsChanged = false;
    Boolean informedConsentChanged = false;
    List<GroupDetail> newGroupList = Lists.newArrayList();
    Map<String, GroupDetail> oldGroupMap = Maps.newHashMap();
    Map<String, InputCollection> oldGrpNameInputCollection = Maps.newHashMap();
    Map<String, InputCollection> newGrpNameInputCollection = Maps.newHashMap();
    CSInformedConsentDao informedConsentDao = new CSInformedConsentDaoImpl();
    boolean isVersionAlreadyPresent = getNumberOfGroups(experimentDao.getId(), experimentDao.getVersion()) > 0;
    if (!isVersionAlreadyPresent) {
      Map<String, ExperimentVersionGroupMapping> allGroupsInPrevVersion = getAllGroupsInVersion(experimentDao.getId(), experimentDao.getVersion()-1);
      allGroupsInNewVersion = converter.convertToExperimentVersionMapping(experimentDao);
      // iterating old version 
      if (allGroupsInPrevVersion != null) {
        Iterator<Entry<String, ExperimentVersionGroupMapping>> itr = allGroupsInPrevVersion.entrySet().iterator();
        while (itr.hasNext()) {
          // we need to get just once for an experiment. all groups share the experiment info
          Entry<String, ExperimentVersionGroupMapping> crtMapping = itr.next();
          oldExperimentInfo = crtMapping.getValue().getExperimentInfo();
          oldGroupMap.put(crtMapping.getValue().getGroupInfo().getName(), crtMapping.getValue().getGroupInfo());
          oldGrpNameInputCollection.put(crtMapping.getValue().getGroupInfo().getName(), crtMapping.getValue().getInputCollection());
        }
      }
      // iterating new version
      for (ExperimentVersionGroupMapping evm : allGroupsInNewVersion) {
        newExperimentInfo = evm.getExperimentInfo();
        newGroupList.add(evm.getGroupInfo());
        newGrpNameInputCollection.put(evm.getGroupInfo().getName(), evm.getInputCollection());
      }
  
      // Item 1 - InformedConsent
      newInformedConsent = newExperimentInfo.getInformedConsent();
      if (newInformedConsent != null) {
        // new is not null, old might / might not have data
        oldInformedConsent = oldExperimentInfo != null ? oldExperimentInfo.getInformedConsent() : null;
        informedConsentChanged = newInformedConsent.hasChanged(oldInformedConsent);
        if (!informedConsentChanged) { 
          newInformedConsent.setInformedConsentId(oldInformedConsent.getInformedConsentId());
        }
      }    
      // Item 2 - Experiment
      if (!informedConsentChanged) {
        // if informed consent has changes, we anyway have to persist this change with new experiment facet id.
        // So, we don't need to check if other experiment properties like title description etc changed or not
        experimentDetailsChanged = newExperimentInfo.hasChanged(oldExperimentInfo);
        if (!experimentDetailsChanged) {
          newExperimentInfo.setExperimentDetailId(oldExperimentInfo.getExperimentDetailId());
        }
      } else {
        informedConsentDao.insertInformedConsent(newInformedConsent, experimentDao.getVersion());
      }
      // if no detail id, there is some change in expt info, so we need to insert into experiment table
      if (newExperimentInfo.getExperimentDetailId() == null) {
        experimentDaoImpl.insertExperimentDetail(newExperimentInfo);
      }
      // Item 3 - Groups
      if ( oldGroupMap != null) {
        updateNewGroupsWithOldId(oldGroupMap, newGroupList);
      }
      groupDaoImpl.insertGroup(newGroupList);
      // Item 4 - Inputs
      updateNewInputCollectionWithOldId(oldGrpNameInputCollection, newGrpNameInputCollection);
      inputCollectionDaoImpl.createInputCollectionId(experimentDao, newGrpNameInputCollection, oldGrpNameInputCollection);
      
      int grpCt = 0;
      for (ExperimentVersionGroupMapping evm : allGroupsInNewVersion) {
        evm.setExperimentId(experimentDao.getId());
        evm.setExperimentVersion(experimentDao.getVersion());
        evm.setExperimentInfo(newExperimentInfo);
        evm.setGroupInfo(newGroupList.get(grpCt++));
        evm.setInputCollection(newGrpNameInputCollection.get(evm.getGroupInfo().getName()));
      }
      createExperimentVersionGroupMapping(allGroupsInNewVersion);
      log.info("experiment version mapping process ends");
    } else {
      log.warning("experiment version mapping ends - version already exists");
    }
  }

  @Override
  public Map<String, ExperimentVersionGroupMapping> getAllGroupsInVersion(Long experimentId, Integer version) throws SQLException {
    Long experimentIdFromResultSet = null;
    Connection conn = null;
    PreparedStatement statementSelectExperiment = null;
    Map<String, ExperimentVersionGroupMapping> groupNameEVMMap = null;
    ExperimentVersionGroupMapping returnEVMH = null;
    ExperimentDetail eHistory = null;
    GroupDetail gHistory = null;
    Input iHistory = null;
    InputCollection icHistory = null;
    ChoiceCollection ccHistory = null;
    Choice choice = null;
    Map<String, Choice> choices = null;
    ExternStringListLabel externStringListLabel = null;
    ExternStringInput externName = null;
    ExternStringInput externText = null;
    String currentGroupName = null;
    Integer currentGroupType = null;
    Map<String, InputOrderAndChoice> returnEVMHInputOrderAndChoices = null;
    InputOrderAndChoice inputOrderAndChoiceObj = null;
    DataType dataType = null;
    InformedConsent informedConsent = null;
    ResultSet rs = null;
    Long recordCt = 0L;
    
    User creator = new User();

    String query = QueryConstants.GET_ALL_GROUPS_IN_VERSION.toString();

    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementSelectExperiment = conn.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
      Long st1Time = System.currentTimeMillis();
      statementSelectExperiment.setLong(1, experimentId);
      statementSelectExperiment.setInt(2, version != null ? version : 0);
      rs = statementSelectExperiment.executeQuery();
      
      if (rs != null) {
        while (rs.next()) {
          recordCt++;
          if (groupNameEVMMap == null) {
            groupNameEVMMap = Maps.newHashMap();
          }
          eHistory = new ExperimentDetail();
          gHistory = new GroupDetail();
          iHistory = new Input();
          dataType = new DataType();
          externStringListLabel = new ExternStringListLabel();
          externText = new ExternStringInput();
          externName = new ExternStringInput();
          inputOrderAndChoiceObj = new InputOrderAndChoice();
          
          experimentIdFromResultSet = rs.getLong(ExperimentVersionGroupMappingColumns.EXPERIMENT_ID);
          currentGroupName = rs.getString(GroupDetailColumns.NAME);
          currentGroupType = rs.getInt(GroupDetailColumns.GROUP_TYPE_ID);
          
          String informedConsentString = rs.getString(InformedConsentColumns.INFORMED_CONSENT);
          
          if (informedConsentString != null) {
            informedConsent = new InformedConsent();
            informedConsent.setExperimentId(experimentIdFromResultSet);
            informedConsent.setInformedConsent(informedConsentString);
            informedConsent.setInformedConsentId(new PacoId (rs.getLong(InformedConsentColumns.INFORMED_CONSENT_ID), false));
          }
          
          creator.setUserId(new PacoId(rs.getLong(UserColumns.USER_ID), false));
          creator.setWho(rs.getString(UserColumns.WHO));
          
          eHistory.setExperimentDetailId(new PacoId(rs.getLong(ExperimentDetailColumns.EXPERIMENT_DETAIL_ID), false));
          eHistory.setTitle(rs.getString(ExperimentDetailColumns.EXPERIMENT_NAME));
          eHistory.setDescription(rs.getString(ExperimentDetailColumns.DESCRIPTION));
          eHistory.setCreator(creator);
          eHistory.setOrganization(rs.getString(ExperimentDetailColumns.ORGANIZATION));
          eHistory.setContactEmail(rs.getString(ExperimentDetailColumns.CONTACT_EMAIL));
          eHistory.setInformedConsent(informedConsent);
          eHistory.setDeleted(rs.getBoolean(ExperimentDetailColumns.DELETED));
          Timestamp dt = rs.getTimestamp(ExperimentDetailColumns.MODIFIED_DATE);
          eHistory.setModifiedDate(dt!=null ? new DateTime(dt.getTime()): new DateTime());
          eHistory.setPublished(rs.getBoolean(ExperimentDetailColumns.PUBLISHED));
          eHistory.setRingtoneUri(rs.getString(ExperimentDetailColumns.RINGTONE_URI));
          eHistory.setPostInstallInstructions(rs.getString(ExperimentDetailColumns.POST_INSTALL_INSTRUCTIONS));
          
          gHistory.setName(currentGroupName);
          gHistory.setGroupId(new PacoId(rs.getLong(GroupDetailColumns.GROUP_DETAIL_ID), false));
          gHistory.setGroupTypeId(currentGroupType);
          gHistory.setCustomRendering(rs.getString(GroupDetailColumns.CUSTOM_RENDERING));
          gHistory.setFixedDuration(rs.getBoolean(GroupDetailColumns.FIXED_DURATION));
          Timestamp startDate = rs.getTimestamp(GroupDetailColumns.START_DATE);
          Timestamp endDate = rs.getTimestamp(GroupDetailColumns.END_DATE);
          gHistory.setStartDate(startDate != null ? new DateTime(startDate.getTime()) : null);
          gHistory.setEndDate(endDate != null ? new DateTime(endDate.getTime()) : null);
          gHistory.setRawDataAccess(rs.getBoolean(GroupDetailColumns.RAW_DATA_ACCESS));
          gHistory.setEndOfDayGroup(rs.getString(GroupDetailColumns.END_OF_DAY_GROUP));
        
          dataType.setDataTypeId(new PacoId(rs.getInt(DataTypeColumns.DATA_TYPE_ID), false));
          dataType.setName(rs.getString(DataTypeColumns.NAME));
          dataType.setNumeric(rs.getBoolean(DataTypeColumns.IS_NUMERIC));
          dataType.setMultiSelect(rs.getBoolean(DataTypeColumns.MULTI_SELECT));
          dataType.setResponseMappingRequired(rs.getBoolean(DataTypeColumns.RESPONSE_MAPPING_REQUIRED));
          
          externName.setExternStringInputId(new PacoId(rs.getLong("esi2." + ExternStringInputColumns.EXTERN_STRING_INPUT_ID), false));
          externName.setLabel(rs.getString("esi2." + ExternStringInputColumns.LABEL));
          externText.setExternStringInputId(new PacoId(rs.getLong("esi1." + ExternStringInputColumns.EXTERN_STRING_INPUT_ID), false));
          externText.setLabel(rs.getString("esi1." + ExternStringInputColumns.LABEL));
          
          iHistory.setInputId(new PacoId(rs.getLong(InputColumns.INPUT_ID), false));
          iHistory.setName(externName);
          iHistory.setRequired(rs.getBoolean(InputColumns.REQUIRED));
          iHistory.setConditional(rs.getString(InputColumns.CONDITIONAL));
          iHistory.setResponseDataType(dataType);
          iHistory.setText(externText);
          iHistory.setLikertSteps(rs.getInt(InputColumns.LIKERT_STEPS));
          iHistory.setLeftLabel(rs.getString(InputColumns.LEFT_LABEL));
          iHistory.setRightLabel(rs.getString(InputColumns.RIGHT_LABEL));
          
          externStringListLabel.setExternStringListLabelId(new PacoId(rs.getLong(ExternStringListLabelColumns.EXTERN_STRING_LIST_LABEL_ID), false));
          externStringListLabel.setLabel(rs.getString(ExternStringListLabelColumns.LABEL));

          inputOrderAndChoiceObj.setInput(iHistory);
          inputOrderAndChoiceObj.setInputOrder(rs.getInt(InputCollectionColumns.INPUT_ORDER));
          if (groupNameEVMMap.get(currentGroupName) == null) {
            // map does not contain group name
            returnEVMH = new ExperimentVersionGroupMapping();
            
            Long tempColId = rs.getLong(InputCollectionColumns.INPUT_COLLECTION_ID);
            icHistory = null;
            // nulls get converted to 0
            if (tempColId != 0) {
              icHistory = new InputCollection();
              icHistory.setInputCollectionId(tempColId);
              returnEVMHInputOrderAndChoices = Maps.newLinkedHashMap();
              returnEVMHInputOrderAndChoices.put(iHistory.getName().getLabel(), inputOrderAndChoiceObj);
              icHistory.setInputOrderAndChoices(returnEVMHInputOrderAndChoices);
            }
            
            if (externStringListLabel.getLabel() != null) {
              ccHistory = new ChoiceCollection();
              choices = Maps.newLinkedHashMap();
              choice = new Choice();
              choice.setChoiceLabel(externStringListLabel);
              choice.setChoiceOrder(rs.getInt(ChoiceCollectionColumns.CHOICE_ORDER));
              choices.put(externStringListLabel.getLabel(), choice);
              ccHistory.setChoiceCollectionId(rs.getLong(ChoiceCollectionColumns.CHOICE_COLLECTION_ID));
              ccHistory.setChoices(choices);
              inputOrderAndChoiceObj.setChoiceCollection(ccHistory);
            }
            
            returnEVMH.setInputCollection(icHistory);
            returnEVMH.setExperimentId(experimentIdFromResultSet);
            returnEVMH.setExperimentVersion(rs.getInt(ExperimentVersionGroupMappingColumns.EXPERIMENT_VERSION));
            returnEVMH.setExperimentInfo(eHistory);
            returnEVMH.setExperimentVersionMappingId(rs.getLong(ExperimentVersionGroupMappingColumns.EXPERIMENT_VERSION_GROUP_MAPPING_ID));
            returnEVMH.setEventsPosted(rs.getBoolean(ExperimentVersionGroupMappingColumns.EVENTS_POSTED));
            returnEVMH.setGroupInfo(gHistory);
            groupNameEVMMap.put(currentGroupName, returnEVMH);
           
          } else {
            // map  contains group name and group type
            returnEVMH = groupNameEVMMap.get(currentGroupName);
            InputOrderAndChoice alreadyPresentInputHistory = returnEVMH.getInputCollection().getInputOrderAndChoices().get(iHistory.getName().getLabel());
            if (alreadyPresentInputHistory != null) {
              if (externStringListLabel.getLabel() != null) {
                choice = new Choice();
                choice.setChoiceLabel(externStringListLabel);
                choice.setChoiceOrder(rs.getInt(ChoiceCollectionColumns.CHOICE_ORDER));
              }
              if (alreadyPresentInputHistory.getChoiceCollection() == null || alreadyPresentInputHistory.getChoiceCollection().getChoices() == null) {
                ccHistory = new ChoiceCollection();
                choices = Maps.newLinkedHashMap();
                choices.put(externStringListLabel.getLabel(), choice);
                ccHistory.setChoiceCollectionId(rs.getLong(ChoiceCollectionColumns.CHOICE_COLLECTION_ID));
                ccHistory.setChoices(choices);
                alreadyPresentInputHistory.setChoiceCollection(ccHistory);
              }  else {
                alreadyPresentInputHistory.getChoiceCollection().getChoices().put(externStringListLabel.getLabel(), choice);
              }
            } else {
              if (externStringListLabel.getLabel() != null) {
                ccHistory = new ChoiceCollection();
                choices = Maps.newLinkedHashMap();
                choice = new Choice();
                choice.setChoiceLabel(externStringListLabel);
                choice.setChoiceOrder(rs.getInt(ChoiceCollectionColumns.CHOICE_ORDER));
                choices.put(externStringListLabel.getLabel(), choice);
                ccHistory.setChoiceCollectionId(rs.getLong(ChoiceCollectionColumns.CHOICE_COLLECTION_ID));
                ccHistory.setChoices(choices);
                inputOrderAndChoiceObj.setChoiceCollection(ccHistory);
              }
              // add to input id map as a new entry with choices if any
              returnEVMH.getInputCollection().getInputOrderAndChoices().put(iHistory.getName().getLabel(), inputOrderAndChoiceObj);
            }
          }
        } // while
      } // rs
      
      log.info("version called" + experimentId + "-->" + version + " and query took " + (System.currentTimeMillis() - st1Time) + " and returned " + (recordCt-1));
    } finally {
      try {
        if(rs != null) {
          rs.close();
        }
        if (statementSelectExperiment != null) {
          statementSelectExperiment.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }
    return groupNameEVMMap;
  }

  private Integer getLatestExperimentVersion(Long experimentId) throws SQLException {
    Connection conn = null;
    ResultSet rs = null;
    PreparedStatement statementClosestExperimentVersion = null;
    Integer latestVersion = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementClosestExperimentVersion = conn.prepareStatement(QueryConstants.GET_LATEST_VERSION.toString());
      statementClosestExperimentVersion.setLong(1, experimentId);
      log.info("get latest version:" + statementClosestExperimentVersion.toString());
      rs = statementClosestExperimentVersion.executeQuery();
      while (rs.next()) {
        latestVersion = rs.getInt(ExperimentVersionGroupMappingColumns.EXPERIMENT_VERSION);
      }
      return latestVersion;

    } finally {
      try {
        if ( rs != null) {
          rs.close();
        }
        if (statementClosestExperimentVersion != null) {
          statementClosestExperimentVersion.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }
  }
  
  private List<Integer> getAllExperimentVersionsInCloudSql(Long experimentId) throws SQLException {
    Connection conn = null;
    ResultSet rs = null;
    PreparedStatement statementClosestExperimentVersion = null;
    List<Integer> allVersions = Lists.newArrayList();
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementClosestExperimentVersion = conn.prepareStatement(QueryConstants.GET_ALL_VERSIONS.toString());
      statementClosestExperimentVersion.setLong(1, experimentId);
      log.info("get all version:" + statementClosestExperimentVersion.toString());
      rs = statementClosestExperimentVersion.executeQuery();
      while (rs.next()) {
        allVersions.add(rs.getInt(ExperimentVersionGroupMappingColumns.EXPERIMENT_VERSION));
      }
      return allVersions;

    } finally {
      try {
        if ( rs != null) {
          rs.close();
        }
        if (statementClosestExperimentVersion != null) {
          statementClosestExperimentVersion.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }
  }
  
  @Override
  public ExperimentVersionGroupMapping getEVGMId(Long experimentId, Integer experimentVersion, String groupName) throws SQLException {
    Connection conn = null;
    ResultSet rs = null;
    PreparedStatement statementGetEvmIdsWithEventsPosted = null;
    ExperimentVersionGroupMapping evm  = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementGetEvmIdsWithEventsPosted = conn.prepareStatement(QueryConstants.GET_EVGM_ID.toString());
      statementGetEvmIdsWithEventsPosted.setLong(1, experimentId);
      statementGetEvmIdsWithEventsPosted.setInt(2, experimentVersion);
      statementGetEvmIdsWithEventsPosted.setString(3, groupName);
      log.info("get evm id with events posted" + statementGetEvmIdsWithEventsPosted.toString());
      rs = statementGetEvmIdsWithEventsPosted.executeQuery();
      InputCollection ic = new InputCollection();
      while (rs.next()) {
        evm = new ExperimentVersionGroupMapping();
        evm.setExperimentVersionMappingId(rs.getLong(ExperimentVersionGroupMappingColumns.EXPERIMENT_VERSION_GROUP_MAPPING_ID));
        evm.setExperimentId(experimentId);
        evm.setExperimentVersion(experimentVersion);
        ic.setInputCollectionId(rs.getLong(ExperimentVersionGroupMappingColumns.INPUT_COLLECTION_ID));
        evm.setInputCollection(ic);
        evm.setEventsPosted(rs.getBoolean(ExperimentVersionGroupMappingColumns.EVENTS_POSTED));
      }
    } finally {
      try {
        if ( rs != null) {
          rs.close();
        }
        if (statementGetEvmIdsWithEventsPosted != null) {
          statementGetEvmIdsWithEventsPosted.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }
    return evm;
  }


  @Override
  public void createEVGMByCopyingFromLatestVersion(Long experimentId, Integer experimentVersion) throws SQLException {
    Integer latestVersion = null;
    List<Integer> allVersions = getAllExperimentVersionsInCloudSql(experimentId);
    if (allVersions.size() > 0) {
      latestVersion = Collections.max(allVersions);
    } else {
      // These records should have been deleted in earlier migration steps. But, in case we run into this situation
      // we need to delete the experiment in exp id version group name table and do not create evgm records in cloud sql
      log.info("Unexpected scenario, cannot find EVGM for "+ experimentId + " and " + experimentVersion);
      return;
    }
    Connection conn = null;
    ResultSet rs = null;
    PreparedStatement statementClosestExperimentVersion = null;
    List<ExperimentVersionGroupMapping> newEVMRecords = Lists.newArrayList();
    ExperimentVersionGroupMapping evm = null;
    ExperimentDetail expFacet = null;
    GroupDetail group = null;
    InputCollection inputCollection = null;
    Long inputCollectionId = null;
    try {
      if (!allVersions.contains(experimentVersion)) {
        conn = CloudSQLConnectionManager.getInstance().getConnection();
        statementClosestExperimentVersion = conn.prepareStatement(QueryConstants.GET_ALL_EVM_RECORDS_FOR_VERSION.toString());
        statementClosestExperimentVersion.setLong(1, experimentId);
        statementClosestExperimentVersion.setInt(2, latestVersion);
        rs = statementClosestExperimentVersion.executeQuery();
        while (rs.next()) {
          evm = new ExperimentVersionGroupMapping();
          expFacet = new ExperimentDetail();
          group = new GroupDetail();
          expFacet.setExperimentDetailId(new PacoId(rs.getLong(ExperimentVersionGroupMappingColumns.EXPERIMENT_DETAIL_ID), false));
          group.setGroupId(new PacoId(rs.getLong(ExperimentVersionGroupMappingColumns.GROUP_DETAIL_ID), false));
          inputCollectionId = rs.getLong(ExperimentVersionGroupMappingColumns.INPUT_COLLECTION_ID);
          // when rs.getLong() has to return null, it returns as '0' 
          if ( inputCollectionId > 0L) {
            inputCollection = new InputCollection();
            inputCollection.setInputCollectionId(inputCollectionId);
          } else {
            inputCollection = null;
          }
          evm.setExperimentId(experimentId);
          // just change the version alone
          evm.setExperimentVersion(experimentVersion);
          evm.setExperimentInfo(expFacet);
          evm.setGroupInfo(group);
          evm.setInputCollection(inputCollection);
          evm.setSource("Copied From Version:"+ latestVersion);
          newEVMRecords.add(evm);
        }
        createExperimentVersionGroupMapping(newEVMRecords);
      }
    } finally {
      try {
        if ( rs != null) {
          rs.close();
        }
        if (statementClosestExperimentVersion != null) {
          statementClosestExperimentVersion.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }
  }
  
  @Override
  public ExperimentVersionGroupMapping findMatchingEVGMRecord(Event event, Map<String, ExperimentVersionGroupMapping> allEVMMap, boolean migrationFlag) throws Exception{
    ExperimentVersionGroupMapping returnEVM = null;
    CSExperimentVersionGroupMappingDao daoImpl = new CSExperimentVersionGroupMappingDaoImpl();
    Long expId = Long.parseLong(event.getExperimentId());
    // fix the group name
    daoImpl.ensureCorrectGroupName(event, allEVMMap);
    // if event is posted for a version where we do not have experiment mapping records
    daoImpl.ensureEVMRecord(expId,event.getId(), event.getExperimentName(), event.getExperimentVersion(), event.getExperimentGroupName(), event.getWho(), event.getWhat(), migrationFlag, allEVMMap);
    returnEVM = allEVMMap.get(event.getExperimentGroupName());
    if (returnEVM != null)  {
      String mightBeModifiedGroupName = returnEVM.getGroupInfo().getName();
      allEVMMap.put(mightBeModifiedGroupName, returnEVM);
    } else {
      throw new Exception("No corresponding group in EVGM table"+ event.getExperimentId() + "--" + event.getExperimentGroupName());
    }
    return returnEVM;
  }
  
  @Override
  public void ensureEVMRecord(Long experimentId, Long eventId, String experimentName, Integer experimentVersion, String groupName, String whoEmail, Set<What> whatSet, boolean migrationFlag, Map<String, ExperimentVersionGroupMapping> allEVMRecords) throws Exception {
    GroupTypeEnum matchingGroupType = null;
    CSInputCollectionDao inputCollDao = new CSInputCollectionDaoImpl();
    // 1. find matching grp name for these outputs
    matchingGroupType = findMatchingGroupType(groupName);
    ExperimentVersionGroupMapping evm = new ExperimentVersionGroupMapping();
    evm.setExperimentId(experimentId);
    evm.setExperimentVersion(experimentVersion);
    ExperimentVersionGroupMapping matchingEVMInDB = allEVMRecords.get(groupName);
    
    if (matchingEVMInDB == null && migrationFlag) {
      ensureEVMMissingGroupName(groupName, whatSet, allEVMRecords, matchingGroupType, evm);
    } else if (matchingEVMInDB != null) {
      Map<String, InputOrderAndChoice> iocsInDB = null;
      List<String> newVariables = Lists.newArrayList();
      Integer numberOfGroups = getNumberOfGroups(evm.getExperimentId(), evm.getExperimentVersion());
      evm.setExperimentInfo(matchingEVMInDB.getExperimentInfo());
      evm.setGroupInfo(matchingEVMInDB.getGroupInfo());
      evm.setInputCollection(matchingEVMInDB.getInputCollection());
      evm.setExperimentVersionMappingId(matchingEVMInDB.getExperimentVersionMappingId());
      // check for inputs
      if (matchingEVMInDB.getInputCollection() != null) {
        // some inputs are already there ??? not needed
        iocsInDB = matchingEVMInDB.getInputCollection().getInputOrderAndChoices();
        for (What eachWhat : whatSet) {
          // input alone missing
          if (iocsInDB.get(eachWhat.getName()) == null) {
            // check and create new ic id
            newVariables.add(eachWhat.getName());
          }
        }
      } else {
        //  grp has no input collection at all in DB 
        if (whatSet != null && whatSet.size() > 0 ) {
          InputCollection ic = new InputCollection();
          iocsInDB = Maps.newHashMap();
          ic.setInputOrderAndChoices(iocsInDB);
          for (What eachWhat : whatSet) {
            // check and create new input
            newVariables.add(eachWhat.getName());
          }
          evm.setInputCollection(ic);
        } else {
          evm.setInputCollection(null);
        }
      }
      if (newVariables.size() > 0) {
        Integer noOfInputCollectionIdsPresentInDB = getInputCollectionIdCountForExperiment(matchingEVMInDB.getExperimentId(), evm.getInputCollection().getInputCollectionId()); 
        if  (noOfInputCollectionIdsPresentInDB > 1) {
            Boolean uniqueFlag = true;
            Long newInputCollectionId = inputCollDao.getInputCollectionId(evm.getExperimentId(), evm.getExperimentVersion(), numberOfGroups, uniqueFlag);
            evm.getInputCollection().setInputCollectionId(newInputCollectionId);
            // add all variables to new input coll id
            addWhatsToInputCollection(evm, newVariables, true);
            // update egvm 
            updateInputCollectionId(evm, evm.getInputCollection().getInputCollectionId());
        }  else {
          // add new variables to existing input coll id
          addWhatsToInputCollection(evm, newVariables, false);
        }
        allEVMRecords.put(groupName, evm);
      }
    } 

  }

  private void ensureEVMMissingGroupName(String groupName, Set<What> whatSet,
                                                             Map<String, ExperimentVersionGroupMapping> allEVMRecords,
                                                             GroupTypeEnum matchingGroupType,
                                                             ExperimentVersionGroupMapping evm) throws SQLException,
                                                                                           Exception {
    // no matching group name in DB. For migrated experiments, we can recreate the grps.
    // get some exp facet object to use
    if (allEVMRecords.size() == 0) {
      return;
    } 
    Iterator<String> evmItr = allEVMRecords.keySet().iterator();
    ExperimentDetail expInfo = null;
    while(evmItr.hasNext()) {
      String grpName = evmItr.next();
      expInfo = allEVMRecords.get(grpName).getExperimentInfo();
      break;
    }
    evm.setExperimentInfo(expInfo);
    // create group, find grp type, then input collection
    addGroupInfoToDB(evm, groupName, matchingGroupType);
    if (matchingGroupType.getGroupTypeId() == GroupTypeEnum.SURVEY.getGroupTypeId()) {
      List<String> whatList = Lists.newArrayList(); 
      for (What eachWhat : whatSet) {
        whatList.add(eachWhat.getName());
      }
      if (whatList.size() > 0) {
        Boolean uniqueFlag = true;
        // flag can be false
        // plain number of groups* 1000 shd be unique
        addAllWhatsToInputCollection(evm, whatList, uniqueFlag);
      }
    } else {
      CSGroupTypeInputMappingDao gtimDao = new CSGroupTypeInputMappingDaoImpl();
      List<Input> inputLst = gtimDao.getAllFeatureInputs().get(matchingGroupType.name());
      List<String> newWhats = Lists.newArrayList();
      Boolean uniqueFlag = true;
      // flag can be false
      // plain number of groups* 1000 shd be unique
      addInputsToInputCollection(evm, inputLst, uniqueFlag);
      Map<String, InputOrderAndChoice> iocsInDB = evm.getInputCollection().getInputOrderAndChoices();
      for (What eachWhat : whatSet) {
        // for scripted inputs
        if (iocsInDB.get(eachWhat.getName()) == null) {
          newWhats.add(eachWhat.getName());
        }
      }
      if (newWhats.size() > 0 ) {
        // just add new ones, since they are scripted
        Boolean addOldOnesFlag = false;
        addWhatsToInputCollection(evm, newWhats, addOldOnesFlag);  
      }
      
    }
    //  insert to db, then return right away
    createExperimentVersionGroupMapping(Lists.newArrayList(evm));
    allEVMRecords.put(groupName, evm);
  }
  
  private void  addGroupInfoToDB(ExperimentVersionGroupMapping evm, String groupName, GroupTypeEnum groupType) throws SQLException {
    CSGroupDao grpDaoImp = new CSGroupDaoImpl();
    GroupDetail grp = new GroupDetail();
    if (!groupType.equals(GroupTypeEnum.SURVEY)) {
      grp.setName(groupType.name());
    } else {
      grp.setName(groupName);
    }
    
    grp.setGroupTypeId(groupType.getGroupTypeId());
    grp.setFixedDuration(false);
    grp.setRawDataAccess(false);
    if (evm.getSource() == null) {
      evm.setSource("Recreating group Info");
    }
    grpDaoImp.insertGroup(Lists.newArrayList(grp));
    evm.setGroupInfo(grp);
  }
  
  private void  addAllWhatsToInputCollection(ExperimentVersionGroupMapping evm, List<String> inputsToBeAdded, Boolean uniqueFlag) throws Exception {
    CSInputCollectionDao inputCollectionDaoImpl = new CSInputCollectionDaoImpl();
    InputCollection inputCollection = new InputCollection();
    CSInputDao inputDaoImpl = new CSInputDaoImpl();
    List<Input> inputs = inputDaoImpl.insertVariableNames(inputsToBeAdded);
    Integer noOfGroups = getNumberOfGroups(evm.getExperimentId(), evm.getExperimentVersion());
    Long newInputCollectionId = inputCollectionDaoImpl.getInputCollectionId(evm.getExperimentId(), evm.getExperimentVersion(), noOfGroups, uniqueFlag);
    inputCollection.setInputCollectionId(newInputCollectionId);
    inputCollectionDaoImpl.addInputsToInputCollection(evm.getExperimentId(), inputCollection, inputs);
    inputCollection.setInputOrderAndChoices(convertInputListToInputOrderAndChoice(inputs));
    evm.setInputCollection(inputCollection);
  }
  
  @Override
  public void  addWhatsToInputCollection(ExperimentVersionGroupMapping evm, List<String> inputsToBeAdded, boolean includeOldOnes) throws Exception {
    InputCollection inputCollection = evm.getInputCollection(); 
    CSInputDao inputDaoImpl = new CSInputDaoImpl();
    List<Input> newInputs = inputDaoImpl.insertVariableNames(inputsToBeAdded);
    // find all existing inputs
    Map<String, InputOrderAndChoice> existingVariablesMap = evm.getInputCollection().getInputOrderAndChoices();
    List<Input> existingInputs = Lists.newArrayList();
    Iterator<String> varNameItr = existingVariablesMap.keySet().iterator();
    while (varNameItr.hasNext()) {
      String existingVarName = varNameItr.next();
      existingInputs.add(existingVariablesMap.get(existingVarName).getInput());
    }
    if (includeOldOnes) {
      inputCollectionDaoImpl.addInputsToInputCollection(evm.getExperimentId(), inputCollection, existingInputs);
    } 
    inputCollectionDaoImpl.addInputsToInputCollection(evm.getExperimentId(), inputCollection, newInputs);
    
    existingInputs.addAll(newInputs);
    inputCollection.setInputOrderAndChoices(convertInputListToInputOrderAndChoice(existingInputs));
    evm.setInputCollection(inputCollection);
  }
  
  @Override
  public boolean updateInputCollectionId(ExperimentVersionGroupMapping evm, Long newInputCollectionId) throws SQLException {
    Connection conn = null;
    PreparedStatement statementUpdateEvent = null;
    String updateQuery = QueryConstants.UPDATE_INPUT_COLLECTION_ID_FOR_EVGM_ID.toString();
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      
      statementUpdateEvent = conn.prepareStatement(updateQuery);
      statementUpdateEvent.setLong(1, evm.getInputCollection().getInputCollectionId());
      statementUpdateEvent.setLong(2, evm.getExperimentVersionMappingId());
      statementUpdateEvent.executeUpdate();
    } finally {
      try {
        if (statementUpdateEvent != null) {
          statementUpdateEvent.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }
    return true;
  }
  
  private void addInputsToInputCollection(ExperimentVersionGroupMapping evm, List<Input> inputs, Boolean uniqueFlag) throws SQLException {
    CSInputCollectionDao inputCollectionDaoImpl = new CSInputCollectionDaoImpl();
    InputCollection inputCollection = evm.getInputCollection(); 
    if (inputCollection == null) {
      inputCollection = new InputCollection();
      Integer noOfGroups = getNumberOfGroups(evm.getExperimentId(), evm.getExperimentVersion());
      Long newInputCollectionId = inputCollectionDaoImpl.getInputCollectionId(evm.getExperimentId(), evm.getExperimentVersion(), noOfGroups, uniqueFlag);
      inputCollection.setInputCollectionId(newInputCollectionId);
    }
    inputCollection.setInputOrderAndChoices(convertInputListToInputOrderAndChoice(inputs));
    evm.setInputCollection(inputCollection);
    inputCollectionDaoImpl.addInputsToInputCollection(evm.getExperimentId(), inputCollection, inputs);
  }
  
  private GroupTypeEnum findMatchingGroupType(String groupName) throws SQLException {
    GroupTypeEnum matchingGroupType = GroupTypeEnum.SURVEY;
    for (GroupTypeEnum gt : GroupTypeEnum.values()) {
      if (gt.name().equals(groupName)) { 
        matchingGroupType = gt;
      }
    }
    return matchingGroupType;
  }
  
  private Map<String, InputOrderAndChoice> convertInputListToInputOrderAndChoice(List<Input> inputs) {
    Map<String, InputOrderAndChoice> varNameIocMap = Maps.newHashMap();
    InputOrderAndChoice ioc = null;
    for (Input input : inputs)  {
      ioc = convertInputToInputOrderAndChoice(input);
      varNameIocMap.put(input.getName().getLabel(), ioc);
    }
    return varNameIocMap;
  }
  
  private InputOrderAndChoice convertInputToInputOrderAndChoice(Input input) {
    InputOrderAndChoice ioc = null;
    ioc = new InputOrderAndChoice();
    ioc.setChoiceCollection(null);
    ioc.setInput(input);
    ioc.setInputOrder(-99);
    return ioc;
  }

  @Override
  public Integer getNumberOfGroups(Long experimentId, Integer version) throws SQLException {
    Integer totalRecords = 0;
    Connection conn = null;
    ResultSet rs = null;
    ResultSet rs1 = null;
    PreparedStatement statementSelectExperimentVersionMapping = null;
   
    final String updateValueForExperimentVersionMappingId = QueryConstants.GET_EGVM_ID_FOR_EXP_ID_AND_VERSION.toString();
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();

      statementSelectExperimentVersionMapping = conn.prepareStatement(updateValueForExperimentVersionMappingId);

      statementSelectExperimentVersionMapping.setLong(1, experimentId);
      statementSelectExperimentVersionMapping.setInt(2, version);
      
      rs = statementSelectExperimentVersionMapping.executeQuery();
      while (rs.next()) {
        totalRecords ++;
      } 
    } finally {
      try {
        if ( rs != null) {
          rs.close();
        }
        if ( rs1 != null) {
          rs1.close();
        }
        if (statementSelectExperimentVersionMapping != null) {
          statementSelectExperimentVersionMapping.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }
    return totalRecords;
  }
  
  @Override
  public boolean updateEventsPosted(Long egvId) throws SQLException {
    Connection conn = null;
    PreparedStatement statementUpdateEvent = null;
    String updateQuery = QueryConstants.UPDATE_EVENTS_POSTED_FOR_EGVM_ID.toString();
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      
      statementUpdateEvent = conn.prepareStatement(updateQuery);
      statementUpdateEvent.setLong(1, egvId);
      statementUpdateEvent.executeUpdate();
      log.info("updated events posted  as 1 for egv id:" + egvId );
    } finally {
      try {
        if (statementUpdateEvent != null) {
          statementUpdateEvent.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }
    return true;
  }
  
  @Override
  public boolean updateEventsPosted(Set<Long> egvmIds) throws SQLException {
    Connection conn = null;
    PreparedStatement statementUpdateEvent = null;
    String updateQuery = QueryConstants.UPDATE_EVENTS_POSTED_FOR_EGVM_ID.toString();
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      
      statementUpdateEvent = conn.prepareStatement(updateQuery);
      for (Long egvmId : egvmIds) {
        statementUpdateEvent.setLong(1, egvmId);
        statementUpdateEvent.addBatch();
      }
      statementUpdateEvent.executeBatch();
      log.info("updated events posted  as 1 for egvm ids:" + egvmIds );
    } finally {
      try {
        if (statementUpdateEvent != null) {
          statementUpdateEvent.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }
    return true;
  }
  
  @Override
  public Integer getInputCollectionIdCountForExperiment(Long expId, Long icId) throws SQLException {
    Connection conn = null;
    PreparedStatement statementDuplicateIcId = null;
    ResultSet rs = null;
    int ct = 0;
    String selectQuery = QueryConstants.GET_COUNT_INPUT_COLLECTION_ID_IN_EXPERIMENT.toString();
    try {
      if ( icId == null) { 
        // For null, we cannot assume it belongs to the same group, but different version. so we need to assume, if the ic id is null,
        // so, we fake a return count of 2, which is other versions.
        return 2;
      }
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      
      statementDuplicateIcId = conn.prepareStatement(selectQuery);
      statementDuplicateIcId.setLong(1, expId);
      statementDuplicateIcId.setLong(2, icId);
      
      rs = statementDuplicateIcId.executeQuery();
      while (rs.next()) {
        ct = rs.getInt(1);
      }
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
        if (statementDuplicateIcId != null) {
          statementDuplicateIcId.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }
    return ct;
  }

  @Override
  public Long getNumberOfEvents(Long experimentgroupVersionMappingId, Integer anonWhoId,
                                Long inputId) throws SQLException {
    Connection conn = null;
    ResultSet rs = null;
    PreparedStatement statementClosestExperimentVersion = null;
    Long noOfEvents = 0L;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementClosestExperimentVersion = conn.prepareStatement(QueryConstants.GET_EVENTS_COUNT.toString());
      statementClosestExperimentVersion.setLong(1, experimentgroupVersionMappingId);
      statementClosestExperimentVersion.setLong(2, anonWhoId);
      statementClosestExperimentVersion.setLong(3, inputId);
      rs = statementClosestExperimentVersion.executeQuery();
      while (rs.next()) {
        noOfEvents = rs.getLong(1);
      }
      log.info("get number of events:" + statementClosestExperimentVersion.toString() + "--" + noOfEvents);
    } finally {
      try {
        if ( rs != null) {
          rs.close();
        }
        if (statementClosestExperimentVersion != null) {
          statementClosestExperimentVersion.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }
    return noOfEvents;
  }
  
  private Map<String, What> convertFromWhatToMap(Set<What> whatSet) {
    Map<String, What> varNames = Maps.newHashMap();
    Iterator<What> whatItr = whatSet.iterator();
    What crtWhat = null;
    while (whatItr.hasNext()) {
      crtWhat = whatItr.next(); 
      varNames.put(crtWhat.getName(), crtWhat);
    }
    return varNames;
  }

  @Override
  public void ensureCorrectGroupName(Event eventDao, Map<String, ExperimentVersionGroupMapping> allEVMMap) throws Exception {
    String featureName = null;
    CSGroupTypeInputMappingDao inputMappingDao = new CSGroupTypeInputMappingDaoImpl();
    Map<String, List<String>>inputMap = inputMappingDao.getAllPredefinedFeatureVariableNames();
    Iterator<String> predefinedInputIterator = inputMap.keySet().iterator();
    Map<String, What> inputsInEvent = convertFromWhatToMap(eventDao.getWhat());
    boolean groupNameChanged = false;
    // for each predefined feature
    log.info("old group name: " + eventDao.getExperimentGroupName());
    while (predefinedInputIterator.hasNext()) {
      featureName = predefinedInputIterator.next();
      if (featureName.equals(GroupTypeEnum.NOTIFICATION.name())) { 
        continue;
      }
      List<String> featuresInputVariableNames = inputMap.get(featureName);
      
      for (String eachFeatureVariableName : featuresInputVariableNames) {
        if (inputsInEvent.containsKey(eachFeatureVariableName)) {
          eventDao.setExperimentGroupName(featureName);
          log.info("new sys group name:"+ eventDao.getExperimentGroupName());
          groupNameChanged = true;
          break;
        }
      }
      if (groupNameChanged) {
        break;
      }
    } // predefined map of all predefined grps
    // Currently, IOS versions supports only 1 group. Also, for that single group, client does not send group name with any of the events
    // we need to identify the IOS events and populate the group name 
    if (eventDao.getExperimentGroupName() == null) {
      if (eventDao.getAppId().equalsIgnoreCase(Constants.IOS)) {
        eventDao.setExperimentGroupName(getNonSystemGroupNameFromEVGMRecords(allEVMMap));
      } else {
        eventDao.setExperimentGroupName(Constants.UNKNOWN);
      }
    }
    log.info("new group name:"+ eventDao.getExperimentGroupName());
    
  }
  
  @Override
  public void ensureSystemGroupName(Event eventDao, Map<String, ExperimentVersionGroupMapping> allEVMMap) throws Exception {
    
    CSGroupTypeInputMappingDao inputMappingDao = new CSGroupTypeInputMappingDaoImpl();
    Map<String, List<String>>inputMap = inputMappingDao.getAllPredefinedFeatureVariableNames();
    Map<String, What> inputsInEvent = convertFromWhatToMap(eventDao.getWhat());
    // for each predefined feature
    log.info("sys - old group name: " + eventDao.getExperimentGroupName());
    List<String> featuresInputVariableNames = inputMap.get(GroupTypeEnum.SYSTEM.name());
    
    for (String eachFeatureVariableName : featuresInputVariableNames) {
      if (inputsInEvent.containsKey(eachFeatureVariableName)) {
        eventDao.setExperimentGroupName(GroupTypeEnum.SYSTEM.name());
        break;
      }
    }
    
//    if (eventDao.getWhat() == null || (eventDao.getWhat().size() == 0 && eventDao.getScheduledTime() != null)) {
//      eventDao.setExperimentGroupName(GroupTypeEnum.SYSTEM.name());
//    }
    // Currently, IOS versions supports only 1 group. Also, for that single group, client does not send group name with any of the events
    // we need to identify the IOS events and populate the group name 
    if (eventDao.getExperimentGroupName() == null) {
      if (eventDao.getAppId().equalsIgnoreCase(Constants.IOS)) {
        eventDao.setExperimentGroupName(getNonSystemGroupNameFromEVGMRecords(allEVMMap));
      } else {
        eventDao.setExperimentGroupName(Constants.UNKNOWN);
      }
    }
    log.info("new group name:"+ eventDao.getExperimentGroupName());
    
  }
  
  private String getNonSystemGroupNameFromEVGMRecords(Map<String, ExperimentVersionGroupMapping> allEVMMap) {
    String nonSystemGroupName = null;
    String crtGrpName = null;
    ExperimentVersionGroupMapping evgm = null;
    Iterator<String> grpNameItr = allEVMMap.keySet().iterator();
    while (grpNameItr.hasNext()) {
      crtGrpName = grpNameItr.next();
      evgm = allEVMMap.get(crtGrpName);
      if (evgm.getGroupInfo().getGroupTypeId().intValue() != GroupTypeEnum.SYSTEM.getGroupTypeId()) {
        nonSystemGroupName = evgm.getGroupInfo().getName();
        break;
      }
    }
    log.info("non sys group:" + nonSystemGroupName);
    return nonSystemGroupName;
  }
}
