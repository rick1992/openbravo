//Sqlc generated V1.O00-1
package pe.com.unifiedgo.report.ad_reports;

import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;

class ReporteEstadoGuiaJRData implements FieldProvider {
  static Logger log4j = Logger.getLogger(ReporteEstadoGuiaJRData.class);
  private String InitRecordNumber = "0";
  public String orgid;
  public String shipmentid;
  public String physicaldocno;
  public String invoiceid;
  public String invphysicaldocno;
  public String shipmentstatus;
  public String bpartnerid;
  public String bpartnername;
  public String movementdate;
  public String pickdate;
  public String packdate;
  public String uwaydate;
  public String deliverydate;

  public String rownum;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  @Override
  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("orgid"))
      return orgid;
    else if (fieldName.equalsIgnoreCase("shipmentid"))
      return shipmentid;
    else if (fieldName.equalsIgnoreCase("physicaldocno"))
      return physicaldocno;
    else if (fieldName.equalsIgnoreCase("invoiceid"))
      return invoiceid;
    else if (fieldName.equalsIgnoreCase("invphysicaldocno"))
      return invphysicaldocno;
    else if (fieldName.equalsIgnoreCase("shipmentstatus"))
      return shipmentstatus;
    else if (fieldName.equalsIgnoreCase("bpartnerid"))
      return bpartnerid;
    else if (fieldName.equalsIgnoreCase("bpartnername"))
      return bpartnername;
    else if (fieldName.equalsIgnoreCase("movementdate"))
      return movementdate;
    else if (fieldName.equalsIgnoreCase("pickdate"))
      return pickdate;
    else if (fieldName.equalsIgnoreCase("packdate"))
      return packdate;
    else if (fieldName.equalsIgnoreCase("uwaydate"))
      return uwaydate;
    else if (fieldName.equalsIgnoreCase("deliverydate"))
      return deliverydate;
    else if (fieldName.equals("rownum"))
      return rownum;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  public static ReporteEstadoGuiaJRData[] select(String adOrgId, String adClientId,
      String strDocDate, String strPhysicalDocNumber, String adUserId) throws ServletException {
    return select(adOrgId, adClientId, strDocDate, strPhysicalDocNumber, adUserId, 0, 0);
  }

  public static ReporteEstadoGuiaJRData[] select(String adOrgId, String adClientId,
      String strDocDate, String strPhysicalDocNumber, String adUserId, int firstRegister,
      int numberRegisters) throws ServletException {

    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);

    String strOptionalCondition = "";
    if (strPhysicalDocNumber != null && !"".equals(strPhysicalDocNumber)
        && strPhysicalDocNumber.matches("^[a-zA-Z0-9]+-[a-zA-Z0-9]+$")) {
      String strPhysicalDocNumber_serie = strPhysicalDocNumber.split("-[a-zA-Z0-9]+$")[0];
      String strPhysicalDocNumber_number = strPhysicalDocNumber
          .substring(strPhysicalDocNumber_serie.length() + 1);

      strOptionalCondition = "(io.em_scr_physical_documentno ~ '" + strPhysicalDocNumber_serie
          + "-[0]*" + strPhysicalDocNumber_number + "')";
    }
    if (strDocDate != null && !"".equals(strDocDate)) {
      if (!"".equals(strOptionalCondition))
        strOptionalCondition = strOptionalCondition + " or ";

      strOptionalCondition = strOptionalCondition + " (DATE(io.MovementDate) = TO_DATE('"
          + strDocDate + "','DD-MM-YYYY'))";
    }

    String strSql = "";
    strSql = "select io.m_inout_id, COALESCE(io.em_scr_physical_documentno,'--') as physicaldocno, "
        + "          (select COALESCE(c_invoice_id,'--')"
        + "             from c_invoiceline"
        + "            where m_inoutline_id=(select m_inoutline_id "
        + "                                    from m_inoutline iol "
        + "                                   where iol.m_inout_id=io.m_inout_id limit 1)) as invoiceid,"
        + "          COALESCE((select coalesce(em_scr_physical_documentno,'--')"
        + "                      from c_invoice inv"
        + "                     where inv.c_invoice_id=(select COALESCE(c_invoice_id,'--')"
        + "                                               from c_invoiceline"
        + "                                              where m_inoutline_id=(select m_inoutline_id "
        + "                                                                      from m_inoutline iol "
        + "                                                                     where iol.m_inout_id=io.m_inout_id limit 1))),'Sin Factura') as invphysicaldocno,"
        + "          (select text(em_spl_isprinted)"
        + "             from OBWPL_pickinglist pl"
        + "            where pl.OBWPL_pickinglist_id=(select iol.em_obwpl_pickinglist_id"
        + "                                             from m_inoutline iol"
        + "                                            where iol.m_inout_id=io.m_inout_id limit 1)) as pickprintedstatus,"
        + "         io.EM_Swa_Shipstatus,"
        + "         bp.c_bpartner_id,"
        + "         bp.name,"
        + "         COALESCE(to_char(DATE(io.MovementDate)),'--'),"
        + "         COALESCE(to_char(DATE(io.EM_Swa_Pickdatetime)),'--'),"
        + "         COALESCE(to_char(DATE(io.EM_Swa_Packdatetime)),'--'),"
        + "         COALESCE(to_char(DATE(io.EM_Swa_Uwaydatetime)),'--'),"
        + "         COALESCE(to_char(DATE(io.EM_Swa_Delidatetime)),'--')"
        + "    from m_inout io, c_doctype dt, c_bpartner bp, c_order so "
        + "   where io.ad_client_id='"
        + adClientId
        + "'"
        + "     and io.c_order_id = so.c_order_id"
        + "     and io.c_doctype_id=dt.c_doctype_id"
        + "     and io.c_bpartner_id=bp.c_bpartner_id"
        + "     and dt.em_sco_specialdoctype='SCOMMSHIPMENT'"
        + "     and io.issotrx='Y'"
        + "     and io.em_sim_is_import='N'"
        + "     and exists(select 1"
        + "                  from m_inoutline iol"
        + "                 where iol.m_inout_id=io.m_inout_id"
        + "                   and iol.em_obwpl_pickinglist_id is not null)"
        + "     and AD_ISORGINCLUDED(io.ad_org_id, '" + adOrgId + "', '" + adClientId + "') > -1";
    strSql = strSql
        + (("".equals(strOptionalCondition)) ? "" : " AND (" + strOptionalCondition + ")");
    strSql = strSql + " order by io.em_scr_physical_documentno";
    
    String shipmentStatus = "", pickprintedstatus = "";
    long countRecord = 0;
    try {
      Query sqlQuery = OBDal.getInstance().getSession().createSQLQuery(strSql);

      List<Object> data = sqlQuery.list();
      for (int k = 0; k < data.size(); k++) {
        Object[] obj = (Object[]) data.get(k);

        countRecord++;

        ReporteEstadoGuiaJRData objectReporteEstadoGuiaJRData = new ReporteEstadoGuiaJRData();

        objectReporteEstadoGuiaJRData.orgid = adOrgId;
        objectReporteEstadoGuiaJRData.shipmentid = (String) obj[0];
        objectReporteEstadoGuiaJRData.physicaldocno = (String) obj[1];
        objectReporteEstadoGuiaJRData.invoiceid = (String) obj[2];
        objectReporteEstadoGuiaJRData.invphysicaldocno = (String) obj[3];

        pickprintedstatus = (String) obj[4];
        shipmentStatus = (String) obj[5];
        if ("SWA_PICK".compareTo(shipmentStatus) == 0) {
          if ("N".compareTo(pickprintedstatus) == 0) {
            objectReporteEstadoGuiaJRData.shipmentstatus = "Pendiente";
          } else {
            objectReporteEstadoGuiaJRData.shipmentstatus = "En Picking";
          }
        } else if ("SWA_UWAY".compareTo(shipmentStatus) == 0) {
          objectReporteEstadoGuiaJRData.shipmentstatus = "En Camino";
        } else if ("SWA_DELI".compareTo(shipmentStatus) == 0) {
          objectReporteEstadoGuiaJRData.shipmentstatus = "Entregado";
        } else {
          objectReporteEstadoGuiaJRData.shipmentstatus = "Empaquetando";
        }

        objectReporteEstadoGuiaJRData.bpartnerid = (String) obj[6];
        objectReporteEstadoGuiaJRData.bpartnername = (String) obj[7];
        objectReporteEstadoGuiaJRData.movementdate = (String) obj[8];
        objectReporteEstadoGuiaJRData.pickdate = (String) obj[9];
        objectReporteEstadoGuiaJRData.packdate = (String) obj[10];
        objectReporteEstadoGuiaJRData.uwaydate = (String) obj[11];
        objectReporteEstadoGuiaJRData.deliverydate = (String) obj[12];

        objectReporteEstadoGuiaJRData.rownum = Long.toString(countRecord);
        objectReporteEstadoGuiaJRData.InitRecordNumber = Integer.toString(firstRegister);

        vector.addElement(objectReporteEstadoGuiaJRData);

      }
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    }

    ReporteEstadoGuiaJRData objectReporteEstadoGuiaJRData[] = new ReporteEstadoGuiaJRData[vector
        .size()];
    vector.copyInto(objectReporteEstadoGuiaJRData);

    return (objectReporteEstadoGuiaJRData);
  }

  public static ReporteEstadoGuiaJRData[] set() throws ServletException {
    ReporteEstadoGuiaJRData objectReporteEstadoGuiaJRData[] = new ReporteEstadoGuiaJRData[1];
    objectReporteEstadoGuiaJRData[0] = new ReporteEstadoGuiaJRData();
    objectReporteEstadoGuiaJRData[0].shipmentid = "";
    objectReporteEstadoGuiaJRData[0].physicaldocno = "";
    objectReporteEstadoGuiaJRData[0].invoiceid = "";
    objectReporteEstadoGuiaJRData[0].invphysicaldocno = "";
    objectReporteEstadoGuiaJRData[0].shipmentstatus = "";
    objectReporteEstadoGuiaJRData[0].bpartnerid = "";
    objectReporteEstadoGuiaJRData[0].bpartnername = "";
    objectReporteEstadoGuiaJRData[0].movementdate = "";
    objectReporteEstadoGuiaJRData[0].pickdate = "";
    objectReporteEstadoGuiaJRData[0].packdate = "";
    objectReporteEstadoGuiaJRData[0].uwaydate = "";
    objectReporteEstadoGuiaJRData[0].deliverydate = "";

    return objectReporteEstadoGuiaJRData;
  }

}
