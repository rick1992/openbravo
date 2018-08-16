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
import org.openbravo.model.common.order.Order;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.warehouse.pickinglist.PickingList;
     
     /**
    32  * Sums the orders passed in through a json array and returns the result.
    33  */
     public class viewListActionHandler extends BaseActionHandler {
     
       protected JSONObject execute(Map<String, Object> parameters, String data) {
         try {
        
           // get the data as json
           final JSONObject jsonData = new JSONObject(data);
           final JSONArray orderIds = jsonData.getJSONArray("orders");
     
           // start with zero
           BigDecimal total = new BigDecimal("0");
           float Peso=0;
           float Cubicaje=0;
           float Cajas=0;
           // iterate over the orderids
           for (int i = 0; i < orderIds.length(); i++) {
             final String orderId = orderIds.getString(i);
     
         //    final Order order = OBDal.getInstance().get(Order.class, orderId);
             final ShipmentInOut order = OBDal.getInstance().get(ShipmentInOut.class, orderId);
                 Peso = Peso +  order.getSwaPesoGuiaTotal().floatValue();
                 Cubicaje = Cubicaje +  order.getSwaCubicajeGuiaTotal().floatValue();
                 Cajas = Cajas +  order.getSwaNumcajasGuiaTotal().floatValue();
             //List<ShipmentInOutLine> orderLine = order.getMaterialMgmtShipmentInOutLineList();
             //for(int j=0; j<orderLine.size(); j++){
            	// Peso = Peso + orderLine.get(j).getSwaPesoPerline().floatValue();
            	//Cubicaje = Cubicaje +  orderLine.get(j).getSwaCubicajePerline().floatValue() + orderLine.get(j).getSwaCubicajeextraPerline().floatValue();
            	// Cajas = Cajas + orderLine.get(j).getSwaNumcajasintPerline().floatValue() + orderLine.get(j).getSwaCajasextrasPerline().floatValue();
             //}
           }
           String s = Float.toString(Cajas);
           //System.out.println(s);
           JSONObject json = new JSONObject();
           json.put("total", Peso);
           json.put("cubicaje",Cubicaje);
           json.put("cajas",Double.parseDouble(s));
           //json.put("cajas",new BigDecimal(Cajas));
         //  System.out.print(json);
           return json;
         } catch (Exception e) {
           throw new OBException(e);
         }
       }
     }