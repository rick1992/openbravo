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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
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
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.transaction.InOutLineAccountingDimension;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.service.db.CallStoredProcedure;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.core.data.SCRComboCategory;
import pe.com.unifiedgo.core.data.SCRComboItem;
import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;
import pe.com.unifiedgo.warehouse.data.SwaRequerimientoReposicion;
import pe.com.unifiedgo.warehouse.data.SwaRequerimientoReposicionDetail;


public class ReviewLinkedPurchaseOrder extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static String strBDErrorMessage = "";
  private static String LocalOrg = null;
  private static String RemoteOrg = null;
  private static String OrderID = null;
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
      printPageDataSheet(request, response, vars,  null,strBPartnerId);

    } else if(vars.commandIn("LISTAR")){
    	String strBPartnerId = vars.getStringParameter("inpBPartnerId");
        String strOrg = vars.getStringParameter("inpadOrgId");
        LocalOrg = strOrg;
        BPartnerID = strBPartnerId;
        
        BusinessPartner BPartner = null;
        try {
        	BPartner = OBDal.getInstance().get(BusinessPartner.class, strBPartnerId);
        	RemoteOrg = BPartner.getSreOtherOrg().getId();
		} catch (Exception e) {
			myMessage.setTitle("");
            myMessage.setType("Error");
            myMessage.setTitle(Utility.messageBD(this, "SWA_noselected", vars.getLanguage()));
            vars.setMessage("GenerateInvoiceReposition", myMessage);
		}

        printPageDataSheet(request, response, vars, null,strBPartnerId);
     }else if(vars.commandIn("TOCOMPLETE")){
    	 String strOrg = LocalOrg;
         String strRequisitionLines = null;
         String strBPartnerId = BPartnerID;
         String[] selectedOrders = null;
         String strOrder = null;
         int temporal = 0;
         try {
         	strOrder = vars.getRequiredInStringParameter("inpPurchaseOrderList",
                 IsIDFilter.instance);
         	 strOrder = strOrder.replace("(", "");
             strOrder = strOrder.replace(")", "");
             strOrder = strOrder.replace("'", "");
             selectedOrders = strOrder.split(",");
           } catch (Exception e) {
             temporal = 1;
             myMessage.setTitle("");
             myMessage.setType("Error");
             myMessage.setTitle(Utility.messageBD(this, "SWA_noselected", vars.getLanguage()));
             vars.setMessage("GenerateInvoiceReposition", myMessage);
           }
         if (temporal != 1) {
        	   myMessage.setTitle("");
               myMessage.setType("Success");
        	   myMessage.setTitle(Utility.messageBD(this, "swa_purchaseorgupdated", vars.getLanguage()));
               String documentno = "";
        	   for (int i = 0; selectedOrders != null && i < selectedOrders.length; i++) {
        		   try {

        			   ReviewLinkedPurchaseOrderData[] stateOrder = null;
            		   stateOrder =  ReviewLinkedPurchaseOrderData.selectOrderReviewStatus(this, strOrg, RemoteOrg, 
                       vars.getSessionValue("#AD_CLIENT_ID"),selectedOrders[i].trim());
            		   if(!stateOrder[0].orderreviewstate.equals("PD")){
            			   myMessage.setTitle("");
                           myMessage.setType("Warning");
            			   documentno = documentno + " : " + stateOrder[0].orderreviewdocnum;
            			   myMessage.setTitle(Utility.messageBD(this, "swa_purchaseorgupdated_war", vars.getLanguage()) + documentno);
                           continue;         			   
            		   }
            		   int tmp = ReviewLinkedPurchaseOrderData.updateOrder(this,selectedOrders[i].trim() , vars.getSessionValue("#AD_CLIENT_ID"));
				   } catch (Exception e) {
					   myMessage.setTitle("");
			           myMessage.setType("Error");
			           myMessage.setTitle(Utility.messageBD(this, "SWA_errorupdateorderorg", vars.getLanguage()));
			           vars.setMessage("ReviewLinkedPurchaseOrder", myMessage);
				   }
        	   }
        	   //myMessage.setTitle(Utility.messageBD(this, "swa_purchaseorgupdated", vars.getLanguage())+documentno);
               vars.setMessage("ReviewLinkedPurchaseOrder", myMessage);
           }
         printPageDataSheet(request, response, vars,  strRequisitionLines,strBPartnerId);
     }else if(vars.commandIn("GENERATELINES")){
         String strOrg = LocalOrg;
         String strToOrg = RemoteOrg;
         String strBPartnerId = BPartnerID;
         OrderID = vars.getStringParameter("inpOrderToEdit");
         String strRequisitionLines = null;
         printPageDataSheet(request, response, vars,  strRequisitionLines,strBPartnerId);
     } else if(vars.commandIn("UPDATELINES")){
         String strOrg = LocalOrg;
         
         String strToOrg = RemoteOrg;
         
         String strOrderId = OrderID;
         String[] selectedOrderLines = null;
         String strOrderLines = null;
         
         String strBPartnerId = BPartnerID;
         
         int temporal=0;
         try {
        	 strOrderLines = vars.getRequiredInStringParameter("inpOrderLineList",
               IsIDFilter.instance);
        	 strOrderLines = strOrderLines.replace("(", "");
        	 strOrderLines = strOrderLines.replace(")", "");
        	 strOrderLines = strOrderLines.replace("'", "");
           selectedOrderLines = strOrderLines.split(",");
         } catch (Exception e) {
           temporal = 1;
           myMessage.setTitle("");
           myMessage.setType("Error");
           myMessage.setTitle(Utility.messageBD(this, "SWA_noselected", vars.getLanguage()));
           vars.setMessage("ReviewLinkedPurchaseOrder", myMessage);
         }
         
         if(temporal != 1){
        	 try {
            	 ReviewLinkedPurchaseOrderData[] stateOrder = null;
            	 stateOrder =  ReviewLinkedPurchaseOrderData.selectOrderReviewStatus(this, strOrg,RemoteOrg,
                               vars.getSessionValue("#AD_CLIENT_ID"),strOrderId);
            	 if(!stateOrder[0].orderreviewstate.equals("PD")){
      			   throw new Exception(); 
      		   }
    		 } catch (Exception e) {
    			temporal=1;
    			myMessage.setTitle("");
                myMessage.setType("Error");
    			myMessage.setTitle(Utility.messageBD(this, "swa_cannotupdateline_org", vars.getLanguage()));
    			vars.setMessage("ReviewLinkedPurchaseOrder", myMessage);
    		 }	 
         }
         
         if (temporal == 1) {
        	 printPageDataSheet(request, response, vars, strOrderLines,strBPartnerId);
          }else{
        	   myMessage.setTitle("");
               myMessage.setType("Success");
        	   for (int i = 0; selectedOrderLines != null && i < selectedOrderLines.length; i++) {
        		     try {
        		    	 String newPrice = vars.getNumericParameter("inpNewPrice" + selectedOrderLines[i].trim());
        		    	 String OrderLineID = selectedOrderLines[i].trim();
        		    	 int tmp = ReviewLinkedPurchaseOrderData.updateOrderLines(this,OrderLineID ,newPrice, vars.getSessionValue("#AD_CLIENT_ID"));
					} catch (Exception e) {
				           myMessage.setTitle("");
				           myMessage.setType("Error");
				           myMessage.setTitle(Utility.messageBD(this, "SWA_updatelinesandupdate", vars.getLanguage()));
				           vars.setMessage("ReviewLinkedPurchaseOrder", myMessage);
					}
        		  }
        	   printPageDataSheet(request, response, vars, strOrderLines,strBPartnerId);
           }
     } 
     else
      pageError(response);
  }
 
  
  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strOrderIdConcat, String strBPartnerId)      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
 
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReviewLinkedPurchaseOrderData[] getData = null;
    ReviewLinkedPurchaseOrderData[] getDataLines = null;
    
    
    /*
     * Load To Org from Client
     * */
    String ad_org_id = LocalOrg;
    String ad_to_org_id = RemoteOrg;

    
    String strConvRateErrorMsg = "";
    String discard[] = { "discard" };

    xmlDocument = xmlEngine.readXmlTemplate(
        "pe/com/unifiedgo/warehouse/ad_reports/ReviewLinkedPurchaseOrder", discard)
        .createXmlDocument();
    
    if(vars.commandIn("LISTAR")){
    	
      	OBError myMessage = null;
        myMessage = new OBError();
        try {
            getData = ReviewLinkedPurchaseOrderData.selectOrdertoReview(this, ad_org_id,ad_to_org_id,
                    vars.getSessionValue("#AD_CLIENT_ID"));
 
         } catch (ServletException ex) {
            myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
         }
        strConvRateErrorMsg = myMessage.getMessage();
        if (!strConvRateErrorMsg.equals("") && strConvRateErrorMsg != null) {
          advise(request, response, "ERROR",
              Utility.messageBD(this, "NoConversionRateHeader", vars.getLanguage()),
              strConvRateErrorMsg);
        } else { // Otherwise, the report is launched
          if ( (getData == null || getData.length == 0)) {
            discard[0] = "selEliminar";
            getData = ReviewLinkedPurchaseOrderData.set();
          } else {
            xmlDocument.setData("structure5", getData);
          }
        }
    }
    if(vars.commandIn("TOCOMPLETE")){
    	OBError myMessage = null;
        myMessage = new OBError();
        try {
            String strOrderId = vars.getStringParameter("inpOrderToEdit");
            getData = ReviewLinkedPurchaseOrderData.selectOrdertoReview(this, ad_org_id,ad_to_org_id,
                    vars.getSessionValue("#AD_CLIENT_ID"));
            
         } catch (ServletException ex) {
            myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
        }
        strConvRateErrorMsg = myMessage.getMessage();
        if (!strConvRateErrorMsg.equals("") && strConvRateErrorMsg != null) {
          advise(request, response, "ERROR",
              Utility.messageBD(this, "NoConversionRateHeader", vars.getLanguage()),
              strConvRateErrorMsg);
        } else { // Otherwise, the report is launched
          if ( (getDataLines == null || getDataLines.length == 0)) {
            discard[0] = "selEliminar";
            getDataLines = ReviewLinkedPurchaseOrderData.set();
          } else {
           xmlDocument.setData("structure6", getDataLines);
          }
          if((getData == null || getData.length == 0)) {
              discard[0] = "selEliminar";
              getData = ReviewLinkedPurchaseOrderData.set();
            } else {
              xmlDocument.setData("structure5", getData);
          }
        }
    	
    }
    if(vars.commandIn("GENERATELINES")){
    	OBError myMessage = null;
        myMessage = new OBError();
        try {
            String strOrderId = vars.getStringParameter("inpOrderToEdit");
            getDataLines = ReviewLinkedPurchaseOrderData.selectOrderLinetoReview(this, ad_org_id,ad_to_org_id,
                    vars.getSessionValue("#AD_CLIENT_ID"),strOrderId);
            getData = ReviewLinkedPurchaseOrderData.selectOrdertoReview(this, ad_org_id,ad_to_org_id,
                    vars.getSessionValue("#AD_CLIENT_ID"));
            
         } catch (ServletException ex) {
            myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
        }
        strConvRateErrorMsg = myMessage.getMessage();
        if (!strConvRateErrorMsg.equals("") && strConvRateErrorMsg != null) {
          advise(request, response, "ERROR",
              Utility.messageBD(this, "NoConversionRateHeader", vars.getLanguage()),
              strConvRateErrorMsg);
        } else { // Otherwise, the report is launched
          if ( (getDataLines == null || getDataLines.length == 0)) {
            discard[0] = "selEliminar";
            getDataLines = ReviewLinkedPurchaseOrderData.set();
          } else {
           xmlDocument.setData("structure6", getDataLines);
          }
          if((getData == null || getData.length == 0)) {
              discard[0] = "selEliminar";
              getData = ReviewLinkedPurchaseOrderData.set();
            } else {
              xmlDocument.setData("structure5", getData);
          }
        }
    	
    }
    
    if(vars.commandIn("UPDATELINES")){
  	
    	OBError myMessage = null;
        myMessage = new OBError();
        try {
            String strOrderId = OrderID;
            getDataLines = ReviewLinkedPurchaseOrderData.selectOrderLinetoReview(this, ad_org_id,ad_to_org_id,
                    vars.getSessionValue("#AD_CLIENT_ID"),strOrderId);
            getData = ReviewLinkedPurchaseOrderData.selectOrdertoReview(this, ad_org_id,ad_to_org_id,
                    vars.getSessionValue("#AD_CLIENT_ID"));
            
         } catch (ServletException ex) {
            myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
        }
        strConvRateErrorMsg = myMessage.getMessage();
        if (!strConvRateErrorMsg.equals("") && strConvRateErrorMsg != null) {
          advise(request, response, "ERROR",
              Utility.messageBD(this, "NoConversionRateHeader", vars.getLanguage()),
              strConvRateErrorMsg);
        } else { // Otherwise, the report is launched
          if ( (getDataLines == null || getDataLines.length == 0)) {
            discard[0] = "selEliminar";
            getDataLines = ReviewLinkedPurchaseOrderData.set();
          } else {
           xmlDocument.setData("structure6", getDataLines);
          }
          if((getData == null || getData.length == 0)) {
              discard[0] = "selEliminar";
              getData = ReviewLinkedPurchaseOrderData.set();
            } else {
              xmlDocument.setData("structure5", getData);
          }
        }
    	
    }

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReviewLinkedPurchaseOrder",
          false, "", "", "", false, "ad_reports", strReplaceWith, false, true);
      toolbar.setEmail(false);
      toolbar.prepareSimpleToolBarTemplate();
      //toolbar.prepareRelationBarTemplate(false, false, "printXLS();return false;");
      xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "pe.com.unifiedgo.report.ad_reports.ReviewLinkedPurchaseOrder");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
            "ReviewLinkedPurchaseOrder.html", classInfo.id, classInfo.type, strReplaceWith,
            tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
            "ReviewLinkedPurchaseOrder.html", strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      {
        OBError myMessage = vars.getMessage("ReviewLinkedPurchaseOrder");
        vars.removeMessage("ReviewLinkedPurchaseOrder");
        if (myMessage != null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }
      
      try {///ABE594ACE1764B7799DEF0BA6E8A389B 0C754881EAD94243A161111916E9B9C6
          ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
              "ABE594ACE1764B7799DEF0BA6E8A389B", Utility.getContext(this, vars, "#AccessibleOrgTree", "ReviewLinkedPurchaseOrder"),
              Utility.getContext(this, vars, "#User_Client", "ReviewLinkedPurchaseOrder"), 0);
          Utility.fillSQLParameters(this, vars, null, comboTableData, "ReviewLinkedPurchaseOrder",
        		  ad_org_id);
          xmlDocument.setData("reportAD_Org_ID", "liststructure", comboTableData.select(false));
        } catch (Exception ex) {
          throw new ServletException(ex);
        }
      
      try {
          ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
              "0C754881EAD94243A161111916E9B9C6", Utility.getContext(this, vars, "#AccessibleOrgTree", "ReviewLinkedPurchaseOrder"),
              Utility.getContext(this, vars, "#User_Client", "ReviewLinkedPurchaseOrder"), 0);
          Utility.fillSQLParameters(this, vars, null, comboTableData, "ReviewLinkedPurchaseOrder",
        		  ad_org_id);
          xmlDocument.setData("reportTOAD_Org_ID", "liststructure", comboTableData.select(false));
        } catch (Exception ex) {
          throw new ServletException(ex);
        }
      
      xmlDocument.setParameter("BPartnerId", strBPartnerId);
      xmlDocument.setParameter("BPartnerDescription",
    		  ReviewLinkedPurchaseOrderData.selectBpartner(this, strBPartnerId));

      xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("docDatedisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("docDatesaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

   
      xmlDocument.setParameter("adOrgId", ad_org_id);
      xmlDocument.setParameter("adToOrgId", "");

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
            "9A4C02741FAD4E5DAE696E0CBE279F9E", "", Utility.getContext(this, vars,
                "#AccessibleOrgTree", "ReviewLinkedPurchaseOrder"), Utility.getContext(this,
                vars, "#User_Client", "ReviewLinkedPurchaseOrder"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReviewLinkedPurchaseOrder",
            "");
        xmlDocument.setData("reportNumMonths", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      out.println(xmlDocument.print());
      out.close();
    }
  }
  
  

  public String getServletInfo() {
    return "Servlet PurchaseOrderFilter. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method

}