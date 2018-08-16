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
package pe.com.unifiedgo.sales.ad_reports;

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
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class ReportBPDueDateDocumentsJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    // HttpSession session = request.getSession(true);
    // @SuppressWarnings("rawtypes")
    // Enumeration e = session.getAttributeNames();
    // while (e.hasMoreElements()) {
    // String name = (String) e.nextElement();
    // System.out.println("name: " + name + " - value: " +
    // session.getAttribute(name));
    // }

    if (vars.commandIn("DEFAULT")) {
      String strDateTo = SREDateTimeData.today(this);
      String strOrgId = vars.getGlobalVariable("inpOrgId", "ReportBPDueDateDocumentsJR|OrgId", "");
      String strcBpartnetId = vars.getGlobalVariable("inpcBPartnerId",
          "ReportBPDueDateDocumentsJR|CB_PARTNER_ID", "");
      String strShowAllDueDocuments = vars.getGlobalVariable("inpShowAllDueDocuments",
          "ReportBPDueDateDocumentsJR|ShowAllDueDocuments", "");

      printPageDataSheet(request, response, vars, strDateTo, strOrgId, strcBpartnetId,
          strShowAllDueDocuments);

    } else if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
      String strDateTo = SREDateTimeData.today(this);
      String strOrgId = vars.getStringParameter("inpOrgId");
      String strcBpartnetId = vars.getStringParameter("inpcBPartnerId");
      String strShowAllDueDocuments = vars.getStringParameter("inpShowAllDueDocuments");

      printPageDataSheet(request, response, vars, strDateTo, strOrgId, strcBpartnetId,
          strShowAllDueDocuments);

    } else if (vars.commandIn("PDF", "XLS")) {
      if (log4j.isDebugEnabled())
        log4j.debug("PDF");
      String strDateTo = SREDateTimeData.today(this);
      String strOrgId = vars.getStringParameter("inpOrgId");
      String strcBpartnetId = vars.getGlobalVariable("inpcBPartnerId",
          "ReportBPDueDateDocumentsJR|CB_PARTNER_ID", "");
      String strShowAllDueDocuments = vars.getStringParameter("inpShowAllDueDocuments");

      if (vars.commandIn("PDF")) {
        printPagePDF(request, response, vars, strDateTo, strOrgId, strcBpartnetId,
            strShowAllDueDocuments);
      }

      if (vars.commandIn("XLS")) {
        printPageXLS(request, response, vars, strDateTo, strOrgId, strcBpartnetId,
            strShowAllDueDocuments);
      }

    } else
      pageError(response);
  }

  private void printPagePDF(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDateTo, String strOrgId, String strcBpartnetId,
      String strShowAllDueDocuments) throws IOException,
      ServletException {
    response.setContentType("text/html; charset=UTF-8");

    ReportBPDueDateDocumentsJRData[] data = null;

    BusinessPartner bpartner = OBDal.getInstance().get(BusinessPartner.class, strcBpartnetId);
    if (bpartner == null)
      throw new ServletException("BPartnerNotFound");


    try {
      data = ReportBPDueDateDocumentsJRData.select(this, Utility.getContext(this, vars,
          "#User_Client", null), Utility.getContext(this, vars, "#User_Org", null), ("Y"
          .equals(strShowAllDueDocuments) ? "Y" : "N"), strDateTo , bpartner.getTaxID(),
          strOrgId);

    } catch (OBException e) {
      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "FINPR_NoConversionFound", vars.getLanguage()) + e.getMessage());
    }

    if (data != null && data.length == 1 && data[0] == null) {

      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "FINPR_NoDataFound", vars.getLanguage()));

    }

    String strReportName = "@basedesign@/pe/com/unifiedgo/sales/ad_reports/Rpt_BPDueDateDocumentsJR.jrxml";
    response.setHeader("Content-disposition", "inline; filename=BPDueDateDocumentsExcel.xls");

    Date document_date = new Date();
    String str_document_date = document_date.getDate() + "-" + document_date.getMonth() + "-"
        + document_date.getYear();
    String strSubTitle = "";
    strSubTitle = Utility.messageBD(this, "Document Date", vars.getLanguage()) + " "
        + str_document_date;

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("REPORT_SUBTITLE", strSubTitle);

    parameters.put("DESDE", StringToDate(strDateTo)); // no hay ya fecha desde
    parameters.put("HASTA", StringToDate(strDateTo));
    parameters.put("BPARTNER", bpartner.getTaxID() + " - " + bpartner.getName());

    if (data != null) {
      renderJR(vars, response, strReportName, "pdf", parameters, data, null);
    }
  }

  private void printPageXLS(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDateTo, String strOrgId, String strcBpartnetId,
      String strShowAllDueDocuments) throws IOException,
      ServletException {
    response.setContentType("text/html; charset=UTF-8");

    ReportBPDueDateDocumentsJRData[] data = null;

    BusinessPartner bpartner = OBDal.getInstance().get(BusinessPartner.class, strcBpartnetId);
    if (bpartner == null)
      throw new ServletException("BPartnerNotFound");

    try {
      data = ReportBPDueDateDocumentsJRData.select(this, Utility.getContext(this, vars,
          "#User_Client", null), Utility.getContext(this, vars, "#User_Org", null), ("Y"
          .equals(strShowAllDueDocuments) ? "Y" : "N"), strDateTo, bpartner.getTaxID(),
          strOrgId);

    } catch (OBException e) {
      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "FINPR_NoConversionFound", vars.getLanguage()) + e.getMessage());
    }

    if (data != null && data.length == 1 && data[0] == null) {

      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "FINPR_NoDataFound", vars.getLanguage()));

    }

    String strReportName = "@basedesign@/pe/com/unifiedgo/sales/ad_reports/Rpt_BPDueDateDocumentsJRExcel.jrxml";
    response.setHeader("Content-disposition", "inline; filename=BPDueDateDocumentsExcel.xls");

    Date document_date = new Date();
    String str_document_date = document_date.getDate() + "-" + document_date.getMonth() + "-"
        + document_date.getYear();
    String strSubTitle = "";
    strSubTitle = Utility.messageBD(this, "Document Date", vars.getLanguage()) + " "
        + str_document_date;

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("REPORT_SUBTITLE", strSubTitle);

    parameters.put("DESDE", StringToDate(strDateTo)); // no hay ya fecha desde
    parameters.put("HASTA", StringToDate(strDateTo));
    parameters.put("BPARTNER", bpartner.getTaxID() + " - " + bpartner.getName());

    if (data != null) {
      renderJR(vars, response, strReportName, "xls", parameters, data, null);
    }
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
      VariablesSecureApp vars, String strDateTo, String strOrgId, String strcBpartnetId,
      String strShowAllDueDocuments) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReportBPDueDateDocumentsJRData[] data = null;
    String strConvRateErrorMsg = "";
    BusinessPartner bpartner = null;

    String discard[] = { "discard" };

    xmlDocument = xmlEngine.readXmlTemplate(
        "pe/com/unifiedgo/sales/ad_reports/ReportBPDueDateDocumentsFilterJR", discard)
        .createXmlDocument();

    if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
      OBError myMessage = null;
      myMessage = new OBError();

      bpartner = OBDal.getInstance().get(BusinessPartner.class, strcBpartnetId);
      if (bpartner == null)
        throw new ServletException("BPartnerNotFound");

      try {
        data = ReportBPDueDateDocumentsJRData.select(this, Utility.getContext(this, vars,
            "#User_Client", null), Utility.getContext(this, vars, "#User_Org", null), ("Y"
            .equals(strShowAllDueDocuments) ? "Y" : "N"), strDateTo, bpartner.getTaxID(), strOrgId);

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
          data = ReportBPDueDateDocumentsJRData.set();
        } else {
          xmlDocument.setData("structure1", data);
        }
      }
    } else {

      if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
        discard[0] = "selEliminar";
        data = ReportBPDueDateDocumentsJRData.set();
      }
    }

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {

      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportBPDueDateDocumentsFilterJR",
          false, "", "", "printPDF(); return false;", false, "ad_reports", strReplaceWith, false,
          true);
      toolbar.prepareSimpleToolBarTemplate();
      toolbar.prepareRelationBarTemplate(false, false, "printXLS(); return false;");
      xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "pe.com.unifiedgo.sales.ad_reports.ReportBPDueDateDocumentsJR");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
            "ReportBPDueDateDocumentsFilterJR.html", classInfo.id, classInfo.type, strReplaceWith,
            tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
            "ReportBPDueDateDocumentsFilterJR.html", strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      {
        OBError myMessage = vars.getMessage("ReportBPDueDateDocumentsJR");
        vars.removeMessage("ReportBPDueDateDocumentsJR");
        if (myMessage != null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }

      Organization org;
      org = OBDal.getInstance().get(Organization.class, strOrgId);
      xmlDocument.setParameter("OrgId", strOrgId);
      xmlDocument.setParameter("OrgDescription", (org != null) ? org.getIdentifier() : "");

      xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("dateTo", strDateTo);
      xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("paramBPartnerId", strcBpartnetId);

      xmlDocument.setParameter("paramBPartnerDescription",
          ReportBPDueDateDocumentsJRData.selectBpartner(this, strcBpartnetId));

      xmlDocument.setParameter("showAllDueDocuments", strShowAllDueDocuments);

      // Print document in the output
      out.println(xmlDocument.print());
      out.close();
    }
  }

  public String getServletInfo() {
    return "Servlet PurchaseOrderFilter. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method

}
