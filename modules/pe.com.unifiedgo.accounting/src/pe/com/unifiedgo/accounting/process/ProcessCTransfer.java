package pe.com.unifiedgo.accounting.process;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.dao.TransactionsDao;
import org.openbravo.advpaymentmngt.process.FIN_TransactionProcess;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;

public class ProcessCTransfer extends DalBaseProcess {
  private AdvPaymentMngtDao dao;

  public void doExecute(ProcessBundle bundle) throws Exception {
    try {

      Set<String> params = bundle.getParams().keySet();
      for (int i = 0; i < params.size(); i++) {
        System.out.println(params.toArray()[i]);
      }

      final String Fin_Finacc_Transaction_ID = (String) bundle.getParams().get("Fin_Finacc_Transaction_ID");
      final String strTransactionDate = (String) bundle.getParams().get("statementdate");
      final String strTrxnumber = (String) bundle.getParams().get("trxnumber");
      final String Ctrans_Finacc_ID = (String) bundle.getParams().get("ctransFinaccId");
      Date statementdate = FIN_Utility.getDate(strTransactionDate);

      System.out.println("Fin_Finacc_Transaction_ID:" + Fin_Finacc_Transaction_ID + " - Ctrans_Finacc_ID:" + Ctrans_Finacc_ID + " - strTrxnumber:" + strTrxnumber);
      final VariablesSecureApp vars = bundle.getContext().toVars();
      OBContext.setOBContext(vars.getUser(), vars.getRole(), vars.getClient(), vars.getOrg());
      final ConnectionProvider conProvider = bundle.getConnection();
      final String language = bundle.getContext().getLanguage();
      User user = OBDal.getInstance().get(User.class, vars.getUser());

      FIN_FinaccTransaction trx = OBDal.getInstance().get(FIN_FinaccTransaction.class, Fin_Finacc_Transaction_ID);
      if (trx == null) {
        throw new Exception("Internal Error Null");
      }

      FIN_FinancialAccount ctrans_finacc = OBDal.getInstance().get(FIN_FinancialAccount.class, Ctrans_Finacc_ID);

      if (ctrans_finacc == null) {
        throw new OBException("@SCO_CTransferInvalidCTransAccount@");
      }

      if (trx.getScoCtransPayin() == null) {
        throw new OBException("@ActionNotAllowedHere@");
      }

      if (trx.getScoCtransFinacctrx() != null) {
        throw new OBException("@ActionNotAllowedHere@");
      }

      FIN_FinancialAccount finacc = trx.getAccount();
      if (finacc.getId().compareTo(ctrans_finacc.getId()) == 0 || finacc.getCurrency().getId().compareTo(ctrans_finacc.getCurrency().getId()) != 0) {
        throw new OBException("@SCO_CTransferInvalidCTransFinacc@");

      }

      DateFormat justDay = new SimpleDateFormat("yyyyMMdd");
      if (statementdate.compareTo(trx.getTransactionDate()) < 0) {
        throw new OBException("@SCO_CTransferInvalidTransDate@");
      }

      // set the selected finacc
      trx.setScoCtransFinacc(ctrans_finacc);
      OBDal.getInstance().save(trx);
      OBDal.getInstance().flush();

      BigDecimal glItemDepositAmt = trx.getDepositAmount().add(trx.getPaymentAmount());
      BigDecimal glItemPaymentAmt = new BigDecimal(0);
      boolean isReceipt = (glItemDepositAmt.compareTo(glItemPaymentAmt) >= 0);

      dao = new AdvPaymentMngtDao();
      FIN_FinaccTransaction finTrans = dao.getNewFinancialTransaction(ctrans_finacc.getOrganization(), ctrans_finacc, TransactionsDao.getTransactionMaxLineNo(ctrans_finacc) + 10, null, trx.getDescription(), statementdate, trx.getGLItem(), !isReceipt ? "RDNC" : "PWNC", glItemDepositAmt, glItemPaymentAmt, null, null, null, !isReceipt ? "BPD" : "BPW", statementdate, null, null, null, null, null, null, null, null, null, null, null,null);
      finTrans.setScoTrxnumber(strTrxnumber);

      OBError processTransactionError = processTransaction(vars, conProvider, "P", finTrans);
      if (processTransactionError != null && "Error".equals(processTransactionError.getType())) {
        throw new OBException(processTransactionError.getMessage());
      }

      trx.setScoCtransFinacctrx(finTrans);
      // payment em_sco_cashtransferstatus column to
      // 'SCO_BANK' status.
      trx.getScoCtransPayin().setScoCashtransferstatus("SCO_BANK");

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