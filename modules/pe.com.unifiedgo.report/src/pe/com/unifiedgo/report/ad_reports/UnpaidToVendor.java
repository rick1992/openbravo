package pe.com.unifiedgo.report.ad_reports;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.ServletException;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.businessUtility.Tree;
import org.openbravo.erpCommon.businessUtility.TreeData;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.currency.ConversionRateDoc;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.service.db.DalConnectionProvider;

import pe.com.unifiedgo.accounting.data.SCOBillofexchange;
import pe.com.unifiedgo.accounting.data.SCOBoeFrom;
import pe.com.unifiedgo.accounting.data.SCOBoeTo;
import pe.com.unifiedgo.accounting.data.SCOBoefromPayschedetail;
import pe.com.unifiedgo.accounting.data.SCOPrepayment;
import pe.com.unifiedgo.accounting.data.SCOPwithhoRecLine;
import pe.com.unifiedgo.accounting.data.SCOPwithholdingReceipt;
import pe.com.unifiedgo.accounting.data.SCOPwlrecliPayschedetail;
import pe.com.unifiedgo.accounting.data.ScoRendicioncuentas;

class UnpaidToVendorLine implements Comparable<UnpaidToVendorLine> {

  public int groupNumber;
  public int groupDocument;
  public String invoicedDate;
  public String dueDate;
  public String docType;
  public String documentNumber;
  public String bankAccount;
  public String paymentCondition;
  public String currency;
  public BigDecimal debit;
  public BigDecimal credit;
  public BigDecimal balance;
  public BigDecimal percepcion;
  public String specialdoctype = "--";
  public String specialdoctypeplus;
  public String tipoOp = "--";
  public String docRecepcion = "--";
  public String estaFacturado = "--";
  public String paymentMethod = "--";
  public Long daysTillDue;
  public String orgName;

  public String recepcionDate = "--";

  public String documentId;

  public String ultFactura;
  public String ultPago;
  public String creditSol;
  public String creditDol;

  public Date dateVenc;

  @Override
  public int compareTo(UnpaidToVendorLine o) {
    if (o.groupDocument > groupDocument) {
      return -1;
    }
    if (o.groupDocument < groupDocument) {
      return 1;
    }
    return dateVenc.compareTo(o.dateVenc);
  }

}

class UnpaidToVendorLineWithPartner {
  public UnpaidToVendorLineWithPartner() {
    unpaidtovendors = new ArrayList<UnpaidToVendorLine>();
    cBpartnerId = "";
  }

  List<UnpaidToVendorLine> unpaidtovendors;
  String cBpartnerId;
}

class RetenRegVendor {
  Date transDate;
  String tipoDoc;
  String serieDoc;
  String numDoc;
  String totalDoc;
  String tipoTrans;
  String debe;
  String haber;
  String saldo;
  String serieComp;
  String numComp;
  String totalComp;
  String partnerUid;
  String partnerCommName;
  String partnerRuc;
  String currencySymbol;

  Invoice invo;
  String montoPagoOriginal;

  int groupNumber;
}

public class UnpaidToVendor {

  private static Date getEndOfDay(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    int year = calendar.get(Calendar.YEAR);
    int month = calendar.get(Calendar.MONTH);
    int day = calendar.get(Calendar.DATE);
    calendar.set(year, month, day, 23, 59, 59);
    return calendar.getTime();
  }

  public static HashMap<String, UnpaidToVendorLineWithPartner> getUnpaidByOrgToDate(
      ConnectionProvider conProvider, String adClientId, String strOrgId, Date startingDate,
      Date endingDate, boolean onlyPendings) throws ServletException, IOException {
    String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_JAVA);
    String strStartingDate = sdf.format(startingDate);
    String strEndingDate = sdf.format(endingDate);

    String strTreeOrg = TreeData.getTreeOrg(conProvider, adClientId);
    String strOrgFamily = Tree.getMembers(conProvider, strTreeOrg, strOrgId);

    HashMap<String, UnpaidToVendorLineWithPartner> map = new HashMap<String, UnpaidToVendorLineWithPartner>();

    UnpaidToVendorData[] data = UnpaidToVendorData.getPendingInvByOrg(conProvider, adClientId,
        strOrgFamily, strEndingDate);

    if (data == null || data.length == 0) {
      return map;
    }

    String ultFactura = "Ninguna";
    String ultPago = "Ninguno";
    int currentGroup = 1;
    int counter = 1;
    for (int i = 0; i < data.length; i++) {
      Invoice inv = OBDal.getInstance().get(Invoice.class, data[i].cInvoiceId);

      List<FIN_PaymentSchedule> schedules = inv.getFINPaymentScheduleList();

      Date dueDate = null;
      Date lastDueDate = null;
      for (int j = 0; j < schedules.size(); j++) {
        if (dueDate == null || schedules.get(j).getDueDate().after(dueDate))
          dueDate = schedules.get(j).getDueDate();

        if (lastDueDate == null || schedules.get(j).getDueDate().after(lastDueDate))
          lastDueDate = schedules.get(j).getDueDate();
      }
      if (dueDate == null)
        dueDate = lastDueDate;
      if (dueDate == null)
        dueDate = inv.getScoNewdateinvoiced();

      String tipoOp = "--";
      String documentType = "";
      String paymentMethod = "";
      if (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOAPINVOICE")) {
        documentType = "FC";
        if (inv.getScoPodocCmbiValue() != null
            && inv.getScoPodocCmbiValue().compareTo("tabla10_03") == 0)
          tipoOp = "Boleta";
        else if (inv.getScoPodocCmbiValue() != null
            && inv.getScoPodocCmbiValue().compareTo("tabla10_08") == 0)
          tipoOp = "Nota de Débito";
        else
          tipoOp = "Factura";
        paymentMethod = inv.getPaymentMethod().getName();
      } else if (inv.getTransactionDocument().getScoSpecialdoctype()
          .equals("SCOAPSIMPLEPROVISIONINVOICE")
          || inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOAPLOANINVOICE")) {
        documentType = "FC";
        tipoOp = "Factura";
      } else if (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOAPBOEINVOICE")) {
        documentType = "LC";
        tipoOp = "Letra";
      } else if (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOAPCREDITMEMO")) {
        documentType = "NA";
        tipoOp = "Nota de Crédito";
      }

      UnpaidToVendorLine lineFC = new UnpaidToVendorLine();
      lineFC.orgName = inv.getOrganization().getName();
      lineFC.specialdoctype = inv.getSCOSpecialDocType();
      lineFC.specialdoctypeplus = inv.getSsaSpecialdoctypeplus() != null
          ? inv.getSsaSpecialdoctypeplus()
          : inv.getSCOSpecialDocType();
      lineFC.groupNumber = currentGroup;
      lineFC.invoicedDate = OBDateUtils.formatDate(inv.getAccountingDate());
      lineFC.documentId = inv.getId();

      lineFC.dueDate = OBDateUtils.formatDate(dueDate);
      lineFC.dateVenc = dueDate;
      lineFC.currency = inv.getCurrency().getISOCode();
      lineFC.docType = documentType;
      lineFC.tipoOp = tipoOp;
      lineFC.paymentMethod = paymentMethod;
      lineFC.paymentCondition = "";
      if (documentType.equals("FC") || documentType.equals("LC") || documentType.equals("NA")) {
        lineFC.paymentCondition = inv.getPaymentTerms().getName();// inv.getPaymentTerms().getSearchKey();
        lineFC.docRecepcion = (inv.isScrIsreceived() ? "SI" : "NO");
        if (inv.isScrIsreceived()) {
          if (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOAPBOEINVOICE")) {
            if (inv.getSsaBoeDatereceipt() != null) {
              lineFC.recepcionDate = OBDateUtils.formatDate(inv.getSsaBoeDatereceipt());
            } else {
              lineFC.recepcionDate = OBDateUtils.formatDate(inv.getInvoiceDate());
            }
          } else {
            lineFC.recepcionDate = OBDateUtils.formatDate(inv.getInvoiceDate());
          }
        }

      }

      lineFC.documentNumber = inv.getScrPhysicalDocumentno();

      lineFC.percepcion = new BigDecimal(0);

      lineFC.debit = new BigDecimal(0);
      lineFC.credit = new BigDecimal(0);

      BigDecimal total = inv.getGrandTotalAmount().abs();

      if (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOAPCREDITMEMO")) {

        total = total.negate();

      }

      BigDecimal saldo = total.subtract(inv.getOutstandingAmount());

      if (saldo.compareTo(BigDecimal.ZERO) > 0 && total.compareTo(BigDecimal.ZERO) < 0) {
        total = total.subtract(saldo);
        saldo = BigDecimal.ZERO;
      } else if (saldo.compareTo(BigDecimal.ZERO) < 0 && total.compareTo(BigDecimal.ZERO) > 0) {
        total = total.subtract(saldo);
        saldo = BigDecimal.ZERO;
      }

      if (total.compareTo(BigDecimal.ZERO) == 1) {
        lineFC.credit = total;
        lineFC.debit = saldo;
      } else {
        lineFC.credit = saldo.negate();
        lineFC.debit = total.negate();
      }

      lineFC.balance = lineFC.debit.subtract(lineFC.credit);
      lineFC.estaFacturado = "SI";

      lineFC.daysTillDue = null;
      if (!inv.isPaymentComplete()) {
        lineFC.daysTillDue = inv.getDaysTillDue();
      }

      lineFC.groupDocument = 1;
      BusinessPartner bp = inv.getBusinessPartner();
      String taxid = bp.getTaxID();

      String creditSol = "0.00";
      String creditDol = "PEN"; // ahora es tipo de moneda de credito
      if (bp.getSsaCreditcurrency() != null)
        creditDol = bp.getSsaCreditcurrency().getISOCode();

      creditSol = bp.getCreditLimit().toString();
      lineFC.creditSol = creditSol;
      lineFC.creditDol = creditDol;
      lineFC.ultFactura = ultFactura;
      lineFC.ultPago = ultPago;

      if (counter % 50 == 0) {
        OBDal.getInstance().getSession().clear();
      }
      counter++;

      if (onlyPendings && lineFC.balance.compareTo(BigDecimal.ZERO) == 0)
        continue;

      UnpaidToVendorLineWithPartner unpaidbyclientliWithPartner = map.get(taxid);
      if (unpaidbyclientliWithPartner != null) {
        unpaidbyclientliWithPartner.unpaidtovendors.add(lineFC);
      } else {
        unpaidbyclientliWithPartner = new UnpaidToVendorLineWithPartner();
        unpaidbyclientliWithPartner.unpaidtovendors.add(lineFC);
        unpaidbyclientliWithPartner.cBpartnerId = bp.getId();
        map.put(taxid, unpaidbyclientliWithPartner);
      }

      currentGroup++;
    }

    for (Map.Entry<String, UnpaidToVendorLineWithPartner> entry : map.entrySet()) {
      List<UnpaidToVendorLine> lsLines = entry.getValue().unpaidtovendors;

      if (lsLines.size() == 0)
        continue;

      UnpaidToVendorLine lineHeader = new UnpaidToVendorLine();
      lineHeader.groupNumber = -1;
      lineHeader.orgName = "--";
      lineHeader.specialdoctype = "";
      lineHeader.specialdoctypeplus = "";
      lineHeader.invoicedDate = "";
      lineHeader.documentId = "";
      lineHeader.dueDate = "";
      lineHeader.currency = "";
      lineHeader.docType = "";
      lineHeader.paymentCondition = "";
      lineHeader.documentNumber = "";
      lineHeader.percepcion = new BigDecimal(0);
      lineHeader.debit = new BigDecimal(0);
      lineHeader.credit = new BigDecimal(0);
      lineHeader.balance = new BigDecimal(0);
      Collections.sort(lsLines);
      lsLines.add(lineHeader);

    }
    return map;

  }

  public static List<UnpaidToVendorLine> getUnpaidToVendor(ConnectionProvider conProvider,
      String strBpId, Date startingDate, Date endingDate, boolean onlyPendings, String strOrgId)
      throws ServletException {

    Date dttToday = new Date();
    List<UnpaidToVendorLine> lsLines = new ArrayList<UnpaidToVendorLine>();

    BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class, strBpId);

    if (bp == null)
      return lsLines;

    Set<String> orgList = OBContext.getOBContext().getOrganizationStructureProvider()
        .getNaturalTree(strOrgId);

    // INVOICES
    OBCriteria<Invoice> accounts = OBDal.getInstance().createCriteria(Invoice.class);
    accounts.createAlias(Invoice.PROPERTY_BUSINESSPARTNER, "bp");

    accounts.add(Restrictions.eq("bp." + BusinessPartner.PROPERTY_TAXID, bp.getTaxID()));
    accounts.add(Restrictions.eq(Invoice.PROPERTY_SALESTRANSACTION, false));
    accounts.add(Restrictions.eq(Invoice.PROPERTY_DOCUMENTSTATUS, "CO"));
    accounts.add(Restrictions.between(Invoice.PROPERTY_ACCOUNTINGDATE, startingDate,
        getEndOfDay(endingDate)));
    accounts.add(Restrictions.eq(Invoice.PROPERTY_ACTIVE, true));
    accounts.add(Restrictions.in(Invoice.PROPERTY_ORGANIZATION + ".id", orgList));

    /*
     * if (onlyPendings) accounts.add(Restrictions.ne(Invoice.PROPERTY_OUTSTANDINGAMOUNT,
     * BigDecimal.ZERO));
     */
    accounts.addOrderBy(Invoice.PROPERTY_ACCOUNTINGDATE, true);
    accounts.addOrderBy(Invoice.PROPERTY_CREATIONDATE, true);

    accounts.setFilterOnReadableClients(false);
    accounts.setFilterOnReadableOrganization(false);

    OBCriteria<Invoice> oldAccounts = OBDal.getInstance().createCriteria(Invoice.class);
    oldAccounts.createAlias(Invoice.PROPERTY_BUSINESSPARTNER, "bp");

    oldAccounts.add(Restrictions.eq("bp." + BusinessPartner.PROPERTY_TAXID, bp.getTaxID()));
    oldAccounts.add(Restrictions.eq(Invoice.PROPERTY_SALESTRANSACTION, false));
    oldAccounts.add(Restrictions.eq(Invoice.PROPERTY_DOCUMENTSTATUS, "CO"));
    oldAccounts.add(Restrictions.lt(Invoice.PROPERTY_ACCOUNTINGDATE, startingDate));
    oldAccounts.add(Restrictions.eq(Invoice.PROPERTY_ACTIVE, true));
    oldAccounts.add(Restrictions.in(Invoice.PROPERTY_ORGANIZATION + ".id", orgList));

    /*
     * if (onlyPendings) oldAccounts.add(Restrictions.ne(Invoice.PROPERTY_OUTSTANDINGAMOUNT,
     * BigDecimal.ZERO));
     */
    oldAccounts
        .add(Restrictions.or(Restrictions.ge(Invoice.PROPERTY_FINALSETTLEMENTDATE, startingDate),
            Restrictions.isNull(Invoice.PROPERTY_FINALSETTLEMENTDATE)));
    oldAccounts.addOrderBy(Invoice.PROPERTY_ACCOUNTINGDATE, true);
    oldAccounts.addOrderBy(Invoice.PROPERTY_CREATIONDATE, true);
    oldAccounts.setFilterOnReadableClients(false);
    oldAccounts.setFilterOnReadableOrganization(false);

    List<Invoice> accountList = oldAccounts.list();

    accountList.addAll(accounts.list());

    int currentGroup = 1;

    String ultFactura = "Ninguna";
    String ultPago = "Ninguno";
    String creditSol = "0.00";
    String creditDol = "PEN"; // ahora es tipo de moneda de credito

    if (bp.getSsaCreditcurrency() != null)
      creditDol = bp.getSsaCreditcurrency().getISOCode();

    creditSol = bp.getCreditLimit().toString();

    OBCriteria<Invoice> ultimaFactura = OBDal.getInstance().createCriteria(Invoice.class);

    ultimaFactura.createAlias(Invoice.PROPERTY_BUSINESSPARTNER, "bp");
    ultimaFactura.add(Restrictions.eq("bp." + BusinessPartner.PROPERTY_TAXID, bp.getTaxID()));
    ultimaFactura.add(Restrictions.eq(Invoice.PROPERTY_SALESTRANSACTION, false));
    ultimaFactura.add(Restrictions.eq(Invoice.PROPERTY_ACTIVE, true));
    ultimaFactura.add(Restrictions.eq(Invoice.PROPERTY_DOCUMENTSTATUS, "CO"));
    ultimaFactura.add(Restrictions.in(Invoice.PROPERTY_ORGANIZATION + ".id", orgList));

    ultimaFactura.addOrderBy(Invoice.PROPERTY_ACCOUNTINGDATE, true);
    ultimaFactura.addOrderBy(Invoice.PROPERTY_CREATIONDATE, true);
    ultimaFactura.setFilterOnReadableClients(false);
    ultimaFactura.setFilterOnReadableOrganization(false);

    List<Invoice> lsUltFactura = ultimaFactura.list();

    SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    for (int i = 0; i < lsUltFactura.size(); i++) {
      Invoice invUlt = lsUltFactura.get(i);
      if (invUlt.getTransactionDocument().getScoSpecialdoctype().equals("SCOAPINVOICE")
          || invUlt.getTransactionDocument().getScoSpecialdoctype()
              .equals("SCOAPSIMPLEPROVISIONINVOICE")
          || invUlt.getTransactionDocument().getScoSpecialdoctype().equals("SCOAPBOEINVOICE")
          || invUlt.getTransactionDocument().getScoSpecialdoctype().equals("SCOAPLOANINVOICE")) {
        ultFactura = DATE_FORMAT.format(invUlt.getInvoiceDate());
        break;
      }
    }

    OBCriteria<FIN_Payment> ultimoPago = OBDal.getInstance().createCriteria(FIN_Payment.class);
    ultimoPago.createAlias(FIN_Payment.PROPERTY_BUSINESSPARTNER, "bp");
    ultimoPago.add(Restrictions.eq("bp." + BusinessPartner.PROPERTY_TAXID, bp.getTaxID()));
    ultimoPago.add(Restrictions.eq(FIN_Payment.PROPERTY_RECEIPT, false));
    ultimoPago.add(Restrictions.eq(FIN_Payment.PROPERTY_ACTIVE, true));
    ultimoPago.add(Restrictions.in(FIN_Payment.PROPERTY_ORGANIZATION + ".id", orgList));

    ultimoPago.addOrderBy(FIN_Payment.PROPERTY_PAYMENTDATE, false);

    ultimoPago.setFilterOnReadableClients(false);
    ultimoPago.setFilterOnReadableOrganization(false);

    List<FIN_Payment> lsUltPago = ultimoPago.list();

    for (int i = 0; i < lsUltPago.size();) {
      FIN_Payment payUlt = lsUltPago.get(i);
      ultPago = DATE_FORMAT.format(payUlt.getPaymentDate());
      break;
    }

    for (int i = 0; i < accountList.size(); i++) {

      OBContext.setAdminMode(true);
      try {

        int actualGroup = currentGroup;

        Invoice inv = accountList.get(i);

        List<FIN_PaymentSchedule> schedules = inv.getFINPaymentScheduleList();

        Date dueDate = null;
        Date lastDueDate = null;
        for (int j = 0; j < schedules.size(); j++) {
          if (dueDate == null || schedules.get(j).getDueDate().after(dueDate))
            dueDate = schedules.get(j).getDueDate();

          if (lastDueDate == null || schedules.get(j).getDueDate().after(lastDueDate))
            lastDueDate = schedules.get(j).getDueDate();
        }
        if (dueDate == null)
          dueDate = lastDueDate;
        if (dueDate == null)
          dueDate = inv.getScoNewdateinvoiced();

        String tipoOp = "--";
        String documentType = "";
        String paymentMethod = "";
        if (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOAPINVOICE")) {
          documentType = "FC";
          if (inv.getScoPodocCmbiValue() != null
              && inv.getScoPodocCmbiValue().compareTo("tabla10_03") == 0)
            tipoOp = "Boleta";
          else if (inv.getScoPodocCmbiValue() != null
              && inv.getScoPodocCmbiValue().compareTo("tabla10_08") == 0)
            tipoOp = "Nota de Débito";
          else
            tipoOp = "Factura";
          paymentMethod = inv.getPaymentMethod().getName();
        } else if (inv.getTransactionDocument().getScoSpecialdoctype()
            .equals("SCOAPSIMPLEPROVISIONINVOICE")
            || inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOAPLOANINVOICE")) {
          documentType = "FC";
          tipoOp = "Factura";
        } else if (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOAPBOEINVOICE")) {
          documentType = "LC";
          tipoOp = "Letra";
        } else if (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOAPCREDITMEMO")) {
          documentType = "NA";
          tipoOp = "Nota de Crédito";
        }

        UnpaidToVendorLine lineFC = new UnpaidToVendorLine();
        lineFC.orgName = inv.getOrganization().getName();
        lineFC.specialdoctype = inv.getSCOSpecialDocType();
        lineFC.specialdoctypeplus = inv.getSsaSpecialdoctypeplus() != null
            ? inv.getSsaSpecialdoctypeplus()
            : inv.getSCOSpecialDocType();
        lineFC.groupNumber = actualGroup;
        lineFC.invoicedDate = OBDateUtils.formatDate(inv.getAccountingDate());
        lineFC.documentId = inv.getId();

        lineFC.dueDate = OBDateUtils.formatDate(dueDate);
        lineFC.dateVenc = dueDate;
        lineFC.currency = inv.getCurrency().getISOCode();
        lineFC.docType = documentType;
        lineFC.tipoOp = tipoOp;
        lineFC.paymentMethod = paymentMethod;
        lineFC.paymentCondition = "";
        if (documentType.equals("FC") || documentType.equals("LC") || documentType.equals("NA")) {
          lineFC.paymentCondition = inv.getPaymentTerms().getName();// inv.getPaymentTerms().getSearchKey();
          lineFC.docRecepcion = (inv.isScrIsreceived() ? "SI" : "NO");
          if (inv.isScrIsreceived()) {
            if (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOAPBOEINVOICE")) {
              if (inv.getSsaBoeDatereceipt() != null) {
                lineFC.recepcionDate = OBDateUtils.formatDate(inv.getSsaBoeDatereceipt());
              } else {
                lineFC.recepcionDate = OBDateUtils.formatDate(inv.getInvoiceDate());
              }
            } else {
              lineFC.recepcionDate = OBDateUtils.formatDate(inv.getInvoiceDate());
            }
          }

        }

        lineFC.documentNumber = inv.getScrPhysicalDocumentno();

        lineFC.percepcion = new BigDecimal(0);

        lineFC.debit = new BigDecimal(0);
        lineFC.credit = new BigDecimal(0);

        // BigDecimal saldo1 = getSaldoInvoice(inv, startingDate, true, true, true);
        BigDecimal saldo1 = getGeneralSaldoInvoice(conProvider, inv, startingDate);

        // BigDecimal saldo2 = getSaldoInvoice(inv, getEndOfDay(endingDate), true, true, true);
        BigDecimal saldo2 = getGeneralSaldoInvoice(conProvider, inv, getEndOfDay(endingDate));

        // BigDecimal saldo3 = getSaldoInvoiceWithGlItem(inv, startingDate);

        // BigDecimal saldo4 = getSaldoInvoiceWithGlItem(inv, getEndOfDay(endingDate));
        // inv,getEndOfDay(endingDate));

        // saldo1 = saldo1.subtract(saldo3);
        // saldo2 = saldo2.subtract(saldo4);

        BigDecimal saldo = saldo1.subtract(saldo2);

        if (saldo1.compareTo(BigDecimal.ZERO) == 1) {
          lineFC.credit = saldo1;
          lineFC.debit = saldo;
        } else {
          lineFC.credit = saldo.negate();
          lineFC.debit = saldo1.negate();
        }

        if (lineFC.credit.compareTo(BigDecimal.ZERO) < 0) {
          BigDecimal tmp = lineFC.credit;
          lineFC.credit = lineFC.debit.negate();
          lineFC.debit = tmp.negate();
        }

        lineFC.balance = lineFC.debit.subtract(lineFC.credit);
        lineFC.estaFacturado = "SI";

        lineFC.daysTillDue = null;
        if (!inv.isPaymentComplete()) {
          lineFC.daysTillDue = inv.getDaysTillDue();
        }

        lineFC.groupDocument = 1;

        if (onlyPendings && lineFC.balance.compareTo(BigDecimal.ZERO) == 0)
          continue;

        lsLines.add(lineFC);

        currentGroup++;

      } finally {
        OBContext.restorePreviousMode();
      }
    }

    // PREPAYMENTS
    OBCriteria<SCOPrepayment> prepayAccounts = OBDal.getInstance()
        .createCriteria(SCOPrepayment.class);

    prepayAccounts.createAlias(SCOPrepayment.PROPERTY_BUSINESSPARTNER, "bp");

    prepayAccounts.add(Restrictions.eq("bp." + BusinessPartner.PROPERTY_TAXID, bp.getTaxID()));
    prepayAccounts
        .add(Restrictions.le(SCOPrepayment.PROPERTY_GENERATEDDATE, getEndOfDay(endingDate)));
    prepayAccounts.add(Restrictions.eq(SCOPrepayment.PROPERTY_ACTIVE, true));
    prepayAccounts.add(Restrictions.ne(SCOPrepayment.PROPERTY_DOCUMENTSTATUS, "DR"));
    prepayAccounts.add(Restrictions.in(SCOPrepayment.PROPERTY_ORGANIZATION + ".id", orgList));

    prepayAccounts.addOrderBy(SCOPrepayment.PROPERTY_GENERATEDDATE, true);

    prepayAccounts.setFilterOnReadableClients(false);
    prepayAccounts.setFilterOnReadableOrganization(false);

    List<SCOPrepayment> prepayAccountList = prepayAccounts.list();

    for (int i = 0; i < prepayAccountList.size(); i++) {
      OBContext.setAdminMode(true);
      try {

        SCOPrepayment prepay = prepayAccountList.get(i);
        if (!prepay.getTransactionDocument().getScoSpecialdoctype().equals("SCOPREPAYMENT"))
          continue;

        UnpaidToVendorLine linePrepay = new UnpaidToVendorLine();
        linePrepay.groupNumber = currentGroup;
        linePrepay.orgName = prepay.getOrganization().getName();
        linePrepay.specialdoctype = prepay.getTransactionDocument().getScoSpecialdoctype();
        linePrepay.specialdoctypeplus = prepay.getTransactionDocument().getScoSpecialdoctype();
        linePrepay.invoicedDate = OBDateUtils.formatDate(prepay.getGeneratedDate());
        linePrepay.documentId = prepay.getId();
        linePrepay.dueDate = OBDateUtils.formatDate(prepay.getGeneratedDate());
        linePrepay.dateVenc = prepay.getGeneratedDate();
        linePrepay.currency = prepay.getCurrency().getISOCode();
        linePrepay.docType = "FC";
        linePrepay.tipoOp = "Anticipo";
        linePrepay.documentNumber = prepay.getDocumentNo();
        linePrepay.percepcion = new BigDecimal(0);

        linePrepay.paymentMethod = "";
        linePrepay.paymentCondition = "";

        linePrepay.debit = new BigDecimal(0);
        linePrepay.credit = new BigDecimal(0);

        BigDecimal saldo1 = getGeneralSaldoPrepayment(conProvider, prepay, startingDate);
        BigDecimal saldo2 = getGeneralSaldoPrepayment(conProvider, prepay, getEndOfDay(endingDate));
        System.out.println("saldo1:" + saldo1);
        System.out.println("saldo2:" + saldo2);
        BigDecimal saldo = saldo1.subtract(saldo2);

        if (saldo1.compareTo(BigDecimal.ZERO) == 1) {
          linePrepay.credit = saldo1;
          linePrepay.debit = saldo;
        } else {
          linePrepay.credit = saldo.negate();
          linePrepay.debit = saldo1.negate();
        }

        if (linePrepay.credit.compareTo(BigDecimal.ZERO) < 0) {
          BigDecimal tmp = linePrepay.credit;
          linePrepay.credit = linePrepay.debit.negate();
          linePrepay.debit = tmp.negate();
        }

        linePrepay.balance = linePrepay.debit.subtract(linePrepay.credit);
        linePrepay.estaFacturado = "SI";

        linePrepay.daysTillDue = null;

        linePrepay.groupDocument = 1;

        if (onlyPendings && linePrepay.balance.compareTo(BigDecimal.ZERO) == 0)
          continue;

        lsLines.add(linePrepay);

        currentGroup++;
      } finally {
        OBContext.restorePreviousMode();
      }
    }

    // ENTREGAS A RENDIR
    OBCriteria<ScoRendicioncuentas> rendAccounts = OBDal.getInstance()
        .createCriteria(ScoRendicioncuentas.class);

    rendAccounts.createAlias(ScoRendicioncuentas.PROPERTY_BUSINESSPARTNER, "bp");

    rendAccounts.add(Restrictions.eq("bp." + BusinessPartner.PROPERTY_TAXID, bp.getTaxID()));
    rendAccounts
        .add(Restrictions.le(ScoRendicioncuentas.PROPERTY_DATEGEN, getEndOfDay(endingDate)));
    rendAccounts.add(Restrictions.eq(ScoRendicioncuentas.PROPERTY_ACTIVE, true));
    rendAccounts.add(Restrictions.ne(ScoRendicioncuentas.PROPERTY_DOCUMENTSTATUS, "DR"));
    rendAccounts.add(Restrictions.in(ScoRendicioncuentas.PROPERTY_ORGANIZATION + ".id", orgList));

    rendAccounts.addOrderBy(ScoRendicioncuentas.PROPERTY_DATEGEN, true);

    rendAccounts.setFilterOnReadableClients(false);
    rendAccounts.setFilterOnReadableOrganization(false);

    List<ScoRendicioncuentas> rendAccountList = rendAccounts.list();

    for (int i = 0; i < rendAccountList.size(); i++) {
      OBContext.setAdminMode(true);
      try {

        ScoRendicioncuentas rendCuentas = rendAccountList.get(i);
        if (!rendCuentas.getTransactionDocument().getScoSpecialdoctype()
            .equals("SCOACCOUNTABILITY"))
          continue;

        UnpaidToVendorLine lineRendCuentas = new UnpaidToVendorLine();
        lineRendCuentas.groupNumber = currentGroup;
        lineRendCuentas.orgName = rendCuentas.getOrganization().getName();
        lineRendCuentas.specialdoctype = rendCuentas.getTransactionDocument()
            .getScoSpecialdoctype();
        lineRendCuentas.specialdoctypeplus = rendCuentas.getTransactionDocument()
            .getScoSpecialdoctype();
        lineRendCuentas.invoicedDate = OBDateUtils.formatDate(rendCuentas.getDategen());
        lineRendCuentas.documentId = rendCuentas.getId();
        lineRendCuentas.dueDate = OBDateUtils.formatDate(rendCuentas.getDategen());
        lineRendCuentas.dateVenc = rendCuentas.getDategen();
        lineRendCuentas.currency = rendCuentas.getCurrency().getISOCode();
        lineRendCuentas.docType = "FC";
        lineRendCuentas.tipoOp = "A Rendir";
        lineRendCuentas.documentNumber = rendCuentas.getDocumentNo();
        lineRendCuentas.percepcion = new BigDecimal(0);

        lineRendCuentas.paymentMethod = "";
        lineRendCuentas.paymentCondition = "";

        lineRendCuentas.debit = new BigDecimal(0);
        lineRendCuentas.credit = new BigDecimal(0);

        BigDecimal saldo1 = getGeneralSaldoRendCuentas(conProvider, rendCuentas, startingDate);
        BigDecimal saldo2 = getGeneralSaldoRendCuentas(conProvider, rendCuentas,
            getEndOfDay(endingDate));
        System.out.println("saldo1:" + saldo1);
        System.out.println("saldo2:" + saldo2);
        BigDecimal saldo = saldo1.subtract(saldo2);

        if (saldo1.compareTo(BigDecimal.ZERO) == 1) {
          lineRendCuentas.credit = saldo1;
          lineRendCuentas.debit = saldo;
        } else {
          lineRendCuentas.credit = saldo.negate();
          lineRendCuentas.debit = saldo1.negate();
        }

        if (lineRendCuentas.credit.compareTo(BigDecimal.ZERO) < 0) {
          BigDecimal tmp = lineRendCuentas.credit;
          lineRendCuentas.credit = lineRendCuentas.debit.negate();
          lineRendCuentas.debit = tmp.negate();
        }

        lineRendCuentas.balance = lineRendCuentas.debit.subtract(lineRendCuentas.credit);

        lineRendCuentas.estaFacturado = "SI";

        lineRendCuentas.daysTillDue = null;

        lineRendCuentas.groupDocument = 1;

        if (onlyPendings && lineRendCuentas.balance.compareTo(BigDecimal.ZERO) == 0)
          continue;

        lsLines.add(lineRendCuentas);

        currentGroup++;
      } finally {
        OBContext.restorePreviousMode();
      }
    }

    // System.out.println("Unpaid Success");

    UnpaidToVendorLine lineHeader = new UnpaidToVendorLine();
    lineHeader.groupNumber = -1;
    lineHeader.orgName = "--";
    lineHeader.specialdoctype = "";
    lineHeader.specialdoctypeplus = "";
    lineHeader.invoicedDate = "";
    lineHeader.documentId = "";
    lineHeader.dueDate = "";
    lineHeader.currency = "";
    lineHeader.docType = "";
    lineHeader.paymentCondition = "";
    lineHeader.documentNumber = "";
    lineHeader.percepcion = new BigDecimal(0);
    lineHeader.debit = new BigDecimal(0);
    lineHeader.credit = new BigDecimal(0);
    lineHeader.balance = new BigDecimal(0);

    Collections.sort(lsLines);
    lsLines.add(lineHeader);

    for (int i = 0; i < lsLines.size(); i++) {
      lsLines.get(i).ultFactura = ultFactura;
      lsLines.get(i).ultPago = ultPago;
      lsLines.get(i).creditSol = creditSol;
      lsLines.get(i).creditDol = creditDol;
    }

    // ordenar por numero de grupo
    /*
     * List<UnpaidToVendorLine> lsLinesOrdered = new ArrayList<UnpaidToVendorLine>();
     * 
     * if (lsLines.size() > 0) { while (lsLines.size() > 0) { int groupNumber =
     * lsLines.get(0).groupNumber; int line = 0; for (int j = 1; j < lsLines.size(); j++) { if
     * (lsLines.get(j).groupNumber < groupNumber) { groupNumber = lsLines.get(j).groupNumber; line =
     * j; } }
     * 
     * lsLinesOrdered.add(lsLines.get(line)); lsLines.remove(line); } }
     */

    return lsLines;

  }

  public static List<UnpaidToVendorLine> getDetailUnpaidToVendor(String documentId,
      Date startingDate, Date endingDate) {

    Date endingDateEnd = getEndOfDay(endingDate);
    List<UnpaidToVendorLine> lsLines = new ArrayList<UnpaidToVendorLine>();

    boolean found = false;
    int currentGroup = 1;

    // INVOICE DETAILS
    OBCriteria<Invoice> accounts = OBDal.getInstance().createCriteria(Invoice.class);
    accounts.add(Restrictions.eq(Invoice.PROPERTY_ID, documentId));

    accounts.setFilterOnReadableClients(false);
    accounts.setFilterOnReadableOrganization(false);

    List<Invoice> accountList = accounts.list();

    if (accountList.size() > 0) {
      found = true;
      OBContext.setAdminMode(true);
      try {

        int actualGroup = currentGroup;

        Invoice inv = accountList.get(0);

        List<FIN_PaymentSchedule> schedules = inv.getFINPaymentScheduleList();

        Date dueDate = null;
        Date lastDueDate = null;
        for (int j = 0; j < schedules.size(); j++) {
          if (dueDate == null || schedules.get(j).getDueDate().after(dueDate))
            dueDate = schedules.get(j).getDueDate();

          if (lastDueDate == null || schedules.get(j).getDueDate().after(lastDueDate))
            lastDueDate = schedules.get(j).getDueDate();
        }
        if (dueDate == null)
          dueDate = lastDueDate;
        if (dueDate == null)
          dueDate = inv.getScoNewdateinvoiced();

        String tipoOp = "--";
        String documentType = "";
        String paymentMethod = "";
        if (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOAPINVOICE")) {
          documentType = "FC";
          if (inv.getScoPodocCmbiValue() != null
              && inv.getScoPodocCmbiValue().compareTo("tabla10_03") == 0)
            tipoOp = "Boleta";
          else if (inv.getScoPodocCmbiValue() != null
              && inv.getScoPodocCmbiValue().compareTo("tabla10_08") == 0)
            tipoOp = "Nota de Débito";
          else
            tipoOp = "Factura";
          paymentMethod = inv.getPaymentMethod().getName();
        } else if (inv.getTransactionDocument().getScoSpecialdoctype()
            .equals("SCOAPSIMPLEPROVISIONINVOICE")
            || inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOAPLOANINVOICE")) {
          documentType = "FC";
          tipoOp = "Factura";
        } else if (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOAPBOEINVOICE")) {
          documentType = "LC";
          tipoOp = "Letra";
        } else if (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOAPCREDITMEMO")) {
          documentType = "NA";
          tipoOp = "Nota de Crédito";
        }

        UnpaidToVendorLine lineFC = new UnpaidToVendorLine();
        lineFC.specialdoctype = inv.getSCOSpecialDocType();
        lineFC.specialdoctypeplus = inv.getSsaSpecialdoctypeplus() != null
            ? inv.getSsaSpecialdoctypeplus()
            : inv.getSCOSpecialDocType();
        lineFC.groupNumber = actualGroup;
        lineFC.invoicedDate = OBDateUtils.formatDate(inv.getAccountingDate());
        lineFC.documentId = inv.getId();

        lineFC.dueDate = OBDateUtils.formatDate(dueDate);
        lineFC.currency = inv.getCurrency().getISOCode();
        lineFC.docType = documentType;
        lineFC.tipoOp = tipoOp;
        lineFC.paymentMethod = paymentMethod;
        lineFC.paymentCondition = "";
        if (documentType.equals("FC") || documentType.equals("LC") || documentType.equals("NA")) {
          lineFC.paymentCondition = inv.getPaymentTerms().getName();// inv.getPaymentTerms().getSearchKey();
          lineFC.docRecepcion = (inv.isScrIsreceived() ? "SI" : "NO");
          if (inv.isScrIsreceived()) {
            if (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOAPBOEINVOICE")) {
              if (inv.getSsaBoeDatereceipt() != null) {
                lineFC.recepcionDate = OBDateUtils.formatDate(inv.getSsaBoeDatereceipt());
              } else {
                lineFC.recepcionDate = OBDateUtils.formatDate(inv.getInvoiceDate());
              }
            } else {
              lineFC.recepcionDate = OBDateUtils.formatDate(inv.getInvoiceDate());
            }
          }

        }

        lineFC.documentNumber = inv.getScrPhysicalDocumentno();

        lineFC.percepcion = new BigDecimal(0);

        lineFC.debit = new BigDecimal(0);
        lineFC.credit = new BigDecimal(0);

        BigDecimal saldo = getSaldoInvoice(inv, startingDate, true);

        if (saldo.compareTo(BigDecimal.ZERO) == 1) {
          if (!inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOAPCREDITMEMO")) {
            lineFC.credit = saldo;
          } else {
            lineFC.debit = saldo;
          }
        } else {
          if (!inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOAPCREDITMEMO")) {
            lineFC.debit = saldo;
          } else {
            lineFC.credit = saldo;
          }
        }

        if (lineFC.credit.compareTo(BigDecimal.ZERO) < 0) {
          BigDecimal tmp = lineFC.credit;
          lineFC.credit = lineFC.debit.negate();
          lineFC.debit = tmp.negate();
        }

        lineFC.balance = lineFC.debit.subtract(lineFC.credit);
        lineFC.estaFacturado = "SI";

        lineFC.daysTillDue = null;
        if (!inv.isPaymentComplete()) {
          lineFC.daysTillDue = inv.getDaysTillDue();
        }

        lineFC.groupDocument = 1;

        lsLines.add(lineFC);

        BigDecimal totalBalance = new BigDecimal(0.00).add(lineFC.balance);

        // Aplicaciones
        for (int j = 0; j < schedules.size(); j++) {

          List<FIN_PaymentScheduleDetail> lsPsd = schedules.get(j)
              .getFINPaymentScheduleDetailInvoicePaymentScheduleList();
          for (int k = 0; k < lsPsd.size(); k++) {

            if (lsPsd.get(k).isScoExternalpayment() || lsPsd.get(k).getPaymentDetails() != null) {

              FIN_PaymentScheduleDetail detail = lsPsd.get(k);
              // pagos especiales y canje a letra
              if (detail.isScoExternalpayment()) {
                if (detail.isScoIsboepayment()) {
                  List<SCOBoefromPayschedetail> boeFromPaysch = detail
                      .getSCOBoefromPayschedetailList();

                  List<SCOBoeFrom> boeFroms = new ArrayList<SCOBoeFrom>();

                  for (int jh = 0; jh < boeFromPaysch.size(); jh++)
                    if (boeFromPaysch.get(jh).getSCOBoeFrom().getInvoiceref().getId()
                        .equals(inv.getId()))
                      boeFroms.add(boeFromPaysch.get(jh).getSCOBoeFrom());

                  for (int ll = 0; ll < boeFroms.size(); ll++) {

                    SCOBillofexchange manageBOE = boeFroms.get(ll).getBillOfExchange();

                    // get percentage:
                    BigDecimal totalFromBoe = new BigDecimal(0);
                    for (int lll = 0; lll < manageBOE.getSCOBoeFromList().size(); lll++) {
                      totalFromBoe = totalFromBoe
                          .add(manageBOE.getSCOBoeFromList().get(lll).getAmount());
                    }
                    BigDecimal percentage = boeFroms.get(ll).getAmount().divide(totalFromBoe, 16,
                        RoundingMode.HALF_UP);

                    String op = "";
                    String specialDocType = manageBOE.getTransactionDocument()
                        .getScoSpecialdoctype();
                    if (specialDocType.equals("SCOAPINVOICEXBOE")) {
                      op = "Canje Letra";
                    }
                    for (int lll = 0; lll < manageBOE.getSCOBoeToList().size(); lll++) {

                      SCOBoeTo boeTo = manageBOE.getSCOBoeToList().get(lll);

                      if (boeTo.getInvoice().getAccountingDate().compareTo(startingDate) < 0
                          || boeTo.getInvoice().getAccountingDate().compareTo(endingDateEnd) > 0)
                        continue;

                      UnpaidToVendorLine lineLA = new UnpaidToVendorLine();
                      lineLA.groupNumber = actualGroup;
                      lineLA.invoicedDate = OBDateUtils
                          .formatDate(boeTo.getInvoice().getAccountingDate());
                      if (boeTo.getInvoice().getFINPaymentScheduleList().size() > 0)
                        lineLA.dueDate = OBDateUtils.formatDate(
                            boeTo.getInvoice().getFINPaymentScheduleList().get(0).getDueDate());
                      else
                        lineLA.dueDate = OBDateUtils.formatDate(manageBOE.getGeneratedDate());
                      lineLA.currency = boeTo.getInvoice().getCurrency().getISOCode();
                      lineLA.docType = "LA"; // letra
                      lineLA.tipoOp = op;
                      lineLA.paymentCondition = boeTo.getInvoice().getPaymentTerms().getName(); // boeTo.getInvoice().getPaymentTerms().getSearchKey();
                      lineLA.documentNumber = boeTo.getInvoice().getScrPhysicalDocumentno();
                      lineLA.percepcion = new BigDecimal(0);
                      lineLA.specialdoctype = boeTo.getInvoice().getSCOSpecialDocType();
                      lineLA.specialdoctypeplus = boeTo.getInvoice()
                          .getSsaSpecialdoctypeplus() != null
                              ? boeTo.getInvoice().getSsaSpecialdoctypeplus()
                              : boeTo.getInvoice().getSCOSpecialDocType();
                      lineLA.documentId = boeTo.getInvoice().getId();

                      lineLA.debit = new BigDecimal(0);
                      lineLA.credit = new BigDecimal(0);
                      BigDecimal totalGrand = boeTo.getInvoice().getGrandTotalAmount()
                          .multiply(percentage).setScale(2, RoundingMode.HALF_UP);

                      if (totalGrand.compareTo(BigDecimal.ZERO) == 1) {
                        lineLA.debit = totalGrand;
                      } else {
                        lineLA.credit = totalGrand.negate();
                      }

                      lineLA.balance = lineLA.debit.subtract(lineLA.credit);
                      totalBalance = totalBalance.add(lineLA.balance);
                      lineLA.balance = new BigDecimal(totalBalance.toString());

                      lineLA.paymentMethod = "";
                      lineLA.daysTillDue = null;

                      lsLines.add(lineLA);
                    }
                  }
                } else {
                  // AI
                  UnpaidToVendorLine lineAI = new UnpaidToVendorLine();
                  lineAI.groupNumber = actualGroup;

                  Date dateAcct = null;
                  String specialdoctype = "", docId = "";
                  if (detail.isScoIswithholdingpayment()) {
                    dateAcct = detail.getSCOPwlrecliPayschedetailList().get(0)
                        .getSCOPwithhoRecLine().getSCOPwithholdingReceipt().getAccountingDate();

                    specialdoctype = detail.getSCOPwlrecliPayschedetailList().get(0)
                        .getSCOPwithhoRecLine().getSCOPwithholdingReceipt().getDocumentType()
                        .getScoSpecialdoctype();
                    docId = detail.getSCOPwlrecliPayschedetailList().get(0).getSCOPwithhoRecLine()
                        .getSCOPwithholdingReceipt().getId();

                  }

                  if (dateAcct.compareTo(startingDate) < 0 || dateAcct.compareTo(endingDateEnd) > 0)
                    continue;

                  lineAI.invoicedDate = OBDateUtils.formatDate(dateAcct);
                  lineAI.dueDate = OBDateUtils.formatDate(schedules.get(j).getDueDate());
                  lineAI.currency = inv.getCurrency().getISOCode();
                  lineAI.docType = "AI";
                  lineAI.paymentCondition = "";
                  if (detail.isScoIswithholdingpayment()) {
                    lineAI.docType = "PR";
                    lineAI.documentNumber = "Retencion";
                  } else if (detail.isScoIsrendcuentapayment()) {
                    lineAI.documentNumber = "A Rendir";
                  } else if (detail.isScoIscompensationpayment()) {
                    lineAI.documentNumber = "Compensacion";
                  }

                  lineAI.tipoOp = lineAI.documentNumber;

                  lineAI.specialdoctype = specialdoctype;
                  lineAI.specialdoctypeplus = specialdoctype;
                  lineAI.documentId = docId;

                  lineAI.percepcion = new BigDecimal(0);

                  lineAI.debit = new BigDecimal(0);
                  lineAI.credit = new BigDecimal(0);

                  if (detail.getAmount().compareTo(BigDecimal.ZERO) == 1) {
                    lineAI.debit = detail.getAmount();
                  } else {
                    lineAI.credit = detail.getAmount().negate();
                  }

                  lineAI.balance = lineAI.debit.subtract(lineAI.credit);
                  totalBalance = totalBalance.add(lineAI.balance);
                  lineAI.balance = new BigDecimal(totalBalance.toString());

                  lineAI.paymentMethod = "";
                  lineAI.daysTillDue = null;

                  lsLines.add(lineAI);
                }
              } else {
                FIN_PaymentDetail paymentDetail = detail.getPaymentDetails();

                if (detail.getPaymentDetails().getFinPayment().getPaymentDate()
                    .compareTo(startingDate) < 0
                    || detail.getPaymentDetails().getFinPayment().getPaymentDate()
                        .compareTo(endingDateEnd) > 0)
                  continue;

                BigDecimal remainingFromPayment = new BigDecimal(
                    paymentDetail.getAmount().toString());
                BigDecimal writeOffPayment = new BigDecimal(
                    paymentDetail.getWriteoffAmount().toString());

                BigDecimal percepcion = new BigDecimal(0);

                // RC or CI LINE
                if (remainingFromPayment.compareTo(BigDecimal.ZERO) != 0) {
                  UnpaidToVendorLine lineRC = new UnpaidToVendorLine();
                  lineRC.groupNumber = actualGroup;
                  lineRC.invoicedDate = OBDateUtils
                      .formatDate(detail.getPaymentDetails().getFinPayment().getPaymentDate());
                  lineRC.dueDate = OBDateUtils.formatDate(schedules.get(j).getDueDate());
                  lineRC.currency = inv.getCurrency().getISOCode();
                  lineRC.docType = "RC"; // pagos
                  lineRC.paymentCondition = "";
                  lineRC.documentNumber = detail.getPaymentDetails().getFinPayment()
                      .getDocumentNo();
                  lineRC.percepcion = percepcion;
                  lineRC.bankAccount = detail.getPaymentDetails().getFinPayment().getAccount()
                      .getName();
                  lineRC.debit = new BigDecimal(0);
                  lineRC.credit = new BigDecimal(0);

                  lineRC.specialdoctype = detail.getPaymentDetails().getFinPayment()
                      .getSCOSpecialDocType();
                  lineRC.specialdoctypeplus = detail.getPaymentDetails().getFinPayment()
                      .getScoSpecialdoctypeplus() != null
                          ? detail.getPaymentDetails().getFinPayment().getScoSpecialdoctypeplus()
                          : detail.getPaymentDetails().getFinPayment().getSCOSpecialDocType();
                  lineRC.documentId = detail.getPaymentDetails().getFinPayment().getId();

                  if (remainingFromPayment.compareTo(BigDecimal.ZERO) == 1) {
                    lineRC.debit = remainingFromPayment;
                  } else {
                    lineRC.credit = remainingFromPayment.negate();
                  }

                  lineRC.balance = lineRC.debit.subtract(lineRC.credit);

                  if (lineRC.balance.compareTo(BigDecimal.ZERO) == -1)// CI
                    lineRC.docType = "CI"; // pago

                  lineRC.tipoOp = "Pago";

                  if (detail.getPaymentDetails().getFinPayment().isScoIsapppayment()) {
                    lineRC.tipoOp = "Aplicación";
                  } else if (detail.getPaymentDetails().getFinPayment().isSCOIsSimpleProvision()) {
                    lineRC.tipoOp = "Cancelación Doc. Compra";
                  }

                  lineRC.paymentMethod = paymentDetail.getFinPayment().getPaymentMethod().getName();

                  totalBalance = totalBalance.add(lineRC.balance);
                  lineRC.balance = new BigDecimal(totalBalance.toString());

                  lineRC.paymentMethod = "";
                  lineRC.daysTillDue = null;

                  lsLines.add(lineRC);

                } else {
                  // last line with percepcion
                  lsLines.get(lsLines.size() - 1).percepcion = percepcion;
                }

                // AJUSTE or CI LINE
                if (writeOffPayment.compareTo(BigDecimal.ZERO) != 0) {
                  UnpaidToVendorLine lineRC = new UnpaidToVendorLine();
                  lineRC.groupNumber = actualGroup;
                  lineRC.invoicedDate = OBDateUtils
                      .formatDate(detail.getPaymentDetails().getFinPayment().getPaymentDate());
                  lineRC.dueDate = OBDateUtils.formatDate(schedules.get(j).getDueDate());
                  lineRC.currency = inv.getCurrency().getISOCode();
                  lineRC.docType = "RC"; // ajuste
                  lineRC.paymentCondition = "";
                  lineRC.documentNumber = detail.getPaymentDetails().getFinPayment()
                      .getDocumentNo();
                  lineRC.percepcion = BigDecimal.ZERO;
                  lineRC.bankAccount = "--";
                  lineRC.debit = new BigDecimal(0);
                  lineRC.credit = new BigDecimal(0);

                  lineRC.specialdoctype = detail.getPaymentDetails().getFinPayment()
                      .getSCOSpecialDocType();
                  lineRC.specialdoctypeplus = detail.getPaymentDetails().getFinPayment()
                      .getScoSpecialdoctypeplus() != null
                          ? detail.getPaymentDetails().getFinPayment().getScoSpecialdoctypeplus()
                          : detail.getPaymentDetails().getFinPayment().getSCOSpecialDocType();
                  lineRC.documentId = detail.getPaymentDetails().getFinPayment().getId();

                  if (writeOffPayment.compareTo(BigDecimal.ZERO) == 1) {
                    lineRC.debit = writeOffPayment;
                  } else {
                    lineRC.credit = writeOffPayment.negate();
                  }

                  lineRC.balance = lineRC.debit.subtract(lineRC.credit);

                  if (lineRC.balance.compareTo(BigDecimal.ZERO) == -1)// CI
                    lineRC.docType = "CI"; // pago

                  lineRC.tipoOp = "Ajuste";

                  totalBalance = totalBalance.add(lineRC.balance);
                  lineRC.balance = new BigDecimal(totalBalance.toString());

                  lineRC.paymentMethod = "";
                  lineRC.daysTillDue = null;

                  lsLines.add(lineRC);
                }

              }

            } else {
              ;
            } // do nothing

          }

        }

        OBCriteria<FIN_PaymentScheduleDetail> schedulesGL = OBDal.getInstance()
            .createCriteria(FIN_PaymentScheduleDetail.class);
        schedulesGL.add(Restrictions.eq(FIN_PaymentScheduleDetail.PROPERTY_SCOINVOICEGLREF, inv));
        List<FIN_PaymentScheduleDetail> scheduleList = schedulesGL.list();

        for (int j = 0; j < scheduleList.size(); j++) {
          FIN_PaymentScheduleDetail dd = scheduleList.get(j);
          if (dd.getPaymentDetails() == null)
            continue;
          if (dd.getPaymentDetails().getFinPayment() == null)
            continue;
          if (dd.getPaymentDetails().getGLItem() == null)
            continue;
          if (!dd.getPaymentDetails().getGLItem().getFinancialMgmtGLItemAccountsList().get(0)
              .getGlitemCreditAcct().getAccount().getSearchKey().startsWith("12"))
            continue;

          Date ddt = dd.getPaymentDetails().getFinPayment().getPaymentDate();

          if (ddt.compareTo(startingDate) < 0 || ddt.compareTo(endingDateEnd) > 0)
            continue;

          UnpaidToVendorLine lineRC = new UnpaidToVendorLine();
          lineRC.groupNumber = actualGroup;
          lineRC.invoicedDate = OBDateUtils
              .formatDate(dd.getPaymentDetails().getFinPayment().getPaymentDate());
          lineRC.dueDate = OBDateUtils
              .formatDate(dd.getPaymentDetails().getFinPayment().getPaymentDate());
          lineRC.currency = inv.getCurrency().getISOCode();
          lineRC.docType = "AJ"; // ajuste
          lineRC.paymentCondition = "";
          lineRC.documentNumber = dd.getPaymentDetails().getFinPayment().getDocumentNo();
          lineRC.percepcion = BigDecimal.ZERO;
          lineRC.bankAccount = "--";
          lineRC.debit = new BigDecimal(0);
          lineRC.credit = new BigDecimal(0);

          lineRC.specialdoctype = dd.getPaymentDetails().getFinPayment().getSCOSpecialDocType();
          lineRC.specialdoctypeplus = dd.getPaymentDetails().getFinPayment()
              .getScoSpecialdoctypeplus() != null
                  ? dd.getPaymentDetails().getFinPayment().getScoSpecialdoctypeplus()
                  : dd.getPaymentDetails().getFinPayment().getSCOSpecialDocType();
          lineRC.documentId = dd.getPaymentDetails().getFinPayment().getId();

          // COBROS
          if (dd.getPaymentDetails().getFinPayment().isReceipt()) {
            if (dd.getPaymentDetails().getAmount().compareTo(BigDecimal.ZERO) == 1) {
              lineRC.debit = dd.getPaymentDetails().getAmount();
            } else {
              lineRC.credit = dd.getPaymentDetails().getAmount().negate();
            }
            // PAGOS
          } else {
            if (dd.getPaymentDetails().getAmount().compareTo(BigDecimal.ZERO) == 1) {
              lineRC.credit = dd.getPaymentDetails().getAmount();
            } else {
              lineRC.debit = dd.getPaymentDetails().getAmount().negate();
            }
          }

          lineRC.balance = lineRC.debit.subtract(lineRC.credit);
          lineRC.tipoOp = "Ajuste";

          totalBalance = totalBalance.add(lineRC.balance);
          lineRC.balance = new BigDecimal(totalBalance.toString());

          lineRC.paymentMethod = "";
          lineRC.daysTillDue = null;

          lsLines.add(lineRC);
        }

        currentGroup++;

      } finally {
        OBContext.restorePreviousMode();
      }
    }

    if (!found) {

      // PREPAYMENT DETAILS
      OBCriteria<SCOPrepayment> prepayments = OBDal.getInstance()
          .createCriteria(SCOPrepayment.class);
      prepayments.add(Restrictions.eq(SCOPrepayment.PROPERTY_ID, documentId));

      prepayments.setFilterOnReadableClients(false);
      prepayments.setFilterOnReadableOrganization(false);

      List<SCOPrepayment> prepayList = prepayments.list();

      if (prepayList.size() > 0) {
        found = true;
        OBContext.setAdminMode(true);
        try {

          int actualGroup = currentGroup;

          SCOPrepayment prepay = prepayList.get(0);

          UnpaidToVendorLine linePrepay = new UnpaidToVendorLine();
          linePrepay.specialdoctype = prepay.getDocumentType().getScoSpecialdoctype();
          linePrepay.specialdoctypeplus = prepay.getDocumentType().getScoSpecialdoctype();
          linePrepay.groupNumber = actualGroup;
          linePrepay.invoicedDate = OBDateUtils.formatDate(prepay.getGeneratedDate());
          linePrepay.documentId = prepay.getId();

          linePrepay.dueDate = OBDateUtils.formatDate(prepay.getGeneratedDate());
          linePrepay.currency = prepay.getCurrency().getISOCode();
          linePrepay.docType = "FC";
          linePrepay.tipoOp = "Anticipo";
          linePrepay.documentNumber = prepay.getDocumentNo();
          linePrepay.percepcion = new BigDecimal(0);

          linePrepay.paymentMethod = "";
          linePrepay.paymentCondition = "";

          linePrepay.debit = new BigDecimal(0);
          linePrepay.credit = new BigDecimal(0);

          BigDecimal saldo = getSaldoPrepayment(prepay, startingDate);

          if (saldo.compareTo(BigDecimal.ZERO) == 1) {
            linePrepay.credit = saldo;
          } else {
            linePrepay.debit = saldo;
          }

          if (linePrepay.credit.compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal tmp = linePrepay.credit;
            linePrepay.credit = linePrepay.debit.negate();
            linePrepay.debit = tmp.negate();
          }

          linePrepay.balance = linePrepay.debit.subtract(linePrepay.credit);
          linePrepay.estaFacturado = "SI";

          linePrepay.daysTillDue = null;

          linePrepay.groupDocument = 1;

          lsLines.add(linePrepay);

          BigDecimal totalBalance = new BigDecimal(0.00).add(linePrepay.balance);

          OBCriteria<FIN_PaymentDetail> paymentDetails = OBDal.getInstance()
              .createCriteria(FIN_PaymentDetail.class);
          paymentDetails
              .add(Restrictions.eq(FIN_PaymentDetail.PROPERTY_SCOPAYOUTPREPAYMENT, prepay.getId()));
          List<FIN_PaymentDetail> paymentDetailList = paymentDetails.list();

          for (int j = 0; j < paymentDetailList.size(); j++) {
            FIN_PaymentDetail pd = paymentDetailList.get(j);
            if (pd.getFinPayment() == null)
              continue;
            if (pd.getFinPayment().getStatus().equals("RPAP")
                || pd.getFinPayment().getStatus().equals("RPAE")) {
              continue;
            }

            Date pdt = pd.getFinPayment().getPaymentDate();

            if (pdt.compareTo(startingDate) < 0 || pdt.compareTo(endingDateEnd) > 0)
              continue;

            UnpaidToVendorLine lineRC = new UnpaidToVendorLine();
            lineRC.groupNumber = actualGroup;
            lineRC.invoicedDate = OBDateUtils.formatDate(pd.getFinPayment().getPaymentDate());
            lineRC.dueDate = OBDateUtils.formatDate(pd.getFinPayment().getPaymentDate());
            lineRC.currency = prepay.getCurrency().getISOCode();
            lineRC.docType = "RC"; // pagos
            lineRC.paymentCondition = "";
            lineRC.documentNumber = pd.getFinPayment().getDocumentNo();
            lineRC.percepcion = BigDecimal.ZERO;
            lineRC.bankAccount = pd.getFinPayment().getAccount().getName();
            lineRC.debit = new BigDecimal(0);
            lineRC.credit = new BigDecimal(0);

            lineRC.specialdoctype = pd.getFinPayment().getSCOSpecialDocType();
            lineRC.specialdoctypeplus = pd.getFinPayment().getScoSpecialdoctypeplus() != null
                ? pd.getFinPayment().getScoSpecialdoctypeplus()
                : pd.getFinPayment().getSCOSpecialDocType();
            lineRC.documentId = pd.getFinPayment().getId();

            if (pd.getAmount().compareTo(BigDecimal.ZERO) == 1) {
              lineRC.debit = pd.getAmount();
            } else {
              lineRC.credit = pd.getAmount().negate();
            }

            lineRC.balance = lineRC.debit.subtract(lineRC.credit);

            if (lineRC.balance.compareTo(BigDecimal.ZERO) == -1)// CI
              lineRC.docType = "CI"; // pago

            lineRC.tipoOp = "Pago";

            totalBalance = totalBalance.add(lineRC.balance);
            lineRC.balance = new BigDecimal(totalBalance.toString());

            lineRC.paymentMethod = "";
            lineRC.daysTillDue = null;

            lsLines.add(lineRC);
          }

          currentGroup++;

        } finally {
          OBContext.restorePreviousMode();
        }
      }
    }

    if (!found) {

      // ENTREGAS A RENDIR DETAILS
      OBCriteria<ScoRendicioncuentas> rendAccounts = OBDal.getInstance()
          .createCriteria(ScoRendicioncuentas.class);
      rendAccounts.add(Restrictions.eq(ScoRendicioncuentas.PROPERTY_ID, documentId));

      rendAccounts.setFilterOnReadableClients(false);
      rendAccounts.setFilterOnReadableOrganization(false);

      List<ScoRendicioncuentas> rendAccountList = rendAccounts.list();

      if (rendAccountList.size() > 0) {
        found = true;
        OBContext.setAdminMode(true);
        try {

          int actualGroup = currentGroup;

          ScoRendicioncuentas rendCuentas = rendAccountList.get(0);

          UnpaidToVendorLine lineRendCuentas = new UnpaidToVendorLine();
          lineRendCuentas.specialdoctype = rendCuentas.getDocumentType().getScoSpecialdoctype();
          lineRendCuentas.specialdoctypeplus = rendCuentas.getDocumentType().getScoSpecialdoctype();
          lineRendCuentas.groupNumber = actualGroup;
          lineRendCuentas.invoicedDate = OBDateUtils.formatDate(rendCuentas.getDategen());
          lineRendCuentas.documentId = rendCuentas.getId();

          lineRendCuentas.dueDate = OBDateUtils.formatDate(rendCuentas.getDategen());
          lineRendCuentas.currency = rendCuentas.getCurrency().getISOCode();
          lineRendCuentas.docType = "FC";
          lineRendCuentas.tipoOp = "A Rendir";
          lineRendCuentas.documentNumber = rendCuentas.getDocumentNo();
          lineRendCuentas.percepcion = new BigDecimal(0);

          lineRendCuentas.paymentMethod = "";
          lineRendCuentas.paymentCondition = "";

          lineRendCuentas.debit = new BigDecimal(0);
          lineRendCuentas.credit = new BigDecimal(0);

          BigDecimal saldo = getSaldoRendCuentas(rendCuentas, startingDate);

          if (saldo.compareTo(BigDecimal.ZERO) == 1) {
            lineRendCuentas.credit = saldo;
          } else {
            lineRendCuentas.debit = saldo;
          }

          if (lineRendCuentas.credit.compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal tmp = lineRendCuentas.credit;
            lineRendCuentas.credit = lineRendCuentas.debit.negate();
            lineRendCuentas.debit = tmp.negate();
          }

          lineRendCuentas.balance = lineRendCuentas.debit.subtract(lineRendCuentas.credit);
          lineRendCuentas.estaFacturado = "SI";

          lineRendCuentas.daysTillDue = null;

          lineRendCuentas.groupDocument = 1;

          lsLines.add(lineRendCuentas);

          BigDecimal totalBalance = new BigDecimal(0.00).add(lineRendCuentas.balance);

          OBCriteria<FIN_PaymentDetail> paymentDetails = OBDal.getInstance()
              .createCriteria(FIN_PaymentDetail.class);
          paymentDetails.add(Restrictions.eq(FIN_PaymentDetail.PROPERTY_SCOPAYOUTRENDCUENTAS,
              rendCuentas.getId()));
          List<FIN_PaymentDetail> paymentDetailList = paymentDetails.list();

          for (int j = 0; j < paymentDetailList.size(); j++) {
            FIN_PaymentDetail pd = paymentDetailList.get(j);
            if (pd.getFinPayment() == null)
              continue;
            if (pd.getFinPayment().getStatus().equals("RPAP")
                || pd.getFinPayment().getStatus().equals("RPAE")) {
              continue;
            }

            Date pdt = pd.getFinPayment().getPaymentDate();

            if (pdt.compareTo(startingDate) < 0 || pdt.compareTo(endingDateEnd) > 0)
              continue;

            UnpaidToVendorLine lineRC = new UnpaidToVendorLine();
            lineRC.groupNumber = actualGroup;
            lineRC.invoicedDate = OBDateUtils.formatDate(pd.getFinPayment().getPaymentDate());
            lineRC.dueDate = OBDateUtils.formatDate(pd.getFinPayment().getPaymentDate());
            lineRC.currency = rendCuentas.getCurrency().getISOCode();
            lineRC.docType = "RC"; // pagos
            lineRC.paymentCondition = "";
            lineRC.documentNumber = pd.getFinPayment().getDocumentNo();
            lineRC.percepcion = BigDecimal.ZERO;
            lineRC.bankAccount = pd.getFinPayment().getAccount().getName();
            lineRC.debit = new BigDecimal(0);
            lineRC.credit = new BigDecimal(0);

            lineRC.specialdoctype = pd.getFinPayment().getSCOSpecialDocType();
            lineRC.specialdoctypeplus = pd.getFinPayment().getScoSpecialdoctypeplus() != null
                ? pd.getFinPayment().getScoSpecialdoctypeplus()
                : pd.getFinPayment().getSCOSpecialDocType();
            lineRC.documentId = pd.getFinPayment().getId();

            if (pd.getAmount().compareTo(BigDecimal.ZERO) == 1) {
              lineRC.debit = pd.getAmount();
            } else {
              lineRC.credit = pd.getAmount().negate();
            }

            lineRC.balance = lineRC.debit.subtract(lineRC.credit);

            if (lineRC.balance.compareTo(BigDecimal.ZERO) == -1)// CI
              lineRC.docType = "CI"; // pago

            lineRC.tipoOp = "Pago";

            totalBalance = totalBalance.add(lineRC.balance);
            lineRC.balance = new BigDecimal(totalBalance.toString());

            lineRC.paymentMethod = "";
            lineRC.daysTillDue = null;

            lsLines.add(lineRC);
          }

          currentGroup++;

        } finally {
          OBContext.restorePreviousMode();
        }
      }
    }

    return lsLines;

  }

  public static BigDecimal getSaldoInvoiceWithGlItem(Invoice inv, Date dttDate) {

    OBCriteria<FIN_PaymentScheduleDetail> schedules = OBDal.getInstance()
        .createCriteria(FIN_PaymentScheduleDetail.class);
    schedules.add(Restrictions.eq(FIN_PaymentScheduleDetail.PROPERTY_SCOINVOICEGLREF, inv));
    List<FIN_PaymentScheduleDetail> scheduleList = schedules.list();
    BigDecimal saldo = new BigDecimal(0).setScale(2);
    for (int i = 0; i < scheduleList.size(); i++) {
      FIN_PaymentScheduleDetail dd = scheduleList.get(i);
      if (dd.getPaymentDetails() == null)
        continue;
      if (dd.getPaymentDetails().getFinPayment() == null)
        continue;
      if (dd.getPaymentDetails().getGLItem() == null)
        continue;

      if (dd.getPaymentDetails().getFinPayment().getPaymentDate().compareTo(dttDate) > 0
          || dd.getPaymentDetails().getFinPayment().getStatus().equals("RPAP")
          || dd.getPaymentDetails().getFinPayment().getStatus().equals("RPAE")) {
        continue;
      }
      if (!dd.getPaymentDetails().getGLItem().getFinancialMgmtGLItemAccountsList().get(0)
          .getGlitemCreditAcct().getAccount().getSearchKey().startsWith("12"))
        continue;

      // COBROS
      if (dd.getPaymentDetails().getFinPayment().isReceipt()) {
        saldo = saldo.add(dd.getPaymentDetails().getAmount());
        // PAGOS
      } else {
        saldo = saldo.subtract(dd.getPaymentDetails().getAmount());
      }
    }

    return saldo;
  }

  public static BigDecimal getSaldoInvoice(Invoice inv, Date dttDate, boolean includeBoeExternal) {

    BigDecimal saldo = inv.getGrandTotalAmount();

    if (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOAPCREDITMEMO")) {
      saldo = saldo.negate();
    }

    // now special payments
    OBCriteria<FIN_PaymentSchedule> specialSchedule = OBDal.getInstance()
        .createCriteria(FIN_PaymentSchedule.class);
    specialSchedule.add(Restrictions.eq(FIN_PaymentSchedule.PROPERTY_INVOICE, inv));
    List<FIN_PaymentSchedule> specialscheduleList = specialSchedule.list();

    for (int i = 0; i < specialscheduleList.size(); i++) {
      List<FIN_PaymentScheduleDetail> specialDetails = specialscheduleList.get(i)
          .getFINPaymentScheduleDetailInvoicePaymentScheduleList();
      for (int j = 0; j < specialDetails.size(); j++) {
        FIN_PaymentScheduleDetail sdetail = specialDetails.get(j);

        if (!sdetail.isScoExternalpayment()) {
          if (sdetail.getPaymentDetails() != null) {
            Date dttDetail = sdetail.getPaymentDetails().getFinPayment().getPaymentDate();

            if (dttDetail.compareTo(dttDate) <= 0
                && (!sdetail.getPaymentDetails().getFinPayment().getStatus().equals("RPAP")
                    && !sdetail.getPaymentDetails().getFinPayment().getStatus().equals("RPAE"))) {
              saldo = saldo.subtract(sdetail.getAmount().add(sdetail.getWriteoffAmount()));
            }
          }
        }

        else if (sdetail.isScoExternalpayment()
            && (includeBoeExternal || !sdetail.isScoIsboepayment())) {

          Date dttDetail = null;

          if (sdetail.isScoIsboepayment()) {
            SCOBillofexchange boe = null;

            OBCriteria<SCOBoefromPayschedetail> qryMaybe1 = OBDal.getInstance()
                .createCriteria(SCOBoefromPayschedetail.class);
            qryMaybe1.add(
                Restrictions.eq(SCOBoefromPayschedetail.PROPERTY_PAYMENTSCHEDULEDETAIL, sdetail));

            List<SCOBoefromPayschedetail> lsMaybe1 = qryMaybe1.list();
            if (lsMaybe1.size() > 0) {
              boe = lsMaybe1.get(0).getSCOBoeFrom().getBillOfExchange();
            }

            dttDetail = boe.getAccountingDate();

          } else if (sdetail.isScoIswithholdingpayment()) {

            OBCriteria<SCOPwlrecliPayschedetail> qryMaybe1 = OBDal.getInstance()
                .createCriteria(SCOPwlrecliPayschedetail.class);
            qryMaybe1.add(
                Restrictions.eq(SCOPwlrecliPayschedetail.PROPERTY_PAYMENTSCHEDULEDETAIL, sdetail));

            List<SCOPwlrecliPayschedetail> lsMaybe1 = qryMaybe1.list();
            if (lsMaybe1.size() > 0) {
              dttDetail = lsMaybe1.get(0).getSCOPwithhoRecLine().getSCOPwithholdingReceipt()
                  .getAccountingDate();
            } else {
              OBCriteria<SCOPwlrecliPayschedetail> qryMaybe2 = OBDal.getInstance()
                  .createCriteria(SCOPwlrecliPayschedetail.class);
              qryMaybe2.add(Restrictions.eq(SCOPwlrecliPayschedetail.PROPERTY_PAYMENTSCHEDULEDETAIL,
                  sdetail));

              List<SCOPwlrecliPayschedetail> lsMaybe2 = qryMaybe2.list();

              if (lsMaybe2.size() > 0) {
                dttDetail = lsMaybe2.get(0).getSCOPwithhoRecLine().getSCOPwithholdingReceipt()
                    .getAccountingDate();
              }
            }
          }

          if (dttDetail.compareTo(dttDate) < 0) {
            saldo = saldo.subtract(sdetail.getAmount());
          }
        }
      }
    }

    return saldo;
  }

  public static BigDecimal getSaldoPrepayment(SCOPrepayment prepay, Date dttDate) {

    BigDecimal saldo = prepay.getAmount();

    // payments
    OBCriteria<FIN_PaymentDetail> paymentDetails = OBDal.getInstance()
        .createCriteria(FIN_PaymentDetail.class);
    paymentDetails
        .add(Restrictions.eq(FIN_PaymentDetail.PROPERTY_SCOPAYOUTPREPAYMENT, prepay.getId()));
    List<FIN_PaymentDetail> paymentDetailList = paymentDetails.list();
    for (int i = 0; i < paymentDetailList.size(); i++) {
      FIN_PaymentDetail pd = paymentDetailList.get(i);

      Date dtt = pd.getFinPayment().getPaymentDate();

      if (dtt.compareTo(dttDate) <= 0 && (!pd.getFinPayment().getStatus().equals("RPAP")
          && !pd.getFinPayment().getStatus().equals("RPAE"))) {
        saldo = saldo.subtract(pd.getAmount().add(pd.getWriteoffAmount()));
      }
    }

    return saldo;
  }

  public static BigDecimal getSaldoRendCuentas(ScoRendicioncuentas rendCuentas, Date dttDate) {

    BigDecimal saldo = rendCuentas.getAmount();

    // payments
    OBCriteria<FIN_PaymentDetail> paymentDetails = OBDal.getInstance()
        .createCriteria(FIN_PaymentDetail.class);
    paymentDetails
        .add(Restrictions.eq(FIN_PaymentDetail.PROPERTY_SCOPAYOUTRENDCUENTAS, rendCuentas.getId()));
    List<FIN_PaymentDetail> paymentDetailList = paymentDetails.list();
    for (int i = 0; i < paymentDetailList.size(); i++) {
      FIN_PaymentDetail pd = paymentDetailList.get(i);

      Date dtt = pd.getFinPayment().getPaymentDate();

      if (dtt.compareTo(dttDate) <= 0 && (!pd.getFinPayment().getStatus().equals("RPAP")
          && !pd.getFinPayment().getStatus().equals("RPAE"))) {
        saldo = saldo.subtract(pd.getAmount().add(pd.getWriteoffAmount()));
      }
    }

    return saldo;
  }

  public static BigDecimal getGeneralSaldoInvoice(ConnectionProvider conProvider, Invoice inv,
      Date dttDate) throws ServletException {

    String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_JAVA);
    String strDttDate = sdf.format(dttDate);

    BigDecimal saldo = inv.getGrandTotalAmount();

    if (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOAPCREDITMEMO")) {
      saldo = saldo.negate();
    }

    String strPaidamount = UnpaidToVendorData.getInvTotalPaidAmountByDate(conProvider,
        inv.getClient().getId(), inv.getId(), strDttDate);
    if (strPaidamount == null) {
      strPaidamount = "0";
    } else if (strPaidamount.isEmpty()) {
      strPaidamount = "0";
    }
    saldo = saldo.subtract(new BigDecimal(strPaidamount));
    return saldo;
  }

  public static BigDecimal getGeneralSaldoPrepayment(ConnectionProvider conProvider,
      SCOPrepayment prepay, Date dttDate) throws ServletException {

    String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_JAVA);
    String strDttDate = sdf.format(dttDate);

    BigDecimal saldo = prepay.getAmount();

    String strPaidamount = UnpaidToVendorData.getPrepayTotalPaidAmountByDate(conProvider,
        prepay.getClient().getId(), prepay.getId(), strDttDate);
    if (strPaidamount == null) {
      strPaidamount = "0";
    } else if (strPaidamount.isEmpty()) {
      strPaidamount = "0";
    }
    saldo = saldo.subtract(new BigDecimal(strPaidamount));
    return saldo;
  }

  public static BigDecimal getGeneralSaldoRendCuentas(ConnectionProvider conProvider,
      ScoRendicioncuentas rendCuentas, Date dttDate) throws ServletException {

    String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_JAVA);
    String strDttDate = sdf.format(dttDate);

    BigDecimal saldo = rendCuentas.getAmount();

    String strPaidamount = UnpaidToVendorData.getRendCuentasTotalPaidAmountByDate(conProvider,
        rendCuentas.getClient().getId(), rendCuentas.getId(), strDttDate);
    if (strPaidamount == null) {
      strPaidamount = "0";
    } else if (strPaidamount.isEmpty()) {
      strPaidamount = "0";
    }
    saldo = saldo.subtract(new BigDecimal(strPaidamount));
    return saldo;
  }

  public static String selectCurrency(String currencyCode) {

    OBCriteria<Currency> currency = OBDal.getInstance().createCriteria(Currency.class);
    currency.add(Restrictions.eq(Currency.PROPERTY_ISOCODE, currencyCode));
    Currency curr = (Currency) currency.uniqueResult();
    return curr.getId();
  }

  public static BigDecimal convertInvoiceAmountToPEN(Invoice inv, BigDecimal amount) {

    String currencyPEN_id = selectCurrency("PEN");

    ConversionRateDoc conversionRateCurrentDoc = AcctServer.getConversionRateDocStatic(
        AcctServer.TABLEID_Invoice, inv.getId(), inv.getCurrency().getId(), currencyPEN_id);
    if (conversionRateCurrentDoc != null) {
      return AcctServer.applyRate(amount, conversionRateCurrentDoc, true).setScale(2,
          BigDecimal.ROUND_HALF_UP);
    } else {
      return new BigDecimal(AcctServer.getConvertedAmt(amount.toString(), inv.getCurrency().getId(),
          currencyPEN_id, OBDateUtils.formatDate(inv.getInvoiceDate()), "", inv.getClient().getId(),
          inv.getOrganization().getId(), new DalConnectionProvider()));
    }

  }

  public static List<RetenRegVendor> getRegistroRetenciones(String strBp, Date startingDate,
      Date endingDate) throws Exception {

    List<RetenRegVendor> lsLines = new ArrayList<RetenRegVendor>();

    BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class, strBp);

    OBCriteria<SCOPwithholdingReceipt> comprobantes = OBDal.getInstance()
        .createCriteria(SCOPwithholdingReceipt.class);
    if (bp != null)
      comprobantes.add(Restrictions.eq(SCOPwithholdingReceipt.PROPERTY_BUSINESSPARTNER, bp));
    if (startingDate != null && endingDate != null)
      comprobantes.add(Restrictions.between(SCOPwithholdingReceipt.PROPERTY_GENERATEDDATE,
          startingDate, endingDate));
    comprobantes.add(Restrictions.eq(SCOPwithholdingReceipt.PROPERTY_DOCUMENTSTATUS, "CO"));
    comprobantes.addOrderBy(SCOPwithholdingReceipt.PROPERTY_GENERATEDDATE, true);

    comprobantes.setFilterOnReadableClients(false);
    comprobantes.setFilterOnReadableOrganization(false);

    List<SCOPwithholdingReceipt> comprobantesList = comprobantes.list();

    List<Invoice> lsInvoicesReten = new ArrayList<Invoice>();
    HashMap<String, Integer> hashInvoices = new HashMap<String, Integer>();
    HashMap<String, List<SCOPwithhoRecLine>> hashReten = new HashMap<String, List<SCOPwithhoRecLine>>();

    for (int i = 0; i < comprobantesList.size(); i++) {
      SCOPwithholdingReceipt perc = comprobantesList.get(i);
      List<SCOPwithhoRecLine> details = perc.getSCOPwithhoRecLineList();

      for (int j = 0; j < details.size(); j++) {
        SCOPwithhoRecLine detail = details.get(j);
        Invoice inv = detail.getInvoiceref();

        List<Invoice> lsInvoices = new ArrayList<Invoice>();
        lsInvoices.add(inv);

        for (int k = 0; k < lsInvoices.size(); k++) {
          Invoice invAux = lsInvoices.get(k);
          Integer pos = hashInvoices.get(invAux.getId());
          if (pos == null) {
            lsInvoicesReten.add(invAux);
            pos = new Integer(lsInvoicesReten.size() - 1);
            hashInvoices.put(invAux.getId(), pos);
            hashReten.put(invAux.getId(), new ArrayList<SCOPwithhoRecLine>());
          }

          Invoice invReten = lsInvoicesReten.get(pos);
          List<SCOPwithhoRecLine> lsRetenDetail = hashReten.get(invReten.getId());
          lsRetenDetail.add(detail);
        }
      }
    }
    int currentGroup = 1;
    // now fill-in
    for (int i = 0; i < lsInvoicesReten.size(); i++) {

      Invoice inv = lsInvoicesReten.get(i);
      // invoice
      RetenRegVendor retenReg = new RetenRegVendor();
      retenReg.transDate = inv.getInvoiceDate();
      retenReg.groupNumber = currentGroup;
      String doctype = inv.getTransactionDocument().getScoSpecialdoctype();
      String comprobante = "00";
      if (doctype != null) {
        if (doctype.equals("SCOAPINVOICE") || doctype.equals("SCOAPSIMPLEPROVISIONINVOICE"))
          comprobante = inv.getScoPodoctypeComboitem().getCode(); // factura
        if (doctype.equals("SCOAPCREDITMEMO"))
          comprobante = "07"; // credito
      }
      retenReg.tipoDoc = comprobante;

      String docnumber = inv.getScrPhysicalDocumentno();
      if (docnumber == null || docnumber.equals(""))
        docnumber = "000-00000";
      StringTokenizer st = new StringTokenizer(docnumber, "-");

      String serieInvoice = st.nextToken().replaceAll("\\D+", "");
      if (serieInvoice.length() < 1 || serieInvoice.length() > 4)
        throw new Exception();

      String nroInvoice = st.nextToken().replaceAll("\\D+", "");
      if (nroInvoice.length() < 1 || nroInvoice.length() > 8)
        throw new Exception();

      retenReg.serieDoc = serieInvoice;
      retenReg.numDoc = nroInvoice;
      retenReg.totalDoc = inv.getScoGrandtotalNoperc().setScale(2).toString();
      retenReg.tipoTrans = "Compra";
      retenReg.debe = retenReg.totalDoc;
      retenReg.haber = "";
      retenReg.saldo = getSaldoInvoice(inv, startingDate, false).toString();
      retenReg.serieComp = "";
      retenReg.numComp = "";
      retenReg.totalComp = "";
      retenReg.partnerUid = inv.getBusinessPartner().getId();
      retenReg.partnerCommName = inv.getBusinessPartner().getName();
      retenReg.partnerRuc = inv.getBusinessPartner().getTaxID();
      retenReg.invo = inv;
      retenReg.currencySymbol = inv.getCurrency().getSymbol();

      List<RetenRegVendor> temporalList = new ArrayList<RetenRegVendor>();
      temporalList.add(retenReg);

      // Comprobantes
      BigDecimal saldoDecimal = new BigDecimal(retenReg.saldo);

      List<SCOPwithhoRecLine> lsReten = hashReten.get(inv.getId());
      DalConnectionProvider provider = new DalConnectionProvider();
      for (int j = 0; j < lsReten.size(); j++) {

        SCOPwithhoRecLine detail = lsReten.get(j);

        RetenRegVendor retenReg2 = new RetenRegVendor();
        retenReg2.transDate = detail.getSCOPwithholdingReceipt().getGeneratedDate();
        retenReg2.tipoDoc = comprobante;
        retenReg2.serieDoc = serieInvoice;
        retenReg2.numDoc = nroInvoice;
        retenReg2.totalDoc = "";
        retenReg2.tipoTrans = "Pago";
        retenReg2.debe = "";
        retenReg2.groupNumber = currentGroup;
        BigDecimal montoaConvertir = detail.getPaymentamount().add(detail.getAmount());// agregado
                                                                                       // por q no
                                                                                       // cuadran
                                                                                       // los montos
                                                                                       // cuando se
                                                                                       // suman
                                                                                       // convertidos
                                                                                       // a si q
                                                                                       // mejor
                                                                                       // sumarlo en
                                                                                       // moneda
                                                                                       // nativa y
                                                                                       // despues
                                                                                       // convertirlos

        String montoTotal = AcctServer.getConvertedAmt(montoaConvertir.toString(),
            inv.getCurrency().getId(),
            Utility.stringBaseCurrencyId(provider, inv.getClient().getId()),
            OBDateUtils.formatDate(detail.getSCOPwithholdingReceipt().getGeneratedDate()), "",
            inv.getClient().getId(), inv.getOrganization().getId(), provider);

        // String montoTotal = AcctServer.getConvertedAmt(detail
        // .getPaymentamount().setScale(2, RoundingMode.HALF_UP)
        // .toString(), inv.getCurrency().getId(),
        // Utility.stringBaseCurrencyId(provider, inv.getClient()
        // .getId()), OBDateUtils
        // .formatDate(detail.getSCOPwithholdingReceipt()
        // .getGeneratedDate()), "", inv
        // .getClient().getId(), inv.getOrganization()
        // .getId(), provider);

        String montoRetencion = detail.getConvertedAmount().toString();

        // BigDecimal totalb = new BigDecimal(montoTotal)
        // .add(new BigDecimal(montoRetencion));

        BigDecimal totalb = new BigDecimal(montoTotal).setScale(2, RoundingMode.HALF_UP);

        retenReg2.haber = totalb.toString();

        saldoDecimal = saldoDecimal.subtract(totalb);

        retenReg2.saldo = saldoDecimal.toString();

        StringTokenizer st2 = new StringTokenizer(
            detail.getSCOPwithholdingReceipt().getWithholdingnumber(), "-");

        String serieComp = st2.nextToken().replaceAll("\\D+", "");
        String nroComp = st2.nextToken().replaceAll("\\D+", "");

        retenReg2.serieComp = serieComp;
        retenReg2.numComp = nroComp;
        retenReg2.totalComp = montoRetencion;
        retenReg2.partnerUid = inv.getBusinessPartner().getId();
        retenReg2.partnerCommName = inv.getBusinessPartner().getName();
        retenReg2.partnerRuc = inv.getBusinessPartner().getTaxID();
        retenReg2.invo = inv;

        retenReg2.currencySymbol = inv.getCurrency().getSymbol();

        // ----------------------------------------------------------------------------------------------------------------------------------------------

        BigDecimal montoPago = detail.getPaymentamount().setScale(2, RoundingMode.HALF_UP)
            .add(detail.getAmount());

        retenReg2.montoPagoOriginal = montoPago.toString();

        temporalList.add(retenReg2);

      }

      if (temporalList.size() > 1) {
        for (int j = 0; j < temporalList.size(); j++) {
          lsLines.add(temporalList.get(j));
        }
        currentGroup++;
      }
    }

    return lsLines;
  }

}