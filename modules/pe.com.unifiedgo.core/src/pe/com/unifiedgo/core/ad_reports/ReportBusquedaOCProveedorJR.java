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
 * All portions are Copyright (C) 2001-2010 Openbravo SLU 
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package pe.com.unifiedgo.core.ad_reports;

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
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SQLReturnObject;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class ReportBusquedaOCProveedorJR extends HttpSecureAppServlet {
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		VariablesSecureApp vars = new VariablesSecureApp(request);

		// HttpSession session = request.getSession(true);
		// @SuppressWarnings("rawtypes")
		// Enumeration e = session.getAttributeNames();
		// while (e.hasMoreElements()) {
		// String name = (String) e.nextElement();
		// System.out.println("name: " + name + " - value: " +
		// session.getAttribute(name));
		// }

		if (vars.commandIn("DEFAULT")) {
			String strDocDate = vars.getGlobalVariable("inpDocDate",
					"ReportBusquedaOCProveedorJR|docDate",
					SREDateTimeData.FirstDayOfMonth(this));
			String strProyDate = vars.getGlobalVariable("inpProyDate",
					"ReportBusquedaOCProveedorJR|proyDate",
					SREDateTimeData.tomorrow(this));
			String strAD_Org_ID = vars.getGlobalVariable("inpadOrgId",
					"ReportBusquedaOCProveedorJR|AD_Org_ID", "");
			String mProductId = vars.getGlobalVariable("inpmProductId",
					"ReportBusquedaOCProveedorJR|M_Product_Id", "");
			String strmProductLineID = vars
					.getStringParameter("inpProductLine");
			String strcBpartnetId = vars.getGlobalVariable("inpcBPartnerId",
					"ReporteComprasPorProveedor|CB_PARTNER_ID", "");
			
			String strcProjectId = vars.getStringParameter("inpcProjectId");

			printPageDataSheet(request, response, vars, strAD_Org_ID,
					strDocDate, strProyDate, mProductId, strmProductLineID,
					strcBpartnetId, "", strcProjectId, true);
		} else if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {

			String strDocDate = vars.getStringParameter("inpDocDate");
			String strProyDate = vars.getStringParameter("inpProyDate");
			String strAD_Org_ID = vars.getStringParameter("inpadOrgId");
			String mProductId = vars.getRequestGlobalVariable("inpmProductId",
					"ReportBusquedaOCProveedorJR|M_Product_Id");
			String strmProductLineID = vars
					.getStringParameter("inpProductLine");
			String strcBpartnetId = vars.getStringParameter("inpcBPartnerId");
			String strPeriodo = vars.getStringParameter("inpPeriodo");
			
			String strcProjectId = vars.getStringParameter("inpcProjectId");

			printPageDataSheet(request, response, vars, strAD_Org_ID,
					strDocDate, strProyDate, mProductId, strmProductLineID,
					strcBpartnetId, strPeriodo, strcProjectId, false);

		} else if (vars.commandIn("PDF", "XLS")) {

			String strDocDate = vars.getStringParameter("inpDocDate");
			String strProyDate = vars.getStringParameter("inpProyDate");
			String strAD_Org_ID = vars.getStringParameter("inpadOrgId");
			String mProductId = vars.getRequestGlobalVariable("inpmProductId",
					"ReportBusquedaOCProveedorJR|M_Product_Id");
			String strmProductLineID = vars
					.getStringParameter("inpProductLine");
			String strcBpartnetId = vars.getStringParameter("inpcBPartnerId");
			
			String strcProjectId = vars.getStringParameter("inpcProjectId");

			printPagePDF(response, vars, strAD_Org_ID, strDocDate, strProyDate,
					mProductId, strmProductLineID, strcBpartnetId, strcProjectId);
		}

		else
			pageError(response);
	}

	private void printPageDataSheet(HttpServletRequest request,
			HttpServletResponse response, VariablesSecureApp vars,
			String strAD_Org_ID, String strDocDate, String strProyDate,
			String mProductId, String strmProductLineID, String strIdProveedor,
			String strPeriodo, String strcProjectId, boolean isFirstLoad) throws IOException,
			ServletException {

		if (log4j.isDebugEnabled())
			log4j.debug("Output: dataSheet");

		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();
		XmlDocument xmlDocument = null;
		ReportBusquedaOCProveedorJRData[] data = null;
		String strConvRateErrorMsg = "";

		String discard[] = { "discard" };

		xmlDocument = xmlEngine
				.readXmlTemplate(
						"pe/com/unifiedgo/core/ad_reports/ReportBusquedaOCProveedorFilterJR",
						discard).createXmlDocument();

		if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
			OBError myMessage = null;
			myMessage = new OBError();
			try {
				String language = vars.getLanguage();
				data = ReportBusquedaOCProveedorJRData.select_trl(vars,
						vars.getSessionValue("#AD_CLIENT_ID"), strAD_Org_ID,
						mProductId, language, strDocDate, strProyDate,
						strmProductLineID, strIdProveedor, strcProjectId);

			} catch (ServletException ex) {
				myMessage = Utility.translateError(this, vars,
						vars.getLanguage(), ex.getMessage());
			}
			strConvRateErrorMsg = myMessage.getMessage();
			if (!strConvRateErrorMsg.equals("") && strConvRateErrorMsg != null) {
				advise(request,
						response,
						"ERROR",
						Utility.messageBD(this, "NoConversionRateHeader",
								vars.getLanguage()), strConvRateErrorMsg);
			} else { // Otherwise, the report is launched
				if (data == null || data.length == 0) {
					discard[0] = "selEliminar";
					data = ReportBusquedaOCProveedorJRData.set();
				} else {
					xmlDocument.setData("structure1", data);
				}
			}
		}

		else {
			if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
				discard[0] = "selEliminar";
				data = ReportBusquedaOCProveedorJRData.set();
			}
		}

		if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
			ToolBar toolbar = new ToolBar(this, vars.getLanguage(),
					"ReportBusquedaOCProveedorFilterJR", false, "", "",
					"imprimir();return false;", false, "ad_reports",
					strReplaceWith, false, true);

			toolbar.setEmail(false);
			toolbar.prepareSimpleToolBarTemplate();
			toolbar.prepareRelationBarTemplate(false, false,
					"imprimirXLS();return false;");

			xmlDocument.setParameter("toolbar", toolbar.toString());

			try {
				WindowTabs tabs = new WindowTabs(this, vars,
						"pe.com.unifiedgo.core.ad_reports.ReportBusquedaOCProveedorJR");
				xmlDocument.setParameter("parentTabContainer",
						tabs.parentTabs());
				xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
				xmlDocument.setParameter("childTabContainer", tabs.childTabs());
				xmlDocument.setParameter("theme", vars.getTheme());
				NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
						"ReportBusquedaOCProveedorFilterJR.html", classInfo.id,
						classInfo.type, strReplaceWith, tabs.breadcrumb());
				xmlDocument.setParameter("navigationBar", nav.toString());
				LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
						"ReportBusquedaOCProveedorFilterJR.html",
						strReplaceWith);
				xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
			} catch (Exception ex) {
				throw new ServletException(ex);
			}
			{
				OBError myMessage = vars
						.getMessage("ReportBusquedaOCProveedorJR");
				vars.removeMessage("ReportBusquedaOCProveedorJR");
				if (myMessage != null) {
					xmlDocument
							.setParameter("messageType", myMessage.getType());
					xmlDocument.setParameter("messageTitle",
							myMessage.getTitle());
					xmlDocument.setParameter("messageMessage",
							myMessage.getMessage());
				}
			}

			xmlDocument.setParameter("calendar",
					vars.getLanguage().substring(0, 2));
			xmlDocument.setParameter("language",
					"defaultLang=\"" + vars.getLanguage() + "\";");
			xmlDocument.setParameter("directory", "var baseDirectory = \""
					+ strReplaceWith + "/\";\n");
			xmlDocument.setParameter("docDate", strDocDate);
			xmlDocument.setParameter("docDatedisplayFormat",
					vars.getSessionValue("#AD_SqlDateFormat"));
			xmlDocument.setParameter("docDatesaveFormat",
					vars.getSessionValue("#AD_SqlDateFormat"));
			xmlDocument.setParameter("proyDate", strProyDate);
			xmlDocument.setParameter("proyDatedisplayFormat",
					vars.getSessionValue("#AD_SqlDateFormat"));
			xmlDocument.setParameter("proyDatesaveFormat",
					vars.getSessionValue("#AD_SqlDateFormat"));

			if (isFirstLoad) {
				xmlDocument.setParameter("FirstLoad", "FirstLoad = \"YES\"; ");
			} else {
				xmlDocument.setParameter("FirstLoad", "FirstLoad = \"NO\"; ");
			}

			// try {
			// ComboTableData comboTableData = new ComboTableData(vars, this,
			// "TABLE", "",
			// "FF808181320073320132009575C90074", "", Utility.getContext(this,
			// vars,
			// "#AccessibleOrgTree", "BusquedaOCProveedorJR"),
			// Utility.getContext(this, vars,
			// "#User_Client", "BusquedaOCProveedorJR"), 0);
			// Utility.fillSQLParameters(this, vars, null, comboTableData,
			// "BusquedaOCProveedorFilterJR",
			// "");
			// xmlDocument.setData("reportProductLine", "liststructure",
			// comboTableData.select(false));
			// comboTableData = null;
			// } catch (Exception ex) {
			// throw new ServletException(ex);
			// }

			// xmlDocument.setParameter(
			// "ProductLineArray",
			// Utility.arrayDobleEntrada(
			// "arrProductLine",
			// ReportBusquedaOCProveedorJRData.selectProductLineDouble(this,
			// Utility.getContext(this, vars, "#User_Client",
			// "BusquedaOCProveedorFilterJR"))));

			try {
				ComboTableData comboTableData = new ComboTableData(vars, this,
						"TABLEDIR", "AD_Org_ID", "",
						"D4DF252DEC3B44858454EE5292A8B836", Utility.getContext(
								this, vars, "#AccessibleOrgTree",
								"BusquedaOCProveedor"), Utility.getContext(
								this, vars, "#User_Client",
								"BusquedaOCProveedorJR"), ' ');
				Utility.fillSQLParameters(this, vars, null, comboTableData,
						"BusquedaOCProveedorFilterJR", strAD_Org_ID);
				xmlDocument.setData("reportAD_Org_ID", "liststructure",
						comboTableData.select(false));
				comboTableData = null;
			} catch (Exception ex) {
				throw new ServletException(ex);
			}
			xmlDocument.setParameter("adOrg", strAD_Org_ID);

			xmlDocument.setParameter("ProductLine", strmProductLineID);
			xmlDocument.setParameter("productLineDescription",
					ReportBusquedaOCProveedorJRData.selectPrdcProductgroup(
							this, strmProductLineID));

			xmlDocument.setParameter("mProduct", mProductId);
			xmlDocument.setParameter("productDescription",
					ReportBusquedaOCProveedorJRData.selectMproduct(this,
							mProductId));

			xmlDocument.setParameter("paramBPartnerId", strIdProveedor);
			xmlDocument.setParameter("paramBPartnerDescription",
					ReportBusquedaOCProveedorJRData.selectCBpartner(this,
							strIdProveedor));

			// ////////////para periodos
			// ConsultasGeneralesData[] datax = ConsultasGeneralesData
			// .select_periodos(this);
			// FieldProvider periodos[] = new FieldProvider[datax.length];
			// Vector<Object> vector = new Vector<Object>(0);
			//
			// for (int i = 0; i < datax.length; i++) {
			// SQLReturnObject sqlReturnObject = new SQLReturnObject();
			// sqlReturnObject.setData("ID", datax[i].idperiodo);
			// sqlReturnObject.setData("NAME", datax[i].periodo);
			// vector.add(sqlReturnObject);
			// }
			// vector.copyInto(periodos);
			//
			// xmlDocument.setData("reportC_PERIODO", "liststructure",
			// periodos);

			// ///////////// fin para periodos

			// para cargar la variables javascrip de periodos

			// xmlDocument
			// .setParameter(
			// "paramPeriodosArray",
			// Utility.arrayInfinitasEntradas(
			// "idperiodo;periodo;fechainicial;fechafinal;idorganizacion",
			// "arrPeriodos", ConsultasGeneralesData
			// .select_periodos(this)));

			// FIN para cargar la variables javascrip de periodos

			// xmlDocument.setParameter("adPeriodo", strPeriodo);

			// Print document in the output

			out.println(xmlDocument.print());
			out.close();
		}
	}

	private void printPagePDF(HttpServletResponse response,
			VariablesSecureApp vars, String strOrg, String strDateFrom,
			String strDateTo, String mProductId, String strmProductLineID,
			String strIdProveedor, String strcProjectId) throws IOException, ServletException {

		ReportBusquedaOCProveedorJRData[] data = null;

		String language = vars.getLanguage();
		data = ReportBusquedaOCProveedorJRData.select_trl(vars,
				vars.getSessionValue("#AD_CLIENT_ID"), strOrg, mProductId,
				language, strDateFrom, strDateTo, strmProductLineID,
				strIdProveedor, strcProjectId);

		String strOutput;
		String strReportName;
		if (vars.commandIn("PDF")) {
			strOutput = "pdf";
			 strReportName = "@basedesign@/pe/com/unifiedgo/core/ad_reports/ReporteBusquedaOCProveedorPDF.jrxml";
		} else {
			strOutput = "xls";
			 strReportName = "@basedesign@/pe/com/unifiedgo/core/ad_reports/ReporteBusquedaOCProveedor.jrxml";
		}

		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("organizacion",
				ReportBusquedaOCProveedorJRData.selectOrg(this, strOrg));

		parameters.put("Ruc",
				ReportBusquedaOCProveedorJRData.selectRucOrg(this, strOrg));

		parameters.put("ruc",
				ReportBusquedaOCProveedorJRData.selectRucOrg(this, strOrg));

		parameters.put("linea_producto", ReportBusquedaOCProveedorJRData
				.selectPrdcProductgroup(this, strmProductLineID));
		parameters.put("producto", ReportBusquedaOCProveedorJRData
				.selectMproduct(this, mProductId));

		// parameters.put("dateFrom", StringToDate(strDateFrom));
		// parameters.put("dateTo", StringToDate(strDateTo));
		parameters.put("dateFrom", StringToDate(strDateFrom));
		parameters.put("dateTo", StringToDate(strDateTo));
		renderJR(vars, response, strReportName,
				"Historial_Compras_por_Producto", strOutput, parameters, data, null);

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

	public String getServletInfo() {
		return "Servlet PurchaseOrderFilter. This Servlet was made by Jon Alegría";
	} // end of getServletInfo() method

}
