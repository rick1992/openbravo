package pe.com.unifiedgo.accounting.process;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetailV;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;
import org.openbravo.service.db.DbUtility;

import pe.com.unifiedgo.accounting.data.SCOInternalDoc;

public class ProcessUIdentTransaction extends DalBaseProcess {
  private AdvPaymentMngtDao dao;

  public void doExecute(ProcessBundle bundle) throws Exception {
    final VariablesSecureApp vars = bundle.getContext().toVars();
    OBContext.setOBContext(vars.getUser(), vars.getRole(), vars.getClient(), vars.getOrg());
    final ConnectionProvider conProvider = bundle.getConnection();
    final String language = bundle.getContext().getLanguage();
    User user = OBDal.getInstance().get(User.class, vars.getUser());
    
     try {

      /*Set<String> params = bundle.getParams().keySet();
      for (int i = 0; i < params.size(); i++) {
        System.out.println(params.toArray()[i]);
      }*/

      final String FIN_Payment_ID = (String) bundle.getParams().get("Fin_Payment_ID");
      final String strTransactionDate = (String) bundle.getParams().get("statementdate");
      final String strTrxnumber = (String) bundle.getParams().get("trxnumber");
      final String UIdent_Finacc_ID = (String) bundle.getParams().get("uidentFinaccId");
      final String C_Glitem_ID = (String) bundle.getParams().get("cGlitemId");
      final String SCO_Internal_Doc_ID = (String) bundle.getParams().get("scoInternalDocId");

      Date statementdate = FIN_Utility.getDate(strTransactionDate);
      //System.out.println("FIN_Payment_ID:" + FIN_Payment_ID + " - statementdate:" + statementdate + " - strTrxnumber:" + strTrxnumber + " - UIdent_Finacc_ID:" + UIdent_Finacc_ID + " - C_Glitem_ID:" + C_Glitem_ID + " - SCO_Internal_Doc_ID:" + SCO_Internal_Doc_ID);


      FIN_Payment payment = OBDal.getInstance().get(FIN_Payment.class, FIN_Payment_ID);
      FIN_FinancialAccount uifinacc = OBDal.getInstance().get(FIN_FinancialAccount.class, UIdent_Finacc_ID);
      GLItem uiglitem = OBDal.getInstance().get(GLItem.class, C_Glitem_ID);
      SCOInternalDoc internaldoc = OBDal.getInstance().get(SCOInternalDoc.class, SCO_Internal_Doc_ID);

      
      if (payment == null || uifinacc == null || uiglitem == null || internaldoc == null) {
        throw new Exception("Internal Error Null");
      }
      String recvapptype = payment.getScoRecvapplicationtype() != null ? payment.getScoRecvapplicationtype() : "";
      if((payment.getStatus().compareTo("RPR")!=0 && payment.getStatus().compareTo("RDNC")!=0 &&  payment.getStatus().compareTo("RPPC")!=0) || recvapptype.compareTo("SSA_UNIDENT_DEPOSIT")!=0 || payment.getScoUidenTrx() != null){
          throw new OBException("@ActionNotAllowedHere@");

      }
      
      List<FIN_PaymentDetail> pds = payment.getFINPaymentDetailList();
      BigDecimal uidentamt = new BigDecimal(0);
      for(int i=0; i<pds.size(); i++){
    	  FIN_PaymentDetail pd = pds.get(i);
    	  List<FIN_PaymentScheduleDetail> psds = pd.getFINPaymentScheduleDetailList();
    	  if(psds.size() > 0){
    		  FIN_PaymentScheduleDetail psd = psds.get(0);
        	  if(pd.getGLItem() != null && pd.getGLItem().getId().compareTo(uiglitem.getId()) == 0 && psd.getScoInternalDoc() != null && psd.getScoInternalDoc().getId().compareTo(internaldoc.getId()) == 0){
        		  uidentamt =  uidentamt.add(pd.getScoPaymentamount());
        	  }
    	  }
      }

      uidentamt = uidentamt.negate();
      if(uidentamt.compareTo(new BigDecimal(0)) <= 0){
          throw new OBException("@SCO_ProcessUIDentTrxZeroAmt@");

      }

      BigDecimal glItemDepositAmt =uidentamt;
      BigDecimal glItemPaymentAmt = new BigDecimal(0);
      boolean isReceipt = (glItemDepositAmt.compareTo(glItemPaymentAmt) >= 0);

      dao = new AdvPaymentMngtDao();
      FIN_FinaccTransaction finTrans = dao.getNewFinancialTransaction(uifinacc.getOrganization(), uifinacc, TransactionsDao.getTransactionMaxLineNo(uifinacc) + 10, null, payment.getDescription(), statementdate, uiglitem, !isReceipt ? "RDNC" : "PWNC", glItemDepositAmt, glItemPaymentAmt, null, null, null, !isReceipt ? "BPD" : "BPW", statementdate, null, null, null, null, null, null, null, null, null, internaldoc, null,null);
      finTrans.setScoTrxnumber(strTrxnumber);

      OBError processTransactionError = processTransaction(vars, conProvider, "P", finTrans);
      if (processTransactionError != null && "Error".equals(processTransactionError.getType())) {
        throw new OBException(processTransactionError.getMessage());
      }

      payment.setScoUidenTrx(finTrans);
      OBDal.getInstance().save(payment);
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
    }catch (Exception ex) {
		String msgType = "Error";
		String message = "";
		OBDal.getInstance().rollbackAndClose();
		Throwable exx = DbUtility.getUnderlyingSQLException(ex);
		msgType = "Error";
		message = OBMessageUtils.translateError(exx.getMessage()).getMessage();
		if (message.contains("@")) {
			message = OBMessageUtils.parseTranslation(message);
		}

		message = Utility.messageBD(conProvider, message,  language);
		// remove mysql message
		int pos = message.toLowerCase().indexOf("where: sql statement");
		if (pos != -1) {
			message = message.substring(0, pos);
		}

		OBError errmsg = new OBError();
		errmsg.setType(msgType);
		errmsg.setMessage(message);
		bundle.setResult(errmsg);
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