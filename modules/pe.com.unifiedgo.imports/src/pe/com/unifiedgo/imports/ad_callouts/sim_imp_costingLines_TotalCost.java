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

public class sim_imp_costingLines_TotalCost extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    
  // Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
  //  { System.out.println(params.nextElement()); }
		
		String mProductId = info.vars.getStringParameter("inpmProductId");
		String mUom =  info.vars.getStringParameter("inpmProductId_UOM"); 
		if (mProductId.equals("")) {
			 return;
		 }
    
		String mTipoCambio = info.vars.getStringParameter("inptipoCambio").replace(",", "");
		String mInvoicedQty = info.vars.getStringParameter("inpqtyinvoiced").replace(",", "");
		String mSourceCost_Dollar = info.vars.getStringParameter("inpcostSource").replace(",", "");
		//String mSourceCost_Soles = info.vars.getStringParameter("inptotalSeguro").replace(",", "");
		String mAdvPercent = info.vars.getStringParameter("inpadvpercent").replace(",", "");
		
		String mcostLines = info.vars.getStringParameter("inpcostLine").replace(",", "");
		
		
		
		float mSourceCost_Soles = Float.parseFloat(mSourceCost_Dollar)*Float.parseFloat(mTipoCambio);
		info.addResult("inpcostSourceCurrencylocal", mSourceCost_Soles);
		
		float mlinenetamt =  Float.parseFloat(mSourceCost_Dollar)*Float.parseFloat(mInvoicedQty);
		info.addResult("inplinenetamt", mlinenetamt);
		
		float mlinenetamtCurrencyLocal =  mSourceCost_Soles*Float.parseFloat(mInvoicedQty);
		info.addResult("inplinenetamtCurrencylocal", mlinenetamtCurrencyLocal);
		
		float madvtotal =  mlinenetamt*Float.parseFloat(mAdvPercent)/100;
		info.addResult("inpadvtotal", madvtotal);
		
		float madvtotalCurrencylocal =  madvtotal*Float.parseFloat(mTipoCambio);
		info.addResult("inpadvtotalCurrencylocal", madvtotalCurrencylocal);
		
		float mtotalCost =   Float.parseFloat(mcostLines);
		//info.addResult("inpcostLine", mtotalCost);
		
		float mcostUnitDollar = mtotalCost/Float.parseFloat(mInvoicedQty);
		info.addResult("inpcostUnit", mcostUnitDollar);
		
		
		float mtotalCost_CurrencyLocal = mtotalCost*Float.parseFloat(mTipoCambio);
		info.addResult("inpcostLineCurrencylocal", mtotalCost_CurrencyLocal);
		
		float mcostUnitCurrencyLocal = mtotalCost_CurrencyLocal/Float.parseFloat(mInvoicedQty);
		info.addResult("inpcostUnitCurrencylocal", mcostUnitCurrencyLocal);
		
		
  }
}