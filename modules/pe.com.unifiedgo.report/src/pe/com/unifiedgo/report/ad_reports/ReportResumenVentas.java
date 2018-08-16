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
 * All portions are Copyright (C) 2001-2014 Openbravo SLU
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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.Tree;
import org.openbravo.erpCommon.businessUtility.TreeData;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.OrganizationInformation;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class ReportResumenVentas extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (log4j.isDebugEnabled())
      log4j.debug("Command: " + vars.getStringParameter("Command"));

    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars.getGlobalVariable("inpDocDate", "ReportResumenVentas|docDate",
          SREDateTimeData.today(this));
      String strDateTo = vars.getGlobalVariable("inpProyDate", "ReportResumenVentas|proyDate",
          SREDateTimeData.tomorrow(this));
      String strAD_Org_ID = vars.getGlobalVariable("inpOrgId", "ReportResumenVentas|OrgId", "");
      String strcBpartnetId = vars.getGlobalVariable("inpcBPartnerId",
          "ReportResumenVentas|CB_PARTNER_ID", "");
      String strPaymentMethodId = vars.getGlobalVariable("inpPaymentMethod",
          "ReportResumenVentas|PaymentMethod", "");
      String strPaymentTermId = vars.getGlobalVariable("inpPaymentTerm",
          "ReportResumenVentas|PaymentTerm", "");
      String strInvoiceDocNo = vars.getGlobalVariable("inpInvoiceDocNo",
          "ReportResumenVentas|InvoiceDocNo", "");
      String strShipmentDocNo = vars.getGlobalVariable("inpShipmentDocNo",
          "ReportResumenVentas|ShipmentDocNo", "");
      String strOCClientDocNo = vars.getGlobalVariable("inpOCClientDocNo",
          "ReportResumenVentas|OCClientDocNo", "");
      String strRecord = vars.getGlobalVariable("inpRecord", "ReportResumenVentas|Record", "");
      String strTable = vars.getGlobalVariable("inpTable", "ReportResumenVentas|Table", "");

      printPageDataSheet(response, vars, strDateFrom, strDateTo, strAD_Org_ID, strcBpartnetId,
          strPaymentMethodId, strPaymentTermId, strInvoiceDocNo, strShipmentDocNo,
          strOCClientDocNo, strTable, strRecord, true);

    } else if (vars.commandIn("DIRECT")) {
      String strTable = vars.getGlobalVariable("inpTable", "ReportResumenVentas|Table");
      String strRecord = vars.getGlobalVariable("inpRecord", "ReportResumenVentas|Record");

      setHistoryCommand(request, "DIRECT");
      vars.setSessionValue("ReportResumenVentas.initRecordNumber", "0");
      printPageDataSheet(response, vars, "", "", "", "", "", "", "", "", "", strTable, strRecord,
          false);

    } else if (vars.commandIn("FIND")) {
      String strDateFrom = vars.getStringParameter("inpDocDate");
      String strDateTo = vars.getStringParameter("inpProyDate");
      String strAD_Org_ID = vars.getStringParameter("inpOrgId");
      String strcBpartnetId = vars.getStringParameter("inpcBPartnerId");
      String strPaymentMethodId = vars.getStringParameter("inpPaymentMethod");
      String strPaymentTermId = vars.getStringParameter("inpPaymentTerm");
      String strInvoiceDocNo = vars.getStringParameter("inpInvoiceDocNo");
      String strShipmentDocNo = vars.getStringParameter("inpShipmentDocNo");
      String strOCClientDocNo = vars.getStringParameter("inpOCClientDocNo");

      vars.setSessionValue("ReportResumenVentas.initRecordNumber", "0");
      setHistoryCommand(request, "DEFAULT");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strAD_Org_ID, strcBpartnetId,
          strPaymentMethodId, strPaymentTermId, strInvoiceDocNo, strShipmentDocNo,
          strOCClientDocNo, "", "", false);

    } else if (vars.commandIn("PDF", "XLS")) {
      if (log4j.isDebugEnabled())
        log4j.debug("PDF");

      String strAD_Org_ID = vars.getStringParameter("inpOrgId");
      String strDateFrom = vars.getGlobalVariable("inpDocDate", "ReportResumenVentas|docDate");
      String strDateTo = vars.getGlobalVariable("inpProyDate", "ReportResumenVentas|proyDate");
      String strcBpartnetId = vars.getStringParameter("inpcBPartnerId");
      String strPaymentMethodId = vars.getStringParameter("inpPaymentMethod");
      String strPaymentTermId = vars.getStringParameter("inpPaymentTerm");
      String strInvoiceDocNo = vars.getStringParameter("inpInvoiceDocNo");
      String strShipmentDocNo = vars.getStringParameter("inpShipmentDocNo");
      String strOCClientDocNo = vars.getStringParameter("inpOCClientDocNo");
      // System.out.println("strDateFrom:" + strDateFrom + " / strDateTo:" + strDateTo
      // + " / strAD_Org_ID:" + strAD_Org_ID + " / strcBpartnetId:" + strcBpartnetId
      // + " / strPaymentType:" + strPaymentType + " / strInvoiceDocNo:" + strInvoiceDocNo
      // + " / strShipmentDocNo:" + strShipmentDocNo + " / strOCClientDocNo:" + strOCClientDocNo);
      String strTable = vars.getStringParameter("inpTable");
      String strRecord = vars.getStringParameter("inpRecord");
      setHistoryCommand(request, "DEFAULT");
      printPagePDF(response, vars, strAD_Org_ID, strDateFrom, strDateTo, strcBpartnetId,
          strPaymentMethodId, strPaymentTermId, strInvoiceDocNo, strShipmentDocNo,
          strOCClientDocNo, strTable, strRecord, false);

    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strOrgId, String strcBpartnetId,
      String strPaymentMethodId, String strPaymentTermId, String strInvoiceDocNo,
      String strShipmentDocNo, String strOCClientDocNo, String strTable, String strRecord,
      boolean isFirstLoad) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    String discard[] = { "secTable" };
    ReportResumenVentasData[] data = null;

    xmlDocument = xmlEngine.readXmlTemplate(
        "pe/com/unifiedgo/report/ad_reports/ReportResumenVentas", discard).createXmlDocument();

    String strPosition = "0";
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportResumenVentas", false, "", "",
        "imprimirPDF();return false;", false, "ad_reports", strReplaceWith, false, true);
    toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");

    toolbar.setEmail(false);
    // if (data == null || data.length == 0) {
    // String discard[] = { "secTable" };
    // toolbar
    // .prepareRelationBarTemplate(false, false,
    // "submitCommandForm('XLS', false, null, 'ReportResumenVentas.xls', 'EXCEL');return false;");
    // xmlDocument = xmlEngine.readXmlTemplate(
    // "pe/com/unifiedgo/report/ad_reports/ReportResumenVentas", discard).createXmlDocument();
    // data = ReportResumenVentasData.set("0");
    // data[0].rownum = "0";
    // } else {
    //
    // // data = notshow(data, vars);
    //
    // toolbar
    // .prepareRelationBarTemplate(true, true,
    // "submitCommandForm('XLS', false, null, 'ReportResumenVentas.xls', 'EXCEL');return false;");
    // xmlDocument = xmlEngine.readXmlTemplate(
    // "pe/com/unifiedgo/report/ad_reports/ReportResumenVentas").createXmlDocument();
    // }
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "pe.com.unifiedgo.report.ad_reports.ReportResumenVentas");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportResumenVentas.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportResumenVentas.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportResumenVentas");
      vars.removeMessage("ReportResumenVentas");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("docDate", strDateFrom);
    xmlDocument.setParameter("docDatedisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("docDatesaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("proyDate", strDateTo);
    xmlDocument.setParameter("proyDatedisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("proyDatesaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("groupId", strPosition);
    xmlDocument.setParameter("paramRecord", strRecord);
    xmlDocument.setParameter("paramTable", strTable);
    vars.setSessionValue("ReportResumenVentas|Record", strRecord);
    vars.setSessionValue("ReportResumenVentas|Table", strTable);

    Organization org;
    org = OBDal.getInstance().get(Organization.class, strOrgId);
    xmlDocument.setParameter("OrgId", strOrgId);
    xmlDocument.setParameter("OrgDescription", (org != null) ? org.getIdentifier() : "");

    xmlDocument.setParameter("paramBPartnerId", strcBpartnetId);
    // xmlDocument.setParameter("paramBPartnerDescription",
    // ReportResumenVentasData.selectBpartner(this, strcBpartnetId));

    xmlDocument.setParameter("paymentMethod", strPaymentMethodId);
    xmlDocument.setParameter("paymentMethodDescription",
        ReportResumenVentasData.selectPaymentMethod(this, strPaymentMethodId));

    xmlDocument.setParameter("paymentTerm", strPaymentTermId);
    xmlDocument.setParameter("paymentTermDescription",
        ReportResumenVentasData.selectPaymentTerm(this, strPaymentTermId));

    xmlDocument.setParameter("InvoiceDocNo", strInvoiceDocNo);
    xmlDocument.setParameter("ShipmentDocNo", strShipmentDocNo);
    xmlDocument.setParameter("OCClientDocNo", strOCClientDocNo);

    if (isFirstLoad) {
      xmlDocument.setParameter("FirstLoad", "FirstLoad = \"YES\"; ");
    } else {
      xmlDocument.setParameter("FirstLoad", "FirstLoad = \"NO\"; ");
    }

    xmlDocument.setData("structure1", data);
    out.println(xmlDocument.print());
    out.close();
  }

  private String getFamily(String strTree, String strChild) throws IOException, ServletException {
    return Tree.getMembers(this, strTree, (strChild == null || strChild.equals("")) ? "0"
        : strChild);
    /*
     * ReportGeneralLedgerData [] data = ReportGeneralLedgerData.selectChildren(this, strTree,
     * strChild); String strFamily = ""; if(data!=null && data.length>0) { for (int i =
     * 0;i<data.length;i++){ if (i>0) strFamily = strFamily + ","; strFamily = strFamily +
     * data[i].id; } return strFamily += ""; }else return "'1'";
     */
  }

  private void printPagePDF(HttpServletResponse response, VariablesSecureApp vars, String strOrg,
      String strDateFrom, String strDateTo, String strcBpartnetId, String strPaymentMethodId,
      String strPaymentTermId, String strInvoiceDocNo, String strShipmentDocNo,
      String strOCClientDocNo, String strTable, String strRecord, boolean isFirstLoad)
      throws IOException, ServletException {

    ReportResumenVentasData[] data = null;

    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    String strOrgFamily = getFamily(strTreeOrg, strOrg);

    System.out.println(vars.getSessionValue("#AD_CLIENT_ID"));
    System.out.println(strOrg);
    System.out.println(strDateFrom);
    System.out.println(strDateTo);
    System.out.println(vars.getSessionValue("#AD_LANGUAGE"));

    data = ReportResumenVentasData.selectVentas(this, "'" + vars.getSessionValue("#AD_LANGUAGE")
        + "'", "'" + vars.getSessionValue("#AD_LANGUAGE") + "'", strOrg,
        vars.getSessionValue("#AD_CLIENT_ID"), strDateFrom, strDateTo, strcBpartnetId,
        strPaymentMethodId, strPaymentTermId, strInvoiceDocNo, strShipmentDocNo, strOCClientDocNo);
    ;

    String strSubtitle = (Utility.messageBD(this, "LegalEntity", vars.getLanguage()) + ": ")
        + ReportResumenVentasData.selectCompany(this, vars.getClient()) + "\n" + "RUC:"
        + ReportResumenVentasData.selectRucOrg(this, strOrg) + "\n";
    ;

    if (!("0".equals(strOrg)))
      strSubtitle += (Utility.messageBD(this, "OBUIAPP_Organization", vars.getLanguage()) + ": ")
          + ReportResumenVentasData.selectOrg(this, strOrg) + "\n";

    // if (!"".equals(strDateFrom) || !"".equals(strDateTo))
    // strSubtitle += (Utility.messageBD(this, "From", vars.getLanguage()) + ": ") + strDateFrom
    // + "  " + (Utility.messageBD(this, "OBUIAPP_To", vars.getLanguage()) + ": ") + strDateTo
    // + "\n";

    String strOutput;
    String strReportName;
    if (vars.commandIn("PDF")) {
      strOutput = "pdf";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportResumenVentas.jrxml";
    } else {
      strOutput = "xls";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportResumenVentasExcel.jrxml";
    }

    HashMap<String, Object> parameters = new HashMap<String, Object>();

    OBCriteria<Organization> organization = OBDal.getInstance().createCriteria(Organization.class);
    organization.add(Restrictions.eq(Organization.PROPERTY_ID, strOrg));

    List<Organization> OrganizationList = organization.list();

    parameters.put("ORGANIZACION", OrganizationList.get(0).getSocialName());

    OBCriteria<OrganizationInformation> orginfo = OBDal.getInstance().createCriteria(
        OrganizationInformation.class);
    orginfo.add(Restrictions.eq(OrganizationInformation.PROPERTY_ID, strOrg));

    List<OrganizationInformation> orginfoList = orginfo.list();

    parameters.put("RUCORGANIZACION", orginfoList.get(0).getTaxID());

    parameters.put("Subtitle", strSubtitle);

    parameters.put("Razon", ReportResumenVentasData.selectCompany(this, vars.getClient()));
    parameters.put("dateFrom", StringToDate(strDateFrom));
    parameters.put("dateTo", StringToDate(strDateTo));
    renderJR(vars, response, strReportName, "ReporteDiarioVentas", strOutput, parameters, data,
        null);
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

  @Override
  public String getServletInfo() {
    return "Servlet ReportResumenVentas. This Servlet was made by Pablo Sarobe modified by everybody";
  } // end of getServletInfo() method
}
