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
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.components.list.ListContents;

import org.eclipse.jdt.internal.compiler.IDocumentElementRequestor;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
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
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.xmlEngine.XmlDocument;

public class ReporteVersusVentas extends
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
					"ReporteVersusVentas|DateFrom", "");
			String strDateTo = vars.getGlobalVariable("inpDateTo",
					"ReporteVersusVentas|DateTo", "");
			String strOrg = vars.getGlobalVariable("inpOrg",
					"ReporteVersusVentas|Org", "0");
			String strRecord = vars.getGlobalVariable("inpRecord",
					"ReporteVersusVentas|Record", "");
			String strTable = vars.getGlobalVariable("inpTable",
					"ReporteVersusVentas|Table", "");
			String mProductId = vars.getGlobalVariable("inpmProductId", 
					"ReporteVersusVentas|M_Product_Id", "");
			String strBPartnerId = vars.getGlobalVariable("inpBPartnerId", 
					"ReporteVersusVentas|BPARTNER_ID", "");

			System.out.println("mProductId:" + mProductId + " - strBPartnerId:" + strBPartnerId);
			printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg,
					strTable, strRecord, mProductId, strBPartnerId);

		} else if (vars.commandIn("DIRECT")) {
			String strTable = vars.getGlobalVariable("inpTable",
					"ReporteVersusVentas|Table");
			String strRecord = vars.getGlobalVariable("inpRecord",
					"ReporteVersusVentas|Record");

			setHistoryCommand(request, "DIRECT");
			vars.setSessionValue(
					"ReporteVersusVentas.initRecordNumber",
					"0");
			
			printPageDataSheet(response, vars, "", "", "", strTable, strRecord,"","");

		} else if (vars.commandIn("FIND")) {
			String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
					"ReporteVersusVentas|DateFrom");
			String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
					"ReporteVersusVentas|DateTo");
			String strOrg = vars.getGlobalVariable("inpOrg",
					"ReporteVersusVentas|Org", "0");
			vars.setSessionValue(
					"ReporteVersusVentas.initRecordNumber",
					"0");
			String mProductId = vars.getRequestGlobalVariable("inpmProductId", 
					"ReporteVersusVentas|M_Product_Id");
			String strBPartnerId = vars.getRequestGlobalVariable("inpBPartnerId", 
					"ReporteVersusVentas|BPARTNER_ID");
			setHistoryCommand(request, "DEFAULT");
			System.out.println("mProductId:" + mProductId + " - strBPartnerId:" + strBPartnerId );
			System.out.println("mProductId:" + mProductId + " - strBPartnerId:" + strBPartnerId);

			printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg,
					"", "", mProductId, strBPartnerId);

		} else if (vars.commandIn("PDF", "XLS")) {
			if (log4j.isDebugEnabled())
				log4j.debug("PDF");
			String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
					"ReporteVersusVentas|DateFrom");
			String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
					"ReporteVersusVentas|DateTo");
			String strOrg = vars.getGlobalVariable("inpOrg",
					"ReporteVersusVentas|Org", "0");
			String mProductId = vars.getRequestGlobalVariable("inpmProductId", 
					"ReporteVersusVentas|M_Product_Id");
			String strBPartnerId = vars.getRequestGlobalVariable("inpBPartnerId", 
					"ReporteVersusVentas|BPARTNER_ID");

			String strTable = vars.getStringParameter("inpTable");
			String strRecord = vars.getStringParameter("inpRecord");
			setHistoryCommand(request, "DEFAULT");
			System.out.println("mProductId:" + mProductId + " - strBPartnerId:" + strBPartnerId);

			printPagePDF(request,response, vars, strDateFrom, strDateTo, strOrg,
					strTable, strRecord, mProductId, strBPartnerId);

		} else
			pageError(response);
	}

	private void printPageDataSheet(HttpServletResponse response,
			VariablesSecureApp vars, String strDateFrom, String strDateTo,
			String strOrg, String strTable, String strRecord, String mProductId, String strBPartnerId)
			throws IOException, ServletException {
		if (log4j.isDebugEnabled())
			log4j.debug("Output: dataSheet");
		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();
		XmlDocument xmlDocument = null;
		ReporteVersusVentasData[] data = null;
		String strPosition = "0";
		ToolBar toolbar = new ToolBar(this, vars.getLanguage(),
				"ReporteVersusVentas", false, "", "",
				"", false, "ad_reports",
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
//					"submitCommandForm('XLS', false, null, 'ReporteVersusVentas.xls', 'EXCEL');return false;");
			xmlDocument = xmlEngine
					.readXmlTemplate(
							"pe/com/unifiedgo/report/ad_reports/ReporteVersusVentas",
							discard).createXmlDocument();
			data = ReporteVersusVentasData.set("0");
			data[0].rownum = "0";
		}
		xmlDocument.setParameter("toolbar", toolbar.toString());
		try {
			WindowTabs tabs = new WindowTabs(this, vars,
					"pe.com.unifiedgo.report.ad_reports.ReporteVersusVentas");
			xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
			xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
			xmlDocument.setParameter("childTabContainer", tabs.childTabs());
			xmlDocument.setParameter("theme", vars.getTheme());
			NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
					"ReporteVersusVentas.html",
					classInfo.id, classInfo.type, strReplaceWith,
					tabs.breadcrumb());
			xmlDocument.setParameter("navigationBar", nav.toString());
			LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
					"ReporteVersusVentas.html",
					strReplaceWith);
			xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
		} catch (Exception ex) {
			throw new ServletException(ex);
		}
		{
			OBError myMessage = vars
					.getMessage("ReporteVersusVentas");
			vars.removeMessage("ReporteVersusVentas");
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
							"ReporteVersusVentas"),
					Utility.getContext(this, vars, "#User_Client",
							"ReporteVersusVentas"), '*');
			comboTableData.fillParameters(null,
					"ReporteVersusVentas", "");
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
		xmlDocument.setParameter("paramPeriodosArray", Utility.arrayInfinitasEntradas("idperiodo;periodo;fechainicial;fechafinal;idorganizacion","arrPeriodos",
	  			ReporteVersusVentasData
				.select_periodos(this)));
		vars.setSessionValue(
				"ReporteVersusVentas|Record", strRecord);
		vars.setSessionValue(
				"ReporteVersusVentas|Table", strTable);

		xmlDocument.setParameter("mProduct", mProductId);
		xmlDocument.setParameter("productDescription", ReporteVersusVentasData.selectMproduct(this, mProductId));

		xmlDocument.setParameter("BPartnerId", strBPartnerId);
		xmlDocument.setParameter("BPartnerDescription", ReporteVersusVentasData.selectBpartner(this, strBPartnerId));

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
			String strOrg, String strTable, String strRecord, String mProductId, String strBPartnerId )
			throws IOException, ServletException {


		List<VersusLineV> listData = new ArrayList<VersusLineV>();
		
		ReporteVersusVentasData [] data = null ;

		String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
		String strOrgFamily = getFamily(strTreeOrg, strOrg);
		
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

        Date dateIni=null;
        Date dateFin=null;
        
        try {

            dateIni = formatter.parse(strDateFrom);
            dateFin = formatter.parse(strDateTo);

        } catch (ParseException e) {
            e.printStackTrace();
        }
           	    

		listData =VersusVentas 
				.getVersusVentas(this, dateIni, dateFin, strOrg, strBPartnerId, mProductId);
		
			      /////////////////////////////
//		listData = new ArrayList<VersusLineV>();
//		
//		 VersusLineV e= new VersusLineV();
//		 e.mProductId="343434343";
//		 e.cInvoiceId="jajaj3";
//		 e.mInout="ma90";
//		 e.productValue=String.valueOf (234.234);
//		 e.invoiceValue="fv-w34234";
//		 e.mInoutValue="gr-234234";
//		 e.fechaInvoice="20-12-2014";
//		 e.fechaInout="20-12-2017";
//		 e.bPartnerId="23123-23423";
//		 e.bPartnerValue="coam - proveedor";
//		 e.orderId="23423423klk";
//		 e.orderValue="orden-234324.";
//		   	  
//		 e.descProducto="producto primario";
//		 e.uom="piezas";
//		 e.precioUnitario=(3.224);
//		 e.precioTotal= (4.53);
//		 e.tc=(4.244);
//
//
//				 
//				 
//			listData.add(e);
//			listData.add(e);	listData.add(e);	listData.add(e);	listData.add(e);
//		
	      /////////////////////////////

	      if ( listData==null || listData.size()==0) {
	          advisePopUp(request, response, "WARNING", Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()), Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
	          return;
	        }
	      	      
	      data= new ReporteVersusVentasData [listData.size()];
	      
	      for(int i=0;i<listData.size();i++){
	    	  VersusLineV o=listData.get(i);
	    	  ReporteVersusVentasData a = new ReporteVersusVentasData();

	    	  
	    	  a.unidadesInout=String.valueOf (o.unidadesInout);
	    	  a.unidadesInvoice=String.valueOf (o.unidadesInvoice);
	    	  a.unidadesDeffered=String.valueOf (o.unidadesDeffered);

	    	  a.mProductId=o.mProductId;
	    	  a.cInvoiceId=o.cInvoiceId;
	    	  a.mInout=o.mInout;
	    	  a.productValue=String.valueOf (o.productValue);
	    	  a.invoiceValue=o.invoiceValue;
	    	  a.mInoutValue=o.mInoutValue;
	    	  a.fechaInvoice=o.fechaInvoice;
	    	  a.fechaInout=o.fechaInout;
	    	  a.bPartnerId=o.bPartnerId;
	    	  a.bPartnerValue=o.bPartnerValue;
	    	  a.orderId=o.orderId;
	    	  a.orderValue=o.orderValue;
	    	  
	    	 a.descProducto=o.descProducto;
	    	 a.uom=o.uom;
	    	a.precioUnitario=String.valueOf (o.precioUnitario);
	    	a.precioTotal=String.valueOf (o.precioTotal);
	    	a.tc=String.valueOf (o.tc);
	    	a.moneda=o.moneda;


	    	  data[i]=a;
	      }

		String strSubtitle = (Utility.messageBD(this, "LegalEntity",
				vars.getLanguage()) + ": ")
				+ ReporteVersusVentasData.selectCompany(
						this, vars.getClient())
				+ "\n"
				+ "RUC:"
				+ ReporteVersusVentasData.selectRucOrg(
						this, strOrg) + "\n";

		if (!("0".equals(strOrg)))
			strSubtitle += (Utility.messageBD(this, "OBUIAPP_Organization",
					vars.getLanguage()) + ": ")
					+ ReporteVersusVentasData.selectOrg(
							this, strOrg) + "\n";

		// if (!"".equals(strDateFrom) || !"".equals(strDateTo))
		// strSubtitle += (Utility.messageBD(this, "From", vars.getLanguage()) +
		// ": ") + strDateFrom
		// + "  " + (Utility.messageBD(this, "OBUIAPP_To", vars.getLanguage()) +
		// ": ") + strDateTo
		// + "\n";
		System.out.println("Llega hasta aqui");
		String strOutput;
		String strReportName;
		if (vars.commandIn("PDF")) {
			strOutput = "pdf";
			strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteVersusVentas.jrxml";
		} else {
			strOutput = "xls";
			strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteVersusVentas.jrxml";
		}

		HashMap<String, Object> parameters = new HashMap<String, Object>();
		// parameters.put("Subtitle", strSubtitle);
		parameters.put("Ruc", ReporteVersusVentasData
				.selectRucOrg(this, strOrg));
		parameters.put("organizacion",
				ReporteVersusVentasData
						.selectSocialName(this, strOrg));

		// parameters.put("dateFrom", StringToDate(strDateFrom));
		// parameters.put("dateTo", StringToDate(strDateTo));
		parameters.put("dateFrom", StringToDate(strDateFrom));
		parameters.put("dateTo", StringToDate(strDateTo));
		renderJR(vars, response, strReportName,
				"Reporte de Versus de Ventas", strOutput,
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

	@Override
	public String getServletInfo() {
		return "Servlet ReporteVersusVentas. This Servlet was made by Pablo Sarobe modified by everybody";
	} // end of getServletInfo() method
}
