package pe.com.unifiedgo.migration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.ad_actionbutton.DeleteTransaction;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.dao.TransactionsDao;
import org.openbravo.advpaymentmngt.process.FIN_AddPayment;
import org.openbravo.advpaymentmngt.process.FIN_AddPaymentFromJournalLine;
import org.openbravo.advpaymentmngt.process.FIN_TransactionProcess;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.materialmgmt.InventoryCountProcess;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.assetmgmt.Asset;
import org.openbravo.model.financialmgmt.gl.GLBatch;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.gl.GLJournal;
import org.openbravo.model.financialmgmt.gl.GLJournalLine;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.materialmgmt.transaction.InventoryCount;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.CallStoredProcedure;

public class SMG_Utils {

  public static void processGLJournalBatch(VariablesSecureApp vars, ConnectionProvider conn,
      String GLJournalBatchId) throws Exception {
    try {

      GLBatch batch = OBDal.getInstance().get(GLBatch.class, GLJournalBatchId);
      boolean success = false;
      // Process the Batch
      for (GLJournal journal : batch.getFinancialMgmtGLJournalList()) {
        success = false;
        if (!journal.isProcessed()) {
          // Recover again the object to avoid problems with Dal
          journal = OBDal.getInstance().get(GLJournal.class, journal.getId());

          try {
            int cont = 0;
            final String journalId = journal.getId();
            String docAction = "CO";
            journal.setDocumentAction(docAction);
            OBDal.getInstance().flush();
            OBDal.getInstance().refresh(journal);

            // If complete check for account number: if 33XXXX or 34XXXX
            if (docAction.equals("CO")) {
              boolean needs_asset = false;
              for (GLJournalLine journalLine : journal.getFinancialMgmtGLJournalLineList()) {
                String account_number = journalLine.getAccountingCombination().getAccount()
                    .getSearchKey();
                if (account_number.substring(0, 2).equals("33")
                    || account_number.substring(0, 2).equals("34")) {
                  needs_asset = true;
                  break;
                }
              }

              if (needs_asset) {
                Asset asset = journal.getAsset();
                if (asset == null) {
                  throw new OBException("@SCO_JournalCONeedsAsset@");
                }
              }

            }

            // Check if the Lines of the Journal have related Payments. In that case
            // the Payments must be deleted before Closing or Reactivating the line.
            String relatedPayments = "";
            if (!"CO".equals(docAction)) {
              for (GLJournalLine journalLine : journal.getFinancialMgmtGLJournalLineList()) {
                if (journalLine.getRelatedPayment() != null) {
                  relatedPayments = relatedPayments + journalLine.getLineNo() + ", ";
                }
              }
            }
            if (!"".equals(relatedPayments)) {
              relatedPayments = relatedPayments.substring(0, relatedPayments.length() - 2);
              throw new OBException("@FIN_JournalLineRelatedPayments@: " + relatedPayments);
            }

            try {
              // Call GL_Journal_Post method from the database.
              final List<Object> parameters = new ArrayList<Object>();
              parameters.add(null);
              parameters.add(journalId);
              final String procedureName = "gl_journal_post";
              CallStoredProcedure mm = CallStoredProcedure.getInstance();
              mm.call(procedureName, parameters, null, false, false);
            } catch (Exception e) {
              OBDal.getInstance().rollbackAndClose();
              OBError error = OBMessageUtils.translateError(conn, vars, vars.getLanguage(),
                  e.getCause().getMessage());
              throw new OBException(error.getMessage());
            }

            OBDal.getInstance().refresh(journal);
            Date date = journal.getDocumentDate();

            // Complete the Journal
            if ("CO".equals(docAction)) {
              for (GLJournalLine journalLine : journal.getFinancialMgmtGLJournalLineList()) {
                // Recover again the object to avoid problems with Dal
                journalLine = OBDal.getInstance().get(GLJournalLine.class, journalLine.getId());
                if (journalLine.isOpenItems() && journalLine.getRelatedPayment() == null) {
                  ProcessBundle pb = new ProcessBundle("DE1B382FDD2540199D223586F6E216D0", vars)
                      .init(conn);
                  HashMap<String, Object> parameters = new HashMap<String, Object>();
                  parameters.put("GL_JournalLine_ID", journalLine.getId());
                  pb.setParams(parameters);
                  OBError myMessage = null;
                  // Create a Payment for the Journal line
                  FIN_AddPaymentFromJournalLine myProcess = new FIN_AddPaymentFromJournalLine();
                  myProcess.setDoCommit(false);
                  myProcess.execute(pb);
                  myMessage = (OBError) pb.getResult();

                  if (myMessage.getType().equals("Error")) {
                    throw new OBException("@FIN_PaymentFromJournalError@ " + journalLine.getLineNo()
                        + " - " + myMessage.getMessage());
                  }
                  cont++;
                }
              }
            }

            success = true;
            OBDal.getInstance().commitAndClose();
          } catch (final OBException e) {
            success = false;
            OBDal.getInstance().rollbackAndClose();
          }

          if (!success) {
            throw new Exception("ERRORPROCESSGLJOURNALBATCH");
          }
        }
      }

    } catch (Exception e) {
      throw new Exception("ERRORPROCESSGLJOURNALBATCH");
    }

  }

  public static void addGLItemTransaction(VariablesSecureApp vars, ConnectionProvider conn,
      FIN_FinancialAccount account, GLItem glitem, BigDecimal glitemamt, Date transactionDate,
      String descripcion, boolean isReceipt) throws Exception {
    AdvPaymentMngtDao dao = new AdvPaymentMngtDao();
    String strMessage = "";
    OBContext.setAdminMode();
    try {

      BigDecimal glItemDepositAmt = new BigDecimal(0);
      BigDecimal glItemPaymentAmt = new BigDecimal(0);
      if (isReceipt) {
        glItemDepositAmt = glitemamt;
      } else {
        glItemPaymentAmt = glitemamt;
      }

      String description = descripcion;// Utility.messageBD(conn, "APRM_GLItem", vars.getLanguage())
                                       // + ": " + glitem.getName();
      FIN_FinaccTransaction finTrans = dao.getNewFinancialTransaction(account.getOrganization(),
          account, TransactionsDao.getTransactionMaxLineNo(account) + 10, null, description,
          transactionDate, glitem, isReceipt ? "RDNC" : "PWNC", glItemDepositAmt, glItemPaymentAmt,
          null, null, null, isReceipt ? "BPD" : "BPW", transactionDate, null, null, null, null,
          null, null, null, null, null, null, null, null);

      ProcessBundle pb = new ProcessBundle("F68F2890E96D4D85A1DEF0274D105BCE", vars).init(conn);
      HashMap<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("action", "P");
      parameters.put("Fin_FinAcc_Transaction_ID", finTrans.getId());
      pb.setParams(parameters);
      OBError processTransactionError = null;
      new FIN_TransactionProcess().execute(pb);

      processTransactionError = (OBError) pb.getResult();

      OBDal.getInstance().refresh(finTrans);
      finTrans.setStatus("RPPC");
      OBDal.getInstance().save(finTrans);
      OBDal.getInstance().flush();

      if (processTransactionError != null && "Error".equals(processTransactionError.getType())) {
        throw new OBException(processTransactionError.getMessage());
      }

    } catch (Exception e) {
      e.printStackTrace();
      throw new Exception("ERRORPROCESSTRANSACTION");
    } finally {
      OBDal.getInstance().getSession().clear();
      OBContext.restorePreviousMode();
    }
  }

  public static void revertPayment(VariablesSecureApp vars, ConnectionProvider conn,
      FIN_Payment payment) throws Exception {
    OBError message = FIN_AddPayment.processPayment(vars, conn, "R", payment);
    if ("Error".equals(message.getType())) {
      throw new Exception(message.getMessage());
    }
  }

  public static void deleteTransaction(VariablesSecureApp vars, ConnectionProvider conn,
      FIN_FinaccTransaction trx) throws Exception {
    ProcessBundle pb = new ProcessBundle("FF8080812F348A97012F349DC24F0007", vars).init(conn);
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("Fin_Finacc_Transaction_ID", trx.getId());
    pb.setParams(parameters);
    OBError deleteTransactionError = null;
    new DeleteTransaction().execute(pb);

    deleteTransactionError = (OBError) pb.getResult();

    if ("Error".equals(deleteTransactionError.getType())) {
      throw new Exception(deleteTransactionError.getMessage());
    }

  }

  private static void mergeInvoices(List<Invoice> lsInvoices, List<BigDecimal> lsAmt) {
    boolean changed = true;
    while (changed) {
      changed = false;
      for (int i = 0; i < lsInvoices.size(); i++) {
        for (int j = i + 1; j < lsInvoices.size(); j++) {
          if (lsInvoices.get(i).getId().equals(lsInvoices.get(j).getId())) {
            lsAmt.set(i, lsAmt.get(i).add(lsAmt.get(j)));
            lsInvoices.remove(j);
            lsAmt.remove(j);
            changed = true;
            break;
          }
        }
        if (changed)
          break;
      }
    }
  }

  public static FIN_Payment execSimplePaymentMultiple(VariablesSecureApp vars,
      ConnectionProvider conn, FIN_Payment payment, List<Invoice> invoices, List<BigDecimal> amts,
      BigDecimal totalamt, boolean isReceipt, Currency paymentcurrency, BigDecimal exchangeRate,
      BigDecimal totalconvertedAmount) throws Exception {
    return execSimplePaymentMultiple(vars, conn, payment, invoices, amts, totalamt, isReceipt,
        paymentcurrency, exchangeRate, totalconvertedAmount, "P");

  }

  // New bicurrency payment, antes los amts estaban en la moneda del invoice pero ahora los amts
  // deben estar en la moneda del banco del payment.
  public static FIN_Payment execSimplePaymentMultiple(VariablesSecureApp vars,
      ConnectionProvider conn, FIN_Payment payment, List<Invoice> invoices, List<BigDecimal> amts,
      BigDecimal totalamt, boolean isReceipt, Currency paymentcurrency, BigDecimal exchangeRate,
      BigDecimal totalconvertedAmount, String strAction) throws Exception {

    mergeInvoices(invoices, amts);

    List<FIN_PaymentScheduleDetail> selectedPaymentDetails = new ArrayList<FIN_PaymentScheduleDetail>();
    HashMap<String, BigDecimal> selectedPaymentDetailAmounts = new HashMap<String, BigDecimal>();
    for (int i = 0; i < invoices.size(); i++) {
      Invoice invoice = invoices.get(i);
      BigDecimal amt = amts.get(i);

      FIN_PaymentScheduleDetail psd = SMG_Utils.getFirstUnpaidPSD(invoice);
      if (psd == null) {
        throw new Exception("NOPSD");
      }

      selectedPaymentDetails.add(psd);
      selectedPaymentDetailAmounts.put(psd.getId(), amt);
    }

    DecimalFormat df = new DecimalFormat("0.00");
    df.setRoundingMode(RoundingMode.HALF_UP);
    String strPaymentAmount = df.format(totalamt);

    OBContext.setAdminMode();

    payment.setAmount(totalamt);
    OBDal.getInstance().save(payment);
    payment.getFINPaymentDetailList();

    FIN_AddPayment.setFinancialTransactionAmountAndRate(null, payment, exchangeRate,
        totalconvertedAmount);

    payment = FIN_AddPayment.savePayment(null, payment, isReceipt, null, null, null, null, null,
        strPaymentAmount, null, null, null, selectedPaymentDetails, selectedPaymentDetailAmounts,
        false, false, paymentcurrency, exchangeRate, totalconvertedAmount);
    boolean issimpleprovision = false;
    if (!isReceipt) {
      issimpleprovision = payment.isSCOIsSimpleProvision();
      payment.setSCOIsSimpleProvision(true);
    }
    OBDal.getInstance().save(payment);
    OBDal.getInstance().flush();
    OBDal.getInstance().refresh(payment);
    try {
      OBError message = FIN_AddPayment.processPayment(vars, conn, strAction, payment);
      if (message.getType().compareTo("Error") == 0) {
        throw new Exception(message.getMessage());
      }
      payment.setSCOIsSimpleProvision(issimpleprovision);
      OBDal.getInstance().flush();
    } finally {
      OBDal.getInstance().getSession().clear();
      OBContext.restorePreviousMode();
    }

    return payment;
  }

  public static FIN_Payment execPaymentMultiple(VariablesSecureApp vars, ConnectionProvider conn,
      FIN_Payment payment, List<Invoice> invoices, List<BigDecimal> amts, List<GLItem> glitems,
      List<BigDecimal> glitemamts, BigDecimal totalamt, boolean isReceipt, Currency paymentcurrency,
      BigDecimal exchangeRate, BigDecimal totalconvertedAmount) throws Exception {

    mergeInvoices(invoices, amts);

    List<FIN_PaymentScheduleDetail> selectedPaymentDetails = new ArrayList<FIN_PaymentScheduleDetail>();
    HashMap<String, BigDecimal> selectedPaymentDetailAmounts = new HashMap<String, BigDecimal>();
    for (int i = 0; i < invoices.size(); i++) {
      Invoice invoice = invoices.get(i);
      BigDecimal amt = amts.get(i);

      FIN_PaymentScheduleDetail psd = SMG_Utils.getFirstUnpaidPSD(invoice);
      if (psd == null) {
        System.out.println(invoice.getScrPhysicalDocumentno() + " " + invoice.getId() + " " + amt);
        throw new Exception("NOPSD");
      }

      selectedPaymentDetails.add(psd);
      selectedPaymentDetailAmounts.put(psd.getId(), amt);
    }

    DecimalFormat df = new DecimalFormat("0.00");
    df.setRoundingMode(RoundingMode.HALF_UP);
    String strPaymentAmount = df.format(totalamt);

    OBContext.setAdminMode();

    payment.setAmount(totalamt);
    OBDal.getInstance().save(payment);
    payment.getFINPaymentDetailList();

    for (int i = 0; i < glitems.size(); i++) {
      GLItem glitem = glitems.get(i);
      BigDecimal glitemamt = glitemamts.get(i);

      FIN_AddPayment.saveGLItem(payment, glitemamt, glitem, null, null, null, null, null, null,
          null, null, null, null, null);

    }

    FIN_AddPayment.setFinancialTransactionAmountAndRate(null, payment, exchangeRate,
        totalconvertedAmount);

    payment = FIN_AddPayment.savePayment(null, payment, isReceipt, null, null, null, null, null,
        strPaymentAmount, null, null, null, selectedPaymentDetails, selectedPaymentDetailAmounts,
        false, false, paymentcurrency, exchangeRate, totalconvertedAmount);

    boolean issimpleprovision = false;
    if (!isReceipt) {
      issimpleprovision = payment.isSCOIsSimpleProvision();
      payment.setSCOIsSimpleProvision(true);
    }

    try {

      OBDal.getInstance().save(payment);
      OBDal.getInstance().refresh(payment);

      OBError message = FIN_AddPayment.processPayment(vars, conn, "P", payment);
      OBDal.getInstance().flush();

      payment.setSCOIsSimpleProvision(issimpleprovision);

      OBDal.getInstance().flush();
    } catch (Exception e) {
      e.printStackTrace();
      throw new Exception("ERRORPROCESSPAYMENT");
    } finally {

      OBDal.getInstance().getSession().clear();
      OBContext.restorePreviousMode();
    }

    return payment;
  }

  public static FIN_Payment execSimplePayment(VariablesSecureApp vars, ConnectionProvider conn,
      FIN_Payment payment, Invoice invoice, BigDecimal amt, boolean isReceipt,
      Currency paymentcurrency, BigDecimal exchangeRate, BigDecimal convertedAmount)
      throws Exception {

    FIN_PaymentScheduleDetail psd = SMG_Utils.getFirstUnpaidPSD(invoice);
    if (psd == null) {
      throw new Exception("NOPSD");
    }

    List<FIN_PaymentScheduleDetail> selectedPaymentDetails = new ArrayList<FIN_PaymentScheduleDetail>();
    selectedPaymentDetails.add(psd);

    HashMap<String, BigDecimal> selectedPaymentDetailAmounts = new HashMap<String, BigDecimal>();
    selectedPaymentDetailAmounts.put(psd.getId(), amt);

    DecimalFormat df = new DecimalFormat("0.00");
    df.setRoundingMode(RoundingMode.HALF_UP);
    String strPaymentAmount = df.format(amt);

    OBContext.setAdminMode();

    payment.setAmount(amt);
    OBDal.getInstance().save(payment);
    payment.getFINPaymentDetailList();

    FIN_AddPayment.setFinancialTransactionAmountAndRate(null, payment, exchangeRate,
        convertedAmount);

    payment = FIN_AddPayment.savePayment(null, payment, isReceipt, null, null, null, null, null,
        strPaymentAmount, null, null, null, selectedPaymentDetails, selectedPaymentDetailAmounts,
        false, false, paymentcurrency, exchangeRate, convertedAmount);
    boolean issimpleprovision = false;
    if (!isReceipt) {
      issimpleprovision = payment.isSCOIsSimpleProvision();
      payment.setSCOIsSimpleProvision(true);
    }

    OBDal.getInstance().save(payment);
    OBDal.getInstance().flush();
    OBDal.getInstance().refresh(payment);

    payment.setSCOIsSimpleProvision(issimpleprovision);
    OBDal.getInstance().flush();

    try {
      OBError message = FIN_AddPayment.processPayment(vars, conn, "P", payment);
    } catch (Exception e) {
      throw new Exception("ERRORPROCESSPAYMENT");
    }

    OBContext.restorePreviousMode();

    return payment;
  }

  public static FIN_PaymentScheduleDetail getFirstUnpaidPSD(Invoice invoice) {

    OBCriteria<FIN_PaymentSchedule> obc = OBDal.getInstance()
        .createCriteria(FIN_PaymentSchedule.class);
    obc.setFilterOnReadableOrganization(false);
    obc.setFilterOnReadableClients(false);
    obc.add(Restrictions.eq(FIN_PaymentSchedule.PROPERTY_CLIENT, invoice.getClient()));
    obc.add(Restrictions.eq(FIN_PaymentSchedule.PROPERTY_ORGANIZATION, invoice.getOrganization()));
    obc.add(Restrictions.eq(FIN_PaymentSchedule.PROPERTY_INVOICE, invoice));
    obc.add(Restrictions.ne(FIN_PaymentSchedule.PROPERTY_OUTSTANDINGAMOUNT, new BigDecimal(0)));
    obc.addOrderBy(FIN_PaymentSchedule.PROPERTY_DUEDATE, true);

    List<FIN_PaymentSchedule> ps_array = obc.list();
    if (ps_array.size() == 0)
      return null;

    FIN_PaymentSchedule ps = ps_array.get(0);

    OBCriteria<FIN_PaymentScheduleDetail> obcd = OBDal.getInstance()
        .createCriteria(FIN_PaymentScheduleDetail.class);
    obcd.setFilterOnReadableOrganization(false);
    obcd.setFilterOnReadableClients(false);
    obcd.add(Restrictions.eq(FIN_PaymentScheduleDetail.PROPERTY_CLIENT, ps.getClient()));
    obcd.add(
        Restrictions.eq(FIN_PaymentScheduleDetail.PROPERTY_ORGANIZATION, ps.getOrganization()));
    obcd.add(Restrictions.eq(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE, ps));
    obcd.add(Restrictions.eq(FIN_PaymentScheduleDetail.PROPERTY_SCOEXTERNALPAYMENT, false));
    obcd.add(Restrictions.isNull(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS));
    obcd.addOrderBy(FIN_PaymentScheduleDetail.PROPERTY_AMOUNT, false);

    List<FIN_PaymentScheduleDetail> psd_array = obcd.list();
    if (psd_array.size() == 0)
      return null;

    return psd_array.get(0);
  }

  public static void processPhysicalInventory(VariablesSecureApp vars, ConnectionProvider conn,
      String mInventoryId) throws Exception {
    ProcessBundle pb = new ProcessBundle("107", vars).init(conn);
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("M_Inventory_ID", mInventoryId);
    pb.setParams(parameters);
    OBError myMessage = null;
    new InventoryCountProcess().execute(pb);
    myMessage = (OBError) pb.getResult();
    if (myMessage == null) {
      throw new Exception("ERRORINVENTORYCOUNTPROCESS");
    } else if (myMessage.getType().equals("Error")) {
      throw new Exception("ERRORINVENTORYCOUNTPROCESS");
    }

  }

  public static void processInventoryCount(VariablesSecureApp vars, ConnectionProvider conn,
      InventoryCount inventory) throws Exception {
    OBContext.setAdminMode();
    try {
      ProcessBundle pb = new ProcessBundle("107", vars).init(conn);
      HashMap<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("M_Inventory_ID", inventory.getId());
      pb.setParams(parameters);
      OBError processInventoryCount = null;
      System.out.println("Inventario:" + inventory.getWarehouse().getName());
      new InventoryCountProcess().execute(pb);

      processInventoryCount = (OBError) pb.getResult();

      OBDal.getInstance().refresh(inventory);

      if (processInventoryCount != null && "Error".equals(processInventoryCount.getType())) {
        throw new OBException(processInventoryCount.getMessage());
      }

    } catch (Exception e) {
      e.printStackTrace();
      throw new Exception("ERRORPROCESSINVENTORYCOUNT");
    } finally {
      // OBDal.getInstance().getSession().clear();
      OBContext.restorePreviousMode();
    }
  }
}
