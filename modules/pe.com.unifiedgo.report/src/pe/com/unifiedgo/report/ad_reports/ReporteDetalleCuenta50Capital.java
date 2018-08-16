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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.service.OBDal;
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
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.accounting.StructPle;
import pe.com.unifiedgo.accounting.SunatUtil;
import pe.com.unifiedgo.sales.SSA_Utils;

public class ReporteDetalleCuenta50Capital extends
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
					"ReporteDetalleCuenta50Capital|DateFrom", "");
			String strDateTo = vars.getGlobalVariable("inpDateTo",
					"ReporteDetalleCuenta50Capital|DateTo", "");
			String strOrg = vars.getGlobalVariable("inpOrg",
					"ReporteDetalleCuenta50Capital|Org", "0");
			String strRecord = vars.getGlobalVariable("inpRecord",
					"ReporteDetalleCuenta50Capital|Record", "");
			String strTable = vars.getGlobalVariable("inpTable",
					"ReporteDetalleCuenta50Capital|Table", "");
			printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg,
					strTable, strRecord);

		} else if (vars.commandIn("DIRECT")) {
			String strTable = vars.getGlobalVariable("inpTable",
					"ReporteDetalleCuenta50Capital|Table");
			String strRecord = vars.getGlobalVariable("inpRecord",
					"ReporteDetalleCuenta50Capital|Record");

			setHistoryCommand(request, "DIRECT");
			vars.setSessionValue(
					"ReporteDetalleCuenta50Capital.initRecordNumber",
					"0");
			printPageDataSheet(response, vars, "", "", "", strTable, strRecord);

		} else if (vars.commandIn("FIND")) {
			String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
					"ReporteDetalleCuenta50Capital|DateFrom");
			String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
					"ReporteDetalleCuenta50Capital|DateTo");
			String strOrg = vars.getGlobalVariable("inpOrg",
					"ReporteDetalleCuenta50Capital|Org", "0");
			vars.setSessionValue(
					"ReporteDetalleCuenta50Capital.initRecordNumber",
					"0");
			setHistoryCommand(request, "DEFAULT");
			printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg,
					"", "");

		} else if (vars.commandIn("PDF", "XLS")) {
			if (log4j.isDebugEnabled())
				log4j.debug("PDF");
//			String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
//					"ReporteDetalleCuenta50Capital|DateFrom");
//			String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
//					"ReporteDetalleCuenta50Capital|DateTo");
			String strOrg = vars.getGlobalVariable("inpOrg",
					"ReporteDetalleCuenta50Capital|Org", "0");
			
			String anio1 = vars.getStringParameter("inpAnio1");

			setHistoryCommand(request, "DEFAULT");
			printPagePDF(request,response, vars,  strOrg,anio1);

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
		ReporteDetalleCuenta50CapitalData[] data = null;
		String strPosition = "0";
		ToolBar toolbar = new ToolBar(this, vars.getLanguage(),
				"ReporteDetalleCuenta50Capital", false, "", "",
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
//					"submitCommandForm('XLS', false, null, 'ReporteDetalleCuenta50Capital.xls', 'EXCEL');return false;");
			xmlDocument = xmlEngine
					.readXmlTemplate(
							"pe/com/unifiedgo/report/ad_reports/ReporteDetalleCuenta50Capital",
							discard).createXmlDocument();
			data = ReporteDetalleCuenta50CapitalData.set("0");
			data[0].rownum = "0";
		}
		xmlDocument.setParameter("toolbar", toolbar.toString());
		try {
			WindowTabs tabs = new WindowTabs(this, vars,
					"pe.com.unifiedgo.report.ad_reports.ReporteDetalleCuenta50Capital");
			xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
			xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
			xmlDocument.setParameter("childTabContainer", tabs.childTabs());
			xmlDocument.setParameter("theme", vars.getTheme());
			NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
					"ReporteDetalleCuenta50Capital.html",
					classInfo.id, classInfo.type, strReplaceWith,
					tabs.breadcrumb());
			xmlDocument.setParameter("navigationBar", nav.toString());
			LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
					"ReporteDetalleCuenta50Capital.html",
					strReplaceWith);
			xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
		} catch (Exception ex) {
			throw new ServletException(ex);
		}
		{
			OBError myMessage = vars
					.getMessage("ReporteDetalleCuenta50Capital");
			vars.removeMessage("ReporteDetalleCuenta50Capital");
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
							"ReporteDetalleCuenta50Capital"),
					Utility.getContext(this, vars, "#User_Client",
							"ReporteDetalleCuenta50Capital"), '*');
			comboTableData.fillParameters(null,
					"ReporteDetalleCuenta50Capital", "");
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
	  			ReporteDetalleCuenta50CapitalData
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
				"ReporteDetalleCuenta50Capital|Record", strRecord);
		vars.setSessionValue(
				"ReporteDetalleCuenta50Capital|Table", strTable);

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
			VariablesSecureApp vars,String strOrg, String anio)
			throws IOException, ServletException {

		ReporteDetalleCuenta50CapitalData[] cabecera = null;
		ReporteDetalleCuenta50CapitalData[] detalle = null;

		
		String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
		String strOrgFamily = getFamily(strTreeOrg, strOrg);
		String strCliente = Utility.getContext(this, vars,"#User_Client","ReporteAnualCentroCostos");
		String periodo = "DIECIMBRE - "+anio;
				
		cabecera = ReporteDetalleCuenta50CapitalData
				.select_cabecera(this,strOrgFamily+",'0'",strCliente,anio );

		detalle = ReporteDetalleCuenta50CapitalData
				.select_detalle(this,strOrgFamily+",'0'",strCliente,anio);
		
		if(cabecera.length>0){
			for(int i=0;i<detalle.length;i++){
				
				ReporteDetalleCuenta50CapitalData doc = detalle[i];
				
				String bPartnerID = doc.terceroId;
				String bpDocumentType = ""; // Tipo de documento
				String bpName = ""; // Razon Social

				if (bPartnerID != null && !bPartnerID.equals("")) {
					BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class, bPartnerID);

					if (bp != null) {
						bpDocumentType = bp.getScrComboItem().getCode();
						bpName = bp.getName();
					}
				}else  {
					bpDocumentType = "0";
					bpName = "varios";
				}
				
				detalle[i].tipo=bpDocumentType;
				detalle[i].tercero=bpName;

				
				detalle[i].capitalSocial=cabecera[0].capitalSocial;
				detalle[i].valorNominal=cabecera[0].valorNominal;
				detalle[i].accionesSuscritas=cabecera[0].accionesSuscritas;
				detalle[i].accionesPagadas=cabecera[0].accionesPagadas;
				detalle[i].numeroAcciones= detalle.length+"";
				detalle[i].cPeriodId=cabecera[0].cPeriodId;
			}
		}
		
		if(detalle.length==0)
			detalle=cabecera;
				
       if (detalle.length==0) {
          advisePopUp(request, response, "WARNING", Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()), 
        		  Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
          return;
        }
       
		String strOutput;
		String strReportName;
		if (vars.commandIn("PDF")) {
			strOutput = "pdf";
			strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteDetalleCuenta50Capital.jrxml";
		} else {
			strOutput = "xls";
			strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteDetalleCuenta50CapitalExcel.jrxml";
		}

		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("Ruc", ReporteDetalleCuenta50CapitalData
				.selectRUC(this, strOrg));
		parameters.put("Razon",
				ReporteDetalleCuenta50CapitalData
						.selectSocialName(this, strOrg));

//		parameters.put("dateFrom", StringToDate(strDateFrom));
//		parameters.put("dateTo", StringToDate(strDateTo));
		parameters.put("ANIOINI", anio);
		parameters.put("PERIODO", periodo);


		renderJR(vars, response, strReportName,
				"Reporte_Detalle_Cuenta_50_Capital", strOutput,
				parameters, detalle, null);
	}
	
	
	public static StructPle getPLE_detalle50_1(ConnectionProvider connp,
				      VariablesSecureApp vars, String strClientId, Date dateTo, Organization org,
				      String strOrgFamily) throws Exception {

	    StringBuffer sb = new StringBuffer();
	    StructPle sunatPle = new StructPle();
	    sunatPle.numEntries = 0;
	    
	    SimpleDateFormat dt = new SimpleDateFormat("yyyyMMdd");
	    SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
	    // get this year uit
	    Calendar caldttTo = new GregorianCalendar();
	    caldttTo.setTime(dateTo);
	    int year = caldttTo.get(Calendar.YEAR);

	    String period = dt.format(dateTo); // Periodo

	    String operationStatus = "1";

	    String strClient = "'" + strClientId + "'";
	    
	    ReporteDetalleCuenta50CapitalData[] data = null;
		
		data = ReporteDetalleCuenta50CapitalData
				.select_cabecera(connp,strOrgFamily+",'0'",strClient, String.valueOf(year));


		for (int i = 0; i < data.length; i++) {

			ReporteDetalleCuenta50CapitalData info = data[i];

	      String strCapitalSocial = info.capitalSocial;
	      String strValorNominal = info.valorNominal;
	      String strNumAccionesSus = info.accionesSuscritas;
	      String strNumAccionesPag = info.accionesPagadas;
	      

	      String linea = period + "|" + strCapitalSocial + "|" + strValorNominal + "|" + 
	    		         strNumAccionesSus + "|" + strNumAccionesPag +"|" + operationStatus + "|" + "|";

		  if (!sb.toString().equals(""))
			     sb.append("\n");
		  sb.append(linea);
		  sunatPle.numEntries++;

		}

		sunatPle.data = sb.toString();
		String rucAdq = SunatUtil.LPad(org.getOrganizationInformationList().get(0).getTaxID(), 11, '0');

		String filename = "LE" + rucAdq + dt.format(dateTo) + "031601011111.TXT"; // LERRRRRRRRRRRAAAAMMDD030700CCOIM1.TXT

		sunatPle.filename = filename;

		return sunatPle;
	 }
	
	
	public static StructPle getPLE_detalle50_2(ConnectionProvider connp,
		      VariablesSecureApp vars, String strClientId, Date dateTo, Organization org,
		      String strOrgFamily) throws Exception {

	StringBuffer sb = new StringBuffer();
	StructPle sunatPle = new StructPle();
	sunatPle.numEntries = 0;
	
	SimpleDateFormat dt = new SimpleDateFormat("yyyyMMdd");
	SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
	// get this year uit
	Calendar caldttTo = new GregorianCalendar();
	caldttTo.setTime(dateTo);
	int year = caldttTo.get(Calendar.YEAR);
	
	String period = dt.format(dateTo); // Periodo
    String operationStatus = "1";
    String strClient = "'" + strClientId + "'";
    ReporteDetalleCuenta50CapitalData[] data = null;


    
	data = ReporteDetalleCuenta50CapitalData
			.select_detalle(connp,strOrgFamily+",'0'",strClient ,String.valueOf(year));
	
	
	for (int i = 0; i < data.length; i++) {
	
		ReporteDetalleCuenta50CapitalData info = data[i];
		String bPartnerID = info.cbpartnerid;
		String strRuc = info.ruc;
		String strSunatCode = info.sunatcode;
		String strPartnerName = info.tercero;
		String strTotalAcciones = info.totalAcciones;
		String strPorcentaje = info.porcentajeAcciones;
		String strBPartnerType = "";
		
	     
		if (bPartnerID != null && !bPartnerID.equals("")) {
	        BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class, bPartnerID);
	        if (bp != null) 
	        	strBPartnerType = bp.getScrComboItem().getCode();
	      }
		
	
		String linea = period + "|" + strBPartnerType + "|" + strRuc + "|" + strSunatCode + "|" + 
				      strPartnerName + "|" + strTotalAcciones +"|"+SSA_Utils.formatNumber(strPorcentaje) +"|" + operationStatus + "|" + "|";
	
		if (!sb.toString().equals(""))
			     sb.append("\n");
		sb.append(linea);
		sunatPle.numEntries++;
	
	}
	
	sunatPle.data = sb.toString();
	String rucAdq = SunatUtil.LPad(org.getOrganizationInformationList().get(0).getTaxID(), 11, '0');
	
	String filename = "LE" + rucAdq + dt.format(dateTo) + "031602011111.TXT"; // LERRRRRRRRRRRAAAAMMDD030700CCOIM1.TXT
	
	sunatPle.filename = filename;
	
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
		return "Servlet ReporteDetalleCuenta50Capital. This Servlet was made by Pablo Sarobe modified by everybody";
	} // end of getServletInfo() method
}
