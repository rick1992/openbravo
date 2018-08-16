package pe.com.unifiedgo.accounting.process;

import java.util.Map;
import java.util.Set;

import org.openbravo.client.application.FilterExpression;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.gl.GLJournal;

public class GetInvoiceLineFromApCreditMemo implements FilterExpression {

  @Override
  public String getExpression(Map<String, String> requestMap) {

    
     /* System.out.println("ADIsOrgIncluded filter expression:"); 
      Set<String> keyset =
      requestMap.keySet(); String[] keys = new String[keyset.size()]; keyset.toArray(keys); for
      (int i = 0; i < keys.length; i++) { System.out.println(keys[i] + ":" +
      requestMap.get(keys[i])); }*/
	  StringBuilder whereClause = new StringBuilder(); 
	  
	  String strInvoiceId = requestMap.get("inpcInvoiceId");
	  Invoice invoiceAPCreditMemo = OBDal.getInstance().get(Invoice.class, strInvoiceId.trim());
	  if(invoiceAPCreditMemo != null){
		//  System.out.println(invoiceAPCreditMemo.getScoInvoiceref().getDocumentNo()); 
		  
		if(invoiceAPCreditMemo.getScoInvoiceref() != null){  
		  //if (whereClause.length() != 0)
		  //    whereClause.append(" and ");
			//System.out.println("PERUUUUUUU");
		    whereClause.append("e.invoice.id = '" + invoiceAPCreditMemo.getScoInvoiceref().getId() + "'");
		} 
	  }
	  
    
  //  System.out.println("whereClause:" + whereClause.toString());
    return whereClause.toString();

  }
}
