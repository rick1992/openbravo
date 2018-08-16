package pe.com.unifiedgo.accounting.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.invoice.InvoiceTax;

public class SCO_Sperception_Rec_Detail_InvoiceTaxRef extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */

    String C_InvoiceTax_ID = info.vars.getStringParameter("inpinvoicetaxrefId");
    if (C_InvoiceTax_ID == null)
      return;

    InvoiceTax invoicetax = OBDal.getInstance().get(InvoiceTax.class, C_InvoiceTax_ID);
    if (invoicetax == null)
      return;

    info.addResult("inpamount", invoicetax.getTaxAmount());

  }

}
