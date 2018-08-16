package pe.com.unifiedgo.warehouse.ad_callouts;

import javax.servlet.ServletException;

//import org.apache.jasper.tagplugins.jstl.core.Out;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.enterprise.Warehouse;

public class swa_permission_orgwarehouse extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    // Enumeration<String> params = info.vars.getParameterNames();
    // while (params.hasMoreElements()) {
    // System.out.println(params.nextElement());
    // }

    String mWarehousetId = info.vars.getStringParameter("inpmWarehouseId");

    Warehouse warehouse = OBDal.getInstance().get(Warehouse.class, mWarehousetId.trim());

    if (warehouse != null) {
      info.addResult("inporgwarehouseId", warehouse.getOrganization().getId());
    }
  }
}