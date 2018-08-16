//Sqlc generated V1.O00-1
package pe.com.unifiedgo.report.ad_reports;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.data.FieldProvider;
import org.openbravo.data.UtilSql;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.database.SessionInfo;
import org.openbravo.service.db.QueryTimeOutUtil;

class ReporteClientesInfocorpVencidoData implements FieldProvider {
  static Logger log4j = Logger.getLogger(ReporteClientesInfocorpVencidoData.class);
  private String InitRecordNumber = "0";
  public String bpartnerid;
  public String bpartner;
  public String organizacion;
  public String fecha;
  public String fechalimite;
  public String vctoinfocorp;

  public String orgpadreid;
  public String orgpadre;
  public String orgpadreruc;

  public String rownum;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  @Override
  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("bpartnerid"))
      return bpartnerid;
    else if (fieldName.equalsIgnoreCase("bpartner"))
      return bpartner;
    else if (fieldName.equalsIgnoreCase("organizacion"))
      return organizacion;
    else if (fieldName.equalsIgnoreCase("fecha"))
      return fecha;
    else if (fieldName.equalsIgnoreCase("fechalimite"))
      return fechalimite;
    else if (fieldName.equalsIgnoreCase("vctoinfocorp"))
      return vctoinfocorp;

    else if (fieldName.equalsIgnoreCase("orgpadreid"))
      return orgpadreid;
    else if (fieldName.equalsIgnoreCase("orgpadre"))
      return orgpadre;
    else if (fieldName.equalsIgnoreCase("orgpadreruc"))
      return orgpadreruc;

    else if (fieldName.equals("rownum"))
      return rownum;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  public static ReporteClientesInfocorpVencidoData[] select(ConnectionProvider connectionProvider,
      String adClientId, String adOrgId, String startDate, String endDate, String cBParnterId)
      throws ServletException {
    return select(connectionProvider, adClientId, adOrgId, startDate, endDate, cBParnterId, 0, 0);
  }

  public static ReporteClientesInfocorpVencidoData[] select(ConnectionProvider connectionProvider,
      String adClientId, String adOrgId, String startDate, String endDate, String cBParnterId,
      int firstRegister, int numberRegisters) throws ServletException {

    String strSql = "";
    strSql = "select bp.c_bpartner_id as bpartnerid, "
        + "bp.taxid||' - '||bp.name as bpartner, "
        + "(select name from ad_org where ad_org_id = bp.ad_org_id) as organizacion, "
        + "to_char((select documentdate from scr_bp_credit_document  "
        + "where c_bpartner_id = bp.c_bpartner_id and documenttype = 'SCR_Infocorp' "
        + "order by documentdate desc limit 1))as fecha, "
        + "to_char((select documentdate + cast('3 month' as interval) from scr_bp_credit_document  "
        + "where c_bpartner_id = bp.c_bpartner_id and documenttype = 'SCR_Infocorp' "
        + "order by documentdate desc limit 1))as fechalimite, "
        + "to_char(bp.em_scr_dateinfocorpdue) as vctoinfocorp " + "from c_bpartner bp "
        + "where ad_isorgincluded(bp.ad_org_id,?,?)<>-1 ";

    if (startDate != null && startDate != "") {
      strSql = strSql + "and bp.em_scr_dateinfocorpdue >= to_date(?) ";
    }

    strSql = strSql + "and bp.em_scr_dateinfocorpdue  <= to_date(?) "
        + "order by bp.name ";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;

    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adOrgId);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adClientId);

      if (startDate != null && startDate != "") {
        iParameter++;
        UtilSql.setValue(st, iParameter, 12, null, startDate);
      }

      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, endDate);
      result = st.executeQuery();
      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while (countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }
      while (continueResult && result.next()) {
        countRecord++;
        ReporteClientesInfocorpVencidoData objectReporteClientesInfocorpVencidoData = new ReporteClientesInfocorpVencidoData();
        objectReporteClientesInfocorpVencidoData.bpartnerid = UtilSql
            .getValue(result, "bpartnerid");
        objectReporteClientesInfocorpVencidoData.bpartner = UtilSql.getValue(result, "bpartner");
        objectReporteClientesInfocorpVencidoData.organizacion = UtilSql.getValue(result,
            "organizacion");
        objectReporteClientesInfocorpVencidoData.fecha = UtilSql.getValue(result, "fecha");
        objectReporteClientesInfocorpVencidoData.fechalimite = UtilSql.getValue(result,
            "fechalimite");
        objectReporteClientesInfocorpVencidoData.vctoinfocorp = UtilSql.getValue(result,
            "vctoinfocorp");
        objectReporteClientesInfocorpVencidoData.rownum = Long.toString(countRecord);
        objectReporteClientesInfocorpVencidoData.InitRecordNumber = Integer.toString(firstRegister);

        vector.addElement(objectReporteClientesInfocorpVencidoData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }
      result.close();
    } catch (SQLException e) {
      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
          + e.getMessage());
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }

    ReporteClientesInfocorpVencidoData objectReporteClientesInfocorpVencidoData[] = new ReporteClientesInfocorpVencidoData[vector
        .size()];
    vector.copyInto(objectReporteClientesInfocorpVencidoData);

    return (objectReporteClientesInfocorpVencidoData);
  }

  public static ReporteClientesInfocorpVencidoData[] selectOrg(
      ConnectionProvider connectionProvider, String adClientId, String adOrgId)
      throws ServletException {
    return selectOrg(connectionProvider, adClientId, adOrgId, 0, 0);
  }

  public static ReporteClientesInfocorpVencidoData[] selectOrg(
      ConnectionProvider connectionProvider, String adClientId, String adOrgId, int firstRegister,
      int numberRegisters) throws ServletException {

    String strSql = "";
    strSql = strSql + "select org.ad_org_id as orgpadreid, org.name as orgpadre, "
        + "inf.taxid as orgpadreruc from ad_org org "
        + "join ad_orgtype typ using (ad_orgtype_id) "
        + "join ad_orginfo inf on org.ad_org_id = inf.ad_org_id "
        + "where ad_isorgincluded(?,org.ad_org_id,org.ad_client_id)<>-1 "
        + "and(typ.islegalentity='Y' or typ.isacctlegalentity='Y') ";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;

    try {

      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());

      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adOrgId);

      result = st.executeQuery();
      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while (countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }

      while (continueResult && result.next()) {
        countRecord++;
        ReporteClientesInfocorpVencidoData objectReporteClientesInfocorpVencidoData = new ReporteClientesInfocorpVencidoData();

        objectReporteClientesInfocorpVencidoData.orgpadreid = UtilSql
            .getValue(result, "orgpadreid");
        objectReporteClientesInfocorpVencidoData.orgpadre = UtilSql.getValue(result, "orgpadre");
        objectReporteClientesInfocorpVencidoData.orgpadreruc = UtilSql.getValue(result,
            "orgpadreruc");
        objectReporteClientesInfocorpVencidoData.rownum = Long.toString(countRecord);
        objectReporteClientesInfocorpVencidoData.InitRecordNumber = Integer.toString(firstRegister);

        vector.addElement(objectReporteClientesInfocorpVencidoData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }

    } catch (SQLException e) {
      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
          + e.getMessage());
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }

    ReporteClientesInfocorpVencidoData objectReporteClientesInfocorpVencidoData[] = new ReporteClientesInfocorpVencidoData[vector
        .size()];
    vector.copyInto(objectReporteClientesInfocorpVencidoData);
    return (objectReporteClientesInfocorpVencidoData);
  }

  public static ReporteClientesInfocorpVencidoData[] set() throws ServletException {
    ReporteClientesInfocorpVencidoData objectReporteClientesInfocorpVencidoData[] = new ReporteClientesInfocorpVencidoData[1];
    objectReporteClientesInfocorpVencidoData[0] = new ReporteClientesInfocorpVencidoData();
    objectReporteClientesInfocorpVencidoData[0].bpartnerid = "";
    objectReporteClientesInfocorpVencidoData[0].bpartner = "";
    objectReporteClientesInfocorpVencidoData[0].organizacion = "";
    objectReporteClientesInfocorpVencidoData[0].fecha = "";
    objectReporteClientesInfocorpVencidoData[0].fechalimite = "";
    objectReporteClientesInfocorpVencidoData[0].vctoinfocorp = "";

    objectReporteClientesInfocorpVencidoData[0].orgpadreid = "";
    objectReporteClientesInfocorpVencidoData[0].orgpadre = "";
    objectReporteClientesInfocorpVencidoData[0].orgpadreruc = "";

    return objectReporteClientesInfocorpVencidoData;
  }

}