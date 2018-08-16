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
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
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

public class ReportRegistroActivosFijos extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (log4j.isDebugEnabled())
      log4j.debug("Command: " + vars.getStringParameter("Command"));

    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom",
          "ReportRegistroActivosFijos|DateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportRegistroActivosFijos|DateTo",
          "");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportRegistroActivosFijos|Org", "0");
      String strRecord = vars.getGlobalVariable("inpRecord", "ReportRegistroActivosFijos|Record",
          "");
      String strTable = vars.getGlobalVariable("inpTable", "ReportRegistroActivosFijos|Table", "");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg, strTable, strRecord);

    } else if (vars.commandIn("DIRECT")) {
      String strTable = vars.getGlobalVariable("inpTable", "ReportRegistroActivosFijos|Table");
      String strRecord = vars.getGlobalVariable("inpRecord", "ReportRegistroActivosFijos|Record");

      setHistoryCommand(request, "DIRECT");
      vars.setSessionValue("ReportRegistroActivosFijos.initRecordNumber", "0");
      printPageDataSheet(response, vars, "", "", "", strTable, strRecord);

    } else if (vars.commandIn("FIND")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportRegistroActivosFijos|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReportRegistroActivosFijos|DateTo");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportRegistroActivosFijos|Org", "0");
      vars.setSessionValue("ReportRegistroActivosFijos.initRecordNumber", "0");
      setHistoryCommand(request, "DEFAULT");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg, "", "");

    } else if (vars.commandIn("PDF", "XLS")) {
      if (log4j.isDebugEnabled())
        log4j.debug("PDF");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportRegistroActivosFijos|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReportRegistroActivosFijos|DateTo");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportRegistroActivosFijos|Org", "0");
      String strReporteAnual = vars.getStringParameter("inpReporteAnual");

      String strTable = vars.getStringParameter("inpTable");
      String strRecord = vars.getStringParameter("inpRecord");
      setHistoryCommand(request, "DEFAULT");
      printPagePDF(request, response, vars, strDateFrom, strDateTo, strOrg, strReporteAnual,
          strTable, strRecord);

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
    ReportRegistroActivosFijosData[] data = null;
    String strPosition = "0";
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportRegistroActivosFijos", false,
        "", "", "imprimir();return false;", false, "ad_reports", strReplaceWith, false, true);
    toolbar.setEmail(false);
    toolbar.prepareSimpleToolBarTemplate();

    toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");

    if (data == null || data.length == 0) {
      String discard[] = { "secTable" };
      // toolbar.prepareRelationBarTemplate(
      // false,
      // false,
      // "submitCommandForm('XLS', false, null, 'ReportRegistroActivosFijos.xls', 'EXCEL');return false;");
      xmlDocument = xmlEngine.readXmlTemplate(
          "pe/com/unifiedgo/report/ad_reports/ReportRegistroActivosFijos", discard)
          .createXmlDocument();
      data = ReportRegistroActivosFijosData.set("0");
      data[0].rownum = "0";
    }

    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "pe.com.unifiedgo.report.ad_reports.ReportRegistroActivosFijos");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ReportRegistroActivosFijos.html", classInfo.id, classInfo.type, strReplaceWith,
          tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
          "ReportRegistroActivosFijos.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportRegistroActivosFijos");
      vars.removeMessage("ReportRegistroActivosFijos");
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
              "ReportRegistroActivosFijos"), Utility.getContext(this, vars, "#User_Client",
              "ReportRegistroActivosFijos"), '*');
      comboTableData.fillParameters(null, "ReportRegistroActivosFijos", "");
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
    vars.setSessionValue("ReportRegistroActivosFijos|Record", strRecord);
    vars.setSessionValue("ReportRegistroActivosFijos|Table", strTable);

    xmlDocument.setParameter("paramPeriodosArray", Utility.arrayInfinitasEntradas(
        "idperiodo;periodo;fechainicial;fechafinal;idorganizacion", "arrPeriodos",
        ReportRegistroActivosFijosData.select_periodos(this)));

    xmlDocument.setData("structure1", data);
    out.println(xmlDocument.print());
    out.close();
  }

  // private ReportRegistroActivosFijosData[]
  // notshow(ReportRegistroActivosFijosData[] data,
  // VariablesSecureApp vars) {
  // for (int i = 0; i < data.length - 1; i++) {
  // if ((data[i].identifier.toString().equals(data[i +
  // 1].identifier.toString()))
  // && (data[i].dateacct.toString().equals(data[i + 1].dateacct.toString())))
  // {
  // data[i + 1].newstyle = "visibility: hidden";
  // }
  // }
  // return data;
  // }

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
      String strReporteAnual, String strTable, String strRecord) throws IOException,
      ServletException {

    // ReportRegistroActivosFijosData[] data = getData(this, vars, strDateFrom, strDateTo,
    // Utility.getContext(this, vars, "#User_Client", "ReportRegistroActivosFijos"), strOrg);
    // ReportRegistroActivosFijosData[] data = getDataNuevaVersion(this, vars, strDateFrom,
    // strDateTo, Utility.getContext(this, vars, "#User_Client", "ReportRegistroActivosFijos"),
    // strOrg);

    ReportRegistroActivosFijosData[] data = null;

    String strOutput;
    String strReportName;

    if (strReporteAnual.equals("Y")) {
      data = getDataUltimaVersionAnual(this, vars, strDateFrom, strDateTo,
          Utility.getContext(this, vars, "#User_Client", "ReportRegistroActivosFijos"), strOrg);

      String claseAnterior = "";
      Integer claseOrden = 0;
      BigDecimal depreciacionAnual = BigDecimal.ZERO;
      BigDecimal adquisiciones = BigDecimal.ZERO;
      BigDecimal saldoInicial = BigDecimal.ZERO;

      for (int i = 0; i < data.length; i++) {

        if (claseAnterior.compareToIgnoreCase(data[i].clase) != 0) {
          claseAnterior = data[i].clase;
          claseOrden++;
        }

        adquisiciones = new BigDecimal(data[i].adquisicionesAdicionales);
        saldoInicial = new BigDecimal(data[i].saldoInicial);

        depreciacionAnual = new BigDecimal(data[i].depreciacionEjercicio1)
            .add(new BigDecimal(data[i].depreciacionEjercicio2))
            .add(new BigDecimal(data[i].depreciacionEjercicio3))
            .add(new BigDecimal(data[i].depreciacionEjercicio4))
            .add(new BigDecimal(data[i].depreciacionEjercicio5))
            .add(new BigDecimal(data[i].depreciacionEjercicio6))
            .add(new BigDecimal(data[i].depreciacionEjercicio7))
            .add(new BigDecimal(data[i].depreciacionEjercicio8))
            .add(new BigDecimal(data[i].depreciacionEjercicio9))
            .add(new BigDecimal(data[i].depreciacionEjercicio10))
            .add(new BigDecimal(data[i].depreciacionEjercicio11))
            .add(new BigDecimal(data[i].depreciacionEjercicio12));

        data[i].depreciacionAnual = depreciacionAnual.toString();
        data[i].idperiodo = claseOrden.toString();
        data[i].saldoInicial = (adquisiciones.compareTo(BigDecimal.ZERO) != 0 ? adquisiciones
            : saldoInicial).toString();

      }

      if (vars.commandIn("PDF")) {
        strOutput = "pdf";
        strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportRegistroActivosFijosControlDepreciacion.jrxml";
      } else {
        strOutput = "xls";
        strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportRegistroActivosFijosControlDepreciacionExcel.jrxml";
      }

    } else {
      data = getDataUltimaVersion(this, vars, strDateFrom, strDateTo,
          Utility.getContext(this, vars, "#User_Client", "ReportRegistroActivosFijos"), strOrg);

      String claseAnterior = "";
      Integer claseOrden = 0;

      for (int i = 0; i < data.length; i++) {

        if (claseAnterior.compareToIgnoreCase(data[i].clase) != 0) {
          claseAnterior = data[i].clase;
          claseOrden++;
        }
        data[i].idperiodo = claseOrden.toString();

      }

      if (vars.commandIn("PDF")) {
        strOutput = "pdf";
        strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportRegistroActivosFijos.jrxml";
      } else {
        strOutput = "xls";
        strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportRegistroActivosFijosExcel.jrxml";
      }
    }

    Integer anioAnterior = new Integer(strDateTo.split("-")[2]) - 1;

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("Razon", ReportRegistroActivosFijosData.selectRucOrg(this, strOrg));
    parameters.put("Ruc", ReportRegistroActivosFijosData.selectSocialName(this, strOrg));
    parameters.put("dateFrom", StringToDate(strDateFrom));
    parameters.put("dateTo", StringToDate(strDateTo));
    parameters.put("ANIO_ANTERIOR", anioAnterior);

    if (data.length == 0) {
      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
      return;
    }
    renderJR(vars, response, strReportName, "Registro_Activos_Fijos", strOutput, parameters, data,
        null);
  }

  private static ReportRegistroActivosFijosData[] getData(ConnectionProvider conn,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String adUserClient,
      String strOrg) throws IOException, ServletException {

    ReportRegistroActivosFijosData[] data = null;

    String strTreeOrg = TreeData.getTreeOrg(conn, vars.getClient());
    String strOrgFamily = getFamily(conn, strTreeOrg, strOrg);

    data = ReportRegistroActivosFijosData.selectActivosFijos(conn, strDateTo, strDateTo, strDateTo,
        strDateTo, strDateFrom, strDateFrom, strDateFrom, strDateTo, strDateFrom, strDateTo,
        strDateTo, strDateFrom, adUserClient, strOrgFamily);

    for (int i = 0; i < data.length; i++) {

      if (data[i].isdepreciated.equals("N")) {
        data[i].depreciacion = "0.00";
        data[i].vidaUtil = "0";
        data[i].vidaXCumplir = "0";
        data[i].tipoRegresion = "--";
        data[i].depreciacionAcumulada = "0.00";
        data[i].depreciacionAcumuladaInflasion = "0.00";
        data[i].depreciacionHistorica = "0.00";
        data[i].activoNeto = data[i].valorHistorico;
        data[i].saldoInicial = data[i].valorHistorico;
      } else {
        if (Integer.valueOf(data[i].vidaUtil) != Integer.valueOf(data[i].vidaUtilScr)) {

          // vida cumplida hasta el inicio del saldo
          data[i].vidaCumplida = (Integer.valueOf(Integer.valueOf(data[i].vidaUtilScr)
              - Integer.valueOf(data[i].vidaUtil))).toString();
          data[i].vidaUtil = data[i].vidaUtilScr;

          // fecha de inicio
          String fecha = data[i].fechaInicio;
          SimpleDateFormat formatterForm = new SimpleDateFormat("dd-MM-yyyy");
          Date fechaNuevaInicio = null;
          try {
            fechaNuevaInicio = formatterForm.parse(fecha);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(fechaNuevaInicio);
            calendar.add(Calendar.MONTH, -Integer.valueOf(data[i].vidaCumplida));

            // UGO CHANGE: Do not change fechainicio as indicated by COAM for migrated assets
            if (data[i].emScoIsmigrated.equals("N"))
              data[i].fechaInicio = formatterForm.format(calendar.getTime());

            Date fechaTo = formatterForm.parse(strDateTo);
            Calendar calendar2 = Calendar.getInstance();
            calendar2.setTime(fechaTo);

            int diffYear = calendar2.get(Calendar.YEAR) - calendar.get(Calendar.YEAR);
            int diffMonth = diffYear * 12 + calendar2.get(Calendar.MONTH)
                - calendar.get(Calendar.MONTH);

            data[i].vidaCumplida = String.valueOf(diffMonth + 1);

            data[i].vidaXCumplir = (Integer.valueOf(Integer.valueOf(data[i].vidaUtil)
                - Integer.valueOf(data[i].vidaCumplida))).toString();

          } catch (Exception ex) {
            System.out.println("Exception: " + strDateFrom);
          }

        }
      }

      if (Integer.valueOf(data[i].vidaXCumplir) < 0) {
        data[i].vidaXCumplir = "0.0";
      }

      if (data[i].idMoneda.compareToIgnoreCase("100") == 0) {

        BigDecimal tipoCambio = new BigDecimal("1.0000");

        tipoCambio = getTasaCambioxFecha(data[i].fechaAdquisicion, vars, strOrg, false);

        data[i].saldoInicial = (new BigDecimal(data[i].saldoInicial).multiply(tipoCambio))
            .setScale(2, BigDecimal.ROUND_HALF_UP).toString();

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

      }

      data[i].retiroBajas = data[i].depreciacionBajas;
    }

    return data;
  }

  private static ReportRegistroActivosFijosData[] getDataNuevaVersion(ConnectionProvider conn,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String adUserClient,
      String strOrg) throws IOException, ServletException {

    ReportRegistroActivosFijosData[] data = null;

    String strTreeOrg = TreeData.getTreeOrg(conn, vars.getClient());
    String strOrgFamily = getFamily(conn, strTreeOrg, strOrg);

    data = ReportRegistroActivosFijosData.selectActivosFijosNuevaVersion(conn, strDateTo,
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

      } else {
        if (Integer.valueOf(data[i].vidaUtil) != Integer.valueOf(data[i].vidaUtilScr)) {

          // vida cumplida hasta el inicio del saldo
          data[i].vidaCumplida = (Integer.valueOf(Integer.valueOf(data[i].vidaUtilScr)
              - Integer.valueOf(data[i].vidaUtil))).toString();
          data[i].vidaUtil = data[i].vidaUtilScr;

          // fecha de inicio
          String fecha = data[i].fechaInicio;
          SimpleDateFormat formatterForm = new SimpleDateFormat("dd-MM-yyyy");
          Date fechaNuevaInicio = null;
          try {
            fechaNuevaInicio = formatterForm.parse(fecha);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(fechaNuevaInicio);
            calendar.add(Calendar.MONTH, -Integer.valueOf(data[i].vidaCumplida));

            // UGO CHANGE: Do not change fechainicio as indicated by COAM for migrated assets
            if (data[i].emScoIsmigrated.equals("N"))
              data[i].fechaInicio = formatterForm.format(calendar.getTime());

            Date fechaTo = formatterForm.parse(strDateTo);
            Calendar calendar2 = Calendar.getInstance();
            calendar2.setTime(fechaTo);

            int diffYear = calendar2.get(Calendar.YEAR) - calendar.get(Calendar.YEAR);
            int diffMonth = diffYear * 12 + calendar2.get(Calendar.MONTH)
                - calendar.get(Calendar.MONTH);

            data[i].vidaCumplida = String.valueOf(diffMonth + 1);

            data[i].vidaXCumplir = (Integer.valueOf(Integer.valueOf(data[i].vidaUtil)
                - Integer.valueOf(data[i].vidaCumplida))).toString();

          } catch (Exception ex) {
            ex.printStackTrace();
          }

        }
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

  private static ReportRegistroActivosFijosData[] getDataUltimaVersion(ConnectionProvider conn,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String adUserClient,
      String strOrg) throws IOException, ServletException {

    ReportRegistroActivosFijosData[] data = null;

    String strTreeOrg = TreeData.getTreeOrg(conn, vars.getClient());
    String strOrgFamily = getFamily(conn, strTreeOrg, strOrg);

    data = ReportRegistroActivosFijosData.selectActivosFijosUltimaVersion(conn, strDateTo,
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
        
        if(data[i].estadoActivo.equals("SCO_RET")){
          data[i].activoNeto = "0.00";          
        }
        else{
          data[i].activoNeto = data[i].assetvalueamt;          
        }
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

  private static ReportRegistroActivosFijosData[] getDataUltimaVersionAnual(
      ConnectionProvider conn, VariablesSecureApp vars, String strDateFrom, String strDateTo,
      String adUserClient, String strOrg) throws IOException, ServletException {

    ReportRegistroActivosFijosData[] data = null;

    String strTreeOrg = TreeData.getTreeOrg(conn, vars.getClient());
    String strOrgFamily = getFamily(conn, strTreeOrg, strOrg);

    String[] partesFecha = strDateTo.split("-");
    String anio = partesFecha[2];

    data = ReportRegistroActivosFijosData.selectActivosFijosAnualUltimaVersion(conn, strDateTo,
        anio, adUserClient, strOrgFamily);

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

  public static StructPle getPLE(ConnectionProvider conn, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String adUserClient, String strOrg) throws Exception {

    ReportRegistroActivosFijosData[] dataFinal = getDataUltimaVersion(conn, vars, strDateFrom,
        strDateTo, adUserClient, strOrg);

    System.out.println("datafinal length:" + dataFinal.length);

    StructPle sunatPle = getStringData(dataFinal, strDateFrom, strDateTo, strOrg);

    return sunatPle;
  }

  private static StructPle getStringData(ReportRegistroActivosFijosData[] data, String strDateFrom,
      String strDateTo, String strOrg) {
    StructPle sunatPle = new StructPle();
    sunatPle.numEntries = 0;

    StringBuffer sb = new StringBuffer();

    int correlativo = 0;
    String prevRegNumber = "";
    Organization org = OBDal.getInstance().get(Organization.class, strOrg);
    String rucAdq = SunatUtil.LPad(org.getOrganizationInformationList().get(0).getTaxID(), 11, '0');

    SimpleDateFormat formatterForm = new SimpleDateFormat("dd-MM-yyyy");
    Date dttFrom = null;
    try {
      dttFrom = formatterForm.parse(strDateFrom);
    } catch (Exception ex) {
      System.out.println("Exception: " + strDateFrom);
    }

    SimpleDateFormat dt = new SimpleDateFormat("yyyy");
    SimpleDateFormat dt2 = new SimpleDateFormat("dd/MM/yyyy");

    String filename = "LE" + rucAdq + dt.format(dttFrom) + "00" + "00070100001111.TXT";// LERRRRRRRRRRRAAAAMM0006010000OIM1.TXT

    ReportRegistroActivosFijosData[] le = data;

    NumberFormat formatter = new DecimalFormat("#0.00");
    NumberFormat formatter3 = new DecimalFormat("#0.000");

    int jj = 0;

    for (int i = 0; i < le.length; i++) {

      ReportRegistroActivosFijosData led = le[i];

      Date dttAcct = null;
      try {
        dttAcct = formatterForm.parse(strDateFrom);
      } catch (Exception ex) {
      }
      String periodoTrib = dt.format(dttAcct) + "0000";

      String tipoAsiento = "M00001";

      Asset asset = OBDal.getInstance().get(Asset.class, led.aAssetId);

      String regnumber = asset.getId() + dt.format(dttAcct);

      String codCatalogo = "9";
      String codActivo = led.codigoActivo;
      String codigoExistencia = ""; // obligatorio en el 2018
      String codTipoActivo = "2"; // REVALUADO CON EFECTO TRIBUTARIO
      String codCuentaActivo = led.codigoCuenta;
      String estadoActivo = "9";
      String descripcionActivo = led.activo;
      String marcaActivo = "-";
      String modeloActivo = "-";
      String importeInicial = formatter.format(Double.parseDouble(led.saldoInicial));
      String nroSerieActivo = "-";
      String importeAdiciones = formatter.format(Double.parseDouble(led.adquisicionesAdicionales));
      String importeMejoras = formatter.format(Double.parseDouble(led.mejoras));
      String importeBajas = formatter.format(Double.parseDouble(led.depreciacionBajas));
      String importeAjustes = "0.00";
      String importeRevaluacion = "0.00";
      String importeRevaluacionOrg = "0.00";
      String importeRevaluacionOtros = "0.00";
      String importeAjusteInflacion = formatter.format(Double
          .parseDouble(led.depreciacionAcumuladaInflasion));

      String metodoAplicadoDepr = "1"; // linea recta
      String docAuthorMetodo = "-";

      String porcentajeDepr = led.depreciacion;
      String depreciacionAccum = formatter.format(Double.parseDouble(led.depreciacionAcumulada));
      String depreciacionEjercicio = formatter
          .format(Double.parseDouble(led.depreciacionEjercicio));
      String depreciacionBajaEjercicio = formatter
          .format(Double.parseDouble(led.depreciacionBajas));
      String depreciacionOtrosAjustes = "0.00";
      String depreciacionReevalVoluntaria = "0.00";
      String depreciacionReevalOrg = "0.00";
      String depreciacionReevalOtros = "0.00";
      String depreciacionValorAjusteInfl = "0.00";

      String fechaAdq = "";
      String fechaIni = "";
      try {
        fechaAdq = dt2.format(formatterForm.parse(led.fechaAdquisicion));
        fechaIni = dt2.format(formatterForm.parse(led.fechaInicio));
      } catch (Exception ex) {
      }

      String estadoOp = "1";

      // GET THE VALUE OF em_sco_amort_anualperc (data.depreciacion) LIKE THE PDF REPORT
      /*
       * if(asset.getCalculateType().equals("PE")){ porcentajeDepr =
       * formatter.format(asset.getAnnualDepreciation()); }else
       * if(asset.getCalculateType().equals("TI")){ if(asset.getAmortize().equals("YE")){
       * porcentajeDepr = formatter.format( new BigDecimal(100).divide(new
       * BigDecimal(asset.getUsableLifeYears()), 2, RoundingMode.HALF_UP) ); }else
       * if(asset.getAmortize().equals("MO")){ System.out.println("asset:" + asset.getIdentifier() +
       * " - asset.getUsableLifeMonths():" + asset.getUsableLifeMonths()); porcentajeDepr =
       * formatter.format( new BigDecimal(1200).divide(new BigDecimal(asset.getUsableLifeMonths()),
       * 2, RoundingMode.HALF_UP) ); } }
       */

      String linea = periodoTrib + "|" + regnumber + "|" + tipoAsiento + "|" + codCatalogo + "|"
          + codActivo + "|" + codigoExistencia + "|" + codTipoActivo + "|" + codCuentaActivo + "|"
          + estadoActivo + "|" + descripcionActivo + "|" + marcaActivo + "|" + modeloActivo + "|"
          + nroSerieActivo + "|" + importeInicial + "|" + importeAdiciones + "|" + importeMejoras
          + "|" + importeBajas + "|" + importeAjustes + "|" + importeRevaluacion + "|"
          + importeRevaluacionOrg + "|" + importeRevaluacionOtros + "|" + importeAjusteInflacion
          + "|" + fechaAdq + "|" + fechaIni + "|" + metodoAplicadoDepr + "|" + docAuthorMetodo
          + "|" + porcentajeDepr + "|" + depreciacionAccum + "|" + depreciacionEjercicio + "|"
          + depreciacionBajaEjercicio + "|" + depreciacionOtrosAjustes + "|"
          + depreciacionReevalVoluntaria + "|" + depreciacionReevalOrg + "|"
          + depreciacionReevalOtros + "|" + depreciacionValorAjusteInfl + "|" + estadoOp + "|";

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
    return "Servlet ReportRegistroActivosFijos. This Servlet was made by Pablo Sarobe modified by everybody";
  } // end of getServletInfo() method
}
