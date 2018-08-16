//Sqlc generated V1.O00-1
package pe.com.unifiedgo.warehouse.ad_forms;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.data.UtilSql;
import org.openbravo.database.SessionInfo;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.onhandquantity.Reservation;
import org.openbravo.model.procurement.RequisitionLine;
import org.openbravo.service.db.QueryTimeOutUtil;


import pe.com.unifiedgo.warehouse.data.SWAProductByAnaquelV;

class FormLocationDetailsJRData implements FieldProvider {
  static Logger log4j = Logger.getLogger(FormLocationDetailsJRData.class);
  private String InitRecordNumber = "0";
  public String clientid;
  public String mproductid;
  public String mproductvalue;
  public String mproductname;
  public String reservationid;
  public String reservationref;
  public String reservationname;
  public String reservationdate;
  public String resrefclient;
  public String reservationuser;
  public String qtyreserved;
  public String quantity;
  public String orgname;
  public String qtyavailable;
  public String rownum;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  @Override
  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("mproductid"))
      return mproductid;
    else if (fieldName.equalsIgnoreCase("mproductname"))
      return mproductname;
    else if (fieldName.equalsIgnoreCase("reservationid"))
      return reservationid;
    else if (fieldName.equalsIgnoreCase("reservationref"))
      return reservationref;
    else if (fieldName.equalsIgnoreCase("reservationname"))
      return reservationname;
    else if (fieldName.equalsIgnoreCase("reservationdate"))
      return reservationdate;
    else if (fieldName.equalsIgnoreCase("resrefclient"))
      return resrefclient;
    else if (fieldName.equalsIgnoreCase("reservationuser"))
      return reservationuser;
    else if (fieldName.equalsIgnoreCase("qtyreserved"))
      return qtyreserved;
    else if (fieldName.equalsIgnoreCase("quantity"))
      return quantity;
    else if (fieldName.equalsIgnoreCase("orgname"))
      return orgname;
    else if(fieldName.equalsIgnoreCase("clientid"))
    	return clientid;
    else if(fieldName.equalsIgnoreCase("mproductvalue"))
    	return mproductvalue; 
    else if(fieldName.equalsIgnoreCase("qtyavailable"))
    	return qtyavailable; 
    else if (fieldName.equals("rownum"))
      return rownum;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  
  
  

   
   public static FormLocationDetailsJRData[] selectDetailProductLocator(VariablesSecureApp vars,
		       String adClientId,  String strM_Locator_ID ) throws ServletException {
	   
	    String strSql = "";	   
	    strSql = strSql + "   select p.ad_client_id , p.m_product_id, p.value, p.name, COALESCE(swa.totalqty,0) as totalqty, COALESCE(swa.reserved,0) as reserved, COALESCE(swa.qtyonhand,0) as qtyonhand"
	        + "   from swa_product_by_anaquel_v swa"
	    	+ "     inner join m_product p on swa.m_product_id = p.m_product_id "
	        + "     where swa.m_locator_id = '" + strM_Locator_ID + "'" 
	        + "       and coalesce(swa.totalqty,0) <> '0'";
	    
	
	    ResultSet result;
	    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
	    PreparedStatement st = null;
       //System.out.println("strSql" + strSql);
	    int iParameter = 0;
	    try {
	    	
	    	Query sqlQuery = OBDal.getInstance().getSession().createSQLQuery(strSql);
	        List<Object> data = sqlQuery.list();
	        long countRecord = 0;
	        long countRecordSkip = 1;
	        boolean continueResult = true;
	        
	        for (int k = 0; k < data.size(); k++) {
	        	Object[] obj = (Object[]) data.get(k);

	        	countRecord++;
	        	FormLocationDetailsJRData objLocationDetailsJRData = new FormLocationDetailsJRData();
		        objLocationDetailsJRData.clientid = obj[0].toString();
		        objLocationDetailsJRData.mproductid = obj[1].toString();
		        objLocationDetailsJRData.mproductvalue = obj[2].toString();
		        objLocationDetailsJRData.mproductname = obj[3].toString();
		        objLocationDetailsJRData.quantity = obj[4].toString();
		        objLocationDetailsJRData.qtyreserved = obj[5].toString();
		        
		       
		        objLocationDetailsJRData.qtyavailable = obj[6].toString();
		        objLocationDetailsJRData.rownum = Long.toString(countRecord);	
		        vector.addElement(objLocationDetailsJRData);
	        }
	       
	   
	    } catch (Exception ex) {
	      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
	      throw new ServletException("@CODE=@" + ex.getMessage());
	    } 
	    
	    FormLocationDetailsJRData objectFormData[] = new FormLocationDetailsJRData[vector.size()];
	    vector.copyInto(objectFormData);
	    return (objectFormData);

   }

  public static FormLocationDetailsJRData[] set() throws ServletException {
	  FormLocationDetailsJRData objReservStockDetail[] = new FormLocationDetailsJRData[1];
    objReservStockDetail[0] = new FormLocationDetailsJRData();
    objReservStockDetail[0].mproductid = "";
    objReservStockDetail[0].mproductname = "";
    objReservStockDetail[0].reservationid = "";
    objReservStockDetail[0].reservationref = "";
    objReservStockDetail[0].reservationname = "";
    objReservStockDetail[0].reservationdate = "";
    objReservStockDetail[0].resrefclient = "";
    objReservStockDetail[0].reservationuser = "";
    objReservStockDetail[0].qtyreserved = "";
    objReservStockDetail[0].quantity = "";
    objReservStockDetail[0].orgname = "";
    return objReservStockDetail;
  }
}

