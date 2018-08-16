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
 * All portions are Copyright (C) 2001-2010 Openbravo SLU 
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package pe.com.unifiedgo.report.ad_reports;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class ReportProductAvgMonthlySales extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static String strBDErrorMessage = "";

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    // HttpSession session = request.getSession(true);
    // @SuppressWarnings("rawtypes")
    // Enumeration e = session.getAttributeNames();
    // while (e.hasMoreElements()) {
    // String name = (String) e.nextElement();
    // System.out.println("name: " + name + " - value: " + session.getAttribute(name));
    // }

    if (vars.commandIn("DEFAULT")) {
      String mProductId = vars.getGlobalVariable("inpmProductId",
          "ReportProductAvgMonthlySales|M_Product_Id", "");
      String strDocDate = vars.getGlobalVariable("inpDocDate",
          "ReportProductAvgMonthlySales|docDate", SREDateTimeData.today(this));
      String strNumMonths = vars.getGlobalVariable("inpNumMonths",
          "ReportProductAvgMonthlySales|NumMonths", "");
      String strAD_Org_ID = vars.getGlobalVariable("inpadOrgId",
          "ReportProductAvgMonthlySales|AD_Org_ID", "");

      printPageDataSheet(request, response, vars, mProductId, strDocDate, strNumMonths,
          strAD_Org_ID);

    } else if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
      String mProductId = vars.getRequestGlobalVariable("inpmProductId",
          "ReportProductAvgMonthlySales|M_Product_Id");
      String strDocDate = vars.getStringParameter("inpDocDate");
      String strNumMonths = vars.getGlobalVariable("inpNumMonths",
          "ReportProductAvgMonthlySales|NumMonths", "");
      String strAD_Org_ID = vars.getStringParameter("inpadOrgId");

      printPageDataSheet(request, response, vars, mProductId, strDocDate, strNumMonths,
          strAD_Org_ID);

    } else if (vars.commandIn("XLS")) {
      String mProductId = vars.getRequestGlobalVariable("inpmProductId",
          "ReportProductAvgMonthlySales|M_Product_Id");
      String strDocDate = vars.getGlobalVariable("inpDocDate",
          "ReportProductAvgMonthlySales|docDate", SREDateTimeData.today(this));
      String strNumMonths = vars.getGlobalVariable("inpNumMonths",
          "ReportProductAvgMonthlySales|NumMonths", "");
      String strAD_Org_ID = vars.getGlobalVariable("inpadOrgId",
          "ReportProductAvgMonthlySales|AD_Org_ID", "");

      String reportData = vars.getStringParameter("inpReportData");
      String reportHeader = vars.getStringParameter("inpReportHeader");
      if (reportData == null || reportData.equals("")) {
        ReportProductAvgMonthlySales.strBDErrorMessage = "SRE_noselected";
        advisePopUp(
            request,
            response,
            "ERROR",
            Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
            Utility.messageBD(this, ReportProductAvgMonthlySales.strBDErrorMessage,
                vars.getLanguage()));
      } else {
        printPage(request, response, vars, mProductId, strDocDate, strNumMonths, reportData,
            reportHeader, strAD_Org_ID);
      }

    } else
      pageError(response);
  }

  private ReportProductAvgMonthlySalesData[] processReportData(String reportData) {
    ReportProductAvgMonthlySales.strBDErrorMessage = "";

    String[] reportDataColumns = reportData.split(";");

    ReportProductAvgMonthlySalesData objectReportProductAvgMonthlySalesData = new ReportProductAvgMonthlySalesData();

    objectReportProductAvgMonthlySalesData.productid = reportDataColumns[0];
    objectReportProductAvgMonthlySalesData.searchkey = reportDataColumns[1];
    objectReportProductAvgMonthlySalesData.internalcode = (reportDataColumns[2] != null) ? reportDataColumns[2]
        : "--";
    objectReportProductAvgMonthlySalesData.name = reportDataColumns[3];
    objectReportProductAvgMonthlySalesData.avgmonthlysales1 = ("--".equals(reportDataColumns[4])) ? "-1"
        : reportDataColumns[4];
    objectReportProductAvgMonthlySalesData.avgmonthlysales2 = ("--".equals(reportDataColumns[5])) ? "-1"
        : reportDataColumns[5];
    objectReportProductAvgMonthlySalesData.avgmonthlysales3 = ("--".equals(reportDataColumns[6])) ? "-1"
        : reportDataColumns[6];
    objectReportProductAvgMonthlySalesData.avgmonthlysales4 = ("--".equals(reportDataColumns[7])) ? "-1"
        : reportDataColumns[7];
    objectReportProductAvgMonthlySalesData.avgmonthlysales5 = ("--".equals(reportDataColumns[8])) ? "-1"
        : reportDataColumns[8];
    objectReportProductAvgMonthlySalesData.avgmonthlysales6 = ("--".equals(reportDataColumns[9])) ? "-1"
        : reportDataColumns[9];
    objectReportProductAvgMonthlySalesData.avgmonthlysales7 = ("--".equals(reportDataColumns[10])) ? "-1"
        : reportDataColumns[10];
    objectReportProductAvgMonthlySalesData.avgmonthlysales8 = ("--".equals(reportDataColumns[11])) ? "-1"
        : reportDataColumns[11];
    objectReportProductAvgMonthlySalesData.avgmonthlysales9 = ("--".equals(reportDataColumns[12])) ? "-1"
        : reportDataColumns[12];
    objectReportProductAvgMonthlySalesData.avgmonthlysales10 = ("--".equals(reportDataColumns[13])) ? "-1"
        : reportDataColumns[13];
    objectReportProductAvgMonthlySalesData.avgmonthlysales11 = ("--".equals(reportDataColumns[14])) ? "-1"
        : reportDataColumns[14];
    objectReportProductAvgMonthlySalesData.avgmonthlysales12 = ("--".equals(reportDataColumns[15])) ? "-1"
        : reportDataColumns[15];

    objectReportProductAvgMonthlySalesData.rownum = new Integer(0).toString();

    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    vector.addElement(objectReportProductAvgMonthlySalesData);

    ReportProductAvgMonthlySalesData objectReportProductAvgMonthlySalesDataArray[] = new ReportProductAvgMonthlySalesData[vector
        .size()];
    vector.copyInto(objectReportProductAvgMonthlySalesDataArray);

    return (objectReportProductAvgMonthlySalesDataArray);
  }

  private void printPage(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String mProductId, String strDocDate, String strNumMonths,
      String reportData, String reportHeader, String strAD_Org_ID) throws IOException,
      ServletException {
    response.setContentType("text/html; charset=UTF-8");

    ReportProductAvgMonthlySalesData[] data = null;
    try {
      data = processReportData(reportData);
    } catch (OBException e) {
      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "FINPR_NoConversionFound", vars.getLanguage()) + e.getMessage());
    }

    if (data != null && data.length == 1 && data[0] == null) {

      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "FINPR_NoDataFound", vars.getLanguage()));

    }

    String strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ProductAvgMonthlySalesExcel.jrxml";
    response.setHeader("Content-disposition", "inline; filename=ProductAvgMonthlySalesExcel.xls");

    String strSubTitle = "";
    strSubTitle = Utility.messageBD(this, "Document Date", vars.getLanguage()) + " " + strDocDate;

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("REPORT_SUBTITLE", strSubTitle);

    String[] reportHeaderMonth = reportHeader.split(";");

    parameters.put("DOCDATE", strDocDate);
    parameters.put("NUMMONTHS", strNumMonths);
    parameters.put("MONTH1", (!"".equals(reportHeaderMonth[0])) ? reportHeaderMonth[0] : "Mes 1");
    parameters.put("MONTH2", (!"".equals(reportHeaderMonth[1])) ? reportHeaderMonth[1] : "Mes 2");
    parameters.put("MONTH3", (!"".equals(reportHeaderMonth[2])) ? reportHeaderMonth[2] : "Mes 3");
    parameters.put("MONTH4", (!"".equals(reportHeaderMonth[3])) ? reportHeaderMonth[3] : "Mes 4");
    parameters.put("MONTH5", (!"".equals(reportHeaderMonth[4])) ? reportHeaderMonth[4] : "Mes 5");
    parameters.put("MONTH6", (!"".equals(reportHeaderMonth[5])) ? reportHeaderMonth[5] : "Mes 6");
    parameters.put("MONTH7", (!"".equals(reportHeaderMonth[6])) ? reportHeaderMonth[6] : "Mes 7");
    parameters.put("MONTH8", (!"".equals(reportHeaderMonth[7])) ? reportHeaderMonth[7] : "Mes 8");
    parameters.put("MONTH9", (!"".equals(reportHeaderMonth[8])) ? reportHeaderMonth[8] : "Mes 9");
    parameters.put("MONTH10", (!"".equals(reportHeaderMonth[9])) ? reportHeaderMonth[9] : "Mes 10");
    parameters.put("MONTH11", (!"".equals(reportHeaderMonth[10])) ? reportHeaderMonth[10]
        : "Mes 11");
    parameters.put("MONTH12", (!"".equals(reportHeaderMonth[11])) ? reportHeaderMonth[11]
        : "Mes 12");

    if (data != null) {
      renderJR(vars, response, strReportName, "xls", parameters, data, null);
    }

  }

  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String mProductId, String strDocDate, String strNumMonths,
      String strAD_Org_ID) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReportProductAvgMonthlySalesData[] data = null;
    String strConvRateErrorMsg = "";

    String discard[] = { "discard" };

    xmlDocument = xmlEngine.readXmlTemplate(
        "pe/com/unifiedgo/report/ad_reports/ReportProductAvgMonthlySales", discard)
        .createXmlDocument();

    if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
      OBError myMessage = null;
      myMessage = new OBError();
      try {
        // String ad_org_id = "05425129884E4EE9B80E006F3288C8F0";
        data = ReportProductAvgMonthlySalesData.select(vars.getSessionValue("#AD_CLIENT_ID"),
            strAD_Org_ID, mProductId, strDocDate, strNumMonths);

      } catch (ServletException ex) {
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
      }
      strConvRateErrorMsg = myMessage.getMessage();
      if (!strConvRateErrorMsg.equals("") && strConvRateErrorMsg != null) {
        advise(request, response, "ERROR",
            Utility.messageBD(this, "NoConversionRateHeader", vars.getLanguage()),
            strConvRateErrorMsg);
      } else { // Otherwise, the report is launched
        if (data == null || data.length == 0) {
          discard[0] = "selEliminar";
          data = ReportProductAvgMonthlySalesData.set();
        } else {
          xmlDocument.setData("structure1", data);
        }
      }
    }

    else {
      if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
        discard[0] = "selEliminar";
        data = ReportProductAvgMonthlySalesData.set();
      }
    }

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportProductAvgMonthlySales",
          false, "", "", "", false, "ad_reports", strReplaceWith, false, true);
      toolbar.setEmail(false);
      toolbar.prepareSimpleToolBarTemplate();
      toolbar.prepareRelationBarTemplate(false, false, "printXLS();return false;");
      xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "pe.com.unifiedgo.report.ad_reports.ReportProductAvgMonthlySales");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
            "ReportProductAvgMonthlySales.html", classInfo.id, classInfo.type, strReplaceWith,
            tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
            "ReportProductAvgMonthlySales.html", strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      {
        OBError myMessage = vars.getMessage("ReportProductAvgMonthlySales");
        vars.removeMessage("ReportProductAvgMonthlySales");
        if (myMessage != null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }

      xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("docDate", strDocDate);
      xmlDocument.setParameter("docDatedisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("docDatesaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

      
      
      xmlDocument.setParameter("mProduct", mProductId);
      xmlDocument.setParameter("productDescription",
          ReportProductAvgMonthlySalesData.selectMproduct(this, mProductId));

      
      
      
      
      xmlDocument.setParameter("adOrg", strAD_Org_ID);
      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "",
            "0C754881EAD94243A161111916E9B9C6", Utility.getContext(this, vars,
                "#AccessibleOrgTree", "ReportProductAvgMonthlySales"), Utility.getContext(this,
                vars, "#User_Client", "ReportProductAvgMonthlySales"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportProductAvgMonthlySales",
            strAD_Org_ID);
        xmlDocument.setData("reportAD_Org_ID", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
            "9A4C02741FAD4E5DAE696E0CBE279F9E", "", Utility.getContext(this, vars,
                "#AccessibleOrgTree", "ReportProductAvgMonthlySales"), Utility.getContext(this,
                vars, "#User_Client", "ReportProductAvgMonthlySales"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportProductAvgMonthlySales",
            "");
        xmlDocument.setData("reportNumMonths", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      xmlDocument.setParameter("NumMonths", "".equals(strNumMonths) ? "3" : strNumMonths);

      // Print document in the output
      out.println(xmlDocument.print());
      out.close();
    }
  }

  public String getServletInfo() {
    return "Servlet PurchaseOrderFilter. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method

}
