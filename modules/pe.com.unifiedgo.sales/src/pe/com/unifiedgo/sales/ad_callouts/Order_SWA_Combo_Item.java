package pe.com.unifiedgo.sales.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;

import pe.com.unifiedgo.core.data.SCRComboItem;

public class Order_SWA_Combo_Item extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */

    // Calculate em_swa_combo_item_id.value
    final String stremSwaComboItemId = info.getStringParameter("inpemSwaComboItemId", IsIDFilter.instance);
    if (stremSwaComboItemId != null) {
      SCRComboItem inouttype = OBDal.getInstance().get(SCRComboItem.class, stremSwaComboItemId);
      if (inouttype != null)
        info.addResult("inpemSsaCmbitemValue", inouttype.getSearchKey());
      else
        info.addResult("inpemSsaCmbitemValue", "");
    } else {
      info.addResult("inpemSsaCmbitemValue", "");
    }
  }

}
