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
import java.util.Vector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

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

public class ReporteAcumuladosMensualAnual extends
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
					"ReporteAcumuladosMensualAnual|DateFrom", "");
			String strDateTo = vars.getGlobalVariable("inpDateTo",
					"ReporteAcumuladosMensualAnual|DateTo", "");
			String strOrg = vars.getGlobalVariable("inpOrg",
					"ReporteAcumuladosMensualAnual|Org", "0");
			String strRecord = vars.getGlobalVariable("inpRecord",
					"ReporteAcumuladosMensualAnual|Record", "");
			String strTable = vars.getGlobalVariable("inpTable",
					"ReporteAcumuladosMensualAnual|Table", "");
			printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg,
					strTable, strRecord);

		} else if (vars.commandIn("DIRECT")) {
			String strTable = vars.getGlobalVariable("inpTable",
					"ReporteAcumuladosMensualAnual|Table");
			String strRecord = vars.getGlobalVariable("inpRecord",
					"ReporteAcumuladosMensualAnual|Record");

			setHistoryCommand(request, "DIRECT");
			vars.setSessionValue(
					"ReporteAcumuladosMensualAnual.initRecordNumber",
					"0");
			printPageDataSheet(response, vars, "", "", "", strTable, strRecord);

		} else if (vars.commandIn("FIND")) {
			String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
					"ReporteAcumuladosMensualAnual|DateFrom");
			String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
					"ReporteAcumuladosMensualAnual|DateTo");
			String strOrg = vars.getGlobalVariable("inpOrg",
					"ReporteAcumuladosMensualAnual|Org", "0");
			vars.setSessionValue(
					"ReporteAcumuladosMensualAnual.initRecordNumber",
					"0");
			setHistoryCommand(request, "DEFAULT");
			printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg,
					"", "");

		} else if (vars.commandIn("PDF", "XLS")) {
			if (log4j.isDebugEnabled())
				log4j.debug("PDF");
			String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
					"ReporteAcumuladosMensualAnual|DateFrom");
			String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
					"ReporteAcumuladosMensualAnual|DateTo");
			String strOrg = vars.getGlobalVariable("inpOrg",
					"ReporteAcumuladosMensualAnual|Org", "0");
			
			String anio1 = vars.getStringParameter("inpAnio1");
			String anio2 = vars.getStringParameter("inpAnio2");
			String mes1 = vars.getStringParameter("inpMes1");
			String mes2 = vars.getStringParameter("inpMes2");
			String strTipoPeriodo = vars.getStringParameter("inpTipoPeriodo");
			String strTipoReporte = vars.getStringParameter("inpTipoReporte");

			String strTable = vars.getStringParameter("inpTable");
			String strRecord = vars.getStringParameter("inpRecord");
			setHistoryCommand(request, "DEFAULT");
			printPagePDF(request,response, vars, strDateFrom, strDateTo, strOrg,
					strTable, strRecord,anio1,anio2,mes1,mes2, strTipoPeriodo,strTipoReporte);

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
		ReporteAcumuladosMensualAnualData[] data = null;
		String strPosition = "0";
		ToolBar toolbar = new ToolBar(this, vars.getLanguage(),
				"ReporteAcumuladosMensualAnual", false, "", "",
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
//					"submitCommandForm('XLS', false, null, 'ReporteAcumuladosMensualAnual.xls', 'EXCEL');return false;");
			xmlDocument = xmlEngine
					.readXmlTemplate(
							"pe/com/unifiedgo/report/ad_reports/ReporteAcumuladosMensualAnual",
							discard).createXmlDocument();
			data = ReporteAcumuladosMensualAnualData.set("0");
			data[0].rownum = "0";
		}
		xmlDocument.setParameter("toolbar", toolbar.toString());
		try {
			WindowTabs tabs = new WindowTabs(this, vars,
					"pe.com.unifiedgo.report.ad_reports.ReporteAcumuladosMensualAnual");
			xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
			xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
			xmlDocument.setParameter("childTabContainer", tabs.childTabs());
			xmlDocument.setParameter("theme", vars.getTheme());
			NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
					"ReporteAcumuladosMensualAnual.html",
					classInfo.id, classInfo.type, strReplaceWith,
					tabs.breadcrumb());
			xmlDocument.setParameter("navigationBar", nav.toString());
			LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
					"ReporteAcumuladosMensualAnual.html",
					strReplaceWith);
			xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
		} catch (Exception ex) {
			throw new ServletException(ex);
		}
		{
			OBError myMessage = vars
					.getMessage("ReporteAcumuladosMensualAnual");
			vars.removeMessage("ReporteAcumuladosMensualAnual");
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
							"ReporteAcumuladosMensualAnual"),
					Utility.getContext(this, vars, "#User_Client",
							"ReporteAcumuladosMensualAnual"), '*');
			comboTableData.fillParameters(null,
					"ReporteAcumuladosMensualAnual", "");
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
		xmlDocument.setParameter("paramAniosArray", Utility.arrayInfinitasEntradas("idorganizacion;anio;","arrAnios",
	  			ReporteAcumuladosMensualAnualData
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
		
		
	    FieldProvider tiposReporte[] = new FieldProvider[3];
	    Vector<Object> vector = new Vector<Object>(0);

	      SQLReturnObject sqlReturnObject = new SQLReturnObject();
	      sqlReturnObject.setData("ID", "EERR");
	      sqlReturnObject.setData("NAME", "ESTADO DE RESULTADOS");
	      vector.add(sqlReturnObject);
	      
	       sqlReturnObject = new SQLReturnObject();
	      sqlReturnObject.setData("ID", "SF");
	      sqlReturnObject.setData("NAME", "SITUACION FINANCIERA");
	      vector.add(sqlReturnObject);
	      
	       sqlReturnObject = new SQLReturnObject();
	      sqlReturnObject.setData("ID", "GGPPN");
	      sqlReturnObject.setData("NAME", "GANANCIAS Y PERDIDAS POR NATURALEZA");
	      vector.add(sqlReturnObject);

	      
	    vector.copyInto(tiposReporte);

	    xmlDocument.setData("reportC_TIPOREPORTE", "liststructure", tiposReporte);
		
		vars.setSessionValue(
				"ReporteAcumuladosMensualAnual|Record", strRecord);
		vars.setSessionValue(
				"ReporteAcumuladosMensualAnual|Table", strTable);

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
			String strOrg, String strTable, String strRecord,String anio1,String anio2,
			String mes1,String mes2,String  strTipoPeriodo,String tipoReporte)
			throws IOException, ServletException {

		ReporteAcumuladosMensualAnualData[] factAccounts = null;
		ReporteAcumuladosMensualAnualData[] subcategoriasCuenta = null;
		ReporteAcumuladosMensualAnualData[] dataFinal = null;

		ArrayList<ReporteAcumuladosMensualAnualData> listData = new ArrayList<ReporteAcumuladosMensualAnualData>();
		HashMap<String, ArrayList<ReporteAcumuladosMensualAnualData>> grupoFactsPorCuentaSubCategoria = new HashMap<String, ArrayList<ReporteAcumuladosMensualAnualData>>();
		HashMap<String, String> grupoMeses = new HashMap<String,String >();
		HashMap<String, String> grupoAnios = new HashMap<String,String >();
		
		String meses [] = {"ENERO","FEBRERO","MARZO","ABRIL","MAYO","JUNIO","JULIO","AGOSTO","SETIEMBRE","OCTUBRE","NOVIEMBRE","DICIEMBRE"};
		
		String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
		String strOrgFamily = getFamily(strTreeOrg, strOrg);
		String strCliente = Utility.getContext(this, vars,"#User_Client","ReporteAnualCentroCostos");
		
		String strOutput;
		String strReportName;
		String tipoRubro="''";
		String tituloReporte="";

		
		if(strTipoPeriodo.equals("mensual")){
			anio1=anio2;
		}else{
			mes1="1";
			mes2="12";
		}
		
		if(tipoReporte.equals("EERR")){
			 tipoRubro="'SCO_ER'";
			 tituloReporte="REPORTE ESTADO DE RESULTADOS";
		}else if(tipoReporte.equals("SF")){
//			 tipoRubro="'SCO_BG_TA','SCO_BG_TP','SCO_BG_TPI'";
			 tipoRubro="'SCO_ESF'";
			 tituloReporte="REPORTE SITUACION FINANCIERA";

		}else if(tipoReporte.equals("GGPPN")){
			 tipoRubro="'SCO_GPN'";
			 tituloReporte="REPORTE GANANCIAS Y PERDIDAS POR NATURALEZA";
		}
				
		factAccounts = ReporteAcumuladosMensualAnualData
				.select_facts(this,strOrgFamily+",'0'",strCliente,anio1,anio2,mes1,mes2 );

		subcategoriasCuenta = ReporteAcumuladosMensualAnualData
				.select_categorias(this, strOrgFamily+",'0'", strCliente,tipoRubro);
		
		//SELECCIONANDO Y  AGRUPANDO FACTS PARA CADA ANIO-MES-CUENTA DE CADA SUBCATEGORIA
		
		String cuentaSubcategoria="";
		
		for ( int j= 0 ; j < subcategoriasCuenta.length ;j ++){

			cuentaSubcategoria=subcategoriasCuenta[j].account.replace("-", "").replace("%", ""); 

			for(int i=0;i<factAccounts.length;i++){
				
				if(factAccounts[i].acctvalue.startsWith(cuentaSubcategoria)){
					
					String key = factAccounts[i].anio+factAccounts[i].mes+"//"+subcategoriasCuenta[j].order1+"//"+
							subcategoriasCuenta[j].order2+"//"+cuentaSubcategoria;
					
					ArrayList<ReporteAcumuladosMensualAnualData> factsCuentasSubcategoria=new ArrayList<ReporteAcumuladosMensualAnualData>();
					if(grupoFactsPorCuentaSubCategoria.containsKey(key))
						factsCuentasSubcategoria = grupoFactsPorCuentaSubCategoria.get(key);

					factsCuentasSubcategoria.add(factAccounts[i]);
					grupoFactsPorCuentaSubCategoria.put(key, factsCuentasSubcategoria);		
					
					if(!grupoAnios.containsKey( factAccounts[i].anio)){
						grupoAnios.put(factAccounts[i].anio, factAccounts[i].anio);
					}
					if(!grupoMeses.containsKey( factAccounts[i].mes)){
						grupoMeses.put(factAccounts[i].ordenMes, factAccounts[i].mes);
					}
				}
			}
		}
		
		listData=getConsolidadoAnioMesCategoriaSubcategoria(subcategoriasCuenta, grupoFactsPorCuentaSubCategoria,
				new ArrayList<String>( Arrays.asList(meses)),new ArrayList<String>( grupoAnios.values()));

		//CADA TIPO DE REPORTE TIENE SUS PARTICULARIDADES AL MOMENTO MOSTRAR EL SIGNO DE LOS MONTOS DE LAS SUBCATEGORIAS
		if(tipoReporte.equals("EERR")){
			dataFinalEstadoResultado(listData);
		}if(tipoReporte.equals("SF")){
			dataFinalSituacionFinanciera(listData);	
		}if(tipoReporte.equals("GGPPN")){
			dataFinalGGPPNaturaleza(listData);
		}
		
		if(strTipoPeriodo.equals("anual")){
			listData=getTotalPorAnio(listData, new ArrayList<String>( grupoAnios.values()));
			tituloReporte+=" POR AÑO : "+anio1+" - "+anio2;
		}else {
			listData=getTotalPorMes(listData);
			tituloReporte+=" MENSUAL - "+anio2;
		}
		
		dataFinal= listData.toArray(new ReporteAcumuladosMensualAnualData [listData.size()]);

       if (factAccounts.length==0) {
          advisePopUp(request, response, "WARNING", Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()), Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
          return;
        }
       
		if (vars.commandIn("PDF")) {
			strOutput = "pdf";
			strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteAcumuladosMensualAnual.jrxml";
		} else {
			strOutput = "xls";
			strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteAcumuladosMensualAnualExcel.jrxml";
		}

		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("Ruc", ReporteAcumuladosMensualAnualData
				.selectRUC(this, strOrg));
		parameters.put("organizacion",
				ReporteAcumuladosMensualAnualData
						.selectSocialName(this, strOrg));

		parameters.put("dateFrom", StringToDate(strDateFrom));
		parameters.put("dateTo", StringToDate(strDateTo));
		parameters.put("ANIOINI", anio1);
		parameters.put("ANIOFIN", anio2);
		parameters.put("TITULO_REPORTE", tituloReporte);
		parameters.put("TIPO_PERIODO", strTipoPeriodo);

		renderJR(vars, response, strReportName,
				"ReporteAcumuladoEstadoFinanciero", strOutput,
				parameters, dataFinal, null);
	}
	
	
	private ArrayList<ReporteAcumuladosMensualAnualData> getTotalPorMes(ArrayList<ReporteAcumuladosMensualAnualData> dataAnual){
		
		HashMap<String, ArrayList<ReporteAcumuladosMensualAnualData>> grupoFactsTodosAnios = 
				new HashMap<String, ArrayList<ReporteAcumuladosMensualAnualData>>();

		for(int i=0;i<dataAnual.size();i++){
			
			ArrayList<ReporteAcumuladosMensualAnualData> subCategoriaAnual = new ArrayList<ReporteAcumuladosMensualAnualData>();
			String key = dataAnual.get(i).order1+"//"+dataAnual.get(i).order2;
			if(grupoFactsTodosAnios.containsKey(key)){
				subCategoriaAnual=grupoFactsTodosAnios.get(key);
			}
			subCategoriaAnual.add(dataAnual.get(i));
			grupoFactsTodosAnios.put(key, subCategoriaAnual);
		}

		ArrayList <String> listCategoriaSubcategorias = new ArrayList<String>(grupoFactsTodosAnios.keySet());
		for (int i=0;i<listCategoriaSubcategorias.size();i++){
			montoCategoriaSubcategoriaAnual(grupoFactsTodosAnios.get(listCategoriaSubcategorias.get(i)),dataAnual,"mensual");
		}
		
		
		//2- SE HALLA EL ACUMULADO DE CADA SUBCATEGORIA MES A MES 
		HashMap<String, ArrayList<ReporteAcumuladosMensualAnualData>> grupoSubCategoriasPorAnio = 
				new HashMap<String, ArrayList<ReporteAcumuladosMensualAnualData>>();

		for(int i=0;i<dataAnual.size();i++){
			
			ArrayList<ReporteAcumuladosMensualAnualData> subCategoriaMes = new ArrayList<ReporteAcumuladosMensualAnualData>();
			if(grupoSubCategoriasPorAnio.containsKey(dataAnual.get(i).mes)){
				subCategoriaMes=grupoSubCategoriasPorAnio.get(dataAnual.get(i).mes);
			}
			subCategoriaMes.add(dataAnual.get(i));
			grupoSubCategoriasPorAnio.put(dataAnual.get(i).mes, subCategoriaMes);
		}
		
		ArrayList <String> listMeses = new ArrayList<String>(grupoSubCategoriasPorAnio.keySet());

		for(int i=0;i<listMeses.size();i++){
				acumuladoPorCategoria(grupoSubCategoriasPorAnio.get(listMeses.get(i)));
		}

		return dataAnual;
	}
	
	
	private ArrayList<ReporteAcumuladosMensualAnualData> getTotalPorAnio(ArrayList<ReporteAcumuladosMensualAnualData> listData,
			ArrayList<String> anios){
		
		HashMap<String, ReporteAcumuladosMensualAnualData> grupoAnioCategoriaSubcategoria = new HashMap<String, ReporteAcumuladosMensualAnualData>();
		ArrayList<ReporteAcumuladosMensualAnualData> dataAnual = new ArrayList<ReporteAcumuladosMensualAnualData>();
		//obtinee lista  de  totales por anio
			for (int j=0;j< listData.size();j++){
				
				String key=listData.get(j).anio+"//"+listData.get(j).order1+"//"+listData.get(j).order2;
				BigDecimal monto = new BigDecimal(listData.get(j).montoCuentaMes);
				BigDecimal montoAnterior=BigDecimal.ZERO;

				if(grupoAnioCategoriaSubcategoria.containsKey(key)){
					montoAnterior= new BigDecimal (grupoAnioCategoriaSubcategoria.get(key).montoCuentaMes);
					monto=monto.add(montoAnterior);
				}
				listData.get(j).montoCuentaMes=monto.toString();
				grupoAnioCategoriaSubcategoria.put(key, listData.get(j));
			}
		
		
		dataAnual=new ArrayList<ReporteAcumuladosMensualAnualData>(grupoAnioCategoriaSubcategoria.values());
		HashMap<String, ArrayList<ReporteAcumuladosMensualAnualData>> grupoFactsTodosAnios = 
				new HashMap<String, ArrayList<ReporteAcumuladosMensualAnualData>>();
		
		for(int i=0;i<dataAnual.size();i++){
			
			ArrayList<ReporteAcumuladosMensualAnualData> subCategoriaAnual = new ArrayList<ReporteAcumuladosMensualAnualData>();
			String key = dataAnual.get(i).order1+"//"+dataAnual.get(i).order2;
			if(grupoFactsTodosAnios.containsKey(key)){
				subCategoriaAnual=grupoFactsTodosAnios.get(key);
			}
			subCategoriaAnual.add(dataAnual.get(i));
			grupoFactsTodosAnios.put(key, subCategoriaAnual);
		}
		

		ArrayList <String> listCategoriaSubcategorias = new ArrayList<String>(grupoFactsTodosAnios.keySet());
		for (int i=0;i<listCategoriaSubcategorias.size();i++){
			montoCategoriaSubcategoriaAnual(grupoFactsTodosAnios.get(listCategoriaSubcategorias.get(i)),dataAnual,"anual");
		}
		
		
		//2- SE HALLA EL ACUMULADO DE CADA SUBCATEGORIA MES A MES 
		HashMap<String, ArrayList<ReporteAcumuladosMensualAnualData>> grupoSubCategoriasPorAnio = 
				new HashMap<String, ArrayList<ReporteAcumuladosMensualAnualData>>();

		for(int i=0;i<dataAnual.size();i++){
			
			ArrayList<ReporteAcumuladosMensualAnualData> subCategoriaAnio = new ArrayList<ReporteAcumuladosMensualAnualData>();
			if(grupoSubCategoriasPorAnio.containsKey(dataAnual.get(i).anio)){
				subCategoriaAnio=grupoSubCategoriasPorAnio.get(dataAnual.get(i).anio);
			}
			subCategoriaAnio.add(dataAnual.get(i));
			grupoSubCategoriasPorAnio.put(dataAnual.get(i).anio, subCategoriaAnio);
		}
		
		ArrayList <String> listAnios = new ArrayList<String>(grupoSubCategoriasPorAnio.keySet());

		for(int i=0;i<listAnios.size();i++){
				acumuladoPorCategoria(grupoSubCategoriasPorAnio.get(listAnios.get(i)));
		}

		return dataAnual;
	}
	

	
	private void acumuladoPorCategoria(ArrayList<ReporteAcumuladosMensualAnualData> facts){
		
		BigDecimal monto=BigDecimal.ZERO;
		
		if(facts == null || facts.size()==0)return;
		
		Collections.sort(facts,
				new Comparator<ReporteAcumuladosMensualAnualData>() {
					@Override
					public int compare(
							ReporteAcumuladosMensualAnualData p1,
							ReporteAcumuladosMensualAnualData p2) {

						int resultado = new Integer(p1.order1)
								.compareTo(new Integer(p2.order1));
						if (resultado != 0) {
							return resultado;
						}
						
						resultado = new String(p1.categoria).compareTo(new String(
								p2.categoria));
						if (resultado != 0) {
							return resultado;
						}

						resultado = new Integer(p1.order2).compareTo(new Integer(
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
	
	
	private void montoCategoriaSubcategoriaAnual(ArrayList<ReporteAcumuladosMensualAnualData> facts, 
			ArrayList<ReporteAcumuladosMensualAnualData> listPrincipal,String tipoPeriodo){
		
		BigDecimal monto=BigDecimal.ZERO;
		
		if(facts == null || facts.size()==0)return ;
		
		for(int i=0;i<facts.size();i++){
			monto = monto.add(new BigDecimal(facts.get(i).montoCuentaMes));
		}
		
		ReporteAcumuladosMensualAnualData o = new ReporteAcumuladosMensualAnualData();
		
		if( tipoPeriodo.equals("mensual")){
			o.order1=facts.get(0).order1;
			o.order2=facts.get(0).order2;
			o.categoria=facts.get(0).categoria;
			o.subCategoria=facts.get(0).subCategoria;
			o.mes="TOTAL";
			o.ordenMes="13";
			o.montoCuentaMes=monto.toString(); 
			
		}else 
		{
			o.order1=facts.get(0).order1;
			o.order2=facts.get(0).order2;
			o.categoria=facts.get(0).categoria;
			o.subCategoria=facts.get(0).subCategoria;
			o.mes=facts.get(0).mes;
			o.ordenMes=facts.get(0).ordenMes;
			o.anio="3000";
			o.montoCuentaMes=monto.toString(); 
		}
		
		listPrincipal.add(o);
		return ;
	}
	
	
	
	private ArrayList<ReporteAcumuladosMensualAnualData> getConsolidadoAnioMesCategoriaSubcategoria(ReporteAcumuladosMensualAnualData[] categorias,
			HashMap<String, ArrayList<ReporteAcumuladosMensualAnualData>> grupoFacts,
			ArrayList<String> meses,ArrayList<String> anios){
		
		HashMap<String, ReporteAcumuladosMensualAnualData> grupoSubcategoriaCuenta= new HashMap<String, ReporteAcumuladosMensualAnualData>(); 
		String 	subcategoriaCuenta="";
		BigDecimal montoSubcategoriaCuenta;

		for(int i=0;i<anios.size();i++){
			for(int j=0;j<meses.size();j++){
				for(int k=0;k<categorias.length;k++){
					
					ReporteAcumuladosMensualAnualData now = categorias[k];

					subcategoriaCuenta=now.account.replace("-", "").replace("%", "");
					montoSubcategoriaCuenta = BigDecimal.ZERO;
					String key = anios.get(i)+meses.get(j)+"//"+now.order1+"//"+
							now.order2+"//"+subcategoriaCuenta;
					
					montoSubcategoriaCuenta=getMontoxCuenta2(grupoFacts.get(key), now.acctvalue, now.consider,
							now.considerwhen, now.greaterThanAYear);

					montoSubcategoriaCuenta=montoSubcategoriaCuenta.multiply(new BigDecimal(categorias[k].considerar));
					
					ReporteAcumuladosMensualAnualData o = new ReporteAcumuladosMensualAnualData();
					o.order1=now.order1;
					o.order2=now.order2;
					o.categoria=now.categoria;
					o.subCategoria=now.subCategoria;
					o.mes=meses.get(j);
					o.ordenMes=(j+1)+"";
					o.considerar=now.considerar;
					o.signo=now.signo;
					o.anio=anios.get(i);
					
					//CONSOLIDANDO POR AÑO -> MES -> CATEGORIA -> SUBCATEGORIA 
					key=anios.get(i)+meses.get(j)+o.order1+"//"+o.order2;// se utiliza los orders por existir subcategorias con la misma descripcion
					
					if(grupoSubcategoriaCuenta.containsKey(key)){ 
						BigDecimal montoAnterior = new BigDecimal(grupoSubcategoriaCuenta.get(key).montoCuentaMes);
						montoSubcategoriaCuenta=montoSubcategoriaCuenta.add(montoAnterior);
					}
					o.montoCuentaMes=montoSubcategoriaCuenta.toString(); 
					grupoSubcategoriaCuenta.put(key, o);
					
				}
			}
		}
		
		return new ArrayList<ReporteAcumuladosMensualAnualData>(grupoSubcategoriaCuenta.values());
	}

	private BigDecimal getMontoxCuenta(ArrayList<ReporteAcumuladosMensualAnualData> facts,Integer condicionMonto,String mayorAnio){
				
		BigDecimal suma= BigDecimal.ZERO;
		BigDecimal monto= BigDecimal.ZERO;

		HashMap<String,BigDecimal> montosConsolidadoPorCuenta=new HashMap<String, BigDecimal>();
		
		if(facts==null) return suma;
		
		if(mayorAnio==null ){
			
			//CONSOLIDANDO POR CUENTA XXXXXXX
			for(int i=0;i<facts.size();i++){
				monto=new BigDecimal(facts.get(i).montoCuentaMes);
				if(montosConsolidadoPorCuenta.containsKey(facts.get(i).acctvalue)){
					monto=monto.add(montosConsolidadoPorCuenta.get(facts.get(i).acctvalue));
				}
				montosConsolidadoPorCuenta.put(facts.get(i).acctvalue, monto);
			}
			
			ArrayList<String> cuentas = new ArrayList<String>(montosConsolidadoPorCuenta.keySet());
			
			for(int i=0;i<cuentas.size();i++){
				monto = montosConsolidadoPorCuenta.get(cuentas.get(i));
				if(condicionMonto==null||monto.compareTo(BigDecimal.ZERO)==condicionMonto)
					suma=suma.add(monto);
			}
			
		}else {
			
			for(int i=0;i<facts.size();i++){
				if(facts.get(i).greaterThanAYear.equals(mayorAnio))
					suma=suma.add(new BigDecimal(facts.get(i).montoCuentaMes));
			}
		}
		
		return suma;
	}
	
	
	private static BigDecimal getMontoxCuenta2(ArrayList<ReporteAcumuladosMensualAnualData> dataCuentas, String cuenta,
			String considerarSigno, String signo, String mayorAnio) {

		if (dataCuentas == null)
			return BigDecimal.ZERO;

		Integer condicionMonto = 0;
		HashMap<String, BigDecimal> grupoMontoCuenta = new HashMap<String, BigDecimal>();
		ArrayList<BigDecimal> montosCuentas = new ArrayList<BigDecimal>();

		cuenta = cuenta.replace("-", "").replace("%", "");

		if (considerarSigno.equals("Y")) {
			if (signo.equals("SCO_PLUS"))
				condicionMonto = 1;
			else if (signo.equals("SCO_MINUS"))
				condicionMonto = -1;
		} else
			condicionMonto = 0;

		if (mayorAnio.equalsIgnoreCase("SCO_Y"))
			mayorAnio = "SI";
		else if (mayorAnio.equalsIgnoreCase("SCO_N"))
			mayorAnio = "NO";
		else {
			mayorAnio = "";
		}

		// CONSOLIDANDO
		BigDecimal monto = BigDecimal.ZERO;
		for (int i = 0; i < dataCuentas.size(); i++) {
			if (dataCuentas.get(i).greaterThanAYear.contains(mayorAnio)) {
				monto = new BigDecimal(dataCuentas.get(i).montoCuentaMes);
				if (grupoMontoCuenta.containsKey(dataCuentas.get(i).acctvalue)) {
					monto = monto.add(grupoMontoCuenta.get(dataCuentas.get(i).acctvalue));
				}
				grupoMontoCuenta.put(dataCuentas.get(i).acctvalue, monto);
			}
		}

		montosCuentas = new ArrayList<BigDecimal>(grupoMontoCuenta.values());

		BigDecimal suma = BigDecimal.ZERO;

		for (int i = 0; i < montosCuentas.size(); i++) {

			monto = montosCuentas.get(i);

			if (condicionMonto != 0 && monto.compareTo(BigDecimal.ZERO) != condicionMonto)
				monto = BigDecimal.ZERO;

			suma = suma.add(monto);
		}

		return suma;
	}

	
	
	
	private void  dataFinalEstadoResultado( ArrayList<ReporteAcumuladosMensualAnualData> listData){
		
		for(int i=0;i<listData.size();i++){
			BigDecimal montoFinal = new BigDecimal(listData.get(i).montoCuentaMes);
			montoFinal=montoFinal.multiply(new BigDecimal(listData.get(i).signo).multiply(new BigDecimal("-1")));
			listData.get(i).montoCuentaMes=montoFinal.toString();			
		}
	}
	
	private void  dataFinalSituacionFinanciera( ArrayList<ReporteAcumuladosMensualAnualData> listData){
		
		for(int i=0;i<listData.size();i++){
			BigDecimal montoFinal = new BigDecimal(listData.get(i).montoCuentaMes);
			montoFinal=montoFinal.multiply(new BigDecimal(listData.get(i).signo));
			listData.get(i).montoCuentaMes=montoFinal.toString();			
		}
	}
	
	private void dataFinalGGPPNaturaleza( ArrayList<ReporteAcumuladosMensualAnualData> listData){
		
		for(int i=0;i<listData.size();i++){
			BigDecimal montoFinal = new BigDecimal(listData.get(i).montoCuentaMes);
			montoFinal=montoFinal.abs().multiply(new BigDecimal(listData.get(i).signo));
			listData.get(i).montoCuentaMes=montoFinal.toString();			
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
		return "Servlet ReporteAcumuladosMensualAnual. This Servlet was made by Pablo Sarobe modified by everybody";
	} // end of getServletInfo() method
}
