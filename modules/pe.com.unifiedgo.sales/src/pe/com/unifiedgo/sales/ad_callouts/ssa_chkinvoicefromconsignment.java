package pe.com.unifiedgo.sales.ad_callouts;
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

public class ssa_chkinvoicefromconsignment extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    
   // Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
   // { System.out.println(params.nextElement()); }
		
		String mcomboItemId = info.vars.getStringParameter("inpemSsaComboItemId");
		SCRComboItem cboitem = OBDal.getInstance().get(SCRComboItem.class, mcomboItemId.trim());
		
		if(cboitem.getCode() == null){
			info.addResult("inpemSsaIsConsignment", "N");
		}else if(cboitem.getCode().equals("consignacion")){
			info.addResult("inpemSsaIsConsignment", "Y");
		}else{
			info.addResult("inpemSsaIsConsignment", "N");
		}
  }
}