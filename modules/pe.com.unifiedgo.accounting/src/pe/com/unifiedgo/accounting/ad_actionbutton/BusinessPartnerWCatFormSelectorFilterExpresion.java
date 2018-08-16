package pe.com.unifiedgo.accounting.ad_actionbutton;

import java.util.Map;

import org.openbravo.client.application.FilterExpression;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;

public class BusinessPartnerWCatFormSelectorFilterExpresion implements FilterExpression {

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
    String bpdefaulttype = requestMap.get("bpdefaulttype") != null ? requestMap.get("bpdefaulttype") : "";

    StringBuilder whereClause = new StringBuilder();

    if (restrictorg) {
      String orgList = Utility.getInStrSet(OBContext.getOBContext().getOrganizationStructureProvider().getNaturalTree(adOrgId));

      if (!orgList.isEmpty()) {
        whereClause.append("e.organization.id in (" + orgList + ")");
      } else {
        whereClause.append("e.organization.id in ('')");
      }

    }

    if (bpdefaulttype.compareTo("customer") == 0) {
      if (whereClause.length() != 0)
        whereClause.append(" and ");
      whereClause.append("e.customer = true");
    } else if (bpdefaulttype.compareTo("vendor") == 0) {
      if (whereClause.length() != 0)
        whereClause.append(" and ");
      whereClause.append("e.vendor = true");
    } else if (bpdefaulttype.compareTo("employee") == 0) {
      if (whereClause.length() != 0)
        whereClause.append(" and ");
      whereClause.append("e.employee=true");
    } else if (bpdefaulttype.compareTo("salesrep") == 0) {
      if (whereClause.length() != 0)
        whereClause.append(" and ");
      whereClause.append("e.vendor=true and e.employee=true and e.isSalesRepresentative=true and exists(from ADUser uu WHERE uu.businessPartner.id = e.id)");
    }

    return whereClause.toString();

  }
}
