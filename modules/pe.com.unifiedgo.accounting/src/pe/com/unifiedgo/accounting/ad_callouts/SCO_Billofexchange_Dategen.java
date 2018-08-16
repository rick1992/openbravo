package pe.com.unifiedgo.accounting.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.utils.FormatUtilities;

public class SCO_Billofexchange_Dategen extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */
    String strdategen = info.vars.getStringParameter("inpdategen");
    // String issotrx = info.vars.getStringParameter("inpissotrx");

    // if (issotrx.equals("N")) {
    info.addResult("inpstartingdate", FormatUtilities.replaceJS(strdategen));
    // }

  }

}
