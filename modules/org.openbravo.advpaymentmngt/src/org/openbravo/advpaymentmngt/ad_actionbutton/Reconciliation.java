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

package org.openbravo.advpaymentmngt.ad_actionbutton;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.time.DateUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.APRM_FinaccTransactionV;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.dao.TransactionsDao;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.financialmgmt.payment.FIN_Reconciliation;
import org.openbravo.model.financialmgmt.payment.FIN_ReconciliationLine_v;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.accounting.data.SCOFixedcashReposition;

public class Reconciliation extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private AdvPaymentMngtDao dao;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strOrgId = vars.getRequestGlobalVariable("inpadOrgId", "Reconciliation|Org");
      String strWindowId = vars.getRequestGlobalVariable("inpwindowId", "Reconciliation|windowId");
      String strTabId = vars.getRequestGlobalVariable("inpTabId", "Reconciliation|tabId");
      String strFinancialAccountId = vars.getStringParameter("inpfinFinancialAccountId");
      final int accesslevel = 3;

      if ((org.openbravo.erpCommon.utility.WindowAccessData.hasReadOnlyAccess(this, vars.getRole(), strTabId)) || !(Utility.isElementInList(Utility.getContext(this, vars, "#User_Client", strWindowId, accesslevel), vars.getClient()) && Utility.isElementInList(Utility.getContext(this, vars, "#User_Org", strWindowId, accesslevel), strOrgId))) {
        OBError myError = Utility.translateError(this, vars, vars.getLanguage(), Utility.messageBD(this, "NoWriteAccess", vars.getLanguage()));
        vars.setMessage(strTabId, myError);
        printPageClosePopUp(response, vars);
      } else {
        printPage(response, vars, strOrgId, strWindowId, strTabId, strFinancialAccountId, null, null);
      }

    } else if (vars.commandIn("GRIDINIT")) {
      String strFinancialAccountId = vars.getStringParameter("inpFinFinancialAccountId", "");
      String strFinReconciliationId = vars.getStringParameter("inpFinReconciliationId", "");
      String strStatementDate = vars.getStringParameter("inpStatementDate");
      boolean strAfterDate = "Y".equals(vars.getStringParameter("inpAfterDate"));
      String selectedTransactionsIds = vars.getInParameter("inpTransactionId", IsIDFilter.instance);
      String strCurrentlyCleared = vars.getNumericParameter("inpCalcCurrentlyCleared");
      String strTotalPayment = vars.getNumericParameter("inpCalcTotalPayment");
      String strTotalDeposit = vars.getNumericParameter("inpCalcTotalDeposit");

      printGridInit(response, vars, strFinancialAccountId, strStatementDate, strAfterDate, selectedTransactionsIds, strCurrentlyCleared, strTotalPayment, strTotalDeposit, strFinReconciliationId);

    } else if (vars.commandIn("GRID")) {
      String strFinancialAccountId = vars.getStringParameter("inpFinFinancialAccountId", "");
      String strFinReconciliationId = vars.getStringParameter("inpFinReconciliationId", "");
      String strStatementDate = vars.getStringParameter("inpStatementDate");
      boolean strAfterDate = "Y".equals(vars.getStringParameter("inpAfterDate"));
      String selectedTransactionsIds = vars.getInParameter("inpTransactionId", IsIDFilter.instance);
      String strCurrentlyCleared = vars.getNumericParameter("inpCalcCurrentlyCleared");
      String strTotalPayment = vars.getNumericParameter("inpCalcTotalPayment");
      String strTotalDeposit = vars.getNumericParameter("inpCalcTotalDeposit");

      printGrid(response, vars, strFinancialAccountId, strStatementDate, strAfterDate, selectedTransactionsIds, strCurrentlyCleared, strTotalPayment, strTotalDeposit, strFinReconciliationId);

    } else if (vars.commandIn("SAVE") || vars.commandIn("PROCESS")) {
      String strTabId = vars.getGlobalVariable("inpTabId", "Reconciliation|tabId");
      String strFinancialAccountId = vars.getStringParameter("inpFinFinancialAccountId", "");
      String strFinReconciliationId = vars.getStringParameter("inpFinReconciliationId", "");
      String strDifference = vars.getNumericParameter("inpCalcDifference");
      String strStatementDate = vars.getStringParameter("inpStatementDate");
      String strBeginBalance = vars.getNumericParameter("inpBeginBalance");
      String strEndBalance = vars.getNumericParameter("inpEndBalance");
      String selectedTransactionsIds = vars.getInParameter("inpTransactionId", IsIDFilter.instance);
      boolean process = vars.commandIn("PROCESS");
      processReconciliation(response, vars, strTabId, strFinancialAccountId, strDifference, strStatementDate, strBeginBalance, strEndBalance, process, selectedTransactionsIds, strFinReconciliationId);

    } else if (vars.commandIn("UPDATESTATUS")) {
      String strFinancialAccountId = vars.getStringParameter("inpFinFinancialAccountId", "");
      String strSelectedTransId = vars.getStringParameter("inpCurrentTransIdSelected", "");
      String strFinReconciliationId = vars.getStringParameter("inpFinReconciliationId", "");
      boolean isChecked = "true".equals(vars.getStringParameter("inpIsCurrentTransSelected"));

      updateTransactionStatus(response, strFinancialAccountId, strSelectedTransId, isChecked, strFinReconciliationId);
    }

  }

  private void updateTransactionStatus(HttpServletResponse response, String strFinancialAccountId, String strSelectedTransId, boolean isChecked, String strFinReconciliationId) {

    OBContext.setAdminMode();
    try {
      if (strSelectedTransId != "") {
        FIN_FinaccTransaction trans = OBDal.getInstance().get(FIN_FinaccTransaction.class, strSelectedTransId);
        String newStatus = "RPPC";
        if (!isChecked) {
          newStatus = (trans.getPaymentAmount().compareTo(trans.getDepositAmount()) >= 0) ? "PWNC" : "RDNC";
          trans.setReconciliation(null);
          if (trans.getFinPayment() != null) {
            trans.getFinPayment().setStatus((trans.getFinPayment().isReceipt()) ? "RDNC" : "PWNC");
          }
        } else {
          FIN_FinancialAccount account = OBDal.getInstance().get(FIN_FinancialAccount.class, strFinancialAccountId);
          FIN_Reconciliation reconciliation = OBDal.getInstance().get(FIN_Reconciliation.class, strFinReconciliationId);
          trans.setReconciliation(reconciliation);
          if (trans.getFinPayment() != null) {
            trans.getFinPayment().setStatus("RPPC");
          }
        }

        trans.setStatus(newStatus);
        OBDal.getInstance().save(trans);
        OBDal.getInstance().flush();
      }
      response.setContentType("text/html; charset=UTF-8");
      PrintWriter out = response.getWriter();
      out.println("");
      out.close();
    } catch (Exception e) {
      throw new OBException(e);
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  private void processReconciliation(HttpServletResponse response, VariablesSecureApp vars, String strTabId, String strFinancialAccountId, String strDifference, String strStatementDate, String strBeginBalance, String strEndBalance, boolean process, String selectedTransactionsIds, String strFinReconciliationId) throws IOException, ServletException {

    log4j.debug("Output: Process or Save button pressed on Financial Account || Transaction || Reconciliation manual window");

    dao = new AdvPaymentMngtDao();
    OBError msg = new OBError();
    OBContext.setAdminMode();
    try {

      String[] selectedLines = null;

      selectedTransactionsIds = selectedTransactionsIds.replace("(", "");
      selectedTransactionsIds = selectedTransactionsIds.replace(")", "");
      selectedTransactionsIds = selectedTransactionsIds.replace("'", "");
      selectedLines = selectedTransactionsIds.split(",");
      if (selectedLines == null) {
        msg.setType("Error");
        msg.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
        msg.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(), "@SCO_InternalError@"));
        vars.setMessage(strTabId, msg);
        msg = null;
        printPageClosePopUpAndRefreshParent(response, vars);
        return;
      }
      FIN_Reconciliation reconciliation = OBDal.getInstance().get(FIN_Reconciliation.class, strFinReconciliationId);

      // UGO CHANGE: The transactions will not be updated on check input because of performance
      // issues. Instead here we will deselect all trx associated with the reconciliation and then
      // select the checked ones.
      List<FIN_FinaccTransaction> finaccs = reconciliation.getFINFinaccTransactionList();
      for (int i = 0; i < finaccs.size(); i++) {
        FIN_FinaccTransaction trans = finaccs.get(i);
        String newStatus = (trans.getPaymentAmount().compareTo(trans.getDepositAmount()) >= 0) ? "PWNC" : "RDNC";
        trans.setReconciliation(null);
        if (trans.getFinPayment() != null) {
          trans.getFinPayment().setStatus((trans.getFinPayment().isReceipt()) ? "RDNC" : "PWNC");
        }
        trans.setStatus(newStatus);
        OBDal.getInstance().save(trans);
      }
      finaccs.clear();
      OBDal.getInstance().flush();

      for (int i = 0; i < selectedLines.length; i++) {
        String finFinaccTransactionId = selectedLines[i].trim();
        if (finFinaccTransactionId.isEmpty())
          continue;

        FIN_FinaccTransaction trans = OBDal.getInstance().get(FIN_FinaccTransaction.class, finFinaccTransactionId);
        if (trans == null) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
          msg.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(), "@SCO_ReconciliationConcurrentTrxDel@"));
          vars.setMessage(strTabId, msg);
          msg = null;
          printPageClosePopUpAndRefreshParent(response, vars);
          return;
        }
        if (trans.getReconciliation() != null || trans.getStatus().compareTo("RPPC") == 0) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
          msg.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(), "@SCO_ReconciliationConcurrentReconSet@"));
          vars.setMessage(strTabId, msg);
          msg = null;
          printPageClosePopUpAndRefreshParent(response, vars);
          return;
        }

        trans.setReconciliation(reconciliation);
        if (trans.getFinPayment() != null) {
          trans.getFinPayment().setStatus("RPPC");
        }
        trans.setStatus("RPPC");
        OBDal.getInstance().save(trans);
      }
      OBDal.getInstance().flush();

      OBDal.getInstance().refresh(reconciliation);

      FIN_FinancialAccount account = OBDal.getInstance().get(FIN_FinancialAccount.class, strFinancialAccountId);

      FIN_Reconciliation lastProcessedReconciliation = TransactionsDao.getLastReconciliation(account, "Y");

      reconciliation.setEndingBalance(new BigDecimal(strEndBalance));
      reconciliation.setTransactionDate(FIN_Utility.getDateTime(strStatementDate));
      reconciliation.setEndingDate(FIN_Utility.getDateTime(strStatementDate));
      reconciliation.setDocumentStatus("DR");
      reconciliation.setProcessed(false);
      reconciliation.setAPRMProcessReconciliation("P");
      reconciliation.setAprmProcessRec("P");

      if (reconciliation != null) {

        SCOFixedcashReposition fixedcashrep = reconciliation.getScoFixcashRep();
        if (fixedcashrep != null) {
          BigDecimal reconc_balance = reconciliation.getStartingbalance().subtract(reconciliation.getEndingBalance());
          BigDecimal fixecashrep_amt = fixedcashrep.getAmount();

          if (reconc_balance.compareTo(fixecashrep_amt) != 0) {

            OBDal.getInstance().rollbackAndClose();

            msg.setType("Error");
            msg.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
            msg.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(), "@SCO_ReconcFixedCashRepDiffBalance@"));
            vars.setMessage(strTabId, msg);
            msg = null;
            printPageClosePopUpAndRefreshParent(response, vars);
            return;
          }
        }
      }

      OBDal.getInstance().save(reconciliation);
      OBDal.getInstance().flush();

      if (process) { // Validations
        String strMessage = "";
        boolean raiseException = false;
        if (new BigDecimal(strDifference).compareTo(BigDecimal.ZERO) != 0) {
          strMessage = "@APRM_ReconciliationDiscrepancy@" + " " + strDifference;
          raiseException = true;
        }

        Calendar calCurrent = Calendar.getInstance();
        calCurrent.setTime(FIN_Utility.getDateTime(strStatementDate));

        if (lastProcessedReconciliation != null) {
          Calendar calLast = Calendar.getInstance();
          calLast.setTime(lastProcessedReconciliation.getEndingDate());
          if (calCurrent.before(calLast)) {
            strMessage = "@APRM_ReconcileInFutureOrPast@";
            raiseException = true;
          }
        }

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DATE, 1);
        tomorrow.setTime(DateUtils.truncate(tomorrow.getTime(), Calendar.DATE));
        if (calCurrent.after(tomorrow)) {
          strMessage = "@APRM_ReconcileInFutureOrPast@";
          raiseException = true;
        }
        if (raiseException) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
          msg.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(), strMessage));
          vars.setMessage(strTabId, msg);
          msg = null;
          printPageClosePopUpAndRefreshParent(response, vars);
          return;
        }

        boolean orgLegalWithAccounting = FIN_Utility.periodControlOpened(reconciliation.TABLE_NAME, reconciliation.getId(), reconciliation.TABLE_NAME + "_ID", "LE");
        if (!FIN_Utility.isPeriodOpen(reconciliation.getClient().getId(), AcctServer.DOCTYPE_Reconciliation, reconciliation.getOrganization().getId(), strStatementDate) && orgLegalWithAccounting) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
          msg.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(), "@PeriodNotAvailable@"));
          vars.setMessage(strTabId, msg);
          msg = null;
          printPageClosePopUpAndRefreshParent(response, vars);
          return;
        }

        if (orgLegalWithAccounting) {
          String identifier = linesInNotAvailablePeriod(reconciliation.getId());
          if (!identifier.equalsIgnoreCase("")) {
            msg.setType("Error");
            msg.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
            msg.setMessage(String.format(Utility.messageBD(this, "APRM_PeriodNotAvailableClearedItem", vars.getLanguage()), identifier));
            vars.setMessage(strTabId, msg);
            msg = null;
            printPageClosePopUpAndRefreshParent(response, vars);
            return;
          }
        }

        for (APRM_FinaccTransactionV finacctrxv : reconciliation.getAPRMFinaccTransactionVList()) {
          if (reconciliation.getEndingDate().compareTo(finacctrxv.getFinancialAccountTransaction().getTransactionDate()) < 0) {
            FIN_FinaccTransaction trans = finacctrxv.getFinancialAccountTransaction();
            // We set processed to false before changing dates to avoid trigger exception
            boolean posted = "Y".equals(trans.getPosted());
            if (posted) {
              trans.setPosted("N");
              OBDal.getInstance().save(trans);
              OBDal.getInstance().flush();
            }
            trans.setProcessed(false);
            OBDal.getInstance().save(trans);
            OBDal.getInstance().flush();
            trans.setTransactionDate(reconciliation.getEndingDate());
            trans.setDateAcct(reconciliation.getEndingDate());
            OBDal.getInstance().save(trans);
            OBDal.getInstance().flush();
            // We set processed to true afterwards
            trans.setProcessed(true);
            OBDal.getInstance().save(trans);
            OBDal.getInstance().flush();
            if (posted) {
              trans.setPosted("Y");
              OBDal.getInstance().save(trans);
              OBDal.getInstance().flush();
            }
            // Changing dates for accounting entries as well
            TransactionsDao.updateAccountingDate(trans);
          }
          Boolean invoicePaidold = false;
          if (finacctrxv.getPayment() != null) {
            for (FIN_PaymentDetail pd : finacctrxv.getPayment().getFINPaymentDetailList()) {
              for (FIN_PaymentScheduleDetail psd : pd.getFINPaymentScheduleDetailList()) {
                invoicePaidold = psd.isInvoicePaid();
                if (!invoicePaidold) {
                  if ((FIN_Utility.invoicePaymentStatus(finacctrxv.getPayment().getPaymentMethod(), reconciliation.getAccount(), finacctrxv.getPayment().isReceipt()).equals(finacctrxv.getPayment().getStatus()))) {
                    psd.setInvoicePaid(true);
                  }
                  if (psd.isInvoicePaid()) {
                    FIN_Utility.updatePaymentAmounts(pd,psd);
                  }
                }
              }
              FIN_Utility.updateBusinessPartnerCredit(finacctrxv.getPayment());
            }
          }
        }

        reconciliation.setDocumentStatus("CO");
        reconciliation.setProcessed(true);
        reconciliation.setAPRMProcessReconciliation("R");
        reconciliation.setAprmProcessRec("R");
        OBDal.getInstance().save(reconciliation);
        OBDal.getInstance().flush();

      }

      String strMessage = "@APRM_ReconciliationNo@" + ": " + reconciliation.getDocumentNo();
      msg.setType("Success");
      msg.setTitle(Utility.messageBD(this, "Success", vars.getLanguage()));
      msg.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(), strMessage));
      vars.setMessage(strTabId, msg);
      msg = null;
      printPageClosePopUpAndRefreshParent(response, vars);

    } catch (Exception e) {
      throw new OBException(e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strOrgId, String strWindowId, String strTabId, String strFinancialAccountId, String strStatementDate, String strEndBalance) throws IOException, ServletException {

    log4j.debug("Output: Reconcile button pressed on Financial Account || Transaction tab");

    dao = new AdvPaymentMngtDao();
    String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty("dateFormat.java");
    SimpleDateFormat dateFormater = new SimpleDateFormat(dateFormat);

    FIN_Reconciliation currentReconciliation = null;
    OBContext.setAdminMode();
    try {
      FIN_FinancialAccount account = OBDal.getInstance().get(FIN_FinancialAccount.class, strFinancialAccountId);

      FIN_Reconciliation lastProcessedReconciliation = TransactionsDao.getLastReconciliation(account, "Y");
      currentReconciliation = TransactionsDao.getLastReconciliation(account, "N");
      if (isAutomaticReconciliation(currentReconciliation)) {
        OBDal.getInstance().rollbackAndClose();
        OBError message = Utility.translateError(this, vars, vars.getLanguage(), Utility.parseTranslation(this, vars, vars.getLanguage(), "@APRM_ReconciliationMixed@"));
        vars.setMessage(strTabId, message);
        printPageClosePopUp(response, vars, Utility.getTabURL(strTabId, "R", true, getDireccion()));
        return;
      }

      XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/advpaymentmngt/ad_actionbutton/Reconciliation").createXmlDocument();

      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("theme", vars.getTheme());

      xmlDocument.setParameter("dateDisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("mainDate", DateTimeData.today(this));
      xmlDocument.setParameter("windowId", strWindowId);
      xmlDocument.setParameter("tabId", strTabId);
      xmlDocument.setParameter("orgId", strOrgId);
      xmlDocument.setParameter("finFinancialAccountId", strFinancialAccountId);

      BigDecimal currentEndBalance = BigDecimal.ZERO;
      if (vars.commandIn("PROCESS")) {
        xmlDocument.setParameter("statementDate", strStatementDate);
        xmlDocument.setParameter("endBalance", strEndBalance);
        xmlDocument.setParameter("calcEndingBalance", strEndBalance);

      } else {
        String currentStatementDate = DateTimeData.today(this);
        if (currentReconciliation != null) {
          currentStatementDate = dateFormater.format(currentReconciliation.getTransactionDate());
          currentEndBalance = currentReconciliation.getEndingBalance();
        }
        xmlDocument.setParameter("statementDate", currentStatementDate);
        xmlDocument.setParameter("endBalance", currentEndBalance.toString());
        xmlDocument.setParameter("calcEndingBalance", currentEndBalance.toString());
      }

      BigDecimal beginBalance = (lastProcessedReconciliation == null) ? account.getInitialBalance() : lastProcessedReconciliation.getEndingBalance();

      xmlDocument.setParameter("account", account.getName());
      xmlDocument.setParameter("beginBalance", beginBalance.toString());

      // Hidden inputs
      xmlDocument.setParameter("calcBeginningBalance", beginBalance.toString());
      xmlDocument.setParameter("calcTotalPayment", BigDecimal.ZERO.toString());
      xmlDocument.setParameter("calcTotalDeposit", BigDecimal.ZERO.toString());
      xmlDocument.setParameter("calcDifferenceToClear", currentEndBalance.subtract(beginBalance).toString());
      xmlDocument.setParameter("calcCurrentlyCleared", TransactionsDao.getCurrentlyClearedAmt(account.getId()).toString());
      xmlDocument.setParameter("calcDifference", currentEndBalance.subtract(beginBalance.add(TransactionsDao.getCurrentlyClearedAmt(account.getId()))).toString());

      OBContext.setAdminMode();
      try {
        xmlDocument.setParameter("precision", account.getCurrency().getStandardPrecision().toString());

        if (currentReconciliation == null) {
          DocumentType docType = FIN_Utility.getDocumentType(account.getOrganization(), "REC");
          if (docType == null) {
            OBError msg = new OBError();
            String strMessage = "@APRM_DocumentTypeNotFound@";
            msg.setType("Error");
            msg.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
            msg.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(), strMessage));
            vars.setMessage(strTabId, msg);
            msg = null;
            printPageClosePopUpAndRefreshParent(response, vars);
            return;
          }
          String docNumber = FIN_Utility.getDocumentNo(account.getOrganization(), "REC", "DocumentNo_FIN_Reconciliation");

          currentReconciliation = dao.getNewReconciliation(account.getOrganization(), account, docNumber, docType, new Date(), new Date(), beginBalance, BigDecimal.ZERO, "DR");
        }
      } finally {
        OBContext.restorePreviousMode();
      }

      xmlDocument.setParameter("finReconciliationId", currentReconciliation.getId());

      OBError myMessage = vars.getMessage(strWindowId);
      vars.removeMessage(strWindowId);
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }

      response.setContentType("text/html; charset=UTF-8");
      PrintWriter out = response.getWriter();
      out.println(xmlDocument.print());
      out.close();
    } catch (Exception e) {
      throw new OBException(e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private void printGridInit(HttpServletResponse response, VariablesSecureApp vars, String strFinancialAccountId, String strStatmentDate, boolean afterDate, String selectedTransactionsIds, String strCurrentlyCleared, String strTotalPayment, String strTotalDeposit, String strFinReconciliationId) throws IOException, ServletException {

    log4j.debug("Output: Grid on Financial Account || Transaction tab || Reconciliation window");

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/advpaymentmngt/ad_actionbutton/ReconciliationGrid").createXmlDocument();

    FIN_FinancialAccount account = OBDal.getInstance().get(FIN_FinancialAccount.class, strFinancialAccountId);

    FieldProvider[] data = TransactionsDao.getTransactionsFiltered(account, FIN_Utility.getDate(DateTimeData.nDaysAfter(this, strStatmentDate, "1")), afterDate);

    BigDecimal currentlyCleared = new BigDecimal(0);
    BigDecimal totalPayment = new BigDecimal(0);
    BigDecimal totalDeposit = new BigDecimal(0);

    for (FieldProvider fp : data) {
      if (!fp.getField("markSelectedId").isEmpty()) {
        BigDecimal payAmt = new BigDecimal(fp.getField("paymentAmount"));
        BigDecimal depAmt = new BigDecimal(fp.getField("depositAmount"));
        currentlyCleared = currentlyCleared.add(depAmt).subtract(payAmt);
        totalPayment = totalPayment.add(payAmt);
        totalDeposit = totalDeposit.add(depAmt);
      }
    }

    xmlDocument.setParameter("calcTotalPayment", totalPayment.toString());
    xmlDocument.setParameter("caclTotalDeposit", totalDeposit.toString());
    xmlDocument.setParameter("calcCurrentlyCleared", currentlyCleared.toString());
    xmlDocument.setData("structure", data);

    JSONObject msg = new JSONObject();
    final String formatOutput = vars.getSessionValue("#FormatOutput|euroEdition", "#0.00");
    try {
      msg.put("reconciliationlines", xmlDocument.print());
      msg.put("calcTotalPayment", totalPayment);
      msg.put("calcTotalDeposit", totalDeposit);
      msg.put("calcCurrentlyCleared", currentlyCleared);
      msg.put("formatOutput", formatOutput);
    } catch (JSONException e) {
      log4j.error("JSON object error" + msg.toString());
    }
    response.setContentType("application/json; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(msg.toString());
    out.close();
  }

  private void printGrid(HttpServletResponse response, VariablesSecureApp vars, String strFinancialAccountId, String strStatmentDate, boolean afterDate, String selectedTransactionsIds, String strCurrentlyCleared, String strTotalPayment, String strTotalDeposit, String strFinReconciliationId) throws IOException, ServletException {

    log4j.debug("Output: Grid on Financial Account || Transaction tab || Reconciliation window");

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/advpaymentmngt/ad_actionbutton/ReconciliationGrid").createXmlDocument();

    FIN_FinancialAccount account = OBDal.getInstance().get(FIN_FinancialAccount.class, strFinancialAccountId);

    Map<String, String> map = FIN_Utility.getMapFromStringList(selectedTransactionsIds);

    // Returns all trx that should be show but not marked
    FieldProvider[] data = TransactionsDao.getTransactionsFilteredNotMarked(account, FIN_Utility.getDate(DateTimeData.nDaysAfter(this, strStatmentDate, "1")), afterDate);

    BigDecimal currentlyCleared = new BigDecimal(0);
    BigDecimal totalPayment = new BigDecimal(0);
    BigDecimal totalDeposit = new BigDecimal(0);

    // Mark all previous selected
    for (FieldProvider fp : data) {

      if (map.containsKey(fp.getField("transactionId"))) {
        BigDecimal payAmt = new BigDecimal(fp.getField("paymentAmount"));
        BigDecimal depAmt = new BigDecimal(fp.getField("depositAmount"));
        currentlyCleared = currentlyCleared.add(depAmt).subtract(payAmt);
        totalPayment = totalPayment.add(payAmt);
        totalDeposit = totalDeposit.add(depAmt);
        FieldProviderFactory.setField(fp, "markSelectedId", fp.getField("transactionId"));
      }
    }

    xmlDocument.setParameter("calcTotalPayment", totalPayment.toString());
    xmlDocument.setParameter("caclTotalDeposit", totalDeposit.toString());
    xmlDocument.setParameter("calcCurrentlyCleared", currentlyCleared.toString());
    xmlDocument.setData("structure", data);

    JSONObject msg = new JSONObject();
    final String formatOutput = vars.getSessionValue("#FormatOutput|euroEdition", "#0.00");
    try {
      msg.put("reconciliationlines", xmlDocument.print());
      msg.put("calcTotalPayment", totalPayment);
      msg.put("calcTotalDeposit", totalDeposit);
      msg.put("calcCurrentlyCleared", currentlyCleared);
      msg.put("formatOutput", formatOutput);
    } catch (JSONException e) {
      log4j.error("JSON object error" + msg.toString());
    }
    response.setContentType("application/json; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(msg.toString());
    out.close();
  }

  private boolean isAutomaticReconciliation(FIN_Reconciliation reconciliation) {
    OBContext.setAdminMode();
    try {
      final OBCriteria<FIN_ReconciliationLine_v> obc = OBDal.getInstance().createCriteria(FIN_ReconciliationLine_v.class);
      obc.add(Restrictions.eq(FIN_ReconciliationLine_v.PROPERTY_RECONCILIATION, reconciliation));
      obc.add(Restrictions.isNotNull(FIN_ReconciliationLine_v.PROPERTY_BANKSTATEMENTLINE));
      obc.setMaxResults(1);
      final List<FIN_ReconciliationLine_v> rec = obc.list();
      return (rec.size() != 0);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public String getServletInfo() {
    return "This servlet manages manual transactions reconciliations.";
  }

  private String linesInNotAvailablePeriod(String reconciliationId) {
    final StringBuilder hql = new StringBuilder();

    hql.append(" as rl ");
    hql.append(" where rl.reconciliation.id = '").append(reconciliationId).append("' ");
    hql.append("   and c_chk_open_period(rl.organization, rl.transactionDate, 'REC', null) = 0 ");
    hql.append(" order by rl.transactionDate");

    final OBQuery<FIN_ReconciliationLine_v> obqRL = OBDal.getInstance().createQuery(FIN_ReconciliationLine_v.class, hql.toString());
    obqRL.setMaxResult(1);

    List<FIN_ReconciliationLine_v> obqRLlist = obqRL.list();

    if (obqRLlist.size() == 0) {
      return "";
    } else {
      return obqRLlist.get(0).getIdentifier();
    }

  }

}
