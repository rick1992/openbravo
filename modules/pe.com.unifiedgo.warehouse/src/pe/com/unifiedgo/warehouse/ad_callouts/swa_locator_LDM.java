

package pe.com.unifiedgo.warehouse.ad_callouts;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;

import org.hibernate.Query;
//import org.apache.jasper.tagplugins.jstl.core.Out;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;

import pe.com.unifiedgo.core.data.SCRComboItem;

import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

public class swa_locator_LDM extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    
  //  Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
  //  { System.out.println(params.nextElement()); }
	    
    
        String chkArmadoDesarmado =   info.vars.getStringParameter("inpemSwaDesarmado");
        String WarehouseID =   info.vars.getStringParameter("inpemSwaMWarehouseId");
        String AdClientId =  info.vars.getStringParameter("inpadClientId");
        String ProductID =   info.vars.getStringParameter("inpmProductId");
        String productionID =   info.vars.getStringParameter("inpmProductionId");
        String LocatorID = info.vars.getStringParameter("inpmLocatorId");
        
        
        
        BigDecimal qtyonLocator = BigDecimal.ZERO;
        BigDecimal qtyReservedLocator = BigDecimal.ZERO; 
        
        
        Warehouse warehouse = OBDal.getInstance().get(Warehouse.class, WarehouseID.trim());
        Locator locator = OBDal.getInstance().get(Locator.class, LocatorID.trim());
        Product product =  OBDal.getInstance().get(Product.class, ProductID.trim());
        
        try {
        	qtyonLocator = getQty(warehouse, locator, product, 2);
            qtyReservedLocator = getQty(warehouse, locator, product, 4);	
		} catch (Exception e) {
			// TODO: handle exception
		}
        
         
        //QTY ON HAND BY LOCATOR
        info.addResult("inpemSwaQtyonhandStorage", qtyonLocator);
        //QTY RESERVED LOCATOR
        info.addResult("inpemSwaQtyreservedStorage", qtyReservedLocator);
        
        
  }   
 
  private BigDecimal getQty(Warehouse warehouse, Locator locator, Product product, int x){
	  BigDecimal val =BigDecimal.ZERO;
	  String SqlQuery = "";
	  if(x==1){//GET QTY TOTAL
		  SqlQuery= " SELECT sum(qtyonhand) FROM swa_product_warehouse_v  "
		  		  + "WHERE m_product_id = '" + product.getId()
		  		  + "' AND m_warehouse_id = '" + warehouse.getId() + "'";
		  
	  }
	  else if(x==2){//Get QTY on Locator FISICA
		  SqlQuery=" SELECT sum(totalqty) FROM swa_product_by_anaquel_v "
		  		+ "  WHERE M_PRODUCT_ID = '"+ product.getId() + "' "
		  		+ "    AND m_warehouse_id = '"+ warehouse.getId() + "' "
		  	    + "    AND M_LOCATOR_ID = '"+ locator.getId() + "' "
		  	    + " GROUP BY m_locator_id ";
	  }
	  else if(x==3){//Get Qty Total Reserved 
		  SqlQuery=" SELECT sum(qtyreserved) FROM swa_product_warehouse_v "
			  		+ "  WHERE M_PRODUCT_ID = '"+ product.getId() + "' "
			  	    + "    AND m_warehouse_id = '"+ warehouse.getId() + "' ";
		  
	  }
	  else if(x==4){//Get Qty Reserved on Locator
		  SqlQuery=" SELECT sum(reserved)  "
			  		+ "  FROM swa_product_by_anaquel_v  "
			  	    + "  WHERE M_PRODUCT_ID = '"+ product.getId() + "' "
			  	    + "    AND m_warehouse_id = '"+ warehouse.getId() + "' "
			  	    + "    AND m_locator_id  = '"+ locator.getId()  + "' "
		            + " GROUP BY m_locator_id ";
	  }
	  
	  try {
		 Query q = OBDal.getInstance().getSession().createSQLQuery(SqlQuery);
		 BigDecimal result = (BigDecimal) q.uniqueResult();
		 if (result != null) {
		        return result;
		 }
		 return val;
	  } catch (Exception e) {
		return val;
	  }
  }
  
}