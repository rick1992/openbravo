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
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.businessUtility.Tree;
import org.openbravo.erpCommon.businessUtility.TreeData;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.accounting.data.SCOEEFFSubcategory;

public class FormEstadoResultadosAcctDetailsJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static String strBDErrorMessage = "";

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    String strAD_Org_ID = "", strSubCategoryId = "", strDateFrom = "", strDateTo = "";

    if (vars.commandIn("DEFAULT")) {
      strAD_Org_ID = vars.getStringParameter("inporgId");
      strSubCategoryId = vars.getStringParameter("inpsubCategoryId");
      strDateFrom = vars.getStringParameter("inpdateFrom");
      strDateTo = vars.getStringParameter("inpdateTo");

      vars.removeSessionValue("FormEstadoResultadosAcctDetailsJR|estresultsacctdetailsdata");
      ReporteEstadoResultadosAcctDetailsData[] data = selectAccountsByCategory(vars, this,
          strAD_Org_ID, strDateFrom, strDateTo, vars.getSessionValue("#AD_CLIENT_ID"),
          strSubCategoryId);
      vars.setSessionObject("FormEstadoResultadosAcctDetailsJR|estresultsacctdetailsdata", data);

      printPageDataSheet(request, response, vars, strAD_Org_ID, strSubCategoryId, strDateFrom,
          strDateTo, true, data);

    } else if (vars.commandIn("PDF", "XLS")) {
      if (log4j.isDebugEnabled())
        log4j.debug("PDF");

      ReporteEstadoResultadosAcctDetailsData[] data = (ReporteEstadoResultadosAcctDetailsData[]) vars
          .getSessionObject("FormEstadoResultadosAcctDetailsJR|estresultsacctdetailsdata");

      printPageXLS(request, response, vars, strDateFrom, strDateTo, strAD_Org_ID, strSubCategoryId,
          data);

    } else
      pageError(response);
  }

  private ReporteEstadoResultadosAcctDetailsData[] selectAccountsByCategory(
      VariablesSecureApp vars, ConnectionProvider connectionProvider, String adOrg,
      String dateFromMes, String dateToMes, String adUserClient, String subCategoryId)
      throws IOException, ServletException {
    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    String strOrgFamily = getFamily(strTreeOrg, adOrg);

    // //////////////////////////////
    String[] fechaAnioActual = dateToMes.split("-");

    Integer anioActual = new Integer(fechaAnioActual[2]);

    String fechaIniAnioActual = "01-01-" + anioActual;
    String fechaFinAnioActual = dateToMes;
    // /////////////////////////////

    ReporteEstadoResultadosAcctDetailsData[] data = ReporteEstadoResultadosAcctDetailsData
        .selectAccountsByCategory(connectionProvider, strOrgFamily, fechaIniAnioActual,
            fechaFinAnioActual,
            Utility.getContext(this, vars, "#User_Client", "ReporteEEFFGGPPNaturaleza"),
            subCategoryId);
    return data;
  }

  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strAD_Org_ID, String strSubCategoryId, String dateFrom,
      String dateTo, boolean isFirstLoad, ReporteEstadoResultadosAcctDetailsData[] objData)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReporteEstadoResultadosAcctDetailsData[] data = null;
    String strConvRateErrorMsg = "";
    String discard[] = { "discard" };

    xmlDocument = xmlEngine.readXmlTemplate(
        "pe/com/unifiedgo/report/ad_forms/FormEstadoResultadosAcctDetailsFilterJR", discard)
        .createXmlDocument();

    String[] fechaAnioActual = dateTo.split("-");

    OBError myMessage = null;
    myMessage = new OBError();
    try {
      if (objData != null) {
        data = objData;
      } else {
        data = selectAccountsByCategory(vars, this, strAD_Org_ID, dateFrom, dateTo,
            vars.getSessionValue("#AD_CLIENT_ID"), strSubCategoryId);
      }

      // formmatting data
      SCOEEFFSubcategory subCat = OBDal.getInstance().get(SCOEEFFSubcategory.class,
          strSubCategoryId);
      if (subCat != null) {
        for (int i = 0; i < data.length; i++) {
          data[i].subCategoriaId = subCat.getId();
          data[i].subCategoria = subCat.getDescription();
          data[i].tittlesaldo2 = fechaAnioActual[2];
        }
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
        data = ReporteEstadoResultadosAcctDetailsData.set();
      } else {
        xmlDocument.setData("structure1", data);
      }
    }

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
      ToolBar toolbar = new ToolBar(this, vars.getLanguage(),
          "FormEstadoResultadosAcctDetailsFilterJR", false, "", "", "imprimirDetallePDF();return false;", false, "ad_forms",
          strReplaceWith, false, true);
      toolbar.setEmail(false);

      toolbar.prepareSimpleToolBarTemplate();
      toolbar.prepareRelationBarTemplate(false, false, "imprimirDetalleXLS();return false;");

      xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "pe.com.unifiedgo.report.ad_forms.FormEstadoResultadosAcctDetailsJR");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
            "FormEstadoResultadosAcctDetailsFilterJR.html", classInfo.id, classInfo.type,
            strReplaceWith, tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
            "FormEstadoResultadosAcctDetailsFilterJR.html", strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      {
        myMessage = vars.getMessage("FormEstadoResultadosAcctDetailsJR");
        vars.removeMessage("FormEstadoResultadosAcctDetailsJR");
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
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String strAD_Org_ID,
      String strSubCategoryId, ReporteEstadoResultadosAcctDetailsData[] objData)
      throws IOException, ServletException {
	  
    ReporteEstadoResultadosAcctDetailsData[] data = null;
	  
	    String[] fechaAnioActual = strDateTo.split("-");

	    try {
	      if (objData != null) {
	        data = objData;
	      } else {
	        data = selectAccountsByCategory(vars, this, strAD_Org_ID, strDateFrom, strDateTo,
	            vars.getSessionValue("#AD_CLIENT_ID"), strSubCategoryId);
	      }

	      // formmatting data
	      SCOEEFFSubcategory subCat = OBDal.getInstance().get(SCOEEFFSubcategory.class,
	          strSubCategoryId);
	      if (subCat != null) {
	        for (int i = 0; i < data.length; i++) {
	          data[i].subCategoriaId = subCat.getId();
	          data[i].subCategoria = subCat.getDescription();
	          data[i].tittlesaldo2 = fechaAnioActual[2];
	        }
	      }

	    } catch (ServletException ex) {
//	      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
	    }
	 
    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());

    String strOutput = "xls";
    String strReportName;
    
    if (vars.commandIn("PDF")) {
        strOutput = "pdf";
        strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_forms/FormEstadoResultadosAcctDetailsPDF.jrxml";
      } else {
        strOutput = "xls";
        strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_forms/FormEstadoResultadosAcctDetailsEXCEL.jrxml";
      }

    HashMap<String, Object> parameters = new HashMap<String, Object>();

    parameters.put("ORGANIZACION", strAD_Org_ID);
    parameters.put("SUBCATEGORIA", strSubCategoryId);
    parameters.put("DESDE", StringToDate(strDateFrom));
    parameters.put("HASTA", StringToDate(strDateTo));

    renderJR(vars, response, strReportName, "Reporte_Estado_Resultados_Cuentas_Detalle", strOutput,
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

  private String getFamily(String strTree, String strChild) throws IOException, ServletException {
    return Tree.getMembers(this, strTree, (strChild == null || strChild.equals("")) ? "0"
        : strChild)
        + ",'0'";
    /*
     * ReportGeneralLedgerData [] data = ReportGeneralLedgerData.selectChildren(this, strTree,
     * strChild); String strFamily = ""; if(data!=null && data.length>0) { for (int i =
     * 0;i<data.length;i++){ if (i>0) strFamily = strFamily + ","; strFamily = strFamily +
     * data[i].id; } return strFamily += ""; }else return "'1'";
     */
  }

  public String getServletInfo() {
    return "Servlet PurchaseOrderFilter. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method

}
