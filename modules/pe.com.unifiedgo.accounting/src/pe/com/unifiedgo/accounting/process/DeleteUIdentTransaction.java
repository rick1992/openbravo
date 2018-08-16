package pe.com.unifiedgo.accounting.process;

import java.util.HashMap;

import org.openbravo.advpaymentmngt.process.FIN_TransactionProcess;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;

public class DeleteUIdentTransaction extends DalBaseProcess {
  public void doExecute(ProcessBundle bundle) throws Exception {
    try {

      /*
       * Set<String> params = bundle.getParams().keySet(); for (int i = 0; i < params.size(); i++) {
       * System.out.println(params.toArray()[i]); }
       */

        final String FIN_Payment_ID = (String) bundle.getParams().get("Fin_Payment_ID");

      final VariablesSecureApp vars = bundle.getContext().toVars();
      OBContext.setOBContext(vars.getUser(), vars.getRole(), vars.getClient(), vars.getOrg());
      final ConnectionProvider conProvider = bundle.getConnection();
      final String language = bundle.getContext().getLanguage();
      User user = OBDal.getInstance().get(User.class, vars.getUser());

      FIN_Payment payment = OBDal.getInstance().get(FIN_Payment.class, FIN_Payment_ID);
      if (payment == null) {
        throw new Exception("Internal Error Null");
      }
      String recvapptype = payment.getScoRecvapplicationtype() != null ? payment.getScoRecvapplicationtype() : "";
      if((payment.getStatus().compareTo("RPR")!=0 && payment.getStatus().compareTo("RDNC")!=0 &&  payment.getStatus().compareTo("RPPC")!=0) || recvapptype.compareTo("SSA_UNIDENT_DEPOSIT")!=0 || payment.getScoUidenTrx() == null){
          throw new OBException("@ActionNotAllowedHere@");

      }

      FIN_FinaccTransaction uident_finacc = payment.getScoUidenTrx();
      if ( uident_finacc == null) {
        throw new OBException("@ActionNotAllowedHere@");
      }

      payment.setScoUidenTrx(null);
      OBDal.getInstance().save(payment);
      OBDal.getInstance().flush();

      OBError msgt = processTransaction(vars, bundle.getConnection(), "R", uident_finacc);
      if (!"Success".equals(msgt.getType())) {
        throw new OBException("SCO_DeleteUIdentTrxCouldntDeleteTrx");
      }
      OBDal.getInstance().remove(uident_finacc);
      OBDal.getInstance().flush();

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

  private OBError processTransaction(VariablesSecureApp vars, ConnectionProvider conn, String strAction, FIN_FinaccTransaction transaction) throws Exception {
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
}