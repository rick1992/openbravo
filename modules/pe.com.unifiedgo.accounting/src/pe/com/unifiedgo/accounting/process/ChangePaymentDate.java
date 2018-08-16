package pe.com.unifiedgo.accounting.process;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.openbravo.advpaymentmngt.process.FIN_AddPayment;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetailV;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;
import org.openbravo.service.db.DbUtility;

import pe.com.unifiedgo.accounting.data.SCOPwithholdingReceipt;

public class ChangePaymentDate extends DalBaseProcess {
	public void doExecute(ProcessBundle bundle) throws Exception {
		final VariablesSecureApp vars = bundle.getContext().toVars();
		final ConnectionProvider conProvider = bundle.getConnection();

		try {

			/*
			 * Set<String> params = bundle.getParams().keySet(); for (int i = 0;
			 * i < params.size(); i++) {
			 * System.out.println(params.toArray()[i]); }
			 */

			final String FIN_Payment_ID = (String) bundle.getParams().get("Fin_Payment_ID");
			final String strPaymentDate = (String) bundle.getParams().get("paymentdate");

			String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty("dateFormat.java");
			SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_JAVA);
			Date paymentdate = sdf.parse(strPaymentDate);

			OBContext.setOBContext(vars.getUser(), vars.getRole(), vars.getClient(), vars.getOrg());
			final String language = bundle.getContext().getLanguage();
			User user = OBDal.getInstance().get(User.class, vars.getUser());

			FIN_Payment payment = OBDal.getInstance().get(FIN_Payment.class, FIN_Payment_ID);
			if (payment == null) {
				throw new OBException("@SCO_InternalError@");
			}

			String specialmethod = payment.getPaymentMethod().getScoSpecialmethod();
			if (specialmethod == null)
				specialmethod = "";

			if ((!payment.getStatus().equals("PPM") && !payment.getStatus().equals("PWNC"))) {
				throw new OBException("@ActionNotAllowedHere@");
			}

			if (payment.getPosted().compareTo("Y") == 0) {
				throw new OBException("@SCO_ChangePayDateErrPosted@");
			}

			if (payment.getAccount().getCurrency().getId().compareTo(payment.getCurrency().getId()) != 0) {
				throw new OBException("@SCO_ChangePayDateErrMultiCurr@");
			}

			payment.setProcessed(false);
			OBDal.getInstance().save(payment);
			OBDal.getInstance().flush();

			payment.setPaymentDate(paymentdate);
			OBDal.getInstance().save(payment);
			OBDal.getInstance().flush();

			List<FIN_FinaccTransaction> finaccs = payment.getFINFinaccTransactionList();
			for (int i = 0; i < finaccs.size(); i++) {
				FIN_FinaccTransaction finacc = finaccs.get(i);
				finacc.setProcessed(false);
				OBDal.getInstance().save(finacc);
				OBDal.getInstance().flush();

				finacc.setTransactionDate(paymentdate);
				OBDal.getInstance().save(finacc);
				OBDal.getInstance().flush();

				finacc.setProcessed(true);
				OBDal.getInstance().save(finacc);
				OBDal.getInstance().flush();
			}
			OBDal.getInstance().flush();

			List<FIN_PaymentDetailV> pdvs = payment.getFINPaymentDetailVList();
			for (int i = 0; i < pdvs.size(); i++) {
				FIN_PaymentDetailV pdv = pdvs.get(i);
				if (pdv.getPaymentPlanInvoice() != null) {
					Invoice invoice = pdv.getPaymentPlanInvoice().getInvoice();
					if (0 == invoice.getOutstandingAmount().compareTo(BigDecimal.ZERO)) {
						Date finalSettlementDate = FIN_AddPayment.getFinalSettlementDate(invoice);
						// If date is null invoice amount = 0 then nothing to
						// set
						if (finalSettlementDate != null) {
							invoice.setFinalSettlementDate(finalSettlementDate);
							invoice.setDaysSalesOutstanding(FIN_Utility.getDaysBetween(invoice.getInvoiceDate(), finalSettlementDate));
						}
						invoice.setPaymentComplete(true);
						OBDal.getInstance().save(invoice);
					}
				}
			}
			OBDal.getInstance().flush();

			// change related withholding receipts generated date(NOT NECESARY ANYMORE TRIGGER IMPLEMENTED)
			/*List<SCOPwithholdingReceipt> pwithhos = payment.getSCOPwithholdingReceiptFINWithholdingpaymentIDList();
			for (int i = 0; i < pwithhos.size(); i++) {
				SCOPwithholdingReceipt pwithho = pwithhos.get(i);
				if (pwithho.getDocumentStatus().compareTo("DR") != 0) {
					throw new OBException("@SCO_ChangePayDateWithhoReNotinDR@");
				}
				pwithho.setGeneratedDate(paymentdate);
				pwithho.setAccountingDate(paymentdate);
				OBDal.getInstance().save(pwithho);

			}
			OBDal.getInstance().flush();*/

			payment.setProcessed(true);
			OBDal.getInstance().save(payment);
			OBDal.getInstance().flush();

			final OBError msg = new OBError();
			msg.setType("Success");
			msg.setTitle("@Success@");
			msg.setMessage("@SCO_ChangePayDateSuccess@");
			bundle.setResult(msg);
			OBDal.getInstance().commitAndClose();
		} catch (final OBException e) {
			final OBError msg = new OBError();
			msg.setType("Error");
			msg.setMessage(e.getMessage());
			msg.setTitle("@Error@");
			OBDal.getInstance().rollbackAndClose();
			bundle.setResult(msg);
		} catch (final Exception ex) {
			String msgType = "Error";
			String message = "";

			OBDal.getInstance().rollbackAndClose();
			Throwable exx = DbUtility.getUnderlyingSQLException(ex);
			msgType = "Error";
			message = OBMessageUtils.translateError(exx.getMessage()).getMessage();
			if (message.contains("@")) {
				message = OBMessageUtils.parseTranslation(message);
			}

			message = Utility.messageBD(conProvider, message, vars.getLanguage());
			// remove mysql message
			int pos = message.toLowerCase().indexOf("where: sql statement");
			if (pos != -1) {
				message = message.substring(0, pos);
			}

			final OBError msg = new OBError();
			msg.setType("Error");
			msg.setMessage(message);
			msg.setTitle("@Error@");
			bundle.setResult(msg);

		}
	}
}