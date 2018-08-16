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
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.accounting.StructPle;
import pe.com.unifiedgo.accounting.SunatUtil;

public class ReportLibroInventariosYBalance40 extends HttpSecureAppServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		VariablesSecureApp vars = new VariablesSecureApp(request);

		if (log4j.isDebugEnabled())
			log4j.debug("Command: " + vars.getStringParameter("Command"));

		if (vars.commandIn("DEFAULT")) {
			String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReportLibroInventariosYBalance40|DateFrom", "");
			String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportLibroInventariosYBalance40|DateTo", "");
			String strOrg = vars.getGlobalVariable("inpOrg", "ReportLibroInventariosYBalance40|Org", "0");
			String strRecord = vars.getGlobalVariable("inpRecord", "ReportLibroInventariosYBalance40|Record", "");
			String strTable = vars.getGlobalVariable("inpTable", "ReportLibroInventariosYBalance40|Table", "");
			printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg, strTable, strRecord);

		} else if (vars.commandIn("DIRECT")) {
			String strTable = vars.getGlobalVariable("inpTable", "ReportLibroInventariosYBalance40|Table");
			String strRecord = vars.getGlobalVariable("inpRecord", "ReportLibroInventariosYBalance40|Record");

			setHistoryCommand(request, "DIRECT");
			vars.setSessionValue("ReportLibroInventariosYBalance40.initRecordNumber", "0");
			printPageDataSheet(response, vars, "", "", "", strTable, strRecord);

		} else if (vars.commandIn("FIND")) {
			String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
					"ReportLibroInventariosYBalance40|DateFrom");
			String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportLibroInventariosYBalance40|DateTo");
			String strOrg = vars.getGlobalVariable("inpOrg", "ReportLibroInventariosYBalance40|Org", "0");
			vars.setSessionValue("ReportLibroInventariosYBalance40.initRecordNumber", "0");
			setHistoryCommand(request, "DEFAULT");
			printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg, "", "");

		} else if (vars.commandIn("PDF", "XLS")) {
			if (log4j.isDebugEnabled())
				log4j.debug("PDF");
			String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
					"ReportLibroInventariosYBalance40|DateFrom");
			String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportLibroInventariosYBalance40|DateTo");
			String strOrg = vars.getGlobalVariable("inpOrg", "ReportLibroInventariosYBalance40|Org", "0");

			String strTable = vars.getStringParameter("inpTable");
			String strRecord = vars.getStringParameter("inpRecord");
			setHistoryCommand(request, "DEFAULT");
			try {
				printPagePDF(request, response, vars, strDateFrom, strDateTo, strOrg, strTable, strRecord);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

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
		ReportLibroInventariosYBalance40Data[] data = null;
		String strPosition = "0";

		String discard[] = { "discard" };

		xmlDocument = xmlEngine
				.readXmlTemplate("pe/com/unifiedgo/report/ad_reports/ReportLibroInventariosYBalance40", discard)
				.createXmlDocument();

		ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportLibroInventariosYBalance40", false, "", "",
				"imprimir();return false;", false, "ad_reports", strReplaceWith, false, true);
		toolbar.setEmail(false);

		toolbar.prepareSimpleToolBarTemplate();

		toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");

		xmlDocument.setParameter("toolbar", toolbar.toString());

		try {
			WindowTabs tabs = new WindowTabs(this, vars,
					"pe.com.unifiedgo.report.ad_reports.ReportLibroInventariosYBalance40");
			xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
			xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
			xmlDocument.setParameter("childTabContainer", tabs.childTabs());
			xmlDocument.setParameter("theme", vars.getTheme());
			NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportLibroInventariosYBalance40.html",
					classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
			xmlDocument.setParameter("navigationBar", nav.toString());
			LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportLibroInventariosYBalance40.html",
					strReplaceWith);
			xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
		} catch (Exception ex) {
			throw new ServletException(ex);
		}
		{
			OBError myMessage = vars.getMessage("ReportLibroInventariosYBalance40");
			vars.removeMessage("ReportLibroInventariosYBalance40");
			if (myMessage != null) {
				xmlDocument.setParameter("messageType", myMessage.getType());
				xmlDocument.setParameter("messageTitle", myMessage.getTitle());
				xmlDocument.setParameter("messageMessage", myMessage.getMessage());
			}
		}

		xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));

		try {
			ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
					"49DC1D6F086945AB82F84C66F5F13F16",
					Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportLibroInventariosYBalance40"),
					Utility.getContext(this, vars, "#User_Client", "ReportLibroInventariosYBalance40"), '*');
			comboTableData.fillParameters(null, "ReportLibroInventariosYBalance40", "");
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
		vars.setSessionValue("ReportLibroInventariosYBalance40|Record", strRecord);
		vars.setSessionValue("ReportLibroInventariosYBalance40|Table", strTable);

		xmlDocument.setParameter("paramPeriodosArray",
				Utility.arrayInfinitasEntradas("idperiodo;periodo;fechainicial;fechafinal;idorganizacion",
						"arrPeriodos", ReportLibroInventariosYBalance40Data.select_periodos(this)));

		xmlDocument.setData("structure1", data);
		out.println(xmlDocument.print());
		out.close();
	}

	private String getFamily(String strTree, String strChild) throws IOException, ServletException {
		return Tree.getMembers(this, strTree, (strChild == null || strChild.equals("")) ? "0" : strChild);
		/*
		 * ReportGeneralLedgerData [] data =
		 * ReportGeneralLedgerData.selectChildren(this, strTree, strChild);
		 * String strFamily = ""; if(data!=null && data.length>0) { for (int i =
		 * 0;i<data.length;i++){ if (i>0) strFamily = strFamily + ","; strFamily
		 * = strFamily + data[i].id; } return strFamily += ""; }else return
		 * "'1'";
		 */
	}

	private void printPagePDF(HttpServletRequest request, HttpServletResponse response, VariablesSecureApp vars,
			String strDateFrom, String strDateTo, String strOrg, String strTable, String strRecord)
			throws IOException, ServletException, ParseException {

		ReportLibroInventariosYBalance40Data[] data = null;
		
		data = ReportLibroInventariosYBalance40Data.getSaldoCuenta40(this, strOrg, strDateTo);
		
		if (data.length == 0) {
			advisePopUp(request, response, "WARNING", Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
					Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
			return;
		}		

		String[] fechaAnioActual = strDateTo.split("-");
		Integer anioActual = new Integer(fechaAnioActual[2]);
		String fechaIniAnioActual = "01-01-" + anioActual;
		
		String strSubtitle = (Utility.messageBD(this, "LegalEntity", vars.getLanguage()) + ": ")
				+ ReportLibroInventariosYBalance40Data.selectCompany(this, vars.getClient()) + "\n" + "RUC:"
				+ ReportLibroInventariosYBalance40Data.selectRucOrg(this, strOrg) + "\n";

		if (!("0".equals(strOrg)))
			strSubtitle += (Utility.messageBD(this, "OBUIAPP_Organization", vars.getLanguage()) + ": ")
					+ ReportLibroInventariosYBalance40Data.selectOrg(this, strOrg) + "\n";

		String strOutput;
		String strReportName;
		if (vars.commandIn("PDF")) {
			strOutput = "pdf";
			strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportLibroInventariosYBalance40.jrxml";
		} else {
			strOutput = "xls";
			strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportLibroInventariosYBalance40Excel.jrxml";
		}

		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("Subtitle", strSubtitle);
		parameters.put("Ruc", ReportLibroInventariosYBalance40Data.selectRucOrg(this, strOrg));
		parameters.put("Razon", ReportLibroInventariosYBalance40Data.selectSocialName(this, strOrg));
		parameters.put("strDateFrom", fechaIniAnioActual);
		parameters.put("strDateTo", strDateTo);
		parameters.put("dateTo", StringToDate(strDateTo));

		
		renderJR(vars, response, strReportName, "LibroIB_Cuenta40", strOutput, parameters, data, null);
	}

	private static ReportLibroInventariosYBalance40Data[] getData(ConnectionProvider conn, VariablesSecureApp vars,
			String strDateFrom, String strDateTo, String adUserClient, String strOrg)
			throws IOException, ServletException, ParseException {

		ReportLibroInventariosYBalance40Data[] data = null;		

		data = ReportLibroInventariosYBalance40Data.getSaldoCuenta40(conn, strOrg, strDateTo);

		return data;
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

	private static String getFamily(ConnectionProvider conn, String strTree, String strChild)
			throws IOException, ServletException {
		return Tree.getMembers(conn, strTree, (strChild == null || strChild.equals("")) ? "0" : strChild);		
	}

	@Override
	public String getServletInfo() {
		return "Servlet ReportLibroInventariosYBalance40. This Servlet was made by Pablo Sarobe modified by everybody";
	} // end of getServletInfo() method
}
