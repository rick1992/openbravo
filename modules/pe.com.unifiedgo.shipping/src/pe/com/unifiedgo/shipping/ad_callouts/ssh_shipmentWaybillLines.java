package pe.com.unifiedgo.shipping.ad_callouts;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;

import org.apache.fop.fo.FObj;
import org.apache.tools.ant.taskdefs.Replace;
import org.hibernate.Query;
//import org.apache.jasper.tagplugins.jstl.core.Out;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;

import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;

public class ssh_shipmentWaybillLines extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    
  //  Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
  //  { System.out.println(params.nextElement()); }
		
	   String mAdClientId = info.vars.getStringParameter("inpadClientId");
	  
		String mShipmentId = info.vars.getStringParameter("inpmInoutId");
		String usertmp = info.vars.getStringParameter("inpsshUserId");
		String usertmp2 = info.vars.getStringParameter("ssh_user_id");
		
		if(mShipmentId == null || mShipmentId.equals(""))
			return;
		
		ShipmentInOut shipment = OBDal.getInstance().get(ShipmentInOut.class, mShipmentId);
		
		
		if(shipment.getBusinessPartner() != null)
		  info.addResult("inpcBpartnerId", shipment.getBusinessPartner().getId());
		
		if(shipment.getOrganization() != null)
		  info.addResult("inparOrgRefId", shipment.getOrganization().getId());
		
		info.addResult("inpdocstatus", shipment.getDocumentStatus());
		info.addResult("inpbultosTotal", shipment.getSwaNumcajasGuiaTotal());
		info.addResult("inppesoTotal", shipment.getSwaPesoGuiaTotal());
		info.addResult("inpshipstatus", shipment.getSwaShipstatus());
		info.addResult("inpcubicajeTotal", shipment.getSwaCubicajeGuiaTotal());
		
		BigDecimal Costing = calculatingCostingforShipping(mAdClientId, mShipmentId, shipment.getMovementDate());
		
		info.addResult("inptotalcost", Costing);
  }
  
  private BigDecimal calculatingCostingforShipping(String mAdClientId, String m_inoutid, Date Datewaybill){
	  try {
		  
		  Query q = OBDal
			        .getInstance()
			        .getSession()
			        .createSQLQuery(
			            "  SELECT  sum((l.movementqty * c.cost)) total FROM m_costing c " +
			            "  INNER JOIN m_inoutline l on c.m_product_id = l.m_product_id " +
			            "  INNER JOIN m_inout i on l.m_inout_id = i.m_inout_id " +
			            "  WHERE i.m_inout_id = '" + m_inoutid + "' " +
			            "     AND c.datefrom <= '" + Datewaybill + "' " +
			            "      AND c.dateto >=  '" + Datewaybill +"' "+
			            "      AND c.ad_client_id =  '" + mAdClientId +"'");
		        
		          BigDecimal CostQuery;
		          CostQuery = (BigDecimal) q.uniqueResult();
		          
			    if(CostQuery.toPlainString().equals("") || CostQuery == null){
			    	CostQuery=new BigDecimal(0);
			    }
			    
			    return CostQuery;
	  } catch (Exception e) {
          return new BigDecimal(0);	  
	  }  
	  
  }
}