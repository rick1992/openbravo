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
import org.openbravo.erpCommon.businessUtility.Tree;
import org.openbravo.erpCommon.businessUtility.TreeData;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class ReporteCargaInventario extends HttpSecureAppServlet {
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
      String strDocDate = vars.getGlobalVariable("inpDocDate", "ReporteCargaInventario|docDate",
          SREDateTimeData.today(this));
      String strProyDate = vars.getGlobalVariable("inpProyDate", "ReporteCargaInventario|proyDate",
          SREDateTimeData.tomorrow(this));
      String strAD_Org_ID = vars.getGlobalVariable("inpadOrgId",
          "ReporteCargaInventario|AD_Org_ID", "0");

      printPageDataSheet(request, response, vars, strDocDate, strProyDate, strAD_Org_ID, true);
    } else if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {

      String strDocDate = vars.getStringParameter("inpDocDate");
      String strProyDate = vars.getStringParameter("inpProyDate");
      String strAD_Org_ID = vars.getStringParameter("inpadOrgId");

      printPageDataSheet(request, response, vars, strDocDate, strProyDate, strAD_Org_ID, false);
    } else if (vars.commandIn("PDF", "XLS")) {// aqui para imprimir como pdf o excel
      if (log4j.isDebugEnabled())
        log4j.debug("PDF");
      String strDocDate = vars.getRequestGlobalVariable("inpDocDate",
          "ReporteCargaInventario|docDate");
      String strProyDate = vars.getRequestGlobalVariable("inpProyDate",
          "ReporteCargaInventario|proyDate");
      String strAD_Org_ID = vars.getGlobalVariable("inpadOrgId",
          "ReporteCargaInventario|AD_Org_ID", "0");

      setHistoryCommand(request, "DEFAULT");
      printPagePDF(request, response, vars, strDocDate, strProyDate, strAD_Org_ID);
    } else {
      pageError(response);
    }
  }

  private void printPagePDF(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDocDate, String strProyDate, String strAD_Org_ID)
      throws IOException, ServletException {

    ReporteCargaInventarioData[] data = null;

    try {

      data = ReporteCargaInventarioData.selectSaldos(strAD_Org_ID,
          vars.getSessionValue("#AD_CLIENT_ID"), strDocDate, strProyDate,
          vars.getSessionValue("#AD_USER_ID"));

    } catch (OBException e) {
      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "FINPR_NoConversionFound", vars.getLanguage()) + e.getMessage());
    }
    for (int i = 0; i < data.length; i++) {
      data[i].fecha = strProyDate;
    }

    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());


    String strSubtitle = (Utility.messageBD(this, "LegalEntity", vars.getLanguage()) + ": ")
        + CustomerAccountsReceivableData.selectCompany(this, vars.getClient()) + "\n" + "RUC:"
        + CustomerAccountsReceivableData.selectRucOrg(this, strAD_Org_ID) + "\n";
    ;

    if (!("0".equals(strAD_Org_ID)))
      strSubtitle += (Utility.messageBD(this, "OBUIAPP_Organization", vars.getLanguage()) + ": ")
          + CustomerAccountsReceivableData.selectOrg(this, strAD_Org_ID) + "\n";

    if (!"".equals(strDocDate) || !"".equals(strProyDate))
      strSubtitle += (Utility.messageBD(this, "From", vars.getLanguage()) + ": ") + strDocDate
          + "  " + (Utility.messageBD(this, "OBUIAPP_To", vars.getLanguage()) + ": ") + strProyDate
          + "\n";

    String strOutput;
    String strReportName;
    if (vars.commandIn("PDF")) {
      strOutput = "pdf";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteCargaInventario.jrxml";
    } else {
      strOutput = "xls";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteCargaInventarioExcel.jrxml";
    }
    HashMap<String, Object> parameters = new HashMap<String, Object>();

    parameters.put("organizacion", ReporteCargaInventarioData.selectSocialName(this, strAD_Org_ID));

    parameters.put("dateFrom", StringToDate(strDocDate));
    parameters.put("dateTo", StringToDate(strProyDate));

    Organization org = OBDal.getInstance().get(Organization.class, strAD_Org_ID);

    String city = "";
    String region = org.getOrganizationInformationList().get(0).getLocationAddress().getRegion()
        .getName();
    if (region.toUpperCase().equals("LIMA"))
      city = "LIMA";
    else
      city = org.getOrganizationInformationList().get(0).getLocationAddress().getCityName()
          .toUpperCase();

    String pattern = "ddMMyyyy";
    SimpleDateFormat formatFecha = new SimpleDateFormat(pattern);
    String fecha = formatFecha.format(new Date());

    pattern = "HHmmss";
    SimpleDateFormat formatHora = new SimpleDateFormat(pattern);
    String hora = formatHora.format(new Date());

    String name = "001B1_INVENTARIO_PE" + org.getOrganizationInformationList().get(0).getTaxID()
        + "_" + city + "_" + fecha + "_" + hora;

    renderJR(vars, response, strReportName, name, strOutput, parameters, data, null);
  }

  private boolean isNumeric(String str) {
    try {
      double d = Double.parseDouble(str);
    } catch (NumberFormatException nfe) {
      return false;
    }
    return true;
  }

  private String getFamily(String strTree, String strChild) throws IOException, ServletException {
    return Tree.getMembers(this, strTree, (strChild == null || strChild.equals("")) ? "0"
        : strChild);
  }

  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDocDate, String strProyDate, String strAD_Org_ID,
      boolean isFirstLoad) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReporteCargaInventarioData[] data = null;
    String strConvRateErrorMsg = "";

    String discard[] = { "discard" };

    xmlDocument = xmlEngine.readXmlTemplate(
        "pe/com/unifiedgo/report/ad_reports/ReporteCargaInventario", discard).createXmlDocument();

    if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
      OBError myMessage = null;
      myMessage = new OBError();
      try {
        data = ReporteCargaInventarioData.select(strAD_Org_ID,
            vars.getSessionValue("#AD_CLIENT_ID"), strDocDate, strProyDate,
            vars.getSessionValue("#AD_USER_ID"));

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
          data = ReporteCargaInventarioData.set();
        } else {
          xmlDocument.setData("structure1", data);
        }
      }
    }

    else {
      if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
        discard[0] = "selEliminar";
        data = ReporteCargaInventarioData.set();
      }
    }

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {

      // ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReporteCargaInventario", false,
      // "",
      // "", "imprimir();return false;", false, "ad_reports", strReplaceWith, false, true);
      // toolbar.prepareSimpleToolBarTemplate();
      //
      //
      //

      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReporteCargaInventario", false, "",
          "", "imprimir();return false;", false, "ad_reports", strReplaceWith, false, true);

      toolbar.setEmail(false);
      toolbar.prepareSimpleToolBarTemplate();
      toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");

      xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "pe.com.unifiedgo.report.ad_reports.ReporteCargaInventario");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
            "RankingClientesFilterJR.html", classInfo.id, classInfo.type, strReplaceWith,
            tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
            "RankingClientesFilterJR.html", strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      {
        OBError myMessage = vars.getMessage("ReporteCargaInventario");
        vars.removeMessage("ReporteCargaInventario");
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

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "",
            "388B0167BE3A4B8A8C67B0234B9DA866", Utility.getContext(this, vars,
                "#AccessibleOrgTree", "ReporteCargaInventario"), Utility.getContext(this, vars,
                "#User_Client", "ReporteCargaInventario"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReporteCargaInventario",
            strAD_Org_ID);
        xmlDocument.setData("reportAD_Org_ID", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      if (isFirstLoad) {
        xmlDocument.setParameter("FirstLoad", "FirstLoad = \"YES\"; ");
      } else {
        xmlDocument.setParameter("FirstLoad", "FirstLoad = \"NO\"; ");
      }

      xmlDocument.setParameter("adOrg", strAD_Org_ID);

      // Print document in the output
      out.println(xmlDocument.print());
      out.close();
    }
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
