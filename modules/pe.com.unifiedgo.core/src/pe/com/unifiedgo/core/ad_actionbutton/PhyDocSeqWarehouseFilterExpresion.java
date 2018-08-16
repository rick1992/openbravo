package pe.com.unifiedgo.core.ad_actionbutton;

import java.util.Map;

import org.openbravo.client.application.FilterExpression;

public class PhyDocSeqWarehouseFilterExpresion implements FilterExpression {

  @Override
  public String getExpression(Map<String, String> requestMap) {

    // System.out.println("ADIsOrgIncluded filter expression:");
    // Set<String> keyset = requestMap.keySet();
    // String[] keys = new String[keyset.size()];
    // keyset.toArray(keys);
    // for (int i = 0; i < keys.length; i++) {
    // System.out.println(keys[i] + ":" + requestMap.get(keys[i]));
    // }

    String org_warehouse_id = requestMap.get("inporgwarehouseId");
    String ad_user_id = requestMap.get("inpadUserId");

    StringBuilder whereClause = new StringBuilder();

    whereClause.append("e.warehouse.organization.id='" + org_warehouse_id + "' ");
    whereClause.append("and e.userContact.id='" + ad_user_id + "'");

    return whereClause.toString();
  }
}
