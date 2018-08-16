package pe.com.unifiedgo.ebilling.ad_actionbutton;

import java.util.Set;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.ad.access.User;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;

import pe.com.unifiedgo.accounting.data.SCOPwithholdingReceipt;

public class Process_eBilling_PwithhoReceipt extends DalBaseProcess {
  public void doExecute(ProcessBundle bundle) throws Exception {
    try {

      System.out.println("PARAMS:");
      Set<String> params = bundle.getParams().keySet();
      for (int i = 0; i < params.size(); i++) {
        System.out.println(params.toArray()[i]);
      }

      final String SCO_Pwithholding_Receipt_ID = (String) bundle.getParams().get("SCO_Pwithholding_Receipt_ID");
      final VariablesSecureApp vars = bundle.getContext().toVars();
      OBContext.setOBContext(vars.getUser(), vars.getRole(), vars.getClient(), vars.getOrg());
      final ConnectionProvider conProvider = bundle.getConnection();
      final String language = bundle.getContext().getLanguage();
      User user = OBDal.getInstance().get(User.class, vars.getUser());

      SCOPwithholdingReceipt pwreceipt = OBDal.getInstance().get(SCOPwithholdingReceipt.class, SCO_Pwithholding_Receipt_ID);
      if (pwreceipt == null) {
        throw new Exception("Internal Error Null");
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