package pe.com.unifiedgo.accounting.ad_actionbutton;

import java.util.Map;
import java.util.Set;

import org.openbravo.client.application.FilterExpression;
import org.openbravo.dal.core.OBContext;
import org.openbravo.model.ad.access.User;

public class ReconciliationFormATSelectorFilterExpresion implements FilterExpression {

  @Override
  public String getExpression(Map<String, String> requestMap) {

    System.out.println("ADIsOrgIncluded filter expression:");
    Set<String> keyset = requestMap.keySet();
    String[] keys = new String[keyset.size()];
    keyset.toArray(keys);
    for (int i = 0; i < keys.length; i++) {
      System.out.println(keys[i] + ":" + requestMap.get(keys[i]));
    }

    User user = OBContext.getOBContext().getUser();
    String adUserId = user.getId();

    String finFinancialAccountId = requestMap.get("inpFinFinancialAccountId") != null ? requestMap.get("inpFinFinancialAccountId") : "";

    StringBuilder whereClause = new StringBuilder();
    whereClause.append("e.documentStatus = 'CO'");

    if (whereClause.length() != 0)
      whereClause.append(" and ");
    whereClause.append("e.scoCashtransferstatus = 'SCO_CASH'");

    if (whereClause.length() != 0)
      whereClause.append(" and ");
    whereClause.append("(e.endingBalance-e.startingbalance) > 0");

    if (whereClause.length() != 0)
      whereClause.append(" and ");
    whereClause.append("e.account.id = '" + finFinancialAccountId + "'");

    return whereClause.toString();

  }
}
