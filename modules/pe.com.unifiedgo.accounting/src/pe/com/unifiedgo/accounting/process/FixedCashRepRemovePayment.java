package pe.com.unifiedgo.accounting.process;

import java.util.Date;

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

import pe.com.unifiedgo.accounting.data.SCOFixedcashReposition;

public class FixedCashRepRemovePayment extends DalBaseProcess {
  public void doExecute(ProcessBundle bundle) throws Exception {
    try {

      /*
       * Set<String> params = bundle.getParams().keySet(); for (int i = 0; i < params.size(); i++) {
       * System.out.println(params.toArray()[i]); }
       */

      final String SCO_Fixedcash_Reposition_ID = (String) bundle.getParams()
          .get("SCO_Fixedcash_Reposition_ID");
      final VariablesSecureApp vars = bundle.getContext().toVars();
      OBContext.setOBContext(vars.getUser(), vars.getRole(), vars.getClient(), vars.getOrg());
      final ConnectionProvider conProvider = bundle.getConnection();
      final String language = bundle.getContext().getLanguage();
      User user = OBDal.getInstance().get(User.class, vars.getUser());

      int count = 0;

      SCOFixedcashReposition fixedcashrep = OBDal.getInstance().get(SCOFixedcashReposition.class,
          SCO_Fixedcash_Reposition_ID);
      if (fixedcashrep == null) {
        throw new Exception("Internal Error Null");
      }

      if (!fixedcashrep.getDocumentStatus().equals("CO")) {
        throw new OBException("@SCO_FixedCashRepNotCO@");
      }

      /*
       * FIN_Reconciliation recon = fixedcashrep.getReconciliation(); if (recon != null) { if
       * (!recon.getDocumentStatus().equals("DR")) { throw new
       * OBException("@SCO_FixedCashRepReconNotDR@"); } }
       */

      // we will reactivate the payment
      FIN_Payment payment = fixedcashrep.getPayment();
      if (payment != null) {

        fixedcashrep.setPayment(null);
        fixedcashrep.setPaymentDetails(null);
        fixedcashrep.setDocumentStatus("DR");
        fixedcashrep.setProcessed(false);
        fixedcashrep.setProcessNow(false);
        fixedcashrep.setUpdated(new Date());
        fixedcashrep.setUpdatedBy(user);
        payment.getSCOFixedcashRepositionList().remove(fixedcashrep);
        OBDal.getInstance().save(fixedcashrep);
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

        fixedcashrep.setFINFinancialAccountFrom(null);
        OBDal.getInstance().save(fixedcashrep);
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