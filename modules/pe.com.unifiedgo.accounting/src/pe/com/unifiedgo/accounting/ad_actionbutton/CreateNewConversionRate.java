package pe.com.unifiedgo.accounting.ad_actionbutton;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.currency.ConversionRate;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class CreateNewConversionRate extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static String strBDErrorMessage = "";
  private AdvPaymentMngtDao dao;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    int conversionRatePrecision = FIN_Utility.getConversionRatePrecision(vars);

    if (vars.commandIn("DEFAULT")) {
      String stradOrgId = vars.getGlobalVariable("inpadOrgId", "CreateNewConversionRate|AD_Org_ID", "");
      String strcConversionRateId = vars.getGlobalVariable("inpcConversionRateId", "CreateNewConversionRate|CConversionRateId", "");
      String strConvrateDate = vars.getGlobalVariable("inpConvrateDate", "CreateNewConversionRate|ConvrateDate", SREDateTimeData.today(this));
      String strExchangeRateUSDPurch = vars.getGlobalVariable("inpExchangeRateUSDPurch", "CreateNewConversionRate|ExchangeRateUSDPuch", "0");
      String strExchangeRateUSDSales = vars.getGlobalVariable("inpExchangeRateUSDSales", "CreateNewConversionRate|ExchangeRateUSDSales", "0");
      String strExchangeRateEURSales = vars.getGlobalVariable("inpExchangeRateEURSales", "CreateNewConversionRate|ExchangeRateEURSales", "0");
      String strExchangeRateEURPurch = vars.getGlobalVariable("inpExchangeRateEURPurch", "CreateNewConversionRate|ExchangeRateEURPurch", "0");
      String strUSDExchangeRateEURSales = vars.getGlobalVariable("inpUSDExchangeRateEURSales", "CreateNewConversionRate|USDExchangeRateEURSales", "0");
      String strUSDExchangeRateEURPurch = vars.getGlobalVariable("inpUSDExchangeRateEURPurch", "CreateNewConversionRate|USDExchangeRateEURPurch", "0");

      String strTabId = vars.getGlobalVariable("inpTabId", "CreateNewConversionRate|Tab_ID");
      String strWindowId = vars.getGlobalVariable("inpwindowId", "CreateNewConversionRate|Window_ID");

      CreateNewConversionRateData[] data = null;

      data = CreateNewConversionRateData.getExchangeRateUSDPurch(this, vars.getClient(), stradOrgId, strConvrateDate);
      if (data != null && data.length > 0) {
        strExchangeRateUSDPurch = data[0].convrate;
      } else {
        strExchangeRateUSDPurch = "0";
      }

      data = CreateNewConversionRateData.getExchangeRateUSDSales(this, vars.getClient(), stradOrgId, strConvrateDate);
      if (data != null && data.length > 0) {
        strExchangeRateUSDSales = data[0].convrate;
      } else {
        strExchangeRateUSDSales = "0";
      }

      data = CreateNewConversionRateData.getExchangeRateEURSales(this, vars.getClient(), stradOrgId, strConvrateDate);
      if (data != null && data.length > 0) {
        strExchangeRateEURSales = data[0].convrate;
      } else {
        strExchangeRateEURSales = "0";
      }

      data = CreateNewConversionRateData.getExchangeRateEURPurch(this, vars.getClient(), stradOrgId, strConvrateDate);
      if (data != null && data.length > 0) {
        strExchangeRateEURPurch = data[0].convrate;
      } else {
        strExchangeRateEURPurch = "0";
      }

      data = CreateNewConversionRateData.getUSDExchangeRateEURSales(this, vars.getClient(), stradOrgId, strConvrateDate);
      if (data != null && data.length > 0) {
        strUSDExchangeRateEURSales = data[0].convrate;
      } else {
        strUSDExchangeRateEURSales = "0";
      }

      data = CreateNewConversionRateData.getUSDExchangeRateEURPurch(this, vars.getClient(), stradOrgId, strConvrateDate);
      if (data != null && data.length > 0) {
        strUSDExchangeRateEURPurch = data[0].convrate;
      } else {
        strUSDExchangeRateEURPurch = "0";
      }

      printPageDataSheet(response, vars, strWindowId, strTabId, stradOrgId, strcConversionRateId, strConvrateDate, strExchangeRateUSDPurch, strExchangeRateUSDSales, strExchangeRateEURSales, strExchangeRateEURPurch, strUSDExchangeRateEURSales, strUSDExchangeRateEURPurch);

    } else if (vars.commandIn("EXCHANGERATE")) {

      String stradOrgId = vars.getGlobalVariable("inpadOrgId", "");
      String strConvrateDate = vars.getGlobalVariable("inpConvrateDate", "");

      refreshExchangeRate(response, vars, stradOrgId, strConvrateDate);

    } else if (vars.commandIn("SAVE")) {

      String stradOrgId = vars.getRequiredStringParameter("inpadOrgId");
      String strcConversionRateId = vars.getRequiredStringParameter("inpcConversionRateId");
      String strConvrateDate = vars.getRequiredStringParameter("inpConvrateDate");
      String strExchangeRateUSDPurch = vars.getNumericParameter("inpExchangeRateUSDPurch", "0");
      String strExchangeRateUSDSales = vars.getNumericParameter("inpExchangeRateUSDSales", "0");
      String strExchangeRateEURSales = vars.getNumericParameter("inpExchangeRateEURSales", "0");
      String strExchangeRateEURPurch = vars.getNumericParameter("inpExchangeRateEURPurch", "0");
      String strUSDExchangeRateEURSales = vars.getNumericParameter("inpUSDExchangeRateEURSales", "0");
      String strUSDExchangeRateEURPurch = vars.getNumericParameter("inpUSDExchangeRateEURPurch", "0");
      String strTabId = vars.getRequiredStringParameter("inpTabId");
      String strWindowId = vars.getRequiredStringParameter("inpadWindowId");

      String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty("dateFormat.java");
      SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_JAVA);
      DecimalFormat dfQty = Utility.getFormat(vars, "generalQtyEdition");
      dfQty.setRoundingMode(RoundingMode.HALF_UP);

      Date ConvrateDate = null;
      BigDecimal ExchangeRateUSDPurch = null;
      BigDecimal ExchangeRateUSDSales = null;
      BigDecimal ExchangeRateEURSales = null;
      BigDecimal ExchangeRateEURPurch = null;
      BigDecimal USDExchangeRateEURSales = null;
      BigDecimal USDExchangeRateEURPurch = null;

      try {
        ConvrateDate = sdf.parse(strConvrateDate);
        ExchangeRateUSDPurch = new BigDecimal(dfQty.parse(strExchangeRateUSDPurch).toString());
        ExchangeRateUSDSales = new BigDecimal(dfQty.parse(strExchangeRateUSDSales).toString());
        ExchangeRateEURSales = new BigDecimal(dfQty.parse(strExchangeRateEURSales).toString());
        ExchangeRateEURPurch = new BigDecimal(dfQty.parse(strExchangeRateEURPurch).toString());
        USDExchangeRateEURSales = new BigDecimal(dfQty.parse(strUSDExchangeRateEURSales).toString());
        USDExchangeRateEURPurch = new BigDecimal(dfQty.parse(strUSDExchangeRateEURPurch).toString());

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

        CreateNewConversionRateData data[] = null;
        ConversionRate crAux = OBDal.getInstance().get(ConversionRate.class, strcConversionRateId);
        Currency currUsd = OBDal.getInstance().get(Currency.class, "100");
        Currency currPen = OBDal.getInstance().get(Currency.class, "308");
        Currency currEur = OBDal.getInstance().get(Currency.class, "102");

        if (ExchangeRateUSDPurch.compareTo(new BigDecimal(0)) > 0) {
          data = CreateNewConversionRateData.getExchangeRateUSDPurch(this, vars.getClient(), stradOrgId, strConvrateDate);
          if (data != null && data.length > 0) {
            ConversionRate cr = OBDal.getInstance().get(ConversionRate.class, data[0].cConversionRateId);
            cr.setDivideRateBy(ExchangeRateUSDPurch);
            cr.setMultipleRateBy(BigDecimal.ONE.divide(ExchangeRateUSDPurch, 15, RoundingMode.HALF_UP));
            OBDal.getInstance().save(cr);

          } else {
            ConversionRate cr = OBProvider.getInstance().get(ConversionRate.class);

            cr.setClient(crAux.getClient());
            cr.setOrganization(crAux.getOrganization());
            cr.setConversionRateType("S");
            cr.setCurrency(currPen);
            cr.setToCurrency(currUsd);
            cr.setValidFromDate(ConvrateDate);
            cr.setValidToDate(ConvrateDate);
            cr.setDivideRateBy(ExchangeRateUSDPurch);
            cr.setMultipleRateBy(BigDecimal.ONE.divide(ExchangeRateUSDPurch, 15, RoundingMode.HALF_UP));
            cr.setSCODescripcin("T/C Compra a Dólares");
            OBDal.getInstance().save(cr);
          }
        }

        if (ExchangeRateUSDSales.compareTo(new BigDecimal(0)) > 0) {
          data = CreateNewConversionRateData.getExchangeRateUSDSales(this, vars.getClient(), stradOrgId, strConvrateDate);
          if (data != null && data.length > 0) {
            ConversionRate cr = OBDal.getInstance().get(ConversionRate.class, data[0].cConversionRateId);
            cr.setDivideRateBy(BigDecimal.ONE.divide(ExchangeRateUSDSales, 15, RoundingMode.HALF_UP));
            cr.setMultipleRateBy(ExchangeRateUSDSales);
            OBDal.getInstance().save(cr);
          } else {
            ConversionRate cr = OBProvider.getInstance().get(ConversionRate.class);
            cr.setClient(crAux.getClient());
            cr.setOrganization(crAux.getOrganization());
            cr.setConversionRateType("S");
            cr.setCurrency(currUsd);
            cr.setToCurrency(currPen);
            cr.setValidFromDate(ConvrateDate);
            cr.setValidToDate(ConvrateDate);
            cr.setDivideRateBy(BigDecimal.ONE.divide(ExchangeRateUSDSales, 15, RoundingMode.HALF_UP));
            cr.setMultipleRateBy(ExchangeRateUSDSales);
            cr.setSCODescripcin("T/C Venta a Dólares");
            OBDal.getInstance().save(cr);
          }
        }

        if (ExchangeRateEURSales.compareTo(new BigDecimal(0)) > 0) {
          data = CreateNewConversionRateData.getExchangeRateEURSales(this, vars.getClient(), stradOrgId, strConvrateDate);
          if (data != null && data.length > 0) {
            ConversionRate cr = OBDal.getInstance().get(ConversionRate.class, data[0].cConversionRateId);
            cr.setDivideRateBy(BigDecimal.ONE.divide(ExchangeRateEURSales, 15, RoundingMode.HALF_UP));
            cr.setMultipleRateBy(ExchangeRateEURSales);
            OBDal.getInstance().save(cr);
          } else {
            ConversionRate cr = OBProvider.getInstance().get(ConversionRate.class);
            cr.setClient(crAux.getClient());
            cr.setOrganization(crAux.getOrganization());
            cr.setConversionRateType("S");
            cr.setCurrency(currEur);
            cr.setToCurrency(currPen);
            cr.setValidFromDate(ConvrateDate);
            cr.setValidToDate(ConvrateDate);
            cr.setDivideRateBy(BigDecimal.ONE.divide(ExchangeRateEURSales, 15, RoundingMode.HALF_UP));
            cr.setMultipleRateBy(ExchangeRateEURSales);
            cr.setSCODescripcin("T/C Venta a Euros");
            OBDal.getInstance().save(cr);
          }
        }

        if (ExchangeRateEURPurch.compareTo(new BigDecimal(0)) > 0) {
          data = CreateNewConversionRateData.getExchangeRateEURPurch(this, vars.getClient(), stradOrgId, strConvrateDate);
          if (data != null && data.length > 0) {
            ConversionRate cr = OBDal.getInstance().get(ConversionRate.class, data[0].cConversionRateId);
            cr.setDivideRateBy(ExchangeRateEURPurch);
            cr.setMultipleRateBy(BigDecimal.ONE.divide(ExchangeRateEURPurch, 15, RoundingMode.HALF_UP));
            OBDal.getInstance().save(cr);

          } else {
            ConversionRate cr = OBProvider.getInstance().get(ConversionRate.class);

            cr.setClient(crAux.getClient());
            cr.setOrganization(crAux.getOrganization());
            cr.setConversionRateType("S");
            cr.setCurrency(currPen);
            cr.setToCurrency(currEur);
            cr.setValidFromDate(ConvrateDate);
            cr.setValidToDate(ConvrateDate);
            cr.setDivideRateBy(ExchangeRateEURPurch);
            cr.setMultipleRateBy(BigDecimal.ONE.divide(ExchangeRateEURPurch, 15, RoundingMode.HALF_UP));
            cr.setSCODescripcin("T/C Compra a Euros");
            OBDal.getInstance().save(cr);
          }
        }

        if (USDExchangeRateEURSales.compareTo(new BigDecimal(0)) > 0) {
          data = CreateNewConversionRateData.getUSDExchangeRateEURSales(this, vars.getClient(), stradOrgId, strConvrateDate);
          if (data != null && data.length > 0) {
            ConversionRate cr = OBDal.getInstance().get(ConversionRate.class, data[0].cConversionRateId);
            cr.setDivideRateBy(BigDecimal.ONE.divide(USDExchangeRateEURSales, 15, RoundingMode.HALF_UP));
            cr.setMultipleRateBy(USDExchangeRateEURSales);
            OBDal.getInstance().save(cr);
          } else {
            ConversionRate cr = OBProvider.getInstance().get(ConversionRate.class);
            cr.setClient(crAux.getClient());
            cr.setOrganization(crAux.getOrganization());
            cr.setConversionRateType("S");
            cr.setCurrency(currEur);
            cr.setToCurrency(currUsd);
            cr.setValidFromDate(ConvrateDate);
            cr.setValidToDate(ConvrateDate);
            cr.setDivideRateBy(BigDecimal.ONE.divide(USDExchangeRateEURSales, 15, RoundingMode.HALF_UP));
            cr.setMultipleRateBy(USDExchangeRateEURSales);
            cr.setSCODescripcin("T/C Venta a Euros(Dólares)");
            OBDal.getInstance().save(cr);
          }
        }

        if (USDExchangeRateEURPurch.compareTo(new BigDecimal(0)) > 0) {
          data = CreateNewConversionRateData.getUSDExchangeRateEURPurch(this, vars.getClient(), stradOrgId, strConvrateDate);
          if (data != null && data.length > 0) {
            ConversionRate cr = OBDal.getInstance().get(ConversionRate.class, data[0].cConversionRateId);
            cr.setDivideRateBy(USDExchangeRateEURPurch);
            cr.setMultipleRateBy(BigDecimal.ONE.divide(USDExchangeRateEURPurch, 15, RoundingMode.HALF_UP));
            OBDal.getInstance().save(cr);

          } else {
            ConversionRate cr = OBProvider.getInstance().get(ConversionRate.class);

            cr.setClient(crAux.getClient());
            cr.setOrganization(crAux.getOrganization());
            cr.setConversionRateType("S");
            cr.setCurrency(currUsd);
            cr.setToCurrency(currEur);
            cr.setValidFromDate(ConvrateDate);
            cr.setValidToDate(ConvrateDate);
            cr.setDivideRateBy(USDExchangeRateEURPurch);
            cr.setMultipleRateBy(BigDecimal.ONE.divide(USDExchangeRateEURPurch, 15, RoundingMode.HALF_UP));
            cr.setSCODescripcin("T/C Compra a Euros(Dólares)");
            OBDal.getInstance().save(cr);
          }
        }

        OBDal.getInstance().flush();
        OBError message = new OBError();
        message.setType("Success");
        message.setTitle(Utility.parseTranslation(this, vars, vars.getLanguage(), "@OBUIAPP_Success@"));
        message.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(), "@SCO_ConversionRate_Success@"));

        vars.setMessage(strTabId, message);
        printPageClosePopUpAndRefreshParent(response, vars);

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

  private void refreshExchangeRate(HttpServletResponse response, VariablesSecureApp vars, String stradOrgId, String strConvrateDate) throws IOException, ServletException {

    final String formatOutput = vars.getSessionValue("#FormatOutput|generalQtyRelation", "#,##0.######");

    CreateNewConversionRateData[] data = null;
    String strExchangeRateUSDPurch = "0";
    String strExchangeRateUSDSales = "0";
    String strExchangeRateEURSales = "0";
    String strExchangeRateEURPurch = "0";
    String strUSDExchangeRateEURSales = "0";
    String strUSDExchangeRateEURPurch = "0";

    data = CreateNewConversionRateData.getExchangeRateUSDPurch(this, vars.getClient(), stradOrgId, strConvrateDate);
    if (data != null && data.length > 0) {
      strExchangeRateUSDPurch = data[0].convrate;
    }

    data = CreateNewConversionRateData.getExchangeRateUSDSales(this, vars.getClient(), stradOrgId, strConvrateDate);
    if (data != null && data.length > 0) {
      strExchangeRateUSDSales = data[0].convrate;
    }

    data = CreateNewConversionRateData.getExchangeRateEURSales(this, vars.getClient(), stradOrgId, strConvrateDate);
    if (data != null && data.length > 0) {
      strExchangeRateEURSales = data[0].convrate;
    }

    data = CreateNewConversionRateData.getExchangeRateEURPurch(this, vars.getClient(), stradOrgId, strConvrateDate);
    if (data != null && data.length > 0) {
      strExchangeRateEURPurch = data[0].convrate;
    }

    data = CreateNewConversionRateData.getUSDExchangeRateEURSales(this, vars.getClient(), stradOrgId, strConvrateDate);
    if (data != null && data.length > 0) {
      strUSDExchangeRateEURSales = data[0].convrate;
    }

    data = CreateNewConversionRateData.getUSDExchangeRateEURPurch(this, vars.getClient(), stradOrgId, strConvrateDate);
    if (data != null && data.length > 0) {
      strUSDExchangeRateEURPurch = data[0].convrate;
    }

    JSONObject msg = new JSONObject();
    try {
      msg.put("ExchangeRateUSDPurch", strExchangeRateUSDPurch);
      msg.put("ExchangeRateUSDSales", strExchangeRateUSDSales);
      msg.put("ExchangeRateEURSales", strExchangeRateEURSales);
      msg.put("ExchangeRateEURPurch", strExchangeRateEURPurch);
      msg.put("USDExchangeRateEURSales", strUSDExchangeRateEURSales);
      msg.put("USDExchangeRateEURPurch", strUSDExchangeRateEURPurch);
      msg.put("formatOutput", formatOutput);
    } catch (JSONException e) {
      log4j.error("JSON object error" + msg.toString());
    }
    response.setContentType("application/json; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(msg.toString());
    out.close();
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strWindowId, String strTabId, String stradOrgId, String strcConversionRateId, String strConvrateDate, String strExchangeRateUSDPurch, String strExchangeRateUSDSales, String strExchangeRateEURSales, String strExchangeRateEURPurch, String strUSDExchangeRateEURSales, String strUSDExchangeRateEURPurch) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    XmlDocument xmlDocument = null;

    String discard[] = { "secTable" };
    xmlDocument = xmlEngine.readXmlTemplate("pe/com/unifiedgo/accounting/ad_actionbutton/CreateNewConversionRate", discard).createXmlDocument();

    OBError myMessage = vars.getMessage("CreateNewConversionRate");
    vars.removeMessage("CreateNewConversionRate");
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

    xmlDocument.setParameter("ConvrateDate", strConvrateDate);
    xmlDocument.setParameter("ConvrateDatedisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("ConvrateDatesaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

    xmlDocument.setParameter("ExchangeRateUSDPurch", strExchangeRateUSDPurch);
    xmlDocument.setParameter("ExchangeRateUSDSales", strExchangeRateUSDSales);
    xmlDocument.setParameter("ExchangeRateEURSales", strExchangeRateEURSales);
    xmlDocument.setParameter("ExchangeRateEURPurch", strExchangeRateEURPurch);
    xmlDocument.setParameter("USDExchangeRateEURSales", strUSDExchangeRateEURSales);
    xmlDocument.setParameter("USDExchangeRateEURPurch", strUSDExchangeRateEURPurch);
    xmlDocument.setParameter("cConversionRateId", strcConversionRateId);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

}