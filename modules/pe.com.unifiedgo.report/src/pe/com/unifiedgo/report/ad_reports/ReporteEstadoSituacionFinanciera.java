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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

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

public class ReporteEstadoSituacionFinanciera extends HttpSecureAppServlet {
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
          "ReporteEstadoSituacionFinanciera|DateFrom", fechaFinAnioAnterior);
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReporteEstadoResultados|DateTo",
          fechaFinAnioActual);

      String strOrg = vars.getGlobalVariable("inpOrg", "ReporteEstadoSituacionFinanciera|Org", "0");
      String strRecord = vars.getGlobalVariable("inpRecord",
          "ReporteEstadoSituacionFinanciera|Record", "");
      String strTable = vars.getGlobalVariable("inpTable",
          "ReporteEstadoSituacionFinanciera|Table", "");
      String strSoloUnMes = vars.getStringParameter("inpSoloUnMes");

      printPageDataSheet(request, response, vars, strDateFrom, strDateTo, strOrg, strTable,
          strRecord, strSoloUnMes);

    } else if (vars.commandIn("DIRECT")) {
      String strTable = vars
          .getGlobalVariable("inpTable", "ReporteEstadoSituacionFinanciera|Table");
      String strRecord = vars.getGlobalVariable("inpRecord",
          "ReporteEstadoSituacionFinanciera|Record");

      setHistoryCommand(request, "DIRECT");
      vars.setSessionValue("ReporteEstadoSituacionFinanciera.initRecordNumber", "0");

      printPageDataSheet(request, response, vars, "", "", "", strTable, strRecord, "");

    } else if (vars.commandIn("FIND")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReporteEstadoSituacionFinanciera|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReporteEstadoSituacionFinanciera|DateTo");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReporteEstadoSituacionFinanciera|Org", "0");
      String strSoloUnAnio = vars.getStringParameter("inpSoloUnMes");

      String strTable = vars.getStringParameter("inpTable");
      String strRecord = vars.getStringParameter("inpRecord");

      vars.setSessionValue("ReporteEstadoSituacionFinanciera.initRecordNumber", "0");
      setHistoryCommand(request, "DEFAULT");
      printPageDataSheet(request, response, vars, strDateFrom, strDateTo, strOrg, strTable,
          strRecord, strSoloUnAnio);

    } else if (vars.commandIn("PDF", "XLS")) {
      if (log4j.isDebugEnabled())
        log4j.debug("PDF");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReporteEstadoSituacionFinanciera|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReporteEstadoSituacionFinanciera|DateTo");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReporteEstadoSituacionFinanciera|Org", "0");
      String strSoloUnAnio = vars.getStringParameter("inpSoloUnMes");

      String strTable = vars.getStringParameter("inpTable");
      String strRecord = vars.getStringParameter("inpRecord");
      setHistoryCommand(request, "DEFAULT");
      printPagePDF(request, response, vars, strDateFrom, strDateTo, strOrg, strTable, strRecord,
          strSoloUnAnio);

    } else
      pageError(response);
  }

  private String cutString(String s) {
    if (s.length() > 140) {
      return s.substring(0, 140);
    } else {
      return s;
    }
  }

  private ReporteEstadoSituacionFinancieraData[] getFormattedData(String strOrg,
      String strDateFrom, String strDateTo, ReporteEstadoSituacionFinancieraData[] data) {
    int countRecord = 0;
    for (int i = 0; i < data.length; i++) {
      if ("--".compareTo(data[i].subCategoriaId) == 0) {
        data[i].subCategoria = "<b>" + cutString(data[i].subCategoria) + "</b>";
      } else {
        data[i].subCategoria = cutString(data[i].subCategoria);
      }
      data[i].saldoAnioAnterior = data[i].saldoAnioAnterior;
      data[i].saldoAnioActual = data[i].saldoAnioActual;

      data[i].paramorgid = strOrg;
      data[i].paramdatefrom = strDateFrom;
      data[i].paramdateto = strDateTo;

      data[i].rownum = Long.toString(countRecord);
      countRecord++;
    }
    return data;
  }

  // For HTML Output
  private ReporteEstadoSituacionFinancieraData[] getTotalsFromEstadoSituacionFinancieraData(
      ReporteEstadoSituacionFinancieraData[] data) {
    ReporteEstadoSituacionFinancieraData[] dataFinal = null;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);

    DecimalFormat df = new DecimalFormat("###0.00");
    df.setRoundingMode(RoundingMode.HALF_UP);

    BigDecimal totGrupoAnioAnterior = BigDecimal.ZERO, totGrupoAnioActual = BigDecimal.ZERO;
    BigDecimal totTipoAnioAnterior = BigDecimal.ZERO, totTipoAnioActual = BigDecimal.ZERO;
    String grupo = (data.length > 0) ? data[0].tipoGrupo : null;
    String categoria = (data.length > 0) ? data[0].categoria : null;

    // CREANDO FILAS EXTRAS CON CATEGORIAS Y TOTALES DE GRUPO y TOTALES POR TIPO
    // FILA PARA CATEGORIA
    if (data.length > 0) {
      ReporteEstadoSituacionFinancieraData row = copy(data[0]);
      row.subCategoriaId = "--";
      row.subCategoria = data[0].categoria;
      row.saldoAnioAnterior = row.saldoAnioActual = "0.00";
      vector.add(row);
    }
    for (int i = 0; i < data.length; i++) {
      // FILA PARA TIPOGRUPO
      if (data[i].tipoGrupo.compareTo(grupo) != 0) {
        ReporteEstadoSituacionFinancieraData row = copy(data[i - 1]);
        row.subCategoriaId = "--";
        row.subCategoria = "TOTAL " + row.tipoGrupo;
        row.saldoAnioAnterior = df.format(totGrupoAnioAnterior);
        row.saldoAnioActual = df.format(totGrupoAnioActual);
        vector.add(row);

        grupo = data[i].tipoGrupo;
        totGrupoAnioAnterior = totGrupoAnioActual = BigDecimal.ZERO;
      }
      // FILA PARA CATEGORIA
      if (data[i].categoria.compareTo(categoria) != 0) {
        ReporteEstadoSituacionFinancieraData row = copy(data[i]);
        row.subCategoriaId = "--";
        row.subCategoria = row.categoria;
        row.saldoAnioAnterior = row.saldoAnioActual = "0.00";
        vector.add(row);

        categoria = data[i].categoria;
      }
      // FILA PARA DATA
      vector.add(data[i]);
      // ACUMULADOS
      totGrupoAnioAnterior = totGrupoAnioAnterior.add(new BigDecimal(data[i].saldoAnioAnterior));
      totGrupoAnioActual = totGrupoAnioActual.add(new BigDecimal(data[i].saldoAnioActual));

      totTipoAnioAnterior = totTipoAnioAnterior.add(new BigDecimal(data[i].saldoAnioAnterior));
      totTipoAnioActual = totTipoAnioActual.add(new BigDecimal(data[i].saldoAnioActual));
    }
    if (data.length > 0) {
      // FILA PARA TIPOGRUPO
      ReporteEstadoSituacionFinancieraData row = copy(data[data.length - 1]);
      row.subCategoriaId = row.categoria = "--";
      row.subCategoria = "TOTAL " + row.tipoGrupo;
      row.saldoAnioAnterior = df.format(totGrupoAnioAnterior);
      row.saldoAnioActual = df.format(totGrupoAnioActual);
      vector.add(row);
      // FILA PARA TIPO
      ReporteEstadoSituacionFinancieraData row2 = copy(data[data.length - 1]);
      row2.subCategoriaId = row2.categoria = row2.grupoCategoria = row2.tipoGrupo = "--";
      row2.subCategoria = "TOTAL " + row2.tipo.toUpperCase();
      row2.saldoAnioAnterior = df.format(totTipoAnioAnterior);
      row2.saldoAnioActual = df.format(totTipoAnioActual);
      vector.add(row2);
    }
    dataFinal = new ReporteEstadoSituacionFinancieraData[vector.size()];
    vector.copyInto(dataFinal);
    return dataFinal;
  }

  private static Object[] getEstadoSituacionFinancieraData(ConnectionProvider conn,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String adUserClient,
      String strOrg) throws IOException, ServletException {
    ReporteEstadoSituacionFinancieraData[] dataCategorias = null;
    ReporteEstadoSituacionFinancieraData[] dataFactsAnterior = null;
    ReporteEstadoSituacionFinancieraData[] dataFactsActual = null;

    ReporteEstadoSituacionFinancieraData[] dataActivos = null;
    ReporteEstadoSituacionFinancieraData[] dataPasivos = null;

    ArrayList<ReporteEstadoSituacionFinancieraData> dataList = new ArrayList<ReporteEstadoSituacionFinancieraData>();
    HashMap<String, ArrayList<ReporteEstadoSituacionFinancieraData>> grupoCuentasActuales = new HashMap<String, ArrayList<ReporteEstadoSituacionFinancieraData>>();
    HashMap<String, ArrayList<ReporteEstadoSituacionFinancieraData>> grupoCuentaAnterior = new HashMap<String, ArrayList<ReporteEstadoSituacionFinancieraData>>();

    String strTreeOrg = TreeData.getTreeOrg(conn, vars.getClient());
    String strOrgFamily = getFamily(conn, strTreeOrg, strOrg);
    String strCliente = Utility.getContext(conn, vars, "#User_Client",
        "ReporteEstadoSituacionFinanciera");

    String[] fechaAnioAnterior = strDateFrom.split("-");
    String[] fechaAnioActual = strDateTo.split("-");

    Integer anioAnterior = new Integer(fechaAnioAnterior[2]);
    Integer anioActual = new Integer(fechaAnioActual[2]);

    String fechaFinAnioAnterior = strDateFrom;
    String fechaIniAnioActual = "01-01-" + anioActual;

    String fechaIniAnioAnterior = "01-01-" + (anioAnterior);
    String fechaFinAnioActual = strDateTo;

    dataCategorias = ReporteEstadoSituacionFinancieraData.select_categorias(conn, adUserClient,
        strOrgFamily, adUserClient, strOrgFamily);

    dataFactsAnterior = ReporteEstadoSituacionFinancieraData.select_facts(conn, strOrgFamily,
        adUserClient, fechaIniAnioAnterior, fechaFinAnioAnterior);

    dataFactsActual = ReporteEstadoSituacionFinancieraData.select_facts(conn, strOrgFamily,
        adUserClient, fechaIniAnioActual, fechaFinAnioActual);

    String cuenta = "";
    for (int i = 0; i < dataCategorias.length; i++) {
      ReporteEstadoSituacionFinancieraData now = dataCategorias[i];
      ArrayList<ReporteEstadoSituacionFinancieraData> lista = new ArrayList<ReporteEstadoSituacionFinancieraData>();

      cuenta = now.acctvalue.replace("-", "").replace("%", "");
      String key = now.order1 + "//" + now.order2 + "//" + cuenta;

      for (int j = 0; j < dataFactsAnterior.length; j++) {
        if (dataFactsAnterior[j].acctvalue.startsWith(cuenta) && !cuenta.equalsIgnoreCase("")) {
          if (grupoCuentaAnterior.containsKey(key))
            lista = grupoCuentaAnterior.get(key);
          lista.add(dataFactsAnterior[j]);
          grupoCuentaAnterior.put(key, lista);
        }
      }

      lista = new ArrayList<ReporteEstadoSituacionFinancieraData>();
      for (int j = 0; j < dataFactsActual.length; j++) {
        if (dataFactsActual[j].acctvalue.startsWith(cuenta) && !cuenta.equalsIgnoreCase("")) {
          if (grupoCuentasActuales.containsKey(key))
            lista = grupoCuentasActuales.get(key);
          lista.add(dataFactsActual[j]);
          grupoCuentasActuales.put(key, lista);
        }
      }
    }

    for (int i = 0; i < dataCategorias.length; i++) {

      ReporteEstadoSituacionFinancieraData now = dataCategorias[i];

      cuenta = now.acctvalue.replace("-", "").replace("%", "");

      String key = now.order1 + "//" + now.order2 + "//" + cuenta;

      now.saldoAnioAnterior = getMontoxCuenta2(grupoCuentaAnterior.get(key), now.acctvalue,
          now.consider, now.considerwhen, now.greaterThanAYear).toString();
      now.saldoAnioActual = getMontoxCuenta2(grupoCuentasActuales.get(key), now.acctvalue,
          now.consider, now.considerwhen, now.greaterThanAYear).toString();
    }

    BigDecimal sumaSaldoAnterior = BigDecimal.ZERO, sumaSaldoActual = BigDecimal.ZERO;

    for (int i = 0; i < dataCategorias.length; i++) {

      ReporteEstadoSituacionFinancieraData now = dataCategorias[i];
      ReporteEstadoSituacionFinancieraData next = (i + 1 == dataCategorias.length) ? new ReporteEstadoSituacionFinancieraData()
          : dataCategorias[i + 1];

      sumaSaldoAnterior = new BigDecimal(now.saldoAnioAnterior).multiply(
          new BigDecimal(now.considerar)).add(sumaSaldoAnterior);
      sumaSaldoActual = new BigDecimal(now.saldoAnioActual)
          .multiply(new BigDecimal(now.considerar)).add(sumaSaldoActual);

      if (!now.order1.equals(next.order1) || !now.order2.equals(next.order2)
          || !now.categoria.equals(next.categoria) || !now.subCategoria.equals(next.subCategoria)
          || !now.grupoCategoria.equals(next.grupoCategoria)) {

        now.saldoAnioAnterior = sumaSaldoAnterior.multiply(new BigDecimal(now.signo)).toString();
        now.saldoAnioActual = sumaSaldoActual.multiply(new BigDecimal(now.signo)).toString();

        dataList.add(now);

        sumaSaldoAnterior = new BigDecimal(0);
        sumaSaldoActual = new BigDecimal(0);
      }
    }

    // SE CREA LAS LISTAS DE BEANS PARA SER PASADADOS COMO DATA_SOURCE A LOS SUBREPORTES
    ArrayList<ReporteEstadoSituacionFinancieraBean> dataSubReporteActivos = new ArrayList<ReporteEstadoSituacionFinancieraBean>();
    ArrayList<ReporteEstadoSituacionFinancieraBean> dataSubReportePasivosPatrimonio = new ArrayList<ReporteEstadoSituacionFinancieraBean>();

    ReporteEstadoSituacionFinancieraBean bean = new ReporteEstadoSituacionFinancieraBean();

    for (int i = 0; i < dataList.size(); i++) {
      bean = new ReporteEstadoSituacionFinancieraBean();
      bean.setTipo(dataList.get(i).tipo);
      bean.setOrder1(dataList.get(i).order1);
      bean.setOrder2(dataList.get(i).order2);
      bean.setCategoria(dataList.get(i).categoria);
      bean.setSubCategoriaId(dataList.get(i).subCategoriaId);
      bean.setSubCategoria(dataList.get(i).subCategoria);
      bean.setTipoGrupo(dataList.get(i).tipoGrupo);
      bean.setSaldoAnioAnterior(new BigDecimal(dataList.get(i).saldoAnioAnterior));
      bean.setSaldoAnioActual(new BigDecimal(dataList.get(i).saldoAnioActual));
      bean.setGrupoCategoria(dataList.get(i).grupoCategoria);
      bean.setSubCategoriaSunatCode(dataList.get(i).subCategoriaSunatCode);
      bean.setCategoriaSunatCode(dataList.get(i).categoriaSunatCode);
      if (bean.getTipo().equals("activos")) {
        dataSubReporteActivos.add(bean);
      } else {
        dataSubReportePasivosPatrimonio.add(bean);
      }
    }

    // BEANS TO 'ReporteEstadoSituacionFinancieraData' ARRAY
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    for (int i = 0; i < dataSubReporteActivos.size(); i++) {
      ReporteEstadoSituacionFinancieraData objActivo = new ReporteEstadoSituacionFinancieraData();
      objActivo.tipo = dataSubReporteActivos.get(i).getTipo();
      objActivo.order1 = dataSubReporteActivos.get(i).getOrder1();
      objActivo.order2 = dataSubReporteActivos.get(i).getOrder2();
      objActivo.categoria = dataSubReporteActivos.get(i).getCategoria();
      objActivo.subCategoriaId = dataSubReporteActivos.get(i).getSubCategoriaId();
      objActivo.subCategoria = dataSubReporteActivos.get(i).getSubCategoria();
      objActivo.tipoGrupo = dataSubReporteActivos.get(i).getTipoGrupo();
      objActivo.saldoAnioAnterior = dataSubReporteActivos.get(i).getSaldoAnioAnterior()
          .toPlainString();
      objActivo.saldoAnioActual = dataSubReporteActivos.get(i).getSaldoAnioActual().toPlainString();
      objActivo.grupoCategoria = dataSubReporteActivos.get(i).getGrupoCategoria();
      objActivo.subCategoriaSunatCode = dataSubReporteActivos.get(i).getSubCategoriaSunatCode();
      objActivo.categoriaSunatCode = dataSubReporteActivos.get(i).getCategoriaSunatCode();
      vector.addElement(objActivo);
    }
    dataActivos = new ReporteEstadoSituacionFinancieraData[vector.size()];
    vector.copyInto(dataActivos);
    vector.clear();

    vector = new Vector<java.lang.Object>(0);
    for (int i = 0; i < dataSubReportePasivosPatrimonio.size(); i++) {
      ReporteEstadoSituacionFinancieraData objPasivo = new ReporteEstadoSituacionFinancieraData();
      objPasivo.tipo = dataSubReportePasivosPatrimonio.get(i).getTipo();
      objPasivo.order1 = dataSubReportePasivosPatrimonio.get(i).getOrder1();
      objPasivo.order2 = dataSubReportePasivosPatrimonio.get(i).getOrder2();
      objPasivo.categoria = dataSubReportePasivosPatrimonio.get(i).getCategoria();
      objPasivo.subCategoriaId = dataSubReportePasivosPatrimonio.get(i).getSubCategoriaId();
      objPasivo.subCategoria = dataSubReportePasivosPatrimonio.get(i).getSubCategoria();
      objPasivo.tipoGrupo = dataSubReportePasivosPatrimonio.get(i).getTipoGrupo();
      objPasivo.saldoAnioAnterior = dataSubReportePasivosPatrimonio.get(i).getSaldoAnioAnterior()
          .toPlainString();
      objPasivo.saldoAnioActual = dataSubReportePasivosPatrimonio.get(i).getSaldoAnioActual()
          .toPlainString();
      objPasivo.grupoCategoria = dataSubReportePasivosPatrimonio.get(i).getGrupoCategoria();
      objPasivo.subCategoriaSunatCode = dataSubReportePasivosPatrimonio.get(i)
          .getSubCategoriaSunatCode();
      objPasivo.categoriaSunatCode = dataSubReportePasivosPatrimonio.get(i).getCategoriaSunatCode();
      vector.addElement(objPasivo);
    }
    dataPasivos = new ReporteEstadoSituacionFinancieraData[vector.size()];
    vector.copyInto(dataPasivos);

    return new Object[] { dataActivos, dataPasivos };
  }

  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String strOrg,
      String strTable, String strRecord, String strSoloUnAnio) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;

    ReporteEstadoSituacionFinancieraData[] dataActivos = null;
    ReporteEstadoSituacionFinancieraData[] dataPasivos = null;
    ReporteEstadoSituacionFinancieraData[] dataAux = null;
    String strPosition = "0";

    String discard[] = { "discard" };

    xmlDocument = xmlEngine.readXmlTemplate(
        "pe/com/unifiedgo/report/ad_reports/ReporteEstadoSituacionFinanciera", discard)
        .createXmlDocument();

    if (vars.commandIn("FIND", "DIRECT")) {
      Object[] data = getEstadoSituacionFinancieraData(this, vars, strDateFrom, strDateTo,
          Utility.getContext(this, vars, "#User_Client", "ReporteEstadoSituacionFinanciera"),
          strOrg);

      // ACTIVOS
      dataAux = (data != null && data[0] != null) ? (ReporteEstadoSituacionFinancieraData[]) data[0]
          : null;
      if (dataAux != null && dataAux.length != 0) {
        dataActivos = getTotalsFromEstadoSituacionFinancieraData(dataAux);
        dataActivos = getFormattedData(strOrg, strDateFrom, strDateTo, dataActivos);

      } else {
        dataActivos = ReporteEstadoSituacionFinancieraData.set();
      }

      // PASIVOS Y PATRMONIO
      dataAux = (data != null && data[1] != null) ? (ReporteEstadoSituacionFinancieraData[]) data[1]
          : null;
      if (dataAux != null && dataAux.length != 0) {
        dataPasivos = getTotalsFromEstadoSituacionFinancieraData(dataAux);
        dataPasivos = getFormattedData(strOrg, strDateFrom, strDateTo, dataPasivos);

      } else {
        dataPasivos = ReporteEstadoSituacionFinancieraData.set();
      }

    } else {
      dataActivos = ReporteEstadoSituacionFinancieraData.set();
      dataPasivos = ReporteEstadoSituacionFinancieraData.set();
    }

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReporteEstadoSituacionFinanciera",
        false, "", "", "imprimir();return false;", false, "ad_reports", strReplaceWith, false, true);
    toolbar.setEmail(false);
    toolbar.prepareSimpleToolBarTemplate();
    toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "pe.com.unifiedgo.report.ad_reports.ReporteEstadoSituacionFinanciera");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ReporteEstadoSituacionFinanciera.html", classInfo.id, classInfo.type, strReplaceWith,
          tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
          "ReporteEstadoSituacionFinanciera.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReporteEstadoSituacionFinanciera");
      vars.removeMessage("ReporteEstadoSituacionFinanciera");
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
    xmlDocument.setParameter("paramSoloUnMes", strSoloUnAnio);
    vars.setSessionValue("ReporteEstadoResultados|Record", strRecord);
    vars.setSessionValue("ReporteEstadoResultados|Table", strTable);

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
          "0C754881EAD94243A161111916E9B9C6", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReporteEstadoSituacionFinanciera"), Utility.getContext(this, vars, "#User_Client",
              "ReporteEstadoSituacionFinanciera"), '*');
      comboTableData.fillParameters(null, "ReporteEstadoSituacionFinanciera", "");
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
        ReporteEstadoSituacionFinancieraData.select_periodos(this)));
    vars.setSessionValue("ReporteEstadoSituacionFinanciera|Record", strRecord);
    vars.setSessionValue("ReporteEstadoSituacionFinanciera|Table", strTable);

    xmlDocument.setData("structure1", dataActivos);
    xmlDocument.setData("structure2", dataPasivos);

    // Print document in the output
    out.println(xmlDocument.print());
    out.close();
  }

  private static String getFamily(ConnectionProvider conn, String strTree, String strChild)
      throws IOException, ServletException {
    return Tree.getMembers(conn, strTree, (strChild == null || strChild.equals("")) ? "0"
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

    ReporteEstadoSituacionFinancieraData[] dataCategorias = null;
    ReporteEstadoSituacionFinancieraData[] dataFactsAnterior = null;
    ReporteEstadoSituacionFinancieraData[] dataFactsActual = null;

    ArrayList<ReporteEstadoSituacionFinancieraData> dataList = new ArrayList<ReporteEstadoSituacionFinancieraData>();
    HashMap<String, ArrayList<ReporteEstadoSituacionFinancieraData>> grupoCuentasActuales = new HashMap<String, ArrayList<ReporteEstadoSituacionFinancieraData>>();
    HashMap<String, ArrayList<ReporteEstadoSituacionFinancieraData>> grupoCuentaAnterior = new HashMap<String, ArrayList<ReporteEstadoSituacionFinancieraData>>();

    ReporteEstadoSituacionFinancieraData[] dataFinal = null;

    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    String strOrgFamily = getFamily(this, strTreeOrg, strOrg);
    String strCliente = Utility.getContext(this, vars, "#User_Client",
        "ReporteEstadoSituacionFinanciera");

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

    String dia_comun = fechaAnioActual[0];
    String mes_comun = nombreMes(fechaAnioActual[1]);

    dataCategorias = ReporteEstadoSituacionFinancieraData.select_categorias(this, strCliente,
        strOrgFamily, strCliente, strOrgFamily);

    dataFactsAnterior = ReporteEstadoSituacionFinancieraData.select_facts(this, strOrgFamily,
        strCliente, fechaIniAnioAnterior, fechaFinAnioAnterior);

    dataFactsActual = ReporteEstadoSituacionFinancieraData.select_facts(this, strOrgFamily,
        strCliente, fechaIniAnioActual, fechaFinAnioActual);

    String cuenta = "";

    for (int i = 0; i < dataCategorias.length; i++) {
      ReporteEstadoSituacionFinancieraData now = dataCategorias[i];
      ArrayList<ReporteEstadoSituacionFinancieraData> lista = new ArrayList<ReporteEstadoSituacionFinancieraData>();

      cuenta = now.acctvalue.replace("-", "").replace("%", "");
      String key = now.order1 + "//" + now.order2 + "//" + cuenta;

      for (int j = 0; j < dataFactsAnterior.length; j++) {
        if (dataFactsAnterior[j].acctvalue.startsWith(cuenta) && !cuenta.equalsIgnoreCase("")) {
          if (grupoCuentaAnterior.containsKey(key))
            lista = grupoCuentaAnterior.get(key);
          lista.add(dataFactsAnterior[j]);
          grupoCuentaAnterior.put(key, lista);
        }
      }

      lista = new ArrayList<ReporteEstadoSituacionFinancieraData>();
      for (int j = 0; j < dataFactsActual.length; j++) {
        if (dataFactsActual[j].acctvalue.startsWith(cuenta) && !cuenta.equalsIgnoreCase("")) {
          if (grupoCuentasActuales.containsKey(key))
            lista = grupoCuentasActuales.get(key);
          lista.add(dataFactsActual[j]);
          grupoCuentasActuales.put(key, lista);
        }
      }
    }

    for (int i = 0; i < dataCategorias.length; i++) {

      ReporteEstadoSituacionFinancieraData now = dataCategorias[i];

      cuenta = now.acctvalue.replace("-", "").replace("%", "");

      String key = now.order1 + "//" + now.order2 + "//" + cuenta;

      now.saldoAnioAnterior = getMontoxCuenta2(grupoCuentaAnterior.get(key), now.acctvalue,
          now.consider, now.considerwhen, now.greaterThanAYear).toString();
      now.saldoAnioActual = getMontoxCuenta2(grupoCuentasActuales.get(key), now.acctvalue,
          now.consider, now.considerwhen, now.greaterThanAYear).toString();

      // if(now.greaterThanAYear.equals("SCO_NONE")){
      // if(now.consider.equals("Y") && now.considerwhen.equals("SCO_PLUS")){
      // now.saldoAnioAnterior=getMontoxCuenta(dataFactsAnterior,now.acctvalue,1,null).toString();
      // now.saldoAnioActual=getMontoxCuenta(dataFactsActual,now.acctvalue,1,null).toString();
      // }else if(now.consider.equals("Y") && !now.considerwhen.equals("SCO_PLUS")){
      // now.saldoAnioAnterior=getMontoxCuenta(dataFactsAnterior,now.acctvalue,-1,null).toString();
      // now.saldoAnioActual=getMontoxCuenta(dataFactsActual,now.acctvalue,-1,null).toString();
      // }else {
      // now.saldoAnioAnterior=getMontoxCuenta(dataFactsAnterior,now.acctvalue,null,null).toString();
      // now.saldoAnioActual=getMontoxCuenta(dataFactsActual,now.acctvalue,null,null).toString();
      // }
      // }else if (now.greaterThanAYear.equals("SCO_N")){
      // now.saldoAnioAnterior=getMontoxCuenta(dataFactsAnterior,now.acctvalue,null,"NO").toString();
      // now.saldoAnioActual=getMontoxCuenta(dataFactsActual,now.acctvalue,null,"NO").toString();
      // }else if (now.greaterThanAYear.equals("SCO_Y")){
      // now.saldoAnioAnterior=getMontoxCuenta(dataFactsAnterior,now.acctvalue,null,"SI").toString();
      // now.saldoAnioActual=getMontoxCuenta(dataFactsActual,now.acctvalue,null,"SI").toString();
      // }
    }

    BigDecimal sumaSaldoAnterior = BigDecimal.ZERO;
    BigDecimal sumaSaldoActual = BigDecimal.ZERO;

    for (int i = 0; i < dataCategorias.length; i++) {

      ReporteEstadoSituacionFinancieraData now = dataCategorias[i];
      ReporteEstadoSituacionFinancieraData next = (i + 1 == dataCategorias.length) ? new ReporteEstadoSituacionFinancieraData()
          : dataCategorias[i + 1];

      sumaSaldoAnterior = new BigDecimal(now.saldoAnioAnterior).multiply(
          new BigDecimal(now.considerar)).add(sumaSaldoAnterior);
      sumaSaldoActual = new BigDecimal(now.saldoAnioActual)
          .multiply(new BigDecimal(now.considerar)).add(sumaSaldoActual);

      if (!now.order1.equals(next.order1) || !now.order2.equals(next.order2)
          || !now.categoria.equals(next.categoria) || !now.subCategoria.equals(next.subCategoria)
          || !now.grupoCategoria.equals(next.grupoCategoria)) {

        now.saldoAnioAnterior = sumaSaldoAnterior.multiply(new BigDecimal(now.signo)).toString();
        now.saldoAnioActual = sumaSaldoActual.multiply(new BigDecimal(now.signo)).toString();

        dataList.add(now);

        sumaSaldoAnterior = new BigDecimal(0);
        sumaSaldoActual = new BigDecimal(0);
      }
    }

    // SE CREA LAS LISTAS DE BEANS PARA SER PASADADOS COMO DATA_SOURCE A LOS SUBREPORTES
    ArrayList<ReporteEstadoSituacionFinancieraBean> dataSubReporteActivos = new ArrayList<ReporteEstadoSituacionFinancieraBean>();
    ArrayList<ReporteEstadoSituacionFinancieraBean> dataSubReportePasivosPatrimonio = new ArrayList<ReporteEstadoSituacionFinancieraBean>();

    BigDecimal totalActivoAnterior = BigDecimal.ZERO;
    BigDecimal totalActivoActual = BigDecimal.ZERO;
    BigDecimal totalPasivoAnterior = BigDecimal.ZERO;
    BigDecimal totalPasivoActual = BigDecimal.ZERO;

    ReporteEstadoSituacionFinancieraBean bean = new ReporteEstadoSituacionFinancieraBean();
    ;
    for (int i = 0; i < dataList.size(); i++) {
      bean = new ReporteEstadoSituacionFinancieraBean();
      bean.setTipo(dataList.get(i).tipo);
      bean.setOrder1(dataList.get(i).order1);
      bean.setOrder2(dataList.get(i).order2);
      bean.setCategoria(dataList.get(i).categoria);
      bean.setSubCategoria(dataList.get(i).subCategoria);
      bean.setTipoGrupo(dataList.get(i).tipoGrupo);
      bean.setSaldoAnioAnterior(new BigDecimal(dataList.get(i).saldoAnioAnterior));
      bean.setSaldoAnioActual(new BigDecimal(dataList.get(i).saldoAnioActual));
      bean.setGrupoCategoria(dataList.get(i).grupoCategoria);

      if (bean.getTipo().equals("activos")) {
        totalActivoActual = totalActivoActual.add(bean.getSaldoAnioActual());
        totalActivoAnterior = totalActivoAnterior.add(bean.getSaldoAnioAnterior());
        dataSubReporteActivos.add(bean);
      } else {
        totalPasivoActual = totalPasivoActual.add(bean.getSaldoAnioActual());
        totalPasivoAnterior = totalPasivoAnterior.add(bean.getSaldoAnioAnterior());
        dataSubReportePasivosPatrimonio.add(bean);
      }
    }

    // COLOCANDO EL SALDO FINAL PARA EL PASIVO :
    ReporteEstadoSituacionFinancieraBean beanAjuste = new ReporteEstadoSituacionFinancieraBean();
    beanAjuste.setTipo(bean.getTipo());
    beanAjuste.setOrder1(bean.getOrder1());
    beanAjuste.setOrder2(bean.getOrder2());
    beanAjuste.setCategoria(bean.getCategoria());
    beanAjuste.setSubCategoria("RESULTADO DEL EJERCICIO ");
    beanAjuste.setTipoGrupo(bean.getTipoGrupo());
    beanAjuste.setSaldoAnioAnterior(totalActivoAnterior.abs().subtract(totalPasivoAnterior));
    beanAjuste.setSaldoAnioActual(totalActivoActual.abs().subtract(totalPasivoActual));
    beanAjuste.setGrupoCategoria(bean.getGrupoCategoria());
    dataSubReportePasivosPatrimonio.add(beanAjuste);

    String strLanguage = vars.getLanguage();
    String strBaseDesign = getBaseDesignPath(strLanguage);

    JasperReport subReportActivo;
    JasperReport subReportPasivoPatrimonio;

    String strOutput;
    String strReportName;
    if (vars.commandIn("PDF")) {
      strOutput = "pdf";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteEstadoSituacionFinanciera.jrxml";

      try {
        JasperDesign jasperDesignLines = JRXmlLoader.load(strBaseDesign
            + "/pe/com/unifiedgo/report/ad_reports/ReporteEstadoSituacionFinanciera_activo.jrxml");

        subReportActivo = JasperCompileManager.compileReport(jasperDesignLines);
        jasperDesignLines = JRXmlLoader
            .load(strBaseDesign
                + "/pe/com/unifiedgo/report/ad_reports/ReporteEstadoSituacionFinanciera_pasivo_patrimonio.jrxml");
        subReportPasivoPatrimonio = JasperCompileManager.compileReport(jasperDesignLines);

      } catch (JRException e) {
        throw new ServletException(e.getMessage());
      }
    } else {
      strOutput = "xls";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteEstadoSituacionFinancieraExcel.jrxml";

      try {
        JasperDesign jasperDesignLines = JRXmlLoader
            .load(strBaseDesign
                + "/pe/com/unifiedgo/report/ad_reports/ReporteEstadoSituacionFinanciera_activoExcel.jrxml");
        subReportActivo = JasperCompileManager.compileReport(jasperDesignLines);

        jasperDesignLines = JRXmlLoader
            .load(strBaseDesign
                + "/pe/com/unifiedgo/report/ad_reports/ReporteEstadoSituacionFinanciera_pasivo_patrimonioExcel.jrxml");

        subReportPasivoPatrimonio = JasperCompileManager.compileReport(jasperDesignLines);

      } catch (JRException e) {
        throw new ServletException(e.getMessage());
      }
    }

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("Ruc", ReporteEstadoSituacionFinancieraData.selectRucOrg(this, strOrg));
    parameters.put("organizacion",
        ReporteEstadoSituacionFinancieraData.selectSocialName(this, strOrg));

    parameters.put("soloUnAnio", strSoloUnAnio);

    parameters.put("dateFrom", strDateFrom);
    parameters.put("dateTo", strDateTo);

    parameters.put("fechaFinAnioAnterior", fechaFinAnioAnterior);
    parameters.put("fechaIniAnioActual", fechaIniAnioActual);

    parameters.put("fechaIniAnioAnterior", fechaIniAnioAnterior);
    parameters.put("fechaFinAnioActual", fechaFinAnioActual);

    parameters.put("anio1", anio1);
    parameters.put("anio2", anio2);

    parameters.put("SUBREP_ACTIVO", subReportActivo);

    parameters.put("SUBREP_PASIVO_PATRIMONIO", subReportPasivoPatrimonio);
    parameters.put("CLIENTE", strCliente);
    parameters.put("ORGANIZACION", strOrgFamily);

    parameters.put("DATA_SOURCE_PRUEBA_LIST", dataSubReporteActivos);

    parameters.put("DATA_SOURCE_ACTIVOS", dataSubReporteActivos);
    parameters.put("DATA_SOURCE_PASIVOS_PATRIMONIO", dataSubReportePasivosPatrimonio);

    parameters.put("DIA_COMUN", dia_comun);
    parameters.put("MES_COMUN", mes_comun);

    dataFinal = new ReporteEstadoSituacionFinancieraData[1];

    renderJR(vars, response, strReportName, "Reporte_Estado_de_Situacion_Financiera", strOutput,
        parameters, dataFinal, null);
  }

  private static BigDecimal getMontoxCuenta2(
      ArrayList<ReporteEstadoSituacionFinancieraData> dataCuentas, String cuenta,
      String considerarSigno, String signo, String mayorAnio) {

    if (dataCuentas == null)
      return BigDecimal.ZERO;

    Integer condicionMonto = 0;
    HashMap<String, BigDecimal> grupoMontoCuenta = new HashMap<String, BigDecimal>();
    ArrayList<BigDecimal> montosCuentas = new ArrayList<BigDecimal>();

    cuenta = cuenta.replace("-", "").replace("%", "");

    if (considerarSigno.equals("Y")) {
      if (signo.equals("SCO_PLUS"))
        condicionMonto = 1;
      else if (signo.equals("SCO_MINUS"))
        condicionMonto = -1;
    } else
      condicionMonto = 0;

    if (mayorAnio.equalsIgnoreCase("SCO_Y"))
      mayorAnio = "SI";
    else if (mayorAnio.equalsIgnoreCase("SCO_N"))
      mayorAnio = "NO";
    else {
      mayorAnio = "";
    }

    // CONSOLIDANDO
    BigDecimal monto = BigDecimal.ZERO;
    for (int i = 0; i < dataCuentas.size(); i++) {
      if (dataCuentas.get(i).greaterThanAYear.contains(mayorAnio)) {
        monto = new BigDecimal(dataCuentas.get(i).factAmount);
        if (grupoMontoCuenta.containsKey(dataCuentas.get(i).acctvalue)) {
          monto = monto.add(grupoMontoCuenta.get(dataCuentas.get(i).acctvalue));
        }
        grupoMontoCuenta.put(dataCuentas.get(i).acctvalue, monto);
      }
    }

    montosCuentas = new ArrayList<BigDecimal>(grupoMontoCuenta.values());

    BigDecimal suma = BigDecimal.ZERO;

    for (int i = 0; i < montosCuentas.size(); i++) {

      monto = montosCuentas.get(i);

      if (condicionMonto != 0 && monto.compareTo(BigDecimal.ZERO) != condicionMonto)
        monto = BigDecimal.ZERO;

      suma = suma.add(monto);
    }

    return suma;
  }

  // private BigDecimal getMontoxCuenta(ReporteEstadoSituacionFinancieraData [] dataCuentas,String
  // cuenta,
  // Integer condicionMonto,String mayorAnio){
  //
  // cuenta=cuenta.replace("-", "").replace("%", "");
  //
  // BigDecimal suma= BigDecimal.ZERO;
  //
  // for(int i=0;i<dataCuentas.length;i++){
  //
  // ReporteEstadoSituacionFinancieraData x = dataCuentas[i];
  // if(x==null)continue;
  //
  // if(x.acctvalue.startsWith(cuenta) && !cuenta.equalsIgnoreCase("")){
  // if(mayorAnio==null ){
  // BigDecimal monto=getConsolidadoXCuenta(dataCuentas, x.acctvalue);
  // if(condicionMonto==null||monto.compareTo(BigDecimal.ZERO)==condicionMonto)
  // suma=suma.add(monto);
  // }else {
  // if(x.greaterThanAYear.equals(mayorAnio))
  // suma=suma.add(new BigDecimal(x.factAmount));
  // }
  // }
  // }
  //
  // if (condicionMonto==null) return suma;
  // if (suma.compareTo(BigDecimal.ZERO)==condicionMonto) return suma;
  // else return BigDecimal.ZERO;
  // }

  // FUNCION QUE PERMITE OBTENER EL CONSOLIDADO POR CUENTA XXXXXXX, POR QUE PUEDEN Q ESTEN AGRUPADAS
  // POR greaterThenAYear Y TENGA MONTOS PARA "SI" y "NO"
  private BigDecimal getConsolidadoXCuenta(ArrayList<ReporteEstadoSituacionFinancieraData> data,
      String cuenta) {
    BigDecimal sumaConsolidada = BigDecimal.ZERO;
    boolean entro = false;
    for (int i = 0; i < data.size(); i++) {
      if (data.get(i) == null)
        continue;
      if (data.get(i).acctvalue.equals(cuenta)) {
        sumaConsolidada = sumaConsolidada.add(new BigDecimal(data.get(i).factAmount));
        entro = true;
      } else {
        if (entro)
          break;
      }
    }
    return sumaConsolidada;
  }

  // FUNCION QUE ELIMINA LOS REGISTROS QUE YA HAN SIDO UTILIZADOS PARA OPTIMIZAR EL TIEMPO DE
  // BUSQUEDA
  private void eliminaCuentasUtilizadas(ReporteEstadoSituacionFinancieraData[] dataCategorias,
      ReporteEstadoSituacionFinancieraData[] dataCuentas, String cuenta, int posicion) {

    if (cuenta == null || cuenta.equalsIgnoreCase(""))
      return;
    cuenta = cuenta.replace("-", "").replace("%", "");
    cuenta = cuenta.substring(0, 2);

    boolean utilizaraDespues = false;
    for (int i = posicion + 1; i < dataCategorias.length; i++) {
      if (dataCategorias[i].acctvalue.replace("-", "").replace("%", "").startsWith(cuenta)) {
        utilizaraDespues = true;
        break;
      }
    }

    if (!utilizaraDespues) {
      boolean entro = false;
      for (int i = 0; i < dataCuentas.length; i++) {
        if (dataCuentas[i] == null)
          continue;
        if (dataCuentas[i].acctvalue.startsWith(cuenta)) {
          dataCuentas[i] = null;
          entro = true;
        } else if (entro)
          break;
      }
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

  private String nombreMes(String mes) {

    Integer mesInt = new Integer(mes);

    String[] meses = { "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto",
        "Septiembre", "Octubre", "Noviembre", "Diciembre" };
    String retornaMes = "";

    retornaMes = meses[mesInt - 1];
    return retornaMes;

  }

  public static ReporteEstadoSituacionFinancieraData copy(ReporteEstadoSituacionFinancieraData obj) {
    ReporteEstadoSituacionFinancieraData newObject = new ReporteEstadoSituacionFinancieraData();
    newObject.acctvalue = obj.acctvalue;
    newObject.categoria = obj.categoria;
    newObject.categoriaId = obj.categoriaId;
    newObject.categoriaSunatCode = obj.categoriaSunatCode;
    newObject.factAmount = obj.factAmount;
    newObject.grupoCategoria = obj.grupoCategoria;
    newObject.idcliente = obj.idcliente;
    newObject.idorganizacion = obj.idorganizacion;
    newObject.idperiodo = obj.idperiodo;
    newObject.order1 = obj.order1;
    newObject.order2 = obj.order2;
    newObject.periodo = obj.periodo;
    newObject.saldoAnioActual = obj.saldoAnioActual;
    newObject.saldoAnioAnterior = obj.saldoAnioAnterior;
    newObject.subCategoria = obj.subCategoria;
    newObject.subCategoriaId = obj.subCategoriaId;
    newObject.subCategoriaSunatCode = obj.subCategoriaSunatCode;
    newObject.tipo = obj.tipo;
    newObject.tipoGrupo = obj.tipoGrupo;
    return newObject;
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

    Object[] dataFinal = getEstadoSituacionFinancieraData(conn, vars, sdf.format(dateFrom),
        sdf.format(dateTo), adUserClient, strOrg);

    System.out.println("datafinal length:" + dataFinal.length);

    StructPle sunatPle = getStringData(dataFinal, dateFrom, dateTo, strOrg);

    return sunatPle;
  }

  private static StructPle getStringData(Object[] data, Date dateFrom, Date dateTo, String strOrg) {
    ReporteEstadoSituacionFinancieraData[] activos = (ReporteEstadoSituacionFinancieraData[]) data[0];
    ReporteEstadoSituacionFinancieraData[] pasivosypatrimonio = (ReporteEstadoSituacionFinancieraData[]) data[1];

    ReporteEstadoSituacionFinancieraData[] pasivos = null;
    ReporteEstadoSituacionFinancieraData[] patrimonio = null;

    Vector<ReporteEstadoSituacionFinancieraData> vector_pasivos = new Vector<ReporteEstadoSituacionFinancieraData>(
        0);
    Vector<ReporteEstadoSituacionFinancieraData> vector_patrimonio = new Vector<ReporteEstadoSituacionFinancieraData>(
        0);
    for (int i = 0; i < pasivosypatrimonio.length; i++) {
      if (pasivosypatrimonio[i].tipoGrupo.compareTo("PATRIMONIO") == 0)
        vector_patrimonio.add(pasivosypatrimonio[i]);
      else
        vector_pasivos.add(pasivosypatrimonio[i]);
    }
    patrimonio = new ReporteEstadoSituacionFinancieraData[vector_patrimonio.size()];
    vector_patrimonio.copyInto(patrimonio);

    pasivos = new ReporteEstadoSituacionFinancieraData[vector_pasivos.size()];
    vector_pasivos.copyInto(pasivos);

    Vector<ReporteEstadoSituacionFinancieraData> vector = new Vector<ReporteEstadoSituacionFinancieraData>(
        0);

    DecimalFormat df = new DecimalFormat("0.00");
    df.setRoundingMode(RoundingMode.HALF_UP);
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    BigDecimal act_totGrupoAnioActual = BigDecimal.ZERO, act_totTipoAnioActual = BigDecimal.ZERO, act_otrosactivosnofinancieros_tot = BigDecimal.ZERO;
    String act_categoria = (activos.length > 0) ? activos[0].categoria : null;
    boolean act_corrienteAgregado = false;
    int act_otrosactivosnofinancieros_index = 0;
    /***** ACTIVOS *****/
    for (int i = 0; i < activos.length; i++) {
      if ((activos[i].categoriaSunatCode == null || "".equals(activos[i].categoriaSunatCode))
          && (activos[i].subCategoriaSunatCode == null || ""
              .equals(activos[i].subCategoriaSunatCode)))
        continue;

      // FILA PARA TIPOGRUPO
      if (!act_corrienteAgregado && activos[i].tipoGrupo.compareTo("ACTIVOS CORRIENTES") != 0) {
        ReporteEstadoSituacionFinancieraData row = new ReporteEstadoSituacionFinancieraData();
        row.sunatCode = "1D01ST";
        row.saldo = df.format(act_totGrupoAnioActual);
        vector.add(row);
        if (act_otrosactivosnofinancieros_tot.compareTo(BigDecimal.ZERO) > 0
            && act_otrosactivosnofinancieros_index < vector.size()) {
          vector.get(act_otrosactivosnofinancieros_index).saldo = df.format(new BigDecimal(vector
              .get(act_otrosactivosnofinancieros_index).saldo)
              .add(act_otrosactivosnofinancieros_tot));
        }
        act_corrienteAgregado = true;
        act_totGrupoAnioActual = BigDecimal.ZERO;
      }
      // FILA PARA CATEGORIA
      if (activos[i].categoria.compareTo(act_categoria) != 0
          && !"ACTIVOS NO CORRIENTES".equals(activos[i].categoria)) {
        ReporteEstadoSituacionFinancieraData row = new ReporteEstadoSituacionFinancieraData();
        row.sunatCode = activos[i].categoriaSunatCode;
        row.saldo = "0.00";
        vector.add(row);

        act_categoria = activos[i].categoria;
      }
      // FILA PARA SUB-CATEGORIA
      if (activos[i].subCategoriaSunatCode != null && !"".equals(activos[i].subCategoriaSunatCode)) {
        ReporteEstadoSituacionFinancieraData row = new ReporteEstadoSituacionFinancieraData();
        row.sunatCode = activos[i].subCategoriaSunatCode;
        row.saldo = activos[i].saldoAnioActual;
        vector.add(row);

        if ("1D0113".equals(row.sunatCode))
          act_otrosactivosnofinancieros_index = vector.size() - 1;
      } else {
        act_otrosactivosnofinancieros_tot = act_otrosactivosnofinancieros_tot.add(new BigDecimal(
            activos[i].saldoAnioActual));
      }
      // ACUMULADOS
      act_totGrupoAnioActual = act_totGrupoAnioActual
          .add(new BigDecimal(activos[i].saldoAnioActual));
      act_totTipoAnioActual = act_totTipoAnioActual.add(new BigDecimal(activos[i].saldoAnioActual));
    }
    if (activos.length > 0) {
      // FILA PARA TIPOGRUPO
      ReporteEstadoSituacionFinancieraData row = new ReporteEstadoSituacionFinancieraData();
      row.sunatCode = "1D02ST"; // "ACTIVOS NO CORRIENTES"
      row.saldo = df.format(act_totGrupoAnioActual);
      vector.add(row);
      // FILA PARA TIPO
      ReporteEstadoSituacionFinancieraData row2 = new ReporteEstadoSituacionFinancieraData();
      row2.sunatCode = "1D020T"; // TOTAL DE ACTIVOS
      row2.saldo = df.format(act_totTipoAnioActual);
      vector.add(row2);
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    BigDecimal pas_totGrupoAnioActual = BigDecimal.ZERO, pas_totTipoAnioActual = BigDecimal.ZERO;
    String pas_categoria = (pasivos != null && pasivos.length > 0) ? pasivos[0].categoria : null;
    boolean pas_corrienteAgregado = false;
    /****** PASIVOS ******/
    for (int i = 0; pasivos != null && i < pasivos.length; i++) {
      if ((pasivos[i].categoriaSunatCode == null || "".equals(pasivos[i].categoriaSunatCode))
          && (pasivos[i].subCategoriaSunatCode == null || ""
              .equals(pasivos[i].subCategoriaSunatCode)))
        continue;

      // FILA PARA TIPOGRUPO
      if (!pas_corrienteAgregado && pasivos[i].tipoGrupo.compareTo("PASIVOS CORRIENTES") != 0) {
        ReporteEstadoSituacionFinancieraData row = new ReporteEstadoSituacionFinancieraData();
        row.sunatCode = "1D03ST";
        row.saldo = df.format(pas_totGrupoAnioActual);
        vector.add(row);

        pas_corrienteAgregado = true;
        pas_totGrupoAnioActual = BigDecimal.ZERO;
      }
      // FILA PARA CATEGORIA
      if (pasivos[i].categoria.compareTo(pas_categoria) != 0
          && !"PASIVOS NO CORRIENTES".equals(pasivos[i].categoria)) {
        ReporteEstadoSituacionFinancieraData row = new ReporteEstadoSituacionFinancieraData();
        row.sunatCode = pasivos[i].categoriaSunatCode;
        row.saldo = "0.00";
        vector.add(row);

        pas_categoria = pasivos[i].categoria;
      }
      // FILA PARA SUB-CATEGORIA
      if (pasivos[i].subCategoriaSunatCode != null && !"".equals(pasivos[i].subCategoriaSunatCode)) {
        ReporteEstadoSituacionFinancieraData row = new ReporteEstadoSituacionFinancieraData();
        row.sunatCode = pasivos[i].subCategoriaSunatCode;
        row.saldo = pasivos[i].saldoAnioActual;
        vector.add(row);
      }
      // ACUMULADOS
      pas_totGrupoAnioActual = pas_totGrupoAnioActual
          .add(new BigDecimal(pasivos[i].saldoAnioActual));
      pas_totTipoAnioActual = pas_totTipoAnioActual.add(new BigDecimal(pasivos[i].saldoAnioActual));
    }
    if (pasivos.length > 0) {
      // FILA PARA TIPOGRUPO
      ReporteEstadoSituacionFinancieraData row = new ReporteEstadoSituacionFinancieraData();
      row.sunatCode = "1D04ST"; // "PASIVOS NO CORRIENTES"
      row.saldo = df.format(pas_totGrupoAnioActual);
      vector.add(row);
      // FILA PARA TIPO
      ReporteEstadoSituacionFinancieraData row2 = new ReporteEstadoSituacionFinancieraData();
      row2.sunatCode = "1D040T"; // TOTAL PASIVOS
      row2.saldo = df.format(pas_totTipoAnioActual);
      vector.add(row2);
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    BigDecimal pat_totTipoAnioActual = BigDecimal.ZERO, pat_otrasreservasdecapital_tot = BigDecimal.ZERO, pat_resultadosacumulados_tot = BigDecimal.ZERO;
    int pat_otrasreservasdecapital_index = 0, pat_resultadosacumulados_index = 0;
    String str1D0702 = "1D0702", str1D0711 = "1D0711";
    boolean is1D0702 = false, is1D0711 = false;
    /***** PATRIMONIO *****/
    for (int i = 0; patrimonio != null && i < patrimonio.length; i++) {
      if ((patrimonio[i].categoriaSunatCode == null || "".equals(patrimonio[i].categoriaSunatCode))
          && (patrimonio[i].subCategoriaSunatCode == null || ""
              .equals(patrimonio[i].subCategoriaSunatCode)))
        continue;

      // FILA PARA SUB-CATEGORIA
      if (patrimonio[i].subCategoriaSunatCode != null
          && !"".equals(patrimonio[i].subCategoriaSunatCode)) {
        ReporteEstadoSituacionFinancieraData row = new ReporteEstadoSituacionFinancieraData();
        row.sunatCode = patrimonio[i].subCategoriaSunatCode;
        row.saldo = patrimonio[i].saldoAnioActual;
        vector.add(row);

        // para no agregarlas al final si ya existen
        if (str1D0702.equals(row.sunatCode))
          is1D0702 = true;
        else if (str1D0711.equals(row.sunatCode))
          is1D0711 = true;

        if ("1D0712".equals(row.sunatCode))
          pat_otrasreservasdecapital_index = vector.size() - 1;
        else if ("1D0707".equals(row.sunatCode))
          pat_resultadosacumulados_index = vector.size() - 1;
      } else {
        if ("CAPITAL ADICIONAL".equals(patrimonio[i].subCategoria)) {
          pat_otrasreservasdecapital_tot = pat_otrasreservasdecapital_tot.add(new BigDecimal(
              patrimonio[i].saldoAnioActual));
        } else if ("RESULTADOS NO REALIZADOS".equals(patrimonio[i].subCategoria)) {
          pat_resultadosacumulados_tot = pat_resultadosacumulados_tot.add(new BigDecimal(
              patrimonio[i].saldoAnioActual));
        }
      }
      // ACUMULADOS
      pat_totTipoAnioActual = pat_totTipoAnioActual.add(new BigDecimal(
          patrimonio[i].saldoAnioActual));
    }
    if (patrimonio.length > 0) {
      if (pat_otrasreservasdecapital_tot.compareTo(BigDecimal.ZERO) > 0
          && pat_otrasreservasdecapital_index < vector.size()) {
        vector.get(pat_otrasreservasdecapital_index).saldo = df.format(new BigDecimal(vector
            .get(pat_otrasreservasdecapital_index).saldo).add(pat_otrasreservasdecapital_tot));
      }
      if (pat_resultadosacumulados_tot.compareTo(BigDecimal.ZERO) > 0
          && pat_resultadosacumulados_index < vector.size()) {
        vector.get(pat_resultadosacumulados_index).saldo = df.format(new BigDecimal(vector
            .get(pat_resultadosacumulados_index).saldo).add(pat_resultadosacumulados_tot));
      }

      // SUB-CATEGORIAS NO CONSIDERADAS EN EL RUBRO DE ESTADOS FINANCIEROS
      if (!is1D0702) {
        ReporteEstadoSituacionFinancieraData row1D0702 = new ReporteEstadoSituacionFinancieraData();
        row1D0702.sunatCode = "1D0702"; // Primas de Emisión no existe como categoria
        row1D0702.saldo = "0.00";
        vector.add(row1D0702);
      }
      if (!is1D0711) {
        ReporteEstadoSituacionFinancieraData row1D0711 = new ReporteEstadoSituacionFinancieraData();
        row1D0711.sunatCode = "1D0711"; // TOTAL PATRIMONIO
        row1D0711.saldo = "0.00";
        vector.add(row1D0711);
      }

      // FILA PARA TIPO
      ReporteEstadoSituacionFinancieraData row = new ReporteEstadoSituacionFinancieraData();
      row.sunatCode = "1D07ST"; // TOTAL PATRIMONIO
      row.saldo = df.format(pat_totTipoAnioActual);
      vector.add(row);
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /***** TOTAL PASIVOS Y PATRIMONIO *****/
    ReporteEstadoSituacionFinancieraData row2 = new ReporteEstadoSituacionFinancieraData();
    row2.sunatCode = "1D070T"; // TOTAL PASIVOS Y PATRIMONIO
    row2.saldo = df.format(pat_totTipoAnioActual.add(pas_totTipoAnioActual));
    vector.add(row2);

    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    StructPle sunatPle = new StructPle();
    sunatPle.numEntries = 0;

    StringBuffer sb = new StringBuffer();

    Organization org = OBDal.getInstance().get(Organization.class, strOrg);
    String rucAdq = SunatUtil.LPad(org.getOrganizationInformationList().get(0).getTaxID(), 11, '0');

    SimpleDateFormat dt = new SimpleDateFormat("yyyyMMdd");

    String filename = "LE" + rucAdq + dt.format(dateTo) + "030100011111.TXT"; // LERRRRRRRRRRRAAAAMMDD030100CCOIM1.TXT

    String periodoTrib = dt.format(dateTo);
    String codigoCatalago = "01";
    String estadoOp = "1";

    int jj = 0;
    for (int i = 0; i < vector.size(); i++) {

      ReporteEstadoSituacionFinancieraData led = (ReporteEstadoSituacionFinancieraData) vector
          .get(i);

      String codigoRubro = led.sunatCode;
      String saldoRubro = led.saldo;

      String linea = periodoTrib + "|" + codigoCatalago + "|" + codigoRubro + "|" + saldoRubro
          + "|" + estadoOp + "|";

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

  @Override
  public String getServletInfo() {
    return "Servlet ReporteEstadoSituacionFinanciera. This Servlet was made by Pablo Sarobe modified by everybody";
  } // end of getServletInfo() method
}
