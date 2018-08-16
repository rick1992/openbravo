package pe.com.unifiedgo.project;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.procurement.Requisition;
import org.openbravo.model.procurement.RequisitionLine;
import org.openbravo.model.project.Project;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.db.DbUtility;

import pe.com.unifiedgo.accounting.SCO_Utils;
import pe.com.unifiedgo.project.data.SPRBudgetline;

public class BDGLiGenerateRequisitionActionHandler extends BaseActionHandler {
  private static final Logger log4j = Logger.getLogger(BDGLiGenerateRequisitionActionHandler.class);

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    JSONObject response = new JSONObject();
    try {
      VariablesSecureApp vars = RequestContext.get().getVariablesSecureApp();
      final JSONObject request = new JSONObject(content);

      final JSONArray spr_bdgline_id_array = request.getJSONArray("recordIdList");

      if (spr_bdgline_id_array.length() == 0) {
        throw new Exception("@NoDataSelected@");
      }

      // Validating before create requisition
      SPRBudgetline bdgline = OBDal.getInstance().get(SPRBudgetline.class,
          spr_bdgline_id_array.getString(0));
      if (bdgline == null) {
        throw new Exception("@DBExecuteError@");
      }
      if ("SPR_EXE".compareTo(bdgline.getSPRBudget().getProject().getProjectStatus()) != 0) {
        throw new Exception("@SPR_ProjectNotInExecution@");
      }
      if ("CO".compareTo(bdgline.getSPRBudget().getDocumentStatus()) != 0) {
        throw new Exception("@SPR_BudgetMustBeCompleted@");
      }

      // Creating vector of sprbudgetlines
      ArrayList<SPRBudgetline> spr_bdgline_list = new ArrayList<SPRBudgetline>();
      for (int i = 0; i < spr_bdgline_id_array.length(); i++) {
        String strBDGLine_ID = spr_bdgline_id_array.getString(i);

        bdgline = OBDal.getInstance().get(SPRBudgetline.class, strBDGLine_ID);
        if (bdgline == null) {
          throw new Exception("@DBExecuteError@");
        }
        spr_bdgline_list.add(bdgline);
      }

      // Creating requisition
      Requisition req = generateRequisitionFromBudgetLines(spr_bdgline_list, vars.getUser());
      if (req == null) {
        throw new Exception("@ProcessRunError@");
      }

      JSONObject errorMessage = new JSONObject();
      errorMessage.put("severity", "success");
      errorMessage.put("text", Utility.messageBD(new DalConnectionProvider(false),
          "SPR_RequisitionCreated", vars.getLanguage()) + req.getDocumentNo());
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

  public static Requisition generateRequisitionFromBudgetLines(List<SPRBudgetline> budgetlines,
      String adUserId) throws Exception {
    if (budgetlines.size() == 0)
      return null;

    Client client = budgetlines.get(0).getClient();
    Organization org = budgetlines.get(0).getOrganization();
    User user = OBDal.getInstance().get(User.class, adUserId);
    Project project = budgetlines.get(0).getSPRBudget().getProject();

    DocumentType doctype = SCO_Utils.getDocTypeFromSpecial(org, "SCOREQUISITION");
    if (doctype == null) {
      throw new Exception("@SRE_DoctypeMissing@");
    }
    String DocumentNo = SCO_Utils.getDocumentNo(doctype, "M_Requisition");

    Requisition objRequisition = OBProvider.getInstance().get(Requisition.class);
    objRequisition.setClient(client);
    objRequisition.setOrganization(org);
    objRequisition.setActive(true);
    objRequisition.setProcessed(false);
    objRequisition.setCreatedBy(user);
    objRequisition.setUpdatedBy(user);
    objRequisition.setUserContact(user);
    objRequisition.setDocumentStatus("DR");
    objRequisition.setDocumentAction("CO");
    objRequisition.setDocumentNo(DocumentNo);
    objRequisition.setSprProject(project);
    objRequisition.setDescription("Generado automáticamente");

    OBDal.getInstance().save(objRequisition);

    long lineNo = 10L;
    for (int i = 0; i < budgetlines.size(); i++) {
      RequisitionLine objReqLine = OBProvider.getInstance().get(RequisitionLine.class);
      BigDecimal newQty = budgetlines.get(i).getOrderedQuantity()
          .subtract(budgetlines.get(i).getQtyinrequisition());
      if (newQty.compareTo(BigDecimal.ZERO) <= 0) {
        continue;
      }
      objReqLine.setClient(client);
      objReqLine.setOrganization(org);
      objReqLine.setActive(true);
      objReqLine.setLineNo(lineNo);
      objReqLine.setNeedByDate(new Date());
      objReqLine.setProduct(budgetlines.get(i).getProduct());
      objReqLine.setUOM(budgetlines.get(i).getUOM());
      objReqLine.setQuantity(newQty);
      objReqLine.setSprBudgetline(budgetlines.get(i));
      objReqLine.setSprBudgetitem(budgetlines.get(i).getSPRBudgetitem());
      objReqLine.setRequisition(objRequisition);

      OBDal.getInstance().save(objReqLine);
      lineNo += 10L;
    }

    OBDal.getInstance().flush();
    OBDal.getInstance().commitAndClose();

    return objRequisition;
  }

}