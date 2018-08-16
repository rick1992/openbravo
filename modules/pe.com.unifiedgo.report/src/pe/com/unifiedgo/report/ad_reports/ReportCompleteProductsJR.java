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
import org.openbravo.erpCommon.businessUtility.Tree;
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

public class ReportCompleteProductsJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strStartDate = vars.getGlobalVariable("inpStartDate",
          "ReportCompleteProductsJR|startDate", SREDateTimeData.today(this));
      String strEndDate = vars.getGlobalVariable("inpEndDate", "ReportCompleteProductsJR|endDate",
          SREDateTimeData.tomorrow(this));
      String strAD_Org_ID = vars.getGlobalVariable("inpadOrgId",
          "ReportCompleteProductsJR|AD_Org_ID", "");

      printPageDataSheet(request, response, vars, strStartDate, strEndDate, strAD_Org_ID);
    } else if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
      String strStartDate = vars.getStringParameter("inpStartDate");
      String strEndDate = vars.getStringParameter("inpEndDate");
      String strAD_Org_ID = vars.getStringParameter("inpadOrgId");

      printPageDataSheet(request, response, vars, strStartDate, strEndDate, strAD_Org_ID);

    } else if (vars.commandIn("PDF", "XLS")) {
      String strStartDate = vars.getGlobalVariable("inpStartDate",
          "ReportCompleteProductsJR|startDate", SREDateTimeData.today(this));
      String strEndDate = vars.getGlobalVariable("inpEndDate", "ReportCompleteProductsJR|endDate",
          SREDateTimeData.tomorrow(this));
      String strAD_Org_ID = vars.getGlobalVariable("inpadOrgId",
          "ReportCompleteProductsJR|AD_Org_ID", "");

      printPage(request, response, vars, strStartDate, strEndDate, strAD_Org_ID);

    } else
      pageError(response);
  }

  private void printPage(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strStartDate, String strEndDate, String strAD_Org_ID)
      throws IOException, ServletException {

    ReportCompleteProductsJRData[] data = null;
    ReportCompleteProductsJRData[] dataSaldo = null;
    
	String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
	String strOrgFamily = getFamily(strTreeOrg, strAD_Org_ID);


    try {
      data = ReportCompleteProductsJRData.select(strOrgFamily,
          vars.getSessionValue("#AD_CLIENT_ID"), strStartDate, strEndDate);
      
      dataSaldo=ReportCompleteProductsJRData.selectSaldo(strOrgFamily,
              vars.getSessionValue("#AD_CLIENT_ID"), strStartDate, strEndDate);
      
    } catch (OBException e) {
      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "FINPR_NoConversionFound", vars.getLanguage()) + e.getMessage());
    }

    if ( data.length == 0) {

      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "FINPR_NoDataFound", vars.getLanguage()));

    }
    
    for(int i=0;i<data.length;i++){
    	data[i].COS_INI=dataSaldo[0].COS_INI;
    }

    String strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/Rpt_Producto_Terminado_ugo.jrxml";
    // response.setHeader("Content-disposition",
    // "inline; filename=ReporteProductoTerminadoExcel.xls");

    String strSubTitle = "";
    strSubTitle = Utility.messageBD(this, "Start Date", vars.getLanguage()) + " " + strStartDate
        + " " + Utility.messageBD(this, "End Date", vars.getLanguage()) + " " + strEndDate;

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("REPORT_SUBTITLE", strSubTitle);
    
    parameters.put("ORGANIZACION",ReportCompleteProductsJRData.selectNombreOrganizacion(this, strAD_Org_ID));


    parameters.put("INICIO", StringToDate(strStartDate));
    parameters.put("FIN", StringToDate(strEndDate));
    parameters.put("ORG_ID", strAD_Org_ID);

    if (data != null) {
      renderJR(vars, response, strReportName, "ReporteProductosTerminados", "pdf", parameters,
          data, null);
    }

  }
  
	private String getFamily(String strTree, String strChild)
			throws IOException, ServletException {
		return Tree.getMembers(this, strTree,
				(strChild == null || strChild.equals("")) ? "0" : strChild);
	}
  

  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strStartDate, String strEndDate, String strAD_Org_ID)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReportCompleteProductsJRData[] data = null;
    String strConvRateErrorMsg = "";

    String discard[] = { "discard" };

    if (data == null || data.length == 0) {
      xmlDocument = xmlEngine.readXmlTemplate(
          "pe/com/unifiedgo/report/ad_reports/ReportCompleteProductsFilterJR", discard)
          .createXmlDocument();
      data = ReportCompleteProductsJRData.set();
      data[0].rownum = "0";
    } 

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportCompleteProductsFilterJR",
        false, "", "", "printCompleteProductsPDF();return false;", false, "ad_reports",
        strReplaceWith, false, true);
    toolbar.setEmail(false);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "pe.com.unifiedgo.report.ad_reports.ReportCompleteProductsJR");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ReportCompleteProductsFilterJR.html", classInfo.id, classInfo.type, strReplaceWith,
          tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
          "ReportCompleteProductsFilterJR.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportCompleteProductsJR");
      vars.removeMessage("ReportCompleteProductsJR");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("startDate", strStartDate);
    xmlDocument.setParameter("startDatedisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("startDatesaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("endDate", strEndDate);
    xmlDocument.setParameter("endDatedisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("endDatesaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "",
          "D4DF252DEC3B44858454EE5292A8B836", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "CompleteProductsJR"), Utility.getContext(this, vars, "#User_Client",
              "CompleteProductsJR"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "CompleteProductsFilterJR",
          strAD_Org_ID);
      xmlDocument.setData("reportAD_Org_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    
    
    xmlDocument.setParameter("paramPeriodosArray", Utility.arrayInfinitasEntradas("idperiodo;periodo;fechainicial;fechafinal;idorganizacion","arrPeriodos",
  			ReportPurchasingRecordsData
			.select_periodos(this)));


    xmlDocument.setParameter("adOrg", strAD_Org_ID);

    // Print document in the output
    xmlDocument.setData("structure1", data);
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

  public String getServletInfo() {
    return "Servlet CompleteProductsFilter. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method

}
