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
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.TreeData;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class RankingVendedoresJR extends HttpSecureAppServlet {
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

    String strUserCurrencyId = Utility.stringBaseCurrencyId(this, vars.getClient());
    if (vars.commandIn("DEFAULT")) {
      String strDocDate = vars.getGlobalVariable("inpDocDate", "RankingVendedoresJR|docDate",
          SREDateTimeData.today(this));
      String strProyDate = vars.getGlobalVariable("inpProyDate", "RankingVendedoresJR|proyDate",
          SREDateTimeData.tomorrow(this));
      String strAD_Org_ID = vars.getGlobalVariable("inpOrgId", "RankingVendedoresJR|OrgId", "");

      vars.removeSessionValue("RankingVendedoresJR|rankvendedoresdata");

      printPageDataSheet(request, response, vars, strDocDate, strProyDate, strAD_Org_ID, true, null);

    } else if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
      String strAD_Org_ID = vars.getStringParameter("inpOrgId");
      String strDocDate = vars.getStringParameter("inpDocDate");
      String strProyDate = vars.getStringParameter("inpProyDate");

      RankingVendedoresJRData[] data = RankingVendedoresJRData.select(vars, strAD_Org_ID,
          vars.getSessionValue("#AD_CLIENT_ID"), strDocDate, strProyDate,
          vars.getSessionValue("#AD_USER_ID"));
      vars.setSessionObject("RankingVendedoresJR|rankvendedoresdata", data);

      printPageDataSheet(request, response, vars, strDocDate, strProyDate, strAD_Org_ID, false,
          data);

    } else if (vars.commandIn("PDF", "XLS")) {// aqui para imprimir como pdf o excel
      if (log4j.isDebugEnabled())
        log4j.debug("PDF");
      String strAD_Org_ID = vars.getStringParameter("inpOrgId");
      String strDocDate = vars.getGlobalVariable("inpDocDate", "RankingVendedoresJR|docDate");
      String strProyDate = vars.getGlobalVariable("inpProyDate", "RankingVendedoresJR|proyDate");
      String strCurrencyId = "308";

      setHistoryCommand(request, "DEFAULT");

      RankingVendedoresJRData[] data = (RankingVendedoresJRData[]) vars
          .getSessionObject("RankingVendedoresJR|rankvendedoresdata");

      if (vars.commandIn("XLS")) {
        printPageXLS(request, response, vars, strDocDate, strProyDate, strAD_Org_ID, strCurrencyId,
            data);
      }

    } else
      pageError(response);
  }

  private void printPageXLS(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDocDate, String strProyDate, String strAD_Org_ID,
      String strCurrencyId, RankingVendedoresJRData[] objData) throws IOException, ServletException {

    RankingVendedoresJRData[] data = null;
    try {
      if (objData != null) {
        data = objData;
      } else {
        data = RankingVendedoresJRData.select(vars, strAD_Org_ID,
            vars.getSessionValue("#AD_CLIENT_ID"), strDocDate, strProyDate,
            vars.getSessionValue("#AD_USER_ID"));
      }

    } catch (OBException e) {
      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "FINPR_NoConversionFound", vars.getLanguage()) + e.getMessage());
    }

    for (int i = 0; i < data.length; i++) {
      String monto_soles = data[i].totalpenlinenetamt.replace(",", "");
      String monto_dolares = data[i].totalusdlinenetamt.replace(",", "");

      data[i].totalpenlinenetamt = new BigDecimal(monto_soles).toString();
      data[i].totalusdlinenetamt = new BigDecimal(monto_dolares).toString();
    }

    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    String strSubtitle = (Utility.messageBD(this, "LegalEntity", vars.getLanguage()) + ": ")
        + CustomerAccountsReceivableData.selectCompany(this, vars.getClient()) + "\n" + "RUC:"
        + CustomerAccountsReceivableData.selectRucOrg(this, strAD_Org_ID) + "\n";

    if (!("0".equals(strAD_Org_ID)))
      strSubtitle += (Utility.messageBD(this, "OBUIAPP_Organization", vars.getLanguage()) + ": ")
          + CustomerAccountsReceivableData.selectOrg(this, strAD_Org_ID) + "\n";

    String strOutput = "xls";
    String strReportName;
    strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/RankingVendedoresExcel.jrxml";

    HashMap<String, Object> parameters = new HashMap<String, Object>();

    parameters.put("ORGANIZACION", RankingVendedoresJRData.selectOrganization(this, strAD_Org_ID));
    parameters.put("MONEDA", RankingVendedoresJRData.selectCurrency(this, strCurrencyId));
    parameters.put("DESDE", StringToDate(strDocDate));
    parameters.put("HASTA", StringToDate(strProyDate));

    renderJR(vars, response, strReportName, "Reporte_Ranking_Vendedores", strOutput, parameters,
        data, null);
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

  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDocDate, String strProyDate, String strAD_Org_ID,
      boolean isFirstLoad, RankingVendedoresJRData[] objData) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    RankingVendedoresJRData[] data = null;
    String strConvRateErrorMsg = "";

    String discard[] = { "discard" };

    xmlDocument = xmlEngine.readXmlTemplate(
        "pe/com/unifiedgo/report/ad_reports/RankingVendedoresFilterJR", discard)
        .createXmlDocument();

    if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
      OBError myMessage = null;
      myMessage = new OBError();
      try {
        if (objData != null) {
          data = objData;
        } else {
          data = RankingVendedoresJRData.select(vars, strAD_Org_ID,
              vars.getSessionValue("#AD_CLIENT_ID"), strDocDate, strProyDate,
              vars.getSessionValue("#AD_USER_ID"));
        }

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
          data = RankingVendedoresJRData.set();
        } else {
          xmlDocument.setData("structure1", data);
        }
      }
    }

    else {
      if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
        discard[0] = "selEliminar";
        data = RankingVendedoresJRData.set();
      }
    }

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {

      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "RankingVendedoresFilterJR", false,
          "", "", "", false, "ad_reports", strReplaceWith, false, true);
      toolbar.setEmail(false);
      toolbar.prepareSimpleToolBarTemplate();
      toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");
      xmlDocument.setParameter("toolbar", toolbar.toString());

      // ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "RankingVendedoresFilterJR", false,
      // "", "", "", false, "ad_reports", strReplaceWith, false, true);
      // toolbar.prepareSimpleToolBarTemplate();
      // xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "pe.com.unifiedgo.report.ad_reports.RankingVendedoresJR");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
            "RankingVendedoresFilterJR.html", classInfo.id, classInfo.type, strReplaceWith,
            tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
            "RankingVendedoresFilterJR.html", strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      {
        OBError myMessage = vars.getMessage("RankingVendedoresJR");
        vars.removeMessage("RankingVendedoresJR");
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

      Organization org;
      org = OBDal.getInstance().get(Organization.class, strAD_Org_ID);
      xmlDocument.setParameter("OrgId", strAD_Org_ID);
      xmlDocument.setParameter("OrgDescription", (org != null) ? org.getIdentifier() : "");

      if (isFirstLoad) {
        xmlDocument.setParameter("FirstLoad", "FirstLoad = \"YES\"; ");
      } else {
        xmlDocument.setParameter("FirstLoad", "FirstLoad = \"NO\"; ");
      }

      // xmlDocument.setParameter("ccurrencyid", strCurrencyId);
      // try {
      // ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "", "112",
      // "554D913FDE194DEEACA68104E38D10BE", Utility.getContext(this, vars,
      // "#AccessibleOrgTree", "RankingClientesJR"), Utility.getContext(this, vars,
      // "#User_Client", "RankingClientesJR"), 0);
      // Utility.fillSQLParameters(this, vars, null, comboTableData, "RankingVendedoresJR",
      // strCurrencyId);
      // xmlDocument.setData("reportC_Currency_ID", "liststructure", comboTableData.select(false));
      // comboTableData = null;
      // } catch (Exception ex) {
      // throw new ServletException(ex);
      // }

      xmlDocument.setParameter("adOrg", strAD_Org_ID);

      // Print document in the output
      out.println(xmlDocument.print());
      out.close();
    }
  }

  public String getServletInfo() {
    return "Servlet PurchaseOrderFilter. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method

}
