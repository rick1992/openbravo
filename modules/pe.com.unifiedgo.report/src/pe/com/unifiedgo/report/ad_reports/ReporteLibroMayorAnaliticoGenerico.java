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
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
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
import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class ReporteLibroMayorAnaliticoGenerico extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strcAcctSchemaId = vars.getGlobalVariable("inpcAcctSchemaId",
          "ReporteLibroMayorAnaliticoGenerico|cAcctSchemaId", "");
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReporteLibroMayorAnaliticoGenerico|DateFrom",
          "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReporteLibroMayorAnaliticoGenerico|DateTo", "");
      String strPageNo = vars.getGlobalVariable("inpPageNo", "ReporteLibroMayorAnaliticoGenerico|PageNo", "1");
      String strAmtFrom = vars.getNumericGlobalVariable("inpAmtFrom", "ReporteLibroMayorAnaliticoGenerico|AmtFrom",
          "");
      String strAmtTo = vars.getNumericGlobalVariable("inpAmtTo", "ReporteLibroMayorAnaliticoGenerico|AmtTo", "");
      String strcelementvaluefrom = vars.getGlobalVariable("inpcElementValueIdFrom",
          "ReporteLibroMayorAnaliticoGenerico|C_ElementValue_IDFROM", "");
      String strcelementvalueto = vars.getGlobalVariable("inpcElementValueIdTo",
          "ReporteLibroMayorAnaliticoGenerico|C_ElementValue_IDTO", "");
      String strcelementvaluefromdes = "", strcelementvaluetodes = "";
      if (!strcelementvaluefrom.equals(""))
        strcelementvaluefromdes = ReporteLibroMayorAnaliticoGenericoData.selectSubaccountDescription(this,
            strcelementvaluefrom);
      if (!strcelementvalueto.equals(""))
        strcelementvaluetodes = ReporteLibroMayorAnaliticoGenericoData.selectSubaccountDescription(this,
            strcelementvalueto);
      strcelementvaluefromdes = (strcelementvaluefromdes.equals("null")) ? ""
          : strcelementvaluefromdes;
      strcelementvaluetodes = (strcelementvaluetodes.equals("null")) ? "" : strcelementvaluetodes;
      vars.setSessionValue("inpElementValueIdFrom_DES", strcelementvaluefromdes);
      vars.setSessionValue("inpElementValueIdTo_DES", strcelementvaluetodes);
      String strOrg = vars.getGlobalVariable("inpOrg", "ReporteLibroMayorAnaliticoGenerico|Org", "0");
      String strcBpartnerId = vars.getInGlobalVariable("inpcBPartnerId_IN",
          "ReporteLibroMayorAnaliticoGenerico|cBpartnerId", "", IsIDFilter.instance);
      String strmProductId = vars.getInGlobalVariable("inpmProductId_IN",
          "ReporteLibroMayorAnaliticoGenerico|mProductId", "", IsIDFilter.instance);
      String strcProjectId = vars.getInGlobalVariable("inpcProjectId_IN",
          "ReporteLibroMayorAnaliticoGenerico|cProjectId", "", IsIDFilter.instance);
      String strDescargarLibroElectronico = vars.getGlobalVariable("inpDescargarLibroElectronico",
          "ReporteLibroMayorAnaliticoGenerico|DescargarLibroElectronico", "");

      String strGroupBy = "";
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strPageNo, strAmtFrom, strAmtTo,
          strcelementvaluefrom, strcelementvalueto, strOrg, strcBpartnerId, strmProductId,
          strcProjectId, strGroupBy, strcAcctSchemaId, strcelementvaluefromdes,
          strcelementvaluetodes, strDescargarLibroElectronico);
    } else if (vars.commandIn("PREVIOUS_RELATION")) {
      String strInitRecord = vars.getSessionValue("ReporteLibroMayorAnaliticoGenerico.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRange", "ReporteLibroMayorAnaliticoGenerico");
      int intRecordRange = strRecordRange.equals("") ? 0 : Integer.parseInt(strRecordRange);
      if (strInitRecord.equals("") || strInitRecord.equals("0"))
        vars.setSessionValue("ReporteLibroMayorAnaliticoGenerico.initRecordNumber", "0");
      else {
        int initRecord = (strInitRecord.equals("") ? 0 : Integer.parseInt(strInitRecord));
        initRecord -= intRecordRange;
        strInitRecord = ((initRecord < 0) ? "0" : Integer.toString(initRecord));
        vars.setSessionValue("ReporteLibroMayorAnaliticoGenerico.initRecordNumber", strInitRecord);
      }
      response.sendRedirect(getDireccion() + request.getServletPath());
    } else if (vars.commandIn("NEXT_RELATION")) {
      String strInitRecord = vars.getSessionValue("ReporteLibroMayorAnaliticoGenerico.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRange", "ReporteLibroMayorAnaliticoGenerico");
      int intRecordRange = strRecordRange.equals("") ? 0 : Integer.parseInt(strRecordRange);
      int initRecord = (strInitRecord.equals("") ? 0 : Integer.parseInt(strInitRecord));
      
      initRecord += intRecordRange;
      strInitRecord = ((initRecord < 0) ? "0" : Integer.toString(initRecord));
      vars.setSessionValue("ReporteLibroMayorAnaliticoGenerico.initRecordNumber", strInitRecord);
      response.sendRedirect(getDireccion() + request.getServletPath());
    } else if (vars.commandIn("PDF", "XLS")) {
    	
    	String strOrg = vars.getStringParameter("inpOrg");
    	String strDateFrom = vars.getStringParameter("inpDateFrom");
    	String strDateTo = vars.getStringParameter("inpDateTo");
    	String strcelementvaluefrom = vars.getStringParameter("inpcElementValueIdFrom");

      printPageDataPDF_ugo(request, response, vars, strDateFrom, strDateTo, strcelementvaluefrom,  strOrg, 
           "", "", "");

      
    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strPageNo, String strAmtFrom, String strAmtTo,
      String strcelementvaluefrom, String strcelementvalueto, String strOrg, String strcBpartnerId,
      String strmProductId, String strcProjectId, String strGroupBy, String strcAcctSchemaId,
      String strcelementvaluefromdes, String strcelementvaluetodes,
      String strDescargarLibroElectronico) throws IOException, ServletException {

    String strRecordRange = Utility.getContext(this, vars, "#RecordRange", "ReporteLibroMayorAnaliticoGenerico");
    int intRecordRange = (strRecordRange.equals("") ? 0 : Integer.parseInt(strRecordRange));
    String strInitRecord = vars.getSessionValue("ReporteLibroMayorAnaliticoGenerico.initRecordNumber");
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
    ReporteLibroMayorAnaliticoGenericoData[] data = null;
    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    String strOrgFamily = "";
    String toDatePlusOne = DateTimeData.nDaysAfter(this, strDateTo, "1");

    String strGroupByText = (strGroupBy.equals("BPartner")
        ? Utility.messageBD(this, "BusPartner", vars.getLanguage())
        : (strGroupBy.equals("Product") ? Utility.messageBD(this, "Product", vars.getLanguage())
            : (strGroupBy.equals("Project") ? Utility.messageBD(this, "Project", vars.getLanguage())
                : "")));

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReporteLibroMayorAnaliticoGenerico", true, "", "",
        "imprimir();return false;", false, "ad_reports", strReplaceWith, false, true);

    toolbar.prepareSimpleToolBarTemplate();
    toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");

    String strcBpartnerIdAux = strcBpartnerId;
    String strmProductIdAux = strmProductId;
    String strcProjectIdAux = strcProjectId;
    if (strDateFrom.equals("") && strDateTo.equals("")) {
      String discard[] = { "sectionAmount", "sectionPartner" };
      xmlDocument = xmlEngine
          .readXmlTemplate("pe/com/unifiedgo/report/ad_reports/ReporteLibroMayorAnaliticoGenerico", discard)
          .createXmlDocument();
      // toolbar
      // .prepareRelationBarTemplate(false, false,
      // "submitCommandForm('XLS', false, frmMain, 'ReporteLibroMayorAnaliticoGenericoExcel.xls', 'EXCEL');return
      // false;");
      data = ReporteLibroMayorAnaliticoGenericoData.set();
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
          strcelementvaluetodes = ReporteLibroMayorAnaliticoGenericoData.selectSubaccountDescription(this,
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
      

      log4j.debug("Select2. Time in mils: " + (System.currentTimeMillis() - initMainSelect));
      log4j.debug("RecordNo: " + initRecordNumber);

      ReporteLibroMayorAnaliticoGenericoData[] dataTotal = null;
      ReporteLibroMayorAnaliticoGenericoData[] dataSubtotal = null;
      String strOld = "";
      // boolean firstPagBlock = false;
      ReporteLibroMayorAnaliticoGenericoData[] subreportElement = new ReporteLibroMayorAnaliticoGenericoData[1];
      for (int i = 0; data != null && i < data.length; i++) {
        if (!strOld.equals(data[i].groupbyid + data[i].id)) {
          subreportElement = new ReporteLibroMayorAnaliticoGenericoData[1];
          // firstPagBlock = false;
          if (i == 0 && initRecordNumber > 0) {
            // firstPagBlock = true;
            Long init = System.currentTimeMillis();
            dataTotal = ReporteLibroMayorAnaliticoGenericoData.select2Total(this, rowNum, strGroupByText,
                strGroupBy, strAllaccounts, strcelementvaluefrom, strcelementvalueto,
                Utility.getContext(this, vars, "#AccessibleOrgTree", "ReporteLibroMayorAnaliticoGenerico"),
                Utility.getContext(this, vars, "#User_Client", "ReporteLibroMayorAnaliticoGenerico"),
                strcAcctSchemaId, "", DateTimeData.nDaysAfter(this, data[0].dateacct, "1"),
                strOrgFamily, strcBpartnerId, strmProductId, strcProjectId, strAmtFrom, strAmtTo,
                data[0].id, data[0].groupbyid, null, null, null, data[0].dateacctnumber
                    + data[0].factaccttype + data[0].factAcctGroupId + data[0].factAcctId);
            dataSubtotal = ReporteLibroMayorAnaliticoGenericoData.select2sum(this, rowNum, strGroupByText,
                strGroupBy, strAllaccounts, strcelementvaluefrom, strcelementvalueto,
                Utility.getContext(this, vars, "#AccessibleOrgTree", "ReporteLibroMayorAnaliticoGenerico"),
                Utility.getContext(this, vars, "#User_Client", "ReporteLibroMayorAnaliticoGenerico"),
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
            subreportElement = new ReporteLibroMayorAnaliticoGenericoData[1];
            subreportElement[0] = new ReporteLibroMayorAnaliticoGenericoData();
            subreportElement[0].totalacctdr = previousDebit.toPlainString();
            subreportElement[0].totalacctcr = previousCredit.toPlainString();
            data[0].amtacctdrprevsum = (dataSubtotal != null) ? dataSubtotal[0].amtacctdr
                : data[0].amtacctdrprevsum;
            data[0].amtacctcrprevsum = (dataSubtotal != null) ? dataSubtotal[0].amtacctcr
                : data[0].amtacctcrprevsum;
            subreportElement[0].total = previousDebit.subtract(previousCredit).toPlainString();
          } else {
            if ("".equals(data[i].groupbyid)) {
              Long init = System.currentTimeMillis();
              subreportElement = ReporteLibroMayorAnaliticoGenericoData.selectTotal2(this, strcBpartnerId,
                  (strGroupBy.equals("BPartner") ? " " : null), strmProductId,
                  (strGroupBy.equals("Product") ? " " : null), strcProjectId,
                  (strGroupBy.equals("Project") ? " " : null), strcAcctSchemaId, data[i].id, "",
                  strDateFrom, strOrgFamily);
              log4j.debug("SelectTotalNew. Time in mils: " + (System.currentTimeMillis() - init));
            } else {
              Long init = System.currentTimeMillis();
              subreportElement = ReporteLibroMayorAnaliticoGenericoData.selectTotal2(this,
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
      subreportElement = new ReporteLibroMayorAnaliticoGenericoData[1];
      for (int i = 0; data != null && i < data.length; i++) {
        if (!strTotal.equals(data[i].groupbyid + data[i].id)) {
          subreportElement = new ReporteLibroMayorAnaliticoGenericoData[1];
          if ("".equals(data[i].groupbyid)) {
            
            Long init = System.currentTimeMillis();
            subreportElement = ReporteLibroMayorAnaliticoGenericoData.selectTotal2(this, strcBpartnerId,
                (strGroupBy.equals("BPartner") ? " " : null), strmProductId,
                (strGroupBy.equals("Product") ? " " : null), strcProjectId,
                (strGroupBy.equals("Project") ? " " : null), strcAcctSchemaId, data[i].id, "",
                toDatePlusOne, strOrgFamily);
            log4j.debug("SelectTotal2. Time in mils: " + (System.currentTimeMillis() - init));
          } else {
            Long init = System.currentTimeMillis();
            subreportElement = ReporteLibroMayorAnaliticoGenericoData
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
      
      xmlDocument = xmlEngine
          .readXmlTemplate("pe/com/unifiedgo/report/ad_reports/ReporteLibroMayorAnaliticoGenerico", discard)
          .createXmlDocument();
    }
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "pe.com.unifiedgo.report.ad_reports.ReporteLibroMayorAnaliticoGenerico");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReporteLibroMayorAnaliticoGenerico.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReporteLibroMayorAnaliticoGenerico.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReporteLibroMayorAnaliticoGenerico");
      vars.removeMessage("ReporteLibroMayorAnaliticoGenerico");
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
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ReporteLibroMayorAnaliticoGenerico"),
          Utility.getContext(this, vars, "#User_Client", "ReporteLibroMayorAnaliticoGenerico"), '*');
      comboTableData.fillParameters(null, "ReporteLibroMayorAnaliticoGenerico", "");
      xmlDocument.setData("reportAD_ORGID", "liststructure", comboTableData.select(false));

      ReporteLibroMayorAnaliticoGenericoData[] datax = ReporteLibroMayorAnaliticoGenericoData.select_periodos(this);
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

      // para cargar la variables javascrip de periodos
      xmlDocument.setParameter("paramPeriodosArray",
          Utility.arrayInfinitasEntradas("idperiodo;periodo;fechainicial;fechafinal;idorganizacion",
              "arrPeriodos", ReporteLibroMayorAnaliticoGenericoData.select_periodos(this)));

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

    ReporteLibroMayorAnaliticoGenericoData[] datalibros = ReporteLibroMayorAnaliticoGenericoData.select_libros(this);
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
    
    xmlDocument.setParameter("paramLibrosArray",
            Utility.arrayInfinitasEntradas("idlibro;nombrelibro;idorganizacion", "arrLibroMayor",
                ReportGeneralLedgerData.select_libros(this)));


    if (data != null && data.length > 0) {
      if (strGroupBy.equals(""))
        xmlDocument.setData("structure1", data);
      else
        xmlDocument.setData("structure2", data);
    }

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
      VariablesSecureApp vars, String strDateFrom, String strDateTo,  String strcelementvaluefrom,  String strOrg,
      String strcAcctSchemaId, String strMostrarSaldos, String strShowAll) throws IOException, ServletException {
    log4j.debug("Output: PDF");
    
    String datefrom = strDateFrom;
    
    BigDecimal saldoinicial = BigDecimal.ZERO;
    BigDecimal saldo = BigDecimal.ZERO;
    
    ReporteLibroMayorAnaliticoGenericoData datainicial[] = null;
    ReporteLibroMayorAnaliticoGenericoData datareport[] = null;
    
    String startyear = "01-01-" + strDateTo.substring(6, 10);
    String endyear = "31-12-" + strDateTo.substring(6, 10);
    String nextmonth = SREDateTimeData.nDaysAfter(this, startyear, "31");
    
    String strtype = "";
    
    if(strDateFrom.equalsIgnoreCase(startyear)){
    	strtype="'O'";
    	strDateFrom = nextmonth;
    }else{
    	strtype="'O','N'";
    }
    
    datainicial = ReporteLibroMayorAnaliticoGenericoData.selectsaldoinicial(this, strOrg, strcelementvaluefrom, startyear, strDateFrom, strtype);
    
    datareport = ReporteLibroMayorAnaliticoGenericoData.selectdata(this, strOrg, strcelementvaluefrom, datefrom, strDateTo);
    
    if (datainicial.length == 0 && datareport.length == 0) {
        advisePopUp(request, response, "WARNING",
            Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
            Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
        return;
      }
    
    if(datainicial.length > 0){
    	saldoinicial = new BigDecimal(datainicial[0].saldo);
    	saldo = new BigDecimal(datainicial[0].saldo);
    }else{
    	if(datainicial[0].saldo != null && !datainicial[0].saldo.equals("")){
    		saldoinicial = new BigDecimal(datainicial[0].saldo);
    		saldo = new BigDecimal(datainicial[0].saldo);
    	}
    }
    
    for(int i=0;i<datareport.length;i++){
    	ReporteLibroMayorAnaliticoGenericoData dd = datareport[i];
    	
    	saldo = saldo.add(new BigDecimal(dd.debe)).subtract(new BigDecimal(dd.haber));
    	
    	dd.saldo = saldo.toString();
    }
    
    String strOutput;
    String strReportName;
    
    if(vars.commandIn("PDF")){
    	strOutput = "pdf";
        strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteLibroMayorAnaliticoGenerico.jrxml";
    }else{
    	strOutput = "xls";
        strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteLibroMayorAnaliticoGenericoExcel.jrxml";
    }
    
      HashMap<String, Object> parameters = new HashMap<String, Object>();

      String strLanguage = vars.getLanguage();

      parameters.put("ShowGrouping", new Boolean(!"".equals("")));
      StringBuilder strSubTitle = new StringBuilder();
      strSubTitle.append(Utility.messageBD(this, "DateFrom", strLanguage) + ": " + strDateFrom
          + " - " + Utility.messageBD(this, "DateTo", strLanguage) + ": " + strDateTo + " (");
      strSubTitle.append(ReporteLibroMayorAnaliticoGenericoData.selectCompany(this, vars.getClient()) + " - ");
      strSubTitle.append(ReporteLibroMayorAnaliticoGenericoData.selectOrganization(this, strOrg) + ")");
      parameters.put("REPORT_SUBTITLE", strSubTitle.toString());
      parameters.put("Previous", Utility.messageBD(this, "Initial Balance", strLanguage));
      parameters.put("Total", Utility.messageBD(this, "Total", strLanguage));
      
      parameters.put("Periodo", Utility.messageBD(this, "Del", strLanguage) + ": " + strDateFrom
          + " - " + Utility.messageBD(this, "al", strLanguage) + ": " + strDateTo);
      parameters.put("Ruc", ReporteLibroMayorAnaliticoGenericoData.selectRUC(this, strOrg));
      parameters.put("Razon_social", ReporteLibroMayorAnaliticoGenericoData.selectSocialName(this, strOrg));
      String strDateFormat;
      strDateFormat = vars.getJavaDateFormat();
      parameters.put("strDateFormat", strDateFormat);
      parameters.put("DATE_FROM", strDateFrom);
      parameters.put("DATE_TO", strDateTo);
      parameters.put("MONTO_INICIAL", saldoinicial);

      parameters.put("dateFrom", StringToDate(strDateFrom));
      parameters.put("dateTo", StringToDate(strDateTo));
      parameters.put("account", ReporteLibroMayorAnaliticoGenericoData.selectcuenta(this,strcelementvaluefrom));

      renderJR(vars, response, strReportName, "Libro_Mayor_Analitico_Generico", strOutput, parameters, datareport, null);
  }

  @Override
  public String getServletInfo() {
    return "Servlet ReporteLibroMayorAnaliticoGenerico. This Servlet was made by Pablo Sarobe";
  } // end of getServletInfo() method
}
