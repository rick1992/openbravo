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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.dao.TransactionsDao;
import org.openbravo.advpaymentmngt.process.FIN_TransactionProcess;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.DimensionDisplayUtility;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.financialmgmt.accounting.Costcenter;
import org.openbravo.model.financialmgmt.accounting.UserDimension1;
import org.openbravo.model.financialmgmt.accounting.UserDimension2;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatementLine;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_Reconciliation;
import org.openbravo.model.marketing.Campaign;
import org.openbravo.model.materialmgmt.cost.ABCActivity;
import org.openbravo.model.project.Project;
import org.openbravo.model.sales.SalesRegion;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.accounting.SCO_Utils;
import pe.com.unifiedgo.accounting.data.SCOEEFFCashflow;
import pe.com.unifiedgo.accounting.data.SCOInternalDoc;

public class AddTransaction extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private AdvPaymentMngtDao dao;

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strOrgId = vars.getRequestGlobalVariable("inpadOrgId", "AddTransaction|Org");
      String strWindowId = vars.getRequestGlobalVariable("inpwindowId", "AddTransaction|windowId");
      String strTabId = vars.getRequestGlobalVariable("inpTabId", "AddTransaction|tabId");
      String strFinancialAccountId = vars.getStringParameter("inpfinFinancialAccountId");
      String strFinBankStatementLineId = vars.getStringParameter("inpFinBankStatementLineId", "",
          IsIDFilter.instance);
      String strExchangeRate = vars.getGlobalVariable("inpExchangeRate",
          "PFOLCreatePayment|ExchangeRate", "");
      String strAltExchangeRate = vars.getGlobalVariable("inpAltExchangeRate",
          "PFOLCreatePayment|ExchangeRate", "");
      String strC_Currency_ID = vars.getRequestGlobalVariable("C_Currency_ID",
          "AddTransaction|Org");
      String strTransactionDate = DateTimeData.today(this);
      strC_Currency_ID = getBaseCurrencyId(strFinancialAccountId);
      strAltExchangeRate = getAltExchangeRate(strFinancialAccountId, strTransactionDate,
          strC_Currency_ID);
      final int accesslevel = 3;

      if ((org.openbravo.erpCommon.utility.WindowAccessData.hasReadOnlyAccess(this, vars.getRole(),
          strTabId))
          || !(Utility.isElementInList(
              Utility.getContext(this, vars, "#User_Client", strWindowId, accesslevel),
              vars.getClient())
              && Utility.isElementInList(
                  Utility.getContext(this, vars, "#User_Org", strWindowId, accesslevel),
                  strOrgId))) {
        OBError myError = Utility.translateError(this, vars, vars.getLanguage(),
            Utility.messageBD(this, "NoWriteAccess", vars.getLanguage()));
        vars.setMessage(strTabId, myError);
        printPageClosePopUp(response, vars);
      } else {
        printPage(response, vars, strOrgId, strWindowId, strTabId, strFinancialAccountId,
            strFinBankStatementLineId, strExchangeRate, strAltExchangeRate, strTransactionDate,
            strC_Currency_ID);
      }

    } else if (vars.commandIn("EXCHANGERATE")) {

      String strFinancialAccountId = vars.getStringParameter("inpFinFinancialAccountId", "");
      String strTransactionDate = vars.getStringParameter("inpMainDate", "");
      String strC_Currency_ID = vars.getStringParameter("C_Currency_ID", "");

      refreshAltExchangeRate(response, vars, strFinancialAccountId, strTransactionDate,
          strC_Currency_ID);
    } else if (vars.commandIn("GRID")) {
      String strFinancialAccountId = vars.getStringParameter("inpFinFinancialAccountId", "");
      boolean strIsReceipt = "RCIN".equals(vars.getStringParameter("inpDocumentType"));
      String strFromDate = vars.getStringParameter("inpDateFrom");
      String strToDate = vars.getStringParameter("inpDateTo");
      String closeAutomatically = vars
          .getSessionValue("AddPaymentFromTransaction|closeAutomatically");
      String paymentWithTransaction = vars.getSessionValue("AddPaymentFromTransaction|PaymentId");
      vars.removeSessionValue("AddPaymentFromTransaction|closeAutomatically");
      vars.removeSessionValue("AddPaymentFromTransaction|PaymentId");
      String strFinBankStatementLineId = vars.getStringParameter("inpFinBankStatementLineId", "",
          IsIDFilter.instance);
      Boolean showAlternativeFA = "Y".equals(vars.getStringParameter("inpAlternativeFA", "N"));

      printGrid(response, vars, strFinancialAccountId, strFromDate, strToDate, strIsReceipt,
          strFinBankStatementLineId, closeAutomatically, paymentWithTransaction, showAlternativeFA);

    } else if (vars.commandIn("SAVE")) {
      String strTabId = vars.getGlobalVariable("inpTabId", "AddTransaction|tabId");
      String strFinancialAccountId = vars.getStringParameter("inpFinFinancialAccountId", "");
      String selectedPaymentsIds = vars.getInParameter("inpPaymentId", IsIDFilter.instance);
      Boolean trxWithPaymentDate = "Y"
          .equals(vars.getStringParameter("inpTrxWithPaymentDate", "N"));

      String strTransactionType = vars.getStringParameter("inpTransactionType");
      String strTransactionDate = vars.getStringParameter("inpMainDate", "");

      String strFinAccountDId = vars.getStringParameter("inpFinAccountDId", "");
      String strFinPaymentDId = vars.getStringParameter("inpfinPaymentDId", "");
      String strCBPartnerDId = vars.getStringParameter("inpCBPartnerDId", "");

      String strConvertionRate = vars.getNumericParameter("inpConvertionRate", "");

      String strGLItemId = vars.getStringParameter("inpGLItemId", "");
      String strtrxnumber = vars.getStringParameter("inpTrnumber", "");
      String strGLItemDId = vars.getStringParameter("inpGLItemDId", "");
      String strGLItemDepositAmount = vars.getNumericParameter("inpDepositAmountGLItem", "");
      String strGLItemPaymentAmount = vars.getNumericParameter("inpPaymentAmountGLItem", "");

      String strGLItemDescription = vars.getStringParameter("inpGLItemDescription", "");

      String strFeeDepositAmount = vars.getNumericParameter("inpDepositAmount", "");
      String strFeePaymentAmount = vars.getNumericParameter("inpPaymentAmount", "");
      String strFeeDescription = vars.getStringParameter("inpFeeDescription", "");

      String strGLItemTId = vars.getStringParameter("inpGLItemTId", "");
      String strFinAccountTId = vars.getStringParameter("inpFinAccountTId", "");
      String strGLItemTPaymentAmount = vars.getNumericParameter("inpPaymentAmountGLItemT", "");

      String strFinBankStatementLineId = vars.getStringParameter("inpFinBankStatementLineId", "",
          IsIDFilter.instance);

      String strExchangeRate = vars.getNumericParameter("inpExchangeRate", "");
      String strAltExchangeRate = vars.getNumericParameter("inpAltExchangeRate", "");

      saveAndCloseWindow(response, vars, strTabId, strFinancialAccountId, selectedPaymentsIds,
          strTransactionType, strGLItemId, strGLItemDepositAmount, strGLItemPaymentAmount,
          strFeeDepositAmount, strFeePaymentAmount, strTransactionDate, strFinBankStatementLineId,
          strGLItemDescription, strFeeDescription, strFinAccountDId, strConvertionRate,
          strGLItemDId, strFinPaymentDId, strCBPartnerDId, strGLItemTId, strFinAccountTId,
          strGLItemTPaymentAmount, strtrxnumber, trxWithPaymentDate, strExchangeRate,
          strAltExchangeRate);
    } else if (vars.commandIn("PERIOD")) {

      String strTransactionDate = vars.getStringParameter("inpMainDate", "");
      String strOrgId = vars.getRequestGlobalVariable("inpadOrgId", "AddTransaction|Org");
      OBContext.setAdminMode();
      try {
        final Organization org = OBDal.getInstance().get(Organization.class, strOrgId);
        String strclient = org.getClient().getId();
        boolean orgLegalWithAccounting = false;
        if ((org.getOrganizationType().isLegalEntity())
            || (org.getOrganizationType().isBusinessUnit())) {
          orgLegalWithAccounting = true;
        }
        if ((!FIN_Utility.isPeriodOpen(strclient, AcctServer.DOCTYPE_FinAccTransaction, strOrgId,
            strTransactionDate)) && orgLegalWithAccounting) {
          try {
            JSONObject json = new JSONObject();
            json.put("text", "PeriodNotAvailable");

            response.setContentType("text/html; charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("objson = " + json);
            out.close();

          } catch (JSONException e) {

            e.printStackTrace();
          }

        }
      } finally {
        OBContext.restorePreviousMode();
      }
    }

  }

  private void refreshAltExchangeRate(HttpServletResponse response, VariablesSecureApp vars,
      String strFinancialAccountId, String strTransactionDate, String strC_Currency_ID)
      throws IOException, ServletException {

    final String formatOutput = vars.getSessionValue("#FormatOutput|generalQtyRelation",
        "#,##0.######");
    JSONObject msg = new JSONObject();
    try {
      msg.put("altExchangeRate",
          getAltExchangeRate(strFinancialAccountId, strTransactionDate, strC_Currency_ID));
      msg.put("formatOutput", formatOutput);
    } catch (JSONException e) {
      log4j.error("JSON object error" + msg.toString());
    }

    response.setContentType("application/json; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(msg.toString());
    out.close();
  }

  private String getBaseCurrencyId(String strFinancialAccountId) {
    String baseCurrencyId = "308";
    FIN_FinancialAccount account = OBDal.getInstance().get(FIN_FinancialAccount.class,
        strFinancialAccountId);
    if (account == null)
      return baseCurrencyId;

    Organization parentOrg = OBContext.getOBContext().getOrganizationStructureProvider()
        .getLegalEntity(account.getOrganization());
    if (parentOrg != null) {
      if (parentOrg.getGeneralLedger() != null) {
        baseCurrencyId = parentOrg.getGeneralLedger().getCurrency().getId();
      }
    }

    return baseCurrencyId;
  }

  private String getAltExchangeRate(String strFinancialAccountId, String strTransactionDate,
      String cCurrencyToId) {
    Date trxdate = FIN_Utility.getDate(strTransactionDate);
    FIN_FinancialAccount account = OBDal.getInstance().get(FIN_FinancialAccount.class,
        strFinancialAccountId);
    if (account == null)
      return "";

    String cCurrencyFromId = account.getCurrency().getId();

    String strAltExchangeRate = "";
    try {
      strAltExchangeRate = SCO_Utils.getMultiplyRateGenSales(this, account.getClient().getId(),
          account.getOrganization().getId(), cCurrencyFromId, cCurrencyToId, trxdate);
    } catch (Exception e) {
    }

    return strAltExchangeRate;
  }

  private void saveAndCloseWindow(HttpServletResponse response, VariablesSecureApp vars,
      String strTabId, String strFinancialAccountId, String selectedPaymentIds,
      String strTransactionType, String strGLItemId, String strGLItemDepositAmount,
      String strGLItemPaymentAmount, String strFeeDepositAmount, String strFeePaymentAmount,
      String strTransactionDate, String strFinBankStatementLineId, String strGLItemDescription,
      String strFeeDescription, String strFinAccountDId, String strConvertionRate,
      String strGLItemDId, String strFinPaymentDId, String strCBPartnerDId, String strGLItemTId,
      String strFinAccountTId, String strGLItemTPaymentAmount, String strtrxnumber,
      Boolean trxWithPaymentDate, String strExchangeRate, String strAltExchangeRate)
      throws IOException, ServletException {

    dao = new AdvPaymentMngtDao();
    String strMessage = "";
    OBError msg = new OBError();
    OBContext.setAdminMode();
    try {

      // SALES = DEPOSIT
      // PURCHASE = PAYMENT
      if (strTransactionType.equals("P")) { // Payment

        List<FIN_Payment> selectedPayments = FIN_Utility.getOBObjectList(FIN_Payment.class,
            selectedPaymentIds);

        for (FIN_Payment p : selectedPayments) {
          BigDecimal depositAmt = FIN_Utility.getDepositAmount(p.isReceipt(),
              p.getFinancialTransactionAmount());
          BigDecimal paymentAmt = FIN_Utility.getPaymentAmount(p.isReceipt(),
              p.getFinancialTransactionAmount());

          String description = null;
          if (p.getDescription() != null) {
            description = p.getDescription().replace("\n", ". ");
          }

          Date trxdate = FIN_Utility.getDate(strTransactionDate);
          if (trxWithPaymentDate) {
            trxdate = p.getPaymentDate();
          }

          FIN_FinaccTransaction finTrans = dao.getNewFinancialTransactionFull(p.getOrganization(),
              OBDal.getInstance().get(FIN_FinancialAccount.class, strFinancialAccountId),
              TransactionsDao.getTransactionMaxLineNo(
                  OBDal.getInstance().get(FIN_FinancialAccount.class, strFinancialAccountId)) + 10,
              p, description, trxdate, null, p.isReceipt() ? "RDNC" : "PWNC", depositAmt,
              paymentAmt, null, null, null, p.isReceipt() ? "BPD" : "BPW", trxdate, p.getCurrency(),
              p.getFinancialTransactionConvertRate(), p.getAmount(), null, null, null, null);

          // change transaction date
          /*
           * p.setProcessed(false); OBDal.getInstance().save(p); OBDal.getInstance().flush();
           * 
           * p.setPaymentDate(FIN_Utility.getDate(strTransactionDate)); p.setProcessed(true);
           * OBDal.getInstance().save(p); OBDal.getInstance().flush();
           */

          OBError processTransactionError = processTransaction(vars, this, "P", finTrans);
          if (processTransactionError != null
              && "Error".equals(processTransactionError.getType())) {
            throw new OBException(processTransactionError.getMessage());
          }
          if (!"".equals(strFinBankStatementLineId)) {
            matchBankStatementLine(vars, finTrans, strFinBankStatementLineId);
          }
        }

        if (selectedPaymentIds != null && selectedPayments.size() > 0) {
          strMessage = selectedPayments.size() + " " + "@RowsInserted@";
        }

      } else if (strTransactionType.equals("GL")) { // GL Item
        // Accounting Dimensions
        final String strElement_OT = vars.getStringParameter("inpadOrgTrxId", IsIDFilter.instance);
        final Organization organization = OBDal.getInstance().get(Organization.class,
            strElement_OT);

        final String strElement_BP = vars.getStringParameter("inpCBPartnerId", IsIDFilter.instance);
        final BusinessPartner businessPartner = OBDal.getInstance().get(BusinessPartner.class,
            strElement_BP);

        final String strElement_PR = vars.getStringParameter("inpMProductId", IsIDFilter.instance);
        final Product product = OBDal.getInstance().get(Product.class, strElement_PR);

        final String strElement_PJ = vars.getStringParameter("inpCProjectId", IsIDFilter.instance);
        final Project project = OBDal.getInstance().get(Project.class, strElement_PJ);

        final String strElement_AY = vars.getStringParameter("inpCActivityId", IsIDFilter.instance);
        final ABCActivity activity = OBDal.getInstance().get(ABCActivity.class, strElement_AY);

        final String strElement_SR = vars.getStringParameter("inpCSalesRegionId",
            IsIDFilter.instance);
        final SalesRegion salesRegion = OBDal.getInstance().get(SalesRegion.class, strElement_SR);

        final String strElement_MC = vars.getStringParameter("inpCampaignId", IsIDFilter.instance);
        final Campaign campaign = OBDal.getInstance().get(Campaign.class, strElement_MC);

        final String strElement_U1 = vars.getStringParameter("inpUser1ID", IsIDFilter.instance);
        final UserDimension1 user1 = OBDal.getInstance().get(UserDimension1.class, strElement_U1);

        final String strElement_U2 = vars.getStringParameter("inpUser2ID", IsIDFilter.instance);
        final UserDimension2 user2 = OBDal.getInstance().get(UserDimension2.class, strElement_U2);

        final String strElement_CC = vars.getStringParameter("inpCCostcenterId",
            IsIDFilter.instance);
        final Costcenter costcenter = OBDal.getInstance().get(Costcenter.class, strElement_CC);

        final String strElement_ID = vars.getStringParameter("inpscoInternalDocId",
            IsIDFilter.instance);
        final SCOInternalDoc internaldoc = OBDal.getInstance().get(SCOInternalDoc.class,
            strElement_ID);

        final String strElement_IG = vars.getStringParameter("inpscoInvoiceGlrefId",
            IsIDFilter.instance);
        final Invoice invglref = OBDal.getInstance().get(Invoice.class, strElement_IG);

        final String strElement_CF = vars.getStringParameter("inpscoEeffCashflowId",
            IsIDFilter.instance);
        SCOEEFFCashflow cashflow = null;
        cashflow = OBDal.getInstance().get(SCOEEFFCashflow.class, strElement_CF);

        BigDecimal glItemDepositAmt = new BigDecimal(strGLItemDepositAmount);
        BigDecimal glItemPaymentAmt = new BigDecimal(strGLItemPaymentAmount);

        FIN_FinancialAccount account = OBDal.getInstance().get(FIN_FinancialAccount.class,
            strFinancialAccountId);
        GLItem glItem = OBDal.getInstance().get(GLItem.class, strGLItemId);
        String description = strGLItemDescription.isEmpty()
            ? Utility.messageBD(this, "APRM_GLItem", vars.getLanguage()) + ": " + glItem.getName()
            : strGLItemDescription;
        boolean isReceipt = (glItemDepositAmt.compareTo(glItemPaymentAmt) >= 0);

        // Currency, Organization, paymentDate,
        FIN_FinaccTransaction finTrans = dao.getNewFinancialTransaction(organization, account,
            TransactionsDao.getTransactionMaxLineNo(account) + 10, null, description,
            FIN_Utility.getDate(strTransactionDate), glItem, isReceipt ? "RDNC" : "PWNC",
            glItemDepositAmt, glItemPaymentAmt, project, campaign, activity,
            isReceipt ? "BPD" : "BPW", FIN_Utility.getDate(strTransactionDate), null, null, null,
            businessPartner, product, salesRegion, user1, user2, costcenter, internaldoc, invglref,
            cashflow);
        String trxnumber = null;
        if (strtrxnumber != null)
          trxnumber = strtrxnumber.replaceAll("\\s+", "");
        if (trxnumber != null && trxnumber.isEmpty())
          trxnumber = null;
        finTrans.setScoTrxnumber(trxnumber);

        if (strAltExchangeRate != null && !strAltExchangeRate.isEmpty()) {
          BigDecimal altexchangerate = new BigDecimal(strAltExchangeRate);
          finTrans.setScoAltConvertRate(altexchangerate);
        }

        OBError processTransactionError = processTransaction(vars, this, "P", finTrans);
        if (processTransactionError != null && "Error".equals(processTransactionError.getType())) {
          throw new OBException(processTransactionError.getMessage());
        }
        strMessage = "1 " + "@RowsInserted@";
        if (!"".equals(strFinBankStatementLineId)) {
          matchBankStatementLine(vars, finTrans, strFinBankStatementLineId);
        }

      } else if (strTransactionType.equals("F")) { // Fee
        BigDecimal feeDepositAmt = new BigDecimal(strFeeDepositAmount);
        BigDecimal feePaymentAmt = new BigDecimal(strFeePaymentAmount);
        FIN_FinancialAccount account = OBDal.getInstance().get(FIN_FinancialAccount.class,
            strFinancialAccountId);
        boolean isReceipt = (feeDepositAmt.compareTo(feePaymentAmt) >= 0);
        String description = strFeeDescription.isEmpty()
            ? Utility.messageBD(this, "APRM_BankFee", vars.getLanguage())
            : strFeeDescription;

        FIN_FinaccTransaction finTrans = dao.getNewFinancialTransactionFull(
            account.getOrganization(), account,
            TransactionsDao.getTransactionMaxLineNo(account) + 10, null, description,
            FIN_Utility.getDate(strTransactionDate), null, isReceipt ? "RDNC" : "PWNC",
            feeDepositAmt, feePaymentAmt, null, null, null, "BF",
            FIN_Utility.getDate(strTransactionDate), null, null, null, null, null, null, null);
        OBError processTransactionError = processTransaction(vars, this, "P", finTrans);
        if (processTransactionError != null && "Error".equals(processTransactionError.getType())) {
          throw new OBException(processTransactionError.getMessage());
        }
        strMessage = "1 " + "@RowsInserted@";
        if (!"".equals(strFinBankStatementLineId)) {
          matchBankStatementLine(vars, finTrans, strFinBankStatementLineId);
        }

      } else if (strTransactionType.equals("SCO_CTOB")) {

        GLItem glItem = OBDal.getInstance().get(GLItem.class, strGLItemDId);
        FIN_Payment payment = OBDal.getInstance().get(FIN_Payment.class, strFinPaymentDId);
        BusinessPartner bpartnerd = OBDal.getInstance().get(BusinessPartner.class, strCBPartnerDId);
        FIN_FinancialAccount accountct = OBDal.getInstance().get(FIN_FinancialAccount.class,
            strFinAccountDId);

        if (glItem == null || payment == null || bpartnerd == null) {
          throw new OBException("@SCO_InvalidArgumentsForDeposit@");
        }

        if (payment.getStatus().compareTo("RDNC") != 0
            || payment.getScoCashtransferstatus().compareTo("SCO_CASH") != 0) {
          throw new OBException("@SCO_InvalidArgumentsForDeposit@");
        }

        FIN_FinancialAccount account = OBDal.getInstance().get(FIN_FinancialAccount.class,
            strFinancialAccountId);
        final String strElement_OT = vars.getStringParameter("inpadOrgTrxId", IsIDFilter.instance);
        final Organization organization = OBDal.getInstance().get(Organization.class,
            strElement_OT);

        BigDecimal glItemDepositAmt = new BigDecimal(0);
        BigDecimal glItemPaymentAmt = payment.getAmount();
        String description = strGLItemDescription.isEmpty()
            ? Utility.messageBD(this, "APRM_GLItem", vars.getLanguage()) + ": " + glItem.getName()
            : strGLItemDescription;
        boolean isReceipt = (glItemDepositAmt.compareTo(glItemPaymentAmt) >= 0);

        // Currency, Organization, paymentDate,
        FIN_FinaccTransaction finTrans = dao.getNewFinancialTransaction(organization, account,
            TransactionsDao.getTransactionMaxLineNo(account) + 10, null, description,
            FIN_Utility.getDate(strTransactionDate), glItem, isReceipt ? "RDNC" : "PWNC",
            glItemDepositAmt, glItemPaymentAmt, null, null, null, isReceipt ? "BPD" : "BPW",
            FIN_Utility.getDate(strTransactionDate), null, null, null, null, null, null, null, null,
            null, null, null, null);
        finTrans.setScoCtransPayin(payment);
        finTrans.setScoCtransFinacc(accountct);
        finTrans.setScoCtransBpartner(bpartnerd);

        OBError processTransactionError = processTransaction(vars, this, "P", finTrans);
        if (processTransactionError != null && "Error".equals(processTransactionError.getType())) {
          throw new OBException(processTransactionError.getMessage());
        }

        // payment em_sco_cashtransferstatus column to
        // 'SCO_TRANSIT' status.

        payment.setScoCashtransferstatus("SCO_TRANSIT");

      } else if (strTransactionType.equals("SCO_T")) {

        GLItem glItem = OBDal.getInstance().get(GLItem.class, strGLItemTId);
        FIN_FinancialAccount accounttrans = OBDal.getInstance().get(FIN_FinancialAccount.class,
            strFinAccountTId);

        if (glItem == null || accounttrans == null) {
          throw new OBException("@SCO_InvalidArgumentsForTransfer@");
        }

        FIN_FinancialAccount account = OBDal.getInstance().get(FIN_FinancialAccount.class,
            strFinancialAccountId);
        final String strElement_OT = vars.getStringParameter("inpadOrgTrxId", IsIDFilter.instance);
        final Organization organization = OBDal.getInstance().get(Organization.class,
            strElement_OT);

        BigDecimal glItemDepositAmt = new BigDecimal(0);
        BigDecimal glItemPaymentAmt = new BigDecimal(strGLItemTPaymentAmount);
        String description = strGLItemDescription.isEmpty()
            ? Utility.messageBD(this, "APRM_GLItem", vars.getLanguage()) + ": " + glItem.getName()
            : strGLItemDescription;
        boolean isReceipt = (glItemDepositAmt.compareTo(glItemPaymentAmt) >= 0);

        // Currency, Organization, paymentDate,
        FIN_FinaccTransaction finTrans = dao.getNewFinancialTransaction(organization, account,
            TransactionsDao.getTransactionMaxLineNo(account) + 10, null, description,
            FIN_Utility.getDate(strTransactionDate), glItem, isReceipt ? "RDNC" : "PWNC",
            glItemDepositAmt, glItemPaymentAmt, null, null, null, isReceipt ? "BPD" : "BPW",
            FIN_Utility.getDate(strTransactionDate), null, null, null, null, null, null, null, null,
            null, null, null, null);
        OBError processTransactionError = processTransaction(vars, this, "P", finTrans);
        if (processTransactionError != null && "Error".equals(processTransactionError.getType())) {
          throw new OBException(processTransactionError.getMessage());
        }

        BigDecimal glItemDepositAmtT = new BigDecimal(strGLItemTPaymentAmount);
        BigDecimal glItemPaymentAmtT = new BigDecimal(0);
        String descriptionT = strGLItemDescription.isEmpty()
            ? Utility.messageBD(this, "APRM_GLItem", vars.getLanguage()) + ": " + glItem.getName()
            : strGLItemDescription;
        boolean isReceiptT = (glItemDepositAmt.compareTo(glItemPaymentAmt) >= 0);
        FIN_FinaccTransaction finTransT = dao.getNewFinancialTransaction(organization, accounttrans,
            TransactionsDao.getTransactionMaxLineNo(accounttrans) + 10, null, descriptionT,
            FIN_Utility.getDate(strTransactionDate), glItem, isReceiptT ? "RDNC" : "PWNC",
            glItemDepositAmtT, glItemPaymentAmtT, null, null, null, isReceiptT ? "BPD" : "BPW",
            FIN_Utility.getDate(strTransactionDate), null, null, null, null, null, null, null, null,
            null, null, null, null);
        OBError processTransactionErrorT = processTransaction(vars, this, "P", finTransT);
        if (processTransactionErrorT != null
            && "Error".equals(processTransactionErrorT.getType())) {
          throw new OBException(processTransactionErrorT.getMessage());
        }

        OBDal.getInstance().flush();
        finTrans.setScoTrtoaccFinacctrx(finTransT);
        OBDal.getInstance().save(finTrans);
      }

      // Message
      msg.setType("Success");
      msg.setTitle(Utility.messageBD(this, "Success", vars.getLanguage()));
      msg.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(), strMessage));
      vars.setMessage(strTabId, msg);
      msg = null;
      if ("".equals(strFinBankStatementLineId))
        printPageClosePopUpAndRefreshParent(response, vars);
      else {
        log4j.debug("Output: PopUp Response");
        final XmlDocument xmlDocument = xmlEngine
            .readXmlTemplate("org/openbravo/base/secureApp/PopUp_Close_Refresh")
            .createXmlDocument();
        xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
        response.setContentType("text/html; charset=UTF-8");
        final PrintWriter out = response.getWriter();
        out.println(xmlDocument.print());
        out.close();
      }

    } catch (Exception e) {
      OBError newError = Utility.translateError(this, vars, vars.getLanguage(),
          FIN_Utility.getExceptionMessage(e));
      vars.setMessage(strTabId, newError);
      printPageClosePopUp(response, vars);
      OBDal.getInstance().rollbackAndClose();
      // throw new OBException(newError.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strOrgId,
      String strWindowId, String strTabId, String strFinancialAccountId,
      String strBankStatementLineId, String strExchangeRate, String strAltExchangeRate,
      String strTransactionDate, String strC_Currency_ID) throws IOException, ServletException {

    log4j.debug("Output: Add Transaction pressed on Financial Account || Transaction tab");

    XmlDocument xmlDocument = xmlEngine
        .readXmlTemplate("org/openbravo/advpaymentmngt/ad_actionbutton/AddTransaction")
        .createXmlDocument();

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());

    xmlDocument.setParameter("dateDisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("mainDate", strTransactionDate);
    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("tabId", strTabId);
    xmlDocument.setParameter("orgId", strOrgId);
    xmlDocument.setParameter("orgTrxId", strOrgId);
    Organization orgTrx = OBDal.getInstance().get(Organization.class, strOrgId);
    xmlDocument.setParameter("orgTrxName", orgTrx.getIdentifier());

    xmlDocument.setParameter("C_Currency_ID", strC_Currency_ID);
    xmlDocument.setParameter("finFinancialAccountId", strFinancialAccountId);

    FIN_FinancialAccount finacc = OBDal.getInstance().get(FIN_FinancialAccount.class,
        strFinancialAccountId);
    xmlDocument.setParameter("financialAccountCurrencyId", finacc.getCurrency().getId());

    xmlDocument.setParameter("finBankStatementLineId", strBankStatementLineId);
    String transactionType = "P";
    try {
      String Document_Type_AddTransaction_Reference_ID = "40B84CF78FC9435790887846CCDAE875";
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
          Document_Type_AddTransaction_Reference_ID, "",
          Utility.getContext(this, vars, "#AccessibleOrgTree", "AddTransaction"),
          Utility.getContext(this, vars, "#User_Client", "AddTransaction"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "AddTransaction", "");
      xmlDocument.setData("reportDocumentType", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    boolean isReceipt = true;
    if (!"".equals(strBankStatementLineId)) {
      FIN_BankStatementLine bsl = OBDal.getInstance().get(FIN_BankStatementLine.class,
          strBankStatementLineId);
      String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .getProperty("dateFormat.java");
      SimpleDateFormat dateFormater = new SimpleDateFormat(dateFormat);
      // Default signum based on amount
      isReceipt = bsl.getCramount().subtract(bsl.getDramount()).signum() > 0;
      if (bsl.getBusinessPartner() != null && bsl.getBusinessPartner().isCustomer()
          && !bsl.getBusinessPartner().isVendor()) {
        isReceipt = true;
      }
      if (bsl.getBusinessPartner() != null && !bsl.getBusinessPartner().isCustomer()
          && bsl.getBusinessPartner().isVendor()) {
        isReceipt = false;
      }
      xmlDocument.setParameter("depositAmount", bsl.getCramount().toString());
      xmlDocument.setParameter("paymentAmount", bsl.getDramount().toString());
      xmlDocument.setParameter("depositAmountGLItem", bsl.getCramount().toString());
      xmlDocument.setParameter("paymentAmountGLItem", bsl.getDramount().toString());
      xmlDocument.setParameter("mainDate", dateFormater.format(bsl.getTransactionDate()));
      String bslDescription = (!"".equals(bsl.getDescription()) && bsl.getDescription() != null)
          ? bsl.getDescription()
          : "";
      String strDescription = (!"".equals(bsl.getBpartnername()) && bsl.getBpartnername() != null)
          ? bsl.getBpartnername() + "\n" + bslDescription
          : bslDescription;
      xmlDocument.setParameter("GLItemDescription", strDescription);
      xmlDocument.setParameter("FeeDescription", strDescription);
      if (bsl.getGLItem() != null) {
        transactionType = "GL";
        xmlDocument.setParameter("GLItemID", bsl.getGLItem().getId());
        xmlDocument.setParameter("GLItemName", bsl.getGLItem().getIdentifier());
      }

      if (bsl.getBusinessPartner() != null) {
        xmlDocument.setParameter("BPartnerID", bsl.getBusinessPartner().getId());
        xmlDocument.setParameter("BPartnerName", bsl.getBusinessPartner().getIdentifier());
      }
    } else {
      xmlDocument.setParameter("depositAmount", BigDecimal.ZERO.toString());
      xmlDocument.setParameter("paymentAmount", BigDecimal.ZERO.toString());
      xmlDocument.setParameter("depositAmountGLItem", BigDecimal.ZERO.toString());
      xmlDocument.setParameter("paymentAmountGLItem", BigDecimal.ZERO.toString());
      xmlDocument.setParameter("convertionRate", BigDecimal.ONE.toString());
      xmlDocument.setParameter("GLItemDescription", "");
      xmlDocument.setParameter("FeeDescription", "");

    }
    final String RECEIVED_IN_OPTION = "RCIN";
    final String PAID_OUT_OPTION = "PDOUT";
    xmlDocument.setParameter("documentType", isReceipt ? RECEIVED_IN_OPTION : PAID_OUT_OPTION);
    try {
      String Transaction_Type_AddTransaction_Reference_ID = "C1B4345A1F8841C2B1ADD403CA733D75";
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
          Transaction_Type_AddTransaction_Reference_ID, "094852AC1FFE4CDC9926F87F5FFDF883",
          Utility.getContext(this, vars, "#AccessibleOrgTree", "AddTransaction"),
          Utility.getContext(this, vars, "#User_Client", "AddTransaction"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "AddTransaction", "");
      xmlDocument.setData("reportTransactionType", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setParameter("transactionType", transactionType);

    // Accounting Dimensions
    final String strCentrally = Utility.getContext(this, vars,
        DimensionDisplayUtility.IsAcctDimCentrally, strWindowId);

    final String strElement_OT = Utility.getContext(this, vars,
        DimensionDisplayUtility.displayAcctDimensions(strCentrally,
            DimensionDisplayUtility.DIM_Organization, AcctServer.DOCTYPE_FinAccTransaction,
            DimensionDisplayUtility.DIM_Header),
        strWindowId);
    final String strElement_BP = Utility.getContext(this, vars,
        DimensionDisplayUtility.displayAcctDimensions(strCentrally,
            DimensionDisplayUtility.DIM_BPartner, AcctServer.DOCTYPE_FinAccTransaction,
            DimensionDisplayUtility.DIM_Header),
        strWindowId);
    final String strElement_PR = Utility.getContext(this, vars,
        DimensionDisplayUtility.displayAcctDimensions(strCentrally,
            DimensionDisplayUtility.DIM_Product, AcctServer.DOCTYPE_FinAccTransaction,
            DimensionDisplayUtility.DIM_Header),
        strWindowId);
    String strElement_PJ = Utility.getContext(this, vars,
        DimensionDisplayUtility.displayAcctDimensions(strCentrally,
            DimensionDisplayUtility.DIM_Project, AcctServer.DOCTYPE_FinAccTransaction,
            DimensionDisplayUtility.DIM_Header),
        strWindowId);
    final String strElement_AY = Utility.getContext(this, vars, "$Element_AY", strWindowId);
    final String strElement_SR = Utility.getContext(this, vars, "$Element_SR", strWindowId);
    final String strElement_MC = Utility.getContext(this, vars, "$Element_MC", strWindowId);
    final String strElement_U1 = Utility.getContext(this, vars,
        DimensionDisplayUtility.displayAcctDimensions(strCentrally,
            DimensionDisplayUtility.DIM_User1, AcctServer.DOCTYPE_FinAccTransaction,
            DimensionDisplayUtility.DIM_Header),
        strWindowId);
    final String strElement_U2 = Utility.getContext(this, vars,
        DimensionDisplayUtility.displayAcctDimensions(strCentrally,
            DimensionDisplayUtility.DIM_User2, AcctServer.DOCTYPE_FinAccTransaction,
            DimensionDisplayUtility.DIM_Header),
        strWindowId);
    final String strElement_CC = Utility.getContext(this, vars,
        DimensionDisplayUtility.displayAcctDimensions(strCentrally,
            DimensionDisplayUtility.DIM_CostCenter, AcctServer.DOCTYPE_FinAccTransaction,
            DimensionDisplayUtility.DIM_Header),
        strWindowId);
    final String strElement_ID = "Y"; // always show the internal document dimension
    final String strElement_IG = "Y"; // always show the purchase/sales document dimension
    final String strElement_CF = "Y"; // always show the eeff cash flow
    strElement_PJ = "N"; // never show Project dimension

    xmlDocument.setParameter("strElement_OT", strElement_OT);
    xmlDocument.setParameter("strElement_BP", strElement_BP);
    xmlDocument.setParameter("strElement_PR", strElement_PR);
    xmlDocument.setParameter("strElement_PJ", strElement_PJ);
    xmlDocument.setParameter("strElement_AY", strElement_AY);
    xmlDocument.setParameter("strElement_SR", strElement_SR);
    xmlDocument.setParameter("strElement_MC", strElement_MC);
    xmlDocument.setParameter("strElement_U1", strElement_U1);
    xmlDocument.setParameter("strElement_U2", strElement_U2);
    xmlDocument.setParameter("strElement_CC", strElement_CC);
    xmlDocument.setParameter("strElement_ID", strElement_ID);
    xmlDocument.setParameter("strElement_IG", strElement_IG);
    xmlDocument.setParameter("strElement_CF", strElement_CF);

    xmlDocument.setParameter("exchangeRate", strExchangeRate);
    xmlDocument.setParameter("altExchangeRate", strAltExchangeRate);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printGrid(HttpServletResponse response, VariablesSecureApp vars,
      String strFinancialAccountId, String strFromDate, String strToDate, boolean isReceipt,
      String strFinBankStatementLineId, String closeAutomatically, String paymentWithTransaction,
      Boolean showAlternativeFA) throws IOException, ServletException {
    dao = new AdvPaymentMngtDao();

    log4j.debug("Output: Grid with transactions not reconciled");

    XmlDocument xmlDocument = xmlEngine
        .readXmlTemplate("org/openbravo/advpaymentmngt/ad_actionbutton/AddTransactionGrid")
        .createXmlDocument();

    OBContext.setAdminMode();
    try {
      // From AddPaymentFromTransaction the payment has been deposited and the transaction exist
      if (!"".equals(strFinBankStatementLineId) && !"".equals(paymentWithTransaction)
          && "Y".equals(closeAutomatically)) {

        FIN_Payment payment = OBDal.getInstance().get(FIN_Payment.class, paymentWithTransaction);
        OBCriteria<FIN_FinaccTransaction> obcTrans = OBDal.getInstance()
            .createCriteria(FIN_FinaccTransaction.class);
        obcTrans.add(Restrictions.eq(FIN_FinaccTransaction.PROPERTY_FINPAYMENT, payment));
        FIN_FinaccTransaction finTrans = obcTrans.list().get(0);

        matchBankStatementLine(vars, finTrans, strFinBankStatementLineId);
      }
    } finally {
      OBContext.restorePreviousMode();
    }

    FIN_FinancialAccount account = OBDal.getInstance().get(FIN_FinancialAccount.class,
        strFinancialAccountId);

    // Payments not deposited/withdrawal
    // Not stored in Fin_Finacc_Transaction table
    final FieldProvider[] data;
    if (!"Y".equals(closeAutomatically)) {
      if (showAlternativeFA & isReceipt) {
        data = dao.getAlternativePaymentsNotDeposited(account, FIN_Utility.getDate(strFromDate),
            FIN_Utility.getDate(DateTimeData.nDaysAfter(this, strToDate, "1")), isReceipt);
      } else {
        data = dao.getPaymentsNotDeposited(account, FIN_Utility.getDate(strFromDate),
            FIN_Utility.getDate(DateTimeData.nDaysAfter(this, strToDate, "1")), isReceipt);
      }
    } else {
      vars.setSessionValue("MatchTransaction.executeMatching", "N");
      data = null;
    }
    xmlDocument.setData("structure", (data == null) ? set() : data);
    JSONObject table = new JSONObject();
    try {
      table.put("grid", xmlDocument.print());
      table.put("closeAutomatically", closeAutomatically);
    } catch (JSONException e) {
      log4j.debug("JSON object error" + table.toString());
    }
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println("data = " + table.toString());
    out.close();
  }

  private FieldProvider[] set() throws ServletException {
    HashMap<String, String> empty = new HashMap<String, String>();
    empty.put("finAcc", "");
    empty.put("paymentId", "");
    empty.put("paymentInfo", "");
    empty.put("paymentDescription", "");
    empty.put("paymentDate", "");
    empty.put("depositAmount", "");
    empty.put("paymentAmount", "");
    ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
    result.add(empty);
    return FieldProviderFactory.getFieldProviderArray(result);
  }

  private void matchBankStatementLine(VariablesSecureApp vars, FIN_FinaccTransaction finTrans,
      String strFinBankStatementLineId) {
    FIN_BankStatementLine bsline = dao.getObject(FIN_BankStatementLine.class,
        strFinBankStatementLineId);
    // The amounts must match
    if (bsline.getCramount().compareTo(finTrans.getDepositAmount()) != 0
        || bsline.getDramount().compareTo(finTrans.getPaymentAmount()) != 0) {
      vars.setSessionValue("AddTransaction|ShowJSMessage", "Y");
      vars.setSessionValue("AddTransaction|SelectedTransaction", finTrans.getId());
    } else {
      FIN_Reconciliation reconciliation = TransactionsDao
          .getLastReconciliation(finTrans.getAccount(), "N");
      bsline.setMatchingtype("AD");
      bsline.setFinancialAccountTransaction(finTrans);
      if (finTrans.getFinPayment() != null) {
        bsline.setBusinessPartner(finTrans.getFinPayment().getBusinessPartner());
        finTrans.getFinPayment().setStatus("RPPC");
      }
      finTrans.setReconciliation(reconciliation);
      finTrans.setStatus("RPPC");
      OBDal.getInstance().save(bsline);
      OBDal.getInstance().save(finTrans);
      OBDal.getInstance().flush();
    }
  }

  /**
   * It calls the Transaction Process for the given transaction and action.
   * 
   * @param vars
   *          VariablesSecureApp with the session data.
   * @param conn
   *          ConnectionProvider with the connection being used.
   * @param strAction
   *          String with the action of the process. {P, D, R}
   * @param transaction
   *          FIN_FinaccTransaction that needs to be processed.
   * @return a OBError with the result message of the process.
   * @throws Exception
   */
  private OBError processTransaction(VariablesSecureApp vars, ConnectionProvider conn,
      String strAction, FIN_FinaccTransaction transaction) throws Exception {
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

  public String getServletInfo() {
    return "This servlet adds transaction for a financial account";
  }

}
