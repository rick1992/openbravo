package pe.com.unifiedgo.accounting;

import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.businessUtility.PeriodControlUtility;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.financialmgmt.calendar.Period;
import org.openbravo.model.financialmgmt.calendar.PeriodControl;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.db.DbUtility;

import pe.com.unifiedgo.accounting.data.SCOBoetodiscLine;

public class SelectPaymentActionHandler extends BaseActionHandler {
  private static final Logger log4j = Logger.getLogger(SelectPaymentActionHandler.class);

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    JSONObject response = new JSONObject();
    try {
      final JSONObject request = new JSONObject(content);

      final String action = request.getString("action");
      if ("ACTION_COMBO".equals(action)) {
          response.put("actionComboBox", getActionComboBox());
          return response;
      }
      else{
    	  System.out.println("action:" + action);
    	     final JSONArray sco_boetodisc_line_id_array = request.getJSONArray("recordIdList");
    	      VariablesSecureApp vars = RequestContext.get().getVariablesSecureApp();
    	      User user = OBDal.getInstance().get(User.class, vars.getUser());

    	      boolean isselectedporpayment = false;
    	      if(action.equals("S"))
    	    	  isselectedporpayment = true;
    	      for (int i = 0; i < sco_boetodisc_line_id_array.length(); i++) {
    	        String sco_boetodisc_line_id = sco_boetodisc_line_id_array.getString(i);
    	        SCOBoetodiscLine boetodisc_line = OBDal.getInstance().get(SCOBoetodiscLine.class, sco_boetodisc_line_id);
    	        boetodisc_line.setPayselected(isselectedporpayment);
    	        OBDal.getInstance().save(boetodisc_line);
    	      }

    	      OBDal.getInstance().flush();
    	      OBDal.getInstance().commitAndClose();
    	      JSONObject errorMessage = new JSONObject();
    	      errorMessage.put("severity", "success");
    	      errorMessage.put("text", Utility.messageBD(new DalConnectionProvider(false), "ProcessOK", vars.getLanguage()));
    	      response.put("message", errorMessage);  
      }

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
  
  private JSONObject getActionComboBox()
	      throws Exception {
	    JSONObject response = new JSONObject();
	    JSONObject valueMap = new JSONObject();
	    valueMap.put("S", "Seleccionar");
	    valueMap.put("D", "Deseleccionar");
	    response.put("valueMap", valueMap);
	    response.put("defaultValue", "S");
	    return response;
	  }
}