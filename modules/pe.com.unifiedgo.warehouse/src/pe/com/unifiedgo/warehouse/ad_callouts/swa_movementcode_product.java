package pe.com.unifiedgo.warehouse.ad_callouts;

import java.math.BigDecimal;
import java.util.Enumeration;

import javax.servlet.ServletException;

import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.plm.Product;

import pe.com.unifiedgo.accounting.SCO_Utils;
import pe.com.unifiedgo.warehouse.SWA_Utils;

//import org.apache.jasper.tagplugins.jstl.core.Out;

public class swa_movementcode_product extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

 //    Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
 //    { System.out.println(params.nextElement()); }

    // ComboItem SCRComboItem = OBDal.getInstance().get(SCRComboItem-class, id)
    String madOrgId = info.vars.getStringParameter("inpadOrgId");
    String madClientId = info.vars.getStringParameter("inpadClientId");
    String mProductId = info.vars.getStringParameter("inpmProductId");
    String mWarehouseId = info.vars.getStringParameter("inpmWarehouseId");
    String mLocatorId = info.vars.getStringParameter("inpmLocatorId");
    String mLocatorToId = info.vars.getStringParameter("inpmToLocatorId");
    

    Product product = OBDal.getInstance().get(Product.class, mProductId.trim());
    if(product != null){
      info.addResult("inpcUomId", product.getUOM().getId());
      
      Warehouse warehouse = OBDal.getInstance().get(Warehouse.class, mWarehouseId.trim());
      Locator locator = OBDal.getInstance().get(Locator.class, mLocatorId.trim());
     // System.out.println("HOLAS");
      if(locator != null && warehouse != null){
    	  BigDecimal qtyOnHand = BigDecimal.ZERO;
    	  BigDecimal qtyReserved = BigDecimal.ZERO;
    	  BigDecimal qtyAvailable = BigDecimal.ZERO;
    	  
    	  qtyOnHand = SWA_Utils.getWarehouseStockInfo(warehouse, locator, product, 4);
    	  qtyReserved = SWA_Utils.getWarehouseStockInfo(warehouse, locator, product, 5);
    	  qtyAvailable = SWA_Utils.getWarehouseStockInfo(warehouse, locator, product, 6);
    	  
    	 // System.out.println("inpqtyonhand: " + qtyOnHand);
    	//  System.out.println("qtyReserved: " + qtyReserved);
    	  
    	  
    	  info.addResult("inpqtyonhand", qtyOnHand);
		  info.addResult("inpqtyreserved", qtyReserved);
		  info.addResult("inpqtyavailable", qtyAvailable);
		  info.addResult("inpqtyordered", qtyAvailable);
    	  
		  info.addResult("inpmToLocatorId", locator.getId());
      }
      else{
    	  BigDecimal qtyAvailable = BigDecimal.ZERO;
    	  qtyAvailable = SWA_Utils.getWarehouseStockInfo(warehouse, null, product, 3);
    	  info.addResult("inpqtyavailable", qtyAvailable);
      }
    }

  }
  
  

}