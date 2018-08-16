package pe.com.unifiedgo.imports.ad_callouts;



import java.math.BigDecimal;
import java.util.Enumeration;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.plm.Product;

public class sim_aditional_orderimport extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

	 VariablesSecureApp vars = info.vars;
	 String lastFieldChanged = info.getLastFieldChanged();
	 
	 //System.out.println("TERRICOLA");
	 
	 /*
     Enumeration<String> params = info.vars.getParameterNames();
     while (params.hasMoreElements()) {
     System.out.println(params.nextElement());
     }*/
     // example
     
     
     /*
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
     */
     
     
        String strGrandtotal = info.vars.getStringParameter("inpgrandtotal").replace(",", "");
        String strAditionaOneCost = info.vars.getStringParameter("inpaditionalOneCost").replace(",", "");
	    String strAditionalTwoCost = info.vars.getStringParameter("inpaditionalTwoCost").replace(",", "");
	    String strAditionalThreeCost = info.vars.getStringParameter("inpaditionalThreeCost").replace(",", "");
	    
	    String strAditionaTmpOneCost = info.vars.getStringParameter("inpaditionalOneTmpCost").replace(",", "");
	    String strAditionalTmpTwoCost = info.vars.getStringParameter("inpaditionalTwoTmpCost").replace(",", "");
	    String strAditionalTmpThreeCost = info.vars.getStringParameter("inpaditionalThreeTmpCost").replace(",", "");
	  
	  
	    BigDecimal granTotalCalculate = granTotalCalculate= BigDecimal.ZERO;
	    BigDecimal granToInsert = granTotalCalculate= BigDecimal.ZERO;
	    BigDecimal granTotal = new BigDecimal((strGrandtotal.equals(""))?"0": strGrandtotal );
	    BigDecimal aditionalOneCost = new BigDecimal((strAditionaOneCost.equals(""))?"0": strAditionaOneCost );
	    BigDecimal aditionalTwoCost = new BigDecimal((strAditionalTwoCost.equals(""))?"0": strAditionalTwoCost );
	    BigDecimal aditionalThreeCost = new BigDecimal((strAditionalThreeCost.equals(""))?"0": strAditionalThreeCost );
		
	    BigDecimal aditionalTmpOneCost = new BigDecimal((strAditionaTmpOneCost.equals(""))?"0": strAditionaTmpOneCost );
	    BigDecimal aditionalTmpTwoCost = new BigDecimal((strAditionalTmpTwoCost.equals(""))?"0": strAditionalTmpTwoCost );
	    BigDecimal aditionalTmpThreeCost = new BigDecimal((strAditionalTmpThreeCost.equals(""))?"0": strAditionalTmpThreeCost );
	

	    


	/*    if ("inpaditionalOneCost".equals(lastFieldChanged) && aditionalOneCost != null) {
		    System.out.println("PRIMERO");

	    	granToInsert = aditionalOneCost.subtract(aditionalTmpOneCost);
	    	System.out.println("granToInsert: " + granToInsert); 
	        granTotalCalculate = granTotal.add(granToInsert);
	        System.out.println("granTotalCalculate: " + granTotalCalculate); 
	        info.addResult("inpgrandtotal", granTotalCalculate);
		    info.addResult("inpaditionalOneCost", aditionalOneCost);
		    info.addResult("inpaditionalOneTmpCost", aditionalOneCost);
		   
		    
	    }else if("inpaditionalTwoCost".equals(lastFieldChanged) && aditionalTwoCost != null){
		    System.out.println("SEGUNDO");

	    	granToInsert = aditionalTwoCost.subtract(aditionalTmpTwoCost);
	        granTotalCalculate = granTotal.add(granToInsert);
	        info.addResult("inpgrandtotal", granTotalCalculate);
		    info.addResult("inpaditionalTwoCost", aditionalTwoCost);
		    info.addResult("inpaditionalTwoTmpCost", aditionalTwoCost);
		   
	    	
	    }else if("inpaditionalThreeCost".equals(lastFieldChanged) && aditionalThreeCost != null){
		    System.out.println("TERCERO");

	    	granToInsert = aditionalThreeCost.subtract(aditionalTmpThreeCost);
	        granTotalCalculate = granTotal.add(granToInsert);
	        info.addResult("inpgrandtotal", granTotalCalculate);
		    info.addResult("inpaditionalThreeCost", aditionalThreeCost);
		    info.addResult("inpaditionalThreeTmpCost", aditionalThreeCost);
		   
		    
	    }
     */
     
     
  }
}
