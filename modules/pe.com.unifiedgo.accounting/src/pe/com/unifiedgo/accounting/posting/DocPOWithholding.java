package pe.com.unifiedgo.accounting.posting;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBDao;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.ad_forms.Account;
import org.openbravo.erpCommon.ad_forms.AcctSchema;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.ad_forms.DocLine;
import org.openbravo.erpCommon.ad_forms.Fact;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchemaDefault;
import org.openbravo.model.financialmgmt.gl.GLItem;

import pe.com.unifiedgo.accounting.data.SCOPwithhoRecLine;
import pe.com.unifiedgo.accounting.data.SCOPwithholdingReceipt;

public class DocPOWithholding extends AcctServer {
  private static final long serialVersionUID = 1L;
  static Logger log4jDocInvoice = Logger.getLogger(AcctServer.class);

  String SeqNo = "0";

  /**
   * Constructor
   * 
   * @param AD_Client_ID
   *          AD_Client_ID
   */
  public DocPOWithholding() {

  }

  public DocPOWithholding(String AD_Client_ID, String AD_Org_ID,
      ConnectionProvider connectionProvider) {
    super(AD_Client_ID, AD_Org_ID, connectionProvider);
  }

  public void loadObjectFieldProvider(ConnectionProvider conn, String stradClientId, String Id)
      throws ServletException {
    setObjectFieldProvider(DocPOWithholdingData.selectRegistro(conn, stradClientId, Id));
  }

  public boolean loadDocumentDetails(FieldProvider[] data, ConnectionProvider conn) {
    DateDoc = data[0].getField("DATEACCT");

    // Amounts
    Amounts[AMTTYPE_Gross] = data[0].getField("Amount");
    Amounts[AMTTYPE_Net] = "0";
    Amounts[AMTTYPE_Charge] = "0";
    loadDocumentType(); // lines require doc type
    // Contained Objects
    p_lines = loadLines();

    return true;

  }

  private DocLine[] loadLines() {

    ArrayList<Object> list = new ArrayList<Object>();
    DocLinePOWithholdingData[] data = null;
    try {
      log4jDocInvoice.debug("############### groupLines = " + groupLines);
      if (groupLines.equals("Y"))
        data = DocLinePOWithholdingData.selectTotal(connectionProvider, Record_ID);
      else
        data = DocLinePOWithholdingData.select(connectionProvider, Record_ID);
    } catch (ServletException e) {
      log4jDocInvoice.warn(e);
    }
    if (data == null || data.length == 0)
      return null;
    for (int i = 0; i < data.length; i++) {
      String Line_ID = data[i].scoPwithhoRecLineId;
      DocLine_POWithholding docLine = new DocLine_POWithholding(DocumentType, Record_ID, Line_ID);
      docLine.loadAttributes(data[i], this);
      String amt = data[i].amount;
      docLine.setAmount(amt);
      String docTypeId = data[i].emScoDoctyperefId;
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

    // Find withholding account for schema
    AcctSchemaDefault schemaDefault = OBDao
        .getFilteredCriteria(
            AcctSchemaDefault.class,
            Restrictions.eq(
                AcctSchemaDefault.PROPERTY_ACCOUNTINGSCHEMA,
                OBDal.getInstance().get(
                    org.openbravo.model.financialmgmt.accounting.coa.AcctSchema.class,
                    as.m_C_AcctSchema_ID))).list().get(0);
    String acct = schemaDefault.getScoApwithholdingAcct().getId();
    String strDateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    final SimpleDateFormat dateFormat = new SimpleDateFormat(strDateFormat);

    BigDecimal totalAmount = new BigDecimal(0);

    for (DocLine invoiceline : p_lines) {

      Invoice invoice = OBDal.getInstance().get(Invoice.class,
          ((DocLine_POWithholding) invoiceline).getInvoiceRefId());
      BigDecimal amtFrom = new BigDecimal(getConvertedAmt(invoiceline.getAmount().toString(),
          invoice.getCurrency().getId(), as.m_C_Currency_ID, DateAcct, "", AD_Client_ID, AD_Org_ID,
          conn));

      invoiceline.m_Record_Id3 = invoice.getId();
      invoiceline.m_AD_Table3_Id = "318";
      invoiceline.m_C_Project_ID = invoice.getProject() != null ? invoice.getProject().getId() : "";

      GLItem diverse = invoice.getScoDiverseAccGlitem();
      Account diverseAccount = null;
      if (diverse != null)
        diverseAccount = getAccountGLItem(diverse, as, false, conn);

      Vector<BigDecimal> vOutTC = new Vector<BigDecimal>();
      BigDecimal bpAmountConverted = convertAmount(new BigDecimal(invoiceline.getAmount()), true,
          DateAcct, TABLEID_Invoice, invoice.getId(), invoice.getCurrency().getId(),
          as.m_C_Currency_ID, invoiceline, as, fact, Fact_Acct_Group_ID, nextSeqNo(SeqNo), conn,
          true, amtFrom, true, true, vOutTC);
      bpAmountConverted = new BigDecimal(invoiceline.getAmount());

      fact.createLine(
          invoiceline,
          diverseAccount == null ? getAccountBPartner(invoice.getBusinessPartner().getId(), as,
              false, false, conn) : diverseAccount, invoice.getCurrency().getId(),
          bpAmountConverted.toString(), "", Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType,
          DateAcct, vOutTC.get(0), conn);

      // fact.createLine(invoiceline, diverseAccount==null ?
      // getAccountBPartner(invoice.getBusinessPartner().getId(), as, false, false, conn) :
      // diverseAccount, as.m_C_Currency_ID, amtFrom.toString(), "", Fact_Acct_Group_ID,
      // nextSeqNo(SeqNo), DocumentType, conn);
      totalAmount = totalAmount.add(amtFrom);

    }

    fact.createLine(null, Account.getAccount(conn, acct), as.m_C_Currency_ID, "",
        totalAmount.toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);

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
    DocPOWithholding.log4jDocInvoice = log4jDocInvoice;
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
    SCOPwithholdingReceipt invoice = OBDal.getInstance().get(SCOPwithholdingReceipt.class,
        strRecordId);
    boolean zero = true;
    for (SCOPwithhoRecLine invoiceline : invoice.getSCOPwithhoRecLineList()) {
      if (ZERO.compareTo(invoiceline.getAmount()) != 0) {
        zero = false;
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
