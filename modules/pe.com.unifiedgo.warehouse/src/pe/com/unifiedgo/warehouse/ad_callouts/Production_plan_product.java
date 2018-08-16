package pe.com.unifiedgo.warehouse.ad_callouts;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;

import org.apache.fop.fo.FObj;
import org.apache.tools.ant.taskdefs.Replace;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.service.OBCriteria;
//import org.apache.jasper.tagplugins.jstl.core.Out;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;

import pe.com.unifiedgo.core.data.SCRComboItem;
import pe.com.unifiedgo.warehouse.SWA_Utils;
import pe.com.unifiedgo.warehouse.data.swaProductWarehouse;

import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

public class Production_plan_product extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    
    /*Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
    { System.out.println(params.nextElement()); }*/
	    Locator locatorPlan = null; 
    
        String chkArmadoDesarmado =   info.vars.getStringParameter("inpemSwaDesarmado");
        String WarehouseID =   info.vars.getStringParameter("inpemSwaMWarehouseId");
        String AdClientId =  info.vars.getStringParameter("inpadClientId");
        String ProductID =   info.vars.getStringParameter("inpmProductId");
        String productionID =   info.vars.getStringParameter("inpmProductionId");
        
        
        BigDecimal qtyavailable = BigDecimal.ZERO;
        BigDecimal priceSoles = BigDecimal.ZERO;
        BigDecimal priceDollar = BigDecimal.ZERO;
       
        Warehouse warehouse = OBDal.getInstance().get(Warehouse.class, WarehouseID.trim());
        Product product =  OBDal.getInstance().get(Product.class, ProductID.trim());
        
        Currency CurrencySoles = OBDal.getInstance().get(Currency.class, "308");
        Currency CurrencyDollar = OBDal.getInstance().get(Currency.class, "100");
        
        qtyavailable = SWA_Utils.getWarehouseStockInfo(warehouse, null, product, 3);
        
        priceSoles = SWA_Utils.getLastPriceListFromCurrency(CurrencySoles, product);
        priceDollar = SWA_Utils.getLastPriceListFromCurrency(CurrencyDollar, product);
        
        info.addResult("inpemSwaQtyavailable", qtyavailable);
        
        info.addResult("inpemSwaPricelistSoles", priceSoles);
        info.addResult("inpemSwaPricelistDollar", priceDollar);
        
  }   
  
  
  
  
 
  
  
}