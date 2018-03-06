package com.google.sampling.experiential.dao.impl;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
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
import com.google.sampling.experiential.cloudsql.columns.ExperimentGroupVersionMappingColumns;
import com.google.sampling.experiential.cloudsql.columns.ExternStringInputColumns;
import com.google.sampling.experiential.cloudsql.columns.ExternStringListLabelColumns;
import com.google.sampling.experiential.cloudsql.columns.GroupDetailColumns;
import com.google.sampling.experiential.cloudsql.columns.InformedConsentColumns;
import com.google.sampling.experiential.cloudsql.columns.InputCollectionColumns;
import com.google.sampling.experiential.cloudsql.columns.InputColumns;
import com.google.sampling.experiential.cloudsql.columns.UserColumns;
import com.google.sampling.experiential.dao.CSEventOutputDao;
import com.google.sampling.experiential.dao.CSExperimentDetailDao;
import com.google.sampling.experiential.dao.CSExperimentVersionMappingDao;
import com.google.sampling.experiential.dao.CSFailedEventDao;
import com.google.sampling.experiential.dao.CSGroupDao;
import com.google.sampling.experiential.dao.CSGroupTypeInputMappingDao;
import com.google.sampling.experiential.dao.CSInformedConsentDao;
import com.google.sampling.experiential.dao.CSInputCollectionDao;
import com.google.sampling.experiential.dao.CSInputDao;
import com.google.sampling.experiential.dao.dataaccess.Choice;
import com.google.sampling.experiential.dao.dataaccess.ChoiceCollection;
import com.google.sampling.experiential.dao.dataaccess.DataType;
import com.google.sampling.experiential.dao.dataaccess.ExperimentDetail;
import com.google.sampling.experiential.dao.dataaccess.ExperimentVersionMapping;
import com.google.sampling.experiential.dao.dataaccess.ExternStringInput;
import com.google.sampling.experiential.dao.dataaccess.ExternStringListLabel;
import com.google.sampling.experiential.dao.dataaccess.GroupDetail;
import com.google.sampling.experiential.dao.dataaccess.InformedConsent;
import com.google.sampling.experiential.dao.dataaccess.Input;
import com.google.sampling.experiential.dao.dataaccess.InputCollection;
import com.google.sampling.experiential.dao.dataaccess.InputOrderAndChoice;
import com.google.sampling.experiential.dao.dataaccess.User;
import com.google.sampling.experiential.model.What;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.google.sampling.experiential.server.ExperimentDAOConverter;
import com.google.sampling.experiential.server.IdGenerator;
import com.google.sampling.experiential.server.PacoId;
import com.google.sampling.experiential.server.QueryConstants;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.GroupTypeEnum;
import com.pacoapp.paco.shared.util.ErrorMessages;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.insert.Insert;

public class CSExperimentVersionMappingDaoImpl implements CSExperimentVersionMappingDao {

  public static final Logger log = Logger.getLogger(CSExperimentVersionMappingDaoImpl.class.getName());
  private static List<Column> experimentVersionMappingColList = Lists.newArrayList();
  
  CSExperimentDetailDao experimentDaoImpl = new CSExperimentDetailDaoImpl();
  CSGroupDao groupDaoImpl = new CSGroupDaoImpl();
  CSInputDao inputDaoImpl = new CSInputDaoImpl();
  CSInputCollectionDao inputCollectionDaoImpl = new CSInputCollectionDaoImpl();
  
  static {
    experimentVersionMappingColList.add(new Column(ExperimentGroupVersionMappingColumns.EXPERIMENT_ID));
    experimentVersionMappingColList.add(new Column(ExperimentGroupVersionMappingColumns.EXPERIMENT_VERSION));
    experimentVersionMappingColList.add(new Column(ExperimentGroupVersionMappingColumns.EXPERIMENT_DETAIL_ID));
    experimentVersionMappingColList.add(new Column(ExperimentGroupVersionMappingColumns.GROUP_DETAIL_ID));
    experimentVersionMappingColList.add(new Column(ExperimentGroupVersionMappingColumns.INPUT_COLLECTION_ID));
    experimentVersionMappingColList.add(new Column(ExperimentGroupVersionMappingColumns.SOURCE));
  } 
  
  @Override
  public boolean createExperimentVersionMapping(List<ExperimentVersionMapping> experimentVersionMappingLst) {
    
    ExpressionList insertExperimentVersionMappingExprList = new ExpressionList();
    List<Expression> exp = Lists.newArrayList();
    Insert experimentVersionMappingInsert = new Insert();
    Connection conn = null;
    PreparedStatement statementCreateExperimentVersionMapping = null;
    ResultSet rs = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      conn.setAutoCommit(false);
      experimentVersionMappingInsert.setTable(new Table(ExperimentGroupVersionMappingColumns.TABLE_NAME));
      experimentVersionMappingInsert.setUseValues(true);
      insertExperimentVersionMappingExprList.setExpressions(exp);
      experimentVersionMappingInsert.setItemsList(insertExperimentVersionMappingExprList);
      experimentVersionMappingInsert.setColumns(experimentVersionMappingColList);
      // Adding ? for prepared stmt
      for (Column c : experimentVersionMappingColList) {
        ((ExpressionList) experimentVersionMappingInsert.getItemsList()).getExpressions().add(new JdbcParameter());
      }

      statementCreateExperimentVersionMapping = conn.prepareStatement(experimentVersionMappingInsert.toString(), Statement.RETURN_GENERATED_KEYS);
      for (ExperimentVersionMapping evm : experimentVersionMappingLst) {
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
        for (ExperimentVersionMapping evm : experimentVersionMappingLst) {
          if ( generatedKeys == null || ! generatedKeys.next()){
            log.warning("Unable to retrieve all generated keys");
          }
          evm.setExperimentVersionMappingId(generatedKeys.getLong(1));
        }
      }
      conn.commit();
    } catch(SQLException sqle) {
      log.warning("Exception while inserting to experiment_version_mapping table:" +  sqle);
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
  public void ensureExperimentVersionMapping(ExperimentDAO experimentDao) throws SQLException {
    List<ExperimentVersionMapping> allGroupsInNewVersion = Lists.newArrayList();
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
      Map<String, ExperimentVersionMapping> allGroupsInPrevVersion = getAllGroupsInVersion(experimentDao.getId(), experimentDao.getVersion()-1);
      allGroupsInNewVersion = converter.convertToExperimentVersionMapping(experimentDao);
      // iterating old version 
      if (allGroupsInPrevVersion != null) {
        Iterator<Entry<String, ExperimentVersionMapping>> itr = allGroupsInPrevVersion.entrySet().iterator();
        while (itr.hasNext()) {
          // we need to get just once for an experiment. all groups share the experiment info
          Entry<String, ExperimentVersionMapping> crtMapping = itr.next();
          oldExperimentInfo = crtMapping.getValue().getExperimentInfo();
          oldGroupMap.put(crtMapping.getValue().getGroupInfo().getName(), crtMapping.getValue().getGroupInfo());
          oldGrpNameInputCollection.put(crtMapping.getValue().getGroupInfo().getName(), crtMapping.getValue().getInputCollection());
        }
      }
      // iterating new version
      for (ExperimentVersionMapping evm : allGroupsInNewVersion) {
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
      // if no facet id, there is some change in expt info, so we need to insert into experiment table
      if (newExperimentInfo.getExperimentDetailId() == null) {
        experimentDaoImpl.insertExperimentDetail(newExperimentInfo);
      }
      // Item 3 - Groups
      if ( oldGroupMap != null) {
        updateNewGroupsWithOldId(oldGroupMap, newGroupList);
        groupDaoImpl.insertGroup(newGroupList);
      }
      // Item 4 - Inputs
      updateNewInputCollectionWithOldId(oldGrpNameInputCollection, newGrpNameInputCollection);
      inputCollectionDaoImpl.createInputCollectionId(experimentDao, newGrpNameInputCollection, oldGrpNameInputCollection);
      
      int grpCt = 0;
      for (ExperimentVersionMapping evm : allGroupsInNewVersion) {
        evm.setExperimentId(experimentDao.getId());
        evm.setExperimentVersion(experimentDao.getVersion());
        evm.setExperimentInfo(newExperimentInfo);
        evm.setGroupInfo(newGroupList.get(grpCt++));
        evm.setInputCollection(newGrpNameInputCollection.get(evm.getGroupInfo().getName()));
      }
      
      createExperimentVersionMapping(allGroupsInNewVersion);
      log.info("experiment version mapping process ends");
    } else {
      log.warning("experiment version mapping ends - version already exists");
    }
  }

  @Override
  public Map<String, ExperimentVersionMapping> getAllGroupsInVersion(Long experimentId, Integer version) throws SQLException {
    log.info("version called" + experimentId + "-->" + version);
    Long experimentIdFromResultSet = null;
    Connection conn = null;
    PreparedStatement statementSelectExperiment = null;
    Map<String, ExperimentVersionMapping> groupNameEVMMap = null;
    ExperimentVersionMapping returnEVMH = null;
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

    String query = "select * from experiment_group_version_mapping evmh join experiment_detail eh on evmh.experiment_detail_id = eh.experiment_detail_id " + 
            " join group_detail gh on evmh.group_detail_id = gh.group_detail_id " +
            " join `group_type` gt on gh.group_type_id = gt.group_type_id " + 
            " left join input_collection ich on ich.input_collection_id = evmh.input_collection_id and ich.experiment_ds_id = evmh.experiment_id " +
            " left join input ih on ich.input_id = ih.input_id " +
            " join user u on eh.creator = u.user_id " +
            " left join choice_collection cch on ich.choice_collection_id = cch.choice_collection_id and ich.experiment_ds_id = cch.experiment_ds_id " +
            " left join informed_consent ic on eh.informed_consent_id = ic.informed_consent_id and evmh.experiment_id = ic.experiment_id " +
            " left join extern_string_list_label esll on cch.choice_id = esll.extern_string_list_label_id " + 
            " left join extern_string_input esi1 on ih.text_id = esi1.extern_string_input_id " + 
            " left join extern_string_input esi2 on ih.name_id = esi2.extern_string_input_id " +
            " left join data_type dt on ih.response_data_type_id = dt.data_type_id " +
            " where evmh.experiment_id=? and evmh.experiment_version=? " ;

    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementSelectExperiment = conn.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
      Long st1Time = System.currentTimeMillis();
      statementSelectExperiment.setLong(1, experimentId);
      statementSelectExperiment.setInt(2, version);
      String selString = statementSelectExperiment.toString();
      
      log.info("step 1 " + selString.substring(selString.indexOf(":")));
      rs = statementSelectExperiment.executeQuery();
      
      if (rs != null) {
        while (rs.next()) {
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
          
          experimentIdFromResultSet = rs.getLong(ExperimentGroupVersionMappingColumns.EXPERIMENT_ID);
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
            returnEVMH = new ExperimentVersionMapping();
            
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
            returnEVMH.setExperimentVersion(rs.getInt(ExperimentGroupVersionMappingColumns.EXPERIMENT_VERSION));
            returnEVMH.setExperimentInfo(eHistory);
            returnEVMH.setExperimentVersionMappingId(rs.getLong(ExperimentGroupVersionMappingColumns.EXPERIMENT_GROUP_VERSION_MAPPING_ID));
            returnEVMH.setGroupInfo(gHistory);
            groupNameEVMMap.put(currentGroupName, returnEVMH);
           
          } else {
            // map  contains group name and group type
            returnEVMH = groupNameEVMMap.get(currentGroupName);
            InputOrderAndChoice alreadyPresentInputHistory = returnEVMH.getInputCollection().getInputOrderAndChoices().get(iHistory.getName().getLabel());
            if (alreadyPresentInputHistory != null) {
              // add to choices map of already present input id
              if (externStringListLabel.getLabel() != null) {
                choice = new Choice();
                choice.setChoiceLabel(externStringListLabel);
                choice.setChoiceOrder(rs.getInt(ChoiceCollectionColumns.CHOICE_ORDER));
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
      
      log.info("query took " + (System.currentTimeMillis() - st1Time) + " and returned " + recordCt);
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

  @Override
  public PacoId getExperimentVersionMappingId(Long experimentId, Integer version, String groupName) throws SQLException {
    PacoId returnId = new PacoId();
    Connection conn = null;
    ResultSet rs = null;
    ResultSet rs1 = null;
    int ct = 1;
    PreparedStatement statementSelectExperimentVersionMapping = null;
    PreparedStatement statementCreateExperimentVersionMapping = null;
    final String updateValueForExperimentVersionMappingId = "select "+ ExperimentGroupVersionMappingColumns.EXPERIMENT_GROUP_VERSION_MAPPING_ID +" from " + ExperimentGroupVersionMappingColumns.TABLE_NAME + " evm join "  + GroupDetailColumns.TABLE_NAME + " g on evm. " + GroupDetailColumns.GROUP_DETAIL_ID + " g." + GroupDetailColumns.GROUP_DETAIL_ID +" where " + ExperimentGroupVersionMappingColumns.EXPERIMENT_ID + " = ? and "  + GroupDetailColumns.NAME + " = ? and "  + ExperimentGroupVersionMappingColumns.EXPERIMENT_VERSION + " = ? " ;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementSelectExperimentVersionMapping = conn.prepareStatement(updateValueForExperimentVersionMappingId);
      if (version == null) {
        version = 0;
      }
      statementSelectExperimentVersionMapping.setLong(ct++, experimentId);
      statementSelectExperimentVersionMapping.setString(ct++, groupName);
      statementSelectExperimentVersionMapping.setInt(ct++, version);
      
      rs = statementSelectExperimentVersionMapping.executeQuery();
      if (rs.next()) {
        returnId.setIsCreatedWithThisCall(false);
        returnId.setId(new Long(rs.getInt(ExperimentGroupVersionMappingColumns.EXPERIMENT_GROUP_VERSION_MAPPING_ID)));
      } 
      else {
        //TODO not sure if this is a good option to set to 0
        returnId.setIsCreatedWithThisCall(false);
        returnId.setId(0L);
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
        if (statementCreateExperimentVersionMapping != null) {
          statementCreateExperimentVersionMapping.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }
    return returnId;
  }

  private Integer getClosestExperimentVersion(Long experimentId, Integer version) throws SQLException {
    Connection conn = null;
    ResultSet rs = null;
    PreparedStatement statementClosestExperimentVersion = null;
   
    Integer closestVersion = null;
    List<Integer> possibleVersions = Lists.newArrayList();
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementClosestExperimentVersion = conn.prepareStatement(QueryConstants.GET_CLOSEST_VERSION.toString());
      statementClosestExperimentVersion.setLong(1, experimentId);
      statementClosestExperimentVersion.setInt(2, version);
      statementClosestExperimentVersion.setLong(3, experimentId);
      statementClosestExperimentVersion.setInt(4, version);
      log.info("get closest version:" + statementClosestExperimentVersion.toString());
      rs = statementClosestExperimentVersion.executeQuery();
      while (rs.next()) {
        possibleVersions.add(rs.getInt(ExperimentGroupVersionMappingColumns.EXPERIMENT_VERSION));
      }
      
      if (possibleVersions.size() == 1) {
        closestVersion = possibleVersions.get(0);
        log.info("only 1 version returned" + closestVersion);
      } else if (possibleVersions.size() == 2) {
        closestVersion = Math.abs(version - possibleVersions.get(0)) < Math.abs(version - possibleVersions.get(1)) ? possibleVersions.get(0) : possibleVersions.get(1);
        log.info("out of 2 version returned:" + closestVersion);
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
    return closestVersion;
  }

  @Override
  public void copyClosestVersion(Long experimentId, Integer experimentVersion) throws SQLException {
    Integer closestVersion = getClosestExperimentVersion(experimentId, experimentVersion);
    Connection conn = null;
    ResultSet rs = null;
    PreparedStatement statementClosestExperimentVersion = null;
    
    List<ExperimentVersionMapping> newEVMRecords = Lists.newArrayList();
    ExperimentVersionMapping evm = null;
    ExperimentDetail expFacet = null;
    GroupDetail group = null;
    InputCollection inputCollection = null;
    try {
      if (closestVersion != null) {
        conn = CloudSQLConnectionManager.getInstance().getConnection();
        statementClosestExperimentVersion = conn.prepareStatement(QueryConstants.GET_ALL_EVM_RECORDS_FOR_VERSION.toString());
        statementClosestExperimentVersion.setLong(1, experimentId);
        statementClosestExperimentVersion.setInt(2, closestVersion);
        
        rs = statementClosestExperimentVersion.executeQuery();
        while (rs.next()) {
          evm = new ExperimentVersionMapping();
          expFacet = new ExperimentDetail();
          group = new GroupDetail();
          inputCollection = new InputCollection();
          expFacet.setExperimentDetailId(new PacoId(rs.getLong(ExperimentGroupVersionMappingColumns.EXPERIMENT_DETAIL_ID), false));
          group.setGroupId(new PacoId(rs.getLong(ExperimentGroupVersionMappingColumns.GROUP_DETAIL_ID), false));
          inputCollection.setInputCollectionId(rs.getLong(ExperimentGroupVersionMappingColumns.INPUT_COLLECTION_ID));
          evm.setExperimentId(experimentId);
          // just change the version alone
          evm.setExperimentVersion(experimentVersion);
          evm.setExperimentInfo(expFacet);
          evm.setGroupInfo(group);
          evm.setInputCollection(inputCollection);
          evm.setSource("Copied From Version:"+ closestVersion);
          newEVMRecords.add(evm);
        }
        createExperimentVersionMapping(newEVMRecords);
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
  public ExperimentVersionMapping prepareEVMForGroupWithInputs(Long experimentId, String experimentName, Integer experimentVersion, String groupName, String whoEmail, Set<What> whatSet, boolean migrationFlag) throws SQLException {
    Map<String, ExperimentVersionMapping> allEVMRecords = null;
    GroupTypeEnum matchingGroupType = null;
    CSEventOutputDao eventOutputDaoImpl = new CSEventOutputDaoImpl();
    CSFailedEventDao failedDaoImpl = new CSFailedEventDaoImpl();
    CSExperimentVersionMappingDao daoImpl = new CSExperimentVersionMappingDaoImpl();
    ExperimentVersionMapping evm = new ExperimentVersionMapping();
    evm.setExperimentId(experimentId);
    evm.setExperimentVersion(experimentVersion);
    
    allEVMRecords = daoImpl.getAllGroupsInVersion(experimentId, experimentVersion);
    matchingGroupType = findMatchingGroupType(whatSet);
    if (allEVMRecords == null || allEVMRecords.size() == 0) {
      daoImpl.copyClosestVersion(experimentId, experimentVersion);
      allEVMRecords = daoImpl.getAllGroupsInVersion(experimentId, experimentVersion);
      if (allEVMRecords == null) {
        // It is deleted experiment, so delete all related events and outputs
        failedDaoImpl.insertFailedEvent("expId: " + experimentId + "expVersion: "+ experimentVersion + ",who:"+ whoEmail , "Did not find any closestVersion. ", "Did not find any closestVersion.");
        log.info("expId: " + experimentId + "expVersion: "+ experimentVersion + ",who:"+ whoEmail + "deleted");
        eventOutputDaoImpl.deleteAllEventsAndOutputsData(experimentId, null);
        return null;
      } else {
        log.info("older version of this experiment exists");
      }
    }
    // check for groups if we have some older version
    ExperimentVersionMapping matchingEVMInDB = allEVMRecords.get(groupName);

    if (matchingEVMInDB == null && migrationFlag) {
      // no matching group name in DB. For migrated experiments, we can recreate the grps.
      // get some exp facet object to use
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
        addInputCollectionToDBByVarName(evm, whatList);
      } else {
        CSGroupTypeInputMappingDao gtimDao = new CSGroupTypeInputMappingDaoImpl();
        List<Input> inputLst = gtimDao.getAllFeatureInputs().get(matchingGroupType.name());
        addInputCollectionToDBByInput(evm, inputLst);
      }
      //  insert to db, then return right away
      createExperimentVersionMapping(Lists.newArrayList(evm));
      return evm;
    } else if (matchingEVMInDB != null) {
      evm.setGroupInfo(matchingEVMInDB.getGroupInfo());
      evm.setInputCollection(matchingEVMInDB.getInputCollection());
      // check for inputs
      Map<String, InputOrderAndChoice> iocsInDB = matchingEVMInDB.getInputCollection().getInputOrderAndChoices();
      if (iocsInDB == null) {
        iocsInDB = Maps.newHashMap();
      }
      for (What eachWhat : whatSet) {
        // input alone missing
        if (iocsInDB.get(eachWhat.getName()) == null) {
          // check and create new ic id
          addInputCollectionToDBByVarName(evm, Lists.newArrayList(eachWhat.getName()));
        }
      }
    } else {
      // if we do not hv matching group name in a newer version of experiment with group types
      failedDaoImpl.insertFailedEvent("expId: " + experimentId + "expVersion: "+ experimentVersion + ",who:"+ whoEmail + " grp name:" + groupName , "Event save, does not have matching group ", "Event save does not have matching group");
      return null;
    }
    return matchingEVMInDB;
  }
  
  private void  addGroupInfoToDB(ExperimentVersionMapping evm, String groupName, GroupTypeEnum groupType) throws SQLException {
    CSGroupDao grpDaoImp = new CSGroupDaoImpl();
    GroupDetail grp = new GroupDetail();
    grp.setName(groupName);
    grp.setGroupTypeId(groupType.getGroupTypeId());
    grp.setFixedDuration(false);
    grp.setRawDataAccess(false);
    if (evm.getSource() == null) {
      evm.setSource("Recreating group Info");
    }
    grpDaoImp.insertGroup(Lists.newArrayList(grp));
    evm.setGroupInfo(grp);
  }
  
  private void  addInputCollectionToDBByVarName(ExperimentVersionMapping evm, List<String> inputsToBeAdded) throws SQLException {
    InputCollection inputCollection = evm.getInputCollection(); 
    CSInputDao inputDaoImpl = new CSInputDaoImpl();
    List<Input> inputs = inputDaoImpl.insertVariableNames(inputsToBeAdded);
    if (inputCollection == null) {
      inputCollection = new InputCollection();
      Integer noOfGroups = getNumberOfGroups(evm.getExperimentId(), evm.getExperimentVersion());
      inputCollection.setInputCollectionId(IdGenerator.generate(BigInteger.valueOf(evm.getExperimentVersion()), noOfGroups+1).longValue());
      evm.setInputCollection(inputCollection);
    }
    for (Input eachInput : inputs) {
      inputCollectionDaoImpl.addUndefinedInputToCollection(evm.getExperimentId(), inputCollection.getInputCollectionId(), eachInput.getInputId().getId());
    }
    Map<String, InputOrderAndChoice> varNameIoc = evm.getInputCollection().getInputOrderAndChoices();
    if (varNameIoc != null) {
      for (Input eachInput : inputs) {
        varNameIoc.put(eachInput.getName().getLabel(), convertInputToInputOrderAndChoice(eachInput));
      }
    } else {
      inputCollection.setInputOrderAndChoices(convertInputListToInputOrderAndChoice(inputs));
    }
    evm.setInputCollection(inputCollection);
  }
  
  private void addInputCollectionToDBByInput(ExperimentVersionMapping evm, List<Input> inputs) throws SQLException {
    InputCollection inputCollection = evm.getInputCollection(); 
    if (inputCollection == null) {
      inputCollection = new InputCollection();
      Integer noOfGroups = getNumberOfGroups(evm.getExperimentId(), evm.getExperimentVersion());
      inputCollection.setInputCollectionId(IdGenerator.generate(BigInteger.valueOf(evm.getExperimentVersion()), noOfGroups+1).longValue());
    }
    for (Input eachInput : inputs) {
      inputCollectionDaoImpl.addUndefinedInputToCollection(evm.getExperimentId(), inputCollection.getInputCollectionId(), eachInput.getInputId().getId());
    }
    inputCollection.setInputOrderAndChoices(convertInputListToInputOrderAndChoice(inputs));
    evm.setInputCollection(inputCollection);
  }
  
  
  private GroupTypeEnum findMatchingGroupType(Set<What> inputWhats) throws SQLException {
    GroupTypeEnum matchingGroupType = null;
    CSGroupTypeInputMappingDao daoImpl = new CSGroupTypeInputMappingDaoImpl();
    Map<String, List<Input>> predefinedInputs = daoImpl.getAllFeatureInputs();
    for (What eachWhat : inputWhats ) {
      matchingGroupType = null;
      Iterator<String> predefinedFeatureNameItr = predefinedInputs.keySet().iterator();
      while (predefinedFeatureNameItr.hasNext()) {
        String crFeatureName = predefinedFeatureNameItr.next();
        List<Input> crPredefinedInputList = predefinedInputs.get(crFeatureName);
        if (isVariableNamePresentInInputs(crPredefinedInputList, eachWhat.getName())) {
          if (crFeatureName.equalsIgnoreCase(GroupTypeEnum.SYSTEM.name())) {
            matchingGroupType = GroupTypeEnum.valueOf(crFeatureName.toUpperCase());
            break;
          } else if ( crFeatureName.equalsIgnoreCase(GroupTypeEnum.NOTIFICATION.name()) || (crFeatureName.equalsIgnoreCase(GroupTypeEnum.ACCESSIBILITY.name()))){
            if (eachWhat.getName().equalsIgnoreCase("accessibilityEventType")) {
              if (eachWhat.getValue().equalsIgnoreCase("15")) {
                matchingGroupType = GroupTypeEnum.ACCESSIBILITY;
                break;
              } else {
                matchingGroupType = GroupTypeEnum.NOTIFICATION;
                break;
              }
            }
          } else {
            matchingGroupType = GroupTypeEnum.valueOf(crFeatureName);
            break;
          }
        }
      }
    }
    if (matchingGroupType == null) {
      matchingGroupType = GroupTypeEnum.SURVEY;
    }
    return matchingGroupType;
  }
  
  private boolean isVariableNamePresentInInputs (List<Input> inputLst, String inputVariableName) {
    for (Input i : inputLst) { 
      if (i.getName().getLabel().equalsIgnoreCase(inputVariableName)) {
        return true;
      }
    }
    return false;
    
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
    int ct = 1;
    PreparedStatement statementSelectExperimentVersionMapping = null;
   
    final String updateValueForExperimentVersionMappingId = "select "+ ExperimentGroupVersionMappingColumns.EXPERIMENT_GROUP_VERSION_MAPPING_ID +
            " from " + ExperimentGroupVersionMappingColumns.TABLE_NAME + " evm "+
            " where " + ExperimentGroupVersionMappingColumns.EXPERIMENT_ID + " = ? and  " + ExperimentGroupVersionMappingColumns.EXPERIMENT_VERSION + " = ? " ;
  
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
    String updateQuery = "update experiment_group_version_mapping set events_posted =b'1' where experiment_group_version_mapping_id= ? ";
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
  
  
}
