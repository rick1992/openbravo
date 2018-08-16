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
package pe.com.unifiedgo.report.ad_reports;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
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
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.xmlEngine.XmlDocument;

//import com.sun.org.apache.bcel.internal.generic.IDIV;

import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

//import pe.com.unifiedgo.warehouse.ad_reports.GenerateAnaliticKardexData;

public class ReporteMovimientosValorizado extends HttpSecureAppServlet {
	private static final long serialVersionUID = 1L;
	private static String strBDErrorMessage = "";

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		VariablesSecureApp vars = new VariablesSecureApp(request);

		if (vars.commandIn("DEFAULT")) {
			String mProductId = vars.getGlobalVariable("inpmProductId",
					"ReporteMovimientosValorizado|M_Product_Id", "");
			String strDocDate = vars.getGlobalVariable("inpDocDate",
					"ReporteMovimientosValorizado|docDate",
					SREDateTimeData.FirstDayOfMonth(this));

			String strDateto = vars
					.getGlobalVariable("inpDateTo",
							"ReporteMovimientosValorizado|inpDateTo",
							SREDateTimeData.today(this));
			String strNumMonths = vars.getGlobalVariable("inpNumMonths",
					"ReporteMovimientosValorizado|NumMonths", "");
			String strOrg = vars.getGlobalVariable("inpOrg",
					"GeneralLedgerJournal|Org", "0");
			String strWarehouse = vars.getRequestGlobalVariable(
					"inpmWarehouseId", "ReporteMovimientosValorizado|M_Warehouse_ID");
			String strmProductLineID = vars
					.getStringParameter("inpProductLine");

			printPageDataSheet(request, response, vars, mProductId, strDocDate,
					strNumMonths, strOrg, strDateto, strWarehouse,
					strmProductLineID);

		} else if (vars.commandIn("LISTAR")) {
			String mProductId = vars.getRequestGlobalVariable("inpmProductId",
					"ReporteMovimientosValorizado|M_Product_Id");
			String strDocDate = vars.getStringParameter("inpDocDate");
			String strDateTo = vars.getStringParameter("inpDateTo");
			String strNumMonths = vars.getGlobalVariable("inpNumMonths",
					"ReporteMovimientosValorizado|NumMonths", "");
			String strOrg = vars.getStringParameter("inpadOrgId");
			String strWarehouse = vars.getRequestGlobalVariable(
					"inpmWarehouseId", "ReporteMovimientosValorizado|M_Warehouse_ID");
			String strmProductLineID = vars
					.getStringParameter("inpProductLine");
			printPageDataSheet(request, response, vars, mProductId, strDocDate,
					strNumMonths, strOrg, strDateTo, strWarehouse,
					strmProductLineID);

		} else if (vars.commandIn("PDF", "XLS")) {
			if (log4j.isDebugEnabled())
				log4j.debug("PDF");

			String strDateFrom = vars.getStringParameter("inpDocDate");

			String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
					"ReportLibroEstadosFinancieros|DateTo");

			String strOrg = vars.getStringParameter("inpadOrgId");
			String strTipoGrupo = vars.getStringParameter("inpTipoGrupo");

			String strIdAlmacen = vars.getRequestGlobalVariable(
					"inpmWarehouseId",
					"ReporteGenerateAnaliticKardex|M_Warehouse_ID");

			String strGrupoxTercero = vars.getStringParameter("agruparTercero");
			String strImprimirValorizado = vars
					.getStringParameter("imprimirValorizado");

			String strTable = vars.getStringParameter("inpTable");
			String strRecord = vars.getStringParameter("inpRecord");
			String strTipoMovimiento = vars
					.getStringParameter("inpTipoMovimiento");

			setHistoryCommand(request, "DEFAULT");
			printPagePDF(request, response, vars, strDateFrom, strDateTo,
					strOrg, strTipoGrupo, strIdAlmacen, strGrupoxTercero,
					strTipoMovimiento, strImprimirValorizado, strTable,
					strRecord);

		} else
			pageError(response);
	}

	private void printPageDataSheet(HttpServletRequest request,
			HttpServletResponse response, VariablesSecureApp vars,
			String mProductId, String strDocDate, String strNumMonths,
			String strOrg, String strDateTo, String strWarehouse,
			String strIdLineaProducto) throws IOException, ServletException {
		if (log4j.isDebugEnabled())
			log4j.debug("Output: dataSheet");

		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();
		XmlDocument xmlDocument = null;

		String strConvRateErrorMsg = "";

		String discard[] = { "discard" };

		xmlDocument = xmlEngine.readXmlTemplate(
				"pe/com/unifiedgo/report/ad_reports/ReporteMovimientosValorizado",
				discard).createXmlDocument();

		if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
			OBError myMessage = null;
			myMessage = new OBError();

		}

		if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {

			ToolBar toolbar = new ToolBar(this, vars.getLanguage(),
					"ReporteMovimientosValorizado", false, "", "",
					"", false, "ad_reports",
					strReplaceWith, false, true);
			toolbar.setEmail(false);
			toolbar.prepareSimpleToolBarTemplate();
			toolbar.prepareRelationBarTemplate(false, false,
					"imprimirXLS();return false;");
			xmlDocument.setParameter("toolbar", toolbar.toString());

			try {
				WindowTabs tabs = new WindowTabs(this, vars,
						"pe.com.unifiedgo.report.ad_reports.ReporteMovimientosValorizado");
				xmlDocument.setParameter("parentTabContainer",
						tabs.parentTabs());
				xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
				xmlDocument.setParameter("childTabContainer", tabs.childTabs());
				xmlDocument.setParameter("theme", vars.getTheme());
				NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
						"ReporteMovimientosValorizado.html", classInfo.id,
						classInfo.type, strReplaceWith, tabs.breadcrumb());
				xmlDocument.setParameter("navigationBar", nav.toString());
				LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
						"ReporteMovimientosValorizado.html", strReplaceWith);
				xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
			} catch (Exception ex) {
				throw new ServletException(ex);
			}
			{
				OBError myMessage = vars.getMessage("ReporteMovimientosValorizado");
				vars.removeMessage("ReporteMovimientosValorizado");
				if (myMessage != null) {
					xmlDocument
							.setParameter("messageType", myMessage.getType());
					xmlDocument.setParameter("messageTitle",
							myMessage.getTitle());
					xmlDocument.setParameter("messageMessage",
							myMessage.getMessage());
				}
			}

			try {
				ComboTableData comboTableData = new ComboTableData(vars, this,
						"TABLEDIR", "AD_ORG_ID", "",
						"0C754881EAD94243A161111916E9B9C6", Utility.getContext(
								this, vars, "#AccessibleOrgTree",
								"ReporteMovimientosValorizado"), Utility.getContext(this,
								vars, "#User_Client", "ReporteMovimientosValorizado"), 0);
				// comboTableData.fillParameters(null,
				// "ReporteMovimientosValorizado", "");
				Utility.fillSQLParameters(this, vars, null, comboTableData,
						"ReporteMovimientosValorizado", strOrg);
				xmlDocument.setData("reportAD_Org_ID", "liststructure",
						comboTableData.select(false));
			} catch (Exception ex) {
				throw new ServletException(ex);
			}

			xmlDocument.setParameter("adOrgId", strOrg);

			xmlDocument.setParameter("calendar",
					vars.getLanguage().substring(0, 2));
			xmlDocument.setParameter("language",
					"defaultLang=\"" + vars.getLanguage() + "\";");
			xmlDocument.setParameter("directory", "var baseDirectory = \""
					+ strReplaceWith + "/\";\n");
			xmlDocument.setParameter("docDate", strDocDate);
			xmlDocument.setParameter("docDateto", strDateTo);
			xmlDocument.setParameter("docDatedisplayFormat",
					vars.getSessionValue("#AD_SqlDateFormat"));
			xmlDocument.setParameter("docDatesaveFormat",
					vars.getSessionValue("#AD_SqlDateFormat"));

			try {
				ComboTableData comboTableData = new ComboTableData(vars, this,
						"LIST", "", "9A4C02741FAD4E5DAE696E0CBE279F9E", "",
						Utility.getContext(this, vars, "#AccessibleOrgTree",
								"ReporteMovimientosValorizado"), Utility.getContext(this,
								vars, "#User_Client", "ReporteMovimientosValorizado"), 0);
				Utility.fillSQLParameters(this, vars, null, comboTableData,
						"ReporteMovimientosValorizado", "");
				xmlDocument.setData("reportNumMonths", "liststructure",
						comboTableData.select(false));
				comboTableData = null;
			} catch (Exception ex) {
				throw new ServletException(ex);
			}

//			try {
//
//				FieldProvider tipos[] = new FieldProvider[2];
//
//				Vector<Object> vector = new Vector<Object>(0);
//
//				SQLReturnObject sqlReturnObject = new SQLReturnObject();
//				sqlReturnObject.setData("ID", "D");
//				sqlReturnObject.setData("NAME", "DOCUMENTO");
//				vector.add(sqlReturnObject);
//
//				sqlReturnObject = new SQLReturnObject();
//				sqlReturnObject.setData("ID", "P");
//				sqlReturnObject.setData("NAME", "PRODUCTO");
//				vector.add(sqlReturnObject);
//
//				vector.copyInto(tipos);
//
//				xmlDocument.setData("reportTipoGrupo", "liststructure", tipos);
//			} catch (Exception ex) {
//				throw new ServletException(ex);
//			}

			// PARA EL TIPO DE MOVIMIENTO

			try {

				ReporteMovimientosValorizadoData[] datax = ReporteMovimientosValorizadoData
						.select_tipo_movimientos(this);
				FieldProvider tiposMovimientos[] = new FieldProvider[datax.length + 1];
				Vector<Object> vector = new Vector<Object>(0);

				SQLReturnObject sqlReturnObjectIni = new SQLReturnObject();
				sqlReturnObjectIni.setData("ID", "");
				sqlReturnObjectIni.setData("NAME", "");
				vector.add(sqlReturnObjectIni);

				for (int i = 0; i < datax.length; i++) {
					SQLReturnObject sqlReturnObject = new SQLReturnObject();
					sqlReturnObject.setData("ID", datax[i].comboitemid);
					sqlReturnObject.setData("NAME", datax[i].movementtype);
					vector.add(sqlReturnObject);
				}
				vector.copyInto(tiposMovimientos);

				xmlDocument.setData("reportTipoMovimiento", "liststructure",
						tiposMovimientos);

			} catch (Exception ex) {
				throw new ServletException(ex);
			}
//
//			try {
//
//				FieldProvider tipos[] = new FieldProvider[2];
//
//				Vector<Object> vector = new Vector<Object>(0);
//
//				SQLReturnObject sqlReturnObject = new SQLReturnObject();
//				sqlReturnObject.setData("ID", "D");
//				sqlReturnObject.setData("NAME", "DOCUMENTO");
//				vector.add(sqlReturnObject);
//
//				sqlReturnObject = new SQLReturnObject();
//				sqlReturnObject.setData("ID", "P");
//				sqlReturnObject.setData("NAME", "PRODUCTO");
//				vector.add(sqlReturnObject);
//
//				vector.copyInto(tipos);
//
//				xmlDocument.setData("reportTipoGrupo", "liststructure", tipos);
//			} catch (Exception ex) {
//				throw new ServletException(ex);
//			}

			//
			xmlDocument.setParameter("mWarehouseId", strWarehouse);
			xmlDocument
					.setParameter("mWarehouseDescription",
							ReporteMovimientosValorizadoData.selectMWarehouse(this,
									strWarehouse));

			// Print document in the output
			out.println(xmlDocument.print());
			out.close();
		}
	}

	private void printPagePDF(HttpServletRequest request,
			HttpServletResponse response, VariablesSecureApp vars,
			String strDateFrom, String strDateTo, String strOrg,
			String strTipoGrupo, String strIdAlmacen, String strGrupoxTercero,
			String strTipoMovimiento, String strImprimirValorizado,
			String strTable, String strRecord) throws IOException,
			ServletException {

		ReporteMovimientosValorizadoData[] data = null;

		String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
		String strOrgFamily = getFamily(strTreeOrg, strOrg);
		
		strImprimirValorizado="Y";

//		if (strGrupoxTercero.equals("Y")) {
//
//			if (strTipoGrupo.compareTo("P") == 0) {
//				data = ReporteMovimientosValorizadoData.select_por_producto_conProveedor(
//						this, strDateFrom, strDateTo, Utility.getContext(this,
//								vars, "#User_Client", "ReporteMovimientosValorizado"),
//						strOrgFamily, strIdAlmacen, strTipoMovimiento);
//
//			}
//			if (strTipoGrupo.compareTo("D") == 0) {
//				data = ReporteMovimientosValorizadoData
//						.select_por_documento_conProveedor(this, strDateFrom,
//								strDateTo, Utility.getContext(this, vars,
//										"#User_Client", "ReporteMovimientosValorizado"),
//								strOrgFamily, strIdAlmacen, strTipoMovimiento);
//			}
//
//		} else {
//
//			if (strTipoGrupo.compareTo("P") == 0) {
//				data = ReporteMovimientosValorizadoData.select_por_producto(this,
//						strDateFrom, strDateTo, strDateFrom, strDateTo, Utility
//								.getContext(this, vars, "#User_Client",
//										"ReporteMovimientosValorizado"), strOrgFamily,
//						strIdAlmacen, strTipoMovimiento);
//
//			}
//			if (strTipoGrupo.compareTo("D") == 0) {
//				data = ReporteMovimientosValorizadoData.select_por_documento(this,
//						strDateFrom, strDateTo, strDateFrom, strDateTo, Utility
//								.getContext(this, vars, "#User_Client",
//										"ReporteMovimientosValorizado"), strOrgFamily,
//						strIdAlmacen, strTipoMovimiento);
//			}
//
//		}
		
		if (strImprimirValorizado.equals("Y")){
			data=null;
			data = ReporteMovimientosValorizadoData.select_movimientos_valorizado(this,
					strDateFrom, strDateTo, strDateFrom, strDateTo, Utility
							.getContext(this, vars, "#User_Client",
									"ReporteMovimientosValorizado"), strOrgFamily,
					strIdAlmacen, strTipoMovimiento);
		}


		if (data == null || data.length == 0) {
			advisePopUp(
					request,
					response,
					"WARNING",
					Utility.messageBD(this, "ProcessStatus-W",
							vars.getLanguage()),
					Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
			return;
		}

		String direccionAlmacen = "";
		String nombreAlmacen = "";

		HashMap<String, Object> parameters = new HashMap<String, Object>();

		parameters.put("Ruc",
				ReportLibroEstadosFinancierosData.selectRucOrg(this, strOrg));
		parameters.put("organizacion", ReportLibroEstadosFinancierosData
				.selectSocialName(this, strOrg));
		parameters.put("nombreAlmacen", nombreAlmacen);
		parameters.put("direccionAlmacen", direccionAlmacen);

		parameters.put("dateFrom", StringToDate(strDateFrom));
		parameters.put("dateTo", StringToDate(strDateTo));

		parameters.put("tipoGrupo", strTipoGrupo);
		parameters.put("agrupadoTercero", strGrupoxTercero);

		String nombreArchivo = "Reporte de Movimientos Valorizado";

		String strOutput;
		String strReportName;
		if (vars.commandIn("PDF")) {
			strOutput = "pdf";
			strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteMovimientosValorizado.jrxml";
		} else {
			strOutput = "xls";

			if (strImprimirValorizado.equals("Y"))
				strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteMovimientosExcelValorizado.jrxml";

			else
				strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteMovimientosValorizadoExcel.jrxml";
		}

		renderJR(vars, response, strReportName, nombreArchivo, strOutput,
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

	private String getFamily(String strTree, String strChild)
			throws IOException, ServletException {
		return Tree.getMembers(this, strTree,
				(strChild == null || strChild.equals("")) ? "0" : strChild);

	}

	public String getMes(int month) {
		String result;
		switch (month) {
		case 0: {
			result = "Enero";
			break;
		}
		case 1: {
			result = "Febrero";
			break;
		}
		case 2: {
			result = "Marzo";
			break;
		}
		case 3: {
			result = "Abril";
			break;
		}
		case 4: {
			result = "Mayo";
			break;
		}
		case 5: {
			result = "Junio";
			break;
		}
		case 6: {
			result = "Julio";
			break;
		}
		case 7: {
			result = "Agosto";
			break;
		}
		case 8: {
			result = "Septiembre";
			break;
		}
		case 9: {
			result = "Octubre";
			break;
		}
		case 10: {
			result = "Noviembre";
			break;
		}
		case 11: {
			result = "Diciembre";
			break;
		}
		default: {
			result = "Error";
			break;
		}
		}

		return result;
	}

	public String getServletInfo() {
		return "Servlet PurchaseOrderFilter. This Servlet was made by Jon Alegría";
	} // end of getServletInfo() method

}
