package pe.com.unifiedgo.core.ad_actionbutton;

import java.util.Map;

import org.openbravo.client.application.FilterExpression;
import org.openbravo.dal.core.OBContext;
import org.openbravo.model.ad.access.User;

public class ElementValueFormSelectorFilterExpresion implements FilterExpression {

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
    String restrictcacctschemaid = requestMap.get("restrictcacctschemaid") != null ? requestMap.get("restrictcacctschemaid") : "";
    String cAcctschemaId = requestMap.get(restrictcacctschemaid) != null ? requestMap.get(restrictcacctschemaid) : "";

    StringBuilder whereClause = new StringBuilder();

    whereClause.append("exists(from FinancialMgmtAcctSchemaElement se where se.accountingElement.id = e.accountingElement.id and se.accountingSchema.id='" + cAcctschemaId + "')");

    return whereClause.toString();

  }
}
