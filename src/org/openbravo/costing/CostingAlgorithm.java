/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.costing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.costing.CostingServer.TrxType;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.info.SalesOrder;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.financial.FinancialUtils;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.plm.ProductBOM;
import org.openbravo.model.materialmgmt.cost.Costing;
import org.openbravo.model.materialmgmt.cost.CostingRule;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;
import org.openbravo.model.materialmgmt.transaction.ProductionLine;
import org.openbravo.model.materialmgmt.transaction.ProductionPlan;
import org.openbravo.model.materialmgmt.transaction.ProductionTransaction;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.model.pricing.pricelist.ProductPrice;
import org.openbravo.service.db.CallStoredProcedure;

import pe.com.unifiedgo.imports.data.SimFolioImport;
import pe.com.unifiedgo.imports.data.SimImpCosting;
import pe.com.unifiedgo.imports.data.SimImpCostingLines;
import pe.com.unifiedgo.warehouse.data.SwaMovementCode;
import pe.com.unifiedgo.warehouse.data.SwaMovementCodeProduct;

public abstract class CostingAlgorithm {
  protected MaterialTransaction transaction;
  protected HashMap<CostDimension, BaseOBObject> costDimensions = new HashMap<CostDimension, BaseOBObject>();
  protected Organization costOrg;
  protected Currency costCurrency;
  protected TrxType trxType;
  protected CostingRule costingRule;
  protected static Logger log4j = Logger.getLogger(CostingAlgorithm.class);

  /**
   * Initializes the instance of the CostingAlgorith with the MaterialTransaction that is being to
   * be calculated and the cost dimensions values in case they have to be used.
   * 
   * It initializes several values: <list><li>Organization, it's used the Legal Entity dimension. If
   * this is null Asterisk organization is used. <li>Currency, it takes the currency defined for the
   * Organization. If this is null it uses the currency defined for the Client. <li>Transaction
   * Type, it calculates its type. </list>
   * 
   * @param costingServer
   *          CostingServer instance calculating the cost of the transaction.
   */
  public void init(CostingServer costingServer) {
    transaction = costingServer.getTransaction();
    costOrg = costingServer.getOrganization();
    costCurrency = costingServer.getCostCurrency();
    trxType = TrxType.getTrxType(this.transaction);

    costingRule = costingServer.getCostingRule();
    costDimensions = CostingUtils.getEmptyDimensions();
    if (costingRule.isWarehouseDimension()) {
      costDimensions.put(CostDimension.Warehouse, transaction.getStorageBin().getWarehouse());
    }
    // Production products cannot be calculated by warehouse dimension.
    if (transaction.getProduct().isProduction()) {
      costDimensions = CostingUtils.getEmptyDimensions();
    }

  }

  /**
   * Based on the transaction type, calls the corresponding method to calculate and return the total
   * cost amount of the transaction.
   * 
   * @return the total cost amount of the transaction.
   * @throws OBException
   *           when the transaction type is unknown.
   * @throws ImportException
   */
  public BigDecimal getTransactionCost() throws OBException, ImportException {
    log4j.debug("Get transactions cost.");
    if (transaction.getMovementQuantity().compareTo(BigDecimal.ZERO) == 0 && getZeroMovementQtyCost() != null) {
      return getZeroMovementQtyCost();
    }
    
    //System.out.println("trxType: " + trxType);

    switch (trxType) {
    case Shipment:
      return getShipmentCost();
    case ShipmentChangeCode:
      return getShipmentChangeCodeCost();
    case ShipmentReturn:
      return getShipmentReturnCost();
    case ShipmentVoid:
      return getShipmentVoidCost();
    case ShipmentNegative:
      return getShipmentNegativeCost();
    case Receipt:
      return getReceiptCost();
    case ReceiptChangeCode:
    	return getReceiptChangeCodeCost();
    case ReceiptReturn: 
      return getReceiptReturnCost();
    case ReceiptVoid:
      return getReceiptVoidCost();
    case ReceiptNegative:
      return getReceiptNegativeCost();
    case InventoryDecrease:
      return getInventoryDecreaseCost();
    case InventoryIncrease:
      return getInventoryIncreaseCost();
    case IntMovementFrom:
      return getIntMovementFromCost();
    case IntMovementTo:
      return getIntMovementToCost();
    case InternalCons:
      return getInternalConsCost();
    case InternalConsNegative:
      return getInternalConsNegativeCost();
    case InternalConsVoid:
      return getInternalConsVoidCost();
    case BOMPart:
      return getBOMPartCost();
    case BOMProduct:
      return getBOMProductCost();
    case ManufacturingConsumed:
      // Manufacturing transactions are not fully implemented.
      return getManufacturingConsumedCost();
    case ManufacturingProduced:
      // Manufacturing transactions are not fully implemented.
      return getManufacturingProducedCost();
    case Unknown:
      throw new OBException("@UnknownTrxType@: " + transaction.getIdentifier());
    default:
      throw new OBException("@UnknownTrxType@: " + transaction.getIdentifier());
    }
  }

  /**
   * Calculates the total cost amount of an outgoing transaction.
   */
  abstract protected BigDecimal getOutgoingTransactionCost() throws ImportException;

  abstract protected BigDecimal getOutgoingTransactionCost(MaterialTransaction trx)  throws ImportException;

  /**
   * Auxiliary method for transactions with 0 Movement quantity. It can be overwritten by Costing
   * Algorithms to return null if further actions are needed.
   */
  protected BigDecimal getZeroMovementQtyCost() {
    return BigDecimal.ZERO;
  }

  /**
   * Calculates the cost of a Shipment line using by default the
   * {@link #getOutgoingTransactionCost()} method as a regular outgoing transaction.
   * 
   * @return BigDecimal object representing the total cost amount of the transaction.
   */
  protected BigDecimal getShipmentCost()  throws ImportException{
	  //System.out.println("*: " + getOutgoingTransactionCost() );
    return getOutgoingTransactionCost();
  }
  
  
  protected BigDecimal getShipmentChangeCodeCost()  throws ImportException{
	    return getOutgoingTransactionCost();
	  }

  /**
   * Method to calculate cost of Returned Shipments. Cost is calculated based on the proportional
   * cost of the original receipt. If no original receipt is found the default cost is returned.
   * 
   * @return BigDecimal object representing the total cost amount of the transaction.
   * @throws ImportException
   * @throws OBException
   */
  protected BigDecimal getShipmentReturnCost() throws OBException, ImportException {
    if (transaction.getGoodsShipmentLine().getSalesOrderLine() != null && transaction.getGoodsShipmentLine().getSalesOrderLine().getGoodsShipmentLine() != null) {
      return getReturnedInOutLineCost();
    } else {
      return getDefaultCost();
    }
  }

  /**
   * Method to calculate the cost of Voided Shipments. By default the cost is calculated getting the
   * cost of the original shipment. If no original shipment is found the Default Cost is returned.
   * 
   * @return BigDecimal object representing the total cost amount of the transaction.
   * @throws ImportException
   * @throws OBException
   */
  protected BigDecimal getShipmentVoidCost() throws OBException, ImportException {
    return getOriginalInOutLineCost();
  }

  /**
   * Calculates the cost of a negative Shipment line. By default if the product is purchased the
   * cost is based on its purchase price. If it is not purchased its Standard Cost is used.
   * 
   * @return BigDecimal object representing the total cost amount of the transaction.
   */
  protected BigDecimal getShipmentNegativeCost()  throws ImportException{
    return getDefaultCost();
  }

  /*
   * Calculates the cost of a Receipt line based on its related order price. When no order is found,
   * the cost based on the newer of the following three values is returned: 1. Last purchase order
   * price of the receipt's vendor for the product, 2. Purchase pricelist of the product and 3.
   * Default Cost of the product.
   * 
   * @return BigDecimal object representing the total cost amount of the transaction.
   */
  protected BigDecimal getReceiptCost() throws ImportException {
    BigDecimal trxCost = BigDecimal.ZERO;
    org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine receiptline = transaction.getGoodsShipmentLine();
    
    if(receiptline.getShipmentReceipt().getDocumentStatus().equals("VO")){
    	try{
    		return CostingUtils.getStandardCost(receiptline.getProduct(), costOrg, transaction.getTransactionProcessDate(), costDimensions, costCurrency).multiply(transaction.getMovementQuantity());
    	}catch(Exception ex){}//si falla standard cost, intentar obtener el costo del documento
    }
    
    boolean isImport = false;
    SimFolioImport simImport = null;

    OrderLine orderLine = receiptline.getSalesOrderLine();
    if (orderLine != null && orderLine.getSalesOrder().isSimIsImport()) {
      isImport = true;
      simImport = orderLine.getSalesOrder().getSimFolioimport();
    } else {
      Invoice invoice = receiptline.getShipmentReceipt().getInvoice();
      if (invoice != null) {
        Order order = invoice.getSalesOrder();
        if (order != null && order.isSimIsImport()) {
          isImport = true;
          simImport = order.getSimFolioimport();
        }
      }
    }

    if (isImport == false) {
     
      // orden de servicio
      if (receiptline.getShipmentReceipt().getSWAUpdateReason() != null && receiptline.getShipmentReceipt().getSWAUpdateReason().getSearchKey().equals("IngresoporServicio") && receiptline.getShipmentReceipt().getSsaServiceorder() != null) {
        Order serviceOrder = receiptline.getShipmentReceipt().getSsaServiceorder();
        // BigDecimal amt =
        // serviceOrder.getSummedLineAmount().divide(receiptline.getMovementQuantity());
        // BigDecimal amt = serviceOrder.getSummedLineAmount().divide(new
        // BigDecimal(serviceOrder.getScoNproductSorder()));

        if (receiptline.getSreServiceInOrderLine() == null) {
          // return getReceiptDefaultCost();
          throw new OBException("Orden de Servicio no consistente para costeo " + receiptline.getId());
        }
        

        BigDecimal amtService = receiptline.getSreServiceInOrderLine().getUnitPrice();
        if(receiptline.getSreServiceInOrderLine().getInvoiceLineList().size()==0)
        	throw new ImportException("");
        
        InvoiceLine invline = receiptline.getSreServiceInOrderLine().getInvoiceLineList().get(0);
        
        // amt = FinancialUtils.getConvertedAmount(amt, serviceOrder.getCurrency(), costCurrency,
        // transaction.getMovementDate(), costOrg, FinancialUtils.PRECISION_STANDARD);

        Date dttEmision = invline.getInvoice().getScoNewdateinvoiced();
        if(invline.getInvoice().isScoIsmanualref())
        	dttEmision = invline.getInvoice().getScoManualinvoicerefdate();
        else if(invline.getInvoice().getScoInvoiceref()!=null)
        	dttEmision = invline.getInvoice().getScoInvoiceref().getScoNewdateinvoiced();
        
        // NOW BILL OF MATERIAL AVG
        BigDecimal totalCost = BigDecimal.ZERO;
        if (receiptline.getProduct().isBillOfMaterials()) {
          for (ProductBOM prodLine : receiptline.getProduct().getProductBOMList()) {
            Product product = prodLine.getBOMProduct();
            if (product.getProductType().equals("I") && (product.isStocked() || product.isBillOfMaterials())) {
              BigDecimal standardCost = CostingUtils.getStandardCost(product, costOrg, transaction.getTransactionProcessDate(), costDimensions, costCurrency);
              if (standardCost == null) {
                throw new OBException("@NoCostCalculated@: " + product.getIdentifier());
              }
              totalCost = totalCost.add(standardCost.multiply(prodLine.getBOMQuantity()));

            } else {

            }
          }

          trxCost = totalCost.multiply(transaction.getMovementQuantity().abs());
          totalCost = amtService.multiply(transaction.getMovementQuantity().abs());
          trxCost = trxCost.add(FinancialUtils.getConvertedAmount(totalCost, receiptline.getSreServiceInOrderLine().getCurrency(), costCurrency, dttEmision, costOrg, FinancialUtils.PRECISION_COSTING));

        } else {
          // throw new
          // OBException("Service Order receipt doesnt contain a BOM Product @Organization@: " +
          // costOrg.getName() + ", @Product@: " + transaction.getProduct().getName() + ", @Date@: "
          // + OBDateUtils.formatDate(transaction.getTransactionProcessDate()));
          // Productos sin BOM se costean de forma normal tipo nacional
          // OJO Para que esto funcione, tiene que las lineas ser 1 a 1
          BigDecimal orderAmt = amtService.multiply(transaction.getMovementQuantity().abs());
          trxCost = trxCost.add(FinancialUtils.getConvertedAmount(orderAmt, receiptline.getSreServiceInOrderLine().getCurrency(), costCurrency, dttEmision, costOrg, FinancialUtils.PRECISION_COSTING));
        }

        return trxCost;
      }

      
      
      // si es entre empresas extraordinario (reposicion es como un ingreso normal con O/C)
      /*if (receiptline.getShipmentReceipt().getSsaOtherInout() != null) {
    	  
    	 List<OrderLine> lsOrders = receiptline.getOrderLineEMSsaOtherInoutLiIDList(); 
        if(lsOrders.size()>0){
    	 
        	OrderLine orderl = lsOrders.get(0);
	        
	        BigDecimal orderAmt = receiptline.getMovementQuantity().multiply(orderl.getSsaQuotPriceactual());
	        trxCost = trxCost.add(FinancialUtils.getConvertedAmount(orderAmt, orderl.getCurrency(), costCurrency, transaction.getMovementDate(), costOrg, FinancialUtils.PRECISION_STANDARD));
	      
	        return trxCost;
	        
        }
        
      }*/
      
      /*  else{
        	//CASO DE QUE NO ENLACE POR LINEA (BUG EN ENERO YA SE CORRIGIO)
        	if(receiptline.getShipmentReceipt().getSsaOtherInout()!=null){
        		//BUSCAR LINEA
        		
        		boolean notfound =true;
        		
        		List<ShipmentInOutLine> lsLineas = receiptline.getShipmentReceipt().getSsaOtherInout().getMaterialMgmtShipmentInOutLineList();
  	          
  	          for(int ii=0; ii<lsLineas.size(); ii++){
  	        	  ShipmentInOutLine line = lsLineas.get(ii);
  	        	  if(line.getProduct().getSearchKey().equals(receiptline.getProduct().getScrInternalcode()) && line.getMovementQuantity().equals(receiptline.getMovementQuantity()) ){
  	        		  
  	        		 if(line.getInvoiceLineList().size()==0){//no tiene asociacion por detalle, vamos a la cabecera
  	        			notfound=true;
  	        			break;
  	        		 }
  	        		
  	        		  BigDecimal orderAmt = receiptline.getMovementQuantity().multiply(line.getInvoiceLineList().get(0).getUnitPrice());
        				trxCost = trxCost.add(FinancialUtils.getConvertedAmount(orderAmt, line.getInvoiceLineList().get(0).getInvoice().getCurrency(), costCurrency, transaction.getMovementDate(), costOrg, FinancialUtils.PRECISION_STANDARD));
        						notfound=false;
        		        break;
  	        	  }  
  	          }
  	          
  	          if(notfound){
		        		if(receiptline.getShipmentReceipt().getSsaOtherInout().getScrCInvoice()!=null){
			        		
			        		List<InvoiceLine> lsLineasInv = receiptline.getShipmentReceipt().getSsaOtherInout().getScrCInvoice().getInvoiceLineList();
			        		for(int iu=0; iu<lsLineasInv.size(); iu++){
			        			InvoiceLine line = lsLineasInv.get(iu);
			        			if(line.getProduct().getSearchKey().equals(receiptline.getProduct().getScrInternalcode()) && line.getInvoicedQuantity().equals(receiptline.getMovementQuantity()) ){
			        				
			        				BigDecimal orderAmt = receiptline.getMovementQuantity().multiply(line.getUnitPrice());
			        				trxCost = trxCost.add(FinancialUtils.getConvertedAmount(orderAmt, line.getInvoice().getCurrency(), costCurrency, transaction.getMovementDate(), costOrg, FinancialUtils.PRECISION_STANDARD));
			        						notfound=false;
			        		        break;
			        			}
			        		}
		        		}    
        		}
        		
  	          
        		if(!notfound)
        		  return trxCost;
        		
        	}
        }

        

      }*/

      
      if(receiptline.getShipmentReceipt().getSwaRequireposicion()!=null){
    	  
    	  List<ShipmentInOut> ship = new ArrayList<ShipmentInOut>();
    	  
    	  if(receiptline.getShipmentReceipt().getSwaAsociateInout()==null){
    	  
	    	  OBCriteria<ShipmentInOut> obCriteria = OBDal.getInstance().createCriteria(ShipmentInOut.class);
	          obCriteria.add(Restrictions.eq(ShipmentInOut.PROPERTY_SWAASOCIATEINOUT, receiptline.getShipmentReceipt() ));
	          ship = obCriteria.list();
	          if(ship.size()!=1) throw new ImportException("");
	          
    	  }else{
    		  ship.add(receiptline.getShipmentReceipt().getSwaAsociateInout());
    	  }
    	  
          List<ShipmentInOutLine> lines = ship.get(0).getMaterialMgmtShipmentInOutLineList();
          for(int ii=0; ii<lines.size(); ii++){
        	  if(lines.get(ii).getProduct().getId().equals(receiptline.getProduct().getId()) && 
        			  lines.get(ii).getMovementQuantity().compareTo(receiptline.getMovementQuantity())==0 )
        	  {
        		  
        		  return CostingUtils.getTransactionCost(lines.get(ii).getMaterialMgmtMaterialTransactionList().get(0), transaction.getTransactionProcessDate(), costCurrency);
        	  }

        	  
          }
          
          
          int b = 0;
          BigDecimal trxcost = BigDecimal.ZERO;
          for(int ii=0; ii<lines.size(); ii++){
        	  if(lines.get(ii).getProduct().getId().equals(receiptline.getProduct().getId())){
        		  b++;
        		    MaterialTransaction mt = lines.get(ii).getMaterialMgmtMaterialTransactionList().get(0);
        		    trxcost = CostingUtils.getTransactionCost(mt, transaction.getTransactionProcessDate(), costCurrency).multiply(transaction.getMovementQuantity().abs()).divide(mt.getMovementQuantity().abs(), costCurrency.getCostingPrecision().intValue(), RoundingMode.HALF_UP);
        		  	//return CostingUtils.getTransactionCost(mt, transaction.getTransactionProcessDate(), costCurrency).multiply(transaction.getMovementQuantity().abs()).divide(mt.getMovementQuantity().abs(), costCurrency.getCostingPrecision().intValue(), RoundingMode.HALF_UP);
        		  
        	  }
          }
          
          if(b>0){
        	  return trxcost;
          }
          
          
          
          
      }
      
     // if (receiptline.getSalesOrderLine() == null) {
    	  //FIX-ME ACA VAN PRODUCTOS QUE NOS DAN A CONSIGNACION
        
    //	  else throw new OBException("Linea sin Orden de Compra" + receiptline.getId());
    //  }
      
     
      //Buscar factura
      BigDecimal bqty = BigDecimal.ZERO;
      for (org.openbravo.model.procurement.ReceiptInvoiceMatch matchInv : receiptline.getProcurementReceiptInvoiceMatchList()) {
    	  BigDecimal orderAmt = matchInv.getQuantity().multiply(matchInv.getInvoiceLine().getUnitPrice());
    	  bqty = bqty.add(matchInv.getQuantity());
    	  
    	  Invoice inv = matchInv.getInvoiceLine().getInvoice();
    	  Date dttEmision =inv.getScoNewdateinvoiced();
          if(inv.isScoIsmanualref())
          	dttEmision =inv.getScoManualinvoicerefdate();
          else if(inv.getScoInvoiceref()!=null)
          	dttEmision = inv.getScoInvoiceref().getScoNewdateinvoiced();
          
          
    	  trxCost = trxCost.add(FinancialUtils.getConvertedAmount(orderAmt, matchInv.getInvoiceLine().getInvoice().getCurrency(), costCurrency, dttEmision, costOrg, FinancialUtils.PRECISION_COSTING));
      }
      
      String description = receiptline.getShipmentReceipt().getDescription();
	  boolean byDescription=false;
      if(description!=null && description.contains("##")) byDescription=true;
		
      
      if(bqty.compareTo(receiptline.getMovementQuantity())!=0){
    	  //NO SE HA JALADO DE LA GUIA O SE HA CREADO LA FACTURA ANTES QUE LA GUIA
    	  //Verificar que haya enlace
    	  trxCost = BigDecimal.ZERO;
    	  OrderLine order = receiptline.getSalesOrderLine();
    	  Order morder = receiptline.getShipmentReceipt().getSalesOrder();
		  
    	  if(receiptline.getInvoiceLineList().size()==1){
    		  
    		  InvoiceLine line = receiptline.getInvoiceLineList().get(0);
    		  
    		  Invoice inv = line.getInvoice();
        	  Date dttEmision =inv.getScoNewdateinvoiced();
              if(inv.isScoIsmanualref())
              	dttEmision =inv.getScoManualinvoicerefdate();
              else if(inv.getScoInvoiceref()!=null)
              	dttEmision = inv.getScoInvoiceref().getScoNewdateinvoiced();
              
    		  
    		  BigDecimal orderAmt = receiptline.getMovementQuantity().multiply(line.getUnitPrice());
    		  trxCost = trxCost.add(FinancialUtils.getConvertedAmount(orderAmt, line.getInvoice().getCurrency(), costCurrency, dttEmision, costOrg, FinancialUtils.PRECISION_COSTING));
        	  
    	  }
    	  else if(byDescription){//ASOCIACION A FACTURA MANUAL
    		  
    		  if(description!=null && description.contains("##")){
				  int index1 = description.indexOf("##");
				  int index2 = description.indexOf("##", index1+1);
				  
				  if(index1!=-1 && index2!=-1){
					  String doc = description.substring(index1+2, index2);
				
					  OBCriteria<Invoice> obCriteria = OBDal.getInstance().createCriteria(Invoice.class);
				      obCriteria.add(Restrictions.eq(Invoice.PROPERTY_SCRPHYSICALDOCUMENTNO, doc));
				      obCriteria.add(Restrictions.eq(Invoice.PROPERTY_BUSINESSPARTNER, receiptline.getShipmentReceipt().getBusinessPartner()));
				      List<Invoice> lsInvoices = obCriteria.list();
				     if(lsInvoices.size()!=0){
				    	 Invoice invoice = lsInvoices.get(0);
				    	 List<InvoiceLine> lsInvlines = invoice.getInvoiceLineList();
				    	 int ii=0;
				    	 boolean found =false;
				    	 		  for(int iii=0; iii<lsInvlines.size(); iii++){
			    					  InvoiceLine line = lsInvlines.get(iii);
			    					  if(line.getProduct().getId().equals(receiptline.getProduct().getId()) && line.getUnitPrice().compareTo(BigDecimal.ZERO)!=0 ){
			    						  	BigDecimal orderAmt = receiptline.getMovementQuantity().multiply(line.getUnitPrice());
			    						  	
			    						    Invoice inv = line.getInvoice();
			    				        	  Date dttEmision =inv.getScoNewdateinvoiced();
			    				              if(inv.isScoIsmanualref())
			    				              	dttEmision =inv.getScoManualinvoicerefdate();
			    				              else if(inv.getScoInvoiceref()!=null)
			    				              	dttEmision = inv.getScoInvoiceref().getScoNewdateinvoiced();
			    				              
			    				              
			    						  	trxCost = trxCost.add(FinancialUtils.getConvertedAmount(orderAmt, line.getInvoice().getCurrency(), costCurrency, dttEmision, costOrg, FinancialUtils.PRECISION_COSTING));
			    						  	found=true;
			    						  	break;
			    					  }
				    	 		  }
			    				  
			    		 if(!found) throw new ImportException("");
				     }
					  
				  }
				  
			  }
			
			  
    		  
    		  
    	  }else if(order!=null && order.getInvoiceLineList().size()==1 && order.getInvoiceLineList().get(0).getInvoicedQuantity().compareTo(receiptline.getMovementQuantity())==0){
    		  InvoiceLine line = order.getInvoiceLineList().get(0);
    		  
    		  Invoice inv = line.getInvoice();
        	  Date dttEmision =inv.getScoNewdateinvoiced();
              if(inv.isScoIsmanualref())
              	dttEmision =inv.getScoManualinvoicerefdate();
              else if(inv.getScoInvoiceref()!=null)
              	dttEmision = inv.getScoInvoiceref().getScoNewdateinvoiced();
              
              
    		  BigDecimal orderAmt = receiptline.getMovementQuantity().multiply(line.getUnitPrice());
    		  trxCost = trxCost.add(FinancialUtils.getConvertedAmount(orderAmt, line.getInvoice().getCurrency(), costCurrency, dttEmision, costOrg, FinancialUtils.PRECISION_COSTING));
        	  
    	  } else if(order!=null ){
    		  
    		  //QUIZA NO TIENE INVOICELINE, PERO HAY OTRO ORDERLINE CON EL MISMO PRODUCTO Y EL MISMO PRECIO
    		  Order orderParent = order.getSalesOrder();
    		  List<OrderLine> lsLines = orderParent.getOrderLineList();
    		  int ii=0;
    		  for(ii=0; ii<lsLines.size(); ii++){
    			  OrderLine orderline= lsLines.get(ii);
    			  if(orderline.getProduct().getId().equals(receiptline.getProduct().getId()) && orderline.getUnitPrice().compareTo(order.getUnitPrice())==0
    					  && orderline.getInvoiceLineList().size()==1){
    				  InvoiceLine line = orderline.getInvoiceLineList().get(0);
    				  
    				  Invoice inv = line.getInvoice();
    	        	  Date dttEmision =inv.getScoNewdateinvoiced();
    	              if(inv.isScoIsmanualref())
    	              	dttEmision =inv.getScoManualinvoicerefdate();
    	              else if(inv.getScoInvoiceref()!=null)
    	              	dttEmision = inv.getScoInvoiceref().getScoNewdateinvoiced();
    	              
    	              
    				  BigDecimal orderAmt = receiptline.getMovementQuantity().multiply(line.getUnitPrice());
    	    		  trxCost = trxCost.add(FinancialUtils.getConvertedAmount(orderAmt, line.getInvoice().getCurrency(), costCurrency, dttEmision, costOrg, FinancialUtils.PRECISION_COSTING));
    	        	  break;
    			  }
    		  }
    	  
    		  if(ii==lsLines.size()){
    			  //PRIMERO BUSCAR EN DESCRIPCION POR PISTA DE FACTURA
    			  boolean found =false;
    			  description = receiptline.getShipmentReceipt().getDescription();
    			  if(description!=null && description.contains("##")){
    				  int index1 = description.indexOf("##");
    				  int index2 = description.indexOf("##", index1+1);
    				  
    				  if(index1!=-1 && index2!=-1){
    					  String doc = description.substring(index1+2, index2);
    					  
    					  for(ii=0; ii<lsLines.size(); ii++){
    		    			  OrderLine orderline= lsLines.get(ii);
    		    			  if(orderline.getProduct().getId().equals(receiptline.getProduct().getId())
    		    					  && orderline.getInvoiceLineList().size()>0){
    		    				  
    		    				  List<InvoiceLine> lsInvlines = orderline.getInvoiceLineList();
    		    				  
    		    				  for(int iii=0; iii<lsInvlines.size(); iii++){
    		    					  InvoiceLine line = lsInvlines.get(iii);
    		    					  if(line.getInvoice().getScrPhysicalDocumentno().equals(doc)){
    		    						  	BigDecimal orderAmt = receiptline.getMovementQuantity().multiply(line.getUnitPrice());
    		    						  	
    		    						    Invoice inv = line.getInvoice();
    		    				        	  Date dttEmision =inv.getScoNewdateinvoiced();
    		    				              if(inv.isScoIsmanualref())
    		    				              	dttEmision =inv.getScoManualinvoicerefdate();
    		    				              else if(inv.getScoInvoiceref()!=null)
    		    				              	dttEmision = inv.getScoInvoiceref().getScoNewdateinvoiced();
    		    				              
    		    				              
    		    						  	trxCost = trxCost.add(FinancialUtils.getConvertedAmount(orderAmt, line.getInvoice().getCurrency(), costCurrency, dttEmision, costOrg, FinancialUtils.PRECISION_COSTING));
    		    						  	found=true;
    		    						  	break;
    		    					  }
    		    				  }
    		    				  
    		    				  if(found) break;
    		    			  }
    		    		  }
    					  
    				  }
    				  
    			  }
    			  
    			  if(!found)
    				  throw new ImportException("");
    		  }
    		  
    	  }else if(morder!=null && receiptline.getShipmentReceipt().getDocumentStatus().equals("VO")){
    		
    		//QUIZA NO TIENE INVOICELINE, PERO HAY OTRO ORDERLINE CON EL MISMO PRODUCTO Y EL MISMO PRECIO
    		  List<OrderLine> lsLines = morder.getOrderLineList();
    		  boolean found=false;
    		  int ii=0;
    		  for(ii=0; ii<lsLines.size(); ii++){
    			  OrderLine orderline= lsLines.get(ii);
    			  if(orderline.getProduct().getId().equals(receiptline.getProduct().getId())
    					  && orderline.getInvoiceLineList().size()>0){
    				  InvoiceLine line = orderline.getInvoiceLineList().get(0);
    				  
    				   Invoice inv = line.getInvoice();
			        	  Date dttEmision =inv.getScoNewdateinvoiced();
			              if(inv.isScoIsmanualref())
			              	dttEmision =inv.getScoManualinvoicerefdate();
			              else if(inv.getScoInvoiceref()!=null)
			              	dttEmision = inv.getScoInvoiceref().getScoNewdateinvoiced();
			              
			              
    				  BigDecimal orderAmt = receiptline.getMovementQuantity().multiply(line.getUnitPrice());
    	    		  trxCost = trxCost.add(FinancialUtils.getConvertedAmount(orderAmt, line.getInvoice().getCurrency(), costCurrency, dttEmision, costOrg, FinancialUtils.PRECISION_COSTING));
    	    		  found=true;
    	    		  break;
    			  }
    		  }
    	  
    		  if(!found)
				  throw new ImportException("");
    	  }else{
    		  
    		  String categoria = receiptline.getShipmentReceipt().getSWAUpdateReason().getComboCategory().getSearchKey();
    		  if(categoria.equals("Tipo Muestra Devolucion") || categoria.equals("OtherOrdersforReceipt") || categoria.equals("OtherOrdersforShipment")  
    				   || categoria.equals("OrderTypeReturnMaterialReceipt")  || categoria.equals("AjustesInventory")  || categoria.equals("Tipo Reposition")){
    			  return getOutgoingTransactionCost();
    			  	
    		  }
    		  else throw new ImportException("");
    		  
    	  }
      }
      
      //BUSCAR NOTA DE CREDITO POR DIFERENCIA DE PRECIO ASOCIADA A LA LINEA
      OBCriteria<InvoiceLine> obCriteria = OBDal.getInstance().createCriteria(InvoiceLine.class);
      obCriteria.add(Restrictions.eq(InvoiceLine.PROPERTY_SIMAPCREDITTOSHIPLINE, receiptline));
      List<InvoiceLine> diffLines = obCriteria.list();
      
      for(int ii=0; ii<diffLines.size(); ii++){
    	  Date dttEmision = null;
    	  boolean ismanual = diffLines.get(ii).getInvoice().isScoIsmanualref();
    	  if(ismanual) dttEmision = diffLines.get(ii).getInvoice().getScoManualinvoicerefdate();
    	  else if(diffLines.get(ii).getInvoice().getScoInvoiceref()!=null) dttEmision = diffLines.get(ii).getInvoice().getScoInvoiceref().getScoNewdateinvoiced();
    	  else dttEmision = diffLines.get(ii).getInvoice().getScoNewdateinvoiced();
    	  
    	  trxCost = trxCost.subtract(FinancialUtils.getConvertedAmount(diffLines.get(ii).getLineNetAmount(), diffLines.get(ii).getInvoice().getCurrency(), costCurrency, dttEmision, costOrg, FinancialUtils.PRECISION_COSTING));
      }
      
      /*for (org.openbravo.model.procurement.POInvoiceMatch matchPO : receiptline.getProcurementPOInvoiceMatchList()) {
        BigDecimal orderAmt = matchPO.getQuantity().multiply(matchPO.getSalesOrderLine().getUnitPrice());
        
        trxCost = trxCost.add(FinancialUtils.getConvertedAmount(orderAmt, matchPO.getSalesOrderLine().getCurrency(), costCurrency, transaction.getMovementDate(), costOrg, FinancialUtils.PRECISION_STANDARD));
      }*/

    } else {

      System.out.println("Aca Costeo Imp ");

      if(simImport == null){
    	  throw new OBException("@sim_partialWithoutFolio@: " + orderLine.getSalesOrder().getDocumentNo());
      }
      
      List<SimImpCosting> vCosting = simImport.getSimImpCostingList();
      for(int kj=0; kj<vCosting.size(); kj++){
    	  if(vCosting.get(kj).getDocumentStatus().equals("CO")){
    		  SimImpCosting stemp = vCosting.get(0);
    		  vCosting.set(0, vCosting.get(kj));
    		  vCosting.set(kj, stemp);
    		  break;
    	  }
      }
      
      if (vCosting.size() > 0 && vCosting.get(0).getDocumentStatus().equals("CO")) {

        List<SimImpCostingLines> vLines = vCosting.get(0).getSimImpCostinglinesList();
        SimImpCostingLines lineProduct = null;
        BigDecimal costingamt = new BigDecimal(0);
        BigDecimal costingqty = new BigDecimal(0);
        for (int i = 0; i < vLines.size(); i++) {
          if (vLines.get(i).getProduct().getId().equals(transaction.getProduct().getId())) {
            lineProduct = vLines.get(i);
            costingamt = costingamt.add(lineProduct.getCostUnitCurrencylocal().multiply(new BigDecimal(lineProduct.getInvoicedQuantity())));
            costingqty =  costingqty.add(new BigDecimal(lineProduct.getInvoicedQuantity()));
          }
        }

        if (lineProduct != null) {

          OBCriteria<Currency> obCriteria = OBDal.getInstance().createCriteria(Currency.class);
          obCriteria.add(Restrictions.eq(Currency.PROPERTY_ISOCODE, "PEN"));
          List<Currency> currencyPen = obCriteria.list();

          BigDecimal unitPrice;
          
          if(costingqty.compareTo(BigDecimal.ZERO)==0)
        	  unitPrice = BigDecimal.ZERO;
          else
        	  unitPrice = costingamt.divide(costingqty, 12, RoundingMode.HALF_UP);
          
          trxCost = trxCost.add(FinancialUtils.getConvertedAmount(unitPrice, currencyPen.get(0), costCurrency, transaction.getMovementDate(), costOrg, FinancialUtils.PRECISION_COSTING));
          trxCost = trxCost.multiply(transaction.getMovementQuantity().abs());

        } else {
          System.out.println("Aca Costeo Imp 2");
          throw new OBException("@NoPriceListOrStandardCostForProduct@ @Organization@: " + costOrg.getName() + ", @Product@: " + transaction.getProduct().getName() + ", @Date@: " + OBDateUtils.formatDate(transaction.getTransactionProcessDate()));
        }
      } else {
        System.out.println("Aca Costeo Imp 3");
        throw new ImportException("");
        // throw new OBException("@NoPriceListOrStandardCostForProduct@ @Organization@: " +
        // costOrg.getName() + ", @Product@: " + transaction.getProduct().getName() + ", @Date@: " +
        // OBDateUtils.formatDate(transaction.getTransactionProcessDate()));
      }

    }

    return trxCost;
  }
  
  
  protected BigDecimal getReceiptChangeCodeCost()  throws ImportException {
	  
	  BigDecimal trxCost = BigDecimal.ZERO;
	  SwaMovementCodeProduct receiptline = transaction.getSwaMovcodeProduct();

	  //buscar producto que salio
	  OBCriteria<MaterialTransaction> obCriteria = OBDal.getInstance().createCriteria(MaterialTransaction.class);
      obCriteria.add(Restrictions.eq(MaterialTransaction.PROPERTY_SWAMOVCODEPRODUCT, receiptline));
      obCriteria.add(Restrictions.lt(MaterialTransaction.PROPERTY_MOVEMENTQUANTITY, BigDecimal.ZERO));
      MaterialTransaction transaction2 = (MaterialTransaction)obCriteria.uniqueResult();
        	  
      return CostingUtils.getTransactionCost(transaction2, transaction.getTransactionProcessDate(), true, costCurrency);
      
  }

  /**
   * Calculates the cost of a Returned Receipt line as a regular outgoing transaction.
   * 
   * @return BigDecimal object representing the total cost amount of the transaction.
   */
  protected BigDecimal getReceiptReturnCost()  throws ImportException{
    return getReceiptCost().negate();//getOutgoingTransactionCost();
  }

  /**
   * Method to calculate the cost of Voided Receipts. By default the cost is calculated getting the
   * cost of the original payment. If no original Receipt is found cost is calculated as a regular
   * outgoing transaction.
   * 
   * @return BigDecimal object representing the total cost amount of the transaction.
   * @throws ImportException
   * @throws OBException
   */
  protected BigDecimal getReceiptVoidCost() throws OBException, ImportException {
    return getOriginalInOutLineCost();
  }

  /**
   * Calculates the cost of a Negative Receipt line using by default the
   * {@link #getOutgoingTransactionCost()} method as a regular outgoing transaction.
   * 
   * @return BigDecimal object representing the total cost amount of the transaction.
   */
  protected BigDecimal getReceiptNegativeCost()  throws ImportException{
    return getOutgoingTransactionCost();
  }

  protected BigDecimal getReceiptDefaultCost()  throws ImportException {
    Costing stdCost = CostingUtils.getStandardCostDefinition(transaction.getProduct(), costOrg, transaction.getTransactionProcessDate(), costDimensions);
    BusinessPartner bp = transaction.getGoodsShipmentLine().getShipmentReceipt().getBusinessPartner();
    // System.out.println("std:"+stdCost+" "+transaction.getTransactionProcessDate()+" "+bp+" ");
    PriceList pricelist = bp.getPurchasePricelist();
    ProductPrice pp = FinancialUtils.getProductPrice(transaction.getProduct(), transaction.getTransactionProcessDate(), false, pricelist, false);
    OrderLine orderLine = CostingUtils.getOrderLine(transaction.getProduct(), bp, costOrg);

    if (stdCost == null && pp == null && orderLine == null) {
      throw new OBException("@NoPriceListOrStandardCostForProduct@ @Organization@: " + costOrg.getName() + ", @Product@: " + transaction.getProduct().getName() + ", @Date@: " + OBDateUtils.formatDate(transaction.getTransactionProcessDate()));
    }
    Date stdCostDate = new Date(0L);
    if (stdCost != null) {
      stdCostDate = stdCost.getStartingDate();
    }
    Date ppDate = new Date(0L);
    if (pp != null) {
      ppDate = pp.getPriceListVersion().getValidFromDate();
    }
    Date olDate = new Date(0L);
    if (orderLine != null) {
      olDate = orderLine.getOrderDate();
    }

    if (ppDate.before(olDate) && stdCostDate.before(olDate)) {
      // purchase order
      @SuppressWarnings("null")
      BigDecimal cost = transaction.getMovementQuantity().abs().multiply(orderLine.getUnitPrice());
      if (costCurrency.getId().equals(orderLine.getCurrency().getId())) {
        return cost;
      } else {
        return FinancialUtils.getConvertedAmount(cost, orderLine.getCurrency(), costCurrency, transaction.getMovementDate(), costOrg, FinancialUtils.PRECISION_COSTING);
      }
    } else {
      return getDefaultCost();
    }
  }

  
  
	  public int compareDate(Date d1, Date d2) {
	    if (d1.getYear() != d2.getYear()) 
	        return d1.getYear() - d2.getYear();
	    if (d1.getMonth() != d2.getMonth()) 
	        return d1.getMonth() - d2.getMonth();
	    return d1.getDate() - d2.getDate();
	  }
	
  
  /**
   * Returns the cost of the canceled Shipment/Receipt line on the date it is canceled.
   * 
   * @return BigDecimal object representing the total cost amount of the transaction.
   * @throws OBException
   *           when no original in out line is found.
   * @throws ImportException
   */
  protected BigDecimal getOriginalInOutLineCost() throws OBException, ImportException {
    if (transaction.getGoodsShipmentLine().getCanceledInoutLine() == null) {
      log4j.error("No canceled line found for transaction: " + transaction.getId());
      throw new OBException("@NoCanceledLineFoundForTrx@ @Transaction@: " + transaction.getIdentifier());
    }
    MaterialTransaction origInOutLineTrx = transaction.getGoodsShipmentLine().getCanceledInoutLine().getMaterialMgmtMaterialTransactionList().get(0);

    return CostingUtils.getTransactionCost(origInOutLineTrx, transaction.getTransactionProcessDate(), costCurrency);
    
  }

  /**
   * Gets the returned in out line and returns the proportional cost amount based on the original
   * movement quantity and the returned movement quantity.
   * 
   * @return BigDecimal object representing the total cost amount of the transaction.
   * @throws OBException
   *           when no original in out line is found.
   * @throws ImportException
   */
  protected BigDecimal getReturnedInOutLineCost() throws OBException, ImportException {
    MaterialTransaction originalTrx = null;
    try {
      originalTrx = transaction.getGoodsShipmentLine().getSalesOrderLine().getGoodsShipmentLine().getMaterialMgmtMaterialTransactionList().get(0);
      BigDecimal originalCost = CostingUtils.getTransactionCost(originalTrx, transaction.getTransactionProcessDate(), costCurrency);
      return originalCost.multiply(transaction.getMovementQuantity().abs()).divide(originalTrx.getMovementQuantity().abs(), costCurrency.getCostingPrecision().intValue(), RoundingMode.HALF_UP);

    } catch (Exception e) {
    	
    	return getOutgoingTransactionCost();
    	
    	//throw new OBException("@NoReturnedLineFoundForTrx@ @Transaction@: " + transaction.getIdentifier());
    }
      }

  /**
   * Calculates the cost of a Inventory line that decrease the stock using by default the
   * {@link #getOutgoingTransactionCost()} method as a regular outgoing transaction.
   * 
   * @return BigDecimal object representing the total cost amount of the transaction.
   */
  protected BigDecimal getInventoryDecreaseCost()  throws ImportException{
    return getOutgoingTransactionCost();
  }

  /**
   * Calculates the total cost amount of a physical inventory that results on an increment of stock.
   * Default Cost is used.
   * 
   * @return BigDecimal object representing the total cost amount of the transaction.
   */
  protected BigDecimal getInventoryIncreaseCost()  throws ImportException{
    if (transaction.getPhysicalInventoryLine().getCost() != null) {
      return transaction.getPhysicalInventoryLine().getCost().multiply(transaction.getMovementQuantity().abs());
    }
    return getDefaultCost();
  }

  /**
   * Calculates the cost of the From transaction of an Internal Movement line using by default the
   * {@link #getOutgoingTransactionCost()} method as a regular outgoing transaction.
   * 
   * @return BigDecimal object representing the total cost amount of the transaction.
   */
  protected BigDecimal getIntMovementFromCost()  throws ImportException{
    return getOutgoingTransactionCost();
  }

  /**
   * Calculates the total cost amount of an incoming internal movement. The cost amount is the same
   * than the related outgoing transaction. The outgoing transaction cost is calculated if it has
   * not been yet.
   * 
   * @return BigDecimal object representing the total cost amount of the transaction.
   * @throws ImportException
   * @throws OBException
   *           when no related internal movement is found.
   */
  protected BigDecimal getIntMovementToCost() throws OBException, ImportException {
    // Get transaction of From movement to retrieve it's cost.
    for (MaterialTransaction movementTransaction : transaction.getMovementLine().getMaterialMgmtMaterialTransactionList()) {
      if (movementTransaction.getId().equals(transaction.getId())) {
        continue;
      }
      // Calculate transaction cost if it is not calculated yet.
      return CostingUtils.getTransactionCost(movementTransaction, transaction.getTransactionProcessDate(), true, costCurrency);
    }
    // If no transaction is found throw an exception.
    throw new OBException("@NoInternalMovementTransactionFound@ @Transaction@: " + transaction.getIdentifier());
  }

  /**
   * Calculates the cost of an Internal Consumption line using by default the
   * {@link #getOutgoingTransactionCost()} method as a regular outgoing transaction.
   * 
   * @return BigDecimal object representing the total cost amount of the transaction.
   */
  protected BigDecimal getInternalConsCost()  throws ImportException{
    return getOutgoingTransactionCost();
  }

  /**
   * Calculates the cost of a negative internal consumption using the Default Cost.
   * 
   * @return BigDecimal object representing the total cost amount of the transaction.
   */
  protected BigDecimal getInternalConsNegativeCost()  throws ImportException{
    return getDefaultCost();
  }

  /**
   * Returns the cost of the original internal consumption.
   * 
   * @return BigDecimal object representing the total cost amount of the transaction.
   * @throws ImportException
   * @throws OBException
   */
  protected BigDecimal getInternalConsVoidCost() throws OBException, ImportException {
    return CostingUtils.getTransactionCost(transaction.getInternalConsumptionLine().getVoidedInternalConsumptionLine().getMaterialMgmtMaterialTransactionList().get(0), transaction.getTransactionProcessDate(), true, costCurrency);
  }

  /**
   * Calculates the cost of a BOM Production used part using by default the
   * {@link #getOutgoingTransactionCost()} method as a regular outgoing transaction.
   * 
   * @return BigDecimal object representing the total cost amount of the transaction.
   */
  protected BigDecimal getBOMPartCost()  throws ImportException {

	  if(transaction.getMovementType().equals("M+")  ||  transaction.getMovementType().equals("M-") ){
  		return BigDecimal.ZERO;
  	}
  	
	  
    if (transaction.getProductionLine() != null) {
    	
      ProductionTransaction production = transaction.getProductionLine().getProductionPlan().getProduction();
      if (production.isSwaDesarmado()) {
        List<ProductionLine> productionLines = transaction.getProductionLine().getProductionPlan().getManufacturingProductionLineList();

        // ver el costo de todos
        Date dttOld = null;
        Date dttOldCreation = null;
        String productionLineId = null;
        int numberWithoutCosting = 0;
        boolean withoutCostingOurs = false;
        for (int i = 0; i < productionLines.size(); i++) {

        	//Obtener transaction de la linea que no este asociada a M+ o M-
        	List<MaterialTransaction> lsTrans =productionLines.get(i).getMaterialMgmtMaterialTransactionList();
        	MaterialTransaction mt =null;
        	for(int j=0; j<lsTrans.size(); j++){
        		 mt = lsTrans.get(j);
        		if(mt.getMovementType().equals("M+") || mt.getMovementType().equals("M-")){
        			continue;
        		}
        		break;
        	}
        	
          Product p = mt.getProduct();

          if (mt.getMovementQuantity().compareTo(BigDecimal.ZERO) < 0) {// este
                                                                                                                                            // es
                                                                                                                                            // el
                                                                                                                                            // outgoing
            continue;
          }

          OBCriteria<Costing> costing = OBDal.getInstance().createCriteria(Costing.class);
          costing.add(Restrictions.eq(Costing.PROPERTY_PRODUCT, p));
          costing.add(Restrictions.ne(Costing.PROPERTY_INVENTORYTRANSACTION, mt));
          costing.addOrderBy(Costing.PROPERTY_ENDINGDATE, false);

          if (costing.list().size() > 0) {
            Costing c = costing.list().get(0);
            if (dttOld == null 
            	|| c.getStartingDate().before(dttOld)
            	|| (c.getStartingDate().equals(dttOld) && c.getCreationDate()
                    .before(dttOldCreation))) {
              dttOld = c.getStartingDate();
              dttOldCreation = c.getCreationDate();
              productionLineId = productionLines.get(i).getId();
            }
          } else {
            numberWithoutCosting++;
            if (mt.equals(transaction.getId()))
              withoutCostingOurs = true;
            if (numberWithoutCosting > 1) {
              throw new OBException("@NoCostCalculated@: " + mt.getIdentifier());
            }
          }
        }

        if (withoutCostingOurs || (numberWithoutCosting == 0 && dttOld != null && productionLineId.equals(transaction.getProductionLine().getId()))) {
          // diferencia
          BigDecimal stpos = BigDecimal.ZERO;
          BigDecimal stneg = BigDecimal.ZERO;
          for (int i = 0; i < productionLines.size(); i++) {
        	  
        	//Obtener transaction de la linea que no este asociada a M+ o M-
          	List<MaterialTransaction> lsTrans =productionLines.get(i).getMaterialMgmtMaterialTransactionList();
          	MaterialTransaction trx =null;
          	for(int j=0; j<lsTrans.size(); j++){
          		trx = lsTrans.get(j);
          		if(trx.getMovementType().equals("M+") || trx.getMovementType().equals("M-")){
          			continue;
          		}
          		break;
          	}
          	
             if (trx.getMovementQuantity().compareTo(BigDecimal.ZERO) < 0) {
              stpos = stpos.add(getOutgoingTransactionCost(trx));
            } else if (!trx.getId().equals(transaction.getId())) {
              stneg = stneg.add(getOutgoingTransactionCost(trx));
            }

            //System.out.println("Cuanto desarmado: " + stpos + " " + stneg);
          }

          return stpos.subtract(stneg).setScale(12, RoundingMode.HALF_UP);

        } else {

          System.out.println("Cuanto desarmado: " + getOutgoingTransactionCost());

          return getOutgoingTransactionCost();
        }

      } else {
        System.out.println("Cuanto desarmado: " + getOutgoingTransactionCost());

        return getOutgoingTransactionCost();
      }
    }

    System.out.println("Cuanto desarmado 3: " + getOutgoingTransactionCost());

    return getOutgoingTransactionCost();

  }

  /**
   * Calculates the cost of a produced BOM product. Its cost is the sum of the used products
   * transactions costs. If these has not been calculated yet they are calculated.
   * 
   * @return BigDecimal object representing the total cost amount of the transaction.
   * @throws ImportException
   * @throws OBException
   */
  protected BigDecimal getBOMProductCost() throws OBException, ImportException {


  	if(transaction.getMovementType().equals("M+")  ||  transaction.getMovementType().equals("M-") ){
  		return BigDecimal.ZERO;
  	}
  	
    List<ProductionLine> productionLines = transaction.getProductionLine().getProductionPlan().getManufacturingProductionLineList();
    // Remove produced BOM line.
    List<ProductionLine> parts = new ArrayList<ProductionLine>(productionLines);
    parts.remove(transaction.getProductionLine());
    //System.out.println("part size: " + parts.size());
    //SI ES DESARMADO, PUEDE HABER VARIAS LINEAS EN NEGATIVO, PERO TODAS DEL MISMO PRODUCTO
    double proporcion = 1.0;
    if(transaction.getProductionLine().getProductionPlan().getProduction().isSwaDesarmado()){
	    BigDecimal qty = transaction.getMovementQuantity();
	    List<ProductionLine> lsProdLines = new ArrayList<ProductionLine>();
	    BigDecimal qtyTotal = new BigDecimal(0);  qtyTotal = qtyTotal.add(qty);
    	for (int i=0; i<parts.size(); i++) {
	    	ProductionLine line = parts.get(i);
	    	if(line.getMovementQuantity().compareTo(BigDecimal.ZERO)<0){
	    		qtyTotal = qtyTotal.add(line.getMovementQuantity());
	    		lsProdLines.add(line);
	    	}
	    }
    	
    	proporcion = qty.divide(qtyTotal, 8, RoundingMode.HALF_UP).doubleValue();
    	//System.out.println("MI PROPORCION ES DE 1:" + proporcion);
    	for(int i=0; i<lsProdLines.size(); i++){
    		parts.remove(lsProdLines.get(i));
    	}
    }
    
    BigDecimal totalCost = BigDecimal.ZERO;
    
    //System.out.println("SE VA A PASAR A = Z " + parts.get(0).getMovementQuantity());
    
    for (ProductionLine prodLine : parts) {
     
    //Obtener transaction de la linea que no este asociada a M+ o M-
    	List<MaterialTransaction> lsTrans =prodLine.getMaterialMgmtMaterialTransactionList();
    	MaterialTransaction partTransaction =null;
    	for(int j=0; j<lsTrans.size(); j++){
    		partTransaction = lsTrans.get(j);
    		if(partTransaction.getMovementType().equals("M+") || partTransaction.getMovementType().equals("M-")){
    			continue;
    		}
    		break;
    	}
    	
      //VAFASTER: Hay productos que son tipo Servicios y que estan en el armado o desarmado
      //Al procesarlos, no generan m_transaction, entonces agrege el sgt IF 
      if(partTransaction == null)
    	  continue;
      
     // Calculate transaction cost if it is not calculated yet.
      Product product = partTransaction.getProduct();
      if (product.getProductType().equals("I") && (product.isStocked() || product.isBillOfMaterials())) {
        BigDecimal trxCost = CostingUtils.getTransactionCost(partTransaction, transaction.getTransactionProcessDate(), true, costCurrency);
        if (trxCost == null) {
           throw new OBException("@NoCostCalculated@: " + partTransaction.getIdentifier());
        	
        }
        totalCost = totalCost.add(trxCost);

      } else {
                
      }
    }
    //System.out.println("OK SEGUIMOS ENTRAR");
    
    BigDecimal serviceCost = new BigDecimal(0);
    ProductionPlan prodplan = transaction.getProductionLine().getProductionPlan();
    if(prodplan.getSWALineaDeOrdenPorServicio()!=null){
    	List<InvoiceLine> lsInvlines = prodplan.getSWALineaDeOrdenPorServicio().getInvoiceLineList();
    	
    	if(lsInvlines.size()==0) throw new ImportException("");
    	for(int i=0; i<lsInvlines.size(); i++){
    		InvoiceLine invline = lsInvlines.get(i);
    		serviceCost = serviceCost.add(FinancialUtils.getConvertedAmount(invline.getLineNetAmount(), invline.getInvoice().getCurrency(), costCurrency, transaction.getMovementDate(), costOrg, FinancialUtils.PRECISION_COSTING));
    	}
    }
    
    //System.out.println("proporcion: " + proporcion + " - serviceCost: " + serviceCost);
    return totalCost.multiply(new BigDecimal(proporcion)).add(serviceCost);
  }

  /**
   * <p>
   * The Manufacturing cost is not fully migrated to the new costing engine. <b>This method must not
   * be overwritten by algorithms.</b>
   * </p>
   * Gets the cost of the manufacturing transaction. It calculates the cost of the Work Effort when
   * needed.
   * 
   * @return BigDecimal object representing the total cost amount of the transaction.
   */
  private BigDecimal getManufacturingProducedCost() {
    if (!transaction.getProductionLine().isCalculated()) {
      calculateWorkEffortCost(transaction.getProductionLine().getProductionPlan().getProduction());
    }
    OBDal.getInstance().refresh(transaction.getProductionLine());
    return transaction.getProductionLine().getEstimatedCost() != null ? transaction.getProductionLine().getEstimatedCost().multiply(transaction.getMovementQuantity().abs()) : BigDecimal.ZERO;
  }

  /**
   * <p>
   * The Manufacturing cost is not fully migrated to the new costing engine. <b>This method must not
   * be overwritten by algorithms.</b>
   * </p>
   * Calculates the cost of a consumed product in a Work Effort using by default the
   * {@link #getOutgoingTransactionCost()} method as a regular outgoing transaction.
   * 
   * @return BigDecimal object representing the total cost amount of the transaction.
   */
  private BigDecimal getManufacturingConsumedCost() {
    if (!transaction.getProductionLine().isCalculated()) {
      calculateWorkEffortCost(transaction.getProductionLine().getProductionPlan().getProduction());
    }
    OBDal.getInstance().refresh(transaction.getProductionLine());
    return transaction.getProductionLine().getEstimatedCost() != null ? transaction.getProductionLine().getEstimatedCost() : BigDecimal.ZERO;
  }

  private void calculateWorkEffortCost(ProductionTransaction production) {

    try {
      List<Object> params = new ArrayList<Object>();
      params.add(production.getId());
      params.add((String) DalUtil.getId(OBContext.getOBContext().getUser()));
      CallStoredProcedure.getInstance().call("MA_PRODUCTION_COST", params, null, true, false);

    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      throw new IllegalStateException(e);
    }

  }

  protected BigDecimal getDefaultCost()  throws ImportException {
    Costing stdCost = CostingUtils.getStandardCostDefinition(transaction.getProduct(), costOrg, transaction.getTransactionProcessDate(), costDimensions);
    BusinessPartner bp = CostingUtils.getTrxBusinessPartner(transaction, trxType);
    PriceList pricelist = null;
    if (bp != null) {
      pricelist = bp.getPurchasePricelist();
    }
    ProductPrice pp = FinancialUtils.getProductPrice(transaction.getProduct(), transaction.getMovementDate(), false, pricelist, false);
    if (stdCost == null && pp == null) {
      throw new OBException("@NoPriceListOrStandardCostForProduct@ @Organization@: " + costOrg.getName() + ", @Product@: " + transaction.getProduct().getName() + ", @Date@: " + OBDateUtils.formatDate(transaction.getTransactionProcessDate()));
    } else if (stdCost != null && pp == null) {
      return getTransactionStandardCost();
    } else if (stdCost == null && pp != null) {
      return getPriceListCost();
    } else if (stdCost != null && pp != null && stdCost.getStartingDate().before(pp.getPriceListVersion().getValidFromDate())) {
      return getPriceListCost();
    } else {
      return getTransactionStandardCost();
    }
  }

  /**
   * Calculates the transaction cost based on the Standard Cost of the product on the Transaction
   * Process Date.
   * 
   * @return BigDecimal object representing the total cost amount of the transaction.
   */
  protected BigDecimal getTransactionStandardCost() {
    BigDecimal standardCost = CostingUtils.getStandardCost(transaction.getProduct(), costOrg, transaction.getTransactionProcessDate(), costDimensions, costCurrency);
    return transaction.getMovementQuantity().abs().multiply(standardCost);
  }

  /**
   * Calculates the transaction cost based on the purchase price list of the product. It searches
   * first for a default price list and if none exists takes one.
   * 
   * @return BigDecimal object representing the total cost amount of the transaction.
   * @throws OBException
   *           when no PriceList is found for the product.
   */
  protected BigDecimal getPriceListCost() {
    BusinessPartner bp = CostingUtils.getTrxBusinessPartner(transaction, trxType);
    PriceList pricelist = null;
    if (bp != null) {
      pricelist = bp.getPurchasePricelist();
    }
    ProductPrice pp = FinancialUtils.getProductPrice(transaction.getProduct(), transaction.getMovementDate(), false, pricelist);
    BigDecimal cost = pp.getStandardPrice().multiply(transaction.getMovementQuantity().abs());
    if (DalUtil.getId(pp.getPriceListVersion().getPriceList().getCurrency()).equals(costCurrency.getId())) {
      // no conversion needed
      return cost;
    }
    return FinancialUtils.getConvertedAmount(cost, pp.getPriceListVersion().getPriceList().getCurrency(), costCurrency, transaction.getMovementDate(), costOrg, FinancialUtils.PRECISION_COSTING);
  }

  /**
   * @return the base currency used to calculate all the costs.
   */
  public Currency getCostCurrency() {
    return costCurrency;
  }

  /**
   * Dimensions available to manage the cost on an entity.
   */
  public enum CostDimension {
    Warehouse
  }
}
