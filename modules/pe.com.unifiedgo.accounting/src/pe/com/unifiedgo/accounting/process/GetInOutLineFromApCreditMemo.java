package pe.com.unifiedgo.accounting.process;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openbravo.client.application.FilterExpression;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.financialmgmt.gl.GLJournal;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;

public class GetInOutLineFromApCreditMemo implements FilterExpression {

  @Override
  public String getExpression(Map<String, String> requestMap) {

    
    /*System.out.println("ADIsOrgIncluded filter expression:"); 
      Set<String> keyset =
      requestMap.keySet(); String[] keys = new String[keyset.size()]; keyset.toArray(keys); for
      (int i = 0; i < keys.length; i++) { System.out.println(keys[i] + ":" +
      requestMap.get(keys[i])); }*/
      
      
	  StringBuilder whereClause = new StringBuilder(); 
	  
	  String strInvoiceLineId= requestMap.get("inpcInvoicelineId");
	  InvoiceLine invoiceLine = OBDal.getInstance().get(InvoiceLine.class, strInvoiceLineId.trim());
	  String strInvoiceLineRefId= requestMap.get("inpemSimApcreditToInvlineId");

		  InvoiceLine invoiceLine_ref = OBDal.getInstance().get(InvoiceLine.class, strInvoiceLineRefId);
          if(invoiceLine_ref.getGoodsShipmentLine()!= null){
        	  //System.out.println("third");
       	   whereClause.append("e.id = '" + invoiceLine_ref.getGoodsShipmentLine().getId() + "'");
          }
          else if(invoiceLine_ref.getSalesOrderLine() != null){
        	  //System.out.println("fourth");
        	  List<ShipmentInOutLine> inOutLineList = invoiceLine_ref.getSalesOrderLine().getMaterialMgmtShipmentInOutLineList();
        	  String strConcatIds = "";
        	  
        	  for(int i =0; i< inOutLineList.size() ;i++){
        		  if(i==0)
        		   strConcatIds = strConcatIds + "'" +inOutLineList.get(i).getId()  + "'";
        		  if(i>0)
        		   strConcatIds = strConcatIds + ",'" +inOutLineList.get(i).getId()  + "'";
        	  }
        	  whereClause.append("e.id in (" + strConcatIds + ")");
          }
          else{
        	 // System.out.println("fiFth");
        	  whereClause.append("e.id = ''");
          }
          
	
	  
	  /*String strInvoiceId = requestMap.get("inpcInvoiceId");
	  Invoice invoiceAPCreditMemo = OBDal.getInstance().get(Invoice.class, strInvoiceId.trim());
	  if(invoiceAPCreditMemo != null){
		//  System.out.println(invoiceAPCreditMemo.getScoInvoiceref().getDocumentNo()); 
		  
		if(invoiceAPCreditMemo.getScoInvoiceref() != null){  
		  //if (whereClause.length() != 0)
		  //    whereClause.append(" and ");
			//System.out.println("PERUUUUUUU");
		    whereClause.append("e.invoice.id = '" + invoiceAPCreditMemo.getScoInvoiceref().getId() + "'");
		} 
	  }*/

   // System.out.println("whereClause:" + whereClause.toString());
    return whereClause.toString();

  }
}
