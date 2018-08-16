package pe.com.unifiedgo.accounting.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;

import pe.com.unifiedgo.core.data.SCRComboItem;

public class C_Invoice_PocDoctype_Cmbi extends SimpleCallout {
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
    final String strPoDoctypeCmbiId = info.getStringParameter("inpemScoPodoctypeComboitemId",
        IsIDFilter.instance);

    if (strPoDoctypeCmbiId == null) {
      info.addResult("inpemScoPodocCmbiValue", "");
      return;
    }

    SCRComboItem podoctype = OBDal.getInstance().get(SCRComboItem.class, strPoDoctypeCmbiId);
    if (podoctype == null) {
      info.addResult("inpemScoPodocCmbiValue", "");
      return;
    }
    info.addResult("inpemScoPodocCmbiValue", podoctype.getSearchKey());

    if ("N".equals(strinpissotrx)) {
      // Boletas de Compra
      if ("tabla10_03".compareTo(podoctype.getSearchKey()) == 0
          || "tabla10_02".compareTo(podoctype.getSearchKey()) == 0) {
        info.addResult("inpemScrNopurchaserecord", true);
      } else {
        info.addResult("inpemScrNopurchaserecord", false);
      }
    }

  }

}
