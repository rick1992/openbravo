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
import org.openbravo.model.materialmgmt.transaction.InOutLineAccountingDimension;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
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
import org.openbravo.model.common.uom.UOM;
import org.openbravo.utils.Replace;
import org.openbravo.xmlEngine.XmlDocument;

import com.thoughtworks.xstream.converters.basic.BigDecimalConverter;

import pe.com.unifiedgo.warehouse.ad_actionButton.CreateLinesFromReposition;
import pe.com.unifiedgo.warehouse.data.SwaRequerimientoReposicion;
import pe.com.unifiedgo.warehouse.data.SwaRequerimientoReposicionDetail;
import pe.com.unifiedgo.warehouse.data.swaRequirementOrderTrace;
import pe.com.unifiedgo.imports.data.SimOrderImport;
import pe.com.unifiedgo.imports.data.SimOrderImportLine;

import java.util.Enumeration;


public class CreateLinesFromReposition extends HttpSecureAppServlet {
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
     	 String strGoodsShipment = vars.getStringParameter("inpOrderRequisition");
     	 
     	 vars.setSessionValue("strReceiptRequirementId", strReceiptRequirementId.trim());
       	 printPageDataSheet(response, vars,strWindowId,strTabId,strGoodsShipment );
    } 
    else if (vars.commandIn("SEARCH")){
    	
    	String strWarehouseCatch = vars.getStringParameter("inptoMWarehouseId");
    	String strGoodsShipment = vars.getStringParameter("inpOrderRequisition");
   	    printPageDataSheet(response, vars, strWindowId, strTabId,strGoodsShipment);
    	
    }
    else if (vars.commandIn("SAVE")) {
    	String[] selectedLines = null;
    	String strRepositionLineID = null;
    	int tmp=0;
    	//Order order = null;
    	//String strOrderTransferencia = vars.getStringParameter("inpOrderRequisition");
    	String strGoodsShipment = vars.getStringParameter("inpOrderRequisition");
    	String receiptReposition = vars.getSessionValue("strReceiptRequirementId");
    	//String strGoodShipment  =  vars.getSessionValue("strReceiptRequirementId");
    	String strOrderTransferencia =  "";
    	
    	Connection conn = null;
    	  
        String strWindowPath = Utility.getTabURL(strTabId, "R", true, getDireccion());
        if (strWindowPath.equals(""))
            strWindowPath = strDefaultServlet;
        
        ShipmentInOut shipment = OBDal.getInstance().get(ShipmentInOut.class, strGoodsShipment.trim());
        if(shipment != null){
        	strOrderTransferencia = shipment.getSwaRequireposicion()==null?"":shipment.getSwaRequireposicion().getId();
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
            myError = insertLinesFromOrderReceipt(vars ,receiptReposition,  strOrderTransferencia , strGoodsShipment,  conn );
            vars.setMessage(strTabId, myError);
        printPageClosePopUp(response, vars, strWindowPath);
    } 
  }
    
  private OBError insertLinesFromOrderReceipt(VariablesSecureApp vars, String strReceiptReposition, String strOrderReceipt, String strGoodsShipmentId, Connection conn) {
  	  OBError myMessage;
  	  myMessage = new OBError();
  	  myMessage.setType("Success");
  	  myMessage.setTitle(OBMessageUtils.messageBD("Success"));
        //Valores Iniciales
        Long lines=0L;
        try {
      	  conn = getTransactionConnection();
            SwaRequerimientoReposicion receiptOrder = OBDal.getInstance().get(SwaRequerimientoReposicion.class, strReceiptReposition.trim());
            SwaRequerimientoReposicion repositionOrder = OBDal.getInstance().get(SwaRequerimientoReposicion.class, strOrderReceipt.trim());
            CreateLinesFromRepositionData.deleteLinesReceiptReposition(conn, this, receiptOrder.getId());
            BigDecimal qtytoInsert = BigDecimal.ZERO;
            ShipmentInOut shipment = OBDal.getInstance().get(ShipmentInOut.class, strGoodsShipmentId);
            //List<ShipmentInOutLine> shipmentLines = shipment.getMaterialMgmtShipmentInOutLineList();
            CreateLinesFromRepositionData[] data = null;
            data = CreateLinesFromRepositionData.selectShipmentLineFromReposition(this,strGoodsShipmentId);
            for(int i=0; i<data.length ; i++){
            /*	System.out.println(data[i].productid);
            	System.out.println(data[i].productvalue);
            	System.out.println(data[i].productname);
            	System.out.println(data[i].uomname);
            	System.out.println(data[i].repodetailid);
            	System.out.println(data[i].movementqty);*/
            	
            	Product product = null;
            	UOM uom = null;
            	SwaRequerimientoReposicionDetail orderRepoDetail =  OBDal.getInstance().get(SwaRequerimientoReposicionDetail.class, data[i].repodetailid.trim());
            	if(orderRepoDetail == null)
            		product = OBDal.getInstance().get(Product.class, data[i].productid.trim());
            	else
            		product = orderRepoDetail.getProduct();
            	uom = product.getUOM();
            	//Si transfiero mill?
            	BigDecimal movementqty = new BigDecimal(data[i].movementqty);
            	
            	lines = lines+ 10;
   	    	    SwaRequerimientoReposicionDetail receiptlines = OBProvider.getInstance().get(SwaRequerimientoReposicionDetail.class);
   	    	    receiptlines.setOrganization(receiptOrder.getOrganization());
   	    	    receiptlines.setClient(receiptOrder.getClient());
   	    	    receiptlines.setLineNo(lines);
   	    	    receiptlines.setProduct(product);
   	    	    receiptlines.setUOM(uom);
   	    	    receiptlines.setRequerepoFromLine(orderRepoDetail);//En la Linea de Receipcion de transferencia se pone como referencia la Linea de Orden de referencia
   	    	    receiptlines.setOrderedQuantity(movementqty);
   	    	    receiptlines.setSWARequerimientoreposicion(receiptOrder);
   	    	    OBDal.getInstance().save(receiptlines);
            }
            
           // CreateLinesFromRepositionData.updateOrderRepositionInReceipt(conn, this, repositionOrder.getId());
      	    
           /* OBContext.setAdminMode(false);
            try {
            	repositionOrder.setGeneratetrx(true);
            	OBDal.getInstance().save(repositionOrder);
                OBDal.getInstance().flush();
                
       
			} catch (Exception e) {
				e.printStackTrace();
				// TODO: handle exception
			}finally{
				OBContext.restorePreviousMode();
			}*/
            
            /*SwaRequerimientoReposicion orderReposition = OBDal.getInstance().get(SwaRequerimientoReposicion.class, repositionOrder.getId());
            orderReposition.setGeneratetrx(true);
            OBDal.getInstance().save(orderReposition);*/
            
            CreateLinesFromRepositionData.updateReceiptReposition(conn, this, receiptOrder.getId(), repositionOrder.getId());
      	    
      	  releaseCommitConnection(conn);
  	  } catch (OBException ob) {
  	      OBDal.getInstance().rollbackAndClose();
  	      try {
  	        if (conn != null)
  	          releaseRollbackConnection(conn);
  	      } catch (Exception ignored) {
  	      }

  	      myMessage.setType("Error");
  	      myMessage.setMessage(Utility.messageBD(this, ob.getMessage(), vars.getLanguage()));
  	      return myMessage;
  	    } catch (Exception e) {
  	      OBDal.getInstance().rollbackAndClose();
  	      try {
  	        if (conn != null)
  	          releaseRollbackConnection(conn);
  	      } catch (Exception ignored) {
  	      }
  	      myMessage.setType("Error");
  	      myMessage.setMessage(Utility.messageBD(this, "ProcessRunError", vars.getLanguage()));
  	      return myMessage;
  	      // TODO: handle exception
  	    }
       //System.out.println("RECORD");
      return myMessage;
   }
    
     
 private OBError insertLinesFromOrderReceiptLines(VariablesSecureApp vars, String[]fromOrderReceiptLines,Connection conn) {
	  OBError myMessage;
	  myMessage = new OBError();
	  myMessage.setType("Success");
	  myMessage.setTitle(OBMessageUtils.messageBD("Success"));
      //Valores Iniciales
      Long lines=0L;
      Warehouse towarehouse_returnSample = null;
      
      try {
    	  conn = getTransactionConnection();
    	  String receiptReposition = vars.getSessionValue("strReceiptRequirementId");
          SwaRequerimientoReposicion receiptOrder = OBDal.getInstance().get(SwaRequerimientoReposicion.class, receiptReposition.trim());
          SwaRequerimientoReposicion repositionOrder = null;
          //si hay Lineas Asociadas a la Orden de Recepcion Entonces antes de insertar vamos a borrarlas
          /*List<SwaRequerimientoReposicionDetail> repolines = receiptOrder.getSwaRequerepoDetailList();
          for(SwaRequerimientoReposicionDetail repodetail : repolines){
        	  OBDal.getInstance().remove(repodetail);
          }*/
          
          CreateLinesFromRepositionData.deleteLinesReceiptReposition(conn, this, receiptOrder.getId());
          
         // repolines.clear();
          //OBDal.getInstance().flush();
          
          BigDecimal qtytoInsert = BigDecimal.ZERO;
          
          for(int i = 0; fromOrderReceiptLines != null && i < fromOrderReceiptLines.length; i++) {
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
        		  towarehouse_returnSample = fromOrderLineReceipt.getSWARequerimientoreposicion().getMWarehouse();
          }
          CreateLinesFromRepositionData.updateOrderRepositionInReceipt(conn, this, repositionOrder.getId());
          CreateLinesFromRepositionData.updateReceiptReposition(conn, this, receiptOrder.getId(), repositionOrder.getId());
    	  
    	  releaseCommitConnection(conn);
	  } catch (OBException ob) {
	      OBDal.getInstance().rollbackAndClose();
	      try {
	        if (conn != null)
	          releaseRollbackConnection(conn);
	      } catch (Exception ignored) {
	      }

	      myMessage.setType("Error");
	      myMessage.setMessage(Utility.messageBD(this, ob.getMessage(), vars.getLanguage()));
	      return myMessage;
	    } catch (Exception e) {
	      OBDal.getInstance().rollbackAndClose();
	      try {
	        if (conn != null)
	          releaseRollbackConnection(conn);
	      } catch (Exception ignored) {
	      }
	      myMessage.setType("Error");
	      myMessage.setMessage(Utility.messageBD(this, "ProcessRunError", vars.getLanguage()));
	      return myMessage;
	      // TODO: handle exception
	    }
     //System.out.println("RECORD");
    return myMessage;
 }
  
 
 
  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
                                  String strWindowId,
                                  String strTabId,
                                  String strGoodsShipment
                                  ) throws IOException, ServletException {
	   
	  CreateLinesFromRepositionData[] data = null;
	  CreateLinesFromRepositionData[] dataLines = null;
	    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
		        "pe/com/unifiedgo/warehouse/ad_actionButton/CreateLinesFromReposition").createXmlDocument();

	      OBError myMessage = null;
	      myMessage = new OBError();
	      
	      boolean isfromSample = false; //Para indicar si la recepción se hará de una muestra
	      
	      //Orden de Recepción
	      String receiptReposition = vars.getSessionValue("strReceiptRequirementId");
	      SwaRequerimientoReposicion orderReposition = OBDal.getInstance().get(SwaRequerimientoReposicion.class, receiptReposition.trim());
	     
	      
	     
	      String adOrgId = orderReposition.getOrganization().getId();
	      	      
	      try{
	    	  if(isfromSample)
	    	    data = CreateLinesFromRepositionData.selectReposition_SamplePending(this,strGoodsShipment);
	    	  else
	    		//data = CreateLinesFromRepositionData.selectReposition_OrderPending(this,strGoodsShipment);
	    		  data = CreateLinesFromRepositionData.selectShipmentLine(this, strGoodsShipment);
	      }catch(ServletException ex){
	    	  myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
	      }
	      
	   
	    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
	    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
	    xmlDocument.setParameter("theme", vars.getTheme());
	    xmlDocument.setParameter("windowId", strWindowId);
	    xmlDocument.setParameter("tabId", strTabId);
        xmlDocument.setData("structure6", data);

	    
        
          String strTowarehouseId = "";
  	      if(orderReposition.getMWarehouse()!= null)
  	    	  strTowarehouseId = orderReposition.getMWarehouse().getId();
          xmlDocument.setData("reportOrderRequisition", "liststructure", CreateLinesFromRepositionData.selectReposition_RepositionOrder(this, strTowarehouseId));	
        
        
        xmlDocument.setParameter("OrderReposition",strGoodsShipment);
	      
	    response.setContentType("text/html; charset=UTF-8");
	    PrintWriter out = response.getWriter();
	    out.println(xmlDocument.print());
	    out.close();
  }

  public String getServletInfo() {
    return "Servlet Copy from order";
  } // end of getServletInfo() method
}
