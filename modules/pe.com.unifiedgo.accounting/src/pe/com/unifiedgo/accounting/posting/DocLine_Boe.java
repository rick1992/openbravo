package pe.com.unifiedgo.accounting.posting;

import org.apache.log4j.Logger;
import org.openbravo.erpCommon.ad_forms.DocLine;

public class DocLine_Boe extends DocLine {
  static Logger log4jDocLine_Invoice = Logger.getLogger(DocLine_Boe.class);

  public DocLine_Boe(String DocumentType, String TrxHeader_ID, String TrxLine_ID) {
    super(DocumentType, TrxHeader_ID, TrxLine_ID);
  }

  private String m_DocType_ID = "";
  private String m_InvoiceRef_Id = "";
  private String type_transaction = "";
  private String previous_boe_type = "";

  public String getInvoiceRefId() {
    return m_InvoiceRef_Id;
  }

  public void setInvoiceRefId(String m_InvoiceRef_Id) {
    this.m_InvoiceRef_Id = m_InvoiceRef_Id;
  }

  public String getDocTypeID() {
    return m_DocType_ID;
  }

  public void setTypeTransaction(String typeTransaction) {
    type_transaction = typeTransaction;
  }

  public String getTypeTransaction() {
    return type_transaction;
  }

  public void setPreviousBoeType(String previous_boe_type) {
    this.previous_boe_type = previous_boe_type;
  }

  public String getPreviousBoeType() {
    return previous_boe_type;
  }

  public void setDocTypeID(String m_DoctypeId) {
    this.m_DocType_ID = m_DoctypeId;
  }

  public String getServletInfo() {
    return "Servlet for the accounting";
  } // end of getServletInfo() method
}
