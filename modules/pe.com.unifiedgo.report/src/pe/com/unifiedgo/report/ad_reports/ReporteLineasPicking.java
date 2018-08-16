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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
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
import org.openbravo.xmlEngine.XmlDocument;

public class ReporteLineasPicking extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (log4j.isDebugEnabled())
      log4j.debug("Command: " + vars.getStringParameter("Command"));

    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReporteLineasPicking|DateFrom",
          "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReporteLineasPicking|DateTo", "");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReporteLineasPicking|Org", "0");
      String strRecord = vars.getGlobalVariable("inpRecord", "ReporteLineasPicking|Record", "");
      String strTable = vars.getGlobalVariable("inpTable", "ReporteLineasPicking|Table", "");
      String strFinancialAccountId = vars.getGlobalVariable("inpFinFinancialAccount",
          "ReporteLineasPicking|FinancialAccountId", "", IsIDFilter.instance);
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg, strTable, strRecord,
          strFinancialAccountId);

    } else if (vars.commandIn("DIRECT")) {
      String strTable = vars.getGlobalVariable("inpTable", "ReporteLineasPicking|Table");
      String strRecord = vars.getGlobalVariable("inpRecord", "ReporteLineasPicking|Record");

      setHistoryCommand(request, "DIRECT");
      vars.setSessionValue("ReporteLineasPicking.initRecordNumber", "0");
      String strFinancialAccountId = vars.getGlobalVariable("inpFinFinancialAccount",
          "ReporteLineasPicking|FinancialAccountId", "", IsIDFilter.instance);
      printPageDataSheet(response, vars, "", "", "", strTable, strRecord, strFinancialAccountId);

    } else if (vars.commandIn("FIND")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReporteLineasPicking|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReporteLineasPicking|DateTo");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReporteLineasPicking|Org", "0");
      String strFinancialAccountId = vars.getGlobalVariable("inpFinFinancialAccount",
          "ReporteLineasPicking|FinancialAccountId", "", IsIDFilter.instance);
      vars.setSessionValue("ReporteLineasPicking.initRecordNumber", "0");
      setHistoryCommand(request, "DEFAULT");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg, "", "",
          strFinancialAccountId);

    } else if (vars.commandIn("PDF", "XLS")) {
      if (log4j.isDebugEnabled())
        log4j.debug("PDF");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReporteLineasPicking|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReporteLineasPicking|DateTo");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReporteLineasPicking|Org", "0");

      String strTable = vars.getStringParameter("inpTable");
      String strRecord = vars.getStringParameter("inpRecord");

      String strFinancialAccountId = vars.getRequestGlobalVariable("inpFinFinancialAccount",
          "ReporteLineasPicking|FinancialAccountId", IsIDFilter.instance);
      setHistoryCommand(request, "DEFAULT");

      printPagePDF(response, vars, strDateFrom, strDateTo, strOrg, strTable, strRecord,
          strFinancialAccountId);

    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strOrg, String strTable, String strRecord,
      String strFinancialAccountId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReporteLineasPickingData[] data = null;
    String strPosition = "0";
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReporteLineasPicking", false, "", "",
        "imprimir();return false;", false, "ad_reports", strReplaceWith, false, true);
    toolbar.setEmail(false);
    if (data == null || data.length == 0) {
      String discard[] = { "secTable" };
      toolbar
          .prepareRelationBarTemplate(false, false,
              "submitCommandForm('XLS', false, null, 'ReporteLineasPicking.xls', 'EXCEL');return false;");
      xmlDocument = xmlEngine.readXmlTemplate(
          "pe/com/unifiedgo/report/ad_reports/ReporteLineasPicking", discard).createXmlDocument();
      data = ReporteLineasPickingData.set("0");
      data[0].rownum = "0";
    } else {

      // data = notshow(data, vars);

      toolbar
          .prepareRelationBarTemplate(true, true,
              "submitCommandForm('XLS', false, null, 'ReporteLineasPicking.xls', 'EXCEL');return false;");
      xmlDocument = xmlEngine.readXmlTemplate(
          "pe/com/unifiedgo/report/ad_reports/ReporteLineasPicking").createXmlDocument();
    }
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "pe.com.unifiedgo.report.ad_reports.ReporteLineasPicking");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReporteLineasPicking.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReporteLineasPicking.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReporteLineasPicking");
      vars.removeMessage("ReporteLineasPicking");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
          "", Utility.getContext(this, vars, "#AccessibleOrgTree", "ReporteLineasPicking"),
          Utility.getContext(this, vars, "#User_Client", "ReporteLineasPicking"), '*');
      comboTableData.fillParameters(null, "ReporteLineasPicking", "");
      xmlDocument.setData("reportAD_ORGID", "liststructure", comboTableData.select(false));
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    // /////////////////////////////////////////////////////////

    xmlDocument.setParameter("FinFinancialAccount", strFinancialAccountId);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
          "FIN_Financial_Account_ID", "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReporteLineasPicking"), Utility.getContext(this, vars, "#User_Client",
              "ReporteLineasPicking"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReporteLineasPicking",
          strFinancialAccountId);
      xmlDocument.setData("reportFinFinancialAccount", "liststructure",
          comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    System.out.println("doble: "
        + Utility.arrayDobleEntrada(
            "arrFinFinancialAccount",
            ReporteLineasPickingData.selectFinFinancialAccountDouble(this,
                Utility.getContext(this, vars, "#User_Client", "ReporteLineasPicking"))));

    System.out.println("triple: "
        + Utility.arrayTripleEntrada(
            "arrFinFinancialAccount",
            ReporteLineasPickingData.selectFinFinancialAccountDouble(this,
                Utility.getContext(this, vars, "#User_Client", "ReporteLineasPicking"))));

    xmlDocument.setParameter(
        "FinFinancialAccountArray",
        Utility.arrayTripleEntrada(
            "arrFinFinancialAccount",
            ReporteLineasPickingData.selectFinFinancialAccountDouble(this,
                Utility.getContext(this, vars, "#User_Client", "ReporteLineasPicking"))));

    // ///////////////////////////////////////////////////////////////////

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
    vars.setSessionValue("ReporteLineasPicking|Record", strRecord);
    vars.setSessionValue("ReporteLineasPicking|Table", strTable);

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

  private void printPagePDF(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strOrg, String strTable, String strRecord,
      String strFinancialAccountId) throws IOException, ServletException {

    ReporteLineasPickingData[] data = null;

    // ArrayList<ReporteLineasPickingData> listData = new ArrayList<ReporteLineasPickingData>();

    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    String strOrgFamily = getFamily(strTreeOrg, strOrg);

    data = ReporteLineasPickingData.select_cro_pag_imp(this,
        Utility.getContext(this, vars, "#User_Client", "ReporteLineasPicking"), strOrgFamily,
        strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), strFinancialAccountId);

    // System.out
    // .println("DATOS DESDE DATAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
    // for (int i = 0; i < data.length; i++) {
    //
    // ReporteLineasPickingData obj = data[i];
    //
    // System.out.println("nDocumento: " + obj.nDocumento);
    // System.out.println("numeroSerie: " + obj.numeroSerie);
    // System.out.println("estado: " + obj.estado);
    // System.out.println("serie: " + obj.serie);
    // System.out.println("numero: " + obj.numero);
    // System.out.println("tipodoc: " + obj.tipodoc);
    // System.out.println("rownum: " + obj.rownum);
    // System.out.println("-------------------------------------");
    // }
    //
    // int serieant = -1;
    // int numeroant = -1;
    // int lengthnum = -1;
    // int serienow = -1;
    // int numeronow = -1;
    //
    // ReporteLineasPickingData objant = new
    // ReporteLineasPickingData();
    //
    // for (ReporteLineasPickingData obj : data) {
    //
    // ReporteLineasPickingData temp = new
    // ReporteLineasPickingData();
    //
    // temp.nDocumento = obj.nDocumento;
    // temp.numeroSerie = obj.numeroSerie;
    // temp.estado = obj.estado;
    // temp.serie = obj.serie;
    // temp.numero = obj.numero;
    // temp.serieN = obj.serieN;
    // temp.numeroN = obj.numeroN;
    // temp.tipodoc = obj.tipodoc;
    //
    // if (obj.estado.compareToIgnoreCase("AA") == 0) {
    //
    // serienow = Integer.parseInt(obj.serie == "" ? "0" : obj.serie);
    // numeronow = Integer.parseInt(obj.numero == "" ? "0"
    // : obj.numero);
    //
    // if (serieant == serienow) {
    // if (numeronow > (numeroant + 1)) {
    // lengthnum = temp.numero.length();
    // temp.nDocumento = "-----";
    // temp.numeroSerie = Integer.toString(serieant) + "-"
    // + completaconzeros(numeroant + 1, lengthnum)
    // + " hasta " + Integer.toString(serieant) + "-"
    // + completaconzeros(numeronow - 1, lengthnum);
    // temp.estado = "NO EXISTEN";
    // temp.serie = Integer.toString(serienow);
    // temp.numero = "0";
    // temp.tipodoc = (temp.tipodoc.compareTo(objant.tipodoc) != 0 ?
    // objant.tipodoc
    // + "->" + temp.tipodoc
    // : temp.tipodoc);
    // listData.add(temp);
    //
    // } else if (numeronow == numeroant) {
    // objant.estado = "NUMERO REPETIDO";
    // objant.serie = Integer.toString(serienow);
    // temp.estado = "NUMERO REPETIDO";
    // temp.serie = Integer.toString(serienow);
    // listData.add(objant);
    // listData.add(temp);
    // }
    // }
    // } else {
    // temp.serie = "---";
    // temp.numero = "------";
    // listData.add(temp);
    // }
    // serieant = serienow;
    // numeroant = numeronow;
    // objant = obj;
    // }
    //
    // Collections.sort(listData,
    // new Comparator<ReporteLineasPickingData>() {
    // @Override
    // public int compare(
    // ReporteLineasPickingData p1,
    // ReporteLineasPickingData p2) {
    //
    // int resultado = new String(p1.serie)
    // .compareTo(new String(p2.serie));
    // if (resultado != 0) {
    // return resultado;
    // }
    //
    // resultado = new String(p1.estado).compareTo(new String(
    // p2.estado));
    // if (resultado != 0) {
    // return resultado;
    // }
    //
    // resultado = Integer.compare(new Integer(p1.numeroN),
    // new Integer(p2.numeroN));
    // if (resultado != 0) {
    // return resultado;
    // }
    // return resultado;
    // }
    // });
    //
    // ReporteLineasPickingData[] bestData = new
    // ReporteLineasPickingData[listData
    // .size()];
    //
    // for (int x = 0; x < listData.size(); x++) {
    // bestData[x] = listData.get(x);
    // }

    String strSubtitle = (Utility.messageBD(this, "LegalEntity", vars.getLanguage()) + ": ")
        + ReporteLineasPickingData.selectCompany(this, vars.getClient()) + "\n" + "RUC:"
        + ReporteLineasPickingData.selectRucOrg(this, strOrg) + "\n";

    if (!("0".equals(strOrg)))
      strSubtitle += (Utility.messageBD(this, "OBUIAPP_Organization", vars.getLanguage()) + ": ")
          + ReporteLineasPickingData.selectOrg(this, strOrg) + "\n";

    System.out.println("Llega hasta aqui");
    String strOutput;
    String strReportName;
    if (vars.commandIn("PDF")) {
      strOutput = "pdf";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteLineasPicking.jrxml";
    } else {
      strOutput = "xls";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteLineasPickingExcel.jrxml";
    }

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    // parameters.put("Subtitle", strSubtitle);
    parameters.put("Ruc", ReporteLineasPickingData.selectRucOrg(this, strOrg));
    parameters.put("organizacion", ReporteLineasPickingData.selectSocialName(this, strOrg));

    // parameters.put("dateFrom", StringToDate(strDateFrom));
    // parameters.put("dateTo", StringToDate(strDateTo));
    parameters.put("dateFrom", StringToDate(strDateFrom));
    parameters.put("dateTo", StringToDate(strDateTo));
    renderJR(vars, response, strReportName, "Reporte_Contable_Verificacion_Guias_de_Remsion",
        "pdf", parameters, data, null);
  }

  private String completaconzeros(int numero, int lengthmax) {

    String snum = Integer.toString(numero);
    int tamori = snum.length();
    String result = "";
    for (int i = tamori; i <= lengthmax; i++)
      result += "0";
    return result + snum;
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
    return "Servlet ReporteLineasPicking. This Servlet was made by Pablo Sarobe modified by everybody";
  } // end of getServletInfo() method
}
