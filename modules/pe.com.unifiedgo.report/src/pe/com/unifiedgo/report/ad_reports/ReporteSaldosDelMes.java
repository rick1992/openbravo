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
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
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
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class ReporteSaldosDelMes extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strcAcctSchemaId = vars.getGlobalVariable("inpcAcctSchemaId",
          "ReporteSaldosDelMes|cAcctSchemaId", "");
      String strDateFrom = vars
          .getGlobalVariable("inpDateFrom", "ReporteSaldosDelMes|DateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReporteSaldosDelMes|DateTo", "");
      String strPageNo = vars.getGlobalVariable("inpPageNo", "ReporteSaldosDelMes|PageNo", "1");
      String strAmtFrom = vars.getNumericGlobalVariable("inpAmtFrom",
          "ReporteSaldosDelMes|AmtFrom", "");
      String strAmtTo = vars.getNumericGlobalVariable("inpAmtTo", "ReporteSaldosDelMes|AmtTo", "");
      String strcelementvaluefrom = vars.getGlobalVariable("inpcElementValueIdFrom",
          "ReporteSaldosDelMes|C_ElementValue_IDFROM", "");
      String strcelementvalueto = vars.getGlobalVariable("inpcElementValueIdTo",
          "ReporteSaldosDelMes|C_ElementValue_IDTO", "");
      String strcelementvaluefromdes = "", strcelementvaluetodes = "";
      if (!strcelementvaluefrom.equals(""))
        strcelementvaluefromdes = ReporteSaldosDelMesData.selectSubaccountDescription(this,
            strcelementvaluefrom);
      if (!strcelementvalueto.equals(""))
        strcelementvaluetodes = ReporteSaldosDelMesData.selectSubaccountDescription(this,
            strcelementvalueto);
      strcelementvaluefromdes = (strcelementvaluefromdes.equals("null")) ? ""
          : strcelementvaluefromdes;
      strcelementvaluetodes = (strcelementvaluetodes.equals("null")) ? "" : strcelementvaluetodes;
      vars.setSessionValue("inpElementValueIdFrom_DES", strcelementvaluefromdes);
      vars.setSessionValue("inpElementValueIdTo_DES", strcelementvaluetodes);
      String strOrg = vars.getGlobalVariable("inpOrg", "ReporteSaldosDelMes|Org", "0");
      String strcBpartnerId = vars.getInGlobalVariable("inpcBPartnerId_IN",
          "ReporteSaldosDelMes|cBpartnerId", "", IsIDFilter.instance);
      String strmProductId = vars.getInGlobalVariable("inpmProductId_IN",
          "ReporteSaldosDelMes|mProductId", "", IsIDFilter.instance);
      String strcProjectId = vars.getInGlobalVariable("inpcProjectId_IN",
          "ReporteSaldosDelMes|cProjectId", "", IsIDFilter.instance);
      String strDescargarLibroElectronico = vars.getGlobalVariable("inpDescargarLibroElectronico",
          "ReporteSaldosDelMes|DescargarLibroElectronico", "");

      String strGroupBy = "";
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strPageNo, strAmtFrom, strAmtTo,
          strcelementvaluefrom, strcelementvalueto, strOrg, strcBpartnerId, strmProductId,
          strcProjectId, strGroupBy, strcAcctSchemaId, strcelementvaluefromdes,
          strcelementvaluetodes, strDescargarLibroElectronico);
    } else if (vars.commandIn("PREVIOUS_RELATION")) {
      String strInitRecord = vars.getSessionValue("ReporteSaldosDelMes.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRange", "ReporteSaldosDelMes");
      int intRecordRange = strRecordRange.equals("") ? 0 : Integer.parseInt(strRecordRange);
      if (strInitRecord.equals("") || strInitRecord.equals("0"))
        vars.setSessionValue("ReporteSaldosDelMes.initRecordNumber", "0");
      else {
        int initRecord = (strInitRecord.equals("") ? 0 : Integer.parseInt(strInitRecord));
        initRecord -= intRecordRange;
        strInitRecord = ((initRecord < 0) ? "0" : Integer.toString(initRecord));
        vars.setSessionValue("ReporteSaldosDelMes.initRecordNumber", strInitRecord);
      }
      response.sendRedirect(getDireccion() + request.getServletPath());
    } else if (vars.commandIn("NEXT_RELATION")) {
      String strInitRecord = vars.getSessionValue("ReporteSaldosDelMes.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRange", "ReporteSaldosDelMes");
      int intRecordRange = strRecordRange.equals("") ? 0 : Integer.parseInt(strRecordRange);
      int initRecord = (strInitRecord.equals("") ? 0 : Integer.parseInt(strInitRecord));
      // if (initRecord == 0)
      // initRecord = 1; Removed by DAL 30/4/09
      initRecord += intRecordRange;
      strInitRecord = ((initRecord < 0) ? "0" : Integer.toString(initRecord));
      vars.setSessionValue("ReporteSaldosDelMes.initRecordNumber", strInitRecord);
      response.sendRedirect(getDireccion() + request.getServletPath());
    } else if (vars.commandIn("PDF", "XLS")) {
      String strcAcctSchemaId = vars.getRequestGlobalVariable("inpcAcctSchemaId",
          "ReporteSaldosDelMes|cAcctSchemaId");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReporteSaldosDelMes|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReporteSaldosDelMes|DateTo");
      String strAmtFrom = vars.getNumericParameter("inpAmtFrom");
      vars.setSessionValue("ReporteSaldosDelMes|AmtFrom", strAmtFrom);
      String strAmtTo = vars.getNumericParameter("inpAmtTo");
      vars.setSessionValue("ReporteSaldosDelMes|AmtTo", strAmtTo);
      String strcelementvaluefrom = vars.getRequestGlobalVariable("inpcElementValueIdFrom",
          "ReporteSaldosDelMes|C_ElementValue_IDFROM");
      String strcelementvalueto = vars.getRequestGlobalVariable("inpcElementValueIdTo",
          "ReporteSaldosDelMes|C_ElementValue_IDTO");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReporteSaldosDelMes|Org", "0");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN",
          "ReporteSaldosDelMes|cBpartnerId", IsIDFilter.instance);
      String strmProductId = vars.getInGlobalVariable("inpmProductId_IN",
          "ReporteSaldosDelMes|mProductId", "", IsIDFilter.instance);
      String strcProjectId = vars.getInGlobalVariable("inpcProjectId_IN",
          "ReporteSaldosDelMes|cProjectId", "", IsIDFilter.instance);
      String strDescargarLibroElectronico = vars.getStringParameter("inpDescargarLibroElectronico");

      String strPageNo = vars.getGlobalVariable("inpPageNo", "ReporteSaldosDelMes|PageNo", "1");

      String strGroupBy = vars
          .getRequestGlobalVariable("inpGroupBy", "ReporteSaldosDelMes|GroupBy");
      if (vars.commandIn("PDF"))
        printPageDataPDF_ugo(request, response, vars, strDateFrom, strDateTo, strAmtFrom, strAmtTo,
            strcelementvaluefrom, strcelementvalueto, strOrg, strcBpartnerId, strmProductId,
            strcProjectId, strGroupBy, strcAcctSchemaId, strPageNo, strDescargarLibroElectronico);
      else
        printPageDataXLS(request, response, vars, strDateFrom, strDateTo, strAmtFrom, strAmtTo,
            strcelementvaluefrom, strcelementvalueto, strOrg, strcBpartnerId, strmProductId,
            strcProjectId, strGroupBy, strcAcctSchemaId, strDescargarLibroElectronico);
    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strPageNo, String strAmtFrom, String strAmtTo,
      String strcelementvaluefrom, String strcelementvalueto, String strOrg, String strcBpartnerId,
      String strmProductId, String strcProjectId, String strGroupBy, String strcAcctSchemaId,
      String strcelementvaluefromdes, String strcelementvaluetodes,
      String strDescargarLibroElectronico) throws IOException, ServletException {

    String strRecordRange = Utility.getContext(this, vars, "#RecordRange", "ReporteSaldosDelMes");
    int intRecordRange = (strRecordRange.equals("") ? 0 : Integer.parseInt(strRecordRange));
    String strInitRecord = vars.getSessionValue("ReporteSaldosDelMes.initRecordNumber");
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
    ReporteSaldosDelMesData[] data = null;
    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    String strOrgFamily = getFamily(strTreeOrg, strOrg);
    String toDatePlusOne = DateTimeData.nDaysAfter(this, strDateTo, "1");

    String strGroupByText = (strGroupBy.equals("BPartner") ? Utility.messageBD(this, "BusPartner",
        vars.getLanguage()) : (strGroupBy.equals("Product") ? Utility.messageBD(this, "Product",
        vars.getLanguage()) : (strGroupBy.equals("Project") ? Utility.messageBD(this, "Project",
        vars.getLanguage()) : "")));

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReporteSaldosDelMes", true, "", "",
        "imprimir();return false;", false, "ad_reports", strReplaceWith, false, true);

    toolbar.prepareSimpleToolBarTemplate();
    toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");

    String strcBpartnerIdAux = strcBpartnerId;
    String strmProductIdAux = strmProductId;
    String strcProjectIdAux = strcProjectId;
    if (strDateFrom.equals("") && strDateTo.equals("")) {
      String discard[] = { "sectionAmount", "sectionPartner" };
      xmlDocument = xmlEngine.readXmlTemplate(
          "pe/com/unifiedgo/report/ad_reports/ReporteSaldosDelMes", discard).createXmlDocument();
      // toolbar
      // .prepareRelationBarTemplate(false, false,
      // "submitCommandForm('XLS', false, frmMain, 'ReporteSaldosDelMesExcel.xls', 'EXCEL');return false;");
      data = ReporteSaldosDelMesData.set();
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
          strcelementvaluetodes = ReporteSaldosDelMesData.selectSubaccountDescription(this,
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
      ReporteSaldosDelMesData scroll = null;
      try {
        scroll = ReporteSaldosDelMesData.select2(this, rowNum, strGroupByText, strGroupBy,
            strAllaccounts, strcelementvaluefrom, strcelementvalueto,
            Utility.getContext(this, vars, "#AccessibleOrgTree", "ReporteSaldosDelMes"),
            Utility.getContext(this, vars, "#User_Client", "ReporteSaldosDelMes"),
            strcAcctSchemaId, strDateFrom, toDatePlusOne, strOrgFamily, strcBpartnerId,
            strmProductId, strcProjectId, strAmtFrom, strAmtTo, null, null, pgLimit, oraLimit1,
            oraLimit2, null);
        Vector<ReporteSaldosDelMesData> res = new Vector<ReporteSaldosDelMesData>();
        while (scroll.next()) {
          res.add(scroll.get());
        }
        data = new ReporteSaldosDelMesData[res.size()];
        res.copyInto(data);
      } finally {
        if (scroll != null) {
          scroll.close();
        }
      }

      log4j.debug("Select2. Time in mils: " + (System.currentTimeMillis() - initMainSelect));
      log4j.debug("RecordNo: " + initRecordNumber);

      ReporteSaldosDelMesData[] dataTotal = null;
      ReporteSaldosDelMesData[] dataSubtotal = null;
      String strOld = "";
      // boolean firstPagBlock = false;
      ReporteSaldosDelMesData[] subreportElement = new ReporteSaldosDelMesData[1];
      for (int i = 0; data != null && i < data.length; i++) {
        if (!strOld.equals(data[i].groupbyid + data[i].id)) {
          subreportElement = new ReporteSaldosDelMesData[1];
          // firstPagBlock = false;
          if (i == 0 && initRecordNumber > 0) {
            // firstPagBlock = true;
            Long init = System.currentTimeMillis();
            dataTotal = ReporteSaldosDelMesData.select2Total(this, rowNum, strGroupByText,
                strGroupBy, strAllaccounts, strcelementvaluefrom, strcelementvalueto,
                Utility.getContext(this, vars, "#AccessibleOrgTree", "ReporteSaldosDelMes"),
                Utility.getContext(this, vars, "#User_Client", "ReporteSaldosDelMes"),
                strcAcctSchemaId, "", DateTimeData.nDaysAfter(this, data[0].dateacct, "1"),
                strOrgFamily, strcBpartnerId, strmProductId, strcProjectId, strAmtFrom, strAmtTo,
                data[0].id, data[0].groupbyid, null, null, null, data[0].dateacctnumber
                    + data[0].factaccttype + data[0].factAcctGroupId + data[0].factAcctId);
            dataSubtotal = ReporteSaldosDelMesData.select2sum(this, rowNum, strGroupByText,
                strGroupBy, strAllaccounts, strcelementvaluefrom, strcelementvalueto,
                Utility.getContext(this, vars, "#AccessibleOrgTree", "ReporteSaldosDelMes"),
                Utility.getContext(this, vars, "#User_Client", "ReporteSaldosDelMes"),
                strcAcctSchemaId, strDateFrom, toDatePlusOne, strOrgFamily,
                (strGroupBy.equals("BPartner") ? "('" + data[i].groupbyid + "')" : strcBpartnerId),
                (strGroupBy.equals("Product") ? "('" + data[i].groupbyid + "')" : strmProductId),
                (strGroupBy.equals("Project") ? "('" + data[i].groupbyid + "')" : strcProjectId),
                strAmtFrom, strAmtTo, null, null, null, null, null, data[0].dateacctnumber
                    + data[0].factaccttype + data[0].factAcctGroupId + data[0].factAcctId,
                data[0].id);

            log4j.debug("Select2Total. Time in mils: " + (System.currentTimeMillis() - init));
            // Now dataTotal is covered adding debit and credit
            // amounts
            for (int j = 0; dataTotal != null && j < dataTotal.length; j++) {
              previousDebit = previousDebit.add(new BigDecimal(dataTotal[j].amtacctdr));
              previousCredit = previousCredit.add(new BigDecimal(dataTotal[j].amtacctcr));
            }
            subreportElement = new ReporteSaldosDelMesData[1];
            subreportElement[0] = new ReporteSaldosDelMesData();
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
              subreportElement = ReporteSaldosDelMesData.selectTotal2(this, strcBpartnerId,
                  (strGroupBy.equals("BPartner") ? " " : null), strmProductId,
                  (strGroupBy.equals("Product") ? " " : null), strcProjectId,
                  (strGroupBy.equals("Project") ? " " : null), strcAcctSchemaId, data[i].id, "",
                  strDateFrom, strOrgFamily);
              log4j.debug("SelectTotalNew. Time in mils: " + (System.currentTimeMillis() - init));
            } else {
              Long init = System.currentTimeMillis();
              subreportElement = ReporteSaldosDelMesData
                  .selectTotal2(this, (strGroupBy.equals("BPartner") ? "('" + data[i].groupbyid
                      + "')" : strcBpartnerId), null, (strGroupBy.equals("Product") ? "('"
                      + data[i].groupbyid + "')" : strmProductId), null, (strGroupBy
                      .equals("Project") ? "('" + data[i].groupbyid + "')" : strcProjectId), null,
                      strcAcctSchemaId, data[i].id, "", strDateFrom, strOrgFamily);
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
      subreportElement = new ReporteSaldosDelMesData[1];
      for (int i = 0; data != null && i < data.length; i++) {
        if (!strTotal.equals(data[i].groupbyid + data[i].id)) {
          subreportElement = new ReporteSaldosDelMesData[1];
          if ("".equals(data[i].groupbyid)) {
            // The argument " " is used to simulate one value and
            // put the optional parameter--> AND
            // FACT_ACCT.C_PROJECT_ID IS NULL for example
            Long init = System.currentTimeMillis();
            subreportElement = ReporteSaldosDelMesData.selectTotal2(this, strcBpartnerId,
                (strGroupBy.equals("BPartner") ? " " : null), strmProductId,
                (strGroupBy.equals("Product") ? " " : null), strcProjectId,
                (strGroupBy.equals("Project") ? " " : null), strcAcctSchemaId, data[i].id, "",
                toDatePlusOne, strOrgFamily);
            log4j.debug("SelectTotal2. Time in mils: " + (System.currentTimeMillis() - init));
          } else {
            Long init = System.currentTimeMillis();
            subreportElement = ReporteSaldosDelMesData.selectTotal2(this, (strGroupBy
                .equals("BPartner") ? "('" + data[i].groupbyid + "')" : strcBpartnerId), null,
                (strGroupBy.equals("Product") ? "('" + data[i].groupbyid + "')" : strmProductId),
                null, (strGroupBy.equals("Project") ? "('" + data[i].groupbyid + "')"
                    : strcProjectId), null, strcAcctSchemaId, data[i].id, "", toDatePlusOne,
                strOrgFamily);
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
      // "submitCommandForm('XLS', true, frmMain, 'ReporteSaldosDelMesExcel.xls', 'EXCEL');return false;");
      xmlDocument = xmlEngine.readXmlTemplate(
          "pe/com/unifiedgo/report/ad_reports/ReporteSaldosDelMes", discard).createXmlDocument();
    }
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "pe.com.unifiedgo.report.ad_reports.ReporteSaldosDelMes");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReporteSaldosDelMes.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReporteSaldosDelMes.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReporteSaldosDelMes");
      vars.removeMessage("ReporteSaldosDelMes");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
          "", Utility.getContext(this, vars, "#AccessibleOrgTree", "ReporteSaldosDelMes"),
          Utility.getContext(this, vars, "#User_Client", "ReporteSaldosDelMes"), '*');
      comboTableData.fillParameters(null, "ReporteSaldosDelMes", "");
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

    xmlDocument.setParameter("paramPeriodosArray", Utility.arrayInfinitasEntradas(
        "idperiodo;periodo;fechainicial;fechafinal;idorganizacion", "arrPeriodos",
        ReporteSaldosDelMesData.select_periodos(this)));

    // xmlDocument.setParameter("groupbyselected", strGroupBy);
    xmlDocument.setData(
        "reportCBPartnerId_IN",
        "liststructure",
        SelectorUtilityData.selectBpartner(this,
            Utility.getContext(this, vars, "#AccessibleOrgTree", ""),
            Utility.getContext(this, vars, "#User_Client", ""), strcBpartnerIdAux));
    xmlDocument.setData(
        "reportMProductId_IN",
        "liststructure",
        SelectorUtilityData.selectMproduct(this,
            Utility.getContext(this, vars, "#AccessibleOrgTree", ""),
            Utility.getContext(this, vars, "#User_Client", ""), strmProductIdAux));
    xmlDocument.setData(
        "reportCProjectId_IN",
        "liststructure",
        SelectorUtilityData.selectProject(this,
            Utility.getContext(this, vars, "#AccessibleOrgTree", ""),
            Utility.getContext(this, vars, "#User_Client", ""), strcProjectIdAux));
    // xmlDocument
    // .setData("reportC_ACCTSCHEMA_ID", "liststructure", AccountingSchemaMiscData
    // .selectC_ACCTSCHEMA_ID(this,
    // Utility.getContext(this, vars, "#AccessibleOrgTree", "ReporteSaldosDelMes"),
    // Utility.getContext(this, vars, "#User_Client", "ReporteSaldosDelMes"),
    // strcAcctSchemaId));

    xmlDocument.setParameter("paramLibrosArray", Utility.arrayInfinitasEntradas(
        "idlibro;nombrelibro;idorganizacion", "arrLibroMayor",
        ReporteSaldosDelMesData.select_libros(this)));

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

  // private void printPageDataPDF(HttpServletRequest request, HttpServletResponse response,
  // VariablesSecureApp vars, String strDateFrom, String strDateTo, String strAmtFrom,
  // String strAmtTo, String strcelementvaluefrom, String strcelementvalueto, String strOrg,
  // String strcBpartnerId, String strmProductId, String strcProjectId, String strGroupBy,
  // String strcAcctSchemaId, String strPageNo, String strDescargarLibroElectronico)
  // throws IOException, ServletException {
  // log4j.debug("Output: PDF");
  // response.setContentType("text/html; charset=UTF-8");
  // ReporteSaldosDelMesData[] subreport = null;
  // String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
  // String strOrgFamily = getFamily(strTreeOrg, strOrg);
  // String toDatePlusOne = DateTimeData.nDaysAfter(this, strDateTo, "1");
  //
  // String strGroupByText = (strGroupBy.equals("BPartner") ? Utility.messageBD(this, "BusPartner",
  // vars.getLanguage()) : (strGroupBy.equals("Product") ? Utility.messageBD(this, "Product",
  // vars.getLanguage()) : (strGroupBy.equals("Project") ? Utility.messageBD(this, "Project",
  // vars.getLanguage()) : "")));
  // String strAllaccounts = "Y";
  //
  // if (strDateFrom.equals("") || strDateTo.equals("")) {
  // advisePopUp(request, response, "WARNING",
  // Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
  // Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
  // return;
  // }
  //
  // if (strcelementvaluefrom != null && !strcelementvaluefrom.equals("")) {
  // if (strcelementvalueto.equals(""))
  // strcelementvalueto = strcelementvaluefrom;
  // strAllaccounts = "N";
  // }
  //
  // ReporteSaldosDelMesData data = null;
  // try {
  // data = ReporteSaldosDelMesData.select2(this, "0", strGroupByText, strGroupBy, strAllaccounts,
  // strcelementvaluefrom, strcelementvalueto,
  // Utility.getContext(this, vars, "#AccessibleOrgTree", "ReporteSaldosDelMes"),
  // Utility.getContext(this, vars, "#User_Client", "ReporteSaldosDelMes"), strcAcctSchemaId,
  // strDateFrom, toDatePlusOne, strOrgFamily, strcBpartnerId, strmProductId, strcProjectId,
  // strAmtFrom, strAmtTo, null, null, null, null, null, null);
  //
  // if (!data.hasData()) {
  // advisePopUp(request, response, "WARNING",
  // Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
  // Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
  // return;
  // }
  //
  // // augment data with totals
  // AddTotals dataWithTotals = new AddTotals(data, strGroupByText, strcBpartnerId, strmProductId,
  // strcProjectId, strcAcctSchemaId, strDateFrom, strOrgFamily, this);
  //
  // String strReportName =
  // "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteSaldosDelMes.jrxml";
  // response.setHeader("Content-disposition", "inline; filename=ReporteSaldosDelMesPDF.pdf");
  //
  // HashMap<String, Object> parameters = new HashMap<String, Object>();
  //
  // String strLanguage = vars.getLanguage();
  //
  // parameters.put("ShowGrouping", new Boolean(!strGroupBy.equals("")));
  // StringBuilder strSubTitle = new StringBuilder();
  // strSubTitle.append(Utility.messageBD(this, "DateFrom", strLanguage) + ": " + strDateFrom
  // + " - " + Utility.messageBD(this, "DateTo", strLanguage) + ": " + strDateTo + " (");
  // strSubTitle.append(ReporteSaldosDelMesData.selectCompany(this, vars.getClient()) + " - ");
  // strSubTitle.append(ReporteSaldosDelMesData.selectOrganization(this, strOrg) + ")");
  // parameters.put("REPORT_SUBTITLE", strSubTitle.toString());
  // parameters.put("Previous", Utility.messageBD(this, "Initial Balance", strLanguage));
  // parameters.put("Total", Utility.messageBD(this, "Total", strLanguage));
  // parameters.put("PageNo", strPageNo);
  // String strDateFormat;
  // strDateFormat = vars.getJavaDateFormat();
  // parameters.put("strDateFormat", strDateFormat);
  // parameters.put("dateFrom", StringToDate(strDateFrom));
  // parameters.put("dateTo", StringToDate(strDateTo));
  //
  // renderJR(vars, response, strReportName, null, "pdf", parameters, dataWithTotals, null);
  // } finally {
  // if (data != null) {
  // data.close();
  // }
  // }
  // }

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
      String strcAcctSchemaId, String strPageNo, String strDescargarLibroElectronico)
      throws IOException, ServletException {
    log4j.debug("Output: PDF");

    boolean isLE = false;
    if (strDescargarLibroElectronico.equals("Y")) {
      isLE = true;
    }

    // quitar
    // if (isLE) {
    //
    // // Libro electronico
    // ReporteSaldosDelMesLEData[] le = ReporteSaldosDelMesLEData.select(this, strDateFrom,
    // strDateTo, strcAcctSchemaId);
    // StringBuffer sb = new StringBuffer();
    //
    // int correlativo = 0;
    // String prevRegNumber = "";
    // Organization org = OBDal.getInstance().get(Organization.class, strOrg);
    // String rucAdq = SunatUtil.LPad(org.getOrganizationInformationList().get(0).getTaxID(), 11,
    // '0');
    //
    // SimpleDateFormat formatterForm = new SimpleDateFormat("dd-MM-yyyy");
    // Date dttFrom = null;
    // try {
    // dttFrom = formatterForm.parse(strDateFrom);
    // } catch (Exception ex) {
    // System.out.println("Exception: " + strDateFrom);
    // }
    //
    // SimpleDateFormat dt = new SimpleDateFormat("yyyyMM");
    // SimpleDateFormat dt2 = new SimpleDateFormat("dd/MM/yyyy");
    //
    // String filename = "LE" + rucAdq + dt.format(dttFrom) + "00060100001111.TXT";//
    // LERRRRRRRRRRRAAAAMM0006010000OIM1.TXT
    //
    // for (int i = 0; i < le.length; i++) {
    //
    // ReporteSaldosDelMesLEData led = le[i];
    //
    // Date dttAcct = null;
    // try {
    // dttAcct = formatterForm.parse(led.dateacct);
    // } catch (Exception ex) {
    // }
    // String periodoTrib = dt.format(dttAcct) + "00";
    //
    // String regnumber = led.emScoRegnumber;
    //
    // String tipoAsiento = "M";
    // if (led.factaccttype.equals("O"))
    // tipoAsiento = "A";
    // if (led.factaccttype.equals("C"))
    // tipoAsiento = "C";
    //
    // if (prevRegNumber.equals(regnumber)) {
    // tipoAsiento = tipoAsiento + SunatUtil.LPad(String.valueOf(correlativo), 5, '0');
    // correlativo++;
    // } else {
    // tipoAsiento = tipoAsiento + "00000";
    // correlativo = 1;
    // }
    // prevRegNumber = regnumber;
    //
    // String plancuentas = "01";
    // String cuentaContable = led.acctvalue;
    //
    // Date dttTrx = null;
    // try {
    // dttTrx = formatterForm.parse(led.datetrx);
    // } catch (Exception ex) {
    // }
    //
    // String fechaOp = dt2.format(dttTrx);
    // String glosa = led.description;
    // if (glosa.length() > 100)
    // glosa = glosa.substring(0, 100);
    //
    // NumberFormat formatter = new DecimalFormat("#0.00");
    //
    // String debe = formatter
    // .format((led.amtacctdr != null && !led.amtacctdr.equals("")) ? Double
    // .parseDouble(led.amtacctdr) : 0.00);
    // String haber = formatter
    // .format((led.amtacctcr != null && !led.amtacctcr.equals("")) ? Double
    // .parseDouble(led.amtacctcr) : 0.00);
    //
    // String estadoOp = "1";
    // SimpleDateFormat formatterYYMM = new SimpleDateFormat("yyyy/MM");
    // try {
    // Date dateAcctf = formatterYYMM.parse(dttAcct.getYear() + "/" + dttAcct.getMonth());
    // Date dateTrxf = formatterYYMM.parse(dttTrx.getYear() + "/" + dttTrx.getMonth());
    // if (dateTrxf.compareTo(dateAcctf) < 0)
    // estadoOp = "8";
    // } catch (Exception e) {
    // }
    //
    // String linea = periodoTrib + "|" + regnumber + "|" + tipoAsiento + "|" + plancuentas + "|"
    // + cuentaContable + "|" + fechaOp + "|" + glosa + "|" + debe + "|" + haber + "||||"
    // + estadoOp + "|";
    // if (i > 0)
    // sb.append("\n");
    // sb.append(linea);
    // }
    //
    // response.setContentType("text/plain");
    // response.setHeader("Content-Disposition", "attachment;filename=" + filename);
    //
    // ServletOutputStream os = response.getOutputStream();
    //
    // os.write(sb.toString().getBytes());
    //
    // os.flush();
    // os.close();
    // quitar
    // } else {/* PDF */

    response.setContentType("text/html; charset=UTF-8");
    ReporteSaldosDelMesData[] subreport = null;
    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    String strOrgFamily = getFamily(strTreeOrg, strOrg);
    String toDatePlusOne = DateTimeData.nDaysAfter(this, strDateTo, "1");

    /****************************************************************/
    String fechaInicioAnnio = strDateFrom.substring(strDateFrom.length() - 4, strDateFrom.length());
    fechaInicioAnnio = "01-01-" + fechaInicioAnnio;

    /****************************************************************/

    String strGroupByText = (strGroupBy.equals("BPartner") ? Utility.messageBD(this, "BusPartner",
        vars.getLanguage()) : (strGroupBy.equals("Product") ? Utility.messageBD(this, "Product",
        vars.getLanguage()) : (strGroupBy.equals("Project") ? Utility.messageBD(this, "Project",
        vars.getLanguage()) : (strGroupBy.equals("Costcenter") ? Utility.messageBD(this,
        "Cost Center", vars.getLanguage()) : ""))));
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

    ReporteSaldosDelMesData data[] = null;
    ReporteSaldosDelMesData temp[] = null;
    // try {
    /******** se cambios select2ugo por selectsaldos ************/

    temp = ReporteSaldosDelMesData.selectsaldos(this, strDateFrom, strDateFrom, toDatePlusOne,
        toDatePlusOne, strcAcctSchemaId.toString(), strcAcctSchemaId.toString(),
        strcAcctSchemaId.toString(),
        // Utility.getContext(this, vars, "#AccessibleOrgTree", "ReporteSaldosDelMes"));
        strOrgFamily);

    data = ReporteSaldosDelMesData.select2ugo(this, "0", strGroupByText, strGroupBy,
        strAllaccounts, strcelementvaluefrom, strcelementvalueto,
        Utility.getContext(this, vars, "#AccessibleOrgTree", "ReporteSaldosDelMes"),
        Utility.getContext(this, vars, "#User_Client", "ReporteSaldosDelMes"), strcAcctSchemaId,
        strDateFrom, toDatePlusOne, strOrgFamily, strcBpartnerId, strmProductId, strcProjectId,
        strAmtFrom, strAmtTo, null, null, null, null, null, null);
    /*
     * temp = ReporteSaldosDelMesData.selectsaldos(this, Utility.getContext(this, vars,
     * "#AccessibleOrgTree", "ReporteSaldosDelMes"));
     */
    // temp = ReporteSaldosDelMesData.selectsaldos(this);

    // if (!data.hasData()) {
    // advisePopUp(request, response, "WARNING",
    // Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
    // Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
    // return;
    // }

    /*************************************************************************/
    ReporteSaldosDelMesData dataBalance[] = null;
    dataBalance = ReporteSaldosDelMesData.select2balanceanterior(this, "0", strGroupByText,
        strGroupBy, strAllaccounts, strcelementvaluefrom, strcelementvalueto,
        Utility.getContext(this, vars, "#AccessibleOrgTree", "ReporteSaldosDelMes"),
        Utility.getContext(this, vars, "#User_Client", "ReporteSaldosDelMes"), strcAcctSchemaId,
        fechaInicioAnnio, DateTimeData.nDaysAfter(this, strDateFrom, "1"), strOrgFamily,
        strcBpartnerId, strmProductId, strcProjectId, strAmtFrom, strAmtTo, null, null, null, null,
        null, null);
    // temp
    Vector<ReporteSaldosDelMesData> vectortemp = new Vector<ReporteSaldosDelMesData>(
        Arrays.asList(temp));

    Vector<ReporteSaldosDelMesData> vectordata = new Vector<ReporteSaldosDelMesData>(
        Arrays.asList(data));

    HashMap<String, Properties> hashmapBalances = new HashMap<String, Properties>();

    for (int i = 0; i < dataBalance.length; i++) {
      for (int j = 0; j < data.length; j++) {
        if (!dataBalance[i].value.equals(data[j].value)) {
          if (j < data.length - 1) { // si no es el utimo elemento del array
            continue;
          } else {
            vectordata.add(dataBalance[i]);
          }

        } else {
          vectordata.get(j).balancecr = dataBalance[i].balancecr;
          vectordata.get(j).balancedr = dataBalance[i].balancedr;
        }

        break;
      }

      // Agregando los valores al hashmap de balance inicial
      Properties prop = new Properties();
      prop.setProperty("balancedr", dataBalance[i].balancedr);
      prop.setProperty("balancecr", dataBalance[i].balancecr);
      hashmapBalances.put(dataBalance[i].value, prop);
    }

    data = new ReporteSaldosDelMesData[vectordata.size()];
    vectordata.copyInto(data);

    Arrays.sort(data, new Comparator<ReporteSaldosDelMesData>() {
      public int compare(ReporteSaldosDelMesData d1, ReporteSaldosDelMesData d2) {
        return d1.value.compareTo(d2.value);
      }
    });
    /*************************************************************************/

    // augment data with totals
    // AddTotals dataWithTotals = new AddTotals(data, strGroupByText, strcBpartnerId,
    // strmProductId, strcProjectId, strcAcctSchemaId, strDateFrom, strOrgFamily, this);

    String strOutput;
    String strReportName;

    if (vars.commandIn("PDF")) {
      strOutput = "pdf";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteSaldosDelMes.jrxml";

    } else {
      strOutput = "xls";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteSaldosDelMes.jrxml";
    }

    response.setHeader("Content-disposition", "inline; filename=ReporteSaldosDelMesPDF.pdf");

    HashMap<String, Object> parameters = new HashMap<String, Object>();

    String strLanguage = vars.getLanguage();

    parameters.put("ShowGrouping", new Boolean(!strGroupBy.equals("")));
    StringBuilder strSubTitle = new StringBuilder();
    strSubTitle.append(Utility.messageBD(this, "DateFrom", strLanguage) + ": " + strDateFrom
        + " - " + Utility.messageBD(this, "DateTo", strLanguage) + ": " + strDateTo + " (");
    strSubTitle.append(ReporteSaldosDelMesData.selectCompany(this, vars.getClient()) + " - ");
    strSubTitle.append(ReporteSaldosDelMesData.selectOrganization(this, strOrg) + ")");
    parameters.put("REPORT_SUBTITLE", strSubTitle.toString());
    parameters.put("Previous", Utility.messageBD(this, "Initial Balance", strLanguage));
    parameters.put("Total", Utility.messageBD(this, "Total", strLanguage));
    parameters.put("PageNo", strPageNo);
    parameters.put("Periodo", Utility.messageBD(this, "Del", strLanguage) + ": " + strDateFrom
        + " - " + Utility.messageBD(this, "al", strLanguage) + ": " + strDateTo);
    parameters.put("Ruc", ReporteSaldosDelMesData.selectRUC(this, strOrg));
    parameters.put("Razon_social", ReporteSaldosDelMesData.selectOrganization(this, strOrg));
    String strDateFormat;
    strDateFormat = vars.getJavaDateFormat();
    parameters.put("strDateFormat", strDateFormat);
    parameters.put("DATE_FROM", strDateFrom);
    parameters.put("DATE_TO", strDateTo);
    parameters.put("dateFrom", StringToDate(strDateFrom));
    parameters.put("dateTo", StringToDate(strDateTo));

    parameters.put("balances", hashmapBalances);

    parameters.put("Ruc", ReportLibroInventariosYBalance12y13Data.selectRucOrg(this, strOrg));
    parameters.put("Razon", ReportLibroInventariosYBalance12y13Data.selectSocialName(this, strOrg));
    // renderJR(vars, response, strReportName, null, "pdf", parameters, data, null);
    // temp
    renderJR(vars, response, strReportName, null, "pdf", parameters, temp, null);
    // renderJR(vars, response, strReportName, null, "pdf", parameters, data, null);
    // } finally {
    // if (data != null) {
    // data.close();

    // }

    // quitar
    // }
  }

  private void printPageDataXLS(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String strAmtFrom,
      String strAmtTo, String strcelementvaluefrom, String strcelementvalueto, String strOrg,
      String strcBpartnerId, String strmProductId, String strcProjectId, String strGroupBy,
      String strcAcctSchemaId, String strDescargarLibroElectronico) throws IOException,
      ServletException {
    log4j.debug("Output: XLS");

    response.setContentType("text/html; charset=UTF-8");
    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    String strOrgFamily = "";
    strOrgFamily = getFamily(strTreeOrg, strOrg);
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

    ReporteSaldosDelMesData data = null;
    try {
      data = ReporteSaldosDelMesData.selectXLS2(this, strAllaccounts, strcelementvaluefrom,
          strcelementvalueto,
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ReporteSaldosDelMes"),
          Utility.getContext(this, vars, "#User_Client", "ReporteSaldosDelMes"), strcAcctSchemaId,
          strDateFrom, toDatePlusOne, strOrgFamily, strcBpartnerId, strmProductId, strcProjectId,
          strAmtFrom, strAmtTo);

      if (!data.hasData()) {
        advisePopUp(request, response, "WARNING",
            Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
            Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
        return;
      }

      ScrollableFieldProvider limitedData = new LimitRowsScrollableFieldProviderFilter(data, 65532);

      String strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteSaldosDelMesExcel.jrxml";

      HashMap<String, Object> parameters = new HashMap<String, Object>();

      String strLanguage = vars.getLanguage();

      StringBuilder strSubTitle = new StringBuilder();
      strSubTitle.append(Utility.messageBD(this, "DateFrom", strLanguage) + ": " + strDateFrom
          + " - " + Utility.messageBD(this, "DateTo", strLanguage) + ": " + strDateTo + " (");
      strSubTitle.append(ReporteSaldosDelMesData.selectCompany(this, vars.getClient()) + " - ");
      strSubTitle.append(ReporteSaldosDelMesData.selectOrganization(this, strOrg) + ")");
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
    ReporteSaldosDelMesData subreport[] = new ReporteSaldosDelMesData[1];

    @Override
    public FieldProvider get() throws ServletException {

      FieldProvider data = input.get();

      ReporteSaldosDelMesData cur = (ReporteSaldosDelMesData) data;

      // adjust data as needed
      if (!strOld.equals(cur.groupbyid + cur.id)) {
        if ("".equals(cur.groupbyid)) {
          // The argument " " is used to simulate one value and put
          // the optional parameter--> AND
          // FACT_ACCT.C_PROJECT_ID IS NULL for example
          subreport = ReporteSaldosDelMesData.selectTotal2(conn, strcBpartnerId,
              (strGroupBy.equals("BPartner") ? " " : null), strmProductId,
              (strGroupBy.equals("Product") ? " " : null), strcProjectId,
              (strGroupBy.equals("Project") ? " " : null), strcAcctSchemaId, cur.id, "",
              strDateFrom, strOrgFamily);
        } else {
          subreport = ReporteSaldosDelMesData.selectTotal2(conn,
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

  private String getFamily(String strTree, String strChild) throws IOException, ServletException {
    return Tree.getMembers(this, strTree, strChild);
  }

  @Override
  public String getServletInfo() {
    return "Servlet ReporteSaldosDelMes. This Servlet was made by Pablo Sarobe";
  } // end of getServletInfo() method
}
