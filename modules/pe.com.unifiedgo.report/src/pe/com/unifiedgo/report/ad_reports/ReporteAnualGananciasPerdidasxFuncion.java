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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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


public class ReporteAnualGananciasPerdidasxFuncion extends
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
					"ReporteAnualGananciasPerdidasxFuncion|DateFrom", "");
			String strDateTo = vars.getGlobalVariable("inpDateTo",
					"ReporteAnualGananciasPerdidasxFuncion|DateTo", "");
			String strOrg = vars.getGlobalVariable("inpOrg",
					"ReporteAnualGananciasPerdidasxFuncion|Org", "0");
			String strRecord = vars.getGlobalVariable("inpRecord",
					"ReporteAnualGananciasPerdidasxFuncion|Record", "");
			String strTable = vars.getGlobalVariable("inpTable",
					"ReporteAnualGananciasPerdidasxFuncion|Table", "");
			printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg,
					strTable, strRecord);

		} else if (vars.commandIn("DIRECT")) {
			String strTable = vars.getGlobalVariable("inpTable",
					"ReporteAnualGananciasPerdidasxFuncion|Table");
			String strRecord = vars.getGlobalVariable("inpRecord",
					"ReporteAnualGananciasPerdidasxFuncion|Record");

			setHistoryCommand(request, "DIRECT");
			vars.setSessionValue(
					"ReporteAnualGananciasPerdidasxFuncion.initRecordNumber",
					"0");
			printPageDataSheet(response, vars, "", "", "", strTable, strRecord);

		} else if (vars.commandIn("FIND")) {
			String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
					"ReporteAnualGananciasPerdidasxFuncion|DateFrom");
			String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
					"ReporteAnualGananciasPerdidasxFuncion|DateTo");
			String strOrg = vars.getGlobalVariable("inpOrg",
					"ReporteAnualGananciasPerdidasxFuncion|Org", "0");
			vars.setSessionValue(
					"ReporteAnualGananciasPerdidasxFuncion.initRecordNumber",
					"0");
			setHistoryCommand(request, "DEFAULT");
			printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg,
					"", "");

		} else if (vars.commandIn("PDF", "XLS")) {
			if (log4j.isDebugEnabled())
				log4j.debug("PDF");
			String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
					"ReporteAnualGananciasPerdidasxFuncion|DateFrom");
			String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
					"ReporteAnualGananciasPerdidasxFuncion|DateTo");
			String strOrg = vars.getGlobalVariable("inpOrg",
					"ReporteAnualGananciasPerdidasxFuncion|Org", "0");
			
			String cuenta = vars.getStringParameter("inpCuenta");
			String anio = vars.getStringParameter("inpAnio");
			String mes1 = vars.getStringParameter("inpMes1");
			String mes2   = vars.getStringParameter("inpMes2");


			String strTable = vars.getStringParameter("inpTable");
			String strRecord = vars.getStringParameter("inpRecord");
			setHistoryCommand(request, "DEFAULT");
			printPagePDF(request,response, vars, strDateFrom, strDateTo, strOrg,
					strTable, strRecord,cuenta,anio,mes1,mes2);

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
		ReporteAnualGananciasPerdidasxFuncionData[] data = null;
		String strPosition = "0";
		ToolBar toolbar = new ToolBar(this, vars.getLanguage(),
				"ReporteAnualGananciasPerdidasxFuncion", false, "", "",
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
//					"submitCommandForm('XLS', false, null, 'ReporteAnualGananciasPerdidasxFuncion.xls', 'EXCEL');return false;");
			xmlDocument = xmlEngine
					.readXmlTemplate(
							"pe/com/unifiedgo/report/ad_reports/ReporteAnualGananciasPerdidasxFuncion",
							discard).createXmlDocument();
			data = ReporteAnualGananciasPerdidasxFuncionData.set("0");
			data[0].rownum = "0";
		}
		xmlDocument.setParameter("toolbar", toolbar.toString());
		try {
			WindowTabs tabs = new WindowTabs(this, vars,
					"pe.com.unifiedgo.report.ad_reports.ReporteAnualGananciasPerdidasxFuncion");
			xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
			xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
			xmlDocument.setParameter("childTabContainer", tabs.childTabs());
			xmlDocument.setParameter("theme", vars.getTheme());
			NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
					"ReporteAnualGananciasPerdidasxFuncion.html",
					classInfo.id, classInfo.type, strReplaceWith,
					tabs.breadcrumb());
			xmlDocument.setParameter("navigationBar", nav.toString());
			LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
					"ReporteAnualGananciasPerdidasxFuncion.html",
					strReplaceWith);
			xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
		} catch (Exception ex) {
			throw new ServletException(ex);
		}
		{
			OBError myMessage = vars
					.getMessage("ReporteAnualGananciasPerdidasxFuncion");
			vars.removeMessage("ReporteAnualGananciasPerdidasxFuncion");
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
							"ReporteAnualGananciasPerdidasxFuncion"),
					Utility.getContext(this, vars, "#User_Client",
							"ReporteAnualGananciasPerdidasxFuncion"), '*');
			comboTableData.fillParameters(null,
					"ReporteAnualGananciasPerdidasxFuncion", "");
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
		xmlDocument.setParameter("paramAniosArray", Utility.arrayInfinitasEntradas("idanio;anio;idorganizacion","arrAnios",
	  			ReporteAnualGananciasPerdidasxFuncionData
				.select_anios(this)));
		
		ReporteAnualGananciasPerdidasxFuncionData [] dataMeses = new ReporteAnualGananciasPerdidasxFuncionData[12];
		String meses [] = {"ENERO","FEBRERO","MARZO","ABRIL","MAYO","JUNIO","JULIO","AGOSTO","SETIEMBRE","OCTUBRE","NOVIEMBRE","DICIEMBRE"};
		for(int i = 0 ; i<meses.length;i++){
			dataMeses[i]=new ReporteAnualGananciasPerdidasxFuncionData();
			dataMeses[i].mes=meses[i];
			dataMeses[i].ordenMes=(i+1)+"";
		}
		
		xmlDocument.setParameter("paramMesesArray", Utility.arrayInfinitasEntradas("ordenMes;mes","arrMeses",
	  			dataMeses));

		
		vars.setSessionValue(
				"ReporteAnualGananciasPerdidasxFuncion|Record", strRecord);
		vars.setSessionValue(
				"ReporteAnualGananciasPerdidasxFuncion|Table", strTable);

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
			String strOrg, String strTable, String strRecord, String cuenta , String anio,String mes1,String mes2)
			throws IOException, ServletException {
				
		if(cuenta!=null && !cuenta.equals("")){
			cuenta= cuenta+"%";
		}
		
		ReporteAnualGananciasPerdidasxFuncionData[] factAccounts = null;
		ReporteAnualGananciasPerdidasxFuncionData[] cuentasSubCategorias = null;
		ReporteAnualGananciasPerdidasxFuncionData[] dataFinal = null;

		ArrayList<ReporteAnualGananciasPerdidasxFuncionData> listData = new ArrayList<ReporteAnualGananciasPerdidasxFuncionData>();

		HashMap<String, ArrayList<ReporteAnualGananciasPerdidasxFuncionData>> grupoFactsPorCuentaSubCategoria = new HashMap<String, ArrayList<ReporteAnualGananciasPerdidasxFuncionData>>();
		HashMap<String, ArrayList<ReporteAnualGananciasPerdidasxFuncionData>> grupoSubCategoriasPorMes = new HashMap<String, ArrayList<ReporteAnualGananciasPerdidasxFuncionData>>();
		HashMap<String, ArrayList<ReporteAnualGananciasPerdidasxFuncionData>> grupoSubCategoriasAnual = new HashMap<String, ArrayList<ReporteAnualGananciasPerdidasxFuncionData>>();

		
		HashMap<String, ReporteAnualGananciasPerdidasxFuncionData> grupoCuentasSubcategoriaPorMes = new HashMap<String, ReporteAnualGananciasPerdidasxFuncionData>();

		String meses [] = {"ENERO","FEBRERO","MARZO","ABRIL","MAYO","JUNIO","JULIO","AGOSTO","SETIEMBRE","OCTUBRE","NOVIEMBRE","DICIEMBRE"};
		
		String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
		String strOrgFamily = getFamily(strTreeOrg, strOrg);
		
		String strOutput;
		String strReportName;
		
		factAccounts = ReporteAnualGananciasPerdidasxFuncionData
				.select_facts(this,strOrgFamily, Utility.getContext(this, vars,
						"#User_Client",
						"ReporteAnualGananciasPerdidasxFuncion"),
						mes1,mes2,anio );
		
		cuentasSubCategorias = ReporteAnualGananciasPerdidasxFuncionData
				.select_categorias(this,strOrgFamily, Utility.getContext(this, vars,
						"#User_Client",
						"ReporteAnualGananciasPerdidasxFuncion"));
		
		//SELECCIONANDO Y  AGRUPANDO FACTS PARA CADA CUENTA% DE CADA SUBCATEGORIA
		
		String cuentaSubcategoria="";
		for(int i=0;i<factAccounts.length;i++){
			
			for ( int j= 0 ; j < cuentasSubCategorias.length ;j ++){
				
				cuentaSubcategoria=cuentasSubCategorias[j].account.replace("-", "").replace("%", "");
				
				if(factAccounts[i].acctvalue.startsWith(cuentaSubcategoria)){
					
					ArrayList<ReporteAnualGananciasPerdidasxFuncionData> factsCuentasSubcategoria=new ArrayList<ReporteAnualGananciasPerdidasxFuncionData>();
					
					if(grupoFactsPorCuentaSubCategoria.containsKey(cuentaSubcategoria))
						factsCuentasSubcategoria = grupoFactsPorCuentaSubCategoria.get(cuentaSubcategoria);

					factsCuentasSubcategoria.add(factAccounts[i]);
					grupoFactsPorCuentaSubCategoria.put(cuentaSubcategoria, factsCuentasSubcategoria);
					
//					break;
				}
			}
		}
		
		// OBTENIENDO TOTALES PARA CADA CUENTA DE CADA SUBCATEGORIA --> CONSOLIDANDO POR SUBCATEGORIA Y MES
		
		BigDecimal montoCuentaSubCategoria=BigDecimal.ZERO;
		for (int k=Integer.parseInt(mes1)-1;k<Integer.parseInt(mes2);k++){
			
			for ( int i= 0 ; i < cuentasSubCategorias.length ;i ++){
				
				cuentaSubcategoria=cuentasSubCategorias[i].account.replace("-", "").replace("%", "");
								
				montoCuentaSubCategoria=totalCuentaSubCategoriaxMes(meses[k],grupoFactsPorCuentaSubCategoria.get(cuentaSubcategoria),cuentasSubCategorias[i]);
								
				ReporteAnualGananciasPerdidasxFuncionData o = new ReporteAnualGananciasPerdidasxFuncionData();
				o.order1=cuentasSubCategorias[i].order1;
				o.order2=cuentasSubCategorias[i].order2;
				o.categoria=cuentasSubCategorias[i].categoria;
				o.subCategoria=cuentasSubCategorias[i].subCategoria;
				o.mes=meses[k];
				o.ordenMes=(k+1)+"";
				o.considerar=cuentasSubCategorias[i].considerar;
				o.signo=cuentasSubCategorias[i].signo;
				o.montoCuentaMes=montoCuentaSubCategoria.multiply(new BigDecimal(o.considerar)).toString(); 
				
				//PARA CONSOLIDAR POR SUBCATEGORIA
				if(grupoCuentasSubcategoriaPorMes.containsKey(o.mes+o.categoria+o.subCategoria)){
					ReporteAnualGananciasPerdidasxFuncionData ant=grupoCuentasSubcategoriaPorMes.get(o.mes+o.categoria+o.subCategoria);
					BigDecimal montoNuevo = new BigDecimal (ant.montoCuentaMes).add(montoCuentaSubCategoria);
					o.montoCuentaMes=montoNuevo.toString();
				}
				grupoCuentasSubcategoriaPorMes.put(o.mes+o.categoria+o.subCategoria, o);
			}
		}
		
		listData= new ArrayList<ReporteAnualGananciasPerdidasxFuncionData> ( grupoCuentasSubcategoriaPorMes.values());
		dataFinal=new ReporteAnualGananciasPerdidasxFuncionData[listData.size()];
		
		for(int i=0;i<listData.size();i++){
			BigDecimal montoFinal = new BigDecimal(listData.get(i).montoCuentaMes);
			montoFinal=montoFinal.multiply(new BigDecimal(listData.get(i).signo).multiply(new BigDecimal("-1")));
			listData.get(i).montoCuentaMes=montoFinal.toString();			
			dataFinal[i]=listData.get(i);
		}
		
						
		if (vars.commandIn("PDF")) {
			///PARA OBTENER EL MONTO ACUMULADO POR SUBCATEGORIA (SIRVE SOLO PARA EL PDF), POR QUE SE NECESITA UN TOTAL ACUMULADO

			//1- SE HALLARA EL TOTAL ANUAL PARA CADA CATEGORIA - SUBCATEGORIA , SE AGREGARA ESE TOTAL PARA CADA SUBCATEGORIA COMO SI FUERA UNA COLUMNA 
//				FINAL ( TRECEAVO MES )
			for(int i=0;i<listData.size();i++){
				
				ArrayList<ReporteAnualGananciasPerdidasxFuncionData> subCategoriaAnual = new ArrayList<ReporteAnualGananciasPerdidasxFuncionData>();
				if(grupoSubCategoriasAnual.containsKey(listData.get(i).categoria+"&&"+listData.get(i).subCategoria)){
					subCategoriaAnual=grupoSubCategoriasAnual.get(listData.get(i).categoria+"&&"+listData.get(i).subCategoria);
				}
				subCategoriaAnual.add(listData.get(i));
				grupoSubCategoriasAnual.put(listData.get(i).categoria+"&&"+listData.get(i).subCategoria, subCategoriaAnual);
			}
			
			ArrayList <String> listCategoriaSubcategorias = new ArrayList<String>(grupoSubCategoriasAnual.keySet());
			for (int i=0;i<listCategoriaSubcategorias.size();i++){
				montoCategoriaSubcategoriaAnual(grupoSubCategoriasAnual.get(listCategoriaSubcategorias.get(i)),listData);
			}
			
			
			//2- SE HALLA EL ACUMULADO DE CADA SUBCATEGORIA MES A MES 
			for(int i=0;i<listData.size();i++){
				
				ArrayList<ReporteAnualGananciasPerdidasxFuncionData> subCategoriaMes = new ArrayList<ReporteAnualGananciasPerdidasxFuncionData>();
				if(grupoSubCategoriasPorMes.containsKey(listData.get(i).mes)){
					subCategoriaMes=grupoSubCategoriasPorMes.get(listData.get(i).mes);
				}
				subCategoriaMes.add(listData.get(i));
				grupoSubCategoriasPorMes.put(listData.get(i).mes, subCategoriaMes);
			}
			
			ArrayList <String> listMeses = new ArrayList<String>(grupoSubCategoriasPorMes.keySet());

			for(int i=0;i<listMeses.size();i++){
					acumuladoPorCategoria(grupoSubCategoriasPorMes.get(listMeses.get(i)));
			}
			
//			dataFinal=new ReporteAnualGananciasPerdidasxFuncionData[listData.size()];
			dataFinal= listData.toArray(new ReporteAnualGananciasPerdidasxFuncionData [listData.size()]);

			strOutput = "pdf";
			strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteAnualGananciasPerdidasxFuncion.jrxml";
		} else {
			strOutput = "xls";
			strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteAnualGananciasPerdidasxFuncionExcel.jrxml";
		}


      if (factAccounts.length==0) {
          advisePopUp(request, response, "WARNING", Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()), Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
          return;
        }

		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("Ruc", ReporteAnualGananciasPerdidasxFuncionData
				.selectRucOrg(this, strOrg));
		parameters.put("organizacion",
				ReporteAnualGananciasPerdidasxFuncionData
						.selectSocialName(this, strOrg));

		parameters.put("dateFrom", StringToDate(strDateFrom));
		parameters.put("dateTo", StringToDate(strDateTo));
		parameters.put("ANIO", anio);


		renderJR(vars, response, strReportName,
				"Reporte_Consolidado_Cuentas_Anual", strOutput,
				parameters, dataFinal, null);
	}
	
	
	private void montoCategoriaSubcategoriaAnual(ArrayList<ReporteAnualGananciasPerdidasxFuncionData> facts, 
			ArrayList<ReporteAnualGananciasPerdidasxFuncionData> listPrincipal){
		
		BigDecimal monto=BigDecimal.ZERO;
		
		if(facts == null || facts.size()==0)return ;
		
		for(int i=0;i<facts.size();i++){
			monto = monto.add(new BigDecimal(facts.get(i).montoCuentaMes));
		}
		
		ReporteAnualGananciasPerdidasxFuncionData o = new ReporteAnualGananciasPerdidasxFuncionData();
		o.order1=facts.get(0).order1;
		o.order2=facts.get(0).order2;
		o.categoria=facts.get(0).categoria;
		o.subCategoria=facts.get(0).subCategoria;
		o.mes="TOTAL";
		o.ordenMes="13";
		o.montoCuentaMes=monto.toString(); 
		
		listPrincipal.add(o);
		return ;
	}
	
	
	private void acumuladoPorCategoria(ArrayList<ReporteAnualGananciasPerdidasxFuncionData> facts){
		
		BigDecimal monto=BigDecimal.ZERO;
		
		if(facts == null || facts.size()==0)return;
		
		Collections.sort(facts,
				new Comparator<ReporteAnualGananciasPerdidasxFuncionData>() {
					@Override
					public int compare(
							ReporteAnualGananciasPerdidasxFuncionData p1,
							ReporteAnualGananciasPerdidasxFuncionData p2) {

						int resultado = new String(p1.order1)
								.compareTo(new String(p2.order1));
						if (resultado != 0) {
							return resultado;
						}
						
						resultado = new String(p1.categoria).compareTo(new String(
								p2.categoria));
						if (resultado != 0) {
							return resultado;
						}

						resultado = new String(p1.order2).compareTo(new String(
								p2.order2));
						if (resultado != 0) {
							return resultado;
						}
		
						resultado = new String(p1.subCategoria).compareTo(new String(
								p2.subCategoria));
						if (resultado != 0) {
							return resultado;
						}
						return resultado;
					}
				});
		
		for(int i=0;i<facts.size();i++){
			monto = monto.add(new BigDecimal(facts.get(i).montoCuentaMes));
			facts.get(i).montoCategoriaAcumulado=monto.toString();
		}
	}
	
	private BigDecimal totalCuentaSubCategoriaxMes(String mes, ArrayList<ReporteAnualGananciasPerdidasxFuncionData> facts,
			ReporteAnualGananciasPerdidasxFuncionData cuentaSubCategoria){
		
		BigDecimal montoCuentaSubCategoria=BigDecimal.ZERO;
		BigDecimal monto=BigDecimal.ZERO;
		
		if(facts==null)return monto;//cuando no tiene facts asociados a la cuenta de la subcategoria

		for(int j=0;j<facts.size();j++){
			
			if(facts.get(j).mes.equals(mes)){
				
				monto=new BigDecimal(facts.get(j).factamt);
				
				if(cuentaSubCategoria.consider.equals("Y") && cuentaSubCategoria.considerwhen.equals("SCO_PLUS")){
					monto=monto.compareTo(BigDecimal.ZERO)>0?monto:BigDecimal.ZERO;
				}else 	if(cuentaSubCategoria.consider.equals("Y") && !cuentaSubCategoria.considerwhen.equals("SCO_PLUS")){
					monto=monto.compareTo(BigDecimal.ZERO)<0?monto:BigDecimal.ZERO;
				}
				montoCuentaSubCategoria=montoCuentaSubCategoria.add(monto);
			}
		}	
		
		return montoCuentaSubCategoria;
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
		return "Servlet ReporteAnualGananciasPerdidasxFuncion. This Servlet was made by Pablo Sarobe modified by everybody";
	} // end of getServletInfo() method
}
