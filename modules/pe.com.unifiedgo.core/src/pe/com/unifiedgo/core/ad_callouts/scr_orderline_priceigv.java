package pe.com.unifiedgo.core.ad_callouts;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.servlet.ServletException;

import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.financialmgmt.tax.TaxRate;

public class scr_orderline_priceigv extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */

    String lastFieldChanged = info.getLastFieldChanged();
    String strPriceTax = info.vars.getStringParameter("inpemScrPricetax").replace(",", "");
    BigDecimal pricetax = new BigDecimal((strPriceTax.equals("")) ? "0" : strPriceTax);

    String strTaxId = info.vars.getStringParameter("inpcTaxId").replace(",", "");

    if ("inpemScrPricetax".equals(lastFieldChanged)) {

      TaxRate tax = OBDal.getInstance().get(TaxRate.class, strTaxId);
      BigDecimal rate = tax.getRate() == null ? new BigDecimal(0)
          : tax.getRate().divide(new BigDecimal(100));

      BigDecimal factor = new BigDecimal(1).add(rate);
      BigDecimal priceActual = BigDecimal.ZERO;

      if (factor.compareTo(BigDecimal.ZERO) != 0)
        priceActual = pricetax.divide(factor, 5, RoundingMode.HALF_UP);

      info.addResult("inppriceactual", priceActual);
    }

    /*
     * final String strInternalCode = info.getStringParameter("inpemScrInternalcode",
     * IsIDFilter.instance);
     * 
     * OBCriteria<Product> product_m = OBDal.getInstance().createCriteria(Product.class);
     * product_m.add(Restrictions.eq(Product.PROPERTY_SCRINTERNALCODE, strInternalCode));
     * 
     * List<Product> products = product_m.list(); if (products.size() > 0) {
     * info.addResult("WARNING", OBMessageUtils.getI18NMessage("SCR_ProductUniqueInternalCode",
     * null)); return; }
     */

  }
}
