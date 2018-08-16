//Sqlc generated V1.O00-1
package pe.com.unifiedgo.report.ad_reports;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.data.UtilSql;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.database.SessionInfo;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.service.db.QueryTimeOutUtil;

class RankingClientesJRData implements FieldProvider {
  static Logger log4j = Logger.getLogger(RankingClientesJRData.class);
  private String InitRecordNumber = "0";
  public String orgid;
  public String bpid;
  public String bpname;
  public String totalpenqtyinvoiced;
  public String totalpenlinenetamt;
  public String totalusdqtyinvoiced;
  public String totalusdlinenetamt;

  public String codcliente;
  public String descliente;

  public String rownum;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  @Override
  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("orgid"))
      return orgid;
    else if (fieldName.equalsIgnoreCase("bpid"))
      return bpid;
    else if (fieldName.equalsIgnoreCase("bpname"))
      return bpname;
    else if (fieldName.equalsIgnoreCase("totalpenqtyinvoiced"))
      return totalpenqtyinvoiced;
    else if (fieldName.equalsIgnoreCase("totalpenlinenetamt"))
      return totalpenlinenetamt;
    else if (fieldName.equalsIgnoreCase("totalusdqtyinvoiced"))
      return totalusdqtyinvoiced;
    else if (fieldName.equalsIgnoreCase("totalusdlinenetamt"))
      return totalusdlinenetamt;
    else if (fieldName.equalsIgnoreCase("codcliente"))
      return codcliente;
    else if (fieldName.equalsIgnoreCase("descliente"))
      return descliente;
    else if (fieldName.equals("rownum"))
      return rownum;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  public static RankingClientesJRData[] select(VariablesSecureApp vars, String adOrgId,
      String adClientId, String startDate, String endDate, String adUserId, String strSalesRepId)
      throws ServletException {
    return select(vars, adOrgId, adClientId, startDate, endDate, adUserId, strSalesRepId, 0, 0);
  }

  public static RankingClientesJRData[] select(VariablesSecureApp vars, String adOrgId,
      String adClientId, String startDate, String endDate, String adUserId, String strSalesRepId,
      int firstRegister, int numberRegisters) throws ServletException {

    String strSql = "";
    strSql = "SELECT i.c_bpartner_id as bp_id, "
        + "                (select (bp.taxid||' - '||bp.name) from c_bpartner bp where bp.c_bpartner_id=i.c_bpartner_id) as bp_name, "

        + "                sum(CASE WHEN i.c_currency_id='308' THEN il.qtyinvoiced ELSE 0 END) AS totalpenqtyinvoiced,   "
        + "                sum(CASE WHEN i.c_currency_id='308' THEN (case when i.em_sco_specialdoctype='SCOARCREDITMEMO' then -il.linenetamt else il.linenetamt end) ELSE 0 END) AS totalPENlinenetamt,  "
        + "                sum(CASE WHEN i.c_currency_id='100' THEN il.qtyinvoiced ELSE 0 END) AS totalusdqtyinvoiced,   "
        + "                sum(CASE WHEN i.c_currency_id='100' THEN (case when i.em_sco_specialdoctype='SCOARCREDITMEMO' then -il.linenetamt else il.linenetamt end) ELSE 0 END) AS totalUSDlinenetamt,  "
        + "                (select (bp.taxid) from c_bpartner bp where bp.c_bpartner_id=i.c_bpartner_id) as codcliente, "
        + "                (select (bp.name) from c_bpartner bp where bp.c_bpartner_id=i.c_bpartner_id) as descliente "
        + "    FROM c_invoice i "
        + "               INNER JOIN c_invoiceline il ON i.c_invoice_id=il.c_invoice_id  "
        + "  WHERE i.em_sco_specialdoctype IN ('SCOARINVOICE','SCOARTICKET','SCOARCREDITMEMO','SCOARINVOICERETURNMAT')"
        + "      AND i.issotrx='Y'" + "              AND i.docstatus='CO' "
        + "      AND il.financial_invoice_line='N' " + "              AND i.em_sco_isforfree='N' "
        + "      AND (i.c_currency_id='308' OR i.c_currency_id='100')"
        + "      AND TRUNC(i.dateacct) BETWEEN TO_DATE('" + startDate
        + "', 'DD-MM-YYYY') and TO_DATE('" + endDate + "', 'DD-MM-YYYY') ";
    strSql = strSql
        + (("".equals(strSalesRepId.trim()) || strSalesRepId == null) ? "" : " AND i.SalesRep_ID='"
            + strSalesRepId + "'");
    strSql = strSql
        + "       AND AD_ISORGINCLUDED(i.ad_org_id,'"
        + adOrgId
        + "', '"
        + adClientId
        + "') <> -1"
        + "  GROUP BY bp_id, bp_name";
    strSql = strSql
        + (("".equals(strSalesRepId.trim()) || strSalesRepId == null) ? "" : ", i.salesrep_id");
    strSql = strSql + "  ORDER BY totalPENlinenetamt DESC, totalUSDlinenetamt DESC";
    System.out.println("Sql:" + strSql);
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);

    DecimalFormat dfPrice = Utility.getFormat(vars, "priceInform");
    dfPrice.setRoundingMode(RoundingMode.HALF_UP);

    DecimalFormat dfQty = Utility.getFormat(vars, "qtyExcel");
    dfQty.setRoundingMode(RoundingMode.HALF_UP);

    BigDecimal sum_totalpenqtyinvoiced = BigDecimal.ZERO, sum_totalpenlinenetamt = BigDecimal.ZERO, sum_totalusdqtyinvoiced = BigDecimal.ZERO, sum_totalusdlinenetamt = BigDecimal.ZERO;

    long countRecord = 0;
    try {
      Query sqlQuery = OBDal.getInstance().getSession().createSQLQuery(strSql);

      List<Object> data = sqlQuery.list();
      for (int k = 0; k < data.size(); k++) {
        Object[] obj = (Object[]) data.get(k);
        countRecord++;

        RankingClientesJRData objectRankingClientesJRData = new RankingClientesJRData();
        objectRankingClientesJRData.orgid = adOrgId;

        objectRankingClientesJRData.bpid = (String) obj[0];
        objectRankingClientesJRData.bpname = (String) obj[1];

        objectRankingClientesJRData.totalpenqtyinvoiced = dfQty.format(obj[2]);
        objectRankingClientesJRData.totalpenlinenetamt = dfPrice.format(obj[3]);

        objectRankingClientesJRData.totalusdqtyinvoiced = dfQty.format(obj[4]);
        objectRankingClientesJRData.totalusdlinenetamt = dfPrice.format(obj[5]);

        objectRankingClientesJRData.codcliente = (String) (obj[6]);
        objectRankingClientesJRData.descliente = (String) (obj[7]);

        sum_totalpenqtyinvoiced = sum_totalpenqtyinvoiced.add((BigDecimal) obj[2]);
        sum_totalpenlinenetamt = sum_totalpenlinenetamt.add((BigDecimal) obj[3]);

        sum_totalusdqtyinvoiced = sum_totalusdqtyinvoiced.add((BigDecimal) obj[4]);
        sum_totalusdlinenetamt = sum_totalusdlinenetamt.add((BigDecimal) obj[5]);

        objectRankingClientesJRData.rownum = Long.toString(countRecord);
        objectRankingClientesJRData.InitRecordNumber = Integer.toString(firstRegister);

        vector.addElement(objectRankingClientesJRData);
      }
      System.out.println("sdata.size():" + data.size() + " - vector.size():" + vector.size());
      System.out.println("sum_totalpenlinenetamt:" + sum_totalpenlinenetamt);
      if (vector.size() > 0) {
        countRecord++;

        RankingClientesJRData objectRankingClientesJRData = new RankingClientesJRData();
        objectRankingClientesJRData.orgid = adOrgId;

        objectRankingClientesJRData.bpid = "--";
        objectRankingClientesJRData.bpname = "TOTAL";
        objectRankingClientesJRData.totalpenqtyinvoiced = dfQty.format(sum_totalpenqtyinvoiced);
        objectRankingClientesJRData.totalpenlinenetamt = dfPrice.format(sum_totalpenlinenetamt);
        objectRankingClientesJRData.totalusdqtyinvoiced = dfQty.format(sum_totalusdqtyinvoiced);
        objectRankingClientesJRData.totalusdlinenetamt = dfPrice.format(sum_totalusdlinenetamt);

        objectRankingClientesJRData.rownum = Long.toString(countRecord);
        objectRankingClientesJRData.InitRecordNumber = Integer.toString(firstRegister);

        vector.addElement(objectRankingClientesJRData);
      }

    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    }

    RankingClientesJRData objectRankingClientesJRData[] = new RankingClientesJRData[vector.size()];
    vector.copyInto(objectRankingClientesJRData);

    return (objectRankingClientesJRData);
  }

  public static RankingClientesJRData[] set() throws ServletException {
    RankingClientesJRData objectRankingClientesJRData[] = new RankingClientesJRData[1];
    objectRankingClientesJRData[0] = new RankingClientesJRData();
    objectRankingClientesJRData[0].orgid = "";
    objectRankingClientesJRData[0].bpid = "";
    objectRankingClientesJRData[0].bpname = "";
    objectRankingClientesJRData[0].totalpenqtyinvoiced = "0";
    objectRankingClientesJRData[0].totalpenlinenetamt = "0";
    objectRankingClientesJRData[0].totalusdqtyinvoiced = "0";
    objectRankingClientesJRData[0].totalusdlinenetamt = "0";
    return objectRankingClientesJRData;
  }

  public static String selectOrganization(ConnectionProvider connectionProvider,
      String organizacion_id) throws ServletException {
    String strSql = "";
    strSql = strSql + " select name from ad_org where ad_org_id = ? ";

    ResultSet result;
    String strReturn = "0";
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, organizacion_id);

      result = st.executeQuery();
      if (result.next()) {
        strReturn = UtilSql.getValue(result, "name");
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
    return (strReturn);
  }

  public static String selectCurrency(ConnectionProvider connectionProvider, String currency_id)
      throws ServletException {
    String strSql = "";
    strSql = strSql + " select iso_code from c_currency where c_currency_id = ? ";

    ResultSet result;
    String strReturn = "0";
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, currency_id);

      result = st.executeQuery();
      if (result.next()) {
        strReturn = UtilSql.getValue(result, "iso_code");
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
    return (strReturn);
  }

  public static String selectSalesRepresentative(ConnectionProvider connectionProvider,
      String strSalesRepId) throws ServletException {
    String strSql = "";
    strSql = strSql + "      SELECT name" + "      FROM AD_User" + "      WHERE AD_User_ID = ?";

    ResultSet result;
    String strReturn = "";
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, strSalesRepId);

      result = st.executeQuery();
      if (result.next()) {
        strReturn = UtilSql.getValue(result, "name");
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
    return (strReturn);
  }

}