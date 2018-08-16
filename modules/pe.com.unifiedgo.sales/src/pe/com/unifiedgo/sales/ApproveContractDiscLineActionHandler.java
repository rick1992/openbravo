package pe.com.unifiedgo.sales;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
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
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.db.DbUtility;

import pe.com.unifiedgo.accounting.SCO_Utils;
import pe.com.unifiedgo.sales.data.SSAProjPropContract;
import pe.com.unifiedgo.sales.data.SSAProjPropContractLine;

public class ApproveContractDiscLineActionHandler extends BaseActionHandler {
  private static final Logger log4j = Logger.getLogger(ApproveContractDiscLineActionHandler.class);

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    JSONObject response = new JSONObject();
    try {
      final JSONObject request = new JSONObject(content);

      final String action = request.getString("action");
      final JSONArray contractline_id_array = request.getJSONArray("recordIdList");
      VariablesSecureApp vars = RequestContext.get().getVariablesSecureApp();
      ConnectionProvider connProvider = new DalConnectionProvider();

      User user = OBDal.getInstance().get(User.class, vars.getUser());

      SSAProjPropContractLine contractline = OBDal.getInstance().get(SSAProjPropContractLine.class,
          contractline_id_array.getString(0));
      SSAProjPropContract contract = contractline.getSSAProjpropContract();

      if ("NAP".compareTo(contract.getDiscountEvalStatus()) == 0) {
        throw new OBException("@SCR_SalescontractDissaproved@");
      }

      BigDecimal discount = BigDecimal.ZERO;

      boolean isEvaluatedLine, is_allpermission_user = false;
      List<User> allPermissionUsers;
      List<User> allUsersCanEvaluate;

      // validating if user is allpermission user
      if (contractline_id_array.length() > 0) {
        allPermissionUsers = SCO_Utils.getDiscPrefUserAllPermissions(contract.getClient().getId(),
            contract.getOrganization().getId());
        for (int i = 0; i < allPermissionUsers.size(); i++) {
          if (user == allPermissionUsers.get(i)) {
            is_allpermission_user = true;
            break;
          }
        }
      }

      for (int i = 0; i < contractline_id_array.length(); i++) {
        String c_contractline_id = contractline_id_array.getString(i);
        contractline = OBDal.getInstance().get(SSAProjPropContractLine.class, c_contractline_id);

        if (contractline != null) {
          discount = (contractline.getDescuento().compareTo(BigDecimal.ZERO) > 0)
              ? contractline.getDescuento()
              : BigDecimal.ZERO;

          if (!contractline.getSSAProjpropContract().getDocumentStatus().equals("DR")) {
            throw new OBException("@SSA_SalesContractCantBeEvaluated@");
          }

          String discount_eval_status = contractline.getEstadoEvalDeDescuento();
          if (discount_eval_status.compareTo("NRE") == 0) {
            throw new OBException("@SSA_SalesContractCantBeEvaluated@");
          } else if (discount_eval_status.compareTo("AP") == 0) {
            throw new OBException("@SSA_SalesContractCantBeEvaluated@");
          } else if (discount_eval_status.compareTo("NAP") == 0) {
            throw new OBException("@SSA_SalesContractCantBeEvaluated@");
          }

          if (!is_allpermission_user) {
            isEvaluatedLine = false;
            allUsersCanEvaluate = SCO_Utils.getDiscPrefUsersCanEvaluate(
                contractline.getClient().getId(), contractline.getOrganization().getId(), discount);
            for (int k = 0; k < allUsersCanEvaluate.size(); k++) {
              if (user == allUsersCanEvaluate.get(k)) {
                contractline.setEstadoEvalDeDescuento("AP");
                contractline.setEvalDeDescuentoActualizado(new Date());
                contractline.setEvalDeDescuentoActualizadoPor(user);

                OBDal.getInstance().save(contractline);

                isEvaluatedLine = true;
                break;
              }
            }

            if (!isEvaluatedLine) {
              throw new OBException("@SSA_UsrNoPermissionToEvalDisc@"); // no puede evaluar
            }

          } else {
            contractline.setEstadoEvalDeDescuento("AP");
            contractline.setEvalDeDescuentoActualizado(new Date());
            contractline.setEvalDeDescuentoActualizadoPor(user);

            OBDal.getInstance().save(contractline);
          }

        }
      }

      OBDal.getInstance().flush();

      if (contractline_id_array.length() > 0) {
        List<SSAProjPropContractLine> contractlines = contract.getSSAProjPropContractLineList();
        boolean approved = true;
        for (int i = 0; i < contractlines.size(); i++) {
          if (contractlines.get(i).getEstadoEvalDeDescuento().compareTo("AP") != 0
              && contractlines.get(i).getEstadoEvalDeDescuento().compareTo("NRE") != 0) {
            approved = false;
            break;
          }
        }

        if (approved) {
          contract.setDiscountEvalStatus("AP");
          contract.setDiscountEvalUpdated(new Date());
          contract.setDiscountEvalUpdatedby(user);
          OBDal.getInstance().save(contract);

          OBError res = SSA_Utils.completeProspectOrContract(connProvider, contract);
          // if ("Error".equals(res.getType()))
          // throw new OBException(res.getMessage());
          // else if (!"Success".equals(res.getType()))
          // throw new OBException("@NotPossibleCompleteInvoice@");
        }
      }

      OBDal.getInstance().flush();
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