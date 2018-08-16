//Sqlc generated V1.O00-1
package pe.com.unifiedgo.report.ad_reports;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

class ReportCompleteProductsJRData implements FieldProvider {
  static Logger log4j = Logger.getLogger(ReportCompleteProductsJRData.class);
  
  private String InitRecordNumber = "0";
  public String CODIGO;
  public String TRANS;
  public String TIP_TRANS;
  public BigDecimal TOTAL;
  public BigDecimal TOT_COST;
  public BigDecimal COS_INI;
  public String ORG_ID;
  public String ORG;
  public String RUC;
  public String rownum;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  @Override
  public String getField(String fieldName) {
	  
	  if (fieldName.equalsIgnoreCase("CODIGO"))
		  return CODIGO;
	  else if (fieldName.equalsIgnoreCase("TRANS"))
		  return TRANS;
	  else if (fieldName.equalsIgnoreCase("TIP_TRANS"))
			  return TIP_TRANS;
	  else if (fieldName.equalsIgnoreCase("TOTAL"))
		  return TOTAL.toString();
	  else if (fieldName.equalsIgnoreCase("TOT_COST"))
		  return TOT_COST.toString();
	  else if (fieldName.equalsIgnoreCase("COS_INI"))
		  return COS_INI.toString();
	  else if (fieldName.equalsIgnoreCase("ORG_ID"))
		  return ORG_ID;
	  else if (fieldName.equalsIgnoreCase("ORG"))
		  return ORG;
	  else if (fieldName.equalsIgnoreCase("RUC"))
		  return RUC;
	  else if (fieldName.equalsIgnoreCase("rownum"))
		  return rownum;
	  else {
		  log4j.debug("Field does not exist: " + fieldName);
	      return null;
	  }
		  
		  
	  
	/*  
    if (fieldName.equalsIgnoreCase("ORG_ID"))
      return ORG_ID;
    else if (fieldName.equalsIgnoreCase("ORG"))
      return ORG;
    else if (fieldName.equalsIgnoreCase("CODIGO"))
      return CODIGO;
    else if (fieldName.equalsIgnoreCase("TRANS"))
      return TRANS;
    else if (fieldName.equalsIgnoreCase("TIP_TRANS"))
      return TIP_TRANS;
    else if (fieldName.equalsIgnoreCase("TOTAL"))
      return TOTAL.toString();
    else if (fieldName.equalsIgnoreCase("TOT_COST"))
      return TOT_COST.toString();
    else if (fieldName.equalsIgnoreCase("RUC"))
      return RUC;
    else if (fieldName.equals("rownum"))
      return rownum;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }*/
  }

  public static ReportCompleteProductsJRData[] select(String adOrgId, String adClientId,
      String startDate, String endDate) throws ServletException {
    return select(adOrgId, adClientId, startDate, endDate, 0, 0);
  }

  public static ReportCompleteProductsJRData[] select(String adOrgId, String adClientId,
      String startDate, String endDate, int firstRegister, int numberRegisters)
      throws ServletException {
	  
	   	String strSql = "";
	   	
	   		strSql = "select ite.value as CODIGO, "+
    		"ite.name as TRANS, "+
    		"case when sum(coalesce(tra.movementqty,0.00)) > 0 then '+' else '-' end as TIP_TRANS, "+
    		"sum(coalesce(tra.movementqty,0.00)) as TOTAL, "+
    		"sum(coalesce(tra.transactioncost,0.00)) as TOT_COST, "+
    		"(0.00) as COS_INI, "+
    		"cast('' as TEXT) as ORG_ID, "+
    		"cast('' as TEXT) as ORG, "+
    		"coalesce(inf.taxid,'') as RUC "+
    		"from m_transaction tra "+
    		" left JOIN m_inoutline miol on tra.m_inoutline_id=miol.m_inoutline_id " +
    		" left join m_inout mio on miol.m_inout_id=mio.m_inout_id " +
    		"left join scr_combo_item ite on tra.em_ssa_combo_item_id = ite.scr_combo_item_id "+
    		"left join ad_org org on tra.ad_org_id=org.ad_org_id "+
    		"left join ad_orginfo inf on inf.ad_org_id=org.ad_org_id  "+
    		"where  "+
    		"  coalesce (mio.docstatus,'') !='VO' "+
    		"and tra.isactive = 'Y' "+
    		"and tra.em_ssa_combo_item_id is not null "+
    		"and tra.movementdate between TO_DATE('"+startDate+"','DD-MM-YYYY') and TO_DATE('"+endDate+"','DD-MM-YYYY') "+
    		"and tra.ad_org_id in ("+adOrgId+") "+
    		"group by ite.value,ite.name,case when (tra.movementqty) > 0 then '+' else '-' end,coalesce(inf.taxid,'') "+
    		"order by case when (tra.movementqty) > 0 then '+' else '-' end desc,ite.name "
    		;
    
    
	   		/*
    strSql = "select ite.value as CODIGO, "
        + "          ite.name as TRANS, "
        + "          case when sum(coalesce(tra.movementqty,0.00)) > 0 then '+' else '-' end as TIP_TRANS,"
        + "          sum(coalesce(tra.movementqty,0.00)) as TOTAL, "
        + "          sum(coalesce(tra.transactioncost,0.00)) as TOT_COST, "
        + "          ORG.ad_org_id as ORG_ID, "
        + "          coalesce(ORG.name,'') as ORG,"
        + "          coalesce(inf.taxid,'') as RUC"
        + "   from m_transaction tra"
        + "           left join scr_combo_item ite on tra.em_ssa_combo_item_id = ite.scr_combo_item_id, "
        + "        ad_org ORG join ad_orgtype typ using (ad_orgtype_id) "
        + "           join ad_orginfo inf on ORG.ad_org_id = inf.ad_org_id"
        + "  where AD_ISORGINCLUDED(tra.ad_org_id, ORG.ad_org_id, tra.ad_client_id)<>-1"
        + "    and(typ.IsLegalEntity='Y' or typ.IsAcctLegalEntity='Y')"
        + "    and tra.isactive = 'Y'"
        + "    and tra.em_ssa_combo_item_id is not null"
        + "    and tra.created between TO_DATE('"
        + startDate
        + "', 'DD-MM-YYYY') and TO_DATE('"
        + endDate
        + "', 'DD-MM-YYYY')"
        + "    and ORG.ad_org_id in ('"
        + adOrgId
        + "')"
        + "  group by ite.value, ite.name, case when (tra.movementqty) > 0 then '+' else '-' end,ORG.ad_org_id,coalesce(ORG.name,''),coalesce(inf.taxid,'') "
        + "  order by case when (tra.movementqty) > 0 then '+' else '-' end desc,ite.name;";
        */

    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);

    long countRecord = 0;

    try {
      Query sqlQuery = OBDal.getInstance().getSession().createSQLQuery(strSql);
      List<Object> data = sqlQuery.list();

      for (int k = 0; k < data.size(); k++) {
        Object[] obj = (Object[]) data.get(k);
        countRecord++;
        ReportCompleteProductsJRData objectReportCompleteProductsJRData = new ReportCompleteProductsJRData();
        
        objectReportCompleteProductsJRData.CODIGO = (String) obj[0];
        objectReportCompleteProductsJRData.TRANS = (String) obj[1];
        objectReportCompleteProductsJRData.TIP_TRANS = (String) obj[2];
        objectReportCompleteProductsJRData.TOTAL = ((BigDecimal) (obj[3])).setScale(3,
                BigDecimal.ROUND_HALF_UP);
            objectReportCompleteProductsJRData.TOT_COST = ((BigDecimal) (obj[4])).setScale(3,
                BigDecimal.ROUND_HALF_UP);
        objectReportCompleteProductsJRData.COS_INI = ((BigDecimal) (obj[5])).setScale(3,
        		BigDecimal.ROUND_HALF_UP);
        objectReportCompleteProductsJRData.ORG_ID = (String) obj[6];
        objectReportCompleteProductsJRData.ORG = (String) obj[7];
        objectReportCompleteProductsJRData.RUC = (String) obj[8];
        
        
        objectReportCompleteProductsJRData.rownum = Long.toString(countRecord);
        objectReportCompleteProductsJRData.InitRecordNumber = Integer.toString(firstRegister);

        vector.addElement(objectReportCompleteProductsJRData);

      }
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    }

    ReportCompleteProductsJRData objectReportCompleteProductsJRData[] = new ReportCompleteProductsJRData[vector
        .size()];
    vector.copyInto(objectReportCompleteProductsJRData);

    return (objectReportCompleteProductsJRData);
  }
  

  public static ReportCompleteProductsJRData[] selectSaldo(String adOrgId, String adClientId,
	      String startDate, String endDate) throws ServletException {
	    return selectSaldo(adOrgId, adClientId, startDate, endDate, 0, 0);
	  }

	  public static ReportCompleteProductsJRData[] selectSaldo(String adOrgId, String adClientId,
	      String startDate, String endDate, int firstRegister, int numberRegisters)
	      throws ServletException {
		  
		   	String strSql = "";
		   	
		   		strSql = " select COALESCE ( sum(case when tra.movementqty > 0  then coalesce(tra.transactioncost,0.00) "
		   				+ " else -1* coalesce(tra.transactioncost,0.00) end ), 0.00) as COS_INI"+
	    		" from m_transaction tra "
	            + "     left join  m_inoutline miol on  tra.m_inoutline_id = miol.m_inoutline_id               "
	            + "     left join  m_inout mio on  miol.m_inout_id = mio.m_inout_id               "+
	    		" where tra.ad_org_id in ("+adOrgId+") and coalesce (mio.docstatus,'') !='VO' "+
	    		" and tra.ad_client_id in ('"+adClientId+"') "+
	    		" and tra.isactive = 'Y' "+
	    		" and tra.movementdate < TO_DATE('"+startDate+"','DD-MM-YYYY')";
	   
	    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);

	    long countRecord = 0;

	    try {
	      Query sqlQuery = OBDal.getInstance().getSession().createSQLQuery(strSql);
	      List<Object> data = sqlQuery.list();

	      for (int k = 0; k < data.size(); k++) {
//	        Object[] obj = (Object[]) data.get(k);
	        countRecord++;
	        ReportCompleteProductsJRData objectReportCompleteProductsJRData = new ReportCompleteProductsJRData();
	        

	        objectReportCompleteProductsJRData.COS_INI = (((BigDecimal)data.get(k))).setScale(3,
	        		BigDecimal.ROUND_HALF_UP);
	        
	        
	        objectReportCompleteProductsJRData.rownum = Long.toString(countRecord);
	        objectReportCompleteProductsJRData.InitRecordNumber = Integer.toString(firstRegister);

	        vector.addElement(objectReportCompleteProductsJRData);

	      }
	    } catch (Exception ex) {
	      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
	      throw new ServletException("@CODE=@" + ex.getMessage());
	    }

	    ReportCompleteProductsJRData objectReportCompleteProductsJRData[] = new ReportCompleteProductsJRData[vector
	        .size()];
	    vector.copyInto(objectReportCompleteProductsJRData);

	    return (objectReportCompleteProductsJRData);
	  }
  

  public static ReportCompleteProductsJRData[] set() throws ServletException {
    ReportCompleteProductsJRData objectReportCompleteProductsJRData[] = new ReportCompleteProductsJRData[1];
    objectReportCompleteProductsJRData[0] = new ReportCompleteProductsJRData();
    
    objectReportCompleteProductsJRData[0].CODIGO = "";
    objectReportCompleteProductsJRData[0].TRANS = "";
    objectReportCompleteProductsJRData[0].TIP_TRANS = "";
    objectReportCompleteProductsJRData[0].TOTAL = new BigDecimal(0);
    objectReportCompleteProductsJRData[0].TOT_COST = new BigDecimal(0);
    objectReportCompleteProductsJRData[0].COS_INI = new BigDecimal(0);
    objectReportCompleteProductsJRData[0].ORG_ID = "";
    objectReportCompleteProductsJRData[0].ORG = "";
    objectReportCompleteProductsJRData[0].RUC = "";
    

    return objectReportCompleteProductsJRData;
  }
  
  
  public static String selectNombreOrganizacion(ConnectionProvider connectionProvider, String organization)    throws ServletException {
	    String strSql = "";
	    strSql = strSql + 
	      "        SELECT name" +
	      "        FROM AD_ORG" +
	      "        WHERE AD_ORG_ID = ?";

	    ResultSet result;
	    String strReturn = "0";
	    PreparedStatement st = null;

	    int iParameter = 0;
	    try {
	    st = connectionProvider.getPreparedStatement(strSql);
	      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
	      iParameter++; UtilSql.setValue(st, iParameter, 12, null, organization);

	      result = st.executeQuery();
	      if(result.next()) {
	        strReturn = UtilSql.getValue(result, "name");
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
