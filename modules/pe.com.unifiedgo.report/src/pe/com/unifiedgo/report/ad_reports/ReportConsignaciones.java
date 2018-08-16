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
import java.math.BigDecimal;
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

import org.openbravo.base.filter.IsIDFilter;
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
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchemaTable;
import org.openbravo.xmlEngine.XmlDocument;

public class ReportConsignaciones extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (log4j.isDebugEnabled())
      log4j.debug("Command: " + vars.getStringParameter("Command"));

    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReportConsignaciones|DateFrom",
          "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportConsignaciones|DateTo", "");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportConsignaciones|Org", "0");
      String strRecord = vars.getGlobalVariable("inpRecord", "ReportConsignaciones|Record", "");
      String strTable = vars.getGlobalVariable("inpTable", "ReportConsignaciones|Table", "");
      String strPeriodId = vars.getGlobalVariable("inpPeriodId", "ReportConsignaciones|PeriodId",
          "", IsIDFilter.instance);
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg, strPeriodId, strTable,
          strRecord);

    } else if (vars.commandIn("DIRECT")) {
      String strTable = vars.getGlobalVariable("inpTable", "ReportConsignaciones|Table");
      String strRecord = vars.getGlobalVariable("inpRecord", "ReportConsignaciones|Record");
      String strPeriodId = vars.getGlobalVariable("inpPeriodId", "ReportConsignaciones|PeriodId",
          "", IsIDFilter.instance);

      setHistoryCommand(request, "DIRECT");
      vars.setSessionValue("ReportConsignaciones.initRecordNumber", "0");
      printPageDataSheet(response, vars, "", "", "", strPeriodId, strTable, strRecord);

    } else if (vars.commandIn("FIND")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportConsignaciones|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportConsignaciones|DateTo");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportConsignaciones|Org", "0");
      String strPeriodId = vars.getGlobalVariable("inpPeriodId", "ReportConsignaciones|PeriodId",
          "", IsIDFilter.instance);
      vars.setSessionValue("ReportConsignaciones.initRecordNumber", "0");
      setHistoryCommand(request, "DEFAULT");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg, strPeriodId, "", "");

    } else if (vars.commandIn("PDF", "XLS")) {
      if (log4j.isDebugEnabled())
        log4j.debug("PDF");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportConsignaciones|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportConsignaciones|DateTo");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportConsignaciones|Org", "0");
      String strPeriodId = vars.getGlobalVariable("inpPeriodId", "ReportConsignaciones|PeriodId",
          "", IsIDFilter.instance);

      String strTable = vars.getStringParameter("inpTable");
      String strRecord = vars.getStringParameter("inpRecord");
      setHistoryCommand(request, "DEFAULT");
      printPagePDF(response, vars, strDateFrom, strDateTo, strOrg, strPeriodId, strTable, strRecord);

    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strOrg, String strPeriodId, String strTable,
      String strRecord) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReportConsignacionesData[] data = null;
    String strPosition = "0";
    String strConvRateErrorMsg = "";

    String discard[] = { "secTable" };

    if (data == null || data.length == 0) {
      xmlDocument = xmlEngine.readXmlTemplate(
          "pe/com/unifiedgo/report/ad_reports/ReportConsignaciones", discard).createXmlDocument();
      data = ReportConsignacionesData.set("0");
      data[0].rownum = "0";
    } else {
      // data = notshow(data, vars);
      xmlDocument = xmlEngine.readXmlTemplate(
          "pe/com/unifiedgo/report/ad_reports/ReportConsignaciones").createXmlDocument();
    }

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportConsignaciones", false, "", "",
        "printPDF();return false;", false, "ad_reports", strReplaceWith, false, true);
    toolbar.setEmail(false);
    toolbar.prepareSimpleToolBarTemplate();
    toolbar.prepareRelationBarTemplate(false, false, "printXLS();return false;");
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "pe.com.unifiedgo.report.ad_reports.ReportConsignaciones");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportConsignaciones.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportConsignaciones.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportConsignaciones");
      vars.removeMessage("ReportConsignaciones");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
          "", Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportConsignaciones"),
          Utility.getContext(this, vars, "#User_Client", "ReportConsignaciones"), '*');
      comboTableData.fillParameters(null, "ReportConsignaciones", "");
      xmlDocument.setData("reportAD_ORGID", "liststructure", comboTableData.select(false));
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setParameter("periodId", strPeriodId);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "",
          "C_Period (Open)", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReportConsignaciones"), Utility.getContext(this, vars, "#User_Client",
              "ReportConsignaciones"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportConsignaciones",
          strPeriodId);
      xmlDocument.setData("reportPeriod_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
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
    vars.setSessionValue("ReportConsignaciones|Record", strRecord);
    vars.setSessionValue("ReportConsignaciones|Table", strTable);

    xmlDocument.setData("structure1", data);
    out.println(xmlDocument.print());
    out.close();
  }

  // private ReportConsignacionesData[] notshow(ReportConsignacionesData[] data,
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
      String strDateFrom, String strDateTo, String strOrg, String strPeriodId, String strTable,
      String strRecord) throws IOException, ServletException {

    ReportConsignacionesData[] data = null;
    ReportConsignacionesData[] periodo = null;
    ReportConsignacionesData[] dataTmp = null;

    periodo = ReportConsignacionesData.selectPeriodo(this, strPeriodId);

    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    String strOrgFamily = getFamily(strTreeOrg, strOrg);

    data = ReportConsignacionesData.selectConsignacion(this,
        Utility.getContext(this, vars, "#User_Client", "ReportConsignaciones"), strOrgFamily,
        Utility.getContext(this, vars, "#User_Client", "ReportConsignaciones"), strOrgFamily,
        periodo[0].desde, periodo[0].hasta);

    Date fechaIni = new Date();
    Date fechaFin = new Date();

    fechaIni = pe.com.unifiedgo.report.common.Utility.ParseFecha(periodo[0].desde, "dd-MM-yyyy");
    fechaFin = pe.com.unifiedgo.report.common.Utility.ParseFecha(periodo[0].hasta, "dd-MM-yyyy");
    for (int i = 0; i < data.length; i++) {

      dataTmp = ReportConsignacionesData.selectSemana(this, data[i].productoid,
          data[i].warehouseid,
          Utility.getContext(this, vars, "#User_Client", "ReportConsignaciones"), strOrgFamily);
      BigDecimal suma1 = new BigDecimal(0);
      BigDecimal suma2 = new BigDecimal(0);
      BigDecimal suma3 = new BigDecimal(0);
      BigDecimal suma4 = new BigDecimal(0);

      for (int j = 0; j < dataTmp.length; j++) {
        Date fechaTmp = pe.com.unifiedgo.report.common.Utility.ParseFecha(dataTmp[j].fecha,
            "dd-MM-yyyy");
        if (fechaTmp.after(fechaIni)
            && fechaTmp.before(pe.com.unifiedgo.report.common.Utility.sumarRestarDiasFecha(
                fechaIni, 7))) {
          suma1 = suma1.add(new BigDecimal(dataTmp[j].montosemanal));
        }
        if (fechaTmp
            .after(pe.com.unifiedgo.report.common.Utility.sumarRestarDiasFecha(fechaIni, 7))
            && fechaTmp.before(pe.com.unifiedgo.report.common.Utility.sumarRestarDiasFecha(
                fechaIni, 14))) {
          suma2 = suma2.add(new BigDecimal(dataTmp[j].montosemanal));
        }
        if (fechaTmp.after(pe.com.unifiedgo.report.common.Utility
            .sumarRestarDiasFecha(fechaIni, 14))
            && fechaTmp.before(pe.com.unifiedgo.report.common.Utility.sumarRestarDiasFecha(
                fechaIni, 21))) {
          suma3 = suma3.add(new BigDecimal(dataTmp[j].montosemanal));
        }
        if (fechaTmp.after(pe.com.unifiedgo.report.common.Utility
            .sumarRestarDiasFecha(fechaIni, 21)) && fechaTmp.before(fechaFin)) {
          suma4 = suma4.add(new BigDecimal(dataTmp[j].montosemanal));
        }
      }
      data[i].semana1 = suma1.toString();
      data[i].semana2 = suma2.toString();
      data[i].semana3 = suma3.toString();
      data[i].semana4 = suma4.toString();

    }

    String strSubtitle = (Utility.messageBD(this, "LegalEntity", vars.getLanguage()) + ": ")
        + ReportConsignacionesData.selectCompany(this, vars.getClient()) + "\n" + "RUC:"
        + ReportConsignacionesData.selectRucOrg(this, strOrg) + "\n";
    ;

    if (!("0".equals(strOrg)))
      strSubtitle += (Utility.messageBD(this, "OBUIAPP_Organization", vars.getLanguage()) + ": ")
          + ReportConsignacionesData.selectOrg(this, strOrg) + "\n";

    // if (!"".equals(strDateFrom) || !"".equals(strDateTo))
    // strSubtitle += (Utility.messageBD(this, "From", vars.getLanguage()) + ": ") + strDateFrom
    // + "  " + (Utility.messageBD(this, "OBUIAPP_To", vars.getLanguage()) + ": ") + strDateTo
    // + "\n";

    String strOutput;
    String strReportName;
    if (vars.commandIn("PDF")) {
      strOutput = "pdf";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportConsignaciones.jrxml";
    } else {
      strOutput = "xls";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportConsignacionesExcel.jrxml";
    }

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("Subtitle", strSubtitle);
    // parameters.put("TaxID", ReportSalesRevenueRecordsData.selectOrgTaxID(this, strOrg));
    parameters.put("dateFrom", fechaIni);
    parameters.put("dateTo", fechaFin);
    parameters.put("totalLines", data.length);
    renderJR(vars, response, strReportName, "Reporte Consignaciones", strOutput, parameters, data,
        null);
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
    return "Servlet ReportConsignaciones. This Servlet was made by Pablo Sarobe modified by everybody";
  } // end of getServletInfo() method

  private String IntervaloSemana(String fecha) {
    Date date = pe.com.unifiedgo.report.common.Utility.ParseFecha(fecha, "yyyy-MM-dd");
    date = pe.com.unifiedgo.report.common.Utility.sumarRestarDiasFecha(date, 7);
    String strDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
    return strDate;
  }
}
