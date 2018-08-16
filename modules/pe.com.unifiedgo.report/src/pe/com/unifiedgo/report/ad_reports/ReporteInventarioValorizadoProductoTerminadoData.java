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

class ReporteInventarioValorizadoProductoTerminadoData implements FieldProvider {
  static Logger log4j = Logger.getLogger(ReporteInventarioValorizadoProductoTerminadoData.class);
  private String InitRecordNumber = "0";
  public String value;
  public String description;
  public String codigo;
  public String descripcion;
  public String uomsymbol;
  public String uomsymbolSunat0;
  public String codetable5;
  public String um;
  public BigDecimal stock;
  public BigDecimal costo;
  public BigDecimal costoTotal;

  public String idperiodo;
  public String periodo;
  public String idorganizacion;
  
  public String almacen;

  public String rownum;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  @Override
  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("value"))
      return value;
    else if (fieldName.equalsIgnoreCase("description"))
      return description;
    else if (fieldName.equalsIgnoreCase("codigo"))
      return codigo;
    else if (fieldName.equalsIgnoreCase("descripcion"))
      return descripcion;
    else if (fieldName.equalsIgnoreCase("uomsymbol"))
        return uomsymbol;  
    else if (fieldName.equalsIgnoreCase("uomsymbolsunat0"))
        return uomsymbolSunat0;
    else if (fieldName.equalsIgnoreCase("codetable5"))
        return codetable5;
    else if (fieldName.equalsIgnoreCase("um"))
      return um;
    else if (fieldName.equalsIgnoreCase("stock"))
      return stock.toString();
    else if (fieldName.equalsIgnoreCase("costo"))
      return costo.toString();
    else if (fieldName.equalsIgnoreCase("costoTotal"))
        return costoTotal.toString();
    else if (fieldName.equalsIgnoreCase("idperiodo"))
      return idperiodo;
    else if (fieldName.equalsIgnoreCase("periodo"))
      return periodo;
    else if (fieldName.equalsIgnoreCase("idorganizacion"))
      return idorganizacion;
    else if (fieldName.equalsIgnoreCase("almacen"))
        return almacen;
    else if (fieldName.equals("rownum"))
      return rownum;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  public static ReporteInventarioValorizadoProductoTerminadoData[] select(String adOrgId,
      String adClientId, String strMWarehouseID, String endDate, String strcBpartnetId,
      String period_id, String adUserId) throws ServletException {
    return select(adOrgId, adClientId, strMWarehouseID, endDate, strcBpartnetId, period_id,
        adUserId, 0, 0);
  }

  public static ReporteInventarioValorizadoProductoTerminadoData[] select(String adOrgId,
      String adClientId, String strMWarehouseID, String endDate, String strcBpartnetId,
      String period_id, String adUserId, int firstRegister, int numberRegisters)
      throws ServletException {

    String strSql = "";
    strSql = ""
        + "     select pg.value,                                                                        "
        + "     pg.description,                                                                         "
        + "     pro.value as codigo,                                                                    "
        + "     pro.name as descripcion,                                                                "

        + "     coalesce((select name from c_uom_trl where c_uom_id = uom.c_uom_id and ad_language = 'es_PE'),uom.name) as um, "
        + "     sum(tra.movementqty) as stock,                                                          "

        + "     coalesce((select cost from m_costing where m_product_id = pro.m_product_id and to_date('"
        + endDate
        + "') between datefrom and dateto  order by updated desc limit 1),0.0) as costo                 "
        + ",cast ( sum  (case when coalesce ( tra.movementqty) <0 then tra.transactioncost *-1 else tra.transactioncost end ) as numeric )   as costo_total "

        + "     from prdc_productgroup pg                                                               "
        + "     join m_product pro on pg.prdc_productgroup_id = pro.em_prdc_productgroup_id                     "
        + "     join c_uom uom on pro.c_uom_id = uom.c_uom_id                                           "

        + "     join m_transaction tra on  pro.m_product_id = tra.m_product_id                          "
        + "     join m_locator loc on tra.m_locator_id = loc.m_locator_id                       "

        + "     left join  m_inoutline miol on  tra.m_inoutline_id = miol.m_inoutline_id               "
        + "     left join  m_inout mio on  miol.m_inout_id = mio.m_inout_id               "

        + "     where coalesce (mio.docstatus,'') !='VO' AND tra.ad_org_id in ("+adOrgId+ ")  "
        + "     and tra.movementdate <= to_date('" + endDate + "')";

    if (period_id.compareToIgnoreCase("") != 0 && period_id != null) {
      strSql = strSql + " and pg.prdc_productgroup_id = ('" + period_id + "') ";
    }

    if (strMWarehouseID.compareToIgnoreCase("") != 0 && strMWarehouseID != null) {
      strSql = strSql + " and loc.m_warehouse_id = ('" + strMWarehouseID + "') ";
    }

    strSql = strSql + " group by 1,2,3,4,5,7 order by pro.value ";

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

        ReporteInventarioValorizadoProductoTerminadoData objectReporteInventarioValorizadoProductoTerminadoData = new ReporteInventarioValorizadoProductoTerminadoData();
        objectReporteInventarioValorizadoProductoTerminadoData.value = (String) obj[0];
        objectReporteInventarioValorizadoProductoTerminadoData.description = (String) obj[1];
        objectReporteInventarioValorizadoProductoTerminadoData.codigo = (String) obj[2];
        objectReporteInventarioValorizadoProductoTerminadoData.descripcion = (String) obj[3];
        objectReporteInventarioValorizadoProductoTerminadoData.um = (String) obj[4];
        objectReporteInventarioValorizadoProductoTerminadoData.stock = ((BigDecimal) obj[5])
            .setScale(3, BigDecimal.ROUND_HALF_UP);
        objectReporteInventarioValorizadoProductoTerminadoData.costo = ((BigDecimal) obj[6])
            .setScale(3, BigDecimal.ROUND_HALF_UP);
        objectReporteInventarioValorizadoProductoTerminadoData.costoTotal = ((BigDecimal) obj[7])
                .setScale(4, BigDecimal.ROUND_HALF_UP);

        objectReporteInventarioValorizadoProductoTerminadoData.rownum = Long.toString(countRecord);
        objectReporteInventarioValorizadoProductoTerminadoData.InitRecordNumber = Integer
            .toString(firstRegister);

        vector.addElement(objectReporteInventarioValorizadoProductoTerminadoData);

      }
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    }

    ReporteInventarioValorizadoProductoTerminadoData objectReporteInventarioValorizadoProductoTerminadoData[] = new ReporteInventarioValorizadoProductoTerminadoData[vector
        .size()];
    vector.copyInto(objectReporteInventarioValorizadoProductoTerminadoData);

    return (objectReporteInventarioValorizadoProductoTerminadoData);
  }
  
  
  
  
  
  
  public static ReporteInventarioValorizadoProductoTerminadoData[] selectWarehouse(String adOrgId,
	      String adClientId, String strMWarehouseID, String endDate, String strcBpartnetId,
	      String period_id, String adUserId, String strWithStock, String isStocked) throws ServletException {
	    return selectWarehouse(adOrgId, adClientId, strMWarehouseID, endDate, strcBpartnetId, period_id,
	        adUserId , strWithStock,isStocked, 0, 0);
	  }

	  public static ReporteInventarioValorizadoProductoTerminadoData[] selectWarehouse(String adOrgId,
	      String adClientId, String strMWarehouseID, String endDate, String strcBpartnetId,
	      String period_id, String adUserId,String strWithStock,String isStocked, int firstRegister, int numberRegisters)
	      throws ServletException {

	    String strSql = "";
	    strSql = " select * from ( "
	        + "     select pg.value,                                                                        "
	        + "     pg.description,                                                                         "
	        + "     pro.value as codigo,                                                                    "
	        + "     pro.name as descripcion,                                                                "

	        + "     coalesce((select name from c_uom_trl where c_uom_id = uom.c_uom_id and ad_language = 'es_PE'),uom.name) as um, "
	        + "     round ( sum(tra.movementqty),2) as stock,                                                          "

	        + "     coalesce((select cost from m_costing where m_product_id = pro.m_product_id and to_date('"
	        + endDate
	        + "') between datefrom and dateto  order by updated desc limit 1),0.0) as costo                 "
	        + ", COALESCE(cast ( sum  (case when coalesce (tra.movementqty,0) <0 then tra.transactioncost *-1 else tra.transactioncost end ) as numeric ),0)   as costo_total "
	        + "     ,mw.value || ' - ' || mw.name     as almacen                  "

	        + "     from prdc_productgroup pg                                                               "
	        + "     join m_product pro on pg.prdc_productgroup_id = pro.em_prdc_productgroup_id                     "
	        + "     join c_uom uom on pro.c_uom_id = uom.c_uom_id                                           "

	        + "     join m_transaction tra on  pro.m_product_id = tra.m_product_id                          "
	        + "     join m_locator loc on tra.m_locator_id = loc.m_locator_id                       "
	        + "     join m_warehouse mw  on loc.m_warehouse_id=mw.m_warehouse_id                       "

	        

	        + "     left join  m_inoutline miol on  tra.m_inoutline_id = miol.m_inoutline_id               "
	        + "     left join  m_inout mio on  miol.m_inout_id = mio.m_inout_id               "

	        + "     where coalesce (mio.docstatus,'') !='VO' AND tra.ad_org_id in ("+adOrgId+ ")  "
	        + "     and tra.movementdate <= to_date('" + endDate + "')";

	    if (period_id != null && period_id.compareToIgnoreCase("") != 0) {
	      strSql = strSql + " and pg.prdc_productgroup_id = ('" + period_id + "') ";
	    }
	    
	    if (strMWarehouseID != null && strMWarehouseID.compareToIgnoreCase("") != 0) {
	      strSql = strSql + " and loc.m_warehouse_id = ('" + strMWarehouseID + "') ";
	    }
	    
	    if (isStocked != null && isStocked.compareToIgnoreCase("Y")==0) {
		      strSql = strSql + " and pro.isstocked = 'Y'  ";
		}
	    
	    strSql = strSql + " group by 1,2,3,4,5,7,9 ";
	    
	    strSql += " ) as t where 1=1 ";
	    
	    if (strWithStock != null && strWithStock.compareToIgnoreCase("Y")==0) {
		      strSql = strSql + " and t.stock != 0  ";
		}
	    		
	    		
	    strSql += " order by t.value,t.codigo ";

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
	        ReporteInventarioValorizadoProductoTerminadoData objectReporteInventarioValorizadoProductoTerminadoData = new ReporteInventarioValorizadoProductoTerminadoData();
	        objectReporteInventarioValorizadoProductoTerminadoData.value = (String) obj[0];
	        objectReporteInventarioValorizadoProductoTerminadoData.description = (String) obj[1];
	        objectReporteInventarioValorizadoProductoTerminadoData.codigo = (String) obj[2];
	        objectReporteInventarioValorizadoProductoTerminadoData.descripcion = (String) obj[3];
	        objectReporteInventarioValorizadoProductoTerminadoData.um = (String) obj[4];
	        objectReporteInventarioValorizadoProductoTerminadoData.stock = ((BigDecimal) obj[5])
	            .setScale(3, BigDecimal.ROUND_HALF_UP);
	        objectReporteInventarioValorizadoProductoTerminadoData.costo = ((BigDecimal) obj[6])
	            .setScale(3, BigDecimal.ROUND_HALF_UP);
	        objectReporteInventarioValorizadoProductoTerminadoData.costoTotal = ((BigDecimal) obj[7])
	                .setScale(4, BigDecimal.ROUND_HALF_UP);
	        
	        objectReporteInventarioValorizadoProductoTerminadoData.almacen =(String) obj[8];


	        objectReporteInventarioValorizadoProductoTerminadoData.rownum = Long.toString(countRecord);
	        objectReporteInventarioValorizadoProductoTerminadoData.InitRecordNumber = Integer
	            .toString(firstRegister);

	        vector.addElement(objectReporteInventarioValorizadoProductoTerminadoData);
	      }
	    } catch (Exception ex) {
	      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
	      throw new ServletException("@CODE=@" + ex.getMessage());
	    }
	    
	    ReporteInventarioValorizadoProductoTerminadoData objectReporteInventarioValorizadoProductoTerminadoData[] = new ReporteInventarioValorizadoProductoTerminadoData[vector
	        .size()];
	    
	    vector.copyInto(objectReporteInventarioValorizadoProductoTerminadoData);

	    return (objectReporteInventarioValorizadoProductoTerminadoData);
	  }
  
  
  
  
  
  
  
  
  
  
  public static ReporteInventarioValorizadoProductoTerminadoData[] selectTodos(String adOrgId,
	      String adClientId, String strMWarehouseID, String endDate, String strcBpartnetId,
	      String period_id, String adUserId,String strWithStock, String isStocked) throws ServletException {
	    return selectTodos(adOrgId, adClientId, strMWarehouseID, endDate, strcBpartnetId, period_id,
	        adUserId,strWithStock,isStocked, 0, 0);
	  }

	  public static ReporteInventarioValorizadoProductoTerminadoData[] selectTodos(String adOrgId,
	      String adClientId, String strMWarehouseID, String endDate, String strcBpartnetId,
	      String period_id, String adUserId,String strWithStock,String isStocked, int firstRegister, int numberRegisters)
	      throws ServletException {

	    String strSql = "";
	    strSql = "select * from ( "
	        + "     select pg.value,                                                                        "
	        + "     pg.description,                                                                         "
	        + "     pro.value as codigo,                                                                    "
	        + "     pro.name as descripcion,                                                                "
	        + "     coalesce((select name from c_uom_trl where c_uom_id = uom.c_uom_id and ad_language = 'es_PE'),uom.name) as um, "
	    
	        
	        + "round( coalesce((SELECT sum( mt.movementqty )from m_transaction mt "
	        +" inner join m_locator ml on ml.m_locator_id=mt.m_locator_id"	  
	        + "     left join  m_inoutline miol on  mt.m_inoutline_id = miol.m_inoutline_id               "
	        + "     left join  m_inout mio on  miol.m_inout_id = mio.m_inout_id               "
	        + " where coalesce (mio.docstatus,'') !='VO' and  mt.m_product_id=pro.m_product_id "
	        + " AND mt.movementdate <= to_date('"+ endDate+"') and mt.ad_org_id in ("+adOrgId+") ";
	        	    if (strMWarehouseID.compareToIgnoreCase("") != 0 && strMWarehouseID != null) {
	        	    	strSql=strSql+" and ml.m_warehouse_id='"+strMWarehouseID+"'";
	        	    }
	        	    strSql=strSql+ " ),0.0) ,2 ) as stock, "

	        + "     coalesce((select cost from m_costing where m_product_id = pro.m_product_id and to_date('"
	        + endDate
	        + "') between datefrom and dateto  order by updated desc limit 1),0.0) as costo,                 "
	        
	        	        + " coalesce((SELECT cast ( sum  (case when coalesce ( mt.movementqty) <0 then mt.transactioncost *-1 else mt.transactioncost end ) as numeric ) "
	        	        + " from m_transaction mt "
	        +" inner join m_locator ml on ml.m_locator_id=mt.m_locator_id"	 
	        + "     left join  m_inoutline miol on  mt.m_inoutline_id = miol.m_inoutline_id               "
	        + "     left join  m_inout mio on  miol.m_inout_id = mio.m_inout_id               "
	        + " where coalesce (mio.docstatus,'') !='VO' and mt.m_product_id=pro.m_product_id "
	        + " AND mt.movementdate <= to_date('"+ endDate+"') and mt.ad_org_id in ("+adOrgId+") ";
	        	    if (strMWarehouseID.compareToIgnoreCase("") != 0 && strMWarehouseID != null) {
	        	    	strSql=strSql+" and ml.m_warehouse_id='"+strMWarehouseID+"'";
	        	    }
	        	    strSql=strSql+ " ),0.0) as costo_total, "
	        + "     uom.em_ssa_code as uomsymbol, "
	        + "     COALESCE(uom.em_scr_code_sunat0,'99') as uomsymbolSunat0, "
	        + "     cboi.code as codetable5"

	        + "     from prdc_productgroup pg                                                               "
	        + "     join m_product pro on pg.prdc_productgroup_id = pro.em_prdc_productgroup_id             "
	        + "     left join scr_combo_item cboi ON pg.em_sco_codTabla5_cmb_item_id = cboi.scr_combo_item_id "
	        + "     join c_uom uom on pro.c_uom_id = uom.c_uom_id                                           "
	        + "     where pg.ad_org_id in ("+adOrgId+") " ;

	    if (period_id.compareToIgnoreCase("") != 0 && period_id != null) {
	      strSql = strSql + " and pg.prdc_productgroup_id = ('" + period_id + "') ";
	    }
	    
	    if (isStocked != null && isStocked.compareToIgnoreCase("Y")==0) {
		      strSql = strSql + " and pro.isstocked = 'Y'  ";
		}
	    
	    strSql += " ) as t where 1=1 ";
	    
	    if (strWithStock != null && strWithStock.compareToIgnoreCase("Y")==0) {
		      strSql = strSql + " and t.stock != 0  ";
		}

	    strSql = strSql + " order by t.value,t.codigo  ";

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

	        ReporteInventarioValorizadoProductoTerminadoData objectReporteInventarioValorizadoProductoTerminadoData = new ReporteInventarioValorizadoProductoTerminadoData();
	        objectReporteInventarioValorizadoProductoTerminadoData.value = (String) obj[0];
	        objectReporteInventarioValorizadoProductoTerminadoData.description = (String) obj[1];
	        objectReporteInventarioValorizadoProductoTerminadoData.codigo = (String) obj[2];
	        objectReporteInventarioValorizadoProductoTerminadoData.descripcion = (String) obj[3];
	        objectReporteInventarioValorizadoProductoTerminadoData.um = (String) obj[4];
	        objectReporteInventarioValorizadoProductoTerminadoData.stock = ((BigDecimal) obj[5])
	            .setScale(3, BigDecimal.ROUND_HALF_UP);
	        objectReporteInventarioValorizadoProductoTerminadoData.costo = ((BigDecimal) obj[6])
	            .setScale(3, BigDecimal.ROUND_HALF_UP);
	        objectReporteInventarioValorizadoProductoTerminadoData.costoTotal = ((BigDecimal) obj[7])
	                .setScale(4, BigDecimal.ROUND_HALF_UP);
	        objectReporteInventarioValorizadoProductoTerminadoData.uomsymbol = (String) obj[8];
	        
	        objectReporteInventarioValorizadoProductoTerminadoData.uomsymbolSunat0 = (String) obj[9];
	        objectReporteInventarioValorizadoProductoTerminadoData.codetable5 = (String) obj[10];

	        objectReporteInventarioValorizadoProductoTerminadoData.rownum = Long.toString(countRecord);
	        objectReporteInventarioValorizadoProductoTerminadoData.InitRecordNumber = Integer
	            .toString(firstRegister);

	        vector.addElement(objectReporteInventarioValorizadoProductoTerminadoData);

	      }
	    } catch (Exception ex) {
	      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
	      throw new ServletException("@CODE=@" + ex.getMessage());
	    }

	    ReporteInventarioValorizadoProductoTerminadoData objectReporteInventarioValorizadoProductoTerminadoData[] = new ReporteInventarioValorizadoProductoTerminadoData[vector
	        .size()];
	    vector.copyInto(objectReporteInventarioValorizadoProductoTerminadoData);

	    return (objectReporteInventarioValorizadoProductoTerminadoData);
	  }
  

  public static ReporteInventarioValorizadoProductoTerminadoData[] select_periodos(
      ConnectionProvider connectionProvider) throws ServletException {
    return select_periodos(connectionProvider, 0, 0);
  }

  public static ReporteInventarioValorizadoProductoTerminadoData[] select_periodos(
      ConnectionProvider connectionProvider, int firstRegister, int numberRegisters)
      throws ServletException {
    String strSql = "";
    strSql = strSql + "select prdc_productgroup_id as idperiodo,description as periodo, "
        + "ad_org_id as idorganizacion from prdc_productgroup order by description ";

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
        ReporteInventarioValorizadoProductoTerminadoData objectReporteInventarioValorizadoProductoTerminadoData = new ReporteInventarioValorizadoProductoTerminadoData();
        objectReporteInventarioValorizadoProductoTerminadoData.idperiodo = UtilSql.getValue(result,
            "idperiodo");
        objectReporteInventarioValorizadoProductoTerminadoData.periodo = UtilSql.getValue(result,
            "periodo");
        objectReporteInventarioValorizadoProductoTerminadoData.idorganizacion = UtilSql.getValue(
            result, "idorganizacion");
        objectReporteInventarioValorizadoProductoTerminadoData.rownum = Long.toString(countRecord);
        objectReporteInventarioValorizadoProductoTerminadoData.InitRecordNumber = Integer
            .toString(firstRegister);
        vector.addElement(objectReporteInventarioValorizadoProductoTerminadoData);
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
    ReporteInventarioValorizadoProductoTerminadoData objectReporteInventarioValorizadoProductoTerminadoData[] = new ReporteInventarioValorizadoProductoTerminadoData[vector
        .size()];
    vector.copyInto(objectReporteInventarioValorizadoProductoTerminadoData);
    System.out.println("LOL inventario");
    return (objectReporteInventarioValorizadoProductoTerminadoData);
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

  public static ReporteInventarioValorizadoProductoTerminadoData[] set() throws ServletException {
    ReporteInventarioValorizadoProductoTerminadoData objectReporteInventarioValorizadoProductoTerminadoData[] = new ReporteInventarioValorizadoProductoTerminadoData[1];
    objectReporteInventarioValorizadoProductoTerminadoData[0] = new ReporteInventarioValorizadoProductoTerminadoData();
    objectReporteInventarioValorizadoProductoTerminadoData[0].value = "";
    objectReporteInventarioValorizadoProductoTerminadoData[0].description = "";
    objectReporteInventarioValorizadoProductoTerminadoData[0].codigo = "";
    objectReporteInventarioValorizadoProductoTerminadoData[0].descripcion = "";
    objectReporteInventarioValorizadoProductoTerminadoData[0].um = "";
    objectReporteInventarioValorizadoProductoTerminadoData[0].stock = new BigDecimal(0);
    objectReporteInventarioValorizadoProductoTerminadoData[0].costo = new BigDecimal(0);

    return objectReporteInventarioValorizadoProductoTerminadoData;
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

  public static String selectPeriodo(ConnectionProvider connectionProvider, String startDate,
      String adOrgId) throws ServletException {
    String strSql = "";
    strSql = strSql
        + " select name from c_period where to_date(?) between startdate and enddate and ad_org_id = ? ";

    ResultSet result;
    String strReturn = "0";
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, startDate);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adOrgId);

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

  public static String selectPrdcProductgroup(ConnectionProvider connectionProvider,
      String mProductId) throws ServletException {
    String strSql = "";
    strSql = strSql
        + "      SELECT PRDC_PRODUCTGROUP.VALUE || ' - ' || PRDC_PRODUCTGROUP.DESCRIPTION AS NAME"
        + "      FROM PRDC_PRODUCTGROUP" + "      WHERE PRDC_PRODUCTGROUP.PRDC_PRODUCTGROUP_ID = ?";

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

}