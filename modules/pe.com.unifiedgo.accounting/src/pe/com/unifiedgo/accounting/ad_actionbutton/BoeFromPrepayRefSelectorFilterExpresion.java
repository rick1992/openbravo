package pe.com.unifiedgo.accounting.ad_actionbutton;

import java.util.Map;

import org.openbravo.client.application.FilterExpression;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.User;

import pe.com.unifiedgo.accounting.data.SCOBillofexchange;

public class BoeFromPrepayRefSelectorFilterExpresion implements FilterExpression {

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

    String scoBillofexchangeId = requestMap.get("inpscoBillofexchangeId") != null
        ? requestMap.get("inpscoBillofexchangeId")
        : "";

    if (scoBillofexchangeId.isEmpty()) {
      return "1=2";
    }

    SCOBillofexchange boe = OBDal.getInstance().get(SCOBillofexchange.class, scoBillofexchangeId);
    if (boe == null) {
      return "1=2";
    }
    StringBuilder whereClause = new StringBuilder();

    String specialdoctype = boe.getSpecialdoctype() != null ? boe.getSpecialdoctype() : "";
    if (specialdoctype.equals("SCOINVOICEXBOE")) {
      if (whereClause.length() > 0) {
        whereClause.append("and ");
      }
      whereClause.append(
          "e.documentStatus=\'CO\'  and e.paymentComplete=false and e.businessPartner.id = \'"
              + boe.getBusinessPartner().getId() + "\' and e.currency.id IN (\'"
              + boe.getCurrency().getId() + "\',\'" + boe.getOtherCurrency().getId()
              + "\') and e.paymentMethod.scoSpecialmethod='SCOBILLOFEXCHANGE' and e.salesTransaction = "
              + (boe.isSalesTransaction() ? "true" : "false"));
    } else {
      if (whereClause.length() > 0) {
        whereClause.append("and ");
      }
      whereClause.append(
          "e.documentStatus=\'CO\'  and e.paymentComplete=false and e.businessPartner.id = \'"
              + boe.getBusinessPartner().getId() + "\' and e.currency.id IN (\'"
              + boe.getCurrency().getId() + "\',\'" + boe.getOtherCurrency().getId()
              + "\') and e.salesTransaction = " + (boe.isSalesTransaction() ? "true" : "false"));
    }

    // System.out.println("whereClause:" + whereClause.toString());
    return whereClause.toString();

  }
}
