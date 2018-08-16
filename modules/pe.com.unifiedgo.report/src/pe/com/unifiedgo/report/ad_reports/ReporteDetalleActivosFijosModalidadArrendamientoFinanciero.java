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
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class ReporteDetalleActivosFijosModalidadArrendamientoFinanciero extends
    HttpSecureAppServlet {
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
      /**/
      String strAD_Org_ID = vars.getGlobalVariable("inpOrgId",
          "ReporteDetalleActivosFijosModalidadArrendamientoFinanciero|OrgId", "");
      String strDateFrom = vars.getStringParameter("inpDocDate",
          SREDateTimeData.FirstDayOfMonth(this));
      String strDateTo = vars.getStringParameter("inpProyDate", SREDateTimeData.today(this));
      /**/

      printPageDataSheet(request, response, vars, strAD_Org_ID, strDateFrom, strDateTo);
    } else if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
      /**/
      String strAD_Org_ID = vars.getStringParameter("inpOrgId");
      String strDateFrom = vars.getStringParameter("inpDocDate",
          SREDateTimeData.FirstDayOfMonth(this));
      String strDateTo = vars.getStringParameter("inpProyDate", SREDateTimeData.today(this));
      /**/

      printPageDataSheet(request, response, vars, strAD_Org_ID, strDateFrom, strDateTo);
    } else if (vars.commandIn("PDF", "XLS")) {// aqui para imprimir como excel
      if (log4j.isDebugEnabled())
        log4j.debug("PDF");

      /**/
      String strAD_Org_ID = vars.getStringParameter("inpOrgId");
      String strDateFrom = vars.getStringParameter("inpDocDate");
      String strDateTo = vars.getStringParameter("inpProyDate");

      /**/

      setHistoryCommand(request, "DEFAULT");

      // if (vars.commandIn("XLS")) {
      printPageXLS(request, response, vars, strAD_Org_ID, strDateFrom, strDateTo);
      // }

    } else {
      pageError(response);
    }
  }

  private void printPageXLS(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strAD_Org_ID, String strDateFrom, String strDateTo)
      throws IOException, ServletException {

    String strOutput;
    String strReportName;

    ReporteDetalleActivosFijosModalidadArrendamientoFinancieroData[] data = null;

    try {
      data = ReporteDetalleActivosFijosModalidadArrendamientoFinancieroData.selectData(this,
          strAD_Org_ID, strDateTo);
    } catch (OBException e) {
      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "FINPR_NoConversionFound", vars.getLanguage()) + e.getMessage());
    }

    if (data.length == 0) {
      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
      return;
    }

    strOutput = "pdf";
    strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteDetalleActivosFijosModalidadArrendamientoFinanciero.jrxml";

    if (vars.commandIn("XLS")) {
      strOutput = "xls";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteDetalleActivosFijosModalidadArrendamientoFinancieroExcel.jrxml";
    }

    HashMap<String, Object> parameters = new HashMap<String, Object>();

    /* parameters */
    parameters.put("Razon", ReporteDetalleActivosFijosModalidadArrendamientoFinancieroData
        .selectOrg(this, strAD_Org_ID));
    parameters.put("Ruc", ReporteDetalleActivosFijosModalidadArrendamientoFinancieroData.selectRuc(
        this, strAD_Org_ID));

    parameters.put("dateFrom", StringToDate(strDateFrom));
    parameters.put("dateTo", StringToDate(strDateTo));

    renderJR(vars, response, strReportName,
        "Reporte_Detalle_Activos_Fijos_Modalidad_Arrendamiento_Financiero", strOutput, parameters,
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

  private boolean isNumeric(String str) {
    try {
      double d = Double.parseDouble(str);
    } catch (NumberFormatException nfe) {
      return false;
    }
    return true;
  }

  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strAD_Org_ID, String strDocDate, String strProyDate)
      throws IOException, ServletException {

    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReporteStockConsolidadoData[] data = null;

    ReporteControlFondosFijosData[] dataItems = null;

    String strConvRateErrorMsg = "";

    String discard[] = { "discard" };

    xmlDocument = xmlEngine
        .readXmlTemplate(
            "pe/com/unifiedgo/report/ad_reports/ReporteDetalleActivosFijosModalidadArrendamientoFinanciero",
            discard).createXmlDocument();

    if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
      OBError myMessage = null;
      myMessage = new OBError();
      try {
        data = ReporteStockConsolidadoData.select(strAD_Org_ID,
            vars.getSessionValue("#AD_CLIENT_ID"), "", "");

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
          data = ReporteStockConsolidadoData.set();
        } else {
          xmlDocument.setData("structure1", data);
        }
      }
    }

    else {
      if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
        discard[0] = "selEliminar";
        data = ReporteStockConsolidadoData.set();
      }
    }

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {

      ToolBar toolbar = new ToolBar(this, vars.getLanguage(),
          "ReporteDetalleActivosFijosModalidadArrendamientoFinanciero", false, "", "",
          "imprimirPDF();return false;", false, "ad_reports", strReplaceWith, false, true);
      toolbar.setEmail(false);
      toolbar.prepareSimpleToolBarTemplate();
      toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");
      xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "pe.com.unifiedgo.report.ad_reports.ReporteDetalleActivosFijosModalidadArrendamientoFinanciero");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
            "ReporteDetalleActivosFijosModalidadArrendamientoFinanciero.html", classInfo.id,
            classInfo.type, strReplaceWith, tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
            "ReporteDetalleActivosFijosModalidadArrendamientoFinanciero.html", strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      /* FIN Llenar controles */

      {
        OBError myMessage = vars
            .getMessage("ReporteDetalleActivosFijosModalidadArrendamientoFinanciero");
        vars.removeMessage("ReporteDetalleActivosFijosModalidadArrendamientoFinanciero");
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

      if (true) {
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

  public String getServletInfo() {
    return "Servlet PurchaseOrderFilter. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method

}
