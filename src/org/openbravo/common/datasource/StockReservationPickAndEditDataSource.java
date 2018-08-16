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
 * All portions are Copyright (C) 2014 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.common.datasource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.materialmgmt.ReservationUtils;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.OrgWarehouse;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.onhandquantity.Reservation;
import org.openbravo.model.materialmgmt.onhandquantity.ReservationStock;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;
import org.openbravo.model.procurement.POInvoiceMatch;
import org.openbravo.service.datasource.ReadOnlyDataSourceService;
import org.openbravo.service.json.DataToJsonConverter;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.service.json.JsonUtils;

public class StockReservationPickAndEditDataSource extends ReadOnlyDataSourceService {

  private static Logger log4j = Logger.getLogger(StockReservationPickAndEditDataSource.class);
  private static final String AD_TABLE_ID = "7BDAC914CA60418795E453BC0E8C89DC";

  String ol = null;

  @Override
  public Entity getEntity() {
    return ModelProvider.getInstance().getEntityByTableId(AD_TABLE_ID);
  }

  @Override
  public String fetch(Map<String, String> parameters) {
    int startRow = 0;

    final List<JSONObject> jsonObjects = fetchJSONObject(parameters);

    final JSONObject jsonResult = new JSONObject();
    final JSONObject jsonResponse = new JSONObject();
    try {
      jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      jsonResponse.put(JsonConstants.RESPONSE_STARTROW, startRow);
      jsonResponse.put(JsonConstants.RESPONSE_ENDROW, jsonObjects.size() - 1);
      jsonResponse.put(JsonConstants.RESPONSE_TOTALROWS, jsonObjects.size());
      jsonResponse.put(JsonConstants.RESPONSE_DATA, new JSONArray(jsonObjects));
      jsonResult.put(JsonConstants.RESPONSE_RESPONSE, jsonResponse);
    } catch (JSONException e) {
    }

    return jsonResult.toString();
  }

  private List<JSONObject> fetchJSONObject(Map<String, String> parameters) {
    final int startRow = Integer.parseInt(parameters.get(JsonConstants.STARTROW_PARAMETER));
    final int endRow = Integer.parseInt(parameters.get(JsonConstants.ENDROW_PARAMETER));
    final List<Map<String, Object>> data = getData(parameters, startRow, endRow);
    final DataToJsonConverter toJsonConverter = OBProvider.getInstance().get(
        DataToJsonConverter.class);
    toJsonConverter.setAdditionalProperties(JsonUtils.getAdditionalProperties(parameters));
    return toJsonConverter.convertToJsonObjects(data);
  }

  @Override
  protected List<Map<String, Object>> getData(Map<String, String> parameters, int startRow,
      int endRow) {
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    if (parameters.get(JsonConstants.DISTINCT_PARAMETER) != null) {
      String distinct = (String) parameters.get(JsonConstants.DISTINCT_PARAMETER);
      log4j.debug("Distinct param: " + distinct);
      if ("warehouse".equals(distinct)) {
        result = getWarehouseFilterData(parameters);

      } else if ("storageBin".equals(distinct)) {
        result = getStorageFilterData(parameters);

      } else if ("attributeSetValue".equals(distinct)) {
        result = getAttributeSetValueFilterData(parameters);

      } else if ("purchaseOrderLine".equals(distinct)) {
        result = getOrderLineSetValueFilterData(parameters);

      }
    } else {
      result = getGridData(parameters);
      log4j.debug("data length: " + result.size());
    }
    return result;
  }

  private List<Map<String, Object>> getOrderLineSetValueFilterData(Map<String, String> parameters) {
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    Map<String, String> filterCriteria = new HashMap<String, String>();
    try {
      // Builds the criteria based on the fetch parameters
      JSONArray criterias = (JSONArray) JsonUtils.buildCriteria(parameters).get("criteria");
      for (int i = 0; i < criterias.length(); i++) {
        final JSONObject criteria = criterias.getJSONObject(i);
        filterCriteria.put(criteria.getString("fieldName"), criteria.getString("value"));
      }
    } catch (JSONException e) {
      log4j.error("Error while building the criteria", e);
    }
    OBContext.setAdminMode();
    try {
      for (OrderLine o : getOrderLineFromGrid(filterCriteria.get("orderLine$_identifier"),
          getGridData(parameters))) {
        Map<String, Object> myMap = new HashMap<String, Object>();
        myMap.put("id", o.getId());
        myMap.put("name", o.getIdentifier());
        myMap.put("_identifier", o.getIdentifier());
        myMap.put("_entityName", "OrderLine");
        result.add(myMap);
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return result;
  }

  private List<OrderLine> getOrderLineFromGrid(String string, List<Map<String, Object>> data) {
    Set<String> ids = new HashSet<String>();
    for (Map<String, Object> record : data) {
      ids.add((String) record.get("purchaseOrderLine"));
    }
    OBCriteria<OrderLine> obc = OBDal.getInstance().createCriteria(OrderLine.class);
    obc.add(Restrictions.in(OrderLine.PROPERTY_ID, ids));
    obc.setFilterOnReadableClients(false);
    obc.setFilterOnReadableOrganization(false);
    obc.setFilterOnActive(false);
    obc.addOrderBy(OrderLine.PROPERTY_SALESORDER, false);
    obc.addOrderBy(OrderLine.PROPERTY_LINENO, true);
    return obc.list();
  }

  private List<Map<String, Object>> getWarehouseFilterData(Map<String, String> parameters) {
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    Map<String, String> filterCriteria = new HashMap<String, String>();
    try {
      // Builds the criteria based on the fetch parameters
      JSONArray criterias = (JSONArray) JsonUtils.buildCriteria(parameters).get("criteria");
      for (int i = 0; i < criterias.length(); i++) {
        final JSONObject criteria = criterias.getJSONObject(i);
        filterCriteria.put(criteria.getString("fieldName"), criteria.getString("value"));
      }
    } catch (JSONException e) {
      log4j.error("Error while building the criteria", e);
    }
    OBContext.setAdminMode();
    try {
      for (Warehouse o : getWarehouseFromGrid(filterCriteria.get("warehouse$_identifier"),
          getGridData(parameters))) {
        Map<String, Object> myMap = new HashMap<String, Object>();
        myMap.put("id", o.getId());
        myMap.put("name", o.getIdentifier());
        myMap.put("_identifier", o.getIdentifier());
        myMap.put("_entityName", "Warehouse");
        result.add(myMap);
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return result;
  }

  private List<Warehouse> getWarehouseFromGrid(String contains, List<Map<String, Object>> data) {
    Set<String> ids = new HashSet<String>();
    for (Map<String, Object> record : data) {
      if (contains != null && !"".equals(contains)) {
        Warehouse warehouse = OBDal.getInstance().get(Warehouse.class,
            (String) record.get("warehouse"));
        if (warehouse.getIdentifier().contains(contains)) {
          continue;
        }
      }
      ids.add((String) record.get("warehouse"));
    }
    OBCriteria<Warehouse> obc = OBDal.getInstance().createCriteria(Warehouse.class);
    obc.add(Restrictions.in(Warehouse.PROPERTY_ID, ids));
    obc.setFilterOnReadableClients(false);
    obc.setFilterOnReadableOrganization(false);
    obc.setFilterOnActive(false);
    obc.addOrderBy(Warehouse.PROPERTY_NAME, true);
    return obc.list();
  }

  private List<Warehouse> getFilteredWarehouse(String contains, Map<String, String> parameters) {
    String strReservation = parameters.get("@MaterialMgmtReservation.id@");
    Reservation reservation = OBDal.getInstance().get(Reservation.class, strReservation);
    new OrganizationStructureProvider().getChildTree(reservation.getOrganization().getId(), true);
    OBCriteria<Warehouse> obc = OBDal.getInstance().createCriteria(Warehouse.class);
    if (reservation.getWarehouse() != null) {
      obc.add(Restrictions.eq(Warehouse.PROPERTY_ID, reservation.getWarehouse().getId()));
      return obc.list();
    }
    if (reservation.getStorageBin() != null) {
      obc.add(Restrictions.eq(Warehouse.PROPERTY_ID, reservation.getStorageBin().getWarehouse()
          .getId()));
      return obc.list();
    }
    // Just on hand warehouses are taken into account as per window validation
    obc.add(Restrictions.in(Warehouse.PROPERTY_ID,
        getOnHandWarehouseIds(reservation.getOrganization())));
    if (contains != null && !"".equals(contains)) {
      if (contains.startsWith("[")) {
        try {
          JSONArray myJSON = new JSONArray(contains);

          Criterion myCriterion = null;
          for (int i = 0; i < myJSON.length(); i++) {
            JSONObject myJSONObject = (JSONObject) myJSON.get(i);
            String operator = (String) myJSONObject.get("operator");

            if (myJSONObject.get("fieldName").equals("warehouse$_identifier")) {
              if (myCriterion == null) {
                if (operator.equals("iEquals")) {
                  myCriterion = Restrictions.ilike(Warehouse.PROPERTY_NAME,
                      myJSONObject.get("value"));
                } else if (operator.equals("iContains")) {
                  myCriterion = Restrictions.ilike(Warehouse.PROPERTY_NAME,
                      "%" + myJSONObject.get("value") + "%");
                }
              } else {
                myCriterion = Restrictions.or(
                    myCriterion,
                    Restrictions.ilike(Warehouse.PROPERTY_NAME, "%" + myJSONObject.get("value")
                        + "%"));
              }
            }
          }
          if (myCriterion != null) {
            obc.add(myCriterion);
          }
        } catch (JSONException e) {
          log4j.error("Error getting filter for warehouses", e);
        }
      } else {
        obc.add(Restrictions.ilike(Warehouse.PROPERTY_NAME, "%" + contains + "%"));
      }
    }
    obc.addOrder(Order.asc(Warehouse.PROPERTY_NAME));
    return obc.list();
  }

  private List<String> getOnHandWarehouseIds(Organization organization) {
    List<String> result = new ArrayList<String>();
    for (Warehouse o : getOnHandWarehouses(organization)) {
      result.add(o.getId());
    }
    return result;
  }

  private List<Warehouse> getOnHandWarehouses(Organization organization) {
    List<Warehouse> result = new ArrayList<Warehouse>();
    OBCriteria<OrgWarehouse> obc = OBDal.getInstance().createCriteria(OrgWarehouse.class);
    obc.add(Restrictions.eq(OrgWarehouse.PROPERTY_ORGANIZATION, organization));
    for (OrgWarehouse ow : obc.list()) {
      result.add(ow.getWarehouse());
    }
    return result;
  }

  private List<Map<String, Object>> getStorageFilterData(Map<String, String> parameters) {
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    Map<String, String> filterCriteria = new HashMap<String, String>();
    try {
      // Builds the criteria based on the fetch parameters
      JSONArray criterias = (JSONArray) JsonUtils.buildCriteria(parameters).get("criteria");
      for (int i = 0; i < criterias.length(); i++) {
        final JSONObject criteria = criterias.getJSONObject(i);
        filterCriteria.put(criteria.getString("fieldName"), criteria.getString("value"));
      }
    } catch (JSONException e) {
      log4j.error("Error while building the criteria", e);
    }
    OBContext.setAdminMode();
    try {
      for (Locator o : getStorageBinFromGrid(filterCriteria.get("storageBin$_identifier"),
          getGridData(parameters))) {
        Map<String, Object> myMap = new HashMap<String, Object>();
        myMap.put("id", o.getId());
        myMap.put("name", o.getIdentifier());
        myMap.put("_identifier", o.getIdentifier());
        myMap.put("_entityName", "Locator");
        result.add(myMap);
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return result;
  }

  private List<Locator> getStorageBinFromGrid(String contains, List<Map<String, Object>> data) {
    Set<String> ids = new HashSet<String>();
    for (Map<String, Object> record : data) {
      if (contains != null && !"".equals(contains)) {
        Locator locator = OBDal.getInstance().get(Locator.class, (String) record.get("storageBin"));
        if (locator.getIdentifier().contains(contains)) {
          continue;
        }
      }
      ids.add((String) record.get("storageBin"));
    }
    OBCriteria<Locator> obc = OBDal.getInstance().createCriteria(Locator.class);
    obc.add(Restrictions.in(Locator.PROPERTY_ID, ids));
    obc.setFilterOnReadableClients(false);
    obc.setFilterOnReadableOrganization(false);
    obc.setFilterOnActive(false);
    obc.addOrderBy(Locator.PROPERTY_WAREHOUSE, true);
    obc.addOrderBy(Locator.PROPERTY_ROWX, true);
    obc.addOrderBy(Locator.PROPERTY_STACKY, true);
    obc.addOrderBy(Locator.PROPERTY_LEVELZ, true);
    return obc.list();
  }

  private List<Locator> getFilteredStorageBin(String contains, Map<String, String> parameters) {
    String strReservation = parameters.get("@MaterialMgmtReservation.id@");
    Reservation reservation = OBDal.getInstance().get(Reservation.class, strReservation);
    new OrganizationStructureProvider().getChildTree(reservation.getOrganization().getId(), true);
    OBCriteria<Locator> obc = OBDal.getInstance().createCriteria(Locator.class);
    obc.add(Restrictions.in(Locator.PROPERTY_WAREHOUSE,
        getOnHandWarehouses(reservation.getOrganization())));
    if (reservation.getWarehouse() != null) {
      obc.add(Restrictions.eq(Locator.PROPERTY_WAREHOUSE, reservation.getWarehouse()));
    }
    if (reservation.getStorageBin() != null) {
      obc.add(Restrictions.eq(Locator.PROPERTY_ID, reservation.getStorageBin().getId()));
      return obc.list();
    }
    if (contains != null && !"".equals(contains)) {
      if (contains.startsWith("[")) {
        try {
          JSONArray myJSON = new JSONArray(contains);
          Criterion myCriterion = null;
          for (int i = 0; i < myJSON.length(); i++) {
            JSONObject myJSONObject = (JSONObject) myJSON.get(i);
            String operator = (String) myJSONObject.get("operator");
            if (myJSONObject.get("fieldName").equals("storageBin$_identifier")
                && myJSONObject.has("value")) {
              if (myCriterion == null) {
                if (operator.equals("iEquals")) {
                  myCriterion = Restrictions.ilike(Locator.PROPERTY_SEARCHKEY,
                      myJSONObject.get("value"));
                } else if (operator.equals("iContains")) {
                  myCriterion = Restrictions.ilike(Locator.PROPERTY_SEARCHKEY,
                      "%" + myJSONObject.get("value") + "%");
                }
              } else {
                myCriterion = Restrictions.or(
                    myCriterion,
                    Restrictions.ilike(Locator.PROPERTY_SEARCHKEY, "%" + myJSONObject.get("value")
                        + "%"));
              }
            }
          }
          if (myCriterion != null) {
            obc.add(myCriterion);
          }
        } catch (JSONException e) {
          log4j.error("Error getting filter for storage bins", e);
        }
      } else {
        obc.add(Restrictions.ilike(Locator.PROPERTY_SEARCHKEY, "%" + contains + "%"));
      }
    }
    obc.addOrderBy(Locator.PROPERTY_WAREHOUSE, true);
    obc.addOrderBy(Locator.PROPERTY_ROWX, true);
    obc.addOrderBy(Locator.PROPERTY_STACKY, true);
    obc.addOrderBy(Locator.PROPERTY_LEVELZ, true);
    return obc.list();
  }

  private List<Map<String, Object>> getAttributeSetValueFilterData(Map<String, String> parameters) {
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    Map<String, String> filterCriteria = new HashMap<String, String>();
    try {
      // Builds the criteria based on the fetch parameters
      JSONArray criterias = (JSONArray) JsonUtils.buildCriteria(parameters).get("criteria");
      for (int i = 0; i < criterias.length(); i++) {
        final JSONObject criteria = criterias.getJSONObject(i);
        filterCriteria.put(criteria.getString("fieldName"), criteria.getString("value"));
      }
    } catch (JSONException e) {
      log4j.error("Error while building the criteria", e);
    }
    OBContext.setAdminMode();
    try {
      for (AttributeSetInstance o : getAttributeSetValueFromGrid(
          filterCriteria.get("attributeSetValue$_identifier"), getGridData(parameters))) {
        Map<String, Object> myMap = new HashMap<String, Object>();
        myMap.put("id", o.getId());
        myMap.put("name", o.getIdentifier());
        myMap.put("_identifier", o.getIdentifier());
        myMap.put("_entityName", "Locator");
        result.add(myMap);
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return result;
  }

  private List<AttributeSetInstance> getAttributeSetValueFromGrid(String contains,
      List<Map<String, Object>> data) {
    Set<String> ids = new HashSet<String>();
    for (Map<String, Object> record : data) {
      ids.add((String) record.get("attributeSetValue"));
    }
    OBCriteria<AttributeSetInstance> obc = OBDal.getInstance().createCriteria(
        AttributeSetInstance.class);
    obc.add(Restrictions.in(AttributeSetInstance.PROPERTY_ID, ids));
    obc.setFilterOnReadableClients(false);
    obc.setFilterOnReadableOrganization(false);
    obc.setFilterOnActive(false);
    obc.addOrderBy(AttributeSetInstance.PROPERTY_DESCRIPTION, true);
    return obc.list();
  }

  private List<Map<String, Object>> getGridData(Map<String, String> parameters) {
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    Map<String, String> filterCriteria = new HashMap<String, String>();
    ArrayList<String> selectedIds = new ArrayList<String>();
    try {
      // Builds the criteria based on the fetch parameters
      JSONArray criterias = (JSONArray) JsonUtils.buildCriteria(parameters).get("criteria");

      for (int i = 0; i < criterias.length(); i++) {
        final JSONObject criteria = criterias.getJSONObject(i);
        if (criteria.has("fieldName") && criteria.getString("fieldName").equals("id")) {
          if (criteria.has("value")) {
            selectedIds.add(criteria.has("value") ? criteria.getString("value") : criteria
                .toString());
          }
        }

        // Multiple selection
        if (criteria.has("criteria") && criteria.has("operator")) {
          JSONArray mySon = new JSONArray(criteria.getString("criteria"));
          for (int j = 0; j < mySon.length(); j++) {
            if (filterCriteria.containsKey(mySon.getJSONObject(j).getString("fieldName"))) {
              JSONArray values = new JSONArray(filterCriteria.get(mySon.getJSONObject(j).getString(
                  "fieldName")));
              filterCriteria.put(mySon.getJSONObject(j).getString("fieldName"), values.put(mySon)
                  .toString());
            } else {
              filterCriteria.put(mySon.getJSONObject(j).getString("fieldName"), new JSONArray()
                  .put(mySon.getJSONObject(j)).toString());
            }
          }
          // lessOrEqual
        } else if (criteria.has("operator")
            && ("greaterThan".equals(criteria.getString("operator"))
                || "lessThan".equals(criteria.getString("operator"))
                || "greaterOrEqual".equals(criteria.getString("operator")) || "lessOrEqual"
                  .equals(criteria.getString("operator")))) {
          filterCriteria.put(criteria.getString("fieldName"), criteria.toString());
        } else if (criteria.has("operator")
            && ("iEquals".equals(criteria.getString("operator")) || "iContains".equals(criteria
                .getString("operator")))) {

          if (filterCriteria.containsKey(criteria.getString("fieldName"))) {
            JSONArray myson = new JSONArray(filterCriteria.get(criteria.getString("fieldName")));
            filterCriteria.put(criteria.getString("fieldName"), myson.put(criteria).toString());
          } else {
            JSONArray myson = new JSONArray();
            filterCriteria.put(criteria.getString("fieldName"), myson.put(criteria).toString());
          }

        } else {
          filterCriteria.put(criteria.getString("fieldName"),
              criteria.has("value") ? criteria.getString("value") : criteria.toString());
        }
      }

    } catch (JSONException e) {
      log4j.error("Error while building the criteria", e);
    }
    OBContext.setAdminMode();
    String strReservation = parameters.get("@MaterialMgmtReservation.id@");
    ol = parameters.get("@OrderLine.id@");
    Reservation reservation = null;
    // Filters
    List<Warehouse> warehousesFiltered = null;
    if (filterCriteria.get("warehouse$_identifier") != null) {
      warehousesFiltered = getFilteredWarehouse(filterCriteria.get("warehouse$_identifier"),
          parameters);
    }
    List<Locator> locatorsFiltered = null;
    if (filterCriteria.get("storageBin$_identifier") != null) {
      locatorsFiltered = getFilteredStorageBin(filterCriteria.get("storageBin$_identifier"),
          parameters);
    }
    List<AttributeSetInstance> attributesFiltered = null;
    if (filterCriteria.get("attributeSetValue$_identifier") != null) {
      attributesFiltered = getFilteredAttribute(
          filterCriteria.get("attributeSetValue$_identifier"), parameters);
    }
    List<OrderLine> orderLinesFiltered = null;
    if (filterCriteria.get("purchaseOrderLine$_identifier") != null) {
      orderLinesFiltered = getFilteredOrderline(
          filterCriteria.get("purchaseOrderLine$_identifier"), parameters);
    }
    String availableQtyFilterCriteria = "";
    if (filterCriteria.get("availableQty") != null) {
      availableQtyFilterCriteria = filterCriteria.get("availableQty");
    }
    String reservedinothersFilterCriteria = "";
    if (filterCriteria.get("reservedinothers") != null) {
      reservedinothersFilterCriteria = filterCriteria.get("reservedinothers");
    }
    String releasedFilterCriteria = "";
    if (filterCriteria.get("released") != null) {
      releasedFilterCriteria = filterCriteria.get("released");
    }

    if (ol != null && !"".equals(ol)) {
      reservation = ReservationUtils.getReservationFromOrder(OBDal.getInstance().get(
          OrderLine.class, ol));
      ReservationUtils.processReserve(reservation, "PR");
    } else {
      reservation = OBDal.getInstance().get(Reservation.class, strReservation);
    }
    String strOrganization = parameters.get("@MaterialMgmtReservation.organization@");
    if (strOrganization == null || strOrganization.equals("")) {
      strOrganization = parameters.get("@Order.organization@");
    }
    Set<String> organizations = new OrganizationStructureProvider().getChildTree(strOrganization,
        true);
    try {
      result.addAll(getSelectedLines(reservation));
      if (orderLinesFiltered == null || orderLinesFiltered.size() == 0) {
        result.addAll(getStorageDetail(reservation, organizations, warehousesFiltered,
            locatorsFiltered, attributesFiltered, availableQtyFilterCriteria,
            reservedinothersFilterCriteria, releasedFilterCriteria, selectedIds));
      }
      if (locatorsFiltered == null || locatorsFiltered.size() == 0) {
        result.addAll(getPurchaseOrderLines(reservation, organizations, warehousesFiltered,
            attributesFiltered, orderLinesFiltered, availableQtyFilterCriteria,
            reservedinothersFilterCriteria, releasedFilterCriteria, selectedIds));
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    if (ol != null && !"".equals(ol)) {
      OBDal.getInstance().rollbackAndClose();
    }
    result = sortResult(result, parameters.get("_sortBy"));
    return result;
  }

  private List<Map<String, Object>> sortResult(List<Map<String, Object>> result, String sortBy) {
    if (sortBy == null || "".equals(sortBy)) {
      return result;
    } else {

      try {
        Collections.sort(result, new MapComparator(sortBy));
      } catch (Exception e) {
        log4j.error("Error in sortResult. sortBy: " + sortBy != null ? sortBy : "null");
        return result;
      }

      return result;
    }
  }

  private static class MapComparator implements Comparator<Map<String, Object>> {
    private String compareBy = "convertedFreightAmount";
    boolean desc = false;

    public MapComparator(String _compareBy) {
      compareBy = _compareBy;
      if (compareBy.startsWith("-")) {
        desc = true;
        compareBy = compareBy.substring(1);
      }
    }

    @Override
    public int compare(Map<String, Object> o1, Map<String, Object> o2) {
      Object obj1 = o1.get(compareBy);
      Object obj2 = o2.get(compareBy);
      if (obj2 == null) {
        return -1;
      } else if (obj1 == null) {
        return 1;
      }
      if (obj1 instanceof BigDecimal) {
        final BigDecimal v1 = (BigDecimal) o1.get(compareBy);
        final BigDecimal v2 = (BigDecimal) o2.get(compareBy);
        return desc ? v2.compareTo(v1) : v1.compareTo(v2);
      } else if (obj1 instanceof String) {
        final String v1 = ((String) o1.get(compareBy)).toLowerCase();
        final String v2 = ((String) o2.get(compareBy)).toLowerCase();
        return desc ? v2.compareTo(v1) : v1.compareTo(v2);
      } else if (obj1 instanceof BaseOBObject) {
        final String v1 = ((BaseOBObject) o1.get(compareBy)).getIdentifier();
        final String v2 = ((BaseOBObject) o2.get(compareBy)).getIdentifier();
        return desc ? v2.compareTo(v1) : v1.compareTo(v2);
      } else if (obj1 instanceof Boolean) {
        final boolean v1 = (Boolean) o1.get(compareBy);
        final boolean v2 = (Boolean) o2.get(compareBy);
        if (v1 == v2) {
          return 0;
        }
        if (v1) {
          return desc ? -1 : 1;
        } else {
          return desc ? 1 : -1;
        }
      } else {
        // unable to compare
        return 0;
      }
    }
  }

  private List<AttributeSetInstance> getFilteredAttribute(String contains,
      Map<String, String> parameters) {
    String strReservation = parameters.get("@MaterialMgmtReservation.id@");
    Reservation reservation = OBDal.getInstance().get(Reservation.class, strReservation);
    new OrganizationStructureProvider().getChildTree(reservation.getOrganization().getId(), true);
    OBCriteria<AttributeSetInstance> obc = OBDal.getInstance().createCriteria(
        AttributeSetInstance.class);
    if (reservation.getAttributeSetValue() != null) {
      obc.add(Restrictions.eq(AttributeSetInstance.PROPERTY_ID, reservation.getAttributeSetValue()));
    }
    if (contains != null && !"".equals(contains)) {
      Criterion myCriterion = null;
      if (contains.startsWith("[")) {
        try {
          JSONArray myJSON = new JSONArray(contains);
          for (int i = 0; i < myJSON.length(); i++) {
            JSONObject myJSONObject = (JSONObject) myJSON.get(i);
            String operator = (String) myJSONObject.get("operator");
            if (myJSONObject.get("fieldName").equals("attributeSetValue$_identifier")
                && myJSONObject.has("value")) {
              if (myCriterion == null) {
                if (operator.equals("iContains")) {
                  myCriterion = Restrictions.ilike(AttributeSetInstance.PROPERTY_DESCRIPTION, "%"
                      + myJSONObject.get("value") + "%");
                } else if (operator.equals("iEquals")) {
                  myCriterion = Restrictions.ilike(AttributeSetInstance.PROPERTY_DESCRIPTION,
                      myJSONObject.get("value"));
                }
              } else {
                myCriterion = Restrictions.or(
                    myCriterion,
                    Restrictions.ilike(AttributeSetInstance.PROPERTY_DESCRIPTION, "%"
                        + myJSONObject.get("value") + "%"));
              }
            }
          }
          if (myCriterion != null) {
            obc.add(myCriterion);
          }
        } catch (JSONException e) {
          log4j.error("Error getting filter for attribute", e);
        }
      } else {
        obc.add(Restrictions.ilike(AttributeSetInstance.PROPERTY_DESCRIPTION, "%" + contains + "%"));
      }
    }
    obc.addOrder(Order.asc(AttributeSetInstance.PROPERTY_DESCRIPTION));
    return obc.list();
  }

  private List<OrderLine> getFilteredOrderline(String contains, Map<String, String> parameters) {
    String strReservation = parameters.get("@MaterialMgmtReservation.id@");
    Reservation reservation = OBDal.getInstance().get(Reservation.class, strReservation);
    new OrganizationStructureProvider().getChildTree(reservation.getOrganization().getId(), true);
    OBCriteria<OrderLine> obc = OBDal.getInstance().createCriteria(OrderLine.class);
    obc.createAlias(OrderLine.PROPERTY_SALESORDER, "o");
    if (reservation.getAttributeSetValue() != null) {
      obc.add(Restrictions.eq(OrderLine.PROPERTY_ATTRIBUTESETVALUE,
          reservation.getAttributeSetValue()));
    }
    obc.add(Restrictions.eq(OrderLine.PROPERTY_PRODUCT, reservation.getProduct()));
    if (contains != null && !"".equals(contains)) {
      Criterion myCriterion = null;
      if (contains.startsWith("[")) {
        try {
          JSONArray myJSON = new JSONArray(contains);
          for (int i = 0; i < myJSON.length(); i++) {
            JSONObject myJSONObject = (JSONObject) myJSON.get(i);
            String operator = (String) myJSONObject.get("operator");
            if (myJSONObject.getString("fieldName").equals("purchaseOrderLine$_identifier")) {
              if (myCriterion == null) {
                if (operator.equals("iContains")) {
                  myCriterion = Restrictions.ilike("o."
                      + org.openbravo.model.common.order.Order.PROPERTY_DOCUMENTNO, "%"
                      + getOrderDocumentNo((String) myJSONObject.get("value")) + "%");
                } else if (operator.equals("iEquals")) {
                  myCriterion = Restrictions.ilike("o."
                      + org.openbravo.model.common.order.Order.PROPERTY_DOCUMENTNO,
                      getOrderDocumentNo((String) myJSONObject.get("value")));
                }
              } else {
                myCriterion = Restrictions.or(
                    myCriterion,
                    Restrictions.ilike("o."
                        + org.openbravo.model.common.order.Order.PROPERTY_DOCUMENTNO, "%"
                        + getOrderDocumentNo((String) myJSONObject.get("value")) + "%"));
              }
            }
          }
          if (myCriterion != null) {
            obc.add(myCriterion);
          }
        } catch (JSONException e) {
          log4j.error("Error getting filter for attribute", e);
        }
      } else {
        obc.add(Restrictions.ilike("o."
            + org.openbravo.model.common.order.Order.PROPERTY_DOCUMENTNO, "%"
            + getOrderDocumentNo(contains) + "%"));
      }
    }

    return obc.list();
  }

  private String getOrderDocumentNo(String orderLineIdentifier) {
    return new StringTokenizer(orderLineIdentifier).nextToken();
  }

  private List<Map<String, Object>> getSelectedLines(Reservation reservation) {
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    final StringBuilder hqlString = new StringBuilder();
    hqlString.append("select rs from MaterialMgmtReservationStock rs ");
    hqlString.append("join rs.reservation as r");
    hqlString.append(" where rs.reservation = :reservation ");
    hqlString.append(" order by rs.salesOrderLine DESC, r.warehouse, rs.storageBin");
    final Session session = OBDal.getInstance().getSession();
    Query query = session.createQuery(hqlString.toString());
    query.setParameter("reservation", reservation);
    for (Object o : query.list()) {
      Map<String, Object> myMap = new HashMap<String, Object>();
      ReservationStock rs = (ReservationStock) o;
      myMap.put("id", rs.getId());
      myMap.put("obSelected", true);
      if (ol == null || "".equals(ol)) {
        myMap.put("reservationStock", rs.getId());
        myMap.put("reservationStock$_identifier", rs.getIdentifier());
      } else {
        myMap.put("reservationStock", null);
        myMap.put("reservationStock$_identifier", "");
      }
      myMap.put("storageBin$_identifier", rs.getStorageBin() != null ? rs.getStorageBin()
          .getIdentifier() : "");
      myMap.put("storageBin", rs.getStorageBin() != null ? rs.getStorageBin().getId() : null);
      myMap.put("warehouse",
          (rs.getStorageBin() != null && rs.getStorageBin().getWarehouse() != null) ? rs
              .getStorageBin().getWarehouse().getId() : null);
      myMap.put("warehouse$_identifier", (rs.getStorageBin() != null && rs.getStorageBin()
          .getWarehouse() != null) ? rs.getStorageBin().getWarehouse().getIdentifier() : "");
      myMap.put("attributeSetValue", rs.getAttributeSetValue() != null ? rs.getAttributeSetValue()
          .getId() : null);
      myMap.put("attributeSetValue$_identifier", rs.getAttributeSetValue() != null ? rs
          .getAttributeSetValue().getIdentifier() : "");
      myMap.put("purchaseOrderLine", rs.getSalesOrderLine() == null ? null : rs.getSalesOrderLine()
          .getId());
      myMap.put("purchaseOrderLine$_identifier", rs.getSalesOrderLine() == null ? "" : rs
          .getSalesOrderLine().getIdentifier());
      myMap.put(
          "availableQty",
          getQtyOnHand(reservation.getProduct(), rs.getStorageBin() != null ? rs.getStorageBin()
              : null, rs.getAttributeSetValue() != null ? rs.getAttributeSetValue() : null, rs
              .getSalesOrderLine() != null ? rs.getSalesOrderLine() : null));
      myMap.put(
          "reservedinothers",
          rs.getSalesOrderLine() != null ? getQtyReserved(reservation, rs.getSalesOrderLine())
              : getQtyReserved(reservation, reservation.getProduct(), rs.getAttributeSetValue(),
                  rs.getStorageBin()));
      myMap.put("quantity", rs.getQuantity());
      myMap.put("reservationQuantity", reservation.getQuantity());
      myMap.put("released", rs.getReleased());
      myMap.put("allocated", rs.isAllocated());
      result.add(myMap);
    }
    return result;
  }

  private List<Map<String, Object>> getPurchaseOrderLines(Reservation reservation,
      Set<String> organizations, List<Warehouse> warehousesFiltered,
      List<AttributeSetInstance> attributeSetInstancesFiltered, List<OrderLine> orderLinesFiltered,
      String availableQtyFilterCriteria, String reservedinothersFilterCriteria,
      String releasedFilterCriteria, ArrayList<String> selectedIds) {
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    final StringBuilder hqlString = new StringBuilder();
    hqlString.append("select ol from OrderLine as ol ");
    hqlString.append("join  ol.salesOrder as o ");
    hqlString.append("where o.salesTransaction = false and o.documentStatus = 'CO' ");
    hqlString.append("and ol.product = :product ");
    // Organization filter not required as on hand warehouses is sufficent
    // hqlString.append("and ol.organization.id in :organizations ");
    hqlString.append("and o.warehouse in :warehouses ");
    hqlString.append("and not exists ( ");

    hqlString.append("select 1 from MaterialMgmtReservationStock as rs ");
    hqlString.append("where rs.reservation = :reservation ");
    hqlString.append("and rs.salesOrderLine = ol ");

    hqlString.append(") ");

    if (reservation.getAttributeSetValue() != null) {
      hqlString.append("and ol.attributeSetValue = :attributeSetValue ");
    }
    if (attributeSetInstancesFiltered != null) {
      hqlString.append("and ol.attributeSetValue in :attributeSetInstancesFiltered ");
    }
    if (reservation.getWarehouse() != null) {
      hqlString.append("and o.warehouse = :warehouse ");
    }
    if (warehousesFiltered != null) {
      hqlString.append("and o.warehouse in :warehousesFiltered ");
    }
    if (orderLinesFiltered != null) {
      hqlString.append("and ol in :orderLinesFiltered ");
    }
    hqlString
        .append("and ol.orderedQuantity <> coalesce((select Sum(mpo.quantity) from ProcurementPOInvoiceMatch as mpo where mpo.salesOrderLine = ol and mpo.goodsShipmentLine is not null),0)");
    hqlString.append("order by o.documentNo, ol.lineNo");
    final Session session = OBDal.getInstance().getSession();
    Query query = session.createQuery(hqlString.toString());
    query.setParameter("product", reservation.getProduct());
    query.setParameter("reservation", reservation);
    // query.setParameterList("organizations", organizations);
    query.setParameterList("warehouses", getOnHandWarehouses(reservation.getOrganization()));
    if (reservation.getAttributeSetValue() != null) {
      query.setParameter("attributeSetValue", reservation.getAttributeSetValue());
    }
    if (attributeSetInstancesFiltered != null) {
      query.setParameterList("attributeSetInstancesFiltered", attributeSetInstancesFiltered);
    }
    if (reservation.getWarehouse() != null) {
      query.setParameter("warehouse", reservation.getWarehouse());
    }
    if (warehousesFiltered != null) {
      query.setParameterList("warehousesFiltered", warehousesFiltered);
    }
    if (orderLinesFiltered != null) {
      query.setParameterList("orderLinesFiltered", orderLinesFiltered);
    }
    for (Object o : query.list()) {
      Map<String, Object> myMap = new HashMap<String, Object>();
      StorageDetail sd = (StorageDetail) o;
      if (selectedIds.size() > 0) {
        for (int i = 0; i < selectedIds.size(); i++) {
          if (!sd.getId().equals(selectedIds.get(i))) {

            // Check Filter Criterias
            if (availableQtyFilterCriteria != null && !"".equals(availableQtyFilterCriteria)
                && !isInScope("availableQty", availableQtyFilterCriteria, sd.getQuantityOnHand())) {
              continue;
            }
            BigDecimal reservedinothers = getQtyReserved(reservation, reservation.getProduct(),
                sd.getAttributeSetValue(), sd.getStorageBin());

            if (reservedinothersFilterCriteria != null
                && !"".equals(reservedinothersFilterCriteria)
                && !isInScope("reservedinothers", reservedinothersFilterCriteria, reservedinothers)) {
              continue;
            }
            if (releasedFilterCriteria != null && !"".equals(releasedFilterCriteria)
                && !isInScope("released", releasedFilterCriteria, BigDecimal.ZERO)) {
              continue;
            }
            result = tomap(sd, false, result, reservedinothers, reservation);
          }
        }
      } else {

        OrderLine orderLine = (OrderLine) o;
        myMap.put("id", orderLine.getId());
        myMap.put("obSelected", false);
        myMap.put("reservationStock", null);
        myMap.put("reservationStock$_identifier", "");
        myMap.put("storageBin$_identifier", "");
        myMap.put("storageBin", "");
        myMap.put("warehouse", (orderLine.getSalesOrder().getWarehouse() != null) ? orderLine
            .getSalesOrder().getWarehouse().getId() : null);
        myMap.put("warehouse$_identifier",
            (orderLine.getSalesOrder().getWarehouse() != null) ? orderLine.getSalesOrder()
                .getWarehouse().getIdentifier() : "");
        myMap.put("attributeSetValue", orderLine.getAttributeSetValue() != null ? orderLine
            .getAttributeSetValue().getId() : null);
        myMap.put("attributeSetValue$_identifier",
            orderLine.getAttributeSetValue() != null ? orderLine.getAttributeSetValue()
                .getIdentifier() : "");
        myMap.put("purchaseOrderLine", orderLine.getId());
        myMap.put("purchaseOrderLine$_identifier", orderLine.getIdentifier());
        // Check Filter Criterias

        if (availableQtyFilterCriteria != null
            && !"".equals(availableQtyFilterCriteria)
            && !isInScope("availableQty", availableQtyFilterCriteria,
                orderLine.getOrderedQuantity())) {
          continue;
        }
        BigDecimal reservedinothers = getQtyReserved(reservation, orderLine);
        if (reservedinothersFilterCriteria != null && !"".equals(reservedinothersFilterCriteria)
            && !isInScope("reservedinothers", reservedinothersFilterCriteria, reservedinothers)) {
          continue;
        }
        if (releasedFilterCriteria != null && !"".equals(releasedFilterCriteria)
            && !isInScope("released", releasedFilterCriteria, BigDecimal.ZERO)) {
          continue;
        }
        myMap.put("availableQty",
            orderLine.getOrderedQuantity().subtract(getDeliveredQuantity(orderLine)));
        myMap.put("reservedinothers", reservedinothers);
        myMap.put("quantity", BigDecimal.ZERO);
        myMap.put("reservationQuantity", reservation.getQuantity());
        myMap.put("released", BigDecimal.ZERO);
        myMap.put("allocated", false);
        result.add(myMap);
      }
    }

    return result;
  }

  private List<Map<String, Object>> getStorageDetail(Reservation reservation,
      Set<String> organizations, List<Warehouse> warehousesFiltered,
      List<Locator> locatorsFiltered, List<AttributeSetInstance> attributeSetInstancesFiltered,
      String availableQtyFilterCriteria, String reservedinothersFilterCriteria,
      String releasedFilterCriteria, ArrayList<String> selectedIds) {
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    final StringBuilder hqlString = new StringBuilder();
    hqlString.append("select sd from MaterialMgmtStorageDetail as sd ");
    hqlString.append("join sd.storageBin as sb ");
    hqlString.append("where sd.quantityOnHand > 0 and sd.orderUOM is null ");
    hqlString.append("and sd.product = :product ");
    hqlString.append("and sd.uOM = :uom ");
    hqlString.append("and not exists ( ");

    hqlString.append("select 1 from MaterialMgmtReservationStock as rs ");
    hqlString.append("where rs.reservation = :reservation ");
    hqlString
        .append("and (rs.attributeSetValue = sd.attributeSetValue or rs.attributeSetValue is null) ");
    hqlString.append("and rs.storageBin = sd.storageBin ");

    hqlString.append(") ");

    if (reservation.getAttributeSetValue() != null) {
      hqlString.append("and sd.attributeSetValue = :attributeSetValue ");
    }
    if (attributeSetInstancesFiltered != null) {
      hqlString.append("and sd.attributeSetValue in :attributeSetInstancesFiltered ");
    }
    if (reservation.getStorageBin() != null) {
      hqlString.append("and sd.storageBin = :storageBin ");
    }
    if (locatorsFiltered != null) {
      hqlString.append("and sd.storageBin in :locatorsFiltered ");
    }
    if (reservation.getWarehouse() != null) {
      hqlString.append("and sb.warehouse = :warehouse ");
    }
    if (warehousesFiltered != null) {
      hqlString.append("and sb.warehouse in :warehousesFiltered ");
    }

    // Organization Filter not required as reservation for warehouse on hand is sufficent
    // hqlString.append("and sd.organization.id in :organizations ");
    hqlString.append("and sb.warehouse in :warehouses ");
    hqlString.append("order by sb.warehouse, sd.storageBin.rowX, sd.storageBin.stackY, ");
    hqlString.append("sd.storageBin.levelZ, sd.attributeSetValue.description");

    final Session session = OBDal.getInstance().getSession();
    Query query = session.createQuery(hqlString.toString());
    query.setParameter("product", reservation.getProduct());
    query.setParameter("uom", reservation.getUOM());
    query.setParameter("reservation", reservation);

    if (reservation.getAttributeSetValue() != null) {
      query.setParameter("attributeSetValue", reservation.getAttributeSetValue());
    }
    if (attributeSetInstancesFiltered != null) {
      query.setParameterList("attributeSetInstancesFiltered", attributeSetInstancesFiltered);
    }
    if (reservation.getStorageBin() != null) {
      query.setParameter("storageBin", reservation.getStorageBin());
    }
    if (locatorsFiltered != null) {
      query.setParameterList("locatorsFiltered", locatorsFiltered);
    }
    if (reservation.getWarehouse() != null) {
      query.setParameter("warehouse", reservation.getWarehouse());
    }
    if (warehousesFiltered != null) {
      query.setParameterList("warehousesFiltered", warehousesFiltered);
    }

    // query.setParameterList("organizations", organizations);
    query.setParameterList("warehouses", getOnHandWarehouses(reservation.getOrganization()));
    for (int i = 0; i < selectedIds.size(); i++) {
      StorageDetail sd = OBDal.getInstance().get(StorageDetail.class, selectedIds.get(i));

      BigDecimal reservedinothers = getQtyReserved(reservation, reservation.getProduct(),
          sd.getAttributeSetValue(), sd.getStorageBin());

      result = tomap(sd, true, result, reservedinothers, reservation);
    }

    for (Object o : query.list()) {
      Map<String, Object> myMap = new HashMap<String, Object>();
      StorageDetail sd = (StorageDetail) o;
      if (selectedIds.size() > 0) {
        for (int i = 0; i < selectedIds.size(); i++) {
          if (!(sd.getId().equals(selectedIds.get(i)))) {
            // Check Filter Criterias
            if (availableQtyFilterCriteria != null && !"".equals(availableQtyFilterCriteria)
                && !isInScope("availableQty", availableQtyFilterCriteria, sd.getQuantityOnHand())) {
              continue;
            }
            BigDecimal reservedinothers = getQtyReserved(reservation, reservation.getProduct(),
                sd.getAttributeSetValue(), sd.getStorageBin());

            if (reservedinothersFilterCriteria != null
                && !"".equals(reservedinothersFilterCriteria)
                && !isInScope("reservedinothers", reservedinothersFilterCriteria, reservedinothers)) {
              continue;
            }
            if (releasedFilterCriteria != null && !"".equals(releasedFilterCriteria)
                && !isInScope("released", releasedFilterCriteria, BigDecimal.ZERO)) {
              continue;
            }
            result = tomap(sd, false, result, reservedinothers, reservation);
          }
        }
      } else {
        // Check Filter Criterias
        if (availableQtyFilterCriteria != null && !"".equals(availableQtyFilterCriteria)
            && !isInScope("availableQty", availableQtyFilterCriteria, sd.getQuantityOnHand())) {
          continue;
        }
        BigDecimal reservedinothers = getQtyReserved(reservation, reservation.getProduct(),
            sd.getAttributeSetValue(), sd.getStorageBin());

        if (reservedinothersFilterCriteria != null && !"".equals(reservedinothersFilterCriteria)
            && !isInScope("reservedinothers", reservedinothersFilterCriteria, reservedinothers)) {
          continue;
        }
        if (releasedFilterCriteria != null && !"".equals(releasedFilterCriteria)
            && !isInScope("released", releasedFilterCriteria, BigDecimal.ZERO)) {
          continue;
        }
        result = tomap(sd, false, result, reservedinothers, reservation);
      }
    }
    return result;
  }

  private List<Map<String, Object>> tomap(StorageDetail sd, boolean obSelected,
      List<Map<String, Object>> result, BigDecimal reservedinothers, Reservation reservation) {
    Map<String, Object> myMap = new HashMap<String, Object>();
    myMap.put("id", sd.getId());
    myMap.put("obSelected", obSelected);
    myMap.put("reservationStock", null);
    myMap.put("reservationStock$_identifier", "");
    myMap.put("storageBin", sd.getStorageBin() != null ? sd.getStorageBin().getId() : "");
    myMap.put("storageBin$_identifier", sd.getStorageBin() != null ? sd.getStorageBin()
        .getIdentifier() : "");
    myMap.put("warehouse", (sd.getStorageBin().getWarehouse() != null) ? sd.getStorageBin()
        .getWarehouse().getId() : null);
    myMap.put("warehouse$_identifier", (sd.getStorageBin().getWarehouse() != null) ? sd
        .getStorageBin().getWarehouse().getIdentifier() : "");
    myMap.put("attributeSetValue", sd.getAttributeSetValue() != null ? sd.getAttributeSetValue()
        .getId() : null);
    myMap.put("attributeSetValue$_identifier", sd.getAttributeSetValue() != null ? sd
        .getAttributeSetValue().getIdentifier() : "");
    myMap.put("purchaseOrderLine", null);
    myMap.put("purchaseOrderLine$_identifier", "");

    myMap.put("availableQty", sd.getQuantityOnHand());
    myMap.put("reservedinothers", reservedinothers);
    myMap.put("reservationQuantity", reservation.getQuantity());
    myMap.put("quantity", BigDecimal.ZERO);
    myMap.put("released", BigDecimal.ZERO);
    myMap.put("allocated", false);
    result.add(myMap);

    return result;

  }

  private boolean isInScope(String fieldName, String filterCriteria, BigDecimal amount) {
    if (amount == null) {
      return false;
    }
    try {
      if (filterCriteria.startsWith("[")) {
        JSONArray myJSON = new JSONArray(filterCriteria);
        if (myJSON.getJSONObject(0).getString("fieldName").equals(fieldName)) {
          return isInScope(fieldName, myJSON.getJSONObject(0).toString(), amount);
        }
      } else if (filterCriteria.startsWith("{")) {
        JSONObject myJSON = new JSONObject(filterCriteria);
        if (myJSON.getString("fieldName").equals(fieldName)) {
          if (myJSON.getString("operator").equals("equals")) {
            return amount.compareTo(new BigDecimal(myJSON.getDouble("value"))) == 0;
          } else if (myJSON.getString("operator").equals("greaterThan")) {
            return amount.compareTo(new BigDecimal(myJSON.getDouble("value"))) > 0;
          } else if (myJSON.getString("operator").equals("lessThan")) {
            return amount.compareTo(new BigDecimal(myJSON.getDouble("value"))) < 0;
          } else if (myJSON.getString("operator").equals("greaterOrEqual")) {
            return amount.compareTo(new BigDecimal(myJSON.getDouble("value"))) >= 0;
          } else if (myJSON.getString("operator").equals("lessOrEqual")) {
            return amount.compareTo(new BigDecimal(myJSON.getDouble("value"))) <= 0;
          }
        }

      } else {
        try {
          return amount.compareTo(new BigDecimal(filterCriteria)) == 0;
        } catch (NumberFormatException e) {
        }
      }
    } catch (JSONException e) {
      log4j.error("Error parsing criteria", e);
    }
    return true;
  }

  private BigDecimal getQtyOnHand(Product product, Locator storageBin,
      AttributeSetInstance attribute, OrderLine orderline) {
    if (orderline != null && !"".equals(orderline)) {
      return getQtyOnHandFromOrderLine(orderline);
    }
    final StringBuilder hqlString = new StringBuilder();
    hqlString.append("select sum(sd.quantityOnHand) from MaterialMgmtStorageDetail sd ");
    hqlString.append(" where sd.product = :product ");
    if (storageBin != null) {
      hqlString.append(" and sd.storageBin = :storageBin ");
    }
    if (attribute != null) {
      hqlString.append(" and sd.attributeSetValue = :attributeSetValue ");
    }
    final Session session = OBDal.getInstance().getSession();
    Query query = session.createQuery(hqlString.toString());
    query.setParameter("product", product);
    if (storageBin != null) {
      query.setParameter("storageBin", storageBin);
    }
    if (attribute != null) {
      query.setParameter("attributeSetValue", attribute);
    }
    return (BigDecimal) query.uniqueResult();
  }

  private BigDecimal getQtyOnHandFromOrderLine(OrderLine orderline) {
    // return orderline.getOrderedQuantity().subtract(getQtyReserved(orderline));
    return orderline.getOrderedQuantity();
  }

  private BigDecimal getDeliveredQuantity(OrderLine orderline) {
    BigDecimal result = BigDecimal.ZERO;
    for (POInvoiceMatch match : orderline.getProcurementPOInvoiceMatchList()) {
      if (match.getGoodsShipmentLine() != null
          && "CO".equals(match.getGoodsShipmentLine().getShipmentReceipt().getDocumentStatus())) {
        result = result.add(match.getGoodsShipmentLine().getMovementQuantity());
      }
    }
    return result;
  }

  private BigDecimal getQtyReserved(Reservation reservation, OrderLine orderLine) {
    final StringBuilder hqlString = new StringBuilder();
    hqlString
        .append("select coalesce(sum(rs.quantity - coalesce(rs.released,0)),0) from MaterialMgmtReservationStock rs ");
    hqlString.append("join rs.reservation as r ");
    hqlString.append(" where r.rESStatus not in ('CL', 'DR') ");
    hqlString.append(" and rs.salesOrderLine = :orderLine ");
    hqlString.append(" and r <> :reservation ");
    final Session session = OBDal.getInstance().getSession();
    Query query = session.createQuery(hqlString.toString());
    query.setParameter("orderLine", orderLine);
    query.setParameter("reservation", reservation);
    return (BigDecimal) query.uniqueResult();
  }

  private BigDecimal getQtyReserved(Reservation reservation, StorageDetail sd) {
    final StringBuilder hqlString = new StringBuilder();
    hqlString
        .append("select coalesce(sum(rs.quantity - coalesce(rs.released,0)),0) from MaterialMgmtReservationStock rs ");
    hqlString.append("join rs.reservation as r ");
    hqlString.append(" where r.rESStatus not in ('CL', 'DR') ");
    hqlString.append(" and rs.storageDetail = :storageDetail ");
    hqlString.append(" and r <> :reservation ");
    final Session session = OBDal.getInstance().getSession();
    Query query = session.createQuery(hqlString.toString());
    query.setParameter("storageDetail", sd);
    query.setParameter("reservation", reservation);
    return (BigDecimal) query.uniqueResult();
  }

  private BigDecimal getQtyReserved(Reservation reservation, Product product,
      AttributeSetInstance attribute, Locator storageBin) {
    final StringBuilder hqlString = new StringBuilder();
    hqlString
        .append("select coalesce(sum(rs.quantity - coalesce(rs.released,0)),0) from MaterialMgmtReservationStock rs ");
    hqlString.append("join rs.reservation as r ");
    hqlString.append(" where r.rESStatus not in ('CL', 'DR') ");
    // hqlString.append(" and rs.salesOrderLine is null ");
    hqlString.append(" and r.product = :product ");
    hqlString.append(" and r <> :reservation ");
    if (attribute != null && !"0".equals(attribute.getId())) {
      hqlString.append(" and rs.attributeSetValue = :attributeSetValue ");
    }
    if (storageBin != null && !"0".equals(storageBin.getId())) {
      hqlString.append(" and rs.storageBin = :storageBin ");
    }
    final Session session = OBDal.getInstance().getSession();
    Query query = session.createQuery(hqlString.toString());
    query.setParameter("product", product);
    query.setParameter("reservation", reservation);
    if (attribute != null && !"0".equals(attribute.getId())) {
      query.setParameter("attributeSetValue", attribute);
    }
    if (storageBin != null) {
      query.setParameter("storageBin", storageBin);
    }
    return (BigDecimal) query.uniqueResult();
  }

  @Override
  protected int getCount(Map<String, String> parameters) {
    // TODO Auto-generated method stub
    return 0;
  }
}