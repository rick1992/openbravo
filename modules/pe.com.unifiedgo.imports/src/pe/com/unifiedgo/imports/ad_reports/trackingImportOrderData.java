//Sqlc generated V1.O00-1
package pe.com.unifiedgo.imports.ad_reports;

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



class trackingImportOrderData implements FieldProvider {
  static Logger log4j = Logger.getLogger(trackingImportOrderData.class);
  private String InitRecordNumber = "0";
  
  public String searchkey;
  
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
  
  //--------------
  public String orderid;
  public String clientid;
  public String orgname;
  public String orgid;
  public String ordernum;   
  public String orderdate;
  public String scurrency;
  public String currencyid;
  public String ordertotallines;
  public String taxamt;
  public String ordertotal;
  //------------
  public String orderlineid;
  public String lineclientid;
  public String docnumberorder;
  public String productid;
  public String fromcodeproductid;   
  public String codeproduct;
  public String qtyorderline;
  public String pricelist;
  public String unitprice;
  public String linetax;
  public String totallineprice;
  
  //------------
  
  public String orderreviewid;
  public String orderreviewstate;
  public String orderreviewdocnum;
  
  //---------------
  
  public String partnername;
  public String documentno;
  public String qtyordered;
  public String qtyreceived;
  public String qtydifference;
  public String cierramanual;
  public String datepromised;
  public String ordergrandtotal;
  
  //-------------------
  
  public String orderimportlineid;
  public String productkey;
  public String requisitiondocno;
  public String internalcode;
  public String partidaarancel;
  public String productname;
  public String uomname;
  public String productqty;
  public String productreceived;
  public String productdiff;
  public String productprice;
  
  
  //-------------------
  public String partiallineid;
  public String partialid;
  public String partialdocno;
  public String partialproductid;
  public String partialqty;
  public String datedespprv; //
  public String datedespacho;//
  public String datetoforwarder;//
  public String dateshipment;
  public String datearrivalcapital;
  public String dateaproxarrivalware;
  public String datearrivalwarehouse;
  public String importname;
  public String adv;
  public String tlcadv;
  public String invoicenro;
  public String dateinvoice;
  public String datedue;
  public String invoicegrandtotal;
  public String invoicetotalpaid;
  
  
  
  
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
    else if (fieldName.equalsIgnoreCase("name"))
      return name;
    
    //
    else if (fieldName.equalsIgnoreCase("orderid"))
        return orderid;
    else if (fieldName.equalsIgnoreCase("clientid"))
        return clientid;
    else if (fieldName.equalsIgnoreCase("orgname"))
        return orgname;
    else if (fieldName.equalsIgnoreCase("orgid"))
        return orgid;
    else if (fieldName.equalsIgnoreCase("ordernum"))
        return ordernum;
    else if (fieldName.equalsIgnoreCase("orderdate"))
        return orderdate;
    else if (fieldName.equalsIgnoreCase("scurrency"))
        return scurrency;
    else if (fieldName.equalsIgnoreCase("currencyid"))
        return currencyid;
    else if (fieldName.equalsIgnoreCase("ordertotallines"))
        return ordertotallines;
    else if (fieldName.equalsIgnoreCase("taxamt"))
        return taxamt;
    else if (fieldName.equalsIgnoreCase("ordertotal"))
        return ordertotal;
    
    //
    
    //
    else if (fieldName.equalsIgnoreCase("orderlineid"))
        return orderlineid;
    else if (fieldName.equalsIgnoreCase("lineclientid"))
        return lineclientid;
    else if (fieldName.equalsIgnoreCase("docnumberorder"))
        return docnumberorder;
    else if (fieldName.equalsIgnoreCase("productid"))
        return productid;
    else if (fieldName.equalsIgnoreCase("fromcodeproductid"))
        return fromcodeproductid;
    else if (fieldName.equalsIgnoreCase("codeproduct"))
        return codeproduct;
    else if (fieldName.equalsIgnoreCase("productname"))
        return productname;
    else if (fieldName.equalsIgnoreCase("uomname"))
        return uomname;
    else if (fieldName.equalsIgnoreCase("qtyorderline"))
        return qtyorderline;
    else if (fieldName.equalsIgnoreCase("pricelist"))
        return pricelist;
    else if (fieldName.equalsIgnoreCase("unitprice"))
        return unitprice;
    else if (fieldName.equalsIgnoreCase("linetax"))
        return linetax;
    else if (fieldName.equalsIgnoreCase("totallineprice"))
        return totallineprice;
    //
    else if (fieldName.equalsIgnoreCase("orderreviewid"))
        return orderreviewid;
    else if (fieldName.equalsIgnoreCase("orderreviewstate"))
        return orderreviewstate;
    else if (fieldName.equalsIgnoreCase("orderreviewdocnum"))
        return orderreviewdocnum;
    //-------
    else if (fieldName.equalsIgnoreCase("partnername"))
        return partnername;
    else if (fieldName.equalsIgnoreCase("documentno"))
        return documentno;
    else if (fieldName.equalsIgnoreCase("qtyordered"))
        return qtyordered;
    else if (fieldName.equalsIgnoreCase("qtyreceived"))
        return qtyreceived;
    else if (fieldName.equalsIgnoreCase("qtydifference"))
        return qtydifference;
    else if (fieldName.equalsIgnoreCase("cierramanual"))
        return cierramanual;
    else if (fieldName.equalsIgnoreCase("datepromised"))
        return datepromised;
    else if (fieldName.equalsIgnoreCase("ordergrandtotal"))
        return ordergrandtotal;
    
    //-----------------
    else if (fieldName.equalsIgnoreCase("orderimportlineid"))
        return orderimportlineid;
    else if (fieldName.equalsIgnoreCase("productkey"))
        return productkey;
    else if (fieldName.equalsIgnoreCase("internalcode"))
        return internalcode;
    else if (fieldName.equalsIgnoreCase("partidaarancel"))
        return partidaarancel;
    else if (fieldName.equalsIgnoreCase("productname"))
        return productname;
    else if (fieldName.equalsIgnoreCase("uomname"))
        return uomname;
    else if (fieldName.equalsIgnoreCase("productqty"))
        return productqty;
    else if (fieldName.equalsIgnoreCase("productreceived"))
        return productreceived;
    else if (fieldName.equalsIgnoreCase("productdiff"))
        return productdiff;
    else if (fieldName.equalsIgnoreCase("productprice")) 
        return productprice;
    else if (fieldName.equalsIgnoreCase("requisitiondocno")) 
        return requisitiondocno;
    //--
    
    else if (fieldName.equalsIgnoreCase("partiallineid"))
        return partiallineid;
    else if (fieldName.equalsIgnoreCase("partialid"))
        return partialid;
    else if (fieldName.equalsIgnoreCase("partialdocno"))
        return partialdocno;
    else if (fieldName.equalsIgnoreCase("partialproductid"))
        return partialproductid;
    else if (fieldName.equalsIgnoreCase("partialqty"))
        return partialqty;
    else if (fieldName.equalsIgnoreCase("dateshipment"))
        return dateshipment;
    else if (fieldName.equalsIgnoreCase("datearrivalcapital"))
        return datearrivalcapital;
    else if (fieldName.equalsIgnoreCase("dateaproxarrivalware"))
        return dateaproxarrivalware;
    else if (fieldName.equalsIgnoreCase("datearrivalwarehouse"))
        return datearrivalwarehouse;
    else if (fieldName.equalsIgnoreCase("importname"))
        return importname;
    else if (fieldName.equalsIgnoreCase("adv"))
        return adv;
    else if (fieldName.equalsIgnoreCase("tlcadv"))
        return tlcadv;
    else if (fieldName.equalsIgnoreCase("invoicenro"))
        return invoicenro;
    else if (fieldName.equalsIgnoreCase("datedespprv"))
        return datedespprv;
    else if (fieldName.equalsIgnoreCase("datedespacho"))
        return datedespacho;
    else if (fieldName.equalsIgnoreCase("datetoforwarder"))
        return datetoforwarder;
    else if (fieldName.equalsIgnoreCase("dateinvoice"))
        return dateinvoice;
    else if (fieldName.equalsIgnoreCase("datedue"))
        return datedue;
    else if (fieldName.equalsIgnoreCase("invoicegrandtotal"))
        return invoicegrandtotal;
    else if (fieldName.equalsIgnoreCase("invoicetotalpaid"))
        return invoicetotalpaid;
    
    ///
    
    
    
    else if (fieldName.equals("rownum"))
      return rownum;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }
  
  public static String selectMproduct(ConnectionProvider connectionProvider,
			String mProductId) throws ServletException {
		String strSql = "";
		strSql = strSql + "      SELECT M_PRODUCT.VALUE || ' - ' ||M_PRODUCT.NAME as name"
				+ "      FROM M_PRODUCT"
				+ "      WHERE M_PRODUCT.M_PRODUCT_ID = ?";

		ResultSet result;
		String strReturn = "";
		PreparedStatement st = null;

		int iParameter = 0;
		try {
			st = connectionProvider.getPreparedStatement(strSql);
			QueryTimeOutUtil.getInstance().setQueryTimeOut(st,
					SessionInfo.getQueryProfile());
			iParameter++;
			UtilSql.setValue(st, iParameter, 12, null, mProductId);

			result = st.executeQuery();
			if (result.next()) {
				strReturn = UtilSql.getValue(result, "name");
			}
			result.close();
		} catch (SQLException e) {
			log4j.error("SQL error in query: " + strSql + "Exception:" + e);
			throw new ServletException("@CODE="
					+ Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
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
  
  public static trackingImportOrderData[] selectOrdertoReviewPending(ConnectionProvider connectionProvider, 
		  String adOrgId,String adClientId ,String dateFrom, String dateTo ,String BpartnerId, String ProductId, String numberOrder) throws ServletException {
	    return selectOrdertoReviewPending(connectionProvider, adOrgId, adClientId,dateFrom,dateTo ,BpartnerId,ProductId,numberOrder, 0, 0 );
	  }

  public static trackingImportOrderData[] selectOrdertoReviewPending(ConnectionProvider connectionProvider, String adOrgId, String adClientId,String DateFrom, String dateTo ,String BpartnerId, String ProductId, String numberOrder, int firstRegister, int numberRegisters)
	      throws ServletException {

	    String strSql = "";
	    strSql = strSql 
	    + 
	    " SELECT distinct (oi.sim_orderimport_id) as ORDERIMPORTID, "
	    + "      oi.ad_client_id as CLIENTID, "
	    + "      oi.ad_org_id as ORGID,"
	    + "      org.name as ORGNAME, "
	    + "      oi.dateordered as DATEORDERED, "
	    + "      oi.datepromised as DATEPROMISED, "
	    + "      oi.c_bpartner_id AS PARTNERID, "
	    + "      cbp.name as PARTNERNAME, "
	    + "      cur.cursymbol AS CURSYMBOL,"
	    + "      oi.documentno AS DOCUMENTNO,"
	    + "      COALESCE(ROUND(oi.qty_ordered,2),0) AS QTYORDERED, "
	    + "      COALESCE(ROUND(oi.qty_received,2),0) AS QTYRECEIVED, "
	    + "      round(oi.grandtotal_aplic_discount,2) as GRANDTOTAL,"
	    + "      CASE WHEN COALESCE(oi.qty_difference,0) < 0 THEN 0 ELSE ROUND(oi.qty_difference,2) END AS QTYDIFFERENCE, "
	    + "      CASE WHEN oi.docstatus = 'CL' THEN COALESCE(oi.qty_difference,0) ELSE 0 END AS CIERRAMANUAL "
	    + " FROM sim_orderimport oi "
	    + "      INNER JOIN AD_ORG org on oi.ad_org_id = org.ad_org_id "
	    + "      INNER JOIN sim_orderimportline oil ON oi.sim_orderimport_id = oil.sim_orderimport_id "
	    + "      INNER JOIN c_bpartner cbp on oi.c_bpartner_id = cbp.c_bpartner_id"
	    + "      INNER JOIN C_currency cur on oi.c_currency_id = cur.c_currency_id "
	    + " WHERE oi.docstatus IN ('CO','CL')"
	    + "       AND oi.ad_client_id = ? "
	    + "       AND AD_ISORGINCLUDED(oi.ad_org_id, ?, ?) > -1 " 
	    + "   AND oi.dateordered between ? AND ?";
	    strSql = strSql + ((BpartnerId==null || BpartnerId.equals(""))?"":"  AND oi.c_bpartner_id = ? ");
	    strSql = strSql + ((ProductId==null || ProductId.equals(""))?"":"  AND oil.m_product_id = ? ");
	    strSql = strSql + ((numberOrder==null || numberOrder.equals(""))?"":"  AND oi.documentno = ? ");
	    strSql = strSql + " GROUP BY oi.sim_orderimport_id,oi.ad_client_id,oi.ad_org_id,org.name,oi.dateordered,     "
	    		        + "  oi.datepromised, oi.c_bpartner_id, cbp.name, cur.cursymbol, oi.documentno,oi.qty_ordered ,oi.qty_received,"
	    		        + "   oi.grandtotal_aplic_discount,  oi.qty_difference , oi.docstatus       ";
	    strSql = strSql + " HAVING (COALESCE(ROUND(oi.qty_ordered,2),0) - COALESCE(ROUND(oi.qty_received,2),0)) > 0   ";
	    strSql = strSql + "  ORDER BY DOCUMENTNO ";
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
          UtilSql.setValue(st, iParameter, 12, null, DateFrom);
          iParameter++;
          UtilSql.setValue(st, iParameter, 12, null, dateTo);
          if (BpartnerId != null && !(BpartnerId.equals(""))) {
              iParameter++; UtilSql.setValue(st, iParameter, 12, null, BpartnerId);
          }
          if (ProductId != null && !(ProductId.equals(""))) {
              iParameter++; UtilSql.setValue(st, iParameter, 12, null, ProductId);
          }
          if (numberOrder != null && !(numberOrder.equals(""))) {
              iParameter++; UtilSql.setValue(st, iParameter, 12, null, numberOrder);
          }
          
          
          //System.out.println(st);
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
            trackingImportOrderData objecttrackingImportOrderData = new trackingImportOrderData();
            objecttrackingImportOrderData.orderid = UtilSql.getValue(result, "ORDERIMPORTID");
            objecttrackingImportOrderData.clientid = UtilSql.getValue(result, "CLIENTID");
            objecttrackingImportOrderData.orgid = UtilSql.getValue(result, "ORGID");
            objecttrackingImportOrderData.orgname = UtilSql.getValue(result, "ORGNAME");
            objecttrackingImportOrderData.orderdate = UtilSql.getDateValue(result, "DATEORDERED");
            objecttrackingImportOrderData.datepromised = UtilSql.getDateValue(result, "DATEPROMISED");
            objecttrackingImportOrderData.partnername = UtilSql.getValue(result, "PARTNERNAME");
            objecttrackingImportOrderData.scurrency = UtilSql.getValue(result, "CURSYMBOL");
            objecttrackingImportOrderData.ordergrandtotal = UtilSql.getValue(result, "GRANDTOTAL");
            objecttrackingImportOrderData.documentno = UtilSql.getValue(result, "DOCUMENTNO");//
            
            objecttrackingImportOrderData.qtyordered = UtilSql.getValue(result, "QTYORDERED");//
            objecttrackingImportOrderData.qtyreceived = UtilSql.getValue(result, "QTYRECEIVED");//
            objecttrackingImportOrderData.qtydifference = UtilSql.getValue(result, "QTYDIFFERENCE");//
            objecttrackingImportOrderData.cierramanual = UtilSql.getValue(result, "CIERRAMANUAL");//
            
            
            
            
            
            
            objecttrackingImportOrderData.rownum = Long.toString(countRecord);
            objecttrackingImportOrderData.InitRecordNumber = Integer.toString(firstRegister);

            vector.addElement(objecttrackingImportOrderData);
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
	    trackingImportOrderData objecttrackingImportOrderData[] = new trackingImportOrderData[vector
	        .size()];
	    vector.copyInto(objecttrackingImportOrderData);
	    return (objecttrackingImportOrderData);
	}
  
  
  public static trackingImportOrderData[] selectOrdertoReview(ConnectionProvider connectionProvider, 
		  String adOrgId,String adClientId ,String dateFrom, String dateTo ,String BpartnerId, String ProductId, String numberOrder) throws ServletException {
	    return selectOrdertoReview(connectionProvider, adOrgId, adClientId,dateFrom,dateTo ,BpartnerId,ProductId,numberOrder, 0, 0 );
	  }
  
  public static trackingImportOrderData[] selectOrdertoReview(ConnectionProvider connectionProvider, String adOrgId, String adClientId,String DateFrom, String dateTo ,String BpartnerId, String ProductId, String numberOrder, int firstRegister, int numberRegisters)
	      throws ServletException {

	    String strSql = "";
	    strSql = strSql 
	    + 
	    " SELECT distinct (oi.sim_orderimport_id) as ORDERIMPORTID, "
	    + "      oi.ad_client_id as CLIENTID, "
	    + "      oi.ad_org_id as ORGID,"
	    + "      org.name as ORGNAME, "
	    + "      oi.dateordered as DATEORDERED, "
	    + "      oi.datepromised as DATEPROMISED, "
	    + "      oi.c_bpartner_id AS PARTNERID, "
	    + "      cbp.name as PARTNERNAME, "
	    + "      cur.cursymbol AS CURSYMBOL,"
	    + "      oi.documentno AS DOCUMENTNO,"
	    + "      COALESCE(ROUND(oi.qty_ordered,2),0) AS QTYORDERED, "
	    + "      COALESCE(ROUND(oi.qty_received,2),0) AS QTYRECEIVED, "
	    + "      COALESCE(round(oi.grandtotal_aplic_discount,2),0) as GRANDTOTAL,"
	    + "      CASE WHEN COALESCE(oi.qty_difference,0) < 0 THEN 0 ELSE ROUND(oi.qty_difference,2) END AS QTYDIFFERENCE, "
	    + "      CASE WHEN oi.docstatus = 'CL' THEN COALESCE(oi.qty_difference,0) ELSE 0 END AS CIERRAMANUAL "
	    + " FROM sim_orderimport oi "
	    + "      INNER JOIN AD_ORG org on oi.ad_org_id = org.ad_org_id "
	    + "      INNER JOIN sim_orderimportline oil ON oi.sim_orderimport_id = oil.sim_orderimport_id "
	    + "      INNER JOIN c_bpartner cbp on oi.c_bpartner_id = cbp.c_bpartner_id"
	    + "      INNER JOIN C_currency cur on oi.c_currency_id = cur.c_currency_id "
	    + " WHERE oi.docstatus IN ('CO','CL')"
	    + "       AND oi.ad_client_id = ? "
	    + "       AND AD_ISORGINCLUDED(oi.ad_org_id, ?, ?) > -1 " 
	    + "   AND oi.dateordered between ? AND ?";
	    strSql = strSql + ((BpartnerId==null || BpartnerId.equals(""))?"":"  AND oi.c_bpartner_id = ? ");
	    strSql = strSql + ((ProductId==null || ProductId.equals(""))?"":"  AND oil.m_product_id = ? ");
	    strSql = strSql + ((numberOrder==null || numberOrder.equals(""))?"":"  AND oi.documentno = ? ");
	    strSql = strSql + "ORDER BY DOCUMENTNO ";
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
          UtilSql.setValue(st, iParameter, 12, null, DateFrom);
          iParameter++;
          UtilSql.setValue(st, iParameter, 12, null, dateTo);
          if (BpartnerId != null && !(BpartnerId.equals(""))) {
              iParameter++; UtilSql.setValue(st, iParameter, 12, null, BpartnerId);
          }
          if (ProductId != null && !(ProductId.equals(""))) {
              iParameter++; UtilSql.setValue(st, iParameter, 12, null, ProductId);
          }
          if (numberOrder != null && !(numberOrder.equals(""))) {
              iParameter++; UtilSql.setValue(st, iParameter, 12, null, numberOrder);
          }
          
          
          //System.out.println(st);
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
            trackingImportOrderData objecttrackingImportOrderData = new trackingImportOrderData();
            objecttrackingImportOrderData.orderid = UtilSql.getValue(result, "ORDERIMPORTID");
            objecttrackingImportOrderData.clientid = UtilSql.getValue(result, "CLIENTID");
            objecttrackingImportOrderData.orgid = UtilSql.getValue(result, "ORGID");
            objecttrackingImportOrderData.orgname = UtilSql.getValue(result, "ORGNAME");
            objecttrackingImportOrderData.orderdate = UtilSql.getDateValue(result, "DATEORDERED");
            objecttrackingImportOrderData.datepromised = UtilSql.getDateValue(result, "DATEPROMISED");
            objecttrackingImportOrderData.partnername = UtilSql.getValue(result, "PARTNERNAME");
            objecttrackingImportOrderData.scurrency = UtilSql.getValue(result, "CURSYMBOL");
            objecttrackingImportOrderData.ordergrandtotal = UtilSql.getValue(result, "GRANDTOTAL");
            objecttrackingImportOrderData.documentno = UtilSql.getValue(result, "DOCUMENTNO");//
            
            objecttrackingImportOrderData.qtyordered = UtilSql.getValue(result, "QTYORDERED");//
            objecttrackingImportOrderData.qtyreceived = UtilSql.getValue(result, "QTYRECEIVED");//
            objecttrackingImportOrderData.qtydifference = UtilSql.getValue(result, "QTYDIFFERENCE");//
            objecttrackingImportOrderData.cierramanual = UtilSql.getValue(result, "CIERRAMANUAL");//
            
            
            
            
            
            
            objecttrackingImportOrderData.rownum = Long.toString(countRecord);
            objecttrackingImportOrderData.InitRecordNumber = Integer.toString(firstRegister);

            vector.addElement(objecttrackingImportOrderData);
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
	    trackingImportOrderData objecttrackingImportOrderData[] = new trackingImportOrderData[vector
	        .size()];
	    vector.copyInto(objecttrackingImportOrderData);
	    return (objecttrackingImportOrderData);
	  }
  
  public static trackingImportOrderData[] selectOrderImportLine(ConnectionProvider connectionProvider, 
		  String OrderImportId ,String ProductId ) throws ServletException {
	    return selectOrderImportLine(connectionProvider, OrderImportId ,ProductId, 0, 0 );
	  }
  
  public static trackingImportOrderData[] selectOrderImportLine(ConnectionProvider connectionProvider, String OrderImportId ,String ProductId,  int firstRegister, int numberRegisters)
	      throws ServletException {

	    String strSql = "";
	    strSql = strSql 
	    + " SELECT oil.sim_orderimportline_id as ORDERIMPORTLINEID,   "
	    + "      p.value AS PRODUCTKEY, "
	    + "      p.em_scr_internalcode AS INTERNALCODE,"
	    + "      oil.partida_arancelaria AS PARTIDAARANCEL, "
	    + "      p.name AS PRODUCTNAME, "
	    + "      uomtrl.name AS UOMNAME, "
	    + "      mr.documentno as REQUISITIONDOCNO, "
	    + "      ROUND(oil.qtyordered,2) AS PRODUCTQTY, "
	    + "      ROUND(oil.received,2) AS PRODUCTRECEIVED, "
	    + "      CASE WHEN ROUND(oil.qty_difference,2) < 0 THEN 0 ELSE ROUND(oil.qty_difference,2) END AS PRODUCTDIFF, "
	    + "      oil.priceactual AS PRODUCTPRICE "
	    + " FROM sim_orderimportline oil "
	    + "      INNER JOIN m_product p ON oil.m_product_id = p.m_product_id "
	    + "      INNER JOIN C_UOM uom ON oil.c_uom_id = uom.c_uom_id "
	    + "      INNER JOIN c_uom_trl uomtrl ON uom.c_uom_id = uomtrl.c_uom_id "
	    + "      LEFT JOIN m_requisitionorder mro ON oil.sim_orderimportline_id = mro.em_sim_orderimportline_id "
	    + "      LEFT JOIN m_requisitionline mrl on mro.m_requisitionline_id = mrl.m_requisitionline_id "
	    + "      LEFT JOIN m_requisition mr on mrl.m_requisition_id = mr.m_requisition_id "
	    + " WHERE oil.sim_orderimport_id = ? "
	    + "   AND uomtrl.ad_language='es_PE' ";
	    strSql = strSql + ((ProductId==null || ProductId.equals(""))?"":"  AND oil.m_product_id = ? ");
	    

	    ResultSet result;
        Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
        PreparedStatement st = null;
       
        int iParameter = 0;
        try {
          st = connectionProvider.getPreparedStatement(strSql);
          QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
          iParameter++;
          UtilSql.setValue(st, iParameter, 12, null, OrderImportId);
          if (ProductId != null && !(ProductId.equals(""))) {
              iParameter++; UtilSql.setValue(st, iParameter, 12, null, ProductId);
          }
          
          //System.out.println(st);
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
            trackingImportOrderData objecttrackingImportOrderData = new trackingImportOrderData();
            objecttrackingImportOrderData.orderimportlineid = UtilSql.getValue(result, "ORDERIMPORTLINEID");
            objecttrackingImportOrderData.productkey = UtilSql.getValue(result, "PRODUCTKEY");
            objecttrackingImportOrderData.internalcode = UtilSql.getValue(result, "INTERNALCODE");
            objecttrackingImportOrderData.partidaarancel = UtilSql.getValue(result, "PARTIDAARANCEL");
            objecttrackingImportOrderData.requisitiondocno = UtilSql.getValue(result, "REQUISITIONDOCNO");
            objecttrackingImportOrderData.productname = UtilSql.getValue(result, "PRODUCTNAME");
            objecttrackingImportOrderData.uomname = UtilSql.getValue(result, "UOMNAME");
            objecttrackingImportOrderData.productqty = UtilSql.getValue(result, "PRODUCTQTY");
            objecttrackingImportOrderData.productreceived = UtilSql.getValue(result, "PRODUCTRECEIVED");
            objecttrackingImportOrderData.productdiff = UtilSql.getValue(result, "PRODUCTDIFF");
            objecttrackingImportOrderData.productprice = UtilSql.getValue(result, "PRODUCTPRICE");
            
            objecttrackingImportOrderData.rownum = Long.toString(countRecord);
            objecttrackingImportOrderData.InitRecordNumber = Integer.toString(firstRegister);

            vector.addElement(objecttrackingImportOrderData);
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
	    trackingImportOrderData objecttrackingImportOrderData[] = new trackingImportOrderData[vector
	        .size()];
	    vector.copyInto(objecttrackingImportOrderData);
	    return (objecttrackingImportOrderData);
	  }
  

  public static trackingImportOrderData[] selectPartialFolioImport(ConnectionProvider connectionProvider, 
		  String simOrderimportLineID) throws ServletException {
	    return selectPartialFolioImport(connectionProvider, simOrderimportLineID , 0, 0 );
	  }

  public static trackingImportOrderData[] selectPartialFolioImport(ConnectionProvider connectionProvider, String simOrderimportLineID , int firstRegister, int numberRegisters)
	      throws ServletException {

	    String strSql = "";
	    strSql = strSql 
	    + " SELECT col.c_orderline_id as partiallineid,   "
	    + "        co.c_order_id as partialid,   "
	    + "        co.documentno as partialdocno, "
	    + "        col.m_product_id as partialproductid, "
	    + "        ROUND(col.qtyordered,2) as partialqty, "
	    
        + "        COALESCE(trunc(TO_DATE(co.em_sim_datedespprv))::VARCHAR,'--') as datedespprv, "
        
        + "        COALESCE(trunc(TO_DATE(co.EM_Sim_Datetoforwarder))::VARCHAR,'--') as datetoforwarder, "
        
        + "        COALESCE(trunc(TO_DATE(fi.datedespacho))::VARCHAR,'--') as datedespacho, "
	    
	    + "        COALESCE(trunc(TO_DATE(fi.Dateshipment))::VARCHAR,'--') as dateshipment, "
	    + "        COALESCE(trunc(TO_DATE(fi.Datearrival))::VARCHAR,'--') as datearrivalcapital, "
	    + "        COALESCE(trunc(TO_DATE(fi.Dateaproxwarehouse))::VARCHAR,'--') as dateaproxarrivalware, "
	    + "        COALESCE(trunc(TO_DATE(fi.Datewarehouse))::VARCHAR,'--') as datearrivalwarehouse, "
	    + "        COALESCE(fi.Description_importation, '--') AS importname, "
	    + "        COALESCE(col.EM_Sim_Advalorem::VARCHAR,'--') AS adv, "
	    + "        COALESCE(col.EM_Sim_Tlc_Disc_Advalorem::VARCHAR,'--') AS tlcadv, "
	    + "        COALESCE(ci.EM_Scr_Physical_Documentno, '--') AS invoicenro, "
	    
	    + "        COALESCE(trunc(TO_DATE(ci.EM_Sco_Newdateinvoiced))::VARCHAR, '--') AS dateinvoice, "
	    + "        COALESCE(trunc(TO_DATE(ci.EM_Sco_Firstduedate))::VARCHAR, '--') AS datedue, "
	    + "        COALESCE(ci.grandTotal, 0) AS invoicegrandtotal, "
	    + "        COALESCE(ci.Totalpaid, 0) AS invoicetotalpaid "
	    
	    + " FROM c_orderline col "
	    + "      INNER JOIN sim_orderimportline oil on col.em_sim_orderimportline_id = oil.sim_orderimportline_id "
	    + "      INNER JOIN c_order co on col.c_order_id = co.c_order_id "
	    + "      LEFT JOIN  sim_folioimport fi on co.em_sim_folioimport_id = fi.sim_folioimport_id "
	    + "      LEFT JOIN c_invoiceline cil on col.c_orderline_id = cil.c_orderline_id "
	    + "      LEFT JOIN c_invoice ci on cil.c_invoice_id = ci.c_invoice_id "
	    + " WHERE col.em_sim_orderimportline_id = ? ";
	   
	    

	    ResultSet result;
        Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
        PreparedStatement st = null;
       
        int iParameter = 0;
        try {
          st = connectionProvider.getPreparedStatement(strSql);
          QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
          iParameter++;
          UtilSql.setValue(st, iParameter, 12, null, simOrderimportLineID);
          
          //System.out.println(st);
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
            trackingImportOrderData objecttrackingImportOrderData = new trackingImportOrderData();
            objecttrackingImportOrderData.partiallineid = UtilSql.getValue(result, "partiallineid");
            objecttrackingImportOrderData.partialid = UtilSql.getValue(result, "partialid");
            objecttrackingImportOrderData.partialdocno = UtilSql.getValue(result, "partialdocno");
            objecttrackingImportOrderData.partialproductid = UtilSql.getValue(result, "partialproductid");
            objecttrackingImportOrderData.partialqty = UtilSql.getValue(result, "partialqty");
            objecttrackingImportOrderData.datedespprv = UtilSql.getValue(result, "datedespprv");
            objecttrackingImportOrderData.datetoforwarder = UtilSql.getValue(result, "datetoforwarder");
            objecttrackingImportOrderData.datedespacho = UtilSql.getValue(result, "datedespacho");
            objecttrackingImportOrderData.dateshipment = UtilSql.getValue(result, "dateshipment");
            objecttrackingImportOrderData.datearrivalcapital = UtilSql.getValue(result, "datearrivalcapital");
            objecttrackingImportOrderData.dateaproxarrivalware = UtilSql.getValue(result, "dateaproxarrivalware");
            objecttrackingImportOrderData.datearrivalwarehouse = UtilSql.getValue(result, "datearrivalwarehouse");
            objecttrackingImportOrderData.importname = UtilSql.getValue(result, "importname");
            objecttrackingImportOrderData.adv = UtilSql.getValue(result, "adv");
            objecttrackingImportOrderData.tlcadv = UtilSql.getValue(result, "tlcadv");
            objecttrackingImportOrderData.invoicenro = UtilSql.getValue(result, "invoicenro");
            objecttrackingImportOrderData.dateinvoice = UtilSql.getValue(result, "dateinvoice");
            objecttrackingImportOrderData.datedue = UtilSql.getValue(result, "datedue");
            objecttrackingImportOrderData.invoicegrandtotal = UtilSql.getValue(result, "invoicegrandtotal");
            objecttrackingImportOrderData.invoicetotalpaid = UtilSql.getValue(result, "invoicetotalpaid");
            
            
            objecttrackingImportOrderData.rownum = Long.toString(countRecord);
            objecttrackingImportOrderData.InitRecordNumber = Integer.toString(firstRegister);

            vector.addElement(objecttrackingImportOrderData);
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
	    trackingImportOrderData objecttrackingImportOrderData[] = new trackingImportOrderData[vector
	        .size()];
	    vector.copyInto(objecttrackingImportOrderData);
	    return (objecttrackingImportOrderData);
 }
  
 
  
  
  
  public static trackingImportOrderData[] set() throws ServletException {
	  trackingImportOrderData objecttrackingImportOrderData[] = new trackingImportOrderData[1];
    objecttrackingImportOrderData[0] = new trackingImportOrderData();
    objecttrackingImportOrderData[0].productid = "";
    objecttrackingImportOrderData[0].searchkey = "";
    objecttrackingImportOrderData[0].internalcode = "";
    objecttrackingImportOrderData[0].name = "";
    objecttrackingImportOrderData[0].prdID = "";
    objecttrackingImportOrderData[0].transactionID = "";
    objecttrackingImportOrderData[0].movementDate = "";
    objecttrackingImportOrderData[0].warehousename = "";
    objecttrackingImportOrderData[0].storagebin = "";
    objecttrackingImportOrderData[0].movementqty = "";
    objecttrackingImportOrderData[0].movementqtynegative = "";
    objecttrackingImportOrderData[0].shipmentid = "";
    objecttrackingImportOrderData[0].inventoryid = "";
    objecttrackingImportOrderData[0].movementid = "";
    objecttrackingImportOrderData[0].shipmentline = "";
    objecttrackingImportOrderData[0].inventoryline = "";
    objecttrackingImportOrderData[0].movementline = "";
    objecttrackingImportOrderData[0].productionline = "";
    objecttrackingImportOrderData[0].productionid = "";
    
    
    objecttrackingImportOrderData[0].orderid = "";
    objecttrackingImportOrderData[0].clientid = "";
    objecttrackingImportOrderData[0].orgname = "";
    objecttrackingImportOrderData[0].orgid = "";
    objecttrackingImportOrderData[0].ordernum = "";
    objecttrackingImportOrderData[0].orderdate = "";
    objecttrackingImportOrderData[0].scurrency = "";
    objecttrackingImportOrderData[0].currencyid = "";
    objecttrackingImportOrderData[0].ordertotallines = "";
    objecttrackingImportOrderData[0].taxamt = "";
    objecttrackingImportOrderData[0].ordertotal = "";
    
    
    
    
    return objecttrackingImportOrderData;
    
  }
  
  
  public static String selectBPartner(ConnectionProvider connectionProvider, String cBpartnerId)    throws ServletException {
	    String strSql = "";
	    strSql = strSql + 
	      "		      SELECT taxid || ' - ' || NAME FROM C_BPARTNER WHERE C_BPARTNER_ID=?";

	    ResultSet result;
	    String strReturn = "";
	    PreparedStatement st = null;

	    int iParameter = 0;
	    try {
	    st = connectionProvider.getPreparedStatement(strSql);
	      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
	      iParameter++; UtilSql.setValue(st, iParameter, 12, null, cBpartnerId);

	      result = st.executeQuery();
	      if(result.next()) {
	        strReturn = UtilSql.getValue(result, "?column?");
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
	    return(strReturn);
	  }

  
 
 
}
