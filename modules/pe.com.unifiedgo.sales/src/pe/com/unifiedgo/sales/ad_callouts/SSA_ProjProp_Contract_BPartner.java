package pe.com.unifiedgo.sales.ad_callouts;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.businessUtility.BpartnerMiscData;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.Utility;

public class SSA_ProjProp_Contract_BPartner extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    // General data

    String strChanged = info.vars.getStringParameter("inpLastFieldChanged");
    if (log4j.isDebugEnabled())
      log4j.debug("CHANGED: " + strChanged);

    printPage(info);

  }

  protected void printPage(CalloutInfo info) throws ServletException {
    String strBPartner = info.vars.getStringParameter("inpcBpartnerId");
    String strOrgId = info.vars.getStringParameter("inpadOrgId");
    String strUserRep = "";
    String strCounterUserRep = "";

    BpartnerMiscData[] data = BpartnerMiscData.select(this, strBPartner);
    if (data != null && data.length > 0) {
      strUserRep = data[0].salesrepId;
    }
    strCounterUserRep = info.vars.getUser();

    // BPartner Location

    FieldProvider[] tdv = null;

    String strLocation = info.vars.getStringParameter("inpcBpartnerId_LOC");
    if (strLocation != null && !strLocation.isEmpty()) {
      info.addResult("inpcBpartnerLocationId", strLocation);
    } else {
      // C_Bpartner_Location
      FieldProvider[] tld = null;
      try {
        ComboTableData comboTableData = new ComboTableData(info.vars, this, "TABLEDIR",
            "C_BPartner_Location_ID", "", "C_BPartner Location - Ship To",
            Utility.getContext(this, info.vars, "#AccessibleOrgTree", info.getWindowId()),
            Utility.getContext(this, info.vars, "#User_Client", info.getWindowId()), 0);
        Utility.fillSQLParameters(this, info.vars, null, comboTableData,
            "SSAProjPropContractBPartner", "");
        tld = comboTableData.select(false);
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      if (tld != null && tld.length > 0) {
        info.addSelect("inpcBpartnerLocationId");
        for (int i = 0; i < tld.length; i++) {
          info.addSelectResult(tld[i].getField("id"), tld[i].getField("name"), true);
        }
        info.endSelect();
      } else {
        BpartnerMiscData[] dataloc = BpartnerMiscData.selectNullLocation(this,
            info.vars.getClient(), strBPartner);
        if (dataloc != null && dataloc.length != 0) {
          if (dataloc[0].cBpartnerLocationId != null) {
            info.addSelect("inpcBpartnerLocationId");
            info.addSelectResult(dataloc[0].cBpartnerLocationId, dataloc[0].locationname, true);
            info.endSelect();
          } else {
            info.addResult("inpcBpartnerLocationId", null);
          }
        } else {
          info.addResult("inpcBpartnerLocationId", null);
        }
      }
    }

    // Sales Representative
    if (!StringUtils.isEmpty(strUserRep)) {
      info.addResult("inpsalesrepId", strUserRep);
    } else {
      info.addResult("inpsalesrepId", null);
    }

    // Counter Sales Representative

    FieldProvider[] tld = null;
    try {
      ComboTableData comboTableData = new ComboTableData(info.vars, this, "TABLE", "",
          "AD36A54426544626B746C6A91E6F63A7", "",
          Utility.getContext(this, info.vars, "#AccessibleOrgTree", "SSAProjPropContractBPartner"),
          Utility.getContext(this, info.vars, "#User_Client", "SSAProjPropContractBPartner"), 0);
      Utility.fillSQLParameters(this, info.vars, null, comboTableData,
          "SSAProjPropContractBPartner", "");
      tld = comboTableData.select(false);
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    if (tld != null && tld.length > 0) {
      info.addSelect("inpcounterSalesrepId");
      for (int i = 0; i < tld.length; i++) {
        info.addSelectResult(tld[i].getField("id"), tld[i].getField("name"),
            tld[i].getField("id").equalsIgnoreCase(strCounterUserRep));
      }

      info.endSelect();

    } else {
      info.addResult("inpcounterSalesrepId", null);
    }

  }

}
