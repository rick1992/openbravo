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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.transaction.InOutLineAccountingDimension;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.core.data.SCRComboItem;
import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;
import pe.com.unifiedgo.warehouse.data.SwaRequerimientoReposicion;
import pe.com.unifiedgo.warehouse.data.SwaRequerimientoReposicionDetail;


public class GenerateReportRepositionTrace extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static String strBDErrorMessage = "";
  private static String LocalOrg = null;
  private static String RemoteOrg = null;
  private static String BPartnerID = null;
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    OBError myMessage = null;
    myMessage = new OBError();
    myMessage.setTitle("");
    if (vars.commandIn("DEFAULT")) {
      String strOrg = vars.getGlobalVariable("inpOrg", "GeneralLedgerJournal|Org", "0");
      
      String strBpartnerId = vars.getStringParameter("inpBPartnerId");
      String strBPartnerId = "";
      
      
      String strWarehouse = vars.getRequestGlobalVariable("inpmWarehouseId",
              "GenerateReportRepositionTrace|M_Warehouse_ID");
      
      /*String strDateFrom = vars.getGlobalVariable("inpDateFrom",
				"GenerateReportRepositionTrace|DateFrom", "");
      
	  String strDateTo = vars.getGlobalVariable("inpDateTo",
				"GenerateReportRepositionTrace|DateTo", "");*/
	  
	  String strChkOnlyDif = vars.getGlobalVariable("inpShowDiff",
              "GenerateReportRepositionTrace|ShowDiff","");
	  
	  String strChkOnlyMin = vars.getGlobalVariable("inpShowMin",
              "GenerateReportRepositionTrace|ShowMin","");
	  
	  String strPeriodoId = "" ;
	  //vars.getRequestGlobalVariable("inpPeriodo",
        //      "GenerateReportRepositionTrace|ShowAll");
	  
	  
	  String strDateFrom = vars.getGlobalVariable("inpDateFrom", "GenerateReportRepositionTrace|DateFrom",
	          SREDateTimeData.FirstDayOfMonth(this));

	      String strDateTo = vars.getGlobalVariable("inpDateTo", "GenerateReportRepositionTrace|DateTo",
	          SREDateTimeData.today(this));
	  
	  
	  
	 // String strChkOnlyMin  ="N";
      
      printPageDataSheet(request, response, vars,strDateFrom,strDateTo,  strWarehouse,strBPartnerId,strChkOnlyDif, strChkOnlyMin,  strPeriodoId, true);

    } else if(vars.commandIn("LISTAR")){
        String strOrg = vars.getStringParameter("inpadOrgId");
        String strBPartnerId = vars.getStringParameter("inpBPartnerId");
        
        String strDateFrom = vars.getGlobalVariable("inpDateFrom",
				"GenerateReportRepositionTrace|DateFrom", "");
	    String strDateTo = vars.getGlobalVariable("inpDateTo",
				"GenerateReportRepositionTrace|DateTo", "");
	    
	    String strChkOnlyDif = vars.getRequestGlobalVariable("inpShowDiff",
	              "GenerateReportRepositionTrace|ShowDiff");
	    
	    String strChkOnlyMin = vars.getRequestGlobalVariable("inpShowMin",
	              "GenerateReportRepositionTrace|ShowMin");
	    
	    /*System.out.println("---------------");
	    System.out.println("strChkOnlyDif: " + strChkOnlyDif);
	    System.out.println("strChkOnlyMin: " + strChkOnlyMin);*/
        
        vars.setSessionValue("strOrgId", strOrg.trim());
        
        String strPeriodoId = "" ; //vars.getRequestGlobalVariable("inpPeriodo",
                //"GenerateReportRepositionTrace|ShowAll");
        
        LocalOrg = strOrg;
        BPartnerID = strBPartnerId;

        String strWarehouse = vars.getRequestGlobalVariable("inpmWarehouseId",
                "GenerateReportRepositionTrace|M_Warehouse_ID");
        
        
        printPageDataSheet(request, response, vars,strDateFrom, strDateTo ,  strWarehouse,strBPartnerId,strChkOnlyDif, strChkOnlyMin, strPeriodoId, false);
     } 
     else
      pageError(response);
  }
  


  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String strWarehouse, String strBPartnerId, String strChkOnlyDif, String strChkOnlyMin, String strPeriodoId,boolean isFirstLoad)      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
 
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    GenerateReportRepositionTraceData[] getData = null;
    GenerateReportRepositionData[] getDataPrd = null;
    
    String strConvRateErrorMsg = "";
    String discard[] = { "discard" };
    
  //  String ad_org_id = vars.getSessionValue("strOrgId") ;
    
    String strOrgId = vars.getSessionValue("strOrgId");
    
    String strSendToView= "Y";
    String strOnlyMina = "ReposicionPorMinaOut";
    if(strChkOnlyMin==null || strChkOnlyMin.equals("")){
    	strOnlyMina=null;
    	strSendToView= "N";
    }
    
    
    
    
    String ad_to_org_id = RemoteOrg;

    xmlDocument = xmlEngine.readXmlTemplate(
        "pe/com/unifiedgo/warehouse/ad_reports/GenerateReportRepositionTrace", discard)
        .createXmlDocument();
    
    if(vars.commandIn("LISTAR")){
    	OBError myMessage = null;
        myMessage = new OBError();
        try {
            System.out.println("ENTRA ???");
            String warehouse_id = strWarehouse;
            getData = GenerateReportRepositionTraceData.selectShipmentWithoutReceipt(this, strOrgId,ad_to_org_id,
                    vars.getSessionValue("#AD_CLIENT_ID"),warehouse_id);
            
            System.out.println("ENTRA SIGUE");
            
           // System.out.println(strChkOnlyDif);
            
            if(strChkOnlyDif.equals("on"))
            	getDataPrd = GenerateReportRepositionData.selectOnlyDif(this,strOnlyMina ,vars.getSessionValue("#AD_CLIENT_ID"), strOrgId,strDateFrom,strDateTo);
            else
            	getDataPrd = GenerateReportRepositionData.select(this ,strOnlyMina, vars.getSessionValue("#AD_CLIENT_ID"), strOrgId,strDateFrom,strDateTo);
            	
            
         } catch (ServletException ex) {
            myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
        }
        
        strConvRateErrorMsg = myMessage.getMessage();
        if (!strConvRateErrorMsg.equals("") && strConvRateErrorMsg != null) {
          advise(request, response, "ERROR",
              Utility.messageBD(this, "NoConversionRateHeader", vars.getLanguage()),
              strConvRateErrorMsg);
          
        } else { // Otherwise, the report is launched
          if ((getData == null || getData.length == 0)) {
            discard[0] = "selEliminar";
            getData = GenerateReportRepositionTraceData.set();
          } else {
            xmlDocument.setData("structure5", getData);
          }
          
          if ((getDataPrd == null || getDataPrd.length == 0)) {
              discard[0] = "selEliminar";
              getData = GenerateReportRepositionTraceData.set();
          } else {
              xmlDocument.setData("structure6", getDataPrd);
            }
        }
    }
    

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "GenerateReportRepositionTrace",
          false, "", "", "", false, "ad_reports", strReplaceWith, false, true);
      toolbar.setEmail(false);
      toolbar.prepareSimpleToolBarTemplate();
      //toolbar.prepareRelationBarTemplate(false, false, "printXLS();return false;");
      xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "pe.com.unifiedgo.warehouse.ad_reports.GenerateReportRepositionTrace");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
            "GenerateReportRepositionTrace.html", classInfo.id, classInfo.type, strReplaceWith,
            tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
            "GenerateReportRepositionTrace.html", strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      {
        OBError myMessage = vars.getMessage("GenerateReportRepositionTrace");
        vars.removeMessage("GenerateReportRepositionTrace");
        if (myMessage != null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }
      
      
   
      
      try {
          ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
              "0C754881EAD94243A161111916E9B9C6", Utility.getContext(this, vars, "#AccessibleOrgTree", "GenerateReportRepositionTrace"),
              Utility.getContext(this, vars, "#User_Client", "GenerateReportRepositionTrace"), 0);
         // comboTableData.fillParameters(null, "GenerateReportRepositionTrace", "");
          Utility.fillSQLParameters(this, vars, null, comboTableData, "GenerateReportRepositionTrace",
        		  strOrgId);
          xmlDocument.setData("reportAD_Org_ID", "liststructure", comboTableData.select(false));
        } catch (Exception ex) {
          throw new ServletException(ex);
        }

      xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      
      xmlDocument.setParameter("docDatedisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("docDatesaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      
      xmlDocument.setParameter("dateFrom", strDateFrom);
	  xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
	  xmlDocument.setParameter("dateFromsaveFormat",vars.getSessionValue("#AD_SqlDateFormat"));
	  xmlDocument.setParameter("dateTo", strDateTo);
	  xmlDocument.setParameter("dateTodisplayFormat",vars.getSessionValue("#AD_SqlDateFormat"));
	  xmlDocument.setParameter("dateTosaveFormat",vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("adOrgId", strOrgId);
   	  xmlDocument.setParameter("allProductosConStock", (strChkOnlyMin==null || strChkOnlyMin.equals(""))?"":"Y");
      xmlDocument.setParameter("allProductosDiff", (strChkOnlyDif==null || strChkOnlyDif.equals(""))?"":"Y");
   	  xmlDocument.setParameter("periodoId", strPeriodoId);
   	  
   	 // System.out.println("---QUE ENVIO ? : " + strSendToView);
   	  xmlDocument.setParameter("paramCboItemMina", strSendToView);
      
      out.println(xmlDocument.print());
      out.close();
    }
  }

  public String getServletInfo() {
    return "Servlet PurchaseOrderFilter. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method

}
