/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2010-2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_callouts;

import java.util.List;

import javax.servlet.ServletException;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.FinAccPaymentMethod;

public class SE_PaymentMethod_FinAccount extends SimpleCallout {

  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    /*
     * Enumeration<String> e = info.vars.getParameterNames(); while (e.hasMoreElements()) {
     * System.out.println(e.nextElement()); }
     */

    String srtPaymentMethodId = info.getStringParameter("inpfinPaymentmethodId",
        IsIDFilter.instance);
    String srtPOPaymentMethodId = info.getStringParameter("inppoPaymentmethodId",
        IsIDFilter.instance);
    String strIsAppPayment = info.getStringParameter("inpemScoIsapppayment", null);
    String strAPApplicationtype = info.getStringParameter("inpemScoApplicationtype", null);
    String strARApplicationtype = info.getStringParameter("inpemScoRecvapplicationtype", null);
    String strSsaPaymentInDocType = info.getStringParameter("inpemSsaPaymentinDoctype", null);
    User user = OBContext.getOBContext().getUser();

    boolean isapppayment = false;
    if (strIsAppPayment != null) {
      if (strIsAppPayment.equals("Y"))
        isapppayment = true;
    }

    boolean iscompensationpayment = false;
    if ((strAPApplicationtype != null && strAPApplicationtype.compareTo("SCO_COMP") == 0)
        || (strARApplicationtype != null && strARApplicationtype.compareTo("SCO_COMP") == 0)) {
      iscompensationpayment = true;
    }

    if (strSsaPaymentInDocType == null || strSsaPaymentInDocType.isEmpty()) {
      strSsaPaymentInDocType = "GENERIC";
    }

    String tabId = info.getTabId();
    boolean isVendorTab = "224".equals(tabId);
    String finIsReceipt = info.getStringParameter("inpisreceipt", null);
    boolean isPaymentOut = isVendorTab || "N".equals(finIsReceipt);
    String srtOrgId = info.getStringParameter("inpadOrgId", IsIDFilter.instance);

    FIN_PaymentMethod paymentMethod = OBDal.getInstance().get(FIN_PaymentMethod.class,
        isVendorTab ? srtPOPaymentMethodId : srtPaymentMethodId);

    info.addSelect(isVendorTab ? "inppoFinancialAccountId" : "inpfinFinancialAccountId");
    String srtSelectedFinancialAccount = info.getStringParameter(
        isVendorTab ? "inppoFinancialAccountId" : "inpfinFinancialAccountId", IsIDFilter.instance);

    boolean isSelected = true;
    boolean isMultiCurrencyEnabled = false;

    // No Payment Method selected
    if (srtPaymentMethodId.isEmpty() && srtPOPaymentMethodId.isEmpty()) {

      OBCriteria<FIN_FinancialAccount> obc = OBDal.getInstance()
          .createCriteria(FIN_FinancialAccount.class);
      obc.add(Restrictions.in("organization.id",
          OBContext.getOBContext().getOrganizationStructureProvider().getNaturalTree(srtOrgId)));
      obc.add(Restrictions.eq(FIN_FinancialAccount.PROPERTY_SCOFORAPPPAYMENT, isapppayment));

      if (iscompensationpayment) {
        obc.add(Restrictions.eq(FIN_FinancialAccount.PROPERTY_TYPE, "SCO_LO"));
      } else {
        obc.add(Restrictions.ne(FIN_FinancialAccount.PROPERTY_TYPE, "SCO_LO"));
      }

      if (!isPaymentOut) {
        if ("INMEDIATE_CASH".equals(strSsaPaymentInDocType)) {
          // Cuenta financiera tipo: Caja
          obc.add(Restrictions.eq(FIN_FinancialAccount.PROPERTY_TYPE, "C"));
        } else { // GENERIC
          obc.add(
              Restrictions.ne("finacc." + FIN_FinancialAccount.PROPERTY_TYPE, "SSA_DISC_STAFF"));
        }
      }

      if (!isapppayment) {
        obc.add(Restrictions.sqlRestriction(
            "((SELECT count(*) FROM SCO_User_Perm_Finaccount UPF where UPF.ad_user_id = '"
                + user.getId()
                + "')=0 OR this_.fin_financial_account_id IN (SELECT UPF.fin_financial_account_id FROM SCO_User_Perm_Finaccount UPF WHERE UPF.ad_user_id = '"
                + user.getId() + "' AND UPF.isactive='Y'))"));
      }

      obc.setFilterOnReadableOrganization(false);

      List<FIN_FinancialAccount> accs = obc.list();

      for (FIN_FinancialAccount acc : accs) {
        info.addSelectResult(acc.getId(), acc.getIdentifier());
      }

    } else {

      OBCriteria<FinAccPaymentMethod> obc = OBDal.getInstance()
          .createCriteria(FinAccPaymentMethod.class);
      obc.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD, paymentMethod));
      obc.add(Restrictions.in("organization.id",
          OBContext.getOBContext().getOrganizationStructureProvider().getNaturalTree(srtOrgId)));
      if (isPaymentOut) {
        obc.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_PAYOUTALLOW, true));
      } else {
        obc.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_PAYINALLOW, true));
      }
      obc.createAlias(FinAccPaymentMethod.PROPERTY_ACCOUNT, "finacc");
      obc.add(Restrictions.eq("finacc." + FIN_FinancialAccount.PROPERTY_SCOFORAPPPAYMENT,
          isapppayment));

      if (iscompensationpayment) {
        obc.add(Restrictions.eq("finacc." + FIN_FinancialAccount.PROPERTY_TYPE, "SCO_LO"));
      } else {
        obc.add(Restrictions.ne("finacc." + FIN_FinancialAccount.PROPERTY_TYPE, "SCO_LO"));
      }

      if (!isPaymentOut) {
        if ("INMEDIATE_CASH".equals(strSsaPaymentInDocType)) {
          // Cuenta financiera tipo: Caja
          obc.add(Restrictions.eq("finacc." + FIN_FinancialAccount.PROPERTY_TYPE, "C"));
        } else { // GENERIC
          obc.add(
              Restrictions.ne("finacc." + FIN_FinancialAccount.PROPERTY_TYPE, "SSA_DISC_STAFF"));
        }
      }

      if (!isapppayment) {
        obc.add(Restrictions.sqlRestriction(
            "((SELECT count(*) FROM SCO_User_Perm_Finaccount UPF where UPF.ad_user_id = '"
                + user.getId()
                + "')=0 OR finacc1_.fin_financial_account_id IN (SELECT UPF.fin_financial_account_id FROM SCO_User_Perm_Finaccount UPF WHERE UPF.ad_user_id = '"
                + user.getId() + "' AND UPF.isactive='Y'))"));
      }

      FinAccPaymentMethod selectedPaymentMethod = null;
      int added = 0;
      for (FinAccPaymentMethod accPm : obc.list()) {
        if (accPm.getAccount().isActive()) {
          if (srtSelectedFinancialAccount.equals(accPm.getAccount().getId())) {
            isSelected = true;
          } else if (srtSelectedFinancialAccount.isEmpty()) {
            srtSelectedFinancialAccount = accPm.getAccount().getIdentifier();
            isSelected = true;
          }
          selectedPaymentMethod = accPm;

          info.addSelectResult(accPm.getAccount().getId(), accPm.getAccount().getIdentifier(),
              isSelected);

          added++;

        }
        isSelected = false;
      }

      if (isapppayment && (added == 0)) {
        // if is apppayment and there wasnt result then show all (OVERWRITE BECAUSE SCO_COMP)
        /*
         * OBCriteria<FinAccPaymentMethod> obc_ = OBDal.getInstance().createCriteria(
         * FinAccPaymentMethod.class);
         * obc_.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD, paymentMethod));
         * obc_.add(Restrictions.in("organization.id", OBContext.getOBContext()
         * .getOrganizationStructureProvider().getNaturalTree(srtOrgId))); if (isPaymentOut) {
         * obc_.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_PAYOUTALLOW, true)); } else {
         * obc_.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_PAYINALLOW, true)); }
         * 
         * selectedPaymentMethod = null; for (FinAccPaymentMethod accPm : obc_.list()) { if
         * (accPm.getAccount().isActive()) { if
         * (srtSelectedFinancialAccount.equals(accPm.getAccount().getId())) { isSelected = true; }
         * else if (srtSelectedFinancialAccount.isEmpty()) { srtSelectedFinancialAccount =
         * accPm.getAccount().getIdentifier(); isSelected = true; } selectedPaymentMethod = accPm;
         * 
         * info.addSelectResult(accPm.getAccount().getId(), accPm.getAccount().getIdentifier(),
         * isSelected);
         * 
         * } isSelected = false; }
         */
      }

      if (selectedPaymentMethod != null) {
        if (isPaymentOut) {
          isMultiCurrencyEnabled = selectedPaymentMethod.isPayoutAllow()
              && selectedPaymentMethod.isPayoutIsMulticurrency();
        } else {
          isMultiCurrencyEnabled = selectedPaymentMethod.isPayinAllow()
              && selectedPaymentMethod.isPayinIsMulticurrency();
        }
      }

    }

    info.endSelect();
    info.addResult("inpismulticurrencyenabled", isMultiCurrencyEnabled ? "Y" : "N");

    // String scrSpecialPaymentMethod = "NO";
    // if (paymentMethod != null) {
    // if (paymentMethod.getScoSpecialmethod() != null) {
    // scrSpecialPaymentMethod = paymentMethod.getScoSpecialmethod();
    // }
    // }
    // info.addResult("inpemScrSpecialpaymentmethod", scrSpecialPaymentMethod);

    String specialmethod = null;
    String pospecialmethod = null;
    FIN_PaymentMethod paymentmethod_ = OBDal.getInstance().get(FIN_PaymentMethod.class,
        srtPaymentMethodId);
    FIN_PaymentMethod popaymentmethod_ = OBDal.getInstance().get(FIN_PaymentMethod.class,
        srtPOPaymentMethodId);

    if (paymentmethod_ != null) {
      specialmethod = paymentmethod_.getScoSpecialmethod();
    }
    if (popaymentmethod_ != null) {
      pospecialmethod = popaymentmethod_.getScoSpecialmethod();
    }

    if (specialmethod != null) {
      info.addResult("inpemScoSpecialmethod", specialmethod);
      info.addResult("inpemScrSpecialpaymentmethod", specialmethod);

      // bill of exchange payment in - type
      if (specialmethod.compareTo("SCOBILLOFEXCHANGE") != 0) {
        info.addResult("inpemScrPaymenttype", "");
      }

    } else {
      info.addResult("inpemScoSpecialmethod", "");
      info.addResult("inpemScrSpecialpaymentmethod", "");
    }

    if (pospecialmethod != null) {
      info.addResult("inpemScoPospecialmethod", pospecialmethod);
    } else {
      info.addResult("inpemScoPospecialmethod", "");
    }

    // if (paymentmethod_ != null) {
    // List<PaymentTerm> payment_term = SCO_Utils.getPayTermListByPayMethodObj(paymentmethod_);
    //
    // info.addSelect("inpcPaymenttermId");
    // for (int i = 0; i < payment_term.size(); i++) {
    // info.addSelectResult(payment_term.get(i).getId(), payment_term.get(i).getName(), true);
    // }
    // info.endSelect();
    // }
    // if (popaymentmethod_ != null) {
    // List<PaymentTerm> po_payment_term = SCO_Utils.getPayTermListByPayMethodObj(popaymentmethod_);
    //
    // info.addSelect("inppoPaymenttermId");
    // for (int i = 0; i < po_payment_term.size(); i++) {
    // info.addSelectResult(po_payment_term.get(i).getId(), po_payment_term.get(i).getName(), true);
    // }
    // info.endSelect();
    // }
  }
}
