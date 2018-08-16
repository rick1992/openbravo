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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.data.ScrollableFieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.businessUtility.Tree;
import org.openbravo.erpCommon.businessUtility.TreeData;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.info.SelectorUtilityData;
import org.openbravo.erpCommon.utility.AbstractScrollableFieldProviderFilter;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.LimitRowsScrollableFieldProviderFilter;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SQLReturnObject;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.accounting.StructPle;
import pe.com.unifiedgo.accounting.SunatUtil;

public class ReportGeneralLedger extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strcAcctSchemaId = vars.getGlobalVariable("inpcAcctSchemaId",
          "ReportGeneralLedger|cAcctSchemaId", "");
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReportGeneralLedger|DateFrom",
          "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportGeneralLedger|DateTo", "");
      String strPageNo = vars.getGlobalVariable("inpPageNo", "ReportGeneralLedger|PageNo", "1");
      String strAmtFrom = vars.getNumericGlobalVariable("inpAmtFrom", "ReportGeneralLedger|AmtFrom",
          "");
      String strAmtTo = vars.getNumericGlobalVariable("inpAmtTo", "ReportGeneralLedger|AmtTo", "");
      String strcelementvaluefrom = vars.getGlobalVariable("inpcElementValueIdFrom",
          "ReportGeneralLedger|C_ElementValue_IDFROM", "");
      String strcelementvalueto = vars.getGlobalVariable("inpcElementValueIdTo",
          "ReportGeneralLedger|C_ElementValue_IDTO", "");
      String strcelementvaluefromdes = "", strcelementvaluetodes = "";
      if (!strcelementvaluefrom.equals(""))
        strcelementvaluefromdes = ReportGeneralLedgerData.selectSubaccountDescription(this,
            strcelementvaluefrom);
      if (!strcelementvalueto.equals(""))
        strcelementvaluetodes = ReportGeneralLedgerData.selectSubaccountDescription(this,
            strcelementvalueto);
      strcelementvaluefromdes = (strcelementvaluefromdes.equals("null")) ? ""
          : strcelementvaluefromdes;
      strcelementvaluetodes = (strcelementvaluetodes.equals("null")) ? "" : strcelementvaluetodes;
      vars.setSessionValue("inpElementValueIdFrom_DES", strcelementvaluefromdes);
      vars.setSessionValue("inpElementValueIdTo_DES", strcelementvaluetodes);
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportGeneralLedger|Org", "0");
      String strcBpartnerId = vars.getInGlobalVariable("inpcBPartnerId_IN",
          "ReportGeneralLedger|cBpartnerId", "", IsIDFilter.instance);
      String strmProductId = vars.getInGlobalVariable("inpmProductId_IN",
          "ReportGeneralLedger|mProductId", "", IsIDFilter.instance);
      String strcProjectId = vars.getInGlobalVariable("inpcProjectId_IN",
          "ReportGeneralLedger|cProjectId", "", IsIDFilter.instance);
      String strDescargarLibroElectronico = vars.getGlobalVariable("inpDescargarLibroElectronico",
          "ReportGeneralLedger|DescargarLibroElectronico", "");

      String strGroupBy = "";
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strPageNo, strAmtFrom, strAmtTo,
          strcelementvaluefrom, strcelementvalueto, strOrg, strcBpartnerId, strmProductId,
          strcProjectId, strGroupBy, strcAcctSchemaId, strcelementvaluefromdes,
          strcelementvaluetodes, strDescargarLibroElectronico);
    } else if (vars.commandIn("PREVIOUS_RELATION")) {
      String strInitRecord = vars.getSessionValue("ReportGeneralLedger.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRange", "ReportGeneralLedger");
      int intRecordRange = strRecordRange.equals("") ? 0 : Integer.parseInt(strRecordRange);
      if (strInitRecord.equals("") || strInitRecord.equals("0"))
        vars.setSessionValue("ReportGeneralLedger.initRecordNumber", "0");
      else {
        int initRecord = (strInitRecord.equals("") ? 0 : Integer.parseInt(strInitRecord));
        initRecord -= intRecordRange;
        strInitRecord = ((initRecord < 0) ? "0" : Integer.toString(initRecord));
        vars.setSessionValue("ReportGeneralLedger.initRecordNumber", strInitRecord);
      }
      response.sendRedirect(getDireccion() + request.getServletPath());
    } else if (vars.commandIn("NEXT_RELATION")) {
      String strInitRecord = vars.getSessionValue("ReportGeneralLedger.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRange", "ReportGeneralLedger");
      int intRecordRange = strRecordRange.equals("") ? 0 : Integer.parseInt(strRecordRange);
      int initRecord = (strInitRecord.equals("") ? 0 : Integer.parseInt(strInitRecord));
      // if (initRecord == 0)
      // initRecord = 1; Removed by DAL 30/4/09
      initRecord += intRecordRange;
      strInitRecord = ((initRecord < 0) ? "0" : Integer.toString(initRecord));
      vars.setSessionValue("ReportGeneralLedger.initRecordNumber", strInitRecord);
      response.sendRedirect(getDireccion() + request.getServletPath());
    } else if (vars.commandIn("PDF", "XLS")) {
      String strcAcctSchemaId = vars.getRequestGlobalVariable("inpcAcctSchemaId",
          "ReportGeneralLedger|cAcctSchemaId");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportGeneralLedger|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportGeneralLedger|DateTo");
      String strAmtFrom = vars.getNumericParameter("inpAmtFrom");
      vars.setSessionValue("ReportGeneralLedger|AmtFrom", strAmtFrom);
      String strAmtTo = vars.getNumericParameter("inpAmtTo");
      vars.setSessionValue("ReportGeneralLedger|AmtTo", strAmtTo);
      String strcelementvaluefrom = vars.getRequestGlobalVariable("inpcElementValueIdFrom",
          "ReportGeneralLedger|C_ElementValue_IDFROM");
      String strcelementvalueto = vars.getRequestGlobalVariable("inpcElementValueIdTo",
          "ReportGeneralLedger|C_ElementValue_IDTO");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportGeneralLedger|Org", "0");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN",
          "ReportGeneralLedger|cBpartnerId", IsIDFilter.instance);
      String strmProductId = vars.getInGlobalVariable("inpmProductId_IN",
          "ReportGeneralLedger|mProductId", "", IsIDFilter.instance);
      String strcProjectId = vars.getInGlobalVariable("inpcProjectId_IN",
          "ReportGeneralLedger|cProjectId", "", IsIDFilter.instance);
      String strDescargarLibroElectronico = vars.getStringParameter("inpDescargarLibroElectronico");
      String strMostrarSaldos = vars.getStringParameter("inpMostrarSaldos");

      String strPageNo = vars.getGlobalVariable("inpPageNo", "ReportGeneralLedger|PageNo", "1");

      String strGroupBy = vars.getRequestGlobalVariable("inpGroupBy",
          "ReportGeneralLedger|GroupBy");

      String strShowAll = vars.getStringParameter("inpShowAll");

      printPageDataPDF_ugo(request, response, vars, strDateFrom, strDateTo, strAmtFrom, strAmtTo,
          strcelementvaluefrom, strcelementvalueto, strOrg, strcBpartnerId, strmProductId,
          strcProjectId, strGroupBy, strcAcctSchemaId, strPageNo, strDescargarLibroElectronico,
          strMostrarSaldos, strShowAll);

      // if (vars.commandIn("PDF"))
      // printPageDataPDF_ugo(request, response, vars, strDateFrom, strDateTo, strAmtFrom, strAmtTo,
      // strcelementvaluefrom, strcelementvalueto, strOrg, strcBpartnerId, strmProductId,
      // strcProjectId, strGroupBy, strcAcctSchemaId, strPageNo, strDescargarLibroElectronico);
      // else
      // printPageDataXLS(request, response, vars, strDateFrom, strDateTo, strAmtFrom, strAmtTo,
      // strcelementvaluefrom, strcelementvalueto, strOrg, strcBpartnerId, strmProductId,
      // strcProjectId, strGroupBy, strcAcctSchemaId, strDescargarLibroElectronico);
    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strPageNo, String strAmtFrom, String strAmtTo,
      String strcelementvaluefrom, String strcelementvalueto, String strOrg, String strcBpartnerId,
      String strmProductId, String strcProjectId, String strGroupBy, String strcAcctSchemaId,
      String strcelementvaluefromdes, String strcelementvaluetodes,
      String strDescargarLibroElectronico) throws IOException, ServletException {

    String strRecordRange = Utility.getContext(this, vars, "#RecordRange", "ReportGeneralLedger");
    int intRecordRange = (strRecordRange.equals("") ? 0 : Integer.parseInt(strRecordRange));
    String strInitRecord = vars.getSessionValue("ReportGeneralLedger.initRecordNumber");
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
    ReportGeneralLedgerData[] data = null;
    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    String strOrgFamily = getFamily(this, strTreeOrg, strOrg);
    String toDatePlusOne = DateTimeData.nDaysAfter(this, strDateTo, "1");

    String strGroupByText = (strGroupBy.equals("BPartner")
        ? Utility.messageBD(this, "BusPartner", vars.getLanguage())
        : (strGroupBy.equals("Product") ? Utility.messageBD(this, "Product", vars.getLanguage())
            : (strGroupBy.equals("Project") ? Utility.messageBD(this, "Project", vars.getLanguage())
                : "")));

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportGeneralLedger", true, "", "",
        "imprimir();return false;", false, "ad_reports", strReplaceWith, false, true);

    toolbar.prepareSimpleToolBarTemplate();
    toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");

    String strcBpartnerIdAux = strcBpartnerId;
    String strmProductIdAux = strmProductId;
    String strcProjectIdAux = strcProjectId;
    if (strDateFrom.equals("") && strDateTo.equals("")) {
      String discard[] = { "sectionAmount", "sectionPartner" };
      xmlDocument = xmlEngine
          .readXmlTemplate("pe/com/unifiedgo/report/ad_reports/ReportGeneralLedger", discard)
          .createXmlDocument();
      // toolbar
      // .prepareRelationBarTemplate(false, false,
      // "submitCommandForm('XLS', false, frmMain, 'ReportGeneralLedgerExcel.xls', 'EXCEL');return
      // false;");
      data = ReportGeneralLedgerData.set();
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
          strcelementvaluetodes = ReportGeneralLedgerData.selectSubaccountDescription(this,
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
      // ReportGeneralLedgerData scroll = null;
      // try {
      // /*
      // * scroll = ReportGeneralLedgerData.select2ugo(this, rowNum, strGroupByText, strGroupBy,
      // * strAllaccounts, strcelementvaluefrom, strcelementvalueto, Utility .getContext(this, vars,
      // * "#AccessibleOrgTree", "ReportGeneralLedger"), Utility .getContext(this, vars,
      // * "#User_Client", "ReportGeneralLedger"), strcAcctSchemaId, strDateFrom, toDatePlusOne,
      // * strOrgFamily, strcBpartnerId, strmProductId, strcProjectId, strAmtFrom, strAmtTo, null,
      // * null, pgLimit, oraLimit1, oraLimit2, null);
      // */
      // Vector<ReportGeneralLedgerData> res = new Vector<ReportGeneralLedgerData>();
      // while (scroll.next()) {
      // res.add(scroll.get());
      // }
      // data = new ReportGeneralLedgerData[res.size()];
      // res.copyInto(data);
      // } finally {
      // if (scroll != null) {
      // scroll.close();
      // }
      // }

      log4j.debug("Select2. Time in mils: " + (System.currentTimeMillis() - initMainSelect));
      log4j.debug("RecordNo: " + initRecordNumber);

      ReportGeneralLedgerData[] dataTotal = null;
      ReportGeneralLedgerData[] dataSubtotal = null;
      String strOld = "";
      // boolean firstPagBlock = false;
      ReportGeneralLedgerData[] subreportElement = new ReportGeneralLedgerData[1];
      for (int i = 0; data != null && i < data.length; i++) {
        if (!strOld.equals(data[i].groupbyid + data[i].id)) {
          subreportElement = new ReportGeneralLedgerData[1];
          // firstPagBlock = false;
          if (i == 0 && initRecordNumber > 0) {
            // firstPagBlock = true;
            Long init = System.currentTimeMillis();
            dataTotal = ReportGeneralLedgerData.select2Total(this, rowNum, strGroupByText,
                strGroupBy, strAllaccounts, strcelementvaluefrom, strcelementvalueto,
                Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportGeneralLedger"),
                Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"),
                strcAcctSchemaId, "", DateTimeData.nDaysAfter(this, data[0].dateacct, "1"),
                strOrgFamily, strcBpartnerId, strmProductId, strcProjectId, strAmtFrom, strAmtTo,
                data[0].id, data[0].groupbyid, null, null, null, data[0].dateacctnumber
                    + data[0].factaccttype + data[0].factAcctGroupId + data[0].factAcctId);
            dataSubtotal = ReportGeneralLedgerData.select2sum(this, rowNum, strGroupByText,
                strGroupBy, strAllaccounts, strcelementvaluefrom, strcelementvalueto,
                Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportGeneralLedger"),
                Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"),
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
            subreportElement = new ReportGeneralLedgerData[1];
            subreportElement[0] = new ReportGeneralLedgerData();
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
              subreportElement = ReportGeneralLedgerData.selectTotal2(this, strcBpartnerId,
                  (strGroupBy.equals("BPartner") ? " " : null), strmProductId,
                  (strGroupBy.equals("Product") ? " " : null), strcProjectId,
                  (strGroupBy.equals("Project") ? " " : null), strcAcctSchemaId, data[i].id, "",
                  strDateFrom, strOrgFamily);
              log4j.debug("SelectTotalNew. Time in mils: " + (System.currentTimeMillis() - init));
            } else {
              Long init = System.currentTimeMillis();
              subreportElement = ReportGeneralLedgerData.selectTotal2(this,
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
      subreportElement = new ReportGeneralLedgerData[1];
      for (int i = 0; data != null && i < data.length; i++) {
        if (!strTotal.equals(data[i].groupbyid + data[i].id)) {
          subreportElement = new ReportGeneralLedgerData[1];
          if ("".equals(data[i].groupbyid)) {
            // The argument " " is used to simulate one value and
            // put the optional parameter--> AND
            // FACT_ACCT.C_PROJECT_ID IS NULL for example
            Long init = System.currentTimeMillis();
            subreportElement = ReportGeneralLedgerData.selectTotal2(this, strcBpartnerId,
                (strGroupBy.equals("BPartner") ? " " : null), strmProductId,
                (strGroupBy.equals("Product") ? " " : null), strcProjectId,
                (strGroupBy.equals("Project") ? " " : null), strcAcctSchemaId, data[i].id, "",
                toDatePlusOne, strOrgFamily);
            log4j.debug("SelectTotal2. Time in mils: " + (System.currentTimeMillis() - init));
          } else {
            Long init = System.currentTimeMillis();
            subreportElement = ReportGeneralLedgerData
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
      // "submitCommandForm('XLS', true, frmMain, 'ReportGeneralLedgerExcel.xls', 'EXCEL');return
      // false;");
      xmlDocument = xmlEngine
          .readXmlTemplate("pe/com/unifiedgo/report/ad_reports/ReportGeneralLedger", discard)
          .createXmlDocument();
    }
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "pe.com.unifiedgo.report.ad_reports.ReportGeneralLedger");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportGeneralLedger.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportGeneralLedger.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportGeneralLedger");
      vars.removeMessage("ReportGeneralLedger");
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
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportGeneralLedger"),
          Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"), '*');
      comboTableData.fillParameters(null, "ReportGeneralLedger", "");
      xmlDocument.setData("reportAD_ORGID", "liststructure", comboTableData.select(false));

      // //////////// para periodos

      ReportGeneralLedgerData[] datax = ReportGeneralLedgerData.select_periodos(this);
      FieldProvider librosMayor[] = new FieldProvider[datax.length];
      Vector<Object> vector = new Vector<Object>(0);

      for (int i = 0; i < datax.length; i++) {
        SQLReturnObject sqlReturnObject = new SQLReturnObject();
        sqlReturnObject.setData("ID", datax[i].idperiodo);
        sqlReturnObject.setData("NAME", datax[i].periodo);
        vector.add(sqlReturnObject);
      }
      vector.copyInto(librosMayor);

      xmlDocument.setData("reportC_ACCTSCHEMA_ID", "liststructure", librosMayor);

      // /////////////fin para vendodores

      // para cargar la variables javascrip de periodos
      xmlDocument.setParameter("paramPeriodosArray",
          Utility.arrayInfinitasEntradas("idperiodo;periodo;fechainicial;fechafinal;idorganizacion",
              "arrPeriodos", ReportGeneralLedgerData.select_periodos(this)));

      System.out.println(Utility.arrayInfinitasEntradas("idperiodo;periodo;fechainicial;fechafinal",
          "arrPeriodos", ReportGeneralLedgerData.select_periodos(this)));
      // FIN para cargar la variables javascrip de periodos

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

    // //////////// para COMBO LIBRO MAYOR

    ReportGeneralLedgerData[] datalibros = ReportGeneralLedgerData.select_libros(this);
    FieldProvider periodos[] = new FieldProvider[datalibros.length];
    Vector<Object> vector = new Vector<Object>(0);

    for (int i = 0; i < datalibros.length; i++) {
      SQLReturnObject sqlReturnObject = new SQLReturnObject();
      sqlReturnObject.setData("ID", datalibros[i].idlibro);
      sqlReturnObject.setData("NAME", datalibros[i].nombrelibro);
      vector.add(sqlReturnObject);
    }
    vector.copyInto(periodos);

    xmlDocument.setData("reportC_PERIODO", "liststructure", periodos);

    // /////////////fin COMBO LIBRO MAYOR

    // para
    xmlDocument.setParameter("paramLibrosArray",
        Utility.arrayInfinitasEntradas("idlibro;nombrelibro;idorganizacion", "arrLibroMayor",
            ReportGeneralLedgerData.select_libros(this)));

    // log4j.debug("data.length: " + data.length);

    if (data != null && data.length > 0) {
      if (strGroupBy.equals(""))
        xmlDocument.setData("structure1", data);
      else
        xmlDocument.setData("structure2", data);
    }

    out.println(xmlDocument.print());
    out.close();
  }

  /*
   * private void printPageDataPDF(HttpServletRequest request, HttpServletResponse response,
   * VariablesSecureApp vars, String strDateFrom, String strDateTo, String strAmtFrom, String
   * strAmtTo, String strcelementvaluefrom, String strcelementvalueto, String strOrg, String
   * strcBpartnerId, String strmProductId, String strcProjectId, String strGroupBy, String
   * strcAcctSchemaId, String strPageNo, String strDescargarLibroElectronico) throws IOException,
   * ServletException { log4j.debug("Output: PDF"); response.setContentType(
   * "text/html; charset=UTF-8"); ReportGeneralLedgerData[] subreport = null; String strTreeOrg =
   * TreeData.getTreeOrg(this, vars.getClient()); String strOrgFamily = getFamily(strTreeOrg,
   * strOrg); String toDatePlusOne = DateTimeData.nDaysAfter(this, strDateTo, "1");
   * 
   * String strGroupByText = (strGroupBy.equals("BPartner") ? Utility .messageBD(this, "BusPartner",
   * vars.getLanguage()) : (strGroupBy.equals("Product") ? Utility.messageBD(this, "Product",
   * vars.getLanguage()) : (strGroupBy .equals("Project") ? Utility.messageBD(this, "Project",
   * vars.getLanguage()) : ""))); String strAllaccounts = "Y";
   * 
   * if (strDateFrom.equals("") || strDateTo.equals("")) { advisePopUp( request, response,
   * "WARNING", Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
   * Utility.messageBD(this, "NoDataFound", vars.getLanguage())); return; }
   * 
   * if (strcelementvaluefrom != null && !strcelementvaluefrom.equals("")) { if
   * (strcelementvalueto.equals("")) strcelementvalueto = strcelementvaluefrom; strAllaccounts =
   * "N"; }
   * 
   * ReportGeneralLedgerData data = null; try { data = ReportGeneralLedgerData.select2(this, "0",
   * strGroupByText, strGroupBy, strAllaccounts, strcelementvaluefrom, strcelementvalueto,
   * Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportGeneralLedger"),
   * Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"), strcAcctSchemaId,
   * strDateFrom, toDatePlusOne, strOrgFamily, strcBpartnerId, strmProductId, strcProjectId,
   * strAmtFrom, strAmtTo, null, null, null, null, null, null);
   * 
   * if (!data.hasData()) { advisePopUp( request, response, "WARNING", Utility.messageBD(this,
   * "ProcessStatus-W", vars.getLanguage()), Utility.messageBD(this, "NoDataFound",
   * vars.getLanguage())); return; }
   * 
   * // augment data with totals AddTotals dataWithTotals = new AddTotals(data, strGroupByText,
   * strcBpartnerId, strmProductId, strcProjectId, strcAcctSchemaId, strDateFrom, strOrgFamily,
   * this);
   * 
   * String strReportName =
   * "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportGeneralLedger.jrxml";
   * response.setHeader("Content-disposition", "inline; filename=ReportGeneralLedgerPDF.pdf");
   * 
   * HashMap<String, Object> parameters = new HashMap<String, Object>();
   * 
   * String strLanguage = vars.getLanguage();
   * 
   * parameters.put("ShowGrouping", new Boolean(!strGroupBy.equals(""))); StringBuilder strSubTitle
   * = new StringBuilder(); strSubTitle.append(Utility.messageBD(this, "DateFrom", strLanguage) +
   * ": " + strDateFrom + " - " + Utility.messageBD(this, "DateTo", strLanguage) + ": " + strDateTo
   * + " ("); strSubTitle.append(ReportGeneralLedgerData.selectCompany(this, vars.getClient()) +
   * " - "); strSubTitle.append(ReportGeneralLedgerData.selectOrganization(this, strOrg) + ")");
   * parameters.put("REPORT_SUBTITLE", strSubTitle.toString()); parameters.put("Previous",
   * Utility.messageBD(this, "Initial Balance", strLanguage)); parameters.put("Total",
   * Utility.messageBD(this, "Total", strLanguage)); parameters.put("PageNo", strPageNo); String
   * strDateFormat; strDateFormat = vars.getJavaDateFormat(); parameters.put("strDateFormat",
   * strDateFormat); parameters.put("dateFrom", StringToDate(strDateFrom)); parameters.put("dateTo",
   * StringToDate(strDateTo));
   * 
   * renderJR(vars, response, strReportName, null, "pdf", parameters, dataWithTotals, null); }
   * finally { if (data != null) { data.close(); } } }
   */
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

  public static StructPle getPLE(ConnectionProvider conn, VariablesSecureApp vars,
      String adUserClient, String adUserOrg, String strDateFrom, String strDateTo,
      String strAmtFrom, String strAmtTo, String strcelementvaluefrom, String strcelementvalueto,
      String strOrg, String strcBpartnerId, String strmProductId, String strcProjectId,
      String strGroupBy, String strcAcctSchemaId, String strPageNo,
      String strDescargarLibroElectronico) throws Exception {
    ReportGeneralLedgerData[] data = getData(conn, vars, adUserClient, adUserOrg, strDateFrom,
        strDateTo, strAmtFrom, strAmtTo, strcelementvaluefrom, strcelementvalueto, strOrg,
        strcBpartnerId, strmProductId, strcProjectId, strGroupBy, strcAcctSchemaId, strPageNo);
    StructPle sunatPle = getStringData(conn, vars, data, strDateFrom, strDateTo, strAmtFrom,
        strAmtTo, strcelementvaluefrom, strcelementvalueto, strOrg, strcBpartnerId, strmProductId,
        strcProjectId, strGroupBy, strcAcctSchemaId, strPageNo, strDescargarLibroElectronico);

    return sunatPle;
  }

  private static ReportGeneralLedgerData[] getData(ConnectionProvider conn, VariablesSecureApp vars,
      String adUserClient, String adUserOrg, String strDateFrom, String strDateTo,
      String strAmtFrom, String strAmtTo, String strcelementvaluefrom, String strcelementvalueto,
      String strOrg, String strcBpartnerId, String strmProductId, String strcProjectId,
      String strGroupBy, String strcAcctSchemaId, String strPageNo)
      throws IOException, ServletException {
    String strTreeOrg = TreeData.getTreeOrg(conn, vars.getClient());
    String strOrgFamily = getFamily(conn, strTreeOrg, strOrg);
    String toDatePlusOne = DateTimeData.nDaysAfter(conn, strDateTo, "1");

    /****************************************************************/

    /*
     * ReportGeneralLedgerData data[] = ReportGeneralLedgerData.select2ugoforple(conn, adUserOrg,
     * adUserClient, strcAcctSchemaId, strDateFrom, strDateTo, strOrgFamily, strcBpartnerId,
     * strmProductId, strcProjectId, strAmtFrom, strAmtTo, null, null, null, null);
     */

    ReportGeneralLedgerData data[] = ReportGeneralLedgerData.select2ugoforpleNew(conn, adUserClient,
        strOrgFamily, strcAcctSchemaId, strDateFrom, strDateTo);

    return data;
  }

  private static StructPle getStringData(ConnectionProvider conn, VariablesSecureApp vars,
      ReportGeneralLedgerData[] data, String strDateFrom, String strDateTo, String strAmtFrom,
      String strAmtTo, String strcelementvaluefrom, String strcelementvalueto, String strOrg,
      String strcBpartnerId, String strmProductId, String strcProjectId, String strGroupBy,
      String strcAcctSchemaId, String strPageNo, String strDescargarLibroElectronico)
      throws ServletException, IOException {
    StructPle sunatPle = new StructPle();
    sunatPle.numEntries = 0;

    StringBuffer sb = new StringBuffer();

    // "Y" solo cuando es para el ple, para el libro electronico es "N"
    if (strDescargarLibroElectronico.equalsIgnoreCase("Y")) {
      String strTreeOrg = TreeData.getTreeOrg(conn, vars.getClient());
      String strOrgFamily = getFamily(conn, strTreeOrg, strOrg);
      data = agregaAsientosRegularizados(data, conn, strOrgFamily, "'" + vars.getClient() + "'",
          strDateFrom, strDateTo);
    }

    Organization org = OBDal.getInstance().get(Organization.class, strOrg);
    String rucAdq = SunatUtil.LPad(org.getOrganizationInformationList().get(0).getTaxID(), 11, '0');

    SimpleDateFormat formatterForm = new SimpleDateFormat("dd-MM-yyyy");
    Date dttFrom = null;
    try {
      dttFrom = formatterForm.parse(strDateFrom);
    } catch (Exception ex) {
      System.out.println("Exception: " + strDateFrom);
    }

    SimpleDateFormat dt = new SimpleDateFormat("yyyyMM");
    SimpleDateFormat dt2 = new SimpleDateFormat("dd/MM/yyyy");

    String filename = "LE" + rucAdq + dt.format(dttFrom) + "00060100001"; // LERRRRRRRRRRRAAAAMM0006010000OIM1.TXT

    HashMap<String, Integer> hMapRegnumber = new HashMap<String, Integer>();

    for (int i = 0; i < data.length; i++) {

      ReportGeneralLedgerData led = data[i];

      Date dttAcct = null;
      try {
        dttAcct = formatterForm.parse(led.dateacct);
      } catch (Exception ex) {
      }
      String periodoTrib = dt.format(dttAcct) + "00";

      String regnumber = led.nreg;

      String tipoAsiento = "M";
      if (led.factaccttype.equals("1"))
        tipoAsiento = "A";
      if (led.factaccttype.equals("5"))
        tipoAsiento = "C";

      if (hMapRegnumber.get(regnumber) != null) {
        Integer correlativo = hMapRegnumber.get(regnumber);
        tipoAsiento = tipoAsiento + SunatUtil.LPad(String.valueOf(correlativo), 5, '0');
        hMapRegnumber.put(regnumber, correlativo + 1);
      } else {
        Integer correlativo = new Integer(0);
        tipoAsiento = tipoAsiento + SunatUtil.LPad(String.valueOf(correlativo), 5, '0');
        hMapRegnumber.put(regnumber, correlativo + 1);
      }

      // prevRegNumber = regnumber;

      String plancuentas = "01";
      String cuentaContable = led.value;

      String cencos = led.cencos;
      String monedaOrigen = SunatUtil.LPad(led.currencycode, 3, '0');

      // sunat de mela
      if (monedaOrigen.equals("001"))
        monedaOrigen = "PEN";
      else if (monedaOrigen.equals("002"))
        monedaOrigen = "USD";
      else
        monedaOrigen = "EUR";

      String adicionalinfo = "";
      String tdbp = led.tdbp;
      String rucbp = led.rucbp;
      String tdcomp = led.tdcomp;
      String physicalDocument = led.physicaldocument;

      rucbp = rucbp.replace("-", "");
      if (rucbp.length() > 15) {
        adicionalinfo = adicionalinfo + rucbp;
        rucbp = rucbp.substring(0, 15);
      }

      String cinvoiceid = led.cInvoiceId;

      if (tdcomp.equals("50") || tdcomp.equals("52")) {// obtener dua si existiera
        Invoice inv = OBDal.getInstance().get(Invoice.class, cinvoiceid);
        if (inv.getScoDua() != null) {
          physicalDocument = inv.getScoDua().getSCODuanumber();
        } else {
          List<InvoiceLine> lsInvoicelines = inv.getInvoiceLineList();
          for (int jk = 0; jk < lsInvoicelines.size(); jk++) {
            if (lsInvoicelines.get(jk).getSimDua() != null) {
              physicalDocument = lsInvoicelines.get(jk).getSimDua().getSCODuanumber();
              break;
            }
          }
        }
      }

      String nroComprobante = physicalDocument;
      String nroSerie = "";
      if (physicalDocument.contains("-")) {
        StringTokenizer st = new StringTokenizer(physicalDocument, "-");
        nroSerie = st.nextToken();
        nroComprobante = st.nextToken();
        if (tdcomp.equals("50") || tdcomp.equals("52")) {
          try {
            nroComprobante = st.nextToken();
            nroComprobante = st.nextToken();
          } catch (Exception ex) {
          }
        }
      }

      Date dttVenc = null;
      Date dttTrx = null;
      try {
        dttTrx = formatterForm.parse(led.fecemi);
        dttVenc = formatterForm.parse(led.fecvenc);
      } catch (Exception ex) {
      }

      if (tdcomp != null && !tdcomp.equals("")) {
        int tdcompInt = Integer.parseInt(tdcomp);
        if (tdcompInt == 7 || tdcompInt == 8 || tdcompInt == 87 || tdcompInt == 88
            || tdcompInt == 97 || tdcompInt == 98)
          dttVenc = null;
      }

      if (tdcomp.equals("99")) {// otros
        tdcomp = "00";
        nroComprobante = "00000000";
        nroSerie = "";
        dttVenc = null;
        dttTrx = dttAcct;
      }

      if (nroSerie.equals("") && tdcomp.equals("12"))
        nroSerie = "-";

      if (!nroSerie.equals("") && !tdcomp.equals("50") && !tdcomp.equals("52")
          && !tdcomp.equals("55") && !tdcomp.equals("05"))
        nroSerie = SunatUtil.LPad(nroSerie, 4, '0');
      if (nroSerie.length() > 4 && nroSerie.charAt(0) == '0') {
        int len = nroSerie.length();
        nroSerie = nroSerie.substring(len - 4, len);
      }

      if (tdcomp.equals(""))
        tdcomp = "00";
      if (nroComprobante.equals(""))
        nroComprobante = "00000000";

      nroComprobante = nroComprobante.replace("/", "");
      nroComprobante = nroComprobante.replace("-", "");

      /* Recortar el numero de comprobante */
      if ((tdcomp.equalsIgnoreCase("01") || tdcomp.equalsIgnoreCase("03")
          || tdcomp.equalsIgnoreCase("04") || tdcomp.equalsIgnoreCase("06")
          || tdcomp.equalsIgnoreCase("07") || tdcomp.equalsIgnoreCase("08")
          || tdcomp.equalsIgnoreCase("09")) && nroComprobante.length() > 8) {
        nroComprobante = nroComprobante.substring(nroComprobante.length() - 8,
            nroComprobante.length());
      }

      if (tdcomp.equalsIgnoreCase("02") && nroComprobante.length() > 7)
        nroComprobante = nroComprobante.substring(nroComprobante.length() - 7,
            nroComprobante.length());

      if (tdcomp.equalsIgnoreCase("05") && nroComprobante.length() > 11)
        nroComprobante = nroComprobante.substring(nroComprobante.length() - 11,
            nroComprobante.length());

      /* Recortar la serie del documento */

      if (nroComprobante.length() > 15)
        nroComprobante = nroComprobante.substring(0, 15);

      String fechaOp = dt2.format(dttTrx);
      String fechaContable = dt2.format(dttAcct);
      String fechaVenc = "";
      if (dttVenc != null)
        fechaVenc = dt2.format(dttVenc);

      String glosa = led.description;
      if (glosa.length() > 100)
        glosa = glosa.substring(0, 100);

      glosa = glosa.replace("\n", "");
      glosa = glosa.replace("|", "");

      NumberFormat formatter = new DecimalFormat("#0.00");

      String debe = formatter.format((led.amtacctdr != null && !led.amtacctdr.equals(""))
          ? Double.parseDouble(led.amtacctdr) : 0.00);
      String haber = formatter.format((led.amtacctcr != null && !led.amtacctcr.equals(""))
          ? Double.parseDouble(led.amtacctcr) : 0.00);

      String estadoOp = led.operationStatus == null ? "1" : led.operationStatus;
      SimpleDateFormat formatterYYMM = new SimpleDateFormat("yyyy/MM");
      /*
       * try { Date dateAcctf = formatterYYMM.parse(dttAcct.getYear() + "/" + dttAcct.getMonth());
       * Date dateTrxf = formatterYYMM.parse(dttTrx.getYear() + "/" + dttTrx.getMonth()); if
       * (dateTrxf.compareTo(dateAcctf) < 0){ estadoOp = "8"; } } catch (Exception e) { }
       */

      tipoAsiento = led.correlativo;

      String linea = periodoTrib + "|" + regnumber + "|" + tipoAsiento + "|" + cuentaContable + "||"
          + cencos + "|" + monedaOrigen + "|" + tdbp + "|" + rucbp + "|" + tdcomp + "|" + nroSerie
          + "|" + nroComprobante + "|" + fechaContable + "|" + fechaVenc + "|" + fechaOp + "|"
          + glosa + "||" + debe + "|" + haber + "||" + estadoOp + "|" + adicionalinfo + "|";
      if (i > 0)
        sb.append("\n");
      sb.append(linea);
      sunatPle.numEntries++;
    }
    if (sunatPle.numEntries > 0) {
      filename = filename + "111.TXT";
    } else {
      filename = filename + "011.TXT";
    }

    sunatPle.filename = filename;
    sunatPle.data = sb.toString();
    return sunatPle;
  }

  private static ReportGeneralLedgerData[] agregaAsientosRegularizados(
      ReportGeneralLedgerData[] data, ConnectionProvider conn, String organizacion, String cliente,
      String strDateFrom, String strDateTo) throws ServletException {

    HashMap<String, ArrayList<ReportGeneralLedgerData>> grupoFactsRegularizados = new HashMap<>();
    HashMap<String, ArrayList<ReportGeneralLedgerData>> grupoFactsOriginales = new HashMap<>();

    ReportGeneralLedgerData[] dataFactsRegularizados = ReportGeneralLedgerData
        .selectFactsARegularizar(conn, cliente, organizacion, strDateFrom, strDateTo);
    ReportGeneralLedgerData[] dataFactsOriginales = ReportGeneralLedgerData
        .selectFactsOriginales(conn, cliente, organizacion, strDateFrom, strDateTo);

    ArrayList<ReportGeneralLedgerData> newData = new ArrayList<>(Arrays.asList(data));

    for (int i = 0; i < dataFactsRegularizados.length; i++) {
      String key = dataFactsRegularizados[i].id;
      ArrayList<ReportGeneralLedgerData> facts = new ArrayList<>();
      if (grupoFactsRegularizados.containsKey(key)) {
        facts = grupoFactsRegularizados.get(key);
      }
      facts.add(dataFactsRegularizados[i]);
      grupoFactsRegularizados.put(dataFactsRegularizados[i].id, facts);
    }

    for (int i = 0; i < dataFactsOriginales.length; i++) {
      String key = dataFactsOriginales[i].id;
      ArrayList<ReportGeneralLedgerData> facts = new ArrayList<>();
      if (grupoFactsOriginales.containsKey(key)) {
        facts = grupoFactsOriginales.get(key);
      }
      facts.add(dataFactsOriginales[i]);
      grupoFactsOriginales.put(dataFactsOriginales[i].id, facts);
    }

    ArrayList<String> listRecordIds = new ArrayList<String>(grupoFactsOriginales.keySet());
    for (int i = 0; i < listRecordIds.size(); i++) {
      String key = listRecordIds.get(i);
      newData.addAll(procesaFactsRegularizados(grupoFactsRegularizados.get(key),
          grupoFactsOriginales.get(key)));
    }

    return newData.toArray(new ReportGeneralLedgerData[newData.size()]);
  }

  private static ArrayList<ReportGeneralLedgerData> procesaFactsRegularizados(
      ArrayList<ReportGeneralLedgerData> fRegularizados,
      ArrayList<ReportGeneralLedgerData> fOriginales) {

    HashMap<String, ReportGeneralLedgerData> grupoFactsPK = new HashMap<>();

    if (fRegularizados != null) {
      Iterator<ReportGeneralLedgerData> itr = fRegularizados.iterator();
      while (itr.hasNext()) {
        ReportGeneralLedgerData fr = itr.next();
        String key = fr.dateacct + "||" + fr.nreg + "||" + fr.correlativo + "||" + fr.value + "||"
            + fr.physicaldocument;
        fr.operationStatus = "8";
        grupoFactsPK.put(key, fr);
      }
    }

    if (fOriginales != null) {
      Iterator<ReportGeneralLedgerData> itr = fOriginales.iterator();
      while (itr.hasNext()) {
        ReportGeneralLedgerData fo = itr.next();
        String key = fo.dateacct + "||" + fo.nreg + "||" + fo.correlativo + "||" + fo.value + "||"
            + fo.physicaldocument;
        if (grupoFactsPK.containsKey(key)) {
          grupoFactsPK.get(key).operationStatus = "9";
        } else {
          fo.operationStatus = "9";
          fo.amtacctdr = "0.00";
          fo.amtacctcr = "0.00";
          grupoFactsPK.put(key, fo);
        }
      }
    }
    return new ArrayList<ReportGeneralLedgerData>(grupoFactsPK.values());
  }

  private void printPageDataPDF_ugo(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String strAmtFrom,
      String strAmtTo, String strcelementvaluefrom, String strcelementvalueto, String strOrg,
      String strcBpartnerId, String strmProductId, String strcProjectId, String strGroupBy,
      String strcAcctSchemaId, String strPageNo, String strDescargarLibroElectronico,
      String strMostrarSaldos, String strShowAll) throws IOException, ServletException {
    log4j.debug("Output: PDF");
    boolean isLE = false;
    if (strDescargarLibroElectronico.equals("Y")) {
      isLE = true;
    }

    if (isLE) {

      // Libro electronico
      // Libro electronico
      response.setContentType("text/html; charset=UTF-8");

      if (strDateFrom.equals("") || strDateTo.equals("")) {
        advisePopUp(request, response, "WARNING",
            Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
            Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
        return;
      }

      ReportGeneralLedgerData[] data = getData(this, vars,
          Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"),
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportGeneralLedger"), strDateFrom,
          strDateTo, strAmtFrom, strAmtTo, strcelementvaluefrom, strcelementvalueto, strOrg,
          strcBpartnerId, strmProductId, strcProjectId, strGroupBy, strcAcctSchemaId, strPageNo);
      StructPle sunatPle = getStringData(this, vars, data, strDateFrom, strDateTo, strAmtFrom,
          strAmtTo, strcelementvaluefrom, strcelementvalueto, strOrg, strcBpartnerId, strmProductId,
          strcProjectId, strGroupBy, strcAcctSchemaId, strPageNo, "N");

      response.setContentType("text/plain");
      response.setHeader("Content-Disposition", "attachment;filename=" + sunatPle.filename);

      ServletOutputStream os = response.getOutputStream();

      os.write(sunatPle.data.getBytes());

      os.flush();
      os.close();

    } else {/* PDF */

      response.setContentType("text/html; charset=UTF-8");

      String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
      String strOrgFamily = getFamily(this, strTreeOrg, strOrg);
      String toDatePlusOne = DateTimeData.nDaysAfter(this, strDateTo, "1");

      /****************************************************************/
      String anio = strDateFrom.substring(strDateFrom.length() - 4, strDateFrom.length());
      String ultimoDiaAnio = "31-12-" + anio;
      String fechaInicioAnnio = "01-01-" + anio;

      /****************************************************************/

      String strGroupByText = (strGroupBy
          .equals(
              "BPartner")
                  ? Utility.messageBD(this, "BusPartner", vars.getLanguage())
                  : (strGroupBy
                      .equals("Product")
                          ? Utility.messageBD(this, "Product", vars.getLanguage())
                          : (strGroupBy.equals("Project")
                              ? Utility.messageBD(this, "Project", vars.getLanguage())
                              : (strGroupBy.equals("Costcenter")
                                  ? Utility.messageBD(this, "Cost Center", vars.getLanguage())
                                  : ""))));
      String strAllaccounts = "Y";

      if (strDateFrom.equals("") || strDateTo.equals("")) {
        advisePopUp(request, response, "WARNING",
            Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
            Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
        return;
      }

      if (strcelementvaluefrom != null && !strcelementvaluefrom.equals("")) {
        if (strcelementvalueto.equals(""))
          strcelementvalueto = strcelementvaluefrom;
        strAllaccounts = "N";
      }

      ReportGeneralLedgerData data[] = null;

      ReportGeneralLedgerData dataBalance[] = null;

      String periodo_ajuste = "";

      if (ultimoDiaAnio.equals(strDateFrom) && strDateFrom.equals(strDateTo)) {

        if (strShowAll.compareToIgnoreCase("Y") == 0) {
          data = ReportGeneralLedgerData.selectMovimientosAjusteAll(this, strOrgFamily,
              Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"),
              strcAcctSchemaId, ultimoDiaAnio, strcelementvaluefrom, strcelementvalueto);
        } else {
          data = ReportGeneralLedgerData.selectMovimientosAjuste(this, strOrgFamily,
              Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"),
              strcAcctSchemaId, ultimoDiaAnio, strcelementvaluefrom, strcelementvalueto);
        }

        dataBalance = ReportGeneralLedgerData.selectSaldoAnteriorAjuste(this, strOrgFamily,
            Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"), fechaInicioAnnio,
            ultimoDiaAnio, strcelementvaluefrom, strcelementvalueto);

        periodo_ajuste = "Ajuste - " + anio;
      } else {

        if (strShowAll.compareToIgnoreCase("Y") == 0) {
          data = ReportGeneralLedgerData.select2ugoAll(this, "0", strGroupByText, strGroupBy,
              strAllaccounts, strcelementvaluefrom, strcelementvalueto,
              Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportGeneralLedger"),
              Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"),
              strcAcctSchemaId, strDateFrom, strDateTo, strOrgFamily, strcBpartnerId, strmProductId,
              strcProjectId, strAmtFrom, strAmtTo, null, null, null, null, null);
        } else {
          data = ReportGeneralLedgerData.select2ugo(this, "0", strGroupByText, strGroupBy,
              strAllaccounts, strcelementvaluefrom, strcelementvalueto,
              Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportGeneralLedger"),
              Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"),
              strcAcctSchemaId, strDateFrom, strDateTo, strOrgFamily, strcBpartnerId, strmProductId,
              strcProjectId, strAmtFrom, strAmtTo, null, null, null, null, null, null);
        }

        dataBalance = ReportGeneralLedgerData.selectSaldoAnterior(this, strOrgFamily,
            Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"), fechaInicioAnnio,
            strDateFrom, strcelementvaluefrom, strcelementvalueto);

      }

      Vector<ReportGeneralLedgerData> vectordata = new Vector<ReportGeneralLedgerData>(
          Arrays.asList(data));

      HashMap<String, Properties> hashmapBalances = new HashMap<String, Properties>();

      BigDecimal montoInicial = BigDecimal.ZERO;
      BigDecimal montoFinal = BigDecimal.ZERO;

      for (int i = 0; i < dataBalance.length; i++) {

        dataBalance[i].amtacctdr = "0.00";
        dataBalance[i].amtacctcr = "0.00";
        dataBalance[i].factaccttype = "XX";
        dataBalance[i].description = null;

        vectordata.add(dataBalance[i]);
        montoInicial = montoInicial.add(new BigDecimal(dataBalance[i].balancedr)
            .subtract(new BigDecimal(dataBalance[i].balancecr)));

        // Agregando los valores al hashmap de balance inicial
        Properties prop = new Properties();
        prop.setProperty("balancedr", dataBalance[i].balancedr);
        prop.setProperty("balancecr", dataBalance[i].balancecr);
        hashmapBalances.put(dataBalance[i].value, prop);
      }

      for (int i = 0; i < vectordata.size(); i++) {
        if (vars.commandIn("PDF")) {
          if (vectordata.get(i).factaccttype.equals("1")) {
            // vectordata.removeElementAt(i);
            vectordata.get(i).description = null;
            vectordata.get(i).amtacctdr = "0.00";
            vectordata.get(i).amtacctcr = "0.00";

          }
        }
        montoFinal = montoFinal.add(new BigDecimal(vectordata.get(i).amtacctdr)
            .subtract(new BigDecimal(vectordata.get(i).amtacctcr)));

      }

      data = new ReportGeneralLedgerData[vectordata.size()];

      if (data.length == 0 && hashmapBalances.size() == 0) {
        advisePopUp(request, response, "WARNING",
            Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
            Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
        return;
      }

      vectordata.copyInto(data);

      Arrays.sort(data, new Comparator<ReportGeneralLedgerData>() {
        public int compare(ReportGeneralLedgerData d1, ReportGeneralLedgerData d2) {
          return d1.value.compareTo(d2.value);
        }
      });
      /*************************************************************************/

      // /GENERANDO EL ARBOL DE CUENTAS

      ReportGeneralLedgerData arbolCuentas[] = ReportGeneralLedgerData.select_arbol_de_cuentas(this,
          "'" + strOrg + "'",
          Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"));
      String valueAnterior = "";
      String nombreDigitos2 = "";
      String nombreDigitos3 = "";
      String nombreDigitos4 = "";
      String nombreDigitos5 = "";
      String nombreDigitos6 = "";
      String nombreDigitos7 = "";
      String nombreDigitos8 = "";

      for (int i = 0; i < data.length; i++) {

        ReportGeneralLedgerData obj = data[i];

        String cuenta = obj.value;
        String nombreCuenta = "";
        if (cuenta.compareTo(valueAnterior) != 0) {

          nombreDigitos2 = "xx";
          nombreDigitos3 = "xxx";
          nombreDigitos4 = "xxxx";
          nombreDigitos5 = "xxxxx";
          nombreDigitos6 = "xxxxxx";
          nombreDigitos7 = "xxxxxxx";
          nombreDigitos8 = "xxxxxxxx";

          if (cuenta.length() >= 2) {
            nombreCuenta = buscaNombreCuenta(arbolCuentas, cuenta.substring(0, 2));
            nombreDigitos2 = nombreCuenta != null ? cuenta.substring(0, 2) + " - " + nombreCuenta
                : nombreDigitos2;
          }
          if (cuenta.length() >= 3) {
            nombreCuenta = buscaNombreCuenta(arbolCuentas, cuenta.substring(0, 3));
            nombreDigitos3 = nombreCuenta != null ? cuenta.substring(0, 3) + " - " + nombreCuenta
                : nombreDigitos3;
          }
          if (cuenta.length() >= 4) {
            nombreCuenta = buscaNombreCuenta(arbolCuentas, cuenta.substring(0, 4));
            nombreDigitos4 = nombreCuenta != null ? cuenta.substring(0, 4) + " - " + nombreCuenta
                : nombreDigitos4;
          }
          if (cuenta.length() >= 5) {
            nombreCuenta = buscaNombreCuenta(arbolCuentas, cuenta.substring(0, 5));
            nombreDigitos5 = nombreCuenta != null ? cuenta.substring(0, 5) + " - " + nombreCuenta
                : nombreDigitos5;
          }
          if (cuenta.length() >= 6) {
            nombreCuenta = buscaNombreCuenta(arbolCuentas, cuenta.substring(0, 6));
            nombreDigitos6 = nombreCuenta != null ? cuenta.substring(0, 6) + " - " + nombreCuenta
                : nombreDigitos6;
          }
          if (cuenta.length() >= 7) {
            nombreCuenta = buscaNombreCuenta(arbolCuentas, cuenta.substring(0, 7));
            nombreDigitos7 = nombreCuenta != null ? cuenta.substring(0, 7) + " - " + nombreCuenta
                : nombreDigitos7;
          }
          if (cuenta.length() >= 8) {
            nombreCuenta = buscaNombreCuenta(arbolCuentas, cuenta.substring(0, 8));
            nombreDigitos8 = nombreCuenta != null ? cuenta.substring(0, 8) + " - " + nombreCuenta
                : nombreDigitos8;
          }

          valueAnterior = obj.value;
        }

        obj.digitos2 = nombreDigitos2;
        obj.digitos3 = nombreDigitos3;
        obj.digitos4 = nombreDigitos4;
        obj.digitos5 = nombreDigitos5;
        obj.digitos6 = nombreDigitos6;
        obj.digitos7 = nombreDigitos7;
        obj.digitos8 = nombreDigitos8;

      }

      // augment data with totals
      // AddTotals dataWithTotals = new AddTotals(data, strGroupByText,
      // strcBpartnerId,
      // strmProductId, strcProjectId, strcAcctSchemaId, strDateFrom,
      // strOrgFamily, this);

      String strOutput;
      String strReportName;

      if (vars.commandIn("PDF")) {
        strOutput = "pdf";
        strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportGeneralLedger.jrxml";

      } else {

        for (int i = 0; i < data.length; i++) {

          String nroRegistro = data[i].nreg;
          if (nroRegistro == null)
            continue;
          // System.out
          // .println("tamaño total: " + data.length + " indice: " + i + " num:" + nroRegistro);
          data[i].rn1 = nroRegistro.contains("-") ? nroRegistro.split("-")[0] : "--";
          data[i].nreg = nroRegistro.contains("-")
              ? nroRegistro.split("-")[1] + "-" + nroRegistro.split("-")[2] : "--";
        }
        strOutput = "xls";
        strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportGeneralLedgerExcel.jrxml";
      }

      // response.setHeader("Content-disposition", "inline; filename=ReportGeneralLedgerPDF.pdf");

      HashMap<String, Object> parameters = new HashMap<String, Object>();

      String strLanguage = vars.getLanguage();

      parameters.put("ShowGrouping", new Boolean(!strGroupBy.equals("")));
      StringBuilder strSubTitle = new StringBuilder();
      strSubTitle.append(Utility.messageBD(this, "DateFrom", strLanguage) + ": " + strDateFrom
          + " - " + Utility.messageBD(this, "DateTo", strLanguage) + ": " + strDateTo + " (");
      strSubTitle.append(ReportGeneralLedgerData.selectCompany(this, vars.getClient()) + " - ");
      strSubTitle.append(ReportGeneralLedgerData.selectOrganization(this, strOrg) + ")");
      parameters.put("REPORT_SUBTITLE", strSubTitle.toString());
      parameters.put("Previous", Utility.messageBD(this, "Initial Balance", strLanguage));
      parameters.put("Total", Utility.messageBD(this, "Total", strLanguage));
      parameters.put("PageNo", strPageNo);
      parameters.put("Periodo", Utility.messageBD(this, "Del", strLanguage) + ": " + strDateFrom
          + " - " + Utility.messageBD(this, "al", strLanguage) + ": " + strDateTo);
      parameters.put("Ruc", ReportGeneralLedgerData.selectRUC(this, strOrg));
      parameters.put("Razon_social", ReportGeneralLedgerData.selectSocialName(this, strOrg));
      String strDateFormat;
      strDateFormat = vars.getJavaDateFormat();
      parameters.put("strDateFormat", strDateFormat);
      parameters.put("DATE_FROM", strDateFrom);
      parameters.put("DATE_TO", strDateTo);
      parameters.put("MONTO_INICIAL", montoInicial);
      parameters.put("MONTO_FINAL", montoFinal);
      parameters.put("MOSTRAR_SALDOS", strMostrarSaldos);
      parameters.put("PERIODO_AJUSTE", periodo_ajuste);

      parameters.put("dateFrom", StringToDate(strDateFrom));
      parameters.put("dateTo", StringToDate(strDateTo));

      parameters.put("balances", hashmapBalances);

      renderJR(vars, response, strReportName, "Libro_Mayor", strOutput, parameters, data, null);
      // } finally {
      // if (data != null) {
      // data.close();
      // }
      // }
    }
  }

  private String buscaNombreCuenta(ReportGeneralLedgerData[] arbolCuentas, String numeroCuenta) {
    String nombre = "";
    for (int i = 0; i < arbolCuentas.length; i++) {
      ReportGeneralLedgerData cuenta = arbolCuentas[i];
      if (cuenta == null)
        continue;
      if (cuenta.value.compareToIgnoreCase(numeroCuenta) == 0) {
        nombre = cuenta.name;
        cuenta = null;
        return nombre;
      }
    }

    return null;
  }

  private void printPageDataXLS(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String strAmtFrom,
      String strAmtTo, String strcelementvaluefrom, String strcelementvalueto, String strOrg,
      String strcBpartnerId, String strmProductId, String strcProjectId, String strGroupBy,
      String strcAcctSchemaId, String strDescargarLibroElectronico)
      throws IOException, ServletException {
    log4j.debug("Output: XLS");

    response.setContentType("text/html; charset=UTF-8");
    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    String strOrgFamily = "";
    strOrgFamily = getFamily(this, strTreeOrg, strOrg);
    String toDatePlusOne = DateTimeData.nDaysAfter(this, strDateTo, "1");

    String strAllaccounts = "Y";

    if (strDateFrom.equals("") || strDateTo.equals("")) {
      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
      return;
    }

    if (strcelementvaluefrom != null && !strcelementvaluefrom.equals("")) {
      if (strcelementvalueto.equals(""))
        strcelementvalueto = strcelementvaluefrom;
      strAllaccounts = "N";
    }

    ReportGeneralLedgerData data = null;
    try {
      data = ReportGeneralLedgerData.selectXLS2(this, strAllaccounts, strcelementvaluefrom,
          strcelementvalueto,
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportGeneralLedger"),
          Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"), strcAcctSchemaId,
          strDateFrom, toDatePlusOne, strOrgFamily, strcBpartnerId, strmProductId, strcProjectId,
          strAmtFrom, strAmtTo);

      if (!data.hasData()) {
        advisePopUp(request, response, "WARNING",
            Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
            Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
        return;
      }

      ScrollableFieldProvider limitedData = new LimitRowsScrollableFieldProviderFilter(data, 65532);

      String strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportGeneralLedgerExcel.jrxml";

      HashMap<String, Object> parameters = new HashMap<String, Object>();

      String strLanguage = vars.getLanguage();

      StringBuilder strSubTitle = new StringBuilder();
      strSubTitle.append(Utility.messageBD(this, "DateFrom", strLanguage) + ": " + strDateFrom
          + " - " + Utility.messageBD(this, "DateTo", strLanguage) + ": " + strDateTo + " (");
      strSubTitle.append(ReportGeneralLedgerData.selectCompany(this, vars.getClient()) + " - ");
      strSubTitle.append(ReportGeneralLedgerData.selectOrganization(this, strOrg) + ")");
      parameters.put("REPORT_SUBTITLE", strSubTitle.toString());
      String strDateFormat;
      strDateFormat = vars.getJavaDateFormat();
      parameters.put("strDateFormat", strDateFormat);

      renderJR(vars, response, strReportName, null, "xls", parameters, limitedData, null);
    } finally {
      if (data != null) {
        data.close();
      }
    }
  }

  private static class AddTotals extends AbstractScrollableFieldProviderFilter {
    public AddTotals(ScrollableFieldProvider input, String strGroupBy, String strcBpartnerId,
        String strmProductId, String strcProjectId, String strcAcctSchemaId, String strDateFrom,
        String strOrgFamily, ConnectionProvider conn) {
      super(input);
      this.strGroupBy = strGroupBy;
      this.strcBpartnerId = strcBpartnerId;
      this.strmProductId = strmProductId;
      this.strcProjectId = strcProjectId;
      this.strcAcctSchemaId = strcAcctSchemaId;
      this.strDateFrom = strDateFrom;
      this.strOrgFamily = strOrgFamily;
      this.conn = conn;
    }

    String strGroupBy;
    String strcBpartnerId;
    String strmProductId;
    String strcProjectId;
    String strcAcctSchemaId;
    String strDateFrom;
    String strOrgFamily;
    ConnectionProvider conn;
    String strOld = "";
    BigDecimal totalDebit = BigDecimal.ZERO;
    BigDecimal totalCredit = BigDecimal.ZERO;
    BigDecimal subTotal = BigDecimal.ZERO;
    ReportGeneralLedgerData subreport[] = new ReportGeneralLedgerData[1];

    @Override
    public FieldProvider get() throws ServletException {

      FieldProvider data = input.get();

      ReportGeneralLedgerData cur = (ReportGeneralLedgerData) data;

      // adjust data as needed
      if (!strOld.equals(cur.groupbyid + cur.id)) {
        if ("".equals(cur.groupbyid)) {
          // The argument " " is used to simulate one value and put
          // the optional parameter--> AND
          // FACT_ACCT.C_PROJECT_ID IS NULL for example
          subreport = ReportGeneralLedgerData.selectTotal2(conn, strcBpartnerId,
              (strGroupBy.equals("BPartner") ? " " : null), strmProductId,
              (strGroupBy.equals("Product") ? " " : null), strcProjectId,
              (strGroupBy.equals("Project") ? " " : null), strcAcctSchemaId, cur.id, "",
              strDateFrom, strOrgFamily);
        } else {
          subreport = ReportGeneralLedgerData.selectTotal2(conn,
              (strGroupBy.equals("BPartner") ? "('" + cur.groupbyid + "')" : strcBpartnerId), null,
              (strGroupBy.equals("Product") ? "('" + cur.groupbyid + "')" : strmProductId), null,
              (strGroupBy.equals("Project") ? "('" + cur.groupbyid + "')" : strcProjectId), null,
              strcAcctSchemaId, cur.id, "", strDateFrom, strOrgFamily);
        }
        totalDebit = BigDecimal.ZERO;
        totalCredit = BigDecimal.ZERO;
        subTotal = BigDecimal.ZERO;
      }
      totalDebit = totalDebit.add(new BigDecimal(cur.amtacctdr));
      cur.totalacctdr = new BigDecimal(subreport[0].totalacctdr).add(totalDebit).toString();
      totalCredit = totalCredit.add(new BigDecimal(cur.amtacctcr));
      cur.totalacctcr = new BigDecimal(subreport[0].totalacctcr).add(totalCredit).toString();
      subTotal = subTotal.add(new BigDecimal(cur.total));
      cur.totalacctsub = new BigDecimal(subreport[0].total).add(subTotal).toString();
      cur.previousdebit = subreport[0].totalacctdr;
      cur.previouscredit = subreport[0].totalacctcr;
      cur.previoustotal = subreport[0].total;
      strOld = cur.groupbyid + cur.id;

      return data;
    }

  }

  static private String getFamily(ConnectionProvider conn, String strTree, String strChild)
      throws IOException, ServletException {
    return Tree.getMembers(conn, strTree, strChild);
  }

  @Override
  public String getServletInfo() {
    return "Servlet ReportGeneralLedger. This Servlet was made by Pablo Sarobe";
  } // end of getServletInfo() method
}
