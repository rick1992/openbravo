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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.time.DateUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.materialmgmt.ReservationUtils;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.ad.ui.Process;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.materialmgmt.onhandquantity.Reservation;
import org.openbravo.model.materialmgmt.onhandquantity.ReservationStock;
import org.openbravo.model.materialmgmt.transaction.ProductionLine;
import org.openbravo.model.materialmgmt.transaction.ProductionPlan;
import org.openbravo.model.materialmgmt.transaction.ProductionTransaction;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.service.db.CallProcess;
import org.openbravo.service.db.CallStoredProcedure;
import org.openbravo.service.db.DbUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pe.com.unifiedgo.requirement.data.SREServiceorderline;
import pe.com.unifiedgo.warehouse.data.SwaRequerimientoReposicion;
import pe.com.unifiedgo.warehouse.data.SwaRequerimientoReposicionDetail;

public class PickingListActionHandler extends BaseActionHandler {
  final private static Logger log = LoggerFactory.getLogger(PickingListActionHandler.class);

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    JSONObject jsonResponse = null;

    try {
      JSONObject jsonRequest = new JSONObject(content);
      final String strAction = jsonRequest.getString("action");
      final JSONArray plIds = jsonRequest.getJSONArray("pickinglist");

      if ("cancel".equals(strAction)) {
        PickingList pl = OBDal.getInstance().get(PickingList.class, plIds.getString(0));

        System.out.println("AJUA");
        if (pl != null && pl.isSplIsprinted() && pl.getPickliststatus().equals("DR")) {
          System.out.println("El documento en estado borrador ya fue impreso");
          OBDal.getInstance().rollbackAndClose();
          jsonResponse = new JSONObject();
          JSONObject errorMessage = new JSONObject();
          errorMessage.put("severity", "error");
          errorMessage.put("text", OBMessageUtils.messageBD("swa_pickingwasprinted"));
          jsonResponse.put("message", errorMessage);

          return jsonResponse;
        }

        if (pl.getSwaRequerepo() != null)
          jsonResponse = doCancelfromReposition(plIds);
        else if (pl.getSsaServiceOrder() != null)
          jsonResponse = doCancelfromService(plIds);
        else if (pl.getSwaMProduction() != null)
          jsonResponse = doCancelfromProduction(plIds);
        else if (pl.getSwaRequerepo() == null && pl.getSsaServiceOrder() == null
            && pl.getSwaCOrder() == null)
          jsonResponse = doCancelfromSystem(plIds);
        else
          jsonResponse = doCancel(plIds);

      } else if ("process".equals(strAction)) {
        jsonResponse = doProcess(plIds);
      } else if ("close".equals(strAction)) {
        jsonResponse = doClose(plIds);
      }

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

  public JSONObject doProcess(JSONArray plIds) throws JSONException {
    JSONObject jsonResponse = new JSONObject();
    // Get pickinglist
    for (int i = 0; i < plIds.length(); i++) {

      PickingList pl = OBDal.getInstance().get(PickingList.class, plIds.getString(i));
      if (pl.getSwaMProduction() != null) { // Si el Picking List fue creado por Armado
        processPL_Production(plIds.getString(i));
      } else { // Si el Picking List fue creado por Pedido de venta u otro documento que genere
               // Documento de salida asociado
        processPL(plIds.getString(i));
      }

    }
    JSONObject errorMessage = new JSONObject();
    errorMessage.put("severity", "success");
    errorMessage.put("text", OBMessageUtils.messageBD("Success"));
    jsonResponse.put("message", errorMessage);

    return jsonResponse;
  }

  private void processPL(String plID) {
    PickingList pl = OBDal.getInstance().get(PickingList.class, plID);
    final Set<String> ships = new HashSet<String>();
    final Set<String> orders = new HashSet<String>();
    int var = 0;

    /*
     * Validation:Si no se ha impreso la Lista de pciking por lo menos una vez, no podrà ser
     * completada
     */
    if (pl.isSplIsprinted() == false) {
      // throw new OBException(OBMessageUtils.messageBD("swa_PLwithoutprint"));
    }
    /* End Validation */

    for (ShipmentInOutLine iol : pl.getMaterialMgmtShipmentInOutLineEMObwplPickinglistIDList()) {
      if (("DR").equals(iol.getShipmentReceipt().getDocumentStatus())) {
        ships.add(iol.getShipmentReceipt().getId());
      }

      if (iol.getSalesOrderLine() != null) {
        var = 1;
        orders.add(iol.getSalesOrderLine().getSalesOrder().getId());
      }

    }

    for (String strShipId : ships) {
      final List<Object> param = new ArrayList<Object>();
      param.add(null);
      param.add(strShipId);
      ShipmentInOut shipment = OBDal.getInstance().get(ShipmentInOut.class, strShipId.trim());

      Date now = DateUtils.truncate(new Date(), Calendar.DATE);
      // if(shipment.getDocumentType().getScoSpecialdoctype().equals("SCOMMSHIPMENT") ||
      // shipment.getDocumentType().getScoSpecialdoctype().equals("SWAMMSHIPMENTINDIRECT")){
      shipment.setSwaShipstatus("SWA_PACK");
      shipment.setSwaShipmentaction("SWA_TOUWAY");
      shipment.setSwaPackdatetime(new Date());
      shipment.setSwaUwaydatetime(null);
      shipment.setSwaDelidatetime(null);
      shipment.setSwaPesoGuiaTotal(pl.getSsaWeightPk());
      shipment.setSwaNumcajasGuiaTotal(pl.getSsaNumboxesPk());
      shipment.setSwaCubicajeGuiaTotal(pl.getSsaVolumenPk());
      OBDal.getInstance().save(shipment);
      OBDal.getInstance().flush(); //
      // }

      /*
       * Ahora Todo documento de Salida serà Completado si el picking se completa //Solo Completamos
       * la guia si es de tipo nota de salida SWAMMDISPATCH para cualquier otro se debe completar
       * manualmente if(shipment.isSwaIsWaitPicking()==false &&
       * shipment.getDocumentType().getScoSpecialdoctype().equals("SWAMMDISPATCH"))
       * CallStoredProcedure.getInstance().call("M_Inout_POST", param, null, true, false);
       * if(shipment.isSwaIsWaitPicking()==true && shipment.getDocumentStatus().equals("DR"))
       * CallStoredProcedure.getInstance().call("M_Inout_POST", param, null, true, false);
       */

      CallStoredProcedure.getInstance().call("M_Inout_POST", param, null, true, false);

      ShipmentInOut ship = OBDal.getInstance().get(ShipmentInOut.class, strShipId.trim());
      OBDal.getInstance().save(ship);
      OBDal.getInstance().flush(); //

    }
    pl.setPickliststatus("CO");
    pl.setSwaZoneStatus("PA");

    if (var == 0) {
      for (String strOrderId : orders) {
        // check there is no orderline in other draft pickinglist
        StringBuilder whereClause = new StringBuilder();
        List<Object> parameters = new ArrayList<Object>();
        whereClause.append(" as iol ");
        whereClause.append(" where iol.");
        whereClause.append(ShipmentInOutLine.PROPERTY_OBWPLPICKINGLIST);
        whereClause.append("." + PickingList.PROPERTY_PICKLISTSTATUS + " = 'DR'");
        whereClause.append(" and ");
        whereClause.append(ShipmentInOutLine.PROPERTY_OBWPLPICKINGLIST);
        whereClause.append(" is not null ");
        whereClause.append(" and ");
        whereClause.append(ShipmentInOutLine.PROPERTY_OBWPLPICKINGLIST);
        whereClause.append(" <> ? ");
        parameters.add(pl);
        whereClause.append(" and ");
        whereClause.append(ShipmentInOutLine.PROPERTY_SALESORDERLINE);
        whereClause.append("." + OrderLine.PROPERTY_ID + " IN (");
        whereClause.append(" SELECT ");
        whereClause.append(OrderLine.PROPERTY_ID);
        whereClause.append(" FROM OrderLine as ol");
        whereClause.append(" WHERE ol." + OrderLine.PROPERTY_SALESORDER + ".id = ?");
        parameters.add(strOrderId);
        whereClause.append(" )");
        OBQuery<ShipmentInOutLine> obData = OBDal.getInstance().createQuery(
            ShipmentInOutLine.class, whereClause.toString(), parameters);
        if (obData.count() == 0) {
          Order o = OBDal.getInstance().get(Order.class, strOrderId);
          o.setObwplIsinpickinglist(false);
        }
      }
    }

    OBDal.getInstance().flush();
  }

  private void processPL_Production(String plID) {
    PickingList pl = OBDal.getInstance().get(PickingList.class, plID);

    final List<Object> param = new ArrayList<Object>();
    param.add(null);

    // Esto era para que cada vez que se complete el Picking, se ejecute el productionrun y termine
    // de
    // completar el Armado, ahora lo que se hace es simplemente completar el picking

    Process process = null;
    try {
      process = OBDal.getInstance().get(Process.class, "536784DAF24D425393F95AB1AD7AA3D5");
    } catch (OBException e) {
    }
    Map<String, String> parameters = null;
    parameters = new HashMap<String, String>();
    // parameters.put("MustBeStocked", "Y");

    ProcessInstance pinstance = CallProcess.getInstance().call(process, pl.getId(), parameters);

    if (pinstance.getResult() == 0) {
      throw new OBException(OBMessageUtils.messageBD(pinstance.getErrorMsg()));
    }
    // System.out.println("result: " + pinstance.getResult());
    // System.out.println("errorMsg" + pinstance.getErrorMsg());

    pl.setPickliststatus("CO");
    pl.setSwaZoneStatus("PA");

    OBDal.getInstance().flush();
  }

  private JSONObject doCancel(JSONArray plIds) throws JSONException {

    JSONObject jsonResponse = new JSONObject();
    boolean processedShip = false;
    // Get orders
    for (int i = 0; i < plIds.length(); i++) {
      PickingList pl = OBDal.getInstance().get(PickingList.class, plIds.getString(i));
      ShipmentInOut ship = null;
      for (ShipmentInOutLine iol : pl.getMaterialMgmtShipmentInOutLineEMObwplPickinglistIDList()) {

        OrderLine oline = null;
        if (iol.getSalesOrderLine() != null) // Comportamiento normal
          oline = iol.getSalesOrderLine();
        else
          //
          oline = iol.getScoVoidorderline();

        oline.getSalesOrder().setObwplIsinpickinglist(false);
        ship = iol.getShipmentReceipt();
        // if shipment is completed or voided,
        // then picklist statsus is set to cancel
        if ("CO".equals(ship.getDocumentStatus())) {
          throw new OBException(OBMessageUtils.messageBD("swa_nodeletepickingconditional"));
        }
        if ("VO".equals(ship.getDocumentStatus())) {
          pl.setPickliststatus("CA");
          processedShip = true;
          OBDal.getInstance().save(pl);
        } else if ("CL".equals(ship.getSalesOrder().getDocumentStatus())) {
          pl.setPickliststatus("CA");
          processedShip = true;
          OBDal.getInstance().save(pl);
        } else {
          Reservation res = ReservationUtils.getReservationFromOrder(oline);
          if (res.isOBWPLGeneratedByPickingList()) {
            // If the reservation was created using picking list it has to be deleted.
            removeReservation(iol);
          }
          OBDal.getInstance().remove(iol);
        }

        //
      }
      OBDal.getInstance().flush();
      if (!processedShip) {
        // Only remove ship that has no lines.
        pl.getMaterialMgmtShipmentInOutLineEMObwplPickinglistIDList().clear();
        OBDal.getInstance().remove(pl);
        if (ship != null)
          OBDal.getInstance().remove(ship);
        OBDal.getInstance().flush();
      }
    }
    JSONObject errorMessage = new JSONObject();
    errorMessage.put("severity", "success");
    errorMessage.put("text", OBMessageUtils.messageBD("Success"));
    jsonResponse.put("message", errorMessage);
    return jsonResponse;
  }

  private JSONObject doCancelfromService(JSONArray plIds) throws JSONException {
    JSONObject jsonResponse = new JSONObject();
    boolean processedShip = false;
    // Get orders
    for (int i = 0; i < plIds.length(); i++) {
      PickingList pl = OBDal.getInstance().get(PickingList.class, plIds.getString(i));
      ShipmentInOut ship = null;
      pl.getSsaServiceOrder().setObwplIsinpickinglist(false);
      for (ShipmentInOutLine iol : pl.getMaterialMgmtShipmentInOutLineEMObwplPickinglistIDList()) {
        SREServiceorderline soline = iol.getSreServiceOrderLine(); // si
        // soline.getSalesOrder().setObwplIsinpickinglist(false);
        ship = iol.getShipmentReceipt();
        // then picklist status is set to cancel
        if ("CO".equals(ship.getDocumentStatus())) {
          throw new OBException(OBMessageUtils.messageBD("swa_nodeletepickingconditional"));
        }
        if ("VO".equals(ship.getDocumentStatus())) {
          pl.setPickliststatus("CA");
          processedShip = true;
          OBDal.getInstance().save(pl);
        } else {
          OBDal.getInstance().remove(iol);
        }
      }
      OBDal.getInstance().flush();
      if (!processedShip) {
        // Only remove ship that has no lines.
        pl.getMaterialMgmtShipmentInOutLineEMObwplPickinglistIDList().clear();
        OBDal.getInstance().remove(pl);
        if (ship != null)
          OBDal.getInstance().remove(ship);
        OBDal.getInstance().flush();
      }
    }

    JSONObject errorMessage = new JSONObject();
    errorMessage.put("severity", "success");
    errorMessage.put("text", OBMessageUtils.messageBD("Success"));
    jsonResponse.put("message", errorMessage);
    return jsonResponse;
  }

  private JSONObject doCancelfromSystem(JSONArray plIds) throws JSONException {
    JSONObject jsonResponse = new JSONObject();
    boolean processedShip = false;
    // Get orders
    PickingList pl = OBDal.getInstance().get(PickingList.class, plIds.getString(0));
    ShipmentInOut ship = null;
    System.out.println("HOLA 1");
    for (ShipmentInOutLine iol : pl.getMaterialMgmtShipmentInOutLineEMObwplPickinglistIDList()) {
      ship = iol.getShipmentReceipt();
      if ("CO".equals(ship.getDocumentStatus())) {
        throw new OBException(OBMessageUtils.messageBD("swa_nodeletepickingconditional"));
      } else if ("VO".equals(ship.getDocumentStatus())) {
        pl.setPickliststatus("CA");
        processedShip = true;
        OBDal.getInstance().save(pl);
      } else {

        Reservation res = ReservationUtils.getReservationFromShipment(iol);
        if (res != null) {
          removeReservationManual(iol);
        }
      }

      iol.setObwplPickinglist(null);
      OBDal.getInstance().save(iol);

    }
    OBDal.getInstance().flush();
    if (!processedShip) {

      pl.setPickliststatus("CA");
      pl.getMaterialMgmtShipmentInOutLineEMObwplPickinglistIDList().clear();
      OBDal.getInstance().save(pl);

      OBDal.getInstance().flush();
    }
    JSONObject errorMessage = new JSONObject();
    errorMessage.put("severity", "success");
    errorMessage.put("text", OBMessageUtils.messageBD("Success"));
    jsonResponse.put("message", errorMessage);
    return jsonResponse;
  }

  private JSONObject doCancelfromReposition(JSONArray plIds) throws JSONException {
    JSONObject jsonResponse = new JSONObject();
    boolean processedShip = false;
    // Get orders
    PickingList pl = OBDal.getInstance().get(PickingList.class, plIds.getString(0));
    ShipmentInOut ship = null;
    ShipmentInOut shipreceipt = null;
    SwaRequerimientoReposicion reposition = pl.getSwaRequerepo();
    for (ShipmentInOutLine iol : pl.getMaterialMgmtShipmentInOutLineEMObwplPickinglistIDList()) {
      ship = iol.getShipmentReceipt();
      // if shipment is completed or voided,
      // then picklist status is set to cancel
      // if ("CO".equals(ship.getDocumentStatus()) || "VO".equals(ship.getDocumentStatus())) {
      if ("CO".equals(ship.getDocumentStatus())) {
        throw new OBException(OBMessageUtils.messageBD("swa_nodeletepickingconditional"));
      } else if ("VO".equals(ship.getDocumentStatus())) {
        pl.setPickliststatus("CA");
        processedShip = true;
        OBDal.getInstance().save(pl);
      } else {
        OBDal.getInstance().remove(iol);
      }
    }
    OBDal.getInstance().flush();
    if (!processedShip) {
      if (ship != null) {
        shipreceipt = ship.getSwaAsociateInout();
        List<ShipmentInOutLine> orline = shipreceipt.getMaterialMgmtShipmentInOutLineList();
        for (int i = 0; i < orline.size(); i++) {
          ShipmentInOutLine sline = OBDal.getInstance().get(ShipmentInOutLine.class,
              orline.get(i).getId());
          if ("CO".equals(ship.getDocumentStatus()) || "VO".equals(ship.getDocumentStatus())) {
            pl.setPickliststatus("CA");
          } else {
            OBDal.getInstance().remove(sline);
          }
        }
      }
      reposition.setPickinglistState("NG");
      // Only remove ship that has no lines.
      pl.getMaterialMgmtShipmentInOutLineEMObwplPickinglistIDList().clear();
      OBDal.getInstance().remove(pl);
      if (ship != null)
        OBDal.getInstance().remove(ship);
      if (shipreceipt != null)
        OBDal.getInstance().remove(shipreceipt);
      OBDal.getInstance().save(reposition);
      OBDal.getInstance().flush();
    }

    removeLocatorReserveReposition(reposition);

    JSONObject errorMessage = new JSONObject();
    errorMessage.put("severity", "success");
    errorMessage.put("text", OBMessageUtils.messageBD("Success"));
    jsonResponse.put("message", errorMessage);
    return jsonResponse;
  }

  private void removeLocatorReserveReposition(SwaRequerimientoReposicion reposition) {

    List<SwaRequerimientoReposicionDetail> repoDetail = reposition.getSwaRequerepoDetailList();
    for (int i = 0; i < repoDetail.size(); i++) {
      Reservation res = ReservationUtils.getReservationFromReposition(repoDetail.get(i));
      for (ReservationStock resStock : res.getMaterialMgmtReservationStockList()) {
        resStock.setStorageBin(null);
        OBDal.getInstance().save(resStock);
      }
    }
  }

  private JSONObject doCancelfromProduction(JSONArray plIds) throws JSONException {
    JSONObject jsonResponse = new JSONObject();
    JSONObject errorMessage = new JSONObject();

    // Get orders
    PickingList pl = OBDal.getInstance().get(PickingList.class, plIds.getString(0));

    try {
      ProductionTransaction production = pl.getSwaMProduction();
      if (production != null && production.isProcessed() == false
          && pl.getPickliststatus().compareTo("CO") == 0) {
        production.setSwaPickinglistGenerate(false);
        production.setSWAEnPicking(false);
        for (ProductionPlan pplan : production.getMaterialMgmtProductionPlanList()) {
          for (ProductionLine pline : pplan.getManufacturingProductionLineList()) {
            pline.setSwaPickinglist(null);
            OBDal.getInstance().save(pline);
            OBDal.getInstance().flush();
          }
        }
        OBDal.getInstance().save(production);
        OBDal.getInstance().flush();
      } else if (production != null && production.isProcessed() == true) {
        throw new OBException(OBMessageUtils.messageBD("swa_productionIsProcess"));
      } else if (production != null && pl.getPickliststatus().compareTo("DR") == 0) {
        production.setSWAEnPicking(false);
        // production.setSwaIsinpicking(false);
        OBDal.getInstance().save(production);
        OBDal.getInstance().flush();
      }
      pl.setPickliststatus("CA");
      pl.setSwaMProduction(null);
      OBDal.getInstance().save(pl);
      OBDal.getInstance().flush();

    } catch (OBException e) {
      OBDal.getInstance().rollbackAndClose();
      errorMessage.put("Error", "Error");
      errorMessage.put("text", e.getMessage());
      jsonResponse.put("message", errorMessage);
      return jsonResponse;
    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      errorMessage.put("Error", "Error");
      errorMessage.put("text", OBMessageUtils.messageBD("ProcessRunError"));
      jsonResponse.put("message", errorMessage);
      return jsonResponse;
    }

    errorMessage.put("severity", "success");
    errorMessage.put("text", OBMessageUtils.messageBD("Success"));
    jsonResponse.put("message", errorMessage);
    return jsonResponse;
  }

  private void removeReservation(ShipmentInOutLine iol) {
    Reservation res = ReservationUtils.getReservationFromOrder(iol.getSalesOrderLine());
    String iolLocator = iol.getStorageBin().getId();
    String iolAttr = iol.getAttributeSetValue() == null ? "0" : iol.getAttributeSetValue().getId();
    BigDecimal iolQty = iol.getMovementQuantity();
    ReservationStock resStock = null;
    for (ReservationStock rSAux : res.getMaterialMgmtReservationStockList()) {
      String rSLocator = rSAux.getStorageBin().getId();
      String rSAttr = rSAux.getAttributeSetValue() == null ? "0" : rSAux.getAttributeSetValue()
          .getId();
      BigDecimal rSQty = rSAux.getQuantity();
      if (rSLocator.equals(iolLocator) && iolAttr.equals(rSAttr) && iolQty.compareTo(rSQty) == 0) {
        resStock = rSAux;
        break;
      }
    }
    if (resStock != null) {
      resStock.setQuantity(resStock.getQuantity().subtract(iol.getMovementQuantity()));
      if (resStock.getQuantity().equals(BigDecimal.ZERO)) {
        res.getMaterialMgmtReservationStockList().remove(resStock);
        // Flush to persist changes in database and execute triggers.
        OBDal.getInstance().flush();
        OBDal.getInstance().refresh(res);
        if (res.getReservedQty().equals(BigDecimal.ZERO)
            && res.getMaterialMgmtReservationStockList().isEmpty()) {
          if (res.getRESStatus().equals("CO")) {
            // Unprocess reservation
            ReservationUtils.processReserve(res, "RE");
            OBDal.getInstance().save(res);
            OBDal.getInstance().flush();
          }
          OBDal.getInstance().remove(res);
        }
      }
    }
  }

  private void removeReservationManual(ShipmentInOutLine iol) {
    Reservation res = ReservationUtils.getReservationFromShipment(iol);
    String iolLocator = iol.getStorageBin().getId();
    String iolAttr = iol.getAttributeSetValue() == null ? "0" : iol.getAttributeSetValue().getId();
    BigDecimal iolQty = iol.getMovementQuantity();
    ReservationStock resStock = null;
    for (ReservationStock rSAux : res.getMaterialMgmtReservationStockList()) {
      String rSLocator = rSAux.getStorageBin().getId();
      String rSAttr = rSAux.getAttributeSetValue() == null ? "0" : rSAux.getAttributeSetValue()
          .getId();
      BigDecimal rSQty = rSAux.getQuantity();
      if (rSLocator.equals(iolLocator) && iolAttr.equals(rSAttr) && iolQty.compareTo(rSQty) == 0) {
        resStock = rSAux;
        break;
      }
    }
    if (resStock != null) {
      resStock.setQuantity(resStock.getQuantity().subtract(iol.getMovementQuantity()));
      if (resStock.getQuantity().equals(BigDecimal.ZERO)) {
        res.getMaterialMgmtReservationStockList().remove(resStock);
        // Flush to persist changes in database and execute triggers.
        OBDal.getInstance().flush();
        OBDal.getInstance().refresh(res);
        if (res.getReservedQty().equals(BigDecimal.ZERO)
            && res.getMaterialMgmtReservationStockList().isEmpty()) {
          if (res.getRESStatus().equals("CO")) {
            // Unprocess reservation
            ReservationUtils.processReserve(res, "RE");
            OBDal.getInstance().save(res);
            OBDal.getInstance().flush();
          }
          OBDal.getInstance().remove(res);
        }
      }
    }
  }

  private JSONObject doClose(JSONArray plIds) throws JSONException {
    boolean hasErrors = false;
    StringBuffer finalMsg = new StringBuffer();
    for (int i = 0; i < plIds.length(); i++) {
      final String strPLId = plIds.getString(i);
      final PickingList pl = OBDal.getInstance().get(PickingList.class, strPLId);
      if (finalMsg.length() > 0) {
        finalMsg.append("\n");
      }
      finalMsg.append(pl.getDocumentNo() + ": ");
      OBError plMsg = OutboundPickingListProcess.close(pl);
      if (!"success".equals(plMsg.getType())) {
        hasErrors = true;
      }
      finalMsg.append(plMsg.getMessage());
    }
    JSONObject jsonResponse = new JSONObject();
    JSONObject jsonMsg = new JSONObject();
    jsonMsg.put("severity", hasErrors ? "error" : "success");
    jsonMsg.put("text", finalMsg.toString());
    jsonResponse.put("message", jsonMsg);

    return jsonResponse;
  }

}
