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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.materialmgmt.ReservationUtils;
import org.openbravo.model.ad.process.Parameter;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.ad.ui.Process;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.materialmgmt.onhandquantity.Reservation;
import org.openbravo.model.materialmgmt.onhandquantity.ReservationStock;
import org.openbravo.model.materialmgmt.onhandquantity.StockProposed;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;
import org.openbravo.service.db.CallStoredProcedure;
import org.openbravo.service.db.DbUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectOrdersHandler extends BaseActionHandler {
  final private static Logger log = LoggerFactory.getLogger(SelectOrdersHandler.class);

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    JSONObject jsonRequest = null;
    JSONObject response = null;
    OBContext.setAdminMode(true);
    try {
      jsonRequest = new JSONObject(content);
      response = new JSONObject();
      final String strPickingId = jsonRequest.getString("Obwpl_Pickinglist_ID");
      final PickingList picking = OBDal.getInstance().get(PickingList.class, strPickingId);
      JSONArray selectedLines = jsonRequest.getJSONArray("_selection");
      log.debug("{}", jsonRequest);
      StringBuffer msg = new StringBuffer();

      for (int i = 0; i < selectedLines.length(); i++) {
        JSONObject row = selectedLines.getJSONObject(i);
        final String strOrderId = row.getString("order");
        Order order = OBDal.getInstance().get(Order.class, strOrderId);
        String strMessage = processOrder(picking, order);
        if (msg.length() > 0) {
          msg.append("<br>");
        }
        msg.append("Order " + order.getDocumentNo() + ": " + strMessage);
      }
      JSONObject jsonMsg = new JSONObject();
      jsonMsg.put("severity", "success");
      jsonMsg.put("text", msg);

      response.put("message", jsonMsg);

    } catch (OBException e) {
      log.error("Error in SelectOrdersHandler", e);
      OBDal.getInstance().rollbackAndClose();
      try {
        response = new JSONObject();
        String message = OBMessageUtils.parseTranslation(e.getMessage());
        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", "error");
        errorMessage.put("text", message);
        response.put("message", errorMessage);
      } catch (Exception e2) {
        log.error("Error generating the error message", e2);
      }

    } catch (Exception e) {
      log.error("Error in SelectOrdersHandler", e);
      OBDal.getInstance().rollbackAndClose();
      try {
        response = new JSONObject();
        Throwable ex = DbUtility.getUnderlyingSQLException(e);
        String message = OBMessageUtils.translateError(ex.getMessage()).getMessage();
        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", "error");
        errorMessage.put("text", message);
        response.put("message", errorMessage);
      } catch (Exception e2) {
        log.error("Error generating the error message", e2);
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return response;

  }

  String processOrder(PickingList picking, Order order) {
    StringBuffer message = new StringBuffer();
    if (order.isObwplIsinpickinglist()) {
      throw new OBException(OBMessageUtils.messageBD("OBWPL_IsInPL") + order.getDocumentNo());
    }
    for (OrderLine line : order.getOrderLineList()) {
      BigDecimal pendingQty = line.getOrderedQuantity().subtract(line.getDeliveredQuantity());
      if (line.getProduct() == null || !line.getProduct().isStocked()
          || !"I".equals(line.getProduct().getProductType())
          || line.getOrderedQuantity().signum() <= 0 || pendingQty.signum() <= 0) {
        continue;
      }
      boolean hasReserve = !line.getMaterialMgmtReservationList().isEmpty();
      Reservation res = ReservationUtils.getReservationFromOrder(line);
      if ("DR".equals(res.getRESStatus())) {
        List<Object> params = new ArrayList<Object>();
        params.add(null);
        params.add(res.getId());
        params.add("PR");
        params.add(OBContext.getOBContext().getUser().getId());
        CallStoredProcedure.getInstance().call("M_RESERVATION_POST", params, null, true, false);
      }
      // Refresh
      OBDal.getInstance().refresh(res);
      if (!hasReserve) {
        res.setOBWPLGeneratedByPickingList(true);
        OBDal.getInstance().save(res);
      }

      // In case the same storage detail is proposed multiple times all the quantities need to be
      // summed up. This can happen when the reserve has some stock allocated and it is proposed
      // to retrieve not allocated stock from the same storage detail.
      List<StockProposed> stocksProposed = callGetStock(line, res);
      Map<StorageDetail, BigDecimal> allocatedQty = new HashMap<StorageDetail, BigDecimal>();
      for (StockProposed stockProposed : stocksProposed) {
        BigDecimal qtyToMove = stockProposed.getQuantity();
        if (qtyToMove.compareTo(pendingQty) == 1) {
          qtyToMove = pendingQty;
        }
        StorageDetail sd = stockProposed.getStorageDetail();

        BigDecimal qty = allocatedQty.get(sd);
        if (qty == null) {
          qty = BigDecimal.ZERO;
        }
        qty = qty.add(qtyToMove);
        OBError result = ReservationUtils.reallocateStock(res, sd.getStorageBin(),
            sd.getAttributeSetValue(), qty);
        if (!"Success".equals(result.getType())) {
          throw new OBException(result.getMessage());
        }
        // Refresh after call to reallocate stock procedure
        OBDal.getInstance().getSession().evict(res);
        res = OBDal.getInstance().get(Reservation.class, res.getId());

        // All reserved stock must be allocated. If the allocated quantity is not at least the
        // quantity reserved in this picking move the difference from the no allocated to
        // allocated.
        ReservationStock allocRS = ReservationUtils.reserveStockManual(res, sd, BigDecimal.ZERO,
            "Y");
        BigDecimal allocRSReleased = allocRS.getReleased() == null ? BigDecimal.ZERO : allocRS
            .getReleased();
        BigDecimal pendingToAllocate = qty
            .subtract(allocRS.getQuantity().subtract(allocRSReleased));
        if (pendingToAllocate.signum() == 1) {
          ReservationStock noAllocRS = ReservationUtils.reserveStockManual(res, sd,
              BigDecimal.ZERO, "N");
          allocRS.setQuantity(allocRS.getQuantity().add(pendingToAllocate));
          noAllocRS.setQuantity(noAllocRS.getQuantity().subtract(pendingToAllocate));
          OBDal.getInstance().save(allocRS);
          OBDal.getInstance().save(noAllocRS);
          OBDal.getInstance().flush();
        }
        OBDal.getInstance().refresh(res);

        allocatedQty.put(sd, qty);
        pendingQty = pendingQty.subtract(qtyToMove);
        if (pendingQty.signum() <= 0) {
          break;
        }
      }
      if (pendingQty.signum() > 0) {
        // Not enough stock
        message.append("</br>");
        message.append(line.getLineNo());
        message.append(": ");
        message.append(OBMessageUtils.messageBD("OBWPL_PartiallyReserved"));
      }
      OBDal.getInstance().flush();

      List<ReservationStock> rsToRemove = new ArrayList<ReservationStock>();
      for (ReservationStock resStock : res.getMaterialMgmtReservationStockList()) {
        BigDecimal resStockReleasedQty = resStock.getReleased() == null ? BigDecimal.ZERO
            : resStock.getReleased();
        BigDecimal qty = resStock.getQuantity().subtract(resStockReleasedQty);
        if (qty.signum() > 0) {
          OBWPL_Utils.createGoodMovement(resStock, picking, null, null);
        } else if (qty.signum() == 0) {
          rsToRemove.add(resStock);
        }
      }
      res.getMaterialMgmtReservationStockList().removeAll(rsToRemove);
      OBDal.getInstance().save(res);
      OBDal.getInstance().flush();
    }
    order.setObwplIsinpickinglist(true);
    OBDal.getInstance().save(order);
    if (message.length() == 0) {
      message.append(OBMessageUtils.messageBD("Success"));
    }
    return message.toString();
  }

  private List<StockProposed> callGetStock(OrderLine line, Reservation res) {
    Process procGetStock = OBDal.getInstance().get(Process.class,
        "FF80818132C964E30132C9747257002E");
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("AD_Client_ID", DalUtil.getId(line.getClient()));
    params.put("AD_Org_ID", DalUtil.getId(line.getOrganization()));
    params.put("M_Product_ID", DalUtil.getId(line.getProduct()));
    params.put("C_Uom_ID", DalUtil.getId(line.getUOM()));
    params.put("M_Product_Uom_ID", null);
    params.put("M_Warehouse_ID", DalUtil.getId(line.getSalesOrder().getWarehouse()));
    if (line.getAttributeSetValue() != null) {
      params.put("M_AttributesetInstance_ID", DalUtil.getId(line.getAttributeSetValue()));
    }
    params.put("Quantity", line.getOrderedQuantity().subtract(line.getDeliveredQuantity()));
    if (line.getWarehouseRule() != null) {
      params.put("M_Warehouse_Rule_ID", DalUtil.getId(line.getWarehouseRule()));
    }
    params.put("M_Reservation_ID", res.getId());

    ProcessInstance pinstance = callProcess(procGetStock, line.getId(), params);

    OBCriteria<StockProposed> critProposed = OBDal.getInstance()
        .createCriteria(StockProposed.class);
    critProposed.add(Restrictions.eq(StockProposed.PROPERTY_PROCESSINSTANCE, pinstance));
    critProposed.addOrderBy(StockProposed.PROPERTY_PRIORITY, true);

    return critProposed.list();
  }

  private ProcessInstance callProcess(org.openbravo.model.ad.ui.Process process, String recordID,
      Map<String, ?> parameters) {
    OBContext.setAdminMode();
    try {
      // Create the pInstance
      final ProcessInstance pInstance = OBProvider.getInstance().get(ProcessInstance.class);
      pInstance.setProcess(process);
      pInstance.setActive(true);
      pInstance.setAllowRead(true);
      pInstance.setRecordID(recordID);
      pInstance.setUserContact(OBContext.getOBContext().getUser());

      // now create the parameters and set their values
      int index = 0;
      for (String key : parameters.keySet()) {
        index++;
        final Object value = parameters.get(key);
        final Parameter parameter = OBProvider.getInstance().get(Parameter.class);
        parameter.setSequenceNumber(index + "");
        parameter.setParameterName(key);
        if (value instanceof String) {
          parameter.setString((String) value);
        } else if (value instanceof Date) {
          parameter.setProcessDate((Date) value);
        } else if (value instanceof BigDecimal) {
          parameter.setProcessNumber((BigDecimal) value);
        }

        // set both sides of the bidirectional association
        pInstance.getADParameterList().add(parameter);
        parameter.setProcessInstance(pInstance);
      }
      OBDal.getInstance().save(pInstance);
      OBDal.getInstance().flush();

      List<Object> params = new ArrayList<Object>();
      params.add(pInstance.getId());
      params.add("N");
      CallStoredProcedure.getInstance().call("M_GET_STOCK", params, null, true, false);

      // refresh the pInstance as the SP has changed it
      OBDal.getInstance().getSession().refresh(pInstance);
      return pInstance;
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
