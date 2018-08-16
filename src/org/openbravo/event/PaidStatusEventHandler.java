/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2014 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.event;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;

public class PaidStatusEventHandler extends EntityPersistenceEventObserver {
  private static Entity[] entities = { ModelProvider.getInstance().getEntity(
      FIN_FinaccTransaction.ENTITY_NAME) };
  protected Logger logger = Logger.getLogger(this.getClass());
  public static final String STATUS_CLEARED = "RPPC";
  public static final String STATUS_DEPOSIT = "RDNC";
  public static final String STATUS_WITHDRAWN = "PWNC";

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onUpdate(@Observes
  EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    final Entity transactionEntity = ModelProvider.getInstance().getEntity(
        FIN_FinaccTransaction.ENTITY_NAME);
    final Property statusProperty = transactionEntity
        .getProperty(FIN_FinaccTransaction.PROPERTY_STATUS);
    String oldStatus = (String) event.getPreviousState(statusProperty);
    String newStatus = (String) event.getCurrentState(statusProperty);
    final FIN_FinaccTransaction transaction = (FIN_FinaccTransaction) event.getTargetInstance();

    if ((oldStatus.equals(STATUS_DEPOSIT) | oldStatus.equals(STATUS_WITHDRAWN))
        & newStatus.equals(STATUS_CLEARED)) {

      Boolean invoicePaidold = false;

      if (transaction.getFinPayment() != null) {
        for (FIN_PaymentDetail pd : transaction.getFinPayment().getFINPaymentDetailList()) {
          for (FIN_PaymentScheduleDetail psd : pd.getFINPaymentScheduleDetailList()) {
            invoicePaidold = psd.isInvoicePaid();
            if (!invoicePaidold) {
              if (newStatus.equals(transaction.getFinPayment().getStatus())) {
                psd.setInvoicePaid(true);
              }
              if (psd.isInvoicePaid()) {
                FIN_Utility.updatePaymentAmounts(pd,psd);
                FIN_Utility.updateBusinessPartnerCredit(transaction.getFinPayment());
              }
            }
          }
        }
      }

    } else if ((newStatus.equals(STATUS_DEPOSIT) | newStatus.equals(STATUS_WITHDRAWN))
        & oldStatus.equals(STATUS_CLEARED)) {
      Boolean invoicePaidold = false;
      if (transaction.getFinPayment() != null) {
        for (FIN_PaymentDetail pd : transaction.getFinPayment().getFINPaymentDetailList()) {
          for (FIN_PaymentScheduleDetail psd : pd.getFINPaymentScheduleDetailList()) {
            invoicePaidold = psd.isInvoicePaid();
            if (invoicePaidold) {
              if (oldStatus.equals(FIN_Utility.invoicePaymentStatus(transaction.getFinPayment()))) {
                FIN_Utility.restorePaidAmounts(pd, psd);
              }
            }
          }
        }
      }
    }
  }
}