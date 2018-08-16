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

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.CashVATUtil;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.financialmgmt.payment.PaymentTerm;

public class SE_Invoice_Organization extends SimpleCallout {
  private static final long serialVersionUID = 1L;
  private static final RequestFilter filterYesNo = new ValueListFilter("Y", "N");

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */

    final String strinpissotrx = info.getStringParameter("inpissotrx", filterYesNo);
    final String strOrgId = info.getStringParameter("inpadOrgId", IsIDFilter.instance);
    final String scoSpecialDocType = info.getStringParameter("inpscospecialdoctype", null);
    final String strBPartner = info.getStringParameter("inpcBpartnerId", null);
    String strChanged = info.vars.getStringParameter("inpLastFieldChanged");
    if (log4j.isDebugEnabled())
      log4j.debug("CHANGED: " + strChanged);

    if ("inpadOrgId".equals(strChanged)) { // Organization changed
      // setting null to bp and bp location
      info.addResult("inpcBpartnerLocationId", null);
      info.addResult("inpcBpartnerId", null);
    }

    info.addResult("inpemScoExpensesmotiveId", "");

    Organization org = OBDal.getInstance().get(Organization.class, strOrgId);
    DocumentType doctype = null;

    if (scoSpecialDocType != null) {

      // Find the correct doctype recursively
      while (org != null && !org.getId().equals("0")) {

        OBCriteria<DocumentType> doctype_c = OBDal.getInstance().createCriteria(DocumentType.class);
        doctype_c.add(Restrictions.eq(DocumentType.PROPERTY_SCOSPECIALDOCTYPE, scoSpecialDocType));
        doctype_c.add(Restrictions.eq(DocumentType.PROPERTY_ORGANIZATION, org));

        doctype = (DocumentType) doctype_c.uniqueResult();
        if (doctype != null)
          break;

        Query q = OBDal
            .getInstance()
            .getSession()
            .createSQLQuery(
                "SELECT ad_org.ad_org_id FROM ad_org, ad_treenode pp, ad_treenode hh WHERE pp.node_id = hh.parent_id AND hh.ad_tree_id = pp.ad_tree_id AND pp.node_id=ad_org.ad_org_id AND hh.node_id='"
                    + org.getId()
                    + "' AND  EXISTS (SELECT 1 FROM ad_tree WHERE ad_tree.treetype='OO' AND hh.ad_tree_id=ad_tree.ad_tree_id AND hh.ad_client_id=ad_tree.ad_client_id);");
        String ad_parentOrg_ID = (String) q.uniqueResult();
        if (ad_parentOrg_ID != null) {
          org = OBDal.getInstance().get(Organization.class, ad_parentOrg_ID);
        } else {
          break;
        }

      }

      // The last resort is the * org
      if (org != null && org.getId().equals("0")) {
        OBCriteria<DocumentType> doctype_c = OBDal.getInstance().createCriteria(DocumentType.class);
        doctype_c.add(Restrictions.eq(DocumentType.PROPERTY_SCOSPECIALDOCTYPE, scoSpecialDocType));
        doctype_c.add(Restrictions.eq(DocumentType.PROPERTY_ORGANIZATION, org));

        doctype = (DocumentType) doctype_c.uniqueResult();
      }

      if (doctype != null)
        info.addResult("inpcDoctypetargetId", doctype.getId());
    }

    org = OBDal.getInstance().get(Organization.class, strOrgId);

    // Sales flow only (from the organization)
    if (StringUtils.equals("Y", strinpissotrx)) {

      final String calculatedIsCashVat = CashVATUtil.getOrganizationIsCashVAT(strOrgId);
      if (calculatedIsCashVat != null && filterYesNo.accept(calculatedIsCashVat)) {
        info.addResult("inpiscashvat", calculatedIsCashVat);
      }

      if (org != null && org.isScoPercepcionagent()) {
        info.addResult("inpemScoPercepcionagent", true);
      } else {
        info.addResult("inpemScoPercepcionagent", false);
      }
      // Percepcion warning//
      final String strPaymentTermId = info.getStringParameter("inpcPaymenttermId",
          IsIDFilter.instance);
      PaymentTerm paymentTerm = OBDal.getInstance().get(PaymentTerm.class, strPaymentTermId);

      if (doctype == null) {
        final String strDocTypeId = info.getStringParameter("inpcDoctypetargetId",
            IsIDFilter.instance);
        doctype = OBDal.getInstance().get(DocumentType.class, strDocTypeId);

      }

      if (org != null && paymentTerm != null && doctype != null
          && doctype.getScoSpecialdoctype() != null && org.isScoPercepcionagent()
          && (doctype.getScoSpecialdoctype().equals("SCOARINVOICE"))) {
        String strCBPartnerId = info.getStringParameter("inpcBpartnerId", null);
        BusinessPartner bpartner = OBDal.getInstance().get(BusinessPartner.class, strCBPartnerId);
        if (bpartner != null) {

          if (bpartner.isScoRetencionagent()) {
            // No se aplica percepcion

          } else if (bpartner.isScoPercepcionagent()) {
            // Se aplica 0.5% de percepcion
            if (paymentTerm.getScoSpecialpayterm() != null
                && paymentTerm.getScoSpecialpayterm().equals("SCOINMEDIATETERM")) {
              info.showWarning(OBMessageUtils.getI18NMessage("SCO_SalesHalfPercepcionInmediate",
                  null));
            } else {
              info.showWarning(OBMessageUtils.getI18NMessage("SCO_SalesHalfPercepcionCredit", null));
            }
          } else {
            // Se aplica percepcion
            if (paymentTerm.getScoSpecialpayterm() != null
                && paymentTerm.getScoSpecialpayterm().equals("SCOINMEDIATETERM")) {
              info.showWarning(OBMessageUtils.getI18NMessage("SCO_SalesPercepcionInmediate", null));
            } else {
              info.showWarning(OBMessageUtils.getI18NMessage("SCO_SalesPercepcionCredit", null));
            }
          }
        }
      }

    }

  }

}
