package pe.com.unifiedgo.project.process;

import java.util.Set;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.project.Project;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;

public class ChangeProjectStatus extends DalBaseProcess {
  public void doExecute(ProcessBundle bundle) throws Exception {
    try {

      Set<String> params = bundle.getParams().keySet();
      for (int i = 0; i < params.size(); i++) {
        System.out.println(params.toArray()[i]);
      }

      final String C_Project_ID = (String) bundle.getParams().get("C_Project_ID");
      final String newprojectstatus = (String) bundle.getParams().get("newprojectstatus");

      final VariablesSecureApp vars = bundle.getContext().toVars();
      OBContext.setOBContext(vars.getUser(), vars.getRole(), vars.getClient(), vars.getOrg());
      final ConnectionProvider conProvider = bundle.getConnection();
      final String language = bundle.getContext().getLanguage();
      User user = OBDal.getInstance().get(User.class, vars.getUser());

      Project project = OBDal.getInstance().get(Project.class, C_Project_ID);
      if (project == null) {
        throw new Exception("Internal Error Null");
      }
      // System.out.println(
      // "project:" + project.getIdentifier() + " - newprojectstatus:" + newprojectstatus);

      if (newprojectstatus.equals("OP")) {
        if (!project.getProjectStatus().equals("SPR_EXE")) {
          throw new OBException("@ActionNotAllowedHere@");
        }

        // Validating if exists reserved/sold out properties in project
        for (int i = 0; i < project.getSSAProjectPropertyList().size(); i++) {
          if ("FREE"
              .compareTo(project.getSSAProjectPropertyList().get(i).getDocumentStatus()) != 0) {
            throw new OBException("@SSA_PropertyIsNotFree@");
          }
        }
        // Validating if exists complete budgets in project
        for (int i = 0; i < project.getSPRBudgetList().size(); i++) {
          if ("CO".compareTo(project.getSPRBudgetList().get(i).getDocumentStatus()) == 0) {
            throw new OBException("@SSA_ProjectBudgetComplete@");
          }
        }

        project.setProcessed(false);
        project.setProcessNow(false);
        project.setProjectStatus("OP");

        OBDal.getInstance().save(project);
        OBDal.getInstance().flush();

      } else if (newprojectstatus.equals("SPR_EXE")) {
        if (!project.getProjectStatus().equals("OP")
            && !project.getProjectStatus().equals("SPR_FIN")) {
          throw new OBException("@ActionNotAllowedHere@");
        }

        // El proyecto podria no tener presupuesto osea ningùn spr_budget inicialmente
        // if (project.getSPRBudgetList().size() == 0) {
        // throw new OBException("@NoProjectLines@");
        // }

        // Validating budget(type BDG) existence in project
        boolean isBDGType = false;
        for (int i = 0; i < project.getSPRBudgetList().size(); i++) {
          if ("BDG".compareTo(project.getSPRBudgetList().get(i).getType()) == 0) {
            isBDGType = true;
            break;
          }
        }

        // No es obligatorio que exista un presupuesto para el proyecto, por eso se comento lo
        // siguiente
        // if (!isBDGType) {
        // throw new OBException("@SPR_NoExistsBudgetInProject@");
        // }

        project.setProcessed(true);
        project.setProcessNow(false);
        project.setProjectStatus("SPR_EXE");

        OBDal.getInstance().save(project);
        OBDal.getInstance().flush();

      } else if (newprojectstatus.equals("SPR_FIN")) {
        if (!project.getProjectStatus().equals("SPR_EXE")) {
          throw new OBException("@ActionNotAllowedHere@");
        }

        project.setProcessed(true);
        project.setProcessNow(false);
        project.setProjectStatus("SPR_FIN");

        OBDal.getInstance().save(project);
        OBDal.getInstance().flush();

      } else {
        throw new OBException("@ActionNotAllowedHere@");
      }
      final OBError msg = new OBError();
      msg.setType("Success");
      msg.setTitle("@Success@");

      bundle.setResult(msg);
      OBDal.getInstance().commitAndClose();
    } catch (

    final OBException e) {
      final OBError msg = new OBError();
      msg.setType("Error");
      msg.setMessage(e.getMessage());
      msg.setTitle("@Error@");
      OBDal.getInstance().rollbackAndClose();
      bundle.setResult(msg);
    }
  }
}
