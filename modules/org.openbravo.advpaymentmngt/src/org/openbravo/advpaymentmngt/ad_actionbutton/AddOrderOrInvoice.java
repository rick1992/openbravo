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
package org.openbravo.advpaymentmngt.ad_actionbutton;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

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
import org.openbravo.base.exception.OBException;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.DimensionDisplayUtility;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.currency.ConversionRate;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.financialmgmt.accounting.Costcenter;
import org.openbravo.model.financialmgmt.accounting.UserDimension1;
import org.openbravo.model.financialmgmt.accounting.UserDimension2;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.marketing.Campaign;
import org.openbravo.model.materialmgmt.cost.ABCActivity;
import org.openbravo.model.project.Project;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.accounting.SCO_Utils;
import pe.com.unifiedgo.accounting.data.SCOEEFFCashflow;
import pe.com.unifiedgo.accounting.data.SCOInternalDoc;
import pe.com.unifiedgo.accounting.data.SCOPrepayment;
import pe.com.unifiedgo.accounting.data.ScoRendicioncuentas;
import pe.com.unifiedgo.core.data.SCRComboItem;
import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class AddOrderOrInvoice extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private AdvPaymentMngtDao dao;
  private static final RequestFilter filterYesNo = new ValueListFilter("Y", "N", "");

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    dao = new AdvPaymentMngtDao();

    if (vars.commandIn("DEFAULT")) {
      String strWindowId = vars.getGlobalVariable("inpwindowId", "AddOrderOrInvoice|Window_ID");
      String strTabId = vars.getGlobalVariable("inpTabId", "AddOrderOrInvoice|Tab_ID");
      String strPaymentId = vars.getGlobalVariable("inpfinPaymentId",
          strWindowId + "|" + "FIN_Payment_ID");
      String strFinancialAccountId = vars.getStringParameter("inpfinFinancialAccountId");

      FIN_Payment payment = OBDal.getInstance().get(FIN_Payment.class, strPaymentId);
      String strSsaPaymentInDocType = (payment != null) ? payment.getSsaPaymentinDoctype() : null;

      try {
        printPage(response, vars, strPaymentId, strWindowId, strTabId, strFinancialAccountId,
            strSsaPaymentInDocType, true);
      } catch (Exception e) {
        bdErrorGeneralPopUp(request, response, "Error", e.getMessage());
      }

    } else if (vars.commandIn("GRIDLIST")) {

      String strBusinessPartnerId = vars.getRequestGlobalVariable("inpBusinessPartnerId", "");
      if ("".equals(strBusinessPartnerId)) {
        strBusinessPartnerId = vars.getRequestGlobalVariable("inpcBPartnerId", "");
      }
      String strOrgId = vars.getRequestGlobalVariable("inpadOrgId", "");
      String strPaymentId = vars.getRequestGlobalVariable("inpfinPaymentId", "");
      String strExpectedDateFrom = vars.getStringParameter("inpExpectedDateFrom", "");
      String strExpectedDateTo = vars.getStringParameter("inpExpectedDateTo", "");
      String strDocNo = vars.getRequestGlobalVariable("inpOrderDocNo", "");
      String strDocumentType = vars.getStringParameter("inpDocumentType", "");
      String strSelectedPaymentDetails = vars.getInStringParameter("inpScheduledPaymentDetailId",
          "", null);
      boolean isReceipt = vars.getRequiredStringParameter("isReceipt").equals("Y");
      Boolean showAlternativePM = "Y"
          .equals(vars.getStringParameter("inpAlternativePaymentMethod", filterYesNo));
      Boolean showAllFinancialAccount = "Y"
          .equals(vars.getStringParameter("inpAllFinancialAccount", filterYesNo));
      String strTabId = vars.getStringParameter("inpTabId", "");

      BigDecimal exchangeRate = new BigDecimal(
          vars.getRequiredNumericParameter("inpExchangeRate", "1"));

      String strSsaPaymentInDocType = vars.getRequestGlobalVariable("inpPaymentInDocType", "");

      String strCContable = vars.getRequestGlobalVariable("inpCContable", "");

      printGrid(response, vars, strBusinessPartnerId, strPaymentId, strOrgId, strExpectedDateFrom,
          strExpectedDateTo, strDocumentType, strSelectedPaymentDetails, isReceipt,
          showAlternativePM, strDocNo, showAllFinancialAccount, strTabId, exchangeRate,
          strSsaPaymentInDocType, strCContable);

      /*
       * } else if (vars.commandIn("FILLINVOICEREF")) { boolean isReceipt =
       * vars.getRequiredStringParameter("isReceipt").equals("Y"); String strOrgId =
       * vars.getRequestGlobalVariable("inpadOrgId", ""); String strBusinessPartnerId =
       * vars.getRequestGlobalVariable("inpBusinessPartnerId", ""); refreshInvoiceRefCombo(response,
       * strOrgId, strBusinessPartnerId, isReceipt);
       */
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
              if (psd == null)
                continue;
              BusinessPartner bPartner = null;
              if (psd.getOrderPaymentSchedule() != null) {
                bPartner = psd.getOrderPaymentSchedule().getOrder().getBusinessPartner();
              } else if (psd.getInvoicePaymentSchedule() != null) {
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

      boolean isReceipt = vars.getRequiredStringParameter("isReceipt").equals("Y");
      String strAction = null;
      if (vars.commandIn("SAVEANDPROCESS")) {
        // The default option is process
        strAction = (isReceipt ? "PRP" : "PPP");
      } else {
        strAction = vars.getRequiredStringParameter("inpActionDocument");
      }

      String strPaymentId = vars.getRequiredStringParameter("inpfinPaymentId");

      FIN_Payment payment = dao.getObject(FIN_Payment.class, strPaymentId);

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
      String strDifferenceAction = "";
      // BigDecimal refundAmount = BigDecimal.ZERO;
      String strDifference = vars.getNumericParameter("inpDifference", "0");

      String strTabId = vars.getRequiredStringParameter("inpTabId");
      String strPaymentAmount = vars.getRequiredNumericParameter("inpActualPayment");

      BigDecimal convertedAmount = new BigDecimal(
          vars.getNumericParameter("inpActualConverted", strPaymentAmount));
      OBError message = null;
      String detracwith_message = "";

      double strRetencion = Double.parseDouble(vars.getRequiredStringParameter("inpRetencion"));
      int strAplicarRetencion = Integer.parseInt(vars.getRequiredStringParameter("inpAplicar"));

      int isDetraccionPayment = Integer
          .parseInt(vars.getRequiredStringParameter("inpChkIsDetraccionPayment"));

      int tipoPago = Integer.parseInt(vars.getRequiredStringParameter("inpTipoPago"));

      String paymentCurrencyId = payment.getAccount().getCurrency().getId();// vars.getRequiredStringParameter("inpCurrencyId");
      BigDecimal exchangeRate = payment.getFinancialTransactionConvertRate();

      if (exchangeRate.compareTo(BigDecimal.ZERO) == 0)
        exchangeRate = new BigDecimal(1);

      boolean multicurrency = !payment.getAccount().getCurrency().getId()
          .equals(payment.getCurrency().getId());

      // FIXME: added to access the FIN_PaymentSchedule and FIN_PaymentScheduleDetail tables to be
      // removed when new security implementation is done
      OBContext.setAdminMode();
      try {

        System.out.println("0.0 PAYMENTTTT:");
        for (FIN_PaymentDetail pd : payment.getFINPaymentDetailList()) {
          System.out.println("pd: id:" + pd.getId() + " - pd.ident:" + pd.getIdentifier());
        }
        if (!"0".equals(strDifference)) {
          // refundAmount = new BigDecimal(vars.getRequiredNumericParameter("inpDifference"));
          strDifferenceAction = vars.getStringParameter("inpDifferenceAction", "");
        }

        // List<FIN_PaymentScheduleDetail> selectedPaymentDetails =
        // FIN_Utility.getOBObjectList(FIN_PaymentScheduleDetail.class,
        // strSelectedScheduledPaymentDetailIds);

        // Pending Payments from invoice
        List<FIN_PaymentScheduleDetail> selectedScheduledPaymentDetailsTmp = FIN_AddPayment
            .getSelectedPaymentDetails(null, strSelectedScheduledPaymentDetailIds);

        HashMap<String, BigDecimal> selectedPaymentDetailAmounts = getSelectedPaymentDetailsAndAmount(
            vars, strSelectedScheduledPaymentDetailIds, multicurrency, paymentCurrencyId,
            BigDecimal.ONE, exchangeRate, isReceipt);

        // agregar anticipos si existen
        List<FIN_PaymentScheduleDetail> selectedPaymentDetails = getSelectedAnticiposWithAmounts(
            vars, selectedScheduledPaymentDetailsTmp, strSelectedScheduledPaymentDetailIds,
            payment);
        selectedScheduledPaymentDetailsTmp = selectedPaymentDetails;
        // agregar entregas a rendir si existen
        selectedPaymentDetails = getSelectedEntregaARendir(selectedScheduledPaymentDetailsTmp,
            strSelectedScheduledPaymentDetailIds, payment);

        // Remove edited lines which are not in final selection and adjust outstanding amount for
        // documents
        removeNonSelectedDetails(payment, selectedPaymentDetails);
        BigDecimal newPaymentAmount = new BigDecimal(strPaymentAmount);

        // load object in memory
        payment.getFINPaymentDetailList();

        System.out.println("0 PAYMENTTTT:");
        for (FIN_PaymentDetail pd : payment.getFINPaymentDetailList()) {
          System.out.println("pd: id:" + pd.getId() + " - pd.ident:" + pd.getIdentifier());
        }
        if (addedGLITemsArray != null) {
          for (int i = 0; i < addedGLITemsArray.length(); i++) {
            JSONObject glItem = addedGLITemsArray.getJSONObject(i);
            BigDecimal glItemOutAmt = new BigDecimal(glItem.getString("glitemPaidOutAmt"));
            BigDecimal glItemInAmt = new BigDecimal(glItem.getString("glitemReceivedInAmt"));

            String glItemDescription = glItem.getString("glitemDescription");

            BigDecimal glItemAmt = BigDecimal.ZERO;
            if (isReceipt) {
              glItemAmt = glItemInAmt.subtract(glItemOutAmt);
            } else {
              glItemAmt = glItemOutAmt.subtract(glItemInAmt);
            }
            final String strGLItemId = glItem.getString("glitemId");
            checkID(strGLItemId);
            // Accounting Dimensions
            final String strElement_BP = glItem.getString("cBpartnerDim");
            checkID(strElement_BP);
            final BusinessPartner businessPartner = dao.getObject(BusinessPartner.class,
                strElement_BP);

            final String strElement_PR = glItem.getString("mProductDim");
            checkID(strElement_PR);
            final Product product = dao.getObject(Product.class, strElement_PR);

            final String strElement_PJ = glItem.getString("cProjectDim");
            checkID(strElement_PJ);
            final Project project = dao.getObject(Project.class, strElement_PJ);

            final String strElement_AY = glItem.getString("cActivityDim");
            checkID(strElement_AY);
            final ABCActivity activity = dao.getObject(ABCActivity.class, strElement_AY);

            final String strElement_CC = glItem.getString("cCostcenterDim");
            checkID(strElement_CC);
            final Costcenter costCenter = dao.getObject(Costcenter.class, strElement_CC);

            final String strElement_ID = glItem.getString("scoInternalDocDim");
            checkID(strElement_ID);
            final SCOInternalDoc internaldoc = dao.getObject(SCOInternalDoc.class, strElement_ID);

            final String strElement_IG = glItem.getString("scoInvoiceGlrefDim");
            checkID(strElement_IG);
            final Invoice invglref = dao.getObject(Invoice.class, strElement_IG);

            final String strElement_CF = glItem.getString("scoEeffCashflowDim");
            checkID(strElement_CF);
            final SCOEEFFCashflow cashflow = dao.getObject(SCOEEFFCashflow.class, strElement_CF);

            final String strElement_MC = glItem.getString("cCampaignDim");
            checkID(strElement_MC);
            final Campaign campaign = dao.getObject(Campaign.class, strElement_MC);

            final String strElement_U1 = glItem.getString("user1Dim");
            checkID(strElement_U1);
            final UserDimension1 user1 = dao.getObject(UserDimension1.class, strElement_U1);

            final String strElement_U2 = glItem.getString("user2Dim");
            checkID(strElement_U2);
            final UserDimension2 user2 = dao.getObject(UserDimension2.class, strElement_U2);

            FIN_AddPayment.saveGLItem(payment, glItemAmt, dao.getObject(GLItem.class, strGLItemId),
                businessPartner, product, project, campaign, activity, null, costCenter, user1,
                user2, glItemDescription, null, null, null, "", internaldoc, invglref, cashflow);
          }
        }

        if (isDetraccionPayment == 1)
          payment.setScoDetractionpayment(true);

        SCRComboItem comboItem = null;

        // Poner descripton
        if (!isReceipt) {
          HashMap<String, String> selectedPaymentDetailDescriptions = getSelectedPaymentDetailsDescriptions(
              vars, strSelectedScheduledPaymentDetailIds);
          HashMap<String, BigDecimal> selectedPaymentDetailDetWithhoAmounts = getSelectedPaymentDetailsAndDetWithhoAmount(
              vars, strSelectedScheduledPaymentDetailIds, multicurrency, paymentCurrencyId,
              exchangeRate);
          for (int i = 0; i < selectedPaymentDetails.size(); i++) {
            FIN_PaymentScheduleDetail det = selectedPaymentDetails.get(i);
            String description = selectedPaymentDetailDescriptions.get(det.getId());

            selectedPaymentDetails.get(i).setSCODescription(description);
            selectedPaymentDetails.get(i)
                .setScoDetretamt(selectedPaymentDetailDetWithhoAmounts.get(det.getId()));

            if (selectedPaymentDetails.get(i).getScoPrepayment() == null
                && selectedPaymentDetails.get(i).getSCOEntregaARendir() == null
                && selectedPaymentDetails.get(i).getScoPayoutprepayment() == null
                && selectedPaymentDetails.get(i).getScoPayoutrendcuentas() == null)
              OBDal.getInstance().save(selectedPaymentDetails.get(i));
          }
        }

        payment.setAmount(newPaymentAmount);

        OBDal.getInstance().save(payment);
        FIN_AddPayment.setFinancialTransactionAmountAndRate(vars, payment, exchangeRate,
            convertedAmount);

        System.out.println("-11 PAYMENTTTT:");
        for (FIN_PaymentDetail pd : payment.getFINPaymentDetailList()) {
          System.out.println("pd: id:" + pd.getId() + " - pd.ident:" + pd.getIdentifier());
        }
        payment = FIN_AddPayment.savePayment(vars, payment, isReceipt, null, null, null, null, null,
            newPaymentAmount.toString(), null, null, null, selectedPaymentDetails,
            selectedPaymentDetailAmounts, strDifferenceAction.equals("writeoff"),
            strDifferenceAction.equals("overpayment"), payment.getCurrency(), exchangeRate,
            convertedAmount);

        System.out.println("-1 PAYMENTTTT:");
        for (FIN_PaymentDetail pd : payment.getFINPaymentDetailList()) {
          System.out.println("pd: id:" + pd.getId() + " - pd.ident:" + pd.getIdentifier());
        }
        if ((payment.getScrPaymenttype() != null
            && payment.getScrPaymenttype().equals("SCR_BOE_DIS"))
            || (payment.isScoIsfactinvPayment() != null && payment.isScoIsfactinvPayment() == true
                && payment.getScoFactinvPaymenttype() != null
                && payment.getScoFactinvPaymenttype().compareTo("SCO_PAYFACTINV_DIS") == 0)) {
          payment.setScoIsmigrationpayment(true);// ya mandado a cuenta financiera
          OBDal.getInstance().save(payment);
          OBDal.getInstance().flush();
        } else {
          payment.setScoIsmigrationpayment(false);
          OBDal.getInstance().save(payment);
          OBDal.getInstance().flush();
        }

        if (strAction.equals("PRP") || strAction.equals("PPP") || strAction.equals("PRD")
            || strAction.equals("PPW")) {
          try {
            // Process just in case there are lines, empty Refund payment does not need to call
            // process
            if (payment.getFINPaymentDetailList().size() > 0) {
              // If Action PRP o PPW, Process payment but as well create a financial transaction
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
             * (payment.getGeneratedCredit().compareTo(BigDecimal.ZERO) != 0) {
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
        /*
         * String strMessage = FIN_Utility.getExceptionMessage(ex); if (message != null &&
         * "Error".equals(message.getType())) { strMessage = message.getMessage(); }
         * bdErrorGeneralPopUp(request, response, "Error", strMessage);
         */
        OBError newError = Utility.translateError(this, vars, vars.getLanguage(),
            FIN_Utility.getExceptionMessage(ex));
        vars.setMessage(strTabId, newError);
        printPageClosePopUp(response, vars);
        OBDal.getInstance().rollbackAndClose();
        log4j.error("AddOrderOrInvoice - SAVE AND PROCESS", ex);
        return;

      } finally {
        OBContext.restorePreviousMode();
      }

      if (message != null && !("Error".equals(message.getType()))) {
        if (detracwith_message != null && !detracwith_message.equals("")) {
          if (message.getMessage() != null && !message.getMessage().equals(""))
            detracwith_message = "." + detracwith_message;

          message.setMessage(message.getMessage() + detracwith_message);
        }
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

      if (pd.getSCOPrepayment() != null) {
        toRemovePDs.add(pd.getId());
        continue;
      }

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

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strPaymentId,
      String strWindowId, String strTabId, String strFinancialAccountId,
      String strSsaPaymentInDocType, boolean isFirstLoad) throws Exception {
    log4j.debug("Output: Add Payment button pressed on Make / Receipt Payment windows");

    OBContext.setAdminMode(true);
    try {
      FIN_Payment payment = new AdvPaymentMngtDao().getObject(FIN_Payment.class, strPaymentId);
      String[] discard = { "discard", "discard", "discard" };
      if (payment.getBusinessPartner() != null) {
        discard[0] = "bpGridColumn";
      }

      if (payment.isReceipt()) {
        discard[1] = "detretGridColumn";
        discard[2] = "descGridColumn";
      } else {
        discard[1] = "methodGridColumn";
      }

      XmlDocument xmlDocument = xmlEngine
          .readXmlTemplate("org/openbravo/advpaymentmngt/ad_actionbutton/AddOrderOrInvoice",
              discard)
          .createXmlDocument();

      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("theme", vars.getTheme());

      xmlDocument.setParameter("paymentInDocType", "");

      if (payment.isReceipt())
        xmlDocument.setParameter("title",
            Utility.messageBD(this, "APRM_AddPaymentIn", vars.getLanguage()));
      else
        xmlDocument.setParameter("title",
            Utility.messageBD(this, "APRM_AddPaymentOut", vars.getLanguage()));
      xmlDocument.setParameter("expectedDateFromdisplayFormat",
          vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("expectedDateTodisplayFormat",
          vars.getSessionValue("#AD_SqlDateFormat"));

      if (payment.getBusinessPartner() != null) {
        xmlDocument.setParameter("businessPartner", payment.getBusinessPartner().getIdentifier());
        xmlDocument.setParameter("businessPartnerId", payment.getBusinessPartner().getId());
        xmlDocument.setParameter("credit",
            dao.getCustomerCredit(payment.getBusinessPartner(), payment.isReceipt(),
                payment.getOrganization(), payment.getAccount().getCurrency()).toString());
        xmlDocument.setParameter("customerBalance",
            payment.getBusinessPartner().getSsaCreditused() != null
                ? payment.getBusinessPartner().getSsaCreditused().toString()
                : BigDecimal.ZERO.toString());

      } else {
        xmlDocument.setParameter("businessPartner", "");
        xmlDocument.setParameter("businessPartnerId", "");
        xmlDocument.setParameter("credit", "");
        xmlDocument.setParameter("customerBalance", "");

        xmlDocument.setParameter("expectedDateFrom",
            SREDateTimeData.nDaysBefore(this, SREDateTimeData.today(this), "5"));
        xmlDocument.setParameter("expectedDateFromdisplayFormat",
            vars.getSessionValue("#AD_SqlDateFormat"));
        xmlDocument.setParameter("expectedDateFromsaveFormat",
            vars.getSessionValue("#AD_SqlDateFormat"));
        xmlDocument.setParameter("expectedDateTo", SREDateTimeData.today(this));
        xmlDocument.setParameter("expectedDateTodisplayFormat",
            vars.getSessionValue("#AD_SqlDateFormat"));
        xmlDocument.setParameter("expectedDateTosaveFormat",
            vars.getSessionValue("#AD_SqlDateFormat"));

      }

      xmlDocument.setParameter("windowId", strWindowId);
      xmlDocument.setParameter("tabId", strTabId);
      xmlDocument.setParameter("strTabId", "strTabId =  \"" + strTabId + "\";\r\n");
      xmlDocument.setParameter("orgId", payment.getOrganization().getId());
      xmlDocument.setParameter("paymentId", strPaymentId);
      xmlDocument.setParameter("actualPayment", payment.getAmount().toString());
      xmlDocument.setParameter("headerAmount", payment.getAmount().toString());
      xmlDocument.setParameter("isReceipt", (payment.isReceipt() ? "Y" : "N"));
      xmlDocument.setParameter("isSoTrx", (payment.isReceipt()) ? "Y" : "N");
      xmlDocument.setParameter("isSimple", (payment.isSCOIsSimpleProvision()) ? "Y" : "N");
      xmlDocument.setParameter("isApp", (payment.isScoIsapppayment()) ? "Y" : "N");
      xmlDocument.setParameter("APApplicationType",
          (payment.getScoApplicationtype() != null) ? payment.getScoApplicationtype() : "");
      xmlDocument.setParameter("ARApplicationType",
          (payment.getScoRecvapplicationtype() != null) ? payment.getScoRecvapplicationtype() : "");

      // Dependiendo del proveedor si tiene detraccion o retencion
      if (!payment.isReceipt()) {
        if (payment.getOrganization().isScoRetencionagent() && payment.getBusinessPartner() != null
            && !payment.getBusinessPartner().isScoRetencionagent()
            && !payment.getBusinessPartner().isScoHasgoodrep())
          xmlDocument.setParameter("retencion", "Y");
        else
          xmlDocument.setParameter("retencion", "N");

        xmlDocument.setParameter("limitForRetencion", "700");

        Currency currSoles = OBDal.getInstance().get(Currency.class, "308");
        ConversionRate cr1 = SCO_Utils.getConversionRate(payment.getAccount().getCurrency(),
            currSoles, payment.getPaymentDate());
        ConversionRate cr0 = SCO_Utils.getConversionRate(payment.getCurrency(), currSoles,
            payment.getPaymentDate());
        if (cr1 == null || cr0 == null) {
          throw new Exception(
              "No se encuentra tipo de cambio adecuado para la fecha del pago y las monedas involucradas");
        }

        xmlDocument.setParameter("tcForRetencion1", cr1.getMultipleRateBy().toString());
        xmlDocument.setParameter("tcForRetencion0", cr0.getMultipleRateBy().toString());
      }

      if (payment.getBusinessPartner() == null && (payment.getGeneratedCredit() == null
          || BigDecimal.ZERO.compareTo(payment.getGeneratedCredit()) != 0)) {
        payment.setGeneratedCredit(BigDecimal.ZERO);
        OBDal.getInstance().save(payment);
        OBDal.getInstance().flush();
      }
      xmlDocument.setParameter("generatedCredit",
          payment.getGeneratedCredit() != null ? payment.getGeneratedCredit().toString()
              : BigDecimal.ZERO.toString());

      final Currency financialAccountCurrency = payment.getAccount().getCurrency();
      if (financialAccountCurrency != null) {
        xmlDocument.setParameter("financialAccountCurrencyId", financialAccountCurrency.getId());
        xmlDocument.setParameter("financialAccountCurrencyName",
            payment.getCurrency().getISOCode());
        xmlDocument.setParameter("financialAccountCurrencyPrecision",
            financialAccountCurrency.getStandardPrecision().toString());

        xmlDocument.setParameter("financialAccountCurrency2Id", financialAccountCurrency.getId());
        xmlDocument.setParameter("financialAccountCurrency2Name",
            financialAccountCurrency.getISOCode());
        xmlDocument.setParameter("financialAccountCurrency2Precision",
            financialAccountCurrency.getStandardPrecision().toString());
      }

      xmlDocument.setParameter("exchangeRate",
          payment.getFinancialTransactionConvertRate() == null ? ""
              : payment.getFinancialTransactionConvertRate().toPlainString());
      xmlDocument.setParameter("altExchangeRate", payment.getScoAltConvertRate() == null ? ""
          : payment.getScoAltConvertRate().toPlainString());
      xmlDocument.setParameter("actualConverted",
          payment.getFinancialTransactionAmount() == null ? ""
              : payment.getFinancialTransactionAmount().toString());
      xmlDocument.setParameter("expectedConverted",
          payment.getFinancialTransactionAmount() == null ? ""
              : payment.getFinancialTransactionAmount().toPlainString());
      xmlDocument.setParameter("currencyId", payment.getCurrency().getId());
      xmlDocument.setParameter("currencyName", financialAccountCurrency.getISOCode());

      boolean forcedFinancialAccountTransaction = false;
      forcedFinancialAccountTransaction = FIN_AddPayment
          .isForcedFinancialAccountTransaction(payment);
      // Action Regarding Document
      xmlDocument.setParameter("ActionDocument", (payment.isReceipt() ? "PRP" : "PPP"));
      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
            (payment.isReceipt() ? "F903F726B41A49D3860243101CEEBA25"
                : "F15C13A199A748F1B0B00E985A64C036"),
            forcedFinancialAccountTransaction ? "29010995FD39439D97A5C0CE8CE27D70" : "",
            Utility.getContext(this, vars, "#AccessibleOrgTree", "AddPaymentFromInvoice"),
            Utility.getContext(this, vars, "#User_Client", "AddPaymentFromInvoice"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "AddOrderOrInvoice", "");
        xmlDocument.setData("reportActionDocument", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      // Accounting Dimensions
      String doctype;
      if (payment.isReceipt()) {
        doctype = AcctServer.DOCTYPE_ARReceipt;
      } else {
        doctype = AcctServer.DOCTYPE_APPayment;
      }
      final String strCentrally = Utility.getContext(this, vars,
          DimensionDisplayUtility.IsAcctDimCentrally, strWindowId);
      final String strElement_BP = "Y";
      Utility.getContext(this, vars,
          DimensionDisplayUtility.displayAcctDimensions(strCentrally,
              DimensionDisplayUtility.DIM_BPartner, doctype, DimensionDisplayUtility.DIM_Header),
          strWindowId);
      final String strElement_PR = Utility.getContext(this, vars,
          DimensionDisplayUtility.displayAcctDimensions(strCentrally,
              DimensionDisplayUtility.DIM_Product, doctype, DimensionDisplayUtility.DIM_Header),
          strWindowId);
      String strElement_PJ = Utility.getContext(this, vars,
          DimensionDisplayUtility.displayAcctDimensions(strCentrally,
              DimensionDisplayUtility.DIM_Project, doctype, DimensionDisplayUtility.DIM_Header),
          strWindowId);
      final String strElement_AY = Utility.getContext(this, vars, "$Element_AY", strWindowId);
      String strElement_CC = Utility.getContext(this, vars,
          DimensionDisplayUtility.displayAcctDimensions(strCentrally,
              DimensionDisplayUtility.DIM_CostCenter, doctype, DimensionDisplayUtility.DIM_Header),
          strWindowId);

      final String strElement_ID = "Y"; // always show the internal document dimension
      final String strElement_IG = "Y"; // always show the purchase/sales document dimension
      final String strElement_CF = "Y"; // always show the cashflow dimension
      strElement_PJ = "N"; // never show Project dimension
      strElement_CC = "Y"; // always show the costcenter dimension

      final String strElement_MC = Utility.getContext(this, vars, "$Element_MC", strWindowId);
      final String strElement_U1 = Utility.getContext(this, vars,
          DimensionDisplayUtility.displayAcctDimensions(strCentrally,
              DimensionDisplayUtility.DIM_User1, doctype, DimensionDisplayUtility.DIM_Header),
          strWindowId);
      final String strElement_U2 = Utility.getContext(this, vars,
          DimensionDisplayUtility.displayAcctDimensions(strCentrally,
              DimensionDisplayUtility.DIM_User2, doctype, DimensionDisplayUtility.DIM_Header),
          strWindowId);
      xmlDocument.setParameter("strElement_BP", strElement_BP);
      xmlDocument.setParameter("strElement_PR", strElement_PR);
      xmlDocument.setParameter("strElement_PJ", strElement_PJ);
      xmlDocument.setParameter("strElement_AY", strElement_AY);
      xmlDocument.setParameter("strElement_CC", strElement_CC);
      xmlDocument.setParameter("strElement_ID", strElement_ID);
      xmlDocument.setParameter("strElement_IG", strElement_IG);
      xmlDocument.setParameter("strElement_CF", strElement_CF);
      xmlDocument.setParameter("strElement_MC", strElement_MC);
      xmlDocument.setParameter("strElement_U1", strElement_U1);
      xmlDocument.setParameter("strElement_U2", strElement_U2);

      // Add GL Items
      JSONArray addedGLITemsArray = new JSONArray();
      List<FIN_PaymentScheduleDetail> gLItemScheduleDetailLines = FIN_AddPayment
          .getGLItemScheduleDetails(payment);
      for (FIN_PaymentScheduleDetail psdGLItem : gLItemScheduleDetailLines) {
        try {
          JSONObject glItem = new JSONObject();
          glItem.put("glitemId", psdGLItem.getPaymentDetails().getGLItem().getId());
          glItem.put("glitemDesc", psdGLItem.getPaymentDetails().getGLItem().getIdentifier());
          glItem.put("finPaymentScheduleDetailId", psdGLItem.getId());
          // Amounts
          if (payment.isReceipt()) {
            glItem.put("glitemPaidOutAmt",
                psdGLItem.getAmount().signum() < 0 ? psdGLItem.getAmount().abs() : BigDecimal.ZERO);
            glItem.put("glitemReceivedInAmt",
                psdGLItem.getAmount().signum() > 0 ? psdGLItem.getAmount() : BigDecimal.ZERO);
          } else {
            glItem.put("glitemReceivedInAmt",
                psdGLItem.getAmount().signum() < 0 ? psdGLItem.getAmount().abs() : BigDecimal.ZERO);
            glItem.put("glitemPaidOutAmt",
                psdGLItem.getAmount().signum() > 0 ? psdGLItem.getAmount() : BigDecimal.ZERO);
          }
          glItem.put("glitemDescription", psdGLItem.getSCODescription());
          // Accounting Dimensions
          glItem.put("cBpartnerDim",
              psdGLItem.getBusinessPartner() != null ? psdGLItem.getBusinessPartner().getId() : "");
          glItem.put("cBpartnerDimDesc",
              psdGLItem.getBusinessPartner() != null
                  ? psdGLItem.getBusinessPartner().getIdentifier()
                  : "");
          glItem.put("mProductDim",
              psdGLItem.getProduct() != null ? psdGLItem.getProduct().getId() : "");
          glItem.put("mProductDimDesc",
              psdGLItem.getProduct() != null ? psdGLItem.getProduct().getIdentifier() : "");
          glItem.put("cProjectDim",
              psdGLItem.getProject() != null ? psdGLItem.getProject().getId() : "");
          glItem.put("cProjectDimDesc",
              psdGLItem.getProject() != null ? psdGLItem.getProject().getIdentifier() : "");
          glItem.put("cActivityDim",
              psdGLItem.getActivity() != null ? psdGLItem.getActivity().getId() : "");
          glItem.put("cActivityDimDesc",
              psdGLItem.getActivity() != null ? psdGLItem.getActivity().getIdentifier() : "");
          glItem.put("cCostcenterDim",
              psdGLItem.getCostCenter() != null ? psdGLItem.getCostCenter().getId() : "");
          glItem.put("cCostcenterDimDesc",
              psdGLItem.getCostCenter() != null ? psdGLItem.getCostCenter().getIdentifier() : "");
          glItem.put("scoInternalDocDim",
              psdGLItem.getScoInternalDoc() != null ? psdGLItem.getScoInternalDoc().getId() : "");
          glItem.put("scoInternalDocDimDesc",
              psdGLItem.getScoInternalDoc() != null ? psdGLItem.getScoInternalDoc().getIdentifier()
                  : "");
          glItem.put("scoInvoiceGlrefDim",
              psdGLItem.getScoInvoiceGlref() != null ? psdGLItem.getScoInvoiceGlref().getId() : "");
          glItem.put("scoInvoiceGlrefDimDesc",
              psdGLItem.getScoInvoiceGlref() != null
                  ? psdGLItem.getScoInvoiceGlref().getIdentifier()
                  : "");
          glItem.put("scoEeffCashflowDim",
              psdGLItem.getScoEeffCashflow() != null ? psdGLItem.getScoEeffCashflow().getId() : "");
          glItem.put("scoEeffCashflowDimDesc",
              psdGLItem.getScoEeffCashflow() != null
                  ? psdGLItem.getScoEeffCashflow().getIdentifier()
                  : "");
          glItem.put("cCampaignDim",
              psdGLItem.getSalesCampaign() != null ? psdGLItem.getSalesCampaign().getId() : "");
          glItem.put("cCampaignDimDesc",
              psdGLItem.getSalesCampaign() != null ? psdGLItem.getSalesCampaign().getIdentifier()
                  : "");
          glItem.put("user1Dim",
              psdGLItem.getStDimension() != null ? psdGLItem.getStDimension().getId() : "");
          glItem.put("user1DimDesc",
              psdGLItem.getStDimension() != null ? psdGLItem.getStDimension().getIdentifier() : "");
          glItem.put("user2Dim",
              psdGLItem.getNdDimension() != null ? psdGLItem.getNdDimension().getId() : "");
          glItem.put("user2DimDesc",
              psdGLItem.getNdDimension() != null ? psdGLItem.getNdDimension().getIdentifier() : "");
          // DisplayLogics
          glItem.put("cBpartnerDimDisplayed", strElement_BP);
          glItem.put("mProductDimDisplayed", strElement_PR);
          glItem.put("cProjectDimDisplayed", strElement_PJ);
          glItem.put("cActivityDimDisplayed", strElement_AY);
          glItem.put("cCostcenterDimDisplayed", strElement_CC);
          glItem.put("scoInternalDocDimDisplayed", strElement_ID);
          glItem.put("scoInvoiceGlrefDimDisplayed", strElement_IG);
          glItem.put("scoEeffCashflowDimDisplayed", strElement_CF);
          glItem.put("cCampaignDimDisplayed", strElement_MC);
          glItem.put("user1DimDisplayed", strElement_U1);
          glItem.put("user2DimDisplayed", strElement_U2);
          addedGLITemsArray.put(glItem);
        } catch (JSONException e) {
          log4j.error(e);
        }
      }
      xmlDocument.setParameter("glItems",
          addedGLITemsArray.toString().replace("'", "").replaceAll("\"", "'"));
      // If UsedCredit is not equal zero, check Use available credit
      xmlDocument.setParameter("useCredit", payment.getUsedCredit().signum() != 0 ? "Y" : "N");

      // Not allow to change exchange rate and amount
      final String strNotAllowExchange = Utility.getContext(this, vars, "NotAllowChangeExchange",
          strWindowId);
      xmlDocument.setParameter("strNotAllowExchange", strNotAllowExchange);

      dao = new AdvPaymentMngtDao();
      FIN_FinancialAccount financialAccount = dao.getObject(FIN_FinancialAccount.class,
          strFinancialAccountId);

      if (financialAccount.getWriteofflimit() != null) {
        final String strtypewriteoff;
        final String strAmountwriteoff;

        strtypewriteoff = financialAccount.getTypewriteoff();
        strAmountwriteoff = financialAccount.getWriteofflimit().toString();
        xmlDocument.setParameter("strtypewriteoff", strtypewriteoff);
        xmlDocument.setParameter("strAmountwriteoff", strAmountwriteoff);

        // Not allow to write off
        final String strWriteOffLimit = Utility.getContext(this, vars, "WriteOffLimitPreference",
            strWindowId);
        xmlDocument.setParameter("strWriteOffLimit", strWriteOffLimit);
      }

      if (isFirstLoad) {
        xmlDocument.setParameter("FirstLoad", "FirstLoad = \"YES\"; ");
      } else {
        xmlDocument.setParameter("FirstLoad", "FirstLoad = \"NO\"; ");
      }

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
      boolean showAllFinancialAcounts, String strTabId, BigDecimal exchangeRate,
      String strSsaPaymentInDocType, String strCContable) throws IOException, ServletException {

    log4j.debug("Output: Grid with pending payments");
    dao = new AdvPaymentMngtDao();
    String[] discard = { "discard", "discard", "discard", "discard" };
    if (!"".equals(vars.getRequestGlobalVariable("inpBusinessPartnerId", ""))) {
      discard[0] = "businessPartnerName";
    }
    if (isReceipt) {
      discard[1] = "inpInvoiceDetRetAmount";
      discard[2] = "inpDescription";
      discard[3] = "fieldScheduledPaymentDetailIdRet";

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
      // Add payment schedule details related to orders or invoices to storedSchedulePaymentDetails
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

        for (FIN_PaymentDetail pd : payment.getFINPaymentDetailList()) {
          if (pd.getScoPayoutprepayment() != null && !pd.getScoPayoutprepayment().isEmpty()) {
            FIN_PaymentScheduleDetail psd = AdvPaymentMngtDao.convertoFromPrepaymentToSchedule(
                OBDal.getInstance().get(SCOPrepayment.class, pd.getScoPayoutprepayment()), true);
            psd.setAmount(pd.getAmount());
            storedScheduledPaymentDetails.add(psd);
          }
          if (pd.getSCOPrepayment() != null) {
            FIN_PaymentScheduleDetail psd = AdvPaymentMngtDao
                .convertoFromPrepaymentToSchedule(pd.getSCOPrepayment(), false);
            psd.setAmount(pd.getAmount());
            storedScheduledPaymentDetails.add(psd);
          }
        }

      } finally {
        OBContext.restorePreviousMode();
      }

    }
    // Pending Payments from invoice
    List<FIN_PaymentScheduleDetail> selectedScheduledPaymentDetailsTmp = FIN_AddPayment
        .getSelectedPaymentDetails("true".equals(strFirstLoad)
            ? new ArrayList<FIN_PaymentScheduleDetail>(storedScheduledPaymentDetails)
            : null, strSelectedPaymentDetails);
    // agregar anticipos si existen
    List<FIN_PaymentScheduleDetail> selectedScheduledPaymentDetails = getSelectedAnticipos(
        selectedScheduledPaymentDetailsTmp, strSelectedPaymentDetails, payment);
    selectedScheduledPaymentDetailsTmp = selectedScheduledPaymentDetails;
    // agregar entregas a rendir si existen
    selectedScheduledPaymentDetails = getSelectedEntregaARendir(selectedScheduledPaymentDetailsTmp,
        strSelectedPaymentDetails, payment);

    // filtered scheduled payments list
    List<FIN_PaymentScheduleDetail> filteredScheduledPaymentDetails = null;

    boolean multicurrency = !payment.getAccount().getCurrency().getId()
        .equals(payment.getCurrency().getId());

    if (strDocumentType.equals("A")) {// anticipos de compra
      filteredScheduledPaymentDetails = dao.getFilteredPrepayments(
          dao.getObject(Organization.class, strOrgId),
          dao.getObject(BusinessPartner.class, strBusinessPartnerId), payment.getCurrency(),
          payment.getAccount().getCurrency(), selectedScheduledPaymentDetails, strDocNo, payment,
          isReceipt);
    } else if (strDocumentType.equals("E")) {
      filteredScheduledPaymentDetails = dao.getFilteredEntregas(
          dao.getObject(Organization.class, strOrgId),
          dao.getObject(BusinessPartner.class, strBusinessPartnerId), payment.getCurrency(),
          payment.getAccount().getCurrency(), selectedScheduledPaymentDetails, strDocNo, payment);
    } else {

      boolean showAlternative = showAlternativePM
          && (payment.getPaymentMethod().getScoSpecialmethod() == null
              || !payment.getPaymentMethod().getScoSpecialmethod().equals("SCOBILLOFEXCHANGE"));
      boolean isfactinv_payment = payment.isScoIsfactinvPayment() != null
          ? payment.isScoIsfactinvPayment()
          : false;
      if (!multicurrency) {

        filteredScheduledPaymentDetails = dao.getFilteredScheduledPaymentDetails(
            dao.getObject(Organization.class, strOrgId),
            dao.getObject(BusinessPartner.class, strBusinessPartnerId),
            payment.getAccount().getCurrency(), null, null,
            FIN_Utility.getDate(strExpectedDateFrom),
            FIN_Utility.getDate(DateTimeData.nDaysAfter(this, strExpectedDateTo, "1")), null, null,
            strDocumentType, "", showAlternative ? null : payment.getPaymentMethod(),
            payment.getScrPaymenttype(), selectedScheduledPaymentDetails, isReceipt,
            payment.getPaymentDate(), strDocNo, payment.isSCOIsSimpleProvision(),
            payment.isScoIsapppayment(), showAllFinancialAcounts, payment.getAccount(), strTabId,
            isfactinv_payment, (isfactinv_payment ? payment.getScoFactinvPaymenttype() : null));
      } else {
        filteredScheduledPaymentDetails = dao.getFilteredScheduledPaymentDetails(
            dao.getObject(Organization.class, strOrgId),
            dao.getObject(BusinessPartner.class, strBusinessPartnerId), payment.getCurrency(),
            payment.getAccount().getCurrency(), null, null,
            FIN_Utility.getDate(strExpectedDateFrom),
            FIN_Utility.getDate(DateTimeData.nDaysAfter(this, strExpectedDateTo, "1")), null, null,
            strDocumentType, "", showAlternative ? null : payment.getPaymentMethod(),
            payment.getScrPaymenttype(), selectedScheduledPaymentDetails, isReceipt,
            payment.getPaymentDate(), strDocNo, payment.isSCOIsSimpleProvision(),
            payment.isScoIsapppayment(), showAllFinancialAcounts, payment.getAccount(), strTabId,
            isfactinv_payment, (isfactinv_payment ? payment.getScoFactinvPaymenttype() : null));
      }
    }

    // Remove related outstanding schedule details related to those ones being edited as amount will
    // be later added to storedScheduledPaymentDetails
    for (FIN_PaymentScheduleDetail psd : storedScheduledPaymentDetails) {
      if (psd.getScoPayoutprepayment() != null)
        continue;
      if (psd.getScoPrepayment() != null)
        continue;
      filteredScheduledPaymentDetails.removeAll(FIN_AddPayment.getOutstandingPSDs(psd));
    }

    // Get stored not selected PSDs
    List<FIN_PaymentScheduleDetail> storedNotSelectedPSDs = new ArrayList<FIN_PaymentScheduleDetail>(
        storedScheduledPaymentDetails);

    List<FIN_PaymentScheduleDetail> toremove = new ArrayList<FIN_PaymentScheduleDetail>();
    for (FIN_PaymentScheduleDetail storedNotSelectedPSD : storedNotSelectedPSDs) {
      boolean found = false;
      for (FIN_PaymentScheduleDetail selectedScheduledPaymentDetail : selectedScheduledPaymentDetails) {
        if (storedNotSelectedPSD.getId().equals(selectedScheduledPaymentDetail.getId())) {
          found = true;
          break;
        }
      }

      if (found) {
        toremove.add(storedNotSelectedPSD);
      }
    }
    storedNotSelectedPSDs.removeAll(toremove);

    // storedNotSelectedPSDs.removeAll(selectedScheduledPaymentDetails);
    // Add stored but not selected details which maps documenttype
    filteredScheduledPaymentDetails
        .addAll(filterDocumenttype(storedNotSelectedPSDs, strDocumentType));

    FieldProvider[] data = null;
    /*
     * if(strDocumentType.equals("A")){//anticipo de compra data = getShownAnticipos(vars,
     * selectedScheduledPaymentDetails, filteredScheduledPaymentDetails, strSelectedPaymentDetails);
     * }else{
     */

    data = FIN_AddPayment.getShownScheduledPaymentDetails(vars, selectedScheduledPaymentDetails,
        filteredScheduledPaymentDetails, false, null, strSelectedPaymentDetails, multicurrency,
        payment.getCurrency(), payment.getAccount().getCurrency(), exchangeRate, isReceipt);
    // }
    for (FIN_PaymentScheduleDetail psd : storedScheduledPaymentDetails) {
      if (psd.getScoPayoutprepayment() != null)
        continue;
      if (psd.getScoPrepayment() != null)
        continue;

      // Calculate pending amount
      BigDecimal outstandingAmount = BigDecimal.ZERO;
      List<FIN_PaymentScheduleDetail> outStandingPSDs = FIN_AddPayment.getOutstandingPSDs(psd);
      if (outStandingPSDs.size() != 0) {
        for (FIN_PaymentScheduleDetail outPSD : outStandingPSDs) {
          outstandingAmount = outstandingAmount.add(outPSD.getAmount());
        }
      }

      Currency currInvoice = payment.getAccount().getCurrency();
      boolean docissotrx = false;
      if (psd.getInvoicePaymentSchedule() != null) {
        currInvoice = psd.getInvoicePaymentSchedule().getCurrency();
        docissotrx = psd.getInvoicePaymentSchedule().getInvoice().isSalesTransaction();
      }
      if (psd.getOrderPaymentSchedule() != null) {
        currInvoice = psd.getOrderPaymentSchedule().getCurrency();
        docissotrx = psd.getOrderPaymentSchedule().getOrder().isSalesTransaction();
      }
      if (psd.getScoPrepayment() != null)
        currInvoice = psd.getScoPrepayment().getCurrency();
      if (psd.getSCOEntregaARendir() != null)
        currInvoice = psd.getSCOEntregaARendir().getCurrency();
      if (psd.getScoPayoutprepayment() != null)
        currInvoice = psd.getScoPayoutprepayment().getCurrency();
      if (psd.getScoPayoutrendcuentas() != null)
        currInvoice = psd.getScoPayoutrendcuentas().getCurrency();

      for (int i = 0; i < data.length; i++) {
        if (data[i].getField("finScheduledPaymentDetailId").equals(psd.getId())) {

          if (!strDocumentType.equals("A") && !strDocumentType.equals("E")) {

            BigDecimal outstandingAmount_ret = outstandingAmount;
            BigDecimal psdAmout = psd.getAmount();
            BigDecimal retdet = psd.getScoDetretamt();

            if (docissotrx != isReceipt) {
              outstandingAmount = outstandingAmount.negate();
              outstandingAmount_ret = outstandingAmount_ret.negate();
              psdAmout = psdAmout.negate();
              retdet = retdet.negate();
            }

            if (multicurrency) {

              if (!currInvoice.getId().equals(payment.getAccount().getCurrency().getId())) {

                if (exchangeRate.compareTo(BigDecimal.ZERO) == 0)
                  exchangeRate = BigDecimal.ONE;

                outstandingAmount_ret = outstandingAmount.divide(exchangeRate, 2,
                    RoundingMode.HALF_UP);
                psdAmout = psdAmout.divide(exchangeRate, 2, RoundingMode.HALF_UP);
                retdet = psd.getScoDetretamt().divide(exchangeRate, 2, RoundingMode.HALF_UP);
              }
            }

            FieldProviderFactory.setField(data[i], "outstandingAmount",
                psdAmout.add(outstandingAmount_ret).toPlainString());

            if ("true".equals(strFirstLoad)) {
              FieldProviderFactory.setField(data[i], "difference",
                  outstandingAmount_ret.toPlainString());
              FieldProviderFactory.setField(data[i], "paymentAmount", psdAmout.toPlainString());
            }

            if ("true".equals(strFirstLoad)) {
              FieldProviderFactory.setField(data[i], "maxLength", new String("255"));
              FieldProviderFactory.setField(data[i], "description", psd.getSCODescription());
              FieldProviderFactory.setField(data[i], "invoiceDetRetAmount", retdet.toPlainString());
            }
          }
        }
      }

    }

    for (FIN_PaymentScheduleDetail psd : storedScheduledPaymentDetails) {
      if (psd.getScoPayoutprepayment() != null) {

        boolean docissotrx = psd.getScoPayoutprepayment().isSalesTransaction();

        SCOPrepayment prepayment = psd.getScoPayoutprepayment();
        for (int i = 0; i < data.length; i++) {
          if (data[i].getField("finScheduledPaymentDetailId").equals(psd.getId())) {
            if ("true".equals(strFirstLoad)) {
              FieldProviderFactory.setField(data[i], "paymentAmount",
                  psd.getAmount().toPlainString());
            }
          }
        }
      } else if (psd.getScoPrepayment() != null) {

        boolean docissotrx = psd.getScoPrepayment().isSalesTransaction();

        SCOPrepayment prepayment = psd.getScoPrepayment();
        for (int i = 0; i < data.length; i++) {
          if (data[i].getField("finScheduledPaymentDetailId").equals(psd.getId())) {
            if ("true".equals(strFirstLoad)) {
              FieldProviderFactory.setField(data[i], "paymentAmount",
                  psd.getAmount().toPlainString());
            }
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

  private static List<FIN_PaymentScheduleDetail> getSelectedAnticipos(
      List<FIN_PaymentScheduleDetail> scheduledPaymentDetails, String strSelectedPaymentDetailsIds,
      FIN_Payment payment) {
    List<FIN_PaymentScheduleDetail> selectedScheduledPaymentDetails;
    if (scheduledPaymentDetails == null)
      selectedScheduledPaymentDetails = new ArrayList<FIN_PaymentScheduleDetail>();
    else
      selectedScheduledPaymentDetails = scheduledPaymentDetails;
    // FIXME: added to access the FIN_PaymentSchedule and FIN_PaymentScheduleDetail tables to be
    // removed when new security implementation is done
    OBContext.setAdminMode();
    try {
      // selected scheduled payments list
      final List<SCOPrepayment> tempSelectedPrepayments = FIN_Utility
          .getOBObjectList(SCOPrepayment.class, strSelectedPaymentDetailsIds);

      for (int j = 0; j < tempSelectedPrepayments.size(); j++) {
        if (tempSelectedPrepayments.get(j) == null) {
          tempSelectedPrepayments.remove(j);
          j--;
        }
      }

      List<FIN_PaymentScheduleDetail> tempSelectedScheduledPaymentDetails = new ArrayList<FIN_PaymentScheduleDetail>();
      for (int i = 0; i < tempSelectedPrepayments.size(); i++) {
        boolean ispayout = true;
        if (tempSelectedPrepayments.get(i).isPaymentComplete())
          ispayout = false;
        tempSelectedScheduledPaymentDetails.add(AdvPaymentMngtDao
            .convertoFromPrepaymentToSchedule(tempSelectedPrepayments.get(i), ispayout));
      }

      for (FIN_PaymentScheduleDetail tempPaymentScheduleDetail : tempSelectedScheduledPaymentDetails) {
        boolean found = false;
        for (FIN_PaymentScheduleDetail selectedScheduledPaymentDetail : selectedScheduledPaymentDetails) {
          if (tempPaymentScheduleDetail.getId().equals(selectedScheduledPaymentDetail.getId())) {
            found = true;
            break;
          }
        }

        if (!found)
          selectedScheduledPaymentDetails.add(tempPaymentScheduleDetail);
      }

    } finally {
      OBContext.restorePreviousMode();
    }
    return selectedScheduledPaymentDetails;
  }

  private static List<FIN_PaymentScheduleDetail> getSelectedAnticiposWithAmounts(
      VariablesSecureApp vars, List<FIN_PaymentScheduleDetail> scheduledPaymentDetails,
      String strSelectedPaymentDetailsIds, FIN_Payment payment) {

    List<FIN_PaymentScheduleDetail> selectedScheduledPaymentDetails;
    if (scheduledPaymentDetails == null)
      selectedScheduledPaymentDetails = new ArrayList<FIN_PaymentScheduleDetail>();
    else
      selectedScheduledPaymentDetails = scheduledPaymentDetails;
    // FIXME: added to access the FIN_PaymentSchedule and FIN_PaymentScheduleDetail tables to be
    // removed when new security implementation is done
    OBContext.setAdminMode();
    try {
      // selected scheduled payments list
      final List<SCOPrepayment> tempSelectedPrepayments = FIN_Utility
          .getOBObjectList(SCOPrepayment.class, strSelectedPaymentDetailsIds);

      for (int j = 0; j < tempSelectedPrepayments.size(); j++) {
        if (tempSelectedPrepayments.get(j) == null) {
          tempSelectedPrepayments.remove(j);
          j--;
        }
      }

      List<FIN_PaymentScheduleDetail> tempSelectedScheduledPaymentDetails = new ArrayList<FIN_PaymentScheduleDetail>();
      for (int i = 0; i < tempSelectedPrepayments.size(); i++) {
        SCOPrepayment prep = tempSelectedPrepayments.get(i);
        boolean docissotrx = prep.isSalesTransaction();
        BigDecimal paymentamount = new BigDecimal(
            vars.getNumericParameter("inpPaymentAmount" + prep.getId(), "0.00"));
        boolean ispayout = true;

        if (docissotrx == payment.isReceipt()) {

          if ((prep.getAmount().compareTo(new BigDecimal(0)) > 0
              && paymentamount.compareTo(new BigDecimal(0)) < 0)
              || (prep.getAmount().compareTo(new BigDecimal(0)) < 0
                  && paymentamount.compareTo(new BigDecimal(0)) > 0))
            ispayout = false;
          tempSelectedScheduledPaymentDetails.add(AdvPaymentMngtDao
              .convertoFromPrepaymentToSchedule(tempSelectedPrepayments.get(i), ispayout));
        } else {
          if ((prep.getAmount().compareTo(new BigDecimal(0)) < 0
              && paymentamount.compareTo(new BigDecimal(0)) < 0)
              || (prep.getAmount().compareTo(new BigDecimal(0)) > 0
                  && paymentamount.compareTo(new BigDecimal(0)) > 0))
            ispayout = false;
          tempSelectedScheduledPaymentDetails.add(AdvPaymentMngtDao
              .convertoFromPrepaymentToSchedule(tempSelectedPrepayments.get(i), ispayout));

        }
      }

      for (FIN_PaymentScheduleDetail tempPaymentScheduleDetail : tempSelectedScheduledPaymentDetails) {
        boolean found = false;
        for (FIN_PaymentScheduleDetail selectedScheduledPaymentDetail : selectedScheduledPaymentDetails) {
          if (tempPaymentScheduleDetail.getId().equals(selectedScheduledPaymentDetail.getId())) {
            found = true;
            break;
          }
        }

        if (!found)
          selectedScheduledPaymentDetails.add(tempPaymentScheduleDetail);
      }
    } catch (

    final Exception e) {
      e.printStackTrace(System.err);
      throw new OBException(e);
    } finally {
      OBContext.restorePreviousMode();
    }
    return selectedScheduledPaymentDetails;
  }

  private static List<FIN_PaymentScheduleDetail> getSelectedEntregaARendir(
      List<FIN_PaymentScheduleDetail> scheduledPaymentDetails, String strSelectedPaymentDetailsIds,
      FIN_Payment payment) {
    List<FIN_PaymentScheduleDetail> selectedScheduledPaymentDetails;
    if (scheduledPaymentDetails == null)
      selectedScheduledPaymentDetails = new ArrayList<FIN_PaymentScheduleDetail>();
    else
      selectedScheduledPaymentDetails = scheduledPaymentDetails;
    // FIXME: added to access the FIN_PaymentSchedule and FIN_PaymentScheduleDetail tables to be
    // removed when new security implementation is done
    OBContext.setAdminMode();
    try {
      // selected scheduled payments list
      final List<ScoRendicioncuentas> tempSelectedEntregas = FIN_Utility
          .getOBObjectList(ScoRendicioncuentas.class, strSelectedPaymentDetailsIds);

      for (int j = 0; j < tempSelectedEntregas.size(); j++) {
        if (tempSelectedEntregas.get(j) == null) {
          tempSelectedEntregas.remove(j);
          j--;
        }
      }

      List<FIN_PaymentScheduleDetail> tempSelectedScheduledPaymentDetails = new ArrayList<FIN_PaymentScheduleDetail>();
      for (int i = 0; i < tempSelectedEntregas.size(); i++) {
        tempSelectedScheduledPaymentDetails.add(AdvPaymentMngtDao
            .convertoFromRendcuentasToSchedule(tempSelectedEntregas.get(i), payment));
      }

      for (FIN_PaymentScheduleDetail tempPaymentScheduleDetail : tempSelectedScheduledPaymentDetails) {
        if (!selectedScheduledPaymentDetails.contains(tempPaymentScheduleDetail))
          selectedScheduledPaymentDetails.add(tempPaymentScheduleDetail);

      }

    } finally {
      OBContext.restorePreviousMode();
    }
    return selectedScheduledPaymentDetails;
  }

  /*
   * private FieldProvider[] getShownAnticipos(VariablesSecureApp vars,
   * List<FIN_PaymentScheduleDetail> selectedScheduledPaymentDetails,
   * List<FIN_PaymentScheduleDetail> filteredScheduledPaymentDetails, String
   * strSelectedPaymentDetails){
   * 
   * String strSelectedRecords = ""; if (!"".equals(strSelectedPaymentDetails) &&
   * strSelectedPaymentDetails != null) { strSelectedRecords = strSelectedPaymentDetails;
   * strSelectedRecords = strSelectedRecords.replace("(", ""); strSelectedRecords =
   * strSelectedRecords.replace(")", ""); }
   * 
   * final List<FIN_PaymentScheduleDetail> shownScheduledPaymentDetails = new
   * ArrayList<FIN_PaymentScheduleDetail>();
   * shownScheduledPaymentDetails.addAll(selectedScheduledPaymentDetails);
   * shownScheduledPaymentDetails.addAll(filteredScheduledPaymentDetails);
   * 
   * FIN_PaymentScheduleDetail[] FIN_PaymentScheduleDetails = new FIN_PaymentScheduleDetail[0];
   * FIN_PaymentScheduleDetails = shownScheduledPaymentDetails.toArray(FIN_PaymentScheduleDetails);
   * FieldProvider[] data =
   * FieldProviderFactory.getFieldProviderArray(shownScheduledPaymentDetails); String dateFormat =
   * OBPropertiesProvider.getInstance().getOpenbravoProperties() .getProperty("dateFormat.java");
   * SimpleDateFormat dateFormater = new SimpleDateFormat(dateFormat);
   * 
   * OBContext.setAdminMode(); try { for (int i = 0; i < data.length; i++) {
   * 
   * 
   * 
   * String selectedId = (selectedScheduledPaymentDetails .contains(FIN_PaymentScheduleDetails[i]))
   * ? FIN_PaymentScheduleDetails[i].getId() : "";
   * 
   * String selectedRecord = FIN_PaymentScheduleDetails[i].getId(); String psdAmount =
   * vars.getNumericParameter("inpPaymentAmount" + selectedRecord, "");
   * 
   * 
   * FieldProviderFactory.setField(data[i], "finSelectedPaymentDetailId", selectedId);
   * FieldProviderFactory.setField(data[i], "paymentAmount", psdAmount);
   * 
   * FieldProviderFactory.setField(data[i], "finScheduledPaymentDetailId",
   * FIN_PaymentScheduleDetails[i].getId());
   * 
   * FieldProviderFactory.setField(data[i], "orderNr", ""); FieldProviderFactory.setField(data[i],
   * "orderNrTrunc", ""); FieldProviderFactory.setField(data[i], "orderPaymentScheduleId", "");
   * 
   * FieldProviderFactory.setField(data[i], "invoiceNr", ""); FieldProviderFactory.setField(data[i],
   * "invoiceNrTrunc", ""); FieldProviderFactory.setField(data[i], "invoicePaymentScheduleId", "");
   * 
   * FieldProviderFactory.setField( data[i], "transactionDate", dateFormater.format(
   * FIN_PaymentScheduleDetails[i].getCreationDate()));
   * 
   * FieldProviderFactory.setField(data[i], "invoicedAmount", FIN_PaymentScheduleDetails[i]
   * .getAmount().toString()); FieldProviderFactory.setField(data[i], "expectedAmount",
   * FIN_PaymentScheduleDetails[i] .getScoDetretamt().toString());
   * 
   * // Truncate Business Partner String businessPartner =
   * FIN_PaymentScheduleDetails[i].getBusinessPartner().getIdentifier();
   * FieldProviderFactory.setField(data[i], "businessPartnerId", FIN_PaymentScheduleDetails[i]
   * .getBusinessPartner().getId()); String truncateBusinessPartner = (businessPartner.length() >
   * 18) ? businessPartner .substring(0, 15).concat("...").toString() : businessPartner;
   * FieldProviderFactory.setField(data[i], "businessPartnerName", (businessPartner.length() > 18) ?
   * businessPartner : ""); FieldProviderFactory.setField(data[i], "businessPartnerNameTrunc",
   * truncateBusinessPartner);
   * 
   * // Truncate Payment Method FieldProviderFactory.setField(data[i], "paymentMethodName", "");
   * FieldProviderFactory.setField(data[i], "paymentMethodNameTrunc", "");
   * 
   * FieldProviderFactory.setField(data[i], "outstandingAmount", FIN_PaymentScheduleDetails[i]
   * .getScoDetretamt().toString()); FieldProviderFactory.setField(data[i], "doubtfulDebtAmount",
   * "0.00".toString()); FieldProviderFactory.setField(data[i], "displayDoubtfulDebt",
   * "display: none;");
   * 
   * String strPaymentAmt = ""; String strDifference = "";
   * 
   * 
   * 
   * if (!"".equals(psdAmount)) { strDifference = FIN_PaymentScheduleDetails[i].getAmount()
   * .subtract(new BigDecimal(psdAmount)).toString(); }
   * 
   * FieldProviderFactory.setField(data[i], "invoicePhysicalDocNr", "");
   * FieldProviderFactory.setField(data[i], "invoicePhysicalDocNrTrunc", "");
   * FieldProviderFactory.setField(data[i], "invoiceDetractionPercent", "");
   * 
   * FieldProviderFactory.setField(data[i], "invoiceDetRetAmount", "0.00");
   * FieldProviderFactory.setField(data[i], "description", "");
   * FieldProviderFactory.setField(data[i], "difference", strDifference);
   * FieldProviderFactory.setField(data[i], "rownum", String.valueOf(i));
   * 
   * } }catch(Exception e){ System.out.println(e.getMessage()); e.printStackTrace(); } finally {
   * OBContext.restorePreviousMode(); } return data; }
   */

  private FieldProvider[] set() throws ServletException {
    HashMap<String, String> empty = new HashMap<String, String>();
    empty.put("finScheduledPaymentId", "");
    empty.put("salesOrderNr", "");
    empty.put("salesInvoiceNr", "");
    empty.put("expectedDate", "");
    empty.put("invoicedAmount", "");
    empty.put("invoicedSourceAmount", ""); // new-multicurrency
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
    } else if ("A".equals(strDocumenType) || "E".equals(strDocumenType)) {
      // no group
      return data;
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
      String _strSelectedScheduledPaymentDetailIds, boolean isMulticurrency,
      String paymentCurrencyId, BigDecimal exchangeRate, BigDecimal realExchangeRate,
      boolean isReceipt) throws ServletException {
    String strSelectedScheduledPaymentDetailIds = _strSelectedScheduledPaymentDetailIds;
    // Remove "(" ")"
    strSelectedScheduledPaymentDetailIds = strSelectedScheduledPaymentDetailIds.replace("(", "");
    strSelectedScheduledPaymentDetailIds = strSelectedScheduledPaymentDetailIds.replace(")", "");
    HashMap<String, BigDecimal> selectedPaymentScheduleDetailsAmounts = new HashMap<String, BigDecimal>();
    // As selected items may contain records with multiple IDs we as well need the records list as
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

      boolean docissotrx = isReceipt;
      FIN_PaymentScheduleDetail psd = OBDal.getInstance().get(FIN_PaymentScheduleDetail.class,
          record);
      if (psd != null) {
        if (psd.getInvoicePaymentSchedule() != null) {
          docissotrx = psd.getInvoicePaymentSchedule().getInvoice().isSalesTransaction();
        }
      }

      if (docissotrx != isReceipt) {
        recordAmount = recordAmount.negate();
      }

      HashMap<String, BigDecimal> recordsAmounts = null;

      String currState = vars.getStringParameter("inpRecordCurrency" + record, "");
      BigDecimal exchange = exchangeRate;
      BigDecimal realExchange = realExchangeRate;
      if (currState.equals("1")) {
        exchange = BigDecimal.ONE;
        realExchange = BigDecimal.ONE;
      }

      if (!isMulticurrency) {
        recordsAmounts = calculateAmounts(recordAmount, psdSet);
      } else {
        recordsAmounts = calculateAmounts(recordAmount, psdSet, paymentCurrencyId, exchange,
            realExchange);
      }

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
      VariablesSecureApp vars, String _strSelectedScheduledPaymentDetailIds,
      boolean isMulticurrency, String paymentCurrencyId, BigDecimal exchangeRate)
      throws ServletException {
    String strSelectedScheduledPaymentDetailIds = _strSelectedScheduledPaymentDetailIds;
    // Remove "(" ")"
    strSelectedScheduledPaymentDetailIds = strSelectedScheduledPaymentDetailIds.replace("(", "");
    strSelectedScheduledPaymentDetailIds = strSelectedScheduledPaymentDetailIds.replace(")", "");
    HashMap<String, BigDecimal> selectedPaymentScheduleDetailsAmounts = new HashMap<String, BigDecimal>();
    // As selected items may contain records with multiple IDs we as well need the records list as
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
          vars.getNumericParameter("inpInvoiceDetRetAmount" + record, "0.00"));

      String currState = vars.getStringParameter("inpRecordCurrency" + record, "");
      BigDecimal exchange = exchangeRate;
      if (currState.equals("1")) {
        exchange = BigDecimal.ONE;
      }

      HashMap<String, BigDecimal> recordsAmounts = null;
      if (!isMulticurrency) {
        recordsAmounts = calculateAmounts(recordAmount, psdSet);
      } else {
        recordsAmounts = calculateAmounts(recordAmount, psdSet, paymentCurrencyId, exchange,
            exchange);
      }

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
    // As selected items may contain records with multiple IDs we as well need the records list as
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
  private HashMap<String, BigDecimal> calculateAmounts(BigDecimal recordAmount, Set<String> psdSet,
      String paymentCurrencyId, BigDecimal exchangeRate, BigDecimal realExchangeRate) {
    BigDecimal remainingAmount = recordAmount.multiply(exchangeRate).setScale(2,
        RoundingMode.HALF_UP);
    HashMap<String, BigDecimal> recordsAmounts = new HashMap<String, BigDecimal>();
    // PSD needs to be properly ordered to ensure negative amounts are processed first
    List<FIN_PaymentScheduleDetail> psds = getOrderedPaymentScheduleDetails(psdSet);

    BigDecimal outstandingAmount = BigDecimal.ZERO;

    for (FIN_PaymentScheduleDetail paymentScheduleDetail : psds) {
      if (paymentScheduleDetail.getPaymentDetails() != null) {
        // This schedule detail comes from an edited payment so outstanding amount needs to be
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

      outstandingAmount = outstandingAmount.divide(realExchangeRate, 2, RoundingMode.HALF_UP);

      // OJO- FIX-ME Redondeo
      outstandingAmount = outstandingAmount.multiply(exchangeRate).setScale(2,
          RoundingMode.HALF_UP);
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

  private HashMap<String, BigDecimal> calculateAmounts(BigDecimal recordAmount,
      Set<String> psdSet) {
    BigDecimal remainingAmount = recordAmount;
    HashMap<String, BigDecimal> recordsAmounts = new HashMap<String, BigDecimal>();
    // PSD needs to be properly ordered to ensure negative amounts are processed first
    List<FIN_PaymentScheduleDetail> psds = getOrderedPaymentScheduleDetails(psdSet);
    BigDecimal outstandingAmount = BigDecimal.ZERO;
    for (FIN_PaymentScheduleDetail paymentScheduleDetail : psds) {
      if (paymentScheduleDetail.getPaymentDetails() != null) {
        // This schedule detail comes from an edited payment so outstanding amount needs to be
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
    // PSD needs to be properly ordered to ensure negative amounts are processed first
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
      if (paymentScheduleDetail.getOrderPaymentSchedule() == null && "O".equals(strDocumentType)) {
        storedNotSelectedPSDs.remove(paymentScheduleDetail);
      } else if (paymentScheduleDetail.getInvoicePaymentSchedule() == null
          && "I".equals(strDocumentType)) {
        storedNotSelectedPSDs.remove(paymentScheduleDetail);
      } else if (paymentScheduleDetail.getScoPrepayment() == null
          && paymentScheduleDetail.getScoPayoutprepayment() == null
          && "A".equals(strDocumentType)) {
        storedNotSelectedPSDs.remove(paymentScheduleDetail);
      } else if (paymentScheduleDetail.getSCOEntregaARendir() == null
          && paymentScheduleDetail.getScoPayoutrendcuentas() == null
          && "E".equals(strDocumentType)) {
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
      log4j.error("AddOrderOrInvoice - Callback", e);
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

  /*
   * private void refreshInvoiceRefCombo(HttpServletResponse response, String strOrgId, String
   * strPartnerId, boolean isReceipt) throws IOException, ServletException {
   * 
   * String invoiceRefComboHtml = getListInvoiceRef(strOrgId, strPartnerId, false, isReceipt);
   * 
   * response.setContentType("text/html; charset=UTF-8"); PrintWriter out = response.getWriter();
   * out.println(invoiceRefComboHtml.replaceAll("\"", "\\'"));
   * 
   * out.close(); }
   */

  public String getServletInfo() {
    return "Servlet that presents the payment proposal";
    // end of getServletInfo() method
  }

}

class PaymentScheduleWitholdingPayment {
  public FIN_PaymentSchedule paymentschedule;
  public BigDecimal withholdingamount;
  public BigDecimal paymentamount;

  static public int find(List<PaymentScheduleWitholdingPayment> paysched_withhos,
      FIN_PaymentSchedule paysched) {
    for (int i = 0; i < paysched_withhos.size(); i++) {
      if (paysched_withhos.get(i).paymentschedule.getId().equals(paysched.getId())) {
        return i;
      }
    }

    return -1;
  }

  public PaymentScheduleWitholdingPayment() {
    withholdingamount = new BigDecimal(0);
    paymentamount = new BigDecimal(0);
    paymentschedule = null;
  }
}

class PWithholdingReceipt {
  public BusinessPartner cbpartner;
  public Currency currency;
  public List<PaymentScheduleWitholdingPayment> lines;

  static public int find(List<PWithholdingReceipt> pwithholding_receipts,
      BusinessPartner cbpartner_, Currency currency_) {
    for (int i = 0; i < pwithholding_receipts.size(); i++) {
      if (pwithholding_receipts.get(i).cbpartner.getId().equals(cbpartner_.getId())
          && pwithholding_receipts.get(i).currency.getId().equals(currency_.getId())) {
        return i;
      }
    }

    return -1;
  }

  public PWithholdingReceipt() {
    cbpartner = null;
    currency = null;
    lines = new ArrayList<PaymentScheduleWitholdingPayment>();
  }

}