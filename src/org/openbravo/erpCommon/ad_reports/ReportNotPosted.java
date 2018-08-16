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
 * All portions are Copyright (C) 2001-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.ad_reports;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.accounting.SCO_Utils;

public class ReportNotPosted extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  // static Category log4j = Category.getInstance(ReportNotPosted.class);

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strAD_Org_ID = vars.getGlobalVariable("inpadOrgId", "ReportNotPosted|AD_Org_ID", "");
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReportNotPosted|DateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportNotPosted|DateTo", "");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strAD_Org_ID);
    } else if (vars.commandIn("FIND")) {
      String strAD_Org_ID = vars
          .getRequestGlobalVariable("inpadOrgId", "ReportNotPosted|AD_Org_ID");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom", "ReportNotPosted|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportNotPosted|DateTo");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strAD_Org_ID);
    } else if (vars.commandIn("GETTABID")) {
      String recordId = vars.getRequestGlobalVariable("recordId", "");
      String adTableId = vars.getRequestGlobalVariable("adTableId", "");
      String strIssotrx = vars.getRequestGlobalVariable("issotrx", "");
      boolean issotrx = false;
      if(strIssotrx != null && strIssotrx.compareTo("Y") == 0)
    	  issotrx = true;
      
      getJSONTab(response, vars, recordId, adTableId, issotrx);

    } else
      pageError(response);
  }

  private void getJSONTab(HttpServletResponse response, VariablesSecureApp vars, String recordId,
      String adTableId, boolean issotrx) throws IOException, ServletException {

    JSONObject msg = new JSONObject();
    try {
      msg.put("adTabId", SCO_Utils.getTabId(this, adTableId, recordId, issotrx));
      msg.put("recordId", recordId);
    } catch (JSONException e) {
      log4j.error("JSON object error" + msg.toString());
    }

    response.setContentType("application/json; charset=UTF-8");
    PrintWriter out = response.getWriter();

    out.println(msg.toString());
    out.close();

  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strAD_Org_ID) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    String discard[] = { "sectionDocType" };
    XmlDocument xmlDocument = null;
    ReportNotPostedData[] data = null;
    // if (strDateFrom.equals("") && strDateTo.equals("")) {
    // xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportNotPosted",
    // discard).createXmlDocument();
    // data = ReportNotPostedData.set();
    // } else {
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportNotPosted")
        .createXmlDocument();
    data = ReportNotPostedData.select(this, vars.getLanguage(), vars.getClient(), strAD_Org_ID,
        strDateFrom, strDateTo);
    // }// DateTimeData.nDaysAfter

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportNotPosted", false, "", "", "",
        false, "ad_reports", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();

    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_reports.ReportNotPosted");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportNotPosted.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportNotPosted.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportNotPosted");
      vars.removeMessage("ReportNotPosted");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    if (vars.commandIn("FIND") && data.length == 0) {
      // No data has been found. Show warning message.
      xmlDocument.setParameter("messageType", "WARNING");
      xmlDocument.setParameter("messageTitle",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()));
      xmlDocument.setParameter("messageMessage",
          Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

    xmlDocument.setParameter("adOrg", strAD_Org_ID);

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "",
          "0C754881EAD94243A161111916E9B9C6", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "NationalPurchasePlanningJR"), Utility.getContext(this, vars, "#User_Client",
              "NationalPurchasePlanningJR"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData,
          "NationalPurchasePlanningFilterJR", strAD_Org_ID);
      xmlDocument.setData("reportAD_Org_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setData("structure1", data);
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet ReportNotPosted. This Servlet was made by Juan Pablo Calvente";
  } // end of the getServletInfo() method
}
