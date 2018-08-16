package pe.com.unifiedgo.accounting.posting;

import java.math.BigDecimal;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.ad_forms.Account;
import org.openbravo.erpCommon.ad_forms.AcctSchema;
import org.openbravo.erpCommon.ad_forms.DocLine;
import org.openbravo.erpCommon.utility.CashVATUtil;

public class DocLine_POWithholding extends DocLine {
  static Logger log4jDocLine_Invoice = Logger.getLogger(DocLine_POWithholding.class);

  public DocLine_POWithholding(String DocumentType, String TrxHeader_ID, String TrxLine_ID) {
    super(DocumentType, TrxHeader_ID, TrxLine_ID);
  }

  private String m_DocType_ID = "";
  private String m_InvoiceRef_Id = "";
  
  public String getInvoiceRefId(){ return m_InvoiceRef_Id; }
  public void setInvoiceRefId(String m_InvoiceRef_Id){ this.m_InvoiceRef_Id = m_InvoiceRef_Id; }
  
  public String getDocTypeID(){
	  return m_DocType_ID;
  }
  
  public void setDocTypeID(String m_DoctypeId){
	  this.m_DocType_ID = m_DoctypeId;
  }
  
  

  public String getServletInfo() {
    return "Servlet for the accounting";
  } // end of getServletInfo() method
}
