package pe.com.unifiedgo.accounting.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;

public class SCO_FactoringInvoice_FinancialAccount extends SimpleCallout {
  private static final long serialVersionUID = 1L;
  private static final RequestFilter filterYesNo = new ValueListFilter("Y", "N");

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    final String strinpissotrx = info.getStringParameter("inpissotrx", filterYesNo);
    final String strOrgId = info.getStringParameter("inpadOrgId", IsIDFilter.instance);
    final String strFinancialAccountId = info.getStringParameter("inpfinFinancialAccountId", IsIDFilter.instance);

    FIN_FinancialAccount finacc = OBDal.getInstance().get(FIN_FinancialAccount.class, strFinancialAccountId);
    if (finacc == null) {
      info.addResult("inpcCurrencyId", "");
      return;
    }

    info.addResult("inpcCurrencyId", finacc.getCurrency().getId());

  }

}
