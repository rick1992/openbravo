package pe.com.unifiedgo.accounting.process;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.gl.GLJournal;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;

public class UndoManualLoanCJournal extends DalBaseProcess {
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

      GLJournal gljournal = invoice.getScoLoancGljournal();
      if (invoice.getDocumentStatus().compareTo("CO") != 0 || gljournal == null) {
        throw new OBException("@ActionNotAllowedHere@");
      }

      invoice.setScoLoancGljournal(null);
      OBDal.getInstance().flush();

      try {
        OBDal.getInstance().remove(gljournal);
        OBDal.getInstance().flush();

      } catch (Exception e) {
        e.printStackTrace();
        throw new OBException("@SCO_UndoLoanCJournalDELGLJournalError@");
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