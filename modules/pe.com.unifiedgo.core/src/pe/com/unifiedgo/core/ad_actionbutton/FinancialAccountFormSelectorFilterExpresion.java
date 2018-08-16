package pe.com.unifiedgo.core.ad_actionbutton;

import java.util.Map;

import org.openbravo.client.application.FilterExpression;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;

public class FinancialAccountFormSelectorFilterExpresion implements FilterExpression {

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
    String restrictpaymethodId = requestMap.get("restrictpaymethodId") != null
        ? requestMap.get("restrictpaymethodId")
        : "";
    String finPaymentmethodId = requestMap.get(restrictpaymethodId) != null
        ? requestMap.get(restrictpaymethodId)
        : "";

    // poutallow, pinallow, all
    String pmdefaulttype = requestMap.get("pmdefaulttype") != null ? requestMap.get("pmdefaulttype")
        : "";

    String excludePermissions = requestMap.get("excludepermissions") != null
        ? requestMap.get("excludepermissions")
        : "N";
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

    if (pmdefaulttype.compareTo("poutallow") == 0) {
      if (whereClause.length() != 0)
        whereClause.append(" and ");
      whereClause.append(
          "exists(select 1 from FinancialMgmtFinAccPaymentMethod fpaym where fpaym.account.id = e.id and fpaym.payoutAllow = true");
      if (finPaymentmethodId.compareTo("") != 0) {
        whereClause.append(" and fpaym.paymentMethod.id = '" + finPaymentmethodId + "'");
      } else {
        whereClause.append(" and fpaym.paymentMethod.id = ''");
      }
      whereClause.append(")");
    } else if (pmdefaulttype.compareTo("pinallow") == 0) {
      if (whereClause.length() != 0)
        whereClause.append(" and ");
      whereClause.append(
          "exists(select 1 from FinancialMgmtFinAccPaymentMethod fpaym where fpaym.account.id = e.id and fpaym.payinAllow = true");
      if (finPaymentmethodId.compareTo("") != 0) {
        whereClause.append(" and fpaym.paymentMethod.id = '" + finPaymentmethodId + "'");
      } else {
        whereClause.append(" and fpaym.paymentMethod.id = ''");
      }
      whereClause.append(")");
    }

    if (excludePermissions.equals("N")) {
      if (whereClause.length() != 0)
        whereClause.append(" and ");
      whereClause.append(
          "((select count(UPF.id) from SCO_User_Perm_Finaccount UPF where UPF.userContact.id = '"
              + adUserId
              + "')=0 or e.id IN (select UPF.financialAccount.id from SCO_User_Perm_Finaccount UPF where UPF.userContact.id = '"
              + adUserId + "' AND UPF.active = true))");
    }
    return whereClause.toString();

  }
}
