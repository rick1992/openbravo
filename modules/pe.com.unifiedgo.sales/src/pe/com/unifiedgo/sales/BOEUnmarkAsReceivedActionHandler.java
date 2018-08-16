package pe.com.unifiedgo.sales;

import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.db.DbUtility;

public class BOEUnmarkAsReceivedActionHandler extends BaseActionHandler {
  private static final Logger log4j = Logger.getLogger(BOEUnmarkAsReceivedActionHandler.class);

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    JSONObject response = new JSONObject();
    try {
      final JSONObject request = new JSONObject(content);

      final JSONArray ssa_boe_id_array = request.getJSONArray("recordIdList");
      VariablesSecureApp vars = RequestContext.get().getVariablesSecureApp();

      for (int i = 0; i < ssa_boe_id_array.length(); i++) {
        String strSCO_BOE_ID = ssa_boe_id_array.getString(i);
        Invoice boe = OBDal.getInstance().get(Invoice.class, strSCO_BOE_ID);
        if (boe != null) {
          if (("SCOARBOEINVOICE".equals(boe.getSCOSpecialDocType())
              || "SCOAPBOEINVOICE".equals(boe.getSCOSpecialDocType())) && boe.isScrIsreceived()) {
            boe.setProcessed(false);
            OBDal.getInstance().save(boe);
            OBDal.getInstance().flush();

            boe.setScrIsreceived(false);
            boe.setSsaBoeDatereceipt(null);

            boe.setProcessed(true);
          }
        }
      }
      OBDal.getInstance().commitAndClose();

      JSONObject errorMessage = new JSONObject();
      errorMessage.put("severity", "success");
      errorMessage.put("text",
          Utility.messageBD(new DalConnectionProvider(false), "ProcessOK", vars.getLanguage()));
      response.put("message", errorMessage);

    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();

      Throwable ex = DbUtility.getUnderlyingSQLException(e);
      String message = OBMessageUtils.translateError(ex.getMessage()).getMessage();
      try {
        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", "error");
        errorMessage.put("text", message);
        response.put("message", errorMessage);
      } catch (JSONException ignore) {
      }
    }
    return response;
  }
}