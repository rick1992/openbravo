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
 * All portions are Copyright (C) 2010-2014 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.advpaymentmngt.process;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.ServletException;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBDao;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.ad.system.Language;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.currency.ConversionRate;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.OrganizationInformation;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.financialmgmt.accounting.Costcenter;
import org.openbravo.model.financialmgmt.accounting.UserDimension1;
import org.openbravo.model.financialmgmt.accounting.UserDimension2;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentPropDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentProposal;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedInvV;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.financialmgmt.payment.FinAccPaymentMethod;
import org.openbravo.model.marketing.Campaign;
import org.openbravo.model.materialmgmt.cost.ABCActivity;
import org.openbravo.model.project.Project;
import org.openbravo.model.sales.SalesRegion;
import org.openbravo.scheduling.ProcessBundle;

import pe.com.unifiedgo.accounting.data.SCOEEFFCashflow;
import pe.com.unifiedgo.accounting.data.SCOInternalDoc;
import pe.com.unifiedgo.accounting.data.SCOPrepayment;
import pe.com.unifiedgo.accounting.data.ScoRendicioncuentas;

public class FIN_AddPayment {
  private static AdvPaymentMngtDao dao;

  /**
   * Saves the payment and the payment details based on the given Payment Schedule Details. If no
   * FIN_Payment is given it creates a new one.
   * 
   * If the Payment Scheduled Detail is not completely paid and the difference is not written a new
   * Payment Schedule Detail is created with the difference.
   * 
   * If a Refund Amount is given an extra Payment Detail will be created with it.
   * 
   * @param _payment
   *          FIN_Payment where new payment details will be saved.
   * @param isReceipt
   *          boolean to define if the Payment is a Receipt Payment (true) or a Payable Payment
   *          (false). Used when no FIN_Payment is given.
   * @param docType
   *          DocumentType of the Payment. Used when no FIN_Payment is given.
   * @param strPaymentDocumentNo
   *          String with the Document Number of the new payment. Used when no FIN_Payment is given.
   * @param businessPartner
   *          BusinessPartner of the new Payment. Used when no FIN_Payment is given.
   * @param paymentMethod
   *          FIN_PaymentMethod of the new Payment. Used when no FIN_Payment is given.
   * @param finAccount
   *          FIN_FinancialAccount of the new Payment. Used when no FIN_Payment is given.
   * @param strPaymentAmount
   *          String with the Payment Amount of the new Payment. Used when no FIN_Payment is given.
   * @param paymentDate
   *          Date when the Payment is done. Used when no FIN_Payment is given.
   * @param organization
   *          Organization of the new Payment. Used when no FIN_Payment is given.
   * @param selectedPaymentScheduleDetails
   *          List of FIN_PaymentScheduleDetail to be included in the Payment. If one of the items
   *          is contained in other payment the method will throw an exception. Prevent
   *          invoice/order to be paid several times.
   * @param selectedPaymentScheduleDetailsAmounts
   *          HashMap with the Amount to be paid for each Scheduled Payment Detail.
   * @param isWriteoff
   *          Boolean to write off the difference when the payment amount is lower than the Payment
   *          Scheduled PAyment Detail amount.
   * @param isRefund
   *          Not used.
   * @param paymentCurrency
   *          The currency that the payment is being made in. Will default to financial account
   *          currency if not specified
   * @param finTxnConvertRate
   *          Exchange rate to convert between payment currency and financial account currency for
   *          this payment. Defaults to 1.0 if not supplied
   * @param finTxnAmountc
   *          Amount of payment in currency of financial account
   * @return The FIN_Payment OBObject containing all the Payment Details.
   */
  public static FIN_Payment savePayment(VariablesSecureApp vars, FIN_Payment _payment,
      boolean isReceipt, DocumentType docType, String strPaymentDocumentNo,
      BusinessPartner businessPartner, FIN_PaymentMethod paymentMethod,
      FIN_FinancialAccount finAccount, String strPaymentAmount, Date paymentDate,
      Organization organization, String referenceNo,
      List<FIN_PaymentScheduleDetail> selectedPaymentScheduleDetails,
      HashMap<String, BigDecimal> selectedPaymentScheduleDetailsAmounts, boolean isWriteoff,
      boolean isOverpayment, Currency paymentCurrency, BigDecimal finTxnConvertRate,
      BigDecimal finTxnAmount) {

    return savePayment(vars, _payment, isReceipt, docType, strPaymentDocumentNo, businessPartner,
        paymentMethod, finAccount, strPaymentAmount, paymentDate, organization, referenceNo,
        selectedPaymentScheduleDetails, selectedPaymentScheduleDetailsAmounts, isWriteoff,
        isOverpayment, paymentCurrency, finTxnConvertRate, finTxnAmount, false);
  }

  public static FIN_Payment savePayment(VariablesSecureApp vars, FIN_Payment _payment,
      boolean isReceipt, DocumentType docType, String strPaymentDocumentNo,
      BusinessPartner businessPartner, FIN_PaymentMethod paymentMethod,
      FIN_FinancialAccount finAccount, String strPaymentAmount, Date paymentDate,
      Organization organization, String referenceNo,
      List<FIN_PaymentScheduleDetail> selectedPaymentScheduleDetails,
      HashMap<String, BigDecimal> selectedPaymentScheduleDetailsAmounts, boolean isWriteoff,
      boolean isOverpayment, Currency paymentCurrency, BigDecimal finTxnConvertRate,
      BigDecimal finTxnAmount, boolean isDetraction) {

    return savePayment(vars, _payment, isReceipt, docType, strPaymentDocumentNo, businessPartner,
        paymentMethod, finAccount, strPaymentAmount, paymentDate, organization, referenceNo,
        selectedPaymentScheduleDetails, null, selectedPaymentScheduleDetailsAmounts, isWriteoff,
        isOverpayment, paymentCurrency, finTxnConvertRate, finTxnAmount, false);
  }

  public static FIN_Payment savePayment(VariablesSecureApp vars, FIN_Payment _payment,
      boolean isReceipt, DocumentType docType, String strPaymentDocumentNo,
      BusinessPartner businessPartner, FIN_PaymentMethod paymentMethod,
      FIN_FinancialAccount finAccount, String strPaymentAmount, Date paymentDate,
      Organization organization, String referenceNo,
      List<FIN_PaymentScheduleDetail> selectedPaymentScheduleDetails, List<String> bPartnerGirados,
      HashMap<String, BigDecimal> selectedPaymentScheduleDetailsAmounts, boolean isWriteoff,
      boolean isOverpayment, Currency paymentCurrency, BigDecimal finTxnConvertRate,
      BigDecimal finTxnAmount, boolean isDetraction) {

    dao = new AdvPaymentMngtDao();

    BigDecimal assignedAmount = BigDecimal.ZERO;
    final FIN_Payment payment;
    if (_payment != null) {
      payment = _payment;
    } else {
      payment = dao.getNewPayment(isReceipt, organization, docType, strPaymentDocumentNo,
          businessPartner, paymentMethod, finAccount, strPaymentAmount, paymentDate, referenceNo,
          paymentCurrency, finTxnConvertRate, finTxnAmount, null);
      try {
        OBDal.getInstance().flush();
      } catch (Exception e) {
        throw new OBException(FIN_Utility.getExceptionMessage(e));
      }
    }

    for (FIN_PaymentDetail paymentDetail : payment.getFINPaymentDetailList()) {
      assignedAmount = assignedAmount.add(paymentDetail.getScoPaymentamount());
    }
    // FIXME: added to access the FIN_PaymentSchedule and
    // FIN_PaymentScheduleDetail tables to be
    // removed when new security implementation is done
    OBContext.setAdminMode();
    try {

      BigDecimal docAmount = new BigDecimal(0);
      BigDecimal finaccAmount = new BigDecimal(0);

      int numSchedule = -1;
      for (FIN_PaymentScheduleDetail paymentScheduleDetail : selectedPaymentScheduleDetails) {

        numSchedule++;
        if (paymentScheduleDetail.getScoPrepayment() != null
            || paymentScheduleDetail.getSCOEntregaARendir() != null
            || paymentScheduleDetail.getScoPayoutprepayment() != null
            || paymentScheduleDetail.getScoPayoutrendcuentas() != null)
          continue;

        boolean docissotrx = isReceipt;
        if (paymentScheduleDetail.getInvoicePaymentSchedule() != null) {
          docissotrx = paymentScheduleDetail.getInvoicePaymentSchedule().getInvoice()
              .isSalesTransaction();
        }

        System.out.println("************" + paymentScheduleDetail.getId() + " "
            + paymentScheduleDetail.getInvoicePaymentSchedule().getId());

        // Payment Schedule Detail already linked to a payment detail.
        String description = paymentScheduleDetail.getSCODescription();
        BigDecimal detret = paymentScheduleDetail.getScoDetretamt();

        OBDal.getInstance().refresh(paymentScheduleDetail);
        paymentScheduleDetail.setSCODescription(description);
        paymentScheduleDetail.setScoDetretamt(detret);

        if (paymentScheduleDetail.getPaymentDetails() != null && !paymentScheduleDetail
            .getPaymentDetails().getFinPayment().getId().equals(payment.getId())) {
          // If payment schedule detail belongs to a different payment
          throw new OBException(String.format(FIN_Utility.messageBD("APRM_PsdInSeveralPayments"),
              paymentScheduleDetail.getIdentifier()));
        } else if (paymentScheduleDetail.getPaymentDetails() != null && paymentScheduleDetail
            .getPaymentDetails().getFinPayment().getId().equals(payment.getId())) {
          // Detail for this payment already exists. Payment being
          // edited
          BigDecimal paymentDetailAmount = selectedPaymentScheduleDetailsAmounts
              .get(paymentScheduleDetail.getId());

          Currency curr = payment.getAccount().getCurrency();
          BigDecimal tc = BigDecimal.ONE;
          if (paymentScheduleDetail.getInvoicePaymentSchedule() != null)
            curr = paymentScheduleDetail.getInvoicePaymentSchedule().getCurrency();
          if (paymentScheduleDetail.getOrderPaymentSchedule() != null)
            curr = paymentScheduleDetail.getOrderPaymentSchedule().getCurrency();

          BigDecimal invPayment = paymentDetailAmount;
          // OJO REDONDEO FIX-ME
          if (!curr.getId().equals(payment.getAccount().getCurrency().getId())) {

            if (!isDetraction || paymentScheduleDetail.getInvoicePaymentSchedule() == null) {
              invPayment = paymentDetailAmount
                  .multiply(payment.getFinancialTransactionConvertRate())
                  .setScale(2, RoundingMode.HALF_UP);
              tc = payment.getFinancialTransactionConvertRate();
              docAmount = docAmount.add(docissotrx == isReceipt ? invPayment : invPayment.negate());
            } else {
              // T/C venta porque detraccion es solo para pagos
              final ConversionRate conversionRate = FIN_Utility.getConversionRate(curr,
                  payment.getAccount().getCurrency(), paymentScheduleDetail
                      .getInvoicePaymentSchedule().getInvoice().getScoNewdateinvoiced(),
                  payment.getOrganization());
              BigDecimal exchangeRate = conversionRate.getDivideRateBy().setScale(12,
                  RoundingMode.HALF_UP);

              invPayment = paymentDetailAmount
                  .multiply(payment.getFinancialTransactionConvertRate())
                  .setScale(12, RoundingMode.HALF_UP);
              docAmount = docAmount.add(docissotrx == isReceipt ? invPayment : invPayment.negate());

              BigDecimal oldPaymentDetailAmount = paymentDetailAmount;
              paymentDetailAmount = invPayment.divide(exchangeRate, 2, RoundingMode.HALF_UP)
                  .setScale(0, RoundingMode.HALF_UP);
              tc = exchangeRate;

              BigDecimal oldInvPayment = invPayment;
              // Ahora cambiar la otra moneda
              invPayment = paymentDetailAmount.multiply(exchangeRate).setScale(2,
                  RoundingMode.HALF_UP);

              docAmount = docAmount.add(docissotrx == isReceipt ? invPayment : invPayment.negate())
                  .subtract(docissotrx == isReceipt ? oldInvPayment : oldInvPayment.negate());
              payment.setAmount(payment.getAmount()
                  .subtract(docissotrx == isReceipt ? oldPaymentDetailAmount
                      : oldPaymentDetailAmount.negate())
                  .add(docissotrx == isReceipt ? paymentDetailAmount
                      : paymentDetailAmount.negate()));
              payment.setFinancialTransactionAmount(payment.getAmount());
            }

          } else {
            finaccAmount = finaccAmount
                .add(docissotrx == isReceipt ? invPayment : invPayment.negate());
          }

          System.out.println("invPayment: " + invPayment + " " + paymentScheduleDetail.getAmount());
          BigDecimal amountDifference = paymentScheduleDetail.getAmount().subtract(invPayment);

          // Ajuste por redondeo, luego en POST se ajusta
          if (amountDifference.abs().compareTo(new BigDecimal(0.03)) == -1
              && !curr.getId().equals(payment.getAccount().getCurrency().getId())) {
            amountDifference = BigDecimal.ZERO;
            invPayment = paymentScheduleDetail.getAmount();
          }

          // If amount or the scoPaymentAmount has changed payment schedule details needs to
          // be updated. Aggregate amount
          // coming from unpaid schedule detail which remains unpaid

          BigDecimal pdscopaymentamount = paymentScheduleDetail.getPaymentDetails()
              .getScoPaymentamount();
          if (docissotrx != isReceipt) {
            pdscopaymentamount = pdscopaymentamount.negate();
          }

          if (paymentScheduleDetail.getAmount().compareTo(invPayment) != 0
              || pdscopaymentamount.compareTo(paymentDetailAmount) != 0) {

            // update Amounts as they have changed
            assignedAmount = assignedAmount.subtract(
                docissotrx == isReceipt ? pdscopaymentamount : pdscopaymentamount.negate());
            assignedAmount = assignedAmount
                .add(docissotrx == isReceipt ? paymentDetailAmount : paymentDetailAmount.negate());
            // update detail with the new value
            List<FIN_PaymentScheduleDetail> outStandingPSDs = getOutstandingPSDs(
                paymentScheduleDetail);
            BigDecimal difference = paymentScheduleDetail.getAmount().subtract(invPayment);
            // Assume doubtful debt is always positive
            BigDecimal doubtFulDebtAmount = BigDecimal.ZERO;

            if (outStandingPSDs.size() == 0) {
              doubtFulDebtAmount = getDoubtFulDebtAmount(
                  paymentScheduleDetail.getAmount().add(paymentScheduleDetail.getWriteoffAmount()),
                  invPayment, paymentScheduleDetail.getDoubtfulDebtAmount());
              if (!isWriteoff) {
                // No outstanding PSD exists so one needs to be
                // created for the difference
                FIN_PaymentScheduleDetail outstandingPSD = (FIN_PaymentScheduleDetail) DalUtil
                    .copy(paymentScheduleDetail, false);
                outstandingPSD.setAmount(difference);
                outstandingPSD.setDoubtfulDebtAmount(
                    paymentScheduleDetail.getDoubtfulDebtAmount().subtract(doubtFulDebtAmount));
                outstandingPSD.setPaymentDetails(null);
                outstandingPSD.setSCODescription(paymentScheduleDetail.getSCODescription());
                OBDal.getInstance().save(outstandingPSD);

              } else {
                // If it is write Off then incorporate all
                // doubtful debt
                doubtFulDebtAmount = paymentScheduleDetail.getDoubtfulDebtAmount();
                // Set difference as writeoff
                paymentScheduleDetail.setWriteoffAmount(difference);
                paymentScheduleDetail.setDoubtfulDebtAmount(doubtFulDebtAmount);
                OBDal.getInstance().save(paymentScheduleDetail);
                paymentScheduleDetail.getPaymentDetails()
                    .setWriteoffAmount(docissotrx == isReceipt ? difference : difference.negate());
                paymentScheduleDetail.getPaymentDetails()
                    .setScoDescription(paymentScheduleDetail.getSCODescription());
                OBDal.getInstance().save(paymentScheduleDetail.getPaymentDetails());

                payment.setWriteoffAmount(docissotrx == isReceipt
                    ? payment.getWriteoffAmount()
                        .add(paymentScheduleDetail.getWriteoffAmount().divide(tc, 2,
                            RoundingMode.HALF_UP))
                    : payment.getWriteoffAmount().add(paymentScheduleDetail.getWriteoffAmount()
                        .divide(tc, 2, RoundingMode.HALF_UP)).negate());

              }
            } else {
              if (!isWriteoff) {
                // First make sure outstanding amount is not
                // equal zero
                if (outStandingPSDs.get(0).getAmount().add(difference).signum() == 0) {
                  doubtFulDebtAmount = paymentScheduleDetail.getDoubtfulDebtAmount()
                      .add(outStandingPSDs.get(0).getDoubtfulDebtAmount());
                  OBDal.getInstance().remove(outStandingPSDs.get(0));
                } else {
                  // update existing PD with difference
                  doubtFulDebtAmount = getDoubtFulDebtAmount(
                      paymentScheduleDetail.getAmount().add(outStandingPSDs.get(0).getAmount()),
                      invPayment, paymentScheduleDetail.getDoubtfulDebtAmount()
                          .add(outStandingPSDs.get(0).getDoubtfulDebtAmount()));
                  outStandingPSDs.get(0)
                      .setAmount(outStandingPSDs.get(0).getAmount().add(difference));
                  outStandingPSDs.get(0).setDoubtfulDebtAmount(
                      outStandingPSDs.get(0).getDoubtfulDebtAmount().add(paymentScheduleDetail
                          .getDoubtfulDebtAmount().subtract(doubtFulDebtAmount)));
                  outStandingPSDs.get(0)
                      .setSCODescription(paymentScheduleDetail.getSCODescription());
                  OBDal.getInstance().save(outStandingPSDs.get(0));

                }
              } else {
                paymentScheduleDetail
                    .setWriteoffAmount(difference.add(outStandingPSDs.get(0).getAmount()));
                doubtFulDebtAmount = outStandingPSDs.get(0).getDoubtfulDebtAmount()
                    .add(paymentScheduleDetail.getDoubtfulDebtAmount());
                OBDal.getInstance().save(paymentScheduleDetail);
                paymentScheduleDetail.getPaymentDetails().setWriteoffAmount(
                    docissotrx == isReceipt ? difference.add(outStandingPSDs.get(0).getAmount())
                        : difference.add(outStandingPSDs.get(0).getAmount()).negate());
                paymentScheduleDetail.getPaymentDetails()
                    .setScoDescription(paymentScheduleDetail.getSCODescription());

                OBDal.getInstance().save(paymentScheduleDetail.getPaymentDetails());

                OBDal.getInstance().remove(outStandingPSDs.get(0));

                payment.setWriteoffAmount(docissotrx == isReceipt
                    ? payment.getWriteoffAmount()
                        .add(paymentScheduleDetail.getWriteoffAmount().divide(tc, 2,
                            RoundingMode.HALF_UP))
                    : payment.getWriteoffAmount().add(paymentScheduleDetail.getWriteoffAmount()
                        .divide(tc, 2, RoundingMode.HALF_UP)).negate());

              }
            }
            paymentScheduleDetail.setAmount(invPayment);
            paymentScheduleDetail.setDoubtfulDebtAmount(doubtFulDebtAmount);
            OBDal.getInstance().save(paymentScheduleDetail);
            paymentScheduleDetail.getPaymentDetails()
                .setAmount(docissotrx == isReceipt ? invPayment : invPayment.negate());
            System.out.println("222 paymentDetailAmount : " + paymentDetailAmount);
            paymentScheduleDetail.getPaymentDetails().setScoPaymentamount(
                docissotrx == isReceipt ? paymentDetailAmount : paymentDetailAmount.negate());
            paymentScheduleDetail.getPaymentDetails()
                .setScoDescription(paymentScheduleDetail.getSCODescription());
            OBDal.getInstance().save(paymentScheduleDetail.getPaymentDetails());

          } else if (isWriteoff) {
            List<FIN_PaymentScheduleDetail> outStandingPSDs = getOutstandingPSDs(
                paymentScheduleDetail);
            if (outStandingPSDs.size() > 0) {
              paymentScheduleDetail.setWriteoffAmount(outStandingPSDs.get(0).getAmount());
              paymentScheduleDetail.setDoubtfulDebtAmount(outStandingPSDs.get(0)
                  .getDoubtfulDebtAmount().add(paymentScheduleDetail.getDoubtfulDebtAmount()));
              OBDal.getInstance().save(paymentScheduleDetail);
              paymentScheduleDetail.getPaymentDetails()
                  .setWriteoffAmount(docissotrx == isReceipt ? outStandingPSDs.get(0).getAmount()
                      : outStandingPSDs.get(0).getAmount().negate());
              paymentScheduleDetail.getPaymentDetails()
                  .setScoDescription(paymentScheduleDetail.getSCODescription());
              OBDal.getInstance().save(paymentScheduleDetail.getPaymentDetails());
              OBDal.getInstance().remove(outStandingPSDs.get(0));

              payment.setWriteoffAmount(docissotrx == isReceipt
                  ? payment.getWriteoffAmount().add(
                      paymentScheduleDetail.getWriteoffAmount().divide(tc, 2, RoundingMode.HALF_UP))
                  : payment.getWriteoffAmount().add(
                      paymentScheduleDetail.getWriteoffAmount().divide(tc, 2, RoundingMode.HALF_UP))
                      .negate());

            }

          } else {// buscar cambiar description

            paymentScheduleDetail.getPaymentDetails()
                .setScoDescription(paymentScheduleDetail.getSCODescription());
            OBDal.getInstance().save(paymentScheduleDetail.getPaymentDetails());
          }

        } else {

          BigDecimal paymentDetailAmount = selectedPaymentScheduleDetailsAmounts
              .get(paymentScheduleDetail.getId());
          // If detail to be added is zero amount, skip it
          if (paymentDetailAmount.signum() == 0 && !isWriteoff) {
            continue;
          }

          Currency curr = payment.getAccount().getCurrency();
          BigDecimal tc = BigDecimal.ONE;
          if (paymentScheduleDetail.getInvoicePaymentSchedule() != null)
            curr = paymentScheduleDetail.getInvoicePaymentSchedule().getCurrency();
          if (paymentScheduleDetail.getOrderPaymentSchedule() != null)
            curr = paymentScheduleDetail.getOrderPaymentSchedule().getCurrency();

          BigDecimal invPayment = paymentDetailAmount;
          // OJO REDONDEO FIX-ME

          if (!curr.getId().equals(payment.getAccount().getCurrency().getId())) {

            if (!isDetraction || paymentScheduleDetail.getInvoicePaymentSchedule() == null) {
              invPayment = paymentDetailAmount
                  .multiply(payment.getFinancialTransactionConvertRate())
                  .setScale(2, RoundingMode.HALF_UP);
              tc = payment.getFinancialTransactionConvertRate();
              docAmount = docAmount.add(docissotrx == isReceipt ? invPayment : invPayment.negate());
            } else {
              // T/C venta porque detraccion es solo para pagos

              final ConversionRate conversionRate = FIN_Utility.getConversionRate(curr,
                  payment.getAccount().getCurrency(), paymentScheduleDetail
                      .getInvoicePaymentSchedule().getInvoice().getScoNewdateinvoiced(),
                  payment.getOrganization());
              BigDecimal exchangeRate = conversionRate.getDivideRateBy().setScale(12,
                  RoundingMode.HALF_UP);

              invPayment = paymentDetailAmount
                  .multiply(payment.getFinancialTransactionConvertRate())
                  .setScale(2, RoundingMode.HALF_UP);
              docAmount = docAmount.add(docissotrx == isReceipt ? invPayment : invPayment.negate());

              BigDecimal oldPaymentDetailAmount = paymentDetailAmount;
              paymentDetailAmount = invPayment.divide(exchangeRate, 2, RoundingMode.HALF_UP)
                  .setScale(0, RoundingMode.HALF_UP);
              tc = exchangeRate;

              BigDecimal oldInvPayment = invPayment;
              // Ahora cambiar la otra moneda
              invPayment = paymentDetailAmount.multiply(exchangeRate).setScale(2,
                  RoundingMode.HALF_UP);

              docAmount = docAmount.add(docissotrx == isReceipt ? invPayment : invPayment.negate())
                  .subtract(docissotrx == isReceipt ? oldInvPayment : oldInvPayment.negate());
              payment.setAmount(payment.getAmount()
                  .subtract(docissotrx == isReceipt ? oldPaymentDetailAmount
                      : oldPaymentDetailAmount.negate())
                  .add(docissotrx == isReceipt ? paymentDetailAmount
                      : paymentDetailAmount.negate()));
              payment.setFinancialTransactionAmount(payment.getAmount());
            }

          } else {
            finaccAmount = finaccAmount
                .add(docissotrx == isReceipt ? invPayment : invPayment.negate());

          }
          System.out.println("invPayment: " + invPayment);
          BigDecimal amountDifference = paymentScheduleDetail.getAmount().subtract(invPayment);

          // Ajuste por redondeo, luego en POST se ajusta
          if (amountDifference.abs().compareTo(new BigDecimal(0.03)) == -1
              && !curr.getId().equals(payment.getAccount().getCurrency().getId())) {
            amountDifference = BigDecimal.ZERO;
            invPayment = paymentScheduleDetail.getAmount();
          }

          System.out.println("paymentScheduleDetail.getAmount() amountDifference: "
              + paymentScheduleDetail.getAmount() + " " + amountDifference);

          // Debt Payment
          BigDecimal doubtfulDebtAmount = getDoubtFulDebtAmount(
              paymentScheduleDetail.getAmount().add(paymentScheduleDetail.getWriteoffAmount()),
              invPayment, paymentScheduleDetail.getDoubtfulDebtAmount());
          if (amountDifference.compareTo(BigDecimal.ZERO) != 0) {
            if (!isWriteoff) {
              dao.duplicateScheduleDetail(paymentScheduleDetail, amountDifference,
                  paymentScheduleDetail.getDoubtfulDebtAmount().subtract(doubtfulDebtAmount));
              amountDifference = BigDecimal.ZERO;
            } else {
              doubtfulDebtAmount = paymentScheduleDetail.getDoubtfulDebtAmount();
              paymentScheduleDetail.setWriteoffAmount(amountDifference);
            }
            paymentScheduleDetail.setAmount(invPayment);
            paymentScheduleDetail.setDoubtfulDebtAmount(doubtfulDebtAmount);
          }

          System.out.println("Invoice:" + paymentDetailAmount);
          assignedAmount = assignedAmount
              .add(docissotrx == isReceipt ? paymentDetailAmount : paymentDetailAmount.negate());
          System.out.println("assignedAmount: " + assignedAmount);

          if (docissotrx == isReceipt) {
            if (bPartnerGirados != null)
              dao.getNewPaymentDetail(payment, paymentScheduleDetail, invPayment, amountDifference,
                  false, null, null, null, tc, curr, paymentDetailAmount,
                  bPartnerGirados.get(numSchedule));
            else
              dao.getNewPaymentDetail(payment, paymentScheduleDetail, invPayment, amountDifference,
                  false, null, tc, curr, paymentDetailAmount);
          } else {
            if (bPartnerGirados != null)
              dao.getNewPaymentDetail(payment, paymentScheduleDetail, invPayment.negate(),
                  amountDifference.negate(), false, null, null, null, tc, curr,
                  paymentDetailAmount.negate(), bPartnerGirados.get(numSchedule));
            else
              dao.getNewPaymentDetail(payment, paymentScheduleDetail, invPayment.negate(),
                  amountDifference.negate(), false, null, tc, curr, paymentDetailAmount.negate());
          }
        }
      }

      payment.setScoDocAmount(docAmount);
      payment.setScoFinaccAmount(finaccAmount);

      System.out.println("XXXX: " + payment.getUsedCredit());

      // ahora pago apertura de anticipos
      for (FIN_PaymentScheduleDetail paymentScheduleDetail : selectedPaymentScheduleDetails) {

        if (paymentScheduleDetail.getScoPayoutprepayment() == null)
          continue;

        System.out.println("paymentScheduleDetail:" + paymentScheduleDetail.getId());
        System.out.println("paymentScheduleDetail2 :" + vars
            .getStringParameter("inpInvoiceDetRetAmount" + paymentScheduleDetail.getId(), "0.00"));
        BigDecimal recordAmount = new BigDecimal(
            vars.getNumericParameter("inpPaymentAmount" + paymentScheduleDetail.getId(), "0.00"));
        BigDecimal recordRet = new BigDecimal(vars
            .getNumericParameter("inpInvoiceDetRetAmount" + paymentScheduleDetail.getId(), "0.00"));

        boolean docissotrx = paymentScheduleDetail.getScoPayoutprepayment().isSalesTransaction();
        if (docissotrx != isReceipt) {
          recordAmount = recordAmount.negate();
          recordRet = recordRet.negate();
        }

        Currency curr = paymentScheduleDetail.getScoPayoutprepayment().getCurrency();
        BigDecimal tc = BigDecimal.ONE;

        BigDecimal invPayment = recordAmount;// .add(recordRet);
        BigDecimal aSustraerRet = recordRet;
        SCOPrepayment prep = paymentScheduleDetail.getScoPayoutprepayment();

        if (prep.getPayment() != null) {
          throw new OBException("@SCO_PrepaymentAlreadyOpen@");
        }
        // OJO FIX-ME redondeo testear
        if (!curr.getId().equals(payment.getAccount().getCurrency().getId())) {
          invPayment = invPayment.multiply(payment.getFinancialTransactionConvertRate()).setScale(2,
              RoundingMode.HALF_UP);
          aSustraerRet = aSustraerRet.multiply(payment.getFinancialTransactionConvertRate())
              .setScale(2, RoundingMode.HALF_UP);
          tc = payment.getFinancialTransactionConvertRate();
        }

        // BigDecimal retencion =
        // prep.getWithholdingamount().multiply(invPayment.divide(prep.getAmount().subtract(prep.getWithholdingamount()),
        // 16, RoundingMode.HALF_UP)).setScale(2, RoundingMode.HALF_UP);

        prep.setTotalPaid(prep.getTotalPaid().add(invPayment.add(aSustraerRet)));

        if (prep.getAmount().compareTo(new BigDecimal(0)) > 0) {
          if (prep.getTotalPaid().compareTo(prep.getAmount()) > 0) {
            throw new OBException("@SCO_PrepaymentAlreadyPaid@");

          } else if (prep.getTotalPaid().compareTo(prep.getAmount()) == 0) {
            prep.setPaymentComplete(true);
          } else {
            prep.setPaymentComplete(false);
          }
        } else {
          if (prep.getTotalPaid().compareTo(prep.getAmount()) < 0) {
            throw new OBException("@SCO_PrepaymentAlreadyPaid@");

          } else if (prep.getTotalPaid().compareTo(prep.getAmount()) == 0) {
            prep.setPaymentComplete(true);
          } else {
            prep.setPaymentComplete(false);
          }
        }
        OBDal.getInstance().save(prep);

        System.out.println("Pago apertura de Anticipo:" + invPayment);
        assignedAmount = assignedAmount
            .add(docissotrx == isReceipt ? recordAmount : recordAmount.negate());

        FIN_PaymentDetail pd = FIN_AddPayment.saveGLItemRetPD(payment,
            (docissotrx == isReceipt ? invPayment : invPayment.negate()), prep.getPaymentglitem(),
            prep.getBusinessPartner(), null, prep.getProject(), null, null, null, null, null, null,
            prep.getDescription(), (docissotrx == isReceipt ? recordAmount : recordAmount.negate()),
            tc, curr, null, null, prep.getId(), null, null, null);

        prep.setPayment(payment);
        // prep.setUsedamount(new BigDecimal(0));
        OBDal.getInstance().save(prep);

        pd.setScoDescription(prep.getDescription());
        prep.setPaymentDetails(pd);
        OBDal.getInstance().save(pd);
        OBDal.getInstance().save(prep);
      }

      // ahora aplicacion de anticipos
      for (FIN_PaymentScheduleDetail paymentScheduleDetail : selectedPaymentScheduleDetails) {

        if (paymentScheduleDetail.getScoPrepayment() == null)
          continue;

        System.out.println("paymentScheduleDetail:" + paymentScheduleDetail.getId());
        System.out.println("paymentScheduleDetail2 :" + vars
            .getStringParameter("inpInvoiceDetRetAmount" + paymentScheduleDetail.getId(), "0.00"));
        BigDecimal recordAmount = new BigDecimal(
            vars.getNumericParameter("inpPaymentAmount" + paymentScheduleDetail.getId(), "0.00"));
        BigDecimal recordRet = new BigDecimal(vars
            .getNumericParameter("inpInvoiceDetRetAmount" + paymentScheduleDetail.getId(), "0.00"));

        Currency curr = paymentScheduleDetail.getScoPrepayment().getCurrency();
        BigDecimal tc = BigDecimal.ONE;

        boolean docissotrx = paymentScheduleDetail.getScoPrepayment().isSalesTransaction();
        if (docissotrx != isReceipt) {
          recordAmount = recordAmount.negate();
          recordRet = recordRet.negate();
        }

        BigDecimal invPayment = recordAmount;// .add(recordRet);
        BigDecimal aSustraerRet = recordRet;
        SCOPrepayment prep = paymentScheduleDetail.getScoPrepayment();

        // OJO FIX-ME redondeo testear
        if (!curr.getId().equals(payment.getAccount().getCurrency().getId())) {
          invPayment = invPayment.multiply(payment.getFinancialTransactionConvertRate()).setScale(2,
              RoundingMode.HALF_UP);
          aSustraerRet = aSustraerRet.multiply(payment.getFinancialTransactionConvertRate())
              .setScale(2, RoundingMode.HALF_UP);
          tc = payment.getFinancialTransactionConvertRate();
        }

        // BigDecimal retencion =
        // prep.getWithholdingamount().multiply(invPayment.divide(prep.getAmount().subtract(prep.getWithholdingamount()),
        // 16, RoundingMode.HALF_UP)).setScale(2, RoundingMode.HALF_UP);

        prep.setUsedamount(prep.getUsedamount().add(invPayment.add(aSustraerRet).negate()));
        OBDal.getInstance().save(prep);

        if (prep.getAmount().compareTo(new BigDecimal(0)) > 0) {
          if (prep.getUsedamount().compareTo(prep.getAmount()) > 0) {
            throw new OBException("@SCO_PrepaymentAlreadyUsed@");
          }
        } else {
          if (prep.getUsedamount().compareTo(prep.getAmount()) < 0) {
            throw new OBException("@SCO_PrepaymentAlreadyUsed@");
          }
        }
        System.out.println("Anticipo:" + invPayment);
        assignedAmount = assignedAmount
            .add(docissotrx == isReceipt ? recordAmount : recordAmount.negate());

        FIN_PaymentDetail detail = null;
        if (bPartnerGirados != null)
          detail = dao.getNewPaymentDetail(payment, paymentScheduleDetail,
              (docissotrx == isReceipt ? invPayment : invPayment.negate()), new BigDecimal(0),
              false, null, paymentScheduleDetail.getScoPrepayment(),
              paymentScheduleDetail.getSCOEntregaARendir(), tc, curr,
              (docissotrx == isReceipt ? recordAmount : recordAmount.negate()),
              bPartnerGirados.get(numSchedule));
        else
          detail = dao.getNewPaymentDetail(payment, paymentScheduleDetail,
              (docissotrx == isReceipt ? invPayment : invPayment.negate()), new BigDecimal(0),
              false, null, paymentScheduleDetail.getScoPrepayment(),
              paymentScheduleDetail.getSCOEntregaARendir(), tc, curr,
              (docissotrx == isReceipt ? recordAmount : recordAmount.negate()));

        if (recordRet.compareTo(BigDecimal.ZERO) != 0) {
          detail
              .setScoPerceptionamt(docissotrx == isReceipt ? aSustraerRet : aSustraerRet.negate());
          OBDal.getInstance().save(detail);
          OBDal.getInstance().flush();
        }
      }

      // ahora pago apertura de entregas a rendir
      for (FIN_PaymentScheduleDetail paymentScheduleDetail : selectedPaymentScheduleDetails) {

        if (paymentScheduleDetail.getScoPayoutrendcuentas() == null)
          continue;

        BigDecimal recordAmount = new BigDecimal(
            vars.getNumericParameter("inpPaymentAmount" + paymentScheduleDetail.getId(), ""));

        Currency curr = paymentScheduleDetail.getScoPayoutrendcuentas().getCurrency();
        BigDecimal tc = BigDecimal.ONE;

        BigDecimal invPayment = recordAmount;
        ScoRendicioncuentas rend = paymentScheduleDetail.getScoPayoutrendcuentas();

        if (rend.getFINPaymentOpen() != null) {
          throw new OBException("@SCO_RendicionCuentasAlreadyOpen@");
        }
        if (rend.getOrganization().getScoRendcuentasGlitem() == null) {
          throw new OBException("@SCO_InternalError@");
        }
        // OJO FIX-ME redondeo testear
        if (!curr.getId().equals(payment.getAccount().getCurrency().getId())) {
          invPayment = recordAmount.multiply(payment.getFinancialTransactionConvertRate())
              .setScale(2, RoundingMode.HALF_UP);
          tc = payment.getFinancialTransactionConvertRate();
        }

        rend.setTotalPaid(rend.getTotalPaid().add(invPayment));

        if (rend.getTotalPaid().compareTo(rend.getAmount()) >= 0) {
          rend.setPaymentComplete(true);
        } else {
          rend.setPaymentComplete(false);
        }
        OBDal.getInstance().save(rend);

        System.out.println("Pago apertura de Entrega a Rendir" + invPayment);
        assignedAmount = assignedAmount.add(recordAmount);

        FIN_PaymentDetail pd = FIN_AddPayment.saveGLItemRetPD(payment, invPayment,
            rend.getOrganization().getScoRendcuentasGlitem(), rend.getBusinessPartner(), null,
            rend.getProject(), null, null, null, null, null, null, rend.getDescription(),
            recordAmount, tc, curr, null, null, null, rend.getId(), null, null);

        rend.setFINPaymentOpen(payment);
        rend.setRefund(rend.getAmount());
        OBDal.getInstance().save(rend);

        pd.setScoDescription(rend.getDescription());
        rend.setPaymentDetails(pd);
        OBDal.getInstance().save(pd);
        OBDal.getInstance().save(rend);
      }

      // ahora aplicacion entregas a rendir
      for (FIN_PaymentScheduleDetail paymentScheduleDetail : selectedPaymentScheduleDetails) {

        if (paymentScheduleDetail.getSCOEntregaARendir() == null)
          continue;

        BigDecimal recordAmount = new BigDecimal(
            vars.getNumericParameter("inpPaymentAmount" + paymentScheduleDetail.getId(), ""));

        Currency curr = paymentScheduleDetail.getSCOEntregaARendir().getCurrency();
        BigDecimal tc = BigDecimal.ONE;

        BigDecimal invPayment = recordAmount;

        // OJO FIX-ME redondeo testear
        if (!curr.getId().equals(payment.getAccount().getCurrency().getId())) {
          invPayment = recordAmount.multiply(payment.getFinancialTransactionConvertRate())
              .setScale(2, RoundingMode.HALF_UP);
          tc = payment.getFinancialTransactionConvertRate();
        }

        paymentScheduleDetail.getSCOEntregaARendir().setRefund(
            paymentScheduleDetail.getSCOEntregaARendir().getRefund().subtract(invPayment.negate()));

        OBDal.getInstance().save(paymentScheduleDetail.getSCOEntregaARendir());
        System.out.println("Entrega a rendir:" + invPayment);
        assignedAmount = assignedAmount.add(recordAmount);

        FIN_PaymentDetail detail = null;
        if (bPartnerGirados != null)
          detail = dao.getNewPaymentDetail(payment, paymentScheduleDetail, invPayment,
              new BigDecimal(0), false, null, paymentScheduleDetail.getScoPrepayment(),
              paymentScheduleDetail.getSCOEntregaARendir(), tc, curr, recordAmount,
              bPartnerGirados.get(numSchedule));
        else
          detail = dao.getNewPaymentDetail(payment, paymentScheduleDetail, invPayment,
              new BigDecimal(0), false, null, paymentScheduleDetail.getScoPrepayment(),
              paymentScheduleDetail.getSCOEntregaARendir(), tc, curr, recordAmount);
      }

      // TODO: Review this condition !=0??
      if (assignedAmount.compareTo(payment.getAmount()) == -1) {
        FIN_PaymentScheduleDetail refundScheduleDetail = dao.getNewPaymentScheduleDetail(
            payment.getOrganization(), payment.getAmount().subtract(assignedAmount));

        if (isOverpayment) {
          payment.setScoOverpaymentamt(payment.getAmount().subtract(assignedAmount));
        }

        dao.getNewPaymentDetail(payment, refundScheduleDetail,
            payment.getAmount().subtract(assignedAmount), BigDecimal.ZERO, isOverpayment, null,
            BigDecimal.ONE, payment.getAccount().getCurrency(),
            payment.getAmount().subtract(assignedAmount));
      }
    } catch (final Exception e) {
      e.printStackTrace(System.err);
      throw new OBException(e);
    } finally {
      OBContext.restorePreviousMode();
    }

    return payment;
  }

  /*
   * Temporary method to supply defaults for exchange Rate and converted amount
   */
  public static FIN_Payment savePayment(FIN_Payment _payment, boolean isReceipt,
      DocumentType docType, String strPaymentDocumentNo, BusinessPartner businessPartner,
      FIN_PaymentMethod paymentMethod, FIN_FinancialAccount finAccount, String strPaymentAmount,
      Date paymentDate, Organization organization, String referenceNo,
      List<FIN_PaymentScheduleDetail> selectedPaymentScheduleDetails,
      HashMap<String, BigDecimal> selectedPaymentScheduleDetailsAmounts, boolean isWriteoff,
      boolean isOverpayment) {
    return savePayment(null, _payment, isReceipt, docType, strPaymentDocumentNo, businessPartner,
        paymentMethod, finAccount, strPaymentAmount, paymentDate, organization, referenceNo,
        selectedPaymentScheduleDetails, selectedPaymentScheduleDetailsAmounts, isWriteoff,
        isOverpayment, null, null, null);
  }

  public static FIN_Payment setFinancialTransactionAmountAndRate(VariablesSecureApp vars,
      FIN_Payment payment, BigDecimal finTxnConvertRate, BigDecimal finTxnAmount) {
    if (payment == null) {
      return payment;
    }

    BigDecimal paymentAmount = payment.getAmount();
    /*
     * if (paymentAmount == null) { paymentAmount = BigDecimal.ZERO; }
     * 
     * if (finTxnConvertRate == null || finTxnConvertRate.compareTo(BigDecimal.ZERO) <= 0) {
     * finTxnConvertRate = BigDecimal.ONE; } if (finTxnAmount == null ||
     * finTxnAmount.compareTo(BigDecimal.ZERO) == 0) { finTxnAmount =
     * paymentAmount.multiply(finTxnConvertRate); } else if
     * (paymentAmount.compareTo(BigDecimal.ZERO) != 0) { // Correct exchange rate for rounding that
     * occurs in UI finTxnConvertRate = finTxnAmount.divide(paymentAmount, MathContext.DECIMAL64);
     * if (vars != null) { DecimalFormat generalQtyRelationFmt = Utility.getFormat(vars,
     * "generalQtyEdition"); finTxnConvertRate = finTxnConvertRate
     * .setScale(generalQtyRelationFmt.getMaximumFractionDigits(), BigDecimal.ROUND_HALF_UP); } }
     * 
     * payment.setFinancialTransactionAmount(finTxnAmount);
     */

    // Ahora siempre el monto a pagar es a la moneda del banco
    payment.setFinancialTransactionAmount(paymentAmount);
    payment.setFinancialTransactionConvertRate(finTxnConvertRate);

    return payment;
  }

  public static FIN_Payment setFinancialTransactionAmountAndRate(FIN_Payment payment,
      BigDecimal finTxnConvertRate, BigDecimal finTxnAmount) {
    return setFinancialTransactionAmountAndRate(null, payment, finTxnConvertRate, finTxnAmount);
  }

  /*
   * public static FIN_Payment createRefundPayment(ConnectionProvider conProvider,
   * VariablesSecureApp vars, FIN_Payment payment, BigDecimal refundAmount) { return
   * createRefundPayment(conProvider, vars, payment, refundAmount, null); }
   */

  /*
   * public static FIN_Payment createRefundPayment(ConnectionProvider conProvider,
   * VariablesSecureApp vars, FIN_Payment payment, BigDecimal refundAmount, BigDecimal
   * conversionRate) { dao = new AdvPaymentMngtDao(); FIN_Payment refundPayment; if
   * (payment.getFINPaymentDetailList().isEmpty()) refundPayment = payment; else { refundPayment =
   * (FIN_Payment) DalUtil.copy(payment, false); String strDescription =
   * Utility.messageBD(conProvider, "APRM_RefundPayment", vars.getLanguage()); strDescription +=
   * ": " + payment.getDocumentNo(); refundPayment.setDescription(strDescription);
   * refundPayment.setGeneratedCredit(BigDecimal.ZERO); final String strDocumentNo =
   * FIN_Utility.getDocumentNo(payment.getOrganization(),
   * payment.getDocumentType().getDocumentCategory(), "DocumentNo_FIN_Payment");
   * refundPayment.setDocumentNo(strDocumentNo); } refundPayment.setProcessed(false);
   * refundPayment.setStatus("RPAP"); OBDal.getInstance().save(refundPayment);
   * OBDal.getInstance().flush(); refundPayment.setAmount(refundAmount);
   * refundPayment.setUsedCredit(refundAmount.negate());
   * 
   * setFinancialTransactionAmountAndRate(refundPayment, conversionRate, null);
   * 
   * FIN_PaymentScheduleDetail refundScheduleDetail =
   * dao.getNewPaymentScheduleDetail(payment.getOrganization(), refundAmount);
   * dao.getNewPaymentDetail(refundPayment, refundScheduleDetail, refundAmount, BigDecimal.ZERO,
   * true, null);
   * 
   * return refundPayment; }
   */
  /**
   * Adds new Details to the given Payment Proposal based on the List of Payment Schedule Details.
   * 
   * @param paymentProposal
   *          FIN_PaymentProposal where new Details are added.
   * @param paymentAmount
   *          Total amount to be paid.
   * @param selectedPaymentScheduleDetails
   *          List of FIN_PaymentScheduleDetail that needs to be added to the Payment Proposal.
   * @param selectedPaymentScheduleDetailAmounts
   *          HashMap with the Amount to be paid for each Scheduled Payment Detail.
   * @param writeOffAmt
   *          Total amount to be written off.
   */
  /*
   * public static void savePaymentProposal(FIN_PaymentProposal paymentProposal, BigDecimal
   * paymentAmount, List<FIN_PaymentScheduleDetail> selectedPaymentScheduleDetails, HashMap<String,
   * BigDecimal> selectedPaymentScheduleDetailAmounts, BigDecimal writeOffAmt) { dao = new
   * AdvPaymentMngtDao(); paymentProposal.setAmount(paymentAmount);
   * paymentProposal.setWriteoffAmount((writeOffAmt != null) ? writeOffAmt : BigDecimal.ZERO);
   * BigDecimal convertRate = paymentProposal.getFinancialTransactionConvertRate(); if
   * (BigDecimal.ONE.equals(convertRate)) {
   * paymentProposal.setFinancialTransactionAmount(paymentAmount); } else { Currency
   * finAccountCurrency = paymentProposal.getAccount().getCurrency(); BigDecimal finAccountTxnAmount
   * = paymentAmount.multiply(convertRate); long faPrecision =
   * finAccountCurrency.getStandardPrecision(); finAccountTxnAmount =
   * finAccountTxnAmount.setScale((int) faPrecision, RoundingMode.HALF_UP);
   * 
   * paymentProposal.setFinancialTransactionAmount(finAccountTxnAmount); }
   * 
   * for (FIN_PaymentScheduleDetail paymentScheduleDetail : selectedPaymentScheduleDetails) {
   * BigDecimal detailWriteOffAmt = null; if (writeOffAmt != null) detailWriteOffAmt =
   * paymentScheduleDetail .getAmount
   * ().subtract(selectedPaymentScheduleDetailAmounts.get(paymentScheduleDetail .getId()));
   * 
   * dao.getNewPaymentProposalDetail(paymentProposal.getOrganization(), paymentProposal,
   * paymentScheduleDetail, selectedPaymentScheduleDetailAmounts.get(paymentScheduleDetail.getId()),
   * detailWriteOffAmt, null); } }
   */

  /**
   * It adds to the Payment a new Payment Detail with the given GL Item and amount.
   * 
   * @param payment
   *          Payment where the new Payment Detail needs to be added.
   * @param glitemAmount
   *          Amount of the new Payment Detail.
   * @param glitem
   *          GLItem to be set in the new Payment Detail.
   */
  public static void saveGLItem(FIN_Payment payment, BigDecimal glitemAmount, GLItem glitem) {
    // FIXME: added to access the FIN_PaymentSchedule and
    // FIN_PaymentScheduleDetail tables to be
    // removed when new security implementation is done
    dao = new AdvPaymentMngtDao();
    OBContext.setAdminMode();
    try {
      FIN_PaymentScheduleDetail psd = dao.getNewPaymentScheduleDetail(payment.getOrganization(),
          glitemAmount);
      FIN_PaymentDetail pd = dao.getNewPaymentDetail(payment, psd, glitemAmount, BigDecimal.ZERO,
          false, glitem, BigDecimal.ONE, payment.getAccount().getCurrency(), glitemAmount);
      pd.setFinPayment(payment);
      OBDal.getInstance().save(pd);
      OBDal.getInstance().save(payment);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * It adds to the Payment a new Payment Detail with the given GL Item, amount and accounting
   * dimensions
   * 
   * @param payment
   *          Payment where the new Payment Detail needs to be added.
   * @param glitemAmount
   *          Amount of the new Payment Detail.
   * @param glitem
   *          GLItem to be set in the new Payment Detail.
   * @param businessPartner
   *          accounting dimension
   * @param product
   *          accounting dimension
   * @param project
   *          accounting dimension
   * @param campaign
   *          accounting dimension
   * @param activity
   *          accounting dimension
   * @param salesRegion
   *          accounting dimension
   * @param costCenter
   *          accounting dimension
   * @param user1
   *          accounting dimension
   * @param user2
   *          accounting dimension
   */
  public static void saveGLItem(FIN_Payment payment, BigDecimal glitemAmount, GLItem glitem,
      BusinessPartner businessPartner, Product product, Project project, Campaign campaign,
      ABCActivity activity, SalesRegion salesRegion, Costcenter costCenter, UserDimension1 user1,
      UserDimension2 user2, SCOInternalDoc internaldoc, Invoice invglref) {
    // FIXME: added to access the FIN_PaymentSchedule and
    // FIN_PaymentScheduleDetail tables to be
    // removed when new security implementation is done
    dao = new AdvPaymentMngtDao();
    OBContext.setAdminMode();
    try {
      FIN_PaymentScheduleDetail psd = dao.getNewPaymentScheduleDetail(payment.getOrganization(),
          glitemAmount, businessPartner, product, project, campaign, activity, salesRegion,
          costCenter, user1, user2, internaldoc, invglref, null);
      psd.setSCODescription("--");
      FIN_PaymentDetail pd = dao.getNewPaymentDetail(payment, psd, glitemAmount, BigDecimal.ZERO,
          false, glitem, BigDecimal.ONE, payment.getAccount().getCurrency(), glitemAmount);
      pd.setScoDescription("--");
      pd.setFinPayment(payment);
      OBDal.getInstance().save(pd);
      OBDal.getInstance().save(payment);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public static void saveGLItemFactInv(FIN_Payment payment, BigDecimal glitemAmount, GLItem glitem,
      BusinessPartner businessPartner, Product product, Project project, Campaign campaign,
      ABCActivity activity, SalesRegion salesRegion, Costcenter costCenter, UserDimension1 user1,
      UserDimension2 user2, String description, String glitemFactoringInvId) {
    // FIXME: added to access the FIN_PaymentSchedule and
    // FIN_PaymentScheduleDetail tables to be
    // removed when new security implementation is done
    dao = new AdvPaymentMngtDao();
    OBContext.setAdminMode();

    if (description == null || description.equals(""))
      description = "--";

    Invoice factoredInv = null;
    if (glitemFactoringInvId != null && glitemFactoringInvId.compareTo("") != 0) {
      factoredInv = OBDal.getInstance().get(Invoice.class, glitemFactoringInvId);
    }

    try {
      FIN_PaymentScheduleDetail psd = dao.getNewPaymentScheduleDetail(payment.getOrganization(),
          glitemAmount, businessPartner, product, project, campaign, activity, salesRegion,
          costCenter, user1, user2, null, null, null);
      psd.setSCODescription(description);
      FIN_PaymentDetail pd = dao.getNewPaymentDetail(payment, psd, glitemAmount, BigDecimal.ZERO,
          false, glitem, BigDecimal.ONE, payment.getAccount().getCurrency(), glitemAmount, null,
          null, null, null, factoredInv);
      pd.setScoDescription(description);
      pd.setFinPayment(payment);

      OBDal.getInstance().save(pd);
      OBDal.getInstance().save(payment);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public static void saveGLItem(FIN_Payment payment, BigDecimal glitemAmount, GLItem glitem,
      BusinessPartner businessPartner, Product product, Project project, Campaign campaign,
      ABCActivity activity, SalesRegion salesRegion, Costcenter costCenter, UserDimension1 user1,
      UserDimension2 user2, String description, String glitemBoeId, String glitemLoanDocInvId,
      String glitemPrepaymentId, String glitemRendcuentasId, SCOInternalDoc internaldoc,
      Invoice invglref, SCOEEFFCashflow cashflow) {
    // FIXME: added to access the FIN_PaymentSchedule and
    // FIN_PaymentScheduleDetail tables to be
    // removed when new security implementation is done
    dao = new AdvPaymentMngtDao();
    OBContext.setAdminMode();

    if (description == null || description.equals(""))
      description = "--";

    Invoice boe = null;
    if (glitemBoeId != null && glitemBoeId.compareTo("") != 0) {
      boe = OBDal.getInstance().get(Invoice.class, glitemBoeId);
    }

    Invoice loandoc_inv = null;
    if (glitemLoanDocInvId != null && glitemLoanDocInvId.compareTo("") != 0) {
      loandoc_inv = OBDal.getInstance().get(Invoice.class, glitemLoanDocInvId);
    }

    try {
      FIN_PaymentScheduleDetail psd = dao.getNewPaymentScheduleDetail(payment.getOrganization(),
          glitemAmount, businessPartner, product, project, campaign, activity, salesRegion,
          costCenter, user1, user2, internaldoc, invglref, cashflow);
      psd.setSCODescription(description);
      FIN_PaymentDetail pd = dao.getNewPaymentDetail(payment, psd, glitemAmount, BigDecimal.ZERO,
          false, glitem, BigDecimal.ONE, payment.getAccount().getCurrency(), glitemAmount,
          glitemPrepaymentId, glitemRendcuentasId, boe, loandoc_inv, null);
      pd.setScoDescription(description);
      pd.setFinPayment(payment);

      OBDal.getInstance().save(pd);
      OBDal.getInstance().save(payment);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public static void saveGLItem(FIN_Payment payment, BigDecimal glitemAmount, GLItem glitem,
      BusinessPartner businessPartner, Product product, Project project, Campaign campaign,
      ABCActivity activity, SalesRegion salesRegion, Costcenter costCenter, UserDimension1 user1,
      UserDimension2 user2, String description, BigDecimal glitemConvertedAmount,
      BigDecimal exchangeRate, Currency curr, SCOInternalDoc internaldoc, Invoice invglref) {

    saveGLItem(payment, glitemAmount, glitem, businessPartner, product, project, campaign, activity,
        salesRegion, costCenter, user1, user2, description, glitemConvertedAmount, exchangeRate,
        curr, internaldoc, invglref, null);

  }

  public static void saveGLItem(FIN_Payment payment, BigDecimal glitemAmount, GLItem glitem,
      BusinessPartner businessPartner, Product product, Project project, Campaign campaign,
      ABCActivity activity, SalesRegion salesRegion, Costcenter costCenter, UserDimension1 user1,
      UserDimension2 user2, String description, BigDecimal glitemConvertedAmount,
      BigDecimal exchangeRate, Currency curr, SCOInternalDoc internaldoc, Invoice invglref,
      String giradoA) {
    // FIXME: added to access the FIN_PaymentSchedule and
    // FIN_PaymentScheduleDetail tables to be
    // removed when new security implementation is done
    dao = new AdvPaymentMngtDao();
    OBContext.setAdminMode();

    if (description == null || description.equals(""))
      description = "--";

    try {
      FIN_PaymentScheduleDetail psd = dao.getNewPaymentScheduleDetail(payment.getOrganization(),
          glitemAmount, businessPartner, product, project, campaign, activity, salesRegion,
          costCenter, user1, user2, internaldoc, invglref, null);
      psd.setSCODescription(description);
      FIN_PaymentDetail pd = dao.getNewPaymentDetail(payment, psd, glitemAmount, BigDecimal.ZERO,
          false, glitem, null, null, exchangeRate, curr, glitemConvertedAmount, giradoA);
      pd.setScoDescription(description);
      pd.setFinPayment(payment);
      OBDal.getInstance().save(pd);
      OBDal.getInstance().save(payment);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public static FIN_PaymentDetail saveGLItemRetPD(FIN_Payment payment, BigDecimal glitemAmount,
      GLItem glitem, BusinessPartner businessPartner, Product product, Project project,
      Campaign campaign, ABCActivity activity, SalesRegion salesRegion, Costcenter costCenter,
      UserDimension1 user1, UserDimension2 user2, String description,
      BigDecimal glitemConvertedAmount, BigDecimal exchangeRate, Currency curr, String glitemBoeId,
      String glitemLoanDocInvId, String glitemPrepaymentId, String glitemRendcuentasId,
      SCOInternalDoc internaldoc, Invoice invglref) {
    // FIXME: added to access the FIN_PaymentSchedule and
    // FIN_PaymentScheduleDetail tables to be
    // removed when new security implementation is done
    dao = new AdvPaymentMngtDao();
    OBContext.setAdminMode();

    if (description == null || description.equals(""))
      description = "--";

    Invoice boe = null;
    if (glitemBoeId != null && glitemBoeId.compareTo("") != 0) {
      boe = OBDal.getInstance().get(Invoice.class, glitemBoeId);
    }

    Invoice loandoc_inv = null;
    if (glitemLoanDocInvId != null && glitemLoanDocInvId.compareTo("") != 0) {
      loandoc_inv = OBDal.getInstance().get(Invoice.class, glitemLoanDocInvId);
    }

    FIN_PaymentDetail pd = null;
    try {
      FIN_PaymentScheduleDetail psd = dao.getNewPaymentScheduleDetail(payment.getOrganization(),
          glitemAmount, businessPartner, product, project, campaign, activity, salesRegion,
          costCenter, user1, user2, internaldoc, invglref, null);
      psd.setSCODescription(description);
      pd = dao.getNewPaymentDetail(payment, psd, glitemAmount, BigDecimal.ZERO, false, glitem,
          exchangeRate, curr, glitemConvertedAmount, glitemPrepaymentId, glitemRendcuentasId, boe,
          loandoc_inv, null);
      pd.setScoDescription(description);
      pd.setFinPayment(payment);

      OBDal.getInstance().save(pd);
      OBDal.getInstance().save(payment);
    } finally {
      OBContext.restorePreviousMode();
    }

    return pd;
  }

  public static void saveGLItem(FIN_Payment payment, BigDecimal glitemAmount, GLItem glitem,
      BusinessPartner businessPartner, Product product, Project project, Campaign campaign,
      ABCActivity activity, SalesRegion salesRegion, Costcenter costCenter, UserDimension1 user1,
      UserDimension2 user2, String description, BigDecimal glitemConvertedAmount,
      BigDecimal exchangeRate, Currency curr, String glitemBoeId, String glitemLoanDocInvId,
      String glitemPrepaymentId, String glitemRendcuentasId, SCOInternalDoc internaldoc,
      Invoice invglref) {
    // FIXME: added to access the FIN_PaymentSchedule and
    // FIN_PaymentScheduleDetail tables to be
    // removed when new security implementation is done
    dao = new AdvPaymentMngtDao();
    OBContext.setAdminMode();

    if (description == null || description.equals(""))
      description = "--";

    Invoice boe = null;
    if (glitemBoeId != null && glitemBoeId.compareTo("") != 0) {
      boe = OBDal.getInstance().get(Invoice.class, glitemBoeId);
    }

    Invoice loandoc_inv = null;
    if (glitemLoanDocInvId != null && glitemLoanDocInvId.compareTo("") != 0) {
      loandoc_inv = OBDal.getInstance().get(Invoice.class, glitemLoanDocInvId);
    }

    try {
      FIN_PaymentScheduleDetail psd = dao.getNewPaymentScheduleDetail(payment.getOrganization(),
          glitemAmount, businessPartner, product, project, campaign, activity, salesRegion,
          costCenter, user1, user2, internaldoc, invglref, null);
      psd.setSCODescription(description);
      FIN_PaymentDetail pd = dao.getNewPaymentDetail(payment, psd, glitemAmount, BigDecimal.ZERO,
          false, glitem, exchangeRate, curr, glitemConvertedAmount, glitemPrepaymentId,
          glitemRendcuentasId, boe, loandoc_inv, null);
      pd.setScoDescription(description);
      pd.setFinPayment(payment);

      OBDal.getInstance().save(pd);
      OBDal.getInstance().save(payment);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public static FIN_PaymentDetail saveGLItemRPD(FIN_Payment payment, BigDecimal glitemAmount,
      GLItem glitem, BusinessPartner businessPartner, Product product, Project project,
      Campaign campaign, ABCActivity activity, SalesRegion salesRegion, Costcenter costCenter,
      UserDimension1 user1, UserDimension2 user2, String description,
      BigDecimal glitemConvertedAmount, BigDecimal exchangeRate, Currency curr,
      String glitemPrepaymentId, String glitemRendcuentasId, SCOInternalDoc internaldoc,
      Invoice invglref) {

    return saveGLItemRPD(payment, glitemAmount, glitem, businessPartner, product, project, campaign,
        activity, salesRegion, costCenter, user1, user2, description, glitemConvertedAmount,
        exchangeRate, curr, glitemPrepaymentId, glitemRendcuentasId, internaldoc, invglref, null);
  }

  public static FIN_PaymentDetail saveGLItemRPD(FIN_Payment payment, BigDecimal glitemAmount,
      GLItem glitem, BusinessPartner businessPartner, Product product, Project project,
      Campaign campaign, ABCActivity activity, SalesRegion salesRegion, Costcenter costCenter,
      UserDimension1 user1, UserDimension2 user2, String description,
      BigDecimal glitemConvertedAmount, BigDecimal exchangeRate, Currency curr,
      String glitemPrepaymentId, String glitemRendcuentasId, SCOInternalDoc internaldoc,
      Invoice invglref, String giradoA) {
    // FIXME: added to access the FIN_PaymentSchedule and
    // FIN_PaymentScheduleDetail tables to be
    // removed when new security implementation is done
    dao = new AdvPaymentMngtDao();
    FIN_PaymentDetail pd = null;
    OBContext.setAdminMode();

    if (description == null || description.equals(""))
      description = "--";

    try {
      FIN_PaymentScheduleDetail psd = dao.getNewPaymentScheduleDetail(payment.getOrganization(),
          glitemAmount, businessPartner, product, project, campaign, activity, salesRegion,
          costCenter, user1, user2, internaldoc, invglref, null);
      psd.setSCODescription(description);
      pd = dao.getNewPaymentDetail(payment, psd, glitemAmount, BigDecimal.ZERO, false, glitem,
          exchangeRate, curr, glitemConvertedAmount, glitemPrepaymentId, glitemRendcuentasId, null,
          null, null, giradoA);
      pd.setScoDescription(description);
      pd.setFinPayment(payment);
      OBDal.getInstance().save(pd);
      OBDal.getInstance().save(payment);
    } finally {
      OBContext.restorePreviousMode();
    }

    return pd;
  }

  /**
   * It adds to the Payment a new Payment Detail with the given GL Item, amount and accounting
   * dimensions
   * 
   * @param payment
   *          Payment where the new Payment Detail needs to be added.
   * @param glitemAmount
   *          Amount of the new Payment Detail.
   * @param glitem
   *          GLItem to be set in the new Payment Detail.
   * @param businessPartner
   *          accounting dimension
   * @param product
   *          accounting dimension
   * @param project
   *          accounting dimension
   * @param campaign
   *          accounting dimension
   * @param activity
   *          accounting dimension
   * @param salesRegion
   *          accounting dimension
   */
  public static void saveGLItem(FIN_Payment payment, BigDecimal glitemAmount, GLItem glitem,
      BusinessPartner businessPartner, Product product, Project project, Campaign campaign,
      ABCActivity activity, SalesRegion salesRegion) {
    saveGLItem(payment, glitemAmount, glitem, businessPartner, product, project, campaign, activity,
        salesRegion, null, null, null, null, null);
  }

  /**
   * Removes the Payment Detail from the Payment when the Detail is related to a GLItem
   * 
   * @param payment
   *          FIN_Payment that contains the Payment Detail.
   * @param paymentDetail
   *          FIN_PaymentDetail to be removed.
   */
  public static void removeGLItem(FIN_Payment payment, FIN_PaymentDetail paymentDetail) {
    // FIXME: added to access the FIN_PaymentSchedule and
    // FIN_PaymentScheduleDetail tables to be
    // removed when new security implementation is done
    dao = new AdvPaymentMngtDao();
    OBContext.setAdminMode();
    try {
      List<FIN_PaymentDetail> pdl = payment.getFINPaymentDetailList();
      if (paymentDetail != null) {
        pdl.remove(paymentDetail);
        OBDal.getInstance().remove(paymentDetail);
      } else {
        List<String> pdlIDs = new ArrayList<String>();
        for (FIN_PaymentDetail deletePaymentDetail : pdl)
          pdlIDs.add(deletePaymentDetail.getId());

        for (String pdlID : pdlIDs) {
          pdl.remove(dao.getObject(FIN_PaymentDetail.class, pdlID));
          OBDal.getInstance().remove(dao.getObject(FIN_PaymentDetail.class, pdlID));
        }
      }
      payment.setFINPaymentDetailList(pdl);
      OBDal.getInstance().save(payment);
      OBDal.getInstance().flush();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * It adds to the scheduledPaymentDetails List the FIN_PaymentScheduleDetails given in the
   * strSelectedPaymentDetailsIds comma separated String of Id's that are not yet included on it.
   * 
   * @param scheduledPaymentDetails
   *          List of FIN_PaymentScheduleDetail.
   * @param strSelectedPaymentDetailsIds
   *          String of comma separated id's that needs to be included in the List if they are not
   *          present.
   * @return returns a List of FIN_PaymentScheduleDetail including all the Payment Schedule Details.
   */
  public static List<FIN_PaymentScheduleDetail> getSelectedPaymentDetails(
      List<FIN_PaymentScheduleDetail> scheduledPaymentDetails,
      String strSelectedPaymentDetailsIds) {
    final List<FIN_PaymentScheduleDetail> selectedScheduledPaymentDetails;
    if (scheduledPaymentDetails == null)
      selectedScheduledPaymentDetails = new ArrayList<FIN_PaymentScheduleDetail>();
    else
      selectedScheduledPaymentDetails = scheduledPaymentDetails;
    // FIXME: added to access the FIN_PaymentSchedule and
    // FIN_PaymentScheduleDetail tables to be
    // removed when new security implementation is done
    OBContext.setAdminMode();
    try {
      // selected scheduled payments list
      final List<FIN_PaymentScheduleDetail> tempSelectedScheduledPaymentDetails = FIN_Utility
          .getOBObjectList(FIN_PaymentScheduleDetail.class, strSelectedPaymentDetailsIds);

      for (int j = 0; j < tempSelectedScheduledPaymentDetails.size(); j++) {
        if (tempSelectedScheduledPaymentDetails.get(j) == null) {
          tempSelectedScheduledPaymentDetails.remove(j);
          j--;
        }
      }

      for (FIN_PaymentScheduleDetail tempPaymentScheduleDetail : tempSelectedScheduledPaymentDetails) {
        boolean found = false;
        for (FIN_PaymentScheduleDetail selectedScheduledPaymentDetail : selectedScheduledPaymentDetails) {
          if (tempPaymentScheduleDetail.getId().equals(selectedScheduledPaymentDetail.getId())) {
            found = true;
            break;
          }
        }

        if (!found)
          selectedScheduledPaymentDetails.add(tempPaymentScheduleDetail);

      }

    } finally {
      OBContext.restorePreviousMode();
    }
    return selectedScheduledPaymentDetails;
  }

  /**
   * Creates a HashMap with the FIN_PaymentScheduleDetail id's and the amount gotten from the
   * Session.
   * 
   * The amounts are stored in Session like "inpPaymentAmount"+paymentScheduleDetail.Id
   * 
   * @param vars
   *          VariablseSecureApp with the session data.
   * @param selectedPaymentScheduleDetails
   *          List of FIN_PaymentScheduleDetails that need to be included in the HashMap.
   * @return A HashMap mapping the FIN_PaymentScheduleDetail's Id with the corresponding amount.
   */
  public static HashMap<String, BigDecimal> getSelectedPaymentDetailsAndAmount(
      VariablesSecureApp vars, List<FIN_PaymentScheduleDetail> selectedPaymentScheduleDetails)
      throws ServletException {
    return getSelectedBaseOBObjectAmount(vars, selectedPaymentScheduleDetails, "inpPaymentAmount");
  }

  /**
   * Creates a HashMap with the BaseOBObject id's and the amount gotten from the Session.
   * 
   * The amounts are stored in Session like "htmlElementId"+basobObject.Id
   * 
   * @param vars
   *          VariablseSecureApp with the session data.
   * @param selectedBaseOBObjects
   *          List of bobs that need to be included in the HashMap.
   * @return A HashMap mapping the Id with the corresponding amount.
   */
  public static <T extends BaseOBObject> HashMap<String, BigDecimal> getSelectedBaseOBObjectAmount(
      VariablesSecureApp vars, List<T> selectedBaseOBObjects, String htmlElementId)
      throws ServletException {
    HashMap<String, BigDecimal> selectedBaseOBObjectAmounts = new HashMap<String, BigDecimal>();

    for (final T o : selectedBaseOBObjects) {
      selectedBaseOBObjectAmounts.put((String) o.getId(),
          new BigDecimal(vars.getNumericParameter(htmlElementId + (String) o.getId(), "")));
    }
    return selectedBaseOBObjectAmounts;
  }

  private static List<FIN_PaymentScheduleDetail> getOrderedPaymentScheduleDetails(
      Set<String> psdSet) {
    OBCriteria<FIN_PaymentScheduleDetail> orderedPSDs = OBDal.getInstance()
        .createCriteria(FIN_PaymentScheduleDetail.class);
    orderedPSDs.add(Restrictions.in(FIN_PaymentScheduleDetail.PROPERTY_ID, psdSet));
    orderedPSDs.addOrderBy(FIN_PaymentScheduleDetail.PROPERTY_AMOUNT, true);
    return orderedPSDs.list();
  }

  private static HashMap<String, BigDecimal> calculateAmounts(BigDecimal recordAmount,
      Set<String> psdSet) {
    BigDecimal remainingAmount = recordAmount;
    HashMap<String, BigDecimal> recordsAmounts = new HashMap<String, BigDecimal>();
    // PSD needs to be properly ordered to ensure negative amounts are
    // processed first
    List<FIN_PaymentScheduleDetail> psds = getOrderedPaymentScheduleDetails(psdSet);
    for (FIN_PaymentScheduleDetail paymentScheduleDetail : psds) {
      BigDecimal outstandingAmount = paymentScheduleDetail.getAmount();
      // Manage negative amounts
      if ((remainingAmount.compareTo(BigDecimal.ZERO) > 0
          && remainingAmount.compareTo(outstandingAmount) >= 0)
          || ((remainingAmount.compareTo(BigDecimal.ZERO) == -1
              && outstandingAmount.compareTo(BigDecimal.ZERO) == -1)
              && (remainingAmount.compareTo(outstandingAmount) <= 0))) {
        recordsAmounts.put(paymentScheduleDetail.getId(), outstandingAmount);
        remainingAmount = remainingAmount.subtract(outstandingAmount);
      } else {
        recordsAmounts.put(paymentScheduleDetail.getId(), remainingAmount);
        if (psdSet.size() > 0) {
          remainingAmount = BigDecimal.ZERO;
        }
      }

    }
    return recordsAmounts;
  }

  /**
   * Builds a FieldProvider with a set of Payment Schedule Details based on the
   * selectedScheduledPaymentDetails and filteredScheduledPaymentDetails Lists. When the firstLoad
   * parameter is true the "paymentAmount" field is loaded from the corresponding Payment Proposal
   * Detail if it exists, when false it gets the amount from session.
   * 
   * @param vars
   *          VariablesSecureApp containing the Session data.
   * @param selectedScheduledPaymentDetails
   *          List of FIN_PaymentScheduleDetails that need to be selected by default.
   * @param filteredScheduledPaymentDetails
   *          List of FIN_PaymentScheduleDetails that need to be unselected by default.
   * @param firstLoad
   *          Boolean to set if the PaymentAmount is gotten from the PaymentProposal (true) or from
   *          Session (false)
   * @param paymentProposal
   *          PaymentProposal used to get the amount when firstLoad is true.
   * @return a FieldProvider object with all the given FIN_PaymentScheduleDetails.
   */
  public static FieldProvider[] getShownScheduledPaymentDetails(VariablesSecureApp vars,
      List<FIN_PaymentScheduleDetail> selectedScheduledPaymentDetails,
      List<FIN_PaymentScheduleDetail> filteredScheduledPaymentDetails, boolean firstLoad,
      FIN_PaymentProposal paymentProposal, boolean isMulticurrency, Currency currency,
      Currency finaccCurrency, BigDecimal exchangeRate, boolean isReceipt) throws ServletException {
    return getShownScheduledPaymentDetails(vars, selectedScheduledPaymentDetails,
        filteredScheduledPaymentDetails, firstLoad, paymentProposal, null, isMulticurrency,
        currency, finaccCurrency, exchangeRate, isReceipt);
  }

  public static FieldProvider[] getShownScheduledPaymentDetails(VariablesSecureApp vars,
      List<FIN_PaymentScheduleDetail> selectedScheduledPaymentDetails,
      List<FIN_PaymentScheduleDetail> filteredScheduledPaymentDetails, boolean firstLoad,
      FIN_PaymentProposal paymentProposal, String strSelectedPaymentDetails,
      boolean isMulticurrency, Currency currency, Currency finaccCurrency, BigDecimal exchangeRate,
      boolean isReceipt) throws ServletException {
    return getShownScheduledPaymentDetails(vars, selectedScheduledPaymentDetails,
        filteredScheduledPaymentDetails, firstLoad, paymentProposal, strSelectedPaymentDetails,
        false, isMulticurrency, currency, finaccCurrency, exchangeRate, isReceipt);
  }

  public static FieldProvider[] getShownScheduledPaymentDetails(VariablesSecureApp vars,
      List<FIN_PaymentScheduleDetail> selectedScheduledPaymentDetails,
      List<FIN_PaymentScheduleDetail> filteredScheduledPaymentDetails, boolean firstLoad,
      FIN_PaymentProposal paymentProposal, String strSelectedPaymentDetails,
      boolean showDoubtfulDebtAmount, boolean isMulticurrency, Currency currency,
      Currency finaccCurrency, BigDecimal exchangeRate, boolean isReceipt) throws ServletException {

    String strSelectedRecords = "";
    if (!"".equals(strSelectedPaymentDetails) && strSelectedPaymentDetails != null) {
      strSelectedRecords = strSelectedPaymentDetails;
      strSelectedRecords = strSelectedRecords.replace("(", "");
      strSelectedRecords = strSelectedRecords.replace(")", "");
    }
    dao = new AdvPaymentMngtDao();
    final List<FIN_PaymentScheduleDetail> shownScheduledPaymentDetails = new ArrayList<FIN_PaymentScheduleDetail>();
    shownScheduledPaymentDetails.addAll(selectedScheduledPaymentDetails);
    shownScheduledPaymentDetails.addAll(filteredScheduledPaymentDetails);
    FIN_PaymentScheduleDetail[] FIN_PaymentScheduleDetails = new FIN_PaymentScheduleDetail[0];
    FIN_PaymentScheduleDetails = shownScheduledPaymentDetails.toArray(FIN_PaymentScheduleDetails);
    FieldProvider[] data = FieldProviderFactory.getFieldProviderArray(shownScheduledPaymentDetails);
    String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat dateFormater = new SimpleDateFormat(dateFormat);
    // FIXME: added to access the FIN_PaymentSchedule and
    // FIN_PaymentScheduleDetail tables to be
    // removed when new security implementation is done
    OBContext.setAdminMode();
    try {
      for (int i = 0; i < data.length; i++) {

        if (FIN_PaymentScheduleDetails[i].getOrderPaymentSchedule() != null
            || FIN_PaymentScheduleDetails[i].getInvoicePaymentSchedule() != null) {

          boolean docissotrx = false;
          if (FIN_PaymentScheduleDetails[i].getInvoicePaymentSchedule() != null) {
            docissotrx = FIN_PaymentScheduleDetails[i].getInvoicePaymentSchedule().getInvoice()
                .isSalesTransaction();
          }

          String selectedId = (selectedScheduledPaymentDetails
              .contains(FIN_PaymentScheduleDetails[i])) ? FIN_PaymentScheduleDetails[i].getId()
                  : "";
          // If selectedId belongs to a grouping selection calculate
          // whether it should be selected
          // or
          // not
          if (!"".equals(selectedId) && !"".equals(strSelectedPaymentDetails)
              && strSelectedPaymentDetails != null) {
            StringTokenizer records = new StringTokenizer(strSelectedRecords, "'");
            Set<String> recordSet = new LinkedHashSet<String>();
            while (records.hasMoreTokens()) {
              recordSet.add(records.nextToken());
            }
            if (recordSet.contains(selectedId)) {
              FieldProviderFactory.setField(data[i], "finSelectedPaymentDetailId", selectedId);
            } else {
              String selectedRecord = FIN_PaymentScheduleDetails[i].getId();
              // Find record which contains psdId
              Set<String> psdIdSet = new LinkedHashSet<String>();
              for (String record : recordSet) {
                if (record.contains(selectedId)) {
                  selectedRecord = record;
                  StringTokenizer st = new StringTokenizer(record, ",");
                  while (st.hasMoreTokens()) {
                    psdIdSet.add(st.nextToken());
                  }
                }
              }
              String psdAmount = vars.getNumericParameter("inpPaymentAmount" + selectedRecord, "");

              /*
               * String currSymbol = vars.getStringParameter("inpRecordCurrSymbol" + selectedRecord,
               * ""); String currState = vars.getStringParameter("inpRecordCurrency" +
               * selectedRecord, ""); System.out.println(psdAmount+
               * " "+currSymbol+" "+currState+" B");
               */
              HashMap<String, BigDecimal> idsAmounts = calculateAmounts(new BigDecimal(psdAmount),
                  psdIdSet);
              BigDecimal paymentAmount = idsAmounts.get(selectedId);
              if (docissotrx != isReceipt) {
                paymentAmount = paymentAmount.negate();
              }
              FieldProviderFactory.setField(data[i], "finSelectedPaymentDetailId", selectedId);
              FieldProviderFactory.setField(data[i], "paymentAmount", paymentAmount.toString());

              /*
               * String sourceAmt = psdAmount; if(currState.equals("1")){ sourceAmt = new BigDecimal
               * (psdAmount).multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP).toString(); }
               * 
               * FieldProviderFactory.setField(data[i], "paymentSourceAmount", currSymbol +
               * sourceAmt);
               */
            }
          }

          String actualCurr = null;
          String currPayment = finaccCurrency.getId();

          FieldProviderFactory.setField(data[i], "finSelectedPaymentDetailId",
              (selectedScheduledPaymentDetails.contains(FIN_PaymentScheduleDetails[i]))
                  ? FIN_PaymentScheduleDetails[i].getId()
                  : "");
          FieldProviderFactory.setField(data[i], "finScheduledPaymentDetailId",
              FIN_PaymentScheduleDetails[i].getId());
          if (FIN_PaymentScheduleDetails[i].getOrderPaymentSchedule() != null) {
            FieldProviderFactory.setField(data[i], "orderNr",
                FIN_PaymentScheduleDetails[i].getOrderPaymentSchedule().getOrder().getDocumentNo());
            FieldProviderFactory.setField(data[i], "orderNrTrunc",
                FIN_PaymentScheduleDetails[i].getOrderPaymentSchedule().getOrder().getDocumentNo());
            FieldProviderFactory.setField(data[i], "orderPaymentScheduleId",
                FIN_PaymentScheduleDetails[i].getOrderPaymentSchedule().getId());
            actualCurr = FIN_PaymentScheduleDetails[i].getOrderPaymentSchedule().getCurrency()
                .getId();
          } else {
            FieldProviderFactory.setField(data[i], "orderNr", "");
            FieldProviderFactory.setField(data[i], "orderNrTrunc", "");
            FieldProviderFactory.setField(data[i], "orderPaymentScheduleId", "");
          }
          if (FIN_PaymentScheduleDetails[i].getInvoicePaymentSchedule() != null) {
            FIN_PaymentSchedule psd = FIN_PaymentScheduleDetails[i].getInvoicePaymentSchedule();
            OrganizationInformation orgInfo = OBDao.getActiveOBObjectList(psd.getOrganization(),
                Organization.PROPERTY_ORGANIZATIONINFORMATIONLIST) != null
                    ? (OrganizationInformation) OBDao.getActiveOBObjectList(psd.getOrganization(),
                        Organization.PROPERTY_ORGANIZATIONINFORMATIONLIST).get(0)
                    : null;
            if (!psd.getInvoice().isSalesTransaction() && orgInfo != null
                && orgInfo.getAPRMPaymentDescription().equals("Supplier Reference")) {
              // When the Organization of the Invoice sets that
              // the Invoice Document No. is the
              // supplier's
              FieldProviderFactory.setField(data[i], "invoiceNr", FIN_PaymentScheduleDetails[i]
                  .getInvoicePaymentSchedule().getInvoice().getOrderReference());
              FieldProviderFactory.setField(data[i], "invoiceNrTrunc", FIN_PaymentScheduleDetails[i]
                  .getInvoicePaymentSchedule().getInvoice().getOrderReference());
            } else {
              // When the Organization of the Invoice sets that
              // the Invoice Document No. is the
              // default
              // Invoice Number
              FieldProviderFactory.setField(data[i], "invoiceNr", FIN_PaymentScheduleDetails[i]
                  .getInvoicePaymentSchedule().getInvoice().getDocumentNo());
              FieldProviderFactory.setField(data[i], "invoiceNrTrunc", FIN_PaymentScheduleDetails[i]
                  .getInvoicePaymentSchedule().getInvoice().getDocumentNo());
            }
            FieldProviderFactory.setField(data[i], "invoicePaymentScheduleId",
                FIN_PaymentScheduleDetails[i].getInvoicePaymentSchedule().getId());

            actualCurr = FIN_PaymentScheduleDetails[i].getInvoicePaymentSchedule().getCurrency()
                .getId();

          } else {
            FieldProviderFactory.setField(data[i], "invoiceNr", "");
            FieldProviderFactory.setField(data[i], "invoiceNrTrunc", "");
            FieldProviderFactory.setField(data[i], "invoicePaymentScheduleId", "");
          }

          if (actualCurr.equals(currPayment))
            FieldProviderFactory.setField(data[i], "currencyFlag", "1");
          else
            FieldProviderFactory.setField(data[i], "currencyFlag", "0");
          // BI-MONEDA
          if (actualCurr.equals("100"))// dolar
            FieldProviderFactory.setField(data[i], "currencySymbol", "$");
          if (actualCurr.equals("102"))// euros
            FieldProviderFactory.setField(data[i], "currencySymbol", "EUR");
          if (actualCurr.equals("308"))// soles
            FieldProviderFactory.setField(data[i], "currencySymbol", "S/.");

          if (FIN_PaymentScheduleDetails[i].getInvoicePaymentSchedule() != null) {
            FieldProviderFactory.setField(data[i], "expectedDate",
                dateFormater
                    .format(
                        FIN_PaymentScheduleDetails[i].getInvoicePaymentSchedule().getExpectedDate())
                    .toString());
            FieldProviderFactory.setField(data[i], "dueDate",
                dateFormater
                    .format(FIN_PaymentScheduleDetails[i].getInvoicePaymentSchedule().getDueDate())
                    .toString());
            FieldProviderFactory.setField(data[i], "transactionDate",
                dateFormater.format(FIN_PaymentScheduleDetails[i].getInvoicePaymentSchedule()
                    .getInvoice().getInvoiceDate()).toString());

            Currency currInvoice = FIN_PaymentScheduleDetails[i].getInvoicePaymentSchedule()
                .getCurrency();
            DecimalFormat df = new DecimalFormat("#.00");

            BigDecimal invgrandTotalAmount = FIN_PaymentScheduleDetails[i]
                .getInvoicePaymentSchedule().getInvoice().getGrandTotalAmount();
            if (docissotrx != isReceipt) {
              invgrandTotalAmount = invgrandTotalAmount.negate();
            }

            BigDecimal psexpectedAmount = FIN_PaymentScheduleDetails[i].getInvoicePaymentSchedule()
                .getAmount();
            if (docissotrx != isReceipt) {
              psexpectedAmount = psexpectedAmount.negate();
            }

            BigDecimal psdoutstandingAmount = FIN_PaymentScheduleDetails[i].getAmount();
            if (docissotrx != isReceipt) {
              psdoutstandingAmount = psdoutstandingAmount.negate();
            }

            FieldProviderFactory.setField(data[i], "invoicedSourceAmount",
                currInvoice.getSymbol() + df.format(invgrandTotalAmount));

            if (!isMulticurrency) {

              FieldProviderFactory.setField(data[i], "invoicedAmount",
                  invgrandTotalAmount.toString());
              FieldProviderFactory.setField(data[i], "expectedAmount", psexpectedAmount.toString());
              FieldProviderFactory.setField(data[i], "outstandingAmount",
                  psdoutstandingAmount.toString());

              FieldProviderFactory.setField(data[i], "recordInvAmt",
                  invgrandTotalAmount.toString());

            } else {

              if (currInvoice.getId().equals(finaccCurrency.getId())) {
                FieldProviderFactory.setField(data[i], "invoicedAmount",
                    invgrandTotalAmount.toString());
                FieldProviderFactory.setField(data[i], "expectedAmount",
                    psexpectedAmount.toString());
                FieldProviderFactory.setField(data[i], "outstandingAmount",
                    psdoutstandingAmount.toString());

                FieldProviderFactory.setField(data[i], "recordInvAmt",
                    invgrandTotalAmount.toString());

              } else {

                if (exchangeRate.compareTo(BigDecimal.ZERO) == 0)
                  exchangeRate = BigDecimal.ONE;

                FieldProviderFactory.setField(data[i], "invoicedAmount",
                    invgrandTotalAmount.divide(exchangeRate, 2, RoundingMode.HALF_UP).toString());
                FieldProviderFactory.setField(data[i], "expectedAmount",
                    psexpectedAmount.divide(exchangeRate, 2, RoundingMode.HALF_UP).toString());
                FieldProviderFactory.setField(data[i], "outstandingAmount",
                    psdoutstandingAmount.divide(exchangeRate, 2, RoundingMode.HALF_UP).toString());
                System.out.println(
                    "Showbi: " + psdoutstandingAmount.divide(exchangeRate, 2, RoundingMode.HALF_UP)
                        + " " + exchangeRate);

                FieldProviderFactory.setField(data[i], "recordInvAmt",
                    invgrandTotalAmount.divide(exchangeRate, 2, RoundingMode.HALF_UP).toString());
              }
            }

            // Truncate Business Partner
            String businessPartner = FIN_PaymentScheduleDetails[i].getInvoicePaymentSchedule()
                .getInvoice().getBusinessPartner().getIdentifier();
            FieldProviderFactory.setField(data[i], "businessPartnerId",
                FIN_PaymentScheduleDetails[i].getInvoicePaymentSchedule().getInvoice()
                    .getBusinessPartner().getId());
            String truncateBusinessPartner = (businessPartner.length() > 40)
                ? businessPartner.substring(0, 37).concat("...").toString()
                : businessPartner;
            FieldProviderFactory.setField(data[i], "businessPartnerName",
                (businessPartner.length() > 40) ? businessPartner : "");
            FieldProviderFactory.setField(data[i], "businessPartnerNameTrunc",
                truncateBusinessPartner);

            // Truncate Payment Method
            String paymentMethodName = FIN_PaymentScheduleDetails[i].getInvoicePaymentSchedule()
                .getFinPaymentmethod().getName();
            String truncatePaymentMethodName = (paymentMethodName.length() > 18)
                ? paymentMethodName.substring(0, 15).concat("...").toString()
                : paymentMethodName;
            FieldProviderFactory.setField(data[i], "paymentMethodName",
                (paymentMethodName.length() > 18) ? paymentMethodName : "");
            FieldProviderFactory.setField(data[i], "paymentMethodNameTrunc",
                truncatePaymentMethodName);

            if (FIN_PaymentScheduleDetails[i].getInvoicePaymentSchedule()
                .getFINPaymentPriority() != null) {
              FieldProviderFactory.setField(data[i], "gridLineColor", FIN_PaymentScheduleDetails[i]
                  .getInvoicePaymentSchedule().getFINPaymentPriority().getColor());
            }
          } else {
            FieldProviderFactory.setField(data[i], "expectedDate",
                dateFormater
                    .format(
                        FIN_PaymentScheduleDetails[i].getOrderPaymentSchedule().getExpectedDate())
                    .toString());
            FieldProviderFactory.setField(data[i], "dueDate",
                dateFormater
                    .format(FIN_PaymentScheduleDetails[i].getOrderPaymentSchedule().getDueDate())
                    .toString());
            FieldProviderFactory.setField(data[i], "transactionDate", dateFormater.format(
                FIN_PaymentScheduleDetails[i].getOrderPaymentSchedule().getOrder().getOrderDate())
                .toString());

            FieldProviderFactory.setField(data[i], "invoicedSourceAmount", "");
            FieldProviderFactory.setField(data[i], "invoicedAmount", "");

            Currency currInvoice = FIN_PaymentScheduleDetails[i].getOrderPaymentSchedule()
                .getCurrency();

            BigDecimal ordexpectedAmount = FIN_PaymentScheduleDetails[i].getOrderPaymentSchedule()
                .getAmount();
            if (docissotrx != isReceipt) {
              ordexpectedAmount = ordexpectedAmount.negate();
            }

            if (!isMulticurrency) {

              FieldProviderFactory.setField(data[i], "expectedAmount",
                  ordexpectedAmount.toString());

            } else {

              if (currInvoice.getId().equals(finaccCurrency.getId())) {
                FieldProviderFactory.setField(data[i], "expectedAmount",
                    ordexpectedAmount.toString());

              } else {

                if (exchangeRate.compareTo(BigDecimal.ZERO) == 0)
                  exchangeRate = BigDecimal.ONE;

                FieldProviderFactory.setField(data[i], "expectedAmount",
                    ordexpectedAmount.divide(exchangeRate, 2, RoundingMode.HALF_UP).toString());
              }
            }

            FieldProviderFactory.setField(data[i], "recordInvAmt", "0.00");

            // Truncate Business Partner
            String businessPartner = FIN_PaymentScheduleDetails[i].getOrderPaymentSchedule()
                .getOrder().getBusinessPartner().getIdentifier();
            FieldProviderFactory.setField(data[i], "businessPartnerId",
                FIN_PaymentScheduleDetails[i].getOrderPaymentSchedule().getOrder()
                    .getBusinessPartner().getId());
            String truncateBusinessPartner = (businessPartner.length() > 18)
                ? businessPartner.substring(0, 15).concat("...").toString()
                : businessPartner;
            FieldProviderFactory.setField(data[i], "businessPartnerName",
                (businessPartner.length() > 18) ? businessPartner : "");
            FieldProviderFactory.setField(data[i], "businessPartnerNameTrunc",
                truncateBusinessPartner);

            // Truncate Payment Method
            String paymentMethodName = FIN_PaymentScheduleDetails[i].getOrderPaymentSchedule()
                .getFinPaymentmethod().getName();
            String truncatePaymentMethodName = (paymentMethodName.length() > 18)
                ? paymentMethodName.substring(0, 15).concat("...").toString()
                : paymentMethodName;
            FieldProviderFactory.setField(data[i], "paymentMethodName",
                (paymentMethodName.length() > 18) ? paymentMethodName : "");
            FieldProviderFactory.setField(data[i], "paymentMethodNameTrunc",
                truncatePaymentMethodName);

            if (FIN_PaymentScheduleDetails[i].getOrderPaymentSchedule()
                .getFINPaymentPriority() != null) {
              FieldProviderFactory.setField(data[i], "gridLineColor", FIN_PaymentScheduleDetails[i]
                  .getOrderPaymentSchedule().getFINPaymentPriority().getColor());
            }
          }

          BigDecimal doubtfulDebtamount = FIN_PaymentScheduleDetails[i].getDoubtfulDebtAmount();
          if (docissotrx != isReceipt) {
            doubtfulDebtamount = doubtfulDebtamount.negate();
          }

          FieldProviderFactory.setField(data[i], "doubtfulDebtAmount",
              doubtfulDebtamount.toString());
          FieldProviderFactory.setField(data[i], "displayDoubtfulDebt",
              showDoubtfulDebtAmount ? "" : "display: none;");

          String strPaymentAmt = "";
          String strDifference = "";
          if (firstLoad && (selectedScheduledPaymentDetails.contains(FIN_PaymentScheduleDetails[i]))
              && paymentProposal != null) {
            strPaymentAmt = dao.getPaymentProposalDetailAmount(FIN_PaymentScheduleDetails[i],
                paymentProposal);
          } else {
            strPaymentAmt = vars.getNumericParameter(
                "inpPaymentAmount" + FIN_PaymentScheduleDetails[i].getId(), "");
          }
          if (!"".equals(strPaymentAmt)) {
            strDifference = FIN_PaymentScheduleDetails[i].getAmount()
                .subtract(new BigDecimal(strPaymentAmt)).toString();
          }
          if (data[i].getField("paymentAmount") == null
              || "".equals(data[i].getField("paymentAmount"))) {
            FieldProviderFactory.setField(data[i], "paymentAmount", strPaymentAmt);
          }

          String detret = vars.getNumericParameter(
              "inpInvoiceDetRetAmount" + FIN_PaymentScheduleDetails[i].getId(), "");
          FieldProviderFactory.setField(data[i], "invoiceDetRetAmount", detret);
          System.out.println("A Retencion:" + detret + " " + selectedId);

          if (detret == null || detret.equals(""))
            FieldProviderFactory.setField(data[i], "invoiceDetRetAmount", "0.00");

          String checkedRet = vars
              .getNumericParameter("inpRetChecked" + FIN_PaymentScheduleDetails[i].getId(), "");

          if (checkedRet.equals("1")) {
            FieldProviderFactory.setField(data[i], "fieldSelectedPaymentDetailIdRet", selectedId);
            FieldProviderFactory.setField(data[i], "retChecked", "1");
          }

          // Physical Invoice Number and Invoice Detraction Perc
          if (FIN_PaymentScheduleDetails[i].getInvoicePaymentSchedule() != null) {

            Invoice inv = FIN_PaymentScheduleDetails[i].getInvoicePaymentSchedule().getInvoice();
            if (inv != null) {
              if (inv.getScrPhysicalDocumentno() != null) {
                FieldProviderFactory.setField(data[i], "invoicePhysicalDocNr",
                    FIN_PaymentScheduleDetails[i].getInvoicePaymentSchedule().getInvoice()
                        .getScrPhysicalDocumentno());
                FieldProviderFactory.setField(data[i], "invoicePhysicalDocNrTrunc",
                    FIN_PaymentScheduleDetails[i].getInvoicePaymentSchedule().getInvoice()
                        .getScrPhysicalDocumentno());
              } else {
                FieldProviderFactory.setField(data[i], "invoicePhysicalDocNr", "");
                FieldProviderFactory.setField(data[i], "invoicePhysicalDocNrTrunc", "");
              }

              if (inv.isScoIsdetractionAffected() != null
                  && inv.isScoIsdetractionAffected() == true) {
                BigDecimal detractionperc = inv.getScoDetractionPercent();
                if (detractionperc != null) {
                  FieldProviderFactory.setField(data[i], "invoiceDetractionPercent",
                      detractionperc.toString());
                } else {
                  FieldProviderFactory.setField(data[i], "invoiceDetractionPercent", "0.00");
                }
              } else {
                FieldProviderFactory.setField(data[i], "invoiceDetractionPercent", "0.00");
              }

              if (inv.getDocumentType() != null) {
                Language lang = OBContext.getOBContext().getLanguage();// OBDal.getInstance().get(Language.class,
                // vars.getLanguage());
                String docTypeName = FIN_PaymentScheduleDetails[i].getInvoicePaymentSchedule()
                    .getInvoice().getDocumentType().get(DocumentType.PROPERTY_NAME, lang)
                    .toString();
                String truncateDocTypeName = (docTypeName.length() > 18)
                    ? docTypeName.substring(0, 15).concat("...").toString()
                    : docTypeName;

                FieldProviderFactory.setField(data[i], "invoiceDocTypeName", docTypeName);
                FieldProviderFactory.setField(data[i], "invoiceDocTypeNameTrunc",
                    truncateDocTypeName);
              } else {
                FieldProviderFactory.setField(data[i], "invoiceDocTypeName", "");
                FieldProviderFactory.setField(data[i], "invoiceDocTypeNameTrunc", "");
              }

            }

          }

          FieldProviderFactory.setField(data[i], "description", "");
          FieldProviderFactory.setField(data[i], "difference", strDifference);
          FieldProviderFactory.setField(data[i], "rownum", String.valueOf(i));
          FieldProviderFactory.setField(data[i], "disablePaymentAmountEdit", "N");

        } else if (FIN_PaymentScheduleDetails[i].getScoPayoutprepayment() != null) {// pago apertura
                                                                                    // anticipo

          boolean docissotrx = false;
          docissotrx = FIN_PaymentScheduleDetails[i].getScoPayoutprepayment().isSalesTransaction();

          String selectedId = (selectedScheduledPaymentDetails
              .contains(FIN_PaymentScheduleDetails[i])) ? FIN_PaymentScheduleDetails[i].getId()
                  : "";

          String selectedRecord = FIN_PaymentScheduleDetails[i].getId();
          String psdAmount = vars.getNumericParameter("inpPaymentAmount" + selectedRecord, "");

          String actualCurr = FIN_PaymentScheduleDetails[i].getScoPayoutprepayment().getCurrency()
              .getId();
          String currPayment = finaccCurrency.getId();

          if (actualCurr.equals(currPayment))
            FieldProviderFactory.setField(data[i], "currencyFlag", "1");
          else
            FieldProviderFactory.setField(data[i], "currencyFlag", "0");
          // BI-MONEDA
          if (actualCurr.equals("100"))// dolar
            FieldProviderFactory.setField(data[i], "currencySymbol", "$");
          if (actualCurr.equals("102"))// euros
            FieldProviderFactory.setField(data[i], "currencySymbol", "EUR");
          if (actualCurr.equals("308"))// soles
            FieldProviderFactory.setField(data[i], "currencySymbol", "S/.");

          FieldProviderFactory.setField(data[i], "finSelectedPaymentDetailId", selectedId);
          FieldProviderFactory.setField(data[i], "paymentAmount", psdAmount);

          FieldProviderFactory.setField(data[i], "finScheduledPaymentDetailId",
              FIN_PaymentScheduleDetails[i].getId());

          FieldProviderFactory.setField(data[i], "orderNr",
              FIN_PaymentScheduleDetails[i].getScoPayoutprepayment().getDocumentNo());
          FieldProviderFactory.setField(data[i], "orderNrTrunc",
              FIN_PaymentScheduleDetails[i].getScoPayoutprepayment().getDocumentNo());
          FieldProviderFactory.setField(data[i], "orderPaymentScheduleId", "");

          FieldProviderFactory.setField(data[i], "invoicePhysicalDocNr",
              FIN_PaymentScheduleDetails[i].getScoPayoutprepayment().getDocumentNo());
          FieldProviderFactory.setField(data[i], "invoicePhysicalDocNrTrunc",
              FIN_PaymentScheduleDetails[i].getScoPayoutprepayment().getDocumentNo());
          FieldProviderFactory.setField(data[i], "invoicePaymentScheduleId", "");

          FieldProviderFactory.setField(data[i], "transactionDate", dateFormater
              .format(FIN_PaymentScheduleDetails[i].getScoPayoutprepayment().getGeneratedDate()));

          BigDecimal leftAmount = FIN_PaymentScheduleDetails[i].getScoPayoutprepayment().getAmount()
              .subtract(FIN_PaymentScheduleDetails[i].getScoPayoutprepayment().getTotalPaid());

          if (docissotrx != isReceipt) {
            leftAmount = leftAmount.negate();
          }

          BigDecimal amount = FIN_PaymentScheduleDetails[i].getScoPayoutprepayment().getAmount();

          if (docissotrx != isReceipt) {
            amount = amount.negate();
          }

          Currency currInvoice = FIN_PaymentScheduleDetails[i].getScoPayoutprepayment()
              .getCurrency();
          DecimalFormat df = new DecimalFormat("#.00");
          FieldProviderFactory.setField(data[i], "invoicedSourceAmount",
              currInvoice.getSymbol() + df.format(amount));

          SCOPrepayment preypay = FIN_PaymentScheduleDetails[i].getScoPayoutprepayment();
          boolean isMulti = false;
          if (!isMulticurrency) {

            FieldProviderFactory.setField(data[i], "invoicedAmount", amount.toString());
            FieldProviderFactory.setField(data[i], "expectedAmount", leftAmount.toString());
            FieldProviderFactory.setField(data[i], "outstandingAmount", leftAmount.toString());

          } else {

            if (currInvoice.getId().equals(finaccCurrency.getId())) {
              FieldProviderFactory.setField(data[i], "invoicedAmount", amount.toString());
              FieldProviderFactory.setField(data[i], "expectedAmount", leftAmount.toString());
              FieldProviderFactory.setField(data[i], "outstandingAmount", leftAmount.toString());

            } else {
              isMulti = true;
              if (exchangeRate.compareTo(BigDecimal.ZERO) == 0)
                exchangeRate = BigDecimal.ONE;

              FieldProviderFactory.setField(data[i], "invoicedAmount",
                  amount.divide(exchangeRate, 2, RoundingMode.HALF_UP).toString());
              FieldProviderFactory.setField(data[i], "expectedAmount",
                  leftAmount.divide(exchangeRate, 2, RoundingMode.HALF_UP).toString());
              FieldProviderFactory.setField(data[i], "outstandingAmount",
                  leftAmount.divide(exchangeRate, 2, RoundingMode.HALF_UP).toString());

            }
          }

          FieldProviderFactory.setField(data[i], "recordPrin", "0.00");
          FieldProviderFactory.setField(data[i], "recordRet", "0.00");
          FieldProviderFactory.setField(data[i], "recordInvAmt", "0.00");

          // Truncate Business Partner
          String businessPartner = FIN_PaymentScheduleDetails[i].getScoPayoutprepayment()
              .getBusinessPartner().getIdentifier();
          FieldProviderFactory.setField(data[i], "businessPartnerId",
              FIN_PaymentScheduleDetails[i].getScoPayoutprepayment().getBusinessPartner().getId());
          String truncateBusinessPartner = (businessPartner.length() > 18)
              ? businessPartner.substring(0, 15).concat("...").toString()
              : businessPartner;
          FieldProviderFactory.setField(data[i], "businessPartnerName",
              (businessPartner.length() > 18) ? businessPartner : "");
          FieldProviderFactory.setField(data[i], "businessPartnerNameTrunc",
              truncateBusinessPartner);

          // Truncate Payment Method
          FieldProviderFactory.setField(data[i], "paymentMethodName", "");
          FieldProviderFactory.setField(data[i], "paymentMethodNameTrunc", "");

          FieldProviderFactory.setField(data[i], "doubtfulDebtAmount", "0.00".toString());
          FieldProviderFactory.setField(data[i], "displayDoubtfulDebt", "display: none;");

          String strPaymentAmt = "";
          String strDifference = "";

          if (!"".equals(psdAmount)) {
            strDifference = FIN_PaymentScheduleDetails[i].getAmount()
                .subtract(new BigDecimal(psdAmount)).toString();
          }

          FieldProviderFactory.setField(data[i], "description", "");
          FieldProviderFactory.setField(data[i], "difference", strDifference);
          FieldProviderFactory.setField(data[i], "rownum", String.valueOf(i));

          String detret = vars.getNumericParameter(
              "inpInvoiceDetRetAmount" + FIN_PaymentScheduleDetails[i].getId(), "");
          FieldProviderFactory.setField(data[i], "invoiceDetRetAmount", "0.00");

          String checkedRet = vars
              .getNumericParameter("inpRetChecked" + FIN_PaymentScheduleDetails[i].getId(), "");

          if (checkedRet.equals("1")) {
            FieldProviderFactory.setField(data[i], "fieldSelectedPaymentDetailIdRet", selectedId);
            FieldProviderFactory.setField(data[i], "retChecked", "1");
          }
          FieldProviderFactory.setField(data[i], "disablePaymentAmountEdit", "Y");

        } else if (FIN_PaymentScheduleDetails[i].getScoPrepayment() != null) {// aplicacion anticipo

          boolean docissotrx = false;
          docissotrx = FIN_PaymentScheduleDetails[i].getScoPrepayment().isSalesTransaction();

          String selectedId = (selectedScheduledPaymentDetails
              .contains(FIN_PaymentScheduleDetails[i])) ? FIN_PaymentScheduleDetails[i].getId()
                  : "";

          String selectedRecord = FIN_PaymentScheduleDetails[i].getId();
          String psdAmount = vars.getNumericParameter("inpPaymentAmount" + selectedRecord, "");

          String actualCurr = FIN_PaymentScheduleDetails[i].getScoPrepayment().getCurrency()
              .getId();
          String currPayment = finaccCurrency.getId();

          if (actualCurr.equals(currPayment))
            FieldProviderFactory.setField(data[i], "currencyFlag", "1");
          else
            FieldProviderFactory.setField(data[i], "currencyFlag", "0");
          // BI-MONEDA
          if (actualCurr.equals("100"))// dolar
            FieldProviderFactory.setField(data[i], "currencySymbol", "$");
          if (actualCurr.equals("102"))// euros
            FieldProviderFactory.setField(data[i], "currencySymbol", "EUR");
          if (actualCurr.equals("308"))// soles
            FieldProviderFactory.setField(data[i], "currencySymbol", "S/.");

          FieldProviderFactory.setField(data[i], "finSelectedPaymentDetailId", selectedId);
          FieldProviderFactory.setField(data[i], "paymentAmount", psdAmount);

          FieldProviderFactory.setField(data[i], "finScheduledPaymentDetailId",
              FIN_PaymentScheduleDetails[i].getId());

          FieldProviderFactory.setField(data[i], "orderNr",
              FIN_PaymentScheduleDetails[i].getScoPrepayment().getDocumentNo());
          FieldProviderFactory.setField(data[i], "orderNrTrunc",
              FIN_PaymentScheduleDetails[i].getScoPrepayment().getDocumentNo());
          FieldProviderFactory.setField(data[i], "orderPaymentScheduleId", "");

          FieldProviderFactory.setField(data[i], "invoicePhysicalDocNr",
              FIN_PaymentScheduleDetails[i].getScoPrepayment().getDocumentNo());
          FieldProviderFactory.setField(data[i], "invoicePhysicalDocNrTrunc",
              FIN_PaymentScheduleDetails[i].getScoPrepayment().getDocumentNo());
          FieldProviderFactory.setField(data[i], "invoicePaymentScheduleId", "");

          FieldProviderFactory.setField(data[i], "transactionDate", dateFormater
              .format(FIN_PaymentScheduleDetails[i].getScoPrepayment().getGeneratedDate()));

          BigDecimal leftAmount = FIN_PaymentScheduleDetails[i].getScoPrepayment().getAmount()
              .subtract(FIN_PaymentScheduleDetails[i].getScoPrepayment().getUsedamount());

          if (docissotrx != isReceipt) {
            leftAmount = leftAmount.negate();
          }
          BigDecimal amount = FIN_PaymentScheduleDetails[i].getScoPrepayment().getAmount();

          if (docissotrx != isReceipt) {
            amount = amount.negate();
          }

          Currency currInvoice = FIN_PaymentScheduleDetails[i].getScoPrepayment().getCurrency();
          DecimalFormat df = new DecimalFormat("#.00");
          FieldProviderFactory.setField(data[i], "invoicedSourceAmount",
              currInvoice.getSymbol() + df.format(amount.negate()));

          SCOPrepayment preypay = FIN_PaymentScheduleDetails[i].getScoPrepayment();
          boolean isMulti = false;
          if (!isMulticurrency) {

            FieldProviderFactory.setField(data[i], "invoicedAmount", amount.negate().toString());
            FieldProviderFactory.setField(data[i], "expectedAmount",
                leftAmount.negate().toString());
            FieldProviderFactory.setField(data[i], "outstandingAmount",
                leftAmount.negate().toString());

          } else {

            if (currInvoice.getId().equals(finaccCurrency.getId())) {
              FieldProviderFactory.setField(data[i], "invoicedAmount", amount.negate().toString());
              FieldProviderFactory.setField(data[i], "expectedAmount",
                  leftAmount.negate().toString());
              FieldProviderFactory.setField(data[i], "outstandingAmount",
                  leftAmount.negate().toString());

            } else {
              isMulti = true;
              if (exchangeRate.compareTo(BigDecimal.ZERO) == 0)
                exchangeRate = BigDecimal.ONE;

              FieldProviderFactory.setField(data[i], "invoicedAmount",
                  amount.negate().divide(exchangeRate, 2, RoundingMode.HALF_UP).toString());
              FieldProviderFactory.setField(data[i], "expectedAmount",
                  leftAmount.negate().divide(exchangeRate, 2, RoundingMode.HALF_UP).toString());
              FieldProviderFactory.setField(data[i], "outstandingAmount",
                  leftAmount.negate().divide(exchangeRate, 2, RoundingMode.HALF_UP).toString());

            }
          }

          FieldProviderFactory.setField(data[i], "recordPrin", "0.00");
          FieldProviderFactory.setField(data[i], "recordRet", "0.00");
          FieldProviderFactory.setField(data[i], "recordInvAmt", "0.00");

          // Truncate Business Partner
          String businessPartner = FIN_PaymentScheduleDetails[i].getScoPrepayment()
              .getBusinessPartner().getIdentifier();
          FieldProviderFactory.setField(data[i], "businessPartnerId",
              FIN_PaymentScheduleDetails[i].getScoPrepayment().getBusinessPartner().getId());
          String truncateBusinessPartner = (businessPartner.length() > 18)
              ? businessPartner.substring(0, 15).concat("...").toString()
              : businessPartner;
          FieldProviderFactory.setField(data[i], "businessPartnerName",
              (businessPartner.length() > 18) ? businessPartner : "");
          FieldProviderFactory.setField(data[i], "businessPartnerNameTrunc",
              truncateBusinessPartner);

          // Truncate Payment Method
          FieldProviderFactory.setField(data[i], "paymentMethodName", "");
          FieldProviderFactory.setField(data[i], "paymentMethodNameTrunc", "");

          FieldProviderFactory.setField(data[i], "doubtfulDebtAmount", "0.00".toString());
          FieldProviderFactory.setField(data[i], "displayDoubtfulDebt", "display: none;");

          String strPaymentAmt = "";
          String strDifference = "";

          if (!"".equals(psdAmount)) {
            strDifference = FIN_PaymentScheduleDetails[i].getAmount()
                .subtract(new BigDecimal(psdAmount)).toString();
          }

          FieldProviderFactory.setField(data[i], "description", "");
          FieldProviderFactory.setField(data[i], "difference", strDifference);
          FieldProviderFactory.setField(data[i], "rownum", String.valueOf(i));

          String detret = vars.getNumericParameter(
              "inpInvoiceDetRetAmount" + FIN_PaymentScheduleDetails[i].getId(), "");
          FieldProviderFactory.setField(data[i], "invoiceDetRetAmount", detret);
          System.out.println("Ant Retencion:" + detret + " " + selectedId);

          if (detret == null || detret.equals(""))
            FieldProviderFactory.setField(data[i], "invoiceDetRetAmount", "0.00");

          String checkedRet = vars
              .getNumericParameter("inpRetChecked" + FIN_PaymentScheduleDetails[i].getId(), "");

          if (checkedRet.equals("1")) {
            FieldProviderFactory.setField(data[i], "fieldSelectedPaymentDetailIdRet", selectedId);
            FieldProviderFactory.setField(data[i], "retChecked", "1");
          }

          FieldProviderFactory.setField(data[i], "disablePaymentAmountEdit", "N");

        } else if (FIN_PaymentScheduleDetails[i].getScoPayoutrendcuentas() != null) { // Pago
                                                                                      // apertura de
                                                                                      // Entrega a
                                                                                      // rendir

          String selectedId = (selectedScheduledPaymentDetails
              .contains(FIN_PaymentScheduleDetails[i])) ? FIN_PaymentScheduleDetails[i].getId()
                  : "";

          String selectedRecord = FIN_PaymentScheduleDetails[i].getId();
          String psdAmount = vars.getNumericParameter("inpPaymentAmount" + selectedRecord, "");

          String actualCurr = FIN_PaymentScheduleDetails[i].getScoPayoutrendcuentas().getCurrency()
              .getId();
          String currPayment = finaccCurrency.getId();

          if (actualCurr.equals(currPayment))
            FieldProviderFactory.setField(data[i], "currencyFlag", "1");
          else
            FieldProviderFactory.setField(data[i], "currencyFlag", "0");
          // BI-MONEDA
          if (actualCurr.equals("100"))// dolar
            FieldProviderFactory.setField(data[i], "currencySymbol", "$");
          if (actualCurr.equals("102"))// euros
            FieldProviderFactory.setField(data[i], "currencySymbol", "EUR");
          if (actualCurr.equals("308"))// soles
            FieldProviderFactory.setField(data[i], "currencySymbol", "S/.");

          FieldProviderFactory.setField(data[i], "finSelectedPaymentDetailId", selectedId);
          FieldProviderFactory.setField(data[i], "paymentAmount", psdAmount);

          FieldProviderFactory.setField(data[i], "finScheduledPaymentDetailId",
              FIN_PaymentScheduleDetails[i].getId());

          FieldProviderFactory.setField(data[i], "orderNr",
              FIN_PaymentScheduleDetails[i].getScoPayoutrendcuentas().getDocumentNo());
          FieldProviderFactory.setField(data[i], "orderNrTrunc",
              FIN_PaymentScheduleDetails[i].getScoPayoutrendcuentas().getDocumentNo());
          FieldProviderFactory.setField(data[i], "orderPaymentScheduleId", "");

          FieldProviderFactory.setField(data[i], "invoicePhysicalDocNr",
              FIN_PaymentScheduleDetails[i].getScoPayoutrendcuentas().getDocumentNo());
          FieldProviderFactory.setField(data[i], "invoicePhysicalDocNrTrunc",
              FIN_PaymentScheduleDetails[i].getScoPayoutrendcuentas().getDocumentNo());
          FieldProviderFactory.setField(data[i], "invoicePaymentScheduleId", "");

          FieldProviderFactory.setField(data[i], "transactionDate", dateFormater
              .format(FIN_PaymentScheduleDetails[i].getScoPayoutrendcuentas().getCreationDate()));

          BigDecimal leftAmount = FIN_PaymentScheduleDetails[i].getScoPayoutrendcuentas()
              .getAmount()
              .subtract(FIN_PaymentScheduleDetails[i].getScoPayoutrendcuentas().getTotalPaid());

          Currency currInvoice = FIN_PaymentScheduleDetails[i].getScoPayoutrendcuentas()
              .getCurrency();
          DecimalFormat df = new DecimalFormat("#.00");
          FieldProviderFactory.setField(data[i], "invoicedSourceAmount", currInvoice.getSymbol()
              + df.format(FIN_PaymentScheduleDetails[i].getScoPayoutrendcuentas().getAmount()));

          boolean isMulti = false;
          if (!isMulticurrency) {

            FieldProviderFactory.setField(data[i], "invoicedAmount",
                FIN_PaymentScheduleDetails[i].getScoPayoutrendcuentas().getAmount().toString());
            FieldProviderFactory.setField(data[i], "expectedAmount", leftAmount.toString());
            FieldProviderFactory.setField(data[i], "outstandingAmount", leftAmount.toString());

          } else {

            if (currInvoice.getId().equals(finaccCurrency.getId())) {
              FieldProviderFactory.setField(data[i], "invoicedAmount",
                  FIN_PaymentScheduleDetails[i].getScoPayoutrendcuentas().getAmount().toString());
              FieldProviderFactory.setField(data[i], "expectedAmount", leftAmount.toString());
              FieldProviderFactory.setField(data[i], "outstandingAmount", leftAmount.toString());

            } else {
              isMulti = true;
              if (exchangeRate.compareTo(BigDecimal.ZERO) == 0)
                exchangeRate = BigDecimal.ONE;

              FieldProviderFactory.setField(data[i], "invoicedAmount",
                  FIN_PaymentScheduleDetails[i].getScoPayoutrendcuentas().getAmount()
                      .divide(exchangeRate, 2, RoundingMode.HALF_UP).toString());
              FieldProviderFactory.setField(data[i], "expectedAmount",
                  leftAmount.divide(exchangeRate, 2, RoundingMode.HALF_UP).toString());
              FieldProviderFactory.setField(data[i], "outstandingAmount",
                  leftAmount.divide(exchangeRate, 2, RoundingMode.HALF_UP).toString());

            }
          }

          FieldProviderFactory.setField(data[i], "recordPrin", "0.00");
          FieldProviderFactory.setField(data[i], "recordRet", "0.00");
          FieldProviderFactory.setField(data[i], "recordInvAmt", "0.00");

          // Truncate Business Partner
          String businessPartner = FIN_PaymentScheduleDetails[i].getScoPayoutrendcuentas()
              .getBusinessPartner().getIdentifier();
          FieldProviderFactory.setField(data[i], "businessPartnerId",
              FIN_PaymentScheduleDetails[i].getScoPayoutrendcuentas().getBusinessPartner().getId());
          String truncateBusinessPartner = (businessPartner.length() > 18)
              ? businessPartner.substring(0, 15).concat("...").toString()
              : businessPartner;
          FieldProviderFactory.setField(data[i], "businessPartnerName",
              (businessPartner.length() > 18) ? businessPartner : "");
          FieldProviderFactory.setField(data[i], "businessPartnerNameTrunc",
              truncateBusinessPartner);

          // Truncate Payment Method
          FieldProviderFactory.setField(data[i], "paymentMethodName", "");
          FieldProviderFactory.setField(data[i], "paymentMethodNameTrunc", "");

          FieldProviderFactory.setField(data[i], "doubtfulDebtAmount", "0.00".toString());
          FieldProviderFactory.setField(data[i], "displayDoubtfulDebt", "display: none;");

          String strPaymentAmt = "";
          String strDifference = "";

          if (!"".equals(psdAmount)) {
            strDifference = FIN_PaymentScheduleDetails[i].getAmount()
                .subtract(new BigDecimal(psdAmount)).toString();
          }

          FieldProviderFactory.setField(data[i], "description", "");
          FieldProviderFactory.setField(data[i], "difference", strDifference);
          FieldProviderFactory.setField(data[i], "rownum", String.valueOf(i));

          String detret = vars.getNumericParameter(
              "inpInvoiceDetRetAmount" + FIN_PaymentScheduleDetails[i].getId(), "");
          FieldProviderFactory.setField(data[i], "invoiceDetRetAmount", detret);
          System.out.println("Ant Retencion:" + detret + " " + selectedId);

          if (detret == null || detret.equals(""))
            FieldProviderFactory.setField(data[i], "invoiceDetRetAmount", "0.00");

          String checkedRet = vars
              .getNumericParameter("inpRetChecked" + FIN_PaymentScheduleDetails[i].getId(), "");

          if (checkedRet.equals("1")) {
            FieldProviderFactory.setField(data[i], "fieldSelectedPaymentDetailIdRet", selectedId);
            FieldProviderFactory.setField(data[i], "retChecked", "1");
          }

          FieldProviderFactory.setField(data[i], "disablePaymentAmountEdit", "Y");

        } else if (FIN_PaymentScheduleDetails[i].getSCOEntregaARendir() != null) { // Aplicacion de
                                                                                   // Entrega a
                                                                                   // rendir

          String selectedId = (selectedScheduledPaymentDetails
              .contains(FIN_PaymentScheduleDetails[i])) ? FIN_PaymentScheduleDetails[i].getId()
                  : "";

          String selectedRecord = FIN_PaymentScheduleDetails[i].getId();
          String psdAmount = vars.getNumericParameter("inpPaymentAmount" + selectedRecord, "");

          String actualCurr = FIN_PaymentScheduleDetails[i].getSCOEntregaARendir().getCurrency()
              .getId();
          String currPayment = finaccCurrency.getId();

          if (actualCurr.equals(currPayment))
            FieldProviderFactory.setField(data[i], "currencyFlag", "1");
          else
            FieldProviderFactory.setField(data[i], "currencyFlag", "0");
          // BI-MONEDA
          if (actualCurr.equals("100"))// dolar
            FieldProviderFactory.setField(data[i], "currencySymbol", "$");
          if (actualCurr.equals("102"))// euros
            FieldProviderFactory.setField(data[i], "currencySymbol", "EUR");
          if (actualCurr.equals("308"))// soles
            FieldProviderFactory.setField(data[i], "currencySymbol", "S/.");

          FieldProviderFactory.setField(data[i], "finSelectedPaymentDetailId", selectedId);
          FieldProviderFactory.setField(data[i], "paymentAmount", psdAmount);

          FieldProviderFactory.setField(data[i], "finScheduledPaymentDetailId",
              FIN_PaymentScheduleDetails[i].getId());

          FieldProviderFactory.setField(data[i], "orderNr",
              FIN_PaymentScheduleDetails[i].getSCOEntregaARendir().getDocumentNo());
          FieldProviderFactory.setField(data[i], "orderNrTrunc",
              FIN_PaymentScheduleDetails[i].getSCOEntregaARendir().getDocumentNo());
          FieldProviderFactory.setField(data[i], "orderPaymentScheduleId", "");

          FieldProviderFactory.setField(data[i], "invoicePhysicalDocNr",
              FIN_PaymentScheduleDetails[i].getSCOEntregaARendir().getDocumentNo());
          FieldProviderFactory.setField(data[i], "invoicePhysicalDocNrTrunc",
              FIN_PaymentScheduleDetails[i].getSCOEntregaARendir().getDocumentNo());
          FieldProviderFactory.setField(data[i], "invoicePaymentScheduleId", "");

          FieldProviderFactory.setField(data[i], "transactionDate", dateFormater
              .format(FIN_PaymentScheduleDetails[i].getSCOEntregaARendir().getCreationDate()));

          BigDecimal leftAmount = FIN_PaymentScheduleDetails[i].getSCOEntregaARendir().getRefund();

          Currency currInvoice = FIN_PaymentScheduleDetails[i].getSCOEntregaARendir().getCurrency();
          DecimalFormat df = new DecimalFormat("#.00");
          FieldProviderFactory.setField(data[i], "invoicedSourceAmount",
              currInvoice.getSymbol() + df.format(
                  FIN_PaymentScheduleDetails[i].getSCOEntregaARendir().getAmount().negate()));

          ScoRendicioncuentas preypay = FIN_PaymentScheduleDetails[i].getSCOEntregaARendir();
          boolean isMulti = false;
          if (!isMulticurrency) {

            FieldProviderFactory.setField(data[i], "invoicedAmount", FIN_PaymentScheduleDetails[i]
                .getSCOEntregaARendir().getAmount().negate().toString());
            FieldProviderFactory.setField(data[i], "expectedAmount",
                leftAmount.negate().toString());
            FieldProviderFactory.setField(data[i], "outstandingAmount",
                leftAmount.negate().toString());

          } else {

            if (currInvoice.getId().equals(finaccCurrency.getId())) {
              FieldProviderFactory.setField(data[i], "invoicedAmount", FIN_PaymentScheduleDetails[i]
                  .getSCOEntregaARendir().getAmount().negate().toString());
              FieldProviderFactory.setField(data[i], "expectedAmount",
                  leftAmount.negate().toString());
              FieldProviderFactory.setField(data[i], "outstandingAmount",
                  leftAmount.negate().toString());

            } else {
              isMulti = true;
              if (exchangeRate.compareTo(BigDecimal.ZERO) == 0)
                exchangeRate = BigDecimal.ONE;

              FieldProviderFactory.setField(data[i], "invoicedAmount",
                  FIN_PaymentScheduleDetails[i].getSCOEntregaARendir().getAmount().negate()
                      .divide(exchangeRate, 2, RoundingMode.HALF_UP).toString());
              FieldProviderFactory.setField(data[i], "expectedAmount",
                  leftAmount.negate().divide(exchangeRate, 2, RoundingMode.HALF_UP).toString());
              FieldProviderFactory.setField(data[i], "outstandingAmount",
                  leftAmount.negate().divide(exchangeRate, 2, RoundingMode.HALF_UP).toString());

            }
          }

          FieldProviderFactory.setField(data[i], "recordPrin", "0.00");
          FieldProviderFactory.setField(data[i], "recordRet", "0.00");
          FieldProviderFactory.setField(data[i], "recordInvAmt", "0.00");

          // Truncate Business Partner
          String businessPartner = FIN_PaymentScheduleDetails[i].getSCOEntregaARendir()
              .getBusinessPartner().getIdentifier();
          FieldProviderFactory.setField(data[i], "businessPartnerId",
              FIN_PaymentScheduleDetails[i].getSCOEntregaARendir().getBusinessPartner().getId());
          String truncateBusinessPartner = (businessPartner.length() > 18)
              ? businessPartner.substring(0, 15).concat("...").toString()
              : businessPartner;
          FieldProviderFactory.setField(data[i], "businessPartnerName",
              (businessPartner.length() > 18) ? businessPartner : "");
          FieldProviderFactory.setField(data[i], "businessPartnerNameTrunc",
              truncateBusinessPartner);

          // Truncate Payment Method
          FieldProviderFactory.setField(data[i], "paymentMethodName", "");
          FieldProviderFactory.setField(data[i], "paymentMethodNameTrunc", "");

          FieldProviderFactory.setField(data[i], "doubtfulDebtAmount", "0.00".toString());
          FieldProviderFactory.setField(data[i], "displayDoubtfulDebt", "display: none;");

          String strPaymentAmt = "";
          String strDifference = "";

          if (!"".equals(psdAmount)) {
            strDifference = FIN_PaymentScheduleDetails[i].getAmount()
                .subtract(new BigDecimal(psdAmount)).toString();
          }

          FieldProviderFactory.setField(data[i], "description", "");
          FieldProviderFactory.setField(data[i], "difference", strDifference);
          FieldProviderFactory.setField(data[i], "rownum", String.valueOf(i));

          String detret = vars.getNumericParameter(
              "inpInvoiceDetRetAmount" + FIN_PaymentScheduleDetails[i].getId(), "");
          FieldProviderFactory.setField(data[i], "invoiceDetRetAmount", detret);
          System.out.println("Ant Retencion:" + detret + " " + selectedId);

          if (detret == null || detret.equals(""))
            FieldProviderFactory.setField(data[i], "invoiceDetRetAmount", "0.00");

          String checkedRet = vars
              .getNumericParameter("inpRetChecked" + FIN_PaymentScheduleDetails[i].getId(), "");

          if (checkedRet.equals("1")) {
            FieldProviderFactory.setField(data[i], "fieldSelectedPaymentDetailIdRet", selectedId);
            FieldProviderFactory.setField(data[i], "retChecked", "1");
          }

          FieldProviderFactory.setField(data[i], "disablePaymentAmountEdit", "N");

        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return data;
  }

  /**
   * Returns a List of FIN_PaymentScheduleDetails related to the Proposal Details of the given
   * Payment Proposal.
   * 
   * @param paymentProposal
   */
  public static List<FIN_PaymentScheduleDetail> getSelectedPendingPaymentsFromProposal(
      FIN_PaymentProposal paymentProposal) {
    List<FIN_PaymentScheduleDetail> existingPaymentScheduleDetail = new ArrayList<FIN_PaymentScheduleDetail>();
    for (FIN_PaymentPropDetail proposalDetail : paymentProposal.getFINPaymentPropDetailList())
      existingPaymentScheduleDetail.add(proposalDetail.getFINPaymentScheduledetail());

    return existingPaymentScheduleDetail;
  }

  /**
   * This method groups several payment schedule details by {PaymentDetails, OrderPaymenSchedule,
   * InvoicePaymentSchedule}.
   * 
   * @param psd
   *          Payment Schedule Detail base. The amount will be updated here.
   */
  public static void mergePaymentScheduleDetails(FIN_PaymentScheduleDetail psd) {
    // FIXME: added to access the FIN_PaymentSchedule and
    // FIN_PaymentScheduleDetail tables to be
    // removed when new security implementation is done

    OBContext.setAdminMode();
    try {
      OBCriteria<FIN_PaymentScheduleDetail> psdFilter = OBDal.getInstance()
          .createCriteria(FIN_PaymentScheduleDetail.class);
      psdFilter.add(Restrictions.eq(FIN_PaymentScheduleDetail.PROPERTY_CLIENT, psd.getClient()));
      psdFilter.add(
          Restrictions.eq(FIN_PaymentScheduleDetail.PROPERTY_ORGANIZATION, psd.getOrganization()));
      psdFilter.add(Restrictions.isNull(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS));
      psdFilter.add(Restrictions.eq(FIN_PaymentScheduleDetail.PROPERTY_SCOEXTERNALPAYMENT, false));
      if (psd.getOrderPaymentSchedule() == null) {
        psdFilter.add(Restrictions.isNull(FIN_PaymentScheduleDetail.PROPERTY_ORDERPAYMENTSCHEDULE));
      } else {
        psdFilter.add(Restrictions.eq(FIN_PaymentScheduleDetail.PROPERTY_ORDERPAYMENTSCHEDULE,
            psd.getOrderPaymentSchedule()));
      }
      if (psd.getInvoicePaymentSchedule() == null) {
        psdFilter
            .add(Restrictions.isNull(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE));
      } else {
        psdFilter.add(Restrictions.eq(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE,
            psd.getInvoicePaymentSchedule()));
      }

      // Update amount and remove payment schedule detail
      final List<String> removedPDSIds = new ArrayList<String>();
      for (FIN_PaymentScheduleDetail psdToRemove : psdFilter.list()) {
        psd.setAmount(psd.getAmount().add(psdToRemove.getAmount()));
        psd.setDoubtfulDebtAmount(
            psd.getDoubtfulDebtAmount().add(psdToRemove.getDoubtfulDebtAmount()));
        // TODO: Set 0 as default value for writeoffamt column in
        // FIN_Payment_ScheduleDetail table
        BigDecimal sum1 = (psd.getWriteoffAmount() == null) ? BigDecimal.ZERO
            : psd.getWriteoffAmount();
        BigDecimal sum2 = (psdToRemove.getWriteoffAmount() == null) ? BigDecimal.ZERO
            : psdToRemove.getWriteoffAmount();
        psd.setWriteoffAmount(sum1.add(sum2));

        OBDal.getInstance().save(psdToRemove);
        removedPDSIds.add(psdToRemove.getId());
      }

      for (String pdToRm : removedPDSIds) {
        FIN_PaymentScheduleDetail psdToRemove = OBDal.getInstance()
            .get(FIN_PaymentScheduleDetail.class, pdToRm);
        if (psdToRemove.getInvoicePaymentSchedule() != null) {
          psdToRemove.getInvoicePaymentSchedule()
              .getFINPaymentScheduleDetailInvoicePaymentScheduleList().remove(psdToRemove);
          OBDal.getInstance().save(psdToRemove.getInvoicePaymentSchedule());
        }
        if (psdToRemove.getOrderPaymentSchedule() != null) {
          psdToRemove.getOrderPaymentSchedule()
              .getFINPaymentScheduleDetailOrderPaymentScheduleList().remove(psdToRemove);
          OBDal.getInstance().save(psdToRemove.getOrderPaymentSchedule());
        }
        OBDal.getInstance().remove(psdToRemove);
      }

      psd.setAmount(psd.getAmount()
          .add((psd.getWriteoffAmount() == null) ? BigDecimal.ZERO : psd.getWriteoffAmount()));
      psd.setWriteoffAmount(BigDecimal.ZERO);
      psd.setPaymentDetails(null);
      OBDal.getInstance().save(psd);
      OBDal.getInstance().flush();
      OBDal.getInstance().getSession().refresh(psd);

    } catch (Exception e) {
      throw new OBException(e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Update Payment Schedule amounts with the amount of the Payment Schedule Detail or Payment
   * Detail. Useful when paying orders
   * 
   * @param paymentSchedule
   *          Payment Schedule to be updated
   * @param amount
   *          Amount of the Payment Schedule Detail or Payment Detail
   * @param writeOffAmount
   *          Write off amount, null or 0 if not applicable.
   */
  public static void updatePaymentScheduleAmounts(FIN_PaymentSchedule paymentSchedule,
      BigDecimal amount, BigDecimal writeOffAmount, String docaction) {
    updatePaymentScheduleAmounts(null, paymentSchedule, amount, writeOffAmount, docaction);
  }

  /**
   * Update Payment Schedule amounts with the amount of the Payment Schedule Detail or Payment
   * Detail. Useful when paying invoices. It supports Invoices with Cash VAT, creating the records
   * into the Cash VAT management table (InvoiceTaxCashVAT)
   * 
   * @param paymentDetail
   *          payment
   * @param paymentSchedule
   *          Payment Schedule to be updated
   * @param amount
   *          Amount of the Payment Schedule Detail or Payment Detail
   * @param writeOffAmount
   *          Write off amount, null or 0 if not applicable.
   */

  public static void updatePaymentScheduleAmounts(FIN_PaymentDetail paymentDetail,
      FIN_PaymentSchedule paymentSchedule, BigDecimal amount, BigDecimal writeOffAmount,
      String docaction) {
    paymentSchedule.setPaidAmount(paymentSchedule.getPaidAmount().add(amount));
    paymentSchedule.setOutstandingAmount(paymentSchedule.getOutstandingAmount().subtract(amount));
    if (writeOffAmount != null && writeOffAmount.compareTo(BigDecimal.ZERO) != 0) {
      paymentSchedule.setPaidAmount(paymentSchedule.getPaidAmount().add(writeOffAmount));
      paymentSchedule
          .setOutstandingAmount(paymentSchedule.getOutstandingAmount().subtract(writeOffAmount));
    }
    OBDal.getInstance().save(paymentSchedule);
    // CashVATUtil.createInvoiceTaxCashVAT(paymentDetail, paymentSchedule,
    // amount.add(writeOffAmount));

    if (paymentSchedule.getInvoice() != null) {
      updateInvoicePaymentMonitor(paymentDetail, paymentSchedule, amount, writeOffAmount);
    }
  }

  /**
   * Method used to update the payment monitor based on the payment made by the user.
   * 
   * @param invoicePaymentSchedule
   * @param amount
   *          Amount of the transaction.
   * @param writeOffAmount
   *          Amount that has been wrote off.
   */
  private static void updateInvoicePaymentMonitor(FIN_PaymentDetail paymentDetail,
      FIN_PaymentSchedule invoicePaymentSchedule, BigDecimal amount, BigDecimal writeOffAmount) {
    Invoice invoice = invoicePaymentSchedule.getInvoice();
    Date dueDate = invoicePaymentSchedule.getDueDate();
    boolean isDueDateFlag = dueDate.compareTo(new Date()) <= 0;
    invoice.setTotalPaid(invoice.getTotalPaid().add(amount));
    invoice.setLastCalculatedOnDate(new Date());
    invoice.setOutstandingAmount(invoice.getOutstandingAmount().subtract(amount));
    if (isDueDateFlag)
      invoice.setDueAmount(invoice.getDueAmount().subtract(amount));
    if (writeOffAmount != null && writeOffAmount.compareTo(BigDecimal.ZERO) != 0) {
      invoice.setTotalPaid(invoice.getTotalPaid().add(writeOffAmount));
      invoice.setOutstandingAmount(invoice.getOutstandingAmount().subtract(writeOffAmount));
      if (isDueDateFlag)
        invoice.setDueAmount(invoice.getDueAmount().subtract(writeOffAmount));
    }

    if (0 == invoice.getOutstandingAmount().compareTo(BigDecimal.ZERO)) {
      Date finalSettlementDate = getFinalSettlementDate(invoice);
      // If date is null invoice amount = 0 then nothing to set
      if (finalSettlementDate != null) {
        invoice.setFinalSettlementDate(finalSettlementDate);
        invoice.setDaysSalesOutstanding(
            FIN_Utility.getDaysBetween(invoice.getInvoiceDate(), finalSettlementDate));
      }
      invoice.setPaymentComplete(true);
    } else
      invoice.setPaymentComplete(false);
    List<FIN_PaymentSchedule> paymentSchedList = invoice.getFINPaymentScheduleList();
    Date firstDueDate = null;
    for (FIN_PaymentSchedule paymentSchedule : paymentSchedList) {
      if (paymentSchedule.getOutstandingAmount().compareTo(BigDecimal.ZERO) > 0
          && (firstDueDate == null || firstDueDate.after(paymentSchedule.getDueDate())))
        firstDueDate = paymentSchedule.getDueDate();
    }

    BigDecimal overdueAmount = calculateOverdueAmount(invoicePaymentSchedule);
    invoice.setPercentageOverdue(overdueAmount.multiply(new BigDecimal("100"))
        .divide(invoice.getGrandTotalAmount(), 2, BigDecimal.ROUND_HALF_UP).longValue());

    if (firstDueDate != null)
      invoice.setDaysTillDue(FIN_Utility.getDaysToDue(firstDueDate));
    else
      invoice.setDaysTillDue(0L);

    if (paymentDetail != null) {
      // si es pago por detraccion actualizar invoice
      if (paymentDetail.getFinPayment().isScoDetractionpayment() != null
          && paymentDetail.getFinPayment().isScoDetractionpayment() == true) {
        if (invoice.getScoPaydetractamount() != null)
          invoice.setScoPaydetractamount(invoice.getScoPaydetractamount().add(amount));
        else
          invoice.setScoPaydetractamount(amount);

      }
    }
    OBDal.getInstance().save(invoice);
  }

  private static BigDecimal calculateOverdueAmount(FIN_PaymentSchedule invoicePaymentSchedule) {
    Invoice invoice = invoicePaymentSchedule.getInvoice();
    BigDecimal overdueOriginal = BigDecimal.ZERO;
    FIN_PaymentScheduleDetail currentPSD = getLastCreatedPaymentScheduleDetail(
        invoicePaymentSchedule);
    for (FIN_PaymentSchedule paymentSchedule : invoice.getFINPaymentScheduleList()) {
      Date paymentDueDate = paymentSchedule.getDueDate();
      for (FIN_PaymentScheduleDetail psd : paymentSchedule
          .getFINPaymentScheduleDetailInvoicePaymentScheduleList()) {
        if (!psd.isCanceled() && psd.getPaymentDetails() != null
            && (psd.isInvoicePaid() || currentPSD.getId().equals(psd.getId()))) {
          Date paymentDate = psd.getPaymentDetails().getFinPayment().getPaymentDate();
          if (paymentDate.after(paymentDueDate)) {
            overdueOriginal = overdueOriginal.add(psd.getAmount());
          }
        }
      }

    }
    return overdueOriginal;
  }

  private static FIN_PaymentScheduleDetail getLastCreatedPaymentScheduleDetail(
      FIN_PaymentSchedule invoicePaymentSchedule) {
    final OBCriteria<FIN_PaymentScheduleDetail> obc = OBDal.getInstance()
        .createCriteria(FIN_PaymentScheduleDetail.class);
    OBContext.setAdminMode();
    try {
      obc.add(Restrictions.eq(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE,
          invoicePaymentSchedule));
      obc.addOrderBy(FIN_PaymentScheduleDetail.PROPERTY_CREATIONDATE, false);
      obc.setMaxResults(1);
      return (FIN_PaymentScheduleDetail) obc.uniqueResult();
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  /**
   * Returns the date in which last payment for this invoice took place
   * 
   * @param invoice
   * @return
   */
  public static Date getFinalSettlementDate(Invoice invoice) {
    final OBCriteria<FIN_PaymentSchedInvV> obc = OBDal.getInstance()
        .createCriteria(FIN_PaymentSchedInvV.class);
    OBContext.setAdminMode();
    try {
      obc.add(Restrictions.eq(FIN_PaymentSchedInvV.PROPERTY_INVOICE, invoice));
      obc.setProjection(Projections.max(FIN_PaymentSchedInvV.PROPERTY_LASTPAYMENT));
      return (Date) obc.uniqueResult();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns true if a financial account transactions has to be automatically triggered after
   * payment is processed.
   * 
   * @param payment
   * @return Returns true if a financial account transactions has to be automatically triggered
   *         after payment is processed.
   */
  public static Boolean isForcedFinancialAccountTransaction(FIN_Payment payment) {
    OBCriteria<FinAccPaymentMethod> psdFilter = OBDal.getInstance()
        .createCriteria(FinAccPaymentMethod.class);
    psdFilter.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_ACCOUNT, payment.getAccount()));
    psdFilter.add(
        Restrictions.eq(FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD, payment.getPaymentMethod()));
    for (FinAccPaymentMethod paymentMethod : psdFilter.list()) {
      return payment.isReceipt() ? paymentMethod.isAutomaticDeposit()
          : paymentMethod.isAutomaticWithdrawn();
    }
    return false;
  }

  public static Boolean isForcedFinancialAccountTransaction(FIN_FinancialAccount finacc,
      FIN_PaymentMethod paymethod, boolean isreceipt) {
    OBCriteria<FinAccPaymentMethod> psdFilter = OBDal.getInstance()
        .createCriteria(FinAccPaymentMethod.class);
    psdFilter.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_ACCOUNT, finacc));
    psdFilter.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD, paymethod));
    for (FinAccPaymentMethod paymentMethod : psdFilter.list()) {
      return isreceipt ? paymentMethod.isAutomaticDeposit() : paymentMethod.isAutomaticWithdrawn();
    }
    return false;
  }

  /**
   * Method used to get a list of payments identifiers associated to a payment proposal
   * 
   * @param paymentProposal
   * @return List of payment identifiers
   */
  @SuppressWarnings("unchecked")
  public static List<String> getPaymentFromPaymentProposal(FIN_PaymentProposal paymentProposal) {
    // FIXME: added to access the FIN_PaymentSchedule and
    // FIN_PaymentScheduleDetail tables to be
    // removed when new security implementation is done
    OBContext.setAdminMode();
    try {
      StringBuilder hql = new StringBuilder();
      final Session session = OBDal.getInstance().getSession();
      hql.append("SELECT distinct(p." + FIN_Payment.PROPERTY_ID + ") ");
      hql.append("FROM " + FIN_PaymentPropDetail.TABLE_NAME + " as ppd ");
      hql.append(
          "inner join ppd." + FIN_PaymentPropDetail.PROPERTY_FINPAYMENTSCHEDULEDETAIL + " as psd ");
      hql.append("inner join psd." + FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS + " as pd ");
      hql.append("inner join pd." + FIN_PaymentDetail.PROPERTY_FINPAYMENT + " as p ");
      hql.append("WHERE ppd." + FIN_PaymentPropDetail.PROPERTY_FINPAYMENTPROPOSAL + "."
          + FIN_PaymentProposal.PROPERTY_ID + "= ?");
      final Query obqPay = session.createQuery(hql.toString());
      obqPay.setParameter(0, paymentProposal.getId());

      return obqPay.list();

    } catch (Exception e) {
      throw new OBException(e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * It calls the PAyment Process for the given payment and action.
   * 
   * @param vars
   *          VariablesSecureApp with the session data.
   * @param conn
   *          ConnectionProvider with the connection being used.
   * @param strAction
   *          String with the action of the process. {P, D, R}
   * @param payment
   *          FIN_Payment that needs to be processed.
   * @return a OBError with the result message of the process.
   * @throws Exception
   */
  public static OBError processPayment(VariablesSecureApp vars, ConnectionProvider conn,
      String strAction, FIN_Payment payment) throws Exception {

    ProcessBundle pb = new ProcessBundle("6255BE488882480599C81284B70CD9B3", vars).init(conn);
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("action", strAction);
    parameters.put("Fin_Payment_ID", payment.getId());
    pb.setParams(parameters);
    OBError myMessage = null;
    new FIN_PaymentProcess().execute(pb);
    myMessage = (OBError) pb.getResult();
    return myMessage;
  }

  /**
   * It calls the Payment Proposal Process for the given payment proposal and action.
   * 
   * @param vars
   *          VariablesSecureApp with the session data.
   * @param conn
   *          ConnectionProvider with the connection being used.
   * @param strProcessProposalAction
   *          String with the action of the process. {GSP, RE}
   * @param strFinPaymentProposalId
   *          String with FIN_PaymentProposal Id to be processed.
   * @return a OBError with the result message of the process.
   * @throws Exception
   */
  public static OBError processPaymentProposal(VariablesSecureApp vars, ConnectionProvider conn,
      String strProcessProposalAction, String strFinPaymentProposalId) throws Exception {
    ProcessBundle pb = new ProcessBundle("D16966FBF9604A3D91A50DC83C6EA8E3", vars).init(conn);
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("processProposalAction", strProcessProposalAction);
    parameters.put("Fin_Payment_Proposal_ID", strFinPaymentProposalId);
    pb.setParams(parameters);
    OBError myMessage = null;
    new FIN_PaymentProposalProcess().execute(pb);
    myMessage = (OBError) pb.getResult();
    return myMessage;
  }

  /**
   * It calls the Bank Statement Process for the given bank statement and action.
   * 
   * @param vars
   *          VariablesSecureApp with the session data.
   * @param conn
   *          ConnectionProvider with the connection being used.
   * @param strBankStatementAction
   *          String with the action of the process. {P, R}
   * @param strBankStatementId
   *          String with FIN_BankStatement Id to be processed.
   * @return a OBError with the result message of the process.
   * @throws Exception
   */
  public static OBError processBankStatement(VariablesSecureApp vars, ConnectionProvider conn,
      String strBankStatementAction, String strBankStatementId) throws Exception {
    ProcessBundle pb = new ProcessBundle("58A9261BACEF45DDA526F29D8557272D", vars).init(conn);
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("action", strBankStatementAction);
    parameters.put("FIN_Bankstatement_ID", strBankStatementId);
    pb.setParams(parameters);
    OBError myMessage = null;
    new FIN_BankStatementProcess().execute(pb);
    myMessage = (OBError) pb.getResult();
    return myMessage;
  }

  public static List<FIN_PaymentScheduleDetail> getOutstandingPSDs(
      FIN_PaymentScheduleDetail paymentScheduleDetail) {
    OBContext.setAdminMode();
    try {
      OBCriteria<FIN_PaymentScheduleDetail> obc = OBDal.getInstance()
          .createCriteria(FIN_PaymentScheduleDetail.class);
      obc.add(Restrictions.isNull(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS));
      obc.add(Restrictions.eq(FIN_PaymentScheduleDetail.PROPERTY_SCOEXTERNALPAYMENT, false));
      if (paymentScheduleDetail.getInvoicePaymentSchedule() != null) {
        obc.add(Restrictions.eq(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE,
            paymentScheduleDetail.getInvoicePaymentSchedule()));
      }
      if (paymentScheduleDetail.getOrderPaymentSchedule() != null) {
        obc.add(Restrictions.eq(FIN_PaymentScheduleDetail.PROPERTY_ORDERPAYMENTSCHEDULE,
            paymentScheduleDetail.getOrderPaymentSchedule()));
      }
      return obc.list();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public static List<FIN_PaymentScheduleDetail> getGLItemScheduleDetails(FIN_Payment payment) {
    if (payment.getFINPaymentDetailList().size() > 0) {
      OBContext.setAdminMode();
      try {
        OBCriteria<FIN_PaymentScheduleDetail> selectedGLItems = OBDal.getInstance()
            .createCriteria(FIN_PaymentScheduleDetail.class);
        selectedGLItems.createAlias(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS, "pd");
        selectedGLItems.add(Restrictions.in(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS,
            payment.getFINPaymentDetailList()));
        selectedGLItems.add(Restrictions.isNotNull("pd." + FIN_PaymentDetail.PROPERTY_GLITEM));
        selectedGLItems
            .add(Restrictions.isNull("pd." + FIN_PaymentDetail.PROPERTY_SCOPAYOUTPREPAYMENT));
        selectedGLItems.add(Restrictions.isNull("pd." + FIN_PaymentDetail.PROPERTY_SCOPREPAYMENT));
        return selectedGLItems.list();
      } finally {
        OBContext.restorePreviousMode();
      }
    } else {
      return new ArrayList<FIN_PaymentScheduleDetail>();
    }
  }

  /**
   * Calculates the resultant doubtful debt amount. Used when editing payment schedule detail amount
   * to be collected.
   * 
   * @param scheduleDetailsTotalAmount
   *          Payment Schedule Detail amount.
   * @param paymentAmount
   *          Amount selected to be collected. Always less or equal than scheduleDetailAmount.
   * @param doubtfulDebtAmount
   *          Payment Schedule Detail doubtFulDebt amount.
   * @return resultant doubtful debt amount. Zero if no doubtful debt amount was present.
   */
  private static BigDecimal getDoubtFulDebtAmount(BigDecimal scheduleDetailsTotalAmount,
      BigDecimal paymentAmount, BigDecimal doubtfulDebtTotalAmount) {
    BigDecimal calculatedDoubtFulDebtAmount = BigDecimal.ZERO;
    if (doubtfulDebtTotalAmount.compareTo(BigDecimal.ZERO) == 0) {
      return calculatedDoubtFulDebtAmount;
    }
    calculatedDoubtFulDebtAmount = paymentAmount
        .subtract(scheduleDetailsTotalAmount.subtract(doubtfulDebtTotalAmount));
    // There can not be negative Doubtful Debt Amounts. If it is negative,
    // set it to Zero as the
    // other Payment Schedule Detail will compensate it.
    if (calculatedDoubtFulDebtAmount.signum() > 0) {
      return calculatedDoubtFulDebtAmount;
    }
    return BigDecimal.ZERO;
  }
}