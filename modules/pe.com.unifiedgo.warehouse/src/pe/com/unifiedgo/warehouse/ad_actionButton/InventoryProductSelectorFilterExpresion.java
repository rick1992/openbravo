package pe.com.unifiedgo.warehouse.ad_actionButton;

import java.util.Map;
import java.util.Set;

import org.openbravo.client.application.FilterExpression;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.materialmgmt.transaction.InternalMovement;
import org.openbravo.model.materialmgmt.transaction.InventoryCount;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;

public class InventoryProductSelectorFilterExpresion implements FilterExpression {

  @Override
  public String getExpression(Map<String, String> requestMap) {
    
  
	  String strInventoryId = requestMap.get("inpmInventoryId") != null ? requestMap.get("inpmInventoryId") : "";
	  String strWarehouse = "";

	  InventoryCount inventory = OBDal.getInstance().get(InventoryCount.class, strInventoryId);
	  
	  if(inventory !=null)
		  strWarehouse = inventory.getWarehouse().getId();
   
	  StringBuilder whereClause = new StringBuilder();

	
	
	if(!strWarehouse.equals("")){
		whereClause.append("e.stocked = false or  e.warehouse.id = '"+ strWarehouse + "'" );
	}else{
		whereClause.append("1=1" );
	}

	return whereClause.toString();
    
    
  }
}