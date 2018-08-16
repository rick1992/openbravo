package pe.com.unifiedgo.accounting.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;

public class SE_RendCuentas_AccountingDate extends SimpleCallout {

  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    VariablesSecureApp vars = info.vars;
    String strchanged = vars.getStringParameter("inpdategen");
    try {
    } catch (Exception e) {
      log4j.info("Process failed populating accounting date from open date");
    }
  }
}
