package pe.com.unifiedgo.accounting.ad_actionbutton;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.currency.ConversionRate;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.pricing.pricelist.PriceListVersion;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.accounting.SCO_Utils;

public class CalculateNewBasedVersion extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static String strBDErrorMessage = "";
  private AdvPaymentMngtDao dao;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    int conversionRatePrecision = FIN_Utility.getConversionRatePrecision(vars);

    if (vars.commandIn("DEFAULT")) {
      String stradOrgId = vars.getGlobalVariable("inpadOrgId", "CalculateNewBasedVersion|AD_Org_ID", "");
      String strmPricelistVersionId = vars.getGlobalVariable("inpmPricelistVersionId", "CalculateNewBasedVersion|MPricelistVersionID", "");
      String strAltExchangeRate = vars.getGlobalVariable("inpAltExchangeRate", "CalculateNewBasedVersion|ExchangeRate", "");
      String strTabId = vars.getGlobalVariable("inpTabId", "CalculateNewBasedVersion|Tab_ID");
      String strWindowId = vars.getGlobalVariable("inpwindowId", "CalculateNewBasedVersion|Window_ID");

      PriceListVersion plv = OBDal.getInstance().get(PriceListVersion.class, strmPricelistVersionId);
      if (plv.isScrIsbasedonversion() == null || plv.isScrIsbasedonversion() == false || plv.getScrPrilversion() == null) {
        OBError message = new OBError();
        message.setType("Error");
        message.setTitle(Utility.parseTranslation(this, vars, vars.getLanguage(), "@OBUIAPP_Error@"));
        message.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(), "@SCO_CalculateNewBasedVersion_NotBased@"));
        vars.setMessage(strTabId, message);
        printPageClosePopUpAndRefreshParent(response, vars);

        OBDal.getInstance().rollbackAndClose();
        return;
      }

      DecimalFormat dfQty = Utility.getFormat(vars, "generalQtyEdition");
      dfQty.setRoundingMode(RoundingMode.HALF_UP);

      if (plv.getScoBaseverAltconvrate() != null) {
        strAltExchangeRate = dfQty.format(plv.getScoBaseverAltconvrate());
      } else {
        if (plv.getScrPrilversion().getPriceList().getCurrency().getId().compareTo(plv.getPriceList().getCurrency().getId()) == 0) {
          strAltExchangeRate = "1";
        } else {
          // Calculate todays alternative conversion rate
          final ConversionRate conversionRate = getConversionRate(plv.getScrPrilversion().getPriceList().getCurrency(), plv.getPriceList().getCurrency(), new Date(), plv.getOrganization());

          if (conversionRate != null) {

            BigDecimal exchangeRate = conversionRate.getMultipleRateBy();
            BigDecimal altExchangeRate = SCO_Utils.calculateAltConvRate(plv.getScrPrilversion().getPriceList().getCurrency().getId(), plv.getPriceList().getCurrency().getId(), exchangeRate);
            strAltExchangeRate = dfQty.format(altExchangeRate);
          }
        }
      }

      printPageDataSheet(response, vars, strWindowId, strTabId, stradOrgId, strmPricelistVersionId, strAltExchangeRate);

    } else if (vars.commandIn("SAVE")) {

      String stradOrgId = vars.getRequiredStringParameter("inpadOrgId");
      String strmPricelistVersionId = vars.getRequiredStringParameter("inpmPricelistVersionId");
      String strAltExchangeRate = vars.getNumericParameter("inpAltExchangeRate", "0");
      String strTabId = vars.getRequiredStringParameter("inpTabId");
      String strWindowId = vars.getRequiredStringParameter("inpadWindowId");

      DecimalFormat dfQty = Utility.getFormat(vars, "generalQtyEdition");
      dfQty.setRoundingMode(RoundingMode.HALF_UP);

      BigDecimal ExchangeRate = null;
      BigDecimal AltExchangeRate = null;

      try {
        AltExchangeRate = new BigDecimal(dfQty.parse(strAltExchangeRate).toString());
      } catch (Exception e) {
        e.printStackTrace();
        OBError message = new OBError();
        message.setType("Error");
        message.setTitle(Utility.parseTranslation(this, vars, vars.getLanguage(), "@OBUIAPP_Error@"));
        message.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(), "@SCO_ParseException@"));
        vars.setMessage(strTabId, message);
        printPageClosePopUpAndRefreshParent(response, vars);

        OBDal.getInstance().rollbackAndClose();
        return;
      }

      try {

        // calculate real exchange rate
        PriceListVersion plv = OBDal.getInstance().get(PriceListVersion.class, strmPricelistVersionId);
        if (plv.isScrIsbasedonversion() == null || plv.isScrIsbasedonversion() == false || plv.getScrPrilversion() == null) {
          OBError message = new OBError();
          message.setType("Error");
          message.setTitle(Utility.parseTranslation(this, vars, vars.getLanguage(), "@OBUIAPP_Error@"));
          message.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(), "@SCO_CalculateNewBasedVersion_NotBased@"));
          vars.setMessage(strTabId, message);
          printPageClosePopUpAndRefreshParent(response, vars);

          OBDal.getInstance().rollbackAndClose();
          return;
        }

        ExchangeRate = SCO_Utils.calculateNormalConvRate(plv.getScrPrilversion().getPriceList().getCurrency().getId(), plv.getPriceList().getCurrency().getId(), AltExchangeRate);
        OBError myMessage = processGeneratePricesForVersion(vars, plv.getId(), plv.getScrPrilversion().getId(), dfQty.format(AltExchangeRate), dfQty.format(ExchangeRate), dfQty.format(plv.getPriceList().getCurrency().getPricePrecision()));

        vars.setMessage(strTabId, myMessage);
        printPageClosePopUp(response, vars);
        return;

      } catch (Exception ex) {
        ex.printStackTrace();
        String strMessage = FIN_Utility.getExceptionMessage(ex);
        OBError message = new OBError();
        message.setType("Error");
        message.setTitle(Utility.parseTranslation(this, vars, vars.getLanguage(), "@OBUIAPP_Error@"));
        message.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(), strMessage));
        vars.setMessage(strTabId, message);
        printPageClosePopUpAndRefreshParent(response, vars);

        OBDal.getInstance().rollbackAndClose();
        return;

      }
    }
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strWindowId, String strTabId, String stradOrgId, String strmPricelistVersionId, String strAltExchangeRate) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    XmlDocument xmlDocument = null;

    String discard[] = { "secTable" };
    xmlDocument = xmlEngine.readXmlTemplate("pe/com/unifiedgo/accounting/ad_actionbutton/CalculateNewBasedVersion", discard).createXmlDocument();

    OBError myMessage = vars.getMessage("CalculateNewBasedVersion");
    vars.removeMessage("CalculateNewBasedVersion");
    if (myMessage != null) {
      xmlDocument.setParameter("messageType", myMessage.getType());
      xmlDocument.setParameter("messageTitle", myMessage.getTitle());
      xmlDocument.setParameter("messageMessage", myMessage.getMessage());
    }

    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("orgId", stradOrgId);
    xmlDocument.setParameter("tabId", strTabId);
    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("strTabId", "strTabId =  \"" + strTabId + "\";\r\n");
    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("isReceipt", "N");

    xmlDocument.setParameter("AltExchangeRate", strAltExchangeRate);
    xmlDocument.setParameter("mPricelistVersionId", strmPricelistVersionId);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private OBError processGeneratePricesForVersion(VariablesSecureApp vars, String mPricelistVersionId, String basedmPricelistVersionId, String strAltExchangeRate, String strExchangeRate, String strPrecision) throws IOException, ServletException {

    Connection conn = null;

    OBError myMessage = null;
    myMessage = new OBError();
    myMessage.setTitle("");

    try {
      conn = getTransactionConnection();
      PriceListVersion plv = OBDal.getInstance().get(PriceListVersion.class, mPricelistVersionId);

      CalculateNewBasedVersionData[] basedpp = CalculateNewBasedVersionData.getBasedProductPrices(this, strExchangeRate, strPrecision, basedmPricelistVersionId);
      String strmProductpriceId = "";
      for (int i = 0; i < basedpp.length; i++) {

        CalculateNewBasedVersionData[] data = CalculateNewBasedVersionData.getProductPricesByVersion(this, mPricelistVersionId, basedpp[i].mProductId);
        if (data != null && data.length != 0) {
          CalculateNewBasedVersionData.updateProductPrice(conn, this, basedpp[i].pricelist, basedpp[i].pricestd, basedpp[i].pricelimit, vars.getUser(), data[0].mProductpriceId);
        } else {
          strmProductpriceId = SequenceIdData.getUUID();
          try {
            CalculateNewBasedVersionData.insertProductPrice(conn, this, strmProductpriceId, plv.getClient().getId(), plv.getOrganization().getId(), vars.getUser(), mPricelistVersionId, basedpp[i].mProductId, basedpp[i].pricelist, basedpp[i].pricestd, basedpp[i].pricelimit);
          } catch (ServletException ex) {
            myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
            releaseRollbackConnection(conn);
            return myMessage;
          }
        }
      }

      CalculateNewBasedVersionData.updatePriceListVersionBCV(conn, this, strAltExchangeRate, mPricelistVersionId);

      releaseCommitConnection(conn);
      myMessage.setMessage(Utility.messageBD(this, "SCO_CalculateNewBasedVersion_Success", vars.getLanguage()));
      myMessage.setType("Success");
      return myMessage;
    } catch (Exception e) {
      try {
        if (conn != null)
          releaseRollbackConnection(conn);
      } catch (Exception ignored) {
      }
      e.printStackTrace();
      log4j.warn("Rollback in transaction");
      myMessage.setType("Error");
      myMessage.setMessage(Utility.messageBD(this, "ProcessRunError", vars.getLanguage()));
      return myMessage;
    }
  }

  private ConversionRate getConversionRate(Currency fromCurrency, Currency toCurrency, Date _conversionDate, Organization org) {

    // TRUNCATE DATE
    Date conversionDate = null;
    try {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      conversionDate = sdf.parse(sdf.format(_conversionDate));
    } catch (Exception e) {
      return null;
    }

    java.util.List<ConversionRate> conversionRateList;
    ConversionRate conversionRate;
    OBContext.setAdminMode(true);
    try {
      final OBCriteria<ConversionRate> obcConvRate = OBDal.getInstance().createCriteria(ConversionRate.class);
      obcConvRate.setFilterOnReadableOrganization(false);
      obcConvRate.add(Restrictions.eq(ConversionRate.PROPERTY_ORGANIZATION, org));
      obcConvRate.add(Restrictions.eq(ConversionRate.PROPERTY_CURRENCY, fromCurrency));
      obcConvRate.add(Restrictions.eq(ConversionRate.PROPERTY_TOCURRENCY, toCurrency));
      obcConvRate.add(Restrictions.le(ConversionRate.PROPERTY_VALIDFROMDATE, conversionDate));
      obcConvRate.add(Restrictions.ge(ConversionRate.PROPERTY_VALIDTODATE, conversionDate));
      conversionRateList = obcConvRate.list();
      if ((conversionRateList != null) && (conversionRateList.size() != 0)) {
        conversionRate = conversionRateList.get(0);
      } else {
        if ("0".equals(org.getId())) {
          conversionRate = null;
        } else {
          return getConversionRate(fromCurrency, toCurrency, conversionDate, OBDal.getInstance().get(Organization.class, OBContext.getOBContext().getOrganizationStructureProvider().getParentOrg(org.getId())));
        }
      }
    } catch (Exception e) {
      log4j.error(e);
      return null;
    } finally {
      OBContext.restorePreviousMode();
    }
    return conversionRate;
  }

}