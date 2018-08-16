package pe.com.unifiedgo.accounting.ad_callouts;

import java.util.List;

import javax.servlet.ServletException;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.invoice.Invoice;

public class SCO_PercepPurch_Detail_DocTypeRef extends SimpleCallout {
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
    final String strCBPartnerId = info.getStringParameter("inpcBpartnerId", null);

    boolean searchForMatch = false;

    if (strDocTypeRefId == null) {
      info.addResult("inpinvoicerefId", "");
      return;
    } else if (strDocTypeRefId.equals("")) {
      info.addResult("inpinvoicerefId", "");
      return;
    }

    if (strInvoiceRefId != null && !strInvoiceRefId.equals("")) {
      // check if the invoice is correct for the doctype
      Invoice invoice = OBDal.getInstance().get(Invoice.class, strInvoiceRefId);
      if (invoice == null) {
        info.addResult("inpinvoicerefId", "");
        return;
      }

      if (invoice.getTransactionDocument().getId().equals(strDocTypeRefId) && invoice.getDocumentStatus().equals("CO") && invoice.getBusinessPartner().getId().equals(strCBPartnerId)) {
        info.addResult("inpinvoicerefId", strInvoiceRefId);
      } else {
        searchForMatch = true;

      }
    } else {
      searchForMatch = true;
    }

    if (searchForMatch) {

      DocumentType doctype = OBDal.getInstance().get(DocumentType.class, strDocTypeRefId);
      BusinessPartner bpartner = OBDal.getInstance().get(BusinessPartner.class, strCBPartnerId);

      OBCriteria<Invoice> invoice_c = OBDal.getInstance().createCriteria(Invoice.class);
      invoice_c.add(Restrictions.sqlRestriction("AD_ISORGINCLUDED( ad_org_id, " + "'" + strOrgId + "','" + doctype.getClient().getId() + "') > -1"));

      invoice_c.add(Restrictions.eq(Invoice.PROPERTY_TRANSACTIONDOCUMENT, doctype));
      invoice_c.add(Restrictions.eq(Invoice.PROPERTY_DOCUMENTSTATUS, "CO"));
      invoice_c.add(Restrictions.eq(Invoice.PROPERTY_BUSINESSPARTNER, bpartner));

      List<Invoice> invoices = invoice_c.list();
      if (invoices.size() == 0) {
        info.addResult("inpinvoicerefId", "");
        return;
      }

      info.addResult("inpinvoicerefId", invoices.get(0).getId());
    }

  }

}
