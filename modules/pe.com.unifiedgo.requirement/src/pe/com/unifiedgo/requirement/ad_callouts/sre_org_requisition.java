package pe.com.unifiedgo.requirement.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.erpCommon.ad_callouts.SimpleCallout;

//import org.apache.jasper.tagplugins.jstl.core.Out;

public class sre_org_requisition extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    // Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
    // { System.out.println(params.nextElement()); }

    // String SalesOrderId = "";
    final String strBPartner = info.getStringParameter("inpcBpartnerId", null);
    String strChanged = info.vars.getStringParameter("inpLastFieldChanged");
    if (log4j.isDebugEnabled())
      log4j.debug("CHANGED: " + strChanged);

    if ("inpadOrgId".equals(strChanged)) { // Organization changed
      // setting null to bp and bp location
      info.addResult("inpcBpartnerId", null);
    }

    info.addResult("inpemSreSalesorderId", null);

  }

}