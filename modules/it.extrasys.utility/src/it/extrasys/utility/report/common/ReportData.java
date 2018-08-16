/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2009-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

//Sqlc generated V1.O00-1
package it.extrasys.utility.report.common;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.data.FieldProvider;
import org.openbravo.data.UtilSql;
import org.openbravo.database.ConnectionProvider;

public class ReportData implements FieldProvider {
  static Logger log4j = Logger.getLogger(ReportData.class);
  private String InitRecordNumber = "0";
  public String adOrgId;
  public String documentId;
  public String docstatus;
  public String doctypetargetid;
  public String ourreference;
  public String cusreference;
  public String salnam;
  public String bpartnerId;
  public String bpartnerName;
  public String bpartnerLanguage;
  public String issalesordertransaction;
  public String isebill;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("ad_org_id") || fieldName.equals("adOrgId"))
      return adOrgId;
    else if (fieldName.equalsIgnoreCase("document_id") || fieldName.equals("documentId"))
      return documentId;
    else if (fieldName.equalsIgnoreCase("docstatus"))
      return docstatus;
    else if (fieldName.equalsIgnoreCase("doctypetargetid"))
      return doctypetargetid;
    else if (fieldName.equalsIgnoreCase("ourreference"))
      return ourreference;
    else if (fieldName.equalsIgnoreCase("cusreference"))
      return cusreference;
    else if (fieldName.equalsIgnoreCase("salnam"))
      return salnam;
    else if (fieldName.equalsIgnoreCase("bpartner_id") || fieldName.equals("bpartnerId"))
      return bpartnerId;
    else if (fieldName.equalsIgnoreCase("bpartner_name") || fieldName.equals("bpartnerName"))
      return bpartnerName;
    else if (fieldName.equalsIgnoreCase("bpartner_language") || fieldName.equals("bpartnerLanguage"))
      return bpartnerLanguage;
    else if (fieldName.equalsIgnoreCase("issalesordertransaction"))
      return issalesordertransaction;
    else if (fieldName.equalsIgnoreCase("isebill"))
      return isebill;
   else {
     log4j.debug("Field does not exist: " + fieldName);
     return null;
   }
 }

  public static ReportData[] dummy(ConnectionProvider connectionProvider, String cOrderId)    throws ServletException {
    return dummy(connectionProvider, cOrderId, 0, 0);
  }

  public static ReportData[] dummy(ConnectionProvider connectionProvider, String cOrderId, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "			select" +
      "				'' as ad_Org_Id," +
      "				'' as document_id," +
      "				'' as docstatus," +
      "				'' as docTypeTargetId," +
      "				'' as ourreference," +
      "				'' as cusreference," +
      "				'' as salnam," +
      "				'' as bpartner_id," +
      "				'' as bpartner_name," +
      "				'' as bpartner_language," +
      "				'' as isSalesOrderTransaction" +
      "                         '' as isebill" +
      "			from" +
      "				c_order" +
      "			where" +
      "				1=1";
    strSql = strSql + ((cOrderId==null || cOrderId.equals(""))?"":"  			 and c_order.c_order_id in  		" + cOrderId);

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    try {
    st = connectionProvider.getPreparedStatement(strSql);
      if (cOrderId != null && !(cOrderId.equals(""))) {
        }

      result = st.executeQuery();
      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while(countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }
      while(continueResult && result.next()) {
        countRecord++;
        ReportData objectReportData = new ReportData();
        objectReportData.adOrgId = UtilSql.getValue(result, "ad_org_id");
        objectReportData.documentId = UtilSql.getValue(result, "document_id");
        objectReportData.docstatus = UtilSql.getValue(result, "docstatus");
        objectReportData.doctypetargetid = UtilSql.getValue(result, "doctypetargetid");
        objectReportData.ourreference = UtilSql.getValue(result, "ourreference");
        objectReportData.cusreference = UtilSql.getValue(result, "cusreference");
        objectReportData.salnam = UtilSql.getValue(result, "salnam");
        objectReportData.bpartnerId = UtilSql.getValue(result, "bpartner_id");
        objectReportData.bpartnerName = UtilSql.getValue(result, "bpartner_name");
        objectReportData.bpartnerLanguage = UtilSql.getValue(result, "bpartner_language");
        objectReportData.issalesordertransaction = UtilSql.getValue(result, "issalesordertransaction");
        objectReportData.isebill = UtilSql.getValue(result, "isebill");
        objectReportData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectReportData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
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
    ReportData objectReportData[] = new ReportData[vector.size()];
    vector.copyInto(objectReportData);
    return(objectReportData);
  }

  public static ReportData[] getOrderInfo(ConnectionProvider connectionProvider, String cOrderId)    throws ServletException {
    return getOrderInfo(connectionProvider, cOrderId, 0, 0);
  }

  public static ReportData[] getOrderInfo(ConnectionProvider connectionProvider, String cOrderId, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "			select" +
      "				c_order.ad_org_id," +
      "				c_order.c_order_id as document_id," +
      "				c_order.issotrx as isSalesOrderTransaction," +
      "				c_order.docstatus," +
      "				c_order.c_doctypetarget_id as docTypeTargetId," +
      "				c_order.documentno as ourreference," +
      "				c_order.poreference as cusreference," +
      "				ad_user.name as salnam," +
      "				c_order.c_bpartner_id as bpartner_id," +
      "				c_bpartner.name as bpartner_name," +
      "				c_bpartner.ad_language as bpartner_language" +
      "                         'N' as isebill" +
      "			from" +
      "				c_order left join c_doctype on c_order.c_doctypetarget_id = c_doctype.c_doctype_id" +
      "				left join c_bpartner on c_order.c_bpartner_id = c_bpartner.c_bpartner_id" +
      "				left join ad_user on c_order.salesrep_id = ad_user.ad_user_id" +
      "			where" +
      "				c_order.c_order_id = ?";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, cOrderId);

      result = st.executeQuery();
      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while(countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }
      while(continueResult && result.next()) {
        countRecord++;
        ReportData objectReportData = new ReportData();
        objectReportData.adOrgId = UtilSql.getValue(result, "ad_org_id");
        objectReportData.documentId = UtilSql.getValue(result, "document_id");
        objectReportData.issalesordertransaction = UtilSql.getValue(result, "issalesordertransaction");
        objectReportData.docstatus = UtilSql.getValue(result, "docstatus");
        objectReportData.doctypetargetid = UtilSql.getValue(result, "doctypetargetid");
        objectReportData.ourreference = UtilSql.getValue(result, "ourreference");
        objectReportData.cusreference = UtilSql.getValue(result, "cusreference");
        objectReportData.salnam = UtilSql.getValue(result, "salnam");
        objectReportData.bpartnerId = UtilSql.getValue(result, "bpartner_id");
        objectReportData.bpartnerName = UtilSql.getValue(result, "bpartner_name");
        objectReportData.bpartnerLanguage = UtilSql.getValue(result, "bpartner_language");
        objectReportData.isebill = UtilSql.getValue(result, "isebill");
        objectReportData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectReportData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
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
    ReportData objectReportData[] = new ReportData[vector.size()];
    vector.copyInto(objectReportData);
    return(objectReportData);
  }

  public static ReportData[] getInvoiceInfo(ConnectionProvider connectionProvider, String cInvoiceId)    throws ServletException {
    return getInvoiceInfo(connectionProvider, cInvoiceId, 0, 0);
  }

  public static ReportData[] getInvoiceInfo(ConnectionProvider connectionProvider, String cInvoiceId, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "			select" +
      "				c_invoice.ad_org_id," +
      "				c_invoice.c_invoice_id as document_id," +
      "				c_invoice.docstatus," +
      "				c_invoice.c_doctypetarget_id as docTypeTargetId," +
      "				c_invoice.documentno as ourreference," +
      "				c_invoice.poreference as cusreference," +
      "				ad_user.name as salnam," +
      "				c_invoice.c_bpartner_id as bpartner_id," +
      "				c_invoice.issotrx as isSalesOrderTransaction," +
      "				c_bpartner.name as bpartner_name," +
      "				c_bpartner.ad_language as bpartner_language" +
      "                COALESCE(em_bill_isebill,'N') as isebill" +
      "			from" +
      "				c_invoice left join c_doctype on c_invoice.c_doctypetarget_id = c_doctype.c_doctype_id" +
      "				left join c_bpartner on c_invoice.c_bpartner_id = c_bpartner.c_bpartner_id" +
      "				left join ad_user on c_invoice.salesrep_id = ad_user.ad_user_id" +
      "			where" +
      "				c_invoice.c_invoice_id = ?";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, cInvoiceId);

      result = st.executeQuery();
      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while(countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }
      while(continueResult && result.next()) {
        countRecord++;
        ReportData objectReportData = new ReportData();
        objectReportData.adOrgId = UtilSql.getValue(result, "ad_org_id");
        objectReportData.documentId = UtilSql.getValue(result, "document_id");
        objectReportData.docstatus = UtilSql.getValue(result, "docstatus");
        objectReportData.doctypetargetid = UtilSql.getValue(result, "doctypetargetid");
        objectReportData.ourreference = UtilSql.getValue(result, "ourreference");
        objectReportData.cusreference = UtilSql.getValue(result, "cusreference");
        objectReportData.salnam = UtilSql.getValue(result, "salnam");
        objectReportData.bpartnerId = UtilSql.getValue(result, "bpartner_id");
        objectReportData.issalesordertransaction = UtilSql.getValue(result, "issalesordertransaction");
        objectReportData.bpartnerName = UtilSql.getValue(result, "bpartner_name");
        objectReportData.bpartnerLanguage = UtilSql.getValue(result, "bpartner_language");
        objectReportData.isebill = UtilSql.getValue(result, "isebill");
        objectReportData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectReportData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
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
    ReportData objectReportData[] = new ReportData[vector.size()];
    vector.copyInto(objectReportData);
    return(objectReportData);
  }

  public static ReportData[] getShipmentInfo(ConnectionProvider connectionProvider, String mInoutId)    throws ServletException {
    return getShipmentInfo(connectionProvider, mInoutId, 0, 0);
  }

  public static ReportData[] getShipmentInfo(ConnectionProvider connectionProvider, String mInoutId, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "			select" +
      "				m_inout.ad_org_id," +
      "				m_inout.m_inout_id as document_id," +
      "				m_inout.docstatus," +
      "				m_inout.c_doctype_id as docTypeTargetId," +
      "				m_inout.documentno as ourreference," +
      "				m_inout.poreference as cusreference," +
      "				ad_user.name as salnam," +
      "				m_inout.c_bpartner_id as bpartner_id," +
      "				m_inout.issotrx as isSalesOrderTransaction," +
      "				c_bpartner.name as bpartner_name," +
      "				c_bpartner.ad_language as bpartner_language" +
      "                'N' as isebill" +
      "			from" +
      "				m_inout left join c_doctype on m_inout.c_doctype_id = c_doctype.c_doctype_id" +
      "				left join c_bpartner on m_inout.c_bpartner_id = c_bpartner.c_bpartner_id" +
      "				left join ad_user on m_inout.salesrep_id = ad_user.ad_user_id" +
      "			where" +
      "				m_inout.m_inout_id = ?";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, mInoutId);

      result = st.executeQuery();
      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while(countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }
      while(continueResult && result.next()) {
        countRecord++;
        ReportData objectReportData = new ReportData();
        objectReportData.adOrgId = UtilSql.getValue(result, "ad_org_id");
        objectReportData.documentId = UtilSql.getValue(result, "document_id");
        objectReportData.docstatus = UtilSql.getValue(result, "docstatus");
        objectReportData.doctypetargetid = UtilSql.getValue(result, "doctypetargetid");
        objectReportData.ourreference = UtilSql.getValue(result, "ourreference");
        objectReportData.cusreference = UtilSql.getValue(result, "cusreference");
        objectReportData.salnam = UtilSql.getValue(result, "salnam");
        objectReportData.bpartnerId = UtilSql.getValue(result, "bpartner_id");
        objectReportData.issalesordertransaction = UtilSql.getValue(result, "issalesordertransaction");
        objectReportData.bpartnerName = UtilSql.getValue(result, "bpartner_name");
        objectReportData.bpartnerLanguage = UtilSql.getValue(result, "bpartner_language");
        objectReportData.isebill = UtilSql.getValue(result, "isebill");
        objectReportData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectReportData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
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
    ReportData objectReportData[] = new ReportData[vector.size()];
    vector.copyInto(objectReportData);
    return(objectReportData);
  }

  public static ReportData[] getPaymentInfo(ConnectionProvider connectionProvider, String FinPaymentId)    throws ServletException {
    return getPaymentInfo(connectionProvider, FinPaymentId, 0, 0);
  }

  public static ReportData[] getPaymentInfo(ConnectionProvider connectionProvider, String FinPaymentId, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "            select" +
      "                fin_payment.ad_org_id," +
      "                fin_payment.fin_payment_id as document_id," +
      "                fin_payment.status as docstatus," +
      "                fin_payment.c_doctype_id as docTypeTargetId," +
      "                fin_payment.documentno as ourreference," +
      "                fin_payment.referenceno as cusreference," +
      "                fin_payment.c_bpartner_id as bpartner_id," +
      "                fin_payment.isreceipt as isSalesOrderTransaction," +
      "                c_bpartner.name as bpartner_name," +
      "                c_bpartner.ad_language as bpartner_language" +
      "                'N' as isebill" +
      "            from" +
      "                fin_payment left join c_doctype on fin_payment.c_doctype_id = c_doctype.c_doctype_id" +
      "                left join c_bpartner on fin_payment.c_bpartner_id = c_bpartner.c_bpartner_id" +
      "            where" +
      "                fin_payment.fin_payment_id = ?";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, FinPaymentId);

      result = st.executeQuery();
      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while(countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }
      while(continueResult && result.next()) {
        countRecord++;
        ReportData objectReportData = new ReportData();
        objectReportData.adOrgId = UtilSql.getValue(result, "ad_org_id");
        objectReportData.documentId = UtilSql.getValue(result, "document_id");
        objectReportData.docstatus = UtilSql.getValue(result, "docstatus");
        objectReportData.doctypetargetid = UtilSql.getValue(result, "doctypetargetid");
        objectReportData.ourreference = UtilSql.getValue(result, "ourreference");
        objectReportData.cusreference = UtilSql.getValue(result, "cusreference");
        objectReportData.bpartnerId = UtilSql.getValue(result, "bpartner_id");
        objectReportData.issalesordertransaction = UtilSql.getValue(result, "issalesordertransaction");
        objectReportData.bpartnerName = UtilSql.getValue(result, "bpartner_name");
        objectReportData.bpartnerLanguage = UtilSql.getValue(result, "bpartner_language");
        objectReportData.isebill = UtilSql.getValue(result, "isebill");
        objectReportData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectReportData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
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
    ReportData objectReportData[] = new ReportData[vector.size()];
    vector.copyInto(objectReportData);
    return(objectReportData);
  }

   /**
   * by DW-NET
   */
  public static ReportData[] getRequisitionInfo(ConnectionProvider connectionProvider,
      String mRequisitionId) throws ServletException {
    return getRequisitionInfo(connectionProvider, mRequisitionId, 0, 0);
  }

  /**
   * by DW-NET
   */
  public static ReportData[] getRequisitionInfo(ConnectionProvider connectionProvider,
      String mRequisitionId, int firstRegister, int numberRegisters) throws ServletException {
    String strSql = "";
    strSql = strSql
        + "			select"
        + "				m_requisition.ad_org_id,"
        + "				m_requisition.m_requisition_id as document_id,"
        + "				m_requisition.docstatus,"
        + "				m_requisition.em_pr_doctypetarget as docTypeTargetId,"
        + "				m_requisition.documentno as ourreference,"
        + "				'' as cusreference,"
        + "				m_requisition.c_bpartner_id as bpartner_id,"
        + "				c_bpartner.ad_language as bpartner_language"
        + "                'N' as isebill"
        + "			from"
        + "				m_requisition left join c_doctype on m_requisition.em_pr_doctypetarget = c_doctype.c_doctype_id"
        + "				left join c_bpartner on m_requisition.c_bpartner_id = c_bpartner.c_bpartner_id"
        + "			where" + "				m_requisition.m_requisition_id = ?";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, mRequisitionId);

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
        ReportData objectReportData = new ReportData();
        objectReportData.adOrgId = UtilSql.getValue(result, "ad_org_id");
        objectReportData.documentId = UtilSql.getValue(result, "document_id");
        objectReportData.docstatus = UtilSql.getValue(result, "docstatus");
        objectReportData.doctypetargetid = UtilSql.getValue(result, "doctypetargetid");
        objectReportData.ourreference = UtilSql.getValue(result, "ourreference");
        objectReportData.cusreference = UtilSql.getValue(result, "cusreference");
        objectReportData.bpartnerId = UtilSql.getValue(result, "bpartner_id");
        objectReportData.bpartnerLanguage = UtilSql.getValue(result, "bpartner_language");
        objectReportData.isebill = UtilSql.getValue(result, "isebill");
        objectReportData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectReportData);
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
    ReportData objectReportData[] = new ReportData[vector.size()];
    vector.copyInto(objectReportData);
    return (objectReportData);
  }

  /**
   * by DW-NET
   */
  public static ReportData[] getManageRequisitionInfo(ConnectionProvider connectionProvider,
      String mRequisitionId) throws ServletException {
    return getManageRequisitionInfo(connectionProvider, mRequisitionId, 0, 0);
  }

  /**
   * by DW-NET
   */
  public static ReportData[] getManageRequisitionInfo(ConnectionProvider connectionProvider,
      String mRequisitionId, int firstRegister, int numberRegisters) throws ServletException {
    String strSql = "";
    strSql = strSql
        + "			select"
        + "				m_requisition.ad_org_id,"
        + "				m_requisition.m_requisition_id as document_id,"
        + "				m_requisition.docstatus,"
        + "				m_requisition.em_pr_doctypetarget as docTypeTargetId,"
        + "				m_requisition.documentno as ourreference,"
        + "				'' as cusreference,"
        + "				m_requisition.c_bpartner_id as bpartner_id,"
        + "				c_bpartner.ad_language as bpartner_language"
        + "                'N' as isebill"
        + "			from"
        + "				m_requisition left join c_doctype on m_requisition.em_pr_doctypetarget = c_doctype.c_doctype_id"
        + "				left join c_bpartner on m_requisition.c_bpartner_id = c_bpartner.c_bpartner_id"
        + "			where" + "				m_requisition.m_requisition_id = ?";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, mRequisitionId);

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
        ReportData objectReportData = new ReportData();
        objectReportData.adOrgId = UtilSql.getValue(result, "ad_org_id");
        objectReportData.documentId = UtilSql.getValue(result, "document_id");
        objectReportData.docstatus = UtilSql.getValue(result, "docstatus");
        objectReportData.doctypetargetid = UtilSql.getValue(result, "doctypetargetid");
        objectReportData.ourreference = UtilSql.getValue(result, "ourreference");
        objectReportData.cusreference = UtilSql.getValue(result, "cusreference");
        objectReportData.bpartnerId = UtilSql.getValue(result, "bpartner_id");
        objectReportData.bpartnerLanguage = UtilSql.getValue(result, "bpartner_language");
        objectReportData.isebill = UtilSql.getValue(result, "isebill");
        objectReportData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectReportData);
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
    ReportData objectReportData[] = new ReportData[vector.size()];
    vector.copyInto(objectReportData);
    return (objectReportData);
  }
}
