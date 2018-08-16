package pe.com.unifiedgo.core.ad_actionbutton;

import java.util.Map;
import java.util.Set;

import org.openbravo.client.application.FilterExpression;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;

public class AdUserFormSelectorFilterExpresion implements FilterExpression {

  @Override
  public String getExpression(Map<String, String> requestMap) {

    
      /*System.out.println("ADIsOrgIncluded filter expression:"); Set<String> keyset =
      requestMap.keySet(); String[] keys = new String[keyset.size()]; keyset.toArray(keys); for
      (int i = 0; i < keys.length; i++) { System.out.println(keys[i] + ":" +
      requestMap.get(keys[i])); }
     */
     
	  
    User user = OBContext.getOBContext().getUser();
    String adUserId = user.getId();
    boolean restrictorg = false;
    String restrictadorgid = requestMap.get("restrictadorgid") != null ? requestMap.get("restrictadorgid") : "";
    
    
    if (restrictadorgid.compareTo("") != 0)
      restrictorg = true;

    String adOrgId = requestMap.get(restrictadorgid) != null ? requestMap.get(restrictadorgid) : "";
    StringBuilder whereClause = new StringBuilder();
    String orgList = Utility.getInStrSet(OBContext.getOBContext().getOrganizationStructureProvider().getNaturalTree(adOrgId));
     
      //Solo para la Referencia que incluye pickeros
       whereClause.append(" e.organization.id = '0'");
       whereClause.append(" and ");
       whereClause.append(" e.swaIsForpicking = 'Y'");

    return whereClause.toString();

  }
}
