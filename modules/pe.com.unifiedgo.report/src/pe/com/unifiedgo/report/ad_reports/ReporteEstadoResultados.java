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
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
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
import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class ReporteEstadoResultados extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (log4j.isDebugEnabled())
      log4j.debug("Command: " + vars.getStringParameter("Command"));

    if (vars.commandIn("DEFAULT")) {

      Calendar fActual = new GregorianCalendar();

      String fechaFinAnioAnterior = "31-12-" + (fActual.get(Calendar.YEAR) - 1);
      String fechaFinAnioActual = "31-12-" + (fActual.get(Calendar.YEAR));

      String strDateFrom = vars.getGlobalVariable("inpDateFrom",
          "ReporteEstadoResultados|DateFrom", fechaFinAnioAnterior);
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReporteEstadoResultados|DateTo",
          fechaFinAnioActual);
      String strOrg = vars.getGlobalVariable("inpOrg", "ReporteEstadoResultados|Org", "0");
      String strRecord = vars.getGlobalVariable("inpRecord", "ReporteEstadoResultados|Record", "");
      String strTable = vars.getGlobalVariable("inpTable", "ReporteEstadoResultados|Table", "");
      String strSoloUnMes = vars.getStringParameter("inpSoloUnMes");
      printPageDataSheet(request, response, vars, strDateFrom, strDateTo, strOrg, strTable,
          strRecord, strSoloUnMes);

    } else if (vars.commandIn("DIRECT")) {
      String strTable = vars.getGlobalVariable("inpTable", "ReporteEstadoResultados|Table");
      String strRecord = vars.getGlobalVariable("inpRecord", "ReporteEstadoResultados|Record");

      setHistoryCommand(request, "DIRECT");
      vars.setSessionValue("ReporteEstadoResultados.initRecordNumber", "0");
      printPageDataSheet(request, response, vars, "", "", "", strTable, strRecord, "");

    } else if (vars.commandIn("FIND")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReporteEstadoResultados|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReporteEstadoResultados|DateTo");
      String strSoloUnMes = vars.getStringParameter("inpSoloUnMes");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReporteEstadoResultados|Org", "0");
      vars.setSessionValue("ReporteEstadoResultados.initRecordNumber", "0");
      setHistoryCommand(request, "DEFAULT");
      printPageDataSheet(request, response, vars, strDateFrom, strDateTo, strOrg, "", "",
          strSoloUnMes);

    } else if (vars.commandIn("PDF", "XLS")) {
      if (log4j.isDebugEnabled())
        log4j.debug("PDF");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReporteEstadoResultados|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReporteEstadoResultados|DateTo");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReporteEstadoResultados|Org", "0");
      String strSoloUnMes = vars.getStringParameter("inpSoloUnMes");

      String strTable = vars.getStringParameter("inpTable");
      String strRecord = vars.getStringParameter("inpRecord");
      setHistoryCommand(request, "DEFAULT");
      printPagePDF(request, response, vars, strDateFrom, strDateTo, strOrg, strTable, strRecord,
          strSoloUnMes);

    } else
      pageError(response);
  }

  private ReporteEstadoResultadosData[] getFormattedData(String strOrg, String strDateFrom,
      String strDateTo, ReporteEstadoResultadosData[] estadoResultadosData) {
    DecimalFormat df = new DecimalFormat("0.00");
    df.setRoundingMode(RoundingMode.HALF_UP);

    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);

    String prev_categoria = "";
    int countRecord = 0;
    BigDecimal saldoAnioAnterior = BigDecimal.ZERO, saldoAnioActual = BigDecimal.ZERO;
    for (int i = 0; i < estadoResultadosData.length; i++) {
      countRecord++;

      if (i == 0) {
        prev_categoria = estadoResultadosData[i].categoria;

      } else if (prev_categoria.compareTo(estadoResultadosData[i].categoria) != 0) {
        ReporteEstadoResultadosData objRptEstadoResultadosData = new ReporteEstadoResultadosData();
        objRptEstadoResultadosData.order1 = estadoResultadosData[i - 1].order1;
        objRptEstadoResultadosData.order2 = estadoResultadosData[i - 1].order2;
        objRptEstadoResultadosData.categoria = estadoResultadosData[i - 1].categoria;
        objRptEstadoResultadosData.subCategoriaId = "--";
        objRptEstadoResultadosData.subCategoria = "<b>" + estadoResultadosData[i - 1].categoria
            + "</b>";
        objRptEstadoResultadosData.grupoCategoria = estadoResultadosData[i - 1].grupoCategoria;
        objRptEstadoResultadosData.idorganizacion = estadoResultadosData[i - 1].idorganizacion;
        objRptEstadoResultadosData.idperiodo = estadoResultadosData[i - 1].idperiodo;
        objRptEstadoResultadosData.periodo = estadoResultadosData[i - 1].periodo;
        objRptEstadoResultadosData.fechainicial = estadoResultadosData[i - 1].fechainicial;
        objRptEstadoResultadosData.fechafinal = estadoResultadosData[i - 1].fechafinal;
        objRptEstadoResultadosData.saldoAnioAnterior = "<b>" + df.format(saldoAnioAnterior)
            + "</b>";
        objRptEstadoResultadosData.saldoAnioActual = "<b>" + df.format(saldoAnioActual) + "</b>";
        objRptEstadoResultadosData.rownum = Long.toString(countRecord);

        objRptEstadoResultadosData.paramorgid = strOrg;
        objRptEstadoResultadosData.paramdatefrom = strDateFrom;
        objRptEstadoResultadosData.paramdateto = strDateTo;

        vector.addElement(objRptEstadoResultadosData);

        prev_categoria = estadoResultadosData[i].categoria;
        countRecord++;
      }

      ReporteEstadoResultadosData objRptEstadoResultadosData = new ReporteEstadoResultadosData();
      objRptEstadoResultadosData.order1 = estadoResultadosData[i].order1;
      objRptEstadoResultadosData.order2 = estadoResultadosData[i].order2;
      objRptEstadoResultadosData.categoria = estadoResultadosData[i].categoria;
      objRptEstadoResultadosData.subCategoriaId = estadoResultadosData[i].subCategoriaId;
      objRptEstadoResultadosData.subCategoria = estadoResultadosData[i].subCategoria;
      objRptEstadoResultadosData.grupoCategoria = estadoResultadosData[i].grupoCategoria;
      objRptEstadoResultadosData.idorganizacion = estadoResultadosData[i].idorganizacion;
      objRptEstadoResultadosData.idperiodo = estadoResultadosData[i].idperiodo;
      objRptEstadoResultadosData.periodo = estadoResultadosData[i].periodo;
      objRptEstadoResultadosData.fechainicial = estadoResultadosData[i].fechainicial;
      objRptEstadoResultadosData.fechafinal = estadoResultadosData[i].fechafinal;
      objRptEstadoResultadosData.saldoAnioAnterior = estadoResultadosData[i].saldoAnioAnterior;
      objRptEstadoResultadosData.saldoAnioActual = estadoResultadosData[i].saldoAnioActual;
      objRptEstadoResultadosData.rownum = Long.toString(countRecord);

      objRptEstadoResultadosData.paramorgid = strOrg;
      objRptEstadoResultadosData.paramdatefrom = strDateFrom;
      objRptEstadoResultadosData.paramdateto = strDateTo;

      vector.addElement(objRptEstadoResultadosData);

      saldoAnioAnterior = saldoAnioAnterior.add(new BigDecimal(
          estadoResultadosData[i].saldoAnioAnterior));
      saldoAnioActual = saldoAnioActual
          .add(new BigDecimal(estadoResultadosData[i].saldoAnioActual));
    }

    ReporteEstadoResultadosData objRptEstadoResultadosData = new ReporteEstadoResultadosData();
    objRptEstadoResultadosData.order1 = estadoResultadosData[estadoResultadosData.length - 1].order1;
    objRptEstadoResultadosData.order2 = estadoResultadosData[estadoResultadosData.length - 1].order2;
    objRptEstadoResultadosData.categoria = estadoResultadosData[estadoResultadosData.length - 1].categoria;
    objRptEstadoResultadosData.subCategoriaId = "--";
    objRptEstadoResultadosData.subCategoria = "<b>"
        + estadoResultadosData[estadoResultadosData.length - 1].categoria + "</b>";
    objRptEstadoResultadosData.grupoCategoria = estadoResultadosData[estadoResultadosData.length - 1].grupoCategoria;
    objRptEstadoResultadosData.idorganizacion = estadoResultadosData[estadoResultadosData.length - 1].idorganizacion;
    objRptEstadoResultadosData.idperiodo = estadoResultadosData[estadoResultadosData.length - 1].idperiodo;
    objRptEstadoResultadosData.periodo = estadoResultadosData[estadoResultadosData.length - 1].periodo;
    objRptEstadoResultadosData.fechainicial = estadoResultadosData[estadoResultadosData.length - 1].fechainicial;
    objRptEstadoResultadosData.fechafinal = estadoResultadosData[estadoResultadosData.length - 1].fechafinal;
    objRptEstadoResultadosData.saldoAnioAnterior = "<b>" + df.format(saldoAnioAnterior) + "</b>";
    objRptEstadoResultadosData.saldoAnioActual = "<b>" + df.format(saldoAnioActual) + "</b>";
    objRptEstadoResultadosData.rownum = Long.toString(countRecord);

    objRptEstadoResultadosData.paramorgid = strOrg;
    objRptEstadoResultadosData.paramdatefrom = strDateFrom;
    objRptEstadoResultadosData.paramdateto = strDateTo;

    vector.addElement(objRptEstadoResultadosData);

    ReporteEstadoResultadosData objectReporteEstadoResultadosData[] = new ReporteEstadoResultadosData[vector
        .size()];
    vector.copyInto(objectReporteEstadoResultadosData);
    return objectReporteEstadoResultadosData;
  }

  private static ReporteEstadoResultadosData[] getFormattedDataForPLE(String strOrg,
      String strDateFrom, String strDateTo, ReporteEstadoResultadosData[] estadoResultadosData) {
    DecimalFormat df = new DecimalFormat("0.00");
    df.setRoundingMode(RoundingMode.HALF_UP);

    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);

    String prev_categoria = "";
    int countRecord = 0;
    BigDecimal saldoAnioAnterior = BigDecimal.ZERO, saldoAnioActual = BigDecimal.ZERO;
    for (int i = 0; i < estadoResultadosData.length; i++) {
      countRecord++;

      if (i == 0) {
        prev_categoria = estadoResultadosData[i].categoria;

      } else if (prev_categoria.compareTo(estadoResultadosData[i].categoria) != 0) {
        ReporteEstadoResultadosData objRptEstadoResultadosData = new ReporteEstadoResultadosData();
        objRptEstadoResultadosData.order1 = estadoResultadosData[i - 1].order1;
        objRptEstadoResultadosData.order2 = estadoResultadosData[i - 1].order2;
        objRptEstadoResultadosData.categoria = estadoResultadosData[i - 1].categoria;
        objRptEstadoResultadosData.subCategoriaId = "--";
        objRptEstadoResultadosData.subCategoria = estadoResultadosData[i - 1].categoria;
        objRptEstadoResultadosData.grupoCategoria = estadoResultadosData[i - 1].grupoCategoria;
        objRptEstadoResultadosData.idorganizacion = estadoResultadosData[i - 1].idorganizacion;
        objRptEstadoResultadosData.idperiodo = estadoResultadosData[i - 1].idperiodo;
        objRptEstadoResultadosData.periodo = estadoResultadosData[i - 1].periodo;
        objRptEstadoResultadosData.fechainicial = estadoResultadosData[i - 1].fechainicial;
        objRptEstadoResultadosData.fechafinal = estadoResultadosData[i - 1].fechafinal;
        objRptEstadoResultadosData.saldoAnioAnterior = df.format(saldoAnioAnterior);
        objRptEstadoResultadosData.saldoAnioActual = df.format(saldoAnioActual);
        objRptEstadoResultadosData.sunatcodeCat = estadoResultadosData[i - 1].sunatcodeCat;
        objRptEstadoResultadosData.sunatcodeSubcat = estadoResultadosData[i - 1].sunatcodeCat;
        objRptEstadoResultadosData.rownum = Long.toString(countRecord);

        objRptEstadoResultadosData.paramorgid = strOrg;
        objRptEstadoResultadosData.paramdatefrom = strDateFrom;
        objRptEstadoResultadosData.paramdateto = strDateTo;

        vector.addElement(objRptEstadoResultadosData);

        prev_categoria = estadoResultadosData[i].categoria;
        countRecord++;
      }

      ReporteEstadoResultadosData objRptEstadoResultadosData = new ReporteEstadoResultadosData();
      objRptEstadoResultadosData.order1 = estadoResultadosData[i].order1;
      objRptEstadoResultadosData.order2 = estadoResultadosData[i].order2;
      objRptEstadoResultadosData.categoria = estadoResultadosData[i].categoria;
      objRptEstadoResultadosData.subCategoriaId = estadoResultadosData[i].subCategoriaId;
      objRptEstadoResultadosData.subCategoria = estadoResultadosData[i].subCategoria;
      objRptEstadoResultadosData.grupoCategoria = estadoResultadosData[i].grupoCategoria;
      objRptEstadoResultadosData.idorganizacion = estadoResultadosData[i].idorganizacion;
      objRptEstadoResultadosData.idperiodo = estadoResultadosData[i].idperiodo;
      objRptEstadoResultadosData.periodo = estadoResultadosData[i].periodo;
      objRptEstadoResultadosData.fechainicial = estadoResultadosData[i].fechainicial;
      objRptEstadoResultadosData.fechafinal = estadoResultadosData[i].fechafinal;
      objRptEstadoResultadosData.saldoAnioAnterior = estadoResultadosData[i].saldoAnioAnterior;
      objRptEstadoResultadosData.saldoAnioActual = estadoResultadosData[i].saldoAnioActual;
      objRptEstadoResultadosData.sunatcodeCat = estadoResultadosData[i].sunatcodeCat;
      objRptEstadoResultadosData.sunatcodeSubcat = estadoResultadosData[i].sunatcodeSubcat;
      objRptEstadoResultadosData.rownum = Long.toString(countRecord);

      objRptEstadoResultadosData.paramorgid = strOrg;
      objRptEstadoResultadosData.paramdatefrom = strDateFrom;
      objRptEstadoResultadosData.paramdateto = strDateTo;

      if (objRptEstadoResultadosData.sunatcodeSubcat != null
          && !objRptEstadoResultadosData.sunatcodeSubcat.equals(""))
        vector.addElement(objRptEstadoResultadosData);

      saldoAnioAnterior = saldoAnioAnterior.add(new BigDecimal(
          estadoResultadosData[i].saldoAnioAnterior));
      saldoAnioActual = saldoAnioActual
          .add(new BigDecimal(estadoResultadosData[i].saldoAnioActual));
    }

    ReporteEstadoResultadosData objRptEstadoResultadosData = new ReporteEstadoResultadosData();
    objRptEstadoResultadosData.order1 = estadoResultadosData[estadoResultadosData.length - 1].order1;
    objRptEstadoResultadosData.order2 = estadoResultadosData[estadoResultadosData.length - 1].order2;
    objRptEstadoResultadosData.categoria = estadoResultadosData[estadoResultadosData.length - 1].categoria;
    objRptEstadoResultadosData.subCategoriaId = "--";
    objRptEstadoResultadosData.subCategoria = estadoResultadosData[estadoResultadosData.length - 1].categoria;
    objRptEstadoResultadosData.grupoCategoria = estadoResultadosData[estadoResultadosData.length - 1].grupoCategoria;
    objRptEstadoResultadosData.idorganizacion = estadoResultadosData[estadoResultadosData.length - 1].idorganizacion;
    objRptEstadoResultadosData.idperiodo = estadoResultadosData[estadoResultadosData.length - 1].idperiodo;
    objRptEstadoResultadosData.periodo = estadoResultadosData[estadoResultadosData.length - 1].periodo;
    objRptEstadoResultadosData.fechainicial = estadoResultadosData[estadoResultadosData.length - 1].fechainicial;
    objRptEstadoResultadosData.fechafinal = estadoResultadosData[estadoResultadosData.length - 1].fechafinal;
    objRptEstadoResultadosData.saldoAnioAnterior = df.format(saldoAnioAnterior);
    objRptEstadoResultadosData.saldoAnioActual = df.format(saldoAnioActual);
    objRptEstadoResultadosData.sunatcodeCat = estadoResultadosData[estadoResultadosData.length - 1].sunatcodeCat;
    objRptEstadoResultadosData.sunatcodeSubcat = estadoResultadosData[estadoResultadosData.length - 1].sunatcodeCat;

    objRptEstadoResultadosData.rownum = Long.toString(countRecord);

    objRptEstadoResultadosData.paramorgid = strOrg;
    objRptEstadoResultadosData.paramdatefrom = strDateFrom;
    objRptEstadoResultadosData.paramdateto = strDateTo;

    vector.addElement(objRptEstadoResultadosData);

    ReporteEstadoResultadosData objectReporteEstadoResultadosData[] = new ReporteEstadoResultadosData[vector
        .size()];
    vector.copyInto(objectReporteEstadoResultadosData);
    return objectReporteEstadoResultadosData;
  }

  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String strOrg,
      String strTable, String strRecord, String strSoloUnMes) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReporteEstadoResultadosData[] data = null, tmpData = null;
    String strPosition = "0";
    String strConvRateErrorMsg = "";

    String discard[] = { "discard" };

    xmlDocument = xmlEngine.readXmlTemplate(
        "pe/com/unifiedgo/report/ad_reports/ReporteEstadoResultados", discard).createXmlDocument();

    if (vars.commandIn("FIND", "DIRECT")) {
      String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
      String strOrgFamily = getFamily(strTreeOrg, strOrg);

      // //////////////////////////////
      String[] fechaAnioAnterior = strDateFrom.split("-");
      String[] fechaAnioActual = strDateTo.split("-");

      Integer anioAnterior = new Integer(fechaAnioAnterior[2]);
      Integer anioActual = new Integer(fechaAnioActual[2]);

      String fechaFinAnioAnterior = strDateFrom;
      String fechaIniAnioActual = "01-01-" + anioActual;

      String fechaIniAnioAnterior = "01-01-" + (anioAnterior);
      String fechaFinAnioActual = strDateTo;
      // /////////////////////////////

      OBError myMessage = null;
      myMessage = new OBError();
      try {

        System.out.println("strOrgFamily: " + strOrgFamily);
        System.out.println("fechaIniAnioAnterior: " + fechaIniAnioAnterior);
        System.out.println("fechaFinAnioAnterior: " + fechaFinAnioAnterior);
        System.out.println("fechaIniAnioActual: " + fechaIniAnioActual);
        System.out.println("fechaFinAnioActual: " + fechaFinAnioActual);

        tmpData = ReporteEstadoResultadosData.select(this, strOrgFamily, fechaIniAnioAnterior,
            fechaFinAnioAnterior, strOrgFamily, fechaIniAnioAnterior, fechaFinAnioAnterior,
            strOrgFamily, fechaIniAnioAnterior, fechaFinAnioAnterior, strOrgFamily,
            fechaIniAnioActual, fechaFinAnioActual, strOrgFamily, fechaIniAnioActual,
            fechaFinAnioActual, strOrgFamily, fechaIniAnioActual, fechaFinAnioActual,
            Utility.getContext(this, vars, "#User_Client", "ReporteEEFFGGPPNaturaleza"),
            strOrgFamily);
        data = tmpData;
        if (data != null && data.length != 0) {
          data = getFormattedData(strOrg, strDateFrom, strDateTo, tmpData);
        }

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
          data = ReporteEstadoResultadosData.set("0");
          data[0].rownum = "0";

        } else {
          xmlDocument.setData("structure1", data);
        }
      }
    }

    else {
      if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
        discard[0] = "selEliminar";
        data = ReporteEstadoResultadosData.set("0");
        data[0].rownum = "0";
      }
    }

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {

      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReporteEstadoResultados", false, "",
          "", "imprimir();return false;", false, "ad_reports", strReplaceWith, false, true);
      toolbar.setEmail(false);
      toolbar.prepareSimpleToolBarTemplate();
      toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");
      xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "pe.com.unifiedgo.report.ad_reports.ReporteEstadoResultados");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
            "ReporteEstadoResultados.html", classInfo.id, classInfo.type, strReplaceWith,
            tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
            "ReporteEstadoResultados.html", strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      {
        OBError myMessage = vars.getMessage("ReporteEstadoResultados");
        vars.removeMessage("ReporteEstadoResultados");
        if (myMessage != null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }

      xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
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
      xmlDocument.setParameter("paramSoloUnMes", strSoloUnMes);
      xmlDocument.setParameter("paramAniosArray", Utility.arrayInfinitasEntradas(
          "idorganizacion;periodo", "arrAnios", ReporteEstadoResultadosData.select_anios(this)));
      vars.setSessionValue("ReporteEstadoResultados|Record", strRecord);
      vars.setSessionValue("ReporteEstadoResultados|Table", strTable);

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
            "0C754881EAD94243A161111916E9B9C6", Utility.getContext(this, vars,
                "#AccessibleOrgTree", "ReporteEstadoResultados"), Utility.getContext(this, vars,
                "#User_Client", "ReporteEstadoResultados"), '*');
        comboTableData.fillParameters(null, "ReporteEstadoResultados", "");
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

      // Print document in the output
      out.println(xmlDocument.print());
      out.close();
    }
  }

  private String getFamily(String strTree, String strChild) throws IOException, ServletException {
    return Tree.getMembers(this, strTree, (strChild == null || strChild.equals("")) ? "0"
        : strChild)
        + ",'0'";
    /*
     * ReportGeneralLedgerData [] data = ReportGeneralLedgerData.selectChildren(this, strTree,
     * strChild); String strFamily = ""; if(data!=null && data.length>0) { for (int i =
     * 0;i<data.length;i++){ if (i>0) strFamily = strFamily + ","; strFamily = strFamily +
     * data[i].id; } return strFamily += ""; }else return "'1'";
     */
  }

  private void printPagePDF(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String strOrg,
      String strTable, String strRecord, String strSoloUnAnio) throws IOException, ServletException {

    ReporteEstadoResultadosData[] data = null;

    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    String strOrgFamily = getFamily(strTreeOrg, strOrg);

    String strCliente = Utility.getContext(this, vars, "#User_Client", "ReporteEstadoResultados");

    // //////////////////////////////
    String[] fechaAnioAnterior = strDateFrom.split("-");
    String[] fechaAnioActual = strDateTo.split("-");

    Integer anioAnterior = new Integer(fechaAnioAnterior[2]);
    Integer anioActual = new Integer(fechaAnioActual[2]);

    String anio1 = anioAnterior.toString();
    String anio2 = anioActual.toString();

    String fechaFinAnioAnterior = strDateFrom;
    String fechaIniAnioActual = "01-01-" + anioActual;

    String fechaIniAnioAnterior = "01-01-" + (anioAnterior);
    String fechaFinAnioActual = strDateTo;

    // /////////////////////////////

    // String[] fechaAnioActual = strDateTo.split("-");
    //
    // String fechaIniAnioAnterior = "01-01-" + anio1;
    // String fechaFinAnioAnterior = strDateTo;
    //
    // String fechaIniAnioActual = "01-01-" + anio2;
    // String fechaFinAnioActual = strDateTo;

    String dia_comun = fechaAnioActual[0];
    String mes_comun = nombreMes(fechaAnioActual[1]);

    data = ReporteEstadoResultadosData.select(this, strOrgFamily, fechaIniAnioAnterior,
        fechaFinAnioAnterior, strOrgFamily, fechaIniAnioAnterior, fechaFinAnioAnterior,
        strOrgFamily, fechaIniAnioAnterior, fechaFinAnioAnterior, strOrgFamily, fechaIniAnioActual,
        fechaFinAnioActual, strOrgFamily, fechaIniAnioActual, fechaFinAnioActual, strOrgFamily,
        fechaIniAnioActual, fechaFinAnioActual,
        Utility.getContext(this, vars, "#User_Client", "ReporteEEFFGGPPNaturaleza"), strOrgFamily);

    String strOutput;
    String strReportName;
    if (vars.commandIn("PDF")) {
      strOutput = "pdf";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteEstadoResultados.jrxml";
    } else {
      strOutput = "xls";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteEstadoResultadosExcel.jrxml";
    }

    String probando = SREDateTimeData.FirstDayOfMonth(this);

    System.out.println(SREDateTimeData.FirstDayOfMonth(this));

    String strLanguage = vars.getLanguage();
    String strBaseDesign = getBaseDesignPath(strLanguage);

    // JasperReport subReportActivo;
    // JasperReport subReportPasivoPatrimonio;
    // try {
    // JasperDesign jasperDesignLines =
    // JRXmlLoader.load(strBaseDesign+"/pe/com/unifiedgo/report/ad_reports/ReporteEstadoResultados_activo.jrxml");
    // subReportActivo = JasperCompileManager.compileReport(jasperDesignLines);
    //
    // jasperDesignLines =
    // JRXmlLoader.load(strBaseDesign+"/pe/com/unifiedgo/report/ad_reports/ReporteEstadoResultados_pasivo_patrimonio.jrxml");
    //
    // subReportPasivoPatrimonio = JasperCompileManager.compileReport(jasperDesignLines);
    //
    //
    // } catch (JRException e) {
    // throw new ServletException(e.getMessage());
    // }

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("Ruc", ReporteEstadoResultadosData.selectRucOrg(this, strOrg));
    parameters.put("organizacion", ReporteEstadoResultadosData.selectSocialName(this, strOrg));

    parameters.put("soloUnAnio", strSoloUnAnio);

    parameters.put("dateFrom", strDateFrom);
    parameters.put("dateTo", strDateTo);
    // parameters.put("fechaFinAnioAnterior", fechaFinAnioAnterior);
    // parameters.put("fechaIniAnioActual", fechaIniAnioActual);

    parameters.put("fechaFinAnioAnterior", fechaFinAnioAnterior);
    parameters.put("fechaIniAnioActual", fechaIniAnioActual);

    parameters.put("fechaIniAnioAnterior", fechaIniAnioAnterior);
    parameters.put("fechaFinAnioActual", fechaFinAnioActual);

    parameters.put("anio1", anio1);
    parameters.put("anio2", anio2);

    parameters.put("DIA_COMUN", dia_comun);
    parameters.put("MES_COMUN", mes_comun);

    parameters.put("CLIENTE", strCliente);
    parameters.put("ORGANIZACION", strOrgFamily);

    renderJR(vars, response, strReportName, "Reporte Estado de Resultados", strOutput, parameters,
        data, null);
  }

  public static StructPle getEstadoResultadosToPLE(ConnectionProvider connp,
      VariablesSecureApp vars, String strClientId, Date dateTo, Organization org,
      String strOrgFamily) throws Exception {

    StringBuffer sb = new StringBuffer();
    StructPle sunatPle = new StructPle();
    sunatPle.numEntries = 0;

    SimpleDateFormat dt = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");

    // get this year uit
    Calendar caldttTo = new GregorianCalendar();
    caldttTo.setTime(dateTo);
    int year = caldttTo.get(Calendar.YEAR);

    // get first day of year
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.DAY_OF_YEAR, 1);
    Date dateFrom = cal.getTime();

    String period = dt.format(dateTo); // Periodo

    String dateFromValidation = df.format(dateFrom);
    String dateToValidation = df.format(dateTo);

    String catolgCode = "01"; // table13
    String operationStatus = "1";

    String fechaIniAnioAnterior = dateFromValidation;
    String fechaFinAnioAnterior = dateToValidation;
    String fechaIniAnioActual = dateFromValidation;
    String fechaFinAnioActual = dateToValidation;

    String strClient = "'" + strClientId + "'";

    ReporteEstadoResultadosData[] data = null, tmpData = null;
    // --GET DATA--
    tmpData = ReporteEstadoResultadosData.select(connp, strOrgFamily, fechaIniAnioAnterior,
        fechaFinAnioAnterior, strOrgFamily, fechaIniAnioAnterior, fechaFinAnioAnterior,
        strOrgFamily, fechaIniAnioAnterior, fechaFinAnioAnterior, strOrgFamily, fechaIniAnioActual,
        fechaFinAnioActual, strOrgFamily, fechaIniAnioActual, fechaFinAnioActual, strOrgFamily,
        fechaIniAnioActual, fechaFinAnioActual, strClient, strOrgFamily);

    data = tmpData;
    if (data != null && data.length != 0) {
      data = getFormattedDataForPLE(org.getId(), fechaIniAnioActual, fechaFinAnioActual, tmpData);
    }

    for (int i = 0; i < data.length; i++) {

      ReporteEstadoResultadosData info = data[i];

      String strSunatCode = info.sunatcodeSubcat;
      String strtotal = info.saldoAnioActual;

      String linea = period + "|" + catolgCode + "|" + strSunatCode + "|" + strtotal + "|"
          + operationStatus + "|";

      if (!sb.toString().equals(""))
        sb.append("\n");
      sb.append(linea);
      sunatPle.numEntries++;

    }

    sunatPle.data = sb.toString();
    String rucAdq = SunatUtil.LPad(org.getOrganizationInformationList().get(0).getTaxID(), 11, '0');

    String filename = "LE" + rucAdq + dt.format(dateTo) + "032000011111.TXT"; // LERRRRRRRRRRRAAAAMMDD030700CCOIM1.TXT

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

  private String nombreMes(String mes) {

    Integer mesInt = new Integer(mes);

    String[] meses = { "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto",
        "Septiembre", "Octubre", "Noviembre", "Diciembre" };
    String retornaMes = "";

    retornaMes = meses[mesInt - 1];
    return retornaMes;

  }

  @Override
  public String getServletInfo() {
    return "Servlet ReporteEstadoResultados. This Servlet was made by Pablo Sarobe modified by everybody";
  } // end of getServletInfo() method
}
