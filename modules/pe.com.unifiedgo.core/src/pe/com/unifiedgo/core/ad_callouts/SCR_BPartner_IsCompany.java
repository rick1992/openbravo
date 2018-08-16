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
 * All portions are Copyright (C) 2012-2016 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package pe.com.unifiedgo.core.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.utils.FormatUtilities;

public class SCR_BPartner_IsCompany extends SimpleCallout {

  // limits name and username to a maximum number of characters
  public final static int maxCharBPName = 100;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    String strChanged = info.getStringParameter("inpLastFieldChanged", IsIDFilter.instance);

    String strBPFirstname = info.getStringParameter("inpemScoFirstname", null);
    String strBPLastname = info.getStringParameter("inpemScoLastname", null);
    String strBPLastname2 = info.getStringParameter("inpemScoLastname2", null);
    System.out.println("strBPFirstname:" + strBPFirstname + " - strBPLastname:" + strBPLastname
        + " - strBPLastname2:" + strBPLastname2);
    String localStrName = "";
    String localStrBPLastname = strBPLastname;
    String localStrBPLastname2 = strBPLastname2;

    if (localStrBPLastname != null && !"".equals(localStrBPLastname)) {
      localStrBPLastname = " " + localStrBPLastname;
    }
    if (localStrBPLastname2 != null && !"".equals(localStrBPLastname2)) {
      localStrBPLastname2 = " " + localStrBPLastname2;
    }

    // do not change the name field, if the user just left it
    if (strChanged.equals("inpemScoFirstname") || strChanged.equals("inpemScoLastname")
        || strChanged.equals("inpemScoLastname2")) {
      localStrName = getbpname(strBPFirstname, localStrBPLastname, localStrBPLastname2,
          maxCharBPName);
      info.addResult("inpname", localStrName);
    }
  }

  static public String getbpname(String strBPFirstname, String localStrBPLastname,
      String localStrBPLastname2, int maxChar) {
    String localStrName = "";
    if (FormatUtilities.replaceJS(strBPFirstname + localStrBPLastname + localStrBPLastname2)
        .length() > maxChar) {
      localStrName = FormatUtilities
          .replaceJS(strBPFirstname + localStrBPLastname + localStrBPLastname2)
          .substring(0, maxChar);
    } else {
      localStrName = FormatUtilities
          .replaceJS(strBPFirstname + localStrBPLastname + localStrBPLastname2);
    }
    return localStrName;
  }

}
