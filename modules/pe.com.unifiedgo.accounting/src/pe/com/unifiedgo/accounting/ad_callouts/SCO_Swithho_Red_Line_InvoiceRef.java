package pe.com.unifiedgo.accounting.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.invoice.Invoice;

public class SCO_Swithho_Red_Line_InvoiceRef extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */

    String C_InvoiceRef_ID = info.vars.getStringParameter("inpinvoicerefId");
    if (C_InvoiceRef_ID == null)
      return;

    Invoice inv = OBDal.getInstance().get(Invoice.class, C_InvoiceRef_ID);
    if (inv == null)
      return;

    info.addResult("inpamount", inv.getOutstandingAmount());

  }

}
