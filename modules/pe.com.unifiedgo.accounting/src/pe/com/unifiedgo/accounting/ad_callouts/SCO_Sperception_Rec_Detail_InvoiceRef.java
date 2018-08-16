package pe.com.unifiedgo.accounting.ad_callouts;

import java.util.List;

import javax.servlet.ServletException;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceTax;

public class SCO_Sperception_Rec_Detail_InvoiceRef extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */

    final String strOrgId = info.getStringParameter("inpadOrgId", IsIDFilter.instance);
    final String strDocTypeRefId = info.getStringParameter("inpdoctyperefId", null);
    final String strInvoiceRefId = info.getStringParameter("inpinvoicerefId", null);
    final String strInvoiceTaxRefId = info.getStringParameter("inpinvoicetaxrefId", null);
    boolean searchForMatch = false;

    if (strInvoiceRefId == null) {
      info.addResult("inpinvoicetaxrefId", "");
      return;
    } else if (strInvoiceRefId.equals("")) {
      info.addResult("inpinvoicetaxrefId", "");
      return;
    }

    if (strInvoiceTaxRefId != null && !strInvoiceTaxRefId.equals("")) {

      InvoiceTax invoice_tax = OBDal.getInstance().get(InvoiceTax.class, strInvoiceTaxRefId);
      if (invoice_tax == null) {
        info.addResult("inpinvoicetaxrefId", "");
        return;
      }

      if (invoice_tax.getInvoice().getId().equals(strInvoiceRefId)) {
        info.addResult("inpinvoicetaxrefId", strInvoiceTaxRefId);
      } else {
        searchForMatch = true;

      }
    } else {
      searchForMatch = true;
    }

    if (searchForMatch) {

      Invoice invoice = OBDal.getInstance().get(Invoice.class, strInvoiceRefId);

      OBCriteria<InvoiceTax> invoicetax_c = OBDal.getInstance().createCriteria(InvoiceTax.class);
      invoicetax_c.add(Restrictions.eq(InvoiceTax.PROPERTY_INVOICE, invoice));
      invoicetax_c.setMaxResults(1);

      List<InvoiceTax> invoicetaxes = invoicetax_c.list();
      if (invoicetaxes.size() == 0) {
        info.addResult("inpinvoicetaxrefId", "");
        return;
      }

      info.addResult("inpinvoicetaxrefId", invoicetaxes.get(0).getId());
    }

  }

}
