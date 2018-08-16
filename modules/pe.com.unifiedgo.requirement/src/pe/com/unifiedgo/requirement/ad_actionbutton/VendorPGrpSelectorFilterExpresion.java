package pe.com.unifiedgo.requirement.ad_actionbutton;

import java.util.Map;

import org.openbravo.client.application.FilterExpression;
import org.openbravo.dal.core.OBContext;
import org.openbravo.model.ad.access.User;

public class VendorPGrpSelectorFilterExpresion implements FilterExpression {

  @Override
  public String getExpression(Map<String, String> requestMap) {

    // System.out.println("ADIsOrgIncluded filter expression:");
    // Set<String> keyset = requestMap.keySet();
    // String[] keys = new String[keyset.size()];
    // keyset.toArray(keys);
    // for (int i = 0; i < keys.length; i++) {
    // System.out.println(keys[i] + ":" + requestMap.get(keys[i]));
    // }

    User user = OBContext.getOBContext().getUser();
    String adUserId = user.getId(), cBPartnerId = "";
    boolean restrictbp = false;
    // for selector in reports
    String restrictbpid = requestMap.get("restrictbpid") != null ? requestMap.get("restrictbpid")
        : "";
    if (restrictbpid.compareTo("") != 0)
      restrictbp = true;
    if (restrictbp) {
      cBPartnerId = requestMap.get(restrictbpid) != null ? requestMap.get(restrictbpid) : "";
    } else {
      // for product window
      cBPartnerId = requestMap.get("inpemSwaPrimaryPartnerId") != null ? requestMap
          .get("inpemSwaPrimaryPartnerId") : "";
    }

    StringBuilder whereClause = new StringBuilder();

    whereClause.append("i.businessPartner.id='" + cBPartnerId + "' ");

    return whereClause.toString();
  }
}
