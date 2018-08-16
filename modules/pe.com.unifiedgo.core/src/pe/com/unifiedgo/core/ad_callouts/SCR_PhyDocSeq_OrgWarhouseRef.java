package pe.com.unifiedgo.core.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.erpCommon.ad_callouts.SimpleCallout;

public class SCR_PhyDocSeq_OrgWarhouseRef extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    // Enumeration<String> params = info.vars.getParameterNames();
    // while (params.hasMoreElements()) {
    // System.out.println(params.nextElement());
    // }

    info.addResult("inpmWarehouseId", null);
    info.addResult("inpbillPhydocSequenceId", null);

  }
}
