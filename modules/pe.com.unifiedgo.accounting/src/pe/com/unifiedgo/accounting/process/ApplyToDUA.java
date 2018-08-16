package pe.com.unifiedgo.accounting.process;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.process.FIN_AddPayment;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;

import pe.com.unifiedgo.accounting.SCO_Utils;

public class ApplyToDUA extends DalBaseProcess {

  private List<FIN_PaymentScheduleDetail> getOrderedPaymentScheduleDetails(Set<String> psdSet) {
    OBCriteria<FIN_PaymentScheduleDetail> orderedPSDs = OBDal.getInstance()
        .createCriteria(FIN_PaymentScheduleDetail.class);
    orderedPSDs.add(Restrictions.in(FIN_PaymentScheduleDetail.PROPERTY_ID, psdSet));
    orderedPSDs.addOrderBy(FIN_PaymentScheduleDetail.PROPERTY_AMOUNT, true);
    return orderedPSDs.list();
  }

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

  private HashMap<String, BigDecimal> calculateAmounts(BigDecimal recordAmount, Set<String> psdSet,
      String paymentCurrencyId, BigDecimal exchangeRate, BigDecimal realExchangeRate) {
    BigDecimal remainingAmount = recordAmount.multiply(exchangeRate).setScale(2,
        RoundingMode.HALF_UP);
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

  public void doExecute(ProcessBundle bundle) throws Exception {
    try {

      /*
       * Set<String> params = bundle.getParams().keySet(); for (int i = 0; i < params.size(); i++) {
       * System.out.println(params.toArray()[i]); }
       */

      final String C_Invoice_ID = (String) bundle.getParams().get("C_Invoice_ID");

      final VariablesSecureApp vars = bundle.getContext().toVars();
      OBContext.setOBContext(vars.getUser(), vars.getRole(), vars.getClient(), vars.getOrg());
      final ConnectionProvider conProvider = bundle.getConnection();
      final String language = bundle.getContext().getLanguage();
      User user = OBDal.getInstance().get(User.class, vars.getUser());

      Invoice invoice = OBDal.getInstance().get(Invoice.class, C_Invoice_ID);
      if (invoice == null) {
        throw new OBException("@SCO_InternalError@");
      }

      if (invoice.getDocumentStatus().compareTo("CO") != 0 || invoice.isPaymentComplete()) {
        throw new OBException("@ActionNotAllowedHere@");
      }

      final List<Object> parameters = new ArrayList<Object>();
      parameters.add(vars.getClient());
      parameters.add(invoice.getOrganization().getId());
      parameters.add("APP");

      String strDocTypeId = SCO_Utils
          .getDocTypeFromSpecial(invoice.getOrganization(), "SCOAPPAYMENT").getId();
      String strDocBaseType = parameters.get(2).toString();
      String strPaymentDocumentNo = Utility.getDocumentNo(conProvider, vars,
          "AddPaymentFromInvoice"/*
                                  * solo referencia
                                  */, "FIN_Payment", strDocTypeId, strDocTypeId, false, true);

      String date = OBDateUtils.formatDate(invoice.getAccountingDate());

      if (!FIN_Utility.isPeriodOpen(vars.getClient(), strDocBaseType,
          invoice.getOrganization().getId(), date)) {

        throw new OBException("@PeriodNotAvailable@");

      }

      OrganizationStructureProvider osp = OBContext.getOBContext()
          .getOrganizationStructureProvider(invoice.getClient().getId());

      OBCriteria<FIN_PaymentMethod> obCriteria = OBDal.getInstance()
          .createCriteria(FIN_PaymentMethod.class);
      obCriteria.add(Restrictions.eq(FIN_PaymentMethod.PROPERTY_SCOSPECIALMETHOD, "SCONOTDEFINED"));
      obCriteria.add(Restrictions.in(FIN_PaymentMethod.PROPERTY_ORGANIZATION + ".id",
          osp.getParentTree(invoice.getOrganization().getId(), true)));

      List<FIN_PaymentMethod> paymentMethod = obCriteria.list();

      OBCriteria<FIN_FinancialAccount> obCriteria2 = OBDal.getInstance()
          .createCriteria(FIN_FinancialAccount.class);
      obCriteria2.add(Restrictions.eq(FIN_FinancialAccount.PROPERTY_SCOFORAPPPAYMENT, true));
      obCriteria2.add(Restrictions.eq(FIN_FinancialAccount.PROPERTY_TYPE, "B"));
      obCriteria2.add(Restrictions.eq(FIN_FinancialAccount.PROPERTY_CURRENCY + ".id",
          invoice.getCurrency().getId()));
      obCriteria2.add(Restrictions.in(FIN_PaymentMethod.PROPERTY_ORGANIZATION + ".id",
          osp.getParentTree(invoice.getOrganization().getId(), true)));

      List<FIN_FinancialAccount> finAcc = obCriteria2.list();

      AdvPaymentMngtDao dao = new AdvPaymentMngtDao();
      FIN_Payment payment = dao.getNewPayment(true, invoice.getOrganization(),
          dao.getObject(DocumentType.class, strDocTypeId), strPaymentDocumentNo, null,
          paymentMethod.get(0), finAcc.get(0), "0.00", invoice.getAccountingDate(), "",
          invoice.getCurrency(), BigDecimal.ONE, BigDecimal.ZERO, null);

      payment.setDescription("Aplicacion de DUA " + invoice.getScrPhysicalDocumentno());
      payment.setScoApplicationtype("SCO_OTHER");
      payment.setScoIsmigrationpayment(false);
      payment.setReferenceNo("");
      payment.setBusinessPartner(invoice.getBusinessPartner());
      payment.setReceipt(false);
      payment.setScoIsapppayment(true);

      try {
        OBDal.getInstance().flush();
      } catch (Exception e) {
        throw new OBException(FIN_Utility.getExceptionMessage(e));
      }

      // Asociar lineas
      List<FIN_PaymentScheduleDetail> selectedPaymentDetails = new ArrayList<FIN_PaymentScheduleDetail>();
      HashMap<String, BigDecimal> selectedPaymentDetailAmounts = new HashMap<String, BigDecimal>();

      BigDecimal grandAmount = new BigDecimal(0);

      ApplyToDUAData[] glitems_4699103 = ApplyToDUAData.selectGLItemByAcctValue(conProvider,
          invoice.getClient().getId(), invoice.getOrganization().getId(), "4699103");
      if (glitems_4699103.length == 0)
        throw new OBException("No existe Concepto Contable asociado a cuenta 4699103");

      GLItem item = OBDal.getInstance().get(GLItem.class, glitems_4699103[0].cGlitemId);

      List<InvoiceLine> invLines = invoice.getInvoiceLineList();

      for (int j = 0; j < invLines.size(); j++) {
        if (invLines.get(j).isFinancialInvoiceLine()
            && invLines.get(j).getAccount().getFinancialMgmtGLItemAccountsList().get(0)
                .getGlitemCreditAcct().getAccount().getSearchKey().equals("4699103"))
          grandAmount = grandAmount.add(invLines.get(j).getLineNetAmount());
      }

      List<FIN_PaymentScheduleDetail> psd = new ArrayList<FIN_PaymentScheduleDetail>();
      for (int j = 0; j < invoice.getFINPaymentScheduleList().size(); j++) {

        List<FIN_PaymentScheduleDetail> lsDetails = invoice.getFINPaymentScheduleList().get(j)
            .getFINPaymentScheduleDetailInvoicePaymentScheduleList();
        for (int k = 0; k < lsDetails.size(); k++) {
          if (lsDetails.get(k).isInvoicePaid())
            continue;
          psd.add(invoice.getFINPaymentScheduleList().get(j)
              .getFINPaymentScheduleDetailInvoicePaymentScheduleList().get(k));
        }

      }

      Set<String> psdSet = new LinkedHashSet<String>();
      for (int k = 0; k < psd.size(); k++)
        psdSet.add(psd.get(k).getId());

      HashMap<String, BigDecimal> recordsAmounts = null;

      recordsAmounts = calculateAmounts(grandAmount, psdSet);
      selectedPaymentDetailAmounts.putAll(recordsAmounts);

      for (int k = 0; k < psd.size(); k++)
        selectedPaymentDetails = FIN_AddPayment.getSelectedPaymentDetails(selectedPaymentDetails,
            psd.get(k).getId());

      // COMPLETAR PAGO

      payment.setAmount(BigDecimal.ZERO);
      FIN_AddPayment.setFinancialTransactionAmountAndRate(vars, payment, BigDecimal.ONE,
          BigDecimal.ZERO);

      try {
        OBDal.getInstance().flush();
      } catch (Exception e) {
        throw new OBException(FIN_Utility.getExceptionMessage(e));
      }

      FIN_AddPayment.saveGLItem(payment, grandAmount.negate(), item, null, null, null, null, null,
          null, null, null, null, null, invoice);

      payment = FIN_AddPayment.savePayment(vars, payment, false, null, null, null, null, null,
          "0.00", null, null, null, selectedPaymentDetails, selectedPaymentDetailAmounts, false,
          false, payment.getCurrency(), BigDecimal.ONE, BigDecimal.ZERO);

      OBDal.getInstance().flush();

      try {
        // Process just in case there are lines, empty Refund payment
        // does not need to call
        // process
        if (payment.getFINPaymentDetailList().size() > 0) {
          // If Action PRP o PPW, Process payment but as well create a
          // financial transaction
          OBError message = FIN_AddPayment.processPayment(vars, conProvider, "D", payment);
          if (!message.getType().equals("Success")) {
            throw new OBException(message.getMessage());
          }
        }
      } catch (Exception ex) {
        throw new Exception("Internal Error Null");
      }

      System.out.println("inv:" + invoice.getIdentifier());

      invoice.setScoDuaapppayment(payment);
      OBDal.getInstance().save(invoice);

      final OBError msg = new OBError();
      msg.setType("Success");
      msg.setTitle("@Success@");
      msg.setMessage("@SCO_ApplyToDUASuccess@");
      bundle.setResult(msg);
      OBDal.getInstance().commitAndClose();
    } catch (final OBException e) {
      final OBError msg = new OBError();
      msg.setType("Error");
      msg.setMessage(e.getMessage());
      msg.setTitle("@Error@");
      OBDal.getInstance().rollbackAndClose();
      bundle.setResult(msg);
    }
  }
}