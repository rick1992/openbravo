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
package pe.com.unifiedgo.core.ad_reports;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

public class ReportBusquedaOCCustomerJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    // HttpSession session = request.getSession(true);
    // @SuppressWarnings("rawtypes")
    // Enumeration e = session.getAttributeNames();
    // while (e.hasMoreElements()) {
    // String name = (String) e.nextElement();
    // System.out.println("name: " + name + " - value: " +
    // session.getAttribute(name));
    // }

    if (vars.commandIn("DEFAULT")) {
      String strDocDate = vars.getGlobalVariable("inpDocDate", "ReportBusquedaOCCustomerJR|docDate", SREDateTimeData.today(this));
      String strProyDate = vars.getGlobalVariable("inpProyDate", "ReportBusquedaOCCustomerJR|proyDate", SREDateTimeData.tomorrow(this));
      String strAD_Org_ID = vars.getGlobalVariable("inpadOrgId", "ReportBusquedaOCCustomerJR|AD_Org_ID", "");
      String mProductId = vars.getGlobalVariable("inpmProductId", "ReportBusquedaOCCustomerJR|M_Product_Id", "");
      String strmProductLineID = vars.getStringParameter("inpProductLine");

      printPageDataSheet(request, response, vars, strAD_Org_ID, strDocDate, strProyDate, mProductId, strmProductLineID, true);

    } else if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {

      String strDocDate = vars.getStringParameter("inpDocDate");
      String strProyDate = vars.getStringParameter("inpProyDate");
      String strAD_Org_ID = vars.getStringParameter("inpadOrgId");
      String mProductId = vars.getRequestGlobalVariable("inpmProductId", "ReportBusquedaOCCustomerJR|M_Product_Id");
      String strmProductLineID = vars.getStringParameter("inpProductLine");

      printPageDataSheet(request, response, vars, strAD_Org_ID, strDocDate, strProyDate, mProductId, strmProductLineID, false);

    }
    if (vars.commandIn("PDF", "XLS")) {

      String strDocDate = vars.getStringParameter("inpDocDate");
      String strProyDate = vars.getStringParameter("inpProyDate");
      String strAD_Org_ID = vars.getStringParameter("inpadOrgId");
      String mProductId = vars.getRequestGlobalVariable("inpmProductId", "ReportBusquedaOCCustomerJR|M_Product_Id");
      String strmProductLineID = vars.getStringParameter("inpProductLine");

      printPagePDF(response, vars, strAD_Org_ID, strDocDate, strProyDate, mProductId, strmProductLineID);
    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response, VariablesSecureApp vars, String strAD_Org_ID, String strDocDate, String strProyDate, String mProductId, String strmProductLineID, boolean isFirstLoad) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReportBusquedaOCCustomerJRData[] data = null;
    String strConvRateErrorMsg = "";

    String discard[] = { "discard" };

    xmlDocument = xmlEngine.readXmlTemplate("pe/com/unifiedgo/core/ad_reports/ReportBusquedaOCCustomerFilterJR", discard).createXmlDocument();

    if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
      OBError myMessage = null;
      myMessage = new OBError();
      try {
        String language = vars.getLanguage();

        data = ReportBusquedaOCCustomerJRData.select_trl(vars.getSessionValue("#AD_CLIENT_ID"), strAD_Org_ID, mProductId, language, strDocDate, strProyDate, vars.getSessionValue("#AD_USER_ID"), strmProductLineID);

      } catch (ServletException ex) {
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
      }
      strConvRateErrorMsg = myMessage.getMessage();
      if (!strConvRateErrorMsg.equals("") && strConvRateErrorMsg != null) {
        advise(request, response, "ERROR", Utility.messageBD(this, "NoConversionRateHeader", vars.getLanguage()), strConvRateErrorMsg);
      } else { // Otherwise, the report is launched
        if (data == null || data.length == 0) {
          discard[0] = "selEliminar";
          data = ReportBusquedaOCCustomerJRData.set();
        } else {
          xmlDocument.setData("structure1", data);
        }
      }
    }

    else {
      if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
        discard[0] = "selEliminar";
        data = ReportBusquedaOCCustomerJRData.set();
      }
    }

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {

      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportBusquedaOCCustomerFilterJR", false, "", "", "imprimir();return false;", false, "ad_reports", strReplaceWith, false, true);
      toolbar.setEmail(false);

      toolbar.prepareSimpleToolBarTemplate();

      xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars, "pe.com.unifiedgo.core.ad_reports.ReportBusquedaOCCustomerJR");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportBusquedaOCCustomerFilterJR.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportBusquedaOCCustomerFilterJR.html", strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      {
        OBError myMessage = vars.getMessage("ReportBusquedaOCCustomerJR");
        vars.removeMessage("ReportBusquedaOCCustomerJR");
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
      xmlDocument.setParameter("proyDate", strProyDate);
      xmlDocument.setParameter("proyDatedisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("proyDatesaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

      if (isFirstLoad) {
        xmlDocument.setParameter("FirstLoad", "FirstLoad = \"YES\"; ");
      } else {
        xmlDocument.setParameter("FirstLoad", "FirstLoad = \"NO\"; ");
      }

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "", "D4DF252DEC3B44858454EE5292A8B836", Utility.getContext(this, vars, "#AccessibleOrgTree", "BusquedaOCCustomer"), Utility.getContext(this, vars, "#User_Client", "BusquedaOCCustomerJR"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "BusquedaOCCustomerFilterJR", strAD_Org_ID);
        xmlDocument.setData("reportAD_Org_ID", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      xmlDocument.setParameter("adOrg", strAD_Org_ID);

      xmlDocument.setParameter("ProductLine", strmProductLineID);
      xmlDocument.setParameter("productLineDescription", ReportBusquedaOCCustomerJRData.selectPrdcProductgroup(this, strmProductLineID));

      xmlDocument.setParameter("mProduct", mProductId);
      xmlDocument.setParameter("productDescription", ReportBusquedaOCCustomerJRData.selectMproduct(this, mProductId));

      // Print document in the output
      out.println(xmlDocument.print());
      out.close();
    }
  }

  private void printPagePDF(HttpServletResponse response, VariablesSecureApp vars, String strOrg, String strDateFrom, String strDateTo, String mProductId, String strmProductLineID) throws IOException, ServletException {

    ReportBusquedaOCCustomerJRData[] data = null;

    String language = vars.getLanguage();
    data = ReportBusquedaOCCustomerJRData.select_trl(vars.getSessionValue("#AD_CLIENT_ID"), strOrg, mProductId, language, strDateFrom, strDateTo, vars.getSessionValue("#AD_USER_ID"), strmProductLineID);
    String strReportName = "@basedesign@/pe/com/unifiedgo/core/ad_reports/ReporteBusquedaOCCustomer.jrxml";

    HashMap<String, Object> parameters = new HashMap<String, Object>();

    parameters.put("dateFrom", StringToDate(strDateFrom));
    parameters.put("dateTo", StringToDate(strDateTo));
    renderJR(vars, response, strReportName, "Reporte_Busqueda_OC_Cliente", "xls", parameters, data, null);

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

  public String getServletInfo() {
    return "Servlet PurchaseOrderFilter. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method

}
