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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.accounting.data.SCOEEFFSubcategory;

public class FormEstadoSituacionFinancieraAcctDetailsJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static String strBDErrorMessage = "";

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    String strAD_Org_ID = "", strDateFrom = "", strDateTo = "", strSubCategoryId = "";

    if (vars.commandIn("DEFAULT")) {
      strAD_Org_ID = vars.getStringParameter("inporgId");
      strSubCategoryId = vars.getStringParameter("inpsubCategoryId");
      strDateFrom = vars.getStringParameter("inpdateFrom");
      strDateTo = vars.getStringParameter("inpdateTo");

      printPageDataSheet(request, response, vars, strDateFrom, strDateTo, strAD_Org_ID,
          strSubCategoryId);

    } else if (vars.commandIn("PDF", "XLS")) {
      if (log4j.isDebugEnabled())
        log4j.debug("PDF");
      strAD_Org_ID = vars.getStringParameter("inporgId");
      strSubCategoryId = vars.getStringParameter("inpsubCategoryId");
      strDateFrom = vars.getStringParameter("inpdateFrom");
      strDateTo = vars.getStringParameter("inpdateTo");

      printPageXLS(request, response, vars, strDateFrom, strDateTo, strAD_Org_ID, strSubCategoryId);

    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String strOrg,
      String strSubCategoryId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReporteEstadoSituacionFinancieraAcctDetailsData[] dataFactsAnterior = null;
    ReporteEstadoSituacionFinancieraAcctDetailsData[] dataFactsActual = null;

    ReporteEstadoSituacionFinancieraAcctDetailsData[] dataFinal = null;

    DecimalFormat df = new DecimalFormat("0.00");
    df.setRoundingMode(RoundingMode.HALF_UP);

    String discard[] = { "discard" };

    xmlDocument = xmlEngine.readXmlTemplate(
        "pe/com/unifiedgo/report/ad_forms/FormEstadoSituacionFinancieraAcctDetailsFilterJR",
        discard).createXmlDocument();

    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    String strOrgFamily = getFamily(strTreeOrg, strOrg);
    String strCliente = Utility.getContext(this, vars, "#User_Client",
        "ReporteEstadoSituacionFinanciera");

    String[] fechaAnioAnterior = strDateFrom.split("-");
    String[] fechaAnioActual = strDateTo.split("-");

    Integer anioAnterior = new Integer(fechaAnioAnterior[2]);
    Integer anioActual = new Integer(fechaAnioActual[2]);

    String fechaFinAnioAnterior = strDateFrom;
    String fechaIniAnioActual = "01-01-" + anioActual;

    String fechaIniAnioAnterior = "01-01-" + (anioAnterior);
    String fechaFinAnioActual = strDateTo;

    // dataFactsAnterior = ReporteEstadoSituacionFinancieraAcctDetailsData
    // .select_facts_por_categorias(this, strOrgFamily, fechaIniAnioAnterior,
    // fechaFinAnioAnterior, strCliente, strSubCategoryId);

    dataFactsActual = ReporteEstadoSituacionFinancieraAcctDetailsData.select_facts_por_categorias(
        this, strOrgFamily, fechaIniAnioActual, fechaFinAnioActual, strCliente, strSubCategoryId);

    if (dataFactsActual != null && dataFactsActual.length != 0) {
      dataFinal = dataFactsActual;

      // formmatting data
      SCOEEFFSubcategory subCat = OBDal.getInstance().get(SCOEEFFSubcategory.class,
          strSubCategoryId);
      if (subCat != null) {
        for (int i = 0; i < dataFinal.length; i++) {
          dataFinal[i].subCategoriaId = subCat.getId();
          dataFinal[i].subCategoria = subCat.getDescription();
          dataFinal[i].tittlesaldo2 = fechaAnioActual[2];
        }
      }

    } else {
      dataFinal = ReporteEstadoSituacionFinancieraAcctDetailsData.set();
    }

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(),
        "FormEstadoSituacionFinancieraAcctDetailsFilterJR", false, "", "", "imprimirDetallePDF();return false;", false, "ad_forms",
        strReplaceWith, false, true);
    toolbar.setEmail(false);

    toolbar.prepareSimpleToolBarTemplate();
    toolbar.prepareRelationBarTemplate(false, false, "imprimirDetalleXLS();return false;");

    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "pe.com.unifiedgo.report.ad_forms.FormEstadoSituacionFinancieraAcctDetailsJR");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "FormEstadoSituacionFinancieraAcctDetailsFilterJR.html", classInfo.id, classInfo.type,
          strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
          "FormEstadoSituacionFinancieraAcctDetailsFilterJR.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("FormEstadoSituacionFinancieraAcctDetailsJR");
      vars.removeMessage("FormEstadoSituacionFinancieraAcctDetailsJR");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");

    xmlDocument.setParameter("adOrg", strOrg);
    
    vars.setSessionObject("FormEstadoSituacionFinancieraAcctDetailsJR|estresultsacctdetailsdata", dataFinal);

    xmlDocument.setData("structure1", dataFinal);
    // Print document in the output
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageXLS(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String strOrg,
      String strSubCategoryId)
    		  throws IOException, ServletException {
	  
    
	ReporteEstadoSituacionFinancieraAcctDetailsData[] data = (ReporteEstadoSituacionFinancieraAcctDetailsData[]) vars
            .getSessionObject("FormEstadoSituacionFinancieraAcctDetailsJR|estresultsacctdetailsdata");
    
    ReporteEstadoSituacionFinancieraAcctDetailsData[] dataFinal = null;

    
    if(data == null){
    	 ReporteEstadoSituacionFinancieraAcctDetailsData[] dataFactsActual = null;


    	    DecimalFormat df = new DecimalFormat("0.00");
    	    df.setRoundingMode(RoundingMode.HALF_UP);

    	    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    	    String strOrgFamily = getFamily(strTreeOrg, strOrg);
    	    String strCliente = Utility.getContext(this, vars, "#User_Client",
    	        "ReporteEstadoSituacionFinanciera");

    	    String[] fechaAnioActual = strDateTo.split("-");

    	    Integer anioActual = new Integer(fechaAnioActual[2]);

    	    String fechaIniAnioActual = "01-01-" + anioActual;

    	    String fechaFinAnioActual = strDateTo;

    	    dataFactsActual = ReporteEstadoSituacionFinancieraAcctDetailsData.select_facts_por_categorias(
    	        this, strOrgFamily, fechaIniAnioActual, fechaFinAnioActual, strCliente, strSubCategoryId);

    	    if (dataFactsActual != null && dataFactsActual.length != 0) {
    	      dataFinal = dataFactsActual;

    	      // formmatting data
    	      SCOEEFFSubcategory subCat = OBDal.getInstance().get(SCOEEFFSubcategory.class,
    	          strSubCategoryId);
    	      if (subCat != null) {
    	        for (int i = 0; i < dataFinal.length; i++) {
    	          dataFinal[i].subCategoriaId = subCat.getId();
    	          dataFinal[i].subCategoria = subCat.getDescription();
    	          dataFinal[i].tittlesaldo2 = fechaAnioActual[2];
    	        }
    	      }

    	    } else {
    	      dataFinal = ReporteEstadoSituacionFinancieraAcctDetailsData.set();
    	    }
    	    
    } else {
    	dataFinal=data;
    }

    String strOutput = "xls";
    String strReportName;
    
    if (vars.commandIn("PDF")) {
        strOutput = "pdf";
        strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_forms/FormEstadoSituacionFinancieraAcctDetailsPDF.jrxml";
      } else {
        strOutput = "xls";
        strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_forms/FormEstadoSituacionFinancieraAcctDetailsEXCEL.jrxml";
      }
    
    
    HashMap<String, Object> parameters = new HashMap<String, Object>();

    parameters.put("ORGANIZACION", strOrg);
    parameters.put("CUENTA", strSubCategoryId);
    parameters.put("DESDE", StringToDate(strDateFrom));
    parameters.put("HASTA", StringToDate(strDateTo));

    renderJR(vars, response, strReportName, "Reporte_Estado_Situacion_Financiera_Cuentas_Detalle", strOutput,
        parameters, dataFinal, null);
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
