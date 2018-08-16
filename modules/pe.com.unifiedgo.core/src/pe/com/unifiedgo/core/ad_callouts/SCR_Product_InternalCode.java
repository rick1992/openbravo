package pe.com.unifiedgo.core.ad_callouts;

import java.util.List;

import javax.servlet.ServletException;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.plm.Product;

public class SCR_Product_InternalCode extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    // Enumeration<String> params = info.vars.getParameterNames();
    // while (params.hasMoreElements()) {
    // System.out.println(params.nextElement());
    // }

    final String strInternalCode = info.getStringParameter("inpemScrInternalcode",
        IsIDFilter.instance);

    OBCriteria<Product> product_m = OBDal.getInstance().createCriteria(Product.class);
    product_m.add(Restrictions.eq(Product.PROPERTY_SCRINTERNALCODE, strInternalCode));

    List<Product> products = product_m.list();
    if (products.size() > 0) {
      info.addResult("WARNING",
          OBMessageUtils.getI18NMessage("SCR_ProductUniqueInternalCode", null));
      return;
    }

  }
}
