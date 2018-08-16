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
import org.openbravo.model.common.invoice.InvoiceTax;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.service.db.DalConnectionProvider;

import pe.com.unifiedgo.accounting.SunatUtil;
import pe.com.unifiedgo.accounting.data.SCOBillofexchange;
import pe.com.unifiedgo.accounting.data.SCOBoeFrom;
import pe.com.unifiedgo.accounting.data.SCOBoeTo;
import pe.com.unifiedgo.accounting.data.SCOBoefromPayschedetail;
import pe.com.unifiedgo.accounting.data.SCOPaymentHistory;
import pe.com.unifiedgo.accounting.data.SCOPercepSales;
import pe.com.unifiedgo.accounting.data.SCOPercepsalesDetail;
import pe.com.unifiedgo.accounting.data.SCOPwlrecliPayschedetail;
import pe.com.unifiedgo.accounting.data.SCOSwlrecliPayschedetail;

class UnpaidByClientLine implements Comparable<UnpaidByClientLine> {

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
  public int compareTo(UnpaidByClientLine o) {
    if (o.groupDocument > groupDocument) {
      return -1;
    }
    if (o.groupDocument < groupDocument) {
      return 1;
    }
    return dateVenc.compareTo(o.dateVenc);
  }

}

class UnpaidByClientLineWithPartner {
  public UnpaidByClientLineWithPartner() {
    unpaidbyclients = new ArrayList<UnpaidByClientLine>();
    cBpartnerId = "";
  }

  List<UnpaidByClientLine> unpaidbyclients;
  String cBpartnerId;
}

class PercepReg {
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

  // String idInvoice;///////////////AGREGADO X RICARDO
  // String fechaEmision;///////////////AGREGADO X RICARDO
  Invoice invo;
  String montoPagoOriginal;
  int groupNumber;

}

public class UnpaidByClient {

  private static Date getEndOfDay(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    int year = calendar.get(Calendar.YEAR);
    int month = calendar.get(Calendar.MONTH);
    int day = calendar.get(Calendar.DATE);
    calendar.set(year, month, day, 23, 59, 59);
    return calendar.getTime();
  }

  public static HashMap<String, UnpaidByClientLineWithPartner> getUnpaidByOrgToDate(
      ConnectionProvider conProvider, String adClientId, String strOrgId, Date startingDate,
      Date endingDate, boolean onlyPendings) throws ServletException, IOException {
    String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_JAVA);
    String strStartingDate = sdf.format(startingDate);
    String strEndingDate = sdf.format(endingDate);

    String strTreeOrg = TreeData.getTreeOrg(conProvider, adClientId);
    String strOrgFamily = Tree.getMembers(conProvider, strTreeOrg, strOrgId);

    HashMap<String, UnpaidByClientLineWithPartner> map = new HashMap<String, UnpaidByClientLineWithPartner>();

    UnpaidByClientData[] data = UnpaidByClientData.getPendingInvByOrg(conProvider, adClientId,
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
        dueDate = inv.getAccountingDate();

      String tipoOp = "--";
      String documentType = "";
      String paymentMethod = "";
      if (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOARINVOICE")) {
        documentType = "FV";
        if (inv.isSsaIsprepayment())
          tipoOp = "Anticipo";
        else if (inv.isScoIsforfree())
          tipoOp = "Factura Título Gratuito";
        else
          tipoOp = "Factura";
        paymentMethod = inv.getPaymentMethod().getName();
      } else if (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOARTICKET")) {
        documentType = "BV";
        tipoOp = "Boleta";
      } else if (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOARCREDITMEMO")
          || inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOARINVOICERETURNMAT")
          || inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOARINVOICEREVERSE")) {
        documentType = "NA";
        tipoOp = "Nota de Crédito";

      } else if (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOARDEBITMEMO")) {
        documentType = "NC";
        tipoOp = "Nota de Débito";
      } else if (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOARBOEINVOICE")) {
        documentType = "LC";
        tipoOp = "Letra";
      }

      UnpaidByClientLine lineFV = new UnpaidByClientLine();
      lineFV.orgName = inv.getOrganization().getName();
      lineFV.specialdoctype = inv.getSCOSpecialDocType();
      lineFV.specialdoctypeplus = inv.getSsaSpecialdoctypeplus() != null
          ? inv.getSsaSpecialdoctypeplus()
          : inv.getSCOSpecialDocType();
      lineFV.groupNumber = currentGroup;
      lineFV.invoicedDate = OBDateUtils.formatDate(inv.getAccountingDate());
      lineFV.documentId = inv.getId();

      lineFV.dueDate = OBDateUtils.formatDate(dueDate);
      lineFV.dateVenc = dueDate;
      lineFV.currency = inv.getCurrency().getISOCode();
      lineFV.docType = documentType;
      lineFV.tipoOp = tipoOp;
      lineFV.paymentMethod = paymentMethod;
      lineFV.paymentCondition = "";
      if (documentType.equals("FV") || documentType.equals("BV") || documentType.equals("LC")
          || documentType.equals("NA") || documentType.equals("NC")) {
        lineFV.paymentCondition = inv.getPaymentTerms().getName();// inv.getPaymentTerms().getSearchKey();
        lineFV.docRecepcion = (inv.isScrIsreceived() ? "SI" : "NO");
        if (inv.isScrIsreceived()) {
          if (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOARBOEINVOICE")) {
            if (inv.getSsaBoeDatereceipt() != null) {
              lineFV.recepcionDate = OBDateUtils.formatDate(inv.getSsaBoeDatereceipt());
            } else {
              lineFV.recepcionDate = OBDateUtils.formatDate(inv.getInvoiceDate());
            }
          } else {
            lineFV.recepcionDate = OBDateUtils.formatDate(inv.getInvoiceDate());
          }
        }

      }

      lineFV.documentNumber = inv.getScrPhysicalDocumentno();
      if (documentType.equals("LC")) {
        lineFV.documentNumber = lineFV.documentNumber + " ";
        if (inv.isSCOIsProtested())
          lineFV.documentNumber = lineFV.documentNumber + "(P)";

        if (inv.isScoIsboerenewal() != null && inv.isScoIsboerenewal())
          lineFV.documentNumber = lineFV.documentNumber + "(R)";
        else if (inv.isSsaIsboerefinancing() != null && inv.isSsaIsboerefinancing())
          lineFV.documentNumber = lineFV.documentNumber + "(F)";

      }
      lineFV.percepcion = new BigDecimal(0);

      if (documentType.equals("FV") || documentType.equals("LC")) {
        List<InvoiceTax> lsTaxes = inv.getInvoiceTaxList();
        for (int k = 0; k < lsTaxes.size(); k++)
          if (lsTaxes.get(k).getTax().getScoSpecialtax().equals("SCOPERCEPCION")) {
            lineFV.percepcion = lineFV.percepcion.add(lsTaxes.get(k).getTaxAmount());
          }
      }

      lineFV.debit = new BigDecimal(0);
      lineFV.credit = new BigDecimal(0);

      BigDecimal total = inv.getGrandTotalAmount().abs();

      if (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOARCREDITMEMO")
          || inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOARINVOICERETURNMAT")
          || inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOARINVOICEREVERSE")) {

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
        lineFV.credit = total;
        lineFV.debit = saldo;
      } else {
        lineFV.credit = saldo.negate();
        lineFV.debit = total.negate();
      }

      lineFV.balance = lineFV.debit.subtract(lineFV.credit);
      lineFV.estaFacturado = "SI";

      lineFV.daysTillDue = null;
      if (!inv.isPaymentComplete()) {
        lineFV.daysTillDue = inv.getDaysTillDue();
      }

      lineFV.groupDocument = 1;
      BusinessPartner bp = inv.getBusinessPartner();
      String taxid = bp.getTaxID();

      String creditSol = "0.00";
      String creditDol = "PEN"; // ahora es tipo de moneda de credito
      if (bp.getSsaCreditcurrency() != null)
        creditDol = bp.getSsaCreditcurrency().getISOCode();

      creditSol = bp.getCreditLimit().toString();
      lineFV.creditSol = creditSol;
      lineFV.creditDol = creditDol;
      lineFV.ultFactura = ultFactura;
      lineFV.ultPago = ultPago;

      if (counter % 50 == 0) {
        OBDal.getInstance().getSession().clear();
      }
      counter++;

      if (onlyPendings && lineFV.balance.compareTo(BigDecimal.ZERO) == 0)
        continue;

      UnpaidByClientLineWithPartner unpaidbyclientliWithPartner = map.get(taxid);
      if (unpaidbyclientliWithPartner != null) {
        unpaidbyclientliWithPartner.unpaidbyclients.add(lineFV);
      } else {
        unpaidbyclientliWithPartner = new UnpaidByClientLineWithPartner();
        unpaidbyclientliWithPartner.unpaidbyclients.add(lineFV);
        unpaidbyclientliWithPartner.cBpartnerId = bp.getId();
        map.put(taxid, unpaidbyclientliWithPartner);
      }

      currentGroup++;
    }

    for (Map.Entry<String, UnpaidByClientLineWithPartner> entry : map.entrySet()) {
      List<UnpaidByClientLine> lsLines = entry.getValue().unpaidbyclients;

      if (lsLines.size() == 0)
        continue;

      UnpaidByClientLine lineHeader = new UnpaidByClientLine();
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

  public static List<UnpaidByClientLine> getUnpaidByClient(ConnectionProvider conProvider,
      String strBpId, Date startingDate, Date endingDate, boolean onlyPendings, String strOrgId)
      throws ServletException {

    Date dttToday = new Date();
    List<UnpaidByClientLine> lsLines = new ArrayList<UnpaidByClientLine>();

    BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class, strBpId);

    if (bp == null)
      return lsLines;

    Set<String> orgList = OBContext.getOBContext().getOrganizationStructureProvider()
        .getNaturalTree(strOrgId);

    OBCriteria<Invoice> accounts = OBDal.getInstance().createCriteria(Invoice.class);
    accounts.createAlias(Invoice.PROPERTY_BUSINESSPARTNER, "bp");

    accounts.add(Restrictions.eq("bp." + BusinessPartner.PROPERTY_TAXID, bp.getTaxID()));
    accounts.add(Restrictions.eq(Invoice.PROPERTY_SALESTRANSACTION, true));
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
    oldAccounts.add(Restrictions.eq(Invoice.PROPERTY_SALESTRANSACTION, true));
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
    ultimaFactura.add(Restrictions.eq(Invoice.PROPERTY_SALESTRANSACTION, true));
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
      if (invUlt.getTransactionDocument().getScoSpecialdoctype().equals("SCOARINVOICE")
          || invUlt.getTransactionDocument().getScoSpecialdoctype().equals("SCOARTICKET")) {
        ultFactura = DATE_FORMAT.format(invUlt.getInvoiceDate());
        break;
      }
    }

    OBCriteria<FIN_Payment> ultimoPago = OBDal.getInstance().createCriteria(FIN_Payment.class);
    ultimoPago.createAlias(FIN_Payment.PROPERTY_BUSINESSPARTNER, "bp");
    ultimoPago.add(Restrictions.eq("bp." + BusinessPartner.PROPERTY_TAXID, bp.getTaxID()));
    ultimoPago.add(Restrictions.eq(FIN_Payment.PROPERTY_RECEIPT, true));
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
          dueDate = inv.getAccountingDate();

        String tipoOp = "--";
        String documentType = "";
        String paymentMethod = "";
        if (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOARINVOICE")) {
          documentType = "FV";
          if (inv.isSsaIsprepayment())
            tipoOp = "Anticipo";
          else if (inv.isScoIsforfree())
            tipoOp = "Factura Título Gratuito";
          else
            tipoOp = "Factura";
          paymentMethod = inv.getPaymentMethod().getName();
        } else if (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOARTICKET")) {
          documentType = "BV";
          tipoOp = "Boleta";
        } else if (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOARCREDITMEMO")
            || inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOARINVOICERETURNMAT")
            || inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOARINVOICEREVERSE")) {
          documentType = "NA";
          tipoOp = "Nota de Crédito";

        } else if (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOARDEBITMEMO")) {
          documentType = "NC";
          tipoOp = "Nota de Débito";
        } else if (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOARBOEINVOICE")) {
          documentType = "LC";
          tipoOp = "Letra";
        }

        UnpaidByClientLine lineFV = new UnpaidByClientLine();
        lineFV.orgName = inv.getOrganization().getName();
        lineFV.specialdoctype = inv.getSCOSpecialDocType();
        lineFV.specialdoctypeplus = inv.getSsaSpecialdoctypeplus() != null
            ? inv.getSsaSpecialdoctypeplus()
            : inv.getSCOSpecialDocType();
        lineFV.groupNumber = actualGroup;
        lineFV.invoicedDate = OBDateUtils.formatDate(inv.getAccountingDate());
        lineFV.documentId = inv.getId();

        lineFV.dueDate = OBDateUtils.formatDate(dueDate);
        lineFV.dateVenc = dueDate;
        lineFV.currency = inv.getCurrency().getISOCode();
        lineFV.docType = documentType;
        lineFV.tipoOp = tipoOp;
        lineFV.paymentMethod = paymentMethod;
        lineFV.paymentCondition = "";
        if (documentType.equals("FV") || documentType.equals("BV") || documentType.equals("LC")
            || documentType.equals("NA") || documentType.equals("NC")) {
          lineFV.paymentCondition = inv.getPaymentTerms().getName();// inv.getPaymentTerms().getSearchKey();
          lineFV.docRecepcion = (inv.isScrIsreceived() ? "SI" : "NO");
          if (inv.isScrIsreceived()) {
            if (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOARBOEINVOICE")) {
              if (inv.getSsaBoeDatereceipt() != null) {
                lineFV.recepcionDate = OBDateUtils.formatDate(inv.getSsaBoeDatereceipt());
              } else {
                lineFV.recepcionDate = OBDateUtils.formatDate(inv.getInvoiceDate());
              }
            } else {
              lineFV.recepcionDate = OBDateUtils.formatDate(inv.getInvoiceDate());
            }
          }

        }

        lineFV.documentNumber = inv.getScrPhysicalDocumentno();
        if (documentType.equals("LC")) {
          lineFV.documentNumber = lineFV.documentNumber + " ";
          if (inv.isSCOIsProtested())
            lineFV.documentNumber = lineFV.documentNumber + "(P)";

          if (inv.isScoIsboerenewal() != null && inv.isScoIsboerenewal())
            lineFV.documentNumber = lineFV.documentNumber + "(R)";
          else if (inv.isSsaIsboerefinancing() != null && inv.isSsaIsboerefinancing())
            lineFV.documentNumber = lineFV.documentNumber + "(F)";

        }
        lineFV.percepcion = new BigDecimal(0);

        if (documentType.equals("FV") || documentType.equals("LC")) {
          List<InvoiceTax> lsTaxes = inv.getInvoiceTaxList();
          for (int k = 0; k < lsTaxes.size(); k++)
            if (lsTaxes.get(k).getTax().getScoSpecialtax().equals("SCOPERCEPCION")) {
              lineFV.percepcion = lineFV.percepcion.add(lsTaxes.get(k).getTaxAmount());
            }
        }

        lineFV.debit = new BigDecimal(0);
        lineFV.credit = new BigDecimal(0);

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
          lineFV.credit = saldo1;
          lineFV.debit = saldo;
        } else {
          lineFV.credit = saldo.negate();
          lineFV.debit = saldo1.negate();
        }

        if (lineFV.credit.compareTo(BigDecimal.ZERO) < 0) {
          BigDecimal tmp = lineFV.credit;
          lineFV.credit = lineFV.debit.negate();
          lineFV.debit = tmp.negate();
        }

        lineFV.balance = lineFV.debit.subtract(lineFV.credit);
        lineFV.estaFacturado = "SI";

        lineFV.daysTillDue = null;
        if (!inv.isPaymentComplete()) {
          lineFV.daysTillDue = inv.getDaysTillDue();
        }

        lineFV.groupDocument = 1;

        if (onlyPendings && lineFV.balance.compareTo(BigDecimal.ZERO) == 0)
          continue;

        lsLines.add(lineFV);

        currentGroup++;

      } finally {
        OBContext.restorePreviousMode();
      }
    }

    // System.out.println("Unpaid Success");

    UnpaidByClientLine lineHeader = new UnpaidByClientLine();
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
     * List<UnpaidByClientLine> lsLinesOrdered = new ArrayList<UnpaidByClientLine>();
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

  public static List<UnpaidByClientLine> getDetailUnpaidByClient(String documentId,
      Date startingDate, Date endingDate) {

    Date endingDateEnd = getEndOfDay(endingDate);
    List<UnpaidByClientLine> lsLines = new ArrayList<UnpaidByClientLine>();

    OBCriteria<Invoice> accounts = OBDal.getInstance().createCriteria(Invoice.class);
    accounts.add(Restrictions.eq(Invoice.PROPERTY_ID, documentId));

    accounts.setFilterOnReadableClients(false);
    accounts.setFilterOnReadableOrganization(false);

    List<Invoice> accountList = accounts.list();

    int currentGroup = 1;

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
          dueDate = inv.getAccountingDate();

        String tipoOp = "--";
        String documentType = "";
        String paymentMethod = "";
        if (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOARINVOICE")) {
          documentType = "FV";
          if (inv.isSsaIsprepayment())
            tipoOp = "Anticipo";
          else if (inv.isScoIsforfree())
            tipoOp = "Factura Título Gratuito";
          else
            tipoOp = "Factura";
          paymentMethod = inv.getPaymentMethod().getName();
        } else if (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOARTICKET")) {
          documentType = "BV";
          tipoOp = "Boleta";
        } else if (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOARCREDITMEMO")
            || inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOARINVOICERETURNMAT")
            || inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOARINVOICEREVERSE")) {
          documentType = "NA";
          tipoOp = "Nota de Crédito";

        } else if (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOARDEBITMEMO")) {
          documentType = "NC";
          tipoOp = "Nota de Débito";
        } else if (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOARBOEINVOICE")) {
          documentType = "LC";
          tipoOp = "Letra";
        }

        UnpaidByClientLine lineFV = new UnpaidByClientLine();
        lineFV.specialdoctype = inv.getSCOSpecialDocType();
        lineFV.specialdoctypeplus = inv.getSsaSpecialdoctypeplus() != null
            ? inv.getSsaSpecialdoctypeplus()
            : inv.getSCOSpecialDocType();
        lineFV.groupNumber = actualGroup;
        lineFV.invoicedDate = OBDateUtils.formatDate(inv.getAccountingDate());
        lineFV.documentId = inv.getId();

        lineFV.dueDate = OBDateUtils.formatDate(dueDate);
        lineFV.currency = inv.getCurrency().getISOCode();
        lineFV.docType = documentType;
        lineFV.tipoOp = tipoOp;
        lineFV.paymentMethod = paymentMethod;
        lineFV.paymentCondition = "";
        if (documentType.equals("FV") || documentType.equals("BV") || documentType.equals("LC")
            || documentType.equals("NA") || documentType.equals("NC")) {
          lineFV.paymentCondition = inv.getPaymentTerms().getName();// inv.getPaymentTerms().getSearchKey();
          lineFV.docRecepcion = (inv.isScrIsreceived() ? "SI" : "NO");
          if (inv.isScrIsreceived()) {
            if (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOARBOEINVOICE")) {
              if (inv.getSsaBoeDatereceipt() != null) {
                lineFV.recepcionDate = OBDateUtils.formatDate(inv.getSsaBoeDatereceipt());
              } else {
                lineFV.recepcionDate = OBDateUtils.formatDate(inv.getInvoiceDate());
              }
            } else {
              lineFV.recepcionDate = OBDateUtils.formatDate(inv.getInvoiceDate());
            }
          }

        }

        lineFV.documentNumber = inv.getScrPhysicalDocumentno();
        if (documentType.equals("LC")) {
          lineFV.documentNumber = lineFV.documentNumber + " ";
          if (inv.isSCOIsProtested())
            lineFV.documentNumber = lineFV.documentNumber + "(P)";

          if (inv.isScoIsboerenewal() != null && inv.isScoIsboerenewal())
            lineFV.documentNumber = lineFV.documentNumber + "(R)";
          else if (inv.isSsaIsboerefinancing() != null && inv.isSsaIsboerefinancing())
            lineFV.documentNumber = lineFV.documentNumber + "(F)";

        }
        lineFV.percepcion = new BigDecimal(0);

        if (documentType.equals("FV") || documentType.equals("LC")) {
          List<InvoiceTax> lsTaxes = inv.getInvoiceTaxList();
          for (int k = 0; k < lsTaxes.size(); k++)
            if (lsTaxes.get(k).getTax().getScoSpecialtax().equals("SCOPERCEPCION")) {
              lineFV.percepcion = lineFV.percepcion.add(lsTaxes.get(k).getTaxAmount());
            }
        }

        lineFV.debit = new BigDecimal(0);
        lineFV.credit = new BigDecimal(0);

        BigDecimal saldo = getSaldoInvoice(inv, startingDate, true, true, true);

        if (saldo.compareTo(BigDecimal.ZERO) == 1) {
          if (!inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOARCREDITMEMO")
              && !inv.getTransactionDocument().getScoSpecialdoctype()
                  .equals("SCOARINVOICERETURNMAT")
              && !inv.getTransactionDocument().getScoSpecialdoctype()
                  .equals("SCOARINVOICEREVERSE")) {
            lineFV.credit = saldo;
          } else {
            lineFV.debit = saldo;
          }
        } else {
          if (!inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOARCREDITMEMO")
              && !inv.getTransactionDocument().getScoSpecialdoctype()
                  .equals("SCOARINVOICERETURNMAT")
              && !inv.getTransactionDocument().getScoSpecialdoctype()
                  .equals("SCOARINVOICEREVERSE")) {
            lineFV.debit = saldo;
          } else {
            lineFV.credit = saldo;
          }
        }

        if (lineFV.credit.compareTo(BigDecimal.ZERO) < 0) {
          BigDecimal tmp = lineFV.credit;
          lineFV.credit = lineFV.debit.negate();
          lineFV.debit = tmp.negate();
        }

        lineFV.balance = lineFV.debit.subtract(lineFV.credit);
        lineFV.estaFacturado = "SI";

        lineFV.daysTillDue = null;
        if (!inv.isPaymentComplete()) {
          lineFV.daysTillDue = inv.getDaysTillDue();
        }

        lineFV.groupDocument = 1;

        lsLines.add(lineFV);

        BigDecimal totalBalance = new BigDecimal(0.00).add(lineFV.balance);

        // Aplicaciones
        // boolean alreadyBOE = false;
        for (int j = 0; j < schedules.size(); j++) {

          List<FIN_PaymentScheduleDetail> lsPsd = schedules.get(j)
              .getFINPaymentScheduleDetailInvoicePaymentScheduleList();
          for (int k = 0; k < lsPsd.size(); k++) {

            if (lsPsd.get(k).isScoExternalpayment() || lsPsd.get(k).getPaymentDetails() != null) {

              FIN_PaymentScheduleDetail detail = lsPsd.get(k);
              if (detail.isScoExternalpayment()) {// pagos
                // especiales y
                // canje de
                // letra
                if (detail.isScoIsboepayment()) {

                  /*
                   * if (alreadyBOE) continue; else alreadyBOE = true;
                   */

                  /*
                   * OBCriteria<SCOBoeFrom> boeFromCriteria = OBDal.getInstance().createCriteria(
                   * SCOBoeFrom.class);
                   * boeFromCriteria.add(Restrictions.eq(SCOBoeFrom.PROPERTY_INVOICEREF, inv));
                   * boeFromCriteria.setFilterOnReadableClients(false);
                   * boeFromCriteria.setFilterOnReadableOrganization(false);
                   */

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
                    if (specialDocType.equals("SCOINVOICEXBOE")) {
                      op = "Canje Letra";
                    } else if (specialDocType.equals("SCOBOEXBOE")) {
                      if (manageBOE.isBoerenewal())
                        op = "Renovación Letra";
                      else
                        op = "Refinanciación Letra";
                    }
                    for (int lll = 0; lll < manageBOE.getSCOBoeToList().size(); lll++) {

                      SCOBoeTo boeTo = manageBOE.getSCOBoeToList().get(lll);

                      if (boeTo.getInvoice().getAccountingDate().compareTo(startingDate) < 0
                          || boeTo.getInvoice().getAccountingDate().compareTo(endingDateEnd) > 0)
                        continue;

                      UnpaidByClientLine lineLA = new UnpaidByClientLine();
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

                      List<InvoiceTax> lsTaxes = boeTo.getInvoice().getInvoiceTaxList();
                      for (int l = 0; l < lsTaxes.size(); l++)
                        if (lsTaxes.get(l).getTax().getScoSpecialtax().equals("SCOPERCEPCION")) {
                          lineLA.percepcion = lineLA.percepcion
                              .add(lsTaxes.get(l).getTaxAmount().negate());
                        }

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
                  UnpaidByClientLine lineAI = new UnpaidByClientLine();
                  lineAI.groupNumber = actualGroup;

                  Date dateAcct = null;
                  String specialdoctype = "", docId = "";
                  if (detail.isScoIswithholdingpayment()) {
                    dateAcct = detail.getSCOSwlrecliPayschedetailList().get(0)
                        .getSCOSwithhoRecLine().getSalesWithholdingReceipt().getAccountingDate();

                    specialdoctype = detail.getSCOSwlrecliPayschedetailList().get(0)
                        .getSCOSwithhoRecLine().getSalesWithholdingReceipt().getDocumentType()
                        .getScoSpecialdoctype();
                    docId = detail.getSCOSwlrecliPayschedetailList().get(0).getSCOSwithhoRecLine()
                        .getSalesWithholdingReceipt().getId();
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
                List<SCOPercepsalesDetail> lsPercepcion = detail.getPaymentDetails()
                    .getSCOPercepsalesDetailList();
                for (int l = 0; l < lsPercepcion.size(); l++) {
                  SCOPercepsalesDetail perc = lsPercepcion.get(l);
                  if ((perc.getSCOPercepSales().getDocumentStatus().equals("CO")
                      || perc.getSCOPercepSales().getDocumentStatus().equals("CL"))
                      && perc.isActive()) {
                    percepcion = percepcion.add(perc.getAmount());
                  }
                }

                if (remainingFromPayment.compareTo(BigDecimal.ZERO) != 0) {// RC
                  // or
                  // CI
                  // LINE
                  UnpaidByClientLine lineRC = new UnpaidByClientLine();
                  lineRC.groupNumber = actualGroup;
                  lineRC.invoicedDate = OBDateUtils
                      .formatDate(detail.getPaymentDetails().getFinPayment().getPaymentDate());
                  lineRC.dueDate = OBDateUtils.formatDate(schedules.get(j).getDueDate());
                  lineRC.currency = inv.getCurrency().getISOCode();
                  lineRC.docType = "RC"; // cobros
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
                    lineRC.docType = "CI"; // cobro

                  lineRC.tipoOp = "Cobro";

                  if (detail.getPaymentDetails().getFinPayment().getSCOSpecialDocType()
                      .equals("SSAAPPPAYMENT")) {

                    String tipo = detail.getPaymentDetails().getFinPayment()
                        .getScoRecvapplicationtype();
                    lineRC.tipoOp = "Aplicación";

                    if (tipo.equals("SSA_ARCMEMO")) {
                      lineRC.tipoOp = "Aplicación Nota Crédito";
                    }

                    try {
                      if (detail.getInvoicePaymentSchedule().getInvoice().getSCOSpecialDocType()
                          .equals("SCOARCREDITMEMO")
                          || detail.getInvoicePaymentSchedule().getInvoice().getSCOSpecialDocType()
                              .equals("SCOARINVOICERETURNMAT")) {
                        lineRC.tipoOp = "Cobro";
                      }
                    } catch (Exception ex) {
                    }
                  }

                  lineRC.paymentMethod = paymentDetail.getFinPayment().getPaymentMethod().getName();

                  totalBalance = totalBalance.add(lineRC.balance);
                  lineRC.balance = new BigDecimal(totalBalance.toString());

                  lineRC.paymentMethod = "";
                  lineRC.daysTillDue = null;

                  lsLines.add(lineRC);

                } else {
                  lsLines.get(lsLines.size() - 1).percepcion = percepcion;// last
                  // line
                  // with
                  // percepcion
                }

                if (writeOffPayment.compareTo(BigDecimal.ZERO) != 0) {// AJUSTE
                  // or
                  // CI
                  // LINE
                  UnpaidByClientLine lineRC = new UnpaidByClientLine();
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
                    lineRC.docType = "CI"; // cobro

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

          if (dd.getPaymentDetails().getFinPayment().getStatus().equals("RPAP")
              || dd.getPaymentDetails().getFinPayment().getStatus().equals("RPAE")) {
            continue;
          }
          Date ddt = dd.getPaymentDetails().getFinPayment().getPaymentDate();

          if (ddt.compareTo(startingDate) < 0 || ddt.compareTo(endingDateEnd) > 0)
            continue;

          UnpaidByClientLine lineRC = new UnpaidByClientLine();
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

  public static BigDecimal getSaldoInvoice(Invoice inv, Date dttDate, boolean includeBoeExternal,
      boolean includePercepcion, boolean onlyForReport) {

    BigDecimal saldo = inv.getScoGrandtotalNoperc();// SunatUtil.getTotalAmountWithoutPerception(inv);

    if (includePercepcion)
      saldo = inv.getGrandTotalAmount();

    if (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOARCREDITMEMO")
        && !inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOARINVOICERETURNMAT")
        && !inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOARINVOICEREVERSE")) {
      saldo = saldo.negate();
    }

    if (!onlyForReport) {
      OBCriteria<SCOPaymentHistory> schedules = OBDal.getInstance()
          .createCriteria(SCOPaymentHistory.class);
      schedules.add(Restrictions.eq(SCOPaymentHistory.PROPERTY_INVOICE, inv));
      schedules.add(Restrictions.lt(SCOPaymentHistory.PROPERTY_PAYMENTDATE, dttDate));

      List<SCOPaymentHistory> scheduleList = schedules.list();

      if (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOARCREDITMEMO")
          || inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOAPCREDITMEMO"))
        saldo = saldo.negate();

      for (int i = 0; i < scheduleList.size(); i++) {
        SCOPaymentHistory detail = scheduleList.get(i);
        saldo = saldo.subtract(detail.getAmount());
        if (includePercepcion)
          saldo = saldo.subtract(detail.getPerceptionamt());
      }

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

        if (onlyForReport && !sdetail.isScoExternalpayment()) {
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

            OBCriteria<SCOSwlrecliPayschedetail> qryMaybe1 = OBDal.getInstance()
                .createCriteria(SCOSwlrecliPayschedetail.class);
            qryMaybe1.add(
                Restrictions.eq(SCOSwlrecliPayschedetail.PROPERTY_PAYMENTSCHEDULEDETAIL, sdetail));

            List<SCOSwlrecliPayschedetail> lsMaybe1 = qryMaybe1.list();
            if (lsMaybe1.size() > 0) {
              dttDetail = lsMaybe1.get(0).getSCOSwithhoRecLine().getSalesWithholdingReceipt()
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

  public static BigDecimal getGeneralSaldoInvoice(ConnectionProvider conProvider, Invoice inv,
      Date dttDate) throws ServletException {

    String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_JAVA);
    String strDttDate = sdf.format(dttDate);

    BigDecimal saldo = inv.getGrandTotalAmount();

    if (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOARCREDITMEMO")
        && !inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOARINVOICERETURNMAT")
        && !inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOARINVOICEREVERSE")) {
      saldo = saldo.negate();
    }

    String strPaidamount = UnpaidByClientData.getInvTotalPaidAmountByDate(conProvider,
        inv.getClient().getId(), inv.getId(), strDttDate);
    if (strPaidamount == null) {
      strPaidamount = "0";
    } else if (strPaidamount.isEmpty()) {
      strPaidamount = "0";
    }
    saldo = saldo.subtract(new BigDecimal(strPaidamount));
    return saldo;
  }

  public static List<PercepReg> getRegistroPercepciones(String strBp, Date startingDate,
      Date endingDate) throws Exception {

    List<PercepReg> lsLines = new ArrayList<PercepReg>();

    BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class, strBp);

    OBCriteria<SCOPercepSales> comprobantes = OBDal.getInstance()
        .createCriteria(SCOPercepSales.class);
    if (bp != null)
      comprobantes.add(Restrictions.eq(SCOPercepSales.PROPERTY_BUSINESSPARTNER, bp));
    if (startingDate != null && endingDate != null)
      comprobantes.add(
          Restrictions.between(SCOPercepSales.PROPERTY_GENERATEDDATE, startingDate, endingDate));
    comprobantes.add(Restrictions.eq(SCOPercepSales.PROPERTY_DOCUMENTSTATUS, "CO"));
    comprobantes.addOrderBy(SCOPercepSales.PROPERTY_GENERATEDDATE, true);

    comprobantes.setFilterOnReadableClients(false);
    comprobantes.setFilterOnReadableOrganization(false);

    List<SCOPercepSales> comprobantesList = comprobantes.list();

    // int currentGroup = 1;
    List<Invoice> lsInvoicesPercep = new ArrayList<Invoice>();
    HashMap<String, Integer> hashInvoices = new HashMap<String, Integer>();
    HashMap<String, List<SCOPercepsalesDetail>> hashPercep = new HashMap<String, List<SCOPercepsalesDetail>>();

    for (int i = 0; i < comprobantesList.size(); i++) {
      SCOPercepSales perc = comprobantesList.get(i);
      List<SCOPercepsalesDetail> details = perc.getSCOPercepsalesDetailList();

      for (int j = 0; j < details.size(); j++) {
        SCOPercepsalesDetail detail = details.get(j);
        Invoice inv = detail.getInvoiceref();

        Invoice invOld = inv;
        while (inv.getTransactionDocument().getScoSpecialdoctype().equals("SCOARBOEINVOICE")) {
          invOld = inv;
          inv = inv.getScoBoeTo().getBillOfExchange().getSCOBoeFromList().get(0).getInvoiceref();
        }

        List<Invoice> lsInvoices = new ArrayList<Invoice>();
        if (invOld.getTransactionDocument().getScoSpecialdoctype().equals("SCOARBOEINVOICE")) {
          List<SCOBoeFrom> lsBoe = invOld.getScoBoeTo().getBillOfExchange().getSCOBoeFromList();
          for (int k = 0; k < lsBoe.size(); k++) {
            Invoice invAux = lsBoe.get(k).getInvoiceref();
            lsInvoices.add(invAux);
          }
        } else {
          lsInvoices.add(invOld);
        }

        for (int k = 0; k < lsInvoices.size(); k++) {
          Invoice invAux = lsInvoices.get(k);
          Integer pos = hashInvoices.get(invAux.getId());
          if (pos == null) {
            lsInvoicesPercep.add(invAux);
            pos = new Integer(lsInvoicesPercep.size() - 1);
            hashInvoices.put(invAux.getId(), pos);
            hashPercep.put(invAux.getId(), new ArrayList<SCOPercepsalesDetail>());
          }

          Invoice invPercep = lsInvoicesPercep.get(pos);
          List<SCOPercepsalesDetail> lsPercepDetail = hashPercep.get(invPercep.getId());
          lsPercepDetail.add(detail);
        }
      }
    }

    int currentGroup = 1;

    // now fill-in
    for (int i = 0; i < lsInvoicesPercep.size(); i++) {

      Invoice inv = lsInvoicesPercep.get(i);
      // invoice
      PercepReg percReg = new PercepReg();
      percReg.transDate = inv.getInvoiceDate();
      percReg.groupNumber = currentGroup;
      percReg.currencySymbol = inv.getCurrency().getSymbol();

      String doctype = inv.getTransactionDocument().getScoSpecialdoctype();
      String comprobante = "99";
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
      percReg.tipoDoc = comprobante;

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

      percReg.serieDoc = serieInvoice;
      percReg.numDoc = nroInvoice;
      percReg.totalDoc = inv.getScoGrandtotalNoperc().setScale(2).toString();
      percReg.tipoTrans = "Venta";
      percReg.debe = percReg.totalDoc;
      percReg.haber = "";
      percReg.saldo = getSaldoInvoice(inv, startingDate, false, false, false).toString();
      percReg.serieComp = "";
      percReg.numComp = "";
      percReg.totalComp = "";
      percReg.partnerUid = inv.getBusinessPartner().getId();
      percReg.partnerCommName = inv.getBusinessPartner().getName();
      percReg.partnerRuc = inv.getBusinessPartner().getTaxID();
      percReg.invo = inv;

      percReg.montoPagoOriginal = inv.getScoGrandtotalNoperc().setScale(2).toString();

      List<PercepReg> temporalList = new ArrayList<PercepReg>();
      temporalList.add(percReg);

      // Comprobantes
      List<SCOPercepsalesDetail> lsPercep = hashPercep.get(inv.getId());
      DalConnectionProvider provider = new DalConnectionProvider();
      for (int j = 0; j < lsPercep.size(); j++) {

        SCOPercepsalesDetail detail = lsPercep.get(j);

        PercepReg percReg2 = new PercepReg();
        if (detail.getPaymentDetails() != null)
          percReg2.transDate = detail.getPaymentDetails().getFinPayment().getPaymentDate();
        else
          percReg2.transDate = detail.getSCOPercepSales().getGeneratedDate();
        percReg2.tipoDoc = comprobante;
        percReg2.serieDoc = serieInvoice;
        percReg2.numDoc = nroInvoice;
        percReg2.totalDoc = "";
        percReg2.tipoTrans = "Cobros";
        percReg2.debe = "";
        percReg2.groupNumber = currentGroup;
        percReg2.currencySymbol = detail.getSCOPercepSales().getCurrency().getSymbol();

        SCOPaymentHistory history = null;
        OBContext.setAdminMode();
        try {
          if (detail.getPaymentDetails() != null)
            history = SunatUtil.findHistory(detail.getPaymentDetails().getSCOPaymentHistoryList(),
                inv.getId());
        } finally {
          OBContext.restorePreviousMode();
        }

        if (history != null && !history.getPerceptionamt().equals(BigDecimal.ZERO)) {

          String montoTotal = AcctServer.getConvertedAmt(
              history.getAmount().setScale(2, RoundingMode.HALF_UP).toString(),
              inv.getCurrency().getId(),
              Utility.stringBaseCurrencyId(provider, inv.getClient().getId()),
              OBDateUtils.formatDate(detail.getSCOPercepSales().getGeneratedDate()), "",
              inv.getClient().getId(), inv.getOrganization().getId(), provider, false);

          String montoPercepcion = AcctServer.getConvertedAmt(
              // AAA: DESDE ACA VIENE EL PROBLEMA NO DE LA CONVERSION DEL MONTO DE PERCEPCION
              history.getPerceptionamt().setScale(2, RoundingMode.HALF_UP).toString(),
              inv.getCurrency().getId(),
              Utility.stringBaseCurrencyId(provider, inv.getClient().getId()),
              OBDateUtils.formatDate(detail.getSCOPercepSales().getGeneratedDate()), "",
              inv.getClient().getId(), inv.getOrganization().getId(), provider, true);

          percReg2.haber = montoTotal;
          percReg2.saldo = "";

          StringTokenizer st2 = new StringTokenizer(
              detail.getSCOPercepSales().getPerceptionNumber(), "-");

          String serieComp = st2.nextToken().replaceAll("\\D+", "");
          String nroComp = st2.nextToken().replaceAll("\\D+", "");

          percReg2.serieComp = serieComp;
          percReg2.numComp = nroComp;
          percReg2.totalComp = montoPercepcion;
          percReg2.partnerUid = inv.getBusinessPartner().getId();
          percReg2.partnerCommName = inv.getBusinessPartner().getName();
          percReg2.partnerRuc = inv.getBusinessPartner().getTaxID();
          percReg2.invo = inv;
          percReg2.montoPagoOriginal = history.getAmount().setScale(2, RoundingMode.HALF_UP)
              .toString();

          // ----------------------------------------------------------------------------------------------------------------------------------------------------
          temporalList.add(percReg2);
        } else {

          String montoTotal = AcctServer.getConvertedAmt(
              detail.getPaymentAmount().setScale(2, RoundingMode.HALF_UP).toString(),
              inv.getCurrency().getId(),
              Utility.stringBaseCurrencyId(provider, inv.getClient().getId()),
              OBDateUtils.formatDate(detail.getSCOPercepSales().getGeneratedDate()), "",
              inv.getClient().getId(), inv.getOrganization().getId(), provider);
          String montoPercepcion = detail.getConvertedAmount().toString();

          percReg2.haber = montoTotal;
          percReg2.saldo = "";

          StringTokenizer st2 = new StringTokenizer(
              detail.getSCOPercepSales().getPerceptionNumber(), "-");

          String serieComp = st2.nextToken().replaceAll("\\D+", "");
          String nroComp = st2.nextToken().replaceAll("\\D+", "");

          percReg2.serieComp = serieComp;
          percReg2.numComp = nroComp;
          percReg2.totalComp = montoPercepcion;
          percReg2.partnerUid = inv.getBusinessPartner().getId();
          percReg2.partnerCommName = inv.getBusinessPartner().getName();
          percReg2.partnerRuc = inv.getBusinessPartner().getTaxID();
          percReg2.invo = inv;
          percReg2.montoPagoOriginal = detail.getPaymentAmount().setScale(2, RoundingMode.HALF_UP)
              .toString();

          temporalList.add(percReg2);
        }
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

}