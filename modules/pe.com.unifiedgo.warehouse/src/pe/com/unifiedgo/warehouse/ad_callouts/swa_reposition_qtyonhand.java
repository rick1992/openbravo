package pe.com.unifiedgo.warehouse.ad_callouts;

import java.math.BigDecimal;
import java.util.Enumeration;

import javax.servlet.ServletException;

import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.plm.Product;

import pe.com.unifiedgo.accounting.SCO_Utils;

//import org.apache.jasper.tagplugins.jstl.core.Out;

public class swa_reposition_qtyonhand extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

   //  Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
   //  { System.out.println(params.nextElement()); }

    // ComboItem SCRComboItem = OBDal.getInstance().get(SCRComboItem-class, id)
    String madOrgId = info.vars.getStringParameter("inpadOrgId");
    String madClientId = info.vars.getStringParameter("inpadClientId");
    String mFromWarehousetId = info.vars.getStringParameter("inpfromMWarehouseId");
    String mProductId = info.vars.getStringParameter("inpmProductId");

    // System.out.print(madOrgId + " - " + madClientId + " - " + mFromWarehousetId + " - " +
    // mProductId);

    if (!madOrgId.equals("") | !madClientId.equals("") | !mFromWarehousetId.equals("")
        | !mProductId.equals("")) {
      BigDecimal qtyonhand = SCO_Utils.getWarehouseStockAvailable(madOrgId, madClientId,
          mFromWarehousetId, mProductId);

      info.addResult("inpqtyonhand", qtyonhand);
    }
    
    Product product = OBDal.getInstance().get(Product.class, mProductId.trim());
    info.addResult("inpcUomId", product.getUOM().getId());
    

  }

}