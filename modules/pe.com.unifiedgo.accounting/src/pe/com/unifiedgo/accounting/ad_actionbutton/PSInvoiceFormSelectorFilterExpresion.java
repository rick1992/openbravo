package pe.com.unifiedgo.accounting.ad_actionbutton;

import java.util.Map;

import org.openbravo.client.application.FilterExpression;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;

public class PSInvoiceFormSelectorFilterExpresion implements FilterExpression {

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

    boolean restrictissotrx = false;
    String strrestrictissotrx = requestMap.get("restrictissotrx") != null ? requestMap.get("restrictissotrx") : "";
    if (strrestrictissotrx.compareTo("") != 0)
      restrictissotrx = true;

    boolean restrictcurrency = false;
    String restrictcurrencyid = requestMap.get("restrictcurrencyid") != null ? requestMap.get("restrictcurrencyid") : "";
    if (restrictcurrencyid.compareTo("") != 0)
      restrictcurrency = true;

    String adOrgId = requestMap.get(restrictadorgid) != null ? requestMap.get(restrictadorgid) : "";

    StringBuilder whereClause = new StringBuilder();

    if (restrictorg) {
      String orgList = Utility.getInStrSet(OBContext.getOBContext().getOrganizationStructureProvider().getNaturalTree(adOrgId));

      if (!orgList.isEmpty()) {
        whereClause.append("e.organization.id in (" + orgList + ")");
      } else {
        whereClause.append("e.organization.id in ('')");
      }

    }

    if (restrictissotrx) {
      boolean issotrx = false;
      String strissotrx = requestMap.get(strrestrictissotrx) != null ? requestMap.get(strrestrictissotrx) : "";

      if (strissotrx.compareTo("Y") == 0) {
        issotrx = true;
      }

      if (issotrx) {
        if (whereClause.length() != 0)
          whereClause.append(" and ");
        whereClause.append("e.salesTransaction = true");
      } else {
        if (whereClause.length() != 0)
          whereClause.append(" and ");
        whereClause.append("e.salesTransaction = false");
      }
    }

    if (restrictcurrency) {
      String cCurrencyId = requestMap.get(restrictcurrencyid) != null ? requestMap.get(restrictcurrencyid) : "";
      if (whereClause.length() != 0)
        whereClause.append(" and ");
      whereClause.append("e.currency.id = '" + cCurrencyId + "'");
    }

    if (whereClause.length() != 0)
      whereClause.append(" and ");
    whereClause.append("e.documentStatus = 'CO'");

    return whereClause.toString();

  }
}
