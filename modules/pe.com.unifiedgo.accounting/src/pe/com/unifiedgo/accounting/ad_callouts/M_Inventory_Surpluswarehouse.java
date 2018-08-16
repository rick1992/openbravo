package pe.com.unifiedgo.accounting.ad_callouts;

import java.util.List;

import javax.servlet.ServletException;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.Warehouse;

public class M_Inventory_Surpluswarehouse extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */

    String stremScoSurpluswarehouseId = info.getStringParameter("inpemScoSurpluswarehouseId", IsIDFilter.instance);
    String stremScoSurpluslocatorId = info.getStringParameter("inpemScoSurpluslocatorId", IsIDFilter.instance);

    if (stremScoSurpluswarehouseId != null) {
      if (stremScoSurpluswarehouseId.equals("")) {
        stremScoSurpluswarehouseId = null;
      }
    }

    if (stremScoSurpluslocatorId != null) {
      if (stremScoSurpluslocatorId.equals("")) {
        stremScoSurpluslocatorId = null;
      }
    }

    if (stremScoSurpluswarehouseId != null) {

      Warehouse surpluswarehouse = OBDal.getInstance().get(Warehouse.class, stremScoSurpluswarehouseId);
      if (surpluswarehouse == null) {
        info.addResult("inpemScoSurpluswarehouseId", "");
        info.addResult("inpemScoSurpluslocatorId", "");
        return;
      }

      if (stremScoSurpluslocatorId != null) {
        Locator surpluslocator = OBDal.getInstance().get(Locator.class, stremScoSurpluslocatorId);
        if (surpluslocator != null) {
          if (!surpluslocator.getWarehouse().getId().equals(surpluswarehouse.getId())) {
            OBCriteria<Locator> locator_c = OBDal.getInstance().createCriteria(Locator.class);
            locator_c.add(Restrictions.eq(Locator.PROPERTY_WAREHOUSE, surpluswarehouse));
            List<Locator> locators = locator_c.list();
            if (locators.size() > 0) {
              info.addResult("inpemScoSurpluslocatorId", locators.get(0).getId());
            } else {
              info.addResult("inpemScoSurpluslocatorId", "");
            }
          }
        } else {
          info.addResult("inpemScoSurpluslocatorId", "");
        }
      } else {
        OBCriteria<Locator> locator_c = OBDal.getInstance().createCriteria(Locator.class);
        locator_c.add(Restrictions.eq(Locator.PROPERTY_WAREHOUSE, surpluswarehouse));
        List<Locator> locators = locator_c.list();
        if (locators.size() > 0) {
          info.addResult("inpemScoSurpluslocatorId", locators.get(0).getId());
        } else {
          info.addResult("inpemScoSurpluslocatorId", "");
        }
      }
    } else {
      info.addResult("inpemScoSurpluslocatorId", "");
    }

  }

}
