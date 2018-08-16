package pe.com.unifiedgo.core.ad_actionbutton;

import java.util.Map;

import org.openbravo.client.application.FilterExpression;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;

public class PaymentMethodFormSelectorFilterExpresion implements FilterExpression {

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

    // poutallow, pinallow, all
    String pmdefaulttype = requestMap.get("pmdefaulttype") != null ? requestMap.get("pmdefaulttype") : "";

    StringBuilder whereClause = new StringBuilder();

    if (restrictorg) {
      String orgList = Utility.getInStrSet(OBContext.getOBContext().getOrganizationStructureProvider().getNaturalTree(adOrgId));

      if (!orgList.isEmpty()) {
        whereClause.append("e.organization.id in (" + orgList + ")");
      } else {
        whereClause.append("e.organization.id in ('')");
      }

    }

    if (pmdefaulttype.compareTo("poutallow") == 0) {
      if (whereClause.length() != 0)
        whereClause.append(" and ");
      whereClause.append("exists(select 1 from FinancialMgmtFinAccPaymentMethod fpaym where fpaym.paymentMethod.id = e.id and fpaym.payoutAllow = true)");
    } else if (pmdefaulttype.compareTo("pinallow") == 0) {
      if (whereClause.length() != 0)
        whereClause.append(" and ");
      whereClause.append("exists(select 1 from FinancialMgmtFinAccPaymentMethod fpaym where fpaym.paymentMethod.id = e.id and fpaym.payinAllow = true)");
    }

    return whereClause.toString();

  }
}
