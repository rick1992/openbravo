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
import java.text.DecimalFormat;
import java.text.NumberFormat;
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
import org.openbravo.erpCommon.ad_forms.AcctServer;
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
import org.openbravo.model.financialmgmt.assetmgmt.Asset;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.accounting.StructPle;
import pe.com.unifiedgo.accounting.SunatUtil;

public class ReportLibroInventariosYBalance34 extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (log4j.isDebugEnabled())
      log4j.debug("Command: " + vars.getStringParameter("Command"));

    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom",
          "ReportLibroInventariosYBalance34|DateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo",
          "ReportLibroInventariosYBalance34|DateTo", "");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportLibroInventariosYBalance34|Org", "0");
      String strRecord = vars.getGlobalVariable("inpRecord",
          "ReportLibroInventariosYBalance34|Record", "");
      String strTable = vars.getGlobalVariable("inpTable",
          "ReportLibroInventariosYBalance34|Table", "");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg, strTable, strRecord);

    } else if (vars.commandIn("DIRECT")) {
      String strTable = vars
          .getGlobalVariable("inpTable", "ReportLibroInventariosYBalance34|Table");
      String strRecord = vars.getGlobalVariable("inpRecord",
          "ReportLibroInventariosYBalance34|Record");

      setHistoryCommand(request, "DIRECT");
      vars.setSessionValue("ReportLibroInventariosYBalance34.initRecordNumber", "0");
      printPageDataSheet(response, vars, "", "", "", strTable, strRecord);

    } else if (vars.commandIn("FIND")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportLibroInventariosYBalance34|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReportLibroInventariosYBalance34|DateTo");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportLibroInventariosYBalance34|Org", "0");
      vars.setSessionValue("ReportLibroInventariosYBalance34.initRecordNumber", "0");
      setHistoryCommand(request, "DEFAULT");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg, "", "");

    } else if (vars.commandIn("PDF", "XLS")) {
      if (log4j.isDebugEnabled())
        log4j.debug("PDF");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportLibroInventariosYBalance34|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReportLibroInventariosYBalance34|DateTo");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportLibroInventariosYBalance34|Org", "0");

      String strTable = vars.getStringParameter("inpTable");
      String strRecord = vars.getStringParameter("inpRecord");
      setHistoryCommand(request, "DEFAULT");
      printPagePDF(request, response, vars, strDateFrom, strDateTo, strOrg, strTable, strRecord);

    } else
      pageError(response);
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

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strOrg, String strTable, String strRecord)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReportLibroInventariosYBalance34Data[] data = null;
    String strPosition = "0";
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportLibroInventariosYBalance34",
        false, "", "", "imprimir();return false;", false, "ad_reports", strReplaceWith, false, true);
    toolbar.setEmail(false);

    toolbar.prepareSimpleToolBarTemplate();

    toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");

    if (data == null || data.length == 0) {
      String discard[] = { "secTable" };

      xmlDocument = xmlEngine.readXmlTemplate(
          "pe/com/unifiedgo/report/ad_reports/ReportLibroInventariosYBalance34", discard)
          .createXmlDocument();
      data = ReportLibroInventariosYBalance34Data.set("0");
      data[0].rownum = "0";
    }
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "pe.com.unifiedgo.report.ad_reports.ReportLibroInventariosYBalance34");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ReportLibroInventariosYBalance34.html", classInfo.id, classInfo.type, strReplaceWith,
          tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
          "ReportLibroInventariosYBalance34.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportLibroInventariosYBalance34");
      vars.removeMessage("ReportLibroInventariosYBalance34");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
          "49DC1D6F086945AB82F84C66F5F13F16", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReportLibroInventariosYBalance34"), Utility.getContext(this, vars, "#User_Client",
              "ReportLibroInventariosYBalance34"), '*');
      comboTableData.fillParameters(null, "ReportLibroInventariosYBalance34", "");
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
        ReportLibroInventariosYBalance34Data.select_periodos(this)));

    vars.setSessionValue("ReportLibroInventariosYBalance34|Record", strRecord);
    vars.setSessionValue("ReportLibroInventariosYBalance34|Table", strTable);

    xmlDocument.setData("structure1", data);
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPagePDF(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String strOrg,
      String strTable, String strRecord) throws IOException, ServletException {

    ReportLibroInventariosYBalance34Data[] data = null;

    String[] fechaAnioActual = strDateTo.split("-");
    Integer anioActual = new Integer(fechaAnioActual[2]);
    String fechaIniAnioActual = "01-01-" + anioActual;

    data = getDataUltimaVersionCuenta34(this, vars, strDateFrom, strDateTo,
        Utility.getContext(this, vars, "#User_Client", "ReportLibroInventariosYBalance34"), strOrg);

    String claseAnterior = "";
    Integer claseOrden = 0;

    for (int i = 0; i < data.length; i++) {

      if (claseAnterior.compareToIgnoreCase(data[i].clase) != 0) {
        claseAnterior = data[i].clase;
        claseOrden++;
      }
      data[i].idperiodo = claseOrden.toString();

    }

    String strSubtitle = (Utility.messageBD(this, "LegalEntity", vars.getLanguage()) + ": ")
        + ReportLibroInventariosYBalance34Data.selectCompany(this, vars.getClient()) + "\n"
        + "RUC:" + ReportLibroInventariosYBalance34Data.selectRucOrg(this, strOrg) + "\n";
    ;

    if (!("0".equals(strOrg)))
      strSubtitle += (Utility.messageBD(this, "OBUIAPP_Organization", vars.getLanguage()) + ": ")
          + ReportLibroInventariosYBalance34Data.selectOrg(this, strOrg) + "\n";

    if (data.length == 0) {
      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
      return;
    }

    String strOutput;
    String strReportName;
    if (vars.commandIn("PDF")) {
      strOutput = "pdf";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportLibroInventariosYBalance34.jrxml";
    } else {
      strOutput = "xls";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportLibroInventariosYBalance34Excel.jrxml";
    }

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("Subtitle", strSubtitle);
    parameters.put("Ruc", ReportLibroInventariosYBalance34Data.selectRucOrg(this, strOrg));
    parameters.put("Razon", ReportLibroInventariosYBalance34Data.selectOrg(this, strOrg));
    // parameters.put("TaxID",
    // ReportSalesRevenueRecordsData.selectOrgTaxID(this, strOrg));
    parameters.put("strDateFrom", fechaIniAnioActual);
    parameters.put("strDateTo", strDateTo);
    parameters.put("dateTo", StringToDate(strDateTo));

    renderJR(vars, response, strReportName, "Libro Inventarios y Balance 34", strOutput,
        parameters, data, null);
  }

  private static ReportLibroInventariosYBalance34Data[] getDataUltimaVersionCuenta34(
      ConnectionProvider conn, VariablesSecureApp vars, String strDateFrom, String strDateTo,
      String adUserClient, String strOrg) throws IOException, ServletException {

    ReportLibroInventariosYBalance34Data[] data = null;

    String strTreeOrg = TreeData.getTreeOrg(conn, vars.getClient());
    String strOrgFamily = getFamily(conn, strTreeOrg, strOrg);

    data = ReportLibroInventariosYBalance34Data.selectActivosIntangiblesCuenta34(conn, strDateTo,
        adUserClient, strOrgFamily);

    for (int i = 0; i < data.length; i++) {

      if (data[i].isdepreciated.equals("N")) {
        data[i].depreciacion = "0.00";
        data[i].vidaUtil = "0";
        data[i].vidaXCumplir = "0";
        data[i].tipoRegresion = "--";
        data[i].depreciacionAcumulada = "0.00";
        data[i].depreciacionAcumuladaInflasion = "0.00";
        data[i].depreciacionHistorica = "0.00";
        data[i].activoNeto = data[i].assetvalueamt;

      }

      if (Integer.valueOf(data[i].vidaXCumplir) < 0) {
        data[i].vidaXCumplir = "0.0";
      }

      if (data[i].idMoneda.compareToIgnoreCase("100") == 0) {

        BigDecimal tipoCambio = new BigDecimal("1.0000");

        tipoCambio = getTasaCambioxFecha(data[i].fechaAdquisicion, vars, strOrg, false);

        data[i].saldoInicial = (new BigDecimal(data[i].saldoInicial).multiply(tipoCambio))
            .setScale(2, BigDecimal.ROUND_HALF_UP).toString();

        data[i].adquisicionesAdicionales = (new BigDecimal(data[i].adquisicionesAdicionales)
            .multiply(tipoCambio)).setScale(2, BigDecimal.ROUND_HALF_UP).toString();

        data[i].mejoras = (new BigDecimal(data[i].mejoras).multiply(tipoCambio)).setScale(2,
            BigDecimal.ROUND_HALF_UP).toString();

        data[i].valorHistorico = (new BigDecimal(data[i].valorHistorico).multiply(tipoCambio))
            .setScale(2, BigDecimal.ROUND_HALF_UP).toString();

        data[i].valorAjustado = (new BigDecimal(data[i].valorAjustado).multiply(tipoCambio))
            .setScale(2, BigDecimal.ROUND_HALF_UP).toString();

        data[i].depreciacionAcumulada = (new BigDecimal(data[i].depreciacionAcumulada)
            .multiply(tipoCambio)).setScale(2, BigDecimal.ROUND_HALF_UP).toString();

        data[i].depreciacionEjercicio = (new BigDecimal(data[i].depreciacionEjercicio)
            .multiply(tipoCambio)).setScale(2, BigDecimal.ROUND_HALF_UP).toString();

        data[i].depreciacionHistorica = (new BigDecimal(data[i].depreciacionHistorica)
            .multiply(tipoCambio)).setScale(2, BigDecimal.ROUND_HALF_UP).toString();

        data[i].depreciacionAcumuladaInflasion = (new BigDecimal(
            data[i].depreciacionAcumuladaInflasion).multiply(tipoCambio)).setScale(2,
            BigDecimal.ROUND_HALF_UP).toString();

        data[i].activoNeto = (new BigDecimal(data[i].activoNeto).multiply(tipoCambio)).setScale(2,
            BigDecimal.ROUND_HALF_UP).toString();

        data[i].depreciacionBajas = (new BigDecimal(data[i].depreciacionBajas).multiply(tipoCambio))
            .setScale(2, BigDecimal.ROUND_HALF_UP).toString();

        data[i].retiroBajas = (new BigDecimal(data[i].retiroBajas).multiply(tipoCambio)).setScale(
            2, BigDecimal.ROUND_HALF_UP).toString();
      }
    }

    return data;
  }

  public static StructPle getPLECuenta34(ConnectionProvider conn, VariablesSecureApp vars,
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

    ReportLibroInventariosYBalance34Data[] dataFinal = getDataUltimaVersionCuenta34(conn, vars,
        sdf.format(dateFrom), sdf.format(dateTo), adUserClient, strOrg);

    System.out.println("datafinal length:" + dataFinal.length);

    StructPle sunatPle = getStringDataCuenta34(dataFinal, dateFrom, dateTo, strOrg);

    return sunatPle;
  }

  private static StructPle getStringDataCuenta34(ReportLibroInventariosYBalance34Data[] data,
      Date dateFrom, Date dateTo, String strOrg) {
    StructPle sunatPle = new StructPle();
    sunatPle.numEntries = 0;

    StringBuffer sb = new StringBuffer();

    Organization org = OBDal.getInstance().get(Organization.class, strOrg);
    String rucAdq = SunatUtil.LPad(org.getOrganizationInformationList().get(0).getTaxID(), 11, '0');

    SimpleDateFormat formatterForm = new SimpleDateFormat("dd-MM-yyyy");

    SimpleDateFormat dt = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat dt2 = new SimpleDateFormat("dd/MM/yyyy");

    String filename = "LE" + rucAdq + dt.format(dateTo) + "030900011111.TXT"; // LERRRRRRRRRRRAAAAMMDD030900CCOIM1.TXT

    ReportLibroInventariosYBalance34Data[] le = data;

    NumberFormat formatter = new DecimalFormat("#0.00");

    String periodoTrib = dt.format(dateTo);
    String tipoAsiento = "M00001";
    String estadoOp = "1";

    int jj = 0;
    for (int i = 0; i < le.length; i++) {

      ReportLibroInventariosYBalance34Data led = le[i];

      Asset asset = OBDal.getInstance().get(Asset.class, led.aAssetId);

      String regnumber = asset.getId() + dt.format(dateTo);

      String codCuentaActivo = led.codigoCuenta;
      String descripcionActivo = led.activo;
      String importeAjusteActivo = formatter.format(Double.parseDouble(led.valorAjustado));
      String importeAjusteInflacion = formatter.format(Double
          .parseDouble(led.depreciacionAcumuladaInflasion));

      String fechaIni = "";
      try {
        fechaIni = dt2.format(formatterForm.parse(led.fechaInicio));
      } catch (Exception ex) {
        System.out.println("Exception en fecha inicio: " + fechaIni);
      }

      String linea = periodoTrib + "|" + regnumber + "|" + tipoAsiento + "|" + fechaIni + "|"
          + codCuentaActivo + "|" + descripcionActivo + "|" + importeAjusteActivo + "|"
          + importeAjusteInflacion + "|" + estadoOp + "|";

      if (jj > 0)
        sb.append("\n");
      sb.append(linea);
      sunatPle.numEntries++;
      jj++;
    }

    sunatPle.filename = filename;
    sunatPle.data = sb.toString();
    return sunatPle;
  }

  private static BigDecimal getTasaCambioxFecha(String fecUsar, VariablesSecureApp vars,
      String strOrg, Boolean modoCambio) {// true
                                          // :
                                          // ventas
                                          // ;
                                          // false
                                          // :compra

    String amountConverted = "";
    String currencyPEN_id = "308";
    String currencyUSD_id = "100";

    try {
      if (modoCambio) {
        amountConverted = AcctServer.getConvertedAmt("1000.00", currencyUSD_id, currencyPEN_id,
            fecUsar, "", vars.getClient(), strOrg, // cambio
                                                   // por
            // la p
            new DalConnectionProvider());
      } else {
        amountConverted = AcctServer.getConvertedAmt("1000000.00", currencyPEN_id, currencyUSD_id,
            fecUsar, "", vars.getClient(), strOrg, new DalConnectionProvider());
        amountConverted = new BigDecimal(amountConverted).divide(new BigDecimal("1000000.00"), 6,
            BigDecimal.ROUND_HALF_UP).toString();
      }

    } catch (Exception e) {
      return new BigDecimal("1.00");
    }

    BigDecimal tipoCambio;

    if (modoCambio) {
      tipoCambio = new BigDecimal(amountConverted).divide(new BigDecimal("1000.00"), 4,
          BigDecimal.ROUND_HALF_UP);
    } else {
      tipoCambio = new BigDecimal("1.00").divide(new BigDecimal(amountConverted), 8,
          BigDecimal.ROUND_HALF_UP);
    }

    tipoCambio = tipoCambio.setScale(4, BigDecimal.ROUND_HALF_UP);

    return tipoCambio;
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
    return "Servlet ReportLibroInventariosYBalance34. This Servlet was made by Pablo Sarobe modified by everybody";
  } // end of getServletInfo() method
}
