package pe.com.unifiedgo.project.ad_callouts;

import java.math.BigDecimal;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.plm.Product;

public class SPR_BudgetLine_Calculate extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */
    VariablesSecureApp vars = info.vars;
    String lastFieldChanged = info.getLastFieldChanged();

    String strPriceActual = info.vars.getStringParameter("inppriceactual").replace(",", "");
    String strQtyOrdered = info.vars.getStringParameter("inpqtyordered").replace(",", "");
    String strQtyRequested = info.vars.getStringParameter("inpqtyrequested").replace(",", "");
    String strPriceList = info.vars.getStringParameter("inppricelist").replace(",", "");

    String strProductId = info.vars.getStringParameter("inpmProductId");

    BigDecimal priceActual = new BigDecimal((strPriceActual.equals("")) ? "0" : strPriceActual);
    BigDecimal qtyOrder = new BigDecimal((strQtyOrdered.equals("")) ? "0" : strQtyOrdered);
    BigDecimal priceList = new BigDecimal((strPriceList.equals("")) ? "0" : strPriceList);
    BigDecimal lineNetAmt = BigDecimal.ZERO;

    Product product = OBDal.getInstance().get(Product.class, strProductId);

    if ("inpqtyordered".equals(lastFieldChanged)) {
      lineNetAmt = priceActual.multiply(qtyOrder);
      info.addResult("inplinenetamt", lineNetAmt);
    } else if ("inppriceactual".equals(lastFieldChanged)) {
      lineNetAmt = priceActual.multiply(qtyOrder);
      info.addResult("inplinenetamt", lineNetAmt);
    } else if ("inpdiscount".equals(lastFieldChanged)) {

    } else if ("inpqtyrequested".equals(lastFieldChanged)) {
      info.addResult("inpqtyordered", strQtyRequested);
      // System.out.println("HOLAS Q HACE: " + strQtyRequested);

    }

  }

}
