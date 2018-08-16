package pe.com.unifiedgo.accounting.ad_actionbutton;

import java.util.Map;
import java.util.Set;

import org.openbravo.client.application.FilterExpression;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.invoice.Invoice;

public class LoanCGLJournalPrepaymentFormSelectorFilterExpresion implements FilterExpression {

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
    String cInvoiceId = requestMap.get("inpKey") != null ? requestMap.get("inpKey") : "";

    if (cInvoiceId.isEmpty()) {
      System.out.println("BAIL1");
      return "1=2";
    }

    Invoice inv = OBDal.getInstance().get(Invoice.class, cInvoiceId);

    if (inv == null) {
      System.out.println("BAIL2");
      return "1=2";
    }
    StringBuilder whereClause = new StringBuilder();
    whereClause
        .append("e.salesTransaction=false and e.documentStatus='CO' and e.businessPartner.id = '"
            + inv.getBusinessPartner().getId() + "' and e.currency.id = '"
            + inv.getCurrency().getId() + "'");
    System.out.println("whereclause:" + whereClause.toString());

    return whereClause.toString();

  }
}
