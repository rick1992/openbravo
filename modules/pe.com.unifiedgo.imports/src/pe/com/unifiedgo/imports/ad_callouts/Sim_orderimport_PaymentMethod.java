package pe.com.unifiedgo.imports.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;

public class Sim_orderimport_PaymentMethod extends SimpleCallout {
  private static final long serialVersionUID = 1L;
  private static final RequestFilter filterYesNo = new ValueListFilter("Y", "N");

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */

    final String strOrgId = info.getStringParameter("inpadOrgId", IsIDFilter.instance);
    final String strFinPaymentMethodId = info.getStringParameter("inpfinPaymentmethodId", IsIDFilter.instance);

    String specialmethod = null;
    FIN_PaymentMethod paymentmethod = OBDal.getInstance().get(FIN_PaymentMethod.class, strFinPaymentMethodId);
    if (strFinPaymentMethodId != null) {
      specialmethod = paymentmethod.getScoSpecialmethod();
    }

    if (specialmethod != null) {
      info.addResult("inpspecialmethod", specialmethod);
    } else {
      info.addResult("inpspecialmethod", "");
    }
  }

}
