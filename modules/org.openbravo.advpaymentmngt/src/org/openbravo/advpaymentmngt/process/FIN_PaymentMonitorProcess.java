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
 * All portions are Copyright (C) 2010-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */

package org.openbravo.advpaymentmngt.process;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.OBInterceptor;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.erpCommon.utility.PropertyNotFoundException;
import org.openbravo.model.ad.domain.Preference;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.payment.DebtPayment;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedInvV;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.service.db.DalBaseProcess;
import org.openbravo.service.db.DalConnectionProvider;
import org.quartz.JobExecutionException;

public class FIN_PaymentMonitorProcess extends DalBaseProcess {
  private static ProcessLogger logger;

  public void doExecute(ProcessBundle bundle) throws Exception {

    logger = bundle.getLogger();
    // Check to know if PaymentMonitor property is set in the system.
    try {
      Preferences.getPreferenceValue("PaymentMonitor", true, null, null, OBContext.getOBContext().getUser(), null, null);
    } catch (PropertyNotFoundException e) {
      logger.log("Property not found \n");
      return;
    } catch (PropertyException e) {
      logger.log("PropertyException, there is a conflict for PaymentMonitor property\n");
      return;
    }
    // Check to know that this APR is the module implementing the PaymentMonitor property
    if (isPreferenceOfModule("PaymentMonitor", "A918E3331C404B889D69AA9BFAFB23AC")) {
      logger.log("Starting Update Paid Amount for Invoices Background Process.\n");
    } else {
      logger.log("Payment Monitor active for other module.\n");
      logger.log("Core's background process is executed.\n");
      return;
    }

    try {
      int counter = 0;
      final Module migration = OBDal.getInstance().get(Module.class, "4BD3D4B262B048518FE62496EF09D549");

      StringBuilder whereClause = new StringBuilder();
      whereClause.append(" as i");
      whereClause.append("   left join i.fINPaymentScheduleList fps ");
      whereClause.append(" where i.processed=true");
      whereClause.append(" and (i.paymentComplete=false ");
      whereClause.append("      or fps.updated >= i.lastCalculatedOnDate ");
      whereClause.append("      or i.outstandingAmount <> 0");
      if (migration != null) {
        whereClause.append("  or (i.finalSettlementDate is null");
        whereClause.append(" and fps.id is not null");
        whereClause.append(" and i.aprmtIsmigrated = 'N'))");
      } else {
        whereClause.append(" or i.finalSettlementDate is null)");
      }

      // whereClause.append(" and i.scrPhysicalDocumentno = '011-0046177'");

      final OBQuery<Invoice> obc = OBDal.getInstance().createQuery(Invoice.class, whereClause.toString());

      // For Background process execution at system level
      if (OBContext.getOBContext().isInAdministratorMode()) {
        obc.setFilterOnReadableClients(false);
        obc.setFilterOnReadableOrganization(false);
      }
      final Set<String> invoiceIds = new HashSet<String>();
      for (Invoice invoice : obc.list()) {
        invoiceIds.add(invoice.getId());
      }

      for (String invoiceId : invoiceIds) {
        Invoice invoice = OBDal.getInstance().get(Invoice.class, invoiceId);
        System.out.println("updating inv:" + invoice.getIdentifier());

        updateInvoice(invoice);
        counter++;
        OBDal.getInstance().getSession().flush();
        OBDal.getInstance().getSession().clear();
        if (counter % 50 == 0) {
          logger.log("Invoices updated: " + counter + "\n");
        }
      }
      if (counter % 50 != 0)
        logger.log("Invoices updated: " + counter + "\n");
    } catch (Exception e) {
      // catch any possible exception and throw it as a Quartz
      // JobExecutionException
      throw new JobExecutionException(e.getMessage(), e);
    }
  }

  /**
   * Updates the days till due and last calculated on date fields of the invoice.
   * 
   * @param invoice
   * @throws OBException
   */
  public static void updateInvoice(Invoice invoice) throws OBException {
    OBContext.setAdminMode();
    try {
      OBInterceptor.setPreventUpdateInfoChange(true);
      HashMap<String, BigDecimal> oldFlowAmounts = new HashMap<String, BigDecimal>();
      // If the invoice has old flow's related payments calculate its statuses and amounts

      if (invoice.getFinancialMgmtDebtPaymentList() != null && invoice.getFinancialMgmtDebtPaymentList().size() > 0) {
        oldFlowAmounts = getOldflowAmounts(invoice.getFinancialMgmtDebtPaymentList(), invoice.getCurrency().getId(), invoice.getAccountingDate());
      } else {
        oldFlowAmounts.put("paidAmt", BigDecimal.ZERO);
        oldFlowAmounts.put("outstandingAmt", BigDecimal.ZERO);
        oldFlowAmounts.put("overdueAmt", BigDecimal.ZERO);
      }
      HashMap<String, BigDecimal> amounts = calculateAmounts(invoice);

      invoice.setTotalPaid(amounts.get("paidAmt").add(oldFlowAmounts.get("paidAmt")));
      invoice.setOutstandingAmount(amounts.get("outstandingAmt").add(oldFlowAmounts.get("outstandingAmt")));
      invoice.setPaymentComplete(invoice.getOutstandingAmount().compareTo(BigDecimal.ZERO) == 0);
      invoice.setDueAmount(amounts.get("overdueAmt").add(oldFlowAmounts.get("overdueAmt")));
      invoice.setDaysTillDue(getDaysTillDue(invoice));

      if (invoice.getOutstandingAmount().compareTo(BigDecimal.ZERO) == 0) {
        Date finalSettlementDate = getFinalSettlementDate(invoice);
        // If date is null invoice amount = 0 then nothing to set
        if (finalSettlementDate != null) {
          invoice.setFinalSettlementDate(finalSettlementDate);
          invoice.setDaysSalesOutstanding(FIN_Utility.getDaysBetween(invoice.getInvoiceDate(), finalSettlementDate));
        }
      }
      BigDecimal grandTotalAmount = invoice.getGrandTotalAmount();
      // This prevents division by ZERO
      if (grandTotalAmount.compareTo(BigDecimal.ZERO) == 0) {
        grandTotalAmount = BigDecimal.ONE;
      }
      invoice.setPercentageOverdue(amounts.get("overdue").multiply(new BigDecimal(100)).divide(grandTotalAmount, BigDecimal.ROUND_HALF_UP).longValue());
      invoice.setLastCalculatedOnDate(new Date());

      OBDal.getInstance().save(invoice);
      OBDal.getInstance().flush();

    } catch (Exception e) {
    } finally {
      OBInterceptor.setPreventUpdateInfoChange(false);
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns the date in which last payment for this invoice took place
   * 
   * @param invoice
   * @return
   */
  private static Date getFinalSettlementDate(Invoice invoice) {
    final OBCriteria<FIN_PaymentSchedInvV> obc = OBDal.getInstance().createCriteria(FIN_PaymentSchedInvV.class);
    // For Background process execution at system level
    if (OBContext.getOBContext().isInAdministratorMode()) {
      obc.setFilterOnReadableClients(false);
      obc.setFilterOnReadableOrganization(false);
    }
    obc.add(Restrictions.eq(FIN_PaymentSchedInvV.PROPERTY_INVOICE, invoice));
    obc.setProjection(Projections.max(FIN_PaymentSchedInvV.PROPERTY_LASTPAYMENT));
    Object o = obc.list().get(0);
    if (o != null) {
      return ((Date) o);
    } else {
      return null;
    }
  }

  private static HashMap<String, BigDecimal> getOldflowAmounts(List<DebtPayment> debtPayments, String currencyTo, Date conversionDate) {
    BigDecimal paidAmt = BigDecimal.ZERO;
    BigDecimal outstandingAmt = BigDecimal.ZERO;
    BigDecimal overdueAmt = BigDecimal.ZERO;
    for (DebtPayment debtPayment : debtPayments) {
      // Calculate paid amount.
      BigDecimal paid = calculatePaidAmount(debtPayment, currencyTo, conversionDate, BigDecimal.ONE);
      paidAmt = paidAmt.add(paid);
      // Calculate outstanding amount.
      outstandingAmt = outstandingAmt.add(debtPayment.getAmount().subtract(paid));
      // Calculate overdue amount.
      overdueAmt = overdueAmt.add(calculateOverdueAmount(debtPayment, currencyTo, conversionDate, BigDecimal.ONE));
    }
    HashMap<String, BigDecimal> amounts = new HashMap<String, BigDecimal>();
    amounts.put("paidAmt", paidAmt);
    amounts.put("outstandingAmt", outstandingAmt);
    amounts.put("overdueAmt", overdueAmt);
    return amounts;
  }

  private static HashMap<String, BigDecimal> calculateAmounts(Invoice invoice) {
    BigDecimal paidAmt = BigDecimal.ZERO;
    BigDecimal outstandingAmt = BigDecimal.ZERO;
    BigDecimal overdueAmt = BigDecimal.ZERO;
    BigDecimal overdue = BigDecimal.ZERO;
    // System.out.println("enter calculateAmounts");
    for (FIN_PaymentSchedule paymentSchedule : invoice.getFINPaymentScheduleList()) {
      // System.out.println("enter ps:" + paymentSchedule.getIdentifier());
      BigDecimal paid = BigDecimal.ZERO;
      for (FIN_PaymentScheduleDetail psd : paymentSchedule.getFINPaymentScheduleDetailInvoicePaymentScheduleList()) {
        // System.out.println("enter psd:" + psd.getIdentifier());

        if (psd.isCanceled()) {
          // If payment scheduled is cancelled don't consider its amount.
          continue;
        }
        // System.out.println("enter psd 1");
        boolean isexternalpayment = psd.isScoExternalpayment() != null ? psd.isScoExternalpayment() : false;

        if ((psd.getPaymentDetails() != null && FIN_Utility.isPaymentConfirmed(psd.getPaymentDetails().getFinPayment().getStatus(), psd)) || isexternalpayment) {
          // System.out.println("enter psd 2");

          paid = paid.add(psd.getAmount().add(psd.getWriteoffAmount()));
          // System.out.println("enter psd 3 paid:" + paid);

          if (psd.getPaymentDetails() != null && isexternalpayment) {
            // System.out.println("enter psd 4");

            // If an amount has been paid, let's check if any amount was paid late
            Date paymentDate = psd.getPaymentDetails().getFinPayment().getPaymentDate();
            Date dueDate = psd.getInvoicePaymentSchedule().getDueDate();
            if (paymentDate.after(dueDate)) {
              overdue = overdue.add(psd.getAmount());
            }
          }
        }
      }

      // System.out.println("end calculations paymentSchedule.getPaidAmount():" +
      // paymentSchedule.getPaidAmount() + " - paid:" + paid);

      if (paymentSchedule.getPaidAmount().compareTo(paid) != 0) {
        if (logger != null) {
          logger.log("ERROR Invoice " + invoice.getDocumentNo() + ": wrong payment plan info, paid amount is " + paymentSchedule.getPaidAmount().toPlainString() + " when it should be " + paid.toPlainString());
        }
        System.out.println("ERROR Invoice " + invoice.getDocumentNo() + ": wrong payment plan info, paid amount is " + paymentSchedule.getPaidAmount().toPlainString() + " when it should be " + paid.toPlainString());

        paymentSchedule.setPaidAmount(paid);
        OBDal.getInstance().save(paymentSchedule);
      }

      // System.out.println("end calculations 2 paymentSchedule.getOutstandingAmount():" +
      // paymentSchedule.getOutstandingAmount()+ " - paid:" + paid);

      if (paymentSchedule.getOutstandingAmount().compareTo(paymentSchedule.getAmount().subtract(paid)) != 0) {
        if (logger != null) {
          logger.log("ERROR Invoice " + invoice.getDocumentNo() + ": wrong payment plan info, outstanding amount is " + paymentSchedule.getOutstandingAmount().toPlainString() + " when it should be " + paymentSchedule.getAmount().subtract(paid).toPlainString());
        }
        System.out.println("ERROR Invoice " + invoice.getDocumentNo() + ": wrong payment plan info, outstanding amount is " + paymentSchedule.getOutstandingAmount().toPlainString() + " when it should be " + paymentSchedule.getAmount().subtract(paid).toPlainString());

        paymentSchedule.setOutstandingAmount(paymentSchedule.getAmount().subtract(paid));
        OBDal.getInstance().save(paymentSchedule);
      }

      if (paymentSchedule.getDueDate().before(new Date()) && paymentSchedule.getOutstandingAmount() != BigDecimal.ZERO) {
        overdueAmt = overdueAmt.add(paymentSchedule.getOutstandingAmount());
      }

      paidAmt = paidAmt.add(paymentSchedule.getPaidAmount());

      outstandingAmt = outstandingAmt.add(paymentSchedule.getOutstandingAmount());
      // System.out.println("end calculations 2 paidAmt:" + paidAmt+ " - outstandingAmt :" +
      // outstandingAmt );

    }
    HashMap<String, BigDecimal> amounts = new HashMap<String, BigDecimal>();
    amounts.put("paidAmt", paidAmt);
    amounts.put("outstandingAmt", outstandingAmt);
    amounts.put("overdueAmt", overdueAmt);
    amounts.put("overdue", overdue);
    return amounts;
  }

  private static Long getDaysTillDue(Invoice invoice) {
    // Calculate days till due
    final OBCriteria<FIN_PaymentSchedule> obc = OBDal.getInstance().createCriteria(FIN_PaymentSchedule.class);
    // For Background process execution at system level
    if (OBContext.getOBContext().isInAdministratorMode()) {
      obc.setFilterOnReadableClients(false);
      obc.setFilterOnReadableOrganization(false);
    }
    obc.add(Restrictions.eq(FIN_PaymentSchedule.PROPERTY_INVOICE, invoice));
    obc.add(Restrictions.ne(FIN_PaymentSchedule.PROPERTY_OUTSTANDINGAMOUNT, BigDecimal.ZERO));
    obc.setProjection(Projections.min(FIN_PaymentSchedule.PROPERTY_DUEDATE));
    Object o = obc.list().get(0);
    if (o != null) {
      return (FIN_Utility.getDaysToDue((Date) o));
    } else {
      return 0L;
    }
  }

  /**
   * Checks if the module is implementing the specified property.
   * 
   * @param property
   *          Value of the property.
   * @param moduleId
   *          Module identifier.
   * @return true: only if there is one preference for the module or if there are several only one
   *         can be mark as selected. false: in other cases.
   */
  private static boolean isPreferenceOfModule(String property, String moduleId) {

    final OBCriteria<Preference> obcNotSel = OBDal.getInstance().createCriteria(Preference.class);
    obcNotSel.add(Restrictions.eq(Preference.PROPERTY_PROPERTY, property));
    obcNotSel.setFilterOnReadableClients(false);
    obcNotSel.setFilterOnReadableOrganization(false);

    final OBCriteria<Preference> obcSel = OBDal.getInstance().createCriteria(Preference.class);
    obcSel.add(Restrictions.eq(Preference.PROPERTY_PROPERTY, property));
    obcSel.add(Restrictions.eq(Preference.PROPERTY_SELECTED, true));
    obcSel.setFilterOnReadableClients(false);
    obcSel.setFilterOnReadableOrganization(false);

    if (obcNotSel.list() != null && obcNotSel.list().size() == 1) {
      return obcNotSel.list().get(0).getModule().getId().equals(moduleId);
    } else if (obcSel.list() != null && obcSel.list().size() == 1) {
      return obcSel.list().get(0).getModule().getId().equals(moduleId);
    } else {
      return false;
    }
  }

  public static BigDecimal calculatePaidAmount(DebtPayment payment, String strCurrencyTo, Date conversionDate, BigDecimal multiplier) {
    BigDecimal paidAmount = BigDecimal.ZERO;
    String finPaymentStatus = getMigratedPaymentStatus(payment);
    if ("PAID".equals(finPaymentStatus)) {
      return getConvertedAmt(payment.getAmount().multiply(multiplier), payment.getCurrency().getId(), strCurrencyTo, conversionDate, payment.getClient().getId(), payment.getOrganization().getId());
    } else if ("NOTPAID".equals(finPaymentStatus)) {
      return BigDecimal.ZERO;
    } else if (payment.getSettlementCancelled() == null) {
      return paidAmount;
    } else if (payment.getSettlementCancelled().getProcessed().equals("Y")) {
      if (payment.isPaymentComplete())
        return getConvertedAmt(payment.getAmount().multiply(multiplier), payment.getCurrency().getId(), strCurrencyTo, conversionDate, payment.getClient().getId(), payment.getOrganization().getId());

      boolean paymentCompletelyPaid = true;
      for (DebtPayment cancelledPayment : payment.getSettlementCancelled().getFinancialMgmtDebtPaymentSettlementCancelledList()) {
        if (!cancelledPayment.isPaymentComplete() && cancelledPayment.getAmount().compareTo(cancelledPayment.getWriteoffAmount()) != 0 && getMigratedPaymentStatus(cancelledPayment).equals("NOTMIGRATED")) {
          // write off amount is equals to the payment's amount it is considered as paid
          paymentCompletelyPaid = false;
          break;
        } else if (getMigratedPaymentStatus(cancelledPayment).equals("NOTPAID")) {
          paymentCompletelyPaid = false;
          break;
        }
      }
      if (paymentCompletelyPaid) {
        // The sum of all canceled not paid payments in the settlement is zero. This means that the
        // payment has been paid completely, as it was canceled with some other pending payments
        // (for example, the ones comming from a credit memo)
        return getConvertedAmt(payment.getAmount().multiply(multiplier), payment.getCurrency().getId(), strCurrencyTo, conversionDate, payment.getClient().getId(), payment.getOrganization().getId());
      }

      List<DebtPayment> generatedPayments = payment.getSettlementCancelled().getFinancialMgmtDebtPaymentCSettlementGenerateIDList();
      if (generatedPayments == null || generatedPayments.size() == 0) {
        return getConvertedAmt(payment.getAmount().multiply(multiplier), payment.getCurrency().getId(), strCurrencyTo, conversionDate, payment.getClient().getId(), payment.getOrganization().getId());
      }
      BigDecimal generatedPaymentTotalAmount = BigDecimal.ZERO;
      BigDecimal generatedPaymentPaidAmount = BigDecimal.ZERO;
      for (DebtPayment generatedPayment : generatedPayments) {
        BigDecimal signMultiplier = generatedPayment.isReceipt() == payment.isReceipt() ? BigDecimal.ONE : BigDecimal.ONE.negate();
        generatedPaymentTotalAmount = generatedPaymentTotalAmount.add(getConvertedAmt(generatedPayment.getAmount(), generatedPayment.getCurrency().getId(), strCurrencyTo, conversionDate, generatedPayment.getClient().getId(), generatedPayment.getOrganization().getId()).multiply(signMultiplier));
        generatedPaymentPaidAmount = generatedPaymentPaidAmount.add(calculatePaidAmount(generatedPayment, strCurrencyTo, generatedPayment.getSettlementGenerate().getAccountingDate(), BigDecimal.ONE).multiply(signMultiplier));
      }
      if (generatedPaymentTotalAmount.compareTo(BigDecimal.ZERO) == 0) {
        return getConvertedAmt(payment.getAmount().multiply(multiplier), payment.getCurrency().getId(), strCurrencyTo, conversionDate, payment.getClient().getId(), payment.getOrganization().getId());
      }
      // payment amount * (generatedPaymentPaidAmount / generatedPaymentTotalAmount)
      BigDecimal paidAmountTmp = payment.getAmount().subtract(payment.getWriteoffAmount()).multiply(generatedPaymentPaidAmount).divide(generatedPaymentTotalAmount, BigDecimal.ROUND_HALF_UP);
      // set scale of the currency using standard precision
      paidAmount = paidAmount.add(paidAmountTmp.setScale(payment.getCurrency().getStandardPrecision().intValue(), BigDecimal.ROUND_HALF_UP));
      // Add payment's write off amount to the paid amount
      paidAmount = paidAmount.add(getConvertedAmt(payment.getWriteoffAmount(), payment.getCurrency().getId(), strCurrencyTo, conversionDate, payment.getClient().getId(), payment.getOrganization().getId()));
    }
    return paidAmount;
  }

  public static BigDecimal calculateOverdueAmount(DebtPayment payment, String strCurrencyTo, Date conversionDate, BigDecimal multiplier) {
    BigDecimal overdueAmount = BigDecimal.ZERO;

    if (payment.getDueDate().compareTo(new Date(System.currentTimeMillis())) > 0) {
      return BigDecimal.ZERO;
    } else if ("PAID".equals(getMigratedPaymentStatus(payment))) {
      return BigDecimal.ZERO;
    } else if ("NOTPAID".equals(getMigratedPaymentStatus(payment))) {
      return getConvertedAmt(payment.getAmount().multiply(multiplier), payment.getCurrency().getId(), strCurrencyTo, conversionDate, payment.getClient().getId(), payment.getOrganization().getId());
    } else if (payment.getSettlementCancelled() == null) {
      return getConvertedAmt(payment.getAmount().multiply(multiplier), payment.getCurrency().getId(), strCurrencyTo, conversionDate, payment.getClient().getId(), payment.getOrganization().getId());
    } else if (payment.isPaymentComplete()) {
      return BigDecimal.ZERO;
    } else if (payment.getSettlementCancelled() != null && payment.getSettlementCancelled().getProcessed().equals("Y")) {

      boolean paymentCompletelyPaid = true;
      for (DebtPayment cancelledPayment : payment.getSettlementCancelled().getFinancialMgmtDebtPaymentSettlementCancelledList()) {
        if (!cancelledPayment.isPaymentComplete() && cancelledPayment.getAmount().compareTo(cancelledPayment.getWriteoffAmount()) != 0 && getMigratedPaymentStatus(cancelledPayment).equals("NOTMIGRATED")) {
          // write off amount is equals to the payment's amount it is considered as paid
          paymentCompletelyPaid = false;
          break;
        } else if (getMigratedPaymentStatus(cancelledPayment).equals("NOTPAID")) {
          paymentCompletelyPaid = false;
          break;
        }
      }
      if (paymentCompletelyPaid) {
        // The sum of all canceled not paid payments in the settlement is zero. This means that the
        // payment has been paid completely, as it was canceled with some other pending payments
        // (for example, the ones comming from a credit memo)
        return BigDecimal.ZERO;
      }
      List<DebtPayment> generatedPayments = payment.getSettlementCancelled().getFinancialMgmtDebtPaymentCSettlementGenerateIDList();
      if (generatedPayments == null || generatedPayments.size() == 0) {
        return BigDecimal.ZERO;
      }
      BigDecimal generatedPaymentTotalAmount = BigDecimal.ZERO;
      BigDecimal generatedPaymentOverdueAmount = BigDecimal.ZERO;
      for (DebtPayment generatedPayment : generatedPayments) {
        BigDecimal signMultiplier = generatedPayment.isReceipt() == payment.isReceipt() ? BigDecimal.ONE : BigDecimal.ONE.negate();
        generatedPaymentTotalAmount = generatedPaymentTotalAmount.add(getConvertedAmt(generatedPayment.getAmount(), generatedPayment.getCurrency().getId(), strCurrencyTo, conversionDate, generatedPayment.getClient().getId(), generatedPayment.getOrganization().getId()).multiply(signMultiplier));
        if (generatedPayment.isPaymentComplete()) {
          continue;
        }
        generatedPaymentOverdueAmount = generatedPaymentOverdueAmount.add(calculateOverdueAmount(generatedPayment, strCurrencyTo, generatedPayment.getSettlementGenerate().getAccountingDate(), BigDecimal.ONE).multiply(signMultiplier));
      }
      if (generatedPaymentTotalAmount.compareTo(BigDecimal.ZERO) == 0) {
        return BigDecimal.ZERO;
      }
      // payment amount * (generatedPaymentOverdueAmount / generatedPaymentTotalAmount)
      BigDecimal overdueAmountTmp = payment.getAmount().multiply(generatedPaymentOverdueAmount).divide(generatedPaymentTotalAmount, BigDecimal.ROUND_HALF_UP);
      // set scale of the currency using standard precision
      overdueAmount = overdueAmount.add(overdueAmountTmp.setScale(payment.getCurrency().getStandardPrecision().intValue(), RoundingMode.HALF_UP));
    }
    return overdueAmount;
  }

  public static BigDecimal getConvertedAmt(BigDecimal Amt, String CurFrom_ID, String CurTo_ID, Date ConvDate, String client, String org) {
    if (CurFrom_ID == null || CurTo_ID == null || CurFrom_ID.equals(CurTo_ID))
      return Amt;
    ConnectionProvider conn = new DalConnectionProvider(false);

    String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty("dateFormat.java");
    SimpleDateFormat dateFormater = new SimpleDateFormat(dateFormat);
    String strConvertedAmount = AcctServer.getConvertedAmt(Amt.toString(), CurFrom_ID, CurTo_ID, dateFormater.format(ConvDate).toString(), "S", client, org, conn);
    return new BigDecimal(strConvertedAmount);
  }

  private static String getMigratedPaymentStatus(DebtPayment payment) {
    String status = "NOTMIGRATED";

    if (payment.getEntity().hasProperty("aPRMTPayment")) {
      final FIN_Payment migratedPayment = (FIN_Payment) payment.get("aPRMTPayment");
      if (migratedPayment != null) {
        if (FIN_Utility.isPaymentConfirmed(migratedPayment.getStatus(), null)) {
          status = "PAID";
        } else {
          status = "NOTPAID";
        }
      }
    }
    return status;
  }
}
