//Sqlc generated V1.O00-1
package pe.com.unifiedgo.sales.ad_actionButton;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.data.UtilSql;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.database.SessionInfo;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.currency.ConversionRate;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.service.db.QueryTimeOutUtil;

import pe.com.unifiedgo.accounting.SCO_Utils;

class GenerateFreeGLItemData implements FieldProvider {
  static Logger log4j = Logger.getLogger(GenerateFreeGLItemData.class);
  private String InitRecordNumber = "0";

  public String INVDOCNO;
  public String INVPARTNER;
  public String INVDATEORDER;
  public String INVTOTALAMOUNT;
  public String INVCURRENCYNAME;

  public String rownum;


  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  public String getField(String fieldName) {
    if (fieldName.equals("INVDOCNO"))
      return INVDOCNO;
    else if (fieldName.equals("INVPARTNER"))
      return INVPARTNER;
    else if (fieldName.equals("INVDATEORDER"))
      return INVDATEORDER;
    else if (fieldName.equals("INVTOTALAMOUNT"))
      return INVTOTALAMOUNT;
    else if (fieldName.equals("INVCURRENCYNAME"))
      return INVCURRENCYNAME;
    else if (fieldName.equals("rownum"))
      return rownum;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  public static GenerateFreeGLItemData[] selectInvoiceHeader(ConnectionProvider connectionProvider,
      String purchaseOrderImportID) throws ServletException {
    String strSql = "";
    strSql = strSql
        + "   select c_invoice.documentno AS documentno,"
        + "          c_bpartner.name AS c_bpartner,"
        + "          c_invoice.dateinvoiced AS dateordered, c_invoice.TotalLines as totalamount, c_currency.iso_code as currname"
        + "     from c_invoice "
        + "          inner join c_bpartner on c_bpartner.c_bpartner_id = c_invoice.c_bpartner_id "
        + "          inner join c_currency on c_currency.c_currency_id = c_invoice.c_currency_id "
        + "      where c_invoice_id = ? ";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, purchaseOrderImportID);
      result = st.executeQuery();
      long countRecord = 0;
      boolean continueResult = true;

      while (continueResult && result.next()) {
        countRecord++;
        GenerateFreeGLItemData objectGenerateFreeGLItemData = new GenerateFreeGLItemData();

        objectGenerateFreeGLItemData.INVDOCNO = UtilSql.getValue(result, "documentno");
        objectGenerateFreeGLItemData.INVPARTNER = UtilSql.getValue(result, "c_bpartner");
        objectGenerateFreeGLItemData.INVDATEORDER = UtilSql.getDateValue(result, "dateordered",
            "dd-MM-yyyy");
        objectGenerateFreeGLItemData.INVTOTALAMOUNT = UtilSql.getValue(result, "totalamount");
        objectGenerateFreeGLItemData.INVCURRENCYNAME = UtilSql.getValue(result, "currname");

        vector.addElement(objectGenerateFreeGLItemData);
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
    GenerateFreeGLItemData objectGenerateFreeGLItemData[] = new GenerateFreeGLItemData[vector
        .size()];
    vector.copyInto(objectGenerateFreeGLItemData);
    return (objectGenerateFreeGLItemData);
  }

  public static int removeFreeGLItemLine(ConnectionProvider connectionProvider, String ad_client_id, String c_invoiceId)
      throws ServletException {
    String strSql = "";
    strSql = strSql + "      DELETE FROM C_INVOICELINE " + "      WHERE C_INVOICE_ID=? AND ACCOUNT_ID=(SELECT i.c_glitem_id FROM c_glitem i WHERE i.em_sco_specialglitem='SCOFREEIGV' AND i.ad_client_id=? LIMIT 1)";
    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, c_invoiceId);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, ad_client_id);
      updateCount = st.executeUpdate();
    } catch (SQLException e) {
      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
          + e.getMessage());
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releaseTransactionalPreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }
    return (updateCount);
  }

  public static int updateInvoiceForFree(ConnectionProvider connectionProvider,
      String c_invoiceId) throws ServletException {
    String strSql = "";
    strSql = strSql + "      UPDATE C_INVOICE " + "      SET em_sco_isforfree = 'N'"
        + "      WHERE C_INVOICE_ID=?";
    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, c_invoiceId);
      updateCount = st.executeUpdate();
    } catch (SQLException e) {
      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
          + e.getMessage());
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releaseTransactionalPreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }
    return (updateCount);
  }

  public static int updateAnticipo(ConnectionProvider connectionProvider, String invoiceline_id,
      String amount) throws ServletException {
    String strSql = "";
    strSql = strSql + "      UPDATE C_INVOICELINE " + "      set em_sco_creditused = ?"
        + "      WHERE C_INVOICELINE_ID=?";
    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, amount);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, invoiceline_id);
      updateCount = st.executeUpdate();
    } catch (SQLException e) {
      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
          + e.getMessage());
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releaseTransactionalPreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }
    return (updateCount);
  }

  public static GenerateFreeGLItemData[] set() throws ServletException {
    GenerateFreeGLItemData objectGenerateFreeGLItemData[] = new GenerateFreeGLItemData[1];
    objectGenerateFreeGLItemData[0] = new GenerateFreeGLItemData();

    objectGenerateFreeGLItemData[0].INVDOCNO = "";
    objectGenerateFreeGLItemData[0].INVPARTNER = "";
    objectGenerateFreeGLItemData[0].INVDATEORDER = "";
    objectGenerateFreeGLItemData[0].INVTOTALAMOUNT = "";
    objectGenerateFreeGLItemData[0].INVCURRENCYNAME = "";

    return objectGenerateFreeGLItemData;
  }

  public static BigDecimal findExchangeRate(Currency currency_from, Currency currency_to,
      Date date, Organization organization, int conversionRatePrecision) {

    BigDecimal exchangeRate = BigDecimal.ONE;

    final ConversionRate conversionRate = FIN_Utility.getConversionRate(currency_from, currency_to,
        date, organization);
    if (conversionRate == null) {
      exchangeRate = null;
    } else {
      exchangeRate = conversionRate.getMultipleRateBy().setScale(conversionRatePrecision,
          RoundingMode.HALF_UP);
    }

    return exchangeRate;
  }

  public static BigDecimal findExchangeRate(String currency_from, String currency_to, Date date,
      String ad_org_id, int conversionRatePrecision) {

    BigDecimal exchangeRate = BigDecimal.ONE;

    Currency curr_from = OBDal.getInstance().get(Currency.class, currency_from);
    Currency curr_to = OBDal.getInstance().get(Currency.class, currency_to);
    Organization org = OBDal.getInstance().get(Organization.class, ad_org_id);

    final ConversionRate conversionRate = FIN_Utility.getConversionRate(curr_from, curr_to, date,
        org);
    if (conversionRate == null) {
      exchangeRate = null;
    } else {
      exchangeRate = conversionRate.getMultipleRateBy().setScale(conversionRatePrecision,
          RoundingMode.HALF_UP);
    }

    return exchangeRate;
  }

}