package pe.com.unifiedgo.imports.ad_callouts;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;

import org.hibernate.Query;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.invoice.Invoice;

import pe.com.unifiedgo.imports.data.SimOrderImport;
import pe.com.unifiedgo.imports.data.SimOrderImportLine;

import java.util.Enumeration;

public class Sim_orderImportScheduled extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    
   // Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
   // { System.out.println(params.nextElement()); }
     
    //example
	// String mProductPriceList = info.vars.getStringParameter("inpmProductId_PSTD");
    //system.out.println(mProductPriceList);  //imprime lo que hay en el textbox 
    // inpmProductId_PSTD Price for Product
	// ("inpqtyordered"); //Cantidad Ordenada
	// ("inppriceactual"); // Price Actual 
	// ("inppricelist");  //Price List
	// ("inplinenetamt"); // Total Amout
 	// ("inpmProductId"); //Product ID

   String mDateOrdered = info.vars.getStringParameter("inpdateordered");
   String mdatePromised = info.vars.getStringParameter("inpdatepromised");
   String mOrdeRImportId = info.vars.getStringParameter("inpsimOrderimportId");
   
   Query q = OBDal.getInstance().getSession().createSQLQuery("update sim_orderimportline set datepromised = '"  + mdatePromised + "' where sim_orderimport_id = '" + mOrdeRImportId + "'");
   q.executeUpdate();
  }
}
