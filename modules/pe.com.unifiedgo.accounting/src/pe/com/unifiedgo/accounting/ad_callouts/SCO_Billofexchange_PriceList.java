package pe.com.unifiedgo.accounting.ad_callouts;

import java.math.BigDecimal;

import javax.servlet.ServletException;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.pricing.pricelist.PriceList;

public class SCO_Billofexchange_PriceList extends SimpleCallout {
  private static final long serialVersionUID = 1L;
  private static final RequestFilter filterYesNo = new ValueListFilter("Y", "N");

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // Enumeration<String> params = info.vars.getParameterNames();
    // while (params.hasMoreElements()) {
    // System.out.println(params.nextElement());
    // }

    final String strinpissotrx = info.getStringParameter("inpissotrx", filterYesNo);
    final String strOrgId = info.getStringParameter("inpadOrgId", IsIDFilter.instance);
    final String strMPriceListID = info.getStringParameter("inpmPricelistId", IsIDFilter.instance);

    if (strMPriceListID == null) {
      info.addResult("inpcCurrencyId", "");
      info.addResult("inpcOtherCurrencyId", "");
      info.addResult("inpconvertRate", BigDecimal.ONE);
      info.addResult("inpaltConvertRate", BigDecimal.ONE);
      return;
    }

    if (strMPriceListID.isEmpty()) {
      info.addResult("inpcCurrencyId", "");
      info.addResult("inpcOtherCurrencyId", "");
      info.addResult("inpconvertRate", BigDecimal.ONE);
      info.addResult("inpaltConvertRate", BigDecimal.ONE);
      return;
    }

    PriceList pl = OBDal.getInstance().get(PriceList.class, strMPriceListID);
    if (pl == null) {
      info.addResult("inpcCurrencyId", "");
      info.addResult("inpcOtherCurrencyId", "");
      info.addResult("inpconvertRate", BigDecimal.ONE);
      info.addResult("inpaltConvertRate", BigDecimal.ONE);
      return;
    }

    info.addResult("inpcCurrencyId", pl.getCurrency().getId());
    info.addResult("inpcOtherCurrencyId", pl.getCurrency().getId());
    info.addResult("inpconvertRate", BigDecimal.ONE);
    info.addResult("inpaltConvertRate", BigDecimal.ONE);

  }

}
