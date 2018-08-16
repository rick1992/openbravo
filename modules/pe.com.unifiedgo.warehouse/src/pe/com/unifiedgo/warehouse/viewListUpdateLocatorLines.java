/*
     2  *************************************************************************
     3  * The contents of this file are subject to the Openbravo  Public  License
     4  * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
     5  * Version 1.1  with a permitted attribution clause; you may not  use this
     6  * file except in compliance with the License. You  may  obtain  a copy of
     7  * the License at http://www.openbravo.com/legal/license.html 
     8  * Software distributed under the License  is  distributed  on  an "AS IS"
     9  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
    10  * License for the specific  language  governing  rights  and  limitations
    11  * under the License. 
    12  * The Original Code is Openbravo ERP. 
    13  * The Initial Developer of the Original Code is Openbravo SLU 
    14  * All portions are Copyright (C) 2011 Openbravo SLU 
    15  * All Rights Reserved. 
    16  * Contributor(s):  __________
    17  ************************************************************************
    18  */
     package pe.com.unifiedgo.warehouse;
     
     import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
     








     import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.warehouse.pickinglist.PickingList;

import pe.com.unifiedgo.core.data.SCRComboItem;
     
     /**
    32  * Sums the orders passed in through a json array and returns the result.
    33  */
     public class viewListUpdateLocatorLines extends BaseActionHandler {
     
       protected JSONObject execute(Map<String, Object> parameters, String data) {
         try {
        
           // get the data as json
           final JSONObject jsonData = new JSONObject(data);
           final JSONArray LinesIds = jsonData.getJSONArray("orders");
     
           // start with zero
           BigDecimal total = new BigDecimal("0");
           float Peso=0;
           float Cubicaje=0;
           float Cajas=0;
           // iterate over the orderids
           
           ShipmentInOutLine primeralinea = OBDal.getInstance().get(ShipmentInOutLine.class, LinesIds.getString(0));
           final ShipmentInOut Order = OBDal.getInstance().get(ShipmentInOut.class, primeralinea.getShipmentReceipt().getId());
           OBCriteria<Locator> LocatorsByWarehouse = OBDal.getInstance().createCriteria(Locator.class);
           LocatorsByWarehouse.add(Restrictions.eq(Locator.PROPERTY_CLIENT, Order.getClient()));
           LocatorsByWarehouse.add(Restrictions.eq(Locator.PROPERTY_WAREHOUSE, Order.getWarehouse()));
           LocatorsByWarehouse.addOrder(org.hibernate.criterion.Order.asc(Locator.PROPERTY_OBWHSTYPE));
           LocatorsByWarehouse.addOrder(org.hibernate.criterion.Order.asc(Locator.PROPERTY_RELATIVEPRIORITY));
           
           List<Locator> ListLocator = LocatorsByWarehouse.list();
           if(ListLocator.size()<1){
        	   String s = Float.toString(Cajas);
               JSONObject json = new JSONObject();
               return json; 
           }
           
        
           
           
           for (int i = 0; i < LinesIds.length(); i++) {
        	 Locator LocatorBin_Asign = null;
             final String LineId = LinesIds.getString(i);
                ShipmentInOutLine lineaselect = OBDal.getInstance().get(ShipmentInOutLine.class, LineId);
                 for(int j=0;j<ListLocator.size();j++){
                	
                	 
                	 OBCriteria<StorageDetail> StDetail = OBDal.getInstance().createCriteria(StorageDetail.class);
                	 StDetail.add(Restrictions.eq(StorageDetail.PROPERTY_CLIENT, Order.getClient()));
                	 StDetail.add(Restrictions.eq(StorageDetail.PROPERTY_PRODUCT, lineaselect.getProduct()));
                	 StDetail.add(Restrictions.eq(StorageDetail.PROPERTY_STORAGEBIN, ListLocator.get(j)));
                	 StDetail.add(Restrictions.eq(StorageDetail.PROPERTY_CLIENT, lineaselect.getClient()));
                	 StDetail.add(Restrictions.sqlRestriction(" qtyonhand > 0 "));
                	 
                	 List<StorageDetail> ListStDetail = StDetail.list();
                	 if(ListStDetail.size()>0){
                		 LocatorBin_Asign=ListLocator.get(j);
                		 break;
                	 }
                	 
                	// System.out.println(ListLocator.get(j).getSearchKey());  	 
                 }
                 
                 if(LocatorBin_Asign!= null){
                	 lineaselect.setStorageBin(LocatorBin_Asign);
                	 OBDal.getInstance().save(lineaselect);
                 }
           }
           JSONObject json = new JSONObject();
           return json;
         } catch (Exception e) {
           throw new OBException(e);
         }
       }
     }