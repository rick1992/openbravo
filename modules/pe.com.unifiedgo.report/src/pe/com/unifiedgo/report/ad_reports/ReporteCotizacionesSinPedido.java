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
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.businessUtility.Tree;
import org.openbravo.erpCommon.businessUtility.TreeData;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SQLReturnObject;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class ReporteCotizacionesSinPedido extends HttpSecureAppServlet {
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
					"ReporteCotizacionesSinPedido|docDate",
					SREDateTimeData.today(this));
			String strProyDate = vars.getGlobalVariable("inpProyDate",
					"ReporteCotizacionesSinPedido|proyDate",
					SREDateTimeData.tomorrow(this));
			String strAD_Org_ID = vars.getGlobalVariable("inpadOrgId",
					"ReporteCotizacionesSinPedido|AD_Org_ID", "");
			String mProductId = vars.getGlobalVariable("inpmProductId",
					"ReporteCotizacionesSinPedido|M_Product_Id", "");
			String strVendedorId = vars.getStringParameter("inpVendedor");
			String strcBpartnetId = vars.getGlobalVariable("inpcBPartnerId",
					"ReporteCotizacionesSinPedido|CB_PARTNER_ID", "");

			printPageDataSheet(request, response, vars, strAD_Org_ID,
					strDocDate, strProyDate, strcBpartnetId, strVendedorId,
					true);
		} else if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {

			String strDocDate = vars.getStringParameter("inpDocDate");
			String strProyDate = vars.getStringParameter("inpProyDate");
			String strAD_Org_ID = vars.getStringParameter("inpadOrgId");
			String mProductId = vars.getRequestGlobalVariable("inpmProductId",
					"ReporteCotizacionesSinPedido|M_Product_Id");
			String strVendedorId = vars.getStringParameter("inpVendedor");

			String strcBpartnetId = vars.getStringParameter("inpcBPartnerId");

			printPageDataSheet(request, response, vars, strAD_Org_ID,
					strDocDate, strProyDate, strcBpartnetId, strVendedorId,
					false);

		} else if (vars.commandIn("PDF", "XLS")) {

			String strDocDate = vars.getStringParameter("inpDocDate");
			String strProyDate = vars.getStringParameter("inpProyDate");
			String strAD_Org_ID = vars.getStringParameter("inpadOrgId");
			String mProductId = vars.getRequestGlobalVariable("inpmProductId",
					"ReporteCotizacionesSinPedido|M_Product_Id");
			String strmProductLineID = vars
					.getStringParameter("inpProductLine");
			String strcBpartnetId = vars.getStringParameter("inpcBPartnerId");
			String strVendedorId = vars.getStringParameter("inpVendedor");

			printPagePDF(response, vars, strAD_Org_ID, strDocDate, strProyDate,
					mProductId, strmProductLineID, strcBpartnetId,
					strVendedorId);
		}

		else
			pageError(response);
	}

	private void printPageDataSheet(HttpServletRequest request,
			HttpServletResponse response, VariablesSecureApp vars,
			String strAD_Org_ID, String strDocDate, String strProyDate,
			String strIdProveedor, String strVendedorId, boolean isFirstLoad)
			throws IOException, ServletException {

		if (log4j.isDebugEnabled())
			log4j.debug("Output: dataSheet");

		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();
		XmlDocument xmlDocument = null;
		ReporteCotizacionesSinPedidoData[] data = null;
		String strConvRateErrorMsg = "";

		String discard[] = { "discard" };

		xmlDocument = xmlEngine
				.readXmlTemplate(
						"pe/com/unifiedgo/report/ad_reports/ReporteCotizacionesSinPedido",
						discard).createXmlDocument();

		String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
		String cadenaOrganizacion = getFamily(strTreeOrg, strAD_Org_ID);

		String cadenaCliente = Utility.getContext(this, vars, "#User_Client",
				"ReporteCotizacionesSinPedido");

		if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
			OBError myMessage = null;
			myMessage = new OBError();
			try {

				data = ReporteCotizacionesSinPedidoData
						.select_cotizaciones_sinpedidos(this, cadenaCliente,
								cadenaOrganizacion, strIdProveedor,
								strVendedorId, strDocDate, strProyDate);

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
					data = ReporteCotizacionesSinPedidoData.set("0");
				} else {
					xmlDocument.setData("structure1", data);
				}
			}
		}

		else {
			if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
				discard[0] = "selEliminar";
				data = ReporteCotizacionesSinPedidoData.set("0");
			}
		}

		if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
			ToolBar toolbar = new ToolBar(this, vars.getLanguage(),
					"ReporteCotizacionesSinPedido", false, "", "",
					"imprimir();return false;", false, "ad_reports",
					strReplaceWith, false, true);
			toolbar.setEmail(false);

			toolbar.prepareSimpleToolBarTemplate();

			toolbar.prepareRelationBarTemplate(false, false,
					"imprimirXLS();return false;");

			xmlDocument.setParameter("toolbar", toolbar.toString());

			try {
				WindowTabs tabs = new WindowTabs(this, vars,
						"pe.com.unifiedgo.core.ad_reports.ReporteCotizacionesSinPedido");
				xmlDocument.setParameter("parentTabContainer",
						tabs.parentTabs());
				xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
				xmlDocument.setParameter("childTabContainer", tabs.childTabs());
				xmlDocument.setParameter("theme", vars.getTheme());
				NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
						"ReporteCotizacionesSinPedido.html", classInfo.id,
						classInfo.type, strReplaceWith, tabs.breadcrumb());
				xmlDocument.setParameter("navigationBar", nav.toString());
				LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
						"ReporteCotizacionesSinPedido.html", strReplaceWith);
				xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
			} catch (Exception ex) {
				throw new ServletException(ex);
			}
			{
				OBError myMessage = vars
						.getMessage("ReporteCotizacionesSinPedido");
				vars.removeMessage("ReporteCotizacionesSinPedido");
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

			try {
				ComboTableData comboTableData = new ComboTableData(vars, this,
						"TABLEDIR", "AD_Org_ID", "",
						"D4DF252DEC3B44858454EE5292A8B836", Utility.getContext(
								this, vars, "#AccessibleOrgTree",
								"ReporteCotizacionesSinPedido"),
						Utility.getContext(this, vars, "#User_Client",
								"ReporteCotizacionesSinPedido"), 0);
				Utility.fillSQLParameters(this, vars, null, comboTableData,
						"ReporteCotizacionesSinPedido", strAD_Org_ID);

				xmlDocument.setData("reportAD_Org_ID", "liststructure",
						comboTableData.select(false));
				comboTableData = null;
			} catch (Exception ex) {
				throw new ServletException(ex);
			}
			xmlDocument.setParameter("adOrg", strAD_Org_ID);
//
//			xmlDocument.setParameter("cBpartner", strIdProveedor);
//			String nombreProveedor = ReporteCotizacionesSinPedidoData
//					.selectCBpartner(this, strIdProveedor);
//			xmlDocument.setParameter("bpartnerDescription", nombreProveedor
//					.compareToIgnoreCase("0") == 0 ? "" : nombreProveedor);

			// Print document in the output

			// para los vendedores

			ReporteCotizacionesSinPedidoData[] datax = ReporteCotizacionesSinPedidoData
					.select_vendedores(this, Utility.getContext(this, vars,
							"#User_Client", "ReporteCotizacionesSinPedido"));
			FieldProvider vendedores[] = new FieldProvider[datax.length];
			Vector<Object> vector = new Vector<Object>(0);

			for (int i = 0; i < datax.length; i++) {
				SQLReturnObject sqlReturnObject = new SQLReturnObject();
				sqlReturnObject.setData("ID", datax[i].vendedorId);
				sqlReturnObject.setData("NAME", datax[i].vendedorNombre);
				vector.add(sqlReturnObject);
			}
			vector.copyInto(vendedores);

			xmlDocument.setData("reportVendedor_ID", "liststructure",
					vendedores);

			xmlDocument.setParameter("paramVendedorArray", Utility
					.arrayInfinitasEntradas(
							"orgid;orgidpadre;vendedorId;vendedorNombre",
							"arrVendedor", datax));

			xmlDocument.setParameter("vendedorId", strVendedorId);

			// /////////////fin para los vendedores

			xmlDocument.setParameter("paramBPartnerId", strIdProveedor);
			xmlDocument.setParameter("paramBPartnerDescription",
					ReporteCotizacionesSinPedidoData.selectCBpartner(this,
							strIdProveedor));

			out.println(xmlDocument.print());
			out.close();
		}
	}

	private void printPagePDF(HttpServletResponse response,
			VariablesSecureApp vars, String strOrg, String strDateFrom,
			String strDateTo, String mProductId, String strmProductLineID,
			String strIdProveedor, String strVendedorId) throws IOException,
			ServletException {

		ReporteCotizacionesSinPedidoData[] data = null;

		String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
		String cadenaOrganizacion = getFamily(strTreeOrg, strOrg);

		String cadenaCliente = Utility.getContext(this, vars, "#User_Client",
				"ReporteCotizacionesSinPedido");

		String language = vars.getLanguage();
		data = ReporteCotizacionesSinPedidoData.select_cotizaciones_sinpedidos(
				this, cadenaCliente, cadenaOrganizacion, strIdProveedor,
				strVendedorId, strDateFrom, strDateTo);

		String cliente = "Todos";
		String vendedor = "Todos";
		BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class,
				strIdProveedor);
		if (bp != null) {
			cliente = bp.getName();
		}

		User us = OBDal.getInstance().get(User.class, strVendedorId);
		if (us != null && strVendedorId.compareToIgnoreCase("") != 0) {
			vendedor = us.getName();
		}

		String strReportName = "";
		String strTipoReporte = "";

		if (vars.commandIn("PDF")) {
			strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteCotizacionesSinPedido.jrxml";
			strTipoReporte = "pdf";

		} else {
			strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteCotizacionesSinPedidoExcel.jrxml";
			strTipoReporte = "xls";
		}

		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("ruc",
				ReporteCotizacionesSinPedidoData.selectRucOrg(this, strOrg));
		parameters
				.put("organizacion", ReporteCotizacionesSinPedidoData
						.selectSocialName(this, strOrg));
		parameters.put("cliente", cliente);
		parameters.put("vendedor", vendedor);

		parameters.put("dateFrom", strDateFrom);
		parameters.put("dateTo", strDateTo);

		renderJR(vars, response, strReportName, "Cotizaciones_Sin_Pedido",
				strTipoReporte, parameters, data, null);

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
