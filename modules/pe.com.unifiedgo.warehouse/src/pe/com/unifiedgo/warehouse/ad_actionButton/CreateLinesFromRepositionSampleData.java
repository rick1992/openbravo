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


class CreateLinesFromRepositionSampleData implements FieldProvider {
static Logger log4j = Logger.getLogger(CreateLinesFromRepositionSampleData.class);
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
  public String requirementid;
  public String requirementlineid;
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
  
  public static CreateLinesFromRepositionSampleData[] selectLocator(ConnectionProvider connectionProvider, String strWarehouseId, String strMProductid, String chkOnlyLocatorOut, String rbtnSelectLocator)   throws ServletException {
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
	        CreateLinesFromRepositionSampleData objectCreateLinesFromRepositionSampleData = new CreateLinesFromRepositionSampleData();
	        
	        objectCreateLinesFromRepositionSampleData.CLIENTID = UtilSql.getValue(result, "clientid");
	        objectCreateLinesFromRepositionSampleData.LOCATORID = UtilSql.getValue(result, "locatorid");
	        objectCreateLinesFromRepositionSampleData.LOCATORNAME = UtilSql.getValue(result, "locatorname");
	        objectCreateLinesFromRepositionSampleData.PRIORITYNO = UtilSql.getValue(result, "priorityno");
	        objectCreateLinesFromRepositionSampleData.LOCATORTYPE = UtilSql.getValue(result, "locatortype");
	        objectCreateLinesFromRepositionSampleData.LOCATORQTY = UtilSql.getValue(result, "locatorqty");
	        objectCreateLinesFromRepositionSampleData.LOCATORQTYPRODUCT = UtilSql.getValue(result, "locatorqtyproduct");
	        
	        vector.addElement(objectCreateLinesFromRepositionSampleData);
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
	    CreateLinesFromRepositionSampleData objectCreateLinesFromRepositionSampleData[] = new CreateLinesFromRepositionSampleData[vector.size()];
	    vector.copyInto(objectCreateLinesFromRepositionSampleData);
	    return(objectCreateLinesFromRepositionSampleData);
	  }
  
  public static CreateLinesFromRepositionSampleData[] selectReposition_OrderPending(ConnectionProvider connectionProvider, String strRepositionrequestId)   throws ServletException {
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
	      
	      System.out.println("OJO: " + st);
	      
	      result = st.executeQuery();
	      long countRecord = 0;
	      long countRecordSkip = 1;
	      boolean continueResult = true;
	
	      while(continueResult && result.next()) {
	        countRecord++;
	        CreateLinesFromRepositionSampleData objectCreateLinesFromRepositionSampleData = new CreateLinesFromRepositionSampleData();
	        
	        objectCreateLinesFromRepositionSampleData.CLIENTID = UtilSql.getValue(result, "clientid");
	        objectCreateLinesFromRepositionSampleData.requirementid = UtilSql.getValue(result, "requirementid");
	        objectCreateLinesFromRepositionSampleData.requirementlineid = UtilSql.getValue(result, "requirementlineid");
	        objectCreateLinesFromRepositionSampleData.productvalue = UtilSql.getValue(result, "productvalue");
	        objectCreateLinesFromRepositionSampleData.qtypending = UtilSql.getValue(result, "qtypending");
	        objectCreateLinesFromRepositionSampleData.pname = UtilSql.getValue(result, "pname");
	        objectCreateLinesFromRepositionSampleData.rownum = Long.toString(countRecord);
	        vector.addElement(objectCreateLinesFromRepositionSampleData);
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
	    CreateLinesFromRepositionSampleData objectCreateLinesFromRepositionSampleData[] = new CreateLinesFromRepositionSampleData[vector.size()];
	    vector.copyInto(objectCreateLinesFromRepositionSampleData);
	    return(objectCreateLinesFromRepositionSampleData);
	}
  
  public static CreateLinesFromRepositionSampleData[] selectReposition_SamplePending(ConnectionProvider connectionProvider, String strRepositionrequestId)   throws ServletException {
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
	      System.out.println("HOLIS: " + st);
	      result = st.executeQuery();
	      long countRecord = 0;
	      long countRecordSkip = 1;
	      boolean continueResult = true;
	
	      while(continueResult && result.next()) {
	        countRecord++;
	        CreateLinesFromRepositionSampleData objectCreateLinesFromRepositionSampleData = new CreateLinesFromRepositionSampleData();
	        
	        objectCreateLinesFromRepositionSampleData.CLIENTID = UtilSql.getValue(result, "clientid");
	        objectCreateLinesFromRepositionSampleData.requirementid = UtilSql.getValue(result, "requirementid");
	        objectCreateLinesFromRepositionSampleData.requirementlineid = UtilSql.getValue(result, "requirementlineid");
	        objectCreateLinesFromRepositionSampleData.productvalue = UtilSql.getValue(result, "productvalue");
	        objectCreateLinesFromRepositionSampleData.qtypending = UtilSql.getValue(result, "qtypending");
	        objectCreateLinesFromRepositionSampleData.pname = UtilSql.getValue(result, "pname");
	        objectCreateLinesFromRepositionSampleData.rownum = Long.toString(countRecord);
	        vector.addElement(objectCreateLinesFromRepositionSampleData);
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
	    CreateLinesFromRepositionSampleData objectCreateLinesFromRepositionSampleData[] = new CreateLinesFromRepositionSampleData[vector.size()];
	    vector.copyInto(objectCreateLinesFromRepositionSampleData);
	    return(objectCreateLinesFromRepositionSampleData);
	}
  
  
  
  public static CreateLinesFromRepositionSampleData[] selectReposition_Request(ConnectionProvider connectionProvider, String strAdOrgId, String strWarehouseId)   throws ServletException {
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
	      System.out.println("--: " + st);
	      result = st.executeQuery();
	      long countRecord = 0;
	      long countRecordSkip = 1;
	      boolean continueResult = true;
	
	      while(continueResult && result.next()) {
	        countRecord++;
	        CreateLinesFromRepositionSampleData objectCreateLinesFromRepositionSampleData = new CreateLinesFromRepositionSampleData();
	        
	        objectCreateLinesFromRepositionSampleData.id = UtilSql.getValue(result, "repositionid");
	        objectCreateLinesFromRepositionSampleData.name  = UtilSql.getValue(result, "documentno");
	        objectCreateLinesFromRepositionSampleData.rownum = Long.toString(countRecord);
	        vector.addElement(objectCreateLinesFromRepositionSampleData);
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
	    CreateLinesFromRepositionSampleData objectCreateLinesFromRepositionSampleData[] = new CreateLinesFromRepositionSampleData[vector.size()];
	    vector.copyInto(objectCreateLinesFromRepositionSampleData);
	    return(objectCreateLinesFromRepositionSampleData);
	}
  
  public static CreateLinesFromRepositionSampleData[] selectReposition_SampleOrder(ConnectionProvider connectionProvider, String stradOrgId, String strBPartnerId, String strRepSampleId)   throws ServletException {
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
	      System.out.println(st);
	      result = st.executeQuery();
	      long countRecord = 0;
	      long countRecordSkip = 1;
	      boolean continueResult = true;
	
	      while(continueResult && result.next()) {
	        countRecord++;
	        CreateLinesFromRepositionSampleData objectCreateLinesFromRepositionSampleData = new CreateLinesFromRepositionSampleData();
	        
	        objectCreateLinesFromRepositionSampleData.id = UtilSql.getValue(result, "repositionid");
	        objectCreateLinesFromRepositionSampleData.name  = UtilSql.getValue(result, "documentno");
	        objectCreateLinesFromRepositionSampleData.rownum = Long.toString(countRecord);
	        vector.addElement(objectCreateLinesFromRepositionSampleData);
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
	    CreateLinesFromRepositionSampleData objectCreateLinesFromRepositionSampleData[] = new CreateLinesFromRepositionSampleData[vector.size()];
	    vector.copyInto(objectCreateLinesFromRepositionSampleData);
	    return(objectCreateLinesFromRepositionSampleData);
	}
  
  
  public static CreateLinesFromRepositionSampleData[] selectReposition_RepositionOrder(ConnectionProvider connectionProvider, String strToWarehouseId)   throws ServletException {
	    String strSql = "";
	    strSql = strSql + 

	    		"   SELECT distinct r.swa_requerimientoreposicion_id as repositionid, "+
	    		"          r.documentno || ' - ' || i.em_scr_physical_documentno as documentno "+
	    		"      FROM swa_requerepo_detail rd  "+
	    		"          INNER JOIN swa_requerimientoreposicion r ON r.swa_requerimientoreposicion_id = rd.swa_requerimientoreposicion_id "+
	    		"          INNER JOIN c_doctype d ON d.c_doctype_id = r.c_doctype_id " +
	    		"          INNER JOIN m_inout i ON  i.em_swa_requireposicion_id = r.swa_requerimientoreposicion_id " +
	    		"       WHERE r.status='CO' " +
	    		"         AND d.em_sco_specialdoctype ='SWAREPOSITION' " + //solo Vamos a recibir transferencias desde Ordenes 
	            "         AND r.to_m_warehouse_id = ? " +
	    		"         AND i.em_swa_isfromreposition='Y' " +
	            "         AND i.issotrx = 'Y' " +
	    		"         AND i.docstatus='CO' "+
	    		"    GROUP BY r.swa_requerimientoreposicion_id, r.documentno, r.documentno , rd.qtyshipped , rd.qtyreceipt, i.em_scr_physical_documentno " + 
	            "      HAVING COALESCE(rd.qtyshipped,0) - COALESCE(rd.qtyreceipt,0) > 0    ";
		
	    ResultSet result;
	    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
	    PreparedStatement st = null;

	    int iParameter = 0;
	    try {
	      st = connectionProvider.getPreparedStatement(strSql);
	      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
	      iParameter++; UtilSql.setValue(st, iParameter, 12, null, strToWarehouseId);
	      
	      System.out.println(st);
	      
	      result = st.executeQuery();
	      long countRecord = 0;
	      long countRecordSkip = 1;
	      boolean continueResult = true;
	
	      while(continueResult && result.next()) {
	        countRecord++;
	        CreateLinesFromRepositionSampleData objectCreateLinesFromRepositionSampleData = new CreateLinesFromRepositionSampleData();
	        
	        objectCreateLinesFromRepositionSampleData.id = UtilSql.getValue(result, "repositionid");
	        objectCreateLinesFromRepositionSampleData.name  = UtilSql.getValue(result, "documentno");
	        objectCreateLinesFromRepositionSampleData.rownum = Long.toString(countRecord);
	        vector.addElement(objectCreateLinesFromRepositionSampleData);
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
	    CreateLinesFromRepositionSampleData objectCreateLinesFromRepositionSampleData[] = new CreateLinesFromRepositionSampleData[vector.size()];
	    vector.copyInto(objectCreateLinesFromRepositionSampleData);
	    return(objectCreateLinesFromRepositionSampleData);
	}

  
  
  public static CreateLinesFromRepositionSampleData[] selectReposition_Order(ConnectionProvider connectionProvider, String strAdOrgId, String strWarehouseId)   throws ServletException {
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
	        CreateLinesFromRepositionSampleData objectCreateLinesFromRepositionSampleData = new CreateLinesFromRepositionSampleData();
	        
	        objectCreateLinesFromRepositionSampleData.id = UtilSql.getValue(result, "repositionid");
	        objectCreateLinesFromRepositionSampleData.name  = UtilSql.getValue(result, "documentno");
	        objectCreateLinesFromRepositionSampleData.rownum = Long.toString(countRecord);
	        vector.addElement(objectCreateLinesFromRepositionSampleData);
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
	    CreateLinesFromRepositionSampleData objectCreateLinesFromRepositionSampleData[] = new CreateLinesFromRepositionSampleData[vector.size()];
	    vector.copyInto(objectCreateLinesFromRepositionSampleData);
	    return(objectCreateLinesFromRepositionSampleData);
	}
  
  
  
  public static CreateLinesFromRepositionSampleData[] select2(ConnectionProvider connectionProvider, String purchaseOrderImportID)   throws ServletException {
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
	        CreateLinesFromRepositionSampleData objectCreateLinesFromRepositionSampleData = new CreateLinesFromRepositionSampleData();
	        
	        objectCreateLinesFromRepositionSampleData.ORDERLINEID = UtilSql.getValue(result, "orderLineID");
	        objectCreateLinesFromRepositionSampleData.LINE = UtilSql.getValue(result, "line");
	        objectCreateLinesFromRepositionSampleData.PRODUCTID = UtilSql.getValue(result, "productID");
	        objectCreateLinesFromRepositionSampleData.PRODUCTNAME = UtilSql.getValue(result, "product");
	        objectCreateLinesFromRepositionSampleData.UOM = UtilSql.getValue(result, "uom");
	        objectCreateLinesFromRepositionSampleData.PRICE = UtilSql.getValue(result, "price");
	        objectCreateLinesFromRepositionSampleData.ORDERED = UtilSql.getValue(result, "ordered");
	        objectCreateLinesFromRepositionSampleData.PENDING = UtilSql.getValue(result, "pending");
	        objectCreateLinesFromRepositionSampleData.ARRIVAL = UtilSql.getValue(result, "arrival");
	        objectCreateLinesFromRepositionSampleData.VALUE = UtilSql.getValue(result, "value");
                objectCreateLinesFromRepositionSampleData.INTERNALCODE = UtilSql.getValue(result, "internalcode");
	        
	        objectCreateLinesFromRepositionSampleData.rownum = Long.toString(countRecord);
	        
	       
	        
	        
	        vector.addElement(objectCreateLinesFromRepositionSampleData);
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
	    CreateLinesFromRepositionSampleData objectCreateLinesFromRepositionSampleData[] = new CreateLinesFromRepositionSampleData[vector.size()];
	    vector.copyInto(objectCreateLinesFromRepositionSampleData);
	    //System.out.println("DOCNO " + objectCreateLinesFromRepositionSampleData[0].DOCNO);
	    return(objectCreateLinesFromRepositionSampleData);
	  }

  public static CreateLinesFromRepositionSampleData[] set()    throws ServletException {
	  CreateLinesFromRepositionSampleData objectCreateLinesFromRepositionSampleData[] = new CreateLinesFromRepositionSampleData[1];
    objectCreateLinesFromRepositionSampleData[0] = new CreateLinesFromRepositionSampleData();
    
    objectCreateLinesFromRepositionSampleData[0].DOCNO = "";
    objectCreateLinesFromRepositionSampleData[0].PARTNER = "";
    objectCreateLinesFromRepositionSampleData[0].DATEORDER = "";
    
    objectCreateLinesFromRepositionSampleData[0].ORDERLINEID = "";
    objectCreateLinesFromRepositionSampleData[0].LINE = "";
    objectCreateLinesFromRepositionSampleData[0].PRODUCTID = "";
    objectCreateLinesFromRepositionSampleData[0].PRODUCTNAME = "";
    objectCreateLinesFromRepositionSampleData[0].UOM = "";
    objectCreateLinesFromRepositionSampleData[0].PRICE = "";
    objectCreateLinesFromRepositionSampleData[0].ORDERED = "";
    objectCreateLinesFromRepositionSampleData[0].PENDING = "";
    objectCreateLinesFromRepositionSampleData[0].ARRIVAL = "";
    objectCreateLinesFromRepositionSampleData[0].VALUE = "";
    objectCreateLinesFromRepositionSampleData[0].INTERNALCODE = "";
    
    
    objectCreateLinesFromRepositionSampleData[0].CLIENTID = "";
    objectCreateLinesFromRepositionSampleData[0].LOCATORID = "";
    objectCreateLinesFromRepositionSampleData[0].LOCATORNAME = "";
    objectCreateLinesFromRepositionSampleData[0].PRIORITYNO = "";
    objectCreateLinesFromRepositionSampleData[0].LOCATORTYPE = "";
    objectCreateLinesFromRepositionSampleData[0].LOCATORQTY = "";
    objectCreateLinesFromRepositionSampleData[0].LOCATORQTYPRODUCT = "";

    
    
    
    
    return objectCreateLinesFromRepositionSampleData;
  }

}