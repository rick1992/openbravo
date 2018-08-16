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
package pe.com.unifiedgo.warehouse.ad_actionButton;

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
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.geography.Country;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.plm.Product;
import org.openbravo.utils.Replace;
import org.openbravo.xmlEngine.XmlDocument;

import com.thoughtworks.xstream.converters.basic.BigDecimalConverter;

import pe.com.unifiedgo.warehouse.ad_actionButton.CreateLinesFromRepositionSample;
import pe.com.unifiedgo.warehouse.data.SwaRequerimientoReposicion;
import pe.com.unifiedgo.warehouse.data.SwaRequerimientoReposicionDetail;
import pe.com.unifiedgo.warehouse.data.swaRequirementOrderTrace;
import pe.com.unifiedgo.imports.data.SimOrderImport;
import pe.com.unifiedgo.imports.data.SimOrderImportLine;

import java.util.Enumeration;


public class CreateLinesFromRepositionSample extends HttpSecureAppServlet {
  final private Date now = DateUtils.truncate(new Date(), Calendar.DATE);
  private static final long serialVersionUID = 1L;
  private static final BigDecimal ZERO = BigDecimal.ZERO;

 
  private String strClientId= "";
  private String strOrgId= "";

  private String strTabId = "";
  private String strWindowId = "";
  
  private String strMInOutLineId = "";
  private String strMProductId ="";
  private String strQytToDistribute = "";
  private String strWarehouseId= "";
  private String strReceiptRequirementId="";
  
  
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
    	  
	   	 strWindowId = vars.getStringParameter("inpwindowId");
         strClientId = vars.getStringParameter("inpadClientId");
         strOrgId = vars.getStringParameter("inpadOrgId");
         strTabId = vars.getStringParameter("inpTabId");
         strWarehouseId = vars.getStringParameter("inptoMWarehouseId");
         String  strReceiptRequirementId = vars.getStringParameter("inpswaRequerimientoreposicionId");
     	 String strOrderTransferencia = vars.getStringParameter("inpOrderRequisition");
     	 
     	 vars.setSessionValue("strReceiptRequirementId", strReceiptRequirementId.trim());
     	 
     	 
     	 
     	// System.out.println("inpswaRequerimientoreposicionId: " + strReceiptRequirementId);
         
       	 printPageDataSheet(response, vars,strWindowId,strTabId,strOrderTransferencia );
    } 
    else if (vars.commandIn("SEARCH")){
    	
    	String strWarehouseCatch = vars.getStringParameter("inptoMWarehouseId");
    	String strOrderTransferencia = vars.getStringParameter("inpOrderRequisition");
    	
    	
   	    printPageDataSheet(response, vars, strWindowId, strTabId,strOrderTransferencia);
    	
    }
    else if (vars.commandIn("SAVE")) {
    	String[] selectedLines = null;
    	String strRepositionLineID = null;
    	int tmp=0;
    	//Order order = null;
    	String strOrderTransferencia = vars.getStringParameter("inpOrderRequisition");
    	
    	System.out.println("strOrderTransferencia: " + strOrderTransferencia);
    	
    	
    	  
        String strWindowPath = Utility.getTabURL(strTabId, "R", true, getDireccion());
        if (strWindowPath.equals(""))
            strWindowPath = strDefaultServlet;

        
        try {
        	strRepositionLineID = vars.getRequiredInStringParameter("inpRownumId",IsIDFilter.instance);
        	strRepositionLineID = strRepositionLineID.replace("(", "");
        	strRepositionLineID = strRepositionLineID.replace(")", "");
        	strRepositionLineID = strRepositionLineID.replace("'", "");
            selectedLines = strRepositionLineID.split(",");
            
        } catch (Exception e) {
   	        OBError myError;
  	        myError = new OBError();
  	        myError.setType("Error");
  	        myError.setMessage(OBMessageUtils.messageBD(this, "SWA_noselected", vars.getLanguage()));
  	        vars.setMessage(strTabId, myError);
  	        printPageClosePopUp(response, vars, strWindowPath);
        }
        
        if(strOrderTransferencia==null || strOrderTransferencia.equals("")){
    		OBError myError;
	    	myError = new OBError();
	    	myError.setType("Error");
	    	myError.setTitle(OBMessageUtils.messageBD("Error"));
	    	myError.setMessage(OBMessageUtils.messageBD("swa_reposition_invalidOrderReposition"));
	    	vars.setMessage(strTabId, myError);
	    	    
	    	printPageClosePopUp(response, vars, strWindowPath);
    	}
        
        
        	OBError myError;
            myError = new OBError();
            myError = insertLinesFromOrderReceiptLines(vars , selectedLines );
            vars.setMessage(strTabId, myError);
            
        printPageClosePopUp(response, vars, strWindowPath);
    } 
  }
    
     
 private OBError insertLinesFromOrderReceiptLines(VariablesSecureApp vars, String[]fromOrderReceiptLines ) {
	  OBError myError;
      myError = new OBError();
      myError.setType("Success");
      myError.setTitle(OBMessageUtils.messageBD("Success"));
      //Valores Iniciales
      Long lines=0L;
      Warehouse towarehouse_returnSample = null;
      
      String receiptReposition = vars.getSessionValue("strReceiptRequirementId");
      System.out.println("receiptReposition: " + receiptReposition);
      SwaRequerimientoReposicion receiptOrder = OBDal.getInstance().get(SwaRequerimientoReposicion.class, receiptReposition.trim());
      SwaRequerimientoReposicion repositionOrder = null;
      //si hay Lineas Asociadas a la Orden de Recepcion Entonces antes de insertar vamos a borrarlas
      List<SwaRequerimientoReposicionDetail> repolines = receiptOrder.getSwaRequerepoDetailList();
      for(SwaRequerimientoReposicionDetail repodetail : repolines){
    	  OBDal.getInstance().remove(repodetail);
      }
      
      repolines.clear();
      OBDal.getInstance().flush();
      
      /*for(int i=0; i< receiptOrder.getSwaRequerepoDetailList().size() ;i++){
    	  OBDal.getInstance().remove(receiptOrder.getSwaRequerepoDetailList().get(i));
      }*/
      
      BigDecimal qtytoInsert = BigDecimal.ZERO;
      
      for(int i = 0; fromOrderReceiptLines != null && i < fromOrderReceiptLines.length; i++) {
	      try{
	    	  //Se obtiene la Linea desde donde se van a copiar los datos
	    	  SwaRequerimientoReposicionDetail fromOrderLineReceipt = OBDal.getInstance().get(SwaRequerimientoReposicionDetail.class, fromOrderReceiptLines[i].trim());
	    	  
	    	  //Se pondra en la Cabecera cuando se haga una Recepcion de Transferencia, esto para poder
	    	  //dar seguimiento 
	    	  repositionOrder = fromOrderLineReceipt.getSWARequerimientoreposicion();
	    	  String qtyInsert = vars.getNumericParameter("inpReturnOrderQty" + fromOrderReceiptLines[i].trim());
	    	  qtytoInsert = new BigDecimal(qtyInsert);
	    	  
	    	  lines = lines+ 10;
	    	  SwaRequerimientoReposicionDetail receiptlines = OBProvider.getInstance().get(SwaRequerimientoReposicionDetail.class);
	    	  receiptlines.setOrganization(receiptOrder.getOrganization());
	    	  receiptlines.setClient(receiptOrder.getClient());
	    	  receiptlines.setLineNo(lines);
	    	  receiptlines.setProduct(fromOrderLineReceipt.getProduct());
	    	  receiptlines.setUOM(fromOrderLineReceipt.getUOM());
	    	  receiptlines.setRequerepoFromLine(fromOrderLineReceipt);//En la Linea de Receipcion de transferencia se pone como referencia la Linea de Orden de referencia
	    	  receiptlines.setOrderedQuantity(qtytoInsert);
	    	  receiptlines.setSWARequerimientoreposicion(receiptOrder);
	    	  OBDal.getInstance().save(receiptlines);
    		  OBDal.getInstance().flush();
    		  towarehouse_returnSample = fromOrderLineReceipt.getSWARequerimientoreposicion().getMWarehouse();
	      }catch(Exception e){
	    	  OBDal.getInstance().rollbackAndClose();
	          myError = new OBError();
	          myError.setType("Error");
	          myError.setTitle(OBMessageUtils.messageBD("Error"));
	      }
      }
      
      //Temporal, a la orden de recepcion pondremos el almacèn de Muestras con que salio
      if(receiptOrder.getMWarehouse() == null  && towarehouse_returnSample  !=null){
    	  receiptOrder.setMWarehouse(towarehouse_returnSample);
      }
      if(receiptOrder.getOrganization().getId().equals(repositionOrder.getOrganization().getId()))
	    receiptOrder.setSWAFromrequerimiento(repositionOrder);
	  OBDal.getInstance().save(receiptOrder);
	  
	  

      
    return myError;
 }
  
 
 
  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
                                  String strWindowId,
                                  String strTabId,
                                  String strFromOrderReposition
                                  ) throws IOException, ServletException {
	   
	  CreateLinesFromRepositionSampleData[] data = null;
	  CreateLinesFromRepositionSampleData[] dataLines = null;
	    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
		        "pe/com/unifiedgo/warehouse/ad_actionButton/CreateLinesFromRepositionSample").createXmlDocument();

	      OBError myMessage = null;
	      myMessage = new OBError();
	      
	      boolean isfromSample = false; //Para indicar si la recepción se hará de una muestra
	      
	      //Orden de Recepción
	      String receiptReposition = vars.getSessionValue("strReceiptRequirementId");
	      SwaRequerimientoReposicion orderReposition = OBDal.getInstance().get(SwaRequerimientoReposicion.class, receiptReposition.trim());
	      if(orderReposition.getDocumentType().getScoSpecialdoctype().equals("SWARETURNREQUESTSAMPLE")){
	    	  isfromSample=true;
	      }
	     
	      String adOrgId = orderReposition.getOrganization().getId();
	      	      
	      try{
	    	  if(isfromSample)
	    	    data = CreateLinesFromRepositionSampleData.selectReposition_SamplePending(this,strFromOrderReposition);
	    	  else
	    		data = CreateLinesFromRepositionSampleData.selectReposition_OrderPending(this,strFromOrderReposition);
	      }catch(ServletException ex){
	    	  myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
	      }
	      
	   
	    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
	    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
	    xmlDocument.setParameter("theme", vars.getTheme());
	    xmlDocument.setParameter("windowId", strWindowId);
	    xmlDocument.setParameter("tabId", strTabId);
        xmlDocument.setData("structure6", data);

	    
        if(isfromSample){ //Vamos a Obtener las Muestras Pendientes de Devolución para un Encargado determinado
          String respSample = "";
   	      if(orderReposition.getEncargadoDeMuestra() != null)
   	    	  respSample = orderReposition.getEncargadoDeMuestra().getId();
   	      String strcBPartnerId = orderReposition.getBusinessPartner().getId();	
	      xmlDocument.setData("reportOrderRequisition", "liststructure", CreateLinesFromRepositionSampleData.selectReposition_SampleOrder(this, adOrgId, strcBPartnerId,respSample ));
        }
        else{ //Vamos a Obtener las Ordenes de Transferencia Pendientes de Recepción
          String strTowarehouseId = "";
  	      if(orderReposition.getMWarehouse()!= null)
  	    	  strTowarehouseId = orderReposition.getMWarehouse().getId();
          xmlDocument.setData("reportOrderRequisition", "liststructure", CreateLinesFromRepositionSampleData.selectReposition_RepositionOrder(this, strTowarehouseId));	
        }
        
        xmlDocument.setParameter("OrderReposition",strFromOrderReposition);
	      
	    response.setContentType("text/html; charset=UTF-8");
	    PrintWriter out = response.getWriter();
	    out.println(xmlDocument.print());
	    out.close();
  }

  public String getServletInfo() {
    return "Servlet Copy from order";
  } // end of getServletInfo() method
}
