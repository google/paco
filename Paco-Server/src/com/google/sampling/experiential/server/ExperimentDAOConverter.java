package com.google.sampling.experiential.server;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.dao.CSDataTypeDao;
import com.google.sampling.experiential.dao.CSExternStringTextDao;
import com.google.sampling.experiential.dao.CSInformedConsentDao;
import com.google.sampling.experiential.dao.CSUserDao;
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
import com.google.sampling.experiential.dao.impl.CSDataTypeDaoImpl;
import com.google.sampling.experiential.dao.impl.CSExternStringTextDaoImpl;
import com.google.sampling.experiential.dao.impl.CSInformedConsentDaoImpl;
import com.google.sampling.experiential.dao.impl.CSUserDaoImpl;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.Input2;

public class ExperimentDAOConverter {
  DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY/MM/dd");
  public Experiment convertToExperiment(ExperimentDAO experimentDao) throws SQLException {
    InformedConsent newInformedConsent = null;
    if (experimentDao.getInformedConsentForm() != null && !experimentDao.getInformedConsentForm().equals("")) {
      newInformedConsent = new InformedConsent();
      newInformedConsent.setInformedConsent(experimentDao.getInformedConsentForm());
    }
    Experiment experiment = new Experiment();
    experiment.setTitle(experimentDao.getTitle());
    experiment.setDescription(experimentDao.getDescription());
    // create option should always be false. we should have the user authenticated by the time we hit save button.
    experiment.setCreator(convertToUserAndCreate(experimentDao.getCreator(), false));
    experiment.setOrganization(experimentDao.getOrganization());
    experiment.setContactEmail(experimentDao.getContactEmail());
    experiment.setInformedConsent(newInformedConsent);
    experiment.setDeleted(experimentDao.getDeleted());
    experiment.setModifiedDate(experimentDao.getModifyDate() != null ? formatter.parseDateTime(experimentDao.getModifyDate()) : null);
    experiment.setPublished(experimentDao.getPublished());
    experiment.setRingtoneUri(experimentDao.getRingtoneUri());
    experiment.setPostInstallInstructions(experimentDao.getPostInstallInstructions());
    
    return experiment; 
  }
  
  public List<Group> convertToGroup(List<ExperimentGroup> experimentGroups) {
    List<Group> groups = Lists.newArrayList();
    Group group = null;
    for ( ExperimentGroup experimentGroup : experimentGroups) {
      group = new Group();
      group.setName(experimentGroup.getName());
      group.setCustomRendering(experimentGroup.getCustomRenderingCode());
      group.setFixedDuration(experimentGroup.getFixedDuration());
      group.setStartDate(experimentGroup.getStartDate() != null ? formatter.parseDateTime(experimentGroup.getStartDate()) : null);
      group.setEndDate(experimentGroup.getEndDate() != null ? formatter.parseDateTime(experimentGroup.getEndDate()) : null);
      group.setRawDataAccess(experimentGroup.getRawDataAccess());
      group.setEndOfDayGroup(experimentGroup.getEndOfDayReferredGroupName());
      groups.add(group);
    }  
    return groups; 
  }
  
  public InputCollection convertToInputCollection(ExperimentGroup experimentGroup) throws SQLException {
    InputCollection inputCollection = null;
    Input inputObj = null;
    ChoiceCollection choiceCollectionObj = null;
    InputOrderAndChoice inputOrderAndChoiceObj = null;
    Input2 input2Obj = null;
    if (experimentGroup.getInputs().size() > 0) {
      inputCollection = new InputCollection();
      Map<String, InputOrderAndChoice> inputOrderAndChoices = Maps.newLinkedHashMap();
      for (int order=0; order<experimentGroup.getInputs().size(); order++) {
        input2Obj = experimentGroup.getInputs().get(order);
        inputObj = convertToInput(input2Obj);
        inputOrderAndChoiceObj = new InputOrderAndChoice();
        inputOrderAndChoiceObj.setInput(inputObj);
        inputOrderAndChoiceObj.setInputOrder(order+1);
        choiceCollectionObj = convertToChoiceCollectionAndCreate(input2Obj.getListChoices());
        inputOrderAndChoiceObj.setChoiceCollection(choiceCollectionObj);
        inputOrderAndChoices.put(inputObj.getName().getLabel(), inputOrderAndChoiceObj);
      }
      inputCollection.setInputOrderAndChoices(inputOrderAndChoices);
    }
    return inputCollection; 
  }
  
  public User convertToUserAndCreate(String email, Boolean createOption) throws SQLException {
    CSUserDao userDao = new CSUserDaoImpl();
    User user = new User();
    user.setUserId(userDao.getUserAndCreate(email, createOption).getUserId());
    user.setWho(email);
    return user; 
  }
  
  public ChoiceCollection convertToChoiceCollectionAndCreate(List<String> choices) throws SQLException {
    ChoiceCollection choiceCollection = null;
    Map<String, Choice> choiceOrderMap = Maps.newHashMap();
    
    if ( choices != null && choices.size() >0) {
      choiceCollection = new ChoiceCollection();
      ExternStringListLabel label = null;
      String currentChoice = null;
      Choice choice = null;
      for (int order=0; order < choices.size(); order++) {
        currentChoice = choices.get(order);
        choice = new Choice();
        label = new ExternStringListLabel();
        label.setExternStringListLabelId(null);
        label.setLabel(currentChoice);
        choice.setChoiceLabel(label);
        choice.setChoiceOrder(order+1);
        choiceOrderMap.put(currentChoice, choice);
      }
      choiceCollection.setChoices(choiceOrderMap);
    }
    return choiceCollection;
  }
  
  public Input convertToInput(Input2 input2Obj) throws SQLException {
    Input inputObj = new Input();
    CSDataTypeDao dataTypeDao = new CSDataTypeDaoImpl();
    List<DataType> allDataTypes = dataTypeDao.getAllDataTypes();
    ExternStringInput variableName = new ExternStringInput();
    ExternStringInput variableText = new ExternStringInput();
    DataType responseDataType = dataTypeDao.getMatchingDataType(allDataTypes, input2Obj.getResponseType(), input2Obj.isNumeric(), input2Obj.getMultiselect());
    variableName.setLabel(input2Obj.getName());
    variableText.setLabel(input2Obj.getText());
    
    inputObj.setName(variableName);
    inputObj.setRequired(input2Obj.getRequired());
    inputObj.setConditional(input2Obj.getConditionExpression());
    inputObj.setResponseDataType(responseDataType);
    inputObj.setText(variableText);
    inputObj.setLikertSteps(input2Obj.getLikertSteps());
    inputObj.setLeftLabel(input2Obj.getLeftSideLabel());
    inputObj.setRightLabel(input2Obj.getRightSideLabel());
    inputObj.setChannel("Group");
    return inputObj;
  }
  
  public List<ExperimentVersionMapping> convertToExperimentVersionMapping(ExperimentDAO experimentDao) throws SQLException {
    List<ExperimentVersionMapping> newMappingList = Lists.newArrayList();
    ExperimentVersionMapping currentMappingObj = null;
    List<Group> convertedGroups = convertToGroup(experimentDao.getGroups());
   
    for (Group group : convertedGroups) {
      currentMappingObj = new ExperimentVersionMapping();
      currentMappingObj.setExperimentInfo(convertToExperiment(experimentDao));
      currentMappingObj.setExperimentId(experimentDao.getId());
      currentMappingObj.setExperimentVersion(experimentDao.getVersion());
      currentMappingObj.setGroupInfo(group);
      currentMappingObj.setInputCollection(convertToInputCollection(experimentDao.getGroupByName(group.getName())));
      newMappingList.add(currentMappingObj);
    }
    
    return newMappingList;
    
  }
}
