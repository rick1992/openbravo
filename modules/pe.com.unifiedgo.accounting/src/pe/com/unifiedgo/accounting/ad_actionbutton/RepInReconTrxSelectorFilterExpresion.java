package pe.com.unifiedgo.accounting.ad_actionbutton;

import java.util.Map;

import org.openbravo.client.application.FilterExpression;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;

public class RepInReconTrxSelectorFilterExpresion implements FilterExpression {

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

    String strFinFinaccTransactionId = requestMap.get("inpKey") != null ? requestMap.get("inpKey") : "";
    FIN_FinaccTransaction finacctrx = OBDal.getInstance().get(FIN_FinaccTransaction.class, strFinFinaccTransactionId);
    if (finacctrx == null) {
      return "1=2";
    }

    StringBuilder whereClause = new StringBuilder();
    whereClause.append("e.account.id = '" + finacctrx.getAccount().getId() + "' and e.id != '" + finacctrx.getId() + "' and e.reconciliation is null and e.paymentAmount = " + finacctrx.getPaymentAmount().toPlainString() + " and e.depositAmount = " + finacctrx.getDepositAmount().toPlainString());

    return whereClause.toString();

  }
}
