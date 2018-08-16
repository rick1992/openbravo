package pe.com.unifiedgo.accounting.process;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Set;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.utility.Attachment;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;
import org.openbravo.service.db.DbUtility;

import pe.com.unifiedgo.accounting.data.SCOPle8_14_Reg;

public class PLE8_14_GetRegnumber extends DalBaseProcess {
  public void doExecute(ProcessBundle bundle) throws Exception {
    int entryno = 0;
    OBContext.setAdminMode(true);
    try {

      Set<String> params = bundle.getParams().keySet();
      for (int i = 0; i < params.size(); i++) {
        System.out.println(params.toArray()[i]);
      }

      final String SCO_PLE8_14_REG_ID = (String) bundle.getParams().get("SCO_Ple8_14_Reg_ID");
      final String strcFileId = (String) bundle.getParams().get("cfileid");
      final VariablesSecureApp vars = bundle.getContext().toVars();
      OBContext.setOBContext(vars.getUser(), vars.getRole(), vars.getClient(), vars.getOrg());
      final ConnectionProvider conProvider = bundle.getConnection();
      final String language = bundle.getContext().getLanguage();
      User user = OBDal.getInstance().get(User.class, vars.getUser());

      int count = 0;

      SCOPle8_14_Reg ple8_14_reg = OBDal.getInstance().get(SCOPle8_14_Reg.class,
          SCO_PLE8_14_REG_ID);
      if (ple8_14_reg == null) {
        throw new OBException("Internal Error Null");
      }
      Attachment attachment = OBDal.getInstance().get(Attachment.class, strcFileId);
      if (attachment == null) {
        throw new OBException("Internal Error Null");
      }

      System.out.println("ple8_14_reg:" + ple8_14_reg.getIdentifier());
      DecimalFormat df = new DecimalFormat("#0.00");
      DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
      otherSymbols.setDecimalSeparator('.');
      otherSymbols.setGroupingSeparator(',');
      df.setDecimalFormatSymbols(otherSymbols);

      String attachPath = OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .getProperty("attach.path");
      String payrollfilepath = attachPath + "/" + attachment.getPath() + "/" + attachment.getName();

      FileInputStream fstream = new FileInputStream(payrollfilepath);
      BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

      String strLine;
      // Read PayRoll File

      int seqno = -1;
      boolean seqnofound = false;
      while ((strLine = br.readLine()) != null) {
        entryno++;

        String[] parts = strLine.split("\\|", -1);

        // for (int ii = 0; ii < parts.length; ii++) {
        // System.out.print("'" + parts[ii] + "' - ");
        // }
        // System.out.print("\n");

        if (parts.length != 42) {
          System.out
              .println("incorrect: strLine: '" + strLine + "' - parts.length:" + parts.length);
          throw new OBException("@SCO_GenPayRollEntries_IncorrectEntryLen@ " + entryno);
        }

        String regnumber = parts[1];
        String sseqno = parts[2];
        if (regnumber.equals(ple8_14_reg.getSCORegnumber())) {
          seqno = Integer.parseInt(sseqno.replaceAll("[^\\d.]", ""));
          seqnofound = true;
          break;
        }

      }

      br.close();

      final OBError msg = new OBError();
      if (seqnofound) {
        ple8_14_reg.setSCOSeqno(new Long(seqno));
        OBDal.getInstance().save(ple8_14_reg);
        OBDal.getInstance().flush();

        msg.setType("Success");
        msg.setTitle("@Success@");
        msg.setMessage("@SCO_PLE8_14_GetRegNumberFound@ : " + seqno);
      } else {
        msg.setType("Error");
        msg.setTitle("@Error@");
        msg.setMessage("@SCO_PLE8_14_GetRegNumberNotFound@");
      }

      bundle.setResult(msg);
      OBDal.getInstance().commitAndClose();
    } catch (final OBException e) {
      final OBError msg = new OBError();
      msg.setType("Error");
      msg.setMessage(e.getMessage());
      msg.setTitle("@Error@");
      OBDal.getInstance().rollbackAndClose();
      bundle.setResult(msg);
    } catch (final Exception e) {
      OBDal.getInstance().rollbackAndClose();

      String message = DbUtility.getUnderlyingSQLException(e).getMessage();
      if (message.contains("@"))
        message = OBMessageUtils.parseTranslation(message);

      final OBError msg = new OBError();
      msg.setType("Error");
      msg.setMessage(message);
      msg.setTitle("@Error@");
      bundle.setResult(msg);
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}