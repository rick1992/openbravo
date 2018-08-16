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
package pe.com.unifiedgo.core.ad_forms;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class FormReservationAndStockDetailsJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static String strBDErrorMessage = "";

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strAD_Org_ID = vars.getStringParameter("inpadOrgId");
      String strM_Product_ID = vars.getStringParameter("inpmProductId");
      String strM_Warehouse_ID = vars.getStringParameter("inpmWarehouseId");
      printPageDataSheet(request, response, vars, strAD_Org_ID, strM_Product_ID, strM_Warehouse_ID,
          true);
    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strAD_Org_ID, String strM_Product_ID,
      String strM_Warehouse_ID, boolean isFirstLoad) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    FormReservationAndStockDetailsJRData[] data = null;
    String strConvRateErrorMsg = "";
    String discard[] = { "discard" };

    xmlDocument = xmlEngine.readXmlTemplate(
        "pe/com/unifiedgo/core/ad_forms/FormReservationAndStockDetailsFilterJR", discard)
        .createXmlDocument();

    OBError myMessage = null;
    myMessage = new OBError();
    try {
      data = FormReservationAndStockDetailsJRData.select(vars, strAD_Org_ID,
          vars.getSessionValue("#AD_CLIENT_ID"), strM_Product_ID, strM_Warehouse_ID);
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
        data = FormReservationAndStockDetailsJRData.set();
      } else {
    	  
    	  for(int j=0; j<data.length;j++){
    		  System.out.println("RECORDID " + data[j].recordid);
    		  System.out.println("TABID " + data[j].adtabid);
    	  }
    	  
    	
        xmlDocument.setData("structure1", data);
      }
    }

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
      ToolBar toolbar = new ToolBar(this, vars.getLanguage(),
          "FormReservationAndStockDetailsFilterJR", false, "", "", "", false, "ad_forms",
          strReplaceWith, false, true);
      toolbar.prepareSimpleToolBarTemplate();
      xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "pe.com.unifiedgo.core.ad_forms.FormReservationAndStockDetailsJR");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
            "FormReservationAndStockDetailsFilterJR.html", classInfo.id, classInfo.type,
            strReplaceWith, tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
            "FormReservationAndStockDetailsFilterJR.html", strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      {
        myMessage = vars.getMessage("FormReservationAndStockDetailsJR");
        vars.removeMessage("FormReservationAndStockDetailsJR");
        if (myMessage != null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }

      xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");

      xmlDocument.setParameter("adOrg", strAD_Org_ID);
      xmlDocument.setParameter("mProductId", strM_Product_ID);

      // Print document in the output
      out.println(xmlDocument.print());
      out.close();
    }
  }

  public String getServletInfo() {
    return "Servlet PurchaseOrderFilter. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method

}
