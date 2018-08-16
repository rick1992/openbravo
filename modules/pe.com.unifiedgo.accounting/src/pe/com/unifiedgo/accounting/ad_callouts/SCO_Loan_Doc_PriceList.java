package pe.com.unifiedgo.accounting.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.pricing.pricelist.PriceList;

public class SCO_Loan_Doc_PriceList extends SimpleCallout {
  private static final long serialVersionUID = 1L;
  private static final RequestFilter filterYesNo = new ValueListFilter("Y", "N");

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    final String strinpissotrx = info.getStringParameter("inpissotrx", filterYesNo);
    final String strOrgId = info.getStringParameter("inpadOrgId", IsIDFilter.instance);
    final String strMPricelistId = info.getStringParameter("inpmPricelistId", IsIDFilter.instance);

    PriceList pricelist = OBDal.getInstance().get(PriceList.class, strMPricelistId);
    if (pricelist == null) {
      info.addResult("inpcCurrencyId", "");
      return;
    }
    info.addResult("inpcCurrencyId", pricelist.getCurrency().getId());
  }

}
