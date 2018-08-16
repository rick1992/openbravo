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
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
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
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchemaTable;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.accounting.SCO_Utils;
import pe.com.unifiedgo.accounting.StructPle;
import pe.com.unifiedgo.accounting.SunatUtil;
import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

import com.google.common.base.Function;

public class ReportValuedInventory extends HttpSecureAppServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		VariablesSecureApp vars = new VariablesSecureApp(request);

		if (log4j.isDebugEnabled())
			log4j.debug("Command: " + vars.getStringParameter("Command"));

		if (vars.commandIn("DEFAULT")) {
			String strDateFrom = vars.getGlobalVariable("inpDateFrom",
					"ReportValuedInventory|DateFrom",
					SREDateTimeData.FirstDayOfMonth(this));
			String strDateTo = vars
					.getGlobalVariable("inpDateTo",
							"ReportValuedInventory|DateTo",
							SREDateTimeData.today(this));
			String strOrg = vars.getGlobalVariable("inpOrg",
					"ReportValuedInventory|Org", "0");
			String strRecord = vars.getGlobalVariable("inpRecord",
					"ReportValuedInventory|Record", "");
			String strTable = vars.getGlobalVariable("inpTable",
					"ReportValuedInventory|Table", "");
			String strM_Product_ID = vars.getGlobalVariable("inpmProductId",
					"ReportValuedInventory|M_Product_ID", "");
			String strmWarehouseId = vars.getGlobalVariable("inpmWarehouseId",
					"ReportValuedInventory|M_Warehouse_ID", "");
			printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg,
					strmWarehouseId, strM_Product_ID, strTable, strRecord);

		} else if (vars.commandIn("DIRECT")) {
			String strTable = vars.getGlobalVariable("inpTable",
					"ReportValuedInventory|Table");
			String strRecord = vars.getGlobalVariable("inpRecord",
					"ReportValuedInventory|Record");
			String strM_Product_ID = vars.getRequestGlobalVariable(
					"inpmProductId", "ReportValuedInventory|M_Product_ID");
			String strmWarehouseId = vars.getRequestGlobalVariable(
					"inpmWarehouseId", "ReportValuedInventory|M_Warehouse_ID");

			setHistoryCommand(request, "DIRECT");
			vars.setSessionValue("ReportValuedInventory.initRecordNumber", "0");
			printPageDataSheet(response, vars, "", "", "", strmWarehouseId,
					strM_Product_ID, strTable, strRecord);

		} else if (vars.commandIn("FIND")) {
			String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
					"ReportValuedInventory|DateFrom");
			String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
					"ReportValuedInventory|DateTo");
			String strOrg = vars.getGlobalVariable("inpOrg",
					"ReportValuedInventory|Org", "0");
			String strM_Product_ID = vars.getRequestGlobalVariable(
					"inpmProductId", "ReportValuedInventory|M_Product_ID");
			String strmWarehouseId = vars.getRequestGlobalVariable(
					"inpmWarehouseId", "ReportValuedInventory|M_Warehouse_ID");

			vars.setSessionValue("ReportValuedInventory.initRecordNumber", "0");
			setHistoryCommand(request, "DEFAULT");
			printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg,
					strmWarehouseId, strM_Product_ID, "", "");

		} else if (vars.commandIn("PDF", "XLS")) {
			if (log4j.isDebugEnabled())
				log4j.debug("PDF");
			String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
					"ReportValuedInventory|DateFrom");
			String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
					"ReportValuedInventory|DateTo");
			String strOrg = vars.getGlobalVariable("inpOrg",
					"ReportValuedInventory|Org", "0");

			String strTable = vars.getStringParameter("inpTable");
			String strRecord = vars.getStringParameter("inpRecord");

			String strM_Product_ID = vars.getRequestGlobalVariable(
					"inpmProductId", "ReportValuedInventory|M_Product_ID");
			String strmWarehouseId = vars.getRequestGlobalVariable(
					"inpmWarehouseId", "ReportValuedInventory|M_Warehouse_ID");
			setHistoryCommand(request, "DEFAULT");
			printPagePDF(request, response, vars, strDateFrom, strDateTo,
					strOrg, strmWarehouseId, strM_Product_ID, strTable,
					strRecord);

		} else
			pageError(response);
	}

	private void printPageDataSheet(HttpServletResponse response,
			VariablesSecureApp vars, String strDateFrom, String strDateTo,
			String strOrg, String strmWarehouseId, String strM_Product_ID,
			String strTable, String strRecord) throws IOException,
			ServletException {
		if (log4j.isDebugEnabled())
			log4j.debug("Output: dataSheet");
		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();
		XmlDocument xmlDocument = null;
		ReportValuedInventoryData[] data = null;
		String strPosition = "0";
		ToolBar toolbar = new ToolBar(this, vars.getLanguage(),
				"ReportValuedInventory", false, "", "",
				"imprimir();return false;", false, "ad_reports",
				strReplaceWith, false, true);
		toolbar.setEmail(false);
		toolbar.prepareSimpleToolBarTemplate();
		toolbar.prepareRelationBarTemplate(false, false,
				"imprimirXLS();return false;");

		if (data == null || data.length == 0) {
			String discard[] = { "secTable" };

			xmlDocument = xmlEngine.readXmlTemplate(
					"pe/com/unifiedgo/report/ad_reports/ReportValuedInventory",
					discard).createXmlDocument();
			data = ReportValuedInventoryData.set("0");
			data[0].rownum = "0";
		} 
		
		xmlDocument.setParameter("toolbar", toolbar.toString());
		try {
			WindowTabs tabs = new WindowTabs(this, vars,
					"pe.com.unifiedgo.report.ad_reports.ReportValuedInventory");
			xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
			xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
			xmlDocument.setParameter("childTabContainer", tabs.childTabs());
			xmlDocument.setParameter("theme", vars.getTheme());
			NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
					"ReportValuedInventory.html", classInfo.id, classInfo.type,
					strReplaceWith, tabs.breadcrumb());
			xmlDocument.setParameter("navigationBar", nav.toString());
			LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
					"ReportValuedInventory.html", strReplaceWith);
			xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
		} catch (Exception ex) {
			throw new ServletException(ex);
		}
		{
			OBError myMessage = vars.getMessage("ReportValuedInventory");
			vars.removeMessage("ReportValuedInventory");
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
					"TABLEDIR", "AD_ORG_ID", "", "0C754881EAD94243A161111916E9B9C6",
					Utility.getContext(this, vars, "#AccessibleOrgTree",
							"ReportValuedInventory"), Utility.getContext(this,
							vars, "#User_Client", "ReportValuedInventory"), '*');
			comboTableData.fillParameters(null, "ReportValuedInventory", "");
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
		vars.setSessionValue("ReportValuedInventory|Record", strRecord);
		vars.setSessionValue("ReportValuedInventory|Table", strTable);

		xmlDocument.setParameter("paramProductId", strM_Product_ID);
		xmlDocument.setParameter("paramProductDescription",
				ReportValuedInventoryData.mProductDescription(this,
						strM_Product_ID));

		xmlDocument.setParameter("mWarehouseId", strmWarehouseId);
		xmlDocument.setParameter("mWarehouseDescription",
				ReportValuedInventoryData.mWarehouseDescription(this,
						strmWarehouseId));

		// xmlDocument.setParameter("paramMProductIDDES",
		// ReportValuedInventoryData.mProductDescription(this,
		// strM_Product_ID));
		//
		// xmlDocument.setParameter("paramMProductId", strM_Product_ID);

		// try {
		// ComboTableData comboTableData = new ComboTableData(vars, this,
		// "TABLEDIR", "M_Warehouse_ID", "", "", Utility.getContext(
		// this, vars, "#AccessibleOrgTree",
		// "ReportPurchasePlanningJR"), Utility.getContext(
		// this, vars, "#User_Client",
		// "ReportPurchasePlanningJR"), 0);
		// Utility.fillSQLParameters(this, vars, null, comboTableData,
		// "ReportPurchasePlanningJR", "");
		// xmlDocument.setData("reportM_WAREHOUSEID", "liststructure",
		// comboTableData.select(false));
		// comboTableData = null;
		// } catch (Exception ex) {
		// throw new ServletException(ex);
		// }

		// xmlDocument.setParameter("mWarehouseId", strmWarehouseId);

		xmlDocument
				.setParameter(
						"paramPeriodosArray",
						Utility.arrayInfinitasEntradas(
								"idperiodo;periodo;fechainicial;fechafinal;idorganizacion",
								"arrPeriodos",
								ReportValuedInventoryData.select_periodos(this)));

		xmlDocument.setData("structure1", data);
		out.println(xmlDocument.print());
		out.close();
	}

	// private ReportValuedInventoryData[] notshow(ReportValuedInventoryData[]
	// data,
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
	
	private static String getFamily(ConnectionProvider conn, String strTree, String strChild)
			throws IOException, ServletException {
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

	private void printPagePDF(HttpServletRequest request,
			HttpServletResponse response, VariablesSecureApp vars,
			String strDateFrom, String strDateTo, String strOrg,
			String strmWarehouseId, String strM_Product_ID, String strTable,
			String strRecord) throws IOException, ServletException {


		ArrayList<ReportValuedInventoryData> listaOrginal = new ArrayList<ReportValuedInventoryData>();


		if (strmWarehouseId.isEmpty()) { // CUANDO NO SE HA SELECCIONADO PRODUCTO

			
			listaOrginal = getData(this, vars, strDateFrom, strDateTo, Utility
					.getContext(this, vars, "#User_Client",
							"ReportValuedInventory"),strOrg, strM_Product_ID);
			
			
		}else { // CUANDO SE HA SELECCIONADO UN ALMACEN
			
			
			ReportValuedInventoryData[] dataCabecera = null;
			ReportValuedInventoryData[] dataMov = null;
			
			String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
			String strOrgFamily = getFamily(strTreeOrg, strOrg);

			String movnotUsed = "'MovimientoInterno'";

			
			String sunatCode = ReportValuedInventoryData.selectSunatCode(this,
					strmWarehouseId);
			String sunatDescripcion=ReportValuedInventoryData.selectSunatDescription(this,
					sunatCode);
			
			dataCabecera = ReportValuedInventoryData.select_cabecera_final_con_almacen(this,strDateFrom,  Utility
					.getContext(this, vars, "#User_Client",
							"ReportValuedInventory"), strOrgFamily,strM_Product_ID,sunatCode, strDateFrom, DateTimeData
							.nDaysAfter(this, strDateTo, "1"));
			
			for(int i=0 ;i<dataCabecera.length;i++){
				
				ReportValuedInventoryData header=dataCabecera[i];
				
				dataMov = ReportValuedInventoryData.selectMovimientosConAlmacen(this, Utility
						.getContext(this, vars, "#User_Client",
								"ReportValuedInventory"), strOrgFamily, movnotUsed,
						header.productId, sunatCode, strDateFrom, DateTimeData
								.nDaysAfter(this, strDateTo, "1"));
							
				 if( new BigDecimal(header.saldoFinal).compareTo(BigDecimal.ZERO)>0 || dataMov.length>0){
					 					 
					 BigDecimal costingHeader = 	costingActualProducto(this, strDateFrom,
								Utility.getContext(this, vars, "#User_Client",
										"ReportValuedInventory"), strOrgFamily,
								header.productId);
					 BigDecimal costoFinalHeader = costingHeader.multiply(new BigDecimal(header.saldoFinal));
					 
					 header.costing=costingHeader.toString();
					 header.costoFinal=costoFinalHeader.toString();
					 header.mTransactionId= null;
					header.direccion=sunatCode+" - "+sunatDescripcion;
					header.warehouseId=sunatCode;
					header.t10code = "saldo";

					 listaOrginal.add(header);
					 
					 BigDecimal saldoPartida=new BigDecimal(header.saldoFinal);
					 
					 HashMap<String, String> hMapDup = new HashMap<String, String>();
					  
					 for(int j=0;j<dataMov.length;j++){
							//PARA VERIFICAR QUE NO SE TOMEN EN CUENTA LAS TRANSACCIONES ENTRE ALMACENES
							if(dataMov[j].emScrSunatcode.equalsIgnoreCase(dataMov[j].emScrSunatcodeRef)) continue;		
							
							ReportValuedInventoryData mov=dataMov[j];
							
							String t = hMapDup.get(mov.mTransactionId);
							if(t!=null) continue; //ya existe
							hMapDup.put(mov.mTransactionId, "Y");
							
							saldoPartida=saldoPartida.add(new BigDecimal(mov.cant));
							mov.saldoFinal=saldoPartida.toString();
							mov.costoFinal=new BigDecimal(mov.costing).multiply(saldoPartida).toString();
							mov.direccion=sunatCode+" - "+sunatDescripcion;
							mov.warehouseId=sunatCode;
							listaOrginal.add(mov);
					 }
				 }
			}	
		}
		
		ReportValuedInventoryData[] finalData = new ReportValuedInventoryData[listaOrginal
				.size()];

		for (int i = 0; i < listaOrginal.size(); i++) {
			finalData[i] = listaOrginal.get(i);
		}

		

		String strSubtitle = (Utility.messageBD(this, "LegalEntity",
				vars.getLanguage()) + ": ")
				+ ReportValuedInventoryData.selectCompany(this,
						vars.getClient())
				+ "\n"
				+ "RUC:"
				+ ReportValuedInventoryData.selectRucOrg(this, strOrg) + "\n";
		;

		if (!("0".equals(strOrg)))
			strSubtitle += (Utility.messageBD(this, "OBUIAPP_Organization",
					vars.getLanguage()) + ": ")
					+ ReportValuedInventoryData.selectOrg(this, strOrg) + "\n";

		String strOutput;
		String strReportName;
		if (vars.commandIn("PDF")) {
			strOutput = "pdf";
			strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportValuedInventoryCOAM.jrxml";
		} else {
			strOutput = "xls";
			strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportValuedInventoryExcelCOAM.jrxml";
		}

		// dataHeader = ReportValuedInventoryData.selectHeader(this,
		// strM_Product_ID, strmWarehouseId);

		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("Subtitle", strSubtitle);
		// parameters.put("TaxID",
		// ReportSalesRevenueRecordsData.selectOrgTaxID(this, strOrg));
		parameters.put("Ruc",
				ReportValuedInventoryData.selectRucOrg(this, strOrg));
		parameters.put("Razon",
				ReportValuedInventoryData.selectSocialName(this, strOrg));
		parameters.put("dateFrom", StringToDate(strDateFrom));
		parameters.put("dateTo", StringToDate(strDateTo));
		// if (!strmWarehouseId.isEmpty())
		// parameters.put("warehouseGroup", "Y");
		// else
		// parameters.put("warehouseGroup", "N");

		parameters.put("warehouseGroup", "Y");

		parameters.put("totalLines", finalData.length);

		if (finalData.length == 0) {
			advisePopUp(
					request,
					response,
					"WARNING",
					Utility.messageBD(this, "ProcessStatus-W",
							vars.getLanguage()),
					Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
			return;
		}
		renderJR(vars, response, strReportName, "Inventario_Valorizado",
				strOutput, parameters, finalData, null);
	}

	public ReportValuedInventoryData SaldoInicialProducto(String strDateFrom,
			String contexto, String strOrgFamily, String strM_Product_ID,
			String strAlmacenId, ReportValuedInventoryData masFacil) {
		// agregando el saldo inicial

		ReportValuedInventoryData[] filaSaldoInicial = null;
		try {
			filaSaldoInicial = ReportValuedInventoryData.selectSaldoAnterior(
					this, strDateFrom, contexto, strOrgFamily, strM_Product_ID,
					strAlmacenId, strDateFrom);
		} catch (ServletException e) {
			e.printStackTrace();
		}

		ReportValuedInventoryData saldoInicial = masFacil;

		if (filaSaldoInicial.length == 0) {
			filaSaldoInicial = null;
		}

		saldoInicial.t10code = "saldo";

		String cantidadSaldo = filaSaldoInicial == null ? "0.00"
				: filaSaldoInicial[0].cantidadIn;
		
		String costoUnitSaldo = filaSaldoInicial == null ? "0.00"
				: filaSaldoInicial[0].costounitIn;
		
		BigDecimal costoTotalSaldo = new BigDecimal(cantidadSaldo)
		.multiply(new BigDecimal(costoUnitSaldo));
		
		saldoInicial.saldoFinal = cantidadSaldo;
		saldoInicial.costing = costoUnitSaldo;
		saldoInicial.costoFinal = costoTotalSaldo.toString();

		saldoInicial.cantidadIn = "0.00";
				
		saldoInicial.costounitIn = "0.00";

		saldoInicial.costototalIn = "0.00";

		saldoInicial.cant = cantidadSaldo;

		saldoInicial.saldoAntes = "0.00";

		saldoInicial.costo = costoTotalSaldo.toString();

		saldoInicial.costoAntes = "0.00";

		saldoInicial.costototalOut = "0.00";

		saldoInicial.costounitOut = "0.00";

		saldoInicial.cantidadOut = "0.00";

		return saldoInicial;

	}
	
	
	public static BigDecimal costingActualProducto(ConnectionProvider conn, String strDateFrom,
			String contexto, String strOrgFamily, String strM_Product_ID) {
		// agregando el saldo inicial

		BigDecimal costoProducto = new BigDecimal("0.0");

		ReportValuedInventoryData[] filaCosting = null;
		try {
			filaCosting = ReportValuedInventoryData.SelectPrecioCosting(conn,
					strDateFrom, contexto, strOrgFamily, strM_Product_ID);

		} catch (ServletException e) {
			e.printStackTrace();
		}
		if (filaCosting.length > 0)
			costoProducto = new BigDecimal(filaCosting[0].costing);

		return costoProducto;

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
	
	
	
	private static ArrayList<ReportValuedInventoryData> getData(ConnectionProvider conn, VariablesSecureApp vars,
			String strDateFrom, String strDateTo, String adUserClient, String strOrg, String strM_Product_ID) throws IOException,
			ServletException {
		
		ArrayList<ReportValuedInventoryData> listaOrginal = new ArrayList<ReportValuedInventoryData>();
		
		ReportValuedInventoryData[] dataCabecera = null;
		ReportValuedInventoryData[] dataMov = null;
		
		String strTreeOrg = TreeData.getTreeOrg(conn, vars.getClient());
		String strOrgFamily = getFamily(conn, strTreeOrg, strOrg);

		String movnotUsed = "'MovimientoInterno'";
		
		dataCabecera = ReportValuedInventoryData.select_cabecera_final_sin_almacen(conn,strDateFrom,  adUserClient, strOrgFamily,strM_Product_ID, strDateFrom, DateTimeData
				.nDaysAfter(conn, strDateTo, "1"));
		
		int sumRegistros =0;
		for(int i=0 ;i<dataCabecera.length;i++){
			
			ReportValuedInventoryData header=dataCabecera[i];
			
			dataMov = ReportValuedInventoryData.selectMovimientosSinAlmacen(conn, adUserClient, strOrgFamily, movnotUsed,
					header.productId, header.warehouseId, strDateFrom, DateTimeData
							.nDaysAfter(conn, strDateTo, "1"));
								
			 if( new BigDecimal(header.saldoFinal).compareTo(BigDecimal.ZERO)>0 || dataMov.length>0){
				 					 
				 BigDecimal costingHeader = 	costingActualProducto(conn, strDateFrom,
							adUserClient, strOrgFamily,
							header.productId);
				 BigDecimal costoFinalHeader = costingHeader.multiply(new BigDecimal(header.saldoFinal));
				 
				 header.costing=costingHeader.toString();
				 header.costoFinal=costoFinalHeader.toString();
				 header.mTransactionId=null;
				header.direccion=header.codigoAlmacen+" - "+header.almacen;
				header.t10code = "saldo";

				 listaOrginal.add(header);
				 
				 BigDecimal saldoPartida=new BigDecimal(header.saldoFinal);
				 
				 HashMap<String, String> hMapDup = new HashMap<String, String>();
				 
				 for(int j=0;j<dataMov.length;j++){
					 
						//PARA VERIFICAR QUE NO SE TOMEN EN CUENTA LAS TRANSACCIONES ENTRE ALMACENES
//						if(dataMov[j].emScrSunatcode.equalsIgnoreCase(dataMov[j].emScrSunatcodeRef)) continue;			
						
						ReportValuedInventoryData mov=dataMov[j];
						
						String t = hMapDup.get(mov.mTransactionId);
						if(t!=null) continue; //ya existe
						
						hMapDup.put(mov.mTransactionId, "Y");
						
						saldoPartida=saldoPartida.add(new BigDecimal(mov.cant));
						mov.saldoFinal=saldoPartida.toString();
						mov.costoFinal=new BigDecimal(mov.costing).multiply(saldoPartida).toString();
						
						sumRegistros++;
						listaOrginal.add(mov);
				 }
			 }
			 
			// System.out.println("Suma de registros: "+sumRegistros);
		}	
		
		
		
		for (int i = 0; i < listaOrginal.size(); i++) {

			ReportValuedInventoryData led = listaOrginal.get(i);
			
			if(led.t6code!=null && !led.t6code.equals(""))//cabecera
				continue;
			if(led.mTransactionId==null || led.mTransactionId.equals("")) continue;
			
			
			String tipoDoc = led.t10code;
			String tipoOp = led.t12code;
			led.regnumber = "";

			if(tipoDoc.equals("00") && (tipoOp.equals("01") || tipoOp.equals("05") || tipoOp.equals("02")) ){//si es de migracion que coga el valor de la guia como factura
                                led.regnumber = "M00001"; // numero de registro de migracion
				MaterialTransaction mtrans = OBDal.getInstance().get(MaterialTransaction.class, led.mTransactionId);
				
				if(mtrans.getGoodsShipmentLine()!=null){
					ShipmentInOut ship =  mtrans.getGoodsShipmentLine().getShipmentReceipt();
					
					if(ship.getDocumentNo().contains("-TD") ||  ship.getDocumentNo().contains("-SV") || ship.getDocumentNo().contains("-FV") || ship.getDocumentNo().contains("-BV")){
						String doc = ship.getDocumentNo().substring(5, 14);
						led.documento = "0"+doc.substring(0,2)+"-"+doc.substring(2);
						led.serie = "0"+doc.substring(0,2);
						led.numdoc = doc.substring(2);
						tipoDoc = led.t10code = "01";//factura
					}
					else{//buscar invoice por O/C					        
						OrderLine orderline = mtrans.getGoodsShipmentLine().getSalesOrderLine();
						if(orderline!=null){
							List<InvoiceLine> lsInvlines = orderline.getInvoiceLineList();
							for(int ij=0; ij<lsInvlines.size(); ij++){
								Invoice invv = lsInvlines.get(ij).getInvoice();
								if(invv!=null && invv.getDocumentStatus().equals("CO")){								  
									led.documento = invv.getScrPhysicalDocumentno();

									if(led.documento != null){
									  String[] parts = led.documento.split("-");
									  if(parts.length < 2){
									    led.serie = "";
									    led.numdoc = led.documento;
									  }
									  else{
									    led.serie = parts[0];
									    led.numdoc = parts[1];
									    int index=2;
									    while(index < parts.length){
									      led.numdoc = led.numdoc + "-" + parts[index];
									      index++;
									    }
									  }

									}
									else{
									  led.documento = "";
									  led.serie="";
									  led.numdoc = led.documento;
									}
	                                                                

									String td = invv.getTransactionDocument().getName();
									
									if(td.equals("AR Invoice") )
										tipoDoc = led.t10code = "01";//factura
									else if(td.equals("AR Ticket") )
											tipoDoc = led.t10code = "03";//factura
									else if(td.equals("AR Credit Memo" ) || td.equals("Return Material Sales Invoice" )) 
											tipoDoc = led.t10code = "07";//factura
									else if(td.equals("AR Debit Memo") ) 
											tipoDoc = led.t10code = "08";//factura
									else{//tipo de compra
										tipoDoc = led.t10code = invv.getScoPodoctypeComboitem().getCode();
									}
									break;
								}
							}
						}
					}
				}
			}
			else{
			  //search regnumber
			  MaterialTransaction mtrans = OBDal.getInstance().get(MaterialTransaction.class, led.mTransactionId);
                          
                          if(mtrans.getGoodsShipmentLine()!=null){
                            ShipmentInOut ship =  mtrans.getGoodsShipmentLine().getShipmentReceipt();
                            String regnumber = SCO_Utils.getRegNumberFromFactAcct(conn, vars.getClient(), "319", ship.getId());
                            if(!regnumber.isEmpty()){
                              led.regnumber =  "M" + regnumber.replace("-", ""); 
                              if(led.regnumber.length()>10) led.regnumber =led.regnumber.substring(0, 10);
                            }
                            else{
                              //Search for Invoice from O/C
                              OrderLine orderline = mtrans.getGoodsShipmentLine().getSalesOrderLine();
                              if(orderline!=null){
                                  List<InvoiceLine> lsInvlines = orderline.getInvoiceLineList();
                                  for(int ij=0; ij<lsInvlines.size(); ij++){
                                    Invoice invv = lsInvlines.get(ij).getInvoice();
                                    if(invv!=null && invv.getDocumentStatus().equals("CO")){
                                      
                                      if(led.regnumber.isEmpty()){
                                            regnumber = SCO_Utils.getRegNumberFromFactAcct(conn, vars.getClient(), "318", invv.getId());
                                            if(!regnumber.isEmpty()){
                                              led.regnumber =  "M" + regnumber.replace("-", ""); 
                                              if(led.regnumber.length()>10) led.regnumber =led.regnumber.substring(0, 10);
                                            }
                                      }
                                      break;
                                  
                                    }
                                  }
                              }
                            }
                          }
			}
			
			if(led.regnumber.isEmpty()){
			  led.regnumber = "M00002"; // numero de registro cuando no hay asiento
			}
			
			led.documento = led.documento.replace("/", "");
			led.serie = led.serie.replace("/", "");
			led.numdoc = led.numdoc.replace("/", "");
			
		}
		
		return listaOrginal;
	}
	
	public static StructPle getPLE(ConnectionProvider conn,
			VariablesSecureApp vars,
			String strDateFrom, String strDateTo, String adUserClient, String strOrg) throws Exception {
		
		ArrayList<ReportValuedInventoryData> dataFinal = getData(conn,
				vars,
				strDateFrom, strDateTo, adUserClient, strOrg, null);

		StructPle sunatPle = getStringData(dataFinal, strDateFrom, strDateTo,
				strOrg);

		return sunatPle;
	}
	
	
	public static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    } catch(NullPointerException e) {
	        return false;
	    }
	    // only got here if we didn't return false
	    return true;
	}
	
	private static StructPle getStringData(ArrayList<ReportValuedInventoryData> data,
			String strDateFrom, String strDateTo, String strOrg) {
		StructPle sunatPle = new StructPle();
		sunatPle.numEntries = 0;

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
				+ "00130100001111.TXT";// LERRRRRRRRRRRAAAAMM0006010000OIM1.TXT
		
		
		NumberFormat formatter = new DecimalFormat("#0.00");
		NumberFormat formatter3 = new DecimalFormat("#0.000");

		int jj=0;
		
		
		for (int i = 0; i < data.size(); i++) {

			ReportValuedInventoryData led = data.get(i);
			
			if(led.t6code!=null && !led.t6code.equals(""))//cabecera
				continue;
			if(led.mTransactionId==null || led.mTransactionId.equals("")) continue;
			
			
			MaterialTransaction mtrans = OBDal.getInstance().get(MaterialTransaction.class, led.mTransactionId);
			
			Date dttAcct = mtrans.getMovementDate();
			
			String periodoTrib = dt.format(dttAcct) + "00";


			String tipoAsiento = led.regnumber;
			String regnumber = led.mTransactionId;
			
			
			String establecimiento = "0000";
			Warehouse warehouse = OBDal.getInstance().get(Warehouse.class, led.warehouseId);
			String sunatcode = warehouse.getScrSunatcode();
			if(sunatcode!=null && !sunatcode.equals(""))
				establecimiento = sunatcode;
			
			if(establecimiento.length()!=4 || !isInteger(establecimiento))
				establecimiento="0000";
			
			String catalogo = "9";
			
			Product product = OBDal.getInstance().get(Product.class, led.productId);
			String tipoProd = SunatUtil.LPad(product.getPrdcProductgroup().getSCOSUNATProductType().getCode(), 2, '0'); 
			String codigoProd = product.getSearchKey();
			String codigoUnicoBien = "";//obligatorio 2018
			String fechaEmision = "";
			
			try{
				fechaEmision = dt2.format(formatterForm.parse(led.invoicedate));
			} catch (Exception ex) {
			}
			
			String serieDoc=led.serie;
			String numDoc= led.numdoc;
			String tipoDoc = led.t10code;
			String tipoOp = led.t12code;

			/*
			if(tipoDoc.equals("00") && (tipoOp.equals("01") || tipoOp.equals("05") || tipoOp.equals("02")) ){//si es de migracion que coga el valor de la guia como factura
				if(mtrans.getGoodsShipmentLine()!=null){
					ShipmentInOut ship =  mtrans.getGoodsShipmentLine().getShipmentReceipt();
					
					if(ship.getDocumentNo().contains("-TD") ||  ship.getDocumentNo().contains("-SV") || ship.getDocumentNo().contains("-FV") || ship.getDocumentNo().contains("-BV")){
						String doc = ship.getDocumentNo().substring(5, 14);
						led.documento = "0"+doc.substring(0,2)+"-"+doc.substring(2);
						tipoDoc = led.t10code = "01";//factura
					}
					else{//buscar invoice por O/C
						OrderLine orderline = mtrans.getGoodsShipmentLine().getSalesOrderLine();
						if(orderline!=null){
							List<InvoiceLine> lsInvlines = orderline.getInvoiceLineList();
							for(int ij=0; ij<lsInvlines.size(); ij++){
								Invoice invv = lsInvlines.get(ij).getInvoice();
								if(invv!=null && invv.getDocumentStatus().equals("CO")){
									led.documento = invv.getScrPhysicalDocumentno();
									String td = invv.getTransactionDocument().getName();
									
									if(td.equals("AR Invoice") )
										tipoDoc = led.t10code = "01";//factura
									else if(td.equals("AR Ticket") )
											tipoDoc = led.t10code = "03";//factura
									else if(td.equals("AR Credit Memo" ) || td.equals("Return Material Sales Invoice" )) 
											tipoDoc = led.t10code = "07";//factura
									else if(td.equals("AR Debit Memo") ) 
											tipoDoc = led.t10code = "08";//factura
									else{//tipo de compra
										tipoDoc = led.t10code = invv.getScoPodoctypeComboitem().getCode();
									}
									break;
								}
							}
						}
					}
				}
			}
			
			led.documento = led.documento.replace("/", "");
			*/
			
			numDoc = numDoc.replace("-", "");

			if (!serieDoc.equals("")){
				serieDoc = SunatUtil.LPad(serieDoc, 4, '0');
				serieDoc = StringUtils.leftPad(serieDoc, 4,'0');
				      if(serieDoc.length() > 4){
				        //adjust to 4 digits only if we are erasing '0'
				        if(serieDoc.substring(0, serieDoc.length() - 4).matches("[0]+")){
				          serieDoc = serieDoc.substring(serieDoc.length() - 4, serieDoc.length());
				        }
				      }    
			}

			if(numDoc.length()>20) numDoc =numDoc.substring(0, 20);
			
			String descripcionProd = product.getName();
			if(descripcionProd.length()>80) descripcionProd = descripcionProd.substring(0, 80);
			
			
			String uom = product.getUOM().getSsaCode(); if(uom==null) uom="NIU";//unidades
			String tipoCosteo = "1";
			String ctdIn = led.cantidadIn;
			String costoUnitIn = formatter.format(new Double(led.costounitIn));
			String costoTotalIn = formatter.format(new Double(led.costototalIn));
			String ctdOut = led.cantidadOut;
			String costoUnitOut = formatter.format(new Double(led.costounitOut));
			String costoTotalOut = formatter.format(new Double(led.costototalOut));
			String saldoCtd = formatter3.format(new Double(led.saldoFinal));
			String costoUnitFinal = "0.00";
			try{
				BigDecimal costBigFinal = new BigDecimal(led.costoFinal);
				if(costBigFinal.compareTo(BigDecimal.ZERO)!=0) 
					costoUnitFinal = (costBigFinal.divide(new BigDecimal(led.saldoFinal), 8, RoundingMode.HALF_UP)).toString();
			}catch(Exception ex){}
			
			String costoTotalFinal = formatter.format(new Double(led.costoFinal));
			
			String estadoOp = "1";
			
			
			String linea = periodoTrib + "|" + regnumber + "|" + tipoAsiento
					+ "|"+ establecimiento + "|" + catalogo + "|" + tipoProd + "|"+codigoProd+"|"+codigoUnicoBien+"|"+fechaEmision+"|"+tipoDoc+"|"+serieDoc+"|"+numDoc+"|"+tipoOp+"|"
					+descripcionProd+"|"+uom+"|"+tipoCosteo+"|"+ctdIn+"|"+costoUnitIn+"|"+costoTotalIn+"|"+ctdOut+"|"+costoUnitOut+"|"+costoTotalOut+"|"+saldoCtd+"|"+costoUnitFinal+"|"+costoTotalFinal+"|"
					+ estadoOp + "|";
			
			if (jj > 0)
				sb.append("\n");
			sb.append(linea);
			sunatPle.numEntries++;
			jj++;
		}
		
		sunatPle.filename = filename;
		sunatPle.data = sb.toString();
		return sunatPle;
	
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
		return "Servlet ReportValuedInventory. This Servlet was made by Jose Valdez modified by everybody";
	} // end of getServletInfo() method
}
