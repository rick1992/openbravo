package pe.com.unifiedgo.accounting.ad_callouts;

import java.util.List;

import javax.servlet.ServletException;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;

public class SCO_Pwithho_Red_Line_InvoiceRef extends SimpleCallout {
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
    final String strFINPaymentScheduleRefId = info.getStringParameter("inpfinPaymentSchedulerefId", null);
    boolean searchForMatch = false;

    if (strInvoiceRefId == null) {
      info.addResult("inpfinPaymentSchedulerefId", "");
      return;
    } else if (strInvoiceRefId.equals("")) {
      info.addResult("inpfinPaymentSchedulerefId", "");
      return;
    }

    if (strFINPaymentScheduleRefId != null && !strFINPaymentScheduleRefId.equals("")) {
      // check if the invoice is correct for the doctype
      FIN_PaymentSchedule payment_schedule = OBDal.getInstance().get(FIN_PaymentSchedule.class, strFINPaymentScheduleRefId);
      if (payment_schedule == null) {
        info.addResult("inpfinPaymentSchedulerefId", "");
        return;
      }

      if (payment_schedule.getInvoice().getId().equals(strInvoiceRefId) && payment_schedule.getScoPwithhoRecLi() == null) {
        info.addResult("inpfinPaymentSchedulerefId", strFINPaymentScheduleRefId);
      } else {
        searchForMatch = true;

      }
    } else {
      searchForMatch = true;
    }

    if (searchForMatch) {

      Invoice invoice = OBDal.getInstance().get(Invoice.class, strInvoiceRefId);

      OBCriteria<FIN_PaymentSchedule> paysched_c = OBDal.getInstance().createCriteria(FIN_PaymentSchedule.class);
      paysched_c.add(Restrictions.eq(FIN_PaymentSchedule.PROPERTY_INVOICE, invoice));

      paysched_c.add(Restrictions.isNull(FIN_PaymentSchedule.PROPERTY_SCOPWITHHORECLI));
      paysched_c.setMaxResults(1);

      List<FIN_PaymentSchedule> payscheds = paysched_c.list();
      if (payscheds.size() == 0) {
        info.addResult("inpfinPaymentSchedulerefId", "");
        return;
      }

      info.addResult("inpfinPaymentSchedulerefId", payscheds.get(0).getId());
    }

  }

}
