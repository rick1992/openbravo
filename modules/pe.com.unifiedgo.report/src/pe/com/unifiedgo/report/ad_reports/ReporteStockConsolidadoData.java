//Sqlc generated V1.O00-1
package pe.com.unifiedgo.report.ad_reports;

import java.math.BigDecimal;
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

class ReporteStockConsolidadoData implements FieldProvider {
	  static Logger log4j = Logger.getLogger(ReporteStockConsolidadoData.class);
	  private String InitRecordNumber = "0";

	  public String codigo;
	  public String detalle;
	  public String um;
	  public String almacen;
	  public String orden;
	  public BigDecimal total;
	  public String productoid;
	  public String fechainicial;
	  public String fechafinal;
	  public String organizacionid;
	  public String organizacion;
	  public String socialname;
	  public String organizacionruc;
	  public BigDecimal costo;

	  public String rownum;

	  public String getInitRecordNumber() {
	    return InitRecordNumber;
	  }

	  @Override
	  public String getField(String fieldName) {
	    if (fieldName.equalsIgnoreCase("codigo"))
	      return codigo;
	    else if (fieldName.equalsIgnoreCase("detalle"))
	      return detalle;
	    else if (fieldName.equalsIgnoreCase("um"))
	      return um;
	    else if (fieldName.equalsIgnoreCase("almacen"))
	      return almacen;
	    else if (fieldName.equalsIgnoreCase("orden"))
	      return orden;
	    else if (fieldName.equalsIgnoreCase("total"))
	      return total.toString();
	    else if (fieldName.equalsIgnoreCase("productoid"))
	      return productoid;
	    else if (fieldName.equalsIgnoreCase("fechainicial"))
	      return fechainicial;
	    else if (fieldName.equalsIgnoreCase("fechafinal"))
	      return fechafinal;
	    else if (fieldName.equalsIgnoreCase("organizacionid"))
	      return organizacionid;
	    else if (fieldName.equalsIgnoreCase("organizacion"))
	      return organizacion;
	    else if (fieldName.equalsIgnoreCase("socialname"))
	      return socialname;
	    else if (fieldName.equalsIgnoreCase("organizacionruc"))
	      return organizacionruc;
	    else if (fieldName.equalsIgnoreCase("costo"))
	      return costo.toString();
	    else if (fieldName.equals("rownum"))
	      return rownum;
	    else {
	      log4j.debug("Field does not exist: " + fieldName);
	      return null;
	    }
	  }

	  public static ReporteStockConsolidadoData[] select(String adOrgId, String adClientId,
	      String mProductId, String valueTypeWarehouse) throws ServletException {
	    return select(adOrgId, adClientId, mProductId, valueTypeWarehouse, 0, 0);
	  }

	  public static ReporteStockConsolidadoData[] select(String adOrgId, String adClientId,
	      String mProductId, String valueTypeWarehouse, int firstRegister, int numberRegisters)
	      throws ServletException {

	    String strSql = "";
	    strSql = "select * from (  select pro.value as codigo, " + "pro.name as detalle, "
	        + "coalesce((select name from c_uom_trl where c_uom_id = uom.c_uom_id "
	        + "and ad_language = 'es_PE' limit 1),uom.name) as um, "
	        + "wh.value||' - '||wh.name as almacen, " + "to_char('1') as orden, "
	        + "coalesce(qtyonhand,0.0) as total " + "from swa_product_warehouse_v v "
	        + "join m_warehouse wh on v.m_warehouse_id = wh.m_warehouse_id "
	        + "join m_product pro on v.m_product_id = pro.m_product_id "
	        + "join c_uom uom on pro.c_uom_id = uom.c_uom_id " + "where v.isactive = 'Y' ";
	    if (!adOrgId.equalsIgnoreCase("0")) {
	      strSql = strSql + " and pro.ad_org_id in ('" + adOrgId + "') ";
	    }

	    if (!mProductId.isEmpty() || !mProductId.equalsIgnoreCase("")) {
	      strSql = strSql + " and pro.value in ('" + mProductId + "') ";
	    }

	    if (!valueTypeWarehouse.isEmpty() || !valueTypeWarehouse.equalsIgnoreCase("")) {
	      strSql = strSql + " and wh.em_swa_warehousetype in ('" + valueTypeWarehouse + "') ";
	    }

	    strSql = strSql + " union " + "select pro.value as codigo, " + "pro.name as detalle, "
	        + "coalesce((select name from c_uom_trl where c_uom_id = uom.c_uom_id "
	        + "and ad_language = 'es_PE'),uom.name) as um, "

	        + "(select (select name from ad_ref_list_trl where ad_ref_list_id = l.ad_ref_list_id "
	        + "and ad_language = 'es_PE' limit 1) "
	        + "from ad_ref_list l where ad_reference_id = (select ad_reference_id from ad_reference "
	        + "where name = 'swa_warehousetype') "
	        + "and l.value = wh.EM_Swa_Warehousetype ) as almacen, " + "to_char('2') as orden, "

	        + "sum(coalesce(qtyonhand,0.0) ) as total " + "from swa_product_warehouse_v v "
	        + "join m_warehouse wh on v.m_warehouse_id = wh.m_warehouse_id "
	        + "join m_product pro on v.m_product_id = pro.m_product_id "
	        + "join c_uom uom on pro.c_uom_id = uom.c_uom_id " + "where v.isactive = 'Y' ";
	    if (!adOrgId.equalsIgnoreCase("0")) {
	      strSql = strSql + " and pro.ad_org_id in ('" + adOrgId + "') ";
	    }

	    if (!mProductId.isEmpty() || !mProductId.equalsIgnoreCase("")) {
	      strSql = strSql + " and pro.value in ('" + mProductId + "') ";
	    }

	    if (!valueTypeWarehouse.isEmpty() || !valueTypeWarehouse.equalsIgnoreCase("")) {
	      strSql = strSql + " and wh.em_swa_warehousetype in ('" + valueTypeWarehouse + "') ";
	    }

	    strSql = strSql + " GROUP BY 1,2,3,4,5) AS t  " + " order by 5,1 ";

	    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);

	    DecimalFormat df = new DecimalFormat("#0.00");
	    df.setRoundingMode(RoundingMode.HALF_UP);

	    DecimalFormat dfInt = new DecimalFormat("0.##");
	    dfInt.setRoundingMode(RoundingMode.HALF_UP);

	    long countRecord = 0;
	    try {
	      Query sqlQuery = OBDal.getInstance().getSession().createSQLQuery(strSql);

	      List<Object> data = sqlQuery.list();
	      for (int k = 0; k < data.size(); k++) {
	        Object[] obj = (Object[]) data.get(k);
	        countRecord++;

	        ReporteStockConsolidadoData objectReporteStockConsolidadoData = new ReporteStockConsolidadoData();
	        objectReporteStockConsolidadoData.codigo = (String) obj[0];
	        objectReporteStockConsolidadoData.detalle = (String) obj[1];
	        objectReporteStockConsolidadoData.um = (String) obj[2];
	        objectReporteStockConsolidadoData.almacen = (String) obj[3];
	        objectReporteStockConsolidadoData.orden = (String) obj[4];
	        objectReporteStockConsolidadoData.total = ((BigDecimal) obj[5]).setScale(3,
	            BigDecimal.ROUND_HALF_UP);

	        objectReporteStockConsolidadoData.rownum = Long.toString(countRecord);
	        objectReporteStockConsolidadoData.InitRecordNumber = Integer.toString(firstRegister);

	        vector.addElement(objectReporteStockConsolidadoData);

	      }
	    } catch (Exception ex) {
	      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
	      throw new ServletException("@CODE=@" + ex.getMessage());
	    }

	    ReporteStockConsolidadoData objectReporteStockConsolidadoData[] = new ReporteStockConsolidadoData[vector
	        .size()];
	    vector.copyInto(objectReporteStockConsolidadoData);

	    return (objectReporteStockConsolidadoData);
	  }

	  public static ReporteStockConsolidadoData[] selectAlterno(String adOrgId, String adClientId,
	      String mProductId, String mWarehouseId, String dateFrom, String dateTo)
	      throws ServletException {
	    return selectAlterno(adOrgId, adClientId, mProductId, mWarehouseId, dateFrom, dateTo, 0, 0);
	  }

	  public static ReporteStockConsolidadoData[] selectAlterno(String adOrgId, String adClientId,
	      String productoValue, String mWarehouseId, String dateFrom, String dateTo, int firstRegister,
	      int numberRegisters) throws ServletException {

	    String strSql = "";
	    strSql = " select mp.value as codigo , mp.name as detalle ,coalesce((select name from c_uom_trl where c_uom_id = uom.c_uom_id "
	        + " and ad_language = 'es_PE' limit 1),uom.name) as um ,mw.value||' - '||mw.name as almacen "
	        + " ,( select coalesce(arlt. name,arl.name )  from ad_ref_list arl "
	        + "   left join ad_ref_list_trl arlt on arl.ad_ref_list_id= arlt.ad_ref_list_id "
	        + " left join ad_reference ar on arl.ad_reference_id=ar.ad_reference_id "
	        + " where ar.name =  'swa_warehousetype' and arl.value = mw.EM_Swa_Warehousetype "
	        + "limit 1) as orden ,  "
	        + " sum (mt.movementqty) as total , "
	        + "coalesce( (SELECT mc.cost FROM m_costing mc  "
	        + " where mc.m_product_id=mp.m_product_id and (now() BETWEEN mc.datefrom and mc.dateto) limit 1),0) as costo "
	        +

	        " from m_transaction mt  "
	        + " inner join m_product mp on mt.m_product_id= mp.m_product_id "
	        + " inner join m_locator ml on mt.m_locator_id = ml.m_locator_id "
	        + " inner join m_warehouse mw on ml.m_warehouse_id = mw.m_warehouse_id "
	        + " inner join c_uom uom on mp.c_uom_id = uom.c_uom_id " + " where  ";

	    if (!adOrgId.equalsIgnoreCase("0")) {
	      strSql = strSql + "  mp.ad_org_id in ('" + adOrgId + "') ";
	    }

	    if (!productoValue.isEmpty() || !productoValue.equalsIgnoreCase("")) {
	      strSql = strSql + " and mp.value like ('" + productoValue + "') ";
	    }

	    if (!dateFrom.isEmpty() || !dateFrom.equalsIgnoreCase("")) {
	      strSql = strSql + " and mt.movementdate >= to_date ('" + dateFrom + "') ";
	    }

	    if (!dateTo.isEmpty() || !dateTo.equalsIgnoreCase("")) {
	      strSql = strSql + " and mt.movementdate <= to_date ('" + dateTo + "') ";
	    }

	    if (!mWarehouseId.isEmpty() || !mWarehouseId.equalsIgnoreCase("")) {
	      strSql = strSql + " and mw.m_warehouse_id in ('" + mWarehouseId + "') ";
	    }

	    strSql = strSql
	        + " GROUP BY mp.value, mp.name ,uom.c_uom_id, uom.name ,mw.value,mw.name,mw.em_swa_warehousetype,mp.m_product_id "
	        + " having  sum (mt.movementqty) <> 0 " +

	        " order by 2,5,4 ";

	    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);

	    DecimalFormat df = new DecimalFormat("#0.00");
	    df.setRoundingMode(RoundingMode.HALF_UP);

	    DecimalFormat dfInt = new DecimalFormat("0.##");
	    dfInt.setRoundingMode(RoundingMode.HALF_UP);

	    long countRecord = 0;
	    try {
	      Query sqlQuery = OBDal.getInstance().getSession().createSQLQuery(strSql);

	      List<Object> data = sqlQuery.list();
	      for (int k = 0; k < data.size(); k++) {
	        Object[] obj = (Object[]) data.get(k);
	        countRecord++;

	        ReporteStockConsolidadoData objectReporteStockConsolidadoData = new ReporteStockConsolidadoData();
	        objectReporteStockConsolidadoData.codigo = (String) obj[0];
	        objectReporteStockConsolidadoData.detalle = (String) obj[1];
	        objectReporteStockConsolidadoData.um = (String) obj[2];
	        objectReporteStockConsolidadoData.almacen = (String) obj[3];
	        objectReporteStockConsolidadoData.orden = (String) obj[4];
	        objectReporteStockConsolidadoData.total = ((BigDecimal) obj[5]).setScale(3,
	            BigDecimal.ROUND_HALF_UP);
	        objectReporteStockConsolidadoData.costo = ((BigDecimal) obj[6]).setScale(10,
	            BigDecimal.ROUND_HALF_UP);

	        objectReporteStockConsolidadoData.rownum = Long.toString(countRecord);
	        objectReporteStockConsolidadoData.InitRecordNumber = Integer.toString(firstRegister);

	        vector.addElement(objectReporteStockConsolidadoData);

	      }
	    } catch (Exception ex) {
	      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
	      throw new ServletException("@CODE=@" + ex.getMessage());
	    }

	    ReporteStockConsolidadoData objectReporteStockConsolidadoData[] = new ReporteStockConsolidadoData[vector
	        .size()];
	    vector.copyInto(objectReporteStockConsolidadoData);

	    return (objectReporteStockConsolidadoData);
	  }

	  public static ReporteStockConsolidadoData[] selectValorizado(String adOrgId, String adClientId,
	      String mProductId, String valueTypeWarehouse) throws ServletException {
	    return selectValorizado(adOrgId, adClientId, mProductId, valueTypeWarehouse, 0, 0);
	  }

	  public static ReporteStockConsolidadoData[] selectValorizado(String adOrgId, String adClientId,
	      String mProductId, String valueTypeWarehouse, int firstRegister, int numberRegisters)
	      throws ServletException {

	    String strSql = "";
	    strSql = "select * from (  select pro.value as codigo, " + "pro.name as detalle, "
	        + "coalesce((select name from c_uom_trl where c_uom_id = uom.c_uom_id "
	        + "and ad_language = 'es_PE' limit 1),uom.name) as um, "
	        + "wh.value||' - '||wh.name as almacen, " + "to_char('1') as orden, "
	        + "coalesce(qtyonhand,0.0) as total " + "from swa_product_warehouse_v v "
	        + "join m_warehouse wh on v.m_warehouse_id = wh.m_warehouse_id "
	        + "join m_product pro on v.m_product_id = pro.m_product_id "
	        + "join c_uom uom on pro.c_uom_id = uom.c_uom_id " + "where v.isactive = 'Y' ";
	    if (!adOrgId.equalsIgnoreCase("0")) {
	      strSql = strSql + " and pro.ad_org_id in ('" + adOrgId + "') ";
	    }

	    if (!mProductId.isEmpty() || !mProductId.equalsIgnoreCase("")) {
	      strSql = strSql + " and pro.value in ('" + mProductId + "') ";
	    }

	    if (!valueTypeWarehouse.isEmpty() || !valueTypeWarehouse.equalsIgnoreCase("")) {
	      strSql = strSql + " and wh.em_swa_warehousetype in ('" + valueTypeWarehouse + "') ";
	    }

	    strSql = strSql + " union " + "select pro.value as codigo, " + "pro.name as detalle, "
	        + "coalesce((select name from c_uom_trl where c_uom_id = uom.c_uom_id "
	        + "and ad_language = 'es_PE'),uom.name) as um, "

	        + "(select (select name from ad_ref_list_trl where ad_ref_list_id = l.ad_ref_list_id "
	        + "and ad_language = 'es_PE' limit 1) "
	        + "from ad_ref_list l where ad_reference_id = (select ad_reference_id from ad_reference "
	        + "where name = 'swa_warehousetype') "
	        + "and l.value = wh.EM_Swa_Warehousetype ) as almacen, " + "to_char('2') as orden, "

	        + "sum(coalesce(qtyonhand,0.0) ) as total " + "from swa_product_warehouse_v v "
	        + "join m_warehouse wh on v.m_warehouse_id = wh.m_warehouse_id "
	        + "join m_product pro on v.m_product_id = pro.m_product_id "
	        + "join c_uom uom on pro.c_uom_id = uom.c_uom_id " + "where v.isactive = 'Y' ";
	    if (!adOrgId.equalsIgnoreCase("0")) {
	      strSql = strSql + " and pro.ad_org_id in ('" + adOrgId + "') ";
	    }

	    if (!mProductId.isEmpty() || !mProductId.equalsIgnoreCase("")) {
	      strSql = strSql + " and pro.value in ('" + mProductId + "') ";
	    }

	    if (!valueTypeWarehouse.isEmpty() || !valueTypeWarehouse.equalsIgnoreCase("")) {
	      strSql = strSql + " and wh.em_swa_warehousetype in ('" + valueTypeWarehouse + "') ";
	    }

	    strSql = strSql + " GROUP BY 1,2,3,4,5 ";

	    strSql = strSql
	        + " union "
	        + "select pro.value as codigo, "
	        + "pro.name as detalle, "
	        + "coalesce((select name from c_uom_trl where c_uom_id = uom.c_uom_id "
	        + "and ad_language = 'es_PE'),uom.name) as um, "

	        + "('Costo Valorizado') as almacen, "
	        + "to_char('3') as orden, "

	        + "coalesce ( sum(coalesce(qtyonhand,0.0) ) * (SELECT mc.cost FROM m_costing mc  "
	        + " where mc.m_product_id=pro.m_product_id and (now() BETWEEN mc.datefrom and mc.dateto) limit 1),0) as total "
	        + "from swa_product_warehouse_v v "
	        + "join m_warehouse wh on v.m_warehouse_id = wh.m_warehouse_id "
	        + "join m_product pro on v.m_product_id = pro.m_product_id "
	        + "join c_uom uom on pro.c_uom_id = uom.c_uom_id " + "where v.isactive = 'Y' ";
	    if (!adOrgId.equalsIgnoreCase("0")) {
	      strSql = strSql + " and pro.ad_org_id in ('" + adOrgId + "') ";
	    }

	    if (!mProductId.isEmpty() || !mProductId.equalsIgnoreCase("")) {
	      strSql = strSql + " and pro.value in ('" + mProductId + "') ";
	    }

	    if (!valueTypeWarehouse.isEmpty() || !valueTypeWarehouse.equalsIgnoreCase("")) {
	      strSql = strSql + " and wh.em_swa_warehousetype in ('" + valueTypeWarehouse + "') ";
	    }

	    strSql = strSql + " GROUP BY 1,2,3,4,5,pro.m_product_id) AS t  " + " order by 5,1 ";

	    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);

	    DecimalFormat df = new DecimalFormat("#0.00");
	    df.setRoundingMode(RoundingMode.HALF_UP);

	    DecimalFormat dfInt = new DecimalFormat("0.##");
	    dfInt.setRoundingMode(RoundingMode.HALF_UP);

	    long countRecord = 0;
	    try {
	      Query sqlQuery = OBDal.getInstance().getSession().createSQLQuery(strSql);

	      List<Object> data = sqlQuery.list();
	      for (int k = 0; k < data.size(); k++) {
	        Object[] obj = (Object[]) data.get(k);
	        countRecord++;

	        ReporteStockConsolidadoData objectReporteStockConsolidadoData = new ReporteStockConsolidadoData();
	        objectReporteStockConsolidadoData.codigo = (String) obj[0];
	        objectReporteStockConsolidadoData.detalle = (String) obj[1];
	        objectReporteStockConsolidadoData.um = (String) obj[2];
	        objectReporteStockConsolidadoData.almacen = (String) obj[3];
	        objectReporteStockConsolidadoData.orden = (String) obj[4];
	        objectReporteStockConsolidadoData.total = ((BigDecimal) obj[5]).setScale(3,
	            BigDecimal.ROUND_HALF_UP);

	        objectReporteStockConsolidadoData.rownum = Long.toString(countRecord);
	        objectReporteStockConsolidadoData.InitRecordNumber = Integer.toString(firstRegister);

	        vector.addElement(objectReporteStockConsolidadoData);

	      }
	    } catch (Exception ex) {
	      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
	      throw new ServletException("@CODE=@" + ex.getMessage());
	    }

	    ReporteStockConsolidadoData objectReporteStockConsolidadoData[] = new ReporteStockConsolidadoData[vector
	        .size()];
	    vector.copyInto(objectReporteStockConsolidadoData);

	    return (objectReporteStockConsolidadoData);
	  }

	  public static ReporteStockConsolidadoData[] select_organizaciones(
	      ConnectionProvider connectionProvider, String adClientId) throws ServletException {
	    return select_organizaciones(connectionProvider, adClientId, 0, 0);
	  }

	  public static ReporteStockConsolidadoData[] select_organizaciones(
	      ConnectionProvider connectionProvider, String adClientId, int firstRegister,
	      int numberRegisters) throws ServletException {
	    String strSql = "";
	    strSql = strSql + "" + "select org.ad_org_id as organizacionid, "
	        + "org.name as organizacion, " + "org.social_name as socialname " + "from ad_org org "
	        + "join ad_orgtype typ using (ad_orgtype_id) "
	        + "where (typ.IsLegalEntity='Y' or typ.IsAcctLegalEntity='Y') "
	        + "and org.ad_client_id in ('" + adClientId + "') " + " " + "order by 2 ";

	    ResultSet result;
	    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
	    PreparedStatement st = null;

	    try {
	      st = connectionProvider.getPreparedStatement(strSql);
	      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());

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
	        ReporteStockConsolidadoData objectReporteStockConsolidadoData = new ReporteStockConsolidadoData();
	        objectReporteStockConsolidadoData.organizacionid = UtilSql.getValue(result,
	            "organizacionid");
	        objectReporteStockConsolidadoData.organizacion = UtilSql.getValue(result, "organizacion");
	        objectReporteStockConsolidadoData.socialname = UtilSql.getValue(result, "socialname");
	        objectReporteStockConsolidadoData.rownum = Long.toString(countRecord);
	        objectReporteStockConsolidadoData.InitRecordNumber = Integer.toString(firstRegister);
	        vector.addElement(objectReporteStockConsolidadoData);
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
	    ReporteStockConsolidadoData objectReporteStockConsolidadoData[] = new ReporteStockConsolidadoData[vector
	        .size()];
	    vector.copyInto(objectReporteStockConsolidadoData);
	    System.out.println("LOL");
	    return (objectReporteStockConsolidadoData);
	  }

	  public static ReporteStockConsolidadoData[] select_tipos_almacenes(
	      ConnectionProvider connectionProvider, String adClientId) throws ServletException {
	    return select_tipos_almacenes(connectionProvider, adClientId, 0, 0);
	  }

	  public static ReporteStockConsolidadoData[] select_tipos_almacenes(
	      ConnectionProvider connectionProvider, String adClientId, int firstRegister,
	      int numberRegisters) throws ServletException {
	    String strSql = "";
	    // strSql = strSql + "" + "select org.ad_org_id as organizacionid, "
	    // + "org.name as organizacion, " + "org.social_name as socialname " + "from ad_org org "
	    // + "join ad_orgtype typ using (ad_orgtype_id) "
	    // + "where (typ.IsLegalEntity='Y' or typ.IsAcctLegalEntity='Y') "
	    // + "and org.ad_client_id in ('" + adClientId + "') " + " " + "order by 2 ";

	    strSql = "select arl.value as codigo, COALESCE(arlt.name,arl.name) as detalle  from ad_reference ar "
	        +

	        "inner join ad_ref_list arl on ar.ad_reference_id=arl.ad_reference_id "
	        + "INNER JOIN ad_ref_list_trl arlt on arl.ad_ref_list_id=arlt.ad_ref_list_id " +

	        "where ar.name='swa_warehousetype' " + "order by 2 ";

	    ResultSet result;
	    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
	    PreparedStatement st = null;

	    try {
	      st = connectionProvider.getPreparedStatement(strSql);
	      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());

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
	        ReporteStockConsolidadoData objectReporteStockConsolidadoData = new ReporteStockConsolidadoData();
	        objectReporteStockConsolidadoData.codigo = UtilSql.getValue(result, "codigo");
	        objectReporteStockConsolidadoData.detalle = UtilSql.getValue(result, "detalle");
	        objectReporteStockConsolidadoData.rownum = Long.toString(countRecord);
	        objectReporteStockConsolidadoData.InitRecordNumber = Integer.toString(firstRegister);
	        vector.addElement(objectReporteStockConsolidadoData);
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
	    ReporteStockConsolidadoData objectReporteStockConsolidadoData[] = new ReporteStockConsolidadoData[vector
	        .size()];
	    vector.copyInto(objectReporteStockConsolidadoData);
	    System.out.println("LOL");
	    return (objectReporteStockConsolidadoData);
	  }

	  public static String getAlmacen(ConnectionProvider connectionProvider, String mWarehouseId) {
	    String strSql = "";

	    strSql = "select value || ' - ' || name as detalle  from m_warehouse " +

	    "where m_warehouse_id ='" + mWarehouseId + "'";

	    ResultSet result;
	    String strReturn = "TODOS";
	    PreparedStatement st = null;

	    int iParameter = 0;
	    try {
	      st = connectionProvider.getPreparedStatement(strSql);
	      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
	      iParameter++;
	      UtilSql.setValue(st, iParameter, 12, null, mWarehouseId);

	      result = st.executeQuery();
	      if (result.next()) {
	        strReturn = UtilSql.getValue(result, "detalle");
	      }
	      result.close();
	    } catch (SQLException e) {
	      strReturn = "Error sql";
	    } catch (Exception ex) {
	      strReturn = "Error LOL";
	    } finally {
	      try {
	        connectionProvider.releasePreparedStatement(st);
	      } catch (Exception ignore) {
	        ignore.printStackTrace();
	      }
	    }
	    return (strReturn);

	  }

	  public static String selectSocialName(ConnectionProvider connectionProvider, String organization) {
	    String strSql = "";
	    strSql = "select social_name from ad_org where ad_org_id = '" + organization + "'";

	    ResultSet result;
	    String strReturn = "0";
	    PreparedStatement st = null;

	    int iParameter = 0;
	    try {
	      st = connectionProvider.getPreparedStatement(strSql);
	      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
	      iParameter++;
	      UtilSql.setValue(st, iParameter, 12, null, organization);

	      result = st.executeQuery();
	      if (result.next()) {
	        strReturn = UtilSql.getValue(result, "social_name");
	      }
	      result.close();
	    } catch (SQLException e) {
	      strReturn = "Error sql";
	    } catch (Exception ex) {
	      strReturn = "Error LOL";
	    } finally {
	      try {
	        connectionProvider.releasePreparedStatement(st);
	      } catch (Exception ignore) {
	        ignore.printStackTrace();
	      }
	    }
	    return (strReturn);

	  }

	  public static ReporteStockConsolidadoData[] set() throws ServletException {
	    ReporteStockConsolidadoData objectReporteStockConsolidadoData[] = new ReporteStockConsolidadoData[1];
	    objectReporteStockConsolidadoData[0] = new ReporteStockConsolidadoData();
	    objectReporteStockConsolidadoData[0].codigo = "";
	    objectReporteStockConsolidadoData[0].detalle = "";
	    objectReporteStockConsolidadoData[0].um = "";
	    objectReporteStockConsolidadoData[0].almacen = "";
	    objectReporteStockConsolidadoData[0].orden = "";
	    objectReporteStockConsolidadoData[0].total = new BigDecimal(0);

	    return objectReporteStockConsolidadoData;
	  }

	  public static String selectOrganization(ConnectionProvider connectionProvider,
	      String organizacion_id) throws ServletException {
	    String strSql = "";
	    strSql = strSql + " select name from ad_org where ad_org_id = ? ";

	    ResultSet result;
	    String strReturn = "0";
	    PreparedStatement st = null;

	    int iParameter = 0;
	    try {
	      st = connectionProvider.getPreparedStatement(strSql);
	      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
	      iParameter++;
	      UtilSql.setValue(st, iParameter, 12, null, organizacion_id);

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

	  public static String selectRucOrganization(ConnectionProvider connectionProvider,
	      String organizacion_id) throws ServletException {
	    String strSql = "";
	    strSql = strSql + " select ad_orginfo.taxid from ad_org "
	        + "join ad_orginfo on ad_org.ad_org_id = ad_orginfo.ad_org_id "
	        + "where ad_org.ad_org_id = ? ";

	    ResultSet result;
	    String strReturn = "0";
	    PreparedStatement st = null;

	    int iParameter = 0;
	    try {
	      st = connectionProvider.getPreparedStatement(strSql);
	      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
	      iParameter++;
	      UtilSql.setValue(st, iParameter, 12, null, organizacion_id);

	      result = st.executeQuery();
	      if (result.next()) {
	        strReturn = UtilSql.getValue(result, "taxid");
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

	  public static String selectBPartner(ConnectionProvider connectionProvider, String bpartner_id)
	      throws ServletException {
	    String strSql = "";
	    strSql = strSql + " select name from c_bpartner where c_bpartner_id = ? ";

	    ResultSet result;
	    String strReturn = "0";
	    PreparedStatement st = null;

	    int iParameter = 0;
	    try {
	      st = connectionProvider.getPreparedStatement(strSql);
	      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
	      iParameter++;
	      UtilSql.setValue(st, iParameter, 12, null, bpartner_id);

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

	  public static String selectPeriodo(ConnectionProvider connectionProvider, String period_id)
	      throws ServletException {
	    String strSql = "";
	    strSql = strSql + " select name from c_period where c_period_id = ? ";

	    ResultSet result;
	    String strReturn = "0";
	    PreparedStatement st = null;

	    int iParameter = 0;
	    try {
	      st = connectionProvider.getPreparedStatement(strSql);
	      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
	      iParameter++;
	      UtilSql.setValue(st, iParameter, 12, null, period_id);

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