package pe.com.unifiedgo.accounting.posting;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.ad_forms.Account;
import org.openbravo.erpCommon.ad_forms.AcctSchema;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.ad_forms.DocLine;
import org.openbravo.erpCommon.ad_forms.Fact;
import org.openbravo.erpCommon.ad_forms.FactLine;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.model.common.businesspartner.CustomerAccounts;
import org.openbravo.model.common.businesspartner.VendorAccounts;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.gl.GLItem;

import pe.com.unifiedgo.accounting.data.SCOBillofexchange;
import pe.com.unifiedgo.accounting.data.SCOPrepayment;

public class DocBillofExchange extends AcctServer {
  private static final long serialVersionUID = 1L;
  static Logger log4jDocInvoice = Logger.getLogger(AcctServer.class);

  String SeqNo = "0";

  /**
   * Constructor
   * 
   * @param AD_Client_ID
   *          AD_Client_ID
   */
  public DocBillofExchange() {

  }

  public DocBillofExchange(String AD_Client_ID, String AD_Org_ID,
      ConnectionProvider connectionProvider) {
    super(AD_Client_ID, AD_Org_ID, connectionProvider);
  }

  public void loadObjectFieldProvider(ConnectionProvider conn, String stradClientId, String Id)
      throws ServletException {
    setObjectFieldProvider(DocBillofExchangeData.selectRegistro(conn, stradClientId, Id));
  }

  public boolean loadDocumentDetails(FieldProvider[] data, ConnectionProvider conn) {
    DateDoc = data[0].getField("DATEACCT");

    // Amounts
    Amounts[AMTTYPE_Gross] = "0";
    Amounts[AMTTYPE_Net] = "0";
    Amounts[AMTTYPE_Charge] = "0";
    loadDocumentType(); // lines require doc type
    // Contained Objects
    p_lines = loadLines();

    return true;

  }

  private DocLine[] loadLines() {

    ArrayList<Object> list = new ArrayList<Object>();
    DocLineBillofExchangeData[] data = null;
    try {
      log4jDocInvoice.debug("############### groupLines = " + groupLines);
      if (groupLines.equals("Y"))
        data = DocLineBillofExchangeData.selectTotal(connectionProvider, Record_ID);
      else
        data = DocLineBillofExchangeData.select(connectionProvider, Record_ID);
    } catch (ServletException e) {
      log4jDocInvoice.warn(e);
    }
    if (data == null || data.length == 0)
      return null;
    for (int i = 0; i < data.length; i++) {
      String Line_ID = data[i].boelineid;
      DocLine_BillofExchange docLine = new DocLine_BillofExchange(DocumentType, Record_ID, Line_ID);
      docLine.loadAttributes(data[i], this);
      String amt = data[i].amount;
      docLine.setAmount(amt);

      String docInvoiceRef = data[i].invoicerefId;
      docLine.setInvoiceRefId(docInvoiceRef);

      String docScoPrepayment = data[i].scoPrepaymentId;
      docLine.setScoPrepaymentId(docScoPrepayment);

      String isFrom = data[i].isfrom;
      docLine.setIsFrom(isFrom);
      list.add(docLine);
    }
    // Return Array
    DocLine[] dl = new DocLine[list.size()];
    list.toArray(dl);

    return dl;
  } // loadLines

  /**
   * Create Facts (the accounting logic)
   * 
   * @param as
   *          accounting schema
   * @return Fact
   */
  public Fact createFact(AcctSchema as, ConnectionProvider conn, Connection con,
      VariablesSecureApp vars) throws ServletException {

    // create Fact Header
    Fact fact = new Fact(this, as, Fact.POST_Actual);
    String Fact_Acct_Group_ID = SequenceIdData.getUUID();

    // Cash based accounting
    if (!as.isAccrual())
      return null;

    // Find boe account for schema

    SCOBillofexchange exchange = OBDal.getInstance().get(SCOBillofexchange.class, Record_ID);

    if (!exchange.getDocumentStatus().equals("CO"))
      return null;

    String strDateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    final SimpleDateFormat dateFormat = new SimpleDateFormat(strDateFormat);

    String specialdoctype = exchange.getTransactionDocument().getScoSpecialdoctype() != null
        ? exchange.getTransactionDocument().getScoSpecialdoctype() : "";

    if (specialdoctype.equals("SCOINVOICEXBOE") || specialdoctype.equals("SCOBOEXBOE")) {

      // siempre asumimos que se canjea a T/C compra
      isReceiptForConversion = false;

      for (DocLine boeline : p_lines) {

        if (((DocLine_BillofExchange) boeline).getInvoiceRefId() == null) {
          continue;
        }

        if (((DocLine_BillofExchange) boeline).getInvoiceRefId().isEmpty()) {
          continue;
        }

        Invoice invoice = OBDal.getInstance().get(Invoice.class,
            ((DocLine_BillofExchange) boeline).getInvoiceRefId());

        final StringBuilder whereClause = new StringBuilder();
        whereClause.append(" as cusa ");
        whereClause.append(
            " where cusa.businessPartner.id = '" + invoice.getBusinessPartner().getId() + "'");
        whereClause.append(" and cusa.accountingSchema.id = '" + as.m_C_AcctSchema_ID + "'");
        whereClause.append(" and (cusa.status is null or cusa.status = 'DE')");

        final OBQuery<CustomerAccounts> obqParameters = OBDal.getInstance()
            .createQuery(CustomerAccounts.class, whereClause.toString());
        obqParameters.setFilterOnReadableClients(false);
        obqParameters.setFilterOnReadableOrganization(false);
        final List<CustomerAccounts> customerAccounts = obqParameters.list();

        String acctCartera = customerAccounts.get(0).getSCOBillOfExchangeAcc().getId();
        String acctDscto = customerAccounts.get(0).getSCODiscountBillOfExchangeAcc().getId();
        String acctCobranza = customerAccounts.get(0).getScoLetracolAcct().getId();

        boeline.m_Record_Id3 = invoice.getId();
        boeline.m_AD_Table3_Id = "318";
        boeline.m_C_Project_ID = invoice.getProject() != null ? invoice.getProject().getId() : "";
        GLItem itemDiverse = invoice.getScoDiverseAccGlitem();

        Account invoiceAcct = (itemDiverse == null)
            ? getAccountBPartner(invoice.getBusinessPartner().getId(), as, true, false, conn)
            : getAccountGLItem(itemDiverse, as, true, conn);
        String invoiceAcctId = invoiceAcct.C_ValidCombination_ID;

        FactLine fl = null;

        if (((DocLine_BillofExchange) boeline).getIsFrom().equals("N")) {

          // si es letra se crear automaticamente al tipo de cambio de compra (tipo de cambio
          // vigente
          // de factura si se crea al mismo tiempo)

          if (specialdoctype.equals("SCOINVOICEXBOE")) {
            fl = fact.createLine(boeline, Account.getAccount(conn, acctCartera),
                invoice.getCurrency().getId(), boeline.getAmount().toString(), "",
                Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
            if (fl != null) {
              fl.doc_Ad_Org_ID = invoice.getOrganization().getId();
              fl.doc_C_Currency_ID = invoice.getCurrency().getId();
              fl.doc_C_Doctype_ID = invoice.getTransactionDocument().getId();
            }
          } else {

            if (exchange.getSCOBoeFromList().get(0).getInvoiceref().getScoBoeType()
                .equals("SCO_DIS")) {
              fl = fact.createLine(boeline, Account.getAccount(conn, acctDscto),
                  invoice.getCurrency().getId(), boeline.getAmount().toString(), "",
                  Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
              if (fl != null) {
                fl.doc_Ad_Org_ID = invoice.getOrganization().getId();
                fl.doc_C_Currency_ID = invoice.getCurrency().getId();
                fl.doc_C_Doctype_ID = invoice.getTransactionDocument().getId();
              }
            } else if (exchange.getSCOBoeFromList().get(0).getInvoiceref().getScoBoeType()
                .equals("SCO_COL")) {
              fl = fact.createLine(boeline, Account.getAccount(conn, acctCobranza),
                  invoice.getCurrency().getId(), boeline.getAmount().toString(), "",
                  Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
              if (fl != null) {
                fl.doc_Ad_Org_ID = invoice.getOrganization().getId();
                fl.doc_C_Currency_ID = invoice.getCurrency().getId();
                fl.doc_C_Doctype_ID = invoice.getTransactionDocument().getId();
              }
            } else {
              fl = fact.createLine(boeline, Account.getAccount(conn, acctCartera),
                  invoice.getCurrency().getId(), boeline.getAmount().toString(), "",
                  Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
              if (fl != null) {
                fl.doc_Ad_Org_ID = invoice.getOrganization().getId();
                fl.doc_C_Currency_ID = invoice.getCurrency().getId();
                fl.doc_C_Doctype_ID = invoice.getTransactionDocument().getId();
              }
            }

          }

        } else {

          boolean tc = AcctServer.getTCenVigencia(invoice, DateAcct); // siempre T/C compra

          Vector<BigDecimal> vOutTC = new Vector<BigDecimal>();
          BigDecimal bpAmountConverted = convertAmount(new BigDecimal(boeline.getAmount()), false,
              DateAcct, TABLEID_Invoice, invoice.getId(), invoice.getCurrency().getId(),
              as.m_C_Currency_ID, boeline, as, fact, Fact_Acct_Group_ID, nextSeqNo(SeqNo), conn,
              true, tc, false, vOutTC);
          bpAmountConverted = new BigDecimal(boeline.getAmount());

          // this.isReceiptForConversion = tc;

          if (specialdoctype.equals("SCOINVOICEXBOE")) {
            fl = fact.createLine(boeline, Account.getAccount(conn, invoiceAcctId),
                invoice.getCurrency().getId(), "", bpAmountConverted.toString(), Fact_Acct_Group_ID,
                nextSeqNo(SeqNo), DocumentType, DateAcct, vOutTC.get(0), conn);
            if (fl != null) {
              fl.doc_Ad_Org_ID = invoice.getOrganization().getId();
              fl.doc_C_Currency_ID = invoice.getCurrency().getId();
              fl.doc_C_Doctype_ID = invoice.getTransactionDocument().getId();
            }
          } else {

            if (exchange.getSCOBoeFromList().get(0).getInvoiceref().getScoBoeType()
                .equals("SCO_DIS")) {
              fl = fact.createLine(boeline, Account.getAccount(conn, acctDscto),
                  invoice.getCurrency().getId(), "", bpAmountConverted.toString(),
                  Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, DateAcct, vOutTC.get(0),
                  conn);
              if (fl != null) {
                fl.doc_Ad_Org_ID = invoice.getOrganization().getId();
                fl.doc_C_Currency_ID = invoice.getCurrency().getId();
                fl.doc_C_Doctype_ID = invoice.getTransactionDocument().getId();
              }

            } else if (exchange.getSCOBoeFromList().get(0).getInvoiceref().getScoBoeType()
                .equals("SCO_COL")) {
              fl = fact.createLine(boeline, Account.getAccount(conn, acctCobranza),
                  invoice.getCurrency().getId(), "", bpAmountConverted.toString(),
                  Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, DateAcct, vOutTC.get(0),
                  conn);
              if (fl != null) {
                fl.doc_Ad_Org_ID = invoice.getOrganization().getId();
                fl.doc_C_Currency_ID = invoice.getCurrency().getId();
                fl.doc_C_Doctype_ID = invoice.getTransactionDocument().getId();
              }
            } else {
              fl = fact.createLine(boeline, Account.getAccount(conn, acctCartera),
                  invoice.getCurrency().getId(), "", bpAmountConverted.toString(),
                  Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, DateAcct, vOutTC.get(0),
                  conn);
              if (fl != null) {
                fl.doc_Ad_Org_ID = invoice.getOrganization().getId();
                fl.doc_C_Currency_ID = invoice.getCurrency().getId();
                fl.doc_C_Doctype_ID = invoice.getTransactionDocument().getId();
              }
            }

          }
        }
      }

      // FOR PREPAYMENT
      for (DocLine boeline : p_lines) {

        if (((DocLine_BillofExchange) boeline).getScoPrepaymentId() == null) {
          continue;
        }

        if (((DocLine_BillofExchange) boeline).getScoPrepaymentId().isEmpty()) {
          continue;
        }

        SCOPrepayment invoice = OBDal.getInstance().get(SCOPrepayment.class,
            ((DocLine_BillofExchange) boeline).getScoPrepaymentId());

        final StringBuilder whereClause = new StringBuilder();
        whereClause.append(" as cusa ");
        whereClause.append(
            " where cusa.businessPartner.id = '" + invoice.getBusinessPartner().getId() + "'");
        whereClause.append(" and cusa.accountingSchema.id = '" + as.m_C_AcctSchema_ID + "'");
        whereClause.append(" and (cusa.status is null or cusa.status = 'DE')");

        final OBQuery<CustomerAccounts> obqParameters = OBDal.getInstance()
            .createQuery(CustomerAccounts.class, whereClause.toString());
        obqParameters.setFilterOnReadableClients(false);
        obqParameters.setFilterOnReadableOrganization(false);
        final List<CustomerAccounts> customerAccounts = obqParameters.list();

        String acctCartera = customerAccounts.get(0).getSCOBillOfExchangeAcc().getId();
        String acctDscto = customerAccounts.get(0).getSCODiscountBillOfExchangeAcc().getId();
        String acctCobranza = customerAccounts.get(0).getScoLetracolAcct().getId();

        boeline.m_Record_Id3 = invoice.getId();
        boeline.m_AD_Table3_Id = "135FAE2571DB4028A90D5CAA6FAC154C";
        boeline.m_C_Project_ID = invoice.getProject() != null ? invoice.getProject().getId() : "";
        GLItem itemDiverse = invoice.getPaymentglitem();

        Account invoiceAcct = (itemDiverse == null)
            ? getAccountBPartner(invoice.getBusinessPartner().getId(), as, true, false, conn)
            : getAccountGLItem(itemDiverse, as, true, conn);
        String invoiceAcctId = invoiceAcct.C_ValidCombination_ID;

        FactLine fl = null;

        if (((DocLine_BillofExchange) boeline).getIsFrom().equals("N")) {

          // si es letra se crear automaticamente al tipo de cambio de compra (tipo de cambio
          // vigente
          // de factura si se crea al mismo tiempo)

          if (specialdoctype.equals("SCOINVOICEXBOE")) {
            fl = fact.createLine(boeline, Account.getAccount(conn, acctCartera),
                invoice.getCurrency().getId(), boeline.getAmount().toString(), "",
                Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
            if (fl != null) {
              fl.doc_Ad_Org_ID = invoice.getOrganization().getId();
              fl.doc_C_Currency_ID = invoice.getCurrency().getId();
              fl.doc_C_Doctype_ID = invoice.getTransactionDocument().getId();
            }
          } else {

            if (exchange.getSCOBoeFromList().get(0).getInvoiceref().getScoBoeType()
                .equals("SCO_DIS")) {
              fl = fact.createLine(boeline, Account.getAccount(conn, acctDscto),
                  invoice.getCurrency().getId(), boeline.getAmount().toString(), "",
                  Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
              if (fl != null) {
                fl.doc_Ad_Org_ID = invoice.getOrganization().getId();
                fl.doc_C_Currency_ID = invoice.getCurrency().getId();
                fl.doc_C_Doctype_ID = invoice.getTransactionDocument().getId();
              }
            } else if (exchange.getSCOBoeFromList().get(0).getInvoiceref().getScoBoeType()
                .equals("SCO_COL")) {
              fl = fact.createLine(boeline, Account.getAccount(conn, acctCobranza),
                  invoice.getCurrency().getId(), boeline.getAmount().toString(), "",
                  Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
              if (fl != null) {
                fl.doc_Ad_Org_ID = invoice.getOrganization().getId();
                fl.doc_C_Currency_ID = invoice.getCurrency().getId();
                fl.doc_C_Doctype_ID = invoice.getTransactionDocument().getId();
              }
            } else {
              fl = fact.createLine(boeline, Account.getAccount(conn, acctCartera),
                  invoice.getCurrency().getId(), boeline.getAmount().toString(), "",
                  Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
              if (fl != null) {
                fl.doc_Ad_Org_ID = invoice.getOrganization().getId();
                fl.doc_C_Currency_ID = invoice.getCurrency().getId();
                fl.doc_C_Doctype_ID = invoice.getTransactionDocument().getId();
              }
            }

          }

        } else {

          String invspecialdoctype = invoice.getTransactionDocument().getScoSpecialdoctype() != null
              ? invoice.getTransactionDocument().getScoSpecialdoctype() : "";

          boolean tc = invoice.isSalesTransaction() ? false : true; // siempre T/C compra

          Vector<BigDecimal> vOutTC = new Vector<BigDecimal>();
          BigDecimal bpAmountConverted = convertAmount(new BigDecimal(boeline.getAmount()), false,
              DateAcct, TABLEID_Billofexchange, exchange.getId(), invoice.getCurrency().getId(),
              as.m_C_Currency_ID, boeline, as, fact, Fact_Acct_Group_ID, nextSeqNo(SeqNo), conn,
              true, tc, false, vOutTC);
          bpAmountConverted = new BigDecimal(boeline.getAmount());

          // this.isReceiptForConversion = tc;

          if (specialdoctype.equals("SCOINVOICEXBOE")
              || invspecialdoctype.equals("SCOPREPAYMENT")) {
            fl = fact.createLine(boeline, Account.getAccount(conn, invoiceAcctId),
                invoice.getCurrency().getId(), "", bpAmountConverted.toString(), Fact_Acct_Group_ID,
                nextSeqNo(SeqNo), DocumentType, DateAcct, vOutTC.get(0), conn);
            if (fl != null) {
              fl.doc_Ad_Org_ID = invoice.getOrganization().getId();
              fl.doc_C_Currency_ID = invoice.getCurrency().getId();
              fl.doc_C_Doctype_ID = invoice.getTransactionDocument().getId();
            }
          } else {

            if (exchange.getSCOBoeFromList().get(0).getInvoiceref().getScoBoeType()
                .equals("SCO_DIS")) {
              fl = fact.createLine(boeline, Account.getAccount(conn, acctDscto),
                  invoice.getCurrency().getId(), "", bpAmountConverted.toString(),
                  Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, DateAcct, vOutTC.get(0),
                  conn);
              if (fl != null) {
                fl.doc_Ad_Org_ID = invoice.getOrganization().getId();
                fl.doc_C_Currency_ID = invoice.getCurrency().getId();
                fl.doc_C_Doctype_ID = invoice.getTransactionDocument().getId();
              }

            } else if (exchange.getSCOBoeFromList().get(0).getInvoiceref().getScoBoeType()
                .equals("SCO_COL")) {
              fl = fact.createLine(boeline, Account.getAccount(conn, acctCobranza),
                  invoice.getCurrency().getId(), "", bpAmountConverted.toString(),
                  Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, DateAcct, vOutTC.get(0),
                  conn);
              if (fl != null) {
                fl.doc_Ad_Org_ID = invoice.getOrganization().getId();
                fl.doc_C_Currency_ID = invoice.getCurrency().getId();
                fl.doc_C_Doctype_ID = invoice.getTransactionDocument().getId();
              }
            } else {
              fl = fact.createLine(boeline, Account.getAccount(conn, acctCartera),
                  invoice.getCurrency().getId(), "", bpAmountConverted.toString(),
                  Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, DateAcct, vOutTC.get(0),
                  conn);
              if (fl != null) {
                fl.doc_Ad_Org_ID = invoice.getOrganization().getId();
                fl.doc_C_Currency_ID = invoice.getCurrency().getId();
                fl.doc_C_Doctype_ID = invoice.getTransactionDocument().getId();
              }
            }

          }
        }
      }
    } else if (specialdoctype.equals("SCOAPINVOICEXBOE")) {

      // siempre asumimos que se canjea a T/C venta
      isReceiptForConversion = true;

      for (DocLine boeline : p_lines) {

        if (((DocLine_BillofExchange) boeline).getInvoiceRefId() == null) {
          continue;
        }

        if (((DocLine_BillofExchange) boeline).getInvoiceRefId().isEmpty()) {
          continue;
        }

        Invoice invoice = OBDal.getInstance().get(Invoice.class,
            ((DocLine_BillofExchange) boeline).getInvoiceRefId());

        final StringBuilder whereClause = new StringBuilder();
        whereClause.append(" as vena ");
        whereClause.append(
            " where vena.businessPartner.id = '" + invoice.getBusinessPartner().getId() + "'");
        whereClause.append(" and vena.accountingSchema.id = '" + as.m_C_AcctSchema_ID + "'");
        whereClause.append(" and (vena.status is null or vena.status = 'DE')");

        final OBQuery<VendorAccounts> obqParameters = OBDal.getInstance()
            .createQuery(VendorAccounts.class, whereClause.toString());
        obqParameters.setFilterOnReadableClients(false);
        obqParameters.setFilterOnReadableOrganization(false);
        final List<VendorAccounts> vendorAccounts = obqParameters.list();

        String acctCartera = vendorAccounts.get(0).getScoPoletraAcct().getId();

        boeline.m_Record_Id3 = invoice.getId();
        boeline.m_AD_Table3_Id = "318";
        boeline.m_C_Project_ID = invoice.getProject() != null ? invoice.getProject().getId() : "";
        GLItem itemDiverse = invoice.getScoDiverseAccGlitem();

        Account invoiceAcct = (itemDiverse == null)
            ? getAccountBPartner(invoice.getBusinessPartner().getId(), as, false, false, conn)
            : getAccountGLItem(itemDiverse, as, true, conn);
        String invoiceAcctId = invoiceAcct.C_ValidCombination_ID;

        FactLine fl = null;

        if (((DocLine_BillofExchange) boeline).getIsFrom().equals("N")) {

          fl = fact.createLine(boeline, Account.getAccount(conn, acctCartera),
              invoice.getCurrency().getId(), "", boeline.getAmount().toString(), Fact_Acct_Group_ID,
              nextSeqNo(SeqNo), DocumentType, conn);
          if (fl != null) {
            fl.doc_Ad_Org_ID = invoice.getOrganization().getId();
            fl.doc_C_Currency_ID = invoice.getCurrency().getId();
            fl.doc_C_Doctype_ID = invoice.getTransactionDocument().getId();
          }

        } else {

          String invspecialdoctype = invoice.getTransactionDocument().getScoSpecialdoctype() != null
              ? invoice.getTransactionDocument().getScoSpecialdoctype() : "";

          boolean tc = AcctServer.getTCenVigencia(invoice, DateAcct); // siempre T/C venta

          Vector<BigDecimal> vOutTC = new Vector<BigDecimal>();
          BigDecimal bpAmountConverted = convertAmount(new BigDecimal(boeline.getAmount()), true,
              DateAcct, TABLEID_Invoice, invoice.getId(), invoice.getCurrency().getId(),
              as.m_C_Currency_ID, boeline, as, fact, Fact_Acct_Group_ID, nextSeqNo(SeqNo), conn,
              true, tc, true, vOutTC);
          bpAmountConverted = new BigDecimal(boeline.getAmount());

          if (invspecialdoctype.equals("SCOAPINVOICE")
              || invspecialdoctype.equals("SCOAPSIMPLEPROVISIONINVOICE")) {
            fl = fact.createLine(boeline, Account.getAccount(conn, invoiceAcctId),
                invoice.getCurrency().getId(), bpAmountConverted.toString(), "", Fact_Acct_Group_ID,
                nextSeqNo(SeqNo), DocumentType, DateAcct, vOutTC.get(0), conn);
            if (fl != null) {
              fl.doc_Ad_Org_ID = invoice.getOrganization().getId();
              fl.doc_C_Currency_ID = invoice.getCurrency().getId();
              fl.doc_C_Doctype_ID = invoice.getTransactionDocument().getId();
            }
          } else if (invspecialdoctype.equals("SCOAPBOEINVOICE")) {
            fl = fact.createLine(boeline, Account.getAccount(conn, acctCartera),
                invoice.getCurrency().getId(), bpAmountConverted.toString(), "", Fact_Acct_Group_ID,
                nextSeqNo(SeqNo), DocumentType, DateAcct, vOutTC.get(0), conn);
            if (fl != null) {
              fl.doc_Ad_Org_ID = invoice.getOrganization().getId();
              fl.doc_C_Currency_ID = invoice.getCurrency().getId();
              fl.doc_C_Doctype_ID = invoice.getTransactionDocument().getId();
            }
          }
        }
      }

      // FOR PREPAYMENT
      for (DocLine boeline : p_lines) {

        if (((DocLine_BillofExchange) boeline).getScoPrepaymentId() == null) {
          continue;
        }

        if (((DocLine_BillofExchange) boeline).getScoPrepaymentId().isEmpty()) {
          continue;
        }

        SCOPrepayment invoice = OBDal.getInstance().get(SCOPrepayment.class,
            ((DocLine_BillofExchange) boeline).getScoPrepaymentId());

        final StringBuilder whereClause = new StringBuilder();
        whereClause.append(" as vena ");
        whereClause.append(
            " where vena.businessPartner.id = '" + invoice.getBusinessPartner().getId() + "'");
        whereClause.append(" and vena.accountingSchema.id = '" + as.m_C_AcctSchema_ID + "'");
        whereClause.append(" and (vena.status is null or vena.status = 'DE')");

        final OBQuery<VendorAccounts> obqParameters = OBDal.getInstance()
            .createQuery(VendorAccounts.class, whereClause.toString());
        obqParameters.setFilterOnReadableClients(false);
        obqParameters.setFilterOnReadableOrganization(false);
        final List<VendorAccounts> vendorAccounts = obqParameters.list();

        String acctCartera = vendorAccounts.get(0).getScoPoletraAcct().getId();

        boeline.m_Record_Id3 = invoice.getId();
        boeline.m_AD_Table3_Id = "135FAE2571DB4028A90D5CAA6FAC154C";
        boeline.m_C_Project_ID = invoice.getProject() != null ? invoice.getProject().getId() : "";
        GLItem itemDiverse = invoice.getPaymentglitem();

        Account invoiceAcct = (itemDiverse == null)
            ? getAccountBPartner(invoice.getBusinessPartner().getId(), as, false, false, conn)
            : getAccountGLItem(itemDiverse, as, true, conn);
        String invoiceAcctId = invoiceAcct.C_ValidCombination_ID;

        FactLine fl = null;

        if (((DocLine_BillofExchange) boeline).getIsFrom().equals("N")) {

          fl = fact.createLine(boeline, Account.getAccount(conn, acctCartera),
              invoice.getCurrency().getId(), "", boeline.getAmount().toString(), Fact_Acct_Group_ID,
              nextSeqNo(SeqNo), DocumentType, conn);
          if (fl != null) {
            fl.doc_Ad_Org_ID = invoice.getOrganization().getId();
            fl.doc_C_Currency_ID = invoice.getCurrency().getId();
            fl.doc_C_Doctype_ID = invoice.getTransactionDocument().getId();
          }

        } else {

          String invspecialdoctype = invoice.getTransactionDocument().getScoSpecialdoctype() != null
              ? invoice.getTransactionDocument().getScoSpecialdoctype() : "";

          boolean tc = invoice.isSalesTransaction() ? false : true; // siempre T/C venta

          Vector<BigDecimal> vOutTC = new Vector<BigDecimal>();
          BigDecimal bpAmountConverted = convertAmount(new BigDecimal(boeline.getAmount()), true,
              DateAcct, TABLEID_Billofexchange, exchange.getId(), invoice.getCurrency().getId(),
              as.m_C_Currency_ID, boeline, as, fact, Fact_Acct_Group_ID, nextSeqNo(SeqNo), conn,
              true, tc, true, vOutTC);
          bpAmountConverted = new BigDecimal(boeline.getAmount());

          if (invspecialdoctype.equals("SCOAPINVOICE")
              || invspecialdoctype.equals("SCOAPSIMPLEPROVISIONINVOICE")
              || invspecialdoctype.equals("SCOPREPAYMENT")) {
            fl = fact.createLine(boeline, Account.getAccount(conn, invoiceAcctId),
                invoice.getCurrency().getId(), bpAmountConverted.toString(), "", Fact_Acct_Group_ID,
                nextSeqNo(SeqNo), DocumentType, DateAcct, vOutTC.get(0), conn);
            if (fl != null) {
              fl.doc_Ad_Org_ID = invoice.getOrganization().getId();
              fl.doc_C_Currency_ID = invoice.getCurrency().getId();
              fl.doc_C_Doctype_ID = invoice.getTransactionDocument().getId();
            }
          } else if (invspecialdoctype.equals("SCOAPBOEINVOICE")) {
            fl = fact.createLine(boeline, Account.getAccount(conn, acctCartera),
                invoice.getCurrency().getId(), bpAmountConverted.toString(), "", Fact_Acct_Group_ID,
                nextSeqNo(SeqNo), DocumentType, DateAcct, vOutTC.get(0), conn);
            if (fl != null) {
              fl.doc_Ad_Org_ID = invoice.getOrganization().getId();
              fl.doc_C_Currency_ID = invoice.getCurrency().getId();
              fl.doc_C_Doctype_ID = invoice.getTransactionDocument().getId();
            }
          }
        }
      }
    }

    SeqNo = "0";
    return fact;
  }// createFact

  /**
   * Get Source Currency Balance - subtracts line and tax amounts from total - no rounding
   * 
   * @return positive amount, if total invoice is bigger than lines
   */
  public BigDecimal getBalance() {
    // BigDecimal ZERO = new BigDecimal("0");
    BigDecimal retValue = ZERO;
    return retValue;
  } // getBalance

  public String nextSeqNo(String oldSeqNo) {
    log4jDocInvoice.debug("DocAccountability - oldSeqNo = " + oldSeqNo);
    BigDecimal seqNo = new BigDecimal(oldSeqNo);
    SeqNo = (seqNo.add(new BigDecimal("10"))).toString();
    log4jDocInvoice.debug("DocAccountability - nextSeqNo = " + SeqNo);
    return SeqNo;
  }

  /**
   * @return the log4jDocInvoice
   */
  public static Logger getLog4jDocInvoice() {
    return log4jDocInvoice;
  }

  /**
   * @param log4jDocInvoice
   *          the log4jDocInvoice to set
   */
  public static void setLog4jDocInvoice(Logger log4jDocInvoice) {
    DocBoe.log4jDocInvoice = log4jDocInvoice;
  }

  /**
   * @return the seqNo
   */
  public String getSeqNo() {
    return SeqNo;
  }

  /**
   * @param seqNo
   *          the seqNo to set
   */
  public void setSeqNo(String seqNo) {
    SeqNo = seqNo;
  }

  /**
   * @return the serialVersionUID
   */
  public static long getSerialVersionUID() {
    return serialVersionUID;
  }

  /**
   * Get Document Confirmation
   * 
   */
  public boolean getDocumentConfirmation(ConnectionProvider conn, String strRecordId) {
    FieldProvider dataFP[] = getObjectFieldProvider();
    SCOBillofexchange invoice = OBDal.getInstance().get(SCOBillofexchange.class, strRecordId);
    boolean zero = false;
    /*
     * for (SCOBoetodiscLine invoiceline : invoice.getSCOBoetodiscLineList()) { if
     * (ZERO.compareTo(invoiceline.getInvoiceref().getGrandTotalAmount()) != 0) { zero = false; }
     * else { zero = true; break; } }
     */
    if (!invoice.getDocumentStatus().equals("CO")) {
      strMessage = "@TotalGrossIsZero@";
      setStatus(STATUS_DocumentDisabled);
      return false;
    }

    return true;
  }

  public String getServletInfo() {
    return "Servlet for the accounting";
  } // end of getServletInfo() method

}
