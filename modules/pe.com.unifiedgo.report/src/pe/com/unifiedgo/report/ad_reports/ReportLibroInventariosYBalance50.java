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

import org.openbravo.base.secureApp.HttpSecureAppServlet;

public class ReportLibroInventariosYBalance50 extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  // @Override
  // public void doPost(HttpServletRequest request, HttpServletResponse response) throws
  // IOException,
  // ServletException {
  // VariablesSecureApp vars = new VariablesSecureApp(request);
  //
  // if (log4j.isDebugEnabled())
  // log4j.debug("Command: " + vars.getStringParameter("Command"));
  //
  // if (vars.commandIn("DEFAULT")) {
  // String strDateFrom = vars.getGlobalVariable("inpDateFrom",
  // "ReportLibroInventariosYBalance50|DateFrom", "");
  // String strDateTo = vars.getGlobalVariable("inpDateTo",
  // "ReportLibroInventariosYBalance50|DateTo", "");
  // String strOrg = vars.getGlobalVariable("inpOrg", "ReportLibroInventariosYBalance50|Org", "0");
  // String strRecord = vars.getGlobalVariable("inpRecord",
  // "ReportLibroInventariosYBalance50|Record", "");
  // String strTable = vars.getGlobalVariable("inpTable", "ReportLibroInventariosYBalance50|Table",
  // "");
  // printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg, strTable, strRecord);
  //
  // } else if (vars.commandIn("DIRECT")) {
  // String strTable = vars.getGlobalVariable("inpTable", "ReportLibroInventariosYBalance50|Table");
  // String strRecord = vars.getGlobalVariable("inpRecord",
  // "ReportLibroInventariosYBalance50|Record");
  //
  // setHistoryCommand(request, "DIRECT");
  // vars.setSessionValue("ReportLibroInventariosYBalance50.initRecordNumber", "0");
  // printPageDataSheet(response, vars, "", "", "", strTable, strRecord);
  //
  // } else if (vars.commandIn("FIND")) {
  // String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
  // "ReportLibroInventariosYBalance50|DateFrom");
  // String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
  // "ReportLibroInventariosYBalance50|DateTo");
  // String strOrg = vars.getGlobalVariable("inpOrg", "ReportLibroInventariosYBalance50|Org", "0");
  // vars.setSessionValue("ReportLibroInventariosYBalance50.initRecordNumber", "0");
  // setHistoryCommand(request, "DEFAULT");
  // printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg, "", "");
  //
  // } else if (vars.commandIn("PDF", "XLS")) {
  // if (log4j.isDebugEnabled())
  // log4j.debug("PDF");
  // String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
  // "ReportLibroInventariosYBalance50|DateFrom");
  // String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
  // "ReportLibroInventariosYBalance50|DateTo");
  // String strOrg = vars.getGlobalVariable("inpOrg", "ReportLibroInventariosYBalance50|Org", "0");
  //
  // String strTable = vars.getStringParameter("inpTable");
  // String strRecord = vars.getStringParameter("inpRecord");
  // setHistoryCommand(request, "DEFAULT");
  // printPagePDF(response, vars, strDateFrom, strDateTo, strOrg, strTable, strRecord);
  //
  // } else
  // pageError(response);
  // }
  //
  // private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
  // String strDateFrom, String strDateTo, String strOrg, String strTable, String strRecord)
  // throws IOException, ServletException {
  // if (log4j.isDebugEnabled())
  // log4j.debug("Output: dataSheet");
  // response.setContentType("text/html; charset=UTF-8");
  // PrintWriter out = response.getWriter();
  // XmlDocument xmlDocument = null;
  // ReportLibroInventariosYBalance50Data[] data = null;
  // String strPosition = "0";
  // ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportLibroInventariosYBalance50",
  // false, "", "", "imprimir();return false;", false, "ad_reports", strReplaceWith, false, true);
  // toolbar.setEmail(false);
  // if (data == null || data.length == 0) {
  // String discard[] = { "secTable" };
  // toolbar
  // .prepareRelationBarTemplate(
  // false,
  // false,
  // "submitCommandForm('XLS', false, null, 'ReportLibroInventariosYBalance50.xls', 'EXCEL');return false;");
  // xmlDocument = xmlEngine.readXmlTemplate(
  // "pe/com/unifiedgo/report/ad_reports/ReportLibroInventariosYBalance50", discard)
  // .createXmlDocument();
  // data = ReportLibroInventariosYBalance50Data.set("0");
  // data[0].rownum = "0";
  // } else {
  //
  // // data = notshow(data, vars);
  //
  // toolbar
  // .prepareRelationBarTemplate(
  // true,
  // true,
  // "submitCommandForm('XLS', false, null, 'ReportLibroInventariosYBalance50.xls', 'EXCEL');return false;");
  // xmlDocument = xmlEngine.readXmlTemplate(
  // "pe/com/unifiedgo/report/ad_reports/ReportLibroInventariosYBalance50").createXmlDocument();
  // }
  // xmlDocument.setParameter("toolbar", toolbar.toString());
  // try {
  // WindowTabs tabs = new WindowTabs(this, vars,
  // "pe.com.unifiedgo.report.ad_reports.ReportLibroInventariosYBalance50");
  // xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
  // xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
  // xmlDocument.setParameter("childTabContainer", tabs.childTabs());
  // xmlDocument.setParameter("theme", vars.getTheme());
  // NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
  // "ReportLibroInventariosYBalance50.html", classInfo.id, classInfo.type, strReplaceWith,
  // tabs.breadcrumb());
  // xmlDocument.setParameter("navigationBar", nav.toString());
  // LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
  // "ReportLibroInventariosYBalance50.html", strReplaceWith);
  // xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
  // } catch (Exception ex) {
  // throw new ServletException(ex);
  // }
  // {
  // OBError myMessage = vars.getMessage("ReportLibroInventariosYBalance50");
  // vars.removeMessage("ReportLibroInventariosYBalance50");
  // if (myMessage != null) {
  // xmlDocument.setParameter("messageType", myMessage.getType());
  // xmlDocument.setParameter("messageTitle", myMessage.getTitle());
  // xmlDocument.setParameter("messageMessage", myMessage.getMessage());
  // }
  // }
  //
  // xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
  //
  // try {
  // ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
  // "",
  // Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportLibroInventariosYBalance50"),
  // Utility.getContext(this, vars, "#User_Client", "ReportLibroInventariosYBalance50"), '*');
  // comboTableData.fillParameters(null, "ReportLibroInventariosYBalance50", "");
  // xmlDocument.setData("reportAD_ORGID", "liststructure", comboTableData.select(false));
  // } catch (Exception ex) {
  // throw new ServletException(ex);
  // }
  //
  // xmlDocument.setData(
  // "reportC_ACCTSCHEMA_ID",
  // "liststructure",
  // AccountingSchemaMiscData.selectC_ACCTSCHEMA_ID(this,
  // Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportGeneralLedger"),
  // Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"), ""));
  // xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
  // xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
  // xmlDocument.setParameter("dateFrom", strDateFrom);
  // xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
  // xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
  // xmlDocument.setParameter("dateTo", strDateTo);
  // xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
  // xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
  // xmlDocument.setParameter("adOrgId", strOrg);
  // xmlDocument.setParameter("groupId", strPosition);
  // xmlDocument.setParameter("paramRecord", strRecord);
  // xmlDocument.setParameter("paramTable", strTable);
  // vars.setSessionValue("ReportLibroInventariosYBalance50|Record", strRecord);
  // vars.setSessionValue("ReportLibroInventariosYBalance50|Table", strTable);
  //
  // xmlDocument.setData("structure1", data);
  // out.println(xmlDocument.print());
  // out.close();
  // }
  //
  // private void printPagePDF(HttpServletResponse response, VariablesSecureApp vars,
  // String strDateFrom, String strDateTo, String strOrg, String strTable, String strRecord)
  // throws IOException, ServletException {
  //
  // ReportLibroInventariosYBalance50Data[] data = null;
  //
  // data = ReportLibroInventariosYBalance50Data.selectCuenta10(this,
  // Utility.getContext(this, vars, "#User_Client", "ReportLibroInventariosYBalance50"),
  // Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportLibroInventariosYBalance50"),
  // strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"));
  // ;
  //
  // String strSubtitle = (Utility.messageBD(this, "LegalEntity", vars.getLanguage()) + ": ")
  // + ReportLibroInventariosYBalance50Data.selectCompany(this, vars.getClient()) + "\n" + "RUC:"
  // + ReportLibroInventariosYBalance50Data.selectRucOrg(this, strOrg) + "\n";
  // ;
  //
  // if (!("0".equals(strOrg)))
  // strSubtitle += (Utility.messageBD(this, "OBUIAPP_Organization", vars.getLanguage()) + ": ")
  // + ReportLibroInventariosYBalance50Data.selectOrg(this, strOrg) + "\n";
  //
  // // if (!"".equals(strDateFrom) || !"".equals(strDateTo))
  // // strSubtitle += (Utility.messageBD(this, "From", vars.getLanguage()) + ": ") + strDateFrom
  // // + "  " + (Utility.messageBD(this, "OBUIAPP_To", vars.getLanguage()) + ": ") + strDateTo
  // // + "\n";
  //
  // String strOutput;
  // String strReportName;
  // if (vars.commandIn("PDF")) {
  // strOutput = "pdf";
  // strReportName =
  // "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportLibroInventariosYBalance50.jrxml";
  // } else {
  // strOutput = "xls";
  // strReportName =
  // "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportLibroInventariosYBalance50Excel.jrxml";
  // }
  //
  // HashMap<String, Object> parameters = new HashMap<String, Object>();
  // parameters.put("Subtitle", strSubtitle);
  // // parameters.put("TaxID", ReportSalesRevenueRecordsData.selectOrgTaxID(this, strOrg));
  // parameters.put("dateFrom", StringToDate(strDateFrom));
  // parameters.put("dateTo", StringToDate(strDateTo));
  // renderJR(vars, response, strReportName, "Diferencia Facturas Guias", strOutput, parameters,
  // data, null);
  // }
  //
  // private Date StringToDate(String strDate) {
  // SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
  //
  // Date date;
  // try {
  // if (!strDate.equals("")) {
  // date = formatter.parse(strDate);
  // return date;
  // }
  // } catch (ParseException e) {
  // // TODO Auto-generated catch block
  // e.printStackTrace();
  // }
  // return null;
  // }
  //
  // @Override
  // public String getServletInfo() {
  // return
  // "Servlet ReportLibroInventariosYBalance50. This Servlet was made by Pablo Sarobe modified by everybody";
  // } // end of getServletInfo() method
}
