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

import pe.com.unifiedgo.imports.data.simRelExpensesGlitem;

public class sim_financialInvoice extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

     Enumeration<String> params = info.vars.getParameterNames();
     while (params.hasMoreElements()) {
     System.out.println(params.nextElement());
     }
     
     
     
     String strInvoice = info.vars.getStringParameter("inpcInvoiceId");
     String strFinantialInvoiceLine = info.vars.getStringParameter("inpfinancialInvoiceLine");
     
     //System.out.println("strFinantialInvoiceLine: "+ strFinantialInvoiceLine);
     Invoice invoice = OBDal.getInstance().get(Invoice.class, strInvoice);
     if(invoice != null){
    	 if(invoice.isSalesTransaction()==false){
    		 //System.out.println("ENTRA");
    		 //System.out.println(invoice.getScoDuaexpensetype());
    		 if(invoice.getScoDuaexpensetype()!= null || !invoice.getScoDuaexpensetype().equals("")){
    		   OBCriteria<simRelExpensesGlitem>  glItems = OBDal.getInstance().createCriteria(simRelExpensesGlitem.class);
    		   glItems.add(Restrictions.eq(simRelExpensesGlitem.PROPERTY_CLIENT, invoice.getClient()));
    		   glItems.add(Restrictions.eq(simRelExpensesGlitem.PROPERTY_ACTIVE, true));
    		   glItems.add(Restrictions.eq(simRelExpensesGlitem.PROPERTY_DUAEXPENSETYPE, invoice.getScoDuaexpensetype()));
    		   
    		   List<simRelExpensesGlitem> relationEx = glItems.list();
    		   //System.out.println("TMB");
    		   
    		   if(relationEx.size()>0){
    			   //System.out.println("TERRA");
    			   //System.out.println("INSERTO: " + relationEx.get(0).getGLItem());
    			      info.addResult("inpaccountId", relationEx.get(0).getGLItem().getId());

    		   }
    		 }
    		 
    	 }
     }
     
    

  }
}
