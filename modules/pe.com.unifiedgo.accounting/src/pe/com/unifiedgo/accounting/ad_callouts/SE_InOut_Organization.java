package pe.com.unifiedgo.accounting.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;

public class SE_InOut_Organization extends SimpleCallout {
  private static final long serialVersionUID = 1L;
  private static final RequestFilter filterYesNo = new ValueListFilter("Y", "N");

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */
    final String strinpissotrx = info.getStringParameter("inpissotrx", filterYesNo);
    final String strOrgId = info.getStringParameter("inpadOrgId", IsIDFilter.instance);
    final String strBPartner = info.getStringParameter("inpcBpartnerId", null);
    String strChanged = info.vars.getStringParameter("inpLastFieldChanged");
    if (log4j.isDebugEnabled())
      log4j.debug("CHANGED: " + strChanged);

    if ("inpadOrgId".equals(strChanged)) { // Organization changed
      // setting null to bp and bp location
      info.addResult("inpcBpartnerLocationId", null);
      info.addResult("inpcBpartnerId", null);
    }

    info.addResult("inpmWarehouseId", null);
    info.addResult("inpemSwaMLocatorId", null);

  }

}
