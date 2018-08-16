package pe.com.unifiedgo.accounting.ad_actionbutton;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.currency.ConversionRate;
import org.openbravo.model.common.currency.ConversionRateDoc;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.financialmgmt.payment.FinAccPaymentMethod;
import org.openbravo.service.db.CallStoredProcedure;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.accounting.data.ScoRendicioncuentas;

public class AddPaymentFromRendicionCuentas extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private AdvPaymentMngtDao dao;
  private static final RequestFilter filterYesNo = new ValueListFilter("Y", "N", "");

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    int conversionRatePrecision = FIN_Utility.getConversionRatePrecision(vars);

    if (vars.commandIn("DEFAULT")) {

      /*
       * Enumeration<String> params= vars.getParameterNames(); while (params.hasMoreElements()) {
       * String key = (String) params.nextElement(); }
       */

      String strBPfromInvoiceId = vars.getRequestGlobalVariable("inpcBpartnerId", "");
      BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class, strBPfromInvoiceId);
      String strBPfromInvoice = bp == null ? "" : bp.getIdentifier();

      String strCurrencyId = vars.getRequestGlobalVariable("inpcCurrencyId", "");
      String strOrgId = vars.getRequestGlobalVariable("inpadOrgId", "");
      String strWindowId = vars.getGlobalVariable("inpwindowId", "");
      String strTabId = vars.getGlobalVariable("inpTabId", "");
      String strDocumentId = vars.getGlobalVariable("inpscoRendicioncuentasId", "");
      String strDocstatus = vars.getRequestGlobalVariable("inpdocstatus", "");

      ScoRendicioncuentas scoRendcuentas = OBDal.getInstance().get(ScoRendicioncuentas.class,
          strDocumentId);

      String strAmount = vars.getRequestGlobalVariable("inpamount", "");

      Organization org = OBDal.getInstance().get(Organization.class, strOrgId);

      String strGlitemId = org.getScoRendcuentasGlitem().getId();

      printPage(response, vars, strBPfromInvoice, strBPfromInvoiceId, strCurrencyId, strDocumentId,
          strOrgId, strWindowId, strTabId, strDocstatus, conversionRatePrecision, strGlitemId,
          strAmount, scoRendcuentas);

    } else if (vars.commandIn("PAYMENTMETHOD")) {
      String strFinancialAccountId = vars.getRequestGlobalVariable("inpFinancialAccount", "");
      String strPaymentMethodId = vars.getRequestGlobalVariable("inpPaymentMethod", "");
      String strOrgId = vars.getRequestGlobalVariable("inpadOrgId", "");
      String strDocstatus = vars.getRequiredStringParameter("docstatus");
      String strAmount = vars.getRequiredNumericParameter("inpActualPayment");

      NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
      Number number = null;
      try {
        number = format.parse(strAmount);
      } catch (Exception e) {
      }

      boolean isReceipt = false;

      refreshPaymentMethodCombo(response, strPaymentMethodId, strFinancialAccountId, strOrgId,
          isReceipt);

    } else if (vars.commandIn("FINANCIALACCOUNT")) {
      String strFinancialAccountId = vars.getRequestGlobalVariable("inpFinancialAccount", "");
      String strPaymentMethodId = vars.getRequiredStringParameter("inpPaymentMethod");
      String strOrgId = vars.getRequestGlobalVariable("inpadOrgId", "");
      String strCurrencyId = vars.getRequestGlobalVariable("inpCurrencyId", "");
      String strPaymentDate = vars.getRequestGlobalVariable("inpPaymentDate", "");
      String strDocstatus = vars.getRequiredStringParameter("docstatus");
      String strAmount = vars.getRequiredNumericParameter("inpActualPayment");

      NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
      Number number = null;
      try {
        number = format.parse(strAmount);
      } catch (Exception e) {
      }

      boolean isReceipt = false;

      String strDocumentId = vars.getRequestGlobalVariable("inpscoRendicioncuentasId", "");
      refreshFinancialAccountCombo(response, vars, strPaymentMethodId, strFinancialAccountId,
          strOrgId, strCurrencyId, isReceipt, strPaymentDate, conversionRatePrecision,
          strDocumentId);
    } else if (vars.commandIn("FILLFINANCIALACCOUNT")) {
      String strFinancialAccountId = vars.getRequestGlobalVariable("inpFinancialAccount", "");
      String strOrgId = vars.getRequestGlobalVariable("inpadOrgId", "");
      String strCurrencyId = vars.getRequestGlobalVariable("inpCurrencyId", "");
      String strPaymentDate = vars.getRequestGlobalVariable("inpPaymentDate", "");
      String strDocstatus = vars.getRequiredStringParameter("docstatus");
      String strAmount = vars.getRequiredNumericParameter("inpActualPayment");

      NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
      Number number = null;
      try {
        number = format.parse(strAmount);
      } catch (Exception e) {
      }

      boolean isReceipt = false;

      String strDocumentId = vars.getRequestGlobalVariable("inpscoRendicioncuentasId", "");
      refreshFinancialAccountCombo(response, vars, "", strFinancialAccountId, strOrgId,
          strCurrencyId, isReceipt, strPaymentDate, conversionRatePrecision, strDocumentId);

    } else if (vars.commandIn("FILLFINANCIALACCOUNTCURR")) {

      String strFinancialAccountId = vars.getRequestGlobalVariable("inpFinancialAccount", "");
      String strOrgId = vars.getRequestGlobalVariable("inpadOrgId", "");
      String strCurrencyId = vars.getRequestGlobalVariable("inpCurrencyId", "");
      String strPaymentDate = vars.getRequestGlobalVariable("inpPaymentDate", "");
      String strDocstatus = vars.getRequiredStringParameter("docstatus");
      String strAmount = vars.getRequiredNumericParameter("inpActualPayment");

      NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
      Number number = null;
      try {
        number = format.parse(strAmount);
      } catch (Exception e) {
      }

      boolean isReceipt = false;

      String strDocumentId = vars.getRequestGlobalVariable("inpscoRendicioncuentasId", "");
      refreshFinancialAccountCurr(response, vars, strFinancialAccountId, strOrgId, strCurrencyId,
          isReceipt, strPaymentDate, conversionRatePrecision, strDocumentId);

    } else if (vars.commandIn("FILLPAYMENTMETHOD")) {
      String strPaymentMethodId = vars.getRequiredStringParameter("inpPaymentMethod");
      String strOrgId = vars.getRequestGlobalVariable("inpadOrgId", "");
      String strDocstatus = vars.getRequiredStringParameter("docstatus");
      String strAmount = vars.getRequiredNumericParameter("inpActualPayment");

      NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
      Number number = null;
      try {
        number = format.parse(strAmount);
      } catch (Exception e) {
      }

      boolean isReceipt = false;

      refreshPaymentMethodCombo(response, strPaymentMethodId, "", strOrgId, isReceipt);

    } else if (vars.commandIn("EXCHANGERATE")) {
      final String strCurrencyId = vars.getRequestGlobalVariable("inpCurrencyId", "");
      final String strFinancialAccountCurrencyId = vars
          .getRequestGlobalVariable("inpFinancialAccountCurrencyId", "");
      final String strOrgId = vars.getRequestGlobalVariable("inpadOrgId", "");
      final String strPaymentDate = vars.getRequestGlobalVariable("inpPaymentDate", "");
      Organization org = OBDal.getInstance().get(Organization.class, strOrgId);
      String strDocumentId = vars.getRequestGlobalVariable("inpscoRendicioncuentasId", "");
      refreshExchangeRate(response, vars, strCurrencyId, strFinancialAccountCurrencyId,
          strPaymentDate, org, conversionRatePrecision, strDocumentId);

    } else if (vars.commandIn("BPARTNERBLOCK")) {
      boolean isReceipt = vars.getRequiredStringParameter("isReceipt").equals("Y");

      /*
       * String strDocstatus = vars.getRequestGlobalVariable("inpDocstatus", ""); String
       * strReceivedFromId = vars.getRequiredStringParameter("inpcBpartnerId"); BusinessPartner
       * businessPartner = OBDal.getInstance().get(BusinessPartner.class, strReceivedFromId);
       */

    } else if (vars.commandIn("SAVE") || vars.commandIn("SAVEANDPROCESS")) {

      boolean isReceipt = vars.getRequiredStringParameter("isReceipt").equals("Y");

      String strAction = null;
      if (vars.commandIn("SAVEANDPROCESS")) {
        // The default option is process
        strAction = (isReceipt ? "PRP" : "PPP");
      } else {
        strAction = vars.getRequiredStringParameter("inpActionDocument");
      }

      String strDocstatus = vars.getRequiredStringParameter("docstatus");

      String strPaymentDocumentNo = vars.getRequiredStringParameter("inpDocNumber");
      String strReceivedFromId = vars.getRequiredStringParameter("inpBusinessPartnerId");
      String strPaymentMethodId = vars.getRequiredStringParameter("inpPaymentMethod");
      String strFinancialAccountId = vars.getRequiredStringParameter("inpFinancialAccount");
      String convertedAmount = vars.getRequiredNumericParameter("inpActualPayment");
      String strPaymentDate = vars.getRequiredStringParameter("inpPaymentDate");

      FIN_FinancialAccount finacc = dao.getObject(FIN_FinancialAccount.class,
          strFinancialAccountId);

      String glitemId = vars.getRequiredStringParameter("glitemid");
      String strOrgId = vars.getRequiredStringParameter("inpadOrgId");

      String strDocumentId = vars.getRequiredStringParameter("inpcDocumentId");
      String strTabId = vars.getRequiredStringParameter("inpTabId");
      String strReferenceNo = vars.getStringParameter("inpReferenceNo", "");
      String paymentCurrencyId = finacc.getCurrency().getId();
      String docCurrencyId = vars.getRequiredStringParameter("inpCurrencyId");
      BigDecimal exchangeRate = BigDecimal.ONE.divide(
          new BigDecimal(vars.getRequiredNumericParameter("inpExchangeRate", "1")), 6,
          RoundingMode.HALF_UP);
      BigDecimal strPaymentAmount = new BigDecimal(
          vars.getRequiredNumericParameter("inpActualConverted", convertedAmount));
      BusinessPartner businessPartner = OBDal.getInstance().get(BusinessPartner.class,
          strReceivedFromId);

      OBError message = null;

      BigDecimal strConvertedAmount = new BigDecimal(convertedAmount);

      /*
       * PrintWriter writer = new PrintWriter("the-file-name.txt", "UTF-8");
       * writer.println("isReceipt:"+isReceipt); writer.println("strDocstatus:"+strDocstatus);
       * writer.println("strPaymentDocumentNo:"+strPaymentDocumentNo);
       * writer.println("strReceivedFromId:"+strReceivedFromId);
       * writer.println("strPaymentMethodId:"+strPaymentMethodId);
       * writer.println("strFinancialAccountId:"+strFinancialAccountId);
       * writer.println("strPaymentAmount:"+strPaymentAmount);
       * writer.println("strPaymentDate:"+strPaymentDate); writer.println("glitemId:"+glitemId);
       * writer.println("strDocumentId:"+strDocumentId);
       * writer.println("strReferenceNo:"+strReferenceNo);
       * writer.println("paymentCurrencyId:"+paymentCurrencyId);
       * writer.println("exchangeRate:"+exchangeRate);
       * writer.println("convertedAmount:"+convertedAmount);
       * writer.println("businessPartner:"+businessPartner);
       * 
       * writer.close();
       */

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

      OBContext.setAdminMode();
      try {

        ScoRendicioncuentas scoRendicionCuentas = OBDal.getInstance().get(ScoRendicioncuentas.class,
            strDocumentId);

        final List<Object> parameters = new ArrayList<Object>();
        parameters.add(vars.getClient());
        parameters.add(strOrgId);
        parameters.add((isReceipt ? "ARR" : "APP"));
        String strDocTypeId = (String) CallStoredProcedure.getInstance().call("AD_GET_DOCTYPE",
            parameters, null);
        String strDocBaseType = parameters.get(2).toString();

        if (!FIN_Utility.isPeriodOpen(vars.getClient(), strDocBaseType, strOrgId, strPaymentDate)) {
          final OBError myMessage = Utility.translateError(this, vars, vars.getLanguage(),
              Utility.messageBD(this, "PeriodNotAvailable", vars.getLanguage()));
          vars.setMessage(strTabId, myMessage);
          printPageClosePopUp(response, vars);
          return;
        }

        if (strConvertedAmount.compareTo(scoRendicionCuentas.getAmount()) != 0) {
          OBError msg = new OBError();
          msg.setType("Error");
          msg.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(),
              "@SCO_RendCuentasOPAmountMismatch@"));
          msg.setTitle(Utility.parseTranslation(this, vars, vars.getLanguage(), "@Error@"));
          OBDal.getInstance().rollbackAndClose();
          vars.setMessage(strTabId, msg);
          printPageClosePopUpAndRefreshParent(response, vars);
          OBDal.getInstance().rollbackAndClose();
          return;
        }

        if (strPaymentDocumentNo.startsWith("<")) {
          // get DocumentNo
          strPaymentDocumentNo = Utility.getDocumentNo(this, vars, "AddPaymentFromRendicionCuentas",
              "FIN_Payment", strDocTypeId, strDocTypeId, false, true);
        }

        List<FIN_PaymentScheduleDetail> selectedPaymentDetails = new ArrayList<FIN_PaymentScheduleDetail>();
        HashMap<String, BigDecimal> selectedPaymentDetailAmounts = new HashMap<String, BigDecimal>();

        FIN_Payment payment = dao.getNewPayment(isReceipt,
            dao.getObject(Organization.class, strOrgId),
            dao.getObject(DocumentType.class, strDocTypeId), strPaymentDocumentNo, businessPartner,
            dao.getObject(FIN_PaymentMethod.class, strPaymentMethodId), finacc,
            strPaymentAmount.toString(), FIN_Utility.getDate(strPaymentDate), strReferenceNo,
            dao.getObject(Currency.class, paymentCurrencyId), exchangeRate, strConvertedAmount,
            null);

        // Remove edited lines which are not in final selection and adjust outstanding amount for
        // documents
        removeNonSelectedDetails(payment, new ArrayList<FIN_PaymentScheduleDetail>());

        BigDecimal newPaymentAmount = strPaymentAmount;
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
            BigDecimal glItemAmtReal = BigDecimal.ZERO;

            String rendCuentasId = glItem.getString("glitemRendcuentasId");

            if (isReceipt) {
              glItemAmt = glItemInAmt.subtract(glItemOutAmt)
                  .multiply(
                      new BigDecimal(vars.getRequiredNumericParameter("inpExchangeRate", "1")))
                  .setScale(2, RoundingMode.HALF_UP);
              glItemAmtReal = glItemInAmt.subtract(glItemOutAmt);
            } else {
              glItemAmt = glItemOutAmt.subtract(glItemInAmt)
                  .multiply(
                      new BigDecimal(vars.getRequiredNumericParameter("inpExchangeRate", "1")))
                  .setScale(2, RoundingMode.HALF_UP);
              glItemAmtReal = glItemOutAmt.subtract(glItemInAmt);
            }
            final String strGLItemId = glItem.getString("glitemId");

            FIN_AddPayment.saveGLItem(payment, glItemAmtReal,
                dao.getObject(GLItem.class, strGLItemId), businessPartner, null,
                scoRendicionCuentas.getProject(), null, null, null, null, null, null,
                scoRendicionCuentas.getDescription(), glItemAmt, exchangeRate,
                OBDal.getInstance().get(Currency.class, docCurrencyId), null, null, null,
                rendCuentasId, null, null);

          }
        }

        FIN_AddPayment.setFinancialTransactionAmountAndRate(vars, payment, exchangeRate,
            newPaymentAmount);

        scoRendicionCuentas.setFINPaymentOpen(payment);
        scoRendicionCuentas.setRefund(scoRendicionCuentas.getAmount());
        OBDal.getInstance().save(scoRendicionCuentas);

        payment = FIN_AddPayment.savePayment(null, payment, isReceipt, null, null, null, null, null,
            strPaymentAmount.toString(), scoRendicionCuentas.getDategen(), null, null,
            selectedPaymentDetails, selectedPaymentDetailAmounts, false, false,
            finacc.getCurrency(), exchangeRate, strConvertedAmount);

        if (strAction.equals("PRP") || strAction.equals("PPP") || strAction.equals("PRD")
            || strAction.equals("PPW")) {
          try {

            if (payment.getFINPaymentDetailList().size() > 0) {
              // If Action PRP o PPW, Process payment but as well create a financial transaction

              List<FIN_PaymentDetail> pds = payment.getFINPaymentDetailList();
              for (int i = 0; i < pds.size(); i++) {
                FIN_PaymentDetail pd = pds.get(i);
                pd.setScoDescription(scoRendicionCuentas.getDescription());
                scoRendicionCuentas.setPaymentDetails(pd);
                OBDal.getInstance().save(pd);
                OBDal.getInstance().save(scoRendicionCuentas);
              }

              message = FIN_AddPayment.processPayment(vars, this,
                  (strAction.equals("PRP") || strAction.equals("PPP")) ? "P" : "D", payment);

              if (scoRendicionCuentas.getRespbpartner() != null) {
                // add the resp to the payment
                payment.setScoRespbpartner(scoRendicionCuentas.getRespbpartner());
                OBDal.getInstance().save(payment);
              }

              OBDal.getInstance().flush();
            }

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
        log4j.error("AddOrderOrInvoice - SAVE AND PROCESS", ex);
        return;

      } finally {
        OBContext.restorePreviousMode();
      }

      vars.setMessage(strTabId, message);
      printPageClosePopUpAndRefreshParent(response, vars);

    }
  }

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

  private void printPage(HttpServletResponse response, VariablesSecureApp vars,
      String strBPfromInvoice, String strBPfromInvoiceId, String strCurrencyId,
      String strDocumentId, String strOrgId, String strWindowId, String strTabId,
      String strDocstatus, int conversionRatePrecision, String strGlitemId, String strAmount,
      ScoRendicioncuentas scoRendcuentas) throws IOException, ServletException {

    NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
    Number number = null;
    try {
      number = format.parse(strAmount);
    } catch (Exception e) {
    }

    boolean isReceipt = false;

    dao = new AdvPaymentMngtDao();
    BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class, strBPfromInvoiceId);

    XmlDocument xmlDocument = xmlEngine
        .readXmlTemplate(
            "pe/com/unifiedgo/accounting/ad_actionbutton/AddPaymentFromRendicionCuentas")
        .createXmlDocument();

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());

    xmlDocument.setParameter("title",
        Utility.messageBD(this, "Apertura de Rendicion de Cuentas", vars.getLanguage()));

    xmlDocument.setParameter("dateDisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("paymentDate", DateTimeData.today(this));
    xmlDocument.setParameter("businessPartner", strBPfromInvoice);
    xmlDocument.setParameter("businessPartnerId", strBPfromInvoiceId);
    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("tabId", strTabId);
    xmlDocument.setParameter("orgId", strOrgId);
    xmlDocument.setParameter("documentId", strDocumentId);
    xmlDocument.setParameter("docstatus", strDocstatus);
    xmlDocument.setParameter("isReceipt", ((isReceipt) ? "Y" : "N"));

    GLItem item = (GLItem) OBDal.getInstance().get(GLItem.class, strGlitemId);

    xmlDocument.setParameter("glitemid", item.getId());
    xmlDocument.setParameter("glitemdescription", item.getName());
    xmlDocument.setParameter("glitemRendcuentasId", scoRendcuentas.getId());

    if (isReceipt) {
      xmlDocument.setParameter("glitemreceived", strAmount);
      xmlDocument.setParameter("glitempaidout", "0");
    } else {
      xmlDocument.setParameter("glitemreceived", "0");
      xmlDocument.setParameter("glitempaidout", strAmount);
    }

    /*
     * try { OBContext.setAdminMode(true); xmlDocument.setParameter( "credit", "0"); } finally {
     * OBContext.restorePreviousMode(); }
     */

    // get DocumentNo
    final List<Object> parameters = new ArrayList<Object>();
    parameters.add(vars.getClient());
    parameters.add(strOrgId);
    parameters.add((isReceipt ? "ARR" : "APP"));
    // parameters.add(null);
    String strDocTypeId = (String) CallStoredProcedure.getInstance().call("AD_GET_DOCTYPE",
        parameters, null);
    String strDocNo = Utility.getDocumentNo(this, vars, "AddPaymentFromRendicionCuentas",
        "FIN_Payment", strDocTypeId, strDocTypeId, false, false);
    xmlDocument.setParameter("documentNumber", "<" + strDocNo + ">");

    // get the default financialAccount and PaymentMethod for CHECK
    Organization org = OBDal.getInstance().get(Organization.class, strOrgId);
    FIN_FinancialAccount chkfinacc = null;
    FIN_PaymentMethod chkpaymethod = null;
    if (org != null) {
      chkfinacc = org.getScoChkfinacc();
      chkpaymethod = org.getScoChkpaymethod();
    }

    FIN_FinancialAccount account = null;
    FIN_PaymentMethod paymethod = null;
    if (chkfinacc != null && chkpaymethod != null) {
      account = chkfinacc;
      paymethod = chkpaymethod;
    } else {
      account = isReceipt ? bp.getAccount() : bp.getPOFinancialAccount();
      paymethod = isReceipt ? bp.getPaymentMethod() : bp.getPOPaymentMethod();
    }

    if (account == null || paymethod == null) {
      log4j.info("No default info for the selected business partner");
      account = null;
      paymethod = null;
    }

    // always leave the financial account empty so the user can only select the permited ones
    account = null;

    String strFinancialAccountId = account != null ? account.getId() : "";
    String strPaymentMethodId = paymethod != null ? paymethod.getId() : "";

    xmlDocument.setParameter("paymentMethod", strPaymentMethodId);
    xmlDocument.setParameter("paymentMethodDescription",
        AddPaymentFromData.selectPaymentmethod(this, strPaymentMethodId));
    xmlDocument.setParameter("financialAccount", strFinancialAccountId);
    xmlDocument.setParameter("financialAccountDescription",
        AddPaymentFromData.selectFinancialAccount(this, strFinancialAccountId));

    /*
     * final String strtypewriteoff; final String strAmountwriteoff; if (account != null) { if
     * (!financialAccounts.contains(account)) { strFinancialAccountId =
     * financialAccounts.get(0).getId(); if (financialAccounts.size() > 0 &&
     * financialAccounts.get(0).getWriteofflimit() != null) { strtypewriteoff =
     * financialAccounts.get(0).getTypewriteoff(); strAmountwriteoff =
     * financialAccounts.get(0).getWriteofflimit().toString();
     * xmlDocument.setParameter("strtypewriteoff", strtypewriteoff);
     * xmlDocument.setParameter("strAmountwriteoff", strAmountwriteoff); }
     * 
     * } else { if (account.getWriteofflimit() != null) { strtypewriteoff =
     * account.getTypewriteoff(); strAmountwriteoff = account.getWriteofflimit().toString();
     * xmlDocument.setParameter("strtypewriteoff", strtypewriteoff);
     * xmlDocument.setParameter("strAmountwriteoff", strAmountwriteoff); } } } else { if
     * (financialAccounts.size() > 0 && financialAccounts.get(0).getWriteofflimit() != null) {
     * strtypewriteoff = financialAccounts.get(0).getTypewriteoff(); strAmountwriteoff =
     * financialAccounts.get(0).getWriteofflimit().toString();
     * xmlDocument.setParameter("strtypewriteoff", strtypewriteoff);
     * xmlDocument.setParameter("strAmountwriteoff", strAmountwriteoff); } }
     */

    // Currency
    xmlDocument.setParameter("CurrencyId", strCurrencyId);
    final Currency paymentCurrency = dao.getObject(Currency.class, strCurrencyId);

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Currency_ID",
          "", "",
          Utility.getContext(this, vars, "#AccessibleOrgTree", "AddPaymentFromRendicionCuentas"),
          Utility.getContext(this, vars, "#User_Client", "AddPaymentFromRendicionCuentas"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "AddPaymentFromRendicionCuentas",
          strCurrencyId);
      xmlDocument.setData("reportC_Currency_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    try {
      OBContext.setAdminMode(true);

      final Currency financialAccountCurrency = dao
          .getFinancialAccountCurrency(strFinancialAccountId);
      if (financialAccountCurrency != null) {
        xmlDocument.setParameter("financialAccountCurrencyId", financialAccountCurrency.getId());
        xmlDocument.setParameter("financialAccountCurrencyPrecision",
            financialAccountCurrency.getStandardPrecision().toString());
      } else {
        xmlDocument.setParameter("financialAccountCurrencyPrecision", "2");
      }
      BigDecimal exchangeRate = findExchangeRate(vars, paymentCurrency, financialAccountCurrency,
          new Date(), OBDal.getInstance().get(Organization.class, strOrgId),
          conversionRatePrecision);

      if (exchangeRate == null) {
        final OBError myMessage = Utility.translateError(this, vars, vars.getLanguage(),
            Utility.messageBD(this, "NoCurrencyConversion", vars.getLanguage()));
        vars.setMessage(strTabId, myMessage);
        printPageClosePopUp(response, vars);
        return;
      }

      xmlDocument.setParameter("exchangeRate", exchangeRate.toPlainString());

    } finally {
      OBContext.restorePreviousMode();
    }

    boolean forcedFinancialAccountTransaction = false;
    forcedFinancialAccountTransaction = isForcedFinancialAccountTransaction(isReceipt,
        strFinancialAccountId, strPaymentMethodId);

    // Action Regarding Document
    xmlDocument.setParameter("ActionDocument", (isReceipt ? "PRP" : "PPP"));
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
          (isReceipt ? "F903F726B41A49D3860243101CEEBA25" : "F15C13A199A748F1B0B00E985A64C036"),
          forcedFinancialAccountTransaction ? "29010995FD39439D97A5C0CE8CE27D70" : "",
          Utility.getContext(this, vars, "#AccessibleOrgTree", "AddPaymentFromInvoice"),
          Utility.getContext(this, vars, "#User_Client", "AddPaymentFromInvoice"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "AddPaymentFromRendicionCuentas",
          "");
      xmlDocument.setData("reportActionDocument", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    // Not allow to change exchange rate and amount
    final String strNotAllowExchange = "N"; /*
                                             * Utility.getContext(this, vars,
                                             * "NotAllowChangeExchange", strWindowId);
                                             */
    xmlDocument.setParameter("strNotAllowExchange", strNotAllowExchange);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void refreshPaymentMethodCombo(HttpServletResponse response, String srtPaymentMethod,
      String strFinancialAccountId, String strOrgId, boolean isReceipt)
      throws IOException, ServletException {
    log4j.debug("Callout: Financial Account has changed to" + strFinancialAccountId);

    String paymentMethodComboHtml = FIN_Utility.getPaymentMethodList(srtPaymentMethod,
        strFinancialAccountId, strOrgId, true, true, isReceipt);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(paymentMethodComboHtml.replaceAll("\"", "\\'"));

    out.close();
  }

  private void refreshFinancialAccountCombo(HttpServletResponse response, VariablesSecureApp vars,
      String strPaymentMethodId, String strFinancialAccountId, String strOrgId,
      String strCurrencyId, boolean isReceipt, String paymentDate, int conversionRatePrecision,
      String strDocumentId) throws IOException, ServletException {
    log4j.debug("Callout: Payment Method has changed to " + strPaymentMethodId);

    dao = new AdvPaymentMngtDao();

    String finAccountComboHtml = FIN_Utility.getFinancialAccountList(strPaymentMethodId,
        strFinancialAccountId, strOrgId, true, strCurrencyId, isReceipt);

    final Currency financialAccountCurrency = dao
        .getFinancialAccountCurrency(strFinancialAccountId);
    final Currency paymentCurrency = dao.getObject(Currency.class, strCurrencyId);
    final String formatOutput = vars.getSessionValue("#FormatOutput|generalQtyRelation",
        "#,##0.######");

    BigDecimal exchangeRate = findExchangeRate(vars, paymentCurrency, financialAccountCurrency,
        FIN_Utility.getDate(paymentDate), OBDal.getInstance().get(Organization.class, strOrgId),
        conversionRatePrecision);

    FIN_FinancialAccount financialAccount = dao.getObject(FIN_FinancialAccount.class,
        strFinancialAccountId);

    JSONObject msg = new JSONObject();
    try {
      if (financialAccount != null && financialAccount.getWriteofflimit() != null) {
        msg.put("twriteoff", financialAccount.getTypewriteoff());
        msg.put("awriteoff", financialAccount.getWriteofflimit().toString());
      }
      msg.put("combo", finAccountComboHtml);
      if (financialAccountCurrency != null) {
        msg.put("financialAccountCurrencyId", financialAccountCurrency.getId());
        msg.put("financialAccountCurrencyPrecision",
            financialAccountCurrency.getStandardPrecision().toString());
      }
      msg.put("exchangeRate", exchangeRate == null ? "" : exchangeRate);
      msg.put("formatOutput", formatOutput);
    } catch (JSONException e) {
      log4j.error("JSON object error" + msg.toString());
    }

    response.setContentType("application/json; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(msg.toString());
    out.close();

  }

  private void refreshFinancialAccountCurr(HttpServletResponse response, VariablesSecureApp vars,
      String strFinancialAccountId, String strOrgId, String strCurrencyId, boolean isReceipt,
      String paymentDate, int conversionRatePrecision, String strDocumentId)
      throws IOException, ServletException {

    dao = new AdvPaymentMngtDao();

    final Currency financialAccountCurrency = dao
        .getFinancialAccountCurrency(strFinancialAccountId);
    final Currency paymentCurrency = dao.getObject(Currency.class, strCurrencyId);
    final String formatOutput = vars.getSessionValue("#FormatOutput|generalQtyRelation",
        "#,##0.######");

    BigDecimal exchangeRate = findExchangeRate(vars, paymentCurrency, financialAccountCurrency,
        FIN_Utility.getDate(paymentDate), OBDal.getInstance().get(Organization.class, strOrgId),
        conversionRatePrecision);
    FIN_FinancialAccount financialAccount = dao.getObject(FIN_FinancialAccount.class,
        strFinancialAccountId);

    JSONObject msg = new JSONObject();
    try {
      if (financialAccount != null && financialAccount.getWriteofflimit() != null) {
        msg.put("twriteoff", financialAccount.getTypewriteoff());
        msg.put("awriteoff", financialAccount.getWriteofflimit().toString());
      }
      if (financialAccountCurrency != null) {
        msg.put("financialAccountCurrencyId", financialAccountCurrency.getId());
        msg.put("financialAccountCurrencyPrecision",
            financialAccountCurrency.getStandardPrecision().toString());
      }
      msg.put("exchangeRate", exchangeRate == null ? "" : exchangeRate);
      msg.put("formatOutput", formatOutput);
    } catch (JSONException e) {
      log4j.error("JSON object error" + msg.toString());
    }

    response.setContentType("application/json; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(msg.toString());
    out.close();

  }

  private void refreshExchangeRate(HttpServletResponse response, VariablesSecureApp vars,
      String strCurrencyId, String strFinancialAccountCurrencyId, String strPaymentDate,
      Organization organization, int conversionRatePrecision, String strInvoiceId)
      throws IOException, ServletException {

    dao = new AdvPaymentMngtDao();

    final Currency financialAccountCurrency = dao.getObject(Currency.class,
        strFinancialAccountCurrencyId);
    final Currency paymentCurrency = dao.getObject(Currency.class, strCurrencyId);
    final String formatOutput = vars.getSessionValue("#FormatOutput|generalQtyRelation",
        "#,##0.######");

    BigDecimal exchangeRate = findExchangeRate(vars, paymentCurrency, financialAccountCurrency,
        FIN_Utility.getDate(strPaymentDate), organization, conversionRatePrecision);

    JSONObject msg = new JSONObject();
    try {
      msg.put("exchangeRate", exchangeRate == null ? "" : exchangeRate);
      msg.put("formatOutput", formatOutput);
    } catch (JSONException e) {
      log4j.error("JSON object error" + msg.toString());
    }
    response.setContentType("application/json; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(msg.toString());
    out.close();
  }

  private BigDecimal findExchangeRate(VariablesSecureApp vars, Currency paymentCurrency,
      Currency financialAccountCurrency, Date paymentDate, Organization organization,
      int conversionRatePrecision) {

    BigDecimal exchangeRate = BigDecimal.ONE;

    ConversionRateDoc conversionRateDoc = null;

    if (financialAccountCurrency != null && !financialAccountCurrency.equals(paymentCurrency)) {

      final ConversionRate conversionRate = FIN_Utility.getConversionRate(paymentCurrency,
          financialAccountCurrency, paymentDate, organization);
      if (conversionRate == null) {
        exchangeRate = null;
      } else {
        exchangeRate = conversionRate.getMultipleRateBy().setScale(conversionRatePrecision,
            RoundingMode.HALF_UP);
      }

    }
    return exchangeRate;
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

  public String getServletInfo() {
    return "Servlet that presents the payment proposal";
    // end of getServletInfo() method
  }

  /**
   * Returns the boolean value based on the transaction account has a automatic deposit or automatic
   * withdrawn value.
   * 
   * @param isReceipt
   *          . Indicates the transaction is belongs to purchase or sales.
   * @param strFinAccId
   *          . Indicates the financial account id for the transaction.
   * @param strPmtMethodId
   *          . Indicates the payment method id for the transaction. @return. Returns boolean value
   *          based on the automatic deposit or automatic withdrawn value.
   */
  private Boolean isForcedFinancialAccountTransaction(boolean isReceipt, String strFinAccId,
      String strPmtMethodId) {
    FIN_FinancialAccount finAcc = new AdvPaymentMngtDao().getObject(FIN_FinancialAccount.class,
        strFinAccId);
    FIN_PaymentMethod finPmtMethod = new AdvPaymentMngtDao().getObject(FIN_PaymentMethod.class,
        strPmtMethodId);
    OBCriteria<FinAccPaymentMethod> psdFilter = OBDal.getInstance()
        .createCriteria(FinAccPaymentMethod.class);
    psdFilter.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_ACCOUNT, finAcc));
    psdFilter.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD, finPmtMethod));
    for (FinAccPaymentMethod paymentMethod : psdFilter.list()) {
      return isReceipt ? paymentMethod.isAutomaticDeposit() : paymentMethod.isAutomaticWithdrawn();
    }
    return false;
  }

}
