package pe.com.unifiedgo.imports.ad_callouts;
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
import org.openbravo.model.common.invoice.Invoice;

import pe.com.unifiedgo.imports.data.SimFolioImport;

import java.util.Enumeration;
import java.util.Locale;

public class sim_imp_updateinvoice extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    
  //  Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
  //  { System.out.println(params.nextElement()); }
     
	    String mCostingId = info.vars.getStringParameter("inpsimImpCostingId");//SIM_Imp_Costing_ID
	    String mFolioId = info.vars.getStringParameter("inpsimFolioimportId");
        String mDuaId = info.vars.getStringParameter("inpscoDuaId");
    
        SimFolioImport folioImport = OBDal.getInstance().get(SimFolioImport.class, mFolioId.trim());
        if(folioImport == null)
        	info.addResult("inpscoDuaId", "");
        
        String NewDuaId = null;
        try {
        	 NewDuaId = folioImport.getSCODua().getId();
        	 info.addResult("inpscoDuaId", NewDuaId);
		} catch (Exception e) {
			  info.addResult("inpscoDuaId", null);
		}
         

        //ELIMINAMOS TODO REGISTRO ACTUAL ASOCIADO CON EL COSTEO_ID
        Query qu = OBDal.getInstance().getSession().createSQLQuery("update c_invoice set em_sim_imp_costing_id = null WHERE em_sco_isimportation = 'Y'  AND em_sim_imp_costing_id = '" + mCostingId.trim() + "'");
        qu.executeUpdate();
        
        //ELIMINAMOS TODO REGISTRO DE costingLINE
        Query qu2 = OBDal.getInstance().getSession().createSQLQuery("delete from sim_imp_costinglines WHERE sim_imp_costing_id = '" + mCostingId.trim() + "'");
        qu2.executeUpdate();
        
        
        //ACTUALIZAMOS TODOS LOS INVOICES CON EL NUEVO DUA.
       // System.out.println("QUE RARO: "+ mCostingId.trim());
        
        
        //Revisar Si hay tiempo
      //  if (NewDuaId != null) {
      //       Query query = OBDal.getInstance().getSession().createSQLQuery("update c_invoice set em_sim_imp_costing_id = '"  + mCostingId.trim() + "' WHERE em_sco_isimportation = 'Y'  AND em_sco_dua_id = '" + NewDuaId.trim() + "'");
      //       query.executeUpdate();
      //  }
        
  }
}