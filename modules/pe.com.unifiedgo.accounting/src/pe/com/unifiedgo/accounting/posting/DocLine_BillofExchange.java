package pe.com.unifiedgo.accounting.posting;

import org.apache.log4j.Logger;
import org.openbravo.erpCommon.ad_forms.DocLine;

public class DocLine_BillofExchange extends DocLine {
  static Logger log4jDocLine_Invoice = Logger.getLogger(DocLine_BillofExchange.class);

  public DocLine_BillofExchange(String DocumentType, String TrxHeader_ID, String TrxLine_ID) {
    super(DocumentType, TrxHeader_ID, TrxLine_ID);
  }

  private String m_InvoiceRef_Id = "";
  private String m_sco_Prepayment_Id = "";
  private String m_isfrom = "";

  public String getInvoiceRefId() {
    return m_InvoiceRef_Id;
  }

  public void setInvoiceRefId(String m_InvoiceRef_Id) {
    this.m_InvoiceRef_Id = m_InvoiceRef_Id;
  }

  public String getScoPrepaymentId() {
    return m_sco_Prepayment_Id;
  }

  public void setScoPrepaymentId(String m_sco_Prepayment_Id) {
    this.m_sco_Prepayment_Id = m_sco_Prepayment_Id;
  }

  public void setIsFrom(String isfrom) {
    this.m_isfrom = isfrom;
  }

  public String getIsFrom() {
    return m_isfrom;
  }

  public String getServletInfo() {
    return "Servlet for the accounting";
  } // end of getServletInfo() method
}
