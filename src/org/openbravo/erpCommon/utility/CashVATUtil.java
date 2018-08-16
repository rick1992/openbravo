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
 * All portions are Copyright (C) 2013-2014 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.utility;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBDao;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.ad_forms.AcctSchema;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.ad_forms.DocLineCashVATReady_PaymentTransactionReconciliation;
import org.openbravo.erpCommon.ad_forms.DocTax;
import org.openbravo.erpCommon.ad_forms.Fact;
import org.openbravo.erpCommon.ad_forms.FactLine;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceTax;
import org.openbravo.model.common.invoice.InvoiceTaxCashVAT;
import org.openbravo.model.common.invoice.InvoiceTaxCashVAT_V;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.tax.TaxRate;

public class CashVATUtil {

  private static Logger log4j = Logger.getLogger(CashVATUtil.class);

  public static final BigDecimal _100 = new BigDecimal("100");

  /**
   * Returns the associated legal entity Cash VAT configuration. Useful for sales flows
   * 
   * @param strOrgId
   *          organization id
   * @return "Y", "N" or null if not found
   */
  public static String getOrganizationIsCashVAT(final String strOrgId) {
    try {
      OBContext.setAdminMode(true);
      final Organization org = OBDal.getInstance().get(Organization.class, strOrgId);
      final Organization legalEntity = OBContext.getOBContext().getOrganizationStructureProvider(org.getClient().getId()).getLegalEntity(org);
      if (legalEntity != null && legalEntity.getOrganizationInformationList() != null && !legalEntity.getOrganizationInformationList().isEmpty()) {
        return legalEntity.getOrganizationInformationList().get(0).isCashVAT() ? "Y" : "N";
      }
    } catch (final Exception e) {
      log4j.error("Error getting organization'" + strOrgId + "' cash vat. Returning null", e);
    } finally {
      OBContext.restorePreviousMode();
    }

    return null;
  }

  /**
   * Returns the associated legal entity Double Cash Criteria configuration. Useful for purchase
   * flows
   * 
   * @param strOrgId
   *          organization id
   * @return "Y", "N" or null if not found
   */
  public static String getOrganizationIsDoubleCash(final String strOrgId) {
    try {
      OBContext.setAdminMode(true);
      final Organization org = OBDal.getInstance().get(Organization.class, strOrgId);
      final Organization legalEntity = OBContext.getOBContext().getOrganizationStructureProvider(org.getClient().getId()).getLegalEntity(org);
      if (legalEntity != null && legalEntity.getOrganizationInformationList() != null && !legalEntity.getOrganizationInformationList().isEmpty()) {
        return legalEntity.getOrganizationInformationList().get(0).isDoubleCash() ? "Y" : "N";
      }
    } catch (final Exception e) {
      log4j.error("Error getting organization'" + strOrgId + "' double cash. Returning null", e);
    } finally {
      OBContext.restorePreviousMode();
    }

    return null;
  }

  /**
   * Returns the Cash VAT configuration for the given Vendor (Business Partner)
   * 
   * @param strBPId
   *          Vendor (c_bpartner_id)
   * @return "Y", "N" or null if not found
   */
  public static String getBusinessPartnerIsCashVAT(final String strBPId) {
    try {
      OBContext.setAdminMode(true);
      final BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class, strBPId);
      if (bp != null) {
        return bp.isCashVAT() ? "Y" : "N";
      }
    } catch (final Exception e) {
      log4j.error("Error getting business partner'" + strBPId + "' cash vat. Returning null", e);
    } finally {
      OBContext.restorePreviousMode();
    }

    return null;
  }

  /**
   * Creates the records into the Cash VAT management table (InvoiceTaxCashVAT), calculating the
   * percentage paid/collected tax amount and taxable amount. Only for cash vat tax rates
   * 
   */
  public static void createInvoiceTaxCashVAT(final FIN_PaymentDetail paymentDetail, final FIN_PaymentSchedule paymentSchedule, final BigDecimal amount) {
    System.out.println("amount:" + amount);
    try {
      OBContext.setAdminMode(true);
      if (paymentSchedule == null)
        return;

      final Invoice invoice = paymentSchedule.getInvoice();
      if (invoice != null/* && invoice.isCashVAT() */) {
        // A previous cash vat line with this payment detail means we are reactivating the payment.
        // In this case we delete the line
        final List<InvoiceTaxCashVAT> previousITCashVATs = getInvoiceTaxCashVAT(paymentDetail);
        if (previousITCashVATs != null && !previousITCashVATs.isEmpty()) {
          for (InvoiceTaxCashVAT previousITCV : previousITCashVATs) {
            OBDal.getInstance().remove(previousITCV);
          }
        } else {
          final boolean calculateAmountsBasedOnPercentage;
          BigDecimal percentage = null; /* Calculate it later on */
          final BigDecimal outstandingAmt = invoice.getOutstandingAmount();
          if (outstandingAmt.compareTo(amount) == 0) {
            // We are fully paying the invoice. We need to subtract amounts instead of calculating
            // them on the fly
            calculateAmountsBasedOnPercentage = false;
          } else {
            // Calculate amounts based on the paid percentage
            calculateAmountsBasedOnPercentage = true;
            final BigDecimal grandTotalAmt = invoice.getGrandTotalAmount();
            final int currencyPrecission = invoice.getCurrency().getStandardPrecision().intValue();
            percentage = amount.multiply(_100).divide(grandTotalAmt, currencyPrecission, RoundingMode.HALF_UP).abs();
          }

          for (final InvoiceTax invoiceTax : invoice.getInvoiceTaxList()) {
            if (invoiceTax.getTax().isCashVAT()) {
              final InvoiceTaxCashVAT iTCashVAT = OBProvider.getInstance().get(InvoiceTaxCashVAT.class);
              iTCashVAT.setOrganization(invoiceTax.getOrganization());
              iTCashVAT.setInvoiceTax(invoiceTax);
              iTCashVAT.setFINPaymentDetail(paymentDetail);
              final BigDecimal taxAmount;
              final BigDecimal taxableAmount;
              if (calculateAmountsBasedOnPercentage) {
                taxAmount = calculatePercentageAmount(percentage, invoiceTax.getTaxAmount(), invoice.getCurrency());
                taxableAmount = calculatePercentageAmount(percentage, invoiceTax.getTaxableAmount(), invoice.getCurrency());
              } else {
                final Map<String, BigDecimal> outstandingAmounts = getTotalOutstandingCashVATAmount(invoiceTax.getId());
                percentage = outstandingAmounts.get("percentage");
                taxAmount = outstandingAmounts.get("taxAmt");
                taxableAmount = outstandingAmounts.get("taxableAmt");
              }
              iTCashVAT.setPercentage(percentage);
              iTCashVAT.setTaxAmount(taxAmount);
              iTCashVAT.setTaxableAmount(taxableAmount);
              invoiceTax.getInvoiceTaxCashVATList().add(iTCashVAT);
              OBDal.getInstance().save(invoiceTax);
              OBDal.getInstance().save(iTCashVAT);
            }
          }
        }
        OBDal.getInstance().flush();
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Gets the InvoiceTaxCashVAT records linked to the payment detail
   */
  public static List<InvoiceTaxCashVAT> getInvoiceTaxCashVAT(final FIN_PaymentDetail paymentDetail) {
    try {
      OBContext.setAdminMode(true);

      List<FIN_PaymentDetail> lsDetails = new Vector<FIN_PaymentDetail>();
      lsDetails.add(paymentDetail);

      List<FIN_PaymentDetail> details = paymentDetail.getFinPayment().getFINPaymentDetailList();
      String invoiceScheduleId = null;
      if (paymentDetail.getFINPaymentScheduleDetailList().get(0).getInvoicePaymentSchedule() != null)
        invoiceScheduleId = paymentDetail.getFINPaymentScheduleDetailList().get(0).getInvoicePaymentSchedule().getId();

      if (invoiceScheduleId != null) {
        for (int i = 0; i < details.size(); i++) {
          if (!details.get(i).getId().equals(paymentDetail.getId()) && details.get(i).getFINPaymentScheduleDetailList().size() > 0 && details.get(i).getFINPaymentScheduleDetailList().get(0).getInvoicePaymentSchedule() != null && details.get(i).getFINPaymentScheduleDetailList().get(0).getInvoicePaymentSchedule().getId().equals(invoiceScheduleId)) {
            // combinar todos los schedules de un mismo invoice para generar una sola percepcion
            lsDetails.add(details.get(i));
          }
        }
      }

      return OBDao.getFilteredCriteria(InvoiceTaxCashVAT.class, Restrictions.in(InvoiceTaxCashVAT.PROPERTY_FINPAYMENTDETAIL, lsDetails)).list();

    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns the outstanding percentage, tax amount and taxable amount by subtracting the total
   * percentage (100%), total tax amount and total taxable amount with the amounts registered into
   * the InvoiceTaxCashVAT.
   * 
   * Use this method when the invoice is fully paid to avoid rounding issues with on the fly
   * calculations based on the percentage of the invoice that has been paid/collected
   * 
   */
  public static Map<String, BigDecimal> getTotalOutstandingCashVATAmount(final String cInvoiceTaxID) {
    try {
      OBContext.setAdminMode(true);
      final StringBuffer hql = new StringBuffer();
      hql.append(" select 100 - sum(coalesce(itcv." + InvoiceTaxCashVAT_V.PROPERTY_PERCENTAGE + ", 0)) as percentage, ");
      hql.append("  max(it." + InvoiceTax.PROPERTY_TAXABLEAMOUNT + ") - sum(coalesce(itcv." + InvoiceTaxCashVAT_V.PROPERTY_TAXABLEAMOUNT + ", 0)) as taxableAmt, ");
      hql.append("  max(it." + InvoiceTax.PROPERTY_TAXAMOUNT + ") - sum(coalesce(itcv." + InvoiceTaxCashVAT_V.PROPERTY_TAXAMOUNT + ", 0)) as taxAmt ");

      hql.append(" from " + InvoiceTaxCashVAT_V.ENTITY_NAME + " as itcv ");
      hql.append(" right outer join itcv." + InvoiceTaxCashVAT_V.PROPERTY_INVOICETAX + " as it ");
      hql.append(" where it." + InvoiceTax.PROPERTY_ID + "= :cInvoiceTaxID ");
      hql.append(" and coalesce(itcv." + InvoiceTaxCashVAT_V.PROPERTY_CANCELED + ", 'N') = 'N' ");

      final Session session = OBDal.getInstance().getSession();
      final Query query = session.createQuery(hql.toString());
      query.setParameter("cInvoiceTaxID", cInvoiceTaxID);

      final Map<String, BigDecimal> result = new HashMap<String, BigDecimal>();

      final Object[] o = (Object[]) query.uniqueResult();
      result.put("percentage", (BigDecimal) o[0]);
      result.put("taxableAmt", (BigDecimal) o[1]);
      result.put("taxAmt", (BigDecimal) o[2]);
      return result;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns the total percentage that should be applied to the cash vat regime that comes from a
   * prepayment, i.e. from an (partially or totally) paid/collected order. This percentage must be
   * directly registered into the final tax account instead of the transitory tax account as usual,
   * because this part of the invoice has been paid from the order
   */
  public static BigDecimal calculatePrepaidPercentageForCashVATTax(final String cTaxID, final String cInvoiceId) {
    try {
      OBContext.setAdminMode(true);
      final StringBuffer hql = new StringBuffer();
      hql.append(" select coalesce(sum(" + InvoiceTaxCashVAT_V.PROPERTY_PERCENTAGE + "), 0) ");
      hql.append(" from " + InvoiceTaxCashVAT_V.ENTITY_NAME);
      hql.append(" where " + InvoiceTaxCashVAT_V.PROPERTY_INVOICETAX + "." + InvoiceTax.PROPERTY_TAX + "." + TaxRate.PROPERTY_ID + " = :taxId ");
      hql.append(" and " + InvoiceTaxCashVAT_V.PROPERTY_INVOICETAX + "." + InvoiceTax.PROPERTY_INVOICE + "." + Invoice.PROPERTY_ID + " = :invoiceId ");
      hql.append(" and " + InvoiceTaxCashVAT_V.PROPERTY_CANCELED + " = false ");
      hql.append(" and " + InvoiceTaxCashVAT_V.PROPERTY_PAYMENTDETAILS + "." + FIN_PaymentDetail.PROPERTY_ISPREPAYMENT + " = true ");
      hql.append(" group by " + InvoiceTaxCashVAT_V.PROPERTY_INVOICETAX + "." + InvoiceTax.PROPERTY_TAX + "." + TaxRate.PROPERTY_ID + ", " + InvoiceTaxCashVAT_V.PROPERTY_INVOICETAX + "." + InvoiceTax.PROPERTY_INVOICE + "." + Invoice.PROPERTY_ID);

      final Session session = OBDal.getInstance().getSession();
      final Query query = session.createQuery(hql.toString());
      query.setParameter("taxId", cTaxID);
      query.setParameter("invoiceId", cInvoiceId);
      final BigDecimal percentage = (BigDecimal) query.uniqueResult();
      return percentage == null ? BigDecimal.ZERO : percentage;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Generic method to calculate the percentage of an amount (totalAmt) with the currency's standard
   * precision
   * 
   * @param percentage
   *          percentage to apply for the totalAmt
   * @param totalAmt
   *          total amount (represents 100%)
   * @param cCurrencyId
   *          currency ID
   * @return percentage * totalAmt / 100, rounded to the currency's standard precision
   */
  public static BigDecimal calculatePercentageAmount(final BigDecimal percentage, final BigDecimal totalAmt, final String cCurrencyId) {
    try {
      OBContext.setAdminMode(true);
      final Currency currency = OBDal.getInstance().get(Currency.class, cCurrencyId);
      return calculatePercentageAmount(percentage, totalAmt, currency);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Generic method to calculate the percentage of an amount (totalAmt) with the currency's standard
   * precision
   * 
   * @param percentage
   *          percentage to apply for the totalAmt
   * @param totalAmt
   *          total amount (represents 100%)
   * @param currency
   *          currency
   * @return percentage * totalAmt / 100, rounded to the currency's standard precision
   */
  public static BigDecimal calculatePercentageAmount(final BigDecimal percentage, final BigDecimal totalAmt, final Currency currency) {
    try {
      OBContext.setAdminMode(true);
      if (currency != null) {
        int precission = currency.getStandardPrecision().intValue();
        return percentage.multiply(totalAmt).divide(_100, precission, RoundingMode.HALF_UP);
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    throw new RuntimeException("CashVATUtil.calculatePercentageAmount(), wrong parameters :" + percentage + ", " + totalAmt + ", " + currency);
  }

  /**
   * Create the accounting fact lines related to Cash VAT for payments, transactions and
   * reconciliations that come from a cash VAT invoice
   * 
   */
  public static String createFactCashVAT(AcctSchema as, ConnectionProvider conn, Fact fact, String Fact_Acct_Group_ID, DocLineCashVATReady_PaymentTransactionReconciliation line, Invoice invoice, final String documentType, final String cCurrencyID, final String SeqNo, List<String> lsParameters, BigDecimal conversionRate) {

    BigDecimal totalCashVat = new BigDecimal(0);

    String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty("dateFormat.java");
    SimpleDateFormat outputFormat = new SimpleDateFormat(dateFormat);
    String dateAcct = outputFormat.format(invoice.getAccountingDate());

    if (/* invoice.isCashVAT() && */!line.getInvoiceTaxCashVAT_V().isEmpty()) {
      FactLine factLine2 = null;
      for (final InvoiceTaxCashVAT_V itcv : line.getInvoiceTaxCashVAT_V()) {
        final TaxRate tax = itcv.getInvoiceTax().getTax();
        if (tax.isCashVAT()) {
          final BigDecimal taxAmt = itcv.getTaxAmount();

          totalCashVat = totalCashVat.add(taxAmt);

          final DocTax m_tax = new DocTax(tax.getId(), tax.getName(), tax.getRate().toString(), itcv.getInvoiceTax().getTaxableAmount().toString(), itcv.getTaxAmount().toString(), tax.isNotTaxdeductable(), tax.isTaxdeductable());
          final String invoicedocumentType = invoice.getDocumentType().getDocumentCategory();
          final boolean isReversal = invoice.getDocumentType().isReversal();
          // ARI, ARF, ARI_RM
          if (invoicedocumentType.equals(AcctServer.DOCTYPE_ARInvoice) || invoicedocumentType.equals(AcctServer.DOCTYPE_ARProForma) || invoicedocumentType.equals(AcctServer.DOCTYPE_RMSalesInvoice)) {
            if (isReversal) {
              final FactLine factLine1 = fact.createLine(line, m_tax.getAccount(DocTax.ACCTTYPE_TaxDue_Trans, as, conn), cCurrencyID, "", taxAmt.toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo), documentType, dateAcct, conversionRate, conn);
              factLine2 = fact.createLine(line, m_tax.getAccount(DocTax.ACCTTYPE_TaxDue, as, conn), cCurrencyID, taxAmt.toString(), "", Fact_Acct_Group_ID, nextSeqNo(factLine1.m_SeqNo), documentType, dateAcct, conversionRate, conn);
            } else {
              final FactLine factLine1 = fact.createLine(line, m_tax.getAccount(DocTax.ACCTTYPE_TaxDue_Trans, as, conn), cCurrencyID, taxAmt.toString(), "", Fact_Acct_Group_ID, nextSeqNo(SeqNo), documentType, dateAcct, conversionRate, conn);
              factLine2 = fact.createLine(line, m_tax.getAccount(DocTax.ACCTTYPE_TaxDue, as, conn), cCurrencyID, "", taxAmt.toString(), Fact_Acct_Group_ID, nextSeqNo(factLine1.m_SeqNo), documentType, dateAcct, conversionRate, conn);
            }
          }// ARC
          else if (invoicedocumentType.equals(AcctServer.DOCTYPE_ARCredit)) {
            final FactLine factLine1 = fact.createLine(line, m_tax.getAccount(DocTax.ACCTTYPE_TaxDue_Trans, as, conn), cCurrencyID, "", taxAmt.toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo), documentType, dateAcct, conversionRate, conn);
            factLine2 = fact.createLine(line, m_tax.getAccount(DocTax.ACCTTYPE_TaxDue, as, conn), cCurrencyID, taxAmt.toString(), "", Fact_Acct_Group_ID, nextSeqNo(factLine1.m_SeqNo), documentType, dateAcct, conversionRate, conn);
          }
          // API
          else if (invoicedocumentType.equals(AcctServer.DOCTYPE_APInvoice)) {
            if (isReversal) {
              final FactLine factLine1 = fact.createLine(line, m_tax.getAccount(DocTax.ACCTTYPE_TaxCredit_Trans, as, conn), cCurrencyID, taxAmt.toString(), "", Fact_Acct_Group_ID, nextSeqNo(SeqNo), documentType, dateAcct, conversionRate, conn);
              factLine2 = fact.createLine(line, m_tax.getAccount(DocTax.ACCTTYPE_TaxCredit, as, conn), cCurrencyID, "", taxAmt.toString(), Fact_Acct_Group_ID, nextSeqNo(factLine1.m_SeqNo), documentType, dateAcct, conversionRate, conn);
            } else {
              final FactLine factLine1 = fact.createLine(line, m_tax.getAccount(DocTax.ACCTTYPE_TaxCredit_Trans, as, conn), cCurrencyID, "", taxAmt.toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo), documentType, dateAcct, conversionRate, conn);
              factLine2 = fact.createLine(line, m_tax.getAccount(DocTax.ACCTTYPE_TaxCredit, as, conn), cCurrencyID, taxAmt.toString(), "", Fact_Acct_Group_ID, nextSeqNo(factLine1.m_SeqNo), documentType, dateAcct, conversionRate, conn);
            }
          }
          // APC
          else if (invoicedocumentType.equals(AcctServer.DOCTYPE_APCredit)) {
            final FactLine factLine1 = fact.createLine(line, m_tax.getAccount(DocTax.ACCTTYPE_TaxCredit_Trans, as, conn), cCurrencyID, taxAmt.toString(), "", Fact_Acct_Group_ID, nextSeqNo(SeqNo), documentType, null, conversionRate, conn);
            factLine2 = fact.createLine(line, m_tax.getAccount(DocTax.ACCTTYPE_TaxCredit, as, conn), cCurrencyID, "", taxAmt.toString(), Fact_Acct_Group_ID, nextSeqNo(factLine1.m_SeqNo), documentType, null, conversionRate, conn);
          }
        }
      }
      if (factLine2 != null) {
        lsParameters.add(totalCashVat.toString());
        return factLine2.m_SeqNo;
      }
    }

    lsParameters.add(totalCashVat.toString());
    return SeqNo;
  }

  private static String nextSeqNo(String oldSeqNo) {
    BigDecimal seqNo = new BigDecimal(oldSeqNo);
    return (seqNo.add(new BigDecimal("10"))).toString();

  }
}
