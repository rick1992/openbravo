package pe.com.unifiedgo.core;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.ad.system.Client;
import org.openbravo.scheduling.ProcessBundle;

public class ExportClientSQLProcess implements org.openbravo.scheduling.Process {

  private static final Logger log = Logger.getLogger(ExportClientSQLProcess.class);

  public void execute(ProcessBundle bundle) throws Exception {
    try {
      final String clientId = (String) bundle.getParams().get("adClientId");

      if (clientId == null) {
        throw new OBException("Parameter adClientId not present, is the Client combo displayed in the window?");
      }

      log.debug("Exporting client " + clientId);

      Client client = OBDal.getInstance().get(Client.class, clientId);
      if (client == null) {
        final OBError msg = new OBError();
        msg.setType("Error");
        msg.setMessage("Client not found");
        msg.setTitle("Done with Errors");
        bundle.setResult(msg);
        return;
      }
      log.debug("Exporting client " + client.getName());
      System.out.println("Exporting client " + client.getName());
    } catch (final Exception e) {
      log.error(e.getMessage(), e);
      final OBError msg = new OBError();
      msg.setType("Error");
      msg.setMessage(e.getMessage());
      msg.setTitle("Done with Errors");
      bundle.setResult(msg);
    }
  }
}