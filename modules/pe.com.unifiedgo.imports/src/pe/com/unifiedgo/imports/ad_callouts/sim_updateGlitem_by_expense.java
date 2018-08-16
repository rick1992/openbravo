package pe.com.unifiedgo.imports.ad_callouts;



import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;

import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.plm.Product;

import pe.com.unifiedgo.accounting.data.SCOImportLineExpenses;
import pe.com.unifiedgo.imports.data.simRelExpensesGlitem;

public class sim_updateGlitem_by_expense extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

	  
	  /*
	  System.out.println("HELLO");
	  Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
		{ System.out.println(params.nextElement()); }**/
		     
   	  
     String mImportExpenseId = info.vars.getStringParameter("inpemScoImportExpensesId").replace(",", "");
	 String mFinancialInvoiceLine = info.vars.getStringParameter("inpfinancialInvoiceLine");
     String strInvoiceId = info.vars.getStringParameter("inpcInvoiceId");
     
     String strFinancialInvoiceLine = mFinancialInvoiceLine;
     //System.out.println("strInvoiceId: " + strInvoiceId);
     
     SCOImportLineExpenses importlineexpenses = OBDal.getInstance().get(SCOImportLineExpenses.class, mImportExpenseId.trim());
	 
     Invoice invoice = OBDal.getInstance().get(Invoice.class, strInvoiceId.trim());
     String strExpensesType = "";
     if(invoice != null && strFinancialInvoiceLine.equals("Y")){
    	 strExpensesType = invoice.getScoDuaexpensetype();
    	 OBCriteria<simRelExpensesGlitem> relacion = OBDal.getInstance().createCriteria(simRelExpensesGlitem.class);
		 relacion.add(Restrictions.eq(simRelExpensesGlitem.PROPERTY_DUAEXPENSETYPE,strExpensesType));
		 List<simRelExpensesGlitem> RelacionList = relacion.list();
		 if(RelacionList.size()>0){
			 info.addResult("inpaccountId", RelacionList.get(0).getGLItem().getId()); 
		 }
     
     }
    	 
  }
}
