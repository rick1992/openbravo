package pe.com.unifiedgo.accounting.ad_callouts;

import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.financial.FinancialUtils;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.gl.GLItem;

public class SCO_is_for_free_invoice extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    
   //   Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     // { System.out.println(params.nextElement()); }
      
    final String strADClientId = info.getStringParameter("inpadClientId", null);
    Client cliente = OBDal.getInstance().get(Client.class, strADClientId);
      
    String Special_freeIGV = "SCOFREEIGV";
    String Special_freeAsset = "SCOFREEEXPENSES";
    
    
 	OBCriteria<GLItem> GLItem_freeigv = OBDal.getInstance().createCriteria(GLItem.class);
 	GLItem_freeigv.add(Restrictions.eq(GLItem.PROPERTY_CLIENT, cliente));
 	GLItem_freeigv.add(Restrictions.eq(GLItem.PROPERTY_SCOSPECIALGLITEM, Special_freeIGV));
 	List<GLItem> GLitem_igv = GLItem_freeigv.list();
 	
 		for(int i=0;i< GLitem_igv.size();i++){
 			info.addResult("inpemScoForfreeigvexGliId", GLitem_igv.get(i).getId());
 			break;
 		}
 		
 	
    
 	OBCriteria<GLItem> GLItem_freeasset = OBDal.getInstance().createCriteria(GLItem.class);
 	GLItem_freeasset.add(Restrictions.eq(GLItem.PROPERTY_CLIENT, cliente));
 	GLItem_freeasset.add(Restrictions.eq(GLItem.PROPERTY_SCOSPECIALGLITEM, Special_freeAsset));
 	List<GLItem> GLitem_asset = GLItem_freeasset.list();
 	
		for(int i=0;i< GLitem_asset.size();i++){
			info.addResult("inpemScoForfreeassetexGliId", GLitem_asset.get(i).getId());
			break;
		} 
  }
}
