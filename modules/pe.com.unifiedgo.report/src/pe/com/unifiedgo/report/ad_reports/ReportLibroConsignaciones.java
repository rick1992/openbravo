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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.data.FieldProvider;
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
import org.openbravo.erpCommon.utility.SQLReturnObject;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchemaTable;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.accounting.StructPle;
import pe.com.unifiedgo.accounting.SunatUtil;
import pe.com.unifiedgo.core.data.SCRComboItem;
import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class ReportLibroConsignaciones extends HttpSecureAppServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		VariablesSecureApp vars = new VariablesSecureApp(request);

		if (log4j.isDebugEnabled())
			log4j.debug("Command: " + vars.getStringParameter("Command"));

		if (vars.commandIn("DEFAULT")) {
			String strDateFrom = vars.getGlobalVariable("inpDateFrom",
					"ReportLibroConsignaciones|DateFrom",
					SREDateTimeData.FirstDayOfMonth(this));
			String strDateTo = vars.getGlobalVariable("inpDateTo",
					"ReportLibroConsignaciones|DateTo",
					SREDateTimeData.today(this));
			String strOrg = vars.getGlobalVariable("inpOrg",
					"ReportLibroConsignaciones|Org", "0");
			String strRecord = vars.getGlobalVariable("inpRecord",
					"ReportLibroConsignaciones|Record", "");
			String strTable = vars.getGlobalVariable("inpTable",
					"ReportLibroConsignaciones|Table", "");
			String strmProductId = vars.getGlobalVariable("inpmProductId",
					"ReportLibroConsignaciones|M_Product_Id", "");
			String strcBpartnetId = vars.getGlobalVariable("inpcBPartnerId",
					"ReportLibroConsignaciones|BPartnerId", "");
			printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg,
					strmProductId, strcBpartnetId, strTable, strRecord);

		} else if (vars.commandIn("DIRECT")) {
			String strTable = vars.getGlobalVariable("inpTable",
					"ReportLibroConsignaciones|Table");
			String strRecord = vars.getGlobalVariable("inpRecord",
					"ReportLibroConsignaciones|Record");
			String strmProductId = vars.getGlobalVariable("inpmProductId",
					"ReportLibroConsignaciones|M_Product_Id", "");
			String strcBpartnetId = vars.getGlobalVariable("inpcBPartnerId",
					"ReportLibroConsignaciones|BPartnerId", "");
			setHistoryCommand(request, "DIRECT");
			vars.setSessionValue("ReportLibroConsignaciones.initRecordNumber",
					"0");
			printPageDataSheet(response, vars, "", "", "", strmProductId,
					strcBpartnetId, strTable, strRecord);

		} else if (vars.commandIn("FIND")) {
			String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
					"ReportLibroConsignaciones|DateFrom");
			String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
					"ReportLibroConsignaciones|DateTo");
			String strOrg = vars.getGlobalVariable("inpOrg",
					"ReportLibroConsignaciones|Org", "0");
			vars.setSessionValue("ReportLibroConsignaciones.initRecordNumber",
					"0");
			setHistoryCommand(request, "DEFAULT");
			String strmProductId = vars.getStringParameter("inpmProductId", "");
			String strcBpartnetId = vars.getStringParameter("inpcBPartnerId",
					"");
			printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg,
					strmProductId, strcBpartnetId, "", "");

		} else if (vars.commandIn("PDF", "XLS")) {
			if (log4j.isDebugEnabled())
				log4j.debug("PDF");
			String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
					"ReportLibroConsignaciones|DateFrom");
			String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
					"ReportLibroConsignaciones|DateTo");
			String strOrg = vars.getGlobalVariable("inpOrg",
					"ReportLibroConsignaciones|Org", "0");
			String strmProductId = vars.getStringParameter("inpmProductId", "");
			String strcBpartnetId = vars.getStringParameter("inpcBPartnerId",
					"");
			String strTipoReporte = vars.getStringParameter("inpTipoReporte",
					"");
			String strTable = vars.getStringParameter("inpTable");
			String strRecord = vars.getStringParameter("inpRecord");
			setHistoryCommand(request, "DEFAULT");
			printPagePDF(request, response, vars, strDateFrom, strDateTo,
					strOrg, strmProductId, strcBpartnetId, strTipoReporte,
					strTable, strRecord);

		} else
			pageError(response);
	}

	private void printPageDataSheet(HttpServletResponse response,
			VariablesSecureApp vars, String strDateFrom, String strDateTo,
			String strOrg, String strmProductId, String strcBpartnetId,
			String strTable, String strRecord) throws IOException,
			ServletException {
		if (log4j.isDebugEnabled())
			log4j.debug("Output: dataSheet");
		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();
		XmlDocument xmlDocument = null;
		ReportLibroConsignacionesData[] data = null;
		String strPosition = "0";
		ToolBar toolbar = new ToolBar(this, vars.getLanguage(),
				"ReportLibroConsignaciones", false, "", "",
				"imprimir();return false;", false, "ad_reports",
				strReplaceWith, false, true);
		toolbar.setEmail(false);
		toolbar.prepareSimpleToolBarTemplate();
		if (data == null || data.length == 0) {
			String discard[] = { "secTable" };

			xmlDocument = xmlEngine
					.readXmlTemplate(
							"pe/com/unifiedgo/report/ad_reports/ReportLibroConsignaciones",
							discard).createXmlDocument();
			data = ReportLibroConsignacionesData.set("0");
			data[0].rownum = "0";
		} 
		
	    toolbar.setEmail(false);

	    toolbar.prepareSimpleToolBarTemplate();
	    toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");
		xmlDocument.setParameter("toolbar", toolbar.toString());
		try {
			WindowTabs tabs = new WindowTabs(this, vars,
					"pe.com.unifiedgo.report.ad_reports.ReportLibroConsignaciones");
			xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
			xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
			xmlDocument.setParameter("childTabContainer", tabs.childTabs());
			xmlDocument.setParameter("theme", vars.getTheme());
			NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
					"ReportLibroConsignaciones.html", classInfo.id,
					classInfo.type, strReplaceWith, tabs.breadcrumb());
			xmlDocument.setParameter("navigationBar", nav.toString());
			LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
					"ReportLibroConsignaciones.html", strReplaceWith);
			xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
		} catch (Exception ex) {
			throw new ServletException(ex);
		}
		{
			OBError myMessage = vars.getMessage("ReportLibroConsignaciones");
			vars.removeMessage("ReportLibroConsignaciones");
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
					"TABLEDIR", "AD_ORG_ID", "", "0C754881EAD94243A161111916E9B9C6", Utility.getContext(this,
							vars, "#AccessibleOrgTree",
							"ReportLibroConsignaciones"), Utility.getContext(
							this, vars, "#User_Client",
							"ReportLibroConsignaciones"), '*');
			comboTableData
					.fillParameters(null, "ReportLibroConsignaciones", "");
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

		xmlDocument.setParameter("paramProductId", strmProductId);
		xmlDocument.setParameter("paramProductDescription",
				ReportLibroConsignacionesData.selectMproduct(this,
						strmProductId));

		xmlDocument.setParameter("paramBPartnerId", strcBpartnetId);

		xmlDocument.setParameter("paramBPartnerDescription",
				ReportLibroConsignacionesData.selectBPartner(this,
						strcBpartnetId));

		vars.setSessionValue("ReportLibroConsignaciones|Record", strRecord);
		vars.setSessionValue("ReportLibroConsignaciones|Table", strTable);

		xmlDocument.setData("structure1", data);

		// //////////////////////////// PARA ELEGIR EL TIPO REPORTE
		Vector<Object> vector = new Vector<Object>(0);
		FieldProvider tipo_reporte[] = new FieldProvider[2];

		SQLReturnObject sqlReturnObject = new SQLReturnObject();
		sqlReturnObject.setData("ID", "Consignador");
		sqlReturnObject.setData("NAME", "PARA EL CONSIGNADOR");
		vector.add(sqlReturnObject);

		sqlReturnObject = new SQLReturnObject();
		sqlReturnObject.setData("ID", "Consignatario");
		sqlReturnObject.setData("NAME", "PARA EL CONSIGNATARIO");
		vector.add(sqlReturnObject);

		vector.copyInto(tipo_reporte);

		xmlDocument.setData("reportC_TIPO_REPORT", "liststructure",
				tipo_reporte);

		// ////////////////////

		xmlDocument
				.setParameter(
						"paramPeriodosArray",
						Utility.arrayInfinitasEntradas(
								"idperiodo;periodo;fechainicial;fechafinal;idorganizacion",
								"arrPeriodos", ReportLibroConsignacionesData
										.select_periodos(this)));

		out.println(xmlDocument.print());
		out.close();
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
			String strmProductId, String strcBpartnetId, String strTipoReporte,
			String strTable, String strRecord) throws IOException,
			ServletException {
		

		String strOutput;
		String strReportName;
		
		ReportLibroConsignacionesData[] dataNueva = getData(this, vars,
				strDateFrom, strDateTo, Utility.getContext(
						this, vars, "#User_Client",
						"ReportLibroConsignaciones"), strOrg, strmProductId, strcBpartnetId, strTipoReporte);
				
		String strSubtitle = (Utility.messageBD(this, "LegalEntity",
				vars.getLanguage()) + ": ")
				+ ReportLibroConsignacionesData.selectCompany(this,
						vars.getClient())
				+ "\n"
				+ "RUC:"
				+ ReportLibroConsignacionesData.selectRucOrg(this, strOrg)
				+ "\n";
		;

		if (!("0".equals(strOrg)))
			strSubtitle += (Utility.messageBD(this, "OBUIAPP_Organization",
					vars.getLanguage()) + ": ")
					+ ReportLibroConsignacionesData.selectOrg(this, strOrg)
					+ "\n";
		
	      if (vars.commandIn("PDF")) {
	          strOutput = "pdf";
	          
	          if(strTipoReporte.equalsIgnoreCase("Consignador")){
					strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportLibroConsignacionesConsignador.jrxml";

	          }else {
					strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportLibroConsignacionesConsignatario.jrxml";
	          }

	          
//	            strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportSalesRevenueRecords.jrxml";
	        } else {
	          strOutput = "xls";
	          if(strTipoReporte.equalsIgnoreCase("Consignador")){
					strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportLibroConsignacionesConsignadorExcel.jrxml";

	          }else {
					strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportLibroConsignacionesConsignatarioExcel.jrxml";
	          }
//	          strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportSalesRevenueRecordsXls.jrxml";
	        }


		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("Subtitle", strSubtitle);
		parameters.put("Ruc",
				ReportLibroConsignacionesData.selectRucOrg(this, strOrg));
		parameters.put("Razon",
				ReportLibroConsignacionesData.selectSocialName(this, strOrg));
		// parameters.put("TaxID",
		// ReportSalesRevenueRecordsData.selectOrgTaxID(this, strOrg));
		parameters.put("dateFrom", StringToDate(strDateFrom));
		parameters.put("dateTo", StringToDate(strDateTo));

		if (dataNueva.length == 0) {
			advisePopUp(
					request,
					response,
					"WARNING",
					Utility.messageBD(this, "ProcessStatus-W",
							vars.getLanguage()),
					Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
			return;
		}
		renderJR(vars, response, strReportName,
				"Registro de Consignaciones (Para el "+strTipoReporte+")", strOutput, parameters,
				dataNueva, null);
	}
	
	
	
	
	
	
	
	private static ReportLibroConsignacionesData[] getData(ConnectionProvider conn, VariablesSecureApp vars,
			String strDateFrom, String strDateTo, String adUserClient, String strOrg, String strmProductId, String strcBpartnetId, String strTipoReporte) throws IOException,
			ServletException {
		
		ReportLibroConsignacionesData[] data = null;
		ReportLibroConsignacionesData[] comodin = null;
		ArrayList<ReportLibroConsignacionesData> arrayData = new ArrayList<ReportLibroConsignacionesData>();
		ReportLibroConsignacionesData[] dataNueva;

		String strTreeOrg = TreeData.getTreeOrg(conn, vars.getClient());
		String strOrgFamily = getFamily(conn, strTreeOrg, strOrg);

	

		if (strTipoReporte.compareToIgnoreCase("Consignador") == 0) {

			data = ReportLibroConsignacionesData
					.selectLineasCabeceraConsignador(conn, adUserClient, strOrgFamily,
							strmProductId, strcBpartnetId);

			BigDecimal saldoAnterior;


			for (int i = 0; i < data.length; i++) {
				// obtiene el saldo para el producto
				comodin = ReportLibroConsignacionesData
						.selectSaldoAnteriorConsignador(conn, adUserClient,
								strOrgFamily, data[i].idproducto,
								data[i].idtercero, strDateFrom);

				saldoAnterior = new BigDecimal(comodin[0].saldoProducto);

				data[i].saldoProducto = saldoAnterior.toString();

				
				// obtiene movimientos
				comodin = ReportLibroConsignacionesData
						.selectConsignadorMejorado3(conn, adUserClient, strOrgFamily,
								data[i].idproducto, data[i].idtercero,
								strDateFrom, strDateTo);

				// para saber si tiene movimientos durante el periodo o tiene
				// saldo <> de 0

								
				if (comodin.length > 0
						|| saldoAnterior.compareTo(BigDecimal.ZERO) > 0) {

					data[i].tipoTransaccion = "cabecera";
					arrayData.add(data[i]);

					for (int j = 0; j < comodin.length; j++) {
						
						comodin[j].idproducto = data[i].idproducto;
				
						comodin[j].nGuiaConsignador="";
						ShipmentInOut inout = OBDal.getInstance().get(ShipmentInOut.class, comodin[j].guiaRefId);
						if(inout!=null){
							comodin[j].nGuiaConsignador = inout.getSCRPhysicalDocumentNo();
							
						}
						
						saldoAnterior = saldoAnterior.add(new BigDecimal(
								comodin[j].cantidadMovida));
						comodin[j].saldoProducto = saldoAnterior.toString();
						String[] num_guia = comodin[j].nGuiaConsignador
								.split("-");
						String[] num_factura = comodin[j].nFacturaConsignador
								.split("-");
						if(comodin[j].nGuiaConsignador!=null && !comodin[j].nGuiaConsignador.equals("")
								&& Character.isDigit(comodin[j].nGuiaConsignador.charAt(0)) ){//guia
							comodin[j].serieGuia = num_guia.length > 1 ? num_guia[0]
								: "";
							comodin[j].numeroGuia = num_guia.length > 1 ? num_guia[1]
								: num_guia[0];
						}else{
							comodin[j].numeroGuia = comodin[j].nGuiaConsignador;
							comodin[j].serieGuia = "";
						}
						
						comodin[j].serieFactura = num_factura.length > 1 ? num_factura[0]
								: "";
						comodin[j].numeroFactura = num_factura.length > 1 ? num_factura[1]
								: num_factura[0];

						arrayData.add(comodin[j]);
					}
				}
			}

			dataNueva = new ReportLibroConsignacionesData[arrayData.size()];
			for (int i = 0; i < arrayData.size(); i++) {
				dataNueva[i] = arrayData.get(i);
			}

		}

		else {
//			strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportLibroConsignacionesConsignatario.jrxml";

			// / ACA EMPEZAMOS LA MODOFICACION PARA EL CONSIGNATARIO

			data = ReportLibroConsignacionesData
					.selectLineasCabeceraConsignatario(conn, adUserClient, strOrgFamily,
							strmProductId);

			BigDecimal saldoAnterior;

			for (int i = 0; i < data.length; i++) {

				saldoAnterior = BigDecimal.ZERO;
				// obtiene saldo inicial
				comodin = ReportLibroConsignacionesData
						.selectSaldoAnteriorConsignatario(conn, adUserClient,
								strOrgFamily, data[i].idproducto, strDateFrom);

				saldoAnterior = new BigDecimal(comodin[0].saldoProducto);

				data[i].saldoProducto = saldoAnterior.toString();
				// obtiene movimientos
				comodin = ReportLibroConsignacionesData
						.selectConsignatarioMejorado2(conn, adUserClient, strOrgFamily,
								data[i].idproducto, strDateFrom, strDateTo);

				// para saber si tiene movimientos durante el periodo o tiene
				// saldo <> de 0

				if (comodin.length > 0
						|| saldoAnterior.compareTo(BigDecimal.ZERO) > 0) {
					data[i].tipoTransaccion = "cabecera";
					arrayData.add(data[i]);
				}

				for (int j = 0; j < comodin.length; j++) {
					
					comodin[j].idproducto = data[i].idproducto;
					
					saldoAnterior = saldoAnterior.add(new BigDecimal(
							comodin[j].cantidadMovida));
					comodin[j].saldoProducto = saldoAnterior.toString();
					String[] num_guia = comodin[j].nGuiaConsignador.split("-");
					String[] num_factura = comodin[j].nFacturaConsignador
							.split("-");
					comodin[j].serieGuia = num_guia.length > 1 ? num_guia[0]
							: "";
					comodin[j].numeroGuia = num_guia.length > 1 ? num_guia[1]
							: num_guia[0];
					comodin[j].serieFactura = num_factura.length > 1 ? num_factura[0]
							: "";
					comodin[j].numeroFactura = num_factura.length > 1 ? num_factura[1]
							: num_factura[0];
					
					if(comodin[j].tipoTransaccion.compareTo("venta")==0){
						
					}
					else {
						comodin[j].serieFactura=comodin[j].serieGuia;
						comodin[j].numeroFactura=comodin[j].numeroGuia;
					}

					arrayData.add(comodin[j]);
				}
			}

			dataNueva = new ReportLibroConsignacionesData[arrayData.size()];
			for (int i = 0; i < arrayData.size(); i++) {
				dataNueva[i] = arrayData.get(i);
			}
		}
		
		return dataNueva;
	}
	
	
	
	
	public static StructPle getPLE(ConnectionProvider conn,
			VariablesSecureApp vars,
			String strDateFrom, String strDateTo, String adUserClient, String strOrg, String tipoReporte) throws Exception {
		
		ReportLibroConsignacionesData[] dataFinal = getData(conn, vars, strDateFrom, strDateTo, adUserClient, strOrg, null, null, tipoReporte);

		StructPle sunatPle = getStringData(dataFinal, strDateFrom, strDateTo,
				strOrg, tipoReporte);

		return sunatPle;
	}
	
	
	private static StructPle getStringData(ReportLibroConsignacionesData[] data,
			String strDateFrom, String strDateTo, String strOrg, String tipoReporte) {
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
				+ "00090200001111.TXT";// Consignatario
		
		if(tipoReporte.compareToIgnoreCase("Consignador")==0)
			filename = "LE" + rucAdq + dt.format(dttFrom)
			+ "00090100001111.TXT";// Consignador
		
		NumberFormat formatter = new DecimalFormat("#0.00");
		NumberFormat formatter3 = new DecimalFormat("#0.000");

		int jj=0;
		
		
		for (int i = 0; i < data.length; i++) {
			
			ReportLibroConsignacionesData led = data[i];
			String productId = led.idproducto;
			Product prod = OBDal.getInstance().get(Product.class, productId);
			
			if(led.tipoTransaccion!=null && led.tipoTransaccion.equals("cabecera")) continue;
			if(led.qtyIngreso == null) continue;
			
			Date dttAcct = null;
			try {
				dttAcct = formatterForm.parse(led.fecha);
			} catch (Exception ex) {
			}
			String periodoTrib = dt.format(dttAcct) + "00";
			String tipoAsiento = "M00001";
			String linea = "";
			
			if(tipoReporte.compareToIgnoreCase("Consignador")==0){
				
				String catalogo = "9";
				String tipoExistencia = prod.getPrdcProductgroup().getSCOSUNATProductType().getCode();
				String codigoExistencia = prod.getSearchKey();
				String regnumber = led.mTransactionId;
				String nombreExistencia = prod.getName();
				String uom = prod.getUOM().getSsaCode();
				if(uom==null) uom="NIU";//unidades
				
				ShipmentInOut inout = OBDal.getInstance().get(ShipmentInOut.class, led.guiaRefId);
				
				tipoExistencia = SunatUtil.LPad(tipoExistencia, 2, '0');
				
				String fechaEmisionInout = "";
				try{
					fechaEmisionInout = dt2.format(inout.getMovementDate());
				}catch(Exception ex){}
				
				String serieInout = led.serieGuia;
				String numeroInout = led.numeroGuia;
				String tipoComprobante = led.tipoDocT10;
				
				numeroInout = numeroInout.replace("-", "");
				
				Invoice invoice = OBDal.getInstance().get(Invoice.class, led.idFacturaConsignador);
				
				String fechaEmisionInv = "";
				try{
					fechaEmisionInv = dt2.format(invoice.getAccountingDate());
				}catch(Exception ex){}
				
				if(fechaEmisionInv.equals("")) fechaEmisionInv = fechaEmisionInout;
				
				String serieInvoice = led.serieFactura;
				String numeroInvoice = led.numeroFactura;
				
				numeroInvoice = numeroInvoice.replace("-", "");
				
				String fechaMov = "";
				try{
					fechaMov = dt2.format(formatterForm.parse(led.fecha));
				}catch(Exception ex){}
				
				BusinessPartner bpartner = OBDal.getInstance().get(BusinessPartner.class, led.idtercero);
				
				String bTipoDoc = "";
				String bRuc = "";
				String bName = "";
				
				try{
					
					SCRComboItem tipodoc = bpartner.getScrComboItem();
				      String tipodoc_value = tipodoc.getSearchKey();
				      
				      if (tipodoc_value.equals("DNI")) {
				    	  bTipoDoc = "1";
				      } else if (tipodoc_value.equals("CARNET DE EXTRANJERIA")) {
				    	  bTipoDoc = "4";
				      } else if (tipodoc_value.equals("REGISTRO UNICO DE CONTRIBUYENTES")) {
				    	  bTipoDoc = "6";
				      } else if (tipodoc_value.equals("PASAPORTE")) {
				    	  bTipoDoc = "7";
				      } else {
				    	  bTipoDoc = "0"; 
				      }
				    
				      bRuc = bpartner.getTaxID();
				      bName = bpartner.getName();
				      
				}catch(Exception ex){}
				
				//FIX-ME  saldo inicial en la primera tupla?
				String ctdEntregado = formatter.format(new BigDecimal(led.qtyIngreso));
				String ctdDevuelto = formatter.format(new BigDecimal(led.qtyDevolucion).negate());
				String ctdVenta = formatter.format(new BigDecimal(led.qtyVenta).negate());
				
				String estadoOp="1";
				
				
				if(serieInvoice.equals("")) serieInvoice="0";
				if(numeroInvoice.equals("")) numeroInvoice="0";
				
				linea = periodoTrib + "|" + catalogo + "|" + tipoExistencia + "|" + codigoExistencia + "|" + regnumber + "|" + nombreExistencia + "|" +uom+"|"
						+ fechaEmisionInout + "|"+serieInout+"|"+numeroInout+"|"+tipoComprobante+"|"+fechaEmisionInv+"|"+serieInvoice+"|"+numeroInvoice+"|"+fechaMov+"|"
						+ bTipoDoc+"|"+bRuc+"|"+bName+"|" + ctdEntregado + "|"+ ctdDevuelto + "|" + ctdVenta +  "|" + estadoOp + "|";
				
				
			}else{//consignatario
				
				String catalogo = "9";
				String tipoExistencia = prod.getPrdcProductgroup().getSCOSUNATProductType().getCode();
				String codigoExistencia = prod.getSearchKey();
				String regnumber = led.mTransactionId;
				String nombreExistencia = prod.getName();
				String uom = prod.getUOM().getSsaCode();
				if(uom==null) uom="NIU";//unidades
				
				ShipmentInOut inout = OBDal.getInstance().get(ShipmentInOut.class, led.guiaRefId);
				
				tipoExistencia = SunatUtil.LPad(tipoExistencia, 2, '0');
				
				
				String fechaEmisionInout = "";
				try{
					fechaEmisionInout = dt2.format(inout.getMovementDate());
				}catch(Exception ex){}
				
				String serieInout = led.serieGuia;
				String numeroInout = led.numeroGuia;
				String tipoComprobante = led.tipoDocT10;
				
				Invoice invoice = OBDal.getInstance().get(Invoice.class, led.idFacturaConsignador);
				
				String fechaEmisionInv = "";
				try{
					fechaEmisionInout = dt2.format(invoice.getAccountingDate());
				}catch(Exception ex){}
				
				String serieInvoice = led.serieFactura;
				String numeroInvoice = led.numeroFactura;
				
				String fechaMov = "";
				try{
					fechaMov = dt2.format(formatterForm.parse(led.fecha));
				}catch(Exception ex){}
				
				BusinessPartner bpartner = OBDal.getInstance().get(BusinessPartner.class, led.idtercero);
				
				String bRuc = "";
				String bName = "";
				
				
				try{
					
				      bRuc = bpartner.getTaxID();
				      bName = bpartner.getName();
				      
				}catch(Exception ex){}
				
				//FIX-ME  saldo inicial en la primera tupla?
				String ctdEntregado = formatter.format(new BigDecimal(led.qtyIngreso));
				String ctdDevuelto = formatter.format(new BigDecimal(led.qtyDevolucion).negate());
				String ctdVenta = formatter.format(new BigDecimal(led.qtyVenta).negate());

				if(fechaEmisionInout.equals("")) fechaEmisionInout="01/01/0001";
				if(serieInout.equals("")) serieInout="0";
				if(numeroInout.equals("")) numeroInout="0";
				if(tipoComprobante.equals("")) tipoComprobante="00";
				if(fechaEmisionInv.equals("")) fechaEmisionInv="01/01/0001";
				if(serieInvoice.equals("")) serieInvoice="0";
				if(numeroInvoice.equals("")) numeroInvoice="0";
				
				String estadoOp="1";
				
				linea = periodoTrib + "|" + catalogo + "|" + tipoExistencia + "|" + codigoExistencia + "|" + regnumber + "|" + nombreExistencia + "|" +uom+"|"
						+ fechaEmisionInout + "|"+serieInout+"|"+numeroInout+"|"+tipoComprobante+"|"+fechaEmisionInv+"|"+serieInvoice+"|"+numeroInvoice+"|"+fechaMov+"|"
						+bRuc+"|"+bName+"|" + ctdEntregado + "|"+ ctdDevuelto + "|" + ctdVenta +  "|" + estadoOp + "|";
								
			}
			
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
		return "Servlet ReportLibroConsignaciones. This Servlet was made by Pablo Sarobe modified by everybody";
	} // end of getServletInfo() method
}
