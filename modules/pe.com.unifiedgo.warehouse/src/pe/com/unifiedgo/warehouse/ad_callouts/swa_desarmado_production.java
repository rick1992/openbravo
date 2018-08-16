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

import pe.com.unifiedgo.core.data.SCRComboItem;

import java.util.Enumeration;
import java.util.Locale;

public class swa_desarmado_production extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    
   // Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
   // { System.out.println(params.nextElement()); }
		
    
       // ComboItem  SCRComboItem = OBDal.getInstance().get(SCRComboItem-class, id)
		/*String mToWarehousetId = info.vars.getStringParameter("inpfromMWarehouseId");
		String mFromWarehouseId =  info.vars.getStringParameter("inptoMWarehouseId");
		
		Warehouse to_ware = OBDal.getInstance().get(Warehouse.class, mToWarehousetId.trim());
		Warehouse from_ware = OBDal.getInstance().get(Warehouse.class, mFromWarehouseId.trim());
		
		if(to_ware != null && from_ware != null){
			if(from_ware.getSwaWarehousetype().equals("CO") || from_ware.getSwaWarehousetype().equals("CO"))
			  info.addResult("inpwarehouseType", "CO");
			else
   			  info.addResult("inpwarehouseType", to_ware.getSwaWarehousetype());
		}*/
  }
}