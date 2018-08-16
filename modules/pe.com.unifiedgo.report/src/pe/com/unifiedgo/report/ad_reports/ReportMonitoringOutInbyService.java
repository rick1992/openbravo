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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class ReportMonitoringOutInbyService extends HttpSecureAppServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		VariablesSecureApp vars = new VariablesSecureApp(request);

		if (log4j.isDebugEnabled())
			log4j.debug("Command: " + vars.getStringParameter("Command"));

		if (vars.commandIn("DEFAULT")) {
			String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReportMonitoringOutInbyService|DateFrom",
					SREDateTimeData.FirstDayOfMonth(this));
			String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportMonitoringOutInbyService|DateTo",
					SREDateTimeData.today(this));
			String strOrg = vars.getGlobalVariable("inpOrg", "ReportMonitoringOutInbyService|Org", "");
			String strRecord = vars.getGlobalVariable("inpRecord", "ReportMonitoringOutInbyService|Record", "");
			String strTable = vars.getGlobalVariable("inpTable", "ReportMonitoringOutInbyService|Table", "");
			printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg, strTable, strRecord);

		} else if (vars.commandIn("DIRECT")) {
			String strTable = vars.getGlobalVariable("inpTable", "ReportMonitoringOutInbyService|Table");
			String strRecord = vars.getGlobalVariable("inpRecord", "ReportMonitoringOutInbyService|Record");

			setHistoryCommand(request, "DIRECT");
			vars.setSessionValue("ReportMonitoringOutInbyService.initRecordNumber", "0");
			printPageDataSheet(response, vars, "", "", "", strTable, strRecord);

		} else if (vars.commandIn("FIND")) {
			String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
					"ReportMonitoringOutInbyService|DateFrom");
			String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportMonitoringOutInbyService|DateTo");
			String strOrg = vars.getGlobalVariable("inpOrg", "ReportMonitoringOutInbyService|Org", "0");
			vars.setSessionValue("ReportMonitoringOutInbyService.initRecordNumber", "0");
			setHistoryCommand(request, "DEFAULT");
			printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg, "", "");

		} else if (vars.commandIn("PDF", "XLS")) {
			if (log4j.isDebugEnabled())
				log4j.debug("PDF");
			String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
					"ReportMonitoringOutInbyService|DateFrom");
			String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportMonitoringOutInbyService|DateTo");
			String strOrg = vars.getStringParameter("inpOrg");

			String documentno = vars.getStringParameter("inpDocumentno");

			String strTable = vars.getStringParameter("inpTable");
			String strRecord = vars.getStringParameter("inpRecord");
			setHistoryCommand(request, "DEFAULT");

			if (!(documentno.equalsIgnoreCase("")) && documentno != null) {
				strDateFrom = "";
				strDateTo = "";
			}

			if (vars.commandIn("PDF")) {
				printPagePDF(request, response, vars, strDateFrom, strDateTo, strOrg, documentno, strRecord);
			}
			if (vars.commandIn("XLS")) {
				printPageXLS(request, response, vars, strDateFrom, strDateTo, strOrg, documentno, strRecord);
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

		ReportMonitoringOutInbyServiceData[] data = null;
		String strPosition = "0";

		String discard[] = { "discard" };

		xmlDocument = xmlEngine
				.readXmlTemplate("pe/com/unifiedgo/report/ad_reports/ReportMonitoringOutInbyService", discard)
				.createXmlDocument();

		ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportMonitoringOutInbyService", false, "", "",
				"imprimirPDF();return false;", false, "ad_reports", strReplaceWith, false, true);

		toolbar.setEmail(false);
		toolbar.prepareSimpleToolBarTemplate();

		toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");

		// if (data == null || data.length == 0) {

		// toolbar
		// .prepareRelationBarTemplate(
		// false,
		// false,
		// "submitCommandForm('XLS', false, null,
		// 'ReportMonitoringOutInbyService.xls', 'EXCEL');return false;");

		// data = ReportMonitoringOutInbyServiceData.set("0");
		// data[0].rownum = "0";
		// } else {
		//
		// // data = notshow(data, vars);
		//
		// toolbar
		// .prepareRelationBarTemplate(
		// true,
		// true,
		// "submitCommandForm('XLS', false, null,
		// 'ReportMonitoringOutInbyService.xls', 'EXCEL');return false;");
		// xmlDocument = xmlEngine.readXmlTemplate(
		// "pe/com/unifiedgo/report/ad_reports/ReportMonitoringOutInbyService").createXmlDocument();
		// }
		xmlDocument.setParameter("toolbar", toolbar.toString());

		try {
			WindowTabs tabs = new WindowTabs(this, vars,
					"pe.com.unifiedgo.report.ad_reports.ReportMonitoringOutInbyService");
			xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
			xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
			xmlDocument.setParameter("childTabContainer", tabs.childTabs());
			xmlDocument.setParameter("theme", vars.getTheme());
			NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportMonitoringOutInbyService.html",
					classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
			xmlDocument.setParameter("navigationBar", nav.toString());
			LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportMonitoringOutInbyService.html",
					strReplaceWith);
			xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
		} catch (Exception ex) {
			throw new ServletException(ex);
		}
		{
			OBError myMessage = vars.getMessage("ReportMonitoringOutInbyService");
			vars.removeMessage("ReportMonitoringOutInbyService");
			if (myMessage != null) {
				xmlDocument.setParameter("messageType", myMessage.getType());
				xmlDocument.setParameter("messageTitle", myMessage.getTitle());
				xmlDocument.setParameter("messageMessage", myMessage.getMessage());
			}
		}

		xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));

		try {
			ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
					"ABE594ACE1764B7799DEF0BA6E8A389B",
					Utility.getContext(this, vars, "#User_Org", "ReportMonitoringOutInbyService"),
					Utility.getContext(this, vars, "#User_Client", "ReportMonitoringOutInbyService"), 0);
			comboTableData.fillParameters(null, "ReportMonitoringOutInbyService", "");
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
		vars.setSessionValue("ReportMonitoringOutInbyService|Record", strRecord);
		vars.setSessionValue("ReportMonitoringOutInbyService|Table", strTable);

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
			String strDateFrom, String strDateTo, String strOrg, String documentno, String strRecord)
					throws IOException, ServletException {

		// CASO DE PRUEBA : SK12472016

		ReportMonitoringOutInbyServiceData[] data = null;
		ReportMonitoringOutInbyServiceData[] dataUltima = null;

		ReportMonitoringOutInbyServiceData[] dataSaldos = null;
		ArrayList<ReportMonitoringOutInbyServiceData> movimientos = new ArrayList<ReportMonitoringOutInbyServiceData>();


		String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
		String strOrgFamily = getFamily(strTreeOrg, strOrg);
		String strClient = Utility.getContext(this, vars, "#User_Client", "ReportPaymentImportSchedule");

		data = ReportMonitoringOutInbyServiceData.selectOrders(this, strClient,strOrgFamily, strDateFrom, strDateTo, documentno);
		dataSaldos = ReportMonitoringOutInbyServiceData.select_saldos(this, strClient,
        strOrgFamily , strDateFrom);

		if (data.length == 0) {
			advisePopUp(request, response, "WARNING", Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
					Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
			return;
		}
				
		String and_proveedor="";
		String an_codigo_consumo="";
		BigDecimal saldo= BigDecimal.ZERO;

		for( int i=0 ; i< data.length ;i++){
			ReportMonitoringOutInbyServiceData now = data[i];
			
			if (!now.codigoinsumo.equals(an_codigo_consumo)|| !now.proveedor.equals(and_proveedor)){
				ReportMonitoringOutInbyServiceData objSaldo=obtieneSaldo(data[i].proveedor, data[i].codigoinsumo,dataSaldos);
				saldo=new BigDecimal(objSaldo.saldo).setScale(5, RoundingMode.HALF_UP);
				objSaldo.saldo=saldo.toString();
				objSaldo.codigoinsumo=now.codigoinsumo;
				objSaldo.insumo=now.insumo;
				objSaldo.proveedor=now.proveedor;
				
				movimientos.add(objSaldo);
				
				if(!now.proveedor.equals(and_proveedor)){//SI EL PROVEEDOR CAMBIA Y AUN QUEDAN SALDOS DE INSUMOS
					for(int k=0;k<dataSaldos.length;k++){
						if(and_proveedor.equals(dataSaldos[k].proveedor) && !dataSaldos[k].nroordenservicio.equals("usada")){
							dataSaldos[k].saldo=new BigDecimal(dataSaldos[k].saldo).setScale(5, RoundingMode.HALF_UP).toString();
							movimientos.add(dataSaldos[k]);
						}
					}
				}
				
				an_codigo_consumo=now.codigoinsumo;
				and_proveedor=now.proveedor;
			}
			
			saldo= saldo.add(new BigDecimal(now.cantidaddesalida)).subtract(new BigDecimal(now.cantidadeentrada));
			saldo=saldo.setScale(5, RoundingMode.HALF_UP);
			now.saldo=saldo.toString();
			now.cantidaddesalida=new BigDecimal(now.cantidaddesalida).setScale(5, RoundingMode.HALF_UP).toString();
			now.cantidadeentrada=new BigDecimal(now.cantidadeentrada).setScale(5, RoundingMode.HALF_UP).toString();

			movimientos.add(now);
		}
		
		for(int k=0;k<dataSaldos.length;k++){
			if(dataSaldos[k].nroordenservicio==null || !dataSaldos[k].nroordenservicio.equals("usada")){
				dataSaldos[k].saldo=new BigDecimal(dataSaldos[k].saldo).setScale(5, RoundingMode.HALF_UP).toString();
				movimientos.add(dataSaldos[k]);
			}
		}
		
		dataUltima=new ReportMonitoringOutInbyServiceData [movimientos.size()];
		
		for(int i= 0 ; i< movimientos.size();i++){
			dataUltima[i]=movimientos.get(i);
		}

		String strSubtitle = (Utility.messageBD(this, "LegalEntity", vars.getLanguage()) + ": ")
				+ ReportMonitoringOutInbyServiceData.selectCompany(this, vars.getClient()) + "\n" + "RUC:"
				+ ReportMonitoringOutInbyServiceData.selectRucOrg(this, strOrg) + "\n";

		if (!("0".equals(strOrg)))
			strSubtitle += (Utility.messageBD(this, "OBUIAPP_Organization", vars.getLanguage()) + ": ")
					+ ReportMonitoringOutInbyServiceData.selectOrg(this, strOrg) + "\n";

		// if (!"".equals(strDateFrom) || !"".equals(strDateTo))
		// strSubtitle += (Utility.messageBD(this, "From", vars.getLanguage()) +
		// ": ") + strDateFrom
		// + " " + (Utility.messageBD(this, "OBUIAPP_To", vars.getLanguage()) +
		// ": ") + strDateTo
		// + "\n";
		String strOutput = "pdf";
		String strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportMonitoringOutInbyService.jrxml";

		HashMap<String, Object> parameters = new HashMap<String, Object>();
		// parameters.put("Subtitle", strSubtitle);
		// parameters.put("Ruc",
		// ReportMonitoringOutInbyServiceData.selectRucOrg(this, strOrg));
		// parameters.put("organizacion",
		// ReportMonitoringOutInbyServiceData.selectSocialName(this,
		// vars.getClient()));
		parameters.put("organizacion", ReportMonitoringOutInbyServiceData.selectSocialName(this, strOrg));
		// parameters.put("dateFrom", StringToDate(strDateFrom));
		// parameters.put("dateTo", StringToDate(strDateTo));
		parameters.put("dateFrom", StringToDate(strDateFrom));
		parameters.put("dateTo", StringToDate(strDateTo));
		renderJR(vars, response, strReportName, "Seguimiento_Salida_Entrada_por_Servicio", strOutput, parameters, dataUltima,
				null);
	}
	
	
	
	
	
	private ReportMonitoringOutInbyServiceData obtieneSaldo(String proveedor,String codigoInsumo,ReportMonitoringOutInbyServiceData [] listaSaldos ){
		
		for(int i=0;i<listaSaldos.length;i++){
			if(listaSaldos[i].proveedor.equalsIgnoreCase(proveedor) && listaSaldos[i].codigoinsumo.equals(codigoInsumo)){
				listaSaldos[i].nroordenservicio="usada";
				return listaSaldos[i];
			}
		}
		ReportMonitoringOutInbyServiceData a= new ReportMonitoringOutInbyServiceData();
		a.saldo="0.00000";
		a.proveedor=proveedor;
		a.codigoinsumo=codigoInsumo;
		a.tipoLinea="saldo";
		return a;
	}
	
	
	
	
	

	private void printPageXLS(HttpServletRequest request, HttpServletResponse response, VariablesSecureApp vars,
			String strDateFrom, String strDateTo, String strOrg, String documentno, String strRecord)
					throws IOException, ServletException {

		ReportMonitoringOutInbyServiceData[] data = null;
		ReportMonitoringOutInbyServiceData[] dataUltima = null;

		ReportMonitoringOutInbyServiceData[] dataSaldos = null;
		ArrayList<ReportMonitoringOutInbyServiceData> movimientos = new ArrayList<ReportMonitoringOutInbyServiceData>();


		String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
		String strOrgFamily = getFamily(strTreeOrg, strOrg);
		String strClient = Utility.getContext(this, vars, "#User_Client", "ReportPaymentImportSchedule");

		data = ReportMonitoringOutInbyServiceData.selectOrders(this, strClient,strOrgFamily, strDateFrom, strDateTo, documentno);
		dataSaldos = ReportMonitoringOutInbyServiceData.select_saldos(this, strClient,
        strOrgFamily , strDateFrom);

		if (data.length == 0) {
			advisePopUp(request, response, "WARNING", Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
					Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
			return;
		}
				
		String and_proveedor="";
		String an_codigo_consumo="";
		BigDecimal saldo= BigDecimal.ZERO;

		for( int i=0 ; i< data.length ;i++){
			ReportMonitoringOutInbyServiceData now = data[i];
			
			if (!now.codigoinsumo.equals(an_codigo_consumo)|| !now.proveedor.equals(and_proveedor)){
				ReportMonitoringOutInbyServiceData objSaldo=obtieneSaldo(data[i].proveedor, data[i].codigoinsumo,dataSaldos);
				saldo=new BigDecimal(objSaldo.saldo).setScale(5, RoundingMode.HALF_UP);
				objSaldo.saldo=saldo.toString();
				objSaldo.codigoinsumo=now.codigoinsumo;
				objSaldo.insumo=now.insumo;
				objSaldo.proveedor=now.proveedor;
				
				movimientos.add(objSaldo);
				
				if(!now.proveedor.equals(and_proveedor)){//SI EL PROVEEDOR CAMBIA Y AUN QUEDAN SALDOS DE INSUMOS
					for(int k=0;k<dataSaldos.length;k++){
						if(and_proveedor.equals(dataSaldos[k].proveedor) && !dataSaldos[k].nroordenservicio.equals("usada")){
							dataSaldos[k].saldo=new BigDecimal(dataSaldos[k].saldo).setScale(5, RoundingMode.HALF_UP).toString();
							movimientos.add(dataSaldos[k]);
						}
					}
				}
				
				an_codigo_consumo=now.codigoinsumo;
				and_proveedor=now.proveedor;
			}
			
			saldo= saldo.add(new BigDecimal(now.cantidaddesalida)).subtract(new BigDecimal(now.cantidadeentrada));
			saldo=saldo.setScale(5, RoundingMode.HALF_UP);
			now.saldo=saldo.toString();
			now.cantidaddesalida=new BigDecimal(now.cantidaddesalida).setScale(5, RoundingMode.HALF_UP).toString();
			now.cantidadeentrada=new BigDecimal(now.cantidadeentrada).setScale(5, RoundingMode.HALF_UP).toString();

			movimientos.add(now);
		}
		
		for(int k=0;k<dataSaldos.length;k++){
			if(dataSaldos[k].nroordenservicio==null || !dataSaldos[k].nroordenservicio.equals("usada")){
				dataSaldos[k].saldo=new BigDecimal(dataSaldos[k].saldo).setScale(5, RoundingMode.HALF_UP).toString();
				movimientos.add(dataSaldos[k]);
			}
		}
		
		dataUltima=new ReportMonitoringOutInbyServiceData [movimientos.size()];
		
		for(int i= 0 ; i< movimientos.size();i++){
			dataUltima[i]=movimientos.get(i);
		}

		String strSubtitle = (Utility.messageBD(this, "LegalEntity", vars.getLanguage()) + ": ")
				+ ReportMonitoringOutInbyServiceData.selectCompany(this, vars.getClient()) + "\n" + "RUC:"
				+ ReportMonitoringOutInbyServiceData.selectRucOrg(this, strOrg) + "\n";

		if (!("0".equals(strOrg)))
			strSubtitle += (Utility.messageBD(this, "OBUIAPP_Organization", vars.getLanguage()) + ": ")
					+ ReportMonitoringOutInbyServiceData.selectOrg(this, strOrg) + "\n";

		// if (!"".equals(strDateFrom) || !"".equals(strDateTo))
		// strSubtitle += (Utility.messageBD(this, "From", vars.getLanguage()) +
		// ": ") + strDateFrom
		// + " " + (Utility.messageBD(this, "OBUIAPP_To", vars.getLanguage()) +
		// ": ") + strDateTo
		// + "\n";
		String strOutput = "xls";
		String strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportMonitoringOutInbyServiceExcel.jrxml";

		HashMap<String, Object> parameters = new HashMap<String, Object>();
		// parameters.put("Subtitle", strSubtitle);
		// parameters.put("Ruc",
		// ReportMonitoringOutInbyServiceData.selectRucOrg(this, strOrg));
		// parameters.put("organizacion",
		// ReportMonitoringOutInbyServiceData.selectSocialName(this,
		// vars.getClient()));
		parameters.put("organizacion", ReportMonitoringOutInbyServiceData.selectSocialName(this, strOrg));
		// parameters.put("dateFrom", StringToDate(strDateFrom));
		// parameters.put("dateTo", StringToDate(strDateTo));

		parameters.put("dateFrom", StringToDate(strDateFrom));
		parameters.put("dateTo", StringToDate(strDateTo));
		renderJR(vars, response, strReportName, "Seguimiento_Salida_Entrada_por_Servicio", strOutput, parameters, dataUltima,
				null);
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
		return "Servlet ReportMonitoringOutInbyService. This Servlet was made by Pablo Sarobe modified by everybody";
	} // end of getServletInfo() method
}
