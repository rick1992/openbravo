package pe.com.unifiedgo.requirement.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.enterprise.DocumentType;

public class SRE_ContractPaySched_DocType extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */

    final String strDoctypetargetId = info.getStringParameter("inpcDoctypetargetId",
        IsIDFilter.instance);

    DocumentType dt = OBDal.getInstance().get(DocumentType.class, strDoctypetargetId);
    if (dt != null) {
      info.addResult("inpspecialdoctype", dt.getScoSpecialdoctype());
    } else {
      info.addResult("inpspecialdoctype", "");
    }
  }

}
