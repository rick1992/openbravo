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

class FormLocationMovementsJRData implements FieldProvider {
  static Logger log4j = Logger.getLogger(FormLocationMovementsJRData.class);
  private String InitRecordNumber = "0";
  public String clientid;
  public String mproductid;
  public String mproductvalue;
  public String mproductname;
  
  public String mtransactionid;
  public String mlocatorid;
  public String movementdate;
  public String movementhour;
  public String qtypositive;
  public String qtynegative;
  public String movementdescription;
  
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
    else if(fieldName.equalsIgnoreCase("clientid"))
    	return clientid;
    else if(fieldName.equalsIgnoreCase("mproductvalue"))
    	return mproductvalue; 
    else if (fieldName.equals("mtransactionid"))
        return mtransactionid;
    else if (fieldName.equals("mlocatorid"))
        return mlocatorid;
    else if (fieldName.equals("movementdate"))
        return movementdate;
    else if (fieldName.equals("movementhour"))
        return movementhour;
    else if (fieldName.equals("qtypositive"))
        return qtypositive;
    else if (fieldName.equals("qtynegative"))
        return qtynegative;
    else if (fieldName.equals("movementdescription"))
        return movementdescription;
    else if (fieldName.equals("rownum"))
        return rownum;
    
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }
  

   
   public static FormLocationMovementsJRData[] selectDetailProductLocator(VariablesSecureApp vars,
		       String adClientId,  String strM_Locator_ID ) throws ServletException {
	   
	    String strSql = "";	   
	    strSql = strSql + "   select mt.m_transaction_id, "
	    		+ "   mt.ad_client_id, "
	    		+ "   mt.m_locator_id, "
	    		+ "   mt.m_product_id, "
	    		+ "   p.value || ' - ' || p.name as productname, "
	    		+ "   to_char(TRUNC(mt.trxprocessdate),'dd/mm/yyyy') as movementdate, "
	    		+ "   to_char(mt.created,'hh24:mi:ss') as movementhour, "
	    		+ "   case when mt.movementqty >=0 Then to_char(mt.movementqty) else '' end as positiveqty, "
	    		+ "   case when mt.movementqty < 0 Then to_char(mt.movementqty*-1) else '' end as negativeqty, "
	    		+ "   mt.movementqty as movementqty, "
	    		+ "   mt.em_ssa_combo_item_id, "
	    		+ "   cb.name as movementDescription "
	    		
	    		
	        + "   from m_transaction mt  "
	    	+ "     inner join m_product p on p.m_product_id = mt.m_product_id "
	    	+ "     left join scr_combo_item cb on cb.scr_combo_item_id = mt.em_ssa_combo_item_id "
	        + "     where mt.m_locator_id = '" + strM_Locator_ID + "'"
	        + "     order by mt.trxprocessdate desc limit 20" ;
	    
	
	    ResultSet result;
	    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
	    PreparedStatement st = null;
      // System.out.println(strSql);
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
	        	FormLocationMovementsJRData objLocationDetailsJRData = new FormLocationMovementsJRData();
	        	objLocationDetailsJRData.mtransactionid = obj[0].toString();
	        	objLocationDetailsJRData.clientid = obj[1].toString();
		        objLocationDetailsJRData.mlocatorid = obj[2].toString();
		        objLocationDetailsJRData.mproductid = obj[3].toString();
		        objLocationDetailsJRData.mproductname = obj[4].toString();
		        objLocationDetailsJRData.movementdate = obj[5].toString();
		        objLocationDetailsJRData.movementhour = obj[6].toString();
		        objLocationDetailsJRData.qtypositive = obj[7].toString();
		        objLocationDetailsJRData.qtynegative = obj[8].toString();
		        objLocationDetailsJRData.movementdescription = obj[11].toString();
		       
		        objLocationDetailsJRData.rownum = Long.toString(countRecord);	
		        vector.addElement(objLocationDetailsJRData);
	        }
	       
	   
	    } catch (Exception ex) {
	      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
	      throw new ServletException("@CODE=@" + ex.getMessage());
	    } 
	    
	    FormLocationMovementsJRData objectFormData[] = new FormLocationMovementsJRData[vector.size()];
	    vector.copyInto(objectFormData);
	    return (objectFormData);

   }

  public static FormLocationMovementsJRData[] set() throws ServletException {
	  FormLocationMovementsJRData objReservStockDetail[] = new FormLocationMovementsJRData[1];
    objReservStockDetail[0] = new FormLocationMovementsJRData();
    objReservStockDetail[0].mproductid = "";
    objReservStockDetail[0].mproductname = "";
    
    objReservStockDetail[0].mtransactionid = "";
    objReservStockDetail[0].mlocatorid = "";
    objReservStockDetail[0].movementdate = "";
    objReservStockDetail[0].movementhour = "";
    objReservStockDetail[0].qtypositive = "";
    objReservStockDetail[0].qtynegative = "";
    objReservStockDetail[0].movementdescription = "";
    
    
    
    return objReservStockDetail;
  }
}

