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
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.materialmgmt.transaction.ProductionPlan;

import pe.com.unifiedgo.warehouse.data.SwaProductionPlanDetail;



     
     /**
    32  * Sums the orders passed in through a json array and returns the result.
    33  */
     public class viewUpdateQtyProduct extends BaseActionHandler {
     
       protected JSONObject execute(Map<String, Object> parameters, String data) {
         try {
           // get the data as json
           final JSONObject jsonData = new JSONObject(data);
           final JSONArray LinesIds = jsonData.getJSONArray("orders");
     
           // start with zero
           
           ProductionPlan pplan_tst = OBDal.getInstance().get(ProductionPlan.class, LinesIds.get(0));
           SwaProductionPlanDetail pplanDetail_test = OBDal.getInstance().get(SwaProductionPlanDetail.class, LinesIds.get(0));
           
           if(pplan_tst != null){
        	   for(int i=0;i<LinesIds.length();i++){
        		   ProductionPlan pplan = OBDal.getInstance().get(ProductionPlan.class, LinesIds.get(i));
        		   pplan.setSWACantidadDisponible(SWA_Utils.getWarehouseStockInfo(pplan.getProduction().getSWAWarehouse(), null, pplan.getProduct(), 3));
        		   OBDal.getInstance().save(pplan);
        	   }
           }else if (pplanDetail_test!= null){
        	   
        	   for(int i=0;i<LinesIds.length();i++){
        		   SwaProductionPlanDetail pplanDetail = OBDal.getInstance().get(SwaProductionPlanDetail.class, LinesIds.get(i));
        		   pplanDetail.setCantDisponible(SWA_Utils.getWarehouseStockInfo(pplanDetail.getProductionPlan().getProduction().getSWAWarehouse(), null, pplanDetail.getProduct(), 3));
        		  // pplanDetail.set (SWA_Utils.getWarehouseStockInfo(pplanDetail.getProductionPlan().getProduction().getSWAWarehouse(), null, pplanDetail.getProduct(), 3));
        	   }
           }
           
          
           
          
           JSONObject json = new JSONObject();
           return json;
         } catch (Exception e) {
           throw new OBException(e);
         }
       }
     }