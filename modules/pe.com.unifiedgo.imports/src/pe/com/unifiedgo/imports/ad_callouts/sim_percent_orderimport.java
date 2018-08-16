package pe.com.unifiedgo.imports.ad_callouts;



import java.util.Enumeration;

import javax.servlet.ServletException;

import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.plm.Product;

public class sim_percent_orderimport extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

     //Enumeration<String> params = info.vars.getParameterNames();
     //while (params.hasMoreElements()) {
     //System.out.println(params.nextElement());
     //}
     // example
     String mTotalAmount = info.vars.getStringParameter("inpgrandtotal").replace(",", "");
     String percent = info.vars.getStringParameter("inpemScrDiscount").replace(",", "");
     if(mTotalAmount==null || mTotalAmount.equals("") || Float.parseFloat(mTotalAmount)==0){
       	 info.addResult("inpdiscountReport", "0");
     }
     else{
    	 if(percent == null || percent.equals("")){
    		 info.addResult("inpdiscountReport", "0");
    	 }
    	 else{
           float discount = (Float.parseFloat(percent)*Float.parseFloat(mTotalAmount))/100;
           info.addResult("inpdiscountReport", discount);
    	 }
     }
  }
}
