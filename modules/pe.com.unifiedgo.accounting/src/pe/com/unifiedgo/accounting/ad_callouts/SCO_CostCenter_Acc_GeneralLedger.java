package pe.com.unifiedgo.accounting.ad_callouts;

import java.util.Enumeration;

import javax.servlet.ServletException;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.financialmgmt.accounting.coa.AccountingCombination;

public class SCO_CostCenter_Acc_GeneralLedger extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    
      /*Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
      { System.out.println(params.nextElement()); }*/
     

    final String strC_acctschema_id = info.getStringParameter("inpcAcctschemaId", IsIDFilter.instance);
    final String strExpensesAcc_id = info.getStringParameter("inpexpensesAcct", IsIDFilter.instance);

    AccountingCombination expensesAcct = OBDal.getInstance().get(AccountingCombination.class, strExpensesAcc_id);

    if (expensesAcct == null) {
      info.addResult("inpexpensesAcct", "");
    } else {
      if (!expensesAcct.getAccountingSchema().getId().equals(strC_acctschema_id)) {
        info.addResult("inpexpensesAcct", "");
      }
    }

  }

}
