package pe.com.unifiedgo.accounting.ad_callouts;

import java.math.BigDecimal;

import javax.servlet.ServletException;

import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;

public class SCO_Fixedcash_Rep_PreliqPayment extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */

    String PreliqPayment_ID = info.vars.getStringParameter("inppreliqpaymentId");
    if (PreliqPayment_ID == null) {
      info.addResult("inpamount", new BigDecimal(0));
      return;
    }

    FIN_Payment payment = OBDal.getInstance().get(FIN_Payment.class, PreliqPayment_ID);

    if (payment == null) {
      info.addResult("inpamount", new BigDecimal(0));
      return;
    }

    info.addResult("inpamount", payment.getAmount());

  }

}
