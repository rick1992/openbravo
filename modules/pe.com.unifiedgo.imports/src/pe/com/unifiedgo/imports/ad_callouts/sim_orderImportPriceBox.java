package pe.com.unifiedgo.imports.ad_callouts;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Enumeration;

import javax.servlet.ServletException;

import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.plm.Product;

public class sim_orderImportPriceBox extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    // Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
    // { System.out.println(params.nextElement()); }

	    String mlineimportPrice = info.vars.getStringParameter("inplineimportPrice").replace(",", "");
	    String mProductId = info.vars.getStringParameter("inpmProductId");
	   
	    
	    BigDecimal linePriceImport = BigDecimal.ZERO;
	    BigDecimal unitPerBox = BigDecimal.ZERO;
	    BigDecimal totalPrice = BigDecimal.ZERO;
	    
	    if(mlineimportPrice==null || mlineimportPrice.equals("")){
	    	mlineimportPrice="1";
	    	info.addResult("inplineimportPrice", mlineimportPrice);
	    }
	    if(mlineimportPrice.equals("0")){
	    	mlineimportPrice="1";
	    	info.addResult("inplineimportPrice", mlineimportPrice);
	    }
	    
	    linePriceImport = new BigDecimal(mlineimportPrice);
	    
	    Product product = OBDal.getInstance().get(Product.class, mProductId);
	    if(product.getScrUnitsperbox()==null)
	    	unitPerBox = new BigDecimal("1");
	    else if(product.getScrUnitsperbox()==0)
	    	unitPerBox = new BigDecimal("1");
	    else
	    	unitPerBox = new BigDecimal(product.getScrUnitsperbox());
	    
	    totalPrice = linePriceImport.divide(unitPerBox,3,RoundingMode.HALF_UP);
	    
	    info.addResult("inppriceactual", totalPrice);
	  
  }

}
