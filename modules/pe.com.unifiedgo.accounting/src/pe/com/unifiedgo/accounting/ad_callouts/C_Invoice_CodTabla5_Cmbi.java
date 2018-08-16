package pe.com.unifiedgo.accounting.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;

import pe.com.unifiedgo.core.data.SCRComboItem;

public class C_Invoice_CodTabla5_Cmbi extends SimpleCallout {
  private static final long serialVersionUID = 1L;
  private static final RequestFilter filterYesNo = new ValueListFilter("Y", "N");

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */

    final String strinpissotrx = info.getStringParameter("inpissotrx", filterYesNo);
    final String strOrgId = info.getStringParameter("inpadOrgId", IsIDFilter.instance);
    final String strCodTabla5CmbiId = info.getStringParameter("inpemScoCodtabla5CmbItemId", IsIDFilter.instance);

    if (strCodTabla5CmbiId == null) {
      info.addResult("inpemScoDetractionPercent", "");
      return;
    }

    SCRComboItem detrac = OBDal.getInstance().get(SCRComboItem.class, strCodTabla5CmbiId);
    if (detrac == null) {
      info.addResult("inpemScoDetractionPercent", "");
      return;
    }
    info.addResult("inpemScoDetractionPercent", detrac.getScoDetractpercent());

  }

}
