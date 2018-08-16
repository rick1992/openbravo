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
import java.util.Collections;
import java.util.Comparator;
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

public class ReporteEstadoFlujoEfectivo extends HttpSecureAppServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		VariablesSecureApp vars = new VariablesSecureApp(request);

		if (log4j.isDebugEnabled())
			log4j.debug("Command: " + vars.getStringParameter("Command"));

		if (vars.commandIn("DEFAULT")) {

			Calendar fActual = new GregorianCalendar();

			String fechaFinAnioAnterior = "31-12-" + (fActual.get(Calendar.YEAR) - 1);
			String fechaFinAnioActual = "31-12-" + (fActual.get(Calendar.YEAR));

			String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReporteEstadoFlujoEfectivo|DateFrom",
					fechaFinAnioAnterior);
			String strDateTo = vars.getGlobalVariable("inpDateTo", "ReporteEstadoFlujoEfectivo|DateTo",
					fechaFinAnioActual);

			String strOrg = vars.getGlobalVariable("inpOrg", "ReporteEstadoFlujoEfectivo|Org", "0");
			String strRecord = vars.getGlobalVariable("inpRecord", "ReporteEstadoFlujoEfectivo|Record", "");
			String strTable = vars.getGlobalVariable("inpTable", "ReporteEstadoFlujoEfectivo|Table", "");
			printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg, strTable, strRecord);

		} else if (vars.commandIn("DIRECT")) {
			String strTable = vars.getGlobalVariable("inpTable", "ReporteEstadoFlujoEfectivo|Table");
			String strRecord = vars.getGlobalVariable("inpRecord", "ReporteEstadoFlujoEfectivo|Record");

			setHistoryCommand(request, "DIRECT");
			vars.setSessionValue("ReporteEstadoFlujoEfectivo.initRecordNumber", "0");
			printPageDataSheet(response, vars, "", "", "", strTable, strRecord);

		} else if (vars.commandIn("FIND")) {
			String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom", "ReporteEstadoFlujoEfectivo|DateFrom");
			String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReporteEstadoFlujoEfectivo|DateTo");
			String strOrg = vars.getGlobalVariable("inpOrg", "ReporteEstadoFlujoEfectivo|Org", "0");
			vars.setSessionValue("ReporteEstadoFlujoEfectivo.initRecordNumber", "0");
			setHistoryCommand(request, "DEFAULT");
			printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg, "", "");

		} else if (vars.commandIn("PDF", "XLS")) {
			if (log4j.isDebugEnabled())
				log4j.debug("PDF");
			String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom", "ReporteEstadoFlujoEfectivo|DateFrom");
			String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReporteEstadoFlujoEfectivo|DateTo");
			String strOrg = vars.getGlobalVariable("inpOrg", "ReporteEstadoFlujoEfectivo|Org", "0");
			String strSoloUnMes = vars.getStringParameter("inpSoloUnMes");

			String strTable = vars.getStringParameter("inpTable");
			String strRecord = vars.getStringParameter("inpRecord");
			setHistoryCommand(request, "DEFAULT");
			printPagePDF(request, response, vars, strDateFrom, strDateTo, strOrg, strTable, strRecord, strSoloUnMes);

		} else
			pageError(response);
	}

	private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strDateFrom,
			String strDateTo, String strOrg, String strTable, String strRecord) throws IOException, ServletException {
		if (log4j.isDebugEnabled())
			log4j.debug("Output: dataSheet");
		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();
		XmlDocument xmlDocument = null;
		ReporteEstadoFlujoEfectivoData[] data = null;
		String strPosition = "0";
		ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReporteEstadoFlujoEfectivo", false, "", "",
				"imprimir();return false;", false, "ad_reports", strReplaceWith, false, true);
		toolbar.setEmail(false);

		toolbar.prepareSimpleToolBarTemplate();
		toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");

		if (data == null || data.length == 0) {
			String discard[] = { "secTable" };
			xmlDocument = xmlEngine
					.readXmlTemplate("pe/com/unifiedgo/report/ad_reports/ReporteEstadoFlujoEfectivo", discard)
					.createXmlDocument();
			data = new ReporteEstadoFlujoEfectivoData[1];
			data[0] = new ReporteEstadoFlujoEfectivoData();
			data[0].rownum = "0";
		}
		xmlDocument.setParameter("toolbar", toolbar.toString());
		try {
			WindowTabs tabs = new WindowTabs(this, vars,
					"pe.com.unifiedgo.report.ad_reports.ReporteEstadoFlujoEfectivo");
			xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
			xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
			xmlDocument.setParameter("childTabContainer", tabs.childTabs());
			xmlDocument.setParameter("theme", vars.getTheme());
			NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReporteEstadoFlujoEfectivo.html",
					classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
			xmlDocument.setParameter("navigationBar", nav.toString());
			LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReporteEstadoFlujoEfectivo.html",
					strReplaceWith);
			xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
		} catch (Exception ex) {
			throw new ServletException(ex);
		}
		{
			OBError myMessage = vars.getMessage("ReporteEstadoFlujoEfectivo");
			vars.removeMessage("ReporteEstadoFlujoEfectivo");
			if (myMessage != null) {
				xmlDocument.setParameter("messageType", myMessage.getType());
				xmlDocument.setParameter("messageTitle", myMessage.getTitle());
				xmlDocument.setParameter("messageMessage", myMessage.getMessage());
			}
		}

		xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));

		try {
			ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
					"0C754881EAD94243A161111916E9B9C6",
					Utility.getContext(this, vars, "#AccessibleOrgTree", "ReporteEstadoFlujoEfectivo"),
					Utility.getContext(this, vars, "#User_Client", "ReporteEstadoFlujoEfectivo"), '*');
			comboTableData.fillParameters(null, "ReporteEstadoFlujoEfectivo", "");
			xmlDocument.setData("reportAD_ORGID", "liststructure", comboTableData.select(false));
		} catch (Exception ex) {
			throw new ServletException(ex);
		}

		xmlDocument.setData("reportC_ACCTSCHEMA_ID", "liststructure",
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
		xmlDocument.setParameter("paramPeriodosArray",
				Utility.arrayInfinitasEntradas("idperiodo;periodo;fechainicial;fechafinal;idorganizacion",
						"arrPeriodos", ReporteEstadoFlujoEfectivoData.select_periodos(this)));
		vars.setSessionValue("ReporteEstadoFlujoEfectivo|Record", strRecord);
		vars.setSessionValue("ReporteEstadoFlujoEfectivo|Table", strTable);

		xmlDocument.setData("structure1", data);
		out.println(xmlDocument.print());
		out.close();
	}

	public static ReporteEstadoFlujoEfectivoData[] getEstadoFlujoEfectivoData(ConnectionProvider conn,
			VariablesSecureApp vars, String strDateAnioAnterior, String strDateAnioActual, String adUserClient,
			String strOrg) throws IOException, ServletException {
		ReporteEstadoFlujoEfectivoData[] dataCategorias = null;
		ReporteEstadoFlujoEfectivoData[] dataFactsAnterior = null;
		ReporteEstadoFlujoEfectivoData[] dataFactsActual = null;
		ReporteEstadoFlujoEfectivoData[] dataFinal = null;

		ArrayList<String> listCuentas = new ArrayList<String>();
		ArrayList<ReporteEstadoFlujoEfectivoData> dataList = new ArrayList<ReporteEstadoFlujoEfectivoData>();
		HashMap<String, ArrayList<ReporteEstadoFlujoEfectivoData>> grupoFactsAnioActual = new HashMap<String, ArrayList<ReporteEstadoFlujoEfectivoData>>();
		HashMap<String, ArrayList<ReporteEstadoFlujoEfectivoData>> grupoFactsAnioAnterior = new HashMap<String, ArrayList<ReporteEstadoFlujoEfectivoData>>();
		HashMap<String, ReporteEstadoFlujoEfectivoData> grupoConsolidadoPorSubcategoria = new HashMap<String, ReporteEstadoFlujoEfectivoData>();

		String strTreeOrg = TreeData.getTreeOrg(conn, vars.getClient());
		String strOrgFamily = getFamily(conn, strTreeOrg, strOrg);

		String link = "//";

		String[] fechaAnioAnterior, fechaAnioActual;
		Integer anioAnterior, anioActual;
		String fechaIniAnioAnterior = null, fechaFinAnioAnterior = null, fechaIniAnioActual, fechaFinAnioActual;
		// Anio Anterior
		if (strDateAnioAnterior != null && !"".equals(strDateAnioAnterior.trim())) {
			fechaAnioAnterior = strDateAnioAnterior.split("-");
			anioAnterior = new Integer(fechaAnioAnterior[2]);

			fechaIniAnioAnterior = "01-01-" + (anioAnterior);
			fechaFinAnioAnterior = strDateAnioAnterior;
		}
		// Anio Actual
		fechaAnioActual = strDateAnioActual.split("-");
		anioActual = new Integer(fechaAnioActual[2]);

		fechaIniAnioActual = "01-01-" + anioActual;
		fechaFinAnioActual = strDateAnioActual;

		dataCategorias = ReporteEstadoFlujoEfectivoData.select_categorias(conn, adUserClient, strOrgFamily);
		// Anio Anterior
		if (strDateAnioAnterior != null && !"".equals(strDateAnioAnterior.trim())) {
			dataFactsAnterior = ReporteEstadoFlujoEfectivoData.select_facts_con_flujo(conn, strOrgFamily, adUserClient,
					fechaIniAnioAnterior, fechaFinAnioAnterior);
		}
		// Anio Actual
		dataFactsActual = ReporteEstadoFlujoEfectivoData.select_facts_con_flujo(conn, strOrgFamily, adUserClient,
				fechaIniAnioActual, fechaFinAnioActual);

		for (int i = 0; i < dataCategorias.length; i++) {

			ReporteEstadoFlujoEfectivoData now = dataCategorias[i];
			ArrayList<ReporteEstadoFlujoEfectivoData> lista = new ArrayList<ReporteEstadoFlujoEfectivoData>();

			String cuenta = now.acctvalue.replace("-", "").replace("%", "");
			String key = now.order1 + link + now.order2 + link + now.flujoEfectivoId + link + cuenta;
			// Anio Anterior
			if (strDateAnioAnterior != null && !"".equals(strDateAnioAnterior.trim())) {
				for (int j = 0; j < dataFactsAnterior.length; j++) {
					if (dataFactsAnterior[j].acctvalue.startsWith(cuenta) && !cuenta.equalsIgnoreCase("")
							&& dataFactsAnterior[j].flujoEfectivoId.equals(now.flujoEfectivoId)) {

						lista.add(dataFactsAnterior[j]);
					}
				}
				grupoFactsAnioAnterior.put(key, lista);
			}
			// Anio Actual
			lista = new ArrayList<ReporteEstadoFlujoEfectivoData>();
			for (int j = 0; j < dataFactsActual.length; j++) {
				if (dataFactsActual[j].acctvalue.startsWith(cuenta) && !cuenta.equalsIgnoreCase("")
						&& dataFactsActual[j].flujoEfectivoId.equals(now.flujoEfectivoId)) {

					lista.add(dataFactsActual[j]);
				}
			}
			grupoFactsAnioActual.put(key, lista);

			if (!cuenta.equals("") && !listCuentas.contains(cuenta + "%"))
				listCuentas.add(cuenta + "%");
		}

		// OBTENIENDO SALDOS PARA CADA CUENTA DE SUBCATEGORIA ---> CONSOLIDANDO
		// POR SUBCATEGORIA(SUBRUBRO)
		for (int i = 0; i < dataCategorias.length; i++) {

			ReporteEstadoFlujoEfectivoData now = dataCategorias[i];

			String cuenta = now.acctvalue.replace("-", "").replace("%", "");
			String key = now.order1 + link + now.order2 + link + now.flujoEfectivoId + link + cuenta;

			BigDecimal signo = new BigDecimal(now.signo);
			BigDecimal montoAnioAnterior = BigDecimal.ZERO, montoAnioActual;
			// Anio Anterior
			if (strDateAnioAnterior != null && !"".equals(strDateAnioAnterior.trim())) {
				montoAnioAnterior = getMontoxCuenta(grupoFactsAnioAnterior.get(key), now.acctvalue, now.consider,
						now.considerwhen, now.greaterThanAYear).multiply(signo);
			}
			// Anio Actual
			montoAnioActual = getMontoxCuenta(grupoFactsAnioActual.get(key), now.acctvalue, now.consider,
					now.considerwhen, now.greaterThanAYear).multiply(signo);

			// CONSOLIDANDO POR SUBCATEGORIA (SUBRUBRO)
			String keyConsolidado = now.order1 + link + now.order2;

			if (grupoConsolidadoPorSubcategoria.containsKey(keyConsolidado)) {
				// Anio Anterior
				if (strDateAnioAnterior != null && !"".equals(strDateAnioAnterior.trim())) {
					montoAnioAnterior = montoAnioAnterior
							.add(new BigDecimal(grupoConsolidadoPorSubcategoria.get(keyConsolidado).saldoAnioAnterior));
				}
				// Anio Actual
				montoAnioActual = montoAnioActual
						.add(new BigDecimal(grupoConsolidadoPorSubcategoria.get(keyConsolidado).saldoAnioActual));
			}
			// Anio Anterior
			if (strDateAnioAnterior != null && !"".equals(strDateAnioAnterior.trim())) {
				now.saldoAnioAnterior = montoAnioAnterior.toString();
			}
			// Anio Actual
			now.saldoAnioActual = montoAnioActual.toString();

			grupoConsolidadoPorSubcategoria.put(keyConsolidado, now);
		}

		dataList = new ArrayList<ReporteEstadoFlujoEfectivoData>(grupoConsolidadoPorSubcategoria.values());

		agregaMontosFaltantes(conn, dataList, listCuentas, strOrgFamily, adUserClient, fechaIniAnioAnterior,
				fechaIniAnioActual, fechaFinAnioAnterior, fechaFinAnioActual);

		dataList = agregaGrupoCategoriaComoLinea(dataList);

		Collections.sort(dataList, new Comparator<ReporteEstadoFlujoEfectivoData>() {
			@Override
			public int compare(ReporteEstadoFlujoEfectivoData p1, ReporteEstadoFlujoEfectivoData p2) {

				int resultado = new Integer(p1.order1).compareTo(new Integer(p2.order1));
				if (resultado != 0) {
					return resultado;
				}

				resultado = new Integer(p1.order2).compareTo(new Integer(p2.order2));
				if (resultado != 0) {
					return resultado;
				}
				return resultado;
			}
		});

		agregaMontosTotalesPorGrupos(dataList);

		dataFinal = dataList.toArray(new ReporteEstadoFlujoEfectivoData[dataList.size()]);

		return dataFinal;
	}

	private void printPagePDF(HttpServletRequest request, HttpServletResponse response, VariablesSecureApp vars,
			String strDateAnioAnterior, String strDateAnioActual, String strOrg, String strTable, String strRecord,
			String strSoloUnAnio) throws IOException, ServletException {

		String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
		String strOrgFamily = getFamily(this, strTreeOrg, strOrg);
		String strCliente = Utility.getContext(this, vars, "#User_Client", "ReporteEstadoFlujoEfectivo");

		String[] fechaAnioAnterior = strDateAnioAnterior.split("-");
		String[] fechaAnioActual = strDateAnioActual.split("-");

		String dia_comun = fechaAnioActual[0];
		String mes_comun = nombreMes(fechaAnioActual[1]);

		Integer anioAnterior = new Integer(fechaAnioAnterior[2]);
		Integer anioActual = new Integer(fechaAnioActual[2]);

		ReporteEstadoFlujoEfectivoData[] dataFinal = getEstadoFlujoEfectivoData(this, vars, strDateAnioAnterior,
				strDateAnioActual, strCliente, strOrg);

		String strOutput;
		String strReportName;
		if (vars.commandIn("PDF")) {
			strOutput = "pdf";
			strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteEstadoFlujoEfectivo.jrxml";

		} else {
			strOutput = "xls";
			strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteEstadoFlujoEfectivoExcel.jrxml";
		}

		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("Ruc", ReporteEstadoFlujoEfectivoData.selectRucOrg(this, strOrg));
		parameters.put("organizacion", ReporteEstadoFlujoEfectivoData.selectSocialName(this, strOrg));

		parameters.put("soloUnAnio", strSoloUnAnio);

		parameters.put("dateFrom", strDateAnioAnterior);
		parameters.put("dateTo", strDateAnioActual);

		parameters.put("anio1", anioAnterior.toString());
		parameters.put("anio2", anioActual.toString());

		parameters.put("CLIENTE", strCliente);
		parameters.put("ORGANIZACION", strOrgFamily);

		parameters.put("DIA_COMUN", dia_comun);
		parameters.put("MES_COMUN", mes_comun);

		renderJR(vars, response, strReportName, "Reporte_Estado_de_Flujo_Efectivo", strOutput, parameters, dataFinal,
				null);
	}

	private static void agregaMontosTotalesPorGrupos(ArrayList<ReporteEstadoFlujoEfectivoData> dataList) {

		// AGREGANDO MONTOS TOTALES SEGUN AGRUPACION

		HashMap<String, ArrayList<String>> grupoOrdenAgrupacion = new HashMap<String, ArrayList<String>>();
		HashMap<String, BigDecimal> grupoTotalOrderCateroriaAnterior = new HashMap<String, BigDecimal>();
		HashMap<String, BigDecimal> grupoTotalOrderCateroriaActual = new HashMap<String, BigDecimal>();

		cargaConfiguracionAgrupacion(grupoOrdenAgrupacion);

		// CONSOLIDANDO TOTALES POR EL ORDER DE CATEGORIA
		for (int i = 0; i < dataList.size(); i++) {

			BigDecimal sumaAnterior = new BigDecimal(dataList.get(i).saldoAnioAnterior);
			BigDecimal sumaActual = new BigDecimal(dataList.get(i).saldoAnioActual);

			String key = dataList.get(i).order1;

			if (grupoTotalOrderCateroriaAnterior.containsKey(key)) {
				sumaAnterior = sumaAnterior.add(grupoTotalOrderCateroriaAnterior.get(key));
				sumaActual = sumaActual.add(grupoTotalOrderCateroriaActual.get(key));
			}
			grupoTotalOrderCateroriaAnterior.put(key, sumaAnterior);
			grupoTotalOrderCateroriaActual.put(key, sumaActual);
		}

		// AGREGANDO LOS TOTALES DE ACUERDO A LA CONFIGURACION DE AGRUPACION
		for (int i = 0; i < dataList.size(); i++) {

			String key = dataList.get(i).order1;
			String tipo = dataList.get(i).tipo;
			if (grupoOrdenAgrupacion.containsKey(key) && tipo.equals("total")) {

				BigDecimal sumaAnterior = BigDecimal.ZERO;
				BigDecimal sumaActual = BigDecimal.ZERO;

				for (int j = 0; j < grupoOrdenAgrupacion.get(key).size(); j++) {
					String key2 = grupoOrdenAgrupacion.get(key).get(j);
					sumaAnterior = sumaAnterior.add(grupoTotalOrderCateroriaAnterior.get(key2));
					sumaActual = sumaActual.add(grupoTotalOrderCateroriaActual.get(key2));
				}

				sumaAnterior = sumaAnterior.add(grupoTotalOrderCateroriaAnterior.get(key));
				sumaActual = sumaActual.add(grupoTotalOrderCateroriaActual.get(key));

				grupoTotalOrderCateroriaAnterior.put(key, sumaAnterior);
				grupoTotalOrderCateroriaActual.put(key, sumaActual);

				dataList.get(i).saldoAnioAnterior = sumaAnterior.toString();
				dataList.get(i).saldoAnioActual = sumaActual.toString();
			}
		}
	}

	private static void agregaMontosFaltantes(ConnectionProvider conn,
			ArrayList<ReporteEstadoFlujoEfectivoData> dataList, ArrayList<String> listLikeCuentas, String strOrgFamily,
			String strCliente, String fechaIniAnioAnterior, String fechaIniAnioActual, String fechaFinAnioAnterior,
			String fechaFinAnioActual) throws ServletException {

		String likeCuentas = "";
		for (int i = 0; i < listLikeCuentas.size(); i++) {
			likeCuentas += "'" + listLikeCuentas.get(i) + "'" + ",";
		}

		if (likeCuentas.length() > 0)
			likeCuentas = likeCuentas.substring(0, likeCuentas.length() - 1);

		String otrosMontosAnioAnterior = "0", montoInicialAnioAnterior = "0", otrosMontosAnioActual = "0",
				montoInicialAnioActual;
		// Anio Anterior
		if (fechaIniAnioAnterior != null && !"".equals(fechaIniAnioAnterior.trim())) {
//			otrosMontosAnioAnterior = ReporteEstadoFlujoEfectivoData.select_facts_sin_flujo(conn, strOrgFamily,
//					strCliente, fechaIniAnioAnterior, fechaFinAnioAnterior, likeCuentas);

			montoInicialAnioAnterior = ReporteEstadoFlujoEfectivoData.select_facts_inicio(conn, strOrgFamily,
					strCliente, fechaIniAnioAnterior);
		}
		// Anio Actual
//		otrosMontosAnioActual = ReporteEstadoFlujoEfectivoData.select_facts_sin_flujo(conn, strOrgFamily, strCliente,
//				fechaIniAnioActual, fechaFinAnioActual, likeCuentas);

		montoInicialAnioActual = ReporteEstadoFlujoEfectivoData.select_facts_inicio(conn, strOrgFamily, strCliente,
				fechaIniAnioActual);

		for (int i = 0; i < dataList.size(); i++) {

			if (dataList.get(i).order1.equals("18")) {
				// Anio Anterior
				if (fechaIniAnioAnterior != null && !"".equals(fechaIniAnioAnterior.trim())) {
					dataList.get(i).saldoAnioAnterior = montoInicialAnioAnterior;
				}
				// Anio Actual
				dataList.get(i).saldoAnioActual = montoInicialAnioActual;
			}

			if (dataList.get(i).order1.equals("5") && dataList.get(i).order2.equals("60")) {
				// Anio Anterior
				if (fechaIniAnioAnterior != null && !"".equals(fechaIniAnioAnterior.trim())) {
					dataList.get(i).saldoAnioAnterior = new BigDecimal(dataList.get(i).saldoAnioAnterior)
							.add(new BigDecimal(otrosMontosAnioAnterior)).toString();
				}
				// Anio Actual
				dataList.get(i).saldoAnioActual = new BigDecimal(dataList.get(i).saldoAnioActual)
						.add(new BigDecimal(otrosMontosAnioActual)).toString();
			}
		}
	}

	private static ArrayList<ReporteEstadoFlujoEfectivoData> agregaGrupoCategoriaComoLinea(
			ArrayList<ReporteEstadoFlujoEfectivoData> dataList) {

		// SE TRATA DE SETEAR LA EL CAMPO SUBCATEGORIA , EL CUAL ES EL QUE SE VA
		// MOSTRAR EN LE REPORTE
		ArrayList<ReporteEstadoFlujoEfectivoData> listFinal = new ArrayList<ReporteEstadoFlujoEfectivoData>();
		HashMap<String, ArrayList<ReporteEstadoFlujoEfectivoData>> grupoPorOrderCategoria = new HashMap<String, ArrayList<ReporteEstadoFlujoEfectivoData>>();
		ArrayList<ReporteEstadoFlujoEfectivoData> list = new ArrayList<ReporteEstadoFlujoEfectivoData>();

		for (int i = 0; i < dataList.size(); i++) {

			list = new ArrayList<ReporteEstadoFlujoEfectivoData>();
			String key = dataList.get(i).order1;

			if (grupoPorOrderCategoria.containsKey(key)) {
				list = grupoPorOrderCategoria.get(key);
			}
			list.add(dataList.get(i));
			grupoPorOrderCategoria.put(key, list);
		}

		ArrayList<String> listOrder = new ArrayList<String>(grupoPorOrderCategoria.keySet());

		for (int i = 0; i < listOrder.size(); i++) {
			list = grupoPorOrderCategoria.get(listOrder.get(i));

			ReporteEstadoFlujoEfectivoData muestra = list.get(0);

			if (!muestra.subCategoria.equals("") && !muestra.categoria.equals("--")) {

				ReporteEstadoFlujoEfectivoData nuevo = new ReporteEstadoFlujoEfectivoData();
				nuevo.order1 = muestra.order1;
				nuevo.order2 = "-1";
				nuevo.categoria = muestra.categoria;
				nuevo.subCategoria = muestra.categoria;
				nuevo.saldoAnioAnterior = "0.00";
				nuevo.saldoAnioActual = "0.00";
				nuevo.tipo = "titulo";
				nuevo.categoriaSunatCode = muestra.categoriaSunatCode;

				listFinal.add(nuevo);
			}
			if (!muestra.categoria.equals("--") && list.size() == 1) {
				muestra.subCategoria = muestra.categoria;
			}
			listFinal.addAll(list);
		}
		return listFinal;
	}

	private static void cargaConfiguracionAgrupacion(HashMap<String, ArrayList<String>> grupo) {

		ArrayList<String> lista = new ArrayList<String>();
		lista.add("2");
		lista.add("3");
		grupo.put("4", lista);

		lista = new ArrayList<String>();
		lista.add("4");
		lista.add("5");
		grupo.put("6", lista);

		lista = new ArrayList<String>();
		lista.add("8");
		lista.add("9");
		grupo.put("10", lista);

		lista = new ArrayList<String>();
		lista.add("12");
		lista.add("13");
		grupo.put("14", lista);

		lista = new ArrayList<String>();
		lista.add("6");
		lista.add("10");
		lista.add("14");
		grupo.put("15", lista);

		lista = new ArrayList<String>();
		lista.add("15");
		lista.add("16");
		grupo.put("17", lista);

		lista = new ArrayList<String>();
		lista.add("17");
		lista.add("18");
		grupo.put("19", lista);
	}

	private static BigDecimal getMontoxCuenta(ArrayList<ReporteEstadoFlujoEfectivoData> dataCuentas, String cuenta,
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

	private static String getFamily(ConnectionProvider conn, String strTree, String strChild)
			throws IOException, ServletException {
		return Tree.getMembers(conn, strTree, (strChild == null || strChild.equals("")) ? "0" : strChild) + ",'0'";
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

		String[] meses = { "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre",
				"Octubre", "Noviembre", "Diciembre" };
		String retornaMes = "";

		retornaMes = meses[mesInt - 1];
		return retornaMes;

	}

	public static StructPle getPLE(ConnectionProvider conn, VariablesSecureApp vars, String strDateFrom,
			String strDateTo, String adUserClient, String strOrg) throws Exception {
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

		ReporteEstadoFlujoEfectivoData[] dataFinal = getEstadoFlujoEfectivoData(conn, vars, null, sdf.format(dateTo),
				adUserClient, strOrg);

		System.out.println("datafinal length:" + dataFinal.length);

		StructPle sunatPle = getStringData(dataFinal, dateFrom, dateTo, strOrg);

		return sunatPle;
	}

	private static StructPle getStringData(ReporteEstadoFlujoEfectivoData[] data, Date dateFrom, Date dateTo,
			String strOrg) {
		DecimalFormat df = new DecimalFormat("0.00");
		df.setRoundingMode(RoundingMode.HALF_UP);

		StructPle sunatPle = new StructPle();
		sunatPle.numEntries = 0;

		StringBuffer sb = new StringBuffer();

		Organization org = OBDal.getInstance().get(Organization.class, strOrg);
		String rucAdq = SunatUtil.LPad(org.getOrganizationInformationList().get(0).getTaxID(), 11, '0');

		SimpleDateFormat dt = new SimpleDateFormat("yyyyMMdd");

		String filename = "LE" + rucAdq + dt.format(dateTo) + "031800011111.TXT"; // LERRRRRRRRRRRAAAAMMDD031800CCOIM1.TXT

		String periodoTrib = dt.format(dateTo);
		String codigoCatalago = "01";
		String estadoOp = "1";

		int jj = 0;
		for (int i = 0; i < data.length; i++) {
			ReporteEstadoFlujoEfectivoData led = data[i];

			if ((led.categoriaSunatCode == null || "".equals(led.categoriaSunatCode))
					&& (led.subCategoriaSunatCode == null || "".equals(led.subCategoriaSunatCode)))
				continue;

			String codigoRubro = led.subCategoriaSunatCode;
			if (led.categoriaSunatCode != null && led.categoriaSunatCode.trim().compareTo("") != 0) {
				codigoRubro = led.categoriaSunatCode;
			}

			String saldoRubro = led.saldoAnioActual;

			String linea = periodoTrib + "|" + codigoCatalago + "|" + codigoRubro + "|" + saldoRubro + "|" + estadoOp
					+ "|";

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
		return "Servlet ReporteEstadoFlujoEfectivo. This Servlet was made by Pablo Sarobe modified by everybody";
	} // end of getServletInfo() method
}
