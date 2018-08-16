/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2010-2014 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.advpaymentmngt.process;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.APRM_FinaccTransactionV;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.dao.TransactionsDao;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.currency.ConversionRateDoc;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DbUtility;

import pe.com.unifiedgo.accounting.data.SCOFixedcashReposition;

public class FIN_TransactionProcess implements org.openbravo.scheduling.Process {
  private static AdvPaymentMngtDao dao;

  public void execute(ProcessBundle bundle) throws Exception {
    dao = new AdvPaymentMngtDao();
    OBError msg = new OBError();
    msg.setType("Success");
    msg.setTitle(Utility.messageBD(bundle.getConnection(), "Success", bundle.getContext().getLanguage()));
    final VariablesSecureApp vars = bundle.getContext().toVars();
    final ConnectionProvider conProvider = bundle.getConnection();

    try {
      // retrieve custom params
      final String strAction = (String) bundle.getParams().get("action");

      // retrieve standard params
      final String recordID = (String) bundle.getParams().get("Fin_FinAcc_Transaction_ID");
      final FIN_FinaccTransaction transaction = dao.getObject(FIN_FinaccTransaction.class, recordID);
      final String language = bundle.getContext().getLanguage();

      OBContext.setAdminMode();
      try {
        if (strAction.equals("P")) {

          // ***********************
          // Process Transaction
          // ***********************
          boolean orgLegalWithAccounting = FIN_Utility.periodControlOpened(transaction.TABLE_NAME, transaction.getId(), transaction.TABLE_NAME + "_ID", "LE");
          if (!FIN_Utility.isPeriodOpen(transaction.getClient().getId(), AcctServer.DOCTYPE_FinAccTransaction, transaction.getOrganization().getId(), OBDateUtils.formatDate(transaction.getDateAcct())) && orgLegalWithAccounting) {
            msg.setType("Error");
            msg.setTitle(Utility.messageBD(conProvider, "Error", language));
            msg.setMessage(Utility.parseTranslation(conProvider, vars, language, "@PeriodNotAvailable@"));
            bundle.setResult(msg);
            OBDal.getInstance().rollbackAndClose();
            return;
          }

          DateFormat justDay = new SimpleDateFormat("yyyyMMdd");
          Date thisMorningMidnight = justDay.parse(justDay.format(new Date()));
          Date transactionDateOnly = justDay.parse(justDay.format(transaction.getTransactionDate()));

          /*
           * if (thisMorningMidnight.compareTo(transactionDateOnly) < 0) { msg.setType("Error");
           * msg.setTitle(Utility.messageBD(conProvider, "Error", language));
           * msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
           * "@SCO_InvalidTrxDate@")); bundle.setResult(msg);
           * OBDal.getInstance().rollbackAndClose(); return; }
           */

          final FIN_FinancialAccount financialAccount = transaction.getAccount();
          financialAccount.setCurrentBalance(financialAccount.getCurrentBalance().add(transaction.getDepositAmount().subtract(transaction.getPaymentAmount())));
          transaction.setProcessed(true);
          FIN_Payment payment = transaction.getFinPayment();
          if (payment != null) {

            if (transaction.getBusinessPartner() == null) {
              transaction.setBusinessPartner(payment.getBusinessPartner());
            }

            String trxnumber = null;
            String strtrxnumber = payment.getReferenceNo();
            if (strtrxnumber != null)
              trxnumber = strtrxnumber.replaceAll("\\s+", "");
            if (trxnumber != null && trxnumber.isEmpty())
              trxnumber = null;

            transaction.setScoTrxnumber(trxnumber);
            payment.setStatus(payment.isReceipt() ? "RDNC" : "PWNC");
            transaction.setStatus(payment.isReceipt() ? "RDNC" : "PWNC");
            OBDal.getInstance().save(payment);
            if (transaction.getDescription() == null || "".equals(transaction.getDescription())) {
              if (payment.getDescription() != null && !payment.getDescription().equals("")) {
                transaction.setDescription(payment.getDescription());
              }
            }
            Boolean invoicePaidold = false;
            for (FIN_PaymentDetail pd : payment.getFINPaymentDetailList()) {

              for (FIN_PaymentScheduleDetail psd : pd.getFINPaymentScheduleDetailList()) {

                invoicePaidold = psd.isInvoicePaid();
                if (!invoicePaidold) {

                  if ((FIN_Utility.invoicePaymentStatus(payment).equals(payment.getStatus()))) {
                    psd.setInvoicePaid(true);
                  }
                  if (psd.isInvoicePaid()) {
                    FIN_Utility.updatePaymentAmounts(pd,psd);
                    FIN_Utility.updateBusinessPartnerCredit(payment);
                  }
                  OBDal.getInstance().save(psd);
                }
              }
            }

          } else {
            transaction.setStatus(transaction.getDepositAmount().compareTo(transaction.getPaymentAmount()) > 0 ? "RDNC" : "PWNC");
          }
          if (transaction.getForeignCurrency() != null && !transaction.getCurrency().equals(transaction.getForeignCurrency()) && getConversionRateDocument(transaction).size() == 0) {
            insertConversionRateDocument(transaction);
          }

          FIN_Payment p = transaction.getFinPayment();
          if (p != null) {

            // check details for fixed cash reposition
            List<FIN_PaymentDetail> pds = p.getFINPaymentDetailList();
            for (int i = 0; i < pds.size(); i++) {
              FIN_PaymentDetail pd = pds.get(i);
              List<SCOFixedcashReposition> fixedcashreps = pd.getSCOFixedcashRepositionList();
              for (int j = 0; j < fixedcashreps.size(); j++) {
                // got a fixedcashreposition , create a mirror transaction with glitem
                SCOFixedcashReposition fixedcashrep = fixedcashreps.get(j);

                FIN_FinancialAccount account = fixedcashrep.getFinancialAccount();

                if (!fixedcashrep.getDocumentStatus().equals("CO") || account == null) {
                  OBDal.getInstance().rollbackAndClose();
                  msg.setType("Error");
                  msg.setTitle(Utility.messageBD(conProvider, "Error", language));
                  msg.setMessage(Utility.parseTranslation(conProvider, vars, language, "@SCO_InvalidFixedCashRep@"));
                  bundle.setResult(msg);
                  return;
                }

                final Organization organization = transaction.getOrganization();

                BigDecimal glItemPaymentAmt = new BigDecimal(0);
                BigDecimal glItemDepositAmt = pd.getAmount();

                GLItem glItem = organization.getScoFcashrepGlitem();
                String gldescription = Utility.messageBD(conProvider, "APRM_GLItem", vars.getLanguage()) + ": " + glItem.getName();
                boolean isReceipt = (glItemDepositAmt.compareTo(glItemPaymentAmt) >= 0);

                // Currency, Organization, paymentDate,
                FIN_FinaccTransaction glfinTrans = dao.getNewFinancialTransaction(organization, account, TransactionsDao.getTransactionMaxLineNo(account) + 10, null, gldescription, transaction.getTransactionDate(), glItem, isReceipt ? "RDNC" : "PWNC", glItemDepositAmt, glItemPaymentAmt, null, null, null, isReceipt ? "BPD" : "BPW", transaction.getTransactionDate(), null, null, null, null, null, null, null, null, null, null, null,null);
                glfinTrans.setScoFcashrepFinacctrx(transaction);
                OBError glprocessTransactionError = FIN_TransactionProcess.processTransaction(vars, conProvider, "P", glfinTrans);
                if (glprocessTransactionError != null && "Error".equals(glprocessTransactionError.getType())) {
                  OBDal.getInstance().rollbackAndClose();
                  msg.setType("Error");
                  msg.setTitle(Utility.messageBD(conProvider, "Error", language));
                  msg.setMessage(Utility.parseTranslation(conProvider, vars, language, "@SCO_FixedCashRepGLError@" + ": " + glprocessTransactionError.getMessage()));
                  bundle.setResult(msg);
                  return;
                }

              }
            }
          }

          OBDal.getInstance().save(financialAccount);
          OBDal.getInstance().save(transaction);
          OBDal.getInstance().flush();
          bundle.setResult(msg);

        } else if (strAction.equals("R")) {

          // check if the transaction has related transfer to account
          List<FIN_FinaccTransaction> transfertoacc_trxs = transaction.getFINFinaccTransactionEMScoTrtoaccFinacctrxIDList();
          if (transfertoacc_trxs.size() != 0) {
            OBDal.getInstance().rollbackAndClose();
            msg.setType("Error");
            msg.setTitle(Utility.messageBD(conProvider, "Error", language));
            msg.setMessage(Utility.parseTranslation(conProvider, vars, language, "@SCO_DeleteTrxHasTransferToAccTrx@"));
            bundle.setResult(msg);
            return;
          }

          // check if the transaction is a uident deposit
          List<FIN_Payment> uipayments = transaction.getFINPaymentEMScoUidenTrxIDList();
          if (uipayments.size() != 0) {
            OBDal.getInstance().rollbackAndClose();
            msg.setType("Error");
            msg.setTitle(Utility.messageBD(conProvider, "Error", language));
            msg.setMessage(Utility.parseTranslation(conProvider, vars, language, "@SCO_DeleteTrxHasRelatedUIPayment@"));
            bundle.setResult(msg);
            return;
          }

          FIN_FinaccTransaction relatedfixedcashrep_trx = transaction.getScoFcashrepFinacctrx();
          if (relatedfixedcashrep_trx != null) {
            // cannot delete a linked transaction
            OBDal.getInstance().rollbackAndClose();
            msg.setType("Error");
            msg.setTitle(Utility.messageBD(conProvider, "Error", language));
            msg.setMessage(Utility.parseTranslation(conProvider, vars, language, "@SCO_LinkedFixedCashRep@"));
            bundle.setResult(msg);
            return;
          }

          // check if the transaction is a cash transfer
          FIN_FinaccTransaction trxt = transaction.getScoCtransFinacctrx();
          if (trxt != null) {
            OBDal.getInstance().rollbackAndClose();
            msg.setType("Error");
            msg.setTitle(Utility.messageBD(conProvider, "Error", language));
            msg.setMessage(Utility.parseTranslation(conProvider, vars, language, "@SCO_DeleteCashTTrxHasTrx@"));
            bundle.setResult(msg);
            return;
          }

          // check if the transaction has related cash transfer
          List<FIN_FinaccTransaction> ctransfer_trxs = transaction.getFINFinaccTransactionEMScoCtransFinacctrxIDList();
          if (ctransfer_trxs.size() != 0) {
            OBDal.getInstance().rollbackAndClose();
            msg.setType("Error");
            msg.setTitle(Utility.messageBD(conProvider, "Error", language));
            msg.setMessage(Utility.parseTranslation(conProvider, vars, language, "@SCO_DeleteCashTTrxHasCashTTrx@"));
            bundle.setResult(msg);
            return;
          }

          // Check for a automaticaly created transfer to acc trx
          FIN_FinaccTransaction finTransT = transaction.getScoTrtoaccFinacctrx();
          if (finTransT != null) {
            transaction.setScoTrtoaccFinacctrx(null);
            OBDal.getInstance().save(transaction);
            OBDal.getInstance().flush();
            try {
              OBError msg1 = FIN_TransactionProcess.processTransaction(vars, bundle.getConnection(), "R", finTransT);
              if ("Success".equals(msg1.getType())) {
                OBContext.setAdminMode();
                try {
                  OBDal.getInstance().remove(finTransT);
                  OBDal.getInstance().flush();

                } catch (Exception e) {
                  OBDal.getInstance().rollbackAndClose();
                  msg.setType("Error");
                  msg.setTitle(Utility.messageBD(conProvider, "Error", language));
                  msg.setMessage(Utility.parseTranslation(conProvider, vars, language, "@SCO_ErrorDeletingLinkedTransferToAccTrx@"));
                  bundle.setResult(msg);
                  return;
                } finally {
                  OBContext.restorePreviousMode();
                }
              } else {
                OBDal.getInstance().rollbackAndClose();
                bundle.setResult(msg1);
                return;
              }
            } catch (Exception e) {
              OBDal.getInstance().rollbackAndClose();
              msg.setType("Error");
              msg.setTitle(Utility.messageBD(conProvider, "Error", language));
              msg.setMessage(Utility.parseTranslation(conProvider, vars, language, "@SCO_ErrorDeletingLinkedTransferToAccTrx@"));
              bundle.setResult(msg);
              return;
            }

          }

          // Check for a automaticaly created fixedcash trx
          List<FIN_FinaccTransaction> fixedcashrep_finacctrxs = transaction.getFINFinaccTransactionEMScoFcashrepFinacctrxIDList();
          for (int i = 0; i < fixedcashrep_finacctrxs.size(); i++) {
            FIN_FinaccTransaction fixedchasrep_finacctrx = fixedcashrep_finacctrxs.get(i);
            fixedchasrep_finacctrx.setScoFcashrepFinacctrx(null);
            OBDal.getInstance().save(fixedchasrep_finacctrx);
            OBDal.getInstance().flush();
            try {
              OBError msg1 = FIN_TransactionProcess.processTransaction(vars, bundle.getConnection(), "R", fixedchasrep_finacctrx);
              if ("Success".equals(msg1.getType())) {
                OBContext.setAdminMode();
                try {
                  OBDal.getInstance().remove(fixedchasrep_finacctrx);
                  OBDal.getInstance().flush();

                } catch (Exception e) {
                  OBDal.getInstance().rollbackAndClose();
                  msg.setType("Error");
                  msg.setTitle(Utility.messageBD(conProvider, "Error", language));
                  msg.setMessage(Utility.parseTranslation(conProvider, vars, language, "@SCO_ErrorDeletingLinkedFixedCashRep@"));
                  bundle.setResult(msg);
                  return;
                } finally {
                  OBContext.restorePreviousMode();
                }
              } else {
                OBDal.getInstance().rollbackAndClose();
                bundle.setResult(msg1);
                return;
              }
            } catch (Exception e) {
              OBDal.getInstance().rollbackAndClose();
              msg.setType("Error");
              msg.setTitle(Utility.messageBD(conProvider, "Error", language));
              msg.setMessage(Utility.parseTranslation(conProvider, vars, language, "@SCO_ErrorDeletingLinkedFixedCashRep@"));
              bundle.setResult(msg);
              return;
            }
          }

          fixedcashrep_finacctrxs.clear();
          OBDal.getInstance().flush();
          OBDal.getInstance().refresh(transaction);

          // ***********************
          // Reactivate Transaction
          // ***********************
          // Already Posted Document
          if ("Y".equals(transaction.getPosted())) {
            OBDal.getInstance().rollbackAndClose();
            msg.setType("Error");
            msg.setTitle(Utility.messageBD(conProvider, "Error", language));
            msg.setMessage(Utility.parseTranslation(conProvider, vars, language, "@PostedDocument@" + ": " + transaction.getIdentifier()));
            bundle.setResult(msg);
            return;
          }
          // Already Reconciled
          if (transaction.getReconciliation() != null || "RPPC".equals(transaction.getStatus())) {
            OBDal.getInstance().rollbackAndClose();
            msg.setType("Error");
            msg.setTitle(Utility.messageBD(conProvider, "Error", language));
            msg.setMessage(Utility.parseTranslation(conProvider, vars, language, "@APRM_ReconciledDocument@" + ": " + transaction.getIdentifier()));
            bundle.setResult(msg);
            return;
          }

          // Remove conversion rate at document level for the given transaction -- UGO FIX: do not
          // use criteria, use the getlist function instead to avoid cascade problems
          OBContext.setAdminMode();
          try {

            List<ConversionRateDoc> convrdlist = transaction.getCurrencyConversionRateDocList();
            for (int i = 0; i < convrdlist.size(); i++) {
              OBDal.getInstance().remove(convrdlist.get(i));
            }
            convrdlist.clear();

            /*
             * OBCriteria<ConversionRateDoc> obc =
             * OBDal.getInstance().createCriteria(ConversionRateDoc.class);
             * obc.add(Restrictions.eq(ConversionRateDoc.PROPERTY_FINANCIALACCOUNTTRANSACTION,
             * transaction)); obc.add(Restrictions.isNull(ConversionRateDoc.PROPERTY_PAYMENT)); for
             * (ConversionRateDoc conversionRateDoc : obc.list()) {
             * OBDal.getInstance().remove(conversionRateDoc); }
             */

            OBDal.getInstance().flush();
          } finally {
            OBContext.restorePreviousMode();
          }

          transaction.setProcessed(false);
          final FIN_FinancialAccount financialAccount = transaction.getAccount();
          financialAccount.setCurrentBalance(financialAccount.getCurrentBalance().subtract(transaction.getDepositAmount()).add(transaction.getPaymentAmount()));
          OBDal.getInstance().save(financialAccount);
          OBDal.getInstance().save(transaction);
          OBDal.getInstance().flush();

          FIN_Payment payment = transaction.getFinPayment();
          if (payment != null) {
            Boolean invoicePaidold = false;
            for (FIN_PaymentDetail pd : payment.getFINPaymentDetailList()) {
              for (FIN_PaymentScheduleDetail psd : pd.getFINPaymentScheduleDetailList()) {
                invoicePaidold = psd.isInvoicePaid();
                if (invoicePaidold) {
                  boolean restore = (FIN_Utility.seqnumberpaymentstatus(payment.getStatus())) == (FIN_Utility.seqnumberpaymentstatus(FIN_Utility.invoicePaymentStatus(payment)));
                  if (restore) {
                    FIN_Utility.restorePaidAmounts(pd, psd);
                  }
                }
              }
            }
            payment.setStatus(payment.isReceipt() ? "RPR" : "PPM");
            transaction.setStatus(payment.isReceipt() ? "RPR" : "PPM");
            OBDal.getInstance().save(payment);
          } else {
            transaction.setStatus(transaction.getDepositAmount().compareTo(transaction.getPaymentAmount()) > 0 ? "RPR" : "PPM");
          }
          OBDal.getInstance().save(transaction);
          OBDal.getInstance().flush();

          bundle.setResult(msg);

        }
        bundle.setResult(msg);
      } finally {
        OBContext.restorePreviousMode();
      }
    } catch (final Exception e) {

      OBDal.getInstance().rollbackAndClose();
      Throwable exx = DbUtility.getUnderlyingSQLException(e);
      String message = OBMessageUtils.translateError(exx.getMessage()).getMessage();
      if (message.contains("@")) {
        message = OBMessageUtils.parseTranslation(message);
      }

      message = Utility.messageBD(conProvider, message, vars.getLanguage());
      // remove mysql message
      int pos = message.toLowerCase().indexOf("where: sql statement");
      if (pos != -1) {
        message = message.substring(0, pos);
      }

      e.printStackTrace(System.err);
      msg.setType("Error");
      msg.setTitle(Utility.messageBD(bundle.getConnection(), "Error", bundle.getContext().getLanguage()));
      msg.setMessage(message);
      bundle.setResult(msg);
    }
  }

  private List<ConversionRateDoc> getConversionRateDocument(FIN_FinaccTransaction transaction) {
    OBContext.setAdminMode();
    try {
      OBCriteria<ConversionRateDoc> obc = OBDal.getInstance().createCriteria(ConversionRateDoc.class);
      obc.add(Restrictions.eq(ConversionRateDoc.PROPERTY_CURRENCY, transaction.getForeignCurrency()));
      obc.add(Restrictions.eq(ConversionRateDoc.PROPERTY_TOCURRENCY, transaction.getCurrency()));
      obc.add(Restrictions.eq(ConversionRateDoc.PROPERTY_FINANCIALACCOUNTTRANSACTION, transaction));
      return obc.list();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private ConversionRateDoc insertConversionRateDocument(FIN_FinaccTransaction transaction) {
    OBContext.setAdminMode();
    try {
      ConversionRateDoc newConversionRateDoc = OBProvider.getInstance().get(ConversionRateDoc.class);
      newConversionRateDoc.setOrganization(transaction.getOrganization());
      newConversionRateDoc.setCurrency(transaction.getCurrency());
      newConversionRateDoc.setToCurrency(transaction.getForeignCurrency());
      newConversionRateDoc.setRate(transaction.getForeignConversionRate());
      newConversionRateDoc.setForeignAmount(transaction.getForeignAmount());
      newConversionRateDoc.setFinancialAccountTransaction(OBDal.getInstance().get(APRM_FinaccTransactionV.class, transaction.getId()));
      OBDal.getInstance().save(newConversionRateDoc);
      OBDal.getInstance().flush();
      return newConversionRateDoc;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  static private OBError processTransaction(VariablesSecureApp vars, ConnectionProvider conn, String strAction, FIN_FinaccTransaction transaction) throws Exception {
    ProcessBundle pb = new ProcessBundle("F68F2890E96D4D85A1DEF0274D105BCE", vars).init(conn);
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("action", strAction);
    parameters.put("Fin_FinAcc_Transaction_ID", transaction.getId());
    pb.setParams(parameters);
    OBError myMessage = null;
    new FIN_TransactionProcess().execute(pb);
    myMessage = (OBError) pb.getResult();
    return myMessage;
  }

}
