package pe.com.unifiedgo.sales.ad_actionButton;

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

public class VoidSODocuments extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static String strBDErrorMessage = "";
  private AdvPaymentMngtDao dao;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    int conversionRatePrecision = FIN_Utility.getConversionRatePrecision(vars);

    if (vars.commandIn("DEFAULT")) {
      String stradOrgId = vars.getGlobalVariable("inpadOrgId", "VoidSODocuments|AD_Org_ID", "");
      String strcConversionRateId = vars.getGlobalVariable("inpcConversionRateId", "VoidSODocuments|CConversionRateId", "");
      String strConvrateDate = vars.getGlobalVariable("inpConvrateDate", "VoidSODocuments|ConvrateDate", SREDateTimeData.today(this));
      String strExchangeRateUSDPurch = vars.getGlobalVariable("inpExchangeRateUSDPurch", "VoidSODocuments|ExchangeRateUSDPuch", "0");
      String strExchangeRateUSDSales = vars.getGlobalVariable("inpExchangeRateUSDSales", "VoidSODocuments|ExchangeRateUSDSales", "0");
      String strExchangeRateEURSales = vars.getGlobalVariable("inpExchangeRateEURSales", "VoidSODocuments|ExchangeRateEURSales", "0");
      String strExchangeRateEURPurch = vars.getGlobalVariable("inpExchangeRateEURPurch", "VoidSODocuments|ExchangeRateEURPurch", "0");
      String strTabId = vars.getGlobalVariable("inpTabId", "VoidSODocuments|Tab_ID");
      String strWindowId = vars.getGlobalVariable("inpwindowId", "VoidSODocuments|Window_ID");

      printPageDataSheet(response, vars, strWindowId, strTabId, stradOrgId, strcConversionRateId, strConvrateDate, strExchangeRateUSDPurch, strExchangeRateUSDSales, strExchangeRateEURSales, strExchangeRateEURPurch);

    } else if (vars.commandIn("EXCHANGERATE")) {

      String stradOrgId = vars.getGlobalVariable("inpadOrgId", "");
      String strConvrateDate = vars.getGlobalVariable("inpConvrateDate", "");


    } else if (vars.commandIn("SAVE")) {

      String stradOrgId = vars.getRequiredStringParameter("inpadOrgId");
      String strcConversionRateId = vars.getRequiredStringParameter("inpcConversionRateId");
      String strConvrateDate = vars.getRequiredStringParameter("inpConvrateDate");
      String strExchangeRateUSDPurch = vars.getNumericParameter("inpExchangeRateUSDPurch", "0");
      String strExchangeRateUSDSales = vars.getNumericParameter("inpExchangeRateUSDSales", "0");
      String strExchangeRateEURSales = vars.getNumericParameter("inpExchangeRateEURSales", "0");
      String strExchangeRateEURPurch = vars.getNumericParameter("inpExchangeRateEURPurch", "0");
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

     


    }
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strWindowId, String strTabId, String stradOrgId, String strcConversionRateId, String strConvrateDate, String strExchangeRateUSDPurch, String strExchangeRateUSDSales, String strExchangeRateEURSales, String strExchangeRateEURPurch) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    XmlDocument xmlDocument = null;

    String discard[] = { "secTable" };
    xmlDocument = xmlEngine.readXmlTemplate("pe/com/unifiedgo/sales/ad_actionButton/VoidSODocuments", discard).createXmlDocument();

    OBError myMessage = vars.getMessage("VoidSODocuments");
    vars.removeMessage("VoidSODocuments");
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
    xmlDocument.setParameter("cConversionRateId", strcConversionRateId);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

}