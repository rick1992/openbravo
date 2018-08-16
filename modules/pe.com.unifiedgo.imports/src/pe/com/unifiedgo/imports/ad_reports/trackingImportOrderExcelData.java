//Sqlc generated V1.O00-1
package pe.com.unifiedgo.imports.ad_reports;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.data.FieldProvider;
import org.openbravo.data.UtilSql;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.database.SessionInfo;
import org.openbravo.service.db.QueryTimeOutUtil;

class trackingImportOrderExcelData implements FieldProvider {
  static Logger log4j = Logger.getLogger(trackingImportOrderExcelData.class);
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

  // --------------
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
  // ------------
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

  // ------------

  public String orderreviewid;
  public String orderreviewstate;
  public String orderreviewdocnum;

  // ---------------

  public String partnername;
  public String documentno;
  public String qtyordered;
  public String qtyreceived;
  public String qtydifference;
  public String cierramanual;
  public String datepromised;
  public String ordergrandtotal;

  // -------------------

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

  // -------------------
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
    // -------
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

    // -----------------
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
    // --

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

    // /

    else if (fieldName.equals("rownum"))
      return rownum;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  public static String selectMproduct(ConnectionProvider connectionProvider, String mProductId)
      throws ServletException {
    String strSql = "";
    strSql = strSql + "      SELECT M_PRODUCT.NAME" + "      FROM M_PRODUCT"
        + "      WHERE M_PRODUCT.M_PRODUCT_ID = ?";

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

  public static trackingImportOrderExcelData[] selectData(ConnectionProvider connectionProvider,
      String adOrgId, String adClientId, String dateFrom, String dateTo, String BpartnerId,
      String ProductId, String numberOrder) throws ServletException {
    return selectData(connectionProvider, adOrgId, adClientId, dateFrom, dateTo, BpartnerId,
        ProductId, numberOrder, 0, 0);
  }

  public static trackingImportOrderExcelData[] selectData(ConnectionProvider connectionProvider,
      String adOrgId, String adClientId, String DateFrom, String dateTo, String BpartnerId,
      String ProductId, String numberOrder, int firstRegister, int numberRegisters)
      throws ServletException {

    String strSql = "";
    strSql = strSql
        + "select oi.sim_orderimport_id orderid, "
        + "org.name as orgname, "
        + "oi.documentno as documentno, "
        + "oi.datepromised as datepromised, "
        + "bp.name as partnername, "
        + "oi.qty_ordered as qtyordered, "
        + "oi.qty_received as qtyreceived, "
        + "oi.qty_difference as qtydifference, "
        + "cur.cursymbol as scurrency,"
        + "oi.grandtotal as ordergrandtotal, "
        + "case oi.docstatus when 'CL' then coalesce(oi.qty_difference,0) else 0 end as cierramanual, "
        + "lin.* from sim_orderimport oi "
        + "left join c_bpartner bp on oi.c_bpartner_id = bp.c_bpartner_id "
        + "left join (select c.c_currency_id, coalesce(t.cursymbol,c.cursymbol) as cursymbol, "
        + "     coalesce(t.description,c.description) as description from c_currency c "
        + "     left join c_currency_trl t on c.c_currency_id = t.c_currency_id "
        + "     and 'es_PE'::varchar = t.ad_language) cur on oi.c_currency_id = cur.c_currency_id "
        + "left join (select oil.sim_orderimport_id, "
        + "     oil.sim_orderimportline_id as orderimportlineid, "
        + "     oil.m_product_id as productid, "
        + "     pro.value as productkey, "
        + "     pro.em_scr_internalcode as internalcode, "
        + "     oil.partida_arancelaria as partidaarancel, "
        + "     pro.name as productname, "
        + "     coalesce((select name from c_uom_trl where c_uom_id = uom.c_uom_id and ad_language = 'es_PE'),uom.name) as uomname, "
        + "     oil.qtyordered as productqty, "
        + "     oil.received as productreceived, "
        + "     oil.qty_difference as productdiff, "
        + "     oil.priceactual as productprice, "
        + "     mr.documentno as requisitiondocno, "
        + "     par.* "

        + "     from sim_orderimportline oil "
        + "     left join m_product pro on oil.m_product_id = pro.m_product_id "
        + "     left join c_uom uom on oil.c_uom_id = uom.c_uom_id "
        + "     left join m_requisitionorder mro on oil.sim_orderimportline_id = mro.em_sim_orderimportline_id "
        + "     left join m_requisitionline mrl on mro.m_requisitionline_id = mrl.m_requisitionline_id "
        + "     left join m_requisition mr on mrl.m_requisition_id = mr.m_requisition_id "
        + "     left join (select ol.em_sim_orderimportline_id, "
        + "             ord.documentno as partialdocno, "
        + "             fol.description_importation as importname, "
        + "             to_char(ord.em_sim_datedespprv) as datedespprv, "
        + "             to_char(ord.em_sim_datetoforwarder) as datetoforwarder, "
        + "             to_char(fol.datedespacho) as datedespacho, "
        + "             to_char(fol.dateshipment) as dateshipment, "
        + "             to_char(fol.datearrival) as datearrivalcapital, "
        + "             to_char(fol.dateaproxwarehouse) as dateaproxarrivalware, "
        + "             to_char(fol.datewarehouse) as datearrivalwarehouse, "
        + "             ol.qtyordered as partialqty, "
        + "             ol.em_sim_advalorem as adv, "
        + "             ol.em_sim_tlc_disc_advalorem as tlcadv, "
        + "             inv.em_scr_physical_documentno as invoicenro, "
        + "             to_char(inv.em_sco_newdateinvoiced) as dateinvoice, "
        + "             to_char(inv.em_sco_firstduedate) as datedue, "
        + "             inv.grandtotal as invoicegrandtotal, "
        + "             inv.totalpaid as invoicetotalpaid "

        + "             from c_orderline ol"
        + "             left join c_order ord on ol.c_order_id = ord.c_order_id "
        + "             left join sim_folioimport fol on ord.em_sim_folioimport_id = fol.sim_folioimport_id "
        + "             left join c_invoiceline il on ol.c_orderline_id = il.c_orderline_id "
        + "             left join c_invoice inv on il.c_invoice_id = inv.c_invoice_id "
        + "     )par on oil.sim_orderimportline_id = par.em_sim_orderimportline_id "
        + "     order by pro.value "

        + ") lin on oi.sim_orderimport_id = lin.sim_orderimport_id, "
        + "ad_org org "
        + "where org.ad_org_id = oi.ad_org_id and ad_isorgincluded(oi.ad_org_id, ?, ?)<>-1 "
        + "and oi.docstatus in ('CO','CL') " + "and oi.dateordered between ? and ? ";

    strSql = strSql
        + ((BpartnerId == null || BpartnerId.equals("")) ? "" : " and oi.c_bpartner_id = ? ");

    strSql = strSql
        + ((numberOrder == null || numberOrder.equals("")) ? "" : " and oi.documentno = ? ");

    strSql = strSql
        + ((ProductId == null || ProductId.equals("")) ? "" : " and lin.productid = ? ");

    strSql = strSql + "order by oi.documentno, lin.productkey";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, adOrgId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, adClientId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, DateFrom);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, dateTo);

      // if(BpartnerId == null || BpartnerId.equals("")){
      //
      // }
      //
      // iParameter++;
      // UtilSql.setValue(st, iParameter, 12, null, adClientId);
      //
      // iParameter++;
      // UtilSql.setValue(st, iParameter, 12, null, adOrgId);
      //
      // iParameter++;
      // UtilSql.setValue(st, iParameter, 12, null, adClientId);

      if (BpartnerId != null && !(BpartnerId.equals(""))) {
        iParameter++;
        UtilSql.setValue(st, iParameter, 12, null, BpartnerId);
      }

      if (numberOrder != null && !(numberOrder.equals(""))) {
        iParameter++;
        UtilSql.setValue(st, iParameter, 12, null, numberOrder);
      }

      if (ProductId != null && !(ProductId.equals(""))) {
        iParameter++;
        UtilSql.setValue(st, iParameter, 12, null, ProductId);
      }

       System.out.println(" - : "+ st);
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
        trackingImportOrderExcelData objecttrackingImportOrderExcelData = new trackingImportOrderExcelData();
        objecttrackingImportOrderExcelData.orderid = UtilSql.getValue(result, "orderid");
        objecttrackingImportOrderExcelData.orgname = UtilSql.getValue(result, "orgname");
        objecttrackingImportOrderExcelData.documentno = UtilSql.getValue(result, "documentno");
        objecttrackingImportOrderExcelData.datepromised = UtilSql.getDateValue(result,
            "datepromised");
        objecttrackingImportOrderExcelData.partnername = UtilSql.getValue(result, "partnername");
        objecttrackingImportOrderExcelData.qtyordered = UtilSql.getValue(result, "qtyordered");
        objecttrackingImportOrderExcelData.qtyreceived = UtilSql.getValue(result, "qtyreceived");
        objecttrackingImportOrderExcelData.qtydifference = UtilSql
            .getValue(result, "qtydifference");
        objecttrackingImportOrderExcelData.scurrency = UtilSql.getValue(result, "scurrency");
        objecttrackingImportOrderExcelData.ordergrandtotal = UtilSql.getValue(result,
            "ordergrandtotal");
        objecttrackingImportOrderExcelData.cierramanual = UtilSql.getValue(result, "cierramanual");
        objecttrackingImportOrderExcelData.orderimportlineid = UtilSql.getValue(result,
            "orderimportlineid");
        objecttrackingImportOrderExcelData.productkey = UtilSql.getValue(result, "productkey");
        objecttrackingImportOrderExcelData.internalcode = UtilSql.getValue(result, "internalcode");
        objecttrackingImportOrderExcelData.partidaarancel = UtilSql.getValue(result,
            "partidaarancel");
        objecttrackingImportOrderExcelData.productname = UtilSql.getValue(result, "productname");
        objecttrackingImportOrderExcelData.uomname = UtilSql.getValue(result, "uomname");
        objecttrackingImportOrderExcelData.productqty = UtilSql.getValue(result, "productqty");
        objecttrackingImportOrderExcelData.productreceived = UtilSql.getValue(result,
            "productreceived");
        objecttrackingImportOrderExcelData.productdiff = UtilSql.getValue(result, "productdiff");
        objecttrackingImportOrderExcelData.productprice = UtilSql.getValue(result, "productprice");
        objecttrackingImportOrderExcelData.requisitiondocno = UtilSql.getValue(result,
            "requisitiondocno");

        objecttrackingImportOrderExcelData.partialdocno = UtilSql.getValue(result, "partialdocno");
        objecttrackingImportOrderExcelData.importname = UtilSql.getValue(result, "importname");
        objecttrackingImportOrderExcelData.datedespprv = UtilSql.getValue(result, "datedespprv");
        objecttrackingImportOrderExcelData.datetoforwarder = UtilSql.getValue(result,
            "datetoforwarder");
        objecttrackingImportOrderExcelData.datedespacho = UtilSql.getValue(result, "datedespacho");
        objecttrackingImportOrderExcelData.dateshipment = UtilSql.getValue(result, "dateshipment");
        objecttrackingImportOrderExcelData.datearrivalcapital = UtilSql.getValue(result,
            "datearrivalcapital");
        objecttrackingImportOrderExcelData.dateaproxarrivalware = UtilSql.getValue(result,
            "dateaproxarrivalware");
        objecttrackingImportOrderExcelData.datearrivalwarehouse = UtilSql.getValue(result,
            "datearrivalwarehouse");
        objecttrackingImportOrderExcelData.partialqty = UtilSql.getValue(result, "partialqty");
        objecttrackingImportOrderExcelData.adv = UtilSql.getValue(result, "adv");
        objecttrackingImportOrderExcelData.tlcadv = UtilSql.getValue(result, "tlcadv");
        objecttrackingImportOrderExcelData.invoicenro = UtilSql.getValue(result, "invoicenro");
        objecttrackingImportOrderExcelData.dateinvoice = UtilSql.getValue(result, "dateinvoice");
        objecttrackingImportOrderExcelData.datedue = UtilSql.getValue(result, "datedue");
        objecttrackingImportOrderExcelData.invoicegrandtotal = UtilSql.getValue(result,
            "invoicegrandtotal");
        objecttrackingImportOrderExcelData.invoicetotalpaid = UtilSql.getValue(result,
            "invoicetotalpaid");

        objecttrackingImportOrderExcelData.rownum = Long.toString(countRecord);
        objecttrackingImportOrderExcelData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objecttrackingImportOrderExcelData);
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
    trackingImportOrderExcelData objecttrackingImportOrderExcelData[] = new trackingImportOrderExcelData[vector
        .size()];
    vector.copyInto(objecttrackingImportOrderExcelData);
    return (objecttrackingImportOrderExcelData);
  }

  public static trackingImportOrderExcelData[] set() throws ServletException {
    trackingImportOrderExcelData objecttrackingImportOrderExcelData[] = new trackingImportOrderExcelData[1];
    objecttrackingImportOrderExcelData[0] = new trackingImportOrderExcelData();
    objecttrackingImportOrderExcelData[0].productid = "";
    objecttrackingImportOrderExcelData[0].searchkey = "";
    objecttrackingImportOrderExcelData[0].internalcode = "";
    objecttrackingImportOrderExcelData[0].name = "";
    objecttrackingImportOrderExcelData[0].prdID = "";
    objecttrackingImportOrderExcelData[0].transactionID = "";
    objecttrackingImportOrderExcelData[0].movementDate = "";
    objecttrackingImportOrderExcelData[0].warehousename = "";
    objecttrackingImportOrderExcelData[0].storagebin = "";
    objecttrackingImportOrderExcelData[0].movementqty = "";
    objecttrackingImportOrderExcelData[0].movementqtynegative = "";
    objecttrackingImportOrderExcelData[0].shipmentid = "";
    objecttrackingImportOrderExcelData[0].inventoryid = "";
    objecttrackingImportOrderExcelData[0].movementid = "";
    objecttrackingImportOrderExcelData[0].shipmentline = "";
    objecttrackingImportOrderExcelData[0].inventoryline = "";
    objecttrackingImportOrderExcelData[0].movementline = "";
    objecttrackingImportOrderExcelData[0].productionline = "";
    objecttrackingImportOrderExcelData[0].productionid = "";

    objecttrackingImportOrderExcelData[0].orderid = "";
    objecttrackingImportOrderExcelData[0].clientid = "";
    objecttrackingImportOrderExcelData[0].orgname = "";
    objecttrackingImportOrderExcelData[0].orgid = "";
    objecttrackingImportOrderExcelData[0].ordernum = "";
    objecttrackingImportOrderExcelData[0].orderdate = "";
    objecttrackingImportOrderExcelData[0].scurrency = "";
    objecttrackingImportOrderExcelData[0].currencyid = "";
    objecttrackingImportOrderExcelData[0].ordertotallines = "";
    objecttrackingImportOrderExcelData[0].taxamt = "";
    objecttrackingImportOrderExcelData[0].ordertotal = "";

    return objecttrackingImportOrderExcelData;

  }

}
