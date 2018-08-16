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
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.businessUtility.TreeData;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SQLReturnObject;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.plm.Product;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class ReporteStockConsolidado extends HttpSecureAppServlet {
	  private static final long serialVersionUID = 1L;
	  private static String strBDErrorMessage = "";

	  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
	      ServletException {
	    VariablesSecureApp vars = new VariablesSecureApp(request);

	    // HttpSession session = request.getSession(true);
	    // @SuppressWarnings("rawtypes")
	    // Enumeration e = session.getAttributeNames();
	    // while (e.hasMoreElements()) {
	    // String name = (String) e.nextElement();
	    // System.out.println("name: " + name + " - value: " + session.getAttribute(name));
	    // }

	    if (vars.commandIn("DEFAULT")) {

	      String strDocDate = vars.getGlobalVariable("inpDocDate", "GenerateAnaliticKardex|docDate",
	          SREDateTimeData.FirstDayOfMonth(this));

	      String strDateto = vars.getGlobalVariable("inpProyDate", "GenerateAnaliticKardex|inpDateTo",
	          SREDateTimeData.today(this));

	      String strAD_Org_ID = vars.getGlobalVariable("inpadOrgId",
	          "ReporteStockConsolidado|AD_Org_ID", "");

	      String value = vars.getStringParameter("inpSearchKey");

	      OBCriteria<Product> product = OBDal.getInstance().createCriteria(Product.class);
	      product.add(Restrictions.eq(Product.PROPERTY_SEARCHKEY, value));

	      List<Product> ProductList = product.list();

	      String mProductId = "";
	      if (ProductList == null || ProductList.size() == 0) {
	        mProductId = "";
	      } else {
	        mProductId = ProductList.get(0).getId();
	      }

	      printPageDataSheet(request, response, vars, strAD_Org_ID, mProductId, true, strDocDate,
	          strDateto);
	    } else if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {

	      String strAD_Org_ID = vars.getStringParameter("inpadOrgId");
	      String value = vars.getStringParameter("inpSearchKey");
	      OBCriteria<Product> product = OBDal.getInstance().createCriteria(Product.class);
	      product.add(Restrictions.eq(Product.PROPERTY_SEARCHKEY, value));

	      List<Product> ProductList = product.list();

	      String mProductId = "";
	      if (ProductList == null || ProductList.size() == 0) {
	        mProductId = "";
	      } else {
	        mProductId = ProductList.get(0).getId();
	      }

	      printPageDataSheet(request, response, vars, strAD_Org_ID, mProductId, false, "", "");
	    } else if (vars.commandIn("PDF", "XLS")) {
	      if (log4j.isDebugEnabled())
	        log4j.debug("PDF");

	      String strDocDate = vars.getStringParameter("inpDocDate");
	      String strDateto = vars.getStringParameter("inpProyDate");
	      String strAD_Org_ID = vars.getStringParameter("inpadOrgId");
	      String value = vars.getStringParameter("inpSearchKey");
	      String strConFecha = vars.getStringParameter("inpConRangoFechas");

	      String strmwarehouseid = vars.getStringParameter("inpmWarehouseId");

	      setHistoryCommand(request, "DEFAULT");

	      OBCriteria<Product> product = OBDal.getInstance().createCriteria(Product.class);
	      product.add(Restrictions.eq(Product.PROPERTY_SEARCHKEY, value));

	      List<Product> ProductList = product.list();

	      String mProductId = "";
	      if (ProductList == null || ProductList.size() == 0) {
	        mProductId = "";
	      } else {
	        mProductId = ProductList.get(0).getId();
	      }

	      if (vars.commandIn("XLS")) {
	        printPageXLS(request, response, vars, value, strAD_Org_ID, strmwarehouseid, "", strDocDate,
	            strDateto, strConFecha);
	      }

	    } else {
	      pageError(response);
	    }
	  }

	  private void printPageXLS(HttpServletRequest request, HttpServletResponse response,
	      VariablesSecureApp vars, String mProductId, String strAD_Org_ID, String mWarehouseId,
	      String strConStockValorizado, String strDateFrom, String strDateTo, String ConFecha)
	      throws IOException, ServletException {

	    String strOutput;
	    String strReportName;

	    ReporteStockConsolidadoData[] data = null;

	    // if(!ConFecha.equalsIgnoreCase("Y")){
	    strDateFrom = "";
	    // strDateTo="";
	    // }

	    if (mProductId != null && !mProductId.equals(""))
	      mProductId += "%";

	    try {
	      data = ReporteStockConsolidadoData.selectAlterno(strAD_Org_ID,
	          vars.getSessionValue("#AD_CLIENT_ID"), mProductId, mWarehouseId, strDateFrom, strDateTo);
	    } catch (OBException e) {
	      advisePopUp(request, response, "WARNING",
	          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
	          Utility.messageBD(this, "FINPR_NoConversionFound", vars.getLanguage()) + e.getMessage());
	    }

	    strOutput = "xls";
	    strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteStockConsolidadoAlternoExcel.jrxml";

	    HashMap<String, Object> parameters = new HashMap<String, Object>();

	    parameters.put("ORGANIZACION",
	        ReporteStockConsolidadoData.selectOrganization(this, strAD_Org_ID));
	    parameters.put("RUC_ORGANIZACION",
	        ReporteStockConsolidadoData.selectRucOrganization(this, strAD_Org_ID));
	    parameters.put("TIPO_ALMACEN", ReporteStockConsolidadoData.getAlmacen(this, mWarehouseId));
	    parameters.put("VALORIZADO", strConStockValorizado);

	    renderJR(vars, response, strReportName, "Reporte_Stock_Consolidado", strOutput, parameters,
	        data, null);
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

	  private boolean isNumeric(String str) {
	    try {
	      double d = Double.parseDouble(str);
	    } catch (NumberFormatException nfe) {
	      return false;
	    }
	    return true;
	  }

	  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
	      VariablesSecureApp vars, String strAD_Org_ID, String mProductId, boolean isFirstLoad,
	      String strDocDate, String strProyDate) throws IOException, ServletException {

	    if (log4j.isDebugEnabled())
	      log4j.debug("Output: dataSheet");

	    response.setContentType("text/html; charset=UTF-8");
	    PrintWriter out = response.getWriter();
	    XmlDocument xmlDocument = null;
	    ReporteStockConsolidadoData[] data = null;
	    String strConvRateErrorMsg = "";

	    String discard[] = { "discard" };

	    xmlDocument = xmlEngine.readXmlTemplate(
	        "pe/com/unifiedgo/report/ad_reports/ReporteStockConsolidado", discard).createXmlDocument();

	    if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
	      OBError myMessage = null;
	      myMessage = new OBError();
	      try {
	        data = ReporteStockConsolidadoData.select(strAD_Org_ID,
	            vars.getSessionValue("#AD_CLIENT_ID"), mProductId, "");

	      } catch (ServletException ex) {
	        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
	      }
	      strConvRateErrorMsg = myMessage.getMessage();
	      if (!strConvRateErrorMsg.equals("") && strConvRateErrorMsg != null) {
	        advise(request, response, "ERROR",
	            Utility.messageBD(this, "NoConversionRateHeader", vars.getLanguage()),
	            strConvRateErrorMsg);
	      } else { // Otherwise, the report is launched
	        if (data == null || data.length == 0) {
	          discard[0] = "selEliminar";
	          data = ReporteStockConsolidadoData.set();
	        } else {
	          xmlDocument.setData("structure1", data);
	        }
	      }
	    }

	    else {
	      if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
	        discard[0] = "selEliminar";
	        data = ReporteStockConsolidadoData.set();
	      }
	    }

	    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
	      /*
	       * ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReporteStockConsolidado", false,
	       * "", "", "imprimirPDF();return false;", false, "ad_reports", strReplaceWith, false, true);
	       * toolbar.prepareSimpleToolBarTemplate(); toolbar.prepareRelationBarTemplate(false, false,
	       * "imprimirXLS();return false;"); xmlDocument.setParameter("toolbar", toolbar.toString());
	       */

	      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReporteStockConsolidado", false, "",
	          "", "", false, "ad_reports", strReplaceWith, false, true);
	      toolbar.setEmail(false);
	      toolbar.prepareSimpleToolBarTemplate();
	      toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");
	      xmlDocument.setParameter("toolbar", toolbar.toString());

	      //
	      // toolbar.prepareRelationBarTemplate(false, false, "printXLS();return false;");
	      //
	      // xmlDocument.setParameter("toolbar", toolbar.toString());

	      try {
	        WindowTabs tabs = new WindowTabs(this, vars,
	            "pe.com.unifiedgo.report.ad_reports.ReporteStockConsolidado");
	        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
	        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
	        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
	        xmlDocument.setParameter("theme", vars.getTheme());
	        NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
	            "ReporteStockConsolidado.html", classInfo.id, classInfo.type, strReplaceWith,
	            tabs.breadcrumb());
	        xmlDocument.setParameter("navigationBar", nav.toString());
	        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
	            "ReporteStockConsolidado.html", strReplaceWith);
	        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
	      } catch (Exception ex) {
	        throw new ServletException(ex);
	      }
	      {
	        OBError myMessage = vars.getMessage("ReporteStockConsolidado");
	        vars.removeMessage("ReporteStockConsolidado");
	        if (myMessage != null) {
	          xmlDocument.setParameter("messageType", myMessage.getType());
	          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
	          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
	        }
	      }

	      xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
	      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
	      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
	      xmlDocument.setParameter("docDate", strDocDate);
	      xmlDocument.setParameter("docDatedisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
	      xmlDocument.setParameter("docDatesaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
	      xmlDocument.setParameter("proyDate", strProyDate);
	      xmlDocument.setParameter("proyDatedisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
	      xmlDocument.setParameter("proyDatesaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

	      try {
	        // cargar tipo de almacen al html :3
	        // vars.getSessionValue("#AD_CLIENT_ID") : para recuperar el id del cliente
	        ReporteStockConsolidadoData[] dataTipoAlmacenes = ReporteStockConsolidadoData
	            .select_tipos_almacenes(this, vars.getSessionValue("#AD_CLIENT_ID"));

	        FieldProvider TiposAlmacenes[] = new FieldProvider[dataTipoAlmacenes.length + 1];
	        Vector<Object> vector = new Vector<Object>(0);

	        SQLReturnObject sqlReturnObject = new SQLReturnObject();
	        sqlReturnObject.setData("ID", "");
	        sqlReturnObject.setData("NAME", "-- TODOS LOS ALMACENES");
	        vector.add(sqlReturnObject);

	        for (int i = 0; i < dataTipoAlmacenes.length; i++) {
	          sqlReturnObject = new SQLReturnObject();
	          sqlReturnObject.setData("ID", dataTipoAlmacenes[i].codigo);
	          sqlReturnObject.setData("NAME", dataTipoAlmacenes[i].detalle);
	          vector.add(sqlReturnObject);
	        }
	        vector.copyInto(TiposAlmacenes);

	        xmlDocument.setData("reportWAREHOUSE_TYPE_ID", "liststructure", TiposAlmacenes);

	      } catch (Exception ex) {
	        throw new ServletException(ex);
	      }

	      if (isFirstLoad) {
	        xmlDocument.setParameter("FirstLoad", "FirstLoad = \"YES\"; ");
	      } else {
	        xmlDocument.setParameter("FirstLoad", "FirstLoad = \"NO\"; ");
	      }

	      xmlDocument.setParameter("adOrg", strAD_Org_ID);

	      // Print document in the output
	      out.println(xmlDocument.print());
	      out.close();
	    }
	  }

	  public String getServletInfo() {
	    return "Servlet PurchaseOrderFilter. This Servlet was made by Jon Alegría";
	  } // end of getServletInfo() method

	}
