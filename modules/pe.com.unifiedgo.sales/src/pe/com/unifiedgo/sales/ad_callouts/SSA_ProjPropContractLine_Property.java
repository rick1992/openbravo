package pe.com.unifiedgo.sales.ad_callouts;

import java.math.BigDecimal;

import javax.servlet.ServletException;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;

import pe.com.unifiedgo.sales.data.SSAProjectProperty;

public class SSA_ProjPropContractLine_Property extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */

    final String strssaProjectPropertyId = info.getStringParameter("inpssaProjectPropertyId",
        IsIDFilter.instance);
    if (strssaProjectPropertyId != null) {
      SSAProjectProperty property = OBDal.getInstance().get(SSAProjectProperty.class,
          strssaProjectPropertyId);
      if (property != null) {
        info.addResult("inptotalamount", property.getTotalamount());
        info.addResult("inppriceactual", property.getTotalamount());
        info.addResult("inpdiscount", BigDecimal.ZERO);

        String detail = "";
        if (property.getType() != null && !"".equals(property.getType()))
          detail = detail + property.getType().trim();
        detail = detail + " / " + "Nro: ";
        if (property.getNumber() != null && !"".equals(property.getNumber()))
          detail = detail + property.getNumber().trim();
        detail = detail + " / " + "Area Total: ";
        if (property.getTotalarea() != null && !"".equals(property.getTotalarea()))
          detail = detail + property.getTotalarea().trim();
        detail = detail + " / " + "Area Techada: ";
        if (property.getRoofedtarea() != null && !"".equals(property.getRoofedtarea()))
          detail = detail + property.getRoofedtarea().trim();
        detail = detail + " / " + "Area Libre: ";
        if (property.getNotbuiltarea() != null && !"".equals(property.getNotbuiltarea()))
          detail = detail + property.getNotbuiltarea().trim();

        info.addResult("inppropertyDetail", detail);

      } else {
        info.addResult("inptotalamount", "");
        info.addResult("inppropertyDetail", "");
        info.addResult("inppriceactual", BigDecimal.ZERO);
        info.addResult("inpdiscount", BigDecimal.ZERO);
      }

    } else {
      info.addResult("inptotalamount", "");
      info.addResult("inppropertyDetail", "");
      info.addResult("inppriceactual", BigDecimal.ZERO);
      info.addResult("inpdiscount", BigDecimal.ZERO);
    }

  }

}
