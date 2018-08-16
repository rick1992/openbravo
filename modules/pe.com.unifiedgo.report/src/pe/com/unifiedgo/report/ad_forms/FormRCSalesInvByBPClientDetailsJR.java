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
package pe.com.unifiedgo.report.ad_forms;

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
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class FormRCSalesInvByBPClientDetailsJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static String strBDErrorMessage = "";

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    String strAD_Org_ID = "", strSalesRepID = "", strBPClientId = "", strDateFrom = "", strDateTo = "";

    if (vars.commandIn("DEFAULT")) {
      strAD_Org_ID = vars.getStringParameter("inpadOrgId");
      strSalesRepID = vars.getStringParameter("inpsalesRepId");
      strBPClientId = vars.getStringParameter("inpBPClientId");
      strDateFrom = vars.getStringParameter("inpdateFrom");
      strDateTo = vars.getStringParameter("inpdateTo");

      vars.removeSessionValue("FormRCSalesInvByBPClientDetailsJR|rankclntsalesinvdetailsdata");
      FormRCSalesInvByBPClientDetailsJRData[] data = FormRCSalesInvByBPClientDetailsJRData
          .select(vars, strAD_Org_ID, vars.getSessionValue("#AD_CLIENT_ID"),
              vars.getSessionValue("#AD_USER_ID"), strSalesRepID, strDateFrom, strDateTo,
              strBPClientId);
      vars.setSessionObject("FormRCSalesInvByBPClientDetailsJR|rankclntsalesinvdetailsdata", data);

      printPageDataSheet(request, response, vars, strAD_Org_ID, strSalesRepID, strDateFrom,
          strDateTo, strBPClientId, true, data);

    } else if (vars.commandIn("PDF", "XLS")) {
      if (log4j.isDebugEnabled())
        log4j.debug("PDF");

      FormRCSalesInvByBPClientDetailsJRData[] data = (FormRCSalesInvByBPClientDetailsJRData[]) vars
          .getSessionObject("FormRCSalesInvByBPClientDetailsJR|rankclntsalesinvdetailsdata");

      printPageXLS(request, response, vars, strAD_Org_ID, strSalesRepID, strDateFrom, strDateTo,
          strBPClientId, data);
    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strAD_Org_ID, String strSalesRepID, String dateFrom,
      String dateTo, String strBPClientId, boolean isFirstLoad,
      FormRCSalesInvByBPClientDetailsJRData[] objData) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    FormRCSalesInvByBPClientDetailsJRData[] data = null;
    String strConvRateErrorMsg = "";
    String discard[] = { "discard" };

    xmlDocument = xmlEngine.readXmlTemplate(
        "pe/com/unifiedgo/report/ad_forms/FormRCSalesInvByBPClientDetailsFilterJR", discard)
        .createXmlDocument();

    OBError myMessage = null;
    myMessage = new OBError();
    try {
      if (objData != null) {
        data = objData;
      } else {
        data = FormRCSalesInvByBPClientDetailsJRData.select(vars, strAD_Org_ID,
            vars.getSessionValue("#AD_CLIENT_ID"), vars.getSessionValue("#AD_USER_ID"),
            strSalesRepID, dateFrom, dateTo, strBPClientId);
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
        data = FormRCSalesInvByBPClientDetailsJRData.set();
      } else {
        xmlDocument.setData("structure1", data);
      }
    }

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
      ToolBar toolbar = new ToolBar(this, vars.getLanguage(),
          "FormRCSalesInvByBPClientDetailsFilterJR", false, "", "", "", false, "ad_forms",
          strReplaceWith, false, true);
      toolbar.setEmail(false);
      toolbar.prepareSimpleToolBarTemplate();

      toolbar.prepareSimpleToolBarTemplate();
      toolbar.prepareRelationBarTemplate(false, false, "imprimirDetalleXLS();return false;");

      xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "pe.com.unifiedgo.report.ad_forms.FormRCSalesInvByBPClientDetailsJR");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
            "FormRCSalesInvByBPClientDetailsFilterJR.html", classInfo.id, classInfo.type,
            strReplaceWith, tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
            "FormRCSalesInvByBPClientDetailsFilterJR.html", strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      {
        myMessage = vars.getMessage("FormRCSalesInvByBPClientDetailsJR");
        vars.removeMessage("FormRCSalesInvByBPClientDetailsJR");
        if (myMessage != null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }

      xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");

      xmlDocument.setParameter("adOrg", strAD_Org_ID);

      // Print document in the output
      out.println(xmlDocument.print());
      out.close();
    }
  }

  private void printPageXLS(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strAD_Org_ID, String strSalesRepID, String dateFrom,
      String dateTo, String strBPClientId, FormRCSalesInvByBPClientDetailsJRData[] objData)
      throws IOException, ServletException {

    FormRCSalesInvByBPClientDetailsJRData[] data = null;
    try {
      if (objData != null) {
        data = objData;
      } else {
        data = FormRCSalesInvByBPClientDetailsJRData.select(vars, strAD_Org_ID,
            vars.getSessionValue("#AD_CLIENT_ID"), vars.getSessionValue("#AD_USER_ID"),
            strSalesRepID, dateFrom, dateTo, strBPClientId);
      }
    } catch (OBException e) {
      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "FINPR_NoConversionFound", vars.getLanguage()) + e.getMessage());
    }
    
    for(int i=0;i<data.length; i++ ){
    	FormRCSalesInvByBPClientDetailsJRData o= data[i];
    	o.grandtotal=quitaComma(o.grandtotal);

    }

    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());

    String strOutput = "xls";
    String strReportName;
    strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_forms/FormRCSalesInvByBPClientDetailsFilterJR.jrxml";

    HashMap<String, Object> parameters = new HashMap<String, Object>();

    parameters.put("ORGANIZACION", strAD_Org_ID);
    parameters.put("MONEDA", strSalesRepID);
    parameters.put("DESDE", StringToDate(dateFrom));
    parameters.put("HASTA", StringToDate(dateTo));

    renderJR(vars, response, strReportName, "Reporte_Ranking_Cliente_Detalle", strOutput,
        parameters, data, null);
  }
  
  private String quitaComma(String cadena) {
	    return cadena.replace(",", "");
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
