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
 * All portions are Copyright (C) 2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.warehouse.pickinglist;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.domain.ListTrl;
import org.openbravo.model.ad.domain.Reference;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.ad.ui.Process;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.materialmgmt.transaction.InternalMovementLine;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.service.db.CallProcess;

public class OutboundPickingListProcess {
  private static Reference plStatusRef = null;
  final private static Process movementPost = OBDal.getInstance().get(Process.class, "122");

  public static OBError close(PickingList picking) {
    OBError msg = new OBError();
    msg.setType("success");
    try {
      if ("CO".equals(picking.getPickliststatus())) {
        String strMsg = OBMessageUtils.messageBD("Success");
        completeMovements(picking);
        if (picking.getDocumentType().isOBWPLGenerateShipment()) {
          strMsg = createShipments(picking);
        }
        releaseSalesOrders(picking);

        picking.setPickliststatus("CL");
        OBDal.getInstance().save(picking);
        msg.setMessage(strMsg);
      } else {
        msg.setType("error");
        String strPLStatus = getTranslatedListValueName(getPlstatusRef(),
            picking.getPickliststatus());
        Map<String, String> map = new HashMap<String, String>();
        map.put("status", strPLStatus);
        String message = OBMessageUtils.parseTranslation("OBWPL_Close_StatusError", map);
        msg.setMessage(message);
      }
    } catch (OBException e) {
      msg = OBMessageUtils.translateError(e.getMessage());
    }

    return msg;
  }

  private static void completeMovements(PickingList picking) {
    List<InternalMovementLine> mvmtLines = null;
    if (picking.getDocumentType().isOBWPLIsGroup()) {
      mvmtLines = picking.getMaterialMgmtInternalMovementLineEMOBWPLGroupPickinglistList();
    } else {
      mvmtLines = picking.getMaterialMgmtInternalMovementLineEMOBWPLWarehousePickingListList();
    }
    for (InternalMovementLine mvmtLine : mvmtLines) {
      if ("CO".equals(mvmtLine.getOBWPLItemStatus())) {
        continue;
      }
      final ProcessInstance pinstance = CallProcess.getInstance().call(movementPost,
          (String) DalUtil.getId(mvmtLine.getMovement()), null);
      if (pinstance.getResult() != 1) {
        throw new OBException(OBMessageUtils.parseTranslation(pinstance.getErrorMsg()));
      }
    }

  }

  private static void releaseSalesOrders(PickingList picking) {
    HashSet<Order> orders = new HashSet<Order>();
    List<InternalMovementLine> mvmtLines = null;
    if (picking.getDocumentType().isOBWPLIsGroup()) {
      mvmtLines = picking.getMaterialMgmtInternalMovementLineEMOBWPLGroupPickinglistList();
    } else {
      mvmtLines = picking.getMaterialMgmtInternalMovementLineEMOBWPLWarehousePickingListList();
    }
    for (InternalMovementLine mvmtLine : mvmtLines) {
      orders.add(mvmtLine.getStockReservation().getSalesOrderLine().getSalesOrder());
    }
    for (Order order : orders) {
      order.setObwplIsinpickinglist(false);
      OBDal.getInstance().save(order);
    }
  }

  private static String createShipments(PickingList picking) throws OBException {
    String strShipDocNos = "";
    // Map with all shipments created by Sales Order
    Map<Order, ShipmentInOut> shipments = new HashMap<Order, ShipmentInOut>();
    DocumentType shipDocType = picking.getDocumentType().getOBWPLShipmentDocumentType();
    // Map with all shipment lines created by the concatenation of orderline and attribute set
    // instance.
    Map<String, ShipmentInOutLine> shipmentLines = new HashMap<String, ShipmentInOutLine>();
    List<InternalMovementLine> mvmtLines = null;
    if (picking.getDocumentType().isOBWPLIsGroup()) {
      mvmtLines = picking.getMaterialMgmtInternalMovementLineEMOBWPLGroupPickinglistList();
    } else {
      mvmtLines = picking.getMaterialMgmtInternalMovementLineEMOBWPLWarehousePickingListList();
    }
    for (InternalMovementLine mvmtLine : mvmtLines) {
      OrderLine orderLine = mvmtLine.getStockReservation().getSalesOrderLine();
      ShipmentInOut shipment = shipments.get(orderLine.getSalesOrder());
      if (shipment == null) {
        shipment = createShipment(orderLine.getSalesOrder(), shipDocType);
        shipments.put(orderLine.getSalesOrder(), shipment);
        if (StringUtils.isNotEmpty(strShipDocNos)) {
          strShipDocNos += ", ";
        }
        strShipDocNos += shipment.getDocumentNo();
      }
      String strAsi = "0";
      if (mvmtLine.getAttributeSetValue() != null) {
        strAsi = (String) DalUtil.getId(mvmtLine.getAttributeSetValue());
      }
      ShipmentInOutLine shipmentLine = shipmentLines.get(orderLine.getId() + "-" + strAsi + "-"
          + mvmtLine.getNewStorageBin().getId());
      if (shipmentLine == null) {
        shipmentLine = createShipmentLine(shipment, mvmtLine, orderLine);
        shipmentLines.put(orderLine.getId() + "-" + strAsi + "-"
            + mvmtLine.getNewStorageBin().getId(), shipmentLine);
      } else {
        shipmentLine.setMovementQuantity(shipmentLine.getMovementQuantity().add(
            mvmtLine.getMovementQuantity()));
      }
    }
    // Process shipments
    // for (ShipmentInOut shipment : shipments.values()) {
    // processShipment(shipment.getId());
    // }
    Map<String, String> map = new HashMap<String, String>();
    map.put("shipments", strShipDocNos);
    return OBMessageUtils.parseTranslation(OBMessageUtils.messageBD("OBWPL_CreatedShipments"), map);
  }

  private static ShipmentInOut createShipment(Order order, DocumentType shipDocType) {
    ShipmentInOut shipment = OBProvider.getInstance().get(ShipmentInOut.class);
    shipment.setOrganization(order.getOrganization());
    shipment.setSalesTransaction(true);
    shipment.setMovementType("C-");
    shipment.setDocumentType(shipDocType);
    shipment.setDocumentNo(OBWPL_Utils.getDocumentNo(shipDocType, "M_InOut"));
    shipment.setWarehouse(order.getWarehouse());
    shipment.setBusinessPartner(order.getBusinessPartner());
    shipment.setPartnerAddress(order.getPartnerAddress());
    shipment.setDeliveryLocation(order.getDeliveryLocation());
    shipment.setDeliveryMethod(order.getDeliveryMethod());
    shipment.setDeliveryTerms(order.getDeliveryTerms());

    shipment.setMovementDate(new Date());
    shipment.setAccountingDate(new Date());
    shipment.setSalesOrder(order);
    shipment.setUserContact(order.getUserContact());
    shipment.setOrderReference(order.getOrderReference());
    shipment.setPriority(order.getPriority());

    shipment.setProject(order.getProject());
    shipment.setActivity(order.getActivity());
    shipment.setSalesCampaign(order.getSalesCampaign());
    shipment.setStDimension(order.getStDimension());
    shipment.setNdDimension(order.getNdDimension());

    OBDal.getInstance().save(shipment);
    return shipment;
  }

  private static ShipmentInOutLine createShipmentLine(ShipmentInOut shipment,
      InternalMovementLine mvmtLine, OrderLine orderLine) {
    ShipmentInOutLine shipmentLine = OBProvider.getInstance().get(ShipmentInOutLine.class);
    shipmentLine.setOrganization(shipment.getOrganization());
    shipmentLine.setShipmentReceipt(shipment);
    shipmentLine.setSalesOrderLine(orderLine);
    Long lineNo = (shipment.getMaterialMgmtShipmentInOutLineList().size() + 1) * 10L;
    shipmentLine.setLineNo(lineNo);
    shipmentLine.setProduct(orderLine.getProduct());
    shipmentLine.setUOM(orderLine.getUOM());
    shipmentLine.setAttributeSetValue(mvmtLine.getAttributeSetValue());
    shipmentLine.setStorageBin(mvmtLine.getNewStorageBin());
    shipmentLine.setMovementQuantity(mvmtLine.getMovementQuantity());
    shipmentLine.setDescription(orderLine.getDescription());
    if (orderLine.getBOMParent() != null) {
      OBCriteria<ShipmentInOutLine> obc = OBDal.getInstance().createCriteria(
          ShipmentInOutLine.class);
      obc.add(Restrictions.eq(ShipmentInOutLine.PROPERTY_SHIPMENTRECEIPT, shipment));
      obc.add(Restrictions.eq(ShipmentInOutLine.PROPERTY_SALESORDERLINE, orderLine.getBOMParent()));
      obc.setMaxResults(1);
      shipmentLine.setBOMParent((ShipmentInOutLine) obc.uniqueResult());
    }

    OBDal.getInstance().save(shipmentLine);
    shipment.getMaterialMgmtShipmentInOutLineList().add(shipmentLine);
    OBDal.getInstance().save(shipment);

    return shipmentLine;
  }

  // private static void processShipment(String strShipmentId) {
  // final ProcessInstance pinstance = CallProcess.getInstance()
  // .call(inoutPost, strShipmentId, null);
  // if (pinstance.getResult() != 1) {
  // throw new OBException(OBMessageUtils.parseTranslation(pinstance.getErrorMsg()));
  // }
  // }

  private static String getTranslatedListValueName(Reference listRef, String value) {
    OBCriteria<org.openbravo.model.ad.domain.List> critList = OBDal.getInstance().createCriteria(
        org.openbravo.model.ad.domain.List.class);
    critList.add(Restrictions.eq(org.openbravo.model.ad.domain.List.PROPERTY_REFERENCE, listRef));
    critList.add(Restrictions.eq(org.openbravo.model.ad.domain.List.PROPERTY_SEARCHKEY, value));
    org.openbravo.model.ad.domain.List list = (org.openbravo.model.ad.domain.List) critList
        .uniqueResult();
    if (list == null) {
      return value;
    }
    // check if we have a translation
    OBCriteria<ListTrl> critListTrl = OBDal.getInstance().createCriteria(ListTrl.class);
    critListTrl.add(Restrictions.eq(ListTrl.PROPERTY_LISTREFERENCE, list));
    critListTrl.add(Restrictions.eq(ListTrl.PROPERTY_LANGUAGE, OBContext.getOBContext()
        .getLanguage()));
    ListTrl trl = (ListTrl) critListTrl.uniqueResult();
    if (trl != null) {
      return trl.getName();
    } else {
      return list.getName();
    }
  }

  private static Reference getPlstatusRef() {
    if (plStatusRef == null) {
      plStatusRef = OBDal.getInstance().get(Reference.class, "3F698D2435774CFAB5B850C7686E47F4");
    }

    return plStatusRef;
  }

  public static String checkStatus(List<InternalMovementLine> mvmtLines, boolean checkGroupStatus) {
    boolean hasComplete = false;
    boolean hasPending = false;
    boolean hasGrouped = false;
    boolean hasNotGrouped = false;
    String strStatus = "AS";
    for (InternalMovementLine mvmtLine : mvmtLines) {
      if (mvmtLine.isOBWPLRaiseIncidence()) {
        return "IN";
      }
      if (mvmtLine.getOBWPLItemStatus().equals("CO") || mvmtLine.getOBWPLItemStatus().equals("CF")) {
        hasComplete = true;
        strStatus = "CO";
      } else if (mvmtLine.getOBWPLItemStatus().equals("PE")) {
        hasPending = true;
      }
      if (mvmtLine.getOBWPLGroupPickinglist() != null) {
        hasGrouped = true;
      } else {
        hasNotGrouped = true;
      }
      if (hasPending && hasComplete) {
        strStatus = "IP";
        if (!checkGroupStatus) {
          return strStatus;
        }
      }
      if (checkGroupStatus && hasGrouped) {
        if (hasNotGrouped) {
          strStatus = "PG";
        } else {
          strStatus = "GR";
        }
      }

    }
    return strStatus;
  }

}
