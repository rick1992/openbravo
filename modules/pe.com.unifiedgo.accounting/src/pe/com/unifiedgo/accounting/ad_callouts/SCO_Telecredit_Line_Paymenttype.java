package pe.com.unifiedgo.accounting.ad_callouts;

import java.math.BigDecimal;

import javax.servlet.ServletException;

import org.openbravo.erpCommon.ad_callouts.SimpleCallout;

public class SCO_Telecredit_Line_Paymenttype extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */

    String strPaymenttype = info.getStringParameter("inppaymenttype", null);
    if (strPaymenttype == null)
      strPaymenttype = "";

    info.addResult("inpamount", new BigDecimal(0));
    info.addResult("inpcBpartnerId", "");
    info.addResult("inpcGlitemId", "");

    info.addResult("inpdoctyperefId", "");
    info.addResult("inpinvoicerefId", "");
    info.addResult("inpcBpBankaccountId", "");

  }

}
