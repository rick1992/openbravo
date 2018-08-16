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
 * All portions are Copyright (C) 2013 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.warehouse.pickinglist.actionhandler;

import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.materialmgmt.transaction.InternalMovementLine;
import org.openbravo.service.db.DbUtility;
import org.openbravo.warehouse.pickinglist.OutboundPickingListProcess;
import org.openbravo.warehouse.pickinglist.PickingList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RaiseIncidenceHandler extends BaseActionHandler {
  final private static Logger log = LoggerFactory.getLogger(RaiseIncidenceHandler.class);

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    JSONObject jsonResponse = new JSONObject();
    try {
      final JSONObject jsonRequest = new JSONObject(content);
      final String strMvmtLineId = jsonRequest.getString("M_MovementLine_ID");
      InternalMovementLine mvmtLine = OBDal.getInstance().get(InternalMovementLine.class,
          strMvmtLineId);
      boolean incidenceRaised = jsonRequest.getString("inpemObwplRaiseincidence").equals("Y");
      if (incidenceRaised) {
        resetIncidence(mvmtLine);
      } else {
        final JSONObject params = jsonRequest.getJSONObject("_params");
        final String strIncidenceReason = params.getString("IncidenceReason");
        raiseIncidence(mvmtLine, strIncidenceReason);
      }

      // create the result
      jsonResponse = new JSONObject();
      JSONObject message = new JSONObject();
      message.put("text", "");
      message.put("title", OBMessageUtils.messageBD("OBUIAPP_Success"));
      message.put("severity", "success");
      jsonResponse.put("message", message);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      OBDal.getInstance().rollbackAndClose();

      try {
        jsonResponse = new JSONObject();
        Throwable ex = DbUtility.getUnderlyingSQLException(e);
        String message = OBMessageUtils.translateError(ex.getMessage()).getMessage();

        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", "error");
        errorMessage.put("text", message);
        jsonResponse.put("message", errorMessage);

      } catch (Exception e2) {
        log.error(e.getMessage(), e2);
      }
    }
    return jsonResponse;
  }

  public static void raiseIncidence(InternalMovementLine mvmtLine, String strIncidenceReason) {
    mvmtLine.setOBWPLIncidenceReason(strIncidenceReason);
    mvmtLine.setOBWPLRaiseIncidence(true);
    mvmtLine.setOBWPLItemStatus("IN");
    mvmtLine.getOBWPLWarehousePickingList().setPickliststatus("IN");
    if (mvmtLine.getOBWPLGroupPickinglist() != null) {
      mvmtLine.getOBWPLGroupPickinglist().setPickliststatus("IN");
    }
  }

  public static void resetIncidence(InternalMovementLine mvmtLine) {
    mvmtLine.setOBWPLIncidenceReason(null);
    mvmtLine.setOBWPLRaiseIncidence(false);
    mvmtLine.setOBWPLItemStatus("PE");
    OBDal.getInstance().save(mvmtLine);
    // Restore picking previous status
    if (mvmtLine.getOBWPLGroupPickinglist() != null) {
      PickingList groupPL = mvmtLine.getOBWPLGroupPickinglist();
      String strStatus = OutboundPickingListProcess.checkStatus(
          groupPL.getMaterialMgmtInternalMovementLineEMOBWPLGroupPickinglistList(), false);
      groupPL.setPickliststatus(strStatus);
    }
    String strStatus = OutboundPickingListProcess.checkStatus(mvmtLine
        .getOBWPLWarehousePickingList()
        .getMaterialMgmtInternalMovementLineEMOBWPLWarehousePickingListList(), true);
    mvmtLine.getOBWPLWarehousePickingList().setPickliststatus(strStatus);

  }
}
