package pe.com.unifiedgo.imports.ad_callouts;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Enumeration;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.plm.Product;

public class Sim_ImportOrderLine_Calculate extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    // Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
    // { System.out.println(params.nextElement()); }
	    VariablesSecureApp vars = info.vars;
	    String lastFieldChanged = info.getLastFieldChanged();

	    
	    String strPriceActual = info.vars.getStringParameter("inppriceactual").replace(",", "");
	    String strQtyOrdered = info.vars.getStringParameter("inpqtyordered").replace(",", "");
	    String strPriceList = info.vars.getStringParameter("inppricelist").replace(",", "");
	    String strQtyOrderedBox = info.vars.getStringParameter("inplineimportQty").replace(",", "");
	    String strPriceActualBox = info.vars.getStringParameter("inplineimportPrice").replace(",", "");
	    String strDiscount = info.vars.getStringParameter("inpdiscount").replace(",", "");
	    String strProductId = info.vars.getStringParameter("inpmProductId");
	    
	    
	    BigDecimal priceActual = new BigDecimal((strPriceActual.equals(""))?"0": strPriceActual );
	    BigDecimal qtyOrder = new BigDecimal((strQtyOrdered.equals(""))?"0": strQtyOrdered );
	    BigDecimal priceList = new BigDecimal((strPriceList.equals(""))?"0": strPriceList );
	    BigDecimal qtyOrderBox = new BigDecimal((strQtyOrderedBox.equals(""))?"0": strQtyOrderedBox );
	    BigDecimal priceActualBox = new BigDecimal((strPriceActualBox.equals(""))?"0": strPriceActualBox);
	    BigDecimal discount = new BigDecimal((strDiscount.equals(""))?"0": strDiscount);    
	    BigDecimal unitPerBox = BigDecimal.ZERO;
	    BigDecimal lineNetAmt = BigDecimal.ZERO;
	    
	    Product product = OBDal.getInstance().get(Product.class, strProductId);
	    
	    if ("inplineimportQty".equals(lastFieldChanged) && product != null) {
	    	
	    	
	    	if(product.getScrUnitsperbox()==null || product.getScrUnitsperbox()==0)
	    		unitPerBox= new BigDecimal(0);
	    	else
	    		unitPerBox= new BigDecimal(product.getScrUnitsperbox());
	    	
	    	qtyOrder = qtyOrderBox.multiply(unitPerBox);
	    	
	    	//priceActualBox = (qtyOrderBox.compareTo(BigDecimal.ZERO)==0)?BigDecimal.ZERO:lineNetAmt.divide(qtyOrderBox, 5 ,RoundingMode.CEILING);
	    	
	    	lineNetAmt = priceActualBox.multiply(qtyOrderBox);
	    	priceActual = (qtyOrder.compareTo(BigDecimal.ZERO)==0)?BigDecimal.ZERO:lineNetAmt.divide(qtyOrder, 5 ,RoundingMode.CEILING);
	    	
	    	
	    	info.addResult("inpqtyordered", qtyOrder);
	    	info.addResult("inpqtyreserved", qtyOrder);
	    	
	    	info.addResult("inplineimportPrice", priceActualBox);
	    	info.addResult("inplinenetamt", lineNetAmt);
	    	info.addResult("inppriceactual", priceActual);
	    
	    }else if("inplineimportPrice".equals(lastFieldChanged) && product != null){
	    	
	    	if(product.getScrUnitsperbox()==null || product.getScrUnitsperbox()==0)
	    		unitPerBox= new BigDecimal(0);
	    	else
	    		unitPerBox= new BigDecimal(product.getScrUnitsperbox());
	    	
	    	
	    	lineNetAmt = priceActualBox.multiply(qtyOrderBox);
	    	priceActual = (qtyOrder.compareTo(BigDecimal.ZERO)==0)?BigDecimal.ZERO:lineNetAmt.divide(qtyOrder, 5 ,RoundingMode.CEILING);
	    	

	    	info.addResult("inppriceactual", priceActual);
	    	info.addResult("inplinenetamt", lineNetAmt);
	    	
	    	
	    }else if("inpqtyordered".equals(lastFieldChanged) && product != null){
	    	
	    	
	    	if(product.getScrUnitsperbox()==null || product.getScrUnitsperbox()==0)
	    		unitPerBox= new BigDecimal(0);
	    	else
	    		unitPerBox= new BigDecimal(product.getScrUnitsperbox());
	    	
	    	qtyOrderBox = (unitPerBox.compareTo(BigDecimal.ZERO)==0)?BigDecimal.ZERO:qtyOrder.divide(unitPerBox, RoundingMode.HALF_DOWN);
	    	
	    	//priceActualBox = priceActual.multiply(unitPerBox);
	    	lineNetAmt = priceActual.multiply(qtyOrder);
	    	//priceActualBox = lineNetAmt.divide(qtyOrderBox, 5 ,RoundingMode.CEILING);
	    	priceActualBox = (qtyOrderBox.compareTo(BigDecimal.ZERO)==0)?BigDecimal.ZERO:lineNetAmt.divide(qtyOrderBox, 5 ,RoundingMode.CEILING);

	    	
	    	info.addResult("inpqtyreserved", qtyOrder);
	    	info.addResult("inplineimportQty", qtyOrderBox);
	    	info.addResult("inplineimportPrice", priceActualBox);
	    	info.addResult("inplinenetamt", lineNetAmt);
	    		
	    }else if("inppricelist".equals(lastFieldChanged) && product != null){

	    	if(priceActual.compareTo(BigDecimal.ZERO)==0){
	    		priceActual = priceList;
	    		
	    		if(product.getScrUnitsperbox()==null || product.getScrUnitsperbox()==0)
		    		unitPerBox= new BigDecimal(0);
		    	else
		    		unitPerBox= new BigDecimal(product.getScrUnitsperbox());
	    		
	    		
	    		//priceActualBox = priceActual.multiply(unitPerBox);
		    	lineNetAmt = priceActual.multiply(qtyOrder);
		    	//priceActualBox = lineNetAmt.divide(qtyOrderBox, 5 ,RoundingMode.CEILING);
		    	priceActualBox = (qtyOrderBox.compareTo(BigDecimal.ZERO)==0)?BigDecimal.ZERO:lineNetAmt.divide(qtyOrderBox, 5 ,RoundingMode.CEILING);

		    	info.addResult("inppriceactual", priceActual);
		    	info.addResult("inplineimportPrice", priceActualBox);
		    	info.addResult("inplinenetamt", lineNetAmt);

	    	}
	    	
	    	
	    	
	    }else if("inppriceactual".equals(lastFieldChanged) && product != null){
	    	if(product.getScrUnitsperbox()==null || product.getScrUnitsperbox()==0)
	    		unitPerBox= new BigDecimal(0);
	    	else
	    		unitPerBox= new BigDecimal(product.getScrUnitsperbox());
	    	
	    	//priceActualBox = priceActual.multiply(unitPerBox);
	    	lineNetAmt = priceActual.multiply(qtyOrder);
	    	//priceActualBox = lineNetAmt.divide(qtyOrderBox, 5 ,RoundingMode.CEILING);
	    	priceActualBox = (qtyOrderBox.compareTo(BigDecimal.ZERO)==0)?BigDecimal.ZERO:lineNetAmt.divide(qtyOrderBox, 5 ,RoundingMode.CEILING);

	    	info.addResult("inplineimportPrice", priceActualBox);
	    	info.addResult("inplinenetamt", lineNetAmt);
	    	
	    	
	    }else if("inpdiscount".equals(lastFieldChanged) && product != null){
	    	
	    }

	    
	   
	  
  }

}
