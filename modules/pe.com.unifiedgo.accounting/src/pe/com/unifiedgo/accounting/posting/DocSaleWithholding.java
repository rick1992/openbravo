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
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.ad_forms.Account;
import org.openbravo.erpCommon.ad_forms.AcctSchema;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.ad_forms.DocLine;
import org.openbravo.erpCommon.ad_forms.Fact;
import org.openbravo.erpCommon.ad_forms.FactLine;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchemaDefault;
import org.openbravo.model.financialmgmt.gl.GLItem;

import pe.com.unifiedgo.accounting.data.SCOSwithhoRecLine;
import pe.com.unifiedgo.accounting.data.SCOSwithholdingReceipt;

public class DocSaleWithholding extends AcctServer {
  private static final long serialVersionUID = 1L;
  static Logger log4jDocInvoice = Logger.getLogger(AcctServer.class);

  String SeqNo = "0";

  /**
   * Constructor
   * 
   * @param AD_Client_ID
   *          AD_Client_ID
   */
  public DocSaleWithholding() {

  }

  public DocSaleWithholding(String AD_Client_ID, String AD_Org_ID,
      ConnectionProvider connectionProvider) {
    super(AD_Client_ID, AD_Org_ID, connectionProvider);
  }

  public void loadObjectFieldProvider(ConnectionProvider conn, String stradClientId, String Id)
      throws ServletException {
    setObjectFieldProvider(DocSaleWithholdingData.selectRegistro(conn, stradClientId, Id));
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
    DocLineSaleWithholdingData[] data = null;
    try {
      log4jDocInvoice.debug("############### groupLines = " + groupLines);
      if (groupLines.equals("Y"))
        data = DocLineSaleWithholdingData.selectTotal(connectionProvider, Record_ID);
      else
        data = DocLineSaleWithholdingData.select(connectionProvider, Record_ID);
    } catch (ServletException e) {
      log4jDocInvoice.warn(e);
    }
    if (data == null || data.length == 0)
      return null;
    for (int i = 0; i < data.length; i++) {
      String Line_ID = data[i].scoSwithhoRecLineId;
      DocLine_SaleWithholding docLine = new DocLine_SaleWithholding(DocumentType, Record_ID,
          Line_ID);
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

    SCOSwithholdingReceipt header = OBDal.getInstance().get(SCOSwithholdingReceipt.class,
        this.Record_ID);
    BigDecimal exchangeRate = header.getMultiplyexchangerate();
    Currency curr = header.getCurrency();

    // Cash based accounting
    if (!as.isAccrual())
      return null;

    // Find withholding account for schema
    OBCriteria<AcctSchemaDefault> Crit = OBDal.getInstance()
        .createCriteria(AcctSchemaDefault.class);
    Crit.add(Restrictions.eq(
        AcctSchemaDefault.PROPERTY_ACCOUNTINGSCHEMA,
        OBDal.getInstance().get(org.openbravo.model.financialmgmt.accounting.coa.AcctSchema.class,
            as.m_C_AcctSchema_ID)));
    Crit.add(Restrictions.eq(AcctSchemaDefault.PROPERTY_CLIENT,
        OBDal.getInstance().get(Client.class, this.AD_Client_ID)));
    Crit.add(Restrictions.sqlRestriction("AD_ISORGINCLUDED('" + this.AD_Org_ID + "', ad_org_id, '"
        + this.AD_Client_ID + "') > -1"));

    Crit.setFilterOnReadableClients(false);
    Crit.setFilterOnReadableOrganization(false);

    AcctSchemaDefault schemaDefault = Crit.list().get(0);
    String acct = schemaDefault.getScoArwithholdingAcct().getId();
    String strDateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    final SimpleDateFormat dateFormat = new SimpleDateFormat(strDateFormat);

    BigDecimal physicaltotalAmount = header.getTotalamountPhysicaldoc();
    BigDecimal totalamount = header.getTotalAmount();
    isReceiptForConversion = false;

    String genDate = dateFormat.format(header.getGeneratedDate());

    for (DocLine invoiceline : p_lines) {

      Invoice invoice = OBDal.getInstance().get(Invoice.class,
          ((DocLine_SaleWithholding) invoiceline).getInvoiceRefId());
      C_Invoice_ID_Ref = invoice.getId();

      invoiceline.m_Record_Id3 = invoice.getId();
      invoiceline.m_AD_Table3_Id = "318";
      invoiceline.m_C_Project_ID = invoice.getProject() != null ? invoice.getProject().getId() : "";

      GLItem diverse = invoice.getScoDiverseAccGlitem();
      Account diverseAccount = null;
      if (diverse != null)
        diverseAccount = getAccountGLItem(diverse, as, false, conn);

      /*
       * BigDecimal amtFrom = null;
       * 
       * if(invoice.getCurrency().equals(curr)){ amtFrom = new
       * BigDecimal(invoiceline.getAmount().toString()); }else if(exchangeRate==null){ amtFrom = new
       * BigDecimal(getConvertedAmt(invoiceline.getAmount().toString(),
       * invoice.getCurrency().getId(), as.m_C_Currency_ID, dateFormat.format
       * (AcctServer.getDateForInvoiceReference(invoice)), "", AD_Client_ID, AD_Org_ID, conn));
       * }else{ amtFrom = new BigDecimal(invoiceline
       * .getAmount().toString()).multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP); }
       */

      boolean tc = AcctServer.getTCenVigencia(invoice, DateAcct);

      Vector<BigDecimal> vOutTC = new Vector<BigDecimal>();
      BigDecimal bpAmountConverted = convertAmountInDiferentTCFromDate(
          new BigDecimal(invoiceline.getAmount()), false, DateAcct, genDate, TABLEID_Invoice,
          invoice.getId(), invoice.getCurrency().getId(), as.m_C_Currency_ID, invoiceline, as,
          fact, Fact_Acct_Group_ID, nextSeqNo(SeqNo), conn, true, tc, true, vOutTC);

      bpAmountConverted = new BigDecimal(invoiceline.getAmount());
      fact.createLine(
          invoiceline,
          diverseAccount == null ? getAccountBPartner(invoice.getBusinessPartner().getId(), as,
              true, false, conn) : diverseAccount, invoice.getCurrency().getId(), "",
          bpAmountConverted.toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType,
          DateAcct, vOutTC.get(0), conn);

    }

    fact.createLine(null, Account.getAccount(conn, acct), as.m_C_Currency_ID,
        physicaltotalAmount.toString(), "", Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType,
        genDate, conn);

    // diference between totalamount and physicaltotalamount must be put in diferencia de cambio
    // instead of redondeo de moneda
    BigDecimal amtDiff = (totalamount).subtract(physicaltotalAmount);
    amtDiff = amtDiff.setScale(header.getCurrency().getStandardPrecision().intValue(),
        BigDecimal.ROUND_HALF_EVEN);
    if (amtDiff.compareTo(BigDecimal.ZERO) == -1) {
      FactLine fl = fact.createLine(null,
          getAccount(AcctServer.ACCTTYPE_ConvertGainDefaultAmt, as, conn), as.m_C_Currency_ID, "",
          amtDiff.abs().toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
      fl.descripcion = "GANANCIA POR DIFERENCIA DE CAMBIO";
      fl.forceDescription = false;
    } else if (amtDiff.compareTo(BigDecimal.ZERO) != 0) {
      FactLine fl = fact.createLine(null,
          getAccount(AcctServer.ACCTTYPE_ConvertChargeDefaultAmt, as, conn), as.m_C_Currency_ID,
          amtDiff.abs().toString(), "", Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
      fl.descripcion = "PERDIDA POR DIFERENCIA DE CAMBIO";
      fl.setCostcenter(getCostcenter(AcctServer.ACCTTYPE_ConvertChargeDefaultAmt, as, conn));
      fl.forceDescription = false;
    }

    SeqNo = "0";

    fact.forceMultiCurrency = true;
    isReceiptForConversion = true;

    for (int i = 0; i < fact.getLines().length; i++) {
      System.out.println("crdb:" + fact.getLines()[i].getM_AmtAcctCr() + " "
          + fact.getLines()[i].getM_AmtAcctDr() + " " + fact.getLines()[i].getM_acct().Account_ID);
    }

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
    DocSaleWithholding.log4jDocInvoice = log4jDocInvoice;
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
    SCOSwithholdingReceipt invoice = OBDal.getInstance().get(SCOSwithholdingReceipt.class,
        strRecordId);
    boolean zero = true;
    for (SCOSwithhoRecLine invoiceline : invoice.getSCOSwithhoRecLineList()) {
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
