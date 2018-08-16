package pe.com.unifiedgo.shipping;

import java.util.Date;
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


public class WBexportdataActionHandler extends BaseActionHandler {
  private static final Logger log4j = Logger.getLogger(WBexportdataActionHandler.class);

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    JSONObject response = new JSONObject();
    try {
      final JSONObject request = new JSONObject(content);

      final JSONArray ssh_waybill = request.getJSONArray("recordIdList");
      VariablesSecureApp vars = RequestContext.get().getVariablesSecureApp();
      
      String ids="";

      for (int i = 0; i < ssh_waybill.length(); i++) {
        String strWayBillId = ssh_waybill.getString(i); 
        
        System.out.println("RECORRIENDO EL ID DE LA TABLA"
        		+ " que se seleccionaron e la ventana hoja de ruta (tabla ssh_transportista) ");
        
        System.out.println(strWayBillId);

        ids=strWayBillId+"','"+ids;
      }
      
      ids="'"+ids+"'";
      
      
      
      //** RICARDO ACA SEGUIR CON TU CODIGO
      
      
      
      
      
      
      
      
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