package pe.com.unifiedgo.accounting.posting;

import java.math.BigDecimal;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.ad_forms.Account;
import org.openbravo.erpCommon.ad_forms.AcctSchema;
import org.openbravo.erpCommon.ad_forms.DocLine;
import org.openbravo.erpCommon.utility.CashVATUtil;

public class DocLine_ImpCosting extends DocLine {
  static Logger log4jDocLine_Invoice = Logger.getLogger(DocLine_ImpCosting.class);

  public DocLine_ImpCosting(String DocumentType, String TrxHeader_ID, String TrxLine_ID) {
    super(DocumentType, TrxHeader_ID, TrxLine_ID);
  }
  
  
  public String getServletInfo() {
    return "Servlet for the accounting";
  } // end of getServletInfo() method
}
