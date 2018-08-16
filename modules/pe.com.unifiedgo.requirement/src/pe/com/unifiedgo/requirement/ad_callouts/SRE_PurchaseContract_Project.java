package pe.com.unifiedgo.requirement.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.project.Project;

public class SRE_PurchaseContract_Project extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */

    final String strProjectId = info.getStringParameter("inpcProjectId", IsIDFilter.instance);

    Project project = OBDal.getInstance().get(Project.class, strProjectId);
    if (project != null) {
      // info.addResult("inpmPriceListId", project.getPriceList().getId());
      // info.addResult("inpcCurrencyId", project.getCurrency().getId());
    }
  }

}
