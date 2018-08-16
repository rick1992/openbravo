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
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.businessUtility.Tree;
import org.openbravo.erpCommon.businessUtility.TreeData;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SQLReturnObject;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class ReportePorDevolucionCliente extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static String strBDErrorMessage = "";
  private static String LocalOrg = null;
  private static String OrderID = null;

  private static final String CHILD_SHEETS = "frameWindowTreeF3"; // tree

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    OBError myMessage = null;
    myMessage = new OBError();
    myMessage.setTitle("");

    if (vars.commandIn("DEFAULT")) {
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportePorDevolucionCliente|Org", "");
      String strDocumentno = vars.getGlobalVariable("inpDocumentno",
          "ReportePorDevolucionCliente|Documentno", "");
      String strDocDate = vars.getGlobalVariable("inpDocDate", "ReportePorDevolucionCliente|docDate",
          SREDateTimeData.FirstDayOfMonth(this));
      String strDateto = vars.getGlobalVariable("inpDateTo", "ReportePorDevolucionCliente|dateTo",
          SREDateTimeData.today(this));
      String showLines = vars.getGlobalVariable("inpMostrarDetalles",
          "ReportePorDevolucionCliente|MostrarDetalles", "");

      printPageDataSheet(request, response, vars, strOrg, strDocumentno, strDocDate, strDateto,
          showLines);

    } else if (vars.commandIn("LISTAR")) {
      String strOrg = vars.getStringParameter("inpadOrgId");
      String strDocumentno = vars.getStringParameter("inpDocumentno");
      String strDocDate = vars.getStringParameter("inpDocDate");
      String strDateTo = vars.getStringParameter("inpDateTo");
      String showLines = vars.getStringParameter("paramProductOrder");

      printPageDataSheet(request, response, vars, strOrg, strDocumentno, strDocDate, strDateTo,
          showLines);
    } else if (vars.commandIn("PDF", "XLS")) {
      String strOrg = vars.getStringParameter("inpadOrgId");
      String strDocumentno = vars.getStringParameter("inpDocumentno");
      String strDateFrom = vars.getStringParameter("inpDocDate");
      String strDateTo = vars.getStringParameter("inpDateTo");
      String showLines = vars.getStringParameter("inpMostrarDetalles");// Mostrar lineas de la orden
      String strIdProveedor = vars.getStringParameter("inpcBPartnerId");
      String showSinIngresos = vars.getStringParameter("inpSinIngresos");// Mostrar ordenes sin ingreso
      String strIdProducto = vars.getStringParameter("inpmProductId");


      String adLanguage = vars.getSessionValue("#AD_LANGUAGE");

      setHistoryCommand(request, "DEFAULT");
      if (!(strDocumentno.equalsIgnoreCase("")) && strDocumentno != null) {
        strDateFrom = "";
        strDateTo = "";
      }

      if (vars.commandIn("PDF")) {
        printPagePDF(request, response, vars, strOrg, strDocumentno, strDateFrom, strDateTo,
            adLanguage, showLines,strIdProveedor,showSinIngresos,strIdProducto);
      }

      if (vars.commandIn("XLS")) {
        printPageXLS(request, response, vars, strOrg, strDocumentno, strDateFrom, strDateTo,
            adLanguage, showLines,strIdProveedor,showSinIngresos,strIdProducto);
      }

    } else
      pageError(response);
  }

  private void printPagePDF(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strOrg, String strDocumentno, String strDateFrom,
      String strDateTo, String adLanguage, String showLines,String strIdProveedor, String showSinIngresos,String strIdProducto) throws IOException, ServletException {

	showSinIngresos=showSinIngresos.compareToIgnoreCase("on")==0?"Y":"N";

	  
    ReportePorDevolucionClienteData[] data = null;

    OBError myMessage = null;
    myMessage = new OBError();

    Date fecha = null;

    String strOutput;
    String strReportName;

	String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
	String strOrgFamily = getFamily(strTreeOrg, strOrg);

    try {
        data = ReportePorDevolucionClienteData.select(this, Utility
				.getContext(this, vars, "#User_Client",
						"ReportePorDevolucionCliente"),
						strOrgFamily, strDateFrom, strDateTo);
    } catch (OBException e) {
      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "FINPR_NoConversionFound", vars.getLanguage()) + e.getMessage());
      data = ReportePorDevolucionClienteData.set("");
    }
    
   
    
    strOutput = "pdf";
    strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportePorDevolucionCliente.jrxml";

    HashMap<String, Object> parameters = new HashMap<String, Object>();

    OBCriteria<Organization> organization = OBDal.getInstance().createCriteria(Organization.class);
    organization.add(Restrictions.eq(Organization.PROPERTY_ID, strOrg));

    List<Organization> OrganizationList = organization.list();

    parameters.put("ORGANIZACION", OrganizationList.get(0).getSocialName());

    parameters.put("DESDE", StringToDate(strDateFrom));
    parameters.put("HASTA", StringToDate(strDateTo));
    parameters.put("MOSTRARLINEAS", showLines.toString());
    parameters.put("MOSTRARSININGRESOS", showSinIngresos.toString());


    renderJR(vars, response, strReportName, "Reporte_Ingresos_por_Compras", strOutput, parameters,
        data, null);

  }

  private void printPageXLS(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strOrg, String documentNo, String strDateFrom,
      String strDateTo, String adLanguage, String showLines,String strIdProveedor, String showSinIngresos,String strIdProducto) throws IOException, ServletException {

	showSinIngresos=showSinIngresos.compareToIgnoreCase("on")==0?"Y":"N";
	
    ReportePorDevolucionClienteData[] data = null;

    OBError myMessage = null;
    myMessage = new OBError();

    String strOutput;
    String strReportName;
    
	String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
	String strOrgFamily = getFamily(strTreeOrg, strOrg);

    try {
        data = ReportePorDevolucionClienteData.select(this, Utility
				.getContext(this, vars, "#User_Client",
						"ReportePorDevolucionCliente"),
						strOrgFamily, strDateFrom, strDateTo);
    } catch (OBException e) {
      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "FINPR_NoConversionFound", vars.getLanguage()) + e.getMessage());
      data = ReportePorDevolucionClienteData.set("");
    }
  

    strOutput = "xls";
    strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportePorDevolucionClienteExcel.jrxml";

    HashMap<String, Object> parameters = new HashMap<String, Object>();

    OBCriteria<Organization> organization = OBDal.getInstance().createCriteria(Organization.class);
    organization.add(Restrictions.eq(Organization.PROPERTY_ID, strOrg));

    List<Organization> OrganizationList = organization.list();

    parameters.put("ORGANIZACION", OrganizationList.get(0).getSocialName());

    parameters.put("DESDE", StringToDate(strDateFrom));
    parameters.put("HASTA", StringToDate(strDateTo));
    parameters.put("MOSTRARLINEAS", showLines.toString());
    parameters.put("MOSTRARSININGRESOS", showSinIngresos.toString());


    renderJR(vars, response, strReportName, "Reporte por Devolucion de Clientes", strOutput, parameters,
        data, null);

  }
  
	private String getFamily(String strTree, String strChild)
			throws IOException, ServletException {
		return Tree.getMembers(this, strTree,
				(strChild == null || strChild.equals("")) ? "0" : strChild);
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
      VariablesSecureApp vars, String strOrg, String strDocumentno, String strDateFrom,
      String strDateTo, String showLines) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();

    XmlDocument xmlDocument = null;
    // ReportePorDevolucionClienteData[] datausers = null;

    ReportePorDevolucionClienteData[] dataorgs = null;

    String strConvRateErrorMsg = "";
    String discard[] = { "discard" };

    xmlDocument = xmlEngine.readXmlTemplate(
        "pe/com/unifiedgo/report/ad_reports/ReportePorDevolucionCliente", discard)
        .createXmlDocument();

    if (vars.commandIn("DEFAULT")) {
      // xmlDocument.setParameter("menu", loadNodes(null, null, null, "0"));
    }

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {

      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportePorDevolucionCliente", false,
          "", "", "", false, "ad_reports", strReplaceWith, false, true);
       toolbar.setEmail(false);
       toolbar.prepareSimpleToolBarTemplate();
      toolbar.prepareRelationBarTemplate(false, false, "printXLS();return false;");
      // toolbar.prepareRelationBarTemplate(false, false, "printXLS();return false;");
      xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "pe.com.unifiedgo.report.ad_reports.ReportePorDevolucionCliente");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
            "ReportePorDevolucionCliente.html", classInfo.id, classInfo.type, strReplaceWith,
            tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
            "ReportePorDevolucionCliente.html", strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      try {

        dataorgs = ReportePorDevolucionClienteData.selectOrgs(this);

        FieldProvider orgs[] = new FieldProvider[dataorgs.length];
        Vector<Object> vector = new Vector<Object>(0);

        for (int i = 0; i < dataorgs.length; i++) {
          SQLReturnObject sqlReturnObject = new SQLReturnObject();
          sqlReturnObject.setData("ID", dataorgs[i].orgid);
          sqlReturnObject.setData("NAME", dataorgs[i].orgname);
          vector.add(sqlReturnObject);
        }
        vector.copyInto(orgs);
        xmlDocument.setData("reportAD_Org_ID", "liststructure", orgs);
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      {
        OBError myMessage = vars.getMessage("ReportePorDevolucionCliente");
        vars.removeMessage("ReportePorDevolucionCliente");
        if (myMessage != null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }

      xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("docDatedisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("docDatesaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

      xmlDocument.setParameter("docDate", strDateFrom);
      xmlDocument.setParameter("docDateto", strDateTo);
      xmlDocument.setParameter("adOrgId", strOrg);
      
//      xmlDocument.setParameter("paramProductId", strmProductId);
//		xmlDocument.setParameter("paramProductDescription",
//				ReportLibroConsignacionesData.selectMproduct(this,
//						strmProductId));

      out.println(xmlDocument.print());
      out.close();
    }
  }

  public String getServletInfo() {
    return "Servlet PurchaseOrderFilter. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method

}
