package pe.com.unifiedgo.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.util.Check;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.ad.system.Client;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DataExportService;

/**
 * The export client process is called from the ui. It exports all the data from one client using a
 * specific dataset. It again calls the {@link DataExportService} for the actual export.
 * 
 * @author mtaal
 */

public class ExportClientProcess implements org.openbravo.scheduling.Process {

  /** The filename of the export file with client data. */
  public static final String CLIENT_DATA_PREFIX = "client_data_";

  /** The directory within WEB-INF in which the export file is placed. */
  public static final String EXPORT_DIR_NAME = "referencedata";

  /**
   * Returns the export file into which the xml is written or from the export can be read.
   */
  public static File getExportDir() {

    // determine the location where to place the file
    final String sourcePath = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty("source.path");
    Check.isNotNull(sourcePath, "The source.path property is not defined in the " + "Openbravo.properties file or the Openbravo.properties " + "file can not be found.");
    final File exportDir = new File(sourcePath, EXPORT_DIR_NAME);
    if (!exportDir.exists()) {
      log.debug("Exportdir " + exportDir.getAbsolutePath() + " does not exist, creating it");
      exportDir.mkdirs();
    }

    return exportDir;
  }

  private static final Logger log = Logger.getLogger(ExportClientProcess.class);

  /**
   * Executes the export process. The expected parameters in the bundle are clientId (denoting the
   * client) and fileLocation giving the full path location of the file in which the data for the
   * export should go.
   */
  public void execute(ProcessBundle bundle) throws Exception {
    try {
      final String clientId = (String) bundle.getParams().get("adClientId");
      final String exportAuditInfoStr = (String) bundle.getParams().get("exportauditinfo");
      final boolean exportAuditInfo = exportAuditInfoStr != null && exportAuditInfoStr.equalsIgnoreCase("Y");
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

      // setting parameter for querying
      final Map<String, Object> params = new HashMap<String, Object>();
      params.put(DataExportService.CLIENT_ID_PARAMETER_NAME, clientId);
      log.debug("Reading data from database into in-mem xml string");

      String client_data_filepath = CLIENT_DATA_PREFIX + client.getName() + ".xml";
      final File exportFile = new File(getExportDir(), client_data_filepath);
      final OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(exportFile), "UTF-8");
      // write the xml to a file in WEB-INF
      log.debug("Writing export file " + exportFile.getAbsolutePath());
      DataExportService.getInstance().exportClientToXML(params, exportAuditInfo, fw);
      fw.close();

      final OBError msg = new OBError();
      msg.setType("Success");
      msg.setMessage("Client " + client.getName() + " has been exported to " + exportFile.getAbsolutePath());
      msg.setTitle("Done");
      bundle.setResult(msg);

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