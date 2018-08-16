//Sqlc generated V1.O00-1
package pe.com.unifiedgo.warehouse.ad_reports;

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

class ReportInventoryTransactionData implements FieldProvider {
  static Logger log4j = Logger.getLogger(ReportInventoryTransaction.class);
  private String InitRecordNumber = "0";
  // Datos de las lineas
  public String lineaid;
  public BigDecimal linea;
  public String productoid;
  public String codigo;
  public String producto;
  public String um;
  public String ubicacion;
  public BigDecimal cantteorica;
  public BigDecimal canttotal;
  public BigDecimal pricont;
  public BigDecimal segcont;
  public BigDecimal difcont;
  public BigDecimal tercont;
  public String descripcion;

  // Datos del documento

  public String orgpadre;
  public String organizacion;
  public String documento;
  public String numerodoc;
  public String fecmovimiento;
  public String nombre;
  public String almacen;
  public String descripciondoc;

  public String rownum;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  @Override
  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("lineaid"))
      return lineaid;
    else if (fieldName.equalsIgnoreCase("linea"))
      return linea.toString();
    else if (fieldName.equalsIgnoreCase("productoid"))
      return productoid;
    else if (fieldName.equalsIgnoreCase("codigo"))
      return codigo;
    else if (fieldName.equalsIgnoreCase("producto"))
      return producto;
    else if (fieldName.equalsIgnoreCase("um"))
      return um;
    else if (fieldName.equalsIgnoreCase("ubicacion"))
      return ubicacion;
    else if (fieldName.equalsIgnoreCase("cantteorica"))
      return cantteorica.toString();
    else if (fieldName.equalsIgnoreCase("canttotal"))
      return canttotal.toString();
    else if (fieldName.equalsIgnoreCase("pricont"))
      return pricont.toString();
    else if (fieldName.equalsIgnoreCase("segcont"))
      return segcont.toString();
    else if (fieldName.equalsIgnoreCase("difcont"))
      return difcont.toString();
    else if (fieldName.equalsIgnoreCase("tercont"))
      return tercont.toString();
    else if (fieldName.equalsIgnoreCase("descripcion"))
      return descripcion;

    else if (fieldName.equalsIgnoreCase("orgpadre"))
      return orgpadre;
    else if (fieldName.equalsIgnoreCase("organizacion"))
      return organizacion;
    else if (fieldName.equalsIgnoreCase("documento"))
      return documento;
    else if (fieldName.equalsIgnoreCase("numerodoc"))
      return numerodoc;
    else if (fieldName.equalsIgnoreCase("fecmovimiento"))
      return fecmovimiento;
    else if (fieldName.equalsIgnoreCase("nombre"))
      return nombre;
    else if (fieldName.equalsIgnoreCase("almacen"))
      return almacen;
    else if (fieldName.equalsIgnoreCase("descripciondoc"))
      return descripciondoc;

    else if (fieldName.equals("rownum"))
      return rownum;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  public static ReportInventoryTransactionData[] selectHeaderDoc(
      ConnectionProvider connectionProvider, String DocumentId, String adLanguage)
      throws ServletException {

    return selectHeaderDoc(connectionProvider, DocumentId, adLanguage, 0, 0);
  }

  public static ReportInventoryTransactionData[] selectHeaderDoc(
      ConnectionProvider connectionProvider, String DocumentId, String adLanguage,
      int firstRegister, int numberRegisters) throws ServletException {
    String strSql = "";
    strSql = strSql + "select org.name as orgpadre, "
        + "(select name from ad_org where ad_org_id = inv.ad_org_id) as organizacion, "
        + "coalesce((select name from c_doctype_trl where c_doctype_id = doc.c_doctype_id  "
        + "and ad_language = ?),doc.name) as documento, " + "inv.documentno as numerodoc, "
        + "to_char(inv.movementdate) as fecmovimiento, " + "inv.name as nombre, "
        + "wh.value||' - '||wh.name as almacen, " + "inv.description as descripciondoc "
        + "from m_inventory inv "
        + "join c_doctype doc on inv.c_doctypetarget_id = doc.c_doctype_id "
        + "join m_warehouse wh on inv.m_warehouse_id = wh.m_warehouse_id, "
        + "ad_org org join ad_orgtype typ using (ad_orgtype_id) "
        + "where ad_isorgincluded(inv.ad_org_id, org.ad_org_id, inv.ad_client_id)<>-1 "
        + "and(typ.islegalentity='Y' or typ.isacctlegalentity='Y') " + "and inv.isactive = 'Y' "
        + "and inv.m_inventory_id in (?) ";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;

    try {
      st = connectionProvider.getPreparedStatement(strSql);

      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adLanguage);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, DocumentId);
      result = st.executeQuery();

      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while (countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }
      if (result.next()) {
        countRecord++;
        ReportInventoryTransactionData objectReportInventoryTransactionData = new ReportInventoryTransactionData();
        objectReportInventoryTransactionData.orgpadre = UtilSql.getValue(result, "orgpadre");
        objectReportInventoryTransactionData.organizacion = UtilSql
            .getValue(result, "organizacion");
        objectReportInventoryTransactionData.documento = UtilSql.getValue(result, "documento");
        objectReportInventoryTransactionData.numerodoc = UtilSql.getValue(result, "numerodoc");
        objectReportInventoryTransactionData.fecmovimiento = UtilSql.getValue(result,
            "fecmovimiento");
        objectReportInventoryTransactionData.nombre = UtilSql.getValue(result, "nombre");
        objectReportInventoryTransactionData.almacen = UtilSql.getValue(result, "almacen");
        objectReportInventoryTransactionData.descripciondoc = UtilSql.getValue(result,
            "descripciondoc");
        objectReportInventoryTransactionData.rownum = Long.toString(countRecord);
        objectReportInventoryTransactionData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectReportInventoryTransactionData);
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

    ReportInventoryTransactionData objectReportInventoryTransactionData[] = new ReportInventoryTransactionData[vector
        .size()];
    vector.copyInto(objectReportInventoryTransactionData);
    return (objectReportInventoryTransactionData);

  }

  public static ReportInventoryTransactionData[] selectData(ConnectionProvider connectionProvider,
      String DocumentId, String adLanguage) throws ServletException {
    return selectData(connectionProvider, DocumentId, adLanguage, 0, 0);
  }

  public static ReportInventoryTransactionData[] selectData(ConnectionProvider connectionProvider,
      String DocumentId, String adLanguage, int firstRegister, int numberRegisters)
      throws ServletException {

    String strSql = "";
    strSql = strSql + "select lin.m_inventoryline_id as lineaid, " + "lin.line as linea, "
        + "lin.m_product_id as productoid, " + "pro.value as codigo, " + "pro.name as producto, "
        + "coalesce((select name from c_uom_trl where c_uom_id = uom.c_uom_id "
        + "and ad_language = ?),uom.name) as um, " + "loc.value as ubicacion, "
        + "lin.qtybook as cantteorica, " + "lin.qtycount as canttotal, "
        + "em_sco_firstcount as pricont, " + "em_sco_secondcount as segcont, "
        + "em_sco_countdiff as difcont, " + "em_swa_thirdcount as tercont, "
        + "lin.description as descripcion "

        + "from m_inventoryline lin "
        + "left join m_product pro on lin.m_product_id = pro.m_product_id "
        + "left join m_locator loc on lin.m_locator_id = loc.m_locator_id "
        + "left join c_uom uom on lin.c_uom_id = uom.c_uom_id "
        + "where lin.m_inventory_id in (?) " + "order by lin.line,pro.value ";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adLanguage);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, DocumentId);

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
        ReportInventoryTransactionData objectReportInventoryTransactionData = new ReportInventoryTransactionData();
        objectReportInventoryTransactionData.lineaid = UtilSql.getValue(result, "lineaid");
        objectReportInventoryTransactionData.linea = new BigDecimal(UtilSql.getValue(result,
            "linea"));
        objectReportInventoryTransactionData.productoid = UtilSql.getValue(result, "productoid");
        objectReportInventoryTransactionData.codigo = UtilSql.getValue(result, "codigo");
        objectReportInventoryTransactionData.producto = UtilSql.getValue(result, "producto");
        objectReportInventoryTransactionData.um = UtilSql.getValue(result, "um");
        objectReportInventoryTransactionData.ubicacion = UtilSql.getValue(result, "ubicacion");
        objectReportInventoryTransactionData.cantteorica = new BigDecimal(UtilSql.getValue(result,
            "cantteorica"));
        objectReportInventoryTransactionData.canttotal = new BigDecimal(UtilSql.getValue(result,
            "canttotal"));
        objectReportInventoryTransactionData.pricont = new BigDecimal(UtilSql.getValue(result,
            "pricont"));
        objectReportInventoryTransactionData.segcont = new BigDecimal(UtilSql.getValue(result,
            "segcont"));
        objectReportInventoryTransactionData.difcont = new BigDecimal(UtilSql.getValue(result,
            "difcont"));
        objectReportInventoryTransactionData.tercont = new BigDecimal(UtilSql.getValue(result,
            "tercont"));
        objectReportInventoryTransactionData.descripcion = UtilSql.getValue(result, "descripcion");
        objectReportInventoryTransactionData.rownum = Long.toString(countRecord);
        objectReportInventoryTransactionData.InitRecordNumber = Integer.toString(firstRegister);

        vector.addElement(objectReportInventoryTransactionData);
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
    ReportInventoryTransactionData objectReportInventoryTransactionData[] = new ReportInventoryTransactionData[vector
        .size()];
    vector.copyInto(objectReportInventoryTransactionData);
    return (objectReportInventoryTransactionData);
  }

  public static ReportInventoryTransactionData[] set() throws ServletException {
    ReportInventoryTransactionData objectReportInventoryTransactionData[] = new ReportInventoryTransactionData[1];
    objectReportInventoryTransactionData[0] = new ReportInventoryTransactionData();
    objectReportInventoryTransactionData[0].lineaid = "";
    objectReportInventoryTransactionData[0].linea = new BigDecimal(0);
    objectReportInventoryTransactionData[0].productoid = "";
    objectReportInventoryTransactionData[0].codigo = "";
    objectReportInventoryTransactionData[0].producto = "";
    objectReportInventoryTransactionData[0].um = "";
    objectReportInventoryTransactionData[0].ubicacion = "";
    objectReportInventoryTransactionData[0].cantteorica = new BigDecimal(0);
    objectReportInventoryTransactionData[0].canttotal = new BigDecimal(0);
    objectReportInventoryTransactionData[0].pricont = new BigDecimal(0);
    objectReportInventoryTransactionData[0].segcont = new BigDecimal(0);
    objectReportInventoryTransactionData[0].difcont = new BigDecimal(0);
    objectReportInventoryTransactionData[0].tercont = new BigDecimal(0);
    objectReportInventoryTransactionData[0].descripcion = "";

    return objectReportInventoryTransactionData;

  }

  public static ReportInventoryTransactionData[] setHeader() throws ServletException {
    ReportInventoryTransactionData objectReportInventoryTransactionData[] = new ReportInventoryTransactionData[1];
    objectReportInventoryTransactionData[0] = new ReportInventoryTransactionData();
    objectReportInventoryTransactionData[0].orgpadre = "";
    objectReportInventoryTransactionData[0].organizacion = "";
    objectReportInventoryTransactionData[0].documento = "";
    objectReportInventoryTransactionData[0].numerodoc = "";
    objectReportInventoryTransactionData[0].fecmovimiento = "";
    objectReportInventoryTransactionData[0].nombre = "";
    objectReportInventoryTransactionData[0].almacen = "";
    objectReportInventoryTransactionData[0].descripciondoc = "";

    return objectReportInventoryTransactionData;

  }

}
