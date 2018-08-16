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
 * All portions are Copyright (C) 2013 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.ad_forms;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.common.invoice.InvoiceTaxCashVAT_V;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;

/**
 * Use for DocLine that must support Cash VAT regime
 * 
 * 
 */
public class DocLineCashVATReady_PaymentTransactionReconciliation extends DocLine {

  List<InvoiceTaxCashVAT_V> invoiceTaxCashVAT_V = null;

  public DocLineCashVATReady_PaymentTransactionReconciliation(String DocumentType, String TrxHeader_ID, String TrxLine_ID) {
    super(DocumentType, TrxHeader_ID, TrxLine_ID);
  }

  public List<InvoiceTaxCashVAT_V> getInvoiceTaxCashVAT_V() {
    return invoiceTaxCashVAT_V;
  }

  public void setInvoiceTaxCashVAT_V(List<InvoiceTaxCashVAT_V> invoiceTaxCashVAT_V) {
    this.invoiceTaxCashVAT_V = invoiceTaxCashVAT_V;
  }

  public void setInvoiceTaxCashVAT_V(String finPaymentDetailID) {
    if (StringUtils.isBlank(finPaymentDetailID)) {
      this.invoiceTaxCashVAT_V = new ArrayList<InvoiceTaxCashVAT_V>();
    } else {
      try {
        OBContext.setAdminMode(true);

        List<FIN_PaymentDetail> details = OBDal.getInstance().get(FIN_PaymentDetail.class, finPaymentDetailID).getFinPayment().getFINPaymentDetailList();
        String invoiceScheduleId = null;
        String orderFinPaymentDetailsId = null;
        for (int i = 0; i < details.size(); i++) {
          if (details.get(i).getId().equals(finPaymentDetailID)) {
            if (details.get(i).getFINPaymentScheduleDetailList().size() > 0 && details.get(i).getFINPaymentScheduleDetailList().get(0).getInvoicePaymentSchedule() != null) {
              invoiceScheduleId = details.get(i).getFINPaymentScheduleDetailList().get(0).getInvoicePaymentSchedule().getId();
            }
            break;
          }
        }
        if (invoiceScheduleId != null) {
          for (int i = 0; i < details.size(); i++) {
            if (!details.get(i).getId().equals(finPaymentDetailID) && details.get(i).getFINPaymentScheduleDetailList().size() > 0 && details.get(i).getFINPaymentScheduleDetailList().get(0).getInvoicePaymentSchedule() != null && details.get(i).getFINPaymentScheduleDetailList().get(0).getInvoicePaymentSchedule().getId().equals(invoiceScheduleId)) {
              // para 1paymentdetails que son de ordenschedule y invoiceschedule juntos y otros con
              // solo invoiceschedule
              orderFinPaymentDetailsId = details.get(i).getId();
              break;
            }
          }
        }

        final StringBuffer hql = new StringBuffer();
        hql.append(" as itcv ");
        hql.append(" where (itcv." + InvoiceTaxCashVAT_V.PROPERTY_PAYMENTDETAILS + ".id = :finPaymentDetailID OR itcv." + InvoiceTaxCashVAT_V.PROPERTY_PAYMENTDETAILS + ".id = :finPaymentDetailID2 )");
        hql.append(" and itcv." + InvoiceTaxCashVAT_V.PROPERTY_CANCELED + " = false");
        OBQuery<InvoiceTaxCashVAT_V> obq = OBDal.getInstance().createQuery(InvoiceTaxCashVAT_V.class, hql.toString());
        obq.setNamedParameter("finPaymentDetailID", finPaymentDetailID);
        if (orderFinPaymentDetailsId != null)
          obq.setNamedParameter("finPaymentDetailID2", orderFinPaymentDetailsId);
        else
          obq.setNamedParameter("finPaymentDetailID2", finPaymentDetailID);

        obq.setFilterOnReadableClients(false);
        obq.setFilterOnReadableOrganization(false);

        this.invoiceTaxCashVAT_V = obq.list();
      } finally {
        OBContext.restorePreviousMode();
      }
    }
  }
}