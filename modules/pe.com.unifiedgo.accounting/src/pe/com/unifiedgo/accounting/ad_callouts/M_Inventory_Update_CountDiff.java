package pe.com.unifiedgo.accounting.ad_callouts;

import java.math.BigDecimal;
import java.util.Enumeration;

import javax.servlet.ServletException;

import org.openbravo.erpCommon.ad_callouts.SimpleCallout;

public class M_Inventory_Update_CountDiff extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    
      /*Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
      { System.out.println(params.nextElement()); }*/
     

    String strFirstCount = info.getStringParameter("inpemScoFirstcount", null);
    String strSecondCount = info.getStringParameter("inpemScoSecondcount", null);
    String strThirdCount = info.getStringParameter("inpemSwaThirdcount", null);
    String strchkCyclicInventory = info.getStringParameter("inpemSwaCyclicinventory", null);
    String strinpqtybook = info.getStringParameter("inpqtybook", null);
    
  
    if (strFirstCount == null) {
      strFirstCount = "0";
    } else if (strFirstCount.equals("")) {
      strFirstCount = "0";
    }
    if (strSecondCount == null) {
      strSecondCount = "0";
    } else if (strSecondCount.equals("")) {
      strSecondCount = "0";
    }
    if (strThirdCount == null) {
    	strThirdCount = "0";
    } else if (strThirdCount.equals("")) {
    	strThirdCount = "0";
    }
    if(strchkCyclicInventory == null){
    	strchkCyclicInventory = "N";
    }
    if(strinpqtybook == null){
    	strinpqtybook = "0";
    }

    strFirstCount = strFirstCount.replace(",", "");
    strSecondCount = strSecondCount.replace(",", "");
    strinpqtybook = strinpqtybook.replace(",", "");

    if(strchkCyclicInventory != null && strchkCyclicInventory.equals("Y"))
    	 CyclicInventory(info, strinpqtybook, strFirstCount);
    else
    	GeneralInventory(info, strFirstCount, strSecondCount, strThirdCount);
    
   
   /* BigDecimal firstcount = new BigDecimal(strFirstCount);
    BigDecimal secondcount = new BigDecimal(strSecondCount);

    BigDecimal countdiff = firstcount.subtract(secondcount);
    if (countdiff != null) {
      info.addResult("inpemScoCountdiff", countdiff.toPlainString());
      if (countdiff.compareTo(new BigDecimal(0)) == 0) {
        info.addResult("inpqtycount", strFirstCount);
      } else {
        //info.addResult("inpqtycount", "0");
        info.addResult("inpqtycount", strThirdCount);
      }
    } else {
      info.addResult("inpemScoCountdiff", "");
    }
    */
    
  }
  
  
  protected void GeneralInventory(CalloutInfo info, String strFirstCount, String strSecondCount, String strThirdCount){
	    BigDecimal firstcount = new BigDecimal(strFirstCount);
	    BigDecimal secondcount = new BigDecimal(strSecondCount);

	    BigDecimal countdiff = firstcount.subtract(secondcount);
	    if (countdiff != null) {
	      info.addResult("inpemScoCountdiff", countdiff.toPlainString());
	      if (countdiff.compareTo(new BigDecimal(0)) == 0) {
	        info.addResult("inpqtycount", strFirstCount);
	      } else {
	        //info.addResult("inpqtycount", "0");
	        info.addResult("inpqtycount", strThirdCount);
	      }
	    } else {
	      info.addResult("inpemScoCountdiff", "");
	    }   
  }
  
  protected void CyclicInventory(CalloutInfo info,String strinpqtybook, String strFirstCount){
	    BigDecimal qtyBook = new BigDecimal(strinpqtybook);
	    BigDecimal firstcount = new BigDecimal(strFirstCount);

	    BigDecimal countdiff = qtyBook.subtract(firstcount);
	    if (countdiff != null) {
	      info.addResult("inpemScoCountdiff", countdiff.toPlainString());
	        //info.addResult("inpqtycount", "0");
	        info.addResult("inpqtycount", strFirstCount);
	    } else {
	      info.addResult("inpemScoCountdiff", "");
	    }  
  }

}
