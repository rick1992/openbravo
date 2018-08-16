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
package org.openbravo.warehouse.pickinglist;

import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.model.materialmgmt.transaction.InternalMovementLine;
import org.openbravo.service.db.DbUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MovementLineHandler extends BaseActionHandler {
  final private static Logger log = LoggerFactory.getLogger(MovementLineHandler.class);

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    JSONObject jsonRequest = null;

    try {
      jsonRequest = new JSONObject(content);
      final String strAction = jsonRequest.getString("action");
      if ("confirm".equals(strAction)) {
        final JSONArray movLineIds = jsonRequest.getJSONArray("movementlines");
        // Get movementlines
        for (int i = 0; i < movLineIds.length(); i++) {
          processMovementLine(movLineIds.getString(i), "CF");
        }
        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", "success");
        errorMessage.put("text", OBMessageUtils.messageBD("Success"));
        jsonRequest.put("message", errorMessage);
      } else if ("reject".equals(strAction)) {
        final JSONArray movLineIds = jsonRequest.getJSONArray("movementlines");
        // Get movementlines
        for (int i = 0; i < movLineIds.length(); i++) {
          processMovementLine(movLineIds.getString(i), "PE");
        }
        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", "success");
        errorMessage.put("text", OBMessageUtils.messageBD("Success"));
        jsonRequest.put("message", errorMessage);
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      OBDal.getInstance().rollbackAndClose();
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
      }
    }
    return jsonRequest;

  }

  public static void processMovementLine(String strMoveLineId, String strItemStatus)
      throws PropertyException {
    InternalMovementLine mvmtLine = OBDal.getInstance().get(InternalMovementLine.class,
        strMoveLineId);
    mvmtLine.setOBWPLItemStatus(strItemStatus);
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

    String prefValue = Preferences.getPreferenceValue("OBWPL_AutoClose", true, OBContext
        .getOBContext().getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(),
        OBContext.getOBContext().getUser(), OBContext.getOBContext().getRole(), null);

    PickingList picking = (PickingList) mvmtLine.getOBWPLWarehousePickingList();

    if (prefValue.equals("Y") && strStatus.equals("CO")) {
      OutboundPickingListProcess.close(picking);
    }
  }
}
