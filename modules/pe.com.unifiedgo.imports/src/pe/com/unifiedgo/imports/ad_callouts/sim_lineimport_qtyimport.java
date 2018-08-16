package pe.com.unifiedgo.imports.ad_callouts;

import java.math.BigDecimal;
import java.util.Enumeration;

import javax.servlet.ServletException;

import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.plm.Product;

public class sim_lineimport_qtyimport extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    // Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
    // { System.out.println(params.nextElement()); }

	    String mlineimportQty = info.vars.getStringParameter("inplineimportQty").replace(",", "");
	    String mProductId = info.vars.getStringParameter("inpmProductId");
	   
	    
	    BigDecimal lineQtyImport = BigDecimal.ZERO;
	    BigDecimal unitPerBox = BigDecimal.ZERO;
	    BigDecimal totalqty = BigDecimal.ZERO;
	    
	    if(mlineimportQty==null || mlineimportQty.equals("")){
	    	mlineimportQty="1";
	    	info.addResult("inplineimportQty", mlineimportQty);
	    }
	    if(mlineimportQty.equals("0")){
	    	mlineimportQty="1";
	    	info.addResult("inplineimportQty", mlineimportQty);
	    }
	    
	    lineQtyImport = new BigDecimal(mlineimportQty);
	    
	    Product product = OBDal.getInstance().get(Product.class, mProductId);
	    if(product.getScrUnitsperbox()==null)
	    	unitPerBox = new BigDecimal("1");
	    else if(product.getScrUnitsperbox()==0)
	    	unitPerBox = new BigDecimal("1");
	    else
	    	unitPerBox = new BigDecimal(product.getScrUnitsperbox());
	    
	    totalqty = unitPerBox.multiply(lineQtyImport);
	    
	    info.addResult("inpqtyordered", totalqty);
	  
  }

}
