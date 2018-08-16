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
 * All portions are Copyright (C) 2001-2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_callouts;

import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.plm.AttributeSet;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;

public class SL_Movement_Product extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

	// Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
    // { System.out.println(params.nextElement()); }  
	  
	// Locator
	  
    String strLocator = info.vars.getStringParameter("inpmProductId_LOC");

    if (strLocator.startsWith("\"")) {
      strLocator = strLocator.substring(1, strLocator.length() - 1);
    }
    info.addResult("inpmLocatorId", strLocator);
    info.addResult("inpmLocatorId_R",
        SLInOutLineProductData.locator(this, strLocator, info.vars.getLanguage()));

    // UOM

    String strUOM = info.vars.getStringParameter("inpmProductId_UOM");
    info.addResult("inpcUomId", strUOM);

    // Attributes

    String strAttribute = info.vars.getStringParameter("inpmProductId_ATR");

    if (strAttribute.startsWith("\"")) {
      strAttribute = strAttribute.substring(1, strAttribute.length() - 1);
    }
    info.addResult("inpmAttributesetinstanceId", strAttribute);
    info.addResult("inpmAttributesetinstanceId_R",
        SLInOutLineProductData.attribute(this, strAttribute));

    // Attribute set

    String strMProductID = info.vars.getStringParameter("inpmProductId");
    String strAttrSet = "";
    String strAttrSetValueType = "";
    Double qtyReservedLocator = 0.0;
    Double qtyavailable = 0.0;

    OBContext.setAdminMode();
    try {
      final Product product = OBDal.getInstance().get(Product.class, strMProductID);
      if (product != null) {
        AttributeSet attributeset = product.getAttributeSet();
        if (attributeset != null) {
          strAttrSet = product.getAttributeSet().toString();
        }
        strAttrSetValueType = product.getUseAttributeSetValueAs();
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    info.addResult("inpattributeset", strAttrSet);
    info.addResult("inpattrsetvaluetype", strAttrSetValueType);

    // Movement qty

    String strQtyOrder = info.vars.getNumericParameter("inpmProductId_PQTY");
    String strQty = info.vars.getNumericParameter("inpmProductId_QTY");

    info.addResult("inpmovementqty", StringUtils.isEmpty(strQty) ? "\"\"" : (Object) strQty);
    info.addResult("inpquantityorder", StringUtils.isEmpty(strQtyOrder) ? "\"\""
        : (Object) strQtyOrder);

    //Vafaster
     String stradclientId = info.vars.getStringParameter("inpadClientId");
     String strWarehouseId = info.vars.getStringParameter("inpemSwaMWarehouseId");
     String strMovementId = info.vars.getStringParameter("inpmMovementId");
     Warehouse warehouseobj = OBDal.getInstance().get(Warehouse.class, strWarehouseId.trim());
     String StrBinAnul = "";
     String StrBinReceipt = "";
     if(warehouseobj.getSwaAnnulLocator()!=null)
    	 StrBinAnul= warehouseobj.getSwaAnnulLocator().getId();
     if(warehouseobj.getSwaReceiptLocator() != null)
    	 StrBinReceipt = warehouseobj.getSwaReceiptLocator().getId();
     
     //Primera Consulta donde el producto determinarà el bin de preferencia destino el cual 
     //es el que tiene mas movimiento de salida en el ùltimo año, ademas se prioriza por el tipo de bin. 
     //Salida _ Stock, ademàs por prioridad y al ùltimo por la cantidad de movimiento del producto
     //Cabe resaltar que no se toma en cuenta si el bin está vacio o no.
     Query q = OBDal.getInstance().getSession().createSQLQuery("SELECT mt.m_locator_id, coalesce(sum(mt.movementqty),0) as sumqty, ml.em_obwhs_type, ml.priorityno "
              + "  FROM m_transaction mt "
              + "       LEFT  JOIN M_locator ml "
              + "               ON ml.m_locator_id= mt.m_locator_id "
              + "   WHERE mt.m_product_id = '" +strMProductID.trim()+ "' "
              + "     AND ml.m_warehouse_id= '" + strWarehouseId.trim() + "' "
              + "     AND  mt.movementqty<0 "
              + "     AND  mt.created between now()-365 and now()"
              + "     AND  ml.m_locator_id NOT IN (SELECT m_locatorto_id "
              + "                                    FROM m_movementline  " 
              + "                                    WHERE m_movement_id = '" + strMovementId.trim() + "' ) "
              + "     AND  ml.m_locator_id NOT IN ( '" + StrBinAnul+"','"+StrBinReceipt+ "','"+ strLocator+ "')"
 
              + "  GROUP BY mt.m_locator_id,ml.em_obwhs_type,ml.priorityno "
        	  + "  ORDER BY ml.em_obwhs_type asc ,ml.priorityno asc , sumqty asc LIMIT 1;");
     
     List<Object> results= q.list();
     if(results.size()>0){
    	 Object[] obj = (Object[]) results.get(0);
    	 String strTOLocatorPreferedID = obj[0].toString(); //obj[0] locatorid from query
    	 info.addResult("inpmLocatortoId", strTOLocatorPreferedID);
     }
     else{
    	// procedemos con los bins que no tienen nada pero en el mismo
         //orden, quiere decir bin Salida, Stock y luego por prioridad, eso si los bin no deben tener nada en 
         //su locator m_storage_detail.
    	 Query q2 = OBDal.getInstance().getSession().createSQLQuery(" SELECT ml.m_locator_id, ml.value , ml.em_obwhs_type, ml.priorityno "
                 + "  FROM m_storage_detail std  "
                 + "       RIGHT JOIN m_locator ml "
                 + "               ON std.m_locator_id = ml.m_locator_id  "
                 + "       INNER JOIN m_warehouse wr  "
                 + "               ON  ml.m_warehouse_id = wr.m_warehouse_id "
                 + " WHERE wr.m_warehouse_id =  '" + strWarehouseId.trim() + "' "
                 + "     AND ml.m_locator_id NOT IN (SELECT m_locatorto_id "
                 + "                                   FROM m_movementline  "
                 + "                                    WHERE m_movement_id = '" + strMovementId.trim() + "' " 
                 + "                                  ) "
                 + "     AND ml.m_locator_id NOT IN ( '" + StrBinAnul+"','"+StrBinReceipt+ "','"+ strLocator+ "')"
                 + "  GROUP BY ml.m_locator_id , ml.value ,ml.em_obwhs_type, ml.priorityno "
           	     + "  HAVING COALESCE(sum(std.qtyonhand),0) = 0 " 
                 +"   ORDER BY ml.em_obwhs_type asc, ml.priorityno LIMIT 1;");
        
        List<Object> results2= q2.list();
        if(results2.size()>0){
       	 Object[] objt = (Object[]) results2.get(0);
       	 String strTOLocatorPreferedID = objt[0].toString(); //objt[0] locatorid from query
       	 info.addResult("inpmLocatortoId", strTOLocatorPreferedID);
        }
     }
     
     //Verificando la cantidad en Stock Reservado 
     Locator locatorObj = OBDal.getInstance().get(Locator.class, strLocator);
     Product productobj = OBDal.getInstance().get(Product.class, strMProductID);
     if(locatorObj != null)
      qtyReservedLocator = getQty(null, locatorObj, productobj, 4);
     info.addResult("inpemSwaQtyreservedStorage", qtyReservedLocator);
     //qtyavailable =  Double.parseDouble(strQty.replaceAll(",", "")) - qtyReservedLocator;
     qtyavailable =  getQty(null, locatorObj, productobj, 2);
     info.addResult("inpemSwaQtyavailableStorage", qtyavailable);
     
     
     // END Vafaster
     
     
    // Secondary UOM

    String strPUOM = info.vars.getStringParameter("inpmProductId_PUOM");

    String strHasSecondaryUOM = SLOrderProductData.hasSecondaryUOM(this, strMProductID);
    info.addResult("inphasseconduom", (Object) strHasSecondaryUOM);

    if (strPUOM.startsWith("\"")) {
      strPUOM = strPUOM.substring(1, strPUOM.length() - 1);
    }

    FieldProvider[] tld = null;
    try {
      ComboTableData comboTableData = new ComboTableData(info.vars, this, "TABLE", "",
          "M_Product_UOM", "", Utility.getContext(this, info.vars, "#AccessibleOrgTree",
              "SLMovementProduct"), Utility.getContext(this, info.vars, "#User_Client",
              "SLMovementProduct"), 0);
      Utility.fillSQLParameters(this, info.vars, null, comboTableData, "SLOrderProduct", "");
      tld = comboTableData.select(false);
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    if (tld != null && tld.length > 0) {
      info.addSelect("inpmProductUomId");
      for (int i = 0; i < tld.length; i++) {
        info.addSelectResult(tld[i].getField("id"), tld[i].getField("name"), tld[i].getField("id")
            .equalsIgnoreCase(strPUOM));
      }
      info.endSelect();
    } else {
      info.addResult("inpmProductUomId", null);
    }

    // displayLogic

    info.addResult("EXECUTE", "displayLogic();");
  }
  
  private Double getQty(Warehouse warehouse, Locator locator, Product product, int x){
	  Double val = 0.0;
	  String SqlQuery = "";
	  if(x==1){//GET QTY TOTAL
		  SqlQuery= " SELECT COALESCE(sum(qtyonhand),0) FROM swa_product_warehouse_v  "
		  		  + "WHERE m_product_id = '" + product.getId()
		  		  + "' AND m_warehouse_id = '" + warehouse.getId() + "'";
		  
	  }
	  else if(x==2){//Get QTY on Locator
		  SqlQuery=" SELECT COALESCE(sum(qtyonhand),0) FROM swa_product_by_anaquel_v "
		  		+ "  WHERE M_PRODUCT_ID = '"+ product.getId() + "' "
		  	    + "    AND M_LOCATOR_ID = '"+ locator.getId() + "' "
		  	    + " GROUP BY m_locator_id ";
	  }
	  else if(x==3){//Get Qty Total Reserved 
		  SqlQuery=" SELECT COALESCE(sum(qtyreserved),0) FROM swa_product_warehouse_v "
			  		+ "  WHERE M_PRODUCT_ID = '"+ product.getId() + "' "
			  	    + "    AND m_warehouse_id = '"+ warehouse.getId() + "' ";
		  
	  }
	  else if(x==4){//Get Qty Reserved on Locator
		
		  
		  SqlQuery=" SELECT COALESCE(sum(reserved),0)"
			  		+ "  FROM swa_product_by_anaquel_v "
			  	    + "  WHERE M_PRODUCT_ID = '"+ product.getId() + "' "
			  	    + "    AND  m_locator_id  = '"+ locator.getId()  + "' ";
		  
		 // System.out.println(SqlQuery);
	  }
	  
	  try {
		 Query q = OBDal.getInstance().getSession().createSQLQuery(SqlQuery);
		 BigDecimal result = (BigDecimal) q.uniqueResult();
		 if (result != null) {
		        return result.doubleValue();
		 }
		 return 0.0;
	  } catch (Exception e) {
		return val;
	  }
  }
}
