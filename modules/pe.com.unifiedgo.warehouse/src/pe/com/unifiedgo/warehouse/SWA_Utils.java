package pe.com.unifiedgo.warehouse;

import java.math.BigDecimal;

import org.hibernate.Query;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.plm.Product;



public class SWA_Utils {
	
	
	
 public static BigDecimal getLastPriceListFromCurrency(Currency curency,Product product ){
	 BigDecimal val = BigDecimal.ZERO;
	   
	 String SqlQuery = "";
	 SqlQuery= " SELECT COALESCE(sum(spc.pricelist),0) FROM ssa_product_currprice_v spc "
	  		  + " JOIN m_pricelist mpl ON spc.m_pricelist_id = mpl.m_pricelist_id "
	  		  + " WHERE mpl.issopricelist = 'Y' AND mpl.c_currency_id = '"+ curency.getId()
	  		  + "' AND spc.m_product_id = '" + product.getId() + "'";
	  
	 
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


  public static BigDecimal getWarehouseStockInfo(Warehouse warehouse, Locator locator, Product product, int x) {
	  
	/* x = 1 ; Retorna la Cantidad Física     de un Producto en el Almacén
	   x = 2 ; Retorna la Cantidad Reservada  de un Producto en el Almacén
	   x = 3 ; Retorna la Cantidad Disponible de un Producto en el Almacén
	   x = 4 ; Retorna la Cantidad Física     de un Producto en un Almacén y una Ubicación Determinada
	   x = 5 ; Retorna la Cantidad Reservada  de un Producto en el Almacén y una Ubicación Determinada
	   x = 6 ; Retorna la Cantidad Disponible de un Producto en el Almacén y una Ubicación Determinada
	*/
	  
	  
	  BigDecimal val = BigDecimal.ZERO;
	  String SqlQuery = "";
	  if(x==1){
		  
		 if(product == null || warehouse == null)
			  return val;
		  
		  SqlQuery= " SELECT COALESCE(sum(qtyonhand),0) FROM swa_product_warehouse_v  "
		  		  + "WHERE m_product_id = '" + product.getId()
		  		  + "' AND m_warehouse_id = '" + warehouse.getId() + "'";
		  
	  }
	  else if(x==2){
		  
		  if(product == null || warehouse == null)
			  return val;
		  
		  SqlQuery=" SELECT COALESCE(sum(qtyreserved),0) FROM swa_product_warehouse_v "
			  		+ "  WHERE M_PRODUCT_ID = '"+ product.getId() + "' "
			  	    + "    AND m_warehouse_id = '"+ warehouse.getId() + "' ";
		  
	  }
	  else if(x==3){
		  
		  if(product == null || warehouse == null)
			  return val;
		  
		  SqlQuery= " SELECT COALESCE(sum(totalqty),0) FROM swa_product_warehouse_v  "
		  		  + "WHERE m_product_id = '" + product.getId()
		  		  + "' AND m_warehouse_id = '" + warehouse.getId() + "'";
		 
		  
	  }
	  else if(x==4){
		  
		  if(product == null || warehouse == null || locator==null)
			  return val;
		  
		  SqlQuery=" SELECT COALESCE(sum(totalqty),0) FROM swa_product_by_anaquel_v "
		  		+ "  WHERE M_PRODUCT_ID = '"+ product.getId() + "' "
		  		+ "    AND m_warehouse_id = '"+ warehouse.getId() + "' "
		  	    + "    AND M_LOCATOR_ID = '"+ locator.getId() + "' "
		  	    + " GROUP BY m_locator_id ";
		  
		
	  }else if(x==5){
		 
		  if(product == null || warehouse == null || locator==null)
			  return val;
		  
		  SqlQuery=" SELECT COALESCE(sum(reserved),0)  "
			  		+ "  FROM swa_product_by_anaquel_v  "
			  	    + "  WHERE M_PRODUCT_ID = '"+ product.getId() + "' "
			  	    + "    AND m_warehouse_id = '"+ warehouse.getId() + "' "
			  	    + "    AND m_locator_id  = '"+ locator.getId()  + "' "
		            + " GROUP BY m_locator_id ";
		 
	  }else if(x==6){
		  
		  if(product == null || warehouse == null || locator==null)
			  return val;
		  
		  SqlQuery=" SELECT COALESCE(sum(qtyonhand),0)  "
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
