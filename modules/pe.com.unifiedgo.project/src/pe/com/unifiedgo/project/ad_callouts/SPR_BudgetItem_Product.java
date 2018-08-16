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

import java.text.SimpleDateFormat;

import javax.servlet.ServletException;

import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.businessUtility.Tax;
import org.openbravo.model.common.plm.Product;

import pe.com.unifiedgo.project.data.SPRBudgetItem;

public class SPR_BudgetItem_Product extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */

    String mUom = info.vars.getStringParameter("inpmProductId_UOM");
    String mProductId = info.vars.getStringParameter("inpmProductId");
    String mBudgetItem = info.vars.getStringParameter("inpsprBudgetitemId");
    String strCTaxID = "";

    if (mProductId.equals("")) {
      return;
    }

    Product producto = OBDal.getInstance().get(Product.class, mProductId);
    info.addResult("inpcUomId", mUom);

    SPRBudgetItem budgetitem = OBDal.getInstance().get(SPRBudgetItem.class, mBudgetItem);

    try {
      if (budgetitem != null) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy");
        String date = sdf.format(budgetitem.getCreationDate());

        strCTaxID = Tax.get(this, mProductId, date, budgetitem.getOrganization().getId(), "", "",
            "", "", false, false);

        info.addResult("inpcTaxId", strCTaxID);
      }
    } catch (Exception e) {

      System.out.println(e.getMessage());
      // TODO: handle exception
    }

  }

}
