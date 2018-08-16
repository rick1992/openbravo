package pe.com.unifiedgo.accounting.posting;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
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
import org.openbravo.erpCommon.ad_forms.ProductInfo;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.common.plm.CategoryAccounts;
import org.openbravo.model.common.plm.ProductCategory;
import org.openbravo.model.financialmgmt.accounting.coa.AccountingCombination;
import org.openbravo.model.financialmgmt.accounting.coa.ElementValue;
import org.openbravo.model.financialmgmt.gl.GLItem;

import pe.com.unifiedgo.imports.data.SimImpCosting;

public class DocImpCosting extends AcctServer {
  private static final long serialVersionUID = 1L;
  static Logger log4jDocInvoice = Logger.getLogger(AcctServer.class);

  String SeqNo = "0";

  /**
   * Constructor
   * 
   * @param AD_Client_ID
   *          AD_Client_ID
   */
  public DocImpCosting() {
  }

  public DocImpCosting(String AD_Client_ID, String AD_Org_ID, ConnectionProvider connectionProvider) {
    super(AD_Client_ID, AD_Org_ID, connectionProvider);
  }

  public void loadObjectFieldProvider(ConnectionProvider conn, String stradClientId, String Id)
      throws ServletException {
    setObjectFieldProvider(DocImpCostingData.selectRegistro(conn, stradClientId, Id));
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

    SimImpCosting costing = OBDal.getInstance().get(SimImpCosting.class, Record_ID);

    ArrayList<Object> list = new ArrayList<Object>();
    DocLineImpCostingData[] data = null;
    try {
      log4jDocInvoice.debug("############### groupLines = " + groupLines);
      data = DocLineImpCostingData.select(connectionProvider, costing.getNroDua().getId());
    } catch (ServletException e) {
      log4jDocInvoice.warn(e);
    }
    if (data == null || data.length == 0)
      return null;
    for (int i = 0; i < data.length; i++) {
      String Line_ID = data[i].cInvoiceId;
      DocLine_ImpCosting docLine = new DocLine_ImpCosting(DocumentType, Record_ID, Line_ID);
      docLine.loadAttributes(data[i], this);

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

  private String isPorRecibir(String cuenta) {

    if (cuenta.equals("6011104")) {
      return "6011102";
    }

    if (cuenta.equals("6091920")) {
      return "6091902";
    }
    if (cuenta.equals("6091921")) {
      return "6091102";
    }
    if (cuenta.equals("6091922")) {
      return "6091201";
    }
    if (cuenta.equals("6091923")) {
      return "6091301";
    }

    if (cuenta.equals("6091924")) {
      return "6091902";
    }

    return "";
  }

  public Fact createFact(AcctSchema as, ConnectionProvider conn, Connection con,
      VariablesSecureApp vars) throws ServletException {

    SimImpCosting costing = OBDal.getInstance().get(SimImpCosting.class, Record_ID);

    // Select specific definition
    log4jDocInvoice.debug("Starting create fact");
    // create Fact Header
    Fact fact = new Fact(this, as, Fact.POST_Actual);
    String Fact_Acct_Group_ID = SequenceIdData.getUUID();

    // Cash based accounting
    if (!as.isAccrual())
      return null;

    String strDateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    final SimpleDateFormat dateFormat = new SimpleDateFormat(strDateFormat);

    BigDecimal totalAmount = new BigDecimal(0);

    for (DocLine costingline : p_lines) {

      Invoice invoice = OBDal.getInstance().get(Invoice.class,
          ((DocLine_ImpCosting) costingline).m_TrxLine_ID);
      List<InvoiceLine> invLines = invoice.getInvoiceLineList();

      for (int i = 0; i < invLines.size(); i++) {
        InvoiceLine invLine = invLines.get(i);
        String cuenta = "";
        AccountingCombination comb = null;

        if (invLine.getSimDua() != null
            && !invLine.getSimDua().getId().equals(costing.getNroDua().getId()))
          continue;

        if (invLine.isFinancialInvoiceLine()) {

          DocImpCostingData[] dataGlitemLine = null;
          dataGlitemLine = DocImpCostingData.selectGlitem(conn, invLine.getAccount().getId(),
              as.getC_AcctSchema_ID());

          comb = OBDal.getInstance().get(AccountingCombination.class,
              dataGlitemLine[0].glitemDebitAcct);
          cuenta = comb.getAccount().getSearchKey();

        } else {
          // buscar cuenta de producto

          ProductInfo prodInfo = new ProductInfo(invLine.getProduct().getId(), conn);

          if (invLine.isDeferred()) {

            Account deferAcct = prodInfo.getAccount(ProductInfo.ACCTTYPE_P_DefExpense, as, conn);

            if (invLine.getScoAutoAccGlitem() != null && !invLine.getScoAutoAccGlitem().equals("")) {
              deferAcct = getAccountGLItem(
                  OBDal.getInstance().get(GLItem.class, invLine.getScoAutoAccGlitem().getId()), as,
                  true, conn);
            }

            comb = OBDal.getInstance().get(AccountingCombination.class,
                deferAcct.C_ValidCombination_ID);
            cuenta = comb.getAccount().getSearchKey();

          } else {
            cuenta = "0000000";
          }

        }

        String extorno = isPorRecibir(cuenta);
        if (!extorno.equals("")) {
          // crearLinea

          Organization org = OBContext.getOBContext()
              .getOrganizationStructureProvider(invoice.getClient().getId())
              .getLegalEntity(invoice.getOrganization());

          OBCriteria<ElementValue> comb2Crit = OBDal.getInstance().createCriteria(
              ElementValue.class);
          comb2Crit.add(Restrictions.eq(ElementValue.PROPERTY_SEARCHKEY, extorno));
          comb2Crit.add(Restrictions.eq(ElementValue.PROPERTY_ORGANIZATION, org));

          ElementValue ev2 = (ElementValue) comb2Crit.uniqueResult();

          BigDecimal amt = invLine.getLineNetAmount();

          Vector<BigDecimal> vOutTC = new Vector<BigDecimal>();
          BigDecimal bpAmountConverted = convertAmount(amt, false, DateAcct, TABLEID_Invoice,
              invoice.getId(), invoice.getCurrency().getId(), as.m_C_Currency_ID, costingline, as,
              fact, Fact_Acct_Group_ID, nextSeqNo(SeqNo), conn, false, true, true, vOutTC, false,
              true);
          bpAmountConverted = amt;

          String debitStr = bpAmountConverted.toString();
          String creditStr = "";

          if (invoice.getSCOSpecialDocType().equals("SCOAPCREDITMEMO")) {
            creditStr = debitStr;
            debitStr = "";
          }

          costingline.m_description = costing.getDocumentNo() + " DAM/"
              + costing.getNroDua().getSCODuanumber();
          FactLine line = fact.createLine(costingline, Account.getAccount(conn, comb.getId()),
              invoice.getCurrency().getId(), creditStr, debitStr, Fact_Acct_Group_ID,
              nextSeqNo(SeqNo), DocumentType, DateAcct, vOutTC.get(0), conn);
          line.setRecordId3(invoice.getId());
          line.descripcion = "EXTORNO POR RECIBIR DOC/" + invoice.getScrPhysicalDocumentno();
          line = fact.createLine(costingline, Account.getAccount(conn, as.getC_AcctSchema_ID(),
              ev2.getId()), invoice.getCurrency().getId(), debitStr, creditStr, Fact_Acct_Group_ID,
              nextSeqNo(SeqNo), DocumentType, DateAcct, vOutTC.get(0), conn);
          line.setRecordId3(invoice.getId());
          line.descripcion = "EXTORNO POR RECIBIR DOC/" + invoice.getScrPhysicalDocumentno();
        }
      }
    }

    isReceiptForConversion = true;
    fact.forceMultiCurrency = true;

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
    log4jDocInvoice.debug("DocImpCosting - oldSeqNo = " + oldSeqNo);
    BigDecimal seqNo = new BigDecimal(oldSeqNo);
    SeqNo = (seqNo.add(new BigDecimal("10"))).toString();
    log4jDocInvoice.debug("DocImpCosting - nextSeqNo = " + SeqNo);
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
    DocImpCosting.log4jDocInvoice = log4jDocInvoice;
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

    /*
     * FieldProvider dataFP[] = getObjectFieldProvider(); if (ZERO.compareTo(new
     * BigDecimal(dataFP[0].getField("Amount"))) == 0) { ScoRendicioncuentas invoice =
     * OBDal.getInstance().get(ScoRendicioncuentas.class, strRecordId); boolean zero = true; for
     * (SCORendcuentasLine invoiceline : invoice.getSCORendcuentasLineList()) { if
     * (ZERO.compareTo(invoiceline.getAmount()) != 0) { zero = false; } } if (zero) { strMessage =
     * "@TotalGrossIsZero@"; setStatus(STATUS_DocumentDisabled); return false; } }
     * 
     * if (ZERO.compareTo(new BigDecimal(dataFP[0].getField("Refund"))) != 0) { strMessage =
     * "@NotBalance@"; setStatus(STATUS_DocumentDisabled); return false;
     * 
     * }
     */

    return true;
  }

  public String getServletInfo() {
    return "Servlet for the accounting";
  } // end of getServletInfo() method

}
