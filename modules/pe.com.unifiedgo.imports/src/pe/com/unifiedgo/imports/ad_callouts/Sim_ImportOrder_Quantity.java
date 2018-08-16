package pe.com.unifiedgo.imports.ad_callouts;

import java.math.BigDecimal;
import java.util.Enumeration;

import javax.servlet.ServletException;

import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.plm.Product;

public class Sim_ImportOrder_Quantity extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

   //  Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
   //  { System.out.println(params.nextElement()); }

	  
    String mProductActual = info.vars.getStringParameter("inppriceactual").replace(",", "");
    String mProductqty = info.vars.getStringParameter("inpqtyordered").replace(",", "");
    String strPriceList = info.vars.getStringParameter("inppricelist").replace(",", "");
   // String strUnitPrice = info.vars.getStringParameter("inppricelist").replace(",", "");
    String mProductId = info.vars.getStringParameter("inpmProductId");

    // List Price
    Float discount, priceList, priceActual;
  //  BigDecimal discount = BigDecimal.ZERO;
  //  BigDecimal priceList = BigDecimal.ZERO;
   // BigDecimal priceActual = BigDecimal.ZERO;
    
    Float product;
    if (mProductActual==null || mProductActual.equals("")) {
      mProductActual = "0";
      info.addResult("inppriceactual", Float.parseFloat(mProductActual));
     // return;
    }
    
    if (mProductqty == null || mProductqty.equals("")) {
    	mProductqty = "0";
    	info.addResult("inpqtyordered", Float.parseFloat(mProductqty));
     // return;
    }
    
    
    
    
    
    
    BigDecimal preciounitario = BigDecimal.ZERO;
    preciounitario = new BigDecimal(mProductActual);
    BigDecimal qtyOrdered = BigDecimal.ZERO;
    qtyOrdered= new BigDecimal(mProductqty);
    BigDecimal precioDeLinea = BigDecimal.ZERO;
    precioDeLinea = qtyOrdered.multiply(preciounitario);
    info.addResult("inplinenetamt", precioDeLinea);
    
    
    
    
   // System.out.println("ENTRA");
    
    int tmp=0;
    Product prd = OBDal.getInstance().get(Product.class, mProductId);
    BigDecimal unitPerBox = BigDecimal.ZERO;
    String mlineimportQty = info.vars.getStringParameter("inplineimportQty").replace(",", "");
    String mlineimportPrice = info.vars.getStringParameter("inplineimportPrice").replace(",", "");
    
    if(prd.getScrUnitsperbox()==null)
    	tmp=1;
    else if(prd.getScrUnitsperbox()==0)
    	tmp=1;
    else
    	unitPerBox = new BigDecimal(prd.getScrUnitsperbox());
    
    if(tmp==0){
    	BigDecimal lineQtyImport = BigDecimal.ZERO;
    	BigDecimal Qtytotal = BigDecimal.ZERO;
    	BigDecimal qtyToProduct = BigDecimal.ZERO;
    	 
    	lineQtyImport = new BigDecimal(mlineimportQty);
    	Qtytotal =lineQtyImport.multiply(unitPerBox);
    	qtyToProduct = new BigDecimal(mProductqty);
    	
    	if(Qtytotal.compareTo(qtyToProduct)!=0){
    		BigDecimal qtyToUpdate = BigDecimal.ZERO;
    		qtyToUpdate = qtyToProduct.divide(unitPerBox);
    		info.addResult("inplineimportQty", qtyToUpdate);
    	}
    	
    	BigDecimal lineProductPrice = BigDecimal.ZERO;
    	BigDecimal priceToProduct = BigDecimal.ZERO;
    	BigDecimal Pricetotal = BigDecimal.ZERO;
    	
    	lineProductPrice = new BigDecimal(mProductActual);
    	Pricetotal = lineProductPrice.multiply(unitPerBox);
    	priceToProduct = new BigDecimal(mlineimportPrice);
    	
    	if(Pricetotal.compareTo(priceToProduct)!=0){
    		BigDecimal priceToUpdate = BigDecimal.ZERO;
    		priceToUpdate = lineProductPrice.multiply(unitPerBox);
    		info.addResult("inplineimportPrice", priceToUpdate);
    		
    	}

    }
    
    
    
    BigDecimal qtyProduct = BigDecimal.ZERO;
    BigDecimal unitPriceProduct = BigDecimal.ZERO;
    BigDecimal linenetamt = BigDecimal.ZERO; 
    
    qtyProduct = new BigDecimal(mProductqty);
    
  //  System.out.println("qtyProduct: " + qtyProduct + "VunitPriceProduct: "+ unitPriceProduct);
    unitPriceProduct = new BigDecimal(mProductActual);
    
    
    linenetamt = qtyProduct.multiply(unitPriceProduct);
    
   // product = (Float.parseFloat(mProductqty) * Float.parseFloat(mProductActual));
   // info.addResult("inplinenetamt", linenetamt);
    //info.addResult("inpqtyreserved", Integer.parseInt(mProductqty));
    info.addResult("inpqtyreserved", Float.parseFloat(mProductqty));

    priceList = Float.parseFloat(strPriceList);
    priceActual = Float.parseFloat(mProductActual);

    
    
    
    
    
    if (priceList != 0) {
      discount = ((priceList - priceActual) / priceList) * 100;
    } else {
    	info.addResult("inppricelist", priceActual);	
        discount = new Float(0);
    }
    info.addResult("inpdiscount", discount);
  }

}
