//Sqlc generated V1.O00-1
package pe.com.unifiedgo.report.ad_reports;

import java.math.BigDecimal;
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

class ReporteListaPickingData implements FieldProvider {
  static Logger log4j = Logger.getLogger(ReporteListaPickingData.class);
  private String InitRecordNumber = "0";

  public String pickingid;

  public String nropedventa;
  public String nroordtrans;
  public String nroordserv;

  public String nropicking;
  public String numguia;

  public String estado;
  public String feccreacion;
  public String fecimp;
  public String fecfinpicking;
  public String fecdespacho;
  public String pickeroid;
  public String pickero;
  public BigDecimal linea;
  public String productoid;
  public String producto;
  public String um;
  public String ubicacion;
  public BigDecimal cantmov;

  public String orgid;
  public String orgname;

  public String userorgid;
  public String userid;
  public String username;

  public String rownum;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  @Override
  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("pickingid"))
      return pickingid;
    else if (fieldName.equalsIgnoreCase("nropedventa"))
      return nropedventa;
    else if (fieldName.equalsIgnoreCase("nroordtrans"))
      return nroordtrans;
    else if (fieldName.equalsIgnoreCase("nroordserv"))
      return nroordserv;
    else if (fieldName.equalsIgnoreCase("nropicking"))
      return nropicking;
    else if (fieldName.equalsIgnoreCase("numguia"))
      return numguia;
    else if (fieldName.equalsIgnoreCase("estado"))
      return estado;
    else if (fieldName.equalsIgnoreCase("feccreacion"))
      return feccreacion;
    else if (fieldName.equalsIgnoreCase("fecimp"))
      return fecimp;
    else if (fieldName.equalsIgnoreCase("fecfinpicking"))
      return fecfinpicking;
    else if (fieldName.equalsIgnoreCase("fecdespacho"))
      return fecdespacho;
    else if (fieldName.equalsIgnoreCase("pickeroid"))
      return pickeroid;
    else if (fieldName.equalsIgnoreCase("pickero"))
      return pickero;
    else if (fieldName.equalsIgnoreCase("linea"))
      return linea.toString();
    else if (fieldName.equalsIgnoreCase("productoid"))
      return productoid;
    else if (fieldName.equalsIgnoreCase("producto"))
      return producto;
    else if (fieldName.equalsIgnoreCase("um"))
      return um;
    else if (fieldName.equalsIgnoreCase("ubicacion"))
      return ubicacion;
    else if (fieldName.equalsIgnoreCase("cantmov"))
      return cantmov.toString();

    else if (fieldName.equalsIgnoreCase("orgid"))
      return orgid;
    else if (fieldName.equalsIgnoreCase("orgname"))
      return orgname;

    else if (fieldName.equalsIgnoreCase("userorgid"))
      return userorgid;
    else if (fieldName.equalsIgnoreCase("userid"))
      return userid;
    else if (fieldName.equalsIgnoreCase("username"))
      return username;

    else if (fieldName.equals("rownum"))
      return rownum;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  public static ReporteListaPickingData[] selectUsers(ConnectionProvider connectionProvider,
      String adClientId, String adOrgId, String physicalNumber, String dateFrom, String dateTo,
      String adUserId, String groupByPickero) throws ServletException {
    return selectUsers(connectionProvider, adClientId, adOrgId, physicalNumber, dateFrom, dateTo,
        adUserId, groupByPickero, 0, 0);
  }

  public static ReporteListaPickingData[] selectUsers(ConnectionProvider connectionProvider,
      String adClientId, String adOrgId, String physicalNumber, String dateFrom, String dateTo,
      String adUserId, String groupByPickero, int firstRegister, int numberRegisters)
      throws ServletException {

    String strSql = "";
    strSql = strSql
        + "select ad_org_id as userorgid, ad_user_id as userid, name as username from ad_user "
        + "where isactive = 'Y' and char_length(trim(firstname)) > 0 "
        + "and char_length(trim(username)) > 0 and trim(name) ~* '^\\D' and trim(name) ~* '^\\w' order by name ";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;

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
        ReporteListaPickingData objectReporteListaPickingData = new ReporteListaPickingData();

        objectReporteListaPickingData.userorgid = UtilSql.getValue(result, "userorgid");
        objectReporteListaPickingData.userid = UtilSql.getValue(result, "userid");
        objectReporteListaPickingData.username = UtilSql.getValue(result, "username");
        objectReporteListaPickingData.rownum = Long.toString(countRecord);
        objectReporteListaPickingData.InitRecordNumber = Integer.toString(firstRegister);

        vector.addElement(objectReporteListaPickingData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }

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

    ReporteListaPickingData objectReporteListaPickingData[] = new ReporteListaPickingData[vector
        .size()];
    vector.copyInto(objectReporteListaPickingData);
    return (objectReporteListaPickingData);
  }
  
  
  
  public static ReporteListaPickingData[] selectUsers2(ConnectionProvider connectionProvider
	      ) throws ServletException {
	    return selectUsers2(connectionProvider, 0, 0);
	  }

	  public static ReporteListaPickingData[] selectUsers2(ConnectionProvider connectionProvider,
	      int firstRegister, int numberRegisters)
	      throws ServletException {

	    String strSql = "";
	    strSql = strSql

	    
	    + " select distinct opl.ad_org_id as userorgid," 
	    + " opl.em_swa_user_pickero as userid "
	    + " ,(select au.name from ad_user au where opl.em_swa_user_pickero=au.ad_user_id) as username "
	   + "   from obwpl_pickinglist opl ";

	    ResultSet result;
	    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
	    PreparedStatement st = null;

	    int iParameter = 0;

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
	        ReporteListaPickingData objectReporteListaPickingData = new ReporteListaPickingData();

	        objectReporteListaPickingData.userorgid = UtilSql.getValue(result, "userorgid");
	        objectReporteListaPickingData.userid = UtilSql.getValue(result, "userid");
	        objectReporteListaPickingData.username = UtilSql.getValue(result, "username");
	        objectReporteListaPickingData.rownum = Long.toString(countRecord);
	        objectReporteListaPickingData.InitRecordNumber = Integer.toString(firstRegister);

	        vector.addElement(objectReporteListaPickingData);
	        if (countRecord >= numberRegisters && numberRegisters != 0) {
	          continueResult = false;
	        }
	      }

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

	    ReporteListaPickingData objectReporteListaPickingData[] = new ReporteListaPickingData[vector
	        .size()];
	    vector.copyInto(objectReporteListaPickingData);
	    return (objectReporteListaPickingData);
	  }
  
  

  public static ReporteListaPickingData[] selectOrgs(ConnectionProvider connectionProvider,
      String adClientId, String adOrgId, String physicalNumber, String dateFrom, String dateTo,
      String adUserId, String groupByPickero) throws ServletException {
    return selectOrgs(connectionProvider, adClientId, adOrgId, physicalNumber, dateFrom, dateTo,
        adUserId, groupByPickero, 0, 0);
  }

  public static ReporteListaPickingData[] selectOrgs(ConnectionProvider connectionProvider,
      String adClientId, String adOrgId, String physicalNumber, String dateFrom, String dateTo,
      String adUserId, String groupByPickero, int firstRegister, int numberRegisters)
      throws ServletException {

    String strSql = "";
    strSql = strSql + "select org.ad_org_id as orgid, org.name as orgname "
        + "from ad_org org join ad_orgtype t using (ad_orgtype_id) "
        + "where t.islegalentity!='Y' and t.isacctlegalentity!='Y' "
        + "and org.ad_org_id not in ('0') " + "order by org.name ";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;

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
        ReporteListaPickingData objectReporteListaPickingData = new ReporteListaPickingData();

        objectReporteListaPickingData.orgid = UtilSql.getValue(result, "orgid");
        objectReporteListaPickingData.orgname = UtilSql.getValue(result, "orgname");
        objectReporteListaPickingData.rownum = Long.toString(countRecord);
        objectReporteListaPickingData.InitRecordNumber = Integer.toString(firstRegister);

        vector.addElement(objectReporteListaPickingData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }

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

    ReporteListaPickingData objectReporteListaPickingData[] = new ReporteListaPickingData[vector
        .size()];
    vector.copyInto(objectReporteListaPickingData);
    return (objectReporteListaPickingData);
  }

  public static ReporteListaPickingData[] select(ConnectionProvider connectionProvider,
      String adClientId, String adOrgId, String physicalNumber, String dateFrom, String dateTo,
      String adUserId, String groupByPickero, String showLines) throws ServletException {

    return select(connectionProvider, adClientId, adOrgId, physicalNumber, dateFrom, dateTo,
        adUserId, groupByPickero, showLines, 0, 0);

  }

  public static ReporteListaPickingData[] select(ConnectionProvider connectionProvider,
      String adClientId, String adOrgId, String physicalNumber, String dateFrom, String dateTo,
      String adUserId, String groupByPickero, String showLines, int firstRegister,
      int numberRegisters) throws ServletException {

    String strSql = "";
    strSql = strSql
        + "select pl.obwpl_pickinglist_id as pickingid, "
        + "(select documentno from c_order where c_order_id = pl.em_swa_c_order_id) as nropedventa, "
        + "(select documentno from swa_requerimientoreposicion where swa_requerimientoreposicion_id  = pl.em_swa_requerepo_id) as nroordtrans, "
        + "(select documentno from c_order where c_order_id = pl.em_ssa_service_order_id) as nroordserv, "
        + "pl.documentno as nropicking, "
        + "coalesce(	io.em_scr_physical_documentno,'- ')||' / '||io.documentno AS numguia,"
        + "(select coalesce(t.name,l.name) from ad_ref_list l "
        + "left join ad_ref_list_trl t on l.ad_ref_list_id = t.ad_ref_list_id and t.ad_language = 'es_PE' "
        + "where l.ad_reference_id = (select ad_reference_id from ad_reference where name = 'All_Document Status') "
        + "and value = io.docstatus) as estado, "
        + "to_char(io.created) as feccreacion, "
        + "to_char(pl.dateprinted) as fecimp, "
        + "to_char(io.em_swa_pickdatetime) as fecfinpicking, "
        + "to_char(io.em_swa_todispatchzonedatetime) as fecdespacho, "
        + (groupByPickero.equalsIgnoreCase("Y") ? " pl.em_swa_user_pickero " : " to_char('') ")
        + " as pickeroid, "
        + "(select name from ad_user where ad_user_id = pl.em_swa_user_pickero) as pickero, "

        + "coalesce(iol.line,0) as linea, "
        + "pro.m_product_id as productoid, "
        + "pro.value||' - '||pro.name as producto, "
        + "coalesce((select uomsymbol from c_uom_trl where c_uom_id = uom.c_uom_id and ad_language = 'es_PE'),uom.uomsymbol) as um,"
        + "loc.value as ubicacion, " + "coalesce(iol.movementqty,0.0) as cantmov " + "from obwpl_pickinglist pl "
        + "left join m_inoutline iol on pl.obwpl_pickinglist_id = iol.em_obwpl_pickinglist_id "
        + "left join m_product pro on iol.m_product_id = pro.m_product_id "
        + "left join c_uom uom on iol.c_uom_id = uom.c_uom_id "
        + "left join m_locator loc on iol.m_locator_id = loc.m_locator_id "
        + "left join m_inout io on iol.m_inout_id = io.m_inout_id "
        + "where ad_isorgincluded(pl.ad_org_id,?,?)<>-1 "
        + "and pl.documentdate::DATE between to_date(?) and to_date(?)  ";
    if (physicalNumber != null && physicalNumber != "") {
      strSql = strSql + " and to_char(?) in (io.em_scr_physical_documentno,io.documentno) ";
    }
    if (adUserId != null && adUserId != "") {
      strSql = strSql + " and pl.em_swa_user_pickero = ? ";
    }

    strSql = strSql + " order by " + (groupByPickero.equalsIgnoreCase("Y") ? " 13, " : "")
        + " pl.created, pl.documentno asc ";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adOrgId);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adClientId);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, dateFrom);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, dateTo);
      if (physicalNumber != null && physicalNumber != "") {
        iParameter++;
        UtilSql.setValue(st, iParameter, 12, null, physicalNumber);
      }
      if (adUserId != null && adUserId != "") {
        iParameter++;
        UtilSql.setValue(st, iParameter, 12, null, adUserId);
      }

      // iParameter++;
      // UtilSql.setValue(st, iParameter, 12, null, dateTo);
      // if (BpartnerId != null && !(BpartnerId.equals(""))) {
      // iParameter++;
      // UtilSql.setValue(st, iParameter, 12, null, BpartnerId);
      // }
      // if (ProductId != null && !(ProductId.equals(""))) {
      // iParameter++;
      // UtilSql.setValue(st, iParameter, 12, null, ProductId);
      // }
      // if (numberOrder != null && !(numberOrder.equals(""))) {
      // iParameter++;
      // UtilSql.setValue(st, iParameter, 12, null, numberOrder);
      // }

      // System.out.println(st);
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
        ReporteListaPickingData objectReporteListaPickingData = new ReporteListaPickingData();
        objectReporteListaPickingData.pickingid = UtilSql.getValue(result, "pickingid");
        objectReporteListaPickingData.nropedventa = UtilSql.getValue(result, "nropedventa");
        objectReporteListaPickingData.nroordtrans = UtilSql.getValue(result, "nroordtrans");
        objectReporteListaPickingData.nroordserv = UtilSql.getValue(result, "nroordserv");
        objectReporteListaPickingData.nropicking = UtilSql.getValue(result, "nropicking");
        objectReporteListaPickingData.numguia = UtilSql.getValue(result, "numguia");
        objectReporteListaPickingData.estado = UtilSql.getValue(result, "estado");
        objectReporteListaPickingData.feccreacion = UtilSql.getValue(result, "feccreacion");
        objectReporteListaPickingData.fecimp = UtilSql.getValue(result, "fecimp");
        objectReporteListaPickingData.fecfinpicking = UtilSql.getValue(result, "fecfinpicking");
        objectReporteListaPickingData.fecdespacho = UtilSql.getValue(result, "fecdespacho");
        objectReporteListaPickingData.pickeroid = UtilSql.getValue(result, "pickeroid");
        objectReporteListaPickingData.pickero = UtilSql.getValue(result, "pickero");
        objectReporteListaPickingData.linea = new BigDecimal(UtilSql.getValue(result, "linea"));
        objectReporteListaPickingData.productoid = UtilSql.getValue(result, "productoid");
        objectReporteListaPickingData.producto = UtilSql.getValue(result, "producto");
        objectReporteListaPickingData.um = UtilSql.getValue(result, "um");
        objectReporteListaPickingData.ubicacion = UtilSql.getValue(result, "ubicacion");
        objectReporteListaPickingData.cantmov = new BigDecimal(UtilSql.getValue(result, "cantmov"));
        objectReporteListaPickingData.rownum = Long.toString(countRecord);
        objectReporteListaPickingData.InitRecordNumber = Integer.toString(firstRegister);

        vector.addElement(objectReporteListaPickingData);
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
    ReporteListaPickingData objectReporteListaPickingData[] = new ReporteListaPickingData[vector
        .size()];
    vector.copyInto(objectReporteListaPickingData);
    return (objectReporteListaPickingData);
  }

  public static ReporteListaPickingData[] set() throws ServletException {
    ReporteListaPickingData objectReporteListaPickingData[] = new ReporteListaPickingData[1];
    objectReporteListaPickingData[0] = new ReporteListaPickingData();
    objectReporteListaPickingData[0].pickingid = "";
    objectReporteListaPickingData[0].nropedventa = "";
    objectReporteListaPickingData[0].nroordtrans = "";
    objectReporteListaPickingData[0].nroordserv = "";
    objectReporteListaPickingData[0].nropicking = "";
    objectReporteListaPickingData[0].numguia = "";
    objectReporteListaPickingData[0].estado = "";
    objectReporteListaPickingData[0].feccreacion = "";
    objectReporteListaPickingData[0].fecimp = "";
    objectReporteListaPickingData[0].fecfinpicking = "";
    objectReporteListaPickingData[0].pickero = "";
    objectReporteListaPickingData[0].linea = new BigDecimal(0);
    objectReporteListaPickingData[0].productoid = "";
    objectReporteListaPickingData[0].producto = "";
    objectReporteListaPickingData[0].um = "";
    objectReporteListaPickingData[0].ubicacion = "";
    objectReporteListaPickingData[0].cantmov = new BigDecimal(0);

    objectReporteListaPickingData[0].orgid = "";
    objectReporteListaPickingData[0].orgname = "";

    objectReporteListaPickingData[0].userorgid = "";
    objectReporteListaPickingData[0].userid = "";
    objectReporteListaPickingData[0].username = "";

    return objectReporteListaPickingData;

  }

}
