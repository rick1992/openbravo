package pe.com.unifiedgo.warehouse.erpReports;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.data.FieldProvider;
import org.openbravo.data.UtilSql;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.database.RDBMSIndependent;
import org.openbravo.database.SessionInfo;
import org.openbravo.exception.NoConnectionAvailableException;
import org.openbravo.exception.PoolNotFoundException;
import org.openbravo.service.db.QueryTimeOutUtil;

class ReportParetoWarehouseData implements FieldProvider {
  static Logger log4j = Logger.getLogger(ReportParetoWarehouseData.class);
  private String InitRecordNumber = "0";
  public String orgid;
  public String warehouse;
  public String searchkey;
  public String name;
  public String unit;
  public String qty;
  public String cost;
  public String value;
  public String percentage;
  public String percentageacum;
  public String isabc;
  public String padre;
  public String id;

  public String organizacion;
  public String ruc;

  public String almacen;

  public String rownum;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("orgid"))
      return orgid;
    if (fieldName.equalsIgnoreCase("warehouse"))
      return warehouse;
    else if (fieldName.equalsIgnoreCase("searchkey"))
      return searchkey;
    else if (fieldName.equalsIgnoreCase("name"))
      return name;
    else if (fieldName.equalsIgnoreCase("unit"))
      return unit;
    else if (fieldName.equalsIgnoreCase("qty"))
      return qty;
    else if (fieldName.equalsIgnoreCase("cost"))
      return cost;
    else if (fieldName.equalsIgnoreCase("value"))
      return value;
    else if (fieldName.equalsIgnoreCase("percentage"))
      return percentage;
    else if (fieldName.equalsIgnoreCase("percentageacum"))
      return percentageacum;
    else if (fieldName.equalsIgnoreCase("isabc"))
      return isabc;
    else if (fieldName.equalsIgnoreCase("padre"))
      return padre;
    else if (fieldName.equalsIgnoreCase("id"))
      return id;

    else if (fieldName.equalsIgnoreCase("organizacion"))
      return organizacion;
    else if (fieldName.equalsIgnoreCase("ruc"))
      return ruc;
    else if (fieldName.equalsIgnoreCase("almacen"))
      return almacen;

    else if (fieldName.equals("rownum"))
      return rownum;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  public static ReportParetoWarehouseData[] selectOrganizacion(
      ConnectionProvider connectionProvider, String adOrgId, String adClientId, String mProductId,
      String startDate, String endDate, String mWarehouseId) throws ServletException {

    return selectOrganizacion(connectionProvider, adOrgId, adClientId, mProductId, startDate,
        endDate, mWarehouseId, 0, 0);
  }

  public static ReportParetoWarehouseData[] selectOrganizacion(
      ConnectionProvider connectionProvider, String adOrgId, String adClientId, String mProductId,
      String startDate, String endDate, String mWarehouseId, int firstRegister, int numberRegisters)
      throws ServletException {
    String strSql = "";
    strSql = strSql + "select org.name as organizacion, inf.taxid as ruc from ad_org org "
        + "join ad_orginfo inf on org.ad_org_id = inf.ad_org_id where "
        + "org.ad_org_id = ? limit 1 ";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;

    try {
      st = connectionProvider.getPreparedStatement(strSql);

      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adOrgId);
      result = st.executeQuery();

      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while (countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }
      while (continueResult && result.next()) {
        countRecord++;
        ReportParetoWarehouseData objectReportParetoWarehouseData = new ReportParetoWarehouseData();
        objectReportParetoWarehouseData.organizacion = UtilSql.getValue(result, "organizacion");
        objectReportParetoWarehouseData.ruc = UtilSql.getValue(result, "ruc");
        objectReportParetoWarehouseData.rownum = Long.toString(countRecord);
        objectReportParetoWarehouseData.InitRecordNumber = Integer.toString(firstRegister);

        vector.addElement(objectReportParetoWarehouseData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }
      result.close();

    } catch (SQLException e) {
      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
          + e.getMessage());
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }

    ReportParetoWarehouseData objectReportParetoWarehouseData[] = new ReportParetoWarehouseData[vector
        .size()];
    vector.copyInto(objectReportParetoWarehouseData);
    return (objectReportParetoWarehouseData);

  }

  public static ReportParetoWarehouseData[] selectAlmacen(ConnectionProvider connectionProvider,
      String adOrgId, String adClientId, String mProductId, String startDate, String endDate,
      String mWarehouseId) throws ServletException {

    return selectAlmacen(connectionProvider, adOrgId, adClientId, mProductId, startDate, endDate,
        mWarehouseId, 0, 0);
  }

  public static ReportParetoWarehouseData[] selectAlmacen(ConnectionProvider connectionProvider,
      String adOrgId, String adClientId, String mProductId, String startDate, String endDate,
      String mWarehouseId, int firstRegister, int numberRegisters) throws ServletException {
    String strSql = "";
    strSql = strSql + "select (wh.value || ' - ' || wh.name) as almacen from m_warehouse wh "
        + "where wh.m_warehouse_id = ? limit 1";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;

    try {
      st = connectionProvider.getPreparedStatement(strSql);

      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, mWarehouseId);

      result = st.executeQuery();

      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while (countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }
      while (continueResult && result.next()) {
        countRecord++;
        ReportParetoWarehouseData objectReportParetoWarehouseData = new ReportParetoWarehouseData();
        objectReportParetoWarehouseData.almacen = UtilSql.getValue(result, "almacen");
        objectReportParetoWarehouseData.rownum = Long.toString(countRecord);
        objectReportParetoWarehouseData.InitRecordNumber = Integer.toString(firstRegister);

        vector.addElement(objectReportParetoWarehouseData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }
      result.close();

    } catch (SQLException e) {
      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
          + e.getMessage());
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }

    ReportParetoWarehouseData objectReportParetoWarehouseData[] = new ReportParetoWarehouseData[vector
        .size()];
    vector.copyInto(objectReportParetoWarehouseData);
    return (objectReportParetoWarehouseData);

  }

  public static ReportParetoWarehouseData[] select(ConnectionProvider connectionProvider,
      String mWarehouseId, String adClientId, String language, String cCurrencyConv, String adOrgId)
      throws ServletException {
    return select(connectionProvider, mWarehouseId, adClientId, language, cCurrencyConv, adOrgId,
        0, 0);
  }

  public static ReportParetoWarehouseData[] select(ConnectionProvider connectionProvider,
      String mWarehouseId, String adClientId, String language, String cCurrencyConv,
      String adOrgId, int firstRegister, int numberRegisters) throws ServletException {
    String strSql = "";
    strSql = strSql
        + "      select orgid, searchkey, name, unit, qty, cost, value, percentage,"
        + "      m_get_pareto_abc(?, ad_org_id, ?, percentage) as isabc, '' as padre, m_product_id as id"
        + "      from ("
        + "        select ad_column_identifier('AD_Org', ad_org_id, ?) as orgid,"
        + "          value as searchkey, name as name,"
        + "          ad_column_identifier('C_Uom', c_uom_id, ?) as unit,"
        + "          ad_org_id, m_product_id, sum(movementqty) as qty,"
        + "          sum(value_per_currency)/sum(movementqty) as cost, "
        + "          sum(value_per_currency) as value, "
        + "          100 * sum(value_per_currency) / (select sum(cost_per_currency)"
        + "                             from ("
        + "                                  select c_currency_convert_precision(sum(case when t.movementqty>=0 then tc.cost else -tc.cost end),"
        + "                                         tc.c_currency_id, ?, to_date(now()), null, ?, ad_get_org_le_bu (w.ad_org_id, 'LE')) as cost_per_currency,"
        + "                                         sum(t.movementqty) as movementqty, w.m_warehouse_id"
        + "                                  from m_transaction_cost tc, m_transaction t"
        + "                                    left join m_locator l on (t.m_locator_id=l.m_locator_id)"
        + "                                    left join m_warehouse w on (l.m_warehouse_id=w.m_warehouse_id)"
        + "                                  where tc.m_transaction_id = t.m_transaction_id"
        + "                                    and t.iscostcalculated = 'Y'"
        + "                                    and t.transactioncost is not null"
        + "                                    and t.ad_client_id = ?"
        + "                                    and 1=1";
    strSql = strSql
        + ((mWarehouseId == null || mWarehouseId.equals("")) ? "" : "  AND l.M_WAREHOUSE_ID = ?  ");
    strSql = strSql
        + "                                    and 2=2"
        + "                                    AND ad_isorgincluded(w.AD_ORG_ID, ?, w.ad_client_id) <> -1"
        + "                                  group by tc.c_currency_id, w.ad_org_id, w.ad_client_id, w.m_warehouse_id"
        + "                                ) a"
        + "                              where a.m_warehouse_id = warehouse"
        + "                              having sum(a.movementqty)>0"
        + "                            ) as percentage"
        + "        from ("
        + "          select w.ad_org_id, p.value, p.name, p.c_uom_id, sum(t.movementqty) as movementqty, p.m_product_id, w.m_warehouse_id as warehouse,"
        + "                 c_currency_convert_precision(sum(case when t.movementqty>=0 then tc.cost else -tc.cost end),"
        + "                 tc.c_currency_id, ?, to_date(now()), null, ?, ad_get_org_le_bu (w.ad_org_id, 'LE')) as value_per_currency"
        + "          from m_transaction_cost tc, m_transaction t, m_locator l, m_warehouse w, m_product p"
        + "          where tc.m_transaction_id = t.m_transaction_id"
        + "            and t.m_locator_id = l.m_locator_id"
        + "            and l.m_warehouse_id = w.m_warehouse_id"
        + "            and t.m_product_id = p.m_product_id"
        + "            and t.iscostcalculated = 'Y'"
        + "            and t.transactioncost is not null" + "            and t.ad_client_id=?"
        + "            and w.ad_client_id=t.ad_client_id" + "            and 3=3";
    strSql = strSql
        + ((mWarehouseId == null || mWarehouseId.equals("")) ? "" : "  AND l.M_WAREHOUSE_ID = ?  ");
    strSql = strSql
        + "            and 4=4"
        + "            AND ad_isorgincluded(w.AD_ORG_ID, ?, w.ad_client_id) <> -1"
        + "          group by w.ad_org_id, w.ad_client_id, p.m_product_id, tc.c_currency_id, p.name, p.value, p.c_uom_id, w.m_warehouse_id"
        + "          having sum(t.movementqty) > 0" + "        ) a"
        + "        group by ad_org_id, m_product_id, name, value, c_uom_id, warehouse"
        + "        order by orgid, percentage desc" + "      ) b";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, mWarehouseId);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adClientId);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, language);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, language);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, cCurrencyConv);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adClientId);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adClientId);
      if (mWarehouseId != null && !(mWarehouseId.equals(""))) {
        iParameter++;
        UtilSql.setValue(st, iParameter, 12, null, mWarehouseId);
      }
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adOrgId);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, cCurrencyConv);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adClientId);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adClientId);
      if (mWarehouseId != null && !(mWarehouseId.equals(""))) {
        iParameter++;
        UtilSql.setValue(st, iParameter, 12, null, mWarehouseId);
      }
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adOrgId);

      result = st.executeQuery();
      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while (countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }
      while (continueResult && result.next()) {
        countRecord++;
        ReportParetoWarehouseData objRptParetoWarehouseData = new ReportParetoWarehouseData();
        objRptParetoWarehouseData.orgid = UtilSql.getValue(result, "orgid");
        objRptParetoWarehouseData.searchkey = UtilSql.getValue(result, "searchkey");
        objRptParetoWarehouseData.name = UtilSql.getValue(result, "name");
        objRptParetoWarehouseData.unit = UtilSql.getValue(result, "unit");
        objRptParetoWarehouseData.qty = UtilSql.getValue(result, "qty");
        objRptParetoWarehouseData.cost = UtilSql.getValue(result, "cost");
        objRptParetoWarehouseData.value = UtilSql.getValue(result, "value");
        objRptParetoWarehouseData.percentage = UtilSql.getValue(result, "percentage");
        objRptParetoWarehouseData.isabc = UtilSql.getValue(result, "isabc");
        objRptParetoWarehouseData.padre = UtilSql.getValue(result, "padre");
        objRptParetoWarehouseData.id = UtilSql.getValue(result, "id");
        objRptParetoWarehouseData.rownum = Long.toString(countRecord);
        objRptParetoWarehouseData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objRptParetoWarehouseData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }
      result.close();
    } catch (SQLException e) {
      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
          + e.getMessage());
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }
    ReportParetoWarehouseData objRptParetoWarehouseData[] = new ReportParetoWarehouseData[vector
        .size()];
    vector.copyInto(objRptParetoWarehouseData);
    return (objRptParetoWarehouseData);
  }

  public static ReportParetoWarehouseData[] set() throws ServletException {
    ReportParetoWarehouseData objRptParetoWarehouseData[] = new ReportParetoWarehouseData[1];
    objRptParetoWarehouseData[0] = new ReportParetoWarehouseData();
    objRptParetoWarehouseData[0].orgid = "";
    objRptParetoWarehouseData[0].warehouse = "";
    objRptParetoWarehouseData[0].searchkey = "";
    objRptParetoWarehouseData[0].name = "";
    objRptParetoWarehouseData[0].unit = "";
    objRptParetoWarehouseData[0].qty = "";
    objRptParetoWarehouseData[0].cost = "";
    objRptParetoWarehouseData[0].value = "";
    objRptParetoWarehouseData[0].percentage = "";
    objRptParetoWarehouseData[0].percentageacum = "";
    objRptParetoWarehouseData[0].isabc = "";
    objRptParetoWarehouseData[0].padre = "";
    objRptParetoWarehouseData[0].id = "";
    return objRptParetoWarehouseData;
  }

  public static ReportParetoWarehouseData[] selectMWarehouseForChildOrgDouble(
      ConnectionProvider connectionProvider, String adUserClient) throws ServletException {
    return selectMWarehouseForChildOrgDouble(connectionProvider, adUserClient, 0, 0);
  }

  public static ReportParetoWarehouseData[] selectMWarehouseForChildOrgDouble(
      ConnectionProvider connectionProvider, String adUserClient, int firstRegister,
      int numberRegisters) throws ServletException {
    String strSql = "";
    strSql = strSql
        + "       SELECT AD_ORG_WAREHOUSE.AD_ORG_ID AS PADRE, M_WAREHOUSE.M_WAREHOUSE_ID AS ID, TO_CHAR(value) || TO_CHAR(' - ') || TO_CHAR(M_WAREHOUSE.NAME) AS NAME"
        + "         FROM M_WAREHOUSE, AD_ORG_WAREHOUSE "
        + "        WHERE 1=1"
        + "          AND AD_ORG_WAREHOUSE.M_WAREHOUSE_ID = M_WAREHOUSE.M_WAREHOUSE_ID AND AD_ORG_WAREHOUSE.ISACTIVE='Y' "
        + "          AND M_WAREHOUSE.ISACTIVE='Y'" + "          AND M_WAREHOUSE.AD_Client_ID IN(";
    strSql = strSql + ((adUserClient == null || adUserClient.equals("")) ? "" : adUserClient);
    strSql = strSql
        + ")"
        + "       UNION "
        + "       SELECT null AS PADRE, M_WAREHOUSE.M_WAREHOUSE_ID AS ID, TO_CHAR(M_WAREHOUSE.NAME) AS NAME"
        + "         FROM M_WAREHOUSE, AD_ORG_WAREHOUSE "
        + "        WHERE 2=2 "
        + "         AND AD_ORG_WAREHOUSE.M_WAREHOUSE_ID = M_WAREHOUSE.M_WAREHOUSE_ID AND AD_ORG_WAREHOUSE.ISACTIVE='Y' "
        + "         AND M_WAREHOUSE.ISACTIVE='Y'" + "          AND M_WAREHOUSE.AD_Client_ID IN(";
    strSql = strSql + ((adUserClient == null || adUserClient.equals("")) ? "" : adUserClient);
    strSql = strSql + ")    " + "        ORDER BY PADRE, NAME";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      if (adUserClient != null && !(adUserClient.equals(""))) {
      }
      if (adUserClient != null && !(adUserClient.equals(""))) {
      }

      result = st.executeQuery();
      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while (countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }
      while (continueResult && result.next()) {
        countRecord++;
        ReportParetoWarehouseData objRptParetoWarehouseData = new ReportParetoWarehouseData();
        objRptParetoWarehouseData.padre = UtilSql.getValue(result, "padre");
        objRptParetoWarehouseData.id = UtilSql.getValue(result, "id");
        objRptParetoWarehouseData.name = UtilSql.getValue(result, "name");
        objRptParetoWarehouseData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objRptParetoWarehouseData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }
      result.close();
    } catch (SQLException e) {
      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
          + e.getMessage());
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }
    ReportParetoWarehouseData objRptParetoWarehouseData[] = new ReportParetoWarehouseData[vector
        .size()];
    vector.copyInto(objRptParetoWarehouseData);
    return (objRptParetoWarehouseData);
  }

  public static ReportParetoWarehouseData mUpdateParetoProduct0(
      ConnectionProvider connectionProvider, String adPinstanceId) throws ServletException {
    String strSql = "";
    strSql = strSql + "        CALL M_UPDATE_PARETO_PRODUCT0(?)";

    ReportParetoWarehouseData objRptParetoWarehouseData = new ReportParetoWarehouseData();
    CallableStatement st = null;
    if (connectionProvider.getRDBMS().equalsIgnoreCase("ORACLE")) {

      int iParameter = 0;
      try {
        st = connectionProvider.getCallableStatement(strSql);
        QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
        iParameter++;
        UtilSql.setValue(st, iParameter, 12, null, adPinstanceId);

        st.execute();
      } catch (SQLException e) {
        log4j.error("SQL error in query: " + strSql + "Exception:" + e);
        throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
            + e.getMessage());
      } catch (Exception ex) {
        log4j.error("Exception in query: " + strSql + "Exception:" + ex);
        throw new ServletException("@CODE=@" + ex.getMessage());
      } finally {
        try {
          connectionProvider.releasePreparedStatement(st);
        } catch (Exception ignore) {
          ignore.printStackTrace();
        }
      }
    } else {
      Vector<String> parametersData = new Vector<String>();
      Vector<String> parametersTypes = new Vector<String>();
      parametersData.addElement(adPinstanceId);
      parametersTypes.addElement("in");
      try {
        RDBMSIndependent.getCallableResult(null, connectionProvider, strSql, parametersData,
            parametersTypes, 0);
      } catch (SQLException e) {
        log4j.error("SQL error in query: " + strSql + "Exception:" + e);
        throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
            + e.getMessage());
      } catch (NoConnectionAvailableException ec) {
        log4j.error("Connection error in query: " + strSql + "Exception:" + ec);
        throw new ServletException("@CODE=NoConnectionAvailable");
      } catch (PoolNotFoundException ep) {
        log4j.error("Pool error in query: " + strSql + "Exception:" + ep);
        throw new ServletException("@CODE=NoConnectionAvailable");
      } catch (Exception ex) {
        log4j.error("Exception in query: " + strSql + "Exception:" + ex);
        throw new ServletException("@CODE=@" + ex.getMessage());
      }
    }
    return (objRptParetoWarehouseData);
  }

  public static String getWarehouseName(ConnectionProvider connectionProvider, String mWarehouseId)
      throws ServletException {
    String strSql = "";
    strSql = strSql + "        SELECT W.value || ' - ' || W.name AS name"
        + "        FROM M_WAREHOUSE W" + "        WHERE W.M_WAREHOUSE_ID = ?";

    ResultSet result;
    PreparedStatement st = null;
    String warehouse = "";
    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, mWarehouseId);

      result = st.executeQuery();
      boolean continueResult = true;
      while (continueResult & result.next()) {
        warehouse = UtilSql.getValue(result, "name");
        continueResult = false; // only one value is expected to receive from sql consult
      }
      result.close();

      if (warehouse == "") {
        log4j.error("No Warehouse selected or it not exists");
        throw new ServletException("No Warehouse selected or it not exists");
      }

    } catch (SQLException e) {
      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
          + e.getMessage());
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }
    return warehouse;
  }

  public static String getOrganizationName(ConnectionProvider connectionProvider, String adOrgId)
      throws ServletException {
    String strSql = "";
    strSql = strSql + "        SELECT O.name AS name" + "        FROM AD_ORG O"
        + "        WHERE O.AD_ORG_ID = ?";

    ResultSet result;
    PreparedStatement st = null;
    String organization = "";
    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adOrgId);

      result = st.executeQuery();
      boolean continueResult = true;
      while (continueResult & result.next()) {
        organization = UtilSql.getValue(result, "name");
        continueResult = false; // only one value is expected to receive from sql consult
      }
      result.close();

      if (organization == "") {
        log4j.error("No Organization selected or it not exists");
        throw new ServletException("No Organization selected or it not exists");
      }

    } catch (SQLException e) {
      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
          + e.getMessage());
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }
    return organization;
  }

  public static double getTotalWarehouseOutputs(ConnectionProvider connectionProvider,
      String mWarehouseId, String adClientId, String adOrgId) throws ServletException {
    String strSql = "";
    strSql = strSql
        + "        SELECT SUM(T.MOVEMENTQTY)*-1 AS totalOutputs"
        + "        FROM M_TRANSACTION T"
        + "         LEFT JOIN M_LOCATOR L ON T.M_LOCATOR_ID=L.M_LOCATOR_ID"
        + "         LEFT JOIN M_WAREHOUSE W ON L.M_WAREHOUSE_ID=W.M_WAREHOUSE_ID,"
        + "         (SELECT A.AD_ORG_ID,A.AD_CLIENT_ID"
        + "          FROM AD_ORG A WHERE ad_isorgincluded(A.AD_ORG_ID, ?, A.AD_CLIENT_ID) <> -1) AUX"
        + "        WHERE T.MOVEMENTTYPE='C-'" + "        AND L.M_WAREHOUSE_ID = ?"
        + "        AND W.AD_ORG_ID=AUX.AD_ORG_ID" + "        AND (? IS NULL OR T.AD_CLIENT_ID = ?)"
        + "        HAVING SUM(T.MOVEMENTQTY)*-1 > 0";

    ResultSet result;
    PreparedStatement st = null;
    double totalWarehouseOutputs = 0;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adOrgId);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, mWarehouseId);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adClientId);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adClientId);

      result = st.executeQuery();
      String output = "0";
      boolean continueResult = true;
      while (continueResult & result.next()) {
        output = UtilSql.getValue(result, "totalOutputs");
        continueResult = false; // only one value is expected to receive from sql consult
      }
      totalWarehouseOutputs = Double.parseDouble(output);

      result.close();
    } catch (SQLException e) {
      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
          + e.getMessage());
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }
    return totalWarehouseOutputs;
  }

  public static int updateInitialABC(ConnectionProvider connectionProvider, String adClientId,
      String adOrgId) throws ServletException {

    String strSql = "";
    strSql = strSql + "        UPDATE M_PRODUCT" + "        SET em_scr_abc = 'C'"
        + "        WHERE ad_isorgincluded(AD_ORG_ID, ? , ?) <> -1" + "          AND isactive='Y'"
        + "        AND (em_scr_abc IS NULL OR LOWER(em_scr_abc) <> 'c')";

    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adOrgId);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adClientId);
      updateCount = st.executeUpdate();
    } catch (SQLException e) {
      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
          + e.getMessage());
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }
    return (updateCount);
  }

  public static ReportParetoWarehouseData[] selectReportData(ConnectionProvider connectionProvider,
      String mWarehouseId, String adClientId, String language, String cCurrencyConv,
      String adOrgId, String docDate, String DateTo) throws ServletException {
    return selectReportData(connectionProvider, mWarehouseId, adClientId, language, cCurrencyConv,
        adOrgId, docDate, DateTo, 0, 0);
  }

  public static ReportParetoWarehouseData[] selectReportData(ConnectionProvider connectionProvider,
      String mWarehouseId, String adClientId, String language, String cCurrencyConv,
      String adOrgId, String docDate, String DateTo, int firstRegister, int numberRegisters)
      throws ServletException {

    String strSql = "";
    strSql = strSql
        + "        SELECT 0.0 as percentage, 0.0 as percentageacum, to_char('') as isabc, "
        + " T.m_product_id as productid, P.value AS searchkey, P.name AS name,"
        + "               ad_column_identifier('C_Uom', U.c_uom_id, '"
        + language
        + "') as unit,"
        + "               SUM(CASE WHEN (T.movementtype='swa_O-' OR T.movementtype='C-' OR T.movementtype='V-' OR "
        //+ "                              T.movementtype='M-' OR T.movementtype='P-' OR T.movementtype='D-' OR "
        + "                              T.movementtype='P-' OR T.movementtype='D-' OR "
        + "                              T.movementtype='swa_Z-' OR T.movementtype='swa_R-' OR T.movementtype='ssa_S-'"
        + "                             ) THEN T.movementqty ELSE 0 END)*-1 AS value "
        + "          FROM M_TRANSACTION T"
        + "          LEFT JOIN M_LOCATOR L ON T.M_LOCATOR_ID=L.M_LOCATOR_ID"
        + "          LEFT JOIN M_WAREHOUSE W ON L.M_WAREHOUSE_ID=W.M_WAREHOUSE_ID"
        + "          INNER JOIN M_PRODUCT P ON T.M_PRODUCT_ID=P.M_PRODUCT_ID"
        + "          INNER JOIN C_UOM U ON P.C_UOM_ID=U.C_UOM_ID,"
        + "          (SELECT A.AD_ORG_ID,A.AD_CLIENT_ID"
        + "             FROM AD_ORG A WHERE ad_isorgincluded(A.AD_ORG_ID, '"
        + adOrgId
        + "' , A.AD_CLIENT_ID) <> -1) AUX"
        + "        WHERE L.M_WAREHOUSE_ID = '"
        + mWarehouseId
        + "'"
        + "        AND W.AD_ORG_ID=AUX.AD_ORG_ID"
        + "        AND T.movementdate between ? AND ? " // Add by Vafaster Pareto between dates
        + "        AND ('"
        + adClientId
        + "' IS NULL OR T.AD_CLIENT_ID = '"
        + adClientId
        + "')"
        + "        GROUP BY T.m_product_id, P.value, P.name, U.c_uom_id"
        + "        HAVING "
        + "         SUM(CASE WHEN (T.movementtype='swa_O-' OR T.movementtype='C-' OR T.movementtype='V-' OR"
        //+ "         T.movementtype='M-' OR T.movementtype='P-' OR T.movementtype='D-' OR  "
        + "         T.movementtype='P-' OR T.movementtype='D-' OR  "
        + "         T.movementtype='swa_Z-' OR T.movementtype='swa_R-' OR T.movementtype='ssa_S-') THEN T.movementqty ELSE 0 END)*-1 > 0  "
        + "        ORDER BY value DESC";

    ResultSet result;
    Vector<ReportParetoWarehouseData> vector = new Vector<ReportParetoWarehouseData>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());

      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, docDate);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, DateTo);
      // System.out.println(st);
      result = st.executeQuery();
      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while (countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }

      String organization = ReportParetoWarehouseData.getOrganizationName(connectionProvider,
          adOrgId);
      String warehouse = ReportParetoWarehouseData.getWarehouseName(connectionProvider,
          mWarehouseId);
      double totalWarehouseOutputs = 0;
      while (continueResult && result.next()) {
        countRecord++;
        ReportParetoWarehouseData objRptParetoWarehouseData = new ReportParetoWarehouseData();
        objRptParetoWarehouseData.orgid = organization;
        objRptParetoWarehouseData.warehouse = warehouse;
        objRptParetoWarehouseData.id = UtilSql.getValue(result, "productid");
        objRptParetoWarehouseData.searchkey = UtilSql.getValue(result, "searchkey");
        objRptParetoWarehouseData.name = UtilSql.getValue(result, "name");
        objRptParetoWarehouseData.unit = UtilSql.getValue(result, "unit");
        objRptParetoWarehouseData.qty = "";
        objRptParetoWarehouseData.cost = "";
        objRptParetoWarehouseData.value = UtilSql.getValue(result, "value");
        objRptParetoWarehouseData.padre = "";

        objRptParetoWarehouseData.percentage = UtilSql.getValue(result, "percentage");
        objRptParetoWarehouseData.percentageacum = UtilSql.getValue(result, "percentageacum");
        objRptParetoWarehouseData.isabc = UtilSql.getValue(result, "isabc");

        objRptParetoWarehouseData.rownum = Long.toString(countRecord);
        objRptParetoWarehouseData.InitRecordNumber = Integer.toString(firstRegister);

        totalWarehouseOutputs = totalWarehouseOutputs
            + Double.parseDouble(objRptParetoWarehouseData.value);
        vector.addElement(objRptParetoWarehouseData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }
      result.close();

      // Process to Classification of products on a,b,c category
      if (totalWarehouseOutputs == 0) {
        vector.clear();
        // log4j.error("Zero Sum Warehouse Outputs");
        // throw new ServletException("Zero Sum Warehouse Outputs");

      } else {
        double percentage = 0, acumulated = 0;
        double limitA = 80, limitB = 15;
        String isabc;

        for (int i = 0; i < vector.size(); i++) {
          percentage = (Double.parseDouble(vector.get(i).value) / totalWarehouseOutputs) * 100;
          acumulated = acumulated + percentage;

          if ((acumulated <= limitA))// || (acumulated <= limitA))
            isabc = "A";
          else if ((acumulated <= (limitA + limitB)))// || (acumulated <= (limitA + limitB)))
            isabc = "B";
          else
            isabc = "C";

          vector.get(i).percentage = Double.toString(percentage);
          vector.get(i).percentageacum = Double.toString(acumulated);
          vector.get(i).isabc = isabc;
        }
      }
    } catch (SQLException e) {
      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
          + e.getMessage());
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }

    ReportParetoWarehouseData objRptParetoWarehouseData[] = new ReportParetoWarehouseData[vector
        .size()];
    vector.copyInto(objRptParetoWarehouseData);
    return (objRptParetoWarehouseData);
  }

}
