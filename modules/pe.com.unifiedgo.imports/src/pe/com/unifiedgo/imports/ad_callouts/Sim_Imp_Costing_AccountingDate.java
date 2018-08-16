package pe.com.unifiedgo.imports.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.utils.FormatUtilities;

public class Sim_Imp_Costing_AccountingDate extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    VariablesSecureApp vars = info.vars;
    String strchanged = vars.getStringParameter("inpdateordered");
    try {
      info.addResult("inpdateacct", FormatUtilities.replaceJS(strchanged));
    } catch (Exception e) {
      log4j.info("Process failed populating accounting date from invoice date");
    }
  }

}
