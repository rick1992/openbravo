package pe.com.unifiedgo.imports.ad_callouts;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

public class sim_imp_costingLines_advTotal extends SimpleCallout {
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
		
		String mExpensesCurrencyLocal =  info.vars.getStringParameter("inpexpensesCurrencylocal").replace(",", ""); 
		if (mExpensesCurrencyLocal.equals("")) {
			mExpensesCurrencyLocal="0";
		 }
		
		String mExpensesCurrencyDollar =  info.vars.getStringParameter("inpexpensesCurrencydollar").replace(",", ""); 
		if (mExpensesCurrencyDollar.equals("")) {
			mExpensesCurrencyDollar="0";
		 }
		
    
		String mTipoCambio = info.vars.getStringParameter("inpexchangeRate").replace(",", "");
		if(mTipoCambio.equals("")){
			mTipoCambio="0";
			info.addResult("inpexchangeRate", "0");
		}
		
		String mTipoCambioDua = info.vars.getStringParameter("inptipoCambio").replace(",", "");
		if(mTipoCambioDua.equals("")){
			mTipoCambioDua="0";
			info.addResult("inptipoCambio", "0");
		}
		
		String mInvoicedQty = info.vars.getStringParameter("inpqtyinvoiced").replace(",", "");
		if(mInvoicedQty.equals("")){ 
		    mInvoicedQty="0";
		    info.addResult("inpqtyinvoiced", "0");
		}
		
		String mSourceCost_Dollar = info.vars.getStringParameter("inpcostSource").replace(",", "");
		if(mSourceCost_Dollar.equals("0")){
			mSourceCost_Dollar="0";
			info.addResult("inpcostSource", "0");
		}
		//String mSourceCost_Soles = info.vars.getStringParameter("inptotalSeguro").replace(",", "");
		String mAdvTotalString = info.vars.getStringParameter("inpadvtotal").replace(",", "");
		if(mAdvTotalString.equals("")){
			mAdvTotalString="0";
			info.addResult("inpadvtotal", "0");
		}
		
		BigDecimal mSourceCost_Soles = new BigDecimal(mSourceCost_Dollar).multiply(new BigDecimal(mTipoCambio));
		//float mSourceCost_Soles = Float.parseFloat(mSourceCost_Dollar)*Float.parseFloat(mTipoCambio);

		
		String mlinenetamt = info.vars.getStringParameter("inplinenetamt").replace(",", "");
		if(mAdvTotalString.equals("")){
			mAdvTotalString="0";
			info.addResult("inplinenetamt", "0");
		}
		
		
		
		BigDecimal madvtotal = new BigDecimal(mAdvTotalString);
		//float madvtotal =  Float.parseFloat(mAdvTotalString);
		BigDecimal madvtotalCurrencylocal =  madvtotal.multiply(new BigDecimal(mTipoCambioDua));
		//float madvtotalCurrencylocal =  madvtotal*Float.parseFloat(mTipoCambioDua);
		info.addResult("inpadvtotalCurrencylocal", madvtotalCurrencylocal);
		
		BigDecimal mtotalCost =  madvtotal.add(new BigDecimal(mlinenetamt)).add(new BigDecimal(mExpensesCurrencyDollar));
		//float mtotalCost =  madvtotal + Float.parseFloat(mlinenetamt) + Float.parseFloat(mExpensesCurrencyDollar);
		info.addResult("inpcostLine", mtotalCost);
		
		BigDecimal mcostUnitDollar = mtotalCost.divide(new BigDecimal(mInvoicedQty),3,RoundingMode.CEILING);
		//float mcostUnitDollar = mtotalCost/Float.parseFloat(mInvoicedQty);
		info.addResult("inpcostUnit", mcostUnitDollar);
		
		BigDecimal mtotalCost_CurrencyLocal = madvtotalCurrencylocal.add(mSourceCost_Soles.multiply(new BigDecimal(mInvoicedQty))).add(new BigDecimal(mExpensesCurrencyLocal));
		//float mtotalCost_CurrencyLocal = madvtotalCurrencylocal + mSourceCost_Soles*Float.parseFloat(mInvoicedQty) + Float.parseFloat(mExpensesCurrencyLocal);
		info.addResult("inpcostLineCurrencylocal", mtotalCost_CurrencyLocal);
		
		BigDecimal mcostUnitCurrencyLocal = mtotalCost_CurrencyLocal.divide(new BigDecimal(mInvoicedQty),3,RoundingMode.CEILING);
		//float mcostUnitCurrencyLocal = mtotalCost_CurrencyLocal/Float.parseFloat(mInvoicedQty);
		info.addResult("inpcostUnitCurrencylocal", mcostUnitCurrencyLocal);
		
  }
}