package pe.com.unifiedgo.sales.ad_actionButton;

import java.util.Map;

import org.openbravo.client.application.FilterExpression;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;

public class PaymentConditionFormSelectorFilterExpresion implements FilterExpression {

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
    boolean restrictorg = false, restrictpm = false;

    String restrictadorgid = requestMap.get("restrictadorgid") != null ? requestMap
        .get("restrictadorgid") : "";
    if (restrictadorgid.compareTo("") != 0)
      restrictorg = true;
    String adOrgId = requestMap.get(restrictadorgid) != null ? requestMap.get(restrictadorgid) : "";

    String restrictpmid = requestMap.get("restrictpmid") != null ? requestMap.get("restrictpmid")
        : "";
    if (restrictpmid.compareTo("") != 0)
      restrictpm = true;
    String finPaymentMethodId = requestMap.get(restrictpmid) != null ? requestMap.get(restrictpmid)
        : "";

    StringBuilder whereClause = new StringBuilder();

    if (restrictorg) {
      String orgList = Utility.getInStrSet(OBContext.getOBContext()
          .getOrganizationStructureProvider().getNaturalTree(adOrgId));

      if (!orgList.isEmpty()) {
        whereClause.append("e.organization.id in (" + orgList + ")");
      } else {
        whereClause.append("e.organization.id in ('')");
      }

    }

    if (restrictpm) {
      FIN_PaymentMethod pm = OBDal.getInstance().get(FIN_PaymentMethod.class, finPaymentMethodId);

      if (whereClause.length() != 0)
        whereClause.append(" and ");

      if ("SCOBILLOFEXCHANGE".equals(pm.getScoSpecialmethod())) {
        whereClause.append("e.scoIsboeterm=true ");

      } else if ("SCOCHECK".equals(pm.getScoSpecialmethod())) {
        whereClause.append("e.scrIscheckterm=true ");

      } else if ("SCOCREDIT".equals(pm.getScoSpecialmethod())) {
        whereClause.append("e.scrIscreditterm=true ");

      } else if ("SCODEPOSIT".equals(pm.getScoSpecialmethod())) {
        whereClause.append("(e.scrIscreditterm=true ");
        whereClause.append("or e.scoSpecialpayterm='SCOINMEDIATETERM') ");

      } else if ("SCOCASH".equals(pm.getScoSpecialmethod())) {
        whereClause.append("e.scoSpecialpayterm='SCOINMEDIATETERM' ");

      } else { // todos los demas
        whereClause.append("((e.scrIscreditterm=true ");
        whereClause.append("or e.scrIscheckterm=true) ");
        whereClause.append("or (e.scoIsboeterm=false ");
        whereClause.append("and e.scrIscheckterm=false ");
        whereClause.append("and e.scrIscreditterm=false)) ");
      }
    }

    return whereClause.toString();

  }
}
