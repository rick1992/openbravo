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
import org.openbravo.database.ConnectionProvider;
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

public class ResumenCobrosJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static String strBDErrorMessage = "";

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    /*
     * Enumeration e = vars.getParameterNames(); while (e.hasMoreElements()) { String name =
     * (String) e.nextElement(); System.out.println("name: " + name); }
     */

    // Get user Client's base currency
    String strUserCurrencyId = Utility.stringBaseCurrencyId(this, vars.getClient());
    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars.getGlobalVariable("inpDocDate", "ResumenCobrosJR|docDate",
          SREDateTimeData.today(this));
      String strDateTo = vars.getGlobalVariable("inpProyDate", "ResumenCobrosJR|proyDate",
          SREDateTimeData.tomorrow(this));
      String strAD_Org_ID = vars.getGlobalVariable("inpOrgId", "ResumenCobrosJR|OrgId", "");
      String strPaymentMethodId = vars.getGlobalVariable("inpPaymentMethod",
          "ResumenCobrosJR|PaymentMethod", "");
      String strPaymentType = vars.getGlobalVariable("inpPaymentType",
          "ResumenCobrosJR|PaymentType", "");

      printPageDataSheet(request, response, vars, strDateFrom, strDateTo, strAD_Org_ID,
          strPaymentMethodId, strPaymentType, true);
    } else if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {

      String strDateFrom = vars.getStringParameter("inpDocDate");
      String strDateTo = vars.getStringParameter("inpProyDate");
      String strAD_Org_ID = vars.getStringParameter("inpOrgId");
      String strPaymentMethodId = vars.getStringParameter("inpPaymentMethod");
      String strPaymentType = vars.getStringParameter("inpPaymentType");

      printPageDataSheet(request, response, vars, strDateFrom, strDateTo, strAD_Org_ID,
          strPaymentMethodId, strPaymentType, false);
    } else if (vars.commandIn("PDF", "XLS")) {// aqui para imprimir como pdf
      // o excel
      if (log4j.isDebugEnabled())
        log4j.debug("PDF");

      String strDateFrom = vars.getGlobalVariable("inpDocDate", "ResumenCobrosJR|docDate");
      String strDateTo = vars.getGlobalVariable("inpProyDate", "ResumenCobrosJR|proyDate");
      String strAD_Org_ID = vars.getStringParameter("inpOrgId");
      String strPaymentMethodId = vars.getStringParameter("inpPaymentMethod");
      String strPaymentType = vars.getStringParameter("inpPaymentType");

      setHistoryCommand(request, "DEFAULT");

      // if (vars.commandIn("XLS")) {
      printPage(request, response, vars, strDateFrom, strDateTo, strAD_Org_ID, strPaymentMethodId,
          strPaymentType);
      // }

    } else
      pageError(response);
  }

  private void printPage(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String strAD_Org_ID,
      String strPaymentMethodId, String strPaymentType) throws IOException, ServletException {

    ResumenCobrosJRData[] data = null;

    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    String strOrgFamily = getFamily(this, strTreeOrg, strAD_Org_ID);

    try {
      data = ResumenCobrosJRData.selectDiff(this,
          Utility.getContext(this, vars, "#User_Client", "ResumenCobrosJR"), strOrgFamily,
          strDateFrom, strDateTo, strPaymentMethodId, strPaymentType);

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

    String strOutput = "xls";
    String strReportName;

    if (vars.commandIn("PDF")) {
      strOutput = "pdf";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ResumenCobros.jrxml";
    } else {
      strOutput = "xls";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ResumenCobrosExcel.jrxml";
    }

    HashMap<String, Object> parameters = new HashMap<String, Object>();

    parameters.put("metodo_pago", strPaymentMethodId);

    parameters.put("condiciones_pago", ResumenCobrosJRData.selectTipoPago(this, strPaymentType));

    parameters.put("ORGANIZACION", ResumenCobrosJRData.selectOrganization(this, strAD_Org_ID));

    parameters.put("Ruc", ReportSalesRevenueRecordsData.selectRucOrg(this, strAD_Org_ID));
    parameters.put("Razon", ReportSalesRevenueRecordsData.selectOrg(this, strAD_Org_ID));

    parameters.put("dateFrom", StringToDate(strDateFrom));
    parameters.put("dateTo", StringToDate(strDateTo));

    renderJR(vars, response, strReportName, "Resumen de Cobros", strOutput, parameters, data, null);
  }

  static private String getFamily(ConnectionProvider conn, String strTree, String strChild)
      throws IOException, ServletException {
    return Tree.getMembers(conn, strTree, (strChild == null || strChild.equals("")) ? "0"
        : strChild);
    /*
     * ReportGeneralLedgerData [] data = ReportGeneralLedgerData.selectChildren(this, strTree,
     * strChild); String strFamily = ""; if(data!=null && data.length>0) { for (int i =
     * 0;i<data.length;i++){ if (i>0) strFamily = strFamily + ","; strFamily = strFamily +
     * data[i].id; } return strFamily += ""; }else return "'1'";
     */
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
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String strAD_Org_ID,
      String strPaymentMethodId, String strPaymentType, boolean isFirstLoad) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ResumenCobrosJRData[] data = null;
    String strConvRateErrorMsg = "";

    String discard[] = { "discard" };

    xmlDocument = xmlEngine.readXmlTemplate(
        "pe/com/unifiedgo/report/ad_reports/ResumenCobrosFilterJR", discard).createXmlDocument();

    if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
      OBError myMessage = null;
      myMessage = new OBError();
      try {
        // data = ResumenCobrosJRData.select(vars, strAD_Org_ID,
        // vars.getSessionValue("#AD_CLIENT_ID"), strDateFrom,
        // strDateTo,
        // vars.getSessionValue("#AD_USER_ID"));

        data = ResumenCobrosJRData.set("");

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
          data = ResumenCobrosJRData.set("");
        } else {
          xmlDocument.setData("structure1", data);
        }
      }
    }

    else {
      if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
        discard[0] = "selEliminar";
        data = ResumenCobrosJRData.set("");
      }
    }

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {

      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ResumenCobrosFilterJR", false, "",
          "", "imprimir();return false;", false, "ad_reports", strReplaceWith, false, true);
      toolbar.setEmail(false);

      toolbar.prepareSimpleToolBarTemplate();
      toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");
      xmlDocument.setParameter("toolbar", toolbar.toString());

      // ToolBar toolbar = new ToolBar(this, vars.getLanguage(),
      // "ResumenCobrosFilterJR",
      // false,
      // "", "", "", false, "ad_reports", strReplaceWith, false, true);
      // toolbar.prepareSimpleToolBarTemplate();
      // xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "pe.com.unifiedgo.report.ad_reports.ResumenCobrosJR");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
            "ResumenCobrosFilterJR.html", classInfo.id, classInfo.type, strReplaceWith,
            tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ResumenCobrosFilterJR.html",
            strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      {
        OBError myMessage = vars.getMessage("ResumenCobrosJR");
        vars.removeMessage("ResumenCobrosJR");
        if (myMessage != null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }

      xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("docDate", strDateFrom);
      xmlDocument.setParameter("docDatedisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("docDatesaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("proyDate", strDateTo);
      xmlDocument.setParameter("proyDatedisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("proyDatesaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

      Organization org;
      org = OBDal.getInstance().get(Organization.class, strAD_Org_ID);
      xmlDocument.setParameter("OrgId", strAD_Org_ID);
      xmlDocument.setParameter("OrgDescription", (org != null) ? org.getIdentifier() : "");

      xmlDocument.setParameter("paymentMethod", strPaymentMethodId);
      xmlDocument.setParameter("paymentMethodDescription",
          ResumenCobrosJRData.selectPaymentMethod(this, strPaymentMethodId));

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
            "5886F3AB694C4DBB9FEEFD54DDCEA501", "", Utility.getContext(this, vars,
                "#AccessibleOrgTree", "ResumenCobrosJR"), Utility.getContext(this, vars,
                "#User_Client", "ResumenCobrosJR"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ResumenCobrosFilterJR", "");
        xmlDocument.setData("reportPaymentType", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      xmlDocument.setParameter("PaymentType", strPaymentType);

      if (isFirstLoad) {
        xmlDocument.setParameter("FirstLoad", "FirstLoad = \"YES\"; ");
      } else {
        xmlDocument.setParameter("FirstLoad", "FirstLoad = \"NO\"; ");
      }

      // Print document in the output
      out.println(xmlDocument.print());
      out.close();
    }
  }

  public String getServletInfo() {
    return "Servlet PurchaseOrderFilter. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method

}
