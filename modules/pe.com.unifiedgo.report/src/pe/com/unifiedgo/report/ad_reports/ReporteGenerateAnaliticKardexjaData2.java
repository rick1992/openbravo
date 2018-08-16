////Sqlc generated V1.O00-1asdfd
//package pe.com.unifiedgo.report.ad_reports;
//
//import java.math.BigDecimal;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.GregorianCalendar;
//import java.util.Vector;
//
//import javax.servlet.ServletException;
//
//import org.apache.log4j.Logger;
//import org.openbravo.data.FieldProvider;
//import org.openbravo.data.UtilSql;
//import org.openbravo.database.ConnectionProvider;
//import org.openbravo.database.SessionInfo;
//import org.openbravo.service.db.QueryTimeOutUtil;
//
//class ReporteGenerateAnaliticKardexData2 implements FieldProvider {
//  static Logger log4j = Logger.getLogger(ReporteGenerateAnaliticKardexData.class);
//  private String InitRecordNumber = "0";
//  public String productid;
//  public String searchkey;
//  public String internalcode;
//  public String name;
//  public String padre;
//  public String id;
//
//  public String transactionID;
//  public String movementDate;
//  public String movementHour;
//  public String warehousename;
//  public String storagebin;
//  public String movementqty;
//  public String movementqtynegative;
//  public String movementtype;
//  public String shipmentid;
//  public String inventoryid;
//  public String movementid;
//  public String productionid;
//  public String shipmentline;
//  public String inventoryline;
//  public String movementline;
//  public String productionline;
//  public String warehousedata;
//  public String productdata;
//  public String qtyreserved;
//  public String shipmentlinespecialDT;
//
//  public String prdID;
//
//  public String partner;
//  
//  public String movementqtyInitial;
//  public String prdInitialID;
//
//  public String movementqtyFinal;
//  public String prdFinalID;
//
//  public String rownum;
//
//  public String getInitRecordNumber() {
//    return InitRecordNumber;
//  }
//
//  @Override
//  public String getField(String fieldName) {
//    if (fieldName.equalsIgnoreCase("productid"))
//      return productid;
//    else if (fieldName.equalsIgnoreCase("movementqtyFinal"))
//      return movementqtyFinal;
//    else if (fieldName.equalsIgnoreCase("id"))
//      return id;
//    else if (fieldName.equalsIgnoreCase("padre"))
//      return padre;
//    else if (fieldName.equalsIgnoreCase("prdFinalID"))
//      return prdFinalID;
//    else if (fieldName.equalsIgnoreCase("movementqtyInitial"))
//      return movementqtyInitial;
//    else if (fieldName.equalsIgnoreCase("prdInitialID"))
//      return prdInitialID;
//    else if (fieldName.equalsIgnoreCase("shipmentline"))
//      return shipmentline;
//    else if (fieldName.equalsIgnoreCase("inventoryline"))
//      return inventoryline;
//    else if (fieldName.equalsIgnoreCase("movementline"))
//      return movementline;
//    else if (fieldName.equalsIgnoreCase("productionline"))
//      return productionline;
//    else if (fieldName.equalsIgnoreCase("prdID"))
//      return prdID;
//    else if (fieldName.equalsIgnoreCase("transactionID"))
//      return transactionID;
//    else if (fieldName.equalsIgnoreCase("movementDate"))
//      return movementDate;
//    else if (fieldName.equalsIgnoreCase("movementHour"))
//        return movementHour;
//    else if (fieldName.equalsIgnoreCase("warehousename"))
//      return warehousename;
//    else if (fieldName.equalsIgnoreCase("storagebin"))
//      return storagebin;
//    else if (fieldName.equalsIgnoreCase("movementqty"))
//      return movementqty;
//    else if (fieldName.equalsIgnoreCase("movementqtynegative"))
//      return movementqtynegative;
//    else if (fieldName.equalsIgnoreCase("movementtype"))
//      return movementtype;
//    else if (fieldName.equalsIgnoreCase("shipmentid"))
//      return shipmentid;
//    else if (fieldName.equalsIgnoreCase("inventoryid"))
//      return inventoryid;
//    else if (fieldName.equalsIgnoreCase("movementid"))
//      return movementid;
//    else if (fieldName.equalsIgnoreCase("productionid"))
//      return productionid;
//    else if (fieldName.equalsIgnoreCase("searchkey"))
//      return searchkey;
//    else if (fieldName.equalsIgnoreCase("internalcode"))
//      return internalcode;
//    else if (fieldName.equalsIgnoreCase("name"))
//      return name;
//    else if (fieldName.equalsIgnoreCase("partner"))
//        return partner;
//    else if (fieldName.equalsIgnoreCase("warehousedata"))
//        return warehousedata;
//    else if (fieldName.equalsIgnoreCase("productdata")) 
//        return productdata;
//    else if (fieldName.equalsIgnoreCase("qtyreserved"))
//        return qtyreserved; 
//    else if (fieldName.equalsIgnoreCase("shipmentlinespecialDT"))
//        return shipmentlinespecialDT;
//    else if (fieldName.equals("rownum"))
//      return rownum;
//    else {
//      log4j.debug("Field does not exist: " + fieldName);
//      return null;
//    }
//  }
//  
//  public static ReporteGenerateAnaliticKardexData[] selectDataGeneral(
//	      ConnectionProvider connectionProvider, String adOrgId, String adClientId, String mProductId,
//	      String docDate, String DateTo, String warehouse_id) throws ServletException {
//	    return selectDataGeneral(connectionProvider, adOrgId, adClientId, mProductId, docDate, null, 0,
//	        0, DateTo, warehouse_id);
//	  }
//  
//  public static ReporteGenerateAnaliticKardexData[] selectDataGeneral(
//	      ConnectionProvider connectionProvider, String adOrgId, String adClientId, String mProductId,
//	      String docDate, String numMonths, int firstRegister, int numberRegisters, String DateTo,
//	      String warehouse_id) throws ServletException {
//
//	  
//	    String strSql = "";
//	    strSql = strSql + " select (w.value || ' - ' || w.name) as warehousedata, "
//	        + "  (p.value || ' - ' || p.name) as productdata "
//	    	+ "   from m_warehouse w , m_product p "
//	        + "  where m_warehouse_id = ? "
//	        + "  and m_product_id = ? limit 1 ";
//	    
//        ResultSet result;
//	    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
//	    PreparedStatement st = null;
//
//	    int iParameter = 0;
//	    try {
//	      st = connectionProvider.getPreparedStatement(strSql);
//          
//	      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
//	      iParameter++;
//	      UtilSql.setValue(st, iParameter, 12, null, warehouse_id);
//	      iParameter++;
//	      UtilSql.setValue(st, iParameter, 12, null, mProductId);
//	      result = st.executeQuery();
//
//	      long countRecord = 0;
//	      long countRecordSkip = 1;
//	      boolean continueResult = true;
//	      while (countRecordSkip < firstRegister && continueResult) {
//	        continueResult = result.next();
//	        countRecordSkip++;
//	      }
//	      while (continueResult && result.next()) {
//	        countRecord++;
//	        ReporteGenerateAnaliticKardexData objectReporteGenerateAnaliticKardexData = new ReporteGenerateAnaliticKardexData();
//	        objectReporteGenerateAnaliticKardexData.warehousedata = UtilSql.getValue(result, "warehousedata");
//	        objectReporteGenerateAnaliticKardexData.productdata = UtilSql.getValue(result, "productdata");
//	        objectReporteGenerateAnaliticKardexData.rownum = Long.toString(countRecord);
//	        objectReporteGenerateAnaliticKardexData.InitRecordNumber = Integer.toString(firstRegister);
//
//	        vector.addElement(objectReporteGenerateAnaliticKardexData);
//	        if (countRecord >= numberRegisters && numberRegisters != 0) {
//	          continueResult = false;
//	        }
//	      }
//	      result.close();
//	    } catch (SQLException e) {
//	      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
//	      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
//	          + e.getMessage());
//	    } catch (Exception ex) {
//	      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
//	      throw new ServletException("@CODE=@" + ex.getMessage());
//	    } finally {
//	      try {
//	        connectionProvider.releasePreparedStatement(st);
//	      } catch (Exception ignore) {
//	        ignore.printStackTrace();
//	      }
//	    }
//	    ReporteGenerateAnaliticKardexData objectReporteGenerateAnaliticKardexData[] = new ReporteGenerateAnaliticKardexData[vector
//	        .size()];
//	    vector.copyInto(objectReporteGenerateAnaliticKardexData);
//	    return (objectReporteGenerateAnaliticKardexData);
//	  }
//
//  public static ReporteGenerateAnaliticKardexData[] selectKardexFinal(
//      ConnectionProvider connectionProvider, String adOrgId, String adClientId, String mProductId,
//      String docDate, String DateTo, String warehouse_id) throws ServletException {
//    return selectKardexFinal(connectionProvider, adOrgId, adClientId, mProductId, docDate, null, 0,
//        0, DateTo, warehouse_id);
//  }
//
//  public static ReporteGenerateAnaliticKardexData[] selectKardexFinal(
//      ConnectionProvider connectionProvider, String adOrgId, String adClientId, String mProductId,
//      String docDate, String numMonths, int firstRegister, int numberRegisters, String DateTo,
//      String warehouse_id) throws ServletException {
//
//    String strSql = "";
//    strSql = strSql + " SELECT mp.m_product_id AS FINALID, "
//        + "  (SELECT coalesce(sum(mt.movementqty),0) " + "     FROM M_Transaction mt"
//        + "          LEFT JOIN m_locator ml "
//        + "                    ON ml.m_locator_id =  mt.m_locator_id "
//        + "          LEFT JOIN m_warehouse mw "
//        + "                    ON mw.m_warehouse_id = ml.m_warehouse_id "
//        + "         WHERE mt.m_product_id = ?"
//        + "         AND AD_ISORGINCLUDED(mt.ad_org_id, ?, ?) > -1"
//        + "         AND mt.movementdate < to_date(?, 'dd-MM-yyyy')";
//    if (warehouse_id != null && !warehouse_id.isEmpty()) {
//      strSql = strSql + "     AND mw.m_warehouse_id=?";
//    }
//    strSql = strSql
//        + "    ) +"
//        + " coalesce(QTYFINAL2,0) AS QTYFINAL "
//        + " FROM m_product mp "
//        + " LEFT JOIN ("
//        + " SELECT   mt.m_product_id as FINALID2,"
//        // +"         (SELECT coalesce(sum(mt.movementqty),0)"
//        // +"         FROM M_Transaction mt"
//        // +"         WHERE mt.m_product_id = ?"
//        // //+"         AND mt.ad_org_id = ?"
//        // +"         AND AD_ISORGINCLUDED(mt.ad_org_id, ?, ?) > -1"
//        // +"         AND mt.movementdate < to_date(?, 'dd-MM-yyyy')"
//        // +"	       ) + coalesce(sum(mt.movementqty),0) AS QTYFINAL2"
//        + "   coalesce(sum(mt.movementqty),0) AS QTYFINAL2 " + "	 FROM M_Transaction mt"
//        + "        LEFT JOIN m_locator ml "
//        + "              ON ml.m_locator_id =  mt.m_locator_id  "
//        + "        LEFT JOIN m_warehouse mw "
//        + "              ON mw.m_warehouse_id = ml.m_warehouse_id " + "	 WHERE mt.m_product_id = ?"
//        // +"	   AND mt.ad_org_id = ?"
//        + "         AND AD_ISORGINCLUDED(mt.ad_org_id, ?, ?) > -1"
//        + "	   AND mt.movementdate between ? AND ?";
//    if (warehouse_id != null && !warehouse_id.isEmpty()) {
//      strSql = strSql + "     AND mw.m_warehouse_id=?";
//    }
//    strSql = strSql + "	  GROUP BY mt.m_product_id" + "  ) trx ON mp.m_product_id = trx.FINALID2"
//        + " WHERE mp.m_product_id = ?";
//
//    ResultSet result;
//    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
//    PreparedStatement st = null;
//
//    int iParameter = 0;
//    try {
//      st = connectionProvider.getPreparedStatement(strSql);
//
//      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
//      iParameter++;
//      UtilSql.setValue(st, iParameter, 12, null, mProductId);
//      iParameter++;
//      UtilSql.setValue(st, iParameter, 12, null, adOrgId);
//      iParameter++;
//      UtilSql.setValue(st, iParameter, 12, null, adClientId);
//      iParameter++;
//      UtilSql.setValue(st, iParameter, 12, null, docDate);
//      if (warehouse_id != null && !warehouse_id.isEmpty()) {
//        iParameter++;
//        UtilSql.setValue(st, iParameter, 12, null, warehouse_id);
//      }
//      iParameter++;
//      UtilSql.setValue(st, iParameter, 12, null, mProductId);
//      iParameter++;
//      UtilSql.setValue(st, iParameter, 12, null, adOrgId);
//      iParameter++;
//      UtilSql.setValue(st, iParameter, 12, null, adClientId);
//      iParameter++;
//      UtilSql.setValue(st, iParameter, 12, null, docDate);
//      iParameter++;
//      UtilSql.setValue(st, iParameter, 12, null, DateTo);
//      if (warehouse_id != null && !warehouse_id.isEmpty()) {
//        iParameter++;
//        UtilSql.setValue(st, iParameter, 12, null, warehouse_id);
//      }
//      iParameter++;
//      UtilSql.setValue(st, iParameter, 12, null, mProductId);
//      result = st.executeQuery();
//
//      long countRecord = 0;
//      long countRecordSkip = 1;
//      boolean continueResult = true;
//      while (countRecordSkip < firstRegister && continueResult) {
//        continueResult = result.next();
//        countRecordSkip++;
//      }
//      while (continueResult && result.next()) {
//        countRecord++;
//        ReporteGenerateAnaliticKardexData objectReporteGenerateAnaliticKardexData = new ReporteGenerateAnaliticKardexData();
//        objectReporteGenerateAnaliticKardexData.prdFinalID = UtilSql.getValue(result, "FINALID");
//        objectReporteGenerateAnaliticKardexData.movementqtyFinal = UtilSql.getValue(result, "QTYFINAL");
//        objectReporteGenerateAnaliticKardexData.rownum = Long.toString(countRecord);
//        objectReporteGenerateAnaliticKardexData.InitRecordNumber = Integer.toString(firstRegister);
//
//        vector.addElement(objectReporteGenerateAnaliticKardexData);
//        if (countRecord >= numberRegisters && numberRegisters != 0) {
//          continueResult = false;
//        }
//      }
//      result.close();
//    } catch (SQLException e) {
//      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
//      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
//          + e.getMessage());
//    } catch (Exception ex) {
//      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
//      throw new ServletException("@CODE=@" + ex.getMessage());
//    } finally {
//      try {
//        connectionProvider.releasePreparedStatement(st);
//      } catch (Exception ignore) {
//        ignore.printStackTrace();
//      }
//    }
//    ReporteGenerateAnaliticKardexData objectReporteGenerateAnaliticKardexData[] = new ReporteGenerateAnaliticKardexData[vector
//        .size()];
//    vector.copyInto(objectReporteGenerateAnaliticKardexData);
//    return (objectReporteGenerateAnaliticKardexData);
//  }
//
//  public static ReporteGenerateAnaliticKardexData[] selectKardexInitial(
//      ConnectionProvider connectionProvider, String adOrgId, String adClientId, String mProductId,
//      String docDate, String DateTo, String warehouse_id) throws ServletException {
//    return selectKardexInitial(connectionProvider, adOrgId, adClientId, mProductId, docDate, null,
//        0, 0, DateTo, warehouse_id);
//  }
//
//  public static ReporteGenerateAnaliticKardexData[] selectKardexInitial(
//      ConnectionProvider connectionProvider, String adOrgId, String adClientId, String mProductId,
//      String docDate, String numMonths, int firstRegister, int numberRegisters, String DateTo,
//      String warehouse_id) throws ServletException {
//	  
//    String strSql = "";
//    strSql = strSql + " SELECT mp.m_product_id AS INITIALID, coalesce(trx.QTY2POS-trx.QTY2NEG,0) AS QTYINIT,   "
//    	+ " coalesce(trx.QTY2POS,0) AS QTY, coalesce(trx.QTY2NEG,0) AS QTYNEGATIVE "
//        + " FROM m_product mp " + " LEFT JOIN ( " + " SELECT   mt.m_product_id  AS INITI2, "
//        + "         coalesce(sum( CASE WHEN (mt.movementqty)>= 0 THEN mt.movementqty ELSE 0 END ),0) AS QTY2POS, "
//        + "         coalesce(sum( CASE WHEN (mt.movementqty)< 0 THEN -mt.movementqty ELSE 0 END ),0) AS QTY2NEG "
//        + "	 FROM M_Transaction mt "
//        + "        LEFT JOIN m_locator ml " + "         ON ml.m_locator_id =  mt.m_locator_id "
//        + "        LEFT JOIN m_warehouse mw " + "        ON mw.m_warehouse_id = ml.m_warehouse_id "
//        + "	 WHERE mt.m_product_id = ? "
//        // +"	   AND mt.ad_org_id = ? "
//        + "     AND AD_ISORGINCLUDED(mt.ad_org_id, ?, ?) > -1"
//        + "	   AND mt.movementdate < to_date(?, 'dd-MM-yyyy') ";
//    if (warehouse_id != null && !warehouse_id.isEmpty()) {
//      strSql = strSql + "     AND mw.m_warehouse_id=?";
//    }
//    strSql = strSql + "	  GROUP BY mt.m_product_id) trx " + " ON mp.m_product_id = trx.INITI2 "
//        + " WHERE mp.m_product_id = ?";
//    // Falta PONER WHERE CLIENTE
//    ResultSet result;
//    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
//    PreparedStatement st = null;
//
//    int iParameter = 0;
//    try {
//      st = connectionProvider.getPreparedStatement(strSql);
//      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
//      iParameter++;
//      UtilSql.setValue(st, iParameter, 12, null, mProductId);
//      iParameter++;
//      UtilSql.setValue(st, iParameter, 12, null, adOrgId);
//      iParameter++;
//      UtilSql.setValue(st, iParameter, 12, null, adClientId);
//      iParameter++;
//      UtilSql.setValue(st, iParameter, 12, null, docDate);
//      if (warehouse_id != null && !warehouse_id.isEmpty()) {
//        iParameter++;
//        UtilSql.setValue(st, iParameter, 12, null, warehouse_id);
//      }
//      iParameter++;
//      UtilSql.setValue(st, iParameter, 12, null, mProductId);
//      result = st.executeQuery();
//
//      long countRecord = 0;
//      long countRecordSkip = 1;
//      boolean continueResult = true;
//      while (countRecordSkip < firstRegister && continueResult) {
//        continueResult = result.next();
//        countRecordSkip++;
//      }
//      while (continueResult && result.next()) {
//        countRecord++;
//        ReporteGenerateAnaliticKardexData objectReporteGenerateAnaliticKardexData = new ReporteGenerateAnaliticKardexData();
//        objectReporteGenerateAnaliticKardexData.prdInitialID = UtilSql.getValue(result, "INITIALID");
//        objectReporteGenerateAnaliticKardexData.movementqtyInitial = UtilSql.getValue(result, "QTYINIT");
//        objectReporteGenerateAnaliticKardexData.rownum = Long.toString(countRecord);
//        objectReporteGenerateAnaliticKardexData.InitRecordNumber = Integer.toString(firstRegister);
//
//        objectReporteGenerateAnaliticKardexData.movementqty = UtilSql.getValue(result, "QTY");
//        objectReporteGenerateAnaliticKardexData.movementqtynegative = UtilSql.getValue(result, "QTYNEGATIVE");
//        vector.addElement(objectReporteGenerateAnaliticKardexData);
//        if (countRecord >= numberRegisters && numberRegisters != 0) {
//          continueResult = false;
//        }
//      }
//      result.close();
//    } catch (SQLException e) {
//      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
//      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
//          + e.getMessage());
//    } catch (Exception ex) {
//      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
//      throw new ServletException("@CODE=@" + ex.getMessage());
//    } finally {
//      try {
//        connectionProvider.releasePreparedStatement(st);
//      } catch (Exception ignore) {
//        ignore.printStackTrace();
//      }
//    }
//    ReporteGenerateAnaliticKardexData objectReporteGenerateAnaliticKardexData[] = new ReporteGenerateAnaliticKardexData[vector
//        .size()];
//    vector.copyInto(objectReporteGenerateAnaliticKardexData);
//    return (objectReporteGenerateAnaliticKardexData);
//  }
//  
//  public static ReporteGenerateAnaliticKardexData[] selectStockReservado(ConnectionProvider connectionProvider,
//	      String adOrgId, String adClientId, String mProductId, String docDate, String DateTo,
//	      String warehouse_id) throws ServletException {
//	    return selectStockReservado(connectionProvider, adOrgId, adClientId, mProductId, docDate, null, 0, 0,
//	        DateTo, warehouse_id);
//  }
//
//  public static ReporteGenerateAnaliticKardexData[] selectStockReservado(
//	      ConnectionProvider connectionProvider, String adOrgId, String adClientId, String mProductId,
//	      String docDate, String numMonths, int firstRegister, int numberRegisters, String DateTo,
//	      String warehouse_id) throws ServletException {
//
//	  
//	    String strSql = "";
//	    strSql = strSql + " select sum(qtyreserved) as qtyreserved from m_product_warehouse_qtys_v "
//	        + "  where m_warehouse_id = ? "
//	        + "  and m_product_id = ? limit 1 ";
//	    
//        ResultSet result;
//	    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
//	    PreparedStatement st = null;
//
//	    int iParameter = 0;
//	    try {
//	      st = connectionProvider.getPreparedStatement(strSql);
//          
//	      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
//	      iParameter++;
//	      UtilSql.setValue(st, iParameter, 12, null, warehouse_id);
//	      iParameter++;
//	      UtilSql.setValue(st, iParameter, 12, null, mProductId);
//	      result = st.executeQuery();
//
//	      long countRecord = 0;
//	      long countRecordSkip = 1;
//	      boolean continueResult = true;
//	      while (countRecordSkip < firstRegister && continueResult) {
//	        continueResult = result.next();
//	        countRecordSkip++;
//	      }
//	      while (continueResult && result.next()) {
//	        countRecord++;
//	        ReporteGenerateAnaliticKardexData objectReporteGenerateAnaliticKardexData = new ReporteGenerateAnaliticKardexData();
//	        objectReporteGenerateAnaliticKardexData.qtyreserved  = UtilSql.getValue(result, "qtyreserved");
//	        objectReporteGenerateAnaliticKardexData.rownum = Long.toString(countRecord);
//	        objectReporteGenerateAnaliticKardexData.InitRecordNumber = Integer.toString(firstRegister);
//
//	        vector.addElement(objectReporteGenerateAnaliticKardexData);
//	        if (countRecord >= numberRegisters && numberRegisters != 0) {
//	          continueResult = false;
//	        }
//	      }
//	      result.close();
//	    } catch (SQLException e) {
//	      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
//	      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
//	          + e.getMessage());
//	    } catch (Exception ex) {
//	      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
//	      throw new ServletException("@CODE=@" + ex.getMessage());
//	    } finally {
//	      try {
//	        connectionProvider.releasePreparedStatement(st);
//	      } catch (Exception ignore) {
//	        ignore.printStackTrace();
//	      }
//	    }
//	    ReporteGenerateAnaliticKardexData objectReporteGenerateAnaliticKardexData[] = new ReporteGenerateAnaliticKardexData[vector
//	        .size()];
//	    vector.copyInto(objectReporteGenerateAnaliticKardexData);
//	    return (objectReporteGenerateAnaliticKardexData);
//	  }
//  
//  
//  public static ReporteGenerateAnaliticKardexData[] selectKardex(ConnectionProvider connectionProvider,
//      String adOrgId, String adClientId, String mProductId, String docDate, String DateTo,
//      String warehouse_id) throws ServletException {
//    return selectKardex(connectionProvider, adOrgId, adClientId, mProductId, docDate, null, 0, 0,
//        DateTo, warehouse_id);
//  }
//
//  public static ReporteGenerateAnaliticKardexData[] selectKardex(ConnectionProvider connectionProvider,
//      String adOrgId, String adClientId, String mProductId, String docDate, String numMonths,
//      int firstRegister, int numberRegisters, String DateTo, String warehouse_id)
//      throws ServletException {
//
//    String strSql = "";
//    strSql = strSql
//    		+ " SELECT   mt.m_product_id as PRODUCTID,"
//            + "         mt.m_transaction_id as ID,"
//            + "	       mt.movementdate as MOVEMENTDATE,"
//            + "        CASE WHEN (mt.m_inoutline_id IS NOT NULL)"
//            + "             THEN "
//            + "              coalesce((SELECT trxprocessdate +interval '0.001' "
//            + "                 FROM swa_transaction_line_void_v "
//            + "                WHERE m_inoutline_id = mt.m_inoutline_id),mt.trxprocessdate) "  
//            + "             ELSE mt.trxprocessdate END AS trxprocessmovement,  "
//            
//            + "        CASE WHEN (mt.m_inoutline_id IS NOT NULL)"
//            + "             THEN "
//            + "              coalesce((SELECT to_char(trxprocessdate, 'hh24:mi:ss') "
//            + "                 FROM swa_transaction_line_void_v "
//            + "                WHERE m_inoutline_id = mt.m_inoutline_id),to_char(mt.trxprocessdate, 'hh24:mi:ss')) "  
//            + "             ELSE to_char(mt.trxprocessdate, 'hh24:mi:ss') END AS MOVEMENTHOUR,  "
//            
//            + "         mw.name AS WAREHOUSENAME,"
//            + " 	       ml.value AS BIN,"
//            + "	       CASE WHEN (mt.movementqty)>= 0 THEN mt.movementqty ELSE NULL END AS QTY,"
//            + "	       CASE WHEN (mt.movementqty)< 0 THEN mt.movementqty*-1 ELSE NULL END AS QTYNEGATIVE,"
//            + "	       ci.name AS MOVEMENTTYPE," + "	       mt.m_inoutline_id AS SHIPMENT,"
//            + "         mi.line  ||  ' - ' ||  io.documentno AS SHIPMENTLINE,"
//            + "	       mt.m_inventoryLine_id AS INVENTORY,"
//            + "         mil.line  ||  ' - ' ||  miv.documentno AS INVENTORYLINE,"
//            + "         mt.m_productionline_id AS PRODUCTION, "
//            + "         mprdl.line || ' - ' || mprd.documentno AS PRODUCTIONLINE, "
//            + " bp.value || ' - ' || bp.name AS PARTNER, "
//            + "         io.em_sco_specialdoctype AS SHIPMENTSDT " 
//            + "	 FROM M_Transaction mt" + "	  LEFT JOIN m_locator ml"
//            + "	          ON ml.m_locator_id =  mt.m_locator_id" + "    LEFT JOIN m_warehouse mw"
//            + "           ON mw.m_warehouse_id = ml.m_warehouse_id" + "	  LEFT JOIN scr_combo_item ci"
//            + "	          ON ci.scr_combo_item_id = mt.em_ssa_combo_item_id"
//            + "	  LEFT JOIN m_product mp" + "	          ON mp.m_product_id = mt.m_product_id"
//            + "	  LEFT JOIN c_uom cu" + "	          ON cu.c_uom_id = mt.c_uom_id"
//            + "     LEFT JOIN m_inoutline mi" + "            ON mi.m_inoutline_id = mt.m_inoutline_id"
//            + "     LEFT JOIN m_inout io" + "		        ON io.m_inout_id = mi.m_inout_id"
//            + "		LEFT JOIN m_inventoryLine mil"
//            + "		        ON mil.m_inventoryLine_id = mt.m_inventoryLine_id"
//            + "		LEFT JOIN m_inventory miv" + "		        ON miv.m_inventory_id = mil.m_inventory_id"
//            + "      LEFT JOIN m_productionline mprdl"
//            + "              ON mprdl.m_productionline_id = mt.m_productionline_id"
//            + "      LEFT JOIN m_productionplan mprdp"
//            + "              ON mprdp.m_productionplan_id = mprdl.m_productionplan_id"
//            + "      LEFT JOIN m_production mprd"
//            + "              ON mprd.m_production_id = mprdp.m_production_id"
//            + "   LEFT JOIN c_bpartner bp ON io.c_bpartner_id=bp.c_bpartner_id "
//            + "	 WHERE mt.m_product_id = ?" + "	   AND mt.m_movementline_id is null"
//            + "  AND AD_ISORGINCLUDED(mt.ad_org_id, ?, ?) > -1"
//            + "	   AND mt.movementdate between ? AND ? ";
//    if (warehouse_id != null && !warehouse_id.isEmpty()) {
//      strSql = strSql + "	   AND mw.m_warehouse_id=? ";
//    }
//    strSql = strSql + "	  ORDER BY mt.movementdate, trxprocessmovement asc";
//    // Falta PONER WHERE CLIENTE
//    ResultSet result;
//    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
//    PreparedStatement st = null;
//
//    int iParameter = 0;
//    try {
//      st = connectionProvider.getPreparedStatement(strSql);
//      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
//      iParameter++;
//      UtilSql.setValue(st, iParameter, 12, null, mProductId);
//      iParameter++;
//      UtilSql.setValue(st, iParameter, 12, null, adOrgId);
//      iParameter++;
//      UtilSql.setValue(st, iParameter, 12, null, adClientId);
//      iParameter++;
//      UtilSql.setValue(st, iParameter, 12, null, docDate);
//      iParameter++;
//      UtilSql.setValue(st, iParameter, 12, null, DateTo);
//      iParameter++;
//      UtilSql.setValue(st, iParameter, 12, null, warehouse_id);
//      result = st.executeQuery();
//      long countRecord = 0;
//      long countRecordSkip = 1;
//      boolean continueResult = true;
//      while (countRecordSkip < firstRegister && continueResult) {
//        continueResult = result.next();
//        countRecordSkip++;
//      }
//      while (continueResult && result.next()) {
//        countRecord++;
//        ReporteGenerateAnaliticKardexData objectReporteGenerateAnaliticKardexData = new ReporteGenerateAnaliticKardexData();
//        objectReporteGenerateAnaliticKardexData.prdID = UtilSql.getValue(result, "PRODUCTID");
//        objectReporteGenerateAnaliticKardexData.transactionID = UtilSql.getValue(result, "ID");
//        objectReporteGenerateAnaliticKardexData.movementDate = UtilSql
//            .getDateValue(result, "MOVEMENTDATE");
//        objectReporteGenerateAnaliticKardexData.movementHour = UtilSql.getValue(result, "MOVEMENTHOUR");
//        objectReporteGenerateAnaliticKardexData.storagebin = UtilSql.getValue(result, "BIN");
//        objectReporteGenerateAnaliticKardexData.warehousename = UtilSql.getValue(result, "WAREHOUSENAME");
//        objectReporteGenerateAnaliticKardexData.movementqty = UtilSql.getValue(result, "QTY");
//        objectReporteGenerateAnaliticKardexData.movementqtynegative = UtilSql.getValue(result,
//            "QTYNEGATIVE");
//        objectReporteGenerateAnaliticKardexData.movementtype = UtilSql.getValue(result, "MOVEMENTTYPE");
//        objectReporteGenerateAnaliticKardexData.shipmentid = UtilSql.getValue(result, "SHIPMENT");
//        objectReporteGenerateAnaliticKardexData.shipmentline = UtilSql.getValue(result, "SHIPMENTLINE");
//        objectReporteGenerateAnaliticKardexData.inventoryline = UtilSql.getValue(result, "INVENTORYLINE");
//        // objectReporteGenerateAnaliticKardexData.movementline = UtilSql.getValue(result, "MOVEMENTLINE");
//        objectReporteGenerateAnaliticKardexData.productionline = UtilSql
//            .getValue(result, "PRODUCTIONLINE");
//        objectReporteGenerateAnaliticKardexData.inventoryid = UtilSql.getValue(result, "INVENTORY");
//        // objectReporteGenerateAnaliticKardexData.movementid = UtilSql.getValue(result, "MOVEMENT");
//        objectReporteGenerateAnaliticKardexData.productionid = UtilSql.getValue(result, "PRODUCTION");
//        objectReporteGenerateAnaliticKardexData.shipmentlinespecialDT = UtilSql.getValue(result, "SHIPMENTSDT");
//        objectReporteGenerateAnaliticKardexData.rownum = Long.toString(countRecord);
//        objectReporteGenerateAnaliticKardexData.InitRecordNumber = Integer.toString(firstRegister);
//
//        objectReporteGenerateAnaliticKardexData.partner = UtilSql.getValue(result, "PARTNER"); 
//        
//        vector.addElement(objectReporteGenerateAnaliticKardexData);
//        if (countRecord >= numberRegisters && numberRegisters != 0) {
//          continueResult = false;
//        }
//      }
//      result.close();
//    } catch (SQLException e) {
//      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
//      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
//          + e.getMessage());
//    } catch (Exception ex) {
//      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
//      throw new ServletException("@CODE=@" + ex.getMessage());
//    } finally {
//      try {
//        connectionProvider.releasePreparedStatement(st);
//      } catch (Exception ignore) {
//        ignore.printStackTrace();
//      }
//    }
//    ReporteGenerateAnaliticKardexData objectReporteGenerateAnaliticKardexData[] = new ReporteGenerateAnaliticKardexData[vector
//        .size()];
//    vector.copyInto(objectReporteGenerateAnaliticKardexData);
//    return (objectReporteGenerateAnaliticKardexData);
//  }
//
//  public static ReporteGenerateAnaliticKardexData[] set() throws ServletException {
//    ReporteGenerateAnaliticKardexData objectReporteGenerateAnaliticKardexData[] = new ReporteGenerateAnaliticKardexData[1];
//    objectReporteGenerateAnaliticKardexData[0] = new ReporteGenerateAnaliticKardexData();
//    objectReporteGenerateAnaliticKardexData[0].productid = "";
//    objectReporteGenerateAnaliticKardexData[0].searchkey = "";
//    objectReporteGenerateAnaliticKardexData[0].internalcode = "";
//    objectReporteGenerateAnaliticKardexData[0].name = "";
//    objectReporteGenerateAnaliticKardexData[0].prdID = "";
//    objectReporteGenerateAnaliticKardexData[0].transactionID = "";
//    objectReporteGenerateAnaliticKardexData[0].movementDate = "";
//    objectReporteGenerateAnaliticKardexData[0].movementHour = "";
//    objectReporteGenerateAnaliticKardexData[0].warehousename = "";
//    objectReporteGenerateAnaliticKardexData[0].storagebin = "";
//    objectReporteGenerateAnaliticKardexData[0].movementqty = "";
//    objectReporteGenerateAnaliticKardexData[0].movementqtynegative = "";
//    objectReporteGenerateAnaliticKardexData[0].shipmentid = "";
//    objectReporteGenerateAnaliticKardexData[0].inventoryid = "";
//    objectReporteGenerateAnaliticKardexData[0].movementid = "";
//    objectReporteGenerateAnaliticKardexData[0].shipmentline = "";
//    objectReporteGenerateAnaliticKardexData[0].inventoryline = "";
//    objectReporteGenerateAnaliticKardexData[0].movementline = "";
//    objectReporteGenerateAnaliticKardexData[0].productionline = "";
//    objectReporteGenerateAnaliticKardexData[0].productionid = "";
//    objectReporteGenerateAnaliticKardexData[0].productionid = "";
//    objectReporteGenerateAnaliticKardexData[0].productdata = "";
//    objectReporteGenerateAnaliticKardexData[0].qtyreserved = "";
//    objectReporteGenerateAnaliticKardexData[0].shipmentlinespecialDT = "";
//   
//    return objectReporteGenerateAnaliticKardexData;
//
//  }
//
//  public static String selectMproduct(ConnectionProvider connectionProvider, String mProductId)
//      throws ServletException {
//    String strSql = "";
//    strSql = strSql + "      SELECT M_PRODUCT.NAME" + "      FROM M_PRODUCT"
//        + "      WHERE M_PRODUCT.M_PRODUCT_ID = ?";
//
//    ResultSet result;
//    String strReturn = "";
//    PreparedStatement st = null;
//
//    int iParameter = 0;
//    try {
//      st = connectionProvider.getPreparedStatement(strSql);
//      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
//      iParameter++;
//      UtilSql.setValue(st, iParameter, 12, null, mProductId);
//
//      result = st.executeQuery();
//      if (result.next()) {
//        strReturn = UtilSql.getValue(result, "name");
//      }
//      result.close();
//    } catch (SQLException e) {
//      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
//      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
//          + e.getMessage());
//    } catch (Exception ex) {
//      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
//      throw new ServletException("@CODE=@" + ex.getMessage());
//    } finally {
//      try {
//        connectionProvider.releasePreparedStatement(st);
//      } catch (Exception ignore) {
//        ignore.printStackTrace();
//      }
//    }
//    return (strReturn);
//  }
//
//  public static int getDifferenceBetwwenDatesInMonths(String startDate, String endDate) {
//    String[] startDateSplitted = startDate.split("-");
//    String[] endDateSplitted = endDate.split("-");
//
//    Date start_date = new Date();
//    Date end_date = new Date();
//
//    start_date.setDate(Integer.parseInt(startDateSplitted[0]));
//    start_date.setMonth(Integer.parseInt(startDateSplitted[1]));
//    start_date.setYear(Integer.parseInt(startDateSplitted[2]));
//
//    end_date.setDate(Integer.parseInt(endDateSplitted[0]));
//    end_date.setMonth(Integer.parseInt(endDateSplitted[1]));
//    end_date.setYear(Integer.parseInt(endDateSplitted[2]));
//
//    // calculating the difference
//    Calendar startCalendar = new GregorianCalendar();
//    startCalendar.setTime(start_date);
//    Calendar endCalendar = new GregorianCalendar();
//    endCalendar.setTime(end_date);
//
//    int diffYear = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
//    int diffMonth = diffYear * 12 + endCalendar.get(Calendar.MONTH)
//        - startCalendar.get(Calendar.MONTH);
//
//    return diffMonth;
//  }
//
//  public static int getDifferenceBetwwenDatesInDays(Date start_date, Date end_date) {
//    final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
//
//    int diffInDays = (int) ((end_date.getTime() - start_date.getTime() + 1) / DAY_IN_MILLIS);
//
//    if (diffInDays < 0)
//      return 0;
//    return diffInDays;
//  }
//
//  public static ReporteGenerateAnaliticKardexData[] selectWarehouseDouble(
//      ConnectionProvider connectionProvider, String adUserClient) throws ServletException {
//    return selectWarehouseDouble(connectionProvider, adUserClient, 0, 0);
//  }
//
//  public static ReporteGenerateAnaliticKardexData[] selectWarehouseDouble(
//      ConnectionProvider connectionProvider, String adUserClient, int firstRegister,
//      int numberRegisters) throws ServletException {
//    String strSql = "";
//    strSql = strSql
//        + "        SELECT M_WAREHOUSE.AD_ORG_ID AS PADRE, M_WAREHOUSE.M_WAREHOUSE_ID AS ID, TO_CHAR(M_WAREHOUSE.NAME) AS NAME"
//        + "        FROM M_WAREHOUSE" + "        WHERE 1=1"
//        + "         AND M_WAREHOUSE.AD_Client_ID IN(";
//    strSql = strSql + ((adUserClient == null || adUserClient.equals("")) ? "" : adUserClient);
//    strSql = strSql
//        + ")"
//        + "         UNION "
//        + "        SELECT null AS PADRE, M_WAREHOUSE.M_WAREHOUSE_ID AS ID, TO_CHAR(M_WAREHOUSE.NAME) AS NAME"
//        + "        FROM M_WAREHOUSE" + "        WHERE 2=2 AND M_WAREHOUSE.AD_Client_ID IN(";
//    strSql = strSql + ((adUserClient == null || adUserClient.equals("")) ? "" : adUserClient);
//    strSql = strSql + ")    " + "        ORDER BY PADRE, NAME";
//
//    ResultSet result;
//    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
//    PreparedStatement st = null;
//
//    try {
//      st = connectionProvider.getPreparedStatement(strSql);
//      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
//      if (adUserClient != null && !(adUserClient.equals(""))) {
//      }
//      if (adUserClient != null && !(adUserClient.equals(""))) {
//      }
//      result = st.executeQuery();
//
//      long countRecord = 0;
//      long countRecordSkip = 1;
//      boolean continueResult = true;
//      while (countRecordSkip < firstRegister && continueResult) {
//        continueResult = result.next();
//        countRecordSkip++;
//      }
//      while (continueResult && result.next()) {
//        countRecord++;
//        ReporteGenerateAnaliticKardexData objectReporteGenerateAnaliticKardexData = new ReporteGenerateAnaliticKardexData();
//        objectReporteGenerateAnaliticKardexData.padre = UtilSql.getValue(result, "padre");
//        objectReporteGenerateAnaliticKardexData.id = UtilSql.getValue(result, "id");
//        objectReporteGenerateAnaliticKardexData.name = UtilSql.getValue(result, "name");
//        objectReporteGenerateAnaliticKardexData.InitRecordNumber = Integer.toString(firstRegister);
//        vector.addElement(objectReporteGenerateAnaliticKardexData);
//        if (countRecord >= numberRegisters && numberRegisters != 0) {
//          continueResult = false;
//        }
//      }
//      result.close();
//    } catch (SQLException e) {
//      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
//      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
//          + e.getMessage());
//    } catch (Exception ex) {
//      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
//      throw new ServletException("@CODE=@" + ex.getMessage());
//    } finally {
//      try {
//        connectionProvider.releasePreparedStatement(st);
//      } catch (Exception ignore) {
//        ignore.printStackTrace();
//      }
//    }
//    ReporteGenerateAnaliticKardexData objectReporteGenerateAnaliticKardexData[] = new ReporteGenerateAnaliticKardexData[vector
//        .size()];
//    vector.copyInto(objectReporteGenerateAnaliticKardexData);
//    return (objectReporteGenerateAnaliticKardexData);
//  }
//
//}
