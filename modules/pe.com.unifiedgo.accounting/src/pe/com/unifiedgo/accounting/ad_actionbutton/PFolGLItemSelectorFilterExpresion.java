package pe.com.unifiedgo.accounting.ad_actionbutton;

import java.util.Map;

import org.openbravo.client.application.FilterExpression;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;

public class PFolGLItemSelectorFilterExpresion implements FilterExpression {

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

    String adOrgId = requestMap.get("inpadOrgId") != null ? requestMap.get("inpadOrgId") : "";

    StringBuilder whereClause = new StringBuilder();

    String orgList = Utility.getInStrSet(OBContext.getOBContext().getOrganizationStructureProvider().getNaturalTree(adOrgId));

    if (!orgList.isEmpty()) {
      whereClause.append("e.organization.id in (" + orgList + ")");
    } else {
      whereClause.append("e.organization.id in ('')");
    }

    if (whereClause.length() != 0)
      whereClause.append(" and ");
    whereClause.append("e.scoScoGlitemCategory.searchKey = 'pago'");

    return whereClause.toString();

  }
}
