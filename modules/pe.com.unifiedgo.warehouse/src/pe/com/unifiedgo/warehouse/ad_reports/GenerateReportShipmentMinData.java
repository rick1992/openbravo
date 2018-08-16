//Sqlc generated V1.O00-1
package pe.com.unifiedgo.warehouse.ad_reports;

import java.math.BigDecimal;
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

class GenerateReportShipmentMinData implements FieldProvider {
  static Logger log4j = Logger.getLogger(GenerateReportShipmentMinData.class);
  private String InitRecordNumber = "0";

  public String IDS;
  public String BPARTNERNAME;
  public String PRODUCTNAME;
  public String QTYDELIVERED;
  public String TRANSCURRENCYID;
  public String TRANSCLIENTID;
  public String TRANSORGID;
  public String NAME;

  public String TDELIVERED;
  public String DAYS;
  public String AVERAGE;
  public String ONHAND;
  public String REQUIRED;
  public String UNTXBOX;
  public String VALUEINT;
  public String NUMBOXES;
  
  public String STOCKACTUAL;
  public String WAREHOUSENAME;
  public String WAREHOUSESOURCENAME;

  public String orgid;
  public String searchkey;
  public String name;
  public String unit;
  public String qty;
  public String cost;
  public String value;
  public String percentage;
  public String isabc;
  public String padre;
  public String id;
  
  public String orgpadre;
  public String orgchildid;
  public String orgname;
  
  public String rownum;
  
  public String PRODUCTVALUE;

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
    else if (fieldName.equalsIgnoreCase("isabc"))
      return isabc;
    else if (fieldName.equalsIgnoreCase("padre"))
      return padre;
    else if (fieldName.equalsIgnoreCase("orgpadre"))
        return orgpadre;
    else if (fieldName.equalsIgnoreCase("orgchildid"))
        return orgchildid;
    else if (fieldName.equalsIgnoreCase("orgname"))
        return orgname;
    else if (fieldName.equalsIgnoreCase("id"))
      return id;
    else if (fieldName.equalsIgnoreCase("IDS"))
      return IDS;
    else if (fieldName.equalsIgnoreCase("BPARTNERNAME"))
      return BPARTNERNAME;
    else if (fieldName.equalsIgnoreCase("PRODUCTNAME"))
      return PRODUCTNAME;
    else if (fieldName.equalsIgnoreCase("QTYDELIVERED"))
      return QTYDELIVERED;
    else if (fieldName.equalsIgnoreCase("TRANSCURRENCYID"))
      return TRANSCURRENCYID;
    else if (fieldName.equalsIgnoreCase("TRANSCLIENTID"))
      return TRANSCLIENTID;
    else if (fieldName.equalsIgnoreCase("TRANSORGID"))
      return TRANSORGID;
    else if (fieldName.equalsIgnoreCase("NAME"))
      return NAME;
    else if (fieldName.equalsIgnoreCase("WAREHOUSENAME"))
        return WAREHOUSENAME;
    else if (fieldName.equalsIgnoreCase("WAREHOUSESOURCENAME"))
        return WAREHOUSESOURCENAME;
    //
    else if (fieldName.equalsIgnoreCase("TDELIVERED"))
      return TDELIVERED;
    else if (fieldName.equalsIgnoreCase("DAYS"))
      return DAYS;
    else if (fieldName.equalsIgnoreCase("AVERAGE"))
      return AVERAGE;
    else if (fieldName.equalsIgnoreCase("ONHAND"))
      return ONHAND;
    else if (fieldName.equalsIgnoreCase("REQUIRED"))
      return REQUIRED;
    else if (fieldName.equalsIgnoreCase("UNTXBOX"))
      return UNTXBOX;
    else if (fieldName.equalsIgnoreCase("VALUEINT"))
      return VALUEINT;
    else if (fieldName.equalsIgnoreCase("NUMBOXES"))
      return NUMBOXES;
    else if(fieldName.equalsIgnoreCase("PRODUCTVALUE"))
    	 return PRODUCTVALUE;
    else if(fieldName.equalsIgnoreCase("STOCKACTUAL"))
   	 return STOCKACTUAL;
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

  public static GenerateReportShipmentMinData[] select(ConnectionProvider connectionProvider,
      String mWarehouseId, String adClientId, String language, String cCurrencyConv, String adOrgId)
      throws ServletException {
    return select(connectionProvider, mWarehouseId, adClientId, language, cCurrencyConv, adOrgId,
        0, 0);
  }

  public static GenerateReportShipmentMinData[] select(ConnectionProvider connectionProvider,
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
        GenerateReportShipmentMinData objectGenerateReportShipmentMinData = new GenerateReportShipmentMinData();
        objectGenerateReportShipmentMinData.orgid = UtilSql.getValue(result, "orgid");
        objectGenerateReportShipmentMinData.searchkey = UtilSql.getValue(result, "searchkey");
        objectGenerateReportShipmentMinData.name = UtilSql.getValue(result, "name");
        objectGenerateReportShipmentMinData.unit = UtilSql.getValue(result, "unit");
        objectGenerateReportShipmentMinData.qty = UtilSql.getValue(result, "qty");
        objectGenerateReportShipmentMinData.cost = UtilSql.getValue(result, "cost");
        objectGenerateReportShipmentMinData.value = UtilSql.getValue(result, "value");
        objectGenerateReportShipmentMinData.percentage = UtilSql.getValue(result, "percentage");
        objectGenerateReportShipmentMinData.isabc = UtilSql.getValue(result, "isabc");
        objectGenerateReportShipmentMinData.padre = UtilSql.getValue(result, "padre");
        objectGenerateReportShipmentMinData.id = UtilSql.getValue(result, "id");
        objectGenerateReportShipmentMinData.rownum = Long.toString(countRecord);
        objectGenerateReportShipmentMinData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectGenerateReportShipmentMinData);
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
    GenerateReportShipmentMinData objectGenerateReportShipmentMinData[] = new GenerateReportShipmentMinData[vector
        .size()];
    vector.copyInto(objectGenerateReportShipmentMinData);
    return (objectGenerateReportShipmentMinData);
  }
  
  public static String getWarehouseName(ConnectionProvider connectionProvider, String m_warehouseId)
	      throws ServletException {
	    String strSql = "";
	    strSql = strSql + "        SELECT W.name AS name" + "        FROM M_WAREHOUSE W"
	        + "        WHERE W.M_WAREHOUSE_ID = ?";

	    ResultSet result;
	    PreparedStatement st = null;
	    String warehouse = "";
	    int iParameter = 0;
	    try {
	      st = connectionProvider.getPreparedStatement(strSql);
	      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
	      iParameter++;
	      UtilSql.setValue(st, iParameter, 12, null, m_warehouseId);

	      result = st.executeQuery();
	      boolean continueResult = true;
	      while (continueResult & result.next()) {
	    	  warehouse = UtilSql.getValue(result, "name");
	        continueResult = false; // only one value is expected to receive from sql consult
	      }
	      result.close();

	      if (warehouse.equals("")) {
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

  public static GenerateReportShipmentMinData[] select1(ConnectionProvider connectionProvider,
      String mWarehouseId, String mWarehouseSourceId, String adClientId, String language, String strNumDays,
      String strBaseDays, String adOrgId) throws ServletException {
    return select1(connectionProvider, mWarehouseId, mWarehouseSourceId,adClientId, language, strNumDays, strBaseDays,
        adOrgId, 0, 0);
  }

  public static GenerateReportShipmentMinData[] select1(ConnectionProvider connectionProvider,
      String mWarehouseId, String mWarehouseSourceId, String adClientId, String language, String strNumDays,
      String strBaseDays, String adOrgId, int firstRegister, int numberRegisters)
      throws ServletException {
    String strSql = "";
    strSql = strSql
        +

    " SELECT M_PRODUCT.M_PRODUCT_ID AS IDS,"
  + "              M_PRODUCT.VALUE AS PRODUCTVALUE,  "
  + "              M_PRODUCT.NAME AS PRODUCTNAME,  "
  
  +  "  COALESCE(sre_product_avg_monthly_specif(?, ?,M_PRODUCT.M_PRODUCT_ID, "
  + "                                            date(now()) - to_number(?)," 
  + "                                            date(now())"
  + "                                            ,?), 0) AS TDELIVERED, "
  //+ "               COALESCE(floor(SUM(M_INOUTLINE.movementqty)),0) AS TDELIVERED,  "
  + "               ? as DAYS,  "
//  + "               floor((SUM(M_INOUTLINE.movementqty)/to_number(?))*to_number(?)) AS AVERAGE,  "
  + "               COALESCE(QUERY.onHand,'0') as ONHAND,  "
 // + "                case WHEN floor((SUM(M_INOUTLINE.movementqty)/to_number(?))*to_number(?) - QUERY.onHand) < 0 THEN  '0'  "
 // + "                else COALESCE(floor((SUM(M_INOUTLINE.movementqty)/to_number(?))*to_number(?) - QUERY.onHand),'0') end as REQUIRED,     "
  + "                '' as NAME,    "
  + "                case WHEN M_PRODUCT.em_swa_primary_partner_id IS NOT NULL THEN COALESCE((SELECT qtystd FROM M_PRODUCT_PO WHERE M_PRODUCT_PO.M_Product_id = M_PRODUCT.M_Product_id AND C_BPARTNER_ID = M_PRODUCT.em_swa_primary_partner_id),'0') "
  + "                else COALESCE((SELECT qtystd FROM M_PRODUCT_PO WHERE M_PRODUCT_PO.M_Product_id = M_PRODUCT.M_Product_id order by M_PRODUCT_PO.iscurrentvendor desc limit 1),'1') end as UNTXBOX, "
  + "               case WHEN floor((SUM(M_INOUTLINE.movementqty)/to_number(?))*to_number(?) - QUERY.onHand) < 0 THEN  '0' "
  + "                    WHEN COALESCE(M_PRODUCT. EM_Scr_Unitsperbox,'0') = 0 THEN '0' "
  + "               else COALESCE(M_PRODUCT. EM_Scr_Unitsperbox,'0')*(CEIL((COALESCE(floor((SUM(M_INOUTLINE.movementqty)/to_number(?))*to_number(?) - QUERY.onHand),'0'))/(COALESCE(M_PRODUCT. EM_Scr_Unitsperbox,'1')))) end as VALUEINT, "
  + "               CASE WHEN floor((SUM(M_INOUTLINE.movementqty)/to_number(?))*to_number(?) - QUERY.onHand) < 0 THEN  '0' "
  + "               WHEN COALESCE(M_PRODUCT. EM_Scr_Unitsperbox,'0') = 0 THEN '0' "
  + "               else CEIL(COALESCE(floor((SUM(M_INOUTLINE.movementqty)/to_number(?))*to_number(?) - QUERY.onHand),'0')/COALESCE(M_PRODUCT. EM_Scr_Unitsperbox,'0')) end as NUMBOXES, "
  + "               COALESCE(vpw.totalqty,0) as STOCKACTUAL "
  + "      FROM M_INOUT, M_INOUTLINE, M_PRODUCT "
  + "                     left join (  "
  + "                                 select m_product_id as ID ,(sum(qtyonhand) - sum(qtyreserved)) as onHand "
  + "                                   from m_product_warehouse_qtys_v  "
  + "                                    Where m_warehouse_id = ?   "
  + "                                   group by m_product_id "
  + "                        ) QUERY on QUERY.ID = M_PRODUCT.M_PRODUCT_ID "
  
  + "                left join  swa_product_warehouse_v vpw on  M_PRODUCT.M_PRODUCT_ID = vpw.M_PRODUCT_ID and vpw.m_WAREHOUSE_ID = ? "
  + "               WHERE M_INOUT.M_INOUT_ID = M_INOUTLINE.M_INOUT_ID "
  + "                 AND M_INOUTLINE.M_PRODUCT_ID = M_PRODUCT.M_PRODUCT_ID "
  + "                 AND M_INOUT.DOCSTATUS='CO' "
  + "                 AND M_INOUT.ISSOTRX='Y' "
  + "                 AND M_INOUT.movementdate >=  date(now()) - to_number(?) "
  + "                 AND M_INOUT.movementdate <  now() "
  + "                 AND M_INOUT.M_WAREHOUSE_ID = ?  "
  + "                  AND M_INOUT.AD_CLIENT_ID = ? "
  + "          GROUP BY M_PRODUCT.M_PRODUCT_ID,M_PRODUCT.em_swa_primary_partner_id, M_PRODUCT.NAME , QUERY.onHand,M_PRODUCT.EM_Scr_Unitsperbox,M_PRODUCT.VALUE , STOCKACTUAL"
  + "            HAVING SUM(M_INOUTLINE.movementqty) > 0 " + "     ORDER BY TDELIVERED DESC ";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;
    
    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      
      
      System.out.println("adOrgId: " + adOrgId);
      System.out.println("adClientId: " + adClientId);
      System.out.println("strBaseDays: " + strBaseDays);
      System.out.println("mWarehouseId: " + mWarehouseId);
      
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adOrgId);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adClientId);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, strBaseDays);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, mWarehouseId);
      
      
  /*    iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, strNumDays);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, strBaseDays);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, strNumDays);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, strBaseDays);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, strNumDays);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, strBaseDays);*/
      
      
      
      
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, strNumDays); // 7
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, strBaseDays);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, strNumDays);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, strBaseDays);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, strNumDays); // 11
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, strBaseDays);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, strNumDays);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, strBaseDays);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, strNumDays); // 15
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, mWarehouseId);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, mWarehouseSourceId);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, strBaseDays);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, mWarehouseId);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adClientId);
      //System.out.println(st);
      result = st.executeQuery();
      
      System.out.println("ST: " + st);
      
      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while (countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }
      
      System.out.println("strNumDays: " + strNumDays);
      
      String warehousename = getWarehouseName(connectionProvider,
    		  mWarehouseId);
      
      String warehouseSource = getWarehouseName(connectionProvider,
    		  mWarehouseSourceId);
      
      
      BigDecimal totalConsumo = BigDecimal.ZERO;
      BigDecimal totalDias = new BigDecimal(strBaseDays);
      BigDecimal proyeccionDias = new BigDecimal(strNumDays);
      BigDecimal totalPromedio = BigDecimal.ZERO;
      BigDecimal totalonHand = BigDecimal.ZERO;
      BigDecimal totalRequired = BigDecimal.ZERO;
      while (continueResult && result.next()) {
        countRecord++;
        GenerateReportShipmentMinData objectGenerateReportShipmentMinData = new GenerateReportShipmentMinData();

        objectGenerateReportShipmentMinData.WAREHOUSENAME =warehousename; //Solo Para Mostrar en la cabecera del reporte
        objectGenerateReportShipmentMinData.WAREHOUSESOURCENAME = warehouseSource;                        //Solo Para Mostrar en la cabecera del reporte
        
        objectGenerateReportShipmentMinData.IDS = UtilSql.getValue(result, "IDS");
        objectGenerateReportShipmentMinData.orgid = "name";
        objectGenerateReportShipmentMinData.PRODUCTNAME = UtilSql.getValue(result, "PRODUCTNAME");
        objectGenerateReportShipmentMinData.TDELIVERED = UtilSql.getValue(result, "TDELIVERED");
        objectGenerateReportShipmentMinData.DAYS = UtilSql.getValue(result, "DAYS");
        
        totalConsumo = new BigDecimal(UtilSql.getValue(result, "TDELIVERED"));
        totalPromedio = (totalConsumo.divide(totalDias)).multiply(proyeccionDias);
        
        //objectGenerateReportShipmentMinData.AVERAGE =     UtilSql.getValue(result, "AVERAGE");
        objectGenerateReportShipmentMinData.AVERAGE =  totalPromedio.toString();
        objectGenerateReportShipmentMinData.ONHAND = UtilSql.getValue(result, "ONHAND");
        
        totalonHand = new BigDecimal(UtilSql.getValue(result, "ONHAND"));
        totalRequired = (totalPromedio.subtract(totalonHand)).compareTo(BigDecimal.ZERO)==-1?BigDecimal.ZERO:totalPromedio.subtract(totalonHand);
        
        
        //objectGenerateReportShipmentMinData.REQUIRED = UtilSql.getValue(result, "REQUIRED");
        objectGenerateReportShipmentMinData.REQUIRED = totalRequired.toString();
        objectGenerateReportShipmentMinData.NAME = UtilSql.getValue(result, "NAME");
        objectGenerateReportShipmentMinData.UNTXBOX = UtilSql.getValue(result, "UNTXBOX");
        objectGenerateReportShipmentMinData.PRODUCTVALUE = UtilSql.getValue(result, "PRODUCTVALUE");
        
        if (Float.parseFloat(objectGenerateReportShipmentMinData.UNTXBOX) <= 0) {
          objectGenerateReportShipmentMinData.UNTXBOX = "1";
        }
        objectGenerateReportShipmentMinData.VALUEINT = UtilSql.getValue(result, "VALUEINT");
        objectGenerateReportShipmentMinData.NUMBOXES = UtilSql.getValue(result, "NUMBOXES");
        objectGenerateReportShipmentMinData.STOCKACTUAL = UtilSql.getValue(result, "STOCKACTUAL");
        objectGenerateReportShipmentMinData.rownum = Long.toString(countRecord);
        objectGenerateReportShipmentMinData.InitRecordNumber = Integer.toString(firstRegister);

        vector.addElement(objectGenerateReportShipmentMinData);
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
    
    
    
    
    GenerateReportShipmentMinData objectGenerateReportShipmentMinData[] = new GenerateReportShipmentMinData[vector
        .size()];
    vector.copyInto(objectGenerateReportShipmentMinData);
    return (objectGenerateReportShipmentMinData);
  }

  public static GenerateReportShipmentMinData[] set() throws ServletException {
    GenerateReportShipmentMinData objectGenerateReportShipmentMinData[] = new GenerateReportShipmentMinData[1];
    objectGenerateReportShipmentMinData[0] = new GenerateReportShipmentMinData();

    objectGenerateReportShipmentMinData[0].IDS = "";
    objectGenerateReportShipmentMinData[0].BPARTNERNAME = "";
    objectGenerateReportShipmentMinData[0].PRODUCTNAME = "";
    objectGenerateReportShipmentMinData[0].QTYDELIVERED = "";
    objectGenerateReportShipmentMinData[0].TRANSCURRENCYID = "";
    objectGenerateReportShipmentMinData[0].TRANSCLIENTID = "";
    objectGenerateReportShipmentMinData[0].TRANSORGID = "";
    objectGenerateReportShipmentMinData[0].NAME = "";
    objectGenerateReportShipmentMinData[0].VALUEINT = "";
    objectGenerateReportShipmentMinData[0].NUMBOXES = "";

    objectGenerateReportShipmentMinData[0].orgid = "";
    objectGenerateReportShipmentMinData[0].searchkey = "";
    objectGenerateReportShipmentMinData[0].name = "";
    objectGenerateReportShipmentMinData[0].unit = "";
    objectGenerateReportShipmentMinData[0].qty = "";
    objectGenerateReportShipmentMinData[0].cost = "";
    objectGenerateReportShipmentMinData[0].value = "";
    objectGenerateReportShipmentMinData[0].percentage = "";
    objectGenerateReportShipmentMinData[0].isabc = "";
    objectGenerateReportShipmentMinData[0].padre = "";
    objectGenerateReportShipmentMinData[0].orgpadre = ""; 
    objectGenerateReportShipmentMinData[0].orgchildid = "";
    objectGenerateReportShipmentMinData[0].orgname = "";
    objectGenerateReportShipmentMinData[0].PRODUCTVALUE = ""; 
    objectGenerateReportShipmentMinData[0].STOCKACTUAL = ""; 
    objectGenerateReportShipmentMinData[0].WAREHOUSENAME = "";
    objectGenerateReportShipmentMinData[0].WAREHOUSESOURCENAME = "";

    
    objectGenerateReportShipmentMinData[0].id = "";
    return objectGenerateReportShipmentMinData;
  }
  
  public static GenerateReportShipmentMinData[] selectOrganizationChildDouble(
	      ConnectionProvider connectionProvider, String adUserClient) throws ServletException {
	    return selectOrganizationChildDouble(connectionProvider, adUserClient, 0, 0);
 }
  

  public static GenerateReportShipmentMinData[] selectOrganizationChildDouble(
      ConnectionProvider connectionProvider, String adUserClient, int firstRegister,
      int numberRegisters) throws ServletException {
    String strSql = "";
    strSql = strSql
        + "        SELECT (SELECT * from ad_get_org_le_bu(ad_org_id,'LE'))::CHARACTER VARYING(32)  as PADRE , ad_org_id as ID , value as NAME "
        + "        FROM  ad_org " + "        WHERE 1=1"
        + "         AND AD_Client_ID IN(";
    strSql = strSql + ((adUserClient == null || adUserClient.equals("")) ? "" : adUserClient);
    strSql = strSql
        + ")"
        +"     AND  (SELECT * from ad_get_org_le_bu(ad_org_id,'LE')) <> ad_org_id ";
    strSql = strSql + "        ORDER BY PADRE, NAME";

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
        GenerateReportShipmentMinData objectGenerateReportShipmentMinData = new GenerateReportShipmentMinData();
        objectGenerateReportShipmentMinData.padre = UtilSql.getValue(result, "padre");
        objectGenerateReportShipmentMinData.id = UtilSql.getValue(result, "id");
        objectGenerateReportShipmentMinData.name = UtilSql.getValue(result, "name");
        objectGenerateReportShipmentMinData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectGenerateReportShipmentMinData);
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
    GenerateReportShipmentMinData objectGenerateReportShipmentMinData[] = new GenerateReportShipmentMinData[vector
        .size()];
    vector.copyInto(objectGenerateReportShipmentMinData);
    return (objectGenerateReportShipmentMinData);
  }


  public static GenerateReportShipmentMinData[] selectWarehouseDouble(
      ConnectionProvider connectionProvider, String adUserClient) throws ServletException {
    return selectWarehouseDouble(connectionProvider, adUserClient, 0, 0);
  }

  public static GenerateReportShipmentMinData[] selectWarehouseDouble(
      ConnectionProvider connectionProvider, String adUserClient, int firstRegister,
      int numberRegisters) throws ServletException {
    String strSql = "";
    strSql = strSql
        + "        SELECT M_WAREHOUSE.AD_ORG_ID AS PADRE, M_WAREHOUSE.M_WAREHOUSE_ID AS ID, TO_CHAR(value) || TO_CHAR(' - ') || TO_CHAR(M_WAREHOUSE.NAME) AS NAME"
        + "        FROM M_WAREHOUSE" + "        WHERE 1=1"
        + "         AND M_WAREHOUSE.AD_Client_ID IN(";
    strSql = strSql + ((adUserClient == null || adUserClient.equals("")) ? "" : adUserClient);
    strSql = strSql
        + ")"
        + "         UNION "
        + "        SELECT null AS PADRE, M_WAREHOUSE.M_WAREHOUSE_ID AS ID, TO_CHAR(M_WAREHOUSE.NAME) AS NAME"
        + "        FROM M_WAREHOUSE" + "        WHERE 2=2 AND M_WAREHOUSE.AD_Client_ID IN(";
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
        GenerateReportShipmentMinData objectGenerateReportShipmentMinData = new GenerateReportShipmentMinData();
        objectGenerateReportShipmentMinData.padre = UtilSql.getValue(result, "padre");
        objectGenerateReportShipmentMinData.id = UtilSql.getValue(result, "id");
        objectGenerateReportShipmentMinData.name = UtilSql.getValue(result, "name");
        objectGenerateReportShipmentMinData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectGenerateReportShipmentMinData);
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
    GenerateReportShipmentMinData objectGenerateReportShipmentMinData[] = new GenerateReportShipmentMinData[vector
        .size()];
    vector.copyInto(objectGenerateReportShipmentMinData);
    return (objectGenerateReportShipmentMinData);
  }

  public static GenerateReportShipmentMinData mUpdateParetoProduct0(
      ConnectionProvider connectionProvider, String adPinstanceId) throws ServletException {
    String strSql = "";
    strSql = strSql + "        CALL M_UPDATE_PARETO_PRODUCT0(?)";

    GenerateReportShipmentMinData objectGenerateReportShipmentMinData = new GenerateReportShipmentMinData();
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
    return (objectGenerateReportShipmentMinData);
  }

  public static int insertMovements(ConnectionProvider connectionProvider, String strMovementId,
      String adClientId) throws ServletException {
    String strSql = "";
    strSql = strSql +

    "insert into m_movement ( m_movement_id, ad_client_id, ad_org_id, isactive,  created,  createdby,"
        + " updatedby,  updated,  name,  movementdate,  posted,  processed,  processing, "
        + " move_fromto_locator,  documentno) "
        + "Values ( ?, ?, '99766C35EBF14EF69E8F9AA303B8163D',"
        + " 'Y', now(), 100, 100, now(), 'SIMONEL', now(), 'N', 'N', 'N', 'N', '10000065' ) ";

    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, strMovementId);
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
        connectionProvider.releaseTransactionalPreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }
    return (updateCount);
  }

}