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

import java.util.Enumeration;
import java.util.Locale;

public class sim_imp_settlem_lines_process extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    
   // Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
   // { System.out.println(params.nextElement()); }
     
	    String mFob = info.vars.getStringParameter("inptotalFob").replace(",", "");
		String mFreight = info.vars.getStringParameter("inptotalFlete").replace(",", "");
		String mInsurance = info.vars.getStringParameter("inptotalSeguro").replace(",", "");
		float newCif = Float.parseFloat(mFob) + Float.parseFloat(mFreight) + Float.parseFloat(mInsurance);
		info.addResult("inptotalCif", newCif); 
    	//	String mCIF = info.vars.getStringParameter("inptotalCif").replace(",", "");
		String mAdvPercent = info.vars.getStringParameter("inpadvalorem").replace(",", "");
		float newAdvalorem = newCif*Float.parseFloat(mAdvPercent)/100;
		info.addResult("inptotalAdvalorem", newAdvalorem); 
		//String mPerception = info.vars.getStringParameter("inpperception").replace(",", "");
  }
}