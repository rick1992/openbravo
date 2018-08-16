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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.businessUtility.AccountingSchemaMiscData;
import org.openbravo.erpCommon.businessUtility.Tree;
import org.openbravo.erpCommon.businessUtility.TreeData;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchemaTable;
import org.openbravo.xmlEngine.XmlDocument;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;

import pe.com.unifiedgo.accounting.StructPle;
import pe.com.unifiedgo.accounting.SunatUtil;
import pe.com.unifiedgo.accounting.data.SCOInternalDoc;
import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class ReportCashBank extends HttpSecureAppServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		VariablesSecureApp vars = new VariablesSecureApp(request);

		if (log4j.isDebugEnabled())
			log4j.debug("Command: " + vars.getStringParameter("Command"));

		if (vars.commandIn("DEFAULT")) {
			String strDateFrom = vars.getGlobalVariable("inpDateFrom",
					"ReportCashBank|DateFrom",
					SREDateTimeData.FirstDayOfMonth(this));
			String strDateTo = vars.getGlobalVariable("inpDateTo",
					"ReportCashBank|DateTo", SREDateTimeData.today(this));
			String strOrg = vars.getGlobalVariable("inpOrg",
					"ReportCashBank|Org", "0");
			String strRecord = vars.getGlobalVariable("inpRecord",
					"ReportCashBank|Record", "");
			String strTable = vars.getGlobalVariable("inpTable",
					"ReportCashBank|Table", "");
			String strFinancialAccountId = vars.getGlobalVariable(
					"inpFinFinancialAccount",
					"ReportCashBank|FinancialAccountId", "",
					IsIDFilter.instance);
			printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg,
					strFinancialAccountId, strTable, strRecord);

		} else if (vars.commandIn("DIRECT")) {
			String strTable = vars.getGlobalVariable("inpTable",
					"ReportCashBank|Table");
			String strRecord = vars.getGlobalVariable("inpRecord",
					"ReportCashBank|Record");
			String strFinancialAccountId = vars.getRequestGlobalVariable(
					"inpFinFinancialAccount",
					"ReportCashBank|FinancialAccountId", IsIDFilter.instance);

			setHistoryCommand(request, "DIRECT");
			vars.setSessionValue("ReportCashBank.initRecordNumber", "0");
			printPageDataSheet(response, vars, "", "", "",
					strFinancialAccountId, strTable, strRecord);

		} else if (vars.commandIn("FIND")) {
			String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
					"ReportCashBank|DateFrom");
			String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
					"ReportCashBank|DateTo");
			String strOrg = vars.getGlobalVariable("inpOrg",
					"ReportCashBank|Org", "0");
			String strFinancialAccountId = vars.getRequestGlobalVariable(
					"inpFinFinancialAccount",
					"ReportCashBank|FinancialAccountId", IsIDFilter.instance);
			vars.setSessionValue("ReportCashBank.initRecordNumber", "0");
			setHistoryCommand(request, "DEFAULT");
			printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg,
					strFinancialAccountId, "", "");

		} else if (vars.commandIn("PDF", "XLS")) {
			if (log4j.isDebugEnabled())
				log4j.debug("PDF");
			String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
					"ReportCashBank|DateFrom");
			String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
					"ReportCashBank|DateTo");
			String strOrg = vars.getGlobalVariable("inpOrg",
					"ReportCashBank|Org", "0");
			String strTipoLibro = vars.getGlobalVariable("inpTipoLibro",
					"ReportCashBank|TipoLibro");
			String strFinancialAccountId = vars.getRequestGlobalVariable(
					"inpFinFinancialAccount",
					"ReportCashBank|FinancialAccountId", IsIDFilter.instance);

			String strTable = vars.getStringParameter("inpTable");
			String strRecord = vars.getStringParameter("inpRecord");
			setHistoryCommand(request, "DEFAULT");
			printPagePDF(request, response, vars, strDateFrom, strDateTo,
					strOrg, strFinancialAccountId, strTable, strRecord,
					strTipoLibro);

		} else
			pageError(response);
	}

	private void printPageDataSheet(HttpServletResponse response,
			VariablesSecureApp vars, String strDateFrom, String strDateTo,
			String strOrg, String strFinancialAccountId, String strTable,
			String strRecord) throws IOException, ServletException {
		if (log4j.isDebugEnabled())
			log4j.debug("Output: dataSheet");
		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();
		XmlDocument xmlDocument = null;
		ReportCashBankData[] data = null;
		String strPosition = "0";
		ToolBar toolbar = new ToolBar(this, vars.getLanguage(),
				"ReportCashBank", false, "", "", "imprimir();return false;",
				false, "ad_reports", strReplaceWith, false, true);
		toolbar.setEmail(false);

		toolbar.prepareSimpleToolBarTemplate();
		toolbar.prepareRelationBarTemplate(false, false,
				"imprimirXLS();return false;");

		if (data == null || data.length == 0) {
			String discard[] = { "secTable" };

			xmlDocument = xmlEngine.readXmlTemplate(
					"pe/com/unifiedgo/report/ad_reports/ReportCashBank",
					discard).createXmlDocument();
			data = ReportCashBankData.set("0");
			data[0].rownum = "0";
		}

		xmlDocument.setParameter("toolbar", toolbar.toString());
		try {
			WindowTabs tabs = new WindowTabs(this, vars,
					"pe.com.unifiedgo.report.ad_reports.ReportCashBank");
			xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
			xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
			xmlDocument.setParameter("childTabContainer", tabs.childTabs());
			xmlDocument.setParameter("theme", vars.getTheme());
			NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
					"ReportCashBank.html", classInfo.id, classInfo.type,
					strReplaceWith, tabs.breadcrumb());
			xmlDocument.setParameter("navigationBar", nav.toString());
			LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
					"ReportCashBank.html", strReplaceWith);
			xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
		} catch (Exception ex) {
			throw new ServletException(ex);
		}
		{
			OBError myMessage = vars.getMessage("ReportCashBank");
			vars.removeMessage("ReportCashBank");
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
					"TABLEDIR", "AD_ORG_ID", "",
					"0C754881EAD94243A161111916E9B9C6",
					Utility.getContext(this, vars, "#AccessibleOrgTree",
							"ReportCashBank"), Utility.getContext(this, vars,
							"#User_Client", "ReportCashBank"), '*');
			comboTableData.fillParameters(null, "ReportCashBank", "");
			xmlDocument.setData("reportAD_ORGID", "liststructure",
					comboTableData.select(true));
		} catch (Exception ex) {
			throw new ServletException(ex);
		}

		xmlDocument.setParameter("FinFinancialAccount", strFinancialAccountId);
		try {
			ComboTableData comboTableData = new ComboTableData(vars, this,
					"TABLEDIR", "FIN_Financial_Account_ID", "", "",
					Utility.getContext(this, vars, "#AccessibleOrgTree",
							"ReportCashBank"), Utility.getContext(this, vars,
							"#User_Client", "ReportCashBank"), 0);
			Utility.fillSQLParameters(this, vars, null, comboTableData,
					"ReportCashBank", strFinancialAccountId);
			xmlDocument.setData("reportFinFinancialAccount", "liststructure",
					comboTableData.select(false));
			comboTableData = null;
		} catch (Exception ex) {
			throw new ServletException(ex);
		}

		xmlDocument.setParameter("FinFinancialAccountArray", Utility
				.arrayTripleEntrada("arrFinFinancialAccount",
						ReportCashBankData.selectFinFinancialAccountDouble(
								this, Utility.getContext(this, vars,
										"#User_Client", "ReportCashBank"))));

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
		vars.setSessionValue("ReportCashBank|Record", strRecord);
		vars.setSessionValue("ReportCashBank|Table", strTable);

		xmlDocument
				.setParameter(
						"paramPeriodosArray",
						Utility.arrayInfinitasEntradas(
								"idperiodo;periodo;fechainicial;fechafinal;idorganizacion",
								"arrPeriodos",
								ReportCashBankData.select_periodos(this)));

		xmlDocument.setData("structure1", data);
		out.println(xmlDocument.print());
		out.close();
	}

	// private ReportCashBankData[] notshow(ReportCashBankData[] data,
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

	private static ReportCashBankData[] getData(ConnectionProvider conn,
			VariablesSecureApp vars, String strDateFrom, String strDateTo,
			String adUserClient, String strOrg, String strFinancialAccountId,
			String tipoLibro) throws IOException, ServletException {

		HashMap<String, Integer> hashAsientos = new HashMap<String, Integer>();

		System.out.println("strDateFrom:"
				+ strDateFrom
				+ " - strDateTo:"
				+ strDateTo
				+ " - strOrg:"
				+ strOrg
				+ " - strFinancialAccountId:"
				+ strFinancialAccountId
				+ " - tipoLibro:"
				+ tipoLibro
				+ " - a:"
				+ Utility.getContext(conn, vars, "#User_Client",
						"ReportCashBank"));

		ReportCashBankData[] data = null;

		ReportCashBankData LineaSaldo = null;
		String strTreeOrg = TreeData.getTreeOrg(conn, vars.getClient());
		String strOrgFamily = getFamily(conn, strTreeOrg, strOrg);

		ArrayList<ReportCashBankData> listaFinal = new ArrayList<ReportCashBankData>();

		if (tipoLibro.equals("CTACORRIENTE")) {
		
			
			data = ReportCashBankData.selectCuentaCorrienteModificado(conn,
					adUserClient, strOrgFamily, strDateFrom,
					DateTimeData.nDaysAfter(conn, strDateTo, "1"),
					strFinancialAccountId);

		} else {
			data = ReportCashBankData.selectMovimientosEfectivoMejorado(conn,
					adUserClient, strOrgFamily, strDateFrom,
					DateTimeData.nDaysAfter(conn, strDateTo, "1"),
					strFinancialAccountId);
		}

		String cuentaAnterior = "";

		for (int i = 0; i < data.length; i++) {
			ReportCashBankData[] dataTmp = null;

			if (data[i].finFinancialAccountId
					.compareToIgnoreCase(cuentaAnterior) != 0) {

				LineaSaldo = ReportCashBank.getSaldoCuenta(conn, adUserClient,
						strOrgFamily, strDateFrom,
						data[i].finFinancialAccountId, data[i]);
				cuentaAnterior = data[i].finFinancialAccountId;
				listaFinal.add(LineaSaldo);
			}

			dataTmp = ReportCashBankData.selectRelatedAcctsMejorado(conn,
					data[i].emScoRegnumber, data[i].factCuentaFin,
					adUserClient, strOrgFamily);

			if (dataTmp.length > 0) {

				for (int j = 0; j < dataTmp.length; j++) {

					System.out.println("dataTmp[" + i + "|" + j
							+ "].amtacctcr: " + dataTmp[j].amtacctcr);
					System.out.println("dataTmp[" + i + "|" + j
							+ "].amtacctdr: " + dataTmp[j].amtacctdr);
					System.out.println("dataTmp[" + i + "|" + j
							+ "].emScoRegnumber: " + dataTmp[j].emScoRegnumber);
					System.out.println("dataTmp[" + i + "|" + j
							+ "].factAcctId: " + dataTmp[j].factAcctId);

					dataTmp[j].factCuentaFin = data[i].factCuentaFin;
					dataTmp[j].finFinancialAccountId = data[i].finFinancialAccountId;
					dataTmp[j].emScoRegnumber = data[i].emScoRegnumber;
					// dataTmp[j].dateacct = data[i].dateacct;
					dataTmp[j].nrocuenta = data[i].nrocuenta;
					dataTmp[j].codbanco = data[i].codbanco;
					dataTmp[j].mediopago = data[i].mediopago;

					if (dataTmp[j].mediopago == null
							|| dataTmp[j].mediopago.equals("")) {
						dataTmp[j].mediopago = "999";
					}

					// dataTmp[j].description = data[i].description;
					// dataTmp[j].cliente = data[i].cliente;
					dataTmp[j].referenceno = data[i].referenceno;
					dataTmp[j].codcuenta = data[i].codcuenta;
					dataTmp[j].nombrecuenta = data[i].nombrecuenta;

					if (tipoLibro.equals("CTACORRIENTE")) {
						dataTmp[j].dateacct = data[i].dateacct;
						dataTmp[j].description = data[i].description;
					}

					BusinessPartner bp = OBDal.getInstance().get(
							BusinessPartner.class, dataTmp[j].clienteid);

					dataTmp[j].cliente = "VARIOS";
					if (bp != null)
						dataTmp[j].cliente = bp.getName();

					listaFinal.add(dataTmp[j]);
				}
			}
		}

		//ORDENANDO POR FECHAS ...
		Collections.sort(listaFinal, new Comparator<ReportCashBankData>() {
			@Override
			public int compare(ReportCashBankData p1, ReportCashBankData p2) {

				int resultado = new String(p1.codcuenta).compareTo(new String(
						p2.codcuenta));
				if (resultado != 0) {
					return resultado;
				}

				resultado = new String(p1.nombrecuenta).compareTo(new String(
						p2.nombrecuenta));
				if (resultado != 0) {
					return resultado;
				}

				SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
				Date date1 = new Date();
				Date date2 = new Date();
				try {
					date1 = format.parse(p1.dateacct);
					date2 = format.parse(p2.dateacct);
				} catch (ParseException e) {
					e.printStackTrace();
				}

				resultado = date1.compareTo(date2);
				if (resultado != 0) {
					return resultado;
				}

				resultado = new String(p1.emScoRegnumber).compareTo(new String(
						p2.emScoRegnumber));
				if (resultado != 0) {
					return resultado;
				}
				return resultado;
			}
		});

		ReportCashBankData[] dataFinal = new ReportCashBankData[listaFinal
				.size()];

		for (int i = 0; i < listaFinal.size(); i++) {
			dataFinal[i] = listaFinal.get(i);

			String asiento = dataFinal[i].emScoRegnumber;
			Integer iAsiento = hashAsientos.get(asiento);

			if (iAsiento != null) {
				String nuevoAsiento = asiento + "-" + iAsiento;
				hashAsientos.put(asiento, iAsiento + 1);
				hashAsientos.put(nuevoAsiento, 1);
				dataFinal[i].emScoRegnumber = nuevoAsiento;
			} else {
				hashAsientos.put(asiento, 1);
			}

			// MODIFICACION PARA EL NUMERO CORRELATIVO
			asiento = dataFinal[i].emScoRegnumber;
			if (asiento != null && asiento.contains("-")) {
				String[] newAsiento = asiento.split("-");
				asiento = newAsiento[2] + " / "
						+ quitaZerosIzquierda(newAsiento[1]);
				dataFinal[i].emScoRegnumber = asiento;
			}
		}

		System.out.println("enter getData reportcashbank");
		for (int i = 0; i < dataFinal.length; i++) {
			ReportCashBankData data1 = dataFinal[i];
			System.out.println(data1.codbanco);
		}

		return dataFinal;
	}

	private static String quitaZerosIzquierda(String cadena) {
		String strFinal = "";
		boolean flag = true;
		char[] cadenaChar = cadena.toCharArray();
		for (int i = 0; i < cadenaChar.length; i++) {
			if (cadenaChar[i] == '0' && flag)
				continue;
			flag = false;
			strFinal = strFinal + cadenaChar[i];
		}
		return strFinal;
	}

	private void printPagePDF(HttpServletRequest request,
			HttpServletResponse response, VariablesSecureApp vars,
			String strDateFrom, String strDateTo, String strOrg,
			String strFinancialAccountId, String strTable, String strRecord,
			String tipoLibro) throws IOException, ServletException {
		
		ReportCashBankData[] dataFinal = null;
		if (vars.commandIn("PDF")) {


		dataFinal = getDataAlterno(this, vars, strDateFrom,
				strDateTo, Utility.getContext(this, vars, "#User_Client",
						"ReportCashBank"), strOrg, strFinancialAccountId,
				tipoLibro);
		}else {
			dataFinal = getDataAlterno(this, vars, strDateFrom,
					strDateTo, Utility.getContext(this, vars, "#User_Client",
							"ReportCashBank"), strOrg, strFinancialAccountId,
					tipoLibro);
		}
		

		String strSubtitle = (Utility.messageBD(this, "LegalEntity",
				vars.getLanguage()) + ": ")
				+ ReportCashBankData.selectCompany(this, vars.getClient())
				+ "\n"
				+ "RUC:"
				+ ReportCashBankData.selectRucOrg(this, strOrg)
				+ "\n";
		;

		if (!("0".equals(strOrg)))
			strSubtitle += (Utility.messageBD(this, "OBUIAPP_Organization",
					vars.getLanguage()) + ": ")
					+ ReportCashBankData.selectOrg(this, strOrg) + "\n";

		String strOutput;
		String strReportName = "";
		if (vars.commandIn("PDF")) {
			strOutput = "pdf";
			if (tipoLibro.equals("CTACORRIENTE")) {
				strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportCashBank_CtaCorriente.jrxml";
			} else if (tipoLibro.equals("EFECTIVO")) {
				strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportCashBank_Efectivo.jrxml";
			}
		} else {
			strOutput = "xls";
			if (tipoLibro.equals("CTACORRIENTE"))
				strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportCashBank_CtaCorrienteExcel.jrxml";
			else if (tipoLibro.equals("EFECTIVO"))
				strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportCashBank_EfectivoExcel.jrxml";
		}

		BigDecimal montoSaldo = new BigDecimal("0.0");
		BigDecimal montoSaldoDebe = BigDecimal.ZERO;
		BigDecimal montoSaldoHaber = BigDecimal.ZERO;

		HashMap<String, Object> parameters = new HashMap<String, Object>();

		parameters.put("SaldoDebe", montoSaldoDebe);
		parameters.put("SaldoHaber", montoSaldoHaber);
		parameters.put("Subtitle", strSubtitle);
		parameters.put("Ruc", ReportCashBankData.selectRUC(this, strOrg));
		parameters.put("Razon",
				ReportCashBankData.selectSocialName(this, strOrg));
		parameters.put("dateFrom", StringToDate(strDateFrom));
		parameters.put("dateTo", StringToDate(strDateTo));

		if (dataFinal.length == 0) {
			advisePopUp(
					request,
					response,
					"WARNING",
					Utility.messageBD(this, "ProcessStatus-W",
							vars.getLanguage()),
					Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
			return;
		}

		renderJR(vars, response, strReportName, "Libro_Caja_Banco", strOutput,
				parameters, dataFinal, null);

	}

	private static ReportCashBankData getSaldoCuenta(ConnectionProvider conn,
			String cliente, String strOrgFamily, String strDateFrom,
			String cuenta, ReportCashBankData temporal) {

		ReportCashBankData temporal2 = new ReportCashBankData();
		temporal2.emScoIsauto = temporal.emScoIsauto;
		temporal2.idorganizacion = temporal.idorganizacion;
		temporal2.idperiodo = temporal.idperiodo;
		temporal2.periodo = temporal.periodo;
		temporal2.fechainicial = temporal.fechainicial;
		temporal2.fechafinal = temporal.fechafinal;
		temporal2.clienteid = temporal.clienteid;
		temporal2.possibleinvoice = temporal.possibleinvoice;
		temporal2.currencyid = temporal.currencyid;
		temporal2.factCuentaFin = temporal.factCuentaFin;
		temporal2.codbanco = temporal.codbanco;
		temporal2.nrocuenta = temporal.nrocuenta;
		temporal2.finFinancialAccountId = temporal.finFinancialAccountId;
		temporal2.factAcctId = temporal.factAcctId;
		temporal2.emScoRegnumber = temporal.emScoRegnumber;
		temporal2.dateacct = temporal.dateacct;
		temporal2.mediopago = temporal.mediopago;
		temporal2.description = temporal.description;
		temporal2.referenceno = temporal.referenceno;
		temporal2.cliente = temporal.cliente;
		temporal2.codcuenta = temporal.codcuenta;
		temporal2.nombrecuenta = temporal.nombrecuenta;
		temporal2.amtacctcr = temporal.amtacctcr;
		temporal2.amtacctdr = temporal.amtacctdr;
		temporal2.codCuentaRel = temporal.codCuentaRel;
		temporal2.nombreCuentaRel = temporal.nombreCuentaRel;
		temporal2.ticketno = temporal.ticketno;
		temporal2.nombrebanco = temporal.nombrebanco;
		temporal2.padre = temporal.padre;
		temporal2.type = temporal.type;
		temporal2.id = temporal.id;
		temporal2.name = temporal.name;
		temporal2.emScrIsinbankbook = temporal.emScrIsinbankbook;
		temporal2.adClientId = temporal.adClientId;
		temporal2.adOrgId = temporal.adOrgId;
		temporal2.datetrx = temporal.datetrx;
		temporal2.emScoCorrelativo = temporal.emScoCorrelativo;
		temporal2.statementdate = temporal.statementdate;
		temporal2.rownum = temporal.rownum;

		ReportCashBankData[] saldoConsulta = null;
		
		String [] partesFechaFrom=strDateFrom.split("-"); 
		String fechaInicionAnio="01-01-"+partesFechaFrom[2];

		try {
			saldoConsulta = ReportCashBankData
					.selectCuentaCorrienteSaldoInicial(conn, cliente,
							strOrgFamily,fechaInicionAnio,fechaInicionAnio, strDateFrom,temporal.factCuentaFin);
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		BigDecimal montoSaldoDebe = BigDecimal.ZERO;
		BigDecimal montoSaldoHaber = BigDecimal.ZERO;

		if (saldoConsulta.length > 0) {
			BigDecimal montoSaldo = new BigDecimal(saldoConsulta[0].amtacctcr);

			if (montoSaldo.compareTo(BigDecimal.ZERO) > 0)
				montoSaldoDebe = montoSaldo;
			else
				montoSaldoHaber = montoSaldo.abs();

		} else {
			montoSaldoDebe = new BigDecimal("0.0");
			montoSaldoHaber = new BigDecimal("0.0");
		}

		temporal2.amtacctdr = montoSaldoDebe.toString();
		temporal2.amtacctcr = montoSaldoHaber.toString();
		temporal2.codCuentaRel = "Saldo";
		temporal2.dateacct = "01-01-1800";

		return temporal2;
	}

	private static String getFamily(ConnectionProvider conn, String strTree,
			String strChild) throws IOException, ServletException {
		return Tree.getMembers(conn, strTree,
				(strChild == null || strChild.equals("")) ? "0" : strChild);
		/*
		 * ReportGeneralLedgerData [] data =
		 * ReportGeneralLedgerData.selectChildren(this, strTree, strChild);
		 * String strFamily = ""; if(data!=null && data.length>0) { for (int i =
		 * 0;i<data.length;i++){ if (i>0) strFamily = strFamily + ","; strFamily
		 * = strFamily + data[i].id; } return strFamily += ""; }else return
		 * "'1'";
		 */
	}

	public static Set<String> getDocuments(String org, String accSchema) {

		final StringBuilder whereClause = new StringBuilder();
		final List<Object> parameters = new ArrayList<Object>();
		OBContext.setAdminMode();
		try {
			// Set<String> orgStrct =
			// OBContext.getOBContext().getOrganizationStructureProvider()
			// .getChildTree(org, true);
			Set<String> orgStrct = OBContext.getOBContext()
					.getOrganizationStructureProvider().getNaturalTree(org);
			whereClause.append(" as cd ,");
			whereClause.append(AcctSchemaTable.ENTITY_NAME);
			whereClause.append(" as ca ");
			whereClause.append(" where cd.");
			whereClause.append(DocumentType.PROPERTY_TABLE + ".id");
			whereClause.append("= ca.");
			whereClause.append(AcctSchemaTable.PROPERTY_TABLE + ".id");
			whereClause.append(" and ca.");
			whereClause.append(AcctSchemaTable.PROPERTY_ACCOUNTINGSCHEMA
					+ ".id");
			whereClause.append(" = ? ");
			parameters.add(accSchema);
			whereClause.append("and ca.");
			whereClause.append(AcctSchemaTable.PROPERTY_ACTIVE + "='Y'");
			whereClause.append(" and cd.");
			whereClause.append(DocumentType.PROPERTY_ORGANIZATION + ".id");
			whereClause.append(" in (" + Utility.getInStrSet(orgStrct) + ")");
			whereClause.append(" and ca."
					+ AcctSchemaTable.PROPERTY_ORGANIZATION + ".id");
			whereClause.append(" in (" + Utility.getInStrSet(orgStrct) + ")");
			whereClause.append(" order by cd."
					+ DocumentType.PROPERTY_DOCUMENTCATEGORY);
			final OBQuery<DocumentType> obqDt = OBDal.getInstance()
					.createQuery(DocumentType.class, whereClause.toString());
			obqDt.setParameters(parameters);
			obqDt.setFilterOnReadableOrganization(false);
			TreeSet<String> docBaseTypes = new TreeSet<String>();
			for (DocumentType doc : obqDt.list()) {
				docBaseTypes.add(doc.getDocumentCategory());
			}
			return docBaseTypes;

		} finally {
			OBContext.restorePreviousMode();
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

	public static StructPle getPLE(ConnectionProvider conn,
			VariablesSecureApp vars, String strDateFrom, String strDateTo,
			String adUserClient, String strOrg, String strFinancialAccountId,
			String tipoLibro) throws Exception {

		ReportCashBankData[] dataFinal = getDataAlterno(conn, vars, strDateFrom,
				strDateTo, adUserClient, strOrg, strFinancialAccountId,
				tipoLibro);

		StructPle sunatPle = getStringData(dataFinal, strDateFrom, strDateTo,
				strOrg, tipoLibro);

		return sunatPle;
	}

	private static String getCurrencyString(String id) {
		if (id.equals("308"))
			return "PEN";
		if (id.equals("100"))
			return "USD";
		if (id.equals("102"))
			return "EUR";
		return "--";
	}

	private static StructPle getStringData(ReportCashBankData[] data,
			String strDateFrom, String strDateTo, String strOrg,
			String tipoLibro) {
		StructPle sunatPle = new StructPle();
		sunatPle.numEntries = 0;

		System.out.println("getStringData");
		for (int i = 0; i < data.length; i++) {
			System.out.println(data[i].codbanco);
		}

		StringBuffer sb = new StringBuffer();

		int correlativo = 0;
		String prevRegNumber = "";
		Organization org = OBDal.getInstance().get(Organization.class, strOrg);
		String rucAdq = SunatUtil.LPad(org.getOrganizationInformationList()
				.get(0).getTaxID(), 11, '0');

		SimpleDateFormat formatterForm = new SimpleDateFormat("dd-MM-yyyy");
		Date dttFrom = null;
		try {
			dttFrom = formatterForm.parse(strDateFrom);
		} catch (Exception ex) {
			System.out.println("Exception: " + strDateFrom);
		}

		SimpleDateFormat dt = new SimpleDateFormat("yyyyMM");
		SimpleDateFormat dt2 = new SimpleDateFormat("dd/MM/yyyy");

		String filename = "LE" + rucAdq + dt.format(dttFrom)
				+ "00010100001111.TXT";// LERRRRRRRRRRRAAAAMM0006010000OIM1.TXT

		if (tipoLibro.equals("CTACORRIENTE"))
			filename = "LE" + rucAdq + dt.format(dttFrom)
					+ "00010200001111.TXT";

		ReportCashBankData[] le = data;

		NumberFormat formatter = new DecimalFormat("#0.00");
		NumberFormat formatter3 = new DecimalFormat("#0.000");

		int jj = 0;

		for (int i = 0; i < le.length; i++) {

			ReportCashBankData led = le[i];

			Date dttAcct = null;
			try {
				dttAcct = formatterForm.parse(led.dateacct);
			} catch (Exception ex) {
			}
			String periodoTrib = dt.format(dttAcct) + "00";

			String tipoAsiento = "M00001";
			String regnumber = led.emScoRegnumber;

			if (regnumber == null || regnumber.equals(""))
				continue;

			String linea = "";

			if (tipoLibro.equals("CTACORRIENTE")) {

				String codBanco = led.codbanco;
				String codCuenta = led.nrocuenta;
				String dateAcct = "";
				try {
					dateAcct = dt2.format(formatterForm.parse(led.dateacct));
				} catch (Exception ex) {
				}
				String metodoPago = led.mediopago;

				if (metodoPago.equals(""))
					metodoPago = "999";

				String descripcion = led.description;

				if (descripcion.equals(""))
					descripcion = "--";

				String referenceNo = led.referenceno;

				String bpartnerId = led.clienteid;
				String tipoDocBp = "";
				String numDocBp = "";
				String razonSocial = "";

				if (bpartnerId != null && !bpartnerId.equals("")) {
					BusinessPartner bp = OBDal.getInstance().get(
							BusinessPartner.class, bpartnerId);

					if (bp != null) {
						tipoDocBp = bp.getScrComboItem().getCode();
						numDocBp = bp.getTaxID();
						razonSocial = bp.getName();
					}
				}

				if (numDocBp == null || numDocBp.equals("")) {
					tipoDocBp = "-";
					numDocBp = "-";
					razonSocial = "varios";
				}

				String estadoOp = "1";

				String debe = formatter.format(Double
						.parseDouble(led.amtacctdr));
				String haber = formatter.format(Double
						.parseDouble(led.amtacctcr));

				linea = periodoTrib + "|" + regnumber + "|" + tipoAsiento + "|"
						+ codBanco + "|" + codCuenta + "|" + dateAcct + "|"
						+ metodoPago + "|" + descripcion + "|" + tipoDocBp
						+ "|" + numDocBp + "|" + razonSocial + "|"
						+ referenceNo + "|" + debe + "|" + haber + "|"
						+ estadoOp + "|";

			} else {

				if (led.codCuentaRel == null
						|| led.codCuentaRel.equals("Saldo"))
					continue;

				String numFullDoc = "";
				String serieDoc = "";
				String numDoc = "";
				String tipoDoc = "";
				String fechaContableDoc = "";
				String fechaEmisionDoc = "";

				String invoiceId = led.possibleinvoice;

				if (led.possibleinvoice != null
						&& !led.possibleinvoice.equals("")) {
					Invoice inv = OBDal.getInstance().get(Invoice.class,
							invoiceId);
					if (inv != null) {
						numFullDoc = inv.getScrPhysicalDocumentno();
						if (inv.isSalesTransaction()) {
							if (inv.getDocumentType().getName()
									.equals("AR Invoice"))
								tipoDoc = "01";
							else if (inv.getDocumentType().getName()
									.equals("AR Ticket"))
								tipoDoc = "03";
							else if (inv.getDocumentType().getName()
									.equals("AR Credit Memo"))
								tipoDoc = "07";
							else if (inv.getDocumentType().getName()
									.equals("Return Material Sales Invoice"))
								tipoDoc = "07";
							else if (inv.getDocumentType().getName()
									.equals("AR Debit Memo"))
								tipoDoc = "08";
							else {
								tipoDoc = "";
								numFullDoc = "";
							}

						} else {
							tipoDoc = inv.getScoPodoctypeComboitem().getCode();
						}

						if (!tipoDoc.equals("")) {
							try {
								fechaEmisionDoc = dt2.format(inv
										.isSalesTransaction() ? inv
										.getAccountingDate() : inv
										.getScoNewdateinvoiced());
							} catch (Exception ex) {
							}
						} else {
							tipoDoc = "00";
						}

					}

				}

				numDoc = numFullDoc;
				if (numFullDoc.contains("-")) {
					StringTokenizer st = new StringTokenizer(numFullDoc, "-");
					serieDoc = st.nextToken();
					numDoc = st.nextToken();
					if (tipoDoc.equals("50")) {
						try {
							numDoc = st.nextToken();
							numDoc = st.nextToken();// el
							// 3er
							// digito
						} catch (Exception ex) {
						}
					}
				}

				if (!serieDoc.equals(""))
					serieDoc = SunatUtil.LPad(serieDoc, 4, '0');

				if (tipoDoc.equals(""))
					tipoDoc = "00";
				if (numDoc.equals(""))
					numDoc = "00000000";
				if (fechaEmisionDoc.equals(""))
					fechaEmisionDoc = dt2.format(dttAcct);
				fechaContableDoc = dt2.format(dttAcct);
				String estadoOp = "1";

				String debe = formatter.format(Double
						.parseDouble(led.amtacctdr));
				String haber = formatter.format(Double
						.parseDouble(led.amtacctcr));

				linea = periodoTrib + "|" + regnumber + "|" + tipoAsiento + "|"
						+ led.codCuentaRel + "|||"
						+ getCurrencyString(led.currencyid) + "|" + tipoDoc
						+ "|" + serieDoc + "|" + numDoc + "|"
						+ fechaContableDoc + "||" + fechaEmisionDoc + "|"
						+ led.description + "||" + debe + "|" + haber + "||"
						+ estadoOp + "|";
			}

			if (jj > 0)
				sb.append("\n");
			sb.append(linea);
			sunatPle.numEntries++;
			jj++;
		}

		sunatPle.filename = filename;
		sunatPle.data = sb.toString();
		System.out.println("data:" + sunatPle.data);
		return sunatPle;
	}

	@Override
	public String getServletInfo() {
		return "Servlet ReportCashBank. This Servlet was made by Pablo Sarobe modified by everybody";
	} // end of getServletInfo() method
	
	
	
	private static ReportCashBankData[] getDataAlterno(ConnectionProvider conn,
			VariablesSecureApp vars, String strDateFrom, String strDateTo,
			String adUserClient, String strOrg, String strFinancialAccountId,
			String tipoLibro) throws IOException, ServletException {

		HashMap<String, Integer> hashAsientos = new HashMap<String, Integer>();

		HashMap<String, ArrayList<ReportCashBankData>> hashEmScoRegnumber = new HashMap<String, ArrayList<ReportCashBankData>>();

		ReportCashBankData[] data = null;
		ReportCashBankData[] dataFactRelacionadas = null;

		ReportCashBankData LineaSaldo = null;
		String strTreeOrg = TreeData.getTreeOrg(conn, vars.getClient());
		String strOrgFamily = getFamily(conn, strTreeOrg, strOrg);

		ArrayList<ReportCashBankData> listaFinal = new ArrayList<ReportCashBankData>();

		if (tipoLibro.equals("CTACORRIENTE")) {
						
			data = ReportCashBankData.selectCuentaCorrienteModificado(conn,
					adUserClient, strOrgFamily, strDateFrom,
					DateTimeData.nDaysAfter(conn, strDateTo, "1"),
					strFinancialAccountId);

		} else {
			data = ReportCashBankData.selectMovimientosEfectivoMejorado(conn,
					adUserClient, strOrgFamily, strDateFrom,
					DateTimeData.nDaysAfter(conn, strDateTo, "1"),
					strFinancialAccountId);
		}
		
//		dataFactRelacionadas=ReportCashBankData.selectAllRelatedAccts(conn, "''", adUserClient, strOrgFamily);
		dataFactRelacionadas=ReportCashBankData.selectAllRelatedAcctsBooster(conn, "''", adUserClient, strOrgFamily,adUserClient, strOrgFamily,strDateFrom,
				DateTimeData.nDaysAfter(conn, strDateTo, "1"),
				strFinancialAccountId);

		
		String clave="";
		for(int i= 0 ;i<dataFactRelacionadas.length;i++){
			clave = dataFactRelacionadas[i].emScoRegnumber;
			 ArrayList<ReportCashBankData> temp= new ArrayList<ReportCashBankData>();
			 if(hashEmScoRegnumber.containsKey(clave)){
				 temp=hashEmScoRegnumber.get(clave);
				 temp.add(dataFactRelacionadas[i]);
			 }else {
				 temp.add(dataFactRelacionadas[i]);
			 }
			 hashEmScoRegnumber.put(clave, temp);
		}

		String cuentaAnterior = "";

		for (int i = 0; i < data.length; i++) {

			if (data[i].finFinancialAccountId
					.compareToIgnoreCase(cuentaAnterior) != 0) {

				LineaSaldo = ReportCashBank.getSaldoCuenta(conn, adUserClient,
						strOrgFamily, strDateFrom,
						data[i].finFinancialAccountId, data[i]);
				cuentaAnterior = data[i].finFinancialAccountId;
				listaFinal.add(LineaSaldo);
			}

			llenaDataPrincipalConFactRelacionadas(listaFinal, hashEmScoRegnumber.get(
					data[i].emScoRegnumber), data[i],tipoLibro);
		}

		//ORDENANDO POR FECHAS ...
		Collections.sort(listaFinal, new Comparator<ReportCashBankData>() {
			@Override
			public int compare(ReportCashBankData p1, ReportCashBankData p2) {

				int resultado = new String(p1.factCuentaFin).compareTo(new String(
						p2.factCuentaFin));
				if (resultado != 0) {
					return resultado;
				}

				resultado = new String(p1.nombrecuenta).compareTo(new String(
						p2.nombrecuenta));
				if (resultado != 0) {
					return resultado;
				}

				SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
				Date date1 = new Date();
				Date date2 = new Date();
				try {
					date1 = format.parse(p1.dateacct);
					date2 = format.parse(p2.dateacct);
				} catch (ParseException e) {
					e.printStackTrace();
				}

				resultado = date1.compareTo(date2);
				if (resultado != 0) {
					return resultado;
				}

				resultado = new String(p1.emScoRegnumber).compareTo(new String(
						p2.emScoRegnumber));
				if (resultado != 0) {
					return resultado;
				}
				return resultado;
			}
		});

		ReportCashBankData[] dataFinal = new ReportCashBankData[listaFinal
				.size()];

		for (int i = 0; i < listaFinal.size(); i++) {

			dataFinal[i] = listaFinal.get(i);

			String asiento = dataFinal[i].emScoRegnumber;
			Integer iAsiento = hashAsientos.get(asiento);

			if (iAsiento != null) {
				String nuevoAsiento = asiento + "-" + iAsiento;
				hashAsientos.put(asiento, iAsiento + 1);
				hashAsientos.put(nuevoAsiento, 1);
				dataFinal[i].emScoRegnumber = nuevoAsiento;
			} else {
				hashAsientos.put(asiento, 1);
			}

			asiento = dataFinal[i].emScoRegnumber;
			if (asiento != null && asiento.contains("-") && !asiento.contains("/")) {
				String[] newAsiento = asiento.split("-");
				asiento = newAsiento[2] + " / "
						+ quitaZerosIzquierda(newAsiento[1]);
				dataFinal[i].emScoRegnumber = asiento;
			}

		}

//		System.out.println("enter getData reportcashbank");
//		for (int i = 0; i < dataFinal.length; i++) {
//			ReportCashBankData data1 = dataFinal[i];
//			System.out.println(data1.description);
//		}

		return dataFinal;
	}
	
	private static void llenaDataPrincipalConFactRelacionadas(ArrayList<ReportCashBankData> listPrincipal,
			ArrayList<ReportCashBankData> listFactRelacionadas,ReportCashBankData referencia,String tipoLibro ){
		if(listFactRelacionadas == null)return;
		Iterator <ReportCashBankData> it = listFactRelacionadas.iterator();

		while(it.hasNext()){
			
			ReportCashBankData fact = it.next();
			
			ReportCashBankData nuevaFactRelacionada = new ReportCashBankData ();
					
			if(fact.codCuentaRel.equals(referencia.factCuentaFin))
				continue;//PARA NO CONSIDERAR LAS FACTS CON LA MISMA CUENTA QUE LA FINANCIERA

			nuevaFactRelacionada.factCuentaFin = referencia.factCuentaFin;
			nuevaFactRelacionada.finFinancialAccountId = referencia.finFinancialAccountId;
			nuevaFactRelacionada.emScoRegnumber = referencia.emScoRegnumber;
			nuevaFactRelacionada.nrocuenta = referencia.nrocuenta;
			nuevaFactRelacionada.codbanco = referencia.codbanco;
			nuevaFactRelacionada.mediopago = referencia.mediopago;

			nuevaFactRelacionada.referenceno = referencia.referenceno;
			nuevaFactRelacionada.codcuenta = referencia.codcuenta;
			nuevaFactRelacionada.nombrecuenta = referencia.nombrecuenta;

			if (tipoLibro.equals("CTACORRIENTE") ) { 
				nuevaFactRelacionada.dateacct = referencia.dateacct;
				nuevaFactRelacionada.description = referencia.description;
			}else {
				nuevaFactRelacionada.dateacct = fact.dateacct;
				nuevaFactRelacionada.description = fact.description;
			}
			
			nuevaFactRelacionada.mediopago=fact.mediopago;
			
			if (nuevaFactRelacionada.mediopago == null
					|| nuevaFactRelacionada.mediopago.equals("")) {
				nuevaFactRelacionada.mediopago = "999";
			}

			nuevaFactRelacionada.clienteid=fact.clienteid;

			BusinessPartner bp = OBDal.getInstance().get(
					BusinessPartner.class, nuevaFactRelacionada.clienteid);

			nuevaFactRelacionada.cliente = "VARIOS";
			if (bp != null)
				nuevaFactRelacionada.cliente = bp.getName();
			
			nuevaFactRelacionada.amtacctcr=fact.amtacctcr;
			nuevaFactRelacionada.amtacctdr=fact.amtacctdr;
			
			nuevaFactRelacionada.nombreCuentaRel=fact.nombreCuentaRel;
			nuevaFactRelacionada.codCuentaRel=fact.codCuentaRel;

			listPrincipal.add(nuevaFactRelacionada);
		}
	}
}
