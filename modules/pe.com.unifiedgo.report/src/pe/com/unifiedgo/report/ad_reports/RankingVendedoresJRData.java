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

class RankingVendedoresJRData implements FieldProvider {
  static Logger log4j = Logger.getLogger(RankingVendedoresJRData.class);
  private String InitRecordNumber = "0";
  public String orgid;
  public String salesrepid;
  public String salesrepname;
  public String salesrepvalue;
  public String totalpenlinenetamt;
  public String totalusdlinenetamt;
  
  public String dateFrom;
  public String dateTo;  
  
  public String rownum;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  @Override
  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("orgid"))
      return orgid;
    else if (fieldName.equalsIgnoreCase("salesrepid"))
      return salesrepid;
    else if (fieldName.equalsIgnoreCase("salesrepname"))
      return salesrepname;
    else if (fieldName.equalsIgnoreCase("salesrepvalue"))
        return salesrepvalue;    
    else if (fieldName.equalsIgnoreCase("totalpenlinenetamt"))
      return totalpenlinenetamt;
      else if (fieldName.equalsIgnoreCase("totalusdlinenetamt"))
        return totalusdlinenetamt;    
      else if (fieldName.equalsIgnoreCase("dateFrom"))
          return dateFrom;
        else if (fieldName.equalsIgnoreCase("dateTo"))
          return dateTo;    
    else if (fieldName.equals("rownum"))
      return rownum;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  public static RankingVendedoresJRData[] select(VariablesSecureApp vars, String adOrgId,
      String adClientId, String startDate, String endDate, String adUserId)
      throws ServletException {
    return select(vars, adOrgId, adClientId, startDate, endDate, adUserId, 0, 0);
  }

  public static RankingVendedoresJRData[] select(VariablesSecureApp vars, String adOrgId,
      String adClientId, String startDate, String endDate, String adUserId,
      int firstRegister, int numberRegisters) throws ServletException {

    String strSql = "";
    strSql = "SELECT i.salesrep_id as salesrep_id, "
            + "                (select u.name from ad_user u where ad_user_id=i.salesrep_id) as salesrep_name, "
            + "                sum(CASE WHEN i.c_currency_id='308' THEN (case when i.em_sco_specialdoctype='SCOARCREDITMEMO' then -il.linenetamt else il.linenetamt end) ELSE 0 END) AS totalPENlinenetamt, "
            + "                sum(CASE WHEN i.c_currency_id='100' THEN (case when i.em_sco_specialdoctype='SCOARCREDITMEMO' then -il.linenetamt else il.linenetamt end) ELSE 0 END) AS totalUSDlinenetamt, "
            + "                (select bp.value from c_bpartner bp where c_bpartner_id=(select u.ad_user_id from ad_user u where u.ad_user_id=i.salesrep_id)) as salesrep_value "
            + "             FROM c_invoice i INNER JOIN c_invoiceline il ON i.c_invoice_id=il.c_invoice_id"
            + "            WHERE i.em_sco_specialdoctype IN ('SCOARINVOICE','SCOARTICKET','SCOARCREDITMEMO','SCOARINVOICERETURNMAT')"
            + "              AND i.issotrx='Y'"
            + "              AND il.financial_invoice_line='N' AND i.docstatus='CO' "
            + "              AND i.em_sco_isforfree='N' "
            + "              AND (i.c_currency_id='308' OR i.c_currency_id='100')"
            + "              AND TRUNC(i.dateacct) BETWEEN TO_DATE('"
            + startDate
            + "', 'DD-MM-YYYY') and TO_DATE('"
            + endDate
            + "', 'DD-MM-YYYY') "
            + "              AND AD_ISORGINCLUDED(i.ad_org_id,'"
            + adOrgId
            + "', '"
            + adClientId
            + "') <> -1"
            + "  GROUP BY salesrep_id, salesrep_name"
            + "  ORDER BY totalPENlinenetamt DESC, totalUSDlinenetamt DESC";
    System.out.println("STRSQL:" + strSql);
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);

    DecimalFormat dfPrice = Utility.getFormat(vars, "priceInform");
    dfPrice.setRoundingMode(RoundingMode.HALF_UP);

    DecimalFormat dfQty = Utility.getFormat(vars, "qtyExcel");
    dfQty.setRoundingMode(RoundingMode.HALF_UP);
    
    BigDecimal sum_totalpenlinenetamt = BigDecimal.ZERO, sum_totalusdlinenetamt = BigDecimal.ZERO;

    long countRecord = 0;
    try {
      Query sqlQuery = OBDal.getInstance().getSession().createSQLQuery(strSql);

      List<Object> data = sqlQuery.list();
      for (int k = 0; k < data.size(); k++) {
        Object[] obj = (Object[]) data.get(k);
        countRecord++;

        RankingVendedoresJRData objectRankingVendedoresJRData = new RankingVendedoresJRData();
        objectRankingVendedoresJRData.orgid = adOrgId;

        objectRankingVendedoresJRData.salesrepid = (obj[0] != null) ? (String) obj[0] : "--";
        objectRankingVendedoresJRData.salesrepname = (obj[1] != null) ? (String) obj[1]
            : "Desconocido";              

        objectRankingVendedoresJRData.totalpenlinenetamt = dfPrice.format(obj[2]);
        objectRankingVendedoresJRData.totalusdlinenetamt = dfPrice.format(obj[3]);
        
        objectRankingVendedoresJRData.salesrepvalue = (obj[4] != null) ? (String) obj[4]
                : "--";
        
        objectRankingVendedoresJRData.dateFrom = startDate;
        objectRankingVendedoresJRData.dateTo = endDate;

        
        if (obj[2]!= null) {
             sum_totalpenlinenetamt = sum_totalpenlinenetamt.add((BigDecimal)obj[2]);
        }
        if (obj[3]!= null) {
             sum_totalusdlinenetamt = sum_totalusdlinenetamt.add((BigDecimal)obj[3]);
        }
        
        objectRankingVendedoresJRData.rownum = Long.toString(countRecord);
        objectRankingVendedoresJRData.InitRecordNumber = Integer.toString(firstRegister);

        vector.addElement(objectRankingVendedoresJRData);
      }
      
      if (vector.size()>0) {
    	   countRecord++;
    	  
           RankingVendedoresJRData objectRankingVendedoresJRData = new RankingVendedoresJRData();
           objectRankingVendedoresJRData.orgid = adOrgId;
           objectRankingVendedoresJRData.salesrepid = "--";
           objectRankingVendedoresJRData.salesrepname = "TOTAL";
           objectRankingVendedoresJRData.totalpenlinenetamt = dfPrice.format(sum_totalpenlinenetamt);
           objectRankingVendedoresJRData.totalusdlinenetamt = dfPrice.format(sum_totalusdlinenetamt);      
           objectRankingVendedoresJRData.salesrepvalue = "--";
           objectRankingVendedoresJRData.dateFrom = "--";
           objectRankingVendedoresJRData.dateTo = "--";
      
           objectRankingVendedoresJRData.rownum = Long.toString(countRecord);
           objectRankingVendedoresJRData.InitRecordNumber = Integer.toString(firstRegister);
           vector.addElement(objectRankingVendedoresJRData);
      }
      
      
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    }

    RankingVendedoresJRData objectRankingVendedoresJRData[] = new RankingVendedoresJRData[vector
        .size()];
    vector.copyInto(objectRankingVendedoresJRData);

    return (objectRankingVendedoresJRData);
  }

  public static RankingVendedoresJRData[] set() throws ServletException {
    RankingVendedoresJRData objectRankingVendedoresJRData[] = new RankingVendedoresJRData[1];
    objectRankingVendedoresJRData[0] = new RankingVendedoresJRData();
    objectRankingVendedoresJRData[0].orgid = "";
    objectRankingVendedoresJRData[0].salesrepid = "";
    objectRankingVendedoresJRData[0].salesrepname = "";
    objectRankingVendedoresJRData[0].salesrepvalue = "";
    objectRankingVendedoresJRData[0].totalpenlinenetamt = "0";
    objectRankingVendedoresJRData[0].totalusdlinenetamt = "0";    
    objectRankingVendedoresJRData[0].dateFrom = "";
    objectRankingVendedoresJRData[0].dateTo = "";    
    return objectRankingVendedoresJRData;
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

}