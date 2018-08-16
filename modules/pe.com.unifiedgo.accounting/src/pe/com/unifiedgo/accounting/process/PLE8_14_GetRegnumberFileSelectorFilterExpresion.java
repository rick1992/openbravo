package pe.com.unifiedgo.accounting.process;

import java.util.Map;

import org.openbravo.client.application.FilterExpression;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;

import pe.com.unifiedgo.accounting.data.SCOPle8_14_Reg;

public class PLE8_14_GetRegnumberFileSelectorFilterExpresion implements FilterExpression {

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

    String scoPle8_14_Reg_Id = requestMap.get("inpKey") != null ? requestMap.get("inpKey") : "";

    SCOPle8_14_Reg ple8_14_reg = OBDal.getInstance().get(SCOPle8_14_Reg.class, scoPle8_14_Reg_Id);
    if (ple8_14_reg == null) {
      return "1=2";
    }
    StringBuilder whereClause = new StringBuilder();

    String orgList = Utility.getInStrSet(OBContext.getOBContext().getOrganizationStructureProvider()
        .getNaturalTree(ple8_14_reg.getOrganization().getId()));

    if (!orgList.isEmpty()) {
      whereClause.append("e.organization.id in (" + orgList + ")");
    } else {
      whereClause.append("e.organization.id in ('')");
    }

    if (whereClause.length() != 0)
      whereClause.append(" and ");
    whereClause.append("e.table.id = '89B85662FE7E44A28E18BD87D50B4E09' and e.record = '"
        + ple8_14_reg.getId() + "'");

    System.out.println("whereClause:" + whereClause.toString());
    return whereClause.toString();

  }
}
