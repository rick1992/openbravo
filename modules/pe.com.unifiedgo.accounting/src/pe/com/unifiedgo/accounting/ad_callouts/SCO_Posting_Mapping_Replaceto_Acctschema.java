package pe.com.unifiedgo.accounting.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.financialmgmt.accounting.coa.AccountingCombination;

public class SCO_Posting_Mapping_Replaceto_Acctschema extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */

    final String strReplaceto_acctschema_id = info.getStringParameter("inpreplacetoAcctschemaId",
        IsIDFilter.instance);
    final String strReplacefrom_acct_id = info.getStringParameter("inpreplacefromAcctId",
        IsIDFilter.instance);

    AccountingCombination replacefrom_acct = OBDal.getInstance().get(AccountingCombination.class,
        strReplacefrom_acct_id);

    if (replacefrom_acct == null) {
      info.addResult("inpreplacefromAcctId", "");
    } else {
      if (!replacefrom_acct.getAccountingSchema().getId().equals(strReplaceto_acctschema_id)) {
        info.addResult("inpreplacefromAcctId", "");
      }
    }

  }

}
