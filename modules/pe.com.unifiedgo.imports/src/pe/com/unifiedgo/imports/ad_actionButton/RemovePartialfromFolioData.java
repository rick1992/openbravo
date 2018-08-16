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

class RemovePartialfromFolioData implements FieldProvider {
static Logger log4j = Logger.getLogger(RemovePartialfromFolioData.class);
  private String InitRecordNumber="0";
  
  //begin
  public String ORDERID;
  public String PARTNER;
  public String DOCNO;
  public String DATEORDER;
  public String CURRENCY;
  public String COUNTRY;
  public String GRANDTOTAL;
  public String DOCNOORDER;
  public String DOCINVOICE;
  public String rownum;
  
  //
  

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  public String getField(String fieldName) {
    if (fieldName.equals("DOCNO"))
        return DOCNO;
    else if (fieldName.equals("ORDERID"))
        return ORDERID;
    else if (fieldName.equals("PARTNER"))
        return PARTNER;
    else if (fieldName.equals("DATEORDER"))
        return DATEORDER;
    else if (fieldName.equals("CURRENCY"))
        return CURRENCY;
    else if (fieldName.equals("COUNTRY"))
        return COUNTRY;
    else if (fieldName.equals("GRANDTOTAL"))
        return GRANDTOTAL;
    else if (fieldName.equals("DOCNOORDER"))
        return DOCNOORDER;
    else if (fieldName.equals("DOCINVOICE")) 
        return DOCINVOICE;
    else if (fieldName.equals("rownum"))
        return rownum;
   else {
     log4j.debug("Field does not exist: " + fieldName);
     return null;
   }
 }

  
  public static RemovePartialfromFolioData[] select3(ConnectionProvider connectionProvider)   throws ServletException {
	    String strSql = "";
	    strSql = strSql + 

	    		"select c_order.c_order_id as orderid, " + 
	    		"       c_bpartner.name AS c_bpartner, "   +   
	    		"       c_order.documentno As documentno,"  +  
	    		"       c_order.dateordered AS dateordered," + 
	    		"       coalesce  (c_currency.description,'undefined') AS currency," + 
	    		"       coalesce  (c_country.name,'undefined') AS country," +          
	    		"       c_order.grandtotal AS grantotal," +    
	    		"       sim_orderimport.documentno AS documentnoorder" + 
	    		"       from c_order left join c_bpartner on c_bpartner.c_bpartner_id = c_order.c_bpartner_id " +
	    		"                     left join c_currency on c_currency.c_currency_id = c_order.c_currency_id " +
	    		"                     left join c_country on c_country.c_country_id = c_order.em_sim_c_country_id " +
	    		"                     left join sim_orderimport on sim_orderimport.sim_orderimport_id = c_order.em_sim_orderimport_id " +
	    		"       where c_order.em_sim_is_import= 'Y' and c_order.docstatus= 'CO' and c_order.em_sim_is_in_folio = 'N'";
		
	    ResultSet result;
	    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
	    PreparedStatement st = null;

	    int iParameter = 0;
	    try {
	    st = connectionProvider.getPreparedStatement(strSql);
	      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
	     // iParameter++; UtilSql.setValue(st, iParameter, 12, null, purchaseOrderImportID);
	      result = st.executeQuery();
	      long countRecord = 0;
	      long countRecordSkip = 1;
	      boolean continueResult = true;
	
	      while(continueResult && result.next()) {
	        countRecord++;
	        RemovePartialfromFolioData objectRemovePartialfromFolioData = new RemovePartialfromFolioData();
	        
	        objectRemovePartialfromFolioData.ORDERID = UtilSql.getValue(result, "orderid");
	        objectRemovePartialfromFolioData.DOCNO = UtilSql.getValue(result, "documentno");
	        objectRemovePartialfromFolioData.PARTNER = UtilSql.getValue(result, "c_bpartner");
	        objectRemovePartialfromFolioData.DATEORDER = UtilSql.getDateValue(result, "dateordered", "dd-MM-yyyy");
	        objectRemovePartialfromFolioData.CURRENCY = UtilSql.getValue(result, "currency");
	        objectRemovePartialfromFolioData.COUNTRY = UtilSql.getValue(result, "country");
	        objectRemovePartialfromFolioData.GRANDTOTAL = UtilSql.getValue(result, "grantotal");
	        objectRemovePartialfromFolioData.DOCNOORDER = UtilSql.getValue(result, "documentnoorder");
	        objectRemovePartialfromFolioData.rownum = Long.toString(countRecord);
	      //  System.out.println(objectRemovePartialfromFolioData.ORDERID);
	      //  System.out.println(objectRemovePartialfromFolioData.PARTNER);
	      //  System.out.println(objectRemovePartialfromFolioData.DATEORDER);
	        
	        
	        vector.addElement(objectRemovePartialfromFolioData);
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
	    RemovePartialfromFolioData objectRemovePartialfromFolioData[] = new RemovePartialfromFolioData[vector.size()];
	    vector.copyInto(objectRemovePartialfromFolioData);
	    return(objectRemovePartialfromFolioData);
	  }
  

  public static RemovePartialfromFolioData[] select4(ConnectionProvider connectionProvider, String FolioID)   throws ServletException {
	    String strSql = "";
	    strSql = strSql + 

	    		"select c_order.c_order_id as orderid, " + 
	    		"       c_bpartner.name AS c_bpartner, "   +   
	    		"       c_order.documentno As documentno,"  +  
	    		"       c_order.dateordered AS dateordered," + 
	    		"       coalesce  (c_currency.description,'undefined') AS currency," + 
	    		"       coalesce  (c_country.name,'undefined') AS country," +          
	    		"       c_order.grandtotal AS grantotal," +    
	    		"       sim_orderimport.documentno AS documentnoorder, " + 
	    		"       coalesce(i.em_scr_physical_documentno,'--') as documentinvoice  "+
	    		"       from c_order left join c_bpartner on c_bpartner.c_bpartner_id = c_order.c_bpartner_id " +
	    		"                     left join c_currency on c_currency.c_currency_id = c_order.c_currency_id " +
	    		"                     left join c_country on c_country.c_country_id = c_order.em_sim_c_country_id " +
	    		"                     left join sim_orderimport on sim_orderimport.sim_orderimport_id = c_order.em_sim_orderimport_id " +
	    		"                     inner join c_orderline ol on c_order.c_order_id = ol.c_order_id  " +
	    		"                     left join c_invoiceline il on ol.c_orderline_id = il.c_orderline_id  " +
	    		"                     left join c_invoice i on il.c_invoice_id = i.c_invoice_id  " +
	    		"       where c_order.em_sim_is_import= 'Y' and c_order.docstatus= 'CO' and c_order.em_sim_is_in_folio = 'Y' and c_order.em_sim_folioimport_id = ?" +
	    		"       group by c_order.c_order_id,currency,documentnoorder,country,grantotal,c_bpartner.name,c_order.documentno,c_order.dateordered,i.em_scr_physical_documentno ";
		
	    ResultSet result;
	    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
	    PreparedStatement st = null;

	    int iParameter = 0;
	    try {
	    st = connectionProvider.getPreparedStatement(strSql);
	      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
	      iParameter++; UtilSql.setValue(st, iParameter, 12, null, FolioID);
	      result = st.executeQuery();
	      long countRecord = 0;
	      long countRecordSkip = 1;
	      boolean continueResult = true;
	
	      while(continueResult && result.next()) {
	        countRecord++;
	        RemovePartialfromFolioData objectRemovePartialfromFolioData = new RemovePartialfromFolioData();
	        
	        objectRemovePartialfromFolioData.ORDERID = UtilSql.getValue(result, "orderid");
	        objectRemovePartialfromFolioData.DOCNO = UtilSql.getValue(result, "documentno");
	        objectRemovePartialfromFolioData.PARTNER = UtilSql.getValue(result, "c_bpartner");
	        objectRemovePartialfromFolioData.DATEORDER = UtilSql.getDateValue(result, "dateordered", "dd-MM-yyyy");
	        objectRemovePartialfromFolioData.CURRENCY = UtilSql.getValue(result, "currency");
	        objectRemovePartialfromFolioData.COUNTRY = UtilSql.getValue(result, "country");
	        objectRemovePartialfromFolioData.GRANDTOTAL = UtilSql.getValue(result, "grantotal");
	        objectRemovePartialfromFolioData.DOCNOORDER = UtilSql.getValue(result, "documentnoorder");
	        objectRemovePartialfromFolioData.DOCINVOICE = UtilSql.getValue(result, "documentinvoice");
	        objectRemovePartialfromFolioData.rownum = Long.toString(countRecord);
	      //  System.out.println(objectRemovePartialfromFolioData.ORDERID);
	      //  System.out.println(objectRemovePartialfromFolioData.PARTNER);
	      //  System.out.println(objectRemovePartialfromFolioData.DATEORDER);
	        
	        
	        vector.addElement(objectRemovePartialfromFolioData);
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
	    RemovePartialfromFolioData objectRemovePartialfromFolioData[] = new RemovePartialfromFolioData[vector.size()];
	    vector.copyInto(objectRemovePartialfromFolioData);
	    return(objectRemovePartialfromFolioData);
	  }
  
  
  public static int removePartialWithFolio(ConnectionProvider connectionProvider, String cOrderId)    throws ServletException {
	    String strSql = "";
	    strSql = strSql + 
	      "      UPDATE C_ORDER SET EM_SIM_FOLIOIMPORT_ID=NULL " +
	      "      , EM_SIM_IS_IN_FOLIO = 'N' "+
	      "      WHERE C_ORDER_ID=?";
	    int updateCount = 0;
	    PreparedStatement st = null;

	    int iParameter = 0;
	    try {
	    st = connectionProvider.getPreparedStatement(strSql);
	      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
	      iParameter++; UtilSql.setValue(st, iParameter, 12, null, cOrderId);
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
  
  
  
  public static RemovePartialfromFolioData[] set()    throws ServletException {
	  RemovePartialfromFolioData objectRemovePartialfromFolioData[] = new RemovePartialfromFolioData[1];
    objectRemovePartialfromFolioData[0] = new RemovePartialfromFolioData();
    
    objectRemovePartialfromFolioData[0].ORDERID = "";
    objectRemovePartialfromFolioData[0].DOCNO = "";
    objectRemovePartialfromFolioData[0].PARTNER = "";
    objectRemovePartialfromFolioData[0].DATEORDER = "";
    objectRemovePartialfromFolioData[0].CURRENCY = "";
    objectRemovePartialfromFolioData[0].COUNTRY = "";
    objectRemovePartialfromFolioData[0].GRANDTOTAL = "";
    objectRemovePartialfromFolioData[0].DOCNOORDER = "";
    objectRemovePartialfromFolioData[0].DOCINVOICE = "";
    
    return objectRemovePartialfromFolioData;
  }

}