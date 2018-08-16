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
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.accounting.StructPle;
import pe.com.unifiedgo.accounting.SunatUtil;

public class ReporteControlPatrimonio extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (log4j.isDebugEnabled())
      log4j.debug("Command: " + vars.getStringParameter("Command"));

    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom",
          "ReporteControlPatrimonio|DateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReporteControlPatrimonio|DateTo", "");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReporteControlPatrimonio|Org", "0");
      String strRecord = vars.getGlobalVariable("inpRecord", "ReporteControlPatrimonio|Record", "");
      String strTable = vars.getGlobalVariable("inpTable", "ReporteControlPatrimonio|Table", "");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg, strTable, strRecord);

    } else if (vars.commandIn("DIRECT")) {
      String strTable = vars.getGlobalVariable("inpTable", "ReporteControlPatrimonio|Table");
      String strRecord = vars.getGlobalVariable("inpRecord", "ReporteControlPatrimonio|Record");

      setHistoryCommand(request, "DIRECT");
      vars.setSessionValue("ReporteControlPatrimonio.initRecordNumber", "0");
      printPageDataSheet(response, vars, "", "", "", strTable, strRecord);

    } else if (vars.commandIn("FIND")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReporteControlPatrimonio|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReporteControlPatrimonio|DateTo");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReporteControlPatrimonio|Org", "0");
      vars.setSessionValue("ReporteControlPatrimonio.initRecordNumber", "0");
      setHistoryCommand(request, "DEFAULT");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg, "", "");

    } else if (vars.commandIn("PDF", "XLS")) {
      if (log4j.isDebugEnabled())
        log4j.debug("PDF");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReporteControlPatrimonio|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReporteControlPatrimonio|DateTo");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReporteControlPatrimonio|Org", "0");

      String strTable = vars.getStringParameter("inpTable");
      String strRecord = vars.getStringParameter("inpRecord");
      setHistoryCommand(request, "DEFAULT");
      printPagePDF(request, response, vars, strDateFrom, strDateTo, strOrg, strTable, strRecord);

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
    ReporteControlPatrimonioData[] data = null;
    String strPosition = "0";
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReporteControlPatrimonio", false, "",
        "", "imprimir();return false;", false, "ad_reports", strReplaceWith, false, true);
    toolbar.setEmail(false);

    toolbar.prepareSimpleToolBarTemplate();
    toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");

    if (data == null || data.length == 0) {
      String discard[] = { "secTable" };
      // toolbar.prepareRelationBarTemplate(
      // false,
      // false,
      // "submitCommandForm('XLS', false, null, 'ReporteControlPatrimonio.xls', 'EXCEL');return false;");
      xmlDocument = xmlEngine.readXmlTemplate(
          "pe/com/unifiedgo/report/ad_reports/ReporteControlPatrimonio", discard)
          .createXmlDocument();
      data = ReporteControlPatrimonioData.set("0");
      data[0].rownum = "0";
    }
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "pe.com.unifiedgo.report.ad_reports.ReporteControlPatrimonio");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ReporteControlPatrimonio.html", classInfo.id, classInfo.type, strReplaceWith,
          tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReporteControlPatrimonio.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReporteControlPatrimonio");
      vars.removeMessage("ReporteControlPatrimonio");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
          "0C754881EAD94243A161111916E9B9C6", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReporteControlPatrimonio"), Utility.getContext(this, vars, "#User_Client",
              "ReporteControlPatrimonio"), '*');
      comboTableData.fillParameters(null, "ReporteControlPatrimonio", "");
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
    xmlDocument.setParameter("paramPeriodosArray", Utility.arrayInfinitasEntradas(
        "idperiodo;periodo;fechainicial;fechafinal;idorganizacion", "arrPeriodos",
        ReporteControlPatrimonioData.select_periodos(this)));
    vars.setSessionValue("ReporteControlPatrimonio|Record", strRecord);
    vars.setSessionValue("ReporteControlPatrimonio|Table", strTable);

    xmlDocument.setData("structure1", data);
    out.println(xmlDocument.print());
    out.close();
  }

  private static String getFamily(ConnectionProvider conn, String strTree, String strChild)
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

  private void printPagePDF(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String strOrg,
      String strTable, String strRecord) throws IOException, ServletException {
    ReporteControlPatrimonioData[] dataFinal = null;

    dataFinal = getControlPatrimonioData(this, vars, strDateFrom, strDateTo,
        Utility.getContext(this, vars, "#User_Client", "ReporteControlPatrimonioData"), strOrg);
    if (dataFinal.length == 0) {
      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
      return;
    }

    String strOutput;
    String strReportName;
    if (vars.commandIn("PDF")) {
      strOutput = "pdf";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteControlPatrimonio.jrxml";
    } else {
      strOutput = "xls";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteControlPatrimonioExcel.jrxml";
    }

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    // parameters.put("Subtitle", strSubtitle);
    parameters.put("Ruc", ReporteControlPatrimonioData.selectRucOrg(this, strOrg));
    parameters.put("organizacion", ReporteControlPatrimonioData.selectSocialName(this, strOrg));
    parameters.put("dateFrom", StringToDate(strDateFrom));
    parameters.put("dateTo", StringToDate(strDateTo));
    renderJR(vars, response, strReportName, "Reporte_Control_Patrimonio", strOutput, parameters,
        dataFinal, null);
  }

  private static ReporteControlPatrimonioData[] getControlPatrimonioData(ConnectionProvider conn,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String adUserClient,
      String strOrg) throws IOException, ServletException {

    ReporteControlPatrimonioData[] dataFacts = null;
    ReporteControlPatrimonioData[] dataFinal = null;

    ArrayList<ReporteControlPatrimonioData> listCabecera = new ArrayList<ReporteControlPatrimonioData>();
    ArrayList<ReporteControlPatrimonioData> listDataFila = new ArrayList<ReporteControlPatrimonioData>();
    ArrayList<ReporteControlPatrimonioData> listDataFinal = new ArrayList<ReporteControlPatrimonioData>();

    HashMap<String, BigDecimal> grupoMontoXCuentaFila = new HashMap<String, BigDecimal>();

    String[] cuentasFilas = { "5011", "5012", "511", "512", "5221", "5222", "5223", "5224", "5631",
        "5632", "564", "5641", "5642", "5711", "5712", "5713", "581", "582", "583", "584", "585",
        "589", "5911", "5912", "5921", "5922" };

    BigDecimal montoFinal = BigDecimal.ZERO;

    listCabecera = cargaListaCabecera();

    String strTreeOrg = TreeData.getTreeOrg(conn, vars.getClient());
    String strOrgFamily = getFamily(conn, strTreeOrg, strOrg);

    dataFacts = ReporteControlPatrimonioData.select(conn, adUserClient, strOrgFamily, strDateTo);

    for (int i = 0; i < dataFacts.length; i++) {

      BigDecimal monto = new BigDecimal(dataFacts[i].monto);
      for (int k = 0; k < dataFacts[i].cuentaFila.length(); k++) {

        String key = dataFacts[i].cuentaFila.substring(0, k + 1);

        if (grupoMontoXCuentaFila.containsKey(key))
          montoFinal = monto.add(grupoMontoXCuentaFila.get(key));
        else
          montoFinal = monto;

        grupoMontoXCuentaFila.put(key, montoFinal);
      }
    }

    for (int i = 0; i < cuentasFilas.length; i++) {

      String cuentaFila = cuentasFilas[i];
      montoFinal = BigDecimal.ZERO;
      listDataFila = new ArrayList<ReporteControlPatrimonioData>();

      for (int j = 0; j < listCabecera.size(); j++) {

        String cuentaCabecera = listCabecera.get(j).cuentaCabecera;
        BigDecimal monto = grupoMontoXCuentaFila.get(cuentaFila);

        if (!cuentaFila.startsWith(cuentaCabecera) || monto == null)
          monto = BigDecimal.ZERO;

        ReporteControlPatrimonioData e = new ReporteControlPatrimonioData();
        e.orderCabecera = listCabecera.get(j).orderCabecera;
        e.cuentaCabecera = listCabecera.get(j).cuentaCabecera;
        e.descripcionCabecera = listCabecera.get(j).descripcionCabecera;
        e.cuentaFila = cuentaFila;
        e.monto = monto.toString();

        listDataFila.add(e);
        montoFinal = montoFinal.add(monto);
      }

      if (montoFinal.compareTo(BigDecimal.ZERO) != 0) // MOSTRANDO SOLO FILAS CON AL MENOS UN MONTO
                                                      // != 0
        listDataFinal.addAll(listDataFila);
    }

    dataFinal = listDataFinal.toArray(new ReporteControlPatrimonioData[listDataFinal.size()]);

    return dataFinal;
  }

  private static ArrayList<ReporteControlPatrimonioData> cargaListaCabecera() {
    ArrayList<ReporteControlPatrimonioData> listCabecera = new ArrayList<ReporteControlPatrimonioData>();

    ReporteControlPatrimonioData item = null;

    item = new ReporteControlPatrimonioData();
    item.orderCabecera = "1";
    item.cuentaCabecera = "50";
    item.descripcionCabecera = "Capital";
    listCabecera.add(item);

    item = new ReporteControlPatrimonioData();
    item.orderCabecera = "2";
    item.cuentaCabecera = "51";
    item.descripcionCabecera = "Acciones de Inversión";
    listCabecera.add(item);

    item = new ReporteControlPatrimonioData();
    item.orderCabecera = "3";
    item.cuentaCabecera = "52";
    item.descripcionCabecera = "Capital Adicional";
    listCabecera.add(item);

    item = new ReporteControlPatrimonioData();
    item.orderCabecera = "4";
    item.cuentaCabecera = "56";
    item.descripcionCabecera = "Resultados no Realizados";
    listCabecera.add(item);

    item = new ReporteControlPatrimonioData();
    item.orderCabecera = "5";
    item.cuentaCabecera = "582";
    item.descripcionCabecera = "Reservas Legales";
    listCabecera.add(item);

    item = new ReporteControlPatrimonioData();
    item.orderCabecera = "6";
    item.cuentaCabecera = "589";
    item.descripcionCabecera = "Otras Reservas";
    listCabecera.add(item);

    item = new ReporteControlPatrimonioData();
    item.orderCabecera = "7";
    item.cuentaCabecera = "59";
    item.descripcionCabecera = "Resultados Acumulados";
    listCabecera.add(item);

    item = new ReporteControlPatrimonioData();
    item.orderCabecera = "8";
    item.cuentaCabecera = "??";
    item.descripcionCabecera = "Diferencias de Conversión";
    listCabecera.add(item);

    item = new ReporteControlPatrimonioData();
    item.orderCabecera = "9";
    item.cuentaCabecera = "??";
    item.descripcionCabecera = "Ajustes al Patrimonio";
    listCabecera.add(item);

    item = new ReporteControlPatrimonioData();
    item.orderCabecera = "10";
    item.cuentaCabecera = "??";
    item.descripcionCabecera = "Resultado Neto del Ejercicio";
    listCabecera.add(item);

    item = new ReporteControlPatrimonioData();
    item.orderCabecera = "11";
    item.cuentaCabecera = "57";
    item.descripcionCabecera = "Excedente de Revaluación";
    listCabecera.add(item);

    item = new ReporteControlPatrimonioData();
    item.orderCabecera = "12";
    item.cuentaCabecera = "??";
    item.descripcionCabecera = "Resultado del Ejercicio";
    listCabecera.add(item);

    return listCabecera;
  }

  public static StructPle getPLE(ConnectionProvider conn, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String adUserClient, String strOrg) throws Exception {
    String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
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

    ReporteControlPatrimonioData[] dataFinal = getControlPatrimonioData(conn, vars,
        sdf.format(dateFrom), sdf.format(dateTo), adUserClient, strOrg);

    System.out.println("datafinal length:" + dataFinal.length);

    StructPle sunatPle = getStringData(dataFinal, dateFrom, dateTo, strOrg);

    return sunatPle;
  }

  private static StructPle getStringData(ReporteControlPatrimonioData[] data, Date dateFrom,
      Date dateTo, String strOrg) {

    StructPle sunatPle = new StructPle();
    sunatPle.numEntries = 0;

    StringBuffer sb = new StringBuffer();

    Organization org = OBDal.getInstance().get(Organization.class, strOrg);
    String rucAdq = SunatUtil.LPad(org.getOrganizationInformationList().get(0).getTaxID(), 11, '0');

    SimpleDateFormat dt = new SimpleDateFormat("yyyyMMdd");

    String filename = "LE" + rucAdq + dt.format(dateTo) + "031900011111.TXT"; // LERRRRRRRRRRRAAAAMMDD031900CCOIM1.TXT

    String periodoTrib = dt.format(dateTo);
    String codigoCatalago = "01";
    String estadoOp = "1";

    boolean isNewRow = false;
    int jj = 0;
    for (int i = 0; i < data.length; i++) {
      if (data[i].orderCabecera.compareTo("1") == 0) {
        String codigoRubro = data[i].cuentaFila;

        String campo4 = "0"; // Capital
        String campo5 = "0"; // Acciones de Inversión
        String campo6 = "0"; // Capital Adicional
        String campo7 = "0"; // Resultados no Realizados
        String campo8 = "0"; // Reservas Legales
        String campo9 = "0"; // Otras Reservas
        String campo10 = "0"; // Resultados Acumulados
        String campo11 = "0"; // Diferencias de Conversión
        String campo12 = "0"; // Ajustes al Patrimonio
        String campo13 = "0"; // Resultado Neto del Ejercicio
        String campo14 = "0"; // Excedente de Revaluación
        String campo15 = "0"; // Resultado del Ejercicio

        isNewRow = false;
        for (int j = i; j < data.length; j++) {
          ReporteControlPatrimonioData led = data[j];
          if (led.orderCabecera.compareTo("1") == 0 && isNewRow)
            break;

          if (led.orderCabecera.compareTo("1") == 0) { // Capital
            campo4 = led.monto;
            isNewRow = true;
          } else if (led.orderCabecera.compareTo("2") == 0) { // Acciones de Inversión
            campo5 = led.monto;
          } else if (led.orderCabecera.compareTo("3") == 0) { // Capital Adicional
            campo6 = led.monto;
          } else if (led.orderCabecera.compareTo("4") == 0) { // Resultados no Realizados
            campo7 = led.monto;
          } else if (led.orderCabecera.compareTo("5") == 0) { // Reservas Legales
            campo8 = led.monto;
          } else if (led.orderCabecera.compareTo("6") == 0) { // Otras Reservas
            campo9 = led.monto;
          } else if (led.orderCabecera.compareTo("7") == 0) { // Resultados Acumulados
            campo10 = led.monto;
          } else if (led.orderCabecera.compareTo("8") == 0) { // Diferencias de Conversión
            campo11 = led.monto;
          } else if (led.orderCabecera.compareTo("9") == 0) { // Ajustes al Patrimonio
            campo12 = led.monto;
          } else if (led.orderCabecera.compareTo("10") == 0) { // Resultado Neto del Ejercicio
            campo13 = led.monto;
          } else if (led.orderCabecera.compareTo("11") == 0) { // Excedente de Revaluación
            campo14 = led.monto;
          } else if (led.orderCabecera.compareTo("12") == 0) { // Resultado del Ejercicio
            campo15 = led.monto;
          }
        }

        String linea = periodoTrib + "|" + codigoCatalago + "|" + codigoRubro + "|" + campo4 + "|"
            + campo5 + "|" + campo6 + "|" + campo7 + "|" + campo8 + "|" + campo9 + "|" + campo10
            + "|" + campo11 + "|" + campo12 + "|" + campo13 + "|" + campo14 + "|" + campo15 + "|"
            + estadoOp + "|";

        if (jj > 0)
          sb.append("\n");
        sb.append(linea);
        sunatPle.numEntries++;
        jj++;

      } else {
        continue;
      }

    }

    sunatPle.filename = filename;
    sunatPle.data = sb.toString();
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
    return "Servlet ReporteControlPatrimonio. This Servlet was made by Pablo Sarobe modified by everybody";
  } // end of getServletInfo() method
}
