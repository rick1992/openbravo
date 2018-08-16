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

public class ReporteClientesInfocorpVencido extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static String strBDErrorMessage = "";

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strOrg = vars.getGlobalVariable("inpOrg", "ReporteClientesInfocorpVencido|Org", "");
      String strDocDate = vars.getGlobalVariable("inpDocDate",
          "ReporteClientesInfocorpVencido|docDate", "");
      String strDateto = vars.getGlobalVariable("inpDateTo",
          "ReporteClientesInfocorpVencido|dateTo", SREDateTimeData.today(this));

      printPageDataSheet(request, response, vars, strOrg, strDocDate, strDateto, "");

    } else if (vars.commandIn("LISTAR")) {

      String strDocDate = vars.getStringParameter("inpDocDate");
      String strDateTo = vars.getStringParameter("inpDateTo");
      String strOrg = vars.getStringParameter("inpadOrgId");

      printPageDataSheet(request, response, vars, strOrg, strDocDate, strDateTo, "");

    } else if (vars.commandIn("PDF", "XLS")) {

      String strOrg = vars.getStringParameter("inpadOrgId");
      String strDocDate = vars.getStringParameter("inpDocDate");
      String strDateto = vars.getStringParameter("inpDateTo");

      setHistoryCommand(request, "DEFAULT");

      if (vars.commandIn("PDF")) {
        printPagePDF(request, response, vars, strOrg, strDocDate, strDateto, "");
      }
      if (vars.commandIn("XLS")) {
        printPageXLS(request, response, vars, strDocDate, strDateto, "", "", strOrg);
      }
    } else
      pageError(response);
  }

  private void printPagePDF(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strOrg, String strDocDate, String strDateto,
      String strBPartner) throws IOException, ServletException {

    OBError myMessage = null;
    myMessage = new OBError();

    ReporteClientesInfocorpVencidoData[] dataOrg = null;

    ReporteClientesInfocorpVencidoData[] data = null;


    Date fecha = null;

    try {
      dataOrg = ReporteClientesInfocorpVencidoData.selectOrg(this,
          vars.getSessionValue("#AD_CLIENT_ID"), strOrg);

    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
      dataOrg = ReporteClientesInfocorpVencidoData.set();
    }

    try {
      data = ReporteClientesInfocorpVencidoData.select(this, vars.getSessionValue("#AD_CLIENT_ID"),
          strOrg, strDocDate, strDateto, strBPartner);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }
    
    String orgPadre="Todos";
    String orgPadreRuc="--";
    if(dataOrg.length>0)
    {
        orgPadre=dataOrg[0].orgpadre;
        orgPadreRuc=dataOrg[0].orgpadreruc;
    }

    String strReportName;
    String strOutput = "pdf";
    strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteClientesInfocorpVencido.jrxml";

    HashMap<String, Object> parameters = new HashMap<String, Object>();

    parameters.put("ORGANIZACION", orgPadre);
    parameters.put("RUC_ORGANIZACION", orgPadreRuc);

    renderJR(vars, response, strReportName, "Reporte_Clientes_Infocorp_Vencido", strOutput,
        parameters, data, null);
  }

  private void printPageXLS(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDocDate, String strDateto, String strWarehouse,
      String mProductId, String strOrg) throws IOException, ServletException {

    ReporteClientesInfocorpVencidoData[] dataOrg = null;
    ReporteClientesInfocorpVencidoData[] data = null;

    Date fecha = null;

    OBError myMessage = null;
    myMessage = new OBError();

    try {
      dataOrg = ReporteClientesInfocorpVencidoData.selectOrg(this,
          vars.getSessionValue("#AD_CLIENT_ID"), strOrg);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
      dataOrg = ReporteClientesInfocorpVencidoData.set();
    }

    try {
      data = ReporteClientesInfocorpVencidoData.select(this, vars.getSessionValue("#AD_CLIENT_ID"),
          strOrg, strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }
    
    String orgPadre="Todos";
    String orgPadreRuc="--";
    if(dataOrg.length>0)
    {
        orgPadre=dataOrg[0].orgpadre;
        orgPadreRuc=dataOrg[0].orgpadreruc;
    }

    String strOutput = "xls";
    String strReportName;

    strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteClientesInfocorpVencidoExcel.jrxml";

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("ORGANIZACION", orgPadre);
    parameters.put("RUC_ORGANIZACION", orgPadreRuc);

    // data.length;
    // validar posibles null al convertir string a BigDecimal

    SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");

    renderJR(vars, response, strReportName, "Reporte_Clientes_Infocorp_Vencido", strOutput,
        parameters, data, null);

  }

  //
  //
  //
  //
  //
  //

  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strOrg, String strDateFrom, String strDateTo,
      String strBPartner) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    ReporteClientesInfocorpVencidoData[] data = null;

    XmlDocument xmlDocument = null;

    String discard[] = { "discard" };

    xmlDocument = xmlEngine.readXmlTemplate(
        "pe/com/unifiedgo/report/ad_reports/ReporteClientesInfocorpVencido", discard)
        .createXmlDocument();

    //
    //
    //
    //
    //

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();

    String strConvRateErrorMsg = "";

    if (vars.commandIn("LISTAR")) {

      OBError myMessage = null;
      myMessage = new OBError();
      try {

        data = ReporteClientesInfocorpVencidoData.select(this,
            vars.getSessionValue("#AD_CLIENT_ID"), strOrg, strDateFrom, strDateTo, strBPartner);

      } catch (ServletException ex) {
        data = ReporteClientesInfocorpVencidoData.set();
      }

      for (int i = 0; i < data.length; i++) {
        System.out.println(data[i].bpartner);
        System.out.println(data[i].fecha);
        System.out.println(data[i].fechalimite);
        System.out.println(data[i].organizacion);
      }

      xmlDocument.setData("structure2", data);
    }

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReporteClientesInfocorpVencido",
          false, "", "", "imprimirPDF();return false;", false, "ad_reports", strReplaceWith, false,
          true);
       toolbar.setEmail(false);
       toolbar.prepareSimpleToolBarTemplate();
      toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");
      xmlDocument.setParameter("toolbar", toolbar.toString());
      
      toolbar.setEmail(false);
      toolbar.prepareSimpleToolBarTemplate();
      

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "pe.com.unifiedgo.report.ad_reports.ReporteClientesInfocorpVencido");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
            "ReporteClientesInfocorpVencido.html", classInfo.id, classInfo.type, strReplaceWith,
            tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
            "ReporteClientesInfocorpVencido.html", strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
            "", Utility.getContext(this, vars, "#AccessibleOrgTree",
                "ReporteClientesInfocorpVencido"), Utility.getContext(this, vars, "#User_Client",
                "ReporteClientesInfocorpVencido"), 0);

        Utility.fillSQLParameters(this, vars, null, comboTableData,
            "ReporteClientesInfocorpVencido", strOrg);

        comboTableData.fillParameters(null, "ReporteSaldosDelMes", "");
        xmlDocument.setData("reportAD_Org_ID", "liststructure", comboTableData.select(false));

      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("docDate", strDateFrom);
      xmlDocument.setParameter("docDateto", strDateTo);
      xmlDocument.setParameter("docDatedisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("docDatesaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

      // xmlDocument.setParameter("SearchKey", strSearchKey);

      xmlDocument.setParameter("mProduct", strBPartner);

      // xmlDocument.setParameter("adOrgId", strOrg);

      xmlDocument.setParameter("mWarehouseId", strBPartner);
      // xmlDocument.setParameter("mWarehouseDescription",
      // ReporteClientesInfocorpVencidoData.selectMWarehouse(this, strWarehouse));

      xmlDocument.setParameter("adOrgId", strOrg);

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
            "9A4C02741FAD4E5DAE696E0CBE279F9E", "", Utility.getContext(this, vars,
                "#AccessibleOrgTree", "ReporteClientesInfocorpVencido"), Utility.getContext(this,
                vars, "#User_Client", "ReporteClientesInfocorpVencido"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData,
            "ReporteClientesInfocorpVencido", "");
        xmlDocument.setData("reportNumMonths", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      // xmlDocument.setParameter("NumMonths", "".equals(strNumMonths) ? "1" : strNumMonths);
      // Print document in the output
      out.println(xmlDocument.print());
      out.close();
    }
  }

  public String getServletInfo() {
    return "Servlet PurchaseOrderFilter. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method

}
