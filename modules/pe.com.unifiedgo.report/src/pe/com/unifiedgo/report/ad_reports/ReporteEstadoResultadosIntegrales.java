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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Calendar;
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

public class ReporteEstadoResultadosIntegrales extends
		HttpSecureAppServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		VariablesSecureApp vars = new VariablesSecureApp(request);

		if (log4j.isDebugEnabled())
			log4j.debug("Command: " + vars.getStringParameter("Command"));

		if (vars.commandIn("DEFAULT")) {
			
		      Calendar fActual = new GregorianCalendar();

		      String fechaFinAnioAnterior = "31-12-" + (fActual.get(Calendar.YEAR) - 1);
		      String fechaFinAnioActual = "31-12-" + (fActual.get(Calendar.YEAR));

		      String strDateFrom = vars.getGlobalVariable("inpDateFrom",
		          "ReporteEstadoResultadosIntegrales|DateFrom", fechaFinAnioAnterior);
		      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReporteEstadoResultadosIntegrales|DateTo",
		          fechaFinAnioActual);
			

			String strOrg = vars.getGlobalVariable("inpOrg",
					"ReporteEstadoResultadosIntegrales|Org", "0");
			String strRecord = vars.getGlobalVariable("inpRecord",
					"ReporteEstadoResultadosIntegrales|Record", "");
			String strTable = vars.getGlobalVariable("inpTable",
					"ReporteEstadoResultadosIntegrales|Table", "");
			printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg,
					strTable, strRecord);

		} else if (vars.commandIn("DIRECT")) {
			String strTable = vars.getGlobalVariable("inpTable",
					"ReporteEstadoResultadosIntegrales|Table");
			String strRecord = vars.getGlobalVariable("inpRecord",
					"ReporteEstadoResultadosIntegrales|Record");

			setHistoryCommand(request, "DIRECT");
			vars.setSessionValue(
					"ReporteEstadoResultadosIntegrales.initRecordNumber",
					"0");
			printPageDataSheet(response, vars, "", "", "", strTable, strRecord);

		} else if (vars.commandIn("FIND")) {
			String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
					"ReporteEstadoResultadosIntegrales|DateFrom");
			String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
					"ReporteEstadoResultadosIntegrales|DateTo");
			String strOrg = vars.getGlobalVariable("inpOrg",
					"ReporteEstadoResultadosIntegrales|Org", "0");
			vars.setSessionValue(
					"ReporteEstadoResultadosIntegrales.initRecordNumber",
					"0");
			setHistoryCommand(request, "DEFAULT");
			printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg,
					"", "");

		} else if (vars.commandIn("PDF", "XLS")) {
			if (log4j.isDebugEnabled())
				log4j.debug("PDF");
			String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
					"ReporteEstadoResultadosIntegrales|DateFrom");
			String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
					"ReporteEstadoResultadosIntegrales|DateTo");
			String strOrg = vars.getGlobalVariable("inpOrg",
					"ReporteEstadoResultadosIntegrales|Org", "0");
			String strSoloUnMes = vars.getStringParameter("inpSoloUnMes");

			String strTable = vars.getStringParameter("inpTable");
			String strRecord = vars.getStringParameter("inpRecord");
			setHistoryCommand(request, "DEFAULT");
			printPagePDF(request,response, vars, strDateFrom, strDateTo, strOrg,
					strTable, strRecord,strSoloUnMes);

		} else
			pageError(response);
	}

	private void printPageDataSheet(HttpServletResponse response,
			VariablesSecureApp vars, String strDateFrom, String strDateTo,
			String strOrg, String strTable, String strRecord)
			throws IOException, ServletException {
		if (log4j.isDebugEnabled())
			log4j.debug("Output: dataSheet");
		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();
		XmlDocument xmlDocument = null;
		ReporteEstadoResultadosIntegralesData[] data = null;
		String strPosition = "0";
		ToolBar toolbar = new ToolBar(this, vars.getLanguage(),
				"ReporteEstadoResultadosIntegrales", false, "", "",
				"imprimir();return false;", false, "ad_reports",
				strReplaceWith, false, true);
		toolbar.setEmail(false);
		
		toolbar.prepareSimpleToolBarTemplate();
		toolbar.prepareRelationBarTemplate(false, false,
				"imprimirXLS();return false;");
		
		if (data == null || data.length == 0) {
			String discard[] = { "secTable" };
			xmlDocument = xmlEngine
					.readXmlTemplate(
							"pe/com/unifiedgo/report/ad_reports/ReporteEstadoResultadosIntegrales",
							discard).createXmlDocument();
			data = new ReporteEstadoResultadosIntegralesData[1] ;
			data[0] = new ReporteEstadoResultadosIntegralesData();
			data[0].rownum = "0";
		}
		xmlDocument.setParameter("toolbar", toolbar.toString());
		try {
			WindowTabs tabs = new WindowTabs(this, vars,
					"pe.com.unifiedgo.report.ad_reports.ReporteEstadoResultadosIntegrales");
			xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
			xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
			xmlDocument.setParameter("childTabContainer", tabs.childTabs());
			xmlDocument.setParameter("theme", vars.getTheme());
			NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
					"ReporteEstadoResultadosIntegrales.html",
					classInfo.id, classInfo.type, strReplaceWith,
					tabs.breadcrumb());
			xmlDocument.setParameter("navigationBar", nav.toString());
			LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
					"ReporteEstadoResultadosIntegrales.html",
					strReplaceWith);
			xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
		} catch (Exception ex) {
			throw new ServletException(ex);
		}
		{
			OBError myMessage = vars
					.getMessage("ReporteEstadoResultadosIntegrales");
			vars.removeMessage("ReporteEstadoResultadosIntegrales");
			if (myMessage != null) {
				xmlDocument.setParameter("messageType", myMessage.getType());
				xmlDocument.setParameter("messageTitle", myMessage.getTitle());
				xmlDocument.setParameter("messageMessage",
						myMessage.getMessage());
			}
		}

		xmlDocument
				.setParameter("calendar", vars.getLanguage().substring(0, 2));

		try {
			ComboTableData comboTableData = new ComboTableData(vars, this,
					"TABLEDIR", "AD_ORG_ID", "", "", Utility.getContext(this,
							vars, "#AccessibleOrgTree",
							"ReporteEstadoResultadosIntegrales"),
					Utility.getContext(this, vars, "#User_Client",
							"ReporteEstadoResultadosIntegrales"), '*');
			comboTableData.fillParameters(null,
					"ReporteEstadoResultadosIntegrales", "");
			xmlDocument.setData("reportAD_ORGID", "liststructure",
					comboTableData.select(false));
		} catch (Exception ex) {
			throw new ServletException(ex);
		}

		xmlDocument
				.setData("reportC_ACCTSCHEMA_ID", "liststructure",
						AccountingSchemaMiscData.selectC_ACCTSCHEMA_ID(this,
								Utility.getContext(this, vars,
										"#AccessibleOrgTree",
										"ReportGeneralLedger"), Utility
										.getContext(this, vars, "#User_Client",
												"ReportGeneralLedger"), ""));
		xmlDocument.setParameter("directory", "var baseDirectory = \""
				+ strReplaceWith + "/\";\n");
		xmlDocument.setParameter("paramLanguage",
				"defaultLang=\"" + vars.getLanguage() + "\";");
		xmlDocument.setParameter("dateFrom", strDateFrom);
		xmlDocument.setParameter("dateFromdisplayFormat",
				vars.getSessionValue("#AD_SqlDateFormat"));
		xmlDocument.setParameter("dateFromsaveFormat",
				vars.getSessionValue("#AD_SqlDateFormat"));
		xmlDocument.setParameter("dateTo", strDateTo);
		xmlDocument.setParameter("dateTodisplayFormat",
				vars.getSessionValue("#AD_SqlDateFormat"));
		xmlDocument.setParameter("dateTosaveFormat",
				vars.getSessionValue("#AD_SqlDateFormat"));
		xmlDocument.setParameter("adOrgId", strOrg);
		xmlDocument.setParameter("groupId", strPosition);
		xmlDocument.setParameter("paramRecord", strRecord);
		xmlDocument.setParameter("paramTable", strTable);
		xmlDocument.setParameter("paramPeriodosArray", Utility.arrayInfinitasEntradas("idperiodo;periodo;fechainicial;fechafinal;idorganizacion","arrPeriodos",
	  			ReporteEstadoResultadosIntegralesData
				.select_periodos(this)));
		vars.setSessionValue(
				"ReporteEstadoResultadosIntegrales|Record", strRecord);
		vars.setSessionValue(
				"ReporteEstadoResultadosIntegrales|Table", strTable);

		xmlDocument.setData("structure1", data);
		out.println(xmlDocument.print());
		out.close();
	}



	private void printPagePDF(HttpServletRequest request,HttpServletResponse response,
			VariablesSecureApp vars, String strDateFrom, String strDateTo,
			String strOrg, String strTable, String strRecord,String strSoloUnAnio)
			throws IOException, ServletException {

		    ReporteEstadoResultadosIntegralesData[] dataCategorias = null;
		    ReporteEstadoResultadosIntegralesData[] dataFactsAnterior = null;
		    ReporteEstadoResultadosIntegralesData[] dataFactsActual = null;

		    ArrayList<ReporteEstadoResultadosIntegralesData> dataList = new ArrayList<ReporteEstadoResultadosIntegralesData>();
		    HashMap<String, ArrayList<ReporteEstadoResultadosIntegralesData>> grupoCuentasActuales = new HashMap<String, ArrayList<ReporteEstadoResultadosIntegralesData>>();
		    HashMap<String, ArrayList<ReporteEstadoResultadosIntegralesData>> grupoCuentaAnterior = new HashMap<String, ArrayList<ReporteEstadoResultadosIntegralesData>>();

		    ReporteEstadoResultadosIntegralesData[] dataFinal = null;

		    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
		    String strOrgFamily = getFamily(this, strTreeOrg, strOrg);
		    String strCliente = Utility.getContext(this, vars, "#User_Client",
		        "ReporteEstadoResultadosIntegrales");

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

		    dataCategorias = ReporteEstadoResultadosIntegralesData.select_categorias(this, 
		         strCliente, strOrgFamily);

		    dataFactsAnterior = ReporteEstadoResultadosIntegralesData.select_facts(this, strOrgFamily,
		        strCliente, fechaIniAnioAnterior, fechaFinAnioAnterior);

		    dataFactsActual = ReporteEstadoResultadosIntegralesData.select_facts(this, strOrgFamily,
		        strCliente, fechaIniAnioActual, fechaFinAnioActual);

		    String cuenta = "";

		    for (int i = 0; i < dataCategorias.length; i++) {
		      ReporteEstadoResultadosIntegralesData now = dataCategorias[i];
		      ArrayList<ReporteEstadoResultadosIntegralesData> lista = new ArrayList<ReporteEstadoResultadosIntegralesData>();

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

		      lista = new ArrayList<ReporteEstadoResultadosIntegralesData>();
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

		      ReporteEstadoResultadosIntegralesData now = dataCategorias[i];

		      cuenta = now.acctvalue.replace("-", "").replace("%", "");
		      String key = now.order1 + "//" + now.order2 + "//" + cuenta;

		      now.saldoAnioAnterior = getMontoxCuenta(grupoCuentaAnterior.get(key), now.acctvalue,
		          now.consider, now.considerwhen, now.greaterThanAYear).toString();
		      now.saldoAnioActual = getMontoxCuenta(grupoCuentasActuales.get(key), now.acctvalue,
		          now.consider, now.considerwhen, now.greaterThanAYear).toString();
		    }

		    BigDecimal sumaSaldoAnterior = BigDecimal.ZERO;
		    BigDecimal sumaSaldoActual = BigDecimal.ZERO;

		    for (int i = 0; i < dataCategorias.length; i++) {

		      ReporteEstadoResultadosIntegralesData now = dataCategorias[i];
		      ReporteEstadoResultadosIntegralesData next = (i + 1 == dataCategorias.length) ? new ReporteEstadoResultadosIntegralesData()
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
		    
		    dataFinal = dataList.toArray(new ReporteEstadoResultadosIntegralesData[dataList.size()]);

		    String strOutput;
		    String strReportName;
		    if (vars.commandIn("PDF")) {
		      strOutput = "pdf";
		      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteEstadoResultadosIntegrales.jrxml";

		    } else {
		      strOutput = "xls";
		      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteEstadoResultadosIntegralesExcel.jrxml";
		    }

		    HashMap<String, Object> parameters = new HashMap<String, Object>();
		    parameters.put("Ruc", ReporteEstadoResultadosIntegralesData.selectRucOrg(this, strOrg));
		    parameters.put("organizacion",
		        ReporteEstadoResultadosIntegralesData.selectSocialName(this, strOrg));

		    parameters.put("soloUnAnio", strSoloUnAnio);

		    parameters.put("dateFrom", strDateFrom);
		    parameters.put("dateTo", strDateTo);

		    parameters.put("anio1", anio1);
		    parameters.put("anio2", anio2);
		    
		    parameters.put("CLIENTE", strCliente);
		    parameters.put("ORGANIZACION", strOrgFamily);

		    parameters.put("DIA_COMUN", dia_comun);
		    parameters.put("MES_COMUN", mes_comun);

		    renderJR(vars, response, strReportName, "Reporte_Estado_de_Resultados_Integrales", strOutput,
		        parameters, dataFinal, null);
	}
	
	
	
	 public static StructPle getEstadoResultadosIntegralesToPLE(ConnectionProvider connp,
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

		    ReporteEstadoResultadosIntegralesData[] data = null, tmpData = null;
		    
		    
		    ReporteEstadoResultadosIntegralesData[] dataCategorias = null;
		    ReporteEstadoResultadosIntegralesData[] dataFactsAnterior = null;
		    ReporteEstadoResultadosIntegralesData[] dataFactsActual = null;
		    
		    ArrayList<ReporteEstadoResultadosIntegralesData> dataList = new ArrayList<ReporteEstadoResultadosIntegralesData>();
		    HashMap<String, ArrayList<ReporteEstadoResultadosIntegralesData>> grupoCuentasActuales = new HashMap<String, ArrayList<ReporteEstadoResultadosIntegralesData>>();
		    HashMap<String, ArrayList<ReporteEstadoResultadosIntegralesData>> grupoCuentaAnterior = new HashMap<String, ArrayList<ReporteEstadoResultadosIntegralesData>>();

		    ReporteEstadoResultadosIntegralesData[] dataFinal = null;

		    
		    dataCategorias = ReporteEstadoResultadosIntegralesData.select_categorias(connp, 
		    		strClient, strOrgFamily);
		    
		    dataFactsAnterior = ReporteEstadoResultadosIntegralesData.select_facts(connp, strOrgFamily,
		    		strClient, fechaIniAnioAnterior, fechaFinAnioAnterior);

			dataFactsActual = ReporteEstadoResultadosIntegralesData.select_facts(connp, strOrgFamily,
					strClient, fechaIniAnioActual, fechaFinAnioActual);
		    
			String cuenta = "";
			for (int i = 0; i < dataCategorias.length; i++) {
			      ReporteEstadoResultadosIntegralesData now = dataCategorias[i];
			      ArrayList<ReporteEstadoResultadosIntegralesData> lista = new ArrayList<ReporteEstadoResultadosIntegralesData>();

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

			      lista = new ArrayList<ReporteEstadoResultadosIntegralesData>();
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

			      ReporteEstadoResultadosIntegralesData now = dataCategorias[i];

			      cuenta = now.acctvalue.replace("-", "").replace("%", "");
			      String key = now.order1 + "//" + now.order2 + "//" + cuenta;

			      now.saldoAnioAnterior = getMontoxCuenta(grupoCuentaAnterior.get(key), now.acctvalue,
			          now.consider, now.considerwhen, now.greaterThanAYear).toString();
			      now.saldoAnioActual = getMontoxCuenta(grupoCuentasActuales.get(key), now.acctvalue,
			          now.consider, now.considerwhen, now.greaterThanAYear).toString();
			}

			    BigDecimal sumaSaldoAnterior = BigDecimal.ZERO;
			    BigDecimal sumaSaldoActual = BigDecimal.ZERO;
			
			for (int i = 0; i < dataCategorias.length; i++) {

				      ReporteEstadoResultadosIntegralesData now = dataCategorias[i];
				      ReporteEstadoResultadosIntegralesData next = (i + 1 == dataCategorias.length) ? new ReporteEstadoResultadosIntegralesData()
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
				    
			   dataFinal = dataList.toArray(new ReporteEstadoResultadosIntegralesData[dataList.size()]);

			
		    
		    // --GET DATA--
		   /* tmpData = ReporteEstadoResultadosIntegralesData.select(connp, strOrgFamily, fechaIniAnioAnterior,
		        fechaFinAnioAnterior, strOrgFamily, fechaIniAnioAnterior, fechaFinAnioAnterior,
		        strOrgFamily, fechaIniAnioAnterior, fechaFinAnioAnterior, strOrgFamily, fechaIniAnioActual,
		        fechaFinAnioActual, strOrgFamily, fechaIniAnioActual, fechaFinAnioActual, strOrgFamily,
		        fechaIniAnioActual, fechaFinAnioActual, strClient, strOrgFamily);*/

		    // dataFinal;
		    if (dataFinal != null && dataFinal.length != 0) {
		      data = getFormattedDataForPLE(org.getId(), fechaIniAnioActual, fechaFinAnioActual, dataFinal);
		    }

		    for (int i = 0; i < data.length; i++) {

		      ReporteEstadoResultadosIntegralesData info = data[i];

		      String strSunatCode = info.subCategoriaSunatCode;
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

		    String filename = "LE" + rucAdq + dt.format(dateTo) + "032400011111.TXT"; // LERRRRRRRRRRRAAAAMMDD030700CCOIM1.TXT

		    sunatPle.filename = filename;

		    return sunatPle;
		  }
	 
	 private static ReporteEstadoResultadosIntegralesData[] getFormattedDataForPLE(String strOrg,
		      String strDateFrom, String strDateTo, ReporteEstadoResultadosIntegralesData[] dataAll) {
		    DecimalFormat df = new DecimalFormat("0.00");
		    df.setRoundingMode(RoundingMode.HALF_UP);
		    
		    String prev_categoria = "";
		    String sunatcode = "";
		    String sunatcodecat = "";
		    String pre_sunatcode = "";
		    String pre_sunatcodecat = "";
		    boolean isresumen= true;

		    //Begin clean Data
		    Vector<java.lang.Object> vectorData = new Vector<java.lang.Object>(0);
		    int countRecord = 0;
		    for (int i = 0; i < dataAll.length; i++) {
		    	 countRecord++;
		    	 sunatcode = dataAll[i].categoriaSunatCode;
			     sunatcodecat = dataAll[i].subCategoriaSunatCode;
			      
			     if((sunatcode == null || sunatcode.equals(""))&&(sunatcodecat == null || sunatcodecat.equals("")))
			    	  continue;
			     
			     dataAll[i].rownum = Long.toString(countRecord);
			     vectorData.addElement(dataAll[i]);
		    }
		    
		    ReporteEstadoResultadosIntegralesData data[] = new ReporteEstadoResultadosIntegralesData[vectorData.size()];
            vectorData.copyInto(data);
            //finish Clead Data
            
		    
		    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
		    countRecord = 0;
		    BigDecimal saldoAnioAnterior = BigDecimal.ZERO, saldoAnioActual = BigDecimal.ZERO;
		    
		    for (int i = 0; i < data.length; i++) {
		      countRecord++;
		      
		      sunatcode = data[i].categoriaSunatCode;
		      sunatcodecat = data[i].subCategoriaSunatCode;
		      
		      if((sunatcode == null || sunatcode.equals(""))&&(sunatcodecat == null || sunatcodecat.equals("")))
		    	  continue;
		      
		      if (i == 0) {
		        prev_categoria = data[i].categoria;

		      } else if (prev_categoria.compareTo(data[i].categoria) != 0 && isresumen) {
		    	  
		        ReporteEstadoResultadosIntegralesData objRptEstadoResultadosIntegralesData = new ReporteEstadoResultadosIntegralesData();
		        objRptEstadoResultadosIntegralesData.order1 = data[i - 1].order1;
		        objRptEstadoResultadosIntegralesData.order2 = data[i - 1].order2;
		        objRptEstadoResultadosIntegralesData.categoria = data[i - 1].categoria;
		        objRptEstadoResultadosIntegralesData.subCategoriaId = "--";
		        objRptEstadoResultadosIntegralesData.subCategoria = data[i - 1].categoria;
		        objRptEstadoResultadosIntegralesData.grupoCategoria = data[i - 1].grupoCategoria;
		        objRptEstadoResultadosIntegralesData.idorganizacion = data[i - 1].idorganizacion;
		        objRptEstadoResultadosIntegralesData.idperiodo = data[i - 1].idperiodo;
		        objRptEstadoResultadosIntegralesData.periodo = data[i - 1].periodo;
		        objRptEstadoResultadosIntegralesData.fechainicial = data[i - 1].fechainicial;
		        objRptEstadoResultadosIntegralesData.fechafinal = data[i - 1].fechafinal;
		        objRptEstadoResultadosIntegralesData.saldoAnioAnterior = df.format(saldoAnioAnterior);
		        objRptEstadoResultadosIntegralesData.saldoAnioActual = df.format(saldoAnioActual);
		        objRptEstadoResultadosIntegralesData.categoriaSunatCode = data[i - 1].categoriaSunatCode;
		        objRptEstadoResultadosIntegralesData.subCategoriaSunatCode = data[i - 1].categoriaSunatCode;
		        objRptEstadoResultadosIntegralesData.rownum = Long.toString(countRecord);

		        objRptEstadoResultadosIntegralesData.paramorgid = strOrg;
		        objRptEstadoResultadosIntegralesData.paramdatefrom = strDateFrom;
		        objRptEstadoResultadosIntegralesData.paramdateto = strDateTo;

		        vector.addElement(objRptEstadoResultadosIntegralesData);
	       
		        countRecord++;
		      }
		      
		      prev_categoria = data[i].categoria;
		      
		      ReporteEstadoResultadosIntegralesData objRptEstadoResultadosIntegralesData = new ReporteEstadoResultadosIntegralesData();
		      objRptEstadoResultadosIntegralesData.order1 = data[i].order1;
		      objRptEstadoResultadosIntegralesData.order2 = data[i].order2;
		      objRptEstadoResultadosIntegralesData.categoria = data[i].categoria;
		      objRptEstadoResultadosIntegralesData.subCategoriaId = data[i].subCategoriaId;
		      objRptEstadoResultadosIntegralesData.subCategoria = data[i].subCategoria;
		      objRptEstadoResultadosIntegralesData.grupoCategoria = data[i].grupoCategoria;
		      objRptEstadoResultadosIntegralesData.idorganizacion = data[i].idorganizacion;
		      objRptEstadoResultadosIntegralesData.idperiodo = data[i].idperiodo;
		      objRptEstadoResultadosIntegralesData.periodo = data[i].periodo;
		      objRptEstadoResultadosIntegralesData.fechainicial = data[i].fechainicial;
		      objRptEstadoResultadosIntegralesData.fechafinal = data[i].fechafinal;
		      objRptEstadoResultadosIntegralesData.saldoAnioAnterior = df.format(new BigDecimal(data[i].saldoAnioAnterior));
		      objRptEstadoResultadosIntegralesData.saldoAnioActual = df.format(new BigDecimal(data[i].saldoAnioActual));
		      objRptEstadoResultadosIntegralesData.categoriaSunatCode = data[i].categoriaSunatCode;
		      objRptEstadoResultadosIntegralesData.subCategoriaSunatCode = data[i].subCategoriaSunatCode;
		      objRptEstadoResultadosIntegralesData.rownum = Long.toString(countRecord);

		      objRptEstadoResultadosIntegralesData.paramorgid = strOrg;
		      objRptEstadoResultadosIntegralesData.paramdatefrom = strDateFrom;
		      objRptEstadoResultadosIntegralesData.paramdateto = strDateTo;
		      
		      if (objRptEstadoResultadosIntegralesData.subCategoriaSunatCode != null
		          && !objRptEstadoResultadosIntegralesData.subCategoriaSunatCode.equals(""))
		        vector.addElement(objRptEstadoResultadosIntegralesData);
		      
		      
		      
		        pre_sunatcode = data[i].categoriaSunatCode;
			    pre_sunatcodecat = data[i].subCategoriaSunatCode;
			    
			    isresumen=true;
			    if(pre_sunatcode.equals(pre_sunatcodecat))
			    	isresumen=false;

		      saldoAnioAnterior = saldoAnioAnterior.add(new BigDecimal(
		    		  data[i].saldoAnioAnterior));
		      saldoAnioActual = saldoAnioActual
		          .add(new BigDecimal(data[i].saldoAnioActual));
		      
		    //   dataList.add(data[i]);
		    }

		    ReporteEstadoResultadosIntegralesData objRptEstadoResultadosIntegralesData = new ReporteEstadoResultadosIntegralesData();
		    objRptEstadoResultadosIntegralesData.order1 = data[data.length - 1].order1;
		    objRptEstadoResultadosIntegralesData.order2 = data[data.length - 1].order2;
		    objRptEstadoResultadosIntegralesData.categoria = data[data.length - 1].categoria;
		    objRptEstadoResultadosIntegralesData.subCategoriaId = "--";
		    objRptEstadoResultadosIntegralesData.subCategoria = data[data.length - 1].categoria;
		    objRptEstadoResultadosIntegralesData.grupoCategoria = data[data.length - 1].grupoCategoria;
		    objRptEstadoResultadosIntegralesData.idorganizacion = data[data.length - 1].idorganizacion;
		    objRptEstadoResultadosIntegralesData.idperiodo = data[data.length - 1].idperiodo;
		    objRptEstadoResultadosIntegralesData.periodo = data[data.length - 1].periodo;
		    objRptEstadoResultadosIntegralesData.fechainicial = data[data.length - 1].fechainicial;
		    objRptEstadoResultadosIntegralesData.fechafinal = data[data.length - 1].fechafinal;
		    objRptEstadoResultadosIntegralesData.saldoAnioAnterior = df.format(saldoAnioAnterior);
		    objRptEstadoResultadosIntegralesData.saldoAnioActual = df.format(saldoAnioActual);
		    objRptEstadoResultadosIntegralesData.categoriaSunatCode = data[data.length - 1].categoriaSunatCode;
		    objRptEstadoResultadosIntegralesData.subCategoriaSunatCode = data[data.length - 1].categoriaSunatCode;

		    objRptEstadoResultadosIntegralesData.rownum = Long.toString(countRecord);

		    objRptEstadoResultadosIntegralesData.paramorgid = strOrg;
		    objRptEstadoResultadosIntegralesData.paramdatefrom = strDateFrom;
		    objRptEstadoResultadosIntegralesData.paramdateto = strDateTo;

		    vector.addElement(objRptEstadoResultadosIntegralesData);

		    ReporteEstadoResultadosIntegralesData objectReporteEstadoResultadosIntegralesData[] = new ReporteEstadoResultadosIntegralesData[vector
		        .size()];
		    vector.copyInto(objectReporteEstadoResultadosIntegralesData);
		    
		    
		    //return dataList.toArray(new ReporteEstadoResultadosIntegralesData[dataList.size()]);
		    return objectReporteEstadoResultadosIntegralesData;
	 }
	
	 private static BigDecimal getMontoxCuenta(
		      ArrayList<ReporteEstadoResultadosIntegralesData> dataCuentas, String cuenta,
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
		    return Tree.getMembers(conn, strTree, (strChild == null || strChild.equals("")) ? "0"
		        : strChild)
		        + ",'0'";
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

		String[] meses = { "Enero", "Febrero", "Marzo", "Abril", "Mayo",
				"Junio", "Julio", "Agosto", "Septiembre", "Octubre",
				"Noviembre", "Diciembre" };
		String retornaMes = "";

		retornaMes = meses[mesInt - 1];
		return retornaMes;

	}

	@Override
	public String getServletInfo() {
		return "Servlet ReporteEstadoResultadosIntegrales. This Servlet was made by Pablo Sarobe modified by everybody";
	} // end of getServletInfo() method
}
