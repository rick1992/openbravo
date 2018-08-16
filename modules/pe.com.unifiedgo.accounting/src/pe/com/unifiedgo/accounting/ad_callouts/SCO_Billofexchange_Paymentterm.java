package pe.com.unifiedgo.accounting.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;

public class SCO_Billofexchange_Paymentterm extends SimpleCallout {
  private static final long serialVersionUID = 1L;
  private static final RequestFilter filterYesNo = new ValueListFilter("Y", "N");

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // Enumeration<String> params = info.vars.getParameterNames();
    // while (params.hasMoreElements()) {
    // System.out.println(params.nextElement());
    // }

    final String strinpissotrx = info.getStringParameter("inpissotrx", filterYesNo);
    final String strOrgId = info.getStringParameter("inpadOrgId", IsIDFilter.instance);
    final String strPaymenttermId = info.getStringParameter("inpcPaymenttermId",
        IsIDFilter.instance);

    /*
     * PaymentTerm paymentTerm = OBDal.getInstance().get(PaymentTerm.class, strPaymenttermId); int
     * numCondicionTerms = 0; if (paymentTerm != null && paymentTerm.isScoIsboeterm()) {
     * numCondicionTerms = paymentTerm.getSCOBoetermlineList().size(); if (numCondicionTerms == 0) {
     * numCondicionTerms = 1; } } if (numCondicionTerms == 0) { info.addResult("inpcPaymenttermId",
     * ""); } info.addResult("inpnumofboes", numCondicionTerms);
     */

  }

}
