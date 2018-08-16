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
import java.util.ArrayList;

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

public class ReporteContableVerificacionDocumentoCompras extends
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
					"ReporteContableVerificacionDocumentoCompras|DateFrom", "");
			String strDateTo = vars.getGlobalVariable("inpDateTo",
					"ReporteContableVerificacionDocumentoCompras|DateTo", "");
			String strOrg = vars.getGlobalVariable("inpOrg",
					"ReporteContableVerificacionDocumentoCompras|Org", "0");
			String strRecord = vars.getGlobalVariable("inpRecord",
					"ReporteContableVerificacionDocumentoCompras|Record", "");
			String strTable = vars.getGlobalVariable("inpTable",
					"ReporteContableVerificacionDocumentoCompras|Table", "");
			printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg,
					strTable, strRecord);

		} else if (vars.commandIn("DIRECT")) {
			String strTable = vars.getGlobalVariable("inpTable",
					"ReporteContableVerificacionDocumentoCompras|Table");
			String strRecord = vars.getGlobalVariable("inpRecord",
					"ReporteContableVerificacionDocumentoCompras|Record");

			setHistoryCommand(request, "DIRECT");
			vars.setSessionValue(
					"ReporteContableVerificacionDocumentoCompras.initRecordNumber",
					"0");
			printPageDataSheet(response, vars, "", "", "", strTable, strRecord);

		} else if (vars.commandIn("FIND")) {
			String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
					"ReporteContableVerificacionDocumentoCompras|DateFrom");
			String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
					"ReporteContableVerificacionDocumentoCompras|DateTo");
			String strOrg = vars.getGlobalVariable("inpOrg",
					"ReporteContableVerificacionDocumentoCompras|Org", "0");
			vars.setSessionValue(
					"ReporteContableVerificacionDocumentoCompras.initRecordNumber",
					"0");
			setHistoryCommand(request, "DEFAULT");
			printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg,
					"", "");

		} else if (vars.commandIn("PDF", "XLS")) {
			if (log4j.isDebugEnabled())
				log4j.debug("PDF");
			String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
					"ReporteContableVerificacionDocumentoCompras|DateFrom");
			String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
					"ReporteContableVerificacionDocumentoCompras|DateTo");
			String strOrg = vars.getGlobalVariable("inpOrg",
					"ReporteContableVerificacionDocumentoCompras|Org", "0");

			String strTable = vars.getStringParameter("inpTable");
			String strRecord = vars.getStringParameter("inpRecord");
			setHistoryCommand(request, "DEFAULT");
			printPagePDF(request,response, vars, strDateFrom, strDateTo, strOrg,
					strTable, strRecord);

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
		ReporteContableVerificacionDocumentoComprasData[] data = null;
		String strPosition = "0";
		ToolBar toolbar = new ToolBar(this, vars.getLanguage(),
				"ReporteContableVerificacionDocumentoCompras", false, "", "",
				"imprimir();return false;", false, "ad_reports",
				strReplaceWith, false, true);
		toolbar.setEmail(false);
		
		toolbar.prepareSimpleToolBarTemplate();
		toolbar.prepareRelationBarTemplate(false, false,
				"imprimirXLS();return false;");
		
		if (data == null || data.length == 0) {
			String discard[] = { "secTable" };
//			toolbar.prepareRelationBarTemplate(
//					false,
//					false,
//					"submitCommandForm('XLS', false, null, 'ReporteContableVerificacionDocumentoCompras.xls', 'EXCEL');return false;");
			xmlDocument = xmlEngine
					.readXmlTemplate(
							"pe/com/unifiedgo/report/ad_reports/ReporteContableVerificacionDocumentoCompras",
							discard).createXmlDocument();
			data = ReporteContableVerificacionDocumentoComprasData.set("0");
			data[0].rownum = "0";
		}
		xmlDocument.setParameter("toolbar", toolbar.toString());
		try {
			WindowTabs tabs = new WindowTabs(this, vars,
					"pe.com.unifiedgo.report.ad_reports.ReporteContableVerificacionDocumentoCompras");
			xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
			xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
			xmlDocument.setParameter("childTabContainer", tabs.childTabs());
			xmlDocument.setParameter("theme", vars.getTheme());
			NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
					"ReporteContableVerificacionDocumentoCompras.html",
					classInfo.id, classInfo.type, strReplaceWith,
					tabs.breadcrumb());
			xmlDocument.setParameter("navigationBar", nav.toString());
			LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
					"ReporteContableVerificacionDocumentoCompras.html",
					strReplaceWith);
			xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
		} catch (Exception ex) {
			throw new ServletException(ex);
		}
		{
			OBError myMessage = vars
					.getMessage("ReporteContableVerificacionDocumentoCompras");
			vars.removeMessage("ReporteContableVerificacionDocumentoCompras");
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
							"ReporteContableVerificacionDocumentoCompras"),
					Utility.getContext(this, vars, "#User_Client",
							"ReporteContableVerificacionDocumentoCompras"), '*');
			comboTableData.fillParameters(null,
					"ReporteContableVerificacionDocumentoCompras", "");
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
	  			ReporteContableVerificacionDocumentoComprasData
				.select_periodos(this)));
		vars.setSessionValue(
				"ReporteContableVerificacionDocumentoCompras|Record", strRecord);
		vars.setSessionValue(
				"ReporteContableVerificacionDocumentoCompras|Table", strTable);

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

	private void printPagePDF(HttpServletRequest request,HttpServletResponse response,
			VariablesSecureApp vars, String strDateFrom, String strDateTo,
			String strOrg, String strTable, String strRecord)
			throws IOException, ServletException {

		ReporteContableVerificacionDocumentoComprasData[] data = null;

		ArrayList<ReporteContableVerificacionDocumentoComprasData> listData = new ArrayList<ReporteContableVerificacionDocumentoComprasData>();

		String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
		String strOrgFamily = getFamily(strTreeOrg, strOrg);

		data = ReporteContableVerificacionDocumentoComprasData
				.select_cro_pag_imp(this, Utility.getContext(this, vars,
						"#User_Client",
						"ReporteContableVerificacionDocumentoCompras"),
						strOrgFamily, strDateFrom, DateTimeData.nDaysAfter(
								this, strDateTo, "1"));
		
	      if (data.length==0) {
	          advisePopUp(request, response, "WARNING", Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()), Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
	          return;
	        }

		String strSubtitle = (Utility.messageBD(this, "LegalEntity",
				vars.getLanguage()) + ": ")
				+ ReporteContableVerificacionDocumentoComprasData.selectCompany(
						this, vars.getClient())
				+ "\n"
				+ "RUC:"
				+ ReporteContableVerificacionDocumentoComprasData.selectRucOrg(
						this, strOrg) + "\n";

		if (!("0".equals(strOrg)))
			strSubtitle += (Utility.messageBD(this, "OBUIAPP_Organization",
					vars.getLanguage()) + ": ")
					+ ReporteContableVerificacionDocumentoComprasData.selectOrg(
							this, strOrg) + "\n";

		// if (!"".equals(strDateFrom) || !"".equals(strDateTo))
		// strSubtitle += (Utility.messageBD(this, "From", vars.getLanguage()) +
		// ": ") + strDateFrom
		// + "  " + (Utility.messageBD(this, "OBUIAPP_To", vars.getLanguage()) +
		// ": ") + strDateTo
		// + "\n";
		System.out.println("Llega hasta aqui");
		String strOutput;
		String strReportName;
		if (vars.commandIn("PDF")) {
			strOutput = "pdf";
			strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteContableVerificacionDocumentoCompras.jrxml";
		} else {
			strOutput = "xls";
			strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteContableVerificacionDocumentoComprasExcel.jrxml";
		}

		HashMap<String, Object> parameters = new HashMap<String, Object>();
		// parameters.put("Subtitle", strSubtitle);
		parameters.put("Ruc", ReporteContableVerificacionDocumentoComprasData
				.selectRucOrg(this, strOrg));
		parameters.put("organizacion",
				ReporteContableVerificacionDocumentoComprasData
						.selectSocialName(this, strOrg));

		// parameters.put("dateFrom", StringToDate(strDateFrom));
		// parameters.put("dateTo", StringToDate(strDateTo));
		parameters.put("dateFrom", StringToDate(strDateFrom));
		parameters.put("dateTo", StringToDate(strDateTo));
		renderJR(vars, response, strReportName,
				"Reporte_Contable_Verificacion_Documento_Compras", strOutput,
				parameters, data, null);
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
		return "Servlet ReporteContableVerificacionDocumentoCompras. This Servlet was made by Pablo Sarobe modified by everybody";
	} // end of getServletInfo() method
}
