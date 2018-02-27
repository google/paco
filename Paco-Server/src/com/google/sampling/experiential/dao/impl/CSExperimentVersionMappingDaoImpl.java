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
import com.google.sampling.experiential.dao.CSExperimentUserDao;
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
import com.google.sampling.experiential.model.What;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.google.sampling.experiential.server.ExceptionUtil;
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
    experimentVersionMappingColList.add(new Column(ExperimentVersionMappingColumns.SOURCE));
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
        log.info("inputs inserted");
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
    Boolean inputSizeChanged = false;
    
    // for every new grp
    while (newGrpNameItr.hasNext()) {
      currentNewGroupName = newGrpNameItr.next();
      log.info("iterating group names" + currentNewGroupName);
      inputResponseDataTypeChanged = false;
      inputOrderChanged = false;
      inputPropsChanged = false;
      inputSizeChanged = false;
      boolean choiceTextOrOrderOrSizeChangedForAnyInput = false;
      boolean choiceTextOrOrderOrSizeChangedForCurrentInput = false;
      
      currentNewInputCollection = newVersion.get(currentNewGroupName);
      
      if ( currentNewInputCollection == null || (currentNewInputCollection.getInputOrderAndChoices() != null && currentNewInputCollection.getInputOrderAndChoices().size() == 0)) {
        log.info("grp has no inputs yet");
        newVersion.put(currentNewGroupName, null);
      } else {
        matchingOldInputCollection = oldVersion.get(currentNewGroupName) ;
        if ( matchingOldInputCollection != null ) {
          log.info("group name matched");
          if (currentNewInputCollection.getInputOrderAndChoices().size() != matchingOldInputCollection.getInputOrderAndChoices().size()) {
            inputSizeChanged = true;
          }
          // iterate within list of inputs in new and compare with old
          Iterator<String> newInputVariableNameIOCItr = currentNewInputCollection.getInputOrderAndChoices().keySet().iterator();
          // for every new input 
          while (newInputVariableNameIOCItr.hasNext()) {
            choiceTextOrOrderOrSizeChangedForCurrentInput = false;
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
                    currentNewInputOrderAndChoice.getInput().setInputId(matchingOldInputOrderAndChoice.getInput().getInputId());
                    // check input order
                    if (matchingOldInputOrderAndChoice.getInputOrder().intValue() == currentNewInputOrderAndChoice.getInputOrder().intValue()) {
                      log.info("comparing input order - same");
                      // check input choices
                      currentNewChoiceCollection = currentNewInputOrderAndChoice.getChoiceCollection();
                      matchingOldChoiceCollection = matchingOldInputOrderAndChoice.getChoiceCollection();
                      // new is choice
                      if (currentNewChoiceCollection != null) {
                        if ( matchingOldChoiceCollection != null ) { // if old was a choice too
                          if (hasChoiceOrderOrTextOrSizeChanged(currentNewChoiceCollection, matchingOldChoiceCollection)) {
                            log.info("something has changed in the choices");
                            choiceTextOrOrderOrSizeChangedForCurrentInput = true;
                            choiceTextOrOrderOrSizeChangedForAnyInput = true;
                          } 
                        } else { // if old was not choice
                          log.info("old version does not have a choice collection");
                          // old was choice, but now, in new it is a choice
                          inputResponseDataTypeChanged = true;
                          choiceTextOrOrderOrSizeChangedForCurrentInput = true;
                          currentNewInputOrderAndChoice.getInput().setInputId(null);
                        }
                      } else { // new is not choice
                        if (matchingOldChoiceCollection != null) { // old was choice
                          inputResponseDataTypeChanged = true;
                          choiceTextOrOrderOrSizeChangedForCurrentInput = true;
                          currentNewInputOrderAndChoice.getInput().setInputId(null);
                        } // else part not needed (since, old was not choice too)
                      }
                    } else {
                      // input order has changed
                      log.info("input order changed");
                      inputOrderChanged = true;
                    }
                  } else {
                    // data type properties changed
                    log.info("data type prop changed");
                    inputResponseDataTypeChanged = true;
                    currentNewInputOrderAndChoice.getInput().setInputId(null);
                  }
                } else {
                  // input props like right label, left label, likert steps, conditional, multi select etc
                  log.info("input prop changed0");
                  inputPropsChanged = true;
                }
              } catch (IllegalArgumentException | IllegalAccessException e) {
                log.warning("Input Order And choice comparison failed");
              }
            } else {
              // totally new variable in this grp
              // we need to insert input, (choice and collection, if list), and then input collection
              log.info("input prop changed1");
              inputPropsChanged = true;
            }
            if (choiceTextOrOrderOrSizeChangedForCurrentInput && currentNewInputOrderAndChoice.getChoiceCollection() != null) {
              currentNewInputOrderAndChoice.getChoiceCollection().setChoiceCollectionId(null);
            }
          }// while input itr
          
        } else {
          // grp is not present in old version, so there were no inputs on the earlier version
          // all inputs are new
          // no need to update ids
          log.info("input prop changed2");
          inputPropsChanged = true;
        }
      }
      if (!inputPropsChanged && !inputResponseDataTypeChanged && !inputOrderChanged && !inputSizeChanged && !choiceTextOrOrderOrSizeChangedForAnyInput) {
        // copy ic id from old to new
        log.info("nothing changed in inputs or choices");
        InputCollection temp = newVersion.get(currentNewGroupName);
        if (oldVersion.get(currentNewGroupName) != null ) {
          temp.setInputCollectionId(oldVersion.get(currentNewGroupName).getInputCollectionId());
        }
        newVersion.put(currentNewGroupName, temp);
      } else {
        log.info("something changed in inputs or choices");
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
      // when old version has choices c1, c2, c3 and new version has c1, c2. The order has not changed and labels have not changed.
      // but we still need to change the choice collection. we detect this scenario by comparing the sizes
      if (!(currentNewChoices.size() == matchingOldChoices.size())) {
        choiceSizeChanged = true;
      } else {
        currentNewChoiceCollection.setChoiceCollectionId(matchingOldChoiceCollection.getChoiceCollectionId());
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
                break;
              }
            } else {
              choiceTextChanged = true;
              break;
            }
          } else {
            choiceTextChanged = true;
            break;
          }
          
        } // end while for input iterator
        // all choices for this input iterated
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
    boolean isVersionAlreadyPresent = getNumberOfGroups(experimentDao.getId(), experimentDao.getVersion()) > 0;
    if (!isVersionAlreadyPresent) {
      Map<String, ExperimentVersionMapping> allGroupsInPrevVersion = getAllGroupsInVersion(experimentDao.getId(), experimentDao.getVersion()-1);
      log.info("experiment dao is " +experimentDao);
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
    Integer currentGroupType = null;
    Map<String, InputOrderAndChoice> returnEVMHInputOrderAndChoices = null;
    InputOrderAndChoice inputOrderAndChoiceObj = null;
    DataType dataType = null;
    InformedConsent informedConsent = null;
    ResultSet rs = null;
    Long recordCt = 0L;
    
    User creator = new User();

    String query = "select * from experiment_version_mapping evmh join experiment eh on evmh.experiment_facet_id = eh.experiment_facet_id " + 
            " join `group` gh on evmh.group_id = gh.group_id " +
            " join `group_type` gt on gh.group_type_id = gt.group_type_id " + 
            " left join input_collection ich on ich.input_collection_id = evmh.input_collection_id and ich.experiment_ds_id = evmh.experiment_id " +
            " left join input ih on ich.input_id = ih.input_id " +
            " join user u on eh.creator = u.user_id " +
            " left join choice_collection cch on ich.choice_collection_id = cch.choice_collection_id and ich.experiment_ds_id = cch.experiment_ds_id " +
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
          currentGroupType = rs.getInt(GroupColumns.GROUP_TYPE_ID);
          
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
          eHistory.setTitle(rs.getString(ExperimentColumns.EXPERIMENT_NAME));
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
          gHistory.setGroupTypeId(currentGroupType);
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
            returnEVMH.setExperimentVersion(rs.getInt(ExperimentVersionMappingColumns.EXPERIMENT_VERSION));
            returnEVMH.setExperimentInfo(eHistory);
            returnEVMH.setExperimentVersionMappingId(rs.getLong(ExperimentVersionMappingColumns.EXPERIMENT_VERSION_MAPPING_ID));
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

  @Override
  public PacoId getExperimentVersionMappingId(Long experimentId, Integer version, String groupName) throws SQLException {
    PacoId returnId = new PacoId();
    Connection conn = null;
    ResultSet rs = null;
    ResultSet rs1 = null;
    int ct = 1;
    PreparedStatement statementSelectExperimentVersionMapping = null;
    PreparedStatement statementCreateExperimentVersionMapping = null;
    final String updateValueForExperimentVersionMappingId = "select "+ ExperimentVersionMappingColumns.EXPERIMENT_VERSION_MAPPING_ID +" from " + ExperimentVersionMappingColumns.TABLE_NAME + " evm join "  + GroupColumns.TABLE_NAME + " g on evm. " + GroupColumns.GROUP_ID + " g." + GroupColumns.GROUP_ID +" where " + ExperimentVersionMappingColumns.EXPERIMENT_ID + " = ? and "  + GroupColumns.NAME + " = ? and "  + ExperimentVersionMappingColumns.EXPERIMENT_VERSION + " = ? " ;
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
        returnId.setId(new Long(rs.getInt(ExperimentVersionMappingColumns.EXPERIMENT_VERSION_MAPPING_ID)));
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
        possibleVersions.add(rs.getInt(ExperimentVersionMappingColumns.EXPERIMENT_VERSION));
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
    Experiment expFacet = null;
    Group group = null;
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
          expFacet = new Experiment();
          group = new Group();
          inputCollection = new InputCollection();
          expFacet.setExperimentFacetId(new PacoId(rs.getLong(ExperimentVersionMappingColumns.EXPERIMENT_FACET_ID), false));
          group.setGroupId(new PacoId(rs.getLong(ExperimentVersionMappingColumns.GROUP_ID), false));
          inputCollection.setInputCollectionId(rs.getLong(ExperimentVersionMappingColumns.INPUT_COLLECTION_ID));
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
  public ExperimentVersionMapping prepareEVMForGroupWithInputsAllScenarios(Long experimentId, String experimentName, Integer experimentVersion, String groupName, String whoEmail, Set<What> whatSet) throws SQLException {
    Map<String, ExperimentVersionMapping> allEVMRecords = null;
    GroupTypeEnum matchingGroupType = null;
    Boolean deletedExperiment = false;
    
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
        deletedExperiment = true;
        addExperimentInfoToDB(evm, experimentName, whoEmail, deletedExperiment);
        addGroupInfoToDB(evm, groupName, matchingGroupType);
        if (matchingGroupType.getGroupTypeId() == GroupTypeEnum.SURVEY.getGroupTypeId()) {
          List<String> whatList = Lists.newArrayList(); 
          for (What eachWhat : whatSet) {
            whatList.add(eachWhat.getName());
          }
          addInputCollectionToDBByVarName(evm, whatList);
        } else {
          CSGroupTypeInputMappingDao gtimDao = new CSGroupTypeInputMappingDaoImpl();
          List<Input> inputLst = gtimDao.getAllFeatureInputs().get(matchingGroupType.name().toLowerCase());
          addInputCollectionToDBByInput(evm, inputLst);
        }
        // insert to DB then return EVM right away
        createExperimentVersionMapping(Lists.newArrayList(evm));
        allEVMRecords = daoImpl.getAllGroupsInVersion(experimentId, experimentVersion);
        failedDaoImpl.insertFailedEvent("expId: " + experimentId + "expVersion: "+ experimentVersion + ",who:"+ whoEmail , "Did not find any closestVersion. ", "Did not find any closestVersion.");
        return evm;
      } else {
        log.info("older version of this experiment exists");
      }
    }
    // check for groups if we have some older version
    ExperimentVersionMapping matchingEVMInDB = allEVMRecords.get(groupName);

    if (matchingEVMInDB == null) {
      // no matching group name in DB
      // get some exp facet object to use
      Iterator<String> evmItr = allEVMRecords.keySet().iterator();
      Experiment expInfo = null;
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
        List<Input> inputLst = gtimDao.getAllFeatureInputs().get(matchingGroupType.name().toLowerCase());
        addInputCollectionToDBByInput(evm, inputLst);
      }
      //  insert to db, then return right away
      createExperimentVersionMapping(Lists.newArrayList(evm));
      return evm;
    } else {
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
    }
    return matchingEVMInDB;
  }
  
  private void  addExperimentInfoToDB(ExperimentVersionMapping evm, String experimentName, String whoEmail, boolean deletedFlag) throws SQLException {
    Experiment eInfo = new Experiment();
    User user = new User();
    CSExperimentUserDao expUserDao = new CSExperimentUserDaoImpl();
    CSExperimentDao expDao = new CSExperimentDaoImpl();
    if (deletedFlag) { 
      evm.setSource("Recreating Deleted Experiment");
      eInfo.setDeleted(true);
    }
    PacoId userId = expUserDao.getAnonymousIdAndCreate(evm.getExperimentId(), whoEmail, true);
    user.setUserId(userId);
    user.setWho(whoEmail);
    eInfo.setCreator(user);
    eInfo.setTitle(experimentName);
    expDao.insertExperiment(eInfo);
    evm.setExperimentInfo(eInfo);
    
  }
  
  private void  addGroupInfoToDB(ExperimentVersionMapping evm, String groupName, GroupTypeEnum groupType) throws SQLException {
    CSGroupDao grpDaoImp = new CSGroupDaoImpl();
    Group grp = new Group();
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
          } else if ( crFeatureName.equalsIgnoreCase(GroupTypeEnum.NOTFICATION.name()) || (crFeatureName.equalsIgnoreCase(GroupTypeEnum.ACCESSIBILITY.name()))){
            if (eachWhat.getName().equalsIgnoreCase("accessibilityEventType")) {
              if (eachWhat.getValue().equalsIgnoreCase("15")) {
                matchingGroupType = GroupTypeEnum.ACCESSIBILITY;
                break;
              } else {
                matchingGroupType = GroupTypeEnum.NOTFICATION;
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
  
  
//  @Override
//  public ExperimentVersionMapping prepareEVMForGroupWithInputs(Long experimentId, String experimentName, Integer experimentVersion, String groupName, String whoEmail, List<String> inputVariables) throws SQLException {
//    Long expFacetId = null;
//    Experiment exptFacet = null;
//    CSGroupDao groupDao = new CSGroupDaoImpl();
//    CSExperimentDao expDao = new CSExperimentDaoImpl();
//    CSExperimentUserDao expUserDao = new CSExperimentUserDaoImpl();
//    ExperimentVersionMapping evm = new ExperimentVersionMapping();
//    User user = new User();
//    evm.setExperimentId(experimentId);
//    evm.setExperimentVersion(experimentVersion);
//    
//    expFacetId = expDao.getExperimentFacetId(experimentId, experimentVersion);
//    
//    if (expFacetId == null) {
//      exptFacet = new Experiment();
//      exptFacet.setDeleted(true);
//      exptFacet.setTitle(experimentName);
//      PacoId userId = expUserDao.getAnonymousIdAndCreate(experimentId, whoEmail, true);
//      user.setUserId(userId);
//      user.setWho(whoEmail);
//      exptFacet.setCreator(user);
//      expDao.insertExperiment(exptFacet);
//      evm.setSource("Generated ...");
//    } else {
//      // only this grp name is not there, there is something on this expt and version
//      exptFacet = new Experiment();
//      exptFacet.setExperimentFacetId(new PacoId(expFacetId, false));
//      evm.setSource("Only this group was generated");
//    }
//    evm.setExperimentInfo(exptFacet);
//      
//    Group grp = new Group();
//    grp.setName(groupName);
//    grp.setFixedDuration(false);
//    grp.setRawDataAccess(false);
//    grp.setGroupTypeId(GroupTypeEnum.SURVEY.getGroupTypeId());
//    groupDao.insertGroup(Lists.newArrayList(grp));
//    evm.setGroupInfo(grp);
//    
//    InputCollection inputCollection = new InputCollection();
//    CSInputDao inputDaoImpl = new CSInputDaoImpl();
//    CSInputCollectionDao inputCollectionDaoImpl = new CSInputCollectionDaoImpl();
//    List<Input> inputs = inputDaoImpl.insertVariableNames(inputVariables);
//    Integer noOfGroups = getNumberOfGroups(experimentId, experimentVersion);
//    inputCollection.setInputCollectionId(IdGenerator.generate(BigInteger.valueOf(experimentVersion), noOfGroups+1).longValue());
//    
//    for (Input eachInput : inputs) {
//      inputCollectionDaoImpl.addUndefinedInputToCollection(experimentId, inputCollection.getInputCollectionId(), eachInput.getInputId().getId());
//    }
//    inputCollection.setInputOrderAndChoices(convertInputListToInputOrderAndChoice(inputs));
//    evm.setInputCollection(inputCollection);
////    createExperimentVersionMapping(Lists.newArrayList(evm));
//    return evm;
//  }
  
//  @Override
//  public ExperimentVersionMapping prepareEVMForDeletedExperiment(Long experimentId, String experimentName, Integer experimentVersion, String groupName, String whoEmail, List<String> inputVariables) throws SQLException {
//    User user = new User();
//    CSGroupDao groupDao = new CSGroupDaoImpl();
//    CSExperimentUserDao expUserDao = new CSExperimentUserDaoImpl();
//    CSExperimentDao expDao = new CSExperimentDaoImpl();
//    ExperimentVersionMapping evm = new ExperimentVersionMapping();
//    evm.setExperimentId(experimentId);
//    evm.setExperimentVersion(experimentVersion);
//    
//    Experiment exptFacet = new Experiment();
//    exptFacet.setDeleted(true);
//    exptFacet.setTitle(experimentName);
//    PacoId userId = expUserDao.getAnonymousIdAndCreate(experimentId, whoEmail, true);
//    user.setUserId(userId);
//    user.setWho(whoEmail);
//    exptFacet.setCreator(user);
//    expDao.insertExperiment(exptFacet);
//    evm.setExperimentInfo(exptFacet);
//    
//    Group grp = new Group();
//    grp.setName(groupName);
//    grp.setFixedDuration(false);
//    grp.setRawDataAccess(false);
//    grp.setGroupTypeId(GroupTypeEnum.SURVEY.getGroupTypeId());
//    groupDao.insertGroup(Lists.newArrayList(grp));
//    evm.setGroupInfo(grp);
//    
//    InputCollection inputCollection = new InputCollection();
//    CSInputDao inputDaoImpl = new CSInputDaoImpl();
//    CSInputCollectionDao inputCollectionDaoImpl = new CSInputCollectionDaoImpl();
//    
//    List<Input> inputs = inputDaoImpl.insertVariableNames(inputVariables);
//    Integer noOfGroups = getNumberOfGroups(experimentId, experimentVersion);
//    inputCollection.setInputCollectionId(IdGenerator.generate(BigInteger.valueOf(experimentVersion), noOfGroups+1).longValue());
//    
//    for (Input eachInput : inputs) {
//      inputCollectionDaoImpl.addUndefinedInputToCollection(experimentId, inputCollection.getInputCollectionId(), eachInput.getInputId().getId());
//    }
//    inputCollection.setInputOrderAndChoices(convertInputListToInputOrderAndChoice(inputs));
//    evm.setInputCollection(inputCollection);
//    evm.setSource("Generated because experiment was deleted");
//    // TODO ADD SYSTEM GRP
////    List<ExperimentVersionMapping> newLst = Lists.newArrayList(evm);
////    createExperimentVersionMapping(newLst);
//    return evm;
//  }
  
//  @Override
//  public ExperimentVersionMapping prepareEVMWithPredefinedInputs(Long experimentId, Integer experimentVersion, String groupName, List<Input> inputs, String groupTypeName) throws SQLException {
//
//    CSGroupDao groupDao = new CSGroupDaoImpl();
//    CSInputCollectionDao inputCollectionDaoImpl = new CSInputCollectionDaoImpl();
//    
//    ExperimentVersionMapping evm = new ExperimentVersionMapping();
//    evm.setExperimentId(experimentId);
//    evm.setExperimentVersion(experimentVersion);
//    
//    Group grp = new Group();
//    grp.setName(groupTypeName.toLowerCase());
//    grp.setFixedDuration(false);
//    grp.setRawDataAccess(false);
//    grp.setGroupTypeId(GroupTypeEnum.valueOf(groupTypeName).getGroupTypeId());
//    
//    groupDao.insertGroup(Lists.newArrayList(grp));
//    evm.setGroupInfo(grp);
//    
//    InputCollection inputCollection = new InputCollection();
//    inputCollection.setInputOrderAndChoices(convertInputListToInputOrderAndChoice(inputs));
//    Integer noOfGroups = getNumberOfGroups(experimentId, experimentVersion);
//    inputCollection.setInputCollectionId(IdGenerator.generate(BigInteger.valueOf(experimentVersion), noOfGroups+1).longValue());
//    
//    for (Input eachInput : inputs) {
//      inputCollectionDaoImpl.addUndefinedInputToCollection(experimentId, inputCollection.getInputCollectionId(), eachInput.getInputId().getId());
//    }
//    inputCollection.setInputOrderAndChoices(convertInputListToInputOrderAndChoice(inputs));
//    evm.setInputCollection(inputCollection);
//    evm.setSource("Generated during migration1");
//
////    createExperimentVersionMapping(Lists.newArrayList(evm));
//    return evm;
//  }
//  
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
   
    final String updateValueForExperimentVersionMappingId = "select "+ ExperimentVersionMappingColumns.EXPERIMENT_VERSION_MAPPING_ID +
            " from " + ExperimentVersionMappingColumns.TABLE_NAME + " evm "+
            " where " + ExperimentVersionMappingColumns.EXPERIMENT_ID + " = ? and  " + ExperimentVersionMappingColumns.EXPERIMENT_VERSION + " = ? " ;
  
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
  
  
}
