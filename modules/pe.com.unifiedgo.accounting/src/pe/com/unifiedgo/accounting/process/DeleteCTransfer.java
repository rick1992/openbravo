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

public class DeleteCTransfer extends DalBaseProcess {
  public void doExecute(ProcessBundle bundle) throws Exception {
    try {

      /*
       * Set<String> params = bundle.getParams().keySet(); for (int i = 0; i < params.size(); i++) {
       * System.out.println(params.toArray()[i]); }
       */

      final String Fin_Finacc_Transaction_ID = (String) bundle.getParams().get("Fin_Finacc_Transaction_ID");

      final VariablesSecureApp vars = bundle.getContext().toVars();
      OBContext.setOBContext(vars.getUser(), vars.getRole(), vars.getClient(), vars.getOrg());
      final ConnectionProvider conProvider = bundle.getConnection();
      final String language = bundle.getContext().getLanguage();
      User user = OBDal.getInstance().get(User.class, vars.getUser());

      FIN_FinaccTransaction trx = OBDal.getInstance().get(FIN_FinaccTransaction.class, Fin_Finacc_Transaction_ID);
      if (trx == null) {
        throw new Exception("Internal Error Null");
      }

      if (trx.getScoCtransPayin() == null) {
        throw new OBException("@ActionNotAllowedHere@");
      }

      FIN_FinaccTransaction ctrans_finacc = trx.getScoCtransFinacctrx();
      if (ctrans_finacc == null) {
        throw new OBException("@ActionNotAllowedHere@");
      }

      trx.setScoCtransFinacctrx(null);
      OBDal.getInstance().save(trx);
      OBDal.getInstance().flush();

      OBError msgt = processTransaction(vars, bundle.getConnection(), "R", ctrans_finacc);
      if (!"Success".equals(msgt.getType())) {
        throw new OBException("SCO_DeleteCTransCouldntDeleteTrx");
      }
      OBDal.getInstance().remove(ctrans_finacc);
      OBDal.getInstance().flush();

      // put the reconciliation and related payments em_sco_cashtransferstatus column to
      // 'SCO_TRANSIT' status.
      FIN_Payment payment = trx.getScoCtransPayin();
      payment.setScoCashtransferstatus("SCO_TRANSIT");

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