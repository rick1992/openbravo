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
import org.openbravo.model.common.currency.Currency;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.accounting.data.SCOParallelconvRate;
import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class CreateNewParallelConversionRate extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static String strBDErrorMessage = "";
  private AdvPaymentMngtDao dao;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    int conversionRatePrecision = FIN_Utility.getConversionRatePrecision(vars);

    if (vars.commandIn("DEFAULT")) {
      String stradOrgId = vars.getGlobalVariable("inpadOrgId", "CreateNewParallelConversionRate|AD_Org_ID", "");
      String strscoParallelconvRateId = vars.getGlobalVariable("inpscoParallelconvRateId", "CreateNewParallelConversionRate|CConversionRateId", "");
      String strConvrateDate = vars.getGlobalVariable("inpConvrateDate", "CreateNewParallelConversionRate|ConvrateDate", SREDateTimeData.today(this));
      String strExchangeRateUSDSales = vars.getGlobalVariable("inpExchangeRateUSDSales", "CreateNewParallelConversionRate|ExchangeRateUSDSales", "0");
      String strParallelExchangeRateUSDSales = vars.getGlobalVariable("inpParallelExchangeRateUSDSales", "CreateNewParallelConversionRate|ParallelExchangeRateUSDSales", "0");
      String strTabId = vars.getGlobalVariable("inpTabId", "CreateNewParallelConversionRate|Tab_ID");
      String strWindowId = vars.getGlobalVariable("inpwindowId", "CreateNewParallelConversionRate|Window_ID");

      CreateNewParallelConversionRateData[] pdata = null;
      CreateNewConversionRateData[] data = null;

      data = CreateNewConversionRateData.getExchangeRateUSDSales(this, vars.getClient(), stradOrgId, strConvrateDate);
      if (data != null && data.length > 0) {
        strExchangeRateUSDSales = data[0].convrate;
      } else {
        strExchangeRateUSDSales = "0";
      }

      pdata = CreateNewParallelConversionRateData.getParallelExchangeRateUSDSales(this, vars.getClient(), stradOrgId, strConvrateDate);
      if (pdata != null && pdata.length > 0) {
        strParallelExchangeRateUSDSales = pdata[0].convrate;
      } else {
        strParallelExchangeRateUSDSales = "0";
      }

      printPageDataSheet(response, vars, strWindowId, strTabId, stradOrgId, strscoParallelconvRateId, strConvrateDate, strExchangeRateUSDSales, strParallelExchangeRateUSDSales);

    } else if (vars.commandIn("EXCHANGERATE")) {

      String stradOrgId = vars.getGlobalVariable("inpadOrgId", "");
      String strConvrateDate = vars.getGlobalVariable("inpConvrateDate", "");

      refreshExchangeRate(response, vars, stradOrgId, strConvrateDate);

    } else if (vars.commandIn("SAVE")) {

      String stradOrgId = vars.getRequiredStringParameter("inpadOrgId");
      String strscoParallelconvRateId = vars.getRequiredStringParameter("inpscoParallelconvRateId");
      String strConvrateDate = vars.getRequiredStringParameter("inpConvrateDate");
      String strExchangeRateUSDSales = vars.getNumericParameter("inpExchangeRateUSDSales", "0");
      String strParallelExchangeRateUSDSales = vars.getNumericParameter("inpParallelExchangeRateUSDSales", "0");
      String strTabId = vars.getRequiredStringParameter("inpTabId");
      String strWindowId = vars.getRequiredStringParameter("inpadWindowId");

      String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty("dateFormat.java");
      SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_JAVA);
      DecimalFormat dfQty = Utility.getFormat(vars, "generalQtyEdition");
      dfQty.setRoundingMode(RoundingMode.HALF_UP);

      Date ConvrateDate = null;
      BigDecimal ExchangeRateUSDSales = null;
      BigDecimal ParallelExchangeRateUSDSales = null;

      try {
        ConvrateDate = sdf.parse(strConvrateDate);
        ExchangeRateUSDSales = new BigDecimal(dfQty.parse(strExchangeRateUSDSales).toString());
        ParallelExchangeRateUSDSales = new BigDecimal(dfQty.parse(strParallelExchangeRateUSDSales).toString());

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

        CreateNewParallelConversionRateData pdata[] = null;
        SCOParallelconvRate crAux = OBDal.getInstance().get(SCOParallelconvRate.class, strscoParallelconvRateId);
        Currency currUsd = OBDal.getInstance().get(Currency.class, "100");
        Currency currPen = OBDal.getInstance().get(Currency.class, "308");
        Currency currEur = OBDal.getInstance().get(Currency.class, "102");

        if (ParallelExchangeRateUSDSales.compareTo(new BigDecimal(0)) > 0) {
          pdata = CreateNewParallelConversionRateData.getParallelExchangeRateUSDSales(this, vars.getClient(), stradOrgId, strConvrateDate);
          if (pdata != null && pdata.length > 0) {
            SCOParallelconvRate cr = OBDal.getInstance().get(SCOParallelconvRate.class, pdata[0].scoParallelconvRateId);
            cr.setDivideRateBy(BigDecimal.ONE.divide(ParallelExchangeRateUSDSales, 15, RoundingMode.HALF_UP));
            cr.setMultipleRateBy(ParallelExchangeRateUSDSales);
            OBDal.getInstance().save(cr);
          } else {
            SCOParallelconvRate cr = OBProvider.getInstance().get(SCOParallelconvRate.class);
            cr.setClient(crAux.getClient());
            cr.setOrganization(crAux.getOrganization());
            cr.setConversionRateType("S");
            cr.setCurrency(currUsd);
            cr.setToCurrency(currPen);
            cr.setValidFromDate(ConvrateDate);
            cr.setValidToDate(ConvrateDate);
            cr.setDivideRateBy(BigDecimal.ONE.divide(ParallelExchangeRateUSDSales, 15, RoundingMode.HALF_UP));
            cr.setMultipleRateBy(ParallelExchangeRateUSDSales);
            cr.setDescription("T/C Venta a Dólares");
            OBDal.getInstance().save(cr);
          }

          // COPY TO THE PARALLEL PURCHASE
          pdata = CreateNewParallelConversionRateData.getParallelExchangeRateUSDPurch(this, vars.getClient(), stradOrgId, strConvrateDate);
          if (pdata != null && pdata.length > 0) {
            SCOParallelconvRate cr = OBDal.getInstance().get(SCOParallelconvRate.class, pdata[0].scoParallelconvRateId);
            cr.setDivideRateBy(ParallelExchangeRateUSDSales);
            cr.setMultipleRateBy(BigDecimal.ONE.divide(ParallelExchangeRateUSDSales, 15, RoundingMode.HALF_UP));
            OBDal.getInstance().save(cr);
          } else {
            SCOParallelconvRate cr = OBProvider.getInstance().get(SCOParallelconvRate.class);
            cr.setClient(crAux.getClient());
            cr.setOrganization(crAux.getOrganization());
            cr.setConversionRateType("S");
            cr.setCurrency(currPen);
            cr.setToCurrency(currUsd);
            cr.setValidFromDate(ConvrateDate);
            cr.setValidToDate(ConvrateDate);
            cr.setDivideRateBy(ParallelExchangeRateUSDSales);
            cr.setMultipleRateBy(BigDecimal.ONE.divide(ParallelExchangeRateUSDSales, 15, RoundingMode.HALF_UP));
            cr.setDescription("T/C Compra a Dólares");
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

    CreateNewParallelConversionRateData[] pdata = null;
    CreateNewConversionRateData[] data = null;

    String strExchangeRateUSDSales = "0";
    String strParallelExchangeRateUSDSales = "0";

    data = CreateNewConversionRateData.getExchangeRateUSDSales(this, vars.getClient(), stradOrgId, strConvrateDate);
    if (data != null && data.length > 0) {
      strExchangeRateUSDSales = data[0].convrate;
    }

    pdata = CreateNewParallelConversionRateData.getParallelExchangeRateUSDSales(this, vars.getClient(), stradOrgId, strConvrateDate);
    if (pdata != null && pdata.length > 0) {
      strParallelExchangeRateUSDSales = pdata[0].convrate;
    }

    JSONObject msg = new JSONObject();
    try {
      msg.put("ExchangeRateUSDSales", strExchangeRateUSDSales);
      msg.put("ParallelExchangeRateUSDSales", strParallelExchangeRateUSDSales);

      msg.put("formatOutput", formatOutput);
    } catch (JSONException e) {
      log4j.error("JSON object error" + msg.toString());
    }
    response.setContentType("application/json; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(msg.toString());
    out.close();
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strWindowId, String strTabId, String stradOrgId, String strscoParallelconvRateId, String strConvrateDate, String strExchangeRateUSDSales, String strParallelExchangeRateUSDSales) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    XmlDocument xmlDocument = null;

    String discard[] = { "secTable" };
    xmlDocument = xmlEngine.readXmlTemplate("pe/com/unifiedgo/accounting/ad_actionbutton/CreateNewParallelConversionRate", discard).createXmlDocument();

    OBError myMessage = vars.getMessage("CreateNewParallelConversionRate");
    vars.removeMessage("CreateNewParallelConversionRate");
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

    xmlDocument.setParameter("ExchangeRateUSDSales", strExchangeRateUSDSales);
    xmlDocument.setParameter("ParallelExchangeRateUSDSales", strParallelExchangeRateUSDSales);
    xmlDocument.setParameter("scoParallelconvRateId", strscoParallelconvRateId);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

}