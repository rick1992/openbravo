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
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.order.Order;

import java.util.Enumeration;
import java.util.Locale;

public class swa_product_internalmovement extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    
  //  Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
  //  { System.out.println(params.nextElement()); } 
    
   // String 
    
	//    String mLocator = info.vars.getStringParameter("inpmLocatorId").replace(",", ""); inpmLocatorId
	//    Locator locator = OBDal.getInstance().get(Locator.class, mLocator.trim());
	//    info.addResult("inpemSwaMWarehouseId", locator.getWarehouse().getId());
  }
}