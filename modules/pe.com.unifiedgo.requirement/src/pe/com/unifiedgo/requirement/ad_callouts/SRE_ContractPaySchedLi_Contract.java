package pe.com.unifiedgo.requirement.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;

import pe.com.unifiedgo.requirement.data.SREPurchaseContract;

public class SRE_ContractPaySchedLi_Contract extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */

    final String strPurchaseContractId = info.getStringParameter("inpsrePurchaseContractId",
        IsIDFilter.instance);

    SREPurchaseContract pcontract = OBDal.getInstance().get(SREPurchaseContract.class,
        strPurchaseContractId);
    if (pcontract != null) {
      info.addResult("inpcProjectId", pcontract.getProject().getId());
      info.addResult("inpcBpartnerId", pcontract.getBusinessPartner().getId());
      info.addResult("inpcCurrencyId", pcontract.getCurrency().getId());

    } else {
      info.addResult("inpcProjectId", "");
      info.addResult("inpcBpartnerId", "");
    }
  }

}
