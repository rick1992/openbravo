package pe.com.unifiedgo.accounting.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;

public class SCO_Pwithho_Red_Line_PaySchedRef extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */

    String FIN_PaymentSchedRef_ID = info.vars.getStringParameter("inpfinPaymentSchedulerefId");
    if (FIN_PaymentSchedRef_ID == null)
      return;

    FIN_PaymentSchedule paysched = OBDal.getInstance().get(FIN_PaymentSchedule.class, FIN_PaymentSchedRef_ID);
    if (paysched == null)
      return;

    info.addResult("inpamount", paysched.getOutstandingAmount());

  }

}
