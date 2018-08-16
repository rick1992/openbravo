package pe.com.unifiedgo.sales.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;

import pe.com.unifiedgo.core.data.SCRComboItem;

public class ssa_ordertype extends SimpleCallout {
  private static final long serialVersionUID = 1L;
  private static final RequestFilter filterYesNo = new ValueListFilter("Y", "N");

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    final String strinpissotrx = info.getStringParameter("inpissotrx", filterYesNo);
    final String strSTypeComboItemId = info.getStringParameter("inpemSsaComboItemId", IsIDFilter.instance);

    SCRComboItem sTypecomboitem = OBDal.getInstance().get(SCRComboItem.class, strSTypeComboItemId);
    if (sTypecomboitem != null) {
      info.addResult("inpemSsaComboitemValue", sTypecomboitem.getSearchKey());
      info.addResult("inpemSsaOrdertypecmbValue", sTypecomboitem.getSearchKey());
    } else {
      info.addResult("inpemSsaComboitemValue", "");
      info.addResult("inpemSsaOrdertypecmbValue", "");
    }

    // SOCOUNTER SALES ORDER TYPE
    String strIssocounter = info.vars.getStringParameter("inpemSsaIssocounter");
    String strSocounterStatus = info.vars.getStringParameter("inpemSsaSocounterStatus");
    if (strIssocounter == null)
      strIssocounter = "";
    if (strSocounterStatus == null)
      strSocounterStatus = "";
    if (strIssocounter.compareTo("Y") == 0 && strSocounterStatus.compareTo("DR") == 0) {
      return;
    }

    if (strinpissotrx.equals("Y"))
      info.addResult("inpmWarehouseId", null);
  }

}