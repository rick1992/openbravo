package pe.com.unifiedgo.accounting.ad_callouts;

import java.util.Enumeration;

import javax.servlet.ServletException;

import org.openbravo.erpCommon.ad_callouts.SimpleCallout;

public class SCO_Glitem_Acct_Acctschema extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    Enumeration<String> params = info.vars.getParameterNames();
    while (params.hasMoreElements()) {
      System.out.println(params.nextElement());
    }

    info.addResult("inpglitemDebitAcct", "");
    info.addResult("inpglitemCreditAcct", "");

  }

}
