package pe.com.unifiedgo.accounting;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBDao;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.common.invoice.InvoiceTax;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.gl.GLItemAccounts;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.service.db.DalConnectionProvider;

import pe.com.unifiedgo.accounting.data.SCOBoeFrom;
import pe.com.unifiedgo.accounting.data.SCOPaymentHistory;
import pe.com.unifiedgo.accounting.data.SCOPercepPurch;
import pe.com.unifiedgo.accounting.data.SCOPercepSales;
import pe.com.unifiedgo.accounting.data.SCOPerceppurchDetail;
import pe.com.unifiedgo.accounting.data.SCOPercepsalesDetail;
import pe.com.unifiedgo.accounting.data.SCOPwithhoRecLine;
import pe.com.unifiedgo.accounting.data.SCOPwithholdingReceipt;
import pe.com.unifiedgo.accounting.data.SCOSwithhoRecLine;
import pe.com.unifiedgo.accounting.data.SCOSwithholdingReceipt;
import pe.com.unifiedgo.core.data.SCRComboItem;

public class SunatUtil {

  public static final BigDecimal _100 = new BigDecimal("100");

  public static BigDecimal calculatePercentageAmount(final BigDecimal percentage,
      final BigDecimal totalAmt, final Currency currency) {
    try {
      OBContext.setAdminMode(true);
      if (currency != null) {
        int precission = currency.getStandardPrecision().intValue();
        return percentage.multiply(totalAmt).divide(_100, precission, RoundingMode.HALF_UP);
      } else {
        return percentage.multiply(totalAmt).divide(_100, 16, RoundingMode.HALF_UP);

      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public static BigDecimal createEntryForSunatReceipt(final FIN_PaymentDetail paymentDetail,
      final FIN_PaymentSchedule paymentSchedule, final BigDecimal amount, String docaction) {
    try {

      BigDecimal percepcionAmt = new BigDecimal(0);
      boolean generate = true;

      OBContext.setAdminMode(true);

      final Invoice invoice = paymentSchedule.getInvoice();

      if (invoice == null)
        return new BigDecimal(0);

      String scoSpecialDoctype = invoice.getDocumentType().getScoSpecialdoctype();

      if (invoice != null && scoSpecialDoctype != null
          && (scoSpecialDoctype.equals("SCOARINVOICE") || scoSpecialDoctype.equals("SCOARTICKET")
              || scoSpecialDoctype.equals("SCOARCREDITMEMO")
              || scoSpecialDoctype.equals("SCOARDEBITMEMO")
              || scoSpecialDoctype.equals("SCOARBOEINVOICE"))) {

        generate = docaction.equals("P") || docaction.equals("D");

        if (generate) {
          final boolean calculateAmountsBasedOnPercentage;
          boolean noexiste = true;

          final BigDecimal outstandingAmt = invoice.getOutstandingAmount();

          calculateAmountsBasedOnPercentage = true;
          final BigDecimal grandTotalAmt = invoice.getGrandTotalAmount();
          final int currencyPrecission = invoice.getCurrency().getStandardPrecision().intValue();
          BigDecimal percentage = amount.multiply(_100)
              .divide(grandTotalAmt, currencyPrecission, RoundingMode.HALF_UP).abs();

          List<SCOPercepsalesDetail> otherTaxReceipts = OBDao
              .getFilteredCriteria(SCOPercepsalesDetail.class,
                  Restrictions.eq(SCOPercepsalesDetail.PROPERTY_INVOICEREF, invoice))
              .list();

          // existe de pagos de este mismo invoice ya los recibos, asi que ya no generarlos de
          // nuevo
          if (otherTaxReceipts.size() > 0) {

            for (int i = 0; i < otherTaxReceipts.size(); i++) {

              SCOPercepSales percepcion = otherTaxReceipts.get(i).getSCOPercepSales();
              for (int j = 0; j < percepcion.getSCOPercepsalesDetailList().size(); j++) {
                if (percepcion.getDocumentStatus().equals("VO"))
                  continue;

                SCOPercepsalesDetail percepLine = percepcion.getSCOPercepsalesDetailList().get(j);
                if (percepLine.getPaymentDetails().getFinPayment().getId()
                    .equals(paymentDetail.getFinPayment().getId())
                    && percepLine.getInvoiceref().getId().equals(invoice.getId())) {
                  noexiste = false;
                }
              }
            }
          }

          if (noexiste) {

            for (final InvoiceTax invoiceTax : invoice.getInvoiceTaxList()) {

              if (invoiceTax.getTax().getScoSpecialtax().equals("SCOSCREDITPERCEPTION")) {

                if (invoiceTax.getTaxAmount().compareTo(new BigDecimal(0)) == 0) {
                  continue;
                }

                BigDecimal taxAmount = new BigDecimal(0);
                if (calculateAmountsBasedOnPercentage) {
                  taxAmount = calculatePercentageAmount(percentage, invoiceTax.getTaxAmount(),
                      invoice.getCurrency());
                }

                // get previous payments
                BigDecimal previousPercepPayments = new BigDecimal(0);

                for (int k = 0; k < otherTaxReceipts.size(); k++)
                  if (otherTaxReceipts.get(k).getSCOPercepSales().getDocumentStatus()
                      .equals("VO") == false) {
                    previousPercepPayments = previousPercepPayments
                        .add(otherTaxReceipts.get(k).getAmount());
                  }
                // remaining
                BigDecimal remaining = new BigDecimal(0);
                remaining = invoiceTax.getTaxAmount().subtract(previousPercepPayments);

                if (taxAmount.compareTo(remaining) > 0) {
                  taxAmount = remaining;
                }

                if (taxAmount.compareTo(new BigDecimal(0)) == 0) {
                  continue;
                }

                final SCOPercepSales iTSunat = OBProvider.getInstance().get(SCOPercepSales.class);
                iTSunat.setOrganization(invoiceTax.getOrganization());
                iTSunat.setBusinessPartner(invoice.getBusinessPartner());

                // get bplocation
                Location loc = null;
                if (invoice.getBusinessPartner().getBusinessPartnerLocationList().size() != 0) {
                  Location locDefault = null;
                  for (int ij = 0; ij < invoice.getBusinessPartner()
                      .getBusinessPartnerLocationList().size(); ij++) {
                    Location locaux = invoice.getBusinessPartner().getBusinessPartnerLocationList()
                        .get(ij);
                    if (locaux.isScoNullLocation())
                      locDefault = locaux;
                    else if (locaux.isInvoiceToAddress()) {
                      loc = locaux;
                      break;
                    }
                  }
                  if (loc == null)
                    loc = locDefault;
                }
                iTSunat.setPartnerAddress(loc);

                iTSunat.setSalesTransaction(true);
                iTSunat.setDocumentNo(SCO_Utils.getDocumentNo(invoiceTax.getOrganization(),
                    "SCOSPERCEPTIONRECEIPT", "SCO_Percep_Sales"));
                iTSunat.setDocumentStatus("DR");
                iTSunat.setDocumentAction("CO");
                iTSunat.setProcessed(false);
                iTSunat.setProcessNow(false);
                iTSunat.setDocumentType(OBDal.getInstance().get(DocumentType.class, "0"));
                iTSunat.setTransactionDocument(SCO_Utils
                    .getDocTypeFromSpecial(invoiceTax.getOrganization(), "SCOSPERCEPTIONRECEIPT"));
                iTSunat.setGeneratedDate(paymentDetail.getFinPayment().getPaymentDate());
                iTSunat.setCurrency(
                    OBDal.getInstance().get(Currency.class, Utility.stringBaseCurrencyId(
                        new DalConnectionProvider(), invoice.getClient().getId())));
                iTSunat.setPnumberautogen(false);
                iTSunat.setPerceptionNumber("");

                OBDal.getInstance().save(iTSunat);
                BigDecimal amt = new BigDecimal(0);
                Long line = new Long(10);

                SCOPercepsalesDetail perception_line = OBProvider.getInstance()
                    .get(SCOPercepsalesDetail.class);
                perception_line.setOrganization(invoiceTax.getOrganization());
                perception_line.setLineNo(line);
                perception_line.setSCOPercepSales(iTSunat);
                perception_line.setDoctyperef(invoice.getTransactionDocument());
                perception_line.setInvoiceref(invoice);
                perception_line.setInvoicetaxref(invoiceTax);
                perception_line.setPaymentDetails(paymentDetail);
                perception_line.setAmount(taxAmount);
                perception_line.setBusinessPartner(invoice.getBusinessPartner());
                perception_line.setPaymentAmount(amount);
                if (!invoice.getCurrency().getId().equals("308"))// Soles
                {
                  BigDecimal conversionRate = paymentDetail.getFinPayment()
                      .getFinancialTransactionConvertRate();
                  String idPaymentCurr = paymentDetail.getFinPayment().getAccount().getCurrency()
                      .getId();

                  if (conversionRate != null && conversionRate.compareTo(BigDecimal.ONE) != 0) {
                    if (idPaymentCurr.equals(invoice.getCurrency().getId())) {
                      perception_line.setTipoDeCambio(conversionRate);
                    } else {
                      perception_line.setTipoDeCambio(
                          BigDecimal.ONE.divide(conversionRate, 5, RoundingMode.HALF_UP));
                    }
                  } else {
                    // segun la fecha
                    DalConnectionProvider conn = new DalConnectionProvider();

                    if (paymentDetail.getFinPayment().isReceipt()) {// cobro, entonces T/C venta

                      System.out.println("SS: " + new BigDecimal(10000).toString() + " "
                          + invoice.getCurrency().getId() + " "
                          + OBDateUtils.formatDate(paymentDetail.getFinPayment().getPaymentDate()));

                      BigDecimal tc = new BigDecimal(AcctServer.getConvertedAmt(
                          new BigDecimal(10000).toString(), invoice.getCurrency().getId(), "308",
                          OBDateUtils.formatDate(paymentDetail.getFinPayment().getPaymentDate()),
                          "", invoice.getClient().getId(), "", conn));

                      perception_line.setTipoDeCambio(
                          tc.divide(new BigDecimal(10000), 5, RoundingMode.HALF_UP));

                    } else {

                      BigDecimal amtFrom = new BigDecimal(AcctServer.getConvertedAmt(
                          new BigDecimal(10000).toString(), "308", invoice.getCurrency().getId(),
                          OBDateUtils.formatDate(paymentDetail.getFinPayment().getPaymentDate()),
                          "", invoice.getClient().getId(), "", conn));

                      perception_line.setTipoDeCambio(
                          new BigDecimal(10000).divide(amtFrom, 5, RoundingMode.HALF_UP));
                    }
                  }
                }
                OBDal.getInstance().save(perception_line);

                percepcionAmt = percepcionAmt.add(taxAmount);

              } else if (invoiceTax.getTax().getScoSpecialtax().equals("SCOSINMEDIATEPERCEPTION")) {
                BigDecimal taxAmount = new BigDecimal(0);
                taxAmount = calculatePercentageAmount(percentage, invoiceTax.getTaxAmount(),
                    invoice.getCurrency());
                percepcionAmt = percepcionAmt.add(taxAmount);
              }

            }
          } else {
            throw new OBException(OBMessageUtils.messageBD("OBWPL_DoctypeMissing"));
          }
        }

        OBDal.getInstance().flush();
      }

      return percepcionAmt;

    } catch (Exception ex) {
      System.out.print(getStackTrace(ex));
    } finally {
      OBContext.restorePreviousMode();
    }
    return new BigDecimal(0);

  }

  /*
   * public static BigDecimal getTotalAmountWithoutPerception(Invoice inv) { BigDecimal amount =
   * inv.getGrandTotalAmount();
   * 
   * List<InvoiceTax> invTaxes = inv.getInvoiceTaxList();
   * 
   * for (int i = 0; i < invTaxes.size(); i++) { InvoiceTax invTax = invTaxes.get(i);
   * 
   * if (invTax.getTax().getScoSpecialtax().equals("SCOPCREDITPERCEPTION") ||
   * invTax.getTax().getScoSpecialtax().equals("SCOPINMEDIATEPERCEPTION") ||
   * invTax.getTax().getScoSpecialtax().equals("SCOSCREDITPERCEPTION") ||
   * invTax.getTax().getScoSpecialtax().equals("SCOSINMEDIATEPERCEPTION")) amount =
   * amount.subtract(invTax.getTaxAmount()); }
   * 
   * return amount; }
   */
  public static String getStackTrace(final Throwable throwable) {
    final StringWriter sw = new StringWriter();
    final PrintWriter pw = new PrintWriter(sw, true);
    throwable.printStackTrace(pw);
    return sw.getBuffer().toString();
  }

  public static StructPdt getPDT621Percepciones(ConnectionProvider provider, Date dttFrom,
      Date dttTo, Organization org) throws Exception {

    StringBuffer sb = new StringBuffer();
    StructPdt sunatPdt = new StructPdt();
    sunatPdt.numEntries = 0;

    OBCriteria<SCOPercepPurch> invFilter = OBDal.getInstance().createCriteria(SCOPercepPurch.class);
    invFilter.add(Restrictions.between(SCOPercepPurch.PROPERTY_GENERATEDDATE, dttFrom, dttTo));
    invFilter.add(Restrictions.sqlRestriction("AD_ISORGINCLUDED( this_.ad_org_id, " + "'"
        + org.getId() + "','" + org.getClient().getId() + "') > -1"));

    List<SCOPercepPurch> purches = invFilter.list();

    for (int i = 0; i < purches.size(); i++) {

      SCOPercepPurch purch = purches.get(i);

      if (!purch.getDocumentStatus().equals("CO") && !purch.getDocumentStatus().equals("CL"))
        continue;

      List<SCOPerceppurchDetail> details = purch.getSCOPerceppurchDetailList();

      StringTokenizer st = new StringTokenizer(purch.getPerceptionnumber(), "-");

      String serieComprobante = st.nextToken().trim();
      // if (serieComprobante.length() < 1 || serieComprobante.length() > 4)

      String nroComprobante = null;

      try {
        nroComprobante = st.nextToken().trim();
      } catch (Exception ex) {
      }
      // if (nroComprobante.length() < 1 || nroComprobante.length() > 8)

      SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
      String fechaEmision = DATE_FORMAT.format(purch.getGeneratedDate());

      for (int j = 0; j < details.size(); j++) {

        SCOPerceppurchDetail detail = details.get(j);

        String ruc = detail.getBusinessPartner().getTaxID();
        /*
         * if (ruc.length() > 11) throw new Exception();
         */
        ruc = LPad(ruc, 11, '0');

        String montoPercepcion = detail.getConvertedAmount().toString();// AcctServer.getConvertedAmt(detail.getAmount().toString(),
                                                                        // purch.getCurrency().getId(),
                                                                        // Utility.stringBaseCurrencyId(provider,
                                                                        // purch.getClient().getId()),
                                                                        // OBDateUtils.formatDate(purch.getGeneratedDate()),
                                                                        // "",
                                                                        // purch.getClient().getId(),
                                                                        // purch.getOrganization().getId(),
                                                                        // provider);

        String montoPercepcion2f = String.format("%.2f",
            Double.valueOf(purch.getPercepcionamt().doubleValue()));

        String comprobante = "99"; // otros

        String doctype = detail.getDoctyperef().getScoSpecialdoctype(); // aca debe ligarse a los
                                                                        // ids
                                                                        // en la tabla de llenado de
        if (doctype != null) {
          if (doctype.equals("SCOAPINVOICE") || doctype.equals("SCOAPSIMPLEPROVISIONINVOICE"))
            comprobante = detail.getInvoiceref().getScoPodoctypeComboitem().getCode(); // factura
          if (doctype.equals("SCOAPCREDITMEMO"))
            comprobante = "07"; // credito
        }
        if (comprobante.equals("50") || comprobante.equals("52"))
          comprobante = "55";

        String serieInvoice = "";
        String nroInvoice = "";

        // SI ES DUA EL TRATO ES DIFERENTE
        if (detail.getInvoiceref().getScoDua() != null
            && (comprobante.equals("55") || comprobante.equals("50") || comprobante.equals("52"))) {

          String physical = detail.getInvoiceref().getScoDua().getSCODuanumber();
          String nroDua = physical;
          String serieDua = "";
          String anioDua = "";
          if (physical.contains("-")) {
            st = new StringTokenizer(physical, "-");
            serieDua = st.nextToken();
            nroDua = st.nextToken();

            try {
              anioDua = nroDua;
              nroDua = st.nextToken();
              nroDua = st.nextToken();// el
              // 3er
              // digito
            } catch (Exception ex) {
            }

          }

          serieInvoice = serieDua;
          nroInvoice = nroDua;

        } else {
          st = new StringTokenizer(detail.getInvoiceref().getScrPhysicalDocumentno(), "-");
          serieInvoice = st.nextToken().trim();

          try {
            nroInvoice = st.nextToken().trim();
            if (comprobante.equals("55")) {
              nroInvoice = st.nextToken().trim();
              nroInvoice = st.nextToken().trim();// el 3er digito
            }

          } catch (Exception ex) {
          }
        }

        // if (nroInvoice.length() < 1 || nroInvoice.length() > 8)

        String fechaComprobante = DATE_FORMAT.format(detail.getInvoiceref().getAccountingDate());

        Invoice inv = detail.getInvoiceref();
        String montoTotal = AcctServer.getConvertedAmt(inv.getScoGrandtotalNoperc().toString(),
            inv.getCurrency().getId(),
            Utility.stringBaseCurrencyId(provider, inv.getClient().getId()),
            OBDateUtils.formatDate(inv.getAccountingDate()), "", inv.getClient().getId(),
            inv.getOrganization().getId(), provider);

        BigDecimal totalAmount = new BigDecimal(montoTotal);

        String montoTotal2f = String.format("%.2f", totalAmount.doubleValue());

        sb.append(ruc + "|" + serieComprobante + "|" + nroComprobante + "|" + fechaEmision + "|"
            + montoPercepcion2f + "|" + comprobante + "|" + serieInvoice + "|" + nroInvoice + "|"
            + fechaComprobante + "|" + montoTotal2f + "|\r\n");

        double taxamount = new BigDecimal(montoPercepcion).doubleValue();
        if (taxamount > 0)
          sunatPdt.posAmount = sunatPdt.posAmount + taxamount;
        else
          sunatPdt.negAmount = sunatPdt.negAmount - taxamount;
        sunatPdt.numEntries++;
      }
    }

    sunatPdt.data = sb.toString();

    return sunatPdt;
  }

  public static StructPdt getPDT621PercepcionesSC(ConnectionProvider provider, Date dttFrom,
      Date dttTo, Organization org) throws Exception {

    StringBuffer sb = new StringBuffer();
    StructPdt sunatPdt = new StructPdt();
    sunatPdt.numEntries = 0;

    OBCriteria<InvoiceTax> invFilter = OBDal.getInstance().createCriteria(InvoiceTax.class);
    invFilter.createAlias(InvoiceTax.PROPERTY_INVOICE, "inv");
    invFilter.add(Restrictions.between("inv." + Invoice.PROPERTY_INVOICEDATE, dttFrom, dttTo));
    invFilter.createAlias(InvoiceTax.PROPERTY_TAX, "tx");
    invFilter
        .add(Restrictions.eq("tx." + TaxRate.PROPERTY_SCOSPECIALTAX, "SCOPINMEDIATEPERCEPTION"));
    invFilter.add(Restrictions.sqlRestriction("AD_ISORGINCLUDED( this_.ad_org_id, " + "'"
        + org.getId() + "','" + org.getClient().getId() + "') > -1"));

    List<InvoiceTax> invoiceTax = invFilter.list();

    for (int i = 0; i < invoiceTax.size(); i++) {

      InvoiceTax taxPerc = invoiceTax.get(i);
      Invoice invPerc = invoiceTax.get(i).getInvoice();

      if (!invPerc.getDocumentStatus().equals("CO"))
        continue;

      String ruc = invPerc.getBusinessPartner().getTaxID();
      if (ruc == null) {
        ruc = "";
      }
      /*
       * if (ruc.length() > 11) throw new Exception();
       */
      ruc = LPad(ruc, 11, '0');

      SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
      String fechaEmision = DATE_FORMAT.format(invPerc.getScoNewdateinvoiced());

      String montoPercepcion = AcctServer.getConvertedAmt(taxPerc.getTaxAmount().toString(),
          invPerc.getCurrency().getId(),
          Utility.stringBaseCurrencyId(provider, invPerc.getClient().getId()),
          OBDateUtils.formatDate(invPerc.getScoNewdateinvoiced()), "", invPerc.getClient().getId(),
          invPerc.getOrganization().getId(), provider);

      String comprobante = "99"; // otros

      String doctype = invPerc.getDocumentType().getScoSpecialdoctype(); // aca debe ligarse a los

      // combos
      if (doctype != null) {
        if (doctype.equals("SCOAPINVOICE") || doctype.equals("SCOAPSIMPLEPROVISIONINVOICE")
            || doctype.equals("SCOAPBOEINVOICE") || doctype.equals("SCOAPLOANINVOICE"))
          comprobante = invPerc.getScoPodoctypeComboitem().getCode(); // factura
        if (doctype.equals("SCOAPCREDITMEMO"))
          comprobante = "07"; // credito
      }

      if (comprobante.equals("50"))
        comprobante = "55";

      StringTokenizer st = new StringTokenizer(invPerc.getScrPhysicalDocumentno(), "-");

      String serieInvoice = st.nextToken().trim();
      // if (serieInvoice.length() < 1 || serieInvoice.length() > 4)

      String nroInvoice = "";

      try {
        nroInvoice = st.nextToken().trim();
      } catch (Exception ex) {
      }

      // if (nroInvoice.length() < 1 || nroInvoice.length() > 8)

      String montoTotal = AcctServer.getConvertedAmt(invPerc.getScoGrandtotalNoperc().toString(),
          invPerc.getCurrency().getId(),
          Utility.stringBaseCurrencyId(provider, invPerc.getClient().getId()),
          OBDateUtils.formatDate(invPerc.getScoNewdateinvoiced()), "", invPerc.getClient().getId(),
          invPerc.getOrganization().getId(), provider);

      BigDecimal totalAmount = new BigDecimal(montoTotal);

      String montoTotal2f = String.format("%.2f", totalAmount.doubleValue());

      sb.append(ruc + "|" + comprobante + "|" + serieInvoice + "|" + nroInvoice + "|" + fechaEmision
          + "|" + montoTotal2f + "|\r\n");

      double taxamount = new BigDecimal(montoPercepcion).doubleValue();
      if (taxamount > 0)
        sunatPdt.posAmount = sunatPdt.posAmount + taxamount;
      else
        sunatPdt.negAmount = sunatPdt.negAmount - taxamount;
      sunatPdt.numEntries++;

    }

    sunatPdt.data = sb.toString();

    return sunatPdt;
  }

  public static SCOPaymentHistory findHistory(List<SCOPaymentHistory> lsHistory, String invoiceId) {
    for (int i = 0; i < lsHistory.size(); i++) {
      if (lsHistory.get(i).getInvoice().getId().equals(invoiceId))
        return lsHistory.get(i);
    }
    return null;
  }

  public static StructPdt getPDT697Percepciones(ConnectionProvider provider, Date dttFrom,
      Date dttTo, Organization org) throws Exception {

    StringBuffer sb = new StringBuffer();
    StructPdt sunatPdt = new StructPdt();
    sunatPdt.numEntries = 0;

    OBCriteria<SCOPercepSales> invFilter = OBDal.getInstance().createCriteria(SCOPercepSales.class);
    invFilter.add(Restrictions.between(SCOPercepSales.PROPERTY_GENERATEDDATE, dttFrom, dttTo));
    invFilter.add(Restrictions.sqlRestriction("AD_ISORGINCLUDED( this_.ad_org_id, " + "'"
        + org.getId() + "','" + org.getClient().getId() + "') > -1"));

    List<SCOPercepSales> taxes = invFilter.list();

    for (int i = 0; i < taxes.size(); i++) {

      SCOPercepSales taxSunat = taxes.get(i);

      if (!taxSunat.getDocumentStatus().equals("CO") && !taxSunat.getDocumentStatus().equals("CL"))
        continue;

      List<SCOPercepsalesDetail> details = taxSunat.getSCOPercepsalesDetailList();

      StringTokenizer st = new StringTokenizer(taxSunat.getPerceptionNumber(), "-");

      String serieComprobante = st.nextToken().trim();
      // if (serieComprobante.length() < 1 || serieComprobante.length() > 4)

      String nroComprobante = "";

      try {
        nroComprobante = st.nextToken().trim();
      } catch (Exception ex) {
      }
      // if (nroComprobante.length() < 1 || nroComprobante.length() > 8)

      nroComprobante = LPad(nroComprobante, 8, '0');

      for (int j = 0; j < details.size(); j++) {

        SCOPercepsalesDetail detail = details.get(j);

        String ruc = detail.getBusinessPartner().getTaxID();
        if (ruc == null) {
          ruc = "";
        }
        /*
         * if (ruc.length() > 11) throw new Exception();
         */
        ruc = LPad(ruc, 11, '0');

        String apaterno = "";
        String amaterno = "";
        String nombres = "";
        String proveedor = "";

        if (!detail.getBusinessPartner().isScoIscompany()) { // persona natural
          apaterno = detail.getBusinessPartner().getScoLastname();
          amaterno = detail.getBusinessPartner().getScoLastname2();

          nombres = detail.getBusinessPartner().getScoFirstname();
        } else {
          proveedor = detail.getBusinessPartner().getName();
        }

        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
        String fechaEmision = DATE_FORMAT.format(taxSunat.getGeneratedDate());

        String doctype = detail.getDoctyperef().getScoSpecialdoctype(); // aca debe ligarse a los
        Invoice invoice = detail.getInvoiceref();

        Invoice invOld = invoice;
        while (invoice.getDocumentType().getScoSpecialdoctype().equals("SCOARBOEINVOICE")) {
          invOld = invoice;
          invoice = invoice.getScoBoeTo().getBillOfExchange().getSCOBoeFromList().get(0)
              .getInvoiceref();
        }

        List<Invoice> lsInvoices = new ArrayList<Invoice>();
        if (invOld.getDocumentType().getScoSpecialdoctype().equals("SCOARBOEINVOICE")) {
          List<SCOBoeFrom> lsBoe = invOld.getScoBoeTo().getBillOfExchange().getSCOBoeFromList();
          for (int k = 0; k < lsBoe.size(); k++) {
            Invoice invAux = lsBoe.get(k).getInvoiceref();
            lsInvoices.add(invAux);
          }
        } else {
          lsInvoices.add(invOld);
        }

        List<SCOPaymentHistory> lsHistory = detail.getPaymentDetails().getSCOPaymentHistoryList();
        for (int k = 0; k < lsInvoices.size(); k++) {

          invoice = lsInvoices.get(k);
          doctype = invoice.getDocumentType().getScoSpecialdoctype();
          SCOPaymentHistory history = findHistory(lsHistory, invoice.getId());

          String montoPercepcion = AcctServer.getConvertedAmt(history.getPerceptionamt().toString(),
              invoice.getCurrency().getId(),
              Utility.stringBaseCurrencyId(provider, taxSunat.getClient().getId()),
              OBDateUtils.formatDate(taxSunat.getGeneratedDate()), "", taxSunat.getClient().getId(),
              taxSunat.getOrganization().getId(), provider);
          String montoPercepcion2f = String.format("%.2f", Double.valueOf(montoPercepcion));
          String comprobante = "99"; // otros

          // combos
          if (doctype != null) {
            if (doctype.equals("SCOARINVOICE"))
              comprobante = "01"; // factura
            if (doctype.equals("SCOARCREDITMEMO") || doctype.equals("SCOARINVOICERETURNMAT"))
              comprobante = "07"; // credito
            if (doctype.equals("SCOARDEBITMEMO"))
              comprobante = "08"; // debito
            if (doctype.equals("SCOARTICKET"))
              comprobante = "03"; // boleta
          }

          st = new StringTokenizer(invoice.getScrPhysicalDocumentno(), "-");

          String serieInvoice = st.nextToken().trim();
          // if (serieComprobante.length() < 1 || serieComprobante.length() > 4)
          // throw new Exception();

          String nroInvoice = "";

          try {
            nroInvoice = st.nextToken().trim();
          } catch (Exception ex) {
          }
          // if (nroComprobante.length() < 1 || nroComprobante.length() > 8)
          // throw new Exception();
          String fechaComprobante = DATE_FORMAT.format(invoice.getAccountingDate());

          Invoice inv = invoice;
          String montoTotal = AcctServer.getConvertedAmt(history.getAmount().toString(),
              inv.getCurrency().getId(),
              Utility.stringBaseCurrencyId(provider, inv.getClient().getId()),
              OBDateUtils.formatDate(inv.getAccountingDate()), "", inv.getClient().getId(),
              inv.getOrganization().getId(), provider, false);

          BigDecimal totalAmount = new BigDecimal(montoTotal);

          String montoTotal2f = String.format("%.2f", totalAmount.doubleValue());

          String percepClient = "0";
          if (detail.getBusinessPartner().isScoPercepcionagent())
            percepClient = "1";

          String tipodoc = detail.getBusinessPartner().getScrComboItem().getCode();

          if (Double.valueOf(montoPercepcion) != 0.00) {
            sb.append(tipodoc + "|" + ruc + "|" + proveedor + "|" + apaterno + "|" + amaterno + "|"
                + nombres + "|" + serieComprobante + "|" + nroComprobante + "|" + fechaEmision
                + "|1|0|" + percepClient + "|" + montoPercepcion2f + "|" + comprobante + "|"
                + serieInvoice + "|" + nroInvoice + "|" + fechaComprobante + "|" + montoTotal2f
                + "|\r\n");

            double taxamount = new BigDecimal(montoPercepcion).doubleValue();
            if (taxamount > 0)
              sunatPdt.posAmount = sunatPdt.posAmount + taxamount;
            else
              sunatPdt.negAmount = sunatPdt.negAmount - taxamount;
            sunatPdt.numEntries++;
          }
        }
      }
    }

    sunatPdt.data = sb.toString();

    return sunatPdt;
  }

  public static StructPdt getPDT697PercepcionesSC(ConnectionProvider provider, Date dttFrom,
      Date dttTo, Organization org) throws Exception {

    StringBuffer sb = new StringBuffer();
    StructPdt sunatPdt = new StructPdt();
    sunatPdt.numEntries = 0;

    OBCriteria<InvoiceTax> invFilter = OBDal.getInstance().createCriteria(InvoiceTax.class);
    invFilter.createAlias(InvoiceTax.PROPERTY_INVOICE, "inv");
    invFilter.add(Restrictions.between("inv." + Invoice.PROPERTY_INVOICEDATE, dttFrom, dttTo));
    invFilter.createAlias(InvoiceTax.PROPERTY_TAX, "tx");
    invFilter
        .add(Restrictions.eq("tx." + TaxRate.PROPERTY_SCOSPECIALTAX, "SCOSINMEDIATEPERCEPTION"));
    invFilter.add(Restrictions.sqlRestriction("AD_ISORGINCLUDED( this_.ad_org_id, " + "'"
        + org.getId() + "','" + org.getClient().getId() + "') > -1"));

    List<InvoiceTax> invoiceTax = invFilter.list();

    for (int i = 0; i < invoiceTax.size(); i++) {

      InvoiceTax taxPerc = invoiceTax.get(i);
      Invoice invPerc = invoiceTax.get(i).getInvoice();

      if (!invPerc.getDocumentStatus().equals("CO"))
        continue;

      String ruc = invPerc.getBusinessPartner().getTaxID();
      if (ruc == null) {
        ruc = "";
      }
      /*
       * if (ruc.length() > 11) throw new Exception();
       */
      ruc = LPad(ruc, 11, '0');

      SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
      String fechaEmision = DATE_FORMAT.format(invPerc.getAccountingDate());

      String montoPercepcion = AcctServer.getConvertedAmt(taxPerc.getTaxAmount().toString(),
          invPerc.getCurrency().getId(),
          Utility.stringBaseCurrencyId(provider, invPerc.getClient().getId()),
          OBDateUtils.formatDate(invPerc.getAccountingDate()), "", invPerc.getClient().getId(),
          invPerc.getOrganization().getId(), provider);

      String comprobante = "99"; // otros
      String doctype = invPerc.getDocumentType().getScoSpecialdoctype(); // aca debe ligarse a los

      // combos
      if (doctype != null) {
        if (doctype.equals("SCOARINVOICE"))
          comprobante = "01"; // factura
        if (doctype.equals("SCOARCREDITMEMO") || doctype.equals("SCOARINVOICERETURNMAT"))
          comprobante = "07"; // credito
        if (doctype.equals("SCOARDEBITMEMO"))
          comprobante = "08"; // debito
        if (doctype.equals("SCOARTICKET"))
          comprobante = "03"; // boleta
        if (doctype.equals("SCOARBOEINVOICE")) {
          // imposible, letra no es al contado
        }
      }

      StringTokenizer st = new StringTokenizer(invPerc.getScrPhysicalDocumentno(), "-");

      String serieInvoice = st.nextToken().trim();
      /*
       * if (serieInvoice.length() < 1 || serieInvoice.length() > 4) throw new Exception();
       */

      String nroInvoice = "";

      try {
        nroInvoice = st.nextToken().trim();
      } catch (Exception ex) {
      }
      /*
       * if (nroInvoice.length() < 1 || nroInvoice.length() > 8) throw new Exception();
       */

      String montoTotal = AcctServer.getConvertedAmt(invPerc.getScoGrandtotalNoperc().toString(),
          invPerc.getCurrency().getId(),
          Utility.stringBaseCurrencyId(provider, invPerc.getClient().getId()),
          OBDateUtils.formatDate(invPerc.getAccountingDate()), "", invPerc.getClient().getId(),
          invPerc.getOrganization().getId(), provider, false);

      BigDecimal totalAmount = new BigDecimal(montoTotal);

      String montoTotal2f = String.format("%.2f", totalAmount.doubleValue());

      sb.append(ruc + "|" + comprobante + "|" + serieInvoice + "|" + nroInvoice + "|" + fechaEmision
          + "|" + montoTotal2f + "|\r\n");

      double taxamount = new BigDecimal(montoPercepcion).doubleValue();
      if (taxamount > 0)
        sunatPdt.posAmount = sunatPdt.posAmount + taxamount;
      else
        sunatPdt.negAmount = sunatPdt.negAmount - taxamount;
      sunatPdt.numEntries++;

    }

    sunatPdt.data = sb.toString();

    return sunatPdt;
  }

  public static StructPdt getPDT621Retenciones(ConnectionProvider provider, Date dttFrom,
      Date dttTo, Organization org) throws Exception {

    StringBuffer sb = new StringBuffer();
    StructPdt sunatPdt = new StructPdt();
    sunatPdt.numEntries = 0;

    OBCriteria<SCOSwithholdingReceipt> invFilter = OBDal.getInstance()
        .createCriteria(SCOSwithholdingReceipt.class);
    invFilter
        .add(Restrictions.between(SCOSwithholdingReceipt.PROPERTY_ACCOUNTINGDATE, dttFrom, dttTo));
    invFilter.add(Restrictions.sqlRestriction("AD_ISORGINCLUDED( this_.ad_org_id, " + "'"
        + org.getId() + "','" + org.getClient().getId() + "') > -1"));

    List<SCOSwithholdingReceipt> purches = invFilter.list();

    for (int i = 0; i < purches.size(); i++) {

      SCOSwithholdingReceipt purch = purches.get(i);

      if (!purch.getDocumentStatus().equals("CO") && !purch.getDocumentStatus().equals("CL"))
        continue;

      List<SCOSwithhoRecLine> details = purch.getSCOSwithhoRecLineList();

      StringTokenizer st = new StringTokenizer(purch.getWithholdingNumber(), "-");

      String serieComprobante = st.nextToken().trim();
      /*
       * if (serieComprobante.length() < 1 || serieComprobante.length() > 4) throw new Exception();
       */

      String nroComprobante = "";

      try {
        nroComprobante = st.nextToken().trim();
      } catch (Exception ex) {
      }
      /*
       * if (nroComprobante.length() < 1 || nroComprobante.length() > 8) throw new Exception();
       */

      nroComprobante = LPad(nroComprobante, 8, '0');

      if (nroComprobante.length() > 8)
        nroComprobante = nroComprobante.substring(nroComprobante.length() - 8);

      SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
      String fechaEmision = DATE_FORMAT.format(purch.getGeneratedDate());

      BigDecimal totalPhysical = purch.getTotalamountPhysicaldoc();

      for (int j = 0; j < details.size(); j++) {

        SCOSwithhoRecLine detail = details.get(j);

        String ruc = detail.getBusinessPartner().getTaxID();
        /*
         * if (ruc.length() > 11) throw new Exception();
         */
        ruc = LPad(ruc, 11, '0');

        String montoRetencion = detail.getConvertedAmount().toString();// AcctServer.getConvertedAmt(detail.getAmount().toString(),
                                                                       // purch.getCurrency().getId(),
                                                                       // Utility.stringBaseCurrencyId(provider,
                                                                       // purch.getClient().getId()),
                                                                       // OBDateUtils.formatDate(purch.getGeneratedDate()),
                                                                       // "",
                                                                       // purch.getClient().getId(),
                                                                       // purch.getOrganization().getId(),
                                                                       // provider);

        String montoRetencion2f = String.format("%.2f",
            Double.valueOf(totalPhysical.doubleValue()));

        String comprobante = "99"; // otros

        String doctype = detail.getDoctyperef().getScoSpecialdoctype(); // aca debe ligarse a los
                                                                        // ids

        Invoice invoice = detail.getInvoiceref();
        if (doctype != null) {
          if (doctype.equals("SCOARINVOICE"))
            comprobante = "01"; // factura
          if (doctype.equals("SCOARCREDITMEMO") || doctype.equals("SCOARINVOICERETURNMAT"))
            comprobante = "07"; // credito
          if (doctype.equals("SCOARDEBITMEMO"))
            comprobante = "08"; // debito
          if (doctype.equals("SCOARTICKET"))
            comprobante = "03"; // boleta
          if (doctype.equals("SCOARBOEINVOICE")) {
            comprobante = "01";
            Invoice invBOE = detail.getInvoiceref().getScoBoeTo().getBillOfExchange()
                .getSCOBoeFromList().get(0).getInvoiceref();
            while (invBOE.getDocumentType().equals("SCOARBOEINVOICE")) {
              invBOE = invBOE.getScoBoeTo().getBillOfExchange().getSCOBoeFromList().get(0)
                  .getInvoiceref();
            }
            invoice = invBOE;
          }
        }

        st = new StringTokenizer(invoice.getScrPhysicalDocumentno(), "-");

        String serieInvoice = st.nextToken().trim();
        /*
         * if (serieInvoice.length() < 1 || serieInvoice.length() > 4) throw new Exception();
         */

        String nroInvoice = "";

        try {
          nroInvoice = st.nextToken().trim();
        } catch (Exception ex) {
        }
        /*
         * if (nroInvoice.length() < 1 || nroInvoice.length() > 8) throw new Exception();
         */

        String fechaComprobante = DATE_FORMAT.format(invoice.getAccountingDate());

        Invoice inv = invoice;
        String montoTotal = AcctServer.getConvertedAmt(inv.getScoGrandtotalNoperc().toString(),
            inv.getCurrency().getId(),
            Utility.stringBaseCurrencyId(provider, inv.getClient().getId()),
            OBDateUtils.formatDate(inv.getAccountingDate()), "", inv.getClient().getId(),
            inv.getOrganization().getId(), provider, false);

        BigDecimal totalAmount = new BigDecimal(montoTotal);

        String montoTotal2f = String.format("%.2f", totalAmount.doubleValue());

        sb.append(ruc + "|" + serieComprobante + "|" + nroComprobante + "|" + fechaEmision + "|"
            + montoRetencion2f + "|" + comprobante + "|" + serieInvoice + "|" + nroInvoice + "|"
            + fechaComprobante + "|" + montoTotal2f + "|\r\n");

        double taxamount = new BigDecimal(montoRetencion).doubleValue();
        if (taxamount > 0)
          sunatPdt.posAmount = sunatPdt.posAmount + taxamount;
        else
          sunatPdt.negAmount = sunatPdt.negAmount - taxamount;
        sunatPdt.numEntries++;
      }
    }

    sunatPdt.data = sb.toString();

    return sunatPdt;
  }

  public static StructPdt getPDT626Retenciones(ConnectionProvider provider, Date dttFrom,
      Date dttTo, Organization org) throws Exception {

    StringBuffer sb = new StringBuffer();
    StructPdt sunatPdt = new StructPdt();
    sunatPdt.numEntries = 0;

    OBCriteria<SCOPwithholdingReceipt> invFilter = OBDal.getInstance()
        .createCriteria(SCOPwithholdingReceipt.class);
    invFilter
        .add(Restrictions.between(SCOPwithholdingReceipt.PROPERTY_ACCOUNTINGDATE, dttFrom, dttTo));
    invFilter.add(Restrictions.sqlRestriction("AD_ISORGINCLUDED( this_.ad_org_id, " + "'"
        + org.getId() + "','" + org.getClient().getId() + "') > -1"));

    List<SCOPwithholdingReceipt> taxes = invFilter.list();

    for (int i = 0; i < taxes.size(); i++) {

      SCOPwithholdingReceipt taxSunat = taxes.get(i);

      if (!taxSunat.getDocumentStatus().equals("CO") && !taxSunat.getDocumentStatus().equals("CL"))
        continue;

      List<SCOPwithhoRecLine> details = taxSunat.getSCOPwithhoRecLineList();

      StringTokenizer st = new StringTokenizer(taxSunat.getWithholdingnumber(), "-");

      String serieComprobante = st.nextToken().trim();
      /*
       * if (serieComprobante.length() < 1 || serieComprobante.length() > 4) throw new Exception();
       */

      String nroComprobante = "";

      try {
        nroComprobante = st.nextToken().trim();
      } catch (Exception ex) {
      }
      /*
       * if (nroComprobante.length() < 1 || nroComprobante.length() > 8) throw new Exception();
       */

      nroComprobante = LPad(nroComprobante, 8, '0');

      for (int j = 0; j < details.size(); j++) {

        SCOPwithhoRecLine detail = details.get(j);

        String ruc = detail.getBusinessPartner().getTaxID();
        if (ruc == null) {
          ruc = "";
        }
        /*
         * if (ruc.length() > 11) throw new Exception();
         */
        ruc = LPad(ruc, 11, '0');

        String apaterno = "";
        String amaterno = "";
        String nombres = "";
        String proveedor = "";

        if (!detail.getBusinessPartner().isScoIscompany()) { // persona natural
          apaterno = detail.getBusinessPartner().getScoLastname();
          amaterno = detail.getBusinessPartner().getScoLastname2();

          nombres = detail.getBusinessPartner().getScoFirstname();
        } else {
          proveedor = detail.getBusinessPartner().getName();
        }

        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
        String fechaEmision = DATE_FORMAT.format(taxSunat.getGeneratedDate());

        // String montoRetenciones = detail.getConvertedAmount().toString(); //
        // AcctServer.getConvertedAmt(detail.getAmount().toString(),
        // taxSunat.getCurrency().getId(),
        // Utility.stringBaseCurrencyId(provider,
        // taxSunat.getClient().getId()),
        // OBDateUtils.formatDate(taxSunat.getGeneratedDate()),
        // "",
        // taxSunat.getClient().getId(),
        // taxSunat.getOrganization().getId(),
        // provider);

        // String montoRetenciones2f = String.format("%.2f", Double.valueOf(montoRetenciones));

        List<SCOPwithhoRecLine> lsRetLines = taxSunat.getSCOPwithhoRecLineList();
        BigDecimal montoTotalPagado = new BigDecimal(0.00);
        for (int u = 0; u < lsRetLines.size(); u++) {

          String montoRetencionesLine = AcctServer.getConvertedAmt(
              lsRetLines.get(u).getPaymentamount().toString(),
              lsRetLines.get(u).getInvoiceref().getCurrency().getId(),
              Utility.stringBaseCurrencyId(provider, taxSunat.getClient().getId()),
              OBDateUtils.formatDate(taxSunat.getGeneratedDate()), "", taxSunat.getClient().getId(),
              taxSunat.getOrganization().getId(), provider);

          montoTotalPagado = montoTotalPagado.add(new BigDecimal(montoRetencionesLine));
        }

        String montoRetenciones2f = String.format("%.2f", Double.valueOf(montoTotalPagado
            .add(taxSunat.getTotalAmount()).setScale(2, RoundingMode.HALF_UP).toString()));

        // String montoRetenciones2f = String.format("%.2f",
        // Double.valueOf(taxSunat.getTotalAmount().doubleValue()));

        String comprobante = "99"; // otros

        String doctype = detail.getDoctyperef().getScoSpecialdoctype(); // aca debe ligarse a los
                                                                        // ids

        Invoice invoice = detail.getInvoiceref();

        // combos
        if (doctype != null) {
          if (doctype.equals("SCOAPINVOICE") || doctype.equals("SCOAPSIMPLEPROVISIONINVOICE"))
            comprobante = detail.getInvoiceref().getScoPodoctypeComboitem().getCode(); // factura
          if (doctype.equals("SCOAPCREDITMEMO"))
            comprobante = "07"; // credito
        }

        st = new StringTokenizer(invoice.getScrPhysicalDocumentno(), "-");

        String serieInvoice = st.nextToken().trim();
        /*
         * if (serieComprobante.length() < 1 || serieComprobante.length() > 4) throw new
         * Exception();
         */

        String nroInvoice = "";

        try {
          nroInvoice = st.nextToken().trim();
        } catch (Exception ex) {
        }
        /*
         * if (nroComprobante.length() < 1 || nroComprobante.length() > 8) throw new Exception();
         */
        String fechaComprobante = DATE_FORMAT.format(invoice.getScoNewdateinvoiced());

        Invoice inv = invoice;
        String montoTotal = AcctServer.getConvertedAmt(inv.getScoGrandtotalNoperc().toString(),
            inv.getCurrency().getId(),
            Utility.stringBaseCurrencyId(provider, inv.getClient().getId()),
            OBDateUtils.formatDate(inv.getScoNewdateinvoiced()), "", inv.getClient().getId(),
            inv.getOrganization().getId(), provider);

        BigDecimal totalAmount = new BigDecimal(montoTotal);

        String montoTotal2f = String.format("%.2f", totalAmount.doubleValue());

        sb.append(ruc + "|" + proveedor + "|" + apaterno + "|" + amaterno + "|" + nombres + "|"
            + serieComprobante + "|" + nroComprobante + "|" + fechaEmision + "|"
            + montoRetenciones2f + "|" + comprobante + "|" + serieInvoice + "|" + nroInvoice + "|"
            + fechaComprobante + "|" + montoTotal2f + "|\r\n");

        double taxamount = taxSunat.getTotalAmount().doubleValue();
        if (taxamount > 0)
          sunatPdt.posAmount = sunatPdt.posAmount + taxamount;
        else
          sunatPdt.negAmount = sunatPdt.negAmount - taxamount;
        sunatPdt.numEntries++;

      }
    }

    sunatPdt.data = sb.toString();

    return sunatPdt;
  }

  public static StructPdt getPDT3550DetalleDeVentas(ConnectionProvider provider, Date dttFrom,
      Date dttTo, Organization org) throws Exception {

    StringBuffer sb = new StringBuffer();
    StructPdt sunatPdt = new StructPdt();
    sunatPdt.numEntries = 0;

    OBCriteria<Invoice> invFilter = OBDal.getInstance().createCriteria(Invoice.class);
    invFilter.add(Restrictions.between(Invoice.PROPERTY_ACCOUNTINGDATE, dttFrom, dttTo));
    invFilter.add(Restrictions.eq(Invoice.PROPERTY_SALESTRANSACTION, true));
    invFilter.add(Restrictions.eq(Invoice.PROPERTY_DOCUMENTSTATUS, "CO"));
    invFilter.add(Restrictions.sqlRestriction("AD_ISORGINCLUDED( this_.ad_org_id, " + "'"
        + org.getId() + "','" + org.getClient().getId() + "') > -1"));

    List<Invoice> invoices = invFilter.list();

    for (int i = 0; i < invoices.size(); i++) {

      Invoice invoice = invoices.get(i);
      String doctype = invoice.getTransactionDocument().getScoSpecialdoctype();
      String comprobante = "99";
      if (invoice.getTransactionDocument() != null) {
        if (doctype.equals("SCOARINVOICE"))
          comprobante = "01"; // factura
        if (doctype.equals("SCOARCREDITMEMO") || doctype.equals("SCOARINVOICERETURNMAT"))
          comprobante = "07"; // credito
        if (doctype.equals("SCOARDEBITMEMO"))
          comprobante = "08"; // debito
        if (doctype.equals("SCOARTICKET"))
          comprobante = "03"; // boleta
        if (doctype.equals("SCOARBOEINVOICE")) {
          continue;// not consider BOEs
        }

      }

      SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM");
      StringTokenizer st = new StringTokenizer(invoice.getScrPhysicalDocumentno(), "-");

      String serieInvoice = st.nextToken().trim();

      if (!comprobante.equals("99")) {
        /*
         * if (serieInvoice.length() > 4) throw new Exception();
         */

        serieInvoice = LPad(serieInvoice, 4, '0');
      } else {
        /*
         * if (serieInvoice.length() > 15) throw new Exception();
         */

        serieInvoice = LPad(serieInvoice, 15, '0');
      }

      String nroInvoice = "";

      try {
        nroInvoice = st.nextToken().trim();
      } catch (Exception ex) {
      }

      /*
       * if (nroInvoice.length() < 1 || nroInvoice.length() > 8) throw new Exception();
       */
      String mesComprobante = DATE_FORMAT.format(invoice.getAccountingDate());

      String ventasTotales = AcctServer
          .getConvertedAmt(invoice.getSummedLineAmount().toString(), invoice.getCurrency().getId(),
              Utility.stringBaseCurrencyId(provider, invoice.getClient().getId()),
              OBDateUtils.formatDate(invoice.getAccountingDate()), "", invoice.getClient().getId(),
              invoice.getOrganization().getId(), provider)
          .toString();

      List<InvoiceLine> invoiceLines = invoice.getInvoiceLineList();
      BigDecimal amtLineIGV = new BigDecimal(0);
      for (int ij = 0; ij < invoiceLines.size(); ij++) {
        if (invoiceLines.get(ij).getTax().getScoSpecialtax() != null
            && invoiceLines.get(ij).getTax().getScoSpecialtax().equals("SCOIGV"))
          amtLineIGV = amtLineIGV.add(invoiceLines.get(ij).getLineNetAmount());
      }

      String ventasGravadas = AcctServer
          .getConvertedAmt(amtLineIGV.toString(), invoice.getCurrency().getId(),
              Utility.stringBaseCurrencyId(provider, invoice.getClient().getId()),
              OBDateUtils.formatDate(invoice.getAccountingDate()), "", invoice.getClient().getId(),
              invoice.getOrganization().getId(), provider)
          .toString();

      sb.append(comprobante + "|" + serieInvoice + "|" + mesComprobante + "|" + ventasTotales + "|"
          + ventasGravadas + "|\r\n");

      double totalAmt = invoice.getSummedLineAmount().doubleValue();
      if (totalAmt > 0)
        sunatPdt.posAmount = sunatPdt.posAmount + totalAmt;
      else
        sunatPdt.negAmount = sunatPdt.negAmount - totalAmt;
      sunatPdt.numEntries++;
    }
    sunatPdt.data = sb.toString();
    return sunatPdt;
  }

  public static StructPdt getPDT617_2da(ConnectionProvider provider, Date dttFrom, Date dttTo,
      Organization org) throws Exception {

    String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_JAVA);

    String strDttFrom = sdf.format(dttFrom);
    String strDttTo = sdf.format(dttTo);

    StringBuffer sb = new StringBuffer();
    StructPdt sunatPdt = new StructPdt();
    sunatPdt.numEntries = 0;
    sunatPdt.negAmount = 0;
    sunatPdt.posAmount = 0;
    sunatPdt.data = "";

    SunatUtilData[] glitems_4017501 = SunatUtilData.selectGLItemByAcctValue(provider,
        org.getClient().getId(), org.getId(), "40175%");
    if (glitems_4017501.length == 0)
      return sunatPdt;

    String glitemlist = "";
    int counter = 0;
    String currency = "308";
    for (int i = 0; i < glitems_4017501.length; i++) {

      GLItem item = OBDal.getInstance().get(GLItem.class, glitems_4017501[i].cGlitemId);
      List<GLItemAccounts> accounts = item.getFinancialMgmtGLItemAccountsList();

      for (int j = 0; j < accounts.size(); j++) {
        if (accounts.get(j).getAccountingSchema().getOrganization().getId().equals(org.getId())
            && accounts.get(j).getAccountingSchema().getCurrency().getId().equals(currency)) {
          if (counter != 0)
            glitemlist = glitemlist + ",";
          counter++;
          glitemlist = glitemlist + "'" + accounts.get(j).getGlitemCreditAcct().getAccount().getId()
              + "'";
        }
      }

    }

    SunatUtilData[] invoices = SunatUtilData.selectInternalDocByGlItem(provider,
        org.getClient().getId(), org.getId(), glitemlist, strDttFrom, strDttTo);

    HashMap<String, BigDecimal> hashbyBPartner = new HashMap<String, BigDecimal>();
    for (int i = 0; i < invoices.length; i++) {
      String srenta_neta = AcctServer
          .getConvertedAmt(invoices[i].totalamt, invoices[i].cCurrencyId,
              Utility.stringBaseCurrencyId(provider, invoices[i].adClientId),
              invoices[i].dateinvoiced, "", invoices[i].adClientId, invoices[i].adOrgId, provider)
          .toString();
      BigDecimal renta_neta = new BigDecimal(srenta_neta);

      BigDecimal totalamt = hashbyBPartner.get(invoices[i].cBpartnerId);
      if (totalamt == null) {
        hashbyBPartner.put(invoices[i].cBpartnerId, renta_neta);
      } else {
        hashbyBPartner.put(invoices[i].cBpartnerId, totalamt.add(renta_neta));
      }
    }

    Set<String> cbpartners = hashbyBPartner.keySet();
    String[] cbpartnerIds = new String[cbpartners.size()];
    cbpartners.toArray(cbpartnerIds);

    for (int i = 0; i < cbpartnerIds.length; i++) {
      BusinessPartner bpartner = OBDal.getInstance().get(BusinessPartner.class, cbpartnerIds[i]);
      SCRComboItem tipodoc = bpartner.getScrComboItem();
      String tipodoc_value = tipodoc.getSearchKey();
      String tipo_documento = "8";
      if (tipodoc_value.equals("DNI")) {
        tipo_documento = "1";
      } else if (tipodoc_value.equals("CARNET DE EXTRANJERIA")) {
        tipo_documento = "4";
      } else if (tipodoc_value.equals("REGISTRO UNICO DE CONTRIBUYENTES")) {
        tipo_documento = "6";
      } else if (tipodoc_value.equals("PASAPORTE")) {
        tipo_documento = "7";
      } else {
        tipo_documento = "0"; // throw new Exception();
      }

      String numero_doc = bpartner.getTaxID();
      /*
       * if (numero_doc.length() > 15) { throw new Exception(); }
       */

      String tipo_desc = "1";
      if (bpartner.isScoIscompany()) {
        tipo_desc = "2";
      }

      String app_paterno = "";
      if (!bpartner.isScoIscompany()) {
        app_paterno = bpartner.getScoLastname();
        if (app_paterno == null)
          app_paterno = "";
        if (app_paterno.length() > 20)
          app_paterno = app_paterno.substring(0, 20);
      }

      String app_materno = "";
      if (!bpartner.isScoIscompany()) {
        app_materno = bpartner.getScoLastname2();
        if (app_materno == null)
          app_materno = "";
        if (app_materno.length() > 20)
          app_materno = app_materno.substring(0, 20);
      }

      String nombres = "";
      if (!bpartner.isScoIscompany()) {
        nombres = bpartner.getScoFirstname();
        if (nombres == null)
          nombres = "";
        if (nombres.length() > 20)
          nombres = nombres.substring(0, 20);
      }

      String razon_social = "";
      if (bpartner.isScoIscompany()) {
        razon_social = bpartner.getName();
        if (razon_social.length() > 40)
          razon_social = razon_social.substring(0, 40);
      }

      String base_legal = "1";
      String renta_neta = hashbyBPartner.get(cbpartnerIds[i]).toPlainString();
      sb.append(tipo_documento + "|" + numero_doc + "|" + tipo_desc + "|" + app_paterno + "|"
          + app_materno + "|" + nombres + "|" + razon_social + "|" + base_legal + "|" + renta_neta
          + "|\r\n");
      sunatPdt.numEntries++;
    }

    sunatPdt.data = sb.toString();
    return sunatPdt;
  }

  public static StructPdt getPDT617_NoDomiciliado(ConnectionProvider provider, Date dttFrom,
      Date dttTo, Organization org) throws Exception {

    String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_JAVA);

    String strDttFrom = sdf.format(dttFrom);
    String strDttTo = sdf.format(dttTo);

    StringBuffer sb = new StringBuffer();
    StructPdt sunatPdt = new StructPdt();
    sunatPdt.numEntries = 0;
    sunatPdt.negAmount = 0;
    sunatPdt.posAmount = 0;
    sunatPdt.data = "";

    SunatUtilData[] glitems_4017401 = SunatUtilData.selectGLItemByAcctValue(provider,
        org.getClient().getId(), org.getId(), "40174%");
    if (glitems_4017401.length == 0)
      return sunatPdt;

    String glitemlist = "";
    int counter = 0;
    String currency = "308";
    for (int i = 0; i < glitems_4017401.length; i++) {

      GLItem item = OBDal.getInstance().get(GLItem.class, glitems_4017401[i].cGlitemId);
      List<GLItemAccounts> accounts = item.getFinancialMgmtGLItemAccountsList();

      for (int j = 0; j < accounts.size(); j++) {
        if (accounts.get(j).getAccountingSchema().getOrganization().getId().equals(org.getId())
            && accounts.get(j).getAccountingSchema().getCurrency().getId().equals(currency)) {
          if (counter != 0)
            glitemlist = glitemlist + ",";
          counter++;
          glitemlist = glitemlist + "'" + accounts.get(j).getGlitemCreditAcct().getAccount().getId()
              + "'";
        }
      }

    }

    SunatUtilData[] invoices = SunatUtilData.selectInternalDocByGlItem(provider,
        org.getClient().getId(), org.getId(), glitemlist, strDttFrom, strDttTo);

    HashMap<String, BigDecimal> hashbyBPartner = new HashMap<String, BigDecimal>();
    HashMap<String, BigDecimal> hashbyBPartnerRetencion = new HashMap<String, BigDecimal>();
    for (int i = 0; i < invoices.length; i++) {
      String srenta_neta = AcctServer
          .getConvertedAmt(invoices[i].totalamt, invoices[i].cCurrencyId,
              Utility.stringBaseCurrencyId(provider, invoices[i].adClientId),
              invoices[i].dateinvoiced, "", invoices[i].adClientId, invoices[i].adOrgId, provider)
          .toString();

      BigDecimal renta_neta = new BigDecimal(srenta_neta);

      BigDecimal totalamt = hashbyBPartner.get(invoices[i].cBpartnerId);
      BigDecimal totalRet = hashbyBPartnerRetencion.get(invoices[i].cBpartnerId);
      if (totalamt == null) {
        hashbyBPartner.put(invoices[i].cBpartnerId, renta_neta);
        hashbyBPartnerRetencion.put(invoices[i].cBpartnerId, new BigDecimal(invoices[i].retencion));
      } else {
        hashbyBPartner.put(invoices[i].cBpartnerId, totalamt.add(renta_neta));
        hashbyBPartnerRetencion.put(invoices[i].cBpartnerId,
            totalRet.add(new BigDecimal(invoices[i].retencion)));
      }
    }

    Set<String> cbpartners = hashbyBPartner.keySet();
    String[] cbpartnerIds = new String[cbpartners.size()];
    cbpartners.toArray(cbpartnerIds);

    for (int i = 0; i < cbpartnerIds.length; i++) {
      BusinessPartner bpartner = OBDal.getInstance().get(BusinessPartner.class, cbpartnerIds[i]);
      SCRComboItem tipodoc = bpartner.getScrComboItem();
      String tipodoc_value = tipodoc.getSearchKey();
      String tipo_documento = "8";
      if (tipodoc_value.equals("DNI")) {
        tipo_documento = "1";
      } else if (tipodoc_value.equals("CARNET DE EXTRANJERIA")) {
        tipo_documento = "4";
      } else if (tipodoc_value.equals("REGISTRO UNICO DE CONTRIBUYENTES")) {
        tipo_documento = "6";
      } else if (tipodoc_value.equals("PASAPORTE")) {
        tipo_documento = "7";
      } else {
        tipo_documento = "0"; // throw new Exception();
      }

      String numero_doc = bpartner.getTaxID();
      String tipoPersona = bpartner.isScoIscompany() == true ? "2" : "1";
      String tipoDocNoDomiciliado = "01";
      String tipo_desc = "1";
      if (bpartner.isScoIscompany()) {
        tipo_desc = "2";
        tipoDocNoDomiciliado = "02";
      }

      String app_paterno = "";
      if (!bpartner.isScoIscompany()) {
        app_paterno = bpartner.getScoLastname();
        if (app_paterno == null)
          app_paterno = "";
        if (app_paterno.length() > 20)
          app_paterno = app_paterno.substring(0, 20);
      }

      String app_materno = "";
      if (!bpartner.isScoIscompany()) {
        app_materno = bpartner.getScoLastname2();
        if (app_materno == null)
          app_materno = "";
        if (app_materno.length() > 20)
          app_materno = app_materno.substring(0, 20);
      }

      String nombres = "";
      if (!bpartner.isScoIscompany()) {
        nombres = bpartner.getScoFirstname();
        if (nombres == null)
          nombres = "";
        if (nombres.length() > 20)
          nombres = nombres.substring(0, 20);
      }

      String razon_social = "";
      if (bpartner.isScoIscompany()) {
        razon_social = bpartner.getName();
        if (razon_social.length() > 40)
          razon_social = razon_social.substring(0, 40);
      }

      String pais = "";
      String direccion = "";
      try {
        pais = bpartner.getBusinessPartnerLocationList().get(0).getLocationAddress().getCountry()
            .getScoSunatcode();
        if (pais == null)
          pais = "";

        direccion = bpartner.getBusinessPartnerLocationList().get(0).getName();
        if (direccion.length() > 30)
          direccion = direccion.substring(0, 30);
      } catch (Exception ex) {
      }

      String base_legal = "1";
      String renta_neta = hashbyBPartner.get(cbpartnerIds[i]).toPlainString();
      String retencion = hashbyBPartnerRetencion.get(cbpartnerIds[i]).toPlainString();
      String tipoRenta = "99";
      sb.append(tipoPersona + "|" + tipo_desc + "|" + app_paterno + "|" + app_materno + "|"
          + nombres + "|" + razon_social + "|" + renta_neta + "|" + retencion + "|" + pais + "|"
          + direccion + "|" + tipoRenta + "||||" + tipoDocNoDomiciliado + "|" + numero_doc + "||"
          + "04" + "|\r\n");
      sunatPdt.numEntries++;
    }

    sunatPdt.data = sb.toString();
    return sunatPdt;
  }

  public static StructPdt getPDT617_Dividendos(ConnectionProvider provider, Date dttFrom,
      Date dttTo, Organization org) throws Exception {

    String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_JAVA);

    String strDttFrom = sdf.format(dttFrom);
    String strDttTo = sdf.format(dttTo);

    StringBuffer sb = new StringBuffer();
    StructPdt sunatPdt = new StructPdt();
    sunatPdt.numEntries = 0;
    sunatPdt.negAmount = 0;
    sunatPdt.posAmount = 0;
    sunatPdt.data = "";

    SunatUtilData[] invoices = SunatUtilData.getPDT617Dividendos(provider, org.getClient().getId(),
        org.getId(), strDttFrom, strDttTo);

    HashMap<String, BigDecimal> hashbyBPartner = new HashMap<String, BigDecimal>();
    HashMap<String, BigDecimal> hashbyBPartnerRetencion = new HashMap<String, BigDecimal>();
    for (int i = 0; i < invoices.length; i++) {

      BigDecimal totalamt = hashbyBPartner.get(invoices[i].cBpartnerId);
      BigDecimal totalRet = hashbyBPartnerRetencion.get(invoices[i].cBpartnerId);
      if (totalamt == null) {
        hashbyBPartner.put(invoices[i].cBpartnerId, new BigDecimal(invoices[i].baseamount));
        hashbyBPartnerRetencion.put(invoices[i].cBpartnerId, new BigDecimal(invoices[i].retamount));
      } else {
        hashbyBPartner.put(invoices[i].cBpartnerId,
            totalamt.add(new BigDecimal(invoices[i].baseamount)));
        hashbyBPartnerRetencion.put(invoices[i].cBpartnerId,
            totalRet.add(new BigDecimal(invoices[i].retamount)));
      }
    }

    Set<String> cbpartners = hashbyBPartner.keySet();
    String[] cbpartnerIds = new String[cbpartners.size()];
    cbpartners.toArray(cbpartnerIds);

    for (int i = 0; i < cbpartnerIds.length; i++) {
      BusinessPartner bpartner = OBDal.getInstance().get(BusinessPartner.class, cbpartnerIds[i]);
      SCRComboItem tipodoc = bpartner.getScrComboItem();
      String tipodoc_value = tipodoc.getSearchKey();
      String tipo_documento = "8";
      if (tipodoc_value.equals("DNI")) {
        tipo_documento = "1";
      } else if (tipodoc_value.equals("CARNET DE EXTRANJERIA")) {
        tipo_documento = "4";
      } else if (tipodoc_value.equals("REGISTRO UNICO DE CONTRIBUYENTES")) {
        tipo_documento = "6";
      } else if (tipodoc_value.equals("PASAPORTE")) {
        tipo_documento = "7";
      } else {
        tipo_documento = "0"; // throw new Exception();
      }

      String condicionDomicilio = "1";
      if (tipo_documento.equals("0")) {
        condicionDomicilio = "2";
      }

      String numero_doc = bpartner.getTaxID();
      String tipoPersona = bpartner.isScoIscompany() == true ? "2" : "1";
      String tipoDocNoDomiciliado = "01";
      String tipo_desc = "1";
      if (bpartner.isScoIscompany()) {
        tipo_desc = "2";
        tipoDocNoDomiciliado = "02";
      }

      String app_paterno = "";
      if (!bpartner.isScoIscompany()) {
        app_paterno = bpartner.getScoLastname();
        if (app_paterno == null)
          app_paterno = "";
        if (app_paterno.length() > 20)
          app_paterno = app_paterno.substring(0, 20);
      }

      String app_materno = "";
      if (!bpartner.isScoIscompany()) {
        app_materno = bpartner.getScoLastname2();
        if (app_materno == null)
          app_materno = "";
        if (app_materno.length() > 20)
          app_materno = app_materno.substring(0, 20);
      }

      String nombres = "";
      if (!bpartner.isScoIscompany()) {
        nombres = bpartner.getScoFirstname();
        if (nombres == null)
          nombres = "";
        if (nombres.length() > 20)
          nombres = nombres.substring(0, 20);
      }

      String razon_social = "";
      if (bpartner.isScoIscompany()) {
        razon_social = bpartner.getName();
        if (razon_social.length() > 40)
          razon_social = razon_social.substring(0, 40);
      }

      String pais = "";
      String direccion = "";
      try {
        pais = bpartner.getBusinessPartnerLocationList().get(0).getLocationAddress().getCountry()
            .getScoSunatcode();
        if (pais == null)
          pais = "";

        direccion = bpartner.getBusinessPartnerLocationList().get(0).getName();
        if (direccion.length() > 40)
          direccion = direccion.substring(0, 40);
      } catch (Exception ex) {
      }

      BigDecimal renta_neta = hashbyBPartner.get(cbpartnerIds[i]);
      BigDecimal retencion = hashbyBPartnerRetencion.get(cbpartnerIds[i]);

      String tasa = "0.00";

      try {
        tasa = (retencion.divide(renta_neta, 3, RoundingMode.HALF_UP).multiply(new BigDecimal(100))
            .setScale(2, RoundingMode.HALF_UP)).stripTrailingZeros().toPlainString();
      } catch (Exception ex) {
        ex.printStackTrace();
      }

      sb.append(tipoPersona + "|" + condicionDomicilio + "|" + tipo_documento + "|" + numero_doc
          + "|" + tipo_desc + "|" + app_paterno + "|" + app_materno + "|" + nombres + "|"
          + razon_social + "|" + renta_neta + "|" + tasa + "||||" + "04" + "|" + direccion + "|"
          + pais + "|\r\n");
      sunatPdt.numEntries++;
    }

    sunatPdt.data = sb.toString();
    return sunatPdt;
  }

  public static StructPdt getPDT3500_costos(ConnectionProvider provider, Date dttFrom, Date dttTo,
      Organization org) throws Exception {

    String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_JAVA);

    String strDttFrom = sdf.format(dttFrom);
    String strDttTo = sdf.format(dttTo);

    // get this year uit
    Calendar calendar = new GregorianCalendar();
    calendar.setTime(dttTo);
    int year = calendar.get(Calendar.YEAR);
    String uit = SunatUtilData.selectUIT(provider, org.getClient().getId(), org.getId(),
        String.valueOf(year));
    if (uit == null) {
      uit = "0.00";// throw new Exception();
    }
    int uitx2 = Integer.parseInt(uit) * 2;
    StringBuffer sb = new StringBuffer();
    StructPdt sunatPdt = new StructPdt();
    sunatPdt.numEntries = 0;
    sunatPdt.negAmount = 0;
    sunatPdt.posAmount = 0;
    sunatPdt.data = "";

    SunatUtilData[] bpartners = SunatUtilData.selectBPartnersFor3500c(provider,
        org.getClient().getId(), org.getId(), String.valueOf(year), String.valueOf(uitx2));
    for (int i = 0; i < bpartners.length; i++) {
      BusinessPartner bpartner = OBDal.getInstance().get(BusinessPartner.class,
          bpartners[i].cBpartnerId);

      String corr = String.valueOf(i + 1);
      String tipodoc_declarante = "6"; // always RUC
      String ndoc_declarante = org.getOrganizationInformationList().get(0).getTaxID();
      String periodo = String.valueOf(year);

      SCRComboItem tipodoc = bpartner.getScrComboItem();
      String tipodoc_value = tipodoc.getSearchKey();
      String tipodoc_declarado = "-";
      if (tipodoc_value.equals("DNI")) {
        tipodoc_declarado = "1";
      } else if (tipodoc_value.equals("CARNET DE EXTRANJERIA")) {
        tipodoc_declarado = "4";
      } else if (tipodoc_value.equals("REGISTRO UNICO DE CONTRIBUYENTES")) {
        tipodoc_declarado = "6";
      } else if (tipodoc_value.equals("PASAPORTE")) {
        tipodoc_declarado = "7";
      } else {
        tipodoc_declarado = "0"; // throw new Exception();
      }

      String ndoc_declarado = bpartner.getTaxID();
      String tipop_declarado = "01";
      if (bpartner.isScoIscompany()) {
        tipop_declarado = "02";
      }

      if (bpartner.getBusinessPartnerCategory().getSearchKey()
          .compareTo("Proveedor extranjero") == 0) {
        tipop_declarado = "03";
      }

      String importetotal = bpartners[i].totalamt;

      String app_paterno = "";
      if (!bpartner.isScoIscompany()) {
        app_paterno = bpartner.getScoLastname();
        if (app_paterno == null)
          app_paterno = "";
      }

      String app_materno = "";
      if (!bpartner.isScoIscompany()) {
        app_materno = bpartner.getScoLastname2();
        if (app_materno == null)
          app_materno = "";
      }

      String nombre_1 = "";
      String nombre_2 = "";
      if (!bpartner.isScoIscompany()) {
        String nombres = bpartner.getScoFirstname();
        String[] parts = nombres.split(" ");
        if (parts.length > 0) {
          nombre_1 = parts[0];
          if (parts.length > 1) {
            nombre_2 = parts[1];
            for (int k = 2; k < parts.length; k++)
              nombre_2 = nombre_2 + " " + parts[k];
          }
        }
      }

      String razon_social = "";
      if (bpartner.isScoIscompany()) {
        razon_social = bpartner.getName();
      }

      sb.append(corr + "|" + tipodoc_declarante + "|" + ndoc_declarante + "|" + periodo + "|"
          + tipop_declarado + "|" + tipodoc_declarado + "|" + ndoc_declarado + "|" + importetotal
          + "|" + app_paterno + "|" + app_materno + "|" + nombre_1 + "|" + nombre_2 + "|"
          + razon_social + "|\r\n");
      sunatPdt.numEntries++;
    }
    sunatPdt.data = sb.toString();
    return sunatPdt;
  }

  public static StructPdt getPDT3500_ingresos(ConnectionProvider provider, Date dttFrom, Date dttTo,
      Organization org) throws Exception {

    String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_JAVA);

    String strDttFrom = sdf.format(dttFrom);
    String strDttTo = sdf.format(dttTo);

    // get this year uit
    Calendar calendar = new GregorianCalendar();
    calendar.setTime(dttTo);
    int year = calendar.get(Calendar.YEAR);
    String uit = SunatUtilData.selectUIT(provider, org.getClient().getId(), org.getId(),
        String.valueOf(year));

    if (uit == null) {
      uit = "0.00";// throw new Exception();
    }
    int uitx2 = Integer.parseInt(uit) * 2;
    StringBuffer sb = new StringBuffer();
    StructPdt sunatPdt = new StructPdt();
    sunatPdt.numEntries = 0;
    sunatPdt.negAmount = 0;
    sunatPdt.posAmount = 0;
    sunatPdt.data = "";

    SunatUtilData[] bpartners = SunatUtilData.selectBPartnersFor3500i(provider,
        org.getClient().getId(), org.getId(), String.valueOf(year), String.valueOf(uitx2));
    for (int i = 0; i < bpartners.length; i++) {
      BusinessPartner bpartner = OBDal.getInstance().get(BusinessPartner.class,
          bpartners[i].cBpartnerId);

      String corr = String.valueOf(i + 1);
      String tipodoc_declarante = "6"; // always RUC
      String ndoc_declarante = org.getOrganizationInformationList().get(0).getTaxID();
      String periodo = String.valueOf(year);

      SCRComboItem tipodoc = bpartner.getScrComboItem();
      String tipodoc_value = tipodoc.getSearchKey();
      String tipodoc_declarado = "-";
      if (tipodoc_value.equals("DNI")) {
        tipodoc_declarado = "1";
      } else if (tipodoc_value.equals("CARNET DE EXTRANJERIA")) {
        tipodoc_declarado = "4";
      } else if (tipodoc_value.equals("REGISTRO UNICO DE CONTRIBUYENTES")) {
        tipodoc_declarado = "6";
      } else if (tipodoc_value.equals("PASAPORTE")) {
        tipodoc_declarado = "7";
      } else {
        tipodoc_declarado = "0"; // throw new Exception();
      }

      String ndoc_declarado = bpartner.getTaxID();
      String tipop_declarado = "01";
      if (bpartner.isScoIscompany()) {
        tipop_declarado = "02";
      }

      if (bpartner.getBusinessPartnerCategory().getSearchKey()
          .compareTo("Proveedor extranjero") == 0) {
        tipop_declarado = "03";

      }

      String importetotal = bpartners[i].totalamt;

      String app_paterno = "";
      if (!bpartner.isScoIscompany()) {
        app_paterno = bpartner.getScoLastname();
        if (app_paterno == null)
          app_paterno = "";
      }

      String app_materno = "";
      if (!bpartner.isScoIscompany()) {
        app_materno = bpartner.getScoLastname2();
        if (app_materno == null)
          app_materno = "";
      }

      String nombre_1 = "";
      String nombre_2 = "";
      if (!bpartner.isScoIscompany()) {
        String nombres = bpartner.getScoFirstname();
        String[] parts = nombres.split(" ");
        if (parts.length > 0) {
          nombre_1 = parts[0];
          if (parts.length > 1) {
            nombre_2 = parts[1];
            for (int k = 2; k < parts.length; k++)
              nombre_2 = nombre_2 + " " + parts[k];
          }
        }
      }

      String razon_social = "";
      if (bpartner.isScoIscompany()) {
        razon_social = bpartner.getName();
      }

      sb.append(corr + "|" + tipodoc_declarante + "|" + ndoc_declarante + "|" + periodo + "|"
          + tipop_declarado + "|" + tipodoc_declarado + "|" + ndoc_declarado + "|" + importetotal
          + "|" + app_paterno + "|" + app_materno + "|" + nombre_1 + "|" + nombre_2 + "|"
          + razon_social + "|\r\n");
      sunatPdt.numEntries++;
    }
    sunatPdt.data = sb.toString();
    return sunatPdt;
  }

  static private String getMontoEnSoles(BigDecimal rate, String monto) {
    if (monto == null)
      return null;

    if (!monto.isEmpty()) {
      BigDecimal montoME = new BigDecimal(monto).setScale(12, RoundingMode.HALF_UP);
      montoME = montoME.multiply(rate).setScale(2, RoundingMode.HALF_UP);
      return montoME.toString();
    }
    return "";
  }

  public static StructPle getFACEResumenVentas(ConnectionProvider provider, Date dttFrom,
      Date dttTo, Organization org) throws Exception {

    StringBuffer sb = new StringBuffer();
    StructPle sunatPle = new StructPle();
    sunatPle.numEntries = 0;

    String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_JAVA);

    SunatUtilData[] data = SunatUtilData.selectInvoiceResumenVentas(provider, org.getId(),
        sdf.format(dttFrom), sdf.format(dttTo));

    /*
     * OBCriteria<Invoice> invFilter = OBDal.getInstance().createCriteria(Invoice.class);
     * invFilter.add(Restrictions.between(Invoice.PROPERTY_ACCOUNTINGDATE, dttFrom, dttTo));
     * invFilter.add(Restrictions.sqlRestriction("AD_ISORGINCLUDED( this_.ad_org_id, " + "'" +
     * org.getId() + "','" + org.getClient().getId() + "') > -1"));
     * invFilter.add(Restrictions.eq(Invoice.PROPERTY_SALESTRANSACTION, true));
     * invFilter.add(Restrictions.eq(Invoice.PROPERTY_DOCUMENTSTATUS, "CO"));
     * 
     * invFilter.addOrderBy(Invoice.PROPERTY_ACCOUNTINGDATE, true);
     * 
     * 
     * List<Invoice> invoices = invFilter.list();
     */
    for (int i = 0; i < data.length; i++) {

      SunatUtilData inv = data[i];

      // FACTURA - BOLETA - NOTA DE CREDITO - NOTA DE DEBITO
      String str = inv.emScoSpecialdoctype;

      if (str.equals("SCOARCREDITMEMO") || str.equals("SCOARINVOICERETURNMAT")
          || str.equals("SCOARINVOICE") || str.equals("SCOARTICKET")
          || str.equals("SCOARDEBITMEMO")) {

        String tipo = "00";

        String motivo = "7";

        if (str.equals("SCOARCREDITMEMO")) {
          tipo = "07";
        }
        if (str.equals("SCOARINVOICERETURNMAT")) {
          tipo = "07";
        }
        if (str.equals("SCOARINVOICE")) {
          tipo = "01";
        }
        if (str.equals("SCOARTICKET")) {
          tipo = "03";
        }
        if (str.equals("SCOARDEBITMEMO")) {
          tipo = "08";
        }

        StringTokenizer st = new StringTokenizer(inv.emScrPhysicalDocumentno, "-");
        String numSerie = "0" + st.nextToken().trim();
        String numComprobante = st.nextToken().trim();
        String rangoTicket = "";

        String tdoc = inv.partnerdoc;
        String ruc = inv.taxid;
        String razon = inv.name;

        if (razon.length() > 60)
          razon = razon.substring(0, 60);

        double totalBaseImponible = 0;
        double totalExonerada = 0;
        double totalVentaInafecta = 0;
        double totalISC = 0;
        double totalIGV = 0;
        double totalOtrosTributos = 0;
        double totalBruto = 0;

        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        DateFormat df2 = new SimpleDateFormat("dd/MM/yyyy");
        Date fecUsar = df.parse(inv.dateacct);

        String strFechaEmision = df2.format(fecUsar);

        Invoice invoice = null;// SOLO PARA NOTAS DE CREDITO Y DEBITO
        if (tipo.equals("07") || tipo.equals("08")) {
          invoice = OBDal.getInstance().get(Invoice.class, inv.cInvoiceId);
          fecUsar = AcctServer.getDateForInvoiceReference(invoice);
        }

        String strFecUsar = df.format(fecUsar);

        String amountConverted = AcctServer.getConvertedAmt("10000000000.00", inv.cCurrencyId,
            "308", strFecUsar, "", inv.adClientId, inv.adOrgId, new DalConnectionProvider());
        BigDecimal tipoCambio = new BigDecimal(amountConverted)
            .divide(new BigDecimal("10000000000.00"), 12, BigDecimal.ROUND_HALF_UP);

        totalIGV = Double.valueOf(getMontoEnSoles(tipoCambio, inv.igv));
        totalBaseImponible = Double.valueOf(getMontoEnSoles(tipoCambio, inv.baseimponible));
        totalExonerada = Double.valueOf(getMontoEnSoles(tipoCambio, inv.totalexonerado));
        totalVentaInafecta = Double.valueOf(getMontoEnSoles(tipoCambio, inv.totalinafecta));
        totalOtrosTributos = Double.valueOf(getMontoEnSoles(tipoCambio, inv.totalotrostributos));

        if (tipo.equals("07")) {
          totalIGV = Math.abs(totalIGV);
          totalBaseImponible = Math.abs(totalBaseImponible);
          totalExonerada = Math.abs(totalExonerada);
          totalVentaInafecta = Math.abs(totalVentaInafecta);
          totalOtrosTributos = Math.abs(totalOtrosTributos);
        }

        totalBruto = totalOtrosTributos + totalVentaInafecta + totalExonerada + totalBaseImponible
            + totalIGV;

        String tipoRef = "";
        String serieRef = "";
        String numeroRef = "";

        if (tipo.equals("07") || tipo.equals("08")) {

          if (invoice.isScoIsmanualref()) {
            tipoRef = "01";

            StringTokenizer st2 = new StringTokenizer(invoice.getScoManualinvoiceref(), "-");
            serieRef = SunatUtil.LPad(st2.nextToken().trim(), 4, '0');

            try {
              numeroRef = st2.nextToken().trim();
            } catch (Exception ex) {
            }

          } else {

            if (invoice.getScoInvoiceref() != null) {
              StringTokenizer st2 = new StringTokenizer(
                  invoice.getScoInvoiceref().getScrPhysicalDocumentno(), "-");
              serieRef = st2.nextToken().trim();
              numeroRef = "0" + st2.nextToken().trim();

              String str2 = invoice.getScoInvoiceref().getDocumentType().getScoSpecialdoctype();
              if (str2.equals("SCOARCREDITMEMO")) {
                tipoRef = "07";
              }
              if (str2.equals("SCOARINVOICERETURNMAT")) {
                tipoRef = "07";
              }
              if (str2.equals("SCOARINVOICE")) {
                tipoRef = "01";
              }
              if (str2.equals("SCOARTICKET")) {
                tipoRef = "03";
              }
              if (str2.equals("SCOARDEBITMEMO")) {
                tipoRef = "08";
              }

            }
          }

        }

        String linea = motivo + "|" + strFechaEmision + "|" + tipo + "|" + numSerie + "|"
            + numComprobante + "||" + tdoc + "|" + ruc + "|" + razon + "|"
            + String.format("%.2f", totalBaseImponible) + "|"
            + String.format("%.2f", totalExonerada) + "|"
            + String.format("%.2f", totalVentaInafecta) + "|" + String.format("%.2f", totalISC)
            + "|" + String.format("%.2f", totalIGV) + "|"
            + String.format("%.2f", totalOtrosTributos) + "|" + String.format("%.2f", totalBruto)
            + "|" + tipoRef + "|" + serieRef + "|" + numeroRef + "|";

        if (!sb.toString().equals(""))
          sb.append("\n");
        sb.append(linea);
        sunatPle.numEntries++;

      }
    }
    sunatPle.data = sb.toString();
    SimpleDateFormat dt = new SimpleDateFormat("ddMMyyyy");
    String rucAdq = SunatUtil.LPad(org.getOrganizationInformationList().get(0).getTaxID(), 11, '0');

    sunatPle.filename = rucAdq + "-RF-" + dt.format(dttFrom) + "-01.txt";

    return sunatPle;
  }

  public static StructPle getFACEResumenRetenciones(ConnectionProvider provider, Date dttFrom,
      Date dttTo, Organization org) throws OBException, Exception {

    StringBuffer sb = new StringBuffer();
    StructPle sunatPle = new StructPle();
    sunatPle.numEntries = 0;

    String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_JAVA);

    SunatUtilData[] data = SunatUtilData.selectInvoiceResumenRetenciones(provider, org.getId(),
        sdf.format(dttFrom), sdf.format(dttTo));

    String motivo = "6";

    DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
    DateFormat df2 = new SimpleDateFormat("dd/MM/yyyy");

    for (int i = 0; i < data.length; i++) {

      SunatUtilData inv = data[i];
      // System.out.println("inv.phydoc:" + inv.phydoc + " - inv.tasa:" + inv.tasa +
      // " - inv.totalretenido:" + inv.totalretenido + " - inv.totalpagado:" + inv.totalpagado +
      // " - inv.tc" + inv.tc);

      StringTokenizer st = new StringTokenizer(inv.withholdingnumber, "-");
      String numSerieRet = SunatUtil.LPad(st.nextToken(), 4, '0');
      String numComprobanteRet = st.nextToken();

      Date fecRetencion = df.parse(inv.dateacct);
      String strFechaRetencion = df2.format(fecRetencion);

      // PROVEEDOR
      String tdoc = inv.partnerdoc;
      String ruc = inv.taxid;
      String razon = inv.name;

      // DATOS DE RETENCION
      String regimen = "01";
      String tasa = inv.tasa;

      String monedaInv = inv.monedainv;

      if (monedaInv.equals("100"))
        monedaInv = "USD";
      else if (monedaInv.equals("308"))
        monedaInv = "PEN";

      String importTotalRetenido = inv.totalretenido;
      String importeTotalPagado = inv.totalpagado;
      double tC = Double.valueOf(inv.tc);
      if (!monedaInv.equals("PEN")) {// !soles
        importTotalRetenido = String.valueOf(Double.valueOf(importTotalRetenido) * tC);
        importeTotalPagado = String.valueOf(Double.valueOf(importeTotalPagado) * tC);
      }

      // System.out.println("importTotalRetenido:" + importTotalRetenido + " - importeTotalPagado:"
      // + importeTotalPagado);
      SCOPwithholdingReceipt receipt = OBDal.getInstance().get(SCOPwithholdingReceipt.class,
          inv.scoPwithholdingReceiptId);
      List<SCOPwithhoRecLine> lsLines = receipt.getSCOPwithhoRecLineList();

      BigDecimal totalSolesRet = new BigDecimal(0);
      BigDecimal totalSolesPag = new BigDecimal(0);
      // System.out.println("Processing totalsolesret and pag:");
      for (int h = 0; h < lsLines.size(); h++) {
        BigDecimal solesRet = lsLines.get(h).getAmount();
        BigDecimal solesPag = lsLines.get(h).getPaymentamount();

        // System.out.println("before solesRet:" + solesRet + " - solesPag:" + solesPag);
        if (!lsLines.get(h).getInvoiceref().getCurrency().getId().equals("308")) {
          Date paymentdate = receipt.getFINWithholdingpayment().getPaymentDate();
          if (paymentdate == null)
            paymentdate = receipt.getAccountingDate();

          String invTc = SunatUtilData.getRetencionTcFromInv(provider, sdf.format(paymentdate),
              lsLines.get(h).getInvoiceref().getCurrency().getId(), org.getId());
          if (invTc.isEmpty()) {
            throw new OBException("@SCO_FACEResumenRetencionesNotc@");
          }
          solesRet = solesRet.multiply(new BigDecimal(invTc)).setScale(2, BigDecimal.ROUND_HALF_UP);
          solesPag = solesPag.multiply(new BigDecimal(invTc)).setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        // System.out.println("after:" + solesRet + " - solesPag:" + solesPag);

        totalSolesRet = totalSolesRet.add(solesRet);
        totalSolesPag = totalSolesPag.add(solesPag);
      }
      // System.out.println("EndProcessing totalsolesret and pag: totalSolesRet:" + totalSolesRet +
      // " - totalSolesPag:" + totalSolesPag);

      for (int h = 0; h < lsLines.size(); h++) {

      }

      // INFORMACION DE COMPROBANTE
      String tdocinv = inv.tdocinv;

      st = new StringTokenizer(inv.phydoc, "-");
      String numSerieInv = SunatUtil.LPad(st.nextToken(), 4, '0');
      numSerieInv = StringUtils.leftPad(numSerieInv, 4, '0');
      if (numSerieInv.length() > 4) {
        // adjust to 4 digits only if we are erasing '0' otherwise throw error
        if (numSerieInv.substring(0, numSerieInv.length() - 4).matches("[0]+")) {
          numSerieInv = numSerieInv.substring(numSerieInv.length() - 4, numSerieInv.length());
        }
      }
      String numComprobanteInv = st.nextToken();

      Date fecInv = df.parse(inv.fecinv);
      String strFechaInv = df2.format(fecInv);

      String importeTotalInv = inv.totalinv;

      // DATOS DEL PAGO
      Date fecPago = df.parse(inv.paymentdate);
      String strFechaPago = df2.format(fecPago);

      String numeroPago = inv.correlativo;
      if (numeroPago.length() < 3) {
        throw new OBException("@SCO_FACEResumenRetencionesInvalidCorr@");
      }
      if (numeroPago.substring(numeroPago.length() - 3).compareTo("000") == 0) {
        throw new OBException("@SCO_FACEResumenRetencionesPaymentNotPosted@" + inv.paymentdocno);
      }

      String importePago = (new BigDecimal(inv.totalpagado).add(new BigDecimal(inv.totalretenido)))
          .toString();

      String monedaImportePago = monedaInv;

      // DATOS DE RETENCION
      String importeRetenido = importTotalRetenido;

      // TIPO CAMBIO
      String monedaTC = "";
      String tcAplicado = "";
      String fechaTC = "";
      if (!monedaInv.equals("PEN")) {

        monedaTC = monedaInv;
        tcAplicado = inv.tc;
        fechaTC = strFechaPago;
      }

      // FACTURA - BOLETA - NOTA DE CREDITO - NOTA DE DEBITO

      String linea = motivo + "|" + numSerieRet + "|" + numComprobanteRet + "|" + strFechaRetencion
          + "|" + ruc + "|" + tdoc + "|" + razon + "|" + regimen + "|"
          + String.format("%.2f", Double.valueOf(tasa)) + "|"
          + String.format("%.2f", Double.valueOf(totalSolesRet.toString())) + "|"
          + String.format("%.2f", Double.valueOf(totalSolesPag.toString())) + "|" + tdocinv + "|"
          + numSerieInv + "|" + numComprobanteInv + "|" + strFechaInv + "|"
          + String.format("%.2f", Double.valueOf(importeTotalInv)) + "|" + monedaInv + "|"
          + strFechaPago + "|" + numeroPago + "|"
          + String.format("%.2f", Double.valueOf(importePago)) + "|" + monedaImportePago + "|"
          + String.format("%.2f", Double.valueOf(importeRetenido)) + "|" + strFechaRetencion + "|"
          + String.format("%.2f", Double.valueOf(importeTotalPagado)) + "|" + monedaTC + "|"
          + tcAplicado + "|" + fechaTC + "|";

      // System.out.println("linea :" + linea);
      if (!sb.toString().equals(""))
        sb.append("\n");
      sb.append(linea);
      sunatPle.numEntries++;

    }
    sunatPle.data = sb.toString();
    SimpleDateFormat dt = new SimpleDateFormat("yyyyMMdd");
    String rucAdq = SunatUtil.LPad(org.getOrganizationInformationList().get(0).getTaxID(), 11, '0');

    sunatPle.filename = rucAdq + "-20-" + dt.format(dttFrom) + "-1.txt";

    return sunatPle;
  }

  public static String RPad(String s, int n, char car) {
    return String.format("%1$-" + n + "s", s).replace(' ', car);
  }

  public static String LPad(String s, int n, char car) {
    return String.format("%1$" + n + "s", s).replace(' ', car);
  }

  public static StructPle getCuentasxCobrarComercialesRelacionadas_14(ConnectionProvider provider,
      Date dateTo, Organization org) throws Exception {

    StringBuffer sb = new StringBuffer();
    StructPle sunatPle = new StructPle();
    sunatPle.numEntries = 0;

    String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_JAVA);

    SimpleDateFormat dt = new SimpleDateFormat("yyyyMMdd");

    // get this year uit
    Calendar caldttTo = new GregorianCalendar();
    caldttTo.setTime(dateTo);
    int year = caldttTo.get(Calendar.YEAR);

    // get first day of year
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.DAY_OF_YEAR, 1);
    Date dateFrom = cal.getTime();

    SunatUtilData[] data = SunatUtilData.getSaldoCuenta14(provider, sdf.format(dateTo),
        org.getId());

    String period = dt.format(dateTo); // Periodo
    String assetNo = "M00000"; // Nro Asiento (correlativo)
    String OpStatus = "1"; // Estado de la Operación

    for (int i = 0; i < data.length; i++) {

      SunatUtilData doc = data[i];

      String regnumber = doc.cuo.replaceAll("-", "").replaceAll("_", ""); // CUO

      if (regnumber == null || regnumber.equals(""))
        continue;

      String bPartnerID = doc.cBpartnerId;
      String bpDocumentType = ""; // Tipo de documento
      String bpDocNumber = ""; // Nro de documento
      String bpName = ""; // Razon Social

      if (bPartnerID != null && !bPartnerID.equals("")) {
        BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class, bPartnerID);

        if (bp != null) {
          bpDocumentType = bp.getScrComboItem().getCode();
          bpDocNumber = bp.getTaxID();
          bpName = bp.getName();
        }
      }

      if (bpDocNumber == null || bpDocNumber.equals("")) {
        bpDocumentType = "0";
        bpDocNumber = "000";
        bpName = "varios";
      }

      DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
      DateFormat df2 = new SimpleDateFormat("dd/MM/yyyy");
      Date prev_dateacct = df.parse(doc.dateacct);

      String dateacct = df2.format(prev_dateacct); // Fecha de Emision

      double amtToPay = 0; // Monto Cta x Cobrar
      amtToPay = Double.valueOf(doc.amt);

      String linea = period + "|" + regnumber + "|" + assetNo + "|" + bpDocumentType + "|"
          + bpDocNumber + "|" + bpName + "|" + dateacct + "|" + String.format("%.2f", amtToPay)
          + "|" + OpStatus + "|";

      if (!sb.toString().equals(""))
        sb.append("\n");
      sb.append(linea);
      sunatPle.numEntries++;

    }

    sunatPle.data = sb.toString();
    String rucAdq = SunatUtil.LPad(org.getOrganizationInformationList().get(0).getTaxID(), 11, '0');

    String filename = "LE" + rucAdq + dt.format(dateTo) + "030400011111.TXT"; // LERRRRRRRRRRRAAAAMMDD030400CCOIM1.TXT

    sunatPle.filename = filename;

    return sunatPle;
  }

  public static StructPle getCuentasxCobrarComercialesRelacionadas_16_17(
      ConnectionProvider provider, Date dateTo, Organization org) throws Exception {

    StringBuffer sb = new StringBuffer();
    StructPle sunatPle = new StructPle();
    sunatPle.numEntries = 0;

    String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_JAVA);

    SimpleDateFormat dt = new SimpleDateFormat("yyyyMMdd");

    // get this year uit
    Calendar caldttTo = new GregorianCalendar();
    caldttTo.setTime(dateTo);
    int year = caldttTo.get(Calendar.YEAR);

    // get first day of year
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.DAY_OF_YEAR, 1);
    Date dateFrom = cal.getTime();

    SunatUtilData[] data = SunatUtilData.getSaldoCuenta16y17(provider, sdf.format(dateFrom),
        sdf.format(dateTo), org.getId());

    String period = dt.format(dateTo); // Periodo
    String assetNo = "M00000"; // Nro Asiento (correlativo)
    String OpStatus = "1"; // Estado de la Operación

    for (int i = 0; i < data.length; i++) {

      SunatUtilData doc = data[i];

      String regnumber = doc.cuo.replaceAll("-", "").replaceAll("_", ""); // CUO

      if (regnumber == null || regnumber.equals(""))
        continue;

      String bPartnerID = doc.cBpartnerId;
      String bpDocumentType = ""; // Tipo de documento
      String bpDocNumber = ""; // Nro de documento
      String bpName = ""; // Razon Social

      if (bPartnerID != null && !bPartnerID.equals("")) {
        BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class, bPartnerID);

        if (bp != null) {
          bpDocumentType = bp.getScrComboItem().getCode();
          bpDocNumber = bp.getTaxID();
          bpName = bp.getName();
        }
      }

      if (bpDocNumber == null || bpDocNumber.equals("")) {
        bpDocumentType = "0";
        bpDocNumber = "000";
        bpName = "varios";
      }

      DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
      DateFormat df2 = new SimpleDateFormat("dd/MM/yyyy");
      Date prev_dateacct = df.parse(doc.dateacct);

      String dateacct = df2.format(prev_dateacct); // Fecha de Emision

      double amtToPay = 0; // Monto Cta x Cobrar
      amtToPay = Double.valueOf(doc.amt);

      String linea = period + "|" + regnumber + "|" + assetNo + "|" + bpDocumentType + "|"
          + bpDocNumber + "|" + bpName + "|" + dateacct + "|" + String.format("%.2f", amtToPay)
          + "|" + OpStatus + "|";

      if (!sb.toString().equals(""))
        sb.append("\n");
      sb.append(linea);
      sunatPle.numEntries++;

    }

    sunatPle.data = sb.toString();
    String rucAdq = SunatUtil.LPad(org.getOrganizationInformationList().get(0).getTaxID(), 11, '0');

    String filename = "LE" + rucAdq + dt.format(dateTo) + "030500011111.TXT"; // LERRRRRRRRRRRAAAAMMDD030300CCOIM1.TXT

    sunatPle.filename = filename;

    return sunatPle;
  }

  public static StructPle getCuentasCobranzaDudosa_19(ConnectionProvider provider, Date dateTo,
      Organization org) throws Exception {

    StringBuffer sb = new StringBuffer();
    StructPle sunatPle = new StructPle();
    sunatPle.numEntries = 0;

    String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_JAVA);

    SimpleDateFormat dt = new SimpleDateFormat("yyyyMMdd");

    // get this year uit
    Calendar caldttTo = new GregorianCalendar();
    caldttTo.setTime(dateTo);
    int year = caldttTo.get(Calendar.YEAR);

    // get first day of year
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.DAY_OF_YEAR, 1);
    Date dateFrom = cal.getTime();

    SunatUtilData[] data = SunatUtilData.getSaldoCuenta19(provider, sdf.format(dateFrom),
        sdf.format(dateTo), org.getId());

    String period = dt.format(dateTo); // Periodo
    String assetNo = "M00000"; // Nro Asiento (correlativo)
    String OpStatus = "1"; // Estado de la Operación

    for (int i = 0; i < data.length; i++) {

      SunatUtilData doc = data[i];

      String regnumber = doc.cuo.replaceAll("-", "").replaceAll("_", ""); // CUO

      if (regnumber == null || regnumber.equals(""))
        continue;

      String bPartnerID = doc.cBpartnerId;
      String bpDocumentType = ""; // Tipo de documento
      String bpDocNumber = ""; // Nro de documento
      String bpName = ""; // Razon Social
      String physicalDoc = doc.phydoc;
      String specialDocType = doc.specialdoctype;
      String strserie = "";
      String strCorrelativo = "";
      String doctype = "";

      if (physicalDoc != null || !physicalDoc.equals("")) {

        StringTokenizer st = new StringTokenizer(physicalDoc, "-");
        // strserie = "0" + st.nextToken().trim();
        strserie = LPad(st.nextToken().trim(), 4, '0');
        strCorrelativo = st.nextToken().trim();
      }

      if (specialDocType.equals("SCOARINVOICE"))
        doctype = "01";
      else if (specialDocType.equals("SCOARTICKET"))
        doctype = "03";
      else if (specialDocType.equals("SCOARTICKET"))
        doctype = "08";
      else
        doctype = "00";

      if (bPartnerID != null && !bPartnerID.equals("")) {
        BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class, bPartnerID);

        if (bp != null) {
          bpDocumentType = bp.getScrComboItem().getCode();
          bpDocNumber = bp.getTaxID();
          bpName = bp.getName();
        }
      }

      if (bpDocNumber == null || bpDocNumber.equals("")) {
        bpDocumentType = "0";
        bpDocNumber = "000";
        bpName = "varios";
      }

      DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
      DateFormat df2 = new SimpleDateFormat("dd/MM/yyyy");
      Date prev_dateacct = df.parse(doc.dateacct);

      String dateacct = df2.format(prev_dateacct); // Fecha de Emision

      double amtToPay = 0; // Monto Cta x Cobrar
      amtToPay = Double.valueOf(doc.amt);

      String linea = period + "|" + regnumber + "|" + assetNo + "|" + bpDocumentType + "|"
          + bpDocNumber + "|" + bpName + "|" + doctype + "|" + strserie + "|" + strCorrelativo + "|"
          + dateacct + "|" + String.format("%.2f", amtToPay) + "|" + OpStatus + "|";

      if (!sb.toString().equals(""))
        sb.append("\n");
      sb.append(linea);
      sunatPle.numEntries++;

    }

    sunatPle.data = sb.toString();
    String rucAdq = SunatUtil.LPad(org.getOrganizationInformationList().get(0).getTaxID(), 11, '0');

    String filename = "LE" + rucAdq + dt.format(dateTo) + "030600011111.TXT"; // LERRRRRRRRRRRAAAAMMDD030300CCOIM1.TXT

    sunatPle.filename = filename;

    return sunatPle;
  }

  public static StructPle getCuentasxPagarComercialesRelacionadas_41(ConnectionProvider provider,
      Date dateTo, Organization org) throws Exception {

    StringBuffer sb = new StringBuffer();
    StructPle sunatPle = new StructPle();
    sunatPle.numEntries = 0;

    String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_JAVA);

    SimpleDateFormat dt = new SimpleDateFormat("yyyyMMdd");

    // get this year uit
    Calendar caldttTo = new GregorianCalendar();
    caldttTo.setTime(dateTo);
    int year = caldttTo.get(Calendar.YEAR);

    // get first day of year
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.DAY_OF_YEAR, 1);
    Date dateFrom = cal.getTime();

    SunatUtilData[] data = SunatUtilData.getSaldoCuenta41(provider, sdf.format(dateTo),
        org.getId());

    String period = dt.format(dateTo); // Periodo
    String assetNo = "M00000"; // Nro Asiento (correlativo)
    String OpStatus = "1"; // Estado de la Operación

    for (int i = 0; i < data.length; i++) {

      SunatUtilData doc = data[i];

      String regnumber = doc.cuo.replaceAll("-", "").replaceAll("_", ""); // CUO

      if (regnumber == null || regnumber.equals(""))
        continue;

      String cod_acct = doc.codcuenta;

      String bPartnerID = doc.cBpartnerId;
      String bpDocumentType = ""; // Tipo de documento
      String bpDocNumber = ""; // Nro de documento
      String bpName = ""; // Razon Social
      String bpCod = "";

      if (bPartnerID != null && !bPartnerID.equals("")) {
        BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class, bPartnerID);

        if (bp != null) {
          bpDocumentType = bp.getScrComboItem().getCode();
          bpDocNumber = bp.getTaxID();
          bpName = bp.getName();
          bpCod = bpDocNumber;
        }
      }

      if (bpDocNumber == null || bpDocNumber.equals("")) {
        bpDocumentType = "0";
        bpDocNumber = "000";
        bpName = "varios";
      }

      double amtToPay = 0; // Monto Cta x Pagar
      amtToPay = Double.valueOf(doc.amt);

      String linea = period + "|" + regnumber + "|" + assetNo + "|" + cod_acct + "|"
          + bpDocumentType + "|" + bpDocNumber + "|" + bpCod + "|" + bpName + "|"
          + String.format("%.2f", amtToPay) + "|" + OpStatus + "|";

      if (!sb.toString().equals(""))
        sb.append("\n");
      sb.append(linea);
      sunatPle.numEntries++;

    }

    sunatPle.data = sb.toString();
    String rucAdq = SunatUtil.LPad(org.getOrganizationInformationList().get(0).getTaxID(), 11, '0');

    String filename = "LE" + rucAdq + dt.format(dateTo) + "031100011111.TXT"; // LERRRRRRRRRRRAAAAMMDD031100CCOIM1.TXT

    sunatPle.filename = filename;

    return sunatPle;
  }

  /*
   * public static String RPad(String str, Integer length, char car) { return str +
   * String.format("%" + (length - str.length()) + "s", "").replace(" ", String.valueOf(car)); }
   * 
   * public static String LPad(String str, Integer length, char car) { return String.format("%" +
   * (length - str.length()) + "s", "").replace(" ", String.valueOf(car)) + str; }
   */
}