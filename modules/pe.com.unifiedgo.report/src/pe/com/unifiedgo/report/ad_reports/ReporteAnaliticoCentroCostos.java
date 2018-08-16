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
 * All portions are Copyright (C) 2001-2014 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package pe.com.unifiedgo.report.ad_reports;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.AccountingSchemaMiscData;
import org.openbravo.erpCommon.businessUtility.Tree;
import org.openbravo.erpCommon.businessUtility.TreeData;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class ReporteAnaliticoCentroCostos extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strcAcctSchemaId = vars.getGlobalVariable("inpcAcctSchemaId",
          "ReporteAnaliticoCentroCostos|cAcctSchemaId", "");
      String strDateFrom = vars.getGlobalVariable("inpDateFrom",
          "ReporteAnaliticoCentroCostos|DateFrom", SREDateTimeData.FirstDayOfMonth(this));
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReporteAnaliticoCentroCostos|DateTo",
          SREDateTimeData.today(this));
      String strPageNo = vars.getGlobalVariable("inpPageNo", "ReporteAnaliticoCentroCostos|PageNo",
          "1");
      String strAmtFrom = vars.getNumericGlobalVariable("inpAmtFrom",
          "ReporteAnaliticoCentroCostos|AmtFrom", "");
      String strAmtTo = vars.getNumericGlobalVariable("inpAmtTo",
          "ReporteAnaliticoCentroCostos|AmtTo", "");
      String strcelementvaluefrom = vars.getGlobalVariable("inpcElementValueIdFrom",
          "ReporteAnaliticoCentroCostos|C_ElementValue_IDFROM", "");
      String strcelementvalueto = vars.getGlobalVariable("inpcElementValueIdTo",
          "ReporteAnaliticoCentroCostos|C_ElementValue_IDTO", "");

      String strcelementvaluefromdes = "", strcelementvaluetodes = "";
      if (!strcelementvaluefrom.equals(""))
        strcelementvaluefromdes = ReporteAnaliticoCentroCostosData.selectSubaccountDescription(this,
            strcelementvaluefrom);
      if (!strcelementvalueto.equals(""))
        strcelementvaluetodes = ReporteAnaliticoCentroCostosData.selectSubaccountDescription(this,
            strcelementvalueto);
      strcelementvaluefromdes = (strcelementvaluefromdes.equals("null")) ? ""
          : strcelementvaluefromdes;
      strcelementvaluetodes = (strcelementvaluetodes.equals("null")) ? "" : strcelementvaluetodes;
      vars.setSessionValue("inpElementValueIdFrom_DES", strcelementvaluefromdes);
      vars.setSessionValue("inpElementValueIdTo_DES", strcelementvaluetodes);
      String strOrg = vars.getGlobalVariable("inpOrg", "ReporteAnaliticoCentroCostos|Org", "0");
      String strcBpartnerId = vars.getInGlobalVariable("inpcBPartnerId_IN",
          "ReporteAnaliticoCentroCostos|cBpartnerId", "", IsIDFilter.instance);
      String strmProductId = vars.getInGlobalVariable("inpmProductId_IN",
          "ReporteAnaliticoCentroCostos|mProductId", "", IsIDFilter.instance);
      String strcProjectId = vars.getInGlobalVariable("inpcProjectId_IN",
          "ReporteAnaliticoCentroCostos|cProjectId", "", IsIDFilter.instance);
      String strDescargarLibroElectronico = vars.getGlobalVariable("inpDescargarLibroElectronico",
          "ReporteAnaliticoCentroCostos|DescargarLibroElectronico", "");

      String strGroupBy = "";
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strPageNo, strAmtFrom, strAmtTo,
          strcelementvaluefrom, strcelementvalueto, strOrg, strcBpartnerId, strmProductId,
          strcProjectId, strGroupBy, strcAcctSchemaId, strcelementvaluefromdes,
          strcelementvaluetodes, strDescargarLibroElectronico);
    } else if (vars.commandIn("PREVIOUS_RELATION")) {
      String strInitRecord = vars.getSessionValue("ReporteAnaliticoCentroCostos.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRange",
          "ReporteAnaliticoCentroCostos");
      int intRecordRange = strRecordRange.equals("") ? 0 : Integer.parseInt(strRecordRange);
      if (strInitRecord.equals("") || strInitRecord.equals("0"))
        vars.setSessionValue("ReporteAnaliticoCentroCostos.initRecordNumber", "0");
      else {
        int initRecord = (strInitRecord.equals("") ? 0 : Integer.parseInt(strInitRecord));
        initRecord -= intRecordRange;
        strInitRecord = ((initRecord < 0) ? "0" : Integer.toString(initRecord));
        vars.setSessionValue("ReporteAnaliticoCentroCostos.initRecordNumber", strInitRecord);
      }
      response.sendRedirect(getDireccion() + request.getServletPath());
    } else if (vars.commandIn("NEXT_RELATION")) {
      String strInitRecord = vars.getSessionValue("ReporteAnaliticoCentroCostos.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRange",
          "ReporteAnaliticoCentroCostos");
      int intRecordRange = strRecordRange.equals("") ? 0 : Integer.parseInt(strRecordRange);
      int initRecord = (strInitRecord.equals("") ? 0 : Integer.parseInt(strInitRecord));
      // if (initRecord == 0)
      // initRecord = 1; Removed by DAL 30/4/09
      initRecord += intRecordRange;
      strInitRecord = ((initRecord < 0) ? "0" : Integer.toString(initRecord));
      vars.setSessionValue("ReporteAnaliticoCentroCostos.initRecordNumber", strInitRecord);
      response.sendRedirect(getDireccion() + request.getServletPath());
    } else if (vars.commandIn("PDF", "XLS")) {
      String strcAcctSchemaId = vars.getRequestGlobalVariable("inpcAcctSchemaId",
          "ReporteAnaliticoCentroCostos|cAcctSchemaId");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReporteAnaliticoCentroCostos|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReporteAnaliticoCentroCostos|DateTo");
      String strAmtFrom = vars.getNumericParameter("inpAmtFrom");
      vars.setSessionValue("ReporteAnaliticoCentroCostos|AmtFrom", strAmtFrom);
      String strAmtTo = vars.getNumericParameter("inpAmtTo");
      vars.setSessionValue("ReporteAnaliticoCentroCostos|AmtTo", strAmtTo);
      String strcelementvaluefrom = vars.getRequestGlobalVariable("inpcElementValueIdFrom",
          "ReporteAnaliticoCentroCostos|C_ElementValue_IDFROM");
      String strcelementvalueto = vars.getRequestGlobalVariable("inpcElementValueIdTo",
          "ReporteAnaliticoCentroCostos|C_ElementValue_IDTO");
      String strcprojectid = vars.getRequestGlobalVariable("inpcProjectId",
          "ReporteAnaliticoCentroCostos|C_Project_IDTO");
      String strNroDocumento = vars.getStringParameter("nroDocumento");

      String strcelementvaluefromdes = "", strcelementvaluetodes = "";
      if (!strcelementvaluefrom.equals(""))
        strcelementvaluefromdes = ReporteAnaliticoCentroCostosData.selectSubaccountDescription(this,
            strcelementvaluefrom);
      if (!strcelementvalueto.equals(""))
        strcelementvaluetodes = ReporteAnaliticoCentroCostosData.selectSubaccountDescription(this,
            strcelementvalueto);

      String strOrg = vars.getGlobalVariable("inpOrg", "ReporteAnaliticoCentroCostos|Org", "0");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN",
          "ReporteAnaliticoCentroCostos|cBpartnerId", IsIDFilter.instance);
      String strmProductId = vars.getInGlobalVariable("inpmProductId_IN",
          "ReporteAnaliticoCentroCostos|mProductId", "", IsIDFilter.instance);
      String strcProjectId = vars.getInGlobalVariable("inpcProjectId_IN",
          "ReporteAnaliticoCentroCostos|cProjectId", "", IsIDFilter.instance);
      String strDescargarLibroElectronico = vars.getStringParameter("inpDescargarLibroElectronico");

      String strructercero = vars.getStringParameter("inpcBPartnerId");

      String strcdesde = strcelementvaluefromdes.contains("-")
          ? strcelementvaluefromdes.split("-")[0].trim() : "";

      String strchasta = strcelementvaluetodes.contains("-")
          ? strcelementvaluetodes.split("-")[0].trim() : "";

      String strPageNo = vars.getGlobalVariable("inpPageNo", "ReporteAnaliticoCentroCostos|PageNo",
          "1");

      String strGroupBy = vars.getRequestGlobalVariable("inpGroupBy",
          "ReporteAnaliticoCentroCostos|GroupBy");

      String strGenerarExcelConFormato = vars.getStringParameter("inpGenerarExcelGrupo");

      String strCentroCostoId = vars.getStringParameter("inpCCostcenterId");
      String strRuc = vars.getStringParameter("inpRuc");

      // if (vars.commandIn("PDF"))
      printPageDataPDF_ugo(request, response, vars, strDateFrom, strDateTo, strAmtFrom, strAmtTo,
          strcelementvaluefrom, strcelementvalueto, strOrg, strcBpartnerId, strmProductId,
          strcProjectId, strGroupBy, strcAcctSchemaId, strPageNo, strDescargarLibroElectronico,
          strructercero, strcdesde, strchasta, strGenerarExcelConFormato, strcprojectid,
          strNroDocumento, strCentroCostoId, strRuc);
      // else
      // printPageDataXLS(request, response, vars, strDateFrom,
      // strDateTo, strAmtFrom, strAmtTo, strcelementvaluefrom,
      // strcelementvalueto, strOrg, strcBpartnerId,
      // strmProductId, strcProjectId, strGroupBy,
      // strcAcctSchemaId, strDescargarLibroElectronico);
    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strPageNo, String strAmtFrom, String strAmtTo,
      String strcelementvaluefrom, String strcelementvalueto, String strOrg, String strcBpartnerId,
      String strmProductId, String strcProjectId, String strGroupBy, String strcAcctSchemaId,
      String strcelementvaluefromdes, String strcelementvaluetodes,
      String strDescargarLibroElectronico) throws IOException, ServletException {

    String strRecordRange = Utility.getContext(this, vars, "#RecordRange",
        "ReporteAnaliticoCentroCostos");
    int intRecordRange = (strRecordRange.equals("") ? 0 : Integer.parseInt(strRecordRange));
    String strInitRecord = vars.getSessionValue("ReporteAnaliticoCentroCostos.initRecordNumber");
    int initRecordNumber = (strInitRecord.equals("") ? 0 : Integer.parseInt(strInitRecord));
    // built limit/offset parameters for oracle/postgres
    String rowNum = "0";
    String oraLimit1 = null;
    String oraLimit2 = null;
    String pgLimit = null;
    if (intRecordRange != 0) {
      if (this.myPool.getRDBMS().equalsIgnoreCase("ORACLE")) {
        rowNum = "ROWNUM";
        oraLimit1 = String.valueOf(initRecordNumber + intRecordRange);
        oraLimit2 = (initRecordNumber + 1) + " AND " + oraLimit1;
      } else {
        rowNum = "0";
        pgLimit = intRecordRange + " OFFSET " + initRecordNumber;
      }
    }
    log4j.debug("offset= " + initRecordNumber + " pageSize= " + intRecordRange);
    log4j.debug("Output: dataSheet");
    log4j.debug("Date From:" + strDateFrom + "- To:" + strDateTo + " - Schema:" + strcAcctSchemaId);
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReporteAnaliticoCentroCostosData[] data = null;
    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    String strOrgFamily = getFamily(strTreeOrg, strOrg);
    String toDatePlusOne = DateTimeData.nDaysAfter(this, strDateTo, "1");

    String strGroupByText = (strGroupBy.equals("BPartner")
        ? Utility.messageBD(this, "BusPartner", vars.getLanguage())
        : (strGroupBy.equals("Product") ? Utility.messageBD(this, "Product", vars.getLanguage())
            : (strGroupBy.equals("Project") ? Utility.messageBD(this, "Project", vars.getLanguage())
                : "")));

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReporteAnaliticoCentroCostos", true,
        "", "", "imprimir();return false;", false, "ad_reports", strReplaceWith, false, true);

    toolbar.prepareSimpleToolBarTemplate();
    toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");

    String strcBpartnerIdAux = strcBpartnerId;
    String strmProductIdAux = strmProductId;
    String strcProjectIdAux = strcProjectId;
    if (strDateFrom.equals("") && strDateTo.equals("")) {
      String discard[] = { "sectionAmount", "sectionPartner" };
      xmlDocument = xmlEngine
          .readXmlTemplate("pe/com/unifiedgo/report/ad_reports/ReporteAnaliticoCentroCostos",
              discard)
          .createXmlDocument();
      // toolbar
      // .prepareRelationBarTemplate(false, false,
      // "submitCommandForm('XLS', false, frmMain, 'ReporteAnaliticoCentroCostosExcel.xls',
      // 'EXCEL');return false;");
      data = ReporteAnaliticoCentroCostosData.set();
    } else {
      String[] discard = { "discard" };
      if (strGroupBy.equals(""))
        discard[0] = "sectionPartner";
      else
        discard[0] = "sectionAmount";
      BigDecimal previousDebit = BigDecimal.ZERO;
      BigDecimal previousCredit = BigDecimal.ZERO;
      String strAllaccounts = "Y";
      if (strcelementvaluefrom != null && !strcelementvaluefrom.equals("")) {
        if (strcelementvalueto.equals("")) {
          strcelementvalueto = strcelementvaluefrom;
          strcelementvaluetodes = ReporteAnaliticoCentroCostosData.selectSubaccountDescription(this,
              strcelementvalueto);
          vars.setSessionValue("inpElementValueIdTo_DES", strcelementvaluetodes);

        }
        strAllaccounts = "N";
        log4j.debug("##################### strcelementvaluefrom= " + strcelementvaluefrom);
        log4j.debug("##################### strcelementvalueto= " + strcelementvalueto);
      } else {
        strcelementvalueto = "";
        strcelementvaluetodes = "";
        vars.setSessionValue("inpElementValueIdTo_DES", strcelementvaluetodes);
      }
      Long initMainSelect = System.currentTimeMillis();
      ReporteAnaliticoCentroCostosData scroll = null;

      log4j.debug("Select2. Time in mils: " + (System.currentTimeMillis() - initMainSelect));
      log4j.debug("RecordNo: " + initRecordNumber);

      ReporteAnaliticoCentroCostosData[] dataTotal = null;
      ReporteAnaliticoCentroCostosData[] dataSubtotal = null;
      String strOld = "";
      // boolean firstPagBlock = false;
      ReporteAnaliticoCentroCostosData[] subreportElement = new ReporteAnaliticoCentroCostosData[1];

      // TODO: What is strTotal?? is this the proper variable name?
      String strTotal = "";
      subreportElement = new ReporteAnaliticoCentroCostosData[1];

      boolean hasPrevious = !(data == null || data.length == 0 || initRecordNumber <= 1);
      boolean hasNext = !(data == null || data.length == 0 || data.length < intRecordRange);
      // toolbar
      // .prepareRelationBarTemplate(hasPrevious, hasNext,
      // "submitCommandForm('XLS', true, frmMain, 'ReporteAnaliticoCentroCostosExcel.xls',
      // 'EXCEL');return false;");
      xmlDocument = xmlEngine
          .readXmlTemplate("pe/com/unifiedgo/report/ad_reports/ReporteAnaliticoCentroCostos",
              discard)
          .createXmlDocument();
    }
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "pe.com.unifiedgo.report.ad_reports.ReporteAnaliticoCentroCostos");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ReporteAnaliticoCentroCostos.html", classInfo.id, classInfo.type, strReplaceWith,
          tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
          "ReporteAnaliticoCentroCostos.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReporteAnaliticoCentroCostos");
      vars.removeMessage("ReporteAnaliticoCentroCostos");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
          "0C754881EAD94243A161111916E9B9C6",
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ReporteAnaliticoCentroCostos"),
          Utility.getContext(this, vars, "#User_Client", "ReporteAnaliticoCentroCostos"), '*');
      comboTableData.fillParameters(null, "ReporteAnaliticoCentroCostos", "");
      xmlDocument.setData("reportAD_ORGID", "liststructure", comboTableData.select(false));
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("PageNo", strPageNo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("amtFrom", strAmtFrom);
    xmlDocument.setParameter("amtTo", strAmtTo);
    xmlDocument.setParameter("adOrgId", strOrg);
    xmlDocument.setParameter("cAcctschemaId", strcAcctSchemaId);

    xmlDocument.setParameter("paramElementvalueIdTo", strcelementvalueto);
    xmlDocument.setParameter("paramElementvalueIdFrom", strcelementvaluefrom);
    xmlDocument.setParameter("inpElementValueIdTo_DES", strcelementvaluetodes);
    xmlDocument.setParameter("inpElementValueIdFrom_DES", strcelementvaluefromdes);
    xmlDocument.setParameter("paramDescargarLibroElectronico",
        !strDescargarLibroElectronico.equals("Y") ? "0" : "1");

    // xmlDocument.setParameter("groupbyselected", strGroupBy);
    // xmlDocument.setData("reportCBPartnerId_IN", "liststructure",
    // SelectorUtilityData.selectBpartner(this,
    // Utility.getContext(this, vars, "#AccessibleOrgTree", ""),
    // Utility.getContext(this, vars, "#User_Client", ""), strcBpartnerIdAux));
    // xmlDocument.setData("reportMProductId_IN", "liststructure",
    // SelectorUtilityData.selectMproduct(this,
    // Utility.getContext(this, vars, "#AccessibleOrgTree", ""),
    // Utility.getContext(this, vars, "#User_Client", ""), strmProductIdAux));
    // xmlDocument.setData("reportCProjectId_IN", "liststructure",
    // SelectorUtilityData.selectProject(this,
    // Utility.getContext(this, vars, "#AccessibleOrgTree", ""),
    // Utility.getContext(this, vars, "#User_Client", ""), strcProjectIdAux));
    xmlDocument.setData("reportC_ACCTSCHEMA_ID", "liststructure",
        AccountingSchemaMiscData.selectC_ACCTSCHEMA_ID(this,
            Utility.getContext(this, vars, "#AccessibleOrgTree", "ReporteAnaliticoCentroCostos"),
            Utility.getContext(this, vars, "#User_Client", "ReporteAnaliticoCentroCostos"),
            strcAcctSchemaId));

    // xmlDocument.setParameter("paramBPartnerId", strcBpartnerId);
    // xmlDocument.setParameter("paramBPartnerDescription",
    // ReporteAnaliticoCentroCostosData.selectBPartner(this, strcBpartnerId));

    // xmlDocument.setParameter("paramLibrosArray",
    // Utility.arrayInfinitasEntradas("idlibro;nombrelibro;idorganizacion", "arrLibroMayor",
    // ReporteAnaliticoCentroCostosData.select_libros(this)));

    xmlDocument.setParameter("paramPeriodosArray",
        Utility.arrayInfinitasEntradas("idperiodo;periodo;fechainicial;fechafinal;idorganizacion",
            "arrPeriodos", ReporteAnaliticoCentroCostosData.select_periodos(this)));

    // xmlDocument.setParameter("paramElementvalueIdTo",
    // strcelementvalueto);
    // xmlDocument.setParameter("paramElementvalueIdFrom",
    // strcelementvaluefrom);
    // xmlDocument.setParameter("inpElementValueIdTo_DES",
    // strcelementvaluetodes);
    // xmlDocument.setParameter("inpElementValueIdFrom_DES",
    // strcelementvaluefromdes);

    // log4j.debug("data.length: " + data.length);
    //
    // if (data != null && data.length > 0) {
    // if (strGroupBy.equals(""))
    // xmlDocument.setData("structure1", data);
    // else
    // xmlDocument.setData("structure2", data);
    // }

    out.println(xmlDocument.print());
    out.close();
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

  private void printPageDataPDF_ugo(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String strAmtFrom,
      String strAmtTo, String strcelementvaluefrom, String strcelementvalueto, String strOrg,
      String strcBpartnerId, String strmProductId, String strcProjectId, String strGroupBy,
      String strcAcctSchemaId, String strPageNo, String strSoloPendiente, String strructercero,
      String strcdesde, String strchasta, String strGenerarExcelConFormato, String strcprojectid,
      String strNroDocumento, String strCentroCostoId, String strRuc)
      throws IOException, ServletException {
    log4j.debug("Output: PDF");

    ReporteAnaliticoCentroCostosData[] data = null;
    ReporteAnaliticoCentroCostosData[] saldoInicial = null;

    ArrayList<ReporteAnaliticoCentroCostosData> listData = new ArrayList<ReporteAnaliticoCentroCostosData>();
    HashMap<String, BigDecimal> grupoSaldoIniCostos = new HashMap<>();
    HashMap<String, BigDecimal> grupoSaldoIniCostosCuentas = new HashMap<>();

    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    String strOrgFamily = getFamily(strTreeOrg, strOrg);

    if (strructercero.compareToIgnoreCase("") != 0) {
      BusinessPartner cbp = OBDal.getInstance().get(BusinessPartner.class, strructercero);
      strructercero = cbp.getTaxID();
    }

    if (!strRuc.equalsIgnoreCase("")) {
      strRuc = "%" + strRuc + "%";
    }

    if (!strNroDocumento.equalsIgnoreCase("")) {
      strNroDocumento = "%" + strNroDocumento + "%";
    }

    String anio = strDateFrom.split("-")[2];
    String strDateAnioIni = "01-01-" + anio;

    saldoInicial = ReporteAnaliticoCentroCostosData.select_saldo_incial(this,
        Utility.getContext(this, vars, "#User_Client", "ReporteAnaliticoCentroCostos"),
        strOrgFamily, strDateAnioIni, strDateFrom);

    data = ReporteAnaliticoCentroCostosData.select_costos(this,
        Utility.getContext(this, vars, "#User_Client", "ReporteAnaliticoCentroCostos"),
        strOrgFamily, strDateFrom, strDateTo, strcdesde, strCentroCostoId, strRuc, strNroDocumento);

    for (int i = 0; i < saldoInicial.length; i++) {
      ReporteAnaliticoCentroCostosData e = saldoInicial[i];
      String keyCostoCuenta = e.codigoCentro + "||" + e.centroCosto + "||" + e.cuenta;
      String keyCosto = e.codigoCentro + "||" + e.centroCosto;

      grupoSaldoIniCostosCuentas.put(keyCostoCuenta, new BigDecimal(e.monto));

      BigDecimal saldoCosto = new BigDecimal(e.monto);
      if (grupoSaldoIniCostos.containsKey(keyCosto)) {
        saldoCosto = saldoCosto.add(grupoSaldoIniCostos.get(keyCosto));
      }
      grupoSaldoIniCostos.put(keyCosto, saldoCosto);
    }

    for (int i = 0; i < data.length; i++) {
      ReporteAnaliticoCentroCostosData e = data[i];
      String keyCostoCuenta = e.codigoCentro + "||" + e.centroCosto + "||" + e.cuenta;
      String keyCosto = e.codigoCentro + "||" + e.centroCosto;
      e.saldoInicialCosto = grupoSaldoIniCostos.containsKey(keyCosto)
          ? grupoSaldoIniCostos.get(keyCosto).toString() : "0.00";
      e.saldoInicialCuenta = grupoSaldoIniCostosCuentas.containsKey(keyCostoCuenta)
          ? grupoSaldoIniCostosCuentas.get(keyCostoCuenta).toString() : "0.00";
    }

    if (data.length == 0) {
      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
      return;
    }

    String strOutput;
    String strReportName;
    if (vars.commandIn("PDF")) {
      strOutput = "pdf";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteAnaliticoCentroCostos.jrxml";
    } else {
      strOutput = "xls";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteAnaliticoCentroCostosExcel.jrxml";
    }

    HashMap<String, Object> parameters = new HashMap<String, Object>();

    String organizacion = ReporteAnaliticoCentroCostosData.selectSocialName(this, strOrg);
    String rucOrganizacion = ReporteAnaliticoCentroCostosData.selectRucOrg(this, strOrg);

    parameters.put("Razon",
        organizacion.compareToIgnoreCase("") != 0 ? organizacion : "TODAS LAS ORGANIZACIONES");
    parameters.put("Ruc", rucOrganizacion.compareToIgnoreCase("0") != 0 ? rucOrganizacion : "-");

    parameters.put("dateFrom", StringToDate(strDateFrom));
    parameters.put("dateTo", StringToDate(strDateTo));
    renderJR(vars, response, strReportName, "Reporte_Analitico_Centro_Costos", strOutput,
        parameters, data, null);

  }

  private String getFamily(String strTree, String strChild) throws IOException, ServletException {
    return Tree.getMembers(this, strTree, strChild);
  }

  @Override
  public String getServletInfo() {
    return "Servlet ReporteAnaliticoCentroCostos. This Servlet was made by Pablo Sarobe";
  } // end of getServletInfo() method
}
