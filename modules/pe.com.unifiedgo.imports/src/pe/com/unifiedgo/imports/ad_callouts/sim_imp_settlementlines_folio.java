package pe.com.unifiedgo.imports.ad_callouts;
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
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.order.Order;

import pe.com.unifiedgo.imports.data.SimFolioImport;

import java.util.Enumeration;
import java.util.Locale;

public class sim_imp_settlementlines_folio extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    
  //  Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
  //  { System.out.println(params.nextElement()); }
	String StringPartialId = info.vars.getStringParameter("inpcOrderId").replace(",", "");
    Order partial = OBDal.getInstance().get(Order.class, StringPartialId.trim());
    if(partial.getSimOrderimport() != null)
    	info.addResult("inpsimOrderimportId", partial.getSimOrderimport().getId()); 
  }
}