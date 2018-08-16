package pe.com.unifiedgo.sales;

import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.ad.ui.Process;
import org.openbravo.service.db.CallProcess;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.db.DbUtility;

import pe.com.unifiedgo.sales.data.SSAProjPropContractLine;

public class ProjPropVoidContractLineActionHandler extends BaseActionHandler {
  private static final Logger log4j = Logger.getLogger(ProjPropVoidContractLineActionHandler.class);

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    ConnectionProvider conn = new DalConnectionProvider();

    VariablesSecureApp vars = new VariablesSecureApp(OBContext.getOBContext().getUser().getId(),
        OBContext.getOBContext().getCurrentClient().getId(),
        OBContext.getOBContext().getCurrentOrganization().getId(),
        OBContext.getOBContext().getRole().getId(),
        OBContext.getOBContext().getLanguage().getLanguage());

    String strMsg = "";
    OBError msg = null;

    JSONObject response = new JSONObject();
    try {
      final JSONObject request = new JSONObject(content);

      final JSONArray ssa_projprop_line_id_array = request.getJSONArray("recordIdList");

      if (ssa_projprop_line_id_array.length() == 0) {
        throw new Exception("@NoDataSelected@");
      }

      // Validating before create requisition
      SSAProjPropContractLine propertyline = OBDal.getInstance().get(SSAProjPropContractLine.class,
          ssa_projprop_line_id_array.getString(0));
      if (propertyline == null) {
        throw new Exception("@DBExecuteError@");
      }
      if ("CO".compareTo(propertyline.getLinestatus()) != 0) {
        throw new Exception("@SSA_PropLineNotCompleted@"); // cambiar mensaje
      }

      // Voiding sold property contract line
      for (int i = 0; i < ssa_projprop_line_id_array.length(); i++) {
        msg = voidSoldContractPropertyLine(conn, vars, ssa_projprop_line_id_array.getString(i));
        if (msg != null && "Error".equals(msg.getType())) {
          throw new Exception(msg.getMessage());
        } else if (!"Success".equals(msg.getType())) {
          throw new Exception("@InvoiceCreateFailed@");
        }

        strMsg = msg.getMessage();
        if (strMsg != null && "".equals(strMsg)) {
          strMsg = "@ProcessOK@";
        }
        if (strMsg.contains("@")) {
          strMsg = OBMessageUtils.parseTranslation(strMsg);
        }
        strMsg = Utility.messageBD(conn, strMsg, vars.getLanguage());
      }
      //

      JSONObject errorMessage = new JSONObject();
      errorMessage.put("severity", "success");
      errorMessage.put("text", strMsg);
      response.put("message", errorMessage);

    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();

      Throwable ex = DbUtility.getUnderlyingSQLException(e);
      // if (strMsg.contains("@")) {
      // strMsg = OBMessageUtils.parseTranslation(strMsg);
      // }
      // strMsg = Utility.messageBD(conn, strMsg, vars.getLanguage());
      strMsg = OBMessageUtils.translateError(ex.getMessage()).getMessage();
      try {
        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", "error");
        errorMessage.put("text", strMsg);
        response.put("strMsg", errorMessage);
      } catch (JSONException ignore) {
      }
    }
    return response;
  }

  public static OBError voidSoldContractPropertyLine(ConnectionProvider conn,
      VariablesSecureApp vars, String projPropertyContractline_ID) {
    OBError msg = null;
    try {
      OBContext.setAdminMode(true);
      Process process = null;
      try {
        process = OBDal.getInstance().get(Process.class, "6A1781C0D2074C7A9FB5FC4FCAC9EEC6");
      } finally {
        OBContext.restorePreviousMode();
      }

      Map<String, String> parameters = null;
      final ProcessInstance pinstance = CallProcess.getInstance().call(process,
          projPropertyContractline_ID, parameters);

      msg = OBMessageUtils.getProcessInstanceMessage(pinstance);
    } catch (Exception ex) {
      msg = Utility.translateError(conn, vars, vars.getLanguage(), ex.getMessage());
    }
    return msg;
  }

}