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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.businessUtility.AccountingSchemaMiscData;
import org.openbravo.erpCommon.businessUtility.Tree;
import org.openbravo.erpCommon.businessUtility.TreeData;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchemaTable;
import org.openbravo.xmlEngine.XmlDocument;

public class ReportDiffInvoiceGS extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (log4j.isDebugEnabled())
      log4j.debug("Command: " + vars.getStringParameter("Command"));

    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars
          .getGlobalVariable("inpDateFrom", "ReportDiffInvoiceGS|DateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportDiffInvoiceGS|DateTo", "");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportDiffInvoiceGS|Org", "0");
      String strRecord = vars.getGlobalVariable("inpRecord", "ReportDiffInvoiceGS|Record", "");
      String strTable = vars.getGlobalVariable("inpTable", "ReportDiffInvoiceGS|Table", "");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg, strTable, strRecord);

    } else if (vars.commandIn("DIRECT")) {
      String strTable = vars.getGlobalVariable("inpTable", "ReportDiffInvoiceGS|Table");
      String strRecord = vars.getGlobalVariable("inpRecord", "ReportDiffInvoiceGS|Record");

      setHistoryCommand(request, "DIRECT");
      vars.setSessionValue("ReportDiffInvoiceGS.initRecordNumber", "0");
      printPageDataSheet(response, vars, "", "", "", strTable, strRecord);

    } else if (vars.commandIn("FIND")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportDiffInvoiceGS|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportDiffInvoiceGS|DateTo");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportDiffInvoiceGS|Org", "0");
      vars.setSessionValue("ReportDiffInvoiceGS.initRecordNumber", "0");
      setHistoryCommand(request, "DEFAULT");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg, "", "");

    } else if (vars.commandIn("PDF", "XLS")) {
      if (log4j.isDebugEnabled())
        log4j.debug("PDF");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportDiffInvoiceGS|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportDiffInvoiceGS|DateTo");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportDiffInvoiceGS|Org", "0");

      String strTable = vars.getStringParameter("inpTable");
      String strRecord = vars.getStringParameter("inpRecord");
      setHistoryCommand(request, "DEFAULT");
      printPagePDF(response, vars, strDateFrom, strDateTo, strOrg, strTable, strRecord);

    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strOrg, String strTable, String strRecord)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReportDiffInvoiceGSData[] data = null;
    String strPosition = "0";
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportDiffInvoiceGS", false, "", "",
        "imprimir();return false;", false, "ad_reports", strReplaceWith, false, true);
    toolbar.setEmail(false);
    if (data == null || data.length == 0) {
      String discard[] = { "secTable" };
      toolbar
          .prepareRelationBarTemplate(false, false,
              "submitCommandForm('XLS', false, null, 'ReportDiffInvoiceGS.xls', 'EXCEL');return false;");
      xmlDocument = xmlEngine.readXmlTemplate(
          "pe/com/unifiedgo/report/ad_reports/ReportDiffInvoiceGS", discard).createXmlDocument();
      data = ReportDiffInvoiceGSData.set("0");
      data[0].rownum = "0";
    } else {

      // data = notshow(data, vars);

      toolbar
          .prepareRelationBarTemplate(true, true,
              "submitCommandForm('XLS', false, null, 'ReportDiffInvoiceGS.xls', 'EXCEL');return false;");
      xmlDocument = xmlEngine.readXmlTemplate(
          "pe/com/unifiedgo/report/ad_reports/ReportDiffInvoiceGS").createXmlDocument();
    }
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "pe.com.unifiedgo.report.ad_reports.ReportDiffInvoiceGS");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportDiffInvoiceGS.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportDiffInvoiceGS.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportDiffInvoiceGS");
      vars.removeMessage("ReportDiffInvoiceGS");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
          "", Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportDiffInvoiceGS"),
          Utility.getContext(this, vars, "#User_Client", "ReportDiffInvoiceGS"), '*');
      comboTableData.fillParameters(null, "ReportDiffInvoiceGS", "");
      xmlDocument.setData("reportAD_ORGID", "liststructure", comboTableData.select(false));
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setData(
        "reportC_ACCTSCHEMA_ID",
        "liststructure",
        AccountingSchemaMiscData.selectC_ACCTSCHEMA_ID(this,
            Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportGeneralLedger"),
            Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"), ""));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("adOrgId", strOrg);
    xmlDocument.setParameter("groupId", strPosition);
    xmlDocument.setParameter("paramRecord", strRecord);
    xmlDocument.setParameter("paramTable", strTable);
    vars.setSessionValue("ReportDiffInvoiceGS|Record", strRecord);
    vars.setSessionValue("ReportDiffInvoiceGS|Table", strTable);

    xmlDocument.setData("structure1", data);
    out.println(xmlDocument.print());
    out.close();
  }

  // private ReportDiffInvoiceGSData[] notshow(ReportDiffInvoiceGSData[] data,
  // VariablesSecureApp vars) {
  // for (int i = 0; i < data.length - 1; i++) {
  // if ((data[i].identifier.toString().equals(data[i + 1].identifier.toString()))
  // && (data[i].dateacct.toString().equals(data[i + 1].dateacct.toString()))) {
  // data[i + 1].newstyle = "visibility: hidden";
  // }
  // }
  // return data;
  // }

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

  private void printPagePDF(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strOrg, String strTable, String strRecord)
      throws IOException, ServletException {

    ReportDiffInvoiceGSData[] data = null;

    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    String strOrgFamily = getFamily(strTreeOrg, strOrg);

    data = ReportDiffInvoiceGSData.selectDiff(this,
        Utility.getContext(this, vars, "#User_Client", "ReportDiffInvoiceGS"), strOrgFamily,
        strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"));
    ;

    String strSubtitle = (Utility.messageBD(this, "LegalEntity", vars.getLanguage()) + ": ")
        + ReportDiffInvoiceGSData.selectCompany(this, vars.getClient()) + "\n" + "RUC:"
        + ReportDiffInvoiceGSData.selectRucOrg(this, strOrg) + "\n";
    ;

    if (!("0".equals(strOrg)))
      strSubtitle += (Utility.messageBD(this, "OBUIAPP_Organization", vars.getLanguage()) + ": ")
          + ReportDiffInvoiceGSData.selectOrg(this, strOrg) + "\n";

    // if (!"".equals(strDateFrom) || !"".equals(strDateTo))
    // strSubtitle += (Utility.messageBD(this, "From", vars.getLanguage()) + ": ") + strDateFrom
    // + "  " + (Utility.messageBD(this, "OBUIAPP_To", vars.getLanguage()) + ": ") + strDateTo
    // + "\n";

    String strOutput;
    String strReportName;
    if (vars.commandIn("PDF")) {
      strOutput = "pdf";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportDiffInvoiceGS.jrxml";
    } else {
      strOutput = "xls";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportDiffInvoiceGSExcel.jrxml";
    }

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("Subtitle", strSubtitle);
    parameters.put("Ruc", ReportDiffInvoiceGSData.selectRucOrg(this, strOrg));
    parameters.put("Razon", ReportDiffInvoiceGSData.selectOrg(this, strOrg));
    // parameters.put("TaxID", ReportSalesRevenueRecordsData.selectOrgTaxID(this, strOrg));
    parameters.put("dateFrom", StringToDate(strDateFrom));
    parameters.put("dateTo", StringToDate(strDateTo));
    renderJR(vars, response, strReportName, "Diferencia Facturas Guias", strOutput, parameters,
        data, null);
//    renderJR(vars, response, strReportName, "Registro de Consignaciones", strOutput, parameters,
//            data, null);
  }

  public static Set<String> getDocuments(String org, String accSchema) {

    final StringBuilder whereClause = new StringBuilder();
    final List<Object> parameters = new ArrayList<Object>();
    OBContext.setAdminMode();
    try {
      // Set<String> orgStrct = OBContext.getOBContext().getOrganizationStructureProvider()
      // .getChildTree(org, true);
      Set<String> orgStrct = OBContext.getOBContext().getOrganizationStructureProvider()
          .getNaturalTree(org);
      whereClause.append(" as cd ,");
      whereClause.append(AcctSchemaTable.ENTITY_NAME);
      whereClause.append(" as ca ");
      whereClause.append(" where cd.");
      whereClause.append(DocumentType.PROPERTY_TABLE + ".id");
      whereClause.append("= ca.");
      whereClause.append(AcctSchemaTable.PROPERTY_TABLE + ".id");
      whereClause.append(" and ca.");
      whereClause.append(AcctSchemaTable.PROPERTY_ACCOUNTINGSCHEMA + ".id");
      whereClause.append(" = ? ");
      parameters.add(accSchema);
      whereClause.append("and ca.");
      whereClause.append(AcctSchemaTable.PROPERTY_ACTIVE + "='Y'");
      whereClause.append(" and cd.");
      whereClause.append(DocumentType.PROPERTY_ORGANIZATION + ".id");
      whereClause.append(" in (" + Utility.getInStrSet(orgStrct) + ")");
      whereClause.append(" and ca." + AcctSchemaTable.PROPERTY_ORGANIZATION + ".id");
      whereClause.append(" in (" + Utility.getInStrSet(orgStrct) + ")");
      whereClause.append(" order by cd." + DocumentType.PROPERTY_DOCUMENTCATEGORY);
      final OBQuery<DocumentType> obqDt = OBDal.getInstance().createQuery(DocumentType.class,
          whereClause.toString());
      obqDt.setParameters(parameters);
      obqDt.setFilterOnReadableOrganization(false);
      TreeSet<String> docBaseTypes = new TreeSet<String>();
      for (DocumentType doc : obqDt.list()) {
        docBaseTypes.add(doc.getDocumentCategory());
      }
      return docBaseTypes;

    } finally {
      OBContext.restorePreviousMode();
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

  @Override
  public String getServletInfo() {
    return "Servlet ReportDiffInvoiceGS. This Servlet was made by Pablo Sarobe modified by everybody";
  } // end of getServletInfo() method
}
