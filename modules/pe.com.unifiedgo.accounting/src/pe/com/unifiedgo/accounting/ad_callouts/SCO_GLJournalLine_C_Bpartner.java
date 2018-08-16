package pe.com.unifiedgo.accounting.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;

public class SCO_GLJournalLine_C_Bpartner extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */

    final String strOrgId = info.getStringParameter("inpadOrgId", IsIDFilter.instance);
    final String strClientId = info.getStringParameter("inpadClientId", IsIDFilter.instance);
    final String strInvoiceRefId = info.getStringParameter("inpemScoCInvoiceId", null);
    final String strCBPartnerId = info.getStringParameter("inpcBpartnerId", null);

    info.addResult("inpemScoCInvoiceId", "");
    info.addResult("inpemScoRendcuentasId", "");
    info.addResult("inpemScoPrepaymentId", "");

  }

}
