package pe.com.unifiedgo.accounting.ad_actionbutton;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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

import pe.com.unifiedgo.accounting.data.SCOEndperiodconvRate;
import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class CreateNewEPConversionRate extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static String strBDErrorMessage = "";
  private AdvPaymentMngtDao dao;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    int conversionRatePrecision = FIN_Utility.getConversionRatePrecision(vars);

    if (vars.commandIn("DEFAULT")) {
      String stradOrgId = vars.getGlobalVariable("inpadOrgId", "CreateNewEPConversionRate|AD_Org_ID", "");
      String strscoEndperiodconvRateId = vars.getGlobalVariable("inpscoEndperiodconvRateId", "CreateNewEPConversionRate|SCOEndperiodconvRateId", "");
      String strConvrateDate = vars.getGlobalVariable("inpConvrateDate", "CreateNewEPConversionRate|ConvrateDate", SREDateTimeData.today(this));
      String strEPExchangeRateUSDPurch = vars.getGlobalVariable("inpEPExchangeRateUSDPurch", "CreateNewEPConversionRate|EPExchangeRateUSDPuch", "0");
      String strEPExchangeRateUSDSales = vars.getGlobalVariable("inpEPExchangeRateUSDSales", "CreateNewEPConversionRate|EPExchangeRateUSDSales", "0");
      String strEPExchangeRateEURSales = vars.getGlobalVariable("inpEPExchangeRateEURSales", "CreateNewEPConversionRate|EPExchangeRateEURSales", "0");
      String strEPExchangeRateEURPurch = vars.getGlobalVariable("inpEPExchangeRateEURPurch", "CreateNewEPConversionRate|EPExchangeRateEURPurch", "0");
      String strTabId = vars.getGlobalVariable("inpTabId", "CreateNewEPConversionRate|Tab_ID");
      String strWindowId = vars.getGlobalVariable("inpwindowId", "CreateNewEPConversionRate|Window_ID");

      CreateNewEPConversionRateData[] data = null;

      data = CreateNewEPConversionRateData.getEPExchangeRateUSDPurch(this, vars.getClient(), stradOrgId, strConvrateDate);
      if (data != null && data.length > 0) {
        strEPExchangeRateUSDPurch = data[0].convrate;
      } else {
        strEPExchangeRateUSDPurch = "0";
      }

      data = CreateNewEPConversionRateData.getEPExchangeRateUSDSales(this, vars.getClient(), stradOrgId, strConvrateDate);
      if (data != null && data.length > 0) {
        strEPExchangeRateUSDSales = data[0].convrate;
      } else {
        strEPExchangeRateUSDSales = "0";
      }

      data = CreateNewEPConversionRateData.getEPExchangeRateEURSales(this, vars.getClient(), stradOrgId, strConvrateDate);
      if (data != null && data.length > 0) {
        strEPExchangeRateEURSales = data[0].convrate;
      } else {
        strEPExchangeRateEURSales = "0";
      }
      
      data = CreateNewEPConversionRateData.getEPExchangeRateEURPurch(this, vars.getClient(), stradOrgId, strConvrateDate);
      if (data != null && data.length > 0) {
        strEPExchangeRateEURPurch = data[0].convrate;
      } else {
        strEPExchangeRateEURPurch = "0";
      }

      printPageDataSheet(response, vars, strWindowId, strTabId, stradOrgId, strscoEndperiodconvRateId, strConvrateDate, strEPExchangeRateUSDPurch, strEPExchangeRateUSDSales, strEPExchangeRateEURSales, strEPExchangeRateEURPurch);

    } else if (vars.commandIn("EXCHANGERATE")) {

      String stradOrgId = vars.getGlobalVariable("inpadOrgId", "");
      String strConvrateDate = vars.getGlobalVariable("inpConvrateDate", "");

      refreshExchangeRate(response, vars, stradOrgId, strConvrateDate);

    } else if (vars.commandIn("SAVE")) {

      String stradOrgId = vars.getRequiredStringParameter("inpadOrgId");
      String strscoEndperiodconvRateId = vars.getRequiredStringParameter("inpscoEndperiodconvRateId");
      String strConvrateDate = vars.getRequiredStringParameter("inpConvrateDate");
      String strEPExchangeRateUSDPurch = vars.getNumericParameter("inpEPExchangeRateUSDPurch", "0");
      String strEPExchangeRateUSDSales = vars.getNumericParameter("inpEPExchangeRateUSDSales", "0");
      String strEPExchangeRateEURSales = vars.getNumericParameter("inpEPExchangeRateEURSales", "0");
      String strEPExchangeRateEURPurch = vars.getNumericParameter("inpEPExchangeRateEURPurch", "0");
      String strTabId = vars.getRequiredStringParameter("inpTabId");
      String strWindowId = vars.getRequiredStringParameter("inpadWindowId");

      String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty("dateFormat.java");
      SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_JAVA);
      DecimalFormat dfQty = Utility.getFormat(vars, "generalQtyEdition");
      dfQty.setRoundingMode(RoundingMode.HALF_UP);

      Date ConvrateDate = null;
      BigDecimal EPExchangeRateUSDPurch = null;
      BigDecimal EPExchangeRateUSDSales = null;
      BigDecimal EPExchangeRateEURSales = null;
      BigDecimal EPExchangeRateEURPurch = null;

      try {
        ConvrateDate = sdf.parse(strConvrateDate);
        EPExchangeRateUSDPurch = new BigDecimal(dfQty.parse(strEPExchangeRateUSDPurch).toString());
        EPExchangeRateUSDSales = new BigDecimal(dfQty.parse(strEPExchangeRateUSDSales).toString());
        EPExchangeRateEURSales = new BigDecimal(dfQty.parse(strEPExchangeRateEURSales).toString());
        EPExchangeRateEURPurch = new BigDecimal(dfQty.parse(strEPExchangeRateEURPurch).toString());
        
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

        CreateNewEPConversionRateData data[] = null;
        SCOEndperiodconvRate crAux = OBDal.getInstance().get(SCOEndperiodconvRate.class, strscoEndperiodconvRateId);
        Currency currUsd = OBDal.getInstance().get(Currency.class, "100");
        Currency currPen = OBDal.getInstance().get(Currency.class, "308");
        Currency currEur = OBDal.getInstance().get(Currency.class, "102");

        if (EPExchangeRateUSDPurch.compareTo(new BigDecimal(0)) > 0) {
          data = CreateNewEPConversionRateData.getEPExchangeRateUSDPurch(this, vars.getClient(), stradOrgId, strConvrateDate);
          if (data != null && data.length > 0) {
            SCOEndperiodconvRate cr = OBDal.getInstance().get(SCOEndperiodconvRate.class, data[0].scoEndperiodconvRateId);
            cr.setDivideRateBy(EPExchangeRateUSDPurch);
            cr.setMultipleRateBy(BigDecimal.ONE.divide(EPExchangeRateUSDPurch, 15, RoundingMode.HALF_UP));
            OBDal.getInstance().save(cr);

          } else {
            SCOEndperiodconvRate cr = OBProvider.getInstance().get(SCOEndperiodconvRate.class);

            cr.setClient(crAux.getClient());
            cr.setOrganization(crAux.getOrganization());
            cr.setConversionRateType("S");
            cr.setCurrency(currPen);
            cr.setToCurrency(currUsd);
            cr.setValidFromDate(getBeginingofMonth(ConvrateDate));
            cr.setValidToDate(getEndofMonth(ConvrateDate));
            cr.setDivideRateBy(EPExchangeRateUSDPurch);
            cr.setMultipleRateBy(BigDecimal.ONE.divide(EPExchangeRateUSDPurch, 15, RoundingMode.HALF_UP));
            cr.setDescription("T/C Compra a Dólares");
            OBDal.getInstance().save(cr);
          }
        }

        if (EPExchangeRateUSDSales.compareTo(new BigDecimal(0)) > 0) {
          data = CreateNewEPConversionRateData.getEPExchangeRateUSDSales(this, vars.getClient(), stradOrgId, strConvrateDate);
          if (data != null && data.length > 0) {
            SCOEndperiodconvRate cr = OBDal.getInstance().get(SCOEndperiodconvRate.class, data[0].scoEndperiodconvRateId);
            cr.setDivideRateBy(BigDecimal.ONE.divide(EPExchangeRateUSDSales, 15, RoundingMode.HALF_UP));
            cr.setMultipleRateBy(EPExchangeRateUSDSales);
            OBDal.getInstance().save(cr);
          } else {
            SCOEndperiodconvRate cr = OBProvider.getInstance().get(SCOEndperiodconvRate.class);
            cr.setClient(crAux.getClient());
            cr.setOrganization(crAux.getOrganization());
            cr.setConversionRateType("S");
            cr.setCurrency(currUsd);
            cr.setToCurrency(currPen);
            cr.setValidFromDate(getBeginingofMonth(ConvrateDate));
            cr.setValidToDate(getEndofMonth(ConvrateDate));
            cr.setDivideRateBy(BigDecimal.ONE.divide(EPExchangeRateUSDSales, 15, RoundingMode.HALF_UP));
            cr.setMultipleRateBy(EPExchangeRateUSDSales);
            cr.setDescription("T/C Venta a Dólares");
            OBDal.getInstance().save(cr);
          }
        }

        if (EPExchangeRateEURSales.compareTo(new BigDecimal(0)) > 0) {
          data = CreateNewEPConversionRateData.getEPExchangeRateEURSales(this, vars.getClient(), stradOrgId, strConvrateDate);
          if (data != null && data.length > 0) {
            SCOEndperiodconvRate cr = OBDal.getInstance().get(SCOEndperiodconvRate.class, data[0].scoEndperiodconvRateId);
            cr.setDivideRateBy(BigDecimal.ONE.divide(EPExchangeRateEURSales, 15, RoundingMode.HALF_UP));
            cr.setMultipleRateBy(EPExchangeRateEURSales);
            OBDal.getInstance().save(cr);
          } else {
            SCOEndperiodconvRate cr = OBProvider.getInstance().get(SCOEndperiodconvRate.class);
            cr.setClient(crAux.getClient());
            cr.setOrganization(crAux.getOrganization());
            cr.setConversionRateType("S");
            cr.setCurrency(currEur);
            cr.setToCurrency(currPen);
            cr.setValidFromDate(getBeginingofMonth(ConvrateDate));
            cr.setValidToDate(getEndofMonth(ConvrateDate));
            cr.setDivideRateBy(BigDecimal.ONE.divide(EPExchangeRateEURSales, 15, RoundingMode.HALF_UP));
            cr.setMultipleRateBy(EPExchangeRateEURSales);
            cr.setDescription("T/C Venta a Euros");
            OBDal.getInstance().save(cr);
          }
        }
        
        if (EPExchangeRateEURPurch.compareTo(new BigDecimal(0)) > 0) {
            data = CreateNewEPConversionRateData.getEPExchangeRateEURPurch(this, vars.getClient(), stradOrgId, strConvrateDate);
            if (data != null && data.length > 0) {
              SCOEndperiodconvRate cr = OBDal.getInstance().get(SCOEndperiodconvRate.class, data[0].scoEndperiodconvRateId);
              cr.setDivideRateBy(EPExchangeRateEURPurch);
              cr.setMultipleRateBy(BigDecimal.ONE.divide(EPExchangeRateEURPurch, 15, RoundingMode.HALF_UP));
              OBDal.getInstance().save(cr);

            } else {
              SCOEndperiodconvRate cr = OBProvider.getInstance().get(SCOEndperiodconvRate.class);

              cr.setClient(crAux.getClient());
              cr.setOrganization(crAux.getOrganization());
              cr.setConversionRateType("S");
              cr.setCurrency(currPen);
              cr.setToCurrency(currEur);
              cr.setValidFromDate(getBeginingofMonth(ConvrateDate));
              cr.setValidToDate(getEndofMonth(ConvrateDate));
              cr.setDivideRateBy(EPExchangeRateEURPurch);
              cr.setMultipleRateBy(BigDecimal.ONE.divide(EPExchangeRateEURPurch, 15, RoundingMode.HALF_UP));
              cr.setDescription("T/C Compra a Euros");
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

    CreateNewEPConversionRateData[] data = null;
    String strEPExchangeRateUSDPurch = "0";
    String strEPExchangeRateUSDSales = "0";
    String strEPExchangeRateEURSales = "0";
    String strEPExchangeRateEURPurch = "0";

    data = CreateNewEPConversionRateData.getEPExchangeRateUSDPurch(this, vars.getClient(), stradOrgId, strConvrateDate);
    if (data != null && data.length > 0) {
      strEPExchangeRateUSDPurch = data[0].convrate;
    }

    data = CreateNewEPConversionRateData.getEPExchangeRateUSDSales(this, vars.getClient(), stradOrgId, strConvrateDate);
    if (data != null && data.length > 0) {
      strEPExchangeRateUSDSales = data[0].convrate;
    }

    data = CreateNewEPConversionRateData.getEPExchangeRateEURSales(this, vars.getClient(), stradOrgId, strConvrateDate);
    if (data != null && data.length > 0) {
      strEPExchangeRateEURSales = data[0].convrate;
    }
    
    data = CreateNewEPConversionRateData.getEPExchangeRateEURPurch(this, vars.getClient(), stradOrgId, strConvrateDate);
    if (data != null && data.length > 0) {
      strEPExchangeRateEURPurch = data[0].convrate;
    }

    JSONObject msg = new JSONObject();
    try {
      msg.put("EPExchangeRateUSDPurch", strEPExchangeRateUSDPurch);
      msg.put("EPExchangeRateUSDSales", strEPExchangeRateUSDSales);
      msg.put("EPExchangeRateEURSales", strEPExchangeRateEURSales);
      msg.put("EPExchangeRateEURPurch", strEPExchangeRateEURPurch);
      msg.put("formatOutput", formatOutput);
    } catch (JSONException e) {
      log4j.error("JSON object error" + msg.toString());
    }
    response.setContentType("application/json; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(msg.toString());
    out.close();
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strWindowId, String strTabId, String stradOrgId, String strscoEndperiodconvRateId, String strConvrateDate, String strEPExchangeRateUSDPurch, String strEPExchangeRateUSDSales, String strEPExchangeRateEURSales, String strEPExchangeRateEURPurch) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    XmlDocument xmlDocument = null;

    String discard[] = { "secTable" };
    xmlDocument = xmlEngine.readXmlTemplate("pe/com/unifiedgo/accounting/ad_actionbutton/CreateNewEPConversionRate", discard).createXmlDocument();

    OBError myMessage = vars.getMessage("CreateNewEPConversionRate");
    vars.removeMessage("CreateNewEPConversionRate");
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

    xmlDocument.setParameter("EPExchangeRateUSDPurch", strEPExchangeRateUSDPurch);
    xmlDocument.setParameter("EPExchangeRateUSDSales", strEPExchangeRateUSDSales);
    xmlDocument.setParameter("EPExchangeRateEURSales", strEPExchangeRateEURSales);
    xmlDocument.setParameter("EPExchangeRateEURPurch", strEPExchangeRateEURPurch);
    xmlDocument.setParameter("scoEndperiodconvRateId", strscoEndperiodconvRateId);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public static Date getBeginingofMonth(Date date) {
    Calendar calendar = GregorianCalendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
    setTimeToBeginningOfDay(calendar);
    return calendar.getTime();
  }

  public static Date getEndofMonth(Date date) {
    Calendar calendar = GregorianCalendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
    setTimeToBeginningOfDay(calendar);
    return calendar.getTime();
  }

  private static void setTimeToBeginningOfDay(Calendar calendar) {
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
  }

  private static void setTimeToEndofDay(Calendar calendar) {
    calendar.set(Calendar.HOUR_OF_DAY, 23);
    calendar.set(Calendar.MINUTE, 59);
    calendar.set(Calendar.SECOND, 59);
    calendar.set(Calendar.MILLISECOND, 999);
  }

}