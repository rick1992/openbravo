package pe.com.unifiedgo.core.ad_callouts;

import java.util.Enumeration;

import javax.servlet.ServletException;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;

public class C_Invoice_Scr_Fin_Finacc extends SimpleCallout {
  private static final long serialVersionUID = 1L;
  private static final RequestFilter filterYesNo = new ValueListFilter("Y", "N");

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    Enumeration<String> params = info.vars.getParameterNames();
    while (params.hasMoreElements()) {
      System.out.println(params.nextElement());
    }

    final String strinpissotrx = info.getStringParameter("inpissotrx", filterYesNo);
    final String strOrgId = info.getStringParameter("inpadOrgId", IsIDFilter.instance);
    final String strFinaccId = info.getStringParameter("inpemScrFinFinaccountId",
        IsIDFilter.instance);

    if (strFinaccId == null) {
      info.addResult("inpemScoIsautomatictrx", false);
      return;
    }

    FIN_FinancialAccount finacc = OBDal.getInstance().get(FIN_FinancialAccount.class, strFinaccId);
    if (finacc == null) {
      info.addResult("inpemScoIsautomatictrx", false);
      return;
    }
    // info.addResult("inpemScoIsautomatictrx", false);

  }

}
