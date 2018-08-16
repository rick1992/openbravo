//Sqlc generated V1.O00-1
package pe.com.unifiedgo.warehouse.ad_reports;

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



class TrackingNationalPurchaseOrderData implements FieldProvider {
  static Logger log4j = Logger.getLogger(TrackingNationalPurchaseOrderData.class);
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
  public String documentno;
  
  //---------------
  
  
  public String qtyreceived;
  public String qtydifference;
  public String cierramanual;
  public String datepromised;
  public String ordergrandtotal;
  
  //-------------------
  public String orderimportlineid;
  public String requisitiondocno;
  public String internalcode;
  public String partidaarancel;
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
  
  //---------------------------------
  
  public String purchasesalesorderid;
  public String clientid;
  public String orgid;
  public String orgname;
  public String orderdate;
  public String partnername;
  public String ordernum;  
  public String qtyordered;
  public String qtysalesordered;
  public String qtyshipment;
  public String qtyreturned;
  public String qtypending;

  //----------------------------------------
  
  public String purchasesalesorderlineid;
  public String productkey;
  public String productname;
  public String uomname;
  public String qtyorderedline;
  public String qtysalesorderedline;
  public String qtyshipmentline;
  public String qtyreturnedline;
  public String qtypendingline;
  
  //-----------------------------------------

  public String physicaldocnum;
  public String movementshipmentdate;
  public String qtyshipmentocline;
  public String qtyreturnedocline;
  
  
  
  
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
    
    
    
    else if (fieldName.equalsIgnoreCase("qtysalesordered"))
        return qtysalesordered;
    else if (fieldName.equalsIgnoreCase("qtyshipment"))
        return qtyshipment;
    else if (fieldName.equalsIgnoreCase("qtyreturned"))
        return qtyreturned;
    else if (fieldName.equalsIgnoreCase("qtypending"))
        return qtypending;
    
    ///
    else if (fieldName.equalsIgnoreCase("qtyorderedline"))
        return qtyorderedline;
    else if (fieldName.equalsIgnoreCase("qtysalesorderedline"))
        return qtysalesorderedline;
    else if (fieldName.equalsIgnoreCase("qtyshipmentline"))
        return qtyshipmentline;
    else if (fieldName.equalsIgnoreCase("qtyreturnedline"))
        return qtyreturnedline;
    else if (fieldName.equalsIgnoreCase("qtypendingline"))
        return qtypendingline;
    else if (fieldName.equalsIgnoreCase("physicaldocnum"))
        return physicaldocnum;
    else if (fieldName.equalsIgnoreCase("movementshipmentdate"))
        return movementshipmentdate;
    else if (fieldName.equalsIgnoreCase("qtyshipmentocline"))
        return qtyshipmentocline;
    
    else if (fieldName.equalsIgnoreCase("qtyreturnedocline"))
        return qtyreturnedocline;
    
    
    
    
    
    
    
    
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
  
  public static TrackingNationalPurchaseOrderData[] selectPurchaseOrdertoReview(ConnectionProvider connectionProvider, 
		  String adOrgId,String adClientId ,String dateFrom, String dateTo ,String BpartnerId, String ProductId, String numberOrder) throws ServletException {
	    return selectPurchaseOrdertoReview(connectionProvider, adOrgId, adClientId,dateFrom,dateTo ,BpartnerId,ProductId,numberOrder, 0, 0 );
  }
  
  public static TrackingNationalPurchaseOrderData[] selectPurchaseOrdertoReview(ConnectionProvider connectionProvider, String adOrgId, String adClientId,String DateFrom, String dateTo ,String BpartnerId, String ProductId, String numberOrder, int firstRegister, int numberRegisters)
	      throws ServletException {

	    String strSql = "";
	    strSql = strSql 
	    + " SELECT distinct b.ssa_clientpo_id as SALESPURCHASEORDERID, "
	    + "      b.ad_client_id as CLIENTID, "
	    + "      b.ad_org_id as ORGID,"
	    + "      b.name as ORGNAME, "
	    + "      b.dateordered as DATEORDERED, "
	    + "      b.partnername as PARTNERNAME, "
	    + "      b.poreference AS DOCUMENTNO,"
	    + "      COALESCE(sum(b.qtyordered),0) as QTYORDERED, "
	    + "      COALESCE(sum(b.qtysalesordered),0) as QTYSALESORDERED, "
	    + "      COALESCE(sum(b.qtyshipment),0) as QTYSHIPMENT,"
	    + "      COALESCE(sum(b.qtyreturned),0) as QTYRETURNED , "
	    + "      COALESCE(sum(b.qtypending),0) as QTYPENDING "
	    + "FROM ("
	    + " SELECT distinct oc.ssa_clientpo_id, "
	    + "      oc.ad_client_id, "
	    + "      oc.ad_org_id,"
	    + "      pol.m_product_id, "
	    + "      org.name, "
	    + "      oc.dateordered, "
	    + "      cbp.name as partnername, "
	    + "      oc.poreference,"
	    //+ "      COALESCE(sum(pol.qtyordered),0) as QTYORDERED, "
	    //+ "      COALESCE(sum(pol.qtysalesordered),0) as QTYSALESORDERED, "
	    + "      COALESCE(pol.qtyordered,0) as qtyordered, "
	    + "      COALESCE(pol.qtysalesordered,0) as qtysalesordered, "
	    + "      COALESCE(sum(iol.movementqty),0) as qtyshipment,"
	    + "      COALESCE(sum(iol.em_ssa_qtyreturned),0) as qtyreturned , "
	    //+ "      COALESCE(sum(pol.qtyordered) - sum(pol.qtysalesordered),0) as QTYPENDING "
	    + "      COALESCE(pol.qtyordered,0) - COALESCE(pol.qtysalesordered,0) as qtypending "
	    + " FROM ssa_clientpo oc "
	    + "      INNER JOIN AD_ORG org on oc.ad_org_id = org.ad_org_id "
	    + "      INNER JOIN c_bpartner cbp on oc.c_bpartner_id = cbp.c_bpartner_id "
	    + "      inner join ssa_clientpoline pol ON oc.ssa_clientpo_id = pol.ssa_clientpo_id "
	    + "      left join c_orderline col ON col.em_ssa_clientpoline_id = pol.ssa_clientpoline_id "
	    + "      left join m_inoutline iol ON iol.c_orderline_id = col.c_orderline_id "
	    + "      left join m_inout i ON i.m_inout_id = iol.m_inout_id AND i.docstatus='CO'   "
	    + " WHERE oc.docstatus IN ('CO','CL')"
	    + "       AND oc.ad_client_id = ? "
	    + "       AND AD_ISORGINCLUDED(oc.ad_org_id, ?, ?) > -1 " 
	    + "   AND oc.dateordered between ? AND ?";
	    strSql = strSql + ((BpartnerId==null || BpartnerId.equals(""))?"":"  AND oc.c_bpartner_id = ? ");
	    strSql = strSql + ((ProductId==null || ProductId.equals(""))?"":"  AND pol.m_product_id = ? ");
	    strSql = strSql + ((numberOrder==null || numberOrder.equals(""))?"":"  AND lower(oc.poreference) = ? ");
	    strSql = strSql + " GROUP BY oc.ssa_clientpo_id, pol.m_product_id,pol.qtyordered,pol.qtysalesordered, org.name, oc.ad_client_id ,oc.ad_org_id,oc.poreference, oc.dateordered, cbp.name ";
	    strSql = strSql + " ORDER BY dateordered ";
	    strSql = strSql + " ) b";
	    strSql = strSql + " GROUP BY b.ssa_clientpo_id, b.name, b.ad_client_id ,b.ad_org_id,b.poreference, b.dateordered, b.partnername ";
	    strSql = strSql + " ORDER BY b.dateordered ";
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
          
          
          System.out.println(st);
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
            TrackingNationalPurchaseOrderData objectTrackingNationalPurchaseOrderData = new TrackingNationalPurchaseOrderData();
            objectTrackingNationalPurchaseOrderData.purchasesalesorderid = UtilSql.getValue(result, "SALESPURCHASEORDERID");
            objectTrackingNationalPurchaseOrderData.clientid = UtilSql.getValue(result, "CLIENTID");
            objectTrackingNationalPurchaseOrderData.orgname = UtilSql.getValue(result, "ORGNAME");
            objectTrackingNationalPurchaseOrderData.orderdate = UtilSql.getDateValue(result, "DATEORDERED");
            objectTrackingNationalPurchaseOrderData.partnername = UtilSql.getValue(result, "PARTNERNAME");
            objectTrackingNationalPurchaseOrderData.ordernum = UtilSql.getValue(result, "DOCUMENTNO");//
            objectTrackingNationalPurchaseOrderData.qtyordered = UtilSql.getValue(result, "QTYORDERED");//
            
            objectTrackingNationalPurchaseOrderData.qtysalesordered = UtilSql.getValue(result, "QTYSALESORDERED");//
            objectTrackingNationalPurchaseOrderData.qtyshipment = UtilSql.getValue(result, "QTYSHIPMENT");//
            objectTrackingNationalPurchaseOrderData.qtyreturned = UtilSql.getValue(result, "QTYRETURNED");//
            objectTrackingNationalPurchaseOrderData.qtypending = UtilSql.getValue(result, "QTYPENDING");//
            
            objectTrackingNationalPurchaseOrderData.rownum = Long.toString(countRecord);
            objectTrackingNationalPurchaseOrderData.InitRecordNumber = Integer.toString(firstRegister);

            vector.addElement(objectTrackingNationalPurchaseOrderData);
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
	    TrackingNationalPurchaseOrderData objectTrackingNationalPurchaseOrderData[] = new TrackingNationalPurchaseOrderData[vector
	        .size()];
	    vector.copyInto(objectTrackingNationalPurchaseOrderData);
	    return (objectTrackingNationalPurchaseOrderData);
	  }
  

  public static TrackingNationalPurchaseOrderData[] selectOrdertoReview(ConnectionProvider connectionProvider, 
		  String adOrgId,String adClientId ,String dateFrom, String dateTo ,String BpartnerId, String ProductId, String numberOrder) throws ServletException {
	    return selectOrdertoReview(connectionProvider, adOrgId, adClientId,dateFrom,dateTo ,BpartnerId,ProductId,numberOrder, 0, 0 );
	  }
  
  public static TrackingNationalPurchaseOrderData[] selectOrdertoReview(ConnectionProvider connectionProvider, String adOrgId, String adClientId,String DateFrom, String dateTo ,String BpartnerId, String ProductId, String numberOrder, int firstRegister, int numberRegisters)
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
	    + "      oi.qty_ordered AS QTYORDERED, "
	    + "      oi.qty_received AS QTYRECEIVED, "
	    + "      round(oi.grandtotal_aplic_discount,2) as GRANDTOTAL,"
	    + "      oi.qty_difference AS QTYDIFFERENCE, "
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
            TrackingNationalPurchaseOrderData objectTrackingNationalPurchaseOrderData = new TrackingNationalPurchaseOrderData();
            objectTrackingNationalPurchaseOrderData.orderid = UtilSql.getValue(result, "ORDERIMPORTID");
            objectTrackingNationalPurchaseOrderData.clientid = UtilSql.getValue(result, "CLIENTID");
            objectTrackingNationalPurchaseOrderData.orgid = UtilSql.getValue(result, "ORGID");
            objectTrackingNationalPurchaseOrderData.orgname = UtilSql.getValue(result, "ORGNAME");
            objectTrackingNationalPurchaseOrderData.orderdate = UtilSql.getDateValue(result, "DATEORDERED");
            objectTrackingNationalPurchaseOrderData.datepromised = UtilSql.getDateValue(result, "DATEPROMISED");
            objectTrackingNationalPurchaseOrderData.partnername = UtilSql.getValue(result, "PARTNERNAME");
            objectTrackingNationalPurchaseOrderData.scurrency = UtilSql.getValue(result, "CURSYMBOL");
            objectTrackingNationalPurchaseOrderData.ordergrandtotal = UtilSql.getValue(result, "GRANDTOTAL");
            objectTrackingNationalPurchaseOrderData.documentno = UtilSql.getValue(result, "DOCUMENTNO");//
            
            objectTrackingNationalPurchaseOrderData.qtyordered = UtilSql.getValue(result, "QTYORDERED");//
            objectTrackingNationalPurchaseOrderData.qtyreceived = UtilSql.getValue(result, "QTYRECEIVED");//
            objectTrackingNationalPurchaseOrderData.qtydifference = UtilSql.getValue(result, "QTYDIFFERENCE");//
            objectTrackingNationalPurchaseOrderData.cierramanual = UtilSql.getValue(result, "CIERRAMANUAL");//
            
            
            
            
            
            
            objectTrackingNationalPurchaseOrderData.rownum = Long.toString(countRecord);
            objectTrackingNationalPurchaseOrderData.InitRecordNumber = Integer.toString(firstRegister);

            vector.addElement(objectTrackingNationalPurchaseOrderData);
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
	    TrackingNationalPurchaseOrderData objectTrackingNationalPurchaseOrderData[] = new TrackingNationalPurchaseOrderData[vector
	        .size()];
	    vector.copyInto(objectTrackingNationalPurchaseOrderData);
	    return (objectTrackingNationalPurchaseOrderData);
	  }
  
  public static TrackingNationalPurchaseOrderData[] selectOrderClientLine(ConnectionProvider connectionProvider, 
		  String strOrderClientId ,String ProductId ) throws ServletException {
	    return selectOrderClientLine(connectionProvider, strOrderClientId ,ProductId, 0, 0 );
  }
  
  public static TrackingNationalPurchaseOrderData[] selectOrderClientLine(ConnectionProvider connectionProvider, String strOrderClientId ,String ProductId,  int firstRegister, int numberRegisters)
	      throws ServletException {

	    String strSql = "";
	    strSql = strSql 
	    + " SELECT cpl.ssa_clientpoline_id AS SALESPURCHASEORDERLINEID,   "
	    + "      p.value as PRODUCTKEY, "
	    + "      p.name as PRODUCTNAME, "
	    + "      uomtrl.name AS UOMNAME, "
	    //+ "      COALESCE(sum(cpl.qtyordered)) as QTYORDEREDLINE,  "
	    //+ "      COALESCE(sum(cpl.qtysalesordered),0) as QTYSALESORDEREDLINE, "
	    + "      COALESCE(cpl.qtyordered,0) as QTYORDEREDLINE,  "
	    + "      COALESCE(cpl.qtysalesordered,0) as QTYSALESORDEREDLINE, "
	    + "      COALESCE(sum(iol.movementqty),0) as QTYSHIPMENTLINE, "
	    + "      COALESCE(sum(iol.em_ssa_qtyreturned),0) as QTYRETURNEDLINE, "
	    + "      COALESCE(cpl.qtyordered,0) - COALESCE(cpl.qtysalesordered,0) as QTYPENDINGLINE "
	    + " FROM ssa_clientpoline cpl "
	    + "      INNER JOIN m_product p ON cpl.m_product_id =  p.m_product_id "
	    + "      INNER JOIN c_uom uom ON cpl.c_uom_id = uom.c_uom_id "
	    + "      INNER JOIN c_uom_trl uomtrl ON uom.c_uom_id = uomtrl.c_uom_id "
	    + "      LEFT JOIN c_orderline col ON col.em_ssa_clientpoline_id = cpl.ssa_clientpoline_id  "
	    + "      LEFT JOIN m_inoutline iol ON iol.c_orderline_id = col.c_orderline_id  "
	    + " WHERE cpl.ssa_clientpo_id = ? "
	    + "   AND uomtrl.ad_language='es_PE' ";
	    strSql = strSql + ((ProductId==null || ProductId.equals(""))?"":"  AND cpl.m_product_id = ? ");
	    strSql = strSql + "  GROUP BY cpl.ssa_clientpoline_id, cpl.qtyordered,cpl.qtysalesordered, p.value, p.name , uomtrl.name ";

	    ResultSet result;
        Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
        PreparedStatement st = null;
       
        int iParameter = 0;
        try {
          st = connectionProvider.getPreparedStatement(strSql);
          QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
          iParameter++;
          UtilSql.setValue(st, iParameter, 12, null, strOrderClientId);
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
            TrackingNationalPurchaseOrderData objectTrackingNationalPurchaseOrderData = new TrackingNationalPurchaseOrderData();
            objectTrackingNationalPurchaseOrderData.purchasesalesorderlineid = UtilSql.getValue(result, "SALESPURCHASEORDERLINEID");
            objectTrackingNationalPurchaseOrderData.productkey = UtilSql.getValue(result, "PRODUCTKEY");
            objectTrackingNationalPurchaseOrderData.productname = UtilSql.getValue(result, "PRODUCTNAME");
            objectTrackingNationalPurchaseOrderData.uomname = UtilSql.getValue(result, "UOMNAME");
            
            objectTrackingNationalPurchaseOrderData.qtyorderedline = UtilSql.getValue(result, "QTYORDEREDLINE");
            objectTrackingNationalPurchaseOrderData.qtysalesorderedline = UtilSql.getValue(result, "QTYSALESORDEREDLINE");
            objectTrackingNationalPurchaseOrderData.qtyshipmentline = UtilSql.getValue(result, "QTYSHIPMENTLINE");
            objectTrackingNationalPurchaseOrderData.qtyreturnedline = UtilSql.getValue(result, "QTYRETURNEDLINE");
            objectTrackingNationalPurchaseOrderData.qtypendingline = UtilSql.getValue(result, "QTYPENDINGLINE");
            
            objectTrackingNationalPurchaseOrderData.rownum = Long.toString(countRecord);
            objectTrackingNationalPurchaseOrderData.InitRecordNumber = Integer.toString(firstRegister);

            vector.addElement(objectTrackingNationalPurchaseOrderData);
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
	    TrackingNationalPurchaseOrderData objectTrackingNationalPurchaseOrderData[] = new TrackingNationalPurchaseOrderData[vector
	        .size()];
	    vector.copyInto(objectTrackingNationalPurchaseOrderData);
	    return (objectTrackingNationalPurchaseOrderData);
	  }
  
  public static TrackingNationalPurchaseOrderData[] selectShipmentLine(ConnectionProvider connectionProvider, 
		  String strOrderClientLineID) throws ServletException {
	    return selectShipmentLine(connectionProvider, strOrderClientLineID , 0, 0 );
  }
  
  public static TrackingNationalPurchaseOrderData[] selectShipmentLine(ConnectionProvider connectionProvider, String strOrderClientLineID , int firstRegister, int numberRegisters)
	      throws ServletException {

	    String strSql = "";
	    strSql = strSql 
	    + " SELECT  ' GR: ' || i.em_scr_physical_documentno || ' - FV: ' || ci.em_scr_physical_documentno as PHYSICALDOCNUM,   "
	    + "        trunc(i.movementdate) as MOVEMENTSHIPMENTDATE, "
	    + "        COALESCE(sum(iol.movementqty),0) as QTYSHIPMENTLINE,   "
	    + "        COALESCE(sum(iol.em_ssa_qtyreturned),0) as QTYRETURNEDSHIPMENTLINE  "
	    + " FROM ssa_clientpoline cpl "
	    + "      INNER JOIN c_orderline col ON col.em_ssa_clientpoline_id = cpl.ssa_clientpoline_id  "
	    + "      INNER JOIN m_inoutline iol ON iol.c_orderline_id = col.c_orderline_id  "
	    + "      INNER JOIN m_inout i ON i.m_inout_id = iol.m_inout_id "
	    + "      LEFT JOIN  c_invoiceline cil ON cil.c_orderline_id = col.c_orderline_id "
	    + "      LEFT JOIN c_invoice ci ON ci.c_invoice_id = cil.c_invoice_id"
	    + " WHERE cpl.ssa_clientpoline_id = ? " 
	    + "    GROUP BY i.movementdate, i.em_scr_physical_documentno, ci.em_scr_physical_documentno";

	    ResultSet result;
        Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
        PreparedStatement st = null;
       
        int iParameter = 0;
        try {
          st = connectionProvider.getPreparedStatement(strSql);
          QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
          iParameter++; UtilSql.setValue(st, iParameter, 12, null, strOrderClientLineID);
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
            TrackingNationalPurchaseOrderData objectTrackingNationalPurchaseOrderData = new TrackingNationalPurchaseOrderData();
            objectTrackingNationalPurchaseOrderData.physicaldocnum = UtilSql.getValue(result, "PHYSICALDOCNUM");
            objectTrackingNationalPurchaseOrderData.movementshipmentdate = UtilSql.getDateValue(result, "MOVEMENTSHIPMENTDATE");
            objectTrackingNationalPurchaseOrderData.qtyshipmentocline = UtilSql.getValue(result, "QTYSHIPMENTLINE");
            objectTrackingNationalPurchaseOrderData.qtyreturnedocline = UtilSql.getValue(result, "QTYRETURNEDSHIPMENTLINE");
            objectTrackingNationalPurchaseOrderData.rownum = Long.toString(countRecord);
            objectTrackingNationalPurchaseOrderData.InitRecordNumber = Integer.toString(firstRegister);

            vector.addElement(objectTrackingNationalPurchaseOrderData);
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
	    TrackingNationalPurchaseOrderData objectTrackingNationalPurchaseOrderData[] = new TrackingNationalPurchaseOrderData[vector
	        .size()];
	    vector.copyInto(objectTrackingNationalPurchaseOrderData);
	    return (objectTrackingNationalPurchaseOrderData);
 }
  


  
  
  public static TrackingNationalPurchaseOrderData[] set() throws ServletException {
	  TrackingNationalPurchaseOrderData objectTrackingNationalPurchaseOrderData[] = new TrackingNationalPurchaseOrderData[1];
    objectTrackingNationalPurchaseOrderData[0] = new TrackingNationalPurchaseOrderData();
    objectTrackingNationalPurchaseOrderData[0].productid = "";
    objectTrackingNationalPurchaseOrderData[0].searchkey = "";
    objectTrackingNationalPurchaseOrderData[0].internalcode = "";
    objectTrackingNationalPurchaseOrderData[0].name = "";
    objectTrackingNationalPurchaseOrderData[0].prdID = "";
    objectTrackingNationalPurchaseOrderData[0].transactionID = "";
    objectTrackingNationalPurchaseOrderData[0].movementDate = "";
    objectTrackingNationalPurchaseOrderData[0].warehousename = "";
    objectTrackingNationalPurchaseOrderData[0].storagebin = "";
    objectTrackingNationalPurchaseOrderData[0].movementqty = "";
    objectTrackingNationalPurchaseOrderData[0].movementqtynegative = "";
    objectTrackingNationalPurchaseOrderData[0].shipmentid = "";
    objectTrackingNationalPurchaseOrderData[0].inventoryid = "";
    objectTrackingNationalPurchaseOrderData[0].movementid = "";
    objectTrackingNationalPurchaseOrderData[0].shipmentline = "";
    objectTrackingNationalPurchaseOrderData[0].inventoryline = "";
    objectTrackingNationalPurchaseOrderData[0].movementline = "";
    objectTrackingNationalPurchaseOrderData[0].productionline = "";
    objectTrackingNationalPurchaseOrderData[0].productionid = "";
    
    
    objectTrackingNationalPurchaseOrderData[0].orderid = "";
    objectTrackingNationalPurchaseOrderData[0].clientid = "";
    objectTrackingNationalPurchaseOrderData[0].orgname = "";
    objectTrackingNationalPurchaseOrderData[0].orgid = "";
    objectTrackingNationalPurchaseOrderData[0].ordernum = "";
    objectTrackingNationalPurchaseOrderData[0].orderdate = "";
    objectTrackingNationalPurchaseOrderData[0].scurrency = "";
    objectTrackingNationalPurchaseOrderData[0].currencyid = "";
    objectTrackingNationalPurchaseOrderData[0].ordertotallines = "";
    objectTrackingNationalPurchaseOrderData[0].taxamt = "";
    objectTrackingNationalPurchaseOrderData[0].ordertotal = "";
    
    
    
    
    return objectTrackingNationalPurchaseOrderData;
    
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
