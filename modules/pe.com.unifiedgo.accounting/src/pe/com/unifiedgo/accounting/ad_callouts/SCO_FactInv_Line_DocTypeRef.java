package pe.com.unifiedgo.accounting.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;

public class SCO_FactInv_Line_DocTypeRef extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */

    final String strOrgId = info.getStringParameter("inpadOrgId", IsIDFilter.instance);
    final String strDocTypeRefId = info.getStringParameter("inpdoctyperefId", null);
    final String strInvoiceRefId = info.getStringParameter("inpinvoicerefId", null);
    final String strCCurrencyId = info.getStringParameter("inpcCurrencyId", null);

    info.addResult("inpinvoicerefId", "");

  }

}
