package pe.com.unifiedgo.core.ad_actionbutton;

import java.util.Map;

import org.openbravo.client.application.FilterExpression;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;

public class WarehouseFormSelectorFilterExpresion implements FilterExpression {

  @Override
  public String getExpression(Map<String, String> requestMap) {

    /*
     * System.out.println("ADIsOrgIncluded filter expression:"); Set<String> keyset =
     * requestMap.keySet(); String[] keys = new String[keyset.size()]; keyset.toArray(keys); for
     * (int i = 0; i < keys.length; i++) { System.out.println(keys[i] + ":" +
     * requestMap.get(keys[i])); }
     */

    User user = OBContext.getOBContext().getUser();
    String adUserId = user.getId();
    String excludePermissions = requestMap.get("excludepermissions")!=null ? requestMap.get("excludepermissions") : "N";
    
    
    
    boolean restrictorg = false;
    String restrictadorgid = requestMap.get("restrictadorgid") != null ? requestMap.get("restrictadorgid") : "";
    if (restrictadorgid.compareTo("") != 0)
      restrictorg = true;
    String adOrgId = requestMap.get(restrictadorgid) != null ? requestMap.get(restrictadorgid) : "";

    StringBuilder whereClause = new StringBuilder();
      
    if (restrictorg) {
      String orgList = Utility.getInStrSet(OBContext.getOBContext().getOrganizationStructureProvider().getNaturalTree(adOrgId));

      if (!orgList.isEmpty()) {
        whereClause.append("e.organization.id in (" + orgList + ")");
      } else {
        whereClause.append("e.organization.id in ('')");
      }

    }
//System.out.println("excludePermissions: " + excludePermissions);
    // Warehouse Permissions validation
    if (excludePermissions.equals("N")){
      if (whereClause.length() != 0)
        whereClause.append(" and ");
      whereClause.append("((select count(PW.id) from swa_permissions_warehouse PW where PW.userContact.id = '" + adUserId + "' AND PW.permit=true)=0 or e.id IN (select PW.warehouse.id from swa_permissions_warehouse PW where PW.userContact.id = '" + adUserId + "' AND PW.permit = true))");
    }
    return whereClause.toString();

  }
}
