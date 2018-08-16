package pe.com.unifiedgo.accounting.ad_callouts;

import java.math.BigDecimal;
import java.util.List;

import javax.servlet.ServletException;

import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.financialmgmt.payment.FIN_Reconciliation;
import org.openbravo.model.financialmgmt.payment.FIN_ReconciliationLine_v;

public class SCO_Fixedcash_Rep_Reconciliation extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */

    String FIN_Reconciliation_ID = info.vars.getStringParameter("inpfinReconciliationId");
    if (FIN_Reconciliation_ID == null) {
      info.addResult("inpamount", new BigDecimal(0));
      return;
    }

    FIN_Reconciliation reconciliation = OBDal.getInstance().get(FIN_Reconciliation.class, FIN_Reconciliation_ID);

    if (reconciliation == null) {
      info.addResult("inpamount", new BigDecimal(0));
      return;
    }
    List<FIN_ReconciliationLine_v> reconlines = reconciliation.getFINReconciliationLineVList();
    BigDecimal withdrawalamt = new BigDecimal(0);
    for (int i = 0; i < reconlines.size(); i++) {
      withdrawalamt = withdrawalamt.add(reconlines.get(i).getPaymentAmount());
    }
    info.addResult("inpamount", withdrawalamt);

  }

}
