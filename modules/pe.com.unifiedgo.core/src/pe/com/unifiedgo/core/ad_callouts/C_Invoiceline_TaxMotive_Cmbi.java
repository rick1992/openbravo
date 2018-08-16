package pe.com.unifiedgo.core.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;

import pe.com.unifiedgo.core.data.SCRComboItem;

public class C_Invoiceline_TaxMotive_Cmbi extends SimpleCallout {
  private static final long serialVersionUID = 1L;
  private static final RequestFilter filterYesNo = new ValueListFilter("Y", "N");

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */

    final String strTaxMotiveCmbiId = info.getStringParameter("inpemScrComboItemId", IsIDFilter.instance);

    if (strTaxMotiveCmbiId == null) {
      info.addResult("inpemScrTaxmotivecmbiValue", "");
      return;
    }

    SCRComboItem taxmotive = OBDal.getInstance().get(SCRComboItem.class, strTaxMotiveCmbiId);
    if (taxmotive == null) {
      info.addResult("inpemScrTaxmotivecmbiValue", "");
      return;
    }
    info.addResult("inpemScrTaxmotivecmbiValue", taxmotive.getSearchKey());
  }

}
