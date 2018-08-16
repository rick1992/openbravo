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
import org.openbravo.erpCommon.businessUtility.TreeData;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class ReporteComprasPorProveedor extends HttpSecureAppServlet {
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
      String strDocDate = vars.getGlobalVariable("inpDocDate",
          "ReporteComprasPorProveedor|docDate", SREDateTimeData.today(this));
      String strProyDate = vars.getGlobalVariable("inpProyDate",
          "ReporteComprasPorProveedor|proyDate", SREDateTimeData.tomorrow(this));
      String strAD_Org_ID = vars.getGlobalVariable("inpadOrgId",
          "ReporteComprasPorProveedor|AD_Org_ID", "");
      String strcBpartnetId = vars.getGlobalVariable("inpcBPartnerId",
          "ReporteComprasPorProveedor|CB_PARTNER_ID", "");

      printPageDataSheet(request, response, vars, strDocDate, strProyDate, strcBpartnetId,
          strAD_Org_ID, true);
    } else if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {

      String strDocDate = vars.getStringParameter("inpDocDate");
      String strProyDate = vars.getStringParameter("inpProyDate");
      String strAD_Org_ID = vars.getStringParameter("inpadOrgId");
      String strcBpartnetId = vars.getStringParameter("inpcBPartnerId");

      printPageDataSheet(request, response, vars, strDocDate, strProyDate, strcBpartnetId,
          strAD_Org_ID, false);
    } else

    if (vars.commandIn("PDF", "XLS")) {
      if (log4j.isDebugEnabled())
        log4j.debug("PDF");

      String strDocDate = vars.getRequestGlobalVariable("inpDocDate",
          "ReporteComprasPorProveedor|docDate");
      String strProyDate = vars.getRequestGlobalVariable("inpProyDate",
          "ReporteComprasPorProveedor|proyDate");
      String strAD_Org_ID = vars.getGlobalVariable("inpadOrgId",
          "ReporteComprasPorProveedor|AD_Org_ID", "0");
      String strcBpartnetId = vars.getStringParameter("inpcBPartnerId");

      setHistoryCommand(request, "DEFAULT");

      System.out.println("LOL");

      if (vars.commandIn("PDF")) {
        printPagePDF(request, response, vars, strDocDate, strProyDate, strcBpartnetId, strAD_Org_ID);
      } else {
        printPageXLS(request,response, vars, strDocDate, strProyDate, strcBpartnetId, strAD_Org_ID);
      }

      System.out.println("XD");

    } else {
      pageError(response);
    }
  }

  private void printPagePDF(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDocDate, String strProyDate, String strC_BPartner_ID,
      String strAD_Org_ID) throws IOException, ServletException {

    ReporteComprasPorProveedorData[] data = null;

    try {
      data = ReporteComprasPorProveedorData.select(strAD_Org_ID,
          vars.getSessionValue("#AD_CLIENT_ID"), strDocDate, strProyDate, strC_BPartner_ID,
          vars.getSessionValue("#AD_USER_ID"));
    } catch (OBException e) {
      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "FINPR_NoConversionFound", vars.getLanguage()) + e.getMessage());
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
    String strReportName = "";
    if (vars.commandIn("PDF")) {
      strOutput = "pdf";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteComprasPorProveedor.jrxml";
    } else {
      strOutput = "xls";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteComprasPorProveedorExcel.jrxml";
    }

    HashMap<String, Object> parameters = new HashMap<String, Object>();

    parameters.put("ORGANIZACION",
        ReporteComprasPorProveedorData.selectOrganization(this, strAD_Org_ID));
    parameters.put("RUC_ORGANIZACION",
        ReporteComprasPorProveedorData.selectRucOrganization(this, strAD_Org_ID));
    parameters
        .put("TERCERO", ReporteComprasPorProveedorData.selectBPartner(this, strC_BPartner_ID));
    parameters.put("RUC_TERCERO",
        ReporteComprasPorProveedorData.selectRucBPartner(this, strC_BPartner_ID));
    parameters.put("DESDE", StringToDate(strDocDate));
    parameters.put("HASTA", StringToDate(strProyDate));

    renderJR(vars, response, strReportName, "Reporte_Compras_por_Proveedor", strOutput, parameters,
        data, null);

  }

  private void printPageXLS(HttpServletRequest request,HttpServletResponse response, VariablesSecureApp vars,
      String strDocDate, String strProyDate, String strC_BPartner_ID, String strAD_Org_ID)
      throws IOException, ServletException {

	    ReporteComprasPorProveedorData[] data = null;

	    try {
	      data = ReporteComprasPorProveedorData.select(strAD_Org_ID,
	          vars.getSessionValue("#AD_CLIENT_ID"), strDocDate, strProyDate, strC_BPartner_ID,
	          vars.getSessionValue("#AD_USER_ID"));
	    } catch (OBException e) {
	      advisePopUp(request, response, "WARNING",
	          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
	          Utility.messageBD(this, "FINPR_NoConversionFound", vars.getLanguage()) + e.getMessage());
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
	    String strReportName = "";
	    if (vars.commandIn("PDF")) {
	      strOutput = "pdf";
	      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteComprasPorProveedor.jrxml";
	    } else {
	      strOutput = "xls";
	      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteComprasPorProveedorExcel.jrxml";
	    }

	    HashMap<String, Object> parameters = new HashMap<String, Object>();

	    parameters.put("ORGANIZACION",
	        ReporteComprasPorProveedorData.selectOrganization(this, strAD_Org_ID));
	    parameters.put("RUC_ORGANIZACION",
	        ReporteComprasPorProveedorData.selectRucOrganization(this, strAD_Org_ID));
	    parameters
	        .put("TERCERO", ReporteComprasPorProveedorData.selectBPartner(this, strC_BPartner_ID));
	    parameters.put("RUC_TERCERO",
	        ReporteComprasPorProveedorData.selectRucBPartner(this, strC_BPartner_ID));
	    parameters.put("DESDE", StringToDate(strDocDate));
	    parameters.put("HASTA", StringToDate(strProyDate));

	    renderJR(vars, response, strReportName, "Reporte_Compras_por_Proveedor", strOutput, parameters,
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
      VariablesSecureApp vars, String strDocDate, String strProyDate, String strC_BPartner_ID,
      String strAD_Org_ID, boolean isFirstLoad) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReporteComprasPorProveedorData[] data = null;
    String strConvRateErrorMsg = "";

    String discard[] = { "discard" };

    xmlDocument = xmlEngine.readXmlTemplate(
        "pe/com/unifiedgo/report/ad_reports/ReporteComprasPorProveedor", discard)
        .createXmlDocument();

    if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
      OBError myMessage = null;
      myMessage = new OBError();
      try {
        data = ReporteComprasPorProveedorData.select(strAD_Org_ID,
            vars.getSessionValue("#AD_CLIENT_ID"), strDocDate, strProyDate, strC_BPartner_ID,
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
          data = ReporteComprasPorProveedorData.set();
        } else {
          xmlDocument.setData("structure1", data);
        }
      }
    }

    else {
      if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
        discard[0] = "selEliminar";
        data = ReporteComprasPorProveedorData.set();
      }
    }

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {

		ToolBar toolbar = new ToolBar(this, vars.getLanguage(),
				"ReporteComprasPorProveedor", false, "", "",
				"imprimirPDF();return false;", false, "ad_reports",
				strReplaceWith, false, true);

		toolbar.setEmail(false);
		toolbar.prepareSimpleToolBarTemplate();
		toolbar.prepareRelationBarTemplate(false, false,
				"imprimirXLS();return false;");
		
      xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "pe.com.unifiedgo.report.ad_reports.ReporteComprasPorProveedor"); 
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
            "ReporteComprasPorProveedor.html", classInfo.id, classInfo.type, strReplaceWith,
            tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
            "ReporteComprasPorProveedor.html", strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      {
        OBError myMessage = vars.getMessage("ReporteComprasPorProveedor");
        vars.removeMessage("ReporteComprasPorProveedor");
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
      xmlDocument.setParameter("paramBPartnerId", strC_BPartner_ID);
      
		
	  xmlDocument.setParameter("paramBPartnerId", strC_BPartner_ID);
	    xmlDocument.setParameter("paramBPartnerDescription", ReporteComprasPorProveedorData
				.selectBPartner(this, strC_BPartner_ID));

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "",
            "0C754881EAD94243A161111916E9B9C6", Utility.getContext(this, vars,
                "#AccessibleOrgTree", "ReporteComprasPorProveedor"), Utility.getContext(this, vars,
                "#User_Client", "ReporteComprasPorProveedor"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReporteComprasPorProveedor",
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

  public String getServletInfo() {
    return "Servlet PurchaseOrderFilter. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method

}
