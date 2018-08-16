package pe.com.unifiedgo.accounting.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;

public class SCO_Autodetrac_Doc_Organization extends SimpleCallout {
  private static final long serialVersionUID = 1L;
  private static final RequestFilter filterYesNo = new ValueListFilter("Y", "N");

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    final String strinpissotrx = info.getStringParameter("inpissotrx", filterYesNo);
    final String strOrgId = info.getStringParameter("inpadOrgId", IsIDFilter.instance);

    info.addResult("inpcBpartnerId", null);
    info.addResult("inpdoctyperefId", null);
    info.addResult("inpinvoicerefId", null);
  }

}
