package com.google.sampling.experiential.dao.impl;

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
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.google.cloud.sql.jdbc.Statement;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.cloudsql.columns.ChoiceCollectionColumns;
import com.google.sampling.experiential.cloudsql.columns.DataTypeColumns;
import com.google.sampling.experiential.cloudsql.columns.ExperimentColumns;
import com.google.sampling.experiential.cloudsql.columns.ExperimentVersionMappingColumns;
import com.google.sampling.experiential.cloudsql.columns.ExternStringInputColumns;
import com.google.sampling.experiential.cloudsql.columns.ExternStringListLabelColumns;
import com.google.sampling.experiential.cloudsql.columns.GroupColumns;
import com.google.sampling.experiential.cloudsql.columns.InformedConsentColumns;
import com.google.sampling.experiential.cloudsql.columns.InputCollectionColumns;
import com.google.sampling.experiential.cloudsql.columns.InputColumns;
import com.google.sampling.experiential.cloudsql.columns.UserColumns;
import com.google.sampling.experiential.dao.CSExperimentDao;
import com.google.sampling.experiential.dao.CSExperimentVersionMappingDao;
import com.google.sampling.experiential.dao.CSGroupDao;
import com.google.sampling.experiential.dao.CSInformedConsentDao;
import com.google.sampling.experiential.dao.CSInputCollectionDao;
import com.google.sampling.experiential.dao.CSInputDao;
import com.google.sampling.experiential.dao.dataaccess.Choice;
import com.google.sampling.experiential.dao.dataaccess.ChoiceCollection;
import com.google.sampling.experiential.dao.dataaccess.DataType;
import com.google.sampling.experiential.dao.dataaccess.Experiment;
import com.google.sampling.experiential.dao.dataaccess.ExperimentVersionMapping;
import com.google.sampling.experiential.dao.dataaccess.ExternStringInput;
import com.google.sampling.experiential.dao.dataaccess.ExternStringListLabel;
import com.google.sampling.experiential.dao.dataaccess.Group;
import com.google.sampling.experiential.dao.dataaccess.InformedConsent;
import com.google.sampling.experiential.dao.dataaccess.Input;
import com.google.sampling.experiential.dao.dataaccess.InputCollection;
import com.google.sampling.experiential.dao.dataaccess.InputOrderAndChoice;
import com.google.sampling.experiential.dao.dataaccess.User;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.google.sampling.experiential.server.ExceptionUtil;
import com.google.sampling.experiential.server.ExperimentDAOConverter;
import com.google.sampling.experiential.server.PacoId;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
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
  
  CSExperimentDao experimentDaoImpl = new CSExperimentDaoImpl();
  CSGroupDao groupDaoImpl = new CSGroupDaoImpl();
  CSInputDao inputDaoImpl = new CSInputDaoImpl();
  CSInputCollectionDao inputCollectionDaoImpl = new CSInputCollectionDaoImpl();
  
  static {
    experimentVersionMappingColList.add(new Column(ExperimentVersionMappingColumns.EXPERIMENT_ID));
    experimentVersionMappingColList.add(new Column(ExperimentVersionMappingColumns.EXPERIMENT_VERSION));
    experimentVersionMappingColList.add(new Column(ExperimentVersionMappingColumns.EXPERIMENT_FACET_ID));
    experimentVersionMappingColList.add(new Column(ExperimentVersionMappingColumns.GROUP_ID));
    experimentVersionMappingColList.add(new Column(ExperimentVersionMappingColumns.INPUT_COLLECTION_ID));
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
      log.info("Inserting into mapping  table");
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      conn.setAutoCommit(false);
      experimentVersionMappingInsert.setTable(new Table(ExperimentVersionMappingColumns.TABLE_NAME));
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
        statementCreateExperimentVersionMapping.setLong(3, evm.getExperimentInfo().getExperimentFacetId().getId());
        statementCreateExperimentVersionMapping.setLong(4, evm.getGroupInfo().getGroupId().getId());
        statementCreateExperimentVersionMapping.setObject(5, evm.getInputCollection() != null ? evm.getInputCollection().getInputCollectionId() : null, Types.BIGINT);
        log.info(statementCreateExperimentVersionMapping.toString());
        statementCreateExperimentVersionMapping.addBatch();
      }
      statementCreateExperimentVersionMapping.executeBatch();
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
  
  private Experiment updateNewExperimentWithOldId(Experiment oldVersion, Experiment newVersion) {
    try {
      if (oldVersion != null && newVersion.compareWithoutId(oldVersion)) {
        // experiment property fields matched with older version properties
        newVersion.setExperimentFacetId(oldVersion.getExperimentFacetId());
        log.info("updated new experiment id, with old " + newVersion.getExperimentFacetId());
      } 
    } catch (IllegalArgumentException | IllegalAccessException e) {
      log.warning("Compare Experiment fields:"+ ErrorMessages.INVALID_ACCESS_OR_ARGUMENT_EXCEPTION.getDescription() + ExceptionUtil.getStackTraceAsString(e));    
    }  
    return newVersion;
  }

  private void updateNewInformedConsentWithOldId(InformedConsent oldInformedConsent, InformedConsent newInformedConsent) {
    if (newInformedConsent.isInformedConsentStringMatched(oldInformedConsent)) {
      newInformedConsent.setInformedConsentId(oldInformedConsent.getInformedConsentId());
      log.info("updated new informed consent id, with old " + newInformedConsent);
    }
  }
  
  private void updateNewGroupsWithOldId(Map<String, Group> oldGroupMap, List<Group> newGroupList) {
    Group matchingGroupInPreviousVersion = null;
    for (Group currentNewGroup : newGroupList) {
      if (oldGroupMap != null) { // this will not be needed at all, having it to handle erroneous data in db
        matchingGroupInPreviousVersion = oldGroupMap.get(currentNewGroup.getName());
        if (matchingGroupInPreviousVersion != null) {
          try {
            if (matchingGroupInPreviousVersion.compareWithoutId(currentNewGroup)) {
              currentNewGroup.setGroupId(matchingGroupInPreviousVersion.getGroupId());
            } 
          } catch (IllegalArgumentException | IllegalAccessException e) {
            log.warning("Compare Group fields:"+ ErrorMessages.INVALID_ACCESS_OR_ARGUMENT_EXCEPTION.getDescription() + ExceptionUtil.getStackTraceAsString(e));
          }
        }
      }
    }
  }
  
  private void updateNewInputCollectionWithOldId(Map<String, InputCollection> oldVersion, Map<String, InputCollection> newVersion) {
    Iterator<String> newGrpNameItr = newVersion.keySet().iterator();
    InputCollection currentNewInputCollection = null;
    InputOrderAndChoice currentNewInputOrderAndChoice = null;
    InputOrderAndChoice matchingOldInputOrderAndChoice = null;
    ChoiceCollection currentNewChoiceCollection = null;
    ChoiceCollection matchingOldChoiceCollection = null;
    String currentNewGroupName = null;
    String currentInputVariableName = null;
    InputCollection matchingOldInputCollection = null;
    Boolean inputResponseDataTypeChanged = null;
    Boolean inputOrderChanged = null;
    Boolean inputPropsChanged = null;
    
    // for every new grp
    while (newGrpNameItr.hasNext()) {
      log.info("iterating group names");
      currentNewGroupName = newGrpNameItr.next();
      inputResponseDataTypeChanged = false;
      inputOrderChanged = false;
      inputPropsChanged = false;
      boolean choiceTextOrOrderOrSizeChanged = false;
      
      currentNewInputCollection = newVersion.get(currentNewGroupName);
      if ( currentNewInputCollection == null || (currentNewInputCollection.getInputOrderAndChoices() != null && currentNewInputCollection.getInputOrderAndChoices().size() == 0)) {
        log.info("grp has no inputs yet");
        newVersion.put(currentNewGroupName, null);
      } else {
        matchingOldInputCollection = oldVersion.get(currentNewGroupName) ;
        if ( matchingOldInputCollection != null ) {
          log.info("input collection matched");
          // iterate within list of inputs in new and compare with old
          Iterator<String> newInputVariableNameIOCItr = currentNewInputCollection.getInputOrderAndChoices().keySet().iterator();
          // for every new input 
          while (newInputVariableNameIOCItr.hasNext()) {
            // iterating every input in this grp
            currentInputVariableName = newInputVariableNameIOCItr.next();
            currentNewInputOrderAndChoice = currentNewInputCollection.getInputOrderAndChoices().get(currentInputVariableName);
            matchingOldInputOrderAndChoice = matchingOldInputCollection.getInputOrderAndChoices().get(currentInputVariableName);
            if (matchingOldInputOrderAndChoice != null) {
              log.info("This input var name is present in old version" + currentInputVariableName);
              try {
                // check input, instead of hitting db and checking, we can check the params of the datatype (multi select, numeric, name)
                if (matchingOldInputOrderAndChoice.getInput().compareWithoutId(currentNewInputOrderAndChoice.getInput())) {
                  if (currentNewInputOrderAndChoice.getInput().getResponseDataType().getName().equals(matchingOldInputOrderAndChoice.getInput().getResponseDataType().getName()) 
                          && currentNewInputOrderAndChoice.getInput().getResponseDataType().isMultiSelect() == (matchingOldInputOrderAndChoice.getInput().getResponseDataType().isMultiSelect())
                          && currentNewInputOrderAndChoice.getInput().getResponseDataType().isNumeric() == (matchingOldInputOrderAndChoice.getInput().getResponseDataType().isNumeric())) {
                    log.info("comparing inputs and response data types same");
                    currentNewInputOrderAndChoice.getInput().setInputId(matchingOldInputOrderAndChoice.getInput().getInputId());
                    // check input order
                    if (matchingOldInputOrderAndChoice.getInputOrder().intValue() == currentNewInputOrderAndChoice.getInputOrder().intValue()) {
                      log.info("comparing input order - same");
                      // check input choices
                      currentNewChoiceCollection = currentNewInputOrderAndChoice.getChoiceCollection();
                      if (currentNewChoiceCollection != null) {
                        log.info("current input has choice collection");
                        matchingOldChoiceCollection = matchingOldInputOrderAndChoice.getChoiceCollection();
                        if ( matchingOldChoiceCollection != null ) { // if old was a choice too
                          choiceTextOrOrderOrSizeChanged = hasChoiceOrderOrTextOrSizeChanged(currentNewChoiceCollection, matchingOldChoiceCollection);
                        } else {
                          // old was choice, but now, in new it is not
                          inputResponseDataTypeChanged = true;
                          currentNewInputOrderAndChoice.getInput().setInputId(null);
                        }
                      }
                    } else {
                      // input order has changed
                      inputOrderChanged = true;
                    }
                  } else {
                    // data type properties changed
                    inputResponseDataTypeChanged = true;
                    currentNewInputOrderAndChoice.getInput().setInputId(null);
                  }
                } else {
                  // input props like right label, left label, likert steps, conditional, multi select etc
                  inputPropsChanged = true;
                }
              } catch (IllegalArgumentException | IllegalAccessException e) {
                log.warning("Input Order And choice comparison failed");
              }
            } else {
              // totally new variable in this grp
              // we need to insert input, (choice and collection, if list), and then input collection
              inputPropsChanged = true;
            }
          }// while input itr
        } else {
          // grp is not present in old version, so there were no inputs on the earlier version
          // all inputs are new
          // no need to update ids
          inputPropsChanged = true;
        }
      }
      if (!inputPropsChanged && !inputResponseDataTypeChanged && !inputOrderChanged && !choiceTextOrOrderOrSizeChanged) {
        // copy ic id from old to new
        InputCollection temp = newVersion.get(currentNewGroupName);
        if (oldVersion.get(currentNewGroupName) != null ) {
          temp.setInputCollectionId(oldVersion.get(currentNewGroupName).getInputCollectionId());
        }
        newVersion.put(currentNewGroupName, temp);
      } else {
        // set ic id to null 
        InputCollection temp = newVersion.get(currentNewGroupName);
        if ( temp != null) {
          temp.setInputCollectionId(null);
        }
        newVersion.put(currentNewGroupName, temp);
      }   
    } // while grp itr
  }
  
  private boolean hasChoiceOrderOrTextOrSizeChanged(ChoiceCollection currentNewChoiceCollection, ChoiceCollection matchingOldChoiceCollection) {
    boolean choiceTextChanged = false;
    boolean choiceOrderChanged = false;
    boolean choiceSizeChanged = false;
    Map<String, Choice>  currentNewChoices = currentNewChoiceCollection.getChoices();
    Map<String, Choice> matchingOldChoices = matchingOldChoiceCollection.getChoices();
    // chk ind choices labels and choice order
    // if everything is same copy choice collectionid and input collectionid
    Choice matchingOldChoice = null;
    // for every choice in the new list, compare text and then order
    if (currentNewChoices != null ) {
      Iterator<String> newChoicesItr = currentNewChoices.keySet().iterator();
      String currentNewChoice = null;
      while (newChoicesItr.hasNext()) {
        currentNewChoice = newChoicesItr.next();
        if (matchingOldChoices != null) { 
          matchingOldChoice = matchingOldChoices.get(currentNewChoice.toLowerCase());
          if (matchingOldChoice != null) {
            currentNewChoices.get(currentNewChoice).setChoiceLabel(matchingOldChoice.getChoiceLabel());
            if (!(matchingOldChoice.getChoiceOrder() == currentNewChoices.get(currentNewChoice).getChoiceOrder())) {
              choiceOrderChanged = true;
            }
          } else {
            choiceTextChanged = true;
          }
        } else {
          choiceTextChanged = true;
        }
        
      } // end while for input iterator
      // all choices for this input iterated
      // when old version has choices c1, c2, c3 and new version has c1, c2. The order has not changed and labels have not changed.
      // but we still need to change the choice collection. we detect this scenario by comparing the sizes
      if (!(currentNewChoices.size() == matchingOldChoices.size())) {
        currentNewChoiceCollection.setChoiceCollectionId(matchingOldChoiceCollection.getChoiceCollectionId());
        choiceSizeChanged = true;
      }
    }
    return choiceTextChanged || choiceOrderChanged || choiceSizeChanged;
  }
 
  @Override
  public void updateExperimentVersionMapping(ExperimentDAO experimentDao) throws SQLException {
    List<ExperimentVersionMapping> allGroupsInNewVersion = Lists.newArrayList();
    ExperimentDAOConverter converter = new ExperimentDAOConverter();
    InformedConsent newInformedConsent = null;
    InformedConsent oldInformedConsent = null;
    Experiment oldExperimentInfo = null;
    Experiment newExperimentInfo = null;
    List<Group> newGroupList = Lists.newArrayList();
    Map<String, Group> oldGroupMap = Maps.newHashMap();
    Map<String, InputCollection> oldGrpNameInputCollection = Maps.newHashMap();
    Map<String, InputCollection> newGrpNameInputCollection = Maps.newHashMap();
    CSInformedConsentDao informedConsentDao = new CSInformedConsentDaoImpl();
    
    Map<String, ExperimentVersionMapping> allGroupsInPrevVersion = getAllGroupsPreviousVersion(experimentDao.getId(), experimentDao.getVersion()-1);
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
    // itearting new version
    for (ExperimentVersionMapping evm : allGroupsInNewVersion) {
      newExperimentInfo = evm.getExperimentInfo();
      newGroupList.add(evm.getGroupInfo());
      newGrpNameInputCollection.put(evm.getGroupInfo().getName(), evm.getInputCollection());
    }

    log.info("processing informed consent begin");
    // Item 1 - InformedConsent
    boolean hasInformedConsentChanged = false;
    newInformedConsent = newExperimentInfo.getInformedConsent();
    if (oldExperimentInfo != null) {
      oldInformedConsent = oldExperimentInfo.getInformedConsent();
    }
    
    if (newInformedConsent != null) {
      if (oldInformedConsent != null) {
        updateNewInformedConsentWithOldId(oldInformedConsent, newInformedConsent);
      }
    } else if (oldInformedConsent != null) {
      // currently no informed consent, but earlier it had
      hasInformedConsentChanged = true;
    }
    
    if (newInformedConsent != null && newInformedConsent.getInformedConsentId() == null) {
      newInformedConsent.setExperimentId(experimentDao.getId());
      informedConsentDao.insertInformedConsent(newInformedConsent, experimentDao.getVersion());
      hasInformedConsentChanged = true;
    }
    log.info("processing informed consent end, experiment begin");
    
    // Item 2 - Experiment
    if (!hasInformedConsentChanged) {
      // if informed consent has changes, we anyway have to persist this change with new experiment facet id.
      // So, we don't need to check if other experiment properties like title description etc changed or not
      updateNewExperimentWithOldId(oldExperimentInfo, newExperimentInfo);
    }
    // if no facet id, there is some change in expt info, so we need to insert into experiment table
    if (oldExperimentInfo == null || newExperimentInfo.getExperimentFacetId() == null) {
      log.info("experiment changed, so experiment facet insert ");
      newExperimentInfo.setInformedConsent(newInformedConsent);
      experimentDaoImpl.insertExperiment(newExperimentInfo);
    } 
    log.info("processing expt info end, group begin");
    
    // Item 3 - Groups
    updateNewGroupsWithOldId(oldGroupMap, newGroupList);
    groupDaoImpl.insertGroup(newGroupList);
    
    log.info("processing group end, input begin");
    
    // Item 4 - Inputs
    updateNewInputCollectionWithOldId(oldGrpNameInputCollection, newGrpNameInputCollection);
    inputCollectionDaoImpl.createInputCollectionId(experimentDao, newGrpNameInputCollection, oldGrpNameInputCollection);
    log.info("processing input collection end, experiment version mapping begin");
    
    int grpCt = 0;
    for (ExperimentVersionMapping evm : allGroupsInNewVersion) {
      evm.setExperimentId(experimentDao.getId());
      evm.setExperimentVersion(experimentDao.getVersion());
      evm.setExperimentInfo(newExperimentInfo);
      evm.setGroupInfo(newGroupList.get(grpCt++));
      evm.setInputCollection(newGrpNameInputCollection.get(evm.getGroupInfo().getName()));
    }
    
    createExperimentVersionMapping(allGroupsInNewVersion);
    log.info("experiment version mapping ends");
  }

  @Override
  public Map<String, ExperimentVersionMapping> getAllGroupsPreviousVersion(Long experimentId, Integer version) throws SQLException {
    log.info("prev version called" + experimentId + "-->" + version);
    Long experimentIdFromResultSet = null;
    Connection conn = null;
    PreparedStatement statementSelectExperiment = null;
    Map<String, ExperimentVersionMapping> groupNameEVMMap = null;
    ExperimentVersionMapping returnEVMH = null;
    Experiment eHistory = null;
    Group gHistory = null;
    Input iHistory = null;
    InputCollection icHistory = null;
    ChoiceCollection ccHistory = null;
    Choice choice = null;
    Map<String, Choice> choices = null;
    ExternStringListLabel externStringListLabel = null;
    ExternStringInput externName = null;
    ExternStringInput externText = null;
    String currentGroupName = null;
    Map<String, InputOrderAndChoice> returnEVMHInputOrderAndChoices = null;
    InputOrderAndChoice inputOrderAndChoiceObj = null;
    DataType dataType = null;
    InformedConsent informedConsent = null;
    ResultSet rs = null;
    Long recordCt = 0L;
    
    User creator = new User();

    String query = "select * from experiment_version_mapping evmh join experiment eh on evmh.experiment_facet_id = eh.experiment_facet_id " + 
            " join `group` gh on evmh.group_id = gh.group_id " +
            " left join input_collection ich on ich.input_collection_id = evmh.input_collection_id and ich.experiment_id = evmh.experiment_id " +
            " left join input ih on ich.input_id = ih.input_id " +
            " join user u on eh.creator = u.user_id " +
            " left join choice_collection cch on ich.choice_collection_id = cch.choice_collection_id and ich.experiment_id = cch.experiment_id " +
            " left join informed_consent ic on eh.informed_consent_id = ic.informed_consent_id and evmh.experiment_id = ic.experiment_id " +
            " left join extern_string_list_label esll on cch.choice_id = esll.extern_string_list_label_id " + 
            " left join extern_string_input esi1 on ih.text_id = esi1.extern_string_input_id " + 
            " left join extern_string_input esi2 on ih.name_id = esi2.extern_string_input_id " +
            " left join data_type dt on ih.data_type_id = dt.data_type_id " +
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
          log.info("recordset ctr"+ recordCt++);
          if (groupNameEVMMap == null) {
            groupNameEVMMap = Maps.newHashMap();
          }
          eHistory = new Experiment();
          gHistory = new Group();
          iHistory = new Input();
          dataType = new DataType();
          externStringListLabel = new ExternStringListLabel();
          externText = new ExternStringInput();
          externName = new ExternStringInput();
          inputOrderAndChoiceObj = new InputOrderAndChoice();
          
          experimentIdFromResultSet = rs.getLong(ExperimentVersionMappingColumns.EXPERIMENT_ID);
          currentGroupName = rs.getString(GroupColumns.NAME);
          
          String informedConsentString = rs.getString(InformedConsentColumns.INFORMED_CONSENT);
          
          if (informedConsentString != null) {
            informedConsent = new InformedConsent();
            informedConsent.setExperimentId(experimentIdFromResultSet);
            informedConsent.setInformedConsent(informedConsentString);
            informedConsent.setInformedConsentId(new PacoId (rs.getLong(InformedConsentColumns.INFORMED_CONSENT_ID), false));
          }
          
          creator.setUserId(new PacoId(rs.getLong(UserColumns.USER_ID), false));
          creator.setWho(rs.getString(UserColumns.WHO));
          
          eHistory.setExperimentFacetId(new PacoId(rs.getLong(ExperimentColumns.EXPERIMENT_FACET_ID), false));
          eHistory.setTitle(rs.getString(ExperimentColumns.TITLE));
          eHistory.setDescription(rs.getString(ExperimentColumns.DESCRIPTION));
          eHistory.setCreator(creator);
          eHistory.setOrganization(rs.getString(ExperimentColumns.ORGANIZATION));
          eHistory.setContactEmail(rs.getString(ExperimentColumns.CONTACT_EMAIL));
          eHistory.setInformedConsent(informedConsent);
          eHistory.setDeleted(rs.getBoolean(ExperimentColumns.DELETED));
          Timestamp dt = rs.getTimestamp(ExperimentColumns.MODIFIED_DATE);
          eHistory.setModifiedDate(dt!=null ? new DateTime(dt.getTime()): new DateTime());
          eHistory.setPublished(rs.getBoolean(ExperimentColumns.PUBLISHED));
          eHistory.setRingtoneUri(rs.getString(ExperimentColumns.RINGTONE_URI));
          eHistory.setPostInstallInstructions(rs.getString(ExperimentColumns.POST_INSTALL_INSTRUCTIONS));
          
          gHistory.setName(currentGroupName);
          gHistory.setGroupId(new PacoId(rs.getLong(GroupColumns.GROUP_ID), false));
          gHistory.setCustomRendering(rs.getString(GroupColumns.CUSTOM_RENDERING));
          gHistory.setFixedDuration(rs.getBoolean(GroupColumns.FIXED_DURATION));
          Timestamp startDate = rs.getTimestamp(GroupColumns.START_DATE);
          Timestamp endDate = rs.getTimestamp(GroupColumns.END_DATE);
          gHistory.setStartDate(startDate != null ? new DateTime(startDate.getTime()) : null);
          gHistory.setEndDate(endDate != null ? new DateTime(endDate.getTime()) : null);
          gHistory.setRawDataAccess(rs.getBoolean(GroupColumns.RAW_DATA_ACCESS));
          gHistory.setEndOfDayGroup(rs.getString(GroupColumns.END_OF_DAY_GROUP));
        
          dataType.setDataTypeId(new PacoId(rs.getInt(DataTypeColumns.DATA_TYPE_ID), false));
          dataType.setName(rs.getString(DataTypeColumns.NAME));
          dataType.setNumeric(rs.getBoolean(DataTypeColumns.IS_NUMERIC));
          dataType.setMultiSelect(rs.getBoolean(DataTypeColumns.MULTI_SELECT));
          dataType.setResponseMappingRequired(rs.getBoolean(DataTypeColumns.RESPONSE_MAPPING_REQUIRED));
          
          externName.setExternStringInputId(new PacoId(rs.getLong("esi2." + ExternStringInputColumns.EXTERN_STRING_ID), false));
          externName.setLabel(rs.getString("esi2." + ExternStringInputColumns.LABEL));
          externText.setExternStringInputId(new PacoId(rs.getLong("esi1." + ExternStringInputColumns.EXTERN_STRING_ID), false));
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
          iHistory.setChannel(rs.getString(InputColumns.CHANNEL));
          
          externStringListLabel.setExternStringListLabelId(new PacoId(rs.getLong(ExternStringListLabelColumns.EXTERN_STRING_LIST_LABEL_ID), false));
          externStringListLabel.setLabel(rs.getString(ExternStringListLabelColumns.LABEL));

          inputOrderAndChoiceObj.setInput(iHistory);
          inputOrderAndChoiceObj.setInputOrder(rs.getInt(InputCollectionColumns.INPUT_ORDER));
         
          if (groupNameEVMMap.get(currentGroupName) == null) {
            // map does not contain group name
            returnEVMH = new ExperimentVersionMapping();
            
            Long tempColId = rs.getLong(InputCollectionColumns.INPUT_COLLECTION_ID);
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
            returnEVMH.setExperimentVersion(rs.getInt(ExperimentVersionMappingColumns.EXPERIMENT_VERSION));
            returnEVMH.setExperimentInfo(eHistory);
            returnEVMH.setGroupInfo(gHistory);
            groupNameEVMMap.put(currentGroupName, returnEVMH);
          } else {
            // map  contains group name
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
      
      log.info("query took " + (System.currentTimeMillis() - st1Time));
      log.info("returned:  " +recordCt);
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
}
