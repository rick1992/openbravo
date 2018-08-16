//Sqlc generated V1.O00-1
package pe.com.unifiedgo.imports.ad_actionButton;

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

class AddFromOrderData implements FieldProvider {
static Logger log4j = Logger.getLogger(AddFromOrderData.class);
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
    else if (fieldName.equals("rownum"))
        return rownum;
    else if (fieldName.equals("VALUE"))
        return VALUE;
    else if (fieldName.equals("INTERNALCODE"))
        return INTERNALCODE;

    
    
   else {
     log4j.debug("Field does not exist: " + fieldName);
     return null;
   }
 }


  public static AddFromOrderData[] select1(ConnectionProvider connectionProvider, String purchaseOrderImportID)   throws ServletException {
	    String strSql = "";
	    strSql = strSql + 

	    		"   select sim_orderimport.documentno AS documentno,"+
	    		"          c_bpartner.name AS c_bpartner,"+
	    		"          sim_orderimport.dateordered AS dateordered "+
	    		"      from sim_orderimport "+
	    		"          inner join c_bpartner on c_bpartner.c_bpartner_id = sim_orderimport.c_bpartner_id "+
	    		"      where sim_orderimport_id = ? ";
	    		
		
	    ResultSet result;
	    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
	    PreparedStatement st = null;

	    int iParameter = 0;
	    try {
	    st = connectionProvider.getPreparedStatement(strSql);
	      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
	      iParameter++; UtilSql.setValue(st, iParameter, 12, null, purchaseOrderImportID);
	      result = st.executeQuery();
	      long countRecord = 0;
	      long countRecordSkip = 1;
	      boolean continueResult = true;
	
	      while(continueResult && result.next()) {
	        countRecord++;
	        AddFromOrderData objectAddFromOrderData = new AddFromOrderData();
	        
	        
	        objectAddFromOrderData.DOCNO = UtilSql.getValue(result, "documentno");
	        objectAddFromOrderData.PARTNER = UtilSql.getValue(result, "c_bpartner");
	        objectAddFromOrderData.DATEORDER = UtilSql.getDateValue(result, "dateordered", "dd-MM-yyyy");
	        
	      //  System.out.println(objectAddFromOrderData.DOCNO);
	      //  System.out.println(objectAddFromOrderData.PARTNER);
	      //  System.out.println(objectAddFromOrderData.DATEORDER);
	        
	        
	        vector.addElement(objectAddFromOrderData);
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
	    AddFromOrderData objectAddFromOrderData[] = new AddFromOrderData[vector.size()];
	    vector.copyInto(objectAddFromOrderData);
	    return(objectAddFromOrderData);
	  }
  
  public static AddFromOrderData[] select2(ConnectionProvider connectionProvider, String purchaseOrderImportID)   throws ServletException {
	    String strSql = "";
	    strSql = strSql + 

	    		"select sim_orderimportline_id AS orderLineID, line, sim_orderimportline.m_product_id AS productID, "+
	    		"             m_product.value as value,  " +
	    		"             m_product.em_scr_internalcode as internalcode,  " +
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
	      long countRecord = 0;
	      long countRecordSkip = 1;
	      boolean continueResult = true;
	
	      while(continueResult && result.next()) {
	        countRecord++;
	        AddFromOrderData objectAddFromOrderData = new AddFromOrderData();
	        
	        objectAddFromOrderData.ORDERLINEID = UtilSql.getValue(result, "orderLineID");
	        objectAddFromOrderData.LINE = UtilSql.getValue(result, "line");
	        objectAddFromOrderData.PRODUCTID = UtilSql.getValue(result, "productID");
	        objectAddFromOrderData.PRODUCTNAME = UtilSql.getValue(result, "product");
	        objectAddFromOrderData.UOM = UtilSql.getValue(result, "uom");
	        objectAddFromOrderData.PRICE = UtilSql.getValue(result, "price");
	        objectAddFromOrderData.ORDERED = UtilSql.getValue(result, "ordered");
	        objectAddFromOrderData.PENDING = UtilSql.getValue(result, "pending");
	        objectAddFromOrderData.ARRIVAL = UtilSql.getValue(result, "arrival");
	        objectAddFromOrderData.VALUE = UtilSql.getValue(result, "value");
	        objectAddFromOrderData.INTERNALCODE = UtilSql.getValue(result, "internalcode");
	        objectAddFromOrderData.rownum = Long.toString(countRecord);
	        
	       
	        
	        
	        vector.addElement(objectAddFromOrderData);
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
	    AddFromOrderData objectAddFromOrderData[] = new AddFromOrderData[vector.size()];
	    vector.copyInto(objectAddFromOrderData);
	    //System.out.println("DOCNO " + objectAddFromOrderData[0].DOCNO);
	    return(objectAddFromOrderData);
	  }

  public static AddFromOrderData[] set()    throws ServletException {
	  AddFromOrderData objectAddFromOrderData[] = new AddFromOrderData[1];
    objectAddFromOrderData[0] = new AddFromOrderData();
    
    objectAddFromOrderData[0].DOCNO = "";
    objectAddFromOrderData[0].PARTNER = "";
    objectAddFromOrderData[0].DATEORDER = "";
    
    objectAddFromOrderData[0].ORDERLINEID = "";
    objectAddFromOrderData[0].LINE = "";
    objectAddFromOrderData[0].PRODUCTID = "";
    objectAddFromOrderData[0].PRODUCTNAME = "";
    objectAddFromOrderData[0].UOM = "";
    objectAddFromOrderData[0].PRICE = "";
    objectAddFromOrderData[0].ORDERED = "";
    objectAddFromOrderData[0].PENDING = "";
    objectAddFromOrderData[0].ARRIVAL = "";
    objectAddFromOrderData[0].VALUE = "";
    objectAddFromOrderData[0].INTERNALCODE = "";
    
    
    
    return objectAddFromOrderData;
  }

}