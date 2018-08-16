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
package pe.com.unifiedgo.imports.ad_actionButton;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.time.DateUtils;
import org.hibernate.bytecode.buildtime.ExecutionException;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.IsPositiveIntFilter;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.PriceAdjustment;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBErrorBuilder;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.financial.FinancialUtils;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.financialmgmt.payment.Incoterms;
import org.openbravo.model.financialmgmt.payment.PaymentTerm;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.geography.Country;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.plm.Product;
import org.openbravo.utils.Replace;
import org.openbravo.xmlEngine.XmlDocument;

import com.thoughtworks.xstream.converters.basic.BigDecimalConverter;

import pe.com.unifiedgo.imports.ad_actionButton.GeneratePartialLines;
import pe.com.unifiedgo.imports.data.SimOrderImport;
import pe.com.unifiedgo.imports.data.SimOrderImportLine;

import java.util.Enumeration;


public class GeneratePartialLines extends HttpSecureAppServlet {
  final private Date now = DateUtils.truncate(new Date(), Calendar.DATE);
  private static final long serialVersionUID = 1L;
  private static final BigDecimal ZERO = BigDecimal.ZERO;
  private String strOrderImportID= "";
  private String strBpartnerId= "";
  private String strBpartnerLocationId= "";
  private String strIncotermsId= "";
  private String strtypepurchaseorder= "";
  private String strCurrencyId= "";
  private String strCountryId= "";
  private String strClientId= "";
  private String strOrgId= "";
  private String strWarehouseId= "";
  private String strPaymenTerm = "";
  private String strFinPaymenTerm = "";
  private String strTabId = "";
  private String strWindowId = "";
  private Order order=null;
  long lineNo;
  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }
  
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
   // Order Order = null;
    OBError myMessage = null;
    myMessage = new OBError();
    myMessage.setTitle("");
    if (vars.commandIn("DEFAULT")) {
    	 
    //	    Enumeration<String> params = vars.getParameterNames(); while (params.hasMoreElements())
    //	    { System.out.println(params.nextElement()); }
    	     
	   	try {
	   	 strWindowId = vars.getStringParameter("inpwindowId");
         strClientId = vars.getStringParameter("inpadClientId");
         strOrgId = vars.getStringParameter("inpadOrgId");
         strWarehouseId = vars.getStringParameter("inpmWarehouseId"); 
         strPaymenTerm = vars.getStringParameter("inpcPaymenttermId");
         strFinPaymenTerm = vars.getStringParameter("inpfinPaymentmethodId");
         
    	 strOrderImportID = vars.getStringParameter("SIM_Orderimport_ID");
    	 strBpartnerId = vars.getStringParameter("inpcBpartnerId");
    	 strBpartnerLocationId = vars.getStringParameter("inpcBpartnerLocationId");
    	 strIncotermsId = vars.getStringParameter("inpcIncotermsId");
    	 strtypepurchaseorder = vars.getStringParameter("inptypepurchaseorder");
    	 strCurrencyId = vars.getStringParameter("inpcCurrencyId");
    	 strCountryId = vars.getStringParameter("inpcCountryId");
    	 strTabId = vars.getStringParameter("inpTabId");
    	 
    	 SimOrderImport sim_import = OBDal.getInstance().get(SimOrderImport.class, strOrderImportID);
    	 
    	 BigDecimal c_count;
    	 Query q = OBDal.getInstance().getSession().createSQLQuery("select sum(qtyreserved) from sim_orderimportline where sim_orderimport_id = '"  + strOrderImportID + "'");
    	
         if(Float.parseFloat(q.uniqueResult().toString())== 0)
         { 
        	 
               OBError myError;
               myError = new OBError();
               myError.setType("Success");
               myError.setTitle(OBMessageUtils.messageBD("Information"));
               myError.setMessage(OBMessageUtils.messageBD("sim_zeroPendings"));
        	//   myError = OBMessageUtils.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
        	   
        	   String strWindowPath = Utility.getTabURL(strTabId, "R", true, getDireccion());
               if (strWindowPath.equals(""))
                   strWindowPath = strDefaultServlet;
              vars.setMessage(strTabId, myError);
              printPageClosePopUp(response, vars, strWindowPath);
         }else{
        	 printPageDataSheet(response, vars, 
                     strWindowId, 
                     strTabId, 
                     strOrderImportID,
                     strBpartnerId, 
                     strBpartnerLocationId, 
                     strCurrencyId , 
                     strCountryId);
         }
	   		
		} catch (Exception e) {
			OBError myError;
            myError = new OBError();
            myError.setType("Warning");
            myError.setTitle(OBMessageUtils.messageBD("Warning"));
            myError.setMessage(OBMessageUtils.messageBD("Sim_pressButtonOk"));
			
			
			String strWindowPath = Utility.getTabURL(strTabId, "R", true);
            if (strWindowPath.equals(""))
                strWindowPath = strDefaultServlet;
            vars.setMessage(strTabId, myError);
            printPageClosePopUp(response, vars, strWindowPath);
		}
    } else if (vars.commandIn("SAVE")) {
    	String[] selectedLines = null;
    	String strRownum = null;
    	String strRownum2 = null;
    	String strLine = null;
    	String strProductID = null;
    	String strotro = null;
    	String orderQty= null;
    	String v_orderQty= null;
    	//Order order = null;
    	OrderLine orderLine = null;
    	int qty = 0;
    	int temporal = 0;
    	int temporal2 = 0;
        try {
          strProductID = vars.getRequiredInStringParameter("inpRownumId",IsIDFilter.instance);
          strProductID = strProductID.replace("(", "");
          strProductID = strProductID.replace(")", "");
          strProductID = strProductID.replace("'", "");
          selectedLines = strProductID.split(",");
        } catch (Exception e) {
        	temporal=1;
  	        OBError myError;
	        myError = new OBError();
	        myError.setType("Error");
	        //myError.setTitle(OBMessageUtils.messageBD(this, "SWA_noselected", vars.getLanguage()));
	        myError.setMessage(OBMessageUtils.messageBD(this, "SWA_noselected", vars.getLanguage()));
	        vars.setMessage(strTabId, myError);
        }  
        SimOrderImportLine sim_importLines_tmp = null;
        if(temporal==0){ //THERE ARE SELECTED ELEMENTS
        	//VALIDATION
        	BigDecimal qtyOrderedBd = BigDecimal.ZERO;
        	try {
	        	for(int i = 0; selectedLines != null && i < selectedLines.length; i++) {
	        		sim_importLines_tmp = OBDal.getInstance().get(SimOrderImportLine.class, selectedLines[i].trim());
	                 v_orderQty = "";
	              	 v_orderQty = vars.getNumericParameter("inpOrderQty" + selectedLines[i].trim());
	              	qtyOrderedBd = new BigDecimal(v_orderQty);
	              		//if (Float.parseFloat(v_orderQty) < 0){
	              	 if (qtyOrderedBd.compareTo(BigDecimal.ZERO) ==-1){
	              			temporal2 = 1;
	              			OBError myError;
		   		    	    myError = new OBError();
		   		    	    myError.setType("Error");
		   		    	    myError.setTitle(OBMessageUtils.messageBD("Error"));
		   		    	    myError.setMessage(OBMessageUtils.messageBD("sim_lowerthanzero") + ": "+ sim_importLines_tmp.getProduct().getName());
		   		    	    vars.setMessage(strTabId, myError);
		   		    //	    printPageClosePopUp(response, vars, strWindowPath);
		   		    	    break;
	              		//}else if(Float.parseFloat(v_orderQty) > sim_importLines_tmp.getReservedQuantity().floatValue()){
	              		}else if(qtyOrderedBd.compareTo(sim_importLines_tmp.getReservedQuantity())==1 ){	
	              			temporal2 = 1;
	              			OBError myError;
		   		    	    myError = new OBError();
		   		    	    myError.setType("Error");
		   		    	    myError.setTitle(OBMessageUtils.messageBD("Error"));
		   		    	    myError.setMessage(OBMessageUtils.messageBD("sim_arrivalgreater") + ": "+ sim_importLines_tmp.getProduct().getName());
		   		    	    vars.setMessage(strTabId, myError);
	              		}
	        	}
        	 } catch (Exception e) {
				 temporal2 = 1;
				 OBError myError;
	    	     myError = new OBError();
	    	     myError.setType("Error");
	    	     myError.setTitle(OBMessageUtils.messageBD("Error"));
	    	     myError.setMessage(OBMessageUtils.messageBD("sim_invalidnumber") + ": " + sim_importLines_tmp.getProduct().getName());
	    	     vars.setMessage(strTabId, myError);
			 } 
        	//END Validation
        	
        	
        	
        	if(temporal2 == 0){ // NOT EXECPTION - ARRIVAL NUMBER
        		 SimOrderImport sim_import = OBDal.getInstance().get(SimOrderImport.class, strOrderImportID);
                 //Create order in Table C_oder
                // order = createOrder(vars);
                 int createOrder=0;
                 createOrder = createOrder(vars);
//                 System.out.println(order);
                 if(createOrder == 1){
                	 OBError myError;
          	        myError = new OBError();
          	        myError.setType("Error");
          	        myError.setTitle(OBMessageUtils.messageBD("Error"));
          	        myError.setMessage(OBMessageUtils.messageBD("sim_DoctypeMissingP"));
          	        vars.setMessage(strTabId, myError);
                	 
                 }else if(createOrder == 4){
                	 OBError myError;
           	        myError = new OBError();
           	        myError.setType("Error");
           	        myError.setTitle(OBMessageUtils.messageBD("Error"));
           	        myError.setMessage(OBMessageUtils.messageBD("sim_docTypeDuplicate"));
           	        vars.setMessage(strTabId, myError);
                 }
                 else{
                	 lineNo = 10L;
                     //For to Lines Checked in html
                     for(int i = 0; selectedLines != null && i < selectedLines.length; i++) {
                   	  orderQty = "";
                      	  orderQty = vars.getNumericParameter("inpOrderQty" + selectedLines[i].trim());
                         SimOrderImportLine sim_importLines = null;
                         sim_importLines = OBDal.getInstance().get(SimOrderImportLine.class, selectedLines[i].trim());
                     	  orderLine = createOrderLine(vars,sim_import,sim_importLines, order, orderQty);
                     	  if (orderLine == null){
                     		throw new ExecutionException("DocMissing");
                     	  }
                      }
                     
                    OBError myError;
         	        myError = new OBError();
         	        myError.setType("Success");
         	        myError.setTitle(OBMessageUtils.messageBD("Success"));
         	        myError.setMessage(OBMessageUtils.messageBD("sim_createPartialsucess") + order.getDocumentNo());
         	        vars.setMessage(strTabId, myError);
                 }
                 
        	}
        }
          
        String strWindowPath = Utility.getTabURL(strTabId, "R", true);
        if (strWindowPath.equals(""))
            strWindowPath = strDefaultServlet;
        //vars.setMessage(strTabId, myError);
        printPageClosePopUp(response, vars, strWindowPath);
    } 
  }
  
  private OrderLine createOrderLine(VariablesSecureApp vars, SimOrderImport simOrder, SimOrderImportLine simOrderLines, Order order, String orderQty) {
	  
	  BusinessPartner bPartner  = OBDal.getInstance().get(BusinessPartner.class, strBpartnerId);
	  Product product = OBDal.getInstance().get(Product.class, simOrderLines.getProduct().getId());
	  
	try {
		
		
		  OrderLine orderLine  = OBProvider.getInstance().get(OrderLine.class);
		  orderLine.setClient(simOrder.getClient());
		  orderLine.setOrganization(simOrder.getOrganization());
		  orderLine.setUpdated(now);
		  orderLine.setSalesOrder(order);
		  orderLine.setLineNo(lineNo);
		  lineNo += 10L;
		  orderLine.setBusinessPartner(bPartner);
		  orderLine.setPartnerAddress(simOrderLines.getPartnerAddress());
		  orderLine.setOrderDate(simOrder.getOrderDate());
		  orderLine.setScheduledDeliveryDate(simOrder.getScheduledDeliveryDate());
		  orderLine.setProduct(simOrderLines.getProduct());
		  orderLine.setWarehouse(simOrder.getWarehouse());
		  orderLine.setUOM(simOrderLines.getUOM());
		  //BigDecimal Quantity = new BigDecimal(simOrderLines.getOrderedQuantity());
		  BigDecimal Quantity = new BigDecimal(orderQty);
		  orderLine.setOrderedQuantity(Quantity);
		  orderLine.setCurrency(simOrderLines.getCurrency());
		  orderLine.setListPrice(simOrderLines.getListPrice());
		  
		  orderLine.setUnitPrice(simOrderLines.getUnitPrice());
		  
		  //El descuento ahora serà agregando una linea de descuento a la factura
         /* if(simOrder.getScrDiscount()!=null){
        	  BigDecimal Discount = simOrderLines.getUnitPrice().multiply(simOrder.getScrDiscount().divide(new BigDecimal(100)));
        	  orderLine.setUnitPrice(simOrderLines.getUnitPrice().subtract(Discount));
		  }
		  orderLine.setLineNetAmount(simOrderLines.getLineNetAmount());
		  orderLine.setDiscount(simOrder.getScrDiscount());*/
		  orderLine.setTax(simOrderLines.getTax());
		  
		  
		  BigDecimal standarPrice = new BigDecimal(simOrderLines.getStandardPrice());
		  orderLine.setStandardPrice(simOrderLines.getUnitPrice());
         
		  
		  
		  orderLine.setTaxableAmount(simOrderLines.getLineNetAmount());
		  //orderLine.setSIMTariffHeading(orderQty);; setSimPartidaArancelaria(null); setSimPartidaArancelaria(product.getSimPartidaArancelaria());
		  orderLine.setSimPartidaArancelaria(product.getSimPartidaArancelaria());
		  orderLine.setSimTlcDiscAdvalorem(product.getSIMTLCDiscountADdvalorem());
		  orderLine.setSimAdvalorem(product.getSIMAdValorem());
		  orderLine.setSimOrderimportline(simOrderLines);
		  OBDal.getInstance().save(orderLine);
		  return orderLine;
	} catch (Exception e) {
		// TODO: handle exception
		return null;
	}
    	
	  
  }
  
  //private Order createOrder(VariablesSecureApp vars){
	  private int createOrder(VariablesSecureApp vars){
	  OBError myMessage = null;
	  Client cliente = OBDal.getInstance().get(Client.class,strClientId);
      Organization org =  OBDal.getInstance().get(Organization.class, strOrgId);
      Warehouse warehouse  = OBDal.getInstance().get(Warehouse.class, strWarehouseId);
      BusinessPartner bPartner  = OBDal.getInstance().get(BusinessPartner.class, strBpartnerId);
      Location location  = OBDal.getInstance().get(Location.class, strBpartnerLocationId);
      Currency currency  = OBDal.getInstance().get(Currency.class, strCurrencyId);
      Country country = OBDal.getInstance().get(Country.class, strCountryId);
      Incoterms incoterms = OBDal.getInstance().get(Incoterms.class, strIncotermsId);
      SimOrderImport sim_import = OBDal.getInstance().get(SimOrderImport.class, strOrderImportID);
      
      OrganizationStructureProvider osp = OBContext.getOBContext().getOrganizationStructureProvider(
    		  cliente.getId());
      
      OBCriteria<DocumentType> P_doctype_c = OBDal.getInstance().createCriteria(DocumentType.class);
	  P_doctype_c.add(Restrictions.eq(DocumentType.PROPERTY_SCOSPECIALDOCTYPE, "SIMPARTIAL"));
	  //P_doctype_c.add(Restrictions.eq(DocumentType.PROPERTY_ORGANIZATION, org));
	  
	  P_doctype_c.add(Restrictions.in("organization.id", osp.getParentTree(org.getId(), true)));
	    
	  
	  DocumentType P_doc_type_c = null;
	  try {
		//  DocumentType P_doc_type_c;
		  P_doc_type_c = (DocumentType) P_doctype_c.uniqueResult();
		  if (P_doc_type_c == null) { 
			  return 1;//no hay doc type 
		}
	  } catch (Exception e) {
		return 4;//There are some DocType whit the same information
	  }

	try {
		
		 Long contador = sim_import.getPartialcount() + 1;
		 String documentno = sim_import.getDocumentNo() + "-" + contador.toString();

		  order = OBProvider.getInstance().get(Order.class);
		  order.setClient(cliente);
		  order.setOrganization(org);
		  order.setUpdated(now);
		  order.setSalesTransaction(false);
		  order.setDocumentStatus("DR");
		  order.setDocumentAction("CO");
		  order.setTransactionDocument(P_doc_type_c);
		  order.setDocumentType(P_doc_type_c);
		  order.setDocumentNo(documentno);
		  
		  order.setOrderDate(sim_import.getOrderDate());
		  order.setDatePrinted(sim_import.getDatePrinted());
		  order.setScheduledDeliveryDate(sim_import.getScheduledDeliveryDate());
		  order.setBusinessPartner(bPartner);
		  order.setPartnerAddress(location);
		  order.setInvoiceAddress(location);
		  order.setPrintDiscount(false);
		  order.setCurrency(currency);
		  
		  order.setDeliveryMethod("D");
		////  System.out.println(incoterms);
		  order.setIncoterms(sim_import.getIncoterms());
          order.setSimOrderimport(sim_import);
		  order.setAccountingDate(now);
		  order.setPaymentTerms(sim_import.getPaymentTerms());
		  order.setPaymentMethod(sim_import.getPaymentMethod());
		  order.setWarehouse(sim_import.getWarehouse());
		  order.setPriceList(sim_import.getPriceList());
		  order.setInvoiceTerms("I");
		  order.setSimIsImport(true);
		  order.setSimCCountry(country);
		  order.setSimForwarderBpartner(sim_import.getForwarderBpartner());
		  
		  
		  sim_import.setPartialcount(contador);
		  OBDal.getInstance().save(order);
		  return 0;
		
	} catch (Exception e) {
		// TODO: handle exception
		return 1;
	}
	 
	  
  }

  private OBError copyLines(VariablesSecureApp vars, String strRownums, String strKey)
      throws IOException, ServletException {
	OBError myError = null;
    return myError;
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
                                  String strWindowId,
                                  String strTabId, 
                                  String strOrderImportID,
                                  String strBpartner, 
                                  String strBpartnerLocation,
                                  String strCurrencyId,
                                  String strCountryId) throws IOException, ServletException {
	   
	  GeneratePartialLinesData[] data = null;
	  GeneratePartialLinesData[] dataLines = null;
	    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
		        "pe/com/unifiedgo/imports/ad_actionButton/GeneratePartialLines").createXmlDocument();

	    OBError myMessage = null;
	      myMessage = new OBError();
	      try {
	        data = GeneratePartialLinesData.select1(this, strOrderImportID);
	        dataLines = GeneratePartialLinesData.select2(this, strOrderImportID);
	      } catch (ServletException ex) {
	        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
	      }
	      
	 //    System.out.println("SIMON"); 
	 //    System.out.println(data);
	 //    data = GeneratePartialLinesData.set();
	 //     System.out.println(data[0].DOCNO);
	     String Docnum =  data[0].DOCNO;
	     String DateOr =  data[0].DATEORDER;
	     String BPartn =  data[0].PARTNER;
	     

	    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
	    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
	    xmlDocument.setParameter("theme", vars.getTheme());
	    xmlDocument.setParameter("windowId", strWindowId);
	    xmlDocument.setParameter("tabId", strTabId);
	  //  xmlDocument.setParameter("DocumentNo", Docnum);
	  //  xmlDocument.setParameter("DateOrdered", DateOr);
	  //  xmlDocument.setParameter("BPartner", BPartn);
	    
	    //System.out.println("PRODUCT NAME "+dataLines[0].rownum);
	    //System.out.println("PRODUCT NAME "+dataLines[1].rownum);
	   // xmlDocument.setData("structure1", data);
	   // xmlDocument.setData("structure2", dataOrder);
	    xmlDocument.setData("structure3", data);
	    xmlDocument.setData("structure4", dataLines);
	    response.setContentType("text/html; charset=UTF-8");
	    PrintWriter out = response.getWriter();
	    out.println(xmlDocument.print());
	    out.close();
  }

  public String getServletInfo() {
    return "Servlet Copy from order";
  } // end of getServletInfo() method
}
