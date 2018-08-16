package pe.com.unifiedgo.accounting.process;

import java.util.Map;

import org.openbravo.client.application.FilterExpression;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.financialmgmt.gl.GLJournal;

public class GenPayRollEntriesFileSelectorFilterExpresion implements FilterExpression {

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

    String glJournalId = requestMap.get("inpKey") != null ? requestMap.get("inpKey") : "";

    GLJournal journal = OBDal.getInstance().get(GLJournal.class, glJournalId);
    if (journal == null) {
      System.out.println("enter journal null" + glJournalId);
      return "1=2";
    }
    StringBuilder whereClause = new StringBuilder();

    String orgList = Utility.getInStrSet(OBContext.getOBContext().getOrganizationStructureProvider()
        .getNaturalTree(journal.getOrganization().getId()));

    if (!orgList.isEmpty()) {
      whereClause.append("e.organization.id in (" + orgList + ")");
    } else {
      whereClause.append("e.organization.id in ('')");
    }

    if (whereClause.length() != 0)
      whereClause.append(" and ");
    whereClause.append("e.table.id = '224' and e.record = '" + journal.getId() + "'");

    // System.out.println("whereClause:" + whereClause.toString());
    return whereClause.toString();

  }
}
