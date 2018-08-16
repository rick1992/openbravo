package pe.com.unifiedgo.sales.ad_actionButton;

import java.util.Map;

import org.openbravo.client.application.FilterExpression;
import org.openbravo.dal.core.OBContext;
import org.openbravo.model.ad.access.User;

public class ProspectProjectFormSelectorFilterExpresion implements FilterExpression {

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
    String strssaProjpropContractId = requestMap.get("inpKey") != null ? requestMap.get("inpKey")
        : "";

    StringBuilder whereClause = new StringBuilder();

    // Project Validation
    whereClause.append(
        "e.id in (select pl.project.id from SSA_ProjProp_Contract_Line pl where pl.sSAProjpropContract.id='"
            + strssaProjpropContractId + "')");

    return whereClause.toString();
  }
}
