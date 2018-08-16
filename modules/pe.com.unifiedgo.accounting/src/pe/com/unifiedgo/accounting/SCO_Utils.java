package pe.com.unifiedgo.accounting;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.UtilSql;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.database.SessionInfo;
import org.openbravo.erpCommon.reference.PInstanceProcessData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.ui.Process;
import org.openbravo.model.common.currency.ConversionRate;
import org.openbravo.model.common.currency.ConversionRateDoc;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.service.db.CallProcess;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.db.QueryTimeOutUtil;

import pe.com.unifiedgo.sales.data.SSADiscountPreference;

public class SCO_Utils {

  public static final String TABLEID_Invoice = "318";
  public static final String TABLEID_Payment = "D1A97202E832470285C9B1EB026D54E2";
  public static final String TABLEID_Transaction = "4D8C3B3C31D1410DA046140C9F024D17";
  public static final String TABLEID_Reconciliation = "B1B7075C46934F0A9FD4C4D0F1457B42";

  public static TaxRate getTaxRateFromSpecial(Organization org, String specialTaxRate) {
    TaxRate taxrate = null;
    try {
      OBCriteria<TaxRate> obc = OBDal.getInstance().createCriteria(TaxRate.class);
      obc.setFilterOnReadableOrganization(false);
      obc.add(Restrictions.in(TaxRate.PROPERTY_ORGANIZATION + ".id",
          OBContext.getOBContext().getOrganizationStructureProvider().getNaturalTree(org.getId())));
      obc.add(Restrictions.eq(TaxRate.PROPERTY_SCOSPECIALTAX, specialTaxRate));
      obc.add(Restrictions.eq(TaxRate.PROPERTY_ACTIVE, true));

      List<TaxRate> taxrates = obc.list();
      if (taxrates.size() > 0)
        taxrate = taxrates.get(0);
      else
        taxrate = null;

    } catch (Exception e) {
      taxrate = null;
    }
    return taxrate;
  }

  public static GLItem getGLItemFromSpecial(Organization org, String specialGlItem) {
    GLItem glitem = null;
    try {
      OBCriteria<GLItem> obc = OBDal.getInstance().createCriteria(GLItem.class);
      obc.setFilterOnReadableOrganization(false);
      obc.add(Restrictions.in(GLItem.PROPERTY_ORGANIZATION + ".id",
          OBContext.getOBContext().getOrganizationStructureProvider().getNaturalTree(org.getId())));
      obc.add(Restrictions.eq(GLItem.PROPERTY_SCOSPECIALGLITEM, specialGlItem));

      glitem = (GLItem) obc.uniqueResult();
    } catch (Exception e) {
      glitem = null;
    }
    return glitem;
  }

  public static DocumentType getDocTypeFromSpecial(Organization org, String specialDocType) {
    DocumentType doctype = null;
    try {
      OBCriteria<DocumentType> obc = OBDal.getInstance().createCriteria(DocumentType.class);
      obc.setFilterOnReadableOrganization(false);
      obc.add(Restrictions.in(DocumentType.PROPERTY_ORGANIZATION + ".id",
          OBContext.getOBContext().getOrganizationStructureProvider().getNaturalTree(org.getId())));
      obc.add(Restrictions.eq(DocumentType.PROPERTY_SCOSPECIALDOCTYPE, specialDocType));

      doctype = (DocumentType) obc.uniqueResult();
    } catch (Exception e) {
      doctype = null;
    }
    return doctype;

  }

  public static String getDocumentNo(Organization org, String specialDocType, String strTableName) {
    DocumentType doctype = SCO_Utils.getDocTypeFromSpecial(org, specialDocType);
    return getDocumentNo(doctype, strTableName);
  }

  public static String getDocumentNo(DocumentType docType, String strTableName) {
    return getDocumentNo(docType, strTableName, true);
  }

  public static String getDocumentNo(DocumentType docType, String strTableName,
      boolean updateNext) {
    return FIN_Utility.getDocumentNo(docType, strTableName, updateNext);

  }

  public static Long getNextLineNo(String strTable, String strParentColumn, String strParentID) {
    try {
      Query q = OBDal.getInstance().getSession()
          .createSQLQuery("SELECT COALESCE(MAX(LINE),0)+10 AS DefaultValue FROM " + strTable
              + " WHERE " + strParentColumn + "='" + strParentID + "'");
      return new Long(((BigDecimal) q.uniqueResult()).longValue());
    } catch (Exception e) {
      return new Long(0);
    }
  }

  public static OBError completeInvoice(Invoice invoice) {
    OBError myMessage = null;
    DalConnectionProvider conn = new DalConnectionProvider();
    VariablesSecureApp vars = new VariablesSecureApp(OBContext.getOBContext().getUser().getId(),
        OBContext.getOBContext().getCurrentClient().getId(),
        OBContext.getOBContext().getCurrentOrganization().getId(),
        OBContext.getOBContext().getRole().getId(),
        OBContext.getOBContext().getLanguage().getLanguage());
    try {
      invoice.setDocumentAction("CO");
      OBDal.getInstance().save(invoice);
      OBDal.getInstance().flush();

      OBContext.setAdminMode(true);
      Process process = null;
      try {
        process = OBDal.getInstance().get(Process.class, "111");
      } finally {
        OBContext.restorePreviousMode();
      }

      Map<String, String> parameters = null;
      final ProcessInstance pinstance = CallProcess.getInstance().call(process, invoice.getId(),
          parameters);
      OBDal.getInstance().getSession().refresh(invoice);
      invoice.setAPRMProcessinvoice(invoice.getDocumentAction());
      OBDal.getInstance().save(invoice);
      OBDal.getInstance().flush();

      final PInstanceProcessData[] pinstanceData = PInstanceProcessData.select(conn,
          pinstance.getId());

      myMessage = Utility.getProcessInstanceMessage(conn, vars, pinstanceData);
    } catch (Exception ex) {
      myMessage = Utility.translateError(conn, vars, vars.getLanguage(), ex.getMessage());
    }

    return myMessage;
  }

  public static String getFormatedNumber(long number, int numdig) {
    String fnumber = Long.toString(number);
    int newlen = (numdig - fnumber.length());
    for (int i = 0; i < newlen; i++) {
      fnumber = "0" + fnumber;
    }

    return fnumber;
  }

  // transCurrency = fromCurrency
  // baseCurrency = toCurrency
  public static ConversionRate getConversionRate(Currency transCurrency, Currency baseCurrency,
      Date _conversionDate) {

    if (transCurrency.getId().equals(baseCurrency.getId())) {
      ConversionRate cr = new ConversionRate();
      cr.setMultipleRateBy(BigDecimal.ONE);
      cr.setDivideRateBy(BigDecimal.ONE);
      return cr;
    }

    // TRUNCATE DATE
    Date conversionDate = null;
    try {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      conversionDate = sdf.parse(sdf.format(_conversionDate));
    } catch (Exception e) {
      return null;
    }

    java.util.List<ConversionRate> convRateList;
    ConversionRate convRate;

    OBContext.setAdminMode(true);
    try {

      final OBCriteria<ConversionRate> obcConvRate = OBDal.getInstance()
          .createCriteria(ConversionRate.class);
      obcConvRate.add(Restrictions.eq(ConversionRate.PROPERTY_CURRENCY, transCurrency));
      obcConvRate.add(Restrictions.eq(ConversionRate.PROPERTY_TOCURRENCY, baseCurrency));
      obcConvRate.add(Restrictions.le(ConversionRate.PROPERTY_VALIDFROMDATE, conversionDate));
      obcConvRate.add(Restrictions.ge(ConversionRate.PROPERTY_VALIDTODATE, conversionDate));

      convRateList = obcConvRate.list();

      if ((convRateList != null) && (convRateList.size() != 0))
        convRate = convRateList.get(0);
      else
        convRate = null;

    } finally {
      OBContext.restorePreviousMode();
    }

    return convRate;
  }

  public static ConversionRateDoc getConversionRateDoc(String table_ID, String record_ID,
      String curFrom_ID, String curTo_ID) {
    OBCriteria<ConversionRateDoc> docRateCriteria = OBDal.getInstance()
        .createCriteria(ConversionRateDoc.class);
    docRateCriteria.add(Restrictions.eq(ConversionRateDoc.PROPERTY_TOCURRENCY,
        OBDal.getInstance().get(Currency.class, curTo_ID)));
    docRateCriteria.add(Restrictions.eq(ConversionRateDoc.PROPERTY_CURRENCY,
        OBDal.getInstance().get(Currency.class, curFrom_ID)));
    if (record_ID != null) {
      if (table_ID.equals(TABLEID_Invoice)) {
        docRateCriteria.add(Restrictions.eq(ConversionRateDoc.PROPERTY_INVOICE, OBDal.getInstance()
            .get(Invoice.class, OBDal.getInstance().get(Invoice.class, record_ID).getId())));
      } else if (table_ID.equals(TABLEID_Payment)) {
        docRateCriteria
            .add(Restrictions.eq(ConversionRateDoc.PROPERTY_PAYMENT, OBDal.getInstance().get(
                FIN_Payment.class, OBDal.getInstance().get(FIN_Payment.class, record_ID).getId())));
      } else if (table_ID.equals(TABLEID_Transaction)) {
        docRateCriteria.add(Restrictions.eq(ConversionRateDoc.PROPERTY_FINANCIALACCOUNTTRANSACTION,
            OBDal.getInstance().get(FIN_FinaccTransaction.class,
                OBDal.getInstance().get(FIN_FinaccTransaction.class, record_ID).getId())));
      } else {
        return null;
      }
    } else {
      return null;
    }
    List<ConversionRateDoc> conversionRates = docRateCriteria.list();
    if (!conversionRates.isEmpty()) {
      return conversionRates.get(0);
    }
    return null;
  }

  public static BigDecimal applyRate(BigDecimal _amount, ConversionRateDoc conversionRateDoc,
      boolean multiply) {
    BigDecimal amount = _amount;
    if (multiply) {
      return amount.multiply(conversionRateDoc.getRate());
    } else {
      return amount.divide(conversionRateDoc.getRate(), 6, BigDecimal.ROUND_HALF_EVEN);
    }
  }

  public static BigDecimal currencyConvertTable(BigDecimal amount, String stradTableId,
      String recordId, Currency currencyFrom, Currency currencyTo, Date ConvDate,
      String stradClientId, String stradOrgId) {

    if (currencyFrom.getId().equals(currencyTo.getId())) {
      return amount;
    }

    BigDecimal amountConverted;

    ConversionRateDoc conversionRateCurrentDoc = getConversionRateDoc(stradTableId, recordId,
        currencyFrom.getId(), currencyTo.getId());
    if (conversionRateCurrentDoc != null) {
      amountConverted = applyRate(amount, conversionRateCurrentDoc, true)
          .setScale(currencyTo.getStandardPrecision().intValue(), BigDecimal.ROUND_HALF_UP);
    } else {
      ConversionRate convrate = getConversionRate(currencyFrom, currencyTo, ConvDate);
      if (convrate != null) {
        amountConverted = amount.multiply(convrate.getMultipleRateBy())
            .setScale(currencyTo.getStandardPrecision().intValue(), BigDecimal.ROUND_HALF_UP);
      } else {
        return null;
      }
    }

    return amountConverted;
  }

  public static String[] getProductPricesByDate(ConnectionProvider conProvider, String adOrgId,
      String mProductId, String mPriceListId, String strValidFrom) throws ServletException {
    String[] result = new String[4];
    result[0] = "0";
    result[1] = "0";
    result[2] = "0";

    SCOUtilsData[] data = SCOUtilsData.getProductPricesByDate(conProvider, mProductId, mPriceListId,
        strValidFrom);
    if (data.length > 0) {
      result[0] = data[0].pricelist;
      result[1] = data[0].pricestd;
      result[2] = data[0].pricelimit;
    }

    // VIRTUAL PRICE IMPL
    PriceList pricelist = OBDal.getInstance().get(PriceList.class, mPriceListId);
    BigDecimal listprice = new BigDecimal(result[0]);
    if (listprice.compareTo(new BigDecimal(0)) == 0) {
      if (pricelist.getCurrency().getId().compareTo("308") != 0) {

        // if listprice=0 and currency is not PEN then calculate a virtual listprice based on the
        // convrate and related PEN PriceList
        if (pricelist.getScoPricelistPen() != null) {

          String product_data_pen[] = SCO_Utils.getProductPricesByDate(conProvider, adOrgId,
              mProductId, pricelist.getScoPricelistPen().getId(), strValidFrom);
          String strPENPriceList = product_data_pen[0];
          String strPENPriceStd = product_data_pen[1];
          String strPENPriceLimit = product_data_pen[2];

          String convrate = SCO_Utils.getExchangeRatePurch(conProvider,
              pricelist.getClient().getId(), pricelist.getOrganization().getId(),
              pricelist.getCurrency().getId(), strValidFrom);
          if (convrate != null) {
            result[0] = SCOUtilsData.getConvertedAmtPurch(conProvider, strPENPriceList, convrate,
                String.valueOf(pricelist.getCurrency().getPricePrecision()));
            result[1] = SCOUtilsData.getConvertedAmtPurch(conProvider, strPENPriceStd, convrate,
                String.valueOf(pricelist.getCurrency().getPricePrecision()));
            result[2] = SCOUtilsData.getConvertedAmtPurch(conProvider, strPENPriceLimit, convrate,
                String.valueOf(pricelist.getCurrency().getPricePrecision()));
          }
        }
      }
    }

    Organization org = OBDal.getInstance().get(Organization.class, adOrgId);
    BigDecimal incrementPerOrg = org.getScoInctopricelist();
    if (pricelist.isSalesPriceList() && incrementPerOrg != null
        && incrementPerOrg.compareTo(BigDecimal.ZERO) != 0) {
      result[0] = new BigDecimal(result[0])
          .multiply(incrementPerOrg.divide(new BigDecimal(100)).add(new BigDecimal(1.00)))
          .toString();
      result[1] = new BigDecimal(result[1])
          .multiply(incrementPerOrg.divide(new BigDecimal(100)).add(new BigDecimal(1.00)))
          .toString();
    }

    return result;
  }

  public static String[] getProductPricesByDate(ConnectionProvider conProvider, String adOrgId,
      String mProductId, String mPriceListId, Date validfrom) throws ServletException {
    String strDateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    final SimpleDateFormat dateFormat = new SimpleDateFormat(strDateFormat);
    String strValidFrom = dateFormat.format(validfrom);

    return getProductPricesByDate(conProvider, adOrgId, mProductId, mPriceListId, strValidFrom);

  }

  public static BigDecimal getTotalStockAvailable(String adOrgId, String adClientId,
      String mProductId) {

    String strSql = "";
    strSql = "SELECT scr_getStockAvailable('" + adClientId + "','" + adOrgId + "',NULL,'"
        + mProductId + "') AS qtyavailable FROM dual;";
    // System.out.println("consulta:" + strSql);
    try {
      Query q = OBDal.getInstance().getSession().createSQLQuery(strSql);
      BigDecimal result = (BigDecimal) q.uniqueResult();
      if (result != null) {
        return result;
      }
    } catch (Exception ex) {
      return BigDecimal.ZERO;
    }
    return BigDecimal.ZERO;
  }

  public static BigDecimal getWarehouseStockAvailable(String adOrgId, String adClientId,
      String mWarehouseId, String mProductId) {

    String strSql = "";
    strSql = "SELECT scr_getStockAvailable('" + adClientId + "','" + adOrgId + "','" + mWarehouseId
        + "','" + mProductId + "') AS qtyavailable FROM dual;";
    // System.out.println("consulta:" + strSql);
    try {
      Query q = OBDal.getInstance().getSession().createSQLQuery(strSql);
      BigDecimal result = (BigDecimal) q.uniqueResult();
      if (result != null) {
        return result;
      }
    } catch (Exception ex) {
      return BigDecimal.ZERO;
    }
    return BigDecimal.ZERO;
  }

  public static BigDecimal getProductCost(String adOrgId, String adClientId, String mProductId,
      String cCurrencyTo) throws ServletException {

    String strSql = "";
    strSql = "select CASE WHEN c.c_currency_id<>'" + cCurrencyTo
        + "' THEN COALESCE(SUM(C_Currency_Round(C_Currency_Convert(c.cost, c.c_currency_id, to_char('"
        + cCurrencyTo + "'), to_date(NOW()), NULL, c.ad_client_id, c.ad_org_id), to_char('"
        + cCurrencyTo + "'), NULL)), 0) ELSE c.cost END as cost " + "     from m_costing c "
        + "    where c.m_product_id='" + mProductId + "' " + "      and c.isActive='Y' "
        + "      and c.ad_client_id='" + adClientId + "' and ad_isorgincluded('" + adOrgId
        + "', c.ad_org_id, '" + adClientId
        + "') > -1  group by c.c_currency_id, c.cost, c.dateTo, c.created "
        + "order by c.dateTo desc, c.created desc LIMIT 1;";
    System.out.println("strSql");
    try {
      Query q = OBDal.getInstance().getSession().createSQLQuery(strSql);
      BigDecimal result = (BigDecimal) q.uniqueResult();
      if (result != null) {
        return result;
      }
    } catch (Exception ex) {
      return null;
    }
    return null;
  }

  public static Object[] getThreeProductLastSale(String adOrgId, String adClientId,
      String mProductId, String cbPartnerId) throws ServletException {
    return getThreeProductLastSale(adOrgId, adClientId, mProductId, cbPartnerId, 0, 0);
  }

  public static Object[] getThreeProductLastSale(String adOrgId, String adClientId,
      String mProductId, String cbPartnerId, int firstRegister, int numberRegisters)
      throws ServletException {
    ConnectionProvider connectionProvider = new DalConnectionProvider();

    Object[] res = new Object[3];
    String strSql = "";
    strSql = strSql
        + "          select (il.PriceActual || ' ' || curr.iso_code || '(' || date(i.dateacct) || ')') as data "
        + "          FROM c_invoice i, c_invoiceline il, c_currency curr "
        + "          where il.c_invoice_id=i.c_invoice_id "
        + "          and i.c_currency_id=curr.c_currency_id " + "          and i.docstatus='CO'"
        + "          and i.em_sco_specialdoctype='SCOARINVOICE' " + "          and il.isActive='Y' "
        + "          and i.c_bpartner_id=?" + "           and il.m_product_id=?"
        + "          and ad_isorgincluded(?, i.ad_org_id, i.ad_client_id) <> -1"
        + "          order by i.dateacct desc limit 3";
    System.out.println("strSql:" + strSql);
    ResultSet result;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, cbPartnerId);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, mProductId);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adOrgId);
      System.out.println("st:" + st);
      result = st.executeQuery();
      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while (countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }
      int i = 0;
      while (continueResult && result.next()) {
        System.out.println("aca i:" + i);
        countRecord++;
        res[i++] = UtilSql.getValue(result, "data");
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }
      result.close();
    } catch (SQLException e) {
      System.out.println("SQL error in query: " + strSql + "Exception:" + e.getMessage());
      throw new ServletException(
          "@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch (Exception ex) {
      System.out.println("Exception in query:" + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }
    return res;
  }

  public static boolean isOverDuedClient(String adOrgId, String adClientId, String cBPartnerId)
      throws ServletException {

    String strSql = "";
    strSql = "SELECT COUNT(*) AS result " + "     FROM C_Invoice"
        + "          INNER JOIN FIN_Payment_Schedule ON FIN_Payment_Schedule.C_Invoice_ID=C_Invoice.C_Invoice_ID"
        + "    WHERE C_Invoice.C_BPartner_ID IN (SELECT DISTINCT C_BPartner_ID"
        + "                                        FROM C_BPartner"
        + "                                       WHERE C_BPartner_ID='" + cBPartnerId + "'"
        + "                                         AND em_scr_iswhitelist='N')"
        + "     AND ((C_Invoice.Em_Sco_SpecialDocType='SCOARINVOICE'"
        + "           AND C_Invoice.Em_Sco_SpecialMethod!='SCOCHECK'"
        + "           AND DATE(FIN_Payment_Schedule.Duedate+interval '15 day')<DATE(NOW()))"
        + "       OR (C_Invoice.Em_Sco_SpecialDocType='SCOARINVOICE'"
        + "           AND C_Invoice.Em_Sco_SpecialMethod='SCOCHECK'"
        + "           AND DATE(FIN_Payment_Schedule.Duedate+interval '1 day')<DATE(NOW()))"
        + "       OR (C_Invoice.Em_Sco_SpecialDocType='SCOARBOEINVOICE'"
        + "           AND DATE(FIN_Payment_Schedule.Duedate+interval '9 day')<DATE(NOW())))"
        + "    AND C_Invoice.DocStatus='CO'" + "    AND C_Invoice.IsActive='Y'"
        + "    AND FIN_Payment_Schedule.IsActive='Y'"
        + "    AND FIN_Payment_Schedule.OutstandingAmt>0";
    try {
      Query q = OBDal.getInstance().getSession().createSQLQuery(strSql);
      BigInteger result = (BigInteger) q.uniqueResult();
      if (result != null && result.compareTo(BigInteger.ZERO) > 0)
        return true;
      return false;

    } catch (Exception ex) {
      return false;
    }
  }

  public static FIN_PaymentMethod getPaymentMethodBySpecialMethod(String AD_Client_ID,
      String AD_Org_ID, String specialmethod) {
    OBCriteria<FIN_PaymentMethod> oc = OBDal.getInstance().createCriteria(FIN_PaymentMethod.class);
    oc.add(Restrictions.eq(FIN_PaymentMethod.PROPERTY_CLIENT,
        OBDal.getInstance().get(Client.class, AD_Client_ID)));
    oc.add(Restrictions.sqlRestriction(
        "AD_ISORGINCLUDED('" + AD_Org_ID + "', ad_org_id, '" + AD_Client_ID + "') > -1"));
    oc.add(Restrictions.eq(FIN_PaymentMethod.PROPERTY_SCOSPECIALMETHOD, specialmethod));

    List<FIN_PaymentMethod> paymentmethods = oc.list();
    if (paymentmethods.size() > 0)
      return paymentmethods.get(0);

    return null;
  }

  public static BigDecimal calculateAltConvRate(String finaccCurrId, String paymentCurrId,
      BigDecimal convertRate) {

    if (convertRate == null)
      return null;
    if (convertRate.compareTo(new BigDecimal(0)) == 0)
      return null;

    BigDecimal altconverRate = new BigDecimal(1.0);

    if (SCO_Utils.calculateRatePolarity(finaccCurrId, paymentCurrId).compareTo("E") == 0) {
      altconverRate = convertRate;
    } else {
      int precision = 3;
      if (finaccCurrId.compareTo("308") != 0 && paymentCurrId.compareTo("308") != 0) {
        precision = 6;
      }
      BigDecimal one = new BigDecimal(1.0);
      altconverRate = one.divide(convertRate, precision, 4);
    }
    return altconverRate;
  }

  public static BigDecimal calculateNormalConvRate(String finaccCurrId, String paymentCurrId,
      BigDecimal altConvertRate) {
    BigDecimal converRate = new BigDecimal(1.0);

    if (SCO_Utils.calculateRatePolarity(finaccCurrId, paymentCurrId).compareTo("E") == 0) {
      converRate = altConvertRate;
    } else {
      BigDecimal one = new BigDecimal(1.0);
      converRate = one.divide(altConvertRate, 15, 4);
    }
    return converRate;
  }

  public static String calculateRatePolarity(String finaccCurrId, String paymentCurrId) {
    if (finaccCurrId.compareTo("308") == 0) {
      return "D";
    } else if (paymentCurrId.compareTo("308") == 0) {
      return "E";
    } else if (finaccCurrId.compareTo("100") == 0) {
      return "D";
    } else if (paymentCurrId.compareTo("100") == 0) {
      return "E";
    } else if (finaccCurrId.compareTo("102") == 0) {
      return "D";
    } else if (paymentCurrId.compareTo("102") == 0) {
      return "E";
    } else {
      return "E";
    }
  }

  public static String getTabId(ConnectionProvider conn, String adTableId, String recordId,
      boolean issotrx) throws ServletException {
    boolean tabredirected = false;
    String redirectWindowId = null;

    String keywordcolumnname = SCOUtilsData.selectKeyWordColumnName(conn, adTableId);
    String strTableName = SCOUtilsData.selectTableName(conn, adTableId);
    String strKeyReferenceColumnName = strTableName + "_ID";
    String keyword = null;
    if (keywordcolumnname != null && !keywordcolumnname.equals("") && strTableName != null
        && !strTableName.equals("") && strKeyReferenceColumnName != null
        && !strKeyReferenceColumnName.equals("") && recordId != null && !recordId.equals("")) {
      String sqlQuery;
      sqlQuery = "SELECT " + keywordcolumnname + " FROM " + strTableName + " WHERE "
          + strKeyReferenceColumnName + "=\'" + recordId + "\' LIMIT 1;";
      Query q = OBDal.getInstance().getSession().createSQLQuery(sqlQuery);
      keyword = (String) q.uniqueResult();
      if (keyword != null && !keyword.equals("")) {
        redirectWindowId = SCOUtilsData.selectRedirectWindowId(conn, keyword, adTableId);
        if (redirectWindowId != null && !redirectWindowId.equals("")) {
          tabredirected = true;
        }
      }
    }

    String strWindowId = null;
    if (tabredirected) {
      strWindowId = redirectWindowId;
    } else {
      SCOUtilsData[] data = SCOUtilsData.selectWindows(conn, adTableId);
      if (data == null || data.length == 0)
        throw new ServletException("Window not found");

      // only in case an adWindowId is returned
      if (!data[0].adWindowId.equals("")) {
        strWindowId = data[0].adWindowId;
      }
      if (!issotrx && !data[0].poWindowId.equals("")) {
        strWindowId = data[0].poWindowId;
      }
    }

    SCOUtilsData[] data = SCOUtilsData.selectTab(conn, strWindowId, adTableId);
    if (data == null || data.length == 0)
      throw new ServletException("Window not found: " + strWindowId);
    String tabId = data[0].adTabId;
    if (recordId.equals("")) {
      data = SCOUtilsData.selectParent(conn, strWindowId);
      if (data == null || data.length == 0)
        throw new ServletException("Window parent not found: " + strWindowId);
      tabId = data[0].adTabId;
    }
    return tabId;

  }

  public static String getExchangeRateUSDPurch(ConnectionProvider conn, String adClientId,
      String adOrgId, Date date) throws ServletException {
    String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_JAVA);
    String strDate = sdf.format(date);

    SCOUtilsData[] data = SCOUtilsData.getExchangeRateUSDPurch(conn, adClientId, adOrgId, strDate);
    if (data != null && data.length > 0) {
      return data[0].convrate;
    }

    return null;
  }

  public static String getExchangeRatePurch(ConnectionProvider conn, String adClientId,
      String adOrgId, String cCurrencyId, String strDate) throws ServletException {

    SCOUtilsData[] data = SCOUtilsData.getExchangeRatePurch(conn, adClientId, adOrgId, cCurrencyId,
        strDate);
    if (data != null && data.length > 0) {
      return data[0].convrate;
    }

    return null;
  }

  public static String getExchangeRateUSDSales(ConnectionProvider conn, String adClientId,
      String adOrgId, Date date) throws ServletException {
    String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_JAVA);
    String strDate = sdf.format(date);

    SCOUtilsData[] data = SCOUtilsData.getExchangeRateUSDSales(conn, adClientId, adOrgId, strDate);
    if (data != null && data.length > 0) {
      return data[0].convrate;
    }

    return null;
  }

  public static String getExchangeRateEURSales(ConnectionProvider conn, String adClientId,
      String adOrgId, Date date) throws ServletException {
    String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_JAVA);
    String strDate = sdf.format(date);

    SCOUtilsData[] data = SCOUtilsData.getExchangeRateEURSales(conn, adClientId, adOrgId, strDate);
    if (data != null && data.length > 0) {
      return data[0].convrate;
    }

    return null;
  }

  public static String getMultiplyRateGenPurch(ConnectionProvider conn, String adClientId,
      String adOrgId, String cCurrencyFromId, String cCurrencyToId, Date date)
      throws ServletException {
    String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_JAVA);
    String strDate = sdf.format(date);

    if (cCurrencyFromId.compareTo(cCurrencyToId) == 0)
      return "1";

    SCOUtilsData[] data = SCOUtilsData.getMultiplyRateGenPurch(conn, adClientId, adOrgId,
        cCurrencyToId, cCurrencyFromId, strDate);
    if (data != null && data.length > 0) {
      return data[0].convrate;
    }

    return null;
  }

  public static String getMultiplyRateGenSales(ConnectionProvider conn, String adClientId,
      String adOrgId, String cCurrencyFromId, String cCurrencyToId, Date date)
      throws ServletException {
    String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_JAVA);
    String strDate = sdf.format(date);

    if (cCurrencyFromId.compareTo(cCurrencyToId) == 0)
      return "1";

    SCOUtilsData[] data = SCOUtilsData.getMultiplyRateGenSales(conn, adClientId, adOrgId,
        cCurrencyFromId, cCurrencyToId, strDate);
    if (data != null && data.length > 0) {
      return data[0].convrate;
    }

    return null;
  }

  public static String getDiscount(ConnectionProvider conn, String strPriceActual,
      String strPriceList) throws ServletException {
    String discount = SCOUtilsData.getDiscount(conn, strPriceList, strPriceActual);
    if (discount == null)
      return "0";
    return discount;
  }

  public static String getParallelExchangeRateUSDSales(ConnectionProvider conn, String adClientId,
      String adOrgId, Date date) throws ServletException {
    String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_JAVA);
    String strDate = sdf.format(date);

    SCOUtilsData[] data = SCOUtilsData.getParallelExchangeRateUSDSales(conn, adClientId, adOrgId,
        strDate);
    if (data != null && data.length > 0) {
      return data[0].convrate;
    }

    return null;
  }

  public static String getInvoiceMultiplyRate(ConnectionProvider conn, String cInvoiceId,
      String cCurrencyToId) throws ServletException {
    String multiplyrate = SCOUtilsData.getInvoiceMultiplyRate(conn, cInvoiceId, cCurrencyToId);
    if (multiplyrate == null)
      return null;
    if (multiplyrate.isEmpty())
      return null;
    return multiplyrate;
  }

  public static String getRegNumberFromFactAcct(ConnectionProvider conn, String adClientId,
      String adTableId, String recordId) throws ServletException {
    String regnumber = SCOUtilsData.getRegNumberFromFactAcct(conn, adClientId, adTableId, recordId);
    return regnumber;
  }

  public static List<User> getDiscPrefUserAllPermissions(String AD_Client_ID, String AD_Org_ID) {
    OBCriteria<SSADiscountPreference> discount_prefs = OBDal.getInstance()
        .createCriteria(SSADiscountPreference.class);
    List<User> users = new ArrayList<User>();

    discount_prefs.add(Restrictions.eq(SSADiscountPreference.PROPERTY_APPROVEALL, true));
    discount_prefs.add(Restrictions.eq(SSADiscountPreference.PROPERTY_ACTIVE, true));
    discount_prefs.add(Restrictions.eq(SSADiscountPreference.PROPERTY_CLIENT,
        OBDal.getInstance().get(Client.class, AD_Client_ID)));
    discount_prefs.add(Restrictions.sqlRestriction(
        "AD_ISORGINCLUDED('" + AD_Org_ID + "', ad_org_id, '" + AD_Client_ID + "') > -1"));
    discount_prefs.addOrderBy(SSADiscountPreference.PROPERTY_CREATIONDATE, false);
    if (discount_prefs.list().size() > 0) {
      for (int i = 0; i < discount_prefs.list().size(); i++) {
        users.add(discount_prefs.list().get(i).getDiscountEvaluator());
      }
    }
    return users;
  }

  public static List<User> getDiscPrefUsersCanEvaluate(String AD_Client_ID, String AD_Org_ID,
      BigDecimal discount) {
    OBCriteria<SSADiscountPreference> discount_prefs = OBDal.getInstance()
        .createCriteria(SSADiscountPreference.class);
    List<User> users = new ArrayList<User>();

    discount_prefs.add(Restrictions.eq(SSADiscountPreference.PROPERTY_ACTIVE, true));
    discount_prefs.add(Restrictions.eq(SSADiscountPreference.PROPERTY_APPROVEALL, false));
    discount_prefs.add(Restrictions.eq(SSADiscountPreference.PROPERTY_CLIENT,
        OBDal.getInstance().get(Client.class, AD_Client_ID)));
    discount_prefs.add(Restrictions.sqlRestriction(
        "AD_ISORGINCLUDED('" + AD_Org_ID + "', ad_org_id, '" + AD_Client_ID + "') <> -1"));
    discount_prefs.add(Restrictions.le(SSADiscountPreference.PROPERTY_RANGEFROM, discount));
    discount_prefs.addOrderBy(SSADiscountPreference.PROPERTY_CREATIONDATE, false);
    if (discount_prefs.list().size() > 0) {
      for (int i = 0; i < discount_prefs.list().size(); i++) {
        BigDecimal rangeto = (discount_prefs.list().get(i).getRangeTo() != null)
            ? discount_prefs.list().get(i).getRangeTo()
            : new BigDecimal(999999999);
        if (discount.compareTo(rangeto) <= 0) {
          users.add(discount_prefs.list().get(i).getDiscountEvaluator());
        }
      }
    }
    return users;
  }

  public static boolean isUseSalesTCOnly(ConnectionProvider conn, String adClientId)
      throws ServletException {
    String isUseSalesTCOnly = SCOUtilsData.isUseSalesTCOnly(conn, adClientId);
    if (isUseSalesTCOnly.equals("Y")) {
      return true;
    }
    return false;
  }
}
