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
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.data.FieldProvider;
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

public class ReportLibroEstadosFinancieros extends HttpSecureAppServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		VariablesSecureApp vars = new VariablesSecureApp(request);

		if (log4j.isDebugEnabled())
			log4j.debug("Command: " + vars.getStringParameter("Command"));

		if (vars.commandIn("DEFAULT")) {
			String strDateFrom = vars.getGlobalVariable("inpDateFrom",
					"ReportLibroEstadosFinancieros|DateFrom", "");
			String strDateTo = vars.getGlobalVariable("inpDateTo",
					"ReportLibroEstadosFinancieros|DateTo", "");
			String strOrg = vars.getGlobalVariable("inpOrg",
					"ReportLibroEstadosFinancieros|Org", "0");
			String strRecord = vars.getGlobalVariable("inpRecord",
					"ReportLibroEstadosFinancieros|Record", "");
			String strTable = vars.getGlobalVariable("inpTable",
					"ReportLibroEstadosFinancieros|Table", "");
			printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg,
					strTable, strRecord);

		} else if (vars.commandIn("DIRECT")) {
			String strTable = vars.getGlobalVariable("inpTable",
					"ReportLibroEstadosFinancieros|Table");
			String strRecord = vars.getGlobalVariable("inpRecord",
					"ReportLibroEstadosFinancieros|Record");

			setHistoryCommand(request, "DIRECT");
			vars.setSessionValue(
					"ReportLibroEstadosFinancieros.initRecordNumber", "0");
			printPageDataSheet(response, vars, "", "", "", strTable, strRecord);

		} else if (vars.commandIn("FIND")) {
			String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
					"ReportLibroEstadosFinancieros|DateFrom");
			String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
					"ReportLibroEstadosFinancieros|DateTo");
			String strOrg = vars.getGlobalVariable("inpOrg",
					"ReportLibroEstadosFinancieros|Org", "0");
			vars.setSessionValue(
					"ReportLibroEstadosFinancieros.initRecordNumber", "0");
			setHistoryCommand(request, "DEFAULT");
			printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg,
					"", "");

		} else if (vars.commandIn("PDF", "XLS")) {
			if (log4j.isDebugEnabled())
				log4j.debug("PDF");
			String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
					"ReportLibroEstadosFinancieros|DateFrom");
			String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
					"ReportLibroEstadosFinancieros|DateTo");
			String strOrg = vars.getGlobalVariable("inpOrg",
					"ReportLibroEstadosFinancieros|Org", "0");
			String strTipoReporte = vars.getStringParameter("inpTipoReporte",
					"");

			String strTable = vars.getStringParameter("inpTable");
			String strRecord = vars.getStringParameter("inpRecord");
			setHistoryCommand(request, "DEFAULT");
			printPagePDF(response, vars, strDateFrom, strDateTo, strOrg,strTipoReporte,
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
		ReportLibroEstadosFinancierosData[] data = null;
		String strPosition = "0";
		ToolBar toolbar = new ToolBar(this, vars.getLanguage(),
				"ReportLibroEstadosFinancieros", false, "", "",
				"imprimir();return false;", false, "ad_reports",
				strReplaceWith, false, true);
		toolbar.setEmail(false);
		if (data == null || data.length == 0) {
			String discard[] = { "secTable" };
			toolbar.prepareRelationBarTemplate(
					false,
					false,
					"submitCommandForm('XLS', false, null, 'ReportLibroEstadosFinancieros.xls', 'EXCEL');return false;");
			xmlDocument = xmlEngine
					.readXmlTemplate(
							"pe/com/unifiedgo/report/ad_reports/ReportLibroEstadosFinancieros",
							discard).createXmlDocument();
			data = ReportLibroEstadosFinancierosData.set("0");
			data[0].rownum = "0";
		} else {

			// data = notshow(data, vars);

			toolbar.prepareRelationBarTemplate(
					true,
					true,
					"submitCommandForm('XLS', false, null, 'ReportLibroEstadosFinancieros.xls', 'EXCEL');return false;");
			xmlDocument = xmlEngine
					.readXmlTemplate(
							"pe/com/unifiedgo/report/ad_reports/ReportLibroEstadosFinancieros")
					.createXmlDocument();
		}
		xmlDocument.setParameter("toolbar", toolbar.toString());
		try {
			WindowTabs tabs = new WindowTabs(this, vars,
					"pe.com.unifiedgo.report.ad_reports.ReportLibroEstadosFinancieros");
			xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
			xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
			xmlDocument.setParameter("childTabContainer", tabs.childTabs());
			xmlDocument.setParameter("theme", vars.getTheme());
			NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
					"ReportLibroEstadosFinancieros.html", classInfo.id,
					classInfo.type, strReplaceWith, tabs.breadcrumb());
			xmlDocument.setParameter("navigationBar", nav.toString());
			LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
					"ReportLibroEstadosFinancieros.html", strReplaceWith);
			xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
		} catch (Exception ex) {
			throw new ServletException(ex);
		}
		{
			OBError myMessage = vars
					.getMessage("ReportLibroEstadosFinancieros");
			vars.removeMessage("ReportLibroEstadosFinancieros");
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
							"ReportLibroEstadosFinancieros"),
					Utility.getContext(this, vars, "#User_Client",
							"ReportLibroEstadosFinancieros"), '*');
			comboTableData.fillParameters(null,
					"ReportLibroEstadosFinancieros", "");
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
		vars.setSessionValue("ReportLibroEstadosFinancieros|Record", strRecord);
		vars.setSessionValue("ReportLibroEstadosFinancieros|Table", strTable);

		xmlDocument.setData("structure1", data);

		// //////////////////////////// PARA ELEGIR EL TIPO REPORTE
		Vector<Object> vector = new Vector<Object>(0);
		FieldProvider tipo_reporte[] = new FieldProvider[2];

		SQLReturnObject sqlReturnObject = new SQLReturnObject();
		sqlReturnObject.setData("ID", "funcion");
		sqlReturnObject.setData("NAME", "POR FUNCIÓN");
		vector.add(sqlReturnObject);

		sqlReturnObject = new SQLReturnObject();
		sqlReturnObject.setData("ID", "consignatario");
		sqlReturnObject.setData("NAME", "POR NATURALEZA");
		vector.add(sqlReturnObject);

		vector.copyInto(tipo_reporte);

		xmlDocument.setData("reportC_TIPO_REPORT", "liststructure",
				tipo_reporte);

		// ////////////////////

		out.println(xmlDocument.print());
		out.close();
	}

	private String getFamily(String strTree, String strChild)
			throws IOException, ServletException {
		return Tree.getMembers(this, strTree,
				(strChild == null || strChild.equals("")) ? "0" : strChild);
	}

	private void printPagePDF(HttpServletResponse response,
			VariablesSecureApp vars, String strDateFrom, String strDateTo,
			String strOrg,String strTipoReporte, String strTable, String strRecord)
			throws IOException, ServletException {

		ReportLibroEstadosFinancierosData[] data = null;

		String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
		String strOrgFamily = getFamily(strTreeOrg, strOrg);

		// data = ReportLibroEstadosFinancierosData.select_cro_pag_imp(this,
		// Utility.getContext(this, vars, "#User_Client",
		// "ReportLibroEstadosFinancieros"), strOrgFamily,
		// strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"));

		String strOutput = "pdf";
		String strReportName;
		String nombreArchivo;


		if (strTipoReporte.compareToIgnoreCase("funcion") == 0) { // funcion
			strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportLibroEstadosFinancierosFuncion.jrxml";
			nombreArchivo = "Libro EE. FF. de Ganancias"
					+ " y Perdidas x Funcion";
			
			data = new ReportLibroEstadosFinancierosData[22];
			for(int i=0;i<22;i++){
				data[i]=new ReportLibroEstadosFinancierosData();
				data[i].tipo="normal";
			}
			
			data[0].descripcion = "VENTAS";
			data[0].tipo = "cabecera";

			data[1].descripcion = "VENTAS";
			data[2].descripcion = "DESC. REBAJAS Y BONIF. CONCEDIDAS";
			data[2].tipo = "total";
			data[3].descripcion = "VENTAS NETAS";
			data[4].descripcion = "COSTO VENTAS";
			data[4].tipo = "total";

			data[5].descripcion = "UTILIDAD BRUTA";
			data[6].descripcion = "GASTOS ADMINISTRATIVOS";
			data[7].descripcion = "GASTOS DE VENTAS";
			data[7].tipo = "total";

			data[8].descripcion = "UTILIDAD DE OPERACION";
			data[9].descripcion = "OTROS INGRESOS Y EGRESOS";
			data[9].tipo = "cabecera";

			data[10].descripcion = "DESC. REBAJAS Y BONIF. OBTENIDAS";
			data[11].descripcion = "INGRESOS EXCEPCIONALES";
			data[12].descripcion = "OTROS INGRESOS DIVERSOS";
			data[13].descripcion = "INGRESOS FINANCIEROS";
			data[14].descripcion = "CARGAS EXCEPCIONALES";
			data[15].descripcion = "GASTOS FINANCIEROS";
			data[15].tipo = "total";

			data[16].descripcion = "UTILIDAD ANTES DE PARTIC.";
			data[17].descripcion = "DISTRIB. LEGAL DE LA RENTA NETA";
			data[17].tipo = "total";

			data[18].descripcion = "UTILIDAD ANTES DE IMPUESTO";
			data[19].descripcion = "IMPUESTO A LA RENTA";
			data[20].descripcion = "RES. POR EXP. A LA INFLAC. DEL EJER";
			data[20].tipo = "total";

			data[21].descripcion = "UTILIDAD O PERDIDA DEL EJERCICIO";

		} else { // naturaleza
			
			data = new ReportLibroEstadosFinancierosData[30];
			for(int i=0;i<30;i++){
				data[i]=new ReportLibroEstadosFinancierosData();
				data[i].tipo="normal";

			}

			strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportLibroEstadosFinancierosNaturaleza.jrxml";
			nombreArchivo = "Libro EE. FF. de Ganancias y Perdidas x Naturaleza";

			data[0].descripcion = "VENTAS NETAS DE MERCADERIAS";
			data[1].descripcion = "COMPRA DE MERCADERIA";
			data[2].descripcion = "VARIACION DE MERCADERIAS";
			data[3].descripcion = "MARGEN COMERCIAL";
			data[4].descripcion = "VENTAS NETAS DE SERVICIOS";
			data[5].descripcion = "PRODUCCION ALMACENADA";
			data[6].descripcion = "PRODUCCION DEL EJERCICIO";
			data[7].descripcion = "COMPRA DE MATERIAS PRIMAS Y AUX";
			data[8].descripcion = "COMPRA DE SUMINISTROS";
			data[9].descripcion = "COMPRA DE ENVASES Y EMBALAJES";
			data[10].descripcion = "VARIACION DE MATERIAS PRIMAS";
			data[11].descripcion = "VARIACION DE SUMINISTROS";
			data[12].descripcion = "EMVASES Y EMBALAJES";
			data[13].descripcion = "SERVICIOS PRESTADOS";
			data[14].descripcion = "VALOR AGREGADO";
			data[15].descripcion = "CARGAS DE PERSONAL";
			data[16].descripcion = "TRUBUTOS";
			data[17].descripcion = "EXCEDENTE BRUTO DE EXPLOTACION";
			data[18].descripcion = "CARGAS DIVERSAS DE GESTION";
			data[19].descripcion = "PRIVISIONES DEL EJERCICIO";
			data[20].descripcion = "INGRESOS DIVERSOS";
			data[21].descripcion = "CARGAS CUBIERTAS POR PROVISIONES";
			data[22].descripcion = "RESULTADO DE LA EXPLOTACION";
			data[23].descripcion = "INGRESOS FINANCIEROS";
			data[24].descripcion = "INGRESOS EXCEPCIONALES";
			data[25].descripcion = "RESULTADOS ANTES DE PARTIC. E IMP.";
			data[26].descripcion = "DISTRIBUCION LEGAL DE LA RENTA";
			data[27].descripcion = "IMPUESTO A LA RENTA";
			data[28].descripcion = "RES. POR EXP A LA INFL. DEL EJERC.";
			data[29].descripcion = "UTILIDAD O PERDIDA DEL EJERCICIO";

		}

		String strSubtitle = (Utility.messageBD(this, "LegalEntity",
				vars.getLanguage()) + ": ")
				+ ReportLibroEstadosFinancierosData.selectCompany(this,
						vars.getClient())
				+ "\n"
				+ "RUC:"
				+ ReportLibroEstadosFinancierosData.selectRucOrg(this, strOrg)
				+ "\n";
		;

		if (!("0".equals(strOrg)))
			strSubtitle += (Utility.messageBD(this, "OBUIAPP_Organization",
					vars.getLanguage()) + ": ")
					+ ReportLibroEstadosFinancierosData.selectOrg(this, strOrg)
					+ "\n";

		// if (!"".equals(strDateFrom) || !"".equals(strDateTo))
		// strSubtitle += (Utility.messageBD(this, "From", vars.getLanguage()) +
		// ": ") + strDateFrom
		// + "  " + (Utility.messageBD(this, "OBUIAPP_To", vars.getLanguage()) +
		// ": ") + strDateTo
		// + "\n";
		System.out.println("Llega hasta aqui");

//		if (vars.commandIn("PDF")) {
//			strOutput = "pdf";
//			strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportLibroEstadosFinancierosNaturaleza.jrxml";
//		} else {
//			strOutput = "xls";
//			strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportLibroEstadosFinancierosExcel.jrxml";
//		}

		HashMap<String, Object> parameters = new HashMap<String, Object>();
		// parameters.put("Subtitle", strSubtitle);
		parameters.put("columna1",
				"A.C. Diciembre-2014");
		parameters.put("columna2",
				"Noviembre-2014");
		parameters.put("columna3",
				"A.C. Noviembre-2014");
		
		
		parameters.put("Ruc",
				ReportLibroEstadosFinancierosData.selectRucOrg(this, strOrg));
		parameters.put("organizacion", ReportLibroEstadosFinancierosData
				.selectSocialName(this, strOrg));

		// parameters.put("dateFrom", StringToDate(strDateFrom));
		// parameters.put("dateTo", StringToDate(strDateTo));
		parameters.put("dateFrom", StringToDate(strDateFrom));
		parameters.put("dateTo", StringToDate(strDateTo));
		renderJR(vars, response, strReportName, nombreArchivo,
				"pdf", parameters, data, null);
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
		return "Servlet ReportLibroEstadosFinancieros. This Servlet was made by Pablo Sarobe modified by everybody";
	} // end of getServletInfo() method
}
