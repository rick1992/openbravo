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
 * All portions are Copyright (C) 2011-2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package pe.com.unifiedgo.project.actionhandler;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBDao;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.procurement.Requisition;
import org.openbravo.model.procurement.RequisitionLine;
import org.openbravo.service.db.DbUtility;

import pe.com.unifiedgo.project.data.SPRBudgetline;

/**
 * 
 * @author gorkaion
 * 
 */
public class SPRRequisitionPickEditLines extends BaseProcessActionHandler {
  private static Logger log = Logger.getLogger(SPRRequisitionPickEditLines.class);

  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) {
    JSONObject jsonRequest = null;
    OBContext.setAdminMode();
    try {
      jsonRequest = new JSONObject(content);
      log.debug(jsonRequest);
      // When the focus is NOT in the tab of the button (i.e. any child tab) and the tab does not
      // contain any record, the inpmRequisitionId parameter contains "null" string. Use
      // M_Requisition_ID
      // instead because it always contains the id of the selected lines.
      // Issue 20585: https://issues.openbravo.com/view.php?id=20585
      final String strReqId = jsonRequest.getString("M_Requisition_ID");
      // System.out.println("strReqId:" + strReqId);
      Requisition req = OBDal.getInstance().get(Requisition.class, strReqId);
      if (req != null) {
        // System.out.println("req is not null");
        List<String> idList = OBDao.getIDListFromOBObject(req.getProcurementRequisitionLineList());
        createRequisitionLines(req, jsonRequest, idList);
      } else
        System.out.println("req is null");

    } catch (Exception e) {
      log.error(e.getMessage(), e);
      VariablesSecureApp vars = RequestContext.get().getVariablesSecureApp();

      try {
        jsonRequest = new JSONObject();
        Throwable ex = DbUtility.getUnderlyingSQLException(e);
        String message = OBMessageUtils.translateError(ex.getMessage()).getMessage();
        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", "error");
        errorMessage.put("text", message);
        jsonRequest.put("message", errorMessage);

      } catch (Exception e2) {
        log.error(e.getMessage(), e2);
        // do nothing, give up
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return jsonRequest;
  }

  private void createRequisitionLines(Requisition req, JSONObject jsonRequest, List<String> idList)
      throws JSONException {
    JSONArray selectedLines = jsonRequest.getJSONArray("_selection");
    // if no lines selected don't do anything.
    if (selectedLines.length() == 0) {
      // removeNonSelectedLines(idList, req);
      return;
    }

    String strDescription = "";

    for (long i = 0; i < selectedLines.length(); i++) {
      JSONObject selectedLine = selectedLines.getJSONObject((int) i);
      log.debug(selectedLine);
      SPRBudgetline bdgline = OBDal.getInstance().get(SPRBudgetline.class,
          selectedLine.getString("id"));

      strDescription = bdgline.getSPRBudget().getDescription();
      break;
    }

    Long lineNo = (req.getProcurementRequisitionLineList().size() + 1) * 10L;
    for (long i = 0; i < selectedLines.length(); i++) {
      JSONObject selectedLine = selectedLines.getJSONObject((int) i);
      log.debug(selectedLine);
      RequisitionLine newReqLine = null;
      // boolean notExistsRequisitionLine = selectedLine.get("goodsShipmentLine").equals(null);
      // if (notExistsRequisitionLine) {
      // newReqLine = OBProvider.getInstance().get(RequisitionLine.class);
      // } else {
      // newReqLine = OBDal.getInstance().get(RequisitionLine.class,
      // selectedLine.get("goodsShipmentLine"));
      // idList.remove(selectedLine.get("goodsShipmentLine"));
      // }
      newReqLine = OBProvider.getInstance().get(RequisitionLine.class);
      newReqLine.setRequisition(req);
      newReqLine.setClient(req.getClient());
      newReqLine.setOrganization(req.getOrganization());
      newReqLine.setLineNo(lineNo);
      newReqLine.setNeedByDate(new Date());

      SPRBudgetline bdgline = OBDal.getInstance().get(SPRBudgetline.class,
          selectedLine.getString("id"));
      newReqLine.setProduct(bdgline.getProduct());
      newReqLine.setUOM(bdgline.getUOM());
      BigDecimal newqty = new BigDecimal(selectedLine.getString("newqtyordered"));
      BigDecimal availqty = new BigDecimal(selectedLine.getString("maxqtyordered"));
      if (newqty.compareTo(availqty) > 0) {
        throw new JSONException("@SPR_ReqPick_OutOfRange@" + availqty);
      }
      newReqLine.setSreQtyrequested(newqty);
      newReqLine.setQuantity(newqty);
      newReqLine.setSprBudgetline(bdgline);
      newReqLine.setSprBudgetitem(bdgline.getSPRBudgetitem());
      // if (notExistsRequisitionLine) {
      List<RequisitionLine> reqLines = req.getProcurementRequisitionLineList();
      reqLines.add(newReqLine);
      req.setProcurementRequisitionLineList(reqLines);
      // }

      OBDal.getInstance().save(newReqLine);
      OBDal.getInstance().save(req);
      OBDal.getInstance().flush();

      lineNo += 10L;
    }

    req.setDescription(strDescription);
    OBDal.getInstance().save(req);

    // removeNonSelectedLines(idList, req);
  }

  // private void removeNonSelectedLines(List<String> idList, Requisition req) {
  //
  // if (idList.size() > 0) {
  //
  // for (String id : idList) {
  //
  // RequisitionLine reqline = OBDal.getInstance().get(RequisitionLine.class, id);
  // OBDal.getInstance().remove(reqline);
  // req.getProcurementRequisitionLineList().remove(reqline);
  // }
  // OBDal.getInstance().save(req);
  // OBDal.getInstance().flush();
  // }
  // }

}