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

package org.openbravo.erpCommon.ad_callouts;

import java.util.List;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.CashVATUtil;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.order.Order;

public class SE_Order_Organization extends SimpleCallout {
  private static final long serialVersionUID = 1L;
  private static final RequestFilter filterYesNo = new ValueListFilter("Y", "N");

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    final String strinpissotrx = info.getStringParameter("inpissotrx", filterYesNo);
    final String strBPartner = info.getStringParameter("inpcBpartnerId", null);
    String strChanged = info.vars.getStringParameter("inpLastFieldChanged");
    if (log4j.isDebugEnabled())
      log4j.debug("CHANGED: " + strChanged);

    if ("inpadOrgId".equals(strChanged)) { // Organization changed
      // setting null to bp and bp location
      info.addResult("inpcBpartnerLocationId", null);
      info.addResult("inpcBpartnerId", null);
    }

    info.addResult("inpmWarehouseId", null);
    // Sales flow only (from the organization)
    if (StringUtils.equals("Y", strinpissotrx)) {
      final String strOrgId = info.getStringParameter("inpadOrgId", IsIDFilter.instance);
      final String calculatedIsCashVat = CashVATUtil.getOrganizationIsCashVAT(strOrgId);
      if (calculatedIsCashVat != null && filterYesNo.accept(calculatedIsCashVat)) {
        info.addResult("inpiscashvat", calculatedIsCashVat);
      }

    }

    // SOCOUNTER SALES ORDER TYPE
    String strIssocounter = info.vars.getStringParameter("inpemSsaIssocounter");
    String strSocounterStatus = info.vars.getStringParameter("inpemSsaSocounterStatus");
    if (strIssocounter == null)
      strIssocounter = "";
    if (strSocounterStatus == null)
      strSocounterStatus = "";
    if (strIssocounter.compareTo("Y") == 0 && strSocounterStatus.compareTo("DR") == 0) {

      final String strOrgId = info.getStringParameter("inpadOrgId", IsIDFilter.instance);
      Organization org = OBDal.getInstance().get(Organization.class, strOrgId);
      if (org == null)
        return;

      // FILL SALES ORDER WITH (ORG,documentno=TPLCOUNTER) template if exists
      OBCriteria<Order> ordFilter = OBDal.getInstance().createCriteria(Order.class);
      ordFilter.add(Restrictions.eq(Order.PROPERTY_ORGANIZATION, org));
      ordFilter.add(Restrictions.eq(Order.PROPERTY_DOCUMENTNO, "TPLCOUNTER"));
      ordFilter.setMaxResults(1);
      List<Order> orders = ordFilter.list();

      if (orders.size() > 0) {
        Order order = orders.get(0);

        info.addResult("inpcBpartnerId", order.getBusinessPartner() != null ? order
            .getBusinessPartner().getId() : "");
        info.addResult("inpcBpartnerLocationId", order.getPartnerAddress() != null ? order
            .getPartnerAddress().getId() : "");
        info.addResult("inpdeliveryLocationId", order.getDeliveryLocation() != null ? order
            .getDeliveryLocation().getId() : "");
        info.addResult("inpbilltoId", order.getInvoiceAddress() != null ? order.getInvoiceAddress()
            .getId() : "");
        info.addResult("inpaymentrule", order.getFormOfPayment());
        info.addResult("inpinvoicerule", order.getInvoiceTerms());
        info.addResult("inpdeliveryrule", order.getDeliveryTerms());
        info.addResult("inpdeliveryviarule", order.getDeliveryMethod());
        info.addResult("inpfreightcostrule", order.getFreightCostRule());
        info.addResult("inpcPaymenttermId", order.getPaymentTerms() != null ? order
            .getPaymentTerms().getId() : "");
        info.addResult("inpmWarehouseId", order.getWarehouse() != null ? order.getWarehouse()
            .getId() : "");
        info.addResult("inpmPricelistId", order.getPriceList() != null ? order.getPriceList()
            .getId() : "");
        info.addResult("inpfinPaymentmethodId", order.getPaymentMethod() != null ? order
            .getPaymentMethod().getId() : "");
        info.addResult("inpemSsaSalesAreaCboItemId", order.getSsaSalesAreaCboItem() != null ? order
            .getSsaSalesAreaCboItem().getId() : "");
        info.addResult("inpemSsaComboItemId", order.getSsaComboItem() != null ? order
            .getSsaComboItem().getId() : "");

      } else {
        info.addResult("inpcBpartnerId", "");
        info.addResult("inpcBpartnerLocationId", "");
        info.addResult("inpdeliveryLocationId", "");
        info.addResult("inpbilltoId", "");
        info.addResult("inpmWarehouseId", "");

      }

    }

  }
}
