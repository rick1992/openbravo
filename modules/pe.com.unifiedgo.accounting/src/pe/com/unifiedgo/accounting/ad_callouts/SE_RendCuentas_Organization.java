package pe.com.unifiedgo.accounting.ad_callouts;

import javax.servlet.ServletException;

import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;

public class SE_RendCuentas_Organization extends SimpleCallout {
  private static final long serialVersionUID = 1L;
  private static final RequestFilter filterYesNo = new ValueListFilter("Y", "N");

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */

    final String strOrgId = info.getStringParameter("inpadOrgId", IsIDFilter.instance);
    final String scoSpecialDocType = info.getStringParameter("inpscospecialdoctype", null);

    info.addResult("inpcBpartnerId", null);
    info.addResult("inpcRespbpartnerId", null);

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

        Query q = OBDal.getInstance().getSession().createSQLQuery("SELECT ad_org.ad_org_id FROM ad_org, ad_treenode pp, ad_treenode hh WHERE pp.node_id = hh.parent_id AND hh.ad_tree_id = pp.ad_tree_id AND pp.node_id=ad_org.ad_org_id AND hh.node_id='" + org.getId() + "' AND  EXISTS (SELECT 1 FROM ad_tree WHERE ad_tree.treetype='OO' AND hh.ad_tree_id=ad_tree.ad_tree_id AND hh.ad_client_id=ad_tree.ad_client_id);");
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
      else
        info.addResult("inpcDoctypetargetId", "");
    }

  }

}
