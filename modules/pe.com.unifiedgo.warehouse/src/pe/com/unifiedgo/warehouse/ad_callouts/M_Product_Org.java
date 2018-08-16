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
 * All portions are Copyright (C) 2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package pe.com.unifiedgo.warehouse.ad_callouts;

import java.math.BigDecimal;
import java.util.List;

import javax.servlet.ServletException;

import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBDao;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.plm.ProductCategory;

public class M_Product_Org extends SimpleCallout {
  // ddd
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    final String strOrgId = info.getStringParameter("inpadOrgId", null);

    Organization organization = OBDal.getInstance().get(Organization.class, strOrgId);
    Organization orgProduct = OBDal.getInstance().get(Organization.class, strOrgId);

    String taxCategoryId = "";
    String parentOrgId = "";
    String whereClause = "";

    info.addResult("inpemScoProductfamilyId", "");
    info.addResult("inpemSreProductPurchGrpId", "");

    if (strOrgId != null && !"".equals(strOrgId)) {
      try {
        OBContext.setAdminMode();
        info.addSelect("inpmProductCategoryId");
        OBCriteria<ProductCategory> productCatCrit = OBDao.getFilteredCriteria(
            ProductCategory.class,
            Restrictions.in(ProductCategory.PROPERTY_ORGANIZATION + "." + Organization.PROPERTY_ID,
                new OrganizationStructureProvider().getNaturalTree(strOrgId)));
        productCatCrit.add(Restrictions.eq(ProductCategory.PROPERTY_SUMMARYLEVEL, false));
        productCatCrit.addOrderBy(ProductCategory.PROPERTY_NAME, true);
        String defaultCategoryId = getDefaultCategory(strOrgId);
        for (final ProductCategory productCategory : productCatCrit.list()) {
          info.addSelectResult(productCategory.getId(), productCategory.getIdentifier(),
              defaultCategoryId.equals(productCategory.getId()));
        }
        info.endSelect();
      } finally {
        OBContext.restorePreviousMode();
      }

      BigDecimal value = orgProduct.getSwaProductSequence();
      Boolean tmp = false;

      while (!tmp) {

        OBCriteria<Product> product = OBDal.getInstance().createCriteria(Product.class);
        product.add(Restrictions.eq(Product.PROPERTY_SEARCHKEY, value.toString()));

        if (!product.list().isEmpty())
          value = value.add(new BigDecimal(1));
        else
          tmp = true;
      }

      info.addResult("inpvalue", value);

      orgProduct.setSwaProductSequence(value.add(new BigDecimal(1)));
      OBDal.getInstance().save(orgProduct);

    }
  }

  private String getDefaultCategory(String strOrgId) {
    OBContext.setAdminMode();
    try {
      OBCriteria<ProductCategory> productCatCrit = OBDao.getFilteredCriteria(
          ProductCategory.class, Restrictions
              .eq(ProductCategory.PROPERTY_ORGANIZATION + "." + Organization.PROPERTY_ID, strOrgId),
          Restrictions.eq(ProductCategory.PROPERTY_DEFAULT, true));
      productCatCrit.add(Restrictions.eq(ProductCategory.PROPERTY_SUMMARYLEVEL, false));
      List<ProductCategory> categories = productCatCrit.list();
      if (categories.size() > 0) {
        return categories.get(0).getId();
      } else {
        String parentOrg = OBContext.getOBContext().getOrganizationStructureProvider()
            .getParentOrg(strOrgId);
        if (parentOrg != null && !"".equals(parentOrg)) {
          return getDefaultCategory(parentOrg);
        }
      }
      return "";
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
