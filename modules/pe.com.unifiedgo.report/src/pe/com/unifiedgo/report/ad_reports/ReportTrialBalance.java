/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2001-2013 Openbravo SLU 
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package pe.com.unifiedgo.report.ad_reports;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.businessUtility.AccountingSchemaMiscData;
import org.openbravo.erpCommon.businessUtility.Tree;
import org.openbravo.erpCommon.businessUtility.TreeData;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SQLReturnObject;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchema;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.accounting.StructPle;
import pe.com.unifiedgo.accounting.SunatUtil;
import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class ReportTrialBalance extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strcAcctSchemaId = vars.getGlobalVariable("inpcAcctSchemaId",
          "ReportTrialBalance|cAcctSchemaId", "");
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReportTrialBalance|DateFrom",
          SREDateTimeData.FirstDayOfMonth(this));
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportTrialBalance|DateTo",
          SREDateTimeData.today(this));
      String strPageNo = vars.getGlobalVariable("inpPageNo", "ReportTrialBalance|PageNo", "1");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportTrialBalance|Org", "");
      // String strLevel = vars.getGlobalVariable("inpLevel",
      // "ReportTrialBalance|Level", "");
      String strDigitNumbers = vars.getGlobalVariable("inpDigitNumbers",
          "ReportTrialBalance|DigitNumbers", "");
      String strcElementValueFrom = vars.getGlobalVariable("inpcElementValueIdFrom",
          "ReportTrialBalance|C_ElementValue_IDFROM", "");
      String strcElementValueTo = vars.getGlobalVariable("inpcElementValueIdTo",
          "ReportTrialBalance|C_ElementValue_IDTO",
          ReportTrialBalanceData.selectLastAccount(this,
              Utility.getContext(this, vars, "#AccessibleOrgTree", "Account"),
              Utility.getContext(this, vars, "#User_Client", "Account")));
      String strNotInitialBalance = vars.getGlobalVariable("inpNotInitialBalance",
          "ReportTrialBalance|notInitialBalance", "Y");
      String strPeriodo = vars.getGlobalVariable("inpPeriodo", "ReportTrialBalance|PeriodoCmbId",
          "");
      String strcElementValueFromDes = "", strcElementValueToDes = "";
      if (!strcElementValueFrom.equals(""))
        strcElementValueFromDes = ReportTrialBalanceData.selectSubaccountDescription(this,
            strcElementValueFrom);
      if (!strcElementValueTo.equals(""))
        strcElementValueToDes = ReportTrialBalanceData.selectSubaccountDescription(this,
            strcElementValueTo);
      strcElementValueFromDes = (strcElementValueFromDes == null) ? "" : strcElementValueFromDes;
      strcElementValueToDes = (strcElementValueToDes == null) ? "" : strcElementValueToDes;
      vars.setSessionValue("inpElementValueIdFrom_DES", strcElementValueFromDes);
      vars.setSessionValue("inpElementValueIdTo_DES", strcElementValueToDes);

      String strcBpartnerId = "";
      String strmProductId = "";
      String strcProjectId = "";
      String strGroupBy = "p";

      printPageDataSheet(request, response, vars, strDateFrom, strDateTo, strPageNo, strOrg, "",
          strDigitNumbers, strcElementValueFrom, strcElementValueTo, strcElementValueFromDes,
          strcElementValueToDes, strcBpartnerId, strmProductId, strcProjectId, strcAcctSchemaId,
          strNotInitialBalance, strGroupBy, strPeriodo);

    } else if (vars.commandIn("PDF", "XLS")) {
      String strcAcctSchemaId = vars.getRequestGlobalVariable("inpcAcctSchemaId",
          "ReportTrialBalance|cAcctSchemaId");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportTrialBalance|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportTrialBalance|DateTo");
      String strOrg = vars.getRequestGlobalVariable("inpOrg", "ReportTrialBalance|Org");
      // String strLevel = vars.getRequestGlobalVariable("inpLevel",
      // "ReportTrialBalance|Level");
      String strDigitNumbers = vars.getGlobalVariable("inpDigitNumbers",
          "ReportTrialBalance|DigitNumbers", "");
      String strcElementValueFrom = vars.getGlobalVariable("inpcElementValueIdFrom",
          "ReportTrialBalance|C_ElementValue_IDFROM", "");
      String strcElementValueTo = vars.getGlobalVariable("inpcElementValueIdTo",
          "ReportTrialBalance|C_ElementValue_IDTO", "");
      String strcElementValueFromDes = "", strcElementValueToDes = "";
      if (!strcElementValueFrom.equals(""))
        strcElementValueFromDes = ReportTrialBalanceData.selectSubaccountDescription(this,
            strcElementValueFrom);
      if (!strcElementValueTo.equals(""))
        strcElementValueToDes = ReportTrialBalanceData.selectSubaccountDescription(this,
            strcElementValueTo);
      strcElementValueFromDes = (strcElementValueFromDes == null) ? "" : strcElementValueFromDes;
      strcElementValueToDes = (strcElementValueToDes == null) ? "" : strcElementValueToDes;
      String strPageNo = vars.getRequestGlobalVariable("inpPageNo", "ReportTrialBalance|PageNo");
      String strNotInitialBalance = vars.getStringParameter("inpNotInitialBalance", "N");
      vars.setSessionValue("ReportTrialBalance|notInitialBalance", strNotInitialBalance);

      String strcBpartnerId = "";
      String strmProductId = "";
      String strcProjectId = "";
      String strGroupBy = "";

      printPageDataPDF(request, response, vars, strDateFrom, strDateTo, strOrg, "", strDigitNumbers,
          strcElementValueFrom, strcElementValueFromDes, strcElementValueTo, strcElementValueToDes,
          strcBpartnerId, strmProductId, strcProjectId, strcAcctSchemaId, strNotInitialBalance,
          strGroupBy, strPageNo);

      // if (vars.commandIn("PDF"))
      // printPageDataPDF(request, response, vars, strDateFrom, strDateTo,
      // strOrg, "",
      // strDigitNumbers, strcElementValueFrom, strcElementValueFromDes,
      // strcElementValueTo,
      // strcElementValueToDes, strcBpartnerId, strmProductId,
      // strcProjectId, strcAcctSchemaId,
      // strNotInitialBalance, strGroupBy, strPageNo);
      // else
      // printPageDataXLS(request, response, vars, strDateFrom, strDateTo,
      // strOrg, "",
      // strDigitNumbers, strcElementValueFrom, strcElementValueTo,
      // strcBpartnerId,
      // strmProductId, strcProjectId, strcAcctSchemaId,
      // strNotInitialBalance, strGroupBy);

    } else if (vars.commandIn("FIND")) {
      String strcAcctSchemaId = vars.getRequestGlobalVariable("inpcAcctSchemaId",
          "ReportTrialBalance|cAcctSchemaId");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportTrialBalance|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportTrialBalance|DateTo");
      String strOrg = vars.getRequestGlobalVariable("inpOrg", "ReportTrialBalance|Org");
      // String strLevel = vars.getRequestGlobalVariable("inpLevel",
      // "ReportTrialBalance|Level");
      String strDigitNumbers = vars.getGlobalVariable("inpDigitNumbers",
          "ReportTrialBalance|DigitNumbers", "");
      String strcElementValueFrom = vars.getGlobalVariable("inpcElementValueIdFrom",
          "ReportTrialBalance|C_ElementValue_IDFROM", "");
      System.out.println("strcElementValueFrom:" + strcElementValueFrom);
      String strcElementValueTo = vars.getGlobalVariable("inpcElementValueIdTo",
          "ReportTrialBalance|C_ElementValue_IDTO", "");

      String strPeriodo = vars.getStringParameter("inpPeriodo", "");

      String strcElementValueFromDes = "", strcElementValueToDes = "";
      if (!strcElementValueFrom.equals(""))
        strcElementValueFromDes = ReportTrialBalanceData.selectSubaccountDescription(this,
            strcElementValueFrom);
      if (!strcElementValueTo.equals(""))
        strcElementValueToDes = ReportTrialBalanceData.selectSubaccountDescription(this,
            strcElementValueTo);
      strcElementValueFromDes = (strcElementValueFromDes == null) ? "" : strcElementValueFromDes;
      strcElementValueToDes = (strcElementValueToDes == null) ? "" : strcElementValueToDes;
      String strPageNo = vars.getRequestGlobalVariable("inpPageNo", "ReportTrialBalance|PageNo");

      String strNotInitialBalance = vars.getStringParameter("inpNotInitialBalance", "N");

      vars.setSessionValue("ReportTrialBalance|notInitialBalance", strNotInitialBalance);

      String strcBpartnerId = "";
      String strmProductId = "";
      String strcProjectId = "";
      String strGroupBy = "";

      printPageDataSheet(request, response, vars, strDateFrom, strDateTo, strPageNo, strOrg, "",
          strDigitNumbers, strcElementValueFrom, strcElementValueTo, strcElementValueFromDes,
          strcElementValueToDes, strcBpartnerId, strmProductId, strcProjectId, strcAcctSchemaId,
          strNotInitialBalance, strGroupBy, strPeriodo);

    } else if (vars.commandIn("OPEN")) {
      String strAccountId = vars.getRequiredStringParameter("inpcAccountId");
      String strcAcctSchemaId = vars.getRequestGlobalVariable("inpcAcctSchemaId",
          "ReportTrialBalance|cAcctSchemaId");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportTrialBalance|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportTrialBalance|DateTo");
      String strOrg = vars.getRequestGlobalVariable("inpOrg", "ReportTrialBalance|Org");
      // String strLevel = vars.getRequestGlobalVariable("inpLevel",
      // "ReportTrialBalance|Level");
      String strDigitNumbers = vars.getGlobalVariable("inpDigitNumbers",
          "ReportTrialBalance|DigitNumbers", "");
      String strNotInitialBalance = vars.getStringParameter("inpNotInitialBalance", "N");
      vars.setSessionValue("ReportTrialBalance|notInitialBalance", strNotInitialBalance);

      String strcBpartnerId = "";
      String strmProductId = "";
      String strcProjectId = "";
      String strGroupBy = "";
      printPageOpen(response, vars, strDateFrom, strDateTo, strOrg, "", strDigitNumbers,
          strcBpartnerId, strmProductId, strcProjectId, strcAcctSchemaId, strGroupBy, strAccountId,
          strNotInitialBalance);

    } else {
      pageError(response);
    }
  }

  private void printPageOpen(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strOrg, String strLevel, String strDigitNumbers,
      String strcBpartnerId, String strmProductId, String strcProjectId, String strcAcctSchemaId,
      String strGroupBy, String strAccountId, String strNotInitialBalance)
      throws IOException, ServletException {

    ReportTrialBalanceData[] data = null;
    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    String strOrgFamily = getFamily(this, strTreeOrg, strOrg);

    log4j.debug("Output: Expand subaccount details " + strAccountId);

    data = ReportTrialBalanceData.selectAccountLines(this, strGroupBy, vars.getLanguage(),
        strOrgFamily, Utility.getContext(this, vars, "#User_Client", "ReportTrialBalance"),
        Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportTrialBalance"), null, null,
        strDateFrom, strAccountId, strcBpartnerId, strmProductId, strcProjectId, strcAcctSchemaId,
        (strNotInitialBalance.equals("Y") ? "O" : "P"),
        DateTimeData.nDaysAfter(this, strDateTo, "1"));

    if (data == null) {
      data = ReportTrialBalanceData.set();
    }

    // response.setContentType("text/plain");
    response.setContentType("text/html; charset=UTF-8");
    response.setHeader("Cache-Control", "no-cache");
    PrintWriter out = response.getWriter();

    // Create JSON object
    // { "rows" : [
    // {"td1":"Bellen
    // Ent.","td3":"0,00","td2":"0,00","td5":"-48,59","td4":"48,59"},
    // {"td1":"Mafalda
    // Corporation","td3":"34,56","td2":"0,00","td5":"-334,79","td4":"369,35"}],
    // "config" :
    // {"classDefault":"DataGrid_Body_Cell","classAmount":"DataGrid_Body_Cell_Amount"}
    // }
    DecimalFormat df = Utility.getFormat(vars, "euroInform");
    JSONObject table = new JSONObject();
    JSONArray tr = new JSONArray();
    Map<String, String> tds = null;
    try {

      for (int i = 0; i < data.length; i++) {
        tds = new HashMap<String, String>();
        tds.put("td1", data[i].groupbyname);
        tds.put("td2", df.format(new BigDecimal(data[i].saldoInicial)));
        tds.put("td3", df.format(new BigDecimal(data[i].amtacctdr)));
        tds.put("td4", df.format(new BigDecimal(data[i].amtacctcr)));
        tds.put("td5", df.format(new BigDecimal(data[i].saldoFinal)));
        tr.put(data.length - (i + 1), tds);
        table.put("rows", tr);
      }
      Map<String, String> props = new HashMap<String, String>();
      props.put("classAmount", "DataGrid_Body_Cell_Amount");
      props.put("classDefault", "DataGrid_Body_Cell");
      table.put("config", props);

    } catch (JSONException e) {
      log4j.error("Error creating JSON object for representing subaccount lines", e);
      throw new ServletException(e);
    }

    log4j.debug("JSON string: " + table.toString());

    out.println("jsonTable = " + table.toString());
    out.close();
  }

  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String strPageNo,
      String strOrg, String strLevel, String strDigitNumbers, String strcElementValueFrom,
      String strcElementValueTo, String strcElementValueFromDes, String strcElementValueToDes,
      String strcBpartnerId, String strmProductId, String strcProjectId, String strcAcctSchemaId,
      String strNotInitialBalance, String strGroupBy, String strPeriodo)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReportTrialBalanceData[] dataFinal = null;

    DecimalFormat df = new DecimalFormat("0.00");
    df.setRoundingMode(RoundingMode.HALF_UP);

    String discard[] = { "discard" };
    String strMessage = "";

    xmlDocument = xmlEngine
        .readXmlTemplate("pe/com/unifiedgo/report/ad_reports/ReportTrialBalance", discard)
        .createXmlDocument();

    if (vars.commandIn("FIND")) {
      String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
      String strOrgFamily = getFamily(this, strTreeOrg, strOrg);

      ArrayList<ReportTrialBalanceData> listData = new ArrayList<ReportTrialBalanceData>();
      Integer maxDig = new Integer(strDigitNumbers);
      String strClient = Utility.getContext(this, vars, "#User_Client", "ReportTrialBalance");

      dataFinal = getDataBalance(this, strOrgFamily, strClient, strDateFrom, strDateTo,
          strcElementValueFrom, strcElementValueTo);

      for (int i = 0; i < dataFinal.length; i++) {
        if (dataFinal[i].accountId.length() > 1 && dataFinal[i].accountId.length() <= maxDig) {
          listData.add(dataFinal[i]);
        }
      }
      dataFinal = configuraEstadosFinancieros(this,
          listData.toArray(new ReportTrialBalanceData[listData.size()]), strOrgFamily, strClient);

      // FORMATTING DATA FOR HTML REPORT
      for (int i = 0; i < dataFinal.length; i++) {
        dataFinal[i].paramaccountid = dataFinal[i].accountId;
        dataFinal[i].paramorgid = strOrg;
        dataFinal[i].paramdatefrom = strDateFrom;
        dataFinal[i].paramdateto = strDateTo;
        dataFinal[i].paramacctschemaid = strcAcctSchemaId;
        dataFinal[i].paramelementvaluefrom = strcElementValueFrom;
        dataFinal[i].paramelementvalueto = strcElementValueTo;
        dataFinal[i].paramnotinitialbalance = strNotInitialBalance;
        dataFinal[i].paramtitle = dataFinal[i].accountId + " - " + dataFinal[i].name;

        if (dataFinal[i].accountId.length() == 2) {
          dataFinal[i].accountId = "<b>" + dataFinal[i].accountId + "</b>";
          dataFinal[i].name = "<b>" + dataFinal[i].name + "</b>";
        }

        dataFinal[i].debeInicial = (dataFinal[i].debeInicial == ""
            || dataFinal[i].debeInicial == null) ? "0.00"
                : df.format(new BigDecimal(dataFinal[i].debeInicial));
        dataFinal[i].haberInicial = (dataFinal[i].haberInicial == ""
            || dataFinal[i].haberInicial == null) ? "0.00"
                : df.format(new BigDecimal(dataFinal[i].haberInicial));

        dataFinal[i].amtacctdr = (dataFinal[i].amtacctdr == "" || dataFinal[i].amtacctdr == null)
            ? "0.00" : df.format(new BigDecimal(dataFinal[i].amtacctdr));
        dataFinal[i].amtacctcr = (dataFinal[i].amtacctcr == "" || dataFinal[i].amtacctcr == null)
            ? "0.00" : df.format(new BigDecimal(dataFinal[i].amtacctcr));

        dataFinal[i].sumadebeFinal = (dataFinal[i].sumadebeFinal == ""
            || dataFinal[i].sumadebeFinal == null) ? "0.00"
                : df.format(new BigDecimal(dataFinal[i].sumadebeFinal));
        dataFinal[i].sumahaberFinal = (dataFinal[i].sumahaberFinal == ""
            || dataFinal[i].sumahaberFinal == null) ? "0.00"
                : df.format(new BigDecimal(dataFinal[i].sumahaberFinal));

        dataFinal[i].debeFinal = (dataFinal[i].debeFinal == "" || dataFinal[i].debeFinal == null)
            ? "0.00" : df.format(new BigDecimal(dataFinal[i].debeFinal));
        dataFinal[i].haberFinal = (dataFinal[i].haberFinal == "" || dataFinal[i].haberFinal == null)
            ? "0.00" : df.format(new BigDecimal(dataFinal[i].haberFinal));

        dataFinal[i].montoActivo = (dataFinal[i].montoActivo == ""
            || dataFinal[i].montoActivo == null) ? "0.00"
                : df.format(new BigDecimal(dataFinal[i].montoActivo));
        dataFinal[i].montoPasivo = (dataFinal[i].montoPasivo == ""
            || dataFinal[i].montoPasivo == null) ? "0.00"
                : df.format(new BigDecimal(dataFinal[i].montoPasivo));
      }

      // Otherwise, the report is launched
      if (dataFinal == null || dataFinal.length == 0) {
        dataFinal = ReportTrialBalanceData.set();

        if (vars.commandIn("FIND")) {
          // No data has been found. Show warning message.
          xmlDocument.setParameter("messageType", "WARNING");
          xmlDocument.setParameter("messageTitle",
              Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()));
          xmlDocument.setParameter("messageMessage",
              Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
        }

      } else {
        if (dataFinal == null || dataFinal.length == 0) {
          dataFinal = ReportTrialBalanceData.set();
        }
      }

    }

    else {
      dataFinal = ReportTrialBalanceData.set();
    }

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportTrialBalance", false, "", "",
        "if (validate()) { imprimir(); } return false;", false, "ad_reports", strReplaceWith, false,
        true);
    toolbar.setEmail(false);
    toolbar.prepareSimpleToolBarTemplate();
    toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");
    // toolbar
    // .prepareRelationBarTemplate(
    // false,
    // false,
    // "if (validate()) { submitCommandForm('XLS', false, frmMain,
    // 'ReportTrialBalanceExcel.xls', 'EXCEL'); } return false;");
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "pe.com.unifiedgo.report.ad_reports.ReportTrialBalance");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportTrialBalance.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportTrialBalance.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());

    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportTrialBalance");
      vars.removeMessage("ReportTrialBalance");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    try {

      Vector<Object> vector = new Vector<Object>(0);

      SQLReturnObject sqlReturnObject = new SQLReturnObject();

      for (int i = 2; i <= 8; i++) {
        sqlReturnObject = new SQLReturnObject();
        sqlReturnObject.setData("ID", String.valueOf(i));
        sqlReturnObject.setData("NAME", String.valueOf(i));
        vector.add(sqlReturnObject);
      }

      FieldProvider numeros[] = new FieldProvider[vector.size()];

      vector.copyInto(numeros);
      xmlDocument.setData("reportDigitNumbers", "liststructure", numeros);
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setData("reportC_ACCTSCHEMA_ID", "liststructure",
        AccountingSchemaMiscData.selectC_ACCTSCHEMA_ID(this,
            Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportTrialBalance"),
            Utility.getContext(this, vars, "#User_Client", "ReportTrialBalance"),
            strcAcctSchemaId));

    xmlDocument.setParameter("paramLibrosArray",
        Utility.arrayInfinitasEntradas("idlibro;nombrelibro;idorganizacion", "arrLibroMayor",
            ReportTrialBalanceData.select_libros(this)));

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("PageNo", strPageNo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("adOrgId", strOrg);
    // xmlDocument.setParameter("Level", "".equals(strLevel) ? "C" :
    // strLevel);
    xmlDocument.setParameter("DigitNumbers", "".equals(strDigitNumbers) ? "1" : strDigitNumbers);
    xmlDocument.setParameter("cAcctschemaId", strcAcctSchemaId);
    xmlDocument.setParameter("paramElementvalueIdFrom", strcElementValueFrom);
    xmlDocument.setParameter("paramElementvalueIdTo", strcElementValueTo);
    xmlDocument.setParameter("inpElementValueIdFrom_DES", strcElementValueFromDes);
    xmlDocument.setParameter("inpElementValueIdTo_DES", strcElementValueToDes);
    xmlDocument.setParameter("paramMessage",
        (strMessage.equals("") ? "" : "alert('" + strMessage + "');"));
    xmlDocument.setParameter("notInitialBalance", strNotInitialBalance);
    xmlDocument.setParameter("Periodo", strPeriodo);
    xmlDocument.setParameter("paramPeriodosArray",
        Utility.arrayInfinitasEntradas("idperiodo;periodo;fechainicial;fechafinal;idorganizacion",
            "arrPeriodos", ReportTrialBalanceData.select_periodos(this)));

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
          "0C754881EAD94243A161111916E9B9C6",
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportTrialBalance"),
          Utility.getContext(this, vars, "#User_Client", "ReportTrialBalance"), '*');
      comboTableData.fillParameters(null, "ReportTrialBalance", "");
      xmlDocument.setData("reportAD_ORGID", "liststructure", comboTableData.select(false));
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setData("reportC_ACCTSCHEMA_ID", "liststructure",
        AccountingSchemaMiscData.selectC_ACCTSCHEMA_ID(this,
            Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportGeneralLedger"),
            Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"), ""));

    xmlDocument.setData("structure1", dataFinal);

    // Print document in the output
    out.println(xmlDocument.print());
    out.close();
  }

  public static StructPle getPLE(ConnectionProvider conn, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String adUserClient, String strOrg) throws Exception {

    String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_JAVA);

    SimpleDateFormat formatterForm = new SimpleDateFormat("dd-MM-yyyy");
    Date dateTo = null;
    try {
      dateTo = formatterForm.parse(strDateTo);
    } catch (Exception ex) {
      System.out.println("Exception: " + strDateFrom);
    }

    Calendar caldttTo = new GregorianCalendar();
    caldttTo.setTime(dateTo);
    int year = caldttTo.get(Calendar.YEAR);

    // get first day of year
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.DAY_OF_YEAR, 1);
    Date dateFrom = cal.getTime();

    ReportTrialBalanceData[] dataFinal = getData(conn, vars, sdf.format(dateFrom),
        sdf.format(dateTo), adUserClient, strOrg, "", "7", "", "", "", "");

    StringBuffer sb = new StringBuffer();
    StructPle sunatPle = new StructPle();
    sunatPle.numEntries = 0;

    SimpleDateFormat dt = new SimpleDateFormat("yyyyMMdd");
    DecimalFormat df = new DecimalFormat("0.00##");

    String period = dt.format(dateTo); // Periodo
    String OpStatus = "1"; // Estado de la Operación

    for (int i = 0; i < dataFinal.length; i++) {

      ReportTrialBalanceData account = dataFinal[i];
      if (account.accountId.length() != 7)
        continue;

      String montoActivo = "0.00";
      String montoPasivo = "0.00";
      String montoNganancias = "0.00";
      String montoNperdidas = "0.00";
      String montoFganancias = "0.00";
      String montoFperdidas = "0.00";

      if (account.pleEsInventario != null && account.pleEsInventario.equals("Y")) {
        montoActivo = account.pleMontoActivo;
        montoPasivo = account.pleMontoPasivo;
      }

      if (account.pleEsNaturaleza != null && account.pleEsNaturaleza.equals("Y")) {
        montoNganancias = account.pleMontoNganancias;
        montoNperdidas = account.pleMontoNperdidas;
      }

      if (account.pleEsFuncion != null && account.pleEsFuncion.equals("Y")) {
        montoFganancias = account.pleMontoFganancias;
        montoFperdidas = account.pleMontoFperdidas;
      }

      DecimalFormat ndf = new DecimalFormat("0.00##");

      // debeinicial and haberinicial must be exclusive
      BigDecimal debeinicial = new BigDecimal(account.debeInicial);
      BigDecimal haberinicial = new BigDecimal(account.haberInicial);

      if (debeinicial.compareTo(haberinicial) > 0) {
        account.debeInicial = ndf.format(debeinicial.subtract(haberinicial));
        account.haberInicial = "0.00";
      } else {
        account.debeInicial = "0.00";
        account.haberInicial = ndf.format(haberinicial.subtract(debeinicial));
      }

      String linea = period + "|" + account.accountId + "|" + account.debeInicial + "|"
          + account.haberInicial + "|" + ndf.format(ndf.parse(account.amtacctdr)) + "|"
          + ndf.format(ndf.parse(account.amtacctcr)) + "|"
          + ndf.format(ndf.parse(account.sumadebeFinal)) + "|"
          + ndf.format(ndf.parse(account.sumahaberFinal)) + "|"
          + ndf.format(ndf.parse(account.debeFinal)) + "|"
          + ndf.format(ndf.parse(account.haberFinal)) + "|0.00|0.00|"
          + ndf.format(ndf.parse(montoActivo)) + "|" + ndf.format(ndf.parse(montoPasivo)) + "|"
          + ndf.format(ndf.parse(montoNperdidas)) + "|" + ndf.format(ndf.parse(montoNganancias))
          + "|||" + OpStatus + "|";

      if (!sb.toString().equals(""))
        sb.append("\n");
      sb.append(linea);
      sunatPle.numEntries++;

    }

    sunatPle.data = sb.toString();

    Organization org = OBDal.getInstance().get(Organization.class, strOrg);
    String rucAdq = SunatUtil.LPad(org.getOrganizationInformationList().get(0).getTaxID(), 11, '0');

    String filename = "LE" + rucAdq + dt.format(dateTo) + "031700011111.TXT"; // LERRRRRRRRRRRAAAAMMDD031700CCOIM1.TXT

    sunatPle.filename = filename;

    return sunatPle;
  }

  private static ReportTrialBalanceData[] getData(ConnectionProvider conn, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String adUserClient, String strOrg, String strLevel,
      String strDigitNumbers, String strcElementValueFrom, String strcElementValueFromDes,
      String strcElementValueTo, String strcElementValueToDes)
      throws IOException, ServletException {

    String strTreeOrg = TreeData.getTreeOrg(conn, vars.getClient());
    String strOrgFamily = getFamily(conn, strTreeOrg, strOrg);

    ReportTrialBalanceData[] dataFinal = null;

    ArrayList<ReportTrialBalanceData> listData = new ArrayList<ReportTrialBalanceData>();
    Integer maxDig = new Integer(strDigitNumbers);

    dataFinal = getDataBalance(conn, strOrgFamily, adUserClient, strDateFrom, strDateTo,
        strcElementValueFrom, strcElementValueTo);

    for (int i = 0; i < dataFinal.length; i++) {

      ReportTrialBalanceData e = dataFinal[i];
      if (e.accountId.length() <= maxDig) {

        if (maxDig == 2) {// NETEAMOS LOS SALDOS CUANDO SON DE 2 DIGITOS

          BigDecimal debe = new BigDecimal(e.debeInicial);
          BigDecimal haber = new BigDecimal(e.haberInicial);
          BigDecimal neto = debe.subtract(haber);

          e.debeInicial = neto.signum() == 1 ? neto.toString() : "0.00";
          e.haberInicial = neto.signum() == -1 ? neto.abs().toString() : "0.00";

          e.sumadebeFinal = new BigDecimal(e.amtacctdr).add(new BigDecimal(e.debeInicial))
              .toString();
          e.sumahaberFinal = new BigDecimal(e.amtacctcr).add(new BigDecimal(e.haberInicial))
              .toString();

          debe = new BigDecimal(e.debeFinal);
          haber = new BigDecimal(e.haberFinal);
          neto = debe.subtract(haber);

          e.debeFinal = neto.signum() == 1 ? neto.toString() : "0.00";
          e.haberFinal = neto.signum() == -1 ? neto.abs().toString() : "0.00";
        }

        listData.add(e);
      }
    }
    dataFinal = configuraEstadosFinancieros(conn,
        listData.toArray(new ReportTrialBalanceData[listData.size()]), strOrgFamily, adUserClient);

    return dataFinal;
  }

  private void printPageDataPDF(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String strOrg, String strLevel,
      String strDigitNumbers, String strcElementValueFrom, String strcElementValueFromDes,
      String strcElementValueTo, String strcElementValueToDes, String strcBpartnerId,
      String strmProductId, String strcProjectId, String strcAcctSchemaId,
      String strNotInitialBalance, String strGroupBy, String strPageNo)
      throws IOException, ServletException {

    ReportTrialBalanceData[] dataFinal = null;

    boolean strIsSubAccount = false;

    Integer maxDig = new Integer(strDigitNumbers);

    dataFinal = getData(this, vars, strDateFrom, strDateTo,
        Utility.getContext(this, vars, "#User_Client", "ReportTrialBalance"), strOrg, strLevel,
        strDigitNumbers, strcElementValueFrom, strcElementValueFromDes, strcElementValueTo,
        strcElementValueToDes);

    AcctSchema acctSchema = OBDal.getInstance().get(AcctSchema.class, strcAcctSchemaId);

    String strLanguage = vars.getLanguage();

    String[] partesFecha = strDateFrom.split("-");

    String fechaAjuste = "31-12-" + partesFecha[2];

    String periodo_ajuste = "";

    if (fechaAjuste.equals(strDateFrom) && fechaAjuste.equals(strDateTo)) {
      periodo_ajuste = "Ajuste - " + partesFecha[2];
    }

    String strReportName, strOutput;

    if (vars.commandIn("PDF")) {
      strOutput = "pdf";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportTrialBalancePDF2.jrxml";
    } else {
      strOutput = "xls";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportTrialBalanceExcel3.jrxml";
    }

    HashMap<String, Object> parameters = new HashMap<String, Object>();

    parameters.put("TOTAL", Utility.messageBD(this, "Total", strLanguage));
    StringBuilder strSubTitle = new StringBuilder();

    strSubTitle.append(Utility.messageBD(this, "LegalEntity", vars.getLanguage()) + ": ");
    strSubTitle.append(ReportTrialBalanceData.selectCompany(this, vars.getClient()) + " \n");
    strSubTitle
        .append(Utility.messageBD(this, "asof", vars.getLanguage()) + ": " + strDateTo + " \n");

    if (!("0".equals(strOrg)))
      strSubTitle.append(Utility.messageBD(this, "ACCS_AD_ORG_ID_D", vars.getLanguage()) + ": "
          + ReportTrialBalanceData.selectOrgName(this, strOrg) + " \n");

    strSubTitle.append(
        Utility.messageBD(this, "generalLedger", vars.getLanguage()) + ": " + acctSchema.getName());

    parameters.put("numDigitos", maxDig);

    parameters.put("REPORT_SUBTITLE", strSubTitle.toString());
    parameters.put("DEFAULTVIEW", !strIsSubAccount);
    parameters.put("SUBACCOUNTVIEW", strIsSubAccount);
    parameters.put("DUMMY", true);
    parameters.put("PageNo", strPageNo);
    parameters.put("Ruc", ReportTrialBalanceData.selectRucOrg(this, strOrg));
    parameters.put("Razon", ReportTrialBalanceData.selectSocialName(this, strOrg));
    parameters.put("dateFrom", StringToDate(strDateFrom));
    parameters.put("dateTo", StringToDate(strDateTo));
    parameters.put("PAGEOF", Utility.messageBD(this, "PageOfNumber", vars.getLanguage()));
    parameters.put("PERIODO_AJUSTE", periodo_ajuste);

    renderJR(vars, response, strReportName, "Balance_Comprobacion", strOutput, parameters,
        dataFinal, null);
    // }
    //
    // } else {
    // advisePopUp(request, response, "WARNING",
    // Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
    // Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
    // }

  }

  private Date StringToDate(String strDate) {
    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

    Date date;
    try {
      if (!strDate.equals("")) {
        date = formatter.parse(strDate);
        return date;
      }
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  /*
   * private ReportTrialBalanceData[] filterTree(ReportTrialBalanceData[] data, String strLevel) {
   * ArrayList<Object> arrayList = new ArrayList<Object>(); for (int i = 0; data != null && i <
   * data.length; i++) { if (data[i].elementlevel.equals(strLevel)) arrayList.add(data[i]); }
   * ReportTrialBalanceData[] new_data = new ReportTrialBalanceData[arrayList.size()];
   * arrayList.toArray(new_data); return new_data; }
   */

  /**
   * Filters positions with amount credit, amount debit, initial balance and final balance distinct
   * to zero.
   * 
   * @param data
   * @return ReportTrialBalanceData array filtered.
   */
  static private ReportTrialBalanceData[] dataFilter(ReportTrialBalanceData[] data) {
    if (data == null || data.length == 0)
      return data;
    Vector<Object> dataFiltered = new Vector<Object>();
    for (int i = 0; i < data.length; i++) {
      if (new BigDecimal(data[i].amtacctdr).compareTo(BigDecimal.ZERO) != 0
          || new BigDecimal(data[i].amtacctcr).compareTo(BigDecimal.ZERO) != 0
          || new BigDecimal(data[i].debeInicial).compareTo(BigDecimal.ZERO) != 0
          || new BigDecimal(data[i].haberInicial).compareTo(BigDecimal.ZERO) != 0) {
        dataFiltered.addElement(data[i]);
      }
    }
    ReportTrialBalanceData[] result = new ReportTrialBalanceData[dataFiltered.size()];
    dataFiltered.copyInto(result);
    return result;
  }

  static private String getFamily(ConnectionProvider conn, String strTree, String strChild)
      throws IOException, ServletException {
    return Tree.getMembers(conn, strTree, strChild);
  }

  static private ReportTrialBalanceData[] getDataBalance(ConnectionProvider conn, String strOrg,
      String strClient, String dateFrom, String dateTo, String cuentaDesdeId, String cuentaHastaId)
      throws ServletException {

    ReportTrialBalanceData[] dataCuentas = null;
    ReportTrialBalanceData[] dataSaldosIniciales = null;
    ReportTrialBalanceData[] dataMovimientosEjercicio = null;

    HashMap<String, BigDecimal> grupoSaldoInicialDebe = new HashMap<String, BigDecimal>();
    HashMap<String, BigDecimal> grupoSaldoInicialHaber = new HashMap<String, BigDecimal>();
    HashMap<String, BigDecimal> grupoMovimientosEjercicioDebe = new HashMap<String, BigDecimal>();
    HashMap<String, BigDecimal> grupoMovimientosEjercicioHaber = new HashMap<String, BigDecimal>();
    HashMap<String, BigDecimal> grupoSaldosFinalesDebe = new HashMap<String, BigDecimal>();
    HashMap<String, BigDecimal> grupoSaldosFinalesHaber = new HashMap<String, BigDecimal>();

    BigDecimal debeFinal = BigDecimal.ZERO;
    BigDecimal haberFinal = BigDecimal.ZERO;

    String[] partesFecha = dateFrom.split("-");
    String fechaIniAnio = "01-01-" + partesFecha[2];

    dataCuentas = ReportTrialBalanceData.select_arbol_de_cuentas(conn, strOrg, strClient);

    String fechaAjuste = "31-12-" + partesFecha[2];

    if (fechaAjuste.equals(dateFrom) && fechaAjuste.equals(dateTo)) {
      dataSaldosIniciales = ReportTrialBalanceData.select_saldos_iniciales_ajuste(conn, strOrg,
          strClient, fechaIniAnio, dateTo, cuentaDesdeId, cuentaHastaId);

      dataMovimientosEjercicio = ReportTrialBalanceData.select_movimientos_ejercicio_ajuste(conn,
          strOrg, strClient, fechaAjuste, cuentaDesdeId, cuentaHastaId);
    } else {
      dataSaldosIniciales = ReportTrialBalanceData.select_saldos_iniciales(conn, strOrg, strClient,
          fechaIniAnio, fechaIniAnio, dateFrom, cuentaDesdeId, cuentaHastaId);

      dataMovimientosEjercicio = ReportTrialBalanceData.select_movimientos_ejercicio(conn, strOrg,
          strClient, dateFrom, dateTo, cuentaDesdeId, cuentaHastaId);
    }

    for (int i = 0; i < dataCuentas.length; i++) {
      ReportTrialBalanceData e = dataCuentas[i];
      String key = "C" + e.accountId;
      if (e.nivelCuenta.equalsIgnoreCase("C")) {
        grupoSaldoInicialDebe.put(key, BigDecimal.ZERO);
        grupoSaldoInicialHaber.put(key, BigDecimal.ZERO);
        grupoMovimientosEjercicioDebe.put(key, BigDecimal.ZERO);
        grupoMovimientosEjercicioHaber.put(key, BigDecimal.ZERO);
        grupoSaldosFinalesDebe.put(key, BigDecimal.ZERO);
        grupoSaldosFinalesHaber.put(key, BigDecimal.ZERO);
      }
    }

    for (int i = 0; i < dataSaldosIniciales.length; i++) {

      ReportTrialBalanceData e = dataSaldosIniciales[i];
      BigDecimal debe = new BigDecimal(e.amtacctdr);
      BigDecimal haber = new BigDecimal(e.amtacctcr);

      String key = "S" + e.accountId;
      grupoSaldoInicialDebe.put(key, debe);
      grupoSaldoInicialHaber.put(key, haber);

      for (int k = 0; k < e.accountId.length(); k++) {
        key = "C" + e.accountId.substring(0, k + 1);

        if (grupoSaldoInicialDebe.containsKey(key)) {

          debeFinal = debe.add(grupoSaldoInicialDebe.get(key));
          haberFinal = haber.add(grupoSaldoInicialHaber.get(key));
          grupoSaldoInicialDebe.put(key, debeFinal);
          grupoSaldoInicialHaber.put(key, haberFinal);
        }
      }
    }

    for (int i = 0; i < dataMovimientosEjercicio.length; i++) {

      ReportTrialBalanceData e = dataMovimientosEjercicio[i];
      BigDecimal debe = new BigDecimal(e.amtacctdr);
      BigDecimal haber = new BigDecimal(e.amtacctcr);

      String key = "S" + e.accountId;
      grupoMovimientosEjercicioDebe.put(key, debe);
      grupoMovimientosEjercicioHaber.put(key, haber);

      for (int k = 0; k < e.accountId.length(); k++) {
        key = "C" + e.accountId.substring(0, k + 1);

        if (grupoMovimientosEjercicioDebe.containsKey(key)) {

          debeFinal = debe.add(grupoMovimientosEjercicioDebe.get(key));
          haberFinal = haber.add(grupoMovimientosEjercicioHaber.get(key));
          grupoMovimientosEjercicioDebe.put(key, debeFinal);
          grupoMovimientosEjercicioHaber.put(key, haberFinal);
        }
      }
    }

    for (int i = 0; i < dataCuentas.length; i++) {
      ReportTrialBalanceData e = dataCuentas[i];

      String key = e.accountId;
      BigDecimal neto = BigDecimal.ZERO;

      if (e.nivelCuenta.equalsIgnoreCase("C")) {
        key = "C" + key;
      } else {
        key = "S" + key;
      }

      e.debeInicial = grupoSaldoInicialDebe.containsKey(key)
          ? grupoSaldoInicialDebe.get(key).toString() : "0.00";
      e.haberInicial = grupoSaldoInicialHaber.containsKey(key)
          ? grupoSaldoInicialHaber.get(key).toString() : "0.00";
      e.amtacctdr = grupoMovimientosEjercicioDebe.containsKey(key)
          ? grupoMovimientosEjercicioDebe.get(key).toString() : "0.00";
      e.amtacctcr = grupoMovimientosEjercicioHaber.containsKey(key)
          ? grupoMovimientosEjercicioHaber.get(key).toString() : "0.00";
      e.sumadebeFinal = new BigDecimal(e.debeInicial).add(new BigDecimal(e.amtacctdr)).toString();
      e.sumahaberFinal = new BigDecimal(e.haberInicial).add(new BigDecimal(e.amtacctcr)).toString();

      neto = new BigDecimal(e.sumadebeFinal).subtract(new BigDecimal(e.sumahaberFinal));
      e.debeFinal = neto.signum() == 1 ? neto.toString() : "0.00";
      e.haberFinal = neto.signum() == -1 ? neto.abs().toString() : "0.00";
    }

    // for (int i = 0; i < dataCuentas.length; i++) {
    // ReportTrialBalanceData e = dataCuentas[i];
    //
    // String key = e.accountId;
    // BigDecimal debe = BigDecimal.ZERO;
    // BigDecimal haber = BigDecimal.ZERO;
    //
    // if (e.nivelCuenta.equalsIgnoreCase("C")) {
    // for (int k = 0; k < dataSaldosIniciales.length; k++) {
    // ReportTrialBalanceData o = dataSaldosIniciales[k];
    // if (o.accountId.startsWith(key)) {
    // debe = debe.add(new BigDecimal(o.amtacctdr));
    // haber = haber.add(new BigDecimal(o.amtacctcr));
    // }
    // }
    // e.debeInicial = debe.toString();
    // e.haberInicial = haber.toString();
    //
    // for (int k = 0; k < dataMovimientosEjercicio.length; k++) {
    // ReportTrialBalanceData o = dataMovimientosEjercicio[k];
    // if (o.accountId.startsWith(key)) {
    // debe = debe.add(new BigDecimal(o.amtacctdr));
    // haber = haber.add(new BigDecimal(o.amtacctcr));
    // }
    // }
    // e.debeInicial = debe.toString();
    // e.haberInicial = haber.toString();
    //
    // }
    //
    // }

    // dataCuentas = buscaUltimaCuenta(dataCuentas);
    //
    // BigDecimal debeInicial = BigDecimal.ZERO;
    // BigDecimal haberInicial = BigDecimal.ZERO;
    // BigDecimal debeMovEjercicio = BigDecimal.ZERO;
    // BigDecimal haberMovEjercicio = BigDecimal.ZERO;
    // BigDecimal debeSumaMayor = BigDecimal.ZERO;
    // BigDecimal haberSumaMayor = BigDecimal.ZERO;
    //
    // for (int i = 0; i < dataCuentas.length; i++) {
    //
    // String key = dataCuentas[i].accountId;
    //
    // debeInicial = grupoSaldoInicialDebe.get(key) != null ? grupoSaldoInicialDebe.get(key)
    // : BigDecimal.ZERO;
    // haberInicial = grupoSaldoInicialHaber.get(key) != null ? grupoSaldoInicialHaber.get(key)
    // : BigDecimal.ZERO;
    // debeMovEjercicio = grupoMovimientosEjercicioDebe.get(key) != null
    // ? grupoMovimientosEjercicioDebe.get(key) : BigDecimal.ZERO;
    // haberMovEjercicio = grupoMovimientosEjercicioHaber.get(key) != null
    // ? grupoMovimientosEjercicioHaber.get(key) : BigDecimal.ZERO;
    // debeSumaMayor = debeInicial.add(debeMovEjercicio);
    // haberSumaMayor = haberInicial.add(haberMovEjercicio);
    //
    // dataCuentas[i].debeInicial = debeInicial.toString();
    // dataCuentas[i].haberInicial = haberInicial.toString();
    // dataCuentas[i].amtacctdr = debeMovEjercicio.toString();
    // dataCuentas[i].amtacctcr = haberMovEjercicio.toString();
    // dataCuentas[i].sumadebeFinal = debeSumaMayor.toString();
    // dataCuentas[i].sumahaberFinal = haberSumaMayor.toString();
    //
    // if (dataCuentas[i].esUltimaCuenta.equals("Y")) {
    // dataCuentas[i].debeFinal = (debeSumaMayor.compareTo(haberSumaMayor) > 0
    // ? debeSumaMayor.subtract(haberSumaMayor) : BigDecimal.ZERO).toString();
    // dataCuentas[i].haberFinal = (haberSumaMayor.compareTo(debeSumaMayor) > 0
    // ? haberSumaMayor.subtract(debeSumaMayor) : BigDecimal.ZERO).toString();
    //
    // // OBTENIENDO LOS SALDOS FINALES A PARTIR DE LAS CUENTAS BASES :
    // // 7 DIGITOS
    // for (int j = 0; j < key.length(); j++) {
    //
    // String key2 = dataCuentas[i].accountId.substring(0, j + 1);
    // BigDecimal debe = new BigDecimal(dataCuentas[i].debeFinal);
    // BigDecimal haber = new BigDecimal(dataCuentas[i].haberFinal);
    //
    // if (grupoSaldosFinalesDebe.containsKey(key2)) {
    // debeFinal = debe.add(grupoSaldosFinalesDebe.get(key2));
    // haberFinal = haber.add(grupoSaldosFinalesHaber.get(key2));
    // } else {
    // debeFinal = debe;
    // haberFinal = haber;
    // }
    // grupoSaldosFinalesDebe.put(key2, debeFinal);
    // grupoSaldosFinalesHaber.put(key2, haberFinal);
    // }
    // }
    // }

    dataCuentas = dataFilter(dataCuentas);

    // RECORREMOS NUEVAMENTE PARA AGREGAR LOS SALDOS FINALES A LAS CUENTAS
    // QUE NO SON FINALES
    // for (int i = 0; i < dataCuentas.length; i++) {
    //
    // String key = dataCuentas[i].accountId;
    // if (dataCuentas[i].esUltimaCuenta.equals("N")) {
    // dataCuentas[i].debeFinal = (grupoSaldosFinalesDebe.containsKey(key)
    // ? grupoSaldosFinalesDebe.get(key) : BigDecimal.ZERO).toString();
    // dataCuentas[i].haberFinal = (grupoSaldosFinalesHaber.containsKey(key)
    // ? grupoSaldosFinalesHaber.get(key) : BigDecimal.ZERO).toString();
    // }
    // }

    return dataCuentas;
  }

  static private ReportTrialBalanceData[] buscaUltimaCuenta(ReportTrialBalanceData[] dataCuentas) {

    String cuentaAnterior = "??";

    for (int i = 0; i < dataCuentas.length; i++) {

      String cuenta = dataCuentas[i].accountId;
      if (cuenta.startsWith(cuentaAnterior)) {
        dataCuentas[i - 1].esUltimaCuenta = "N";
      }
      dataCuentas[i].esUltimaCuenta = "Y";
      cuentaAnterior = cuenta;
    }

    return dataCuentas;

  }

  private static ReportTrialBalanceData[] configuraEstadosFinancieros(ConnectionProvider conn,
      ReportTrialBalanceData[] dataFinal, String strOrg, String strClient) throws ServletException {

    HashMap<String, ReportTrialBalanceData> grupoConfiguracionEEFF = new HashMap<String, ReportTrialBalanceData>();

    ReportTrialBalanceData[] dataConfig = ReportTrialBalanceData.select_configuracion_eeff(conn,
        strClient, "'0'," + strOrg);

    for (int i = 0; i < dataConfig.length; i++) {
      grupoConfiguracionEEFF.put(dataConfig[i].accountId, dataConfig[i]);
    }

    ReportTrialBalanceData config = null;

    for (int i = 0; i < dataFinal.length; i++) {

      if (dataFinal[i].accountId.length() == 2
          && grupoConfiguracionEEFF.containsKey(dataFinal[i].accountId)) {

        config = grupoConfiguracionEEFF.get(dataFinal[i].accountId);

        BigDecimal montox = new BigDecimal(dataFinal[i].debeFinal)
            .subtract(new BigDecimal(dataFinal[i].haberFinal));
        BigDecimal monto1;
        BigDecimal monto2;
        if (montox.compareTo(BigDecimal.ZERO) > 0) {
          monto1 = montox;
          monto2 = BigDecimal.ZERO;
        } else {
          monto1 = BigDecimal.ZERO;
          monto2 = montox.abs();
        }

        if (config.esFuncion.compareTo("Y") == 0) {
          dataFinal[i].esFuncion = config.esFuncion;
          dataFinal[i].montoFganancias = monto2.toString();
          dataFinal[i].montoFperdidas = monto1.toString();
        }
        if (config.esNaturaleza.compareTo("Y") == 0) {
          dataFinal[i].esNaturaleza = config.esNaturaleza;
          dataFinal[i].montoNganancias = monto2.toString();
          dataFinal[i].montoNperdidas = monto1.toString();
        }
        if (config.esInventario.compareTo("Y") == 0) {
          dataFinal[i].esInventario = config.esInventario;
          dataFinal[i].montoActivo = monto1.toString();
          dataFinal[i].montoPasivo = monto2.toString();
        }
      }

      if (dataFinal[i].accountId.length() == 7
          && grupoConfiguracionEEFF.containsKey(dataFinal[i].accountId.substring(0, 2))) {

        config = grupoConfiguracionEEFF.get(dataFinal[i].accountId.substring(0, 2));

        BigDecimal montox = new BigDecimal(dataFinal[i].debeFinal)
            .subtract(new BigDecimal(dataFinal[i].haberFinal));
        BigDecimal monto1;
        BigDecimal monto2;
        if (montox.compareTo(BigDecimal.ZERO) > 0) {
          monto1 = montox;
          monto2 = BigDecimal.ZERO;
        } else {
          monto1 = BigDecimal.ZERO;
          monto2 = montox.abs();
        }

        if (config.esFuncion.compareTo("Y") == 0) {
          dataFinal[i].pleEsFuncion = config.esFuncion;
          dataFinal[i].pleMontoFganancias = monto2.toString();
          dataFinal[i].pleMontoFperdidas = monto1.toString();
        }
        if (config.esNaturaleza.compareTo("Y") == 0) {
          dataFinal[i].pleEsNaturaleza = config.esNaturaleza;
          dataFinal[i].pleMontoNganancias = monto2.toString();
          dataFinal[i].pleMontoNperdidas = monto1.toString();
        }
        if (config.esInventario.compareTo("Y") == 0) {
          dataFinal[i].pleEsInventario = config.esInventario;
          dataFinal[i].pleMontoActivo = monto1.toString();
          dataFinal[i].pleMontoPasivo = monto2.toString();
        }
      }

    }

    return dataFinal;
  }

  public String getServletInfo() {
    return "Servlet ReportTrialBalance. This Servlet was made by Eduardo Argal and mirurita";
  }

}
