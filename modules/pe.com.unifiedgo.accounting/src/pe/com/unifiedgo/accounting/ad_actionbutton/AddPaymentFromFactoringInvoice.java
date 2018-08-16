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
 * All portions are Copyright (C) 2010-2013 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package pe.com.unifiedgo.accounting.ad_actionbutton;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.process.FIN_AddPayment;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.data.FieldProvider;
import org.openbravo.data.UtilSql;
import org.openbravo.database.SessionInfo;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.DimensionDisplayUtility;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.CustomerAccounts;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.accounting.Costcenter;
import org.openbravo.model.financialmgmt.accounting.coa.AccountingCombination;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.service.db.CallStoredProcedure;
import org.openbravo.service.db.QueryTimeOutUtil;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.accounting.data.SCOFactinvLine;
import pe.com.unifiedgo.accounting.data.SCOFactoringinvoice;

public class AddPaymentFromFactoringInvoice extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private AdvPaymentMngtDao dao;
  private static final RequestFilter filterYesNo = new ValueListFilter("Y", "N", "");

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    dao = new AdvPaymentMngtDao();

    if (vars.commandIn("DEFAULT")) {
      String strWindowId = vars.getGlobalVariable("inpwindowId",
          "AddPaymentFromFactoringInvoice|Window_ID");
      String strTabId = vars.getGlobalVariable("inpTabId", "AddPaymentFromFactoringInvoice|Tab_ID");
      String strScoFactoringinvoiceId = vars.getGlobalVariable("inpscoFactoringinvoiceId", "");
      String strFinancialAccountId = vars.getStringParameter("inpfinFinancialAccountId");

      try {
        printPage(response, vars, strScoFactoringinvoiceId, strWindowId, strTabId,
            strFinancialAccountId);
      } catch (Exception e) {
        System.out.println(e.getMessage());
      }
    } else if (vars.commandIn("GRIDLIST")) {

    } else if (vars.commandIn("BPARTNERBLOCK")) {
      boolean isReceipt = vars.getRequiredStringParameter("isReceipt").equals("Y");
      String strBusinessPartnerId = vars.getRequestGlobalVariable("inpBusinessPartnerId", "");
      if ("".equals(strBusinessPartnerId)) {
        strBusinessPartnerId = vars.getRequestGlobalVariable("inpcBPartnerId", "");
      }
      if (!"".equals(strBusinessPartnerId)) {
        BusinessPartner businessPartner = OBDal.getInstance().get(BusinessPartner.class,
            strBusinessPartnerId);
        if (FIN_Utility.isBlockedBusinessPartner(businessPartner.getId(), isReceipt, 4)) {
          businessPartnerBlocked(response, vars, businessPartner.getIdentifier());
        }
      } else {
        String strSelectedScheduledPaymentDetailIds = vars
            .getInStringParameter("inpScheduledPaymentDetailId", "", null);
        if (!"".equals(strSelectedScheduledPaymentDetailIds)) {
          OBContext.setAdminMode(true);
          try {
            List<FIN_PaymentScheduleDetail> selectedPaymentDetails = FIN_Utility.getOBObjectList(
                FIN_PaymentScheduleDetail.class, strSelectedScheduledPaymentDetailIds);
            for (FIN_PaymentScheduleDetail psd : selectedPaymentDetails) {
              BusinessPartner bPartner;
              if (psd.getInvoicePaymentSchedule() == null) {
                bPartner = psd.getOrderPaymentSchedule().getOrder().getBusinessPartner();
              } else {
                bPartner = psd.getInvoicePaymentSchedule().getInvoice().getBusinessPartner();
              }
              if (FIN_Utility.isBlockedBusinessPartner(bPartner.getId(), isReceipt, 4)) {
                businessPartnerBlocked(response, vars, bPartner.getIdentifier());
              }
            }
          } finally {
            OBContext.restorePreviousMode();
          }
        }
      }
    } else if (vars.commandIn("SAVE") || vars.commandIn("SAVEANDPROCESS")) {

      String strScoFactoringinvoiceId = vars.getRequiredStringParameter("inpfinPaymentId");
      String strTabId = vars.getRequiredStringParameter("inpTabId");
      String strPaymentDate = vars.getRequiredStringParameter("inpPaymentDate");

      SCOFactoringinvoice factinv = new AdvPaymentMngtDao().getObject(SCOFactoringinvoice.class,
          strScoFactoringinvoiceId);

      if (factinv.getPayment() != null) {
        final OBError errmsg = new OBError();
        String errmessage = Utility.parseTranslation(this, vars, vars.getLanguage(),
            "@ActionNotAllowedHere@");

        errmsg.setType("Error");
        errmsg.setMessage(errmessage);
        errmsg.setTitle(Utility.parseTranslation(this, vars, vars.getLanguage(), "@Error@"));
        OBDal.getInstance().rollbackAndClose();
        vars.setMessage(strTabId, errmsg);
        printPageClosePopUpAndRefreshParent(response, vars);
        return;
      }

      List<SCOFactinvLine> lineasTotales = factinv.getSCOFactinvLineList();
      List<SCOFactinvLine> lineas = new ArrayList<SCOFactinvLine>();

      Organization org = null;
      for (int i = 0; i < lineasTotales.size(); i++) {

        lineas.add(lineasTotales.get(i));
        if (org == null)
          org = lineasTotales.get(i).getOrganization();
        else if (lineasTotales.get(i).getOrganization().getName().toUpperCase().contains("LIMA")) {
          org = lineasTotales.get(i).getOrganization();
        }

      }

      boolean isReceipt = true;
      String strAction = null;
      if (vars.commandIn("SAVEANDPROCESS")) {
        // The default option is process
        strAction = (isReceipt ? "PRP" : "PPP");
      } else {
        strAction = vars.getRequiredStringParameter("inpActionDocument");
      }

      String strSelectedScheduledPaymentDetailIds = vars
          .getInStringParameter("inpScheduledPaymentDetailId", "", null);
      String strAddedGLItems = vars.getStringParameter("inpGLItems");
      JSONArray addedGLITemsArray = null;
      try {
        addedGLITemsArray = new JSONArray(strAddedGLItems);
      } catch (JSONException e) {
        log4j.error("Error parsing received GLItems JSON Array: " + strAddedGLItems, e);
        bdErrorGeneralPopUp(request, response, "Error",
            "Error parsing received GLItems JSON Array: " + strAddedGLItems);
        return;
      }

      String strPaymentAmount = vars.getRequiredNumericParameter("inpActualPayment");
      String paymentCurrencyId = vars.getRequiredStringParameter("inpCurrencyId");
      BigDecimal exchangeRate = new BigDecimal("1");
      BigDecimal convertedAmount = new BigDecimal(strPaymentAmount);
      OBError message = null;
      String detracwith_message = "";

      // FIXME: added to access the FIN_PaymentSchedule and
      // FIN_PaymentScheduleDetail tables to be
      // removed when new security implementation is done
      OBContext.setAdminMode();
      try {

        List<FIN_PaymentScheduleDetail> selectedPaymentDetails = FIN_Utility
            .getOBObjectList(FIN_PaymentScheduleDetail.class, strSelectedScheduledPaymentDetailIds);
        HashMap<String, BigDecimal> selectedPaymentDetailAmounts = getSelectedPaymentDetailsAndAmount(
            vars, strSelectedScheduledPaymentDetailIds);

        final List<Object> parameters = new ArrayList<Object>();
        parameters.add(vars.getClient());
        parameters.add(org.getId());
        parameters.add((isReceipt ? "ARR" : "APP"));
        String strDocTypeId = (String) CallStoredProcedure.getInstance().call("AD_GET_DOCTYPE",
            parameters, null);
        String strDocBaseType = parameters.get(2).toString();

        FIN_FinancialAccount finaccount = factinv.getFinancialAccount();

        if (!FIN_Utility.isPeriodOpen(vars.getClient(), strDocBaseType,
            factinv.getOrganization().getId(), strPaymentDate)) {
          final OBError myMessage = Utility.translateError(this, vars, vars.getLanguage(),
              Utility.messageBD(this, "PeriodNotAvailable", vars.getLanguage()));
          vars.setMessage(strTabId, myMessage);
          printPageClosePopUp(response, vars);
          return;
        }

        String strPaymentDocumentNo = Utility.getDocumentNo(this, vars, "AddPaymentFromPrepayment",
            "FIN_Payment", strDocTypeId, strDocTypeId, false, true);
        FIN_Payment payment = dao.getNewPayment(isReceipt, org,
            dao.getObject(DocumentType.class, strDocTypeId), strPaymentDocumentNo, null,
            factinv.getPaymentMethod(), finaccount, strPaymentAmount,
            FIN_Utility.getDate(strPaymentDate), "",
            dao.getObject(Currency.class, paymentCurrencyId), exchangeRate, convertedAmount, null);

        // Remove edited lines which are not in final selection and
        // adjust outstanding amount for
        // documents
        removeNonSelectedDetails(payment, selectedPaymentDetails);
        BigDecimal newPaymentAmount = new BigDecimal(strPaymentAmount);
        if (newPaymentAmount.compareTo(payment.getAmount()) != 0) {
          payment.setAmount(newPaymentAmount);
          OBDal.getInstance().save(payment);
        }
        // load object in memory
        payment.getFINPaymentDetailList();
        if (addedGLITemsArray != null) {
          for (int i = 0; i < addedGLITemsArray.length(); i++) {
            JSONObject glItem = addedGLITemsArray.getJSONObject(i);
            BigDecimal glItemOutAmt = new BigDecimal(glItem.getString("glitemPaidOutAmt"));
            BigDecimal glItemInAmt = new BigDecimal(glItem.getString("glitemReceivedInAmt"));
            BigDecimal glItemAmt = BigDecimal.ZERO;

            String factoredInvId = glItem.getString("glitemFactoredInvId");

            String glItemDescription = glItem.getString("glitemDescription");
            if (isReceipt) {
              glItemAmt = glItemInAmt.subtract(glItemOutAmt);
            } else {
              glItemAmt = glItemOutAmt.subtract(glItemInAmt);
            }
            final String strGLItemId = glItem.getString("glitemId");
            checkID(strGLItemId);
            // Accounting Dimensions
            /*
             * final String strElement_BP = glItem.getString("cBpartnerDim");
             * checkID(strElement_BP); final BusinessPartner businessPartner =
             * dao.getObject(BusinessPartner.class, strElement_BP);
             * 
             * final String strElement_PR = glItem.getString("mProductDim"); checkID(strElement_PR);
             * final Product product = dao.getObject(Product.class, strElement_PR);
             * 
             * final String strElement_PJ = glItem.getString("cProjectDim"); checkID(strElement_PJ);
             * final Project project = dao.getObject(Project.class, strElement_PJ);
             * 
             * final String strElement_AY = glItem.getString("cActivityDim");
             * checkID(strElement_AY); final ABCActivity activity = dao.getObject(ABCActivity.class,
             * strElement_AY);
             */
            final String strElement_CC = glItem.getString("cCostcenterDim");
            checkID(strElement_CC);
            final Costcenter costCenter = dao.getObject(Costcenter.class, strElement_CC);
            /*
             * final String strElement_MC = glItem.getString("cCampaignDim");
             * checkID(strElement_MC); final Campaign campaign = dao.getObject(Campaign.class,
             * strElement_MC);
             * 
             * final String strElement_U1 = glItem.getString("user1Dim"); checkID(strElement_U1);
             * final UserDimension1 user1 = dao.getObject(UserDimension1.class, strElement_U1);
             * 
             * final String strElement_U2 = glItem.getString("user2Dim"); checkID(strElement_U2);
             * final UserDimension2 user2 = dao.getObject(UserDimension2.class, strElement_U2);
             */
            FIN_AddPayment.saveGLItemFactInv(payment, glItemAmt,
                dao.getObject(GLItem.class, strGLItemId), null, null, null, null, null, null,
                costCenter, null, null, glItemDescription, factoredInvId);
          }
        }
        FIN_AddPayment.setFinancialTransactionAmountAndRate(vars, payment, exchangeRate,
            convertedAmount);

        payment = FIN_AddPayment.savePayment(null, payment, isReceipt, null, null, null, null, null,
            strPaymentAmount, null, null, null, selectedPaymentDetails,
            selectedPaymentDetailAmounts, false, false,
            dao.getObject(Currency.class, paymentCurrencyId), exchangeRate, convertedAmount);

        /*
         * for(int i=0; i<lineas.size(); i++){ OBDal.getInstance().save(lineas.get(i)); }
         */

        payment.setScoIsmigrationpayment(false);
        OBDal.getInstance().save(payment);

        boolean processed = factinv.isProcessed();
        factinv.setProcessed(false);
        OBDal.getInstance().save(factinv);
        OBDal.getInstance().flush();

        OBDal.getInstance().refresh(factinv);

        factinv.setProcessed(processed);
        factinv.setPayment(payment);
        OBDal.getInstance().save(factinv);
        OBDal.getInstance().flush();

        if (strAction.equals("PRP") || strAction.equals("PPP") || strAction.equals("PRD")
            || strAction.equals("PPW")) {
          try {
            // Process just in case there are lines, empty Refund
            // payment does not need to call
            // process
            if (payment.getFINPaymentDetailList().size() > 0) {
              // If Action PRP o PPW, Process payment but as well
              // create a financial transaction
              message = FIN_AddPayment.processPayment(vars, this,
                  (strAction.equals("PRP") || strAction.equals("PPP")) ? "P" : "D", payment);
            }
            /*
             * if (strDifferenceAction.equals("refund") && (message == null ||
             * !"Error".equalsIgnoreCase(message.getType()))) { Boolean newPayment =
             * !payment.getFINPaymentDetailList().isEmpty(); FIN_Payment refundPayment =
             * FIN_AddPayment.createRefundPayment(this, vars, payment, refundAmount.negate(),
             * exchangeRate);
             * 
             * OBError auxMessage = FIN_AddPayment.processPayment(vars, this,
             * (strAction.equals("PRP") || strAction.equals("PPP")) ? "P" : "D", refundPayment);
             * 
             * if (newPayment) {
             * 
             * final String strNewRefundPaymentMessage = Utility.parseTranslation(this, vars,
             * vars.getLanguage(), "@APRM_RefundPayment@" + ": " + refundPayment.getDocumentNo()) +
             * "."; message.setMessage(strNewRefundPaymentMessage + " " + message.getMessage()); if
             * (payment.getGeneratedCredit( ).compareTo(BigDecimal.ZERO) != 0) {
             * payment.setDescription(payment.getDescription() + strNewRefundPaymentMessage + "\n");
             * OBDal.getInstance().save(payment); OBDal.getInstance().flush(); } } else { message =
             * auxMessage; } }
             */
          } catch (Exception ex) {
            message = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
            log4j.error(ex);
            if (!message.isConnectionAvailable()) {
              bdErrorConnection(response);
              return;
            }
          }
        }

      } catch (Exception ex) {
        String strMessage = FIN_Utility.getExceptionMessage(ex);
        if (message != null && "Error".equals(message.getType())) {
          strMessage = message.getMessage();
        }
        bdErrorGeneralPopUp(request, response, "Error", strMessage);
        OBDal.getInstance().rollbackAndClose();
        log4j.error("AddPaymentFromFactoringInvoice - SAVE AND PROCESS", ex);
        return;

      } finally {
        OBContext.restorePreviousMode();
      }

      if (detracwith_message != null && !detracwith_message.equals("")) {
        if (message.getMessage() != null && message.getMessage().equals(""))
          detracwith_message = "." + detracwith_message;

        message.setMessage(message.getMessage() + detracwith_message);
      }

      vars.setMessage(strTabId, message);
      printPageClosePopUpAndRefreshParent(response, vars);
    }

  }

  /*
   * Removes lines and schedule details which are not included in the given selected list
   */
  private void removeNonSelectedDetails(FIN_Payment payment,
      List<FIN_PaymentScheduleDetail> selectedPaymentDetails) {
    Set<String> toRemovePDs = new HashSet<String>();
    for (FIN_PaymentDetail pd : payment.getFINPaymentDetailList()) {
      for (FIN_PaymentScheduleDetail psd : pd.getFINPaymentScheduleDetailList()) {
        if (!selectedPaymentDetails.contains(psd)) {
          if (pd.getGLItem() != null) {
            toRemovePDs.add(pd.getId());
            continue;
          }
          // update outstanding amount
          List<FIN_PaymentScheduleDetail> outStandingPSDs = FIN_AddPayment.getOutstandingPSDs(psd);
          if (outStandingPSDs.size() == 0) {
            FIN_PaymentScheduleDetail newOutstanding = (FIN_PaymentScheduleDetail) DalUtil.copy(psd,
                false);
            newOutstanding.setPaymentDetails(null);
            newOutstanding.setWriteoffAmount(BigDecimal.ZERO);
            OBDal.getInstance().save(newOutstanding);
            toRemovePDs.add(pd.getId());
          } else {
            // First make sure outstanding amount is not equal zero
            if (outStandingPSDs.get(0).getAmount().add(psd.getAmount()).signum() == 0) {
              OBDal.getInstance().remove(outStandingPSDs.get(0));
            } else {
              // update existing PD with difference
              outStandingPSDs.get(0)
                  .setAmount(outStandingPSDs.get(0).getAmount().add(psd.getAmount()));
              outStandingPSDs.get(0).setDoubtfulDebtAmount(
                  outStandingPSDs.get(0).getDoubtfulDebtAmount().add(psd.getDoubtfulDebtAmount()));
              OBDal.getInstance().save(outStandingPSDs.get(0));
            }
            toRemovePDs.add(pd.getId());
          }
        }
      }
    }
    for (String pdID : toRemovePDs) {
      FIN_PaymentDetail pd = OBDal.getInstance().get(FIN_PaymentDetail.class, pdID);
      boolean hasPSD = pd.getFINPaymentScheduleDetailList().size() > 0;
      if (hasPSD) {
        FIN_PaymentScheduleDetail psd = OBDal.getInstance().get(FIN_PaymentScheduleDetail.class,
            pd.getFINPaymentScheduleDetailList().get(0).getId());
        pd.getFINPaymentScheduleDetailList().remove(psd);
        OBDal.getInstance().save(pd);
        OBDal.getInstance().remove(psd);
      }
      payment.getFINPaymentDetailList().remove(pd);
      OBDal.getInstance().save(payment);
      OBDal.getInstance().remove(pd);
      OBDal.getInstance().flush();
      OBDal.getInstance().refresh(payment);
    }
  }

  private void checkID(final String id) throws ServletException {
    if (!IsIDFilter.instance.accept(id)) {
      log4j.error("Input: " + id + " not accepted by filter: IsIDFilter");
      throw new ServletException("Input: " + id + " is not an accepted input");
    }
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars,
      String strScoFactoringinvoiceId, String strWindowId, String strTabId,
      String strFinancialAccountId) throws Exception {
    log4j.debug("Output: Add Payment button pressed on Make / Receipt Payment windows");

    OBContext.setAdminMode(true);
    try {
      SCOFactoringinvoice factinv = new AdvPaymentMngtDao().getObject(SCOFactoringinvoice.class,
          strScoFactoringinvoiceId);

      XmlDocument xmlDocument = xmlEngine
          .readXmlTemplate(
              "pe/com/unifiedgo/accounting/ad_actionbutton/AddPaymentFromFactoringInvoice")
          .createXmlDocument();

      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("theme", vars.getTheme());

      xmlDocument.setParameter("title",
          Utility.messageBD(this, "APRM_AddPaymentIn", vars.getLanguage()));

      List<SCOFactinvLine> lineasTotales = factinv.getSCOFactinvLineList();
      List<SCOFactinvLine> lineas = new ArrayList<SCOFactinvLine>();

      Organization org = null;
      for (int i = 0; i < lineasTotales.size(); i++) {

        lineas.add(lineasTotales.get(i));
        if (org == null)
          org = lineasTotales.get(i).getOrganization();
        else if (lineasTotales.get(i).getOrganization().getName().toUpperCase().contains("LIMA")) {
          org = lineasTotales.get(i).getOrganization();
        }

      }

      xmlDocument.setParameter("windowId", strWindowId);
      xmlDocument.setParameter("tabId", strTabId);
      xmlDocument.setParameter("strTabId", "strTabId =  \"" + strTabId + "\";\r\n");
      xmlDocument.setParameter("orgId", org.getId());
      xmlDocument.setParameter("paymentId", strScoFactoringinvoiceId);
      xmlDocument.setParameter("headerAmount", "0.00");
      xmlDocument.setParameter("isReceipt", "Y");
      xmlDocument.setParameter("isSoTrx", "Y");
      xmlDocument.setParameter("isSimple", "N");
      xmlDocument.setParameter("paymentDate", DateTimeData.today(this));
      xmlDocument.setParameter("dateDisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));

      xmlDocument.setParameter("generatedCredit", BigDecimal.ZERO.toString());

      final Currency financialAccountCurrency = factinv.getCurrency();
      if (financialAccountCurrency != null) {
        xmlDocument.setParameter("financialAccountCurrencyId", financialAccountCurrency.getId());
        xmlDocument.setParameter("financialAccountCurrencyName",
            financialAccountCurrency.getISOCode());
        xmlDocument.setParameter("financialAccountCurrencyPrecision",
            financialAccountCurrency.getStandardPrecision().toString());
      } else {
        xmlDocument.setParameter("financialAccountCurrencyPrecision", "2");

      }

      // xmlDocument.setParameter("exchangeRate", "");
      // xmlDocument.setParameter("actualConverted", "");
      // xmlDocument.setParameter("expectedConverted", "");
      xmlDocument.setParameter("currencyId", factinv.getCurrency().getId());
      xmlDocument.setParameter("currencyName", factinv.getCurrency().getISOCode());

      boolean isReceipt = true;
      boolean forcedFinancialAccountTransaction = FIN_AddPayment
          .isForcedFinancialAccountTransaction(factinv.getFinancialAccount(),
              factinv.getPaymentMethod(), isReceipt);

      // Action Regarding Document
      xmlDocument.setParameter("ActionDocument", "PPP");
      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
            (isReceipt ? "F903F726B41A49D3860243101CEEBA25" : "F15C13A199A748F1B0B00E985A64C036"),
            forcedFinancialAccountTransaction ? "29010995FD39439D97A5C0CE8CE27D70" : "",
            Utility.getContext(this, vars, "#AccessibleOrgTree", "AddPaymentFromInvoice"),
            Utility.getContext(this, vars, "#User_Client", "AddPaymentFromInvoice"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData,
            "AddPaymentFromFactoringInvoice", "");
        xmlDocument.setData("reportActionDocument", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      // Accounting Dimensions
      String doctype;
      doctype = AcctServer.DOCTYPE_ARReceipt;

      // GL Items

      ArrayList<GLItem> itemInvs = new ArrayList<GLItem>();
      // String defaultAccount = "1233102";
      for (int i = 0; i < lineas.size(); i++) {
        Invoice inv = lineas.get(i).getInvoiceref();

        /*
         * AccountingCombination comb = inv.getBusinessPartner().getCustomerAccountsList
         * ().get(0).getSCODiscountBillOfExchangeAcc();
         * 
         * // search in the sco_posting_mapping table for a match
         * 
         * final StringBuilder whereClause = new StringBuilder(); whereClause.append(" as cusa ");
         * whereClause.append(" where ad_isorgincluded('" + inv.getOrganization().getId() +
         * "',cusa.organization.id,'" + inv.getClient().getId() + "')<>-1");
         * 
         * final OBQuery<AcctSchema> obqParameters =
         * OBDal.getInstance().createQuery(AcctSchema.class, whereClause.toString());
         * obqParameters.setFilterOnReadableClients(false);
         * obqParameters.setFilterOnReadableOrganization(false); AcctSchema accSchema =
         * obqParameters.list().get(0);
         * 
         * OBCriteria<SCOPostingMapping> posting_mapping_c =
         * OBDal.getInstance().createCriteria(SCOPostingMapping.class);
         * posting_mapping_c.setFilterOnReadableClients(false);
         * posting_mapping_c.setFilterOnReadableOrganization(false); posting_mapping_c
         * .add(Restrictions.eq(SCOPostingMapping.PROPERTY_CLIENT, inv.getClient()));
         * posting_mapping_c.add(Restrictions.eq(SCOPostingMapping
         * .PROPERTY_GENERALLEDGERTOBEMAPPED, accSchema));
         * posting_mapping_c.add(Restrictions.eq(SCOPostingMapping.
         * PROPERTY_ACCOUNTTHATTRIGGERSMAPPING, comb));
         * posting_mapping_c.add(Restrictions.eq(SCOPostingMapping.
         * PROPERTY_ORGANIZATIONTHATTRIGGERSMAPPING, inv.getOrganization()));
         * 
         * List<SCOPostingMapping> posting_mappings = posting_mapping_c.list();
         * 
         * AccountingCombination combination = comb; if (posting_mappings.size() != 0) {// hay
         * cuenta para modificar combination = posting_mappings.get(0).getReplacementAccount(); }
         */
        final StringBuilder whereClausePuente = new StringBuilder();
        whereClausePuente.append(" as cusa ");
        whereClausePuente
            .append(" where cusa.businessPartner.id = '" + inv.getBusinessPartner().getId() + "'");

        final OBQuery<CustomerAccounts> obqParametersPuente = OBDal.getInstance()
            .createQuery(CustomerAccounts.class, whereClausePuente.toString());
        obqParametersPuente.setFilterOnReadableClients(false);
        obqParametersPuente.setFilterOnReadableOrganization(false);
        final List<CustomerAccounts> customerAccounts = obqParametersPuente.list();

        AccountingCombination combination = customerAccounts.get(0).getScoPuentefactinvAcct();

        String stmGlitem = "SELECT c_glitem_id FROM c_glitem_acct WHERE glitem_debit_acct=?";
        String glitemId = "";
        ResultSet result;
        Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
        PreparedStatement st = null;

        int iParameter = 0;
        try {
          st = this.getPreparedStatement(stmGlitem);
          QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
          iParameter++;
          UtilSql.setValue(st, iParameter, 12, null, combination.getId());
          result = st.executeQuery();
          if (result.next()) {
            glitemId = result.getString("c_glitem_id");
          }
          result.close();
        } catch (SQLException e) {
          log4j.error("SQL error in query: " + stmGlitem + "Exception:" + e);
          throw new ServletException(
              "@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
        } catch (Exception ex) {
          log4j.error("Exception in query: " + stmGlitem + "Exception:" + ex);
          throw new ServletException("@CODE=@" + ex.getMessage());
        } finally {
          try {
            this.releasePreparedStatement(st);
          } catch (Exception ignore) {
            ignore.printStackTrace();
          }
        }

        GLItem itemInv = OBDal.getInstance().get(GLItem.class, glitemId);
        if (itemInv != null) {

        } else {
          throw new Exception("Cuenta para inv " + inv.getScrPhysicalDocumentno() + " no existe.");
        }

        itemInvs.add(itemInv);
      }

      // Accounting Dimensions
      final String strCentrally = Utility.getContext(this, vars,
          DimensionDisplayUtility.IsAcctDimCentrally, strWindowId);
      /*
       * final String strElement_BP = "Y"; Utility.getContext(this, vars,
       * DimensionDisplayUtility.displayAcctDimensions(strCentrally,
       * DimensionDisplayUtility.DIM_BPartner, doctype, DimensionDisplayUtility.DIM_Header),
       * strWindowId); final String strElement_PR = Utility.getContext(this, vars,
       * DimensionDisplayUtility.displayAcctDimensions(strCentrally,
       * DimensionDisplayUtility.DIM_Product, doctype, DimensionDisplayUtility.DIM_Header),
       * strWindowId); final String strElement_PJ = Utility.getContext(this, vars,
       * DimensionDisplayUtility.displayAcctDimensions(strCentrally,
       * DimensionDisplayUtility.DIM_Project, doctype, DimensionDisplayUtility.DIM_Header),
       * strWindowId); final String strElement_AY = Utility.getContext(this, vars, "$Element_AY",
       * strWindowId); // final String strElement_CC = Utility.getContext(this, vars, //
       * DimensionDisplayUtility.displayAcctDimensions(strCentrally, //
       * DimensionDisplayUtility.DIM_CostCenter, doctype, DimensionDisplayUtility.DIM_Header), //
       * strWindowId);
       */
      final String strElement_CC = "Y";
      /*
       * final String strElement_ID = "Y"; final String strElement_IG = "Y"; final String
       * strElement_MC = Utility.getContext(this, vars, "$Element_MC", strWindowId); final String
       * strElement_U1 = Utility.getContext(this, vars,
       * DimensionDisplayUtility.displayAcctDimensions(strCentrally,
       * DimensionDisplayUtility.DIM_User1, doctype, DimensionDisplayUtility.DIM_Header),
       * strWindowId); final String strElement_U2 = Utility.getContext(this, vars,
       * DimensionDisplayUtility.displayAcctDimensions(strCentrally,
       * DimensionDisplayUtility.DIM_User2, doctype, DimensionDisplayUtility.DIM_Header),
       * strWindowId); xmlDocument.setParameter("strElement_BP", strElement_BP);
       * xmlDocument.setParameter("strElement_PR", strElement_PR);
       * xmlDocument.setParameter("strElement_PJ", strElement_PJ);
       * xmlDocument.setParameter("strElement_AY", strElement_AY);
       */
      xmlDocument.setParameter("strElement_CC", strElement_CC);
      /*
       * xmlDocument.setParameter("strElement_ID", strElement_ID);
       * xmlDocument.setParameter("strElement_IG", strElement_IG);
       * xmlDocument.setParameter("strElement_MC", strElement_MC);
       * xmlDocument.setParameter("strElement_U1", strElement_U1);
       * xmlDocument.setParameter("strElement_U2", strElement_U2);
       */

      // Add GL Items
      JSONArray addedGLITemsArray = new JSONArray();
      int i = 0;
      for (GLItem itemInv : itemInvs) {

        try {
          JSONObject glItem = new JSONObject();
          glItem.put("glitemId", itemInv.getId());
          glItem.put("glitemDesc", itemInv.getIdentifier());
          // Amounts
          glItem.put("glitemPaidOutAmt", BigDecimal.ZERO);
          glItem.put("glitemReceivedInAmt", lineas.get(i).getInvoiceref().getGrandTotalAmount());
          glItem.put("glitemDescription",
              "Por cobro de inv " + lineas.get(i).getInvoiceref().getScrPhysicalDocumentno());
          glItem.put("glitemFactoredInvId", lineas.get(i).getInvoiceref().getId());
          glItem.put("cCostcenterDim", "");
          glItem.put("cCostcenterDimDesc", "");
          i++;
          addedGLITemsArray.put(glItem);
        } catch (JSONException e) {
          log4j.error(e);
        }
      }
      xmlDocument.setParameter("glItems",
          addedGLITemsArray.toString().replace("'", "").replaceAll("\"", "'"));
      // If UsedCredit is not equal zero, check Use available credit
      xmlDocument.setParameter("useCredit", "N");

      // Not allow to change exchange rate and amount
      final String strNotAllowExchange = Utility.getContext(this, vars, "NotAllowChangeExchange",
          strWindowId);
      xmlDocument.setParameter("strNotAllowExchange", strNotAllowExchange);

      dao = new AdvPaymentMngtDao();

      response.setContentType("text/html; charset=UTF-8");
      PrintWriter out = response.getWriter();

      out.println(xmlDocument.print());
      out.close();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private void printGrid(HttpServletResponse response, VariablesSecureApp vars,
      String strBusinessPartnerId, String strPaymentId, String strOrgId, String strExpectedDateFrom,
      String strExpectedDateTo, String strDocumentType, String strSelectedPaymentDetails,
      boolean isReceipt, boolean showAlternativePM, String strDocNo,
      boolean showAllFinancialAcounts, String strTabId) throws IOException, ServletException {

    log4j.debug("Output: Grid with pending payments");
    dao = new AdvPaymentMngtDao();
    String[] discard = { "discard", "discard", "discard" };
    if (!"".equals(vars.getRequestGlobalVariable("inpBusinessPartnerId", ""))) {
      discard[0] = "businessPartnerName";
    }
    if (isReceipt) {
      discard[1] = "inpInvoiceDetRetAmount";
      discard[2] = "inpDescription";
    } else if (!isReceipt) {
      discard[1] = "fieldPaymentMethod";
    }

    XmlDocument xmlDocument = xmlEngine
        .readXmlTemplate("org/openbravo/advpaymentmngt/ad_actionbutton/AddPaymentGrid", discard)
        .createXmlDocument();

    FIN_Payment payment = dao.getObject(FIN_Payment.class, strPaymentId);

    List<FIN_PaymentScheduleDetail> storedScheduledPaymentDetails = new ArrayList<FIN_PaymentScheduleDetail>();
    // This is to identify first load of the grid
    String strFirstLoad = vars.getStringParameter("isFirstLoad");
    if (payment.getFINPaymentDetailList().size() > 0) {
      // Add payment schedule details related to orders or invoices to
      // storedSchedulePaymentDetails
      OBContext.setAdminMode();
      try {
        OBCriteria<FIN_PaymentScheduleDetail> obc = OBDal.getInstance()
            .createCriteria(FIN_PaymentScheduleDetail.class);
        obc.add(Restrictions.in(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS,
            payment.getFINPaymentDetailList()));
        obc.add(Restrictions.or(
            Restrictions.isNotNull(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE),
            Restrictions.isNotNull(FIN_PaymentScheduleDetail.PROPERTY_ORDERPAYMENTSCHEDULE)));

        storedScheduledPaymentDetails = obc.list();
      } finally {
        OBContext.restorePreviousMode();
      }
    }
    // Pending Payments from invoice
    final List<FIN_PaymentScheduleDetail> selectedScheduledPaymentDetails = FIN_AddPayment
        .getSelectedPaymentDetails("true".equals(strFirstLoad)
            ? new ArrayList<FIN_PaymentScheduleDetail>(storedScheduledPaymentDetails)
            : null, strSelectedPaymentDetails);

    // filtered scheduled payments list
    List<FIN_PaymentScheduleDetail> filteredScheduledPaymentDetails = null;
    filteredScheduledPaymentDetails = dao.getFilteredScheduledPaymentDetails(
        dao.getObject(Organization.class, strOrgId),
        dao.getObject(BusinessPartner.class, strBusinessPartnerId), payment.getCurrency(), null,
        null, FIN_Utility.getDate(strExpectedDateFrom),
        FIN_Utility.getDate(DateTimeData.nDaysAfter(this, strExpectedDateTo, "1")), null, null,
        strDocumentType, "", showAlternativePM ? null : payment.getPaymentMethod(), "",
        selectedScheduledPaymentDetails, isReceipt, payment.getPaymentDate(), strDocNo,
        payment.isSCOIsSimpleProvision(), payment.isScoIsapppayment(), showAllFinancialAcounts,
        payment.getAccount(), strTabId, false, null);
    // Remove related outstanding schedule details related to those ones
    // being edited as amount will
    // be later added to storedScheduledPaymentDetails
    for (FIN_PaymentScheduleDetail psd : storedScheduledPaymentDetails) {
      filteredScheduledPaymentDetails.removeAll(FIN_AddPayment.getOutstandingPSDs(psd));
    }
    // Get stored not selected PSDs
    List<FIN_PaymentScheduleDetail> storedNotSelectedPSDs = new ArrayList<FIN_PaymentScheduleDetail>(
        storedScheduledPaymentDetails);
    storedNotSelectedPSDs.removeAll(selectedScheduledPaymentDetails);
    // Add stored but not selected details which maps documenttype
    filteredScheduledPaymentDetails
        .addAll(filterDocumenttype(storedNotSelectedPSDs, strDocumentType));

    FieldProvider[] data = FIN_AddPayment.getShownScheduledPaymentDetails(vars,
        selectedScheduledPaymentDetails, filteredScheduledPaymentDetails, false, null,
        strSelectedPaymentDetails, false, null, null, null, isReceipt);

    for (FIN_PaymentScheduleDetail psd : storedScheduledPaymentDetails) {
      // Calculate pending amount
      BigDecimal outstandingAmount = BigDecimal.ZERO;
      List<FIN_PaymentScheduleDetail> outStandingPSDs = FIN_AddPayment.getOutstandingPSDs(psd);
      if (outStandingPSDs.size() != 0) {
        for (FIN_PaymentScheduleDetail outPSD : outStandingPSDs) {
          outstandingAmount = outstandingAmount.add(outPSD.getAmount());
        }
      }
      for (int i = 0; i < data.length; i++) {
        if (data[i].getField("finScheduledPaymentDetailId").equals(psd.getId())) {
          FieldProviderFactory.setField(data[i], "outstandingAmount",
              psd.getAmount().add(outstandingAmount).toPlainString());
          if ("true".equals(strFirstLoad)) {
            FieldProviderFactory.setField(data[i], "difference", outstandingAmount.toPlainString());
            FieldProviderFactory.setField(data[i], "paymentAmount",
                psd.getAmount().toPlainString());

            FieldProviderFactory.setField(data[i], "description", psd.getSCODescription());
          }
        }
      }

    }
    data = groupPerDocumentType(data, strDocumentType);
    xmlDocument.setData("structure", (data == null) ? set() : data);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private FieldProvider[] set() throws ServletException {
    HashMap<String, String> empty = new HashMap<String, String>();
    empty.put("finScheduledPaymentId", "");
    empty.put("salesOrderNr", "");
    empty.put("salesInvoiceNr", "");
    empty.put("expectedDate", "");
    empty.put("invoicedAmount", "");
    empty.put("expectedAmount", "");
    empty.put("paymentAmount", "");
    ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
    result.add(empty);
    return FieldProviderFactory.getFieldProviderArray(result);
  }

  private FieldProvider[] groupPerDocumentType(FieldProvider[] data, String strDocumenType) {
    ArrayList<FieldProvider> gridLines = new ArrayList<FieldProvider>();
    HashMap<String, Integer> amountsPerGroupingField = new HashMap<String, Integer>();
    String groupingField = "finScheduledPaymentDetailId";
    if ("I".equals(strDocumenType)) {
      groupingField = "invoicePaymentScheduleId";
    } else if ("O".equals(strDocumenType)) {
      groupingField = "orderPaymentScheduleId";
    }
    for (int i = 0; i < data.length; i++) {
      if (!amountsPerGroupingField.containsKey(data[i].getField(groupingField))
          || "".equals(data[i].getField(groupingField))) {
        amountsPerGroupingField.put(data[i].getField(groupingField), gridLines.size());
        FieldProviderFactory.setField(data[i], "rownum", String.valueOf(gridLines.size()));
        gridLines.add(data[i]);
      } else {
        Integer listIndex = amountsPerGroupingField.get(data[i].getField(groupingField));
        FieldProvider row = gridLines.get(listIndex);
        FieldProviderFactory.setField(row, "finScheduledPaymentDetailId",
            row.getField("finScheduledPaymentDetailId") + ","
                + data[i].getField("finScheduledPaymentDetailId"));
        FieldProviderFactory.setField(row, "finSelectedPaymentDetailId",
            row.getField("finSelectedPaymentDetailId") + ","
                + data[i].getField("finScheduledPaymentDetailId"));
        FieldProviderFactory.setField(row, "outstandingAmount",
            new BigDecimal(row.getField("outstandingAmount"))
                .add(new BigDecimal(data[i].getField("outstandingAmount"))).toString());
        BigDecimal payAmount = BigDecimal.ZERO;
        if (!"".equals(row.getField("paymentAmount"))) {
          payAmount = new BigDecimal(row.getField("paymentAmount"));
        }
        FieldProviderFactory.setField(row, "paymentAmount",
            !"".equals(data[i].getField("paymentAmount"))
                ? payAmount.add(new BigDecimal(data[i].getField("paymentAmount"))).toString()
                : (payAmount.compareTo(BigDecimal.ZERO) == 0 ? "" : payAmount.toString()));
        if ("O".equals(strDocumenType)) {
          String strGroupedInvoicesNr = row.getField("invoiceNr");
          FieldProviderFactory.setField(row, "invoiceNr",
              (strGroupedInvoicesNr.isEmpty() ? "" : strGroupedInvoicesNr + ", ")
                  + data[i].getField("invoiceNr"));
          String invoiceNumber = row.getField("invoiceNr");
          String invoiceNumberTrunc = (invoiceNumber.length() > 17)
              ? invoiceNumber.substring(0, 14).concat("...").toString()
              : invoiceNumber;
          FieldProviderFactory.setField(row, "invoiceNrTrunc", invoiceNumberTrunc);
        } else if ("I".equals(strDocumenType)) {
          String strGroupedOrdersNr = row.getField("orderNr");
          FieldProviderFactory.setField(row, "orderNr",
              (strGroupedOrdersNr.isEmpty() ? "" : strGroupedOrdersNr + ", ")
                  + data[i].getField("orderNr"));
          String orderNumber = row.getField("orderNr");
          String orderNumberTrunc = (orderNumber.length() > 17)
              ? orderNumber.substring(0, 14).concat("...").toString()
              : orderNumber;
          FieldProviderFactory.setField(row, "orderNrTrunc", orderNumberTrunc);
        }
      }
    }
    FieldProvider[] result = new FieldProvider[gridLines.size()];
    gridLines.toArray(result);
    return result;
  }

  /**
   * Creates a HashMap with the FIN_PaymentScheduleDetail id's and the amount gotten from the
   * Session.
   * 
   * The amounts are stored in Session like "inpPaymentAmount"+paymentScheduleDetail.Id
   * 
   * @param vars
   *          VariablseSecureApp with the session data.
   * @param selectedPaymentScheduleDetails
   *          List of FIN_PaymentScheduleDetails that need to be included in the HashMap.
   * @return A HashMap mapping the FIN_PaymentScheduleDetail's Id with the corresponding amount.
   */
  private HashMap<String, BigDecimal> getSelectedPaymentDetailsAndAmount(VariablesSecureApp vars,
      String _strSelectedScheduledPaymentDetailIds) throws ServletException {
    String strSelectedScheduledPaymentDetailIds = _strSelectedScheduledPaymentDetailIds;
    // Remove "(" ")"
    strSelectedScheduledPaymentDetailIds = strSelectedScheduledPaymentDetailIds.replace("(", "");
    strSelectedScheduledPaymentDetailIds = strSelectedScheduledPaymentDetailIds.replace(")", "");
    HashMap<String, BigDecimal> selectedPaymentScheduleDetailsAmounts = new HashMap<String, BigDecimal>();
    // As selected items may contain records with multiple IDs we as well
    // need the records list as
    // amounts are related to records
    StringTokenizer records = new StringTokenizer(strSelectedScheduledPaymentDetailIds, "'");
    Set<String> recordSet = new LinkedHashSet<String>();
    while (records.hasMoreTokens()) {
      recordSet.add(records.nextToken());
    }
    for (String record : recordSet) {
      if (", ".equals(record)) {
        continue;
      }
      Set<String> psdSet = new LinkedHashSet<String>();
      StringTokenizer psds = new StringTokenizer(record, ",");
      while (psds.hasMoreTokens()) {
        psdSet.add(psds.nextToken());
      }
      BigDecimal recordAmount = new BigDecimal(
          vars.getNumericParameter("inpPaymentAmount" + record, ""));
      HashMap<String, BigDecimal> recordsAmounts = calculateAmounts(recordAmount, psdSet);
      selectedPaymentScheduleDetailsAmounts.putAll(recordsAmounts);
    }
    return selectedPaymentScheduleDetailsAmounts;
  }

  /**
   * Creates a HashMap with the FIN_PaymentScheduleDetail id's and the detracction/withholding
   * amount gotten from the Session.
   * 
   * The amounts are stored in Session like "inpPaymentAmount"+paymentScheduleDetail.Id
   * 
   * @param vars
   *          VariablseSecureApp with the session data.
   * @param selectedPaymentScheduleDetails
   *          List of FIN_PaymentScheduleDetails that need to be included in the HashMap.
   * @return A HashMap mapping the FIN_PaymentScheduleDetail's Id with the corresponding amount.
   */
  private HashMap<String, BigDecimal> getSelectedPaymentDetailsAndDetWithhoAmount(
      VariablesSecureApp vars, String _strSelectedScheduledPaymentDetailIds)
      throws ServletException {
    String strSelectedScheduledPaymentDetailIds = _strSelectedScheduledPaymentDetailIds;
    // Remove "(" ")"
    strSelectedScheduledPaymentDetailIds = strSelectedScheduledPaymentDetailIds.replace("(", "");
    strSelectedScheduledPaymentDetailIds = strSelectedScheduledPaymentDetailIds.replace(")", "");
    HashMap<String, BigDecimal> selectedPaymentScheduleDetailsAmounts = new HashMap<String, BigDecimal>();
    // As selected items may contain records with multiple IDs we as well
    // need the records list as
    // amounts are related to records
    StringTokenizer records = new StringTokenizer(strSelectedScheduledPaymentDetailIds, "'");
    Set<String> recordSet = new LinkedHashSet<String>();
    while (records.hasMoreTokens()) {
      recordSet.add(records.nextToken());
    }
    for (String record : recordSet) {
      if (", ".equals(record)) {
        continue;
      }
      Set<String> psdSet = new LinkedHashSet<String>();
      StringTokenizer psds = new StringTokenizer(record, ",");
      while (psds.hasMoreTokens()) {
        psdSet.add(psds.nextToken());
      }
      BigDecimal recordAmount = new BigDecimal(
          vars.getNumericParameter("inpInvoiceDetRetAmount" + record, ""));
      HashMap<String, BigDecimal> recordsAmounts = calculateAmounts(recordAmount, psdSet);
      selectedPaymentScheduleDetailsAmounts.putAll(recordsAmounts);
    }
    return selectedPaymentScheduleDetailsAmounts;
  }

  private HashMap<String, String> getSelectedPaymentDetailsDescriptions(VariablesSecureApp vars,
      String _strSelectedScheduledPaymentDetailIds) throws ServletException {
    String strSelectedScheduledPaymentDetailIds = _strSelectedScheduledPaymentDetailIds;
    // Remove "(" ")"
    strSelectedScheduledPaymentDetailIds = strSelectedScheduledPaymentDetailIds.replace("(", "");
    strSelectedScheduledPaymentDetailIds = strSelectedScheduledPaymentDetailIds.replace(")", "");
    HashMap<String, String> selectedPaymentScheduleDetailsDescriptions = new HashMap<String, String>();
    // As selected items may contain records with multiple IDs we as well
    // need the records list as
    // amounts are related to records
    StringTokenizer records = new StringTokenizer(strSelectedScheduledPaymentDetailIds, "'");
    Set<String> recordSet = new LinkedHashSet<String>();
    while (records.hasMoreTokens()) {
      recordSet.add(records.nextToken());
    }
    for (String record : recordSet) {
      if (", ".equals(record)) {
        continue;
      }
      Set<String> psdSet = new LinkedHashSet<String>();
      StringTokenizer psds = new StringTokenizer(record, ",");
      while (psds.hasMoreTokens()) {
        psdSet.add(psds.nextToken());
      }
      String recordDescription = vars.getStringParameter("inpDescription" + record, "");
      HashMap<String, String> recordsAmounts = calculateDescriptions(recordDescription, psdSet);
      selectedPaymentScheduleDetailsDescriptions.putAll(recordsAmounts);
    }
    return selectedPaymentScheduleDetailsDescriptions;
  }

  /**
   * This method returns a HashMap with pairs of UUID of payment schedule details and amounts
   * related to those ones.
   * 
   * @param recordAmount
   *          : amount to split among the set
   * @param psdSet
   *          : set of payment schedule details where to allocate the amount
   * @return
   */
  private HashMap<String, BigDecimal> calculateAmounts(BigDecimal recordAmount,
      Set<String> psdSet) {
    BigDecimal remainingAmount = recordAmount;
    HashMap<String, BigDecimal> recordsAmounts = new HashMap<String, BigDecimal>();
    // PSD needs to be properly ordered to ensure negative amounts are
    // processed first
    List<FIN_PaymentScheduleDetail> psds = getOrderedPaymentScheduleDetails(psdSet);
    BigDecimal outstandingAmount = BigDecimal.ZERO;
    for (FIN_PaymentScheduleDetail paymentScheduleDetail : psds) {
      if (paymentScheduleDetail.getPaymentDetails() != null) {
        // This schedule detail comes from an edited payment so
        // outstanding amount needs to be
        // properly calculated
        List<FIN_PaymentScheduleDetail> outStandingPSDs = FIN_AddPayment
            .getOutstandingPSDs(paymentScheduleDetail);
        if (outStandingPSDs.size() > 0) {
          outstandingAmount = paymentScheduleDetail.getAmount()
              .add(outStandingPSDs.get(0).getAmount());
        } else {
          outstandingAmount = paymentScheduleDetail.getAmount();
        }
      } else {
        outstandingAmount = paymentScheduleDetail.getAmount();
      }
      // Manage negative amounts
      if ((remainingAmount.compareTo(BigDecimal.ZERO) > 0
          && remainingAmount.compareTo(outstandingAmount) >= 0)
          || ((remainingAmount.compareTo(BigDecimal.ZERO) == -1
              && outstandingAmount.compareTo(BigDecimal.ZERO) == -1)
              && (remainingAmount.compareTo(outstandingAmount) <= 0))) {
        recordsAmounts.put(paymentScheduleDetail.getId(), outstandingAmount);
        remainingAmount = remainingAmount.subtract(outstandingAmount);
      } else {
        recordsAmounts.put(paymentScheduleDetail.getId(), remainingAmount);
        remainingAmount = BigDecimal.ZERO;
      }

    }
    return recordsAmounts;
  }

  private HashMap<String, String> calculateDescriptions(String recordDescription,
      Set<String> psdSet) {
    // PSD needs to be properly ordered to ensure negative amounts are
    // processed first
    HashMap<String, String> recordsDescriptions = new HashMap<String, String>();

    List<FIN_PaymentScheduleDetail> psds = getOrderedPaymentScheduleDetails(psdSet);
    BigDecimal outstandingAmount = BigDecimal.ZERO;
    for (FIN_PaymentScheduleDetail paymentScheduleDetail : psds) {
      recordsDescriptions.put(paymentScheduleDetail.getId(), recordDescription);
    }
    return recordsDescriptions;
  }

  private List<FIN_PaymentScheduleDetail> getOrderedPaymentScheduleDetails(Set<String> psdSet) {
    OBCriteria<FIN_PaymentScheduleDetail> orderedPSDs = OBDal.getInstance()
        .createCriteria(FIN_PaymentScheduleDetail.class);
    orderedPSDs.add(Restrictions.in(FIN_PaymentScheduleDetail.PROPERTY_ID, psdSet));
    orderedPSDs.addOrderBy(FIN_PaymentScheduleDetail.PROPERTY_AMOUNT, true);
    return orderedPSDs.list();
  }

  private List<FIN_PaymentScheduleDetail> filterDocumenttype(
      List<FIN_PaymentScheduleDetail> storedNotSelectedPSDs, String strDocumentType) {
    List<FIN_PaymentScheduleDetail> listIterator = new ArrayList<FIN_PaymentScheduleDetail>(
        storedNotSelectedPSDs);
    for (FIN_PaymentScheduleDetail paymentScheduleDetail : listIterator) {
      if (paymentScheduleDetail.getInvoicePaymentSchedule() != null
          && "O".equals(strDocumentType)) {
        storedNotSelectedPSDs.remove(paymentScheduleDetail);
      } else if (paymentScheduleDetail.getOrderPaymentSchedule() != null
          && "I".equals(strDocumentType)) {
        storedNotSelectedPSDs.remove(paymentScheduleDetail);
      }
    }
    return storedNotSelectedPSDs;
  }

  private void businessPartnerBlocked(HttpServletResponse response, VariablesSecureApp vars,
      String strBPartnerName) throws IOException, ServletException {

    try {
      JSONObject json = new JSONObject();
      json.put("text", "SelectedBPartnerBlocked");
      response.setContentType("text/html; charset=UTF-8");
      PrintWriter out = response.getWriter();
      out.println("objson = " + json);
      out.close();
    } catch (JSONException e) {
      log4j.error("AddPaymentFromFactoringInvoice - Callback", e);
    }
  }

  public static String getListInvoiceRef(String strOrgId, String partnerId, boolean isMandatory,
      boolean isReceipt) {

    OBCriteria<Invoice> psdFilter = OBDal.getInstance().createCriteria(Invoice.class);
    psdFilter.add(Restrictions.eq(Invoice.PROPERTY_BUSINESSPARTNER,
        OBDal.getInstance().get(BusinessPartner.class, partnerId)));

    if (isReceipt)
      psdFilter.add(Restrictions.eq(Invoice.PROPERTY_SALESTRANSACTION, true));
    else
      psdFilter.add(Restrictions.eq(Invoice.PROPERTY_SALESTRANSACTION, false));

    List<Invoice> invoices = psdFilter.list();

    String options = FIN_Utility.getOptionsList(invoices, "", isMandatory);
    return options;
  }

  public String getServletInfo() {
    return "Servlet that presents the payment proposal";
    // end of getServletInfo() method
  }

}