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

import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class ReportValidacionComprobantePercepcionCompra extends
		HttpSecureAppServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		VariablesSecureApp vars = new VariablesSecureApp(request);

		if (log4j.isDebugEnabled())
			log4j.debug("Command: " + vars.getStringParameter("Command"));

		if (vars.commandIn("DEFAULT")) {
			String strDateFrom = vars.getGlobalVariable("inpDateFrom",
					"ReportValidacionComprobantePercepcionCompra|DateFrom", SREDateTimeData.FirstDayOfMonth(this));
			String strDateTo = vars.getGlobalVariable("inpDateTo",
					"ReportValidacionComprobantePercepcionCompra|DateTo", SREDateTimeData.today(this));
			String strOrg = vars.getGlobalVariable("inpOrg",
					"ReportValidacionComprobantePercepcionCompra|Org", "0");
			String strRecord = vars.getGlobalVariable("inpRecord",
					"ReportValidacionComprobantePercepcionCompra|Record", "");
			String strTable = vars.getGlobalVariable("inpTable",
					"ReportValidacionComprobantePercepcionCompra|Table", "");
			String strSoloFacturasPendientes = vars
					.getGlobalVariable(
							"inpDescargarLibroElectronico",
							"ReportValidacionComprobantePercepcionCompra|SoloPendientes",
							"");
			printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg,
					strTable, strRecord, strSoloFacturasPendientes);

		} else if (vars.commandIn("DIRECT")) {
			String strTable = vars.getGlobalVariable("inpTable",
					"ReportValidacionComprobantePercepcionCompra|Table");
			String strRecord = vars.getGlobalVariable("inpRecord",
					"ReportValidacionComprobantePercepcionCompra|Record");

			setHistoryCommand(request, "DIRECT");
			vars.setSessionValue(
					"ReportValidacionComprobantePercepcionCompra.initRecordNumber",
					"0");
			printPageDataSheet(response, vars, "", "", "", strTable, strRecord,
					"");

		} else if (vars.commandIn("FIND")) {
			String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
					"ReportValidacionComprobantePercepcionCompra|DateFrom");
			String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
					"ReportValidacionComprobantePercepcionCompra|DateTo");
			String strOrg = vars.getGlobalVariable("inpOrg",
					"ReportValidacionComprobantePercepcionCompra|Org", "0");
			vars.setSessionValue(
					"ReportValidacionComprobantePercepcionCompra.initRecordNumber",
					"0");
			setHistoryCommand(request, "DEFAULT");
			printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg,
					"", "", "");

		} else if (vars.commandIn("PDF", "XLS")) {
			if (log4j.isDebugEnabled())
				log4j.debug("PDF");
			String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
					"ReportValidacionComprobantePercepcionCompra|DateFrom");
			String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
					"ReportValidacionComprobantePercepcionCompra|DateTo");
			String strOrg = vars.getGlobalVariable("inpOrg",
					"ReportValidacionComprobantePercepcionCompra|Org", "0");

			String strTable = vars.getStringParameter("inpTable");
			String strRecord = vars.getStringParameter("inpRecord");

			String strSoloFacturasPendientes = vars
					.getStringParameter("inpDescargarLibroElectronico");

			setHistoryCommand(request, "DEFAULT");
			printPagePDF(response, vars, strDateFrom, strDateTo, strOrg,
					strTable, strRecord, strSoloFacturasPendientes);

		} else
			pageError(response);
	}

	private void printPageDataSheet(HttpServletResponse response,
			VariablesSecureApp vars, String strDateFrom, String strDateTo,
			String strOrg, String strTable, String strRecord,
			String solopendientes) throws IOException, ServletException {
		if (log4j.isDebugEnabled())
			log4j.debug("Output: dataSheet");
		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();
		XmlDocument xmlDocument = null;
		ReportValidacionComprobantePercepcionCompraData[] data = null;
		String strPosition = "0";
		ToolBar toolbar = new ToolBar(this, vars.getLanguage(),
				"ReportValidacionComprobantePercepcionCompra", false, "", "",
				"imprimir();return false;", false, "ad_reports",
				strReplaceWith, false, true);
		toolbar.setEmail(false);
	      toolbar.prepareSimpleToolBarTemplate();

		if (data == null || data.length == 0) {
			String discard[] = { "secTable" };
//			toolbar.prepareRelationBarTemplate(
//					false,
//					false,
//					"submitCommandForm('XLS', false, null, 'ReportValidacionComprobantePercepcionCompra.xls', 'EXCEL');return false;");
			xmlDocument = xmlEngine
					.readXmlTemplate(
							"pe/com/unifiedgo/report/ad_reports/ReportValidacionComprobantePercepcionCompra",
							discard).createXmlDocument();
			data = ReportValidacionComprobantePercepcionCompraData.set("0");
			data[0].rownum = "0";
		}
		
		xmlDocument.setParameter("toolbar", toolbar.toString());
		try {
			WindowTabs tabs = new WindowTabs(
					this,
					vars,
					"pe.com.unifiedgo.report.ad_reports.ReportValidacionComprobantePercepcionCompra");
			xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
			xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
			xmlDocument.setParameter("childTabContainer", tabs.childTabs());
			xmlDocument.setParameter("theme", vars.getTheme());
			NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
					"ReportValidacionComprobantePercepcionCompra.html",
					classInfo.id, classInfo.type, strReplaceWith,
					tabs.breadcrumb());
			xmlDocument.setParameter("navigationBar", nav.toString());
			LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
					"ReportValidacionComprobantePercepcionCompra.html",
					strReplaceWith);
			xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
		} catch (Exception ex) {
			throw new ServletException(ex);
		}
		{
			OBError myMessage = vars
					.getMessage("ReportValidacionComprobantePercepcionCompra");
			vars.removeMessage("ReportValidacionComprobantePercepcionCompra");
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
					"TABLEDIR", "AD_ORG_ID", "", "D4DF252DEC3B44858454EE5292A8B836", Utility.getContext(this,
							vars, "#AccessibleOrgTree",
							"ReportValidacionComprobantePercepcionCompra"),
					Utility.getContext(this, vars, "#User_Client",
							"ReportValidacionComprobantePercepcionCompra"), '*');
			comboTableData.fillParameters(null,
					"ReportValidacionComprobantePercepcionCompra", "");
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

		xmlDocument.setParameter("paramDescargarLibroElectronico",
				!solopendientes.equals("Y") ? "0" : "1");

		vars.setSessionValue(
				"ReportValidacionComprobantePercepcionCompra|Record", strRecord);
		vars.setSessionValue(
				"ReportValidacionComprobantePercepcionCompra|Table", strTable);

		xmlDocument.setData("structure1", data);
		out.println(xmlDocument.print());
		out.close();
	}

	// private ReportValidacionComprobantePercepcionCompraData[]
	// notshow(ReportValidacionComprobantePercepcionCompraData[] data,
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

	private void printPagePDF(HttpServletResponse response,
			VariablesSecureApp vars, String strDateFrom, String strDateTo,
			String strOrg, String strTable, String strRecord,
			String soloPendientes) throws IOException, ServletException {

		ReportValidacionComprobantePercepcionCompraData[] data = null;

		String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
		String strOrgFamily = getFamily(strTreeOrg, strOrg);

		System.out.println(">>>>>>>>>>>>>");
		System.out.println(soloPendientes);
		System.out.println(soloPendientes.length());
		System.out.println("<<<<<<<<<<<<<");

		data = ReportValidacionComprobantePercepcionCompraData.selectDiff(this,
				Utility.getContext(this, vars, "#User_Client",
						"ReportValidacionComprobantePercepcionCompra"),
				strOrgFamily, strDateFrom, DateTimeData.nDaysAfter(this,
						strDateTo, "1"), soloPendientes);
		;

		String strSubtitle = (Utility.messageBD(this, "LegalEntity",
				vars.getLanguage()) + ": ")
				+ ReportValidacionComprobantePercepcionCompraData
						.selectCompany(this, vars.getClient())
				+ "\n"
				+ "RUC:"
				+ ReportValidacionComprobantePercepcionCompraData.selectRucOrg(
						this, strOrg) + "\n";

		if (!("0".equals(strOrg)))
			strSubtitle += (Utility.messageBD(this, "OBUIAPP_Organization",
					vars.getLanguage()) + ": ")
					+ ReportValidacionComprobantePercepcionCompraData
							.selectOrg(this, strOrg) + "\n";

		// if (!"".equals(strDateFrom) || !"".equals(strDateTo))
		// strSubtitle += (Utility.messageBD(this, "From", vars.getLanguage()) +
		// ": ") + strDateFrom
		// + "  " + (Utility.messageBD(this, "OBUIAPP_To", vars.getLanguage()) +
		// ": ") + strDateTo
		// + "\n";

		String strOutput;
		String strReportName;
		if (vars.commandIn("PDF")) {
			strOutput = "pdf";
			strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportValidacionComprobantePercepcionCompra.jrxml";
		} else {
			strOutput = "xls";
			strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportValidacionComprobantePercepcionCompraExcel.jrxml";
		}

		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("Ruc", ReportValidacionComprobantePercepcionCompraData
				.selectRucOrg(this, strOrg));
		parameters.put("Razon", ReportValidacionComprobantePercepcionCompraData
				.selectOrg(this, strOrg));

		// parameters.put("TaxID",
		// ReportSalesRevenueRecordsData.selectOrgTaxID(this, strOrg));
		parameters.put("dateFrom", StringToDate(strDateFrom));
		parameters.put("dateTo", StringToDate(strDateTo));
		renderJR(vars, response, strReportName, "Validacion de Comprobante de Percepcion de Compra",
				strOutput, parameters, data, null);
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
		return "Servlet ReportValidacionComprobantePercepcionCompra. This Servlet was made by Pablo Sarobe modified by everybody";
	} // end of getServletInfo() method
}
