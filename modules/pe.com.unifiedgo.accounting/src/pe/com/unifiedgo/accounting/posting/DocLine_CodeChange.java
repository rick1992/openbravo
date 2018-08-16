package pe.com.unifiedgo.accounting.posting;

import java.math.BigDecimal;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.ad_forms.Account;
import org.openbravo.erpCommon.ad_forms.AcctSchema;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.ad_forms.DocLine;
import org.openbravo.erpCommon.ad_forms.ProductInfo;
import org.openbravo.erpCommon.utility.CashVATUtil;

public class DocLine_CodeChange extends DocLine {
  static Logger log4jDocLine_Invoice = Logger.getLogger(DocLine_CodeChange.class);

  public ProductInfo p_productInfo_to = null;
  
  public DocLine_CodeChange(String DocumentType, String TrxHeader_ID, String TrxLine_ID) {
    super(DocumentType, TrxHeader_ID, TrxLine_ID);
  }
  
  public void setProductTo(String productTo, AcctServer vo){
	  
	  p_productInfo_to = new ProductInfo(productTo, vo.getConnectionProvider());
  }

  public Account getAccount(String AcctType, AcctSchema as, ConnectionProvider conn) {
	    return p_productInfo.getAccount(AcctType, as, conn);
	  } // getAccount
  
  public Account getAccountTo(String AcctType, AcctSchema as, ConnectionProvider conn) {
	    return p_productInfo_to.getAccount(AcctType, as, conn);
	  }
  
    public void setQty(String qty, ConnectionProvider conn) {
	    super.setQty(qty); // save TrxQty
	    p_productInfo.setQty(qty, p_productInfo.m_C_UOM_ID, conn);
	   
	  } // setQty

	  public String getQty() {
	    return m_qty;
	  }
	  
  public String getServletInfo() {
    return "Servlet for the accounting";
  } // end of getServletInfo() method
}
