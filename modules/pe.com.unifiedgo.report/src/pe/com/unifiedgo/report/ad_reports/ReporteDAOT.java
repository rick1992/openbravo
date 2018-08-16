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
import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.data.FieldProvider;
import org.openbravo.data.UtilSql;
import org.openbravo.erpCommon.businessUtility.AccountingSchemaMiscData;
import org.openbravo.erpCommon.businessUtility.Tree;
import org.openbravo.erpCommon.businessUtility.TreeData;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SQLReturnObject;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

import com.sun.org.apache.bcel.internal.generic.FLOAD;

public class ReporteDAOT extends HttpSecureAppServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		VariablesSecureApp vars = new VariablesSecureApp(request);

		if (log4j.isDebugEnabled())
			log4j.debug("Command: " + vars.getStringParameter("Command"));

		if (vars.commandIn("DEFAULT")) {
			String strDateFrom = vars.getGlobalVariable("inpDateFrom",
					"ReporteDAOT|DateFrom", "");
			String strDateTo = vars.getGlobalVariable("inpDateTo",
					"ReporteDAOT|DateTo", "");
			String strOrg = vars.getGlobalVariable("inpOrg", "ReporteDAOT|Org",
					"0");
			String strRecord = vars.getGlobalVariable("inpRecord",
					"ReporteDAOT|Record", "");
			String strTable = vars.getGlobalVariable("inpTable",
					"ReporteDAOT|Table", "");
			printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg,
					strTable, strRecord);

		} else if (vars.commandIn("DIRECT")) {
			String strTable = vars.getGlobalVariable("inpTable",
					"ReporteDAOT|Table");
			String strRecord = vars.getGlobalVariable("inpRecord",
					"ReporteDAOT|Record");

			setHistoryCommand(request, "DIRECT");
			vars.setSessionValue("ReporteDAOT.initRecordNumber", "0");
			printPageDataSheet(response, vars, "", "", "", strTable, strRecord);

		} else if (vars.commandIn("FIND")) {
			String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
					"ReporteDAOT|DateFrom");
			String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
					"ReporteDAOT|DateTo");
			String strOrg = vars.getGlobalVariable("inpOrg", "ReporteDAOT|Org",
					"0");
			vars.setSessionValue("ReporteDAOT.initRecordNumber", "0");
			setHistoryCommand(request, "DEFAULT");
			printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg,
					"", "");

		} else if (vars.commandIn("PDF", "XLS")) {
			if (log4j.isDebugEnabled())
				log4j.debug("PDF");
			String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
					"ReporteDAOT|DateFrom");
			String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
					"ReporteDAOT|DateTo");
			String strOrg = vars.getGlobalVariable("inpOrg", "ReporteDAOT|Org",
					"0");

			String strmontobase = vars.getStringParameter("montobase");

			String strTerceroId = vars.getStringParameter("inpcBPartnerId");

			String strTipoOperacion = vars.getStringParameter("inpTipoReporte");

			String strTable = vars.getStringParameter("inpTable");
			String strRecord = vars.getStringParameter("inpRecord");
			setHistoryCommand(request, "DEFAULT");
			printPagePDF(request, response, vars, strDateFrom, strDateTo,
					strOrg, strTable, strRecord, strmontobase, strTerceroId,
					strTipoOperacion);

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
		ReporteDAOTData[] data = null;
		String strPosition = "0";
		ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReporteDAOT",
				false, "", "", "imprimir();return false;", false, "ad_reports",
				strReplaceWith, false, true);
		toolbar.setEmail(false);

		toolbar.prepareSimpleToolBarTemplate();
		toolbar.prepareRelationBarTemplate(false, false,
				"imprimirXLS();return false;");

		if (data == null || data.length == 0) {
			String discard[] = { "secTable" };
			// toolbar.prepareRelationBarTemplate(
			// false,
			// false,
			// "submitCommandForm('XLS', false, null, 'ReporteDAOT.xls', 'EXCEL');return false;");
			xmlDocument = xmlEngine.readXmlTemplate(
					"pe/com/unifiedgo/report/ad_reports/ReporteDAOT", discard)
					.createXmlDocument();
			data = ReporteDAOTData.set("0");
			data[0].rownum = "0";
		}

		xmlDocument.setParameter("toolbar", toolbar.toString());
		try {
			WindowTabs tabs = new WindowTabs(this, vars,
					"pe.com.unifiedgo.report.ad_reports.ReporteDAOT");
			xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
			xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
			xmlDocument.setParameter("childTabContainer", tabs.childTabs());
			xmlDocument.setParameter("theme", vars.getTheme());
			NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
					"ReporteDAOT.html", classInfo.id, classInfo.type,
					strReplaceWith, tabs.breadcrumb());
			xmlDocument.setParameter("navigationBar", nav.toString());
			LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
					"ReporteDAOT.html", strReplaceWith);
			xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
		} catch (Exception ex) {
			throw new ServletException(ex);
		}
		{
			OBError myMessage = vars.getMessage("ReporteDAOT");
			vars.removeMessage("ReporteDAOT");
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
							vars, "#AccessibleOrgTree", "ReporteDAOT"),
					Utility.getContext(this, vars, "#User_Client",
							"ReporteDAOT"), '*');
			comboTableData.fillParameters(null, "ReporteDAOT", "");
			xmlDocument.setData("reportAD_ORGID", "liststructure",
					comboTableData.select(false));

		} catch (Exception ex) {
			throw new ServletException(ex);
		}

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

		xmlDocument
				.setParameter(
						"paramPeriodosArray",
						Utility.arrayInfinitasEntradas(
								"idperiodo;periodo;fechainicial;fechafinal;idorganizacion",
								"arrPeriodos",
								ReporteDAOTData.select_periodos(this)));

		vars.setSessionValue("ReporteDAOT|Record", strRecord);
		vars.setSessionValue("ReporteDAOT|Table", strTable);

		xmlDocument.setData("structure1", data);
		out.println(xmlDocument.print());
		out.close();
	}

	private String getFamily(String strTree, String strChild)
			throws IOException, ServletException {
		return Tree.getMembers(this, strTree,
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

	private void printPagePDF(HttpServletRequest request,
			HttpServletResponse response, VariablesSecureApp vars,
			String strDateFrom, String strDateTo, String strOrg,
			String strTable, String strRecord, String strmontobase,
			String strTerceroId, String strTipoOperacion) throws IOException,
			ServletException {

		BigDecimal montobase = BigDecimal.ZERO;

		try {
			montobase = new BigDecimal(strmontobase);
		} catch (Exception e) {
			strmontobase = "0";
		}

		ReporteDAOTData[] data = null;
		String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
		String strOrgFamily = getFamily(strTreeOrg, strOrg);

		String mostrarVentas = "";
		String mostrarCompras = "";
		if (strTipoOperacion.equalsIgnoreCase("venta")) {
			mostrarVentas = "(VENTAS)";
		} else
			mostrarCompras = "(COMPRAS)";

		data = ReporteDAOTData.select_daot2(this,
				Utility.getContext(this, vars, "#User_Client", "ReporteDAOT"),
				strOrgFamily, strDateFrom,
				DateTimeData.nDaysAfter(this, strDateTo, "1"), strTerceroId,
				mostrarVentas, mostrarCompras);

		ArrayList<ReporteDAOTData> listTemp = new ArrayList<ReporteDAOTData>();
		ArrayList<ReporteDAOTData> listFinal = new ArrayList<ReporteDAOTData>();
		BigDecimal sumaMontoTercero = BigDecimal.ZERO;

		for (int i = 0; i < data.length; i++) {

			ReporteDAOTData objActual = data[i];
			ReporteDAOTData objSiguiente = (i + 1 < data.length) ? data[i + 1]
					: new ReporteDAOTData();

			listTemp.add(objActual);
			sumaMontoTercero = new BigDecimal(objActual.totalpagar)
					.add(sumaMontoTercero);

			if (!objActual.ruc.equalsIgnoreCase(objSiguiente.ruc)) {
				if (sumaMontoTercero.compareTo(montobase) >= 0) {
					for (int j = 0; j < listTemp.size(); j++)
						listFinal.add(listTemp.get(j));
				}
				sumaMontoTercero = BigDecimal.ZERO;
				listTemp = new ArrayList<ReporteDAOTData>();
			}
		}

		ReporteDAOTData[] bestData = new ReporteDAOTData[listFinal.size()];

		for (int i = 0; i < listFinal.size(); i++) {
			bestData[i] = listFinal.get(i);
		}

		if (bestData.length == 0) {
			advisePopUp(
					request,
					response,
					"WARNING",
					Utility.messageBD(this, "ProcessStatus-W",
							vars.getLanguage()),
					Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
			return;
		}

		String strOutput;
		String strReportName;
		if (vars.commandIn("PDF")) {
			strOutput = "pdf";
			strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteDAOT.jrxml";
		} else {
			strOutput = "xls";
			strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteDAOTExcel.jrxml";
		}

		HashMap<String, Object> parameters = new HashMap<String, Object>();
		// parameters.put("Subtitle", strSubtitle);
		parameters.put("Ruc", ReporteDAOTData.selectRucOrg(this, strOrg));
		parameters.put("organizacion",
				ReporteDAOTData.selectSocialName(this, strOrg));

		parameters.put("montobase", new BigDecimal(strmontobase));
		parameters.put("dateFrom", StringToDate(strDateFrom));
		parameters.put("dateTo", StringToDate(strDateTo));
		parameters.put("tipoOperacion",mostrarCompras+mostrarVentas );
		renderJR(vars, response, strReportName,
				"Declaracion Anual de Operaciones con Terceros ", strOutput,
				parameters, bestData, null);
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
		return "Servlet ReporteDAOT. This Servlet was made by Pablo Sarobe modified by everybody";
	} // end of getServletInfo() method
}
