package pe.com.unifiedgo.accounting.ad_actionbutton;

import java.util.Map;

import org.openbravo.client.application.FilterExpression;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;

public class UidentFinancialAccountFormSelectorFilterExpresion implements FilterExpression {

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

    String finPaymentId = requestMap.get("inpKey") != null ? requestMap.get("inpKey") : "";

    String excludePermissions = requestMap.get("excludepermissions") != null
        ? requestMap.get("excludepermissions")
        : "N";

    FIN_Payment payment = OBDal.getInstance().get(FIN_Payment.class, finPaymentId);
    if (payment == null) {
      return "1=2";
    }
    StringBuilder whereClause = new StringBuilder();

    String orgList = Utility.getInStrSet(OBContext.getOBContext().getOrganizationStructureProvider()
        .getNaturalTree(payment.getOrganization().getId()));
    FIN_FinancialAccount finacc = payment.getAccount();

    if (!orgList.isEmpty()) {
      whereClause.append("e.organization.id in (" + orgList + ")");
    } else {
      whereClause.append("e.organization.id in ('')");
    }

    if (whereClause.length() != 0)
      whereClause.append(" and ");
    whereClause.append("e.scoForapppayment <> true");

    if (whereClause.length() != 0)
      whereClause.append(" and ");
    whereClause.append("c.id = '" + finacc.getCurrency().getId() + "'");

    if (whereClause.length() != 0)
      whereClause.append(" and ");
    whereClause.append("e.type = 'B'");

    if (excludePermissions.equals("N")) {
      if (whereClause.length() != 0)
        whereClause.append(" and ");
      whereClause.append(
          "((select count(UPF.id) from SCO_User_Perm_Finaccount UPF where UPF.userContact.id = '"
              + adUserId
              + "')=0 or e.id IN (select UPF.financialAccount.id from SCO_User_Perm_Finaccount UPF where UPF.userContact.id = '"
              + adUserId + "' AND UPF.active = true))");
    }
    // System.out.println("whereClause:" + whereClause.toString());
    return whereClause.toString();

  }
}
