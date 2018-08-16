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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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

public class ReporteGenerateAnaliticKardex extends HttpSecureAppServlet {
	private static final long serialVersionUID = 1L;
	private static String strBDErrorMessage = "";

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		VariablesSecureApp vars = new VariablesSecureApp(request);

		if (vars.commandIn("DEFAULT")) {
			String mProductId = vars.getGlobalVariable("inpmProductId", "ReporteGenerateAnaliticKardex|M_Product_Id",
					"");
			String strDocDate = vars.getGlobalVariable("inpDocDate", "ReporteGenerateAnaliticKardex|docDate",
					SREDateTimeData.FirstDayOfMonth(this));

			String strDateto = vars.getGlobalVariable("inpDateTo", "ReporteGenerateAnaliticKardex|inpDateTo",
					SREDateTimeData.today(this));
			String strNumMonths = vars.getGlobalVariable("inpNumMonths", "ReporteGenerateAnaliticKardex|NumMonths", "");
			String strOrg = vars.getGlobalVariable("inpOrg", "GeneralLedgerJournal|Org", "0");
			String strWarehouse = vars.getRequestGlobalVariable("inpmWarehouseId",
					"ReporteGenerateAnaliticKardex|M_Warehouse_ID");
			String strmProductLineID = vars.getStringParameter("inpProductLine");
			
			String strShowNegative = vars.getGlobalVariable("inpShowNegative",
		              "ReporteGenerateAnaliticKardex|ShowNegative", "N");
			

			printPageDataSheet(request, response, vars, mProductId, strDocDate, strNumMonths, strOrg, strDateto,
					strWarehouse, strmProductLineID, strShowNegative);

		} else if (vars.commandIn("LISTAR")) {
			String mProductId = vars.getRequestGlobalVariable("inpmProductId",
					"ReporteGenerateAnaliticKardex|M_Product_Id");
			String strDocDate = vars.getStringParameter("inpDocDate");
			String strDateTo = vars.getStringParameter("inpDateTo");
			String strNumMonths = vars.getGlobalVariable("inpNumMonths", "ReporteGenerateAnaliticKardex|NumMonths", "");
			String strOrg = vars.getStringParameter("inpadOrgId");
			String strWarehouse = vars.getRequestGlobalVariable("inpmWarehouseId",
					"ReporteGenerateAnaliticKardex|M_Warehouse_ID");
			String strmProductLineID = vars.getStringParameter("inpProductLine");
			String strShowNegative = vars.getGlobalVariable("inpShowNegative",
		              "ReporteGenerateAnaliticKardex|ShowNegative", "N");
			
			printPageDataSheet(request, response, vars, mProductId, strDocDate, strNumMonths, strOrg, strDateTo,
					strWarehouse, strmProductLineID, strShowNegative);

		} else if (vars.commandIn("PDF", "XLS")) {
			if (log4j.isDebugEnabled())
				log4j.debug("PDF");

			String strDateFrom = vars.getStringParameter("inpDocDate");

			String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportLibroEstadosFinancieros|DateTo");

			String strOrg = vars.getStringParameter("inpadOrgId");

			String strIdAlmacen = vars.getRequestGlobalVariable("inpmWarehouseId",
					"ReporteGenerateAnaliticKardex|M_Warehouse_ID");

			String strIdLineaProducto = vars.getStringParameter("inpProductLine");

			String strIdProducto = vars.getStringParameter("inpmProductId");

			String strTable = vars.getStringParameter("inpTable");
			String strRecord = vars.getStringParameter("inpRecord");
			setHistoryCommand(request, "DEFAULT");
			String strShowNegative = vars.getGlobalVariable("inpShowNegative",
		              "ReporteGenerateAnaliticKardex|ShowNegative", "N");
			
			printPagePDF(request, response, vars, strDateFrom, strDateTo, strOrg, strIdLineaProducto, strIdAlmacen,
					strIdProducto, strTable, strRecord, strShowNegative);

		} else
			pageError(response);
	}

	private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response, VariablesSecureApp vars,
			String mProductId, String strDocDate, String strNumMonths, String strOrg, String strDateTo,
			String strWarehouse, String strIdLineaProducto, String strShowNegative) throws IOException, ServletException {
		if (log4j.isDebugEnabled())
			log4j.debug("Output: dataSheet");

		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();
		XmlDocument xmlDocument = null;
		
		if(strShowNegative==null || strShowNegative.equals(""))
			strShowNegative = "N";

		String strConvRateErrorMsg = "";

		String discard[] = { "discard" };

		xmlDocument = xmlEngine
				.readXmlTemplate("pe/com/unifiedgo/report/ad_reports/ReporteGenerateAnaliticKardex", discard)
				.createXmlDocument();

		if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
			OBError myMessage = null;
			myMessage = new OBError();

		}

		if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {

			ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReporteGenerateAnaliticKardex", false, "", "",
					"imprimir();return false;", false, "ad_reports", strReplaceWith, false, true);
			toolbar.setEmail(false);
			toolbar.prepareSimpleToolBarTemplate();
			toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");
			xmlDocument.setParameter("toolbar", toolbar.toString());

			try {
				WindowTabs tabs = new WindowTabs(this, vars,
						"pe.com.unifiedgo.report.ad_reports.ReporteGenerateAnaliticKardex");
				xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
				xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
				xmlDocument.setParameter("childTabContainer", tabs.childTabs());
				xmlDocument.setParameter("theme", vars.getTheme());
				NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReporteGenerateAnaliticKardex.html",
						classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
				xmlDocument.setParameter("navigationBar", nav.toString());
				LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReporteGenerateAnaliticKardex.html",
						strReplaceWith);
				xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
			} catch (Exception ex) {
				throw new ServletException(ex);
			}
			{
				OBError myMessage = vars.getMessage("ReporteGenerateAnaliticKardex");
				vars.removeMessage("ReporteGenerateAnaliticKardex");
				if (myMessage != null) {
					xmlDocument.setParameter("messageType", myMessage.getType());
					xmlDocument.setParameter("messageTitle", myMessage.getTitle());
					xmlDocument.setParameter("messageMessage", myMessage.getMessage());
				}
			}

			try {
				ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
						"0C754881EAD94243A161111916E9B9C6",
						Utility.getContext(this, vars, "#AccessibleOrgTree", "ReporteGenerateAnaliticKardex"),
						Utility.getContext(this, vars, "#User_Client", "ReporteGenerateAnaliticKardex"), 0);
				// comboTableData.fillParameters(null,
				// "ReporteGenerateAnaliticKardex", "");
				Utility.fillSQLParameters(this, vars, null, comboTableData, "ReporteGenerateAnaliticKardex", strOrg);
				xmlDocument.setData("reportAD_Org_ID", "liststructure", comboTableData.select(false));
			} catch (Exception ex) {
				throw new ServletException(ex);
			}

			xmlDocument.setParameter("adOrgId", strOrg);

			// //////////// para las lienas de productos

			// ReporteGenerateAnaliticKardexData[] datax =
			// ReporteGenerateAnaliticKardexData
			// .select_linea_producto(this);
			// FieldProvider lineaProducto[] = new FieldProvider[datax.length];
			// Vector<Object> vector = new Vector<Object>(0);
			//
			// for (int i = 0; i < datax.length; i++) {
			// SQLReturnObject sqlReturnObject = new SQLReturnObject();
			// sqlReturnObject.setData("ID", datax[i].idLineaProducto);
			// sqlReturnObject.setData("NAME", datax[i].lineaProducto);
			// vector.add(sqlReturnObject);
			// }
			// vector.copyInto(lineaProducto);
			//
			// xmlDocument.setData("reportC_LINEA_PRODUCTO", "liststructure",
			// lineaProducto);

			// /////////////fin lineas de productos

			// para cargar la variables javascrip de linea de producto
			// xmlDocument.setParameter("paramLineaProductoArray", Utility
			// .arrayInfinitasEntradas(
			// "idOrganizacion;idLineaProducto;lineaProducto",
			// "arrLineaProductos",
			// ReporteGenerateAnaliticKardexData
			// .select_linea_producto(this)));
			//
			// System.out.println(Utility.arrayInfinitasEntradas(
			// "idOrganizacion;idLineaProducto;lineaProducto",
			// "arrLineaProductos", ReporteGenerateAnaliticKardexData
			// .select_linea_producto(this)));
			// FIN para cargar la variables javascrip de linea producto

			// try {
			// ComboTableData comboTableData = new ComboTableData(vars, this,
			// "TABLEDIR", "M_Warehouse_ID", "", "",
			// Utility.getContext(this, vars, "#AccessibleOrgTree",
			// "ReporteGenerateAnaliticKardex"),
			// Utility.getContext(this, vars, "#User_Client",
			// "ReporteGenerateAnaliticKardex"), 0);
			// Utility.fillSQLParameters(this, vars, null, comboTableData,
			// "ReporteGenerateAnaliticKardex", strWarehouse);
			// xmlDocument.setData("reportM_Warehouse_ID", "liststructure",
			// comboTableData.select(false));
			// comboTableData = null;
			// } catch (Exception ex) {
			// throw new ServletException(ex);
			// }

			xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
			xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
			xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
			xmlDocument.setParameter("docDate", strDocDate);
			xmlDocument.setParameter("docDateto", strDateTo);
			xmlDocument.setParameter("docDatedisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
			xmlDocument.setParameter("docDatesaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

			// xmlDocument.setParameter("mProduct", mProductId);
			// xmlDocument
			// .setParameter(
			// "warehouseArray",
			// Utility.arrayInfinitasEntradas(
			// "idOrganizacion;idAlmacen;warehousename",
			// "arrWarehouse",
			// ReporteGenerateAnaliticKardexData
			// .selectWarehouseDouble(
			// this,
			// Utility.getContext(this,
			// vars,
			// "#User_Client",
			// "GenerateAnaliticKardex"),
			// Utility.getContext(this,
			// vars,
			// "#User_Client",
			// "GenerateAnaliticKardex"))));
			//
			// xmlDocument.setParameter("mWarehouseId", strWarehouse);

			try {
				ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
						"9A4C02741FAD4E5DAE696E0CBE279F9E", "",
						Utility.getContext(this, vars, "#AccessibleOrgTree", "ReporteGenerateAnaliticKardex"),
						Utility.getContext(this, vars, "#User_Client", "ReporteGenerateAnaliticKardex"), 0);
				Utility.fillSQLParameters(this, vars, null, comboTableData, "ReporteGenerateAnaliticKardex", "");
				xmlDocument.setData("reportNumMonths", "liststructure", comboTableData.select(false));
				comboTableData = null;
			} catch (Exception ex) {
				throw new ServletException(ex);
			}

			xmlDocument.setParameter("ProductLine", strIdLineaProducto);
			xmlDocument.setParameter("productLineDescription",
					ReporteGenerateAnaliticKardexData.selectPrdcProductgroup(this, strIdLineaProducto));

			xmlDocument.setParameter("paramProductId", mProductId);
			xmlDocument.setParameter("paramProductDescription",
					ReporteGenerateAnaliticKardexData.selectMproduct(this, mProductId));

			xmlDocument.setParameter("mWarehouseId", strWarehouse);
			xmlDocument.setParameter("mWarehouseDescription",
					ReporteGenerateAnaliticKardexData.selectMWarehouse(this, strWarehouse));
			
			
			
			
			xmlDocument.setParameter("paramShowNegative", strShowNegative);

			// Print document in the output
			out.println(xmlDocument.print());
			out.close();
		}
	}

	private void printPagePDF(HttpServletRequest request, HttpServletResponse response, VariablesSecureApp vars,
			String strDateFrom, String strDateTo, String strOrg, String strIdlineaProducto, String strIdalmacen,
			String strIdProducto, String strTable, String strRecord, String strShowNegative) throws IOException, ServletException {

		if(strShowNegative==null || strShowNegative.equals(""))
			strShowNegative = "N";
		
		ReporteGenerateAnaliticKardexData[] data = null;
		ReporteGenerateAnaliticKardexData[] dataSaldos = null;
		ReporteGenerateAnaliticKardexData[] dataFinal = null;

		ArrayList<ReporteGenerateAnaliticKardexData> listaSaldos = null;
		ArrayList<ReporteGenerateAnaliticKardexData> listaMovimientos = new ArrayList<ReporteGenerateAnaliticKardexData>();

		String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
		String strOrgFamily = getFamily(strTreeOrg, strOrg);

		data = ReporteGenerateAnaliticKardexData.select_detalle_stock_fisico_x_fecha(this,
				Utility.getContext(this, vars, "#User_Client", "ReporteGenerateAnaliticKardex"), strOrgFamily,
				strIdalmacen, strIdlineaProducto, strIdProducto, strDateFrom,
				DateTimeData.nDaysAfter(this, strDateTo, "1"));

		dataSaldos = ReporteGenerateAnaliticKardexData.select_saldo_inicial(this,
				Utility.getContext(this, vars, "#User_Client", "ReporteGenerateAnaliticKardex"), strOrgFamily,
				strIdalmacen, strIdlineaProducto, strIdProducto,strDateFrom);

		listaSaldos = new ArrayList<ReporteGenerateAnaliticKardexData>(Arrays.asList(dataSaldos));

		if (data.length == 0 && dataSaldos.length==0) {
			advisePopUp(request, response, "WARNING", Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
					Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
			return;
		}
		
		String ant_value_linea_producto=null;
		String ant_id_almacen=null;


		for (int i = 0; i < data.length; i++) {
			ReporteGenerateAnaliticKardexData obj = data[i];

			// hacemos otra consulta para saber el saldo del producto
			ReporteGenerateAnaliticKardexData objetoSaldoInicial = getSaldo(obj.idAlmacen, obj.valueLineaProducto,obj.valueProducto, listaSaldos);
					
			obj.saldoInicial = objetoSaldoInicial.saldoInicial;
			obj.saldoFinal = new BigDecimal(obj.saldoInicial)
					.add(new BigDecimal(obj.entrada).subtract(new BigDecimal(obj.salida)))
					.setScale(2, RoundingMode.HALF_UP).toString();
			
			
			if( ant_value_linea_producto!=null && ant_id_almacen!=null && (!obj.valueLineaProducto.equals(ant_value_linea_producto) ||
					!obj.idAlmacen.equals(ant_id_almacen))){
				if(!obj.idAlmacen.equals(ant_id_almacen)){
					agregaSaldos(ant_id_almacen, ant_value_linea_producto, listaSaldos, listaMovimientos);
					agregaSaldos(ant_id_almacen, null, listaSaldos, listaMovimientos);
				}else{
					agregaSaldos(ant_id_almacen, ant_value_linea_producto, listaSaldos, listaMovimientos);
				}
			}
			ant_value_linea_producto=obj.valueLineaProducto;
			ant_id_almacen=obj.idAlmacen;

			listaMovimientos.add(obj);
		}
		
		agregaSaldos(null, null, listaSaldos, listaMovimientos);

		
		
		
		//ADD validacion Vafaster
		//Solo agregue esta parte de codigo para validar cuando solo nos solicite mostrar negativos
		//asi no tocar el código original
		 if(strShowNegative.equals("Y")){
			ArrayList<ReporteGenerateAnaliticKardexData> listaMovimientosNegativos = new ArrayList<ReporteGenerateAnaliticKardexData>();
			for(int i = 0;i<listaMovimientos.size();i++){
	  		  if((new BigDecimal(listaMovimientos.get(i).saldoFinal)).compareTo(BigDecimal.ZERO)>=0)
					continue;
	  		  listaMovimientosNegativos.add(listaMovimientos.get(i));
			}
			listaMovimientos = listaMovimientosNegativos;
		 }	
		 //End Validacion Vafaster
		
		dataFinal=new ReporteGenerateAnaliticKardexData[listaMovimientos.size()];
		
		for(int i = 0;i<listaMovimientos.size();i++){
			dataFinal[i]=listaMovimientos.get(i);
		}

		String direccionAlmacen = "--";
		String nombreAlmacen = "Todos los Almacenes";

		if (strIdalmacen.compareToIgnoreCase("") != 0) {
			// String IdFindAlmacen = data[0].idAlmacen;
			Warehouse wh = OBDal.getInstance().get(Warehouse.class, strIdalmacen);
			direccionAlmacen = wh.getLocationAddress().getAddressLine1();
			nombreAlmacen = wh.getName();
		}

		HashMap<String, Object> parameters = new HashMap<String, Object>();

		parameters.put("Ruc", ReportLibroEstadosFinancierosData.selectRucOrg(this, strOrg));
		parameters.put("organizacion", ReportLibroEstadosFinancierosData.selectSocialName(this, strOrg));
		parameters.put("nombreAlmacen", nombreAlmacen);
		parameters.put("direccionAlmacen", direccionAlmacen);

		parameters.put("dateFrom", StringToDate(strDateFrom));
		parameters.put("dateTo", StringToDate(strDateTo));
		
		
		

		String nombreArchivo = "Stock_x_fecha";

		String strOutput;
		String strReportName;
		if (vars.commandIn("PDF")) {
			strOutput = "pdf";
			strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteGenerateAnaliticKardex.jrxml";
		} else {
			strOutput = "xls";
			strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteGenerateAnaliticKardexExcel.jrxml";
		}

		renderJR(vars, response, strReportName, nombreArchivo, strOutput, parameters, dataFinal, null);
	}
	
	private void agregaSaldos(String idAlmacen,String  valueLineaProducto,ArrayList<ReporteGenerateAnaliticKardexData> listaSaldos,
			ArrayList<ReporteGenerateAnaliticKardexData> listaMovimientos ){
		
		Iterator<ReporteGenerateAnaliticKardexData> it = listaSaldos.iterator();
		
		if(valueLineaProducto==null && idAlmacen==null){
			while (it.hasNext()) {
				ReporteGenerateAnaliticKardexData o = it.next();
					o.saldoFinal=o.saldoInicial;
					o.entrada="0.00";
					o.salida="0.00";
					listaMovimientos.add(o);
					it.remove();
			}
		}else {
			if(valueLineaProducto==null){
				while (it.hasNext()) {
					ReporteGenerateAnaliticKardexData o = it.next();
					if (o.idAlmacen.equals(idAlmacen)  ) {
						o.saldoFinal=o.saldoInicial;
						o.entrada="0.00";
						o.salida="0.00";
						listaMovimientos.add(o);
						it.remove();
					}
				}
			}else{
				while (it.hasNext()) {
					ReporteGenerateAnaliticKardexData o = it.next();
					if (o.idAlmacen.equals(idAlmacen)&&o.valueLineaProducto.equals(valueLineaProducto) ) {
						o.saldoFinal=o.saldoInicial;
						o.entrada="0.00";
						o.salida="0.00";
						listaMovimientos.add(o);
						it.remove();
					}
				}
			}
		}
	}

	private ReporteGenerateAnaliticKardexData getSaldo(String almacen_id, String value_linea_producto ,String value_producto,
			ArrayList<ReporteGenerateAnaliticKardexData> listaSaldos) {

		Iterator<ReporteGenerateAnaliticKardexData> it = listaSaldos.iterator();
		while (it.hasNext()) {
			ReporteGenerateAnaliticKardexData obj = it.next();
			if (obj.idAlmacen.equals(almacen_id) &&obj.valueLineaProducto.equals(value_linea_producto) && obj.valueProducto.equals(value_producto)) {
				it.remove();
				return obj;
			}
		}

		ReporteGenerateAnaliticKardexData obj = new ReporteGenerateAnaliticKardexData();
		obj.idAlmacen = almacen_id;
		obj.valueProducto = value_producto;
		obj.saldoInicial = "0.0000";

		return obj;
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

	private String getFamily(String strTree, String strChild) throws IOException, ServletException {
		return Tree.getMembers(this, strTree, (strChild == null || strChild.equals("")) ? "0" : strChild);

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
