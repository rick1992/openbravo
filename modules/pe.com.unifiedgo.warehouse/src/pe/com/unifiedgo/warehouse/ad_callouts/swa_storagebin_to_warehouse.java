package pe.com.unifiedgo.warehouse.ad_callouts;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.apache.fop.fo.FObj;
import org.apache.tools.ant.taskdefs.Replace;
import org.hibernate.Query;
//import org.apache.jasper.tagplugins.jstl.core.Out;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.plm.Product;

import java.util.Enumeration;
import java.util.Locale;

public class swa_storagebin_to_warehouse extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    
   // Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
   // { System.out.println(params.nextElement()); }
	//    String mLocator = info.vars.getStringParameter("inpmLocatorId").replace(",", "");
	//    Locator locator = OBDal.getInstance().get(Locator.class, mLocator.trim());
	//    info.addResult("inpemSwaMWarehouseId", locator.getWarehouse().getId());
	  String strWarehouseId = info.vars.getStringParameter("inpemSwaMWarehouseId");
	  String strLocator = info.vars.getStringParameter("inpmLocatorId");
	  String strMProductID = info.vars.getStringParameter("inpmProductId");
	  
	  String strLocatorToID = info.vars.getStringParameter("inpmLocatortoId");
	  
	  
	  
	  Double qtyonPhysic = 0.0;
	  Double qtyReservedLocator = 0.0;
	  Double qtyavailable = 0.0;
	  
	  Warehouse warehouseobj = OBDal.getInstance().get(Warehouse.class, strWarehouseId.trim());
	  Locator locatorObj = OBDal.getInstance().get(Locator.class, strLocator);
	  Product productobj = OBDal.getInstance().get(Product.class, strMProductID);
	  
	  if(warehouseobj != null && locatorObj != null && productobj!=null){
		  //qtyonPhysic = getQty(warehouseobj, locatorObj, productobj, 2);
		  qtyReservedLocator = getQty(null, locatorObj, productobj, 4);
		  qtyavailable = getQty(warehouseobj, locatorObj, productobj, 2);
		  
		 // info.addResult("inpmovementqty", qtyonPhysic);
		  info.addResult("inpemSwaQtyreservedStorage", qtyReservedLocator);
		  info.addResult("inpemSwaQtyavailableStorage", qtyavailable);
	  }
	  
	  if(strLocator.equals(strLocatorToID)){
		  info.addResult("inpmLocatortoId", null);
	  }
	  
  }
  
  private Double getQty(Warehouse warehouse, Locator locator, Product product, int x){
	  Double val = 0.0;
	  String SqlQuery = "";
	  if(x==1){//GET QTY TOTAL
		  SqlQuery= " SELECT sum(qtyonhand) FROM swa_product_warehouse_v  "
		  		  + "WHERE m_product_id = '" + product.getId()
		  		  + "' AND m_warehouse_id = '" + warehouse.getId() + "'";
		  
	  }
	  else if(x==2){//Get QTY on Locator
		  SqlQuery=" SELECT COALESCE(sum(qtyonhand),0) FROM swa_product_by_anaquel_v "
			  		+ "  WHERE M_PRODUCT_ID = '"+ product.getId() + "' "
			  	    + "    AND M_LOCATOR_ID = '"+ locator.getId() + "' "
			  	    + " GROUP BY m_locator_id ";
	  }
	  else if(x==3){//Get Qty Total Reserved 
		  SqlQuery=" SELECT sum(qtyreserved) FROM swa_product_warehouse_v "
			  		+ "  WHERE M_PRODUCT_ID = '"+ product.getId() + "' "
			  	    + "    AND m_warehouse_id = '"+ warehouse.getId() + "' ";
		  
	  }
	  else if(x==4){//Get Qty Reserved on Locator
		
		  
		  SqlQuery=" SELECT COALESCE(sum(reserved),0)"
			  		+ "  FROM swa_product_by_anaquel_v "
			  	    + "  WHERE M_PRODUCT_ID = '"+ product.getId() + "' "
			  	    + "    AND  m_locator_id  = '"+ locator.getId()  + "' ";
		  
		 // System.out.println(SqlQuery);
	  }
	  /*else if(x==5){
		  SqlQuery=" SELECT qtyonhand  "
			  		+ "  FROM swa_product_by_anaquel_v  "
			  	    + "  WHERE M_PRODUCT_ID = '"+ product.getId() + "' "
			  	    + "    AND  m_locator_id  = '"+ locator.getId()  + "' "
		            + "    AND  m_warehouse_id  = '"+ warehouse.getId()  + "' "
		            + "  LIMIT 1";
	  }*/
	  
	  try {
		 Query q = OBDal.getInstance().getSession().createSQLQuery(SqlQuery);
		 BigDecimal result = (BigDecimal) q.uniqueResult();
		 if (result != null) {
		        return result.doubleValue();
		 }
		 return 0.0;
	  } catch (Exception e) {
		return val;
	  }
  }
  
}