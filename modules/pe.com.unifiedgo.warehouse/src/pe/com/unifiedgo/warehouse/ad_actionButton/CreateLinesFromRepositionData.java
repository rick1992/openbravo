//Sqlc generated V1.O00-1
package pe.com.unifiedgo.warehouse.ad_actionButton;

import java.sql.*;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;

import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.data.UtilSql;
import org.openbravo.service.db.QueryTimeOutUtil;
import org.openbravo.database.SessionInfo;

import java.util.*;

import org.openbravo.database.RDBMSIndependent;
import org.openbravo.exception.*;


class CreateLinesFromRepositionData implements FieldProvider {
static Logger log4j = Logger.getLogger(CreateLinesFromRepositionData.class);
  private String InitRecordNumber="0";
  
  //begin

  
  public String DOCNO;
  public String PARTNER;
  public String DATEORDER;
  
  public String ORDERLINEID;
  public String LINE;
  public String PRODUCTID;
  public String PRODUCTNAME;
  public String UOM;
  public String PRICE;
  public String ORDERED;
  public String PENDING;
  public String ARRIVAL;
  public String VALUE;
  public String INTERNALCODE;
  public String rownum;
  
  public String CLIENTID;
  public String LOCATORID;
  public String LOCATORNAME;
  public String PRIORITYNO;
  public String LOCATORTYPE;
  public String LOCATORQTY;
  public String LOCATORQTYPRODUCT;
  
  public String id;
  public String name;
  public String inoutid;
  public String requirementid;
  public String requirementlineid;
  public String productid;
  public String productname;
  public String uomname;
  public String repodetailid;
  public String movementqty;
  public String productvalue;
  public String pname;
  public String qtypending;
  //
  

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  public String getField(String fieldName) {
    if (fieldName.equals("DOCNO"))
        return DOCNO;
    else if (fieldName.equals("PARTNER"))
        return PARTNER;
    else if (fieldName.equals("DATEORDER"))
        return DATEORDER;
    else if (fieldName.equals("ORDERLINEID"))
        return ORDERLINEID;
    else if (fieldName.equals("LINE"))
        return LINE;
    else if (fieldName.equals("PRODUCTID"))
        return PRODUCTID;
    else if (fieldName.equals("UOM"))
        return UOM;
    else if (fieldName.equals("PRICE"))
        return PRICE;
    else if (fieldName.equals("ORDERED"))
        return ORDERED;
    else if (fieldName.equals("PENDING"))
        return PENDING;
    else if (fieldName.equals("ARRIVAL"))
        return ARRIVAL;
    else if (fieldName.equals("VALUE"))
      return VALUE;
    else if (fieldName.equals("rownum"))
      return rownum;
    else if (fieldName.equals("INTERNALCODE"))
        return INTERNALCODE;
    
    else if (fieldName.equals("LOCATORID"))
        return LOCATORID;
    else if (fieldName.equals("LOCATORNAME"))
        return LOCATORNAME;
    else if (fieldName.equals("PRIORITYNO"))
        return PRIORITYNO;
    else if (fieldName.equals("LOCATORTYPE"))
        return LOCATORTYPE;
    else if (fieldName.equals("LOCATORQTY"))
        return LOCATORQTY; 
    else if (fieldName.equals("LOCATORQTYPRODUCT"))
        return LOCATORQTYPRODUCT;
    else if (fieldName.equals("CLIENTID"))
        return CLIENTID;
    
    
    else if (fieldName.equals("id"))
        return id;
    else if (fieldName.equals("name"))
        return name;
    else if (fieldName.equals("requirementid"))
    	 return requirementid;
    else if (fieldName.equals("requirementlineid"))
   	 return requirementlineid;
    else if (fieldName.equals("productvalue"))
   	 return productvalue;
    else if (fieldName.equals("qtypending"))
        return qtypending;
    else if (fieldName.equals("pname"))
        return pname;
    
       
    
   else {
     log4j.debug("Field does not exist: " + fieldName);
     return null;
   }
 }
  
  public static CreateLinesFromRepositionData[] selectLocator(ConnectionProvider connectionProvider, String strWarehouseId, String strMProductid, String chkOnlyLocatorOut, String rbtnSelectLocator)   throws ServletException {
	    String strSql = "";
	    strSql = strSql + 

	    		"   SELECT L.AD_CLIENT_ID as clientid, "+
	    		"          L.M_LOCATOR_ID AS locatorid, "+
	    		"          L.x || L.y || L.z as locatorname,"+
	    		"          L.PRIORITYNO as priorityno, "+
	    		"          COALESCE(L.EM_OBWHS_TYPE,'NORMAL') as locatortype, "+
	    		"          CASE WHEN (COALESCE(SUM(SD.qtyonHand),0)) = 0 THEN 0 ELSE COALESCE(SUM(SD.qtyonHand),0) END AS locatorqty, "+
	    		"          COALESCE((SELECT qtyonHand FROM M_STORAGE_DETAIL Where m_product_id = ? "+
	    		"           and m_locator_id = L.M_LOCATOR_ID),0) as locatorqtyproduct "+
	    		"      FROM M_LOCATOR L "+
	    		"          LEFT JOIN M_STORAGE_DETAIL SD ON L.M_LOCATOR_ID = SD.M_LOCATOR_ID "+
	    		"       WHERE L.M_warehouse_id = ? ";
	    		strSql = strSql + ((chkOnlyLocatorOut.equals("Y"))?"AND L.EM_OBWHS_TYPE = 'OUT'":"");
	    		//strSql = strSql + (((rbtnSelectLocator.equals("created")))?"AND SD.M_PRODUCT_ID = ?":"");
	    		strSql = strSql + (((rbtnSelectLocator.equals("created")))?"AND L.M_LOCATOR_ID IN (SELECT DISTINCT M_LOCATOR_ID FROM M_STORAGE_DETAIL WHERE M_PRODUCT_ID = ?)":"");
	    		strSql = strSql + 
	    		"       GROUP BY L.AD_CLIENT_ID, L.M_LOCATOR_ID , L.PRIORITYNO, L.EM_OBWHS_TYPE , L.x, L.y,L.z ";
	    		strSql = strSql + (((rbtnSelectLocator.equals("pending")))?"HAVING COALESCE(SUM(SD.qtyonHand),0)=0":"");
	    		strSql = strSql + (((rbtnSelectLocator.equals("created")))?"HAVING ((SELECT qtyonHand FROM M_STORAGE_DETAIL Where m_product_id = ? and m_locator_id = L.M_LOCATOR_ID))<>0":"");
	    		strSql = strSql +	
	    		"       ORDER BY L.PRIORITYNO ASC, L.X, L.Y, L.Z ";
		
	    ResultSet result;
	    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
	    PreparedStatement st = null;

	    int iParameter = 0;
	    try {
	    st = connectionProvider.getPreparedStatement(strSql);
	      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
	      iParameter++; UtilSql.setValue(st, iParameter, 12, null, strMProductid);
	      iParameter++; UtilSql.setValue(st, iParameter, 12, null, strWarehouseId);
    	    if (rbtnSelectLocator != null && !(rbtnSelectLocator.equals("")) && rbtnSelectLocator.equals("created")) {
    	        iParameter++; UtilSql.setValue(st, iParameter, 12, null, strMProductid);
    	    }
    	    if (rbtnSelectLocator != null && !(rbtnSelectLocator.equals("")) && rbtnSelectLocator.equals("created")) {
    	        iParameter++; UtilSql.setValue(st, iParameter, 12, null, strMProductid);
    	    }
    	  //System.out.println("QUERY: " + st);
	      result = st.executeQuery();
	      long countRecord = 0;
	      long countRecordSkip = 1;
	      boolean continueResult = true;
	
	      while(continueResult && result.next()) {
	        countRecord++;
	        CreateLinesFromRepositionData objectCreateLinesFromRepositionData = new CreateLinesFromRepositionData();
	        
	        objectCreateLinesFromRepositionData.CLIENTID = UtilSql.getValue(result, "clientid");
	        objectCreateLinesFromRepositionData.LOCATORID = UtilSql.getValue(result, "locatorid");
	        objectCreateLinesFromRepositionData.LOCATORNAME = UtilSql.getValue(result, "locatorname");
	        objectCreateLinesFromRepositionData.PRIORITYNO = UtilSql.getValue(result, "priorityno");
	        objectCreateLinesFromRepositionData.LOCATORTYPE = UtilSql.getValue(result, "locatortype");
	        objectCreateLinesFromRepositionData.LOCATORQTY = UtilSql.getValue(result, "locatorqty");
	        objectCreateLinesFromRepositionData.LOCATORQTYPRODUCT = UtilSql.getValue(result, "locatorqtyproduct");
	        
	        vector.addElement(objectCreateLinesFromRepositionData);
	      }
	      result.close();
	    } catch(SQLException e){
	      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
	      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
	    } catch(Exception ex){
	      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
	      throw new ServletException("@CODE=@" + ex.getMessage());
	    } finally {
	      try {
	        connectionProvider.releasePreparedStatement(st);
	      } catch(Exception ignore){
	        ignore.printStackTrace();
	      }
	    }
	    CreateLinesFromRepositionData objectCreateLinesFromRepositionData[] = new CreateLinesFromRepositionData[vector.size()];
	    vector.copyInto(objectCreateLinesFromRepositionData);
	    return(objectCreateLinesFromRepositionData);
	  }
  
  public static CreateLinesFromRepositionData[] selectReposition_OrderPending(ConnectionProvider connectionProvider, String strRepositionrequestId)   throws ServletException {
	    String strSql = "";
	    strSql = strSql + 

	    		"   SELECT l.ad_client_id as clientid, "+
	    		"          l.swa_requerimientoreposicion_id as requirementid, "+
	    		"          l.swa_requerepo_detail_id as requirementlineid,"+
	    		"          p.value as productvalue, "+
	    		"          p.name as pname, "+
	    		"          COALESCE(l.qtyshipped,0) - COALESCE(l.qtyreceipt) as qtypending "+
	    		"      FROM swa_requerepo_detail l "+
	    		"         INNER JOIN M_PRODUCT p on p.m_product_id = l.m_product_id "+
	    		"      WHERE l.swa_requerimientoreposicion_id = ? "+
	    		"        GROUP BY l.ad_client_id, l.swa_requerimientoreposicion_id, l.swa_requerepo_detail_id, p.value , p.name, l.qtyshipped, l.qtyreceipt " +
	            "         HAVING COALESCE(l.qtyshipped,0) - COALESCE(l.qtyreceipt) > 0          ";
	    	
		
	    ResultSet result;
	    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
	    PreparedStatement st = null;

	    int iParameter = 0;
	    try {
 	      st = connectionProvider.getPreparedStatement(strSql);
	      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
	      iParameter++; UtilSql.setValue(st, iParameter, 12, null, strRepositionrequestId);
	      
	      result = st.executeQuery();
	      long countRecord = 0;
	      long countRecordSkip = 1;
	      boolean continueResult = true;
	
	      while(continueResult && result.next()) {
	        countRecord++;
	        CreateLinesFromRepositionData objectCreateLinesFromRepositionData = new CreateLinesFromRepositionData();
	        
	        objectCreateLinesFromRepositionData.CLIENTID = UtilSql.getValue(result, "clientid");
	        objectCreateLinesFromRepositionData.requirementid = UtilSql.getValue(result, "requirementid");
	        objectCreateLinesFromRepositionData.requirementlineid = UtilSql.getValue(result, "requirementlineid");
	        objectCreateLinesFromRepositionData.productvalue = UtilSql.getValue(result, "productvalue");
	        objectCreateLinesFromRepositionData.qtypending = UtilSql.getValue(result, "qtypending");
	        objectCreateLinesFromRepositionData.pname = UtilSql.getValue(result, "pname");
	        objectCreateLinesFromRepositionData.rownum = Long.toString(countRecord);
	        vector.addElement(objectCreateLinesFromRepositionData);
	      }
	      result.close();
	    } catch(SQLException e){
	      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
	      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
	    } catch(Exception ex){
	      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
	      throw new ServletException("@CODE=@" + ex.getMessage());
	    } finally {
	      try {
	        connectionProvider.releasePreparedStatement(st);
	      } catch(Exception ignore){
	        ignore.printStackTrace();
	      }
	    }
	    CreateLinesFromRepositionData objectCreateLinesFromRepositionData[] = new CreateLinesFromRepositionData[vector.size()];
	    vector.copyInto(objectCreateLinesFromRepositionData);
	    return(objectCreateLinesFromRepositionData);
	}
  
  public static CreateLinesFromRepositionData[] selectReposition_SamplePending(ConnectionProvider connectionProvider, String strRepositionrequestId)   throws ServletException {
	    String strSql = "";
	    strSql = strSql + 

	    		"   SELECT l.ad_client_id as clientid, "+
	    		"          l.swa_requerimientoreposicion_id as requirementid, "+
	    		"          l.swa_requerepo_detail_id as requirementlineid,"+
	    		"          p.value as productvalue, "+
	    		"          p.name as pname, "+
	    		"          COALESCE(l.qtyordered,0) - (COALESCE(l.qtysamplereturned,0) + COALESCE(l.qtysampleinvoiced,0))  as qtypending "+
	    		"      FROM swa_requerepo_detail l "+
	    		"         INNER JOIN M_PRODUCT p on p.m_product_id = l.m_product_id "+
	    		"      WHERE l.swa_requerimientoreposicion_id = ? "+
	    		"        GROUP BY l.ad_client_id, l.swa_requerimientoreposicion_id, l.swa_requerepo_detail_id, p.value , p.name,l.qtyordered,l.qtysamplereturned, l.qtysampleinvoiced   "+
	    		"         HAVING COALESCE(l.qtyordered,0) - COALESCE(l.qtysamplereturned) > 0          ";
	    	
		
	    ResultSet result;
	    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
	    PreparedStatement st = null;

	    int iParameter = 0;
	    try {
   	      st = connectionProvider.getPreparedStatement(strSql);
	      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
	      iParameter++; UtilSql.setValue(st, iParameter, 12, null, strRepositionrequestId);
	      //System.out.println("HOLIS: " + st);
	      result = st.executeQuery();
	      long countRecord = 0;
	      long countRecordSkip = 1;
	      boolean continueResult = true;
	
	      while(continueResult && result.next()) {
	        countRecord++;
	        CreateLinesFromRepositionData objectCreateLinesFromRepositionData = new CreateLinesFromRepositionData();
	        
	        objectCreateLinesFromRepositionData.CLIENTID = UtilSql.getValue(result, "clientid");
	        objectCreateLinesFromRepositionData.requirementid = UtilSql.getValue(result, "requirementid");
	        objectCreateLinesFromRepositionData.requirementlineid = UtilSql.getValue(result, "requirementlineid");
	        objectCreateLinesFromRepositionData.productvalue = UtilSql.getValue(result, "productvalue");
	        objectCreateLinesFromRepositionData.qtypending = UtilSql.getValue(result, "qtypending");
	        objectCreateLinesFromRepositionData.pname = UtilSql.getValue(result, "pname");
	        objectCreateLinesFromRepositionData.rownum = Long.toString(countRecord);
	        vector.addElement(objectCreateLinesFromRepositionData);
	      }
	      result.close();
	    } catch(SQLException e){
	      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
	      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
	    } catch(Exception ex){
	      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
	      throw new ServletException("@CODE=@" + ex.getMessage());
	    } finally {
	      try {
	        connectionProvider.releasePreparedStatement(st);
	      } catch(Exception ignore){
	        ignore.printStackTrace();
	      }
	    }
	    CreateLinesFromRepositionData objectCreateLinesFromRepositionData[] = new CreateLinesFromRepositionData[vector.size()];
	    vector.copyInto(objectCreateLinesFromRepositionData);
	    return(objectCreateLinesFromRepositionData);
	}
  
  
  
  public static CreateLinesFromRepositionData[] selectReposition_Request(ConnectionProvider connectionProvider, String strAdOrgId, String strWarehouseId)   throws ServletException {
	    String strSql = "";
	    strSql = strSql + 

	    		"   SELECT r.swa_requerimientoreposicion_id as repositionid, "+
	    		"          r.documentno as documentno "+
	    		"      FROM swa_requerepo_detail rd  "+
	    		"          INNER JOIN swa_requerimientoreposicion r ON r.swa_requerimientoreposicion_id = rd.swa_requerimientoreposicion_id "+
	    		"       WHERE R.ISREQUEST = 'Y' " +
	            "         AND R.TO_M_WAREHOUSE_ID = ? " +
	    		"      GROUP BY r.swa_requerimientoreposicion_id, r.documentno ";
		
	    ResultSet result;
	    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
	    PreparedStatement st = null;

	    int iParameter = 0;
	    try {
  	      st = connectionProvider.getPreparedStatement(strSql);
	      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
	      iParameter++; UtilSql.setValue(st, iParameter, 12, null, strWarehouseId);
	      //System.out.println("--: " + st);
	      result = st.executeQuery();
	      long countRecord = 0;
	      long countRecordSkip = 1;
	      boolean continueResult = true;
	
	      while(continueResult && result.next()) {
	        countRecord++;
	        CreateLinesFromRepositionData objectCreateLinesFromRepositionData = new CreateLinesFromRepositionData();
	        
	        objectCreateLinesFromRepositionData.id = UtilSql.getValue(result, "repositionid");
	        objectCreateLinesFromRepositionData.name  = UtilSql.getValue(result, "documentno");
	        objectCreateLinesFromRepositionData.rownum = Long.toString(countRecord);
	        vector.addElement(objectCreateLinesFromRepositionData);
	      }
	      result.close();
	    } catch(SQLException e){
	      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
	      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
	    } catch(Exception ex){
	      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
	      throw new ServletException("@CODE=@" + ex.getMessage());
	    } finally {
	      try {
	        connectionProvider.releasePreparedStatement(st);
	      } catch(Exception ignore){
	        ignore.printStackTrace();
	      }
	    }
	    CreateLinesFromRepositionData objectCreateLinesFromRepositionData[] = new CreateLinesFromRepositionData[vector.size()];
	    vector.copyInto(objectCreateLinesFromRepositionData);
	    return(objectCreateLinesFromRepositionData);
	}
  
  public static CreateLinesFromRepositionData[] selectReposition_SampleOrder(ConnectionProvider connectionProvider, String stradOrgId, String strBPartnerId, String strRepSampleId)   throws ServletException {
	    String strSql = "";
	    strSql = strSql + 

	    		"   SELECT distinct r.swa_requerimientoreposicion_id as repositionid, "+
	    		"          r.documentno as documentno "+
	    		"      FROM swa_requerepo_detail rd  "+
	    		"          INNER JOIN swa_requerimientoreposicion r ON r.swa_requerimientoreposicion_id = rd.swa_requerimientoreposicion_id "+
	    		"          inner join c_doctype d on d.c_doctype_id = r.c_doctype_id " +
	    		"       WHERE r.status='CO' " +
	            "         AND d.em_sco_specialdoctype='SWAREQUESTSAMPE' " +
	            "         AND r.ad_org_id = ? " +
	            "         AND r.c_bpartner_id = ? " +
	            "         AND r.samplerep_id = ? " +
	    		"    GROUP BY r.swa_requerimientoreposicion_id, r.documentno, r.documentno , rd.qtyordered , rd.qtysamplereturned " + 
	            "      HAVING COALESCE(rd.qtyordered,0) - COALESCE(rd.qtysamplereturned,0) > 0    ";
		
	    ResultSet result;
	    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
	    PreparedStatement st = null;

	    int iParameter = 0;
	    try {
	      st = connectionProvider.getPreparedStatement(strSql);
	      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
	      iParameter++; UtilSql.setValue(st, iParameter, 12, null, stradOrgId);
	      iParameter++; UtilSql.setValue(st, iParameter, 12, null, strBPartnerId);
	      iParameter++; UtilSql.setValue(st, iParameter, 12, null, strRepSampleId);
	      //System.out.println("SAMPLE: " + st);
	      result = st.executeQuery();
	      long countRecord = 0;
	      long countRecordSkip = 1;
	      boolean continueResult = true;
	
	      while(continueResult && result.next()) {
	        countRecord++;
	        CreateLinesFromRepositionData objectCreateLinesFromRepositionData = new CreateLinesFromRepositionData();
	        
	        objectCreateLinesFromRepositionData.id = UtilSql.getValue(result, "repositionid");
	        objectCreateLinesFromRepositionData.name  = UtilSql.getValue(result, "documentno");
	        objectCreateLinesFromRepositionData.rownum = Long.toString(countRecord);
	        vector.addElement(objectCreateLinesFromRepositionData);
	      }
	      result.close();
	    } catch(SQLException e){
	      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
	      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
	    } catch(Exception ex){
	      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
	      throw new ServletException("@CODE=@" + ex.getMessage());
	    } finally {
	      try {
	        connectionProvider.releasePreparedStatement(st);
	      } catch(Exception ignore){
	        ignore.printStackTrace();
	      }
	    }
	    CreateLinesFromRepositionData objectCreateLinesFromRepositionData[] = new CreateLinesFromRepositionData[vector.size()];
	    vector.copyInto(objectCreateLinesFromRepositionData);
	    return(objectCreateLinesFromRepositionData);
	}
  
  public static int updateOrderRepositionInReceipt(Connection conn, ConnectionProvider connectionProvider,  String strOrderRepositionId)    throws ServletException {
	    String strSql = "";
	    strSql = strSql + 
	      "        UPDATE swa_requerimientoreposicion " +
	      "           SET generatetrx = 'Y' " +
	      "         WHERE swa_requerimientoreposicion_id = ?";

	    int updateCount = 0;
	    PreparedStatement st = null;

	    int iParameter = 0;
	    try {
	      st = connectionProvider.getPreparedStatement(conn,strSql);
	      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
	      iParameter++; UtilSql.setValue(st, iParameter, 12, null, strOrderRepositionId);
	      //System.out.println("ST: " + st);
	      updateCount = st.executeUpdate();
	    } catch(SQLException e){
	      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
	      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
	    } catch(Exception ex){
	      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
	      throw new ServletException("@CODE=@" + ex.getMessage());
	    } finally {
	      try {
	        connectionProvider.releaseTransactionalPreparedStatement(st);
	      } catch(Exception ignore){
	        ignore.printStackTrace();
	      }
	    }
	    return(updateCount);
	  }
  
  public static int updateReceiptReposition(Connection conn, ConnectionProvider connectionProvider, String strReceiptRepositionId, String strOrderRepositionId)    throws ServletException {
	    String strSql = "";
	    strSql = strSql + 
	      "        UPDATE swa_requerimientoreposicion " +
	      "           SET swa_fromrequerimiento_id = ? " +
	      "         WHERE swa_requerimientoreposicion_id = ?";

	    int updateCount = 0;
	    PreparedStatement st = null;

	    int iParameter = 0;
	    try {
	      st = connectionProvider.getPreparedStatement(conn,strSql);
	      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
	      iParameter++; UtilSql.setValue(st, iParameter, 12, null, strOrderRepositionId);
	      iParameter++; UtilSql.setValue(st, iParameter, 12, null, strReceiptRepositionId);
	      
	      //System.out.println("ST: " + st);
	      updateCount = st.executeUpdate();
	    } catch(SQLException e){
	      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
	      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
	    } catch(Exception ex){
	      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
	      throw new ServletException("@CODE=@" + ex.getMessage());
	    } finally {
	      try {
	        connectionProvider.releaseTransactionalPreparedStatement(st);
	      } catch(Exception ignore){
	        ignore.printStackTrace();
	      }
	    }
	    return(updateCount);
	  }
  
  public static CreateLinesFromRepositionData[] selectShipmentLine(ConnectionProvider connectionProvider, String strGoodsShipmentId)   throws ServletException {
	    String strSql = "";
	    strSql = strSql + 

	    		"   SELECT i.ad_client_id as clientid, " +
	    		"          i.m_product_id as requirementid,   "+
	    		"          p.value as productvalue ,"+
	    		"          p.name as pname, "+
	    		"          ut.name as uomname,  " +
	    		"          em_swa_requerepo_detail_id as requirementlineid , " +
	    		"          sum(movementqty) as qtypending "+
	    		"     FROM M_INOUTLINE i "+
	    		"          INNER JOIN M_PRODUCT p ON p.m_product_id = i.m_product_id " +
	    		"          INNER JOIN C_UOM u ON u.c_uom_id = i.c_uom_id " +
	    		"          INNER JOIN C_UOM_TRL ut ON ut.c_uom_id = u.c_uom_id " +
	    		"       WHERE M_INOUT_ID = ? " +
	    		"         AND ut.ad_language='es_PE' " +  
	    		"    GROUP BY i.ad_client_id, i.m_product_id, p.value, p.name, ut.name, i.em_swa_requerepo_detail_id ";
		
	    ResultSet result;
	    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
	    PreparedStatement st = null;

	    int iParameter = 0;
	    try {
	      st = connectionProvider.getPreparedStatement(strSql);
	      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
	      iParameter++; UtilSql.setValue(st, iParameter, 12, null, strGoodsShipmentId);
	      //System.out.println("Sis: " + st);
	      result = st.executeQuery();
	      long countRecord = 0;
	      long countRecordSkip = 1;
	      boolean continueResult = true;
	
	      while(continueResult && result.next()) {
	        countRecord++;
	        CreateLinesFromRepositionData objectCreateLinesFromRepositionData = new CreateLinesFromRepositionData();
	        
	        objectCreateLinesFromRepositionData.CLIENTID = UtilSql.getValue(result, "clientid");
	        objectCreateLinesFromRepositionData.requirementid = UtilSql.getValue(result, "requirementid");
	        objectCreateLinesFromRepositionData.productvalue = UtilSql.getValue(result, "productvalue");
	        objectCreateLinesFromRepositionData.pname  = UtilSql.getValue(result, "pname");
	        objectCreateLinesFromRepositionData.uomname  = UtilSql.getValue(result, "uomname");
	        objectCreateLinesFromRepositionData.requirementlineid  = UtilSql.getValue(result, "requirementlineid");
	        objectCreateLinesFromRepositionData.qtypending  = UtilSql.getValue(result, "qtypending");
	        
	        objectCreateLinesFromRepositionData.rownum = Long.toString(countRecord);
	        vector.addElement(objectCreateLinesFromRepositionData);
	      }
	      result.close();
	    } catch(SQLException e){
	      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
	      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
	    } catch(Exception ex){
	      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
	      throw new ServletException("@CODE=@" + ex.getMessage());
	    } finally {
	      try {
	        connectionProvider.releasePreparedStatement(st);
	      } catch(Exception ignore){
	        ignore.printStackTrace();
	      }
	    }
	    CreateLinesFromRepositionData objectCreateLinesFromRepositionData[] = new CreateLinesFromRepositionData[vector.size()];
	    vector.copyInto(objectCreateLinesFromRepositionData);
	    return(objectCreateLinesFromRepositionData);
	}
  
  public static CreateLinesFromRepositionData[] selectShipmentLineFromReposition(ConnectionProvider connectionProvider, String strGoodsShipmentId)   throws ServletException {
	    String strSql = "";
	    strSql = strSql + 

	    		"   SELECT i.m_product_id as productid,   "+
	    		"          p.value as productvalue ,"+
	    		"          p.name as productname, "+
	    		"          ut.name as uomname,  " +
	    		"          em_swa_requerepo_detail_id as repodetailid , " +
	    		"          sum(movementqty) as movementqty "+
	    		"     FROM M_INOUTLINE i "+
	    		"          INNER JOIN M_PRODUCT p ON p.m_product_id = i.m_product_id " +
	    		"          INNER JOIN C_UOM u ON u.c_uom_id = i.c_uom_id " +
	    		"          INNER JOIN C_UOM_TRL ut ON ut.c_uom_id = u.c_uom_id " +
	    		"       WHERE M_INOUT_ID = ? " +
	    		"         AND ut.ad_language='es_PE' " +  
	    		"    GROUP BY i.m_product_id, p.value, p.name, ut.name, i.em_swa_requerepo_detail_id ";
		
	    ResultSet result;
	    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
	    PreparedStatement st = null;

	    int iParameter = 0;
	    try {
	      st = connectionProvider.getPreparedStatement(strSql);
	      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
	      iParameter++; UtilSql.setValue(st, iParameter, 12, null, strGoodsShipmentId);
	      //System.out.println("Sis: " + st);
	      result = st.executeQuery();
	      long countRecord = 0;
	      long countRecordSkip = 1;
	      boolean continueResult = true;
	
	      while(continueResult && result.next()) {
	        countRecord++;
	        CreateLinesFromRepositionData objectCreateLinesFromRepositionData = new CreateLinesFromRepositionData();
	        
	        objectCreateLinesFromRepositionData.productid = UtilSql.getValue(result, "productid");
	        objectCreateLinesFromRepositionData.productvalue = UtilSql.getValue(result, "productvalue");
	        objectCreateLinesFromRepositionData.productname  = UtilSql.getValue(result, "productname");
	        objectCreateLinesFromRepositionData.uomname  = UtilSql.getValue(result, "uomname");
	        objectCreateLinesFromRepositionData.repodetailid  = UtilSql.getValue(result, "repodetailid");
	        objectCreateLinesFromRepositionData.movementqty  = UtilSql.getValue(result, "movementqty");
	        
	        objectCreateLinesFromRepositionData.rownum = Long.toString(countRecord);
	        vector.addElement(objectCreateLinesFromRepositionData);
	      }
	      result.close();
	    } catch(SQLException e){
	      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
	      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
	    } catch(Exception ex){
	      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
	      throw new ServletException("@CODE=@" + ex.getMessage());
	    } finally {
	      try {
	        connectionProvider.releasePreparedStatement(st);
	      } catch(Exception ignore){
	        ignore.printStackTrace();
	      }
	    }
	    CreateLinesFromRepositionData objectCreateLinesFromRepositionData[] = new CreateLinesFromRepositionData[vector.size()];
	    vector.copyInto(objectCreateLinesFromRepositionData);
	    return(objectCreateLinesFromRepositionData);
	}
  
  public static CreateLinesFromRepositionData[] selectReposition_RepositionOrder(ConnectionProvider connectionProvider, String strToWarehouseId)   throws ServletException {
	    String strSql = "";
	    strSql = strSql + 

	    		"   SELECT distinct r.swa_requerimientoreposicion_id as repositionid, "+
	    		"          i.m_inout_id as inoutid ,"+
	    		"           i.movementdate, " + 
	    		"           trunc(i.movementdate) || ' - ' || i.em_scr_physical_documentno as documentno "+
	    		"      FROM swa_requerimientoreposicion r "+
	    		"          INNER JOIN c_doctype d ON d.c_doctype_id = r.c_doctype_id " +
	    		"          INNER JOIN m_inout i ON  i.em_swa_requireposicion_id = r.swa_requerimientoreposicion_id " +
	    		"       WHERE r.status='CO' " +
	    		"         AND d.em_sco_specialdoctype ='SWAREPOSITION' " + //solo Vamos a recibir transferencias desde Ordenes 
	            "         AND r.to_m_warehouse_id = ? " +
	    		"         AND i.em_swa_isfromreposition='Y' " +
	            "         AND r.swa_requerimientoreposicion_id not in (SELECT swa_fromrequerimiento_id FROM swa_requerimientoreposicion WHERE swa_fromrequerimiento_id is not null)"+
	            "         AND i.issotrx = 'Y' " +
	    		"         AND i.docstatus='CO' "+
	            "         AND r.generatetrx = 'N'" +
	    		"    GROUP BY i.movementdate, r.swa_requerimientoreposicion_id, r.documentno, r.documentno , i.em_scr_physical_documentno, i.m_inout_id " + 
	            "        ORDER BY i.movementdate ";
		
	    ResultSet result;
	    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
	    PreparedStatement st = null;

	    int iParameter = 0;
	    try {
	      st = connectionProvider.getPreparedStatement(strSql);
	      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
	      iParameter++; UtilSql.setValue(st, iParameter, 12, null, strToWarehouseId);
	      
	      System.out.println("vafaster: " + st);
	      
	      result = st.executeQuery();
	      long countRecord = 0;
	      long countRecordSkip = 1;
	      boolean continueResult = true;
	
	      while(continueResult && result.next()) {
	        countRecord++;
	        CreateLinesFromRepositionData objectCreateLinesFromRepositionData = new CreateLinesFromRepositionData();
	        
	        objectCreateLinesFromRepositionData.id = UtilSql.getValue(result, "inoutid");
	        objectCreateLinesFromRepositionData.inoutid = UtilSql.getValue(result, "inoutid");
	        objectCreateLinesFromRepositionData.name  = UtilSql.getValue(result, "documentno");
	        objectCreateLinesFromRepositionData.rownum = Long.toString(countRecord);
	        vector.addElement(objectCreateLinesFromRepositionData);
	      }
	      result.close();
	    } catch(SQLException e){
	      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
	      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
	    } catch(Exception ex){
	      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
	      throw new ServletException("@CODE=@" + ex.getMessage());
	    } finally {
	      try {
	        connectionProvider.releasePreparedStatement(st);
	      } catch(Exception ignore){
	        ignore.printStackTrace();
	      }
	    }
	    CreateLinesFromRepositionData objectCreateLinesFromRepositionData[] = new CreateLinesFromRepositionData[vector.size()];
	    vector.copyInto(objectCreateLinesFromRepositionData);
	    return(objectCreateLinesFromRepositionData);
	}

  
  
  public static int deleteLinesReceiptReposition(Connection conn,ConnectionProvider connectionProvider, String strReceiptRepositionId)    throws ServletException {
	    String strSql = "";
	    strSql = strSql + 
	      "        DELETE FROM swa_requerepo_detail " +
	      "         WHERE swa_requerimientoreposicion_id = ?";

	    int updateCount = 0;
	    PreparedStatement st = null;

	    int iParameter = 0;
	    try {
	      st = connectionProvider.getPreparedStatement(conn,strSql);
	      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
	      iParameter++; UtilSql.setValue(st, iParameter, 12, null, strReceiptRepositionId);
	      //System.out.println("st: " + st);
	      updateCount = st.executeUpdate();
	    } catch(SQLException e){
	      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
	      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
	    } catch(Exception ex){
	      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
	      throw new ServletException("@CODE=@" + ex.getMessage());
	    } finally {
	      try {
	        connectionProvider.releaseTransactionalPreparedStatement(st);
	      } catch(Exception ignore){
	        ignore.printStackTrace();
	      }
	    }
	    return(updateCount);
	  }
  
  
  public static CreateLinesFromRepositionData[] selectReposition_Order(ConnectionProvider connectionProvider, String strAdOrgId, String strWarehouseId)   throws ServletException {
	    String strSql = "";
	    strSql = strSql + 

	    		"   SELECT r.swa_requerimientoreposicion_id as repositionid, "+
	    		"          r.documentno as documentno "+
	    		"      FROM swa_requerepo_detail rd  "+
	    		"          INNER JOIN swa_requerimientoreposicion r ON r.swa_requerimientoreposicion_id = rd.swa_requerimientoreposicion_id "+
	    		"       WHERE R.ISREQUEST = 'Y' " +
	            "         AND R.TO_M_WAREHOUSE_ID = ? " +
	    		"      GROUP BY r.swa_requerimientoreposicion_id, r.documentno ";
		
	    ResultSet result;
	    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
	    PreparedStatement st = null;

	    int iParameter = 0;
	    try {
	      st = connectionProvider.getPreparedStatement(strSql);
	      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
	      iParameter++; UtilSql.setValue(st, iParameter, 12, null, strWarehouseId);
	      result = st.executeQuery();
	      long countRecord = 0;
	      long countRecordSkip = 1;
	      boolean continueResult = true;
	
	      while(continueResult && result.next()) {
	        countRecord++;
	        CreateLinesFromRepositionData objectCreateLinesFromRepositionData = new CreateLinesFromRepositionData();
	        
	        objectCreateLinesFromRepositionData.id = UtilSql.getValue(result, "repositionid");
	        objectCreateLinesFromRepositionData.name  = UtilSql.getValue(result, "documentno");
	        objectCreateLinesFromRepositionData.rownum = Long.toString(countRecord);
	        vector.addElement(objectCreateLinesFromRepositionData);
	      }
	      result.close();
	    } catch(SQLException e){
	      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
	      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
	    } catch(Exception ex){
	      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
	      throw new ServletException("@CODE=@" + ex.getMessage());
	    } finally {
	      try {
	        connectionProvider.releasePreparedStatement(st);
	      } catch(Exception ignore){
	        ignore.printStackTrace();
	      }
	    }
	    CreateLinesFromRepositionData objectCreateLinesFromRepositionData[] = new CreateLinesFromRepositionData[vector.size()];
	    vector.copyInto(objectCreateLinesFromRepositionData);
	    return(objectCreateLinesFromRepositionData);
	}
  
  
  
  public static CreateLinesFromRepositionData[] select2(ConnectionProvider connectionProvider, String purchaseOrderImportID)   throws ServletException {
	    String strSql = "";
	    strSql = strSql + 

	    		"select sim_orderimportline_id AS orderLineID, line, sim_orderimportline.m_product_id AS productID, "+
	    		"             m_product.value as value, "+
	    		"             m_product.em_scr_internalcode as internalcode, "+
	    		"             m_product.name AS product, " +
	    		"             c_uom.name AS uom, " +
	    		"             priceactual as price, " +
	    		"             qtyordered AS ordered, " +
	    		"	     qtyreserved AS pending, " +
	    		"             qtyreserved AS arrival " +
	    		"       from sim_orderimportline  " +
	    		"          inner join m_product on m_product.m_product_id = sim_orderimportline.m_product_id " +
	    		"          inner join c_uom on c_uom.c_uom_id = sim_orderimportline.c_uom_id " +
	    		"       where sim_orderimport_id = ? " +
	    		"       AND  Docstatus = 'NG' "+
	    		"       AND  qtyreserved > 0"+
	    		"       order by line asc  ";
	    		
	    ResultSet result;
	    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
	    PreparedStatement st = null;

	    int iParameter = 0;
	    try {
	    st = connectionProvider.getPreparedStatement(strSql);
	      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
	      iParameter++; UtilSql.setValue(st, iParameter, 12, null, purchaseOrderImportID);
	      result = st.executeQuery();
	      //System.out.println(result);
	      long countRecord = 0;
	      long countRecordSkip = 1;
	      boolean continueResult = true;
	
	      while(continueResult && result.next()) {
	        countRecord++;
	        CreateLinesFromRepositionData objectCreateLinesFromRepositionData = new CreateLinesFromRepositionData();
	        
	        objectCreateLinesFromRepositionData.ORDERLINEID = UtilSql.getValue(result, "orderLineID");
	        objectCreateLinesFromRepositionData.LINE = UtilSql.getValue(result, "line");
	        objectCreateLinesFromRepositionData.PRODUCTID = UtilSql.getValue(result, "productID");
	        objectCreateLinesFromRepositionData.PRODUCTNAME = UtilSql.getValue(result, "product");
	        objectCreateLinesFromRepositionData.UOM = UtilSql.getValue(result, "uom");
	        objectCreateLinesFromRepositionData.PRICE = UtilSql.getValue(result, "price");
	        objectCreateLinesFromRepositionData.ORDERED = UtilSql.getValue(result, "ordered");
	        objectCreateLinesFromRepositionData.PENDING = UtilSql.getValue(result, "pending");
	        objectCreateLinesFromRepositionData.ARRIVAL = UtilSql.getValue(result, "arrival");
	        objectCreateLinesFromRepositionData.VALUE = UtilSql.getValue(result, "value");
                objectCreateLinesFromRepositionData.INTERNALCODE = UtilSql.getValue(result, "internalcode");
	        
	        objectCreateLinesFromRepositionData.rownum = Long.toString(countRecord);
	        
	        vector.addElement(objectCreateLinesFromRepositionData);
	      }
	      result.close();
	    } catch(SQLException e){
	      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
	      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
	    } catch(Exception ex){
	      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
	      throw new ServletException("@CODE=@" + ex.getMessage());
	    } finally {
	      try {
	        connectionProvider.releasePreparedStatement(st);
	      } catch(Exception ignore){
	        ignore.printStackTrace();
	      }
	    }
	    CreateLinesFromRepositionData objectCreateLinesFromRepositionData[] = new CreateLinesFromRepositionData[vector.size()];
	    vector.copyInto(objectCreateLinesFromRepositionData);
	    //System.out.println("DOCNO " + objectCreateLinesFromRepositionData[0].DOCNO);
	    return(objectCreateLinesFromRepositionData);
	  }

  public static CreateLinesFromRepositionData[] set()    throws ServletException {
	  CreateLinesFromRepositionData objectCreateLinesFromRepositionData[] = new CreateLinesFromRepositionData[1];
    objectCreateLinesFromRepositionData[0] = new CreateLinesFromRepositionData();
    
    objectCreateLinesFromRepositionData[0].DOCNO = "";
    objectCreateLinesFromRepositionData[0].PARTNER = "";
    objectCreateLinesFromRepositionData[0].DATEORDER = "";
    
    objectCreateLinesFromRepositionData[0].ORDERLINEID = "";
    objectCreateLinesFromRepositionData[0].LINE = "";
    objectCreateLinesFromRepositionData[0].PRODUCTID = "";
    objectCreateLinesFromRepositionData[0].PRODUCTNAME = "";
    objectCreateLinesFromRepositionData[0].UOM = "";
    objectCreateLinesFromRepositionData[0].PRICE = "";
    objectCreateLinesFromRepositionData[0].ORDERED = "";
    objectCreateLinesFromRepositionData[0].PENDING = "";
    objectCreateLinesFromRepositionData[0].ARRIVAL = "";
    objectCreateLinesFromRepositionData[0].VALUE = "";
    objectCreateLinesFromRepositionData[0].INTERNALCODE = "";
    
    
    objectCreateLinesFromRepositionData[0].CLIENTID = "";
    objectCreateLinesFromRepositionData[0].LOCATORID = "";
    objectCreateLinesFromRepositionData[0].LOCATORNAME = "";
    objectCreateLinesFromRepositionData[0].PRIORITYNO = "";
    objectCreateLinesFromRepositionData[0].LOCATORTYPE = "";
    objectCreateLinesFromRepositionData[0].LOCATORQTY = "";
    objectCreateLinesFromRepositionData[0].LOCATORQTYPRODUCT = "";

    
    
    
    
    return objectCreateLinesFromRepositionData;
  }

}