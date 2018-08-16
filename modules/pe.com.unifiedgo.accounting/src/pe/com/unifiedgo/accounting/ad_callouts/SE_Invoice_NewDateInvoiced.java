package pe.com.unifiedgo.accounting.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.utils.FormatUtilities;

public class SE_Invoice_NewDateInvoiced extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */
    String strnewdateinvoiced = info.vars.getStringParameter("inpemScoNewdateinvoiced");
    String issotrx = info.vars.getStringParameter("inpissotrx");

    if (issotrx.equals("N")) {
      info.addResult("inpdateinvoiced", FormatUtilities.replaceJS(strnewdateinvoiced));
      info.addResult("inpdateacct", FormatUtilities.replaceJS(strnewdateinvoiced));
    }

  }

}
