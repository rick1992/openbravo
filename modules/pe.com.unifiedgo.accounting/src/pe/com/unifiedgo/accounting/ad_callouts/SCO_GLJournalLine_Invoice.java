package pe.com.unifiedgo.accounting.ad_callouts;

import java.math.BigDecimal;

import javax.servlet.ServletException;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.gl.GLJournal;
import org.openbravo.service.db.DalConnectionProvider;

import pe.com.unifiedgo.accounting.SCO_Utils;

public class SCO_GLJournalLine_Invoice extends SimpleCallout {
  private static final long serialVersionUID = 1L;
  private static final RequestFilter filterYesNo = new ValueListFilter("Y", "N");

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */

    final String strinpissotrx = info.getStringParameter("inpissotrx", filterYesNo);
    final String strinvoicerefId = info.getStringParameter("inpemScoCInvoiceId", IsIDFilter.instance);
    final String strglJournalId = info.getStringParameter("inpglJournalId", IsIDFilter.instance);

    GLJournal journal = OBDal.getInstance().get(GLJournal.class, strglJournalId);
    if (journal == null) {
      return;
    }
    Invoice inv = OBDal.getInstance().get(Invoice.class, strinvoicerefId);
    if (inv == null) {
      info.addResult("inpcurrencyrate", journal.getRate());
      return;
    }
    if (journal.getCurrency().getId().compareTo(journal.getAccountingSchema().getCurrency().getId()) != 0 && inv.getCurrency().getId().compareTo(journal.getCurrency().getId()) == 0) {

      String multiplyrate = SCO_Utils.getInvoiceMultiplyRate(new DalConnectionProvider(), inv.getId(), journal.getAccountingSchema().getCurrency().getId());
      if (multiplyrate == null) {
        info.addResult("inpcurrencyrate", "");
        return;
      }

      info.addResult("inpcurrencyrate", new BigDecimal(multiplyrate));
    }
  }

}
