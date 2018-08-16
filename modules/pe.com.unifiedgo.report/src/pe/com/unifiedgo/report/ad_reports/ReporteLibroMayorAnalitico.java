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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.Tree;
import org.openbravo.erpCommon.businessUtility.TreeData;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.info.SelectorUtilityData;
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

public class ReporteLibroMayorAnalitico extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strcAcctSchemaId = vars.getGlobalVariable("inpcAcctSchemaId",
          "ReporteLibroMayorAnalitico|cAcctSchemaId", "");
      String strDateFrom = vars.getGlobalVariable("inpDateFrom",
          "ReporteLibroMayorAnalitico|DateFrom", SREDateTimeData.FirstDayOfMonth(this));
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReporteLibroMayorAnalitico|DateTo",
          SREDateTimeData.today(this));
      String strPageNo = vars.getGlobalVariable("inpPageNo", "ReporteLibroMayorAnalitico|PageNo",
          "1");
      String strAmtFrom = vars.getNumericGlobalVariable("inpAmtFrom",
          "ReporteLibroMayorAnalitico|AmtFrom", "");
      String strAmtTo = vars.getNumericGlobalVariable("inpAmtTo",
          "ReporteLibroMayorAnalitico|AmtTo", "");
      String strcelementvaluefrom = vars.getGlobalVariable("inpcElementValueIdFrom",
          "ReporteLibroMayorAnalitico|C_ElementValue_IDFROM", "");
      String strcelementvalueto = vars.getGlobalVariable("inpcElementValueIdTo",
          "ReporteLibroMayorAnalitico|C_ElementValue_IDTO", "");

      String strcelementvaluefromdes = "", strcelementvaluetodes = "";
      if (!strcelementvaluefrom.equals(""))
        strcelementvaluefromdes = ReporteLibroMayorAnaliticoData.selectSubaccountDescription(this,
            strcelementvaluefrom);
      if (!strcelementvalueto.equals(""))
        strcelementvaluetodes = ReporteLibroMayorAnaliticoData.selectSubaccountDescription(this,
            strcelementvalueto);
      strcelementvaluefromdes = (strcelementvaluefromdes.equals("null")) ? ""
          : strcelementvaluefromdes;
      strcelementvaluetodes = (strcelementvaluetodes.equals("null")) ? "" : strcelementvaluetodes;
      vars.setSessionValue("inpElementValueIdFrom_DES", strcelementvaluefromdes);
      vars.setSessionValue("inpElementValueIdTo_DES", strcelementvaluetodes);
      String strOrg = vars.getGlobalVariable("inpOrg", "ReporteLibroMayorAnalitico|Org", "0");
      String strcBpartnerId = vars.getInGlobalVariable("inpcBPartnerId_IN",
          "ReporteLibroMayorAnalitico|cBpartnerId", "", IsIDFilter.instance);
      String strmProductId = vars.getInGlobalVariable("inpmProductId_IN",
          "ReporteLibroMayorAnalitico|mProductId", "", IsIDFilter.instance);
      String strcProjectId = vars.getInGlobalVariable("inpcProjectId_IN",
          "ReporteLibroMayorAnalitico|cProjectId", "", IsIDFilter.instance);
      String strDescargarLibroElectronico = vars.getGlobalVariable("inpDescargarLibroElectronico",
          "ReporteLibroMayorAnalitico|DescargarLibroElectronico", "");

      String strGroupBy = "";
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strPageNo, strAmtFrom, strAmtTo,
          strcelementvaluefrom, strcelementvalueto, strOrg, strcBpartnerId, strmProductId,
          strcProjectId, strGroupBy, strcAcctSchemaId, strcelementvaluefromdes,
          strcelementvaluetodes, strDescargarLibroElectronico);
    } else if (vars.commandIn("PREVIOUS_RELATION")) {
      String strInitRecord = vars.getSessionValue("ReporteLibroMayorAnalitico.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRange",
          "ReporteLibroMayorAnalitico");
      int intRecordRange = strRecordRange.equals("") ? 0 : Integer.parseInt(strRecordRange);
      if (strInitRecord.equals("") || strInitRecord.equals("0"))
        vars.setSessionValue("ReporteLibroMayorAnalitico.initRecordNumber", "0");
      else {
        int initRecord = (strInitRecord.equals("") ? 0 : Integer.parseInt(strInitRecord));
        initRecord -= intRecordRange;
        strInitRecord = ((initRecord < 0) ? "0" : Integer.toString(initRecord));
        vars.setSessionValue("ReporteLibroMayorAnalitico.initRecordNumber", strInitRecord);
      }
      response.sendRedirect(getDireccion() + request.getServletPath());
    } else if (vars.commandIn("NEXT_RELATION")) {
      String strInitRecord = vars.getSessionValue("ReporteLibroMayorAnalitico.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRange",
          "ReporteLibroMayorAnalitico");
      int intRecordRange = strRecordRange.equals("") ? 0 : Integer.parseInt(strRecordRange);
      int initRecord = (strInitRecord.equals("") ? 0 : Integer.parseInt(strInitRecord));
      // if (initRecord == 0)
      // initRecord = 1; Removed by DAL 30/4/09
      initRecord += intRecordRange;
      strInitRecord = ((initRecord < 0) ? "0" : Integer.toString(initRecord));
      vars.setSessionValue("ReporteLibroMayorAnalitico.initRecordNumber", strInitRecord);
      response.sendRedirect(getDireccion() + request.getServletPath());
    } else if (vars.commandIn("PDF", "XLS")) {
      String strcAcctSchemaId = vars.getRequestGlobalVariable("inpcAcctSchemaId",
          "ReporteLibroMayorAnalitico|cAcctSchemaId");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReporteLibroMayorAnalitico|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReporteLibroMayorAnalitico|DateTo");
      String strAmtFrom = vars.getNumericParameter("inpAmtFrom");
      vars.setSessionValue("ReporteLibroMayorAnalitico|AmtFrom", strAmtFrom);
      String strAmtTo = vars.getNumericParameter("inpAmtTo");
      vars.setSessionValue("ReporteLibroMayorAnalitico|AmtTo", strAmtTo);
      String strcelementvaluefrom = vars.getRequestGlobalVariable("inpcElementValueIdFrom",
          "ReporteLibroMayorAnalitico|C_ElementValue_IDFROM");
      String strcelementvalueto = vars.getRequestGlobalVariable("inpcElementValueIdTo",
          "ReporteLibroMayorAnalitico|C_ElementValue_IDTO");
      String strcprojectid = vars.getRequestGlobalVariable("inpcProjectId",
          "ReporteLibroMayorAnalitico|C_Project_IDTO");
      String strNroDocumento = vars.getStringParameter("nroDocumento");

      String strcelementvaluefromdes = "", strcelementvaluetodes = "";
      if (!strcelementvaluefrom.equals(""))
        strcelementvaluefromdes = ReporteLibroMayorAnaliticoData.selectSubaccountDescription(this,
            strcelementvaluefrom);
      if (!strcelementvalueto.equals(""))
        strcelementvaluetodes = ReporteLibroMayorAnaliticoData.selectSubaccountDescription(this,
            strcelementvalueto);

      String strOrg = vars.getGlobalVariable("inpOrg", "ReporteLibroMayorAnalitico|Org", "0");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN",
          "ReporteLibroMayorAnalitico|cBpartnerId", IsIDFilter.instance);
      String strmProductId = vars.getInGlobalVariable("inpmProductId_IN",
          "ReporteLibroMayorAnalitico|mProductId", "", IsIDFilter.instance);
      String strcProjectId = vars.getInGlobalVariable("inpcProjectId_IN",
          "ReporteLibroMayorAnalitico|cProjectId", "", IsIDFilter.instance);
      String strDescargarLibroElectronico = vars.getStringParameter("inpDescargarLibroElectronico");

      String strructercero = vars.getStringParameter("inpcBPartnerId");

      String strcdesde = strcelementvaluefromdes.contains("-")
          ? strcelementvaluefromdes.split("-")[0].trim() : "";

      String strchasta = strcelementvaluetodes.contains("-")
          ? strcelementvaluetodes.split("-")[0].trim() : "";

      String strPageNo = vars.getGlobalVariable("inpPageNo", "ReporteLibroMayorAnalitico|PageNo",
          "1");

      String strGroupBy = vars.getRequestGlobalVariable("inpGroupBy",
          "ReporteLibroMayorAnalitico|GroupBy");

      String strGenerarExcelConFormato = vars.getStringParameter("inpGenerarExcelGrupo");

      // if (vars.commandIn("PDF"))
      printPageDataPDF_ugo(request, response, vars, strDateFrom, strDateTo, strAmtFrom, strAmtTo,
          strcelementvaluefrom, strcelementvalueto, strOrg, strcBpartnerId, strmProductId,
          strcProjectId, strGroupBy, strcAcctSchemaId, strPageNo, strDescargarLibroElectronico,
          strructercero, strcdesde, strchasta, strGenerarExcelConFormato, strcprojectid,
          strNroDocumento);
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
        "ReporteLibroMayorAnalitico");
    int intRecordRange = (strRecordRange.equals("") ? 0 : Integer.parseInt(strRecordRange));
    String strInitRecord = vars.getSessionValue("ReporteLibroMayorAnalitico.initRecordNumber");
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
    ReporteLibroMayorAnaliticoData[] data = null;
    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    String strOrgFamily = getFamily(strTreeOrg, strOrg);
    String toDatePlusOne = DateTimeData.nDaysAfter(this, strDateTo, "1");

    String strGroupByText = (strGroupBy.equals("BPartner")
        ? Utility.messageBD(this, "BusPartner", vars.getLanguage())
        : (strGroupBy.equals("Product") ? Utility.messageBD(this, "Product", vars.getLanguage())
            : (strGroupBy.equals("Project") ? Utility.messageBD(this, "Project", vars.getLanguage())
                : "")));

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReporteLibroMayorAnalitico", true, "",
        "", "imprimir();return false;", false, "ad_reports", strReplaceWith, false, true);

    toolbar.prepareSimpleToolBarTemplate();
    toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");

    String strcBpartnerIdAux = strcBpartnerId;
    String strmProductIdAux = strmProductId;
    String strcProjectIdAux = strcProjectId;
    if (strDateFrom.equals("") && strDateTo.equals("")) {
      String discard[] = { "sectionAmount", "sectionPartner" };
      xmlDocument = xmlEngine
          .readXmlTemplate("pe/com/unifiedgo/report/ad_reports/ReporteLibroMayorAnalitico", discard)
          .createXmlDocument();
      // toolbar
      // .prepareRelationBarTemplate(false, false,
      // "submitCommandForm('XLS', false, frmMain, 'ReporteLibroMayorAnaliticoExcel.xls',
      // 'EXCEL');return false;");
      data = ReporteLibroMayorAnaliticoData.set();
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
          strcelementvaluetodes = ReporteLibroMayorAnaliticoData.selectSubaccountDescription(this,
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
      ReporteLibroMayorAnaliticoData scroll = null;
      try {
        scroll = ReporteLibroMayorAnaliticoData.select2(this, rowNum, strGroupByText, strGroupBy,
            strAllaccounts, strcelementvaluefrom, strcelementvalueto,
            Utility.getContext(this, vars, "#AccessibleOrgTree", "ReporteLibroMayorAnalitico"),
            Utility.getContext(this, vars, "#User_Client", "ReporteLibroMayorAnalitico"),
            strcAcctSchemaId, strDateFrom, toDatePlusOne, strOrgFamily, strcBpartnerId,
            strmProductId, strcProjectId, strAmtFrom, strAmtTo, null, null, pgLimit, oraLimit1,
            oraLimit2, null);
        Vector<ReporteLibroMayorAnaliticoData> res = new Vector<ReporteLibroMayorAnaliticoData>();
        while (scroll.next()) {
          res.add(scroll.get());
        }
        data = new ReporteLibroMayorAnaliticoData[res.size()];
        res.copyInto(data);
      } finally {
        if (scroll != null) {
          scroll.close();
        }
      }

      log4j.debug("Select2. Time in mils: " + (System.currentTimeMillis() - initMainSelect));
      log4j.debug("RecordNo: " + initRecordNumber);

      ReporteLibroMayorAnaliticoData[] dataTotal = null;
      ReporteLibroMayorAnaliticoData[] dataSubtotal = null;
      String strOld = "";
      // boolean firstPagBlock = false;
      ReporteLibroMayorAnaliticoData[] subreportElement = new ReporteLibroMayorAnaliticoData[1];
      for (int i = 0; data != null && i < data.length; i++) {
        if (!strOld.equals(data[i].groupbyid + data[i].id)) {
          subreportElement = new ReporteLibroMayorAnaliticoData[1];
          // firstPagBlock = false;
          if (i == 0 && initRecordNumber > 0) {
            // firstPagBlock = true;
            Long init = System.currentTimeMillis();
            dataTotal = ReporteLibroMayorAnaliticoData.select2Total(this, rowNum, strGroupByText,
                strGroupBy, strAllaccounts, strcelementvaluefrom, strcelementvalueto,
                Utility.getContext(this, vars, "#AccessibleOrgTree", "ReporteLibroMayorAnalitico"),
                Utility.getContext(this, vars, "#User_Client", "ReporteLibroMayorAnalitico"),
                strcAcctSchemaId, "", DateTimeData.nDaysAfter(this, data[0].dateacct, "1"),
                strOrgFamily, strcBpartnerId, strmProductId, strcProjectId, strAmtFrom, strAmtTo,
                data[0].id, data[0].groupbyid, null, null, null, data[0].dateacctnumber
                    + data[0].factaccttype + data[0].factAcctGroupId + data[0].factAcctId);
            dataSubtotal = ReporteLibroMayorAnaliticoData.select2sum(this, rowNum, strGroupByText,
                strGroupBy, strAllaccounts, strcelementvaluefrom, strcelementvalueto,
                Utility.getContext(this, vars, "#AccessibleOrgTree", "ReporteLibroMayorAnalitico"),
                Utility.getContext(this, vars, "#User_Client", "ReporteLibroMayorAnalitico"),
                strcAcctSchemaId, strDateFrom, toDatePlusOne, strOrgFamily,
                (strGroupBy.equals("BPartner") ? "('" + data[i].groupbyid + "')" : strcBpartnerId),
                (strGroupBy.equals("Product") ? "('" + data[i].groupbyid + "')" : strmProductId),
                (strGroupBy.equals("Project") ? "('" + data[i].groupbyid + "')" : strcProjectId),
                strAmtFrom,
                strAmtTo, null, null, null, null, null, data[0].dateacctnumber
                    + data[0].factaccttype + data[0].factAcctGroupId + data[0].factAcctId,
                data[0].id);

            log4j.debug("Select2Total. Time in mils: " + (System.currentTimeMillis() - init));
            // Now dataTotal is covered adding debit and credit
            // amounts
            for (int j = 0; dataTotal != null && j < dataTotal.length; j++) {
              previousDebit = previousDebit.add(new BigDecimal(dataTotal[j].amtacctdr));
              previousCredit = previousCredit.add(new BigDecimal(dataTotal[j].amtacctcr));
            }
            subreportElement = new ReporteLibroMayorAnaliticoData[1];
            subreportElement[0] = new ReporteLibroMayorAnaliticoData();
            subreportElement[0].totalacctdr = previousDebit.toPlainString();
            subreportElement[0].totalacctcr = previousCredit.toPlainString();
            data[0].amtacctdrprevsum = (dataSubtotal != null) ? dataSubtotal[0].amtacctdr
                : data[0].amtacctdrprevsum;
            data[0].amtacctcrprevsum = (dataSubtotal != null) ? dataSubtotal[0].amtacctcr
                : data[0].amtacctcrprevsum;
            subreportElement[0].total = previousDebit.subtract(previousCredit).toPlainString();
          } else {
            if ("".equals(data[i].groupbyid)) {
              // The argument " " is used to simulate one value
              // and put the optional parameter-->
              // AND FACT_ACCT.C_PROJECT_ID IS NULL for example
              Long init = System.currentTimeMillis();
              subreportElement = ReporteLibroMayorAnaliticoData.selectTotal2(this, strcBpartnerId,
                  (strGroupBy.equals("BPartner") ? " " : null), strmProductId,
                  (strGroupBy.equals("Product") ? " " : null), strcProjectId,
                  (strGroupBy.equals("Project") ? " " : null), strcAcctSchemaId, data[i].id, "",
                  strDateFrom, strOrgFamily);
              log4j.debug("SelectTotalNew. Time in mils: " + (System.currentTimeMillis() - init));
            } else {
              Long init = System.currentTimeMillis();
              subreportElement = ReporteLibroMayorAnaliticoData.selectTotal2(this,
                  (strGroupBy.equals("BPartner") ? "('" + data[i].groupbyid + "')"
                      : strcBpartnerId),
                  null,
                  (strGroupBy.equals("Product") ? "('" + data[i].groupbyid + "')" : strmProductId),
                  null,
                  (strGroupBy.equals("Project") ? "('" + data[i].groupbyid + "')" : strcProjectId),
                  null, strcAcctSchemaId, data[i].id, "", strDateFrom, strOrgFamily);
              log4j.debug("SelectTotalNew. Time in mils: " + (System.currentTimeMillis() - init));
            }
          }
          data[i].totalacctdr = subreportElement[0].totalacctdr;
          data[i].totalacctcr = subreportElement[0].totalacctcr;
        }

        data[i].totalacctsub = subreportElement[0].total;

        data[i].previousdebit = subreportElement[0].totalacctdr;
        data[i].previouscredit = subreportElement[0].totalacctcr;
        data[i].previoustotal = subreportElement[0].total;
        strOld = data[i].groupbyid + data[i].id;
      }
      // TODO: What is strTotal?? is this the proper variable name?
      String strTotal = "";
      subreportElement = new ReporteLibroMayorAnaliticoData[1];
      for (int i = 0; data != null && i < data.length; i++) {
        if (!strTotal.equals(data[i].groupbyid + data[i].id)) {
          subreportElement = new ReporteLibroMayorAnaliticoData[1];
          if ("".equals(data[i].groupbyid)) {
            // The argument " " is used to simulate one value and
            // put the optional parameter--> AND
            // FACT_ACCT.C_PROJECT_ID IS NULL for example
            Long init = System.currentTimeMillis();
            subreportElement = ReporteLibroMayorAnaliticoData.selectTotal2(this, strcBpartnerId,
                (strGroupBy.equals("BPartner") ? " " : null), strmProductId,
                (strGroupBy.equals("Product") ? " " : null), strcProjectId,
                (strGroupBy.equals("Project") ? " " : null), strcAcctSchemaId, data[i].id, "",
                toDatePlusOne, strOrgFamily);
            log4j.debug("SelectTotal2. Time in mils: " + (System.currentTimeMillis() - init));
          } else {
            Long init = System.currentTimeMillis();
            subreportElement = ReporteLibroMayorAnaliticoData
                .selectTotal2(this,
                    (strGroupBy.equals("BPartner") ? "('" + data[i].groupbyid + "')"
                        : strcBpartnerId),
                    null,
                    (strGroupBy.equals("Product") ? "('" + data[i].groupbyid + "')"
                        : strmProductId),
                    null,
                    (strGroupBy.equals("Project") ? "('" + data[i].groupbyid + "')"
                        : strcProjectId),
                    null, strcAcctSchemaId, data[i].id, "", toDatePlusOne, strOrgFamily);
            log4j.debug("SelectTotal2. Time in mils: " + (System.currentTimeMillis() - init));
          }
        }

        data[i].finaldebit = subreportElement[0].totalacctdr;
        data[i].finalcredit = subreportElement[0].totalacctcr;
        data[i].finaltotal = subreportElement[0].total;
        strTotal = data[i].groupbyid + data[i].id;
      }

      boolean hasPrevious = !(data == null || data.length == 0 || initRecordNumber <= 1);
      boolean hasNext = !(data == null || data.length == 0 || data.length < intRecordRange);
      // toolbar
      // .prepareRelationBarTemplate(hasPrevious, hasNext,
      // "submitCommandForm('XLS', true, frmMain, 'ReporteLibroMayorAnaliticoExcel.xls',
      // 'EXCEL');return false;");
      xmlDocument = xmlEngine
          .readXmlTemplate("pe/com/unifiedgo/report/ad_reports/ReporteLibroMayorAnalitico", discard)
          .createXmlDocument();
    }
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "pe.com.unifiedgo.report.ad_reports.ReporteLibroMayorAnalitico");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ReporteLibroMayorAnalitico.html", classInfo.id, classInfo.type, strReplaceWith,
          tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
          "ReporteLibroMayorAnalitico.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReporteLibroMayorAnalitico");
      vars.removeMessage("ReporteLibroMayorAnalitico");
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
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ReporteLibroMayorAnalitico"),
          Utility.getContext(this, vars, "#User_Client", "ReporteLibroMayorAnalitico"), '*');
      comboTableData.fillParameters(null, "ReporteLibroMayorAnalitico", "");
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
    xmlDocument.setData("reportCBPartnerId_IN", "liststructure",
        SelectorUtilityData.selectBpartner(this,
            Utility.getContext(this, vars, "#AccessibleOrgTree", ""),
            Utility.getContext(this, vars, "#User_Client", ""), strcBpartnerIdAux));
    xmlDocument.setData("reportMProductId_IN", "liststructure",
        SelectorUtilityData.selectMproduct(this,
            Utility.getContext(this, vars, "#AccessibleOrgTree", ""),
            Utility.getContext(this, vars, "#User_Client", ""), strmProductIdAux));
    xmlDocument.setData("reportCProjectId_IN", "liststructure",
        SelectorUtilityData.selectProject(this,
            Utility.getContext(this, vars, "#AccessibleOrgTree", ""),
            Utility.getContext(this, vars, "#User_Client", ""), strcProjectIdAux));
    // xmlDocument
    // .setData("reportC_ACCTSCHEMA_ID", "liststructure",
    // AccountingSchemaMiscData.selectC_ACCTSCHEMA_ID(this,
    // Utility.getContext(this, vars,
    // "#AccessibleOrgTree",
    // "ReporteLibroMayorAnalitico"), Utility
    // .getContext(this, vars, "#User_Client",
    // "ReporteLibroMayorAnalitico"),
    // strcAcctSchemaId));

    xmlDocument.setParameter("paramBPartnerId", strcBpartnerId);
    xmlDocument.setParameter("paramBPartnerDescription",
        ReporteLibroMayorAnaliticoData.selectBPartner(this, strcBpartnerId));

    xmlDocument.setParameter("paramLibrosArray",
        Utility.arrayInfinitasEntradas("idlibro;nombrelibro;idorganizacion", "arrLibroMayor",
            ReporteLibroMayorAnaliticoData.select_libros(this)));

    xmlDocument.setParameter("paramPeriodosArray",
        Utility.arrayInfinitasEntradas("idperiodo;periodo;fechainicial;fechafinal;idorganizacion",
            "arrPeriodos", ReporteLibroMayorAnaliticoData.select_periodos(this)));

    // xmlDocument.setParameter("paramElementvalueIdTo",
    // strcelementvalueto);
    // xmlDocument.setParameter("paramElementvalueIdFrom",
    // strcelementvaluefrom);
    // xmlDocument.setParameter("inpElementValueIdTo_DES",
    // strcelementvaluetodes);
    // xmlDocument.setParameter("inpElementValueIdFrom_DES",
    // strcelementvaluefromdes);

    log4j.debug("data.length: " + data.length);

    if (data != null && data.length > 0) {
      if (strGroupBy.equals(""))
        xmlDocument.setData("structure1", data);
      else
        xmlDocument.setData("structure2", data);
    }

    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageDataPDF(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String strAmtFrom,
      String strAmtTo, String strcelementvaluefrom, String strcelementvalueto, String strOrg,
      String strcBpartnerId, String strmProductId, String strcProjectId, String strGroupBy,
      String strcAcctSchemaId, String strPageNo, String strDescargarLibroElectronico)
      throws IOException, ServletException {
    log4j.debug("Output: PDF");
    // response.setContentType("text/html; charset=UTF-8");
    // ReporteLibroMayorAnaliticoData[] subreport = null;
    // String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    // String strOrgFamily = getFamily(strTreeOrg, strOrg);
    // String toDatePlusOne = DateTimeData.nDaysAfter(this, strDateTo, "1");
    //
    // String strGroupByText = (strGroupBy.equals("BPartner") ? Utility
    // .messageBD(this, "BusPartner", vars.getLanguage())
    // : (strGroupBy.equals("Product") ? Utility.messageBD(this,
    // "Product", vars.getLanguage()) : (strGroupBy
    // .equals("Project") ? Utility.messageBD(this, "Project",
    // vars.getLanguage()) : "")));
    // String strAllaccounts = "Y";
    //
    // if (strDateFrom.equals("") || strDateTo.equals("")) {
    // advisePopUp(
    // request,
    // response,
    // "WARNING",
    // Utility.messageBD(this, "ProcessStatus-W",
    // vars.getLanguage()),
    // Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
    // return;
    // }
    //
    // if (strcelementvaluefrom != null && !strcelementvaluefrom.equals(""))
    // {
    // if (strcelementvalueto.equals(""))
    // strcelementvalueto = strcelementvaluefrom;
    // strAllaccounts = "N";
    // }
    //
    // ReporteLibroMayorAnaliticoData data = null;
    // try {
    // data = ReporteLibroMayorAnaliticoData.select2(this, "0",
    // strGroupByText, strGroupBy, strAllaccounts,
    // strcelementvaluefrom, strcelementvalueto, Utility
    // .getContext(this, vars, "#AccessibleOrgTree",
    // "ReporteLibroMayorAnalitico"), Utility
    // .getContext(this, vars, "#User_Client",
    // "ReporteLibroMayorAnalitico"),
    // strcAcctSchemaId, strDateFrom, toDatePlusOne, strOrgFamily,
    // strcBpartnerId, strmProductId, strcProjectId, strAmtFrom,
    // strAmtTo, null, null, null, null, null, null);
    //
    // if (!data.hasData()) {
    // advisePopUp(
    // request,
    // response,
    // "WARNING",
    // Utility.messageBD(this, "ProcessStatus-W",
    // vars.getLanguage()),
    // Utility.messageBD(this, "NoDataFound",
    // vars.getLanguage()));
    // return;
    // }
    //
    // // augment data with totals
    // AddTotals dataWithTotals = new AddTotals(data, strGroupByText,
    // strcBpartnerId, strmProductId, strcProjectId,
    // strcAcctSchemaId, strDateFrom, strOrgFamily, this);
    //
    // String strReportName =
    // "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteLibroMayorAnalitico.jrxml";
    // response.setHeader("Content-disposition",
    // "inline; filename=ReporteLibroMayorAnaliticoPDF.pdf");
    //
    // HashMap<String, Object> parameters = new HashMap<String, Object>();
    //
    // String strLanguage = vars.getLanguage();
    //
    // parameters.put("ShowGrouping", new Boolean(!strGroupBy.equals("")));
    // StringBuilder strSubTitle = new StringBuilder();
    // strSubTitle.append(Utility.messageBD(this, "DateFrom", strLanguage)
    // + ": " + strDateFrom + " - "
    // + Utility.messageBD(this, "DateTo", strLanguage) + ": "
    // + strDateTo + " (");
    // strSubTitle.append(ReporteLibroMayorAnaliticoData.selectCompany(
    // this, vars.getClient()) + " - ");
    // strSubTitle.append(ReporteLibroMayorAnaliticoData
    // .selectOrganization(this, strOrg) + ")");
    // parameters.put("REPORT_SUBTITLE", strSubTitle.toString());
    // parameters.put("Previous",
    // Utility.messageBD(this, "Initial Balance", strLanguage));
    // parameters.put("Total",
    // Utility.messageBD(this, "Total", strLanguage));
    // parameters.put("PageNo", strPageNo);
    // String strDateFormat;
    // strDateFormat = vars.getJavaDateFormat();
    // parameters.put("strDateFormat", strDateFormat);
    // parameters.put("dateFrom", StringToDate(strDateFrom));
    // parameters.put("dateTo", StringToDate(strDateTo));
    //
    // renderJR(vars, response, strReportName, null, "pdf", parameters,
    // dataWithTotals, null);
    // } finally {
    // if (data != null) {
    // data.close();
    // }
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

  private void printPageDataPDF_ugo(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String strAmtFrom,
      String strAmtTo, String strcelementvaluefrom, String strcelementvalueto, String strOrg,
      String strcBpartnerId, String strmProductId, String strcProjectId, String strGroupBy,
      String strcAcctSchemaId, String strPageNo, String strSoloPendiente, String strructercero,
      String strcdesde, String strchasta, String strGenerarExcelConFormato, String strcprojectid,
      String strNroDocumento) throws IOException, ServletException {
    log4j.debug("Output: PDF");

    ReporteLibroMayorAnaliticoData[] data = null;

    ArrayList<ReporteLibroMayorAnaliticoData> listData = new ArrayList<ReporteLibroMayorAnaliticoData>();
    ArrayList<ReporteLibroMayorAnaliticoData> listDataSoloPendiente = new ArrayList<ReporteLibroMayorAnaliticoData>();
    ArrayList<ReporteLibroMayorAnaliticoData> listDataTodos = new ArrayList<ReporteLibroMayorAnaliticoData>();

    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    String strOrgFamily = getFamily(strTreeOrg, strOrg);

    if (strructercero.compareToIgnoreCase("") != 0) {
      BusinessPartner cbp = OBDal.getInstance().get(BusinessPartner.class, strructercero);
      strructercero = cbp.getTaxID();
    }

    String anio = strDateFrom.split("-")[2];
    String strDateAnioIni = "01-01-" + anio;

    String ultimoDiaAnio = "31-12-" + anio;
    String tipoPeriodo = "S";

    if (ultimoDiaAnio.equals(strDateFrom) && strDateFrom.equals(strDateTo)) {
      tipoPeriodo = "A";
    }

    if (strSoloPendiente.equals("Y")) {
      data = ReporteLibroMayorAnaliticoData.selectmioMejorado(this, tipoPeriodo, strcprojectid,
          Utility.getContext(this, vars, "#User_Client", "ReporteLibroMayorAnalitico"),
          strOrgFamily, strDateAnioIni
          /* null */, DateTimeData.nDaysAfter(this, strDateTo, "1"), strructercero, strNroDocumento, strcdesde, strchasta);//
    } else {
      data = ReporteLibroMayorAnaliticoData.selectmioTodosMejorado(this, tipoPeriodo, strcprojectid,
          Utility.getContext(this, vars, "#User_Client", "ReporteLibroMayorAnalitico"),
          strOrgFamily, strDateAnioIni/* null */, DateTimeData.nDaysAfter(this, strDateTo, "1"),
          strructercero, strNroDocumento, strcdesde, strchasta);//

    }

    if (data.length == 0) {
      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
      return;
    }

    String previousCodCuenta = data[0].codCuenta;
    String previousTercero = data[0].tercero;
    String previousNumeroDoc = data[0].numeroDoc;

    for (int kkk = 1; kkk < data.length; kkk++) {

      String currCodCuenta = data[kkk].codCuenta;
      String currTercero = data[kkk].tercero;
      String currNumeroDoc = data[kkk].numeroDoc;

      if (previousCodCuenta.equals(currCodCuenta) && previousTercero.equals(currTercero)
          && previousNumeroDoc.equals(currNumeroDoc)) {
        data[kkk].saldo = (new BigDecimal(data[kkk].saldo).add(new BigDecimal(data[kkk - 1].saldo)))
            .setScale(2).toString();
      }

      previousCodCuenta = currCodCuenta;
      previousTercero = currTercero;
      previousNumeroDoc = currNumeroDoc;

    }

    // PARA AGRUPAR EL HISTORIAL DE LINEAS EN UNA SOLA :

    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
    Date fechaDoc = new Date();
    Date fechaTope = new Date();
    try {
      fechaTope = formatter.parse(strDateFrom);
    } catch (ParseException e) {
      e.printStackTrace();
    }

    BigDecimal montoDolares = BigDecimal.ZERO;
    BigDecimal montoDebe = BigDecimal.ZERO;
    BigDecimal montoHaber = BigDecimal.ZERO;
    BigDecimal montoSaldo = BigDecimal.ZERO;
    Integer contador = 0;

    for (int i = 0; i < data.length; i++) {
      ReporteLibroMayorAnaliticoData actual = data[i];
      ReporteLibroMayorAnaliticoData siquiente = i + 1 == data.length ? data[i] : data[i + 1];

      try {
        fechaDoc = formatter.parse(siquiente.fecha);
      } catch (ParseException e) {
        e.printStackTrace();
      }

      if (new BigDecimal(actual.haber).compareTo(BigDecimal.ZERO) > 0)
        montoDolares = montoDolares.subtract(new BigDecimal(actual.importeDol));
      else
        montoDolares = montoDolares.add(new BigDecimal(actual.importeDol));

      montoDebe = new BigDecimal(actual.debe).add(montoDebe);
      montoHaber = new BigDecimal(actual.haber).add(montoHaber);
      montoSaldo = montoDebe.subtract(montoHaber);

      if (actual.codCuenta.compareTo(siquiente.codCuenta) == 0
          && actual.ruc.compareTo(siquiente.ruc) == 0
          && actual.numeroDoc.compareTo(siquiente.numeroDoc) == 0 && fechaDoc.before(fechaTope)) {
        contador++;
        if (i != data.length - 1)
          continue;
      }

      if (contador > 0) {
        if (montoSaldo.compareTo(BigDecimal.ZERO) != 0) {

          // montoDebe=montoDebe.subtract(montoHaber).compareTo(BigDecimal.ZERO)>0?montoDebe.subtract(montoHaber):BigDecimal.ZERO;
          // montoHaber=montoHaber.subtract(montoDebe).compareTo(BigDecimal.ZERO)>0?montoHaber.subtract(montoDebe):BigDecimal.ZERO;

          montoDebe = montoSaldo.compareTo(BigDecimal.ZERO) > 0 ? montoSaldo : BigDecimal.ZERO;
          montoHaber = montoSaldo.compareTo(BigDecimal.ZERO) < 0 ? montoSaldo.abs()
              : BigDecimal.ZERO;

          ReporteLibroMayorAnaliticoData nuevo = new ReporteLibroMayorAnaliticoData();
          nuevo.ap = null;
          nuevo.nAsiento = null;
          nuevo.fecha = null;
          nuevo.tipoDoc = actual.tipoDoc;
          nuevo.numeroDoc = actual.numeroDoc;
          nuevo.fechaDoc = actual.fechaDoc;
          nuevo.fechaVto = actual.fechaVto;
          nuevo.codCuenta = actual.codCuenta;
          nuevo.cuenta = actual.cuenta;
          nuevo.ruc = actual.ruc;
          nuevo.tercero = actual.tercero;
          nuevo.codTercero = actual.codTercero;
          nuevo.importeDol = montoDolares.toString();
          nuevo.debe = montoDebe.toString();
          nuevo.haber = montoHaber.toString();
          // nuevo.saldo = montoSaldo.toString();
          // nuevo.saldo = null;

          if (actual.codCuenta.compareTo(siquiente.codCuenta) == 0
              && actual.ruc.compareTo(siquiente.ruc) == 0
              && actual.numeroDoc.compareTo(siquiente.numeroDoc) == 0 && i != data.length - 1) {
            nuevo.saldo = null;
          } else {
            nuevo.saldo = montoSaldo.toString();
          }

          listData.add(nuevo);
        }

      } else {

        if (actual.codCuenta.compareTo(siquiente.codCuenta) == 0
            && actual.ruc.compareTo(siquiente.ruc) == 0
            && actual.numeroDoc.compareTo(siquiente.numeroDoc) == 0 && i != data.length - 1) {
          actual.saldo = null;
        }

        listData.add(actual);
      }
      montoDolares = BigDecimal.ZERO;
      montoDebe = BigDecimal.ZERO;
      montoHaber = BigDecimal.ZERO;
      montoSaldo = BigDecimal.ZERO;
      contador = 0;
    }

    // para eliminar las que no son pendientes que se colaron de la
    // consulta.

    ReporteLibroMayorAnaliticoData[] dataFinal;

    int ini = -1;
    int fin = 0;
    BigDecimal sumaDebe = BigDecimal.ZERO;
    BigDecimal sumaHaber = BigDecimal.ZERO;

    if (strSoloPendiente.equals("Y")) {
      for (int i = 0; i < listData.size(); i++) {

        ReporteLibroMayorAnaliticoData actual = listData.get(i);
        ReporteLibroMayorAnaliticoData siguiente = i + 1 == listData.size() ? actual
            : listData.get(i + 1);

        sumaDebe = sumaDebe.add(new BigDecimal(actual.debe));

        sumaHaber = sumaHaber.add(new BigDecimal(actual.haber));

        if (!actual.codCuenta.equals(siguiente.codCuenta) || !actual.ruc.equals(siguiente.ruc)
            || !actual.numeroDoc.equals(siguiente.numeroDoc) || i + 1 == listData.size()) {// variar

          if (sumaDebe.subtract(sumaHaber).compareTo(BigDecimal.ZERO) != 0) {
            for (int k = ini + 1; k <= i; k++) {
              listDataSoloPendiente.add(listData.get(k));
            }
          }

          ini = i;
          sumaDebe = BigDecimal.ZERO;
          sumaHaber = BigDecimal.ZERO;

        } else {
          continue;
        }
      }

      dataFinal = new ReporteLibroMayorAnaliticoData[listDataSoloPendiente.size()];

      for (int k = 0; k < listDataSoloPendiente.size(); k++) {
        dataFinal[k] = listDataSoloPendiente.get(k);
        if (dataFinal[k].numeroDoc.equals("????")) {
          dataFinal[k].fechaVto = "";
          dataFinal[k].numeroDoc = "";
        }
      }

    } else {
      int withRecord = 0;
      for (int i = 0; i < listData.size(); i++) {

        ReporteLibroMayorAnaliticoData actual = listData.get(i);
        ReporteLibroMayorAnaliticoData siguiente = i + 1 == listData.size() ? actual
            : listData.get(i + 1);

        Date dateacct = null;
        if (actual.fecha != null) {
          try {
            dateacct = formatter.parse(actual.fecha);
          } catch (Exception e) {
            e.printStackTrace();
          }
        } else {
          // this is a saldo record set dateacct to the day before datefrom
          Calendar cal = Calendar.getInstance();
          cal.setTime(fechaTope);
          cal.add(Calendar.DAY_OF_YEAR, -1);
          dateacct = cal.getTime();
        }

        if (dateacct.compareTo(fechaTope) < 0) {
          sumaDebe = sumaDebe.add(new BigDecimal(actual.debe));
          sumaHaber = sumaHaber.add(new BigDecimal(actual.haber));
        } else {
          withRecord = 1;
        }

        if (!actual.codCuenta.equals(siguiente.codCuenta) || !actual.ruc.equals(siguiente.ruc)
            || !actual.numeroDoc.equals(siguiente.numeroDoc) || i + 1 == listData.size()) {// variar

          if (sumaDebe.subtract(sumaHaber).compareTo(BigDecimal.ZERO) != 0 || withRecord == 1) {
            for (int k = ini + 1; k <= i; k++) {
              listDataTodos.add(listData.get(k));
            }
          }

          ini = i;
          sumaDebe = BigDecimal.ZERO;
          sumaHaber = BigDecimal.ZERO;
          withRecord = 0;

        } else {
          continue;
        }
      }
      dataFinal = new ReporteLibroMayorAnaliticoData[listDataTodos.size()];

      for (int k = 0; k < listDataTodos.size(); k++) {
        dataFinal[k] = listDataTodos.get(k);

        if (dataFinal[k].numeroDoc.equals("????")) {
          dataFinal[k].fechaVto = "";
          dataFinal[k].numeroDoc = "";
        }
      }
    }

    String strOutput;
    String strReportName;
    if (vars.commandIn("PDF")) {
      strOutput = "pdf";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteLibroMayorAnalitico.jrxml";
    } else {
      strOutput = "xls";

      if (strGenerarExcelConFormato.equals("Y")) {
        strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteLibroMayorAnaliticoExcel.jrxml";

      } else {
        strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteLibroMayorAnaliticoExcelPlano.jrxml";
      }
    }

    HashMap<String, Object> parameters = new HashMap<String, Object>();

    String organizacion = ReporteLibroMayorAnaliticoData.selectSocialName(this, strOrg);
    String rucOrganizacion = ReporteLibroMayorAnaliticoData.selectRucOrg(this, strOrg);

    parameters.put("Razon",
        organizacion.compareToIgnoreCase("") != 0 ? organizacion : "TODAS LAS ORGANIZACIONES");
    parameters.put("Ruc", rucOrganizacion.compareToIgnoreCase("0") != 0 ? rucOrganizacion : "-");

    parameters.put("dateFrom", StringToDate(strDateFrom));
    parameters.put("dateTo", StringToDate(strDateTo));
    renderJR(vars, response, strReportName, "Libro_Mayor_Analitico", strOutput, parameters,
        dataFinal, null);

  }

  private String getFamily(String strTree, String strChild) throws IOException, ServletException {
    return Tree.getMembers(this, strTree, strChild);
  }

  @Override
  public String getServletInfo() {
    return "Servlet ReporteLibroMayorAnalitico. This Servlet was made by Pablo Sarobe";
  } // end of getServletInfo() method
}
