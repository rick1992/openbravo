/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2013 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package pe.com.unifiedgo.sales.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.erpCommon.ad_callouts.SimpleCallout;

public class SSA_ProjProp_Contract_Organization extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    final String strBPartner = info.getStringParameter("inpcBpartnerId", null);
    String strChanged = info.vars.getStringParameter("inpLastFieldChanged");
    if (log4j.isDebugEnabled())
      log4j.debug("CHANGED: " + strChanged);

    if ("inpadOrgId".equals(strChanged)) { // Organization changed
      // setting null to bp and bp location
      info.addResult("inpcBpartnerLocationId", null);
      info.addResult("inpcBpartnerId", null);
      info.addResult("inpsalesrepId", null);
    }

  }
}
