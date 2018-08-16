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
import org.openbravo.erpCommon.businessUtility.TreeData;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class DetalleProductosVendidosJR extends HttpSecureAppServlet {
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
      String strProyDate = vars.getGlobalVariable("inpProyDate",
          "DetalleProductosVendidosJR|proyDate", SREDateTimeData.lastDayofCurrentMonth(this));
      String strDocDate = vars.getGlobalVariable("inpDocDate",
          "DetalleProductosVendidosJR|docDate", SREDateTimeData.FirstDayOfMonth(this));
      String strAD_Org_ID = vars.getGlobalVariable("inpOrgId", "DetalleProductosVendidosJR|OrgId",
          "");

      vars.removeSessionValue("DetalleProductosVendidosJR|salesdetaildata");

      printPageDataSheet(request, response, vars, strDocDate, strProyDate, strAD_Org_ID, true, null,"","");

    } else if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
      String strAD_Org_ID = vars.getStringParameter("inpOrgId");
      String strDocDate = vars.getStringParameter("inpDocDate");
      String strProyDate = vars.getStringParameter("inpProyDate");
		String strIdCliente = vars.getStringParameter("inpcBPartnerId");

		String strIdVendedor = vars.getStringParameter("inpSalesRepId");

      DetalleProductosVendidosJRData[] data = DetalleProductosVendidosJRData.selectSalesDetail(
          this, Utility.getContext(this, vars, "#User_Client", null),
          Utility.getContext(this, vars, "#User_Org", null), strDocDate, strProyDate,strIdCliente,strIdVendedor, strAD_Org_ID);
      vars.setSessionObject("DetalleProductosVendidosJR|salesdetaildata", data);

      printPageDataSheet(request, response, vars, strDocDate, strProyDate, strAD_Org_ID, false,
          data,strIdCliente,strIdVendedor);

    } else if (vars.commandIn("PDF", "XLS")) {// aqui para imprimir como pdf o excel
      if (log4j.isDebugEnabled())
        log4j.debug("PDF");
      String strAD_Org_ID = vars.getStringParameter("inpOrgId");
      String strDocDate = vars
          .getGlobalVariable("inpDocDate", "DetalleProductosVendidosJR|docDate");
      String strProyDate = vars.getGlobalVariable("inpProyDate",
          "DetalleProductosVendidosJR|proyDate");
      String strCurrencyId = "308";
      
		String strIdCliente = vars.getStringParameter("inpcBPartnerId");

		String strIdVendedor = vars.getStringParameter("inpSalesRepId");

      setHistoryCommand(request, "DEFAULT");

      DetalleProductosVendidosJRData[] data = (DetalleProductosVendidosJRData[]) vars
          .getSessionObject("DetalleProductosVendidosJR|salesdetaildata");

      if (vars.commandIn("XLS")) {
        printPageXLS(request, response, vars, strDocDate, strProyDate, strAD_Org_ID, strCurrencyId,
            data,strIdCliente,strIdVendedor);
      }

    } else
      pageError(response);
  }

  private void printPageXLS(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDocDate, String strProyDate, String strAD_Org_ID,
      String strCurrencyId, DetalleProductosVendidosJRData[] objData,String strIdCliente,String strIdVendedor) throws IOException,
      ServletException {

    DetalleProductosVendidosJRData[] data = null;
    try {
      if (objData != null) {
        data = objData;
      } else {
        data = DetalleProductosVendidosJRData.selectSalesDetail(this,
            Utility.getContext(this, vars, "#User_Client", null),
            Utility.getContext(this, vars, "#User_Org", null), strDocDate, strProyDate,strIdCliente,strIdVendedor,
            strAD_Org_ID);
      }

    } catch (OBException e) {
      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "FINPR_NoConversionFound", vars.getLanguage()) + e.getMessage());
    }

    /*
     * for (int i = 0; i < data.length; i++) { String monto_soles =
     * data[i].totalpenlinenetamt.replace(",", ""); String monto_dolares =
     * data[i].totalusdlinenetamt.replace(",", "");
     * 
     * data[i].totalpenlinenetamt = new BigDecimal(monto_soles).toString();
     * data[i].totalusdlinenetamt = new BigDecimal(monto_dolares).toString(); }
     */

    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    String strSubtitle = (Utility.messageBD(this, "LegalEntity", vars.getLanguage()) + ": ")
        + CustomerAccountsReceivableData.selectCompany(this, vars.getClient()) + "\n" + "RUC:"
        + CustomerAccountsReceivableData.selectRucOrg(this, strAD_Org_ID) + "\n";

    if (!("0".equals(strAD_Org_ID)))
      strSubtitle += (Utility.messageBD(this, "OBUIAPP_Organization", vars.getLanguage()) + ": ")
          + CustomerAccountsReceivableData.selectOrg(this, strAD_Org_ID) + "\n";

    String strOutput = "xls";
    String strReportName;
    strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/DetalleProductosVendidosExcel.jrxml";

    HashMap<String, Object> parameters = new HashMap<String, Object>();

    // parameters.put("ORGANIZACION",
    // DetalleProductosVendidosJRData.selectOrganization(this, strAD_Org_ID));
	parameters.put("RUC_ORG", DetalleProductosVendidosJRData
			.selectRUC(this, strAD_Org_ID));
	parameters.put("ORGANIZACION",
			DetalleProductosVendidosJRData
					.selectOrg(this, strAD_Org_ID));

	
    parameters.put("dateFrom", StringToDate(strDocDate));
    parameters.put("dateTo", StringToDate(strProyDate));

    renderJR(vars, response, strReportName, "Reporte_Detalle_Productos_Vendidos", strOutput,
        parameters, data, null);
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
      boolean isFirstLoad, DetalleProductosVendidosJRData[] objData,String strIdCliente,String strIdVendedor)
    		  throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    DetalleProductosVendidosJRData[] data = null;
    String strConvRateErrorMsg = "";

    String discard[] = { "discard" };

    xmlDocument = xmlEngine.readXmlTemplate(
        "pe/com/unifiedgo/report/ad_reports/DetalleProductosVendidosFilterJR", discard)
        .createXmlDocument();

    if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
      OBError myMessage = null;
      myMessage = new OBError();
      try {
        if (objData != null) {
          data = objData;
        } else {
          data = DetalleProductosVendidosJRData.selectSalesDetail(this,
              Utility.getContext(this, vars, "#User_Client", null),
              Utility.getContext(this, vars, "#User_Org", null), strDocDate, strProyDate,strIdCliente,strIdVendedor,
              strAD_Org_ID);
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
          data = DetalleProductosVendidosJRData.set();
        } else {
          xmlDocument.setData("structure1", data);
        }
      }
    }

    else {
      if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
        discard[0] = "selEliminar";
        data = DetalleProductosVendidosJRData.set();
      }
    }

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {

      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "DetalleProductosVendidosFilterJR",
          false, "", "", "", false, "ad_reports", strReplaceWith, false, true);
      toolbar.setEmail(false);
      toolbar.prepareSimpleToolBarTemplate();
      toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");
      xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "pe.com.unifiedgo.report.ad_reports.DetalleProductosVendidosJR");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
            "DetalleProductosVendidosFilterJR.html", classInfo.id, classInfo.type, strReplaceWith,
            tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
            "DetalleProductosVendidosFilterJR.html", strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      {
        OBError myMessage = vars.getMessage("DetalleProductosVendidosJR");
        vars.removeMessage("DetalleProductosVendidosJR");
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
      
      User vendedor = OBDal.getInstance().get(User.class, strIdVendedor);
      xmlDocument.setParameter("paramSalesRepId", strIdVendedor);
      xmlDocument.setParameter("paramSalesRepDescription",vendedor!=null?vendedor.getName():"");
      
      BusinessPartner cliente = OBDal.getInstance().get(BusinessPartner.class,strIdCliente);
      xmlDocument.setParameter("paramBPartnerId", strIdCliente);
      xmlDocument.setParameter("paramBPartnerDescription",cliente!=null?cliente.getTaxID()+" - "+cliente.getName():"");
      
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
