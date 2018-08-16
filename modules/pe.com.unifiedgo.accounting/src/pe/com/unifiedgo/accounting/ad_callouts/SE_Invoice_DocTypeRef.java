package pe.com.unifiedgo.accounting.ad_callouts;

import java.math.BigDecimal;
import java.util.List;

import javax.servlet.ServletException;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.invoice.Invoice;

public class SE_Invoice_DocTypeRef extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */

    final String strOrgId = info.getStringParameter("inpadOrgId", IsIDFilter.instance);
    final String strDocTypeRefId = info.getStringParameter("inpemScoDoctyperefId", null);
    final String strInvoiceRefId = info.getStringParameter("inpemScoInvoicerefId", null);
    final String strCBPartnerId = info.getStringParameter("inpcBpartnerId", null);
    final String strCCurrencyId = info.getStringParameter("inpcCurrencyId", null);

    boolean searchForMatch = false;

    if (strDocTypeRefId == null) {
      info.addResult("inpemScoInvoicerefId", "");
      return;
    } else if (strDocTypeRefId.equals("")) {
      info.addResult("inpemScoInvoicerefId", "");
      return;
    }

    if (strInvoiceRefId != null && !strInvoiceRefId.equals("")) {
      // check if the invoice is correct for the doctype and cbpartner
      Invoice invoice = OBDal.getInstance().get(Invoice.class, strInvoiceRefId);
      if (invoice == null) {
        info.addResult("inpemScoInvoicerefId", "");
        return;
      }

      if (invoice.getTransactionDocument().getId().equals(strDocTypeRefId) && invoice.getDocumentStatus().equals("CO") && invoice.isPaymentComplete() == false && invoice.getBusinessPartner().getId().equals(strCBPartnerId) && invoice.getCurrency().getId().equals(strCCurrencyId) && invoice.getGrandTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
        info.addResult("inpemScoInvoicerefId", strInvoiceRefId);
      } else {
        searchForMatch = true;

      }
    } else {
      searchForMatch = true;
    }

    if (searchForMatch) {

      DocumentType doctype = OBDal.getInstance().get(DocumentType.class, strDocTypeRefId);
      BusinessPartner bpartner = OBDal.getInstance().get(BusinessPartner.class, strCBPartnerId);
      Currency currency = OBDal.getInstance().get(Currency.class, strCCurrencyId);

      OBCriteria<Invoice> invoice_c = OBDal.getInstance().createCriteria(Invoice.class);
      invoice_c.add(Restrictions.sqlRestriction("AD_ISORGINCLUDED( ad_org_id, " + "'" + strOrgId + "','" + doctype.getClient().getId() + "') > -1"));

      invoice_c.add(Restrictions.eq(Invoice.PROPERTY_TRANSACTIONDOCUMENT, doctype));
      invoice_c.add(Restrictions.eq(Invoice.PROPERTY_BUSINESSPARTNER, bpartner));
      invoice_c.add(Restrictions.eq(Invoice.PROPERTY_CURRENCY, currency));
      invoice_c.add(Restrictions.eq(Invoice.PROPERTY_DOCUMENTSTATUS, "CO"));
      invoice_c.add(Restrictions.gt(Invoice.PROPERTY_GRANDTOTALAMOUNT, new BigDecimal(0)));
      invoice_c.setMaxResults(1);

      List<Invoice> invoices = invoice_c.list();
      if (invoices.size() == 0) {
        info.addResult("inpemScoInvoicerefId", "");
        return;
      }

      info.addResult("inpemScoInvoicerefId", invoices.get(0).getId());
    }

  }

}
