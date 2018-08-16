package pe.com.unifiedgo.accounting.ad_callouts;

import javax.servlet.ServletException;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.FinAccPaymentMethod;

public class SCO_FinancialAcc_Paymeth_Repdefault extends SimpleCallout {

  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    /*
     * Enumeration<String> e = info.vars.getParameterNames(); while (e.hasMoreElements()) {
     * System.out.println(e.nextElement()); }
     */

    String srtPaymentMethodId = info.getStringParameter("inpemScoPaymethRepdefaultId", IsIDFilter.instance);
    String srtOrgId = info.getStringParameter("inpadOrgId", IsIDFilter.instance);

    FIN_PaymentMethod paymentMethod = OBDal.getInstance().get(FIN_PaymentMethod.class, srtPaymentMethodId);

    info.addSelect("inpemScoFinaccRepdefaultId");
    String srtSelectedFinancialAccount = info.getStringParameter("inpemScoFinaccRepdefaultId", IsIDFilter.instance);

    boolean isSelected = true;

    // No Payment Method selected
    if (srtPaymentMethodId.isEmpty()) {
      OBCriteria<FIN_FinancialAccount> obc = OBDal.getInstance().createCriteria(FIN_FinancialAccount.class);
      obc.add(Restrictions.in("organization.id", OBContext.getOBContext().getOrganizationStructureProvider().getNaturalTree(srtOrgId)));
      obc.add(Restrictions.ne(FIN_FinancialAccount.PROPERTY_SCOFORAPPPAYMENT, true));
      obc.setFilterOnReadableOrganization(false);
      for (FIN_FinancialAccount acc : obc.list()) {
        info.addSelectResult(acc.getId(), acc.getIdentifier());
      }

    } else {
      OBCriteria<FinAccPaymentMethod> obc = OBDal.getInstance().createCriteria(FinAccPaymentMethod.class);
      obc.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD, paymentMethod));
      obc.add(Restrictions.in("organization.id", OBContext.getOBContext().getOrganizationStructureProvider().getNaturalTree(srtOrgId)));
      obc.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_PAYOUTALLOW, true));
      obc.createAlias(FinAccPaymentMethod.PROPERTY_ACCOUNT, "finacc");
      obc.add(Restrictions.ne("finacc." + FIN_FinancialAccount.PROPERTY_SCOFORAPPPAYMENT, true));

      for (FinAccPaymentMethod accPm : obc.list()) {
        if (accPm.getAccount().isActive()) {
          if (srtSelectedFinancialAccount.equals(accPm.getAccount().getId())) {
            isSelected = true;
          } else if (srtSelectedFinancialAccount.isEmpty()) {
            srtSelectedFinancialAccount = accPm.getAccount().getIdentifier();
            isSelected = true;
          }
          info.addSelectResult(accPm.getAccount().getId(), accPm.getAccount().getIdentifier(), isSelected);
        }
        isSelected = false;
      }

    }

    info.endSelect();
  }
}
