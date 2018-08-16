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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
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
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.financialmgmt.accounting.AccountingFact;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.accounting.StructPle;
import pe.com.unifiedgo.accounting.SunatUtil;

public class ReportLibroInventariosYBalance37y49 extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (log4j.isDebugEnabled())
      log4j.debug("Command: " + vars.getStringParameter("Command"));

    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReportLibroInventariosYBalance37y49|DateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportLibroInventariosYBalance37y49|DateTo", "");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportLibroInventariosYBalance37y49|Org", "0");
      String strRecord = vars.getGlobalVariable("inpRecord", "ReportLibroInventariosYBalance37y49|Record", "");
      String strTable = vars.getGlobalVariable("inpTable", "ReportLibroInventariosYBalance37y49|Table", "");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg, strTable, strRecord);

    } else if (vars.commandIn("DIRECT")) {
      String strTable = vars.getGlobalVariable("inpTable", "ReportLibroInventariosYBalance37y49|Table");
      String strRecord = vars.getGlobalVariable("inpRecord", "ReportLibroInventariosYBalance37y49|Record");

      setHistoryCommand(request, "DIRECT");
      vars.setSessionValue("ReportLibroInventariosYBalance37y49.initRecordNumber", "0");
      printPageDataSheet(response, vars, "", "", "", strTable, strRecord);

    } else if (vars.commandIn("FIND")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom", "ReportLibroInventariosYBalance37y49|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportLibroInventariosYBalance37y49|DateTo");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportLibroInventariosYBalance37y49|Org", "0");
      vars.setSessionValue("ReportLibroInventariosYBalance37y49.initRecordNumber", "0");
      setHistoryCommand(request, "DEFAULT");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg, "", "");

    } else if (vars.commandIn("PDF", "XLS")) {
      if (log4j.isDebugEnabled())
        log4j.debug("PDF");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom", "ReportLibroInventariosYBalance37y49|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportLibroInventariosYBalance37y49|DateTo");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportLibroInventariosYBalance37y49|Org", "0");

      String strTable = vars.getStringParameter("inpTable");
      String strRecord = vars.getStringParameter("inpRecord");
      setHistoryCommand(request, "DEFAULT");
      printPagePDF(request, response, vars, strDateFrom, strDateTo, strOrg, strTable, strRecord);

    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strDateFrom, String strDateTo, String strOrg, String strTable, String strRecord) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReportLibroInventariosYBalance37y49Data[] data = null;
    String strPosition = "0";

    String discard[] = { "discard" };

    xmlDocument = xmlEngine.readXmlTemplate("pe/com/unifiedgo/report/ad_reports/ReportLibroInventariosYBalance37y49", discard).createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportLibroInventariosYBalance37y49", false, "", "", "imprimir();return false;", false, "ad_reports", strReplaceWith, false, true);
    toolbar.setEmail(false);

    toolbar.prepareSimpleToolBarTemplate();

    toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");

    xmlDocument.setParameter("toolbar", toolbar.toString());

    data = ReportLibroInventariosYBalance37y49Data.set("0");
    data[0].rownum = "0";

    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars, "pe.com.unifiedgo.report.ad_reports.ReportLibroInventariosYBalance37y49");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportLibroInventariosYBalance37y49.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportLibroInventariosYBalance37y49.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportLibroInventariosYBalance37y49");
      vars.removeMessage("ReportLibroInventariosYBalance37y49");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "", "49DC1D6F086945AB82F84C66F5F13F16", Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportLibroInventariosYBalance37y49"), Utility.getContext(this, vars, "#User_Client", "ReportLibroInventariosYBalance37y49"), '*');
      comboTableData.fillParameters(null, "ReportLibroInventariosYBalance37y49", "");
      xmlDocument.setData("reportAD_ORGID", "liststructure", comboTableData.select(false));
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setData("reportC_ACCTSCHEMA_ID", "liststructure", AccountingSchemaMiscData.selectC_ACCTSCHEMA_ID(this, Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportGeneralLedger"), Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"), ""));
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
    vars.setSessionValue("ReportLibroInventariosYBalance37y49|Record", strRecord);
    vars.setSessionValue("ReportLibroInventariosYBalance37y49|Table", strTable);

    xmlDocument.setParameter("paramPeriodosArray", Utility.arrayInfinitasEntradas("idperiodo;periodo;fechainicial;fechafinal;idorganizacion", "arrPeriodos", ReportLibroInventariosYBalance37y49Data.select_periodos(this)));

    xmlDocument.setData("structure1", data);
    out.println(xmlDocument.print());
    out.close();
  }

  private static String getFamily(ConnectionProvider conn, String strTree, String strChild) throws IOException, ServletException {
    return Tree.getMembers(conn, strTree, (strChild == null || strChild.equals("")) ? "0" : strChild);
    /*
     * ReportGeneralLedgerData [] data = ReportGeneralLedgerData.selectChildren(this, strTree,
     * strChild); String strFamily = ""; if(data!=null && data.length>0) { for (int i =
     * 0;i<data.length;i++){ if (i>0) strFamily = strFamily + ","; strFamily = strFamily +
     * data[i].id; } return strFamily += ""; }else return "'1'";
     */
  }

  private void printPagePDF(HttpServletRequest request, HttpServletResponse response, VariablesSecureApp vars, String strDateFrom, String strDateTo, String strOrg, String strTable, String strRecord) throws IOException, ServletException {

    ReportLibroInventariosYBalance37y49Data[] data = null;

    String[] fechaAnioActual = strDateTo.split("-");
    Integer anioActual = new Integer(fechaAnioActual[2]);
    String fechaIniAnioActual = "01-01-" + anioActual;

    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    String strOrgFamily = getFamily(this, strTreeOrg, strOrg);

    data = ReportLibroInventariosYBalance37y49Data.getSaldoCuenta37y49(this, strDateTo, Utility.getContext(this, vars, "#User_Client", "ReportLibroInventariosYBalance37y49"), strOrgFamily, strOrg, Utility.getContext(this, vars, "#User_Client", "ReportLibroInventariosYBalance37y49"), strOrgFamily,"49%");

    for (int i = 0; i < data.length; i++) {
      if (data[i].cBpartnerId != null && !"".equals(data[i].cBpartnerId)) {
        BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class, data[i].cBpartnerId);
        if (bp != null) {
          data[i].bpDocumentType = bp.getScrComboItem().getCode();
        }
      }
    }

    String strSubtitle = (Utility.messageBD(this, "LegalEntity", vars.getLanguage()) + ": ") + ReportLibroInventariosYBalance37y49Data.selectCompany(this, vars.getClient()) + "\n" + "RUC:" + ReportLibroInventariosYBalance37y49Data.selectRucOrg(this, strOrg) + "\n";
    ;

    if (!("0".equals(strOrg)))
      strSubtitle += (Utility.messageBD(this, "OBUIAPP_Organization", vars.getLanguage()) + ": ") + ReportLibroInventariosYBalance37y49Data.selectOrg(this, strOrg) + "\n";

    // if (!"".equals(strDateFrom) || !"".equals(strDateTo))
    // strSubtitle += (Utility.messageBD(this, "From", vars.getLanguage()) + ": ") + strDateFrom
    // + "  " + (Utility.messageBD(this, "OBUIAPP_To", vars.getLanguage()) + ": ") + strDateTo
    // + "\n";

    String strOutput;
    String strReportName;
    if (vars.commandIn("PDF")) {
      strOutput = "pdf";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportLibroInventariosYBalance37y49.jrxml";
    } else {
      strOutput = "xls";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportLibroInventariosYBalance37y49Excel.jrxml";
    }

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("Subtitle", strSubtitle);
    parameters.put("Ruc", ReportLibroInventariosYBalance37y49Data.selectRucOrg(this, strOrg));
    parameters.put("Razon", ReportLibroInventariosYBalance37y49Data.selectSocialName(this, strOrg));
    parameters.put("strDateFrom", fechaIniAnioActual);
    parameters.put("strDateTo", strDateTo);
    parameters.put("dateTo", StringToDate(strDateTo));

    if (data.length == 0) {
      advisePopUp(request, response, "WARNING", Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()), Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
      return;
    }

    renderJR(vars, response, strReportName, "LibroIB_Cuenta49", strOutput, parameters, data, null);
  }

  public static StructPle getPLECuenta37y49(ConnectionProvider conn, VariablesSecureApp vars, String strDateFrom, String strDateTo, String adUserClient, String strOrg) throws Exception {
    String strTreeOrg = TreeData.getTreeOrg(conn, vars.getClient());
    String strOrgFamily = getFamily(conn, strTreeOrg, strOrg);

    String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty("dateFormat.java");
    SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_JAVA);

    SimpleDateFormat formatterForm = new SimpleDateFormat("dd-MM-yyyy");
    Date dateTo = null;
    try {
      dateTo = formatterForm.parse(strDateTo);
    } catch (Exception ex) {
      System.out.println("Exception: " + strDateFrom);
    }

    // get this year uit
    Calendar caldttTo = new GregorianCalendar();
    caldttTo.setTime(dateTo);
    int year = caldttTo.get(Calendar.YEAR);

    // get first day of year
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.DAY_OF_YEAR, 1);
    Date dateFrom = cal.getTime();

    ReportLibroInventariosYBalance37y49Data[] dataFinal = ReportLibroInventariosYBalance37y49Data.getSaldoCuenta37y49(conn, sdf.format(dateTo), adUserClient, strOrgFamily, strOrg, adUserClient, strOrgFamily,null);

    System.out.println("datafinal length:" + dataFinal.length);

    StructPle sunatPle = getPLECuentas_37_49(dataFinal, dateFrom, dateTo, strOrg);

    return sunatPle;
  }

  public static StructPle getPLECuentas_37_49(ReportLibroInventariosYBalance37y49Data[] data, Date dateFrom, Date dateTo, String strOrg) throws Exception {

    StringBuffer sb = new StringBuffer();
    StructPle sunatPle = new StructPle();
    sunatPle.numEntries = 0;

    SimpleDateFormat dt = new SimpleDateFormat("yyyyMMdd");

    String period = dt.format(dateTo); // Periodo
    String assetNo = "M00000"; // Nro Asiento (correlativo)
    String OpStatus = "1"; // Estado de la Operación

    for (int i = 0; i < data.length; i++) {

      ReportLibroInventariosYBalance37y49Data doc = data[i];
      String factAcctId = doc.factAcctId;
      if (factAcctId.isEmpty()) {
        continue;
      }
      AccountingFact fact = OBDal.getInstance().get(AccountingFact.class, factAcctId);
      if (fact == null) {
        continue;
      }
      String cuo = fact.getId();
      String cuo_correl;
      if (fact.getType().equals("O")) {
        cuo_correl = "A001";
      } else if (fact.getType().equals("C")) {
        cuo_correl = "C001";
      } else {
        cuo_correl = "M" + fact.getScoRegnumber().replace("-", "");
        if (cuo_correl.length() > 10)
          cuo_correl = cuo_correl.substring(0, 10);
      }

      String tipoDoc = doc.t10code;
      String serie = doc.serie;
      String numdoc = doc.numdoc;
      if (tipoDoc.equals("00")) {
        serie = "00";
        numdoc = "00";
      }

      DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
      DateFormat df2 = new SimpleDateFormat("dd/MM/yyyy");
      Date prev_dateacct = df.parse(doc.dateacct);

      String dateacct = df2.format(prev_dateacct); // Fecha de Emision

      double amtToPay = 0;
      amtToPay = Double.valueOf(doc.amt);

      String linea = period + "|" + cuo + "|" + cuo_correl + "|" + tipoDoc + "|" + serie + "|" + numdoc + "|" + doc.codCuenta + "|" + doc.descripcion + "|" + String.format("%.2f", amtToPay) + "|0.00|0.00|" + OpStatus + "|";

      if (!sb.toString().equals(""))
        sb.append("\n");
      sb.append(linea);
      sunatPle.numEntries++;

    }

    sunatPle.data = sb.toString();

    Organization org = OBDal.getInstance().get(Organization.class, strOrg);
    String rucAdq = SunatUtil.LPad(org.getOrganizationInformationList().get(0).getTaxID(), 11, '0');

    String filename = "LE" + rucAdq + dt.format(dateTo) + "031500011111.TXT"; // LERRRRRRRRRRRAAAAMMDD031500CCOIM1.TXT

    sunatPle.filename = filename;

    return sunatPle;
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
    return "Servlet ReportLibroInventariosYBalance37y49. This Servlet was made by Pablo Sarobe modified by everybody";
  } // end of getServletInfo() method
}
