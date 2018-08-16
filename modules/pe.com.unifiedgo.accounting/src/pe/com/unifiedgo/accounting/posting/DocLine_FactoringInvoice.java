package pe.com.unifiedgo.accounting.posting;

import org.apache.log4j.Logger;
import org.openbravo.erpCommon.ad_forms.DocLine;

public class DocLine_FactoringInvoice extends DocLine {
  static Logger log4jDocLine_Invoice = Logger.getLogger(DocLine_Boe.class);

  public DocLine_FactoringInvoice(String DocumentType, String TrxHeader_ID, String TrxLine_ID) {
    super(DocumentType, TrxHeader_ID, TrxLine_ID);
  }

  private String m_DocType_ID = "";
  private String m_InvoiceRef_Id = "";
  private String transactiontype = "";

  public String getInvoiceRefId() {
    return m_InvoiceRef_Id;
  }

  public void setInvoiceRefId(String m_InvoiceRef_Id) {
    this.m_InvoiceRef_Id = m_InvoiceRef_Id;
  }

  public String getDocTypeID() {
    return m_DocType_ID;
  }

  public void setTransactiontype(String transactiontype) {
    this.transactiontype = transactiontype;
  }

  public String getTransactiontype() {
    return transactiontype;
  }

  public void setDocTypeID(String m_DoctypeId) {
    this.m_DocType_ID = m_DoctypeId;
  }

  public String getServletInfo() {
    return "Servlet for the accounting";
  } // end of getServletInfo() method
}
