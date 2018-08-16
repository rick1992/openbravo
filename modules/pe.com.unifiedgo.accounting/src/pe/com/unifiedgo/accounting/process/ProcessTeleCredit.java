package pe.com.unifiedgo.accounting.process;

import java.util.Date;
import java.util.List;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.ad.access.User;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;

import pe.com.unifiedgo.accounting.data.SCOTelecredit;
import pe.com.unifiedgo.accounting.data.SCOTelecreditLine;

public class ProcessTeleCredit extends DalBaseProcess {
  public void doExecute(ProcessBundle bundle) throws Exception {
    try {

      /*
       * Set<String> params = bundle.getParams().keySet(); for (int i = 0; i < params.size(); i++) {
       * System.out.println(params.toArray()[i]); }
       */

      final String SCO_Telecredit_ID = (String) bundle.getParams().get("SCO_Telecredit_ID");
      final String docaction = (String) bundle.getParams().get("docaction");
      final VariablesSecureApp vars = bundle.getContext().toVars();
      OBContext.setOBContext(vars.getUser(), vars.getRole(), vars.getClient(), vars.getOrg());
      final ConnectionProvider conProvider = bundle.getConnection();
      final String language = bundle.getContext().getLanguage();
      User user = OBDal.getInstance().get(User.class, vars.getUser());

      int count = 0;

      SCOTelecredit telecredit = OBDal.getInstance().get(SCOTelecredit.class, SCO_Telecredit_ID);
      if (telecredit == null) {
        throw new Exception("Internal Error Null");
      }

      if (docaction.equals("CO")) {
        List<SCOTelecreditLine> telecreditlines = telecredit.getSCOTelecreditLineList();
        if (telecreditlines.size() == 0) {
          throw new OBException("@SCO_TeleCreditWithoutLines@");
        }

        if (telecredit.getReferenceNo() == null) {
          throw new OBException("@SCO_TeleCreditWithoutReferenceNo@");
        }
        if (telecredit.getReferenceNo().isEmpty()) {
          throw new OBException("@SCO_TeleCreditWithoutReferenceNo@");
        }
      }

      if (telecredit.getDocumentStatus().equals("CL") || telecredit.getDocumentStatus().equals("VO") || telecredit.getDocumentStatus().equals("RE")) {
        throw new OBException("@AlreadyPosted@");
      }

      if (telecredit.isProcessed().equals('Y') && !(docaction.equals("RC") || docaction.equals("RE") || docaction.equals("CL"))) {
        throw new OBException("@AlreadyPosted@");
      }

      if (docaction.equals("RE")) {
        telecredit.setProcessed(false);
        telecredit.setProcessNow(false);
        telecredit.setDocumentStatus("DR");
        telecredit.setDocumentAction("CO");
        telecredit.setProcesstelecredit("CO");
        telecredit.setUpdated(new Date());
        telecredit.setUpdatedBy(user);
        OBDal.getInstance().save(telecredit);

      } else {
        count = Integer.parseInt(ProcessTeleCreditData.checkDocType(conProvider, SCO_Telecredit_ID));
        if (count == 0) {
          throw new OBException("@SCO_NotCorrectOrgDoctypeTeleCredit@");
        }

        count = Integer.parseInt(ProcessTeleCreditData.checkDocTypeLines(conProvider, SCO_Telecredit_ID));
        if (count > 0) {
          throw new OBException("@NotCorrectOrgLines@");
        }

        telecredit.setDocumentType(telecredit.getTransactionDocument());
        telecredit.setUpdated(new Date());
        telecredit.setUpdatedBy(user);
        OBDal.getInstance().save(telecredit);
        OBDal.getInstance().flush();

        if (!(docaction.equals("CO") || docaction.equals("CL") || docaction.equals("PR"))) {
          throw new OBException("@ActionNotAllowedHere@");
        }

        if (docaction.equals("CO")) {

          telecredit.setProcessed(true);
          telecredit.setProcessNow(false);
          telecredit.setDocumentStatus("CO");
          telecredit.setDocumentAction("RE");
          telecredit.setProcesstelecredit("RE");
          telecredit.setUpdated(new Date());
          telecredit.setUpdatedBy(user);
          OBDal.getInstance().save(telecredit);
        } else if (docaction.equals("CL")) {

          telecredit.setProcessed(true);
          telecredit.setProcessNow(false);
          telecredit.setDocumentStatus("CL");
          telecredit.setDocumentAction("--");
          telecredit.setProcesstelecredit("--");
          telecredit.setUpdated(new Date());
          telecredit.setUpdatedBy(user);
          OBDal.getInstance().save(telecredit);

        } else {
          throw new OBException("@ActionNotAllowedHere@");
        }

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