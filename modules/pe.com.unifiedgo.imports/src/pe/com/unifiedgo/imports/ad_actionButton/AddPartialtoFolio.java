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
import java.util.List;
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

import pe.com.unifiedgo.imports.ad_actionButton.AddPartialtoFolio;
import pe.com.unifiedgo.imports.data.SimFolioImport;
import pe.com.unifiedgo.imports.data.SimOrderImport;
import pe.com.unifiedgo.imports.data.SimOrderImportLine;

import java.util.Enumeration;

public class AddPartialtoFolio extends HttpSecureAppServlet {
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
  private String strFolioID = "";
  private String strDuaID = "";
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
 //           Enumeration<String> params = vars.getParameterNames(); while (params.hasMoreElements())
 //   	    { System.out.println(params.nextElement()); }
    	
    	 strWindowId = vars.getStringParameter("inpwindowId");
         strClientId = vars.getStringParameter("inpadClientId");
         strOrgId = vars.getStringParameter("inpadOrgId");
         strTabId = vars.getStringParameter("inpTabId");
         strFolioID = vars.getStringParameter("SIM_Folioimport_ID");
         strDuaID = vars.getStringParameter("inpscoDuaId");
       	 printPageDataSheet(response, vars, 
                     strWindowId, 
                     strTabId 
                     );
    } else if (vars.commandIn("SAVE")) {
    	String[] selectedLines = null;
    	String strLines = null;
    	int temporal = 0;
    	 try {
    		 strLines = vars.getRequiredInStringParameter("inpRownumId",IsIDFilter.instance);
    		 strLines = strLines.replace("(", "");
    		 strLines = strLines.replace(")", "");
    		 strLines = strLines.replace("'", "");
             selectedLines = strLines.split(",");
           } catch (Exception e) {
           	temporal=1;
     	    OBError myError;
   	        myError = new OBError();
   	        myError.setType("Error");
   	        myError.setMessage(OBMessageUtils.messageBD(this, "SWA_noselected", vars.getLanguage()));
   	        vars.setMessage(strTabId, myError);
           }
    	 
    	 int validation=0;
		 if(selectedLines != null && 1 != selectedLines.length)
			 validation = validatecurrencyselected(selectedLines);
		 if(validation==1){ //SELECTED Currency different 
			 temporal=1;
	     	 OBError myError;
	   	     myError = new OBError();
	   	     myError.setType("Error");
	   	     myError.setMessage(OBMessageUtils.messageBD(this, "sim_selectedcurrencydiferent", vars.getLanguage()));
	   	     vars.setMessage(strTabId, myError); 
		 }else{
			 validation = validateCurrencySelectedWithPartialExisting(selectedLines);
			 if(validation==1){ //SELECTED Currency different With Partial Existing 
				 temporal=1;
		     	 OBError myError;
		   	     myError = new OBError();
		   	     myError.setType("Error");
		   	     myError.setMessage(OBMessageUtils.messageBD(this, "sim_currencydiferentwithexisting", vars.getLanguage()));
		   	     vars.setMessage(strTabId, myError); 
			 }
		 }
		 
    	 if(temporal == 0) {
    		 try{
   	    		for(int i = 0; selectedLines != null && i < selectedLines.length; i++){
   	    			final int total = AddPartialtoFolioData.updatePartialWithFolio(this, selectedLines[i].trim(), strFolioID);
   	        	}
   	    		final int update = AddPartialtoFolioData.updateCurrencyToFolio(this, strCurrencyId, strFolioID);
   	    		//UpdateFieldinFolio(); -- Actualiza Los pesos, el Volumen y Numero de Bultos segun configurados en los campos del Producto
   	    	    OBError myError;
    	        myError = new OBError();
    	        myError.setType("Success");
    	        myError.setTitle(OBMessageUtils.messageBD("Success"));
    	        vars.setMessage(strTabId, myError);
			} catch (Exception e) {
				System.out.println();
				 OBError myError;
	    	        myError = new OBError();
	    	        myError.setType("Error");
	    	        myError.setTitle(e.getMessage());
	    	        vars.setMessage(strTabId, myError);
			}
    		 
    	 }
    	 
    	 String strWindowPath = Utility.getTabURL(strTabId, "R", true, getDireccion());
         if (strWindowPath.equals(""))
             strWindowPath = strDefaultServlet;
         //vars.setMessage(strTabId, myError);
         printPageClosePopUp(response, vars, strWindowPath);
    	 
    } 
  }
    
  private int validateCurrencySelectedWithPartialExisting(String[] selectedLines){
	  int val=0;
	  String Currency_id="";
	  Order order = OBDal.getInstance().get(Order.class, selectedLines[0].trim());
	  Currency_id = order.getCurrency().getId(); //Currency Selected
	  strCurrencyId = Currency_id;
	  SimFolioImport folio = OBDal.getInstance().get(SimFolioImport.class, strFolioID);
	  if(folio.getCurrency() != null && Currency_id != folio.getCurrency().getId())
		   return 1;
	  return 0;
  }
  
  private int validatecurrencyselected(String[] selectedLines){
	  int val=0;
	  String Currency_id="";
	  Order order = OBDal.getInstance().get(Order.class, selectedLines[0].trim());
	  Currency_id = order.getCurrency().getId();
	  try{
		  for(int i = 1; selectedLines != null && i < selectedLines.length; i++){
				order = OBDal.getInstance().get(Order.class, selectedLines[i].trim());
				if(Currency_id != order.getCurrency().getId())
					return 1;
		  }  
	  }catch (Exception e) {
          return 1;
	  }
	  return 0;
  }
  
  private void UpdateFieldinFolio(){
	  double WeightTotal = 0;
	  double VolumenTotal = 0;
	  double NumBoxes = 0;
	  SimFolioImport folio = OBDal.getInstance().get(SimFolioImport.class, strFolioID);
	  OBCriteria<Order> OrderWithFolio = OBDal.getInstance().createCriteria(Order.class);
	  OrderWithFolio.add(Restrictions.eq(Order.PROPERTY_ORGANIZATION, folio.getOrganization()));
	  OrderWithFolio.add(Restrictions.eq(Order.PROPERTY_CLIENT, folio.getClient()));
	  OrderWithFolio.add(Restrictions.eq(Order.PROPERTY_SIMFOLIOIMPORT, folio));
	  List<Order> OrderList = OrderWithFolio.list();
	  for(int i=0; i<OrderList.size() ; i++){
		  List<OrderLine> orderLines = OrderList.get(i).getOrderLineList();
		  for(int j=0; j < orderLines.size() ; j++){
			  if(orderLines.get(j).getProduct().getWeight() != null)
		      	WeightTotal = WeightTotal + (orderLines.get(j).getProduct().getWeight().doubleValue()*orderLines.get(j).getOrderedQuantity().doubleValue());
			  if(orderLines.get(j).getProduct().getScrUnitsperbox() != null)
			    NumBoxes = NumBoxes + (orderLines.get(j).getOrderedQuantity().doubleValue()/orderLines.get(j).getProduct().getScrUnitsperbox().doubleValue());
			  if(orderLines.get(j).getProduct().getVolume().doubleValue() != 0  && orderLines.get(j).getProduct().getScrUnitsperbox().doubleValue() != 0)
				  VolumenTotal = VolumenTotal + (orderLines.get(j).getProduct().getVolume().doubleValue()*orderLines.get(j).getOrderedQuantity().doubleValue()/orderLines.get(j).getProduct().getScrUnitsperbox().doubleValue()); 
		  }
	  }
   		folio.setTotalNumCajas(new BigDecimal(NumBoxes));
   		folio.setTotalPeso(new BigDecimal(WeightTotal));
   		folio.setTotalCubicaje(new BigDecimal(VolumenTotal));
   		OBDal.getInstance().save(folio);
  }
 
    
  
  private OrderLine createOrderLine(VariablesSecureApp vars, SimOrderImport simOrder, SimOrderImportLine simOrderLines, Order order, String orderQty, int band) {
	  
	  BusinessPartner bPartner  = OBDal.getInstance().get(BusinessPartner.class, strBpartnerId);
	  //OrderLine orderLine = null;
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
                                  String strTabId) throws IOException, ServletException {
	   
	  
	  AddPartialtoFolioData[] data = null;
	  AddPartialtoFolioData[] dataLines = null;
	    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
		        "pe/com/unifiedgo/imports/ad_actionButton/AddPartialtoFolio").createXmlDocument();

	    OBError myMessage = null;
	      myMessage = new OBError();
	      try {
	        data = AddPartialtoFolioData.select3(this,strOrgId );
	    //    dataLines = AddPartialtoFolioData.select2(this, strOrderImportID);
	      } catch (ServletException ex) {
	        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
	      }
	      
	
        xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
	    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
	    xmlDocument.setParameter("theme", vars.getTheme());
	    xmlDocument.setParameter("windowId", strWindowId);
	    xmlDocument.setParameter("tabId", strTabId);
	 
	    xmlDocument.setData("structure5", data);
	    //xmlDocument.setData("structure4", dataLines);
	    response.setContentType("text/html; charset=UTF-8");
	    PrintWriter out = response.getWriter();
	    out.println(xmlDocument.print());
	    out.close();
  }

  public String getServletInfo() {
    return "Servlet Copy from order";
  } // end of getServletInfo() method
}
