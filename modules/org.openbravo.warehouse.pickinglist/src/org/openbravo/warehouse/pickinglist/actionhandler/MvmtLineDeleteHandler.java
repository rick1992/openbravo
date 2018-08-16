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

import java.util.HashSet;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.materialmgmt.transaction.InternalMovementLine;
import org.openbravo.service.db.DbUtility;
import org.openbravo.warehouse.pickinglist.OutboundPickingListProcess;
import org.openbravo.warehouse.pickinglist.PickingList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MvmtLineDeleteHandler extends BaseActionHandler {
  final private static Logger log = LoggerFactory.getLogger(MvmtLineDeleteHandler.class);

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    JSONObject jsonResponse = new JSONObject();
    try {
      final JSONObject jsonData = new JSONObject(content);
      final JSONArray mvmtLineIds = jsonData.getJSONArray("mvmtLines");
      HashSet<PickingList> picklists = new HashSet<PickingList>();
      HashSet<PickingList> groupPicklists = new HashSet<PickingList>();
      HashSet<String> removedOrderIds = new HashSet<String>();
      for (int i = 0; i < mvmtLineIds.length(); i++) {
        final String strMvmtLineId = mvmtLineIds.getString(i);
        final InternalMovementLine mvmtLine = OBDal.getInstance().get(InternalMovementLine.class,
            strMvmtLineId);
        if (mvmtLine.getOBWPLWarehousePickingList() != null) {
          PickingList picklist = mvmtLine.getOBWPLWarehousePickingList();
          picklist.getMaterialMgmtInternalMovementLineEMOBWPLWarehousePickingListList().remove(
              mvmtLine);
          picklists.add(picklist);
        }
        if (mvmtLine.getOBWPLGroupPickinglist() != null) {
          PickingList picklist = mvmtLine.getOBWPLWarehousePickingList();
          picklist.getMaterialMgmtInternalMovementLineEMOBWPLWarehousePickingListList().remove(
              mvmtLine);
          groupPicklists.add(picklist);
        }
        removedOrderIds.add(mvmtLine.getStockReservation().getSalesOrderLine().getSalesOrder()
            .getId());

        if (!mvmtLine.getOBWPLItemStatus().equals("PE")) {
          throw new OBException(OBMessageUtils.messageBD("DocumentProcessed"));
        }
        mvmtLine.setOBWPLAllowDelete(true);
        OBDal.getInstance().save(mvmtLine);
        OBDal.getInstance().flush();

        // delete it
        OBDal.getInstance().remove(mvmtLine.getMovement());
        OBDal.getInstance().flush();
      }
      for (PickingList picklist : picklists) {
        for (InternalMovementLine mvmtLine : picklist
            .getMaterialMgmtInternalMovementLineEMOBWPLWarehousePickingListList()) {
          removedOrderIds.remove(mvmtLine.getStockReservation().getSalesOrderLine().getSalesOrder()
              .getId());
        }
        String strStatus = OutboundPickingListProcess.checkStatus(
            picklist.getMaterialMgmtInternalMovementLineEMOBWPLWarehousePickingListList(), true);
        if (!strStatus.equals(picklist.getPickliststatus())) {
          picklist.setPickliststatus(strStatus);
        }
      }
      for (PickingList picklist : groupPicklists) {
        for (InternalMovementLine mvmtLine : picklist
            .getMaterialMgmtInternalMovementLineEMOBWPLWarehousePickingListList()) {
          removedOrderIds.remove(mvmtLine.getStockReservation().getSalesOrderLine().getSalesOrder()
              .getId());
        }
        String strStatus = OutboundPickingListProcess.checkStatus(
            picklist.getMaterialMgmtInternalMovementLineEMOBWPLGroupPickinglistList(), false);
        if ("AS".equals(strStatus) && picklist.getUserContact() == null) {
          strStatus = "DR";
        }
        if (!strStatus.equals(picklist.getPickliststatus())) {
          picklist.setPickliststatus(strStatus);
        }
      }
      for (String strOrderId : removedOrderIds) {
        Order order = OBDal.getInstance().get(Order.class, strOrderId);
        order.setObwplIsinpickinglist(false);
        OBDal.getInstance().save(order);
      }
      // create the result
      jsonResponse = new JSONObject();
      JSONObject message = new JSONObject();
      message.put(
          "text",
          OBMessageUtils.messageBD("OBUIAPP_DeleteResult").replace("%0",
              Integer.toString(mvmtLineIds.length())));
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

}
