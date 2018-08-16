package pe.com.unifiedgo.warehouse.ad_callouts;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;

import org.apache.fop.fo.FObj;
import org.apache.tools.ant.taskdefs.Replace;
//import org.apache.jasper.tagplugins.jstl.core.Out;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.order.Order;

import java.util.Enumeration;
import java.util.Locale;

public class swa_warehouse_reposition extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    
 //   Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
 //   { System.out.println(params.nextElement()); }
	  
		
		String mToWarehousetId = info.vars.getStringParameter("inptoMWarehouseId");
		String mFromWarehouseId =  info.vars.getStringParameter("inpfromMWarehouseId");
		
		Warehouse to_ware = OBDal.getInstance().get(Warehouse.class, mToWarehousetId.trim());
		Warehouse from_ware = OBDal.getInstance().get(Warehouse.class, mFromWarehouseId.trim());
		
		if(to_ware != null && from_ware != null){
			if(to_ware.getSwaWarehousetype().equals("CO") || to_ware.getSwaWarehousetype().equals("CO"))
			  info.addResult("inpwarehouseType", "CO");
			else{
   			  info.addResult("inpwarehouseType", from_ware.getSwaWarehousetype());
   			}
			info.addResult("inptowarehousetype",to_ware.getSwaWarehousetype());
		}
		

  }
}