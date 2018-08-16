package pe.com.unifiedgo.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.ClientImportProcessor;
import org.openbravo.service.db.DataImportService;
import org.openbravo.service.db.ImportResult;

/**
 * The import client process is called from the ui. It imports the data of a new client (including
 * the client itself). It again calls the {@link DataImportService} for the actual import.
 * 
 * @author mtaal
 */

public class ImportClientProcess implements org.openbravo.scheduling.Process {
  public static final String CLIENT_DATA_PREFIX = "client_data_";
  private static final Logger log = Logger.getLogger(ImportClientProcess.class);

  /**
   * Executes the import process. The expected parameters in the bundle are clientId (denoting the
   * client) and fileLocation giving the full path location of the file with the data to import.
   */
  public void execute(ProcessBundle bundle) throws Exception {

    try {
      final String name = (String) bundle.getParams().get("name");
      final String importAuditInfoStr = (String) bundle.getParams().get("importauditinfo");
      final boolean importAuditInfo = importAuditInfoStr != null && importAuditInfoStr.equalsIgnoreCase("Y");

      Query q = OBDal.getInstance().getSession().createSQLQuery("SELECT ad_client_id FROM ad_client WHERE name='" + name + "'");
      String ad_client_id = (String) q.uniqueResult();
      if (ad_client_id != null) {
        final OBError msg = new OBError();
        msg.setType("Error");
        msg.setMessage("A Client with name:" + name + " already exists. Please delete the client before importing it again.");
        msg.setTitle("Error occurred");
        bundle.setResult(msg);
        throw new OBException("A Client with name:" + name + " already exists. Please delete the client before importing it again.");

      }

      File import_file = getImportFile(name);
      log.debug("Importing file : " + import_file.getAbsolutePath());

      final ClientImportProcessor importProcessor = new ClientImportProcessor();
      importProcessor.setNewName("");
      final InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(import_file), "UTF-8");
      final ImportResult ir = DataImportService.getInstance().importClientData(importProcessor, importAuditInfo, inputStreamReader);
      inputStreamReader.close();
      if (ir.hasErrorOccured()) {
        final StringBuilder sb = new StringBuilder();
        if (ir.getException() != null) {
          log.error(ir.getException().getMessage(), ir.getException());
          sb.append(ir.getException().getMessage());
        }
        if (ir.getErrorMessages() != null) {
          log.debug(ir.getErrorMessages());
          if (sb.length() > 0) {
            sb.append("\n");
          }
          sb.append(ir.getErrorMessages());
        }
        final OBError msg = new OBError();
        msg.setType("Error");
        msg.setMessage(sb.toString());
        msg.setTitle("Errors occured");
        bundle.setResult(msg);
        return;
      }
      final OBError msg = new OBError();
      msg.setType("Success");

      if (ir.getWarningMessages() != null) {
        msg.setTitle("Done with messages");
        log.debug(ir.getWarningMessages());
        msg.setMessage("Imported client data with the following messages:<br/><ul><li>" + ir.getWarningMessages().replaceAll("\n", "</li><li>") + "</li></ul>");
      } else {
        msg.setTitle("Done");
        msg.setMessage("Imported client data");
      }
      bundle.setResult(msg);
    } catch (final Exception e) {
      log.error(e.getMessage(), e);
      final OBError msg = new OBError();
      msg.setType("Error");
      msg.setMessage(e.getMessage());
      msg.setTitle("Error occurred");
      bundle.setResult(msg);
    }
  }

  private File getImportFile(String name) {
    final File exportDir = ExportClientProcess.getExportDir();
    final File importDir = new File(exportDir, "importclient");
    File import_file = new File(importDir + "/" + CLIENT_DATA_PREFIX + name + ".xml");
    if (!import_file.exists()) {
      throw new OBException("Import filepath:" + import_file.getAbsolutePath() + " doesnt exist.");
    }
    return import_file;
  }
}
