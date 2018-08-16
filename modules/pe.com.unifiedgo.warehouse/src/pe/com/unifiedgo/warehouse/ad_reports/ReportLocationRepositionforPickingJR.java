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
package pe.com.unifiedgo.warehouse.ad_reports;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.plm.Product;
import org.openbravo.xmlEngine.XmlDocument;

public class ReportLocationRepositionforPickingJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

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
      String strAD_Org_ID = vars.getGlobalVariable("inpadOrgId",
          "ReportLocationRepositionforPickingJR|AD_Org_ID", "");
      String strmWarehouseId = vars.getGlobalVariable("inpmWarehouseId",
          "ReportLocationRepositionforPickingJR|M_Warehouse_ID", "");

      /*String mProductId = vars.getGlobalVariable("inpmProductId",
          "ReportLocationRepositionforPickingJR|M_Product_Id", "");
      */
      String mProductId = vars.getStringParameter("inpmProductId");

      String strIncludeVendor = vars.getGlobalVariable("inpShowNullVendor",
          "ReportLocationRepositionforPickingJR|ShowNullVendor", "Y");

      String strCheckedbyrbtn = "allbin";
      String strLocatorType = vars.getStringParameter("inpLocatorType");
      String strCodValue = vars.getStringParameter("inpSearchKey").trim().toLowerCase();
      

      printPageDataSheet(request, response, vars, strAD_Org_ID, strmWarehouseId, mProductId,
          strIncludeVendor, strCheckedbyrbtn, true,strLocatorType, strCodValue);

    } else if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
      String strAD_Org_ID = vars.getStringParameter("inpadOrgId");
      String strmWarehouseId = vars.getRequestGlobalVariable("inpmWarehouseId",
          "ReportLocationRepositionforPickingJR|M_Warehouse_ID");

      /*
      String mProductId = vars.getGlobalVariable("inpmProductId",
          "ReportLocationRepositionforPickingJR|M_Product_Id", "");
      */
      String mProductId = vars.getStringParameter("inpmProductId");
      
      String strIncludeVendor = vars.getStringParameter("inpShowNullVendor");
      String strCheckedbyrbtn = vars.getStringParameter("inprbtnSearch");
      String strLocatorType = vars.getStringParameter("inpLocatorType");
      String strCodValue = vars.getStringParameter("inpSearchKey").toLowerCase();

      printPageDataSheet(request, response, vars, strAD_Org_ID, strmWarehouseId, mProductId,
          strIncludeVendor, strCheckedbyrbtn, false, strLocatorType, strCodValue);

    }else if (vars.commandIn("PDF", "XLS")) {

    	   String strAD_Org_ID = vars.getStringParameter("inpadOrgId");
    	      String strmWarehouseId = vars.getRequestGlobalVariable("inpmWarehouseId",
    	          "ReportLocationRepositionforPickingJR|M_Warehouse_ID");

    	      /*
    	      String mProductId = vars.getGlobalVariable("inpmProductId",
    	          "ReportLocationRepositionforPickingJR|M_Product_Id", "");
    	      */
    	      String mProductId = vars.getStringParameter("inpmProductId");
    	      
    	      String strIncludeVendor = vars.getStringParameter("inpShowNullVendor");
    	      String strCheckedbyrbtn = vars.getStringParameter("inprbtnSearch");
    	      String strLocatorType = vars.getStringParameter("inpLocatorType");
    	      String strCodValue = vars.getStringParameter("inpSearchKey").toLowerCase();
    	      
    	      
        if (vars.commandIn("XLS")) {
          printPageXLS(request, response, vars, strAD_Org_ID, strmWarehouseId, mProductId,
                  strIncludeVendor, strCheckedbyrbtn, false, strLocatorType, strCodValue);
        }
      } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strAD_Org_ID, String strmWarehouseId, String mProductId,
      String strChkLocatorOut, String strCheckedbyrbtn, boolean isFirstLoad,String strLocatorType, String strLocatorValue) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    
   // System.out.println("strLocatorType: " + strLocatorType);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;

    ReportLocationRepositionforPickingJRData[] data = null;
    ReportLocationRepositionforPickingJRData[] data_General = null;
    String strConvRateErrorMsg = "";

    String discard[] = { "discard" };

    xmlDocument = xmlEngine
        .readXmlTemplate(
            "pe/com/unifiedgo/warehouse/ad_reports/ReportLocationRepositionforPickingFilterJR",
            discard).createXmlDocument();

    if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
      OBError myMessage = null;
      myMessage = new OBError();
      try {
        data = ReportLocationRepositionforPickingJRData.select(strAD_Org_ID,
            vars.getSessionValue("#AD_CLIENT_ID"), strmWarehouseId);
        data_General = ReportLocationRepositionforPickingJRData.selectLocator(this,
              strmWarehouseId, mProductId, strChkLocatorOut,strCheckedbyrbtn,strLocatorType, strLocatorValue );
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
          data = ReportLocationRepositionforPickingJRData.set();
          // data = ReportLocationRepositionforPickingJRData.set();
        } else {
        	
          xmlDocument.setData("structure1", data);
        }
        
        if (data_General == null || data_General.length == 0) {
            discard[0] = "selEliminar";
            data_General = ReportLocationRepositionforPickingJRData.set();
            // data = ReportLocationRepositionforPickingJRData.set();
          } else {
        	  xmlDocument.setData("structure2", data_General);
        	  
          }
      }
    }

    else {
      if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
        discard[0] = "selEliminar";
        data = ReportLocationRepositionforPickingJRData.set();
      }
    }

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
      ToolBar toolbar = new ToolBar(this, vars.getLanguage(),
          "ReportLocationRepositionforPickingFilterJR", false, "", "", "", false, "ad_reports",
          strReplaceWith, false, true);
      toolbar.setEmail(false);
      toolbar.prepareSimpleToolBarTemplate();
       toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");
      xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "pe.com.unifiedgo.warehouse.ad_reports.ReportLocationRepositionforPickingJR");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
            "ReportLocationRepositionforPickingFilterJR.html", classInfo.id, classInfo.type,
            strReplaceWith, tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
            "ReportLocationRepositionforPickingFilterJR.html", strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      {
        OBError myMessage = vars.getMessage("ReportLocationRepositionforPickingJR");
        vars.removeMessage("ReportLocationRepositionforPickingJR");
        if (myMessage != null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }

      xmlDocument.setParameter("paramShowNullVendor", strChkLocatorOut);
      xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "",
            "0C754881EAD94243A161111916E9B9C6", Utility.getContext(this, vars,
                "#AccessibleOrgTree", "LocationRepositionforPickingJR"), Utility.getContext(this,
                vars, "#User_Client", "LocationRepositionforPickingJR"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData,
            "LocationRepositionforPickingFilterJR", strAD_Org_ID);
        xmlDocument.setData("reportAD_Org_ID", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      
      try {
          ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
              "359DDEE9B03540499E6E4759D026F646", "", Utility.getContext(this, vars,
                  "#AccessibleOrgTree", "LocationRepositionforPickingJR"), Utility.getContext(this, vars,
                  "#User_Client", "LocationRepositionforPickingJR"), 0);
          Utility.fillSQLParameters(this, vars, null, comboTableData, "LocationRepositionforPickingJR", "");
          xmlDocument.setData("reportLocatorType", "liststructure", comboTableData.select(false));
          comboTableData = null;
        } catch (Exception ex) {
          throw new ServletException(ex);
        }

      /*
       * try { ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
       * "M_Warehouse_ID", "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
       * "ReportLocationRepositionforPickingJR"), Utility.getContext(this, vars, "#User_Client",
       * "ReportLocationRepositionforPickingJR"), 0); Utility.fillSQLParameters(this, vars, null,
       * comboTableData, "ReportLocationRepositionforPickingJR", "");
       * xmlDocument.setData("reportM_WAREHOUSEID", "liststructure", comboTableData.select(false));
       * comboTableData = null; } catch (Exception ex) { throw new ServletException(ex); }
       * 
       * xmlDocument.setParameter("MWarehouseArray", Utility.arrayDobleEntrada("arrMWarehouse",
       * ReportLocationRepositionforPickingJRData.selectMWarehouseForParentOrgDouble(this, Utility
       * .getContext(this, vars, "#User_Client", "ReportLocationRepositionforPickingJR"))));
       */

      if (isFirstLoad) {
        xmlDocument.setParameter("FirstLoad", "FirstLoad = \"YES\"; ");
      } else {
        xmlDocument.setParameter("FirstLoad", "FirstLoad = \"NO\"; ");
      }
     // System.out.println("mProductId: " + mProductId);

      // xmlDocument.setParameter("paramShowNullVendor", strChkLocatorOut);
      xmlDocument.setParameter("pending", strCheckedbyrbtn);
      xmlDocument.setParameter("created", strCheckedbyrbtn);
      xmlDocument.setParameter("allbin", strCheckedbyrbtn);
      xmlDocument.setParameter("LocatorType", strLocatorType);
      
     // System.out.println("strLocatorValue: " + strLocatorValue);
      xmlDocument.setParameter("paramSearchKey", strLocatorValue);
      
      
      //System.out.println("strCheckedbyrbtn : " + strCheckedbyrbtn);
      
      
      String productValue = "Producto";
      if(strCheckedbyrbtn.equals("created")){
    	  Product product = OBDal.getInstance().get(Product.class, mProductId);
    	  if(product !=null){
    		  productValue = product.getSearchKey();
    	  }
      }

      
  //    System.out.println("productValue: " + productValue);
  //    System.out.println("mProductId: " + mProductId);
      
      xmlDocument.setParameter("mProductValue", productValue);
      xmlDocument.setParameter("mProduct", mProductId);
      
      xmlDocument.setParameter("productDescription",
        ReportLocationRepositionforPickingJRData.selectMproduct(this, mProductId));
      
      xmlDocument.setParameter("mWarehouseId", strmWarehouseId);
      xmlDocument.setParameter("mWarehouseDescription",
          ReportLocationRepositionforPickingJRData.selectMWarehouse(this, strmWarehouseId));

      // xmlDocument.setParameter("mWarehouseId", strmWarehouseId);
      xmlDocument.setParameter("adOrg", strAD_Org_ID);
      // Print document in the output

      out.println(xmlDocument.print());
      out.close();
    }
  }
  
  
  private void printPageXLS(HttpServletRequest request, HttpServletResponse response,
	      VariablesSecureApp vars, String strAD_Org_ID, String strmWarehouseId, String mProductId,
	      String strChkLocatorOut, String strCheckedbyrbtn, boolean isFirstLoad,String strLocatorType, String strLocatorValue) throws IOException, ServletException {
	  
	  
	    ReportLocationRepositionforPickingJRData[] data_General = null;
	      OBError myMessage = null;


	      try {

	        data_General = ReportLocationRepositionforPickingJRData.selectLocatorNew(this,
	              strmWarehouseId, mProductId, strChkLocatorOut,strCheckedbyrbtn,strLocatorType, strLocatorValue );
	      } catch (ServletException ex) {
	        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
	      }
	  

	  String strOutput = "xls";
	    String strReportName;

	    strReportName = "@basedesign@/pe/com/unifiedgo/warehouse/ad_reports/ReportLocationRepositionforPickingJR.jrxml";

	    HashMap<String, Object> parameters = new HashMap<String, Object>();
	    parameters.put("ORGANIZACION","");
	 
	     
	    renderJR(vars, response, strReportName, "Reporte Stock por Ubicaciones", strOutput, parameters, data_General,
	        null);

	  }

  public String getServletInfo() {
    return "Servlet PurchaseOrderFilter. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method

}
