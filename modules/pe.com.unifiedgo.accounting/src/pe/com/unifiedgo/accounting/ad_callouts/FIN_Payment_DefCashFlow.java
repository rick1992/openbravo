package pe.com.unifiedgo.accounting.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;

public class FIN_Payment_DefCashFlow extends SimpleCallout {
  private static final long serialVersionUID = 1L;
  private static final RequestFilter filterYesNo = new ValueListFilter("Y", "N");

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    /*
     * Enumeration<String> en = info.vars.getParameterNames(); while (en.hasMoreElements()) {
     * System.out.println(en.nextElement()); }
     */

    String strADClientId = info.getStringParameter("inpadClientId", IsIDFilter.instance);
    String strADOrgId = info.getStringParameter("inpadOrgId", IsIDFilter.instance);
    String strADWindowId = info.getStringParameter("inpwindowId", IsIDFilter.instance);

    System.out.println("strADClientId:" + strADClientId + " - strADOrgId:" + strADOrgId
        + " - strADWindowId:" + strADWindowId);
    if (strADOrgId.isEmpty() || strADWindowId.isEmpty()) {
      info.addResult("inpemScoEeffCashflowId", "");
      return;
    }

    // OBCriteria<SCOEEFFCashflowDefs> oc = OBDal.getInstance().createCriteria(
    // SCOEEFFCashflowDefs.class);
    // oc.add(Restrictions.eq(SCOEEFFCashflowDefs.PROPERTY_CLIENT,
    // OBDal.getInstance().get(Client.class, strADClientId)));
    // oc.add(Restrictions.sqlRestriction("AD_ISORGINCLUDED('" + strADOrgId + "', ad_org_id, '"
    // + strADClientId + "') > -1"));
    // oc.add(Restrictions.eq(SCOEEFFCashflowDefs.PROPERTY_WINDOW,
    // OBDal.getInstance().get(Window.class, strADWindowId)));
    // oc.setMaxResults(1);
    //
    // List<SCOEEFFCashflowDefs> cashflowdefs = oc.list();
    // if (cashflowdefs.size() > 0) {
    // System.out.println("got cashflowdefs.get(0).getId():" + cashflowdefs.get(0).getId());
    // info.addResult("inpemScoEeffCashflowId", cashflowdefs.get(0).getSCOEeffCashflow().getId());
    // return;
    // }

  }

}
