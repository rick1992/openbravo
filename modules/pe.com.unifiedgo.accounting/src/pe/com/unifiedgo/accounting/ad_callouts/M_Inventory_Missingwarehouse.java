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

public class M_Inventory_Missingwarehouse extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */

    String stremScoMissingwarehouseId = info.getStringParameter("inpemScoMissingwarehouseId", IsIDFilter.instance);
    String stremScoMissinglocatorId = info.getStringParameter("inpemScoMissinglocatorId", IsIDFilter.instance);

    if (stremScoMissingwarehouseId != null) {
      if (stremScoMissingwarehouseId.equals("")) {
        stremScoMissingwarehouseId = null;
      }
    }

    if (stremScoMissinglocatorId != null) {
      if (stremScoMissinglocatorId.equals("")) {
        stremScoMissinglocatorId = null;
      }
    }

    if (stremScoMissingwarehouseId != null) {
      Warehouse missingwarehouse = OBDal.getInstance().get(Warehouse.class, stremScoMissingwarehouseId);
      if (missingwarehouse == null) {
        info.addResult("inpemScoMissingwarehouseId", "");
        info.addResult("inpemScoMissinglocatorId", "");
        return;
      }

      if (stremScoMissinglocatorId != null) {
        Locator missinglocator = OBDal.getInstance().get(Locator.class, stremScoMissinglocatorId);
        if (missinglocator != null) {
          if (!missinglocator.getWarehouse().getId().equals(missingwarehouse.getId())) {
            OBCriteria<Locator> locator_c = OBDal.getInstance().createCriteria(Locator.class);
            locator_c.add(Restrictions.eq(Locator.PROPERTY_WAREHOUSE, missingwarehouse));
            List<Locator> locators = locator_c.list();
            if (locators.size() > 0) {
              info.addResult("inpemScoMissinglocatorId", locators.get(0).getId());
            } else {
              info.addResult("inpemScoMissinglocatorId", "");
            }
          }
        } else {
          info.addResult("inpemScoMissinglocatorId", "");
        }
      } else {
        OBCriteria<Locator> locator_c = OBDal.getInstance().createCriteria(Locator.class);
        locator_c.add(Restrictions.eq(Locator.PROPERTY_WAREHOUSE, missingwarehouse));
        List<Locator> locators = locator_c.list();
        if (locators.size() > 0) {
          info.addResult("inpemScoMissinglocatorId", locators.get(0).getId());
        } else {
          info.addResult("inpemScoMissinglocatorId", "");
        }
      }
    } else {
      info.addResult("inpemScoMissinglocatorId", "");
    }

  }

}
