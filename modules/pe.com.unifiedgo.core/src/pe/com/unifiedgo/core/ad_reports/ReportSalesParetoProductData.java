//Sqlc generated V1.O00-1
package pe.com.unifiedgo.core.ad_reports;

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

class ReportSalesParetoProductData implements FieldProvider {
  static Logger log4j = Logger.getLogger(ReportSalesParetoProductData.class);
  private String InitRecordNumber = "0";
  public String orgid;
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

  public static ReportSalesParetoProductData[] selectOrganizacion(
      ConnectionProvider connectionProvider, String adOrgId, String adClientId, String mProductId,
      String startDate, String endDate, String mWarehouseId) throws ServletException {

    return selectOrganizacion(connectionProvider, adOrgId, adClientId, mProductId, startDate,
        endDate, mWarehouseId, 0, 0);
  }

  public static ReportSalesParetoProductData[] selectOrganizacion(
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
        ReportSalesParetoProductData objectReportSalesParetoProductData = new ReportSalesParetoProductData();
        objectReportSalesParetoProductData.organizacion = UtilSql.getValue(result, "organizacion");
        objectReportSalesParetoProductData.ruc = UtilSql.getValue(result, "ruc");
        objectReportSalesParetoProductData.rownum = Long.toString(countRecord);
        objectReportSalesParetoProductData.InitRecordNumber = Integer.toString(firstRegister);

        vector.addElement(objectReportSalesParetoProductData);
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

    ReportSalesParetoProductData objectReportSalesParetoProductData[] = new ReportSalesParetoProductData[vector
        .size()];
    vector.copyInto(objectReportSalesParetoProductData);
    return (objectReportSalesParetoProductData);

  }

  public static ReportSalesParetoProductData[] selectAlmacen(ConnectionProvider connectionProvider,
      String adOrgId, String adClientId, String mProductId, String startDate, String endDate,
      String mWarehouseId) throws ServletException {

    return selectAlmacen(connectionProvider, adOrgId, adClientId, mProductId, startDate, endDate,
        mWarehouseId, 0, 0);
  }

  public static ReportSalesParetoProductData[] selectAlmacen(ConnectionProvider connectionProvider,
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
        ReportSalesParetoProductData objectReportSalesParetoProductData = new ReportSalesParetoProductData();
        objectReportSalesParetoProductData.almacen = UtilSql.getValue(result, "almacen");
        objectReportSalesParetoProductData.rownum = Long.toString(countRecord);
        objectReportSalesParetoProductData.InitRecordNumber = Integer.toString(firstRegister);

        vector.addElement(objectReportSalesParetoProductData);
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

    ReportSalesParetoProductData objectReportSalesParetoProductData[] = new ReportSalesParetoProductData[vector
        .size()];
    vector.copyInto(objectReportSalesParetoProductData);
    return (objectReportSalesParetoProductData);

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

  public static ReportSalesParetoProductData[] selectReportData(
      ConnectionProvider connectionProvider, String mWarehouseId, String adClientId,
      String language, String adOrgId, String docDate, String DateTo) throws ServletException {
    return selectReportData(connectionProvider, mWarehouseId, adClientId, language, adOrgId,
        docDate, DateTo, 0, 0);
  }

  public static ReportSalesParetoProductData[] selectReportData(
      ConnectionProvider connectionProvider, String mWarehouseId, String adClientId,
      String language, String adOrgId, String docDate, String DateTo, int firstRegister,
      int numberRegisters) throws ServletException {

    String strSql = "";
    strSql = strSql
        + "        SELECT t.m_product_id as productid, P.value AS searchkey, P.name AS name,"
        + "               ad_column_identifier('C_Uom', U.c_uom_id, ?) as unit,"
        + "               SUM(ABS(t.movementqty)) AS value "
        + "          FROM M_TRANSACTION T"
        + "          JOIN M_LOCATOR L ON T.M_LOCATOR_ID=L.M_LOCATOR_ID"
        + "          JOIN M_WAREHOUSE W ON L.M_WAREHOUSE_ID=W.M_WAREHOUSE_ID"
        + "          JOIN m_inoutline iol ON t.m_inoutline_id = iol.m_inoutline_id"
        + "          JOIN m_inout io ON iol.m_inout_id = io.m_inout_id"
        + "          JOIN scr_combo_item cmbi ON io.EM_Swa_Combo_Item_ID = cmbi.scr_combo_item_id"
        + "          JOIN M_PRODUCT P ON T.M_PRODUCT_ID=P.M_PRODUCT_ID"
        + "          JOIN C_UOM U ON P.C_UOM_ID=U.C_UOM_ID "
        + "        WHERE t.movementtype IN ('P-', 'swa_O-', 'C-') "
        + "        AND cmbi.value IN ('boletadeventa','facturadeventa','FacturacionCliente','consumomina','consignacion','CompraextraordinariaentreEmpresasOut','ReposiciondecompraentreEmpresasOut','facturadeservicio') "
        + "        AND t.isactive = 'Y' " + "        AND io.docstatus = 'CO'";
    strSql = strSql
        + ((mWarehouseId != null && !"".equals(mWarehouseId) ? "        AND L.M_WAREHOUSE_ID = '"
            + mWarehouseId + "'" : ""));
    strSql = strSql + "        AND t.movementdate between ? AND ? "
        + "        AND AD_ISORGINCLUDED(t.AD_ORG_ID, ?, ?) <> -1 "
        + "        GROUP BY t.m_product_id, P.value, P.name, U.c_uom_id"
        + "        ORDER BY value DESC";
    // System.out.println("PARETO VENTAS:" + strSql);
    ResultSet result;
    Vector<ReportSalesParetoProductData> vector = new Vector<ReportSalesParetoProductData>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, language);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, docDate);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, DateTo);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adOrgId);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adClientId);

      result = st.executeQuery();
      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while (countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }

      String organization = ReportSalesParetoProductData.getOrganizationName(connectionProvider,
          adOrgId);
      double totalWarehouseOutputs = 0;
      while (continueResult && result.next()) {
        countRecord++;
        ReportSalesParetoProductData objRptSalesParetoProductData = new ReportSalesParetoProductData();
        objRptSalesParetoProductData.orgid = organization;
        objRptSalesParetoProductData.id = UtilSql.getValue(result, "productid");
        objRptSalesParetoProductData.searchkey = UtilSql.getValue(result, "searchkey");
        objRptSalesParetoProductData.name = UtilSql.getValue(result, "name");
        objRptSalesParetoProductData.unit = UtilSql.getValue(result, "unit");
        objRptSalesParetoProductData.qty = UtilSql.getValue(result, "value");
        objRptSalesParetoProductData.cost = "";
        objRptSalesParetoProductData.value = UtilSql.getValue(result, "value");
        objRptSalesParetoProductData.padre = "";

        objRptSalesParetoProductData.rownum = Long.toString(countRecord);
        objRptSalesParetoProductData.InitRecordNumber = Integer.toString(firstRegister);

        totalWarehouseOutputs = totalWarehouseOutputs
            + Double.parseDouble(objRptSalesParetoProductData.value);
        vector.addElement(objRptSalesParetoProductData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }
      result.close();

      // Process to Classification of products on a,b,c category
      if (totalWarehouseOutputs == 0) {
        log4j.error("Zero Sum Warehouse Outputs");
        throw new ServletException("Zero Sum Warehouse Outputs");

      } else {
        double percentage = 0, acumulated = 0;
        double limitA = 80, limitB = 15;
        String isabc;
        for (int i = 0; i < vector.size(); i++) {
          percentage = (Double.parseDouble(vector.get(i).value) / totalWarehouseOutputs) * 100;
          acumulated = acumulated + percentage;
          // percentage = Math.round(percentage * 1000.0) / 1000.0;
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

    ReportSalesParetoProductData objRptSalesParetoProductData[] = new ReportSalesParetoProductData[vector
        .size()];
    vector.copyInto(objRptSalesParetoProductData);
    return (objRptSalesParetoProductData);
  }

  public static ReportSalesParetoProductData[] set() throws ServletException {
    ReportSalesParetoProductData objRptSalesParetoProductData[] = new ReportSalesParetoProductData[1];
    objRptSalesParetoProductData[0] = new ReportSalesParetoProductData();
    objRptSalesParetoProductData[0].orgid = "";
    objRptSalesParetoProductData[0].searchkey = "";
    objRptSalesParetoProductData[0].name = "";
    objRptSalesParetoProductData[0].unit = "";
    objRptSalesParetoProductData[0].qty = "";
    objRptSalesParetoProductData[0].cost = "";
    objRptSalesParetoProductData[0].value = "";
    objRptSalesParetoProductData[0].percentage = "";
    objRptSalesParetoProductData[0].percentageacum = "";
    objRptSalesParetoProductData[0].isabc = "";
    objRptSalesParetoProductData[0].padre = "";
    objRptSalesParetoProductData[0].id = "";
    return objRptSalesParetoProductData;
  }

  public static ReportSalesParetoProductData[] selectMWarehouseForChildOrgDouble(
      ConnectionProvider connectionProvider, String adUserClient) throws ServletException {
    return selectMWarehouseForChildOrgDouble(connectionProvider, adUserClient, 0, 0);
  }

  public static ReportSalesParetoProductData[] selectMWarehouseForChildOrgDouble(
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
        ReportSalesParetoProductData objRptSalesParetoProductData = new ReportSalesParetoProductData();
        objRptSalesParetoProductData.padre = UtilSql.getValue(result, "padre");
        objRptSalesParetoProductData.id = UtilSql.getValue(result, "id");
        objRptSalesParetoProductData.name = UtilSql.getValue(result, "name");
        objRptSalesParetoProductData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objRptSalesParetoProductData);
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
    ReportSalesParetoProductData objRptSalesParetoProductData[] = new ReportSalesParetoProductData[vector
        .size()];
    vector.copyInto(objRptSalesParetoProductData);
    return (objRptSalesParetoProductData);
  }

  public static ReportSalesParetoProductData[] salespareto_select_bk(
      ConnectionProvider connectionProvider, String mWarehouseId, String adClientId,
      String language, String adOrgId) throws ServletException {
    return sales_pareto_select_bk(connectionProvider, mWarehouseId, adClientId, language, adOrgId,
        0, 0);
  }

  public static ReportSalesParetoProductData[] sales_pareto_select_bk(
      ConnectionProvider connectionProvider, String mWarehouseId, String adClientId,
      String language, String adOrgId, int firstRegister, int numberRegisters)
      throws ServletException {
    String strSql = "";
    strSql = strSql
        + "      select orgid, searchkey, name, unit, qty, '0.00' as cost, '0.00' as value, percentage,"
        + "      scr_get_sales_pareto_abc(?, ad_org_id, ?, percentage) as isabc, '' as padre, '' as id"
        + "      from ("
        + "        select ad_column_identifier('AD_Org', ad_org_id, ?) as orgid,"
        + "          value as searchkey, name as name,"
        + "          ad_column_identifier('C_Uom', c_uom_id, ?) as unit,"
        + "          ad_org_id, m_product_id, sum(movementqty) as qty,"
        + "          100 * sum(movementqty) / (select sum(movementqty) as totalqty"
        + "                             from ("
        + "                                  select sum(CASE WHEN T.MOVEMENTQTY<0 THEN -T.MOVEMENTQTY ELSE 0 END) as movementqty, w.m_warehouse_id"
        + "                                  from m_transaction t"
        + "                                    left join m_locator l on (t.m_locator_id=l.m_locator_id)"
        + "                                    left join m_warehouse w on (l.m_warehouse_id=w.m_warehouse_id)"
        + "                                    inner join m_inoutline iol on (t.m_inoutline_id = iol.m_inoutline_id)"
        + "                                    inner join m_inout io on (iol.m_inout_id = io.m_inout_id)"
        + "                                    inner join scr_combo_item cmbi on(io.EM_Swa_Combo_Item_ID = cmbi.scr_combo_item_id)"
        + "                                  where t.m_inoutline_id is not null  "
        + "                                    and cmbi.scr_combo_category_id = '38F1793A888C4E7784436C80F48F6169'               "
        + "                                    and t.ad_client_id = ?"
        + "                                    and 1=1";
    strSql = strSql
        + ((mWarehouseId == null || mWarehouseId.equals("")) ? "" : "  AND l.M_WAREHOUSE_ID = ?  ");
    strSql = strSql
        + "                                    and 2=2"
        + "                                    AND ad_isorgincluded(w.AD_ORG_ID, ?, w.ad_client_id) <> -1"
        + "                                  group by w.ad_org_id, w.ad_client_id, w.m_warehouse_id"
        + "                                ) a"
        + "                              where a.m_warehouse_id = warehouse    "
        + "                              having sum(a.movementqty) > 0                                                        "
        + "                            ) as percentage"
        + "        from ("
        + "          select w.ad_org_id, p.value, p.name, p.c_uom_id, sum(CASE WHEN T.MOVEMENTQTY<0 THEN -T.MOVEMENTQTY ELSE 0 END) as movementqty, t.m_product_id, w.m_warehouse_id as warehouse"
        + "          from m_transaction t, m_locator l, m_warehouse w, m_product p, m_inoutline iol, m_inout io, scr_combo_item cmbi"
        + "          where t.m_locator_id = l.m_locator_id"
        + "            and t.m_inoutline_id is not null"
        + "            and t.m_inoutline_id = iol.m_inoutline_id"
        + "            and iol.m_inout_id = io.m_inout_id"
        + "            and io.EM_Swa_Combo_Item_ID = cmbi.scr_combo_item_id"
        + "            and cmbi.scr_combo_category_id = '38F1793A888C4E7784436C80F48F6169'"
        + "            and l.m_warehouse_id = w.m_warehouse_id"
        + "            and t.m_product_id = p.m_product_id" + "            and t.ad_client_id=?"
        + "            and w.ad_client_id=t.ad_client_id" + "            and 3=3";
    strSql = strSql
        + ((mWarehouseId == null || mWarehouseId.equals("")) ? "" : "  AND l.M_WAREHOUSE_ID = ?  ");
    strSql = strSql
        + "            and 4=4"
        + "            AND ad_isorgincluded(w.AD_ORG_ID, ?, w.ad_client_id) <> -1"
        + "          group by w.ad_org_id, w.ad_client_id, t.m_product_id, p.name, p.value, p.c_uom_id, w.m_warehouse_id"
        + "          having sum(CASE WHEN T.MOVEMENTQTY<0 THEN -T.MOVEMENTQTY ELSE 0 END) > 0"
        + "        ) a"
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
      UtilSql.setValue(st, iParameter, 12, null, adClientId);
      if (mWarehouseId != null && !(mWarehouseId.equals(""))) {
        iParameter++;
        UtilSql.setValue(st, iParameter, 12, null, mWarehouseId);
      }
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adOrgId);
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
        ReportSalesParetoProductData objRptSalesParetoProductData = new ReportSalesParetoProductData();
        objRptSalesParetoProductData.orgid = UtilSql.getValue(result, "orgid");
        objRptSalesParetoProductData.searchkey = UtilSql.getValue(result, "searchkey");
        objRptSalesParetoProductData.name = UtilSql.getValue(result, "name");
        objRptSalesParetoProductData.unit = UtilSql.getValue(result, "unit");
        objRptSalesParetoProductData.qty = UtilSql.getValue(result, "qty");
        objRptSalesParetoProductData.cost = UtilSql.getValue(result, "cost");
        objRptSalesParetoProductData.value = UtilSql.getValue(result, "value");
        objRptSalesParetoProductData.percentage = UtilSql.getValue(result, "percentage");
        objRptSalesParetoProductData.isabc = UtilSql.getValue(result, "isabc");
        objRptSalesParetoProductData.padre = UtilSql.getValue(result, "padre");
        objRptSalesParetoProductData.id = UtilSql.getValue(result, "id");
        objRptSalesParetoProductData.rownum = Long.toString(countRecord);
        objRptSalesParetoProductData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objRptSalesParetoProductData);
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
    ReportSalesParetoProductData objRptSalesParetoProductData[] = new ReportSalesParetoProductData[vector
        .size()];
    vector.copyInto(objRptSalesParetoProductData);
    return (objRptSalesParetoProductData);
  }

  public static ReportSalesParetoProductData[] backupSelect(ConnectionProvider connectionProvider,
      String mWarehouseId, String adClientId, String language, String cCurrencyConv, String adOrgId)
      throws ServletException {
    return backupSelect(connectionProvider, mWarehouseId, adClientId, language, cCurrencyConv,
        adOrgId, 0, 0);
  }

  public static ReportSalesParetoProductData[] backupSelect(ConnectionProvider connectionProvider,
      String mWarehouseId, String adClientId, String language, String cCurrencyConv,
      String adOrgId, int firstRegister, int numberRegisters) throws ServletException {
    String strSql = "";
    strSql = strSql
        + "      select orgid, searchkey, name, unit, qty, cost, value, percentage,"
        + "      m_get_pareto_abc(?, ad_org_id, ?, percentage) as isabc, '' as padre, '' as id"
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
        ReportSalesParetoProductData objRptSalesParetoProductData = new ReportSalesParetoProductData();
        objRptSalesParetoProductData.orgid = UtilSql.getValue(result, "orgid");
        objRptSalesParetoProductData.searchkey = UtilSql.getValue(result, "searchkey");
        objRptSalesParetoProductData.name = UtilSql.getValue(result, "name");
        objRptSalesParetoProductData.unit = UtilSql.getValue(result, "unit");
        objRptSalesParetoProductData.qty = UtilSql.getValue(result, "qty");
        objRptSalesParetoProductData.cost = UtilSql.getValue(result, "cost");
        objRptSalesParetoProductData.value = UtilSql.getValue(result, "value");
        objRptSalesParetoProductData.percentage = UtilSql.getValue(result, "percentage");
        objRptSalesParetoProductData.isabc = UtilSql.getValue(result, "isabc");
        objRptSalesParetoProductData.padre = UtilSql.getValue(result, "padre");
        objRptSalesParetoProductData.id = UtilSql.getValue(result, "id");
        objRptSalesParetoProductData.rownum = Long.toString(countRecord);
        objRptSalesParetoProductData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objRptSalesParetoProductData);
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
    ReportSalesParetoProductData objRptSalesParetoProductData[] = new ReportSalesParetoProductData[vector
        .size()];
    vector.copyInto(objRptSalesParetoProductData);
    return (objRptSalesParetoProductData);
  }

  public static ReportSalesParetoProductData mUpdateParetoProduct0(
      ConnectionProvider connectionProvider, String adPinstanceId) throws ServletException {
    String strSql = "";
    strSql = strSql + "        CALL SCR_UPDATE_SALES_PARETO_PROD0(?)";

    ReportSalesParetoProductData objRptSalesParetoProductData = new ReportSalesParetoProductData();
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
    return (objRptSalesParetoProductData);
  }

  public static String selectMWarehouse(ConnectionProvider connectionProvider, String mWarehouseId)
      throws ServletException {
    String strSql = "";
    strSql = strSql + "      SELECT M_WAREHOUSE.VALUE || ' - ' || M_WAREHOUSE.NAME AS NAME"
        + "      FROM M_WAREHOUSE" + "      WHERE M_WAREHOUSE.M_WAREHOUSE_ID = ?";

    ResultSet result;
    String strReturn = "";
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, mWarehouseId);

      result = st.executeQuery();
      if (result.next()) {
        strReturn = UtilSql.getValue(result, "name");
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
    return (strReturn);
  }

}
