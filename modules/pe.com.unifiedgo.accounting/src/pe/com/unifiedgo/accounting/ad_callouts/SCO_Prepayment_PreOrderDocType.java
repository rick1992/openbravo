package pe.com.unifiedgo.accounting.ad_callouts;

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
import org.openbravo.model.common.order.Order;

public class SCO_Prepayment_PreOrderDocType extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */

    final String strOrgId = info.getStringParameter("inpadOrgId", IsIDFilter.instance);
    final String strDocTypeRefId = info.getStringParameter("inppreorderdoctypeId", null);
    final String strPreorderId = info.getStringParameter("inpcPreorderId", null);
    final String strCBPartnerId = info.getStringParameter("inpcBpartnerId", null);
    final String strCCurrencyId = info.getStringParameter("inpcCurrencyId", null);

    boolean searchForMatch = false;

    if (strDocTypeRefId == null) {
      info.addResult("inpcPreorderId", "");
      return;
    } else if (strDocTypeRefId.equals("")) {
      info.addResult("inpcPreorderId", "");
      return;
    }

    if (strPreorderId != null && !strPreorderId.equals("")) {
      // check if the order is correct for the doctype and cbpartner and currency
      Order order = OBDal.getInstance().get(Order.class, strPreorderId);
      if (order == null) {
        info.addResult("inpcPreorderId", "");
        return;
      }

      if (order.getTransactionDocument().getId().equals(strDocTypeRefId) && order.getDocumentStatus().equals("CO") && order.isReinvoice() == false && order.getBusinessPartner().getId().equals(strCBPartnerId) && order.getCurrency().getId().equals(strCCurrencyId)) {
        info.addResult("inpcPreorderId", strPreorderId);
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

      OBCriteria<Order> order_c = OBDal.getInstance().createCriteria(Order.class);
      order_c.add(Restrictions.sqlRestriction("AD_ISORGINCLUDED( ad_org_id, " + "'" + strOrgId + "','" + doctype.getClient().getId() + "') > -1"));
      order_c.add(Restrictions.eq(Order.PROPERTY_TRANSACTIONDOCUMENT, doctype));
      order_c.add(Restrictions.eq(Order.PROPERTY_DOCUMENTSTATUS, "CO"));

      order_c.add(Restrictions.eq(Order.PROPERTY_REINVOICE, false));

      order_c.add(Restrictions.eq(Order.PROPERTY_BUSINESSPARTNER, bpartner));
      order_c.add(Restrictions.eq(Order.PROPERTY_CURRENCY, currency));

      List<Order> orders = order_c.list();
      if (orders.size() == 0) {
        info.addResult("inpcPreorderId", "");
        return;
      }

      info.addResult("inpcPreorderId", orders.get(0).getId());
    }

  }

}
