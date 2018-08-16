package pe.com.unifiedgo.accounting;

import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.financialmgmt.accounting.coa.AccountingCombination;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchema;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.gl.GLItemAccounts;

public class GenerateGLActionHandler extends BaseActionHandler {

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String data) {
    try {
      final JSONObject jsonData = new JSONObject(data);
      final JSONArray glitem_id_array = jsonData.getJSONArray("glitem_id_array");
      final String action = jsonData.getString("action");

      final OBCriteria<AcctSchema> r_accSchemas_crit = OBDal.getInstance().createCriteria(AcctSchema.class);
      final List<AcctSchema> r_accSchemas = r_accSchemas_crit.list();
      AccountingCombination[] AccountingCombination_array = new AccountingCombination[r_accSchemas.size()];
      for (int i = 0; i < r_accSchemas.size(); i++) {

        Query q = OBDal.getInstance().getSession().createSQLQuery("SELECT c_validcombination.c_validcombination_id FROM c_acctschema_default INNER JOIN c_validcombination ON c_acctschema_default.v_liability_acct=c_validcombination.c_validcombination_id WHERE c_acctschema_default.c_acctschema_id='" + r_accSchemas.get(i).getId() + "' LIMIT 1");
        String c_validcombination_id = (String) q.uniqueResult();

        AccountingCombination acc = OBDal.getInstance().get(AccountingCombination.class, c_validcombination_id);
        AccountingCombination_array[i] = acc;

      }

      for (int i = 0; i < glitem_id_array.length(); i++) {
        String glitem_id = glitem_id_array.getString(i);

        GLItem glitem = OBDal.getInstance().get(GLItem.class, glitem_id);
        if (glitem != null) {
          for (int j = 0; j < r_accSchemas.size(); j++) {

            Query q = OBDal.getInstance().getSession().createSQLQuery("SELECT c_glitem_acct_id FROM c_glitem_acct WHERE c_glitem_id='" + glitem.getId() + "' AND c_acctschema_id='" + r_accSchemas.get(j).getId() + "' LIMIT 1;");
            String exist = (String) q.uniqueResult();
            if (exist == null) {
              GLItemAccounts glItem_acc = OBProvider.getInstance().get(GLItemAccounts.class);
              glItem_acc.set("gLItem", glitem);
              glItem_acc.set("accountingSchema", r_accSchemas.get(j));
              glItem_acc.set("glitemDebitAcct", AccountingCombination_array[j]);
              glItem_acc.set("glitemCreditAcct", AccountingCombination_array[j]);

              OBDal.getInstance().save(glItem_acc);
            }

          }
        }
      }

      JSONObject result = new JSONObject();
      result.put("result", OBMessageUtils.getI18NMessage("SCO_GenerateGL_Success", null));

      return result;
    } catch (Exception e) {
      throw new OBException(e);
    }
  }
}