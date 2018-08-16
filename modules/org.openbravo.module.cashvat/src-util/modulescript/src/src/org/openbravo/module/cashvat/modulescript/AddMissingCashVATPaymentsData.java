//Sqlc generated V1.O00-1
package org.openbravo.module.cashvat.modulescript;

import java.sql.*;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;

import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.data.UtilSql;
import java.util.*;

class AddMissingCashVATPaymentsData implements FieldProvider {
static Logger log4j = Logger.getLogger(AddMissingCashVATPaymentsData.class);
  private String InitRecordNumber="0";
  public String cInvoiceId;
  public String ispaid;
  public String cInvoicetaxId;
  public String finPaymentDetailId;
  public String stdprecision;
  public String percentage;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("c_invoice_id") || fieldName.equals("cInvoiceId"))
      return cInvoiceId;
    else if (fieldName.equalsIgnoreCase("ispaid"))
      return ispaid;
    else if (fieldName.equalsIgnoreCase("c_invoicetax_id") || fieldName.equals("cInvoicetaxId"))
      return cInvoicetaxId;
    else if (fieldName.equalsIgnoreCase("fin_payment_detail_id") || fieldName.equals("finPaymentDetailId"))
      return finPaymentDetailId;
    else if (fieldName.equalsIgnoreCase("stdprecision"))
      return stdprecision;
    else if (fieldName.equalsIgnoreCase("percentage"))
      return percentage;
   else {
     log4j.debug("Field does not exist: " + fieldName);
     return null;
   }
 }

/**
fin_payment_detail_id paying a cash vat invoice but not yet into c_invoicetax_cashvat table
 */
  public static AddMissingCashVATPaymentsData[] selectMissedPaymentDetails(ConnectionProvider connectionProvider)    throws ServletException {
    return selectMissedPaymentDetails(connectionProvider, 0, 0);
  }

/**
fin_payment_detail_id paying a cash vat invoice but not yet into c_invoicetax_cashvat table
 */
  public static AddMissingCashVATPaymentsData[] selectMissedPaymentDetails(ConnectionProvider connectionProvider, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "	select i.c_invoice_id, i.ispaid, it.c_invoiceTax_id, fpsd.fin_payment_detail_id, c.stdprecision, round((fpd.amount + fpd.writeoffamt) * 100 / i.grandtotal, c.stdPrecision) as percentage" +
      "	from fin_payment_detail fpd" +
      "	inner join fin_payment_scheduledetail fpsd on (fpsd.fin_payment_detail_id = fpd.fin_payment_detail_id)" +
      "	inner join fin_payment_schedule fps on (fps.fin_payment_schedule_id = fpsd.fin_payment_schedule_invoice)" +
      "	inner join fin_payment fp on (fp.fin_payment_id = fpd.fin_payment_id)" +
      "	inner join c_invoice i on (fps.c_invoice_id = i.c_invoice_id)" +
      "	inner join c_currency c on (i.c_currency_id = c.c_currency_id)" +
      "	inner join c_invoicetax it on (it.c_invoice_id = i.c_invoice_id)" +
      "	inner join c_tax t on (t.c_tax_id = it.c_tax_id)" +
      "	where i.iscashvat = 'Y'" +
      "	and t.iscashvat = 'Y'" +
      "	and fp.c_currency_id = i.c_currency_id" +
      "	and not exists (select 1 from c_invoicetax_cashvat itcv where itcv.fin_payment_detail_id = fpsd.fin_payment_detail_id and it.c_invoicetax_id = itcv.c_invoicetax_id)" +
      "	and fp.status in ('RPR', 'PPM', 'PWNC', 'RDNC', 'RPPC', 'REM_CANCEL')" +
      "	order by i.c_invoice_id, c_invoicetax_id, fpd.created asc";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    try {
    st = connectionProvider.getPreparedStatement(strSql);

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
        AddMissingCashVATPaymentsData objectAddMissingCashVATPaymentsData = new AddMissingCashVATPaymentsData();
        objectAddMissingCashVATPaymentsData.cInvoiceId = UtilSql.getValue(result, "c_invoice_id");
        objectAddMissingCashVATPaymentsData.ispaid = UtilSql.getValue(result, "ispaid");
        objectAddMissingCashVATPaymentsData.cInvoicetaxId = UtilSql.getValue(result, "c_invoicetax_id");
        objectAddMissingCashVATPaymentsData.finPaymentDetailId = UtilSql.getValue(result, "fin_payment_detail_id");
        objectAddMissingCashVATPaymentsData.stdprecision = UtilSql.getValue(result, "stdprecision");
        objectAddMissingCashVATPaymentsData.percentage = UtilSql.getValue(result, "percentage");
        objectAddMissingCashVATPaymentsData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectAddMissingCashVATPaymentsData);
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
    AddMissingCashVATPaymentsData objectAddMissingCashVATPaymentsData[] = new AddMissingCashVATPaymentsData[vector.size()];
    vector.copyInto(objectAddMissingCashVATPaymentsData);
    return(objectAddMissingCashVATPaymentsData);
  }

/**
last fin_payment_detail_id paying a cash vat invoice
 */
  public static AddMissingCashVATPaymentsData[] selectLastPaymentDetail(ConnectionProvider connectionProvider, String invoiceId)    throws ServletException {
    return selectLastPaymentDetail(connectionProvider, invoiceId, 0, 0);
  }

/**
last fin_payment_detail_id paying a cash vat invoice
 */
  public static AddMissingCashVATPaymentsData[] selectLastPaymentDetail(ConnectionProvider connectionProvider, String invoiceId, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "	select fpsd.fin_payment_detail_id" +
      "	from fin_payment_detail fpd" +
      "	inner join fin_payment_scheduledetail fpsd on (fpsd.fin_payment_detail_id = fpd.fin_payment_detail_id)" +
      "	inner join fin_payment_schedule fps on (fps.fin_payment_schedule_id = fpsd.fin_payment_schedule_invoice)" +
      "	inner join fin_payment fp on (fp.fin_payment_id = fpd.fin_payment_id)" +
      "	inner join c_invoice i on (fps.c_invoice_id = i.c_invoice_id)" +
      "	inner join c_invoicetax it on (it.c_invoice_id = i.c_invoice_id)" +
      "	inner join c_tax t on (t.c_tax_id = it.c_tax_id)" +
      "	where i.iscashvat = 'Y'" +
      "	and t.iscashvat = 'Y'" +
      "	and not exists (select 1 from c_invoicetax_cashvat itcv where itcv.fin_payment_detail_id = fpsd.fin_payment_detail_id)" +
      "	and fp.status in ('RPR', 'PPM', 'PWNC', 'RDNC', 'RPPC', 'REM_CANCEL')" +
      "	and i.c_invoice_id = ?" +
      "	order by fpd.created desc";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, invoiceId);

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
        AddMissingCashVATPaymentsData objectAddMissingCashVATPaymentsData = new AddMissingCashVATPaymentsData();
        objectAddMissingCashVATPaymentsData.finPaymentDetailId = UtilSql.getValue(result, "fin_payment_detail_id");
        objectAddMissingCashVATPaymentsData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectAddMissingCashVATPaymentsData);
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
    AddMissingCashVATPaymentsData objectAddMissingCashVATPaymentsData[] = new AddMissingCashVATPaymentsData[vector.size()];
    vector.copyInto(objectAddMissingCashVATPaymentsData);
    return(objectAddMissingCashVATPaymentsData);
  }

/**
Delete Cash VAT Info without payment detail. We will populate later on
 */
  public static int deleteCashVATInfoWithoutPaymentDetail(ConnectionProvider connectionProvider)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "        delete from c_invoicetax_cashvat itcv" +
      "	where itcv.fin_payment_detail_id is null";

    int updateCount = 0;
    PreparedStatement st = null;

    try {
    st = connectionProvider.getPreparedStatement(strSql);

      updateCount = st.executeUpdate();
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
    return(updateCount);
  }

/**
Insert Cash VAT Info for non last payment
 */
  public static int insertCashVATInfoNonLastPayment(ConnectionProvider connectionProvider, String invoiceTaxId, String percentage, String stdPrecision, String finPaymentDetailId)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "        INSERT INTO c_invoicetax_cashvat(" +
      "            c_invoicetax_cashvat_id, ad_client_id, ad_org_id, isactive, created, " +
      "            createdby, updated, updatedby, c_invoicetax_id, percentage, taxamt, " +
      "            taxbaseamt, fin_payment_detail_id)" +
      "	VALUES (get_uuid(), (select ad_client_id from c_invoicetax where c_invoicetax_id = ?), (select ad_org_id from c_invoicetax where c_invoicetax_id = ?), 'Y', now(), " +
      "            '100', now(), '100', ?,  to_number(?), (select round(taxamt * to_number(?) / 100, to_number(?)) from c_invoicetax it where c_invoicetax_id = ?), " +
      "            (select round(taxbaseamt * to_number(?) / 100, to_number(?)) from c_invoicetax where c_invoicetax_id = ?), ?)";

    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, invoiceTaxId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, invoiceTaxId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, invoiceTaxId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, percentage);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, percentage);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, stdPrecision);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, invoiceTaxId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, percentage);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, stdPrecision);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, invoiceTaxId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, finPaymentDetailId);

      updateCount = st.executeUpdate();
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
    return(updateCount);
  }

/**
Insert Cash VAT Info for last payment
 */
  public static int insertCashVATInfoLastPayment(ConnectionProvider connectionProvider, String invoiceTaxId, String finPaymentDetailId)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "        INSERT INTO c_invoicetax_cashvat(" +
      "            c_invoicetax_cashvat_id, ad_client_id, ad_org_id, isactive, created, " +
      "            createdby, updated, updatedby, c_invoicetax_id, percentage," +
      "	    taxamt, " +
      "            taxbaseamt, " +
      "	    fin_payment_detail_id)" +
      "	VALUES (get_uuid(), (select ad_client_id from c_invoicetax where c_invoicetax_id = ?), (select ad_org_id from c_invoicetax where c_invoicetax_id = ?), 'Y', now(), " +
      "            '100', now(), '100', ?, (select 100-coalesce(sum(percentage), 0) from c_invoicetax_cashvat where c_invoicetax_id = ?), " +
      "            (select max(it.taxamt) - coalesce(sum(itcv.taxamt), 0) from c_invoicetax it left join c_invoicetax_cashvat itcv on (it.c_invoicetax_id = itcv.c_invoicetax_id)" +
      "                left join fin_payment_detail fpd on (fpd.fin_payment_detail_id = itcv.fin_payment_detail_id) " +
      "		left join fin_payment fp on (fp.fin_payment_id = fpd.fin_payment_id and fp.status in ('RPR', 'PPM', 'PWNC', 'RDNC', 'RPPC', 'REM_CANCEL'))" +
      "		where it.c_invoicetax_id = ? ), " +
      "            (select max(it.taxbaseamt) - coalesce(sum(itcv.taxbaseamt), 0) from c_invoicetax it left join c_invoicetax_cashvat itcv on (it.c_invoicetax_id = itcv.c_invoicetax_id)" +
      "                left join fin_payment_detail fpd on (fpd.fin_payment_detail_id = itcv.fin_payment_detail_id) " +
      "		left join fin_payment fp on (fp.fin_payment_id = fpd.fin_payment_id and fp.status in ('RPR', 'PPM', 'PWNC', 'RDNC', 'RPPC', 'REM_CANCEL'))" +
      "		where it.c_invoicetax_id = ? )," +
      "	    ?)";

    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, invoiceTaxId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, invoiceTaxId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, invoiceTaxId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, invoiceTaxId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, invoiceTaxId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, invoiceTaxId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, finPaymentDetailId);

      updateCount = st.executeUpdate();
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
    return(updateCount);
  }
}
