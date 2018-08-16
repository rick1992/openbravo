package pe.com.unifiedgo.warehouse.ad_actionButton;

import java.util.Map;

import org.openbravo.client.application.FilterExpression;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;

public class RequisitionFormSelectorFilterExpresion implements FilterExpression {

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

    boolean restrictorg = false;
    String restrictadorgid = requestMap.get("restrictadorgid") != null
        ? requestMap.get("restrictadorgid")
        : "";
    if (restrictadorgid.compareTo("") != 0)
      restrictorg = true;

    String adOrgId = requestMap.get(restrictadorgid) != null ? requestMap.get(restrictadorgid) : "";

    boolean restrictproject = false;
    String restrictprojectid = requestMap.get("restrictprojectid") != null
        ? requestMap.get("restrictprojectid")
        : "";
    if (restrictprojectid.compareTo("") != 0)
      restrictproject = true;

    String cProjectId = requestMap.get(restrictprojectid) != null
        ? requestMap.get(restrictprojectid)
        : "";

    StringBuilder whereClause = new StringBuilder();

    if (restrictorg) {
      String orgList = Utility.getInStrSet(
          OBContext.getOBContext().getOrganizationStructureProvider().getNaturalTree(adOrgId));

      if (!orgList.isEmpty()) {
        whereClause.append("e.organization.id in (" + orgList + ")");
      } else {
        whereClause.append("e.organization.id in ('')");
      }
    }

    if (restrictproject) {
      if (whereClause.length() != 0)
        whereClause.append(" and ");
      whereClause.append("e.sprProject.id = '" + cProjectId + "'");
    }

    return whereClause.toString();
  }
}
