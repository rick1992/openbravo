package pe.com.unifiedgo.imports.ad_callouts;


import java.math.BigDecimal;

import javax.servlet.ServletException;

import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.plm.Product;

public class Sim_ImportOrder_Product extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

   //  Enumeration<String> params = info.vars.getParameterNames();
   //  while (params.hasMoreElements()) {
   //  System.out.println(params.nextElement());
   //  }

    // example
    // String mProductPriceList = info.vars.getStringParameter("inpmProductId_PSTD");
    // system.out.println(mProductPriceList); //imprime lo que hay en el textbox
    // inpmProductId_PSTD Price for Product
    // ("inpqtyordered"); //Cantidad Ordenada
    // ("inppriceactual"); // Price Actual
    // ("inppricelist"); //Price List
    // ("inplinenetamt"); // Total Amout
    // ("inpmProductId"); //Product ID
    String mUom = info.vars.getStringParameter("inpmProductId_UOM");
    String mProductPriceList = info.vars.getStringParameter("inpmProductId_PSTD").replace(",", "");

    String mProductId = info.vars.getStringParameter("inpmProductId");
    String mProductqty = info.vars.getStringParameter("inpqtyordered").replace(",", "");

    if (mProductId.equals("")) {
      return;
    }
    
    Product producto = OBDal.getInstance().get(Product.class, mProductId);
    info.addResult("inppartidaArancelaria", producto.getSimPartidaArancelaria());
    info.addResult("inpinternalcode", producto.getScrInternalcode());
    info.addResult("inpadvalorem", producto.getSIMAdValorem());
    info.addResult("inptlcDiscAdvalorem", producto.getSIMTLCDiscountADdvalorem());
    info.addResult("inpcUomId", mUom);
    
   // BigDecimal priceList = BigDecimal.ZERO;
   // BigDecimal productqty = BigDecimal.ZERO;
    BigDecimal priceList = new BigDecimal((mProductPriceList.equals(""))?"0": mProductPriceList );
    BigDecimal productqty = new BigDecimal((mProductqty.equals(""))?"0": mProductqty );

    
    if (priceList.compareTo(BigDecimal.ZERO)!=0) {
      info.addResult("inppricelist", priceList);
      info.addResult("inppriceactual", priceList);
      info.addResult("inplinenetamt",priceList.multiply(productqty));
      info.addResult("inpdiscount", 0);
    } else {
      info.addResult("inppricelist", 0);
      info.addResult("inppriceactual", 0);
      info.addResult("inplinenetamt", 0);
      info.addResult("inpdiscount", 0);
    }

  }
}
