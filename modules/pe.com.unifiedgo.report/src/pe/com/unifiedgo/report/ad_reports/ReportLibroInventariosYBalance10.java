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
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.accounting.StructPle;
import pe.com.unifiedgo.accounting.SunatUtil;

public class ReportLibroInventariosYBalance10 extends HttpSecureAppServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		VariablesSecureApp vars = new VariablesSecureApp(request);

		if (log4j.isDebugEnabled())
			log4j.debug("Command: " + vars.getStringParameter("Command"));

		if (vars.commandIn("DEFAULT")) {
			String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReportLibroInventariosYBalance10|DateFrom", "");
			String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportLibroInventariosYBalance10|DateTo", "");
			String strOrg = vars.getGlobalVariable("inpOrg", "ReportLibroInventariosYBalance10|Org", "0");
			String strRecord = vars.getGlobalVariable("inpRecord", "ReportLibroInventariosYBalance10|Record", "");
			String strTable = vars.getGlobalVariable("inpTable", "ReportLibroInventariosYBalance10|Table", "");
			printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg, strTable, strRecord);

		} else if (vars.commandIn("DIRECT")) {
			String strTable = vars.getGlobalVariable("inpTable", "ReportLibroInventariosYBalance10|Table");
			String strRecord = vars.getGlobalVariable("inpRecord", "ReportLibroInventariosYBalance10|Record");

			setHistoryCommand(request, "DIRECT");
			vars.setSessionValue("ReportLibroInventariosYBalance10.initRecordNumber", "0");
			printPageDataSheet(response, vars, "", "", "", strTable, strRecord);

		} else if (vars.commandIn("FIND")) {
			String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
					"ReportLibroInventariosYBalance10|DateFrom");
			String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportLibroInventariosYBalance10|DateTo");
			String strOrg = vars.getGlobalVariable("inpOrg", "ReportLibroInventariosYBalance10|Org", "0");
			vars.setSessionValue("ReportLibroInventariosYBalance10.initRecordNumber", "0");
			setHistoryCommand(request, "DEFAULT");
			printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg, "", "");

		} else if (vars.commandIn("PDF", "XLS")) {
			if (log4j.isDebugEnabled())
				log4j.debug("PDF");
			String strDateFrom = vars.getStringParameter("inpDateFrom");
			String strDateTo = vars.getStringParameter("inpDateTo");
			String strOrg = vars.getStringParameter("inpOrg");			
			setHistoryCommand(request, "DEFAULT");
			printPagePDF(request, response, vars, strDateFrom, strDateTo, strOrg);

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
		ReportLibroInventariosYBalance10Data[] data = null;
		String strPosition = "0";

		String discard[] = { "discard" };

		xmlDocument = xmlEngine
				.readXmlTemplate("pe/com/unifiedgo/report/ad_reports/ReportLibroInventariosYBalance10", discard)
				.createXmlDocument();

		ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportLibroInventariosYBalance10", false, "", "",
				"imprimir();return false;", false, "ad_reports", strReplaceWith, false, true);
		toolbar.setEmail(false);

		toolbar.prepareSimpleToolBarTemplate();

		toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");

		xmlDocument.setParameter("toolbar", toolbar.toString());

		try {
			WindowTabs tabs = new WindowTabs(this, vars,
					"pe.com.unifiedgo.report.ad_reports.ReportLibroInventariosYBalance10");
			xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
			xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
			xmlDocument.setParameter("childTabContainer", tabs.childTabs());
			xmlDocument.setParameter("theme", vars.getTheme());
			NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportLibroInventariosYBalance10.html",
					classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
			xmlDocument.setParameter("navigationBar", nav.toString());
			LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportLibroInventariosYBalance10.html",
					strReplaceWith);
			xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
		} catch (Exception ex) {
			throw new ServletException(ex);
		}
		{
			OBError myMessage = vars.getMessage("ReportLibroInventariosYBalance10");
			vars.removeMessage("ReportLibroInventariosYBalance10");
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
					Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportLibroInventariosYBalance10"),
					Utility.getContext(this, vars, "#User_Client", "ReportLibroInventariosYBalance10"), '*');
			comboTableData.fillParameters(null, "ReportLibroInventariosYBalance10", "");
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
		vars.setSessionValue("ReportLibroInventariosYBalance10|Record", strRecord);
		vars.setSessionValue("ReportLibroInventariosYBalance10|Table", strTable);

		xmlDocument.setParameter("paramPeriodosArray",
				Utility.arrayInfinitasEntradas("idperiodo;periodo;fechainicial;fechafinal;idorganizacion",
						"arrPeriodos", ReportLibroInventariosYBalance10Data.select_periodos(this)));

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
			String strDateFrom, String strDateTo, String strOrg)
			throws IOException, ServletException {

		ReportLibroInventariosYBalance10Data[] data = null;
		
		data = ReportLibroInventariosYBalance10Data.selectCuenta10(this, strOrg, strDateTo);
		
		if (data.length == 0) {
			advisePopUp(request, response, "WARNING", Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
					Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
			return;
		}
		
		data = getData(this, vars, data, strDateTo, strOrg);

		String[] fechaAnioActual = strDateTo.split("-");
		Integer anioActual = new Integer(fechaAnioActual[2]);
		String fechaIniAnioActual = "01-01-" + anioActual;
		
		for (int i = 0; i < data.length; i++) {
			
		}

		String strSubtitle = (Utility.messageBD(this, "LegalEntity", vars.getLanguage()) + ": ")
				+ ReportLibroInventariosYBalance10Data.selectCompany(this, vars.getClient()) + "\n" + "RUC:"
				+ ReportLibroInventariosYBalance10Data.selectRucOrg(this, strOrg) + "\n";
		;

		if (!("0".equals(strOrg)))
			strSubtitle += (Utility.messageBD(this, "OBUIAPP_Organization", vars.getLanguage()) + ": ")
					+ ReportLibroInventariosYBalance10Data.selectOrg(this, strOrg) + "\n";

		String strOutput;
		String strReportName;
		if (vars.commandIn("PDF")) {
			strOutput = "pdf";
			strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportLibroInventariosYBalance10.jrxml";
		} else {
			strOutput = "xls";
			strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportLibroInventariosYBalance10Excel.jrxml";
		}

		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("Subtitle", strSubtitle);
		parameters.put("Ruc", ReportLibroInventariosYBalance10Data.selectRucOrg(this, strOrg));
		parameters.put("Razon", ReportLibroInventariosYBalance10Data.selectSocialName(this, strOrg));
		parameters.put("strDateFrom", fechaIniAnioActual);
		parameters.put("strDateTo", strDateTo);
		parameters.put("dateTo", StringToDate(strDateTo));

		if (data.length == 0) {
			advisePopUp(request, response, "WARNING", Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
					Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
			return;
		}
		renderJR(vars, response, strReportName, "LibroIB_Cuenta10", strOutput, parameters, data, null);
	}

	public static StructPle getPLECuenta10(ConnectionProvider conn, VariablesSecureApp vars, String strDateFrom,
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
		
		ReportLibroInventariosYBalance10Data[] data = ReportLibroInventariosYBalance10Data.selectCuenta10(conn, strOrg, strDateTo);
		
		data = getData(conn, vars, data, strDateTo,  strOrg);

		StringBuffer sb = new StringBuffer();
		StructPle sunatPle = new StructPle();
		sunatPle.numEntries = 0;

		SimpleDateFormat dt = new SimpleDateFormat("yyyyMMdd");
		DecimalFormat df = new DecimalFormat("0.00##");

		String period = dt.format(dateTo); // Periodo
		String OpStatus = "1"; // Estado de la Operación

		for (int i = 0; i < data.length; i++) {

			ReportLibroInventariosYBalance10Data account = data[i];
			String linea = period + "|" + account.codcuenta + "|" + account.codbanco + "|" + account.nrocuenta + "|"
					+ account.moneda + "|" + df.format(df.parse(account.saldodebe)) + "|"
					+ df.format(df.parse(account.saldohaber)) + "|" + OpStatus + "|";

			if (!sb.toString().equals(""))
				sb.append("\n");
			sb.append(linea);
			sunatPle.numEntries++;

		}

		sunatPle.data = sb.toString();

		Organization org = OBDal.getInstance().get(Organization.class, strOrg);
		String rucAdq = SunatUtil.LPad(org.getOrganizationInformationList().get(0).getTaxID(), 11, '0');

		String filename = "LE" + rucAdq + dt.format(dateTo) + "030200011111.TXT"; // LERRRRRRRRRRRAAAAMMDD030200CCOIM1.TXT

		sunatPle.filename = filename;

		return sunatPle;
	}
	
	
	/* Generar objeto del PLE */
	public static StructPle getStructPLECuenta10(ConnectionProvider conn, VariablesSecureApp vars, Date strDateFrom,
			Date strDateTo, String strOrg) throws Exception {
		
		String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty("dateFormat.java");
		SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_JAVA);
		String strDttTo = sdf.format(strDateTo);
		
	    ReportLibroInventariosYBalance10Data[] data = ReportLibroInventariosYBalance10Data.selectCuenta10(conn, strDttTo, strOrg);
	    data = getData(conn, vars, data, strDttTo, strOrg);

	    StringBuffer sb = new StringBuffer();
	    StructPle sunatPle = new StructPle();
	    sunatPle.numEntries = 0;

	    SimpleDateFormat dt = new SimpleDateFormat("yyyyMMdd");
	    DecimalFormat df = new DecimalFormat("0.00##");

	    String period = dt.format(strDateTo); // Periodo
	    String OpStatus = "1"; // Estado de la Operación

	    for (int i = 0; i < data.length; i++) {

	      ReportLibroInventariosYBalance10Data account = data[i];
	      String linea = period + "|" + account.codcuenta + "|" + account.codbanco + "|"
	          + account.nrocuenta + "|" + account.moneda + "|" + df.format(df.parse(account.saldodebe))
	          + "|" + df.format(df.parse(account.saldohaber)) + "|" + OpStatus + "|";

	      if (!sb.toString().equals(""))
	        sb.append("\n");
	      sb.append(linea);
	      sunatPle.numEntries++;
	    }

	    sunatPle.data = sb.toString();

	    Organization org = OBDal.getInstance().get(Organization.class, strOrg);
	    String rucAdq = SunatUtil.LPad(org.getOrganizationInformationList().get(0).getTaxID(), 11, '0');

	    String filename = "LE" + rucAdq + dt.format(strDateTo) + "030200011111.TXT"; // LERRRRRRRRRRRAAAAMMDD030200CCOIM1.TXT

	    sunatPle.filename = filename;

	    return sunatPle;
	}

	private static ReportLibroInventariosYBalance10Data[] getData(ConnectionProvider conn, VariablesSecureApp vars, ReportLibroInventariosYBalance10Data[] data,
			 String strDateTo, String strOrg)
			throws IOException, ServletException {

		for (int i = 0; i < data.length; i++) {
			ReportLibroInventariosYBalance10Data[] accountinfo = ReportLibroInventariosYBalance10Data
					.getFinancialAccountInfoFromAccvalue(conn, strOrg, vars.getClient(), data[i].codcuenta);
			if (accountinfo != null && accountinfo.length > 0) {
				if (accountinfo[0].tipocuenta.equals("B")) {
					data[i].finFinancialAccountId = accountinfo[0].finFinancialAccountId;
					data[i].nombrecuenta = accountinfo[0].nombrecuenta;
					data[i].nrocuenta = accountinfo[0].nrocuenta;
					data[i].nrocuentaoriginal = accountinfo[0].nrocuentaoriginal;
					data[i].codbanco = accountinfo[0].codbanco;
					data[i].moneda = accountinfo[0].moneda;
					data[i].tipocuenta = accountinfo[0].tipocuenta;
				} else {
					data[i].finFinancialAccountId = accountinfo[0].finFinancialAccountId;
					data[i].nombrecuenta = accountinfo[0].nombrecuenta;
					data[i].nrocuenta = "-";
					data[i].nrocuentaoriginal = "-";
					data[i].codbanco = "99";
					data[i].moneda = accountinfo[0].moneda;
					data[i].tipocuenta = accountinfo[0].tipocuenta;
				}
			} else {
				data[i].finFinancialAccountId = "";
				data[i].nombrecuenta = "-";
				data[i].nrocuenta = "-";
				data[i].nrocuentaoriginal = "-";
				data[i].codbanco = "99";
				data[i].moneda = "PEN";
				data[i].tipocuenta = "N/A";
			}
			
			String codigoMoneda = "";
			if (data[i].moneda.equals("PEN")) {
				codigoMoneda = "1";
			} else if (data[i].moneda.equals("USD")) {
				codigoMoneda = "2";
			} else {
				codigoMoneda = "9";
			}
			data[i].moneda=codigoMoneda;
		}
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
		return "Servlet ReportLibroInventariosYBalance10. This Servlet was made by Pablo Sarobe modified by everybody";
	} // end of getServletInfo() method
}
