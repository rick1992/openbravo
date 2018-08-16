package pe.com.unifiedgo.accounting.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.financialmgmt.payment.PaymentTerm;

public class SE_Invoice_Paymentterm extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */

    final String strOrgId = info.getStringParameter("inpadOrgId", IsIDFilter.instance);
    final String strCPaymenttermId = info.getStringParameter("inpcPaymenttermId",
        IsIDFilter.instance);
    PaymentTerm paymentTerm = OBDal.getInstance().get(PaymentTerm.class, strCPaymenttermId);
    if (paymentTerm != null) {
      info.addResult("inpemScoSpecialpayterm", paymentTerm.getScoSpecialpayterm());
    } else {
      info.addResult("inpemScoSpecialpayterm", "");

    }

  }

}
