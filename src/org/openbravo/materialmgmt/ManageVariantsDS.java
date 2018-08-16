/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
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
 *************************************************************************
 */
package org.openbravo.materialmgmt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.common.plm.Characteristic;
import org.openbravo.model.common.plm.CharacteristicValue;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.plm.ProductCharacteristic;
import org.openbravo.model.common.plm.ProductCharacteristicConf;
import org.openbravo.model.common.plm.ProductCharacteristicValue;
import org.openbravo.service.datasource.ReadOnlyDataSourceService;
import org.openbravo.service.json.JsonUtils;

public class ManageVariantsDS extends ReadOnlyDataSourceService {
  private static final int searchKeyLength = getSearchKeyColumnLength();
  private List<String> selectedIds = new ArrayList<String>();
  private HashMap<String, List<CharacteristicValue>> selectedChValues = new HashMap<String, List<CharacteristicValue>>();
  private String nameFilter;
  private String searchKeyFilter;
  private Boolean variantCreated;

  @Override
  protected int getCount(Map<String, String> parameters) {
    return getData(parameters, 0, Integer.MAX_VALUE).size();
  }

  @Override
  protected List<Map<String, Object>> getData(Map<String, String> parameters, int startRow,
      int endRow) {
    OBContext.setAdminMode(true);
    final List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    try {
      readCriteria(parameters);
      final String strProductId = parameters.get("@Product.id@");
      final Product product = OBDal.getInstance().get(Product.class, strProductId);

      int totalMaxLength = product.getSearchKey().length();
      long variantNumber = 1;
      Map<ProductCharacteristic, ProductCharacteristicAux> prChUseCode = new HashMap<ProductCharacteristic, ProductCharacteristicAux>();

      OBCriteria<ProductCharacteristic> prChCrit = OBDal.getInstance().createCriteria(
          ProductCharacteristic.class);
      prChCrit.add(Restrictions.eq(ProductCharacteristic.PROPERTY_PRODUCT, product));
      prChCrit.add(Restrictions.eq(ProductCharacteristic.PROPERTY_VARIANT, true));
      prChCrit.addOrderBy(ProductCharacteristic.PROPERTY_SEQUENCENUMBER, true);
      List<ProductCharacteristic> prChs = prChCrit.list();
      int chNumber = prChs.size();
      if (chNumber == 0) {
        return result;
      }
      ProductCharacteristicConf[] currentValues = new ProductCharacteristicConf[chNumber];
      boolean includeInResult = true;

      int i = 0;
      for (ProductCharacteristic prCh : prChs) {
        OBCriteria<ProductCharacteristicConf> prChConfCrit = OBDal.getInstance().createCriteria(
            ProductCharacteristicConf.class);
        prChConfCrit.add(Restrictions.eq(
            ProductCharacteristicConf.PROPERTY_CHARACTERISTICOFPRODUCT, prCh));
        List<ProductCharacteristicConf> prChConfs = prChConfCrit.list();
        long valuesCount = prChConfs.size();

        boolean useCode = true;
        int maxLength = 0;
        for (ProductCharacteristicConf prChConf : prChConfs) {
          if (StringUtils.isBlank(prChConf.getCode())) {
            useCode = false;
            break;
          }
          if (prChConf.getCode().length() > maxLength) {
            maxLength = prChConf.getCode().length();
          }
        }

        variantNumber = variantNumber * valuesCount;
        if (useCode) {
          totalMaxLength += maxLength;
        }
        List<CharacteristicValue> filteredValues = selectedChValues.get(prCh.getCharacteristic()
            .getId());
        ProductCharacteristicAux prChAux = new ProductCharacteristicAux(useCode, prChConfs,
            filteredValues);
        currentValues[i] = prChAux.getNextValue();
        if (filteredValues != null) {
          includeInResult = includeInResult
              && filteredValues.contains(currentValues[i].getCharacteristicValue());
        }

        prChUseCode.put(prCh, prChAux);
        i++;
      }
      totalMaxLength += Long.toString(variantNumber).length();
      boolean useCodes = totalMaxLength <= searchKeyLength;

      boolean hasNext = true;
      int productNo = 0;
      do {
        // reset boolean value.
        includeInResult = true;
        // Create variant product
        Map<String, Object> variantMap = new HashMap<String, Object>();
        variantMap.put("Client", product.getClient());
        variantMap.put("Organization", product.getOrganization());
        variantMap.put("Active", "Y");
        variantMap.put("creationDate", new Date());
        variantMap.put("createdBy", OBContext.getOBContext().getUser());
        variantMap.put("updated", new Date());
        variantMap.put("updatedBy", OBContext.getOBContext().getUser());
        variantMap.put("name", product.getName());
        variantMap.put("variantCreated", false);
        variantMap.put("obSelected", false);

        String searchKey = product.getSearchKey();
        for (i = 0; i < chNumber; i++) {
          ProductCharacteristicConf prChConf = currentValues[i];
          ProductCharacteristicAux prChConfAux = prChUseCode.get(prChs.get(i));
          List<CharacteristicValue> filteredValues = prChConfAux.getFilteredValues();
          if (filteredValues != null) {
            includeInResult = includeInResult
                && filteredValues.contains(currentValues[i].getCharacteristicValue());
          }

          if (useCodes && prChConfAux.isUseCode()) {
            searchKey += prChConf.getCode();
          }
        }
        for (int j = 0; j < (Long.toString(variantNumber).length() - Integer.toString(productNo)
            .length()); j++) {
          searchKey += "0";
        }
        searchKey += productNo;
        variantMap.put("searchKey", searchKey);

        StringBuffer where = new StringBuffer();
        where.append(" as p ");
        where.append(" where p." + Product.PROPERTY_GENERICPRODUCT + " = :product");

        String strChDesc = "";
        String strKeyId = "";
        JSONArray valuesArray = new JSONArray();
        for (i = 0; i < chNumber; i++) {
          ProductCharacteristicConf prChConf = currentValues[i];
          Characteristic characteristic = prChConf.getCharacteristicOfProduct().getCharacteristic();
          where.append(buildExistsClause(i));
          if (StringUtils.isNotBlank(strChDesc)) {
            strChDesc += ", ";
          }
          strChDesc += characteristic.getName() + ":";
          strChDesc += " " + prChConf.getCharacteristicValue().getName();
          strKeyId += prChConf.getCharacteristicValue().getId();
          JSONObject value = new JSONObject();
          value.put("characteristic", characteristic.getId());
          value.put("characteristicValue", prChConf.getCharacteristicValue().getId());
          value.put("characteristicConf", prChConf.getId());
          valuesArray.put(value);
        }
        variantMap.put("characteristicArray", valuesArray);
        variantMap.put("characteristicDescription", strChDesc);
        variantMap.put("id", strKeyId);

        OBQuery<Product> variantQry = OBDal.getInstance().createQuery(Product.class,
            where.toString());
        variantQry.setNamedParameter("product", product);
        for (i = 0; i < chNumber; i++) {
          ProductCharacteristicConf prChConf = currentValues[i];
          Characteristic characteristic = prChConf.getCharacteristicOfProduct().getCharacteristic();
          variantQry.setNamedParameter("ch" + i, characteristic.getId());
          variantQry.setNamedParameter("chvalue" + i, prChConf.getCharacteristicValue().getId());
        }
        Product existingProduct = variantQry.uniqueResult();
        if (existingProduct != null) {
          variantMap.put("name", existingProduct.getName());
          variantMap.put("searchKey", existingProduct.getSearchKey());
          variantMap.put("variantCreated", true);
          variantMap.put("variantId", existingProduct.getId());
          variantMap.put("id", existingProduct.getId());
        }
        if (StringUtils.isNotEmpty(searchKeyFilter)) {
          includeInResult = includeInResult
              && StringUtils.contains((String) variantMap.get("searchKey"), searchKeyFilter);
        }
        if (StringUtils.isNotEmpty(nameFilter)) {
          includeInResult = includeInResult
              && StringUtils.contains((String) variantMap.get("name"), nameFilter);
        }
        if (variantCreated != null) {
          includeInResult = includeInResult && variantCreated == (existingProduct != null);
        }
        if (!selectedIds.isEmpty()) {
          includeInResult = includeInResult || selectedIds.contains(variantMap.get("id"));
        }

        if (includeInResult) {
          result.add(variantMap);
        }

        for (i = 0; i < chNumber; i++) {
          ProductCharacteristicAux prChConfAux = prChUseCode.get(prChs.get(i));
          currentValues[i] = prChConfAux.getNextValue();
          if (!prChConfAux.isIteratorReset()) {
            break;
          } else if (i + 1 == chNumber) {
            hasNext = false;
          }
        }
        productNo++;
      } while (hasNext);
      String strSortBy = parameters.get("_sortBy");
      if (strSortBy == null) {
        strSortBy = "characteristicDescription";
      }
      boolean ascending = true;
      if (strSortBy.startsWith("-")) {
        ascending = false;
        strSortBy = strSortBy.substring(1);
      }

      Collections.sort(result, new ResultComparator(strSortBy, ascending));

    } catch (JSONException e) {
      // Do nothing
    } finally {
      OBContext.restorePreviousMode();
    }
    return result;
  }

  private void readCriteria(Map<String, String> parameters) throws JSONException {
    JSONArray criteriaArray = (JSONArray) JsonUtils.buildCriteria(parameters).get("criteria");
    selectedIds = new ArrayList<String>();
    selectedChValues = new HashMap<String, List<CharacteristicValue>>();
    nameFilter = null;
    searchKeyFilter = null;
    variantCreated = null;

    for (int i = 0; i < criteriaArray.length(); i++) {
      JSONObject criteria = criteriaArray.getJSONObject(i);
      // Basic advanced criteria handling
      if (criteria.has("_constructor")
          && "AdvancedCriteria".equals(criteria.getString("_constructor"))
          && criteria.has("criteria")) {
        JSONArray innerCriteriaArray = new JSONArray(criteria.getString("criteria"));
        criteria = innerCriteriaArray.getJSONObject(0);
      }
      String fieldName = criteria.getString("fieldName");
      // String operatorName = criteria.getString("operator");
      String value = criteria.getString("value");
      if (fieldName.equals("name")) {
        nameFilter = value;
      } else if (fieldName.equals("searchKey")) {
        searchKeyFilter = value;
      } else if (fieldName.equals("id")) {
        selectedIds.add(value);
      } else if (fieldName.equals("variantCreated")) {
        variantCreated = criteria.getBoolean("value");
      } else if (fieldName.equals("characteristicDescription")) {
        JSONArray values = new JSONArray(value);
        // All values belong to the same characteristicId, get the first one.
        String strCharacteristicId = null;
        List<CharacteristicValue> chValueIds = new ArrayList<CharacteristicValue>();
        for (int j = 0; j < values.length(); j++) {
          CharacteristicValue chValue = OBDal.getInstance().get(CharacteristicValue.class,
              values.getString(j));
          chValueIds.add(chValue);
          if (strCharacteristicId == null) {
            strCharacteristicId = (String) DalUtil.getId(chValue.getCharacteristic());
          }
        }
        selectedChValues.put(strCharacteristicId, chValueIds);
      }

    }
  }

  private String buildExistsClause(int i) {
    StringBuffer clause = new StringBuffer();
    clause.append(" and exists (select 1 from " + ProductCharacteristicValue.ENTITY_NAME
        + " as pcv");
    clause.append("    where pcv." + ProductCharacteristicValue.PROPERTY_PRODUCT + " = p");
    clause.append("      and pcv." + ProductCharacteristicValue.PROPERTY_CHARACTERISTIC
        + ".id = :ch" + i);
    clause.append("      and pcv." + ProductCharacteristicValue.PROPERTY_CHARACTERISTICVALUE
        + ".id = :chvalue" + i);
    clause.append("     )");
    return clause.toString();
  }

  private static int getSearchKeyColumnLength() {
    final Entity prodEntity = ModelProvider.getInstance().getEntity(Product.ENTITY_NAME);

    final Property searchKeyProperty = prodEntity.getProperty(Product.PROPERTY_SEARCHKEY);
    return searchKeyProperty.getFieldLength();
  }

  private static class ResultComparator implements Comparator<Map<String, Object>> {
    private String sortByField;
    private boolean ascending;

    public ResultComparator(String _sortByField, boolean _ascending) {
      sortByField = _sortByField;
      ascending = _ascending;
    }

    @Override
    public int compare(Map<String, Object> map1, Map<String, Object> map2) {
      boolean sortByChanged = false;
      if ("variantCreated".equals(sortByField)) {
        Boolean o1 = (Boolean) map1.get(sortByField);
        Boolean o2 = (Boolean) map2.get(sortByField);
        if (o1 == o2) {
          sortByField = "characteristicDescription";
          sortByChanged = true;
        } else if (ascending) {
          return o1 ? -1 : 1;
        } else {
          return o2 ? -1 : 1;
        }
      }
      // previous if might have changed the value of sortByField
      if (!"variantCreated".equals(sortByField)) {
        String str1 = (String) map1.get(sortByField);
        String str2 = (String) map2.get(sortByField);
        if (sortByChanged) {
          sortByField = "variantCreated";
        }
        if (ascending) {
          return str1.compareTo(str2);
        } else {
          return str2.compareTo(str1);
        }
      }
      // returning 0 but should never reach this point.
      return 0;
    }
  }

  private class ProductCharacteristicAux {
    private boolean useCode;
    private boolean isIteratorReset;
    private List<ProductCharacteristicConf> values;
    private List<CharacteristicValue> filteredValues;
    private Iterator<ProductCharacteristicConf> iterator;

    ProductCharacteristicAux(boolean _useCode, List<ProductCharacteristicConf> _values,
        List<CharacteristicValue> _filteredValues) {
      useCode = _useCode;
      values = _values;
      filteredValues = _filteredValues;
    }

    public boolean isUseCode() {
      return useCode;
    }

    public boolean isIteratorReset() {
      return isIteratorReset;
    }

    public List<CharacteristicValue> getFilteredValues() {
      return filteredValues;
    }

    public ProductCharacteristicConf getNextValue() {
      ProductCharacteristicConf prChConf;
      if (iterator == null || !iterator.hasNext()) {
        iterator = values.iterator();
        isIteratorReset = true;
      } else {
        isIteratorReset = false;
      }
      prChConf = iterator.next();
      return prChConf;
    }
  }

}
