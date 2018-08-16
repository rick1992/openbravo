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

class CreateFromRetainerData implements FieldProvider {
  static Logger log4j = Logger.getLogger(CreateFromRetainerData.class);
  private String InitRecordNumber = "0";

  public String INVDOCNO;
  public String INVPARTNER;
  public String INVDATEORDER;
  public String INVTOTALAMOUNT;
  public String INVCURRENCYNAME;

  public String rownum;

  public String PREPAYLINE;
  public String PREPAYGLITEMNAME;
  public String PREPAYPHYDOC;
  public String PREPAYAMT;
  public String PREPAYTAX;
  public String PREPAYCREDITAVA;
  public String PREPAYCURRENCY;
  public String PREPAYDATEACCT;
  public String PREPAYAMTTOAPPLY;
  public String PREPAYEXCHANGERATE;
  public String PREPAYALTEXCHANGERATE;
  public String PREPAYEXCHANGERATEOP;

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
    else if (fieldName.equals("PREPAYCREDITAVA"))
      return PREPAYCREDITAVA;
    else if (fieldName.equals("PREPAYTAX"))
      return PREPAYTAX;
    else if (fieldName.equals("PREPAYAMT"))
      return PREPAYAMT;
    else if (fieldName.equals("PREPAYPHYDOC"))
      return PREPAYPHYDOC;
    else if (fieldName.equals("PREPAYGLITEMNAME"))
      return PREPAYGLITEMNAME;
    else if (fieldName.equals("PREPAYCURRENCY"))
      return PREPAYCURRENCY;
    else if (fieldName.equals("PREPAYDATEACCT"))
      return PREPAYDATEACCT;
    else if (fieldName.equals("PREPAYAMTTOAPPLY"))
      return PREPAYAMTTOAPPLY;
    else if (fieldName.equals("PREPAYEXCHANGERATE"))
      return PREPAYEXCHANGERATE;
    else if (fieldName.equals("PREPAYALTEXCHANGERATE"))
      return PREPAYALTEXCHANGERATE;
    else if (fieldName.equals("PREPAYEXCHANGERATEOP")) {
      return PREPAYEXCHANGERATEOP;
    } else if (fieldName.equals("PREPAYLINE"))
      return PREPAYLINE;
    else if (fieldName.equals("rownum"))
      return rownum;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  public static CreateFromRetainerData[] selectInvoiceHeader(ConnectionProvider connectionProvider,
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
        CreateFromRetainerData objectCreateFromRetainerData = new CreateFromRetainerData();

        objectCreateFromRetainerData.INVDOCNO = UtilSql.getValue(result, "documentno");
        objectCreateFromRetainerData.INVPARTNER = UtilSql.getValue(result, "c_bpartner");
        objectCreateFromRetainerData.INVDATEORDER = UtilSql.getDateValue(result, "dateordered",
            "dd-MM-yyyy");
        objectCreateFromRetainerData.INVTOTALAMOUNT = UtilSql.getValue(result, "totalamount");
        objectCreateFromRetainerData.INVCURRENCYNAME = UtilSql.getValue(result, "currname");

        vector.addElement(objectCreateFromRetainerData);
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
    CreateFromRetainerData objectCreateFromRetainerData[] = new CreateFromRetainerData[vector
        .size()];
    vector.copyInto(objectCreateFromRetainerData);
    return (objectCreateFromRetainerData);
  }

  public static int removeAnticipo(ConnectionProvider connectionProvider, String c_invoiceId)
      throws ServletException {
    String strSql = "";
    strSql = strSql + "      DELETE FROM C_INVOICELINE " + "      WHERE C_INVOICELINE_ID=?"
        + "      AND em_ssa_isprepayment_inline = 'N'";
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

  public static int changeStateBeforeRemoveAnticipo(ConnectionProvider connectionProvider,
      String c_invoiceId) throws ServletException {
    String strSql = "";
    strSql = strSql + "      UPDATE C_INVOICELINE " + "      SET em_ssa_isprepayment_inline = 'N'"
        + "      WHERE C_INVOICELINE_ID=?";
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

  public static CreateFromRetainerData[] selectPrepaymentLines(
      ConnectionProvider connectionProvider, String Client_Id, String Org_Id, String BPartner_id,
      String Invoice_Id, int conversionRatePrecision, DecimalFormat PriceFormat,
      SimpleDateFormat DateFormat) throws ServletException {
    String strSql = "";
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    try {

      Invoice invoice = OBDal.getInstance().get(Invoice.class, Invoice_Id);

      OBCriteria<Invoice> prepayments = OBDal.getInstance().createCriteria(Invoice.class);
      prepayments.add(Restrictions.eq(Invoice.PROPERTY_SCOSPECIALDOCTYPE, "SCOARINVOICE"));
      prepayments.add(Restrictions.eq(Invoice.PROPERTY_BUSINESSPARTNER,
          OBDal.getInstance().get(BusinessPartner.class, BPartner_id)));
      prepayments.add(Restrictions.eq(Invoice.PROPERTY_DOCUMENTSTATUS, "CO"));
      prepayments.add(Restrictions.eq(Invoice.PROPERTY_SSAISPREPAYMENT, true));
      prepayments.add(Restrictions.eq(Invoice.PROPERTY_ACTIVE, true));
      prepayments.add(Restrictions.eq(Invoice.PROPERTY_CLIENT,
          OBDal.getInstance().get(Client.class, Client_Id)));
      prepayments.add(Restrictions.sqlRestriction("AD_ISORGINCLUDED( ad_org_id, " + "'" + Org_Id
          + "','" + Client_Id + "') > -1"));
      prepayments.addOrderBy(Invoice.PROPERTY_CREATIONDATE, false);
      List<Invoice> prepayment_list = prepayments.list();

      Currency currency_from = null, currency_to = null;
      Date exchange_rate_date = null;
      long countRecord = 0;
      for (int i = 0; i < prepayment_list.size(); i++) {
        Invoice prepayment = prepayment_list.get(i);
        List<InvoiceLine> prepaymentLines = prepayment.getInvoiceLineList();
        for (int j = 0; j < prepaymentLines.size(); j++) {
          InvoiceLine prepaymentLine = prepaymentLines.get(j);

          if (!prepaymentLine.isFinancialInvoiceLine())
            continue;

          if (prepaymentLine.getLineNetAmount().compareTo(BigDecimal.ZERO) <= 0)
            continue;

          BigDecimal creditToApply = prepaymentLine.getLineNetAmount().subtract(
              prepaymentLine.getScoCreditused());
          if (creditToApply.compareTo(BigDecimal.ZERO) <= 0)
            continue;

          BigDecimal exchangeRate = null, altExchangeRate = null;
          String operation = "";
          if (prepayment.getCurrency() == invoice.getCurrency()) {
            exchangeRate = BigDecimal.ONE;
            altExchangeRate = exchangeRate;

          } else {
            if ("PEN".equals(prepayment.getCurrency().getISOCode())) {
              currency_from = prepayment.getCurrency();
              currency_to = invoice.getCurrency();
              exchange_rate_date = invoice.getAccountingDate();
              operation = "*";
            } else {
              currency_from = invoice.getCurrency();
              currency_to = prepayment.getCurrency();
              exchange_rate_date = prepayment.getAccountingDate();
              operation = "/";
            }

            exchangeRate = findExchangeRate(currency_from, currency_to, exchange_rate_date,
                prepayment.getOrganization(), conversionRatePrecision);
            altExchangeRate = SCO_Utils.calculateAltConvRate(currency_from.getId(),
                currency_to.getId(), exchangeRate);
          }
          if (exchangeRate == null) {
            continue;
          }

          countRecord++;
          CreateFromRetainerData objectCreateFromRetainerData = new CreateFromRetainerData();
          objectCreateFromRetainerData.rownum = Long.toString(countRecord);
          objectCreateFromRetainerData.PREPAYLINE = prepaymentLine.getId();
          objectCreateFromRetainerData.PREPAYGLITEMNAME = prepaymentLine.getAccount().getName();
          objectCreateFromRetainerData.PREPAYPHYDOC = prepayment.getScrPhysicalDocumentno();
          objectCreateFromRetainerData.PREPAYAMT = PriceFormat.format(prepaymentLine
              .getLineNetAmount());
          objectCreateFromRetainerData.PREPAYTAX = prepaymentLine.getTax().getName();
          objectCreateFromRetainerData.PREPAYCREDITAVA = PriceFormat.format(creditToApply);

          objectCreateFromRetainerData.PREPAYCURRENCY = prepayment.getCurrency().getISOCode();
          objectCreateFromRetainerData.PREPAYDATEACCT = DateFormat.format(prepayment
              .getAccountingDate());
          objectCreateFromRetainerData.PREPAYEXCHANGERATE = exchangeRate.toString();
          objectCreateFromRetainerData.PREPAYALTEXCHANGERATE = altExchangeRate.toString();

          if (operation.isEmpty()) {
            objectCreateFromRetainerData.PREPAYAMTTOAPPLY = PriceFormat.format(creditToApply);
          } else if ("*".equals(operation)) {
            objectCreateFromRetainerData.PREPAYAMTTOAPPLY = PriceFormat.format(creditToApply
                .multiply(exchangeRate));
          } else if ("/".equals(operation)) {
            objectCreateFromRetainerData.PREPAYAMTTOAPPLY = PriceFormat.format(creditToApply
                .divide(exchangeRate, conversionRatePrecision, RoundingMode.HALF_UP));
          }

          objectCreateFromRetainerData.PREPAYEXCHANGERATEOP = operation;

          vector.addElement(objectCreateFromRetainerData);
        }
      }

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
    CreateFromRetainerData objectCreateFromRetainerData[] = new CreateFromRetainerData[vector
        .size()];
    vector.copyInto(objectCreateFromRetainerData);
    return (objectCreateFromRetainerData);
  }

  public static CreateFromRetainerData[] set() throws ServletException {
    CreateFromRetainerData objectCreateFromRetainerData[] = new CreateFromRetainerData[1];
    objectCreateFromRetainerData[0] = new CreateFromRetainerData();

    objectCreateFromRetainerData[0].INVDOCNO = "";
    objectCreateFromRetainerData[0].INVPARTNER = "";
    objectCreateFromRetainerData[0].INVDATEORDER = "";
    objectCreateFromRetainerData[0].INVTOTALAMOUNT = "";
    objectCreateFromRetainerData[0].INVCURRENCYNAME = "";

    objectCreateFromRetainerData[0].PREPAYLINE = "";
    objectCreateFromRetainerData[0].PREPAYGLITEMNAME = "";
    objectCreateFromRetainerData[0].PREPAYPHYDOC = "";
    objectCreateFromRetainerData[0].PREPAYAMT = "";
    objectCreateFromRetainerData[0].PREPAYTAX = "";
    objectCreateFromRetainerData[0].PREPAYCREDITAVA = "";

    return objectCreateFromRetainerData;
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