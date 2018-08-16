package pe.com.unifiedgo.accounting.ad_actionbutton;

import java.util.Map;
import java.util.Set;

import org.openbravo.client.application.FilterExpression;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;

public class FINPaymentFCPreliqSelectorFilterExpresion implements FilterExpression {

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

    String finFinancialAccountId = requestMap.get("inpfinFinancialAccountId") != null ? requestMap.get("inpfinFinancialAccountId") : "";
    FIN_FinancialAccount finacc = OBDal.getInstance().get(FIN_FinancialAccount.class, finFinancialAccountId);
    if (finacc == null) {
      return "1=2";
    }

    StringBuilder whereClause = new StringBuilder();
    whereClause.append("e.status = 'PWNC'");

    if (whereClause.length() != 0)
      whereClause.append(" and ");
    whereClause.append("e.scoFixedcashrepStatus = 'SCO_DR'");

    if (whereClause.length() != 0)
      whereClause.append(" and ");
    whereClause.append("e.account.id = '" + finacc.getId() + "'");

    System.out.println("whereClause:" + whereClause.toString());
    return whereClause.toString();

  }
}
