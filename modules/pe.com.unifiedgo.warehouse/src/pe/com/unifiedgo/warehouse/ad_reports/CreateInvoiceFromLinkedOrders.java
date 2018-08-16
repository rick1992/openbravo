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

import org.hibernate.Criteria;
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
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
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

import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;


public class CreateInvoiceFromLinkedOrders extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static String strBDErrorMessage = "";
  private static String LocalOrg = null;
  private static String RemoteOrg = null;
  private static String LocalOrgInv = null;
  private static String OrderID = null;
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    OBError myMessage = null;
    myMessage = new OBError();
    myMessage.setTitle("");
    
    
    if (vars.commandIn("DEFAULT")) {
      String strOrg = vars.getGlobalVariable("inpOrg", "GeneralLedgerJournal|Org", "0");
      String strToOrg = vars.getStringParameter("inpToadOrgId");
      String strCheckedbyrbtn = "pending";
    //  vars.setSessionValue("CreateInvoiceFromLinkedOrders.btnSearch", strCheckedbyrbtn);
      printPageDataSheet(request, response, vars, strOrg,strToOrg, null,strCheckedbyrbtn,null);

    } else if(vars.commandIn("LISTAR")){
        String strOrg = vars.getStringParameter("inpadOrgId");
        String strToOrg = vars.getStringParameter("inpToadOrgId");
        
        String strCheckedbyrbtn = vars.getStringParameter("inprbtnSearch");
        //String strCheckedbyrbtn = "pending";
        LocalOrg = strOrg;
        RemoteOrg = strToOrg;
        printPageDataSheet(request, response, vars, strOrg,strToOrg,null,strCheckedbyrbtn,null);
     }else if(vars.commandIn("TOGENERATEINVOICE")){

    	 LocalOrgInv = vars.getStringParameter("inpadOrgInvId");
    	 String strOrg = LocalOrg;
         String strToOrg = RemoteOrg;
         String strOrgInvc = LocalOrgInv;
         String strCheckedbyrbtn = "pending";
         
         String strRequisitionLines = null;
         
         Organization org = OBDal.getInstance().get(Organization.class, strOrg);
         Organization ToOrg = OBDal.getInstance().get(Organization.class, strToOrg);
         Organization orgInv = OBDal.getInstance().get(Organization.class, strOrgInvc);
         
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
             vars.setMessage("CreateInvoiceFromLinkedOrders", myMessage);
           }
            
         
            //////////////////////////////// Obteniendo Document Type
	        OrganizationStructureProvider osp = OBContext.getOBContext().getOrganizationStructureProvider(
	                 org.getClient().getId());
			OBCriteria<DocumentType> P_doctype_c = OBDal.getInstance().createCriteria(DocumentType.class);
			P_doctype_c.add(Restrictions.eq(DocumentType.PROPERTY_SCOSPECIALDOCTYPE, "SCOARINVOICE"));
			P_doctype_c.add(Restrictions.in("organization.id", osp.getParentTree(org.getId(), true)));
			P_doctype_c.setMaxResults(1);
			DocumentType P_shipDocType = (DocumentType) P_doctype_c.uniqueResult();
			
			if("".equals(P_shipDocType) || P_shipDocType == null) {
				temporal=1;
				myMessage.setTitle("");
				myMessage.setType("Error");
				myMessage.setTitle(Utility.messageBD(this, "swa_salesinvoice_doctype_missing", vars.getLanguage()));
				vars.setMessage("CreateInvoiceFromLinkedOrders", myMessage);
			}
			/////////////////////////////////////////
			
			/////////////////////////////////Obteniendo Order Type for Invoice
			 OBCriteria<SCRComboItem> comboItem = OBDal.getInstance().createCriteria(SCRComboItem.class);
	         comboItem.add(Restrictions.eq(SCRComboItem.PROPERTY_CLIENT, org.getClient()));
        	 comboItem.add(Restrictions.eq(SCRComboItem.PROPERTY_SEARCHKEY, "CompraextraordinariaentreEmpresasOut"));
	       	 SCRComboItem comboItem_get = null;
	       		try {
	       			comboItem_get = (SCRComboItem) comboItem.uniqueResult();
	       			if (comboItem_get == null)
	       				throw new OBException("");	
	       		} catch (Exception e) {
	       			 temporal=1;
	       			 myMessage.setTitle("");
		             myMessage.setType("Error");
		             myMessage.setTitle(Utility.messageBD(this, "swa_nofound_comboitem_purchaseorder_org", vars.getLanguage()));
		             vars.setMessage("CreateInvoiceFromLinkedOrders", myMessage);
	       		}
	        ////////////////////////////////////////////
	       		
	       		
	       	//////////////////   Validando Org Hija para Factura
	       	if(orgInv == null || "".equals(orgInv)){
	       		temporal=1;
				myMessage.setTitle("");
				myMessage.setType("Error");
				myMessage.setTitle(Utility.messageBD(this, "swa_notfoundOrgtoInvoice", vars.getLanguage()));
				vars.setMessage("CreateInvoiceFromLinkedOrders", myMessage);
	       	}
	        ///////////////////
	       		
	       	String auxiliar = null;	
	       	if(temporal==1){
				printPageDataSheet(request, response, vars, strOrg,strToOrg,strRequisitionLines,strCheckedbyrbtn,null);
			}else{
				String invoiceCreated ="";
				mainloop:
				for (int i = 0; selectedOrders != null && i < selectedOrders.length; i++) {
					  ///////   Revisando Estado de La Orden de Compra para generar la Factura -- Vafaster was Here.!!!
	        		   try {
	        			   CreateInvoiceFromLinkedOrdersData[] stateOrder = null;
	            		   stateOrder =  CreateInvoiceFromLinkedOrdersData.selectOrderReviewStatus(this, strOrg,strToOrg,
	                       vars.getSessionValue("#AD_CLIENT_ID"),selectedOrders[i].trim());
	            		   if(!stateOrder[0].orderreviewstate.equals("CH")){
	            			   myMessage.setTitle("");
	                           myMessage.setType("Warning");
	            			   myMessage.setTitle(Utility.messageBD(this, "swa_purchaseorgupdated_war", vars.getLanguage()));
	            			   vars.setMessage("CreateInvoiceFromLinkedOrders", myMessage);
	            			   break;
	            		   }
					   } catch (Exception e) {
						   myMessage.setTitle("");
				           myMessage.setType("Error");
				           myMessage.setTitle(Utility.messageBD(this, "SWA_errorupdateorderorg", vars.getLanguage()));
				           vars.setMessage("CreateInvoiceFromLinkedOrders", myMessage);
				           break;
					   }
	        		  /////////////////////////////////
	        		   
	        		  Order fromOrder = null;
	        		  fromOrder = OBDal.getInstance().get(Order.class,selectedOrders[i].trim());
	        		  List<OrderLine> orderLineList = fromOrder.getOrderLineList();
	        		  
	        		  //////////////////////////  Obteniendo BusinessPartner
	        		  BusinessPartner bPartnerToInvoice = null;
	        		  Location bPartnerLocation = null;
	        		  
	        		  try {
	        			  CreateInvoiceFromLinkedOrdersData[] BusinessData = null;
	        			   BusinessData =  CreateInvoiceFromLinkedOrdersData.selectBusinessPartner(this, strOrg,strToOrg,
	                       vars.getSessionValue("#AD_CLIENT_ID"),selectedOrders[i].trim());
	        			   //No hay Tercero
	            		   if(BusinessData[0].businesspartnerid.equals("") || BusinessData[0].businesspartnerid == null){
	            			   myMessage.setTitle("");
	                           myMessage.setType("Error");
	            			   myMessage.setTitle(Utility.messageBD(this, "swa_businesspartnernotfound_org", vars.getLanguage()));
	            			   vars.setMessage("CreateInvoiceFromLinkedOrders", myMessage);
	            			   break;
	            		   }
	            		   //No hay Dirección Para Tercero
	            		   if(BusinessData[0].busineslocationid.equals("") || BusinessData[0].busineslocationid == null){
	            			   myMessage.setTitle("");
	                           myMessage.setType("Error");
	            			   myMessage.setTitle(Utility.messageBD(this, "swa_businessLocatornotfound_org", vars.getLanguage()));
	            			   vars.setMessage("CreateInvoiceFromLinkedOrders", myMessage);
	            			   break;
	            		   }
	            		   bPartnerToInvoice = OBDal.getInstance().get(BusinessPartner.class, BusinessData[0].businesspartnerid);
	            		   bPartnerLocation = OBDal.getInstance().get(Location.class, BusinessData[0].busineslocationid);
					   } catch (Exception e) {
						   myMessage.setTitle("");
				           myMessage.setType("Error");
				           myMessage.setTitle(Utility.messageBD(this, "swa_businesspartnernotfound_org", vars.getLanguage()));
				           vars.setMessage("CreateInvoiceFromLinkedOrders", myMessage);
				           break;
					   }
	        		  //////////////////////////
	        		  
	        		  
					  ///////////////////////////  Obteniendo Price List
					 		OBCriteria<PriceList> m_plicelist = OBDal.getInstance().createCriteria(PriceList.class);
							m_plicelist.add(Restrictions.eq(PriceList.PROPERTY_CLIENT, org.getClient()));
							m_plicelist.add(Restrictions.eq(PriceList.PROPERTY_CURRENCY, fromOrder.getCurrency()));
							m_plicelist.add(Restrictions.eq(PriceList.PROPERTY_ORGANIZATION, org));
							PriceList newpricelist = null;
							List<PriceList> listPriceList = m_plicelist.list();
							if(listPriceList == null  || listPriceList.size()<1){
					     			 myMessage.setTitle("");
						             myMessage.setType("Error");
						             myMessage.setTitle(Utility.messageBD(this, "swa_noPriceListForCurrency", vars.getLanguage()));
						             vars.setMessage("CreateInvoiceFromLinkedOrders", myMessage);
							}
							newpricelist = (PriceList) listPriceList.get(0);
					 	//////////////////////////////////
	        		  
	        		  Invoice invoice = null;
	        		  long lineNo = 0L;
	        		  for(int j=0;j<orderLineList.size();j++){
	        			try {
	        				if(j%15==0){
		        				  lineNo = 10L;
		        				  invoice = CopyInvoice(orgInv ,bPartnerToInvoice ,bPartnerLocation, P_shipDocType, comboItem_get, fromOrder, ToOrg, newpricelist);
		        				  invoiceCreated = invoiceCreated + " : " + invoice.getDocumentNo();
		        			  }
		        			  ////  Obteniendo El producto a insertar en la Facutra, a partir de la Orden de la Otra Organización, se referencia el campo InternalCode
	        				  OBCriteria<Product> product = OBDal.getInstance().createCriteria(Product.class);
	        				  product.add(Restrictions.eq(Product.PROPERTY_CLIENT, org.getClient()));
	        				  product.add(Restrictions.eq(Product.PROPERTY_ORGANIZATION, org));
	        				  product.add(Restrictions.eq(Product.PROPERTY_SEARCHKEY, orderLineList.get(j).getProduct().getScrInternalcode().trim()));
	        				  List<Product> productList = product.list();
	        				  Product ProductInv=null;
	        				  if(productList==null || productList.size() ==0){
	        					  myMessage.setTitle("");
						          myMessage.setType("Error");
						          myMessage.setTitle(Utility.messageBD(this, "em_swa_nofound_product_org", vars.getLanguage()) + fromOrder.getDocumentNo() + " - " + orderLineList.get(j).getProduct().getSearchKey());
						          vars.setMessage("CreateInvoiceFromLinkedOrders", myMessage);
	        					  break mainloop;
	        				  }
	        				  else
	        					  ProductInv = (Product) productList.get(0);

	        				  createlinesInvoice(invoice,bPartnerToInvoice , ProductInv , orderLineList.get(j), lineNo);
	        				  lineNo += 10L;
						} catch (Exception e) {
							myMessage.setTitle("");
					        myMessage.setType("Error");
					        myMessage.setTitle(Utility.messageBD(this, "em_swa_not_insert_invoice", vars.getLanguage()) + e.getMessage());
					        vars.setMessage("CreateInvoiceFromLinkedOrders", myMessage);
 						    break mainloop;
						}  
	        		  }
	        			myMessage.setTitle("");
	    	            myMessage.setType("Success");
	     			    myMessage.setTitle(Utility.messageBD(this, "swa_invoice_created_org", vars.getLanguage()) + " : " + invoiceCreated);
	     			    vars.setMessage("CreateInvoiceFromLinkedOrders", myMessage);
	     			    auxiliar="ShowData";
	        	   }
			
			}
         printPageDataSheet(request, response, vars, strOrg,strToOrg,strRequisitionLines,strCheckedbyrbtn,auxiliar);
     }else if(vars.commandIn("VIEWINVOICE")){
         String strOrg = LocalOrg;
         String strToOrg = RemoteOrg;
         OrderID = vars.getStringParameter("inpOrderToEdit");
         String strRequisitionLines = null;
         printPageDataSheet(request, response, vars, strOrg,strToOrg,strRequisitionLines,null,null);
     } else if(vars.commandIn("UPDATELINES")){
         String strOrg = LocalOrg;
         String strToOrg = RemoteOrg;
         String strOrderId = OrderID;
         String[] selectedOrderLines = null;
         String strOrderLines = null;
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
           vars.setMessage("CreateInvoiceFromLinkedOrders", myMessage);
         }
         
         if(temporal != 1){
        	 try {
            	 CreateInvoiceFromLinkedOrdersData[] stateOrder = null;
            	 stateOrder =  CreateInvoiceFromLinkedOrdersData.selectOrderReviewStatus(this, strOrg,strToOrg,
                               vars.getSessionValue("#AD_CLIENT_ID"),strOrderId);
            	 if(!stateOrder[0].orderreviewstate.equals("PD")){
      			   throw new Exception(); 
      		    }
    		 } catch (Exception e) {
    			temporal=1;
    			myMessage.setTitle("");
                myMessage.setType("Error");
    			myMessage.setTitle(Utility.messageBD(this, "swa_cannotupdateline_org", vars.getLanguage()));
    			vars.setMessage("CreateInvoiceFromLinkedOrders", myMessage);
    		 }	 
         }
         
         if (temporal == 1) {
        	 printPageDataSheet(request, response, vars, strOrg,strToOrg,strOrderLines,null,null);
          }else{
        	   myMessage.setTitle("");
               myMessage.setType("Success");
        	   for (int i = 0; selectedOrderLines != null && i < selectedOrderLines.length; i++) {
        		     try {
        		    	 String newPrice = vars.getNumericParameter("inpNewPrice" + selectedOrderLines[i].trim());
        		    	 String OrderLineID = selectedOrderLines[i].trim();
        		    	 int tmp = CreateInvoiceFromLinkedOrdersData.updateOrderLines(this,OrderLineID ,newPrice, vars.getSessionValue("#AD_CLIENT_ID"));
					} catch (Exception e) {
				           myMessage.setTitle("");
				           myMessage.setType("Error");
				           myMessage.setTitle(Utility.messageBD(this, "SWA_updatelinesandupdate", vars.getLanguage()));
				           vars.setMessage("CreateInvoiceFromLinkedOrders", myMessage);
					}
        		  }
        	   printPageDataSheet(request, response, vars, strOrg,strToOrg,strOrderLines,null,null);
           }
     } 
     else
      pageError(response);
  }
 
  
  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strOrg, String strToOrg, String strOrderIdConcat, String strValuerdBtn,String auxiliar)      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    CreateInvoiceFromLinkedOrdersData[] getData = null;
    CreateInvoiceFromLinkedOrdersData[] getDataLines = null;
    
    String strConvRateErrorMsg = "";
    String discard[] = { "discard" };

    xmlDocument = xmlEngine.readXmlTemplate(
        "pe/com/unifiedgo/warehouse/ad_reports/CreateInvoiceFromLinkedOrders", discard)
        .createXmlDocument();
    
    if(vars.commandIn("LISTAR")){
    	OBError myMessage = null;
        myMessage = new OBError();
        try {
            String ad_org_id = strOrg;
            String ad_to_org_id = strToOrg;
            
            if(strValuerdBtn.equals("pending")){
                getData = CreateInvoiceFromLinkedOrdersData.selectOrdertoCreateInvoice(this, ad_org_id,ad_to_org_id,
                    vars.getSessionValue("#AD_CLIENT_ID"));
            }else if(strValuerdBtn.equals("created")){
            	getData = CreateInvoiceFromLinkedOrdersData.selectOrdertoReviewInvoice(this, ad_org_id,ad_to_org_id,
                        vars.getSessionValue("#AD_CLIENT_ID"));	
            }else{
            	getData = CreateInvoiceFromLinkedOrdersData.selectOrdertoCreateInvoice(this, ad_org_id,ad_to_org_id,
                        vars.getSessionValue("#AD_CLIENT_ID"));
            }
           
            
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
            getData = CreateInvoiceFromLinkedOrdersData.set();
          } else {
            xmlDocument.setData("structure5", getData);
          }
        }
    }
    if(vars.commandIn("TOGENERATEINVOICE")){
    	strOrg=LocalOrg;
    	strToOrg=RemoteOrg;
    	OBError myMessage = null;
        myMessage = new OBError();
        if(auxiliar==null){
        	try {
                String strOrderId = vars.getStringParameter("inpOrderToEdit");
                getData = CreateInvoiceFromLinkedOrdersData.selectOrdertoCreateInvoice(this, strOrg,strToOrg,
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
                getDataLines = CreateInvoiceFromLinkedOrdersData.set();
              } else {
               xmlDocument.setData("structure6", getDataLines);
              }
              if((getData == null || getData.length == 0)) {
                  discard[0] = "selEliminar";
                  getData = CreateInvoiceFromLinkedOrdersData.set();
                } else {
                	
                  xmlDocument.setData("structure5", getData);
              }
            }
        }
    }
    if(vars.commandIn("VIEWINVOICE")){
    	strOrg=LocalOrg;
    	strToOrg=RemoteOrg;
    	OBError myMessage = null;
        myMessage = new OBError();
        try {
            String ad_org_id = strOrg;
            String ad_to_org_id = strToOrg;
            String strOrderId = vars.getStringParameter("inpOrderToEdit");
            getDataLines = CreateInvoiceFromLinkedOrdersData.selectInvoiceReview(this, ad_org_id,ad_to_org_id,
                    vars.getSessionValue("#AD_CLIENT_ID"),strOrderId);
            getData = CreateInvoiceFromLinkedOrdersData.selectOrdertoReviewInvoice(this, strOrg,strToOrg,
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
            getDataLines = CreateInvoiceFromLinkedOrdersData.set();
          } else {
           xmlDocument.setData("structure6", getDataLines);
          }
          if((getData == null || getData.length == 0)) {
              discard[0] = "selEliminar";
              getData = CreateInvoiceFromLinkedOrdersData.set();
            } else {
              xmlDocument.setData("structure5", getData);
          }
        }
    	
    }
    
    if(vars.commandIn("UPDATELINES")){
    	strOrg=LocalOrg;
    	strToOrg=RemoteOrg;
    	
    	OBError myMessage = null;
        myMessage = new OBError();
        try {
            String ad_org_id = strOrg;
            String ad_to_org_id = strToOrg;
            String strOrderId = OrderID;
            getDataLines = CreateInvoiceFromLinkedOrdersData.selectInvoiceReview(this, ad_org_id,ad_to_org_id,
                    vars.getSessionValue("#AD_CLIENT_ID"),strOrderId);
            getData = CreateInvoiceFromLinkedOrdersData.selectOrdertoCreateInvoice(this, ad_org_id,ad_to_org_id,
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
            getDataLines = CreateInvoiceFromLinkedOrdersData.set();
          } else {
           xmlDocument.setData("structure6", getDataLines);
          }
          if((getData == null || getData.length == 0)) {
              discard[0] = "selEliminar";
              getData = CreateInvoiceFromLinkedOrdersData.set();
            } else {
              xmlDocument.setData("structure5", getData);
          }
        }
    	
    }

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "CreateInvoiceFromLinkedOrders",
          false, "", "", "", false, "ad_reports", strReplaceWith, false, true);
      toolbar.setEmail(false);
      toolbar.prepareSimpleToolBarTemplate();
      //toolbar.prepareRelationBarTemplate(false, false, "printXLS();return false;");
      xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "pe.com.unifiedgo.report.ad_reports.CreateInvoiceFromLinkedOrders");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
            "CreateInvoiceFromLinkedOrders.html", classInfo.id, classInfo.type, strReplaceWith,
            tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
            "CreateInvoiceFromLinkedOrders.html", strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      {
        OBError myMessage = vars.getMessage("CreateInvoiceFromLinkedOrders");
        vars.removeMessage("CreateInvoiceFromLinkedOrders");
        if (myMessage != null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }
      
      try {
          ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
              "0C754881EAD94243A161111916E9B9C6", Utility.getContext(this, vars, "#AccessibleOrgTree", "CreateInvoiceFromLinkedOrders"),
              Utility.getContext(this, vars, "#User_Client", "CreateInvoiceFromLinkedOrders"), 0);
          Utility.fillSQLParameters(this, vars, null, comboTableData, "CreateInvoiceFromLinkedOrders",
        		  strOrg);
          xmlDocument.setData("reportAD_Org_ID", "liststructure", comboTableData.select(false));
        } catch (Exception ex) {
          throw new ServletException(ex);
        }
      
      try {
          ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
              "0C754881EAD94243A161111916E9B9C6", Utility.getContext(this, vars, "#AccessibleOrgTree", "CreateInvoiceFromLinkedOrders"),
              Utility.getContext(this, vars, "#User_Client", "CreateInvoiceFromLinkedOrders"), 0);
          Utility.fillSQLParameters(this, vars, null, comboTableData, "CreateInvoiceFromLinkedOrders",
        		  strOrg);
          xmlDocument.setData("reportTOAD_Org_ID", "liststructure", comboTableData.select(false));
        } catch (Exception ex) {
          throw new ServletException(ex);
        }
      
      try {
          ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "",
              "ABE594ACE1764B7799DEF0BA6E8A389B", Utility.getContext(this, vars,"#AccessibleOrgTree", "CreateInvoiceFromLinkedOrders"), 
              Utility.getContext(this, vars,"#User_Client", "CreateInvoiceFromLinkedOrders"), 0);
          Utility.fillSQLParameters(this, vars, null, comboTableData, "CreateInvoiceFromLinkedOrders",
        		  strOrg);
          xmlDocument.setData("reportAD_Org_Inv_ID", "liststructure", comboTableData.select(false));
          comboTableData = null;
        } catch (Exception ex) {
          throw new ServletException(ex);
        }

      xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("docDatedisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("docDatesaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

   
      xmlDocument.setParameter("adOrgId", strOrg);
      xmlDocument.setParameter("adToOrgId", strToOrg);
      xmlDocument.setParameter("pending", strValuerdBtn);
      xmlDocument.setParameter("created", strValuerdBtn);
      xmlDocument.setParameter("all", strValuerdBtn);

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
            "9A4C02741FAD4E5DAE696E0CBE279F9E", "", Utility.getContext(this, vars,
                "#AccessibleOrgTree", "CreateInvoiceFromLinkedOrders"), Utility.getContext(this,
                vars, "#User_Client", "CreateInvoiceFromLinkedOrders"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "CreateInvoiceFromLinkedOrders",
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
  
  private Invoice CopyInvoice(Organization OrgInv,BusinessPartner Tercero, Location BpartnerLocation ,DocumentType P_shipDocType,SCRComboItem comboItem ,Order MyRequestOrder, Organization  org, PriceList newpricelist){
	 
	   Invoice invoice = OBProvider.getInstance().get(Invoice.class);
	   invoice.setOrganization(OrgInv);
	   invoice.setClient(MyRequestOrder.getClient());
	   invoice.setDocumentType(OBDal.getInstance().get(DocumentType.class, "0"));
	   invoice.setTransactionDocument(P_shipDocType);
	   
	   invoice.setSSAOrderType(comboItem);//; coPodoctypeComboitem(comboItem);
	   
	   
	   invoice.setDocumentNo(FIN_Utility.getDocumentNo(org, "ARI", "C_Invoice"));
	   invoice.setAccountingDate(new Date());
	   invoice.setInvoiceDate(MyRequestOrder.getOrderDate());
	   invoice.setBusinessPartner(Tercero);
	   invoice.setPartnerAddress(BpartnerLocation);
	   invoice.setPriceList(newpricelist);
	   invoice.setCurrency(MyRequestOrder.getCurrency());
	   invoice.setSalesTransaction(true);
	   //invoice.setSsaOtherInvoice(FromInvoice);
	   //invoice.setScrPhysicalDocumentno(FromInvoice.getScrPhysicalDocumentno());
	   //invoice.setScoPurchaseinvoicetype("SCO_PURNA");//National Geographic :)
	   //invoice.setGrandTotalAmount(FromInvoice.getGrandTotalAmount());
	   //invoice.setSummedLineAmount(FromInvoice.getSummedLineAmount());
	   
	   invoice.setPaymentMethod(MyRequestOrder.getPaymentMethod());
	   invoice.setPaymentTerms(MyRequestOrder.getPaymentTerms());
	   OBDal.getInstance().save(invoice);
	   //createlinesInvoice(invoice,FromInvoice,MyRequestOrder);
	   //createlinesInvoice2(invoice,FromInvoice,MyRequestOrder);
	  return invoice;
 }
  
 private void createlinesInvoice(Invoice newInvoice,BusinessPartner bPartner ,Product product,OrderLine orderline, long lineNo){
	    InvoiceLine newInvoiceLine = OBProvider.getInstance().get(InvoiceLine.class);
   	    		    newInvoiceLine.setClient(newInvoice.getClient());
       	    	    newInvoiceLine.setOrganization(newInvoice.getOrganization());
	     	    	newInvoiceLine.setUOM(orderline.getUOM());
	     	    	newInvoiceLine.setProduct(product);
	     	    	newInvoiceLine.setLineNo(lineNo);
	     	    	newInvoiceLine.setInvoice(newInvoice);
	     	    	newInvoiceLine.setBusinessPartner(newInvoice.getBusinessPartner());
	     	    	newInvoiceLine.setInvoicedQuantity(orderline.getOrderedQuantity());
	     	    	newInvoiceLine.setUnitPrice(orderline.getUnitPrice());
	     	    	newInvoiceLine.setLineNetAmount(orderline.getLineNetAmount());
	     	    	newInvoiceLine.setTax(orderline.getTax());
	     	    	newInvoiceLine.setBusinessPartner(bPartner);
	     	    	newInvoiceLine.setStandardPrice(orderline.getUnitPrice());
	     	    	newInvoiceLine.setGrossAmount(orderline.getUnitPrice());
	     	    	newInvoiceLine.setBaseGrossUnitPrice(orderline.getUnitPrice());
	     	    	
	     OBDal.getInstance().save(newInvoiceLine); 
	     
 }
  
  
  

  public String getServletInfo() {
    return "Servlet PurchaseOrderFilter. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method

}
