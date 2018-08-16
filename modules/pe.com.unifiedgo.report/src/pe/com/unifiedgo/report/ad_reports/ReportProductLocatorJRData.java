//Sqlc generated V1.O00-1
package pe.com.unifiedgo.report.ad_reports;

import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.data.UtilSql;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.database.SessionInfo;
import org.openbravo.service.db.QueryTimeOutUtil;

class ReportProductLocatorJRData implements FieldProvider {
  static Logger log4j = Logger.getLogger(ReportProductLocatorJRData.class);
  private String InitRecordNumber = "0";
  public String orgid;
  public String productid;
  public String searchkey;
  public String productname;
  public String stockonhand;
  public String stockonhandavailable;
  public String locatorid;
  public String locatorvalue;
  public String locatorx;
  public String locatory;
  public String locatorz;
  public String locatorqty;

  public String padre;
  public String id;
  public String name;

  public String rownum;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  @Override
  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("orgid"))
      return orgid;
    else if (fieldName.equalsIgnoreCase("productid"))
      return productid;
    else if (fieldName.equalsIgnoreCase("searchkey"))
      return searchkey;
    else if (fieldName.equalsIgnoreCase("productname"))
      return productname;
    else if (fieldName.equalsIgnoreCase("stockonhand"))
      return stockonhand;
    else if (fieldName.equalsIgnoreCase("stockonhandavailable"))
      return stockonhandavailable;
    else if (fieldName.equalsIgnoreCase("locatorid"))
      return locatorid;
    else if (fieldName.equalsIgnoreCase("locatorvalue"))
      return locatorvalue;
    else if (fieldName.equalsIgnoreCase("locatorx"))
      return locatorx;
    else if (fieldName.equalsIgnoreCase("locatory"))
      return locatory;
    else if (fieldName.equalsIgnoreCase("locatorz"))
      return locatorz;
    else if (fieldName.equalsIgnoreCase("locatorqty"))
      return locatorqty;
    else if (fieldName.equalsIgnoreCase("padre"))
      return padre;
    else if (fieldName.equalsIgnoreCase("id"))
      return id;
    else if (fieldName.equalsIgnoreCase("name"))
      return name;
    else if (fieldName.equals("rownum"))
      return rownum;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }
  
  public static String selectMWarehouse(ConnectionProvider connectionProvider, String mWarehouseId) throws ServletException {
	    String strSql = "";
	    strSql = strSql + "      SELECT M_WAREHOUSE.VALUE || ' - ' || M_WAREHOUSE.NAME AS NAME" + "      FROM M_WAREHOUSE" + "      WHERE M_WAREHOUSE.M_WAREHOUSE_ID = ?";

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
	      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
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

  public static ReportProductLocatorJRData[] select(String adOrgId, String adClientId,
      String strmWarehouseId, String strSearchKey) throws ServletException {
    return select(adOrgId, adClientId, strmWarehouseId, strSearchKey, 0, 0);
  }

  public static ReportProductLocatorJRData[] select(String adOrgId, String adClientId,
      String strmWarehouseId, String strSearchKey, int firstRegister, int numberRegisters)
      throws ServletException {

    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);

    long countRecord = 0;

    List<ProductData> products = ReportProductLocatorJRData.getProductPriceWarehouseData(adOrgId,
        adClientId, strmWarehouseId, strSearchKey);
    for (int i = 0; i < products.size(); i++) {
      for (String key : products.get(i).locator.keySet()) {
        countRecord++;

        ReportProductLocatorJRData objectReportProductLocatorJRData = new ReportProductLocatorJRData();

        objectReportProductLocatorJRData.orgid = adOrgId;
        objectReportProductLocatorJRData.productid = products.get(i).productid;
        objectReportProductLocatorJRData.searchkey = products.get(i).searckkey;
        objectReportProductLocatorJRData.productname = products.get(i).productname;
        objectReportProductLocatorJRData.stockonhand = products.get(i).stockonhand;
        objectReportProductLocatorJRData.stockonhandavailable = products.get(i).stockonhandavailable;

        String[] locatordata = products.get(i).locator.get(key).split(":");
        objectReportProductLocatorJRData.locatorid = key;
        objectReportProductLocatorJRData.locatorvalue = locatordata[0];
        objectReportProductLocatorJRData.locatorx = locatordata[1];
        objectReportProductLocatorJRData.locatory = locatordata[2];
        objectReportProductLocatorJRData.locatorz = locatordata[3];
        objectReportProductLocatorJRData.locatorqty = locatordata[4];

        objectReportProductLocatorJRData.rownum = Long.toString(countRecord);
        objectReportProductLocatorJRData.InitRecordNumber = Integer.toString(firstRegister);

        vector.addElement(objectReportProductLocatorJRData);
      }
    }

    ReportProductLocatorJRData objectReportProductLocatorJRData[] = new ReportProductLocatorJRData[vector
        .size()];
    vector.copyInto(objectReportProductLocatorJRData);

    return (objectReportProductLocatorJRData);
  }

  public static List<ProductData> getProductPriceWarehouseData(String adOrgId, String adClientId,
      String strmWarehouseId, String strSearchKey) throws ServletException {

    DecimalFormat df = new DecimalFormat("#0.###");
    df.setRoundingMode(RoundingMode.HALF_UP);

    List<ProductData> productDataList = new ArrayList<ProductData>();

    String strSql = "";
    strSql = "SELECT p.m_product_id, p.value, p.name, ppwv.qty_onhand, ppwv.qty_available, "
        + "          l.m_locator_id,l.value, l.x, l.y, l.z, sd.qtyonhand "
        + "     FROM sre_product_price_warehouse_v ppwv "
        + "          INNER JOIN m_product p ON p.m_product_id=ppwv.m_product_id "
        + "          INNER JOIN m_warehouse w ON w.m_warehouse_id=ppwv.m_warehouse_id "
        + "          INNER JOIN m_locator l ON l.m_warehouse_id=w.m_warehouse_id "
        + "          INNER JOIN m_storage_detail sd ON sd.m_locator_id=l.m_locator_id AND sd.m_product_id=p.m_product_id "
        + "    WHERE p.ad_client_id='" + adClientId + "' "
        + "      AND AD_ISORGINCLUDED(p.ad_org_id, '" + adOrgId + "', '" + adClientId + "') > -1 "
        + "      AND w.m_warehouse_id='" + strmWarehouseId + "' " + "      AND p.value = '"
        + strSearchKey + "' " + "      AND sd.isActive='Y' " + "      AND sd.qtyonhand>0 "
        + "      AND AD_ISORGINCLUDED(ppwv.orgwarehouse, '" + adOrgId + "', '" + adClientId
        + "') > -1 ";

    try {
      Query sqlQuery = OBDal.getInstance().getSession().createSQLQuery(strSql);

      List<Object> data = sqlQuery.list();
      for (int k = 0; k < data.size(); k++) {
        Object[] obj = (Object[]) data.get(k);

        String productid = (String) obj[0];

        int pos = ProductData.hasProduct(productDataList, productid);
        if (pos != -1) {
          if (ProductData.hasLocator(productDataList.get(pos).locator, (String) obj[5]) == -1) {
            productDataList.get(pos).locator.put((String) obj[5],
                (String) obj[6] + ":" + (String) obj[7] + ":" + (String) obj[8] + ":"
                    + (String) obj[9] + ":" + df.format(obj[10]));
          }
        } else {
          ProductData productData = new ProductData();
          productData.productid = (String) obj[0];
          productData.searckkey = (String) obj[1];
          productData.productname = (String) obj[2];
          productData.stockonhand = df.format(obj[3]);
          productData.stockonhandavailable = df.format(obj[4]);
          productData.locator.put((String) obj[5], (String) obj[6] + ":" + (String) obj[7] + ":"
              + (String) obj[8] + ":" + (String) obj[9] + ":" + df.format(obj[10]));

          productDataList.add(productData);
        }

      }
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    }

    return productDataList;
  }

  public static ReportProductLocatorJRData[] set() throws ServletException {
    ReportProductLocatorJRData objectReportProductLocatorJRData[] = new ReportProductLocatorJRData[1];
    objectReportProductLocatorJRData[0] = new ReportProductLocatorJRData();
    objectReportProductLocatorJRData[0].productid = "";
    objectReportProductLocatorJRData[0].searchkey = "";
    objectReportProductLocatorJRData[0].productname = "";
    objectReportProductLocatorJRData[0].stockonhand = "";
    objectReportProductLocatorJRData[0].stockonhandavailable = "";
    objectReportProductLocatorJRData[0].locatorid = "";
    objectReportProductLocatorJRData[0].locatorvalue = "";
    objectReportProductLocatorJRData[0].locatorx = "";
    objectReportProductLocatorJRData[0].locatory = "";
    objectReportProductLocatorJRData[0].locatorz = "";
    objectReportProductLocatorJRData[0].locatorqty = "";
    objectReportProductLocatorJRData[0].padre = "";
    objectReportProductLocatorJRData[0].id = "";
    objectReportProductLocatorJRData[0].name = "";

    return objectReportProductLocatorJRData;
  }

  public static ReportProductLocatorJRData[] selectMWarehouseForParentOrgDouble(
      ConnectionProvider connectionProvider, String adUserClient) throws ServletException {
    return selectMWarehouseForParentOrgDouble(connectionProvider, adUserClient, 0, 0);
  }

  public static ReportProductLocatorJRData[] selectMWarehouseForParentOrgDouble(
      ConnectionProvider connectionProvider, String adUserClient, int firstRegister,
      int numberRegisters) throws ServletException {
    String strSql = "";
    strSql = strSql
        + "       SELECT M_WAREHOUSE.AD_ORG_ID AS PADRE, M_WAREHOUSE.M_WAREHOUSE_ID AS ID, TO_CHAR(M_WAREHOUSE.NAME) AS NAME"
        + "         FROM M_WAREHOUSE " + "        WHERE 1=1"
        + "          AND M_WAREHOUSE.ISACTIVE='Y'" + "          AND M_WAREHOUSE.AD_Client_ID IN(";
    strSql = strSql + ((adUserClient == null || adUserClient.equals("")) ? "" : adUserClient);
    strSql = strSql
        + ")"
        + "       UNION "
        + "       SELECT null AS PADRE, M_WAREHOUSE.M_WAREHOUSE_ID AS ID, TO_CHAR(M_WAREHOUSE.NAME) AS NAME"
        + "         FROM M_WAREHOUSE " + "        WHERE 2=2 "
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
        ReportProductLocatorJRData objectReportProductLocatorJRData = new ReportProductLocatorJRData();
        objectReportProductLocatorJRData.padre = UtilSql.getValue(result, "padre");
        objectReportProductLocatorJRData.id = UtilSql.getValue(result, "id");
        objectReportProductLocatorJRData.name = UtilSql.getValue(result, "name");
        objectReportProductLocatorJRData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectReportProductLocatorJRData);
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
    ReportProductLocatorJRData objectReportProductLocatorJRData[] = new ReportProductLocatorJRData[vector
        .size()];
    vector.copyInto(objectReportProductLocatorJRData);
    return (objectReportProductLocatorJRData);
  }

}

class ProductData {
  public String productid;
  public String searckkey;
  public String productname;
  public String stockonhand;
  public String stockonhandavailable;
  public Map<String, String> locator;

  public ProductData() {
    productid = "";
    searckkey = "";
    productname = "";
    stockonhand = "";
    stockonhandavailable = "";
    locator = new HashMap<String, String>();
  }

  static public int hasProduct(List<ProductData> productDataList, String productid) {
    for (int i = 0; i < productDataList.size(); i++) {
      if (productDataList.get(i).productid.compareTo(productid) == 0)
        return i;
    }
    return -1;
  }

  static public int hasLocator(Map<String, String> locatorList, String locatorId) {
    if (locatorList.get(locatorId) != null)
      return 1;
    return -1;
  }
}
