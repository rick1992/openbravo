package pe.com.unifiedgo.shipping.ad_callouts;
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
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;

import java.util.Enumeration;
import java.util.Locale;

public class ssh_reject_transport extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    
  //  Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
  //  { System.out.println(params.nextElement()); }
		
		String mShipmentId = info.vars.getStringParameter("inpmInoutId");
		String misreject = info.vars.getStringParameter("inpisrejected");
		//System.out.println(misreject );
		if(mShipmentId == null || mShipmentId.equals(""))
			return;
		
	/*	ShipmentInOut shipment = OBDal.getInstance().get(ShipmentInOut.class, mShipmentId);
		if(misreject.equals("Y"))
		 shipment.setSwaShipstatus("");
		else
		 shipment.setSwaShipstatus("");
		OBDal.getInstance().save(shipment);*/
	
  }
}