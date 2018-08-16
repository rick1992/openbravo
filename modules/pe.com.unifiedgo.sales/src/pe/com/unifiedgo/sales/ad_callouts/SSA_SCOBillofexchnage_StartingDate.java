package pe.com.unifiedgo.sales.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.utils.FormatUtilities;

public class SSA_SCOBillofexchnage_StartingDate extends SimpleCallout {

  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    VariablesSecureApp vars = info.vars;
    String strchanged = vars.getStringParameter("inpstartingdate");
    try {
      info.addResult("inpdateacct", FormatUtilities.replaceJS(strchanged));
    } catch (Exception e) {
      log4j.info("Process failed populating accounting date from dategen");
    }
  }
}
