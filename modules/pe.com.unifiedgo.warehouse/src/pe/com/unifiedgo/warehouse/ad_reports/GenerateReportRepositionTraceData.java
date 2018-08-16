//Sqlc generated V1.O00-1
package pe.com.unifiedgo.warehouse.ad_reports;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.hamcrest.core.IsEqual;
import org.hibernate.Query;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.data.UtilSql;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.database.SessionInfo;
import org.openbravo.model.common.plm.Product;
import org.openbravo.service.db.QueryTimeOutUtil;



class GenerateReportRepositionTraceData implements FieldProvider {
  static Logger log4j = Logger.getLogger(GenerateReportRepositionTraceData.class);
  private String InitRecordNumber = "0";
  public String productid;
  public String searchkey;
  public String internalcode;
  public String name;
  public String padre;
  public String id;
  
  public String transactionID;
  public String movementDate;
  public String warehousename;
  public String storagebin;
  public String movementqty;
  public String movementqtynegative;
  public String movementtype;
  public String shipmentid;
  public String inventoryid;
  public String movementid;
  public String productionid;
  public String shipmentline;
  public String inventoryline;
  public String movementline;
  public String productionline;
  public String prdID;
  
  public String movementqtyInitial;
  public String prdInitialID;
  
  public String movementqtyFinal;
  public String prdFinalID;
  
  
  
  
  //-------------
  public String ioid;
  public String bpname;
  public String shipnum;
  public String shipphysical;
  public String orderid;
  public String ordernum;   
  public String clientid;
  public String shipmentdate;
  //-------------
  
  
  
  
  
  
  
  
  public BigDecimal avgmonthlysales1;
  public BigDecimal avgmonthlysales2;
  public BigDecimal avgmonthlysales3;
  public BigDecimal avgmonthlysales4;
  public BigDecimal avgmonthlysales5;
  public BigDecimal avgmonthlysales6;
  public BigDecimal avgmonthlysales7;
  public BigDecimal avgmonthlysales8;
  public BigDecimal avgmonthlysales9;
  public BigDecimal avgmonthlysales10;
  public BigDecimal avgmonthlysales11;
  public BigDecimal avgmonthlysales12;
  public String rownum;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  @Override
  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("productid"))
      return productid;
    else if (fieldName.equalsIgnoreCase("movementqtyFinal"))
        return movementqtyFinal;
    else if (fieldName.equalsIgnoreCase("id"))
        return id;
    else if (fieldName.equalsIgnoreCase("padre"))
        return padre;
    else if (fieldName.equalsIgnoreCase("prdFinalID"))
        return prdFinalID;
    else if (fieldName.equalsIgnoreCase("movementqtyInitial"))
        return movementqtyInitial;
    else if (fieldName.equalsIgnoreCase("prdInitialID"))
        return prdInitialID;
    else if (fieldName.equalsIgnoreCase("shipmentline"))
        return shipmentline;
    else if (fieldName.equalsIgnoreCase("inventoryline"))
        return inventoryline;
    else if (fieldName.equalsIgnoreCase("movementline"))
        return movementline;
    else if (fieldName.equalsIgnoreCase("productionline"))
        return productionline;
    else if (fieldName.equalsIgnoreCase("prdID"))
          return prdID;
    else if (fieldName.equalsIgnoreCase("transactionID"))
        return transactionID;
    else if (fieldName.equalsIgnoreCase("movementDate"))
        return movementDate;
    else if (fieldName.equalsIgnoreCase("warehousename"))
        return warehousename;
    else if (fieldName.equalsIgnoreCase("storagebin"))
        return storagebin;
    else if (fieldName.equalsIgnoreCase("movementqty"))
        return movementqty;
    else if (fieldName.equalsIgnoreCase("movementqtynegative"))
        return movementqtynegative;
    else if (fieldName.equalsIgnoreCase("movementtype"))
        return movementtype;
    else if (fieldName.equalsIgnoreCase("shipmentid"))
        return shipmentid;
    else if (fieldName.equalsIgnoreCase("inventoryid"))
        return inventoryid;
    else if (fieldName.equalsIgnoreCase("inventoryid"))
        return movementid;
    else if (fieldName.equalsIgnoreCase("productionid"))
        return productionid;
    else if (fieldName.equalsIgnoreCase("searchkey"))
      return searchkey;
    else if (fieldName.equalsIgnoreCase("internalcode"))
      return internalcode;
    else if (fieldName.equalsIgnoreCase("name"))
      return name;
    
    else if (fieldName.equalsIgnoreCase("ioid"))
        return ioid;
    else if (fieldName.equalsIgnoreCase("bpname"))
        return bpname;
    else if (fieldName.equalsIgnoreCase("shipnum"))
        return shipnum;
    else if (fieldName.equalsIgnoreCase("shipphysical"))
        return shipphysical;
    else if (fieldName.equalsIgnoreCase("orderid"))
        return orderid;
    else if (fieldName.equalsIgnoreCase("ordernum"))
        return ordernum;
    else if (fieldName.equalsIgnoreCase("clientid"))
        return clientid;
    else if (fieldName.equalsIgnoreCase("shipmentdate"))
        return shipmentdate;
    
    else if (fieldName.equalsIgnoreCase("avgmonthlysales1")) {
      if (avgmonthlysales1.intValue() == -1)
        return "--";
      return avgmonthlysales1.toString();
    } else if (fieldName.equalsIgnoreCase("avgmonthlysales2")) {
      if (avgmonthlysales2.intValue() == -1)
        return "--";
      return avgmonthlysales2.toString();
    } else if (fieldName.equalsIgnoreCase("avgmonthlysales3")) {
      if (avgmonthlysales3.intValue() == -1)
        return "--";
      return avgmonthlysales3.toString();
    } else if (fieldName.equalsIgnoreCase("avgmonthlysales4")) {
      if (avgmonthlysales4.intValue() == -1)
        return "--";
      return avgmonthlysales4.toString();
    } else if (fieldName.equalsIgnoreCase("avgmonthlysales5")) {
      if (avgmonthlysales5.intValue() == -1)
        return "--";
      return avgmonthlysales5.toString();
    } else if (fieldName.equalsIgnoreCase("avgmonthlysales6")) {
      if (avgmonthlysales6.intValue() == -1)
        return "--";
      return avgmonthlysales6.toString();
    } else if (fieldName.equalsIgnoreCase("avgmonthlysales7")) {
      if (avgmonthlysales7.intValue() == -1)
        return "--";
      return avgmonthlysales7.toString();
    } else if (fieldName.equalsIgnoreCase("avgmonthlysales8")) {
      if (avgmonthlysales8.intValue() == -1)
        return "--";
      return avgmonthlysales8.toString();
    } else if (fieldName.equalsIgnoreCase("avgmonthlysales9")) {
      if (avgmonthlysales9.intValue() == -1)
        return "--";
      return avgmonthlysales9.toString();
    } else if (fieldName.equalsIgnoreCase("avgmonthlysales10")) {
      if (avgmonthlysales10.intValue() == -1)
        return "--";
      return avgmonthlysales10.toString();
    } else if (fieldName.equalsIgnoreCase("avgmonthlysales11")) {
      if (avgmonthlysales11.intValue() == -1)
        return "--";
      return avgmonthlysales11.toString();
    } else if (fieldName.equalsIgnoreCase("avgmonthlysales12")) {
      if (avgmonthlysales12.intValue() == -1)
        return "--";
      return avgmonthlysales12.toString();
    } else if (fieldName.equals("rownum"))
      return rownum;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }
  
  public static GenerateReportRepositionTraceData[] selectKardexFinal(ConnectionProvider connectionProvider, String adOrgId, String adClientId,
	      String mProductId, String docDate, String DateTo, String warehouse_id) throws ServletException {
	    return selectKardexFinal(connectionProvider, adOrgId, adClientId, mProductId, docDate,null, 0, 0,DateTo, warehouse_id );
	  }
  
  public static GenerateReportRepositionTraceData[] selectKardexFinal(ConnectionProvider connectionProvider, String adOrgId, String adClientId,
	      String mProductId, String docDate, String numMonths, int firstRegister, int numberRegisters,String DateTo, String warehouse_id)
	      throws ServletException {

	    String strSql = "";
	    strSql = strSql
        +" SELECT mp.m_product_id AS FINALID, "
        +"  (SELECT coalesce(sum(mt.movementqty),0) "
        +"     FROM M_Transaction mt"
        +"          LEFT JOIN m_locator ml "
        +"                    ON ml.m_locator_id =  mt.m_locator_id "
        +"          LEFT JOIN m_warehouse mw "
        +"                    ON mw.m_warehouse_id = ml.m_warehouse_id "
        +"         WHERE mt.m_product_id = ?"
        +"         AND AD_ISORGINCLUDED(mt.ad_org_id, ?, ?) > -1"
        +"         AND mt.movementdate < to_date(?, 'dd-MM-yyyy')";
        if(warehouse_id != null && !warehouse_id.isEmpty()){
	     strSql = strSql
            + "     AND mw.m_warehouse_id=?";
        }
	    strSql = strSql
        +"    ) +"
        +" coalesce(QTYFINAL2,0) AS QTYFINAL "
        +" FROM m_product mp "
        +" LEFT JOIN ("
	    + " SELECT   mt.m_product_id as FINALID2,"
	  //  +"         (SELECT coalesce(sum(mt.movementqty),0)"
	  //  +"         FROM M_Transaction mt"
	  //  +"         WHERE mt.m_product_id = ?"
	  //  //+"         AND mt.ad_org_id = ?"
	  //  +"         AND AD_ISORGINCLUDED(mt.ad_org_id, ?, ?) > -1"
	  //  +"         AND mt.movementdate < to_date(?, 'dd-MM-yyyy')"
	  //  +"	       ) + coalesce(sum(mt.movementqty),0) AS QTYFINAL2"
	    +"   coalesce(sum(mt.movementqty),0) AS QTYFINAL2 "
	    +"	 FROM M_Transaction mt"
	    +"        LEFT JOIN m_locator ml "
	    +"              ON ml.m_locator_id =  mt.m_locator_id  "
	    +"        LEFT JOIN m_warehouse mw "
	    +"              ON mw.m_warehouse_id = ml.m_warehouse_id "
	    +"	 WHERE mt.m_product_id = ?"
	   // +"	   AND mt.ad_org_id = ?"
	    +"         AND AD_ISORGINCLUDED(mt.ad_org_id, ?, ?) > -1"
	    +"	   AND mt.movementdate between ? AND ?";
	    if(warehouse_id != null && !warehouse_id.isEmpty()){
	     strSql = strSql
            + "     AND mw.m_warehouse_id=?";
        }
   strSql = strSql
	    +"	  GROUP BY mt.m_product_id"
	    +"  ) trx ON mp.m_product_id = trx.FINALID2"
	    +" WHERE mp.m_product_id = ?";
	    
	    

	    ResultSet result;
        Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
        PreparedStatement st = null;
       
        int iParameter = 0;
        try {
          st = connectionProvider.getPreparedStatement(strSql);
          
          QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
          iParameter++;
          UtilSql.setValue(st, iParameter, 12, null, mProductId);
          iParameter++;
          UtilSql.setValue(st, iParameter, 12, null, adOrgId);
          iParameter++;
          UtilSql.setValue(st, iParameter, 12, null, adClientId);
          iParameter++;
          UtilSql.setValue(st, iParameter, 12, null, docDate);
          if(warehouse_id != null && !warehouse_id.isEmpty()){
        	  iParameter++;
              UtilSql.setValue(st, iParameter, 12, null, warehouse_id); 
          }
          iParameter++;
          UtilSql.setValue(st, iParameter, 12, null, mProductId);
          iParameter++;
          UtilSql.setValue(st, iParameter, 12, null, adOrgId);
          iParameter++;
          UtilSql.setValue(st, iParameter, 12, null, adClientId);
          iParameter++;
          UtilSql.setValue(st, iParameter, 12, null, docDate);
          iParameter++;
          UtilSql.setValue(st, iParameter, 12, null, DateTo);
          if(warehouse_id != null && !warehouse_id.isEmpty()){
        	  iParameter++;
              UtilSql.setValue(st, iParameter, 12, null, warehouse_id); 
          }
          iParameter++;
          UtilSql.setValue(st, iParameter, 12, null, mProductId);
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
            GenerateReportRepositionTraceData objectGenerateReportRepositionTraceData = new GenerateReportRepositionTraceData();
            objectGenerateReportRepositionTraceData.prdFinalID = UtilSql.getValue(result, "FINALID");
            objectGenerateReportRepositionTraceData.movementqtyFinal = UtilSql.getValue(result, "QTYFINAL");
            objectGenerateReportRepositionTraceData.rownum = Long.toString(countRecord);
            objectGenerateReportRepositionTraceData.InitRecordNumber = Integer.toString(firstRegister);

            vector.addElement(objectGenerateReportRepositionTraceData);
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
	    GenerateReportRepositionTraceData objectGenerateReportRepositionTraceData[] = new GenerateReportRepositionTraceData[vector
	        .size()];
	    vector.copyInto(objectGenerateReportRepositionTraceData);
	    return (objectGenerateReportRepositionTraceData);
	  }
  
  public static GenerateReportRepositionTraceData[] selectKardexInitial(ConnectionProvider connectionProvider, String adOrgId, String adClientId,
	      String mProductId, String docDate, String DateTo, String warehouse_id) throws ServletException {
	    return selectKardexInitial(connectionProvider, adOrgId, adClientId, mProductId, docDate,null, 0, 0,DateTo,  warehouse_id );
	  }
  
  public static GenerateReportRepositionTraceData[] selectKardexInitial(ConnectionProvider connectionProvider, String adOrgId, String adClientId,
	      String mProductId, String docDate, String numMonths, int firstRegister, int numberRegisters,String DateTo, String warehouse_id)
	      throws ServletException {

	    String strSql = "";
	    strSql = strSql
	    +" SELECT mp.m_product_id AS INITIALID, coalesce(trx.QTY2,0) AS QTYINIT   "
	    +" FROM m_product mp "
	    +" LEFT JOIN ( "
	    +" SELECT   mt.m_product_id  AS INITI2, "
	    +"         coalesce(sum(mt.movementqty),0) AS QTY2 " 
	    +"	 FROM M_Transaction mt "
	    +"        LEFT JOIN m_locator ml "
	    +"         ON ml.m_locator_id =  mt.m_locator_id "
	    +"        LEFT JOIN m_warehouse mw "
	    +"        ON mw.m_warehouse_id = ml.m_warehouse_id "
	    +"	 WHERE mt.m_product_id = ? "
	    //+"	   AND mt.ad_org_id = ? "
	    +"     AND AD_ISORGINCLUDED(mt.ad_org_id, ?, ?) > -1"
	    +"	   AND mt.movementdate < to_date(?, 'dd-MM-yyyy') ";
	    if(warehouse_id != null && !warehouse_id.isEmpty()){
    	    	strSql = strSql
	    + "     AND mw.m_warehouse_id=?";
	    }
	    strSql = strSql
	    +"	  GROUP BY mt.m_product_id) trx "
	    +" ON mp.m_product_id = trx.INITI2 "
	    +" WHERE mp.m_product_id = ?";
	    //Falta PONER WHERE CLIENTE
	    ResultSet result;
        Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
        PreparedStatement st = null;
       
        int iParameter = 0;
        try {
          st = connectionProvider.getPreparedStatement(strSql);
          QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
          iParameter++;
          UtilSql.setValue(st, iParameter, 12, null, mProductId);
          iParameter++;
          UtilSql.setValue(st, iParameter, 12, null, adOrgId);
          iParameter++;
          UtilSql.setValue(st, iParameter, 12, null, adClientId);
          iParameter++;
          UtilSql.setValue(st, iParameter, 12, null, docDate);
          if(warehouse_id != null && !warehouse_id.isEmpty()){
        	  iParameter++;
              UtilSql.setValue(st, iParameter, 12, null, warehouse_id);
          }
          iParameter++;
          UtilSql.setValue(st, iParameter, 12, null, mProductId);
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
            GenerateReportRepositionTraceData objectGenerateReportRepositionTraceData = new GenerateReportRepositionTraceData();
            objectGenerateReportRepositionTraceData.prdInitialID = UtilSql.getValue(result, "INITIALID");
            objectGenerateReportRepositionTraceData.movementqtyInitial = UtilSql.getValue(result, "QTYINIT");
            objectGenerateReportRepositionTraceData.rownum = Long.toString(countRecord);
            objectGenerateReportRepositionTraceData.InitRecordNumber = Integer.toString(firstRegister);

            vector.addElement(objectGenerateReportRepositionTraceData);
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
	    GenerateReportRepositionTraceData objectGenerateReportRepositionTraceData[] = new GenerateReportRepositionTraceData[vector
	        .size()];
	    vector.copyInto(objectGenerateReportRepositionTraceData);
	    return (objectGenerateReportRepositionTraceData);
	  }
  
  public static GenerateReportRepositionTraceData[] selectShipmentWithoutReceipt(ConnectionProvider connectionProvider, String adOrgId,String adToOrgId ,String adClientId,
	      String warehouse_id) throws ServletException {
	    return selectShipmentWithoutReceipt(connectionProvider, adOrgId, adToOrgId ,adClientId,  0, 0,warehouse_id );
	  }
  public static GenerateReportRepositionTraceData[] selectShipmentWithoutReceipt(ConnectionProvider connectionProvider, String adOrgId, String adToOrgId ,String adClientId,
	      int firstRegister, int numberRegisters, String warehouse_id)
	      throws ServletException {

	    String strSql = "";
	    strSql = strSql 
	    + 
	    " SELECT   io.m_inout_id AS IOID,"
	    +"         io.ad_client_id AS CLIENTID, "
	    +"         io.movementdate AS shipmentdate,"
	    +"         org.name AS BPNAME," 
	    +"	       io.documentno as SHIPNUM," 
	    +"         io.em_scr_physical_documentno AS SHIPPHYSICAL,"
 	    +" 	       cot.c_order_id AS ORDERID," 
	    +"	       cot.documentno AS ORDERNUM "
	    +"	 FROM M_inout io "
	    +"	  left join ad_org org"
	    +"	          on org.ad_org_id = io.ad_org_id"
	    +"    left join c_order co"
		+"           on io.c_order_id = co.c_order_id"
	    +"	  left join c_order cot"
	    +"	         on cot.c_order_id = co.em_ssa_other_solicitud_id"
	    +"	 where io.issotrx='Y' "
	    +"     AND io.docstatus='CO'"
	    +"     AND co.em_ssa_other_solicitud_id is not null"
	    +"     AND io.m_inout_id not in (SELECT mt.em_ssa_other_inout_id FROM m_inout mt " 
	    +"                               WHERE mt.em_ssa_other_inout_id is not null " 
	    +"                               AND mt.ad_client_id= ? AND mt.docstatus <>'VO')"
	    +"     AND AD_ISORGINCLUDED(cot.ad_org_id, ?, ?) > -1"
	    +"     AND AD_ISORGINCLUDED(io.ad_org_id, ?, ?) > -1 "
	    +"	   AND io.ad_client_id = ? ";
	    ResultSet result;
        Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
        PreparedStatement st = null;
       
        int iParameter = 0;
        try {
          st = connectionProvider.getPreparedStatement(strSql);
          QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
          iParameter++;
          UtilSql.setValue(st, iParameter, 12, null, adClientId);
          iParameter++;
          UtilSql.setValue(st, iParameter, 12, null, adOrgId);
          iParameter++;
          UtilSql.setValue(st, iParameter, 12, null, adClientId);
          iParameter++;
          UtilSql.setValue(st, iParameter, 12, null, adToOrgId);
          iParameter++;
          UtilSql.setValue(st, iParameter, 12, null, adClientId);
          iParameter++;
          UtilSql.setValue(st, iParameter, 12, null, adClientId);
          System.out.println("-------st: " + st);
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
            GenerateReportRepositionTraceData objectGenerateReportRepositionTraceData = new GenerateReportRepositionTraceData();
            objectGenerateReportRepositionTraceData.ioid = UtilSql.getValue(result, "IOID");
            objectGenerateReportRepositionTraceData.bpname = UtilSql.getValue(result, "BPNAME");
            objectGenerateReportRepositionTraceData.shipnum = UtilSql.getValue(result, "SHIPNUM");
            objectGenerateReportRepositionTraceData.shipphysical = UtilSql.getValue(result, "SHIPPHYSICAL");
            objectGenerateReportRepositionTraceData.orderid = UtilSql.getValue(result, "ORDERID");
            objectGenerateReportRepositionTraceData.ordernum = UtilSql.getValue(result, "ORDERNUM");//clientid
            objectGenerateReportRepositionTraceData.clientid = UtilSql.getValue(result, "CLIENTID");//
            objectGenerateReportRepositionTraceData.shipmentdate = UtilSql.getDateValue(result, "SHIPMENTDATE");
            
            objectGenerateReportRepositionTraceData.rownum = Long.toString(countRecord);
            objectGenerateReportRepositionTraceData.InitRecordNumber = Integer.toString(firstRegister);

            vector.addElement(objectGenerateReportRepositionTraceData);
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
	    GenerateReportRepositionTraceData objectGenerateReportRepositionTraceData[] = new GenerateReportRepositionTraceData[vector
	        .size()];
	    vector.copyInto(objectGenerateReportRepositionTraceData);
	    return (objectGenerateReportRepositionTraceData);
	  }
  
  public static GenerateReportRepositionTraceData[] selectKardex(ConnectionProvider connectionProvider, String adOrgId, String adClientId,
	      String mProductId, String docDate, String DateTo, String warehouse_id) throws ServletException {
	    return selectKardex(connectionProvider, adOrgId, adClientId, mProductId, docDate,null, 0, 0,DateTo,warehouse_id );
	  }
  
  public static GenerateReportRepositionTraceData[] selectKardex(ConnectionProvider connectionProvider, String adOrgId, String adClientId,
	      String mProductId, String docDate, String numMonths, int firstRegister, int numberRegisters,String DateTo, String warehouse_id)
	      throws ServletException {

	    String strSql = "";
	    strSql = strSql 
	    + 
	    " SELECT   mt.m_product_id as PRODUCTID,"
	    +"         mt.m_transaction_id as ID," 
	    +"	       mt.movementdate as MOVEMENTDATE," 
	    +"         mw.name AS WAREHOUSENAME,"
 	    +" 	       ml.value AS BIN," 
	    +"	       CASE WHEN (mt.movementqty)>= 0 THEN mt.movementqty ELSE NULL END AS QTY,"
	    +"	       CASE WHEN (mt.movementqty)< 0 THEN mt.movementqty*-1 ELSE NULL END AS QTYNEGATIVE,"
	    +"	       ci.name AS MOVEMENTTYPE,"
	    +"	       mt.m_inoutline_id AS SHIPMENT,"
	    +"         mi.line  ||  ' - ' ||  io.documentno AS SHIPMENTLINE,"
	    +"	       mt.m_inventoryLine_id AS INVENTORY,"
	    +"         mil.line  ||  ' - ' ||  miv.documentno AS INVENTORYLINE,"
	    +"	       mt.m_movementline_id AS MOVEMENT,"
	    +"         mml.line  ||  ' - ' ||  mmm.documentno AS MOVEMENTLINE,"
	    +"         mt.m_productionline_id AS PRODUCTION, "
	    +"         mprdl.line || ' - ' || mprd.documentno AS PRODUCTIONLINE "
	    +"	 FROM M_Transaction mt"
	    +"	  LEFT JOIN m_locator ml"
	    +"	          ON ml.m_locator_id =  mt.m_locator_id"
	    +"    LEFT JOIN m_warehouse mw"
		+"           ON mw.m_warehouse_id = ml.m_warehouse_id"
	    +"	  LEFT JOIN scr_combo_item ci"
	    +"	          ON ci.scr_combo_item_id = mt.em_ssa_combo_item_id"
	    +"	  LEFT JOIN m_product mp"
	    +"	          ON mp.m_product_id = mt.m_product_id"
	    +"	  LEFT JOIN c_uom cu"
	    +"	          ON cu.c_uom_id = mt.c_uom_id"
	    +"     LEFT JOIN m_inoutline mi"
	    +"            ON mi.m_inoutline_id = mt.m_inoutline_id"
		+"     LEFT JOIN m_inout io"
		+"		        ON io.m_inout_id = mi.m_inout_id"
		+"		LEFT JOIN m_inventoryLine mil"
		+"		        ON mil.m_inventoryLine_id = mt.m_inventoryLine_id"
		+"		LEFT JOIN m_inventory miv"
		+"		        ON miv.m_inventory_id = mil.m_inventory_id"
		+"		LEFT JOIN m_movementline mml"
		+"		        ON mml.m_movementline_id = mt.m_movementline_id"
		+"		LEFT JOIN m_movement mmm"
		+"		        ON mmm.m_movement_id = mml.m_movement_id"
		+"      LEFT JOIN m_productionline mprdl"
		+"              ON mprdl.m_productionline_id = mt.m_productionline_id"
		+"      LEFT JOIN m_productionplan mprdp"
		+"              ON mprdp.m_productionplan_id = mprdl.m_productionplan_id"
		+"      LEFT JOIN m_production mprd"
		+"              ON mprd.m_production_id = mprdp.m_production_id"
	    +"	 WHERE mt.m_product_id = ?"
	   // +"	   AND mt.ad_org_id = ?"
	    +"  AND AD_ISORGINCLUDED(mt.ad_org_id, ?, ?) > -1"
	    +"	   AND mt.movementdate between ? AND ? ";
	    if(warehouse_id != null && !warehouse_id.isEmpty()){
	    	strSql = strSql
	    	+"	   AND mw.m_warehouse_id=? ";
	    }
	    strSql = strSql
	    +"	  ORDER BY mt.movementdate";
	    //Falta PONER WHERE CLIENTE
	    ResultSet result;
        Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
        PreparedStatement st = null;
       
        int iParameter = 0;
        try {
          st = connectionProvider.getPreparedStatement(strSql);
          QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
          iParameter++;
          UtilSql.setValue(st, iParameter, 12, null, mProductId);
          iParameter++;
          UtilSql.setValue(st, iParameter, 12, null, adOrgId);
          iParameter++;
          UtilSql.setValue(st, iParameter, 12, null, adClientId);
          iParameter++;
          UtilSql.setValue(st, iParameter, 12, null, docDate);
          iParameter++;
          UtilSql.setValue(st, iParameter, 12, null, DateTo);
          iParameter++;
          UtilSql.setValue(st, iParameter, 12, null, warehouse_id);
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
            GenerateReportRepositionTraceData objectGenerateReportRepositionTraceData = new GenerateReportRepositionTraceData();
            objectGenerateReportRepositionTraceData.prdID = UtilSql.getValue(result, "PRODUCTID");
            objectGenerateReportRepositionTraceData.transactionID = UtilSql.getValue(result, "ID");
            objectGenerateReportRepositionTraceData.movementDate = UtilSql.getDateValue(result, "MOVEMENTDATE");
            objectGenerateReportRepositionTraceData.storagebin = UtilSql.getValue(result, "BIN");
            objectGenerateReportRepositionTraceData.warehousename = UtilSql.getValue(result, "WAREHOUSENAME");
            objectGenerateReportRepositionTraceData.movementqty = UtilSql.getValue(result, "QTY");
            objectGenerateReportRepositionTraceData.movementqtynegative = UtilSql.getValue(result, "QTYNEGATIVE");
            objectGenerateReportRepositionTraceData.movementtype = UtilSql.getValue(result, "MOVEMENTTYPE");
            objectGenerateReportRepositionTraceData.shipmentid = UtilSql.getValue(result, "SHIPMENT");
            objectGenerateReportRepositionTraceData.shipmentline = UtilSql.getValue(result, "SHIPMENTLINE");
            objectGenerateReportRepositionTraceData.inventoryline = UtilSql.getValue(result, "INVENTORYLINE");
            objectGenerateReportRepositionTraceData.movementline = UtilSql.getValue(result, "MOVEMENTLINE");
            objectGenerateReportRepositionTraceData.productionline = UtilSql.getValue(result, "PRODUCTIONLINE");
            objectGenerateReportRepositionTraceData.inventoryid = UtilSql.getValue(result, "INVENTORY");
            objectGenerateReportRepositionTraceData.movementid = UtilSql.getValue(result, "MOVEMENT");
            objectGenerateReportRepositionTraceData.productionid = UtilSql.getValue(result, "PRODUCTION");
            objectGenerateReportRepositionTraceData.rownum = Long.toString(countRecord);
            objectGenerateReportRepositionTraceData.InitRecordNumber = Integer.toString(firstRegister);

            vector.addElement(objectGenerateReportRepositionTraceData);
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
	    GenerateReportRepositionTraceData objectGenerateReportRepositionTraceData[] = new GenerateReportRepositionTraceData[vector
	        .size()];
	    vector.copyInto(objectGenerateReportRepositionTraceData);
	    return (objectGenerateReportRepositionTraceData);
	  }

  public static GenerateReportRepositionTraceData[] select(String adOrgId, String adClientId,
      String mProductId, String docDate, String numMonths) throws ServletException {
    return select(adOrgId, adClientId, mProductId, docDate, numMonths, 0, 0);
  }

  public static GenerateReportRepositionTraceData[] select(String adOrgId, String adClientId,
      String mProductId, String docDate, String numMonths, int firstRegister, int numberRegisters)
      throws ServletException {

    String strSql = "";
    strSql = "SELECT COALESCE(sre_product_avg_monthly_sales(?, ?, ?, "
        + "                                        DATE(to_char((TO_DATE(?) - interval '12 month'),'YYYY-MM') || '-01'), "
        + "                                        DATE(to_char((TO_DATE(?) - interval '11 month'),'YYYY-MM') || '-01') - 1), 0) AS avg_monthly_sales_1, "
        + "          COALESCE(sre_product_avg_monthly_sales(?, ?, ?, "
        + "                                        DATE(to_char((TO_DATE(?) - interval '11 month'),'YYYY-MM') || '-01'), "
        + "                                        DATE(to_char((TO_DATE(?) - interval '10 month'),'YYYY-MM') || '-01') - 1), 0) AS avg_monthly_sales_2, "
        + "          COALESCE(sre_product_avg_monthly_sales(?, ?, ?, "
        + "                                        DATE(to_char((TO_DATE(?) - interval '10 month'),'YYYY-MM') || '-01'), "
        + "                                        DATE(to_char((TO_DATE(?) - interval '9 month'),'YYYY-MM') || '-01') - 1), 0) AS avg_monthly_sales_3, "
        + "          COALESCE(sre_product_avg_monthly_sales(?, ?, ?, "
        + "                                        DATE(to_char((TO_DATE(?) - interval '9 month'),'YYYY-MM') || '-01'), "
        + "                                        DATE(to_char((TO_DATE(?) - interval '8 month'),'YYYY-MM') || '-01') - 1), 0) AS avg_monthly_sales_4, "
        + "          COALESCE(sre_product_avg_monthly_sales(?, ?, ?, "
        + "                                        DATE(to_char((TO_DATE(?) - interval '8 month'),'YYYY-MM') || '-01'), "
        + "                                        DATE(to_char((TO_DATE(?) - interval '7 month'),'YYYY-MM') || '-01') - 1), 0) AS avg_monthly_sales_5, "
        + "          COALESCE(sre_product_avg_monthly_sales(?, ?, ?, "
        + "                                        DATE(to_char((TO_DATE(?) - interval '7 month'),'YYYY-MM') || '-01'), "
        + "                                        DATE(to_char((TO_DATE(?) - interval '6 month'),'YYYY-MM') || '-01') - 1), 0) AS avg_monthly_sales_6, "
        + "          COALESCE(sre_product_avg_monthly_sales(?, ?, ?, "
        + "                                        DATE(to_char((TO_DATE(?) - interval '6 month'),'YYYY-MM') || '-01'), "
        + "                                        DATE(to_char((TO_DATE(?) - interval '5 month'),'YYYY-MM') || '-01') - 1), 0) AS avg_monthly_sales_7, "
        + "          COALESCE(sre_product_avg_monthly_sales(?, ?, ?, "
        + "                                        DATE(to_char((TO_DATE(?) - interval '5 month'),'YYYY-MM') || '-01'), "
        + "                                        DATE(to_char((TO_DATE(?) - interval '4 month'),'YYYY-MM') || '-01') - 1), 0) AS avg_monthly_sales_8, "
        + "          COALESCE(sre_product_avg_monthly_sales(?, ?, ?, "
        + "                                        DATE(to_char((TO_DATE(?) - interval '4 month'),'YYYY-MM') || '-01'), "
        + "                                        DATE(to_char((TO_DATE(?) - interval '3 month'),'YYYY-MM') || '-01') - 1), 0) AS avg_monthly_sales_9, "
        + "          COALESCE(sre_product_avg_monthly_sales(?, ?, ?, "
        + "                                        DATE(to_char((TO_DATE(?) - interval '3 month'),'YYYY-MM') || '-01'), "
        + "                                        DATE(to_char((TO_DATE(?) - interval '2 month'),'YYYY-MM') || '-01') - 1), 0) AS avg_monthly_sales_10, "
        + "          COALESCE(sre_product_avg_monthly_sales(?, ?, ?, "
        + "                                        DATE(to_char((TO_DATE(?) - interval '2 month'),'YYYY-MM') || '-01'), "
        + "                                        DATE(to_char((TO_DATE(?) - interval '1 month'),'YYYY-MM') || '-01') - 1), 0) AS avg_monthly_sales_11, "
        + "          COALESCE(sre_product_avg_monthly_sales(?, ?, ?, "
        + "                                        DATE(to_char((TO_DATE(?) - interval '1 month'),'YYYY-MM') || '-01'), "
        + "                                        DATE(to_char((TO_DATE(?) - interval '0 month'),'YYYY-MM') || '-01') - 1), 0) AS avg_monthly_sales_12 ";

    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);

    long countRecord = 0;
    int countColumn = 0, numberOfMonths = new Integer(numMonths).intValue();
    Product product = OBDal.getInstance().get(Product.class, mProductId);
    if (product != null) {
      try {
        Query sqlQuery = OBDal.getInstance().getSession().createSQLQuery(strSql);
        sqlQuery.setParameter(0, adOrgId);
        sqlQuery.setParameter(1, adClientId);
        sqlQuery.setParameter(2, product.getId());
        sqlQuery.setParameter(3, docDate);
        sqlQuery.setParameter(4, docDate);
        sqlQuery.setParameter(5, adOrgId);
        sqlQuery.setParameter(6, adClientId);
        sqlQuery.setParameter(7, product.getId());
        sqlQuery.setParameter(8, docDate);
        sqlQuery.setParameter(9, docDate);
        sqlQuery.setParameter(10, adOrgId);
        sqlQuery.setParameter(11, adClientId);
        sqlQuery.setParameter(12, product.getId());
        sqlQuery.setParameter(13, docDate);
        sqlQuery.setParameter(14, docDate);
        sqlQuery.setParameter(15, adOrgId);
        sqlQuery.setParameter(16, adClientId);
        sqlQuery.setParameter(17, product.getId());
        sqlQuery.setParameter(18, docDate);
        sqlQuery.setParameter(19, docDate);
        sqlQuery.setParameter(20, adOrgId);
        sqlQuery.setParameter(21, adClientId);
        sqlQuery.setParameter(22, product.getId());
        sqlQuery.setParameter(23, docDate);
        sqlQuery.setParameter(24, docDate);
        sqlQuery.setParameter(25, adOrgId);
        sqlQuery.setParameter(26, adClientId);
        sqlQuery.setParameter(27, product.getId());
        sqlQuery.setParameter(28, docDate);
        sqlQuery.setParameter(29, docDate);
        sqlQuery.setParameter(30, adOrgId);
        sqlQuery.setParameter(31, adClientId);
        sqlQuery.setParameter(32, product.getId());
        sqlQuery.setParameter(33, docDate);
        sqlQuery.setParameter(34, docDate);
        sqlQuery.setParameter(35, adOrgId);
        sqlQuery.setParameter(36, adClientId);
        sqlQuery.setParameter(37, product.getId());
        sqlQuery.setParameter(38, docDate);
        sqlQuery.setParameter(39, docDate);
        sqlQuery.setParameter(40, adOrgId);
        sqlQuery.setParameter(41, adClientId);
        sqlQuery.setParameter(42, product.getId());
        sqlQuery.setParameter(43, docDate);
        sqlQuery.setParameter(44, docDate);
        sqlQuery.setParameter(45, adOrgId);
        sqlQuery.setParameter(46, adClientId);
        sqlQuery.setParameter(47, product.getId());
        sqlQuery.setParameter(48, docDate);
        sqlQuery.setParameter(49, docDate);
        sqlQuery.setParameter(50, adOrgId);
        sqlQuery.setParameter(51, adClientId);
        sqlQuery.setParameter(52, product.getId());
        sqlQuery.setParameter(53, docDate);
        sqlQuery.setParameter(54, docDate);
        sqlQuery.setParameter(55, adOrgId);
        sqlQuery.setParameter(56, adClientId);
        sqlQuery.setParameter(57, product.getId());
        sqlQuery.setParameter(58, docDate);
        sqlQuery.setParameter(59, docDate);

        List<Object> productdata = sqlQuery.list();
        for (int k = 0; k < productdata.size(); k++) {
          Object[] obj = (Object[]) productdata.get(k);
          countRecord++;
          countColumn = 0;

          GenerateReportRepositionTraceData objectGenerateReportRepositionTraceData = new GenerateReportRepositionTraceData();
          objectGenerateReportRepositionTraceData.productid = product.getId();
          objectGenerateReportRepositionTraceData.searchkey = product.getSearchKey();
          objectGenerateReportRepositionTraceData.internalcode = (product.getScrInternalcode() != null) ? product
              .getScrInternalcode() : "--";
          objectGenerateReportRepositionTraceData.name = product.getName();

          if (countColumn < numberOfMonths) {
            objectGenerateReportRepositionTraceData.avgmonthlysales12 = ((BigDecimal) obj[11])
                .setScale(3, BigDecimal.ROUND_HALF_UP);
          } else {
            objectGenerateReportRepositionTraceData.avgmonthlysales12 = new BigDecimal(-1);
          }
          countColumn++;

          if (countColumn < numberOfMonths) {
            objectGenerateReportRepositionTraceData.avgmonthlysales11 = ((BigDecimal) obj[10])
                .setScale(3, BigDecimal.ROUND_HALF_UP);
          } else {
            objectGenerateReportRepositionTraceData.avgmonthlysales11 = new BigDecimal(-1);
          }
          countColumn++;

          if (countColumn < numberOfMonths) {
            objectGenerateReportRepositionTraceData.avgmonthlysales10 = ((BigDecimal) obj[9])
                .setScale(3, BigDecimal.ROUND_HALF_UP);
          } else {
            objectGenerateReportRepositionTraceData.avgmonthlysales10 = new BigDecimal(-1);
          }
          countColumn++;

          if (countColumn < numberOfMonths) {
            objectGenerateReportRepositionTraceData.avgmonthlysales9 = ((BigDecimal) obj[8])
                .setScale(3, BigDecimal.ROUND_HALF_UP);
          } else {
            objectGenerateReportRepositionTraceData.avgmonthlysales9 = new BigDecimal(-1);
          }
          countColumn++;

          if (countColumn < numberOfMonths) {
            objectGenerateReportRepositionTraceData.avgmonthlysales8 = ((BigDecimal) obj[7])
                .setScale(3, BigDecimal.ROUND_HALF_UP);
          } else {
            objectGenerateReportRepositionTraceData.avgmonthlysales8 = new BigDecimal(-1);
          }
          countColumn++;

          if (countColumn < numberOfMonths) {
            objectGenerateReportRepositionTraceData.avgmonthlysales7 = ((BigDecimal) obj[6])
                .setScale(3, BigDecimal.ROUND_HALF_UP);
          } else {
            objectGenerateReportRepositionTraceData.avgmonthlysales7 = new BigDecimal(-1);
          }
          countColumn++;

          if (countColumn < numberOfMonths) {
            objectGenerateReportRepositionTraceData.avgmonthlysales6 = ((BigDecimal) obj[5])
                .setScale(3, BigDecimal.ROUND_HALF_UP);
          } else {
            objectGenerateReportRepositionTraceData.avgmonthlysales6 = new BigDecimal(-1);
          }
          countColumn++;

          if (countColumn < numberOfMonths) {
            objectGenerateReportRepositionTraceData.avgmonthlysales5 = ((BigDecimal) obj[4])
                .setScale(3, BigDecimal.ROUND_HALF_UP);
          } else {
            objectGenerateReportRepositionTraceData.avgmonthlysales5 = new BigDecimal(-1);
          }
          countColumn++;

          if (countColumn < numberOfMonths) {
            objectGenerateReportRepositionTraceData.avgmonthlysales4 = ((BigDecimal) obj[3])
                .setScale(3, BigDecimal.ROUND_HALF_UP);
          } else {
            objectGenerateReportRepositionTraceData.avgmonthlysales4 = new BigDecimal(-1);
          }
          countColumn++;

          if (countColumn < numberOfMonths) {
            objectGenerateReportRepositionTraceData.avgmonthlysales3 = ((BigDecimal) obj[2])
                .setScale(3, BigDecimal.ROUND_HALF_UP);
          } else {
            objectGenerateReportRepositionTraceData.avgmonthlysales3 = new BigDecimal(-1);
          }
          countColumn++;

          if (countColumn < numberOfMonths) {
            objectGenerateReportRepositionTraceData.avgmonthlysales2 = ((BigDecimal) obj[1])
                .setScale(3, BigDecimal.ROUND_HALF_UP);
          } else {
            objectGenerateReportRepositionTraceData.avgmonthlysales2 = new BigDecimal(-1);
          }
          countColumn++;

          if (countColumn < numberOfMonths) {
            objectGenerateReportRepositionTraceData.avgmonthlysales1 = ((BigDecimal) obj[0])
                .setScale(3, BigDecimal.ROUND_HALF_UP);
          } else {
            objectGenerateReportRepositionTraceData.avgmonthlysales1 = new BigDecimal(-1);
          }

          objectGenerateReportRepositionTraceData.rownum = Long.toString(countRecord);
          objectGenerateReportRepositionTraceData.InitRecordNumber = Integer.toString(firstRegister);

          vector.addElement(objectGenerateReportRepositionTraceData);
        }

      } catch (Exception ex) {
        log4j.error("Exception in query: " + strSql + "Exception:" + ex);
        throw new ServletException("@CODE=@" + ex.getMessage());
      }
    }

    GenerateReportRepositionTraceData objectGenerateReportRepositionTraceData[] = new GenerateReportRepositionTraceData[vector
        .size()];
    vector.copyInto(objectGenerateReportRepositionTraceData);

    return (objectGenerateReportRepositionTraceData);
  }
  
  public static String selectBpartner(ConnectionProvider connectionProvider, String cBpartnerId)
	      throws ServletException {
	    String strSql = "";
	    strSql = strSql + "      SELECT C_BPARTNER.TAXID || ' - ' || C_BPARTNER.NAME AS NAME"
	        + "      FROM C_BPARTNER" + "      WHERE C_BPARTNER.C_BPARTNER_ID = ?";

	    ResultSet result;
	    String strReturn = "";
	    PreparedStatement st = null;

	    int iParameter = 0;
	    try {
	      st = connectionProvider.getPreparedStatement(strSql);
	      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
	      iParameter++;
	      UtilSql.setValue(st, iParameter, 12, null, cBpartnerId);

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


  public static GenerateReportRepositionTraceData[] set() throws ServletException {
	  GenerateReportRepositionTraceData objectGenerateReportRepositionTraceData[] = new GenerateReportRepositionTraceData[1];
    objectGenerateReportRepositionTraceData[0] = new GenerateReportRepositionTraceData();
    objectGenerateReportRepositionTraceData[0].productid = "";
    objectGenerateReportRepositionTraceData[0].searchkey = "";
    objectGenerateReportRepositionTraceData[0].internalcode = "";
    objectGenerateReportRepositionTraceData[0].name = "";
    objectGenerateReportRepositionTraceData[0].prdID = "";
    objectGenerateReportRepositionTraceData[0].transactionID = "";
    objectGenerateReportRepositionTraceData[0].movementDate = "";
    objectGenerateReportRepositionTraceData[0].warehousename = "";
    objectGenerateReportRepositionTraceData[0].storagebin = "";
    objectGenerateReportRepositionTraceData[0].movementqty = "";
    objectGenerateReportRepositionTraceData[0].movementqtynegative = "";
    objectGenerateReportRepositionTraceData[0].shipmentid = "";
    objectGenerateReportRepositionTraceData[0].inventoryid = "";
    objectGenerateReportRepositionTraceData[0].movementid = "";
    objectGenerateReportRepositionTraceData[0].shipmentline = "";
    objectGenerateReportRepositionTraceData[0].inventoryline = "";
    objectGenerateReportRepositionTraceData[0].movementline = "";
    objectGenerateReportRepositionTraceData[0].productionline = "";
    objectGenerateReportRepositionTraceData[0].productionid = "";
    
    
    objectGenerateReportRepositionTraceData[0].ioid = "";
    objectGenerateReportRepositionTraceData[0].bpname = "";
    objectGenerateReportRepositionTraceData[0].shipnum = "";
    objectGenerateReportRepositionTraceData[0].shipphysical = "";
    objectGenerateReportRepositionTraceData[0].orderid = "";
    objectGenerateReportRepositionTraceData[0].ordernum = "";
    objectGenerateReportRepositionTraceData[0].clientid = "";
    objectGenerateReportRepositionTraceData[0].shipmentdate = "";
    
    objectGenerateReportRepositionTraceData[0].avgmonthlysales1 = new BigDecimal(-1);
    objectGenerateReportRepositionTraceData[0].avgmonthlysales2 = new BigDecimal(-1);
    objectGenerateReportRepositionTraceData[0].avgmonthlysales3 = new BigDecimal(-1);
    objectGenerateReportRepositionTraceData[0].avgmonthlysales4 = new BigDecimal(-1);
    objectGenerateReportRepositionTraceData[0].avgmonthlysales5 = new BigDecimal(-1);
    objectGenerateReportRepositionTraceData[0].avgmonthlysales6 = new BigDecimal(-1);
    objectGenerateReportRepositionTraceData[0].avgmonthlysales7 = new BigDecimal(-1);
    objectGenerateReportRepositionTraceData[0].avgmonthlysales8 = new BigDecimal(-1);
    objectGenerateReportRepositionTraceData[0].avgmonthlysales9 = new BigDecimal(-1);
    objectGenerateReportRepositionTraceData[0].avgmonthlysales10 = new BigDecimal(-1);
    objectGenerateReportRepositionTraceData[0].avgmonthlysales11 = new BigDecimal(-1);
    objectGenerateReportRepositionTraceData[0].avgmonthlysales12 = new BigDecimal(-1);
    return objectGenerateReportRepositionTraceData;
    
  }

  public static String selectMproduct(ConnectionProvider connectionProvider, String mProductId)
      throws ServletException {
    String strSql = "";
    strSql = strSql + "      SELECT M_PRODUCT.NAME" + "      FROM M_PRODUCT"
        + "      WHERE M_PRODUCT.M_PRODUCT_ID = ?";

    ResultSet result;
    String strReturn = "";
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, mProductId);

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

  public static int getDifferenceBetwwenDatesInMonths(String startDate, String endDate) {
    String[] startDateSplitted = startDate.split("-");
    String[] endDateSplitted = endDate.split("-");

    Date start_date = new Date();
    Date end_date = new Date();

    start_date.setDate(Integer.parseInt(startDateSplitted[0]));
    start_date.setMonth(Integer.parseInt(startDateSplitted[1]));
    start_date.setYear(Integer.parseInt(startDateSplitted[2]));

    end_date.setDate(Integer.parseInt(endDateSplitted[0]));
    end_date.setMonth(Integer.parseInt(endDateSplitted[1]));
    end_date.setYear(Integer.parseInt(endDateSplitted[2]));

    // calculating the difference
    Calendar startCalendar = new GregorianCalendar();
    startCalendar.setTime(start_date);
    Calendar endCalendar = new GregorianCalendar();
    endCalendar.setTime(end_date);

    int diffYear = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
    int diffMonth = diffYear * 12 + endCalendar.get(Calendar.MONTH)
        - startCalendar.get(Calendar.MONTH);

    return diffMonth;
  }

  public static int getDifferenceBetwwenDatesInDays(Date start_date, Date end_date) {
    final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;

    int diffInDays = (int) ((end_date.getTime() - start_date.getTime() + 1) / DAY_IN_MILLIS);

    if (diffInDays < 0)
      return 0;
    return diffInDays;
  }
  
  public static GenerateReportRepositionTraceData[] selectWarehouseDouble(
	      ConnectionProvider connectionProvider, String adUserClient) throws ServletException {
	    return selectWarehouseDouble(connectionProvider, adUserClient, 0, 0);
	  }

	  public static GenerateReportRepositionTraceData[] selectWarehouseDouble(
	      ConnectionProvider connectionProvider, String adUserClient, int firstRegister,
	      int numberRegisters) throws ServletException {
	    String strSql = "";
	    strSql = strSql
	        + "        SELECT M_WAREHOUSE.AD_ORG_ID AS PADRE, M_WAREHOUSE.M_WAREHOUSE_ID AS ID, TO_CHAR(M_WAREHOUSE.NAME) AS NAME"
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
	        GenerateReportRepositionTraceData objectGenerateReportRepositionTraceData = new GenerateReportRepositionTraceData();
	        objectGenerateReportRepositionTraceData.padre = UtilSql.getValue(result, "padre");
	        objectGenerateReportRepositionTraceData.id = UtilSql.getValue(result, "id");
	        objectGenerateReportRepositionTraceData.name = UtilSql.getValue(result, "name");
	        objectGenerateReportRepositionTraceData.InitRecordNumber = Integer.toString(firstRegister);
	        vector.addElement(objectGenerateReportRepositionTraceData);
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
	    GenerateReportRepositionTraceData objectGenerateReportRepositionTraceData[] = new GenerateReportRepositionTraceData[vector
	        .size()];
	    vector.copyInto(objectGenerateReportRepositionTraceData);
	    return (objectGenerateReportRepositionTraceData);
	  }
  
}
