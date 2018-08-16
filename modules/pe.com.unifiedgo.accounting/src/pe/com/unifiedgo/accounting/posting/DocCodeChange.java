package pe.com.unifiedgo.accounting.posting;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.openbravo.erpCommon.ad_forms.DocLine_Material;
import org.openbravo.erpCommon.ad_forms.Fact;
import org.openbravo.erpCommon.ad_forms.FactLine;
import org.openbravo.erpCommon.ad_forms.ProductInfo;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.financialmgmt.accounting.Costcenter;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;

import pe.com.unifiedgo.accounting.data.ScoRendicioncuentas;
import pe.com.unifiedgo.warehouse.data.SwaMovementCode;
import pe.com.unifiedgo.warehouse.data.SwaMovementCodeProduct;

public class DocCodeChange extends AcctServer {
  private static final long serialVersionUID = 1L;
  static Logger log4jDocInvoice = Logger.getLogger(AcctServer.class);

  String SeqNo = "0";

  /**
   * Constructor
   * 
   * @param AD_Client_ID
   *          AD_Client_ID
   */
  public DocCodeChange() {
  }

  public DocCodeChange(String AD_Client_ID, String AD_Org_ID, ConnectionProvider connectionProvider) {
    super(AD_Client_ID, AD_Org_ID, connectionProvider);
  }

  public void loadObjectFieldProvider(ConnectionProvider conn, String stradClientId, String Id) throws ServletException {
    setObjectFieldProvider(DocCodeChangeData.selectRegistro(conn, stradClientId, Id));
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
    DocLineCodeChangeData[] data = null;
    try {
      log4jDocInvoice.debug("############### groupLines = " + groupLines);
      if (groupLines.equals("Y"))
        data = DocLineCodeChangeData.selectTotal(connectionProvider, Record_ID);
      else
        data = DocLineCodeChangeData.select(connectionProvider, Record_ID);
    } catch (ServletException e) {
      log4jDocInvoice.warn(e);
    }
    if (data == null || data.length == 0)
      return null;
    for (int i = 0; i < data.length; i++) {
      String Line_ID = data[i].swaMovementcodeProductId;
      DocLine_CodeChange docLine = new DocLine_CodeChange(DocumentType, Record_ID, Line_ID);
      docLine.loadAttributes(data[i], this);
      docLine.setProductTo(data[i].getField("mToProductId"), this);
      docLine.setQty(data[i].getField("qtyordered"), connectionProvider);
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
  public Fact createFact(AcctSchema as, ConnectionProvider conn, Connection con, VariablesSecureApp vars) throws ServletException {

    // Select specific definition
    log4jDocInvoice.debug("Starting create fact");
    // create Fact Header
    Fact fact = new Fact(this, as, Fact.POST_Actual);
    String Fact_Acct_Group_ID = SequenceIdData.getUUID();

    // Cash based accounting
    if (!as.isAccrual())
      return null;

    // create facts according to all documents referenced
    SwaMovementCode codechange = OBDal.getInstance().get(SwaMovementCode.class, Record_ID);

    for (int i = 0; p_lines != null && i < p_lines.length; i++) {
        DocLine_CodeChange line = (DocLine_CodeChange) p_lines[i];
        	  
        	  SwaMovementCodeProduct prodcodechange = OBDal.getInstance().get(SwaMovementCodeProduct.class, line.m_TrxLine_ID);
        	  
        	  OBCriteria<MaterialTransaction> obCriteria = OBDal.getInstance().createCriteria(MaterialTransaction.class);
              obCriteria.add(Restrictions.eq(MaterialTransaction.PROPERTY_SWAMOVCODEPRODUCT, prodcodechange));
              obCriteria.add(Restrictions.gt(MaterialTransaction.PROPERTY_MOVEMENTQUANTITY, BigDecimal.ZERO));
              MaterialTransaction transaction = (MaterialTransaction)obCriteria.uniqueResult();
        	  
              if(!transaction.isCostCalculated()){
            	  Map<String, String> parameters = getNotCalculatedCostParameters(transaction);
            	  setMessageResult(conn, STATUS_NotCalculatedCost, "error", parameters);
                  throw new IllegalStateException();
              }
              
              
              BigDecimal amount = transaction.getTransactionCost();
              
              //crear asiento
              Account cogsAccount = line.getAccount(ProductInfo.ACCTTYPE_P_Expense, as, conn);
              Account cogsAccountTo = line.getAccountTo(ProductInfo.ACCTTYPE_P_Expense, as, conn);
              
              fact.createLine(line, cogsAccount, transaction.getCurrency().getId(), amount.toString(), "", Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
              fact.createLine(line, cogsAccountTo, transaction.getCurrency().getId(), "", amount.toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
              
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
    log4jDocInvoice.debug("DocCodeChange - oldSeqNo = " + oldSeqNo);
    BigDecimal seqNo = new BigDecimal(oldSeqNo);
    SeqNo = (seqNo.add(new BigDecimal("10"))).toString();
    log4jDocInvoice.debug("DocCodeChange - nextSeqNo = " + SeqNo);
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
    DocCodeChange.log4jDocInvoice = log4jDocInvoice;
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
	  setStatus(STATUS_DocumentDisabled);
	  
	    return false;
  }

  public String getServletInfo() {
    return "Servlet for the accounting";
  } // end of getServletInfo() method

}
