package pe.com.unifiedgo.imports.ad_callouts;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;

import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.invoice.Invoice;

import java.util.Enumeration;

public class Sim_ImportOrder_PriceList extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    
  String mProductPriceListOriginal = info.vars.getStringParameter("inpmProductId_PSTD"); //Precio Original
  String mProductActual = info.vars.getStringParameter("inppriceactual").replace(",", "");;  //Precio en Text BOx
  String strPriceList = info.vars.getStringParameter("inppricelist").replace(",", "");;  //List Price
  String discount = info.vars.getStringParameter("inpdiscount").replace(",", "");;     //Descuentos en text Box
  String mProductqty = info.vars.getStringParameter("inpqtyordered").replace(",", "");; //Cantidad en Text Box
  Float newAmount;
  Float newProductActual ;
  Float priceList, priceActual;
  
  priceList = Float.parseFloat(strPriceList);
  priceActual = Float.parseFloat(mProductActual);
  newProductActual = priceList - ((Float.parseFloat(discount)*priceList)/100);

  newAmount = (Float.parseFloat(mProductqty)*newProductActual);
  info.addResult("inppriceactual", newProductActual);
  info.addResult("inplinenetamt", newAmount);  
 
  }

}
