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
 * All portions are Copyright (C) 2001-2013 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package pe.com.unifiedgo.project.ad_callouts;

import java.math.BigDecimal;

import javax.servlet.ServletException;

import org.openbravo.erpCommon.ad_callouts.SimpleCallout;

public class spr_budgetentry_title extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */

    /*
     * String mSprBudgetItemId = info.vars.getStringParameter("inpsprBudgetitemId"); SPRBudgetItem
     * bItem = OBDal.getInstance().get(SPRBudgetItem.class, mSprBudgetItemId); if (bItem != null)
     * info.addResult("inpcUomId", bItem.getUOM().getId());
     */

    info.addResult("inpsprBudgetitemId", null);
    info.addResult("inpcUomId", null);
    info.addResult("inpqtyordered", BigDecimal.ZERO);
    info.addResult("inppriceactual", BigDecimal.ZERO);
    info.addResult("inptotallines", BigDecimal.ZERO);

  }

}
