package pe.com.unifiedgo.sales.ad_callouts;

import java.math.BigDecimal;

import javax.servlet.ServletException;

import org.openbravo.erpCommon.ad_callouts.SimpleCallout;

public class SSA_ProjPropContractLine_Price extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */

    final BigDecimal totalamount = info.getBigDecimalParameter("inptotalamount");
    final BigDecimal priceactual = info.getBigDecimalParameter("inppriceactual");
    final BigDecimal discount = info.getBigDecimalParameter("inpdiscount");

    String strChanged = info.vars.getStringParameter("inpLastFieldChanged");

    if ("inppriceactual".equals(strChanged)) { // Price actual changed
      info.addResult("inpdiscount", totalamount.subtract(priceactual));

    } else if ("inpdiscount".equals(strChanged)) { // Discount changed
      info.addResult("inppriceactual", totalamount.subtract(discount));
    }

  }

}
