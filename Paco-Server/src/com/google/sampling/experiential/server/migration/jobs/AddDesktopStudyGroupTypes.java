package com.google.sampling.experiential.server.migration.jobs;

import com.google.sampling.experiential.cloudsql.columns.GroupTypeColumns;
import com.google.sampling.experiential.dao.CSGroupTypeDao;
import com.google.sampling.experiential.dao.CSGroupTypeInputMappingDao;
import com.google.sampling.experiential.dao.CSInputDao;
import com.google.sampling.experiential.dao.dataaccess.DataType;
import com.google.sampling.experiential.dao.dataaccess.GroupTypeInputMapping;
import com.google.sampling.experiential.dao.dataaccess.Input;
import com.google.sampling.experiential.dao.dataaccess.PredefinedInputNames;
import com.google.sampling.experiential.dao.impl.CSGroupTypeDaoImpl;
import com.google.sampling.experiential.dao.impl.CSGroupTypeInputMappingDaoImpl;
import com.google.sampling.experiential.dao.impl.CSInputDaoImpl;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.google.sampling.experiential.server.ExceptionUtil;
import com.pacoapp.paco.shared.model2.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

public class AddDesktopStudyGroupTypes {
  public static final Logger log = Logger.getLogger(AddDesktopStudyGroupTypes.class.getName());

  final String insertAppUsageDesktopType = "INSERT INTO `pacodb`.`" + GroupTypeColumns.TABLE_NAME +
          "` (`" + GroupTypeColumns.GROUP_TYPE_NAME + "`) VALUES ('" + GroupTypeEnum.APPUSAGE_DESKTOP + "')";
  final String insertAppUsageShellType = "INSERT INTO `pacodb`.`" + GroupTypeColumns.TABLE_NAME +
          "` (`" + GroupTypeColumns.GROUP_TYPE_NAME + "`) VALUES ('" + GroupTypeEnum.APPUSAGE_SHELL + "')";
  final String insertIdeIdeaUsageType = "INSERT INTO `pacodb`.`" + GroupTypeColumns.TABLE_NAME +
          "` (`" + GroupTypeColumns.GROUP_TYPE_NAME + "`) VALUES ('" + GroupTypeEnum.IDE_IDEA_USAGE + "')";

  public boolean insertNewGroupTypes() throws Exception {
    executeCreationOrInsertionQueries(new String[]{
            insertAppUsageDesktopType,
            insertAppUsageShellType,
            insertIdeIdeaUsageType
    });
    populateInputsForGroupTypes();
    return true;
  }

  private boolean populateInputsForGroupTypes() throws Exception {

    CSInputDao inputDaoImpl = new CSInputDaoImpl();
    CSGroupTypeInputMappingDao predfinedDaoImpl = new CSGroupTypeInputMappingDaoImpl();
    CSGroupTypeDao groupTypeDapImpl = new CSGroupTypeDaoImpl();
    DataType openTextDataType = new DataType("open text", false, false);

    try {
      addAppUsageDesktopGroup(inputDaoImpl, predfinedDaoImpl, groupTypeDapImpl, openTextDataType);
      addAppUsageShellGroup(inputDaoImpl, predfinedDaoImpl, groupTypeDapImpl, openTextDataType);
      addIdeIdeaUsageGroup(inputDaoImpl, predfinedDaoImpl, groupTypeDapImpl, openTextDataType);
    } catch (SQLException sqle) {
      log.warning("SQLException while adding new group type " + ExceptionUtil.getStackTraceAsString(sqle));
      throw sqle;
    } catch (Exception e) {
      log.warning("GException while adding new group type " + ExceptionUtil.getStackTraceAsString(e));
      throw e;
    }
    return true;
  }

  private void addAppUsageDesktopGroup(CSInputDao inputDaoImpl, CSGroupTypeInputMappingDao predfinedDaoImpl, CSGroupTypeDao groupTypeDapImpl, DataType openTextDataType) throws Exception {
    Input appsUsedInput = new Input(PredefinedInputNames.APPS_USED, false, null, openTextDataType,
            PredefinedInputNames.APPS_USED, 0, null, null, null);
    Input appUsageRawInput = new Input(PredefinedInputNames.APPS_USED_RAW, false, null, openTextDataType,
            PredefinedInputNames.APPS_USED_RAW, 0, null, null, null);
    Input foregroundInput = new Input(PredefinedInputNames.FOREGROUND, false, null, openTextDataType,
            PredefinedInputNames.FOREGROUND, 0, null, null, null);
    Input userPresentInput = new Input(PredefinedInputNames.USER_PRESENT, false, null, openTextDataType,
            PredefinedInputNames.USER_PRESENT, 0, null, null, null);
    Input userNotPresentInput = new Input(PredefinedInputNames.USER_NOT_PRESENT, false, null, openTextDataType,
            PredefinedInputNames.USER_NOT_PRESENT, 0, null, null, null);

    Integer groupId = groupTypeDapImpl.getGroupTypeId(GroupTypeEnum.APPUSAGE_DESKTOP.name());

    inputDaoImpl.insertInput(appsUsedInput);
    inputDaoImpl.insertInput(appUsageRawInput);
    inputDaoImpl.insertInput(foregroundInput);
    inputDaoImpl.insertInput(userPresentInput);
    inputDaoImpl.insertInput(userNotPresentInput);

    predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(groupId, appsUsedInput));
    predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(groupId, appUsageRawInput));
    predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(groupId, foregroundInput));
    predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(groupId, userPresentInput));
    predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(groupId, userNotPresentInput));
  }

  private void addAppUsageShellGroup(CSInputDao inputDaoImpl, CSGroupTypeInputMappingDao predfinedDaoImpl, CSGroupTypeDao groupTypeDapImpl, DataType openTextDataType) throws Exception {
    Input appsUsedInput = new Input(PredefinedInputNames.APPS_USED, false, null, openTextDataType,
            PredefinedInputNames.APPS_USED, 0, null, null, null);
    Input appUsageRawInput = new Input(PredefinedInputNames.APPS_USED_RAW, false, null, openTextDataType,
            PredefinedInputNames.APPS_USED_RAW, 0, null, null, null);
    Input foregroundInput = new Input(PredefinedInputNames.FOREGROUND, false, null, openTextDataType,
            PredefinedInputNames.FOREGROUND, 0, null, null, null);
    Input userPresentInput = new Input(PredefinedInputNames.USER_PRESENT, false, null, openTextDataType,
            PredefinedInputNames.USER_PRESENT, 0, null, null, null);
    Input userNotPresentInput = new Input(PredefinedInputNames.USER_NOT_PRESENT, false, null, openTextDataType,
            PredefinedInputNames.USER_NOT_PRESENT, 0, null, null, null);

    Integer groupId = groupTypeDapImpl.getGroupTypeId(GroupTypeEnum.APPUSAGE_SHELL.name());

    inputDaoImpl.insertInput(appsUsedInput);
    inputDaoImpl.insertInput(appUsageRawInput);
    inputDaoImpl.insertInput(foregroundInput);
    inputDaoImpl.insertInput(userPresentInput);
    inputDaoImpl.insertInput(userNotPresentInput);

    predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(groupId, appsUsedInput));
    predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(groupId, appUsageRawInput));
    predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(groupId, foregroundInput));
    predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(groupId, userPresentInput));
    predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(groupId, userNotPresentInput));
  }

  private void addIdeIdeaUsageGroup(CSInputDao inputDaoImpl, CSGroupTypeInputMappingDao predfinedDaoImpl,
                                    CSGroupTypeDao groupTypeDapImpl, DataType openTextDataType) throws Exception {
    Input appsUsedInput = new Input(PredefinedInputNames.APPS_USED, false, null, openTextDataType,
            PredefinedInputNames.APPS_USED, 0, null, null, null);
    Input appUsageRawInput = new Input(PredefinedInputNames.APPS_USED_RAW, false, null, openTextDataType,
            PredefinedInputNames.APPS_USED_RAW, 0, null, null, null);
    Input foregroundInput = new Input(PredefinedInputNames.FOREGROUND, false, null, openTextDataType,
            PredefinedInputNames.FOREGROUND, 0, null, null, null);
    Input userPresentInput = new Input(PredefinedInputNames.USER_PRESENT, false, null, openTextDataType,
            PredefinedInputNames.USER_PRESENT, 0, null, null, null);
    Input userNotPresentInput = new Input(PredefinedInputNames.USER_NOT_PRESENT, false, null, openTextDataType,
            PredefinedInputNames.USER_NOT_PRESENT, 0, null, null, null);

    Integer groupId = groupTypeDapImpl.getGroupTypeId(GroupTypeEnum.IDE_IDEA_USAGE.name());

    inputDaoImpl.insertInput(appsUsedInput);
    inputDaoImpl.insertInput(appUsageRawInput);
    inputDaoImpl.insertInput(foregroundInput);
    inputDaoImpl.insertInput(userPresentInput);
    inputDaoImpl.insertInput(userNotPresentInput);

    predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(groupId, appsUsedInput));
    predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(groupId, appUsageRawInput));
    predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(groupId, foregroundInput));
    predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(groupId, userPresentInput));
    predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(groupId, userNotPresentInput));
  }


  private boolean executeCreationOrInsertionQueries(String[] creationQrys) throws SQLException {
    boolean isComplete = false;

    Connection conn = null;
    PreparedStatement statementCreateOrInsertQuery = null;

    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      for (int i = 0; i < creationQrys.length; i++) {
        statementCreateOrInsertQuery = conn.prepareStatement(creationQrys[i]);
        log.info(creationQrys[i]);
        statementCreateOrInsertQuery.execute();
      }
      isComplete = true;
    } catch (SQLException sqle) {
      log.warning("SQLException while creating tables" + ExceptionUtil.getStackTraceAsString(sqle));
      throw sqle;
    } catch (Exception e) {
      log.warning("GException while creating tables" + ExceptionUtil.getStackTraceAsString(e));
      throw e;
    } finally {
      try {
        if (statementCreateOrInsertQuery != null) {
          statementCreateOrInsertQuery.close();
        }

        if (conn != null) {
          conn.close();
        }
      } catch (SQLException e) {
        log.warning("Exception in finally block" + ExceptionUtil.getStackTraceAsString(e));
      }
    }

    return isComplete;
  }


}
