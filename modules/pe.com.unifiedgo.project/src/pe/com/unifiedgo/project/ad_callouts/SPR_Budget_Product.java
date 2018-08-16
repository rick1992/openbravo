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
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;

import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.businessUtility.Tax;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.pricing.pricelist.PriceList;

import pe.com.unifiedgo.accounting.SCO_Utils;
import pe.com.unifiedgo.project.data.SPRBudget;

public class SPR_Budget_Product extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */

    String strbudgetId = info.vars.getStringParameter("inpsprBudgetId");
    String mProductId = info.vars.getStringParameter("inpmProductId");
    String mProductPriceList = info.vars.getStringParameter("inpmPricelistId");
    String mProductqty = info.vars.getStringParameter("inpqtyordered").replace(",", "");

    if (mProductId.equals("")) {
      return;
    }

    Product producto = OBDal.getInstance().get(Product.class, mProductId);
    SPRBudget budget = OBDal.getInstance().get(SPRBudget.class, strbudgetId);

    if (producto != null) {
      info.addResult("inpcUomId", producto.getUOM().getId());
    }

    BigDecimal priceList = BigDecimal.ZERO;
    PriceList pricelist = OBDal.getInstance().get(PriceList.class, mProductPriceList);
    if (pricelist != null && budget != null) {
      String product_data[] = SCO_Utils.getProductPricesByDate(this,
          budget.getOrganization().getId(), mProductId, mProductPriceList, budget.getBudgetdate());

      String strPriceList = product_data[0];
      String strNetPriceList = strPriceList;
      if (strPriceList.startsWith("\""))
        strNetPriceList = strPriceList.substring(1, strPriceList.length() - 1);

      priceList = (strNetPriceList.equals("") ? BigDecimal.ZERO : new BigDecimal(strNetPriceList));

    }

    // BigDecimal priceList = BigDecimal.ZERO;
    // BigDecimal productqty = BigDecimal.ZERO;

    BigDecimal productqty = new BigDecimal((mProductqty.equals("")) ? "0" : mProductqty);

    info.addResult("inppricelist", priceList);
    info.addResult("inppriceactual", priceList);
    info.addResult("inplinenetamt", priceList.multiply(productqty));

    String strCTaxID = "";

    /*
     * strCTaxID = Tax.get(this, mProductId, budget.getBudgetdate().toString(),
     * budget.getOrganization().getId(), "", "", "", budget.getProject().getId(), false);
     */

    try {

      SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy");
      String date = sdf.format(budget.getBudgetdate());

      strCTaxID = Tax.get(this, mProductId, date, budget.getOrganization().getId(), "", "", "", "",
          false, false);

      info.addResult("inpcTaxId", strCTaxID);

    } catch (Exception e) {
      // TODO: handle exception
    }

  }

}
