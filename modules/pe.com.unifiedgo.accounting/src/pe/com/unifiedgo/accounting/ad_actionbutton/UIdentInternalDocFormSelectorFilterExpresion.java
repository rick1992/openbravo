package pe.com.unifiedgo.accounting.ad_actionbutton;

import java.util.Map;

import org.openbravo.client.application.FilterExpression;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;

public class UIdentInternalDocFormSelectorFilterExpresion implements FilterExpression {

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

    FIN_Payment payment = OBDal.getInstance().get(FIN_Payment.class, finPaymentId);
    if (payment == null) {
      return "1=2";
    }

    String adOrgId = payment.getOrganization().getId();
    StringBuilder whereClause = new StringBuilder();


      String orgList = Utility.getInStrSet(OBContext.getOBContext().getOrganizationStructureProvider().getNaturalTree(adOrgId));

    if (!orgList.isEmpty()) {
      whereClause.append("e.organization.id in (" + orgList + ")");
    } else {
      whereClause.append("e.organization.id in ('')");
    }

    if (whereClause.length() != 0)
        whereClause.append(" and ");
      whereClause.append("e.documentStatus = 'CO'");

    return whereClause.toString();

  }
}
