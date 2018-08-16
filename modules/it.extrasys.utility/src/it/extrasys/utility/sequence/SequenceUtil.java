package it.extrasys.utility.sequence;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.data.UtilSql;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.database.RDBMSIndependent;
import org.openbravo.exception.NoConnectionAvailableException;
import org.openbravo.exception.PoolNotFoundException;
import org.openbravo.service.db.DalConnectionProvider;

public class SequenceUtil {

  private static Logger log4j = Logger.getLogger(SequenceUtil.class);

  public static String nextDocumentNumber(String cDocTypeTableName, String adClientId,
      String adOrgId, String updateNext) throws Exception {
    ConnectionProvider connectionProvider = null;
    connectionProvider = new DalConnectionProvider(false);
    String strSql = "";
    strSql = strSql + "        CALL UTIL_Sequence_Doc(?,?,?,?)";

    String razon = "";
    CallableStatement st = null;
    if (connectionProvider.getRDBMS().equalsIgnoreCase("ORACLE")) {

      int iParameter = 0;
      try {
        st = connectionProvider.getCallableStatement(strSql);
        iParameter++;
        UtilSql.setValue(st, iParameter, 12, null, cDocTypeTableName);
        iParameter++;
        UtilSql.setValue(st, iParameter, 12, null, adClientId);
        iParameter++;
        UtilSql.setValue(st, iParameter, 12, null, adOrgId);
        iParameter++;
        UtilSql.setValue(st, iParameter, 12, null, updateNext);
        int iParameterrazon = iParameter + 1;
        iParameter++;
        st.registerOutParameter(iParameter, 12);

        st.execute();
        razon = UtilSql.getStringCallableStatement(st, iParameterrazon);
      } catch (Exception ex) {

        throw new Exception("@CODE=@" + ex.getMessage());
      } finally {
        try {
          connectionProvider.releasePreparedStatement(st);
        } catch (Exception ignore) {
          ignore.printStackTrace();
        }
      }
    } else {
      Vector<String> parametersData = new Vector<String>();
      Vector<String> parametersTypes = new Vector<String>();
      parametersData.addElement(cDocTypeTableName);
      parametersTypes.addElement("in");
      parametersData.addElement(adClientId);
      parametersTypes.addElement("in");
      parametersData.addElement(adOrgId);
      parametersTypes.addElement("in");
      parametersData.addElement(updateNext);
      parametersTypes.addElement("in");
      parametersData.addElement("razon");
      parametersTypes.addElement("out");
      Vector<String> vecTotal = new Vector<String>();
      try {
        vecTotal = RDBMSIndependent.getCallableResult(null, connectionProvider, strSql,
            parametersData, parametersTypes, 1);
        razon = (String) vecTotal.elementAt(0);
      } catch (SQLException e) {
        log4j.error("SQL error in query: " + strSql + "Exception:" + e);
        throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
            + e.getMessage());
      } catch (NoConnectionAvailableException ec) {
        log4j.error("Connection error in query: " + strSql + "Exception:" + ec);
        throw new ServletException("@CODE=NoConnectionAvailable");
      } catch (PoolNotFoundException ep) {
        log4j.error("Pool error in query: " + strSql + "Exception:" + ep);
        throw new ServletException("@CODE=NoConnectionAvailable");
      } catch (Exception ex) {
        log4j.error("Exception in query: " + strSql + "Exception:" + ex);
        throw new ServletException("@CODE=@" + ex.getMessage());
      }
    }
    return (razon);
  }

  public static String nextDocumentNumber(String cDocTypeTableName, String adClientId,
      String updateNext) throws Exception {
    ConnectionProvider connectionProvider = null;
    connectionProvider = new DalConnectionProvider(false);
    String strSql = "";
    strSql = strSql + "        CALL AD_Sequence_Doc(?,?,?,?)";

    String razon = "";
    CallableStatement st = null;
    if (connectionProvider.getRDBMS().equalsIgnoreCase("ORACLE")) {

      int iParameter = 0;
      try {
        st = connectionProvider.getCallableStatement(strSql);
        iParameter++;
        UtilSql.setValue(st, iParameter, 12, null, cDocTypeTableName);
        iParameter++;
        UtilSql.setValue(st, iParameter, 12, null, adClientId);
        iParameter++;
        UtilSql.setValue(st, iParameter, 12, null, updateNext);
        int iParameterrazon = iParameter + 1;
        iParameter++;
        st.registerOutParameter(iParameter, 12);

        st.execute();
        razon = UtilSql.getStringCallableStatement(st, iParameterrazon);
      } catch (Exception ex) {

        throw new Exception("@CODE=@" + ex.getMessage());
      } finally {
        try {
          connectionProvider.releasePreparedStatement(st);
        } catch (Exception ignore) {
          ignore.printStackTrace();
        }
      }
    } else {
      Vector<String> parametersData = new Vector<String>();
      Vector<String> parametersTypes = new Vector<String>();
      parametersData.addElement(cDocTypeTableName);
      parametersTypes.addElement("in");
      parametersData.addElement(adClientId);
      parametersTypes.addElement("in");
      parametersData.addElement(updateNext);
      parametersTypes.addElement("in");
      parametersData.addElement("razon");
      parametersTypes.addElement("out");
      Vector<String> vecTotal = new Vector<String>();
      try {
        vecTotal = RDBMSIndependent.getCallableResult(null, connectionProvider, strSql,
            parametersData, parametersTypes, 1);
        razon = (String) vecTotal.elementAt(0);
      } catch (SQLException e) {
        log4j.error("SQL error in query: " + strSql + "Exception:" + e);
        throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
            + e.getMessage());
      } catch (NoConnectionAvailableException ec) {
        log4j.error("Connection error in query: " + strSql + "Exception:" + ec);
        throw new ServletException("@CODE=NoConnectionAvailable");
      } catch (PoolNotFoundException ep) {
        log4j.error("Pool error in query: " + strSql + "Exception:" + ep);
        throw new ServletException("@CODE=NoConnectionAvailable");
      } catch (Exception ex) {
        log4j.error("Exception in query: " + strSql + "Exception:" + ex);
        throw new ServletException("@CODE=@" + ex.getMessage());
      }
    }
    return (razon);
  }
}
