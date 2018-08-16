package pe.com.unifiedgo.accounting.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.gl.GLJournal;
import org.openbravo.service.db.DalConnectionProvider;

public class SCO_PLE8_14_Reg_Invoice extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */

    final String strcInvoiceId = info.getStringParameter("inpcInvoiceId", IsIDFilter.instance);
    if (strcInvoiceId.isEmpty()) {
      info.addResult("inpscoRegnumber", "");
      info.addResult("inpscoSeqno", "");
      return;
    }

    Invoice inv = OBDal.getInstance().get(Invoice.class, strcInvoiceId);
    if (inv == null) {
      info.addResult("inpscoRegnumber", "");
      info.addResult("inpscoSeqno", "");
      return;
    }

    if (inv.isScoIsmanualTranstax()) {
      GLJournal journal = inv.getScoMtrtaxGljournal();
      if (journal == null) {
        info.addResult("inpscoRegnumber", "");
        info.addResult("inpscoSeqno", "");
        return;
      }

      String regnumber = SCOPLE814GetRegnumberData.getRegNumberFromFactAcct(
          new DalConnectionProvider(), journal.getClient().getId(), "224", journal.getId());
      if (regnumber != null && !regnumber.isEmpty()) {
        info.addResult("inpscoRegnumber", regnumber);
        info.addResult("inpscoSeqno", 1);
      } else {
        info.addResult("inpscoRegnumber", "");
        info.addResult("inpscoSeqno", "");
        return;
      }

    } else {
      info.addResult("inpscoRegnumber", inv.getScoRegnumber());
      info.addResult("inpscoSeqno", inv.getScoSeqno());
    }

  }

}
