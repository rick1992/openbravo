

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

public class BillOfMaterialCheckDesarmado extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    
    //Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
    //{ System.out.println(params.nextElement()); }
    
    String lastFieldChanged = info.getLastFieldChanged();

        
    String strchkArmadoDesarmado =   info.vars.getStringParameter("inpemSwaDesarmado");

    
    if ("inpemSwaDesarmado".equals(lastFieldChanged)) {
    	
    	if(strchkArmadoDesarmado==null || strchkArmadoDesarmado.equals(""))
    		strchkArmadoDesarmado="N";
    	
	    if(strchkArmadoDesarmado.equals("Y")){
	    	info.addResult("inpemSwaServiceorderId", null);
	    	info.addResult("inpemSwaMWarehouseId", null);
	    }
    }    
	    
    if ("inpemSwaServiceorderId".equals(lastFieldChanged)) {    
	    	 info.addResult("inpemSwaMWarehouseId", null);
    }

    
  }   
 
  
}