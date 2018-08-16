package pe.com.unifiedgo.accounting.process;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;

import pe.com.unifiedgo.migration.SMG_Utils;

public class UndoApplyToDUA extends DalBaseProcess {
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

      FIN_Payment payment = invoice.getScoDuaapppayment();
      if (payment == null) {
        throw new OBException("@ActionNotAllowedHere@");
      }

      if (payment.getStatus().compareTo("PWNC") == 0) {
        throw new OBException("@SCO_RemovePaymentErrorTrxExists@");
      }

      invoice.setScoDuaapppayment(null);
      payment.getInvoiceEMScoDuaapppaymentIDList().remove(invoice);
      OBDal.getInstance().save(invoice);
      OBDal.getInstance().save(payment);
      OBDal.getInstance().flush();

      OBDal.getInstance().refresh(payment);

      if (payment.getStatus().compareTo("PWNC") == 0) {
        throw new OBException("@SCO_RemovePaymentErrorTrxExists@");
      }

      if (payment.getStatus().compareTo("PPM") == 0) {
        try {
          SMG_Utils.revertPayment(vars, conProvider, payment);
        } catch (Exception e) {
          throw new OBException("@SCO_RemovePaymentErrorRevert@ " + e.getMessage());

        }
      }

      if (payment.getStatus().compareTo("RPAP") != 0) {
        throw new OBException("@SCO_RemovePaymentErrorRevert@");
      }

      OBDal.getInstance().remove(payment);
      OBDal.getInstance().flush();

      System.out.println("inv:" + invoice.getIdentifier());
      final OBError msg = new OBError();
      msg.setType("Success");
      msg.setTitle("@Success@");
      msg.setMessage("@SCO_UndoApplyToDUASuccess@");
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