package pe.com.unifiedgo.accounting.ad_callouts;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import javax.servlet.ServletException;

import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;

import pe.com.unifiedgo.accounting.data.SCOPrepayment;

public class SCO_Boe_From_Prepayment extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */
    DecimalFormat df = new DecimalFormat("#0.00");
    df.setRoundingMode(RoundingMode.HALF_UP);

    String stradOrgId = info.vars.getStringParameter("inpadOrgId");
    String stradClientId = info.vars.getStringParameter("inpadClientId");
    String SCO_Prepayment_ID = info.vars.getStringParameter("inpscoPrepaymentId");
    String strScoBillofexchangeId = info.vars.getStringParameter("inpscoBillofexchangeId");
    String strSpecialDocType = info.vars.getStringParameter("inpspecialdoctype");
    String strPercentRenewalType = info.vars.getStringParameter("inppercentrenewalType");
    if (SCO_Prepayment_ID == null)
      return;

    SCOPrepayment prep = OBDal.getInstance().get(SCOPrepayment.class, SCO_Prepayment_ID);
    if (prep == null)
      return;

    info.addResult("inpamount", prep.getAmount().subtract(prep.getTotalPaid()));

  }
}
