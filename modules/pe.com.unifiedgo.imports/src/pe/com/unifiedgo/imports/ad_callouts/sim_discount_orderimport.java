package pe.com.unifiedgo.imports.ad_callouts;



import java.util.Enumeration;

import javax.servlet.ServletException;

import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.plm.Product;

public class sim_discount_orderimport extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

     /*Enumeration<String> params = info.vars.getParameterNames();
     while (params.hasMoreElements()) {
     System.out.println(params.nextElement());
     }*/

    // example
     String mTotalAmount = info.vars.getStringParameter("inpgrandtotal").replace(",", "");
     String mDiscount = info.vars.getStringParameter("inpdiscountReport").replace(",", "");
     if(mTotalAmount==null || mTotalAmount.equals("") || Float.parseFloat(mTotalAmount)==0){
    	 mDiscount = "0";
    	 //info.addResult("inpemScrDiscount", "0");
    	 info.addResult("inpdiscountReport", "0");
     }
     else{
    	 if(mDiscount == null || mDiscount.equals("")){
    		 info.addResult("inpemScrDiscount", "0");
    	 }
    	 else{
           float percent = (Float.parseFloat(mDiscount)*100)/Float.parseFloat(mTotalAmount);
           info.addResult("inpemScrDiscount", percent);
    	 }
     }
  }
}
