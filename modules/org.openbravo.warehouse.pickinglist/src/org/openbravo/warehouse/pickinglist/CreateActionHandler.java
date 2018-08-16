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
 * All portions are Copyright (C) 2012-2014 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.warehouse.pickinglist;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.axis.utils.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.erpCommon.utility.PropertyNotFoundException;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.materialmgmt.ReservationUtils;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.ad.ui.Process;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.onhandquantity.Reservation;
import org.openbravo.model.materialmgmt.onhandquantity.ReservationStock;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;
import org.openbravo.model.materialmgmt.transaction.ProductionPlan;
import org.openbravo.model.materialmgmt.transaction.ProductionTransaction;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.service.db.CallProcess;
import org.openbravo.service.db.CallStoredProcedure;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.db.DbUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pe.com.unifiedgo.core.SCR_Utils;
import pe.com.unifiedgo.core.data.SCRComboItem;
import pe.com.unifiedgo.requirement.data.SREServiceorderline;
import pe.com.unifiedgo.warehouse.data.SWAProductByAnaquelV;
import pe.com.unifiedgo.warehouse.data.SwaRequerimientoReposicion;
import pe.com.unifiedgo.warehouse.data.SwaRequerimientoReposicionDetail;
import pe.com.unifiedgo.warehouse.data.swaProductWarehouse;

public class CreateActionHandler extends BaseActionHandler {
  final private static Logger log = LoggerFactory.getLogger(CreateActionHandler.class);
  final private Date now = DateUtils.truncate(new Date(), Calendar.DATE);
  long lineNo;

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    JSONObject jsonResponse = new JSONObject();
    OBContext.setAdminMode(true);
    try {
      final JSONObject jsonRequest = new JSONObject(content);
      final String strAction = jsonRequest.getString("action");

      VariablesSecureApp vars = RequestContext.get().getVariablesSecureApp();
      if ("getGroupingCriteria".equals(strAction)) {
        jsonResponse = getActionComboBox(vars);
        return jsonResponse;
      } else if ("getOutboundLocatorLists".equals(strAction)) {
        jsonResponse = getLocatorCombos(jsonRequest);
        return jsonResponse;
      } else if ("create".equals(strAction)) {
        final String strPLType = jsonRequest.getString("plType");
        if ("OUT".equals(strPLType)) {
          return jsonResponse = doCreateOutbound(jsonRequest);
        } else {
          return jsonResponse = doCreate(jsonRequest, vars);
        }
      } else if ("showGRFields".equals(strAction)) {
        jsonResponse = needsSalesShipmentParameters(jsonRequest);
        return jsonResponse;
      } else if ("getPhysicalDocNo".equals(strAction)) {
        jsonResponse = getInOutPhysicalDocumentNo(jsonRequest, vars);
        return jsonResponse;
      }

    } catch (Exception e) {
      log.error("Error in CreateActionHandler", e);
      OBDal.getInstance().rollbackAndClose();

      try {
        jsonResponse = new JSONObject();
        Throwable ex = DbUtility.getUnderlyingSQLException(e);
        String message = OBMessageUtils.translateError(ex.getMessage()).getMessage();
        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", "error");
        errorMessage.put("text", "".equals(message) ? e : message);
        jsonResponse.put("message", errorMessage);
      } catch (Exception e2) {
        log.error("Error generating the error message", e2);
      }
    } finally {
      OBContext.restorePreviousMode();
    }

    return jsonResponse;
  }

  private JSONObject needsSalesShipmentParameters(JSONObject jsonRequest) throws JSONException {
    final JSONArray orderIds = jsonRequest.getJSONArray("orders");
    boolean isSalesShipment = false;
    if (orderIds.length() > 0) {
      Order order = OBDal.getInstance().get(Order.class, orderIds.getString(0));
      if (order != null) {
        if (!"P".equals(order.getDeliveryMethod()) && !"SCR_C".equals(order.getDeliveryMethod())
            && !"SCR_VSGR".equals(order.getDeliveryMethod())
            && !"SCR_P_NIC".equals(order.getDeliveryMethod())
            && !"SCR_C_NIC".equals(order.getDeliveryMethod())
            && !"SCR_VSGR_NIC".equals(order.getDeliveryMethod())) {
          isSalesShipment = true;
        }
      }
    }

    JSONObject response = new JSONObject();
    response.put("isSalesShipment", (isSalesShipment ? "true" : "false"));
    return response;
  }

  private JSONObject getInOutPhysicalDocumentNo(JSONObject jsonRequest, VariablesSecureApp vars)
      throws JSONException {
    ConnectionProvider conn = new DalConnectionProvider();

    final JSONArray orderIds = jsonRequest.getJSONArray("orders");
    String strPhysicalDocNoGR = null;

    if (orderIds.length() > 0) {
      Order order = OBDal.getInstance().get(Order.class, orderIds.getString(0));
      if (order != null) {
        if (!"P".equals(order.getDeliveryMethod()) && !"SCR_C".equals(order.getDeliveryMethod())
            && !"SCR_VSGR".equals(order.getDeliveryMethod())
            && !"SCR_P_NIC".equals(order.getDeliveryMethod())
            && !"SCR_C_NIC".equals(order.getDeliveryMethod())
            && !"SCR_VSGR_NIC".equals(order.getDeliveryMethod())) {
          try {
            strPhysicalDocNoGR = SCR_Utils.getInOutPhysicalDocumentNo(conn, vars,
                OBContext.getOBContext().getUser().getId(), order.getClient().getId(),
                order.getOrganization().getId(), order.getWarehouse().getId());
          } catch (ServletException e) {
            // System.out.println("Error en funcion
            // CreateActionHandler.getInOutPhysicalDocumentNo()");
          }
        }
      }
    }

    JSONObject response = new JSONObject();
    response.put("physicalDocNoGR", strPhysicalDocNoGR);
    return response;
  }

  private JSONObject getActionComboBox(VariablesSecureApp vars) throws Exception {
    final String SO_WINDOW_ID = "143";
    final String PICK_OPTIONS = "C13AD141699C45168090496CF88FEED9";
    JSONObject response = new JSONObject();
    DalConnectionProvider conn = new DalConnectionProvider(false);
    ComboTableData comboTableData = new ComboTableData(vars, conn, "LIST", "", PICK_OPTIONS, "",
        Utility.getContext(conn, vars, "#AccessibleOrgTree", SO_WINDOW_ID),
        Utility.getContext(conn, vars, "#User_Client", SO_WINDOW_ID), 0);
    Utility.fillSQLParameters(conn, vars, null, comboTableData, SO_WINDOW_ID, "");
    FieldProvider[] fpArray = comboTableData.select(false);
    JSONObject valueMap = new JSONObject();
    for (FieldProvider fp : fpArray) {
      String key = fp.getField("id");
      String value = fp.getField("name");
      valueMap.put(key, value);
    }
    response.put("valueMap", valueMap);
    String val = "NG";
    try {
      val = Preferences.getPreferenceValue("OBWPL_GroupPL", true,
          OBContext.getOBContext().getCurrentClient(),
          OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
          OBContext.getOBContext().getRole(), null);
    } catch (PropertyNotFoundException e) {
      // Do nothing
    } catch (PropertyException e) {
      log.error("Error retrieving preference", e);
    }
    response.put("defaultValue", val);
    return response;
  }

  private JSONObject getLocatorCombos(JSONObject jsonRequest) throws JSONException {
    JSONArray orders = jsonRequest.getJSONArray("orders");
    JSONArray returnWhs = new JSONArray();
    List<Warehouse> whs = new ArrayList<Warehouse>();
    for (int i = 0; i < orders.length(); i++) {
      String strOrderId = orders.getString(i);
      Order order = OBDal.getInstance().get(Order.class, strOrderId);
      if (order == null)
        continue;
      Warehouse wh = order.getWarehouse();
      if (whs.contains(wh)) {
        continue;
      }
      whs.add(wh);

      OBCriteria<Locator> critSB = OBDal.getInstance().createCriteria(Locator.class);
      critSB.add(Restrictions.eq(Locator.PROPERTY_OBWHSTYPE, "OUT"));
      critSB.add(Restrictions.eq(Locator.PROPERTY_WAREHOUSE, wh));
      List<Locator> outbounds = critSB.list();
      JSONObject locators = new JSONObject();
      for (Locator outbound : outbounds) {
        locators.put(outbound.getId(), outbound.getIdentifier());
      }
      JSONObject whValueMap = new JSONObject();
      whValueMap.put("valueMap", locators);
      whValueMap.put("warehouseId", wh.getId());
      whValueMap.put("warehouseName", wh.getName());
      returnWhs.put(whValueMap);
    }
    JSONObject response = new JSONObject();

    response.put("warehouses", returnWhs);
    return response;
  }

  private JSONObject doCreateOutbound(JSONObject jsonRequest) throws JSONException {
    JSONObject jsonResponse = new JSONObject();
    final JSONArray orderIds = jsonRequest.getJSONArray("orders");
    final JSONObject locatorIds = jsonRequest.getJSONObject("locators");
    StringBuffer msg = new StringBuffer();

    for (int i = 0; i < orderIds.length(); i++) {
      Order order = OBDal.getInstance().get(Order.class, orderIds.getString(i));
      String strLocatorId = locatorIds.getString(order.getWarehouse().getId());
      Locator outbound = null;
      if (StringUtils.isEmpty(strLocatorId)) {
        throw new OBException(OBMessageUtils.messageBD("OBWPL_OutboundLocatorNotDefined"));
      } else {
        outbound = OBDal.getInstance().get(Locator.class, strLocatorId);
      }
      PickingList pickingList = createPL(order, outbound);
      String strMessage = new SelectOrdersHandler().processOrder(pickingList, order);
      if (msg.length() > 0) {
        msg.append("<br>");
      }
      msg.append("Order " + order.getDocumentNo() + ": " + strMessage);
    }

    JSONObject jsonMsg = new JSONObject();
    jsonMsg.put("severity", "success");
    jsonMsg.put("text", msg);

    jsonResponse.put("message", jsonMsg);

    return jsonResponse;
  }

  private JSONObject doCreate(JSONObject jsonRequest, VariablesSecureApp vars)
      throws JSONException {
    JSONObject jsonResponse = new JSONObject();
    final JSONArray orderIds = jsonRequest.getJSONArray("orders");
    final String groupPL = jsonRequest.getString("groupingCrit");

    // Get Parameters for GR
    String strPhysicalDocNo = jsonRequest.getString("physicalDocumentNo");
    String strMovementDate = jsonRequest.getString("movementDate");

    // Get orders
    HashMap<String, PickingList> createdPLs = new HashMap<String, PickingList>();
    HashSet<String> notCompletedPL = new HashSet<String>();
    String strResultMsg = "";
    int flag = 0; // flag 0 Sales Order | flag 1 Reposition | flag 2 Service
    // Order
    for (int i = 0; i < orderIds.length(); i++) {
      Order order = OBDal.getInstance().get(Order.class, orderIds.getString(i));
      SwaRequerimientoReposicion reposition = null;
      ProductionTransaction production = null;
      // ADD THIS

      if (order == null) {
        flag = 1;
        reposition = OBDal.getInstance().get(SwaRequerimientoReposicion.class,
            orderIds.getString(i));

        if (reposition == null) {
          flag = 3;
          production = OBDal.getInstance().get(ProductionTransaction.class, orderIds.getString(i));

          if (production == null) {
            JSONObject errorMessage = new JSONObject();
            errorMessage.put("severity", "success");
            errorMessage.put("title", OBMessageUtils.messageBD("OBWPL_PickingList_Created"));
            errorMessage.put("text", "No hay picking para este tipo de ordenes");
            jsonResponse.put("message", errorMessage);
            return jsonResponse;
          }
        } else {
          // Verificando si la reposicion es de tipo muestra
          if (reposition.getDocumentType().getScoSpecialdoctype().equals("SWAREQUESTSAMPE")) {

            if (!reposition.getAlertStatus().equals("CO")) {// Tener
              // cuidado // porque // el // proceso // a // llamar // hace // reserva,
              // podria // darse // el // caso // de // que // se // haga // dos // reservas
              // si // elimino // el // picking // y // creo // otro // nuevamente
              Process process = null;
              try {
                process = OBDal.getInstance().get(Process.class,
                    "183931BF970346048E418CACAA862E62"); // process
                // para // completar
              } catch (OBException e) {
              }
              Map<String, String> parameters = null;
              parameters = new HashMap<String, String>();
              parameters.put("MustBeStocked", "Y");
              ProcessInstance pinstance = CallProcess.getInstance().call(process,
                  reposition.getId(), parameters);
            }
          }
        }
      } else {
        if (order.isSalesTransaction() == false && order.isSsaIsserviceorder() == true) {
          flag = 2;
        } else if (order.isSalesTransaction() == true) {
          flag = 0;
        } else if (order.isSalesTransaction() == false
            && order.getDocumentType().getScoSpecialdoctype().equals("SSARTVORDER")) {
          flag = 4;// Devolución a Proveedor

        } else {
          JSONObject errorMessage = new JSONObject();
          errorMessage.put("severity", "success");
          errorMessage.put("title", OBMessageUtils.messageBD("OBWPL_PickingList_Created"));
          errorMessage.put("text", "No hay picking para este tipo de ordenes");
          jsonResponse.put("message", errorMessage);
          return jsonResponse;
        }
      }
      // END ADD THIS
      PickingList pickingList = null;
      String strDesc = "";
      if ("NG".equals(groupPL)) {

        // add vafaster
        // flag0: Sales Order | flag1: Reposition Between Warehouse |
        // flag2: Service Purchase order
        // ;
        if (flag == 1) {
          // Validate If Exist any PL with this ORDER
          JSONObject errorMessage = new JSONObject();
          errorMessage.put("severity", "error");
          errorMessage.put("title", "Error");
          OBCriteria<PickingList> plverify = OBDal.getInstance().createCriteria(PickingList.class);
          plverify.add(Restrictions.eq(PickingList.PROPERTY_SWAREQUEREPO, reposition));
          plverify.add(Restrictions.ne(PickingList.PROPERTY_PICKLISTSTATUS, "CA"));
          PickingList pl_get = null;
          try {
            pl_get = (PickingList) plverify.uniqueResult();
            if (pl_get != null && !"CA".equals(pl_get.getPickliststatus())) {
              errorMessage.put("text",
                  OBMessageUtils.messageBD("SWA_RepositionWithPL") + " " + pl_get.getDocumentNo());
              jsonResponse.put("message", errorMessage);
              return jsonResponse;
            }
          } catch (Exception e) {
            errorMessage.put("text", OBMessageUtils.messageBD("SWA_RepositionWithPL"));
            jsonResponse.put("message", errorMessage);
            return jsonResponse;
          }

          pickingList = createPL_Reposition(reposition, null);

        } else if (flag == 2) {
          // Picking List to Service Purchase Order
          JSONObject errorMessage = new JSONObject();
          errorMessage.put("severity", "error");
          errorMessage.put("title", "Error");
          OBCriteria<PickingList> plverify = OBDal.getInstance().createCriteria(PickingList.class);
          plverify.add(Restrictions.eq(PickingList.PROPERTY_SSASERVICEORDER, order));
          plverify.add(Restrictions.ne(PickingList.PROPERTY_PICKLISTSTATUS, "CA"));
          PickingList pl_get = null;
          try {
            List<PickingList> lisPickingList = plverify.list();
            if (lisPickingList.size() > 0) {
              errorMessage.put("text", OBMessageUtils.messageBD("SWA_RepositionWithPL") + " "
                  + lisPickingList.get(0).getDocumentNo());
              jsonResponse.put("message", errorMessage);
              return jsonResponse;
            }
          } catch (Exception e) {
            errorMessage.put("text", OBMessageUtils.messageBD("SWA_RepositionWithPL"));
            jsonResponse.put("message", errorMessage);
            return jsonResponse;
          }
          pickingList = createPL3(order, null);
        } else if (flag == 3) {
          // Picking List to Production
          JSONObject errorMessage = new JSONObject();
          errorMessage.put("severity", "error");
          errorMessage.put("title", "Error");
          // Verify if Production has plan
          List<ProductionPlan> pplan = production.getMaterialMgmtProductionPlanList();
          if (pplan.size() == 0) {
            errorMessage.put("text",
                OBMessageUtils.messageBD("swa_bom_need_pplan") + " " + production.getDocumentNo());
            jsonResponse.put("message", errorMessage);
            return jsonResponse;
          }

          OBCriteria<PickingList> plverify = OBDal.getInstance().createCriteria(PickingList.class);
          plverify.add(Restrictions.eq(PickingList.PROPERTY_SWAMPRODUCTION, production));
          plverify.add(Restrictions.ne(PickingList.PROPERTY_PICKLISTSTATUS, "CA"));
          PickingList pl_get = null;

          try {
            List<PickingList> lisPickingList = plverify.list();
            if (lisPickingList.size() > 0) {
              errorMessage.put("text", OBMessageUtils.messageBD("SWA_ProductionWithPL") + " "
                  + lisPickingList.get(0).getDocumentNo());
              jsonResponse.put("message", errorMessage);
              return jsonResponse;
            }
          } catch (Exception e) {
            errorMessage.put("text", OBMessageUtils.messageBD("SWA_ProductionWithPL"));
            jsonResponse.put("message", errorMessage);
            return jsonResponse;
          }
          pickingList = createPL_Production(production, null);
        } else if (flag == 4) {
          JSONObject errorMessage = new JSONObject();
          errorMessage.put("severity", "error");
          errorMessage.put("title", "Error");
          OBCriteria<PickingList> plverify = OBDal.getInstance().createCriteria(PickingList.class);
          plverify.add(Restrictions.eq(PickingList.PROPERTY_SWARTVCORDER, order));
          plverify.add(Restrictions.ne(PickingList.PROPERTY_PICKLISTSTATUS, "CA"));
          PickingList pl_get = null;
          try {
            List<PickingList> lisPickingList = plverify.list();
            if (lisPickingList.size() > 0) {
              errorMessage.put("text", OBMessageUtils.messageBD("SWA_ReturnToVendorWithPL") + " "
                  + lisPickingList.get(0).getDocumentNo());
              jsonResponse.put("message", errorMessage);
              return jsonResponse;
            }
          } catch (Exception e) {
            errorMessage.put("text", OBMessageUtils.messageBD("SWA_ReturnToVendorWithPL"));
            jsonResponse.put("message", errorMessage);
            return jsonResponse;
          }
          pickingList = createPL4(order, null);
        } else {// flag 0
          pickingList = createPL(order, null);
        }
        // ////END ADD
        strResultMsg += pickingList.getDocumentNo() + ", ";
      } else {
        String strKey = order.getOrganization().getId();
        if ("GBP".equals(groupPL)) {
          strKey += "-" + (String) DalUtil.getId(order.getBusinessPartner());
        }
        pickingList = createdPLs.get(strKey);
        if (pickingList == null) {
          // Create Picking List
          pickingList = createPL(order, null);
          createdPLs.put(strKey, pickingList);
          strResultMsg += pickingList.getDocumentNo() + ", ";
        } else {
          strDesc = pickingList.getDescription() + ", \n";
        }
      }
      if (flag == 1) {
        strDesc += OBMessageUtils.messageBD("OBWPL_docNo") + reposition.getDocumentNo();
      } else if (flag == 3) {
        strDesc += OBMessageUtils.messageBD("OBWPL_docNo") + production.getDocumentNo();
      } else if (flag == 4) {
        strDesc += OBMessageUtils.messageBD("OBWPL_docNo") + order.getDocumentNo();
      } else {
        strDesc += OBMessageUtils.messageBD("OBWPL_docNo") + " " + order.getDocumentNo() + " "
            + OBMessageUtils.messageBD("OBWPL_BPartner") + order.getBusinessPartner().getName();
      }

      if (strDesc.length() > 2000) {
        strDesc = strDesc.substring(0, 1997) + "...";
      }
      pickingList.setDescription(strDesc);
      OBDal.getInstance().save(pickingList);

      if (flag == 1) { // Crear Guias para transferencias

        // 1. Se crea la Nota de Ingreso al almacén de Recepciòn En
        // estado Borrador
        // Si se trata de una transferencia se creara el ingreso
        // temporal al almacén de Tránsito
        // Si se trata de una transferencia por Muestra se crearà el
        // ingreso al almacèn configurado
        // como el destino en la Orden de Transferencia (Deberìa de ser
        // el alamcèn de Muestras)
        ShipmentInOut notaIngreso_ref = createInOut_reposition_Referenceinout(reposition,
            pickingList, notCompletedPL);
        strResultMsg += OBMessageUtils.messageBD("swa_shipmentcreated");

        // 2.Creando El documento de Salida En estado de Borrador desde
        // el almacén de transferencia
        strResultMsg += createInOut_reposition(reposition, pickingList, notCompletedPL,
            notaIngreso_ref);
        reposition.setPickinglistState("GR");
        reposition.setAlertStatus("CO");// si no estuvo completado.
        OBDal.getInstance().save(reposition);
      } else if (flag == 2) { // crear guia para Ordenes de Servicio
        strResultMsg += OBMessageUtils.messageBD("swa_shipmentcreated");
        strResultMsg += processOrder3(order, pickingList, notCompletedPL);
      } else if (flag == 0) { // Crear guias para Pedidos de Venta
        strResultMsg += OBMessageUtils.messageBD("swa_shipmentcreated");
        strResultMsg += processOrder(order, pickingList, notCompletedPL, vars, strPhysicalDocNo,
            strMovementDate);
      } else if (flag == 3) {// Actualizar las lineas de la producción con
        // la referencia a
        updatelinesProductionPlan(production, pickingList);
      } else if (flag == 4) {// Actualizar las lineas de la devolucion de
        // proveedor
        processOrder4(order, pickingList, notCompletedPL);
      }
    }

    JSONObject errorMessage = new JSONObject();
    errorMessage.put("severity", "success");
    errorMessage.put("title", OBMessageUtils.messageBD("OBWPL_PickingList_Created"));
    errorMessage.put("text", strResultMsg);
    jsonResponse.put("message", errorMessage);

    return jsonResponse;
  }

  // /////////////////Picking Listo to Service Order
  private PickingList createPL3(Order order, Locator locator) {
    PickingList pickingList = OBProvider.getInstance().get(PickingList.class);
    pickingList.setOrganization(order.getOrganization());
    pickingList.setDocumentdate(now);
    boolean useOutbound = locator != null;
    DocumentType plDocType = OBWPL_Utils.getDocumentType(order.getOrganization(), "OBWPL_doctype",
        useOutbound);
    if (plDocType == null) {
      throw new OBException(OBMessageUtils.messageBD("OBWPL_DoctypeMissing"));
    }
    pickingList.setDocumentType(plDocType);
    pickingList.setDocumentNo(OBWPL_Utils.getDocumentNo(plDocType, "OBWPL_PickingList"));
    pickingList.setPickliststatus("DR");
    pickingList.setOutboundStorageBin(locator);
    pickingList.setSsaServiceOrder(order);
    pickingList.setSwaMWarehouse(order.getWarehouse());
    pickingList.setSWAUpdateReason(order.getSsaComboItem());
    OBDal.getInstance().save(pickingList);

    return pickingList;
  }

  // /////////////////Picking Listo to Production
  private PickingList createPL_Production(ProductionTransaction production, Locator locator) {
    PickingList pickingList = OBProvider.getInstance().get(PickingList.class);
    pickingList.setOrganization(production.getOrganization());
    pickingList.setDocumentdate(now);
    boolean useOutbound = locator != null;
    DocumentType plDocType = OBWPL_Utils.getDocumentType(production.getOrganization(),
        "OBWPL_doctype", useOutbound);
    if (plDocType == null) {
      throw new OBException(OBMessageUtils.messageBD("OBWPL_DoctypeMissing"));
    }
    pickingList.setDocumentType(plDocType);
    pickingList.setDocumentNo(OBWPL_Utils.getDocumentNo(plDocType, "OBWPL_PickingList"));
    pickingList.setPickliststatus("DR");
    pickingList.setOutboundStorageBin(locator);
    pickingList.setSwaMProduction(production);
    pickingList.setSwaMWarehouse(production.getSWAWarehouse());
    pickingList.setSWAUpdateReason(production.getSWAOrderType());
    OBDal.getInstance().save(pickingList);

    production.setSWAEnPicking(true);
    OBDal.getInstance().save(production);
    OBDal.getInstance().save(pickingList);

    return pickingList;
  }

  // ///ADD funcion Pickin List para crear Picking List a partir de
  // requiremetrepositcion
  private PickingList createPL_Reposition(SwaRequerimientoReposicion order, Locator locator) {

    // Esto es para el nuevo proceso de Transferencia Directa,
    // En transferencia Directa, nosotros indicamos como es el tipo de movimiento
    // Este movimiento serà para ingreso y salida
    SCRComboItem cboItemReposition = null;
    if (order.getComboItem() != null)
      cboItemReposition = OBDal.getInstance().get(SCRComboItem.class, order.getComboItem().getId());

    // Combo Item for Reposition

    OBCriteria<SCRComboItem> comboItem = OBDal.getInstance().createCriteria(SCRComboItem.class);
    if ("CO".equals(order.getMWarehouse().getSwaWarehousetype().toString().trim()))
      comboItem
          .add(Restrictions.eq(SCRComboItem.PROPERTY_SEARCHKEY, "ReposicionPorConsignacionOut"));
    else if ("CO".equals(order.getFromMWarehouse().getSwaWarehousetype().toString().trim()))
      comboItem
          .add(Restrictions.eq(SCRComboItem.PROPERTY_SEARCHKEY, "SalidaDevolucionConsignacion"));
    else if ("VS".equals(order.getFromMWarehouse().getSwaWarehousetype().toString().trim()))
      comboItem
          .add(Restrictions.eq(SCRComboItem.PROPERTY_SEARCHKEY, "SalidaDevolucionVentaSujeta"));
    else if ("MI".equals(order.getMWarehouse().getSwaWarehousetype().toString().trim()))
      comboItem.add(Restrictions.eq(SCRComboItem.PROPERTY_SEARCHKEY, "ReposicionPorMinaOut"));
    else if ("MU".equals(order.getMWarehouse().getSwaWarehousetype().toString().trim()))
      comboItem.add(Restrictions.eq(SCRComboItem.PROPERTY_SEARCHKEY, "SalidaPorMuestra"));
    else if ("VS".equals(order.getMWarehouse().getSwaWarehousetype().toString().trim()))
      comboItem.add(Restrictions.eq(SCRComboItem.PROPERTY_SEARCHKEY, "SalidaVentaSujeta"));
    else
      comboItem
          .add(Restrictions.eq(SCRComboItem.PROPERTY_SEARCHKEY, "ReposicionEntreAlmacenesOut"));

    SCRComboItem comboItem_get = null;
    try {

      comboItem_get = cboItemReposition;// Si hay un comboItem por TransferenciaDirecta, Entonces
                                        // asumir ese como tipo de movimiento
      if (comboItem_get == null)
        comboItem_get = (SCRComboItem) comboItem.uniqueResult();

      if (comboItem_get == null)
        throw new OBException(OBMessageUtils.messageBD("swa_not_ComboItem_repositionOut"));
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("swa_not_ComboItem_repositionOut"));
    }

    // validando Tipo de documento para Picking

    boolean useOutbound = locator != null;
    DocumentType plDocType = OBWPL_Utils.getDocumentType(order.getOrganization(), "OBWPL_doctype",
        useOutbound);
    if (plDocType == null) {
      throw new OBException(OBMessageUtils.messageBD("OBWPL_DoctypeMissing"));
    }

    // creando Lista de Picking
    PickingList pickingList = OBProvider.getInstance().get(PickingList.class);
    pickingList.setOrganization(order.getOrganization());
    pickingList.setDocumentdate(now);
    pickingList.setDocumentType(plDocType);
    pickingList.setDocumentNo(OBWPL_Utils.getDocumentNo(plDocType, "OBWPL_PickingList"));
    pickingList.setPickliststatus("DR");
    pickingList.setOutboundStorageBin(locator);
    pickingList.setSwaRequerepo(order);
    pickingList.setSwaMWarehouse(order.getFromMWarehouse());
    pickingList.setSWAUpdateReason(comboItem_get);
    OBDal.getInstance().save(pickingList);

    return pickingList;
  }

  // ///END funcion Pickin List para crear Picking List a partir de
  // requiremetrepositcion

  public PickingList createPL(Order order, Locator locator) {
    PickingList pickingList = OBProvider.getInstance().get(PickingList.class);
    pickingList.setOrganization(order.getOrganization());
    // Removing: FIXME-ENERO
    pickingList.setDocumentdate(now);
    // pickingList.setDocumentdate(order.getOrderDate());
    boolean useOutbound = locator != null;
    DocumentType plDocType = OBWPL_Utils.getDocumentType(order.getOrganization(), "OBWPL_doctype",
        useOutbound);
    if (plDocType == null) {
      throw new OBException(OBMessageUtils.messageBD("OBWPL_DoctypeMissing"));
    }
    pickingList.setDocumentType(plDocType);
    pickingList.setDocumentNo(OBWPL_Utils.getDocumentNo(plDocType, "OBWPL_PickingList"));
    pickingList.setPickliststatus("DR");
    pickingList.setOutboundStorageBin(locator);
    pickingList.setSwaCOrder(order);
    pickingList.setSwaMWarehouse(order.getWarehouse());
    pickingList.setSwaDatepromised(order.getScheduledDeliveryDate());
    pickingList.setSWAUpdateReason(order.getSsaComboItem());
    OBDal.getInstance().save(pickingList);

    return pickingList;
  }

  private PickingList createPL4(Order order, Locator locator) {
    PickingList pickingList = OBProvider.getInstance().get(PickingList.class);
    pickingList.setOrganization(order.getOrganization());
    pickingList.setDocumentdate(now);
    boolean useOutbound = locator != null;
    DocumentType plDocType = OBWPL_Utils.getDocumentType(order.getOrganization(), "OBWPL_doctype",
        useOutbound);
    if (plDocType == null) {
      throw new OBException(OBMessageUtils.messageBD("OBWPL_DoctypeMissing"));
    }
    pickingList.setDocumentType(plDocType);
    pickingList.setDocumentNo(OBWPL_Utils.getDocumentNo(plDocType, "OBWPL_PickingList"));
    pickingList.setPickliststatus("DR");
    pickingList.setOutboundStorageBin(locator);
    pickingList.setSwaRtvCOrder(order);
    // pickingList.setSwaCOrder(order);
    pickingList.setSwaMWarehouse(order.getWarehouse());
    pickingList.setSwaDatepromised(order.getScheduledDeliveryDate());
    pickingList.setSWAUpdateReason(order.getSsaComboItem());
    OBDal.getInstance().save(pickingList);

    return pickingList;
  }

  private void updatelinesProductionPlan(ProductionTransaction production,
      PickingList pickingList) {
    DalConnectionProvider conn = new DalConnectionProvider();
    try {
      PickingListCreateActionData.updateProductionLines(conn, pickingList.getId(),
          production.getId());
      PickingListCreateActionData.updateProduction(conn, production.getId());
      OBDal.getInstance().commitAndClose();
    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      throw new OBException(OBMessageUtils.messageBD("OBWPL_IsExcluded"));
    }

  }

  public String processOrder(Order order, PickingList pickingList, HashSet<String> notCompletedPL) {
    if (order.isObwplIsinpickinglist()) {

      OBCriteria<PickingList> picking = OBDal.getInstance().createCriteria(PickingList.class);
      picking.add(Restrictions.eq(PickingList.PROPERTY_SWACORDER, order));
      picking.add(Restrictions.ne(PickingList.PROPERTY_PICKLISTSTATUS, "CA"));
      picking.setMaxResults(1);
      PickingList pickinglist = (PickingList) picking.uniqueResult();
      if (pickinglist != null)
        // throw new
        // OBException(OBMessageUtils.messageBD("OBWPL_IsInPL") +
        // order.getDocumentNo()+" - "+pickinglist.getDocumentNo());
        throw new OBException(OBMessageUtils.messageBD("swa_IsInPL") + order.getDocumentNo() + " - "
            + pickinglist.getDocumentNo());

    }
    if (order.isObwplReadypl()) {
      throw new OBException(OBMessageUtils.messageBD("OBWPL_IsExcluded") + order.getDocumentNo());
    }
    order.setObwplIsinpickinglist(true);

    String description = "";
    // Create In Out
    ShipmentInOut shipment = OBProvider.getInstance().get(ShipmentInOut.class);
    shipment.setOrganization(order.getOrganization());
    shipment.setSalesTransaction(true);

    if (order.getDocumentType().getScoSpecialdoctype().trim().equals("SSASAMPLEORDER"))
      shipment.setMovementType("ssa_S-");
    else
      shipment.setMovementType("C-");

    String specialDoc = "SCOMMSHIPMENT";
    String strMethod = order.getDeliveryMethod();
    String SpecialDocFromOrder = order.getDocumentType().getScoSpecialdoctype().trim();

    if (strMethod.compareTo("SCR_C") == 0 || strMethod.compareTo("P") == 0
        || strMethod.compareTo("SCR_VSGR") == 0 || strMethod.compareTo("SCR_P_NIC") == 0
        || strMethod.compareTo("SCR_C_NIC") == 0 || strMethod.compareTo("SCR_VSGR_NIC") == 0)
      specialDoc = "SWAMMDISPATCH";

    // MODIFY BY VAFASTER. GENERATE SHIPMENT WITH DOC TYPE. IN ORG,
    // DOCSPECIALTYPE, WAREHOUSE
    // ADD LINES
    OrganizationStructureProvider osp = OBContext.getOBContext()
        .getOrganizationStructureProvider(order.getClient().getId());

    OBCriteria<DocumentType> shipDocType = OBDal.getInstance().createCriteria(DocumentType.class);
    shipDocType.add(Restrictions.eq(DocumentType.PROPERTY_SCOSPECIALDOCTYPE, specialDoc));
    shipDocType.add(Restrictions.in("organization.id",
        osp.getParentTree(order.getOrganization().getId(), true)));
    shipDocType.setMaxResults(1);
    DocumentType P_shipDocType = (DocumentType) shipDocType.uniqueResult();
    // END ADD LINES

    if ("".equals(P_shipDocType) || P_shipDocType == null) {
      throw new OBException(OBMessageUtils.messageBD("OBWPL_DocType_Shipment"));
    }

    shipment.setDocumentType(P_shipDocType);
    shipment.setDocumentNo(OBWPL_Utils.getDocumentNo(P_shipDocType, "M_InOut"));
    shipment.setWarehouse(order.getWarehouse());
    shipment.setBusinessPartner(order.getBusinessPartner());
    shipment.setPartnerAddress(order.getPartnerAddress());
    shipment.setDeliveryLocation(order.getDeliveryLocation());
    shipment.setDeliveryMethod(order.getDeliveryMethod());
    shipment.setDeliveryTerms(order.getDeliveryTerms());

    // Removing: FIXME-ENERO
    shipment.setMovementDate(now);
    shipment.setAccountingDate(now);
    // shipment.setMovementDate(order.getOrderDate());
    // shipment.setAccountingDate(order.getOrderDate());
    shipment.setSalesOrder(order);
    shipment.setUserContact(order.getUserContact());
    shipment.setOrderReference(order.getOrderReference());
    shipment.setFreightCostRule(order.getFreightCostRule());
    shipment.setFreightAmount(order.getFreightAmount());
    shipment.setShippingCompany(order.getShippingCompany());
    shipment.setPriority(order.getPriority());
    shipment.setProject(order.getProject());
    shipment.setActivity(order.getActivity());
    shipment.setSalesCampaign(order.getSalesCampaign());
    shipment.setStDimension(order.getStDimension());
    shipment.setNdDimension(order.getNdDimension());
    shipment.setTrxOrganization(order.getTrxOrganization());

    // description = "Picking List: " + pickingList.getDocumentNo();
    if (!"".equals(order.getDescription()) && order.getDescription() != null)
      description = description + " " + order.getDescription();
    // description = description + "\n\n" + order.getDescription();
    if (order.getDropShipPartner() != null)
      description = description + " " + order.getDropShipPartner().getName();
    if (order.getDropShipLocation() != null)
      description = description + ": " + order.getDropShipLocation().getName();
    shipment.setDescription(description);

    shipment.setDocumentStatus("DR");
    shipment.setDocumentAction("CO");
    shipment.setProcessNow(false);
    shipment.setSWAUpdateReason(order.getSsaComboItem());

    OBDal.getInstance().save(shipment);

    lineNo = 10L;
    for (OrderLine orderLine : order.getOrderLineList()) {
      // Only consider pending to deliver lines of stocked item products.
      if (orderLine.getProduct() == null || !orderLine.getProduct().getProductType().equals("I")
          || (orderLine.getProduct().getProductType().equals("I")
              && !orderLine.getProduct().isStocked())
          || orderLine.getOrderedQuantity().signum() == 0
          || orderLine.getOrderedQuantity().compareTo(orderLine.getDeliveredQuantity()) <= 0
          || (orderLine.isObwplReadypl() != null && orderLine.isObwplReadypl())) {
        continue;
      }

      if (orderLine.getOrderedQuantity().signum() < 0) {
        throw new OBException(OBMessageUtils.messageBD("OBWPL_OrderedQtyMustBePositive"));
      }
      // Removing: FIXME-ENERO(descomentar)
      if (orderLine.getProduct().isStocked()) {

        processOrderLine(orderLine, shipment, pickingList, notCompletedPL);
      } else {
        // Create Shipment Line for non stocked products.
        BigDecimal qty = orderLine.getOrderedQuantity().subtract(orderLine.getDeliveredQuantity());
        // Removing: FIXME-ENERO reemplazar por funcion
        // createShipmentLine(...)
        createShipmentLine(orderLine.getAttributeSetValue(), null, qty, orderLine, shipment,
            pickingList);
        // createShipmentLine_FIXME_ENERO_REPLACE(orderLine.getAttributeSetValue(),
        // null, qty,
        // orderLine, shipment,
        // pickingList);
        OBDal.getInstance().flush();
      }
    }

    OBDal.getInstance().refresh(shipment);
    if (shipment.getMaterialMgmtShipmentInOutLineList().size() == 0) {
      throw new OBException(OBMessageUtils.messageBD("NotEnoughAvailableStock"));
    }

    return shipment.getDocumentNo();
  }

  public String processOrder(Order order, PickingList pickingList, HashSet<String> notCompletedPL,
      VariablesSecureApp vars, String strPhysicalDocumentNo, String strMovementDate) {
    ConnectionProvider conProvider = new DalConnectionProvider();
    Connection conn = null;

    ShipmentInOut shipment;
    try {

      if (order.isObwplIsinpickinglist()) {

        OBCriteria<PickingList> picking = OBDal.getInstance().createCriteria(PickingList.class);
        picking.add(Restrictions.eq(PickingList.PROPERTY_SWACORDER, order));
        picking.add(Restrictions.ne(PickingList.PROPERTY_PICKLISTSTATUS, "CA"));
        picking.setMaxResults(1);
        PickingList pickinglist = (PickingList) picking.uniqueResult();
        if (pickinglist != null)
          // throw new
          // OBException(OBMessageUtils.messageBD("OBWPL_IsInPL") +
          // order.getDocumentNo()+" - "+pickinglist.getDocumentNo());
          throw new OBException(OBMessageUtils.messageBD("swa_IsInPL") + order.getDocumentNo()
              + " - " + pickinglist.getDocumentNo());

      }
      if (order.isObwplReadypl()) {
        throw new OBException(OBMessageUtils.messageBD("OBWPL_IsExcluded") + order.getDocumentNo());
      }
      order.setObwplIsinpickinglist(true);

      String description = "";
      // Create In Out
      shipment = OBProvider.getInstance().get(ShipmentInOut.class);
      shipment.setOrganization(order.getOrganization());
      shipment.setSalesTransaction(true);

      if (order.getDocumentType().getScoSpecialdoctype().trim().equals("SSASAMPLEORDER"))
        shipment.setMovementType("ssa_S-");
      else
        shipment.setMovementType("C-");

      String specialDoc = "SCOMMSHIPMENT";
      String strMethod = order.getDeliveryMethod();
      String SpecialDocFromOrder = order.getDocumentType().getScoSpecialdoctype().trim();

      if (strMethod.compareTo("SCR_C") == 0 || strMethod.compareTo("P") == 0
          || strMethod.compareTo("SCR_VSGR") == 0 || strMethod.compareTo("SCR_P_NIC") == 0
          || strMethod.compareTo("SCR_C_NIC") == 0 || strMethod.compareTo("SCR_VSGR_NIC") == 0)
        specialDoc = "SWAMMDISPATCH";

      // MODIFY BY VAFASTER. GENERATE SHIPMENT WITH DOC TYPE. IN ORG,
      // DOCSPECIALTYPE, WAREHOUSE
      // ADD LINES
      OrganizationStructureProvider osp = OBContext.getOBContext()
          .getOrganizationStructureProvider(order.getClient().getId());

      OBCriteria<DocumentType> shipDocType = OBDal.getInstance().createCriteria(DocumentType.class);
      shipDocType.add(Restrictions.eq(DocumentType.PROPERTY_SCOSPECIALDOCTYPE, specialDoc));
      shipDocType.add(Restrictions.in("organization.id",
          osp.getParentTree(order.getOrganization().getId(), true)));
      shipDocType.setMaxResults(1);
      DocumentType P_shipDocType = (DocumentType) shipDocType.uniqueResult();
      // END ADD LINES

      if ("".equals(P_shipDocType) || P_shipDocType == null) {
        throw new OBException(OBMessageUtils.messageBD("OBWPL_DocType_Shipment"));
      }

      shipment.setDocumentType(P_shipDocType);
      shipment.setDocumentNo(OBWPL_Utils.getDocumentNo(P_shipDocType, "M_InOut"));
      shipment.setWarehouse(order.getWarehouse());
      shipment.setBusinessPartner(order.getBusinessPartner());
      shipment.setPartnerAddress(order.getPartnerAddress());
      shipment.setDeliveryLocation(order.getDeliveryLocation());
      shipment.setDeliveryMethod(order.getDeliveryMethod());
      shipment.setDeliveryTerms(order.getDeliveryTerms());

      // Removing: FIXME-ENERO
      // shipment.setMovementDate(order.getOrderDate());
      // shipment.setAccountingDate(order.getOrderDate());
      shipment.setMovementDate(now);
      shipment.setAccountingDate(now);
      shipment.setSalesOrder(order);
      shipment.setUserContact(order.getUserContact());
      shipment.setOrderReference(order.getOrderReference());
      shipment.setFreightCostRule(order.getFreightCostRule());
      shipment.setFreightAmount(order.getFreightAmount());
      shipment.setShippingCompany(order.getShippingCompany());
      shipment.setPriority(order.getPriority());
      shipment.setProject(order.getProject());
      shipment.setActivity(order.getActivity());
      shipment.setSalesCampaign(order.getSalesCampaign());
      shipment.setStDimension(order.getStDimension());
      shipment.setNdDimension(order.getNdDimension());
      shipment.setTrxOrganization(order.getTrxOrganization());

      // description = "Picking List: " + pickingList.getDocumentNo();
      if (!"".equals(order.getDescription()) && order.getDescription() != null)
        description = description + " " + order.getDescription();
      // description = description + "\n\n" + order.getDescription();
      if (order.getDropShipPartner() != null)
        description = description + " " + order.getDropShipPartner().getName();
      if (order.getDropShipLocation() != null)
        description = description + ": " + order.getDropShipLocation().getName();
      shipment.setDescription(description);

      shipment.setDocumentStatus("DR");
      shipment.setDocumentAction("CO");
      shipment.setProcessNow(false);
      shipment.setSWAUpdateReason(order.getSsaComboItem());

      conn = conProvider.getTransactionConnection();

      if (strPhysicalDocumentNo != null && strMovementDate != null) {
        if (!"P".equals(order.getDeliveryMethod()) && !"SCR_C".equals(order.getDeliveryMethod())
            && !"SCR_VSGR".equals(order.getDeliveryMethod())
            && !"SCR_P_NIC".equals(order.getDeliveryMethod())
            && !"SCR_C_NIC".equals(order.getDeliveryMethod())
            && !"SCR_VSGR_NIC".equals(order.getDeliveryMethod())) {
          Date movementDate = pe.com.unifiedgo.report.common.Utility.ParseFecha(strMovementDate,
              "yyyy-MM-dd");
          shipment.setMovementDate(movementDate);
          shipment.setAccountingDate(movementDate);
          shipment.setSCRPhysicalDocumentNo(strPhysicalDocumentNo);
        }
      }

      OBDal.getInstance().save(shipment);

      lineNo = 10L;
      for (OrderLine orderLine : order.getOrderLineList()) {
        // Only consider pending to deliver lines of stocked item products.
        if (orderLine.getProduct() == null || !orderLine.getProduct().getProductType().equals("I")
            || (orderLine.getProduct().getProductType().equals("I")
                && !orderLine.getProduct().isStocked())
            || orderLine.getOrderedQuantity().signum() == 0
            || orderLine.getOrderedQuantity().compareTo(orderLine.getDeliveredQuantity()) <= 0
            || (orderLine.isObwplReadypl() != null && orderLine.isObwplReadypl())) {
          continue;
        }

        if (orderLine.getOrderedQuantity().signum() < 0) {
          throw new OBException(OBMessageUtils.messageBD("OBWPL_OrderedQtyMustBePositive"));
        }
        // Removing: FIXME-ENERO(descomentar)
        if (orderLine.getProduct().isStocked()) {

          processOrderLine(orderLine, shipment, pickingList, notCompletedPL);
        } else {
          // Create Shipment Line for non stocked products.
          BigDecimal qty = orderLine.getOrderedQuantity()
              .subtract(orderLine.getDeliveredQuantity());
          // Removing: FIXME-ENERO reemplazar por funcion
          // createShipmentLine(...)
          createShipmentLine(orderLine.getAttributeSetValue(), null, qty, orderLine, shipment,
              pickingList);
          // createShipmentLine_FIXME_ENERO_REPLACE(orderLine.getAttributeSetValue(),
          // null, qty,
          // orderLine, shipment,
          // pickingList);
          OBDal.getInstance().flush();
        }
      }

      OBDal.getInstance().refresh(shipment);
      if (shipment.getMaterialMgmtShipmentInOutLineList().size() == 0) {
        throw new OBException(OBMessageUtils.messageBD("NotEnoughAvailableStock"));
      }

      conProvider.releaseCommitConnection(conn);

    } catch (Exception e) {
      try {
        conProvider.releaseRollbackConnection(conn);
      } catch (SQLException ex) {
        throw new OBException(ex.getMessage());
      }
      throw new OBException(e.getMessage());
    }

    return shipment.getDocumentNo();
  }

  // add function to requirementReposition createInOut_reposition
  private String createInOut_reposition(SwaRequerimientoReposicion order, PickingList pickingList,
      HashSet<String> notCompletedPL, ShipmentInOut orderReference) {
    int aux = 0;
    Location bAddress_loc;

    // Estructura de La Organización
    OrganizationStructureProvider osp = OBContext.getOBContext()
        .getOrganizationStructureProvider(order.getClient().getId());

    // El tipo de Movimiento de Salida serà el mismo que se creo en la Lista
    // de Picking
    SCRComboItem comboItem_get = null;
    try {
      comboItem_get = pickingList.getSWAUpdateReason();
      if (comboItem_get == null)
        throw new OBException(OBMessageUtils.messageBD("swa_not_ComboItem_transito"));
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("swa_not_ComboItem_transito"));
    }

    // Determinando el DocType de salida, el Doctype a crear està
    // determinado por la Orden de
    // Transferencia
    DocumentType P_shipDocType = order.getDoctypeCreate();
    if ("".equals(P_shipDocType) || P_shipDocType == null) {
      throw new OBException(OBMessageUtils.messageBD("OBWPL_DocType_Shipment"));
    }

    ShipmentInOut shipment = OBProvider.getInstance().get(ShipmentInOut.class);
    shipment.setOrganization(order.getOrganization());
    shipment.setSalesTransaction(true);
    shipment.setMovementType("M-"); // Movement From -
    shipment.setDocumentType(P_shipDocType); // From
    // swa_requerimientoreposicion
    shipment.setDocumentNo(OBWPL_Utils.getDocumentNo(P_shipDocType, "M_InOut"));
    shipment.setWarehouse(order.getFromMWarehouse());
    shipment.setBusinessPartner(order.getBusinessPartner()); // BP
    shipment.setDescription("Picking List: " + pickingList.getDocumentNo());
    shipment.setPartnerAddress(order.getPartnerAddress()); // ADDRESS
    // BParnet_LOCATION
    // ID
    shipment.setDeliveryMethod("P"); // P Pickup List Reference: C_Order
    // DeliveryViaRule
    shipment.setDeliveryTerms("A"); // A Availability List Reference:
    // C_Order DeliveryRule
    shipment.setMovementDate(order.getMovementDate() == null ? now : order.getMovementDate());
    shipment.setAccountingDate(order.getMovementDate() == null ? now : order.getMovementDate());
    shipment.setSwaRequireposicion(order);
    shipment.setFreightCostRule("I"); // I Freight included List Reference:
    // C_Order FreightCostRule
    shipment.setPriority("5"); // 5 Medium List Reference: Priority scale
    // list
    shipment.setDocumentStatus("DR");
    shipment.setDocumentAction("CO");
    shipment.setSwaIsfromreposition(true);
    shipment.setProcessNow(false);
    shipment.setSWAUpdateReason(comboItem_get);
    shipment.setSwaAsociateInout(orderReference);

    OBDal.getInstance().save(shipment);
    lineNo = 10L;

    // Validando que la Orden de Transferencia tenga lineas para insertar
    if (order.getSwaRequerepoDetailList().size() == 0) {
      throw new OBException(OBMessageUtils.messageBD("SWA_notlinesforPicking"));
    }
    // Recorriendo las Lineas de la Orden de Transferencia para Crear
    // nuestro Documento de Salida
    for (SwaRequerimientoReposicionDetail orderLine : order.getSwaRequerepoDetailList()) {
      if (orderLine.getOrderedQuantity().compareTo(new BigDecimal(0)) <= 0) {
        throw new OBException(OBMessageUtils.messageBD("OBWPL_OrderedQtyMustBePositive"));
      }
      if (orderLine.getProduct().isStocked()) {
        // Removing: FIXME-ENERO
        processOrderLine2(order, orderLine, shipment, pickingList, notCompletedPL);
        // processOrderLine2_FIXME_ENERO_REPLACE(order, orderLine,
        // shipment, pickingList,
        // notCompletedPL);
      }
    }

    try {
      final List<Object> param = new ArrayList<Object>();
      param.add(null);
      param.add(shipment.getId());
      CallStoredProcedure.getInstance().call("M_Inout_POST", param, null, true, false);
    } catch (Exception e) {
      throw new OBException(e.getMessage());
      // TODO: handle exception
    }

    // Completar Documento de Salida

    return shipment.getDocumentNo();
  }

  // ShipmentInOut
  private String processOrder3(Order order, PickingList pickingList,
      HashSet<String> notCompletedPL) {

    String strigGetDocShip;

    int tmp1 = 0;
    int tmp2 = 0;
    int aux = 0;
    Location bAddress_loc;
    ShipmentInOut shipment = OBProvider.getInstance().get(ShipmentInOut.class);
    shipment.setOrganization(order.getOrganization());
    shipment.setSalesTransaction(true);
    shipment.setMovementType("M-"); // Movement From -

    order.setObwplIsinpickinglist(true);

    OrganizationStructureProvider osp = OBContext.getOBContext()
        .getOrganizationStructureProvider(order.getClient().getId());

    OBCriteria<SCRComboItem> comboItem = OBDal.getInstance().createCriteria(SCRComboItem.class);
    comboItem.add(Restrictions.eq(SCRComboItem.PROPERTY_CLIENT, pickingList.getClient()));
    comboItem.add(Restrictions.eq(SCRComboItem.PROPERTY_SEARCHKEY, "Salida por Servicio"));
    SCRComboItem comboItem_get = null;
    List<SCRComboItem> SCRComboItem_List = comboItem.list();
    if (SCRComboItem_List.size() < 1) {
      throw new OBException(OBMessageUtils.messageBD("sim_nofoundComboItem"));
    }
    comboItem_get = SCRComboItem_List.get(0);

    OBCriteria<DocumentType> shipDocType = OBDal.getInstance().createCriteria(DocumentType.class);
    shipDocType.add(Restrictions.eq(DocumentType.PROPERTY_SCOSPECIALDOCTYPE, "SCOMMSHIPMENT"));
    shipDocType.add(Restrictions.in("organization.id",
        osp.getParentTree(order.getOrganization().getId(), true)));

    shipDocType.setMaxResults(1);
    DocumentType P_shipDocType = (DocumentType) shipDocType.uniqueResult();

    // DocumentType P_shipDocType = order.getDoctypeCreate();

    if ("".equals(P_shipDocType) || P_shipDocType == null) {
      throw new OBException(OBMessageUtils.messageBD("OBWPL_DocType_Shipment"));
    }
    shipment.setDocumentType(P_shipDocType);
    shipment.setDocumentNo(OBWPL_Utils.getDocumentNo(P_shipDocType, "M_InOut"));

    shipment.setWarehouse(order.getWarehouse());
    shipment.setBusinessPartner(order.getBusinessPartner()); // BP

    /*
     * OBCriteria<Location> bAdress = OBDal.getInstance().createCriteria(Location.class);
     * bAdress.add(Restrictions.eq(Location.PROPERTY_BUSINESSPARTNER, order.getBusinessPartner()));
     * bAddress_loc = (Location) bAdress.uniqueResult(); // si es unico sino es otro get
     */

    shipment.setDescription("Picking List: " + pickingList.getDocumentNo());
    // shipment.setPartnerAddress(bAddress_loc); //ADDRESS BParnet_LOCATION
    // ID
    shipment.setPartnerAddress(order.getPartnerAddress()); // ADDRESS
    // BParnet_LOCATION
    // ID
    shipment.setDeliveryMethod("P"); // P Pickup List Reference: C_Order
    // DeliveryViaRule
    shipment.setDeliveryTerms("A"); // A Availability List Reference:
    // C_Order DeliveryRule
    shipment.setMovementDate(now);
    shipment.setAccountingDate(now);
    shipment.setSalesOrder(order); // VAFASTER verificar
    // shipment.setSwaRequireposicion(order);
    shipment.setFreightCostRule("I"); // I Freight included List Reference:
    // C_Order FreightCostRule
    shipment.setPriority("5"); // 5 Medium List Reference: Priority scale
    // list
    shipment.setDocumentStatus("DR");
    shipment.setDocumentAction("CO");
    shipment.setProcessNow(false);
    shipment.setSWAUpdateReason(comboItem_get);
    shipment.setSsaServiceorder(order);
    OBDal.getInstance().save(shipment);
    lineNo = 10L;

    // for (OrderLine orderLine : order.getOrderLineList() ) {
    for (SREServiceorderline orderLine : order.getSREServiceorderlineList()) {
      if (orderLine.getOrderedQuantity().signum() == 0) {
        throw new OBException(OBMessageUtils.messageBD("OBWPL_OrderedQtyMustBePositive"));
      }
      if (orderLine.getProduct().isStocked()) {
        processOrderLine3(order, orderLine, shipment, pickingList, notCompletedPL);
        OBDal.getInstance().flush();
      }

    }
    strigGetDocShip = shipment.getDocumentNo();
    return strigGetDocShip;
  }

  private String processOrder4(Order order, PickingList pickingList,
      HashSet<String> notCompletedPL) {

    // Combo para Tipo de Movimiento
    OrganizationStructureProvider osp = OBContext.getOBContext()
        .getOrganizationStructureProvider(order.getClient().getId());

    OBCriteria<SCRComboItem> comboItem = OBDal.getInstance().createCriteria(SCRComboItem.class);
    comboItem.add(Restrictions.eq(SCRComboItem.PROPERTY_CLIENT, pickingList.getClient()));
    comboItem.add(Restrictions.eq(SCRComboItem.PROPERTY_SEARCHKEY, "DevolucionProveedor"));
    SCRComboItem comboItem_get = null;
    List<SCRComboItem> SCRComboItem_List = comboItem.list();
    if (SCRComboItem_List.size() < 1) {
      throw new OBException(OBMessageUtils.messageBD("sim_nofoundComboItem"));
    }
    comboItem_get = SCRComboItem_List.get(0);

    // /Get Doctype
    OBCriteria<DocumentType> shipDocType = OBDal.getInstance().createCriteria(DocumentType.class);
    shipDocType.add(Restrictions.eq(DocumentType.PROPERTY_SCOSPECIALDOCTYPE, "SWARTVRECEIPT"));
    shipDocType.add(Restrictions.in("organization.id",
        osp.getParentTree(order.getOrganization().getId(), true)));
    shipDocType.setMaxResults(1);
    DocumentType P_shipDocType = (DocumentType) shipDocType.uniqueResult();

    if ("".equals(P_shipDocType) || P_shipDocType == null) {
      throw new OBException(OBMessageUtils.messageBD("OBWPL_DocType_Shipment"));
    }
    // ///////

    String strigGetDocShip;
    int tmp1 = 0;
    int tmp2 = 0;
    int aux = 0;
    Location bAddress_loc;
    ShipmentInOut shipment = OBProvider.getInstance().get(ShipmentInOut.class);
    shipment.setOrganization(order.getOrganization());
    shipment.setSalesTransaction(false);
    shipment.setMovementType("V+");
    order.setObwplIsinpickinglist(true);
    shipment.setDocumentType(P_shipDocType);
    shipment.setDocumentNo(OBWPL_Utils.getDocumentNo(P_shipDocType, "M_InOut"));
    shipment.setWarehouse(order.getWarehouse());
    shipment.setBusinessPartner(order.getBusinessPartner()); // BP
    shipment.setDescription("Picking List: " + pickingList.getDocumentNo());
    shipment.setPartnerAddress(order.getPartnerAddress()); // ADDRESS
    // BParnet_LOCATION
    // ID
    shipment.setDeliveryMethod("P"); // P Pickup List Reference: C_Order
    // DeliveryViaRule
    shipment.setDeliveryTerms("A"); // A Availability List Reference:
    // C_Order DeliveryRule
    shipment.setMovementDate(now);
    shipment.setAccountingDate(now);
    // shipment.setSalesOrder(order); //VAFASTER verificar

    shipment.setFreightCostRule("I"); // I Freight included List Reference:
    // C_Order FreightCostRule
    shipment.setPriority("5"); // 5 Medium List Reference: Priority scale
    // list
    shipment.setDocumentStatus("DR");
    shipment.setDocumentAction("CO");
    shipment.setProcessNow(false);
    shipment.setSWAUpdateReason(comboItem_get);
    // shipment.set SsaServiceorder(order);
    OBDal.getInstance().save(shipment);
    lineNo = 10L;

    for (OrderLine orderLine : order.getOrderLineList()) {
      if (orderLine.getOrderedQuantity().signum() == 0) {
        throw new OBException(OBMessageUtils.messageBD("OBWPL_OrderedQtyMustBePositive"));
      }
      if (orderLine.getProduct().isStocked()) {
        processOrderLine4(order, orderLine, shipment, pickingList, notCompletedPL);
        OBDal.getInstance().flush();
      }
    }
    strigGetDocShip = shipment.getDocumentNo();
    return strigGetDocShip;
  }

  // ShipmentInOut
  private ShipmentInOut createInOut_reposition_Referenceinout(SwaRequerimientoReposicion order,
      PickingList pickingList, HashSet<String> notCompletedPL) {
    int aux = 0;
    boolean aux_validateSample = false;
    boolean aux_validateService = false;
    boolean aux_validateTransferDirect = false;
    Location bAddress_loc;

    // Estructura de la Organizaciòn
    OrganizationStructureProvider osp = OBContext.getOBContext()
        .getOrganizationStructureProvider(order.getClient().getId());

    // Hay que considerar que Si se trata de una Orden de Transferencia por
    // Solicitud de Muestra,
    // Tendrà sus propiar validaciones aux_validateSample = true
    // if(order.isFromrequest() &&
    // order.getIsfromrequestList().compareTo("SM")==0)

    if (order.getDocumentType().getScoSpecialdoctype().equals("SWAREQUESTSAMPE"))
      aux_validateSample = true;

    if (order.getDocumentType().getScoSpecialdoctype().equals("SWAREQUESTSERVICE"))
      aux_validateService = true;

    SCRComboItem comboItemReposition = null;
    if (order.getDocumentType().getScoSpecialdoctype().equals("SWAREPOSITIONDIRECT")) {
      aux_validateTransferDirect = true;
      comboItemReposition = order.getComboItem();
    }

    // Definiendo el tipo de Orden
    // El tipo de Orden definirà por que concepto se està haciendo el
    // Ingreso
    // Si se trata de una simple y común transferencia entonces se deberá
    // insertar por concepto de
    // IngresoProductosTransito
    // Si se trata de una Transferencia por Solicitud de Muestra entonces se //
    // deberá hacer el ingreso
    // por concepto de IngresoMuestras
    OBCriteria<SCRComboItem> comboItem = OBDal.getInstance().createCriteria(SCRComboItem.class);
    comboItem.add(Restrictions.eq(SCRComboItem.PROPERTY_CLIENT, pickingList.getClient()));
    if (aux_validateSample)
      comboItem.add(Restrictions.eq(SCRComboItem.PROPERTY_SEARCHKEY, "IngresoMuestras"));
    else if (aux_validateService)
      comboItem.add(Restrictions.eq(SCRComboItem.PROPERTY_SEARCHKEY, "ReposicionEntreAlmacenes"));
    else
      comboItem.add(Restrictions.eq(SCRComboItem.PROPERTY_SEARCHKEY, "IngresoProductosTransito"));
    SCRComboItem comboItem_get = null;
    try {
      comboItem_get = comboItemReposition;
      if (comboItem_get == null)
        comboItem_get = (SCRComboItem) comboItem.uniqueResult();// Transferencia Directa
      if (comboItem_get == null)
        throw new OBException(OBMessageUtils.messageBD("swa_not_ComboItem_transito"));
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("swa_not_ComboItem_transito"));
    }

    // Definiendo el almacén al cuál se hará el ingreso, teniendo en cuenta
    // la validacion
    // aux_validateSample

    Warehouse warehouse_transito = null;
    try {
      if (aux_validateSample || aux_validateService || aux_validateTransferDirect) { // si es una
                                                                                     // muestra, se
                                                                                     // harà el
                                                                                     // ingreso
        // al almacèn configurado como
        // destino en la Orden de Transferencia,
        // el cuàl deberà ser de
        // Muestra
        warehouse_transito = OBDal.getInstance().get(Warehouse.class,
            order.getMWarehouse().getId());
      } else {
        OBCriteria<Warehouse> warehouseTransito = OBDal.getInstance()
            .createCriteria(Warehouse.class);
        warehouseTransito.add(Restrictions.eq(Warehouse.PROPERTY_CLIENT, order.getClient()));
        warehouseTransito.add(Restrictions.eq(Warehouse.PROPERTY_SWAWAREHOUSETYPE, "TR"));
        warehouseTransito.add(Restrictions.in("organization.id",
            osp.getParentTree(order.getOrganization().getId(), true)));
        warehouseTransito.setMaxResults(1);
        warehouse_transito = (Warehouse) warehouseTransito.uniqueResult();
      }
      if (warehouse_transito == null)
        throw new OBException(OBMessageUtils.messageBD("swa_not_warehouse_transit"));
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("swa_not_warehouse_transit"));
    }

    // Definiendo el Tipo de Documento C_doctype y su Número Correspondiente
    OBCriteria<DocumentType> shipDocType = OBDal.getInstance().createCriteria(DocumentType.class);
    if (aux_validateTransferDirect == true)
      shipDocType.add(Restrictions.eq(DocumentType.PROPERTY_SCOSPECIALDOCTYPE, "SCOMMRECEIPT"));
    else
      shipDocType
          .add(Restrictions.eq(DocumentType.PROPERTY_SCOSPECIALDOCTYPE, "SWAINTERNALRECEIPT"));
    shipDocType.add(Restrictions.in("organization.id",
        osp.getParentTree(order.getOrganization().getId(), true)));
    shipDocType.setMaxResults(1);
    DocumentType P_shipDocType = (DocumentType) shipDocType.uniqueResult();
    if ("".equals(P_shipDocType) || P_shipDocType == null) {
      throw new OBException(OBMessageUtils.messageBD("swa_DocType_Shipment"));
    }

    // Creando Nota de Ingreso que corresponderà al
    ShipmentInOut shipment = OBProvider.getInstance().get(ShipmentInOut.class);
    shipment.setOrganization(order.getOrganization());
    shipment.setSalesTransaction(false); // Necesito que en este Shipment
    // los productos ingresen
    shipment.setMovementType("V+"); // Movement From +
    shipment.setDocumentType(P_shipDocType);
    shipment.setDocumentNo(OBWPL_Utils.getDocumentNo(P_shipDocType, "M_InOut"));
    shipment.setWarehouse(warehouse_transito);
    shipment.setBusinessPartner(order.getBusinessPartner()); // BP Como
    // tercero
    // //Se esta
    // poniendo
    // el mismo
    // que la
    // salida -
    // Revisar
    shipment.setPartnerAddress(order.getPartnerAddress());
    shipment.setDeliveryMethod("P"); // P Pickup List Reference: C_Order
    // DeliveryViaRule
    shipment.setDeliveryTerms("A"); // A Availability List Reference:
    // C_Order DeliveryRule
    shipment.setMovementDate(order.getMovementDate() == null ? now : order.getMovementDate());
    shipment.setAccountingDate(order.getMovementDate() == null ? now : order.getMovementDate());
    shipment.setSwaRequireposicion(order); // Referencia de la Orden de
    // Transferencia
    shipment.setFreightCostRule("I"); // I Freight included List Reference:
    // C_Order FreightCostRule
    shipment.setPriority("5"); // 5 Medium List Reference: Priority scale
    // list
    shipment.setDocumentStatus("DR");
    shipment.setDocumentAction("CO");
    shipment.setSwaIsfromreposition(true);
    shipment.setProcessNow(false);
    shipment.setSWAUpdateReason(comboItem_get);
    OBDal.getInstance().save(shipment);
    lineNo = 10L;

    // Validando que el Almacèn para Hacer el Ingreso tenga por lo menos una
    // Ubicación
    OBCriteria<Locator> OBJlocator = OBDal.getInstance().createCriteria(Locator.class);
    OBJlocator.add(Restrictions.eq(Locator.PROPERTY_WAREHOUSE, shipment.getWarehouse()));
    List<Locator> locators = OBJlocator.list();
    if (locators.size() < 1)
      throw new OBException(OBMessageUtils.messageBD("swa_noLocatorWarehouseTransit"));

    // Validando que la Orden de Transferencia tenga lineas para insertar
    if (order.getSwaRequerepoDetailList().size() == 0) {
      throw new OBException(OBMessageUtils.messageBD("SWA_notlinesforPicking"));
    }

    // Recorriendo Las lineas para insertarlas
    for (SwaRequerimientoReposicionDetail orderLine : order.getSwaRequerepoDetailList()) {
      // las Lineas de Transferencias deben tener cantidades positivas
      if (orderLine.getOrderedQuantity().compareTo(new BigDecimal(0)) <= 0) {
        throw new OBException(OBMessageUtils.messageBD("OBWPL_OrderedQtyMustBePositive"));
      }
      processOrderLine2_inoutReference(locators.get(0), order, orderLine, shipment, pickingList,
          notCompletedPL);
    }
    return shipment;
  }

  // ADD FUNCIONT
  // processOrderLine3-------------------------------------------------------------
  private void processOrderLine3(Order order, SREServiceorderline orderLine, ShipmentInOut shipment,
      PickingList pickingList, HashSet<String> notCompletedPL) {

    // ///Revisando -- Stock Reservation
    Reservation res = ReservationUtils.getReservationFromServiceOrder(orderLine);
    if (res.getRESStatus().equals("DR")) {
      ReservationUtils.processReserve(res, "PR");
    }
    // refresh
    res = OBDal.getInstance().get(Reservation.class, res.getId());
    OBDal.getInstance().refresh(res);

    List<ReservationStock> listResStock = res.getMaterialMgmtReservationStockList();

    boolean verifyIfCheckStock = false;

    if (res.getReservedQty() == res.getQuantity()) {
      verifyIfCheckStock = true;
    }

    for (ReservationStock resStock : listResStock) {
      OBDal.getInstance().remove(resStock);
    }

    listResStock.clear();
    OBDal.getInstance().flush();

    // refresh
    res = OBDal.getInstance().get(Reservation.class, res.getId());
    OBDal.getInstance().refresh(res);
    OBDal.getInstance().flush();

    // Controles para el picking
    BigDecimal qtyPendienteInsertar = orderLine.getOrderedQuantity();
    BigDecimal qtyLine = new BigDecimal(0);

    // Vista de Stock Disponible en el Almacén
    OBCriteria<swaProductWarehouse> productWarehouse = OBDal.getInstance()
        .createCriteria(swaProductWarehouse.class);
    productWarehouse
        .add(Restrictions.eq(swaProductWarehouse.PROPERTY_WAREHOUSE, order.getWarehouse()));
    productWarehouse
        .add(Restrictions.eq(swaProductWarehouse.PROPERTY_PRODUCT, orderLine.getProduct()));
    productWarehouse.add(Restrictions.eq(swaProductWarehouse.PROPERTY_CLIENT, order.getClient()));
    List<swaProductWarehouse> swaProductWarehouseList = productWarehouse.list();

    if (swaProductWarehouseList.size() == 0) {// La vista no me retorna
      // Almacenes Disponibles
      throw new OBException(OBMessageUtils.messageBD("NotEnoughAvailableStock") + " - "
          + orderLine.getProduct().getSearchKey() + " - " + orderLine.getProduct().getName());
    }

    swaProductWarehouse prwr = swaProductWarehouseList.get(0);// tomo el
    // Primer
    // almacen,
    // esto no
    // debe
    // pasar por
    // que ya
    // filtro
    // por
    // almacèn
    // solo que
    // por
    // siaaaacaso
    OBDal.getInstance().refresh(prwr);
    if (!verifyIfCheckStock) { // Si hay algo que no estaba en Reserva,
      // Entonces ver si hay Stock
      if (prwr.getTotalqty().compareTo(qtyPendienteInsertar) == -1) {
        throw new OBException(OBMessageUtils.messageBD("NotEnoughAvailableStock") + " - "
            + orderLine.getProduct().getSearchKey() + " - " + orderLine.getProduct().getName());
      }
    }

    // Revisando La Vista de Stock por Ubicaciones para proceder con el
    // Picking
    OBCriteria<SWAProductByAnaquelV> productbyAnaquel = OBDal.getInstance()
        .createCriteria(SWAProductByAnaquelV.class);
    productbyAnaquel
        .add(Restrictions.eq(SWAProductByAnaquelV.PROPERTY_WAREHOUSE, order.getWarehouse()));
    productbyAnaquel
        .add(Restrictions.eq(SWAProductByAnaquelV.PROPERTY_PRODUCT, orderLine.getProduct()));
    productbyAnaquel.add(Restrictions.eq(SWAProductByAnaquelV.PROPERTY_CLIENT, order.getClient()));
    productbyAnaquel.add(Restrictions.sqlRestriction(" qtyonhand > 0 "));
    // productbyAnaquel.add(Restrictions.ne(SWAProductByAnaquelV.PROPERTY_QUANTITYONHAND,
    // BigDecimal.ZERO)); // Verificando que se insertar cantidades
    // diferentes a Cero //
    productbyAnaquel.add(Restrictions.sqlRestriction(" coalesce(locatortype,'-') <> 'swa_RCP' ")); // No
    // se
    // toman
    // en
    // cuenta
    // las
    // ubicaciones
    // de
    // Recepcion
    // para
    // Picking
    productbyAnaquel.addOrderBy(SWAProductByAnaquelV.PROPERTY_RELATIVEPRIORITY, true);
    List<SWAProductByAnaquelV> productbyAnaquelList = productbyAnaquel.list();
    // Recorriendo las ubicaciones disponibles para el Picking

    for (int j = 0; j < productbyAnaquelList.size(); j++) {

      if (productbyAnaquelList.get(j).getQuantityOnHand().compareTo(qtyPendienteInsertar) > 0) {
        qtyLine = qtyPendienteInsertar;
      } else {
        qtyLine = productbyAnaquelList.get(j).getQuantityOnHand();
      }

      createShipmentLine3(productbyAnaquelList.get(j).getStorageBin(), qtyLine, orderLine, shipment,
          pickingList);
      createStockReservationForServices(res, productbyAnaquelList.get(j).getStorageBin(), qtyLine,
          orderLine, shipment, pickingList);

      // Actualizando cantidades
      qtyPendienteInsertar = qtyPendienteInsertar.subtract(qtyLine);
      if (qtyPendienteInsertar.compareTo(BigDecimal.ZERO) == 0) // Si ya
        // no
        // hay
        // pendiente
        // por
        // insertar
        // entonces
        // salir
        // del
        // bucle
        break;
    }

    // si falta pickar productos y ya no hay ubicaciones.
    if (qtyPendienteInsertar.compareTo(BigDecimal.ZERO) > 0) {
      throw new OBException(
          OBMessageUtils.messageBD("swa_NotEnoughLocatorAvailaleforPicking") + " - "
              + orderLine.getProduct().getSearchKey() + " - " + orderLine.getProduct().getName());
    }

  }

  // ADD FUNCIONT
  // processOrderLine4-------------------------------------------------------------
  private void processOrderLine4(Order order, OrderLine orderLine, ShipmentInOut shipment,
      PickingList pickingList, HashSet<String> notCompletedPL) {

    // ///Revisando -- Stock Reservation
    /*
     * Reservation res = ReservationUtils.getReservationFromServiceOrder(orderLine); if
     * (res.getRESStatus().equals("DR")) { ReservationUtils.processReserve(res, "PR"); } // refresh
     * res = OBDal.getInstance().get(Reservation.class, res.getId());
     * OBDal.getInstance().refresh(res);
     * 
     * List<ReservationStock> listResStock = res.getMaterialMgmtReservationStockList();
     * 
     * boolean verifyIfCheckStock = false;
     * 
     * if(res.getReservedQty() == res.getQuantity()){ verifyIfCheckStock = true; }
     * 
     * for(ReservationStock resStock : listResStock){ OBDal.getInstance().remove(resStock); }
     * 
     * listResStock.clear(); OBDal.getInstance().flush();
     * 
     * 
     * 
     * // refresh res = OBDal.getInstance().get(Reservation.class, res.getId());
     * OBDal.getInstance().refresh(res); OBDal.getInstance().flush();
     */

    // Controles para el picking
    BigDecimal qtyPendienteInsertar = orderLine.getOrderedQuantity();
    BigDecimal qtyLine = new BigDecimal(0);

    // Vista de Stock Disponible en el Almacén
    OBCriteria<swaProductWarehouse> productWarehouse = OBDal.getInstance()
        .createCriteria(swaProductWarehouse.class);
    productWarehouse
        .add(Restrictions.eq(swaProductWarehouse.PROPERTY_WAREHOUSE, order.getWarehouse()));
    productWarehouse
        .add(Restrictions.eq(swaProductWarehouse.PROPERTY_PRODUCT, orderLine.getProduct()));
    productWarehouse.add(Restrictions.eq(swaProductWarehouse.PROPERTY_CLIENT, order.getClient()));
    List<swaProductWarehouse> swaProductWarehouseList = productWarehouse.list();

    if (swaProductWarehouseList.size() == 0) {// La vista no me retorna
      // Almacenes Disponibles
      throw new OBException(OBMessageUtils.messageBD("NotEnoughAvailableStock") + " - "
          + orderLine.getProduct().getSearchKey() + " - " + orderLine.getProduct().getName());
    }

    swaProductWarehouse prwr = swaProductWarehouseList.get(0);// tomo el
    // Primer
    // almacen,
    // esto no
    // debe
    // pasar por
    // que ya
    // filtro
    // por
    // almacèn
    // solo que
    // por
    // siaaaacaso
    OBDal.getInstance().refresh(prwr);

    /*
     * if(!verifyIfCheckStock){ //Si hay algo que no estaba en Reserva, Entonces ver si hay Stock
     * if(prwr.getTotalqty().compareTo(qtyPendienteInsertar)== -1){ throw new
     * OBException(OBMessageUtils.messageBD("NotEnoughAvailableStock") + " - " +
     * orderLine.getProduct().getSearchKey() + " - " + orderLine.getProduct().getName()); } }
     */

    // Revisando La Vista de Stock por Ubicaciones para proceder con el
    // Picking
    OBCriteria<SWAProductByAnaquelV> productbyAnaquel = OBDal.getInstance()
        .createCriteria(SWAProductByAnaquelV.class);
    productbyAnaquel
        .add(Restrictions.eq(SWAProductByAnaquelV.PROPERTY_WAREHOUSE, order.getWarehouse()));
    productbyAnaquel
        .add(Restrictions.eq(SWAProductByAnaquelV.PROPERTY_PRODUCT, orderLine.getProduct()));
    productbyAnaquel.add(Restrictions.eq(SWAProductByAnaquelV.PROPERTY_CLIENT, order.getClient()));
    productbyAnaquel.add(Restrictions.sqlRestriction(" qtyonhand > 0 "));

    // productbyAnaquel.add(Restrictions.ne(SWAProductByAnaquelV.PROPERTY_QUANTITYONHAND,
    // BigDecimal.ZERO)); // Verificando que se insertar cantidades
    // diferentes a Cero //
    productbyAnaquel.add(Restrictions.sqlRestriction(" coalesce(locatortype,'-') <> 'swa_RCP' ")); // No
    // se
    // toman
    // en
    // cuenta
    // las
    // ubicaciones
    // de
    // Recepcion
    // para
    // Picking
    productbyAnaquel.addOrderBy(SWAProductByAnaquelV.PROPERTY_RELATIVEPRIORITY, true);
    List<SWAProductByAnaquelV> productbyAnaquelList = productbyAnaquel.list();
    // Recorriendo las ubicaciones disponibles para el Picking

    if (qtyPendienteInsertar.compareTo(BigDecimal.ZERO) < 0) {
      qtyPendienteInsertar = qtyPendienteInsertar.multiply(new BigDecimal(-1));
    }

    for (int j = 0; j < productbyAnaquelList.size(); j++) {

      if (productbyAnaquelList.get(j).getQuantityOnHand().compareTo(qtyPendienteInsertar) > 0) {
        qtyLine = qtyPendienteInsertar;
      } else {
        qtyLine = productbyAnaquelList.get(j).getQuantityOnHand();
      }

      BigDecimal qtyLineaNegativa = BigDecimal.ZERO;

      // Esto es por que siempre se debe pasar a la guia con nactidades
      // negativas
      qtyLineaNegativa = qtyLine;
      if (qtyLineaNegativa.compareTo(BigDecimal.ZERO) > 0) // Si es
        // positivo
        // lo
        // ponemos a
        // negativo
        qtyLineaNegativa = qtyLineaNegativa.multiply(new BigDecimal(-1));

      createShipmentLine4(productbyAnaquelList.get(j).getStorageBin(), qtyLineaNegativa, orderLine,
          shipment, pickingList);
      // createStockReservationForServices(res,
      // productbyAnaquelList.get(j).getStorageBin(),
      // qtyLine, orderLine, shipment, pickingList);
      // Actualizando cantidades
      qtyPendienteInsertar = qtyPendienteInsertar.subtract(qtyLine);
      if (qtyPendienteInsertar.compareTo(BigDecimal.ZERO) == 0) // Si ya
        // no
        // hay
        // pendiente
        // por
        // insertar
        // entonces
        // salir
        // del
        // bucle
        break;
    }

    // si falta pickar productos y ya no hay ubicaciones.
    if (qtyPendienteInsertar.compareTo(BigDecimal.ZERO) > 0) {
      throw new OBException(
          OBMessageUtils.messageBD("swa_NotEnoughLocatorAvailaleforPicking") + " - "
              + orderLine.getProduct().getSearchKey() + " - " + orderLine.getProduct().getName());
    }

  }

  // Removing: FIXME-ENERO --funcion para ingreso info, para enero utilizar
  // processOrderLine2
  // private void
  // processOrderLine2_FIXME_ENERO_REPLACE(SwaRequerimientoReposicion order,
  // SwaRequerimientoReposicionDetail orderLine, ShipmentInOut shipment,
  // PickingList pickingList,
  // HashSet<String> notCompletedPL) {
  //
  // // Controles para el picking
  // BigDecimal qtyPendienteInsertar = orderLine.getOrderedQuantity();
  // BigDecimal qtyLine = BigDecimal.ZERO;
  //
  // // Vista de Stock Disponible en el Almacén
  // OBCriteria<Locator> locators =
  // OBDal.getInstance().createCriteria(Locator.class);
  // locators.add(Restrictions.eq(Locator.PROPERTY_WAREHOUSE,
  // order.getFromMWarehouse()));
  // locators.add(Restrictions.eq(Locator.PROPERTY_SEARCHKEY, "*00"));
  // locators.add(Restrictions.eq(swaProductWarehouse.PROPERTY_CLIENT,
  // order.getClient()));
  // List<Locator> locatorList = locators.list();
  //
  // if (locatorList.size() == 0) {// NO HAY UBICACION *00
  // throw new OBException(" NO HAY UBICACION *00 - ");
  // }
  //
  // Locator locator00 = locatorList.get(0);// se supone que el primero es *00
  //
  // qtyLine = qtyPendienteInsertar;
  //
  // // Crear la Linea de el Documento de Salida
  // createShipmentLineToReposition(locator00, qtyLine, orderLine, shipment,
  // pickingList);
  //
  // }

  // ADD FUNCIONT
  // processOrderLine2-------------------------------------------------------------
  private void processOrderLine2(SwaRequerimientoReposicion order,
      SwaRequerimientoReposicionDetail orderLine, ShipmentInOut shipment, PickingList pickingList,
      HashSet<String> notCompletedPL) {

    // ///Revisando -- Stock Reservation
    Reservation res = ReservationUtils.getReservationFromReposition(orderLine);
    boolean verifyIfCheckStock = false;
    if (res != null) // Hubo Reserva Al momento de hacer la orden de
    // Transferencia
    {
      if (res.getRESStatus().equals("DR")) {
        ReservationUtils.processReserve(res, "PR");
      }

      // refresh Obligatorio para que se actulice la Reserva
      res = OBDal.getInstance().get(Reservation.class, res.getId());
      OBDal.getInstance().refresh(res);

      List<ReservationStock> listResStock = res.getMaterialMgmtReservationStockList();

      if (res.getReservedQty() == res.getQuantity()) {
        verifyIfCheckStock = true;
      }

      // Se remueven las reservar para agregarlas
      // CreateActionHandler.java:1005luego pero ya con
      // ubicaciones
      for (ReservationStock resStock : listResStock) {
        OBDal.getInstance().remove(resStock);
      }

      listResStock.clear();
      OBDal.getInstance().flush();

      // refresh
      res = OBDal.getInstance().get(Reservation.class, res.getId());
      OBDal.getInstance().refresh(res);
      OBDal.getInstance().flush();
    }

    // Controles para el picking
    BigDecimal qtyPendienteInsertar = orderLine.getOrderedQuantity();
    BigDecimal qtyLine = BigDecimal.ZERO;

    // Vista de Stock Disponible en el Almacén
    OBCriteria<swaProductWarehouse> productWarehouse = OBDal.getInstance()
        .createCriteria(swaProductWarehouse.class);
    productWarehouse
        .add(Restrictions.eq(swaProductWarehouse.PROPERTY_WAREHOUSE, order.getFromMWarehouse()));
    productWarehouse
        .add(Restrictions.eq(swaProductWarehouse.PROPERTY_PRODUCT, orderLine.getProduct()));
    productWarehouse.add(Restrictions.eq(swaProductWarehouse.PROPERTY_CLIENT, order.getClient()));
    List<swaProductWarehouse> swaProductWarehouseList = productWarehouse.list();

    if (swaProductWarehouseList.size() == 0) {// La vista no me retorna
      // Almacenes Disponibles
      throw new OBException(OBMessageUtils.messageBD("NotEnoughAvailableStock") + " - "
          + orderLine.getProduct().getSearchKey() + " - " + orderLine.getProduct().getName());
    }

    swaProductWarehouse prwr = swaProductWarehouseList.get(0);// tomo el
    // Primer
    // almacen,
    // esto no
    // debe
    // pasar por
    // que ya
    // filtro
    // por
    // almacèn
    // solo que
    // por
    // siaaaacaso
    OBDal.getInstance().refresh(prwr);
    if (!verifyIfCheckStock) {// Si hay algo que no estaba en Reserva,
      // Entonces ver si hay Stock
      if (prwr.getTotalqty().compareTo(qtyPendienteInsertar) == -1) {
        throw new OBException(OBMessageUtils.messageBD("NotEnoughAvailableStock") + " - "
            + orderLine.getProduct().getSearchKey() + " - " + orderLine.getProduct().getName());
      }
    }

    // Revisando La Vista de Stock por Ubicaciones para proceder con el
    // Picking
    OBCriteria<SWAProductByAnaquelV> productbyAnaquel = OBDal.getInstance()
        .createCriteria(SWAProductByAnaquelV.class);
    productbyAnaquel
        .add(Restrictions.eq(SWAProductByAnaquelV.PROPERTY_WAREHOUSE, order.getFromMWarehouse()));
    productbyAnaquel
        .add(Restrictions.eq(SWAProductByAnaquelV.PROPERTY_PRODUCT, orderLine.getProduct()));
    productbyAnaquel.add(Restrictions.eq(SWAProductByAnaquelV.PROPERTY_CLIENT, order.getClient()));
    productbyAnaquel.add(Restrictions.sqlRestriction(" qtyonhand > 0 "));
    productbyAnaquel.add(Restrictions.sqlRestriction(" coalesce(locatortype,'-') <> 'swa_RCP' ")); // No
    // se
    // toman
    // en
    // cuenta
    // las
    // ubicaciones
    // de
    // Recepcion
    // para
    // Picking
    // productbyAnaquel.add(Restrictions.ne(SWAProductByAnaquelV.PROPERTY_LOCATORTYPE!=null?
    // SWAProductByAnaquelV.PROPERTY_LOCATORTYPE:"-", "swa_RCP"));//No se
    // toman en cuenta las
    // ubicaciones de Recepcion para Picking
    productbyAnaquel.addOrderBy(SWAProductByAnaquelV.PROPERTY_RELATIVEPRIORITY, true);

    List<SWAProductByAnaquelV> productbyAnaquelList = productbyAnaquel.list();

    // Recorriendo las ubicaciones disponibles para el Picking
    for (int j = 0; j < productbyAnaquelList.size(); j++) {

      // si la ubicaciones tiene stock suficiente para insertar lo que
      // tengo pendiente
      // qtyPendienteInsertar
      if (productbyAnaquelList.get(j).getQuantityOnHand().compareTo(qtyPendienteInsertar) > 0) {
        qtyLine = qtyPendienteInsertar;
      } else { // Solo vamos a insertar lo que hay disponible en la
        // ubicaciones
        qtyLine = productbyAnaquelList.get(j).getQuantityOnHand();
      }

      // Crear la Linea de el Documento de Salida
      createShipmentLineToReposition(productbyAnaquelList.get(j).getStorageBin(), qtyLine,
          orderLine, shipment, pickingList);

      // Crear m_reserve Stock
      createStockReservationForReposition(res, orderLine, qtyLine,
          productbyAnaquelList.get(j).getStorageBin());
      // Actualizando cantidades
      qtyPendienteInsertar = qtyPendienteInsertar.subtract(qtyLine);
      if (qtyPendienteInsertar.compareTo(BigDecimal.ZERO) == 0) // Si ya
        // no
        // hay
        // pendiente
        // por
        // insertar
        // entonces
        // salir
        // del
        // bucle
        break;
    }

    // si falta pickar productos y ya no hay ubicaciones.
    if (qtyPendienteInsertar.compareTo(BigDecimal.ZERO) > 0) {
      throw new OBException(
          OBMessageUtils.messageBD("swa_NotEnoughLocatorAvailaleforPicking") + " - "
              + orderLine.getProduct().getSearchKey() + " - " + orderLine.getProduct().getName());
    }

  }

  private void createStockReservationForServices(Reservation res, Locator locators, BigDecimal qty,
      SREServiceorderline orderLine, ShipmentInOut shipment, PickingList pickingList) {

    ReservationStock mReservationStock = OBProvider.getInstance().get(ReservationStock.class);
    mReservationStock.setClient(res.getClient());
    mReservationStock.setOrganization(shipment.getOrganization());
    mReservationStock.setReservation(res);
    mReservationStock.setQuantity(qty);
    mReservationStock.setStorageBin(locators);

    OBDal.getInstance().save(mReservationStock);
    OBDal.getInstance().flush();
    OBDal.getInstance().refresh(mReservationStock);

    /*
     * final List<Object> param = new ArrayList<Object>();
     * 
     * param.add(null); param.add(mReservation.getId()); param.add("PR");
     * param.add(mReservation.getCreatedBy().getId());
     * CallStoredProcedure.getInstance().call("M_RESERVATION_POST", param, null, true, false);
     */

  }

  private void createStockReservationForReposition(Reservation res,
      SwaRequerimientoReposicionDetail repoLines, BigDecimal qtyLine, Locator sb) {
    ReservationStock resStock = OBProvider.getInstance().get(ReservationStock.class);
    resStock.setClient(res.getClient());
    resStock.setOrganization(res.getOrganization());
    resStock.setReservation(res);
    resStock.setQuantity(qtyLine);
    resStock.setStorageBin(sb);
    resStock.setSwaRequerepoDetail(repoLines);
    OBDal.getInstance().save(resStock);
    OBDal.getInstance().flush();
  }

  // ADD FUNCTION processOrderLine2_inoutReference
  private void processOrderLine2_inoutReference(Locator locators, SwaRequerimientoReposicion order,
      SwaRequerimientoReposicionDetail orderLine, ShipmentInOut shipment, PickingList pickingList,
      HashSet<String> notCompletedPL) {
    createShipmentLine2_inoutreference(locators, orderLine.getOrderedQuantity(), orderLine,
        shipment, pickingList);
  }

  // END FUNCTION processOrderLine2_inoutReference---------------------

  private void processOrderLine(OrderLine orderLine, ShipmentInOut shipment,
      PickingList pickingList, HashSet<String> notCompletedPL) {
    // Reserve Order Line
    boolean existsReservation = !orderLine.getMaterialMgmtReservationList().isEmpty();
    Reservation res = ReservationUtils.getReservationFromOrder(orderLine);
    if (res.getRESStatus().equals("DR")) {
      ReservationUtils.processReserve(res, "PR");
    } else if (res.getQuantity().compareTo(res.getReservedQty()) != 0) {
      ReservationUtils.reserveStockAuto(res);
    }
    // refresh
    res = OBDal.getInstance().get(Reservation.class, res.getId());
    OBDal.getInstance().refresh(res);

    List<ReservationStock> listResStock = new ArrayList<ReservationStock>();
    for (ReservationStock resStock : res.getMaterialMgmtReservationStockList()) {
      if (!resStock.isAllocated()) {
        if (resStock.getStorageBin() != null) {
          OBCriteria<StorageDetail> critSD = OBDal.getInstance()
              .createCriteria(StorageDetail.class);
          critSD.add(Restrictions.eq(StorageDetail.PROPERTY_UOM, res.getUOM()));
          critSD.add(Restrictions.eq(StorageDetail.PROPERTY_PRODUCT, res.getProduct()));
          critSD.add(Restrictions.eq(StorageDetail.PROPERTY_STORAGEBIN, resStock.getStorageBin()));
          critSD.add(Restrictions.eq(StorageDetail.PROPERTY_ATTRIBUTESETVALUE,
              resStock.getAttributeSetValue()));
          critSD.add(Restrictions.isNull(StorageDetail.PROPERTY_ORDERUOM));
          critSD.setMaxResults(1);

          StorageDetail sd = (StorageDetail) critSD.uniqueResult();
          listResStock.add(
              ReservationUtils.reserveStockManual(res, sd, resStock.getQuantity().negate(), "N"));
          ReservationUtils.reserveStockManual(res, sd, resStock.getQuantity(), "Y");
        } else {
          listResStock.add(ReservationUtils.reserveStockManual(res, resStock.getSalesOrderLine(),
              resStock.getQuantity().negate(), "N"));
          /*
           * ReservationUtils.reserveStockManual(res, resStock.getSalesOrderLine(),
           * resStock.getQuantity(), "Y");
           */
          // System.out.println("resStock.getQuantity(): " +
          // resStock.getQuantity());
          ReservationUtils.reserveStockAutoMaxPermit(res, resStock.getQuantity().toString());
        }
      }
    }

    // refresh
    res = OBDal.getInstance().get(Reservation.class, res.getId());
    OBDal.getInstance().refresh(res);
    // OBDal.getInstance().refresh(orderLine);
    OBDal.getInstance().flush();

    /*
     * for (ReservationStock resStock : orderLine.getMaterialMgmtReservationStockList()) {
     * System.out.println( "**FF: "+resStock.getId()+" "+resStock.getQuantity()); }
     */

    if (!listResStock.isEmpty()) {
      for (ReservationStock resStock : listResStock) {
        if (resStock.getQuantity().equals(BigDecimal.ZERO) && !resStock.isAllocated()) {

          res.getMaterialMgmtReservationStockList().remove(resStock);
          // Agregado por PALK porque sino sale error
          orderLine.getMaterialMgmtReservationStockList().clear();

          OBDal.getInstance().remove(resStock);

        }
      }
    }

    OBDal.getInstance().flush();

    if (!existsReservation) {
      res.setOBWPLGeneratedByPickingList(true);
      OBDal.getInstance().save(res);
    }
    OBDal.getInstance().flush();

    if (res.getQuantity().compareTo(res.getReservedQty()) != 0) {
      notCompletedPL.add(pickingList.getDocumentNo());

      throw new OBException(OBMessageUtils.messageBD("NotEnoughAvailableStock") + " "
          + res.getProduct().getSearchKey() + " - " + res.getProduct().getName());
    }

    for (ReservationStock resStock : res.getMaterialMgmtReservationStockList()) {

      if (resStock.getStorageBin() == null) {
        // If pre-reserve is not yet reserve
        continue;

      }
      BigDecimal releasedQty = resStock.getReleased() == null ? BigDecimal.ZERO
          : resStock.getReleased();

      if (resStock.getQuantity().compareTo(releasedQty) <= 0) {
        // Ignore released stock
        continue;
      }

      BigDecimal quantity = resStock.getQuantity().subtract(releasedQty);
      // Create InOut line.
      // Removing: FIXME-ENERO
      createShipmentLine(resStock.getAttributeSetValue(), resStock.getStorageBin(), quantity,
          orderLine, shipment, pickingList);
      // createShipmentLine_FIXME_ENERO_REPLACE(resStock.getAttributeSetValue(),
      // resStock.getStorageBin(), quantity, orderLine, shipment,
      // pickingList);
    }

    OBDal.getInstance().flush();
  }

  // Removing: FIXME-ENERO --funcion para ingreso info, para enero utilizar
  // createShipmentLine
  // private void createShipmentLine_FIXME_ENERO_REPLACE(AttributeSetInstance
  // asi, Locator sb,
  // BigDecimal qty, OrderLine orderLine, ShipmentInOut shipment, PickingList
  // pickingList) {
  //
  // OBCriteria<Locator> locators =
  // OBDal.getInstance().createCriteria(Locator.class);
  // locators.add(Restrictions.eq(Locator.PROPERTY_WAREHOUSE,
  // shipment.getWarehouse()));
  // locators.add(Restrictions.eq(Locator.PROPERTY_SEARCHKEY, "*00"));
  // locators.add(Restrictions.eq(swaProductWarehouse.PROPERTY_CLIENT,
  // shipment.getClient()));
  // List<Locator> locatorList = locators.list();
  //
  // if (locatorList.size() == 0) {// NO HAY UBICACION *00
  // throw new OBException(" NO HAY UBICACION *00 - ");
  // }
  //
  // Locator locator00 = locatorList.get(0);// se supone que el primero es *00
  // sb = locator00;
  //
  // ShipmentInOutLine line =
  // OBProvider.getInstance().get(ShipmentInOutLine.class);
  // line.setOrganization(shipment.getOrganization());
  // line.setShipmentReceipt(shipment);
  // line.setSalesOrderLine(orderLine);
  // line.setObwplPickinglist(pickingList);
  // line.setLineNo(lineNo);
  // lineNo += 10L;
  // line.setProduct(orderLine.getProduct());
  // line.setUOM(orderLine.getUOM());
  //
  // line.setAttributeSetValue(asi);
  // line.setStorageBin(sb);
  // line.setMovementQuantity(qty);
  // line.setDescription(orderLine.getDescription());
  // line.setExplode(orderLine.isExplode());
  //
  // if (orderLine.getBOMParent() != null) {
  // OBCriteria<ShipmentInOutLine> obc = OBDal.getInstance().createCriteria(
  // ShipmentInOutLine.class);
  // obc.add(Restrictions.eq(ShipmentInOutLine.PROPERTY_SHIPMENTRECEIPT,
  // shipment));
  // obc.add(Restrictions.eq(ShipmentInOutLine.PROPERTY_SALESORDERLINE,
  // orderLine.getBOMParent()));
  // obc.setMaxResults(1);
  // line.setBOMParent((ShipmentInOutLine) obc.uniqueResult());
  // }
  //
  // shipment.getMaterialMgmtShipmentInOutLineList().add(line);
  // OBDal.getInstance().save(line);
  // OBDal.getInstance().save(shipment);
  // }

  private void createShipmentLine(AttributeSetInstance asi, Locator sb, BigDecimal qty,
      OrderLine orderLine, ShipmentInOut shipment, PickingList pickingList) {
    ShipmentInOutLine line = OBProvider.getInstance().get(ShipmentInOutLine.class);
    line.setOrganization(shipment.getOrganization());
    line.setShipmentReceipt(shipment);
    line.setSalesOrderLine(orderLine);
    line.setObwplPickinglist(pickingList);
    line.setLineNo(lineNo);
    lineNo += 10L;
    line.setProduct(orderLine.getProduct());
    line.setUOM(orderLine.getUOM());

    line.setAttributeSetValue(asi);
    line.setStorageBin(sb);
    line.setMovementQuantity(qty);
    line.setDescription(orderLine.getDescription());
    line.setExplode(orderLine.isExplode());

    if (orderLine.getBOMParent() != null) {
      OBCriteria<ShipmentInOutLine> obc = OBDal.getInstance()
          .createCriteria(ShipmentInOutLine.class);
      obc.add(Restrictions.eq(ShipmentInOutLine.PROPERTY_SHIPMENTRECEIPT, shipment));
      obc.add(Restrictions.eq(ShipmentInOutLine.PROPERTY_SALESORDERLINE, orderLine.getBOMParent()));
      obc.setMaxResults(1);
      line.setBOMParent((ShipmentInOutLine) obc.uniqueResult());
    }

    shipment.getMaterialMgmtShipmentInOutLineList().add(line);
    OBDal.getInstance().save(line);
    OBDal.getInstance().save(shipment);
  }

  private void createReserveforServiceLine(Locator locators, BigDecimal qty,
      SREServiceorderline orderLine, ShipmentInOut shipment, PickingList pickingList) {

    // Creando m_reservation
    Reservation mReservation = OBProvider.getInstance().get(Reservation.class);
    mReservation.setOrganization(shipment.getOrganization());
    mReservation.setProduct(orderLine.getProduct());
    Product pd = OBDal.getInstance().get(Product.class, orderLine.getProduct().getId());
    mReservation.setUOM(pd.getUOM());
    mReservation.setQuantity(qty);
    mReservation.setRESStatus("DR");
    mReservation.setRESProcess("PR");
    mReservation.setWarehouse(locators.getWarehouse());
    mReservation.setOBWPLGeneratedByPickingList(true);
    mReservation.setSwaServiceorderline(orderLine);

    OBDal.getInstance().save(mReservation);
    OBDal.getInstance().flush();
    OBDal.getInstance().refresh(mReservation);

    // Creando m_reseravation_stock

    ReservationStock mReservationStock = OBProvider.getInstance().get(ReservationStock.class);
    mReservationStock.setOrganization(shipment.getOrganization());
    mReservationStock.setReservation(mReservation);
    mReservationStock.setQuantity(qty);
    mReservationStock.setStorageBin(locators);

    OBDal.getInstance().save(mReservationStock);
    OBDal.getInstance().flush();
    OBDal.getInstance().refresh(mReservationStock);

    final List<Object> param = new ArrayList<Object>();

    param.add(null);
    param.add(mReservation.getId());
    param.add("PR");
    param.add(mReservation.getCreatedBy().getId());
    CallStoredProcedure.getInstance().call("M_RESERVATION_POST", param, null, true, false);
  }

  private void createShipmentLine3(Locator locators, BigDecimal qty, SREServiceorderline orderLine,
      ShipmentInOut shipment, PickingList pickingList) {
    ShipmentInOutLine line = OBProvider.getInstance().get(ShipmentInOutLine.class);
    line.setOrganization(shipment.getOrganization());
    line.setShipmentReceipt(shipment);
    line.setObwplPickinglist(pickingList);
    line.setLineNo(lineNo);
    lineNo += 10L;
    line.setProduct(orderLine.getProduct());
    Product pd = OBDal.getInstance().get(Product.class, orderLine.getProduct().getId());
    line.setUOM(pd.getUOM());
    line.setSreServiceOrderLine(orderLine);
    line.setStorageBin(locators);
    BigDecimal qtyDecimal = qty;
    line.setMovementQuantity(qtyDecimal);
    line.setExplode(false);
    shipment.getMaterialMgmtShipmentInOutLineList().add(line);
    OBDal.getInstance().save(line);
    OBDal.getInstance().save(shipment);
    OBDal.getInstance().save(line);
  }

  private void createShipmentLine4(Locator locators, BigDecimal qty, OrderLine orderLine,
      ShipmentInOut shipment, PickingList pickingList) {
    ShipmentInOutLine line = OBProvider.getInstance().get(ShipmentInOutLine.class);
    line.setOrganization(shipment.getOrganization());
    line.setShipmentReceipt(shipment);
    line.setObwplPickinglist(pickingList);
    line.setLineNo(lineNo);
    lineNo += 10L;
    line.setProduct(orderLine.getProduct());
    Product pd = OBDal.getInstance().get(Product.class, orderLine.getProduct().getId());
    line.setUOM(pd.getUOM());
    line.setSalesOrderLine(orderLine);
    line.setStorageBin(locators);
    BigDecimal qtyDecimal = qty;
    line.setMovementQuantity(qtyDecimal);
    line.setExplode(false);
    shipment.getMaterialMgmtShipmentInOutLineList().add(line);
    OBDal.getInstance().save(line);
    OBDal.getInstance().save(shipment);
    OBDal.getInstance().save(line);
  }

  private void createShipmentLine2(List<Locator> sb, Long qty,
      SwaRequerimientoReposicionDetail orderLine, ShipmentInOut shipment, PickingList pickingList,
      int i) {
    ShipmentInOutLine line = OBProvider.getInstance().get(ShipmentInOutLine.class);
    line.setOrganization(shipment.getOrganization());
    line.setShipmentReceipt(shipment);
    line.setObwplPickinglist(pickingList);
    line.setLineNo(lineNo);
    lineNo += 10L;
    line.setProduct(orderLine.getProduct());
    Product pd = OBDal.getInstance().get(Product.class, orderLine.getProduct().getId());
    line.setUOM(pd.getUOM());

    line.setStorageBin(sb.get(i));
    BigDecimal qtyDecimal = new BigDecimal(qty);
    line.setMovementQuantity(qtyDecimal);
    line.setExplode(false);
    shipment.getMaterialMgmtShipmentInOutLineList().add(line);
    OBDal.getInstance().save(line);
    OBDal.getInstance().save(shipment);
    orderLine.setAlertStatus("PR");
    OBDal.getInstance().save(line);
  }

  private void createShipmentLine2_inoutreference(Locator locators, BigDecimal qty,
      SwaRequerimientoReposicionDetail orderLine, ShipmentInOut shipment, PickingList pickingList) {

    Product pd = OBDal.getInstance().get(Product.class, orderLine.getProduct().getId());
    BigDecimal qtyDecimal = qty;

    ShipmentInOutLine line = OBProvider.getInstance().get(ShipmentInOutLine.class);
    line.setOrganization(shipment.getOrganization());
    line.setShipmentReceipt(shipment);
    line.setLineNo(lineNo);
    line.setProduct(orderLine.getProduct());
    line.setUOM(pd.getUOM());
    line.setStorageBin(locators);
    line.setMovementQuantity(qtyDecimal);
    line.setExplode(false);
    shipment.getMaterialMgmtShipmentInOutLineList().add(line);
    lineNo += 10L;
    OBDal.getInstance().save(line);
    OBDal.getInstance().save(shipment);
    OBDal.getInstance().save(line);
  }

  private void createShipmentLineToReposition(Locator sb, BigDecimal qty,
      SwaRequerimientoReposicionDetail orderLine, ShipmentInOut shipment, PickingList pickingList) {
    Product pd = OBDal.getInstance().get(Product.class, orderLine.getProduct().getId());
    BigDecimal qtyDecimal = qty;

    // creando m_inoutline
    ShipmentInOutLine line = OBProvider.getInstance().get(ShipmentInOutLine.class);
    line.setOrganization(shipment.getOrganization());
    line.setShipmentReceipt(shipment);
    line.setObwplPickinglist(pickingList);
    line.setSwaRequerepoDetail(orderLine);
    line.setLineNo(lineNo);
    lineNo += 10L;
    line.setProduct(orderLine.getProduct());
    line.setUOM(pd.getUOM());
    line.setStorageBin(sb);
    line.setMovementQuantity(qtyDecimal);
    line.setExplode(false);
    shipment.getMaterialMgmtShipmentInOutLineList().add(line);
    OBDal.getInstance().save(line);
    OBDal.getInstance().save(shipment);
    orderLine.setAlertStatus("PR");
    OBDal.getInstance().save(line);
  }
}