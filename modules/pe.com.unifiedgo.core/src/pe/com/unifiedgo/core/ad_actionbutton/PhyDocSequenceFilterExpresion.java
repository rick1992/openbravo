package pe.com.unifiedgo.core.ad_actionbutton;

import java.util.Map;

import org.openbravo.client.application.FilterExpression;

public class PhyDocSequenceFilterExpresion implements FilterExpression {

  @Override
  public String getExpression(Map<String, String> requestMap) {

    // System.out.println("ADIsOrgIncluded filter expression:");
    // Set<String> keyset = requestMap.keySet();
    // String[] keys = new String[keyset.size()];
    // keyset.toArray(keys);
    // for (int i = 0; i < keys.length; i++) {
    // System.out.println(keys[i] + ":" + requestMap.get(keys[i]));
    // }

    String phydoc_sequence_id = requestMap.get("inpKey");
    String org_warehouse_id = requestMap.get("inporgwarehouseId");

    // BILLPhyDocSequence phydoc_seq = OBDal.getInstance().get(BILLPhyDocSequence.class,
    // phydoc_sequence_id);

    StringBuilder whereClause = new StringBuilder();

    whereClause.append("e.organization.id='" + org_warehouse_id + "'");

    return whereClause.toString();
  }
}
