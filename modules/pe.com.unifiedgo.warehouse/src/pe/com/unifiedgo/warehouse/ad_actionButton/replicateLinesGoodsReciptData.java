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


class replicateLinesGoodsReciptData implements FieldProvider {
static Logger log4j = Logger.getLogger(replicateLinesGoodsReciptData.class);
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
    else if (fieldName.equals("PRODUCTNAME"))
        return PRODUCTNAME;
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
    
    
    

    
    
   else {
     log4j.debug("Field does not exist: " + fieldName);
     return null;
   }
 }
  
  public static replicateLinesGoodsReciptData[] selectLocator(ConnectionProvider connectionProvider, String strWarehouseId, String strMProductid, String chkOnlyLocatorOut, String rbtnSelectLocator)   throws ServletException {
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
	        replicateLinesGoodsReciptData objectreplicateLinesGoodsReciptData = new replicateLinesGoodsReciptData();
	        
	        objectreplicateLinesGoodsReciptData.CLIENTID = UtilSql.getValue(result, "clientid");
	        objectreplicateLinesGoodsReciptData.LOCATORID = UtilSql.getValue(result, "locatorid");
	        objectreplicateLinesGoodsReciptData.LOCATORNAME = UtilSql.getValue(result, "locatorname");
	        objectreplicateLinesGoodsReciptData.PRIORITYNO = UtilSql.getValue(result, "priorityno");
	        objectreplicateLinesGoodsReciptData.LOCATORTYPE = UtilSql.getValue(result, "locatortype");
	        objectreplicateLinesGoodsReciptData.LOCATORQTY = UtilSql.getValue(result, "locatorqty");
	        objectreplicateLinesGoodsReciptData.LOCATORQTYPRODUCT = UtilSql.getValue(result, "locatorqtyproduct");
	        
	      //  System.out.println(objectreplicateLinesGoodsReciptData.DOCNO);
	      //  System.out.println(objectreplicateLinesGoodsReciptData.PARTNER);
	      //  System.out.println(objectreplicateLinesGoodsReciptData.DATEORDER);
	        
	        
	        vector.addElement(objectreplicateLinesGoodsReciptData);
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
	    replicateLinesGoodsReciptData objectreplicateLinesGoodsReciptData[] = new replicateLinesGoodsReciptData[vector.size()];
	    vector.copyInto(objectreplicateLinesGoodsReciptData);
	    return(objectreplicateLinesGoodsReciptData);
	  }
  
  public static replicateLinesGoodsReciptData[] select2(ConnectionProvider connectionProvider, String purchaseOrderImportID)   throws ServletException {
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
	        replicateLinesGoodsReciptData objectreplicateLinesGoodsReciptData = new replicateLinesGoodsReciptData();
	        
	        objectreplicateLinesGoodsReciptData.ORDERLINEID = UtilSql.getValue(result, "orderLineID");
	        objectreplicateLinesGoodsReciptData.LINE = UtilSql.getValue(result, "line");
	        objectreplicateLinesGoodsReciptData.PRODUCTID = UtilSql.getValue(result, "productID");
	        objectreplicateLinesGoodsReciptData.PRODUCTNAME = UtilSql.getValue(result, "product");
	        objectreplicateLinesGoodsReciptData.UOM = UtilSql.getValue(result, "uom");
	        objectreplicateLinesGoodsReciptData.PRICE = UtilSql.getValue(result, "price");
	        objectreplicateLinesGoodsReciptData.ORDERED = UtilSql.getValue(result, "ordered");
	        objectreplicateLinesGoodsReciptData.PENDING = UtilSql.getValue(result, "pending");
	        objectreplicateLinesGoodsReciptData.ARRIVAL = UtilSql.getValue(result, "arrival");
	        objectreplicateLinesGoodsReciptData.VALUE = UtilSql.getValue(result, "value");
                objectreplicateLinesGoodsReciptData.INTERNALCODE = UtilSql.getValue(result, "internalcode");
	        
	        objectreplicateLinesGoodsReciptData.rownum = Long.toString(countRecord);
	        
	       
	        
	        
	        vector.addElement(objectreplicateLinesGoodsReciptData);
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
	    replicateLinesGoodsReciptData objectreplicateLinesGoodsReciptData[] = new replicateLinesGoodsReciptData[vector.size()];
	    vector.copyInto(objectreplicateLinesGoodsReciptData);
	    //System.out.println("DOCNO " + objectreplicateLinesGoodsReciptData[0].DOCNO);
	    return(objectreplicateLinesGoodsReciptData);
	  }

  public static replicateLinesGoodsReciptData[] set()    throws ServletException {
	  replicateLinesGoodsReciptData objectreplicateLinesGoodsReciptData[] = new replicateLinesGoodsReciptData[1];
    objectreplicateLinesGoodsReciptData[0] = new replicateLinesGoodsReciptData();
    
    objectreplicateLinesGoodsReciptData[0].DOCNO = "";
    objectreplicateLinesGoodsReciptData[0].PARTNER = "";
    objectreplicateLinesGoodsReciptData[0].DATEORDER = "";
    
    objectreplicateLinesGoodsReciptData[0].ORDERLINEID = "";
    objectreplicateLinesGoodsReciptData[0].LINE = "";
    objectreplicateLinesGoodsReciptData[0].PRODUCTID = "";
    objectreplicateLinesGoodsReciptData[0].PRODUCTNAME = "";
    objectreplicateLinesGoodsReciptData[0].UOM = "";
    objectreplicateLinesGoodsReciptData[0].PRICE = "";
    objectreplicateLinesGoodsReciptData[0].ORDERED = "";
    objectreplicateLinesGoodsReciptData[0].PENDING = "";
    objectreplicateLinesGoodsReciptData[0].ARRIVAL = "";
    objectreplicateLinesGoodsReciptData[0].VALUE = "";
    objectreplicateLinesGoodsReciptData[0].INTERNALCODE = "";
    
    
    objectreplicateLinesGoodsReciptData[0].CLIENTID = "";
    objectreplicateLinesGoodsReciptData[0].LOCATORID = "";
    objectreplicateLinesGoodsReciptData[0].LOCATORNAME = "";
    objectreplicateLinesGoodsReciptData[0].PRIORITYNO = "";
    objectreplicateLinesGoodsReciptData[0].LOCATORTYPE = "";
    objectreplicateLinesGoodsReciptData[0].LOCATORQTY = "";
    objectreplicateLinesGoodsReciptData[0].LOCATORQTYPRODUCT = "";

    
    
    
    
    return objectreplicateLinesGoodsReciptData;
  }

}