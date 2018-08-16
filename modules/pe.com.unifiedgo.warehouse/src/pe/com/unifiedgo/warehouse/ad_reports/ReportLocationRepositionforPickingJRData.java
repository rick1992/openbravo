//Sqlc generated V1.O00-1
package pe.com.unifiedgo.warehouse.ad_reports;

import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.data.UtilSql;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.database.SessionInfo;
import org.openbravo.service.db.QueryTimeOutUtil;

class ReportLocationRepositionforPickingJRData implements FieldProvider {
  static Logger log4j = Logger.getLogger(ReportLocationRepositionforPickingJRData.class);
  private String InitRecordNumber = "0";
  public String adorgid;
  public String productid;
  public String locatorid;
  public String locatorvalue;
  public String locatorx;
  public String locatory;
  public String locatorz;
  public String stockmin;
  public String qtyonhand;
  public String requiredqty;
  public String name;
  public String rownum;

  public String padre;
  public String id;

  public String clientid;
  public String locatorname;
  public String priorityno;
  public String locatortype;
  public String locatorempty;
  public String locatorqty;
  public String locatorproductphysical;
  public String locatorproductreserved;
  public String locatorproductavailable;
  public String movementDate;
  
  public String codproducto;
  public String desproducto;
  public String codalmacen;
  public String desalmacen;


  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  @Override
  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("adorgid"))
      return adorgid;
    else if (fieldName.equalsIgnoreCase("clientid"))
      return clientid;
    else if (fieldName.equalsIgnoreCase("productid"))
      return productid;
    else if (fieldName.equalsIgnoreCase("locatorid"))
      return locatorid;
    else if (fieldName.equalsIgnoreCase("locatorvalue"))
      return locatorvalue;
    else if (fieldName.equalsIgnoreCase("movementDate"))
        return movementDate;
    else if (fieldName.equalsIgnoreCase("locatorx"))
      return locatorx;
    else if (fieldName.equalsIgnoreCase("locatory"))
      return locatory;
    else if (fieldName.equalsIgnoreCase("locatorz"))
      return locatorz;
    else if (fieldName.equalsIgnoreCase("stockmin"))
      return stockmin;
    else if (fieldName.equalsIgnoreCase("qtyonhand"))
      return qtyonhand;
    else if (fieldName.equalsIgnoreCase("requiredqty"))
      return requiredqty;
    else if (fieldName.equalsIgnoreCase("name"))
      return name;
    else if (fieldName.equalsIgnoreCase("locatorname"))
      return locatorname;
    else if (fieldName.equalsIgnoreCase("locatortype"))
      return locatortype;
    else if (fieldName.equalsIgnoreCase("priorityno"))
      return priorityno;
    else if (fieldName.equalsIgnoreCase("locatorempty"))
      return locatorempty;
    else if (fieldName.equalsIgnoreCase("locatorqty"))
      return locatorqty;
    else if (fieldName.equalsIgnoreCase("locatorproductphysical"))
      return locatorproductphysical;
    else if (fieldName.equalsIgnoreCase("locatorproductreserved"))
      return locatorproductreserved;
    else if (fieldName.equalsIgnoreCase("locatorproductavailable"))
      return locatorproductavailable;
    
    else if (fieldName.equalsIgnoreCase("codproducto"))
        return codproducto;
    else if (fieldName.equalsIgnoreCase("desproducto"))
        return desproducto;
    else if (fieldName.equalsIgnoreCase("codalmacen"))
        return codalmacen;
    else if (fieldName.equalsIgnoreCase("desalmacen"))
        return desalmacen;

    else if (fieldName.equals("rownum"))
      return rownum;
    else if (fieldName.equals("padre"))
      return padre;
    else if (fieldName.equals("id"))
      return id;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  public static String selectMproduct(ConnectionProvider connectionProvider, String mProductId)
      throws ServletException {
    String strSql = "";
    strSql = strSql + "      SELECT (M_PRODUCT.VALUE || ' - ' || M_PRODUCT.NAME) as name "
        + "      FROM M_PRODUCT" + "      WHERE M_PRODUCT.M_PRODUCT_ID = ?";

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

  public static ReportLocationRepositionforPickingJRData[] selectLocator(
      ConnectionProvider connectionProvider, String strWarehouseId, String strMProductid,
      String chkOnlyLocatorOut, String rbtnSelectLocator, String strLocatorType,String strLocatorValue) throws ServletException {
    return selectLocator(connectionProvider, strWarehouseId, strMProductid, chkOnlyLocatorOut,
        rbtnSelectLocator,strLocatorType, strLocatorValue, 0, 0);
  }

  public static ReportLocationRepositionforPickingJRData[] selectLocator(
      ConnectionProvider connectionProvider, String strWarehouseId, String strMProductid,
      String chkOnlyLocatorOut, String rbtnSelectLocator, String strLocatorType, String strLocatorValue ,int firstRegister, int numberRegisters)
      throws ServletException {
    String strSql = "";
    strSql = strSql
        +

        "   SELECT L.AD_CLIENT_ID as clientid, "
        + "          L.M_LOCATOR_ID AS locatorid, "
        + "          L.value as locatorname,"
        + "          L.x  as locatorx,"
        + "          L.y  as locatory,"
        + "          L.z  as locatorz,"
        + "          L.PRIORITYNO as priorityno, "     
        
       // + "          CASE WHEN L.EM_OBWHS_TYPE = 'OUT' THEN 'De Salida' ELSE 'Stock' END locatortype,"
        
        
        + "         COALESCE((SELECT trl.name FROM ad_ref_list_trl trl "
        + "                               INNER JOIN ad_ref_list  rli on rli.ad_ref_list_id = trl.ad_ref_list_id  "
        + "                               INNER JOIN AD_REFERENCE adr on adr.ad_reference_id = rli.ad_reference_id "
        + "                               WHERE adr.AD_REFERENCE_ID = '359DDEE9B03540499E6E4759D026F646'"
        + "                                 AND rli.value = L.EM_OBWHS_TYPE"
        + "                                 AND trl.ad_language = 'es_PE'),'-') AS locatortype, "
        
        + "         COALESCE(SD.qtyonHand,0) as locatorqty, "
        + "         COALESCE((Select movementdate from m_transaction where m_locator_id = L.m_locator_id order by movementdate desc limit 1),now()) as lastmovement,"
        + "          CASE WHEN COALESCE(SUM(swap.totalqty),0) = 0 "
       // + "                    THEN SD.qtyonHand  "
        + "                   THEN COALESCE((SELECT SUM(qtyonHand) FROM M_STORAGE_DETAIL WHERE M_PRODUCT_ID = '" +strMProductid+ "' AND M_LOCATOR_ID = L.M_LOCATOR_ID),0)  "
        + "               ELSE  COALESCE(SUM(swap.totalqty),0) END AS locatorproductphysical, "
        +
        // "          COALESCE(SUM(swap.totalqty) , COALESCE(SELECT SUM(SD.qtyonHand) FROM M_STORAGE_DETAIL WHERE M_PRODUCT_ID = ? AND M_LOCATOR_ID = L.M_LOCATOR_ID,0)) as locatorproductphysical, "+
        "           COALESCE(SUM(swap.reserved) , 0) as locatorproductreserved, "
        + "         COALESCE(SUM(swap.qtyonhand) , 0) as locatorproductavailable "
        +

        "      FROM M_LOCATOR L "
        //+ "          LEFT JOIN M_STORAGE_DETAIL SD ON L.M_LOCATOR_ID = SD.M_LOCATOR_ID "
        + "     LEFT JOIN (SELECT m_locator_id, COALESCE(SUM(qtyonHand),0) as qtyonhand FROM M_STORAGE_DETAIL group by m_locator_id) SD ON "
        + "                L.m_locator_id = SD.m_locator_id  "
        + "          LEFT JOIN swa_product_by_anaquel_v swap  on L.m_locator_id = swap.m_locator_id "
        + "                                                    and  L.m_warehouse_id =  swap.m_warehouse_id "
        + "                                                    and swap.m_product_id = ?  "
        + "       WHERE L.M_warehouse_id = ? ";

    strSql = strSql + ((!strLocatorType.equals("")) ? " AND L.EM_OBWHS_TYPE = ? " : "");
    // strSql = strSql + (((rbtnSelectLocator.equals("created")))?"AND SD.M_PRODUCT_ID = ?":"");
    strSql = strSql
        + (((rbtnSelectLocator.equals("created"))) ? " AND L.M_LOCATOR_ID IN (SELECT DISTINCT M_LOCATOR_ID FROM M_STORAGE_DETAIL WHERE M_PRODUCT_ID = ?)"
            : "");
    
   
    
    strSql = strSql + ((!strLocatorValue.equals("")) ? "AND lower(L.value) = ? " : "");
    
    
    strSql = strSql
        + "       GROUP BY L.AD_CLIENT_ID, L.M_LOCATOR_ID , L.PRIORITYNO, L.EM_OBWHS_TYPE , L.x, L.y,L.z, L.value,SD.qtyonHand  ";

    strSql = strSql
        + (((rbtnSelectLocator.equals("pending"))) ? "HAVING COALESCE(SD.qtyonHand ,0)=0" : "");
    
    strSql = strSql
            + (((!strMProductid.equals(""))&& !rbtnSelectLocator.equals("pending")) ? "HAVING COALESCE(SUM(swap.totalqty), COALESCE((SELECT SUM(qtyonHand) FROM M_STORAGE_DETAIL WHERE M_PRODUCT_ID = '" +strMProductid+ "' AND M_LOCATOR_ID = L.M_LOCATOR_ID),0))<>0"
                : "");
    

    strSql = strSql + "       ORDER BY L.PRIORITYNO ASC, L.X, L.Y, L.Z ";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      //iParameter++;
      //UtilSql.setValue(st, iParameter, 12, null, strMProductid);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, strMProductid);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, strWarehouseId);
      if(strLocatorType!= null && !(strLocatorType.equals(""))){
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, strLocatorType);
      }
      if (rbtnSelectLocator != null && !(rbtnSelectLocator.equals(""))
          && rbtnSelectLocator.equals("created")) {
        iParameter++;
        UtilSql.setValue(st, iParameter, 12, null, strMProductid);
      }
      if(strLocatorValue!= null && !(strLocatorValue.equals(""))){
          iParameter++;
          UtilSql.setValue(st, iParameter, 12, null, strLocatorValue);
      }
      
      
      
      
      System.out.println("st: " + st);
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
        ReportLocationRepositionforPickingJRData objectLocationRepositionData = new ReportLocationRepositionforPickingJRData();

        objectLocationRepositionData.clientid = UtilSql.getValue(result, "clientid");
        objectLocationRepositionData.locatorid = UtilSql.getValue(result, "locatorid");
        objectLocationRepositionData.locatorname = UtilSql.getValue(result, "locatorname");
        objectLocationRepositionData.locatorx = UtilSql.getValue(result, "locatorx");
        objectLocationRepositionData.locatory = UtilSql.getValue(result, "locatory");
        objectLocationRepositionData.locatorz = UtilSql.getValue(result, "locatorz");
        objectLocationRepositionData.priorityno = UtilSql.getValue(result, "priorityno");
        objectLocationRepositionData.locatortype = UtilSql.getValue(result, "locatortype");
        objectLocationRepositionData.locatorqty = UtilSql.getValue(result, "locatorqty");
        objectLocationRepositionData.movementDate = UtilSql.getDateValue(result, "lastmovement");
        objectLocationRepositionData.locatorproductphysical = UtilSql.getValue(result,
            "locatorproductphysical");
        objectLocationRepositionData.locatorproductreserved = UtilSql.getValue(result,
            "locatorproductreserved");
        objectLocationRepositionData.locatorproductavailable = UtilSql.getValue(result,
            "locatorproductavailable");
        objectLocationRepositionData.rownum = Long.toString(countRecord);
        objectLocationRepositionData.InitRecordNumber = Integer.toString(firstRegister);
        // System.out.println(objectLocationRepositionData.DOCNO);
        // System.out.println(objectLocationRepositionData.PARTNER);
        // System.out.println(objectLocationRepositionData.DATEORDER);

        vector.addElement(objectLocationRepositionData);
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
    ReportLocationRepositionforPickingJRData objectLocationRepositionData[] = new ReportLocationRepositionforPickingJRData[vector
        .size()];
    vector.copyInto(objectLocationRepositionData);
    return (objectLocationRepositionData);
  }
  
  
  public static ReportLocationRepositionforPickingJRData[] selectLocatorNew(
	      ConnectionProvider connectionProvider, String strWarehouseId, String strMProductid,
	      String chkOnlyLocatorOut, String rbtnSelectLocator, String strLocatorType,String strLocatorValue) throws ServletException {
	    return selectLocatorNew(connectionProvider, strWarehouseId, strMProductid, chkOnlyLocatorOut,
	        rbtnSelectLocator,strLocatorType, strLocatorValue, 0, 0);
	  }

	  public static ReportLocationRepositionforPickingJRData[] selectLocatorNew(
	      ConnectionProvider connectionProvider, String strWarehouseId, String strMProductid,
	      String chkOnlyLocatorOut, String rbtnSelectLocator, String strLocatorType, String strLocatorValue ,int firstRegister, int numberRegisters)
	      throws ServletException {
	    String strSql = "";
	    strSql = strSql
	        

	     +" select mp.value as codproducto, mp.name as desproducto, mw.name as desalmacen, "
		 +" ml.x as locatorx, ml.y as locatory, ml.z as locatorz , spw.totalqty  as locatorproductphysical "
		 +" ,spw.reserved as locatorproductreserved "
		
		
		 +"  from swa_product_by_anaquel_v spw "
		
		 +" inner join m_product mp on spw.m_product_id=mp.m_product_id "
		 +" inner join m_warehouse mw on spw.m_warehouse_id=mw.m_warehouse_id "
		 +" inner join m_locator ml on spw.m_locator_id=ml.m_locator_id "
		
		 +" where coalesce(spw.totalqty,0) <> '0' and 1=1 and ";
		 if(strMProductid!=null && strMProductid.compareToIgnoreCase("")!=0){
			  strSql = strSql+ " spw.m_product_id='" +strMProductid +"' and ";
		 }
		 if(strWarehouseId!=null && strWarehouseId.compareToIgnoreCase("")!=0){
			  strSql = strSql+ " spw.m_warehouse_id='" +strWarehouseId +"' and ";
		 }
		 if(strLocatorValue!=null && strLocatorValue.compareToIgnoreCase("")!=0){
			  strSql = strSql+ " lower(ml.value)='" +strLocatorValue +"' and ";
		 }
		 strSql = strSql + " 2=2 "
		
		 +" order by mw.name , mp.name , ml.x,ml.y,ml.z ";


	    ResultSet result;
	    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
	    PreparedStatement st = null;

	    int iParameter = 0;
	    try {
	      st = connectionProvider.getPreparedStatement(strSql);
	      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
	      //iParameter++;
	      //UtilSql.setValue(st, iParameter, 12, null, strMProductid);
//	      iParameter++;
//	      UtilSql.setValue(st, iParameter, 12, null, strMProductid);
//	      iParameter++;
//	      UtilSql.setValue(st, iParameter, 12, null, strWarehouseId);
//	      if(strLocatorType!= null && !(strLocatorType.equals(""))){
//	      iParameter++;
//	      UtilSql.setValue(st, iParameter, 12, null, strLocatorType);
//	      }
//	      if (rbtnSelectLocator != null && !(rbtnSelectLocator.equals(""))
//	          && rbtnSelectLocator.equals("created")) {
//	        iParameter++;
//	        UtilSql.setValue(st, iParameter, 12, null, strMProductid);
//	      }
//	      if(strLocatorValue!= null && !(strLocatorValue.equals(""))){
//	          iParameter++;
//	          UtilSql.setValue(st, iParameter, 12, null, strLocatorValue);
//	      }
//	      

	      System.out.println("st: " + st);
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
	        ReportLocationRepositionforPickingJRData objectLocationRepositionData = new ReportLocationRepositionforPickingJRData();

//	        objectLocationRepositionData.clientid = UtilSql.getValue(result, "clientid");
//	        objectLocationRepositionData.locatorid = UtilSql.getValue(result, "locatorid");
//	        objectLocationRepositionData.locatorname = UtilSql.getValue(result, "locatorname");
	        objectLocationRepositionData.locatorx = UtilSql.getValue(result, "locatorx");
	        objectLocationRepositionData.locatory = UtilSql.getValue(result, "locatory");
	        objectLocationRepositionData.locatorz = UtilSql.getValue(result, "locatorz");
//	        objectLocationRepositionData.priorityno = UtilSql.getValue(result, "priorityno");
//	        objectLocationRepositionData.locatortype = UtilSql.getValue(result, "locatortype");
//	        objectLocationRepositionData.locatorqty = UtilSql.getValue(result, "locatorqty");
//	        objectLocationRepositionData.movementDate = UtilSql.getDateValue(result, "lastmovement");
	        objectLocationRepositionData.locatorproductphysical = UtilSql.getValue(result,
	            "locatorproductphysical");
	        objectLocationRepositionData.locatorproductreserved = UtilSql.getValue(result,
	            "locatorproductreserved");
//	        objectLocationRepositionData.locatorproductavailable = UtilSql.getValue(result,
//	            "locatorproductavailable");
	        
	        objectLocationRepositionData.codproducto = UtilSql.getValue(result, "codproducto");
	        objectLocationRepositionData.desproducto = UtilSql.getValue(result, "desproducto");
	        objectLocationRepositionData.desalmacen = UtilSql.getValue(result, "desalmacen");
	        
	        
	        objectLocationRepositionData.rownum = Long.toString(countRecord);
	        objectLocationRepositionData.InitRecordNumber = Integer.toString(firstRegister);
	        
	        


	        // System.out.println(objectLocationRepositionData.DOCNO);
	        // System.out.println(objectLocationRepositionData.PARTNER);
	        // System.out.println(objectLocationRepositionData.DATEORDER);

	        vector.addElement(objectLocationRepositionData);
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
	    ReportLocationRepositionforPickingJRData objectLocationRepositionData[] = new ReportLocationRepositionforPickingJRData[vector
	        .size()];
	    vector.copyInto(objectLocationRepositionData);
	    return (objectLocationRepositionData);
	  }
  

  public static ReportLocationRepositionforPickingJRData[] selectLocatorTest(
      ConnectionProvider connectionProvider, String strWarehouseId, String strMProductid,
      String chkOnlyLocatorOut, String rbtnSelectLocator, int firstRegister, int numberRegisters)
      throws ServletException {
    String strSql = "";
    strSql = strSql +

    "   SELECT L.AD_CLIENT_ID AS clientid, L.M_LOCATOR_ID AS locatorid, "
        + "          L.value as locatorname " + "      FROM M_LOCATOR L " + "       LIMIT 2 ";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());

      //System.out.println("QUERY: " + st);
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
        ReportLocationRepositionforPickingJRData objectLocationRepositionData = new ReportLocationRepositionforPickingJRData();

        objectLocationRepositionData.clientid = UtilSql.getValue(result, "clientid");
        objectLocationRepositionData.locatorid = UtilSql.getValue(result, "locatorid");
        objectLocationRepositionData.locatorname = UtilSql.getValue(result, "locatorname");

        objectLocationRepositionData.rownum = Long.toString(countRecord);
        objectLocationRepositionData.InitRecordNumber = Integer.toString(firstRegister);
        // System.out.println(objectLocationRepositionData.DOCNO);
        // System.out.println(objectLocationRepositionData.PARTNER);
        // System.out.println(objectLocationRepositionData.DATEORDER);

        vector.addElement(objectLocationRepositionData);
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
    ReportLocationRepositionforPickingJRData objectLocationRepositionData[] = new ReportLocationRepositionforPickingJRData[vector
        .size()];
    vector.copyInto(objectLocationRepositionData);
    return (objectLocationRepositionData);
  }

  public static String selectMWarehouse(ConnectionProvider connectionProvider, String mWarehouseId)
      throws ServletException {
    String strSql = "";
    strSql = strSql + "      SELECT M_WAREHOUSE.VALUE || ' - ' || M_WAREHOUSE.NAME AS NAME"
        + "      FROM M_WAREHOUSE" + "      WHERE M_WAREHOUSE.M_WAREHOUSE_ID = ?";

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

  public static ReportLocationRepositionforPickingJRData[] select(String adOrgId,
      String adClientId, String mWarehouseId) throws ServletException {
    return select(adOrgId, adClientId, mWarehouseId, 0, 0);
  }

  public static ReportLocationRepositionforPickingJRData[] select(String adOrgId,
      String adClientId, String mWarehouseId, int firstRegister, int numberRegisters)
      throws ServletException {

    String strSql = "";
    strSql = "SELECT l.m_locator_id, l.value, l.x, l.y, l.z, p.value || ' ' ||p.name as name, "
        + "          spd.m_product_id, spd.stockmin, COALESCE((SELECT COALESCE(QtyOnHand,0) "
        + "                                             FROM m_storage_detail sd "
        + "                                            WHERE sd.m_locator_id=spd.m_locator_id "
        + "                                              AND sd.m_product_id=spd.m_product_id "
        + "                                              AND sd.isActive='Y'),0) as qtyonhand "

        + "     FROM swa_storagepicking_detail spd, m_locator l, m_warehouse w, m_product p "
        + "    WHERE spd.m_locator_id=l.m_locator_id "
        + "      AND l.m_warehouse_id=w.m_warehouse_id "
        + "      AND spd.m_product_id=p.m_product_id " + "      AND l.em_obwhs_type='OUT' "
        + "      AND spd.m_product_id=p.m_product_id " + "      AND spd.ad_org_id='" + adOrgId
        + "' " + "      AND spd.ad_client_id='" + adClientId + "' "
        + "      AND l.m_warehouse_id='" + mWarehouseId + "' "
        + "      AND spd.stockmin>COALESCE((SELECT COALESCE(QtyOnHand,0) "
        + "                          FROM m_storage_detail sd "
        + "                         WHERE sd.m_locator_id=spd.m_locator_id "
        + "                           AND sd.m_product_id=spd.m_product_id "
        + "                           AND sd.isActive='Y'),0)" + "    ORDER BY l.m_locator_id ; ";

    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
  //  System.out.println("strSql:" + strSql);
    DecimalFormat dfInt = new DecimalFormat("0");
    dfInt.setRoundingMode(RoundingMode.HALF_UP);

    long countRecord = 0;
    try {
    	
    	//System.out.println("strSql: " + strSql);
      Query sqlQuery = OBDal.getInstance().getSession().createSQLQuery(strSql);

      List<Object> data = sqlQuery.list();
      for (int k = 0; k < data.size(); k++) {
        Object[] obj = (Object[]) data.get(k);
        countRecord++;

        ReportLocationRepositionforPickingJRData objRptLocationRepositionforPickingJRData = new ReportLocationRepositionforPickingJRData();

        objRptLocationRepositionforPickingJRData.adorgid = adOrgId;
        objRptLocationRepositionforPickingJRData.locatorid = (String) obj[0];
        objRptLocationRepositionforPickingJRData.locatorvalue = (String) obj[1];
        objRptLocationRepositionforPickingJRData.locatorx = (String) obj[2];
        objRptLocationRepositionforPickingJRData.locatory = (String) obj[3];
        objRptLocationRepositionforPickingJRData.locatorz = (String) obj[4];
        objRptLocationRepositionforPickingJRData.name = (String) obj[5];
        objRptLocationRepositionforPickingJRData.productid = (String) obj[6];
        objRptLocationRepositionforPickingJRData.stockmin = dfInt.format(obj[7]);
        objRptLocationRepositionforPickingJRData.qtyonhand = dfInt.format(obj[8]);
        objRptLocationRepositionforPickingJRData.requiredqty = String.valueOf(Integer
            .parseInt(dfInt.format(obj[7])) - Integer.parseInt(dfInt.format(obj[8])));

        objRptLocationRepositionforPickingJRData.rownum = Long.toString(countRecord);
        objRptLocationRepositionforPickingJRData.InitRecordNumber = Integer.toString(firstRegister);

        vector.addElement(objRptLocationRepositionforPickingJRData);

      }
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    }

    ReportLocationRepositionforPickingJRData objRptLocationRepositionforPickingJRData[] = new ReportLocationRepositionforPickingJRData[vector
        .size()];
    vector.copyInto(objRptLocationRepositionforPickingJRData);

    return (objRptLocationRepositionforPickingJRData);
  }
  
  
  public static ReportLocationRepositionforPickingJRData[] selectNew(String adOrgId,
	      String adClientId, String mWarehouseId) throws ServletException {
	    return selectNew(adOrgId, adClientId, mWarehouseId, 0, 0);
	  }

	  public static ReportLocationRepositionforPickingJRData[] selectNew(String adOrgId,
	      String adClientId, String mWarehouseId, int firstRegister, int numberRegisters)
	      throws ServletException {

	    String strSql = "";
	    strSql = "SELECT l.m_locator_id, l.value, l.x, l.y, l.z, p.value || ' ' ||p.name as name, "
	        + "          spd.m_product_id, spd.stockmin, COALESCE((SELECT COALESCE(QtyOnHand,0) "
	        + "                                             FROM m_storage_detail sd "
	        + "                                            WHERE sd.m_locator_id=spd.m_locator_id "
	        + "                                              AND sd.m_product_id=spd.m_product_id "
	        + "                                              AND sd.isActive='Y'),0) as qtyonhand "
	        + ", p.value as codproducto,p.name as desproducto,w.value as codalmacen,w.name as desalmacen"

	        + "     FROM swa_storagepicking_detail spd, m_locator l, m_warehouse w, m_product p "
	        + "    WHERE spd.m_locator_id=l.m_locator_id "
	        + "      AND l.m_warehouse_id=w.m_warehouse_id "
	        + "      AND spd.m_product_id=p.m_product_id " + "      AND l.em_obwhs_type='OUT' "
	        + "      AND spd.m_product_id=p.m_product_id " + "      AND spd.ad_org_id='" + adOrgId
	        + "' " + "      AND spd.ad_client_id='" + adClientId + "' "
	        + "      AND l.m_warehouse_id='" + mWarehouseId + "' "
	        + "      AND spd.stockmin>COALESCE((SELECT COALESCE(QtyOnHand,0) "
	        + "                          FROM m_storage_detail sd "
	        + "                         WHERE sd.m_locator_id=spd.m_locator_id "
	        + "                           AND sd.m_product_id=spd.m_product_id "
	        + "                           AND sd.isActive='Y'),0)" + "    ORDER BY l.m_locator_id ; ";

	    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
	  //  System.out.println("strSql:" + strSql);
	    DecimalFormat dfInt = new DecimalFormat("0");
	    dfInt.setRoundingMode(RoundingMode.HALF_UP);

	    long countRecord = 0;
	    try {
	    	
	    	//System.out.println("strSql: " + strSql);
	      Query sqlQuery = OBDal.getInstance().getSession().createSQLQuery(strSql);

	      List<Object> data = sqlQuery.list();
	      for (int k = 0; k < data.size(); k++) {
	        Object[] obj = (Object[]) data.get(k);
	        countRecord++;

	        ReportLocationRepositionforPickingJRData objRptLocationRepositionforPickingJRData = new ReportLocationRepositionforPickingJRData();

	        objRptLocationRepositionforPickingJRData.adorgid = adOrgId;
	        objRptLocationRepositionforPickingJRData.locatorid = (String) obj[0];
	        objRptLocationRepositionforPickingJRData.locatorvalue = (String) obj[1];
	        objRptLocationRepositionforPickingJRData.locatorx = (String) obj[2];
	        objRptLocationRepositionforPickingJRData.locatory = (String) obj[3];
	        objRptLocationRepositionforPickingJRData.locatorz = (String) obj[4];
	        objRptLocationRepositionforPickingJRData.name = (String) obj[5];
	        objRptLocationRepositionforPickingJRData.productid = (String) obj[6];
	        objRptLocationRepositionforPickingJRData.stockmin = dfInt.format(obj[7]);
	        objRptLocationRepositionforPickingJRData.qtyonhand = dfInt.format(obj[8]);
	        objRptLocationRepositionforPickingJRData.requiredqty = String.valueOf(Integer
	            .parseInt(dfInt.format(obj[7])) - Integer.parseInt(dfInt.format(obj[8])));
	        
	        objRptLocationRepositionforPickingJRData.codproducto = (String) obj[9];
	        objRptLocationRepositionforPickingJRData.desproducto = (String) obj[10];
	        objRptLocationRepositionforPickingJRData.codalmacen = (String) obj[11];
	        objRptLocationRepositionforPickingJRData.desalmacen = (String) obj[12];

	        objRptLocationRepositionforPickingJRData.rownum = Long.toString(countRecord);
	        objRptLocationRepositionforPickingJRData.InitRecordNumber = Integer.toString(firstRegister);

	        vector.addElement(objRptLocationRepositionforPickingJRData);

	      }
	    } catch (Exception ex) {
	      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
	      throw new ServletException("@CODE=@" + ex.getMessage());
	    }

	    ReportLocationRepositionforPickingJRData objRptLocationRepositionforPickingJRData[] = new ReportLocationRepositionforPickingJRData[vector
	        .size()];
	    vector.copyInto(objRptLocationRepositionforPickingJRData);

	    return (objRptLocationRepositionforPickingJRData);
	  }
  

  public static ReportLocationRepositionforPickingJRData[] set() throws ServletException {
    ReportLocationRepositionforPickingJRData objRptLocationRepositionforPickingJRData[] = new ReportLocationRepositionforPickingJRData[1];
    objRptLocationRepositionforPickingJRData[0] = new ReportLocationRepositionforPickingJRData();
    objRptLocationRepositionforPickingJRData[0].adorgid = "";
    objRptLocationRepositionforPickingJRData[0].productid = "";
    objRptLocationRepositionforPickingJRData[0].locatorid = "";
    objRptLocationRepositionforPickingJRData[0].name = "";
    objRptLocationRepositionforPickingJRData[0].locatorvalue = "";
    objRptLocationRepositionforPickingJRData[0].locatorx = "";
    objRptLocationRepositionforPickingJRData[0].locatory = "";
    objRptLocationRepositionforPickingJRData[0].locatorz = "";
    objRptLocationRepositionforPickingJRData[0].stockmin = "";
    objRptLocationRepositionforPickingJRData[0].qtyonhand = "";
    objRptLocationRepositionforPickingJRData[0].requiredqty = "";
    objRptLocationRepositionforPickingJRData[0].movementDate = "";

    return objRptLocationRepositionforPickingJRData;
  }

  public static ReportLocationRepositionforPickingJRData[] selectMWarehouseForParentOrgDouble(
      ConnectionProvider connectionProvider, String adUserClient) throws ServletException {
    return selectMWarehouseForParentOrgDouble(connectionProvider, adUserClient, 0, 0);
  }

  public static ReportLocationRepositionforPickingJRData[] selectMWarehouseForParentOrgDouble(
      ConnectionProvider connectionProvider, String adUserClient, int firstRegister,
      int numberRegisters) throws ServletException {
    String strSql = "";
    strSql = strSql
        + "       SELECT M_WAREHOUSE.AD_ORG_ID AS PADRE, M_WAREHOUSE.M_WAREHOUSE_ID AS ID, TO_CHAR(M_WAREHOUSE.NAME) AS NAME"
        + "         FROM M_WAREHOUSE " + "        WHERE 1=1"
        + "          AND M_WAREHOUSE.ISACTIVE='Y'" + "          AND M_WAREHOUSE.AD_Client_ID IN(";
    strSql = strSql + ((adUserClient == null || adUserClient.equals("")) ? "" : adUserClient);
    strSql = strSql
        + ")"
        + "       UNION "
        + "       SELECT null AS PADRE, M_WAREHOUSE.M_WAREHOUSE_ID AS ID, TO_CHAR(M_WAREHOUSE.NAME) AS NAME"
        + "         FROM M_WAREHOUSE " + "        WHERE 2=2 "
        + "         AND M_WAREHOUSE.ISACTIVE='Y'" + "          AND M_WAREHOUSE.AD_Client_ID IN(";
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
        ReportLocationRepositionforPickingJRData objRptLocationRepositionforPickingJRData = new ReportLocationRepositionforPickingJRData();
        objRptLocationRepositionforPickingJRData.padre = UtilSql.getValue(result, "padre");
        objRptLocationRepositionforPickingJRData.id = UtilSql.getValue(result, "id");
        objRptLocationRepositionforPickingJRData.name = UtilSql.getValue(result, "name");

        objRptLocationRepositionforPickingJRData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objRptLocationRepositionforPickingJRData);
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
    ReportLocationRepositionforPickingJRData objRptLocationRepositionforPickingJRData[] = new ReportLocationRepositionforPickingJRData[vector
        .size()];
    vector.copyInto(objRptLocationRepositionforPickingJRData);
    return (objRptLocationRepositionforPickingJRData);
  }

  public static String selectBpartner(ConnectionProvider connectionProvider, String cBpartnerId)
      throws ServletException {
    String strSql = "";
    strSql = strSql + "      SELECT C_BPARTNER.NAME" + "      FROM C_BPARTNER"
        + "      WHERE C_BPARTNER.C_BPARTNER_ID = ?";

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

}