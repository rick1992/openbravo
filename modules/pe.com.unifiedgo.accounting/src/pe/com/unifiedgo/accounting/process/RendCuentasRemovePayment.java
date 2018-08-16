package pe.com.unifiedgo.accounting.process;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.openbravo.advpaymentmngt.process.FIN_AddPayment;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;

import pe.com.unifiedgo.accounting.data.SCORendcurefundLine;
import pe.com.unifiedgo.accounting.data.ScoRendicioncuentas;

public class RendCuentasRemovePayment extends DalBaseProcess {
  public void doExecute(ProcessBundle bundle) throws Exception {
    try {

      /*
       * Set<String> params = bundle.getParams().keySet(); for (int i = 0; i < params.size(); i++) {
       * System.out.println(params.toArray()[i]); }
       */

      final String SCO_Rendicioncuentas_ID = (String) bundle.getParams()
          .get("SCO_Rendicioncuentas_ID");
      final VariablesSecureApp vars = bundle.getContext().toVars();
      OBContext.setOBContext(vars.getUser(), vars.getRole(), vars.getClient(), vars.getOrg());
      final ConnectionProvider conProvider = bundle.getConnection();
      final String language = bundle.getContext().getLanguage();
      User user = OBDal.getInstance().get(User.class, vars.getUser());

      int count = 0;

      ScoRendicioncuentas rendcuentas = OBDal.getInstance().get(ScoRendicioncuentas.class,
          SCO_Rendicioncuentas_ID);
      if (rendcuentas == null) {
        throw new Exception("Internal Error Null");
      }

      if (!rendcuentas.getDocumentStatus().equals("CO")
          || rendcuentas.getFINPaymentOpen() == null) {
        throw new OBException("@SCO_RendCuentasNotCO@");
      }

      List<SCORendcurefundLine> rendcu_refundlines = rendcuentas.getSCORendcurefundLineList();
      if (rendcu_refundlines.size() > 0) {
        throw new OBException("@SCO_RendCuentasReopenUsedAmountDiff0@");
      }

      List<FIN_PaymentDetail> pds = rendcuentas.getFINPaymentDetailEMScoRendcuentasIDList();
      if (pds.size() > 0) {
        throw new OBException("@SCO_RendCuentasReopenUsedAmountDiff0@");
      }

      // we will reactivate the payment
      FIN_Payment payment = rendcuentas.getFINPaymentOpen();
      if (payment != null) {

        rendcuentas.setFINPaymentOpen(null);
        rendcuentas.setPaymentDetails(null);
        rendcuentas.setRefund(new BigDecimal(0));
        rendcuentas.setUpdated(new Date());
        rendcuentas.setUpdatedBy(user);
        payment.getSCORendicioncuentasFINPaymentOpenIDList().remove(rendcuentas);
        OBDal.getInstance().save(rendcuentas);
        OBDal.getInstance().save(payment);
        OBDal.getInstance().flush();

        if (!payment.getStatus().equals("RPAP")) {
          OBError message = FIN_AddPayment.processPayment(vars, conProvider, "R", payment);
          if (message.getType().equals("Error")) {
            throw new OBException("@SCO_RemovePaymentErrorRevert@:" + message.getMessage());
          }
        }

        payment = OBDal.getInstance().get(FIN_Payment.class, payment.getId());

        if (payment.getStatus().compareTo("RPAP") != 0) {
          throw new OBException("@SCO_RemovePaymentErrorRevert@");
        }

        OBDal.getInstance().save(rendcuentas);
        OBDal.getInstance().remove(payment);
      }

      final OBError msg = new OBError();
      msg.setType("Success");
      msg.setTitle("@Success@");

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