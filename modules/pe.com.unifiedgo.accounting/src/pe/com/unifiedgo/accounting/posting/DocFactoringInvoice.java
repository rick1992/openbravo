package pe.com.unifiedgo.accounting.posting;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
import org.openbravo.model.common.invoice.Invoice;

import pe.com.unifiedgo.accounting.data.SCOFactinvLine;
import pe.com.unifiedgo.accounting.data.SCOFactoringinvoice;

public class DocFactoringInvoice extends AcctServer {
  private static final long serialVersionUID = 1L;
  static Logger log4jDocFactoringInvoice = Logger.getLogger(AcctServer.class);

  String SeqNo = "0";

  /**
   * Constructor
   * 
   * @param AD_Client_ID
   *          AD_Client_ID
   */
  public DocFactoringInvoice() {

  }

  public DocFactoringInvoice(String AD_Client_ID, String AD_Org_ID,
      ConnectionProvider connectionProvider) {
    super(AD_Client_ID, AD_Org_ID, connectionProvider);
  }

  public void loadObjectFieldProvider(ConnectionProvider conn, String stradClientId, String Id)
      throws ServletException {
    setObjectFieldProvider(DocFactoringInvoiceData.selectRegistro(conn, stradClientId, Id));
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
    DocLineFactoringInvoiceData[] data = null;
    try {
      log4jDocFactoringInvoice.debug("############### groupLines = " + groupLines);
      if (groupLines.equals("Y"))
        data = DocLineFactoringInvoiceData.selectTotal(connectionProvider, Record_ID);
      else
        data = DocLineFactoringInvoiceData.select(connectionProvider, Record_ID);
    } catch (ServletException e) {
      log4jDocFactoringInvoice.warn(e);
    }
    if (data == null || data.length == 0)
      return null;
    for (int i = 0; i < data.length; i++) {
      String Line_ID = data[i].scoFactinvLineId;
      DocLine_FactoringInvoice docLine = new DocLine_FactoringInvoice(DocumentType, Record_ID,
          Line_ID);
      docLine.loadAttributes(data[i], this);
      String amt = data[i].amount;
      docLine.setAmount(amt);
      docLine.setTransactiontype(data[i].transactiontype);
      String docTypeId = data[i].doctyperefId;
      docLine.setDocTypeID(docTypeId);
      String docInvoiceRef = data[i].invoicerefId;
      docLine.setInvoiceRefId(docInvoiceRef);
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

    // Find factinv account for schema
    SCOFactoringinvoice factoringinv = OBDal.getInstance().get(SCOFactoringinvoice.class,
        Record_ID);

    String strDateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    final SimpleDateFormat dateFormat = new SimpleDateFormat(strDateFormat);

    for (DocLine factinvline : p_lines) {

      boolean tc = false;// siempre factinv con T/C compra
      Invoice invoice = OBDal.getInstance().get(Invoice.class,
          ((DocLine_FactoringInvoice) factinvline).getInvoiceRefId());

      String conversionDate = dateFormat.format(invoice.getAccountingDate());

      Calendar aCalendar = Calendar.getInstance();
      aCalendar.setTime(factoringinv.getAccountingDate());
      // add -1 month to current month
      aCalendar.add(Calendar.MONTH, -1);
      // set DATE to 1, so first date of previous month
      aCalendar.set(Calendar.DATE, 1);
      aCalendar.set(Calendar.DATE, aCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));

      Date firstDateOfPreviousMonth = aCalendar.getTime();
      // modified by UGO
      boolean skipreeval = invoice.isScoSkipreeval() != null ? invoice.isScoSkipreeval() : false;
      if (invoice.getAccountingDate().before(firstDateOfPreviousMonth) && !skipreeval) {

        // set actual maximum date of previous month
        aCalendar.set(Calendar.DATE, aCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        // read it
        Date lastDateOfPreviousMonth = aCalendar.getTime();

        conversionDate = dateFormat.format(lastDateOfPreviousMonth);
        tc = false;
      }

      BigDecimal amtFrom = new BigDecimal(
          getConvertedAmt(invoice.getGrandTotalAmount().toString(), invoice.getCurrency().getId(),
              as.m_C_Currency_ID, conversionDate, "", AD_Client_ID, AD_Org_ID, conn, tc));

      BigDecimal tcOut = BigDecimal.ONE;
      if (invoice.getGrandTotalAmount().compareTo(BigDecimal.ZERO) != 0)
        tcOut = amtFrom.divide(invoice.getGrandTotalAmount(), 16, RoundingMode.HALF_UP);

      final StringBuilder whereClause = new StringBuilder();
      whereClause.append(" as cusa ");
      whereClause.append(
          " where cusa.businessPartner.id = '" + invoice.getBusinessPartner().getId() + "'");

      final OBQuery<CustomerAccounts> obqParameters = OBDal.getInstance()
          .createQuery(CustomerAccounts.class, whereClause.toString());
      obqParameters.setFilterOnReadableClients(false);
      obqParameters.setFilterOnReadableOrganization(false);
      final List<CustomerAccounts> customerAccounts = obqParameters.list();

      String acctReceivable = customerAccounts.get(0).getCustomerReceivablesNo().getId();
      String acctFactoringInv = customerAccounts.get(0).getScoFactinvAcct().getId();
      String acctFactCol = customerAccounts.get(0).getScoColfactinvAcct().getId();

      factinvline.m_Record_Id3 = invoice.getId();
      factinvline.m_AD_Table3_Id = "318";
      factinvline.m_C_Project_ID = invoice.getProject() != null ? invoice.getProject().getId() : "";

      String curr = invoice.getCurrency().getId();
      isReceiptForConversion = tc;
      org.openbravo.model.financialmgmt.accounting.coa.AcctSchema accSchema = OBDal.getInstance()
          .get(org.openbravo.model.financialmgmt.accounting.coa.AcctSchema.class,
              as.m_C_AcctSchema_ID);

      FactLine fl = null;

      if (((DocLine_FactoringInvoice) factinvline).getTransactiontype().equals("SCO_TOCOL")) {
        fl = fact.createLine(factinvline, Account.getAccount(conn, acctReceivable), curr, "",
            invoice.getGrandTotalAmount().toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, conversionDate, tcOut, conn);
        if (fl != null) {
          fl.doc_Ad_Org_ID = invoice.getOrganization().getId();
          fl.doc_C_Currency_ID = invoice.getCurrency().getId();
          fl.doc_C_Doctype_ID = invoice.getTransactionDocument().getId();
        }

        fl = fact.createLine(factinvline, Account.getAccount(conn, acctFactCol), curr,
            invoice.getGrandTotalAmount().toString(), "", Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, conversionDate, tcOut, conn);
        if (fl != null) {
          fl.doc_Ad_Org_ID = invoice.getOrganization().getId();
          fl.doc_C_Currency_ID = invoice.getCurrency().getId();
          fl.doc_C_Doctype_ID = invoice.getTransactionDocument().getId();
        }

      } else if (((DocLine_FactoringInvoice) factinvline).getTransactiontype()
          .equals("SCO_TOFACT")) {
        fl = fact.createLine(factinvline, Account.getAccount(conn, acctReceivable), curr, "",
            invoice.getGrandTotalAmount().toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, conversionDate, tcOut, conn);
        if (fl != null) {
          fl.doc_Ad_Org_ID = invoice.getOrganization().getId();
          fl.doc_C_Currency_ID = invoice.getCurrency().getId();
          fl.doc_C_Doctype_ID = invoice.getTransactionDocument().getId();
        }

        fl = fact.createLine(factinvline, Account.getAccount(conn, acctFactoringInv), curr,
            invoice.getGrandTotalAmount().toString(), "", Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, conversionDate, tcOut, conn);
        if (fl != null) {
          fl.doc_Ad_Org_ID = invoice.getOrganization().getId();
          fl.doc_C_Currency_ID = invoice.getCurrency().getId();
          fl.doc_C_Doctype_ID = invoice.getTransactionDocument().getId();
        }

      } else if (((DocLine_FactoringInvoice) factinvline).getTransactiontype().equals("SCO_PRO")) {
        fl = fact.createLine(factinvline, Account.getAccount(conn, acctFactoringInv), curr, "",
            invoice.getGrandTotalAmount().toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, conversionDate, tcOut, conn);
        if (fl != null) {
          fl.doc_Ad_Org_ID = invoice.getOrganization().getId();
          fl.doc_C_Currency_ID = invoice.getCurrency().getId();
          fl.doc_C_Doctype_ID = invoice.getTransactionDocument().getId();
        }

        fl = fact.createLine(factinvline, Account.getAccount(conn, acctReceivable), curr,
            invoice.getGrandTotalAmount().toString(), "", Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, conversionDate, tcOut, conn);
        if (fl != null) {
          fl.doc_Ad_Org_ID = invoice.getOrganization().getId();
          fl.doc_C_Currency_ID = invoice.getCurrency().getId();
          fl.doc_C_Doctype_ID = invoice.getTransactionDocument().getId();
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
    log4jDocFactoringInvoice.debug("DocFactoringInvoice - oldSeqNo = " + oldSeqNo);
    BigDecimal seqNo = new BigDecimal(oldSeqNo);
    SeqNo = (seqNo.add(new BigDecimal("10"))).toString();
    log4jDocFactoringInvoice.debug("DocFactoringInvoice - nextSeqNo = " + SeqNo);
    return SeqNo;
  }

  /**
   * @return the log4jDocFactoringInvoice
   */
  public static Logger getlog4jDocFactoringInvoice() {
    return log4jDocFactoringInvoice;
  }

  /**
   * @param log4jDocFactoringInvoice
   *          the log4jDocFactoringInvoice to set
   */
  public static void setlog4jDocFactoringInvoice(Logger log4jDocFactoringInvoice) {
    DocFactoringInvoice.log4jDocFactoringInvoice = log4jDocFactoringInvoice;
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
    SCOFactoringinvoice factoringinv = OBDal.getInstance().get(SCOFactoringinvoice.class,
        strRecordId);
    boolean zero = true;
    for (SCOFactinvLine factinvline : factoringinv.getSCOFactinvLineList()) {
      if (ZERO.compareTo(factinvline.getInvoiceref().getGrandTotalAmount()) != 0) {
        zero = false;
      } else {
        zero = true;
        break;
      }
    }
    if (zero) {
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
