package pe.com.unifiedgo.sales.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.project.Project;

public class SSA_ProjPropContractLine_Project extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    // Enumeration<String> params = info.vars.getParameterNames();
    // while (params.hasMoreElements()) {
    // System.out.println(params.nextElement());
    // }

    final String strcProjectId = info.getStringParameter("inpcProjectId", null);

    Project project = OBDal.getInstance().get(Project.class, strcProjectId);
    if (project != null) {
      info.addResult("inppromotorBank", project.getScrPromotorBank());
      info.addResult("inppromotorFinacctId",
          (project.getScrPromotorFinacct() != null) ? project.getScrPromotorFinacct().getId() : "");
      info.addResult("inpcboitemBuildingtypeId",
          (project.getSprCboitemBuildingtype() != null)
              ? project.getSprCboitemBuildingtype().getId()
              : "");
      info.addResult("inppropertyDetail", "");
      info.addResult("inpssaProjectPropertyId", "");

    } else {
      info.addResult("inppromotorBank", "");
      info.addResult("inppromotorFinacctId", "");
      info.addResult("inpcboitemBuildingtypeId", "");
      info.addResult("inppropertyDetail", "");
      info.addResult("inpssaProjectPropertyId", "");
    }

  }

}
