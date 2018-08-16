package pe.com.unifiedgo.accounting.process;

import org.openbravo.advpaymentmngt.process.FIN_AddPayment;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;

import pe.com.unifiedgo.accounting.data.SCORendcurefundLine;
import pe.com.unifiedgo.accounting.data.ScoRendicioncuentas;

public class RendCuentasRemoveRefundPayment extends DalBaseProcess {
  public void doExecute(ProcessBundle bundle) throws Exception {
    try {

      /*
       * Set<String> params = bundle.getParams().keySet(); for (int i = 0; i < params.size(); i++) {
       * System.out.println(params.toArray()[i]); }
       */

      final String SCO_Rendcurefund_Line_ID = (String) bundle.getParams()
          .get("SCO_Rendcurefund_Line_ID");
      final VariablesSecureApp vars = bundle.getContext().toVars();
      OBContext.setOBContext(vars.getUser(), vars.getRole(), vars.getClient(), vars.getOrg());
      final ConnectionProvider conProvider = bundle.getConnection();
      final String language = bundle.getContext().getLanguage();
      User user = OBDal.getInstance().get(User.class, vars.getUser());

      int count = 0;

      SCORendcurefundLine rendcuentas_refundline = OBDal.getInstance()
          .get(SCORendcurefundLine.class, SCO_Rendcurefund_Line_ID);
      if (rendcuentas_refundline == null) {
        throw new Exception("Internal Error Null");
      }

      ScoRendicioncuentas rendcuentas = rendcuentas_refundline.getSCORendicioncuentas();
      if (rendcuentas == null) {
        throw new Exception("Internal Error Null");
      }

      if (!rendcuentas.getDocumentStatus().equals("CO")) {
        throw new OBException("@SCO_RendCuentasNotCO@");
      }

      // we will reactivate the payment
      FIN_Payment payment = rendcuentas_refundline.getFINPaymentRefund();
      if (payment != null) {

        payment.getSCORendcurefundLineFINPaymentRefundIDList().remove(rendcuentas_refundline);
        OBDal.getInstance().remove(rendcuentas_refundline);
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