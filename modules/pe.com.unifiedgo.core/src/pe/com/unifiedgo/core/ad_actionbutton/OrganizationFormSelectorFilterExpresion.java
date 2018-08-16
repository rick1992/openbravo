package pe.com.unifiedgo.core.ad_actionbutton;

import java.util.Map;

import org.openbravo.client.application.FilterExpression;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;

public class OrganizationFormSelectorFilterExpresion implements FilterExpression {

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
    String restrictadorgid = requestMap.get("restrictadorgid") != null ? requestMap.get("restrictadorgid") : "";
    if (restrictadorgid.compareTo("") != 0)
      restrictorg = true;
    String adOrgId = requestMap.get(restrictadorgid) != null ? requestMap.get(restrictadorgid) : "";

    boolean parentorg = false;
    String parentadorgid = requestMap.get("parentadorgid") != null ? requestMap.get("parentadorgid") : "";
    if (parentadorgid.compareTo("") != 0)
      parentorg = true;
    String padOrgId = requestMap.get(parentadorgid) != null ? requestMap.get(parentadorgid) : "";

    // all, parentonly childonly
    String orgtype = requestMap.get("orgtype") != null ? requestMap.get("orgtype") : "";
    String notitself = requestMap.get("notitself") != null ? requestMap.get("notitself") : "";
    String nozeroorg = requestMap.get("nozeroorg") != null ? requestMap.get("nozeroorg") : "";

    StringBuilder whereClause = new StringBuilder();

    if (restrictorg) {
      String orgList = Utility.getInStrSet(OBContext.getOBContext().getOrganizationStructureProvider().getNaturalTree(adOrgId));

      if (!orgList.isEmpty()) {
        whereClause.append("e.id in (" + orgList + ")");
      } else {
        whereClause.append("e.id in ('')");
      }

      if (notitself.compareTo("yes") == 0) {
        if (whereClause.length() != 0)
          whereClause.append(" and ");
        whereClause.append("e.id <> '" + adOrgId + "'");
      }
    } else if (parentorg) {
      String orgList = Utility.getInStrSet(OBContext.getOBContext().getOrganizationStructureProvider().getChildTree(padOrgId, true));

      if (!orgList.isEmpty()) {
        whereClause.append("e.id in (" + orgList + ")");
      } else {
        whereClause.append("e.id in ('')");
      }

      if (notitself.compareTo("yes") == 0) {
        if (whereClause.length() != 0)
          whereClause.append(" and ");
        whereClause.append("e.id <> '" + padOrgId + "'");
      }
    }

    if (orgtype.compareTo("parentonly") == 0) {
      if (whereClause.length() != 0)
        whereClause.append(" and ");
      whereClause.append("exists(from OrganizationType ot where ot.id = e.organizationType.id and ot.legalEntity = true) and e.ready = true");
    } else if (orgtype.compareTo("childonly") == 0) {
      if (whereClause.length() != 0)
        whereClause.append(" and ");
      whereClause.append("exists(from OrganizationType ot where ot.id = e.organizationType.id and ot.legalEntity = false) and e.ready = true");
    }

    if (nozeroorg.compareTo("yes") == 0) {
      if (whereClause.length() != 0)
        whereClause.append(" and ");
      whereClause.append("e.id <> '0'");
    }

    System.out.println(whereClause.toString());
    return whereClause.toString();

  }
}
