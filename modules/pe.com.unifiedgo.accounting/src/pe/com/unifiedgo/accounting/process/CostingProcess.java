package pe.com.unifiedgo.accounting.process;

import java.util.HashMap;
import java.util.Set;

import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.costing.CostingBackground;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;
import org.openbravo.service.db.DbUtility;

public class CostingProcess extends DalBaseProcess {
  private AdvPaymentMngtDao dao;

  public void doExecute(ProcessBundle bundle) throws Exception {
    final VariablesSecureApp vars = bundle.getContext().toVars();
    OBContext.setOBContext(vars.getUser(), vars.getRole(), vars.getClient(), vars.getOrg());
    final ConnectionProvider conProvider = bundle.getConnection();
    final String language = bundle.getContext().getLanguage();
    User user = OBDal.getInstance().get(User.class, vars.getUser());

    try {

      Set<String> params = bundle.getParams().keySet();
      for (int i = 0; i < params.size(); i++) {
        System.out.println(params.toArray()[i]);
      }

      final String strDateTo = (String) bundle.getParams().get("dateto");
      final String strAdOrgId = (String) bundle.getParams().get("adOrgId");
      System.out.println("strDateTo:" + strDateTo + " - strAdOrgId:" + strAdOrgId);
      final OBError msg = backgroundCostingProcess(vars, conProvider, strDateTo, strAdOrgId);

      bundle.setResult(msg);
      OBDal.getInstance().commitAndClose();
    } catch (final OBException e) {
      final OBError msg = new OBError();
      msg.setType("Error");
      msg.setMessage(e.getMessage());
      msg.setTitle("@Error@");
      OBDal.getInstance().rollbackAndClose();
      bundle.setResult(msg);
    } catch (Exception ex) {
      String msgType = "Error";
      String message = "";
      OBDal.getInstance().rollbackAndClose();
      Throwable exx = DbUtility.getUnderlyingSQLException(ex);
      msgType = "Error";
      message = OBMessageUtils.translateError(exx.getMessage()).getMessage();
      if (message.contains("@")) {
        message = OBMessageUtils.parseTranslation(message);
      }

      message = Utility.messageBD(conProvider, message, language);
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

  private OBError backgroundCostingProcess(VariablesSecureApp vars, ConnectionProvider conn, String strDateTo, String strAdOrgId) throws Exception {
    ProcessBundle pb = new ProcessBundle("3F2B4AAC707B4CE7B98D2005CF7310B5", vars).init(conn);
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("dateto", strDateTo);
    parameters.put("adOrgId", strAdOrgId);

    pb.setParams(parameters);
    OBError myMessage = null;
    new CostingBackground().execute(pb);
    myMessage = (OBError) pb.getResult();
    return myMessage;
  }
}