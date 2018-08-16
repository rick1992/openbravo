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
import org.openbravo.model.financialmgmt.payment.PaymentTerm;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.plm.Product;
import org.openbravo.utils.Replace;
import org.openbravo.xmlEngine.XmlDocument;

import com.thoughtworks.xstream.converters.basic.BigDecimalConverter;

import pe.com.unifiedgo.imports.ad_actionButton.AddFromOrder;
import pe.com.unifiedgo.imports.data.SimOrderImport;
import pe.com.unifiedgo.imports.data.SimOrderImportLine;

import java.util.Enumeration;

public class AddFromOrder extends HttpSecureAppServlet {
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
  private String strc_orderID = "";
  private String lineupdate = "";
  long lineNo;
  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }
  
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
    ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    Order Order = null;
    OBError myMessage = null;
    myMessage = new OBError();
    myMessage.setTitle("");
    if (vars.commandIn("DEFAULT")) {
    
    	try {
    		  strWindowId = vars.getStringParameter("inpwindowId");
    	         strClientId = vars.getStringParameter("inpadClientId");
    	         strOrgId = vars.getStringParameter("inpadOrgId");
    	         strWarehouseId = vars.getStringParameter("inpmWarehouseId"); 
    	         strPaymenTerm = vars.getStringParameter("inpcPaymenttermId");
    	         strFinPaymenTerm = vars.getStringParameter("inpfinPaymentmethodId");
    	         
    	         
    	    	 strOrderImportID = vars.getStringParameter("inpemSimOrderimportId");
    	    	 strBpartnerId = vars.getStringParameter("inpcBpartnerId");
    	    	 strBpartnerLocationId = vars.getStringParameter("inpcBpartnerLocationId");
    	    	 strIncotermsId = vars.getStringParameter("inpcIncotermsId");
    	    	 strtypepurchaseorder = vars.getStringParameter("inptypepurchaseorder"); // NO
    	    	 strCurrencyId = vars.getStringParameter("inpcCurrencyId");
    	    	 strCountryId = vars.getStringParameter("inpcCountryId");  //NO
    	    	 strTabId = vars.getStringParameter("inpTabId");
    	    	 strc_orderID = vars.getStringParameter("inpcOrderId");
    	    	 
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
    	                     strCurrencyId
    	                     );
    	         }
		} catch (Exception e) {
			OBError myError;
            myError = new OBError();
            myError.setType("Warning");
            myError.setTitle(OBMessageUtils.messageBD("Warning"));
            myError.setMessage(OBMessageUtils.messageBD("Sim_pressButtonOkUpdate"));
			
			
			String strWindowPath = Utility.getTabURL(strTabId, "R", true);
            if (strWindowPath.equals(""))
                strWindowPath = strDefaultServlet;
            vars.setMessage(strTabId, myError);
            printPageClosePopUp(response, vars, strWindowPath);
		}
    } else if (vars.commandIn("SAVE")) {
    	String[] selectedLines = null;
    	String strProductID = null;
    	String orderQty= null;
    	String v_orderQty= null;
    	int band;
    	Order order = null;
    	OrderLine orderLine = null;
    	SimOrderImport sim_import = null;
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
	        myError.setMessage(OBMessageUtils.messageBD(this, "SWA_noselected", vars.getLanguage()));
	        vars.setMessage(strTabId, myError);
        }
        SimOrderImportLine sim_importLines_tmp = null;
        if(temporal==0){//THERE ARE SELECTED ELEMENTS
        	//VALIDATION
        	BigDecimal qtyOrderedBd = BigDecimal.ZERO;
        	try {
        		for(int i = 0; selectedLines != null && i < selectedLines.length; i++) {
	        		 v_orderQty = "";
	              	 v_orderQty = vars.getNumericParameter("inpOrderQty" + selectedLines[i].trim());
	              	 qtyOrderedBd = new BigDecimal(v_orderQty);
	              	//  SimOrderImportLine sim_importLines = null;
	              	sim_importLines_tmp = OBDal.getInstance().get(SimOrderImportLine.class, selectedLines[i].trim());
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
        	// END VALIDATION
        	
        	if(temporal2 == 0){ // NOT EXECPTION - ARRIVAL NUMBER
        		order = OBDal.getInstance().get(Order.class, strc_orderID);
                sim_import = OBDal.getInstance().get(SimOrderImport.class, strOrderImportID);
            	lineNo = 10L;
            	for(int i = 0; selectedLines != null && i < selectedLines.length; i++) {
              	  orderQty = "";
                  orderQty = vars.getNumericParameter("inpOrderQty" + selectedLines[i].trim());
                  SimOrderImportLine sim_importLines = null;
                  sim_importLines = OBDal.getInstance().get(SimOrderImportLine.class, selectedLines[i].trim());
                  
                  BigDecimal c_count;
             	  Query q = OBDal.getInstance().getSession().createSQLQuery("select count(c_orderline_id) from c_orderline where c_order_id = '"  + strc_orderID + "' and em_sim_orderimportline_id = '" +selectedLines[i].trim()+"'");
             	  Query q2 = OBDal.getInstance().getSession().createSQLQuery("select c_orderline_id from c_orderline where c_order_id = '"  + strc_orderID + "' and em_sim_orderimportline_id = '" +selectedLines[i].trim()+"'");
             	  lineupdate = (String)q2.uniqueResult();
             	   if(Float.parseFloat(q.uniqueResult().toString()) > 0){
             		   band = 1;
             	   }else{
             		   band = 0;
             	   }
                  orderLine = createOrderLine(vars,sim_import,sim_importLines, order, orderQty, band);
                 }	
      	        OBError myError;
      	        myError = new OBError();
      	        myError.setType("Success");
      	        myError.setTitle(OBMessageUtils.messageBD("Success"));
      	       // myError.setMessage(OBMessageUtils.messageBD("sim_createPartialsucess"));
      	        vars.setMessage(strTabId, myError);
        	}
        }
        
        String strWindowPath = Utility.getTabURL(strTabId, "R", true);
        if (strWindowPath.equals(""))
            strWindowPath = strDefaultServlet;
        //vars.setMessage(strTabId, myError);
        printPageClosePopUp(response, vars, strWindowPath);
    	
    } 
  }
  
  private OrderLine createOrderLine(VariablesSecureApp vars, SimOrderImport simOrder, SimOrderImportLine simOrderLines, Order order, String orderQty, int band) {
	  
	  BusinessPartner bPartner  = OBDal.getInstance().get(BusinessPartner.class, strBpartnerId);
	  Product product = OBDal.getInstance().get(Product.class, simOrderLines.getProduct().getId());
	  //OrderLine orderLine = null;
	  lineNo = simOrder.getSimOrderimportLineList().size()*10;
	  if(band==0){ //Insert
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
				  orderLine.setLineNetAmount(simOrderLines.getLineNetAmount());
				  orderLine.setDiscount(simOrderLines.getDiscount());
				  orderLine.setTax(simOrderLines.getTax());
				  BigDecimal standarPrice = new BigDecimal(simOrderLines.getStandardPrice());
				  orderLine.setStandardPrice(simOrderLines.getUnitPrice());
				  orderLine.setTaxableAmount(simOrderLines.getLineNetAmount());
				  orderLine.setSimPartidaArancelaria(product.getSimPartidaArancelaria());
				  //orderLine.setSimPartidaArancelaria(product.getSimPartidaArancelaria());
				  orderLine.setSimTlcDiscAdvalorem(product.getSIMTLCDiscountADdvalorem());
				  orderLine.setSimAdvalorem(product.getSIMAdValorem());
				  orderLine.setSimOrderimportline(simOrderLines);
				  OBDal.getInstance().save(orderLine);
				  return orderLine;
			} catch (Exception e) {
				// TODO: handle exception
				return null;
			}
	  }else {  //Update
		  
		  try{
			  OrderLine orderLine  = OBDal.getInstance().get(OrderLine.class,lineupdate);
			  BigDecimal QuantityAnt = orderLine.getOrderedQuantity();
			  BigDecimal Quantity = new BigDecimal(orderQty);
			  
			//  Float qtyf  = orderLine.getOrderedQuantity().floatValue() + Float.parseFloat(orderQty);
			  
		  
		//	  Quantity.add(new BigDecimal(orderQty));
			  orderLine.setSimPartidaArancelaria(product.getSimPartidaArancelaria());
			  orderLine.setSimTlcDiscAdvalorem(product.getSIMTLCDiscountADdvalorem());
			  orderLine.setSimAdvalorem(product.getSIMAdValorem());
			  orderLine.setOrderedQuantity(QuantityAnt.add(Quantity));
			  OBDal.getInstance().save(orderLine);
			  return orderLine;
		  }
		  catch (Exception e) {
				// TODO: handle exception
				return null;
		}
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
                                  String strCurrencyId) throws IOException, ServletException {
	   
	  AddFromOrderData[] data = null;
	  AddFromOrderData[] dataLines = null;
	    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
		        "pe/com/unifiedgo/imports/ad_actionButton/AddFromOrder").createXmlDocument();

	    OBError myMessage = null;
	      myMessage = new OBError();
	      try {
	        data = AddFromOrderData.select1(this, strOrderImportID);
	        dataLines = AddFromOrderData.select2(this, strOrderImportID);
	      } catch (ServletException ex) {
	        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
	      }
	      
	
	     String Docnum =  data[0].DOCNO;
	     String DateOr =  data[0].DATEORDER;
	     String BPartn =  data[0].PARTNER;
	     

	    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
	    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
	    xmlDocument.setParameter("theme", vars.getTheme());
	    xmlDocument.setParameter("windowId", strWindowId);
	    xmlDocument.setParameter("tabId", strTabId);
	 
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
