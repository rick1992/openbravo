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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.dao.TransactionsDao;
import org.openbravo.advpaymentmngt.exception.NoExecutionProcessFoundException;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.CashVATUtil;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.currency.ConversionRateDoc;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceTax;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentPropDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentProposal;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.financialmgmt.payment.FIN_Payment_Credit;
import org.openbravo.model.financialmgmt.payment.PaymentExecutionProcess;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalConnectionProvider;

import pe.com.unifiedgo.accounting.SunatUtil;
import pe.com.unifiedgo.accounting.data.SCOBoeFrom;
import pe.com.unifiedgo.accounting.data.SCOBoeToDiscount;
import pe.com.unifiedgo.accounting.data.SCOFixedcashReposition;
import pe.com.unifiedgo.accounting.data.SCOLoanDoc;
import pe.com.unifiedgo.accounting.data.SCOPaymentHistory;
import pe.com.unifiedgo.accounting.data.SCOPercepsalesDetail;
import pe.com.unifiedgo.accounting.data.SCOPrepayment;
import pe.com.unifiedgo.accounting.data.SCOPwithholdingReceipt;
import pe.com.unifiedgo.accounting.data.SCORendcurefundLine;
import pe.com.unifiedgo.accounting.data.ScoRendicioncuentas;
import pe.com.unifiedgo.migration.SMG_Utils;

public class FIN_PaymentProcess implements org.openbravo.scheduling.Process {
  private static AdvPaymentMngtDao dao;

  /*
   * private class CreditScheduleDetail { public FIN_PaymentScheduleDetail scheduleDetailId; public
   * BigDecimal amount; public BigDecimal creditUsed; }
   */

  public void execute(ProcessBundle bundle) throws Exception {
    dao = new AdvPaymentMngtDao();
    final String language = bundle.getContext().getLanguage();

    OBError msg = new OBError();
    msg.setType("Success");
    msg.setTitle(Utility.messageBD(bundle.getConnection(), "Success", language));

    try {
      // retrieve custom params
      final String strAction = (String) bundle.getParams().get("action");
      // retrieve standard params
      final String recordID = (String) bundle.getParams().get("Fin_Payment_ID");
      FIN_Payment payment = dao.getObject(FIN_Payment.class, recordID);
      final VariablesSecureApp vars = bundle.getContext().toVars();

      final ConnectionProvider conProvider = bundle.getConnection();
      final boolean isReceipt = payment.isReceipt();
      if (strAction.equals("P") || strAction.equals("D")) {
        if (payment.getBusinessPartner() != null) {
          if (FIN_Utility.isBlockedBusinessPartner(payment.getBusinessPartner().getId(), isReceipt,
              4)) {
            // If the Business Partner is blocked for Payments, the Payment will not be completed.
            msg.setType("Error");
            msg.setTitle(Utility.messageBD(conProvider, "Error", language));
            msg.setMessage(OBMessageUtils.messageBD("ThebusinessPartner") + " "
                + payment.getBusinessPartner().getIdentifier() + " "
                + OBMessageUtils.messageBD("SSA_BusinessPartnerBlocked"));
            bundle.setResult(msg);
            OBDal.getInstance().rollbackAndClose();
            return;
          }
        } else {
          OBContext.setAdminMode(true);
          try {
            for (FIN_PaymentDetail pd : payment.getFINPaymentDetailList()) {
              for (FIN_PaymentScheduleDetail psd : pd.getFINPaymentScheduleDetailList()) {
                BusinessPartner bPartner = null;
                if (psd.getInvoicePaymentSchedule() != null) {
                  bPartner = psd.getInvoicePaymentSchedule().getInvoice().getBusinessPartner();
                } else if (psd.getOrderPaymentSchedule() != null) {
                  bPartner = psd.getOrderPaymentSchedule().getOrder().getBusinessPartner();
                }
                if (bPartner != null && FIN_Utility.isBlockedBusinessPartner(bPartner.getId(),
                    payment.isReceipt(), 4)) {
                  // If the Business Partner is blocked for Payments, the Payment will not be
                  // completed.
                  msg.setType("Error");
                  msg.setTitle(Utility.messageBD(conProvider, "Error", language));
                  msg.setMessage(OBMessageUtils.messageBD("ThebusinessPartner") + " "
                      + bPartner.getIdentifier() + " "
                      + OBMessageUtils.messageBD("SSA_BusinessPartnerBlocked"));
                  bundle.setResult(msg);
                  OBDal.getInstance().rollbackAndClose();
                  return;
                }
              }
            }
          } finally {
            OBContext.restorePreviousMode();
          }
        }
      }
      OBDal.getInstance().flush();
      if (strAction.equals("P") || strAction.equals("D")) {

        System.out.println("1: payment.usedcredit:" + payment.getUsedCredit()
            + " - payment.generatedcredit:" + payment.getGeneratedCredit());

        System.out.println("1 PAYMENTTTT:");
        for (FIN_PaymentDetail pd : payment.getFINPaymentDetailList()) {
          System.out.println("pd: id:" + pd.getId() + " - pd.ident:" + pd.getIdentifier());
        }

        // Guess if this is a refund payment
        boolean isRefund = false;
        OBContext.setAdminMode(false);
        try {
          if (payment.getFINPaymentDetailList().size() > 0
              && payment.getFINPaymentDetailList().get(0).isRefund()) {
            isRefund = true;
          }
        } finally {
          OBContext.restorePreviousMode();
        }
        if (!isRefund) {
          // Undo Used credit as it will be calculated again
          payment.setUsedCredit(BigDecimal.ZERO);
          OBDal.getInstance().save(payment);
        }
        // Set APRM_Ready preference
        if (vars.getSessionValue("APRMT_MigrationToolRunning", "N").equals("Y")
            && !dao.existsAPRMReadyPreference()) {
          dao.createAPRMReadyPreference();
        }
        System.out.println("2: payment.usedcredit:" + payment.getUsedCredit()
            + " - payment.generatedcredit:" + payment.getGeneratedCredit());

        boolean orgLegalWithAccounting = FIN_Utility.periodControlOpened(payment.TABLE_NAME,
            payment.getId(), payment.TABLE_NAME + "_ID", "LE");
        if (!FIN_Utility.isPeriodOpen(payment.getClient().getId(),
            payment.getDocumentType().getDocumentCategory(), payment.getOrganization().getId(),
            OBDateUtils.formatDate(payment.getPaymentDate())) && orgLegalWithAccounting) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(conProvider, "Error", language));
          msg.setMessage(
              Utility.parseTranslation(conProvider, vars, language, "@PeriodNotAvailable@"));
          bundle.setResult(msg);
          OBDal.getInstance().rollbackAndClose();
          return;
        }
        Set<String> documentOrganizations = OBContext.getOBContext()
            .getOrganizationStructureProvider(payment.getClient().getId())
            .getNaturalTree(payment.getOrganization().getId());
        if (!documentOrganizations.contains(payment.getAccount().getOrganization().getId())) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(conProvider, "Error", language));
          msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
              "@APRM_FinancialAccountNotInNaturalTree@"));
          bundle.setResult(msg);
          OBDal.getInstance().rollbackAndClose();
          return;
        }
        System.out.println("3: payment.usedcredit:" + payment.getUsedCredit()
            + " - payment.generatedcredit:" + payment.getGeneratedCredit());

        // Check for special payment application
        boolean isapppayment = false;
        if (payment.isScoIsapppayment() != null) {
          isapppayment = payment.isScoIsapppayment();
        }
        if (isapppayment) {

          boolean iscompensationpayment = false;
          if ((payment.getScoApplicationtype() != null
              && payment.getScoApplicationtype().compareTo("SCO_COMP") == 0)
              || (payment.getScoRecvapplicationtype() != null
                  && payment.getScoRecvapplicationtype().compareTo("SCO_COMP") == 0)) {
            iscompensationpayment = true;
          }

          if (!iscompensationpayment) {
            // only payment with 0 amount allowed
            if (payment.getAmount().setScale(2, RoundingMode.HALF_UP)
                .compareTo(new BigDecimal(0)) != 0) {
              msg.setType("Error");
              msg.setTitle(Utility.messageBD(conProvider, "Error", language));
              msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                  "@SCO_AppPaymentInvalidAmount@"));
              bundle.setResult(msg);
              OBDal.getInstance().rollbackAndClose();
              return;
            }
          }
        }

        if (payment.getAccount().getCurrency().getId().equals(payment.getCurrency().getId())) {
          payment.setScoMulticurrencypayment(false);
        } else {
          payment.setScoMulticurrencypayment(true);
        }
        System.out.println("4: payment.usedcredit:" + payment.getUsedCredit()
            + " - payment.generatedcredit:" + payment.getGeneratedCredit());

        Set<String> invoiceDocNos = new TreeSet<String>();
        Set<String> orderDocNos = new TreeSet<String>();
        Set<String> glitems = new TreeSet<String>();
        BigDecimal paymentAmount = BigDecimal.ZERO;
        BigDecimal paymentWriteOfAmount = BigDecimal.ZERO;

        // FIXME: added to access the FIN_PaymentSchedule and FIN_PaymentScheduleDetail tables to be
        // removed when new security implementation is done
        OBContext.setAdminMode();
        try {
          String strRefundCredit = "";
          // update payment schedule amount
          List<FIN_PaymentDetail> paymentDetails = payment.getFINPaymentDetailList();

          // Show error message when payment has no lines
          if (paymentDetails.size() == 0) {
            msg.setType("Error");
            msg.setTitle(Utility.messageBD(conProvider, "Error", language));
            msg.setMessage(
                Utility.parseTranslation(conProvider, vars, language, "@APRM_PaymentNoLines@"));
            bundle.setResult(msg);
            OBDal.getInstance().rollbackAndClose();
            return;
          }
          System.out.println("5: payment.usedcredit:" + payment.getUsedCredit()
              + " - payment.generatedcredit:" + payment.getGeneratedCredit());

          for (FIN_PaymentDetail paymentDetail : paymentDetails) {

            // if (paymentDetail.getSCOPrepayment() != null) {
            paymentAmount = paymentAmount.add(paymentDetail.getScoPaymentamount());
            System.out.println(
                "paymentDetail.getScoPaymentamount():" + paymentDetail.getScoPaymentamount());
            // }
            for (FIN_PaymentScheduleDetail paymentScheduleDetail : paymentDetail
                .getFINPaymentScheduleDetailList()) {
              // paymentAmount = paymentAmount.add(paymentScheduleDetail.getAmount());
              // paymentAmount = paymentDetail.getScoPaymentamount();
              paymentWriteOfAmount = paymentDetail.getWriteoffAmount();
              /*
               * BigDecimal writeoff = paymentScheduleDetail.getWriteoffAmount(); if (writeoff ==
               * null) writeoff = BigDecimal.ZERO; paymentWriteOfAmount =
               * paymentWriteOfAmount.add(writeoff);
               */

              if (paymentScheduleDetail.getInvoicePaymentSchedule() != null) {
                final Invoice invoice = paymentScheduleDetail.getInvoicePaymentSchedule()
                    .getInvoice();
                invoiceDocNos
                    .add(FIN_Utility.getDesiredDocumentNo(payment.getOrganization(), invoice));
              }
              if (paymentScheduleDetail.getOrderPaymentSchedule() != null) {
                orderDocNos.add(
                    paymentScheduleDetail.getOrderPaymentSchedule().getOrder().getDocumentNo());
              }
              if (paymentScheduleDetail.getInvoicePaymentSchedule() == null
                  && paymentScheduleDetail.getOrderPaymentSchedule() == null
                  && paymentScheduleDetail.getPaymentDetails().getGLItem() == null
                  && paymentDetail.getSCOPrepayment() == null
                  && paymentDetail.getScoRendcuentas() == null) {
                if (paymentDetail.getSCOPrepayment() == null) {
                  System.out.println("WHATTTTTT paymentDetail: id:" + paymentDetail.getId()
                      + " - ident:" + paymentDetail.getIdentifier());
                }
                System.out.println("5.5: payment.usedcredit:" + payment.getUsedCredit()
                    + " - payment.generatedcredit:" + payment.getGeneratedCredit());

                if (paymentDetail.isRefund() || paymentDetail.isScoIsoverpayment())
                  strRefundCredit = Utility.messageBD(conProvider, "APRM_RefundAmount", language);
                else {
                  strRefundCredit = Utility.messageBD(conProvider, "APRM_CreditAmount", language);
                  payment.setGeneratedCredit(paymentDetail.getScoPaymentamount());
                }
                strRefundCredit += ": " + paymentDetail.getScoPaymentamount().toString();
              }
            }
            if (paymentDetail.getGLItem() != null)
              glitems.add(paymentDetail.getGLItem().getName());
          }
          System.out.println("6: payment.usedcredit:" + payment.getUsedCredit()
              + " - payment.generatedcredit:" + payment.getGeneratedCredit());

          // Set description
          if (bundle.getParams().get("isPOSOrder") == null
              || !bundle.getParams().get("isPOSOrder").equals("Y")) {
            StringBuffer description = new StringBuffer();

            if (payment.getDescription() != null && !payment.getDescription().equals(""))
              description.append(payment.getDescription()).append("\n");
            if (!invoiceDocNos.isEmpty()) {
              description.append(Utility.messageBD(conProvider, "InvoiceDocumentno", language));
              description.append(": ").append(
                  invoiceDocNos.toString().substring(1, invoiceDocNos.toString().length() - 1));
              description.append("\n");
            }
            if (!orderDocNos.isEmpty()) {
              description.append(Utility.messageBD(conProvider, "OrderDocumentno", language));
              description.append(": ")
                  .append(orderDocNos.toString().substring(1, orderDocNos.toString().length() - 1));
              description.append("\n");
            }
            if (!glitems.isEmpty()) {
              description.append(Utility.messageBD(conProvider, "APRM_GLItem", language));
              description.append(": ")
                  .append(glitems.toString().substring(1, glitems.toString().length() - 1));
              description.append("\n");
            }
            if (!"".equals(strRefundCredit))
              description.append(strRefundCredit).append("\n");

            String truncateDescription = (description.length() > 255)
                ? description.substring(0, 251).concat("...").toString()
                : description.toString();
            // payment.setDescription(truncateDescription);
          }
          // NO HAY CREDIT EN MIGRACION
          System.out.println("7: payment.usedcredit:" + payment.getUsedCredit()
              + " - payment.generatedcredit:" + payment.getGeneratedCredit() + " - paymentAmount:"
              + paymentAmount + " - payment.getAmount():" + payment.getAmount());

          if (paymentAmount.compareTo(payment.getAmount()) != 0) {
            payment.setUsedCredit(paymentAmount.subtract(payment.getAmount()));
          }

          /*
           * List<CreditScheduleDetail> lsCreditSetup = new ArrayList<CreditScheduleDetail>(); //
           * create temp objects to conver credit inside scheduledetails - added by Pedro for
           * (FIN_PaymentDetail paymentDetail : paymentDetails) { for (FIN_PaymentScheduleDetail
           * paymentScheduleDetail : paymentDetail.getFINPaymentScheduleDetailList()) {
           * 
           * if (paymentScheduleDetail.getAmount().compareTo(BigDecimal.ZERO) == 1) { // greater //
           * than 0 can // use credit CreditScheduleDetail csd = new CreditScheduleDetail();
           * csd.scheduleDetailId = paymentScheduleDetail; csd.creditUsed = new BigDecimal(0);
           * csd.amount = paymentScheduleDetail.getAmount(); lsCreditSetup.add(csd); } } }
           */

          if (payment.getUsedCredit().compareTo(BigDecimal.ZERO) != 0) {
            updateUsedCredit(payment/* , lsCreditSetup */);
          }
          System.out.println("8: payment.usedcredit:" + payment.getUsedCredit()
              + " - payment.generatedcredit:" + payment.getGeneratedCredit());

          // usedCreditFromNegatives(payment, paymentDetails, lsCreditSetup);

          // payment.setWriteoffAmount(paymentWriteOfAmount);
          payment.setAPRMProcessPayment("R");
          if (payment.getGeneratedCredit() == null) {
            payment.setGeneratedCredit(BigDecimal.ZERO);
          }
          System.out.println("9: payment.usedcredit:" + payment.getUsedCredit()
              + " - payment.generatedcredit:" + payment.getGeneratedCredit());

          if (BigDecimal.ZERO.compareTo(payment.getUsedCredit()) != 0
              || BigDecimal.ZERO.compareTo(payment.getGeneratedCredit()) != 0) {
            BusinessPartner businessPartner = payment.getBusinessPartner();
            if (businessPartner == null) {
              msg.setType("Error");
              msg.setTitle(Utility.messageBD(conProvider, "Error", language));
              msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                  "@APRM_CreditWithoutBPartner@"));
              bundle.setResult(msg);
              OBDal.getInstance().rollbackAndClose();
              return;
            }
            /*
             * PriceList priceList = payment.isReceipt() ? businessPartner.getPriceList() :
             * businessPartner.getPurchasePricelist(); if
             * (!payment.getCurrency().getId().equals(priceList != null ?
             * priceList.getCurrency().getId() : "")) { msg.setType("Error");
             * msg.setTitle(Utility.messageBD(conProvider, "Error", language));
             * msg.setMessage(String.format(Utility.parseTranslation(conProvider, vars, language,
             * "@APRM_CreditCurrency@"), priceList != null ? priceList.getCurrency().getISOCode() :
             * Utility.parseTranslation(conProvider, vars, language,
             * "@APRM_CreditNoPricelistCurrency@"))); bundle.setResult(msg);
             * OBDal.getInstance().rollbackAndClose(); return; }
             */
          }

          // Execution Process
          if (dao.isAutomatedExecutionPayment(payment.getAccount(), payment.getPaymentMethod(),
              payment.isReceipt())) {

            try {
              payment.setStatus("RPAE");

              if (dao.hasNotDeferredExecutionProcess(payment.getAccount(),
                  payment.getPaymentMethod(), payment.isReceipt())) {
                PaymentExecutionProcess executionProcess = dao.getExecutionProcess(payment);
                if (dao.isAutomaticExecutionProcess(executionProcess)) {
                  final List<FIN_Payment> payments = new ArrayList<FIN_Payment>(1);
                  payments.add(payment);
                  FIN_ExecutePayment executePayment = new FIN_ExecutePayment();
                  executePayment.init("APP", executionProcess, payments, null,
                      payment.getOrganization());
                  OBError result = executePayment.execute();
                  if ("Error".equals(result.getType())) {
                    msg.setType("Warning");
                    msg.setMessage(
                        Utility.parseTranslation(conProvider, vars, language, result.getMessage()));
                  } else if (!"".equals(result.getMessage())) {
                    String execProcessMsg = Utility.parseTranslation(conProvider, vars, language,
                        result.getMessage());
                    if (!"".equals(msg.getMessage()))
                      msg.setMessage(msg.getMessage() + "<br>");
                    msg.setMessage(msg.getMessage() + execProcessMsg);
                  }
                }
              }
            } catch (final NoExecutionProcessFoundException e) {
              e.printStackTrace(System.err);
              msg.setType("Warning");
              msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                  "@NoExecutionProcessFound@"));
              bundle.setResult(msg);
              OBDal.getInstance().rollbackAndClose();
              return;
            } catch (final Exception e) {
              e.printStackTrace(System.err);
              msg.setType("Warning");
              msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                  "@IssueOnExecutionProcess@"));
              bundle.setResult(msg);
              OBDal.getInstance().rollbackAndClose();
              return;
            }
          } else {
            BusinessPartner businessPartner = payment.getBusinessPartner();
            // When credit is used (consumed) we compensate so_creditused as this amount is already
            // included in the payment details. Credit consumed should not affect to so_creditused
            if (payment.getGeneratedCredit().compareTo(BigDecimal.ZERO) == 0
                && payment.getUsedCredit().compareTo(BigDecimal.ZERO) != 0) {
              if (isReceipt) {
                increaseCustomerCredit(businessPartner, payment.getUsedCredit());// Deprecated
              } else {
                decreaseCustomerCredit(businessPartner, payment.getUsedCredit());// Deprecated
              }
            }
            for (FIN_PaymentDetail paymentDetail : payment.getFINPaymentDetailList()) {
              // Get payment schedule1 detail list ordered by amount asc.
              // First negative if they exist and then positives
              OBCriteria<FIN_PaymentScheduleDetail> obcPSD = OBDal.getInstance()
                  .createCriteria(FIN_PaymentScheduleDetail.class);
              obcPSD.add(Restrictions.eq(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS,
                  paymentDetail));
              obcPSD.addOrderBy(FIN_PaymentScheduleDetail.PROPERTY_AMOUNT, true);

              for (FIN_PaymentScheduleDetail paymentScheduleDetail : obcPSD.list()) {

                boolean docissotrx = paymentDetail.getFinPayment().isReceipt();
                if (paymentScheduleDetail.getInvoicePaymentSchedule() != null) {
                  docissotrx = paymentScheduleDetail.getInvoicePaymentSchedule().getInvoice()
                      .isSalesTransaction();
                }

                BigDecimal amount = paymentScheduleDetail.getAmount()
                    .add(paymentScheduleDetail.getWriteoffAmount());
                // Do not restore paid amounts if the payment is awaiting execution.
                boolean invoicePaidAmounts = (FIN_Utility
                    .seqnumberpaymentstatus(isReceipt ? "RPR" : "PPM")) >= (FIN_Utility
                        .seqnumberpaymentstatus(FIN_Utility.invoicePaymentStatus(payment)));
                paymentScheduleDetail.setInvoicePaid(false);
                // Payment = 0 when the payment is generated by a invoice that consume credit

                if (invoicePaidAmounts
                    | (payment.getAmount().compareTo(new BigDecimal("0.00")) == 0)) {
                  if (paymentScheduleDetail.getInvoicePaymentSchedule() != null) {
                    // BP SO_CreditUsed
                    businessPartner = paymentScheduleDetail.getInvoicePaymentSchedule().getInvoice()
                        .getBusinessPartner();

                    // Payments update credit opposite to invoices
                    if (isReceipt) {
                      decreaseCustomerCredit(businessPartner,
                          docissotrx == isReceipt ? amount : amount.negate());// Deprecated
                    } else {
                      increaseCustomerCredit(businessPartner,
                          docissotrx == isReceipt ? amount : amount.negate());// Deprecated
                    }
                    FIN_AddPayment.updatePaymentScheduleAmounts(paymentDetail,
                        paymentScheduleDetail.getInvoicePaymentSchedule(),
                        paymentScheduleDetail.getAmount(),
                        paymentScheduleDetail.getWriteoffAmount(), strAction);
                    paymentScheduleDetail.setInvoicePaid(true);

                  }

                  if (paymentScheduleDetail.getOrderPaymentSchedule() != null) {
                    FIN_AddPayment.updatePaymentScheduleAmounts(
                        paymentScheduleDetail.getOrderPaymentSchedule(),
                        paymentScheduleDetail.getAmount(),
                        paymentScheduleDetail.getWriteoffAmount(), strAction);
                  }
                  // when generating credit for a BP SO_CreditUsed is also updated
                  if (paymentScheduleDetail.getInvoicePaymentSchedule() == null
                      && paymentScheduleDetail.getOrderPaymentSchedule() == null
                      && paymentScheduleDetail.getPaymentDetails().getGLItem() == null
                      && !paymentDetail.isRefund() && paymentDetail.getSCOPrepayment() == null
                      && paymentDetail.getScoRendcuentas() == null) {
                    // BP SO_CreditUsed
                    if (isReceipt) {
                      decreaseCustomerCredit(businessPartner,
                          docissotrx == isReceipt ? amount : amount.negate());// Deprecated
                    } else {
                      increaseCustomerCredit(businessPartner,
                          docissotrx == isReceipt ? amount : amount.negate());// Deprecated
                    }
                  }
                }
              }
            }

            // CASH VAT
            Vector<String> vInvoiceScheduleIds = new Vector<String>();

            for (FIN_PaymentDetail paymentDetail : paymentDetails) {

              if (paymentDetail.getFINPaymentScheduleDetailList().size() == 0)
                continue;
              if (paymentDetail.getFINPaymentScheduleDetailList().get(0)
                  .getInvoicePaymentSchedule() == null)
                continue;

              FIN_PaymentScheduleDetail paymentScheduleDetail = paymentDetail
                  .getFINPaymentScheduleDetailList().get(0);
              BigDecimal psdWriteoffAmount = paymentScheduleDetail.getWriteoffAmount();
              BigDecimal psdAmount = paymentScheduleDetail.getAmount();
              BigDecimal amount = psdAmount.add(psdWriteoffAmount);

              String invoiceScheduleId = null;
              if (paymentDetail.getFINPaymentScheduleDetailList().get(0)
                  .getInvoicePaymentSchedule() != null) {
                invoiceScheduleId = paymentDetail.getFINPaymentScheduleDetailList().get(0)
                    .getInvoicePaymentSchedule().getId();
              }

              boolean alreadyProcessed = false;
              if (invoiceScheduleId != null) {
                for (int i = 0; i < vInvoiceScheduleIds.size(); i++) {
                  if (vInvoiceScheduleIds.get(i).equals(invoiceScheduleId)) {
                    alreadyProcessed = true;
                    break;
                  }
                }

                vInvoiceScheduleIds.add(invoiceScheduleId);
              }

              if (!alreadyProcessed) {

                for (int j = 0; j < paymentDetails.size(); j++) {
                  if (!paymentDetails.get(j).getId().equals(paymentDetail.getId())
                      && paymentDetails.get(j).getFINPaymentScheduleDetailList().size() > 0
                      && paymentDetails.get(j).getFINPaymentScheduleDetailList().get(0)
                          .getInvoicePaymentSchedule() != null
                      && paymentDetails.get(j).getFINPaymentScheduleDetailList().get(0)
                          .getInvoicePaymentSchedule().getId().equals(invoiceScheduleId)) {
                    psdWriteoffAmount = psdWriteoffAmount.add(paymentDetails.get(j)
                        .getFINPaymentScheduleDetailList().get(0).getWriteoffAmount());
                    psdAmount = psdAmount.add(
                        paymentDetails.get(j).getFINPaymentScheduleDetailList().get(0).getAmount());
                    amount = psdAmount.add(psdWriteoffAmount);
                  }
                }

                CashVATUtil.createInvoiceTaxCashVAT(paymentDetail,
                    paymentScheduleDetail.getInvoicePaymentSchedule() != null
                        ? paymentScheduleDetail.getInvoicePaymentSchedule()
                        : paymentScheduleDetail.getOrderPaymentSchedule(),
                    amount.negate());

                BigDecimal percepcion = SunatUtil.createEntryForSunatReceipt(paymentDetail,
                    paymentScheduleDetail.getInvoicePaymentSchedule() != null
                        ? paymentScheduleDetail.getInvoicePaymentSchedule()
                        : paymentScheduleDetail.getOrderPaymentSchedule(),
                    amount.negate(), strAction);

                // ADDED BY PEDRO
                // get invoices with percentage
                if (paymentScheduleDetail.getInvoicePaymentSchedule() != null) {
                  Invoice inv = paymentScheduleDetail.getInvoicePaymentSchedule().getInvoice();
                  if (inv != null) { // only payment from invoices
                    List<Invoice> lsInvoices = new ArrayList<Invoice>();
                    List<BigDecimal> lsPercAmt = new ArrayList<BigDecimal>();
                    List<BigDecimal> lsPercPercep = new ArrayList<BigDecimal>();

                    boolean letraMigracion = false;
                    if (inv.getTransactionDocument().getScoSpecialdoctype()
                        .equals("SCOARBOEINVOICE")
                        || inv.getTransactionDocument().getScoSpecialdoctype()
                            .equals("SCOAPBOEINVOICE")
                        || inv.getTransactionDocument().getScoSpecialdoctype()
                            .equals("SCOAPLOANINVOICE")) {
                      Invoice invOld = inv;
                      while (inv.getTransactionDocument().getScoSpecialdoctype()
                          .equals("SCOARBOEINVOICE")
                          || inv.getTransactionDocument().getScoSpecialdoctype()
                              .equals("SCOAPBOEINVOICE")
                          || inv.getTransactionDocument().getScoSpecialdoctype()
                              .equals("SCOAPLOANINVOICE")) {
                        invOld = inv;
                        if (inv.getScoBoeTo() != null) {
                          inv = inv.getScoBoeTo().getBillOfExchange().getSCOBoeFromList().get(0)
                              .getInvoiceref();
                        } else {
                          letraMigracion = true;
                          break;
                        }
                      }

                      if (!letraMigracion) {
                        List<SCOBoeFrom> lsBoe = invOld.getScoBoeTo().getBillOfExchange()
                            .getSCOBoeFromList();
                        BigDecimal totalAmt = new BigDecimal(0);
                        BigDecimal totalPerc = new BigDecimal(0);
                        for (int j = 0; j < lsBoe.size(); j++) {

                          if (lsBoe.get(j).getInvoiceref() == null)
                            continue;

                          Invoice invAux = lsBoe.get(j).getInvoiceref();
                          lsInvoices.add(invAux);
                          List<InvoiceTax> taxes = invAux.getInvoiceTaxList();

                          BigDecimal taxAmt = new BigDecimal(0);
                          for (int k = 0; k < taxes.size(); k++) {
                            if (taxes.get(k).getTax().getScoSpecialtax()
                                .equals("SCOSINMEDIATEPERCEPTION")
                                || taxes.get(k).getTax().getScoSpecialtax()
                                    .equals("SCOSCREDITPERCEPTION")) {
                              taxAmt = taxAmt.add(taxes.get(k).getTaxAmount());
                            }
                          }

                          BigDecimal amt = lsBoe.get(j).getAmount();
                          taxAmt = taxAmt.multiply(lsBoe.get(j).getAmount())
                              .divide(invAux.getGrandTotalAmount(), 32, RoundingMode.HALF_UP);
                          amt = amt.subtract(taxAmt);
                          lsPercAmt.add(amt);
                          lsPercPercep.add(taxAmt);

                          totalAmt = totalAmt.add(amt);
                          totalPerc = totalPerc.add(taxAmt);
                        }

                        for (int j = 0; j < lsInvoices.size(); j++) {
                          BigDecimal aux = null;
                          if (totalAmt.compareTo(BigDecimal.ZERO) != 0) {
                            aux = lsPercAmt.get(j).divide(totalAmt, 32, RoundingMode.HALF_UP);
                            lsPercAmt.set(j, aux);
                          }
                          if (totalPerc.compareTo(BigDecimal.ZERO) != 0) {
                            aux = lsPercPercep.get(j).divide(totalPerc, 32, RoundingMode.HALF_UP);
                            lsPercPercep.set(j, aux);
                          }

                        }
                      }

                    } else {
                      // only 1
                      lsInvoices.add(inv);
                      lsPercAmt.add(new BigDecimal(1));
                      lsPercPercep.add(new BigDecimal(1));
                    }

                    paymentDetail.getSCOPaymentHistoryList().clear();

                    for (int i = 0; i < lsInvoices.size(); i++) {
                      SCOPaymentHistory history = OBProvider.getInstance()
                          .get(SCOPaymentHistory.class);
                      history.setInvoice(lsInvoices.get(i));
                      history.setPaymentDate(paymentDetail.getFinPayment().getPaymentDate());
                      history.setAmount(amount.subtract(percepcion).multiply(lsPercAmt.get(i)));
                      history.setPerceptionamt(percepcion.multiply(lsPercPercep.get(i)));
                      history.setPaymentDetails(paymentDetail);
                      history.setClient(paymentDetail.getClient());
                      history.setOrganization(paymentDetail.getOrganization());
                      paymentDetail.getSCOPaymentHistoryList().add(history);
                    }

                    OBDal.getInstance().save(paymentDetail);
                  }
                }
                // END ADDED BY PEDRO

              }
            }

            payment.setStatus(isReceipt ? "RPR" : "PPM");
            boolean ismigrationpayment = false;
            if (payment.isScoIsmigrationpayment() != null)
              ismigrationpayment = payment.isScoIsmigrationpayment();
            if ((strAction.equals("D") || FIN_Utility.isAutomaticDepositWithdrawn(payment))
                && payment.getAmount().compareTo(BigDecimal.ZERO) != 0
                && ismigrationpayment == false) {

              OBError result = triggerAutomaticFinancialAccountTransaction(vars, conProvider,
                  payment);
              if ("Error".equals(result.getType())) {
                OBDal.getInstance().rollbackAndClose();
                bundle.setResult(result);
                return;
              }
            }
          }
          OBDal.getInstance().flush();
          payment.setProcessed(true);

          if (!payment.getAccount().getCurrency().equals(payment.getCurrency())) {
            insertConversionRateDocument(payment);
          }
        } finally {
          OBDal.getInstance().flush();
          OBContext.restorePreviousMode();
        }

        // ***********************
        // Reverse Payment
        // ***********************
      } else if (strAction.equals("RV")) {
        FIN_Payment reversedPayment = (FIN_Payment) DalUtil.copy(payment, false);
        final String paymentDate = (String) bundle.getParams().get("paymentdate");
        OBContext.setAdminMode();
        try {
          if (BigDecimal.ZERO.compareTo(payment.getGeneratedCredit()) != 0
              && BigDecimal.ZERO.compareTo(payment.getUsedCredit()) != 0) {
            throw new OBException("@APRM_CreditConsumed@");
          } else if (BigDecimal.ZERO.compareTo(payment.getGeneratedCredit()) != 0
              && BigDecimal.ZERO.compareTo(payment.getUsedCredit()) == 0) {
            reversedPayment.setUsedCredit(payment.getGeneratedCredit());
            reversedPayment.setGeneratedCredit(BigDecimal.ZERO);
          } else {
            reversedPayment.setUsedCredit(BigDecimal.ZERO);
            reversedPayment.setGeneratedCredit(BigDecimal.ZERO);
          }
          reversedPayment.setDocumentNo(
              "*R*" + FIN_Utility.getDocumentNo(payment.getDocumentType(), "FIN_Payment"));
          reversedPayment.setPaymentDate(FIN_Utility.getDate(paymentDate));
          reversedPayment.setDescription("");
          reversedPayment.setProcessed(false);
          reversedPayment.setPosted("N");
          reversedPayment.setProcessNow(false);
          reversedPayment.setAPRMProcessPayment("P");
          reversedPayment.setStatus("RPAP");
          // Amounts
          reversedPayment.setAmount(payment.getAmount().negate());
          reversedPayment.setScoDocAmount(payment.getScoDocAmount().negate());
          reversedPayment.setScoFinaccAmount(payment.getScoFinaccAmount().negate());
          reversedPayment.setWriteoffAmount(payment.getWriteoffAmount().negate());
          reversedPayment
              .setFinancialTransactionAmount(payment.getFinancialTransactionAmount().negate());
          OBDal.getInstance().save(reversedPayment);

          List<FIN_PaymentDetail> reversedDetails = new ArrayList<FIN_PaymentDetail>();

          OBDal.getInstance().save(reversedPayment);
          List<FIN_Payment_Credit> credits = payment.getFINPaymentCreditList();

          for (FIN_PaymentDetail pd : payment.getFINPaymentDetailList()) {
            FIN_PaymentDetail reversedPaymentDetail = (FIN_PaymentDetail) DalUtil.copy(pd, false);
            reversedPaymentDetail.setFinPayment(reversedPayment);
            reversedPaymentDetail.setAmount(pd.getAmount().negate());
            reversedPaymentDetail.setScoPaymentamount(pd.getScoPaymentamount().negate());
            reversedPaymentDetail.setWriteoffAmount(pd.getWriteoffAmount().negate());
            if (pd.isRefund() || pd.isScoIsoverpayment()) {// FIX-ME ahora prepayment se usa para
                                                           // otra cosa
              reversedPaymentDetail.setPrepayment(true);
              reversedPaymentDetail.setRefund(false);
              reversedPayment.setGeneratedCredit(
                  reversedPayment.getGeneratedCredit().add(pd.getScoPaymentamount()));
              credits = new ArrayList<FIN_Payment_Credit>();
              OBDal.getInstance().save(reversedPayment);
            } else if (pd.isPrepayment()
                && pd.getFINPaymentScheduleDetailList().get(0).getOrderPaymentSchedule() == null) {
              reversedPaymentDetail.setPrepayment(true);
              reversedPaymentDetail.setRefund(true);
            }
            List<FIN_PaymentScheduleDetail> reversedSchedDetails = new ArrayList<FIN_PaymentScheduleDetail>();
            OBDal.getInstance().save(reversedPaymentDetail);
            // Create or update PSD of orders and invoices to set the new outstanding amount
            for (FIN_PaymentScheduleDetail psd : pd.getFINPaymentScheduleDetailList()) {
              if (psd.getInvoicePaymentSchedule() != null
                  || psd.getOrderPaymentSchedule() != null) {
                OBCriteria<FIN_PaymentScheduleDetail> unpaidSchedDet = OBDal.getInstance()
                    .createCriteria(FIN_PaymentScheduleDetail.class);
                if (psd.getInvoicePaymentSchedule() != null)
                  unpaidSchedDet.add(
                      Restrictions.eq(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE,
                          psd.getInvoicePaymentSchedule()));
                if (psd.getOrderPaymentSchedule() != null)
                  unpaidSchedDet
                      .add(Restrictions.eq(FIN_PaymentScheduleDetail.PROPERTY_ORDERPAYMENTSCHEDULE,
                          psd.getOrderPaymentSchedule()));
                unpaidSchedDet
                    .add(Restrictions.isNull(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS));
                List<FIN_PaymentScheduleDetail> openPSDs = unpaidSchedDet.list();
                // If invoice/order not fully paid, update outstanding amount
                if (openPSDs.size() > 0) {
                  FIN_PaymentScheduleDetail openPSD = openPSDs.get(0);
                  BigDecimal openAmount = openPSD.getAmount().add(psd.getAmount());
                  if (openAmount.compareTo(BigDecimal.ZERO) == 0) {
                    OBDal.getInstance().remove(openPSD);
                  } else {
                    openPSD.setAmount(openAmount);
                  }
                } else {
                  // If invoice is fully paid create a new schedule detail.
                  FIN_PaymentScheduleDetail openPSD = (FIN_PaymentScheduleDetail) DalUtil.copy(psd,
                      false);
                  openPSD.setPaymentDetails(null);
                  // Amounts
                  openPSD.setWriteoffAmount(BigDecimal.ZERO);
                  openPSD.setAmount(psd.getAmount());

                  openPSD.setCanceled(false);
                  OBDal.getInstance().save(openPSD);
                }
              }

              FIN_PaymentScheduleDetail reversedPaymentSchedDetail = (FIN_PaymentScheduleDetail) DalUtil
                  .copy(psd, false);
              reversedPaymentSchedDetail.setPaymentDetails(reversedPaymentDetail);
              // Amounts
              reversedPaymentSchedDetail.setWriteoffAmount(psd.getWriteoffAmount().negate());
              reversedPaymentSchedDetail.setAmount(psd.getAmount().negate());
              OBDal.getInstance().save(reversedPaymentSchedDetail);
              reversedSchedDetails.add(reversedPaymentSchedDetail);

              if ((FIN_Utility.invoicePaymentStatus(reversedPayment)
                  .equals(reversedPayment.getStatus()))) {
                reversedPaymentSchedDetail.setInvoicePaid(true);

              } else {
                reversedPaymentSchedDetail.setInvoicePaid(false);
              }
              OBDal.getInstance().save(reversedPaymentSchedDetail);

            }

            reversedPaymentDetail.setFINPaymentScheduleDetailList(reversedSchedDetails);
            OBDal.getInstance().save(reversedPaymentDetail);
            reversedDetails.add(reversedPaymentDetail);
          }
          reversedPayment.setFINPaymentDetailList(reversedDetails);
          OBDal.getInstance().save(reversedPayment);

          List<FIN_Payment_Credit> reversedCredits = new ArrayList<FIN_Payment_Credit>();
          for (FIN_Payment_Credit pc : credits) {
            FIN_Payment_Credit reversedPaymentCredit = (FIN_Payment_Credit) DalUtil.copy(pc, false);
            reversedPaymentCredit.setAmount(pc.getAmount().negate());
            reversedPaymentCredit.setCreditPaymentUsed(pc.getCreditPaymentUsed());
            pc.getCreditPaymentUsed().setUsedCredit(
                pc.getCreditPaymentUsed().getUsedCredit().add(pc.getAmount().negate()));
            reversedPaymentCredit.setPayment(reversedPayment);
            OBDal.getInstance().save(pc.getCreditPaymentUsed());
            OBDal.getInstance().save(reversedPaymentCredit);
            reversedCredits.add(reversedPaymentCredit);
          }

          // undoUsedInvoiceCredit(payment);

          reversedPayment.setFINPaymentCreditList(reversedCredits);
          OBDal.getInstance().save(reversedPayment);

          List<ConversionRateDoc> conversions = new ArrayList<ConversionRateDoc>();
          for (ConversionRateDoc cr : payment.getCurrencyConversionRateDocList()) {
            ConversionRateDoc reversedCR = (ConversionRateDoc) DalUtil.copy(cr, false);
            reversedCR.setForeignAmount(cr.getForeignAmount().negate());
            reversedCR.setPayment(reversedPayment);
            OBDal.getInstance().save(reversedCR);
            conversions.add(reversedCR);
          }
          reversedPayment.setCurrencyConversionRateDocList(conversions);
          OBDal.getInstance().save(reversedPayment);

          OBDal.getInstance().flush();
        } finally {
          OBContext.restorePreviousMode();
        }

        payment.setReversedPayment(reversedPayment);
        OBDal.getInstance().save(payment);

        OBDal.getInstance().flush();

        HashMap<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put("Fin_Payment_ID", reversedPayment.getId());
        parameterMap.put("action", "P");
        parameterMap.put("isReversedPayment", "Y");
        bundle.setParams(parameterMap);
        execute(bundle);

        return;

        // ***********************
        // Reactivate Payment
        // ***********************
      } else if (strAction.equals("R") || strAction.equals("RE")) {
        // Already Posted Document
        if ("Y".equals(payment.getPosted())) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(conProvider, "Error", language));
          msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
              "@PostedDocument@" + ": " + payment.getDocumentNo()));
          bundle.setResult(msg);
          OBDal.getInstance().rollbackAndClose();
          return;
        }

        // Already Posted Document
        if ("sco_M".equals(payment.getPosted())) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(conProvider, "Error", language));
          msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
              "@SCO_MigrationDoc@" + ": " + payment.getDocumentNo()));
          bundle.setResult(msg);
          OBDal.getInstance().rollbackAndClose();
          return;
        }

        // Reversed Payment
        if (payment.getReversedPayment() != null) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(conProvider, "Error", language));
          msg.setMessage(
              Utility.parseTranslation(conProvider, vars, language, "@APRM_PaymentReversed@"));
          bundle.setResult(msg);
          OBDal.getInstance().rollbackAndClose();
          return;
        }
        // Reverse Payment
        if (strAction.equals("RE") && FIN_Utility.isReversePayment(payment)) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(conProvider, "Error", language));
          msg.setMessage(
              Utility.parseTranslation(conProvider, vars, language, "@APRM_ReversePayment@"));
          bundle.setResult(msg);
          OBDal.getInstance().rollbackAndClose();
          return;
        }

        OBContext.setAdminMode(true);
        try {

          // Do not reactivate if there is a related purchase withholding receipt not in void status
          List<SCOPwithholdingReceipt> related_pwithholding_receipts = payment
              .getSCOPwithholdingReceiptFINWithholdingpaymentIDList();
          for (int i = 0; i < related_pwithholding_receipts.size(); i++) {
            if (!related_pwithholding_receipts.get(i).getDocumentStatus().equals("VO")) {
              msg.setType("Error");
              msg.setTitle(Utility.messageBD(conProvider, "Error", language));
              msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                  "@SCO_PaymentRelatedPWithholding@"));
              bundle.setResult(msg);
              OBDal.getInstance().rollbackAndClose();
              return;
            }
          }

          // Do not reactivate if there is a related fixedcash rep
          List<SCOFixedcashReposition> fixedcashreps = payment.getSCOFixedcashRepositionList();
          if (fixedcashreps.size() != 0) {
            msg.setType("Error");
            msg.setTitle(Utility.messageBD(conProvider, "Error", language));
            msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                "@SCO_PaymentRelatedFixedCashRep@"));
            bundle.setResult(msg);
            OBDal.getInstance().rollbackAndClose();
            return;
          }

          // Do not reactivate if there is a related uident deposit
          if (payment.getScoUidenTrx() != null) {
            msg.setType("Error");
            msg.setTitle(Utility.messageBD(conProvider, "Error", language));
            msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                "@SCO_PaymentRelatedUIdentTrx@"));
            bundle.setResult(msg);
            OBDal.getInstance().rollbackAndClose();
            return;
          }

          // do not reactivate if there is a related application of rendcuentas
          List<ScoRendicioncuentas> rends = payment.getSCORendicioncuentasFINPaymentOpenIDList();
          for (int i = 0; i < rends.size(); i++) {
            ScoRendicioncuentas rend = rends.get(i);
            List<FIN_PaymentDetail> pds = rend.getFINPaymentDetailEMScoRendcuentasIDList();
            if (pds.size() > 0) {
              // throw new OBException("@SCO_RendCuentasReopenUsedAmountDiff0@");
            }
          }

          // Do not reactivate if there is a related rendcuentas refund payment
          List<SCORendcurefundLine> rendcuentas_refundlines = payment
              .getSCORendcurefundLineFINPaymentRefundIDList();
          if (rendcuentas_refundlines.size() != 0) {
            msg.setType("Error");
            msg.setTitle(Utility.messageBD(conProvider, "Error", language));
            msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                "@SCO_PaymentRelatedRendCuentasRefund@"));
            bundle.setResult(msg);
            OBDal.getInstance().rollbackAndClose();
            return;
          }

          // do not reactivate if there is a related application of prepayment
          List<SCOPrepayment> prepayments = payment.getSCOPrepaymentList();
          for (int i = 0; i < prepayments.size(); i++) {
            SCOPrepayment prep = prepayments.get(i);
            List<FIN_PaymentDetail> pds = prep.getFINPaymentDetailEMSCOPrepaymentList();
            if (pds.size() > 0) {
              // throw new OBException("@SCO_PrepaymentReopenUsedAmountDiff0@");
            }
          }

          // Do not reactivate if there is a related planilla de letras payment
          List<SCOBoeToDiscount> boetodiscs = payment.getSCOBoeToDiscountList();
          if (boetodiscs.size() != 0) {
            msg.setType("Error");
            msg.setTitle(Utility.messageBD(conProvider, "Error", language));
            msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                "@SCO_PaymentRelatedBoeToDisc@"));
            bundle.setResult(msg);
            OBDal.getInstance().rollbackAndClose();
            return;
          }

          // Do not reactivate if there is a related loan document
          List<SCOLoanDoc> loandocs = payment.getSCOLoanDocList();
          if (loandocs.size() != 0) {
            msg.setType("Error");
            msg.setTitle(Utility.messageBD(conProvider, "Error", language));
            msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                "@SCO_PaymentRelatedLoanDoc@"));
            bundle.setResult(msg);
            OBDal.getInstance().rollbackAndClose();
            return;
          }

          // Do not reactivate if there is a related dua invoice
          List<Invoice> duainvoices = payment.getInvoiceEMScoDuaapppaymentIDList();
          if (duainvoices.size() != 0) {
            msg.setType("Error");
            msg.setTitle(Utility.messageBD(conProvider, "Error", language));
            msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                "@SCO_PaymentRelatedDUAInvoices@"));
            bundle.setResult(msg);
            OBDal.getInstance().rollbackAndClose();
            return;
          }

          List<FIN_PaymentDetail> details = payment.getFINPaymentDetailList();
          for (int i = 0; i < details.size(); i++) {
            FIN_PaymentDetail pd = details.get(i);
            if (pd.getSCOPercepsalesDetailList().size() > 0) {
              List<SCOPercepsalesDetail> related_psales_receipts = details.get(i)
                  .getSCOPercepsalesDetailList();
              for (int j = 0; j < related_psales_receipts.size(); i++) {
                if (!related_psales_receipts.get(j).getSCOPercepSales().getDocumentStatus()
                    .equals("VO")) {
                  msg.setType("Error");
                  msg.setTitle(Utility.messageBD(conProvider, "Error", language));
                  msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                      "@SCO_PaymentRelatedSPerception@"));
                  bundle.setResult(msg);
                  OBDal.getInstance().rollbackAndClose();
                  return;
                }
              }
            }
          }
        } finally {
          OBContext.restorePreviousMode();
        }

        // Do not reactivate if its EM_Sco_Cashtransferstatus is SCO_TRANSIT or SCO_BANK
        String transferstatus = payment.getScoCashtransferstatus() != null
            ? payment.getScoCashtransferstatus()
            : "";
        if (transferstatus.compareTo("SCO_TRANSIT") == 0
            || transferstatus.compareTo("SCO_BANK") == 0) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(conProvider, "Error", language));
          msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
              "@SCO_PaymentCashProcessedStatus@"));
          bundle.setResult(msg);
          OBDal.getInstance().rollbackAndClose();
          return;
        }

        // Do not reactivate if its em_sco_fixedcashrep_status is SCO_TA,SCO_AP,SCO_REP
        String fixedcashrep_status = payment.getScoFixedcashrepStatus() != null
            ? payment.getScoFixedcashrepStatus()
            : "";
        if (fixedcashrep_status.compareTo("SCO_TA") == 0
            || fixedcashrep_status.compareTo("SCO_AP") == 0
            || fixedcashrep_status.compareTo("SCO_REP") == 0) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(conProvider, "Error", language));
          msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
              "@SCO_PaymentPreliqProcessedStatus@"));
          bundle.setResult(msg);
          OBDal.getInstance().rollbackAndClose();
          return;
        }

        // Do not reactivate if it is in a fixed_cash_Rep
        List<SCOFixedcashReposition> fixedcashreps = payment
            .getSCOFixedcashRepositionPreliqpaymentIDList();
        if (fixedcashreps.size() != 0) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(conProvider, "Error", language));
          msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
              "@SCO_PaymentPreliqProcessedStatus@"));
          bundle.setResult(msg);
          OBDal.getInstance().rollbackAndClose();
          return;
        }

        // Do not reactive the payment if it is tax payment
        if (payment.getFinancialMgmtTaxPaymentList().size() != 0) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(conProvider, "Error", language));
          msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
              "@APRM_TaxPaymentReactivation@"));
          bundle.setResult(msg);
          OBDal.getInstance().rollbackAndClose();
          return;
        }

        // Check for RevertAutomatic Trx in paymentmethod config
        if (FIN_Utility.isAutomaticRevertDepositWithdrawn(payment)) {
          // try to revert the trx
          if (payment.getStatus().compareTo("PWNC") == 0
              || payment.getStatus().compareTo("RDNC") == 0) {
            List<FIN_FinaccTransaction> trxs = payment.getFINFinaccTransactionList();
            try {
              for (int i = 0; i < trxs.size(); i++) {
                FIN_FinaccTransaction trx = trxs.get(i);
                SMG_Utils.deleteTransaction(vars, conProvider, trx);

              }
            } catch (Exception e) {
              msg.setType("Error");
              msg.setTitle(Utility.messageBD(conProvider, "Error", language));
              msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                  "@SCO_PaymentREAutoRETrxError@:" + e.getMessage()));
              bundle.setResult(msg);
              OBDal.getInstance().rollbackAndClose();
              return;
            }
          }

          OBDal.getInstance().flush();
          payment = OBDal.getInstance().get(FIN_Payment.class, payment.getId());
        }

        // Transaction exists
        if (hasTransaction(payment)) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(conProvider, "Error", language));
          msg.setMessage(
              Utility.parseTranslation(conProvider, vars, language, "@APRM_TransactionExists@"));
          bundle.setResult(msg);
          OBDal.getInstance().rollbackAndClose();
          return;
        }
        // Payment with generated credit already used on other payments.
        if (payment.getGeneratedCredit().compareTo(BigDecimal.ZERO) == 1
            && payment.getUsedCredit().compareTo(BigDecimal.ZERO) == 1) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(conProvider, "Error", language));
          msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
              "@APRM_PaymentGeneratedCreditIsUsed@"));
          bundle.setResult(msg);
          OBDal.getInstance().rollbackAndClose();
          return;
        }

        // Do not restore paid amounts if the payment is awaiting execution.
        boolean restorePaidAmounts = (FIN_Utility
            .seqnumberpaymentstatus(payment.getStatus())) == (FIN_Utility
                .seqnumberpaymentstatus(FIN_Utility.invoicePaymentStatus(payment)));

        // Migration fix: if the payment is migrated then it can be reversed from reconciliated
        // status(RPPC), deposited(RDNC) and withdrawed(PWNC), in this cases we also need to
        // restorepaidamounts
        if (payment.getPosted().compareTo("sco_M") == 0
            && (payment.getStatus().compareTo("RPPC") == 0
                || payment.getStatus().compareTo("RDNC") == 0
                || payment.getStatus().compareTo("PWNC") == 0)) {
          restorePaidAmounts = true;
        }

        // Initialize amounts
        payment.setProcessed(false);
        OBDal.getInstance().save(payment);
        OBDal.getInstance().flush();
        payment.setWriteoffAmount(BigDecimal.ZERO);

        // if all line are deleted then update amount to zero
        if (strAction.equals("R")) {
          payment.setAmount(BigDecimal.ZERO);
        }

        payment.setStatus("RPAP");
        payment.setAPRMProcessPayment("P");
        OBDal.getInstance().save(payment);
        OBDal.getInstance().flush();

        final List<FIN_PaymentDetail> removedPD = new ArrayList<FIN_PaymentDetail>();
        List<FIN_PaymentScheduleDetail> removedPDS = new ArrayList<FIN_PaymentScheduleDetail>();
        final List<String> removedPDIds = new ArrayList<String>();
        // FIXME: added to access the FIN_PaymentSchedule and FIN_PaymentScheduleDetail tables to be
        // removed when new security implementation is done
        OBContext.setAdminMode();
        try {
          BusinessPartner businessPartner = payment.getBusinessPartner();
          // When credit is used (consumed) we compensate so_creditused as this amount is already
          // included in the payment details. Credit consumed should not affect to so_creditused
          if (payment.getGeneratedCredit().compareTo(BigDecimal.ZERO) == 0
              && payment.getUsedCredit().compareTo(BigDecimal.ZERO) != 0) {
            if (isReceipt) {
              decreaseCustomerCredit(businessPartner, payment.getUsedCredit()); // Deprecated
            } else {
              increaseCustomerCredit(businessPartner, payment.getUsedCredit()); // Deprecated
            }
          }
          List<FIN_PaymentDetail> paymentDetails = payment.getFINPaymentDetailList();
          List<ConversionRateDoc> conversionRates = payment.getCurrencyConversionRateDocList();
          Set<String> invoiceDocNos = new HashSet<String>();
          // Undo Reversed payment relationship
          List<FIN_Payment> revPayments = new ArrayList<FIN_Payment>();
          for (FIN_Payment reversedPayment : payment.getFINPaymentReversedPaymentList()) {
            reversedPayment.setReversedPayment(null);
            OBDal.getInstance().save(reversedPayment);
          }
          payment.setFINPaymentReversedPaymentList(revPayments);
          OBDal.getInstance().save(payment);

          // CASH VAT
          Vector<String> vInvoiceScheduleIds = new Vector<String>();

          for (FIN_PaymentDetail paymentDetail : paymentDetails) {
            if (paymentDetail.getFINPaymentScheduleDetailList().size() == 0)
              continue;
            if (paymentDetail.getFINPaymentScheduleDetailList().get(0)
                .getInvoicePaymentSchedule() == null)
              continue;

            FIN_PaymentScheduleDetail paymentScheduleDetail = paymentDetail
                .getFINPaymentScheduleDetailList().get(0);
            BigDecimal psdWriteoffAmount = paymentScheduleDetail.getWriteoffAmount();
            BigDecimal psdAmount = paymentScheduleDetail.getAmount();
            BigDecimal amount = psdAmount.add(psdWriteoffAmount);

            String invoiceScheduleId = null;
            if (paymentDetail.getFINPaymentScheduleDetailList().get(0)
                .getInvoicePaymentSchedule() != null) {
              invoiceScheduleId = paymentDetail.getFINPaymentScheduleDetailList().get(0)
                  .getInvoicePaymentSchedule().getId();
            }

            boolean alreadyProcessed = false;
            if (invoiceScheduleId != null) {
              for (int i = 0; i < vInvoiceScheduleIds.size(); i++) {
                if (vInvoiceScheduleIds.get(i).equals(invoiceScheduleId)) {
                  alreadyProcessed = true;
                  break;
                }
              }

              vInvoiceScheduleIds.add(invoiceScheduleId);
            }

            if (!alreadyProcessed) {

              for (int j = 0; j < paymentDetails.size(); j++) {
                if (!paymentDetails.get(j).getId().equals(paymentDetail.getId())
                    && paymentDetails.get(j).getFINPaymentScheduleDetailList().size() > 0
                    && paymentDetails.get(j).getFINPaymentScheduleDetailList().get(0)
                        .getInvoicePaymentSchedule() != null
                    && paymentDetails.get(j).getFINPaymentScheduleDetailList().get(0)
                        .getInvoicePaymentSchedule().getId().equals(invoiceScheduleId)) {
                  psdWriteoffAmount = psdWriteoffAmount.add(paymentDetails.get(j)
                      .getFINPaymentScheduleDetailList().get(0).getWriteoffAmount());
                  psdAmount = psdAmount.add(
                      paymentDetails.get(j).getFINPaymentScheduleDetailList().get(0).getAmount());
                  amount = psdAmount.add(psdWriteoffAmount);
                }
              }

              CashVATUtil.createInvoiceTaxCashVAT(paymentDetail,
                  paymentScheduleDetail.getInvoicePaymentSchedule() != null
                      ? paymentScheduleDetail.getInvoicePaymentSchedule()
                      : paymentScheduleDetail.getOrderPaymentSchedule(),
                  amount.negate());

              BigDecimal percepcion = SunatUtil.createEntryForSunatReceipt(paymentDetail,
                  paymentScheduleDetail.getInvoicePaymentSchedule() != null
                      ? paymentScheduleDetail.getInvoicePaymentSchedule()
                      : paymentScheduleDetail.getOrderPaymentSchedule(),
                  amount.negate(), strAction);
            }

          }

          for (FIN_PaymentDetail paymentDetail : paymentDetails) {

            if (paymentDetail.getSCOPrepayment() != null) {
              BigDecimal psdAmount = paymentDetail.getAmount()
                  .add(paymentDetail.getScoPerceptionamt());

              boolean docissotrx = paymentDetail.getSCOPrepayment().isSalesTransaction();
              if (docissotrx != isReceipt) {
                psdAmount = psdAmount.negate();
              }

              SCOPrepayment prep = paymentDetail.getSCOPrepayment();
              paymentDetail.getSCOPrepayment()
                  .setUsedamount(paymentDetail.getSCOPrepayment().getUsedamount().add(psdAmount));

              OBDal.getInstance().save(paymentDetail.getSCOPrepayment());
            }

            if (paymentDetail.getScoRendcuentas() != null) {
              BigDecimal psdAmount = paymentDetail.getAmount();
              ScoRendicioncuentas prep = paymentDetail.getScoRendcuentas();
              paymentDetail.getScoRendcuentas()
                  .setRefund(paymentDetail.getScoRendcuentas().getRefund().subtract(psdAmount));
            }

            if (paymentDetail.getScoPayoutprepayment() != null
                && !paymentDetail.getScoPayoutprepayment().isEmpty()) {

              BigDecimal psdAmount = paymentDetail.getAmount()
                  .add(paymentDetail.getScoPerceptionamt());
              ;
              SCOPrepayment prep = OBDal.getInstance().get(SCOPrepayment.class,
                  paymentDetail.getScoPayoutprepayment());

              boolean docissotrx = prep.isSalesTransaction();
              if (docissotrx != isReceipt) {
                psdAmount = psdAmount.negate();
              }

              prep.setTotalPaid(prep.getTotalPaid().subtract(psdAmount));

              if (prep.getAmount().compareTo(new BigDecimal(0)) > 0) {
                if (prep.getTotalPaid().compareTo(prep.getAmount()) >= 0) {
                  prep.setPaymentComplete(true);
                } else {
                  prep.setPaymentComplete(false);
                }
              } else {
                if (prep.getTotalPaid().compareTo(prep.getAmount()) <= 0) {
                  prep.setPaymentComplete(true);
                } else {
                  prep.setPaymentComplete(false);
                }
              }

              // prep.setUsedamount(new BigDecimal(0));
              OBDal.getInstance().save(prep);

              prep.setPayment(null);
              prep.setPaymentDetails(null);
              payment.getSCOPrepaymentList().remove(prep);
              OBDal.getInstance().save(prep);
              OBDal.getInstance().save(payment);
              OBDal.getInstance().flush();
            }

            if (paymentDetail.getScoPayoutrendcuentas() != null
                && !paymentDetail.getScoPayoutrendcuentas().isEmpty()) {
              BigDecimal psdAmount = paymentDetail.getAmount();

              ScoRendicioncuentas rend = OBDal.getInstance().get(ScoRendicioncuentas.class,
                  paymentDetail.getScoPayoutrendcuentas());
              rend.setTotalPaid(rend.getTotalPaid().subtract(psdAmount));
              if (rend.getTotalPaid().compareTo(rend.getAmount()) >= 0) {
                rend.setPaymentComplete(true);
              } else {
                rend.setPaymentComplete(false);
              }
              rend.setRefund(new BigDecimal(0));
              OBDal.getInstance().save(rend);

              rend.setFINPaymentOpen(null);
              rend.setPaymentDetails(null);
              payment.getSCORendicioncuentasFINPaymentOpenIDList().remove(rend);
              OBDal.getInstance().save(rend);
              OBDal.getInstance().save(payment);
              OBDal.getInstance().flush();
            }

            removedPDS = new ArrayList<FIN_PaymentScheduleDetail>();
            for (FIN_PaymentScheduleDetail paymentScheduleDetail : paymentDetail
                .getFINPaymentScheduleDetailList()) {

              boolean docissotrx = paymentDetail.getFinPayment().isReceipt();
              if (paymentScheduleDetail.getInvoicePaymentSchedule() != null) {
                docissotrx = paymentScheduleDetail.getInvoicePaymentSchedule().getInvoice()
                    .isSalesTransaction();
              }

              Boolean invoicePaidold = paymentScheduleDetail.isInvoicePaid();
              if (invoicePaidold | paymentScheduleDetail.getInvoicePaymentSchedule() == null) {
                BigDecimal psdWriteoffAmount = paymentScheduleDetail.getWriteoffAmount();
                BigDecimal psdAmount = paymentScheduleDetail.getAmount();
                BigDecimal amount = psdAmount.add(psdWriteoffAmount);
                if (psdWriteoffAmount.signum() != 0 && strAction.equals("RE")) {
                  // Restore write off
                  List<FIN_PaymentScheduleDetail> outstandingPDSs = FIN_AddPayment
                      .getOutstandingPSDs(paymentScheduleDetail);
                  BigDecimal outstandingDebtAmount = BigDecimal.ZERO;
                  if (outstandingPDSs.size() > 0) {
                    outstandingPDSs.get(0)
                        .setAmount(outstandingPDSs.get(0).getAmount().add(psdWriteoffAmount));
                    OBDal.getInstance().save(outstandingPDSs.get(0));
                  } else {
                    FIN_PaymentScheduleDetail outstandingPSD = (FIN_PaymentScheduleDetail) DalUtil
                        .copy(paymentScheduleDetail, false);
                    outstandingPSD.setAmount(psdWriteoffAmount);
                    if (paymentScheduleDetail.getDoubtfulDebtAmount().signum() != 0) {
                      if (psdWriteoffAmount
                          .compareTo(paymentScheduleDetail.getDoubtfulDebtAmount()) >= 0) {
                        outstandingDebtAmount = paymentScheduleDetail.getDoubtfulDebtAmount();
                      } else {
                        outstandingDebtAmount = psdWriteoffAmount;
                      }
                    }
                    outstandingPSD.setDoubtfulDebtAmount(outstandingDebtAmount);
                    outstandingPSD.setWriteoffAmount(BigDecimal.ZERO);
                    outstandingPSD.setPaymentDetails(null);
                    OBDal.getInstance().save(outstandingPSD);
                  }
                  paymentScheduleDetail.setWriteoffAmount(BigDecimal.ZERO);
                  paymentScheduleDetail.setDoubtfulDebtAmount(paymentScheduleDetail
                      .getDoubtfulDebtAmount().subtract(outstandingDebtAmount));
                  paymentScheduleDetail.getPaymentDetails().setWriteoffAmount(BigDecimal.ZERO);
                  OBDal.getInstance().save(paymentScheduleDetail.getPaymentDetails());
                  OBDal.getInstance().save(paymentScheduleDetail);
                }
                if (paymentScheduleDetail.getInvoicePaymentSchedule() != null) {

                  // Remove invoice description related to the credit payments
                  final Invoice invoice = paymentScheduleDetail.getInvoicePaymentSchedule()
                      .getInvoice();
                  invoiceDocNos.add(invoice.getDocumentNo());
                  final String invDesc = invoice.getDescription();
                  if (invDesc != null) {
                    final String creditMsg = Utility.messageBD(new DalConnectionProvider(),
                        "APRM_InvoiceDescUsedCredit", vars.getLanguage());
                    if (creditMsg != null) {
                      StringBuffer newDesc = new StringBuffer();
                      for (final String line : invDesc.split("\n")) {
                        if (!line.startsWith(creditMsg.substring(0, creditMsg.lastIndexOf("%s")))) {
                          newDesc.append(line);
                          if (!"".equals(line))
                            newDesc.append("\n");
                        }
                      }
                      if (newDesc.length() > 255) {
                        newDesc = newDesc.delete(251, newDesc.length());
                        newDesc = newDesc.append("...\n");
                      }
                      invoice.setDescription(newDesc.toString());

                    }
                  }
                  if (restorePaidAmounts) {
                    FIN_AddPayment.updatePaymentScheduleAmounts(paymentDetail,
                        paymentScheduleDetail.getInvoicePaymentSchedule(), psdAmount.negate(),
                        psdWriteoffAmount.negate(), strAction);
                    paymentScheduleDetail.setInvoicePaid(false);
                    OBDal.getInstance().save(paymentScheduleDetail);
                    // BP SO_CreditUsed
                    businessPartner = paymentScheduleDetail.getInvoicePaymentSchedule().getInvoice()
                        .getBusinessPartner();
                    if (isReceipt) {
                      increaseCustomerCredit(businessPartner,
                          docissotrx == isReceipt ? amount : amount.negate());// Deprecated
                    } else {
                      decreaseCustomerCredit(businessPartner,
                          docissotrx == isReceipt ? amount : amount.negate());// Deprecated

                    }

                  }
                }
                if (paymentScheduleDetail.getOrderPaymentSchedule() != null && restorePaidAmounts) {
                  FIN_AddPayment.updatePaymentScheduleAmounts(paymentDetail,
                      paymentScheduleDetail.getOrderPaymentSchedule(), psdAmount.negate(),
                      psdWriteoffAmount.negate(), strAction);
                }
                if (restorePaidAmounts) {
                  // when generating credit for a BP SO_CreditUsed is also updated
                  if (paymentScheduleDetail.getInvoicePaymentSchedule() == null
                      && paymentScheduleDetail.getOrderPaymentSchedule() == null
                      && paymentScheduleDetail.getPaymentDetails().getGLItem() == null
                      && restorePaidAmounts && !paymentDetail.isRefund()) {
                    // BP SO_CreditUsed
                    if (isReceipt) {
                      increaseCustomerCredit(businessPartner,
                          docissotrx == isReceipt ? amount : amount.negate());// Deprecated
                    } else {
                      decreaseCustomerCredit(businessPartner,
                          docissotrx == isReceipt ? amount : amount.negate());// Deprecated
                    }
                  }
                }
              }

              // ACA ESTA BORRANDO CUANDO REACTIVAS SIN BORRAR LINEAS
              // if (strAction.equals("R") || (strAction.equals("RE")
              // && paymentScheduleDetail.getInvoicePaymentSchedule() == null
              // && paymentScheduleDetail.getOrderPaymentSchedule() == null
              // && (paymentScheduleDetail.getPaymentDetails().getGLItem() == null
              // || (paymentScheduleDetail.getPaymentDetails().getScoPayoutprepayment() != null
              // && !paymentScheduleDetail.getPaymentDetails().getScoPayoutprepayment()
              // .isEmpty())
              // || (paymentScheduleDetail.getPaymentDetails()
              // .getScoPayoutrendcuentas() != null
              // && !paymentScheduleDetail.getPaymentDetails().getScoPayoutrendcuentas()
              // .isEmpty())))) {
              // FIN_AddPayment.mergePaymentScheduleDetails(paymentScheduleDetail);
              // removedPDS.add(paymentScheduleDetail);
              // }

              if (strAction.equals("R") || (strAction.equals("RE")
                  && paymentScheduleDetail.getInvoicePaymentSchedule() == null
                  && paymentScheduleDetail.getOrderPaymentSchedule() == null
                  && (paymentScheduleDetail.getPaymentDetails().getGLItem() == null))) {
                FIN_AddPayment.mergePaymentScheduleDetails(paymentScheduleDetail);
                removedPDS.add(paymentScheduleDetail);
              }

            }

            paymentDetail.getFINPaymentScheduleDetailList().removeAll(removedPDS);
            if (strAction.equals("R")) {
              OBDal.getInstance().getSession().refresh(paymentDetail);
            }
            // If there is any schedule detail with amount zero, those are deleted
            for (FIN_PaymentScheduleDetail psd : removedPDS) {

              if (BigDecimal.ZERO.compareTo(psd.getAmount()) == 0
                  && BigDecimal.ZERO.compareTo(psd.getWriteoffAmount()) == 0) {
                paymentDetail.getFINPaymentScheduleDetailList().remove(psd);
                OBDal.getInstance().getSession().refresh(paymentDetail);
                if (psd.getInvoicePaymentSchedule() != null) {
                  psd.getInvoicePaymentSchedule()
                      .getFINPaymentScheduleDetailInvoicePaymentScheduleList().remove(psd);
                }
                if (psd.getOrderPaymentSchedule() != null) {
                  psd.getOrderPaymentSchedule()
                      .getFINPaymentScheduleDetailOrderPaymentScheduleList().remove(psd);
                }
                OBDal.getInstance().remove(psd);
              }
            }
            if (paymentDetail.getFINPaymentScheduleDetailList().size() == 0) {

              if (!strAction.equals("RE") || paymentDetail.getSCOPrepayment() == null) {
                removedPD.add(paymentDetail);
                removedPDIds.add(paymentDetail.getId());
              }

            }
            OBDal.getInstance().save(paymentDetail);
          }

          for (String pdToRm : removedPDIds) {
            OBDal.getInstance().remove(OBDal.getInstance().get(FIN_PaymentDetail.class, pdToRm));
          }
          payment.getFINPaymentDetailList().removeAll(removedPD);
          if (strAction.equals("R")) {
            payment.getCurrencyConversionRateDocList().removeAll(conversionRates);
            // payment.setFinancialTransactionConvertRate(BigDecimal.ZERO);
          }
          OBDal.getInstance().save(payment);

          if (payment.getGeneratedCredit().compareTo(BigDecimal.ZERO) == 0
              && payment.getUsedCredit().compareTo(BigDecimal.ZERO) != 0) {
            undoUsedCredit(payment, vars, invoiceDocNos);
          }

          // undoUsedInvoiceCredit(payment);

          List<FIN_Payment> creditPayments = new ArrayList<FIN_Payment>();
          for (final FIN_Payment_Credit pc : payment.getFINPaymentCreditList()) {
            creditPayments.add(pc.getCreditPaymentUsed());
          }
          for (final FIN_Payment creditPayment : creditPayments) {
            // Update Description
            final String payDesc = creditPayment.getDescription();
            if (payDesc != null) {
              final String invoiceDocNoMsg = Utility.messageBD(new DalConnectionProvider(),
                  "APRM_CreditUsedinInvoice", vars.getLanguage());
              if (invoiceDocNoMsg != null) {
                final StringBuffer newDesc = new StringBuffer();
                for (final String line : payDesc.split("\n")) {
                  boolean include = true;
                  if (line.startsWith(
                      invoiceDocNoMsg.substring(0, invoiceDocNoMsg.lastIndexOf("%s")))) {
                    for (final String docNo : invoiceDocNos) {
                      if (line.indexOf(docNo) > 0) {
                        include = false;
                        break;
                      }
                    }
                  }
                  if (include) {
                    newDesc.append(line);
                    if (!"".equals(line))
                      newDesc.append("\n");
                  }
                }
                // Truncate Description to keep length as 255
                creditPayment.setDescription(
                    newDesc.toString().length() > 255 ? newDesc.toString().substring(0, 255)
                        : newDesc.toString());
              }
            }
          }

          payment.getFINPaymentCreditList().clear();
          payment.setGeneratedCredit(BigDecimal.ZERO);
          if (strAction.equals("R")) {
            payment.setUsedCredit(BigDecimal.ZERO);
            for (FIN_PaymentScheduleDetail psd : removedPDS) {
              List<FIN_PaymentPropDetail> ppds = psd.getFINPaymentPropDetailList();
              if (ppds.size() > 0) {
                for (FIN_PaymentPropDetail ppd : ppds) {
                  FIN_PaymentProposal paymentProposal = OBDal.getInstance()
                      .get(FIN_PaymentProposal.class, ppd.getFinPaymentProposal().getId());
                  paymentProposal.setProcessed(false);
                  OBDal.getInstance().save(paymentProposal);
                  OBDal.getInstance().remove(ppd);
                  OBDal.getInstance().flush();
                  paymentProposal.setProcessed(true);
                }
              }
            }
          }

        } finally {
          OBDal.getInstance().flush();
          OBContext.restorePreviousMode();
        }

      } else if (strAction.equals("V")) {
        // Void
        OBContext.setAdminMode();
        try {
          if (payment.isProcessed()) {
            // Already Posted Document
            if ("Y".equals(payment.getPosted())) {
              msg.setType("Error");
              msg.setTitle(Utility.messageBD(conProvider, "Error", language));
              msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                  "@PostedDocument@" + ": " + payment.getDocumentNo()));
              bundle.setResult(msg);
              OBDal.getInstance().rollbackAndClose();
              return;
            }
            // Transaction exists
            if (hasTransaction(payment)) {
              msg.setType("Error");
              msg.setTitle(Utility.messageBD(conProvider, "Error", language));
              msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                  "@APRM_TransactionExists@"));
              bundle.setResult(msg);
              OBDal.getInstance().rollbackAndClose();
              return;
            }

            OBContext.setAdminMode(true);
            try {
              // Do not reactivate if there is a related purchase withholding receipt not in void
              // status
              List<SCOPwithholdingReceipt> related_pwithholding_receipts = payment
                  .getSCOPwithholdingReceiptFINWithholdingpaymentIDList();
              for (int i = 0; i < related_pwithholding_receipts.size(); i++) {
                if (!related_pwithholding_receipts.get(i).getDocumentStatus().equals("VO")) {
                  msg.setType("Error");
                  msg.setTitle(Utility.messageBD(conProvider, "Error", language));
                  msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                      "@SCO_PaymentRelatedPWithholding@"));
                  bundle.setResult(msg);
                  OBDal.getInstance().rollbackAndClose();
                  return;
                }
              }

              List<FIN_PaymentDetail> details = payment.getFINPaymentDetailList();
              for (int i = 0; i < details.size(); i++) {
                if (details.get(i).getSCOPercepsalesDetailList().size() > 0) {
                  List<SCOPercepsalesDetail> related_psales_receipts = details.get(i)
                      .getSCOPercepsalesDetailList();
                  for (int j = 0; j < related_psales_receipts.size(); i++) {
                    if (!related_psales_receipts.get(j).getSCOPercepSales().getDocumentStatus()
                        .equals("VO")) {
                      msg.setType("Error");
                      msg.setTitle(Utility.messageBD(conProvider, "Error", language));
                      msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                          "@SCO_PaymentRelatedSPerception@"));
                      bundle.setResult(msg);
                      OBDal.getInstance().rollbackAndClose();
                      return;
                    }
                  }
                }
              }
            } finally {
              OBContext.restorePreviousMode();
            }
            // Payment with generated credit already used on other payments.
            if (payment.getGeneratedCredit().compareTo(BigDecimal.ZERO) == 1
                && payment.getUsedCredit().compareTo(BigDecimal.ZERO) == 1) {
              msg.setType("Error");
              msg.setTitle(Utility.messageBD(conProvider, "Error", language));
              msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                  "@APRM_PaymentGeneratedCreditIsUsed@"));
              bundle.setResult(msg);
              OBDal.getInstance().rollbackAndClose();
              return;
            }
            // Payment not in Awaiting Execution
            boolean restorePaidAmounts = (FIN_Utility
                .seqnumberpaymentstatus(payment.getStatus())) < (FIN_Utility
                    .seqnumberpaymentstatus(FIN_Utility.invoicePaymentStatus(payment)));
            if (!restorePaidAmounts) {
              msg.setType("Error");
              msg.setTitle(Utility.messageBD(conProvider, "Error", language));
              msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                  "@APRM_PaymentNotRPAE_NotVoid@"));
              bundle.setResult(msg);
              OBDal.getInstance().rollbackAndClose();
              return;
            }

            /*
             * Void the payment
             */
            payment.setStatus("RPVOID");

            /*
             * Cancel all payment schedule details related to the payment
             */
            final List<FIN_PaymentScheduleDetail> removedPDS = new ArrayList<FIN_PaymentScheduleDetail>();
            Set<String> invoiceDocNos = new HashSet<String>();
            for (final FIN_PaymentDetail paymentDetail : payment.getFINPaymentDetailList()) {
              for (final FIN_PaymentScheduleDetail paymentScheduleDetail : paymentDetail
                  .getFINPaymentScheduleDetailList()) {
                Boolean invoicePaidold = paymentScheduleDetail.isInvoicePaid();

                if (invoicePaidold | paymentScheduleDetail.getInvoicePaymentSchedule() == null) {
                  paymentScheduleDetail.setInvoicePaid(false);
                }
                BigDecimal outStandingAmt = BigDecimal.ZERO;

                if (paymentScheduleDetail.getInvoicePaymentSchedule() != null) {
                  // Related to invoices
                  for (final FIN_PaymentScheduleDetail invScheDetail : paymentScheduleDetail
                      .getInvoicePaymentSchedule()
                      .getFINPaymentScheduleDetailInvoicePaymentScheduleList()) {
                    if (invScheDetail.isCanceled()) {
                      continue;
                    }
                    if (invScheDetail.getPaymentDetails() == null) {
                      outStandingAmt = outStandingAmt.add(invScheDetail.getAmount())
                          .add(invScheDetail.getWriteoffAmount());
                      removedPDS.add(invScheDetail);
                    } else if (invScheDetail.equals(paymentScheduleDetail)) {
                      outStandingAmt = outStandingAmt.add(invScheDetail.getAmount())
                          .add(invScheDetail.getWriteoffAmount());
                      paymentScheduleDetail.setCanceled(true);
                    }
                    invoiceDocNos.add(paymentScheduleDetail.getInvoicePaymentSchedule().getInvoice()
                        .getDocumentNo());
                  }
                  // Create merged Payment Schedule Detail with the pending to be paid amount
                  if (outStandingAmt.compareTo(BigDecimal.ZERO) != 0) {
                    final FIN_PaymentScheduleDetail mergedScheduleDetail = dao
                        .getNewPaymentScheduleDetail(payment.getOrganization(), outStandingAmt);
                    mergedScheduleDetail.setInvoicePaymentSchedule(
                        paymentScheduleDetail.getInvoicePaymentSchedule());
                    mergedScheduleDetail
                        .setOrderPaymentSchedule(paymentScheduleDetail.getOrderPaymentSchedule());
                    OBDal.getInstance().save(mergedScheduleDetail);
                  }
                } else if (paymentScheduleDetail.getOrderPaymentSchedule() != null) {
                  // Related to orders
                  for (final FIN_PaymentScheduleDetail ordScheDetail : paymentScheduleDetail
                      .getOrderPaymentSchedule()
                      .getFINPaymentScheduleDetailOrderPaymentScheduleList()) {
                    if (ordScheDetail.isCanceled()) {
                      continue;
                    }
                    if (ordScheDetail.getPaymentDetails() == null) {
                      outStandingAmt = outStandingAmt.add(ordScheDetail.getAmount())
                          .add(ordScheDetail.getWriteoffAmount());
                      removedPDS.add(ordScheDetail);
                    } else if (ordScheDetail.equals(paymentScheduleDetail)) {
                      outStandingAmt = outStandingAmt.add(ordScheDetail.getAmount())
                          .add(ordScheDetail.getWriteoffAmount());
                      paymentScheduleDetail.setCanceled(true);
                    }
                  }
                  // Create merged Payment Schedule Detail with the pending to be paid amount
                  if (outStandingAmt.compareTo(BigDecimal.ZERO) != 0) {
                    final FIN_PaymentScheduleDetail mergedScheduleDetail = dao
                        .getNewPaymentScheduleDetail(payment.getOrganization(), outStandingAmt);
                    mergedScheduleDetail
                        .setOrderPaymentSchedule(paymentScheduleDetail.getOrderPaymentSchedule());
                    OBDal.getInstance().save(mergedScheduleDetail);
                  }
                } else if (paymentDetail.getGLItem() != null) {
                  paymentScheduleDetail.setCanceled(true);
                } else if (paymentScheduleDetail.getOrderPaymentSchedule() == null
                    && paymentScheduleDetail.getInvoicePaymentSchedule() == null) {
                  // Credit payment
                  // B. reemplazo por monto de paymentdetail y no de paymentschedule
                  // payment.setGeneratedCredit(payment.getGeneratedCredit().subtract(paymentScheduleDetail.getAmount()));
                  removedPDS.add(paymentScheduleDetail);
                }

                OBDal.getInstance().save(payment);
                OBDal.getInstance().flush();
              }

              // agregado en reemplazo de B.
              payment.setGeneratedCredit(
                  payment.getGeneratedCredit().subtract(paymentDetail.getScoPaymentamount()));
              OBDal.getInstance().save(payment);
              OBDal.getInstance().flush();

              paymentDetail.getFINPaymentScheduleDetailList().removeAll(removedPDS);
              for (FIN_PaymentScheduleDetail removedPD : removedPDS) {
                if (removedPD.getOrderPaymentSchedule() != null) {
                  removedPD.getOrderPaymentSchedule()
                      .getFINPaymentScheduleDetailOrderPaymentScheduleList().remove(removedPD);
                  OBDal.getInstance().save(removedPD.getOrderPaymentSchedule());
                }
                if (removedPD.getInvoicePaymentSchedule() != null) {
                  removedPD.getInvoicePaymentSchedule()
                      .getFINPaymentScheduleDetailInvoicePaymentScheduleList().remove(removedPD);
                  OBDal.getInstance().save(removedPD.getInvoicePaymentSchedule());
                }
                OBDal.getInstance().remove(removedPD);
              }
              OBDal.getInstance().flush();
              removedPDS.clear();

            }
            if (payment.getGeneratedCredit().compareTo(BigDecimal.ZERO) == 0
                && payment.getUsedCredit().compareTo(BigDecimal.ZERO) == 1) {
              undoUsedCredit(payment, vars, invoiceDocNos);
            }

            // undoUsedInvoiceCredit(payment);

            payment.getFINPaymentCreditList().clear();
            payment.setUsedCredit(BigDecimal.ZERO);

          }
          OBDal.getInstance().flush();
        } finally {
          OBContext.restorePreviousMode();
        }
      }

      bundle.setResult(msg);

    } catch (final Exception e) {
      e.printStackTrace(System.err);
      msg.setType("Error");
      msg.setTitle(
          Utility.messageBD(bundle.getConnection(), "Error", bundle.getContext().getLanguage()));
      msg.setMessage(FIN_Utility.getExceptionMessage(e));
      bundle.setResult(msg);
      OBDal.getInstance().rollbackAndClose();
    }
  }

  /**
   * Method used to update the credit used when the user doing invoice processing or payment
   * processing
   * 
   * @param amount
   *          Payment amount
   */
  private void updateCustomerCredit(BusinessPartner businessPartner, BigDecimal amount,
      boolean add) {
    // NO SIRVE Y NO LO USAMOS, VA A SUMAR DE DIFERENTES MONEDAS
    /*
     * BigDecimal creditUsed = businessPartner.getCreditUsed(); if (add) { creditUsed =
     * creditUsed.add(amount); } else { creditUsed = creditUsed.subtract(amount); }
     * businessPartner.setCreditUsed(creditUsed); OBDal.getInstance().save(businessPartner);
     * OBDal.getInstance().flush();
     */
  }

  private void increaseCustomerCredit(BusinessPartner businessPartner, BigDecimal amount) {
    updateCustomerCredit(businessPartner, amount, true);
  }

  private void decreaseCustomerCredit(BusinessPartner businessPartner, BigDecimal amount) {
    updateCustomerCredit(businessPartner, amount, false);
  }

  private OBError triggerAutomaticFinancialAccountTransaction(VariablesSecureApp vars,
      ConnectionProvider connectionProvider, FIN_Payment payment) {
    FIN_FinaccTransaction transaction = TransactionsDao.createFinAccTransaction(payment);
    try {
      return processTransaction(vars, connectionProvider, "P", transaction);

    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      e.printStackTrace(System.err);
      OBError msg = new OBError();
      msg.setType("Error");
      msg.setMessage(e.getMessage());
      msg.setTitle(e.getMessage());
      return msg;
    }
  }

  private static boolean hasTransaction(FIN_Payment payment) {
    OBCriteria<FIN_FinaccTransaction> transaction = OBDal.getInstance()
        .createCriteria(FIN_FinaccTransaction.class);
    transaction.add(Restrictions.eq(FIN_FinaccTransaction.PROPERTY_FINPAYMENT, payment));
    List<FIN_FinaccTransaction> list = transaction.list();
    if (list == null || list.size() == 0)
      return false;
    return true;
  }

  private void updateUsedCredit(FIN_Payment newPayment/* , List<CreditScheduleDetail> scds */) {
    if (newPayment.getFINPaymentCreditList().isEmpty()) {

      // We process the payment from the Payment In/Out window (not from the Process Invoice flow)
      final BigDecimal usedAmount = newPayment.getUsedCredit();
      final BusinessPartner bp = newPayment.getBusinessPartner();
      final boolean isReceipt = newPayment.isReceipt();
      final Organization Org = newPayment.getOrganization();

      List<FIN_Payment> creditPayments = dao.getCustomerPaymentsWithCredit(Org, bp, isReceipt,
          newPayment.getAccount().getCurrency());
      BigDecimal pendingToAllocateAmount = usedAmount;

      int indexCredit = 0;
      // CreditScheduleDetail currentScheduleDetail = null;

      // if (scds.size() > 0)
      // currentScheduleDetail = scds.get(indexCredit);

      for (FIN_Payment creditPayment : creditPayments) {

        BigDecimal toAllocate = new BigDecimal(0);
        boolean toBreak = false;

        BigDecimal availableAmount = creditPayment.getGeneratedCredit()
            .subtract(creditPayment.getUsedCredit());
        if (pendingToAllocateAmount.compareTo(availableAmount) == 1) {
          creditPayment.setUsedCredit(creditPayment.getUsedCredit().add(availableAmount));
          pendingToAllocateAmount = pendingToAllocateAmount.subtract(availableAmount);
          toAllocate = availableAmount;

        } else {
          creditPayment.setUsedCredit(creditPayment.getUsedCredit().add(pendingToAllocateAmount));
          toAllocate = pendingToAllocateAmount;
          toBreak = true;
        }

        linkCreditPayment(newPayment, toAllocate, creditPayment);
        // find suitable schedule to reduce
        /*
         * while (toAllocate.compareTo(BigDecimal.ZERO) > 0) {
         * 
         * BigDecimal allowed = null;
         * 
         * if (currentScheduleDetail.amount.compareTo(currentScheduleDetail.creditUsed) > 0) {
         * BigDecimal remainingAllowed =
         * currentScheduleDetail.amount.subtract(currentScheduleDetail.creditUsed); if
         * (remainingAllowed.compareTo(toAllocate) > 0) allowed = toAllocate; else allowed =
         * remainingAllowed;
         * 
         * linkCreditPayment(newPayment, allowed, creditPayment,
         * currentScheduleDetail.scheduleDetailId); toAllocate = toAllocate.subtract(allowed);
         * currentScheduleDetail.creditUsed = currentScheduleDetail.creditUsed.add(allowed); } else
         * {// next schedule indexCredit++; currentScheduleDetail = scds.get(indexCredit); } }
         */

        OBDal.getInstance().save(creditPayment);

        if (toBreak)
          break;
      }
    }
  }

  /*
   * DEPRECATED -- era para el reporte de cuentas x cobrar private void
   * usedCreditFromNegatives(FIN_Payment newPayment, List<FIN_PaymentDetail> paymentDetails,
   * List<CreditScheduleDetail> scds) { List<FIN_PaymentScheduleDetail> lsPositiveSD = new
   * ArrayList<FIN_PaymentScheduleDetail>(); List<FIN_PaymentScheduleDetail> lsNegativeSD = new
   * ArrayList<FIN_PaymentScheduleDetail>();
   * 
   * if (scds.size() == 0) {// no asociar credit return; }
   * 
   * for (FIN_PaymentDetail paymentDetail : paymentDetails) { for (FIN_PaymentScheduleDetail
   * paymentScheduleDetail : paymentDetail.getFINPaymentScheduleDetailList()) {
   * 
   * if (paymentScheduleDetail.getAmount().compareTo(BigDecimal.ZERO) == -1)
   * lsNegativeSD.add(paymentScheduleDetail); else if
   * (paymentScheduleDetail.getAmount().compareTo(BigDecimal.ZERO) == 1)
   * lsPositiveSD.add(paymentScheduleDetail); } }
   * 
   * if (lsNegativeSD.size() > 0) {
   * 
   * int indexNeg = 0; FIN_PaymentScheduleDetail currentNeg = lsNegativeSD.get(indexNeg); int
   * indexPos = 0; CreditScheduleDetail currentPos = scds.get(indexPos); while (indexNeg <
   * lsNegativeSD.size()) { currentNeg = lsNegativeSD.get(indexNeg); BigDecimal toAllocate =
   * currentNeg.getAmount().negate(); while (toAllocate.compareTo(BigDecimal.ZERO) > 0) { BigDecimal
   * allowed = null;
   * 
   * if (currentPos.amount.compareTo(currentPos.creditUsed) > 0) { BigDecimal remainingAllowed =
   * currentPos.amount.subtract(currentPos.creditUsed); if (remainingAllowed.compareTo(toAllocate) >
   * 0) allowed = toAllocate; else allowed = remainingAllowed;
   * 
   * linkCreditInvoice(newPayment, allowed, currentNeg, currentPos.scheduleDetailId); toAllocate =
   * toAllocate.subtract(allowed); currentPos.creditUsed = currentPos.creditUsed.add(allowed);
   * 
   * } else {// next schedule indexPos++; if (scds.size() <= indexPos) return;// no more payments to
   * link with credit currentPos = scds.get(indexPos); }
   * 
   * } indexNeg++; } } }
   */

  public static void linkCreditPayment(FIN_Payment newPayment, BigDecimal usedAmount,
      FIN_Payment creditPayment) {
    final FIN_Payment_Credit creditInfo = OBProvider.getInstance().get(FIN_Payment_Credit.class);
    creditInfo.setPayment(newPayment);
    creditInfo.setAmount(usedAmount);
    creditInfo.setCurrency(newPayment.getAccount().getCurrency());
    creditInfo.setCreditPaymentUsed(creditPayment);
    creditInfo.setOrganization(newPayment.getOrganization());
    creditInfo.setClient(newPayment.getClient());
    newPayment.getFINPaymentCreditList().add(creditInfo);
  }

  private void undoUsedCredit(FIN_Payment myPayment, VariablesSecureApp vars,
      Set<String> invoiceDocNos) {
    final List<FIN_Payment> payments = new ArrayList<FIN_Payment>();
    for (final FIN_Payment_Credit pc : myPayment.getFINPaymentCreditList()) {
      final FIN_Payment creditPaymentUsed = pc.getCreditPaymentUsed();
      creditPaymentUsed.setUsedCredit(creditPaymentUsed.getUsedCredit().subtract(pc.getAmount()));
      payments.add(creditPaymentUsed);
    }

    for (final FIN_Payment payment : payments) {
      // Update Description
      final String payDesc = payment.getDescription();
      if (payDesc != null) {
        final String invoiceDocNoMsg = Utility.messageBD(new DalConnectionProvider(),
            "APRM_CreditUsedinInvoice", vars.getLanguage());
        if (invoiceDocNoMsg != null) {
          final StringBuffer newDesc = new StringBuffer();
          for (final String line : payDesc.split("\n")) {
            boolean include = true;
            if (line.startsWith(invoiceDocNoMsg.substring(0, invoiceDocNoMsg.lastIndexOf("%s")))) {
              for (final String docNo : invoiceDocNos) {
                if (line.indexOf(docNo) > 0) {
                  include = false;
                  break;
                }
              }
            }
            if (include) {
              newDesc.append(line);
              if (!"".equals(line))
                newDesc.append("\n");
            }
          }
          // Truncate Description to keep length as 255
          payment.setDescription(
              newDesc.toString().length() > 255 ? newDesc.toString().substring(0, 255)
                  : newDesc.toString());
        }
      }
    }
  }

  private List<ConversionRateDoc> getConversionRateDocument(FIN_Payment payment) {
    OBContext.setAdminMode();
    try {
      OBCriteria<ConversionRateDoc> obc = OBDal.getInstance()
          .createCriteria(ConversionRateDoc.class);
      obc.add(
          Restrictions.eq(ConversionRateDoc.PROPERTY_CURRENCY, payment.getAccount().getCurrency()));
      obc.add(Restrictions.eq(ConversionRateDoc.PROPERTY_TOCURRENCY, payment.getCurrency()));
      obc.add(Restrictions.eq(ConversionRateDoc.PROPERTY_PAYMENT, payment));
      return obc.list();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private ConversionRateDoc insertConversionRateDocument(FIN_Payment payment) {
    OBContext.setAdminMode();
    try {
      List<ConversionRateDoc> cnvratedoc_list = getConversionRateDocument(payment);
      ConversionRateDoc newConversionRateDoc = null;
      if (cnvratedoc_list.size() != 0) {
        newConversionRateDoc = cnvratedoc_list.get(0);
        newConversionRateDoc.setRate(payment.getFinancialTransactionConvertRate());
        newConversionRateDoc.setForeignAmount(payment.getFinancialTransactionAmount());
      } else {
        newConversionRateDoc = OBProvider.getInstance().get(ConversionRateDoc.class);
        newConversionRateDoc.setOrganization(payment.getOrganization());
        newConversionRateDoc.setCurrency(payment.getAccount().getCurrency());
        newConversionRateDoc.setToCurrency(payment.getCurrency());
        newConversionRateDoc.setRate(payment.getFinancialTransactionConvertRate());
        newConversionRateDoc.setForeignAmount(payment.getFinancialTransactionAmount());
        newConversionRateDoc.setPayment(payment);
        newConversionRateDoc.setClient(payment.getClient());
      }

      OBDal.getInstance().save(newConversionRateDoc);
      OBDal.getInstance().flush();
      return newConversionRateDoc;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * It calls the Transaction Process for the given transaction and action.
   * 
   * @param vars
   *          VariablesSecureApp with the session data.
   * @param conn
   *          ConnectionProvider with the connection being used.
   * @param strAction
   *          String with the action of the process. {P, D, R}
   * @param transaction
   *          FIN_FinaccTransaction that needs to be processed.
   * @return a OBError with the result message of the process.
   * @throws Exception
   */
  private OBError processTransaction(VariablesSecureApp vars, ConnectionProvider conn,
      String strAction, FIN_FinaccTransaction transaction) throws Exception {
    ProcessBundle pb = new ProcessBundle("F68F2890E96D4D85A1DEF0274D105BCE", vars).init(conn);
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("action", strAction);
    parameters.put("Fin_FinAcc_Transaction_ID", transaction.getId());
    pb.setParams(parameters);
    OBError myMessage = null;
    new FIN_TransactionProcess().execute(pb);
    myMessage = (OBError) pb.getResult();
    return myMessage;
  }

}